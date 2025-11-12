# 🔄 AI Chat Message Flow - Detailed Architecture

**Comprehensive guide to message processing flow in PCM WebApp AI Chat**

**Version**: 1.0.0  
**Last Updated**: November 10, 2025  
**Status**: ✅ Production Reference

---

## 📋 Table of Contents

1. [Overview](#-overview)
2. [Complete Message Flow](#-complete-message-flow)
3. [Phase 1: User Input](#-phase-1-user-input)
4. [Phase 2: Planning & Intent Detection](#-phase-2-planning--intent-detection)
5. [Phase 3: Provider Communication](#-phase-3-provider-communication)
6. [Phase 4: Function Calling](#-phase-4-function-calling)
7. [Phase 5: Response Processing](#-phase-5-response-processing)
8. [Error Handling](#-error-handling)
9. [Code References](#-code-references)
10. [Integration Examples](#-integration-examples)

---

## 🎯 Overview

### System Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        PCM WebApp                             │
│                                                               │
│  ┌────────────┐    ┌──────────────┐    ┌─────────────┐     │
│  │  AIPanel   │───▶│ Conversation │───▶│   Provider  │     │
│  │    (UI)    │    │   Manager    │    │  Registry   │     │
│  └────────────┘    └──────────────┘    └─────────────┘     │
│         │                  │                    │            │
│         │                  │                    │            │
│         ▼                  ▼                    ▼            │
│  ┌────────────┐    ┌──────────────┐    ┌─────────────┐     │
│  │  ChatView  │    │   History    │    │  OpenAI API │     │
│  │ (Display)  │    │   Manager    │    │ Claude API  │     │
│  └────────────┘    └──────────────┘    │ Gemini API  │     │
│         │                  │            │  ViByte API │     │
│         │                  │            └─────────────┘     │
│         ▼                  ▼                    │            │
│  ┌────────────┐    ┌──────────────┐           │            │
│  │  Message   │    │   IndexedDB  │           │            │
│  │  Renderer  │    │   Storage    │           │            │
│  └────────────┘    └──────────────┘           │            │
│                                                │            │
│  ┌────────────────────────────────────────────┘            │
│  │                                                          │
│  ▼                                                          │
│  ┌─────────────────────────────────────────┐               │
│  │       Function Calling System           │               │
│  │  ┌─────────────┐    ┌──────────────┐   │               │
│  │  │  Planning   │───▶│   Function   │   │               │
│  │  │  Service    │    │   Registry   │   │               │
│  │  └─────────────┘    └──────────────┘   │               │
│  │         │                    │          │               │
│  │         ▼                    ▼          │               │
│  │  ┌─────────────┐    ┌──────────────┐   │               │
│  │  │   Intent    │    │   Database   │   │               │
│  │  │  Detection  │    │   Manager    │   │               │
│  │  └─────────────┘    └──────────────┘   │               │
│  └─────────────────────────────────────────┘               │
└──────────────────────────────────────────────────────────────┘
```

### Key Components

| Component                  | File                                  | Responsibility         |
|----------------------------|---------------------------------------|------------------------|
| **AIPanel**                | `components/AIPanel.js`               | Main UI controller     |
| **ChatView**               | `components/AIChatView.js`            | Message display        |
| **ConversationManager**    | `components/AIConversationManager.js` | State management       |
| **ProviderRegistry**       | `services/ProviderRegistry.js`        | AI provider management |
| **FunctionCallingService** | `services/FunctionCallingService.js`  | Function execution     |
| **PlanningService**        | `services/PlanningService.js`         | Query planning         |
| **IntentDetectionService** | `services/IntentDetectionService.js`  | Intent analysis        |

---

## 🔄 Complete Message Flow

### High-Level Sequence

```
User Input
    ↓
[1] Input Validation & UI Update
    ↓
[2] Conversation Management
    ↓
[3] Planning & Intent Detection (if enabled)
    ↓
[4] System Prompt Building
    ↓
[5] Provider Selection & Health Check
    ↓
[6] AI API Call (with/without function calling)
    ↓
[7] Response Processing
    ↓
[8] Function Execution (if tool calls present)
    ↓
[9] Continuation Loop (if more tool calls needed)
    ↓
[10] Final Response Display
    ↓
[11] History Persistence
```

---

## 📝 Phase 1: User Input

### Step 1.1: User Types & Submits

**Source Code**: `components/AIPanel.js` (lines 328-361)

```javascript
/**
 * Handle send message
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/components/AIPanel.js
 * Lines: 328-361
 */
async handleSendMessage(e) {
  e.preventDefault();
  const input = document.getElementById("ai-input");
  if (!input) return;

  const message = input.value.trim();
  if (!message || this.isProcessing) return;  // Validation

  // Create conversation if needed
  if (!conversationManager.getCurrentConversationId()) {
    await conversationManager.createConversation("New Conversation", {
      provider: providerRegistry.activeProviderId,
      ...this.conversationSettings,
    });
  }

  // Add user message to UI
  this.chatView.addMessage("user", message);

  // Persist to IndexedDB
  await conversationManager.addMessage({ role: "user", content: message });

  // Clear input
  input.value = "";

  // Auto scroll to bottom
  setTimeout(() => {
    const messagesContainer = document.getElementById("ai-messages");
    if (messagesContainer) {
      messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
  }, 100);

  // Get AI response
  await this.getAIResponse(message);
}
```

### Step 1.2: Message Display

**Source Code**: `components/AIChatView.js` (lines 18-21)

```javascript
/**
 * Add message to chat
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/components/AIChatView.js
 * Lines: 18-21
 */
addMessage(role, content) {
  AIMessageRenderer.addMessage(this.container, role, content);
  this.messages.push({ role, content, timestamp: Date.now() });
}
```

### Step 1.3: Conversation Persistence

**Source Code**: `components/AIConversationManager.js` (lines 64-79)

```javascript
/**
 * Add message to current conversation
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/components/AIConversationManager.js
 * Lines: 64-79
 */
async addMessage(message) {
  if (!this.currentConversationId) {
    // Create new conversation if none exists
    this.currentConversationId = await this.createConversation(
      "New Conversation",
      {},
    );
  }

  try {
    await chatHistoryManager.addMessage(this.currentConversationId, message);
  } catch (error) {
    console.error("Failed to add message:", error);
    throw error;
  }
}
```

**Source Code**: `services/ChatHistoryManager.js` (lines 168-194)

```javascript
/**
 * Add message to chat (IndexedDB)
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/services/ChatHistoryManager.js
 * Lines: 168-194
 */
async addMessage(chatId, roleOrMessage, content) {
  const chat = await this.getChat(chatId);
  if (!chat) throw new Error("Chat not found");

  // Support both signatures:
  // addMessage(id, role, content)
  // addMessage(id, { role, content })
  let message;
  if (typeof roleOrMessage === "object") {
    message = {
      role: roleOrMessage.role,
      content: roleOrMessage.content,
      timestamp: Date.now(),
    };
  } else {
    message = {
      role: roleOrMessage,
      content: content,
      timestamp: Date.now(),
    };
  }

  chat.messages.push(message);

  await this.saveChat(chat);
  return chat;
}
```

---

## 🧠 Phase 2: Planning & Intent Detection

### Step 2.1: Intent Detection (Enhanced Mode)

**Source Code**: `services/IntentDetectionService.js`

```javascript
/**
 * Detect user intent from message
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/services/IntentDetectionService.js
 *
 * Returns: {
 *   category: IntentCategory,
 *   confidence: number,
 *   entities: string[],
 *   suggestedTools: string[]
 * }
 */
import { detectIntent } from "./IntentDetectionService.js";

const intent = detectIntent(userMessage);
console.log("[AIPanel] Intent:", intent);
// Example output:
// {
//   category: "ANALYSIS",
//   confidence: 0.85,
//   entities: ["screens", "database"],
//   suggestedTools: ["semanticSearch", "analyzeRelationships"]
// }
```

### Step 2.2: Planning Decision

**Source Code**: `services/PlanningService.js`

```javascript
/**
 * Check if query needs planning
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/services/PlanningService.js
 *
 * Algorithm:
 * - Check intent complexity (ANALYSIS, COMPARISON, TROUBLESHOOTING)
 * - Check query keywords ("analyze", "impact", "trace", "how", "why")
 * - Check entity count (>2 entities)
 * - Check estimated tools (>2 function calls)
 */
import { needsPlanning, generatePlan } from "./PlanningService.js";

const shouldPlan = needsPlanning(userMessage, intent);

if (shouldPlan) {
  const plan = generatePlan(userMessage, intent);
  console.log("[AIPanel] Execution plan:", plan);
  // Example output:
  // {
  //   query: "Analyze impact of adding phone field",
  //   complexity: "complex",
  //   steps: [
  //     { stepNumber: 1, action: "search", tool: "semanticSearch", ... },
  //     { stepNumber: 2, action: "fetch", tool: "getDetails", ... },
  //     { stepNumber: 3, action: "analyze", tool: "analyzeRelationships", ... }
  //   ],
  //   estimatedTime: 12
  // }
}
```

**Integration Example**: `examples/AIPanel.planning-integration.example.js` (lines 30-64)

```javascript
/**
 * Planning integration in handleSendMessage
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/examples/AIPanel.planning-integration.example.js
 * Lines: 30-64
 */
try {
  // 1. Detect intent
  console.log("[AIPanel] Detecting intent...");
  const intent = detectIntent(message);

  // 2. Check if planning is needed
  const shouldPlan = needsPlanning(message, intent);

  // 3. Generate plan if needed
  let plan = null;
  if (shouldPlan) {
    plan = generatePlan(message, intent);

    // 4. Show plan to user (optional but recommended)
    const planMessage = `📋 **Execution Plan** (${plan.complexity})\n\n${formatPlan(plan)}`;
    this.chatView.addMessage("system", planMessage, {
      type: "plan",
      collapsible: true,
      metadata: { plan },
    });

    // Show progress indicator
    this.showProcessingIndicator(
      `Processing ${plan.steps.length} steps (~${plan.estimatedTime}s)...`,
    );
  }

  // 5. Get AI response with planning context
  await this.getAIResponseWithPlanning(message, intent, plan);
} catch (error) {
  console.error("[AIPanel] Error:", error);
  this.chatView.addMessage("error", `Error: ${error.message}`);
}
```

### Step 2.3: System Prompt Building

**Source Code**: `services/EnhancedPromptService.js` (lines 10-205)

```javascript
/**
 * Build comprehensive system prompt with context
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/services/EnhancedPromptService.js
 * Lines: 10-205
 */
import { buildSystemPrompt } from "./EnhancedPromptService.js";

const systemPrompt = await buildSystemPrompt({
  includeStatistics: true, // Add system statistics
  includeExamples: true, // Add few-shot examples
  includeGuidelines: true, // Add best practices
  includePlanning: plan !== null, // Add planning instructions
  plan: plan, // The execution plan
  userContext: {
    // User-specific context
    currentProject: this.currentProject,
    currentScreen: this.currentScreen,
    recentActivity: this.recentActivity,
  },
});

// System prompt will include:
// - AI capabilities description
// - Current system statistics (project count, screen count, etc.)
// - Few-shot examples of good queries
// - Tool usage guidelines
// - Execution plan (if planning enabled)
```

**With Planning** (lines 167-202):

```javascript
// Add planning section if planning is enabled
if (includePlanning && plan) {
  systemPrompt += `\n## EXECUTION PLAN\n\n`;
  systemPrompt += `This is a **${plan.complexity.toUpperCase()}** query that requires systematic execution.\n\n`;
  systemPrompt += `### Planned Steps (${plan.steps.length} steps, ~${plan.estimatedTime}s):\n\n`;

  plan.steps.forEach((step) => {
    const deps = step.dependsOn
      ? ` [Requires: Step ${step.dependsOn.join(", ")}]`
      : "";
    systemPrompt += `**Step ${step.stepNumber}**: ${step.description}\n`;
    systemPrompt += `  - Action: ${step.action}\n`;
    systemPrompt += `  - Tool: \`${step.tool}\`\n`;
    if (step.parameters) {
      systemPrompt += `  - Parameters: ${JSON.stringify(step.parameters)}\n`;
    }
    systemPrompt += `  - Estimated time: ~${step.estimatedTime}s${deps}\n\n`;
  });

  systemPrompt += `\n### Planning Instructions:\n\n`;
  systemPrompt += `1. **Follow the plan** - Execute steps in order\n`;
  systemPrompt += `2. **Check dependencies** - Verify prerequisites completed\n`;
  systemPrompt += `3. **Validate results** - Check results before proceeding\n`;
  systemPrompt += `4. **Handle failures** - Try alternatives or skip dependents\n`;
  systemPrompt += `5. **Synthesize at the end** - Combine all results\n`;
}
```

---

## 🌐 Phase 3: Provider Communication

### Step 3.1: Provider Selection

**Source Code**: `components/AIPanel.js` (lines 366-379)

```javascript
/**
 * Get AI response - Provider setup
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/components/AIPanel.js
 * Lines: 366-379
 */
async getAIResponse(userMessage) {
  try {
    this.isProcessing = true;
    this.chatView.showTypingIndicator();

    // Get active provider
    const provider = providerRegistry.getActive();
    if (!provider) {
      throw new Error("No AI provider available");
    }

    // Health check
    const isAvailable = await provider.isAvailable();
    if (!isAvailable) {
      throw new Error(`${provider.name} is not available`);
    }

    // Check function calling support
    const useFunctionCalling =
      this.enableFunctionCalling && provider.capabilities.tools;

    if (useFunctionCalling) {
      await this.handleFunctionCallingMode(provider);
    }
  } catch (error) {
    // Error handling...
  }
}
```

**Source Code**: `services/ProviderRegistry.js` (lines 85-87)

```javascript
/**
 * Get active provider
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/services/ProviderRegistry.js
 * Lines: 85-87
 */
getActive() {
  return this.providers.get(this.activeProviderId);
}
```

### Step 3.2: Function Calling Mode Setup

**Source Code**: `components/AIPanel.js` (lines 400-427)

```javascript
/**
 * Handle function calling mode
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/components/AIPanel.js
 * Lines: 400-427
 */
async handleFunctionCallingMode(provider) {
  console.log("🔧 Using Function Calling mode");

  // Get conversation messages
  const conversationMessages = this.chatView.getMessages().map((msg) => ({
    role: msg.role,
    content: msg.content,
  }));

  // Get function definitions from FunctionCallingService
  const availableFunctions = functionCallingService.getAvailableFunctions();

  // Format tools for the provider (OpenAI format)
  const tools = availableFunctions.map((func) => ({
    type: "function",
    function: {
      name: func.name,
      description: func.description,
      parameters: func.parameters,
    },
  }));

  const options = {
    ...this.conversationSettings,        // maxTokens, temperature, etc.
    model: this.modelSelector?.getSelectedModelId(),
    tools,                                // Available functions
    tool_choice: "auto",                  // Let AI decide when to use tools
  };

  // Continue to iteration loop...
}
```

### Step 3.3: Provider API Call

**Different providers have different implementations:**

#### OpenAI Provider

**Source Code**: `services/impl/OpenAIProvider.js` (lines 84-143)

```javascript
/**
 * Chat completion - OpenAI
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/services/impl/OpenAIProvider.js
 * Lines: 84-143
 */
async chat(messages, options = {}) {
  try {
    const requestBody = {
      model: options.model || this.defaultModel,
      messages: messages,
      max_completion_tokens: options.maxTokens || 2048,
      temperature: options.temperature || 1.0,
      stream: false,
    };

    // Add tools if provided (NATIVE function calling)
    if (options.tools && options.tools.length > 0) {
      requestBody.tools = options.tools;
      if (options.tool_choice) {
        requestBody.tool_choice = options.tool_choice;
      }
    }

    const response = await fetch(`${this.baseURL}/chat/completions`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${this.apiKey}`,
      },
      body: JSON.stringify(requestBody),
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
    const message = data.choices[0].message;

    const result = {
      role: "assistant",
      content: message.content || "",
      metadata: {
        model: data.model,
        provider: this.id,
        usage: data.usage,
        finishReason: data.choices[0].finish_reason,
      },
    };

    // Include tool_calls if present
    if (message.tool_calls && message.tool_calls.length > 0) {
      result.tool_calls = message.tool_calls;
    }

    return result;
  } catch (error) {
    return this.handleError(error);
  }
}
```

#### ViByte Provider (Text-Based Function Calling)

**Source Code**: `services/impl/ViByteProvider.js` (lines 196-323)

```javascript
/**
 * Chat completion - ViByte (with text-based function calling)
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/services/impl/ViByteProvider.js
 * Lines: 196-323
 */
async chat(messages, options = {}) {
  try {
    let requestMessages = messages;

    // Handle tools if provided (TEXT-BASED adapter)
    if (options.tools && this.functionCallingAdapter) {
      console.log(
        `[${this.name}] Function calling enabled with ${options.tools.length} tools`,
      );

      // Prepare tools (generates system prompt with tool descriptions)
      const preparedTools = this.functionCallingAdapter.prepareTools(
        options.tools,
      );

      // Inject tool descriptions into messages
      requestMessages = this.functionCallingAdapter.injectToolsIntoMessages(
        messages,
        preparedTools,
      );
    }

    // Convert messages to prompt
    const prompt = this.messagesToPrompt(requestMessages);

    // Use thinking endpoint if thinking mode is enabled
    const endpoint = options.thinking ? "/api/thinking" : "/api/generate";

    const requestBody = {
      model: options.model || this.defaultModel,
      prompt: prompt,
      stream: false,
    };

    // Add options
    requestBody.options = {
      temperature: options.temperature || 0.7,
      num_predict: options.maxTokens || 2048,
    };

    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${this.apiKey}`,
      },
      body: JSON.stringify(requestBody),
      signal: AbortSignal.timeout(this.timeout),
    });

    // ... response handling

    // Try to extract tool calls from text response
    if (options.tools && this.functionCallingAdapter) {
      const toolCalls = this.functionCallingAdapter.extractToolCalls({
        content: content,
      });

      if (toolCalls) {
        return {
          role: "assistant",
          content: "",
          tool_calls: toolCalls,
          metadata: { ... },
        };
      }
    }

    // No tool calls, return normal response
    return { role: "assistant", content, metadata: { ... } };
  } catch (error) {
    return this.handleError(error);
  }
}
```

---

## 🛠️ Phase 4: Function Calling

### Step 4.1: Tool Call Detection

**Source Code**: `components/AIPanel.js` (lines 453-458)

```javascript
/**
 * Detect tool calls in AI response
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/components/AIPanel.js
 * Lines: 453-458
 */
if (response.tool_calls && response.tool_calls.length > 0) {
  console.log(`🛠️ AI requested ${response.tool_calls.length} tool call(s)`);

  // Process tool calls...
}
```

### Step 4.2: Tool Execution Loop

**Source Code**: `components/AIPanel.js` (lines 431-553)

```javascript
/**
 * Tool iteration loop
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/components/AIPanel.js
 * Lines: 431-553
 */
let iteration = 0;
let responseContent = "";

while (iteration < this.maxToolIterations) {
  iteration++;
  console.log(`🔄 Tool iteration ${iteration}/${this.maxToolIterations}`);

  // Get AI response (may include tool calls)
  let response;
  if (provider.capabilities.stream) {
    response = await this.streamingHandler.streamResponseWithTools(
      provider,
      conversationMessages,
      options,
    );
  } else {
    response = await provider.chat(conversationMessages, options);
  }

  // Check for tool calls
  if (response.tool_calls && response.tool_calls.length > 0) {
    console.log(`🛠️ AI requested ${response.tool_calls.length} tool call(s)`);

    // Add assistant message with tool calls to conversation
    conversationMessages.push({
      role: "assistant",
      content: response.content || "",
      tool_calls: response.tool_calls,
    });

    // Execute each tool call
    for (const toolCall of response.tool_calls) {
      console.log(`📞 Calling function: ${toolCall.function.name}`);

      const functionName = toolCall.function.name;
      let functionArgs = {};

      try {
        functionArgs =
          typeof toolCall.function.arguments === "string"
            ? JSON.parse(toolCall.function.arguments)
            : toolCall.function.arguments;
      } catch (error) {
        console.error("Failed to parse function arguments:", error);
        functionArgs = {};
      }

      try {
        // Execute the function
        const result = await functionCallingService.executeFunction(
          functionName,
          functionArgs,
        );

        console.log(`✅ Function result:`, result);

        // Add function result to conversation
        conversationMessages.push({
          role: "tool",
          tool_call_id: toolCall.id,
          name: functionName,
          content: JSON.stringify(result.result || result),
        });
      } catch (error) {
        console.error(`❌ Function execution error:`, error);

        // Add error result to conversation
        conversationMessages.push({
          role: "tool",
          tool_call_id: toolCall.id,
          name: functionName,
          content: JSON.stringify({
            error: error.message,
            success: false,
          }),
        });
      }
    }

    // Continue loop to get final response
    continue;
  }

  // No tool calls - final response
  if (response.content) {
    responseContent = response.content;
    this.chatView.hideTypingIndicator();
    this.chatView.addMessage("assistant", response.content);
    await conversationManager.addMessage({
      role: "assistant",
      content: response.content,
    });
  }

  // Break loop
  break;
}
```

### Step 4.3: Function Execution

**Source Code**: `services/FunctionCallingService.js` (lines 55-152)

```javascript
/**
 * Execute a function call from AI
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/services/FunctionCallingService.js
 * Lines: 55-152
 */
async executeFunction(functionName, parameters = {}) {
  const executionId = this.generateExecutionId();
  const startTime = Date.now();

  console.log(`[FunctionCalling] Executing function: ${functionName}`, {
    parameters,
    executionId,
  });

  // Log function call start
  aiChatLogger.logFunctionCall(functionName, parameters);

  try {
    // Validate function exists
    if (!this.hasFunction(functionName)) {
      // Debug: List all available functions
      const available = this.getAvailableFunctions();
      console.error(
        `[FunctionCalling] Function "${functionName}" not found!`,
      );
      console.error(
        `[FunctionCalling] Available functions:`,
        available.map((f) => f.name),
      );
      const error = new Error(`Function "${functionName}" not found`);

      // Log function error
      aiChatLogger.logFunctionResult(
        functionName,
        null,
        error,
        Date.now() - startTime,
      );
      throw error;
    }

    // Execute the function
    const result = await executeFunction(functionName, parameters);

    const executionTime = Date.now() - startTime;

    // Log function result
    aiChatLogger.logFunctionResult(functionName, result, null, executionTime);

    // Record execution in history
    this.recordExecution({
      executionId,
      functionName,
      parameters,
      result,
      success: true,
      executionTime,
      timestamp: new Date().toISOString(),
    });

    console.log(`[FunctionCalling] Function executed successfully:`, {
      functionName,
      executionTime: `${executionTime}ms`,
      result,
    });

    return {
      success: true,
      result,
      executionId,
      executionTime,
    };
  } catch (error) {
    // Error handling...
  }
}
```

**Function Registry**: `services/functions/index.js`

```javascript
/**
 * All available functions for AI
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/services/functions/index.js
 */
import { advancedQueryFunctions } from "./AdvancedQueryFunctions.js";
import { impactTracingFunctions } from "./ImpactTracingFunctions.js";

// ... other function modules

export const allFunctions = {
  // Project functions
  projects: projectFunctions,

  // Screen functions
  screens: screenFunctions,

  // Database functions
  database: databaseFunctions,

  // Knowledge base functions
  knowledge: knowledgeFunctions,

  // Advanced query functions
  advancedQuery: advancedQueryFunctions,

  // Impact tracing functions
  impactTracing: impactTracingFunctions,

  // Data management functions
  data: dataFunctions,
};
```

---

## 💬 Phase 5: Response Processing

### Step 5.1: Display Final Response

**Source Code**: `components/AIPanel.js` (lines 540-551)

```javascript
/**
 * Display final AI response
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/components/AIPanel.js
 * Lines: 540-551
 */
// No tool calls - final response
if (response.content) {
  responseContent = response.content;

  // Hide typing indicator
  this.chatView.hideTypingIndicator();

  // Display message in UI
  this.chatView.addMessage("assistant", response.content);

  // Persist to IndexedDB
  await conversationManager.addMessage({
    role: "assistant",
    content: response.content,
  });
}
```

### Step 5.2: Step Validation (Planning Mode)

**Source Code**: `services/PlanningService.js`

```javascript
/**
 * Validate plan execution results
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/services/PlanningService.js
 *
 * This should be integrated in tool execution loop
 */
export function validateStepResult(step, result) {
  const validation = {
    success: false,
    canProceed: false,
    message: "",
  };

  if (!result) {
    validation.message = `Step ${step.stepNumber} returned no result`;
    return validation;
  }

  // Check if result indicates success
  if (result.success === false) {
    validation.message = `Step ${step.stepNumber} failed: ${result.error || "Unknown error"}`;
    return validation;
  }

  // Check if result has data
  if (
    step.action === "search" ||
    step.action === "fetch" ||
    step.action === "analyze"
  ) {
    if (!result.data) {
      validation.message = `Step ${step.stepNumber} returned no data`;
      return validation;
    }

    // For search results, check if we got any matches
    if (step.action === "search") {
      const isEmpty = Array.isArray(result.data) && result.data.length === 0;
      const isEmptyObject =
        typeof result.data === "object" &&
        Object.values(result.data).every(
          (v) => Array.isArray(v) && v.length === 0,
        );

      if (isEmpty || isEmptyObject) {
        validation.success = true; // Not an error, just no results
        validation.canProceed = false; // But can't proceed
        validation.message = `Step ${step.stepNumber} found no matching results`;
        return validation;
      }
    }
  }

  // Success!
  validation.success = true;
  validation.canProceed = true;
  validation.message = `Step ${step.stepNumber} completed successfully`;
  return validation;
}
```

**Integration Example**:

```javascript
// In tool execution loop (with planning)
for (const toolCall of response.tool_calls) {
  // Find corresponding step in plan
  const step = plan?.steps.find((s) => s.tool === toolCall.function.name);

  // Execute function
  const result = await functionCallingService.executeFunction(
    toolCall.function.name,
    functionArgs,
  );

  // Validate if planning mode
  if (step) {
    const validation = validateStepResult(step, result);

    if (!validation.canProceed) {
      console.warn(`[Planning] ${validation.message}`);
      // Handle: skip dependent steps, inform user, etc.
    }
  }

  // Add to conversation
  conversationMessages.push({
    role: "tool",
    tool_call_id: toolCall.id,
    name: toolCall.function.name,
    content: JSON.stringify(result.result || result),
  });
}
```

---

## ⚠️ Error Handling

### Error Handling Locations

```javascript
/**
 * Error handling in AIPanel
 *
 * Location: apps/pcm-webapp/public/js/modules/ai/components/AIPanel.js
 * Lines: 388-395
 */
catch (error) {
  console.error("AI response error:", error);

  // Hide typing indicator
  this.chatView.hideTypingIndicator();

  // Show error message to user
  this.chatView.addMessage("assistant", `Error: ${error.message}`);
} finally {
  // Always reset processing state
  this.isProcessing = false;
}
```

### Common Error Scenarios

| Error Type                   | Location                                   | Handling                                   |
|------------------------------|--------------------------------------------|--------------------------------------------|
| **No provider**              | `AIPanel.getAIResponse()`                  | Show "No AI provider available"            |
| **Provider unavailable**     | `AIPanel.getAIResponse()`                  | Show "{provider} is not available"         |
| **API error**                | `Provider.chat()`                          | Parse error response, show message         |
| **Function not found**       | `FunctionCallingService.executeFunction()` | List available functions, throw error      |
| **Function execution error** | Tool execution loop                        | Add error result to conversation, continue |
| **Parse error**              | Tool argument parsing                      | Use empty object, log error                |
| **Network timeout**          | Provider API call                          | Catch timeout, show timeout error          |

---

## 📚 Code References

### Complete File Structure

```
apps/pcm-webapp/public/js/modules/ai/
│
├── components/
│   ├── AIPanel.js                      # Main chat UI (lines 17-658)
│   │   ├── handleSendMessage()         # Lines 328-361
│   │   ├── getAIResponse()             # Lines 366-395
│   │   └── handleFunctionCallingMode() # Lines 400-553
│   │
│   ├── AIChatView.js                   # Message display (lines 7-180)
│   │   └── addMessage()                # Lines 18-21
│   │
│   ├── AIConversationManager.js        # State management (lines 8-94)
│   │   ├── createConversation()        # Lines 42-58
│   │   └── addMessage()                # Lines 64-79
│   │
│   └── AIModelSelector.js              # Model selection (lines 8-373)
│
├── services/
│   ├── ProviderRegistry.js             # Provider management (lines 1-100)
│   │   ├── getActive()                 # Lines 85-87
│   │   └── setActive()                 # Lines 92-100
│   │
│   ├── FunctionCallingService.js       # Function execution (lines 17-152)
│   │   └── executeFunction()           # Lines 55-152
│   │
│   ├── PlanningService.js              # Query planning (~450 lines)
│   │   ├── needsPlanning()             # Check if planning needed
│   │   ├── generatePlan()              # Generate execution plan
│   │   └── validateStepResult()        # Validate step results
│   │
│   ├── IntentDetectionService.js       # Intent analysis
│   │   └── detectIntent()              # Detect user intent
│   │
│   ├── EnhancedPromptService.js        # Prompt building (lines 10-409)
│   │   ├── buildSystemPrompt()         # Lines 10-205
│   │   └── enrichUserMessage()         # Lines 172-196
│   │
│   ├── ChatHistoryManager.js           # Persistence (lines 6-206)
│   │   └── addMessage()                # Lines 168-194
│   │
│   ├── impl/
│   │   ├── OpenAIProvider.js           # OpenAI integration (lines 7-361)
│   │   │   └── chat()                  # Lines 84-143
│   │   │
│   │   ├── ClaudeProvider.js           # Claude integration (lines 7-364)
│   │   ├── GeminiProvider.js           # Gemini integration (lines 7-309)
│   │   ├── ViByteProvider.js           # ViByte integration (lines 11-546)
│   │   │   └── chat()                  # Lines 196-323
│   │   └── HuggingFaceProvider.js      # HuggingFace integration (lines 7-231)
│   │
│   └── functions/
│       ├── index.js                    # Function registry
│       ├── AdvancedQueryFunctions.js   # Advanced queries
│       ├── ImpactTracingFunctions.js   # Impact analysis
│       └── ...                         # Other function modules
│
└── docs-intergration/
    ├── MESSAGE_FLOW_ARCHITECTURE.md    # This document
    ├── AI_PLANNING_STRATEGY_GUIDE.md   # Planning guide
    └── IMPACT_ANALYSIS_TRACEABILITY_GUIDE.md  # Tracing guide
```

---

## 🎓 Integration Examples

### Example 1: Basic Message Flow (No Planning)

```javascript
/**
 * Basic integration without planning
 * Simple queries: "Get project #5", "Show all screens"
 */

// In AIPanel.js
async handleSendMessage(e) {
  e.preventDefault();
  const message = this.messageInput.value.trim();
  if (!message || this.isProcessing) return;

  // 1. Create conversation if needed
  if (!conversationManager.getCurrentConversationId()) {
    await conversationManager.createConversation("New Chat", {
      provider: providerRegistry.activeProviderId,
    });
  }

  // 2. Add to UI and persist
  this.chatView.addMessage("user", message);
  await conversationManager.addMessage({ role: "user", content: message });

  // 3. Clear input
  this.messageInput.value = "";

  // 4. Get AI response (with function calling if enabled)
  await this.getAIResponse(message);
}

async getAIResponse(message) {
  try {
    this.isProcessing = true;
    this.chatView.showTypingIndicator();

    const provider = providerRegistry.getActive();
    if (!provider) throw new Error("No provider");

    // Check function calling support
    if (this.enableFunctionCalling && provider.capabilities.tools) {
      await this.handleFunctionCallingMode(provider);
    } else {
      // Simple chat without tools
      const response = await provider.chat([
        { role: "system", content: "You are a helpful assistant" },
        { role: "user", content: message }
      ]);

      this.chatView.addMessage("assistant", response.content);
      await conversationManager.addMessage({
        role: "assistant",
        content: response.content
      });
    }
  } catch (error) {
    this.chatView.addMessage("assistant", `Error: ${error.message}`);
  } finally {
    this.chatView.hideTypingIndicator();
    this.isProcessing = false;
  }
}
```

### Example 2: With Planning & Intent Detection

```javascript
/**
 * Advanced integration with planning
 * Complex queries: "Analyze impact of X", "How does Y affect Z?"
 *
 * Based on: examples/AIPanel.planning-integration.example.js
 */

import { detectIntent } from "./services/IntentDetectionService.js";
import {
  needsPlanning,
  generatePlan,
  formatPlan,
  validateStepResult,
} from "./services/PlanningService.js";
import { buildSystemPrompt } from "./services/EnhancedPromptService.js";

async handleSendMessage(e) {
  e.preventDefault();
  const message = this.messageInput.value.trim();
  if (!message || this.isProcessing) return;

  this.messageInput.value = "";
  this.chatView.addMessage("user", message);

  try {
    // === PLANNING INTEGRATION ===

    // 1. Detect intent
    const intent = detectIntent(message);
    console.log("[AIPanel] Intent:", intent.category, intent.confidence);

    // 2. Check if planning needed
    const shouldPlan = needsPlanning(message, intent);

    // 3. Generate plan if needed
    let plan = null;
    if (shouldPlan) {
      plan = generatePlan(message, intent);

      // Show plan to user
      this.chatView.addMessage("system", formatPlan(plan), {
        type: "plan",
        collapsible: true,
      });

      this.showProgressIndicator(
        `Processing ${plan.steps.length} steps (~${plan.estimatedTime}s)...`,
      );
    }

    // 4. Build enhanced system prompt
    const systemPrompt = await buildSystemPrompt({
      includeStatistics: true,
      includeExamples: true,
      includeGuidelines: true,
      includePlanning: shouldPlan,
      plan: plan,
      userContext: {
        currentProject: this.currentProject,
        currentScreen: this.currentScreen,
      },
    });

    // 5. Get AI response with planning context
    await this.getAIResponseWithPlanning(message, systemPrompt, plan);

  } catch (error) {
    console.error("[AIPanel] Error:", error);
    this.chatView.addMessage("error", `Error: ${error.message}`);
  }
}

async getAIResponseWithPlanning(message, systemPrompt, plan) {
  const provider = providerRegistry.getActive();

  // Build messages
  const messages = [
    { role: "system", content: systemPrompt },
    ...this.conversationHistory,
    { role: "user", content: message }
  ];

  // Get available functions
  const tools = functionCallingService.getAvailableFunctions().map((f) => ({
    type: "function",
    function: {
      name: f.name,
      description: f.description,
      parameters: f.parameters,
    },
  }));

  // Tool iteration loop with validation
  let iteration = 0;
  while (iteration < this.maxToolIterations) {
    iteration++;

    const response = await provider.chat(messages, {
      model: this.selectedModel,
      tools: tools,
      tool_choice: "auto",
      temperature: 0.3, // Lower for planning mode
    });

    // Check for tool calls
    if (response.tool_calls && response.tool_calls.length > 0) {
      messages.push({
        role: "assistant",
        content: response.content || "",
        tool_calls: response.tool_calls,
      });

      // Execute each tool call
      for (const toolCall of response.tool_calls) {
        const functionName = toolCall.function.name;
        const functionArgs = JSON.parse(toolCall.function.arguments);

        // Find step in plan (if planning mode)
        const step = plan?.steps.find((s) => s.tool === functionName);

        if (step) {
          console.log(`[Planning] Executing step ${step.stepNumber}: ${step.description}`);
        }

        // Execute function
        const result = await functionCallingService.executeFunction(
          functionName,
          functionArgs,
        );

        // Validate result (if planning mode)
        if (step) {
          const validation = validateStepResult(step, result);

          if (!validation.canProceed) {
            console.warn(`[Planning] Step ${step.stepNumber} validation failed:`, validation.message);
            // Optionally: inform user, adjust plan, etc.
          } else {
            console.log(`[Planning] Step ${step.stepNumber} completed successfully`);
          }
        }

        // Add result to conversation
        messages.push({
          role: "tool",
          tool_call_id: toolCall.id,
          name: functionName,
          content: JSON.stringify(result.result || result),
        });
      }

      continue; // Next iteration
    }

    // Final response
    if (response.content) {
      this.chatView.hideTypingIndicator();
      this.chatView.addMessage("assistant", response.content);
      await conversationManager.addMessage({
        role: "assistant",
        content: response.content,
      });
    }

    break;
  }
}
```

### Example 3: Streaming with Tools

```javascript
/**
 * Streaming response with function calling
 * Provides real-time feedback to user
 */

async handleFunctionCallingMode(provider) {
  const messages = this.chatView.getMessages().map((msg) => ({
    role: msg.role,
    content: msg.content,
  }));

  const tools = functionCallingService.getAvailableFunctions().map((f) => ({
    type: "function",
    function: {
      name: f.name,
      description: f.description,
      parameters: f.parameters,
    },
  }));

  const options = {
    model: this.modelSelector?.getSelectedModelId(),
    tools: tools,
    tool_choice: "auto",
  };

  let iteration = 0;
  while (iteration < this.maxToolIterations) {
    iteration++;

    // Use streaming handler if provider supports streaming
    let response;
    if (provider.capabilities.stream) {
      console.log("🌊 Starting streaming response...");

      // StreamingHandler will:
      // 1. Display response chunks in real-time
      // 2. Parse tool calls from stream
      // 3. Return complete response
      response = await this.streamingHandler.streamResponseWithTools(
        provider,
        messages,
        options,
      );
    } else {
      response = await provider.chat(messages, options);
    }

    // Process tool calls (same as above)
    if (response.tool_calls && response.tool_calls.length > 0) {
      // Execute tools and continue...
    }

    // Final response
    if (response.content) {
      // Already displayed via streaming
      break;
    }
  }
}
```

---

## 📊 Performance Considerations

### Metrics to Monitor

```javascript
/**
 * Add performance tracking
 */

// In handleSendMessage
const startTime = Date.now();

// ... message processing ...

const totalTime = Date.now() - startTime;
console.log(`[Performance] Total message processing time: ${totalTime}ms`);

// Track by phase:
const metrics = {
  intentDetection: 0,
  planGeneration: 0,
  promptBuilding: 0,
  providerCall: 0,
  toolExecution: 0,
  responseDisplay: 0,
  total: totalTime,
};
```

### Optimization Tips

1. **Caching**:
    - Cache system statistics for prompt building
    - Cache function definitions
    - Cache intent detection results for similar queries

2. **Parallel Execution**:
    - Execute independent function calls in parallel
    - Pre-load provider models

3. **Streaming**:
    - Use streaming for better perceived performance
    - Show progress indicators

4. **Request Batching**:
    - Batch multiple tool results in single message

---

## ✅ Summary

### Complete Flow Checklist

- [x] **Phase 1: User Input**
    - [x] Input validation
    - [x] UI update
    - [x] Conversation management
    - [x] Message persistence

- [x] **Phase 2: Planning & Intent Detection**
    - [x] Intent detection
    - [x] Planning decision
    - [x] Plan generation
    - [x] System prompt building

- [x] **Phase 3: Provider Communication**
    - [x] Provider selection
    - [x] Health check
    - [x] Function definitions formatting
    - [x] API call

- [x] **Phase 4: Function Calling**
    - [x] Tool call detection
    - [x] Tool execution
    - [x] Result validation
    - [x] Iteration loop

- [x] **Phase 5: Response Processing**
    - [x] Response display
    - [x] History persistence
    - [x] Error handling
    - [x] UI cleanup

---

## 🔗 Related Documentation

### Core Guides

- [AI_PLANNING_STRATEGY_GUIDE.md](./AI_PLANNING_STRATEGY_GUIDE.md) - Planning system details
- [IMPACT_ANALYSIS_TRACEABILITY_GUIDE.md](./IMPACT_ANALYSIS_TRACEABILITY_GUIDE.md) - Impact analysis
- [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md) - Implementation guide

### API References

- [DATABASE_MANAGER_API.md](../../../../../../docs/api-reference/DATABASE_MANAGER_API.md) - Database API
- [functions/README.md](../services/functions/README.md) - Available functions

### Examples

- [AIPanel.planning-integration.example.js](../examples/AIPanel.planning-integration.example.js) - Planning integration
- [AIPanel.enhanced.example.js](./examples/AIPanel.enhanced.example.js) - Enhanced features

---

**Version**: 1.0.0  
**Created**: November 10, 2025  
**Maintainer**: AI Development Team  
**Status**: ✅ Production Reference

**For questions or updates, refer to the source code locations listed in this document.**
