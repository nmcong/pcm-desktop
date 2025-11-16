# Multiple Tool Calls & Auto-Summarization

## 🎯 New Features

### 1. **Multiple Tool Calls in One Response**

LLM có thể trả về nhiều tool calls cùng lúc, execute tuần tự hoặc song song.

### 2. **Auto-Summarization**

Tự động tóm tắt conversation khi gần hết context window, với custom summarizer.

---

## 🔧 Multiple Tool Calls

### **Why?**

- ✅ Efficient: Execute multiple actions in one round trip
- ✅ Natural: "Search for X and get details for Y"
- ✅ Faster: Parallel execution where possible

### **Architecture**

```
User: "Search for 'noteflix' and get details for project 123"
    ↓
LLM Response: {
  toolCalls: [
    { id: "1", name: "search_projects", arguments: {query: "noteflix"} },
    { id: "2", name: "get_project_details", arguments: {id: 123} }
  ]
}
    ↓
ToolExecutor.executeAll() → [Result1, Result2]
    ↓
Add both results to conversation
    ↓
LLM synthesizes final answer with both results
```

### **Implementation**

```java
public class ChatResponse {
    private List<ToolCall> toolCalls;  // Can be multiple!
    
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
    
    public boolean hasMultipleToolCalls() {
        return toolCalls != null && toolCalls.size() > 1;
    }
}

public class ToolCall {
    private String id;           // Unique ID
    private String type;         // "function"
    private String name;         // Function name
    private Map<String, Object> arguments;
    private int index;           // Order in sequence
}

public class ToolExecutor {
    private FunctionRegistry registry;
    
    // Sequential execution (safe, order guaranteed)
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
    
    // Optimized execution (parallel where possible)
    public CompletableFuture<List<ToolResult>> executeOptimized(List<ToolCall> calls) {
        // 1. Analyze dependencies between calls
        Map<ToolCall, Set<ToolCall>> dependencies = analyzeDependencies(calls);
        
        // 2. Create execution plan (topological sort)
        List<List<ToolCall>> executionPlan = createExecutionPlan(calls, dependencies);
        
        // 3. Execute in waves (parallel within wave)
        List<ToolResult> results = new ArrayList<>();
        for (List<ToolCall> wave : executionPlan) {
            List<CompletableFuture<ToolResult>> futures = wave.stream()
                .map(call -> CompletableFuture.supplyAsync(() -> 
                    executeOne(call)
                ))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            results.addAll(futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
        }
        
        return CompletableFuture.completedFuture(results);
    }
}
```

### **Usage Example**

```java
// Register multiple functions
FunctionRegistry registry = FunctionRegistry.getInstance();

registry.register("search_projects", new RegisteredFunction() {
    public Object execute(Map<String, Object> args) {
        String query = (String) args.get("query");
        return projectService.search(query);
    }
});

registry.register("get_project_details", new RegisteredFunction() {
    public Object execute(Map<String, Object> args) {
        int id = (Integer) args.get("id");
        return projectService.getDetails(id);
    }
});

registry.register("analyze_dependencies", new RegisteredFunction() {
    public Object execute(Map<String, Object> args) {
        int projectId = (Integer) args.get("projectId");
        return projectService.analyzeDependencies(projectId);
    }
});

// Chat with tools
List<Message> messages = List.of(
    Message.user("Search for 'noteflix' and analyze its dependencies")
);

ChatOptions options = ChatOptions.builder()
    .tools(registry.getAllTools())
    .build();

ChatResponse response = provider.chat(messages, options).get();

if (response.hasToolCalls()) {
    System.out.println("LLM requested " + response.getToolCalls().size() + " tool calls");
    
    // Execute all tools
    ToolExecutor executor = new ToolExecutor(registry);
    List<ToolResult> results = executor.executeAll(response.getToolCalls());
    
    // Add results to conversation
    for (ToolResult result : results) {
        messages.add(Message.tool(result.getToolCallId(), result.getResultAsString()));
    }
    
    // Get final answer
    response = provider.chat(messages, options).get();
    System.out.println(response.getContent());
}

// Output example:
// LLM requested 2 tool calls
// I found the Noteflix project and analyzed its dependencies. 
// It has 15 dependencies, including JavaFX, Jackson, and SQLite...
```

---

## 📝 Auto-Summarization

### **Why?**

- ✅ **Long conversations**: Keep context within limits
- ✅ **Cost reduction**: Fewer tokens = lower cost
- ✅ **Better responses**: Focus on recent context
- ✅ **Automatic**: No manual intervention

### **Architecture**

```
Conversation: 4000 tokens (threshold: 3200)
    ↓
SmartContextManager detects > threshold
    ↓
Split: [Old messages] + [Recent messages]
    ↓
Summarize old messages
    ↓
Create new context: [Summary] + [Recent messages]
    ↓
Continue conversation (2000 tokens now)
```

