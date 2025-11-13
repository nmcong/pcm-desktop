# LLM Module Refactoring Design

## 🎯 Goals

1. ✅ **Simplified API** - One method, not multiple
2. ✅ **Provider Registry** - Easy provider management
3. ✅ **Token Limiting** - With custom token counter
4. ✅ **System Messages** - Full support
5. ✅ **Standardized Function Calling** - OpenAI format
6. ✅ **Function Registry** - Centralized tool management
7. ✅ **Thinking Mode** - For reasoning models
8. ✅ **Event-Driven** - Callbacks for all events
9. ✅ **Provider Capabilities** - Check support & list models
10. ✅ **Common Patterns** - Error handling, retry, token counting, context mgmt
11. ✅ **Multiple Tool Calls** - Sequential execution in one response
12. ✅ **Auto Summarization** - Smart context management with custom summarizer

---

## 🏗️ New Architecture

```
┌─────────────────────────────────────────────────┐
│           ProviderRegistry (Singleton)          │
│  - register(name, provider)                     │
│  - get(name) → Provider                         │
│  - setActive(name)                              │
│  - getActive() → Provider                       │
└────────────────┬────────────────────────────────┘
                 │
      ┌──────────┴──────────┐
      │                     │
┌─────▼─────┐      ┌────────▼────────┐
│  OpenAI   │      │   Anthropic     │
│ Provider  │      │   Provider      │
└───────────┘      └─────────────────┘

Each Provider implements:
├── chat(messages, options)
├── configure(config)
├── getCapabilities()
├── getModels()
└── countTokens(text)
```

---

## 📦 New Components

### 1. **Provider Interface**

```java
public interface LLMProvider {
    // Core
    String getName();
    void configure(ProviderConfig config);
    
    // Chat
    CompletableFuture<ChatResponse> chat(
        List<Message> messages, 
        ChatOptions options
    );
    
    // Streaming with events
    void chatStream(
        List<Message> messages,
        ChatOptions options,
        ChatEventListener listener
    );
    
    // Capabilities
    ProviderCapabilities getCapabilities();
    List<ModelInfo> getModels();
    
    // Token management
    int countTokens(String text);
    int countTokens(List<Message> messages);
    TokenCounter getTokenCounter();
    void setTokenCounter(TokenCounter counter);
}
```

### 2. **ProviderRegistry**

```java
public class ProviderRegistry {
    private static ProviderRegistry instance;
    private Map<String, LLMProvider> providers;
    private String activeProvider;
    
    public static ProviderRegistry getInstance() { ... }
    
    public void register(String name, LLMProvider provider) { ... }
    public LLMProvider get(String name) { ... }
    public void setActive(String name) { ... }
    public LLMProvider getActive() { ... }
    public List<String> getAvailableProviders() { ... }
}
```

### 3. **TokenCounter Interface**

```java
public interface TokenCounter {
    int count(String text);
    int count(List<Message> messages);
    int estimate(String text); // Fast approximation
}

public class DefaultTokenCounter implements TokenCounter {
    // GPT-2/GPT-3 tokenizer approximation
    @Override
    public int count(String text) {
        // ~4 chars per token average
        return text.length() / 4;
    }
}

public class TikTokenCounter implements TokenCounter {
    // Accurate GPT tokenizer (if library available)
}
```

### 4. **Message with System Support**

```java
public class Message {
    public enum Role {
        SYSTEM,    // NEW!
        USER,
        ASSISTANT,
        FUNCTION,
        TOOL
    }
    
    private Role role;
    private String content;
    private String name;        // For function/tool messages
    private Object toolCalls;   // For assistant calling tools
    
    // Builder
    public static Message system(String content) { ... }
    public static Message user(String content) { ... }
    public static Message assistant(String content) { ... }
    public static Message function(String name, String result) { ... }
}
```

### 5. **Standardized Tool/Function Format**

```java
public class Tool {
    private String type = "function";  // Always "function"
    private FunctionDefinition function;
    
    public static Tool function(
        String name,
        String description,
        JsonSchema parameters
    ) { ... }
}

public class FunctionDefinition {
    private String name;
    private String description;
    private JsonSchema parameters;
}

public class JsonSchema {
    private String type = "object";
    private Map<String, PropertySchema> properties;
    private List<String> required;
}
```

