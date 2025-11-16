# 🚀 LLM Module - Quick Start Guide

> Get started with the new LLM module in 5 minutes!

---

## 📦 **What You Get**

✅ **3 Providers Ready:** OpenAI, Anthropic (Claude), Ollama (Local)  
✅ **Streaming Support:** Real-time responses  
✅ **Function Calling:** Let LLM use your tools  
✅ **Event-Driven:** Callbacks for UI integration  
✅ **Production Ready:** Retry, logging, error handling

---

## 🎯 **Basic Usage**

### 1. Setup Provider

```java
import com.noteflix.pcm.llm.provider.*;
import com.noteflix.pcm.llm.registry.ProviderRegistry;
import com.noteflix.pcm.llm.model.*;

// Create provider
OpenAIProvider provider = new OpenAIProvider();

// Configure
provider.configure(ProviderConfig.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4-turbo-preview")
    .build());

// Register
ProviderRegistry registry = ProviderRegistry.getInstance();
registry.register("openai", provider);
registry.setActive("openai");
```

### 2. Simple Chat

```java
LLMProvider provider = ProviderRegistry.getInstance().getActive();

List<Message> messages = List.of(
    Message.system("You are a helpful assistant."),
    Message.user("What is 2+2?")
);

ChatResponse response = provider.chat(messages, ChatOptions.defaults()).get();
System.out.println(response.getContent());
```

### 3. Streaming

```java
provider.chatStream(messages, ChatOptions.defaults(), new ChatEventAdapter() {
    @Override
    public void onToken(String token) {
        System.out.print(token); // Real-time!
    }
    
    @Override
    public void onComplete(ChatResponse response) {
        System.out.println("\nDone!");
    }
});
```

---

## 🎨 **All Providers**

### OpenAI (GPT-4, GPT-3.5)

```java
OpenAIProvider openai = new OpenAIProvider();
openai.configure(ProviderConfig.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4-turbo-preview")
    .build());
```

**Models:**

- `gpt-4-turbo-preview` - Most capable, 128K context
- `gpt-4` - High intelligence, 8K context
- `gpt-3.5-turbo` - Fast & efficient, 16K context

### Anthropic (Claude 3.5, Claude 3)

```java
AnthropicProvider anthropic = new AnthropicProvider();
anthropic.configure(ProviderConfig.builder()
    .apiKey(System.getenv("ANTHROPIC_API_KEY"))
    .model("claude-3-5-sonnet-20241022")
    .build());
```

**Models:**

- `claude-3-5-sonnet-20241022` - Latest, most intelligent
- `claude-3-opus-20240229` - Powerful analysis
- `claude-3-sonnet-20240229` - Balanced
- `claude-3-haiku-20240307` - Fast & efficient

All Claude models: **200K context window!**

### Ollama (Local Models - FREE!)

```java
OllamaProvider ollama = new OllamaProvider();
ollama.configure(ProviderConfig.builder()
    .baseUrl("http://localhost:11434/api")
    .model("llama2")
    .build());
```

**Models:** (Download with `ollama pull <model>`)

- `llama2` - Meta's Llama 2
- `llama3` - Meta's Llama 3
- `mistral` - Mistral 7B
- `phi` - Microsoft Phi
- `gemma` - Google Gemma

**Advantages:** Free, Private, No API key needed!

---

## 🛠️ **Function Calling**

Let the LLM call your functions!

### Define Function

```java
@FunctionProvider
public class WeatherFunctions {
    
    @LLMFunction(description = "Get current weather for a location")
    public Map<String, Object> getWeather(
        @Param(description = "City name", required = true)
        String location
    ) {
        return Map.of(
            "location", location,
            "temperature", "22°C",
            "condition", "Sunny"
        );
    }
}
```

### Register & Use

```java
// Register functions
FunctionRegistry funcRegistry = FunctionRegistry.getInstance();
funcRegistry.scanClass(WeatherFunctions.class);

// Chat with tools
ChatOptions options = ChatOptions.withTools(funcRegistry.getAllTools());
ChatResponse response = provider.chat(messages, options).get();

// Execute tool calls
if (response.hasToolCalls()) {
    ToolExecutor executor = new ToolExecutor(funcRegistry);
    List<ToolResult> results = executor.executeAll(response.getToolCalls());
}
```

