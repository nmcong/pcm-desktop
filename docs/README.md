# PCM Desktop - Documentation

Complete documentation for the PCM Desktop application.

## 📂 **Documentation Structure**

```
docs/
├── README.md              # This file - Main documentation hub
│
└── rag/                   # 🤖 RAG System Documentation
    ├── README.md          #    RAG system overview
    │
    ├── embedding/         # 🔤 Embedding Service
    │   ├── DJL_OVERVIEW.md
    │   ├── MODEL_SELECTION_GUIDE.md
    │   ├── MODEL_COMPARISON.md
    │   └── EMBEDDING_SERVICES_COMPLETE.md
    │
    └── vectorstore/       # 🗄️ Vector Databases
        └── QDRANT_IMPLEMENTATION.md
```

---

## 🚀 **Quick Start**

### I'm a...

#### Beginner - New to RAG systems

**Start here:**
1. Read [RAG Overview](./rag/README.md) (10 min)
2. Read [DJL Overview](./rag/embedding/DJL_OVERVIEW.md) (20 min)
3. Run setup: `./scripts/setup-embeddings-djl.sh`
4. Run example: `java -cp "..." com.noteflix.pcm.rag.examples.embedding.DJLEmbeddingExample`

**Time:** ~30 minutes  
**Goal:** Generate your first embeddings

---

#### Developer - Building RAG applications

**Recommended path:**
1. [Model Selection Guide](./rag/embedding/MODEL_SELECTION_GUIDE.md) (15 min)
2. [Model Comparison](./rag/embedding/MODEL_COMPARISON.md) (20 min)
3. [Qdrant Implementation](./rag/vectorstore/QDRANT_IMPLEMENTATION.md) (20 min)
4. Build your application using examples

**Time:** ~1 hour  
**Goal:** Build production RAG system

---

#### Expert - Optimizing & deploying

**Focus on:**
1. [Complete Implementation Summary](./rag/embedding/EMBEDDING_SERVICES_COMPLETE.md)
2. Performance benchmarking
3. Production deployment guide
4. Scaling & optimization

**Time:** ~2-3 hours  
**Goal:** Production-ready deployment

---

## 📚 **Documentation by Topic**

### 🤖 RAG (Retrieval-Augmented Generation)

**Main Hub:** [rag/README.md](./rag/README.md)

Complete RAG system documentation including:
- Text embedding services
- Vector database integration
- Full pipeline examples
- Performance benchmarks

**Key Documents:**
- [RAG System Overview](./rag/README.md) - Start here
- [Complete Implementation](./rag/embedding/EMBEDDING_SERVICES_COMPLETE.md) - Full summary

---

### 🔤 Embedding Services

**Location:** [rag/embedding/](./rag/embedding/)

Convert text to vector representations for semantic search.

**Production-Ready Implementation:**
- **DJLEmbeddingService** - The ONLY embedding service you need

**Key Documents:**

| Document | Purpose | Read Time |
|----------|---------|-----------|
| [DJL Overview](./rag/embedding/DJL_OVERVIEW.md) | Complete embedding guide | 20 min |
| [Model Selection](./rag/embedding/MODEL_SELECTION_GUIDE.md) | Choose right model | 15 min |
| [Model Comparison](./rag/embedding/MODEL_COMPARISON.md) | Benchmarks & analysis | 20 min |

**Quick Start:**
```bash
./scripts/setup-embeddings-djl.sh
./scripts/build.sh
# Run examples...
```

---

### 🗄️ Vector Stores

**Location:** [rag/vectorstore/](./rag/vectorstore/)

Store and search vector embeddings efficiently.

**Available Implementations:**
- **QdrantVectorStore** - Production-grade (Qdrant REST API)
- **LuceneVectorStore** - Offline search (Apache Lucene)
- **SimpleVectorStore** - In-memory testing

**Key Documents:**

| Document | Purpose | Read Time |
|----------|---------|-----------|
| [Qdrant Implementation](./rag/vectorstore/QDRANT_IMPLEMENTATION.md) | Complete Qdrant guide | 20 min |

**Quick Start:**
```bash
docker run -p 6333:6333 qdrant/qdrant
# Use QdrantVectorStore in your code
```

---

## 🎯 **Common Use Cases**

### Use Case 1: Semantic Code Search

**Goal:** Search codebase by meaning, not keywords

**What you need:**
- Embeddings: `DJLEmbeddingService` with `all-MiniLM-L6-v2`
- Vector Store: `LuceneVectorStore` (offline) or `QdrantVectorStore` (scalable)

**Docs to read:**
1. [DJL Overview](./rag/embedding/DJL_OVERVIEW.md)
2. [Qdrant Implementation](./rag/vectorstore/QDRANT_IMPLEMENTATION.md)

