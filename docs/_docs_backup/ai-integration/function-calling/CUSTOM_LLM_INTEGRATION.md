# Custom LLM Integration Guide

## 📋 Overview

**Question**: Can I use LLMs with interfaces that are NOT compatible with OpenAI?

**Answer**: ✅ **YES, ABSOLUTELY!**

This guide shows you how to integrate ANY LLM into the system, regardless of its API format. We'll cover:

- Local models (Ollama, LM Studio)
- Cloud APIs (Cohere, AI21, Together AI)
- Custom/proprietary APIs
- Self-hosted models
- Even APIs with completely different formats!

---

## 🎯 Table of Contents

- [Can I Use Non-OpenAI LLMs?](#can-i-use-non-openai-llms)
- [Architecture Overview](#architecture-overview)
- [Integration Approaches](#integration-approaches)
- [Method 1: Direct Integration](#method-1-direct-integration)
- [Method 2: Adapter Pattern](#method-2-adapter-pattern)
- [Method 3: Proxy/Gateway](#method-3-proxy-gateway)
- [Complete Examples](#complete-examples)
- [Function Calling Support](#function-calling-support)
- [Streaming Support](#streaming-support)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)
- [Real-World Examples](#real-world-examples)

---

## Can I Use Non-OpenAI LLMs?

### Short Answer: YES! ✅

Our system is designed to work with **ANY** LLM through the `BaseProvider` abstraction layer.

### Supported Integration Types

| Integration Type      | Difficulty | Function Calling | Streaming | Example           |
| --------------------- | ---------- | ---------------- | --------- | ----------------- |
| **OpenAI-Compatible** | 🟢 Easy    | ✅ Yes           | ✅ Yes    | Ollama, LM Studio |
| **Similar Format**    | 🟡 Medium  | ⚠️ Adapt         | ⚠️ Adapt  | Cohere, AI21      |
| **Different Format**  | 🔴 Hard    | ❌ No\*          | ❌ No\*   | Custom APIs       |
| **Local Models**      | 🟢 Easy    | ⚠️ Depends       | ✅ Yes    | Ollama, vLLM      |

\*Can be added with custom implementation

---

## Architecture Overview

### How Our System Works

```
┌─────────────────────────────────────────────────────────────┐
│                      Your LLM                                │
│  (Any format, any protocol, any location)                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ Your API Format
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Your Provider Class                        │
│  (Implements BaseProvider interface)                         │
│                                                               │
│  - convertToYourFormat()    ← Convert our format to yours   │
│  - callYourAPI()            ← Make API calls                 │
│  - parseYourResponse()      ← Parse your API response       │
│  - normalizeToOurFormat()   ← Convert back to our format    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ Standard Format
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    AIPanel & App                             │
│  (Works with ANY provider through standard interface)       │
└─────────────────────────────────────────────────────────────┘
```

### Key Concept: Adapter Pattern

```javascript
Your API Format  →  Adapter  →  Standard Format  →  App
     ↑                             ↓
     └──────── Adapter ← ─────────┘
```

The adapter handles ALL format conversions, so the rest of your app doesn't need to know about your specific LLM's format!

---

## Integration Approaches

### Approach 1: OpenAI-Compatible APIs ✅ EASIEST

**When to use:**

- Your LLM provides OpenAI-compatible endpoints
- Examples: Ollama, LM Studio, Together AI (compatibility mode)

**Difficulty**: 🟢 Very Easy  
**Implementation Time**: 5-10 minutes

**How it works:**

```javascript
// Just change the baseURL!
export class OllamaProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "ollama",
      name: "Ollama",
      baseURL: "http://localhost:11434/v1", // ← Different URL
      // Everything else same as OpenAI!
    });
  }
}
```

---

### Approach 2: Similar but Different Format ⚠️ MODERATE

**When to use:**

- Your LLM has similar concepts but different field names
- Examples: Cohere, AI21, some cloud providers

**Difficulty**: 🟡 Moderate  
**Implementation Time**: 30-60 minutes

**How it works:**

```javascript
export class CohereProvider extends BaseProvider {
  async chat(messages, options = {}) {
    // Convert our format to Cohere format
    const cohereRequest = this.convertToCohereFormat(messages);

    // Call Cohere API
    const response = await fetch(this.baseURL, {
      method: "POST",
      body: JSON.stringify(cohereRequest),
    });

    // Convert Cohere response back to our format
    const data = await response.json();
    return this.normalizeFromCohere(data);
  }
}
```

---

### Approach 3: Completely Different Format 🔴 ADVANCED

**When to use:**

- Your LLM has a unique API structure
- Custom/proprietary protocols
- Legacy systems

**Difficulty**: 🔴 Advanced  
**Implementation Time**: 2-4 hours

**How it works:**

```javascript
export class CustomProvider extends BaseProvider {
  async chat(messages, options = {}) {
    // Your custom logic here
    // Can be ANYTHING - REST, GraphQL, gRPC, WebSocket, etc.

    // Just return our standard format at the end:
    return {
      role: "assistant",
      content: "...",
      metadata: { ... }
    };
  }
}
```

---

## Method 1: Direct Integration

### Step-by-Step Guide

#### Step 1: Create Provider File

**Location**: `apps/pcm-webapp/public/js/services/ai/YourProvider.js`

```javascript
/**
 * Your LLM Provider
 * Description of what this LLM is
 */
import { BaseProvider } from "./BaseProvider.js";

export class YourProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "your-provider", // Unique ID
      name: "Your LLM Name", // Display name
      apiKey: config.apiKey || "", // API key (if needed)
      baseURL: config.baseURL || "https://your-api.com/v1",

      // API Key configuration
      apiKeyConfig: {
        required: true, // Does it need API key?
        storageKey: "your-provider-api-key",
        label: "Your LLM API Key",
        placeholder: "Enter your API key",
        hint: "Get your API key from https://...",
      },

      // Capabilities
      capabilities: {
        chat: true, // Supports chat?
        generate: false, // Supports completion?
        stream: true, // Supports streaming?
        thinking: false, // Supports thinking mode?
        vision: false, // Supports images?
        tools: false, // Supports function calling?
      },

      // Settings
      settings: {
        maxTokens: {
          supported: true,
          min: 1,
          max: 4096,
          default: 2048,
          step: 256,
        },
        temperature: {
          supported: true,
          min: 0,
          max: 2,
          default: 0.7,
          step: 0.1,
        },
      },

      ...config,
    });

    this.defaultModel = config.defaultModel || "your-default-model";
    this.timeout = config.timeout || 60000;

    this.loadApiKey();
  }

  /**
   * Health check - verify provider is accessible
   */
  async healthCheck() {
    try {
      // Ping your API or check availability
      const response = await fetch(`${this.baseURL}/health`, {
        method: "GET",
        headers: this.getHeaders(),
        signal: AbortSignal.timeout(5000),
      });

      return response.ok;
    } catch (error) {
      console.error(`[${this.name}] Health check error:`, error.message);
      return false;
    }
  }

  /**
   * Get available models
   */
  async getModels() {
    try {
      const response = await fetch(`${this.baseURL}/models`, {
        method: "GET",
        headers: this.getHeaders(),
        signal: AbortSignal.timeout(5000),
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch models: ${response.statusText}`);
      }

      const data = await response.json();

      // Parse your API's model list format
      return this.parseModels(data);
    } catch (error) {
      console.error(`[${this.name}] Failed to fetch models:`, error);
      return [];
    }
  }

  /**
   * Chat completion
   */
  async chat(messages, options = {}) {
    try {
      // 1. Convert our format to your API format
      const requestBody = this.convertToYourFormat(messages, options);

      // 2. Call your API
      const response = await fetch(`${this.baseURL}/chat`, {
        method: "POST",
        headers: this.getHeaders(),
        body: JSON.stringify(requestBody),
        signal: AbortSignal.timeout(this.timeout),
      });

      if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(
          error.message || `HTTP ${response.status}: ${response.statusText}`,
        );
      }

      const data = await response.json();

      // 3. Convert your API response to our format
      return this.normalizeResponse(data);
    } catch (error) {
      return this.handleError(error);
    }
  }

  /**
   * Stream chat completion
   */
  async *streamChat(messages, options = {}) {
    try {
      // 1. Convert to your format
      const requestBody = this.convertToYourFormat(messages, options);
      requestBody.stream = true; // Enable streaming

      // 2. Call your API
      const response = await fetch(`${this.baseURL}/chat`, {
        method: "POST",
        headers: this.getHeaders(),
        body: JSON.stringify(requestBody),
        signal: AbortSignal.timeout(this.timeout),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      // 3. Parse streaming response
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split("\n");
        buffer = lines.pop() || "";

        for (const line of lines) {
          // Parse your streaming format
          const chunk = this.parseStreamChunk(line);
          if (chunk) {
            yield chunk;
          }
        }
      }
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // ============================================
  // Helper Methods - Customize for Your API
  // ============================================

  /**
   * Get headers for API requests
   */
  getHeaders() {
    return {
      "Content-Type": "application/json",
      Authorization: `Bearer ${this.apiKey}`,
      // Add any other headers your API needs
    };
  }

  /**
   * Convert our message format to your API format
   */
  convertToYourFormat(messages, options) {
    // Example: Your API might use different field names
    return {
      model: options.model || this.defaultModel,
      prompt: this.messagesToPrompt(messages), // If your API uses prompt instead of messages
      max_tokens: options.maxTokens || 2048,
      temperature: options.temperature || 0.7,
      // ... your API's specific fields
    };
  }

  /**
   * Convert messages array to single prompt (if needed)
   */
  messagesToPrompt(messages) {
    // Some APIs don't support messages array, need single prompt
    return messages
      .map((msg) => {
        const prefix = msg.role === "user" ? "Human:" : "Assistant:";
        return `${prefix} ${msg.content}`;
      })
      .join("\n\n");
  }

  /**
   * Convert your API response to our standard format
   */
  normalizeResponse(data) {
    // Map your API's response fields to our format
    return {
      role: "assistant",
      content: data.text || data.response || data.output || "", // Try common field names
      metadata: {
        model: data.model || this.defaultModel,
        provider: this.id,
        usage: {
          prompt_tokens: data.usage?.prompt_tokens || 0,
          completion_tokens: data.usage?.completion_tokens || 0,
          total_tokens: data.usage?.total_tokens || 0,
        },
        finishReason: data.finish_reason || "stop",
      },
    };
  }

  /**
   * Parse models from your API response
   */
  parseModels(data) {
    // Adapt to your API's model list format
    if (Array.isArray(data.models)) {
      return data.models.map((model) => ({
        id: model.id || model.name,
        name: model.name || model.id,
        created: model.created || Date.now(),
      }));
    }

    return [];
  }

  /**
   * Parse streaming chunk
   */
  parseStreamChunk(line) {
    // Adapt to your API's streaming format

    // Example: SSE format (like OpenAI)
    if (line.startsWith("data: ")) {
      const data = line.slice(6);
      if (data === "[DONE]") {
        return { delta: "", done: true };
      }

      try {
        const parsed = JSON.parse(data);
        const delta = parsed.choices?.[0]?.delta?.content || "";
        return { delta, done: false };
      } catch (e) {
        return null;
      }
    }

    // Example: JSONL format (one JSON per line)
    try {
      const parsed = JSON.parse(line);
      const delta = parsed.text || parsed.delta || "";
      return { delta, done: parsed.done || false };
    } catch (e) {
      return null;
    }
  }

  /**
   * Handle provider-specific errors
   */
  handleError(error) {
    console.error(`[${this.name}] Error:`, error);

    // Customize error messages for your API
    if (error.message?.includes("timeout")) {
      throw new Error("Request timeout. Please try again.");
    }

    if (error.message?.includes("401")) {
      throw new Error("Invalid API key. Please check your configuration.");
    }

    if (error.message?.includes("429")) {
      throw new Error("Rate limit exceeded. Please wait and try again.");
    }

    throw new Error(`${this.name} error: ${error.message}`);
  }
}
```

#### Step 2: Register Provider

**File**: `apps/pcm-webapp/public/js/services/ai/ProviderRegistry.js`

```javascript
import { YourProvider } from "./YourProvider.js";

class ProviderRegistry {
  constructor() {
    this.providers = new Map();
    this.activeProviderId = null;

    // Register all providers
    this.register(new OpenAIProvider());
    this.register(new ClaudeProvider());
    this.register(new YourProvider()); // ← Add here!
  }

  // ... rest of the code
}
```

#### Step 3: Test

```javascript
// In browser console:

// 1. Get your provider
const provider = providerRegistry.get("your-provider");

// 2. Test health check
await provider.healthCheck(); // Should return true

// 3. Test models
const models = await provider.getModels();
console.log(models);

// 4. Test chat
const response = await provider.chat([{ role: "user", content: "Hello!" }]);
console.log(response);
```

---

## Method 2: Adapter Pattern

For more complex integrations where you need to significantly transform data.

### Example: Cohere API Integration

```javascript
export class CohereProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "cohere",
      name: "Cohere",
      baseURL: "https://api.cohere.ai/v1",
      capabilities: {
        chat: true,
        stream: true,
        tools: false, // Cohere has different tool format
      },
      ...config,
    });
  }

  async chat(messages, options = {}) {
    // Cohere uses "chat_history" and "message" instead of "messages"
    const { chatHistory, message } = this.convertToCohereMessages(messages);

    const response = await fetch(`${this.baseURL}/chat`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${this.apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: options.model || "command",
        message: message, // Last user message
        chat_history: chatHistory, // Previous messages
        temperature: options.temperature || 0.7,
        max_tokens: options.maxTokens || 2048,
      }),
    });

    const data = await response.json();

    // Cohere returns "text" field
    return {
      role: "assistant",
      content: data.text,
      metadata: {
        model: data.generation_id,
        provider: this.id,
        usage: {
          prompt_tokens: data.meta?.tokens?.input_tokens || 0,
          completion_tokens: data.meta?.tokens?.output_tokens || 0,
          total_tokens:
            (data.meta?.tokens?.input_tokens || 0) +
            (data.meta?.tokens?.output_tokens || 0),
        },
      },
    };
  }

  convertToCohereMessages(messages) {
    // Cohere format: separate chat_history and final message
    const chatHistory = [];
    let message = "";

    for (let i = 0; i < messages.length; i++) {
      const msg = messages[i];

      if (i === messages.length - 1) {
        // Last message
        message = msg.content;
      } else {
        // Previous messages
        chatHistory.push({
          role: msg.role === "assistant" ? "CHATBOT" : "USER",
          message: msg.content,
        });
      }
    }

    return { chatHistory, message };
  }
}
```

---

## Method 3: Proxy/Gateway

For cases where you want to keep LLM details completely separate.

### Architecture

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   AIPanel    │ ──────► │ Your Gateway │ ──────► │  Your LLM    │
│              │         │   (Proxy)    │         │              │
└──────────────┘         └──────────────┘         └──────────────┘
   OpenAI format         Translates format        Any format
```

### Gateway Provider

```javascript
export class GatewayProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "gateway",
      name: "Custom Gateway",
      baseURL: config.gatewayURL || "http://localhost:8080/api",
      capabilities: {
        chat: true,
        stream: true,
        tools: true, // Your gateway handles tool conversion
      },
      ...config,
    });
  }

  async chat(messages, options = {}) {
    // Send to your gateway in OpenAI format
    const response = await fetch(`${this.baseURL}/chat`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${this.apiKey}`,
      },
      body: JSON.stringify({
        messages: messages,
        options: options,
        // Your gateway will:
        // 1. Receive this OpenAI-format request
        // 2. Convert to your LLM's format
        // 3. Call your LLM
        // 4. Convert response back to OpenAI format
        // 5. Return to us
      }),
    });

    const data = await response.json();

    // Gateway returns OpenAI-compatible format
    return {
      role: "assistant",
      content: data.choices[0].message.content,
      metadata: data.metadata,
    };
  }
}
```

### Gateway Server (Example in Node.js)

```javascript
// gateway-server.js
const express = require("express");
const app = express();

