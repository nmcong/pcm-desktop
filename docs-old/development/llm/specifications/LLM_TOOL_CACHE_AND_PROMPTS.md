# LLM Tool Result Caching & Prompt Templates

## 🎯 Goals

1. ✅ **Tool Result Caching** - Cache tool execution results
2. ✅ **Configurable Caching Strategy** - When to cache, when to summarize
3. ✅ **Prompt Template System** - Customizable prompts for all operations
4. ✅ **Template Variables** - Dynamic prompt generation
5. ✅ **Multi-language Support** - i18n for prompts
6. ✅ **Template Registry** - Centralized prompt management

---

## 🔧 Problem 1: Tool Result Caching

### **The Challenge**

Khi LLM gọi tool và nhận kết quả, chúng ta có 2 chiến lược:

**Option A: Gửi nguyên bản (No Summarization)**

- ✅ Giữ nguyên toàn bộ context, chính xác 100%
- ❌ Token cost cao khi kết quả lớn
- ❌ Có thể vượt quá context window

**Option B: Summarize trước khi gửi**

- ✅ Tiết kiệm tokens
- ✅ Fit được nhiều tool calls hơn trong context
- ❌ Có thể mất thông tin quan trọng
- ❌ Thêm latency (phải chờ summarize)

### **The Solution: Flexible Caching Strategy**

```java
public interface ToolResultCacheStrategy {
    
    /**
     * Determine if tool result should be cached as-is or summarized
     */
    CacheDecision decide(ToolExecutionContext context);
    
    /**
     * Get cached result if available
     */
    Optional<CachedToolResult> getFromCache(String toolName, Map<String, Object> args);
    
    /**
     * Store result in cache
     */
    void cache(String toolName, Map<String, Object> args, Object result, String summary);
}

@Data
@Builder
public class ToolExecutionContext {
    private String toolName;
    private Map<String, Object> arguments;
    private Object rawResult;
    private int resultTokenCount;
    private int remainingContextTokens;
    private boolean isLastInSequence;
    private ToolMetadata metadata;
}

@Data
public class CacheDecision {
    private boolean shouldCache;           // Cache for reuse?
    private boolean shouldSummarize;       // Summarize before sending to LLM?
    private String summarizationStrategy;  // "extractive", "llm", "none"
    private String reason;                 // Why this decision?
}

@Data
public class CachedToolResult {
    private String toolName;
    private Map<String, Object> arguments;
    private Object rawResult;              // Original result
    private String summary;                // Summarized version
    private LocalDateTime cachedAt;
    private Duration ttl;                  // Time to live
    private int useCount;                  // How many times reused
}
```

### **Built-in Caching Strategies**

#### **1. AlwaysFullStrategy** (Default - No Summarization)

```java
public class AlwaysFullStrategy implements ToolResultCacheStrategy {
    
    @Override
    public CacheDecision decide(ToolExecutionContext context) {
        return CacheDecision.builder()
            .shouldCache(true)
            .shouldSummarize(false)
            .summarizationStrategy("none")
            .reason("Always send full results for maximum accuracy")
            .build();
    }
}
```

**Use case:** Khi accuracy quan trọng nhất, token cost không phải vấn đề

#### **2. SmartSummarizationStrategy** (Intelligent)

```java
@Slf4j
public class SmartSummarizationStrategy implements ToolResultCacheStrategy {
    
    private static final int SMALL_RESULT_THRESHOLD = 500;   // tokens
    private static final int LARGE_RESULT_THRESHOLD = 2000;  // tokens
    
    @Override
    public CacheDecision decide(ToolExecutionContext context) {
        int resultTokens = context.getResultTokenCount();
        int remainingTokens = context.getRemainingContextTokens();
        
        // Small results: always full
        if (resultTokens < SMALL_RESULT_THRESHOLD) {
            return CacheDecision.builder()
                .shouldCache(true)
                .shouldSummarize(false)
                .summarizationStrategy("none")
                .reason("Result is small enough to send in full")
                .build();
        }
        
        // Large results: always summarize
        if (resultTokens > LARGE_RESULT_THRESHOLD) {
            return CacheDecision.builder()
                .shouldCache(true)
                .shouldSummarize(true)
                .summarizationStrategy("extractive")
                .reason("Result is too large, must summarize")
                .build();
        }
        
        // Medium results: depends on remaining context
        if (remainingTokens < resultTokens * 1.5) {
            return CacheDecision.builder()
                .shouldCache(true)
                .shouldSummarize(true)
                .summarizationStrategy("extractive")
                .reason("Not enough context window remaining")
                .build();
        }
        
        // Default: send full
        return CacheDecision.builder()
            .shouldCache(true)
            .shouldSummarize(false)
            .summarizationStrategy("none")
            .reason("Sufficient context window available")
            .build();
    }
}
```

