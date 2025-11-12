# PCM WebApp Documentation

**Version**: 1.2.0  
**Last Updated**: November 6, 2025  
**Status**: ✅ Production Ready

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Documentation Index](#-documentation-index)
- [Quick Start](#-quick-start)
- [Feature Overview](#-feature-overview)
- [Technical Architecture](#-technical-architecture)
- [Development Workflow](#-development-workflow)
- [Roadmap](#-roadmap)
- [Changelog](#-changelog)
- [Quick Links](#quick-links)

---

## 🎯 Overview

PCM (Project Configuration Management) WebApp is a comprehensive system for managing complex software projects with:

- **AI-Powered Insights** - Query project data naturally
- **Function Calling** - ANY LLM can query IndexedDB
- **BPMN Workflows** - Visualize screen relationships
- **Multi-Subsystem Management** - Organize large codebases
- **Local-First** - IndexedDB for client-side storage

---

## 📚 Documentation Index

### Core Features ⭐ NEW

1. **[AI Function Calling System](./AI_FUNCTION_CALLING_SYSTEM.md)** ⭐ UPDATED
    - Native function calling for OpenAI/Claude
    - Multi-turn tool execution with feedback loop
    - Transparent UI for tool calls and results
    - **Status**: ✅ Implemented

2. **[Unified Function Calling](./UNIFIED_FUNCTION_CALLING.md)** ⭐ **NEW**
    - **Add function calling to ANY LLM** (even without native support!)
    - Text-based adapter for Ollama, Hugging Face, custom APIs
    - Automatic format conversion and parsing
    - Works with local models, cloud APIs, proprietary systems
    - **Status**: ✅ Implemented

3. **[Function Calling Quick Start](./FUNCTION_CALLING_QUICK_START.md)** ⭐ **NEW**
    - 3-minute guide to add function calling
    - Copy-paste examples
    - Common issues and fixes
    - **Status**: ✅ Implemented

4. **[Custom LLM Integration](./CUSTOM_LLM_INTEGRATION.md)**
    - Integrate ANY LLM (OpenAI-compatible or not)
    - Step-by-step guides for different API formats
    - 5+ complete real-world examples
    - REST, GraphQL, WebSocket support
    - **Status**: ✅ Implemented

5. **[BPMN Workflow System](./BPMN_WORKFLOW_SYSTEM.md)**
    - Auto-generate workflows from screen relationships
    - Interactive BPMN diagrams
    - Export to `.bpmn` files
    - **Status**: ✅ Implemented

---

## 🚀 Quick Start

### AI Function Calling (Native Support)

```javascript
// For LLMs with native support (OpenAI, Claude)
import { OpenAIProvider } from "./services/ai/OpenAIProvider.js";

const provider = new OpenAIProvider({ apiKey: "your-key" });

const response = await provider.chat(
  [{ role: "user", content: "Find authentication projects" }],
  {
    tools: databaseQueryTool.getFunctionDefinitions(),
    tool_choice: "auto",
  },
);

// AI automatically calls search_projects() function!
```

### Universal Function Calling ⭐ NEW

```javascript
// For ANY LLM (even without native support!)
import { BaseProvider } from "./BaseProvider.js";
import { TextBasedFunctionCallingAdapter } from "./FunctionCallingAdapter.js";

export class YourProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "your-llm",
      name: "Your LLM",
      capabilities: { chat: true, tools: false }, // ← No native support
      ...config,
    });

    // ✨ Add function calling via adapter
    this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this);
    this.capabilities.tools = true; // ✅ Now supports function calling!
  }

  async chat(messages, options = {}) {
    // Adapter handles everything automatically!
    if (options.tools) {
      const preparedTools = this.functionCallingAdapter.prepareTools(
        options.tools,
      );
      messages = this.functionCallingAdapter.injectToolsIntoMessages(
        messages,
        preparedTools,
      );
    }

    const response = await this.callYourAPI(messages);

    if (options.tools) {
      const toolCalls = this.functionCallingAdapter.extractToolCalls(response);
      if (toolCalls)
        return { role: "assistant", content: "", tool_calls: toolCalls };
    }

    return { role: "assistant", content: response.text };
  }
}
```

**Works with:**

- ✅ Ollama (local models)
- ✅ LM Studio
- ✅ Hugging Face
- ✅ Any custom API
- ✅ ANY format (REST, GraphQL, WebSocket)

### AI Database Queries

```javascript
// Ask AI about your data
"Show me all projects in the Authentication subsystem";
"What screens are in the Login project?";
"Which screens have Java files?";

// AI automatically:
// 1. Calls appropriate database functions
// 2. Gets real data from IndexedDB
// 3. Provides accurate answers with context
```

### BPMN Workflow

```javascript
// Navigate to Project Detail
Projects → Select Project → Workflow Tab

// Generate workflows
Click "Generate" → System analyzes screens → Creates workflows

// View diagram
Click workflow in list → See BPMN diagram → Export if needed
```

### Custom LLM Integration

```javascript
// Integrate your own LLM in 3 steps:

// 1. Create provider file
class YourProvider extends BaseProvider {
  async chat(messages, options) {
    // Convert format → Call your API → Return standard format
  }
}

// 2. Register provider
providerRegistry.register(new YourProvider());

// 3. Use it!
// Works with: Ollama, LM Studio, Cohere, Together AI, or ANY custom API!
```

---

## 📖 Documentation Structure

```
docs/
├── README.md                              # This file
│
├── AI Function Calling (3 files) ⭐ NEW
│   ├── AI_FUNCTION_CALLING_SYSTEM.md      # Native function calling (OpenAI/Claude)
│   ├── UNIFIED_FUNCTION_CALLING.md        # Universal function calling (ANY LLM)
│   └── FUNCTION_CALLING_QUICK_START.md    # Quick 3-minute guide
│
├── LLM Integration
│   ├── CUSTOM_LLM_INTEGRATION.md          # Integrate any LLM
│   └── IMPLEMENTATION_SUMMARY.md          # Implementation details
│
├── Features
│   └── BPMN_WORKFLOW_SYSTEM.md            # Workflow generation
│
└── Quick References
    └── QUICK_ANSWER_CUSTOM_LLM.md         # Can I use non-OpenAI LLMs?
```

---

## 🎯 Feature Overview

### 1. AI Function Calling (Native) ⭐ UPDATED

**What it does:**

- LLMs with native support (OpenAI, Claude) can call tools directly
- Multi-turn execution: AI calls tools → Gets results → Decides next step
- Transparent UI showing tool calls and results
- Works out-of-the-box with supported providers

**Key Benefits:**

- ✅ **Accurate**: LLM explicitly requests specific data
- ✅ **Transparent**: User sees what data AI is accessing
- ✅ **Flexible**: AI can call multiple tools in sequence
- ✅ **Reliable**: Built-in to provider's API

**Supported Providers:**

- OpenAI (GPT-4, GPT-3.5)
- Claude (Claude 3 family)
- Any OpenAI-compatible API with tools support

**Read more**: [AI_FUNCTION_CALLING_SYSTEM.md](./AI_FUNCTION_CALLING_SYSTEM.md)

---

### 2. Unified Function Calling ⭐ **NEW FEATURE**

**What it does:**

- **Adds function calling to ANY LLM**, even without native support!
- Text-based adapter parses tool calls from LLM's text response
- Works with local models (Ollama, LM Studio)
- Works with cloud APIs (Hugging Face, Cohere, custom APIs)
- Unified interface - same code for all LLMs

**How it works:**

```
┌─────────────────────────────────────────┐
│  LLM (Any Type)                         │
├─────────────────────────────────────────┤
│                                          │
│  Has Native Support?                    │
│  ├─ YES → NativeFunctionCallingAdapter │
│  └─ NO  → TextBasedAdapter             │
│            (Parse from text)            │
│                                          │
└─────────────────────────────────────────┘
```

**Key Benefits:**

- 🔌 **Universal**: Works with ANY LLM
- 🏠 **Local Models**: Run privately with Ollama
- 💰 **Cost-Effective**: Use cheaper providers
- 🔧 **Flexible**: Custom formats supported
- 📝 **Easy**: 3-minute integration

**Supported LLMs:**

| Type              | Examples             | Integration Time |
|-------------------|----------------------|------------------|
| OpenAI-compatible | Ollama, LM Studio    | 5 minutes        |
| Cloud APIs        | Hugging Face, Cohere | 30 minutes       |
| Custom APIs       | Your proprietary API | 1-2 hours        |

**Use Cases:**

- Run models locally for privacy
- Use cheaper cloud alternatives
- Integrate company's proprietary LLM
- Test multiple providers easily
- Switch providers without code changes

**Read more**:

- [UNIFIED_FUNCTION_CALLING.md](./UNIFIED_FUNCTION_CALLING.md) - Complete guide
- [FUNCTION_CALLING_QUICK_START.md](./FUNCTION_CALLING_QUICK_START.md) - Quick start

---

### 3. Custom LLM Integration

**What it does:**

- Enables integration of ANY LLM regardless of API format
- Provides adapter pattern for format conversion
- Supports REST, GraphQL, WebSocket protocols
- Works with local models and cloud APIs

**Key Benefits:**

- 🔌 **Universal**: Works with ANY LLM
- 🏠 **Local Support**: Run models locally
- 🌐 **Cloud Support**: Use any cloud provider
- 🔧 **Custom APIs**: Integrate proprietary systems
- 📝 **Well Documented**: 5+ complete examples

**Integration Types:**

| Type              | Difficulty  | Time    | Example           |
|-------------------|-------------|---------|-------------------|
| OpenAI-compatible | 🟢 Easy     | 5 min   | Ollama, LM Studio |
| Similar format    | 🟡 Medium   | 30 min  | Cohere, AI21      |
| Different format  | 🔴 Advanced | 1-2 hrs | Custom APIs       |

**Read more**: [CUSTOM_LLM_INTEGRATION.md](./CUSTOM_LLM_INTEGRATION.md)

---

### 4. BPMN Workflow System

**What it does:**

- Auto-generates workflows from screen relationships
- Analyzes events (navigate, branch) between screens
- Creates visual BPMN diagrams
- Interactive viewer with zoom/pan
- Export to `.bpmn` files

**Key Benefits:**

- ✅ **Automatic**: No manual workflow creation
- ✅ **Visual**: Easy to understand system flow
- ✅ **Interactive**: Zoom, pan, explore
- ✅ **Exportable**: Share with stakeholders

**Use Cases:**

- Onboard new developers
- Document system flows
- Analyze screen relationships
- Export for presentations
- Share with stakeholders

**Read more**: [BPMN_WORKFLOW_SYSTEM.md](./BPMN_WORKFLOW_SYSTEM.md)

---

## 🛠 Technical Architecture

### High-Level System Design

```
┌──────────────────────────────────────────────────────────────┐
│                      User Interface                           │
│  (AI Panel, Project Detail, Settings, Dashboard)             │
└──────────────────┬───────────────────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────────────────┐
│                   AI System                                   │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ AIPanel                                                  │ │
│  │  - handleFunctionCallingMode() ⭐                       │ │
│  │  - handleContextInjectionMode()                         │ │
│  │  - displayToolCalls()                                    │ │
│  │  - displayToolResults()                                  │ │
│  └─────────────────────────────────────────────────────────┘ │
│                           │                                   │
│  ┌────────────────────────▼────────────────────────────────┐ │
│  │ ProviderRegistry                                         │ │
│  │  - Multiple AI providers                                 │ │
│  │  - Dynamic provider switching                            │ │
│  └──────────────────┬──────────────────┬───────────────────┘ │
│                     │                  │                      │
│  ┌──────────────────▼─────┐ ┌─────────▼─────────────────┐   │
│  │ Native Support         │ │ Text-Based Adapter ⭐ NEW│   │
│  │ - OpenAI               │ │ - Ollama                  │   │
│  │ - Claude               │ │ - Hugging Face            │   │
│  │ - OpenAI-compatible    │ │ - Custom APIs             │   │
│  └────────────────────────┘ └───────────────────────────┘   │
└──────────────────┬───────────────────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────────────────┐
│               Function Calling Layer ⭐ NEW                  │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ FunctionCallingAdapter                                   │ │
│  │  - NativeFunctionCallingAdapter                         │ │
│  │  - TextBasedFunctionCallingAdapter                      │ │
│  │  - Custom adapters via registry                         │ │
│  └─────────────────────────────────────────────────────────┘ │
│                           │                                   │
│  ┌────────────────────────▼────────────────────────────────┐ │
│  │ ToolExecutor                                             │ │
│  │  - executeToolCall()                                     │ │
│  │  - executeToolCalls()                                    │ │
│  │  - formatToolCallForDisplay()                            │ │
│  └─────────────────────────────────────────────────────────┘ │
│                           │                                   │
│  ┌────────────────────────▼────────────────────────────────┐ │
│  │ DatabaseQueryTool                                        │ │
│  │  - getFunctionDefinitions()                              │ │
│  │  - executeFunction()                                     │ │
│  │  - 10+ database query functions                          │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────┬───────────────────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────────────────┐
│                  Data Layer                                   │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ DatabaseManager (IndexedDB)                              │ │
│  │  - subsystems, projects, screens                         │ │
│  │  - events, source files, database tables                 │ │
│  │  - chatHistory, appSettings                              │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

### Key Innovation: Unified Function Calling ⭐

**Before:**

```
OpenAI → ✅ Function Calling
Claude → ✅ Function Calling (different format)
Ollama → ❌ No function calling
Custom API → ❌ No function calling
```

**After (with Unified System):**

```
OpenAI → ✅ Native adapter
Claude → ✅ Native adapter (format converted)
Ollama → ✅ Text-based adapter (parses from text)
Custom API → ✅ Text-based adapter (works with ANY format)

ALL providers now support function calling! 🎉
```

---

## 💻 Development Workflow

### Adding a New LLM Provider

#### Option 1: With Native Function Calling

```javascript
// Example: Your LLM has built-in tool support
import { BaseProvider } from "./BaseProvider.js";
import { NativeFunctionCallingAdapter } from "./FunctionCallingAdapter.js";

export class YourProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "your-provider",
      name: "Your Provider",
      capabilities: { chat: true, tools: true }, // Native support
      ...config,
    });

    this.functionCallingAdapter = new NativeFunctionCallingAdapter(this, {
      toolFormat: "openai", // or "claude"
    });
  }

  async chat(messages, options = {}) {
    // Your implementation using native tools API
  }
}
```

#### Option 2: Without Native Function Calling ⭐ NEW

```javascript
// Example: Your LLM doesn't have tool support
import { BaseProvider } from "./BaseProvider.js";
import { TextBasedFunctionCallingAdapter } from "./FunctionCallingAdapter.js";

export class YourProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "your-provider",
      name: "Your Provider",
      capabilities: { chat: true, tools: false }, // No native support
      ...config,
    });

    // ✨ Add function calling via adapter
    this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this, {
      format: "json", // or "xml", "custom"
    });

    this.capabilities.tools = true; // Now supports tools!
  }

  async chat(messages, options = {}) {
    // Adapter handles tool injection and extraction
    if (options.tools) {
      const preparedTools = this.functionCallingAdapter.prepareTools(
        options.tools,
      );
      messages = this.functionCallingAdapter.injectToolsIntoMessages(
        messages,
        preparedTools,
      );
    }

    const response = await this.callYourAPI(messages);

    if (options.tools) {
      const toolCalls = this.functionCallingAdapter.extractToolCalls(response);
      if (toolCalls)
        return { role: "assistant", content: "", tool_calls: toolCalls };
    }

    return { role: "assistant", content: response.text };
  }
}
```

### Adding Custom Database Functions

```javascript
// In DatabaseQueryTool.js
class DatabaseQueryTool {
  getFunctionDefinitions() {
    return [
      // ... existing functions
      {
        name: "your_new_function",
        description: "Description of what it does",
        parameters: {
          type: "object",
          properties: {
            param1: { type: "string", description: "..." },
            param2: { type: "number", description: "..." },
          },
          required: ["param1"],
        },
      },
    ];
  }

