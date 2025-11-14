# Source Code Changes for PyTorch Engine

> 📝 **Complete guide** on code changes needed for PyTorch Engine support  
> ⏱️ **Estimated time:** 30-60 minutes  
> 🎯 **Goal:** Support both ONNX and PyTorch engines with config switch

---

## 📋 OVERVIEW

### Files Created (New)
1. ✅ `PyTorchEmbeddingService.java` - Already created!
2. `EngineConfig.java` - Engine selection config
3. `VietnamesePyTorchService.java` - Vietnamese-specific PyTorch wrapper
4. `BgePyTorchService.java` - English-specific PyTorch wrapper

### Files Modified (Updates)
1. `EmbeddingServiceRegistry.java` - Add engine selection logic
2. `MultiModelConfig.java` - Add engine config options

### Total Changes
- **New files:** 4
- **Modified files:** 2
- **Lines of code:** ~300 new, ~50 modified

---

## ✅ OFFLINE 100%

### Setup Phase (1 lần, cần internet)

```bash
# Step 1: Download PyTorch libs (~500 MB)
wget https://repo1.maven.org/maven2/ai/djl/pytorch/...
# Save to: lib/pytorch/

# Step 2: Download models (no conversion!)
python3 scripts/setup-pytorch-models.sh
# Save to: data/models/vietnamese-sbert-pytorch/
#          data/models/bge-m3-pytorch/
```

### Runtime Phase (100% offline!)

```
Java Application
├── lib/pytorch/           ✅ Local
│   ├── pytorch-engine.jar
│   └── pytorch-native-*.jar
├── data/models/           ✅ Local
│   ├── vietnamese-sbert-pytorch/
│   └── bge-m3-pytorch/
└── No Python needed       ✅
    No Internet needed     ✅
```

**Same as ONNX! Both 100% offline after setup!**

---

## 📝 CODE CHANGES DETAIL

### 1. EngineConfig.java (NEW)

**Location:** `src/main/java/com/noteflix/pcm/rag/embedding/config/EngineConfig.java`

```java
package com.noteflix.pcm.rag.embedding.config;

/**
 * Configuration for embedding engine selection.
 * 
 * Supports both ONNX Runtime and PyTorch engines.
 */
public class EngineConfig {
  
  /** Embedding engine type */
  public enum EngineType {
    /** ONNX Runtime (lightweight, fast CPU, requires fast tokenizer) */
    ONNX,
    
    /** PyTorch Engine (full compatibility, all tokenizers, GPU support) */
    PYTORCH
  }
  
  // Engine selection
  public static final EngineType DEFAULT_ENGINE = EngineType.ONNX;
  public static final EngineType VIETNAMESE_ENGINE = EngineType.PYTORCH;  // Use PyTorch for Vietnamese
  public static final EngineType ENGLISH_ENGINE = EngineType.PYTORCH;     // Use PyTorch for English
  
  // Model paths by engine
  public static final String VIETNAMESE_ONNX_PATH = "data/models/vietnamese-sbert";
  public static final String VIETNAMESE_PYTORCH_PATH = "data/models/vietnamese-sbert-pytorch";
  
  public static final String ENGLISH_ONNX_PATH = "data/models/bge-m3";
  public static final String ENGLISH_PYTORCH_PATH = "data/models/bge-m3-pytorch";
  
  /**
   * Get model path based on engine type.
   */
  public static String getVietnameseModelPath() {
    return VIETNAMESE_ENGINE == EngineType.PYTORCH 
        ? VIETNAMESE_PYTORCH_PATH 
        : VIETNAMESE_ONNX_PATH;
  }
  
  public static String getEnglishModelPath() {
    return ENGLISH_ENGINE == EngineType.PYTORCH 
        ? ENGLISH_PYTORCH_PATH 
        : ENGLISH_ONNX_PATH;
  }
}
```

**Purpose:** Config để switch giữa ONNX và PyTorch

**Changes needed:** Create new file

---

### 2. VietnamesePyTorchService.java (NEW)

**Location:** `src/main/java/com/noteflix/pcm/rag/embedding/core/VietnamesePyTorchService.java`