### 6. **FunctionRegistry**

```java
public class FunctionRegistry {
    private static FunctionRegistry instance;
    private Map<String, RegisteredFunction> functions;
    
    public void register(String name, RegisteredFunction function) { ... }
    public RegisteredFunction get(String name) { ... }
    public List<Tool> getAllTools() { ... }
    public Object execute(String name, Map<String, Object> args) { ... }
}

public interface RegisteredFunction {
    String getName();
    String getDescription();
    JsonSchema getParameters();
    Object execute(Map<String, Object> args);
}
```

### 7. **Thinking Mode**

```java
public class ChatOptions {
    private String model;
    private double temperature = 0.7;
    private int maxTokens = 2000;
    private List<Tool> tools;
    private boolean stream = false;
    private boolean thinking = false;  // NEW! For o1/o3 models
    private int maxThinkingTokens = 5000;  // NEW!
}

// In response
public class ChatResponse {
    private String content;
    private String thinkingContent;  // NEW! Internal reasoning
    private Usage usage;
    private List<ToolCall> toolCalls;  // NEW! Can be multiple!
    
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
    
    public boolean hasMultipleToolCalls() {
        return toolCalls != null && toolCalls.size() > 1;
    }
}
```

### 8. **Event System**

```java
public interface ChatEventListener {
    void onToken(String token);
    void onThinking(String thinkingToken);  // For reasoning
    void onToolCall(ToolCall toolCall);
    void onComplete(ChatResponse response);
    void onError(Throwable error);
}

// Adapter for convenience
public class ChatEventAdapter implements ChatEventListener {
    @Override public void onToken(String token) {}
    @Override public void onThinking(String thinkingToken) {}
    @Override public void onToolCall(ToolCall toolCall) {}
    @Override public void onComplete(ChatResponse response) {}
    @Override public void onError(Throwable error) {}
}
```

### 9. **Provider Capabilities**

```java
public class ProviderCapabilities {
    private boolean supportsStreaming;
    private boolean supportsFunctionCalling;
    private boolean supportsThinking;
    private boolean supportsVision;
    private boolean supportsAudio;
    private int maxTokens;
    private int maxContextWindow;
    private List<String> supportedFeatures;
}

public class ModelInfo {
    private String id;
    private String name;
    private String description;
    private int contextWindow;
    private boolean supportsTools;
    private boolean supportsThinking;
    private boolean supportsVision;
    private double costPer1kTokens;
}
```

### 10. **Common Patterns**

```java
// Error Handling
public class LLMErrorHandler {
    public <T> T withRetry(Callable<T> task, RetryConfig config) { ... }
    public void handleError(Throwable error) { ... }
}

// Token Counting
public class ContextWindowManager {
    public List<Message> fitToWindow(
        List<Message> messages, 
        int maxTokens
    ) { ... }
    
    public List<Message> trimOldest(
        List<Message> messages,
        int targetTokens
    ) { ... }
}

// Retry Logic
public class RetryConfig {
    private int maxRetries = 3;
    private long initialDelayMs = 1000;
    private double backoffMultiplier = 2.0;
    private Set<Class<? extends Exception>> retryableExceptions;
}
```

### 11. **Multiple Tool Calls (NEW!)**

```java
public class ToolCall {
    private String id;           // Unique ID for this call
    private String type = "function";
    private String name;         // Function name
    private Map<String, Object> arguments;
    private int index;           // Order in sequence
}

public class ToolExecutor {
    private FunctionRegistry registry;
    
    // Execute all tool calls sequentially
    public List<ToolResult> executeAll(List<ToolCall> calls) {
        List<ToolResult> results = new ArrayList<>();
        for (ToolCall call : calls) {
            try {
                Object result = registry.execute(call.getName(), call.getArguments());
                results.add(ToolResult.success(call.getId(), result));
            } catch (Exception e) {
                results.add(ToolResult.error(call.getId(), e.getMessage()));
            }
        }
        return results;
    }
    
    // Execute with dependencies (parallel where possible)
    public List<ToolResult> executeOptimized(List<ToolCall> calls) {
        // Analyze dependencies
        // Execute in parallel where possible
        // Return results in order
    }
}

public class ToolResult {
    private String toolCallId;
    private boolean success;
    private Object result;
    private String error;
    
    public static ToolResult success(String id, Object result) { ... }
    public static ToolResult error(String id, String error) { ... }
}
```

