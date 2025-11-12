# Hướng Dẫn Tích Hợp API - PCM Desktop

## 📋 Tổng Quan

PCM Desktop cung cấp hệ thống tích hợp API mạnh mẽ và linh hoạt để gọi các dịch vụ LLM (Large Language Model) khác nhau như OpenAI GPT, Anthropic Claude, và Ollama. Hệ thống được thiết kế theo mô hình kiến trúc clean architecture với khả năng mở rộng và dễ bảo trì.

## 🏗️ Kiến Trúc Hệ Thống

```
📁 com.noteflix.pcm.llm/
├── 📂 api/                     # Interfaces và contracts
│   ├── LLMClient.java         # Interface cơ bản cho client
│   ├── StreamingCapable.java  # Interface cho streaming
│   ├── FunctionCallingCapable.java # Interface cho function calling
│   └── EmbeddingsCapable.java # Interface cho embeddings
├── 📂 client/                  # Implementations cho từng provider
│   ├── openai/OpenAIClient.java
│   ├── anthropic/AnthropicClient.java
│   └── ollama/OllamaClient.java
├── 📂 service/                # High-level services
│   └── LLMService.java        # Service chính
├── 📂 model/                  # Data models
│   ├── LLMRequest.java
│   ├── LLMResponse.java
│   ├── Message.java
│   └── LLMProviderConfig.java
└── 📂 factory/               # Factory pattern
    └── LLMClientFactory.java
```

## 🚀 Cách Sử Dụng

### 1. Cấu Hình Provider

#### OpenAI Configuration

```java
LLMProviderConfig openaiConfig = LLMProviderConfig.builder()
    .provider(LLMProviderConfig.Provider.OPENAI)
    .name("OpenAI GPT-4")
    .url("https://api.openai.com/v1/chat/completions")
    .token(System.getenv("OPENAI_API_KEY"))  // Đặt trong environment variable
    .model("gpt-4")
    .supportsStreaming(true)
    .supportsFunctionCalling(true)
    .timeout(30)
    .maxRetries(3)
    .build();
```

#### Anthropic Configuration

```java
LLMProviderConfig anthropicConfig = LLMProviderConfig.builder()
    .provider(LLMProviderConfig.Provider.ANTHROPIC)
    .name("Anthropic Claude")
    .url("https://api.anthropic.com/v1/messages")
    .token(System.getenv("ANTHROPIC_API_KEY"))
    .model("claude-3-opus-20240229")
    .supportsStreaming(true)
    .supportsFunctionCalling(false)
    .timeout(30)
    .maxRetries(3)
    .build();
```

#### Ollama Configuration (Self-hosted)

```java
LLMProviderConfig ollamaConfig = LLMProviderConfig.builder()
    .provider(LLMProviderConfig.Provider.OLLAMA)
    .name("Ollama Local")
    .url("http://localhost:11434/api/chat")
    .model("llama2")
    .supportsStreaming(true)
    .supportsFunctionCalling(false)
    .timeout(60)
    .maxRetries(2)
    .build();
```

### 2. Khởi Tạo LLMService

```java
// Initialize service
LLMService llmService = new LLMService();

// Add providers
llmService.addProvider(openaiConfig);
llmService.addProvider(anthropicConfig);
llmService.addProvider(ollamaConfig);

// Set default provider
llmService.setDefaultProvider("OpenAI GPT-4");
```

### 3. Gọi API Cơ Bản

```java
// Tạo request
LLMRequest request = LLMRequest.builder()
    .messages(List.of(
        Message.system("You are a helpful assistant"),
        Message.user("Explain quantum computing in simple terms")
    ))
    .temperature(0.7)
    .maxTokens(1000)
    .build();

// Gọi API
CompletableFuture<LLMResponse> future = llmService.callLLM(request);

// Xử lý response
future.thenAccept(response -> {
    if (response.isSuccess()) {
        System.out.println("Response: " + response.getContent());
    } else {
        System.err.println("Error: " + response.getErrorMessage());
    }
});
```

### 4. Streaming Response

```java
// Tạo request với streaming
LLMRequest streamingRequest = LLMRequest.builder()
    .messages(List.of(Message.user("Write a short story about AI")))
    .temperature(0.8)
    .maxTokens(500)
    .stream(true)
    .build();

// Stream response với callback
llmService.streamLLM(streamingRequest, new StreamCallback() {
    @Override
    public void onToken(String token) {
        System.out.print(token);
    }
    
    @Override
    public void onComplete(LLMResponse response) {
        System.out.println("\n\nStream completed successfully");
    }
    
    @Override
    public void onError(Throwable error) {
        System.err.println("Stream error: " + error.getMessage());
    }
});
```

