# Offline Deployment Guide - 100% No Python Required

> 🔒 **Deploy without Python or Internet on production servers**

---

## 🎯 Overview

### Two-Stage Approach

```
┌────────────────────────────────────────────────────────────┐
│                    DEPLOYMENT STRATEGY                      │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  STAGE 1: Preparation (Development Machine)                │
│  ──────────────────────────────────────────────────────────│
│  • Has Python + Internet                                   │
│  • Download models                                          │
│  • Convert to ONNX                                          │
│  • Package for deployment                                   │
│                                                             │
│  STAGE 2: Production (Target Server)                       │
│  ──────────────────────────────────────────────────────────│
│  • NO Python needed                                         │
│  • NO Internet needed                                       │
│  • Just Java + ONNX files                                   │
│  • 100% offline operation                                   │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

---

## 📦 Option 1: Pre-Download Models (Recommended)

### On Development Machine (With Python + Internet)

#### Step 1: Download & Convert Models

```bash
cd /path/to/pcm-desktop

# Download all models
./scripts/setup-multilingual-embeddings.sh

# Verify downloads
ls -lh data/models/

# Expected:
# data/models/vietnamese-sbert/   (~140 MB)
# data/models/bge-m3/              (~560 MB)
# data/models/all-MiniLM-L6-v2/    (~90 MB)
```

#### Step 2: Package Models

```bash
# Create deployment package
cd data/
tar -czf models-package.tar.gz models/

# OR zip format
zip -r models-package.zip models/

# Result: models-package.tar.gz (~800 MB compressed)
```

#### Step 3: Verify Package

```bash
# Check package size
ls -lh models-package.tar.gz

# Should be ~600-700 MB (compressed from ~800 MB)
```

---

### On Production Server (NO Python, NO Internet)

#### Step 1: Copy Package

**Option A: USB/Physical Media**
```bash
# Copy to USB
cp models-package.tar.gz /Volumes/USB/

# Then on production server
cp /media/usb/models-package.tar.gz /path/to/pcm-desktop/
```

**Option B: SCP (if network available)**
```bash
# From dev machine
scp models-package.tar.gz production-server:/path/to/pcm-desktop/

# Or rsync
rsync -avz data/models/ production-server:/path/to/pcm-desktop/data/models/
```

**Option C: Network Share**
```bash
# Copy to shared drive
cp models-package.tar.gz /mnt/shared/

# Access from production server
cp /mnt/shared/models-package.tar.gz /path/to/pcm-desktop/
```

#### Step 2: Extract on Production

```bash
cd /path/to/pcm-desktop

# Extract
tar -xzf models-package.tar.gz

# OR unzip
unzip models-package.zip

# Verify
ls -la data/models/
# Should see:
# - vietnamese-sbert/
# - bge-m3/
# - all-MiniLM-L6-v2/
```

#### Step 3: Run (No Python Needed!)

```bash
# Build Java application
./scripts/build.sh

# Run with ONLY Java
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*" \
  com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample

# ✅ Works 100% offline
# ✅ No Python required
# ✅ No internet required
```

---

## 📦 Option 2: Docker Image (Fully Contained)

### Build on Development Machine

```dockerfile
# Dockerfile.embeddings
FROM python:3.11-slim AS builder

# Install dependencies
RUN pip install --no-cache-dir \
    optimum \
    optimum[onnxruntime] \
    onnxruntime \
    transformers \
    torch

# Copy scripts
COPY scripts/ /app/scripts/
WORKDIR /app

# Download models
RUN ./scripts/setup-multilingual-embeddings.sh

# ────────────────────────────────────────
# Stage 2: Runtime (NO Python)
FROM openjdk:21-slim

# Copy ONLY models (no Python)
COPY --from=builder /app/data/models /app/data/models

# Copy Java application
COPY out/ /app/out/
COPY lib/ /app/lib/

WORKDIR /app

# Run
CMD ["java", "-cp", "out:lib/*", \
     "com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample"]
```

### Build & Export

```bash
# Build image
docker build -f Dockerfile.embeddings -t pcm-embeddings .

# Save image to file
docker save pcm-embeddings -o pcm-embeddings.tar

# Compress
gzip pcm-embeddings.tar
# Result: pcm-embeddings.tar.gz (~1.5 GB)
```

### Deploy on Production

```bash
# Load image (no internet needed)
docker load -i pcm-embeddings.tar.gz

# Run
docker run pcm-embeddings

