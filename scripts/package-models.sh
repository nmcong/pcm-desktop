#!/bin/bash
# Package models for offline deployment
# Creates a distributable archive of all models

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"
MODELS_DIR="$PROJECT_ROOT/data/models"
OUTPUT_DIR="$PROJECT_ROOT"

echo "═══════════════════════════════════════════════════════════════"
echo "   📦 Package Models for Offline Deployment"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Check if models directory exists
if [ ! -d "$MODELS_DIR" ]; then
    echo "❌ Models directory not found: $MODELS_DIR"
    echo ""
    echo "Please download models first:"
    echo "  ./scripts/setup-multilingual-embeddings.sh"
    echo ""
    exit 1
fi

# Check if models exist
echo "Checking models..."
echo ""

VIETNAMESE_EXISTS=false
ENGLISH_EXISTS=false
FALLBACK_EXISTS=false

if [ -f "$MODELS_DIR/vietnamese-sbert/model.onnx" ]; then
    VIETNAMESE_EXISTS=true
    echo "  ✅ Vietnamese model found"
else
    echo "  ⚠️  Vietnamese model not found"
fi

if [ -f "$MODELS_DIR/bge-m3/model.onnx" ]; then
    ENGLISH_EXISTS=true
    echo "  ✅ English model found"
else
    echo "  ⚠️  English model not found"
fi

if [ -f "$MODELS_DIR/all-MiniLM-L6-v2/model.onnx" ]; then
    FALLBACK_EXISTS=true
    echo "  ✅ Fallback model found"
else
    echo "  ⚠️  Fallback model not found"
fi

echo ""

# Check if at least one model exists
if [ "$VIETNAMESE_EXISTS" = false ] && [ "$ENGLISH_EXISTS" = false ] && [ "$FALLBACK_EXISTS" = false ]; then
    echo "❌ No models found!"
    echo ""
    echo "Please download models first:"
    echo "  ./scripts/setup-multilingual-embeddings.sh"
    echo ""
    exit 1
fi

# Calculate sizes
echo "Calculating sizes..."
echo ""

TOTAL_SIZE=$(du -sh "$MODELS_DIR" | awk '{print $1}')
echo "  Total size: $TOTAL_SIZE"
echo ""

# Ask for package format
echo "═══════════════════════════════════════════════════════════════"
echo "  Select package format:"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "  1. tar.gz (recommended for Unix/Linux/macOS)"
echo "  2. zip (recommended for Windows)"
echo "  3. Both"
echo ""
read -p "Choice [1-3]: " FORMAT_CHOICE

# Generate version/timestamp
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
VERSION="v1.0"
PACKAGE_NAME="pcm-models-${VERSION}-${TIMESTAMP}"

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  Creating package: $PACKAGE_NAME"
echo "═══════════════════════════════════════════════════════════════"
echo ""

cd "$PROJECT_ROOT"

# Create tar.gz
if [ "$FORMAT_CHOICE" = "1" ] || [ "$FORMAT_CHOICE" = "3" ]; then
    echo "📦 Creating tar.gz archive..."
    
    TAR_FILE="${OUTPUT_DIR}/${PACKAGE_NAME}.tar.gz"
    
    tar -czf "$TAR_FILE" \
        --exclude='.DS_Store' \
        --exclude='*.pyc' \
        --exclude='__pycache__' \
        -C "$PROJECT_ROOT" \
        data/models/
    
    if [ $? -eq 0 ]; then
        TAR_SIZE=$(du -sh "$TAR_FILE" | awk '{print $1}')
        echo "  ✅ Created: $TAR_FILE"
        echo "  📊 Size: $TAR_SIZE"
        echo ""
    else
        echo "  ❌ Failed to create tar.gz"
        exit 1
    fi
fi

# Create zip
if [ "$FORMAT_CHOICE" = "2" ] || [ "$FORMAT_CHOICE" = "3" ]; then
    echo "📦 Creating zip archive..."
    
    ZIP_FILE="${OUTPUT_DIR}/${PACKAGE_NAME}.zip"
    
    # Check if zip command exists
    if command -v zip &> /dev/null; then
        zip -r "$ZIP_FILE" data/models/ \
            -x '*.DS_Store' '*.pyc' '*__pycache__*' \
            -q
        
        if [ $? -eq 0 ]; then
            ZIP_SIZE=$(du -sh "$ZIP_FILE" | awk '{print $1}')
            echo "  ✅ Created: $ZIP_FILE"
            echo "  📊 Size: $ZIP_SIZE"
            echo ""
        else
            echo "  ❌ Failed to create zip"
        fi
    else
        echo "  ⚠️  zip command not found, skipping zip creation"
        echo "  💡 Install zip: brew install zip (macOS) or apt install zip (Linux)"
        echo ""
    fi
fi

# Create README for package
README_FILE="${OUTPUT_DIR}/${PACKAGE_NAME}-README.txt"

