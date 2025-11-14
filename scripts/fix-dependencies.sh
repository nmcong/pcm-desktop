#!/bin/bash
# Fix Python dependencies for embedding model setup
# Resolves torch version conflicts and optimum import issues

set -e

echo "═══════════════════════════════════════════════════════════════"
echo "   🔧 Fix Python Dependencies for Embeddings"
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

# Step 1: Uninstall conflicting packages
echo "───────────────────────────────────────────────────────────────"
echo "Step 1: Removing conflicting packages..."
echo "───────────────────────────────────────────────────────────────"
echo ""

pip3 uninstall -y torch torchvision torchaudio optimum onnxruntime transformers 2>/dev/null || true

echo "✅ Old packages removed"
echo ""

# Step 2: Install packages in correct order
echo "───────────────────────────────────────────────────────────────"
echo "Step 2: Installing packages (correct order)..."
echo "───────────────────────────────────────────────────────────────"
echo ""

# Install torch first (without torchvision/torchaudio to avoid conflicts)
echo "Installing torch 2.6+ (CPU version)..."
pip3 install --no-cache-dir "torch>=2.6.0" --index-url https://download.pytorch.org/whl/cpu
echo "  ✓ torch installed"
echo ""

# Install core packages
echo "Installing optimum and dependencies..."
pip3 install --no-cache-dir optimum
pip3 install --no-cache-dir "optimum[onnxruntime]"
echo "  ✓ optimum installed"
echo ""

echo "Installing onnxruntime..."
pip3 install --no-cache-dir onnxruntime
echo "  ✓ onnxruntime installed"
echo ""

echo "Installing transformers..."
pip3 install --no-cache-dir transformers
echo "  ✓ transformers installed"
echo ""

echo "Installing supporting packages..."
pip3 install --no-cache-dir sentencepiece safetensors
echo "  ✓ supporting packages installed"
echo ""

# Step 3: Verify installation
echo "───────────────────────────────────────────────────────────────"
echo "Step 3: Verifying installation..."
echo "───────────────────────────────────────────────────────────────"
echo ""

# Test imports
python3 << 'EOF'
import sys

print("Testing imports...")
print("")

errors = []

try:
    import torch
    print(f"  ✓ torch {torch.__version__}")
except ImportError as e:
    print(f"  ✗ torch: {e}")
    errors.append("torch")

try:
    import onnxruntime
    print(f"  ✓ onnxruntime {onnxruntime.__version__}")
except ImportError as e:
    print(f"  ✗ onnxruntime: {e}")
    errors.append("onnxruntime")

try:
    import transformers
    print(f"  ✓ transformers {transformers.__version__}")
except ImportError as e:
    print(f"  ✗ transformers: {e}")
    errors.append("transformers")

try:
    import optimum
    print(f"  ✓ optimum {optimum.__version__}")
except ImportError as e:
    print(f"  ✗ optimum: {e}")
    errors.append("optimum")

try:
    from optimum.onnxruntime import ORTModelForFeatureExtraction
    print(f"  ✓ optimum.onnxruntime.ORTModelForFeatureExtraction")
except ImportError as e:
    print(f"  ✗ ORTModelForFeatureExtraction: {e}")
    errors.append("ORTModelForFeatureExtraction")

try:
    import sentencepiece
    print(f"  ✓ sentencepiece")
except ImportError as e:
    print(f"  ✗ sentencepiece: {e}")
    errors.append("sentencepiece")

try:
    import safetensors
    print(f"  ✓ safetensors")
except ImportError as e:
    print(f"  ✗ safetensors: {e}")
    errors.append("safetensors")

print("")

if errors:
    print(f"❌ {len(errors)} import(s) failed: {', '.join(errors)}")
    sys.exit(1)
else:
    print("✅ All imports successful!")
    sys.exit(0)
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "   ✅ Dependencies Fixed Successfully!"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    echo "Installed packages:"
    pip3 list | grep -E "torch|optimum|onnx|transformers|sentencepiece|safetensors"
    echo ""
    echo "───────────────────────────────────────────────────────────────"
    echo "Next steps:"
    echo "───────────────────────────────────────────────────────────────"
    echo ""
    echo "1. Run model setup:"
    echo "   ./scripts/setup-multilingual-embeddings.sh"
    echo ""
    echo "2. Or download individual models:"
    echo "   ./scripts/setup-embeddings-vietnamese.sh"
    echo "   ./scripts/setup-embeddings-english.sh"
    echo ""
else
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "   ❌ Some imports failed"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    echo "Troubleshooting:"
    echo "  1. Try using virtual environment:"
    echo "     python3 -m venv venv"
    echo "     source venv/bin/activate"
    echo "     ./scripts/fix-dependencies.sh"
    echo ""
    echo "  2. Check Python version (need 3.8+):"
    echo "     python3 --version"
    echo ""
    echo "  3. See full troubleshooting guide:"
    echo "     docs/rag/embedding/TROUBLESHOOTING.md"
    echo ""
    exit 1
fi

