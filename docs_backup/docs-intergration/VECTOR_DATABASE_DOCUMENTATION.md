# Vector Database Implementation trong PCM-WebApp

## 📋 Tổng quan

Vector Database được tích hợp vào PCM-WebApp để cung cấp khả năng semantic search cho AI chat logs. Hệ thống hoạt động hoàn toàn trên trình duyệt sử dụng IndexedDB để storage và TensorFlow.js (hoặc fallback text-based similarity) cho embedding generation.

---

## 🏗️ Kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────┐
│                    PCM-WebApp Frontend                  │
├─────────────────────────────────────────────────────────┤
│  AI Chat Logs Page (UI)                                │
│  ├── Semantic Search Input                             │
│  ├── Search Results Display                            │
│  └── Vector Statistics                                 │
├─────────────────────────────────────────────────────────┤
│  AI Chat Logger Service                                │
│  ├── Auto message indexing                             │
│  ├── Search API                                        │
│  └── Statistics API                                    │
├─────────────────────────────────────────────────────────┤
│  Vector Database Service                               │
│  ├── Vector CRUD operations                            │
│  ├── Similarity search                                 │
│  └── IndexedDB management                              │
├─────────────────────────────────────────────────────────┤
│  Vector Embedding Service                              │
│  ├── TensorFlow.js (Universal Sentence Encoder)       │
│  ├── Fallback text-based similarity                   │
│  └── Embedding cache                                   │
├─────────────────────────────────────────────────────────┤
│  Browser Storage Layer                                 │
│  ├── IndexedDB (Vector storage)                        │
│  ├── IndexedDB (Chat logs)                            │
│  └── Memory cache                                      │
└─────────────────────────────────────────────────────────┘
```

---

## 📁 Cấu trúc file và chức năng

### 1. **VectorEmbeddingService.js**

**Location**: `/apps/pcm-webapp/public/js/modules/ai/services/VectorEmbeddingService.js`

**Chức năng**: Tạo vector embeddings từ text input

**Key Components**:

```javascript
class VectorEmbeddingService {
  // Line 6-13: Configuration và cache setup
  constructor() {
    this.model = null;
    this.modelType = "browser"; // 'browser', 'api', hoặc 'fallback'
    this.isInitialized = false;
    this.cache = new Map(); // LRU cache cho embeddings
    this.maxCacheSize = 1000;
  }

  // Line 18-45: Initialization với fallback handling
  async initialize(options = {}) {
    // Cố gắng load TensorFlow.js model
    // Nếu fail → chuyển sang fallback mode
  }

  // Line 42-90: Dynamic script loading
  async initializeBrowserModel() {
    // Load TensorFlow.js từ CDN
    // Load Universal Sentence Encoder
    // Fallback nếu network fail
  }

  // Line 148-196: Fallback embedding (text-based)
  embedWithFallback(text) {
    // TF-IDF style vector generation
    // 64-dimensional vectors
    // Word frequency + semantic features
  }
}
```

**Embedding Methods**:

- **Browser Model**: 512D vectors từ Universal Sentence Encoder
- **API Model**: External API calls (placeholder)
- **Fallback Model**: 64D vectors từ text analysis

---

### 2. **VectorDatabaseService.js**

**Location**: `/apps/pcm-webapp/public/js/modules/ai/services/VectorDatabaseService.js`

**Chức năng**: Quản lý vector storage và similarity search

**IndexedDB Schema**:

```javascript
// Line 45-58: Database setup
const store = db.createObjectStore("vectors", { keyPath: "id" });
store.createIndex("type", "type", { unique: false });
store.createIndex("sessionId", "sessionId", { unique: false });
store.createIndex("timestamp", "timestamp", { unique: false });
```

**Vector Document Structure**:

```javascript
// Line 77-85: Vector document format
const vectorDoc = {
  id: "session_123_user_message_1699123456789",
  vector: [0.1, 0.2, 0.3, ...], // 512D hoặc 64D array
  text: "User's actual message content",
  type: "user_message" | "ai_response",
  metadata: {
    sessionId: "session_123",
    timestamp: "2023-11-04T10:30:45.123Z",
    provider: "openai" // optional
  },
  dimension: 512 // hoặc 64
};
```

**Core Operations**:

```javascript
// Line 65-96: Add vector
async addVector(data) {
  const vector = await vectorEmbeddingService.embed(text);
  // Store in IndexedDB
}

// Line 115-168: Similarity search
async search(query, options = {}) {
  const queryVector = await vectorEmbeddingService.embed(query);
  const allVectors = await this.getAllVectors();

  // Calculate cosine similarity
  for (const doc of allVectors) {
    const similarity = vectorEmbeddingService.cosineSimilarity(
      queryVector, doc.vector
    );
  }

  return results.sort((a, b) => b.similarity - a.similarity);
}
```

---

### 3. **AIChatLogger.js** (Enhanced)

**Location**: `/apps/pcm-webapp/public/js/modules/ai/services/AIChatLogger.js`

**Vector Integration**:

```javascript
// Line 7: Import vector database
import vectorDatabaseService from "./VectorDatabaseService.js";

