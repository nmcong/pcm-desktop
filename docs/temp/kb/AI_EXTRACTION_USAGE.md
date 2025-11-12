# 🤖 AI Extraction Service - Usage Guide

## 📋 Overview

`AIExtractionService` uses **ProviderRegistry** to automatically extract knowledge items from text using any configured AI provider (OpenAI, Claude, Gemini, ViByte, HuggingFace).

### ✨ Features

- ✅ **Multi-Provider Support** - Works with any AI provider
- ✅ **Auto Active Provider** - Uses currently active provider
- ✅ **Mock Mode** - Test without real LLM
- ✅ **Validation** - Auto-validates extracted items
- ✅ **Batch Processing** - Process multiple texts
- ✅ **Smart Parsing** - Handles JSON in markdown blocks

---

## 🚀 Quick Start

### Basic Usage

```javascript
import aiExtractionService from './services/AIExtractionService.js';

// 1. Check if available
const isAvailable = await aiExtractionService.isAvailable();
if (!isAvailable) {
  alert('Please configure AI provider in Settings');
  return;
}

// 2. Extract ideas from text
const longText = `
Troubleshooting Guide:

1. Login Issues
- Check username/password
- Clear browser cache
- Verify network connection

2. Vector Database Problems
- Initialize before using
- Verify TensorFlow.js loaded
- Check IndexedDB enabled
`;

const items = await aiExtractionService.extractIdeas(longText, {
  maxItems: 5,
  temperature: 0.3
});

// 3. Review extracted items
console.log(`Extracted ${items.length} items:`);
items.forEach(item => {
  console.log(`- ${item.problem}`);
  console.log(`  Solution: ${item.solution.substring(0, 50)}...`);
});
```

---

## 🔧 API Reference

### `extractIdeas(text, options)`

Extract knowledge items from text using AI.

**Parameters:**

- `text` (string) - Text to extract ideas from
- `options` (object) - Optional configuration
  - `maxItems` (number) - Max items to extract (default: 10)
  - `temperature` (number) - AI temperature 0-1 (default: 0.3)
  - `providerId` (string) - Specific provider ID or null for active
  - `model` (string) - Specific model or null for provider default

**Returns:** `Promise<Array<ExtractedItem>>`

**Example:**

```javascript
// Use active provider
const items = await aiExtractionService.extractIdeas(text);

// Use specific provider
const items = await aiExtractionService.extractIdeas(text, {
  providerId: 'openai',
  model: 'gpt-4',
  temperature: 0.2,
  maxItems: 10
});
```

### `getAvailableProviders()`

Get list of available (configured) AI providers.

**Returns:** `Promise<Array<Provider>>`

**Example:**

```javascript
const providers = await aiExtractionService.getAvailableProviders();
console.log(`Available: ${providers.map((p) => p.name).join(", ")}`);
```

### `getActiveProvider()`

Get currently active provider info.

**Returns:** `Object|null`

**Example:**

```javascript
const provider = aiExtractionService.getActiveProvider();
if (provider) {
  console.log(`Using: ${provider.name}`);
  console.log(`Models: ${provider.models.join(", ")}`);
}
```

### `isAvailable()`

Check if AI extraction is available (has configured provider).

**Returns:** `Promise<boolean>`

**Example:**

```javascript
if (await aiExtractionService.isAvailable()) {
  showAIExtractButton();
} else {
  showConfigureProviderMessage();
}
```

### `postProcess(items, options)`

Post-process extracted items with metadata.

**Parameters:**

- `items` (Array) - Extracted items
- `options` (object) - Options
  - `defaultCategoryId` - Default category
  - `additionalTags` - Tags to add
  - `provider` - Provider name
  - `model` - Model name

**Returns:** `Array<ProcessedItem>`

**Example:**

```javascript
const processed = aiExtractionService.postProcess(items, {
  defaultCategoryId: "troubleshooting",
  additionalTags: ["auto-extracted", "ai-generated"],
  provider: "openai",
  model: "gpt-4",
});
```

### `estimateIdeas(text)`

Estimate number of ideas that can be extracted.

**Returns:** `number` (1-20)

**Example:**

```javascript
const estimated = aiExtractionService.estimateIdeas(longText);
console.log(`Can extract ~${estimated} ideas`);
```

### `splitText(text, maxChunkSize)`

Split long text into processable chunks.

**Returns:** `Array<string>`

**Example:**

```javascript
const chunks = aiExtractionService.splitText(veryLongText, 4000);
for (const chunk of chunks) {
  const items = await aiExtractionService.extractIdeas(chunk);
  // Process items...
}
```

---

## 💡 Usage Examples

### Example 1: Full Workflow with Error Handling

