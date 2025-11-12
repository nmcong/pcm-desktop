# Unified Function Calling System

## 📋 Overview

**Problem**: Some LLMs have native function calling support (OpenAI, Claude), but many don't (Ollama local models, Hugging Face, custom APIs).

**Solution**: A **unified adapter system** that provides function calling for **ALL** LLMs, regardless of native support!

---

## 🎯 Key Concept

```
┌────────────────────────────────────────────────────────────┐
│              YOUR LLM (Any Type)                           │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  Has Native Support?                                        │
│                                                             │
│  ├─ YES → Use NativeFunctionCallingAdapter                │
│  │         (OpenAI, Claude format)                         │
│  │                                                          │
│  └─ NO  → Use TextBasedFunctionCallingAdapter             │
│            (Parse from text response)                       │
│                                                             │
└────────────────────────────────────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────────┐
│         Standard Function Calling Interface                 │
│  - prepareTools()                                          │
│  - extractToolCalls()                                       │
│  - hasToolCalls()                                           │
└────────────────────────────────────────────────────────────┘
```

---

## 🔧 Architecture

### Components

1. **BaseFunctionCallingAdapter** - Abstract base class
2. **NativeFunctionCallingAdapter** - For LLMs with built-in support
3. **TextBasedFunctionCallingAdapter** - For LLMs without support
4. **FunctionCallingAdapterRegistry** - Manage custom adapters

### How It Works

#### Native Support (OpenAI, Claude)

```javascript
// LLM natively understands tools
Request: {
  messages: [...],
  tools: [...]  // ← LLM knows what this is
}

Response: {
  tool_calls: [...]  // ← LLM returns tool calls directly
}
```

#### Text-Based Support (Other LLMs)

````javascript
// Step 1: Inject tool descriptions into system prompt
System Prompt: "You have access to these tools:
1. search_projects(query) - Search for projects
2. get_project_details(id) - Get project info
..."

// Step 2: LLM responds with special format
LLM Response: "```json
{
  \"tool_calls\": [
    {\"name\": \"search_projects\", \"arguments\": {\"query\": \"auth\"}}
  ]
}
```"

// Step 3: Parse tool calls from text
Extracted: [
  { id: "call_123", type: "function", function: { name: "search_projects", ... } }
]
````

---

## 📚 Implementation Guide

### For Provider with Native Support

```javascript
import { BaseProvider } from "./BaseProvider.js";
import { NativeFunctionCallingAdapter } from "./FunctionCallingAdapter.js";

export class YourProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "your-provider",
      name: "Your Provider",
      capabilities: {
        chat: true,
        tools: true, // ← Native support!
      },
      ...config,
    });

    // Set up adapter
    this.functionCallingAdapter = new NativeFunctionCallingAdapter(this, {
      toolFormat: "openai", // or "claude"
    });
  }

  async chat(messages, options = {}) {
    // If tools are provided
    if (options.tools && this.functionCallingAdapter) {
      // Prepare tools (convert if needed)
      const preparedTools = this.functionCallingAdapter.prepareTools(
        options.tools,
      );

      // Call API with tools
      const response = await this.callAPIWithTools(messages, preparedTools);

      // Extract tool calls
      const toolCalls = this.functionCallingAdapter.extractToolCalls(response);

      return {
        role: "assistant",
        content: response.content || "",
        tool_calls: toolCalls, // Standard format!
      };
    }

    // Regular chat without tools
    return this.regularChat(messages, options);
  }
}
```

---

### For Provider WITHOUT Native Support

```javascript
import { BaseProvider } from "./BaseProvider.js";
import { TextBasedFunctionCallingAdapter } from "./FunctionCallingAdapter.js";

