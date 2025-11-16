# RAG & Search Architecture - Detailed Design Specification

**Tạo từ các file nguồn:**

- `docs/RaD/ideas/rag-strategy.md`
- `docs/RaD/ideas/semantic-search.md`
- `docs/RaD/ideas/source-search-guide.md`

**Phiên bản:** 1.0  
**Ngày tạo:** 2025-11-15

---

## 1. Tổng quan

Tài liệu này mô tả chi tiết kiến trúc Retrieval-Augmented Generation (RAG) và hệ thống tìm kiếm của PCM Desktop, bao
gồm:

- Hybrid retrieval (vector + lexical search)
- Chunking strategies
- Embedding generation và caching
- FTS5 full-text search với BM25 scoring
- Context assembly và ranking
- LLM integration

### 1.1 Mục tiêu

1. ✅ Cung cấp câu trả lời chính xác, có truy xuất nguồn cho yêu cầu của người dùng
2. ✅ Hỗ trợ cả truy vấn code-centric và documentation-centric
3. ✅ Độ trễ tối thiểu (< 2s cho retrieval, < 10s cho full response)
4. ✅ Đảm bảo reproducibility và observability
5. ✅ Support offline mode (local embeddings + LLM)

### 1.2 Architecture Overview

```
┌─────────────┐
│ User Query  │
└──────┬──────┘
       │
       ▼
┌──────────────────────────────────────┐
│   Intent & Filter Classification     │
│  - Extract keywords                  │
│  - Infer project/subsystem           │
│  - Determine search scope            │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│      Hybrid Retriever Layer          │
│  ┌─────────────┐  ┌──────────────┐  │
│  │   Vector    │  │   Lexical    │  │
│  │   Search    │  │   Search     │  │
│  │  (Qdrant)   │  │  (FTS5)      │  │
│  └─────────────┘  └──────────────┘  │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│        Context Builder               │
│  - Deduplicate results               │
│  - Rank by relevance                 │
│  - Add AST metadata                  │
│  - Limit token budget                │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│          LLM Provider                │
│  - OpenAI / Anthropic / Local        │
│  - Streaming response                │
│  - Citation extraction               │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│       Response Logging               │
│  - agent_responses                   │
│  - request_artifacts                 │
│  - answer_feedback                   │
└──────────────────────────────────────┘
```

---

## 2. Technology Stack

### 2.1 Component Selection

| Layer                | Technology                                          | Rationale                                          |
|----------------------|-----------------------------------------------------|----------------------------------------------------|
| **Embeddings**       | OpenAI text-embedding-3-large / local BGE           | High quality, configurable per deployment          |
| **Vector DB**        | Qdrant                                              | HNSW index, payload filtering, on-disk collections |
| **Keyword Search**   | SQLite FTS5                                         | Built-in, BM25 scoring, no external deps           |
| **Parser/Chunker**   | tree-sitter, JavaParser                             | Language-aware chunking aligned with AST           |
| **LLM**              | OpenAI GPT-4o, Anthropic Claude 3.5, Local (Llama3) | Configurable provider registry                     |
| **Orchestration**    | Java services with async tasks                      | Integrate with existing stack                      |
| **Prompt Framework** | Template engine / LangChain4j                       | Deterministic prompts                              |

### 2.2 Deployment Modes

**Cloud Mode:**

- OpenAI embeddings + GPT-4o
- Qdrant Cloud or local Qdrant
- Internet required

**Hybrid Mode:**

- Local embeddings (BGE-large ONNX)
- Local Qdrant
- Cloud LLM fallback

**Offline Mode:**

- Local embeddings
- Local Qdrant
- Local LLM (Llama3 via Ollama)

---

## 3. Data Preparation

### 3.1 Chunking Strategy

#### Code Chunking

**Parameters:**

- Chunk size: 150-300 tokens
- Overlap: 20% (30-60 tokens)
- Method: AST-aware splitting

**Rules:**

1. Keep complete functions/methods intact if < 300 tokens
2. Split large classes at method boundaries
3. Include class declaration context in each method chunk
4. Preserve imports and package declarations in metadata

**Example:**

```java
// Original file: AuthService.java (500 lines)

// Chunk 1 (lines 1-50): Package + imports + class declaration
package com.example.auth;
import ...;
public class AuthService {
    // fields
}

// Chunk 2 (lines 45-95): login method with context
public class AuthService {
    public void login(User user) {
        // implementation
    }
}

// Chunk 3 (lines 90-140): logout method with context
public class AuthService {
    public void logout(String token) {
        // implementation
    }
}
```

**Metadata per chunk:**

```json
{
  "chunk_id": "auth-service-login-v1",
  "file_path": "src/main/java/com/example/auth/AuthService.java",
  "language": "java",
  "node_type": "method",
  "fq_name": "com.example.auth.AuthService.login",
  "start_line": 45,
  "end_line": 95,
  "imports": ["java.util.*", "com.example.model.User"],
  "class_context": "AuthService"
}
```

#### Documentation Chunking

**Parameters:**

- Chunk size: 300-500 tokens
- Overlap: 15% (45-75 tokens)
- Method: Heading-aware splitting

**Rules:**

1. Split at heading boundaries (H1-H6)
2. Keep heading hierarchy in metadata
3. Include parent section context
4. Preserve code blocks intact

**Example:**

