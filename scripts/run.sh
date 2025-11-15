#!/bin/bash

# =================================================================
# PCM Desktop - Unified Run Script (macOS/Linux)
# =================================================================
# Runs the PCM Desktop application with various modes
#
# Usage:
#   ./run.sh                # Run main application
#   ./run.sh --text         # Run with text component support
#   ./run.sh --api-demo     # Run API integration demo
#   ./run.sh --sso-demo     # Run SSO integration demo
#   ./run.sh --help         # Show help
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
                # Force this session to use Java 21
                alias java="$JAVA_HOME/bin/java"
                alias javac="$JAVA_HOME/bin/javac"
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
RUN_MODE="normal"
WITH_TEXT_COMPONENT=false
AUTO_COMPILE=true

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --text|--text-component)
            WITH_TEXT_COMPONENT=true
            shift
            ;;
        --api-demo)
            RUN_MODE="api-demo"
            shift
            ;;
        --sso-demo)
            RUN_MODE="sso-demo"
            shift
            ;;
        --no-compile)
            AUTO_COMPILE=false
            shift
            ;;
        --help|-h)
            echo "PCM Desktop - Run Script"
            echo ""
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --text, --text-component    Run with Universal Text Component support"
            echo "  --api-demo                  Run API integration demo"
            echo "  --sso-demo                  Run SSO integration demo"
            echo "  --no-compile                Skip auto-compilation check"
            echo "  --help, -h                  Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                          # Run main application"
            echo "  $0 --text                   # Run with text component"
            echo "  $0 --api-demo               # Run API demo"
            echo "  $0 --sso-demo               # Run SSO demo"
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}🚀 PCM Desktop - Run Script${NC}"
echo "================================"
echo ""

# Check if compiled
if [ "$AUTO_COMPILE" = true ]; then
    if [ ! -d "out" ] || [ -z "$(ls -A out 2>/dev/null)" ] || [ ! -f "out/com/noteflix/pcm/PCMApplication.class" ]; then
        echo -e "${YELLOW}⚠️  Project not compiled. Compiling now...${NC}"
        
        if [ "$WITH_TEXT_COMPONENT" = true ]; then
            ./scripts/build.sh --text
        else
            ./scripts/build.sh
        fi
        
        if [ $? -ne 0 ]; then
            echo -e "${RED}❌ Compilation failed!${NC}"
            exit 1
        fi
        
        echo ""
    fi
fi

# Copy resources to output directory
echo -e "${BLUE}📦 Copying resources...${NC}"
mkdir -p out/fxml/components out/css out/images/icons out/db/migration out/i18n

# Copy i18n files
cp src/main/resources/i18n/*.properties out/i18n/ 2>/dev/null || true

# Copy FXML files
find src/main/resources/fxml -name "*.fxml" -exec sh -c '
    rel_path=$(echo "{}" | sed "s|src/main/resources/fxml/||")
    mkdir -p "out/fxml/$(dirname "$rel_path")"
    cp "{}" "out/fxml/$rel_path"
' \; 2>/dev/null || true

# Copy other resources
cp src/main/resources/css/*.css out/css/ 2>/dev/null || true
cp src/main/resources/images/icons/*.png out/images/icons/ 2>/dev/null || true
cp src/main/resources/images/icons/*.svg out/images/icons/ 2>/dev/null || true
cp src/main/resources/images/*.png out/images/ 2>/dev/null || true
cp src/main/resources/db/migration/*.sql out/db/migration/ 2>/dev/null || true
cp src/main/resources/logback.xml out/ 2>/dev/null || true

echo -e "${GREEN}✅ Resources copied${NC}"
echo ""

# Build classpath
CLASSPATH="out:lib/javafx/*:lib/database/*:lib/logs/*:lib/utils/*:lib/ui/*:lib/icons/*:lib/rag/*"

# Run based on mode
case $RUN_MODE in
    api-demo)
        echo -e "${BLUE}🎯 Running API Integration Demo${NC}"
        echo "================================"
        echo ""
        
        # Check if OPENAI_API_KEY is set
        if [ -z "$OPENAI_API_KEY" ]; then
            echo -e "${YELLOW}⚠️  OPENAI_API_KEY environment variable not found!${NC}"
            echo ""
            echo "Please set your OpenAI API key first:"
            echo "  export OPENAI_API_KEY=your-api-key-here"
            echo ""
            echo "Or create a .env file in the project root:"
            echo "  echo 'OPENAI_API_KEY=your-api-key-here' > .env"
            echo "  source .env"
            echo ""
            exit 1
        fi
        
        echo -e "${GREEN}✅ API Key found${NC}"
        echo ""
        
        "$JAVA_HOME/bin/java" -cp "$CLASSPATH" com.noteflix.pcm.llm.examples.APIDemo
        
        echo ""
        echo -e "${GREEN}👋 Demo completed!${NC}"
        ;;
        
    sso-demo)
        echo -e "${BLUE}🔐 Running SSO Integration Demo${NC}"
        echo "================================"
        echo ""
        
        # Create logs directory if it doesn't exist
        mkdir -p logs
        
        "$JAVA_HOME/bin/java" -cp "$CLASSPATH" com.noteflix.pcm.examples.SSOIntegrationDemo
        
        echo ""
        echo -e "${GREEN}👋 SSO Integration Demo completed!${NC}"
        echo ""
        echo "📚 For more information, see:"
        echo "  • docs/SSO_INTEGRATION_GUIDE.md"
        echo "  • logs/security-audit.log"
        ;;
        
    normal)
        if [ "$WITH_TEXT_COMPONENT" = true ]; then
            echo -e "${BLUE}🎬 Starting PCM Desktop with Text Component...${NC}"
            echo ""
            
            # Verify text component libraries
            TEXT_LIBS=(
                "lib/text-component/javafx-markdown-preview-all-1.0.3.jar"
                "lib/text-component/richtextfx-0.11.4.jar"
                "lib/text-component/flowless-0.7.3.jar"
                "lib/text-component/reactfx-2.0-M5.jar"
            )
            
            MISSING_LIBS=0
            for lib in "${TEXT_LIBS[@]}"; do
                if [ ! -f "$lib" ]; then
                    echo -e "${RED}❌ Missing: $(basename $lib)${NC}"
                    MISSING_LIBS=$((MISSING_LIBS + 1))
                fi
            done
            
            if [ $MISSING_LIBS -gt 0 ]; then
                echo -e "${YELLOW}⚠️  Some text component libraries are missing${NC}"
            fi
            
            # Run with text component support
            "$JAVA_HOME/bin/java" \
                --module-path lib/javafx \
                --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media \
                --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
                --add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
                --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED \
                --add-opens javafx.base/javafx.beans.binding=ALL-UNNAMED \
                --add-opens javafx.base/javafx.beans.property=ALL-UNNAMED \
                --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED \
                -cp "$CLASSPATH" \
                com.noteflix.pcm.PCMApplication
        else
            echo -e "${BLUE}🎬 Starting PCM Desktop...${NC}"
            echo ""
            
            # Standard run - same as run.bat on Windows
            "$JAVA_HOME/bin/java" -Djava.library.path=lib/javafx \
                --module-path lib/javafx \
                --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media \
                -cp "$CLASSPATH" \
                com.noteflix.pcm.PCMApplication
        fi
        
        EXIT_CODE=$?
        
        if [ $EXIT_CODE -ne 0 ]; then
            echo ""
            echo -e "${RED}❌ Application exited with error code: $EXIT_CODE${NC}"
            exit $EXIT_CODE
        fi
        ;;
esac

