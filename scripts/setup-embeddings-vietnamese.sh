#!/bin/bash
# Vietnamese Embedding Model Setup Script

set -e

MODEL_NAME="keepitreal/vietnamese-sbert"
OUTPUT_DIR="data/models/vietnamese-sbert"

echo "════════════════════════════════════════════════════════"
echo "  📥 Vietnamese Embedding Model Setup"
echo "════════════════════════════════════════════════════════"
echo ""
echo "Model: ${MODEL_NAME}"
echo "Output: ${OUTPUT_DIR}"
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

# Install packages separately (optimum 2.0+ changed structure)
pip3 install -q optimum
pip3 install -q optimum[onnxruntime]
pip3 install -q onnxruntime
pip3 install -q transformers

# Install torch 2.6+ (security fix for CVE-2025-32434)
echo "  Installing torch 2.6+ (security update)..."
pip3 install -q "torch>=2.6.0"

pip3 install -q sentencepiece
pip3 install -q safetensors

echo "✅ Dependencies installed"

echo ""
echo "────────────────────────────────────────────────────────"
echo "Step 2: Downloading and converting model to ONNX..."
echo "────────────────────────────────────────────────────────"
echo ""
echo "⏳ This may take several minutes..."
echo ""

python3 << EOF
import os
import sys

try:
    from optimum.onnxruntime import ORTModelForFeatureExtraction
    from transformers import AutoTokenizer
    
    print("📥 Downloading Vietnamese SBERT model...")
    print(f"   Model: ${MODEL_NAME}")
    print()
    
    # Download and convert to ONNX
    print("🔄 Converting to ONNX format...")
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
    for file in os.listdir('${OUTPUT_DIR}'):
        size = os.path.getsize(os.path.join('${OUTPUT_DIR}', file))
        size_mb = size / (1024 * 1024)
        print(f"   - {file:40s} ({size_mb:>8.2f} MB)")
    
except Exception as e:
    print(f"❌ Error: {e}", file=sys.stderr)
    sys.exit(1)
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "════════════════════════════════════════════════════════"
    echo "✅ Vietnamese embedding model setup complete!"
    echo "════════════════════════════════════════════════════════"
    echo ""
    echo "Model location: ${OUTPUT_DIR}"
    echo "Dimension: 768"
    echo "Base: PhoBERT"
    echo ""
    echo "Usage in Java:"
    echo "  EmbeddingService service = new VietnameseEmbeddingService("
    echo "      \"${OUTPUT_DIR}\""
    echo "  );"
    echo ""
else
    echo ""
    echo "❌ Failed to setup Vietnamese model"
    echo "   Check error messages above"
    exit 1
fi

