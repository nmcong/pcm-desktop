# 🤖 LLM Integration - PCM Desktop

## 📋 Overview

Hệ thống tích hợp LLM linh hoạt, extensible, tuân thủ SOLID principles và clean code practices.

### ✨ Key Features

- ✅ **Multiple Providers**: OpenAI, Anthropic, Ollama, Custom APIs
- ✅ **Streaming Support**: Real-time response streaming (SSE)
- ✅ **Function Calling**: Tool/function execution
- ✅ **Flexible Configuration**: URL + Token per provider
- ✅ **Easy Extension**: Add new provider in < 2 hours
- ✅ **Type-safe**: Strong typing throughout
- ✅ **Clean Architecture**: SOLID principles applied

---

## 📚 Documentation

### Main Documents

1. **[LLM_INTEGRATION_PLAN.md](./development/LLM_INTEGRATION_PLAN.md)** - Complete Implementation Plan
    - Architecture design
    - SOLID principles explained
    - Design patterns
    - 6-week implementation phases
    - Provider examples
    - Testing strategy

2. **[LLM_QUICK_START.md](./development/LLM_QUICK_START.md)** - Quick Start Guide
    - Core concepts
    - Usage examples
    - Adding new providers
    - Configuration examples
    - Common issues

---

## 🏗️ Architecture

```
┌─────────────────────────────────┐
│      UI Layer (JavaFX)          │
│  Chat UI, Streaming Display     │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│    Service Layer (Business)     │
│  LLMService, ConversationService│
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│  Client Layer (Abstraction)     │
│  LLMClient Interface            │
└────────────┬────────────────────┘
             │
    ┌────────┴────────┬─────────┐
    │                 │         │
┌───▼───┐  ┌─────────▼──┐  ┌──▼────┐
│OpenAI │  │ Anthropic  │  │Custom │
│Client │  │  Client    │  │Client │
└───────┘  └────────────┘  └───────┘
```

---

## 📦 Package Structure

```
com.noteflix.pcm.llm/
├── api/                    # ✅ Interfaces
│   ├── LLMClient.java
│   ├── StreamingCapable.java
│   └── FunctionCallingCapable.java
│
├── model/                  # ✅ Data models  
│   ├── LLMRequest.java
│   ├── LLMResponse.java
│   ├── Message.java
│   ├── LLMChunk.java
│   ├── FunctionDefinition.java
│   ├── FunctionCall.java
│   ├── StreamingObserver.java
│   └── LLMProviderConfig.java
│
├── client/                 # ⏳ Provider implementations
│   ├── openai/
│   ├── anthropic/
│   ├── ollama/
│   └── custom/
│
├── factory/                # ⏳ Factory
│   └── LLMClientFactory.java
│
└── service/                # ⏳ Services
    └── LLMService.java
```

---

## 💻 Quick Example

### Basic Chat

```java
// Configure provider
LLMProviderConfig config = LLMProviderConfig.builder()
    .provider(Provider.OPENAI)
    .url("https://api.openai.com/v1/chat/completions")
    .token(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
    .build();

// Create client
LLMClient client = LLMClientFactory.createClient(config);

// Send message
LLMRequest request = LLMRequest.builder()
    .model("gpt-4")
    .messages(List.of(Message.user("What is Java?")))
    .build();

LLMResponse response = client.sendMessage(request);
System.out.println(response.getContent());
```

### Streaming Chat

```java
if (client instanceof StreamingCapable streamingClient) {
    streamingClient.streamMessage(request, new StreamingObserver() {
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
            error.printStackTrace();
        }
    });
}
```

### Function Calling

```java
FunctionDefinition func = FunctionDefinition.builder()
    .name("get_weather")
    .description("Get current weather")
    .parameters(createJsonSchema())
    .build();

if (client instanceof FunctionCallingCapable funcClient) {
    LLMResponse response = funcClient.sendWithFunctions(
        request, 
        List.of(func)
    );
    
    if (response.hasFunctionCall()) {
        // Execute function
        Object result = executeFunction(response.getFunctionCall());
        // Send result back
    }
}
```

---

## ✅ SOLID Principles

### Single Responsibility

```java
LLMClient        → Send/receive messages
StreamingCapable → Handle streaming
FunctionExecutor → Execute functions
```

### Open/Closed

```java
// Add new provider WITHOUT modifying existing code
public class NewProvider implements LLMClient { }
```

### Liskov Substitution

```java
// Any LLMClient implementation works
LLMClient client = new AnyProvider(config);
```

### Interface Segregation

```java
// Implement only what you support
public class SimpleClient implements LLMClient { }
public class FullClient implements LLMClient, StreamingCapable, FunctionCallingCapable { }
```

### Dependency Inversion

```java
// Depend on abstraction
public class Service {
    private final LLMClient client; // Interface
}
```

---

## 🎨 Design Patterns

| Pattern                     | Purpose              | Benefit                    |
|-----------------------------|----------------------|----------------------------|
| **Strategy**                | Provider selection   | Easy provider switching    |
| **Factory**                 | Client creation      | Centralized creation logic |
| **Builder**                 | Request construction | Flexible object building   |
| **Observer**                | Streaming            | Reactive updates           |
| **Adapter**                 | Format conversion    | Uniform interface          |
| **Chain of Responsibility** | Middleware           | Extensible processing      |