### **Implementation**

```java
// Summarizer interface
public interface ConversationSummarizer {
    String summarize(List<Message> messages);
    String getStrategyName();
}

// Strategy 1: LLM-based (best quality)
public class LLMSummarizer implements ConversationSummarizer {
    private LLMProvider provider;
    private String summaryPrompt;
    
    public LLMSummarizer(LLMProvider provider) {
        this.provider = provider;
        this.summaryPrompt = 
            "Summarize this conversation concisely. " +
            "Preserve: key facts, decisions, user preferences. " +
            "Maximum 200 words.";
    }
    
    @Override
    public String summarize(List<Message> messages) {
        List<Message> toSummarize = new ArrayList<>();
        toSummarize.add(Message.system(summaryPrompt));
        toSummarize.addAll(messages);
        
        ChatResponse response = provider.chat(
            toSummarize,
            ChatOptions.builder()
                .maxTokens(500)
                .temperature(0.3)  // More focused
                .build()
        ).get();
        
        return response.getContent();
    }
}

// Strategy 2: Extractive (fast, cheap)
public class ExtractiveSummarizer implements ConversationSummarizer {
    @Override
    public String summarize(List<Message> messages) {
        StringBuilder summary = new StringBuilder("Previous conversation:\n");
        
        // Extract key sentences using heuristics
        for (Message msg : messages) {
            if (msg.getRole() == Role.USER) {
                String key = extractKeyPhrase(msg.getContent());
                summary.append("- User: ").append(key).append("\n");
            } else if (msg.getRole() == Role.ASSISTANT) {
                String key = extractFirstSentence(msg.getContent());
                summary.append("- Assistant: ").append(key).append("\n");
            }
        }
        
        return summary.toString();
    }
}

// Strategy 3: Custom
public class CustomSummarizer implements ConversationSummarizer {
    private Function<List<Message>, String> strategy;
    
    public CustomSummarizer(Function<List<Message>, String> strategy) {
        this.strategy = strategy;
    }
    
    @Override
    public String summarize(List<Message> messages) {
        return strategy.apply(messages);
    }
}

// Smart context manager
public class SmartContextManager {
    private TokenCounter tokenCounter;
    private ConversationSummarizer summarizer;
    private int maxTokens;
    private int summarizeThreshold;
    private int keepRecentTokens;
    
    public SmartContextManager(
        TokenCounter tokenCounter,
        ConversationSummarizer summarizer,
        int maxTokens,
        int summarizeThreshold
    ) {
        this.tokenCounter = tokenCounter;
        this.summarizer = summarizer;
        this.maxTokens = maxTokens;
        this.summarizeThreshold = summarizeThreshold;
        this.keepRecentTokens = maxTokens / 2;  // Keep last 50%
    }
    
    public List<Message> manageContext(List<Message> messages) {
        int currentTokens = tokenCounter.count(messages);
        
        if (currentTokens <= summarizeThreshold) {
            return messages;  // No action needed
        }
        
        log.info("Context exceeds threshold ({} > {}), summarizing...",
            currentTokens, summarizeThreshold);
        
        // Split messages
        SplitResult split = splitMessages(messages, keepRecentTokens);
        
        if (split.oldMessages.isEmpty()) {
            return messages;  // Can't summarize further
        }
        
        // Summarize old messages
        String summary = summarizer.summarize(split.oldMessages);
        
        // Create new context
        List<Message> result = new ArrayList<>();
        
        // Keep system message if exists
        if (!messages.isEmpty() && messages.get(0).getRole() == Role.SYSTEM) {
            result.add(messages.get(0));
        }
        
        // Add summary
        result.add(Message.system(
            "[Previous conversation summary]: " + summary
        ));
        
        // Add recent messages
        result.addAll(split.recentMessages);
        
        int newTokens = tokenCounter.count(result);
        log.info("Context reduced: {} → {} tokens", currentTokens, newTokens);
        
        return result;
    }
    
    private SplitResult splitMessages(List<Message> messages, int keepTokens) {
        // Binary search to find split point
        // Keep as many recent messages as possible within keepTokens
    }
}
```

### **Usage Examples**

#### **Example 1: LLM Summarizer**

```java
LLMProvider provider = registry.getActive();

SmartContextManager contextManager = new SmartContextManager(
    provider.getTokenCounter(),
    new LLMSummarizer(provider),
    4000,   // max tokens
    3200    // summarize at 80%
);

List<Message> conversation = new ArrayList<>();
conversation.add(Message.system("You are a helpful assistant"));

// Long conversation
for (int i = 0; i < 100; i++) {
    conversation.add(Message.user("Tell me about topic " + i));
    
    // Auto-manage context
    conversation = contextManager.manageContext(conversation);
    
    ChatResponse response = provider.chat(conversation, options).get();
    conversation.add(Message.assistant(response.getContent()));
}

// Context stays within limits automatically!
```