cat > "$README_FILE" << 'EOF'
═══════════════════════════════════════════════════════════════
  PCM Desktop - Multi-Language Embedding Models Package
═══════════════════════════════════════════════════════════════

📦 Package Contents:
  - Vietnamese Model (PhoBERT-based, 768 dim)
  - English Model (BGE-M3, 1024 dim)
  - Fallback Model (MiniLM-L6-v2, 384 dim)

🚀 Quick Deployment:

  1. Extract package:
     tar -xzf pcm-models-*.tar.gz
     # OR
     unzip pcm-models-*.zip

  2. Verify extraction:
     ls -la data/models/
     # Should see: vietnamese-sbert/, bge-m3/, all-MiniLM-L6-v2/

  3. Build & Run:
     cd pcm-desktop/
     ./scripts/build.sh
     ./scripts/run.sh

✅ Requirements:
  - Java 21 or newer
  - NO Python required
  - NO Internet required

📚 Documentation:
  See: docs/rag/embedding/OFFLINE_DEPLOYMENT_GUIDE.md

═══════════════════════════════════════════════════════════════
  Package Info:
═══════════════════════════════════════════════════════════════

EOF

# Add package-specific info
echo "Version: $VERSION" >> "$README_FILE"
echo "Created: $(date)" >> "$README_FILE"
echo "Total Size: $TOTAL_SIZE" >> "$README_FILE"
echo "" >> "$README_FILE"

echo "Models included:" >> "$README_FILE"
if [ "$VIETNAMESE_EXISTS" = true ]; then
    echo "  ✅ Vietnamese (keepitreal/vietnamese-sbert)" >> "$README_FILE"
fi
if [ "$ENGLISH_EXISTS" = true ]; then
    echo "  ✅ English (BAAI/bge-m3)" >> "$README_FILE"
fi
if [ "$FALLBACK_EXISTS" = true ]; then
    echo "  ✅ Fallback (all-MiniLM-L6-v2)" >> "$README_FILE"
fi

echo "" >> "$README_FILE"
echo "═══════════════════════════════════════════════════════════════" >> "$README_FILE"

echo "📝 Created README: $README_FILE"
echo ""

# Create checksum
echo "🔒 Creating checksums..."
echo ""

if [ "$FORMAT_CHOICE" = "1" ] || [ "$FORMAT_CHOICE" = "3" ]; then
    if [ -f "$TAR_FILE" ]; then
        CHECKSUM_FILE="${TAR_FILE}.sha256"
        shasum -a 256 "$TAR_FILE" > "$CHECKSUM_FILE"
        echo "  ✅ SHA256: $CHECKSUM_FILE"
    fi
fi

if [ "$FORMAT_CHOICE" = "2" ] || [ "$FORMAT_CHOICE" = "3" ]; then
    if [ -f "$ZIP_FILE" ]; then
        CHECKSUM_FILE="${ZIP_FILE}.sha256"
        shasum -a 256 "$ZIP_FILE" > "$CHECKSUM_FILE"
        echo "  ✅ SHA256: $CHECKSUM_FILE"
    fi
fi

echo ""

# Summary
echo "═══════════════════════════════════════════════════════════════"
echo "  ✅ Packaging Complete!"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "📦 Package files created:"
echo ""

if [ "$FORMAT_CHOICE" = "1" ] || [ "$FORMAT_CHOICE" = "3" ]; then
    if [ -f "$TAR_FILE" ]; then
        echo "  • $TAR_FILE"
        echo "    Size: $(du -sh "$TAR_FILE" | awk '{print $1}')"
    fi
fi

if [ "$FORMAT_CHOICE" = "2" ] || [ "$FORMAT_CHOICE" = "3" ]; then
    if [ -f "$ZIP_FILE" ]; then
        echo "  • $ZIP_FILE"
        echo "    Size: $(du -sh "$ZIP_FILE" | awk '{print $1}')"
    fi
fi

echo "  • $README_FILE"
echo ""

echo "═══════════════════════════════════════════════════════════════"
echo "  📤 Distribution Instructions"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "1. Transfer package to target server:"
echo "   scp ${PACKAGE_NAME}.tar.gz user@server:/path/"
echo "   # OR"
echo "   # Copy to USB/DVD/Network share"
echo ""
echo "2. On target server (NO Python needed):"
echo "   tar -xzf ${PACKAGE_NAME}.tar.gz"
echo "   cd pcm-desktop/"
echo "   ./scripts/build.sh"
echo "   ./scripts/run.sh"
echo ""
echo "3. Verify checksums (optional):"
echo "   shasum -a 256 -c ${PACKAGE_NAME}.tar.gz.sha256"
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "📚 For more info, see:"
echo "  docs/rag/embedding/OFFLINE_DEPLOYMENT_GUIDE.md"
echo ""

