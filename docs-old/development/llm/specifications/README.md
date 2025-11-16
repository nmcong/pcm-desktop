# LLM Module Specification Documents

> **Tài liệu đặc tả đầy đủ cho module LLM của PCM Desktop**

---

## 📚 Danh Mục Tài Liệu

### 1️⃣ [LLM_REFACTOR_DESIGN.md](./LLM_REFACTOR_DESIGN.md)

**Thiết kế kiến trúc tổng thể cho LLM Module**

**Nội dung chính:**

- ✅ Simplified API - Một phương thức duy nhất `provider.chat()`
- ✅ Provider Registry - Quản lý và chuyển đổi providers
- ✅ Token Limiting - Giới hạn tokens với custom counter
- ✅ System Messages - Hỗ trợ đầy đủ system messages
- ✅ Standardized Function Calling - Định dạng chuẩn OpenAI
- ✅ Function Registry - Quản lý tập trung các functions
- ✅ Thinking Mode - Hỗ trợ reasoning models (o1, o3)
- ✅ Event-Driven - Callbacks: onToken, onComplete, onError, onThinking, onToolCall
- ✅ Provider Capabilities - Check support & list models
- ✅ Common Patterns - Error handling, retry, token counting, context management

**Kiến trúc:**

```
ProviderRegistry
  ├── OpenAI Provider
  ├── Anthropic Provider
  └── Ollama Provider

Each Provider implements:
  ├── chat(messages, options)
  ├── configure(config)
  ├── getCapabilities()
  ├── getModels()
  └── countTokens(text)
```

**Đọc khi:**

- Cần hiểu tổng quan về kiến trúc LLM module
- Thiết kế providers mới
- Implement core features

---

### 2️⃣ [LLM_MULTIPLE_TOOLS_AND_SUMMARY.md](./LLM_MULTIPLE_TOOLS_AND_SUMMARY.md)

**Chi tiết về multiple tool calls và auto-summarization**

**Nội dung chính:**

- ✅ **Multiple Tool Calls** - LLM có thể gọi nhiều tools trong một response
- ✅ **Sequential Execution** - Thực thi tools theo thứ tự
- ✅ **Parallel Execution** - Tối ưu với dependency analysis
- ✅ **Auto Summarization** - Tự động tóm tắt khi conversation dài
- ✅ **Custom Summarizer** - Tùy chỉnh cách tóm tắt
- ✅ **Smart Context Management** - Quản lý context window thông minh

**Tool Execution Strategies:**

- Sequential: Thực thi từng tool theo thứ tự
- Parallel: Phân tích dependencies và chạy song song khi có thể

**Summarization Strategies:**

- LLMSummarizer: Dùng LLM để tóm tắt (chất lượng cao)
- ExtractiveSummarizer: Trích xuất key points (nhanh, rẻ)
- CustomSummarizer: Tùy chỉnh logic riêng

**Đọc khi:**

- Implement function calling với nhiều tools
- Cần quản lý conversations dài
- Tối ưu token usage

---

### 3️⃣ [LLM_FUNCTION_ANNOTATION_DESIGN.md](./LLM_FUNCTION_ANNOTATION_DESIGN.md)

**Annotation-based function definition với auto-scanning**

**Nội dung chính:**

- ✅ **@LLMFunction** - Đánh dấu methods là LLM-callable
- ✅ **@Param** - Mô tả parameters với validation
- ✅ **@FunctionProvider** - Đánh dấu classes chứa functions
- ✅ **Auto Scanning** - Tự động quét và đăng ký functions
- ✅ **DI Integration** - Thực thi trong application context
- ✅ **Reflection-based Execution** - Gọi methods động

**Example:**

```java
@FunctionProvider
public class SearchFunctions {
    
    @LLMFunction(
        name = "search_projects",
        description = "Search for projects in database"
    )
    public List<Project> searchProjects(
        @Param(description = "Search query", required = true)
        String query,
        
        @Param(description = "Maximum results", defaultValue = "10")
        int limit
    ) {
        // Implementation
        return projectService.search(query, limit);
    }
}
```

**Auto-registration:**

```java
// At startup
FunctionRegistry registry = FunctionRegistry.getInstance();
registry.scanPackage("com.noteflix.pcm.functions");
// All @LLMFunction methods are now registered!
```

**Đọc khi:**

- Implement custom functions cho LLM
- Setup function scanning
- Integrate với DI container

---

### 4️⃣ [LLM_LOGGING_DESIGN.md](./LLM_LOGGING_DESIGN.md)

**Logging và audit trail system cho LLM calls**

**Nội dung chính:**

