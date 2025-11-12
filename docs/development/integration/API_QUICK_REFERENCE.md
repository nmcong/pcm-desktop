# API Quick Reference - PCM Desktop

## 🚀 Quick Start

### 1. Basic Setup

```java
// Initialize service
LLMService llmService = new LLMService();

// Configure OpenAI
LLMProviderConfig config = LLMProviderConfig.builder()
    .provider(LLMProviderConfig.Provider.OPENAI)
    .url("https://api.openai.com/v1/chat/completions")
    .token(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
    .build();

llmService.initialize(config);
```

### 2. Simple Chat

```java
String response = llmService.chat("Explain Java Streams");
System.out.println(response);
```

### 3. Advanced Request

```java
LLMRequest request = LLMRequest.builder()
    .model("gpt-4")
    .messages(List.of(
        Message.system("You are a Java expert"),
        Message.user("Write a singleton example")
    ))
    .temperature(0.7)
    .maxTokens(1000)
    .build();

LLMResponse response = llmService.sendMessage(request);
```

### 4. Streaming

```java
llmService.streamMessage(request, new StreamingObserver() {
    @Override
    public void onChunk(LLMChunk chunk) {
        System.out.print(chunk.getContent());
    }
    
    @Override
    public void onComplete() {
        System.out.println("\\n[Done]");
    }
    
    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }
});
```

### 5. Conversation

```java
LLMService.ConversationBuilder chat = llmService.newConversation()
    .addSystemMessage("You are a helpful assistant")
    .addUserMessage("Hello!")
    .temperature(0.8);

LLMResponse response1 = chat.send();
chat.addUserMessage("Tell me about Java");
LLMResponse response2 = chat.send();
```

## 📋 Common Configurations

### OpenAI
```java
.provider(Provider.OPENAI)
.url("https://api.openai.com/v1/chat/completions")
.token(System.getenv("OPENAI_API_KEY"))
.model("gpt-4") // or "gpt-3.5-turbo"
```

### Anthropic Claude  
```java
.provider(Provider.ANTHROPIC)
.url("https://api.anthropic.com/v1/messages")
.token(System.getenv("ANTHROPIC_API_KEY"))
.model("claude-3-5-sonnet-20241022")
.headers(Map.of("anthropic-version", "2023-06-01"))
```

### Local Ollama
```java
.provider(Provider.OLLAMA) 
.url("http://localhost:11434/api/chat")
.model("llama3") // or "mistral", "codellama"
```

## 🔧 Environment Variables

```bash
# .env file
OPENAI_API_KEY=sk-your-openai-key
ANTHROPIC_API_KEY=sk-ant-your-claude-key
```

## ⚠️ Error Handling

```java
try {
    String response = llmService.chat("Hello");
} catch (LLMProviderException e) {
    // API key issues, invalid model, etc.
    log.error("Provider error: {}", e.getMessage());
} catch (LLMException e) {
    // General LLM system errors
    log.error("LLM error: {}", e.getMessage());
}
```

## 📊 Check Capabilities

```java
System.out.println("Provider: " + llmService.getCurrentProvider());
System.out.println("Streaming: " + llmService.supportsStreaming());
System.out.println("Functions: " + llmService.supportsFunctionCalling());
```

## 🔄 Switch Providers

```java
// Start with OpenAI
llmService.initialize(openaiConfig);
String response1 = llmService.chat("Hello");

// Switch to Claude
llmService.switchProvider(claudeConfig);  
String response2 = llmService.chat("Hello");
```

## 💡 Quick Tips

1. **Environment Variables**: Store API keys in `.env` file
2. **Timeouts**: Set reasonable timeouts (30-60s)
3. **Error Handling**: Always wrap in try-catch
4. **Token Limits**: Monitor usage with `response.getUsage()`
5. **Models**: Choose appropriate model for your use case:
   - `gpt-3.5-turbo`: Fast, cost-effective
   - `gpt-4`: More capable, slower, expensive
   - `claude-3-5-sonnet`: Great reasoning, long context

## 📁 File Structure

```
src/main/java/com/noteflix/pcm/llm/
├── service/LLMService.java          # Main service
├── model/LLMProviderConfig.java     # Configuration  
├── model/LLMRequest.java            # Request model
├── model/LLMResponse.java           # Response model
├── examples/LLMUsageExample.java    # Code examples
└── client/                          # Provider implementations
```

---
*For detailed documentation, see [API_INTEGRATION_GUIDE.md](./API_INTEGRATION_GUIDE.md)*