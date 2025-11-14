# RAG System Documentation

Complete documentation for PCM Desktop's RAG (Retrieval-Augmented Generation) system.

## 📚 **Overview**

This directory contains comprehensive documentation for:
- **Embedding Services** - Text-to-vector conversion
- **Vector Stores** - Vector database integration
- **RAG Pipeline** - Complete workflows

---

## 📂 **Documentation Structure**

```
rag/
├── README.md                          # This file
│
├── embedding/                         # 🔤 Embedding Service
│   ├── DJL_OVERVIEW.md                #    Complete embedding guide
│   ├── MODEL_SELECTION_GUIDE.md       #    Model selection guide
│   └── MODEL_COMPARISON.md            #    Benchmarks & comparison
│
└── vectorstore/                       # 🗄️ Vector Databases
    └── QDRANT_IMPLEMENTATION.md       #    Qdrant integration guide
```

---

## 🚀 **Quick Start**

### Option 1: Embeddings Only (DJL)

```bash
# 1. Setup
./scripts/setup-embeddings-djl.sh

# 2. Build
./scripts/build.sh

# 3. Run example
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*" \
  com.noteflix.pcm.rag.examples.embedding.DJLEmbeddingExample
```

**Result:** Generate embeddings (~70-90ms per text)

---

### Option 2: Full RAG System

```bash
# 1. Start Qdrant
docker run -p 6333:6333 qdrant/qdrant

# 2. Setup embeddings
./scripts/setup-embeddings-djl.sh

# 3. Build
./scripts/build.sh

# 4. Run RAG example
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*" \
  com.noteflix.pcm.rag.examples.pipeline.BasicRAGExample
```

**Result:** Full RAG pipeline with vector search

---

## 📖 **Documentation Guide**

### For Beginners

**Start here:**
1. [DJL Overview](./embedding/DJL_OVERVIEW.md) - Learn embedding basics
2. [Model Selection](./embedding/MODEL_SELECTION_GUIDE.md) - Choose your model
3. Run examples in `src/main/java/com/noteflix/pcm/rag/examples/`

**Time:** ~30 minutes

---

### For Intermediate Users

**Recommended path:**
1. [Model Comparison](./embedding/MODEL_COMPARISON.md) - Compare performance
2. [Qdrant Implementation](./vectorstore/QDRANT_IMPLEMENTATION.md) - Vector database
3. Build your RAG application

**Time:** ~1 hour

---

### For Advanced Users

**Production deployment:**
1. Read all embedding docs
2. Read vector store docs
3. Performance benchmarking
4. Production deployment guide
5. Optimization & scaling

**Time:** ~2-3 hours

---

## 🎯 **Key Topics**

### 🔤 Embedding Services

**What:** Convert text to vector representations

**Production Implementation:**
- **DJLEmbeddingService** - The ONLY embedding service (DJL + ONNX Runtime + HuggingFace Tokenizer)

**When to use:**
- Semantic search
- Document similarity
- Clustering & classification
- RAG systems

**Documentation:** [embedding/](./embedding/)

---

### 🗄️ Vector Stores

**What:** Store and search vector embeddings

**Implementations:**
- **QdrantVectorStore** - Production-grade (Qdrant REST API)
- **LuceneVectorStore** - Offline search (Apache Lucene)
- **SimpleVectorStore** - In-memory (for testing)

**When to use:**
- Large document collections (>1K docs)
- Fast similarity search required
- Production RAG systems
- Scalable deployments

**Documentation:** [vectorstore/](./vectorstore/)

---

## 📊 **Comparison Matrix**

### Embedding Services

| Service | Memory | Speed | Quality | Status |
|---------|--------|-------|---------|--------|
| **DJLEmbeddingService** | ~400MB | ~70-90ms (single)<br>~500ms (batch 100) | Production-grade | ✅ **USE THIS** |

### Vector Stores

| Store | Scalability | Speed | Setup | Best For |
|-------|-------------|-------|-------|----------|
| **Qdrant** | Millions | ⚡ Fast | Docker | ✅ Production |
| **Lucene** | ~100K | 🔵 Medium | None | Offline |
| **Simple** | ~10K | ⚡ Fast | None | Testing |

---

## 💡 **Common Use Cases**

### Use Case 1: Code Search

```java
// 1. Index codebase
DocumentIndexer indexer = new DocumentIndexer(ragService);
indexer.indexDirectory(Paths.get("src/"), true);

// 2. Search
String query = "How to validate user input?";
List<ScoredDocument> results = ragService.search(query);

// 3. Show results
for (ScoredDocument doc : results) {
    System.out.println(doc.getDocument().getTitle());
    System.out.println(doc.getSnippet());
}
```

**Recommended:**
- **Embeddings:** DJLEmbeddingService with all-MiniLM-L6-v2
- **Vector Store:** Lucene (offline) or Qdrant (scalable)

---

### Use Case 2: Documentation Search

```java
// 1. Index docs
ragService.indexDocuments(loadDocuments());

// 2. Search with filters
RetrievalOptions options = RetrievalOptions.builder()
    .maxResults(10)
    .minScore(0.7)
    .filters(Map.of("type", "API_DOC"))
    .build();

List<ScoredDocument> results = ragService.search(query, options);
```

**Recommended:**
- **Embeddings:** DJLEmbeddingService with all-mpnet-base-v2 (best quality)
- **Vector Store:** Qdrant (scalable)

---

### Use Case 3: FAQ Matching

