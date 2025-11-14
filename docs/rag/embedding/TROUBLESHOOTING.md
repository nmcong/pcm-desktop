# Troubleshooting Guide - Multi-Language Embeddings

> 🔧 Common issues and solutions for model setup

---

## 🐛 Issue #1: optimum[exporters] Not Found

### Error Message

```
WARNING: optimum 2.0.0 does not provide the extra 'exporters'
❌ Error: No module named 'optimum.onnxruntime'
```

### Root Cause

**Optimum 2.0.0** changed package structure. The `optimum[exporters]` extra is no longer available. Need to install `optimum[onnxruntime]` instead.

### Solution ✅ **FIXED**

**Scripts have been updated!** Re-run the setup:

```bash
./scripts/setup-multilingual-embeddings.sh
```

**What changed:**

```bash
# OLD (doesn't work with optimum 2.0+):
pip3 install optimum[exporters]

# NEW (works with optimum 2.0+):
pip3 install optimum
pip3 install optimum[onnxruntime]
pip3 install onnxruntime
```

### Manual Fix (if needed)

If you already started and hit this error:

```bash
# Uninstall old packages
pip3 uninstall -y optimum onnxruntime transformers torch

# Reinstall correctly
pip3 install optimum
pip3 install optimum[onnxruntime]
pip3 install onnxruntime
pip3 install transformers
pip3 install torch
pip3 install sentencepiece

# Now run the script again
./scripts/setup-multilingual-embeddings.sh
```

---

## 🐛 Issue #2: Python Not Found

### Error Message

```
❌ Python 3 is required but not installed.
```

### Solution

**macOS:**
```bash
brew install python@3.11
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install python3 python3-pip
```

**Windows:**
Download from: https://www.python.org/downloads/

---

## 🐛 Issue #3: Permission Denied

### Error Message

```
Permission denied: '/usr/local/lib/python3.x/site-packages'
```

### Solution

Use `--user` flag:

```bash
pip3 install --user optimum
pip3 install --user optimum[onnxruntime]
pip3 install --user onnxruntime
pip3 install --user transformers
pip3 install --user torch
```

Or use virtual environment (recommended):

```bash
# Create virtual environment
python3 -m venv venv

# Activate it
source venv/bin/activate  # macOS/Linux
# or
venv\Scripts\activate     # Windows

# Install packages
pip install optimum optimum[onnxruntime] onnxruntime transformers torch

# Run setup
./scripts/setup-multilingual-embeddings.sh
```

---

## 🐛 Issue #4: Out of Memory During Download

### Error Message

```
MemoryError: Unable to allocate array
```

### Solution

Download models one at a time:

```bash
# Download Vietnamese only
./scripts/setup-embeddings-vietnamese.sh

# Download English only
./scripts/setup-embeddings-english.sh
```

Or increase swap space (Linux):

```bash
# Check current swap
free -h

# Add swap file
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

---

## 🐛 Issue #5: Slow Download Speed

### Error Message

(Download stuck or very slow)

### Solution

**Option 1: Use HuggingFace mirror (China users)**

```bash
export HF_ENDPOINT=https://hf-mirror.com
./scripts/setup-multilingual-embeddings.sh
```

**Option 2: Download manually with resume support**

```bash
# Install huggingface-cli
pip3 install huggingface-hub

# Download with resume
huggingface-cli download keepitreal/vietnamese-sbert \
  --local-dir data/models/vietnamese-sbert \
  --resume-download
```

**Option 3: Use download manager**

```bash
# Install aria2
brew install aria2  # macOS
sudo apt install aria2  # Linux

# Download in parallel
# (Manual download URLs from HuggingFace)
```

---

## 🐛 Issue #6: PyTorch Security Vulnerability (CVE-2025-32434)

### Error Message

```
❌ Error: Due to a serious vulnerability issue in `torch.load`, 
even with `weights_only=True`, we now require users to upgrade 
torch to at least v2.6 in order to use the function.
```

### Root Cause

**Security vulnerability** in PyTorch < 2.6. The conversion script requires torch 2.6+ for safe model loading.

### Solution ✅ **FIXED**

**Scripts have been updated!** Re-run the setup:

```bash
# Uninstall old torch
pip3 uninstall -y torch

# Re-run setup (will install torch 2.6+)
./scripts/setup-multilingual-embeddings.sh
```

### Manual Fix

```bash
# Option 1: Upgrade torch to 2.6+
pip3 install --upgrade "torch>=2.6.0"

# Then re-run conversion
./scripts/setup-multilingual-embeddings.sh
```

**Alternative (if torch 2.6+ not available):**

The model already downloaded `model.safetensors` (safer format). The conversion should use safetensors automatically.

### Verification

```bash
# Check torch version
pip3 show torch | grep Version
# Should show: Version: 2.6.0 or higher

# Check if safetensors installed
pip3 show safetensors
```

---

## 🐛 Issue #7: torch Installation Failed

### Error Message

```
ERROR: Could not find a version that satisfies the requirement torch
```

### Solution

**macOS (Apple Silicon):**
```bash
# Use conda instead
conda install pytorch::pytorch
```

Or specify platform:
```bash
pip3 install torch --index-url https://download.pytorch.org/whl/cpu
```

**Linux:**
```bash
# CPU version
pip3 install torch --index-url https://download.pytorch.org/whl/cpu

