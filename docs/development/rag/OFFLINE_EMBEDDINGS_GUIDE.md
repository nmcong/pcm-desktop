# Offline Embeddings Guide - 100% Không Cần Internet

## 🎯 Vấn đề

**Để có semantic search với Qdrant, cần:**
1. ❌ **ChatGPT/GPT models**: Không có embedding API, cần internet
2. ✅ **Embedding models**: Convert text → vectors (embeddings)

**Text → Embedding → Vector DB → Semantic Search**

---

## 📊 So sánh Keyword vs Semantic Search

### Keyword Search (Lucene - hiện tại)
```
Query: "validate customer email"
→ Tìm documents có từ "validate", "customer", "email"
→ BM25 scoring

Result: Documents có exact words
```

### Semantic Search (Embeddings + Qdrant)
```
Query: "validate customer email"
→ Convert to vector: [0.23, -0.45, 0.12, ...]
→ Find similar vectors (cosine similarity)

Result: Documents có nghĩa tương tự:
- "check user email address"
- "verify client email format"
- "email validation for customers"
```

**Semantic search hiểu nghĩa, không chỉ match keywords!**

---

## ✅ GIẢI PHÁP: Local Embedding Models

### Phương án 1: ONNX Runtime + Sentence Transformers (RECOMMENDED) ⭐

**Ưu điểm:**
- ✅ 100% offline
- ✅ Fast inference
- ✅ Small models (80-420 MB)
- ✅ High quality embeddings
- ✅ Pure Java (ONNX Runtime Java)

**Models phổ biến:**

| Model | Dimensions | Size | Speed | Quality | Recommended |
|-------|-----------|------|-------|---------|-------------|
| **all-MiniLM-L6-v2** | 384 | 80 MB | ⚡⚡⚡ Very fast | ⭐⭐⭐ Good | ✅ Best for desktop |
| **all-mpnet-base-v2** | 768 | 420 MB | ⚡⚡ Fast | ⭐⭐⭐⭐ Better | For larger corpus |
| **multilingual-e5-small** | 384 | 120 MB | ⚡⚡⚡ Fast | ⭐⭐⭐ Good | ✅ Vietnamese support |

---

## 🚀 IMPLEMENTATION

### Step 1: Download ONNX Runtime

```bash
cd lib/rag

# ONNX Runtime (Java)
wget https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.16.3/onnxruntime-1.16.3.jar

# Optional: GPU support (if needed)
# wget https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime_gpu/1.16.3/onnxruntime_gpu-1.16.3.jar
```

### Step 2: Download Embedding Model

```bash
cd data/models

# Download all-MiniLM-L6-v2 (ONNX format)
mkdir -p sentence-transformers
cd sentence-transformers

# Model files
wget https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/model.onnx
wget https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer.json
wget https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/config.json

echo "✅ Model downloaded!"
```

### Step 3: Implement Embedding Service

**Interface:**
```java
package com.noteflix.pcm.rag.embedding;

public interface EmbeddingService {
    /**
     * Generate embedding for text.
     * 
     * @param text Input text
     * @return Embedding vector (float array)
     */
    float[] embed(String text);
    
    /**
     * Generate embeddings for multiple texts (batch).
     * 
     * @param texts Input texts
     * @return Embedding vectors
     */
    float[][] embedBatch(String[] texts);
    
    /**
     * Get embedding dimension.
     * 
     * @return Vector dimension (e.g., 384, 768)
     */
    int getDimension();
}
```