**Use case:** Cân bằng giữa accuracy và cost, tự động quyết định dựa trên kích thước

#### **3. AdaptiveStrategy** (Learning)

```java
public class AdaptiveStrategy implements ToolResultCacheStrategy {
    
    private Map<String, ToolStatistics> statistics = new ConcurrentHashMap<>();
    
    @Override
    public CacheDecision decide(ToolExecutionContext context) {
        ToolStatistics stats = statistics.get(context.getToolName());
        
        if (stats == null) {
            // First time: use smart strategy
            return new SmartSummarizationStrategy().decide(context);
        }
        
        // Learn from past: if tool results are often similar, cache & reuse
        if (stats.getCacheHitRate() > 0.7) {
            // Check cache first
            String cacheKey = generateCacheKey(context);
            if (hasInCache(cacheKey)) {
                return CacheDecision.builder()
                    .shouldCache(false)  // Already cached
                    .shouldSummarize(false)
                    .reason("Using cached result from similar previous call")
                    .build();
            }
        }
        
        // Learn from past: if summaries caused issues, prefer full
        if (stats.getSummaryErrorRate() > 0.3) {
            return CacheDecision.builder()
                .shouldCache(true)
                .shouldSummarize(false)
                .reason("Summaries have caused errors, sending full result")
                .build();
        }
        
        // Default to smart strategy
        return new SmartSummarizationStrategy().decide(context);
    }
}
```

**Use case:** Production systems that learn from usage patterns

#### **4. TokenBudgetStrategy** (Cost-Conscious)

```java
public class TokenBudgetStrategy implements ToolResultCacheStrategy {
    
    private int maxTokensPerToolResult;
    
    public TokenBudgetStrategy(int maxTokensPerToolResult) {
        this.maxTokensPerToolResult = maxTokensPerToolResult;
    }
    
    @Override
    public CacheDecision decide(ToolExecutionContext context) {
        if (context.getResultTokenCount() <= maxTokensPerToolResult) {
            return CacheDecision.builder()
                .shouldCache(true)
                .shouldSummarize(false)
                .summarizationStrategy("none")
                .reason("Within token budget")
                .build();
        }
        
        return CacheDecision.builder()
            .shouldCache(true)
            .shouldSummarize(true)
            .summarizationStrategy("extractive")
            .reason("Exceeds token budget: " + maxTokensPerToolResult)
            .build();
    }
}
```

**Use case:** Strict token budget requirements

### **Tool Result Cache Implementation**