export class YourProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "your-provider",
      name: "Your Provider",
      capabilities: {
        chat: true,
        tools: false, // ← NO native support
      },
      ...config,
    });

    // Set up text-based adapter
    this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this, {
      format: "json", // Tool call format in text
    });

    // Enable function calling via adapter
    this.capabilities.tools = true; // Now we support it!
  }

  async chat(messages, options = {}) {
    let enhancedMessages = messages;

    // If tools are provided
    if (options.tools && this.functionCallingAdapter) {
      // Prepare tools (get system prompt with tool descriptions)
      const preparedTools = this.functionCallingAdapter.prepareTools(
        options.tools,
      );

      // Inject tool descriptions into messages
      enhancedMessages = this.functionCallingAdapter.injectToolsIntoMessages(
        messages,
        preparedTools,
      );
    }

    // Call API with enhanced messages
    const response = await this.callAPI(enhancedMessages, options);

    // Try to extract tool calls from text
    if (options.tools && this.functionCallingAdapter) {
      const toolCalls = this.functionCallingAdapter.extractToolCalls(response);

      if (toolCalls) {
        return {
          role: "assistant",
          content: "", // Tool call, no text content
          tool_calls: toolCalls,
        };
      }
    }

    // No tool calls, return normal response
    return {
      role: "assistant",
      content: response.content || response.text || "",
    };
  }
}
```

---

## 🎓 Complete Examples

### Example 1: Ollama with Llama 3

Llama 3 doesn't have native function calling, but we can add it!

```javascript
import { BaseProvider } from "./BaseProvider.js";
import { TextBasedFunctionCallingAdapter } from "./FunctionCallingAdapter.js";

export class OllamaProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "ollama",
      name: "Ollama (Local)",
      baseURL: config.baseURL || "http://localhost:11434/v1",
      apiKeyConfig: {
        required: false,
      },
      capabilities: {
        chat: true,
        stream: true,
        tools: false, // Llama 3 doesn't natively support tools
      },
      ...config,
    });

    // Add text-based function calling
    this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this, {
      format: "json",
      systemPromptTemplate: `You are Llama 3, a helpful AI assistant with tool access.

{TOOLS_DESCRIPTION}

To use a tool, respond ONLY with a JSON code block:
\`\`\`json
{
  "tool_calls": [
    {"name": "function_name", "arguments": {"param": "value"}}
  ]
}
\`\`\`

If no tool is needed, respond normally.`,
    });

    // Now supports tools!
    this.capabilities.tools = true;
  }

  async chat(messages, options = {}) {
    let requestMessages = messages;

    // Handle tools if provided
    if (options.tools && this.functionCallingAdapter) {
      const preparedTools = this.functionCallingAdapter.prepareTools(
        options.tools,
      );
      requestMessages = this.functionCallingAdapter.injectToolsIntoMessages(
        messages,
        preparedTools,
      );
    }

    // Call Ollama API
    const response = await fetch(`${this.baseURL}/chat/completions`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        model: options.model || "llama3",
        messages: requestMessages,
        temperature: options.temperature || 0.7,
        max_tokens: options.maxTokens || 2048,
      }),
    });

    const data = await response.json();
    const content = data.choices[0].message.content;

    // Extract tool calls if tools were provided
    if (options.tools && this.functionCallingAdapter) {
      const toolCalls = this.functionCallingAdapter.extractToolCalls({
        content,
      });

      if (toolCalls) {
        return {
          role: "assistant",
          content: "",
          tool_calls: toolCalls,
        };
      }
    }

    return {
      role: "assistant",
      content: content,
    };
  }
}
```

**Usage:**

```javascript
const provider = new OllamaProvider();

const response = await provider.chat(
  [{ role: "user", content: "Search for authentication projects" }],
  {
    model: "llama3",
    tools: [
      {
        type: "function",
        function: {
          name: "search_projects",
          description: "Search for projects by query",
          parameters: {
            type: "object",
            properties: {
              query: { type: "string" },
            },
          },
        },
      },
    ],
  },
);

// Response:
// {
//   role: "assistant",
//   content: "",
//   tool_calls: [
//     {
//       id: "call_123",
//       type: "function",
//       function: {
//         name: "search_projects",
//         arguments: '{"query": "authentication"}'
//       }
//     }
//   ]
// }
```

---

### Example 2: Hugging Face with Custom Format

```javascript
import { BaseProvider } from "./BaseProvider.js";
import { TextBasedFunctionCallingAdapter } from "./FunctionCallingAdapter.js";

