# 🔧 Quick Fix: Dependency Conflicts

> ⚠️ **Issues:** torch version conflict + optimum import error  
> ✅ **Solution:** Use fix script (1 command)

---

## 🎯 TL;DR - One Command Fix

```bash
./scripts/fix-dependencies.sh
```

**Then:**

```bash
./scripts/setup-multilingual-embeddings.sh
```

**Done!** 🎉

---

## 📋 Problems Detected

### Problem 1: Torch Version Conflict

```
ERROR: torchvision 0.20.1 requires torch==2.5.1, 
but you have torch 2.9.1 which is incompatible.

torchaudio 2.5.1 requires torch==2.5.1, 
but you have torch 2.9.1 which is incompatible.
```

**Cause:** `torchvision` and `torchaudio` installed with old torch 2.5.1, but we upgraded torch to 2.9.1.

**Impact:** ⚠️ Warning only, doesn't break model conversion (we don't use torchvision/torchaudio)

---

### Problem 2: Optimum Import Error

```
❌ Error: Could not import module 'ORTModelForFeatureExtraction'. 
Are this object's requirements defined correctly?
```

**Cause:** `optimum[onnxruntime]` not installed correctly, or cache issue.

**Impact:** ❌ **BLOCKS** model conversion

---

## ✅ Solutions

### Solution 1: Use Fix Script (Recommended) ⭐

```bash
# Run fix script
./scripts/fix-dependencies.sh

# Expected output:
# ✅ All imports successful!
# ✅ Dependencies Fixed Successfully!

# Then continue setup
./scripts/setup-multilingual-embeddings.sh
```

**What the script does:**
1. Uninstalls conflicting packages
2. Installs packages in correct order
3. Uses CPU-only torch (no torchvision/torchaudio)
4. Verifies all imports
5. Shows clear success/failure

---

### Solution 2: Manual Fix

```bash
# Step 1: Clean up
pip3 uninstall -y torch torchvision torchaudio optimum onnxruntime transformers

# Step 2: Install torch (CPU only, no vision/audio)
pip3 install "torch>=2.6.0" --index-url https://download.pytorch.org/whl/cpu

# Step 3: Install optimum correctly
pip3 install optimum
pip3 install "optimum[onnxruntime]"

# Step 4: Install other packages
pip3 install onnxruntime transformers sentencepiece safetensors

# Step 5: Verify
python3 -c "from optimum.onnxruntime import ORTModelForFeatureExtraction; print('✅ OK')"

# Step 6: Run setup
./scripts/setup-multilingual-embeddings.sh
```

---

### Solution 3: Virtual Environment (Cleanest)

```bash
# Create fresh environment
python3 -m venv venv-embeddings

# Activate
source venv-embeddings/bin/activate  # macOS/Linux
# or
venv-embeddings\Scripts\activate     # Windows

# Run fix script
./scripts/fix-dependencies.sh

# Run setup
./scripts/setup-multilingual-embeddings.sh

# Later: deactivate when done
deactivate
```

**Benefits:**
- ✅ No conflicts with system packages
- ✅ Clean slate
- ✅ Reproducible
- ✅ Easy to delete if needed

---

## 🔍 Why These Errors?

### Torch Version Conflict Explained

```
Timeline:
1. System had: torch 2.5.1 + torchvision 0.20.1 + torchaudio 2.5.1
2. Script upgraded: torch → 2.9.1
3. But didn't upgrade: torchvision, torchaudio
4. Result: Version mismatch

Why it happened:
- pip doesn't auto-upgrade dependent packages
- torchvision/torchaudio locked to torch==2.5.1
```

**Good news:** We don't need torchvision/torchaudio for embeddings!

**Solution:** Install torch without vision/audio dependencies.

---

### Optimum Import Error Explained

```
Error: Could not import module 'ORTModelForFeatureExtraction'

Possible causes:
1. optimum[onnxruntime] not installed
2. onnxruntime not found by optimum
3. Package cache corruption
4. Installation interrupted
```

**Solution:** Clean install with correct order.

---

## 📊 Dependency Tree

### What We Actually Need

```
embeddings/
├── torch (2.6+)           # Core ML framework
├── optimum                # HuggingFace optimization
│   └── onnxruntime        # ONNX Runtime integration
├── onnxruntime            # ONNX inference engine
├── transformers           # HuggingFace models
├── sentencepiece          # Tokenizer
└── safetensors            # Safe model format

NOT needed:
❌ torchvision (computer vision)
❌ torchaudio (audio processing)
❌ torchtext (text processing)
```