```java
@Slf4j
public class ToolResultCache {
    
    private Map<String, CachedToolResult> cache = new ConcurrentHashMap<>();
    private ToolResultCacheStrategy strategy;
    private ToolResultSummarizer summarizer;
    
    public ToolResultCache(ToolResultCacheStrategy strategy) {
        this.strategy = strategy;
        this.summarizer = new ToolResultSummarizer();
    }
    
    /**
     * Process tool result according to cache strategy
     */
    public ProcessedToolResult process(ToolExecutionContext context) {
        // Check cache first
        String cacheKey = generateCacheKey(context.getToolName(), context.getArguments());
        Optional<CachedToolResult> cached = Optional.ofNullable(cache.get(cacheKey));
        
        if (cached.isPresent() && !isExpired(cached.get())) {
            log.debug("Cache hit for tool: {}", context.getToolName());
            CachedToolResult result = cached.get();
            result.setUseCount(result.getUseCount() + 1);
            
            CacheDecision decision = strategy.decide(context);
            return ProcessedToolResult.builder()
                .rawResult(result.getRawResult())
                .displayResult(decision.isShouldSummarize() ? 
                    result.getSummary() : result.getRawResult().toString())
                .fromCache(true)
                .build();
        }
        
        // Not in cache, process new result
        CacheDecision decision = strategy.decide(context);
        
        String displayResult;
        String summary = null;
        
        if (decision.isShouldSummarize()) {
            // Summarize result
            summary = summarizer.summarize(
                context.getRawResult(), 
                decision.getSummarizationStrategy()
            );
            displayResult = summary;
            log.debug("Summarized tool result: {} -> {} tokens", 
                context.getResultTokenCount(), 
                countTokens(summary));
        } else {
            // Use full result
            displayResult = context.getRawResult().toString();
        }
        
        // Cache if requested
        if (decision.isShouldCache()) {
            CachedToolResult cacheEntry = CachedToolResult.builder()
                .toolName(context.getToolName())
                .arguments(context.getArguments())
                .rawResult(context.getRawResult())
                .summary(summary)
                .cachedAt(LocalDateTime.now())
                .ttl(Duration.ofHours(1))  // Configurable
                .useCount(0)
                .build();
            
            cache.put(cacheKey, cacheEntry);
            log.debug("Cached tool result: {}", cacheKey);
        }
        
        return ProcessedToolResult.builder()
            .rawResult(context.getRawResult())
            .displayResult(displayResult)
            .fromCache(false)
            .wasSummarized(decision.isShouldSummarize())
            .tokensSaved(decision.isShouldSummarize() ? 
                context.getResultTokenCount() - countTokens(summary) : 0)
            .build();
    }
    
    private String generateCacheKey(String toolName, Map<String, Object> args) {
        // Create deterministic key from tool name + args
        return toolName + ":" + JsonUtils.toJson(args);
    }
    
    private boolean isExpired(CachedToolResult result) {
        return LocalDateTime.now().isAfter(
            result.getCachedAt().plus(result.getTtl())
        );
    }
}

@Data
@Builder
public class ProcessedToolResult {
    private Object rawResult;          // Original result (for app use)
    private String displayResult;      // What to send to LLM
    private boolean fromCache;
    private boolean wasSummarized;
    private int tokensSaved;
}
```

### **Integration with Provider**

```java
public abstract class BaseProvider implements LLMProvider {
    
    protected ToolResultCache toolCache;
    
    protected List<Message> executeToolsAndBuildMessages(
        String callId,
        List<ToolCall> toolCalls,
        List<Message> conversation
    ) {
        List<Message> newMessages = new ArrayList<>();
        
        for (ToolCall call : toolCalls) {
            // Execute tool
            Object rawResult = FunctionRegistry.getInstance()
                .execute(call.getName(), call.getArguments());
            
            // Count tokens in result
            int resultTokens = tokenCounter.count(rawResult.toString());
            int remainingTokens = maxContextTokens - 
                tokenCounter.count(conversation);
            
            // Build context for cache strategy
            ToolExecutionContext context = ToolExecutionContext.builder()
                .toolName(call.getName())
                .arguments(call.getArguments())
                .rawResult(rawResult)
                .resultTokenCount(resultTokens)
                .remainingContextTokens(remainingTokens)
                .isLastInSequence(call == toolCalls.get(toolCalls.size() - 1))
                .build();
            
            // Process with cache strategy
            ProcessedToolResult processed = toolCache.process(context);
            
            // Create message with processed result
            Message toolMessage = Message.tool(
                call.getId(),
                processed.getDisplayResult()  // May be full or summary
            );
            
            newMessages.add(toolMessage);
            
            // Log
            log.info("Tool execution: {} | Cache: {} | Summarized: {} | Tokens saved: {}",
                call.getName(),
                processed.isFromCache(),
                processed.isWasSummarized(),
                processed.getTokensSaved());
        }
        
        return newMessages;
    }
}
```

---

## 📝 Problem 2: Prompt Template System

### **Why Templates?**

Hiện tại các prompts bị hardcode:

- ❌ Không thể customize
- ❌ Không có i18n support
- ❌ Khó maintain khi thay đổi
- ❌ Không thể A/B test

### **The Solution: Template System**

```java
public interface PromptTemplate {
    String render(Map<String, Object> variables);
    String getName();
    String getDescription();
}

@Data
@Builder
public class SimplePromptTemplate implements PromptTemplate {
    
    private String name;
    private String description;
    private String template;
    private Set<String> requiredVariables;
    
    @Override
    public String render(Map<String, Object> variables) {
        // Validate required variables
        for (String required : requiredVariables) {
            if (!variables.containsKey(required)) {
                throw new IllegalArgumentException(
                    "Missing required variable: " + required
                );
            }
        }
        
        // Simple string replacement
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, String.valueOf(entry.getValue()));
        }
        
        return result;
    }
}
```

