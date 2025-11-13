#!/bin/bash

# =================================================================
# Split Archive Script for macOS
# =================================================================
# Splits large files into smaller chunks
#
# Usage:
#   ./scripts/split-archive.sh <file> [chunk-size]
#
# Examples:
#   ./scripts/split-archive.sh models.zip 50m
#   ./scripts/split-archive.sh libs.tar.gz 100m
# =================================================================

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check arguments
if [ $# -lt 1 ]; then
    echo -e "${RED}Usage: $0 <file> [chunk-size]${NC}"
    echo ""
    echo "Examples:"
    echo "  $0 models.zip 50m      # Split into 50MB chunks"
    echo "  $0 libs.tar.gz 100m    # Split into 100MB chunks"
    echo "  $0 data.zip            # Split into 100MB chunks (default)"
    exit 1
fi

FILE="$1"
CHUNK_SIZE="${2:-100m}"

# Validate file exists
if [ ! -f "$FILE" ]; then
    echo -e "${RED}❌ File not found: $FILE${NC}"
    exit 1
fi

# Get file info
FILE_SIZE=$(stat -f%z "$FILE")
FILE_SIZE_MB=$((FILE_SIZE / 1024 / 1024))

echo -e "${BLUE}=== Split Archive ===${NC}"
echo ""
echo "File: $FILE"
echo "Size: ${FILE_SIZE_MB}MB"
echo "Chunk size: $CHUNK_SIZE"
echo ""

# Extract chunk size in bytes for calculation
CHUNK_BYTES=$(echo $CHUNK_SIZE | sed 's/m/*1024*1024/; s/k/*1024/; s/g/*1024*1024*1024/' | bc)
EXPECTED_PARTS=$(($FILE_SIZE / $CHUNK_BYTES + 1))

echo -e "${YELLOW}Expected parts: ~$EXPECTED_PARTS${NC}"
echo ""

# Split file
echo -e "${BLUE}📦 Splitting file...${NC}"

split -b "$CHUNK_SIZE" "$FILE" "${FILE}.part"

# Count parts
PARTS=$(ls ${FILE}.part* 2>/dev/null | wc -l | tr -d ' ')

echo -e "${GREEN}✅ Split completed!${NC}"
echo ""
echo "Created $PARTS parts:"
ls -lh ${FILE}.part*
echo ""

# Create merge script
MERGE_SCRIPT="${FILE}.merge.sh"
echo "#!/bin/bash" > "$MERGE_SCRIPT"
echo "# Merge script for $FILE" >> "$MERGE_SCRIPT"
echo "cat ${FILE}.part* > ${FILE}.merged" >> "$MERGE_SCRIPT"
echo "echo \"Merged to: ${FILE}.merged\"" >> "$MERGE_SCRIPT"
chmod +x "$MERGE_SCRIPT"

echo -e "${GREEN}✅ Created merge script: $MERGE_SCRIPT${NC}"
echo ""
echo "To merge back:"
echo -e "  ${BLUE}./$MERGE_SCRIPT${NC}"
echo -e "  or: ${BLUE}cat ${FILE}.part* > ${FILE}.merged${NC}"