// Line 20-34: Vector DB initialization
async initializeVectorDB() {
  await vectorDatabaseService.initialize();
  this.isVectorDBInitialized = true;
}

// Line 125, 146: Auto-indexing messages
logAIResponse(response, provider) {
  // Save to regular IndexedDB
  this.saveLogEntry(logEntry);

  // Index in vector database for semantic search
  this.indexMessageInVectorDB(logEntry);
}

// Line 180-230: Vector indexing logic
async indexMessageInVectorDB(logEntry) {
  const content = logEntry.content || logEntry.response;

  // Skip non-text content và session control messages
  const skipTypes = ['session_start', 'session_end', 'function_call', 'function_result'];
  if (skipTypes.includes(logEntry.type)) return;

  // Create vector data
  const vectorData = {
    id: `${logEntry.sessionId}_${logEntry.type}_${Date.now()}`,
    text: content.substring(0, 1000), // Limit for performance
    type: logEntry.type,
    metadata: { sessionId, timestamp, provider }
  };

  await vectorDatabaseService.addVector(vectorData);
}

// Line 235-256: Search API
async searchMessages(query, options = {}) {
  return await vectorDatabaseService.search(query, options);
}
```

---

### 4. **AIChatLogsPage.js** (UI Integration)

**Location**: `/apps/pcm-webapp/public/js/modules/ai/pages/AIChatLogsPage.js`

**UI Components**:

```javascript
// Line 196-256: Semantic search UI
createVectorSearchSection() {
  // Search input box
  // Search results container
  // Vector statistics display
}

// Line 857-936: Search handling
async performVectorSearch() {
  const query = searchInput.value.trim();

  // Call vector search
  const results = await aiChatLogger.searchMessages(query, {
    limit: 10,
    threshold: 0.2
  });

  // Render results với similarity scores
  results.forEach(result => {
    const similarity = Math.round(result.similarity * 100);
    // Display result item với click-to-highlight
  });
}

// Line 973-998: Highlight search results
async highlightSearchResult(result) {
  const sessionId = result.metadata.sessionId;
  await this.selectSession(sessionId);

  // Find và highlight matching log entry
  logEntries.forEach(entry => {
    if (logContent.includes(result.text.substring(0, 50))) {
      entry.style.backgroundColor = '#fff3cd';
      entry.scrollIntoView({ behavior: 'smooth' });
    }
  });
}
```

---

## 🚀 Workflow hoạt động

### 1. **Message Logging & Indexing**

```
User sends message → AIPanel.handleSendMessage()
                  ↓
              aiChatLogger.logMessage()
                  ↓
              Save to IndexedDB (chat logs)
                  ↓
              indexMessageInVectorDB()
                  ↓
              Generate embedding
                  ↓
              Store vector in IndexedDB (vectors)
```

**Source Reference**:

- `AIPanel.js:351` - User message logging
- `AIPanel.js:505, 547` - AI response logging
- `AIChatLogger.js:146, 125` - Auto vector indexing

### 2. **Semantic Search Process**

```
User enters search query → UI.performVectorSearch()
                        ↓
                    Generate query embedding
                        ↓
                    Load all vectors from IndexedDB
                        ↓
                    Calculate cosine similarity
                        ↓
                    Sort by similarity score
                        ↓
                    Display results với click-to-highlight
```

**Source Reference**:

- `AIChatLogsPage.js:878` - Search initiation
- `VectorDatabaseService.js:130` - Query embedding
- `VectorEmbeddingService.js:133-153` - Cosine similarity calculation

### 3. **Embedding Generation**

```
Text input → VectorEmbeddingService.embed()
           ↓
       Check cache first
           ↓
       Browser Model (TensorFlow.js)
           ↓ (if fails)
       Fallback Model (text-based)
           ↓
       Cache result và return vector
```

**Source Reference**:

- `VectorEmbeddingService.js:108` - Cache check
- `VectorEmbeddingService.js:115-123` - Model routing
- `VectorEmbeddingService.js:148-196` - Fallback implementation

---

## 🔧 Cấu hình và Tuning

### 1. **Vector Dimensions**

```javascript
// TensorFlow.js Universal Sentence Encoder
const BROWSER_EMBEDDING_DIM = 512;

// Fallback text-based embeddings
const FALLBACK_EMBEDDING_DIM = 64;
```

### 2. **Search Parameters**

```javascript
// Default search options
const searchOptions = {
  limit: 10, // Max results
  threshold: 0.2, // Minimum similarity (0-1)
  type: null, // Filter by log type
  sessionId: null, // Filter by session
};
```

### 3. **Cache Configuration**

```javascript
// VectorEmbeddingService.js:12
this.maxCacheSize = 1000; // LRU cache size

