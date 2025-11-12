# AI Module - Quick Reference

> Tra cứu nhanh các APIs và patterns thường dùng

## 📑 Mục Lục

- [Setup](#setup)
- [Basic Chat](#basic-chat)
- [Function Calling](#function-calling)
- [Streaming](#streaming)
- [Chat History](#chat-history)
- [Provider Management](#provider-management)
- [Common Patterns](#common-patterns)
- [Troubleshooting](#troubleshooting)

---

## Setup

### 1. Include Styles

```html
<link rel="stylesheet" href="public/js/modules/ai/styles/ai-panel.css" />
<link rel="stylesheet" href="public/js/modules/ai/styles/ai-chat-history.css" />
```

### 2. Initialize

```javascript
import aiPanel from "./modules/ai/components/AIPanel.js";

await aiPanel.init();
```

### 3. Configure Provider

```javascript
import providerRegistry from "./modules/ai/services/ProviderRegistry.js";

const provider = providerRegistry.get("openai");
provider.configure({
  apiKey: "sk-...",
  model: "gpt-4-turbo-preview",
});
providerRegistry.setActive("openai");
```

---

## Basic Chat

### Simple Request

```javascript
const provider = providerRegistry.getActive();

const response = await provider.chat([{ role: "user", content: "Hello!" }]);

console.log(response.content);
```

### With System Message

```javascript
const response = await provider.chat(
  [
    { role: "system", content: "You are a helpful assistant." },
    { role: "user", content: "Tell me a joke" },
  ],
  {
    temperature: 0.8,
    maxTokens: 150,
  },
);
```

### Multi-turn Conversation

```javascript
const messages = [
  { role: "user", content: "What is 2+2?" },
  { role: "assistant", content: "2+2 equals 4." },
  { role: "user", content: "What about 2+3?" },
];

const response = await provider.chat(messages);
```

---

## Function Calling

### Define Tools

```javascript
const tools = [
  {
    type: "function",
    function: {
      name: "search_projects",
      description: "Search for projects in database",
      parameters: {
        type: "object",
        properties: {
          query: { type: "string", description: "Search query" },
        },
        required: ["query"],
      },
    },
  },
];
```

### Chat with Tools

```javascript
const response = await provider.chat(messages, { tools });

if (response.tool_calls) {
  for (const call of response.tool_calls) {
    const result = await functionCallingService.executeFunction(call);
    console.log(result);
  }
}
```

### Execute Single Function

```javascript
import functionCallingService from "./modules/ai/services/FunctionCallingService.js";

const result = await functionCallingService.executeFunction({
  name: "search_projects",
  arguments: { query: "authentication" },
});
```

---

## Streaming

### Basic Streaming

```javascript
import { AIStreamingHandler } from "./modules/ai/components/AIStreamingHandler.js";

const handler = new AIStreamingHandler(containerElement);

const fullResponse = await handler.streamResponse(provider, messages, {
  temperature: 0.7,
});
```

### With Callbacks

```javascript
await handler.streamResponse(provider, messages, {
  onToken: (token) => console.log("Token:", token),
  onComplete: (text) => console.log("Done:", text),
  onError: (error) => console.error("Error:", error),
});
```

### Manual Streaming

```javascript
for await (const chunk of provider.streamChat(messages)) {
  const token = chunk.choices[0]?.delta?.content || "";
  process(token);
}
```

---

## Chat History

### Create Conversation

```javascript
import conversationManager from "./modules/ai/components/AIConversationManager.js";

const convId = await conversationManager.createConversation("My Chat", {
  provider: "openai",
  model: "gpt-4",
});
```

### Save Messages

```javascript
await conversationManager.saveMessage(convId, {
  role: "user",
  content: "Hello",
});

await conversationManager.saveMessage(convId, {
  role: "assistant",
  content: "Hi there!",
});
```

### Load Conversation

```javascript
const messages = await conversationManager.loadConversation(convId);
console.log(messages);
```

### List All Conversations

```javascript
const conversations = await conversationManager.loadConversations();
```

### Delete Conversation

```javascript
await conversationManager.deleteConversation(convId);
```

---

## Provider Management

### List Providers

```javascript
const providers = providerRegistry.getAll();
// { openai: {...}, claude: {...}, gemini: {...}, ... }
```

### Switch Provider

```javascript
providerRegistry.setActive("claude");
```

### Get Current Provider

```javascript
const current = providerRegistry.getActive();
console.log(current.name); // "Claude"
```

### Check Capabilities

```javascript
console.log(provider.supportsStreaming()); // true/false
console.log(provider.supportsFunctionCalling()); // true/false
```

### Get Models

```javascript
const models = await provider.getModels();
// [{ id: 'gpt-4', name: 'GPT-4' }, ...]
```

---

## Common Patterns

### Error Handling

```javascript
try {
  const response = await provider.chat(messages);
} catch (error) {
  if (error.message.includes("API key")) {
    console.error("Invalid API key");
  } else if (error.message.includes("rate limit")) {
    console.error("Rate limited");
  } else {
    console.error("Unknown error:", error);
  }
}
```

### Retry Logic

```javascript
async function chatWithRetry(messages, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await provider.chat(messages);
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await new Promise((r) => setTimeout(r, 1000 * (i + 1)));
    }
  }
}
```

### Token Counting (Approximate)

```javascript
function estimateTokens(text) {
  // Rough estimate: 1 token ≈ 4 characters
  return Math.ceil(text.length / 4);
}

const totalTokens = messages.reduce(
  (sum, msg) => sum + estimateTokens(msg.content),
  0,
);
```

### Context Window Management

```javascript
function truncateMessages(messages, maxTokens = 4000) {
  let total = 0;
  const result = [];

  for (let i = messages.length - 1; i >= 0; i--) {
    const tokens = estimateTokens(messages[i].content);
    if (total + tokens > maxTokens) break;
    result.unshift(messages[i]);
    total += tokens;
  }

  return result;
}
```

### Add Message to UI

```javascript
import { AIMessageRenderer } from "./modules/ai/components/AIMessageRenderer.js";

AIMessageRenderer.addMessage(containerElement, "user", "Hello!");

AIMessageRenderer.addMessage(
  containerElement,
  "assistant",
  "Hi there! How can I help?",
);
```

### Show Typing Indicator

```javascript
const typingIndicator = document.createElement("div");
typingIndicator.className = "ai-typing-indicator";
typingIndicator.innerHTML = `
  <div class="ai-message-avatar">🤖</div>
  <div class="ai-typing-dots">
    <span></span><span></span><span></span>
  </div>
`;
container.appendChild(typingIndicator);

// Remove when done
typingIndicator.remove();
```

---

## Troubleshooting

### Provider Not Configured

```javascript
const provider = providerRegistry.getActive();
if (!provider.validateConfig()) {
  console.error("Configure provider first!");
  provider.configure({
    apiKey: "your-key",
    model: "model-name",
  });
}
```

### Panel Not Opening

```javascript
// Force open
aiPanel.open();

// Check CSS
const hasCss = !!document.querySelector('link[href*="ai-panel.css"]');
console.log("CSS loaded:", hasCss);

// Re-init
await aiPanel.init();
```

### Function Not Executing

```javascript
import { executeFunction } from "./modules/ai/services/functions/index.js";

// List available functions
console.log("Functions:", Object.keys(allFunctions));

// Test function
try {
  const result = await executeFunction({
    name: "search_projects",
    arguments: { query: "test" },
  });
  console.log("Success:", result);
} catch (error) {
  console.error("Failed:", error);
}
```

### Streaming Not Working

```javascript
if (!provider.supportsStreaming()) {
  console.error("Provider does not support streaming");
  // Use regular chat instead
  const response = await provider.chat(messages);
}
```

### Clear Everything

```javascript
// Clear history
import chatHistoryManager from "./modules/ai/services/ChatHistoryManager.js";

// Clear UI
aiPanel.clearConversation();

await chatHistoryManager.clearAllChats();

// Reset settings
localStorage.removeItem("ai-settings");
```

---

## Quick Commands

```javascript
// Toggle panel
eventBus.emit("ai-panel:toggle");

// Send message programmatically
await aiPanel.sendMessage("Hello AI!");

// Change size
aiPanel.setSize("large"); // small, medium, large

// Show settings
const settingsModal = new AISettingsModal(aiPanel);
await settingsModal.show();

// Show history
const historyModal = new AIHistoryModal(aiPanel);
await historyModal.show();
```

---

## Configuration Options

### Chat Options

```javascript
const response = await provider.chat(messages, {
  temperature: 0.7, // 0-2, higher = more creative
  maxTokens: 2048, // Max response length
  topP: 1.0, // Nucleus sampling
  frequencyPenalty: 0, // -2 to 2
  presencePenalty: 0, // -2 to 2
  stream: false, // Enable streaming
  tools: [], // Function calling tools
  toolChoice: "auto", // auto, none, or specific function
});
```

### Provider Config

```javascript
// OpenAI
provider.configure({
  apiKey: "sk-...",
  model: "gpt-4-turbo-preview",
  baseURL: "https://api.openai.com/v1", // Optional
  organization: "org-...", // Optional
});

// Claude
provider.configure({
  apiKey: "sk-ant-...",
  model: "claude-3-sonnet-20240229",
});

// ViByte (Ollama)
provider.configure({
  baseURL: "http://localhost:11434",
  model: "llama2",
});
```

---

## Keyboard Shortcuts

- `Ctrl/Cmd + K` - Toggle AI panel
- `Enter` - Send message
- `Shift + Enter` - New line in message
- `Esc` - Close modals

---

## Best Practices

✅ **DO:**

- Always validate config before using provider
- Handle errors gracefully
- Use streaming for better UX
- Save important conversations
- Clear old history periodically
- Use appropriate temperature (0.7-0.9 for creative, 0.1-0.3 for factual)

❌ **DON'T:**

- Don't expose API keys in frontend code
- Don't send sensitive data without encryption
- Don't exceed provider rate limits
- Don't ignore error messages
- Don't use very high temperatures (>1.5) unless needed

---

## Examples

### Complete Chat Flow

```javascript
// 1. Setup
await aiPanel.init();
const provider = providerRegistry.get("openai");
provider.configure({ apiKey: "sk-...", model: "gpt-4" });
providerRegistry.setActive("openai");

// 2. Create conversation
const convId = await conversationManager.createConversation(
  "Project Planning",
  { provider: "openai", model: "gpt-4" },
);

// 3. Chat
const messages = [
  { role: "system", content: "You are a project planning assistant." },
  { role: "user", content: "Help me plan a new feature" },
];

const response = await provider.chat(messages);

// 4. Save
await conversationManager.saveMessage(convId, messages[1]);
await conversationManager.saveMessage(convId, {
  role: "assistant",
  content: response.content,
});

// 5. Display
AIMessageRenderer.addMessage(container, "assistant", response.content);
```

### Function Calling Flow

```javascript
// 1. Define tools
const tools = [
  {
    type: "function",
    function: {
      name: "search_projects",
      description: "Search projects",
      parameters: {
        type: "object",
        properties: {
          query: { type: "string" },
        },
        required: ["query"],
      },
    },
  },
];

// 2. Initial request
const response = await provider.chat(
  [{ role: "user", content: "Find authentication projects" }],
  { tools },
);

// 3. Execute tools
if (response.tool_calls) {
  const results = [];
  for (const call of response.tool_calls) {
    const result = await functionCallingService.executeFunction(call);
    results.push({ tool_call_id: call.id, content: JSON.stringify(result) });
  }

  // 4. Send results back
  const finalResponse = await provider.chat([
    { role: "user", content: "Find authentication projects" },
    { role: "assistant", tool_calls: response.tool_calls },
    ...results.map((r) => ({ role: "tool", ...r })),
  ]);

  console.log(finalResponse.content);
}
```

---

**Last Updated:** November 9, 2024  
**Version:** 1.0.0

For detailed documentation, see [README.md](./README.md)
