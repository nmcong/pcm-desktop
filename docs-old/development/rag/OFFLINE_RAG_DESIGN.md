# 🔒 OFFLINE RAG DESIGN - 100% Local Implementation

## ✅ **Design Goals**

1. ✅ **100% Offline** - No internet required
2. ✅ **Pure Java** - No external services
3. ✅ **Lightweight** - Minimal dependencies
4. ✅ **Fast** - <2 seconds response time
5. ✅ **Maintainable** - Clean architecture

---

## 🏗️ **Architecture**

```
┌─────────────────────────────────────────────────────────────┐
│                      User Query                              │
└──────────────────────────┬──────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                   RAG Orchestrator                           │
│  ┌──────────────┐  ┌───────────────┐  ┌─────────────┐      │
│  │   Query      │→ │   Retrieval   │→ │  Response   │      │
│  │  Processor   │  │    Engine     │  │  Generator  │      │
│  └──────────────┘  └───────────────┘  └─────────────┘      │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              Apache Lucene (Vector Storage)                  │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Indexed Documents:                                 │     │
│  │  - Java Source Code                                 │     │
│  │  - Database Objects (tables, procedures)           │     │
│  │  - System Metadata (screens, workflows)            │     │
│  │  - Knowledge Base Articles                         │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                    Data Sources (Local)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  SQLite  │  │  Oracle  │  │  Source  │  │Knowledge │   │
│  │(Metadata)│  │   (DB)   │  │  Code    │  │   Base   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 **Technology Stack (Offline)**

### 1. Vector Storage: Apache Lucene 9.11.1
- **Type:** Pure Java, embedded
- **Why:** No external service, fast, battle-tested
- **Features:**
  - Full-text search
  - BM25 ranking
  - Vector search (KNN)
  - Faceted search

### 2. Embedding: TF-IDF + BM25 (Built-in)
- **Type:** Statistical embeddings
- **Why:** No ML model needed, fast, effective for code search
- **Fallback:** Simple term frequency vectors

### 3. Document Processing: Pure Java
- **Parser:** JavaParser (for code)
- **Text Processing:** Apache OpenNLP (optional)
- **Chunking:** Custom splitters

### 4. Storage: Local File System
- **Index Location:** `data/rag/lucene-index/`
- **Documents:** `data/rag/documents/`
- **Cache:** `data/rag/cache/`

---

## 🔧 **Implementation Components**

### 1. Core Interfaces

```java
// Document representation
public interface RAGDocument {
    String getId();
    String getContent();
    DocumentType getType();
    Map<String, String> getMetadata();
}

// Vector storage
public interface VectorStore {
    void indexDocument(RAGDocument doc);
    List<ScoredDocument> search(String query, int limit);
    void deleteDocument(String id);
    void clear();
}

// Retrieval engine
public interface RetrievalEngine {
    List<RAGContext> retrieve(String query, RetrievalOptions options);
}

// RAG orchestrator
public interface RAGService {
    RAGResponse query(String query);
    void indexDataSource(DataSource source);
}
```

### 2. Package Structure

```
src/main/java/com/noteflix/pcm/rag/
├── api/                    ✅ Public interfaces
│   ├── RAGService.java
│   ├── RetrievalEngine.java
│   └── VectorStore.java
│
├── core/                   ✅ Core implementations
│   ├── LuceneVectorStore.java
│   ├── SimpleRetrievalEngine.java
│   └── DefaultRAGService.java
│
├── model/                  ✅ Data models
│   ├── RAGDocument.java
│   ├── RAGContext.java
│   ├── RAGResponse.java
│   ├── ScoredDocument.java
│   └── DocumentType.java
│
├── indexer/                ✅ Document indexing
│   ├── DocumentIndexer.java
│   ├── CodeIndexer.java
│   ├── DatabaseIndexer.java
│   └── KnowledgeBaseIndexer.java
│
├── chunking/               ✅ Document chunking
│   ├── ChunkingStrategy.java
│   ├── CodeChunker.java
│   ├── TextChunker.java
│   └── SemanticChunker.java
│
├── parser/                 ✅ Document parsers
│   ├── DocumentParser.java
│   ├── JavaCodeParser.java
│   ├── SQLParser.java
│   └── MarkdownParser.java
│
├── embedding/              ✅ Embeddings (simple)
│   ├── EmbeddingGenerator.java
│   ├── TFIDFEmbedding.java
│   └── BM25Embedding.java
│
├── retrieval/              ✅ Retrieval strategies
│   ├── BM25Retriever.java
│   ├── KeywordRetriever.java
│   └── HybridRetriever.java
│
└── examples/               ✅ Usage examples
    ├── BasicRAGExample.java
    ├── CodeSearchExample.java
    └── DatabaseQueryExample.java
