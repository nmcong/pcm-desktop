#!/bin/bash
# =================================================================
# PCM Desktop - Create Library Archive (Unix/Linux/Mac)
# Creates multi-part archives with max 45MB per part using native zip
# =================================================================

cd "$(dirname "$0")/.."

echo
echo "========================================"
echo "   Create Library Archive"
echo "========================================"
echo

# Check if lib directory exists
if [ ! -d "lib" ]; then
    echo "[ERROR] lib directory not found!"
    echo "Please run setup.sh script first."
    exit 1
fi

# Create archives directory
mkdir -p archives

# Remove old archives
rm -f archives/pcm-libs*.zip*

echo "[INFO] Creating multi-part archive (max 45MB per part)..."
echo

# Create multi-part zip archive
# -r: recursive
# -s 45m: split into 45MB parts
# -9: maximum compression
cd lib
zip -r -s 45m -9 ../archives/pcm-libs.zip *

if [ $? -eq 0 ]; then
    echo
    echo "=================================================="
    echo "[SUCCESS] Archive created successfully!"
    echo
    echo "[INFO] Archive parts created:"
    ls -lh ../archives/pcm-libs.z* 2>/dev/null || ls -lh ../archives/pcm-libs.zip
    echo
    echo "=================================================="
else
    echo
    echo "[ERROR] Archive creation failed!"
    exit 1
fi

cd ..
