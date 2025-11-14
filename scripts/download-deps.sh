#!/bin/bash
# =================================================================
# PCM Desktop - Download Dependencies from pom.xml (Unix/Linux/Mac)
# =================================================================

cd "$(dirname "$0")/.."

echo
echo "========================================"
echo "   Download Dependencies from pom.xml"
echo "========================================"
echo

# Check for Python
if ! command -v python3 &> /dev/null; then
    echo "[ERROR] Python 3 not found!"
    echo "Please install Python 3.x"
    exit 1
fi

# Run Python script to download dependencies
python3 "$(dirname "$0")/download_deps.py"

if [ $? -ne 0 ]; then
    echo
    echo "[ERROR] Download failed!"
    exit 1
fi

echo
echo "[SUCCESS] All dependencies downloaded successfully!"
echo