# GPU version (CUDA 11.8)
pip3 install torch --index-url https://download.pytorch.org/whl/cu118
```

---

## 🐛 Issue #7: Model Not Found After Download

### Error Message

```
IOException: Model not found at specified path
```

### Solution

Check directory structure:

```bash
ls -la data/models/vietnamese-sbert/
# Should contain:
# - model.onnx
# - tokenizer.json
# - config.json
# - (other files)
```

If files are missing, re-download:

```bash
# Remove incomplete download
rm -rf data/models/vietnamese-sbert/

# Download again
./scripts/setup-embeddings-vietnamese.sh
```

---

## 🐛 Issue #8: Java OutOfMemoryError

### Error Message

```
java.lang.OutOfMemoryError: Java heap space
```

### Solution

**Increase heap size:**

```bash
# Option 1: Environment variable
export JAVA_OPTS="-Xmx4g"
./scripts/run.sh

# Option 2: Direct command
java -Xmx4g -cp "out:lib/*" \
  com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample
```

**Or load fewer models:**

```java
// Load only one model at a time
EmbeddingService viService = new VietnameseEmbeddingService(
    "data/models/vietnamese-sbert"
);
// Don't load all 3 at once
```

---

## 🐛 Issue #9: ONNX Runtime Error

### Error Message

```
OrtException: Failed to load model
```

### Solution

**Check ONNX Runtime version:**

```bash
pip3 show onnxruntime
# Should be 1.16.0 or newer
```

**Reinstall if needed:**

```bash
pip3 uninstall onnxruntime
pip3 install onnxruntime>=1.16.0
```

**Check model file:**

```bash
# Model file should exist and be valid ONNX
ls -lh data/models/vietnamese-sbert/model.onnx

# Should be ~130-150 MB
# If much smaller, re-download
```

---

## 🐛 Issue #10: Tokenizer Error

### Error Message

```
Error loading tokenizer.json
```

### Solution

**Check tokenizer file:**

```bash
ls -lh data/models/vietnamese-sbert/tokenizer.json
# Should be ~2-3 MB
```

**If missing or corrupted:**

```bash
# Re-download model
rm -rf data/models/vietnamese-sbert/
./scripts/setup-embeddings-vietnamese.sh
```

---

## 🔍 Diagnostic Commands

### Check Python Environment

```bash
python3 --version
pip3 --version
pip3 list | grep optimum
pip3 list | grep onnx
pip3 list | grep transformers
```

### Check Models

```bash
# List downloaded models
ls -la data/models/

# Check model sizes
du -sh data/models/*

# Vietnamese model should be ~140 MB
# English model should be ~560 MB
# Fallback should be ~90 MB
```

### Check Java Environment

```bash
java -version
java -XshowSettings:vm -version
```

### Test Model Loading

```bash
# Vietnamese
java -cp "out:lib/*" \
  com.noteflix.pcm.rag.embedding.core.VietnameseEmbeddingService

# English
java -cp "out:lib/*" \
  com.noteflix.pcm.rag.embedding.core.BgeEmbeddingService
```

---

## 📞 Getting Help

### Before Asking

1. ✅ Check this troubleshooting guide
2. ✅ Check error message carefully
3. ✅ Run diagnostic commands above
4. ✅ Search existing issues on GitHub

### When Asking for Help

**Include:**
- Error message (full stack trace)
- OS and version
- Python version
- Java version
- Command you ran
- Diagnostic output

**Example:**

```
OS: macOS 14.0 (Apple Silicon)
Python: 3.11.5
Java: OpenJDK 21
Command: ./scripts/setup-multilingual-embeddings.sh

Error:
optimum 2.0.0 does not provide the extra 'exporters'
No module named 'optimum.onnxruntime'

Diagnostic:
$ pip3 list | grep optimum
optimum 2.0.0

$ pip3 list | grep onnx
(empty)
```

---

## 🎓 Prevention Tips

### Use Virtual Environment

```bash
# Create venv (recommended)
python3 -m venv venv
source venv/bin/activate

# Now install packages
pip install optimum optimum[onnxruntime] onnxruntime transformers torch
```

### Keep Dependencies Updated

```bash
# Update pip first
pip3 install --upgrade pip

# Then install packages
pip3 install optimum optimum[onnxruntime]
```

### Check Disk Space

```bash
# Need at least 2 GB free
df -h .
```

### Use Stable Versions

```bash
# Pin versions if needed
pip3 install optimum==2.0.0
pip3 install onnxruntime==1.16.0
pip3 install transformers==4.35.0
```

---

## ✅ Quick Fixes Summary

| Issue | Quick Fix |
|-------|-----------|
| optimum[exporters] error | ✅ **Fixed in scripts** - re-run setup |
| Python not found | Install Python 3.8+ |
| Permission denied | Use `--user` or venv |
| Out of memory | Download one model at a time |
| Slow download | Use HF mirror or download manager |
| torch install failed | Use conda or specify platform |
| Model not found | Check directory structure |
| Java OOM | Increase heap: `-Xmx4g` |
| ONNX error | Reinstall onnxruntime |
| Tokenizer error | Re-download model |

---

## 📚 Related Documents

- [Quick Start Guide](./QUICK_START_MULTILINGUAL.md)
- [Implementation Summary](./IMPLEMENTATION_SUMMARY.md)
- [Model Recommendations](./MULTILINGUAL_MODEL_RECOMMENDATIONS.md)

---

**Last Updated:** November 14, 2024  
**Author:** PCM Team