**ONNX Implementation:**
```java
package com.noteflix.pcm.rag.embedding;

import ai.onnxruntime.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.FloatBuffer;
import java.util.*;

@Slf4j
public class ONNXEmbeddingService implements EmbeddingService {
    
    private final OrtEnvironment env;
    private final OrtSession session;
    private final Tokenizer tokenizer;
    private final int dimension;
    
    public ONNXEmbeddingService(String modelPath) throws Exception {
        this.env = OrtEnvironment.getEnvironment();
        
        // Load ONNX model
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
        this.session = env.createSession(modelPath + "/model.onnx", opts);
        
        // Load tokenizer
        this.tokenizer = new Tokenizer(modelPath + "/tokenizer.json");
        
        // Get dimension from model output
        this.dimension = getDimensionFromModel();
        
        log.info("ONNX Embedding model loaded: {} dimensions", dimension);
    }
    
    @Override
    public float[] embed(String text) {
        try {
            // 1. Tokenize
            TokenizerOutput tokens = tokenizer.encode(text);
            
            // 2. Create ONNX inputs
            Map<String, OnnxTensor> inputs = new HashMap<>();
            
            long[] inputIds = tokens.getInputIds();
            long[] attentionMask = tokens.getAttentionMask();
            
            long[][] inputIdsArray = new long[][] { inputIds };
            long[][] attentionMaskArray = new long[][] { attentionMask };
            
            inputs.put("input_ids", OnnxTensor.createTensor(env, inputIdsArray));
            inputs.put("attention_mask", OnnxTensor.createTensor(env, attentionMaskArray));
            
            // 3. Run inference
            OrtSession.Result result = session.run(inputs);
            
            // 4. Get embeddings (last_hidden_state)
            float[][] embeddings = (float[][]) result.get(0).getValue();
            
            // 5. Mean pooling
            float[] pooled = meanPooling(embeddings[0], attentionMask);
            
            // 6. Normalize
            normalize(pooled);
            
            return pooled;
            
        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
            throw new RuntimeException("Embedding failed", e);
        }
    }
    
    @Override
    public float[][] embedBatch(String[] texts) {
        float[][] embeddings = new float[texts.length][];
        
        for (int i = 0; i < texts.length; i++) {
            embeddings[i] = embed(texts[i]);
        }
        
        return embeddings;
    }
    
    @Override
    public int getDimension() {
        return dimension;
    }
    
    // ========== Helper Methods ==========
    
    private float[] meanPooling(float[] embeddings, long[] mask) {
        // Average pooling with attention mask
        float[] result = new float[dimension];
        int validTokens = 0;
        
        for (int i = 0; i < mask.length; i++) {
            if (mask[i] == 1) {
                for (int j = 0; j < dimension; j++) {
                    result[j] += embeddings[i * dimension + j];
                }
                validTokens++;
            }
        }
        
        for (int j = 0; j < dimension; j++) {
            result[j] /= validTokens;
        }
        
        return result;
    }
    
    private void normalize(float[] vector) {
        // L2 normalization
        float norm = 0;
        
        for (float v : vector) {
            norm += v * v;
        }
        
        norm = (float) Math.sqrt(norm);
        
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }
    }
    
    private int getDimensionFromModel() {
        // Get from model metadata
        return 384; // Default for all-MiniLM-L6-v2
    }
}
```

---

## 🎨 Phương án 2: DJL (Deep Java Library) - Easier!

**DJL = Deep Learning for Java (Amazon)**

**Ưu điểm:**
- ✅ 100% offline
- ✅ Easier API
- ✅ Auto-download models
- ✅ Support nhiều frameworks (PyTorch, TensorFlow, ONNX)

**Setup:**
```bash
cd lib/rag

# DJL API
wget https://repo1.maven.org/maven2/ai/djl/api/0.25.0/api-0.25.0.jar

# DJL ONNX Runtime
wget https://repo1.maven.org/maven2/ai/djl/onnx/onnx-engine/0.25.0/onnx-engine-0.25.0.jar

# DJL HuggingFace Tokenizers
wget https://repo1.maven.org/maven2/ai/djl/huggingface/tokenizers/0.25.0/tokenizers-0.25.0.jar
```