```markdown
# Authentication Guide

## Overview
This guide explains...

## Login Process
The login process involves...

### Step 1: Validate Credentials
First, validate...

### Step 2: Generate Token
After validation...
```

**Chunks:**

```
Chunk 1:
# Authentication Guide
## Overview
This guide explains...

Chunk 2:
# Authentication Guide > Login Process
The login process involves...
### Step 1: Validate Credentials
First, validate...

Chunk 3:
# Authentication Guide > Login Process
### Step 2: Generate Token
After validation...
```

**Metadata:**

```json
{
  "chunk_id": "auth-guide-login-step1",
  "source_type": "doc",
  "label": "Authentication Guide",
  "toc_path": "Authentication Guide > Login Process > Step 1",
  "order_index": 2,
  "has_code_blocks": true
}
```

#### Agent Response Chunking

**Parameters:**

- Chunk size: 200-400 tokens
- No overlap (responses are self-contained)
- Include full conversation context in metadata

**Use case:** Reuse previous good answers for similar questions

### 3.2 Embedding Generation

#### Process Flow

```
1. Extract chunk content
2. Normalize text:
   - Strip excessive whitespace
   - Remove code comments (optional)
   - Truncate to model max tokens
3. Generate checksum (for caching)
4. Check cache: if exists, reuse embedding
5. Call embedding API/model
6. Store in cache with checksum key
7. Upsert to Qdrant with payload
```

#### Caching Strategy

**Cache table:**

```sql
CREATE TABLE IF NOT EXISTS embedding_cache (
    cache_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    content_hash  TEXT UNIQUE NOT NULL,
    model_name    TEXT NOT NULL,
    embedding     BLOB NOT NULL,
    token_count   INTEGER,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_embedding_cache_hash ON embedding_cache(content_hash, model_name);
```

**Cache invalidation:**

- Never expire (embeddings are immutable per content hash)
- Only clear when model changes
- Size limit: 100k entries (~4GB for 1536-dim vectors)

#### Batching

**Batch parameters:**

- Batch size: 100-500 chunks (depending on API limits)
- Parallel batches: 3-5 (rate limiting)
- Retry logic: Exponential backoff (1s, 2s, 4s, 8s)

**Code example:**

```java
public class EmbeddingBatchProcessor {
    private static final int BATCH_SIZE = 100;
    private final EmbeddingService embeddingService;
    private final EmbeddingCache cache;
    
    public List<Embedding> processChunks(List<Chunk> chunks) {
        List<Embedding> results = new ArrayList<>();
        
        for (List<Chunk> batch : partition(chunks, BATCH_SIZE)) {
            // Check cache first
            List<Chunk> uncached = new ArrayList<>();
            for (Chunk chunk : batch) {
                String hash = computeHash(chunk.getContent());
                Embedding cached = cache.get(hash);
                if (cached != null) {
                    results.add(cached);
                } else {
                    uncached.add(chunk);
                }
            }
            
            // Generate embeddings for uncached
            if (!uncached.isEmpty()) {
                List<Embedding> generated = embeddingService.generateBatch(uncached);
                for (int i = 0; i < uncached.size(); i++) {
                    String hash = computeHash(uncached.get(i).getContent());
                    cache.put(hash, generated.get(i));
                    results.add(generated.get(i));
                }
            }
        }
        
        return results;
    }
}
```

#### Model Selection

**OpenAI:**

- `text-embedding-3-large`: 3072 dims, high quality, $0.13/1M tokens
- `text-embedding-3-small`: 1536 dims, fast, $0.02/1M tokens

**Local (ONNX):**

- `bge-large-en-v1.5`: 1024 dims, free, ~200ms/batch on CPU
- `all-MiniLM-L6-v2`: 384 dims, fast, ~50ms/batch on CPU

**Selection criteria:**

- Large projects (>100k files): Use small/fast model
- High-quality requirements: Use large model
- Offline/air-gapped: Use local model

---

## 4. Lexical Search (FTS5)

### 4.1 FTS5 Configuration

**Schema:**

```sql
CREATE VIRTUAL TABLE search_index USING fts5(
    corpus_id UNINDEXED,
    project_id UNINDEXED,
    source_type,
    label,
    content,
    tokenize = 'unicode61 remove_diacritics 2',
    detail = 'column'
);
```

**Tokenizer options:**

- `unicode61`: Full Unicode support (CJK, Arabic, etc.)
- `remove_diacritics 2`: Normalize accents (café → cafe)
- `detail='column'`: Store per-column statistics for accurate BM25

### 4.2 Query Syntax

**Basic search:**

```sql
SELECT corpus_id, bm25(search_index, 1.2, 0.75) AS score
FROM search_index
WHERE search_index MATCH 'user authentication'
ORDER BY score
LIMIT 50;
```

**Boolean operators:**

```sql
-- AND
WHERE search_index MATCH 'login AND password'

-- OR
WHERE search_index MATCH 'login OR signin'

-- NOT
WHERE search_index MATCH 'login NOT oauth'

-- Phrase
WHERE search_index MATCH '"user authentication"'

-- Prefix
WHERE search_index MATCH 'auth*'

-- Column-specific
WHERE search_index MATCH 'label:AuthService'
```

**Advanced scoring:**