# ✅ Fully contained
# ✅ No Python on host
# ✅ No internet needed
```

---

## 📦 Option 3: Manual Download (Advanced)

### Download ONNX Files Directly

If you want to download models **without Python tools**:

#### Vietnamese Model

```bash
# Create directory
mkdir -p data/models/vietnamese-sbert

# Download files from HuggingFace (using curl/wget)
cd data/models/vietnamese-sbert

# Model file
wget https://huggingface.co/keepitreal/vietnamese-sbert/resolve/main/model.onnx

# Tokenizer
wget https://huggingface.co/keepitreal/vietnamese-sbert/resolve/main/tokenizer.json

# Config
wget https://huggingface.co/keepitreal/vietnamese-sbert/resolve/main/config.json

# Special tokens
wget https://huggingface.co/keepitreal/vietnamese-sbert/resolve/main/special_tokens_map.json

# Tokenizer config
wget https://huggingface.co/keepitreal/vietnamese-sbert/resolve/main/tokenizer_config.json
```

#### English Model (BGE-M3)

```bash
mkdir -p data/models/bge-m3
cd data/models/bge-m3

# Download all required files
wget https://huggingface.co/BAAI/bge-m3/resolve/main/model.onnx
wget https://huggingface.co/BAAI/bge-m3/resolve/main/tokenizer.json
wget https://huggingface.co/BAAI/bge-m3/resolve/main/config.json
# ... (other files)
```

**⚠️ Warning:** This approach requires:
- Knowing exact file URLs
- Manual ONNX conversion if not available
- More error-prone

**Recommendation:** Use Option 1 (pre-download package) instead.

---

## 🗂️ Pre-Built Models Package

### Directory Structure

```
data/models/
├── vietnamese-sbert/
│   ├── model.onnx              (~135 MB)
│   ├── tokenizer.json          (~2.1 MB)
│   ├── config.json             (~1 KB)
│   ├── special_tokens_map.json (~0.5 KB)
│   └── tokenizer_config.json   (~1 KB)
│
├── bge-m3/
│   ├── model.onnx              (~550 MB)
│   ├── tokenizer.json          (~2.8 MB)
│   ├── config.json             (~1.5 KB)
│   ├── special_tokens_map.json (~0.5 KB)
│   └── tokenizer_config.json   (~1 KB)
│
└── all-MiniLM-L6-v2/
    ├── model.onnx              (~85 MB)
    ├── tokenizer.json          (~1.8 MB)
    ├── config.json             (~0.8 KB)
    └── (other files)

Total: ~770 MB (all models)
Compressed: ~600 MB (tar.gz)
```

### Download Pre-Built Package

**If someone creates a pre-built package**, users can download directly:

```bash
# Download pre-built models (hypothetical)
wget https://example.com/pcm-models-v1.0.tar.gz

# Extract
tar -xzf pcm-models-v1.0.tar.gz -C /path/to/pcm-desktop/data/

# Run immediately (no Python needed!)
cd /path/to/pcm-desktop
./scripts/build.sh
./scripts/run.sh
```

---

## ✅ Verification Checklist

### On Production Server (No Python)

```bash
# 1. Check Java (REQUIRED)
java -version
# Should show: OpenJDK 21 or newer

# 2. Check models exist
ls -la data/models/vietnamese-sbert/model.onnx
ls -la data/models/bge-m3/model.onnx
ls -la data/models/all-MiniLM-L6-v2/model.onnx

