# Quick Answer: Can I Use Non-OpenAI Compatible LLMs?

## ✅ YES, ABSOLUTELY!

You can integrate **ANY** LLM into the system, regardless of its API format!

---

## 🎯 Quick Summary

### What Works?

| LLM Type                                  | Compatibility | Integration Time |
| ----------------------------------------- | ------------- | ---------------- |
| **OpenAI-compatible** (Ollama, LM Studio) | ✅ Perfect    | 5 minutes        |
| **Similar format** (Cohere, AI21)         | ✅ Good       | 30 minutes       |
| **Different format** (Custom APIs)        | ✅ Yes        | 1-2 hours        |
| **WebSocket, GraphQL, gRPC**              | ✅ Yes        | 2-4 hours        |

### How It Works

```
Your LLM (any format)
    ↓
Your Provider (adapter)
    ↓
Standard Format
    ↓
AIPanel (works with all!)
```

---

## 🚀 Quick Start Examples

### Example 1: Ollama (Local) - 5 Minutes

```javascript
// Just inherit from OpenAIProvider and change URL!
export class OllamaProvider extends OpenAIProvider {
  constructor(config = {}) {
    super({
      ...config,
      id: "ollama",
      name: "Ollama (Local)",
      baseURL: "http://localhost:11434/v1",
      apiKeyConfig: { required: false }, // No API key needed!
    });
  }
}

// Register and use
providerRegistry.register(new OllamaProvider());
```

**That's it!** Ollama is OpenAI-compatible, so it just works!

---

### Example 2: Custom API - 30 Minutes

```javascript
export class YourProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "your-llm",
      name: "Your LLM",
      baseURL: "https://your-api.com",
      capabilities: { chat: true, stream: true },
      ...config,
    });
  }

  async chat(messages, options = {}) {
    // 1. Convert our format to yours
    const yourFormat = {
      prompt: messages.map((m) => m.content).join("\n"),
      max_tokens: options.maxTokens || 2048,
      // ... your API's fields
    };

    // 2. Call your API
    const response = await fetch(`${this.baseURL}/generate`, {
      method: "POST",
      headers: { "X-API-Key": this.apiKey },
      body: JSON.stringify(yourFormat),
    });

    const data = await response.json();

    // 3. Return standard format
    return {
      role: "assistant",
      content: data.text || data.output || data.response,
      metadata: { model: data.model, provider: this.id },
    };
  }
}
```

---

## 📚 What You Need

### Minimum (Required)

1. **Create provider class** extending `BaseProvider`
2. **Implement `chat()` method** - Convert formats
3. **Register provider** - Add to registry

### Recommended

- ✅ `healthCheck()` - Test connectivity
- ✅ `getModels()` - List available models
- ✅ `streamChat()` - Streaming support
- ✅ Error handling

### Optional (Advanced)

- ⭐ Function calling
- ⭐ Vision support
- ⭐ Custom features

---

## 🎓 Integration Patterns

### Pattern 1: OpenAI-Compatible (Easiest)

**Use when:** Your LLM provides OpenAI-compatible endpoints

```javascript
// Inherit and change URL
export class YourProvider extends OpenAIProvider {
  constructor() {
    super({ baseURL: "your-url" });
  }
}
```

**Works with:** Ollama, LM Studio, Together AI, many others

---

### Pattern 2: Adapter (Moderate)

**Use when:** Similar concepts, different field names

```javascript
export class YourProvider extends BaseProvider {
  convertToYourFormat(messages) {
    // Convert messages array to your format
    return {
      /* your format */
    };
  }

  normalizeFromYourFormat(data) {
    // Convert your response to our format
    return { role: "assistant", content: data.text };
  }
}
```

**Works with:** Cohere, AI21, Hugging Face

---

### Pattern 3: Gateway/Proxy (Advanced)

**Use when:** Want isolation or centralized control

```
Frontend → Gateway Server → Your LLM
         OpenAI format   Any format
```

Gateway handles all conversions server-side!

---

## 💡 Real-World Examples

### Local Model (Ollama)

```javascript
const provider = new OllamaProvider();
const response = await provider.chat([{ role: "user", content: "Hello!" }], {
  model: "llama3",
});
```

### Cloud API (Together AI)

```javascript
const provider = new TogetherAIProvider({
  apiKey: "your-key",
});
const response = await provider.chat([{ role: "user", content: "Hello!" }], {
  model: "mistralai/Mixtral-8x7B-Instruct-v0.1",
});
```

### Custom API

```javascript
const provider = new YourCustomProvider({
  apiKey: "your-key",
  baseURL: "https://your-company.com/api",
});
const response = await provider.chat([{ role: "user", content: "Hello!" }]);
```

---

## 🔧 Function Calling Support

### Can Custom LLMs Support Function Calling?

**Yes, if they have tool support!**

```javascript
export class YourProvider extends BaseProvider {
  constructor() {
    super({
      capabilities: { tools: true }, // Enable!
    });
  }

  async chat(messages, options = {}) {
    // If your LLM has tool/function support:
    if (options.tools) {
      // Convert our tool format to yours
      const yourTools = this.convertTools(options.tools);

      // Call your API with tools
      const response = await this.callWithTools(messages, yourTools);

      // Convert tool_calls back to our format
      return {
        role: "assistant",
        content: response.content,
        tool_calls: this.extractToolCalls(response),
      };
    }

    // Regular chat without tools
    return this.regularChat(messages);
  }
}
```

**Even if no native support:** You can parse tool calls from text!

---

## 📖 Full Documentation

For complete details, see:

**[CUSTOM_LLM_INTEGRATION.md](./CUSTOM_LLM_INTEGRATION.md)**

Includes:

- ✅ 5+ complete working examples
- ✅ Step-by-step guides
- ✅ All integration patterns
- ✅ Streaming support
- ✅ Function calling
- ✅ Error handling
- ✅ Best practices
- ✅ Troubleshooting

---

## ✅ Summary

### Yes, You Can!

- ✅ **Any API format** - REST, GraphQL, WebSocket, etc.
- ✅ **Any location** - Local, cloud, self-hosted
- ✅ **Any protocol** - HTTP, gRPC, custom
- ✅ **Easy integration** - 5 minutes to 2 hours

### Key Point

> **The system uses an adapter pattern. As long as you can convert your LLM's format to our standard format, it will work!**

### Need Help?

1. Check [CUSTOM_LLM_INTEGRATION.md](./CUSTOM_LLM_INTEGRATION.md)
2. Look at existing providers (OpenAI, Claude)
3. Copy the closest example
4. Customize for your API

---

**Start integrating your LLM today! 🚀**
