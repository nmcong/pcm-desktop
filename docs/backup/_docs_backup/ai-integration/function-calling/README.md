# PCM WebApp Documentation

Welcome to the PCM (Project Control & Management) WebApp documentation! This directory contains comprehensive guides for
all major features and systems.

## 📚 Documentation Index

### Core Features

1. **[AI Function Calling System](./AI_FUNCTION_CALLING_SYSTEM.md)** ⭐ NEW
    - OpenAI/Claude native function calling
    - AI decides when and how to query database
    - Multi-turn tool execution workflows
    - Transparent UI showing tool calls
    - **Status**: ✅ Implemented

2. **[AI Database Query System](./AI_DATABASE_QUERY_SYSTEM.md)**
    - Enable AI to query IndexedDB without backend
    - Auto context injection for smart responses
    - 7 query functions for comprehensive data access
    - Privacy-first, client-side only
    - **Status**: ✅ Implemented

3. **[BPMN Workflow System](./BPMN_WORKFLOW_SYSTEM.md)**
    - Auto-generate BPMN 2.0 compliant diagrams
    - Analyze screen relationships and events
    - Interactive viewer with zoom/pan
    - Export to `.bpmn` files
    - **Status**: ✅ Implemented

4. **[Custom LLM Integration](./CUSTOM_LLM_INTEGRATION.md)** ⭐ NEW
    - Integrate ANY LLM (OpenAI-compatible or not)
    - Step-by-step guides for different API formats
    - 5+ complete real-world examples
    - Function calling for custom LLMs
    - **Status**: ✅ Implemented

5. **[API Development Standards](../../../docs/vibytes-framework/API-STANDARD.md)**
    - Response format guidelines
    - Controller standards
    - Exception handling
    - Validation patterns
    - **Status**: 📘 Reference

---

## 🚀 Quick Start

### AI Function Calling ⭐ NEW

```javascript
// Enable in AI Settings (OpenAI or Claude only)
Settings → Conversation Settings → ☑ Enable function calling

// AI will automatically decide when to query:
"Show me authentication projects"        → AI calls search_projects()
"Compare Login and SSO projects"         → AI calls get_project_details() x2
"What are recent changes in the system?" → AI calls appropriate functions

// Watch tool execution in real-time!
```

### AI Database Query

```javascript
// Enable in AI Settings (All providers)
Settings → Conversation Settings → ☑ Enable database access

// Then ask AI questions like:
"Show me all projects in the Authentication subsystem"
"What screens are in the Login project?"
"Which screens use Java files?"
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

### Custom LLM Integration ⭐ NEW

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
├── README.md                          # This file
├── AI_FUNCTION_CALLING_SYSTEM.md      # ⭐ NEW: Native function calling
├── CUSTOM_LLM_INTEGRATION.md          # ⭐ NEW: Integrate any LLM
├── AI_DATABASE_QUERY_SYSTEM.md        # AI IndexedDB integration
├── BPMN_WORKFLOW_SYSTEM.md            # Workflow generation & visualization
├── IMPLEMENTATION_SUMMARY.md          # Function calling implementation summary
│
├── vibytes-framework/
│   ├── API-STANDARD.md                # Backend API standards
│   └── ...
│
└── guides/
    └── ...
```

---

## 🎯 Feature Overview

### 1. AI Function Calling System ⭐ NEW

**What it does:**

- Enables OpenAI/Claude to natively call database functions
- AI intelligently decides when and which tools to use
- Supports multi-turn conversations with tool execution
- Displays tool calls transparently in the UI

**Key Benefits:**

- 🤖 **AI-Driven**: AI chooses when to query, not keyword matching
- ⚡ **Efficient**: Only queries what's needed, saves tokens
- 🔄 **Multi-Turn**: Complex workflows with multiple tool calls
- 👁️ **Transparent**: See exactly what AI is doing
- 🎯 **Precise**: AI provides exact parameters

**Use Cases:**

- Complex comparative analysis (e.g., "Compare Project A with B")
- Multi-step research (e.g., "Find JSP screens in auth projects, show events")
- Smart exploration (AI decides what data is relevant)
- Workflow analysis across multiple screens/projects

**Comparison with Context Injection:**

