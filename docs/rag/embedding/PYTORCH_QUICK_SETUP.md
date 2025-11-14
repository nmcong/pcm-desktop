# PyTorch Engine - Quick Setup Guide

> ✅ **Source code đã update!** Sẵn sàng chuyển sang PyTorch  
> 📥 **Cần:** Download models + PyTorch libraries  
> ⏱️ **Thời gian:** ~30 phút

---

## ✅ WHAT'S DONE

### Source Code Updates

1. ✅ `EngineConfig.java` - Engine selection (PYTORCH mode enabled!)
2. ✅ `VietnamesePyTorchService.java` - Vietnamese PyTorch wrapper
3. ✅ `BgePyTorchService.java` - English PyTorch wrapper  
4. ✅ `EmbeddingServiceRegistry.java` - Updated to use PyTorch services
5. ✅ `PyTorchEmbeddingService.java` - Core PyTorch implementation
6. ✅ Build successful! (892 classes)

**Current Config:**
```java
VIETNAMESE_ENGINE = PYTORCH  ✅
ENGLISH_ENGINE = PYTORCH     ✅
FALLBACK_ENGINE = ONNX       ✅
```

---

## 📥 WHAT'S NEEDED

### 1. PyTorch Models (~1 GB)

Download models trong format PyTorch (không cần convert ONNX!)

### 2. PyTorch Libraries (~500 MB)

DJL PyTorch engine + native libraries

---

## 🚀 SETUP STEP-BY-STEP

### STEP 1: Download PyTorch Models

**Option A: Dùng Script (Recommended)**

```bash
# Run setup script
./scripts/setup-pytorch-models.sh

# Sẽ download:
# - Vietnamese: keepitreal/vietnamese-sbert (~540 MB)
# - English: BAAI/bge-m3 (~550 MB)
```

**Option B: Manual Download**

```bash
# Vietnamese model
python3 << 'EOF'
from transformers import AutoModel, AutoTokenizer

model_name = "keepitreal/vietnamese-sbert"
output_dir = "data/models/vietnamese-sbert-pytorch"

print(f"📥 Downloading {model_name}...")
model = AutoModel.from_pretrained(model_name)
tokenizer = AutoTokenizer.from_pretrained(model_name)

print(f"💾 Saving to {output_dir}...")
model.save_pretrained(output_dir)
tokenizer.save_pretrained(output_dir)

print("✅ Vietnamese model ready!")
EOF

# English model
python3 << 'EOF'
from transformers import AutoModel, AutoTokenizer

model_name = "BAAI/bge-m3"
output_dir = "data/models/bge-m3-pytorch"

print(f"📥 Downloading {model_name}...")
model = AutoModel.from_pretrained(model_name)
tokenizer = AutoTokenizer.from_pretrained(model_name)

print(f"💾 Saving to {output_dir}...")
model.save_pretrained(output_dir)
tokenizer.save_pretrained(output_dir)

print("✅ English model ready!")
EOF
```

**Verify:**
```bash
# Check downloaded files
ls -la data/models/vietnamese-sbert-pytorch/
# Should have: pytorch_model.bin, config.json, vocab.txt, bpe.codes

ls -la data/models/bge-m3-pytorch/
# Should have: model.safetensors, config.json, tokenizer files
```

---

### STEP 2: Download PyTorch Libraries

**Need 3 JARs:**
1. `pytorch-engine` - DJL PyTorch integration
2. `pytorch-jni` - JNI bridge
3. `pytorch-native-cpu` - LibTorch (OS-specific)

**Download:**

```bash
# Create directory
mkdir -p lib/pytorch

# Detect OS
OS=$(uname -s)
case "$OS" in
  Darwin)
    PLATFORM="osx-x86_64"
    ;;
  Linux)
    PLATFORM="linux-x86_64"
    ;;
  *)
    echo "Unsupported OS: $OS"
    exit 1
    ;;
esac

echo "Platform: $PLATFORM"

# Download JARs
echo "📥 Downloading PyTorch libraries..."

# 1. PyTorch Engine
wget -P lib/pytorch/ \
  https://repo1.maven.org/maven2/ai/djl/pytorch/pytorch-engine/0.25.0/pytorch-engine-0.25.0.jar

# 2. PyTorch JNI
wget -P lib/pytorch/ \
  https://repo1.maven.org/maven2/ai/djl/pytorch/pytorch-jni/2.1.1-0.25.0/pytorch-jni-2.1.1-0.25.0.jar

# 3. PyTorch Native (OS-specific, ~500 MB!)
wget -P lib/pytorch/ \
  "https://repo1.maven.org/maven2/ai/djl/pytorch/pytorch-native-cpu/2.1.1/pytorch-native-cpu-2.1.1-${PLATFORM}.jar"

echo "✅ PyTorch libraries downloaded!"
```

**Verify:**
```bash
ls -lh lib/pytorch/

# Should show:
# pytorch-engine-0.25.0.jar          (~100 KB)
# pytorch-jni-2.1.1-0.25.0.jar       (~50 KB)
# pytorch-native-cpu-2.1.1-*.jar     (~500 MB)
```

---

### STEP 3: Update Build Script

Update `scripts/build.sh` to include PyTorch libraries:

```bash
# Find this line in build.sh:
CLASSPATH="lib/javafx/*:lib/others/*:lib/rag/*"

# Change to:
CLASSPATH="lib/javafx/*:lib/others/*:lib/rag/*:lib/pytorch/*"
```

Or run with explicit classpath:

```bash
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*:lib/pytorch/*" \
  com.noteflix.pcm.rag.examples.VietnameseEnglishEmbeddingDemo
```

---

