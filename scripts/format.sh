#!/bin/bash

# =================================================================
# PCM Desktop - Code Formatting Script (macOS/Linux)
# =================================================================
# Formats Java source code using Google Java Format
#
# Usage:
#   ./format.sh              # Format all Java files
#   ./format.sh --check      # Check formatting only (no changes)
#   ./format.sh --file FILE  # Format specific file
#   ./format.sh --help       # Show help
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

# Google Java Format JAR
FORMATTER_JAR="lib/others/google-java-format-1.32.0-all-deps.jar"

# Default options
CHECK_ONLY=false
SPECIFIC_FILE=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --check|-c)
            CHECK_ONLY=true
            shift
            ;;
        --file|-f)
            SPECIFIC_FILE="$2"
            shift 2
            ;;
        --help|-h)
            echo "PCM Desktop - Code Formatting Script"
            echo ""
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --check, -c              Check formatting only (no changes)"
            echo "  --file FILE, -f FILE     Format specific file"
            echo "  --help, -h               Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                                    # Format all Java files"
            echo "  $0 --check                            # Check if files need formatting"
            echo "  $0 --file src/main/java/MyClass.java # Format specific file"
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}🎨 PCM Desktop - Code Formatter${NC}"
echo "================================"
echo ""

# Check if formatter JAR exists
if [ ! -f "$FORMATTER_JAR" ]; then
    echo -e "${RED}❌ Google Java Format JAR not found!${NC}"
    echo ""
    echo "Expected location: $FORMATTER_JAR"
    echo ""
    echo "Please run the setup script first:"
    echo -e "  ${GREEN}./scripts/setup.sh${NC}"
    exit 1
fi

# Find Java 21+ for formatter (required by google-java-format 1.29.0+)
JAVA_CMD="java"
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    JAVA_21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
    if [ -n "$JAVA_21_HOME" ]; then
        JAVA_CMD="$JAVA_21_HOME/bin/java"
        echo -e "${GREEN}✅ Using Java 21: $JAVA_21_HOME${NC}"
    else
        echo -e "${YELLOW}⚠️  Java 21 not found, trying default java command...${NC}"
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -lt 21 ]; then
            echo -e "${RED}❌ Google Java Format 1.29.0+ requires Java 21 or higher!${NC}"
            echo -e "${YELLOW}Current Java version: $(java -version 2>&1 | head -n 1)${NC}"
            echo ""
            echo "Please install Java 21+ or set JAVA_HOME to Java 21:"
            echo -e "  ${BLUE}export JAVA_HOME=\$(/usr/libexec/java_home -v 21)${NC}"
            exit 1
        fi
    fi
fi
echo ""

# Format specific file
if [ -n "$SPECIFIC_FILE" ]; then
    if [ ! -f "$SPECIFIC_FILE" ]; then
        echo -e "${RED}❌ File not found: $SPECIFIC_FILE${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}📝 Formatting file: $SPECIFIC_FILE${NC}"
    
    if [ "$CHECK_ONLY" = true ]; then
        "$JAVA_CMD" -jar "$FORMATTER_JAR" --dry-run --set-exit-if-changed "$SPECIFIC_FILE"
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ File is correctly formatted${NC}"
        else
            echo -e "${YELLOW}⚠️  File needs formatting${NC}"
            exit 1
        fi
    else
        "$JAVA_CMD" -jar "$FORMATTER_JAR" --replace "$SPECIFIC_FILE"
        echo -e "${GREEN}✅ File formatted successfully${NC}"
    fi
    
    exit 0
fi

# Find all Java files
echo -e "${BLUE}🔍 Finding Java source files...${NC}"
JAVA_FILES=$(find src/main/java -name "*.java" -type f)
JAVA_COUNT=$(echo "$JAVA_FILES" | wc -l | tr -d ' ')

if [ $JAVA_COUNT -eq 0 ]; then
    echo -e "${YELLOW}⚠️  No Java files found${NC}"
    exit 0
fi

echo -e "${GREEN}✅ Found $JAVA_COUNT Java files${NC}"
echo ""

# Format or check all files
if [ "$CHECK_ONLY" = true ]; then
    echo -e "${BLUE}🔍 Checking code formatting...${NC}"
    
    UNFORMATTED=0
    while IFS= read -r file; do
        "$JAVA_CMD" -jar "$FORMATTER_JAR" --dry-run --set-exit-if-changed "$file" > /dev/null 2>&1
        if [ $? -ne 0 ]; then
            echo -e "  ${YELLOW}⚠️  $file${NC}"
            ((UNFORMATTED++))
        fi
    done <<< "$JAVA_FILES"
    
    echo ""
    if [ $UNFORMATTED -eq 0 ]; then
        echo -e "${GREEN}✅ All files are correctly formatted!${NC}"
        exit 0
    else
        echo -e "${YELLOW}⚠️  $UNFORMATTED file(s) need formatting${NC}"
        echo ""
        echo "To format all files, run:"
        echo -e "  ${BLUE}./scripts/format.sh${NC}"
        exit 1
    fi
else
    echo -e "${BLUE}🎨 Formatting Java source files...${NC}"
    
    FORMATTED=0
    FAILED=0
    
    while IFS= read -r file; do
        if "$JAVA_CMD" -jar "$FORMATTER_JAR" --replace "$file" > /dev/null 2>&1; then
            ((FORMATTED++))
            echo -e "  ${GREEN}✓${NC} $file"
        else
            ((FAILED++))
            echo -e "  ${RED}✗${NC} $file"
        fi
    done <<< "$JAVA_FILES"
    
    echo ""
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}✅ Successfully formatted $FORMATTED file(s)!${NC}"
        echo ""
        echo -e "${YELLOW}💡 Tip: Use --check to verify formatting without changes${NC}"
    else
        echo -e "${YELLOW}⚠️  Formatted $FORMATTED file(s), $FAILED failed${NC}"
        exit 1
    fi
fi