- ✅ **Complete Audit Trail** - Lưu đầy đủ request/response
- ✅ **Tool Execution Logs** - Log từng tool call với params & results
- ✅ **Multiple Storage** - Database (SQLite) hoặc File (JSON)
- ✅ **Async Logging** - Không block LLM calls
- ✅ **Queryable** - Search và filter logs dễ dàng
- ✅ **Analytics** - Token usage, cost tracking, performance metrics

**Data Models:**

- **LLMCallLog**: Toàn bộ thông tin 1 LLM call
    - Request: messages, options, tools
    - Response: content, thinking, tool calls, usage
    - Metadata: timestamp, duration, user, session
    - Error: có lỗi không, error message

- **ToolCallLog**: Chi tiết execution của 1 tool
    - Input: tool name, arguments
    - Output: result hoặc error
    - Timing: execution duration

**Storage Options:**

- **DatabaseLogger**: SQLite với indexes (fast queries)
- **FileLogger**: JSON files organized by date (simple)
- **CompositeLogger**: Log to multiple destinations

**Đọc khi:**

- Setup logging cho production
- Cần tracking costs
- Debug LLM interactions
- Compliance requirements

---

### 5️⃣ [LLM_TOOL_CACHE_AND_PROMPTS.md](./LLM_TOOL_CACHE_AND_PROMPTS.md)

**Tool result caching và prompt template system**

**Nội dung chính:**

#### **A. Tool Result Caching**

- ✅ **Cache Strategy Pattern** - Linh hoạt chọn cách xử lý tool results
- ✅ **Smart Summarization** - Tự động quyết định full/summary
- ✅ **Token Budget Control** - Kiểm soát chi phí tokens
- ✅ **Adaptive Learning** - Học từ usage patterns

**Caching Strategies:**

1. **AlwaysFullStrategy** (Default)
    - Luôn gửi full results
    - Accuracy tối đa
    - Use case: Khi chính xác quan trọng nhất

2. **SmartSummarizationStrategy**
    - Tự động quyết định dựa trên kích thước
    - Small results: full
    - Large results: summarize
    - Medium: depends on context window
    - Use case: Cân bằng accuracy vs cost

3. **TokenBudgetStrategy**
    - Strict token limit per tool result
    - Use case: Budget constraints

4. **AdaptiveStrategy**
    - Học từ history
    - Cache & reuse similar results
    - Avoid summarization nếu gây lỗi
    - Use case: Production optimization

**Cache Decision Logic:**

```java
CacheDecision {
    shouldCache: boolean        // Cache để reuse?
    shouldSummarize: boolean    // Summarize trước khi gửi LLM?
    summarizationStrategy: String
    reason: String
}
```

#### **B. Prompt Template System**

- ✅ **Template Registry** - Quản lý tập trung prompts
- ✅ **Variable Substitution** - Dynamic prompt generation
- ✅ **i18n Support** - Multi-language prompts
- ✅ **File-based Loading** - Load từ files
- ✅ **Advanced Features** - Conditionals, loops, nested variables

**Built-in Templates:**

- `system.default` - Default system message
- `system.with_role` - System message với role
- `summarize.conversation` - Tóm tắt conversation
- `summarize.tool_result` - Tóm tắt tool result
- `function.instruction` - Function calling instructions
- `thinking.instruction` - Thinking mode instructions
- `error.tool_execution` - Tool error recovery

**Multi-language Example:**

```java
promptRegistry.register("system.helpful", I18nPromptTemplate.builder()
    .templatesByLocale(Map.of(
        Locale.ENGLISH, "You are a helpful AI assistant.",
        Locale.forLanguageTag("vi"), "Bạn là một trợ lý AI hữu ích.",
        Locale.CHINESE, "你是一个有用的AI助手。"
    ))
    .build());

promptRegistry.setLocale(Locale.forLanguageTag("vi"));
String prompt = promptRegistry.render("system.helpful", Map.of());
// Output: "Bạn là một trợ lý AI hữu ích."
```

**Đọc khi:**

- Cần tối ưu token costs
- Setup prompt templates
- Implement multi-language support
- Cache tool results

---

### 6️⃣ [LLM_MODULE_STRUCTURE.md](./LLM_MODULE_STRUCTURE.md)

**Current structure của LLM module (existing code)**

**Nội dung chính:**

- 📂 Package structure hiện tại
- 📄 Các file quan trọng
- 🔍 Code cần refactor

**Đọc khi:**

- Cần hiểu code hiện tại
- Planning refactoring
- Onboarding new developers

---

## 🎯 Quick Reference

### **Tôi muốn...**