### 5. Function Calling (OpenAI)

```java
// Định nghĩa function
Function getWeatherFunction = Function.builder()
    .name("get_weather")
    .description("Get weather information for a location")
    .parameter("location", "string", "The location to get weather for")
    .parameter("unit", "string", "Temperature unit (celsius/fahrenheit)")
    .build();

// Request với function calling
LLMRequest functionRequest = LLMRequest.builder()
    .messages(List.of(Message.user("What's the weather like in San Francisco?")))
    .functions(List.of(getWeatherFunction))
    .functionCall("auto")
    .build();

// Gọi API và xử lý function calls
CompletableFuture<LLMResponse> future = llmService.callLLM(functionRequest);
future.thenAccept(response -> {
    if (response.getFunctionCall() != null) {
        FunctionCall call = response.getFunctionCall();
        System.out.println("Function called: " + call.getName());
        System.out.println("Arguments: " + call.getArguments());
        
        // Thực hiện function call và trả response
        String weatherData = getActualWeather(call.getArguments());
        
        // Gửi lại với function result
        LLMRequest followUpRequest = LLMRequest.builder()
            .messages(List.of(
                Message.user("What's the weather like in San Francisco?"),
                Message.assistant("", call),
                Message.function(call.getName(), weatherData)
            ))
            .build();
            
        llmService.callLLM(followUpRequest).thenAccept(finalResponse -> {
            System.out.println("Final response: " + finalResponse.getContent());
        });
    }
});
```

## 🔧 Configuration Management

### Environment Variables

```bash
# API Keys
export OPENAI_API_KEY="sk-..."
export ANTHROPIC_API_KEY="sk-..."

# Optional configurations
export LLM_DEFAULT_PROVIDER="OpenAI GPT-4"
export LLM_REQUEST_TIMEOUT="30"
export LLM_MAX_RETRIES="3"
```

### Configuration File (config/llm-config.json)

```json
{
  "providers": [
    {
      "name": "OpenAI GPT-4",
      "provider": "OPENAI",
      "url": "https://api.openai.com/v1/chat/completions",
      "model": "gpt-4",
      "supportsStreaming": true,
      "supportsFunctionCalling": true,
      "timeout": 30,
      "maxRetries": 3
    },
    {
      "name": "Anthropic Claude",
      "provider": "ANTHROPIC", 
      "url": "https://api.anthropic.com/v1/messages",
      "model": "claude-3-opus-20240229",
      "supportsStreaming": true,
      "supportsFunctionCalling": false,
      "timeout": 30,
      "maxRetries": 3
    }
  ],
  "defaultProvider": "OpenAI GPT-4",
  "globalTimeout": 30,
  "globalMaxRetries": 3,
  "enableLogging": true,
  "logLevel": "INFO"
}
```

## 🔐 Security Best Practices

### 1. API Key Management

```java
// ❌ Không làm như này
String apiKey = "sk-hardcoded-key-here";

// ✅ Làm như này
String apiKey = System.getenv("OPENAI_API_KEY");
if (apiKey == null || apiKey.isEmpty()) {
    throw new IllegalStateException("OPENAI_API_KEY environment variable not set");
}
```

### 2. Rate Limiting

```java
LLMProviderConfig config = LLMProviderConfig.builder()
    .provider(LLMProviderConfig.Provider.OPENAI)
    .rateLimit(10) // 10 requests per minute
    .timeout(30)
    .build();
```

### 3. Request Validation

```java
// Validate input before sending
public class RequestValidator {
    public static void validateRequest(LLMRequest request) {
        if (request.getMessages().isEmpty()) {
            throw new IllegalArgumentException("Request must contain at least one message");
        }
        
        // Check for potential sensitive data
        for (Message message : request.getMessages()) {
            if (containsSensitiveData(message.getContent())) {
                throw new SecurityException("Request contains potentially sensitive data");
            }
        }
    }
    
    private static boolean containsSensitiveData(String content) {
        // Implement your sensitivity checks
        return content.matches(".*\\b\\d{4}\\b.*"); // Simple credit card check
    }
}
```