```java
// 1. Pre-compute FAQ embeddings
for (FAQ faq : faqs) {
    float[] embedding = embeddingService.embed(faq.getQuestion());
    faq.setEmbedding(embedding);
}

// 2. Match user query
float[] queryEmb = embeddingService.embed(userQuestion);
FAQ bestMatch = findMostSimilar(queryEmb, faqs);
```

**Recommended:**
- **Embeddings:** DJLEmbeddingService (fast with batch processing)
- **Vector Store:** In-memory (small dataset)

---

## 🔧 **Setup & Configuration**

### System Requirements

**Minimum:**
- Java 21+
- 4GB RAM
- 500MB disk space

**Recommended:**
- Java 21+
- 8GB RAM
- 2GB disk space (with models)

### Dependencies

**For Embeddings (DJLEmbeddingService):**
```
✅ ai.djl:api:0.35.0
✅ ai.djl.onnxruntime:onnxruntime-engine:0.35.0
✅ ai.djl.huggingface:tokenizers:0.35.0
✅ com.microsoft.onnxruntime:onnxruntime:1.23.2
```

**For Qdrant Vector Store:**
```
✅ Java HttpClient (built-in)
✅ Jackson JSON (included)
✅ Qdrant server (Docker or binary)
```

---

## 🐛 **Troubleshooting**

### Common Issues

**1. Model not found**
```bash
# Solution
./scripts/setup-embeddings-djl.sh
```

**2. Qdrant connection failed**
```bash
# Solution
docker run -p 6333:6333 qdrant/qdrant
```

**3. OutOfMemoryError**
```bash
# Solution
export JAVA_OPTS="-Xmx4g"
```

**4. Slow performance**
```java
// Solution: Warm up JVM
for (int i = 0; i < 20; i++) {
    service.embed("warmup");
}
```

See individual docs for detailed troubleshooting.

---

## 📚 **Complete Reading List**

### Must Read (Everyone)

1. [DJL Overview](./embedding/DJL_OVERVIEW.md) - 20 min
2. [Model Selection Guide](./embedding/MODEL_SELECTION_GUIDE.md) - 15 min

### Recommended (Most Users)

3. [Model Comparison](./embedding/MODEL_COMPARISON.md) - 20 min
4. [Qdrant Implementation](./vectorstore/QDRANT_IMPLEMENTATION.md) - 20 min

### Optional (Advanced Users)

6. Source code in `src/main/java/com/noteflix/pcm/rag/`
7. Examples in `src/main/java/com/noteflix/pcm/rag/examples/`

**Total time:** ~90 minutes for complete understanding

---

## 🎓 **Best Practices**

### 1. Choose the Right Model

```
Small/Fast → all-MiniLM-L6-v2 (384d)
Best Quality → all-mpnet-base-v2 (768d)
Multilingual → paraphrase-multilingual-mpnet-base-v2
```

### 2. Optimize Performance

```java
// ✅ Reuse service instances
// ✅ Enable caching
// ✅ Use batch processing
// ✅ Warm up JVM
```

### 3. Resource Management

```java
// ✅ Always close resources
try (EmbeddingService service = ...) {
    // Use service
} // Auto-closed
```

### 4. Error Handling

```java
// ✅ Handle exceptions gracefully
try {
    float[] emb = service.embed(text);
} catch (Exception e) {
    log.error("Embedding failed", e);
    // Fallback logic
}
```

---

## 🚀 **Production Checklist**

Before deploying to production:

- [ ] ✅ Choose appropriate embedding model
- [ ] ✅ Choose appropriate vector store
- [ ] ✅ Performance benchmarking with real data
- [ ] ✅ Memory profiling
- [ ] ✅ Error handling & logging
- [ ] ✅ Resource cleanup
- [ ] ✅ Caching strategy
- [ ] ✅ Monitoring & alerts
- [ ] ✅ Load testing
- [ ] ✅ Documentation for team

---

## 📖 **External Resources**

### Official Documentation

- **[DJL Website](https://djl.ai/)** - Deep Java Library
- **[Qdrant](https://qdrant.tech/)** - Vector database
- **[Sentence Transformers](https://www.sbert.net/)** - Model documentation
- **[HuggingFace](https://huggingface.co/)** - Model hub

### Community

- **[DJL Discord](https://discord.gg/deepjavalibrary)** - Get help
- **[Qdrant Discord](https://qdrant.to/discord)** - Vector DB community
- **[HuggingFace](https://huggingface.co/)** - Model hub

---

## 🤝 **Contributing**

Want to improve the RAG system or documentation?

**How to contribute:**
1. Read existing documentation
2. Identify improvements
3. Make changes
4. Test thoroughly
5. Submit pull request

**Areas for contribution:**
- More embedding models
- Additional vector stores
- Performance optimizations
- Better examples
- Improved documentation

---

## 📊 **Statistics**

| Category | Components | Lines of Code | Documentation |
|----------|------------|---------------|---------------|
| **Embeddings** | 3 services | ~1,200 | 4 guides |
| **Vector Stores** | 3 implementations | ~800 | 1 guide |
| **Pipeline** | RAG service + indexer | ~600 | Examples |
| **Examples** | 6 examples | ~1,200 | 2 READMEs |
| **Documentation** | 6 guides | ~50,000 words | This folder |
| **Total** | **15+ components** | **~3,800 LOC** | **~50K words** |

---

## 📄 **License**

See [LICENSE](../../LICENSE) in project root.

---

**Last Updated:** November 13, 2024  
**Version:** 2.0.0  
**Status:** ✅ Production Ready  
**Author:** PCM Team

---

**🎉 Everything you need for production RAG systems!**

**Start here:** [embedding/DJL_OVERVIEW.md](./embedding/DJL_OVERVIEW.md)