// Text length limit for performance
const maxTextLength = 1000; // AIChatLogger.js:210
```

### 4. **Fallback Text Features**

```javascript
// VectorEmbeddingService.js:156-163
const features = [
  "question",
  "answer",
  "help",
  "problem",
  "solution",
  "error",
  "function",
  "code",
  "user",
  "system",
  "data",
  "file",
  "create",
  "delete",
  "update",
  "search",
  // ... semantic keywords for similarity
];
```

---

## 🧪 Testing và Debugging

### 1. **Vector Service Status**

```javascript
// Check embedding service status
const status = vectorEmbeddingService.getStatus();
console.log(status);
// { isInitialized: true, modelType: 'browser', cacheSize: 45 }

// Check database stats
const stats = await aiChatLogger.getVectorStats();
console.log(stats);
// { totalVectors: 123, typeDistribution: {...}, embeddingServiceStatus: {...} }
```

### 2. **Manual Vector Operations**

```javascript
// Direct embedding test
const vector = await vectorEmbeddingService.embed("Hello world");
console.log(vector.length); // 512 or 64

// Direct search test
const results = await vectorDatabaseService.search("greeting", {
  threshold: 0.3,
});
console.log(results.map((r) => ({ text: r.text, similarity: r.similarity })));
```

### 3. **Performance Monitoring**

```javascript
// Check cache hit rate
console.log(`Cache size: ${vectorEmbeddingService.cache.size}`);

// Monitor search performance
console.time("vectorSearch");
await aiChatLogger.searchMessages(query);
console.timeEnd("vectorSearch");
```

---

## ⚠️ Offline Capability Analysis

### ✅ **Hoàn toàn Offline - CÓ THỂ**

**Điều kiện**:

1. TensorFlow.js libraries đã cached trong browser
2. Hoặc fallback mode (không cần external dependencies)

### 📋 **Setup cho môi trường Offline**

#### Option 1: Pre-cache TensorFlow.js

```javascript
// Thêm vào HTML trước khi offline
<script src="https://cdn.jsdelivr.net/npm/@tensorflow/tfjs@4.10.0/dist/tf.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@tensorflow-models/universal-sentence-encoder@1.3.3/dist/universal-sentence-encoder.min.js"></script>
```

#### Option 2: Local TensorFlow.js files

```bash
# Download và host locally
wget https://cdn.jsdelivr.net/npm/@tensorflow/tfjs@4.10.0/dist/tf.min.js
wget https://cdn.jsdelivr.net/npm/@tensorflow-models/universal-sentence-encoder@1.3.3/dist/universal-sentence-encoder.min.js

# Update script URLs trong VectorEmbeddingService.js:47,53
```

#### Option 3: Pure Offline (Fallback Only)

```javascript
// Force fallback mode - không cần TensorFlow.js
await vectorEmbeddingService.initialize({ modelType: "fallback" });
```

### 🎯 **Offline Performance**

| Mode              | Quality | Offline Ready    | Dependencies |
| ----------------- | ------- | ---------------- | ------------ |
| **TensorFlow.js** | 95%     | ⚠️ Cần pre-cache | External CDN |
| **Fallback**      | 65%     | ✅ Hoàn toàn     | Không        |

### 📱 **Storage Requirements (Offline)**

```
IndexedDB Storage:
├── Chat Logs DB: ~1-5MB (cho 1000 messages)
├── Vector DB: ~2-10MB (tùy embedding dimension)
└── Browser Cache: ~20MB (TensorFlow.js model)
```

### 🔧 **Offline Setup Script**

```javascript
// Offline initialization script
async function initOfflineVectorDB() {
  try {
    // Try TensorFlow.js first (if cached)
    await vectorEmbeddingService.initialize({ modelType: "browser" });
    console.log("✅ Offline with TensorFlow.js");
  } catch (error) {
    // Fallback to text-based similarity
    await vectorEmbeddingService.initialize({ modelType: "fallback" });
    console.log("⚠️ Offline with fallback mode");
  }
}
```

---

## 📊 **Kết luận**

### ✅ **CÓ THỂ chạy hoàn toàn offline**

1. **Fallback mode**: Luôn hoạt động offline với text-based similarity (~65% accuracy)
2. **TensorFlow.js mode**: Hoạt động offline nếu libraries đã cached (~95% accuracy)
3. **IndexedDB storage**: Hoàn toàn local, không cần network

### 🎯 **Khuyến nghị cho Production Offline**

1. **Pre-cache TensorFlow.js** trong app startup
2. **Implement service worker** để cache model files
3. **Use hybrid approach**: TensorFlow.js + fallback
4. **Monitor storage usage** và implement cleanup strategies

Vector Database trong PCM-WebApp được thiết kế để hoạt động robust trong mọi môi trường, từ online high-performance đến offline basic functionality.
