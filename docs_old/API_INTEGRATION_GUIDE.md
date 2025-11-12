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

#### Anthropic Claude Configuration

```java
LLMProviderConfig claudeConfig = LLMProviderConfig.builder()
    .provider(LLMProviderConfig.Provider.ANTHROPIC)
    .name("Claude 3.5 Sonnet")
    .url("https://api.anthropic.com/v1/messages")
    .token(System.getenv("ANTHROPIC_API_KEY"))
    .model("claude-3-5-sonnet-20241022")
    .headers(Map.of("anthropic-version", "2023-06-01"))
    .supportsStreaming(true)
    .supportsFunctionCalling(true)
    .timeout(45)
    .build();
```

#### Ollama Local Configuration

```java
LLMProviderConfig ollamaConfig = LLMProviderConfig.builder()
    .provider(LLMProviderConfig.Provider.OLLAMA)
    .name("Local Llama 3")
    .url("http://localhost:11434/api/chat")
    .model("llama3")
    .supportsStreaming(true)
    .supportsFunctionCalling(false)
    .timeout(60)
    .build();
```

### 2. Khởi Tạo Service

```java
// Tạo và khởi tạo LLMService
LLMService llmService = new LLMService();
llmService.initialize(openaiConfig);
```

### 3. Chat Đơn Giản

```java
public class SimpleChatExample {
    public void basicChat() {
        try {
            // Gửi tin nhắn đơn giản
            String response = llmService.chat("Hãy giải thích về Java Streams");
            System.out.println("AI Response: " + response);
            
        } catch (Exception e) {
            log.error("Lỗi khi gọi API: {}", e.getMessage());
        }
    }
}
```

### 4. Chat Với Cấu Hình Chi Tiết

```java
public class DetailedChatExample {
    public void detailedChat() {
        // Tạo request với cấu hình chi tiết
        LLMRequest request = LLMRequest.builder()
            .model("gpt-4")
            .messages(List.of(
                Message.system("Bạn là một chuyên gia Java programming."),
                Message.user("Hãy viết ví dụ về Singleton pattern với thread-safety")
            ))
            .temperature(0.7)        // Độ sáng tạo (0-2)
            .maxTokens(1000)        // Giới hạn số token response
            .topP(0.9)              // Nucleus sampling
            .frequencyPenalty(0.0)  // Penalty cho từ lặp lại
            .presencePenalty(0.0)   // Penalty cho chủ đề lặp lại
            .build();
        
        try {
            LLMResponse response = llmService.sendMessage(request);
            
            // Truy cập thông tin chi tiết
            System.out.println("Content: " + response.getContent());
            System.out.println("Model: " + response.getModel());
            System.out.println("Tokens Used: " + response.getUsage().getTotalTokens());
            System.out.println("Finish Reason: " + response.getFinishReason());
            
        } catch (Exception e) {
            log.error("Lỗi API call: {}", e.getMessage());
        }
    }
}
```

### 5. Cuộc Trò Chuyện Nhiều Lượt

```java
public class ConversationExample {
    public void multiTurnConversation() {
        // Sử dụng ConversationBuilder để quản lý ngữ cảnh
        LLMService.ConversationBuilder conversation = llmService.newConversation()
            .addSystemMessage("Bạn là trợ lý lập trình Java chuyên nghiệp.")
            .addUserMessage("Tôi cần tạo một REST API với Spring Boot")
            .temperature(0.8)
            .maxTokens(800);
        
        try {
            // Lượt 1
            LLMResponse response1 = conversation.send();
            System.out.println("AI: " + response1.getContent());
            
            // Lượt 2 - tiếp tục cuộc trò chuyện
            conversation.addUserMessage("Làm sao để thêm authentication với JWT?");
            LLMResponse response2 = conversation.send();
            System.out.println("AI: " + response2.getContent());
            
            // Lượt 3
            conversation.addUserMessage("Có thể show code example không?");
            LLMResponse response3 = conversation.send();
            System.out.println("AI: " + response3.getContent());
            
            // Xem toàn bộ lịch sử cuộc trò chuyện
            List<Message> history = conversation.getMessages();
            System.out.println("Conversation has " + history.size() + " messages");
            
        } catch (Exception e) {
            log.error("Lỗi conversation: {}", e.getMessage());
        }
    }
}
```