### 12. **Conversation Summarization (NEW!)**

```java
public interface ConversationSummarizer {
    // Generate summary of messages
    String summarize(List<Message> messages);
    
    // Get strategy name
    String getStrategyName();
}

// Default implementation using LLM
public class LLMSummarizer implements ConversationSummarizer {
    private LLMProvider provider;
    private String summaryPrompt = 
        "Summarize this conversation concisely, preserving key information:";
    
    @Override
    public String summarize(List<Message> messages) {
        // Use LLM to generate summary
        Message systemMsg = Message.system(summaryPrompt);
        List<Message> toSummarize = new ArrayList<>();
        toSummarize.add(systemMsg);
        toSummarize.addAll(messages);
        
        ChatResponse response = provider.chat(
            toSummarize, 
            ChatOptions.builder().maxTokens(500).build()
        ).get();
        
        return response.getContent();
    }
}

// Simple extractive summarizer
public class ExtractiveSummarizer implements ConversationSummarizer {
    @Override
    public String summarize(List<Message> messages) {
        // Extract key sentences
        // No LLM needed - faster, cheaper
        return extractKeyPoints(messages);
    }
}

// Custom summarizer
public class CustomSummarizer implements ConversationSummarizer {
    private Function<List<Message>, String> customStrategy;
    
    public CustomSummarizer(Function<List<Message>, String> strategy) {
        this.customStrategy = strategy;
    }
    
    @Override
    public String summarize(List<Message> messages) {
        return customStrategy.apply(messages);
    }
}

// Context manager with auto-summarization
public class SmartContextManager {
    private ContextWindowManager windowManager;
    private ConversationSummarizer summarizer;
    private int maxTokens;
    private int summarizeThreshold;  // e.g., 80% of max
    
    public List<Message> manageContext(List<Message> messages) {
        int currentTokens = windowManager.countTokens(messages);
        
        if (currentTokens > summarizeThreshold) {
            // Auto-summarize old messages
            List<Message> toKeep = getRecentMessages(messages, maxTokens / 2);
            List<Message> toSummarize = getOldMessages(messages, maxTokens / 2);
            
            if (!toSummarize.isEmpty()) {
                String summary = summarizer.summarize(toSummarize);
                Message summaryMsg = Message.system(
                    "[Previous conversation summary]: " + summary
                );
                
                List<Message> result = new ArrayList<>();
                result.add(summaryMsg);
                result.addAll(toKeep);
                return result;
            }
        }
        
        return messages;
    }
}
```

---

## 🎯 Usage Examples

### **1. Provider Registry**

```java
// Setup
ProviderRegistry registry = ProviderRegistry.getInstance();

// Register OpenAI
LLMProvider openai = new OpenAIProvider();
openai.configure(ProviderConfig.builder()
    .apiKey("sk-...")
    .model("gpt-4-turbo-preview")
    .build());
registry.register("openai", openai);

// Register Anthropic
LLMProvider anthropic = new AnthropicProvider();
anthropic.configure(ProviderConfig.builder()
    .apiKey("sk-ant-...")
    .model("claude-3-5-sonnet-20241022")
    .build());
registry.register("anthropic", anthropic);

// Set active
registry.setActive("openai");

// Use active provider
LLMProvider provider = registry.getActive();
```

### **2. Simple Chat with System Message**

```java
List<Message> messages = List.of(
    Message.system("You are a helpful assistant"),
    Message.user("Hello!")
);

ChatResponse response = provider.chat(messages, ChatOptions.defaults()).get();
System.out.println(response.getContent());
```

### **3. Streaming with Events**

```java
provider.chatStream(messages, ChatOptions.defaults(), new ChatEventAdapter() {
    @Override
    public void onToken(String token) {
        System.out.print(token);
    }
    
    @Override
    public void onComplete(ChatResponse response) {
        System.out.println("\n[Done] Tokens: " + response.getUsage().getTotalTokens());
    }
    
    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }
});
```