### **Template Registry**

```java
public class PromptTemplateRegistry {
    
    private static PromptTemplateRegistry instance;
    private Map<String, PromptTemplate> templates = new ConcurrentHashMap<>();
    private Locale currentLocale = Locale.ENGLISH;
    
    public static PromptTemplateRegistry getInstance() {
        if (instance == null) {
            instance = new PromptTemplateRegistry();
            instance.registerDefaultTemplates();
        }
        return instance;
    }
    
    public void register(String key, PromptTemplate template) {
        templates.put(key, template);
    }
    
    public PromptTemplate get(String key) {
        PromptTemplate template = templates.get(key);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + key);
        }
        return template;
    }
    
    public String render(String key, Map<String, Object> variables) {
        return get(key).render(variables);
    }
    
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        reloadTemplatesForLocale(locale);
    }
    
    private void registerDefaultTemplates() {
        // System message templates
        register("system.default", SimplePromptTemplate.builder()
            .name("Default System Message")
            .template("You are a helpful AI assistant.")
            .requiredVariables(Collections.emptySet())
            .build());
        
        register("system.with_role", SimplePromptTemplate.builder()
            .name("System Message with Role")
            .template("You are a helpful AI assistant specialized in {role}.")
            .requiredVariables(Set.of("role"))
            .build());
        
        // Summarization templates
        register("summarize.conversation", SimplePromptTemplate.builder()
            .name("Conversation Summarization")
            .template("""
                Please summarize the following conversation concisely, 
                preserving all key information, decisions made, and important context.
                Focus on actionable items and main topics discussed.
                
                Conversation:
                {messages}
                """)
            .requiredVariables(Set.of("messages"))
            .build());
        
        register("summarize.tool_result", SimplePromptTemplate.builder()
            .name("Tool Result Summarization")
            .template("""
                Summarize the following tool execution result in a concise way,
                keeping only the most relevant information for {purpose}.
                
                Tool: {tool_name}
                Result:
                {result}
                """)
            .requiredVariables(Set.of("tool_name", "result", "purpose"))
            .build());
        
        // Function calling templates
        register("function.instruction", SimplePromptTemplate.builder()
            .name("Function Calling Instructions")
            .template("""
                You have access to the following functions:
                {functions}
                
                When you need to use a function, respond with a function call.
                You can make multiple function calls in sequence if needed.
                """)
            .requiredVariables(Set.of("functions"))
            .build());
        
        // Thinking mode templates
        register("thinking.instruction", SimplePromptTemplate.builder()
            .name("Thinking Mode Instructions")
            .template("""
                Think step by step. Show your reasoning process before providing 
                the final answer. Consider multiple approaches and evaluate them.
                """)
            .requiredVariables(Collections.emptySet())
            .build());
        
        // Error recovery templates
        register("error.tool_execution", SimplePromptTemplate.builder()
            .name("Tool Execution Error")
            .template("""
                The function call to '{tool_name}' failed with error: {error_message}
                
                Please try again with corrected parameters, or suggest an alternative approach.
                """)
            .requiredVariables(Set.of("tool_name", "error_message"))
            .build());
    }
}
```

### **Multi-language Support**

```java
public class I18nPromptTemplate implements PromptTemplate {
    
    private String name;
    private Map<Locale, String> templatesByLocale;
    private Set<String> requiredVariables;
    private Locale fallbackLocale = Locale.ENGLISH;
    
    @Override
    public String render(Map<String, Object> variables) {
        Locale currentLocale = PromptTemplateRegistry.getInstance()
            .getCurrentLocale();
        
        String template = templatesByLocale.getOrDefault(
            currentLocale,
            templatesByLocale.get(fallbackLocale)
        );
        
        // Render with variables
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            template = template.replace(placeholder, String.valueOf(entry.getValue()));
        }
        
        return template;
    }
}

// Usage
PromptTemplateRegistry registry = PromptTemplateRegistry.getInstance();

registry.register("system.helpful", I18nPromptTemplate.builder()
    .name("Helpful Assistant")
    .templatesByLocale(Map.of(
        Locale.ENGLISH, "You are a helpful AI assistant.",
        Locale.forLanguageTag("vi"), "Bạn là một trợ lý AI hữu ích.",
        Locale.CHINESE, "你是一个有用的AI助手。",
        Locale.JAPANESE, "あなたは役立つAIアシスタントです。"
    ))
    .build());

registry.setLocale(Locale.forLanguageTag("vi"));
String prompt = registry.render("system.helpful", Map.of());
// Output: "Bạn là một trợ lý AI hữu ích."
```