export class HuggingFaceProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "huggingface",
      name: "Hugging Face",
      baseURL: "https://api-inference.huggingface.co/models",
      capabilities: {
        chat: true,
        tools: false,
      },
      ...config,
    });

    // Custom XML-based format for some models
    this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this, {
      format: "xml",
      systemPromptTemplate: `You can use tools. Format:
<tool_call>
  <name>function_name</name>
  <arguments>{"param": "value"}</arguments>
</tool_call>

{TOOLS_DESCRIPTION}`,
    });

    this.capabilities.tools = true;
    this.defaultModel = "meta-llama/Llama-2-13b-chat-hf";
  }

  async chat(messages, options = {}) {
    let prompt = this.formatPrompt(messages);

    // Add tools to prompt
    if (options.tools && this.functionCallingAdapter) {
      const preparedTools = this.functionCallingAdapter.prepareTools(
        options.tools,
      );
      prompt = preparedTools.systemPrompt + "\n\n" + prompt;
    }

    const model = options.model || this.defaultModel;
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
        },
      }),
    });

    const data = await response.json();
    const generatedText = data[0]?.generated_text || "";

    // Parse tool calls
    if (options.tools && this.functionCallingAdapter) {
      const toolCalls = this.functionCallingAdapter.extractToolCalls({
        content: generatedText,
      });

      if (toolCalls) {
        return {
          role: "assistant",
          content: "",
          tool_calls: toolCalls,
        };
      }
    }

    return {
      role: "assistant",
      content: generatedText,
    };
  }

  formatPrompt(messages) {
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
}
```

---

### Example 3: Custom API with Function Calling

```javascript
import { BaseProvider } from "./BaseProvider.js";
import { TextBasedFunctionCallingAdapter } from "./FunctionCallingAdapter.js";

export class CustomAPIProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "custom-api",
      name: "Custom API",
      baseURL: config.baseURL || "https://your-api.com",
      capabilities: {
        chat: true,
        tools: false,
      },
      ...config,
    });

    // Use custom format
    this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this, {
      format: "custom",
      startDelimiter: "<<<TOOL_START>>>",
      endDelimiter: "<<<TOOL_END>>>",
      systemPromptTemplate: `Available tools:
{TOOLS_DESCRIPTION}

Use format:
<<<TOOL_START>>>
TOOL: function_name | ARGS: {"param": "value"}
<<<TOOL_END>>>`,
    });

    this.capabilities.tools = true;
  }

  async chat(messages, options = {}) {
    let requestBody = {
      conversation: messages,
      settings: {
        max_length: options.maxTokens || 2048,
      },
    };

    // Add tools
    if (options.tools && this.functionCallingAdapter) {
      const preparedTools = this.functionCallingAdapter.prepareTools(
        options.tools,
      );

      // Inject into system message
      const enhancedMessages =
        this.functionCallingAdapter.injectToolsIntoMessages(
          messages,
          preparedTools,
        );

      requestBody.conversation = enhancedMessages;
    }

    const response = await fetch(`${this.baseURL}/generate`, {
      method: "POST",
      headers: {
        "X-API-Key": this.apiKey,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody),
    });

    const data = await response.json();

    // Parse tool calls
    if (options.tools && this.functionCallingAdapter) {
      const toolCalls = this.functionCallingAdapter.extractToolCalls({
        content: data.response,
      });

      if (toolCalls) {
        return {
          role: "assistant",
          content: "",
          tool_calls: toolCalls,
        };
      }
    }

    return {
      role: "assistant",
      content: data.response,
    };
  }
}
```

---

## 🎨 Custom Adapters

You can create custom adapters for specific LLMs:

```javascript
import { BaseFunctionCallingAdapter } from "./FunctionCallingAdapter.js";
// Use custom adapter
import { adapterRegistry } from "./FunctionCallingAdapter.js";

export class MyCustomAdapter extends BaseFunctionCallingAdapter {
  constructor(provider, config = {}) {
    super(provider);
    this.type = "custom";
    this.config = config;
  }

  prepareTools(tools) {
    // Convert to your custom format
    return {
      customFormat: tools.map((t) => ({
        func_name: t.function.name,
        func_desc: t.function.description,
        func_params: t.function.parameters,
      })),
    };
  }

  extractToolCalls(response) {
    // Parse from your custom format
    const content = response.content || "";

    // Your custom parsing logic
    const regex = /CALL:(\w+)\((.*?)\)/g;
    const toolCalls = [];
    let match;

    while ((match = regex.exec(content)) !== null) {
      const [, name, argsStr] = match;

      toolCalls.push({
        id: `call_${Date.now()}_${toolCalls.length}`,
        type: "function",
        function: {
          name: name,
          arguments: argsStr,
        },
      });
    }

    return toolCalls.length > 0 ? toolCalls : null;
  }
}