app.post("/api/chat", async (req, res) => {
  const { messages, options } = req.body;

  // Convert OpenAI format to your LLM format
  const yourFormat = convertToYourLLMFormat(messages, options);

  // Call your LLM
  const llmResponse = await fetch("https://your-llm-api.com/generate", {
    method: "POST",
    body: JSON.stringify(yourFormat),
  });

  const llmData = await llmResponse.json();

  // Convert back to OpenAI format
  const openaiFormat = {
    choices: [
      {
        message: {
          role: "assistant",
          content: llmData.output, // Your LLM's response field
        },
      },
    ],
    metadata: {
      model: llmData.model,
      usage: llmData.token_usage,
    },
  };

  res.json(openaiFormat);
});

app.listen(8080);
```

**Benefits:**

- ✅ Isolate LLM details from frontend
- ✅ Easy to switch LLMs without changing frontend
- ✅ Can add custom logic (caching, logging, etc.)
- ✅ Centralized API key management

---

## Complete Examples

### Example 1: Ollama (OpenAI-Compatible)

**Difficulty**: 🟢 Very Easy (5 minutes)

```javascript
/**
 * Ollama Provider
 * Local LLM with OpenAI-compatible API
 */
import { BaseProvider } from "./BaseProvider.js";

export class OllamaProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "ollama",
      name: "Ollama (Local)",
      baseURL: config.baseURL || "http://localhost:11434/v1",
      apiKeyConfig: {
        required: false,  // Ollama doesn't need API key!
      },
      capabilities: {
        chat: true,
        stream: true,
        tools: false,  // Most local models don't support tools yet
      },
      ...config,
    });
  }

  // Everything else is the same as OpenAI!
  // Just inherit from OpenAIProvider if you want:
}