#### **Example 2: Extractive Summarizer (Fast & Cheap)**

```java
SmartContextManager contextManager = new SmartContextManager(
    provider.getTokenCounter(),
    new ExtractiveSummarizer(),  // No LLM calls!
    4000,
    3200
);

// Same usage, but faster and no cost for summarization
```

#### **Example 3: Custom Summarizer**

```java
ConversationSummarizer customSummarizer = new CustomSummarizer(messages -> {
    StringBuilder summary = new StringBuilder();
    summary.append("Conversation summary:\n");
    
    // Custom logic: extract only important messages
    for (Message msg : messages) {
        if (msg.getContent().contains("important") || 
            msg.getContent().contains("remember")) {
            summary.append("- ").append(msg.getContent()).append("\n");
        }
    }
    
    return summary.toString();
});

SmartContextManager contextManager = new SmartContextManager(
    provider.getTokenCounter(),
    customSummarizer,
    4000,
    3200
);
```

#### **Example 4: Different Strategies for Different Scenarios**

```java
public class AdaptiveSummarizer implements ConversationSummarizer {
    private LLMSummarizer llmSummarizer;
    private ExtractiveSummarizer extractiveSummarizer;
    
    @Override
    public String summarize(List<Message> messages) {
        int messageCount = messages.size();
        
        if (messageCount < 10) {
            // Few messages: use extractive (fast)
            return extractiveSummarizer.summarize(messages);
        } else {
            // Many messages: use LLM (better quality)
            return llmSummarizer.summarize(messages);
        }
    }
}
```

---

## 🎯 Benefits

### **Multiple Tool Calls**

- ✅ **Efficiency**: One round trip for multiple actions
- ✅ **Natural**: Matches how users think
- ✅ **Performance**: Parallel execution where possible
- ✅ **Flexibility**: Sequential or optimized

### **Auto-Summarization**

- ✅ **Cost Effective**: Fewer tokens = lower bills
- ✅ **Better Context**: Focus on recent, relevant info
- ✅ **Automatic**: No manual management
- ✅ **Customizable**: Choose strategy that fits your needs
- ✅ **Transparent**: See what's being summarized

---

## 📊 Performance Comparison

### **Without Summarization**

```
Conversation length: 100 messages
Total tokens: 8000
Cost: $0.24 per request (at $0.03/1k tokens)
Context limit exceeded: ❌
```

### **With Auto-Summarization**

```
Conversation length: 100 messages
Active context: 2000 tokens (after summarization)
Cost: $0.06 per request
Context limit exceeded: ✅ Never
```

### **Savings: 75% cost reduction!**

---

## 🔄 Complete Example: Everything Together

```java
// Setup
ProviderRegistry registry = ProviderRegistry.getInstance();
LLMProvider provider = registry.getActive();
FunctionRegistry functions = FunctionRegistry.getInstance();

// Register functions
functions.register("search", ...);
functions.register("analyze", ...);
functions.register("recommend", ...);

// Setup smart context with auto-summarization
SmartContextManager contextManager = new SmartContextManager(
    provider.getTokenCounter(),
    new LLMSummarizer(provider),
    4000,  // max tokens
    3200   // summarize threshold
);

// Chat options with tools
ChatOptions options = ChatOptions.builder()
    .tools(functions.getAllTools())
    .temperature(0.7)
    .build();

// Conversation loop
List<Message> conversation = new ArrayList<>();
conversation.add(Message.system("You are a helpful project assistant"));

while (userContinues) {
    // Get user input
    String userInput = getUserInput();
    conversation.add(Message.user(userInput));
    
    // Auto-manage context (summarize if needed)
    conversation = contextManager.manageContext(conversation);
    
    // Chat
    ChatResponse response = provider.chat(conversation, options).get();
    
    // Handle multiple tool calls
    if (response.hasToolCalls()) {
        ToolExecutor executor = new ToolExecutor(functions);
        List<ToolResult> results = executor.executeAll(response.getToolCalls());
        
        for (ToolResult result : results) {
            conversation.add(Message.tool(
                result.getToolCallId(),
                result.getResultAsString()
            ));
        }
        
        // Get final response after tool execution
        response = provider.chat(conversation, options).get();
    }
    
    // Show response
    System.out.println(response.getContent());
    conversation.add(Message.assistant(response.getContent()));
}

// Features working:
// ✅ Auto-summarization (context never exceeded)
// ✅ Multiple tool calls (parallel execution)
// ✅ Clean API (one method for everything)
// ✅ Cost optimized (75% savings)
```

---

**Status:** 🎨 Design Complete - Ready for Implementation

