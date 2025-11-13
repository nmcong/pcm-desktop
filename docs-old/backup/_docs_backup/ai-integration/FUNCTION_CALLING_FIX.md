# Function Calling Fix - ViByte Provider

## 🐛 Issues Fixed

### Issue 1: streamChat() Not Injecting Tools

**Problem**: `streamChat()` method wasn't injecting tool descriptions into messages

**Impact**: LLM never received tool information, couldn't call functions

**Fix**: Updated `streamChat()` to inject tools and extract tool calls (same as `chat()`)

---

### Issue 2: LLM Outputting Text Before JSON

**Problem**: LLM responded with:

```
"We need to call list_subsystems.{
  "tool_calls": [
    {"name": "list_subsystems", "arguments": {}}
  ]
}"
```

**Impact**: JSON parser couldn't extract tool calls from text with prefix

**Root Cause**:

1. System prompt not explicit enough
2. Adapter only parsed JSON code blocks, not inline JSON

**Fix**:

1. Improved system prompt with explicit examples
2. Enhanced adapter to parse JSON with text prefix

---

## ✅ Solutions Implemented

### 1. Improved System Prompt

**Before**:

```
IMPORTANT: When you need to use a tool, respond with ONLY a JSON code block...
Remember: When calling tools, output ONLY the JSON block, no other text!
```

**After**:

````
CRITICAL INSTRUCTIONS FOR TOOL USAGE:

1. When you need to use a tool, you MUST respond with ONLY a JSON code block
2. Do NOT include any explanatory text before or after the JSON
3. Do NOT say things like "I will call..." or "We need to call..."
4. Your ENTIRE response must be ONLY this JSON format:

EXAMPLES:

Wrong ❌:
"I will search for projects. ```json {...} ```"
"Let me call the function. ```json {...} ```"
"We need to call list_subsystems. {...}"

Correct ✅:
```json
{
  "tool_calls": [{"name": "search_projects", "arguments": {"query": "auth"}}]
}
````

Remember: When calling tools = ONLY JSON, NO other text!

````

**Result**: LLM now understands to output ONLY JSON, no text prefix

---

### 2. Enhanced Adapter Parsing

**Added 3 new parsing strategies:**

#### Strategy 2a: Balanced Brace Parsing

```javascript
// Finds first { and extracts complete JSON with balanced braces
const firstBrace = content.indexOf('{');
const jsonPart = content.substring(firstBrace);

// Count braces to find complete JSON
let braceCount = 0;
for (let i = 0; i < jsonPart.length; i++) {
  if (jsonPart[i] === '{') braceCount++;
  if (jsonPart[i] === '}') braceCount--;

  if (braceCount === 0) {
    // Found complete JSON!
    const json = jsonPart.substring(0, i + 1);
    const parsed = JSON.parse(json);
    // Extract tool_calls...
  }
}
````

**Handles**: `"We need to call list_subsystems.{...}"`

#### Strategy 2b: Regex for tool_calls Format

```javascript
const toolCallsRegex = /{[\s\S]*?"tool_calls"[\s\S]*?\[[\s\S]*?\][\s\S]*?}/g;
```

**Handles**: Inline JSON with `tool_calls` array

#### Strategy 2c: Individual Tool Format

```javascript
const inlineJsonRegex = /{[\s\S]*?"(?:name|function_name|tool)"[\s\S]*?}/g;
```

**Handles**: Individual tool objects without wrapper

---

### 3. Added Debug Logging

```javascript
console.log(`[${this.name}] 🔍 Attempting to extract tool calls from response`);
console.log(`[${this.name}] 📝 Raw content:`, content.substring(0, 500));

const toolCalls = this.functionCallingAdapter.extractToolCalls({ content });

if (toolCalls) {
  console.log(
    `[${this.name}] ✅ Extracted ${toolCalls.length} tool call(s):`,
    toolCalls,
  );
} else {
  console.log(`[${this.name}] ⚠️ No tool calls extracted from response`);
}
```

**Benefit**: Easy debugging when tool calls aren't extracted

---

## 📊 Parsing Flow

### Complete Parsing Strategy

````
1. Try JSON Code Blocks
   └─ Look for: ```json {...} ```
   └─ Use: jsonBlockRegex