```java
package com.noteflix.pcm.rag.embedding.core;

import ai.djl.ModelException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vietnamese embedding service using PyTorch engine.
 * 
 * Wrapper around PyTorchEmbeddingService with Vietnamese-specific settings.
 */
public class VietnamesePyTorchService extends PyTorchEmbeddingService {
  
  private static final Logger log = LoggerFactory.getLogger(VietnamesePyTorchService.class);
  
  public VietnamesePyTorchService(String modelPath) throws IOException, ModelException {
    super(modelPath);
    log.info("✅ Vietnamese PyTorch embedding service initialized ({}d)", getDimension());
  }
}
```

**Purpose:** Vietnamese-specific wrapper (similar to VietnameseEmbeddingService for ONNX)

---

### 3. BgePyTorchService.java (NEW)

**Location:** `src/main/java/com/noteflix/pcm/rag/embedding/core/BgePyTorchService.java`

```java
package com.noteflix.pcm.rag.embedding.core;

import ai.djl.ModelException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BGE-M3 embedding service using PyTorch engine.
 * 
 * Wrapper around PyTorchEmbeddingService with BGE-specific settings.
 */
public class BgePyTorchService extends PyTorchEmbeddingService {
  
  private static final Logger log = LoggerFactory.getLogger(BgePyTorchService.class);
  
  public BgePyTorchService(String modelPath) throws IOException, ModelException {
    super(modelPath);
    log.info("✅ BGE-M3 PyTorch embedding service initialized ({}d, MTEB #1)", getDimension());
  }
}
```

**Purpose:** BGE-M3-specific wrapper

---

### 4. Update EmbeddingServiceRegistry.java

**Changes needed:** Add engine selection logic

**Before:**
```java
// Hard-coded ONNX services
EmbeddingService viService = new VietnameseEmbeddingService(
    MultiModelConfig.VIETNAMESE_MODEL_PATH
);
```

**After:**
```java
// Engine-aware service creation
EmbeddingService viService = createService(
    Language.VIETNAMESE,
    EngineConfig.VIETNAMESE_ENGINE,
    EngineConfig.getVietnameseModelPath()
);
```

**Add method:**
```java
private EmbeddingService createService(
    Language language, 
    EngineConfig.EngineType engine,
    String modelPath
) throws Exception {
  
  if (engine == EngineConfig.EngineType.PYTORCH) {
    // Use PyTorch engine
    switch (language) {
      case VIETNAMESE:
        return new VietnamesePyTorchService(modelPath);
      case ENGLISH:
        return new BgePyTorchService(modelPath);
      default:
        return new PyTorchEmbeddingService(modelPath);
    }
  } else {
    // Use ONNX engine (existing code)
    switch (language) {
      case VIETNAMESE:
        return new VietnameseEmbeddingService(modelPath);
      case ENGLISH:
        return new BgeEmbeddingService(modelPath);
      default:
        return new DJLEmbeddingService(modelPath);
    }
  }
}
```

**Lines changed:** ~50 lines (add factory method, update 3 service creation calls)

---

### 5. Update MultiModelConfig.java (Optional)

**Add constants:**
```java
// Engine configuration
public static final boolean USE_PYTORCH_FOR_VIETNAMESE = true;
public static final boolean USE_PYTORCH_FOR_ENGLISH = true;

// Model paths (engine-aware)
public static String getVietnameseModelPath() {
  return USE_PYTORCH_FOR_VIETNAMESE
      ? "data/models/vietnamese-sbert-pytorch"
      : "data/models/vietnamese-sbert";
}
```

**Lines changed:** ~20 lines

---

## 🔧 IMPLEMENTATION STEPS

### Step 1: Create New Files

```bash
# Already done!
✅ src/main/java/com/noteflix/pcm/rag/embedding/core/PyTorchEmbeddingService.java

# Need to create:
touch src/main/java/com/noteflix/pcm/rag/embedding/config/EngineConfig.java
touch src/main/java/com/noteflix/pcm/rag/embedding/core/VietnamesePyTorchService.java
touch src/main/java/com/noteflix/pcm/rag/embedding/core/BgePyTorchService.java
```

### Step 2: Update Existing Files

```bash
# Edit these files:
vim src/main/java/com/noteflix/pcm/rag/embedding/core/EmbeddingServiceRegistry.java
vim src/main/java/com/noteflix/pcm/rag/embedding/config/MultiModelConfig.java
```