### **Advanced Template Features**

```java
public class AdvancedPromptTemplate implements PromptTemplate {
    
    private String template;
    private TemplateEngine engine;  // Mustache, Freemarker, etc.
    
    @Override
    public String render(Map<String, Object> variables) {
        // Support for:
        // - Conditionals: {{#if variable}}...{{/if}}
        // - Loops: {{#each items}}...{{/each}}
        // - Nested variables: {{user.name}}
        // - Helpers: {{uppercase text}}
        
        return engine.process(template, variables);
    }
}

// Example: Conditional formatting
String template = """
    You are a helpful AI assistant.
    {{#if role}}
    You specialize in {role}.
    {{/if}}
    {{#if tools}}
    You have access to: {{#each tools}}{this}, {{/each}}
    {{/if}}
    """;

Map<String, Object> vars = Map.of(
    "role", "software engineering",
    "tools", List.of("search", "calculator", "database")
);

// Output:
// You are a helpful AI assistant.
// You specialize in software engineering.
// You have access to: search, calculator, database,
```

### **Template Loading from Files**

```java
public class FileBasedPromptTemplateRegistry extends PromptTemplateRegistry {
    
    private Path templatesDirectory;
    
    public FileBasedPromptTemplateRegistry(Path templatesDirectory) {
        this.templatesDirectory = templatesDirectory;
        loadTemplatesFromDirectory();
    }
    
    private void loadTemplatesFromDirectory() {
        try (Stream<Path> paths = Files.walk(templatesDirectory)) {
            paths.filter(p -> p.toString().endsWith(".template"))
                 .forEach(this::loadTemplate);
        } catch (IOException e) {
            log.error("Failed to load templates", e);
        }
    }
    
    private void loadTemplate(Path file) {
        try {
            // Parse template file
            // Format:
            // ---
            // name: System Message
            // description: Default system message
            // variables: role, context
            // ---
            // You are a {role} assistant. {context}
            
            String content = Files.readString(file);
            PromptTemplateFile parsed = parseTemplateFile(content);
            
            register(parsed.getKey(), SimplePromptTemplate.builder()
                .name(parsed.getName())
                .description(parsed.getDescription())
                .template(parsed.getTemplate())
                .requiredVariables(parsed.getVariables())
                .build());
                
        } catch (IOException e) {
            log.error("Failed to load template: {}", file, e);
        }
    }
}
```

### **Template Usage in Provider**

```java
public class OpenAIProvider extends BaseProvider {
    
    private PromptTemplateRegistry promptRegistry = 
        PromptTemplateRegistry.getInstance();
    
    @Override
    public CompletableFuture<ChatResponse> chat(
        List<Message> messages,
        ChatOptions options
    ) {
        // Add system message from template if not present
        if (messages.stream().noneMatch(m -> m.getRole() == Role.SYSTEM)) {
            String systemPrompt = promptRegistry.render(
                "system.default",
                Map.of()
            );
            messages.add(0, Message.system(systemPrompt));
        }
        
        // Add function calling instructions if tools provided
        if (options.getTools() != null && !options.getTools().isEmpty()) {
            String functionPrompt = promptRegistry.render(
                "function.instruction",
                Map.of("functions", formatTools(options.getTools()))
            );
            messages.add(1, Message.system(functionPrompt));
        }
        
        // Add thinking mode instructions if enabled
        if (options.isThinking()) {
            String thinkingPrompt = promptRegistry.render(
                "thinking.instruction",
                Map.of()
            );
            messages.add(Message.system(thinkingPrompt));
        }
        
        return super.chat(messages, options);
    }
}
```

---

## 🎯 Complete Usage Example

### **1. Setup Cache Strategy**

```java
// At application startup
ToolResultCache toolCache = new ToolResultCache(
    new SmartSummarizationStrategy()
    // Or: new AlwaysFullStrategy()
    // Or: new TokenBudgetStrategy(1000)
    // Or: new AdaptiveStrategy()
);

// Configure providers
OpenAIProvider openai = new OpenAIProvider();
openai.setToolCache(toolCache);
```

### **2. Setup Prompt Templates**