### **4. Function Calling with Multiple Tool Calls**

```java
// Register functions
FunctionRegistry functions = FunctionRegistry.getInstance();

functions.register("search_projects", new RegisteredFunction() {
    @Override
    public String getName() { return "search_projects"; }
    
    @Override
    public String getDescription() { 
        return "Search for projects in database"; 
    }
    
    @Override
    public JsonSchema getParameters() {
        return JsonSchema.builder()
            .property("query", PropertySchema.string("Search query"))
            .required("query")
            .build();
    }
    
    @Override
    public Object execute(Map<String, Object> args) {
        String query = (String) args.get("query");
        // Execute search...
        return results;
    }
});

functions.register("get_project_details", new RegisteredFunction() {
    // Another function...
});

// Use with chat
ChatOptions options = ChatOptions.builder()
    .tools(functions.getAllTools())
    .build();

ChatResponse response = provider.chat(messages, options).get();

if (response.hasToolCalls()) {
    // Execute ALL tool calls sequentially
    ToolExecutor executor = new ToolExecutor(functions);
    List<ToolResult> results = executor.executeAll(response.getToolCalls());
    
    // Add all results to conversation
    for (ToolResult result : results) {
        messages.add(Message.tool(
            result.getToolCallId(), 
            result.getResultAsString()
        ));
    }
    
    // Continue conversation with all tool results
    response = provider.chat(messages, options).get();
    System.out.println(response.getContent());
}

// Example: LLM returns multiple tool calls
// User: "Search for 'noteflix' and get details for project 123"
// LLM Response: {
//   toolCalls: [
//     { id: "1", name: "search_projects", arguments: {query: "noteflix"} },
//     { id: "2", name: "get_project_details", arguments: {id: 123} }
//   ]
// }
// Both execute, both results sent back, LLM synthesizes final answer
```

### **5. Thinking Mode (o1/o3)**

```java
ChatOptions options = ChatOptions.builder()
    .model("o1-preview")
    .thinking(true)
    .maxThinkingTokens(5000)
    .build();

provider.chatStream(messages, options, new ChatEventAdapter() {
    @Override
    public void onThinking(String thinkingToken) {
        System.out.print("[THINKING] " + thinkingToken);
    }
    
    @Override
    public void onToken(String token) {
        System.out.print(token);
    }
});
```

### **6. Token Limiting**

```java
// Custom token counter
provider.setTokenCounter(new TikTokenCounter());

// Check before sending
int tokenCount = provider.countTokens(messages);
if (tokenCount > 4000) {
    messages = ContextWindowManager.trimOldest(messages, 3000);
}

// Set max tokens
ChatOptions options = ChatOptions.builder()
    .maxTokens(2000)
    .build();
```

### **7. Check Capabilities**

```java
ProviderCapabilities caps = provider.getCapabilities();

if (caps.supportsFunctionCalling()) {
    // Use tools
}

if (caps.supportsThinking()) {
    // Enable thinking mode
}

// List models
List<ModelInfo> models = provider.getModels();
for (ModelInfo model : models) {
    System.out.println(model.getName() + " - " + model.getContextWindow() + " tokens");
}
```

### **8. Auto-Summarization for Long Conversations**

```java
// Setup smart context manager
SmartContextManager contextManager = new SmartContextManager(
    provider.getTokenCounter(),
    new LLMSummarizer(provider),  // or ExtractiveSummarizer, or custom
    4000,  // max tokens
    3200   // summarize when > 3200 tokens (80%)
);

// Use in conversation loop
List<Message> conversation = new ArrayList<>();
conversation.add(Message.system("You are a helpful assistant"));

while (userContinues) {
    // Add user message
    conversation.add(Message.user(userInput));
    
    // Auto-manage context (summarize if needed)
    conversation = contextManager.manageContext(conversation);
    
    // Chat with managed context
    ChatResponse response = provider.chat(conversation, options).get();
    conversation.add(Message.assistant(response.getContent()));
    
    // Context automatically stays within limits!
}

// Custom summarizer example
ConversationSummarizer customSummarizer = new CustomSummarizer(messages -> {
    // Your custom logic
    StringBuilder summary = new StringBuilder("Key points:\n");
    for (Message msg : messages) {
        if (msg.getRole() == Role.USER) {
            summary.append("- User asked: ").append(extractKeyPhrase(msg)).append("\n");
        }
    }
    return summary.toString();
});

contextManager.setSummarizer(customSummarizer);
```