```javascript
import aiExtractionService from "./services/AIExtractionService.js";
import knowledgeVectorService from "./services/KnowledgeVectorService.js";

async function extractAndSave(text, categoryId) {
  try {
    // 1. Check availability
    if (!(await aiExtractionService.isAvailable())) {
      throw new Error(
        "No AI provider configured. Please set up in AI Settings.",
      );
    }

    // 2. Show active provider
    const provider = aiExtractionService.getActiveProvider();
    console.log(`Using: ${provider.name} (${provider.defaultModel})`);

    // 3. Estimate items
    const estimated = aiExtractionService.estimateIdeas(text);
    console.log(`Estimating ~${estimated} ideas...`);

    // 4. Extract
    const items = await aiExtractionService.extractIdeas(text, {
      maxItems: estimated,
      temperature: 0.3,
    });

    // 5. Post-process
    const processed = aiExtractionService.postProcess(items, {
      defaultCategoryId: categoryId,
      additionalTags: ["ai-extracted"],
      provider: provider.name,
      model: provider.defaultModel,
    });

    // 6. Save to database
    const saved = [];
    for (const item of processed) {
      const savedItem = await databaseManager.create("knowledge_base", item);

      // 7. Create vector (background)
      await knowledgeVectorService.indexKnowledgeItem(savedItem);

      saved.push(savedItem);
    }

    console.log(`✅ Saved ${saved.length} knowledge items`);
    return saved;
  } catch (error) {
    console.error("❌ Extraction failed:", error);
    throw error;
  }
}

// Usage
await extractAndSave(longText, "troubleshooting");
```

### Example 2: With Provider Selection UI

```javascript
async function showProviderSelector() {
  // Get available providers
  const providers = await aiExtractionService.getAvailableProviders();

  if (providers.length === 0) {
    alert("No AI providers configured. Please set up in AI Settings.");
    return;
  }

  // Show selector
  const selected = await showModal({
    title: "Select AI Provider",
    options: providers.map((p) => ({
      id: p.id,
      label: `${p.name} (${p.models.join(", ")})`,
    })),
  });

  // Extract with selected provider
  const items = await aiExtractionService.extractIdeas(text, {
    providerId: selected.id,
    model: selected.models[0], // Use first model
    maxItems: 10,
  });

  return items;
}
```

### Example 3: Batch Processing Long Documents

```javascript
async function extractFromLongDocument(longText) {
  // 1. Split into chunks
  const chunks = aiExtractionService.splitText(longText, 4000);
  console.log(`Split into ${chunks.length} chunks`);

  // 2. Process each chunk
  const allItems = [];
  for (let i = 0; i < chunks.length; i++) {
    console.log(`Processing chunk ${i + 1}/${chunks.length}...`);

    const items = await aiExtractionService.extractIdeas(chunks[i], {
      maxItems: 5,
      temperature: 0.3,
    });

    allItems.push(...items);

    // Add delay to avoid rate limiting
    await new Promise((resolve) => setTimeout(resolve, 1000));
  }

  console.log(`✅ Total extracted: ${allItems.length} items`);
  return allItems;
}
```

### Example 4: Mock Mode for Testing

```javascript
// Enable mock mode (no real API calls)
window.mockAIExtraction = true;

// Test extraction
const items = await aiExtractionService.extractIdeas(`
Test text here...
`);

console.log("Mock items:", items);
// Returns pre-defined mock items

// Disable mock mode
window.mockAIExtraction = false;
```

---

## 🎯 Integration with Knowledge Base

### Auto-Extract Button

```javascript
// In KnowledgeBasePage.js
async showAIExtractModal() {
  // Check availability
  if (!(await aiExtractionService.isAvailable())) {
    alert('Please configure AI provider in Settings first');
    return;
  }

  // Show modal with textarea
  const modal = new AITextExtractorModal({
    onExtract: async (text, options) => {
      try {
        // Extract
        const items = await aiExtractionService.extractIdeas(text, {
          maxItems: options.maxItems,
          temperature: options.temperature
        });

        // Show preview
        return items;
      } catch (error) {
        alert(`Extraction failed: ${error.message}`);
      }
    },

    onSave: async (items, categoryId) => {
      // Post-process
      const provider = aiExtractionService.getActiveProvider();
      const processed = aiExtractionService.postProcess(items, {
        defaultCategoryId: categoryId,
        provider: provider.name,
        model: provider.defaultModel
      });

      // Save all
      for (const item of processed) {
        const saved = await databaseManager.create('knowledge_base', item);
        await knowledgeVectorService.indexKnowledgeItem(saved);
      }

      // Refresh
      await this.loadData();
    }
  });

  modal.show();
}
```

---

## ⚠️ Error Handling

### Common Errors

#### 1. No Provider Configured

```javascript
try {
  const items = await aiExtractionService.extractIdeas(text);
} catch (error) {
  if (error.message.includes("No AI provider available")) {
    // Guide user to settings
    showNotification("Please configure AI provider in Settings", "warning");
    redirectToSettings();
  }
}
```