```sql
-- Custom BM25 parameters
SELECT *, bm25(search_index, 
               1.2,  -- k1 (term frequency saturation)
               0.75  -- b (length normalization)
       ) AS score
FROM search_index
WHERE search_index MATCH :query
  AND project_id = :projectId
  AND source_type IN ('code', 'doc')
ORDER BY score
LIMIT 50;
```

### 4.3 Snippet Generation

```sql
-- Generate highlighted snippets
SELECT snippet(search_index, 4,  -- column index (content)
               '<mark>', '</mark>',  -- highlight tags
               '...', 32  -- ellipsis and context tokens
       ) AS highlighted
FROM search_index
WHERE search_index MATCH :query;
```

**Output example:**

```
...public void <mark>login</mark>(User user) {
    validate<mark>User</mark>(user);
    String token = generate<mark>Token</mark>...
```

### 4.4 Performance Optimization

**Optimize index:**

```sql
-- Run nightly
INSERT INTO search_index(search_index) VALUES('optimize');

-- Check index size
SELECT (SELECT page_count FROM pragma_page_count()) * 
       (SELECT page_size FROM pragma_page_size()) / 1024 / 1024 
       AS size_mb;
```

**Query rewriting:**

```java
public class QueryRewriter {
    public String rewrite(String userQuery) {
        // Expand abbreviations
        userQuery = userQuery.replace("auth", "authentication OR auth");
        
        // Add wildcards for incomplete terms
        String[] terms = userQuery.split("\\s+");
        for (int i = 0; i < terms.length; i++) {
            if (terms[i].length() > 3 && !terms[i].endsWith("*")) {
                terms[i] += "*";
            }
        }
        
        return String.join(" ", terms);
    }
}
```

---

## 5. Vector Search (Qdrant)

### 5.1 Collection Setup

**Collection schema:**

```json
{
  "name": "pcm_chunks",
  "config": {
    "vector_size": 1536,
    "distance": "Cosine",
    "on_disk": true,
    "replication_factor": 1,
    "shard_number": 1,
    "payload_schema": {
      "chunk_id": "keyword",
      "project_id": "integer",
      "file_path": "keyword",
      "language": "keyword",
      "content_type": "keyword",
      "start_line": "integer",
      "end_line": "integer"
    }
  },
  "hnsw_config": {
    "m": 16,
    "ef_construct": 100
  }
}
```

**Parameters:**

- `distance="Cosine"`: Best for embeddings (range 0-2, lower is better)
- `on_disk=true`: Reduce memory usage
- `m=16`: HNSW graph connections (balance quality/speed)
- `ef_construct=100`: Build time quality (higher = better index)

### 5.2 Indexing (Upsert)

**Single point:**

```java
public void upsertChunk(VectorDocument doc) {
    Points.Point point = Points.Point.newBuilder()
        .setId(Points.PointId.newBuilder()
            .setUuid(doc.getChunkId())
            .build())
        .setVector(toVector(doc.getEmbedding()))
        .putAllPayload(Map.of(
            "chunk_id", Values.value(doc.getChunkId()),
            "project_id", Values.value(doc.getProjectId()),
            "file_path", Values.value(doc.getFilePath()),
            "language", Values.value(doc.getLanguage()),
            "content", Values.value(doc.getContent()),
            "start_line", Values.value(doc.getStartLine()),
            "end_line", Values.value(doc.getEndLine())
        ))
        .build();
    
    client.upsert(collectionName, List.of(point));
}
```

**Batch upsert:**

```java
public void upsertBatch(List<VectorDocument> docs) {
    List<Points.Point> points = docs.stream()
        .map(this::toPoint)
        .collect(Collectors.toList());
    
    // Split into chunks of 100 (Qdrant limit)
    for (List<Points.Point> batch : partition(points, 100)) {
        client.upsert(collectionName, batch);
    }
}
```

### 5.3 Search (Query)

**Basic search:**

```java
public List<ScoredPoint> search(float[] queryVector, int topK) {
    SearchRequest request = SearchRequest.newBuilder()
        .setCollectionName(collectionName)
        .addAllVector(toFloatList(queryVector))
        .setLimit(topK)
        .setWithPayload(WithPayloadSelector.newBuilder()
            .setEnable(true)
            .build())
        .build();
    
    return client.search(request);
}
```

**Filtered search:**

```java
public List<ScoredPoint> searchWithFilter(
    float[] queryVector, 
    int topK, 
    int projectId,
    String language
) {
    Filter filter = Filter.newBuilder()
        .addMust(Condition.newBuilder()
            .setField(FieldCondition.newBuilder()
                .setKey("project_id")
                .setMatch(Match.newBuilder()
                    .setInteger(projectId)
                    .build())
                .build())
            .build())
        .addMust(Condition.newBuilder()
            .setField(FieldCondition.newBuilder()
                .setKey("language")
                .setMatch(Match.newBuilder()
                    .setValue(language)
                    .build())
                .build())
            .build())
        .build();
    
    SearchRequest request = SearchRequest.newBuilder()
        .setCollectionName(collectionName)
        .addAllVector(toFloatList(queryVector))
        .setFilter(filter)
        .setLimit(topK)
        .setWithPayload(WithPayloadSelector.newBuilder()
            .setEnable(true)
            .build())
        .build();
    
    return client.search(request);
}
```

**Advanced: Scroll (for batch processing):**

