# 🚀 AI Chat Message Flow - Quick Reference

**Fast lookup for developers**

---

## 📍 Where to Find Code

### User Input Handling

```
📂 components/AIPanel.js
   📝 handleSendMessage()         → Lines 328-361
   📝 Input validation           → Line 334
   📝 Conversation creation      → Lines 337-342
   📝 Message display            → Line 345
   📝 Message persistence        → Line 346
```

### Planning & Intent Detection

```
📂 services/IntentDetectionService.js
   📝 detectIntent()             → Main function
   📝 IntentCategory enum        → Category definitions

📂 services/PlanningService.js
   📝 needsPlanning()            → Decision logic
   📝 generatePlan()             → Plan generation
   📝 validateStepResult()       → Step validation
   📝 formatPlan()               → Display formatting
```

### System Prompt Building

```
📂 services/EnhancedPromptService.js
   📝 buildSystemPrompt()        → Lines 10-205
   📝 Planning injection         → Lines 167-202
   📝 Statistics loading         → Lines 67-85
   📝 enrichUserMessage()        → Lines 172-196
```

### Provider Management

```
📂 services/ProviderRegistry.js
   📝 getActive()                → Lines 85-87
   📝 setActive()                → Lines 92-100
   📝 register()                 → Registration logic
```

### Provider Implementations

```
📂 services/impl/OpenAIProvider.js
   📝 chat()                     → Lines 84-143 (Native function calling)

📂 services/impl/ClaudeProvider.js
   📝 chat()                     → Native function calling

📂 services/impl/ViByteProvider.js
   📝 chat()                     → Lines 196-323 (Text-based adapter)

📂 services/impl/GeminiProvider.js
   📝 chat()                     → Native function calling
```

### Function Calling

```
📂 components/AIPanel.js
   📝 handleFunctionCallingMode() → Lines 400-553
   📝 Tool iteration loop         → Lines 431-553
   📝 Tool execution              → Lines 460-530

📂 services/FunctionCallingService.js
   📝 executeFunction()           → Lines 55-152
   📝 getAvailableFunctions()     → Function list
```

### Function Registry

```
📂 services/functions/index.js
   📝 allFunctions                → Complete registry

📂 services/functions/AdvancedQueryFunctions.js
   📝 semanticSearch              → Semantic search
   📝 analyzeRelationships        → Relationship analysis

📂 services/functions/ImpactTracingFunctions.js
   📝 traceScreenImpact           → Impact analysis
   📝 generateImpactReport        → Report generation
```

### Conversation Management

```
📂 components/AIConversationManager.js
   📝 createConversation()        → Lines 42-58
   📝 addMessage()                → Lines 64-79

📂 services/ChatHistoryManager.js
   📝 addMessage()                → Lines 168-194 (IndexedDB)
   📝 getChat()                   → Load conversation
   📝 saveChat()                  → Persist conversation
```

### Message Display

```
📂 components/AIChatView.js
   📝 addMessage()                → Lines 18-21
   📝 showTypingIndicator()       → Typing UI
   📝 hideTypingIndicator()       → Hide typing
```

---

## 🎯 Common Tasks

### Task: Add New AI Function

**Steps**:

1. Create function definition in `services/functions/YourFunctions.js`:

```javascript
export const yourFunctions = {
  myNewFunction: {
    name: "myNewFunction",
    description: "What this function does",
    parameters: {
      type: "object",
      properties: {
        param1: { type: "string", description: "..." },
      },
      required: ["param1"],
    },
    handler: async ({ param1 }) => {
      // Implementation
      return { success: true, result: data };
    },
  },
};
```

2. Register in `services/functions/index.js`:

```javascript
import { yourFunctions } from "./YourFunctions.js";

export const allFunctions = {
  // ... existing
  yourCategory: yourFunctions,
};
```

3. Test:

```javascript
// AI will automatically have access
const result = await functionCallingService.executeFunction("myNewFunction", {
  param1: "test",
});
```

### Task: Add New Provider

**Steps**:

1. Create provider class in `services/impl/YourProvider.js`:

```javascript
import { BaseProvider } from "../BaseProvider.js";

export class YourProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "your-provider",
      name: "Your Provider",
      capabilities: {
        chat: true,
        tools: true, // Native function calling
        stream: false,
      },
    });
  }

  async chat(messages, options = {}) {
    // Implementation
  }
}
```

2. Register in `services/ProviderRegistry.js`:

```javascript
import { YourProvider } from "./impl/YourProvider.js";

registerDefaultProviders() {
  this.register(new YourProvider({
    apiKey: localStorage.getItem("your_provider_api_key"),
  }));
}
```

### Task: Customize System Prompt

**Location**: `services/EnhancedPromptService.js` (line 18-64)

