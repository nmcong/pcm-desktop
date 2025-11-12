# AI Assistant Module

## 📖 Mục Lục

- [Tổng Quan](#-tổng-quan)
- [Cấu Trúc Module](#-cấu-trúc-module)
- [Components](#-components)
- [Services](#-services)
- [Styles](#-styles)
- [Hướng Dẫn Sử Dụng](#-hướng-dẫn-sử-dụng)
- [API Reference](#-api-reference)
- [Mở Rộng Module](#-mở-rộng-module)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Tổng Quan

AI Assistant Module cung cấp khả năng tương tác với các Large Language Models (LLMs) thông qua một interface thống nhất. Module hỗ trợ nhiều providers và tích hợp function calling để truy vấn dữ liệu hệ thống.

### ✨ Tính Năng Chính

- 🤖 **Multi-Provider Support**: OpenAI, Claude, Gemini, HuggingFace, ViByte
- 🔧 **Function Calling**: AI có thể gọi functions để truy vấn database
- 💬 **Streaming Responses**: Real-time response với typing indicators
- 📝 **Chat History**: Lưu và quản lý lịch sử conversations
- ⚙️ **Settings Management**: Cấu hình providers và models
- 🎨 **Modern UI**: Responsive design với animations mượt mà
- 🔄 **Model Selection**: Dynamic model picker với search
- 🛠️ **Tool Execution**: Visual feedback cho function calls

### 📊 Thống Kê

- **Total Files**: 30 files
- **JavaScript**: 26 files (9 components + 17 services)
- **CSS**: 2 files (1,345 lines)
- **Documentation**: 2 README files

---

## 📂 Cấu Trúc Module

```
ai/
├── components/                    # UI Components (9 files)
│   ├── AIPanel.js                 # Main container component
│   ├── AIChatView.js              # Chat messages display manager
│   ├── AIConversationManager.js   # Conversation state & history
│   ├── AIFunctionCallHandler.js   # Tool execution UI & display
│   ├── AIHistoryModal.js          # Chat history modal
│   ├── AIMessageRenderer.js       # Individual message rendering
│   ├── AIModelSelector.js         # Model selection dropdown
│   ├── AISettingsModal.js         # Settings configuration UI
│   └── AIStreamingHandler.js      # Streaming response handler
│
├── services/                      # Business Logic (12 files)
│   ├── ChatHistoryManager.js      # IndexedDB persistence
│   ├── ProviderRegistry.js        # Provider management & switching
│   ├── BaseProvider.js            # Abstract provider base class
│   ├── OpenAIProvider.js          # OpenAI GPT integration
│   ├── ClaudeProvider.js          # Anthropic Claude integration
│   ├── GeminiProvider.js          # Google Gemini integration
│   ├── HuggingFaceProvider.js     # HuggingFace models
│   ├── ViByteProvider.js          # ViByte Cloud AI (Ollama-compatible)
│   ├── FunctionCallingAdapter.js  # Unified function calling system
│   ├── FunctionCallingService.js  # Function execution service
│   ├── TemplateProvider.js        # Template for new providers
│   ├── README.md                  # Provider documentation
│   │
│   └── functions/                 # AI Functions (6 files)
│       ├── index.js               # Functions registry & executor
│       ├── DataFunctions.js       # Data import/export operations
│       ├── ProjectFunctions.js    # Project CRUD operations
│       ├── ScreenFunctions.js     # Screen CRUD operations
│       ├── GitHubFunctions.js     # GitHub integration functions
│       ├── SubsystemFunctions.js  # Subsystem CRUD operations
│       └── README.md              # Functions documentation
│
├── styles/                        # CSS Styles (2 files)
│   ├── ai-panel.css               # Main panel styles (1,202 lines)
│   └── ai-chat-history.css        # Chat history styles (143 lines)
│
└── README.md                      # This file
```

---

## 🧩 Components

### 1. AIPanel.js

**Main container component** - Quản lý toàn bộ AI panel UI.

```javascript
import aiPanel from "./modules/ai/components/AIPanel.js";

// Initialize
await aiPanel.init();

// Toggle panel
aiPanel.toggle();

// Open/Close
aiPanel.open();
aiPanel.close();
```

**Features:**

- Sliding panel với smooth animations
- Resizable (small, medium, large)
- Header với provider badge và model selector
- Chat messages area với auto-scroll
- Input form với send button
- Action buttons (new chat, history, settings, close)

**Events:**

```javascript
eventBus.on("ai-panel:toggle", () => {});
eventBus.on("ai-panel:open", () => {});
eventBus.on("ai-panel:close", () => {});
```

---

### 2. AIChatView.js

**Chat display manager** - Quản lý hiển thị messages.

```javascript
import { AIChatView } from "./modules/ai/components/AIChatView.js";

const chatView = new AIChatView(containerElement);

// Add message
chatView.addMessage("user", "Hello AI!");
chatView.addMessage("assistant", "Hi! How can I help?");

// Clear messages
chatView.clear();

// Scroll to bottom
chatView.scrollToBottom();
```

**Features:**

- Welcome screen khi chưa có messages
- User và assistant message rendering
- Auto-scroll to bottom
- Typing indicators

---

### 3. AIConversationManager.js

**Conversation state manager** - Quản lý state và persistence.

```javascript
import conversationManager from "./modules/ai/components/AIConversationManager.js";

await conversationManager.init();

// Create conversation
const conversationId = await conversationManager.createConversation(
  "Chat with Claude",
  { provider: "claude", model: "claude-3-sonnet" },
);

// Load conversation
const messages = await conversationManager.loadConversation(conversationId);

// Save message
await conversationManager.saveMessage(conversationId, {
  role: "user",
  content: "Hello",
});

// Delete conversation
await conversationManager.deleteConversation(conversationId);
```

---

### 4. AIFunctionCallHandler.js

**Tool execution UI** - Hiển thị function calls và results.

```javascript
import { AIFunctionCallHandler } from "./modules/ai/components/AIFunctionCallHandler.js";

const handler = new AIFunctionCallHandler(containerElement);

// Create tool call UI
const toolCallElement = handler.createCompactToolCallUI(toolCalls, llmResponse);

// Execute tools
await handler.executeToolCalls(toolCalls);
```

**Features:**

- Compact collapsible UI
- Loading states với spinners
- Success/error indicators
- Tool arguments display
- Results display với formatting

---

### 5. AIHistoryModal.js

**History modal** - Modal hiển thị chat history.

```javascript
import { AIHistoryModal } from "./modules/ai/components/AIHistoryModal.js";

const historyModal = new AIHistoryModal(aiPanelInstance);

// Show modal
await historyModal.show();
```

**Features:**

- List tất cả conversations
- Search và filter
- Load conversation
- Delete conversation
- New conversation button

---

### 6. AIMessageRenderer.js

**Message renderer** - Render individual messages.

```javascript
import { AIMessageRenderer } from "./modules/ai/components/AIMessageRenderer.js";

// Add message
const messageElement = AIMessageRenderer.addMessage(
  container,
  "assistant",
  "**Bold** text with `code`",
);
```

**Features:**

- Markdown rendering với syntax highlighting
- Code blocks với copy button
- User/assistant avatar styling
- Timestamp display
- Responsive layout

---

### 7. AIModelSelector.js

**Model selector** - Dropdown để chọn model.

```javascript
import { AIModelSelector } from "./modules/ai/components/AIModelSelector.js";

const selector = new AIModelSelector(containerElement, (modelId) => {
  console.log("Model changed:", modelId);
});

await selector.init();
```

**Features:**

- Dynamic model fetching từ provider API
- Search functionality
- Active model indicator
- Keyboard navigation
- Loading states

---

### 8. AISettingsModal.js

**Settings modal** - Modal cấu hình AI settings.

```javascript
import { AISettingsModal } from "./modules/ai/components/AISettingsModal.js";

const settingsModal = new AISettingsModal(aiPanelInstance);

// Show modal
await settingsModal.show();
```

**Features:**

- Provider selection
- API key configuration
- Model selection
- Conversation settings (temperature, max tokens)
- Function calling toggle
- Database access toggle

---

### 9. AIStreamingHandler.js

**Streaming handler** - Xử lý streaming responses.

```javascript
import { AIStreamingHandler } from "./modules/ai/components/AIStreamingHandler.js";

const streamingHandler = new AIStreamingHandler(containerElement);

// Stream response
const fullResponse = await streamingHandler.streamResponse(provider, messages, {
  temperature: 0.7,
});
```

**Features:**

- Real-time text streaming
- Typing cursor animation
- Markdown rendering on-the-fly
- Error handling
- Cancellation support

---

## ⚙️ Services

### Provider System

#### ProviderRegistry.js

**Provider management** - Singleton quản lý tất cả providers.

```javascript
import providerRegistry from "./modules/ai/services/ProviderRegistry.js";

// Get active provider
const provider = providerRegistry.getActive();

// Get specific provider
const openai = providerRegistry.get("openai");

// Switch provider
providerRegistry.setActive("claude");

// List all providers
const providers = providerRegistry.getAll();
// Returns: { openai: {...}, claude: {...}, gemini: {...}, ... }
```

**Available Providers:**

- `openai` - OpenAI GPT models
- `claude` - Anthropic Claude models
- `gemini` - Google Gemini models
- `huggingface` - HuggingFace inference API
- `vibyte` - ViByte Cloud AI (Ollama-compatible)

---

#### BaseProvider.js

**Abstract base class** - Tất cả providers extend từ class này.

```javascript
import { BaseProvider } from "./modules/ai/services/BaseProvider.js";

class CustomProvider extends BaseProvider {
  constructor() {
    super("custom", "Custom Provider", "https://api.custom.com");
  }

  async chat(messages, options = {}) {
    // Implementation
  }

  async getModels() {
    // Implementation
  }
}
```

**Required Methods:**

- `chat(messages, options)` - Send chat request
- `getModels()` - Fetch available models
- `validateConfig()` - Validate API configuration

**Optional Methods:**

- `streamChat(messages, options)` - Streaming chat
- `supportsStreaming()` - Check streaming support
- `supportsFunctionCalling()` - Check function calling support

---

#### OpenAIProvider.js

**OpenAI integration** - Hỗ trợ GPT models.

```javascript
import { OpenAIProvider } from './modules/ai/services/OpenAIProvider.js';

const provider = new OpenAIProvider();

// Configure
provider.configure({
  apiKey: 'sk-...',
  model: 'gpt-4-turbo-preview',
  baseURL: 'https://api.openai.com/v1' // Optional
});

// Chat
const response = await provider.chat([
  { role: 'user', content: 'Hello!' }
], {
  temperature: 0.7,
  maxTokens: 2048,
  stream: false
});

// Function calling
const response = await provider.chat(messages, {
  tools: [
    {
      type: 'function',
      function: {
        name: 'search_projects',
        description: 'Search projects',
        parameters: { /* JSON Schema */ }
      }
    }
  ]
});
```

**Models:** GPT-4 Turbo, GPT-4, GPT-3.5 Turbo

---

#### ClaudeProvider.js

**Claude integration** - Hỗ trợ Claude models.

```javascript
import { ClaudeProvider } from "./modules/ai/services/ClaudeProvider.js";

const provider = new ClaudeProvider();

provider.configure({
  apiKey: "sk-ant-...",
  model: "claude-3-sonnet-20240229",
});

// Same API as OpenAI
const response = await provider.chat(messages, options);
```

**Models:** Claude 3 Opus, Sonnet, Haiku

---

#### GeminiProvider.js

**Gemini integration** - Google Gemini models.

```javascript
import { GeminiProvider } from "./modules/ai/services/GeminiProvider.js";

const provider = new GeminiProvider();

provider.configure({
  apiKey: "AIza...",
  model: "gemini-pro",
});
```

**Models:** Gemini Pro, Gemini Pro Vision

---

#### ViByteProvider.js

**ViByte Cloud AI** - Ollama-compatible local models.

```javascript
import { ViByteProvider } from "./modules/ai/services/ViByteProvider.js";

const provider = new ViByteProvider();

provider.configure({
  baseURL: "http://localhost:11434",
  model: "llama2",
});
```

**Models:** Llama 2, Mistral, Mixtral, Code Llama, và các models khác

---

### Function Calling System

#### FunctionCallingAdapter.js

**Unified function calling** - Adapter thống nhất cho tất cả providers.

```javascript
import {
  NativeFunctionCallingAdapter,
  TextBasedFunctionCallingAdapter
} from './modules/ai/services/FunctionCallingAdapter.js';

// For providers with native support (OpenAI, Claude)
const adapter = new NativeFunctionCallingAdapter();

// For providers without native support (Ollama, HuggingFace)
const adapter = new TextBasedFunctionCallingAdapter();

// Convert tools to provider format
const formattedTools = adapter.formatTools(tools);

// Parse response
const toolCalls = adapter.parseResponse(response);
```

---

#### FunctionCallingService.js

**Function execution** - Service thực thi functions.

```javascript
import functionCallingService from "./modules/ai/services/FunctionCallingService.js";

// Execute function call
const result = await functionCallingService.executeFunction({
  name: "search_projects",
  arguments: { query: "authentication" },
});
```

---

### AI Functions

#### Function Format

Tất cả functions follow format chuẩn:

```javascript
export const myFunctions = {
  functionName: {
    name: "functionName",
    description: "Function description for AI",
    parameters: {
      type: "object",
      properties: {
        param1: {
          type: "string",
          description: "Parameter description",
        },
      },
      required: ["param1"],
    },
    handler: async (args) => {
      // Implementation
      return result;
    },
  },
};
```

#### Available Functions

**DataFunctions.js**

- `exportAllData` - Export all system data
- `importData` - Import data from JSON

**ProjectFunctions.js**

- `getAllProjects` - Get all projects
- `getProjectDetails` - Get specific project
- `searchProjects` - Search projects by query
- `createProject` - Create new project
- `updateProject` - Update project
- `deleteProject` - Delete project

**ScreenFunctions.js**

- `getAllScreens` - Get all screens
- `getScreenDetails` - Get specific screen
- `searchScreens` - Search screens
- `createScreen` - Create screen
- `updateScreen` - Update screen
- `deleteScreen` - Delete screen

**GitHubFunctions.js**

- `checkGitHubAuth` - Check GitHub auth status
- `connectGitHub` - Connect GitHub account
- `getRepositories` - Get user repositories
- `getRepoFiles` - Get repository files

**SubsystemFunctions.js**

- `getAllSubsystems` - Get all subsystems
- `getSubsystemDetails` - Get subsystem details
- `createSubsystem` - Create subsystem
- `updateSubsystem` - Update subsystem
- `deleteSubsystem` - Delete subsystem

---

### Chat History Manager

#### ChatHistoryManager.js

**IndexedDB persistence** - Lưu và quản lý chat history.

```javascript
import chatHistoryManager from "./modules/ai/services/ChatHistoryManager.js";

await chatHistoryManager.init();

// Create chat
const chatId = await chatHistoryManager.createChat("openai", "OpenAI", "gpt-4");

// Add message
await chatHistoryManager.addMessage(chatId, {
  role: "user",
  content: "Hello",
  timestamp: Date.now(),
});

// Get chat
const chat = await chatHistoryManager.getChat(chatId);

// Get all chats
const chats = await chatHistoryManager.getAllChats();

// Delete chat
await chatHistoryManager.deleteChat(chatId);

// Clear all
await chatHistoryManager.clearAllChats();
```

**Storage:** IndexedDB (pcm-webapp-db, chatHistory store)

---

## 🎨 Styles

### ai-panel.css (1,202 lines)

**Main panel styles** - Tất cả styles cho AI panel UI.

**Sections:**

- `.ai-panel` - Main container
- `.ai-panel-header` - Header với title và actions
- `.ai-provider-badge` - Provider indicator
- `.ai-model-selector` - Model dropdown
- `.ai-messages` - Messages container
- `.ai-message` - Individual message
- `.ai-tool-call-compact` - Tool execution UI
- `.ai-input` - Input form
- `.ai-settings-form` - Settings modal
- Animations: sparkle, blink, pulse, spin, typing, etc.

### ai-chat-history.css (143 lines)

**Chat history styles** - Styles cho history modal.

**Sections:**

- `.chat-history-container` - Modal container
- `.chat-history-list` - Conversations list
- `.chat-history-item` - Individual conversation
- `.chat-history-empty-state` - Empty state

---

## 🚀 Hướng Dẫn Sử Dụng

### Quick Start

1. **Include styles trong HTML:**

```html
<link rel="stylesheet" href="public/js/modules/ai/styles/ai-panel.css" />
<link rel="stylesheet" href="public/js/modules/ai/styles/ai-chat-history.css" />
```

2. **Import và khởi tạo:**

```javascript
import aiPanel from "./modules/ai/components/AIPanel.js";

// Initialize
await aiPanel.init();
```

3. **Cấu hình provider:**

```javascript
import providerRegistry from "./modules/ai/services/ProviderRegistry.js";

const openai = providerRegistry.get("openai");
openai.configure({
  apiKey: "sk-...",
  model: "gpt-4-turbo-preview",
});

providerRegistry.setActive("openai");
```

4. **Sử dụng:**

```javascript
// Toggle panel
eventBus.emit("ai-panel:toggle");

// Or programmatically
aiPanel.toggle();
```

---

### Basic Chat

```javascript
import providerRegistry from "./modules/ai/services/ProviderRegistry.js";

const provider = providerRegistry.getActive();

const response = await provider.chat(
  [
    { role: "system", content: "You are a helpful assistant." },
    { role: "user", content: "Hello!" },
  ],
  {
    temperature: 0.7,
    maxTokens: 2048,
  },
);

console.log(response.content);
```

---

### Function Calling

```javascript
// Define tools
const tools = [
  {
    type: "function",
    function: {
      name: "search_projects",
      description: "Search for projects in the database",
      parameters: {
        type: "object",
        properties: {
          query: {
            type: "string",
            description: "Search query",
          },
        },
        required: ["query"],
      },
    },
  },
];

// Chat with tools
const response = await provider.chat(messages, { tools });

// Check if AI wants to call functions
if (response.tool_calls) {
  // Execute tools
  for (const toolCall of response.tool_calls) {
    const result = await functionCallingService.executeFunction(toolCall);
    console.log("Tool result:", result);
  }
}
```

---

### Streaming Chat

```javascript
import { AIStreamingHandler } from "./modules/ai/components/AIStreamingHandler.js";

const streamingHandler = new AIStreamingHandler(messagesContainer);

// Stream response
const fullResponse = await streamingHandler.streamResponse(provider, messages, {
  temperature: 0.7,
  onToken: (token) => {
    console.log("Token:", token);
  },
  onComplete: (fullText) => {
    console.log("Complete:", fullText);
  },
  onError: (error) => {
    console.error("Error:", error);
  },
});
```

---

### Chat History

```javascript
import conversationManager from "./modules/ai/components/AIConversationManager.js";

// Create conversation
const convId = await conversationManager.createConversation("My Chat", {
  provider: "openai",
  model: "gpt-4",
});

// Save messages
await conversationManager.saveMessage(convId, {
  role: "user",
  content: "Hello",
});

await conversationManager.saveMessage(convId, {
  role: "assistant",
  content: "Hi there!",
});

// Load later
const messages = await conversationManager.loadConversation(convId);
```

---

## 📖 API Reference

### AIPanel

**Properties:**

- `isOpen: boolean` - Panel open state
- `currentSize: string` - Panel size (small/medium/large)
- `messages: Array` - Current conversation messages
- `conversationSettings: Object` - Current settings

**Methods:**

- `init(): Promise<void>` - Initialize panel
- `open(): void` - Open panel
- `close(): void` - Close panel
- `toggle(): void` - Toggle open/close
- `sendMessage(message: string): Promise<void>` - Send message
- `clearConversation(): void` - Clear current conversation
- `setSize(size: string): void` - Set panel size
- `destroy(): void` - Cleanup and destroy

---

### ProviderRegistry

**Methods:**

- `register(provider: BaseProvider): void` - Register provider
- `get(id: string): BaseProvider` - Get provider by ID
- `getActive(): BaseProvider` - Get active provider
- `setActive(id: string): void` - Set active provider
- `getAll(): Object` - Get all providers
- `configure(id: string, config: Object): void` - Configure provider

---

### BaseProvider

**Properties:**

- `id: string` - Provider ID
- `name: string` - Display name
- `baseURL: string` - API base URL
- `config: Object` - Current configuration

**Methods:**

- `configure(config: Object): void` - Set configuration
- `validateConfig(): boolean` - Validate config
- `chat(messages: Array, options: Object): Promise<Object>` - Send chat
- `streamChat(messages: Array, options: Object): AsyncGenerator` - Stream chat
- `getModels(): Promise<Array>` - Get available models
- `supportsStreaming(): boolean` - Check streaming support
- `supportsFunctionCalling(): boolean` - Check function calling support

---

### ChatHistoryManager

**Methods:**

- `init(): Promise<void>` - Initialize database
- `createChat(providerId, providerName, model): Promise<number>` - Create chat
- `getChat(id): Promise<Object>` - Get chat by ID
- `getAllChats(): Promise<Array>` - Get all chats
- `addMessage(chatId, message): Promise<void>` - Add message
- `updateChat(id, updates): Promise<void>` - Update chat
- `deleteChat(id): Promise<void>` - Delete chat
- `clearAllChats(): Promise<void>` - Delete all chats

---

## 🔧 Mở Rộng Module

### Thêm Provider Mới

1. **Tạo file provider:**

```javascript
// services/MyProvider.js
import { BaseProvider } from "./BaseProvider.js";

export class MyProvider extends BaseProvider {
  constructor() {
    super("myprovider", "My Provider", "https://api.example.com");
  }

  async chat(messages, options = {}) {
    // Validate config
    if (!this.validateConfig()) {
      throw new Error("Provider not configured");
    }

    // Make API request
    const response = await fetch(`${this.baseURL}/chat`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${this.config.apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        messages,
        ...options,
      }),
    });

    const data = await response.json();

    return {
      content: data.message,
      role: "assistant",
      model: options.model || this.config.model,
    };
  }

  async getModels() {
    const response = await fetch(`${this.baseURL}/models`, {
      headers: {
        Authorization: `Bearer ${this.config.apiKey}`,
      },
    });

    return await response.json();
  }

  validateConfig() {
    return !!(this.config?.apiKey && this.config?.model);
  }
}
```

2. **Register provider:**

```javascript
// services/ProviderRegistry.js
import { MyProvider } from "./MyProvider.js";

// In constructor
this.register(new MyProvider());
```

---

### Thêm Function Mới

1. **Tạo function trong functions/:**

```javascript
// services/functions/MyFunctions.js
export const myFunctions = {
  myCustomFunction: {
    name: "myCustomFunction",
    description: "Does something useful",
    parameters: {
      type: "object",
      properties: {
        param1: {
          type: "string",
          description: "First parameter",
        },
        param2: {
          type: "number",
          description: "Second parameter",
          minimum: 0,
        },
      },
      required: ["param1"],
    },
    handler: async ({ param1, param2 = 0 }) => {
      try {
        // Do something
        const result = await someOperation(param1, param2);

        return {
          success: true,
          data: result,
        };
      } catch (error) {
        throw new Error(`Function failed: ${error.message}`);
      }
    },
  },
};

export default myFunctions;
```

2. **Register trong index.js:**

```javascript
// services/functions/index.js
import myFunctions from "./MyFunctions.js";

// Add to registry
const allFunctions = {
  ...dataFunctions,
  ...projectFunctions,
  ...myFunctions, // Add here
};
```

---

### Customize Styles

1. **Override CSS variables:**

```css
/* In your custom CSS */
:root {
  --ai-panel-width: 800px;
  --ai-primary-color: #your-color;
}
```

2. **Or create theme:**

```css
/* styles/ai-panel-dark-theme.css */
.ai-panel {
  --ai-bg: #1a1a1a;
  --ai-text: #ffffff;
  /* ... more overrides */
}
```

---

## 🐛 Troubleshooting

### Panel không mở

**Vấn đề:** Panel không xuất hiện khi click.

**Giải pháp:**

```javascript
// Check if panel initialized
console.log(aiPanel.element);

// Re-initialize
await aiPanel.init();

// Check CSS loaded
const link = document.querySelector('link[href*="ai-panel.css"]');
console.log("CSS loaded:", !!link);
```

---

### Provider API Error

**Vấn đề:** "Provider not configured" hoặc API errors.

**Giải pháp:**

```javascript
// Check provider config
const provider = providerRegistry.getActive();
console.log("Config:", provider.config);
console.log("Valid:", provider.validateConfig());

// Reconfigure
provider.configure({
  apiKey: "your-api-key",
  model: "model-name",
});
```

---

### Function Calling Not Working

**Vấn đề:** AI không gọi functions.

**Giải pháp:**

1. Check if provider supports function calling:

```javascript
const provider = providerRegistry.getActive();
console.log("Supports functions:", provider.supportsFunctionCalling());
```

2. Check if functions enabled:

```javascript
// In AI Settings
conversationSettings.enableFunctionCalling = true;
```

3. Verify function definitions:

```javascript
import { executeFunction } from "./modules/ai/services/functions/index.js";

console.log("Available functions:", Object.keys(allFunctions));
```

---

### Streaming Issues

**Vấn đề:** Streaming không hoạt động hoặc bị lag.

**Giải pháp:**

```javascript
// Check streaming support
const provider = providerRegistry.getActive();
console.log("Supports streaming:", provider.supportsStreaming());

// Adjust render interval
streamingHandler.RENDER_INTERVAL = 500; // ms

// Check network
// Streaming requires stable connection
```

---

### Chat History Not Saving

**Vấn đề:** Messages không được lưu.

**Giải pháp:**

```javascript
// Check IndexedDB
await chatHistoryManager.init();

// Test save
const testId = await chatHistoryManager.createChat("test", "Test", "test");
await chatHistoryManager.addMessage(testId, {
  role: "user",
  content: "test",
});

const chat = await chatHistoryManager.getChat(testId);
console.log("Saved:", chat);

// Clear if corrupted
await chatHistoryManager.clearAllChats();
```

---

### Import Path Errors

**Vấn đề:** "Module not found" errors.

**Giải pháp:**

```javascript
// Use correct relative paths
import aiPanel from './modules/ai/components/AIPanel.js';
//                  ↑ Adjust based on your file location

// From components/
import aiPanel from '../modules/ai/components/AIPanel.js';

// From services/
import aiPanel from '../modules/ai/components/AIPanel.js';
```

---

## 📚 Resources

### Internal Documentation

- [Services README](./services/README.md) - Provider và function docs
- [Functions README](./services/functions/README.md) - Function calling guide
- [Modules README](../README.md) - General modules guide

### External Resources

- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Anthropic Claude API](https://docs.anthropic.com/)
- [Google Gemini API](https://ai.google.dev/docs)
- [Function Calling Guide](https://platform.openai.com/docs/guides/function-calling)

### Related Documentation

- [PCM WebApp Documentation](../../../docs/)
- [AI Integration Guides](../../../docs/ai-integration/)
- [API Standards](../../../docs/vibytes-framework/API-STANDARD.md)

---

## 🤝 Contributing

### Coding Standards

- Follow [JavaScript Best Practices](../../README.md)
- Use ES6+ features
- Document public APIs with JSDoc
- Write descriptive commit messages

### Pull Request Process

1. Create feature branch
2. Implement changes
3. Add/update tests
4. Update documentation
5. Submit PR với detailed description

### Testing

```bash
# Run tests (nếu có test suite)
npm test

# Lint
npm run lint

# Format
npm run format
```

---

## 📝 Changelog

### Version 1.0.0 (2024-11-09)

**Initial Release**

✅ **Components:**

- AIPanel với full-featured UI
- Chat view với streaming support
- Function calling UI
- History management
- Settings configuration
- Model selection

✅ **Services:**

- 5 AI providers (OpenAI, Claude, Gemini, HuggingFace, ViByte)
- Function calling system
- Chat history persistence
- Provider registry

✅ **Functions:**

- Data operations
- Project CRUD
- Screen CRUD
- GitHub integration
- Subsystem management

✅ **Styles:**

- Modern, responsive UI
- Smooth animations
- Dark/light theme support
- 1,345 lines of CSS

✅ **Documentation:**

- Comprehensive README
- API reference
- Usage examples
- Troubleshooting guide

---

## 📄 License

This module is part of PCM WebApp project.

**Maintained by:** PCM WebApp Team  
**Last Updated:** November 9, 2024  
**Version:** 1.0.0

---

## 🙋 Support

Nếu gặp vấn đề hoặc cần hỗ trợ:

1. Kiểm tra [Troubleshooting](#-troubleshooting)
2. Xem [Documentation](../../../docs/)
3. Contact team qua issue tracker

**Happy Coding! 🚀**