```java
public List<ScoredPoint> scrollAll(Filter filter) {
    List<ScoredPoint> allPoints = new ArrayList<>();
    String offset = null;
    
    do {
        ScrollRequest request = ScrollRequest.newBuilder()
            .setCollectionName(collectionName)
            .setFilter(filter)
            .setLimit(100)
            .setOffset(offset != null ? 
                Points.PointId.newBuilder().setUuid(offset).build() : 
                null)
            .build();
        
        ScrollResponse response = client.scroll(request);
        allPoints.addAll(response.getResultList());
        offset = response.hasNextPageOffset() ? 
            response.getNextPageOffset().getUuid() : null;
    } while (offset != null);
    
    return allPoints;
}
```

### 5.4 Performance Tuning

**Search parameters:**

```java
SearchParams params = SearchParams.newBuilder()
    .setHnswEf(128)  // Higher = more accurate but slower
    .setExact(false)  // false = use HNSW approximation
    .build();

SearchRequest request = SearchRequest.newBuilder()
    .setCollectionName(collectionName)
    .addAllVector(queryVector)
    .setLimit(topK)
    .setParams(params)
    .build();
```

**Recommendations:**

- `hnsw_ef=128`: Good balance (default=128, range 4-512)
- `exact=false`: Use HNSW (10-100x faster)
- `exact=true`: Brute-force (only for <10k vectors)

---

## 6. Hybrid Retrieval

### 6.1 Fusion Algorithm

**Reciprocal Rank Fusion (RRF):**

```java
public class ReciprocalRankFusion {
    private static final double K = 60.0;
    
    public List<ChunkHit> fuse(
        List<ChunkHit> vectorResults,
        List<ChunkHit> lexicalResults
    ) {
        Map<String, Double> scores = new HashMap<>();
        
        // Score from vector search
        for (int i = 0; i < vectorResults.size(); i++) {
            ChunkHit hit = vectorResults.get(i);
            double score = 1.0 / (K + i + 1);
            scores.merge(hit.getChunkId(), score, Double::sum);
        }
        
        // Score from lexical search
        for (int i = 0; i < lexicalResults.size(); i++) {
            ChunkHit hit = lexicalResults.get(i);
            double score = 1.0 / (K + i + 1);
            scores.merge(hit.getChunkId(), score, Double::sum);
        }
        
        // Deduplicate and sort
        Set<String> seen = new HashSet<>();
        List<ChunkHit> fused = new ArrayList<>();
        
        scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(entry -> {
                if (seen.add(entry.getKey())) {
                    ChunkHit hit = findChunk(entry.getKey(), 
                                            vectorResults, lexicalResults);
                    hit.setFusedScore(entry.getValue());
                    fused.add(hit);
                }
            });
        
        return fused;
    }
}
```

**Weighted Sum:**

```java
public class WeightedFusion {
    private final double vectorWeight = 0.7;
    private final double lexicalWeight = 0.3;
    
    public List<ChunkHit> fuse(
        List<ChunkHit> vectorResults,
        List<ChunkHit> lexicalResults
    ) {
        Map<String, ChunkHit> merged = new HashMap<>();
        
        // Normalize vector scores (cosine distance → similarity)
        for (ChunkHit hit : vectorResults) {
            double normalized = 1.0 - (hit.getScore() / 2.0);  // cosine [0,2] → [1,0]
            hit.setNormalizedScore(normalized * vectorWeight);
            merged.put(hit.getChunkId(), hit);
        }
        
        // Normalize lexical scores (BM25 → 0-1 range)
        double maxBm25 = lexicalResults.stream()
            .mapToDouble(ChunkHit::getScore)
            .max()
            .orElse(1.0);
        
        for (ChunkHit hit : lexicalResults) {
            double normalized = hit.getScore() / maxBm25;
            double weighted = normalized * lexicalWeight;
            
            ChunkHit existing = merged.get(hit.getChunkId());
            if (existing != null) {
                existing.setNormalizedScore(
                    existing.getNormalizedScore() + weighted);
            } else {
                hit.setNormalizedScore(weighted);
                merged.put(hit.getChunkId(), hit);
            }
        }
        
        return merged.values().stream()
            .sorted(Comparator.comparingDouble(ChunkHit::getNormalizedScore).reversed())
            .collect(Collectors.toList());
    }
}
```

### 6.2 Deduplication

**Strategy:**

```java
public List<ChunkHit> deduplicate(List<ChunkHit> hits) {
    Map<String, ChunkHit> byFileAndLine = new HashMap<>();
    
    for (ChunkHit hit : hits) {
        String key = hit.getFilePath() + ":" + 
                     hit.getStartLine() + "-" + hit.getEndLine();
        
        ChunkHit existing = byFileAndLine.get(key);
        if (existing == null || hit.getScore() > existing.getScore()) {
            byFileAndLine.put(key, hit);
        }
    }
    
    return new ArrayList<>(byFileAndLine.values());
}
```

### 6.3 Re-ranking (Optional)

**Cross-encoder re-ranking:**