// Or even simpler:
export class OllamaProvider extends OpenAIProvider {
  constructor(config = {}) {
    super({
      ...config,
      id: "ollama",
      name: "Ollama (Local)",
      baseURL: "http://localhost:11434/v1",
      apiKeyConfig: { required: false },
    });
  }
}
```

**Usage:**

```javascript
// Register
providerRegistry.register(new OllamaProvider());

// Use
const provider = providerRegistry.get("ollama");
const response = await provider.chat([{ role: "user", content: "Hello!" }], {
  model: "llama3",
});
```

---

### Example 2: Hugging Face Inference API

**Difficulty**: 🟡 Moderate (30 minutes)

```javascript
/**
 * Hugging Face Provider
 * Cloud-hosted models with different API format
 */
import { BaseProvider } from "./BaseProvider.js";

export class HuggingFaceProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "huggingface",
      name: "Hugging Face",
      baseURL: "https://api-inference.huggingface.co/models",
      capabilities: {
        chat: true,
        stream: false, // HF API doesn't support streaming
        tools: false,
      },
      ...config,
    });

    this.defaultModel = config.defaultModel || "meta-llama/Llama-2-7b-chat-hf";
  }

  async chat(messages, options = {}) {
    const model = options.model || this.defaultModel;

    // HF uses "inputs" field with formatted prompt
    const prompt = this.formatPrompt(messages);

    const response = await fetch(`${this.baseURL}/${model}`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${this.apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        inputs: prompt,
        parameters: {
          max_new_tokens: options.maxTokens || 512,
          temperature: options.temperature || 0.7,
          top_p: 0.95,
          return_full_text: false,
        },
      }),
    });

    const data = await response.json();

    // HF returns array with "generated_text"
    const generatedText = data[0]?.generated_text || "";

    return {
      role: "assistant",
      content: generatedText,
      metadata: {
        model: model,
        provider: this.id,
        usage: {
          prompt_tokens: 0, // HF doesn't provide token counts
          completion_tokens: 0,
          total_tokens: 0,
        },
      },
    };
  }

  formatPrompt(messages) {
    // Format for Llama-2 chat model
    let prompt = "<s>";

    for (const msg of messages) {
      if (msg.role === "user") {
        prompt += `[INST] ${msg.content} [/INST]`;
      } else {
        prompt += ` ${msg.content} </s><s>`;
      }
    }

    return prompt;
  }

  async getModels() {
    // HF has thousands of models, return curated list
    return [
      { id: "meta-llama/Llama-2-7b-chat-hf", name: "Llama 2 7B Chat" },
      { id: "meta-llama/Llama-2-13b-chat-hf", name: "Llama 2 13B Chat" },
      { id: "mistralai/Mistral-7B-Instruct-v0.2", name: "Mistral 7B Instruct" },
      { id: "google/flan-t5-xxl", name: "FLAN-T5 XXL" },
    ];
  }
}
```

---

### Example 3: Custom REST API

**Difficulty**: 🟡 Moderate (45 minutes)

```javascript
/**
 * Custom REST API Provider
 * Your proprietary API with unique format
 */