```java
// Load templates from files
PromptTemplateRegistry promptRegistry = new FileBasedPromptTemplateRegistry(
    Paths.get("prompts/")
);

// Or register programmatically
promptRegistry.register("custom.coder", SimplePromptTemplate.builder()
    .name("Expert Coder")
    .template("""
        You are an expert {language} developer with {years} years of experience.
        You write clean, efficient, and well-documented code.
        """)
    .requiredVariables(Set.of("language", "years"))
    .build());

// Set language
promptRegistry.setLocale(Locale.forLanguageTag("vi"));
```

### **3. Use in Conversation**

```java
// Build messages with templates
List<Message> messages = new ArrayList<>();

messages.add(Message.system(
    promptRegistry.render("custom.coder", Map.of(
        "language", "Java",
        "years", 10
    ))
));

messages.add(Message.user("How do I implement a binary search tree?"));

// Make LLM call
ChatResponse response = provider.chat(messages, ChatOptions.builder()
    .tools(functionRegistry.getAllTools())
    .build()
).get();

// Tool execution (with automatic caching/summarization)
if (response.hasToolCalls()) {
    for (ToolCall call : response.getToolCalls()) {
        // Execute tool - cache automatically handles result
        Object result = functionRegistry.execute(call.getName(), call.getArguments());
        
        // Tool cache decides: full or summary?
        // Result is added to conversation automatically
    }
}
```

### **4. Custom Summarization Template**

```java
// Define custom summarization strategy
promptRegistry.register("summarize.code_search", SimplePromptTemplate.builder()
    .name("Code Search Result Summarization")
    .template("""
        The following code search returned {count} results.
        Summarize the most relevant findings for the query: "{query}"
        
        Results:
        {results}
        
        Provide:
        1. Key files/functions found
        2. Main patterns identified
        3. Recommendation for which result to explore first
        """)
    .requiredVariables(Set.of("count", "query", "results"))
    .build());

// Use in tool execution
if (toolResultRequiresSummarization) {
    String summary = promptRegistry.render("summarize.code_search", Map.of(
        "count", searchResults.size(),
        "query", searchQuery,
        "results", formatResults(searchResults)
    ));
    
    // Send summary instead of full results
    messages.add(Message.tool(toolCallId, summary));
}
```

---

## 📁 File Structure

```
llm/
├── cache/
│   ├── ToolResultCache.java              # Main cache
│   ├── ToolResultCacheStrategy.java      # Strategy interface
│   ├── AlwaysFullStrategy.java
│   ├── SmartSummarizationStrategy.java
│   ├── TokenBudgetStrategy.java
│   ├── AdaptiveStrategy.java
│   ├── CachedToolResult.java
│   ├── ProcessedToolResult.java
│   └── ToolResultSummarizer.java
│
├── prompt/
│   ├── PromptTemplate.java               # Template interface
│   ├── PromptTemplateRegistry.java       # Registry
│   ├── SimplePromptTemplate.java         # Basic template
│   ├── I18nPromptTemplate.java           # Multi-language
│   ├── AdvancedPromptTemplate.java       # Conditional/loops
│   └── FileBasedPromptTemplateRegistry.java
│
└── provider/
    └── BaseProvider.java                  # Integrated with cache & templates
```

---

## ✅ Benefits

### **Tool Result Caching:**

1. ✅ **Flexibility** - Choose strategy per use case
2. ✅ **Cost Control** - Smart summarization saves tokens
3. ✅ **Performance** - Cache reuse avoids redundant execution
4. ✅ **Accuracy** - Can prioritize full results when needed
5. ✅ **Adaptive** - Learn from usage patterns

### **Prompt Templates:**

1. ✅ **Maintainability** - Centralized prompt management
2. ✅ **Customization** - Easy to modify without code changes
3. ✅ **i18n Support** - Multi-language prompts
4. ✅ **Consistency** - Reusable prompt patterns
5. ✅ **A/B Testing** - Easy to test different prompts
6. ✅ **Version Control** - Track prompt changes over time

---

## 🎨 Design Principles

1. **Default to Simple** - AlwaysFullStrategy by default (accuracy first)
2. **Make it Configurable** - Easy to switch strategies
3. **Optimize When Needed** - Add smart caching when cost/performance matters
4. **Template Everything** - All prompts should be templates
5. **Support i18n** - Multi-language from day one
6. **Log Everything** - Track cache hits, token savings, template usage

---

**Status:** 🎨 Complete Design - Ready for Implementation!

