# Hướng dẫn Chọn Model Embedding

## 📚 Mục lục
- [Giới thiệu](#giới-thiệu)
- [Ma trận Lựa chọn](#ma-trận-lựa-chọn)
- [Models phổ biến](#models-phổ-biến)
- [Use Cases](#use-cases)
- [Decision Tree](#decision-tree)
- [Setup Guide](#setup-guide)

---

## 🎯 Giới thiệu

Việc chọn model embedding phù hợp phụ thuộc vào nhiều yếu tố:

### Các yếu tố cần xem xét

| Yếu tố | Câu hỏi cần trả lời |
|--------|---------------------|
| **Performance** | Tốc độ inference quan trọng như thế nào? |
| **Quality** | Cần độ chính xác cao đến mức nào? |
| **Memory** | Có giới hạn về RAM không? |
| **Domain** | Domain/ngôn ngữ chuyên biệt không? |
| **Size** | Có giới hạn về kích thước deployment? |
| **Multilingual** | Cần hỗ trợ nhiều ngôn ngữ không? |

### Trade-offs chính

```
Quality ←→ Speed
Quality ←→ Size
Quality ←→ Memory
```

**Quy tắc chung:**
- Model lớn hơn → Chất lượng tốt hơn, chậm hơn
- Model nhỏ hơn → Nhanh hơn, chất lượng thấp hơn
- Multilingual models → Chậm hơn, cần nhiều memory hơn

---

## 📊 Ma trận Lựa chọn

### Theo Use Case

| Use Case | Model khuyến nghị | Lý do |
|----------|-------------------|-------|
| **Code search/RAG** | all-MiniLM-L6-v2 | ✅ Cân bằng speed/quality<br>✅ Kích thước nhỏ<br>✅ Nhanh |
| **Semantic search (General)** | all-mpnet-base-v2 | ✅ Quality tốt nhất<br>⚠️ Chậm hơn 1.5x |
| **Multilingual** | paraphrase-multilingual-mpnet-base-v2 | ✅ Hỗ trợ 50+ ngôn ngữ<br>⚠️ Cần nhiều memory |
| **Real-time chat** | all-MiniLM-L6-v2 | ✅ Inference < 20ms<br>✅ Lightweight |
| **High accuracy** | all-mpnet-base-v2 | ✅ State-of-the-art quality |
| **Large datasets** | all-MiniLM-L6-v2 | ✅ Fast batch processing<br>✅ Low memory |
| **Specialized (Legal, Medical)** | Legal-BERT, BioBERT | ✅ Domain-specific |

### Theo Constraints

#### Ưu tiên Tốc độ

```
1. all-MiniLM-L6-v2       (~15-20ms)  ⭐ Khuyến nghị
2. all-MiniLM-L12-v2      (~25-30ms)
3. paraphrase-MiniLM-L6-v2 (~20ms)
```

#### Ưu tiên Chất lượng

```
1. all-mpnet-base-v2      (Best)      ⭐ Khuyến nghị
2. all-roberta-large-v1   (Excellent, slow)
3. all-MiniLM-L12-v2      (Good)
```

#### Ưu tiên Memory

```
1. all-MiniLM-L6-v2       (~100 MB)   ⭐ Khuyến nghị
2. all-MiniLM-L12-v2      (~150 MB)
3. all-mpnet-base-v2      (~400 MB)
```

#### Multilingual

```
1. paraphrase-multilingual-mpnet-base-v2  (50+ langs) ⭐ Khuyến nghị
2. paraphrase-multilingual-MiniLM-L12-v2  (Faster)
3. distiluse-base-multilingual-cased-v2   (Smaller)
```

---

## 🔍 Models phổ biến

### 1. all-MiniLM-L6-v2 ⭐ **KHUYẾN NGHỊ MẶC ĐỊNH**

**Tổng quan:**
- Model nhỏ, nhanh, chất lượng tốt
- "Sweet spot" cho hầu hết use cases

**Thông số:**
```yaml
Dimension: 384
Parameters: 22M
Model size: ~90 MB
Inference: ~15-20ms
Quality score: 69.4/100
```

**Ưu điểm:**
- ✅ Cực kỳ nhanh
- ✅ Kích thước nhỏ
- ✅ Chất lượng tốt cho general use cases
- ✅ Production-ready
- ✅ Ít tốn memory

**Nhược điểm:**
- ⚠️ Quality không bằng MPNet
- ⚠️ Chỉ tiếng Anh

**Khi nào dùng:**
- ✅ Code search & RAG systems
- ✅ Real-time applications
- ✅ Large-scale deployments
- ✅ Resource-constrained environments
- ✅ Fast prototyping

**Setup:**
```bash
./scripts/setup-embeddings-djl.sh all-MiniLM-L6-v2
```

**Code:**
```java
EmbeddingService service = new DJLEmbeddingService(
    "data/models/all-MiniLM-L6-v2"
);
```

---

### 2. all-mpnet-base-v2 ⭐ **CHẤT LƯỢNG CAO NHẤT**

**Tổng quan:**
- Model chất lượng cao nhất cho tiếng Anh
- Dựa trên MPNet architecture

**Thông số:**
```yaml
Dimension: 768
Parameters: 110M
Model size: ~420 MB
Inference: ~30-40ms
Quality score: 72.8/100 (Highest!)
```

**Ưu điểm:**
- ✅ Chất lượng tốt nhất
- ✅ State-of-the-art performance
- ✅ Tốt cho semantic understanding
- ✅ Robust với diverse queries

**Nhược điểm:**
- ⚠️ Chậm hơn 2x so với MiniLM
- ⚠️ Cần nhiều memory hơn
- ⚠️ Model size lớn (~420 MB)

**Khi nào dùng:**
- ✅ High-accuracy applications
- ✅ Semantic search (không realtime)
- ✅ Quality > Speed
- ✅ Sufficient resources

**Không dùng khi:**
- ❌ Real-time requirements
- ❌ Resource constraints
- ❌ Mobile/edge deployment

**Setup:**
```bash
./scripts/setup-embeddings-djl.sh all-mpnet-base-v2
```

**Code:**
```java
EmbeddingService service = new DJLEmbeddingService(
    "data/models/all-mpnet-base-v2"
);
```

---

### 3. paraphrase-multilingual-mpnet-base-v2 ⭐ **CHO ĐA NGÔN NGỮ**

**Tổng quan:**
- Hỗ trợ 50+ ngôn ngữ
- Cross-lingual semantic search

**Thông số:**
```yaml
Dimension: 768
Parameters: 278M
Model size: ~1 GB
Inference: ~50ms
Languages: 50+
Quality score: 65.7/100
```

**Ngôn ngữ hỗ trợ:**
```
✅ English, Tiếng Việt, Chinese, Japanese, Korean
✅ French, German, Spanish, Italian, Portuguese
✅ Arabic, Hindi, Russian, Turkish, Thai
... và 35+ ngôn ngữ khác
```

**Ưu điểm:**
- ✅ Multilingual support
- ✅ Cross-lingual search
- ✅ Unified embedding space
- ✅ Good quality across languages

**Nhược điểm:**
- ⚠️ Rất chậm
- ⚠️ Cần nhiều memory (~1.5 GB RAM)
- ⚠️ Model size lớn
- ⚠️ Quality thấp hơn monolingual

**Khi nào dùng:**
- ✅ Multilingual applications
- ✅ Cross-language search
- ✅ International products
- ✅ Mixed-language content

**Setup:**
```bash
./scripts/setup-embeddings-djl.sh paraphrase-multilingual-mpnet-base-v2
```

---

### 4. all-MiniLM-L12-v2

**Tổng quan:**
- Version lớn hơn của MiniLM-L6
- Middle ground giữa speed và quality

**Thông số:**
```yaml
Dimension: 384
Parameters: 33M
Model size: ~120 MB
Inference: ~25ms
Quality score: 70.2/100
```

**So sánh với L6:**
- Quality: +1% better
- Speed: 25% slower
- Size: 30% larger

**Khi nào dùng:**
- ✅ Cần quality tốt hơn L6 một chút
- ✅ Vẫn giữ được tốc độ tốt
- ✅ Có resources đủ

**Không cần dùng nếu:**
- ❌ L6 đã đủ tốt (hầu hết cases)
- ❌ Cần optimize memory

---

### 5. all-distilroberta-v1

**Tổng quan:**
- Distilled version của RoBERTa
- Cân bằng giữa RoBERTa-large và MiniLM

**Thông số:**
```yaml
Dimension: 768
Parameters: 82M
Model size: ~330 MB
Inference: ~35ms
Quality score: 71.5/100
```

**Khi nào dùng:**
- ✅ Cần quality giữa MiniLM và MPNet
- ✅ Code understanding tốt
- ✅ Technical content

---

## 🎯 Use Cases Chi tiết

### Use Case 1: Code Search & Documentation RAG

**Yêu cầu:**
- Fast retrieval (< 50ms)
- Good semantic understanding
- Code + natural language
- Large codebase (1000s files)

**Model khuyến nghị:** `all-MiniLM-L6-v2`

**Lý do:**
```
✅ Speed: ~15ms inference
✅ Quality: Đủ tốt cho code search
✅ Memory: Có thể cache nhiều embeddings
✅ Production-proven
```

**Alternative:** `all-MiniLM-L12-v2` (nếu cần quality tốt hơn)

---

### Use Case 2: Customer Support Chatbot

**Yêu cầu:**
- Real-time response
- Semantic matching với FAQ
- ~1000 FAQ entries
- Multilingual (optional)

**Model khuyến nghị:**

**Monolingual (English/Vietnamese):** `all-MiniLM-L6-v2`
```
✅ Real-time: < 20ms
✅ User experience: Instant
✅ Cache: Có thể cache tất cả FAQ
```

**Multilingual:** `paraphrase-multilingual-MiniLM-L12-v2`
```
✅ 50+ languages
⚠️ Slower (~40ms) - vẫn acceptable
```

---

### Use Case 3: Academic Paper Search

**Yêu cầu:**
- High accuracy
- Scientific/technical content
- Complex queries
- Batch processing (offline)

**Model khuyến nghị:** `all-mpnet-base-v2`

**Lý do:**
```
✅ Best quality: 72.8/100
✅ Scientific understanding
✅ Batch processing: Speed không quan trọng
✅ Worth the trade-off
```

---

### Use Case 4: E-commerce Product Search

**Yêu cầu:**
- Fast search (< 100ms total)
- Product descriptions
- User queries
- 10,000+ products

**Model khuyến nghị:** `all-MiniLM-L6-v2` + caching

**Strategy:**
```java
// Pre-compute tất cả product embeddings
float[][] productEmbeddings = precomputeEmbeddings(products);

// Runtime: Chỉ embed user query
float[] queryEmb = service.embed(userQuery);

// Search: Vector similarity (cực nhanh)
List<Product> results = searchSimilar(queryEmb, productEmbeddings);
```

**Performance:**
- Pre-compute: 1 lần (offline)
- Query embed: ~15ms
- Vector search: ~5ms
- **Total: ~20ms** ✅

---

### Use Case 5: Legal Document Analysis

**Yêu cầu:**
- Legal domain
- High accuracy
- Complex legal language
- Batch processing

**Model khuyến nghị:** Domain-specific model

**Options:**
1. **Legal-BERT** (specialized)
2. **all-mpnet-base-v2** (general but good)
3. Fine-tune MiniLM trên legal corpus

**Setup Legal-BERT:**
```bash
# Download từ HuggingFace
./scripts/setup-embeddings-djl.sh nlpaueb/legal-bert-base-uncased
```

---

## 🌳 Decision Tree

```
Bắt đầu
   │
   ├─ Cần multilingual?
   │  ├─ Yes → paraphrase-multilingual-mpnet-base-v2
   │  └─ No ↓
   │
   ├─ Ưu tiên gì?
   │  ├─ Quality > All
   │  │  └─ all-mpnet-base-v2 ⭐
   │  │
   │  ├─ Speed > All
   │  │  └─ all-MiniLM-L6-v2 ⭐
   │  │
   │  └─ Cân bằng
   │     ├─ General use → all-MiniLM-L6-v2 ⭐
   │     └─ Need better quality → all-MiniLM-L12-v2
   │
   └─ Domain đặc biệt?
      ├─ Code → all-MiniLM-L6-v2
      ├─ Legal → Legal-BERT
      ├─ Medical → BioBERT
      └─ Scientific → SciBERT hoặc all-mpnet-base-v2
```

---

## 🚀 Setup Guide

### Quick Setup

```bash
# 1. Chọn model
MODEL="all-MiniLM-L6-v2"  # hoặc model khác

# 2. Download
./scripts/setup-embeddings-djl.sh $MODEL

# 3. Build
./scripts/build.sh

# 4. Test
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*" \
  com.noteflix.pcm.rag.examples.embedding.DJLEmbeddingExample
```

### Multiple Models

```bash
# Download nhiều models
./scripts/setup-embeddings-djl.sh all-MiniLM-L6-v2
./scripts/setup-embeddings-djl.sh all-mpnet-base-v2
./scripts/setup-embeddings-djl.sh paraphrase-multilingual-mpnet-base-v2

# Cấu trúc thư mục
data/models/
├── all-MiniLM-L6-v2/
│   ├── model.onnx
│   ├── tokenizer.json
│   └── config.json
├── all-mpnet-base-v2/
│   └── ...
└── paraphrase-multilingual-mpnet-base-v2/
    └── ...
```

### Switching Models

```java
// Configuration
public class EmbeddingConfig {
    // Development: Fast
    public static final String DEV_MODEL = "all-MiniLM-L6-v2";
    
    // Production: Quality
    public static final String PROD_MODEL = "all-mpnet-base-v2";
}

// Usage
String model = isProduction() 
    ? EmbeddingConfig.PROD_MODEL 
    : EmbeddingConfig.DEV_MODEL;
    
EmbeddingService service = new DJLEmbeddingService(
    "data/models/" + model
);
```

---

## 📊 Performance Comparison

### Inference Time (Single text)

```
all-MiniLM-L6-v2:              ████░░░░░░ 15ms  ⭐ Nhanh nhất
all-MiniLM-L12-v2:             ██████░░░░ 25ms
all-distilroberta-v1:          ████████░░ 35ms
all-mpnet-base-v2:             ██████████ 40ms
multilingual-mpnet-base-v2:    ███████████████ 60ms
```

### Quality (MTEB Score)

```
all-mpnet-base-v2:             ██████████ 72.8  ⭐ Tốt nhất
all-distilroberta-v1:          █████████░ 71.5
all-MiniLM-L12-v2:             █████████░ 70.2
all-MiniLM-L6-v2:              ████████░░ 69.4
multilingual-mpnet-base-v2:    ███████░░░ 65.7
```

### Memory Usage (Runtime)

```
all-MiniLM-L6-v2:              ██░░░░░░░░ 100 MB  ⭐ Nhỏ nhất
all-MiniLM-L12-v2:             ███░░░░░░░ 150 MB
all-distilroberta-v1:          ██████░░░░ 300 MB
all-mpnet-base-v2:             ████████░░ 400 MB
multilingual-mpnet-base-v2:    ███████████████ 800 MB
```

### Model Size (Disk)

```
all-MiniLM-L6-v2:              ██░░░░░░░░  90 MB
all-MiniLM-L12-v2:             ███░░░░░░░ 120 MB
all-distilroberta-v1:          ██████░░░░ 330 MB
all-mpnet-base-v2:             ████████░░ 420 MB
multilingual-mpnet-base-v2:    ███████████████ 1.0 GB
```

---

## 🎯 Khuyến nghị tổng hợp

### Default Choice (90% cases)

```
Model: all-MiniLM-L6-v2
Lý do: Best balance speed/quality/size
```

### High-Quality Applications

```
Model: all-mpnet-base-v2
Lý do: Best quality, acceptable speed
```

### Multilingual Applications

```
Model: paraphrase-multilingual-mpnet-base-v2
Lý do: Best multilingual support
```

### Mobile/Edge Deployment

```
Model: all-MiniLM-L6-v2
Lý do: Smallest, fastest
```

---

## 📚 Resources

- **Model Hub**: https://huggingface.co/sentence-transformers
- **Benchmarks**: https://www.sbert.net/docs/pretrained_models.html
- **MTEB Leaderboard**: https://huggingface.co/spaces/mteb/leaderboard
- **Model Comparison**: `MODEL_COMPARISON.md`

---

**Cập nhật lần cuối:** 13/11/2024  
**Tác giả:** PCM Team