| Mục tiêu                     | Đọc tài liệu                                                             | Phần cụ thể              |
|------------------------------|--------------------------------------------------------------------------|--------------------------|
| Hiểu tổng quan kiến trúc     | [LLM_REFACTOR_DESIGN.md](./LLM_REFACTOR_DESIGN.md)                       | § New Architecture       |
| Setup providers              | [LLM_REFACTOR_DESIGN.md](./LLM_REFACTOR_DESIGN.md)                       | § Provider Registry      |
| Implement function calling   | [LLM_FUNCTION_ANNOTATION_DESIGN.md](./LLM_FUNCTION_ANNOTATION_DESIGN.md) | Toàn bộ                  |
| Handle multiple tool calls   | [LLM_MULTIPLE_TOOLS_AND_SUMMARY.md](./LLM_MULTIPLE_TOOLS_AND_SUMMARY.md) | § Multiple Tool Calls    |
| Setup logging                | [LLM_LOGGING_DESIGN.md](./LLM_LOGGING_DESIGN.md)                         | § Complete Example       |
| Tối ưu token costs           | [LLM_TOOL_CACHE_AND_PROMPTS.md](./LLM_TOOL_CACHE_AND_PROMPTS.md)         | § Tool Result Caching    |
| Customize prompts            | [LLM_TOOL_CACHE_AND_PROMPTS.md](./LLM_TOOL_CACHE_AND_PROMPTS.md)         | § Prompt Template System |
| Multi-language prompts       | [LLM_TOOL_CACHE_AND_PROMPTS.md](./LLM_TOOL_CACHE_AND_PROMPTS.md)         | § Multi-language Support |
| Auto-summarize conversations | [LLM_MULTIPLE_TOOLS_AND_SUMMARY.md](./LLM_MULTIPLE_TOOLS_AND_SUMMARY.md) | § Auto Summarization     |

---

## 📊 Feature Matrix

| Feature             | Design Doc                                    | Priority  | Status  |
|---------------------|-----------------------------------------------|-----------|---------|
| Provider Registry   | [Design](./LLM_REFACTOR_DESIGN.md)            | 🔴 High   | Pending |
| Token Counter       | [Design](./LLM_REFACTOR_DESIGN.md)            | 🔴 High   | Pending |
| Function Registry   | [Design](./LLM_REFACTOR_DESIGN.md)            | 🔴 High   | Pending |
| Event System        | [Design](./LLM_REFACTOR_DESIGN.md)            | 🔴 High   | Pending |
| Multiple Tool Calls | [Design](./LLM_MULTIPLE_TOOLS_AND_SUMMARY.md) | 🟡 Medium | Pending |
| Auto Summarization  | [Design](./LLM_MULTIPLE_TOOLS_AND_SUMMARY.md) | 🟡 Medium | Pending |
| Annotation Scanning | [Design](./LLM_FUNCTION_ANNOTATION_DESIGN.md) | 🟡 Medium | Pending |
| LLM Call Logging    | [Design](./LLM_LOGGING_DESIGN.md)             | 🔴 High   | Pending |
| Tool Result Caching | [Design](./LLM_TOOL_CACHE_AND_PROMPTS.md)     | 🟡 Medium | Pending |
| Prompt Templates    | [Design](./LLM_TOOL_CACHE_AND_PROMPTS.md)     | 🟢 Low    | Pending |

---

## 🏗️ Implementation Order

### **Phase 1: Core Infrastructure** 🔴

1. Provider Interface & Registry
2. Token Counter (default + custom)
3. Message models với System support
4. Event system (ChatEventListener)

**Docs:** [LLM_REFACTOR_DESIGN.md](./LLM_REFACTOR_DESIGN.md)

### **Phase 2: Function Calling** 🔴

1. Standardized Tool format
2. FunctionRegistry basic
3. Annotation scanning
4. DI integration

**Docs:
** [LLM_REFACTOR_DESIGN.md](./LLM_REFACTOR_DESIGN.md), [LLM_FUNCTION_ANNOTATION_DESIGN.md](./LLM_FUNCTION_ANNOTATION_DESIGN.md)

### **Phase 3: Providers** 🔴

1. Refactor OpenAI provider
2. Refactor Anthropic provider
3. Refactor Ollama provider
4. Provider capabilities

**Docs:** [LLM_REFACTOR_DESIGN.md](./LLM_REFACTOR_DESIGN.md)

### **Phase 4: Advanced Features** 🟡

1. Multiple tool calls support
2. Tool result caching
3. Auto-summarization
4. Thinking mode

**Docs:
** [LLM_MULTIPLE_TOOLS_AND_SUMMARY.md](./LLM_MULTIPLE_TOOLS_AND_SUMMARY.md), [LLM_TOOL_CACHE_AND_PROMPTS.md](./LLM_TOOL_CACHE_AND_PROMPTS.md)

### **Phase 5: Observability** 🔴

1. LLM call logging
2. Tool execution logging
3. Analytics & statistics
4. UI integration