  async executeFunction(functionName, args) {
    switch (functionName) {
      case "your_new_function":
        return await this.yourNewFunction(args);
      // ... other cases
    }
  }

  async yourNewFunction({ param1, param2 }) {
    // Your implementation
    const data = await databaseManager.yourQuery(param1, param2);
    return { success: true, data };
  }
}
```

---

## 🗺 Roadmap

### ✅ Completed (v1.2.0)

- [x] AI Function Calling System (Native support)
- [x] **Unified Function Calling** (ANY LLM support) ⭐ NEW
- [x] **Text-Based Function Calling Adapter** ⭐ NEW
- [x] Custom LLM Integration Guide
- [x] BPMN Workflow Generation
- [x] Multi-provider support
- [x] Database Query Tool
- [x] Chat history management
- [x] Markdown & Mermaid rendering

### 🚧 In Progress

- [ ] Streaming support for text-based adapters
- [ ] Vision support in function calling
- [ ] Multi-modal tool calls (image + text)

### 📋 Planned (v1.3.0)

- [ ] Voice input/output
- [ ] Collaborative features (multi-user)
- [ ] Advanced workflow editing
- [ ] Plugin system for custom tools
- [ ] Real-time provider performance metrics

### 💡 Future Ideas

- [ ] GraphQL API for database queries
- [ ] WebSocket for real-time updates
- [ ] Mobile app version
- [ ] Docker deployment
- [ ] Cloud sync option

---

## 📝 Changelog

### v1.2.0 (November 6, 2025) ⭐ NEW

**Major Features:**

- ✨ **Unified Function Calling System** - Add function calling to ANY LLM!
    - `FunctionCallingAdapter.js` with base class and two implementations
    - `NativeFunctionCallingAdapter` for OpenAI/Claude
    - `TextBasedFunctionCallingAdapter` for all other LLMs
    - Auto-parsing of JSON, XML, and custom formats
    - Works with local models (Ollama), cloud APIs, custom endpoints

- 📚 **New Documentation** (3,000+ lines)
    - `UNIFIED_FUNCTION_CALLING.md` - Complete system guide
    - `FUNCTION_CALLING_QUICK_START.md` - 3-minute quick start
    - Updated `CUSTOM_LLM_INTEGRATION.md` with adapter examples

- 🔧 **BaseProvider Updates**
    - Added `functionCallingAdapter` property
    - Added `supportsFunctionCalling()` method
    - Added `getFunctionCallingType()` method
    - Added adapter management methods

**Files Added:**

- `public/js/services/ai/FunctionCallingAdapter.js` (750+ lines)
- `docs/UNIFIED_FUNCTION_CALLING.md` (1,500+ lines)
- `docs/FUNCTION_CALLING_QUICK_START.md` (400+ lines)

**Files Updated:**

- `public/js/services/ai/BaseProvider.js`
- `docs/CUSTOM_LLM_INTEGRATION.md`
- `docs/QUICK_ANSWER_CUSTOM_LLM.md`
- `docs/README.md`

### v1.1.0 (November 5, 2025)

**Major Features:**

- ✨ Native AI Function Calling (OpenAI & Claude)
- 🔧 Tool Executor System
- 📊 Database Query Tool with 10+ functions
- 🎨 Transparent UI for tool calls
- 💬 Multi-turn function calling loop

**Files Added:**

- `AI_FUNCTION_CALLING_SYSTEM.md` (2,000+ lines)
- `ToolExecutor.js`
- Updated `OpenAIProvider.js` and `ClaudeProvider.js`

### v1.0.0 (October 2025)

- Initial release
- Project management system
- AI chat integration
- BPMN workflow generation
- IndexedDB storage
- Multiple AI providers

---

## Quick Links

### AI Function Calling

- [AI Function Calling System](./AI_FUNCTION_CALLING_SYSTEM.md) - Native support (OpenAI/Claude)
- [Unified Function Calling](./UNIFIED_FUNCTION_CALLING.md) ⭐ NEW - Universal support (ANY LLM)
- [Quick Start Guide](./FUNCTION_CALLING_QUICK_START.md) ⭐ NEW - 3-minute integration

### LLM Integration

- [Custom LLM Integration](./CUSTOM_LLM_INTEGRATION.md) - Integrate any LLM
- [Quick Answer](./QUICK_ANSWER_CUSTOM_LLM.md) - Can I use non-OpenAI LLMs?

### Features

- [BPMN Workflow System](./BPMN_WORKFLOW_SYSTEM.md) - Workflow generation
- [Implementation Summary](./IMPLEMENTATION_SUMMARY.md) - Technical details

---

## 🎓 Learning Path

### Beginners

1. Start with [Quick Start](#-quick-start)
2. Read [FUNCTION_CALLING_QUICK_START.md](./FUNCTION_CALLING_QUICK_START.md)
3. Try the examples
4. Explore the UI

### Integration Developers

1. Read [UNIFIED_FUNCTION_CALLING.md](./UNIFIED_FUNCTION_CALLING.md)
2. Choose your LLM type
3. Follow the relevant example
4. Test with real tools
5. Deploy!

### Advanced Users

1. Read all documentation
2. Understand architecture
3. Create custom adapters
4. Extend database functions
5. Contribute back!

---

## 🤝 Contributing

We welcome contributions! Areas of interest:

- New AI provider integrations
- Custom function calling adapters
- Additional database query functions
- Documentation improvements
- Bug fixes and optimizations

---

## 📄 License

MIT License - See LICENSE file for details

---

**Last Updated**: November 6, 2025  
**Version**: 1.2.0  
**Status**: ✅ Production Ready

---

**Happy coding! 🚀**

**Any LLM can now intelligently query your database!** 🎉
