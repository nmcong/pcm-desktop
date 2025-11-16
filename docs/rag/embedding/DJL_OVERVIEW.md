# DJL (Deep Java Library) - Tổng quan và Hướng dẫn

## 📚 Mục lục

- [Giới thiệu](#giới-thiệu)
- [Kiến trúc](#kiến-trúc)
- [Cài đặt](#cài-đặt)
- [Sử dụng](#sử-dụng)
- [API Reference](#api-reference)
- [Troubleshooting](#troubleshooting)
- [Tài nguyên](#tài-nguyên)

---

## 🎯 Giới thiệu

### DJL là gì?

**Deep Java Library (DJL)** là một framework deep learning mã nguồn mở dành cho Java, được phát triển bởi AWS. DJL cung
cấp:

- ✅ **API Java native**: Không cần Python, hoàn toàn Java
- ✅ **Multi-engine**: Hỗ trợ nhiều backend (PyTorch, TensorFlow, ONNX Runtime, MXNet)
- ✅ **Production-ready**: Tối ưu cho môi trường production
- ✅ **Offline-first**: Chạy hoàn toàn offline sau khi download model
- ✅ **Cross-platform**: Windows, macOS, Linux

### Tại sao chọn DJL cho Embeddings?

| Đặc điểm        | DJL                     | Python (sentence-transformers) |
|-----------------|-------------------------|--------------------------------|
| **Tốc độ**      | ⚡ Nhanh (JVM optimized) | 🐢 Chậm hơn                    |
| **Memory**      | 💚 Hiệu quả             | 💛 Tốn memory hơn              |
| **Deployment**  | ✅ Single JAR            | ❌ Cần Python runtime           |
| **Integration** | ✅ Trực tiếp trong Java  | ❌ Cần bridge/API               |
| **Offline**     | ✅ Hoàn toàn offline     | ✅ Có thể offline               |
| **Production**  | ✅ Enterprise-ready      | 💛 Cần setup phức tạp          |

---

## 🏗️ Kiến trúc

### Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────┐
│                   PCM Desktop Application                │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│              DJLEmbeddingService (Service Layer)         │
│  • Text → Embeddings conversion                         │
│  • Model management                                      │
│  • Caching & optimization                                │
└─────────────────────────────────────────────────────────┘
                           │
        ┌──────────────────┴──────────────────┐
        ▼                                     ▼
┌──────────────────┐              ┌──────────────────────┐
│   ONNX Runtime   │              │  HuggingFace         │
│                  │              │  Tokenizer           │
│  • Model         │              │                      │
│    inference     │              │  • Text → Tokens     │
│  • Optimization  │              │  • Vocab mapping     │
└──────────────────┘              └──────────────────────┘
        │                                     │
        └──────────────────┬──────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────┐
│              Sentence-Transformers Model                 │
│                   (ONNX Format)                          │
│                                                           │
│  • model.onnx        - Neural network weights            │
│  • tokenizer.json    - Tokenization config               │
│  • config.json       - Model configuration               │
└─────────────────────────────────────────────────────────┘
```

### Flow hoạt động

```
User Input: "How to validate customers?"
    │
    ▼
┌─────────────────────────────────┐
│  1. Tokenization                │
│     Text → Token IDs            │
│     [101, 2129, 2000, ...]      │
└─────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────┐
│  2. ONNX Inference              │
│     Token IDs → Hidden States   │
│     Shape: [1, seq_len, 384]    │
└─────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────┐
│  3. Mean Pooling                │
│     Average token embeddings    │
│     Shape: [384]                │
└─────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────┐
│  4. L2 Normalization            │
│     Normalize to unit vector    │
│     Final: float[384]           │
└─────────────────────────────────┘
```

---

## 📦 Cài đặt

### 1. Tự động (Khuyến nghị)

```bash
# Download DJL libraries và model
./scripts/setup-embeddings-djl.sh

# Build project
./scripts/build.sh
```

### 2. Thủ công

#### Bước 1: Download DJL JARs

Download các JARs sau vào `lib/rag/`:

```bash
cd lib/rag

# DJL API (Core)
curl -LO https://repo1.maven.org/maven2/ai/djl/api/0.35.0/api-0.35.0.jar

# ONNX Runtime Engine
curl -LO https://repo1.maven.org/maven2/ai/djl/onnxruntime/onnxruntime-engine/0.35.0/onnxruntime-engine-0.35.0.jar

# HuggingFace Tokenizers
curl -LO https://repo1.maven.org/maven2/ai/djl/huggingface/tokenizers/0.35.0/tokenizers-0.35.0.jar

# ONNX Runtime (Backend)
curl -LO https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.23.2/onnxruntime-1.23.2.jar
```

#### Bước 2: Download Model

```bash
# Tạo thư mục model
mkdir -p data/models/all-MiniLM-L6-v2
cd data/models/all-MiniLM-L6-v2

# Download model files
curl -LO "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/model.onnx"
curl -LO "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer.json"
curl -LO "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/config.json"
```

#### Bước 3: Build

```bash
./scripts/build.sh
```

### 3. Kiểm tra cài đặt

```bash
# Liệt kê JARs
ls -lh lib/rag/*.jar

# Kết quả mong đợi:
# api-0.35.0.jar              (~900 KB)
# onnxruntime-engine-0.35.0.jar  (~56 KB)
# tokenizers-0.35.0.jar         (~18 MB)
# onnxruntime-1.23.2.jar        (~72 MB)
```

---

## 💻 Sử dụng

### Basic Usage

```java
import com.noteflix.pcm.rag.embedding.DJLEmbeddingService;
import com.noteflix.pcm.rag.embedding.EmbeddingService;

// Khởi tạo service
EmbeddingService embeddings = new DJLEmbeddingService(
    "data/models/all-MiniLM-L6-v2"
);

// Tạo embedding cho một text
String text = "How to validate customer information?";
float[] vector = embeddings.embed(text);

System.out.println("Dimension: " + vector.length);  // 384
System.out.println("First values: " + Arrays.toString(
    Arrays.copyOf(vector, 5)
));

// Cleanup
((DJLEmbeddingService) embeddings).close();
```

### Batch Processing

```java
// Embed nhiều texts cùng lúc
String[] texts = {
    "How to validate customer information?",
    "Steps to verify user data",
    "Customer verification process"
};

float[][] embeddings = embeddingService.embedBatch(texts);

// embeddings.length = 3
// embeddings[0].length = 384
```

### Semantic Similarity

```java
// Tính cosine similarity
public float cosineSimilarity(float[] v1, float[] v2) {
    double dotProduct = 0.0;
    for (int i = 0; i < v1.length; i++) {
        dotProduct += v1[i] * v2[i];
    }
    return (float) dotProduct;  // Already normalized
}

// Sử dụng
float[] emb1 = embeddings.embed("validate customer");
float[] emb2 = embeddings.embed("verify user");
float[] emb3 = embeddings.embed("prepare dinner");

float sim12 = cosineSimilarity(emb1, emb2);  // ~0.85 (cao - tương tự)
float sim13 = cosineSimilarity(emb1, emb3);  // ~0.12 (thấp - khác nhau)

System.out.printf("Similarity (1-2): %.3f%n", sim12);
System.out.printf("Similarity (1-3): %.3f%n", sim13);
```

### Production Usage với Cache

```java
public class CachedEmbeddingService implements EmbeddingService {
    private final EmbeddingService delegate;
    private final Map<String, float[]> cache = new ConcurrentHashMap<>();
    
    public CachedEmbeddingService(EmbeddingService delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public float[] embed(String text) {
        return cache.computeIfAbsent(text, delegate::embed);
    }
    
    // ... other methods
}

// Usage
EmbeddingService base = new DJLEmbeddingService("data/models/all-MiniLM-L6-v2");
EmbeddingService cached = new CachedEmbeddingService(base);

// Lần 1: Tính toán (chậm)
float[] emb1 = cached.embed("test");  // ~20ms

// Lần 2: Lấy từ cache (nhanh)
float[] emb2 = cached.embed("test");  // ~0.01ms
```

---

## 📖 API Reference

### DJLEmbeddingService

#### Constructor

```java
public DJLEmbeddingService(String modelPath) throws IOException
```

**Parameters:**

- `modelPath` - Đường dẫn đến thư mục chứa model (phải có `model.onnx`, `tokenizer.json`, `config.json`)

**Throws:**

- `IOException` - Nếu model không tồn tại hoặc không load được

**Example:**

```java
EmbeddingService service = new DJLEmbeddingService(
    "data/models/all-MiniLM-L6-v2"
);
```

#### Methods

##### `embed(String text)`

Tạo embedding vector cho một text.

```java
public float[] embed(String text)
```

**Parameters:**

- `text` - Text cần embed (tối đa 512 tokens)

**Returns:**

- `float[]` - Vector embedding đã được normalize (L2 norm = 1)

**Throws:**

- `RuntimeException` - Nếu inference failed

**Time complexity:** O(n) với n là số tokens

**Example:**

```java
float[] vector = service.embed("Hello world");
```

##### `embedBatch(String[] texts)`

Tạo embeddings cho nhiều texts.

```java
public float[][] embedBatch(String[] texts)
```

**Parameters:**

- `texts` - Array of texts

**Returns:**

- `float[][]` - Array of embedding vectors

**Example:**

```java
float[][] vectors = service.embedBatch(new String[]{
    "Text 1",
    "Text 2",
    "Text 3"
});
```

##### `getDimension()`

Lấy số chiều của embedding vector.

```java
public int getDimension()
```

**Returns:**

- `int` - Dimension (384 cho all-MiniLM-L6-v2)

##### `getModelName()`

Lấy tên model.

```java
public String getModelName()
```

**Returns:**

- `String` - Model name

##### `close()`

Giải phóng resources.

```java
public void close()
```

**Example:**

```java
try (DJLEmbeddingService service = new DJLEmbeddingService("...")) {
    // Use service
} // Auto-closes
```

#### Static Methods

##### `createDefault()`

Tạo service với model mặc định.

```java
public static DJLEmbeddingService createDefault() throws IOException
```

**Returns:**

- `DJLEmbeddingService` - Service với model all-MiniLM-L6-v2

**Example:**

```java
EmbeddingService service = DJLEmbeddingService.createDefault();
```

---

## 🔧 Troubleshooting

### Lỗi: "Model not found"

**Nguyên nhân:** Model chưa được download

**Giải pháp:**

```bash
./scripts/setup-embeddings-djl.sh
```

### Lỗi: "zip END header not found"

**Nguyên nhân:** JAR file bị corrupt

**Giải pháp:**

```bash
# Xóa file bị lỗi
rm lib/rag/*.jar

# Download lại
./scripts/setup-embeddings-djl.sh
```

### Lỗi: "OutOfMemoryError"

**Nguyên nhân:** Không đủ heap memory

**Giải pháp:**

```bash
# Tăng heap size khi chạy
export JAVA_OPTS="-Xmx4g"
./scripts/run.sh
```

### Lỗi: "UnsatisfiedLinkError" (Windows)

**Nguyên nhân:** Thiếu Visual C++ Redistributable

**Giải pháp:**

1. Download [VC++ Redistributable](https://learn.microsoft.com/en-us/cpp/windows/latest-supported-vc-redist)
2. Cài đặt
3. Restart application

### Performance chậm

**Giải pháp:**

1. **Batch processing**: Embed nhiều texts cùng lúc
2. **Caching**: Cache embeddings đã tính
3. **Warm-up**: Chạy vài inference để JVM optimize
4. **Thread pool**: Dùng parallel processing

```java
// Warm-up
for (int i = 0; i < 10; i++) {
    service.embed("warmup");
}

// Parallel processing
List<String> texts = ...;
List<float[]> embeddings = texts.parallelStream()
    .map(service::embed)
    .collect(Collectors.toList());
```

---

## 📚 Tài nguyên

### Documentation

- **DJL Official**: https://djl.ai/
- **ONNX Runtime**: https://onnxruntime.ai/
- **Sentence Transformers**: https://www.sbert.net/

### Models

- **HuggingFace Models**: https://huggingface.co/sentence-transformers
- **Model Selection Guide**: `MODEL_SELECTION_GUIDE.md`
- **Model Comparison**: `MODEL_COMPARISON.md`

### Examples

- **Basic Example**: `src/main/java/com/noteflix/pcm/rag/examples/DJLEmbeddingExample.java`
- **RAG Example**: `src/main/java/com/noteflix/pcm/rag/examples/RAGExample.java`

### Community

- **DJL Discord**: https://discord.gg/deepjavalibrary
- **GitHub Issues**: https://github.com/deepjavalibrary/djl/issues

---

## 🎓 Best Practices

### 1. Resource Management

```java
// ✅ Good: Auto-close
try (DJLEmbeddingService service = new DJLEmbeddingService("...")) {
    float[] emb = service.embed("text");
}

// ❌ Bad: Memory leak
DJLEmbeddingService service = new DJLEmbeddingService("...");
service.embed("text");
// Never closed!
```

### 2. Reuse Service Instance

```java
// ✅ Good: Reuse
DJLEmbeddingService service = new DJLEmbeddingService("...");
for (String text : texts) {
    float[] emb = service.embed(text);
}
service.close();

// ❌ Bad: Recreate every time (slow!)
for (String text : texts) {
    DJLEmbeddingService service = new DJLEmbeddingService("...");
    float[] emb = service.embed(text);
    service.close();
}
```

### 3. Batch Processing

```java
// ✅ Good: Batch
float[][] embeddings = service.embedBatch(texts);

// ❌ Bad: One by one
float[][] embeddings = new float[texts.length][];
for (int i = 0; i < texts.length; i++) {
    embeddings[i] = service.embed(texts[i]);
}
```

### 4. Error Handling

```java
try {
    float[] emb = service.embed(text);
} catch (RuntimeException e) {
    log.error("Embedding failed for text: {}", text, e);
    // Fallback hoặc retry
}
```

---

## 📊 Performance Tips

### Benchmarks

| Operation            | Time   | Memory  |
|----------------------|--------|---------|
| Load model           | ~500ms | ~300 MB |
| First inference      | ~100ms | +50 MB  |
| Subsequent inference | ~20ms  | Stable  |
| Batch (10 texts)     | ~80ms  | Stable  |

### Optimization

1. **Warm-up JVM**: 10-20 inference calls
2. **Use caching**: Cache frequent queries
3. **Batch processing**: Process multiple texts together
4. **Thread pooling**: Parallel processing cho large datasets
5. **Model selection**: Chọn model phù hợp (xem `MODEL_SELECTION_GUIDE.md`)

---

## 📝 Changelog

### Version 2.1.0 (2025-11-14)

- ✅ Updated to ONNX Runtime 1.23.2 (latest stable version)
- ✅ Enhanced security with official Microsoft build
- ✅ Smaller file size (72MB vs 89MB)

### Version 2.0.0 (2024-11-13)

- ✅ Full DJL ONNX Runtime implementation
- ✅ Updated to DJL 0.35.0
- ✅ Updated to ONNX Runtime 1.19.0
- ✅ Improved error handling
- ✅ Better resource management

### Version 1.0.0 (Initial)

- ✅ Basic structure
- ❌ Placeholder implementation

---

## 🤝 Contributing

Nếu bạn muốn contribute:

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push và create Pull Request

---

## 📄 License

Xem `LICENSE` file trong root directory.

---

**Cập nhật lần cuối:** 13/11/2024  
**Phiên bản:** 2.0.0  
**Tác giả:** PCM Team

