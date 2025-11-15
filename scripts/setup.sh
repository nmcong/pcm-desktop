#!/bin/bash
# =================================================================
# PCM Desktop - Unified Setup Script (Unix/Linux/Mac)
# =================================================================
# Downloads all required libraries for PCM Desktop
#
# Usage:
#   ./setup.sh              - Download all libraries
#   ./setup.sh --javafx     - Show JavaFX download instructions
#   ./setup.sh --core       - Download only core libraries
#   ./setup.sh --ui         - Download only UI libraries
#   ./setup.sh --help       - Show help
# =================================================================

set -e

# Change to project root directory
cd "$(dirname "$0")/.."

# :start_setup

echo
echo "========================================"
echo "   PCM Desktop - Setup Script"
echo "========================================"
echo

# Check for curl
if ! command -v curl &> /dev/null; then
    echo "[ERROR] curl not found!"
    echo "Please install curl or download libraries manually."
    exit 1
fi

echo "[INFO] Cleaning old library directory..."
if [ -d "lib" ]; then
    rm -rf lib
    echo "[OK] Old library directory removed"
fi
echo

echo "[INFO] Creating library directories..."
mkdir -p lib/javafx
mkdir -p lib/others
mkdir -p lib/rag
mkdir -p lib/text-component
echo "[OK] Directories created"
echo

# Download core libraries
echo "========================================"
echo "   Downloading Core Libraries"
echo "========================================"
echo

cd lib/others

echo "[INFO] 1. Downloading Lombok"
curl -L -o lombok-1.18.34.jar https://projectlombok.org/downloads/lombok.jar
echo "[OK] Lombok downloaded"
echo

echo "[INFO] 2. Downloading Jackson"
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.20.1/jackson-databind-2.20.1.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.20.1/jackson-core-2.20.1.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.20/jackson-annotations-2.20.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.20.1/jackson-datatype-jsr310-2.20.1.jar
echo "[OK] Jackson downloaded"
echo

echo "[INFO] 3. Downloading SLF4J"
curl -O https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar
echo "[OK] SLF4J downloaded"
echo

echo "[INFO] 4. Downloading Logback"
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.21/logback-classic-1.5.21.jar
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.21/logback-core-1.5.21.jar
echo "[OK] Logback downloaded"
echo

echo "[INFO] 5. Downloading SQLite JDBC"
curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.51.0.0/sqlite-jdbc-3.51.0.0.jar
echo "[OK] SQLite JDBC downloaded"
echo

echo "[INFO] 6. Downloading Oracle OJDBC"
curl -L -o ojdbc11-23.26.0.0.0.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/23.26.0.0.0/ojdbc11-23.26.0.0.0.jar
echo "[OK] Oracle OJDBC downloaded"
echo

echo "[INFO] 7. Downloading HikariCP"
curl -O https://repo1.maven.org/maven2/com/zaxxer/HikariCP/7.0.2/HikariCP-7.0.2.jar
echo "[OK] HikariCP downloaded"
echo

echo "[INFO] 8. Downloading Oracle UCP"
curl -L -o ucp-23.26.0.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ucp/23.26.0/ucp-23.26.0.jar
echo "[OK] Oracle UCP downloaded"
echo

# Download AtlantaFX
echo "[INFO] 9. Downloading AtlantaFX"
curl -L -o atlantafx-base-2.1.0.jar https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/2.1.0/atlantafx-base-2.1.0.jar
echo "[OK] AtlantaFX downloaded"
echo

# Download Ikonli
echo "[INFO] 10. Downloading Ikonli"
curl -L -o ikonli-core-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-core/12.4.0/ikonli-core-12.4.0.jar
curl -L -o ikonli-javafx-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-javafx/12.4.0/ikonli-javafx-12.4.0.jar
curl -L -o ikonli-feather-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-feather-pack/12.4.0/ikonli-feather-pack-12.4.0.jar
curl -L -o ikonli-antdesignicons-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-antdesignicons-pack/12.4.0/ikonli-antdesignicons-pack-12.4.0.jar
curl -L -o ikonli-bpmn-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-bpmn-pack/12.4.0/ikonli-bpmn-pack-12.4.0.jar
curl -L -o ikonli-octicons-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-octicons-pack/12.4.0/ikonli-octicons-pack-12.4.0.jar
echo "[OK] Ikonli downloaded"
echo

cd ../..

echo "[OK] Core and UI libraries downloaded successfully!"
echo

# Download RAG libraries
echo "========================================"
echo "   Downloading RAG Libraries"
echo "========================================"
echo

cd lib/rag

echo "[INFO] 1. Downloading Apache Lucene"
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-core/10.3.1/lucene-core-10.3.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-analysis-common/10.3.1/lucene-analysis-common-10.3.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-queryparser/10.3.1/lucene-queryparser-10.3.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-queries/10.3.1/lucene-queries-10.3.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-highlighter/10.3.1/lucene-highlighter-10.3.1.jar
echo "[OK] Apache Lucene downloaded"
echo