---

## 🚀 Implementation Status

### Phase 1: Foundation ✅ COMPLETE

- [x] Core interfaces (LLMClient, StreamingCapable, FunctionCallingCapable)
- [x] Model classes (LLMRequest, LLMResponse, Message, etc.)
- [x] Configuration model
- [x] Documentation

### Phase 2: OpenAI Client ⏳ TODO

- [ ] OpenAIClient implementation
- [ ] Basic chat support
- [ ] Streaming support
- [ ] Function calling support
- [ ] HTTP client wrapper
- [ ] Error handling
- [ ] Unit tests

### Phase 3: Additional Providers ⏳ TODO

- [ ] AnthropicClient (Claude)
- [ ] OllamaClient (Local)
- [ ] CustomClient (Generic)
- [ ] Provider adapters
- [ ] Tests

### Phase 4: Factory & Service ⏳ TODO

- [ ] LLMClientFactory
- [ ] LLMService
- [ ] ConversationService
- [ ] FunctionExecutorService
- [ ] Middleware chain

### Phase 5: UI Integration ⏳ TODO

- [ ] AIAssistantPage integration
- [ ] Streaming UI
- [ ] Function calling UI
- [ ] Provider selection
- [ ] Configuration UI

### Phase 6: Advanced Features ⏳ TODO

- [ ] Response caching
- [ ] Rate limiting
- [ ] Retry logic
- [ ] Token counting
- [ ] Conversation persistence

---

## 🔧 Supported Providers

| Provider      | Status    | Streaming | Function Calling | Config      |
|---------------|-----------|-----------|------------------|-------------|
| **OpenAI**    | ⏳ Planned | ✅ Yes     | ✅ Yes            | URL + Token |
| **Anthropic** | ⏳ Planned | ✅ Yes     | ✅ Yes            | URL + Token |
| **Ollama**    | ⏳ Planned | ✅ Yes     | ❌ No             | URL only    |
| **Custom**    | ⏳ Planned | ⚠️ Maybe  | ⚠️ Maybe         | URL + Token |

---

## 📊 Configuration Examples

### OpenAI

```java
LLMProviderConfig.builder()
    .provider(Provider.OPENAI)
    .url("https://api.openai.com/v1/chat/completions")
    .token(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
    .supportsStreaming(true)
    .supportsFunctionCalling(true)
    .build();
```

### Anthropic (Claude)

```java
LLMProviderConfig.builder()
    .provider(Provider.ANTHROPIC)
    .url("https://api.anthropic.com/v1/messages")
    .token(System.getenv("ANTHROPIC_API_KEY"))
    .model("claude-3-5-sonnet-20241022")
    .supportsStreaming(true)
    .supportsFunctionCalling(true)
    .build();
```

### Ollama (Local)

```java
LLMProviderConfig.builder()
    .provider(Provider.OLLAMA)
    .url("http://localhost:11434/api/chat")
    .token("") // No token
    .model("llama3")
    .supportsStreaming(true)
    .supportsFunctionCalling(false)
    .build();
```

---

## 🎯 Success Metrics

| Metric           | Target    | Status            |
|------------------|-----------|-------------------|
| Add new provider | < 2 hours | ✅ Design complete |
| Response time    | < 3s      | 🎯 To measure     |
| Test coverage    | > 80%     | 📊 To achieve     |
| Code quality     | A rating  | 📈 To achieve     |
| Extensibility    | Easy      | ✅ SOLID applied   |

---

## 📖 Learn More

### Core Concepts

- **LLMClient**: Base interface for all providers
- **StreamingCapable**: Optional streaming support
- **FunctionCallingCapable**: Optional function calling
- **LLMRequest/Response**: Universal message format
- **LLMProviderConfig**: Provider configuration

### Best Practices

- Use interfaces for flexibility
- Implement only what you need (ISP)
- Depend on abstractions (DIP)
- Builder pattern for complex objects
- Observer pattern for streaming

---

## 🆘 Support

### Get Help

- Read [LLM_INTEGRATION_PLAN.md](./development/LLM_INTEGRATION_PLAN.md) for details
- Read [LLM_QUICK_START.md](./development/LLM_QUICK_START.md) for examples
- Check code documentation (JavaDoc)

### Resources

- [OpenAI API Docs](https://platform.openai.com/docs/api-reference)
- [Anthropic API Docs](https://docs.anthropic.com/claude/reference/getting-started-with-the-api)
- [Ollama API](https://github.com/ollama/ollama/blob/main/docs/api.md)

---

## 📝 Next Steps

1. ✅ **Review** the implementation plan
2. ✅ **Understand** SOLID principles applied
3. ⏳ **Start Phase 2**: Implement OpenAIClient
4. ⏳ **Write tests** for all components
5. ⏳ **Add more providers** as needed
6. ⏳ **Integrate with UI** for user-facing features

---

**Status**: ✅ Foundation Complete - Ready for Implementation  
**Created**: November 12, 2025  
**Last Updated**: November 12, 2025  
**Version**: 1.0.0

---

**© 2025 PCM Desktop - Noteflix Team**