import { BaseProvider } from "./BaseProvider.js";

export class CustomRESTProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "custom-rest",
      name: "Custom REST API",
      baseURL: config.baseURL || "https://api.yourcompany.com/v2",
      capabilities: {
        chat: true,
        stream: false,
        tools: false,
      },
      ...config,
    });
  }

  async chat(messages, options = {}) {
    // Your API format might be completely different
    const response = await fetch(`${this.baseURL}/generate`, {
      method: "POST",
      headers: {
        "X-API-Key": this.apiKey, // Different auth header
        "Content-Type": "application/json",
        "X-User-ID": "pcm-webapp", // Custom headers
      },
      body: JSON.stringify({
        // Your custom request format
        conversation: messages.map((msg) => ({
          speaker: msg.role === "user" ? "human" : "ai",
          text: msg.content,
          timestamp: Date.now(),
        })),
        settings: {
          model_name: options.model || "your-default-model",
          max_length: options.maxTokens || 2048,
          creativity: options.temperature || 0.7,
        },
        metadata: {
          app: "pcm-webapp",
          version: "1.1.0",
        },
      }),
    });

    const data = await response.json();

    // Your API response format
    return {
      role: "assistant",
      content: data.result.ai_response, // Your response field
      metadata: {
        model: data.result.model_used,
        provider: this.id,
        usage: {
          prompt_tokens: data.metrics.input_tokens,
          completion_tokens: data.metrics.output_tokens,
          total_tokens: data.metrics.total_tokens,
        },
        customData: data.debug_info, // Your custom fields
      },
    };
  }
}
```

---

### Example 4: GraphQL API

**Difficulty**: 🔴 Advanced (60 minutes)

```javascript
/**
 * GraphQL API Provider
 * For LLMs exposed via GraphQL
 */
