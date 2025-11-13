# 🧠 Knowledge Base - AI Vector Architecture

## 📋 New Features

### 1. AI Text Extraction

- **Input**: Long text/document
- **Process**: LLM extracts key ideas/points
- **Output**: Multiple knowledge items

### 2. Vector Indexing

- **Auto-generate**: Vector for each knowledge item
- **Storage**: IndexedDB + Vector DB
- **Dimension**: 512D (TensorFlow.js)

### 3. Semantic Search

- **Query**: Natural language
- **Match**: Semantic similarity
- **Results**: Ranked by relevance

### 4. Web Worker

- **Background**: Vector generation
- **Non-blocking**: UI remains responsive
- **Batch**: Multiple items at once

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     UI Layer                             │
├─────────────────────────────────────────────────────────┤
│  KnowledgeBasePage                                      │
│  ├── Search (with semantic option)                     │
│  ├── Grid View                                         │
│  └── AI Extract Button                                 │
├─────────────────────────────────────────────────────────┤
│  AITextExtractorModal                                   │
│  ├── Text Input (large textarea)                       │
│  ├── LLM Options (model, temperature)                  │
│  ├── Preview Extracted Items                           │
│  └── Batch Import Button                               │
└─────────────────────────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                  Service Layer                           │
├─────────────────────────────────────────────────────────┤
│  KnowledgeVectorService                                 │
│  ├── indexKnowledgeItem()                              │
│  ├── searchSemantic()                                  │
│  ├── batchIndex()                                      │
│  └── removeVector()                                    │
├─────────────────────────────────────────────────────────┤
│  AIExtractionService                                    │
│  ├── extractIdeas(text, options)                       │
│  ├── parseAIResponse()                                 │
│  └── validateExtractedItems()                          │
└─────────────────────────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                 Vector Layer                             │
├─────────────────────────────────────────────────────────┤
│  VectorDatabaseService (Existing)                       │
│  ├── addVector()                                       │
│  ├── search()                                          │
│  └── getStats()                                        │
└─────────────────────────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│              Web Worker Layer (Optional)                 │
├─────────────────────────────────────────────────────────┤
│  knowledge-vector-worker.js                             │
│  ├── generateEmbedding(text)                           │
│  ├── batchGenerate(texts[])                            │
│  └── searchSimilar(query, vectors)                     │
└─────────────────────────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                Storage Layer                             │
├─────────────────────────────────────────────────────────┤
│  IndexedDB                                              │
│  ├── knowledge_base (existing)                         │
│  └── vectors (vector embeddings)                       │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 Data Model

### Knowledge Item (Enhanced)

```typescript
interface KnowledgeItem {
  // Existing fields
  id: string;
  problem: string;
  solution: string;
  categoryId: string;
  tags: string[];
  priority: boolean;

  // New fields for AI
  vectorId?: string; // Reference to vector in Vector DB
  extractedBy?: "manual" | "ai";
  extractionMetadata?: {
    sourceText?: string; // Original text nếu extracted by AI
    aiModel?: string; // Model used for extraction
    confidence?: number; // AI confidence score
  };
}
```

### Vector Document

```typescript
interface VectorDocument {
  id: string; // vectorId
  vector: number[]; // 512D embedding
  text: string; // Combined problem + solution
  type: "knowledge_item";
  metadata: {
    knowledgeItemId: string;
    categoryId: string;
    tags: string[];
    priority: boolean;
    timestamp: number;
  };
  dimension: 512;
}
```

---

## 🔄 Workflows

### Workflow 1: AI Text Extraction

```
User Input
    │
    ▼
┌─────────────────────────────────┐
│ Paste Long Text                 │
│ "Here's a document with..."     │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Send to LLM                     │
│ Prompt: "Extract key ideas..."  │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ LLM Returns JSON                │
│ [                               │
│   {problem, solution},          │
│   {problem, solution},          │
│   ...                           │
│ ]                               │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Preview Extracted Items         │
│ User can edit/remove            │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Batch Import                    │
│ Save to IndexedDB               │
│ Generate vectors (background)   │
└─────────────────────────────────┘
```