### 6. Streaming Response (Phản Hồi Theo Thời Gian Thực)

```java
public class StreamingExample {
    public void streamingChat() {
        LLMRequest streamRequest = LLMRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(List.of(Message.user("Viết một câu chuyện ngắn về AI")))
            .stream(true)
            .build();
        
        // Cách 1: Sử dụng Observer Pattern
        llmService.streamMessage(streamRequest, new StreamingObserver() {
            @Override
            public void onChunk(LLMChunk chunk) {
                // Hiển thị từng chunk khi nhận được
                System.out.print(chunk.getContent());
                System.out.flush();
            }
            
            @Override
            public void onComplete() {
                System.out.println("\\n[Stream completed]");
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.println("Stream error: " + error.getMessage());
            }
        });
        
        // Cách 2: Sử dụng Java Stream API
        try {
            Stream<LLMChunk> stream = llmService.streamMessage(streamRequest);
            stream.forEach(chunk -> {
                System.out.print(chunk.getContent());
                System.out.flush();
            });
        } catch (Exception e) {
            log.error("Streaming error: {}", e.getMessage());
        }
    }
}
```

### 7. Function Calling (Gọi Hàm)

```java
public class FunctionCallingExample {
    public void functionCallingDemo() {
        // Định nghĩa function
        FunctionDefinition weatherFunction = FunctionDefinition.builder()
            .name("get_weather")
            .description("Lấy thông tin thời tiết hiện tại của một thành phố")
            .parameters(Map.of(
                "type", "object",
                "properties", Map.of(
                    "city", Map.of(
                        "type", "string",
                        "description", "Tên thành phố, ví dụ: 'Hà Nội', 'TP.HCM'"
                    ),
                    "unit", Map.of(
                        "type", "string",
                        "enum", List.of("celsius", "fahrenheit"),
                        "description", "Đơn vị nhiệt độ"
                    )
                ),
                "required", List.of("city")
            ))
            .build();
        
        LLMRequest request = LLMRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(List.of(
                Message.user("Thời tiết ở Hà Nội hôm nay như thế nào?")
            ))
            .build();
        
        try {
            LLMResponse response = llmService.sendWithFunctions(
                request, 
                List.of(weatherFunction)
            );
            
            if (response.hasFunctionCall()) {
                FunctionCall call = response.getFunctionCall();
                System.out.println("AI muốn gọi function: " + call.getName());
                System.out.println("Với tham số: " + call.getArguments());
                
                // Thực hiện function call thật
                String weatherResult = callWeatherAPI(call.getArguments());
                
                // Gửi kết quả lại cho AI
                LLMRequest followUpRequest = LLMRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(List.of(
                        Message.user("Thời tiết ở Hà Nội hôm nay như thế nào?"),
                        Message.assistant("", call), // Message với function call
                        Message.function(call.getName(), weatherResult)
                    ))
                    .build();
                
                LLMResponse finalResponse = llmService.sendMessage(followUpRequest);
                System.out.println("Kết quả cuối: " + finalResponse.getContent());
                
            } else {
                System.out.println("AI Response: " + response.getContent());
            }
            
        } catch (Exception e) {
            log.error("Function calling error: {}", e.getMessage());
        }
    }
    
    private String callWeatherAPI(String arguments) {
        // Mock implementation - trong thực tế sẽ gọi API thời tiết thật
        return "{\\"temperature\\": 28, \\"condition\\": \\"sunny\\", \\"humidity\\": 65}";
    }
}
```

### 8. Chuyển Đổi Provider