### **9. Error Handling & Retry**

```java
LLMErrorHandler errorHandler = new LLMErrorHandler();

ChatResponse response = errorHandler.withRetry(() -> {
    return provider.chat(messages, options).get();
}, RetryConfig.builder()
    .maxRetries(3)
    .initialDelayMs(1000)
    .backoffMultiplier(2.0)
    .retryOn(RateLimitException.class, TimeoutException.class)
    .build());
```

---

## 📁 New File Structure

```
llm/
├── api/
│   ├── LLMProvider.java               # Main provider interface
│   ├── TokenCounter.java              # Token counting interface
│   ├── ChatEventListener.java        # Event callbacks
│   └── RegisteredFunction.java       # Function interface
│
├── model/
│   ├── Message.java                   # Enhanced with SYSTEM role
│   ├── ChatOptions.java               # All chat options
│   ├── ChatResponse.java              # Response with thinking
│   ├── Tool.java                      # Standardized tool format
│   ├── FunctionDefinition.java       # Function metadata
│   ├── JsonSchema.java                # JSON schema for params
│   ├── ToolCall.java                  # Tool call result
│   ├── ProviderCapabilities.java     # Provider features
│   ├── ModelInfo.java                 # Model metadata
│   └── Usage.java                     # Token usage stats
│
├── provider/
│   ├── openai/
│   │   └── OpenAIProvider.java        # Implements LLMProvider
│   ├── anthropic/
│   │   └── AnthropicProvider.java     # Implements LLMProvider
│   └── ollama/
│       └── OllamaProvider.java        # Implements LLMProvider
│
├── registry/
│   ├── ProviderRegistry.java          # Provider management
│   └── FunctionRegistry.java          # Function management
│
├── token/
│   ├── DefaultTokenCounter.java       # Simple counter
│   ├── TikTokenCounter.java           # Accurate counter
│   ├── ContextWindowManager.java      # Context management
│   └── SmartContextManager.java       # Auto-summarization
│
├── summarizer/
│   ├── ConversationSummarizer.java    # Interface
│   ├── LLMSummarizer.java             # Use LLM to summarize
│   ├── ExtractiveSummarizer.java      # Fast extractive
│   └── CustomSummarizer.java          # Custom strategy
│
├── tool/
│   ├── ToolExecutor.java              # Execute tool calls
│   ├── ToolResult.java                # Execution result
│   └── ToolDependencyAnalyzer.java    # Parallel execution
│
├── error/
│   ├── LLMException.java
│   ├── LLMErrorHandler.java           # Error handling
│   └── RetryConfig.java               # Retry configuration
│
└── examples/
    ├── BasicChatExample.java
    ├── StreamingExample.java
    ├── FunctionCallingExample.java
    └── ThinkingModeExample.java
```

---

## 🚀 Migration Path

### Phase 1: Core Infrastructure
1. Create new interfaces
2. Create registries
3. Create token counter

### Phase 2: Provider Implementation
4. Refactor OpenAI provider
5. Refactor Anthropic provider
6. Refactor Ollama provider

### Phase 3: Advanced Features
7. Add function registry
8. Add thinking mode
9. Add event system

### Phase 4: Polish
10. Add error handling
11. Update examples
12. Update documentation

---

## ✅ Benefits

1. ✅ **Simpler API** - One method, consistent interface
2. ✅ **Provider Agnostic** - Easy to switch providers
3. ✅ **Event-Driven** - Real-time feedback for UI
4. ✅ **Token Control** - Prevent overruns
5. ✅ **Standardized** - OpenAI-compatible format
6. ✅ **Extensible** - Easy to add features
7. ✅ **Professional** - Production-ready patterns

---

**Status:** 🎨 Design Complete - Ready for Implementation