### Workflow 2: Vector Indexing

```
Knowledge Item Saved
    │
    ▼
┌─────────────────────────────────┐
│ KnowledgeVectorService          │
│ .indexKnowledgeItem()           │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Combine Text                    │
│ text = problem + "\n" + solution│
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Generate Vector                 │
│ (Web Worker if available)       │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Save to Vector DB               │
│ vectorId = "kb_" + itemId       │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Update Knowledge Item           │
│ item.vectorId = vectorId        │
└─────────────────────────────────┘
```

### Workflow 3: Semantic Search

```
User Enters Query
    │
    ▼
┌─────────────────────────────────┐
│ "How to fix login error?"       │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Generate Query Vector           │
│ (Web Worker if available)       │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Search Vector DB                │
│ Find similar vectors            │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Rank by Similarity              │
│ [                               │
│   {item, similarity: 0.92},     │
│   {item, similarity: 0.85},     │
│   ...                           │
│ ]                               │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Display Results                 │
│ Show similarity scores          │
└─────────────────────────────────┘
```

---

## 💻 Web Worker Implementation

### Benefits

1. **Non-blocking UI**: Vector generation doesn't freeze browser
2. **Parallel processing**: Multiple embeddings at once
3. **Better UX**: Loading indicators, progress bars
4. **Resource management**: Isolate heavy computation

### Structure

```javascript
// knowledge-vector-worker.js
self.onmessage = async (e) => {
  const { action, data } = e.data;

  switch (action) {
    case "INIT":
      await initializeTensorFlow();
      self.postMessage({ type: "READY" });
      break;

    case "GENERATE_EMBEDDING":
      const embedding = await generateEmbedding(data.text);
      self.postMessage({
        type: "EMBEDDING_GENERATED",
        vectorId: data.vectorId,
        embedding,
      });
      break;

    case "BATCH_GENERATE":
      const results = await batchGenerate(data.items);
      self.postMessage({
        type: "BATCH_COMPLETED",
        results,
      });
      break;
  }
};
```

### Usage

```javascript
// In KnowledgeVectorService.js
class KnowledgeVectorService {
  constructor() {
    this.worker = null;
    this.useWorker = typeof Worker !== "undefined";
  }

  async initializeWorker() {
    if (this.useWorker) {
      this.worker = new Worker("./workers/knowledge-vector-worker.js");
      this.worker.onmessage = this.handleWorkerMessage.bind(this);
    }
  }

  async indexKnowledgeItem(item) {
    if (this.worker) {
      // Use worker (non-blocking)
      return new Promise((resolve) => {
        this.worker.postMessage({
          action: "GENERATE_EMBEDDING",
          data: { text, vectorId },
        });
        this.pendingPromises.set(vectorId, resolve);
      });
    } else {
      // Direct call (fallback)
      return await vectorEmbeddingService.embed(text);
    }
  }
}
```

---

## 🎯 Implementation Plan

### Phase 1: Core Vector Service (2-3 hours)

- [ ] Create `KnowledgeVectorService.js`
- [ ] Implement `indexKnowledgeItem()`
- [ ] Implement `searchSemantic()`
- [ ] Integrate with existing VectorDatabaseService

### Phase 2: AI Text Extraction (3-4 hours)

- [ ] Create `AIExtractionService.js`
- [ ] Design LLM prompt for extraction
- [ ] Create `AITextExtractorModal.js`
- [ ] Implement preview & edit UI
- [ ] Add batch import

### Phase 3: Enhanced Search UI (2-3 hours)

- [ ] Add semantic search toggle
- [ ] Show similarity scores
- [ ] Highlight matching sections
- [ ] Add "Find Similar" button on items

### Phase 4: Web Worker (2-3 hours)

- [ ] Create `knowledge-vector-worker.js`
- [ ] Implement worker initialization
- [ ] Add progress tracking
- [ ] Fallback to main thread if worker unavailable

### Phase 5: Testing & Polish (2-3 hours)