# 3. Check file sizes
du -sh data/models/*
# vietnamese-sbert:  ~140 MB
# bge-m3:            ~560 MB
# all-MiniLM-L6-v2:  ~90 MB

# 4. Check Python (should NOT exist for pure offline)
which python3
# Can be "not found" - that's OK!

# 5. Test Java application
java -cp "out:lib/*" \
  com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample

# Should work WITHOUT Python!
```

---

## 🎯 Deployment Scenarios

### Scenario 1: Air-Gapped Environment

```
Environment: Completely offline, no internet ever
Solution: Option 1 (Pre-download package)

Steps:
1. Download on different machine (with internet)
2. Copy via USB/DVD
3. Extract on air-gapped server
4. Run with Java only

✅ No Python on air-gapped server
✅ No internet needed
```

---

### Scenario 2: Corporate Network (No Internet)

```
Environment: Internal network only, no external internet
Solution: Option 1 via internal file share

Steps:
1. Download on DMZ machine (with internet)
2. Copy to internal file share
3. Distribute to servers via network
4. Run with Java only

✅ No Python on production servers
✅ No external internet
```

---

### Scenario 3: Multiple Servers

```
Environment: Deploy to 10+ servers
Solution: Option 2 (Docker image)

Steps:
1. Build Docker image once
2. Save to tar file
3. Distribute tar file to all servers
4. Load and run

✅ Consistent environment
✅ Easy distribution
✅ No Python on hosts
```

---

### Scenario 4: Embedded/Edge Device

```
Environment: Limited resources, no Python
Solution: Option 1 (Pre-download) + Only essential models

Steps:
1. Download only required models (e.g., only Vietnamese)
2. Create minimal package (~140 MB vs ~800 MB)
3. Copy to device
4. Run with minimal Java

✅ Smaller footprint
✅ No Python
✅ Optimized for edge
```

---

## 📋 Quick Reference

### Development Machine (One-Time Setup)

```bash
# 1. Install Python dependencies
pip3 install optimum optimum[onnxruntime] onnxruntime transformers torch

# 2. Download models
./scripts/setup-multilingual-embeddings.sh

# 3. Package models
tar -czf models-package.tar.gz data/models/

# 4. Transfer to production
scp models-package.tar.gz production-server:/path/
```

### Production Server (Runtime)

```bash
# 1. Extract models
tar -xzf models-package.tar.gz

# 2. Build Java app
./scripts/build.sh

# 3. Run (NO Python needed!)
java -cp "out:lib/*" com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample

# ✅ Done! Fully offline, no Python!
```

---

## 🔒 Security Benefits

### Offline Deployment Advantages

1. **✅ No internet access needed**
   - Reduced attack surface
   - No data leakage risk
   - Compliance with air-gap requirements

2. **✅ No Python runtime**
   - Fewer dependencies to patch
   - Smaller attack surface
   - Simpler security audits

3. **✅ Reproducible**
   - Same models everywhere
   - No download variations
   - Version-locked

4. **✅ Fast deployment**
   - No download time
   - No conversion time
   - Instant startup

---

## ❓ FAQ

### Q: Có thực sự KHÔNG cần Python ở production không?

**A:** ✅ **Đúng!** Production KHÔNG cần Python.

```
Production server needs:
✅ Java 21+
✅ ONNX model files (data/models/)
✅ ONNX Runtime Java lib (included in lib/)

Does NOT need:
❌ Python
❌ pip
❌ PyTorch
❌ transformers library
❌ Internet connection
```

---

### Q: Model files có cần internet để load không?

**A:** ❌ **KHÔNG!** Models là local files.

```java
// Load from LOCAL disk, no internet needed
EmbeddingService service = new VietnameseEmbeddingService(
    "data/models/vietnamese-sbert"  // ← Local path
);
```

---

### Q: Có cần cài gì thêm trên production không?

**A:** ❌ **KHÔNG!** Chỉ cần:

1. Java 21+ (OpenJDK)
2. Model files (data/models/)
3. Application files (out/, lib/)

That's it! No other dependencies.

---

### Q: Nếu muốn update models thì sao?

**A:** Download trên máy khác, copy qua:

```bash
# On dev machine (with Python)
./scripts/setup-embeddings-vietnamese.sh

# Package new version
tar -czf models-v2.tar.gz data/models/

# Copy to production
scp models-v2.tar.gz production:/path/

# On production (extract & restart)
tar -xzf models-v2.tar.gz
./scripts/run.sh  # Uses new models
```

---

### Q: Docker image có chứa Python không?

**A:** ⚠️ Build stage có, Runtime stage KHÔNG:

```dockerfile
# Build stage: Has Python (for conversion)
FROM python:3.11 AS builder
RUN pip install optimum ...
RUN ./scripts/setup-multilingual-embeddings.sh

# Runtime stage: NO Python!
FROM openjdk:21-slim
COPY --from=builder /app/data/models /app/data/models
# ↑ Only copy models, not Python
```

Final image = Java + Models only!

---

## 🎉 Summary

### ✅ Deployment Confirmed

**Production Runtime:**
- ✅ **100% Offline** - No internet required
- ✅ **No Python** - Java only
- ✅ **Portable** - Copy models anywhere
- ✅ **Secure** - Air-gap compatible
- ✅ **Fast** - No download delays

**Setup Process:**
1. Download models once (dev machine with Python)
2. Package models
3. Copy to production (no Python)
4. Run with Java

**Result:** Pure Java application với pre-compiled ONNX models! 🚀

---

**Created:** November 14, 2024  
**Author:** PCM Team  
**Status:** ✅ Production-Ready for Offline Deployment