adapterRegistry.register("my-provider", MyCustomAdapter);
```

---

## 🔍 Parsing Formats Supported

The `TextBasedFunctionCallingAdapter` can parse multiple formats:

### Format 1: JSON Code Blocks (Recommended)

````
```json
{
  "tool_calls": [
    {
      "name": "search_projects",
      "arguments": {"query": "authentication"}
    }
  ]
}
````

```

### Format 2: XML Tags

```

<tool_call>
<name>search_projects</name>
<arguments>{"query": "authentication"}</arguments>
</tool_call>

```

### Format 3: Custom Format

```

TOOL: search_projects | ARGS: {"query": "authentication"}

```

### Format 4: Function Call Syntax

```

search_projects(query="authentication")

````

---

## ⚙️ Configuration Options

### NativeFunctionCallingAdapter

```javascript
new NativeFunctionCallingAdapter(provider, {
  toolFormat: "openai",  // or "claude"
  toolCallsField: "tool_calls",  // Field name in response
});
````

### TextBasedFunctionCallingAdapter

````javascript
new TextBasedFunctionCallingAdapter(provider, {
  // Output format
  format: "json", // 'json', 'xml', or 'custom'

  // Delimiters
  startDelimiter: "```json",
  endDelimiter: "```",

  // System prompt template
  systemPromptTemplate: `Your custom template here
{TOOLS_DESCRIPTION}`,

  // Parsing options
  strict: true, // Strict JSON parsing
  allowMultiple: true, // Allow multiple tool calls
});
````

---

## 📊 Comparison: Native vs Text-Based

| Feature          | Native Support   | Text-Based Adapter |
| ---------------- | ---------------- | ------------------ |
| **Accuracy**     | ✅ Very High     | ⚠️ Depends on LLM  |
| **Speed**        | ✅ Fast          | ⚠️ Slightly slower |
| **Reliability**  | ✅ 99%+          | ⚠️ 80-95%          |
| **Setup**        | 🟢 Easy          | 🟡 Moderate        |
| **Flexibility**  | ⚠️ Limited       | ✅ Very flexible   |
| **Cost**         | ⚠️ May be higher | ✅ Often cheaper   |
| **Local Models** | ❌ Rare          | ✅ Yes             |

---

## 🎯 Best Practices

### 1. Choose Right Adapter

```javascript
// Has native support? Use native adapter
if (provider.hasNativeToolSupport()) {
  adapter = new NativeFunctionCallingAdapter(provider);
} else {
  // Use text-based
  adapter = new TextBasedFunctionCallingAdapter(provider);
}
```

### 2. Clear Function Descriptions

```javascript
// ❌ Bad
{
  name: "search",
  description: "Searches"
}

// ✅ Good
{
  name: "search_projects",
  description: "Search for projects by name or description. Returns a list of matching projects with their details.",
  parameters: {
    type: "object",
    properties: {
      query: {
        type: "string",
        description: "The search query (project name or keywords)"
      }
    }
  }
}
```

### 3. Add Examples

```javascript
{
  name: "search_projects",
  description: "...",
  parameters: {...},
  examples: [
    { query: "authentication" },
    { query: "user management" }
  ]
}
```

### 4. Test Thoroughly

```javascript
// Test with and without tools
const testCases = [
  {
    input: "Hello",
    expectTools: false,
  },
  {
    input: "Search for auth projects",
    expectTools: true,
    expectedTool: "search_projects",
  },
];

for (const test of testCases) {
  const response = await provider.chat(
    [{ role: "user", content: test.input }],
    { tools: allTools },
  );

  const hasTools = response.tool_calls && response.tool_calls.length > 0;

  if (hasTools !== test.expectTools) {
    console.error(`Test failed: ${test.input}`);
  }
}
```

### 5. Handle Parsing Failures

```javascript
extractToolCalls(response) {
  try {
    const toolCalls = this.parseJSON(response.content);

    if (!toolCalls) {
      // Log for debugging
      console.warn("[Adapter] No tool calls found in:", response.content.substring(0, 200));
    }

    return toolCalls;
  } catch (error) {
    console.error("[Adapter] Parsing error:", error);
    return null;
  }
}
```

