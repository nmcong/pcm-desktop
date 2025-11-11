#!/bin/bash

# =================================================================
# PCM Desktop - Run with Universal Text Component Libraries  
# =================================================================

echo "🚀 Starting PCM Desktop with Universal Text Component..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project paths
PROJECT_DIR="/Users/nguyencong/Workspace/pcm-desktop"
OUT_DIR="$PROJECT_DIR/out"
LIB_DIR="$PROJECT_DIR/lib"

# Check if compiled
if [ ! -d "$OUT_DIR" ] || [ ! -f "$OUT_DIR/com/noteflix/pcm/PCMApplication.class" ]; then
    echo -e "${YELLOW}⚠️  Application not compiled. Running compilation first...${NC}"
    "$PROJECT_DIR/scripts/compile-with-text-component.sh"
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Compilation failed. Cannot start application.${NC}"
        exit 1
    fi
fi

# Build classpath with all libraries
echo -e "${BLUE}🔗 Building runtime classpath...${NC}"

CLASSPATH="$OUT_DIR"

# Add JavaFX libraries
for jar in "$LIB_DIR/javafx"/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Add other libraries
for jar in "$LIB_DIR/others"/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Add Universal Text Component libraries  
for jar in "$LIB_DIR/text-component"/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

echo -e "${GREEN}✅ Runtime classpath built${NC}"

# Verify Universal Text Component libraries
echo -e "${BLUE}🔍 Verifying text component libraries...${NC}"

TEXT_LIBS=(
    "$LIB_DIR/text-component/javafx-markdown-preview-all-1.0.3.jar"
    "$LIB_DIR/text-component/richtextfx-0.11.4.jar"
    "$LIB_DIR/text-component/flowless-0.7.3.jar"
    "$LIB_DIR/text-component/reactfx-2.0-M5.jar"
)

MISSING_LIBS=0

for lib in "${TEXT_LIBS[@]}"; do
    if [ -f "$lib" ]; then
        echo -e "${GREEN}✅ $(basename $lib)${NC}"
    else
        echo -e "${RED}❌ $(basename $lib)${NC}"
        MISSING_LIBS=$((MISSING_LIBS + 1))
    fi
done

if [ $MISSING_LIBS -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Some text component libraries are missing. The component might not work properly.${NC}"
fi

# Get Java version for logging
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo -e "${BLUE}☕ Java version: $JAVA_VERSION${NC}"

# Start the application
echo -e "${GREEN}🎬 Starting PCM Desktop...${NC}"
echo -e "${BLUE}📱 Universal Text Component demo available in sidebar${NC}"
echo -e "${YELLOW}💡 Navigate to 'Text Component' to see the demo${NC}"

java \
    --module-path "$LIB_DIR/javafx" \
    --add-modules javafx.controls,javafx.fxml,javafx.web \
    --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
    --add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
    --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED \
    --add-opens javafx.base/javafx.beans.binding=ALL-UNNAMED \
    --add-opens javafx.base/javafx.beans.property=ALL-UNNAMED \
    --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED \
    -cp "$CLASSPATH" \
    com.noteflix.pcm.PCMApplication \
    "$@"

# Check exit status
EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ PCM Desktop closed normally${NC}"
else
    echo -e "${RED}❌ PCM Desktop exited with error code: $EXIT_CODE${NC}"
fi

echo -e "${BLUE}👋 Thanks for using PCM Desktop with Universal Text Component!${NC}"