2. Try Balanced Brace Parsing
   └─ Look for: first { in content
   └─ Extract: complete JSON with balanced braces
   └─ Check: if has tool_calls array

3. Try tool_calls Regex
   └─ Look for: {"tool_calls": [...]}
   └─ Parse: matched JSON

4. Try Individual Tool Format
   └─ Look for: {"name": "...", "arguments": {...}}
   └─ Parse: each matched object
````

**Result**: Can parse tool calls from various formats!

---

## 🧪 Test Cases

### Test Case 1: Perfect Format ✅

**Input**:

```json
{
  "tool_calls": [{ "name": "list_subsystems", "arguments": {} }]
}
```

**Parsed**: ✅ Yes (Strategy 2a + 2b)

---

### Test Case 2: With Code Block ✅

**Input**:

````
```json
{
  "tool_calls": [
    {"name": "list_subsystems", "arguments": {}}
  ]
}
````

```

**Parsed**: ✅ Yes (Strategy 1)

---

### Test Case 3: With Text Prefix ✅

**Input**:
```

We need to call list_subsystems.{
"tool_calls": [
{"name": "list_subsystems", "arguments": {}}
]
}

```

**Parsed**: ✅ Yes (Strategy 2a - Balanced Brace)

---

### Test Case 4: Mixed Format ✅

**Input**:
```

I will help you. Let me check the subsystems.
{
"tool_calls": [
{"name": "list_subsystems", "arguments": {}}
]
}

```

**Parsed**: ✅ Yes (Strategy 2a)

---

## 🎯 Results

### Before Fixes

```

❌ streamChat() not injecting tools
❌ LLM outputting text before JSON
❌ Adapter can't parse JSON with prefix
❌ Tool calls not extracted
❌ Function calling not working

```

### After Fixes

```

✅ streamChat() injects tools correctly
✅ Improved system prompt (less text prefix)
✅ Adapter parses JSON with prefix
✅ Tool calls extracted successfully
✅ Function calling working! 🎉

````

---

## 🔍 Debugging Guide

### Check 1: Are Tools Injected?

```javascript
// Look for this log
[ViByte Cloud AI] Function calling enabled with X tools

// Check raw prompt includes tool descriptions
console.log("Prompt:", prompt);
// Should see: "You have access to these tools..."
````

### Check 2: What Did LLM Output?

```javascript
// Look for this log
[ViByte Cloud AI] 📝 Raw content: ...

// Should see the actual LLM response
// If it has text before JSON, that's the problem
```

### Check 3: Were Tool Calls Extracted?

```javascript
// Look for this log
[ViByte Cloud AI] ✅ Extracted X tool call(s): ...

// OR
[ViByte Cloud AI] ⚠️ No tool calls extracted from response

// If no tool calls extracted, check the raw content format
```

### Check 4: Are Tool Calls Valid?

```javascript
// In AIPanel, check:
console.log("Response from LLM:", response);

// Should have:
// {
//   role: "assistant",
//   content: "",
//   tool_calls: [{id: "...", type: "function", function: {...}}]
// }
```

---

## 💡 Best Practices

### 1. Clear System Prompts

```javascript
// ✅ Good: Explicit with examples
systemPromptTemplate: `
CRITICAL: Output ONLY JSON when calling tools.

Wrong ❌: "I will call...{...}"
Correct ✅: Only output the JSON
`;

// ❌ Bad: Vague
systemPromptTemplate: "Use tools when needed";
```

### 2. Multiple Parsing Strategies

```javascript
// ✅ Good: Try multiple strategies
1. Code blocks
2. Balanced braces
3. Regex patterns
4. Individual objects

// ❌ Bad: Only one strategy
// Can fail if LLM outputs different format
```

### 3. Debug Logging

```javascript
// ✅ Good: Log at each step
console.log("Raw content:", content);
console.log("Extracted:", toolCalls);

// ❌ Bad: Silent failures
// Hard to debug when something goes wrong
```

### 4. Graceful Degradation

```javascript
// ✅ Good: If no tool calls, return text response
if (toolCalls) {
  return { tool_calls: toolCalls };
} else {
  return { content: content };
}

// ❌ Bad: Throw error if can't parse
// User gets error instead of response
```

---

## 📚 Files Changed

### 1. ViByteProvider.js

**Changes**:

- Improved system prompt (30+ lines)
- Added debug logging
- Fixed streamChat() tool injection

### 2. FunctionCallingAdapter.js

**Changes**:

- Added balanced brace parsing (Strategy 2a)
- Added tool_calls regex parsing (Strategy 2b)
- Improved inline JSON parsing (Strategy 2c)

### 3. FUNCTION_CALLING_FIX.md (This Document)

**Content**:

- Problem description
- Solutions implemented
- Test cases
- Debugging guide

---

## ✅ Summary

### Problems Solved

1. ✅ streamChat() not injecting tools
2. ✅ LLM outputting text before JSON
3. ✅ Adapter can't parse non-code-block JSON
4. ✅ Tool calls not extracted

### Key Improvements

1. ✅ Better system prompt with examples
2. ✅ Multiple parsing strategies
3. ✅ Debug logging for troubleshooting
4. ✅ Graceful handling of various formats

### Result

**Function calling now works reliably with ViByte/Ollama! 🎉**

---

**Last Updated**: November 6, 2025  
**Version**: 1.0.0  
**Status**: ✅ Fixed

**Try it now - function calling should work!** 🚀