## 🚨 Error Handling

### 1. Automatic Retry Logic

```java
public class RetryableRequest {
    private final int maxRetries;
    private final long retryDelay;
    
    public CompletableFuture<LLMResponse> executeWithRetry(LLMRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            int attempts = 0;
            Exception lastException = null;
            
            while (attempts < maxRetries) {
                try {
                    return llmService.callLLM(request).get();
                } catch (Exception e) {
                    lastException = e;
                    attempts++;
                    
                    if (attempts < maxRetries) {
                        try {
                            Thread.sleep(retryDelay * attempts); // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            
            throw new RuntimeException("Failed after " + maxRetries + " attempts", lastException);
        });
    }
}
```

### 2. Circuit Breaker Pattern

```java
public class LLMCircuitBreaker {
    private AtomicInteger failureCount = new AtomicInteger(0);
    private volatile long lastFailureTime = 0;
    private final int threshold = 5;
    private final long timeout = 60000; // 1 minute
    
    public enum State { CLOSED, OPEN, HALF_OPEN }
    private volatile State state = State.CLOSED;
    
    public CompletableFuture<LLMResponse> execute(LLMRequest request) {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > timeout) {
                state = State.HALF_OPEN;
            } else {
                return CompletableFuture.failedFuture(
                    new RuntimeException("Circuit breaker is OPEN"));
            }
        }
        
        return llmService.callLLM(request)
            .thenApply(response -> {
                onSuccess();
                return response;
            })
            .exceptionally(throwable -> {
                onFailure();
                throw new RuntimeException(throwable);
            });
    }
    
    private void onSuccess() {
        failureCount.set(0);
        state = State.CLOSED;
    }
    
    private void onFailure() {
        int failures = failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        
        if (failures >= threshold) {
            state = State.OPEN;
        }
    }
}
```

## 📊 Monitoring và Logging

### 1. Request/Response Logging

```java
public class LLMLogger {
    private static final Logger logger = LoggerFactory.getLogger(LLMLogger.class);
    
    public static void logRequest(LLMRequest request, String provider) {
        logger.info("LLM Request - Provider: {}, Model: {}, Messages: {}", 
            provider, request.getModel(), request.getMessages().size());
        
        if (logger.isDebugEnabled()) {
            logger.debug("Request details: {}", request.toString());
        }
    }
    
    public static void logResponse(LLMResponse response, long duration) {
        if (response.isSuccess()) {
            logger.info("LLM Response - Success, Duration: {}ms, Tokens: {}", 
                duration, response.getUsage().getTotalTokens());
        } else {
            logger.error("LLM Response - Error: {}, Duration: {}ms", 
                response.getErrorMessage(), duration);
        }
    }
}
```

### 2. Metrics Collection

```java
public class LLMMetrics {
    private final Counter requestCounter = Counter.build()
        .name("llm_requests_total")
        .help("Total LLM requests")
        .labelNames("provider", "model", "status")
        .register();
        
    private final Histogram requestDuration = Histogram.build()
        .name("llm_request_duration_seconds")
        .help("LLM request duration")
        .labelNames("provider", "model")
        .register();
    
    public void recordRequest(String provider, String model, String status, double duration) {
        requestCounter.labels(provider, model, status).inc();
        requestDuration.labels(provider, model).observe(duration);
    }
}
```

## 🧪 Testing

### 1. Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class LLMServiceTest {
    
    @Mock
    private LLMClient mockClient;
    
    @InjectMocks
    private LLMService llmService;
    
    @Test
    void shouldCallLLMSuccessfully() {
        // Given
        LLMRequest request = LLMRequest.builder()
            .messages(List.of(Message.user("test")))
            .build();
            
        LLMResponse expectedResponse = LLMResponse.builder()
            .content("test response")
            .success(true)
            .build();
            
        when(mockClient.call(request)).thenReturn(CompletableFuture.completedFuture(expectedResponse));
        
        // When
        CompletableFuture<LLMResponse> result = llmService.callLLM(request);
        
        // Then
        assertThat(result).succeedsWithin(Duration.ofSeconds(1));
        assertThat(result.join().getContent()).isEqualTo("test response");
    }
}
```

### 2. Integration Tests

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LLMIntegrationTest {
    
    private LLMService llmService;
    
    @BeforeAll
    void setup() {
        // Use test API keys or mock servers
        LLMProviderConfig config = LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .token(System.getenv("OPENAI_TEST_API_KEY"))
            .model("gpt-3.5-turbo")
            .build();
            
        llmService = new LLMService();
        llmService.addProvider(config);
    }
    
    @Test
    void shouldHandleRealApiCall() {
        LLMRequest request = LLMRequest.builder()
            .messages(List.of(Message.user("Say 'Hello World'")))
            .maxTokens(10)
            .build();
            
        CompletableFuture<LLMResponse> result = llmService.callLLM(request);
        
        assertThat(result).succeedsWithin(Duration.ofSeconds(30));
        assertThat(result.join().isSuccess()).isTrue();
        assertThat(result.join().getContent()).contains("Hello");
    }
}
```

