# LLM Module Structure

## 📁 Cấu Trúc Thư Mục

```
src/main/java/com/noteflix/pcm/llm/
├── 📦 api/                          # Interfaces & Contracts
│   ├── LLMClient.java              # Base interface cho tất cả LLM clients
│   ├── StreamingCapable.java      # Interface cho streaming responses
│   ├── FunctionCallingCapable.java # Interface cho function calling
│   └── EmbeddingsCapable.java     # Interface cho embeddings API
│
├── 🔌 client/                       # Provider Implementations
│   ├── openai/
│   │   ├── OpenAIClient.java      # OpenAI GPT implementation
│   │   └── SSEParser.java         # Server-Sent Events parser
│   ├── anthropic/
│   │   └── AnthropicClient.java   # Anthropic Claude implementation
│   └── ollama/
│       └── OllamaClient.java      # Ollama local models implementation
│
├── 🏭 factory/                      # Factory Pattern
│   └── LLMClientFactory.java      # Factory để tạo LLM clients
│
├── 📊 model/                        # Data Models
│   ├── LLMRequest.java            # Request model
│   ├── LLMResponse.java           # Response model
│   ├── LLMChunk.java              # Streaming chunk model
│   ├── Message.java               # Chat message model
│   ├── StreamingObserver.java     # Observer pattern cho streaming
│   ├── LLMProviderConfig.java     # Provider configuration
│   ├── FunctionDefinition.java    # Function definition cho function calling
│   ├── FunctionCall.java          # Function call result
│   ├── EmbeddingsRequest.java     # Embeddings request
│   └── EmbeddingsResponse.java    # Embeddings response
│
├── 🛡️ exception/                    # Custom Exceptions
│   ├── LLMException.java          # Base exception
│   ├── LLMProviderException.java  # Provider-specific exception
│   └── StreamingException.java    # Streaming-specific exception
│
├── ⚙️ middleware/                   # Middleware Components
│   ├── RateLimiter.java           # Rate limiting
│   ├── RetryPolicy.java           # Retry logic
│   └── RequestLogger.java         # Request/response logging
│
├── 🎯 service/                      # High-level Services
│   └── LLMService.java            # Main service với business logic
│
└── 📚 examples/                     # Code Examples
    ├── LLMUsageExample.java       # Basic usage examples
    ├── APIDemo.java               # API demo
    └── MiddlewareExample.java     # Middleware usage examples
```

---

## 🏗️ Architecture Pattern

### **Strategy Pattern** (Client Implementations)

```
LLMClient (Interface)
    ↑
    ├── OpenAIClient
    ├── AnthropicClient
    └── OllamaClient
```

### **Factory Pattern** (Client Creation)

```
LLMClientFactory
    │
    ├── getClient(config) → LLMClient
    └── Cache clients by provider
```

### **Observer Pattern** (Streaming)

```
StreamingObserver (Interface)
    │
    ├── onChunk(chunk)
    ├── onComplete()
    └── onError(error)
```

---

## 📦 Core Components

### 1️⃣ **API Layer** (`api/`)

#### `LLMClient.java` - Base Interface

```java
public interface LLMClient {
    LLMResponse sendMessage(LLMRequest request);
    String getProviderName();
    boolean isAvailable();
    String getModelName();
}
```

**Mục đích:**

- ✅ Định nghĩa contract cho tất cả LLM providers
- ✅ Dependency Inversion Principle
- ✅ Dễ dàng swap providers

#### `StreamingCapable.java`

```java
public interface StreamingCapable {
    void streamMessage(LLMRequest request, StreamingObserver observer);
}
```

**Mục đích:**

- ✅ Support streaming responses (như ChatGPT)
- ✅ Real-time token generation
- ✅ Better UX

#### `FunctionCallingCapable.java`

```java
public interface FunctionCallingCapable {
    LLMResponse sendMessageWithFunctions(
        LLMRequest request, 
        List<FunctionDefinition> functions
    );
}
```

**Mục đích:**

- ✅ Support OpenAI function calling
- ✅ Tool use (như Anthropic)
- ✅ Agent capabilities

#### `EmbeddingsCapable.java`

```java
public interface EmbeddingsCapable {
    EmbeddingsResponse getEmbeddings(EmbeddingsRequest request);
}
```

**Mục đích:**