```java
public class ProviderSwitchingExample {
    public void switchProviders() {
        LLMService service = new LLMService();
        
        // Bắt đầu với OpenAI
        LLMProviderConfig openaiConfig = LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .url("https://api.openai.com/v1/chat/completions")
            .token(System.getenv("OPENAI_API_KEY"))
            .model("gpt-3.5-turbo")
            .build();
        
        service.initialize(openaiConfig);
        String response1 = service.chat("Hello from OpenAI!");
        System.out.println("OpenAI: " + response1);
        
        // Chuyển sang Anthropic
        LLMProviderConfig claudeConfig = LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.ANTHROPIC)
            .url("https://api.anthropic.com/v1/messages")
            .token(System.getenv("ANTHROPIC_API_KEY"))
            .model("claude-3-5-sonnet-20241022")
            .build();
        
        service.switchProvider(claudeConfig);
        String response2 = service.chat("Hello from Claude!");
        System.out.println("Claude: " + response2);
        
        // Kiểm tra khả năng của provider hiện tại
        System.out.println("Current provider: " + service.getCurrentProvider());
        System.out.println("Supports streaming: " + service.supportsStreaming());
        System.out.println("Supports function calling: " + service.supportsFunctionCalling());
    }
}
```

## 🔧 Configuration Management

### 1. Environment Variables

Tạo file `.env` hoặc đặt trong system environment:

```bash
# OpenAI
OPENAI_API_KEY=sk-your-openai-key-here
OPENAI_MODEL=gpt-4

# Anthropic
ANTHROPIC_API_KEY=sk-ant-your-anthropic-key-here
ANTHROPIC_MODEL=claude-3-5-sonnet-20241022

# Custom endpoints
CUSTOM_LLM_URL=https://your-custom-api.com/v1/chat
CUSTOM_LLM_TOKEN=your-custom-token
```

### 2. Configuration File

Tạo file `llm-config.json`:

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
      "name": "Claude 3.5 Sonnet", 
      "provider": "ANTHROPIC",
      "url": "https://api.anthropic.com/v1/messages",
      "model": "claude-3-5-sonnet-20241022",
      "headers": {
        "anthropic-version": "2023-06-01"
      },
      "supportsStreaming": true,
      "supportsFunctionCalling": true,
      "timeout": 45
    },
    {
      "name": "Local Ollama",
      "provider": "OLLAMA", 
      "url": "http://localhost:11434/api/chat",
      "model": "llama3",
      "supportsStreaming": true,
      "supportsFunctionCalling": false,
      "timeout": 60
    }
  ]
}
```

### 3. Configuration Loader

```java
public class ConfigurationManager {
    private static final String CONFIG_FILE = "llm-config.json";
    