### Step 3: Add PyTorch Dependencies

**Option A: Manual (offline-ready)**
```bash
# Download JARs once, save locally
mkdir -p lib/pytorch
wget -P lib/pytorch/ \
  https://repo1.maven.org/maven2/ai/djl/pytorch/pytorch-engine/0.25.0/pytorch-engine-0.25.0.jar
wget -P lib/pytorch/ \
  https://repo1.maven.org/maven2/ai/djl/pytorch/pytorch-native-cpu/2.1.1/pytorch-native-cpu-2.1.1-osx-x86_64.jar

# Update build script to include lib/pytorch/*
```

**Option B: Maven (requires internet during build)**
```xml
<dependency>
    <groupId>ai.djl.pytorch</groupId>
    <artifactId>pytorch-engine</artifactId>
    <version>0.25.0</version>
</dependency>
```

### Step 4: Rebuild

```bash
./scripts/build.sh
```

### Step 5: Test

```bash
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*:lib/pytorch/*" \
  com.noteflix.pcm.rag.examples.VietnameseEnglishEmbeddingDemo
```

---

## 📊 COMPARISON: Code Complexity

### ONNX (Current)
```
Files: 5
  - DJLEmbeddingService.java
  - VietnameseEmbeddingService.java
  - BgeEmbeddingService.java
  - EmbeddingServiceRegistry.java
  - MultiModelConfig.java

Dependencies: 
  - DJL ONNX Runtime (~20 MB)
  
Total LOC: ~800 lines
```

### ONNX + PyTorch (New)
```
Files: 9 (+4 new)
  - [All ONNX files]
  - PyTorchEmbeddingService.java          (new)
  - VietnamesePyTorchService.java        (new)
  - BgePyTorchService.java               (new)
  - EngineConfig.java                    (new)

Dependencies:
  - DJL ONNX Runtime (~20 MB)
  - DJL PyTorch Engine (~500 MB)
  
Total LOC: ~1100 lines (+300)
```

**Impact:** +4 files, +300 LOC, +480 MB dependencies

---

## ⚖️ TRADE-OFFS

### Advantages ✅

1. **Original Models Work**
   - vietnamese-sbert: ✅
   - bge-m3: ✅
   - Best quality

2. **Full Tokenizer Support**
   - vocab.txt ✅
   - bpe.codes ✅
   - All formats ✅

3. **Still 100% Offline**
   - After setup, same as ONNX
   - No Python at runtime
   - No internet at runtime

### Disadvantages ⚠️

1. **Larger Codebase**
   - +4 files
   - +300 lines
   - More maintenance

2. **Larger Dependencies**
   - +480 MB (PyTorch libs)
   - Total: ~500 MB vs ~20 MB

3. **More Complex Build**
   - Need both ONNX and PyTorch JARs
   - OS-specific native libs

---

## 🎯 MINIMAL CHANGES APPROACH

### If You Want Minimal Code Changes

**Option 1: Replace ONNX with PyTorch completely**

```java
// Just replace implementation
// In VietnameseEmbeddingService.java:

public class VietnameseEmbeddingService extends PyTorchEmbeddingService {
  public VietnameseEmbeddingService(String modelPath) 
      throws IOException, ModelException {
    super(modelPath);  // Use PyTorch instead of ONNX
  }
}
```

**Changes:** 2 lines per service (Vietnamese, English)

**Total changes:** 4 lines only!

**Trade-off:** Can't use ONNX anymore, only PyTorch

---

**Option 2: Config-based switching**

Add single config flag:

```java
// In MultiModelConfig.java
public static final boolean USE_PYTORCH = true;

// In service constructors:
if (USE_PYTORCH) {
  return new PyTorchEmbeddingService(modelPath);
} else {
  return new DJLEmbeddingService(modelPath);
}
```

**Changes:** ~20 lines total

**Trade-off:** Simple but less flexible

---

## 🚀 RECOMMENDED APPROACH

### For Production: Engine Selection Config

```
EngineConfig.java
  ├── VIETNAMESE_ENGINE = PYTORCH  ← Configure here
  ├── ENGLISH_ENGINE = PYTORCH     ← Configure here
  └── FALLBACK_ENGINE = ONNX       ← Fast fallback
  
EmbeddingServiceRegistry
  └── createService(engine, path)  ← Factory method
  
Runtime:
  → Switch engines via config
  → No code changes needed
  → Best flexibility
```

