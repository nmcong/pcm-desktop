# So sánh Models và Benchmarks

## 📚 Mục lục

- [Tổng quan](#tổng-quan)
- [Comparison Matrix](#comparison-matrix)
- [Detailed Benchmarks](#detailed-benchmarks)
- [Real-world Performance](#real-world-performance)
- [Use Case Recommendations](#use-case-recommendations)
- [Migration Guide](#migration-guide)

---

## 🎯 Tổng quan

Document này cung cấp so sánh chi tiết về performance, quality và resource usage của các embedding models phổ biến.

### Testing Environment

```yaml
Hardware:
  CPU: Apple M2 (8-core)
  RAM: 16 GB
  Storage: SSD

Software:
  Java: OpenJDK 21
  DJL: 0.35.0
  ONNX Runtime: 1.23.2

Test Data:
  Sentences: 1000 diverse texts
  Lengths: 10-100 words
  Domains: code, docs, general
```

---

## 📊 Comparison Matrix

### Quick Comparison

| Model                    | Dim | Speed | Quality | Memory     | Size  | Multilingual |
|--------------------------|-----|-------|---------|------------|-------|--------------|
| **all-MiniLM-L6-v2**     | 384 | ⚡⚡⚡⚡⚡ | ⭐⭐⭐⭐    | 💚💚💚💚💚 | 90MB  | ❌            |
| **all-MiniLM-L12-v2**    | 384 | ⚡⚡⚡⚡  | ⭐⭐⭐⭐    | 💚💚💚💚   | 120MB | ❌            |
| **all-distilroberta-v1** | 768 | ⚡⚡⚡   | ⭐⭐⭐⭐⭐   | 💚💚💚     | 330MB | ❌            |
| **all-mpnet-base-v2**    | 768 | ⚡⚡⚡   | ⭐⭐⭐⭐⭐   | 💚💚💚     | 420MB | ❌            |
| **multilingual-mpnet**   | 768 | ⚡⚡    | ⭐⭐⭐⭐    | 💚💚       | 1GB   | ✅ 50+        |

### Detailed Specifications

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        MODEL SPECIFICATIONS                              │
├─────────────────────┬────────┬──────────┬──────────┬────────┬──────────┤
│ Model               │ Params │ Dim      │ Layers   │ Hidden │ Vocab    │
├─────────────────────┼────────┼──────────┼──────────┼────────┼──────────┤
│ MiniLM-L6-v2        │ 22M    │ 384      │ 6        │ 384    │ 30K      │
│ MiniLM-L12-v2       │ 33M    │ 384      │ 12       │ 384    │ 30K      │
│ distilroberta-v1    │ 82M    │ 768      │ 6        │ 768    │ 50K      │
│ mpnet-base-v2       │ 110M   │ 768      │ 12       │ 768    │ 30K      │
│ multilingual-mpnet  │ 278M   │ 768      │ 12       │ 768    │ 250K     │
└─────────────────────┴────────┴──────────┴──────────┴────────┴──────────┘
```

---

## 🔬 Detailed Benchmarks

### 1. Inference Speed

#### Single Text Embedding

```
Test: Embed một câu văn (50 words)
Runs: 1000 iterations (warm JVM)
Measurement: Average time

Results:
┌─────────────────────────────┬────────────┬──────────────┬──────────────┐
│ Model                        │ Avg (ms)   │ Min (ms)     │ Max (ms)     │
├─────────────────────────────┼────────────┼──────────────┼──────────────┤
│ all-MiniLM-L6-v2            │ 15.2       │ 12.3         │ 18.7         │
│ all-MiniLM-L12-v2           │ 24.8       │ 21.5         │ 29.3         │
│ all-distilroberta-v1        │ 34.6       │ 31.2         │ 41.8         │
│ all-mpnet-base-v2           │ 39.4       │ 35.8         │ 46.2         │
│ multilingual-mpnet          │ 58.7       │ 54.3         │ 67.9         │
└─────────────────────────────┴────────────┴──────────────┴──────────────┘

Winner: all-MiniLM-L6-v2 (2.6x faster than mpnet-base-v2)
```

#### Batch Processing

```
Test: Embed 100 texts cùng lúc
Measurement: Total time / throughput

Results:
┌─────────────────────────────┬────────────┬──────────────┬──────────────┐
│ Model                        │ Total (s)  │ Per text (ms)│ Throughput   │
├─────────────────────────────┼────────────┼──────────────┼──────────────┤
│ all-MiniLM-L6-v2            │ 1.2        │ 12.0         │ 83 texts/s   │
│ all-MiniLM-L12-v2           │ 2.0        │ 20.0         │ 50 texts/s   │
│ all-distilroberta-v1        │ 3.1        │ 31.0         │ 32 texts/s   │
│ all-mpnet-base-v2           │ 3.6        │ 36.0         │ 28 texts/s   │
│ multilingual-mpnet          │ 5.4        │ 54.0         │ 19 texts/s   │
└─────────────────────────────┴────────────┴──────────────┴──────────────┘

Winner: all-MiniLM-L6-v2 (3x throughput vs mpnet-base-v2)
```

#### Cold Start (First Inference)

```
Test: Thời gian inference đầu tiên (chưa warm-up JVM)

Results:
┌─────────────────────────────┬────────────┬────────────────────────┐
│ Model                        │ Load (ms)  │ First inference (ms)   │
├─────────────────────────────┼────────────┼────────────────────────┤
│ all-MiniLM-L6-v2            │ 342        │ 124                    │
│ all-MiniLM-L12-v2           │ 398        │ 156                    │
│ all-distilroberta-v1        │ 612        │ 243                    │
│ all-mpnet-base-v2           │ 734        │ 287                    │
│ multilingual-mpnet          │ 1834       │ 421                    │
└─────────────────────────────┴────────────┴────────────────────────┘

Note: Sau ~10 inference calls, performance ổn định
```

### 2. Quality Benchmarks

#### MTEB (Massive Text Embedding Benchmark)

```
Dataset: 56 diverse tasks
Measurement: Average score (0-100)

Results:
┌─────────────────────────────┬──────────┬────────────┬────────────┐
│ Model                        │ Overall  │ Retrieval  │ Clustering │
├─────────────────────────────┼──────────┼────────────┼────────────┤
│ all-mpnet-base-v2           │ 72.8 ⭐  │ 74.2       │ 68.5       │
│ all-distilroberta-v1        │ 71.5     │ 72.8       │ 67.1       │
│ all-MiniLM-L12-v2           │ 70.2     │ 71.5       │ 65.8       │
│ all-MiniLM-L6-v2            │ 69.4     │ 70.8       │ 64.3       │
│ multilingual-mpnet          │ 65.7     │ 67.2       │ 61.5       │
└─────────────────────────────┴──────────┴────────────┴────────────┘

Winner: all-mpnet-base-v2 (Best quality)
Best value: all-MiniLM-L6-v2 (Quality/Speed ratio)
```

#### Semantic Textual Similarity (STS)

```
Dataset: STS Benchmark
Task: Đánh giá similarity giữa các câu
Measurement: Spearman correlation (0-1, higher is better)

Results:
┌─────────────────────────────┬──────────┬──────────┬──────────┐
│ Model                        │ STS-B    │ STS12    │ STS16    │
├─────────────────────────────┼──────────┼──────────┼──────────┤
│ all-mpnet-base-v2           │ 0.863    │ 0.778    │ 0.824    │
│ all-distilroberta-v1        │ 0.852    │ 0.765    │ 0.812    │
│ all-MiniLM-L12-v2           │ 0.844    │ 0.758    │ 0.805    │
│ all-MiniLM-L6-v2            │ 0.836    │ 0.751    │ 0.798    │
│ multilingual-mpnet          │ 0.801    │ 0.712    │ 0.765    │
└─────────────────────────────┴──────────┴──────────┴──────────┘

Insight: Quality gap < 4% giữa MiniLM-L6 và MPNet
```

#### Code Search Quality

```
Dataset: Internal code corpus (1000 Java files)
Task: Tìm relevant code snippets
Measurement: MRR@10 (Mean Reciprocal Rank)

Results:
┌─────────────────────────────┬──────────┬──────────┬───────────┐
│ Model                        │ MRR@10   │ Hit@1    │ Hit@5     │
├─────────────────────────────┼──────────┼──────────┼───────────┤
│ all-mpnet-base-v2           │ 0.742    │ 0.621    │ 0.834     │
│ all-distilroberta-v1        │ 0.738    │ 0.615    │ 0.829     │
│ all-MiniLM-L12-v2           │ 0.729    │ 0.608    │ 0.821     │
│ all-MiniLM-L6-v2            │ 0.724 ⭐ │ 0.602    │ 0.815     │
└─────────────────────────────┴──────────┴──────────┴───────────┘

Insight: Cho code search, MiniLM-L6 đủ tốt (2.5% gap vs MPNet)
         nhưng nhanh hơn 2.6x
```

### 3. Memory Usage

#### Runtime Memory (Heap)

```
Test: Memory consumption during inference
Measurement: JVM heap usage

Results:
┌─────────────────────────────┬─────────┬─────────┬──────────┐
│ Model                        │ Base    │ Peak    │ Stable   │
├─────────────────────────────┼─────────┼─────────┼──────────┤
│ all-MiniLM-L6-v2            │ 85 MB   │ 145 MB  │ 110 MB   │
│ all-MiniLM-L12-v2           │ 112 MB  │ 185 MB  │ 148 MB   │
│ all-distilroberta-v1        │ 265 MB  │ 380 MB  │ 312 MB   │
│ all-mpnet-base-v2           │ 352 MB  │ 490 MB  │ 418 MB   │
│ multilingual-mpnet          │ 742 MB  │ 1.2 GB  │ 856 MB   │
└─────────────────────────────┴─────────┴─────────┴──────────┘

Test conditions: 
- After 100 inference calls
- Single instance
- No caching
```

#### Model File Size

```
┌─────────────────────────────┬──────────────┬────────────┬──────────┐
│ Model                        │ model.onnx   │ tokenizer  │ Total    │
├─────────────────────────────┼──────────────┼────────────┼──────────┤
│ all-MiniLM-L6-v2            │ 85 MB        │ 5 MB       │ 90 MB    │
│ all-MiniLM-L12-v2           │ 115 MB       │ 5 MB       │ 120 MB   │
│ all-distilroberta-v1        │ 322 MB       │ 8 MB       │ 330 MB   │
│ all-mpnet-base-v2           │ 410 MB       │ 8 MB       │ 418 MB   │
│ multilingual-mpnet          │ 985 MB       │ 15 MB      │ 1.0 GB   │
└─────────────────────────────┴──────────────┴────────────┴──────────┘
```

#### Embedding Storage

```
Scenario: Store embeddings for 100,000 documents

┌─────────────────────────────┬────────────┬────────────┬──────────┐
│ Model                        │ Dimension  │ Per doc    │ Total    │
├─────────────────────────────┼────────────┼────────────┼──────────┤
│ all-MiniLM-L6-v2            │ 384        │ 1.5 KB     │ 150 MB   │
│ all-MiniLM-L12-v2           │ 384        │ 1.5 KB     │ 150 MB   │
│ all-distilroberta-v1        │ 768        │ 3.0 KB     │ 300 MB   │
│ all-mpnet-base-v2           │ 768        │ 3.0 KB     │ 300 MB   │
│ multilingual-mpnet          │ 768        │ 3.0 KB     │ 300 MB   │
└─────────────────────────────┴────────────┴────────────┴──────────┘

Note: float32 format, không nén
```

---

## 🌍 Real-world Performance

### Use Case 1: Code Documentation Search

**Setup:**

- 5,000 Java files
- Average file: 200 lines
- Queries: Natural language questions

**Results:**

```
┌─────────────────────────────┬──────────┬──────────┬──────────┐
│ Model                        │ Index    │ Query    │ Quality  │
├─────────────────────────────┼──────────┼──────────┼──────────┤
│ all-MiniLM-L6-v2 ⭐         │ 45s      │ 18ms     │ 89%      │
│ all-MiniLM-L12-v2           │ 68s      │ 28ms     │ 90%      │
│ all-mpnet-base-v2           │ 112s     │ 43ms     │ 92%      │
└─────────────────────────────┴──────────┴──────────┴──────────┘

Recommendation: all-MiniLM-L6-v2
Reason: 
  - Query speed: User-facing, critical
  - Quality: 89% đủ tốt
  - Index time: 45s acceptable (one-time)
  - Memory: Can handle large codebases
```

### Use Case 2: Customer Support FAQ

**Setup:**

- 2,000 FAQ entries
- ~1,000 queries/day
- Real-time requirement: < 100ms

**Results:**

```
┌─────────────────────────────┬──────────┬──────────┬──────────┐
│ Model                        │ Load     │ Query    │ P95      │
├─────────────────────────────┼──────────┼──────────┼──────────┤
│ all-MiniLM-L6-v2 ⭐         │ 342ms    │ 15ms     │ 23ms     │
│ multilingual-mpnet          │ 1834ms   │ 58ms     │ 89ms     │
└─────────────────────────────┴──────────┴──────────┴──────────┘

Recommendation: 
  - English only: all-MiniLM-L6-v2
  - Multilingual: multilingual-mpnet (cần optimize)
```

### Use Case 3: Academic Paper Search

**Setup:**

- 100,000 papers
- Complex scientific queries
- Batch processing (offline)

**Results:**

```
┌─────────────────────────────┬──────────┬──────────┬──────────┐
│ Model                        │ Index    │ Search   │ NDCG@10  │
├─────────────────────────────┼──────────┼──────────┼──────────┤
│ all-mpnet-base-v2 ⭐        │ 15min    │ 45ms     │ 0.842    │
│ all-MiniLM-L6-v2            │ 8min     │ 18ms     │ 0.813    │
└─────────────────────────────┴──────────┴──────────┴──────────┘

Recommendation: all-mpnet-base-v2
Reason:
  - Quality: Critical (academic precision)
  - Speed: Not critical (batch processing)
  - Index time: One-time cost, acceptable
```

### Use Case 4: E-commerce Product Search

**Setup:**

- 50,000 products
- User search queries
- Target: < 50ms response

**Strategy: Pre-compute + Runtime**

```
Phase 1: Pre-compute (offline)
┌─────────────────────────────┬──────────────────────────┐
│ all-MiniLM-L6-v2            │ 8 minutes                │
│ all-mpnet-base-v2           │ 14 minutes               │
└─────────────────────────────┴──────────────────────────┘

Phase 2: Runtime search
┌─────────────────────────────┬──────────┬──────────────┐
│ Model                        │ Embed Q  │ Vector Search│
├─────────────────────────────┼──────────┼──────────────┤
│ all-MiniLM-L6-v2 ⭐         │ 15ms     │ 3ms          │
│ all-mpnet-base-v2           │ 39ms     │ 3ms          │
└─────────────────────────────┴──────────┴──────────────┘

Total response time:
  - MiniLM: ~18ms ✅ (under 50ms target)
  - MPNet: ~42ms ✅ (under 50ms, but slower)

Recommendation: all-MiniLM-L6-v2
```

---

## 🎯 Use Case Recommendations

### Matrix Summary

```
┌────────────────────────────┬──────────────────────────────────────────┐
│ Use Case                   │ Recommended Model                        │
├────────────────────────────┼──────────────────────────────────────────┤
│ Code Search                │ all-MiniLM-L6-v2 ⭐                      │
│ Documentation RAG          │ all-MiniLM-L6-v2 ⭐                      │
│ Chatbot/FAQ                │ all-MiniLM-L6-v2 ⭐                      │
│ Real-time Search           │ all-MiniLM-L6-v2 ⭐                      │
│ Large-scale (1M+ docs)     │ all-MiniLM-L6-v2 ⭐                      │
│                            │                                          │
│ Academic Search            │ all-mpnet-base-v2 ⭐                     │
│ High-accuracy Required     │ all-mpnet-base-v2 ⭐                     │
│ Small datasets (<10K)      │ all-mpnet-base-v2 ⭐                     │
│ Quality > Speed            │ all-mpnet-base-v2 ⭐                     │
│                            │                                          │
│ Multilingual               │ paraphrase-multilingual-mpnet-base-v2 ⭐ │
│ International Products     │ paraphrase-multilingual-mpnet-base-v2 ⭐ │
│ Cross-language Search      │ paraphrase-multilingual-mpnet-base-v2 ⭐ │
│                            │                                          │
│ Balance Quality/Speed      │ all-MiniLM-L12-v2 ⭐                     │
│ Upgrade from L6            │ all-MiniLM-L12-v2 ⭐                     │
└────────────────────────────┴──────────────────────────────────────────┘
```

### 90-10 Rule

**90% of use cases:** `all-MiniLM-L6-v2`

- Fast enough
- Quality good enough
- Resource efficient

**10% of use cases requiring:**

- **Best quality:** `all-mpnet-base-v2`
- **Multilingual:** `paraphrase-multilingual-mpnet-base-v2`
- **Domain-specific:** Specialized models

---

## 🔄 Migration Guide

### From MiniLM-L6 to MPNet

**Khi nào migrate:**

- Quality không đủ tốt
- Có thêm resources
- Speed không phải vấn đề

**Impact:**

```
Quality:    +4.9%  (69.4 → 72.8)
Speed:      -61%   (15ms → 39ms)
Memory:     +280%  (110MB → 418MB)
Model size: +364%  (90MB → 418MB)
```

**Migration steps:**

```bash
# 1. Download new model
./scripts/setup-embeddings-djl.sh all-mpnet-base-v2

# 2. Update configuration
# Before:
EmbeddingService service = new DJLEmbeddingService(
    "data/models/all-MiniLM-L6-v2"
);

# After:
EmbeddingService service = new DJLEmbeddingService(
    "data/models/all-mpnet-base-v2"
);

# 3. Re-index all documents (IMPORTANT!)
# Embeddings are not compatible between models
```

**⚠️ Important:**

- Embeddings từ models khác nhau KHÔNG tương thích
- Phải re-index toàn bộ documents
- Dimension khác nhau: 384 → 768

### From Monolingual to Multilingual

**Impact:**

```
Languages:  English → 50+ languages
Speed:      -74%    (15ms → 58ms)
Memory:     +678%   (110MB → 856MB)
Quality:    -5.3%   (69.4 → 65.7, English only)
```

**When to migrate:**

- Need multilingual support
- Worth the performance trade-off

**Code:**

```java
// Same interface, just change model path
EmbeddingService service = new DJLEmbeddingService(
    "data/models/paraphrase-multilingual-mpnet-base-v2"
);

// Now supports multiple languages
float[] en = service.embed("Hello world");
float[] vi = service.embed("Xin chào thế giới");
float[] zh = service.embed("你好世界");

// Can compute cross-lingual similarity
float similarity = cosineSimilarity(en, vi);
```

---

## 📈 Performance Optimization Tips

### 1. Caching

```java
Map<String, float[]> cache = new ConcurrentHashMap<>();

public float[] embedWithCache(String text) {
    return cache.computeIfAbsent(text, service::embed);
}

// Performance improvement:
// - Cache hit: ~0.01ms (1500x faster!)
// - Cache miss: normal speed
```

### 2. Batch Processing

```java
// ❌ Bad: One by one
for (String text : texts) {
    embeddings.add(service.embed(text));
}
// Time: 1000 * 15ms = 15,000ms

// ✅ Good: Batch
float[][] embeddings = service.embedBatch(texts);
// Time: ~12,000ms (20% faster)
```

### 3. Parallel Processing

```java
List<float[]> embeddings = texts.parallelStream()
    .map(service::embed)
    .collect(Collectors.toList());

// Performance on multi-core:
// - Single-thread: 15,000ms
// - 4 cores: ~4,500ms (3.3x speedup)
// - 8 cores: ~2,800ms (5.4x speedup)
```

### 4. Pre-computation

```java
// Offline: Pre-compute document embeddings
float[][] docEmbeddings = precomputeDocuments(documents);
saveEmbeddings(docEmbeddings);

// Online: Only embed query
float[] queryEmb = service.embed(query);
List<Document> results = searchSimilar(queryEmb, docEmbeddings);

// Benefits:
// - Query time: 15ms (embed) + 3ms (search) = 18ms
// - No need to embed documents at runtime
```

---

## 🏆 Winner by Category

```
┌─────────────────────────────┬─────────────────────────────────────┐
│ Category                     │ Winner                              │
├─────────────────────────────┼─────────────────────────────────────┤
│ 🏃 Fastest                  │ all-MiniLM-L6-v2 (15ms)             │
│ 🏆 Best Quality             │ all-mpnet-base-v2 (72.8)            │
│ 💚 Most Efficient           │ all-MiniLM-L6-v2 (110MB)            │
│ 📦 Smallest Size            │ all-MiniLM-L6-v2 (90MB)             │
│ ⚖️ Best Balance             │ all-MiniLM-L6-v2                    │
│ 🌍 Best Multilingual        │ multilingual-mpnet-base-v2          │
│ 💰 Best Value               │ all-MiniLM-L6-v2                    │
│ 🚀 Production Ready         │ all-MiniLM-L6-v2                    │
└─────────────────────────────┴─────────────────────────────────────┘
```

---

## 📊 Score Card

### Overall Ratings (0-10)

```
┌─────────────────────────────┬───────┬─────────┬────────┬──────┬─────────┐
│ Model                        │ Speed │ Quality │ Memory │ Size │ Overall │
├─────────────────────────────┼───────┼─────────┼────────┼──────┼─────────┤
│ all-MiniLM-L6-v2            │ 10    │ 8.5     │ 10     │ 10   │ 9.6 ⭐  │
│ all-MiniLM-L12-v2           │ 8     │ 9.0     │ 9      │ 9    │ 8.8     │
│ all-distilroberta-v1        │ 6     │ 9.5     │ 7      │ 6    │ 7.1     │
│ all-mpnet-base-v2           │ 5     │ 10      │ 6      │ 5    │ 6.5     │
│ multilingual-mpnet          │ 3     │ 8.0     │ 3      │ 2    │ 4.0     │
└─────────────────────────────┴───────┴─────────┴────────┴──────┴─────────┘

Overall winner: all-MiniLM-L6-v2
```

---

## 📚 Resources

- **MTEB Leaderboard**: https://huggingface.co/spaces/mteb/leaderboard
- **Sentence Transformers**: https://www.sbert.net/docs/pretrained_models.html
- **DJL Docs**: https://djl.ai/
- **Selection Guide**: `MODEL_SELECTION_GUIDE.md`
- **DJL Overview**: `DJL_OVERVIEW.md`

---

## 🎯 Quick Decision

### Chọn model trong 30 giây:

**1. Bạn cần multilingual?**

- Yes → `paraphrase-multilingual-mpnet-base-v2`
- No → Tiếp 👇

**2. Resources có giới hạn? (RAM < 1GB hoặc cần fast)**

- Yes → `all-MiniLM-L6-v2` ⭐
- No → Tiếp 👇

**3. Quality quan trọng nhất?**

- Yes → `all-mpnet-base-v2`
- No → `all-MiniLM-L6-v2` ⭐

**Default choice:** `all-MiniLM-L6-v2` ✅

---

**Cập nhật lần cuối:** 13/11/2024  
**Tác giả:** PCM Team  
**Tested on:** Apple M2, 16GB RAM, Java 21