**Implementation (Simpler!):**
```java
package com.noteflix.pcm.rag.embedding;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.*;
import ai.djl.translate.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class DJLEmbeddingService implements EmbeddingService {
    
    private final ZooModel<String, float[]> model;
    private final Predictor<String, float[]> predictor;
    private final int dimension;
    
    public DJLEmbeddingService(String modelPath) throws Exception {
        // Load model
        Criteria<String, float[]> criteria = Criteria.builder()
            .setTypes(String.class, float[].class)
            .optModelPath(Path.of(modelPath))
            .optEngine("OnnxRuntime")
            .optTranslator(new SentenceTransformer())
            .build();
        
        this.model = criteria.loadModel();
        this.predictor = model.newPredictor();
        this.dimension = 384; // from model config
        
        log.info("DJL Embedding model loaded: {} dimensions", dimension);
    }
    
    @Override
    public float[] embed(String text) {
        try {
            return predictor.predict(text);
        } catch (TranslateException e) {
            log.error("Failed to generate embedding", e);
            throw new RuntimeException("Embedding failed", e);
        }
    }
    
    @Override
    public float[][] embedBatch(String[] texts) {
        float[][] embeddings = new float[texts.length][];
        
        for (int i = 0; i < texts.length; i++) {
            embeddings[i] = embed(texts[i]);
        }
        
        return embeddings;
    }
    
    @Override
    public int getDimension() {
        return dimension;
    }
    
    public void close() {
        predictor.close();
        model.close();
    }
}
```

---

## 🔄 Integration với RAG System

### Update RAGDocument
```java
@Data
@Builder
public class RAGDocument {
    private String id;
    private String content;
    private DocumentType type;
    
    // NEW: Store embedding
    private float[] embedding;
    
    // Metadata
    private Map<String, String> metadata;
    private LocalDateTime indexedAt;
}
```

### Update VectorStore
```java
public interface VectorStore {
    /**
     * Index document with embedding.
     */
    void indexDocument(RAGDocument document, float[] embedding);
    
    /**
     * Search by embedding (semantic search).
     */
    List<ScoredDocument> searchByEmbedding(float[] queryEmbedding, RetrievalOptions options);
    
    /**
     * Hybrid search: keyword + semantic.
     */
    List<ScoredDocument> hybridSearch(String query, float[] embedding, RetrievalOptions options);
}
```

### Enhanced RAG Service
```java
@Slf4j
public class SemanticRAGService implements RAGService {
    
    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;
    
    public SemanticRAGService(VectorStore vectorStore, EmbeddingService embeddingService) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
    }
    
    @Override
    public void indexDocument(RAGDocument document) {
        // Generate embedding
        float[] embedding = embeddingService.embed(document.getContent());
        
        // Store with embedding
        document.setEmbedding(embedding);
        vectorStore.indexDocument(document, embedding);
        
        log.info("Indexed document with embedding: {}", document.getId());
    }
    
    @Override
    public RAGResponse query(String query, RetrievalOptions options) {
        // Generate query embedding
        float[] queryEmbedding = embeddingService.embed(query);
        
        // Search by semantic similarity
        List<ScoredDocument> results;
        
        if (options.getSearchMode() == SearchMode.SEMANTIC) {
            results = vectorStore.searchByEmbedding(queryEmbedding, options);
        } else if (options.getSearchMode() == SearchMode.HYBRID) {
            results = vectorStore.hybridSearch(query, queryEmbedding, options);
        } else {
            results = vectorStore.search(query, options); // Keyword only
        }
        
        return buildResponse(query, results);
    }
}
```

---

## 📊 Performance Comparison

### Embedding Generation Speed

**all-MiniLM-L6-v2 (384d):**
- Single text: ~10-20ms
- Batch 100 texts: ~500ms
- Memory: ~200MB

**all-mpnet-base-v2 (768d):**
- Single text: ~30-50ms
- Batch 100 texts: ~1.5s
- Memory: ~500MB

**Recommendation:** all-MiniLM-L6-v2 cho desktop app! ⚡

---

## 🎯 RECOMMENDED SETUP

### Phase 1: Keyword Search (DONE) ✅
```java
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.lucene("data/rag/index")
);

RAGService rag = new DefaultRAGService(store);
```