**Changes:** 4 new files + 50 lines updates = **~350 lines total**

**Benefit:** Can switch ONNX ↔ PyTorch per language with config only

---

## ✅ OFFLINE DEPLOYMENT

### Package Structure

```
pcm-desktop-offline/
├── bin/
│   └── run.sh                    # No Python!
├── lib/
│   ├── javafx/                   # JavaFX
│   ├── others/                   # Other deps
│   ├── rag/                      # DJL ONNX
│   └── pytorch/                  # DJL PyTorch ✅ Add this
│       ├── pytorch-engine.jar
│       └── pytorch-native-*.jar
├── data/
│   └── models/
│       ├── vietnamese-sbert-pytorch/  ✅
│       ├── bge-m3-pytorch/            ✅
│       └── all-MiniLM-L6-v2/          ✅ Fallback
└── out/
    └── *.class

Total size: ~2.5 GB
  - Application: ~100 MB
  - JavaFX: ~50 MB
  - ONNX Runtime: ~20 MB
  - PyTorch Engine: ~500 MB
  - Models: ~1.8 GB
```

### Runtime

```bash
# No Python needed!
# No internet needed!
# 100% offline!

./bin/run.sh
# → Loads PyTorch native libs
# → Loads models
# → Runs embedding
# → Everything local!
```

---

## 📈 EXPECTED RESULTS

### With PyTorch Engine

```
✅ Vietnamese Model
   Model: keepitreal/vietnamese-sbert
   Engine: PyTorch
   Tokenizer: vocab.txt + bpe.codes (works!)
   Dimension: 768
   Quality: ⭐⭐⭐⭐⭐ (0.85 scores)

✅ English Model
   Model: BAAI/bge-m3
   Engine: PyTorch
   Tokenizer: All formats (works!)
   Dimension: 1024
   Quality: ⭐⭐⭐⭐⭐ (0.87 scores)

✅ Runtime
   Offline: 100% ✅
   Python: Not needed ✅
   Internet: Not needed ✅
```

---

## 🎉 SUMMARY

### Question 1: Có offline hoàn toàn không?

**✅ CÓ! 100% offline sau setup!**

```
Setup (1 lần):
  - Download PyTorch libs (internet)
  - Download models (internet + Python)

Runtime (mãi mãi):
  - Java only ✅
  - Local libs ✅
  - Local models ✅
  - No Python ✅
  - No internet ✅
```

### Question 2: Có cần update source code không?

**✅ CÓ, nhưng ÍT!**

**Minimum (4 lines):**
```java
// Replace ONNX with PyTorch in existing services
extends PyTorchEmbeddingService  // Instead of DJLEmbeddingService
```

**Recommended (350 lines):**
```java
// Full engine selection support
+ EngineConfig.java              (100 lines)
+ PyTorchEmbeddingService.java   (already done!)
+ VietnamesePyTorchService.java  (50 lines)
+ BgePyTorchService.java         (50 lines)
+ Registry updates               (50 lines)
+ Config updates                 (20 lines)
```

---

## 📋 CHECKLIST

Setup:
- [ ] Download PyTorch libs (~500 MB, 1 lần)
- [ ] Download models with Python (~1 GB, 1 lần)
- [ ] Save to lib/pytorch/ and data/models/

Code:
- [ ] Create PyTorchEmbeddingService.java (✅ Done!)
- [ ] Create EngineConfig.java
- [ ] Create Vietnamese/BgePyTorchService.java
- [ ] Update EmbeddingServiceRegistry.java
- [ ] Update build script (include lib/pytorch/)

Test:
- [ ] Rebuild project
- [ ] Run demo
- [ ] Verify scores improve (0.85+ for Vietnamese)
- [ ] Verify offline works (disconnect internet)

Deploy:
- [ ] Package libs + models
- [ ] Test on clean machine (no Python, no internet)
- [ ] ✅ Should work 100% offline!

---

**Created:** November 14, 2024  
**Status:** ✅ Complete guide  
**Offline:** 100% after setup  
**Code changes:** Minimal to moderate (4-350 lines)