---

## 🐛 Troubleshooting

### Issue 1: LLM Not Calling Tools

**Problem**: LLM responds normally instead of calling tools

**Solutions**:

1. **Make prompt clearer**:

```javascript
systemPromptTemplate: `IMPORTANT: You MUST use tools when relevant.
To use a tool, output ONLY a JSON code block.
DO NOT output any other text when calling tools.

{TOOLS_DESCRIPTION}`;
```

2. **Add examples in tool descriptions**

3. **Test with different models** (some models better at following instructions)

### Issue 2: Parsing Fails

**Problem**: Tool calls not extracted from response

**Solutions**:

1. **Check response format**:

```javascript
console.log("Raw response:", response.content);
```

2. **Adjust delimiters**:

````javascript
new TextBasedFunctionCallingAdapter(provider, {
  startDelimiter: "```", // More flexible
  endDelimiter: "```",
});
````

3. **Add custom parsing**:

```javascript
extractToolCalls(response) {
  // Try built-in parsers first
  let toolCalls = super.extractToolCalls(response);

  if (!toolCalls) {
    // Try your custom format
    toolCalls = this.parseMyCustomFormat(response.content);
  }

  return toolCalls;
}
```

### Issue 3: Multiple Tool Calls Not Working

**Problem**: Only first tool call extracted

**Solution**:

```javascript
// Ensure allowMultiple is true
new TextBasedFunctionCallingAdapter(provider, {
  allowMultiple: true,
});

// Check regex flags (global flag 'g')
const regex = /pattern/g; // ← 'g' flag for multiple matches
```

---

## 📚 API Reference

### BaseFunctionCallingAdapter

```typescript
abstract class BaseFunctionCallingAdapter {
  constructor(provider: BaseProvider);

  abstract prepareTools(tools: Tool[]): any;
  abstract extractToolCalls(response: any): ToolCall[] | null;

  hasToolCalls(response: any): boolean;
  getType(): string;
}
```

### NativeFunctionCallingAdapter

```typescript
class NativeFunctionCallingAdapter extends BaseFunctionCallingAdapter {
  constructor(
    provider: BaseProvider,
    config?: {
      toolFormat?: "openai" | "claude";
      toolCallsField?: string;
    },
  );

  prepareTools(tools: Tool[]): Tool[];
  extractToolCalls(response: any): ToolCall[] | null;
}
```

### TextBasedFunctionCallingAdapter

```typescript
class TextBasedFunctionCallingAdapter extends BaseFunctionCallingAdapter {
  constructor(
    provider: BaseProvider,
    config?: {
      format?: "json" | "xml" | "custom";
      startDelimiter?: string;
      endDelimiter?: string;
      systemPromptTemplate?: string;
      strict?: boolean;
      allowMultiple?: boolean;
    },
  );

  prepareTools(tools: Tool[]): { systemPrompt: string; originalTools: Tool[] };
  extractToolCalls(response: any): ToolCall[] | null;
  injectToolsIntoMessages(messages: Message[], preparedTools: any): Message[];

  // Parsing methods
  parseJSONFormat(content: string): ToolCall[] | null;
  parseXMLFormat(content: string): ToolCall[] | null;
  parseCustomFormat(content: string): ToolCall[] | null;
}
```

---

## 🎓 Summary

### ✅ What This System Provides

1. **Unified Interface** - Same API for all LLMs
2. **Automatic Adaptation** - System chooses right adapter
3. **Flexible Parsing** - Multiple format support
4. **Easy Extension** - Add custom adapters
5. **No Native Support Needed** - Works with ANY LLM

### 🚀 Quick Start Checklist

- [ ] Import `FunctionCallingAdapter.js`
- [ ] Create adapter in provider constructor
- [ ] Set `this.functionCallingAdapter = ...`
- [ ] Use adapter in `chat()` method
- [ ] Test with real tools

### 📖 Next Steps

1. Read full examples above
2. Copy closest example to your LLM
3. Customize for your needs
4. Test thoroughly
5. Deploy!

---

**Last Updated**: November 6, 2025  
**Version**: 1.0.0  
**Author**: PCM Development Team

**You can now add function calling to ANY LLM! 🎉**