echo "[INFO] 2. Downloading DJL ONNX Runtime"
curl -O https://repo1.maven.org/maven2/ai/djl/onnxruntime/onnxruntime-engine/0.35.0/onnxruntime-engine-0.35.0.jar
curl -O https://repo1.maven.org/maven2/ai/djl/api/0.35.0/api-0.35.0.jar
curl -O https://repo1.maven.org/maven2/ai/djl/huggingface/tokenizers/0.35.0/tokenizers-0.35.0.jar
echo "[OK] DJL ONNX Runtime downloaded"
echo

echo "[INFO] 3. Downloading ONNX Runtime"
curl -L -o onnxruntime-1.23.2.jar https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.23.2/onnxruntime-1.23.2.jar
echo "[OK] ONNX Runtime downloaded"
echo

echo "[INFO] 4. Downloading JavaParser"
curl -O https://repo1.maven.org/maven2/com/github/javaparser/javaparser-core/3.26.3/javaparser-core-3.26.3.jar
curl -O https://repo1.maven.org/maven2/com/github/javaparser/javaparser-symbol-solver-core/3.26.3/javaparser-symbol-solver-core-3.26.3.jar
echo "[OK] JavaParser downloaded"
echo

cd ../..

echo "[OK] RAG libraries downloaded successfully!"
echo

# Download JavaFX
echo "========================================"
echo "   Downloading and Extracting JavaFX"
echo "========================================"
echo

# Detect platform
OS_TYPE=$(uname -s)
ARCH=$(uname -m)

if [ "$OS_TYPE" = "Darwin" ]; then
    # macOS
    if [ "$ARCH" = "arm64" ]; then
        JAVAFX_URL="https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-aarch64_bin-sdk.zip"
        JAVAFX_FILE="javafx-21.0.9-osx-aarch64.zip"
    else
        JAVAFX_URL="https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-x64_bin-sdk.zip"
        JAVAFX_FILE="javafx-21.0.9-osx-x64.zip"
    fi
elif [ "$OS_TYPE" = "Linux" ]; then
    JAVAFX_URL="https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_linux-x64_bin-sdk.zip"
    JAVAFX_FILE="javafx-21.0.9-linux-x64.zip"
else
    echo "[ERROR] Unsupported platform: $OS_TYPE"
    echo "Please download JavaFX manually from: https://gluonhq.com/products/javafx/"
    exit 1
fi

echo "[INFO] 1. Downloading JavaFX 21.0.9 for $OS_TYPE..."
curl -L -o "$JAVAFX_FILE" "$JAVAFX_URL"
echo "[OK] JavaFX downloaded"

echo "[INFO] 2. Extracting JavaFX..."
unzip -q "$JAVAFX_FILE" -d temp-javafx
echo "[OK] JavaFX extracted"

echo "[INFO] 3. Copying JavaFX files..."
cp temp-javafx/javafx-sdk-21.0.9/lib/*.jar lib/javafx/
if [ "$OS_TYPE" = "Darwin" ]; then
    cp temp-javafx/javafx-sdk-21.0.9/lib/*.dylib lib/javafx/ 2>/dev/null || true
else
    cp temp-javafx/javafx-sdk-21.0.9/lib/*.so lib/javafx/ 2>/dev/null || true
fi
echo "[OK] JavaFX files copied"

echo "[INFO] 4. Cleaning up temporary files..."
rm -rf temp-javafx
rm "$JAVAFX_FILE"
echo "[OK] Cleanup completed"
echo

echo "[OK] JavaFX setup completed!"
echo

# Download Text Component libraries
echo "========================================"
echo "   Downloading Text Component Libraries"
echo "========================================"
echo

cd lib/text-component

echo "[INFO] 1. Downloading RichTextFX"
curl -O https://repo1.maven.org/maven2/org/fxmisc/richtext/richtextfx/0.11.6/richtextfx-0.11.6.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/flowless/flowless/0.7.4/flowless-0.7.4.jar
curl -O https://repo1.maven.org/maven2/org/reactfx/reactfx/2.0-M6/reactfx-2.0-M6.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/undo/undofx/2.1.1/undofx-2.1.1.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/wellbehaved/wellbehavedfx/0.3.3/wellbehavedfx-0.3.3.jar
echo "[OK] RichTextFX and dependencies downloaded"
echo

cd ../..

echo "[OK] Text Component libraries downloaded successfully!"
echo

# Summary
echo "========================================"
echo "   Setup Completed Successfully!"
echo "========================================"
echo

echo "[SUCCESS] All libraries downloaded and ready to use!"
echo

echo "[INFO] Libraries Summary:"
echo

echo "  JavaFX Libraries (lib/javafx/):"
ls -1 lib/javafx/*.jar 2>/dev/null || echo "  [WARNING] JavaFX not found!"
echo

echo "  Core and Oracle Libraries (lib/others/):"
ls -1 lib/others/*.jar 2>/dev/null
echo

echo "  RAG Libraries (lib/rag/):"
ls -1 lib/rag/*.jar 2>/dev/null
echo

echo "  Text Component Libraries (lib/text-component/):"
ls -1 lib/text-component/*.jar 2>/dev/null
echo

echo "[INFO] Next Steps:"
echo "  1. Run: ./scripts/build.sh"
echo "  2. Run: ./scripts/run.sh"
echo
