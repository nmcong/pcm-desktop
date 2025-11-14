#!/bin/bash
# Download alternative embedding models with fast tokenizer support
# These models have tokenizer.json built-in

set -e

echo "═══════════════════════════════════════════════════════════════"
echo "  Alternative Embedding Models - Fast Tokenizer Support"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 not found"
    exit 1
fi

PYTHON_VERSION=$(python3 --version 2>&1 | awk '{print $2}')
echo "✓ Python ${PYTHON_VERSION} found"
echo ""

# ═══════════════════════════════════════════════════════════════
# Option 1: LaBSE (Multilingual - includes Vietnamese)
# ═══════════════════════════════════════════════════════════════

download_labse() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  Option 1: LaBSE (Multilingual, 109 languages)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Model: sentence-transformers/LaBSE"
    echo "Dimensions: 768"
    echo "Languages: Vietnamese, English, and 107 others"
    echo "Size: ~470 MB"
    echo ""
    
    read -p "Download LaBSE? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "📥 Downloading LaBSE..."
        python3 << 'EOF'
from optimum.onnxruntime import ORTModelForFeatureExtraction
from transformers import AutoTokenizer

model_name = "sentence-transformers/LaBSE"
output_dir = "data/models/labse"

print(f"  Loading model: {model_name}")
model = ORTModelForFeatureExtraction.from_pretrained(
    model_name, 
    export=True,
    provider="CPUExecutionProvider"
)

print(f"  Loading tokenizer...")
tokenizer = AutoTokenizer.from_pretrained(model_name)

print(f"  Saving to: {output_dir}")
model.save_pretrained(output_dir)
tokenizer.save_pretrained(output_dir)

import os
if os.path.exists(f"{output_dir}/tokenizer.json"):
    print("✅ LaBSE downloaded with fast tokenizer!")
else:
    print("⚠️  No fast tokenizer found")
EOF
        echo ""
    fi
}

# ═══════════════════════════════════════════════════════════════
# Option 2: Multilingual E5 Base
# ═══════════════════════════════════════════════════════════════

download_e5_multilingual() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  Option 2: Multilingual E5 Base"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Model: intfloat/multilingual-e5-base"
    echo "Dimensions: 768"
    echo "Languages: 100+ including Vietnamese"
    echo "Size: ~560 MB"
    echo ""
    
    read -p "Download Multilingual E5? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "📥 Downloading Multilingual E5..."
        python3 << 'EOF'
from optimum.onnxruntime import ORTModelForFeatureExtraction
from transformers import AutoTokenizer

model_name = "intfloat/multilingual-e5-base"
output_dir = "data/models/multilingual-e5-base"

print(f"  Loading model: {model_name}")
model = ORTModelForFeatureExtraction.from_pretrained(
    model_name, 
    export=True,
    provider="CPUExecutionProvider"
)

print(f"  Loading tokenizer...")
tokenizer = AutoTokenizer.from_pretrained(model_name)

print(f"  Saving to: {output_dir}")
model.save_pretrained(output_dir)
tokenizer.save_pretrained(output_dir)

import os
if os.path.exists(f"{output_dir}/tokenizer.json"):
    print("✅ Multilingual E5 downloaded with fast tokenizer!")
else:
    print("⚠️  No fast tokenizer found")
EOF
        echo ""
    fi
}

# ═══════════════════════════════════════════════════════════════
# Option 3: MPNet (English only, SOTA)
# ═══════════════════════════════════════════════════════════════

download_mpnet() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  Option 3: MPNet (English only, high quality)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Model: sentence-transformers/all-mpnet-base-v2"
    echo "Dimensions: 768"
    echo "Languages: English only"
    echo "Size: ~420 MB"
    echo ""
    
    read -p "Download MPNet? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "📥 Downloading MPNet..."
        python3 << 'EOF'
from optimum.onnxruntime import ORTModelForFeatureExtraction
from transformers import AutoTokenizer

