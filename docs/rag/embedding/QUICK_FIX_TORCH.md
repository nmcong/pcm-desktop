# 🔧 Quick Fix: PyTorch Security Issue (CVE-2025-32434)

> ⚠️ **Issue:** torch version too old, has security vulnerability  
> ✅ **Status:** Scripts updated, easy fix available

---

## 🎯 TL;DR - Quick Fix

```bash
# 1. Upgrade torch to 2.6+
pip3 install --upgrade "torch>=2.6.0" safetensors

# 2. Re-run setup
./scripts/setup-multilingual-embeddings.sh
```

**That's it!** 🎉

---

## 📋 What Happened?

### The Error

```
❌ Error: Due to a serious vulnerability issue in `torch.load`, 
even with `weights_only=True`, we now require users to upgrade 
torch to at least v2.6 in order to use the function.

See: https://nvd.nist.gov/vuln/detail/CVE-2025-32434
```

### Why?

- **PyTorch < 2.6** has security vulnerability (CVE-2025-32434)
- **HuggingFace tools** now require torch 2.6+ for safety
- Your system has **older torch version**

---

## ✅ Solution (Choose One)

### Option 1: Quick Upgrade (Recommended)

```bash
# Upgrade torch
pip3 install --upgrade "torch>=2.6.0"

# Install safetensors (safer format)
pip3 install --upgrade safetensors

# Re-run setup (models already partially downloaded)
./scripts/setup-multilingual-embeddings.sh
```

**Note:** Models already downloaded will be reused! Script continues from where it stopped.

---

### Option 2: Clean Reinstall

```bash
# Uninstall old packages
pip3 uninstall -y torch optimum onnxruntime transformers

# Re-run setup (will install torch 2.6+)
./scripts/setup-multilingual-embeddings.sh
```

**Note:** Scripts have been updated to install torch 2.6+ automatically.

---

### Option 3: Use Conda (If pip fails)

```bash
# Create conda environment
conda create -n pcm-env python=3.11
conda activate pcm-env

# Install torch 2.6+ via conda
conda install pytorch>=2.6.0 -c pytorch

# Install other packages
pip install optimum optimum[onnxruntime] onnxruntime transformers safetensors

# Run setup
./scripts/setup-multilingual-embeddings.sh
```

---

## 🔍 Verify Fix

```bash
# Check torch version
pip3 show torch | grep Version
# Should show: Version: 2.6.0 or higher ✅

# Check safetensors
pip3 show safetensors
# Should be installed ✅

# Test conversion
python3 -c "import torch; print(f'PyTorch {torch.__version__}')"
# Should print: PyTorch 2.6.0 or higher ✅
```

---

## 📊 What Changed in Scripts

### Before (Had Issue)

```bash
pip3 install torch  # ← Installs old version
```

### After (Fixed) ✅

```bash
pip3 install "torch>=2.6.0"  # ← Forces 2.6+
pip3 install safetensors     # ← Safer format
```

**All 3 scripts updated:**
- ✅ `setup-embeddings-vietnamese.sh`
- ✅ `setup-embeddings-english.sh`
- ✅ `setup-multilingual-embeddings.sh`

---

## ⏱️ Expected Timeline

```
┌─────────────────────────────────────────────────────────┐
│  Step                        Time                       │
├─────────────────────────────────────────────────────────┤
│  Upgrade torch               ~2 minutes                 │
│  Install safetensors         ~30 seconds                │
│  Re-run setup                ~5-10 minutes (downloads)  │
│  Total                       ~8-13 minutes               │
└─────────────────────────────────────────────────────────┘
```

**Note:** Models already partially downloaded will be reused!

---

## 🆘 If Torch 2.6+ Not Available

### Check PyTorch Availability

```bash
# Check available versions
pip3 index versions torch

# Should show versions including 2.6.0+
```

### If 2.6+ Not Found

**Option A: Update pip**
```bash
pip3 install --upgrade pip
pip3 install "torch>=2.6.0"
```

**Option B: Use PyTorch Index**
```bash
# CPU version
pip3 install "torch>=2.6.0" --index-url https://download.pytorch.org/whl/cpu

# CUDA version (if you have GPU)
pip3 install "torch>=2.6.0" --index-url https://download.pytorch.org/whl/cu121
```

**Option C: Install Pre-release**
```bash
pip3 install --pre torch
```

---

## 🎓 Background: CVE-2025-32434

### What is it?

**CVE-2025-32434** is a security vulnerability in PyTorch's `torch.load()` function that allows arbitrary code execution when loading malicious model files.