import { BaseProvider } from "./BaseProvider.js";

export class GraphQLProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "graphql-llm",
      name: "GraphQL LLM",
      baseURL: config.baseURL || "https://graphql.yourapi.com",
      capabilities: {
        chat: true,
        stream: false, // GraphQL typically doesn't support streaming
        tools: false,
      },
      ...config,
    });
  }

  async chat(messages, options = {}) {
    // GraphQL mutation
    const mutation = `
      mutation GenerateResponse($input: ChatInput!) {
        generateResponse(input: $input) {
          text
          model
          usage {
            promptTokens
            completionTokens
            totalTokens
          }
        }
      }
    `;

    const variables = {
      input: {
        messages: messages,
        settings: {
          model: options.model || "default",
          maxTokens: options.maxTokens || 2048,
          temperature: options.temperature || 0.7,
        },
      },
    };

    const response = await fetch(this.baseURL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${this.apiKey}`,
      },
      body: JSON.stringify({
        query: mutation,
        variables: variables,
      }),
    });

    const data = await response.json();
    const result = data.data.generateResponse;

    return {
      role: "assistant",
      content: result.text,
      metadata: {
        model: result.model,
        provider: this.id,
        usage: {
          prompt_tokens: result.usage.promptTokens,
          completion_tokens: result.usage.completionTokens,
          total_tokens: result.usage.totalTokens,
        },
      },
    };
  }
}
```

---

### Example 5: WebSocket API (Real-time)

**Difficulty**: 🔴 Advanced (90 minutes)

```javascript
/**
 * WebSocket Provider
 * For LLMs that use WebSocket for real-time streaming
 */
import { BaseProvider } from "./BaseProvider.js";

export class WebSocketProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "websocket-llm",
      name: "WebSocket LLM",
      baseURL: config.baseURL || "wss://ws.yourapi.com",
      capabilities: {
        chat: true,
        stream: true, // WebSocket is perfect for streaming!
        tools: false,
      },
      ...config,
    });

    this.ws = null;
  }

  async connect() {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      return;
    }

    return new Promise((resolve, reject) => {
      this.ws = new WebSocket(this.baseURL);

      this.ws.onopen = () => {
        // Authenticate
        this.ws.send(
          JSON.stringify({
            type: "auth",
            token: this.apiKey,
          }),
        );
        resolve();
      };

      this.ws.onerror = (error) => reject(error);
    });
  }

  async chat(messages, options = {}) {
    await this.connect();

    return new Promise((resolve, reject) => {
      let fullResponse = "";

      const messageHandler = (event) => {
        const data = JSON.parse(event.data);

        if (data.type === "chunk") {
          fullResponse += data.text;
        } else if (data.type === "done") {
          this.ws.removeEventListener("message", messageHandler);

          resolve({
            role: "assistant",
            content: fullResponse,
            metadata: {
              model: data.model,
              provider: this.id,
              usage: data.usage,
            },
          });
        } else if (data.type === "error") {
          this.ws.removeEventListener("message", messageHandler);
          reject(new Error(data.error));
        }
      };

      this.ws.addEventListener("message", messageHandler);

      // Send request
      this.ws.send(
        JSON.stringify({
          type: "generate",
          messages: messages,
          options: options,
        }),
      );
    });
  }

  async *streamChat(messages, options = {}) {
    await this.connect();

    const stream = new ReadableStream({
      start: (controller) => {
        const messageHandler = (event) => {
          const data = JSON.parse(event.data);

          if (data.type === "chunk") {
            controller.enqueue({
              delta: data.text,
              done: false,
            });
          } else if (data.type === "done") {
            controller.enqueue({
              delta: "",
              done: true,
              metadata: {
                model: data.model,
                provider: this.id,
                usage: data.usage,
              },
            });
            controller.close();
            this.ws.removeEventListener("message", messageHandler);
          }
        };

        this.ws.addEventListener("message", messageHandler);

        this.ws.send(
          JSON.stringify({
            type: "generate",
            messages: messages,
            options: options,
          }),
        );
      },
    });

    const reader = stream.getReader();

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      yield value;
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}
```

---

## Function Calling Support

### Can Non-OpenAI LLMs Support Function Calling?

**Answer**: It depends!

| LLM Type                    | Function Calling | How to Add                 |
| --------------------------- | ---------------- | -------------------------- |
| **OpenAI-compatible**       | ✅ Yes           | Already works!             |
| **Has native tool support** | ✅ Yes           | Adapt format (like Claude) |
| **No native support**       | ⚠️ Manual        | Parse from text response   |
| **Local models**            | ⚠️ Manual        | Fine-tune or parse text    |

### Approach 1: Native Tool Support (Like Claude)

If your LLM has its own tool/function calling format:

```javascript
export class YourProvider extends BaseProvider {
  constructor() {
    super({
      capabilities: {
        tools: true, // Enable!
      },
    });
  }

  async chat(messages, options = {}) {
    // Convert our tool format to yours
    const yourTools = this.convertTools(options.tools);

    // Call your API with tools
    const response = await this.callAPI(messages, yourTools);

    // Convert your tool_calls back to our format
    const toolCalls = this.extractToolCalls(response);

    return {
      role: "assistant",
      content: response.content,
      tool_calls: toolCalls, // Our standard format
    };
  }

  convertTools(tools) {
    // Convert from OpenAI format to your format
    return tools.map((tool) => ({
      // Your format here
      name: tool.function.name,
      description: tool.function.description,
      parameters: tool.function.parameters,
    }));
  }

  extractToolCalls(response) {
    // Convert from your format to OpenAI format
    return response.tool_uses.map((use) => ({
      id: use.id,
      type: "function",
      function: {
        name: use.name,
        arguments: JSON.stringify(use.parameters),
      },
    }));
  }
}
```

### Approach 2: Parse from Text (No Native Support)

If your LLM doesn't support tools, you can parse them from text:

```javascript
export class TextBasedToolProvider extends BaseProvider {
  constructor() {
    super({
      capabilities: {
        tools: true, // We'll handle it manually
      },
    });
  }

  async chat(messages, options = {}) {
    // Add tool descriptions to system prompt
    const systemPrompt = this.createSystemPromptWithTools(options.tools);
    const enhancedMessages = [
      { role: "system", content: systemPrompt },
      ...messages,
    ];

    // Get response
    const response = await this.callAPI(enhancedMessages);

    // Parse tool calls from text
    const toolCalls = this.parseToolCallsFromText(response.content);

    if (toolCalls.length > 0) {
      return {
        role: "assistant",
        content: "",
        tool_calls: toolCalls,
      };
    }

    return response;
  }

  createSystemPromptWithTools(tools) {
    let prompt = "You have access to the following tools:\n\n";

    for (const tool of tools) {
      prompt += `- ${tool.function.name}: ${tool.function.description}\n`;
      prompt += `  Parameters: ${JSON.stringify(tool.function.parameters)}\n\n`;
    }

    prompt += `
To use a tool, respond with:
TOOL_CALL: function_name
ARGUMENTS: {"param1": "value1", "param2": "value2"}

If you don't need a tool, just respond normally.
`;

    return prompt;
  }

  parseToolCallsFromText(text) {
    const toolCalls = [];
    const regex = /TOOL_CALL:\s*(\w+)\s*ARGUMENTS:\s*({[^}]+})/g;

    let match;
    while ((match = regex.exec(text)) !== null) {
      const [, name, args] = match;

      toolCalls.push({
        id: `call_${Date.now()}_${Math.random()}`,
        type: "function",
        function: {
          name: name,
          arguments: args,
        },
      });
    }

    return toolCalls;
  }
}
```

---

## Streaming Support

### Handling Different Streaming Formats

#### Format 1: Server-Sent Events (SSE) - Like OpenAI

```javascript
async *streamChat(messages, options = {}) {
  const response = await fetch(this.baseURL, {
    method: "POST",
    headers: this.getHeaders(),
    body: JSON.stringify(this.convertRequest(messages, options)),
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split("\n");
    buffer = lines.pop() || "";

    for (const line of lines) {
      if (line.startsWith("data: ")) {
        const data = line.slice(6);
        if (data === "[DONE]") {
          yield { delta: "", done: true };
          return;
        }

        try {
          const parsed = JSON.parse(data);
          const delta = parsed.choices[0]?.delta?.content || "";
          if (delta) yield { delta, done: false };
        } catch (e) {
          // Skip invalid JSON
        }
      }
    }
  }
}
```

#### Format 2: JSONL (JSON Lines)

```javascript
async *streamChat(messages, options = {}) {
  const response = await fetch(this.baseURL, ...);
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split("\n");
    buffer = lines.pop() || "";

    for (const line of lines) {
      if (!line.trim()) continue;

      try {
        const data = JSON.parse(line);
        yield {
          delta: data.text || data.delta || "",
          done: data.done || false,
        };
      } catch (e) {
        console.warn("Failed to parse JSONL:", line);
      }
    }
  }
}
```

#### Format 3: Custom Delimiters

```javascript
async *streamChat(messages, options = {}) {
  const response = await fetch(this.baseURL, ...);
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  const DELIMITER = "|||";  // Your custom delimiter

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });

    while (buffer.includes(DELIMITER)) {
      const index = buffer.indexOf(DELIMITER);
      const chunk = buffer.slice(0, index);
      buffer = buffer.slice(index + DELIMITER.length);

      if (chunk.trim()) {
        yield {
          delta: chunk,
          done: false,
        };
      }
    }
  }

  // Last chunk
  if (buffer.trim()) {
    yield { delta: buffer, done: true };
  }
}
```

---

## Best Practices

### 1. Error Handling

✅ **Always implement robust error handling:**

```javascript
async chat(messages, options = {}) {
  try {
    const response = await fetch(this.baseURL, ...);

    if (!response.ok) {
      // Parse error details
      const error = await response.json().catch(() => ({}));

      // Map to user-friendly messages
      if (response.status === 401) {
        throw new Error("Invalid API key");
      } else if (response.status === 429) {
        throw new Error("Rate limit exceeded");
      } else if (response.status >= 500) {
        throw new Error("Provider service unavailable");
      }

      throw new Error(error.message || "Request failed");
    }

    return this.normalizeResponse(await response.json());
  } catch (error) {
    // Log for debugging
    console.error(`[${this.name}] Error:`, error);

    // Re-throw with context
    if (error.name === "AbortError") {
      throw new Error("Request timeout");
    }

    throw error;
  }
}
```

### 2. Timeout Management

✅ **Always set timeouts:**

```javascript
const controller = new AbortController();
const timeout = setTimeout(() => controller.abort(), 60000);

try {
  const response = await fetch(this.baseURL, {
    signal: controller.signal,
    ...
  });

  clearTimeout(timeout);
  // Process response
} catch (error) {
  clearTimeout(timeout);
  throw error;
}
```

### 3. Response Validation

✅ **Validate API responses:**

```javascript
normalizeResponse(data) {
  // Check required fields
  if (!data || typeof data !== 'object') {
    throw new Error("Invalid response format");
  }

  // Safely access nested fields
  const content = data.result?.text ||
                  data.output?.content ||
                  data.response ||
                  "";

  if (typeof content !== 'string') {
    throw new Error("Response content is not a string");
  }

  return {
    role: "assistant",
    content: content,
    metadata: {
      model: data.model || "unknown",
      provider: this.id,
      usage: this.parseUsage(data),
    },
  };
}

parseUsage(data) {
  return {
    prompt_tokens: Number(data.usage?.input || 0),
    completion_tokens: Number(data.usage?.output || 0),
    total_tokens: Number(data.usage?.total || 0),
  };
}
```

### 4. Logging

✅ **Add comprehensive logging:**

```javascript
async chat(messages, options = {}) {
  console.log(`[${this.name}] Chat request:`, {
    messageCount: messages.length,
    model: options.model,
    maxTokens: options.maxTokens,
  });

  const startTime = Date.now();

  try {
    const response = await this.callAPI(messages, options);

    const duration = Date.now() - startTime;
    console.log(`[${this.name}] Chat response received:`, {
      duration: `${duration}ms`,
      contentLength: response.content.length,
      tokens: response.metadata.usage.total_tokens,
    });

    return response;
  } catch (error) {
    console.error(`[${this.name}] Chat error:`, {
      duration: `${Date.now() - startTime}ms`,
      error: error.message,
    });
    throw error;
  }
}
```

### 5. Configuration

✅ **Make everything configurable:**

```javascript
constructor(config = {}) {
  super({
    id: "your-provider",
    name: config.name || "Your Provider",
    baseURL: config.baseURL || process.env.YOUR_API_URL || "https://default.api.com",
    timeout: config.timeout || 60000,
    retries: config.retries || 3,
    retryDelay: config.retryDelay || 1000,
    ...config,
  });

  // Load from config or environment
  this.apiKey = config.apiKey ||
                process.env.YOUR_API_KEY ||
                localStorage.getItem(`${this.id}-api-key`);
}
```

---

## Troubleshooting

### Issue 1: API Returns Different Format

**Problem:**

```javascript
// Expected: { content: "..." }
// Got: { result: { text: "..." } }
```

**Solution:**

```javascript
normalizeResponse(data) {
  // Try multiple possible fields
  const content =
    data.content ||
    data.text ||
    data.result?.text ||
    data.output?.content ||
    data.response ||
    "";

  if (!content) {
    console.warn("Could not find content in response:", data);
  }

  return { role: "assistant", content, metadata: {...} };
}
```

### Issue 2: Streaming Doesn't Work

**Problem:** Chunks not being parsed correctly

**Solution:**

```javascript
// Add debugging
async *streamChat(messages, options = {}) {
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  let chunkCount = 0;

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    const decoded = decoder.decode(value, { stream: true });
    console.log(`Chunk ${++chunkCount}:`, decoded);  // Debug

    buffer += decoded;
    // ... parse buffer
  }
}
```

### Issue 3: CORS Errors

**Problem:** Browser blocks requests

**Solution 1:** Use proxy

```javascript
constructor(config = {}) {
  super({
    baseURL: config.useProxy
      ? "http://localhost:8080/proxy"
      : "https://api.yourllm.com",
    ...config,
  });
}
```

**Solution 2:** Add CORS headers (if you control the API)

```javascript
// Server-side
app.use((req, res, next) => {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
  next();
});
```

### Issue 4: Rate Limiting

**Problem:** Too many requests

**Solution:** Add retry logic

```javascript
async chat(messages, options = {}, retryCount = 0) {
  try {
    return await this.callAPI(messages, options);
  } catch (error) {
    if (error.status === 429 && retryCount < this.maxRetries) {
      const delay = this.retryDelay * Math.pow(2, retryCount);
      console.log(`Rate limited, retrying in ${delay}ms...`);

      await new Promise(resolve => setTimeout(resolve, delay));
      return this.chat(messages, options, retryCount + 1);
    }

    throw error;
  }
}
```

---

## Real-World Examples

### Complete Example: Together AI

```javascript
/**
 * Together AI Provider
 * Cloud platform with multiple open-source models
 */
import { BaseProvider } from "./BaseProvider.js";

export class TogetherAIProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "together",
      name: "Together AI",
      baseURL: config.baseURL || "https://api.together.xyz/v1",
      apiKeyConfig: {
        required: true,
        storageKey: "together-api-key",
        label: "Together AI API Key",
        placeholder: "Enter your API key",
        hint: "Get your API key from https://api.together.xyz/settings/api-keys",
      },
      capabilities: {
        chat: true,
        generate: true,
        stream: true,
        tools: false, // Not supported yet
        vision: false,
      },
      settings: {
        maxTokens: {
          supported: true,
          min: 1,
          max: 8192,
          default: 2048,
          step: 256,
        },
        temperature: {
          supported: true,
          min: 0,
          max: 2,
          default: 0.7,
          step: 0.1,
        },
      },
      ...config,
    });

    this.defaultModel =
      config.defaultModel || "mistralai/Mixtral-8x7B-Instruct-v0.1";
    this.timeout = config.timeout || 60000;

    this.loadApiKey();
  }

  async healthCheck() {
    try {
      const response = await fetch(`${this.baseURL}/models`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${this.apiKey}`,
        },
        signal: AbortSignal.timeout(5000),
      });

      return response.ok;
    } catch (error) {
      console.error(`[${this.name}] Health check error:`, error.message);
      return false;
    }
  }

  async chat(messages, options = {}) {
    try {
      const response = await fetch(`${this.baseURL}/chat/completions`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${this.apiKey}`,
        },
        body: JSON.stringify({
          model: options.model || this.defaultModel,
          messages: messages,
          max_tokens: options.maxTokens || 2048,
          temperature: options.temperature || 0.7,
          top_p: 0.7,
          top_k: 50,
          repetition_penalty: 1,
          stream: false,
        }),
        signal: AbortSignal.timeout(this.timeout),
      });

      if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(
          error.error?.message ||
            `HTTP ${response.status}: ${response.statusText}`,
        );
      }

      const data = await response.json();

      return {
        role: "assistant",
        content: data.choices[0].message.content,
        metadata: {
          model: data.model,
          provider: this.id,
          usage: {
            prompt_tokens: data.usage.prompt_tokens,
            completion_tokens: data.usage.completion_tokens,
            total_tokens: data.usage.total_tokens,
          },
          finishReason: data.choices[0].finish_reason,
        },
      };
    } catch (error) {
      return this.handleError(error);
    }
  }

  async *streamChat(messages, options = {}) {
    try {
      const response = await fetch(`${this.baseURL}/chat/completions`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${this.apiKey}`,
        },
        body: JSON.stringify({
          model: options.model || this.defaultModel,
          messages: messages,
          max_tokens: options.maxTokens || 2048,
          temperature: options.temperature || 0.7,
          stream: true,
        }),
        signal: AbortSignal.timeout(this.timeout),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split("\n");
        buffer = lines.pop() || "";

        for (const line of lines) {
          if (line.startsWith("data: ")) {
            const data = line.slice(6);
            if (data === "[DONE]") {
              yield { delta: "", done: true };
              return;
            }

            try {
              const parsed = JSON.parse(data);
              const delta = parsed.choices[0]?.delta?.content || "";
              if (delta) {
                yield { delta, done: false };
              }
            } catch (e) {
              console.warn("Failed to parse SSE data:", e);
            }
          }
        }
      }
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async getModels() {
    try {
      const response = await fetch(`${this.baseURL}/models`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${this.apiKey}`,
        },
        signal: AbortSignal.timeout(5000),
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch models: ${response.statusText}`);
      }

      const data = await response.json();

      // Filter to chat models only
      const chatModels = data
        .filter((model) => model.type === "chat" && !model.id.includes("embed"))
        .map((model) => ({
          id: model.id,
          name: model.display_name || model.id,
          context_length: model.context_length,
        }));

      return chatModels;
    } catch (error) {
      console.error(`[${this.name}] Failed to fetch models:`, error);
      return [];
    }
  }

  handleError(error) {
    console.error(`[${this.name}] Error:`, error);

    if (error.name === "AbortError" || error.message.includes("timeout")) {
      throw new Error("Request timeout. Please try again.");
    }

    if (error.message?.includes("401")) {
      throw new Error("Invalid API key. Please check your configuration.");
    }

    if (error.message?.includes("429")) {
      throw new Error("Rate limit exceeded. Please wait and try again.");
    }

    throw new Error(`Together AI error: ${error.message}`);
  }
}
```

---

## Summary

### ✅ Yes, You Can Use ANY LLM!

**Key Points:**

1. **Interface doesn't matter** - Use adapter pattern
2. **Any protocol works** - REST, GraphQL, WebSocket, etc.
3. **Format conversion** - Your adapter handles it
4. **Standard output** - Always return same format

### Integration Difficulty

| Scenario              | Time      | Difficulty   |
| --------------------- | --------- | ------------ |
| OpenAI-compatible API | 5 min     | 🟢 Very Easy |
| Similar format (REST) | 30 min    | 🟡 Easy      |
| Different format      | 1 hour    | 🟡 Medium    |
| Completely custom     | 2-4 hours | 🔴 Advanced  |

### What You Need to Implement

**Minimum (Required):**

- ✅ `constructor()` - Setup
- ✅ `chat()` - Basic chat
- ✅ `normalizeResponse()` - Format conversion

**Recommended:**

- ✅ `healthCheck()` - Verify availability
- ✅ `getModels()` - List models
- ✅ `streamChat()` - Streaming
- ✅ `handleError()` - Error handling

**Optional (Advanced):**

- ⭐ Function calling support
- ⭐ Vision support
- ⭐ Thinking mode
- ⭐ Custom features

### Next Steps

1. **Choose your integration approach**
2. **Copy appropriate example**
3. **Customize for your API**
4. **Test thoroughly**
5. **Register provider**
6. **Use it!**

---

## Additional Resources

### Official Documentation

- [BaseProvider Reference](../public/js/services/ai/BaseProvider.js)
- [OpenAIProvider Example](../public/js/services/ai/OpenAIProvider.js)
- [ClaudeProvider Example](../public/js/services/ai/ClaudeProvider.js)
- [ProviderRegistry](../public/js/services/ai/ProviderRegistry.js)

### Related Guides

- [AI Function Calling System](./AI_FUNCTION_CALLING_SYSTEM.md)
- [AI Database Query System](./AI_DATABASE_QUERY_SYSTEM.md)
- [Main README](./README.md)

### External References

- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Anthropic Claude API](https://docs.anthropic.com)
- [Ollama API](https://github.com/ollama/ollama/blob/main/docs/api.md)
- [Together AI](https://docs.together.ai)
- [Hugging Face Inference](https://huggingface.co/docs/api-inference)

---

**Last Updated**: November 6, 2025  
**Version**: 1.0.0  
**Author**: PCM Development Team

**Questions? Check the troubleshooting section or consult the existing provider implementations!**