#### 2. Provider Not Available

```javascript
// Check before using
const provider = aiExtractionService.getActiveProvider();
if (provider) {
  const isAvailable = await provider.isAvailable();
  if (!isAvailable) {
    showNotification(`${provider.name} API key not configured`, "error");
  }
}
```

#### 3. Validation Failed

```javascript
try {
  const items = await aiExtractionService.extractIdeas(text);
} catch (error) {
  if (error.message.includes("Validation failed")) {
    console.error("AI returned invalid data:", error);
    showNotification("AI response was invalid. Please try again.", "error");
  }
}
```

#### 4. Rate Limiting

```javascript
async function extractWithRetry(text, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await aiExtractionService.extractIdeas(text);
    } catch (error) {
      if (error.message.includes("rate limit") && i < maxRetries - 1) {
        const delay = Math.pow(2, i) * 1000; // Exponential backoff
        console.warn(`Rate limited, retrying in ${delay}ms...`);
        await new Promise((resolve) => setTimeout(resolve, delay));
      } else {
        throw error;
      }
    }
  }
}
```

---

## 🔧 Configuration

### Setting Active Provider

```javascript
// In AI Settings or before extraction
import providerRegistry from "../ai/services/ProviderRegistry.js";

// Set active provider
providerRegistry.setActive("openai"); // or 'claude', 'gemini', etc.

// Now AIExtractionService will use this provider
const items = await aiExtractionService.extractIdeas(text);
```

### Provider-Specific Options

```javascript
// OpenAI
const items = await aiExtractionService.extractIdeas(text, {
  providerId: 'openai',
  model: 'gpt-4-turbo',
  temperature: 0.2
});

// Claude
const items = await aiExtractionService.extractIdeas(text, {
  providerId: 'claude',
  model: 'claude-3-sonnet-20240229',
  temperature: 0.3
});

// Gemini
const items = await aiExtractionService.extractIdeas(text, {
  providerId: 'gemini',
  model: 'gemini-1.5-pro',
  temperature: 0.3
});

// ViByte (local)
const items = await aiExtractionService.extractIdeas(text, {
  providerId: 'vibyte-cloud',
  model: 'qwen2.5:7b',
  temperature: 0.4
});
```

---

## 📊 Output Format

### Extracted Item Structure

```javascript
{
  "problem": "Làm thế nào để khởi tạo Vector Database?",
  "solution": "1. Import VectorDatabaseService\n2. Call initialize()\n3. Check isInitialized",
  "tags": ["vector-db", "initialization", "tutorial"],
  "priority": false
}
```

### After Post-Processing

```javascript
{
  "id": "extracted_1699123456789_abc123",
  "problem": "...",
  "solution": "...",
  "tags": ["vector-db", "initialization", "auto-extracted"],
  "priority": false,
  "categoryId": "troubleshooting",
  "extractedBy": "ai",
  "extractionMetadata": {
    "timestamp": 1699123456789,
    "provider": "openai",
    "model": "gpt-4"
  }
}
```

---

## ✅ Best Practices

### 1. Always Check Availability

```javascript
if (await aiExtractionService.isAvailable()) {
  // Proceed with extraction
} else {
  // Show configuration prompt
}
```

### 2. Show Provider Info to User

```javascript
const provider = aiExtractionService.getActiveProvider();
showNotification(`Using ${provider.name} for extraction`);
```

### 3. Use Lower Temperature for Extraction

```javascript
// More consistent output
const items = await aiExtractionService.extractIdeas(text, {
  temperature: 0.2, // Lower = more consistent
});
```

### 4. Handle Long Texts

```javascript
if (text.length > 4000) {
  const chunks = aiExtractionService.splitText(text);
  // Process chunks separately
} else {
  // Process directly
}
```

### 5. Validate Before Saving

```javascript
const items = await aiExtractionService.extractIdeas(text);
// Validation happens automatically in extractIdeas
// If validation fails, it throws error
```

---

## 🎉 Summary

### What Changed

**Before (Mock only):**

```javascript
// Required custom API integration
throw new Error("AI service not configured");
```

**After (ProviderRegistry):**

```javascript
// Works with any configured provider
const provider = providerRegistry.getActive();
const response = await provider.chat(messages, options);
```

### Benefits

- ✅ **Works with all providers** - OpenAI, Claude, Gemini, ViByte, etc.
- ✅ **Auto provider selection** - Uses active provider from AI Settings
- ✅ **No duplicate code** - Reuses existing provider system
- ✅ **Consistent behavior** - Same API for all providers
- ✅ **Easy testing** - Mock mode still available

---

**Ready to use! 🚀**

See `AI_VECTOR_QUICK_START.md` for full integration example.