### Phase 2: Add Semantic Search (Optional)
```java
// 1. Create embedding service
EmbeddingService embeddings = new DJLEmbeddingService(
    "data/models/all-MiniLM-L6-v2"
);

// 2. Create Qdrant store
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.qdrantLocal()
);

// 3. Create semantic RAG
RAGService rag = new SemanticRAGService(store, embeddings);

// 4. Index documents
RAGDocument doc = RAGDocument.builder()
    .content("Customer validation logic...")
    .build();

rag.indexDocument(doc); // Auto-generates embedding!

// 5. Semantic search
RAGResponse response = rag.query(
    "How to validate customers?",
    RetrievalOptions.builder()
        .searchMode(SearchMode.SEMANTIC)
        .build()
);
```

---

## 🌟 Vietnamese Support

**Model: multilingual-e5-small**
- ✅ Supports Vietnamese
- ✅ 384 dimensions
- ✅ 120 MB
- ✅ Good quality

```bash
# Download Vietnamese model
cd data/models
mkdir multilingual-e5-small

wget https://huggingface.co/intfloat/multilingual-e5-small/resolve/main/model.onnx
wget https://huggingface.co/intfloat/multilingual-e5-small/resolve/main/tokenizer.json
```

```java
EmbeddingService embeddings = new DJLEmbeddingService(
    "data/models/multilingual-e5-small"
);

// Works with Vietnamese!
float[] embedding = embeddings.embed("Làm sao để xác thực khách hàng?");
```

---

## 📦 Complete Setup Script

```bash
#!/bin/bash

echo "=== Setting up Offline Embeddings ==="

# 1. Create directories
mkdir -p lib/rag data/models

# 2. Download DJL libraries
cd lib/rag
echo "Downloading DJL..."
wget -q https://repo1.maven.org/maven2/ai/djl/api/0.25.0/api-0.25.0.jar
wget -q https://repo1.maven.org/maven2/ai/djl/onnx/onnx-engine/0.25.0/onnx-engine-0.25.0.jar
wget -q https://repo1.maven.org/maven2/ai/djl/huggingface/tokenizers/0.25.0/tokenizers-0.25.0.jar

echo "✅ DJL downloaded"

# 3. Download embedding model
cd ../../data/models
mkdir -p all-MiniLM-L6-v2
cd all-MiniLM-L6-v2

echo "Downloading embedding model..."
wget -q https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/model.onnx
wget -q https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer.json
wget -q https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/config.json

echo "✅ Model downloaded"

cd ../../../

echo ""
echo "✅ Setup complete!"
echo ""
echo "Model: all-MiniLM-L6-v2"
echo "Size: ~80 MB"
echo "Dimensions: 384"
echo "Location: data/models/all-MiniLM-L6-v2"
echo ""
echo "Ready for semantic search! 🚀"
```

---

## ✅ SUMMARY

### Câu hỏi:
**"LLM để tạo vector db thì sao? Cách offline là gì?"**

### Trả lời:

1. ❌ **ChatGPT/GPT không dùng được** cho embeddings offline
   - Cần internet
   - Không có embedding API public
   
2. ✅ **Dùng local embedding models:**
   - **all-MiniLM-L6-v2** (RECOMMENDED)
   - 100% offline
   - Fast (~10-20ms per text)
   - Small (80 MB)
   - High quality

3. ✅ **Implementation:**
   - DJL (Deep Java Library) - Easier! ⭐
   - ONNX Runtime - More control
   - Both 100% offline

4. ✅ **Setup:**
   - Download model (1 time, ~80 MB)
   - Add DJL libs (3 JARs)
   - Use `EmbeddingService`

### Flow:
```
Text → EmbeddingService → Vector (384 floats)
         ↓
    VectorStore (Qdrant/Lucene)
         ↓
    Semantic Search 🎯
```

**Hiện tại:**
- ✅ Lucene (keyword search) - Working!

**Nếu cần semantic search:**
- ✅ Add DJL + all-MiniLM-L6-v2
- ✅ 100% offline
- ✅ Vietnamese support available!

**Docs:** `docs/development/rag/OFFLINE_EMBEDDINGS_GUIDE.md` 📖
