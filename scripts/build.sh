#!/bin/bash

# =================================================================
# PCM Desktop - Unified Build Script (macOS/Linux)
# =================================================================
# Compiles Java source code with optional text component support
#
# Usage:
#   ./build.sh              # Standard build
#   ./build.sh --text       # Build with text component
#   ./build.sh --clean      # Clean and build
#   ./build.sh --help       # Show help
# =================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Change to project root directory
cd "$(dirname "$0")/.."
PROJECT_ROOT="$(pwd)"

# Default options
WITH_TEXT_COMPONENT=false
CLEAN_BUILD=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --text|--text-component)
            WITH_TEXT_COMPONENT=true
            shift
            ;;
        --clean|-c)
            CLEAN_BUILD=true
            shift
            ;;
        --help|-h)
            echo "PCM Desktop - Build Script"
            echo ""
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --text, --text-component    Build with Universal Text Component support"
            echo "  --clean, -c                 Clean build directory before compilation"
            echo "  --help, -h                  Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                          # Standard build"
            echo "  $0 --text                   # Build with text component"
            echo "  $0 --clean                  # Clean and build"
            echo "  $0 --clean --text           # Clean build with text component"
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}🔨 PCM Desktop - Build Script${NC}"
echo "================================"
echo ""

# Clean build directory if requested
if [ "$CLEAN_BUILD" = true ]; then
    echo -e "${YELLOW}🧹 Cleaning build directory...${NC}"
    rm -rf out/*
    echo -e "${GREEN}✅ Build directory cleaned${NC}"
    echo ""
fi

# Create output directory
mkdir -p out

# Build classpath
echo -e "${BLUE}🔗 Building classpath...${NC}"
CLASSPATH=""

# Add JavaFX libraries
for jar in lib/javafx/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Add other libraries
for jar in lib/others/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Add text component libraries if requested
if [ "$WITH_TEXT_COMPONENT" = true ]; then
    echo -e "${YELLOW}📚 Including Universal Text Component libraries...${NC}"
    for jar in lib/text-component/*.jar; do
        if [ -f "$jar" ]; then
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
fi

# Remove leading colon
CLASSPATH="${CLASSPATH#:}"

echo -e "${GREEN}✅ Classpath configured${NC}"
echo ""

# Compile Java source files
echo -e "${BLUE}🔨 Compiling Java source files...${NC}"

# Find all Java files
find src/main/java -name "*.java" > sources.txt

# Count Java files
JAVA_COUNT=$(wc -l < sources.txt | tr -d ' ')
echo -e "${BLUE}📊 Found $JAVA_COUNT Java files to compile${NC}"

# Compile with appropriate options
if [ "$WITH_TEXT_COMPONENT" = true ]; then
    # Compile with module exports for text component
    javac \
        --module-path lib/javafx \
        --add-modules javafx.controls,javafx.fxml,javafx.web \
        --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
        --add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
        -cp "$CLASSPATH" \
        -d out \
        -encoding UTF-8 \
        -Xlint:unchecked \
        @sources.txt
else
    # Standard compilation
    javac \
        -cp "$CLASSPATH" \
        -d out \
        -encoding UTF-8 \
        -Xlint:unchecked \
        @sources.txt
fi

rm sources.txt

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Compilation successful!${NC}"
    echo ""
    
    # Copy resources to output directory
    echo -e "${BLUE}📦 Copying resources...${NC}"
    
    # Create resource directories
    mkdir -p out/fxml/components out/css out/images/icons out/db/migration
    
    # Copy FXML files
    if [ -d "src/main/resources/fxml" ]; then
        find src/main/resources/fxml -name "*.fxml" -exec sh -c '
            rel_path=$(echo "{}" | sed "s|src/main/resources/fxml/||")
            mkdir -p "out/fxml/$(dirname "$rel_path")"
            cp "{}" "out/fxml/$rel_path"
        ' \; 2>/dev/null || true
        echo -e "${GREEN}✅ FXML files copied${NC}"
    fi
    
    # Copy CSS files
    if [ -d "src/main/resources/css" ]; then
        cp src/main/resources/css/*.css out/css/ 2>/dev/null || true
        echo -e "${GREEN}✅ CSS files copied${NC}"
    fi
    
    # Copy image files
    if [ -d "src/main/resources/images" ]; then
        cp src/main/resources/images/icons/*.png out/images/icons/ 2>/dev/null || true
        cp src/main/resources/images/icons/*.svg out/images/icons/ 2>/dev/null || true
        cp src/main/resources/images/*.png out/images/ 2>/dev/null || true
        echo -e "${GREEN}✅ Image files copied${NC}"
    fi
    
    # Copy database migration files
    if [ -d "src/main/resources/db/migration" ]; then
        cp src/main/resources/db/migration/*.sql out/db/migration/ 2>/dev/null || true
        echo -e "${GREEN}✅ Database migration files copied${NC}"
    fi
    
    # Copy other resource files
    cp src/main/resources/logback.xml out/ 2>/dev/null || true
    
    echo ""
    
    # Count compiled class files
    CLASS_COUNT=$(find out -name "*.class" -type f | wc -l | tr -d ' ')
    echo -e "${GREEN}📊 Generated $CLASS_COUNT class files${NC}"
    
    echo ""
    echo -e "${GREEN}✨ Build completed successfully!${NC}"
    echo ""
    
    if [ "$WITH_TEXT_COMPONENT" = true ]; then
        echo -e "${YELLOW}💡 To run with text component support:${NC}"
        echo -e "   ${BLUE}./scripts/run.sh --text${NC}"
    else
        echo -e "${YELLOW}💡 To run the application:${NC}"
        echo -e "   ${BLUE}./scripts/run.sh${NC}"
    fi
    
else
    echo ""
    echo -e "${RED}❌ Compilation failed!${NC}"
    echo ""
    echo "Please check the errors above."
    exit 1
fi