```java
public class CrossEncoderReranker {
    private final CrossEncoder model;
    
    public List<ChunkHit> rerank(String query, List<ChunkHit> candidates) {
        // Score each candidate with cross-encoder
        List<RerankScore> scores = new ArrayList<>();
        
        for (ChunkHit hit : candidates) {
            String pair = query + " [SEP] " + hit.getContent();
            float score = model.predict(pair);
            scores.add(new RerankScore(hit, score));
        }
        
        // Sort by cross-encoder score
        scores.sort(Comparator.comparingDouble(
            RerankScore::getScore).reversed());
        
        return scores.stream()
            .map(RerankScore::getHit)
            .collect(Collectors.toList());
    }
}
```

**When to use:**

- Top-k candidates (k=50-100) from hybrid search
- Rerank to top-n (n=10-20) for context
- Cost: ~100ms per batch on GPU, ~500ms on CPU

---

## 7. Context Assembly

### 7.1 Token Budget

**Limits:**

```java
public class ContextBudget {
    private static final int MAX_CONTEXT_TOKENS = 4000;
    private static final int RESERVED_FOR_SYSTEM = 500;
    private static final int RESERVED_FOR_QUESTION = 200;
    
    public int getAvailableTokens() {
        return MAX_CONTEXT_TOKENS - RESERVED_FOR_SYSTEM - RESERVED_FOR_QUESTION;
    }
}
```

**Allocation strategy:**

- System prompt: 500 tokens
- User question: 200 tokens
- Context chunks: 3300 tokens (≈10-15 chunks)
- Model response: Separate (completion budget)

### 7.2 Chunk Selection

```java
public List<ChunkHit> selectChunks(
    List<ChunkHit> rankedHits, 
    int tokenBudget
) {
    List<ChunkHit> selected = new ArrayList<>();
    int usedTokens = 0;
    
    for (ChunkHit hit : rankedHits) {
        int chunkTokens = hit.getTokenCount();
        
        if (usedTokens + chunkTokens <= tokenBudget) {
            selected.add(hit);
            usedTokens += chunkTokens;
        } else {
            break;
        }
    }
    
    return selected;
}
```

### 7.3 AST Metadata Enrichment

```java
public List<EnrichedChunk> enrichWithAst(List<ChunkHit> chunks) {
    List<EnrichedChunk> enriched = new ArrayList<>();
    
    for (ChunkHit chunk : chunks) {
        // Fetch AST node if available
        AstNode node = astRepository.findByFileAndLine(
            chunk.getFileId(), 
            chunk.getStartLine()
        );
        
        EnrichedChunk e = new EnrichedChunk(chunk);
        
        if (node != null) {
            e.setNodeType(node.getNodeType());
            e.setFullyQualifiedName(node.getFqName());
            
            // Add parent context (class declaration)
            AstNode parent = astRepository.findParent(node.getNodeId());
            if (parent != null) {
                e.setParentContext(parent.getName());
            }
            
            // Add relationships (calls, extends, etc.)
            List<AstRelationship> rels = 
                relationshipRepository.findByNode(node.getNodeId());
            e.setRelationships(rels);
        }
        
        enriched.add(e);
    }
    
    return enriched;
}
```

### 7.4 Context Formatting

```java
public String formatContext(List<EnrichedChunk> chunks) {
    StringBuilder context = new StringBuilder();
    
    for (int i = 0; i < chunks.size(); i++) {
        EnrichedChunk chunk = chunks.get(i);
        
        context.append(String.format("=== Source %d ===\n", i + 1));
        context.append(String.format("File: %s\n", chunk.getFilePath()));
        context.append(String.format("Lines: %d-%d\n", 
            chunk.getStartLine(), chunk.getEndLine()));
        
        if (chunk.getFullyQualifiedName() != null) {
            context.append(String.format("Symbol: %s\n", 
                chunk.getFullyQualifiedName()));
        }
        
        context.append(String.format("Language: %s\n", chunk.getLanguage()));
        context.append("\n```" + chunk.getLanguage() + "\n");
        context.append(chunk.getContent());
        context.append("\n```\n\n");
    }
    
    return context.toString();
}
```

**Output example:**

```
=== Source 1 ===
File: src/main/java/com/example/auth/AuthService.java
Lines: 45-95
Symbol: com.example.auth.AuthService.login
Language: java

```java
public void login(User user) {
    if (user == null || user.getUsername() == null) {
        throw new IllegalArgumentException("Invalid user");
    }
    
    // Validate credentials
    if (!validateCredentials(user)) {
        throw new AuthenticationException("Invalid credentials");
    }
    
    // Generate token
    String token = tokenService.generateToken(user);
    
    // Store session
    sessionRepository.save(new Session(user.getId(), token));
}
```

=== Source 2 ===
...

```

---

## 8. LLM Integration

### 8.1 Provider Registry

```java
public interface LlmProvider {
    String getName();
    CompletableFuture<String> generate(String prompt, LlmConfig config);
    CompletableFuture<Stream<String>> stream(String prompt, LlmConfig config);
}

public class LlmProviderRegistry {
    private final Map<String, LlmProvider> providers = new HashMap<>();
    
    public void register(String name, LlmProvider provider) {
        providers.put(name, provider);
    }
    
    public LlmProvider get(String name) {
        return providers.getOrDefault(name, getDefault());
    }
    
