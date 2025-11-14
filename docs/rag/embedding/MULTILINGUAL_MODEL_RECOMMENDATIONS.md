# Đề xuất Models Embedding Đa Ngôn Ngữ

## 📚 Mục lục
- [Executive Summary](#executive-summary)
- [Model Recommendations](#model-recommendations)
- [Architecture Design](#architecture-design)
- [Implementation Plan](#implementation-plan)
- [Performance Comparison](#performance-comparison)
- [Setup Instructions](#setup-instructions)

---

## 🎯 Executive Summary

### Chiến lược Multi-Model

Hệ thống sẽ hỗ trợ **3 models đồng thời**:

```
┌─────────────────────────────────────────────────────────────┐
│                    MULTI-MODEL ARCHITECTURE                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────────┐│
│  │  Vietnamese Model │  │  English Model   │  │  Fallback  ││
│  │                   │  │                   │  │            ││
│  │  PhoBERT-based   │  │  BGE-M3 or E5    │  │  MiniLM-L6 ││
│  │  384-768 dim      │  │  1024 dim        │  │  384 dim   ││
│  └──────────────────┘  └──────────────────┘  └────────────┘│
│           ▲                     ▲                    ▲       │
│           │                     │                    │       │
│           └─────────────┬───────┴────────────────────┘       │
│                         │                                    │
│                  ┌──────▼──────┐                            │
│                  │   Selector   │                            │
│                  │  (Language)  │                            │
│                  └──────────────┘                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏆 Model Recommendations

### 1. Model cho Tiếng Việt ⭐

#### Option A: keepitreal/vietnamese-sbert (RECOMMENDED)

**Thông số:**
```yaml
Name: keepitreal/vietnamese-sbert
Base: PhoBERT
Dimension: 768
Size: ~140 MB
Format: ONNX available
Type: Sentence Transformers
```

**Ưu điểm:**
- ✅ Được train đặc biệt cho Vietnamese sentence embeddings
- ✅ Dựa trên PhoBERT (state-of-the-art cho tiếng Việt)
- ✅ Compatible với Sentence Transformers
- ✅ Có sẵn trên HuggingFace
- ✅ Kích thước vừa phải (~140MB)
- ✅ Chất lượng cao cho semantic similarity

**Nhược điểm:**
- ⚠️ Chậm hơn MiniLM (~35-45ms)
- ⚠️ Cần nhiều memory hơn (~300MB RAM)

**Use cases:**
- ✅ Vietnamese document search
- ✅ Vietnamese code comments
- ✅ Vietnamese Q&A systems
- ✅ Mixed Vietnamese-English content

**HuggingFace:**
```
https://huggingface.co/keepitreal/vietnamese-sbert
```

---

#### Option B: VoVanPhuc/sup-SimCSE-VietNamese-phobert-base

**Thông số:**
```yaml
Name: VoVanPhuc/sup-SimCSE-VietNamese-phobert-base
Base: PhoBERT + SimCSE
Dimension: 768
Size: ~135 MB
Format: PyTorch/ONNX
Type: Contrastive Learning
```

**Ưu điểm:**
- ✅ SimCSE approach (very good for semantic similarity)
- ✅ PhoBERT foundation
- ✅ Good performance on Vietnamese STS tasks
- ✅ Smaller than multilingual models

**Nhược điểm:**
- ⚠️ Ít documentation hơn
- ⚠️ Community nhỏ hơn

**HuggingFace:**
```
https://huggingface.co/VoVanPhuc/sup-SimCSE-VietNamese-phobert-base
```

---

### 2. Model cho Tiếng Anh ⭐

#### Option A: BAAI/bge-m3 (RECOMMENDED)

**Thông số:**
```yaml
Name: BAAI/bge-m3
Rank: #1 on MTEB (Nov 2024)
Dimension: 1024
Size: ~560 MB
Inference: ~40ms
Format: ONNX available
Type: Multi-Functional (dense, sparse, multi-vector)
```

**MTEB Scores:**
```
Overall: 75.4 ⭐ (TOP 1)
Retrieval: 78.9
Classification: 82.4
Clustering: 71.2
```

**Ưu điểm:**
- ✅ **State-of-the-art** cho English embeddings (2024)
- ✅ Multi-functional (dense + sparse + multi-vector)
- ✅ Context length: 8192 tokens
- ✅ Multilingual (100+ languages, bao gồm Vietnamese)
- ✅ Excellent for retrieval tasks
- ✅ Production-ready
- ✅ Active development from BAAI

**Nhược điểm:**
- ⚠️ Lớn hơn (~560MB)
- ⚠️ Chậm hơn (~40ms)
- ⚠️ Cần ~800MB RAM

**Khi nào dùng:**
- ✅ High-quality English retrieval
- ✅ Code search (excellent for technical content)
- ✅ Long documents (up to 8192 tokens)
- ✅ Production systems requiring best quality
- ✅ Can also serve as Vietnamese model (multilingual)

**HuggingFace:**
```
https://huggingface.co/BAAI/bge-m3
```

---

#### Option B: BAAI/bge-large-en-v1.5

**Thông số:**
```yaml
Name: BAAI/bge-large-en-v1.5
Rank: Top 5 on MTEB
Dimension: 1024
Size: ~335 MB
Inference: ~35ms
Format: ONNX available
```

**MTEB Scores:**
```
Overall: 74.8
Retrieval: 78.2
Classification: 81.5
```

**Ưu điểm:**
- ✅ Excellent quality/speed balance
- ✅ English-specific (better than multilingual for English)
- ✅ Smaller than bge-m3
- ✅ Well-tested in production
- ✅ Great for code search

**Nhược điểm:**
- ⚠️ English only
- ⚠️ Shorter context (512 tokens)

**HuggingFace:**
```
https://huggingface.co/BAAI/bge-large-en-v1.5
```

---

#### Option C: intfloat/e5-large-v2

**Thông số:**
```yaml
Name: intfloat/e5-large-v2
Rank: Top 10 on MTEB
Dimension: 1024
Size: ~335 MB
Inference: ~35ms
Format: ONNX available
```

**MTEB Scores:**
```
Overall: 73.2
Retrieval: 76.8
Classification: 80.1
```

**Ưu điểm:**
- ✅ Very good quality
- ✅ Instruction-aware (prefix-based)
- ✅ Good for Q&A systems
- ✅ Well-documented

**Nhược điểm:**
- ⚠️ Requires prefix ("query: " and "passage: ")
- ⚠️ Slightly lower quality than BGE

**HuggingFace:**
```
https://huggingface.co/intfloat/e5-large-v2
```

---

### 3. Fallback Model (Current)

#### all-MiniLM-L6-v2

**Thông số:**
```yaml
Name: all-MiniLM-L6-v2
Dimension: 384
Size: ~90 MB
Inference: ~15ms
Quality: 69.4/100
```

**Vai trò:**
- ✅ Fallback khi Vietnamese/English models fail
- ✅ Fast prototyping
- ✅ Lightweight option
- ✅ Multi-language support (limited)

**Khi nào dùng:**
- ✅ Error recovery
- ✅ Unknown language
- ✅ Resource constraints
- ✅ Fast preview/testing

---

## 🏗️ Architecture Design

### Multi-Model Service Architecture

```java
┌────────────────────────────────────────────────────────────┐
│                  EmbeddingServiceRegistry                   │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  Map<Language, EmbeddingService>                           │
│                                                             │
│  - VIETNAMESE  → VietnameseSbertEmbeddingService           │
│  - ENGLISH     → BgeM3EmbeddingService                     │
│  - FALLBACK    → MiniLMEmbeddingService (existing)         │
│                                                             │
│  getService(Language lang)                                  │
│  getService(String text) [auto-detect]                     │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

### Component Structure

```
src/main/java/com/noteflix/pcm/rag/
├── core/
│   ├── EmbeddingService.java (interface)
│   ├── EmbeddingServiceRegistry.java ⭐ NEW
│   └── LanguageDetector.java ⭐ NEW (optional)
│
├── embeddings/
│   ├── DJLEmbeddingService.java (existing)
│   ├── VietnameseEmbeddingService.java ⭐ NEW
│   ├── BgeEmbeddingService.java ⭐ NEW
│   └── FallbackEmbeddingService.java ⭐ NEW
│
└── config/
    └── MultiModelConfig.java ⭐ NEW
```

---

## 📋 Implementation Plan

### Phase 1: Setup Infrastructure (Week 1)

#### 1.1 Create Model Registry

```java
public class EmbeddingServiceRegistry {
    private final Map<Language, EmbeddingService> services;
    private final EmbeddingService fallbackService;
    
    public EmbeddingService getService(Language language) {
        return services.getOrDefault(language, fallbackService);
    }
    
    public float[] embed(String text, Language language) {
        EmbeddingService service = getService(language);
        try {
            return service.embed(text);
        } catch (Exception e) {
            log.warn("Failed with primary model, using fallback");
            return fallbackService.embed(text);
        }
    }
}
```

#### 1.2 Define Language Enum

```java
public enum Language {
    VIETNAMESE("vi"),
    ENGLISH("en"),
    AUTO("auto"),
    UNKNOWN("unknown");
    
    private final String code;
    
    Language(String code) {
        this.code = code;
    }
}
```

#### 1.3 Create Configuration

```java
public class MultiModelConfig {
    public static final String VIETNAMESE_MODEL_PATH = 
        "data/models/vietnamese-sbert";
    public static final String ENGLISH_MODEL_PATH = 
        "data/models/bge-m3";
    public static final String FALLBACK_MODEL_PATH = 
        "data/models/all-MiniLM-L6-v2";
    
    // Model-specific configs
    public static final int VIETNAMESE_DIM = 768;
    public static final int ENGLISH_DIM = 1024;
    public static final int FALLBACK_DIM = 384;
}
```

---

### Phase 2: Download and Convert Models (Week 1-2)

#### 2.1 Download Vietnamese Model

```bash
# Script: scripts/setup-embeddings-vietnamese.sh

#!/bin/bash
set -e

MODEL_NAME="keepitreal/vietnamese-sbert"
OUTPUT_DIR="data/models/vietnamese-sbert"

echo "📥 Downloading Vietnamese embedding model..."

# Option 1: Using Optimum (ONNX conversion)
pip install optimum[exporters] onnxruntime

python -c "
from optimum.onnxruntime import ORTModelForFeatureExtraction
from transformers import AutoTokenizer

model = ORTModelForFeatureExtraction.from_pretrained(
    '${MODEL_NAME}',
    export=True
)
tokenizer = AutoTokenizer.from_pretrained('${MODEL_NAME}')

model.save_pretrained('${OUTPUT_DIR}')
tokenizer.save_pretrained('${OUTPUT_DIR}')

print('✅ Vietnamese model downloaded and converted to ONNX')
"
```

#### 2.2 Download English Model (BGE-M3)

```bash
# Script: scripts/setup-embeddings-english.sh

#!/bin/bash
set -e

MODEL_NAME="BAAI/bge-m3"
OUTPUT_DIR="data/models/bge-m3"

echo "📥 Downloading English embedding model (BGE-M3)..."

python -c "
from optimum.onnxruntime import ORTModelForFeatureExtraction
from transformers import AutoTokenizer

model = ORTModelForFeatureExtraction.from_pretrained(
    '${MODEL_NAME}',
    export=True
)
tokenizer = AutoTokenizer.from_pretrained('${MODEL_NAME}')

model.save_pretrained('${OUTPUT_DIR}')
tokenizer.save_pretrained('${OUTPUT_DIR}')

print('✅ English model (BGE-M3) downloaded and converted to ONNX')
"
```

#### 2.3 Master Setup Script

```bash
# Script: scripts/setup-multilingual-embeddings.sh

#!/bin/bash
set -e

echo "🚀 Setting up multi-language embedding models..."

# Check Python and dependencies
command -v python3 >/dev/null 2>&1 || {
    echo "❌ Python 3 required but not found"
    exit 1
}

# Install dependencies
echo "📦 Installing dependencies..."
pip install -q optimum[exporters] onnxruntime transformers torch

# Create directories
mkdir -p data/models

# Download Vietnamese model
echo ""
echo "═══════════════════════════════════════════════"
echo "  1/3: Vietnamese Model (PhoBERT-based)"
echo "═══════════════════════════════════════════════"
./scripts/setup-embeddings-vietnamese.sh

# Download English model
echo ""
echo "═══════════════════════════════════════════════"
echo "  2/3: English Model (BGE-M3)"
echo "═══════════════════════════════════════════════"
./scripts/setup-embeddings-english.sh

# Verify existing fallback model
echo ""
echo "═══════════════════════════════════════════════"
echo "  3/3: Verifying Fallback Model"
echo "═══════════════════════════════════════════════"
if [ -d "data/models/all-MiniLM-L6-v2" ]; then
    echo "✅ Fallback model already exists"
else
    echo "⚠️  Fallback model not found, downloading..."
    ./scripts/setup-embeddings-djl.sh all-MiniLM-L6-v2
fi

echo ""
echo "═══════════════════════════════════════════════"
echo "✅ All models downloaded successfully!"
echo "═══════════════════════════════════════════════"
echo ""
echo "Model locations:"
echo "  Vietnamese: data/models/vietnamese-sbert"
echo "  English:    data/models/bge-m3"
echo "  Fallback:   data/models/all-MiniLM-L6-v2"
echo ""
echo "Next steps:"
echo "  1. Build the project: ./scripts/build.sh"
echo "  2. Run tests: java -cp ... TestMultilingualEmbedding"
```

---

### Phase 3: Implement Services (Week 2-3)

#### 3.1 Vietnamese Embedding Service

```java
// File: src/main/java/com/noteflix/pcm/rag/embeddings/VietnameseEmbeddingService.java

package com.noteflix.pcm.rag.embeddings;

import com.noteflix.pcm.rag.core.EmbeddingService;
import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;

import java.io.IOException;

public class VietnameseEmbeddingService implements EmbeddingService {
    
    private final DJLEmbeddingService delegate;
    
    public VietnameseEmbeddingService(String modelPath) 
            throws ModelNotFoundException, MalformedModelException, IOException {
        this.delegate = new DJLEmbeddingService(modelPath);
    }
    
    @Override
    public float[] embed(String text) throws Exception {
        // Preprocess Vietnamese text if needed
        String processed = preprocessVietnamese(text);
        return delegate.embed(processed);
    }
    
    @Override
    public float[][] embedBatch(String[] texts) throws Exception {
        String[] processed = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            processed[i] = preprocessVietnamese(texts[i]);
        }
        return delegate.embedBatch(processed);
    }
    
    @Override
    public int getDimension() {
        return delegate.getDimension(); // 768 for PhoBERT
    }
    
    @Override
    public void close() {
        delegate.close();
    }
    
    private String preprocessVietnamese(String text) {
        // Vietnamese-specific preprocessing
        // - Normalize tone marks
        // - Handle special characters
        return text.trim();
    }
}
```

#### 3.2 BGE Embedding Service

```java
// File: src/main/java/com/noteflix/pcm/rag/embeddings/BgeEmbeddingService.java

package com.noteflix.pcm.rag.embeddings;

import com.noteflix.pcm.rag.core.EmbeddingService;

public class BgeEmbeddingService implements EmbeddingService {
    
    private final DJLEmbeddingService delegate;
    
    public BgeEmbeddingService(String modelPath) 
            throws Exception {
        this.delegate = new DJLEmbeddingService(modelPath);
    }
    
    @Override
    public float[] embed(String text) throws Exception {
        // BGE-M3 specific preprocessing
        String processed = addBgePrefix(text);
        return delegate.embed(processed);
    }
    
    @Override
    public float[][] embedBatch(String[] texts) throws Exception {
        String[] processed = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            processed[i] = addBgePrefix(texts[i]);
        }
        return delegate.embedBatch(processed);
    }
    
    @Override
    public int getDimension() {
        return delegate.getDimension(); // 1024 for BGE-M3
    }
    
    @Override
    public void close() {
        delegate.close();
    }
    
    private String addBgePrefix(String text) {
        // BGE models may benefit from prefixes for retrieval tasks
        // For search queries: "Represent this sentence for searching: "
        // For documents: just use as-is
        return text;
    }
}
```

#### 3.3 Service Registry Implementation

```java
// File: src/main/java/com/noteflix/pcm/rag/core/EmbeddingServiceRegistry.java

package com.noteflix.pcm.rag.core;

import com.noteflix.pcm.rag.embeddings.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

public class EmbeddingServiceRegistry implements AutoCloseable {
    
    private static final Logger log = LoggerFactory.getLogger(
        EmbeddingServiceRegistry.class
    );
    
    private final Map<Language, EmbeddingService> services;
    private final EmbeddingService fallbackService;
    
    public EmbeddingServiceRegistry() throws Exception {
        this.services = new EnumMap<>(Language.class);
        
        // Initialize Vietnamese model
        try {
            log.info("Loading Vietnamese embedding model...");
            EmbeddingService viService = new VietnameseEmbeddingService(
                "data/models/vietnamese-sbert"
            );
            services.put(Language.VIETNAMESE, viService);
            log.info("✅ Vietnamese model loaded (dim: {})", 
                viService.getDimension());
        } catch (Exception e) {
            log.warn("⚠️  Failed to load Vietnamese model: {}", 
                e.getMessage());
        }
        
        // Initialize English model
        try {
            log.info("Loading English embedding model (BGE-M3)...");
            EmbeddingService enService = new BgeEmbeddingService(
                "data/models/bge-m3"
            );
            services.put(Language.ENGLISH, enService);
            log.info("✅ English model loaded (dim: {})", 
                enService.getDimension());
        } catch (Exception e) {
            log.warn("⚠️  Failed to load English model: {}", 
                e.getMessage());
        }
        
        // Initialize fallback model
        log.info("Loading fallback model (MiniLM)...");
        this.fallbackService = new DJLEmbeddingService(
            "data/models/all-MiniLM-L6-v2"
        );
        log.info("✅ Fallback model loaded (dim: {})", 
            fallbackService.getDimension());
    }
    
    /**
     * Get embedding service for specific language
     */
    public EmbeddingService getService(Language language) {
        return services.getOrDefault(language, fallbackService);
    }
    
    /**
     * Embed text with language-specific model
     */
    public float[] embed(String text, Language language) throws Exception {
        EmbeddingService service = getService(language);
        
        try {
            return service.embed(text);
        } catch (Exception e) {
            log.warn("Failed with {} model, using fallback: {}", 
                language, e.getMessage());
            return fallbackService.embed(text);
        }
    }
    
    /**
     * Batch embedding with language-specific model
     */
    public float[][] embedBatch(String[] texts, Language language) 
            throws Exception {
        EmbeddingService service = getService(language);
        
        try {
            return service.embedBatch(texts);
        } catch (Exception e) {
            log.warn("Failed with {} model, using fallback", language);
            return fallbackService.embedBatch(texts);
        }
    }
    
    /**
     * Get dimension for specific language model
     */
    public int getDimension(Language language) {
        return getService(language).getDimension();
    }
    
    /**
     * Check if model is available for language
     */
    public boolean hasModel(Language language) {
        return services.containsKey(language);
    }
    
    @Override
    public void close() {
        log.info("Closing embedding services...");
        services.values().forEach(service -> {
            try {
                service.close();
            } catch (Exception e) {
                log.warn("Error closing service: {}", e.getMessage());
            }
        });
        
        try {
            fallbackService.close();
        } catch (Exception e) {
            log.warn("Error closing fallback service: {}", e.getMessage());
        }
    }
}
```

---

### Phase 4: Integration (Week 3-4)

#### 4.1 Update RAG Pipeline

```java
// Update existing RAG code to use registry

// Before:
EmbeddingService service = new DJLEmbeddingService(modelPath);
float[] embedding = service.embed(text);

// After:
EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry();

// For Vietnamese content
float[] viEmbedding = registry.embed(viText, Language.VIETNAMESE);

// For English content
float[] enEmbedding = registry.embed(enText, Language.ENGLISH);

// Auto-fallback on error
```

#### 4.2 Update Vector Store

```java
// Support multiple embedding dimensions

public class Document {
    private String id;
    private String content;
    private Language language;  // ⭐ NEW
    private float[] embedding;
    private int embeddingDimension;  // ⭐ NEW
}
```

---

## 📊 Performance Comparison

### Expected Performance

```
┌────────────────────┬──────────┬─────────┬────────┬──────────┬──────────┐
│ Model              │ Language │ Dim     │ Speed  │ Quality  │ Memory   │
├────────────────────┼──────────┼─────────┼────────┼──────────┼──────────┤
│ vietnamese-sbert   │ VI       │ 768     │ ~40ms  │ ⭐⭐⭐⭐⭐ │ ~300 MB  │
│ bge-m3             │ EN       │ 1024    │ ~45ms  │ ⭐⭐⭐⭐⭐ │ ~800 MB  │
│ all-MiniLM-L6-v2   │ Fallback │ 384     │ ~15ms  │ ⭐⭐⭐⭐   │ ~110 MB  │
└────────────────────┴──────────┴─────────┴────────┴──────────┴──────────┘
```

### Quality Estimates

**Vietnamese:**
- Semantic similarity: Excellent (PhoBERT-based)
- Code comments: Very Good
- Mixed content: Good

**English:**
- MTEB Score: 75.4 (State-of-the-art)
- Code search: Excellent
- Technical content: Excellent

**Fallback:**
- General purpose: Good
- Fast retrieval: Excellent
- Quality: Acceptable

---

## 🚀 Setup Instructions

### Quick Start

```bash
# 1. Install Python dependencies
pip install optimum[exporters] onnxruntime transformers torch

# 2. Download all models
./scripts/setup-multilingual-embeddings.sh

# 3. Build project
./scripts/build.sh

# 4. Run test
java -cp "out:lib/*" com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample
```

### Manual Setup

#### Step 1: Vietnamese Model

```bash
python -c "
from optimum.onnxruntime import ORTModelForFeatureExtraction
from transformers import AutoTokenizer

model = ORTModelForFeatureExtraction.from_pretrained(
    'keepitreal/vietnamese-sbert',
    export=True
)
tokenizer = AutoTokenizer.from_pretrained('keepitreal/vietnamese-sbert')

model.save_pretrained('data/models/vietnamese-sbert')
tokenizer.save_pretrained('data/models/vietnamese-sbert')
"
```

#### Step 2: English Model

```bash
python -c "
from optimum.onnxruntime import ORTModelForFeatureExtraction
from transformers import AutoTokenizer

model = ORTModelForFeatureExtraction.from_pretrained(
    'BAAI/bge-m3',
    export=True
)
tokenizer = AutoTokenizer.from_pretrained('BAAI/bge-m3')

model.save_pretrained('data/models/bge-m3')
tokenizer.save_pretrained('data/models/bge-m3')
"
```

#### Step 3: Verify Installation

```bash
ls -lh data/models/

# Expected output:
# drwxr-xr-x  vietnamese-sbert/  (~140 MB)
# drwxr-xr-x  bge-m3/            (~560 MB)
# drwxr-xr-x  all-MiniLM-L6-v2/  (~90 MB)
```

---

## 🧪 Testing

### Test Code

```java
// File: src/test/java/com/noteflix/pcm/rag/MultilingualEmbeddingTest.java

public class MultilingualEmbeddingTest {
    
    @Test
    public void testVietnameseEmbedding() throws Exception {
        try (EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry()) {
            String viText = "Xin chào, đây là văn bản tiếng Việt";
            float[] embedding = registry.embed(viText, Language.VIETNAMESE);
            
            assertNotNull(embedding);
            assertEquals(768, embedding.length);
            System.out.println("✅ Vietnamese embedding: " + embedding.length);
        }
    }
    
    @Test
    public void testEnglishEmbedding() throws Exception {
        try (EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry()) {
            String enText = "Hello, this is English text";
            float[] embedding = registry.embed(enText, Language.ENGLISH);
            
            assertNotNull(embedding);
            assertEquals(1024, embedding.length);
            System.out.println("✅ English embedding: " + embedding.length);
        }
    }
    
    @Test
    public void testFallback() throws Exception {
        try (EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry()) {
            String text = "Test fallback";
            float[] embedding = registry.embed(text, Language.UNKNOWN);
            
            assertNotNull(embedding);
            assertEquals(384, embedding.length);
            System.out.println("✅ Fallback embedding: " + embedding.length);
        }
    }
}
```

---

## 📈 Migration Strategy

### For Existing Data

**⚠️ Important:** Different models have different dimensions, embeddings are NOT compatible!

**Strategy:**

1. **Keep existing data** (MiniLM-L6-v2 embeddings)
2. **Add language field** to documents
3. **Gradual re-indexing:**
   - New documents: Use language-specific model
   - Old documents: Keep using MiniLM (fallback)
   - Optional: Batch re-index old documents

```java
// Migration code
public void migrateDocument(Document doc) {
    // Detect language
    Language lang = detectLanguage(doc.getContent());
    
    // Re-embed with appropriate model
    float[] newEmbedding = registry.embed(doc.getContent(), lang);
    
    // Update document
    doc.setLanguage(lang);
    doc.setEmbedding(newEmbedding);
    doc.setEmbeddingDimension(registry.getDimension(lang));
    
    // Save to database
    documentRepository.update(doc);
}
```

---

## 🎯 Recommendations Summary

### For Your Project

**Vietnamese:**
```
Recommended: keepitreal/vietnamese-sbert
- Size: ~140 MB
- Quality: Excellent for Vietnamese
- Speed: Good (~40ms)
- Based on PhoBERT (proven for Vietnamese NLP)
```

**English:**
```
Recommended: BAAI/bge-m3
- Size: ~560 MB
- Quality: State-of-the-art (MTEB #1)
- Speed: Good (~45ms)
- Best for code search and technical content
- Multi-functional (dense + sparse + multi-vector)
```

**Fallback:**
```
Keep: all-MiniLM-L6-v2
- Size: ~90 MB
- Quality: Good enough
- Speed: Excellent (~15ms)
- Universal compatibility
```

---

## 📚 Resources

### Vietnamese Models
- **keepitreal/vietnamese-sbert**: https://huggingface.co/keepitreal/vietnamese-sbert
- **PhoBERT**: https://github.com/VinAIResearch/PhoBERT
- **VoVanPhuc/sup-SimCSE**: https://huggingface.co/VoVanPhuc/sup-SimCSE-VietNamese-phobert-base

### English Models
- **BGE-M3**: https://huggingface.co/BAAI/bge-m3
- **BGE Large**: https://huggingface.co/BAAI/bge-large-en-v1.5
- **E5 Large**: https://huggingface.co/intfloat/e5-large-v2

### Benchmarks
- **MTEB Leaderboard**: https://huggingface.co/spaces/mteb/leaderboard
- **Sentence Transformers**: https://www.sbert.net/

### Tools
- **Optimum ONNX**: https://huggingface.co/docs/optimum/main/en/onnxruntime/modeling_ort
- **DJL**: https://djl.ai/

---

**Created:** November 2024  
**Author:** PCM Team  
**Status:** Ready for Implementation

