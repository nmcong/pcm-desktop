# Apache Lucene Vector Store - Comprehensive Guide

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Core Components](#core-components)
4. [Implementation Details](#implementation-details)
5. [Configuration](#configuration)
6. [Performance Optimization](#performance-optimization)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)
9. [API Reference](#api-reference)

## Overview

Apache Lucene is a powerful, mature, and feature-rich information retrieval library written entirely in Java. Our `LuceneVectorStore` implementation provides a 100% offline, production-ready vector store solution with the following key characteristics:

### Key Features
- **100% Offline**: No external dependencies or cloud services required
- **BM25 Ranking**: Industry-standard ranking algorithm for text relevance
- **Full-Text Search**: Advanced query parsing with boolean operators
- **Metadata Filtering**: Rich filtering capabilities on document metadata
- **Persistent Storage**: File-system based index with ACID properties
- **Scalable**: Handles millions of documents efficiently
- **Thread-Safe**: Concurrent read/write operations supported

### Use Cases
- **Document Search**: Full-text search across large document collections
- **Knowledge Base**: Enterprise knowledge management systems
- **Content Discovery**: Finding relevant content based on text similarity
- **Research Applications**: Academic and research document retrieval
- **Compliance**: On-premise deployment for sensitive data

## Architecture

### High-Level Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │    │  LuceneVector   │    │   File System   │
│                 │───▶│     Store       │───▶│     Index       │
│   (RAG System)  │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Lucene Core   │
                       │                 │
                       │ • IndexWriter   │
                       │ • IndexSearcher │
                       │ • Analyzer      │
                       │ • QueryParser   │
                       └─────────────────┘
```

### Component Interaction Flow
```
1. Document Indexing:
   RAGDocument → LuceneDocument → IndexWriter → File System Index

2. Search Process:
   Query String → QueryParser → Lucene Query → IndexSearcher → Results

3. Resource Management:
   SearcherManager → IndexSearcher Pool → Concurrent Access
```

## Core Components

### 1. IndexWriter
**Purpose**: Manages document indexing and updates to the Lucene index.

**Key Responsibilities**:
- Document insertion, update, and deletion
- Index commits and transaction management
- Memory management and index optimization

**Configuration**:
```java
IndexWriterConfig config = new IndexWriterConfig(analyzer);
config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
config.setRAMBufferSizeMB(256.0); // Memory buffer for indexing
config.setMaxBufferedDocs(1000);   // Documents before flush
```

### 2. SearcherManager
**Purpose**: Manages IndexSearcher instances for concurrent search operations.

**Key Benefits**:
- Thread-safe searcher pooling
- Automatic refresh of search results after index updates
- Resource lifecycle management

**Usage Pattern**:
```java
IndexSearcher searcher = searcherManager.acquire();
try {
    // Perform search operations
    TopDocs results = searcher.search(query, maxResults);
} finally {
    searcherManager.release(searcher);
}
```

### 3. Analyzer
**Purpose**: Text processing and tokenization for indexing and searching.

**StandardAnalyzer Features**:
- Tokenization on whitespace and punctuation
- Lowercase normalization
- Stop word filtering (optional)
- Unicode normalization

**Custom Analyzer Example**:
```java
Analyzer customAnalyzer = new StandardAnalyzer(
    CharArraySet.copy(StopAnalyzer.ENGLISH_STOP_WORDS_SET)
);
```

### 4. Query Parser
**Purpose**: Converts human-readable queries into Lucene Query objects.

**Supported Query Types**:
- **Term Queries**: `java programming`
- **Phrase Queries**: `"machine learning"`
- **Boolean Queries**: `java AND programming`
- **Wildcard Queries**: `program*`
- **Range Queries**: `date:[2023-01-01 TO 2023-12-31]`

## Implementation Details

### Document Schema

Our implementation uses the following field mapping:

| Field Name | Lucene Type | Stored | Indexed | Purpose |
|------------|-------------|---------|---------|---------|
| `id` | StringField | YES | NO | Unique document identifier |
| `content` | TextField | YES | YES | Main searchable content |
| `type` | StringField | YES | YES | Document type filtering |
| `title` | TextField | YES | YES | Document title (searchable) |
| `sourcePath` | StringField | YES | NO | Original file path |
| `indexedAt` | StringField | YES | NO | Indexing timestamp |
| `meta_*` | StringField | YES | YES | Custom metadata fields |

### Indexing Process

```java
// 1. Convert RAGDocument to Lucene Document
Document luceneDoc = convertToLuceneDocument(ragDocument);

// 2. Create or update document in index
Term idTerm = new Term(FIELD_ID, document.getId());
writer.updateDocument(idTerm, luceneDoc);

// 3. Commit changes and refresh searchers
writer.commit();
searcherManager.maybeRefresh();
```

### Search Process

```java
// 1. Build complex query with filters
BooleanQuery.Builder builder = new BooleanQuery.Builder();

// Content search
QueryParser parser = new QueryParser(FIELD_CONTENT, analyzer);
Query contentQuery = parser.parse(queryString);
builder.add(contentQuery, BooleanClause.Occur.MUST);

// Type filtering
if (options.getTypes() != null) {
    BooleanQuery.Builder typeBuilder = new BooleanQuery.Builder();
    for (DocumentType type : options.getTypes()) {
        TermQuery typeQuery = new TermQuery(new Term(FIELD_TYPE, type.name()));
        typeBuilder.add(typeQuery, BooleanClause.Occur.SHOULD);
    }
    builder.add(typeBuilder.build(), BooleanClause.Occur.FILTER);
}

// 2. Execute search
TopDocs topDocs = searcher.search(builder.build(), maxResults);

// 3. Convert results with scoring
for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
    double normalizedScore = normalizeScore(scoreDoc.score);
    Document doc = searcher.doc(scoreDoc.doc);
    // Convert back to RAGDocument...
}
```

### Score Normalization

Our implementation uses dynamic score normalization:

```java
private volatile double maxScoreSeen = 1.0;

private double normalizeScore(float score) {
    // Update maximum score seen
    if (score > maxScoreSeen) {
        maxScoreSeen = score;
    }
    
    // Normalize to 0-1 range
    return Math.min(1.0, score / maxScoreSeen);
}
```

**Benefits**:
- Adaptive to different document collections
- Consistent scoring across different queries
- Improved relevance threshold filtering

## Configuration

### Index Configuration

```java
// Basic configuration
IndexWriterConfig config = new IndexWriterConfig(analyzer);
config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

// Performance tuning
config.setRAMBufferSizeMB(512.0);     // Larger buffer for better performance
config.setMaxBufferedDocs(2000);      // More docs before flush
config.setUseCompoundFile(false);     // Better performance, more files

// Merge policy for large indices
TieredMergePolicy mergePolicy = new TieredMergePolicy();
mergePolicy.setMaxMergeAtOnce(30);
mergePolicy.setSegmentsPerTier(30);
config.setMergePolicy(mergePolicy);
```

### Directory Configuration

```java
// File system directory (recommended for production)
Directory directory = FSDirectory.open(Paths.get(indexPath));

// RAM directory (for testing only)
Directory ramDirectory = new ByteBuffersDirectory();

// NIO directory for better performance on some systems
Directory nioDirectory = new NIOFSDirectory(Paths.get(indexPath));
```

### Analyzer Configuration

```java
// Standard analyzer with custom stop words
Set<String> customStopWords = new HashSet<>(Arrays.asList(
    "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by"
));
Analyzer analyzer = new StandardAnalyzer(CharArraySet.copy(customStopWords));

// Language-specific analyzer
Analyzer englishAnalyzer = new EnglishAnalyzer();

// Custom analyzer chain
Analyzer customAnalyzer = CustomAnalyzer.builder()
    .withTokenizer(StandardTokenizerFactory.class)
    .addTokenFilter(LowerCaseFilterFactory.class)
    .addTokenFilter(StopFilterFactory.class, "words", "stopwords.txt")
    .addTokenFilter(PorterStemFilterFactory.class)
    .build();
```

## Performance Optimization

### Memory Management

```java
// JVM heap settings for Lucene
-Xmx8g                    // Large heap for better performance
-XX:+UseG1GC              // G1 garbage collector
-XX:MaxGCPauseMillis=200  // Low latency GC pauses
```

### Index Optimization

```java
// Periodic optimization (not during peak hours)
writer.forceMerge(1);  // Merge to single segment (expensive)

// Or optimize partially
writer.forceMergeDeletes(); // Remove deleted documents only
```

### Search Performance

```java
// Warm up searchers
for (int i = 0; i < 10; i++) {
    searcher.search(new MatchAllDocsQuery(), 1);
}

// Use searcher pooling
SearcherFactory factory = new SearcherFactory() {
    @Override
    public IndexSearcher newSearcher(IndexReader reader, IndexReader previousReader) {
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(1.2f, 0.75f)); // Tuned BM25
        return searcher;
    }
};
```

### Batch Operations

```java
// Batch indexing for better performance
List<Document> batch = new ArrayList<>();
for (RAGDocument doc : documents) {
    batch.add(convertToLuceneDocument(doc));
    if (batch.size() >= 1000) {
        for (Document luceneDoc : batch) {
            writer.addDocument(luceneDoc);
        }
        batch.clear();
    }
}
writer.commit(); // Single commit for entire batch
```

## Best Practices

### 1. Resource Management
```java
// Always use try-with-resources or finally blocks
IndexSearcher searcher = null;
try {
    searcher = searcherManager.acquire();
    // Use searcher...
} finally {
    if (searcher != null) {
        searcherManager.release(searcher);
    }
}
```

### 2. Exception Handling
```java
// Use specific exceptions
try {
    // Lucene operations...
} catch (IOException e) {
    throw new VectorStoreException("Index operation failed", e);
} catch (ParseException e) {
    throw new VectorStoreException("Query parsing failed", e);
}
```

### 3. Input Validation
```java
// Validate all inputs
if (document == null || document.getId() == null || document.getId().trim().isEmpty()) {
    throw new VectorStoreException("Invalid document");
}
```

### 4. Logging Strategy
```java
// Use appropriate log levels
log.debug("Indexed document: {}", documentId);           // Detailed operations
log.info("Indexed {} documents in {}ms", count, time);   // Summary statistics
log.warn("Query fallback used for: {}", query);          // Potential issues
log.error("Index corruption detected", exception);        // Critical errors
```

### 5. Threading Considerations
```java
// SearcherManager is thread-safe
// IndexWriter is thread-safe for different operations
// Analyzer instances should not be shared between threads

// Good pattern:
private final SearcherManager searcherManager;
private final IndexWriter writer;
private final ThreadLocal<Analyzer> analyzerThreadLocal = 
    ThreadLocal.withInitial(() -> new StandardAnalyzer());
```

## Troubleshooting

### Common Issues

#### 1. Lock Acquisition Failed
**Error**: `LockObtainFailedException`
**Cause**: Another process holds the index lock
**Solution**:
```java
// Check for stale locks
if (IndexWriter.isLocked(directory)) {
    IndexWriter.unlock(directory);
}
```

#### 2. Too Many Open Files
**Error**: `IOException: Too many open files`
**Cause**: OS file descriptor limit exceeded
**Solution**:
```bash
# Increase file descriptor limit
ulimit -n 65536

# Or configure in /etc/security/limits.conf
* soft nofile 65536
* hard nofile 65536
```

#### 3. Out of Memory
**Error**: `OutOfMemoryError`
**Cause**: Large documents or insufficient heap
**Solution**:
```java
// Reduce RAM buffer size
config.setRAMBufferSizeMB(64.0);

// Process documents in smaller batches
int batchSize = 100; // Reduce from larger batch sizes
```

#### 4. Search Performance Degradation
**Symptoms**: Slow search responses
**Causes & Solutions**:
```java
// Cause: Too many segments
writer.forceMergeDeletes(); // Periodic cleanup

// Cause: Cold searchers
searcherManager.maybeRefreshBlocking(); // Warm up new searchers

// Cause: Complex queries
// Simplify queries or add query caching
```

### Debugging Tools

#### Index Statistics
```java
public void printIndexStats(IndexReader reader) {
    log.info("Total documents: {}", reader.numDocs());
    log.info("Deleted documents: {}", reader.numDeletedDocs());
    log.info("Index size: {} MB", getIndexSize());
    log.info("Number of segments: {}", reader.leaves().size());
}
```

#### Query Analysis
```java
public void analyzeQuery(String queryString) {
    QueryParser parser = new QueryParser(FIELD_CONTENT, analyzer);
    try {
        Query query = parser.parse(queryString);
        log.info("Parsed query: {}", query.toString());
    } catch (ParseException e) {
        log.error("Query parsing failed: {}", queryString, e);
    }
}
```

#### Performance Monitoring
```java
public class LuceneMetrics {
    private final Timer indexTimer = Timer.newTimer("lucene.index.time");
    private final Timer searchTimer = Timer.newTimer("lucene.search.time");
    private final Counter indexedDocs = Counter.newCounter("lucene.docs.indexed");
    private final Gauge indexSize = Gauge.newGauge("lucene.index.size", this::getIndexSize);
}
```

## API Reference

### Constructor
```java
public LuceneVectorStore(String indexPath) throws VectorStoreException
```
Creates a new Lucene vector store at the specified path.

### Core Operations

#### Document Management
```java
// Single document operations
void indexDocument(RAGDocument document) throws VectorStoreException
void deleteDocument(String documentId) throws VectorStoreException
RAGDocument getDocument(String documentId) throws VectorStoreException
boolean exists(String documentId)

// Batch operations
void indexDocuments(List<RAGDocument> documents) throws VectorStoreException
void deleteDocuments(List<String> documentIds) throws VectorStoreException

// Index management
void clear() throws VectorStoreException
long getDocumentCount()
void close()
```

#### Search Operations
```java
List<ScoredDocument> search(String query, RetrievalOptions options) throws VectorStoreException
```

### RetrievalOptions
```java
public class RetrievalOptions {
    private int maxResults = 10;
    private double minScore = 0.0;
    private Set<DocumentType> types;
    private Map<String, String> filters;
    private boolean includeSnippets = false;
    
    // Getters and setters...
}
```

### Exception Hierarchy
```java
VectorStoreException
├── IndexingException
├── SearchException
├── ValidationException
└── ConfigurationException
```

### Usage Examples

#### Basic Search
```java
LuceneVectorStore store = new LuceneVectorStore("/path/to/index");

RetrievalOptions options = new RetrievalOptions();
options.setMaxResults(20);
options.setMinScore(0.1);

List<ScoredDocument> results = store.search("machine learning", options);
```

#### Advanced Search with Filters
```java
RetrievalOptions options = new RetrievalOptions();
options.setTypes(Set.of(DocumentType.RESEARCH_PAPER, DocumentType.ARTICLE));
options.setFilters(Map.of("author", "John Doe", "year", "2023"));
options.setIncludeSnippets(true);

List<ScoredDocument> results = store.search("neural networks", options);
```

#### Batch Indexing
```java
List<RAGDocument> documents = loadDocuments();
store.indexDocuments(documents);
```

---

**Next**: [Qdrant vs Lucene Comparison](qdrant-vs-lucene.md)