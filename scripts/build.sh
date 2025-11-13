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

# ============================================================
# Ensure Java 21 is used
# ============================================================
ensure_java_21() {
    # Check if running on macOS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # Use java_home to find Java 21
        if command -v /usr/libexec/java_home &> /dev/null; then
            JAVA_21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
            if [ -n "$JAVA_21_HOME" ]; then
                export JAVA_HOME="$JAVA_21_HOME"
                export PATH="$JAVA_HOME/bin:$PATH"
                echo -e "${GREEN}☕ Using Java 21: $JAVA_HOME${NC}"
                return 0
            fi
        fi
    fi
    
    # Check current java version
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            echo -e "${GREEN}☕ Java 21 is already active${NC}"
            return 0
        fi
    fi
    
    # Java 21 not found
    echo -e "${RED}❌ Java 21 not found!${NC}"
    echo ""
    echo "This project requires Java 21."
    echo ""
    echo "On macOS, install with:"
    echo "  brew install --cask temurin21"
    echo ""
    echo "Or download from: https://adoptium.net/"
    exit 1
}

# Ensure Java 21 before proceeding
ensure_java_21
echo ""

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

# Verify libraries function
verify_libraries() {
    echo -e "${BLUE}🔍 Verifying required libraries...${NC}"
    local errors=0
    
    # Check JavaFX libraries
    local javafx_libs=(
        "javafx.base.jar"
        "javafx.controls.jar"
        "javafx.fxml.jar"
        "javafx.graphics.jar"
        "javafx.media.jar"
        "javafx.web.jar"
    )
    
    for lib in "${javafx_libs[@]}"; do
        if [ ! -f "lib/javafx/$lib" ]; then
            echo -e "  ${RED}✗${NC} $lib - MISSING"
            ((errors++))
        fi
    done
    
    # Check other libraries
    local other_libs=(
        "lombok-1.18.34.jar"
        "jackson-databind-2.18.2.jar"
        "jackson-core-2.18.2.jar"
        "slf4j-api-2.0.16.jar"
        "logback-classic-1.5.12.jar"
        "sqlite-jdbc-3.47.1.0.jar"
    )
    
    for lib in "${other_libs[@]}"; do
        if [ ! -f "lib/others/$lib" ]; then
            echo -e "  ${RED}✗${NC} $lib - MISSING"
            ((errors++))
        fi
    done
    
    if [ $errors -gt 0 ]; then
        echo -e "${RED}❌ $errors library/libraries missing!${NC}"
        echo ""
        echo "Run the setup script to download libraries:"
        echo -e "  ${GREEN}./scripts/setup.sh${NC}"
        echo ""
        exit 1
    fi
    
    echo -e "${GREEN}✅ All required libraries present${NC}"
    echo ""
}

# Verify libraries before building
verify_libraries

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

# Add RAG libraries (Lucene, etc.)
for jar in lib/rag/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Add LangChain4j libraries
for jar in lib/langchain4j/*.jar; do
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
    mkdir -p out/fxml/components out/css out/images/icons out/db/migration out/i18n
    
    # Copy i18n files
    if [ -d "src/main/resources/i18n" ]; then
        cp src/main/resources/i18n/*.properties out/i18n/ 2>/dev/null || true
        echo -e "${GREEN}✅ i18n files copied${NC}"
    fi
    
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

