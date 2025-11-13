#!/bin/bash

# =================================================================
# DJL Embedding Setup Script
# =================================================================
# Downloads DJL libraries and embedding model for offline use
#
# Usage:
#   ./scripts/setup-embeddings-djl.sh [model-name]
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

echo -e "${BLUE}=== DJL Embedding Setup ===${NC}"
echo ""
echo "Model: $MODEL_NAME"
echo "Project: $PROJECT_ROOT"
echo ""

# Create directories
echo -e "${BLUE}📁 Creating directories...${NC}"
mkdir -p lib/rag
mkdir -p data/models/$MODEL_NAME

# Download DJL libraries
echo ""
echo -e "${BLUE}📦 Downloading DJL libraries...${NC}"
cd lib/rag

# DJL API
if [ ! -f "api-0.35.0.jar" ]; then
    echo "  - Downloading DJL API..."
    curl -L -O --progress-bar https://repo1.maven.org/maven2/ai/djl/api/0.35.0/api-0.35.0.jar
    echo -e "${GREEN}  ✓ DJL API downloaded${NC}"
else
    echo -e "${YELLOW}  ✓ DJL API already exists${NC}"
fi

# DJL ONNX Runtime Engine
if [ ! -f "onnxruntime-engine-0.35.0.jar" ]; then
    echo "  - Downloading DJL ONNX Runtime Engine..."
    curl -L -O --progress-bar https://repo1.maven.org/maven2/ai/djl/onnxruntime/onnxruntime-engine/0.35.0/onnxruntime-engine-0.35.0.jar
    echo -e "${GREEN}  ✓ DJL ONNX Runtime Engine downloaded${NC}"
else
    echo -e "${YELLOW}  ✓ DJL ONNX Runtime Engine already exists${NC}"
fi

# DJL Tokenizers
if [ ! -f "tokenizers-0.35.0.jar" ]; then
    echo "  - Downloading DJL Tokenizers..."
    curl -L -O --progress-bar https://repo1.maven.org/maven2/ai/djl/huggingface/tokenizers/0.35.0/tokenizers-0.35.0.jar
    echo -e "${GREEN}  ✓ DJL Tokenizers downloaded${NC}"
else
    echo -e "${YELLOW}  ✓ DJL Tokenizers already exists${NC}"
fi

# Download embedding model
echo ""
echo -e "${BLUE}📥 Downloading embedding model: $MODEL_NAME${NC}"
cd "$PROJECT_ROOT/data/models/$MODEL_NAME"

# Model ONNX file
if [ ! -f "model.onnx" ]; then
    echo "  - Downloading model.onnx..."
    curl -L -o model.onnx --progress-bar "https://huggingface.co/sentence-transformers/$MODEL_NAME/resolve/main/onnx/model.onnx"
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
echo "  📦 DJL libraries (3 JARs)"
echo "  🤖 Model: $MODEL_NAME"
echo ""
echo "Next steps:"
echo -e "  1. Rebuild: ${BLUE}./scripts/build.sh${NC}"
echo -e "  2. Test: ${BLUE}java -cp \"out:lib/javafx/*:lib/others/*:lib/rag/*\" com.noteflix.pcm.rag.examples.DJLEmbeddingExample${NC}"
echo ""
echo "Ready for semantic search! 🚀"