    private LlmProvider getDefault() {
        return providers.get("openai");
    }
}
```

**Providers:**

- OpenAI (GPT-4o, GPT-4-turbo)
- Anthropic (Claude 3.5 Sonnet)
- Local (Ollama, LlamaCpp)

### 8.2 Prompt Template

```java
public class PromptTemplate {
    private static final String SYSTEM_PROMPT = """
        You are an AI assistant helping developers analyze source code.
        
        When answering:
        1. Always cite sources using [Source N] notation
        2. Reference specific file paths and line numbers
        3. Explain your reasoning step by step
        4. If unsure, say so explicitly
        5. Suggest next steps or follow-up questions
        
        Available context:
        - Source code snippets with file paths and line numbers
        - AST metadata (classes, methods, relationships)
        - Documentation excerpts
        """;
    
    public String build(String question, String context) {
        return String.format("""
            <|system|>
            %s
            <|context|>
            %s
            <|question|>
            %s
            <|assistant|>
            """, SYSTEM_PROMPT, context, question);
    }
}
```

### 8.3 Streaming Response

```java
public class StreamingResponseHandler {
    private final StringBuilder accumulated = new StringBuilder();
    private final List<String> citations = new ArrayList<>();
    
    public void onChunk(String chunk) {
        accumulated.append(chunk);
        
        // Extract citations on the fly
        Pattern pattern = Pattern.compile("\\[Source (\\d+)\\]");
        Matcher matcher = pattern.matcher(chunk);
        while (matcher.find()) {
            citations.add(matcher.group(1));
        }
        
        // Send to UI
        ui.appendToChat(chunk);
    }
    
    public void onComplete() {
        String fullResponse = accumulated.toString();
        
        // Store in database
        agentResponseRepository.save(new AgentResponse(
            requestId,
            fullResponse,
            citations
        ));
        
        // Log artifacts (retrieved chunks)
        for (String citationIndex : citations) {
            int index = Integer.parseInt(citationIndex) - 1;
            ChunkHit chunk = retrievedChunks.get(index);
            
            requestArtifactRepository.save(new RequestArtifact(
                requestId,
                "retrieved_chunk",
                chunk.getFilePath(),
                chunk.toJson()
            ));
        }
    }
}
```

### 8.4 Citation Extraction

```java
public class CitationExtractor {
    public List<Citation> extract(String response, List<ChunkHit> sources) {
        List<Citation> citations = new ArrayList<>();
        
        Pattern pattern = Pattern.compile(
            "\\[Source (\\d+)\\]|" +  // [Source 1]
            "\\(([^:]+):(\\d+)-(\\d+)\\)|" +  // (file.java:10-20)
            "`([^`]+)`"  // `ClassName.method`
        );
        
        Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // [Source N] format
                int index = Integer.parseInt(matcher.group(1)) - 1;
                if (index < sources.size()) {
                    ChunkHit chunk = sources.get(index);
                    citations.add(new Citation(
                        chunk.getFilePath(),
                        chunk.getStartLine(),
                        chunk.getEndLine(),
                        "source_reference"
                    ));
                }
            } else if (matcher.group(2) != null) {
                // (file:lines) format
                citations.add(new Citation(
                    matcher.group(2),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)),
                    "inline_reference"
                ));
            } else if (matcher.group(5) != null) {
                // `symbol` format
                citations.add(new Citation(
                    null,
                    null,
                    null,
                    "symbol_reference",
                    matcher.group(5)
                ));
            }
        }
        
        return citations;
    }
}
```

---

## 9. Quality & Monitoring

### 9.1 Metrics Collection

```java
public class RetrievalMetrics {
    public void recordRetrieval(
        String requestId,
        long vectorSearchMs,
        long lexicalSearchMs,
        int vectorHits,
        int lexicalHits,
        int fusedHits,
        int selectedChunks
    ) {
        MetricsRegistry.timer("retrieval.vector.duration")
            .record(Duration.ofMillis(vectorSearchMs));
        
        MetricsRegistry.timer("retrieval.lexical.duration")
            .record(Duration.ofMillis(lexicalSearchMs));
        
        MetricsRegistry.counter("retrieval.vector.hits")
            .increment(vectorHits);
        
        MetricsRegistry.counter("retrieval.lexical.hits")
            .increment(lexicalHits);
        
        MetricsRegistry.histogram("retrieval.fused.hits")
            .record(fusedHits);
        
        MetricsRegistry.histogram("retrieval.selected.chunks")
            .record(selectedChunks);
    }
}
```

### 9.2 Feedback Loop

```sql
-- Analyze low-rated responses
SELECT 
    r.request_id,
    r.description,
    ar.answer_text,
    af.rating,
    af.comment,
    (SELECT COUNT(*) FROM request_artifacts 
     WHERE request_id = r.request_id 
       AND artifact_type = 'retrieved_chunk') AS chunks_used
