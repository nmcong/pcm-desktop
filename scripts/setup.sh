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

# =================================================================
# Function Definitions (must be defined before use)
# =================================================================

generate_classpath_unix() {
    echo -e "${BLUE}📄 Creating macOS/Unix classpath file...${NC}"
    echo "#!/bin/bash" > classpath.sh
    echo "# PCM Desktop - macOS/Unix Classpath" >> classpath.sh
    echo "export CLASSPATH=." >> classpath.sh
    
    # Add JavaFX libraries
    for jar in lib/javafx/*.jar; do
        if [ -f "$jar" ]; then
            echo "export CLASSPATH=\$CLASSPATH:$jar" >> classpath.sh
        fi
    done
    
    # Add other libraries  
    for jar in lib/others/*.jar; do
        if [ -f "$jar" ]; then
            echo "export CLASSPATH=\$CLASSPATH:$jar" >> classpath.sh
        fi
    done
    
    # Add RAG libraries
    for jar in lib/rag/*.jar; do
        if [ -f "$jar" ]; then
            echo "export CLASSPATH=\$CLASSPATH:$jar" >> classpath.sh
        fi
    done
    
    # Add LangChain4j libraries
    for jar in lib/langchain4j/*.jar; do
        if [ -f "$jar" ]; then
            echo "export CLASSPATH=\$CLASSPATH:$jar" >> classpath.sh
        fi
    done
    
    # Add Text Component libraries
    for jar in lib/text-component/*.jar; do
        if [ -f "$jar" ]; then
            echo "export CLASSPATH=\$CLASSPATH:$jar" >> classpath.sh
        fi
    done
    
    chmod +x classpath.sh
    echo -e "${GREEN}✅ macOS classpath generated: classpath.sh${NC}"
}

generate_library_list() {
    echo -e "${BLUE}📋 Creating library inventory...${NC}"
    echo "# PCM Desktop - Library Inventory" > library_list.txt
    echo "# Generated on $(date)" >> library_list.txt
    echo "" >> library_list.txt
    
    echo "## JavaFX Libraries (lib/javafx/)" >> library_list.txt
    for jar in lib/javafx/*.jar; do
        if [ -f "$jar" ]; then
            echo "- $(basename "$jar")" >> library_list.txt
        fi
    done
    echo "" >> library_list.txt
    
    echo "## Core and Oracle Libraries (lib/others/)" >> library_list.txt
    for jar in lib/others/*.jar; do
        if [ -f "$jar" ]; then
            echo "- $(basename "$jar")" >> library_list.txt
        fi
    done
    echo "" >> library_list.txt
    
    echo "## RAG Libraries (lib/rag/)" >> library_list.txt
    for jar in lib/rag/*.jar; do
        if [ -f "$jar" ]; then
            echo "- $(basename "$jar")" >> library_list.txt
        fi
    done
    echo "" >> library_list.txt
    
    echo "## LLM Libraries (lib/langchain4j/)" >> library_list.txt
    for jar in lib/langchain4j/*.jar; do
        if [ -f "$jar" ]; then
            echo "- $(basename "$jar")" >> library_list.txt
        fi
    done
    echo "" >> library_list.txt
    
    echo "## Text Component Libraries (lib/text-component/)" >> library_list.txt
    for jar in lib/text-component/*.jar; do
        if [ -f "$jar" ]; then
            echo "- $(basename "$jar")" >> library_list.txt
        fi
    done
    echo "" >> library_list.txt
    
    echo -e "${GREEN}✅ Library inventory generated: library_list.txt${NC}"
}

configure_intellij_libraries() {
    echo -e "${BLUE}🔧 Configuring IntelliJ IDEA libraries...${NC}"
    
    # Create .idea/libraries directory
    mkdir -p .idea/libraries
    
    # Function to create library XML
    create_library_xml() {
        local lib_name="$1"
        local lib_dir="$2"
        local xml_file=".idea/libraries/${lib_name}.xml"
        
        echo "<component name=\"libraryTable\">" > "$xml_file"
        echo "  <library name=\"${lib_name}\">" >> "$xml_file"
        echo "    <CLASSES>" >> "$xml_file"
        
        # Add all JAR files from the directory
        for jar in ${lib_dir}/*.jar; do
            if [ -f "$jar" ]; then
                echo "      <root url=\"jar://\$PROJECT_DIR\$/${jar}!/\" />" >> "$xml_file"
            fi
        done
        
        echo "    </CLASSES>" >> "$xml_file"
        echo "    <JAVADOC />" >> "$xml_file"
        echo "    <SOURCES />" >> "$xml_file"
        echo "  </library>" >> "$xml_file"
        echo "</component>" >> "$xml_file"
        
        echo -e "${GREEN}  ✓ ${lib_name}${NC}"
    }
    
    # Create library configurations for each directory
    if [ -d "lib/javafx" ] && [ "$(ls -A lib/javafx/*.jar 2>/dev/null)" ]; then
        create_library_xml "JavaFX_21_0_9" "lib/javafx"
    fi
    
    if [ -d "lib/others" ] && [ "$(ls -A lib/others/*.jar 2>/dev/null)" ]; then
        create_library_xml "PCM_Core_Libraries" "lib/others"
    fi
    
    if [ -d "lib/rag" ] && [ "$(ls -A lib/rag/*.jar 2>/dev/null)" ]; then
        create_library_xml "PCM_RAG_Libraries" "lib/rag"
    fi
    
    if [ -d "lib/langchain4j" ] && [ "$(ls -A lib/langchain4j/*.jar 2>/dev/null)" ]; then
        create_library_xml "LangChain4j" "lib/langchain4j"
    fi
    
    if [ -d "lib/text-component" ] && [ "$(ls -A lib/text-component/*.jar 2>/dev/null)" ]; then
        create_library_xml "Text_Components" "lib/text-component"
    fi
    
    echo -e "${GREEN}✅ IntelliJ IDEA libraries configured${NC}"
    echo -e "${YELLOW}   ℹ️  Restart IntelliJ IDEA to see the libraries in Project Structure${NC}"
}

# =================================================================
# Main Setup Process
# =================================================================

# Auto-download everything without parameters

echo -e "${BLUE}📦 PCM Desktop - Setup Script${NC}"
echo "================================"
echo ""

# Create directories
echo -e "${BLUE}📁 Creating library directories...${NC}"
mkdir -p lib/javafx
mkdir -p lib/others
mkdir -p lib/rag
mkdir -p lib/langchain4j
mkdir -p lib/text-component
echo -e "${GREEN}✅ Directories created${NC}"
echo ""

# Download core libraries
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

echo -e "${YELLOW}6️⃣ Downloading Oracle OJDBC 23.3.0...${NC}"
curl -L -o ojdbc11-23.3.0.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/23.3.0.23.09/ojdbc11-23.3.0.23.09.jar
echo -e "${GREEN}✅ Oracle OJDBC downloaded${NC}"
echo ""

echo -e "${YELLOW}7️⃣ Downloading HikariCP 5.1.0...${NC}"
curl -O https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar
echo -e "${GREEN}✅ HikariCP downloaded${NC}"
echo ""

echo -e "${YELLOW}8️⃣ Downloading Oracle UCP 23.26.0...${NC}"
curl -L -o ucp-23.26.0.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ucp/23.26.0.0.0/ucp-23.26.0.0.0.jar
echo -e "${GREEN}✅ Oracle UCP downloaded${NC}"
echo ""

# Download AtlantaFX
echo -e "${YELLOW}9️⃣ Downloading AtlantaFX 2.0.1...${NC}"
ATLANTAFX_VERSION="2.0.1"
ATLANTAFX_JAR="atlantafx-base-${ATLANTAFX_VERSION}.jar"
ATLANTAFX_URL="https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/${ATLANTAFX_VERSION}/${ATLANTAFX_JAR}"

curl -L -o "$ATLANTAFX_JAR" "$ATLANTAFX_URL"
echo -e "${GREEN}✅ AtlantaFX downloaded${NC}"
echo ""

# Download Ikonli
echo -e "${YELLOW}🔟 Downloading Ikonli 12.3.1...${NC}"
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

echo -e "${GREEN}✅ Core and UI libraries downloaded successfully!${NC}"
echo ""

# Download RAG libraries
echo -e "${BLUE}📥 Downloading RAG Libraries${NC}"
echo "================================"
echo ""

cd lib/rag

echo -e "${YELLOW}1️⃣ Downloading Apache Lucene 9.11.1...${NC}"
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-core/9.11.1/lucene-core-9.11.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-analysis-common/9.11.1/lucene-analysis-common-9.11.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-analyzers-common/9.11.1/lucene-analyzers-common-9.11.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-queryparser/9.11.1/lucene-queryparser-9.11.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-queries/9.11.1/lucene-queries-9.11.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-highlighter/9.11.1/lucene-highlighter-9.11.1.jar
echo -e "${GREEN}✅ Apache Lucene downloaded${NC}"
echo ""

echo -e "${YELLOW}2️⃣ Downloading DJL ONNX Runtime 0.35.0...${NC}"
curl -O https://repo1.maven.org/maven2/ai/djl/onnxruntime/onnxruntime-engine/0.35.0/onnxruntime-engine-0.35.0.jar
curl -O https://repo1.maven.org/maven2/ai/djl/api/0.35.0/api-0.35.0.jar
curl -O https://repo1.maven.org/maven2/ai/djl/tokenizers/0.35.0/tokenizers-0.35.0.jar
echo -e "${GREEN}✅ DJL ONNX Runtime downloaded${NC}"
echo ""

echo -e "${YELLOW}3️⃣ Downloading ONNX Runtime 1.23.2...${NC}"
curl -L -o onnxruntime-1.23.2.jar https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.23.2/onnxruntime-1.23.2.jar
echo -e "${GREEN}✅ ONNX Runtime downloaded${NC}"
echo ""

cd ../..

echo -e "${GREEN}✅ RAG libraries downloaded successfully!${NC}"
echo ""

# Download LLM libraries  
echo -e "${BLUE}📥 Downloading LLM Libraries${NC}"
echo "================================"
echo ""

cd lib/langchain4j

echo -e "${YELLOW}1️⃣ Downloading LangChain4j 1.8.0...${NC}"
curl -O https://repo1.maven.org/maven2/dev/langchain4j/langchain4j/1.8.0/langchain4j-1.8.0.jar
curl -O https://repo1.maven.org/maven2/dev/langchain4j/langchain4j-core/1.8.0/langchain4j-core-1.8.0.jar
echo -e "${GREEN}✅ LangChain4j downloaded${NC}"
echo ""

cd ../..

echo -e "${GREEN}✅ LLM libraries downloaded successfully!${NC}"
echo ""

# Download JavaFX
echo -e "${BLUE}📥 Downloading and Extracting JavaFX${NC}"
echo "================================"
echo ""

# Detect architecture
if [[ $(uname -m) == "arm64" ]]; then
    JAVAFX_ARCH="osx-aarch64"
    echo -e "${YELLOW}📱 Detected Apple Silicon (ARM64)${NC}"
else
    JAVAFX_ARCH="osx-x64"
    echo -e "${YELLOW}💻 Detected Intel x64${NC}"
fi

JAVAFX_URL="https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_${JAVAFX_ARCH}_bin-sdk.zip"

echo -e "${YELLOW}1️⃣ Downloading JavaFX 21.0.9 for ${JAVAFX_ARCH}...${NC}"
curl -L -o javafx-21.0.9.zip "$JAVAFX_URL"
echo -e "${GREEN}✅ JavaFX downloaded${NC}"

echo -e "${YELLOW}2️⃣ Extracting JavaFX...${NC}"
unzip -q javafx-21.0.9.zip -d temp-javafx
echo -e "${GREEN}✅ JavaFX extracted${NC}"

echo -e "${YELLOW}3️⃣ Copying JavaFX files...${NC}"
cp temp-javafx/javafx-sdk-21.0.9/lib/*.jar lib/javafx/
cp temp-javafx/javafx-sdk-21.0.9/lib/*.dylib lib/javafx/
echo -e "${GREEN}✅ JavaFX files copied${NC}"

echo -e "${YELLOW}4️⃣ Cleaning up temporary files...${NC}"
rm -rf temp-javafx
rm javafx-21.0.9.zip
echo -e "${GREEN}✅ Cleanup completed${NC}"

echo -e "${GREEN}✅ JavaFX setup completed!${NC}"
echo ""

# Download Text Component libraries
echo -e "${BLUE}📥 Downloading Text Component Libraries${NC}"
echo "================================"
echo ""

cd lib/text-component

echo -e "${YELLOW}1️⃣ Downloading RichTextFX 0.11.6...${NC}"
curl -O https://repo1.maven.org/maven2/org/fxmisc/richtext/richtextfx/0.11.6/richtextfx-0.11.6.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/flowless/flowless/0.7.4/flowless-0.7.4.jar
curl -O https://repo1.maven.org/maven2/org/reactfx/reactfx/2.0-M5/reactfx-2.0-M5.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/undo/undofx/2.1.1/undofx-2.1.1.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/wellbehaved/wellbehavedfx/0.3.3/wellbehavedfx-0.3.3.jar
echo -e "${GREEN}✅ RichTextFX and dependencies downloaded${NC}"
echo ""

echo -e "${YELLOW}2️⃣ Downloading JavaFX Markdown Preview 1.0.3...${NC}"
curl -L -o javafx-markdown-preview-all-1.0.3.jar https://repo1.maven.org/maven2/com/sandec/mdfx/1.0.3/mdfx-1.0.3.jar
echo -e "${GREEN}✅ JavaFX Markdown Preview downloaded${NC}"
echo ""

cd ../..

echo -e "${GREEN}✅ Text Component libraries downloaded successfully!${NC}"
echo ""

# Generate classpath files
echo -e "${BLUE}📥 Generating Classpath Files${NC}"
echo "================================"
echo ""

echo -e "${YELLOW}1️⃣ Generating macOS/Unix classpath...${NC}"
generate_classpath_unix

echo -e "${YELLOW}2️⃣ Generating library list...${NC}"
generate_library_list

echo -e "${YELLOW}3️⃣ Configuring IntelliJ IDEA libraries...${NC}"
configure_intellij_libraries

echo -e "${GREEN}✅ Classpath and IDE configuration completed successfully!${NC}"
echo ""

# Summary
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   Setup Completed Successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

echo -e "${BLUE}📋 Downloaded ALL Libraries:${NC}"
echo ""
echo -e "  ${GREEN}Core Libraries:${NC}"
echo "  • Lombok 1.18.34"
echo "  • Jackson 2.18.2 (Core, Databind, Annotations, JSR310)"
echo "  • SLF4J 2.0.16"
echo "  • Logback 1.5.12 (Classic, Core)"
echo "  • SQLite JDBC 3.47.1.0"
echo ""
echo -e "  ${GREEN}Oracle Libraries:${NC}"
echo "  • Oracle OJDBC 23.3.0.23.09"
echo "  • HikariCP 5.1.0"
echo "  • Oracle UCP 23.3.0.23.09"
echo ""
echo -e "  ${GREEN}UI Libraries:${NC}"
echo "  • AtlantaFX 2.0.1"
echo "  • Ikonli 12.3.1 (Core, JavaFX, Material2, Feather)"
echo ""
echo -e "  ${GREEN}RAG Libraries:${NC}"
echo "  • Apache Lucene 9.11.1 (Core, Analysis, QueryParser, Highlighter)"
echo "  • DJL ONNX Runtime 0.35.0 (API, Engine, Tokenizers)"
echo "  • ONNX Runtime 1.23.2"
echo ""
echo -e "  ${GREEN}LLM Libraries:${NC}"
echo "  • LangChain4j 1.8.0 (Core and Main)"
echo ""
echo -e "  ${GREEN}JavaFX Libraries:${NC}"
echo "  • JavaFX 21.0.9 (automatically downloaded and extracted for $(uname -m))"
echo ""
echo -e "  ${GREEN}Text Component Libraries:${NC}"
echo "  • RichTextFX 0.11.6 and dependencies"
echo "  • JavaFX Markdown Preview 1.0.3"
echo ""

echo -e "${GREEN}✅ All libraries downloaded and ready to use!${NC}"
echo ""

echo -e "${BLUE}📚 Libraries Summary:${NC}"
echo ""
echo -e "  ${YELLOW}JavaFX Libraries (lib/javafx/):${NC}"
if [ -d "lib/javafx" ] && [ "$(ls -A lib/javafx)" ]; then
    echo "  JavaFX SDK files:"
    ls lib/javafx/*.jar 2>/dev/null | sed 's/.*\//    • /'
    echo "  Native libraries:"
    ls lib/javafx/*.dylib 2>/dev/null | sed 's/.*\//    • /'
else
    echo "    ⚠️ JavaFX not found!"
fi
echo ""

echo -e "  ${YELLOW}Core and Oracle Libraries (lib/others/):${NC}"
ls lib/others/*.jar 2>/dev/null | sed 's/.*\//    • /'
echo ""

echo -e "  ${YELLOW}RAG Libraries (lib/rag/):${NC}"
ls lib/rag/*.jar 2>/dev/null | sed 's/.*\//    • /'
echo ""

echo -e "  ${YELLOW}LLM Libraries (lib/langchain4j/):${NC}"
ls lib/langchain4j/*.jar 2>/dev/null | sed 's/.*\//    • /'
echo ""

echo -e "  ${YELLOW}Text Component Libraries (lib/text-component/):${NC}"
ls lib/text-component/*.jar 2>/dev/null | sed 's/.*\//    • /'
echo ""

echo -e "${YELLOW}💡 Next Steps:${NC}"
echo "  1. Run: ${GREEN}./scripts/build.sh${NC}"
echo "  2. Run: ${GREEN}./scripts/run.sh${NC}"
echo "  3. Restart IntelliJ IDEA to load libraries"
echo ""
