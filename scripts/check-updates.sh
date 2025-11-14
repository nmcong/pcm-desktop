#!/bin/bash
# =================================================================
# PCM Desktop - Check for Library Updates (Unix/Linux/Mac)
# =================================================================

cd "$(dirname "$0")/.."

echo
echo "========================================"
echo "   Check for Library Updates"
echo "========================================"
echo

# Check for Python
if ! command -v python3 &> /dev/null; then
    echo "[ERROR] Python 3 not found!"
    echo "Please install Python 3.x"
    exit 1
fi

# Run Python script to check updates
python3 "$(dirname "$0")/check_updates.py"

if [ $? -ne 0 ]; then
    echo
    echo "[ERROR] Update check failed!"
    exit 1
fi

echo

