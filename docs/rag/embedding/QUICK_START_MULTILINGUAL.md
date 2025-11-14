# Quick Start: Multi-Language Embeddings

## 🎯 Overview

Hệ thống pcm-desktop hiện đã hỗ trợ **3 models embedding** đồng thời:

| Language | Model | Dimension | Quality | Speed |
|----------|-------|-----------|---------|-------|
| 🇻🇳 **Vietnamese** | keepitreal/vietnamese-sbert (PhoBERT) | 768 | ⭐⭐⭐⭐⭐ | ~40ms |
| 🇺🇸 **English** | BAAI/bge-m3 (MTEB #1) | 1024 | ⭐⭐⭐⭐⭐ | ~45ms |
| 🔄 **Fallback** | all-MiniLM-L6-v2 | 384 | ⭐⭐⭐⭐ | ~15ms |

---

## 🚀 Quick Setup (5 minutes)

### Step 1: Download Models

```bash
# Download all models (Vietnamese + English + Fallback)
./scripts/setup-multilingual-embeddings.sh

# Or download individually:
./scripts/setup-embeddings-vietnamese.sh  # Vietnamese only
./scripts/setup-embeddings-english.sh     # English only
```

**Download size:** ~800 MB total  
**Time:** 5-10 minutes (depends on internet speed)

---

### Step 2: Build Project

```bash
./scripts/build.sh
```

---

### Step 3: Test Installation

```bash
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*" \
  com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample
```

Expected output:
```
✅ Vietnamese model loaded successfully
✅ English model loaded successfully
✅ Fallback model loaded successfully
```

---

## 💻 Usage

### Basic Usage

```java
import com.noteflix.pcm.rag.embedding.core.EmbeddingServiceRegistry;
import com.noteflix.pcm.rag.embedding.model.Language;

// Initialize registry (loads all models)
try (EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry()) {
    
    // Embed Vietnamese text
    float[] viEmbedding = registry.embed(
        "Xin chào, đây là văn bản tiếng Việt",
        Language.VIETNAMESE
    );
    
    // Embed English text
    float[] enEmbedding = registry.embed(
        "Hello, this is English text",
        Language.ENGLISH
    );
    
    // Check dimensions
    System.out.println("Vietnamese dim: " + viEmbedding.length);  // 768
    System.out.println("English dim: " + enEmbedding.length);     // 1024
}
```

---

### Batch Processing

```java
String[] documents = {
    "Document 1",
    "Document 2",
    "Document 3"
};

// Batch embedding (faster than one-by-one)
float[][] embeddings = registry.embedBatch(documents, Language.ENGLISH);
```

---

### Semantic Search

```java
// Embed query and documents
float[] queryVec = registry.embed("How to validate input?", Language.ENGLISH);
float[] doc1Vec = registry.embed("Input validation guide", Language.ENGLISH);
float[] doc2Vec = registry.embed("Python tutorial", Language.ENGLISH);

// Calculate similarity
float sim1 = cosineSimilarity(queryVec, doc1Vec);  // High
float sim2 = cosineSimilarity(queryVec, doc2Vec);  // Low
```

---

## 📋 Integration Checklist

### For Existing RAG Systems

- [ ] **Step 1:** Download models
  ```bash
  ./scripts/setup-multilingual-embeddings.sh
  ```

- [ ] **Step 2:** Update document model
  ```java
  public class Document {
      private String content;
      private Language language;  // ⭐ ADD THIS
      private float[] embedding;
      private int embeddingDim;   // ⭐ ADD THIS
  }
  ```

- [ ] **Step 3:** Replace EmbeddingService with Registry
  ```java
  // Before:
  EmbeddingService service = new DJLEmbeddingService(modelPath);
  float[] embedding = service.embed(text);
  
  // After:
  EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry();
  float[] embedding = registry.embed(text, language);
  ```

- [ ] **Step 4:** Update indexing logic
  ```java
  // Detect or specify language when indexing
  Language lang = detectLanguage(document.getContent());
  float[] embedding = registry.embed(document.getContent(), lang);
  document.setLanguage(lang);
  document.setEmbedding(embedding);
  ```

- [ ] **Step 5:** Update search logic
  ```java
  // Use same language as document for queries
  float[] queryEmbed = registry.embed(query, document.getLanguage());
  ```

---

## 🎨 Advanced Features

### 1. Check Model Availability

```java
if (registry.hasModel(Language.VIETNAMESE)) {
    System.out.println("✅ Vietnamese model loaded");
} else {
    System.out.println("⚠️  Using fallback for Vietnamese");
}
```

### 2. Get Model Information

```java
String modelName = registry.getModelName(Language.ENGLISH);
int dimension = registry.getDimension(Language.ENGLISH);

System.out.println("Model: " + modelName);
System.out.println("Dimension: " + dimension);
```

### 3. Statistics

```java
System.out.println(registry.getStatistics());
```

Output:
```
Embedding Service Registry Statistics
═══════════════════════════════════════
VIETNAMESE  : ✅ vietnamese-sbert (768d)
ENGLISH     : ✅ bge-m3 (1024d)
Fallback    : ✅ all-MiniLM-L6-v2 (384d)
```

---

## ⚙️ Configuration

### Custom Configuration

```java
// Enable/disable features
boolean autoFallback = true;      // Auto fallback on error
boolean logWarnings = true;       // Log fallback warnings
boolean failFast = false;         // Allow partial initialization

EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry(
    autoFallback,
    logWarnings,
    failFast
);
```

### Model Paths

Edit `MultiModelConfig.java`:

```java
public class MultiModelConfig {
    public static final String VIETNAMESE_MODEL_PATH = 
        "data/models/vietnamese-sbert";
    
    public static final String ENGLISH_MODEL_PATH = 
        "data/models/bge-m3";
    
    public static final String FALLBACK_MODEL_PATH = 
        "data/models/all-MiniLM-L6-v2";
}
```

---

## 🔍 Troubleshooting

### Issue: Models not found

```
❌ Failed to load Vietnamese model: Model not found
```

**Solution:**
```bash
# Re-download models
./scripts/setup-multilingual-embeddings.sh
```

---

### Issue: Out of memory

```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
```bash
# Increase heap size
export JAVA_OPTS="-Xmx4g"
./scripts/run.sh
```

Or load only one model:
```java
// Load only English model
EmbeddingService service = new BgeEmbeddingService(
    "data/models/bge-m3"
);
```

---

### Issue: Slow performance

**Tips:**
1. Use batch processing instead of one-by-one
2. Pre-compute embeddings offline
3. Cache embeddings for frequently used texts
4. Use appropriate model (MiniLM for speed, BGE for quality)

---

## 📊 Performance Benchmarks

### Inference Speed

```
Vietnamese Model (768d):   ~40ms per text
English Model (1024d):     ~45ms per text
Fallback Model (384d):     ~15ms per text
```

### Quality (MTEB Scores)

```
BGE-M3 (English):          75.4 ⭐ State-of-the-art
Vietnamese SBERT:          Excellent for Vietnamese
MiniLM-L6-v2 (Fallback):   69.4 (Good enough)
```

### Memory Usage

```
Vietnamese Model:  ~300 MB
English Model:     ~800 MB
Fallback Model:    ~110 MB
Total (all 3):     ~1.2 GB
```

---

## 📚 More Resources

- **Full Documentation:** [MULTILINGUAL_MODEL_RECOMMENDATIONS.md](./MULTILINGUAL_MODEL_RECOMMENDATIONS.md)
- **Model Comparison:** [MODEL_COMPARISON.md](./MODEL_COMPARISON.md)
- **Selection Guide:** [MODEL_SELECTION_GUIDE.md](./MODEL_SELECTION_GUIDE.md)

### External Links

- **BGE-M3 Model:** https://huggingface.co/BAAI/bge-m3
- **Vietnamese SBERT:** https://huggingface.co/keepitreal/vietnamese-sbert
- **MTEB Leaderboard:** https://huggingface.co/spaces/mteb/leaderboard

---

## ✅ Summary

**What you get:**
- ✅ High-quality Vietnamese embeddings (PhoBERT)
- ✅ State-of-the-art English embeddings (BGE-M3)
- ✅ Reliable fallback model
- ✅ Automatic error handling
- ✅ Easy integration
- ✅ Production-ready

**Setup time:** 5-10 minutes  
**Code changes:** Minimal  
**Performance impact:** Better quality, slightly slower  
**Reliability:** High (automatic fallback)

---

**Created:** November 2024  
**Author:** PCM Team  
**Status:** ✅ Ready for Production

