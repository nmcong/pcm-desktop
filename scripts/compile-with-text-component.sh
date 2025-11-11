#!/bin/bash

# =================================================================
# PCM Desktop - Compile with Universal Text Component Libraries
# =================================================================

echo "🚀 Compiling PCM Desktop with Universal Text Component..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project paths
PROJECT_DIR="/Users/nguyencong/Workspace/pcm-desktop"
SRC_DIR="$PROJECT_DIR/src/main/java"
RESOURCES_DIR="$PROJECT_DIR/src/main/resources"
OUT_DIR="$PROJECT_DIR/out"
LIB_DIR="$PROJECT_DIR/lib"

echo -e "${BLUE}📂 Project directory: $PROJECT_DIR${NC}"

# Create output directory
mkdir -p "$OUT_DIR"

# Clean previous compilation
echo -e "${YELLOW}🧹 Cleaning previous compilation...${NC}"
rm -rf "$OUT_DIR"/*

# Copy resources
echo -e "${BLUE}📋 Copying resources...${NC}"
if [ -d "$RESOURCES_DIR" ]; then
    cp -r "$RESOURCES_DIR"/* "$OUT_DIR/"
    echo -e "${GREEN}✅ Resources copied successfully${NC}"
else
    echo -e "${YELLOW}⚠️  No resources directory found${NC}"
fi

# Build classpath with all libraries
echo -e "${BLUE}🔗 Building classpath...${NC}"

CLASSPATH=""

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

# Remove leading colon
CLASSPATH="${CLASSPATH#:}"

echo -e "${GREEN}✅ Classpath built with $(echo $CLASSPATH | tr ':' '\n' | wc -l) libraries${NC}"

# Compile Java source files
echo -e "${BLUE}🔨 Compiling Java source files...${NC}"

# Find all Java files
JAVA_FILES=$(find "$SRC_DIR" -name "*.java" -type f)
JAVA_COUNT=$(echo "$JAVA_FILES" | wc -l)

echo -e "${BLUE}📊 Found $JAVA_COUNT Java files to compile${NC}"

# Compile with JavaFX modules and Universal Text Component libraries
javac \
    --module-path "$LIB_DIR/javafx" \
    --add-modules javafx.controls,javafx.fxml,javafx.web \
    --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
    --add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
    --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED \
    -cp "$CLASSPATH:$OUT_DIR" \
    -d "$OUT_DIR" \
    $JAVA_FILES

# Check compilation result
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Compilation successful!${NC}"
    echo -e "${GREEN}📦 Classes compiled to: $OUT_DIR${NC}"
    
    # Count compiled class files
    CLASS_COUNT=$(find "$OUT_DIR" -name "*.class" -type f | wc -l)
    echo -e "${GREEN}📊 Generated $CLASS_COUNT class files${NC}"
    
    # Show Universal Text Component classes
    echo -e "${BLUE}🎨 Universal Text Component classes:${NC}"
    find "$OUT_DIR" -path "*/text/*" -name "*.class" -type f | sed 's|.*/out/||' | sed 's|\.class$||' | sed 's|/|.|g' | sort
    
else
    echo -e "${RED}❌ Compilation failed!${NC}"
    echo -e "${YELLOW}💡 Check for missing dependencies or syntax errors${NC}"
    exit 1
fi

# Verify key Universal Text Component classes exist
echo -e "${BLUE}🔍 Verifying Universal Text Component...${NC}"

REQUIRED_CLASSES=(
    "com/noteflix/pcm/ui/components/text/UniversalTextComponent.class"
    "com/noteflix/pcm/ui/components/text/TextContentType.class"
    "com/noteflix/pcm/ui/components/text/ViewMode.class"
    "com/noteflix/pcm/ui/components/text/renderers/MarkdownRenderer.class"
    "com/noteflix/pcm/ui/components/text/renderers/PlainTextRenderer.class"
    "com/noteflix/pcm/ui/pages/UniversalTextDemoPage.class"
)

MISSING_CLASSES=0

for class_file in "${REQUIRED_CLASSES[@]}"; do
    if [ -f "$OUT_DIR/$class_file" ]; then
        echo -e "${GREEN}✅ $class_file${NC}"
    else
        echo -e "${RED}❌ $class_file${NC}"
        MISSING_CLASSES=$((MISSING_CLASSES + 1))
    fi
done

if [ $MISSING_CLASSES -eq 0 ]; then
    echo -e "${GREEN}🎉 All Universal Text Component classes compiled successfully!${NC}"
    echo -e "${BLUE}🚀 Ready to run with text-component support!${NC}"
    echo -e "${YELLOW}💡 Use run-with-text-component.sh to launch the application${NC}"
else
    echo -e "${RED}❌ $MISSING_CLASSES required classes are missing${NC}"
    exit 1
fi

echo -e "${GREEN}✨ Compilation completed successfully!${NC}"