FROM user_requests r
JOIN agent_responses ar ON r.request_id = ar.request_id
JOIN answer_feedback af ON ar.response_id = af.response_id
WHERE af.rating <= 2
ORDER BY af.created_at DESC
LIMIT 50;
```

### 9.3 Evaluation Harness

```java
public class RagEvaluator {
    public EvaluationResults evaluate(List<TestCase> testCases) {
        List<TestResult> results = new ArrayList<>();
        
        for (TestCase testCase : testCases) {
            // Run retrieval
            List<ChunkHit> retrieved = hybridRetrieve(
                testCase.getQuery(),
                testCase.getProjectId()
            );
            
            // Calculate metrics
            Set<String> retrievedFiles = retrieved.stream()
                .map(ChunkHit::getFilePath)
                .collect(Collectors.toSet());
            
            Set<String> groundTruthFiles = testCase.getRelevantFiles();
            
            int truePositives = Sets.intersection(
                retrievedFiles, groundTruthFiles).size();
            int falsePositives = retrievedFiles.size() - truePositives;
            int falseNegatives = groundTruthFiles.size() - truePositives;
            
            double precision = truePositives / (double) (truePositives + falsePositives);
            double recall = truePositives / (double) (truePositives + falseNegatives);
            double f1 = 2 * (precision * recall) / (precision + recall);
            
            results.add(new TestResult(testCase, precision, recall, f1));
        }
        
        return new EvaluationResults(results);
    }
}
```

**Golden test set structure:**

```json
{
  "test_cases": [
    {
      "id": "auth-001",
      "query": "How does user authentication work?",
      "project_id": 5,
      "relevant_files": [
        "src/main/java/com/example/auth/AuthService.java",
        "src/main/java/com/example/auth/TokenService.java",
        "docs/authentication.md"
      ],
      "relevant_symbols": [
        "com.example.auth.AuthService.login",
        "com.example.auth.TokenService.generateToken"
      ]
    }
  ]
}
```

### 9.4 Alerting

**Prometheus alerts:**

```yaml
groups:
  - name: rag_alerts
    rules:
      - alert: HighRetrievalLatency
        expr: histogram_quantile(0.95, retrieval_duration_seconds) > 5
        for: 5m
        annotations:
          summary: "95th percentile retrieval latency > 5s"
      
      - alert: LowEmbeddingCacheHitRate
        expr: embedding_cache_hits / (embedding_cache_hits + embedding_cache_misses) < 0.7
        for: 10m
        annotations:
          summary: "Embedding cache hit rate < 70%"
      
      - alert: QdrantDown
        expr: up{job="qdrant"} == 0
        for: 1m
        annotations:
          summary: "Qdrant instance is down"
      
      - alert: LowAnswerQuality
        expr: avg_over_time(answer_feedback_rating[1h]) < 3
        for: 30m
        annotations:
          summary: "Average answer rating < 3 for 30 minutes"
```

---

## 10. Deployment Considerations

### 10.1 Resource Requirements

**Minimum (small projects, <10k files):**

- CPU: 4 cores
- RAM: 8 GB
- Storage: 20 GB (10 GB for DB, 10 GB for Qdrant)
- Network: Stable internet (if using cloud LLM)

**Recommended (medium projects, 10k-100k files):**

- CPU: 8 cores
- RAM: 16 GB
- Storage: 100 GB SSD
- GPU: Optional (for local embeddings, 10x speedup)

**Large (>100k files):**

- CPU: 16+ cores
- RAM: 32+ GB
- Storage: 500+ GB SSD
- GPU: Recommended (RTX 3060 or better)
- Qdrant: Dedicated server or cloud

### 10.2 Configuration

**application.yml:**

```yaml
rag:
  embeddings:
    provider: openai  # openai | local | custom
    model: text-embedding-3-large
    dimensions: 3072
    batch_size: 100
    cache_enabled: true
    max_cache_size_mb: 4096
  
  vector_search:
    provider: qdrant
    url: http://localhost:6333
    collection: pcm_chunks
    top_k: 50
    hnsw_ef: 128
  
  lexical_search:
    enabled: true
    top_k: 50
    bm25_k1: 1.2
    bm25_b: 0.75
  
  fusion:
    algorithm: rrf  # rrf | weighted | cross_encoder
    rrf_k: 60
    vector_weight: 0.7
    lexical_weight: 0.3
  
  context:
    max_tokens: 4000
    system_prompt_tokens: 500
    question_tokens: 200
    chunk_selection: top_down  # top_down | diversity
  
  llm:
    provider: openai  # openai | anthropic | local
    model: gpt-4o
    temperature: 0.2
    max_tokens: 2000
    stream: true
    timeout_seconds: 30
```

### 10.3 Scaling Strategy

**Horizontal scaling:**

- Run multiple app instances behind load balancer
- Share SQLite DB via NFS (read-only replicas)
- Qdrant cluster with sharding

**Vertical scaling:**

- Increase RAM for larger caches
- Add GPU for faster local embeddings
- SSD for faster I/O

**Caching layers:**

- Redis for hot queries (TTL 1 hour)
- CDN for static assets (models, docs)
- Embedding cache (persistent, no expiry)

---

## 11. Troubleshooting

### 11.1 Common Issues

**Issue: Vector search returns irrelevant results**

Solution:

- Check embedding model consistency (same model for index & query)
- Verify payload filters are correct
- Try different fusion weights (increase lexical weight)
- Add query expansion/rewriting

**Issue: FTS5 search is slow**

Solution:

- Run `INSERT INTO search_index(search_index) VALUES('optimize');`
- Check query complexity (avoid too many wildcards)
- Add project_id filter to limit scope
- Consider external content mode for large datasets

**Issue: Embedding generation is slow**

Solution:

- Enable batching (100-500 chunks per batch)
- Use caching (check cache hit rate)
- Switch to smaller/faster model (text-embedding-3-small)
- Use local model (BGE) with ONNX runtime

**Issue: Out of memory errors**

Solution:

- Reduce chunk size (300 → 200 tokens)
- Lower top_k (50 → 20)
- Enable Qdrant on-disk mode
- Increase JVM heap size (-Xmx)

### 11.2 Debug Queries

**Check embedding cache stats:**

```sql
SELECT 
    model_name,
    COUNT(*) AS cached_embeddings,
    SUM(LENGTH(embedding)) / 1024 / 1024 AS size_mb,
    MIN(created_at) AS oldest,
    MAX(created_at) AS newest
