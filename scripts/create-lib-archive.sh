#!/bin/bash
# =================================================================
# PCM Desktop - Create Library Archive (Unix/Linux/Mac)
# Creates multi-part archives with max 45MB per part
# =================================================================

cd "$(dirname "$0")/.."

echo
echo "========================================"
echo "   Create Library Archive"
echo "========================================"
echo

# Check for Python
if ! command -v python3 &> /dev/null; then
    echo "[ERROR] Python 3 not found!"
    echo "Please install Python 3.x"
    exit 1
fi

# Run Python script to create archive
python3 "$(dirname "$0")/create_archive.py"

if [ $? -ne 0 ]; then
    echo
    echo "[ERROR] Archive creation failed!"
    exit 1
fi

echo
echo "[SUCCESS] Library archives created successfully!"
echo

