#!/bin/bash
# =================================================================
# PCM Desktop - Create Archives (Unix/Linux/Mac)
# Creates separate multi-part archives for libraries and source code
# Uses copy-then-zip approach for better structure
# =================================================================

cd "$(dirname "$0")/.."

echo
echo "========================================"
echo "   Create PCM Desktop Archives"
echo "========================================"
echo

# Check if lib directory exists
if [ ! -d "lib" ]; then
    echo "[ERROR] lib directory not found!"
    echo "Please run setup.sh script first."
    exit 1
fi

# Clean and create archives directory structure
echo "[INFO] Preparing archive directories..."
rm -rf archives/pcm-libs archives/pcm-source archives/pcm-data archives/pcm-bin archives/*.zip archives/*.z*
mkdir -p archives/pcm-libs archives/pcm-source archives/pcm-data archives/pcm-bin
echo "[OK] Archive directories prepared"
echo

# =================================================================
# Copy Library Files (excluding JavaFX)
# =================================================================
echo "[INFO] Copying library files (excluding JavaFX)..."

for dir in lib/*/; do
    dirname=$(basename "$dir")
    if [ "$dirname" != "javafx" ]; then
        echo "[INFO] Copying $dirname..."
        cp -r "$dir" "archives/pcm-libs/"
    fi
done

echo "[SUCCESS] Library files copied to archives/pcm-libs/"
echo

# =================================================================
# Copy Data Models
# =================================================================
echo "[INFO] Copying data/models files..."

if [ -d "data/models" ]; then
    cp -r data/models/* archives/pcm-data/ 2>/dev/null || true
    echo "[SUCCESS] Data models copied to archives/pcm-data/"
else
    echo "[SKIP] data/models directory not found"
fi
echo

# =================================================================
# Copy Binary Files
# =================================================================
echo "[INFO] Copying bin files..."

if [ -d "bin" ]; then
    cp -r bin/* archives/pcm-bin/ 2>/dev/null || true
    echo "[SUCCESS] Binary files copied to archives/pcm-bin/"
else
    echo "[SKIP] bin directory not found"
fi
echo

# =================================================================
# Copy Source Code Files
# =================================================================
echo "[INFO] Copying source code files..."
echo "[INFO] Excluding: bin, data, examples, lib, logs, out, target, test-extract, .git, .idea, archives"

# Copy all files and directories except excluded ones
for item in *; do
    case "$item" in
        bin|data|examples|lib|logs|out|target|test-extract|archives|.git|.idea)
            echo "[SKIP] $item"
            ;;
        .*)
            # Skip hidden files/directories except specific ones we want
            case "$item" in
                .claude|.gitignore)
                    echo "[COPY] $item"
                    cp -r "$item" "archives/pcm-source/"
                    ;;
                *)
                    echo "[SKIP] $item (hidden)"
                    ;;
            esac
            ;;
        *)
            echo "[COPY] $item"
            cp -r "$item" "archives/pcm-source/"
            ;;
    esac
done

echo "[SUCCESS] Source code files copied to archives/pcm-source/"
echo

# =================================================================
# Create Library Archive
# =================================================================
echo "[INFO] Creating library archive (45MB parts)..."
cd archives
zip -r -s 45m -9 pcm-libs.zip pcm-libs

if [ $? -eq 0 ]; then
    echo "[SUCCESS] Library archive created successfully!"
    echo "[INFO] Library archive parts:"
    ls -lh pcm-libs.z* 2>/dev/null || ls -lh pcm-libs.zip
    echo
else
    echo "[ERROR] Library archive creation failed!"
    exit 1
fi

# =================================================================
# Create Source Code Archive
# =================================================================
echo "[INFO] Creating source code archive (45MB parts)..."
zip -r -s 45m -9 pcm-source.zip pcm-source

if [ $? -eq 0 ]; then
    echo "[SUCCESS] Source code archive created successfully!"
    echo "[INFO] Source archive parts:"
    ls -lh pcm-source.z* 2>/dev/null || ls -lh pcm-source.zip
    echo
else
    echo "[ERROR] Source code archive creation failed!"
    exit 1
fi

# =================================================================
# Create Data Models Archive
# =================================================================
if [ -d "pcm-data" ] && [ "$(ls -A pcm-data 2>/dev/null)" ]; then
    echo "[INFO] Creating data models archive (45MB parts)..."
    zip -r -s 45m -9 pcm-data.zip pcm-data

    if [ $? -eq 0 ]; then
        echo "[SUCCESS] Data models archive created successfully!"
        echo "[INFO] Data archive parts:"
        ls -lh pcm-data.z* 2>/dev/null || ls -lh pcm-data.zip
        echo
    else
        echo "[ERROR] Data models archive creation failed!"
        exit 1
    fi
else
    echo "[SKIP] No data models to archive"
fi

# =================================================================
# Create Binary Archive
# =================================================================
if [ -d "pcm-bin" ] && [ "$(ls -A pcm-bin 2>/dev/null)" ]; then
    echo "[INFO] Creating binary archive (45MB parts)..."
    zip -r -s 45m -9 pcm-bin.zip pcm-bin

    if [ $? -eq 0 ]; then
        echo "[SUCCESS] Binary archive created successfully!"
        echo "[INFO] Binary archive parts:"
        ls -lh pcm-bin.z* 2>/dev/null || ls -lh pcm-bin.zip
        echo
    else
        echo "[ERROR] Binary archive creation failed!"
        exit 1
    fi
else
    echo "[SKIP] No binary files to archive"
fi

cd ..

# =================================================================
# Cleanup and Summary
# =================================================================
echo "[INFO] Cleaning up temporary directories..."
rm -rf archives/pcm-libs archives/pcm-source archives/pcm-data archives/pcm-bin
echo "[OK] Cleanup completed"
echo

echo "=================================================="
echo "[SUCCESS] All archives created successfully!"
echo
echo "[INFO] Archive Summary:"
echo
echo "📚 Library Archive (excluding JavaFX):"
ls -lh archives/pcm-libs.z* 2>/dev/null || ls -lh archives/pcm-libs.zip
echo
echo "📁 Source Code Archive:"
ls -lh archives/pcm-source.z* 2>/dev/null || ls -lh archives/pcm-source.zip
echo
if ls archives/pcm-data.z* archives/pcm-data.zip >/dev/null 2>&1; then
    echo "📊 Data Models Archive:"
    ls -lh archives/pcm-data.z* 2>/dev/null || ls -lh archives/pcm-data.zip
    echo
fi
if ls archives/pcm-bin.z* archives/pcm-bin.zip >/dev/null 2>&1; then
    echo "⚙️ Binary Archive:"
    ls -lh archives/pcm-bin.z* 2>/dev/null || ls -lh archives/pcm-bin.zip
    echo
fi
echo "=================================================="
echo
echo "[INFO] To extract and setup:"
echo "  1. Extract libraries: unzip archives/pcm-libs.zip"
echo "  2. Extract source:    unzip archives/pcm-source.zip"
echo "  3. Download JavaFX:   ./scripts/setup.sh"
echo "  4. Build project:     ./scripts/build.sh"
echo
echo "[NOTE] JavaFX libraries are excluded from pcm-libs archive"
echo "       Run setup script to download JavaFX automatically"
echo