| Feature          | Function Calling   | Context Injection         |
|------------------|--------------------|---------------------------|
| Provider Support | OpenAI, Claude     | All providers             |
| Precision        | High (AI decides)  | Medium (keyword matching) |
| Token Efficiency | Excellent          | Good                      |
| Complexity       | Multi-turn         | Single-turn               |
| Transparency     | Full (shows tools) | Hidden                    |

**Read more**: [AI_FUNCTION_CALLING_SYSTEM.md](./AI_FUNCTION_CALLING_SYSTEM.md)

---

### 2. AI Database Query System

**What it does:**

- Allows AI to directly query your local IndexedDB
- Automatically injects relevant data into AI context
- Provides 7 specialized query functions
- Works without any backend server

**Key Benefits:**

- 🚀 **Zero Backend**: Everything runs in browser
- 🔒 **Privacy First**: Data never leaves your device
- ⚡ **Real-time**: Instant responses with fresh data
- 🤖 **Smart Context**: Auto-detects user intent

**Use Cases:**

- Project discovery and exploration
- Screen analysis and navigation
- Workflow understanding
- Technology stack search
- Architecture overview

**Read more**: [AI_DATABASE_QUERY_SYSTEM.md](./AI_DATABASE_QUERY_SYSTEM.md)

---

### 2. BPMN Workflow System

**What it does:**

- Automatically generates workflows from screen events
- Creates BPMN 2.0 compliant diagrams
- Provides interactive visualization
- Exports to standard `.bpmn` format

**Key Benefits:**

- 📊 **Auto Generation**: No manual diagram creation
- 🎨 **Professional**: Industry-standard BPMN 2.0
- 🔄 **Interactive**: Pan, zoom, explore
- 💾 **Exportable**: Use in other BPMN tools

**Use Cases:**

- Visualize user journeys
- Document system flows
- Analyze screen relationships
- Export for presentations
- Share with stakeholders

**Read more**: [BPMN_WORKFLOW_SYSTEM.md](./BPMN_WORKFLOW_SYSTEM.md)

---

### 4. Custom LLM Integration ⭐ NEW

**What it does:**

- Enables integration of ANY LLM regardless of API format
- Provides adapter pattern for format conversion
- Supports local models (Ollama, LM Studio)
- Supports cloud APIs (Cohere, Together AI, Hugging Face)
- Works with custom/proprietary APIs

**Key Benefits:**

- 🔌 **Universal**: Works with ANY LLM
- 🏠 **Local Support**: Run models locally (Ollama, vLLM)
- 🌐 **Cloud Support**: Use any cloud provider
- 🔧 **Custom APIs**: Integrate proprietary systems
- 📝 **Well Documented**: 5+ complete examples

**Integration Types:**

| Type              | Difficulty  | Time    | Example           |
|-------------------|-------------|---------|-------------------|
| OpenAI-compatible | 🟢 Easy     | 5 min   | Ollama, LM Studio |
| Similar format    | 🟡 Medium   | 30 min  | Cohere, AI21      |
| Different format  | 🔴 Advanced | 1-2 hrs | Custom APIs       |

**Use Cases:**

- Run models locally for privacy
- Use cheaper cloud alternatives
- Integrate company's proprietary LLM
- Test multiple providers easily
- Switch providers without code changes

**Read more**: [CUSTOM_LLM_INTEGRATION.md](./CUSTOM_LLM_INTEGRATION.md)

---

## 🛠 Technical Architecture

### High-Level System Design

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend (React)                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌───────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  AIPanel  │  │ Workflow │  │  Pages   │  │Components│  │
│  └─────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  │
│        │             │               │             │         │
│        └─────────────┴───────────────┴─────────────┘         │
│                          │                                    │
├──────────────────────────┼────────────────────────────────────┤
│                    Services Layer                             │
├───────────────────────────────────────────────────────────────┤
│                          │                                    │
│  ┌───────────────┐  ┌───┴──────────┐  ┌──────────────┐     │
│  │DatabaseQuery  │  │  Database    │  │BpmnEngine/   │     │
│  │Tool           │  │  Manager     │  │Viewer        │     │
│  └───────┬───────┘  └───────┬──────┘  └──────┬───────┘     │
│          │                   │                 │              │
│          └───────────────────┼─────────────────┘              │
│                              │                                │
├──────────────────────────────┼────────────────────────────────┤
│                       IndexedDB                               │
│                                                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │Projects  │  │ Screens  │  │Subsystems│  │Workflows │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Data Flow