### Impact

- ⚠️ **Severity:** HIGH
- ⚠️ **Risk:** Remote code execution
- ⚠️ **Affected:** PyTorch < 2.6.0

### Fix

- ✅ PyTorch 2.6.0+ patches the vulnerability
- ✅ `safetensors` format is immune (doesn't use pickle)

### More Info

- **NVD:** https://nvd.nist.gov/vuln/detail/CVE-2025-32434
- **PyTorch Advisory:** https://github.com/pytorch/pytorch/security/advisories

---

## 🔐 Why Safetensors?

### Comparison

```
┌──────────────────┬────────────────┬───────────────────┐
│ Format           │ Security       │ Speed             │
├──────────────────┼────────────────┼───────────────────┤
│ pytorch_model.bin│ ⚠️  Vulnerable │ Medium            │
│                  │ (uses pickle)  │                   │
├──────────────────┼────────────────┼───────────────────┤
│ model.safetensors│ ✅ Secure      │ Fast              │
│                  │ (no pickle)    │ (zero-copy load)  │
└──────────────────┴────────────────┴───────────────────┘
```

### Benefits

- ✅ **Safe:** No code execution possible
- ✅ **Fast:** Zero-copy loading
- ✅ **Portable:** Works across frameworks
- ✅ **Future-proof:** Industry standard

**Recommendation:** Always prefer `safetensors` format!

---

## 📝 Checklist

After running fix, verify:

- [ ] torch version ≥ 2.6.0
- [ ] safetensors installed
- [ ] Models downloaded successfully
- [ ] ONNX conversion completed
- [ ] Test example runs

```bash
# Quick verification
pip3 show torch | grep Version
pip3 show safetensors
ls -la data/models/*/model.onnx
java -cp "out:lib/*" com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample
```

---

## 🚀 Next Steps

After fixing torch issue:

1. ✅ **Complete setup:**
   ```bash
   ./scripts/setup-multilingual-embeddings.sh
   ```

2. ✅ **Build project:**
   ```bash
   ./scripts/build.sh
   ```

3. ✅ **Test models:**
   ```bash
   java -cp "out:lib/*" \
     com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample
   ```

4. ✅ **Package for deployment** (if needed):
   ```bash
   ./scripts/package-models.sh
   ```

---

## 💡 Prevention

### For Future

To avoid this issue:

```bash
# Always specify minimum version
pip3 install "torch>=2.6.0"

# Keep packages updated
pip3 install --upgrade torch transformers optimum

# Use requirements.txt with versions
cat > requirements-embeddings.txt << EOF
torch>=2.6.0
safetensors>=0.4.0
optimum>=2.0.0
optimum[onnxruntime]>=2.0.0
onnxruntime>=1.16.0
transformers>=4.35.0
sentencepiece>=0.1.99
EOF

pip3 install -r requirements-embeddings.txt
```

---

## ❓ FAQ

### Q: Will this affect production servers?

**A:** ❌ **NO!** Production doesn't use Python/torch.

```
Production needs:
✅ Java + ONNX files
❌ NO Python
❌ NO torch

This issue only affects:
⚠️  Model download/conversion (one-time, dev machine)
```

---

### Q: Do I need to re-download models?

**A:** ❌ **NO!** Models already downloaded are reused.

```bash
# Script checks existing files
if [ -f "model.onnx" ]; then
    echo "Already exists, skipping"
fi
```

---

### Q: Can I skip torch and use safetensors only?

**A:** ⚠️ **Partially.** 

Conversion still needs torch, but with 2.6+ it's safe. Safetensors is the storage format, torch is the processing tool.

---

### Q: Is this a pcm-desktop bug?

**A:** ❌ **NO.** This is a PyTorch security update.

- Issue: PyTorch vulnerability (CVE-2025-32434)
- Fix: Upgrade to PyTorch 2.6+
- Impact: All projects using PyTorch, not just pcm-desktop

---

## 🎉 Summary

### Issue
```
❌ PyTorch < 2.6 has security vulnerability
❌ Conversion failed with old torch version
```

### Fix
```
✅ Upgrade to torch 2.6+
✅ Install safetensors
✅ Re-run setup
```

### Result
```
✅ Secure model conversion
✅ Safe for production use
✅ Better performance with safetensors
```

---

**Created:** November 14, 2024  
**Issue:** CVE-2025-32434  
**Status:** ✅ Fixed in scripts  
**Action:** Run `pip3 install --upgrade "torch>=2.6.0" safetensors`