```javascript
// Add custom section
systemPrompt += `\n## Your Custom Section\n\n`;
systemPrompt += `Your custom instructions here...\n`;
```

### Task: Modify Planning Logic

**Location**: `services/PlanningService.js`

**1. Adjust Detection**:

```javascript
export function needsPlanning(userMessage, intent) {
  // Add your custom logic
  const customCondition = checkCustomCondition(userMessage);

  if (customCondition) return true;

  // Existing logic...
}
```

**2. Customize Plans**:

```javascript
function generateAnalysisPlan(userMessage, intent) {
  const steps = [];

  // Add your custom steps
  steps.push({
    stepNumber: 1,
    action: "custom_action",
    tool: "yourCustomTool",
    description: "Your step description",
    estimatedTime: 3,
  });

  return steps;
}
```

### Task: Add Error Handling

**Location**: `components/AIPanel.js` (lines 388-395)

```javascript
catch (error) {
  console.error("AI response error:", error);

  // Add custom error handling
  if (error.code === "RATE_LIMIT") {
    this.chatView.addMessage("assistant",
      "⚠️ Rate limit exceeded. Please try again in a moment."
    );
  } else if (error.code === "INVALID_API_KEY") {
    this.chatView.addMessage("assistant",
      "🔑 API key is invalid. Please check your settings."
    );
  } else {
    // Default error
    this.chatView.addMessage("assistant", `Error: ${error.message}`);
  }
}
```

### Task: Enable Streaming

**Location**: `components/AIPanel.js` (lines 436-448)

```javascript
// Check provider streaming capability
if (provider.capabilities.stream) {
  console.log("🌊 Starting streaming response...");

  response = await this.streamingHandler.streamResponseWithTools(
    provider,
    conversationMessages,
    options,
  );
} else {
  // Non-streaming fallback
  response = await provider.chat(conversationMessages, options);
}
```

---

## 🔍 Debugging Guide

### Issue: Message not sending

**Check**:

1. `AIPanel.isProcessing` → Should be `false`
2. Input value → Should not be empty
3. Provider → Should be active and available
4. Console → Check for errors in `handleSendMessage()`

**Debug**:

```javascript
// Add logging in AIPanel.handleSendMessage()
console.log("[Debug] Message:", message);
console.log("[Debug] isProcessing:", this.isProcessing);
console.log("[Debug] Provider:", providerRegistry.getActive());
```

### Issue: AI not responding

**Check**:

1. Provider health → Call `provider.isAvailable()`
2. API key → Verify in settings
3. Network → Check browser console for failed requests
4. Response format → Verify API response structure

**Debug**:

```javascript
// Add logging in AIPanel.getAIResponse()
const provider = providerRegistry.getActive();
console.log("[Debug] Provider:", provider.id, provider.name);

const isAvailable = await provider.isAvailable();
console.log("[Debug] Provider available:", isAvailable);

const response = await provider.chat(messages, options);
console.log("[Debug] API response:", response);
```

### Issue: Functions not executing

**Check**:

1. Function exists → `functionCallingService.hasFunction(name)`
2. Arguments valid → Verify JSON parsing
3. DatabaseManager → Check if method exists
4. Tool calls format → Verify OpenAI format

**Debug**:

```javascript
// Add logging in FunctionCallingService.executeFunction()
console.log("[Debug] Function name:", functionName);
console.log("[Debug] Parameters:", parameters);

const available = this.getAvailableFunctions();
console.log(
  "[Debug] Available functions:",
  available.map((f) => f.name),
);

const result = await executeFunction(functionName, parameters);
console.log("[Debug] Execution result:", result);
```

### Issue: Planning not triggering

**Check**:

1. Intent detection → Verify intent category
2. Planning flag → Check `needsPlanning()` result
3. Query keywords → Add more keywords if needed

**Debug**:

```javascript
// Add logging in handleSendMessage()
const intent = detectIntent(message);
console.log("[Debug] Intent:", intent);

const shouldPlan = needsPlanning(message, intent);
console.log("[Debug] Should plan:", shouldPlan);

if (shouldPlan) {
  const plan = generatePlan(message, intent);
  console.log("[Debug] Generated plan:", plan);
}
```

### Issue: Response not displaying

**Check**:

1. ChatView → Verify container exists
2. Message format → Check role and content
3. Rendering → Verify AIMessageRenderer
4. CSS → Check display properties

**Debug**:

```javascript
// Add logging in AIChatView.addMessage()
console.log("[Debug] Adding message:", role, content);
console.log("[Debug] Container:", this.container);
console.log("[Debug] Messages count:", this.messages.length);
```

---

## 📝 Code Snippets

### Get Current Conversation Messages

```javascript
// In AIPanel
const conversationMessages = this.chatView.getMessages().map((msg) => ({
  role: msg.role,
  content: msg.content,
}));
```

### Execute Function Manually

```javascript
import functionCallingService from "./services/FunctionCallingService.js";

const result = await functionCallingService.executeFunction("semanticSearch", {
  query: "authentication",
  entityTypes: ["projects"],
  limit: 10,
});

console.log("Result:", result);
```

### Build Custom Prompt

```javascript
import { buildSystemPrompt } from "./services/EnhancedPromptService.js";

