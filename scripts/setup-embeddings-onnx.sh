#!/bin/bash

# =================================================================
# ONNX Runtime Embedding Setup Script
# =================================================================
# Downloads ONNX Runtime and embedding model for offline use
#
# Usage:
#   ./scripts/setup-embeddings-onnx.sh [model-name]
#
# Default model: all-MiniLM-L6-v2
# =================================================================

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Change to project root
cd "$(dirname "$0")/.."
PROJECT_ROOT="$(pwd)"

# Model name
MODEL_NAME="${1:-all-MiniLM-L6-v2}"

echo -e "${BLUE}=== ONNX Runtime Embedding Setup ===${NC}"
echo ""
echo "Model: $MODEL_NAME"
echo "Project: $PROJECT_ROOT"
echo ""

# Create directories
echo -e "${BLUE}📁 Creating directories...${NC}"
mkdir -p lib/rag
mkdir -p data/models/$MODEL_NAME

# Download ONNX Runtime
echo ""
echo -e "${BLUE}📦 Downloading ONNX Runtime...${NC}"
cd lib/rag

if [ ! -f "onnxruntime-1.19.0.jar" ]; then
    echo "  - Downloading ONNX Runtime..."
    curl -L -O --progress-bar https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.19.0/onnxruntime-1.19.0.jar
    echo -e "${GREEN}  ✓ ONNX Runtime downloaded${NC}"
else
    echo -e "${YELLOW}  ✓ ONNX Runtime already exists${NC}"
fi

# Download tokenizer library (reuse DJL tokenizers)
if [ ! -f "tokenizers-0.35.0.jar" ]; then
    echo "  - Downloading Tokenizers..."
    curl -L -O --progress-bar https://repo1.maven.org/maven2/ai/djl/huggingface/tokenizers/0.35.0/tokenizers-0.35.0.jar
    echo -e "${GREEN}  ✓ Tokenizers downloaded${NC}"
else
    echo -e "${YELLOW}  ✓ Tokenizers already exists${NC}"
fi

# Download embedding model
echo ""
echo -e "${BLUE}📥 Downloading embedding model: $MODEL_NAME${NC}"
cd "$PROJECT_ROOT/data/models/$MODEL_NAME"

# Model ONNX file
if [ ! -f "model.onnx" ]; then
    echo "  - Downloading model.onnx..."
    curl -L -O --progress-bar "https://huggingface.co/sentence-transformers/$MODEL_NAME/resolve/main/model.onnx"
    echo -e "${GREEN}  ✓ model.onnx downloaded${NC}"
else
    echo -e "${YELLOW}  ✓ model.onnx already exists${NC}"
fi

# Tokenizer
if [ ! -f "tokenizer.json" ]; then
    echo "  - Downloading tokenizer.json..."
    curl -L -O --progress-bar "https://huggingface.co/sentence-transformers/$MODEL_NAME/resolve/main/tokenizer.json"
    echo -e "${GREEN}  ✓ tokenizer.json downloaded${NC}"
else
    echo -e "${YELLOW}  ✓ tokenizer.json already exists${NC}"
fi

# Config
if [ ! -f "config.json" ]; then
    echo "  - Downloading config.json..."
    curl -L -O --progress-bar "https://huggingface.co/sentence-transformers/$MODEL_NAME/resolve/main/config.json"
    echo -e "${GREEN}  ✓ config.json downloaded${NC}"
else
    echo -e "${YELLOW}  ✓ config.json already exists${NC}"
fi

cd "$PROJECT_ROOT"

# Summary
echo ""
echo -e "${GREEN}✅ Setup completed!${NC}"
echo ""
echo "Downloaded:"
echo "  📦 ONNX Runtime (1 JAR)"
echo "  📦 Tokenizers (1 JAR)"
echo "  🤖 Model: $MODEL_NAME"
echo ""
echo "Next steps:"
echo -e "  1. Rebuild: ${BLUE}./scripts/build.sh${NC}"
echo -e "  2. Test: ${BLUE}java -cp \"out:lib/javafx/*:lib/others/*:lib/rag/*\" com.noteflix.pcm.rag.examples.ONNXEmbeddingExample${NC}"
echo ""
echo "Ready for semantic search! 🚀"

