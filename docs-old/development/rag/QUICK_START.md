# RAG System - Quick Start Guide

## 🚀 Quick Start (5 minutes)

### 1. Build the project
```bash
./scripts/build.sh    # macOS/Linux
scripts\build.bat     # Windows
```

### 2. Run the example
```bash
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*" \
  com.noteflix.pcm.rag.examples.BasicRAGExample
```

### 3. See the results!
```
=== Basic RAG Example (Offline) ===

✅ Indexed 4 documents

Query: "customer service validation"
  - Documents found: 3
  - Processing time: 30ms
  - Best match: Customer Registration Troubleshooting (6.6%)
```

---

## 💻 Basic Usage

### Step 1: Create Vector Store
```java
// Offline mode (Lucene)
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.lucene("data/rag/index")
);

// Or in-memory (for testing)
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.inMemory()
);
```

### Step 2: Create RAG Service
```java
RAGService rag = new DefaultRAGService(store);
```

### Step 3: Index Documents
```java
RAGDocument doc = RAGDocument.builder()
    .id(UUID.randomUUID().toString())
    .type(DocumentType.SOURCE_CODE)
    .title("CustomerService.java")
    .content("public class CustomerService { ... }")
    .indexedAt(LocalDateTime.now())
    .build();

rag.indexDocument(doc);
```

### Step 4: Query
```java
RAGResponse response = rag.query("How do I validate customers?");

System.out.println("Answer: " + response.getAnswer());
System.out.println("Found: " + response.getDocumentsRetrieved() + " docs");
System.out.println("Time: " + response.getProcessingTimeMs() + "ms");

// Access sources
for (RAGContext ctx : response.getSources()) {
    System.out.println("- " + ctx.getDocument().getTitle());
    System.out.println("  Score: " + ctx.getScore());
}
```

---

## 🔄 Switching Vector Stores

The system uses **Strategy Pattern** - easy to swap!

### Lucene (Offline)
```java
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.lucene("data/rag/index")
);
```

### Qdrant (When implemented)
```java
// Local
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.qdrantLocal()
);

// Remote
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.qdrant("api.qdrant.io", 6333, "api-key")
);
```

### In-Memory (Testing)
```java
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.inMemory()
);
```

**Same API for all implementations!**

---

## 📖 Document Types

```java
DocumentType.SOURCE_CODE        // Java, Python, etc.
DocumentType.DATABASE_SCHEMA    // DDL, tables
DocumentType.SYSTEM_METADATA    // Screens, workflows
DocumentType.KNOWLEDGE_BASE     // Markdown docs
DocumentType.OTHER              // Everything else
```

---

## 🔍 Advanced Querying

### Filter by Type
```java
RetrievalOptions options = RetrievalOptions.builder()
    .maxResults(5)
    .minScore(0.5)
    .build();

options.addTypes(DocumentType.SOURCE_CODE, DocumentType.DATABASE_SCHEMA);

RAGResponse response = rag.query("database connection", options);
```

### Filter by Metadata
```java
RetrievalOptions options = RetrievalOptions.builder()
    .build();

options.addFilter("package", "com.example.customer");
options.addFilter("schema", "PROD");

RAGResponse response = rag.query("customer validation", options);
```

---

## 📚 Chunking Large Documents

```java
import com.noteflix.pcm.rag.chunking.*;

// Create chunking strategy
ChunkingStrategy chunker = FixedSizeChunking.defaults();  // 1000 chars, 200 overlap

// Or custom size
ChunkingStrategy chunker = new FixedSizeChunking(500, 100);

// Chunk document
List<DocumentChunk> chunks = chunker.chunk(document);

// Index each chunk as separate document
for (DocumentChunk chunk : chunks) {
    RAGDocument chunkDoc = RAGDocument.builder()
        .id(chunk.getChunkId())
        .content(chunk.getContent())
        .type(document.getType())
        .title(document.getTitle() + " (chunk " + chunk.getIndex() + ")")
        .build();
    
    rag.indexDocument(chunkDoc);
}
```

---

## 🎯 Real-World Example

```java
// Initialize
VectorStore store = VectorStoreFactory.createDefault();
RAGService rag = new DefaultRAGService(store);

// Index Java files
File srcDir = new File("src/main/java");
for (File javaFile : findJavaFiles(srcDir)) {
    String content = Files.readString(javaFile.toPath());
    
    RAGDocument doc = RAGDocument.builder()
        .id(UUID.randomUUID().toString())
        .type(DocumentType.SOURCE_CODE)
        .title(javaFile.getName())
        .sourcePath(javaFile.getPath())
        .content(content)
        .indexedAt(LocalDateTime.now())
        .build();
    
    doc.addMetadata("language", "java");
    doc.addMetadata("package", extractPackage(content));
    
    rag.indexDocument(doc);
}

// Query
RAGResponse response = rag.query("How do I connect to database?");

// Use response
updateUIWithAnswer(response.getAnswer());
showSources(response.getSources());
```

---

## 📊 Performance Tips

1. **Batch indexing** for better performance:
   ```java
   List<RAGDocument> docs = loadAllDocs();
   rag.indexDocuments(docs);  // Faster than indexDocument() in loop
   ```

2. **Use appropriate chunk size**:
   - Small chunks (500): Better precision, more results
   - Large chunks (2000): Better context, fewer results
   - Default (1000): Balanced

3. **Set appropriate filters**:
   ```java
   options.setMaxResults(10);      // Limit results
   options.setMinScore(0.3);       // Filter low-quality matches
   options.addTypes(...);          // Reduce search space
   ```

4. **Close when done**:
   ```java
   rag.close();  // Release resources
   ```

---

## 🐛 Troubleshooting

### Build fails with "package org.apache.lucene does not exist"
```bash
# Check Lucene JARs
ls -la lib/rag/

# Should see:
# lucene-core-9.11.1.jar
# lucene-analyzers-common-9.11.1.jar
# lucene-queryparser-9.11.1.jar
# ...
```

### Example fails with "Cannot find vector store"
```bash
# Ensure build includes RAG libs
./scripts/build.sh

# Check classpath
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*" ...
```

### No results returned
```java
// Lower min score
options.setMinScore(0.0);

// Increase max results
options.setMaxResults(100);

// Check document count
long count = rag.getDocumentCount();
System.out.println("Indexed documents: " + count);
```

---

## 🔗 Related Docs

- [Implementation Summary](./RAG_IMPLEMENTATION_SUMMARY.md)
- [Offline Design](./OFFLINE_RAG_DESIGN.md)
- [Implementation Plan](./RAG_IMPLEMENTATION_PLAN.md)

---

## ✅ Next Steps

1. **Index your project**:
   - Java source files
   - SQL procedures
   - Knowledge base articles

2. **Integrate with UI**:
   - Add search box
   - Display results
   - Show sources

3. **Optional enhancements**:
   - Semantic search (embeddings)
   - LLM integration (answer generation)
   - Qdrant support (for scale)

---

**Ready to use! Happy coding! 🚀**

