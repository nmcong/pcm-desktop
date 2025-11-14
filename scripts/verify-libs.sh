#!/bin/bash

# PCM Desktop - Library Verification Script
# Verifies that all required libraries are present

# Change to project root directory (parent of scripts folder)
cd "$(dirname "$0")/.."

echo "🔍 Verifying PCM Desktop Libraries..."
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

errors=0

# Check JavaFX
echo "📦 Checking JavaFX libraries..."
javafx_libs=(
    "javafx.base.jar"
    "javafx.controls.jar"
    "javafx.fxml.jar"
    "javafx.graphics.jar"
    "javafx.media.jar"
    "javafx.swing.jar"
    "javafx.web.jar"
    "javafx-swt.jar"
)

for lib in "${javafx_libs[@]}"; do
    if [ -f "lib/javafx/$lib" ]; then
        echo -e "  ${GREEN}✓${NC} $lib"
    else
        echo -e "  ${RED}✗${NC} $lib - MISSING"
        ((errors++))
    fi
done

echo ""
echo "📦 Checking other libraries..."

# Check Others
other_libs=(
    "lombok-1.18.34.jar"
    "jackson-databind-2.18.2.jar"
    "jackson-core-2.18.2.jar"
    "jackson-annotations-2.18.2.jar"
    "jackson-datatype-jsr310-2.18.2.jar"
    "slf4j-api-2.0.16.jar"
    "logback-classic-1.5.12.jar"
    "logback-core-1.5.12.jar"
    "sqlite-jdbc-3.47.1.0.jar"
)

for lib in "${other_libs[@]}"; do
    if [ -f "lib/others/$lib" ]; then
        echo -e "  ${GREEN}✓${NC} $lib"
    else
        echo -e "  ${RED}✗${NC} $lib - MISSING"
        ((errors++))
    fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $errors -eq 0 ]; then
    echo -e "${GREEN}✅ All libraries are present!${NC}"
    echo ""
    echo "If IntelliJ still shows errors:"
    echo "1. File → Invalidate Caches → Invalidate and Restart"
    echo "2. Wait for indexing to complete"
    echo "3. Build → Rebuild Project"
else
    echo -e "${RED}❌ $errors library/libraries missing!${NC}"
    echo ""
    echo "Run the download script:"
    echo "  ./scripts/download-libs.sh"
    echo ""
    echo "And download JavaFX 21.0.9 manually:"
    echo "  https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-aarch64_bin-sdk.zip"
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check Java version
echo "☕ Checking Java version..."
java -version 2>&1 | head -n 1

echo ""
echo "📊 Library sizes:"
echo "  JavaFX: $(du -sh lib/javafx 2>/dev/null | cut -f1)"
echo "  Others: $(du -sh lib/others 2>/dev/null | cut -f1)"

exit $errors

