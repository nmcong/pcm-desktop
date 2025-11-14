#!/bin/bash
# English Embedding Model Setup Script (BGE-M3)

set -e

MODEL_NAME="BAAI/bge-m3"
OUTPUT_DIR="data/models/bge-m3"

echo "════════════════════════════════════════════════════════"
echo "  📥 English Embedding Model Setup (BGE-M3)"
echo "════════════════════════════════════════════════════════"
echo ""
echo "Model: ${MODEL_NAME}"
echo "Output: ${OUTPUT_DIR}"
echo "Rank: #1 on MTEB Leaderboard (Nov 2024)"
echo ""

# Check if Python is available
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is required but not installed."
    echo "   Please install Python 3.8+ and try again."
    exit 1
fi

# Check Python version
PYTHON_VERSION=$(python3 --version 2>&1 | awk '{print $2}')
echo "✓ Python ${PYTHON_VERSION} found"

# Create output directory
mkdir -p "${OUTPUT_DIR}"
echo "✓ Output directory created"

# Check if model already exists
if [ -f "${OUTPUT_DIR}/model.onnx" ]; then
    echo ""
    echo "⚠️  Model already exists at ${OUTPUT_DIR}"
    read -p "   Do you want to re-download? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "✅ Using existing model"
        exit 0
    fi
    echo "   Re-downloading model..."
fi

echo ""
echo "────────────────────────────────────────────────────────"
echo "Step 1: Installing Python dependencies..."
echo "────────────────────────────────────────────────────────"

pip3 install -q --upgrade pip
pip3 install -q optimum[exporters] onnxruntime transformers torch

echo "✅ Dependencies installed"

echo ""
echo "────────────────────────────────────────────────────────"
echo "Step 2: Downloading and converting model to ONNX..."
echo "────────────────────────────────────────────────────────"
echo ""
echo "⚠️  This is a large model (~560 MB)"
echo "⏳ Download may take 5-10 minutes depending on connection..."
echo ""

python3 << EOF
import os
import sys

try:
    from optimum.onnxruntime import ORTModelForFeatureExtraction
    from transformers import AutoTokenizer
    
    print("📥 Downloading BGE-M3 model...")
    print(f"   Model: ${MODEL_NAME}")
    print(f"   Features: Dense + Sparse + Multi-vector")
    print()
    
    # Download and convert to ONNX
    print("🔄 Converting to ONNX format...")
    print("   This may take several minutes for large models...")
    model = ORTModelForFeatureExtraction.from_pretrained(
        '${MODEL_NAME}',
        export=True
    )
    
    print("📥 Downloading tokenizer...")
    tokenizer = AutoTokenizer.from_pretrained('${MODEL_NAME}')
    
    print(f"💾 Saving to {os.path.abspath('${OUTPUT_DIR}')}...")
    model.save_pretrained('${OUTPUT_DIR}')
    tokenizer.save_pretrained('${OUTPUT_DIR}')
    
    print()
    print("✅ Model downloaded and converted successfully!")
    
    # Verify files
    print()
    print("📁 Files created:")
    total_size = 0
    for file in os.listdir('${OUTPUT_DIR}'):
        file_path = os.path.join('${OUTPUT_DIR}', file)
        if os.path.isfile(file_path):
            size = os.path.getsize(file_path)
            size_mb = size / (1024 * 1024)
            total_size += size_mb
            print(f"   - {file:40s} ({size_mb:>8.2f} MB)")
    
    print(f"   {'─' * 52}")
    print(f"   {'Total':40s} ({total_size:>8.2f} MB)")
    
except Exception as e:
    print(f"❌ Error: {e}", file=sys.stderr)
    import traceback
    traceback.print_exc()
    sys.exit(1)
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "════════════════════════════════════════════════════════"
    echo "✅ English embedding model (BGE-M3) setup complete!"
    echo "════════════════════════════════════════════════════════"
    echo ""
    echo "Model location: ${OUTPUT_DIR}"
    echo "Dimension: 1024"
    echo "Context length: 8192 tokens"
    echo "MTEB Score: 75.4 (State-of-the-art)"
    echo ""
    echo "Usage in Java:"
    echo "  EmbeddingService service = new BgeEmbeddingService("
    echo "      \"${OUTPUT_DIR}\""
    echo "  );"
    echo ""
else
    echo ""
    echo "❌ Failed to setup English model"
    echo "   Check error messages above"
    exit 1
fi

