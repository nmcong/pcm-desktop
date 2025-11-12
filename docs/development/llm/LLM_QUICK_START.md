# LLM Integration - Quick Start Guide

## 🚀 Quick Overview

This guide helps you quickly understand and use the LLM integration system.

---

## 📁 Project Structure

```
src/main/java/com/noteflix/pcm/llm/
├── api/                           # Interfaces
│   ├── LLMClient.java            ✅ Base interface (all providers)
│   ├── StreamingCapable.java    ✅ Optional: Streaming support
│   └── FunctionCallingCapable.java ✅ Optional: Function calling
│
├── model/                         # Data models
│   ├── LLMRequest.java           ✅ Request model
│   ├── LLMResponse.java          ✅ Response model
│   ├── Message.java              ✅ Chat message
│   ├── LLMChunk.java             ✅ Streaming chunk
│   ├── FunctionDefinition.java  ✅ Function definition
│   ├── FunctionCall.java         ✅ Function call request
│   ├── StreamingObserver.java   ✅ Streaming callback
│   └── LLMProviderConfig.java   ✅ Provider config
│
├── client/                        # Provider implementations
│   ├── openai/                   ⏳ TODO
│   ├── anthropic/                ⏳ TODO
│   ├── ollama/                   ⏳ TODO
│   └── custom/                   ⏳ TODO
│
├── factory/                       # Factory
│   └── LLMClientFactory.java    ⏳ TODO
│
└── service/                       # Services
    └── LLMService.java           ⏳ TODO
```

---

## 🎯 Core Concepts

### 1. **Provider Interface (LLMClient)**

All LLM providers implement this base interface:

```java
public interface LLMClient {
    LLMResponse sendMessage(LLMRequest request);
    String getProviderName();
    boolean isAvailable();
    String getModel();
}
```

### 2. **Optional Capabilities**

Providers can implement optional interfaces:

```java
// For streaming support
public interface StreamingCapable {
    Stream<LLMChunk> streamMessage(LLMRequest request);
    void streamMessage(LLMRequest request, StreamingObserver observer);
}

// For function calling support
public interface FunctionCallingCapable {
    LLMResponse sendWithFunctions(LLMRequest request, List<FunctionDefinition> functions);
    boolean supportsFunctionCalling();
}
```

### 3. **Configuration**

Each provider needs configuration:

```java
LLMProviderConfig config = LLMProviderConfig.builder()
    .provider(Provider.OPENAI)
    .name("OpenAI GPT-4")
    .url("https://api.openai.com/v1/chat/completions")
    .token(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
    .supportsStreaming(true)
    .supportsFunctionCalling(true)
    .build();
```

---

## 💻 Usage Examples

### Example 1: Basic Chat

```java
// Build request
LLMRequest request = LLMRequest.builder()
    .model("gpt-4")
    .messages(List.of(
        Message.user("What is Java?")
    ))
    .temperature(0.7)
    .maxTokens(500)
    .build();

// Send request
LLMResponse response = client.sendMessage(request);

// Get response
String answer = response.getContent();
System.out.println(answer);
```

### Example 2: Multi-turn Conversation

```java
List<Message> conversation = new ArrayList<>();

// Add system message
conversation.add(Message.system("You are a helpful coding assistant"));

// Add user message
conversation.add(Message.user("How do I create a Java class?"));

// Send and get response
LLMRequest request = LLMRequest.builder()
    .model("gpt-4")
    .messages(conversation)
    .build();

LLMResponse response = client.sendMessage(request);

// Add assistant response to conversation
conversation.add(Message.assistant(response.getContent()));

// Continue conversation
conversation.add(Message.user("Can you show an example?"));
// ... send again
```

### Example 3: Streaming Response

```java
// Check if streaming is supported
if (client instanceof StreamingCapable streamingClient) {
    
    LLMRequest request = LLMRequest.builder()
        .model("gpt-4")
        .messages(List.of(Message.user("Tell me a story")))
        .stream(true)
        .build();
    
    // Option 1: Using Stream API
    streamingClient.streamMessage(request)
        .forEach(chunk -> System.out.print(chunk.getContent()));
    
    // Option 2: Using Observer pattern
    streamingClient.streamMessage(request, new StreamingObserver() {
        @Override
        public void onChunk(LLMChunk chunk) {
            System.out.print(chunk.getContent());
        }
        
        @Override
        public void onComplete() {
            System.out.println("\n\nDone!");
        }
        
        @Override
        public void onError(Throwable error) {
            error.printStackTrace();
        }
    });
}
```

### Example 4: Function Calling