---

## 🎯 **UI Integration**

Perfect for JavaFX UI with real-time updates!

```java
// In your ViewModel
public void sendMessage(String userMessage) {
    setBusy(true);
    
    List<Message> messages = buildConversation(userMessage);
    
    provider.chatStream(messages, ChatOptions.defaults(), new ChatEventAdapter() {
        @Override
        public void onToken(String token) {
            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                assistantMessage.append(token);
                scrollToBottom();
            });
        }
        
        @Override
        public void onComplete(ChatResponse response) {
            Platform.runLater(() -> {
                setBusy(false);
                updateTokenCount(response.getTotalTokens());
            });
        }
        
        @Override
        public void onError(Throwable error) {
            Platform.runLater(() -> {
                setBusy(false);
                showError(error.getMessage());
            });
        }
    });
}
```

---

## ⚙️ **Advanced Options**

### Custom Configuration

```java
ChatOptions options = ChatOptions.builder()
    .model("gpt-4-turbo-preview")
    .temperature(0.7)
    .maxTokens(2000)
    .topP(0.9)
    .stop(List.of("END", "STOP"))
    .build();
```

### Token Counting

```java
LLMProvider provider = ProviderRegistry.getInstance().getActive();

int tokens = provider.countTokens(messages);
System.out.println("Tokens: " + tokens);

// Set custom token counter
provider.setTokenCounter(new TikTokenCounter("cl100k_base"));
```

### Provider Capabilities

```java
ProviderCapabilities caps = provider.getCapabilities();

if (caps.isSupportsStreaming()) {
    // Use streaming
}

if (caps.isSupportsFunctionCalling()) {
    // Use function calling
}

System.out.println("Max context: " + caps.getMaxContextWindow());
```

---

## 🔥 **Best Practices**

### 1. Always Use Try-Catch

```java
try {
    ChatResponse response = provider.chat(messages, options).get();
    // Use response
} catch (Exception e) {
    log.error("LLM request failed", e);
    showErrorToUser(e.getMessage());
}
```

### 2. Set Reasonable Timeouts

```java
provider.configure(ProviderConfig.builder()
    .apiKey(apiKey)
    .timeoutMs(30000) // 30 seconds
    .maxRetries(3)
    .build());
```

### 3. Use System Messages

```java
List<Message> messages = List.of(
    Message.system("You are a helpful coding assistant. Be concise."),
    Message.user("How do I sort a list in Java?")
);
```

### 4. Monitor Token Usage

```java
ChatResponse response = provider.chat(messages, options).get();
Usage usage = response.getUsage();

log.info("Tokens used: {} (prompt: {}, completion: {})",
    usage.getTotalTokens(),
    usage.getPromptTokens(),
    usage.getCompletionTokens());
```

---

## 📚 **More Examples**

See `src/main/java/com/noteflix/pcm/llm/examples/ProviderUsageExample.java` for complete examples:

1. Basic setup & chat
2. Streaming responses
3. Using multiple providers
4. Function calling

---

## 🆘 **Troubleshooting**

### Provider not ready?

```java
if (!provider.isReady()) {
    throw new IllegalStateException("Provider not configured");
}
```

### Connection test?

```java
boolean connected = provider.testConnection();
if (!connected) {
    log.warn("Provider connection failed");
}
```

### Check available models?

```java
List<ModelInfo> models = provider.getModels();
models.forEach(m -> 
    System.out.println(m.getName() + ": $" + m.getCostPer1kInputTokens())
);
```

---

## 🎉 **You're Ready!**

You now have:

- ✅ 3 LLM providers (OpenAI, Anthropic, Ollama)
- ✅ Streaming support
- ✅ Function calling
- ✅ Event-driven architecture
- ✅ Production-ready error handling

**Start building amazing AI features!** 🚀

---

## 📖 **Full Documentation**

- **Design Specs:** `docs/development/llm/specifications/`
- **Implementation Guide:** `docs/development/llm/FINAL_IMPLEMENTATION_SUMMARY.md`
- **Examples:** `src/main/java/com/noteflix/pcm/llm/examples/`

---

*Last Updated: November 13, 2025*

