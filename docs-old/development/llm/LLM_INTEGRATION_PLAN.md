# LLM Integration Plan - PCM Desktop

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture Design](#architecture-design)
3. [SOLID Principles Applied](#solid-principles-applied)
4. [Design Patterns](#design-patterns)
5. [Core Components](#core-components)
6. [Implementation Phases](#implementation-phases)
7. [Provider Examples](#provider-examples)
8. [Configuration](#configuration)
9. [Testing Strategy](#testing-strategy)

---

## 🎯 Overview

### Goals

- ✅ Support multiple LLM providers (OpenAI, custom APIs)
- ✅ Easy to add new providers
- ✅ Support streaming and non-streaming responses
- ✅ Support function calling
- ✅ Flexible configuration (URL + token)
- ✅ Type-safe and extensible
- ✅ Follow SOLID principles and clean code

### Key Requirements

1. **Multiple Providers**: OpenAI, Anthropic, Custom APIs
2. **Streaming Support**: Real-time response streaming
3. **Function Calling**: Tool/function execution
4. **Chat History**: Multi-turn conversations
5. **Configuration**: URL + Token per provider
6. **Error Handling**: Robust error management
7. **Retry Logic**: Automatic retry with backoff

---

## 🏗️ Architecture Design

### High-Level Architecture

```
┌─────────────────────────────────────────────────┐
│              UI Layer (JavaFX)                  │
│  AIAssistantPage, ChatWidget, StreamingUI      │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│           Service Layer (LLMService)            │
│  LLMService, ConversationService                │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│         LLM Client Layer (Abstraction)          │
│  LLMClient (interface)                          │
│  - sendMessage()                                │
│  - streamMessage()                              │
│  - sendWithFunctions()                          │
└────────────────────┬────────────────────────────┘
                     │
         ┌───────────┴───────────┬────────────────┬──────────────┐
         │                       │                │              │
┌────────▼────────┐  ┌──────────▼──────┐  ┌─────▼─────┐  ┌────▼─────┐
│  OpenAIClient   │  │ AnthropicClient │  │ OllamaClient│  │CustomClient│
│  (GPT-4, etc)   │  │ (Claude, etc)   │  │ (Local)   │  │ (Your API)│
└─────────────────┘  └─────────────────┘  └───────────┘  └──────────┘
         │                       │                │              │
         └───────────┬───────────┴────────────────┴──────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│         HTTP Client & JSON Processing           │
│  HttpClient, Jackson ObjectMapper               │
└─────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### 1. **UI Layer**

- Display chat interface
- Handle streaming UI updates
- Show function calling results
- User interaction

#### 2. **Service Layer**

- Business logic
- Conversation management
- Function execution orchestration
- Error handling

#### 3. **Client Layer**

- Provider-specific implementations
- API communication
- Request/response mapping
- Streaming handling

#### 4. **HTTP Layer**

- HTTP communication
- JSON serialization/deserialization
- Connection management

---

## ✅ SOLID Principles Applied

### **S - Single Responsibility Principle**

```java
// ✅ Each class has ONE responsibility

// Handles ONLY LLM client creation
public class LLMClientFactory {
    public LLMClient createClient(LLMProviderConfig config) { }
}

// Handles ONLY OpenAI API communication
public class OpenAIClient implements LLMClient {
    // OpenAI-specific implementation
}

// Handles ONLY conversation state management
public class ConversationManager {
    public void addMessage(Message message) { }
}

// Handles ONLY function execution
public class FunctionExecutor {
    public Object executeFunction(FunctionCall call) { }
}
```

### **O - Open/Closed Principle**

```java
// ✅ Open for extension (new providers), closed for modification

// Base interface - never needs to change
public interface LLMClient {
    LLMResponse sendMessage(LLMRequest request);
}

// Add new provider without modifying existing code
public class CustomProvider implements LLMClient {
    @Override
    public LLMResponse sendMessage(LLMRequest request) {
        // Custom implementation
    }
}
```

### **L - Liskov Substitution Principle**

```java
// ✅ Any LLMClient implementation can replace another

public class LLMService {
    private final LLMClient client; // Can be ANY implementation
    
    public LLMService(LLMClient client) {
        this.client = client; // Works with OpenAI, Anthropic, etc.
    }
    
    public String chat(String message) {
        return client.sendMessage(new LLMRequest(message)).getContent();
    }
}
```

### **I - Interface Segregation Principle**

```java
// ✅ Specific interfaces for specific capabilities

// Base capability - all providers must support
public interface LLMClient {
    LLMResponse sendMessage(LLMRequest request);
}

// Optional capability - only providers that support streaming
public interface StreamingCapable {
    Stream<LLMChunk> streamMessage(LLMRequest request);
}

// Optional capability - only providers that support function calling
public interface FunctionCallingCapable {
    LLMResponse sendWithFunctions(LLMRequest request, List<FunctionDefinition> functions);
}

// Provider implements only what it supports
public class OpenAIClient implements LLMClient, StreamingCapable, FunctionCallingCapable {
    // Full implementation
}

public class SimpleClient implements LLMClient {
    // Only basic chat - no streaming, no functions
}
```

### **D - Dependency Inversion Principle**

```java
// ✅ Depend on abstractions, not concrete implementations

// High-level module depends on abstraction
public class AIAssistantService {
    private final LLMClient client; // Interface, not concrete class
    
    public AIAssistantService(LLMClient client) {
        this.client = client; // Injected dependency
    }
}

// Low-level modules implement abstraction
public class OpenAIClient implements LLMClient { }
public class ClaudeClient implements LLMClient { }
```

---

## 🎨 Design Patterns

### 1. **Strategy Pattern** - Provider Selection

```java
// Different strategies for different providers
public interface LLMClient {
    LLMResponse sendMessage(LLMRequest request);
}

public class OpenAIClient implements LLMClient { }
public class AnthropicClient implements LLMClient { }

// Context uses strategy
public class LLMService {
    private LLMClient strategy;
    
    public void setProvider(LLMClient client) {
        this.strategy = client;
    }
}
```

### 2. **Factory Pattern** - Client Creation

```java
public class LLMClientFactory {
    public static LLMClient createClient(LLMProviderConfig config) {
        return switch (config.getProvider()) {
            case OPENAI -> new OpenAIClient(config);
            case ANTHROPIC -> new AnthropicClient(config);
            case OLLAMA -> new OllamaClient(config);
            case CUSTOM -> new CustomClient(config);
        };
    }
}
```

### 3. **Builder Pattern** - Request Construction

```java
LLMRequest request = LLMRequest.builder()
    .model("gpt-4")
    .messages(messages)
    .temperature(0.7)
    .maxTokens(2000)
    .functions(functions)
    .stream(true)
    .build();
```

### 4. **Observer Pattern** - Streaming

```java
public interface StreamingObserver {
    void onChunk(LLMChunk chunk);
    void onComplete();
    void onError(Throwable error);
}

public class StreamingLLMClient {
    private List<StreamingObserver> observers = new ArrayList<>();
    
    public void addObserver(StreamingObserver observer) {
        observers.add(observer);
    }
    
    public void streamMessage(LLMRequest request) {
        // Stream chunks and notify observers
        observers.forEach(obs -> obs.onChunk(chunk));
    }
}
```

### 5. **Chain of Responsibility** - Middleware

```java
public interface LLMMiddleware {
    LLMResponse handle(LLMRequest request, LLMMiddleware next);
}

public class LoggingMiddleware implements LLMMiddleware {
    public LLMResponse handle(LLMRequest request, LLMMiddleware next) {
        log.info("Request: {}", request);
        LLMResponse response = next.handle(request, next);
        log.info("Response: {}", response);
        return response;
    }
}

public class RetryMiddleware implements LLMMiddleware {
    public LLMResponse handle(LLMRequest request, LLMMiddleware next) {
        try {
            return next.handle(request, next);
        } catch (Exception e) {
            // Retry logic
            return next.handle(request, next);
        }
    }
}
```

### 6. **Adapter Pattern** - API Format Conversion

```java
// Adapter for different API formats
public class OpenAIAdapter implements LLMClient {
    public LLMResponse sendMessage(LLMRequest request) {
        // Convert internal format to OpenAI format
        OpenAIRequest openAIRequest = convertToOpenAI(request);
        OpenAIResponse openAIResponse = callOpenAI(openAIRequest);
        // Convert back to internal format
        return convertFromOpenAI(openAIResponse);
    }
}
```

---

## 📦 Core Components

### Package Structure

```
com.noteflix.pcm.llm/
├── api/
│   ├── LLMClient.java                    # Base client interface
│   ├── StreamingCapable.java            # Streaming interface
│   ├── FunctionCallingCapable.java      # Function calling interface
│   └── EmbeddingCapable.java            # Embedding interface (future)
│
├── model/
│   ├── LLMRequest.java                  # Request model
│   ├── LLMResponse.java                 # Response model
│   ├── LLMChunk.java                    # Streaming chunk
│   ├── Message.java                     # Chat message
│   ├── FunctionDefinition.java         # Function definition
│   ├── FunctionCall.java               # Function call request
│   ├── LLMProviderConfig.java          # Provider configuration
│   └── StreamingObserver.java          # Streaming observer
│
├── client/
│   ├── openai/
│   │   ├── OpenAIClient.java           # OpenAI implementation
│   │   ├── OpenAIRequest.java          # OpenAI-specific request
│   │   └── OpenAIResponse.java         # OpenAI-specific response
│   │
│   ├── anthropic/
│   │   ├── AnthropicClient.java        # Claude implementation
│   │   ├── AnthropicRequest.java       
│   │   └── AnthropicResponse.java      
│   │
│   ├── ollama/
│   │   └── OllamaClient.java           # Local Ollama
│   │
│   └── custom/
│       └── CustomClient.java            # Custom API
│
├── factory/
│   └── LLMClientFactory.java           # Client factory
│
├── service/
│   ├── LLMService.java                 # Main LLM service
│   ├── ConversationService.java        # Conversation management
│   └── FunctionExecutorService.java    # Function execution
│
├── middleware/
│   ├── LLMMiddleware.java              # Middleware interface
│   ├── LoggingMiddleware.java          # Logging
│   ├── RetryMiddleware.java            # Retry logic
│   ├── RateLimitMiddleware.java        # Rate limiting
│   └── CachingMiddleware.java          # Response caching
│
├── util/
│   ├── LLMHttpClient.java              # HTTP client wrapper
│   ├── StreamingParser.java            # SSE/streaming parser
│   └── TokenCounter.java               # Token counting
│
└── exception/
    ├── LLMException.java               # Base exception
    ├── LLMProviderException.java       # Provider error
    ├── StreamingException.java         # Streaming error
    └── FunctionExecutionException.java # Function error
```

---

## 💻 Core Interfaces & Classes

### 1. **LLMClient Interface**

```java
package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.*;

/**
 * Base interface for all LLM clients
 * Following Interface Segregation Principle
 */
public interface LLMClient {
    
    /**
     * Send a message and get response
     * All providers must implement this
     */
    LLMResponse sendMessage(LLMRequest request);
    
    /**
     * Get provider name
     */
    String getProviderName();
    
    /**
     * Check if provider is available
     */
    boolean isAvailable();
}
```

### 2. **StreamingCapable Interface**

```java
package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.*;
import java.util.stream.Stream;

/**
 * Optional interface for streaming support
 */
public interface StreamingCapable {
    
    /**
     * Stream message chunks in real-time
     * Returns Java Stream for reactive processing
     */
    Stream<LLMChunk> streamMessage(LLMRequest request);
    
    /**
     * Stream with observer pattern
     */
    void streamMessage(LLMRequest request, StreamingObserver observer);
}
```

### 3. **FunctionCallingCapable Interface**

```java
package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.*;
import java.util.List;

/**
 * Optional interface for function calling support
 */
public interface FunctionCallingCapable {
    
    /**
     * Send message with function definitions
     * Model can decide to call functions
     */
    LLMResponse sendWithFunctions(
        LLMRequest request, 
        List<FunctionDefinition> functions
    );
    
    /**
     * Check if function calling is supported
     */
    boolean supportsFunctionCalling();
}
```

### 4. **LLMRequest Model**

```java
package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Universal LLM request model
 * Compatible with OpenAI format
 */
@Data
@Builder
public class LLMRequest {
    
    private String model;                    // Model name (gpt-4, claude-3, etc)
    private List<Message> messages;          // Conversation messages
    
    // Generation parameters
    @Builder.Default
    private Double temperature = 0.7;        // Randomness (0-2)
    
    @Builder.Default
    private Integer maxTokens = 2000;        // Max response length
    
    private Double topP;                     // Nucleus sampling
    private Integer n;                       // Number of completions
    private List<String> stop;               // Stop sequences
    private Double presencePenalty;          // Presence penalty
    private Double frequencyPenalty;         // Frequency penalty
    
    // Function calling
    private List<FunctionDefinition> functions;
    private Object functionCall;             // "auto", "none", or {"name": "function_name"}
    
    // Streaming
    @Builder.Default
    private Boolean stream = false;          // Enable streaming
    
    // Custom parameters for specific providers
    private java.util.Map<String, Object> customParams;
}
```

### 5. **LLMResponse Model**

```java
package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

/**
 * Universal LLM response model
 */
@Data
@Builder
public class LLMResponse {
    
    private String id;                       // Response ID
    private String model;                    // Model used
    private String content;                  // Main response content
    private String finishReason;             // stop, length, function_call, etc
    
    // Function calling
    private FunctionCall functionCall;       // Function to call (if any)
    
    // Usage statistics
    private Usage usage;                     // Token usage
    
    // Metadata
    private Instant createdAt;
    private java.util.Map<String, Object> metadata;
    
    @Data
    @Builder
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
```

### 6. **Message Model**

```java
package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;

/**
 * Chat message
 */
@Data
@Builder
public class Message {
    
    public enum Role {
        SYSTEM,
        USER,
        ASSISTANT,
        FUNCTION
    }
    
    private Role role;
    private String content;
    
    // For function results
    private String name;                     // Function name
    private FunctionCall functionCall;       // Function call details
}
```

### 7. **LLMProviderConfig Model**

```java
package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for LLM provider
 */
@Data
@Builder
public class LLMProviderConfig {
    
    public enum Provider {
        OPENAI,
        ANTHROPIC,
        OLLAMA,
        CUSTOM
    }
    
    private Provider provider;
    private String name;                     // Display name
    
    // Required fields
    private String url;                      // API endpoint URL
    private String token;                    // API token/key
    
    // Optional fields
    private String model;                    // Default model
    private Integer timeout;                 // Request timeout (seconds)
    private Integer maxRetries;              // Max retry attempts
    
    // Feature flags
    @Builder.Default
    private Boolean supportsStreaming = false;
    
    @Builder.Default
    private Boolean supportsFunctionCalling = false;
    
    // Custom headers
    private java.util.Map<String, String> headers;
}
```

### 8. **FunctionDefinition Model**

```java
package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

/**
 * Function definition for function calling
 * Following OpenAI function calling spec
 */
@Data
@Builder
public class FunctionDefinition {
    
    private String name;                     // Function name
    private String description;              // Function description
    
    // JSON Schema for parameters
    private Map<String, Object> parameters;
    
    /**
     * Example:
     * {
     *   "type": "object",
     *   "properties": {
     *     "location": {"type": "string"},
     *     "unit": {"type": "string", "enum": ["celsius", "fahrenheit"]}
     *   },
     *   "required": ["location"]
     * }
     */
}
```

### 9. **StreamingObserver Interface**

```java
package com.noteflix.pcm.llm.model;

/**
 * Observer for streaming responses
 */
public interface StreamingObserver {
    
    /**
     * Called when a new chunk arrives
     */
    void onChunk(LLMChunk chunk);
    
    /**
     * Called when streaming completes
     */
    void onComplete();
    
    /**
     * Called when an error occurs
     */
    void onError(Throwable error);
}
```

---

## 🚀 Implementation Phases

### **Phase 1: Foundation (Week 1)**

#### Deliverables:

- [x] Create package structure
- [ ] Define core interfaces (LLMClient, StreamingCapable, FunctionCallingCapable)
- [ ] Define model classes (LLMRequest, LLMResponse, Message, etc.)
- [ ] Create LLMProviderConfig
- [ ] Create base exceptions
- [ ] Write unit tests for models

**Files to Create:**

```
src/main/java/com/noteflix/pcm/llm/
├── api/LLMClient.java
├── api/StreamingCapable.java
├── api/FunctionCallingCapable.java
├── model/LLMRequest.java
├── model/LLMResponse.java
├── model/Message.java
├── model/LLMProviderConfig.java
├── model/FunctionDefinition.java
└── exception/LLMException.java
```

---

### **Phase 2: OpenAI Client (Week 2)**

#### Deliverables:

- [ ] Implement OpenAIClient
- [ ] Support basic chat completion
- [ ] Support streaming
- [ ] Support function calling
- [ ] HTTP client wrapper
- [ ] Error handling
- [ ] Unit tests
- [ ] Integration tests

**Implementation:**

```java
public class OpenAIClient implements LLMClient, StreamingCapable, FunctionCallingCapable {
    
    private final LLMProviderConfig config;
    private final LLMHttpClient httpClient;
    
    @Override
    public LLMResponse sendMessage(LLMRequest request) {
        // Convert to OpenAI format
        // Send HTTP request
        // Parse response
        // Return LLMResponse
    }
    
    @Override
    public Stream<LLMChunk> streamMessage(LLMRequest request) {
        // SSE streaming implementation
    }
    
    @Override
    public LLMResponse sendWithFunctions(LLMRequest request, List<FunctionDefinition> functions) {
        // Function calling implementation
    }
}
```

---

### **Phase 3: Additional Providers (Week 3)**

#### Deliverables:

- [ ] Implement AnthropicClient (Claude)
- [ ] Implement OllamaClient (local models)
- [ ] Implement CustomClient (generic OpenAI-compatible)
- [ ] Provider adapters
- [ ] Tests for all providers

---

### **Phase 4: Factory & Service Layer (Week 4)**

#### Deliverables:

- [ ] LLMClientFactory
- [ ] LLMService
- [ ] ConversationService
- [ ] FunctionExecutorService
- [ ] Middleware chain
- [ ] Configuration management

**LLMService:**

```java
public class LLMService {
    
    private final LLMClient client;
    private final ConversationService conversationService;
    private final FunctionExecutorService functionExecutor;
    
    public String chat(String userMessage) {
        // Add message to conversation
        // Send to LLM
        // Handle function calls if any
        // Return response
    }
    
    public void streamChat(String userMessage, StreamingObserver observer) {
        // Streaming chat
    }
}
```

---

### **Phase 5: UI Integration (Week 5)**

#### Deliverables:

- [ ] Update AIAssistantPage with LLM integration
- [ ] Streaming UI updates
- [ ] Function calling UI
- [ ] Provider selection UI
- [ ] Configuration UI
- [ ] Error display

---

### **Phase 6: Advanced Features (Week 6)**

#### Deliverables:

- [ ] Response caching
- [ ] Rate limiting
- [ ] Retry logic with exponential backoff
- [ ] Token counting
- [ ] Conversation persistence
- [ ] Export conversations

---

## 📝 Provider Examples

### 1. **OpenAI Provider**

```java
LLMProviderConfig openAIConfig = LLMProviderConfig.builder()
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

LLMClient client = LLMClientFactory.createClient(openAIConfig);
```

### 2. **Anthropic Provider (Claude)**

```java
LLMProviderConfig claudeConfig = LLMProviderConfig.builder()
    .provider(Provider.ANTHROPIC)
    .name("Claude 3.5 Sonnet")
    .url("https://api.anthropic.com/v1/messages")
    .token(System.getenv("ANTHROPIC_API_KEY"))
    .model("claude-3-5-sonnet-20241022")
    .supportsStreaming(true)
    .supportsFunctionCalling(true)
    .build();
```

### 3. **Ollama (Local)**

```java
LLMProviderConfig ollamaConfig = LLMProviderConfig.builder()
    .provider(Provider.OLLAMA)
    .name("Ollama Llama 3")
    .url("http://localhost:11434/api/chat")
    .token("") // No token needed
    .model("llama3")
    .supportsStreaming(true)
    .supportsFunctionCalling(false)
    .build();
```

### 4. **Custom Provider**

```java
LLMProviderConfig customConfig = LLMProviderConfig.builder()
    .provider(Provider.CUSTOM)
    .name("My Custom LLM")
    .url("https://my-api.com/v1/chat")
    .token("my-secret-token")
    .model("custom-model-v1")
    .supportsStreaming(false)
    .supportsFunctionCalling(false)
    .build();
```

---

## ⚙️ Configuration

### Configuration File (YAML)

```yaml
# config/llm-providers.yaml

providers:
  - name: "OpenAI GPT-4"
    provider: "OPENAI"
    url: "https://api.openai.com/v1/chat/completions"
    token: "${OPENAI_API_KEY}"
    model: "gpt-4"
    timeout: 30
    maxRetries: 3
    supportsStreaming: true
    supportsFunctionCalling: true
    
  - name: "Claude 3.5 Sonnet"
    provider: "ANTHROPIC"
    url: "https://api.anthropic.com/v1/messages"
    token: "${ANTHROPIC_API_KEY}"
    model: "claude-3-5-sonnet-20241022"
    timeout: 30
    supportsStreaming: true
    supportsFunctionCalling: true
    
  - name: "Ollama Llama 3"
    provider: "OLLAMA"
    url: "http://localhost:11434/api/chat"
    token: ""
    model: "llama3"
    supportsStreaming: true
    supportsFunctionCalling: false
    
  - name: "Custom API"
    provider: "CUSTOM"
    url: "https://my-api.com/v1/chat"
    token: "${CUSTOM_API_TOKEN}"
    model: "custom-model"
    supportsStreaming: false
    supportsFunctionCalling: false
```

### Configuration Manager

```java
public class LLMConfigManager {
    
    private static final String CONFIG_FILE = "config/llm-providers.yaml";
    private final List<LLMProviderConfig> providers;
    
    public LLMConfigManager() {
        this.providers = loadFromFile(CONFIG_FILE);
    }
    
    public List<LLMProviderConfig> getAllProviders() {
        return providers;
    }
    
    public LLMProviderConfig getProvider(String name) {
        return providers.stream()
            .filter(p -> p.getName().equals(name))
            .findFirst()
            .orElseThrow();
    }
}
```

---

## 🧪 Testing Strategy

### Unit Tests

```java
@Test
void testOpenAIClientBasicChat() {
    LLMProviderConfig config = createTestConfig();
    OpenAIClient client = new OpenAIClient(config);
    
    LLMRequest request = LLMRequest.builder()
        .model("gpt-4")
        .messages(List.of(
            Message.builder()
                .role(Message.Role.USER)
                .content("Hello!")
                .build()
        ))
        .build();
    
    LLMResponse response = client.sendMessage(request);
    
    assertNotNull(response);
    assertNotNull(response.getContent());
}
```

### Integration Tests

```java
@Test
void testStreamingChat() {
    // Test real streaming with provider
}

@Test
void testFunctionCalling() {
    // Test function calling flow
}
```

### Mock Tests

```java
@Test
void testWithMockProvider() {
    LLMClient mockClient = Mockito.mock(LLMClient.class);
    // Test service layer with mock
}
```

---

## 📚 Usage Examples

### Example 1: Basic Chat

```java
// Setup
LLMProviderConfig config = LLMProviderConfig.builder()
    .provider(Provider.OPENAI)
    .url("https://api.openai.com/v1/chat/completions")
    .token("sk-...")
    .build();

LLMClient client = LLMClientFactory.createClient(config);
LLMService service = new LLMService(client);

// Chat
String response = service.chat("What is Java?");
System.out.println(response);
```

### Example 2: Streaming Chat

```java
service.streamChat("Tell me a story", new StreamingObserver() {
    @Override
    public void onChunk(LLMChunk chunk) {
        System.out.print(chunk.getContent());
    }
    
    @Override
    public void onComplete() {
        System.out.println("\nDone!");
    }
    
    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }
});
```

### Example 3: Function Calling

```java
// Define functions
FunctionDefinition weatherFunction = FunctionDefinition.builder()
    .name("get_weather")
    .description("Get current weather for a location")
    .parameters(Map.of(
        "type", "object",
        "properties", Map.of(
            "location", Map.of("type", "string"),
            "unit", Map.of("type", "string", "enum", List.of("celsius", "fahrenheit"))
        ),
        "required", List.of("location")
    ))
    .build();

// Send request with functions
LLMRequest request = LLMRequest.builder()
    .model("gpt-4")
    .messages(List.of(
        Message.builder()
            .role(Message.Role.USER)
            .content("What's the weather in Tokyo?")
            .build()
    ))
    .functions(List.of(weatherFunction))
    .build();

LLMResponse response = ((FunctionCallingCapable) client)
    .sendWithFunctions(request, List.of(weatherFunction));

if (response.getFunctionCall() != null) {
    // Execute function
    Object result = functionExecutor.execute(response.getFunctionCall());
    // Send result back to LLM
}
```

---

## 🎯 Success Metrics

1. **Extensibility**: Add new provider in < 2 hours
2. **Performance**: Response time < 3 seconds for basic chat
3. **Reliability**: 99% success rate with retry logic
4. **Code Quality**: > 80% test coverage
5. **Maintainability**: Low cyclomatic complexity

---

## 📖 References

- [OpenAI API Documentation](https://platform.openai.com/docs/api-reference)
- [Anthropic API Documentation](https://docs.anthropic.com/claude/reference/getting-started-with-the-api)
- [Ollama API](https://github.com/ollama/ollama/blob/main/docs/api.md)
- [Function Calling Guide](https://platform.openai.com/docs/guides/function-calling)

---

**Status**: 📋 Planning Complete  
**Last Updated**: November 12, 2025  
**Next Step**: Phase 1 Implementation

