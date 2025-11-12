#!/bin/bash

# =================================================================
# PCM Desktop - Unified Setup Script (macOS/Linux)
# =================================================================
# Downloads all required libraries for PCM Desktop
#
# Usage:
#   ./setup.sh              # Download all libraries
#   ./setup.sh --javafx     # Download only JavaFX (manual step guidance)
#   ./setup.sh --core       # Download only core libraries
#   ./setup.sh --ui         # Download only UI libraries (AtlantaFX, Ikonli)
#   ./setup.sh --help       # Show help
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

# Default options
DOWNLOAD_CORE=false
DOWNLOAD_UI=false
DOWNLOAD_ALL=false

# Parse command line arguments
if [ $# -eq 0 ]; then
    DOWNLOAD_ALL=true
else
    while [[ $# -gt 0 ]]; do
        case $1 in
            --javafx)
                # Show JavaFX download instructions
                echo -e "${BLUE}📦 JavaFX Download Instructions${NC}"
                echo "================================"
                echo ""
                echo "JavaFX 21.0.9 must be downloaded manually from:"
                echo "https://gluonhq.com/products/javafx/"
                echo ""
                echo "Download for your platform:"
                echo ""
                echo -e "${GREEN}macOS (Apple Silicon):${NC}"
                echo "  https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-aarch64_bin-sdk.zip"
                echo ""
                echo -e "${GREEN}macOS (Intel):${NC}"
                echo "  https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-x64_bin-sdk.zip"
                echo ""
                echo -e "${GREEN}Linux:${NC}"
                echo "  https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_linux-x64_bin-sdk.zip"
                echo ""
                echo -e "${YELLOW}Installation Steps:${NC}"
                echo "  1. Download the appropriate ZIP file for your platform"
                echo "  2. Extract the ZIP file"
                echo "  3. Copy all .jar files from 'javafx-sdk-21.0.9/lib/' to: ./lib/javafx/"
                echo "  4. Copy all native libraries (.dylib, .so) to: ./lib/javafx/"
                echo ""
                exit 0
                ;;
            --core)
                DOWNLOAD_CORE=true
                shift
                ;;
            --ui)
                DOWNLOAD_UI=true
                shift
                ;;
            --all)
                DOWNLOAD_ALL=true
                shift
                ;;
            --help|-h)
                echo "PCM Desktop - Setup Script"
                echo ""
                echo "Usage: $0 [OPTIONS]"
                echo ""
                echo "Options:"
                echo "  --all              Download all libraries (default)"
                echo "  --core             Download only core libraries (Lombok, Jackson, SLF4J, SQLite)"
                echo "  --ui               Download only UI libraries (AtlantaFX, Ikonli)"
                echo "  --javafx           Show JavaFX download instructions"
                echo "  --help, -h         Show this help message"
                echo ""
                echo "Examples:"
                echo "  $0                 # Download all libraries"
                echo "  $0 --core          # Download core libraries only"
                echo "  $0 --ui            # Download UI libraries only"
                echo "  $0 --javafx        # Show JavaFX instructions"
                exit 0
                ;;
            *)
                echo -e "${RED}❌ Unknown option: $1${NC}"
                echo "Use --help for usage information"
                exit 1
                ;;
        esac
    done
fi

# If --all is set, download everything
if [ "$DOWNLOAD_ALL" = true ]; then
    DOWNLOAD_CORE=true
    DOWNLOAD_UI=true
fi

echo -e "${BLUE}📦 PCM Desktop - Setup Script${NC}"
echo "================================"
echo ""

# Create directories
echo -e "${BLUE}📁 Creating library directories...${NC}"
mkdir -p lib/javafx
mkdir -p lib/others
echo -e "${GREEN}✅ Directories created${NC}"
echo ""

# Download core libraries
if [ "$DOWNLOAD_CORE" = true ]; then
    echo -e "${BLUE}📥 Downloading Core Libraries${NC}"
    echo "================================"
    echo ""
    
    cd lib/others
    
    echo -e "${YELLOW}1️⃣ Downloading Lombok 1.18.34...${NC}"
    curl -L -o lombok-1.18.34.jar https://projectlombok.org/downloads/lombok.jar
    echo -e "${GREEN}✅ Lombok downloaded${NC}"
    echo ""
    
    echo -e "${YELLOW}2️⃣ Downloading Jackson 2.18.2...${NC}"
    curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.18.2/jackson-databind-2.18.2.jar
    curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.18.2/jackson-core-2.18.2.jar
    curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.18.2/jackson-annotations-2.18.2.jar
    curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.18.2/jackson-datatype-jsr310-2.18.2.jar
    echo -e "${GREEN}✅ Jackson downloaded${NC}"
    echo ""
    
    echo -e "${YELLOW}3️⃣ Downloading SLF4J 2.0.16...${NC}"
    curl -O https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar
    echo -e "${GREEN}✅ SLF4J downloaded${NC}"
    echo ""
    
    echo -e "${YELLOW}4️⃣ Downloading Logback 1.5.12...${NC}"
    curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.12/logback-classic-1.5.12.jar
    curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.12/logback-core-1.5.12.jar
    echo -e "${GREEN}✅ Logback downloaded${NC}"
    echo ""
    
    echo -e "${YELLOW}5️⃣ Downloading SQLite JDBC 3.47.1.0...${NC}"
    curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.47.1.0/sqlite-jdbc-3.47.1.0.jar
    echo -e "${GREEN}✅ SQLite JDBC downloaded${NC}"
    echo ""
    
    cd ../..
    
    echo -e "${GREEN}✅ Core libraries downloaded successfully!${NC}"
    echo ""