- [ ] Test all workflows
- [ ] Performance optimization
- [ ] Error handling
- [ ] Documentation

**Total Estimate: 11-16 hours**

---

## 🔧 API Design

### KnowledgeVectorService

```javascript
class KnowledgeVectorService {
  // Initialize service
  async initialize();

  // Index a single item
  async indexKnowledgeItem(item: KnowledgeItem): Promise<string>;

  // Batch index multiple items
  async batchIndex(items: KnowledgeItem[]): Promise<string[]>;

  // Semantic search
  async searchSemantic(query: string, options?: SearchOptions): Promise<SearchResult[]>;

  // Remove vector
  async removeVector(vectorId: string): Promise<void>;

  // Get stats
  async getStats(): Promise<VectorStats>;
}
```

### AIExtractionService

```javascript
class AIExtractionService {
  // Extract ideas from text
  async extractIdeas(text: string, options?: ExtractionOptions): Promise<ExtractedItem[]>;

  // Parse AI response
  parseAIResponse(response: string): ExtractedItem[];

  // Validate extracted items
  validateExtractedItems(items: ExtractedItem[]): ValidationResult;
}
```

---

## 📝 Prompt Engineering

### LLM Prompt for Extraction

```
You are a knowledge extraction assistant. Extract key ideas from the following text
and format them as problem-solution pairs for a knowledge base.

Rules:
1. Each idea should be self-contained
2. Problem should be clear and specific
3. Solution should be actionable
4. Extract 3-10 ideas (depending on text length)
5. Return ONLY valid JSON array

Format:
[
  {
    "problem": "Clear problem statement",
    "solution": "Detailed solution with steps",
    "tags": ["tag1", "tag2"],
    "priority": false
  }
]

Text:
{USER_INPUT_TEXT}

Extract ideas:
```

---

## 🎨 UI/UX Enhancements

### New UI Elements

1. **AI Extract Button** (in toolbar)

   ```html
   <button class="btn-primary">🤖 AI Extract</button>
   ```

2. **Search Mode Toggle**

   ```html
   <div class="search-mode">
     <button class="active">🔍 Keyword</button>
     <button>🧠 Semantic</button>
   </div>
   ```

3. **Similarity Score Badge**

   ```html
   <span class="similarity-badge"> 92% match </span>
   ```

4. **Processing Indicator**
   ```html
   <div class="processing-status">
     <div class="spinner"></div>
     <span>Generating vectors... (3/10)</span>
   </div>
   ```

---

## ⚠️ Considerations

### Performance

- **Vector generation**: ~100-200ms per item
- **Batch of 10**: ~2 seconds (with worker)
- **Search**: ~80ms for 1000 vectors
- **Cache**: Reuse vectors when text unchanged

### Storage

- **1 item**: ~2KB (512D vector)
- **1000 items**: ~2MB
- **Acceptable**: Up to 10,000 items (~20MB)

### Fallbacks

1. **No TensorFlow**: Disable semantic search, keep keyword search
2. **No Web Worker**: Use main thread (with loading indicator)
3. **No LLM**: Manual entry still works
4. **API errors**: Show clear error messages

---

## 🚀 Quick Start Example

```javascript
// 1. Initialize
await knowledgeVectorService.initialize();

// 2. Index existing items
const items = await databaseManager.getAll("knowledge_base");
await knowledgeVectorService.batchIndex(items);

// 3. Semantic search
const results = await knowledgeVectorService.searchSemantic("login error", {
  limit: 5,
  threshold: 0.7,
});

// 4. Display results
results.forEach((result) => {
  console.log(
    `${result.item.problem} (${Math.round(result.similarity * 100)}% match)`,
  );
});
```

---

## ✅ Success Criteria

1. ✅ User can paste text and AI extracts ideas
2. ✅ Each knowledge item has vector
3. ✅ Semantic search works and ranks by similarity
4. ✅ Web Worker doesn't block UI
5. ✅ Graceful fallbacks for missing features
6. ✅ Performance: <3s for batch of 10 items

---

**Ready to implement? Let's start with Phase 1! 🚀**