    public static List<LLMProviderConfig> loadConfigurations() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(CONFIG_FILE));
            JsonNode providers = root.get("providers");
            
            List<LLMProviderConfig> configs = new ArrayList<>();
            for (JsonNode provider : providers) {
                LLMProviderConfig config = LLMProviderConfig.builder()
                    .provider(LLMProviderConfig.Provider.valueOf(
                        provider.get("provider").asText()
                    ))
                    .name(provider.get("name").asText())
                    .url(provider.get("url").asText())
                    .token(getTokenFromEnv(provider.get("provider").asText()))
                    .model(provider.get("model").asText())
                    .supportsStreaming(provider.get("supportsStreaming").asBoolean())
                    .supportsFunctionCalling(provider.get("supportsFunctionCalling").asBoolean())
                    .timeout(provider.get("timeout").asInt())
                    .maxRetries(provider.path("maxRetries").asInt(3))
                    .build();
                
                configs.add(config);
            }
            
            return configs;
            
        } catch (Exception e) {
            log.error("Error loading configuration: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private static String getTokenFromEnv(String provider) {
        switch (provider) {
            case "OPENAI": return System.getenv("OPENAI_API_KEY");
            case "ANTHROPIC": return System.getenv("ANTHROPIC_API_KEY");
            case "OLLAMA": return ""; // No token needed
            default: return System.getenv("CUSTOM_LLM_TOKEN");
        }
    }
}
```

## 🔒 Xử Lý Lỗi và Bảo Mật

### 1. Exception Handling

```java
public class ErrorHandlingExample {
    public void robustAPICall() {
        try {
            String response = llmService.chat("Test message");
            System.out.println("Success: " + response);
            
        } catch (LLMProviderException e) {
            // Lỗi từ provider (API key sai, model không tồn tại, v.v.)
            log.error("Provider error: {}", e.getMessage());
            System.err.println("Lỗi từ nhà cung cấp: " + e.getMessage());
            
        } catch (StreamingException e) {
            // Lỗi trong quá trình streaming
            log.error("Streaming error: {}", e.getMessage());
            System.err.println("Lỗi streaming: " + e.getMessage());
            
        } catch (LLMException e) {
            // Lỗi chung từ hệ thống LLM
            log.error("LLM error: {}", e.getMessage());
            System.err.println("Lỗi hệ thống LLM: " + e.getMessage());
            
        } catch (Exception e) {
            // Lỗi không mong đợi khác
            log.error("Unexpected error: {}", e.getMessage());
            System.err.println("Lỗi không xác định: " + e.getMessage());
        }
    }
}
```

### 2. Rate Limiting

```java
public class RateLimitingExample {
    public void implementRateLimit() {
        // Sử dụng middleware cho rate limiting
        LLMService service = new LLMService();
        
        // Giới hạn 10 requests/minute
        RateLimiter rateLimiter = new RateLimiter(10, Duration.ofMinutes(1));
        
        for (int i = 0; i < 15; i++) {
            try {
                if (rateLimiter.allowRequest()) {
                    String response = service.chat("Message " + i);
                    System.out.println("Response " + i + ": " + response);
                } else {
                    System.out.println("Rate limit exceeded, waiting...");
                    Thread.sleep(1000);
                    i--; // Retry
                }
            } catch (Exception e) {
                log.error("Error in request {}: {}", i, e.getMessage());
            }
        }
    }
}
```

### 3. Retry Logic

```java
public class RetryExample {
    public void implementRetry() {
        RetryPolicy retryPolicy = new RetryPolicy(
            3,              // max attempts  
            Duration.ofSeconds(1),  // initial delay
            2.0             // backoff multiplier
        );
        
        try {
            String response = retryPolicy.execute(() -> {
                return llmService.chat("Important message that must succeed");
            });
            
            System.out.println("Success after retries: " + response);
            
        } catch (Exception e) {
            log.error("Failed after all retry attempts: {}", e.getMessage());
        }
    }
}
```

## 📊 Monitoring và Logging

### 1. Request Logging

```java
public class RequestLoggingExample {
    public void enableRequestLogging() {
        // Enable request/response logging
        RequestLogger logger = new RequestLogger();
        
        LLMService service = new LLMService();
        service.addMiddleware(logger);
        
        // All requests will now be logged
        String response = service.chat("Test message");
        
        // Log output:
        // [REQUEST] POST https://api.openai.com/v1/chat/completions
        // [REQUEST BODY] {"model":"gpt-3.5-turbo","messages":[...]}
        // [RESPONSE] 200 OK (1.2s)
        // [RESPONSE BODY] {"choices":[...]}
    }
}
```

### 2. Metrics Collection

```java
public class MetricsExample {
    private final MeterRegistry meterRegistry = Metrics.globalRegistry;
    
    public void collectMetrics() {
        // Counter cho số lượng requests
        Counter requestCounter = Counter.builder("llm.requests.total")
            .description("Total number of LLM requests")
            .tag("provider", "openai")
            .register(meterRegistry);
        
        // Timer cho response time
        Timer responseTimer = Timer.builder("llm.response.time")
            .description("LLM response time")
            .register(meterRegistry);
        
        // Gauge cho số tokens sử dụng
        AtomicInteger tokensUsed = new AtomicInteger(0);
        Gauge.builder("llm.tokens.used")
            .description("Total tokens used")
            .register(meterRegistry, tokensUsed, AtomicInteger::get);
        
        // Sử dụng metrics
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            LLMResponse response = llmService.sendMessage(request);
            requestCounter.increment();
            tokensUsed.addAndGet(response.getUsage().getTotalTokens());
        } finally {
            sample.stop(responseTimer);
        }
    }
}
```

## 🧪 Testing

### 1. Unit Tests

```java
@TestMethodOrder(OrderAnnotation.class)
public class LLMServiceTest {
    