```java
// Define function
FunctionDefinition weatherFunc = FunctionDefinition.builder()
    .name("get_weather")
    .description("Get current weather for a location")
    .parameters(Map.of(
        "type", "object",
        "properties", Map.of(
            "location", Map.of(
                "type", "string",
                "description", "City name"
            ),
            "unit", Map.of(
                "type", "string",
                "enum", List.of("celsius", "fahrenheit")
            )
        ),
        "required", List.of("location")
    ))
    .build();

// Check if function calling is supported
if (client instanceof FunctionCallingCapable funcClient) {
    
    LLMRequest request = LLMRequest.builder()
        .model("gpt-4")
        .messages(List.of(
            Message.user("What's the weather in Tokyo?")
        ))
        .build();
    
    // Send with function definitions
    LLMResponse response = funcClient.sendWithFunctions(
        request, 
        List.of(weatherFunc)
    );
    
    // Check if LLM wants to call function
    if (response.hasFunctionCall()) {
        FunctionCall call = response.getFunctionCall();
        
        // Parse arguments
        Map<String, Object> args = call.parseArgumentsAsMap();
        String location = (String) args.get("location");
        
        // Execute function
        String weatherData = getWeather(location);
        
        // Send result back to LLM
        List<Message> messages = new ArrayList<>(request.getMessages());
        messages.add(Message.assistant(response.getContent())
            .toBuilder()
            .functionCall(call)
            .build());
        messages.add(Message.function(call.getName(), weatherData));
        
        // Get final response
        LLMRequest finalRequest = request.toBuilder()
            .messages(messages)
            .build();
        LLMResponse finalResponse = client.sendMessage(finalRequest);
        
        System.out.println(finalResponse.getContent());
    }
}
```

### Example 5: JavaFX UI Integration with Streaming

```java
// In your JavaFX controller
public class ChatController {
    
    @FXML
    private TextArea responseArea;
    
    @FXML
    private Button sendButton;
    
    private LLMClient client;
    
    public void handleSend() {
        String userMessage = inputField.getText();
        
        if (client instanceof StreamingCapable streamingClient) {
            responseArea.clear();
            sendButton.setDisable(true);
            
            LLMRequest request = LLMRequest.builder()
                .model("gpt-4")
                .messages(List.of(Message.user(userMessage)))
                .stream(true)
                .build();
            
            streamingClient.streamMessage(request, new StreamingObserver() {
                @Override
                public void onChunk(LLMChunk chunk) {
                    Platform.runLater(() -> {
                        responseArea.appendText(chunk.getContent());
                    });
                }
                
                @Override
                public void onComplete() {
                    Platform.runLater(() -> {
                        sendButton.setDisable(false);
                    });
                }
                
                @Override
                public void onError(Throwable error) {
                    Platform.runLater(() -> {
                        responseArea.setText("Error: " + error.getMessage());
                        sendButton.setDisable(false);
                    });
                }
            });
        }
    }
}
```

---

## 🏗️ SOLID Principles Applied

### 1. Single Responsibility

```java
// ✅ Each class has ONE job
LLMClient        → Send/receive messages
StreamingCapable → Handle streaming
FunctionExecutor → Execute functions
```

### 2. Open/Closed

```java
// ✅ Add new provider WITHOUT modifying existing code
public class CustomProvider implements LLMClient {
    // New implementation
}
```

### 3. Liskov Substitution

```java
// ✅ Any LLMClient works the same
LLMClient client1 = new OpenAIClient(config);
LLMClient client2 = new ClaudeClient(config);
LLMClient client3 = new CustomClient(config);

// All work identically
LLMResponse response = client1.sendMessage(request);
```

### 4. Interface Segregation

```java
// ✅ Implement only what you need
public class SimpleClient implements LLMClient {
    // Only basic chat
}

public class AdvancedClient implements LLMClient, StreamingCapable, FunctionCallingCapable {
    // Full features
}
```

### 5. Dependency Inversion

```java
// ✅ Depend on abstraction
public class ChatService {
    private final LLMClient client; // Interface, not concrete class
    
    public ChatService(LLMClient client) {
        this.client = client;
    }
}
```

---

## 🎨 Design Patterns

| Pattern      | Usage                 | Benefit                  |
|--------------|-----------------------|--------------------------|
| **Strategy** | Different providers   | Easy to switch providers |
| **Factory**  | Create clients        | Centralized creation     |
| **Builder**  | Build requests        | Flexible construction    |
| **Observer** | Streaming             | Reactive updates         |
| **Adapter**  | API format conversion | Uniform interface        |

---

## ✅ Adding a New Provider

To add a new provider, follow these steps:

### Step 1: Create Provider Class

