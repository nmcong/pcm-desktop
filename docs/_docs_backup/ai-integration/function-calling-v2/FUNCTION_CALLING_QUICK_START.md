# Function Calling Quick Start

## ⚡ 3-Minute Guide to Add Function Calling to ANY LLM

---

## 🎯 The Magic Formula

```javascript
// 1. Import adapter
import { TextBasedFunctionCallingAdapter } from "./FunctionCallingAdapter.js";

// 2. Add to your provider constructor
this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this);
this.capabilities.tools = true;  // Enable function calling!

// 3. Use in chat() method
if (options.tools) {
  // Prepare tools
  const preparedTools = this.functionCallingAdapter.prepareTools(options.tools);

  // Inject into messages
  messages = this.functionCallingAdapter.injectToolsIntoMessages(messages, preparedTools);
}

// 4. Call your API normally
const response = await this.callYourAPI(messages);

// 5. Extract tool calls
if (options.tools) {
  const toolCalls = this.functionCallingAdapter.extractToolCalls(response);

  if (toolCalls) {
    return { role: "assistant", content: "", tool_calls: toolCalls };
  }
}

return { role: "assistant", content: response.text };
```

**Done!** Your LLM now supports function calling! 🎉

---

## 📝 Complete Example

```javascript
import { BaseProvider } from "./BaseProvider.js";
import { TextBasedFunctionCallingAdapter } from "./FunctionCallingAdapter.js";

export class MyLLMProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "my-llm",
      name: "My LLM",
      baseURL: "https://api.myllm.com",
      capabilities: {
        chat: true,
        tools: false, // ← Doesn't have native support
      },
      ...config,
    });

    // ✨ Add function calling support
    this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this, {
      format: "json", // LLM will output JSON
    });

    // ✅ Now supports tools!
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

    // Call your API
    const response = await fetch(`${this.baseURL}/chat`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${this.apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        messages: requestMessages,
        max_tokens: options.maxTokens || 2048,
      }),
    });

    const data = await response.json();

    // Extract tool calls if tools were provided
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

    // No tool calls, return normal response
    return {
      role: "assistant",
      content: data.response,
    };
  }
}
```

---

## 🧪 Testing

```javascript
// Test it!
const provider = new MyLLMProvider({ apiKey: "your-key" });

const response = await provider.chat(
  [{ role: "user", content: "Search for authentication projects" }],
  {
    tools: [
      {
        type: "function",
        function: {
          name: "search_projects",
          description: "Search for projects by query",
          parameters: {
            type: "object",
            properties: {
              query: { type: "string", description: "Search query" },
            },
            required: ["query"],
          },
        },
      },
    ],
  },
);

// Expected output:
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

console.log("Tool calls:", response.tool_calls);
```

---

## 🎨 Customization

### Custom System Prompt

```javascript
this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this, {
  systemPromptTemplate: `You are my custom AI.

Available tools:
{TOOLS_DESCRIPTION}

To call a tool, use this format:
\`\`\`json
{"tool_calls": [{"name": "...", "arguments": {...}}]}
\`\`\``,
});
```

### Custom Format (XML)

```javascript
this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this, {
  format: "xml",
  systemPromptTemplate: `Tools: {TOOLS_DESCRIPTION}
  
Format:
<tool_call>
  <name>function_name</name>
  <arguments>{"param": "value"}</arguments>
</tool_call>`,
});
```

### Custom Parsing

```javascript
class MyCustomAdapter extends TextBasedFunctionCallingAdapter {
  extractToolCalls(response) {
    // Try default parsers first
    let toolCalls = super.extractToolCalls(response);

    if (!toolCalls) {
      // Try your custom format
      toolCalls = this.parseMyFormat(response.content);
    }

    return toolCalls;
  }

  parseMyFormat(content) {
    // Your custom parsing logic
    const regex = /CALL:(\w+)\((.*?)\)/g;
    const calls = [];
    let match;

    while ((match = regex.exec(content)) !== null) {
      calls.push({
        id: `call_${Date.now()}_${calls.length}`,
        type: "function",
        function: {
          name: match[1],
          arguments: match[2],
        },
      });
    }

    return calls.length > 0 ? calls : null;
  }
}

// Use custom adapter
this.functionCallingAdapter = new MyCustomAdapter(this);
```

---

## 🔧 Common Issues

### Issue: LLM doesn't call tools

**Fix:** Make prompt more explicit:

```javascript
systemPromptTemplate: `CRITICAL: When user asks to search/find/get data, you MUST use the available tools.

{TOOLS_DESCRIPTION}

OUTPUT FORMAT WHEN USING TOOLS (no other text):
\`\`\`json
{"tool_calls": [...]}
\`\`\``;
```

### Issue: Parsing fails

**Fix:** Log the raw response:

```javascript
if (options.tools) {
  console.log("Raw response:", data.response);
  const toolCalls = this.functionCallingAdapter.extractToolCalls({
    content: data.response,
  });
}
```

### Issue: Multiple calls not working

**Fix:** Ensure `allowMultiple: true`:

```javascript
this.functionCallingAdapter = new TextBasedFunctionCallingAdapter(this, {
  format: "json",
  allowMultiple: true, // ← Enable multiple tool calls
});
```

---

## 📚 Full Documentation

For complete guide, see:

- **[UNIFIED_FUNCTION_CALLING.md](./UNIFIED_FUNCTION_CALLING.md)** - Complete system documentation
- **[CUSTOM_LLM_INTEGRATION.md](./CUSTOM_LLM_INTEGRATION.md)** - General LLM integration
- **[AI_FUNCTION_CALLING_SYSTEM.md](./AI_FUNCTION_CALLING_SYSTEM.md)** - Native function calling (OpenAI/Claude)

---

## ✅ Checklist

Before deploying:

- [ ] Adapter created in constructor
- [ ] `capabilities.tools = true` set
- [ ] Tools prepared when provided
- [ ] Tools injected into messages
- [ ] Tool calls extracted from response
- [ ] Tested with real tools
- [ ] Tested without tools (normal chat)
- [ ] Error handling added
- [ ] Logging added for debugging

---

**That's it! You now have function calling support! 🚀**

Any LLM can now intelligently query your database!