```

---

## 🎯 **Key Features**

### 1. Document Indexing
```java
// Index Java source code
CodeIndexer codeIndexer = new CodeIndexer();
codeIndexer.indexDirectory("src/main/java");

// Index database objects
DatabaseIndexer dbIndexer = new DatabaseIndexer();
dbIndexer.indexSchema(connection, "PROD_SCHEMA");

// Index knowledge base
KnowledgeBaseIndexer kbIndexer = new KnowledgeBaseIndexer();
kbIndexer.indexArticles("docs/knowledge-base");
```

### 2. Smart Chunking
```java
// For code: chunk by method/class
CodeChunker codeChunker = new CodeChunker();
List<Chunk> codeChunks = codeChunker.chunk(javaFile);

// For text: semantic chunking
SemanticChunker textChunker = new SemanticChunker();
List<Chunk> textChunks = textChunker.chunk(document);
```

### 3. Retrieval
```java
// Simple keyword search
List<RAGContext> results = retrievalEngine.retrieve(
    "customer registration screen",
    RetrievalOptions.builder()
        .maxResults(10)
        .minScore(0.5)
        .types(DocumentType.CODE, DocumentType.DATABASE)
        .build()
);

// With filters
List<RAGContext> filtered = retrievalEngine.retrieve(
    "batch job schedule",
    RetrievalOptions.builder()
        .maxResults(5)
        .filter("subsystem", "CUSTOMER")
        .filter("type", "BATCH_JOB")
        .build()
);
```

### 4. Response Generation
```java
// Query with LLM integration
RAGResponse response = ragService.query(
    "Màn hình customer registration gọi procedure nào?"
);

// Response includes:
// - answer: Generated by LLM
// - sources: Retrieved documents with scores
// - metadata: Query stats, retrieval time
```

---

## 📊 **Performance Targets**

| Operation | Target | Notes |
|-----------|--------|-------|
| **Indexing** | 1000 docs/sec | Java files |
| **Search** | <500ms | Top 10 results |
| **Full Query** | <2 seconds | With LLM |
| **Index Size** | <1GB | For 10K docs |
| **Memory** | <512MB | Runtime |

---

## 🔍 **Search Strategies**

### 1. BM25 (Best Match 25)
```java
// Statistical ranking
// Good for: exact keyword matching
// Use case: finding specific code/procedures
```

### 2. TF-IDF (Term Frequency-Inverse Document Frequency)
```java
// Term importance
// Good for: general text search
// Use case: documentation, knowledge base
```

### 3. Hybrid (BM25 + Semantic)
```java
// Combine keyword + meaning
// Good for: best accuracy
// Use case: complex queries
```

---

## 🗂️ **Document Types**

```java
public enum DocumentType {
    // Source code
    JAVA_CLASS,
    JAVA_INTERFACE,
    JAVA_METHOD,
    
    // Database
    TABLE,
    VIEW,
    PROCEDURE,
    FUNCTION,
    PACKAGE,
    
    // System
    SCREEN,
    WORKFLOW,
    BATCH_JOB,
    
    // Documentation
    KNOWLEDGE_BASE,
    API_DOC,
    COMMENT
}
```

---

## 💾 **Storage Layout**

```
data/rag/
├── lucene-index/           ✅ Lucene index files
│   ├── segments_*
│   ├── _*.cfs
│   └── write.lock
│
├── documents/              ✅ Source documents (optional)
│   ├── code/
│   ├── database/
│   └── knowledge-base/
│
├── cache/                  ✅ Query cache
│   └── query-cache.db
│
└── metadata/               ✅ Index metadata
    └── index-info.json