model_name = "sentence-transformers/all-mpnet-base-v2"
output_dir = "data/models/all-mpnet-base-v2"

print(f"  Loading model: {model_name}")
model = ORTModelForFeatureExtraction.from_pretrained(
    model_name, 
    export=True,
    provider="CPUExecutionProvider"
)

print(f"  Loading tokenizer...")
tokenizer = AutoTokenizer.from_pretrained(model_name)

print(f"  Saving to: {output_dir}")
model.save_pretrained(output_dir)
tokenizer.save_pretrained(output_dir)

import os
if os.path.exists(f"{output_dir}/tokenizer.json"):
    print("✅ MPNet downloaded with fast tokenizer!")
else:
    print("⚠️  No fast tokenizer found")
EOF
        echo ""
    fi
}

# ═══════════════════════════════════════════════════════════════
# Option 4: Paraphrase Multilingual MiniLM
# ═══════════════════════════════════════════════════════════════

download_paraphrase_multilingual() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  Option 4: Paraphrase Multilingual MiniLM (Compact)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Model: sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    echo "Dimensions: 384"
    echo "Languages: 50+ including Vietnamese"
    echo "Size: ~420 MB"
    echo ""
    
    read -p "Download Paraphrase Multilingual? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "📥 Downloading Paraphrase Multilingual..."
        python3 << 'EOF'
from optimum.onnxruntime import ORTModelForFeatureExtraction
from transformers import AutoTokenizer

model_name = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
output_dir = "data/models/paraphrase-multilingual-minilm"

print(f"  Loading model: {model_name}")
model = ORTModelForFeatureExtraction.from_pretrained(
    model_name, 
    export=True,
    provider="CPUExecutionProvider"
)

print(f"  Loading tokenizer...")
tokenizer = AutoTokenizer.from_pretrained(model_name)

print(f"  Saving to: {output_dir}")
model.save_pretrained(output_dir)
tokenizer.save_pretrained(output_dir)

import os
if os.path.exists(f"{output_dir}/tokenizer.json"):
    print("✅ Paraphrase Multilingual downloaded with fast tokenizer!")
else:
    print("⚠️  No fast tokenizer found")
EOF
        echo ""
    fi
}

# ═══════════════════════════════════════════════════════════════
# Main Menu
# ═══════════════════════════════════════════════════════════════

echo "Available options:"
echo ""
echo "1. LaBSE (Multilingual, 768d, best for Vietnamese)"
echo "2. Multilingual E5 Base (768d, good quality)"
echo "3. MPNet (English only, 768d, SOTA)"
echo "4. Paraphrase Multilingual (384d, compact)"
echo "5. Download all"
echo "6. Exit"
echo ""
read -p "Select option (1-6): " -n 1 -r
echo
echo ""

case $REPLY in
    1)
        download_labse
        ;;
    2)
        download_e5_multilingual
        ;;
    3)
        download_mpnet
        ;;
    4)
        download_paraphrase_multilingual
        ;;
    5)
        download_labse
        download_e5_multilingual
        download_mpnet
        download_paraphrase_multilingual
        ;;
    6)
        echo "Exiting..."
        exit 0
        ;;
    *)
        echo "Invalid option"
        exit 1
        ;;
esac

echo "═══════════════════════════════════════════════════════════════"
echo "  ✅ Download Complete"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "Downloaded models:"
ls -d data/models/*/ 2>/dev/null | while read dir; do
    if [ -f "${dir}model.onnx" ] && [ -f "${dir}tokenizer.json" ]; then
        echo "  ✅ $(basename $dir)"
    elif [ -f "${dir}model.onnx" ]; then
        echo "  ⚠️  $(basename $dir) (no fast tokenizer)"
    fi
done
echo ""
echo "Next steps:"
echo "  1. Update config to use new models"
echo "  2. Rebuild: ./scripts/build.sh"
echo "  3. Test: java -cp 'out:lib/*' com.noteflix.pcm.rag.examples.VietnameseEnglishEmbeddingDemo"