**Example:** `src/main/java/com/noteflix/pcm/rag/examples/pipeline/IndexerExample.java`

---

### Use Case 2: Documentation Search

**Goal:** Find relevant documentation quickly

**What you need:**
- Embeddings: `DJLEmbeddingService` with `all-mpnet-base-v2` (best quality)
- Vector Store: `QdrantVectorStore` (for large doc sets)

**Docs to read:**
1. [Model Selection Guide](./rag/embedding/MODEL_SELECTION_GUIDE.md)
2. [Qdrant Implementation](./rag/vectorstore/QDRANT_IMPLEMENTATION.md)

**Example:** `src/main/java/com/noteflix/pcm/rag/examples/pipeline/BasicRAGExample.java`

---

### Use Case 3: FAQ Matching

**Goal:** Match user questions to FAQ database

**What you need:**
- Embeddings: `DJLEmbeddingService` (fast with batch processing)
- Vector Store: In-memory (small dataset)

**Docs to read:**
1. [DJL Overview](./rag/embedding/DJL_OVERVIEW.md)

**Example:** `src/main/java/com/noteflix/pcm/rag/examples/embedding/DJLEmbeddingExample.java`

---

## 📊 **Quick Comparison**

### Embedding Services

| Service | Memory | Speed | Quality | Status |
|---------|--------|-------|---------|--------|
| **DJLEmbeddingService** | ~400MB | ~70-90ms (single)<br>~500ms (batch 100) | Production-grade | ✅ **USE THIS** |

**This is the ONLY embedding service you need!**

---

### Vector Stores

| Store | Max Documents | Speed | Setup | Best For |
|-------|--------------|-------|-------|----------|
| **Qdrant** | Millions | ⚡ Fast | Docker | ✅ Production |
| **Lucene** | ~100K | 🔵 Medium | None | Offline |
| **Simple** | ~10K | ⚡ Fast | None | Testing |

**Recommendation:** Use Qdrant for production, Lucene for offline, Simple for testing.

---

## 🔧 **Setup Guide**

### Prerequisites

**System Requirements:**
- Java 21+
- 4GB RAM (minimum), 8GB recommended
- 2GB disk space (with models)

**For Embeddings:**
```bash
# Option 1: DJL (recommended)
./scripts/setup-embeddings-djl.sh

# Option 2: ONNX (lightweight)
./scripts/setup-embeddings-onnx.sh
```

**For Vector Store (Qdrant):**
```bash
# Docker (easiest)
docker run -p 6333:6333 qdrant/qdrant

# Or download binary from qdrant.tech
```

**Build Project:**
```bash
./scripts/build.sh
```

---

## 💡 **Best Practices**

### 1. Choose the Right Tools

```
Small datasets (< 10K docs):
├─ Embeddings: DJL (all-MiniLM-L6-v2)
└─ Vector Store: Simple or Lucene

Medium datasets (10K - 100K docs):
├─ Embeddings: DJL (all-MiniLM-L6-v2)
└─ Vector Store: Lucene or Qdrant

Large datasets (> 100K docs):
├─ Embeddings: DJL (all-MiniLM-L6-v2 or all-mpnet-base-v2)
└─ Vector Store: Qdrant ✅
```

### 2. Optimize Performance

```java
// ✅ DO: Reuse service instances
EmbeddingService service = new DJLEmbeddingService("...");
for (String text : texts) {
    embeddings.add(service.embed(text));
}
service.close();

// ❌ DON'T: Recreate every time
for (String text : texts) {
    EmbeddingService service = new DJLEmbeddingService("...");
    embeddings.add(service.embed(text));
    service.close(); // Wasteful!
}
```

### 3. Handle Resources Properly

```java
// ✅ DO: Use try-with-resources
try (EmbeddingService service = new DJLEmbeddingService("...")) {
    float[] emb = service.embed("text");
} // Automatically closed

// ❌ DON'T: Forget to close
EmbeddingService service = new DJLEmbeddingService("...");
service.embed("text");
// Never closed - memory leak!
```

### 4. Enable Caching

```java
// ✅ DO: Cache frequent queries
Map<String, float[]> cache = new ConcurrentHashMap<>();
float[] embedding = cache.computeIfAbsent(text, service::embed);
// First call: ~80ms, subsequent: ~0.01ms
```

---

## 🐛 **Troubleshooting**

### Common Issues

**Issue 1: Model not found**
```
Error: Model not found: data/models/all-MiniLM-L6-v2
```
**Solution:**
```bash
./scripts/setup-embeddings-djl.sh
```

---

**Issue 2: Qdrant connection failed**
```
Error: Connection refused: localhost:6333
```
**Solution:**
```bash
docker run -p 6333:6333 qdrant/qdrant
```

---