## 📚 Examples và Use Cases

### 1. Code Generation

```java
public class CodeGenerator {
    
    public CompletableFuture<String> generateJavaClass(String className, String description) {
        LLMRequest request = LLMRequest.builder()
            .messages(List.of(
                Message.system("You are a Java code generator. Generate clean, documented Java code."),
                Message.user(String.format(
                    "Generate a Java class named '%s' that %s. Include JavaDoc comments.",
                    className, description
                ))
            ))
            .temperature(0.1) // Low temperature for consistent code
            .maxTokens(1000)
            .build();
            
        return llmService.callLLM(request)
            .thenApply(response -> extractCodeFromResponse(response.getContent()));
    }
    
    private String extractCodeFromResponse(String response) {
        // Extract code block from markdown
        Pattern codePattern = Pattern.compile("```java\\n(.*?)\\n```", Pattern.DOTALL);
        Matcher matcher = codePattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return response; // Return as-is if no code block found
    }
}
```

### 2. Text Analysis

```java
public class TextAnalyzer {
    
    public CompletableFuture<Map<String, Object>> analyzeText(String text) {
        String prompt = """
            Analyze the following text and return a JSON response with:
            - sentiment: positive/negative/neutral
            - topics: array of main topics
            - complexity: simple/medium/complex
            - word_count: number of words
            
            Text: %s
            """.formatted(text);
            
        LLMRequest request = LLMRequest.builder()
            .messages(List.of(Message.user(prompt)))
            .temperature(0.0)
            .build();
            
        return llmService.callLLM(request)
            .thenApply(response -> parseJsonResponse(response.getContent()));
    }
    
    private Map<String, Object> parseJsonResponse(String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }
}
```

## 🔄 Advanced Features

### 1. Context Management

```java
public class ConversationManager {
    private final List<Message> conversationHistory = new ArrayList<>();
    private final int maxHistorySize = 10;
    
    public CompletableFuture<LLMResponse> continueConversation(String userMessage) {
        // Add user message to history
        conversationHistory.add(Message.user(userMessage));
        
        // Trim history if too long
        if (conversationHistory.size() > maxHistorySize) {
            conversationHistory.subList(0, conversationHistory.size() - maxHistorySize).clear();
        }
        
        LLMRequest request = LLMRequest.builder()
            .messages(new ArrayList<>(conversationHistory))
            .build();
            
        return llmService.callLLM(request)
            .thenApply(response -> {
                // Add assistant response to history
                if (response.isSuccess()) {
                    conversationHistory.add(Message.assistant(response.getContent()));
                }
                return response;
            });
    }
}
```

### 2. Template System

```java
public class PromptTemplate {
    private final String template;
    private final Map<String, String> variables = new HashMap<>();
    
    public PromptTemplate(String template) {
        this.template = template;
    }
    
    public PromptTemplate setVariable(String key, String value) {
        variables.put(key, value);
        return this;
    }
    
    public String render() {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}

// Usage
PromptTemplate template = new PromptTemplate(
    "Analyze the {{document_type}} document about {{topic}} and provide {{analysis_type}} analysis."
);

String prompt = template
    .setVariable("document_type", "technical specification")
    .setVariable("topic", "API design")
    .setVariable("analysis_type", "detailed")
    .render();
```

---

## 🔗 References

- [OpenAI API Documentation](https://platform.openai.com/docs/api-reference)
- [Anthropic Claude API](https://docs.anthropic.com/claude/reference)
- [Ollama API Documentation](https://github.com/ollama/ollama/blob/main/docs/api.md)
- [Java CompletableFuture Guide](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html)

---

*Được tạo bởi PCM Desktop Development Team*