```java
public class MyCustomClient implements LLMClient, StreamingCapable {
    
    private final LLMProviderConfig config;
    
    public MyCustomClient(LLMProviderConfig config) {
        this.config = config;
        config.validate();
    }
    
    @Override
    public LLMResponse sendMessage(LLMRequest request) {
        // 1. Convert request to provider format
        // 2. Send HTTP request
        // 3. Parse response
        // 4. Convert to LLMResponse
        return LLMResponse.builder()
            .content("Response from custom provider")
            .build();
    }
    
    @Override
    public String getProviderName() {
        return "MyCustomProvider";
    }
    
    @Override
    public boolean isAvailable() {
        // Check if provider is reachable
        return true;
    }
    
    @Override
    public String getModel() {
        return config.getModel();
    }
    
    @Override
    public Stream<LLMChunk> streamMessage(LLMRequest request) {
        // Streaming implementation
        return Stream.empty();
    }
    
    @Override
    public void streamMessage(LLMRequest request, StreamingObserver observer) {
        // Observer-based streaming
    }
}
```

### Step 2: Add to Factory

```java
public class LLMClientFactory {
    public static LLMClient createClient(LLMProviderConfig config) {
        return switch (config.getProvider()) {
            case OPENAI -> new OpenAIClient(config);
            case ANTHROPIC -> new AnthropicClient(config);
            case CUSTOM -> new MyCustomClient(config); // ✅ Add here
            default -> throw new IllegalArgumentException("Unknown provider");
        };
    }
}
```

### Step 3: Configure

```java
LLMProviderConfig config = LLMProviderConfig.builder()
    .provider(Provider.CUSTOM)
    .name("My Custom LLM")
    .url("https://my-api.com/v1/chat")
    .token("my-secret-token")
    .model("custom-model")
    .supportsStreaming(true)
    .build();

LLMClient client = LLMClientFactory.createClient(config);
```

---

## 📊 Configuration Examples

### OpenAI Configuration

```java
LLMProviderConfig.builder()
    .provider(Provider.OPENAI)
    .name("OpenAI GPT-4")
    .url("https://api.openai.com/v1/chat/completions")
    .token(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
    .supportsStreaming(true)
    .supportsFunctionCalling(true)
    .timeout(30)
    .maxRetries(3)
    .build();
```

### Anthropic Configuration

```java
LLMProviderConfig.builder()
    .provider(Provider.ANTHROPIC)
    .name("Claude 3.5 Sonnet")
    .url("https://api.anthropic.com/v1/messages")
    .token(System.getenv("ANTHROPIC_API_KEY"))
    .model("claude-3-5-sonnet-20241022")
    .supportsStreaming(true)
    .supportsFunctionCalling(true)
    .headers(Map.of("anthropic-version", "2023-06-01"))
    .build();
```

### Ollama Configuration (Local)

```java
LLMProviderConfig.builder()
    .provider(Provider.OLLAMA)
    .name("Ollama Llama 3")
    .url("http://localhost:11434/api/chat")
    .token("") // No token needed
    .model("llama3")
    .supportsStreaming(true)
    .supportsFunctionCalling(false)
    .build();
```

---

## 🧪 Testing

### Unit Test Example

```java
@Test
void testBasicChat() {
    LLMProviderConfig config = createTestConfig();
    LLMClient client = new OpenAIClient(config);
    
    LLMRequest request = LLMRequest.builder()
        .model("gpt-4")
        .messages(List.of(Message.user("Hello!")))
        .build();
    
    LLMResponse response = client.sendMessage(request);
    
    assertNotNull(response);
    assertNotNull(response.getContent());
}
```

---

## 📚 Next Steps

1. ✅ **Read** the full [LLM_INTEGRATION_PLAN.md](./LLM_INTEGRATION_PLAN.md)
2. ⏳ **Implement** OpenAIClient (Phase 2)
3. ⏳ **Add** more providers (Phase 3)
4. ⏳ **Create** LLMService layer (Phase 4)
5. ⏳ **Integrate** with UI (Phase 5)

---

## 🆘 Common Issues

### Issue 1: Provider Not Available

```java
if (!client.isAvailable()) {
    throw new RuntimeException("Provider is not available");
}
```

### Issue 2: Streaming Not Supported

```java
if (!(client instanceof StreamingCapable)) {
    // Fall back to non-streaming
    LLMResponse response = client.sendMessage(request);
}
```

### Issue 3: Function Calling Not Supported

```java
if (client instanceof FunctionCallingCapable funcClient) {
    // Use function calling
} else {
    // Use regular chat
}
```

---

**Status**: ✅ Foundation Complete - Ready for Implementation  
**Last Updated**: November 12, 2025  
**Next Step**: Implement OpenAIClient