**Issue 3: OutOfMemoryError**
```
java.lang.OutOfMemoryError: Java heap space
```
**Solution:**
```bash
export JAVA_OPTS="-Xmx4g"
./scripts/run.sh
```

---

**Issue 4: Slow performance**
```
Embeddings taking 500ms+ (should be ~70-90ms)
```
**Solution:**
```java
// Warm up JVM
for (int i = 0; i < 20; i++) {
    service.embed("warmup");
}
// Now measure performance
```

**More help:** See individual documentation for detailed troubleshooting.

---

## 📖 **Complete Documentation Index**

### RAG System

- [RAG Overview](./rag/README.md) - Complete RAG documentation hub
- [Implementation Summary](./rag/embedding/EMBEDDING_SERVICES_COMPLETE.md) - All implementations

### Embeddings

- [DJL Overview](./rag/embedding/DJL_OVERVIEW.md) - Complete embedding guide
- [Model Selection](./rag/embedding/MODEL_SELECTION_GUIDE.md) - Choose model
- [Model Comparison](./rag/embedding/MODEL_COMPARISON.md) - Benchmarks

### Vector Stores

- [Qdrant Implementation](./rag/vectorstore/QDRANT_IMPLEMENTATION.md) - Qdrant guide

### Examples

- Code examples: `src/main/java/com/noteflix/pcm/rag/examples/`
- Embedding examples: `examples/embedding/`
- Pipeline examples: `examples/pipeline/`

---

## 📊 **Documentation Statistics**

| Category | Documents | Total Words | Total Size |
|----------|-----------|-------------|------------|
| **RAG Overview** | 1 | ~3,000 | ~20KB |
| **Embeddings** | 5 | ~30,000 | ~180KB |
| **Vector Store** | 1 | ~8,000 | ~50KB |
| **Total** | **7** | **~41,000** | **~250KB** |

---

## 🎓 **Learning Paths**

### Path 1: Quick Start (30 minutes)

```
1. docs/rag/README.md (10 min)
   ↓
2. docs/rag/embedding/DJL_OVERVIEW.md (20 min)
   ↓
3. Run ./scripts/setup-embeddings-djl.sh
   ↓
4. Run DJLEmbeddingExample
   ↓
5. Done! ✅
```

---

### Path 2: Full Understanding (2 hours)

```
1. RAG Overview (10 min)
   ↓
2. DJL Overview (20 min)
   ↓
3. ONNX Overview (15 min)
   ↓
4. Model Selection Guide (15 min)
   ↓
5. Model Comparison (20 min)
   ↓
6. Qdrant Implementation (20 min)
   ↓
7. Run all examples (20 min)
   ↓
8. Build your application ✅
```

---

### Path 3: Production Deployment (1 day)

```
Day 1 Morning:
- Read all documentation (2 hours)
- Understand architecture (30 min)
- Setup local environment (30 min)

Day 1 Afternoon:
- Performance benchmarking (1 hour)
- Choose optimal configuration (30 min)
- Integration testing (1.5 hours)

Day 1 Evening:
- Production setup (1 hour)
- Load testing (1 hour)
- Deployment ✅
```

---

## 🚀 **Next Steps**

### For New Users

1. **Read:** [RAG Overview](./rag/README.md)
2. **Setup:** Run `./scripts/setup-embeddings-djl.sh`
3. **Learn:** Read [DJL Overview](./rag/embedding/DJL_OVERVIEW.md)
4. **Practice:** Run examples
5. **Build:** Create your application

### For Developers

1. **Read:** All embedding documentation
2. **Read:** Vector store documentation
3. **Compare:** Benchmark different options
4. **Choose:** Select optimal configuration
5. **Build:** Integrate into your app
6. **Deploy:** Move to production

---

## 🤝 **Contributing**

Want to improve documentation?

**How to contribute:**
1. Read existing docs
2. Identify gaps or improvements
3. Make changes
4. Test examples
5. Submit pull request

**Areas for improvement:**
- More examples
- Better explanations
- Additional use cases
- Performance tips
- Troubleshooting guides

---

## 📄 **License**

See [LICENSE](../LICENSE) in project root.

---

**Last Updated:** November 13, 2024  
**Version:** 2.0.0  
**Status:** ✅ Complete & Production Ready

---

## 💡 **Quick Tips**

```
💡 Tip 1: Start with DJL (all-MiniLM-L6-v2) - works for 90% of use cases
💡 Tip 2: Use Qdrant for production (handles millions of documents)
💡 Tip 3: Enable caching for frequent queries (1500x faster!)
💡 Tip 4: Warm up JVM before measuring performance
💡 Tip 5: Reuse service instances (don't recreate every time)
```

---

**🎉 Start exploring: [rag/README.md](./rag/README.md)**