### AI Query Flow

```
User Question
    ↓
AIPanel detects keywords
    ↓
DatabaseQueryTool executes query
    ↓
IndexedDB returns data
    ↓
Context injected into message
    ↓
AI Provider receives enriched message
    ↓
AI generates data-driven response
```

### Workflow Generation Flow

```
User clicks "Generate"
    ↓
ScreenWorkflowManager loads screens
    ↓
Analyzes events (BFS algorithm)
    ↓
Builds workflow paths
    ↓
BpmnEngine converts to BPMN XML
    ↓
BpmnViewer renders diagram
    ↓
User interacts with visualization
```

---

## 🎓 Learning Path

### For New Users

1. **Start Here**
    - Read this README
    - Explore the application UI
    - Try asking AI simple questions

2. **Deep Dive**
    - [AI Database Query System](./AI_DATABASE_QUERY_SYSTEM.md)
    - Understand how AI accesses data
    - Learn about available query functions

3. **Advanced Features**
    - [BPMN Workflow System](./BPMN_WORKFLOW_SYSTEM.md)
    - Learn workflow generation
    - Master BPMN visualization

### For Developers

1. **Architecture**
    - Review technical implementation sections
    - Study component interactions
    - Understand data flow

2. **Extension**
    - Read "Extending the System" sections
    - Learn how to add new features
    - Follow best practices

3. **Integration**
    - See integration examples
    - Understand API contracts
    - Follow coding standards

---

## 🔧 Configuration

### AI Database Access

**Enable/Disable:**

```
AI Panel → Settings → ☑ Enable database access
```

**Storage:**

- Location: `localStorage`
- Key: `ai-database-access`
- Default: `true`

### BPMN Workflow

**Configuration:**

- Max workflow depth: `10` levels
- Max workflow steps: `20` steps
- Layout: Vertical, 120px spacing

**Library:**

- bpmn-js: `./public/vendor/bpmn-js/bpmn-js.js`
- Auto-loaded on demand

---

## 🧪 Testing

### AI Database Query

**Test Queries:**

```javascript
// 1. Project search
"Show me all authentication projects";

// 2. Screen search
"What screens are in project X?";

// 3. Event analysis
"What events are on the Dashboard screen?";

// 4. Technology search
"Which screens use JSP files?";

// 5. Subsystem overview
"List all subsystems";
```

**Expected Results:**

- AI responds with accurate data from IndexedDB
- Console shows "📊 Database context injected"
- Data appears in markdown format

---

### BPMN Workflow

**Test Workflow:**

1. Create project with 3+ screens
2. Add navigation events between screens
3. Go to Workflow tab
4. Click "Generate"
5. Verify workflows created
6. Click workflow to view diagram
7. Test zoom, pan, export

**Expected Results:**

- Workflows generated successfully
- BPMN diagram renders correctly
- All controls work (zoom, export)
- Export downloads `.bpmn` file

---

## 🐛 Troubleshooting

### Common Issues

**AI not accessing database?**

- ✓ Check if database access is enabled in settings
- ✓ Verify IndexedDB has data
- ✓ Check browser console for errors

**BPMN not loading?**

- ✓ Verify bpmn-js library path
- ✓ Check browser console for 404 errors
- ✓ Ensure `./public/vendor/bpmn-js/` exists

**Workflows not generating?**

- ✓ Ensure screens have navigation events
- ✓ Check for circular references
- ✓ Verify event data structure

**Performance issues?**

- ✓ Limit workflow depth and steps
- ✓ Reduce injected data size
- ✓ Clear old data from IndexedDB

---

## 📚 Additional Resources

### External Documentation

- **BPMN 2.0 Spec**: https://www.omg.org/spec/BPMN/2.0/
- **bpmn-js**: https://bpmn.io/toolkit/bpmn-js/
- **IndexedDB API**: https://developer.mozilla.org/en-US/docs/Web/API/IndexedDB_API