---

## ✅ Verification

After running fix, verify everything works:

```bash
# Check torch version
python3 -c "import torch; print(f'torch: {torch.__version__}')"
# Should show: torch: 2.6.0 or higher

# Check optimum
python3 -c "import optimum; print(f'optimum: {optimum.__version__}')"

# Check critical import
python3 -c "from optimum.onnxruntime import ORTModelForFeatureExtraction; print('✅ Import OK')"

# List all packages
pip3 list | grep -E "torch|optimum|onnx|transformers"
```

**Expected output:**
```
torch: 2.6.0 (or higher)
optimum: 2.0.0 (or higher)
✅ Import OK

torch                     2.6.0
optimum                   2.0.0
onnxruntime              1.16.3
transformers              4.35.2
sentencepiece             0.1.99
safetensors              0.4.1
```

---

## 🎓 Best Practices Going Forward

### For Clean Installations

```bash
# Option 1: Always use virtual environment
python3 -m venv venv
source venv/bin/activate
pip install <packages>

# Option 2: Use requirements.txt
cat > requirements.txt << EOF
torch>=2.6.0
optimum>=2.0.0
optimum[onnxruntime]>=2.0.0
onnxruntime>=1.16.0
transformers>=4.35.0
sentencepiece>=0.1.99
safetensors>=0.4.0
EOF

pip install -r requirements.txt
```

### To Avoid Conflicts

```bash
# Don't install:
❌ pip install torch torchvision torchaudio  # Installs all 3

# Instead install only what you need:
✅ pip install torch  # Or torch[cpu]

# For embeddings, we don't need vision/audio
```

---

## 📋 Checklist

After fixing dependencies:

- [ ] `torch >= 2.6.0` installed
- [ ] `optimum` installed
- [ ] `optimum[onnxruntime]` installed
- [ ] `onnxruntime` installed
- [ ] `transformers` installed
- [ ] `sentencepiece` installed
- [ ] `safetensors` installed
- [ ] Import test passes
- [ ] No version conflicts

**Verification command:**
```bash
./scripts/fix-dependencies.sh
# Should end with: ✅ Dependencies Fixed Successfully!
```

---

## 🆘 If Fix Script Fails

### Try Virtual Environment

```bash
# Create venv
python3 -m venv venv-clean
source venv-clean/bin/activate

# Upgrade pip
pip install --upgrade pip

# Run fix
./scripts/fix-dependencies.sh

# Should work in clean environment
```

### Check Python Version

```bash
python3 --version
# Need: 3.8 or higher
# Recommended: 3.11 or 3.12

# If too old:
# macOS: brew install python@3.12
# Ubuntu: sudo apt install python3.12
```

### Check Available Disk Space

```bash
df -h .
# Need: At least 5 GB free for downloads + packages
```

---

## 🎯 Summary

### The Issues

1. ⚠️ **torch version conflict** (2.9.1 vs 2.5.1)
   - Caused by: torchvision/torchaudio locked to old version
   - Impact: Warning only, doesn't break conversion
   - Fix: Install torch without vision/audio

2. ❌ **optimum import error**
   - Caused by: Missing or broken optimum[onnxruntime]
   - Impact: Blocks model conversion
   - Fix: Clean reinstall in correct order

### The Solution

```bash
# One command:
./scripts/fix-dependencies.sh

# Then:
./scripts/setup-multilingual-embeddings.sh
```

### The Result

```
✅ Clean dependencies
✅ No conflicts
✅ All imports working
✅ Ready for model conversion
```

---

## 📚 Related Documents

- [TROUBLESHOOTING.md](docs/rag/embedding/TROUBLESHOOTING.md) - Full troubleshooting guide
- [QUICK_FIX_TORCH.md](QUICK_FIX_TORCH.md) - PyTorch security fix
- [QUICK_START_MULTILINGUAL.md](docs/rag/embedding/QUICK_START_MULTILINGUAL.md) - Setup guide

---

**Created:** November 14, 2024  
**Issues:** torch conflict + optimum import  
**Status:** ✅ Fix script available  
**Action:** Run `./scripts/fix-dependencies.sh`