**Docs:** [LLM_LOGGING_DESIGN.md](./LLM_LOGGING_DESIGN.md)

### **Phase 6: Polish** 🟢

1. Prompt template system
2. i18n prompts
3. Error handling improvements
4. Documentation & examples

**Docs:** [LLM_TOOL_CACHE_AND_PROMPTS.md](./LLM_TOOL_CACHE_AND_PROMPTS.md)

---

## 🔑 Key Design Decisions

### **1. Why Provider Registry Pattern?**

- ✅ Easy to switch between providers
- ✅ Centralized configuration
- ✅ Consistent API across providers
- ✅ Support multiple active providers

### **2. Why Annotation-based Functions?**

- ✅ Declarative & clean
- ✅ Auto-discovery
- ✅ Type-safe with validation
- ✅ Easy to maintain

### **3. Why Cache Tool Results?**

- ✅ Avoid redundant expensive operations
- ✅ Control token costs
- ✅ Improve response time
- ✅ Flexible strategies per use case

### **4. Why Prompt Templates?**

- ✅ Separate prompts from code
- ✅ Easy A/B testing
- ✅ Multi-language support
- ✅ Version control friendly

### **5. Why Comprehensive Logging?**

- ✅ Debugging LLM interactions
- ✅ Cost tracking & optimization
- ✅ Audit trail for compliance
- ✅ Performance monitoring

---

## 📖 How to Read These Docs

### **For Architects/Tech Leads:**

1. Start with [LLM_REFACTOR_DESIGN.md](./LLM_REFACTOR_DESIGN.md) - Overall architecture
2. Review all § Architecture sections
3. Evaluate design decisions
4. Plan implementation phases

### **For Developers (Implementing):**

1. Read relevant spec for your feature
2. Follow code examples
3. Check § Integration sections
4. Refer to § File Structure for where to put code

### **For New Team Members:**

1. Start with [LLM_MODULE_STRUCTURE.md](./LLM_MODULE_STRUCTURE.md) - Current state
2. Read [LLM_REFACTOR_DESIGN.md](./LLM_REFACTOR_DESIGN.md) - Future state
3. Skim other docs to understand capabilities
4. Deep dive when implementing specific features

---

## 🎨 Design Principles

All specifications follow these principles:

1. **Simple by Default** - Easy to use for common cases
2. **Flexible for Advanced** - Powerful features available when needed
3. **Provider Agnostic** - Work with any LLM provider
4. **Type Safe** - Leverage Java type system
5. **Observable** - Log everything, measure everything
6. **Maintainable** - Clean architecture, clear separation
7. **Testable** - All components are unit testable

---

## 🚀 Getting Started

### **To Implement a New Feature:**

1. **Read the spec** - Find relevant document above
2. **Understand the design** - Review architecture diagrams
3. **Check dependencies** - See implementation order
4. **Write tests first** - Test-driven development
5. **Implement** - Follow code examples in specs
6. **Integrate** - Connect with existing components
7. **Document** - Update examples and README

### **To Propose Changes:**

1. **Identify the issue** - What's wrong with current design?
2. **Suggest solution** - How to improve?
3. **Update spec** - Modify relevant document
4. **Discuss trade-offs** - What are pros/cons?
5. **Get consensus** - Team agreement
6. **Implement** - Make it happen!

---

## 📞 Questions?

**Architecture Questions:**

- Review [LLM_REFACTOR_DESIGN.md](./LLM_REFACTOR_DESIGN.md) first
- Check design principles section
- Discuss with tech lead

**Implementation Questions:**

- Find the relevant spec
- Check code examples
- Refer to § Integration sections

**Missing Information:**

- Document is incomplete
- Open an issue or discussion
- Propose additions to specs

---

## 📝 Document Status

| Document                          | Version | Last Updated | Status     |
|-----------------------------------|---------|--------------|------------|
| LLM_REFACTOR_DESIGN.md            | 1.0     | 2025-11-12   | ✅ Complete |
| LLM_MULTIPLE_TOOLS_AND_SUMMARY.md | 1.0     | 2025-11-12   | ✅ Complete |
| LLM_FUNCTION_ANNOTATION_DESIGN.md | 1.0     | 2025-11-12   | ✅ Complete |
| LLM_LOGGING_DESIGN.md             | 1.0     | 2025-11-12   | ✅ Complete |
| LLM_TOOL_CACHE_AND_PROMPTS.md     | 1.0     | 2025-11-12   | ✅ Complete |
| README.md (this)                  | 1.0     | 2025-11-12   | ✅ Complete |

---

**All designs are complete and ready for implementation!** 🎉

Next step: Start Phase 1 implementation (Core Infrastructure)