const systemPrompt = await buildSystemPrompt({
  includeStatistics: true,
  includeExamples: false,
  includeGuidelines: true,
  includePlanning: false,
  userContext: {
    currentProject: { id: 1, name: "Auth Service" },
  },
});

console.log("System prompt:", systemPrompt);
```

### Detect Intent

```javascript
import { detectIntent } from "./services/IntentDetectionService.js";

const intent = detectIntent("Analyze impact of adding phone field");

console.log("Intent:", intent);
// {
//   category: "ANALYSIS",
//   confidence: 0.85,
//   entities: ["screens", "database"],
//   suggestedTools: ["semanticSearch", "analyzeRelationships"]
// }
```

### Generate Plan

```javascript
import { generatePlan, needsPlanning } from "./services/PlanningService.js";

const message = "How does login screen affect other components?";
const intent = detectIntent(message);

if (needsPlanning(message, intent)) {
  const plan = generatePlan(message, intent);
  console.log("Execution plan:", plan);
}
```

---

## 🎓 Learning Path

### Level 1: Understanding the Flow

1. Read [MESSAGE_FLOW_ARCHITECTURE.md](./MESSAGE_FLOW_ARCHITECTURE.md)
2. View [MESSAGE_FLOW_DIAGRAM.md](./MESSAGE_FLOW_DIAGRAM.md)
3. Trace through `AIPanel.handleSendMessage()` in debugger

### Level 2: Function Calling

1. Read function definitions in `services/functions/`
2. Test existing functions via AI chat
3. Add a simple custom function
4. Test your function

### Level 3: Planning Integration

1. Read [AI_PLANNING_STRATEGY_GUIDE.md](./AI_PLANNING_STRATEGY_GUIDE.md)
2. Study `PlanningService.js`
3. Test with complex queries
4. Customize planning logic

### Level 4: Provider Integration

1. Study existing providers in `services/impl/`
2. Understand BaseProvider interface
3. Add a new provider (e.g., local LLM)
4. Test with your provider

### Level 5: Advanced Customization

1. Study `EnhancedPromptService.js`
2. Customize system prompts
3. Add custom intent categories
4. Implement adaptive planning

---

## 📊 Performance Tips

### Optimize Response Time

```javascript
// 1. Cache system statistics
let cachedStats = null;
let cacheTime = 0;
const CACHE_TTL = 60000; // 1 minute

async function getSystemStatistics() {
  const now = Date.now();
  if (cachedStats && now - cacheTime < CACHE_TTL) {
    return cachedStats;
  }

  cachedStats = await loadSystemStatistics();
  cacheTime = now;
  return cachedStats;
}

// 2. Parallel tool execution (for independent tools)
const results = await Promise.all(
  toolCalls.map((tc) =>
    functionCallingService.executeFunction(
      tc.function.name,
      tc.function.arguments,
    ),
  ),
);

// 3. Use streaming for better perceived performance
if (provider.capabilities.stream) {
  await this.streamingHandler.streamResponseWithTools(
    provider,
    messages,
    options,
  );
}
```

### Reduce Token Usage

```javascript
// 1. Limit conversation history
const recentMessages = messages.slice(-10); // Last 10 messages only

// 2. Summarize long tool results
const summarizedResult = {
  success: result.success,
  count: result.data?.length || 0,
  summary: "Found X items...",
};

// 3. Use lower temperature for factual queries
const options = {
  temperature: intent.category === "INFORMATION" ? 0.3 : 0.7,
  maxTokens: intent.category === "SEARCH" ? 512 : 2048,
};
```

---

## 🔗 Quick Links

### Documentation

- [MESSAGE_FLOW_ARCHITECTURE.md](./MESSAGE_FLOW_ARCHITECTURE.md) - Complete flow documentation
- [MESSAGE_FLOW_DIAGRAM.md](./MESSAGE_FLOW_DIAGRAM.md) - Visual diagrams
- [AI_PLANNING_STRATEGY_GUIDE.md](./AI_PLANNING_STRATEGY_GUIDE.md) - Planning guide
- [IMPACT_ANALYSIS_TRACEABILITY_GUIDE.md](./IMPACT_ANALYSIS_TRACEABILITY_GUIDE.md) - Impact analysis

### Source Code

- [components/AIPanel.js](../components/AIPanel.js) - Main UI
- [services/FunctionCallingService.js](../services/FunctionCallingService.js) - Function execution
- [services/PlanningService.js](../services/PlanningService.js) - Planning logic
- [services/functions/index.js](../services/functions/index.js) - Function registry

### Examples

- [AIPanel.planning-integration.example.js](../examples/AIPanel.planning-integration.example.js) - Planning integration
- [AIPanel.enhanced.example.js](./examples/AIPanel.enhanced.example.js) - Enhanced features

---

**Version**: 1.0.0  
**Created**: November 10, 2025  
**Type**: Quick Reference Card

**Print this for quick desk reference! 📄**