- ✅ Vector embeddings cho RAG
- ✅ Semantic search
- ✅ Knowledge base integration

---

### 2️⃣ **Client Layer** (`client/`)

#### **OpenAI Client**

- File: `openai/OpenAIClient.java`
- Supports: GPT-4, GPT-3.5-turbo, GPT-4-turbo
- Features:
    - ✅ Chat completions
    - ✅ Streaming với SSE
    - ✅ Function calling
    - ✅ Embeddings (text-embedding-ada-002)

#### **Anthropic Client**

- File: `anthropic/AnthropicClient.java`
- Supports: Claude 3.5 Sonnet, Claude 3 Opus, Claude 3 Haiku
- Features:
    - ✅ Chat completions
    - ✅ Tool use
    - ✅ Long context (200k tokens)

#### **Ollama Client**

- File: `ollama/OllamaClient.java`
- Supports: Local models (Llama, Mistral, etc.)
- Features:
    - ✅ Local inference
    - ✅ No API keys needed
    - ✅ Privacy-first

---

### 3️⃣ **Factory Layer** (`factory/`)

#### `LLMClientFactory.java`

```java
public class LLMClientFactory {
    public LLMClient getClient(LLMProviderConfig config) {
        return switch (config.getProvider()) {
            case OPENAI -> new OpenAIClient(config);
            case ANTHROPIC -> new AnthropicClient(config);
            case OLLAMA -> new OllamaClient(config);
        };
    }
}
```

**Features:**

- ✅ Singleton pattern
- ✅ Client caching
- ✅ Lazy initialization
- ✅ Easy to add new providers

---

### 4️⃣ **Model Layer** (`model/`)

#### Key Models:

**`LLMRequest.java`**

```java
public class LLMRequest {
    private List<Message> messages;
    private String model;
    private double temperature;
    private int maxTokens;
    private Map<String, Object> additionalParams;
}
```

**`LLMResponse.java`**

```java
public class LLMResponse {
    private String content;
    private String model;
    private int tokensUsed;
    private long latencyMs;
    private String finishReason;
}
```

**`LLMChunk.java`** (for streaming)

```java
public class LLMChunk {
    private String content;
    private boolean isComplete;
    private String finishReason;
}
```

**`StreamingObserver.java`**

```java
public interface StreamingObserver {
    void onChunk(LLMChunk chunk);
    void onComplete();
    void onError(Throwable error);
}
```

**`LLMProviderConfig.java`**

```java
public class LLMProviderConfig {
    private LLMProvider provider;  // OPENAI, ANTHROPIC, OLLAMA
    private String apiKey;
    private String model;
    private String baseUrl;
    private int timeout;
}
```

---

### 5️⃣ **Exception Layer** (`exception/`)

```
LLMException (Base)
    ↑
    ├── LLMProviderException (Provider errors)
    └── StreamingException (Streaming errors)
```

**Hierarchy:**

- ✅ Clear error types
- ✅ Easy to catch and handle
- ✅ Good error messages

---

### 6️⃣ **Middleware Layer** (`middleware/`)

#### `RateLimiter.java`

```java
public class RateLimiter {
    // Token bucket algorithm
    public boolean tryAcquire();
    public void acquire() throws InterruptedException;
}
```

**Mục đích:**

- ✅ Prevent API rate limit errors
- ✅ Configurable rates
- ✅ Per-provider limits

#### `RetryPolicy.java`

```java
public class RetryPolicy {
    public <T> T execute(Callable<T> task);
    // Exponential backoff
    // Max retries
}
```

**Mục đích:**

- ✅ Handle transient errors
- ✅ Automatic retry
- ✅ Exponential backoff

#### `RequestLogger.java`

```java
public class RequestLogger {
    public void logRequest(LLMRequest request);
    public void logResponse(LLMResponse response);
}
```

**Mục đích:**

- ✅ Debug requests/responses
- ✅ Audit trail
- ✅ Performance monitoring

---

### 7️⃣ **Service Layer** (`service/`)

#### `LLMService.java` - Main Service

```java
public class LLMService {
    // High-level methods
    public String chat(String message);
    public void chatStreaming(String message, StreamingObserver observer);
    public void switchProvider(LLMProviderConfig config);
    
    // Advanced
    public LLMResponse chatWithFunctions(String message, List<FunctionDefinition> functions);
    public double[] getEmbeddings(String text);
}
```