FROM embedding_cache
GROUP BY model_name;
```

**Analyze retrieval patterns:**

```sql
SELECT 
    source_type,
    COUNT(*) AS retrieval_count,
    AVG(CAST(json_extract(metadata, '$.relevance_score') AS REAL)) AS avg_score
FROM request_artifacts
WHERE artifact_type = 'retrieved_chunk'
  AND created_at >= DATE('now', '-7 days')
GROUP BY source_type
ORDER BY retrieval_count DESC;
```

**Find queries with no results:**

```sql
SELECT r.request_id, r.description
FROM user_requests r
LEFT JOIN request_artifacts ra 
    ON r.request_id = ra.request_id 
    AND ra.artifact_type = 'retrieved_chunk'
WHERE ra.artifact_id IS NULL
  AND r.created_at >= DATE('now', '-7 days')
ORDER BY r.created_at DESC;
```

---

## 12. Best Practices

### 12.1 Chunking

✅ DO: Align chunks with code structure (functions, classes)  
✅ DO: Include context (class name, imports) in metadata  
✅ DO: Use overlap to avoid boundary issues  
❌ DON'T: Split mid-sentence or mid-statement  
❌ DON'T: Create chunks > 500 tokens (hurts retrieval quality)

### 12.2 Embeddings

✅ DO: Cache embeddings by content hash  
✅ DO: Use same model for indexing and querying  
✅ DO: Batch requests to minimize API calls  
❌ DON'T: Generate embeddings for binary files  
❌ DON'T: Mix different embedding models in same collection

### 12.3 Search

✅ DO: Use filters (project_id, language) to narrow scope  
✅ DO: Combine vector + lexical for best results  
✅ DO: Monitor and tune fusion weights per use case  
❌ DON'T: Return >100 candidates (diminishing returns)  
❌ DON'T: Skip deduplication (wastes context tokens)

### 12.4 LLM Prompting

✅ DO: Include structured context with citations  
✅ DO: Set clear instructions in system prompt  
✅ DO: Stream responses for better UX  
❌ DON'T: Exceed context window (trim chunks if needed)  
❌ DON'T: Send sensitive data (strip before indexing)

---

## 13. Future Enhancements

### 13.1 Advanced Retrieval

- **Multi-hop reasoning**: Chain multiple retrievals
- **Query expansion**: Use LLM to rephrase/expand queries
- **Dynamic top-k**: Adjust based on query complexity
- **Personalization**: Learn user preferences over time

### 13.2 Advanced Reranking

- **Cross-encoder**: Fine-tuned for code search
- **ColBERT**: Token-level late interaction
- **LLM-as-reranker**: Use GPT-4 for final ranking

### 13.3 Advanced Context

- **Adaptive chunking**: Vary size based on content type
- **Graph-based context**: Include AST relationships
- **Multi-modal**: Support diagrams, screenshots
- **Temporal context**: Prefer recent code/docs

### 13.4 Advanced Monitoring

- **A/B testing**: Compare fusion strategies
- **User feedback loop**: Auto-retrain on ratings
- **Drift detection**: Alert when quality degrades
- **Cost optimization**: Track token usage per user

---

## 14. Appendix

### 14.1 API Reference

**Hybrid Retrieval:**

```java
List<ChunkHit> hybridRetrieve(
    String query,
    int projectId,
    int topKVector,
    int topKLexical
)
```

**Embedding Generation:**

```java
List<float[]> generateEmbeddings(
    List<String> texts,
    String modelName
)
```

**FTS5 Search:**

```java
List<ChunkHit> fts5Search(
    String query,
    int projectId,
    String sourceType,
    int limit
)
```

**Vector Search:**

```java
List<ScoredPoint> vectorSearch(
    float[] queryVector,
    Filter filter,
    int limit
)
```

### 14.2 Performance Benchmarks

**Embedding generation (1000 chunks, avg 200 tokens):**

- OpenAI text-embedding-3-large: ~5s (API latency)
- Local BGE-large (CPU): ~40s
- Local BGE-large (GPU): ~4s

**Search latency (100k vectors, 10k FTS docs):**

- Vector search (Qdrant, top-50): ~50ms
- FTS5 search (SQLite, top-50): ~20ms
- Hybrid retrieval + fusion: ~100ms

**Context assembly:**

- AST enrichment (10 chunks): ~10ms
- Formatting: ~5ms

**Total retrieval pipeline: 100-150ms**

### 14.3 References

- [Qdrant Documentation](https://qdrant.tech/documentation/)
- [SQLite FTS5](https://www.sqlite.org/fts5.html)
- [OpenAI Embeddings](https://platform.openai.com/docs/guides/embeddings)
- [BGE Embeddings](https://huggingface.co/BAAI/bge-large-en-v1.5)
- [Reciprocal Rank Fusion Paper](https://plg.uwaterloo.ca/~gvcormac/cormacksigir09-rrf.pdf)

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-15  
**Maintainer:** PCM Desktop Team

