#!/bin/bash

# =================================================================
# Merge Archive Script for macOS
# =================================================================
# Merges split archive parts back into single file
#
# Usage:
#   ./scripts/merge-archive.sh <base-name>
#
# Examples:
#   ./scripts/merge-archive.sh models.zip.part
#   ./scripts/merge-archive.sh libs.tar.gz.part
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
    echo -e "${RED}Usage: $0 <base-name>${NC}"
    echo ""
    echo "Examples:"
    echo "  $0 models.zip.part     # Merges models.zip.partaa, models.zip.partab, ..."
    echo "  $0 libs.tar.gz.part    # Merges libs.tar.gz.partaa, libs.tar.gz.partab, ..."
    exit 1
fi

BASE_NAME="$1"

# Find parts
PARTS=$(ls ${BASE_NAME}* 2>/dev/null | wc -l | tr -d ' ')

if [ "$PARTS" -eq 0 ]; then
    echo -e "${RED}❌ No parts found matching: ${BASE_NAME}*${NC}"
    exit 1
fi

# Remove .part from output filename
OUTPUT=$(echo "$BASE_NAME" | sed 's/.part$//')

echo -e "${BLUE}=== Merge Archive ===${NC}"
echo ""
echo "Base name: $BASE_NAME"
echo "Found parts: $PARTS"
echo "Output: $OUTPUT"
echo ""

# List parts
echo "Parts:"
ls -lh ${BASE_NAME}*
echo ""

# Merge
echo -e "${BLUE}📦 Merging parts...${NC}"

cat ${BASE_NAME}* > "$OUTPUT"

# Get output size
OUTPUT_SIZE=$(stat -f%z "$OUTPUT")
OUTPUT_SIZE_MB=$((OUTPUT_SIZE / 1024 / 1024))

echo -e "${GREEN}✅ Merge completed!${NC}"
echo ""
echo "Output file: $OUTPUT"
echo "Size: ${OUTPUT_SIZE_MB}MB"
echo ""

# Verify if it's a valid archive
echo -e "${BLUE}🔍 Verifying archive...${NC}"

if file "$OUTPUT" | grep -q "Zip archive"; then
    echo -e "${GREEN}✓ Valid ZIP archive${NC}"
    unzip -t "$OUTPUT" > /dev/null 2>&1 && echo -e "${GREEN}✓ ZIP integrity OK${NC}" || echo -e "${YELLOW}⚠ ZIP integrity check failed${NC}"
elif file "$OUTPUT" | grep -q "gzip compressed"; then
    echo -e "${GREEN}✓ Valid GZIP archive${NC}"
    gzip -t "$OUTPUT" 2>/dev/null && echo -e "${GREEN}✓ GZIP integrity OK${NC}" || echo -e "${YELLOW}⚠ GZIP integrity check failed${NC}"
elif file "$OUTPUT" | grep -q "tar archive"; then
    echo -e "${GREEN}✓ Valid TAR archive${NC}"
fi

echo ""
echo -e "${GREEN}Done! You can now use: $OUTPUT${NC}"