### Related Docs

- **Backend Standards**: [API-STANDARD.md](../../../docs/vibytes-framework/API-STANDARD.md)
- **React Best Practices**: Check workspace rules
- **TypeScript Guide**: Check workspace rules

---

## 🤝 Contributing

### Adding Documentation

1. Create new `.md` file in `docs/`
2. Follow existing structure
3. Include:
    - Overview
    - Architecture
    - Usage examples
    - Troubleshooting
    - References

### Updating Docs

1. Keep docs in sync with code
2. Update version and last updated date
3. Add examples for new features
4. Document breaking changes

---

## 📝 Changelog

### Version 1.1.0 (November 6, 2025)

**Added:**

- ✨ **AI Function Calling System** (OpenAI, Claude)
- 🔧 Tool execution loop with multi-turn support
- 👁️ Transparent UI for tool calls and results
- 🎛️ Function calling toggle in settings
- 📚 Comprehensive documentation for function calling

**Changed:**

- 🤖 AI Panel now supports both function calling and context injection modes
- ⚡ Improved token efficiency with function calling
- 🎨 Enhanced tool execution UI with real-time display

### Version 1.0.0 (November 6, 2025)

**Added:**

- ✨ AI Database Query System
- ✨ BPMN Workflow System
- 📚 Comprehensive documentation
- 🎨 Enhanced UI/UX
- 🔧 Settings for database access

**Changed:**

- 🎨 Workflow tab moved to project level
- 📐 Workflow panel layout (300px fixed left)
- 🚀 Performance optimizations

**Fixed:**

- 🐛 BPMN library loading path
- 🐛 Workflow generation cycles
- 🐛 Context injection edge cases

---

## 📞 Support

### Getting Help

- 📖 Read relevant documentation
- 🔍 Check troubleshooting section
- 💬 Ask AI assistant (with database access enabled!)
- 👥 Contact development team

### Reporting Issues

When reporting issues, include:

- Feature/system affected
- Steps to reproduce
- Expected vs actual behavior
- Browser console errors
- Screenshots if applicable

---

## 🎯 Roadmap

### Recently Completed ✅

**AI Function Calling:**

- [x] OpenAI function calling support
- [x] Claude function calling support
- [x] Multi-turn tool execution
- [x] Transparent UI for tool calls
- [x] Tool execution loop with iteration limit

### Upcoming Features

**AI Database Query:**

- [ ] Additional providers with function calling (Gemini, Hugging Face)
- [ ] Semantic search with embeddings
- [ ] Query history and analytics
- [ ] More specialized queries
- [ ] Performance analytics dashboard

**BPMN Workflow:**

- [ ] Exclusive/Parallel gateways
- [ ] Interactive diagram editing
- [ ] Multiple export formats (SVG, PNG, PDF)
- [ ] Workflow validation
- [ ] Collaboration features

**General:**

- [ ] Performance monitoring
- [ ] User analytics
- [ ] Offline support
- [ ] Mobile optimization

---

## 📄 License

This project is proprietary software. All rights reserved.

---

## 👥 Authors

**PCM Development Team**

- Architecture & Design
- Implementation
- Documentation
- Maintenance

---

**Last Updated**: November 6, 2025  
**Version**: 1.1.0  
**Status**: ✅ Production Ready

---

## Quick Links

### AI Features

- [AI Function Calling System](./AI_FUNCTION_CALLING_SYSTEM.md) ⭐ NEW - Native function calling for OpenAI/Claude
- [Custom LLM Integration](./CUSTOM_LLM_INTEGRATION.md) ⭐ NEW - Integrate ANY LLM (local or cloud)
- [AI Database Query System](./AI_DATABASE_QUERY_SYSTEM.md) - Complete guide to AI database integration

### Workflow & Architecture

- [BPMN Workflow System](./BPMN_WORKFLOW_SYSTEM.md) - Complete guide to workflow generation
- [Implementation Summary](./IMPLEMENTATION_SUMMARY.md) - Function calling implementation details
- [API Standards](../../../docs/vibytes-framework/API-STANDARD.md) - Backend API development guide

**Happy coding! 🚀**
