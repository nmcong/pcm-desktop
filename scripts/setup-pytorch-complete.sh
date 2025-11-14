#!/bin/bash
# Complete PyTorch setup: Download models + libraries
# Run this after updating source code to PyTorch

set -e

echo "═══════════════════════════════════════════════════════════════"
echo "  Complete PyTorch Setup"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "This will download:"
echo "  - Vietnamese PyTorch model (~540 MB)"
echo "  - English PyTorch model (~550 MB)"
echo "  - PyTorch libraries (~500 MB)"
echo "  Total: ~1.6 GB"
echo ""
read -p "Continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 0
fi

# ═══════════════════════════════════════════════════════════════
# STEP 1: Download PyTorch Models
# ═══════════════════════════════════════════════════════════════

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  STEP 1/2: Download PyTorch Models"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 not found. Please install Python 3."
    exit 1
fi

echo "✓ Python found: $(python3 --version)"
echo ""

# Vietnamese model
echo "📥 1/2: Downloading Vietnamese model..."
echo "    Model: keepitreal/vietnamese-sbert"
echo "    Size: ~540 MB"
echo ""

python3 << 'EOF'
from transformers import AutoModel, AutoTokenizer
import os

model_name = "keepitreal/vietnamese-sbert"
output_dir = "data/models/vietnamese-sbert-pytorch"

print("  Loading model from HuggingFace...")
model = AutoModel.from_pretrained(model_name)
tokenizer = AutoTokenizer.from_pretrained(model_name)

print(f"  Saving to {output_dir}...")
os.makedirs(output_dir, exist_ok=True)
model.save_pretrained(output_dir)
tokenizer.save_pretrained(output_dir)

print("\n✅ Vietnamese model downloaded!")
print(f"   Location: {output_dir}")

# List files
print("\n   Files:")
for f in sorted(os.listdir(output_dir)):
    size = os.path.getsize(os.path.join(output_dir, f))
    print(f"     - {f} ({size:,} bytes)")
EOF

echo ""

# English model
echo "📥 2/2: Downloading English model..."
echo "    Model: BAAI/bge-m3"
echo "    Size: ~550 MB"
echo ""

python3 << 'EOF'
from transformers import AutoModel, AutoTokenizer
import os

model_name = "BAAI/bge-m3"
output_dir = "data/models/bge-m3-pytorch"

print("  Loading model from HuggingFace...")
model = AutoModel.from_pretrained(model_name)
tokenizer = AutoTokenizer.from_pretrained(model_name)

print(f"  Saving to {output_dir}...")
os.makedirs(output_dir, exist_ok=True)
model.save_pretrained(output_dir)
tokenizer.save_pretrained(output_dir)

print("\n✅ English model downloaded!")
print(f"   Location: {output_dir}")

# List files
print("\n   Files:")
for f in sorted(os.listdir(output_dir)):
    size = os.path.getsize(os.path.join(output_dir, f))
    print(f"     - {f} ({size:,} bytes)")
EOF

echo ""
echo "✅ Models downloaded successfully!"
echo ""

# ═══════════════════════════════════════════════════════════════
# STEP 2: Download PyTorch Libraries
# ═══════════════════════════════════════════════════════════════

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  STEP 2/2: Download PyTorch Libraries"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Create directory
mkdir -p lib/pytorch

# Detect OS
OS=$(uname -s)
case "$OS" in
  Darwin)
    PLATFORM="osx-x86_64"
    echo "✓ Detected OS: macOS"
    ;;
  Linux)
    PLATFORM="linux-x86_64"
    echo "✓ Detected OS: Linux"
    ;;
  *)
    echo "❌ Unsupported OS: $OS"
    exit 1
    ;;
esac

echo "  Platform: $PLATFORM"
echo ""

# Download JARs
echo "📥 Downloading PyTorch libraries..."
echo ""

# 1. PyTorch Engine
echo "  1/3: pytorch-engine-0.25.0.jar (~100 KB)"
curl -sL -o lib/pytorch/pytorch-engine-0.25.0.jar \
  https://repo1.maven.org/maven2/ai/djl/pytorch/pytorch-engine/0.25.0/pytorch-engine-0.25.0.jar
echo "  ✓ Downloaded"

# 2. PyTorch JNI
echo "  2/3: pytorch-jni-2.1.1-0.25.0.jar (~50 KB)"
curl -sL -o lib/pytorch/pytorch-jni-2.1.1-0.25.0.jar \
  https://repo1.maven.org/maven2/ai/djl/pytorch/pytorch-jni/2.1.1-0.25.0/pytorch-jni-2.1.1-0.25.0.jar
echo "  ✓ Downloaded"

# 3. PyTorch Native (large!)
echo "  3/3: pytorch-native-cpu-2.1.1-${PLATFORM}.jar (~500 MB)"
echo "      This will take a few minutes..."
curl -L --progress-bar -o "lib/pytorch/pytorch-native-cpu-2.1.1-${PLATFORM}.jar" \
  "https://repo1.maven.org/maven2/ai/djl/pytorch/pytorch-native-cpu/2.1.1/pytorch-native-cpu-2.1.1-${PLATFORM}.jar"
echo "  ✓ Downloaded"

echo ""
echo "✅ PyTorch libraries downloaded!"
echo ""

# ═══════════════════════════════════════════════════════════════
# Summary
# ═══════════════════════════════════════════════════════════════

echo "═══════════════════════════════════════════════════════════════"
echo "  ✅ Complete Setup Successful!"
echo "═══════════════════════════════════════════════════════════════"
echo ""

echo "Downloaded:"
echo ""

echo "📦 Models:"
if [ -f "data/models/vietnamese-sbert-pytorch/pytorch_model.bin" ] || \
   [ -f "data/models/vietnamese-sbert-pytorch/model.safetensors" ]; then
    echo "  ✅ Vietnamese: data/models/vietnamese-sbert-pytorch/"
else
    echo "  ⚠️  Vietnamese: Missing model file"
fi

if [ -f "data/models/bge-m3-pytorch/pytorch_model.bin" ] || \
   [ -f "data/models/bge-m3-pytorch/model.safetensors" ]; then
    echo "  ✅ English:    data/models/bge-m3-pytorch/"
else
    echo "  ⚠️  English: Missing model file"
fi

echo ""
echo "📚 Libraries (lib/pytorch/):"
ls -lh lib/pytorch/ | tail -n +2 | awk '{printf "  ✅ %-40s %10s\n", $9, $5}'

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Next Steps:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Rebuild project:"
echo "   ./scripts/build.sh"
echo ""
echo "2. Run demo with PyTorch:"
echo "   java -cp \"out:lib/javafx/*:lib/others/*:lib/rag/*:lib/pytorch/*\" \\"
echo "     com.noteflix.pcm.rag.examples.VietnameseEnglishEmbeddingDemo"
echo ""
echo "3. Look for this in output:"
echo "   📥 Loading Vietnamese model (PhoBERT-based, PyTorch engine)..."
echo "   📥 Loading English model (BGE-M3, PyTorch engine)..."
echo ""
echo "Expected results:"
echo "  Vietnamese: 0.85+ scores (vs 0.55 fallback)"
echo "  English:    0.87+ scores (vs 0.70 fallback)"
echo ""