### STEP 4: Test!

```bash
# Rebuild (should already be done)
./scripts/build.sh

# Run demo with PyTorch classpath
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*:lib/pytorch/*" \
  com.noteflix.pcm.rag.examples.VietnameseEnglishEmbeddingDemo
```

**Expected Output:**
```
═══════════════════════════════════════════════════════════════
  Initializing Multi-Language Embedding Service Registry
═══════════════════════════════════════════════════════════════

📥 Loading Vietnamese model (PhoBERT-based, PyTorch engine)...
🔧 Initializing PyTorch embedding service: vietnamese-sbert-pytorch
✅ PyTorch embedding service initialized: vietnamese-sbert-pytorch (768d)
✅ Vietnamese PyTorch embedding service initialized (768d, PhoBERT-based)
✅ Vietnamese model loaded successfully
   Path: data/models/vietnamese-sbert-pytorch
   Engine: PyTorch
   Dimension: 768

📥 Loading English model (BGE-M3, PyTorch engine)...
🔧 Initializing PyTorch embedding service: bge-m3-pytorch
✅ PyTorch embedding service initialized: bge-m3-pytorch (1024d)
✅ BGE-M3 PyTorch embedding service initialized (1024d, MTEB #1)
✅ English model loaded successfully
   Path: data/models/bge-m3-pytorch
   Engine: PyTorch
   Dimension: 1024

Models loaded: 3/3
  Vietnamese: ✅ (768d, PyTorch)
  English:    ✅ (1024d, PyTorch)
  Fallback:   ✅ (384d, ONNX)
```

---

## 🎯 EXPECTED RESULTS

### Quality Improvement

**Vietnamese:**
```
Before (Fallback): 0.55-0.60 scores
After (PyTorch):   0.85+ scores     ⬆️ +49%

Query: "Kiểm tra dữ liệu người dùng nhập vào"
  1. [Score: 0.85] validate dữ liệu đầu vào ✅ Perfect!
```

**English:**
```
Before (Fallback): 0.65-0.72 scores  
After (PyTorch):   0.87+ scores     ⬆️ +21%

Query: "How to secure REST APIs?"
  1. [Score: 0.87] JWT authentication ✅ Excellent!
```

---

## 📊 DISK SPACE

```
Before (ONNX Fallback only):
  Models:     80 MB
  Libraries:  20 MB
  Total:     100 MB

After (PyTorch):
  Models:    1.1 GB  (Vietnamese + English PyTorch)
  ONNX:       80 MB  (Fallback)
  Libs:      500 MB  (PyTorch engine)
  Total:     1.7 GB  (+1.6 GB)
```

---

## 🆘 TROUBLESHOOTING

### Issue: Models not found

```bash
# Check if models downloaded
ls data/models/vietnamese-sbert-pytorch/pytorch_model.bin
ls data/models/bge-m3-pytorch/model.safetensors

# If missing, re-run download
./scripts/setup-pytorch-models.sh
```

### Issue: PyTorch libraries not found

```bash
# Check JARs
ls -lh lib/pytorch/

# Should have 3 files totaling ~500 MB
# If missing, re-download (see STEP 2)
```

### Issue: ClassNotFoundException

```bash
# Make sure classpath includes lib/pytorch/*
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*:lib/pytorch/*" ...

# Not: lib/rag/* only
```

### Issue: UnsatisfiedLinkError (Native library)

```bash
# Wrong OS-specific JAR downloaded
# Check your OS:
uname -s

# Download correct version:
# macOS: osx-x86_64
# Linux: linux-x86_64
# Windows: win-x86_64
```

---

## ✅ VERIFICATION CHECKLIST

After setup, verify:

- [ ] Models downloaded
  ```bash
  ls data/models/vietnamese-sbert-pytorch/pytorch_model.bin
  ls data/models/bge-m3-pytorch/model.safetensors
  ```

- [ ] PyTorch libraries present
  ```bash
  ls lib/pytorch/*.jar | wc -l
  # Should output: 3
  ```

- [ ] Build includes PyTorch
  ```bash
  ./scripts/build.sh
  # Should succeed
  ```

- [ ] Demo runs
  ```bash
  java -cp "out:lib/*:lib/pytorch/*" \
    com.noteflix.pcm.rag.examples.VietnameseEnglishEmbeddingDemo
  # Should show "PyTorch engine" in logs
  ```

- [ ] Quality improved
  ```
  Vietnamese scores > 0.8
  English scores > 0.8
  ```

---

## 🎉 SUCCESS!

If you see:
```
📥 Loading Vietnamese model (PhoBERT-based, PyTorch engine)...
✅ Vietnamese model loaded successfully
   Engine: PyTorch
   
📥 Loading English model (BGE-M3, PyTorch engine)...
✅ English model loaded successfully
   Engine: PyTorch
```

**Congratulations! PyTorch engine is working!** 🎉

---

## 🔄 SWITCH BACK TO ONNX

If you want to switch back to ONNX:

```java
// Edit EngineConfig.java:
public static final EngineType VIETNAMESE_ENGINE = EngineType.ONNX;
public static final EngineType ENGLISH_ENGINE = EngineType.ONNX;

// Rebuild
./scripts/build.sh
```

---

## 📚 NEXT STEPS

1. ✅ Download models (STEP 1)
2. ✅ Download PyTorch libraries (STEP 2)
3. ✅ Update build script (STEP 3)
4. ✅ Run and test (STEP 4)
5. 🎯 Enjoy high-quality embeddings!

---

**Created:** November 14, 2024  
**Status:** ✅ Code updated, ready for PyTorch  
**Action:** Download models + libraries (Steps 1-2)