    @Mock
    private LLMClient mockClient;
    
    @InjectMocks 
    private LLMService llmService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @Order(1)
    void testBasicChat() {
        // Given
        String userMessage = "Hello";
        String expectedResponse = "Hi there!";
        
        LLMResponse mockResponse = LLMResponse.builder()
            .content(expectedResponse)
            .model("gpt-3.5-turbo")
            .build();
            
        when(mockClient.sendMessage(any(LLMRequest.class)))
            .thenReturn(mockResponse);
        
        // When
        String actualResponse = llmService.chat(userMessage);
        
        // Then
        assertEquals(expectedResponse, actualResponse);
        verify(mockClient, times(1)).sendMessage(any(LLMRequest.class));
    }
    
    @Test
    @Order(2)
    void testStreamingSupport() {
        // Given
        StreamingCapable streamingClient = mock(StreamingCapable.class);
        LLMChunk chunk1 = LLMChunk.builder().content("Hello").build();
        LLMChunk chunk2 = LLMChunk.builder().content(" World").build();
        
        when(streamingClient.streamMessage(any(LLMRequest.class)))
            .thenReturn(Stream.of(chunk1, chunk2));
        
        // When
        Stream<LLMChunk> stream = ((StreamingCapable) streamingClient)
            .streamMessage(LLMRequest.builder().build());
        
        // Then
        String result = stream
            .map(LLMChunk::getContent)
            .collect(Collectors.joining());
        assertEquals("Hello World", result);
    }
}
```

### 2. Integration Tests

```java
@SpringBootTest
@TestPropertySource(properties = {
    "llm.openai.api-key=test-key",
    "llm.openai.model=gpt-3.5-turbo"
})
public class LLMIntegrationTest {
    
    @Autowired
    private LLMService llmService;
    
    @Test
    @EnabledIf("#{environment['OPENAI_API_KEY'] != null}")
    void testRealOpenAIIntegration() {
        // Chỉ chạy khi có API key thật
        LLMProviderConfig config = LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .url("https://api.openai.com/v1/chat/completions")
            .token(System.getenv("OPENAI_API_KEY"))
            .model("gpt-3.5-turbo")
            .build();
        
        llmService.initialize(config);
        
        String response = llmService.chat("Say 'Integration test successful'");
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.toLowerCase().contains("integration"));
    }
}
```

## 📝 Best Practices

### 1. API Key Security
- ✅ Luôn lưu API keys trong environment variables
- ✅ Không commit API keys vào code repository  
- ✅ Sử dụng secret management tools trong production
- ✅ Rotate API keys định kỳ

### 2. Error Handling
- ✅ Implement retry logic với exponential backoff
- ✅ Set reasonable timeouts
- ✅ Log errors với đủ context để debug
- ✅ Có fallback mechanisms khi API không khả dụng

### 3. Performance
- ✅ Cache responses khi có thể
- ✅ Implement connection pooling
- ✅ Monitor API usage và costs
- ✅ Sử dụng streaming cho responses dài

### 4. Cost Optimization
- ✅ Monitor token usage
- ✅ Sử dụng model phù hợp cho từng use case
- ✅ Implement usage limits
- ✅ Cache expensive operations

## 🔗 Tài Liệu Tham Khảo

- [OpenAI API Documentation](https://platform.openai.com/docs/api-reference)
- [Anthropic API Documentation](https://docs.anthropic.com/claude/reference)
- [Ollama API Documentation](https://github.com/ollama/ollama/blob/main/docs/api.md)

## 📞 Hỗ Trợ

Nếu gặp vấn đề khi sử dụng API integration:

1. Kiểm tra logs trong `logs/pcm-desktop.log`
2. Verify API keys và configuration
3. Test connectivity tới API endpoints
4. Tham khảo code examples trong `com.noteflix.pcm.llm.examples`

---

*Tài liệu này được cập nhật thường xuyên. Vui lòng check phiên bản mới nhất trên repository.*