fi

# Download UI libraries
if [ "$DOWNLOAD_UI" = true ]; then
    echo -e "${BLUE}📥 Downloading UI Libraries${NC}"
    echo "================================"
    echo ""
    
    cd lib/others
    
    # Download AtlantaFX
    echo -e "${YELLOW}1️⃣ Downloading AtlantaFX 2.0.1...${NC}"
    ATLANTAFX_VERSION="2.0.1"
    ATLANTAFX_JAR="atlantafx-base-${ATLANTAFX_VERSION}.jar"
    ATLANTAFX_URL="https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/${ATLANTAFX_VERSION}/${ATLANTAFX_JAR}"
    
    curl -L -o "$ATLANTAFX_JAR" "$ATLANTAFX_URL"
    echo -e "${GREEN}✅ AtlantaFX downloaded${NC}"
    echo ""
    
    # Download Ikonli
    echo -e "${YELLOW}2️⃣ Downloading Ikonli 12.3.1...${NC}"
    IKONLI_VERSION="12.3.1"
    MAVEN_BASE="https://repo1.maven.org/maven2/org/kordamp/ikonli"
    
    # ikonli-core
    curl -L -o "ikonli-core-${IKONLI_VERSION}.jar" \
        "${MAVEN_BASE}/ikonli-core/${IKONLI_VERSION}/ikonli-core-${IKONLI_VERSION}.jar"
    
    # ikonli-javafx
    curl -L -o "ikonli-javafx-${IKONLI_VERSION}.jar" \
        "${MAVEN_BASE}/ikonli-javafx/${IKONLI_VERSION}/ikonli-javafx-${IKONLI_VERSION}.jar"
    
    # material design pack
    curl -L -o "ikonli-material2-pack-${IKONLI_VERSION}.jar" \
        "${MAVEN_BASE}/ikonli-material2-pack/${IKONLI_VERSION}/ikonli-material2-pack-${IKONLI_VERSION}.jar"
    
    # feather pack
    curl -L -o "ikonli-feather-pack-${IKONLI_VERSION}.jar" \
        "${MAVEN_BASE}/ikonli-feather-pack/${IKONLI_VERSION}/ikonli-feather-pack-${IKONLI_VERSION}.jar"
    
    echo -e "${GREEN}✅ Ikonli downloaded${NC}"
    echo ""
    
    cd ../..
    
    echo -e "${GREEN}✅ UI libraries downloaded successfully!${NC}"
    echo ""
fi

# Summary
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   Setup Completed Successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

if [ "$DOWNLOAD_CORE" = true ]; then
    echo -e "${BLUE}📋 Downloaded Core Libraries:${NC}"
    echo "  • Lombok 1.18.34"
    echo "  • Jackson 2.18.2"
    echo "  • SLF4J 2.0.16"
    echo "  • Logback 1.5.12"
    echo "  • SQLite JDBC 3.47.1.0"
    echo ""
fi

if [ "$DOWNLOAD_UI" = true ]; then
    echo -e "${BLUE}🎨 Downloaded UI Libraries:${NC}"
    echo "  • AtlantaFX 2.0.1"
    echo "  • Ikonli 12.3.1 (Core, JavaFX, Material2, Feather)"
    echo ""
fi

echo -e "${YELLOW}⚠️  Manual Step Required:${NC}"
echo ""
echo -e "${BLUE}JavaFX 21.0.9 must be downloaded manually.${NC}"
echo "Run this command for instructions:"
echo ""
echo -e "  ${GREEN}./scripts/setup.sh --javafx${NC}"
echo ""

echo -e "${BLUE}📚 Libraries in lib/others/:${NC}"
ls -lh lib/others/ | grep "\.jar$" | awk '{print "  •", $9, "(" $5 ")"}'
echo ""

echo -e "${YELLOW}💡 Next Steps:${NC}"
echo "  1. Download JavaFX (see instructions above)"
echo "  2. Run: ${GREEN}./scripts/build.sh${NC}"
echo "  3. Run: ${GREEN}./scripts/run.sh${NC}"
echo ""

