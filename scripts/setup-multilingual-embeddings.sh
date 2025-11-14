#!/bin/bash
# Multi-language Embedding Models Setup Script
# Sets up Vietnamese, English, and Fallback models

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "                                                                "
echo "     🚀 Multi-Language Embedding Models Setup                  "
echo "                                                                "
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "This script will download and setup 3 embedding models:"
echo ""
echo "  1. 🇻🇳 Vietnamese Model  (keepitreal/vietnamese-sbert)"
echo "     - Size: ~140 MB"
echo "     - Dimension: 768"
echo "     - Base: PhoBERT"
echo ""
echo "  2. 🇺🇸 English Model     (BAAI/bge-m3)"
echo "     - Size: ~560 MB"
echo "     - Dimension: 1024"
echo "     - Rank: #1 on MTEB"
echo ""
echo "  3. 🔄 Fallback Model    (all-MiniLM-L6-v2)"
echo "     - Size: ~90 MB"
echo "     - Dimension: 384"
echo "     - Universal compatibility"
echo ""
echo "Total download size: ~800 MB"
echo "Total disk space required: ~1 GB"
echo ""
read -p "Continue? (Y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Nn]$ ]]; then
    echo "Setup cancelled"
    exit 0
fi

# Check system requirements
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  ✓ Checking system requirements..."
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is required but not installed."
    echo ""
    echo "Please install Python 3.8+ and try again."
    echo ""
    echo "macOS:"
    echo "  brew install python@3.11"
    echo ""
    echo "Ubuntu/Debian:"
    echo "  sudo apt update && sudo apt install python3 python3-pip"
    echo ""
    exit 1
fi

PYTHON_VERSION=$(python3 --version 2>&1 | awk '{print $2}')
echo "✓ Python ${PYTHON_VERSION} found"

# Check pip
if ! command -v pip3 &> /dev/null; then
    echo "❌ pip3 is required but not installed."
    echo "   Please install pip3 and try again."
    exit 1
fi

echo "✓ pip3 found"

# Check disk space (at least 2 GB free)
AVAILABLE_SPACE=$(df -k . | tail -1 | awk '{print $4}')
REQUIRED_SPACE=$((2 * 1024 * 1024))  # 2 GB in KB

if [ "$AVAILABLE_SPACE" -lt "$REQUIRED_SPACE" ]; then
    echo "⚠️  Warning: Low disk space"
    echo "   Available: $(($AVAILABLE_SPACE / 1024 / 1024)) GB"
    echo "   Required: 2 GB"
    read -p "   Continue anyway? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "✓ Sufficient disk space available"
fi

# Install Python dependencies
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  📦 Installing Python dependencies..."
echo "═══════════════════════════════════════════════════════════════"
echo ""

echo "Installing: optimum, onnxruntime, transformers, torch..."
pip3 install -q --upgrade pip

# Install packages separately (optimum 2.0+ changed structure)
echo "  - optimum (with onnxruntime support)"
pip3 install -q optimum
pip3 install -q optimum[onnxruntime]

echo "  - onnxruntime"
pip3 install -q onnxruntime

echo "  - transformers"
pip3 install -q transformers

echo "  - torch (v2.6+ for security)"
pip3 install -q "torch>=2.6.0"

echo "  - sentencepiece"
pip3 install -q sentencepiece

echo "  - safetensors"
pip3 install -q safetensors

if [ $? -eq 0 ]; then
    echo "✅ Python dependencies installed successfully"
else
    echo "❌ Failed to install Python dependencies"
    exit 1
fi

# Create models directory
mkdir -p data/models

# Download Vietnamese model
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  1/3: Vietnamese Model (PhoBERT-based)"
echo "═══════════════════════════════════════════════════════════════"
echo ""

if [ -f "${SCRIPT_DIR}/setup-embeddings-vietnamese.sh" ]; then
    bash "${SCRIPT_DIR}/setup-embeddings-vietnamese.sh"
    VIETNAMESE_STATUS=$?
else
    echo "❌ setup-embeddings-vietnamese.sh not found"
    VIETNAMESE_STATUS=1