**Mục đích:**

- ✅ Simplified API
- ✅ Business logic
- ✅ Error handling
- ✅ Logging & metrics

---

## 🎯 Usage Examples

### **Basic Chat**

```java
LLMService service = new LLMService();
service.initialize(LLMProviderConfig.builder()
    .provider(LLMProvider.OPENAI)
    .apiKey("sk-...")
    .model("gpt-4")
    .build());

String response = service.chat("Hello, how are you?");
System.out.println(response);
```

### **Streaming Chat**

```java
service.chatStreaming("Tell me a story", new StreamingObserver() {
    @Override
    public void onChunk(LLMChunk chunk) {
        System.out.print(chunk.getContent());
    }
    
    @Override
    public void onComplete() {
        System.out.println("\n[Done]");
    }
    
    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }
});
```

### **Function Calling**

```java
List<FunctionDefinition> functions = List.of(
    FunctionDefinition.builder()
        .name("get_weather")
        .description("Get current weather")
        .parameters(...)
        .build()
);

LLMResponse response = service.chatWithFunctions(
    "What's the weather in Tokyo?", 
    functions
);

if (response.hasFunctionCall()) {
    FunctionCall call = response.getFunctionCall();
    // Execute function...
}
```

### **Switch Providers**

```java
// Start with OpenAI
service.initialize(openAIConfig);

// Switch to Anthropic
service.switchProvider(anthropicConfig);

// Switch to local Ollama
service.switchProvider(ollamaConfig);
```

---

## ✅ Design Principles Applied

### **SOLID Principles**

1. ✅ **Single Responsibility**
    - Each client handles one provider
    - Each middleware handles one concern
    - Clear separation

2. ✅ **Open/Closed**
    - Easy to add new providers (extend)
    - Don't need to modify existing code

3. ✅ **Liskov Substitution**
    - All clients can replace `LLMClient`
    - Polymorphism works correctly

4. ✅ **Interface Segregation**
    - Small, focused interfaces
    - Optional capabilities (Streaming, Functions, Embeddings)

5. ✅ **Dependency Inversion**
    - Depend on abstractions (`LLMClient`)
    - Not on concrete implementations

### **Design Patterns**

- ✅ **Strategy Pattern** - Multiple LLM providers
- ✅ **Factory Pattern** - Client creation
- ✅ **Observer Pattern** - Streaming
- ✅ **Singleton Pattern** - Factory instance
- ✅ **Builder Pattern** - Config objects
- ✅ **Middleware Pattern** - Request/response processing

---

## 🚀 Benefits

### **Flexibility**

- ✅ Easy to switch providers
- ✅ Support multiple providers simultaneously
- ✅ Provider-specific features available

### **Maintainability**

- ✅ Clear structure
- ✅ Easy to find code
- ✅ Well-documented

### **Testability**

- ✅ Mock `LLMClient` interface
- ✅ Test each provider independently
- ✅ Integration tests with real APIs

### **Extensibility**

- ✅ Add new providers easily
- ✅ Add new capabilities
- ✅ Plugin architecture ready

### **Performance**

- ✅ Client caching
- ✅ Connection pooling
- ✅ Rate limiting
- ✅ Retry logic

---

## 📈 Future Enhancements

### Planned Features:

- [ ] **Embeddings Support** - For RAG systems
- [ ] **Vision APIs** - GPT-4 Vision, Claude with images
- [ ] **Audio APIs** - Whisper, TTS
- [ ] **Custom Providers** - Easy plugin system
- [ ] **Prompt Templates** - Reusable prompt management
- [ ] **Token Counting** - Pre-request token estimation
- [ ] **Cost Tracking** - Monitor API costs
- [ ] **Caching Layer** - Cache similar requests
- [ ] **A/B Testing** - Compare providers/models
- [ ] **Load Balancing** - Distribute across providers

---

## 📚 Related Documentation

- [AI Assistant Architecture](./development/ai-assistant/AI_ASSISTANT_REFACTOR_PLAN.md)
- [MVVM Refactoring](../MVVM_REFACTORING_COMPLETE.md)
- [Best Practices](../BESTPRACTICES.md)

---

**Created:** November 12, 2025  
**Version:** 1.0.0  
**Status:** ✅ Production Ready

