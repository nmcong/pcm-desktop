#!/bin/bash
# Download models for PyTorch engine (no ONNX conversion needed)

set -e

echo "═══════════════════════════════════════════════════════════════"
echo "  Download Models for PyTorch Engine"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "PyTorch models don't need ONNX conversion!"
echo "Just download and use directly."
echo ""

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 not found"
    exit 1
fi

echo "✓ Python found"
echo ""

# ═══════════════════════════════════════════════════════════════
# Vietnamese Model
# ═══════════════════════════════════════════════════════════════

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  1/2: Vietnamese Model (PhoBERT-based)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Model: keepitreal/vietnamese-sbert"
echo "Format: PyTorch (pytorch_model.bin)"
echo "Tokenizer: vocab.txt + bpe.codes (fully supported!)"
echo "Output: data/models/vietnamese-sbert-pytorch"
echo ""

read -p "Download Vietnamese model? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "📥 Downloading Vietnamese model..."
    python3 << 'EOF'
from transformers import AutoModel, AutoTokenizer

model_name = "keepitreal/vietnamese-sbert"
output_dir = "data/models/vietnamese-sbert-pytorch"

print(f"  Loading model: {model_name}")
model = AutoModel.from_pretrained(model_name)
tokenizer = AutoTokenizer.from_pretrained(model_name)

print(f"  Saving to: {output_dir}")
model.save_pretrained(output_dir)
tokenizer.save_pretrained(output_dir)

import os
print("\n✅ Downloaded files:")
for f in sorted(os.listdir(output_dir)):
    size = os.path.getsize(os.path.join(output_dir, f))
    print(f"  - {f} ({size:,} bytes)")

# Check for pytorch_model.bin
if os.path.exists(f"{output_dir}/pytorch_model.bin"):
    print("\n✅ PyTorch model ready!")
elif os.path.exists(f"{output_dir}/model.safetensors"):
    print("\n✅ SafeTensors model ready!")
else:
    print("\n⚠️  Warning: No PyTorch model file found")
EOF
    echo ""
fi

# ═══════════════════════════════════════════════════════════════
# English Model
# ═══════════════════════════════════════════════════════════════

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  2/2: English Model (BGE-M3)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Model: BAAI/bge-m3"
echo "Format: SafeTensors (model.safetensors)"
echo "Tokenizer: All formats supported"
echo "Output: data/models/bge-m3-pytorch"
echo ""

read -p "Download English model? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "📥 Downloading English model..."
    python3 << 'EOF'
from transformers import AutoModel, AutoTokenizer

model_name = "BAAI/bge-m3"
output_dir = "data/models/bge-m3-pytorch"

print(f"  Loading model: {model_name}")
model = AutoModel.from_pretrained(model_name)
tokenizer = AutoTokenizer.from_pretrained(model_name)

print(f"  Saving to: {output_dir}")
model.save_pretrained(output_dir)
tokenizer.save_pretrained(output_dir)

import os
print("\n✅ Downloaded files:")
for f in sorted(os.listdir(output_dir)):
    size = os.path.getsize(os.path.join(output_dir, f))
    print(f"  - {f} ({size:,} bytes)")

print("\n✅ PyTorch model ready!")
EOF
    echo ""
fi

# ═══════════════════════════════════════════════════════════════
# Summary
# ═══════════════════════════════════════════════════════════════

echo "═══════════════════════════════════════════════════════════════"
echo "  ✅ Download Complete"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "Downloaded models:"
ls -d data/models/*-pytorch/ 2>/dev/null | while read dir; do
    if [ -f "${dir}pytorch_model.bin" ] || [ -f "${dir}model.safetensors" ]; then
        echo "  ✅ $(basename $dir)"
        if [ -f "${dir}vocab.txt" ]; then
            echo "     Tokenizer: vocab.txt (old format - PyTorch supports it!)"
        elif [ -f "${dir}tokenizer.json" ]; then
            echo "     Tokenizer: tokenizer.json (fast tokenizer)"
        fi
    fi
done
echo ""
echo "Next steps:"
echo "  1. Setup PyTorch engine: ./scripts/setup-pytorch-engine.sh"
echo "  2. Implement PyTorchEmbeddingService.java"
echo "  3. Update config to use -pytorch models"
echo "  4. Rebuild and test"
echo ""
echo "Benefits:"
echo "  ✅ No ONNX conversion needed"
echo "  ✅ Full tokenizer support (vocab.txt, bpe.codes, etc.)"
echo "  ✅ All HuggingFace models compatible"