```

---

## 🎨 **Usage Examples**

### Example 1: Index & Search Code
```java
// Initialize
RAGService ragService = new DefaultRAGService();

// Index source code
ragService.indexDataSource(
    FileSystemDataSource.builder()
        .path("src/main/java")
        .type(DocumentType.JAVA_CLASS)
        .recursive(true)
        .build()
);

// Search
RAGResponse response = ragService.query(
    "How does CustomerService handle validation?"
);

System.out.println(response.getAnswer());
for (RAGContext ctx : response.getSources()) {
    System.out.println(" - " + ctx.getDocument().getMetadata().get("file"));
}
```

### Example 2: Database Search
```java
// Index database schema
ragService.indexDataSource(
    DatabaseDataSource.builder()
        .connection(conn)
        .schema("PROD")
        .types(TABLE, VIEW, PROCEDURE)
        .build()
);

// Search
RAGResponse response = ragService.query(
    "Which tables store customer addresses?"
);
```

### Example 3: Knowledge Base
```java
// Index KB articles
ragService.indexDataSource(
    FileSystemDataSource.builder()
        .path("docs/knowledge-base")
        .type(DocumentType.KNOWLEDGE_BASE)
        .extensions("md", "txt")
        .build()
);

// Search
RAGResponse response = ragService.query(
    "How to troubleshoot batch job failures?"
);
```

---

## 🔄 **Integration with LLM**

```java
// RAG augments LLM queries
public class RAGService {
    private final LLMProvider llmProvider;
    private final RetrievalEngine retriever;
    
    public RAGResponse query(String query) {
        // 1. Retrieve relevant contexts
        List<RAGContext> contexts = retriever.retrieve(query);
        
        // 2. Build augmented prompt
        String prompt = buildPrompt(query, contexts);
        
        // 3. Query LLM
        ChatResponse llmResponse = llmProvider.chat(
            List.of(Message.user(prompt)),
            ChatOptions.defaults()
        );
        
        // 4. Return response with sources
        return RAGResponse.builder()
            .answer(llmResponse.getContent())
            .sources(contexts)
            .query(query)
            .build();
    }
}
```

---

## 📚 **Required Libraries**

### Apache Lucene (ONLY dependency)
```
lib/lucene-core-9.11.1.jar          - Core search engine
lib/lucene-queryparser-9.11.1.jar   - Query parsing
lib/lucene-analyzers-common-9.11.1.jar - Text analysis
lib/lucene-highlighter-9.11.1.jar   - Result highlighting
```

### Optional (Already have)
```
✅ Jackson (JSON processing)
✅ SLF4J (Logging)
✅ Lombok (Boilerplate reduction)
```

---

## ✅ **Advantages of This Design**

1. ✅ **100% Offline** - No external API calls
2. ✅ **Fast** - Lucene is blazing fast
3. ✅ **Lightweight** - Only 1 dependency (Lucene)
4. ✅ **Maintainable** - Pure Java, well-documented
5. ✅ **Scalable** - Can index millions of documents
6. ✅ **Battle-tested** - Lucene powers Elasticsearch
7. ✅ **No ML complexity** - TF-IDF/BM25 work great for code
8. ✅ **Easy to understand** - No black-box models

---

## 🚀 **Implementation Plan**

### Phase 1: Core (This Session)
- [x] Design architecture
- [ ] Create package structure
- [ ] Implement Lucene vector store
- [ ] Implement basic retrieval
- [ ] Add simple examples

### Phase 2: Indexers
- [ ] Java code indexer
- [ ] Database schema indexer
- [ ] Knowledge base indexer
- [ ] Metadata indexer

### Phase 3: Advanced
- [ ] Smart chunking
- [ ] Query rewriting
- [ ] Result ranking
- [ ] Caching

### Phase 4: Integration
- [ ] LLM integration
- [ ] UI integration
- [ ] Testing & optimization

---

## 📝 **Next Steps**

1. Download Apache Lucene 9.11.1 JARs
2. Create package structure
3. Implement core interfaces
4. Build Lucene vector store
5. Add examples
6. Test & verify

---

*Design Version: 1.0*  
*Created: November 13, 2025*  
*Status: 🎯 READY TO IMPLEMENT*

