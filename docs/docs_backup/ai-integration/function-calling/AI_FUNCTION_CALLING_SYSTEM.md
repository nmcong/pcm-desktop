# AI Function Calling System

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [How It Works](#how-it-works)
- [Source Code Reference](#source-code-reference)
- [Provider Implementation](#provider-implementation)
- [Tool Execution Flow](#tool-execution-flow)
- [User Interface](#user-interface)
- [Configuration](#configuration)
- [Extending the System](#extending-the-system)
- [Comparison: Function Calling vs Context Injection](#comparison-function-calling-vs-context-injection)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)
- [Examples](#examples)

---

## Overview

The AI Function Calling System enables AI providers (OpenAI, Claude) to intelligently decide when and how to query your IndexedDB database. Unlike the simpler context injection approach, function calling allows the AI to:

- **Decide** when database access is needed
- **Choose** which specific function to call
- **Provide** precise parameters
- **Handle** multi-turn tool execution
- **Optimize** token usage

### Key Features

- **🤖 AI-Driven**: AI decides when to query
- **🔧 7 Available Tools**: Full database access
- **🔄 Multi-Turn**: Support for complex workflows
- **📊 Transparent**: UI shows all tool calls
- **⚡ Efficient**: Only queries when needed
- **🔌 Extensible**: Easy to add new tools

### Supported Providers

| Provider               | Support | Notes                      |
| ---------------------- | ------- | -------------------------- |
| OpenAI (GPT-4, GPT-4o) | ✅ Full | Native function calling    |
| Claude (3.5 Sonnet+)   | ✅ Full | Converted to Claude format |
| Mock Provider          | ❌ No   | No tools capability        |
| ViByte Cloud           | ❌ No   | No tools capability        |
| Gemini                 | ❌ No   | Not implemented yet        |
| Hugging Face           | ❌ No   | Not implemented yet        |

---

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                      User Query                              │
│          "Show me authentication projects"                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    AIPanel.js                                │
│  - Check if function calling enabled & supported             │
│  - Prepare conversation history                              │
│  - Add tool definitions to request                           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│             OpenAIProvider / ClaudeProvider                  │
│  - Send chat request with tools                              │
│  - Receive response (may contain tool_calls)                 │
│  - Stream with tool accumulation                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  AI Response                                 │
│  - Option 1: Regular text response (no tools)                │
│  - Option 2: tool_calls + optional text                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼ (if tool_calls present)
┌─────────────────────────────────────────────────────────────┐
│                  ToolExecutor.js                             │
│  - Parse tool_calls                                          │
│  - Execute via DatabaseQueryTool                             │
│  - Format results                                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              Tool Results → AI                               │
│  - Send results back to AI                                   │
│  - AI generates final response with context                  │
│  - Display to user                                           │
└─────────────────────────────────────────────────────────────┘
```

### File Structure

```
apps/pcm-webapp/public/js/
├── services/
│   ├── DatabaseManager.js              # IndexedDB operations
│   ├── DatabaseQueryTool.js            # Query functions (shared)
│   ├── ToolExecutor.js                 # NEW: Execute tool calls
│   └── ai/
│       ├── BaseProvider.js             # Base class (tools capability)
│       ├── OpenAIProvider.js           # UPDATED: Function calling
│       ├── ClaudeProvider.js           # UPDATED: Function calling
│       └── ProviderRegistry.js
├── components/
│   └── AIPanel.js                      # UPDATED: Tool execution loop
└── utils/
    └── MarkdownRenderer.js
```

---

## How It Works

### Step-by-Step Flow

#### 1. User Sends Message

```javascript
// User types: "Show me all authentication projects"
// AIPanel.handleSendMessage() is called
```

#### 2. AIPanel Checks Mode

```javascript
// apps/pcm-webapp/public/js/components/AIPanel.js:456-465
const useFunctionCalling =
  this.enableFunctionCalling && provider.capabilities.tools;

if (useFunctionCalling) {
  await this.handleFunctionCallingMode(provider, userMessage);
} else {
  await this.handleContextInjectionMode(provider, userMessage);
}
```

#### 3. Prepare Tools Definitions

```javascript
// apps/pcm-webapp/public/js/components/AIPanel.js:486-494
const tools = databaseQueryTool.getFunctionDefinitions().map((func) => ({
  type: "function",
  function: {
    name: func.name,
    description: func.description,
    parameters: func.parameters,
  },
}));

// Result: 7 tools sent to AI
```

**Tools Sent to AI:**

```json
[
  {
    "type": "function",
    "function": {
      "name": "search_projects",
      "description": "Search for projects by name or description...",
      "parameters": {
        "type": "object",
        "properties": {
          "query": {
            "type": "string",
            "description": "Search query..."
          }
        },
        "required": ["query"]
      }
    }
  }
  // ... 6 more tools
]
```

#### 4. Send to AI Provider

```javascript
// For OpenAI
// apps/pcm-webapp/public/js/services/ai/OpenAIProvider.js:86-100
const requestBody = {
  model: options.model || this.defaultModel,
  messages: messages,
  max_completion_tokens: options.maxTokens || 2048,
  temperature: options.temperature || 1.0,
  stream: false,
  tools: options.tools, // ← Tools included
  tool_choice: "auto", // ← AI decides
};
```

#### 5. AI Decides to Call Tool

**AI Response:**

```json
{
  "role": "assistant",
  "content": null,
  "tool_calls": [
    {
      "id": "call_abc123",
      "type": "function",
      "function": {
        "name": "search_projects",
        "arguments": "{\"query\": \"authentication\"}"
      }
    }
  ]
}
```

#### 6. Execute Tool

```javascript
// apps/pcm-webapp/public/js/components/AIPanel.js:542-544
const toolResults = await toolExecutor.executeToolCalls(response.tool_calls);
```

**Tool Execution:**

```javascript
// apps/pcm-webapp/public/js/services/ToolExecutor.js:13-46
async executeToolCall(toolCall) {
  const functionName = toolCall.function.name;
  const args = JSON.parse(toolCall.function.arguments);

  console.log(`🔧 [ToolExecutor] Executing: ${functionName}`, args);

  // Execute via DatabaseQueryTool
  const result = await databaseQueryTool.executeFunction(functionName, args);

  return {
    tool_call_id: toolCall.id,
    role: "tool",
    name: functionName,
    content: JSON.stringify(result),
  };
}
```

#### 7. Display Tool Execution in UI

```javascript
// apps/pcm-webapp/public/js/components/AIPanel.js:770-815
displayToolCalls(toolCalls) {
  // Shows: "🛠️ Calling 1 tool(s)..."
  // Displays: function name + arguments
}

displayToolResults(toolResults) {
  // Shows: "✅ Tool results (1)"
  // Displays: success/failure + summary
}
```

**UI Example:**

```
┌─────────────────────────────────────┐
│ 🛠️ Calling 1 tool(s)...            │
│ ┌─────────────────────────────────┐ │
│ │ 🔍 Search Projects              │ │
│ │ { "query": "authentication" }   │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ✅ Tool results (1)                 │
│ ┌─────────────────────────────────┐ │
│ │ ✓ search_projects               │ │
│ │ Found 2 result(s)               │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

#### 8. Send Results Back to AI

```javascript
// apps/pcm-webapp/public/js/components/AIPanel.js:550-552
for (const toolResult of toolResults) {
  conversationMessages.push(toolResult);
}
// Loop continues → AI gets tool results → generates final response
```

**Conversation Now:**

```javascript
[
  { role: "user", content: "Show me authentication projects" },
  {
    role: "assistant",
    content: "",
    tool_calls: [{ id: "call_abc123", function: {...} }]
  },
  {
    role: "tool",
    tool_call_id: "call_abc123",
    name: "search_projects",
    content: '{"success":true,"count":2,"projects":[...]}'
  }
]
```

#### 9. AI Generates Final Response

```
I found 2 authentication projects:

1. **Login System** (LOGIN)
   - Subsystem: Authentication
   - Repository: https://github.com/org/login
   - Screens: 8 screens

2. **SSO Integration** (SSO)
   - Subsystem: Authentication
   - Repository: https://github.com/org/sso
   - Screens: 5 screens

Would you like more details about any of these?
```

---

## Source Code Reference

### Core Components

#### 1. AIPanel.js - Main Controller

**Location**: `apps/pcm-webapp/public/js/components/AIPanel.js`

**Key Methods:**

| Method                         | Lines   | Purpose                   |
| ------------------------------ | ------- | ------------------------- |
| `simulateAIResponse()`         | 439-472 | Entry point, decides mode |
| `handleFunctionCallingMode()`  | 477-587 | Tool execution loop       |
| `handleContextInjectionMode()` | 592-658 | Legacy context injection  |
| `streamResponseWithTools()`    | 663-765 | Stream with tool support  |
| `displayToolCalls()`           | 770-815 | UI for tool calls         |
| `displayToolResults()`         | 820-871 | UI for tool results       |

**Key Code Snippets:**

```javascript
// Line 456-465: Mode selection
const useFunctionCalling =
  this.enableFunctionCalling && provider.capabilities.tools;

if (useFunctionCalling) {
  await this.handleFunctionCallingMode(provider, userMessage);
} else {
  await this.handleContextInjectionMode(provider, userMessage);
}
```

```javascript
// Line 507-556: Tool execution loop
while (iteration < this.maxToolIterations) {
  iteration++;

  let response = await provider.chat(conversationMessages, options);

  if (response.tool_calls && response.tool_calls.length > 0) {
    // Execute tools
    const toolResults = await toolExecutor.executeToolCalls(
      response.tool_calls,
    );

    // Add to conversation
    conversationMessages.push(...toolResults);

    continue; // Loop back to AI
  }

  // No more tools, done
  break;
}
```

---

#### 2. ToolExecutor.js - Tool Execution

**Location**: `apps/pcm-webapp/public/js/services/ToolExecutor.js`

**Key Methods:**

| Method                         | Lines   | Purpose                |
| ------------------------------ | ------- | ---------------------- |
| `executeToolCall()`            | 13-46   | Execute single tool    |
| `executeToolCalls()`           | 51-60   | Execute multiple tools |
| `formatToolCallForDisplay()`   | 65-83   | Format for UI          |
| `getToolDisplayName()`         | 88-102  | Human-readable names   |
| `formatToolResultForDisplay()` | 107-137 | Format results for UI  |

**Key Code Snippet:**

```javascript
// Line 13-46: Execute tool call
async executeToolCall(toolCall) {
  try {
    const functionName = toolCall.function.name;
    const args = JSON.parse(toolCall.function.arguments);

    console.log(`🔧 [ToolExecutor] Executing: ${functionName}`, args);

    // Execute via DatabaseQueryTool
    const result = await databaseQueryTool.executeFunction(functionName, args);

    console.log(`✅ [ToolExecutor] Result:`, result);

    return {
      tool_call_id: toolCall.id,
      role: "tool",
      name: functionName,
      content: JSON.stringify(result),
    };
  } catch (error) {
    console.error(`❌ [ToolExecutor] Error executing ${toolCall.function.name}:`, error);

    return {
      tool_call_id: toolCall.id,
      role: "tool",
      name: toolCall.function.name,
      content: JSON.stringify({
        success: false,
        error: error.message,
      }),
    };
  }
}
```

---

#### 3. OpenAIProvider.js - OpenAI Function Calling

**Location**: `apps/pcm-webapp/public/js/services/ai/OpenAIProvider.js`

**Key Changes:**

| Section        | Lines   | Changes                         |
| -------------- | ------- | ------------------------------- |
| Constructor    | 21-27   | Added `tools: true` capability  |
| `chat()`       | 84-143  | Added tools to request body     |
| `streamChat()` | 148-259 | Accumulate tool_calls in stream |

**Key Code Snippets:**

```javascript
// Line 94-100: Add tools to request
if (options.tools && options.tools.length > 0) {
  requestBody.tools = options.tools;
  if (options.tool_choice) {
    requestBody.tool_choice = options.tool_choice;
  }
}
```

```javascript
// Line 134-137: Include tool_calls in response
if (message.tool_calls && message.tool_calls.length > 0) {
  result.tool_calls = message.tool_calls;
}
```

```javascript
// Line 228-248: Accumulate tool_calls during streaming
if (deltaObj?.tool_calls) {
  for (const toolCallDelta of deltaObj.tool_calls) {
    const index = toolCallDelta.index;

    if (!toolCalls[index]) {
      toolCalls[index] = {
        id: toolCallDelta.id || "",
        type: toolCallDelta.type || "function",
        function: {
          name: toolCallDelta.function?.name || "",
          arguments: toolCallDelta.function?.arguments || "",
        },
      };
    } else {
      // Accumulate arguments
      if (toolCallDelta.function?.arguments) {
        toolCalls[index].function.arguments += toolCallDelta.function.arguments;
      }
    }
  }
}
```

---

#### 4. ClaudeProvider.js - Claude Function Calling

**Location**: `apps/pcm-webapp/public/js/services/ai/ClaudeProvider.js`

**Key Changes:**

| Section        | Lines   | Changes                        |
| -------------- | ------- | ------------------------------ |
| Constructor    | 21-27   | Added `tools: true` capability |
| `chat()`       | 81-173  | Convert OpenAI → Claude format |
| `streamChat()` | 178-300 | Stream tool_use blocks         |

**Key Code Snippets:**

```javascript
// Line 92-106: Convert OpenAI format to Claude format
if (options.tools && options.tools.length > 0) {
  // Convert OpenAI format to Claude format
  requestBody.tools = options.tools.map((tool) => ({
    name: tool.function.name,
    description: tool.function.description,
    input_schema: tool.function.parameters,
  }));

  if (options.tool_choice && options.tool_choice !== "auto") {
    requestBody.tool_choice = {
      type: "tool",
      name: options.tool_choice.function?.name,
    };
  }
}
```

```javascript
// Line 129-147: Convert Claude response to OpenAI format
let textContent = "";
const toolCalls = [];

for (const block of data.content) {
  if (block.type === "text") {
    textContent += block.text;
  } else if (block.type === "tool_use") {
    // Convert Claude tool_use to OpenAI tool_calls format
    toolCalls.push({
      id: block.id,
      type: "function",
      function: {
        name: block.name,
        arguments: JSON.stringify(block.input),
      },
    });
  }
}
```

```javascript
// Line 252-263: Handle tool_use during streaming
if (
  parsed.type === "content_block_start" &&
  parsed.content_block?.type === "tool_use"
) {
  currentToolIndex++;
  toolCalls[currentToolIndex] = {
    id: parsed.content_block.id,
    type: "function",
    function: {
      name: parsed.content_block.name,
      arguments: "",
    },
  };
}
```

---

#### 5. DatabaseQueryTool.js - Available Functions

**Location**: `apps/pcm-webapp/public/js/services/DatabaseQueryTool.js`

**Available Functions:**

| Function               | Lines   | Purpose                             |
| ---------------------- | ------- | ----------------------------------- |
| `search_projects`      | 72-124  | Search projects by name/description |
| `get_project_details`  | 129-192 | Get full project information        |
| `search_screens`       | 197-249 | Search screens across projects      |
| `get_screen_events`    | 254-305 | Get events for a screen             |
| `list_subsystems`      | 310-349 | List all subsystems                 |
| `get_workflow`         | 354-390 | Get project workflows               |
| `search_by_technology` | 395-438 | Find by tech stack                  |

**Key Methods:**

```javascript
// Line 32-108: Define functions for AI
defineFunctions() {
  return [
    {
      name: "search_projects",
      description: "Search for projects by name or description...",
      parameters: {
        type: "object",
        properties: {
          query: { type: "string", description: "..." }
        },
        required: ["query"]
      },
      handler: this.searchProjects.bind(this)
    },
    // ... 6 more functions
  ];
}
```

```javascript
// Line 443-461: Execute function by name
async executeFunction(functionName, parameters) {
  const func = this.availableFunctions.find((f) => f.name === functionName);
  if (!func) {
    return {
      success: false,
      error: `Function '${functionName}' not found`,
    };
  }

  try {
    return await func.handler(parameters);
  } catch (error) {
    return {
      success: false,
      error: error.message,
    };
  }
}
```

---

## Provider Implementation

### How Providers Support Tools

#### OpenAI Format (Standard)

OpenAI uses the standard function calling format:

**Request:**

```json
{
  "model": "gpt-4o",
  "messages": [...],
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "search_projects",
        "description": "...",
        "parameters": { ... }
      }
    }
  ],
  "tool_choice": "auto"
}
```

**Response:**

```json
{
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": null,
        "tool_calls": [
          {
            "id": "call_abc123",
            "type": "function",
            "function": {
              "name": "search_projects",
              "arguments": "{\"query\":\"auth\"}"
            }
          }
        ]
      }
    }
  ]
}
```

---

#### Claude Format (Converted)

Claude uses a different format, which we convert:

**Our Code Converts:**

```javascript
// OpenAI format (input)
{
  type: "function",
  function: {
    name: "search_projects",
    description: "Search for projects...",
    parameters: { type: "object", properties: {...} }
  }
}

// ↓ Convert to Claude format ↓

// Claude format (sent to API)
{
  name: "search_projects",
  description: "Search for projects...",
  input_schema: { type: "object", properties: {...} }
}
```

**Claude Response (tool_use blocks):**

```json
{
  "content": [
    {
      "type": "tool_use",
      "id": "toolu_abc123",
      "name": "search_projects",
      "input": {
        "query": "auth"
      }
    }
  ]
}
```

**We Convert Back:**

```javascript
// Claude tool_use → OpenAI tool_calls
{
  id: block.id,
  type: "function",
  function: {
    name: block.name,
    arguments: JSON.stringify(block.input)
  }
}
```

This normalization allows our `ToolExecutor` to work uniformly with both providers!

---

## Tool Execution Flow

### Complete Execution Cycle

```
┌─────────────────────────────────────────────────────────────┐
│ Step 1: User Message                                         │
│ "Show me authentication projects"                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 2: AIPanel Prepares Request                            │
│ - Conversation: [{ role: "user", content: "..." }]          │
│ - Tools: [search_projects, get_project_details, ...]        │
│ - Options: { model, maxTokens, temperature, tools }         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 3: Send to Provider (OpenAI/Claude)                    │
│ POST /chat/completions                                       │
│ Body: { model, messages, tools, tool_choice: "auto" }       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 4: AI Analyzes & Decides                               │
│ AI thinks: "User wants authentication projects"             │
│           "I should use search_projects function"           │
│           "Parameter: query = 'authentication'"             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 5: AI Returns tool_calls                               │
│ {                                                            │
│   role: "assistant",                                         │
│   content: "",                                               │
│   tool_calls: [{                                             │
│     id: "call_xyz",                                          │
│     function: {                                              │
│       name: "search_projects",                               │
│       arguments: '{"query":"authentication"}'                │
│     }                                                         │
│   }]                                                         │
│ }                                                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 6: AIPanel Detects tool_calls                          │
│ if (response.tool_calls && response.tool_calls.length > 0)  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 7: Display Tool Call in UI                             │
│ displayToolCalls([...])                                      │
│ Shows: "🛠️ Calling 1 tool(s)..."                           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 8: ToolExecutor.executeToolCalls()                     │
│ - Parse: name="search_projects", args={query:"auth"}        │
│ - Execute: databaseQueryTool.executeFunction(...)           │
│ - Query IndexedDB                                            │
│ - Return: { success: true, count: 2, projects: [...] }      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 9: Display Tool Results in UI                          │
│ displayToolResults([...])                                    │
│ Shows: "✅ Tool results (1)"                                │
│        "✓ search_projects - Found 2 result(s)"              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 10: Add Results to Conversation                        │
│ conversationMessages.push({                                  │
│   role: "tool",                                              │
│   tool_call_id: "call_xyz",                                  │
│   name: "search_projects",                                   │
│   content: '{"success":true,"count":2,...}'                  │
│ })                                                           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 11: Loop Back to AI (iteration 2)                      │
│ Conversation now:                                            │
│ 1. User: "Show me auth projects"                            │
│ 2. Assistant: [tool_calls]                                   │
│ 3. Tool: [results]                                           │
│ Send to AI again...                                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 12: AI Generates Final Response                        │
│ AI now has tool results → generates answer:                 │
│ "I found 2 authentication projects:                         │
│  1. Login System (8 screens)                                │
│  2. SSO Integration (5 screens)"                            │
│ Returns: { role: "assistant", content: "...", tool_calls:   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 13: Display Final Response                             │
│ this.addMessage("assistant", response.content)              │
│ User sees AI's answer with data!                            │
└─────────────────────────────────────────────────────────────┘
```

### Multi-Turn Example

**Complex Query Requiring Multiple Tools:**

```
User: "Compare the Login project with SSO project"

↓ AI decides to call 2 tools

Tool Call 1: get_project_details({ project_id: 1 })
Tool Call 2: get_project_details({ project_id: 2 })

↓ Both execute simultaneously

Results: [
  { success: true, project: { id: 1, name: "Login", screens: 8, ... } },
  { success: true, project: { id: 2, name: "SSO", screens: 5, ... } }
]

↓ AI receives results

AI Response: "Comparison of Login vs SSO:
- Login has 8 screens, SSO has 5
- Login uses JSP, SSO uses React
- Both in Authentication subsystem..."
```

---

## User Interface

### Tool Call Display

```css
.ai-tool-calls {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  margin: var(--spacing-sm) 0;
  font-family: var(--font-mono);
  font-size: var(--font-size-sm);
}
```

**Visual Example:**

```
┌────────────────────────────────────────────┐
│ 🛠️ Calling 2 tool(s)...                   │
│                                             │
│ ┌─────────────────────────────────────────┐│
│ │ 🔍 Search Projects                      ││
│ │ {                                       ││
│ │   "query": "authentication"             ││
│ │ }                                       ││
│ └─────────────────────────────────────────┘│
│                                             │
│ ┌─────────────────────────────────────────┐│
│ │ 📋 Get Project Details                  ││
│ │ {                                       ││
│ │   "project_id": 1                       ││
│ │ }                                       ││
│ └─────────────────────────────────────────┘│
└────────────────────────────────────────────┘
```

### Tool Results Display

```css
.ai-tool-results {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  margin: var(--spacing-sm) 0;
  font-family: var(--font-mono);
  font-size: var(--font-size-sm);
}
```

**Visual Example:**

```
┌────────────────────────────────────────────┐
│ ✅ Tool results (2)                        │
│                                             │
│ ┌─────────────────────────────────────────┐│
│ │ ✓ search_projects                       ││
│ │ Found 2 result(s)                       ││
│ └─────────────────────────────────────────┘│
│                                             │
│ ┌─────────────────────────────────────────┐│
│ │ ✓ get_project_details                   ││
│ │ Retrieved project: Login System         ││
│ └─────────────────────────────────────────┘│
└────────────────────────────────────────────┘
```

### Settings UI

**Location**: AI Panel → Settings → Conversation Settings

```
┌────────────────────────────────────────────┐
│ Conversation Settings                       │
├────────────────────────────────────────────┤
│                                             │
│ ☑ Enable database access                   │
│   (AI can query IndexedDB)                 │
│                                             │
│ ☑ Enable function calling                  │
│   (AI decides when to query)               │
│   ⚠️ Only visible if provider supports     │
│                                             │
│ [Save]                                      │
└────────────────────────────────────────────┘
```

---

## Configuration

### Enable/Disable Function Calling

**Via UI:**

1. Open AI Panel
2. Click Settings (⚙️)
3. Scroll to "Conversation Settings"
4. Check/uncheck "🔧 Enable function calling"
5. Click "Save"

**Via Code:**

```javascript
// In AIPanel.js constructor (line 44-45)
this.enableFunctionCalling = true; // or false
this.maxToolIterations = 5; // Max loops

// In localStorage
localStorage.setItem("ai-function-calling", JSON.stringify(true));
```

### Settings Storage

```javascript
// Settings keys
"ai-database-access"      → true/false
"ai-function-calling"     → true/false

// Provider settings
"ai-provider-settings-openai"  → { maxTokens, temperature, thinking }
"openai-api-key"               → "sk-..."
```

### Load Settings on Init

```javascript
// apps/pcm-webapp/public/js/components/AIPanel.js:2349-2353
const functionCallingSaved = localStorage.getItem("ai-function-calling");
if (functionCallingSaved !== null) {
  this.enableFunctionCalling = JSON.parse(functionCallingSaved);
}
```

---

## Extending the System

### Adding a New Tool/Function

Want to add a new database query function? Follow these steps:

#### Step 1: Define Function in DatabaseQueryTool

**File**: `apps/pcm-webapp/public/js/services/DatabaseQueryTool.js`

```javascript
// Add to defineFunctions() array (line 32-108)
{
  name: "your_new_function",
  description: "Clear description for AI to understand when to use this",
  parameters: {
    type: "object",
    properties: {
      param1: {
        type: "string",
        description: "What this parameter does"
      },
      param2: {
        type: "number",
        description: "Another parameter"
      }
    },
    required: ["param1"]
  },
  handler: this.yourNewFunction.bind(this)
}
```

#### Step 2: Implement Handler Method

```javascript
// Add after existing handlers (after line 438)
/**
 * Your new function
 */
async yourNewFunction({ param1, param2 }) {
  try {
    // 1. Query IndexedDB via databaseManager
    const data = await databaseManager.yourQueryMethod(param1, param2);

    // 2. Process and format results
    const results = data.map(item => ({
      id: item.id,
      name: item.name,
      // ... format as needed
    }));

    // 3. Return success response
    return {
      success: true,
      count: results.length,
      data: results
    };
  } catch (error) {
    // 4. Return error response
    return {
      success: false,
      error: error.message
    };
  }
}
```

#### Step 3: Add Display Name (Optional)

**File**: `apps/pcm-webapp/public/js/services/ToolExecutor.js`

```javascript
// Add to getToolDisplayName() (line 88-102)
getToolDisplayName(functionName) {
  const nameMap = {
    // ... existing mappings
    your_new_function: "🎨 Your Display Name",
  };

  return nameMap[functionName] || `🛠️ ${functionName}`;
}
```

#### Step 4: Test

```javascript
// Test manually
const result = await databaseQueryTool.executeFunction("your_new_function", {
  param1: "test",
  param2: 123,
});
console.log(result);
```

**Complete Example: Get Recent Activity**

```javascript
// Step 1: Define
{
  name: "get_recent_activity",
  description: "Get recently modified projects and screens in the last N days",
  parameters: {
    type: "object",
    properties: {
      days: {
        type: "number",
        description: "Number of days to look back (default: 7)"
      }
    }
  },
  handler: this.getRecentActivity.bind(this)
}

// Step 2: Implement
async getRecentActivity({ days = 7 }) {
  try {
    const cutoffDate = Date.now() - (days * 24 * 60 * 60 * 1000);

    const projects = await databaseManager.getProjects();
    const screens = await databaseManager.getScreens();

    const recentProjects = projects.filter(p => p.updatedAt >= cutoffDate);
    const recentScreens = screens.filter(s => s.updatedAt >= cutoffDate);

    return {
      success: true,
      days: days,
      projects: recentProjects.length,
      screens: recentScreens.length,
      changes: {
        projects: recentProjects.map(p => ({
          id: p.id,
          name: p.name,
          updatedAt: new Date(p.updatedAt).toISOString()
        })),
        screens: recentScreens.map(s => ({
          id: s.id,
          name: s.name,
          projectId: s.projectId,
          updatedAt: new Date(s.updatedAt).toISOString()
        }))
      }
    };
  } catch (error) {
    return {
      success: false,
      error: error.message
    };
  }
}

// Step 3: Add display name
getToolDisplayName(functionName) {
  const nameMap = {
    // ...
    get_recent_activity: "📅 Get Recent Activity",
  };
  return nameMap[functionName] || `🛠️ ${functionName}`;
}
```

**Usage:**

```
User: "What changed in the last 3 days?"

AI calls: get_recent_activity({ days: 3 })

AI responds: "In the last 3 days:
- 2 projects were modified
- 5 screens were updated
..."
```

---

### Adding a New Provider with Tools Support

Want to add Gemini, Hugging Face, or custom provider?

#### Step 1: Create Provider File

**File**: `apps/pcm-webapp/public/js/services/ai/YourProvider.js`

```javascript
import { BaseProvider } from "./BaseProvider.js";

export class YourProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "your-provider",
      name: "Your Provider",
      apiKey: config.apiKey || "default-key",
      baseURL: config.baseURL || "https://api.example.com/v1",
      capabilities: {
        chat: true,
        stream: true,
        tools: true,  // ← Enable tools!
      },
      settings: {
        maxTokens: { supported: true, min: 1, max: 8192, default: 2048 },
        temperature: { supported: true, min: 0, max: 1, default: 0.7 },
      },
      ...config,
    });

    this.loadApiKey();
  }

  async chat(messages, options = {}) {
    // Implement chat with tools support
    const requestBody = {
      model: options.model || this.defaultModel,
      messages: messages,
      max_tokens: options.maxTokens || 2048,
    };

    // Add tools if provided
    if (options.tools && options.tools.length > 0) {
      requestBody.tools = this.convertToProviderFormat(options.tools);
    }

    const response = await fetch(`${this.baseURL}/chat`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${this.apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody),
    });

    const data = await response.json();

    // Convert provider response to standard format
    return this.normalizeResponse(data);
  }

  convertToProviderFormat(tools) {
    // Convert our standard tool format to provider's format
    // Similar to ClaudeProvider conversion
    return tools.map(tool => ({
      // Provider-specific format
    }));
  }

  normalizeResponse(data) {
    // Convert provider response to our standard format
    return {
      role: "assistant",
      content: data.content || "",
      tool_calls: this.extractToolCalls(data), // Convert to OpenAI format
      metadata: { ... }
    };
  }

  extractToolCalls(data) {
    // Extract and convert tool calls to standard OpenAI format
    // Return: [{ id, type: "function", function: { name, arguments } }]
  }
}
```

#### Step 2: Register Provider

**File**: `apps/pcm-webapp/public/js/services/ai/ProviderRegistry.js`

```javascript
import { YourProvider } from "./YourProvider.js";

// In constructor
this.register(new YourProvider());
```

#### Step 3: Test

```
1. Restart app
2. Open AI Settings
3. Select your provider
4. Configure API key
5. Enable function calling
6. Test: "Show me all projects"
```

---

## Comparison: Function Calling vs Context Injection

### Feature Comparison

| Feature              | Function Calling                  | Context Injection            |
| -------------------- | --------------------------------- | ---------------------------- |
| **Precision**        | ✅ High - AI decides exactly      | ❌ Low - Keyword matching    |
| **Token Usage**      | ✅ Efficient - Only what's needed | ❌ High - Injects all data   |
| **Multi-Turn**       | ✅ Yes - Complex workflows        | ❌ No - Single query         |
| **Provider Support** | ⚠️ OpenAI, Claude only            | ✅ All providers             |
| **Complexity**       | ⚠️ Higher - Tool loop             | ✅ Simple - Direct injection |
| **Transparency**     | ✅ Shows tool calls in UI         | ❌ Hidden from user          |
| **Performance**      | ✅ Fast - Targeted queries        | ⚠️ Slower - Large context    |

### Example: Same Query, Different Approaches

**Query:** "Show me authentication projects"

#### Approach 1: Context Injection

```
1. User asks question
2. System detects "project" keyword
3. Automatically runs search_projects("authentication")
4. Injects full results into message:
   "Show me authentication projects

    **Relevant Projects:**
    [{"id":1,"name":"Login",...}, {"id":2,"name":"SSO",...}]"
5. AI receives message with data
6. AI responds with info

Pros: Works with any provider
Cons: - Always searches even if not needed
      - Wastes tokens on irrelevant data
      - Can't do multi-turn
```

#### Approach 2: Function Calling

```
1. User asks question
2. System sends message + tool definitions
3. AI analyzes: "I need to search projects"
4. AI calls: search_projects({"query": "authentication"})
5. System executes function
6. System sends results back to AI
7. AI generates response with context

Pros: - AI decides when to query
      - Only queries what's needed
      - Supports multi-turn
      - Shows tool calls in UI
Cons: - Only OpenAI/Claude
      - More complex
```

### Token Usage Example

**Query:** "Tell me about the Login project"

**Context Injection:**

```
Prompt: ~50 tokens
Injected data: ~500 tokens (full project list)
Total: ~550 tokens
```

**Function Calling:**

```
Prompt: ~50 tokens
Tool definitions: ~200 tokens (one-time)
Tool call: ~10 tokens
Tool result: ~100 tokens (specific project)
Total: ~360 tokens (35% savings!)
```

---

## Best Practices

### 1. Function Design

✅ **DO:**

- Write clear, specific function descriptions
- Include examples in parameter descriptions
- Use appropriate parameter types (string, number, boolean)
- Mark required parameters explicitly
- Return consistent format: `{ success, data/error }`
- Handle errors gracefully
- Log function execution for debugging

❌ **DON'T:**

- Use vague descriptions like "Gets data"
- Forget to document parameters
- Return different formats for success/failure
- Throw unhandled exceptions
- Return sensitive data (passwords, keys)

### 2. Tool Loop Management

✅ **DO:**

- Set reasonable iteration limit (default: 5)
- Log each iteration for debugging
- Display tool calls transparently in UI
- Handle errors at each step
- Break loop when no more tools needed

❌ **DON'T:**

- Allow infinite loops
- Hide tool execution from users
- Skip error handling
- Continue loop on errors

### 3. Provider Implementation

✅ **DO:**

- Convert formats consistently (Claude ↔ OpenAI)
- Handle streaming tool calls correctly
- Accumulate tool arguments in stream
- Test with real API keys
- Document format conversion

❌ **DON'T:**

- Mix format standards
- Forget to handle tool_calls in streaming
- Skip testing streaming mode
- Assume formats are compatible

### 4. Performance

✅ **DO:**

- Cache tool definitions (don't regenerate)
- Execute independent tools in parallel
- Limit result size (pagination)
- Use specific queries over broad ones
- Monitor token usage

❌ **DON'T:**

- Send all database in one query
- Execute tools sequentially if independent
- Return unlimited results
- Query everything "just in case"

### 5. Security

✅ **DO:**

- Validate all tool parameters
- Sanitize user inputs
- Filter sensitive information from results
- Use read-only database operations
- Log suspicious patterns

❌ **DON'T:**

- Trust AI-provided parameters blindly
- Return password or API keys
- Allow database modifications
- Execute arbitrary code
- Skip validation

---

## Troubleshooting

### Issue: AI Doesn't Call Tools

**Symptoms:**

- AI responds without calling any functions
- Expected tool call doesn't happen

**Causes & Solutions:**

1. **Provider doesn't support tools**

   ```javascript
   // Check capability
   console.log(provider.capabilities.tools); // Should be true
   ```

2. **Function calling disabled**

   ```javascript
   // Check setting
   console.log(this.enableFunctionCalling); // Should be true
   // Fix: Enable in Settings
   ```

3. **Unclear function descriptions**

   ```javascript
   // Bad: "Gets data"
   // Good: "Search for projects by name or description. Returns project details including subsystem, repo URL, and environment links."
   ```

4. **AI decided not needed**
   ```
   // This is normal! AI is smart enough to know when tools aren't needed
   User: "What is BPMN?"
   AI: "BPMN stands for..." (No tools needed)
   ```

---

### Issue: Tool Execution Fails

**Error:**

```
❌ [ToolExecutor] Error executing search_projects: ...
```

**Causes & Solutions:**

1. **Invalid parameters**

   ```javascript
   // Check console for parameter validation errors
   // Fix parameter types or add validation
   ```

2. **Database error**

   ```javascript
   // Check if IndexedDB has data
   const projects = await databaseManager.getProjects();
   console.log(projects.length);
   ```

3. **Function not found**
   ```javascript
   // Verify function is registered
   const funcs = databaseQueryTool.getFunctionDefinitions();
   console.log(funcs.map((f) => f.name));
   ```

---

### Issue: Infinite Loop

**Symptoms:**

- Tool execution never stops
- "Max tool iterations reached" warning

**Causes & Solutions:**

1. **Tool returns error continuously**

   ```javascript
   // AI keeps retrying failed tool
   // Fix: Return proper error format
   return {
     success: false,
     error: "Clear error message"
   };
   ```

2. **Tool result not helpful**

   ```javascript
   // AI doesn't get useful data, tries again
   // Fix: Improve result formatting
   ```

3. **Circular dependencies**
   ```javascript
   // Tool A calls Tool B which needs Tool A
   // Fix: Redesign functions to avoid circularity
   ```

**Workaround:**

```javascript
// Reduce max iterations temporarily
this.maxToolIterations = 2; // Default: 5
```

---

### Issue: Tool Calls Not Displayed

**Symptoms:**

- Tools execute but UI doesn't show them
- No "🛠️ Calling tools..." message

**Solutions:**

1. **Check console**

   ```javascript
   // Should see:
   🔧 [ToolExecutor] Executing: search_projects
   ```

2. **Verify UI elements exist**

   ```javascript
   const container = document.getElementById("ai-messages");
   console.log(container); // Should exist
   ```

3. **Check CSS**
   ```css
   /* Ensure styles are loaded */
   .ai-tool-calls {
     display: block;
   }
   .ai-tool-results {
     display: block;
   }
   ```

---

### Issue: Streaming with Tools Broken

**Error:**

```
Failed to accumulate tool_calls in stream
```

**Solutions:**

1. **Check provider implementation**

   ```javascript
   // Verify tool_calls accumulation in streamChat()
   // OpenAI: Line 228-248
   // Claude: Line 252-270
   ```

2. **Test non-streaming first**

   ```javascript
   // Temporarily disable streaming
   provider.capabilities.stream = false;
   // If this works, issue is in streamChat()
   ```

3. **Check tool_calls format**
   ```javascript
   // Should match:
   [
     {
       id: "call_...",
       type: "function",
       function: { name: "...", arguments: "..." },
     },
   ];
   ```

---

## Examples

### Example 1: Simple Query

**User Input:**

```
"Show me all projects"
```

**Execution:**

```javascript
// 1. AI analyzes
AI thinks: "User wants list of all projects"
           "I should use list_subsystems to get overview"

// 2. Tool call
{
  id: "call_abc",
  function: {
    name: "list_subsystems",
    arguments: "{}"
  }
}

// 3. Execution
const result = await databaseQueryTool.executeFunction('list_subsystems', {});
// Returns: { success: true, count: 4, subsystems: [...] }

// 4. AI response
"The system has 4 subsystems with a total of 17 projects:

1. **Authentication** (5 projects)
   - Login System
   - SSO Integration
   - Password Reset
   - MFA System
   - Session Management

2. **User Management** (3 projects)
   ...
"
```

---

### Example 2: Multi-Step Query

**User Input:**

```
"Compare Login project with SSO project"
```

**Execution:**

```javascript
// Iteration 1: AI requests data
Tool calls: [
  { function: { name: "get_project_details", arguments: '{"project_id":1}' } },
  { function: { name: "get_project_details", arguments: '{"project_id":2}' } }
]

// Both execute
Results: [
  { success: true, project: { id: 1, name: "Login", screens: 8, ... } },
  { success: true, project: { id: 2, name: "SSO", screens: 5, ... } }
]

// Iteration 2: AI generates comparison
"Comparison of Login System vs SSO Integration:

**Login System:**
- 8 screens
- Technologies: JSP, Java, MySQL
- Last updated: 2 days ago

**SSO Integration:**
- 5 screens
- Technologies: React, Node.js, PostgreSQL
- Last updated: 1 week ago

**Key Differences:**
- Login has more screens (8 vs 5)
- Different tech stacks
- Login more recently updated
"
```

---

### Example 3: Complex Workflow

**User Input:**

```
"Find all screens using JSP in authentication projects, then show me their events"
```

**Execution:**

```javascript
// Iteration 1: Find auth projects
Tool call: search_projects({ query: "authentication" })
Result: { count: 2, projects: [1, 2] }

// Iteration 2: Find JSP screens
Tool call: search_by_technology({ technology: "jsp" })
Result: { count: 12, results: [...] }

// Iteration 3: Get events for each screen
Tool calls: [
  get_screen_events({ screen_id: 1 }),
  get_screen_events({ screen_id: 3 }),
  get_screen_events({ screen_id: 5 }),
]

// Iteration 4: AI summarizes
"I found 3 JSP screens in authentication projects:

1. **Login Screen**
   - 5 events:
     • Submit Login → Dashboard
     • Forgot Password → Reset Screen
     • ...

2. **Password Reset Screen**
   - 3 events:
     • Submit → Confirmation
     ...

3. **MFA Verification**
   - 4 events:
     ...
"
```

---

### Example 4: Error Handling

**User Input:**

```
"Show me project 999"
```

**Execution:**

```javascript
// AI calls
Tool call: get_project_details({ project_id: 999 })

// Execution fails
Result: {
  success: false,
  error: "Project not found"
}

// AI handles gracefully
"I couldn't find a project with ID 999. Would you like me to:
1. List all available projects?
2. Search for a specific project by name?
"
```

---

## Conclusion

The AI Function Calling System provides a powerful, efficient, and transparent way for AI assistants to access your database. By letting the AI decide when and how to query, it:

- ✅ Reduces token waste
- ✅ Improves response accuracy
- ✅ Enables complex multi-turn workflows
- ✅ Provides transparency through UI
- ✅ Extends easily with new tools

### Key Takeaways

1. **Two Modes Available**:
   - Function Calling (OpenAI/Claude)
   - Context Injection (All providers)

2. **Smart Decision Making**:
   - AI chooses when to query
   - AI selects appropriate tools
   - AI provides precise parameters

3. **Transparent Execution**:
   - Tool calls shown in UI
   - Results displayed clearly
   - Logs for debugging

4. **Extensible Architecture**:
   - Easy to add new tools
   - Simple provider integration
   - Modular design

### Next Steps

- **Try It**: Enable function calling in settings
- **Extend It**: Add custom tools for your use cases
- **Monitor It**: Check console logs during execution
- **Optimize It**: Add new providers or improve existing ones

---

## References

### Main Implementation Files

- **AIPanel.js**: `apps/pcm-webapp/public/js/components/AIPanel.js`
- **ToolExecutor.js**: `apps/pcm-webapp/public/js/services/ToolExecutor.js`
- **OpenAIProvider.js**: `apps/pcm-webapp/public/js/services/ai/OpenAIProvider.js`
- **ClaudeProvider.js**: `apps/pcm-webapp/public/js/services/ai/ClaudeProvider.js`
- **DatabaseQueryTool.js**: `apps/pcm-webapp/public/js/services/DatabaseQueryTool.js`

### Related Documentation

- [AI Database Query System](./AI_DATABASE_QUERY_SYSTEM.md) - Available query functions
- [API Standards](../../../docs/vibytes-framework/API-STANDARD.md) - Backend API guidelines
- [OpenAI Function Calling](https://platform.openai.com/docs/guides/function-calling) - Official docs
- [Claude Tool Use](https://docs.anthropic.com/en/docs/tool-use) - Official docs

---

**Last Updated**: November 6, 2025  
**Version**: 1.0.0  
**Author**: PCM Development Team