fi

# Download English model
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  2/3: English Model (BGE-M3)"
echo "═══════════════════════════════════════════════════════════════"
echo ""

if [ -f "${SCRIPT_DIR}/setup-embeddings-english.sh" ]; then
    bash "${SCRIPT_DIR}/setup-embeddings-english.sh"
    ENGLISH_STATUS=$?
else
    echo "❌ setup-embeddings-english.sh not found"
    ENGLISH_STATUS=1
fi

# Check fallback model
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  3/3: Fallback Model (MiniLM-L6-v2)"
echo "═══════════════════════════════════════════════════════════════"
echo ""

if [ -d "data/models/all-MiniLM-L6-v2" ]; then
    echo "✅ Fallback model already exists"
    FALLBACK_STATUS=0
else
    echo "⚠️  Fallback model not found"
    echo "   Downloading all-MiniLM-L6-v2..."
    
    if [ -f "${SCRIPT_DIR}/setup-embeddings-djl.sh" ]; then
        bash "${SCRIPT_DIR}/setup-embeddings-djl.sh" all-MiniLM-L6-v2
        FALLBACK_STATUS=$?
    else
        echo "❌ setup-embeddings-djl.sh not found"
        FALLBACK_STATUS=1
    fi
fi

# Summary
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "                     📊 SETUP SUMMARY"
echo "═══════════════════════════════════════════════════════════════"
echo ""

if [ $VIETNAMESE_STATUS -eq 0 ]; then
    echo "✅ Vietnamese Model   : SUCCESS"
else
    echo "❌ Vietnamese Model   : FAILED"
fi

if [ $ENGLISH_STATUS -eq 0 ]; then
    echo "✅ English Model      : SUCCESS"
else
    echo "❌ English Model      : FAILED"
fi

if [ $FALLBACK_STATUS -eq 0 ]; then
    echo "✅ Fallback Model     : SUCCESS"
else
    echo "❌ Fallback Model     : FAILED"
fi

echo ""

# Check if at least one model is available
if [ $VIETNAMESE_STATUS -eq 0 ] || [ $ENGLISH_STATUS -eq 0 ] || [ $FALLBACK_STATUS -eq 0 ]; then
    echo "═══════════════════════════════════════════════════════════════"
    echo "✅ Setup completed with at least one model available!"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    echo "📁 Model locations:"
    echo ""
    
    if [ $VIETNAMESE_STATUS -eq 0 ]; then
        echo "  Vietnamese: data/models/vietnamese-sbert/ (768 dim)"
    fi
    
    if [ $ENGLISH_STATUS -eq 0 ]; then
        echo "  English:    data/models/bge-m3/ (1024 dim)"
    fi
    
    if [ $FALLBACK_STATUS -eq 0 ]; then
        echo "  Fallback:   data/models/all-MiniLM-L6-v2/ (384 dim)"
    fi
    
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "                     📚 NEXT STEPS"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    echo "1. Build the project:"
    echo "   ./scripts/build.sh"
    echo ""
    echo "2. View implementation guide:"
    echo "   cat docs/rag/embedding/MULTILINGUAL_MODEL_RECOMMENDATIONS.md"
    echo ""
    echo "3. Test the models:"
    echo "   java -cp \"out:lib/*\" \\"
    echo "     com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample"
    echo ""
    echo "4. Integration checklist:"
    echo "   - Update RAG pipeline to use EmbeddingServiceRegistry"
    echo "   - Add Language field to documents"
    echo "   - Configure model selection strategy"
    echo "   - Test with Vietnamese and English content"
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    
    exit 0
else
    echo "═══════════════════════════════════════════════════════════════"
    echo "❌ Setup failed - no models available"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    echo "Please check error messages above and try again."
    echo ""
    echo "Common issues:"
    echo "  - Network connection problems"
    echo "  - Insufficient disk space"
    echo "  - Python dependencies not installed correctly"
    echo ""
    echo "For help, see:"
    echo "  docs/rag/embedding/MULTILINGUAL_MODEL_RECOMMENDATIONS.md"
    echo ""
    exit 1
fi

