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

echo "[INFO] Creating library directories..."
mkdir -p lib/javafx
mkdir -p lib/database
mkdir -p lib/logs
mkdir -p lib/rag
mkdir -p lib/ui
mkdir -p lib/icons
mkdir -p lib/utils
mkdir -p bin
mkdir -p models
echo "[OK] Directories created"
echo

# Download core libraries
echo "========================================"
echo "   Downloading Core Libraries"
echo "========================================"
echo

# Download Database Libraries
echo "========================================"
echo "   Downloading Database Libraries"
echo "========================================"
echo

cd lib/database

echo "[INFO] 1. Downloading SQLite JDBC"
[ -f sqlite-jdbc-3.51.0.0.jar ] && echo "[SKIP] SQLite JDBC already exists" || {
    curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.51.0.0/sqlite-jdbc-3.51.0.0.jar
    echo "[OK] SQLite JDBC downloaded"
}
echo

echo "[INFO] 2. Downloading Oracle OJDBC"
[ -f ojdbc11-23.26.0.0.0.jar ] && echo "[SKIP] Oracle OJDBC already exists" || {
    curl -L -o ojdbc11-23.26.0.0.0.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/23.26.0.0.0/ojdbc11-23.26.0.0.0.jar
    echo "[OK] Oracle OJDBC downloaded"
}
echo

echo "[INFO] 3. Downloading HikariCP"
[ -f HikariCP-7.0.2.jar ] && echo "[SKIP] HikariCP already exists" || {
    curl -O https://repo1.maven.org/maven2/com/zaxxer/HikariCP/7.0.2/HikariCP-7.0.2.jar
    echo "[OK] HikariCP downloaded"
}
echo

echo "[INFO] 4. Downloading Oracle UCP"
[ -f ucp-23.26.0.0.0.jar ] && echo "[SKIP] Oracle UCP already exists" || {
    curl -L -o ucp-23.26.0.0.0.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ucp/23.26.0.0.0/ucp-23.26.0.0.0.jar
    echo "[OK] Oracle UCP downloaded"
}
echo

cd ../..
echo "[OK] Database libraries downloaded successfully!"
echo

# Download Logging Libraries
echo "========================================"
echo "   Downloading Logging Libraries"
echo "========================================"
echo

cd lib/logs

echo "[INFO] 1. Downloading SLF4J"
[ -f slf4j-api-2.0.17.jar ] && echo "[SKIP] SLF4J already exists" || {
    curl -O https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar
    echo "[OK] SLF4J downloaded"
}
echo

echo "[INFO] 2. Downloading Logback"
[ -f logback-classic-1.5.21.jar ] || curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.21/logback-classic-1.5.21.jar
[ -f logback-core-1.5.21.jar ] || curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.21/logback-core-1.5.21.jar
echo "[OK] Logback libraries checked"
echo

cd ../..
echo "[OK] Logging libraries downloaded successfully!"
echo

# Download Utils Libraries
echo "========================================"
echo "   Downloading Utils Libraries"
echo "========================================"
echo

cd lib/utils

echo "[INFO] 1. Downloading Lombok"
[ -f lombok-1.18.34.jar ] && echo "[SKIP] Lombok already exists" || {
    curl -L -o lombok-1.18.34.jar https://projectlombok.org/downloads/lombok.jar
    echo "[OK] Lombok downloaded"
}
echo

echo "[INFO] 2. Downloading Jackson"
[ -f jackson-databind-2.20.1.jar ] || curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.20.1/jackson-databind-2.20.1.jar
[ -f jackson-core-2.20.1.jar ] || curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.20.1/jackson-core-2.20.1.jar
[ -f jackson-annotations-2.20.jar ] || curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.20/jackson-annotations-2.20.jar
[ -f jackson-datatype-jsr310-2.20.1.jar ] || curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.20.1/jackson-datatype-jsr310-2.20.1.jar
echo "[OK] Jackson libraries checked"
echo

cd ../..
echo "[OK] Utils libraries downloaded successfully!"
echo

# Download UI Libraries
echo "========================================"
echo "   Downloading UI Libraries"
echo "========================================"
echo

cd lib/ui

echo "[INFO] 1. Downloading AtlantaFX"
[ -f atlantafx-base-2.1.0.jar ] && echo "[SKIP] AtlantaFX already exists" || {
    curl -L -o atlantafx-base-2.1.0.jar https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/2.1.0/atlantafx-base-2.1.0.jar
    echo "[OK] AtlantaFX downloaded"
}
echo

echo "[OK] AtlantaFX checked"
echo

echo "[INFO] 3. Downloading RichTextFX"
[ -f richtextfx-0.11.6.jar ] || curl -O https://repo1.maven.org/maven2/org/fxmisc/richtext/richtextfx/0.11.6/richtextfx-0.11.6.jar
[ -f flowless-0.7.4.jar ] || curl -O https://repo1.maven.org/maven2/org/fxmisc/flowless/flowless/0.7.4/flowless-0.7.4.jar
[ -f reactfx-2.0-M6.jar ] || curl -O https://repo1.maven.org/maven2/org/reactfx/reactfx/2.0-M6/reactfx-2.0-M6.jar
[ -f undofx-2.1.1.jar ] || curl -O https://repo1.maven.org/maven2/org/fxmisc/undo/undofx/2.1.1/undofx-2.1.1.jar
[ -f wellbehavedfx-0.3.3.jar ] || curl -O https://repo1.maven.org/maven2/org/fxmisc/wellbehaved/wellbehavedfx/0.3.3/wellbehavedfx-0.3.3.jar
echo "[OK] RichTextFX and dependencies checked"
echo

cd ../..
echo "[OK] UI libraries downloaded successfully!"
echo

# Download Icons Libraries
echo "========================================"
echo "   Downloading Icons Libraries"
echo "========================================"
echo

cd lib/icons

echo "[INFO] 1. Downloading Ikonli"
[ -f ikonli-core-12.4.0.jar ] || curl -L -o ikonli-core-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-core/12.4.0/ikonli-core-12.4.0.jar
[ -f ikonli-javafx-12.4.0.jar ] || curl -L -o ikonli-javafx-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-javafx/12.4.0/ikonli-javafx-12.4.0.jar
[ -f ikonli-feather-pack-12.4.0.jar ] || curl -L -o ikonli-feather-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-feather-pack/12.4.0/ikonli-feather-pack-12.4.0.jar
[ -f ikonli-antdesignicons-pack-12.4.0.jar ] || curl -L -o ikonli-antdesignicons-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-antdesignicons-pack/12.4.0/ikonli-antdesignicons-pack-12.4.0.jar
[ -f ikonli-bpmn-pack-12.4.0.jar ] || curl -L -o ikonli-bpmn-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-bpmn-pack/12.4.0/ikonli-bpmn-pack-12.4.0.jar
[ -f ikonli-octicons-pack-12.4.0.jar ] || curl -L -o ikonli-octicons-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-octicons-pack/12.4.0/ikonli-octicons-pack-12.4.0.jar
echo "[OK] Ikonli libraries checked"
echo

cd ../..
echo "[OK] Icons libraries downloaded successfully!"
echo

# Download RAG libraries
echo "========================================"
echo "   Downloading RAG Libraries"
echo "========================================"
echo

cd lib/rag

echo "[INFO] 1. Downloading Apache Lucene"
[ -f lucene-core-10.3.1.jar ] || curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-core/10.3.1/lucene-core-10.3.1.jar
[ -f lucene-analysis-common-10.3.1.jar ] || curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-analysis-common/10.3.1/lucene-analysis-common-10.3.1.jar
[ -f lucene-queryparser-10.3.1.jar ] || curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-queryparser/10.3.1/lucene-queryparser-10.3.1.jar
[ -f lucene-queries-10.3.1.jar ] || curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-queries/10.3.1/lucene-queries-10.3.1.jar
[ -f lucene-highlighter-10.3.1.jar ] || curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-highlighter/10.3.1/lucene-highlighter-10.3.1.jar
echo "[OK] Apache Lucene libraries checked"
echo

echo "[INFO] 2. Downloading DJL ONNX Runtime"
[ -f onnxruntime-engine-0.35.0.jar ] || curl -O https://repo1.maven.org/maven2/ai/djl/onnxruntime/onnxruntime-engine/0.35.0/onnxruntime-engine-0.35.0.jar
[ -f api-0.35.0.jar ] || curl -O https://repo1.maven.org/maven2/ai/djl/api/0.35.0/api-0.35.0.jar
[ -f tokenizers-0.35.0.jar ] || curl -O https://repo1.maven.org/maven2/ai/djl/huggingface/tokenizers/0.35.0/tokenizers-0.35.0.jar
echo "[OK] DJL ONNX Runtime libraries checked"
echo

echo "[INFO] 3. Downloading ONNX Runtime"
[ -f onnxruntime-1.23.2.jar ] && echo "[SKIP] ONNX Runtime already exists" || {
    curl -L -o onnxruntime-1.23.2.jar https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.23.2/onnxruntime-1.23.2.jar
    echo "[OK] ONNX Runtime downloaded"
}
echo

echo "[INFO] 4. Downloading JavaParser"
[ -f javaparser-core-3.27.1.jar ] || curl -O https://repo1.maven.org/maven2/com/github/javaparser/javaparser-core/3.27.1/javaparser-core-3.27.1.jar
[ -f javaparser-symbol-solver-core-3.27.1.jar ] || curl -O https://repo1.maven.org/maven2/com/github/javaparser/javaparser-symbol-solver-core/3.27.1/javaparser-symbol-solver-core-3.27.1.jar
echo "[OK] JavaParser libraries checked"
echo

cd ../..

echo "[OK] RAG libraries downloaded successfully!"
echo

# Download JavaFX
echo "========================================"
echo "   Downloading and Extracting JavaFX"
echo "========================================"
echo

# Check if JavaFX already exists by checking for jar files in lib/javafx
if ls lib/javafx/*.jar 1> /dev/null 2>&1; then
    echo "[SKIP] JavaFX already exists in lib/javafx"
else
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
    if [ -f "$JAVAFX_FILE" ]; then
        echo "[SKIP] JavaFX zip already downloaded"
    else
        curl -L -o "$JAVAFX_FILE" "$JAVAFX_URL"
        echo "[OK] JavaFX downloaded"
    fi

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
fi
echo

echo "[OK] JavaFX setup completed!"
echo


# Download Qdrant Vector Database
echo "========================================"
echo "   Downloading Qdrant Vector Database"
echo "========================================"
echo

# Detect platform for Qdrant
OS_TYPE=$(uname -s)
ARCH=$(uname -m)

if [ "$OS_TYPE" = "Darwin" ]; then
    # macOS
    QDRANT_URL="https://github.com/qdrant/qdrant/releases/download/v1.15.5/qdrant-aarch64-apple-darwin.tar.gz"
    QDRANT_FILE="qdrant-aarch64-apple-darwin.tar.gz"
elif [ "$OS_TYPE" = "Linux" ]; then
    QDRANT_URL="https://github.com/qdrant/qdrant/releases/download/v1.15.5/qdrant-v1.15.5-x86_64-unknown-linux-gnu.tar.gz"
    QDRANT_FILE="qdrant-v1.15.5-x86_64-unknown-linux-gnu.tar.gz"
else
    echo "[ERROR] Unsupported platform for Qdrant: $OS_TYPE"
    echo "Please download Qdrant manually from: https://github.com/qdrant/qdrant/releases"
fi

if [ -n "$QDRANT_URL" ]; then
    echo "[INFO] 1. Downloading Qdrant v1.15.5 for $OS_TYPE..."
    if [ -f bin/qdrant ]; then
        echo "[SKIP] Qdrant binary already exists"
    else
        curl -L -o "$QDRANT_FILE" "$QDRANT_URL"
        echo "[OK] Qdrant downloaded"
        
        echo "[INFO] 2. Extracting Qdrant..."
        tar -xzf "$QDRANT_FILE"
        mv qdrant bin/
        chmod +x bin/qdrant
        rm "$QDRANT_FILE"
        echo "[OK] Qdrant extracted and ready"
    fi
fi

echo "[OK] Qdrant Vector Database setup completed!"
echo

# Download Embedding Model
echo "========================================"
echo "   Downloading Embedding Model"
echo "========================================"
echo

MODEL_NAME="all-MiniLM-L6-v2"
echo "[INFO] Downloading $MODEL_NAME model..."

if [ -d "models/$MODEL_NAME" ] && [ -f "models/$MODEL_NAME/model.onnx" ]; then
    echo "[SKIP] Model $MODEL_NAME already exists"
else
    mkdir -p models/$MODEL_NAME
    cd models/$MODEL_NAME
    
    echo "[INFO] 1. Downloading model.onnx..."
    [ -f model.onnx ] || curl -L -o model.onnx "https://huggingface.co/sentence-transformers/$MODEL_NAME/resolve/main/onnx/model.onnx"
    
    echo "[INFO] 2. Downloading tokenizer.json..."
    [ -f tokenizer.json ] || curl -L -O "https://huggingface.co/sentence-transformers/$MODEL_NAME/resolve/main/tokenizer.json"
    
    echo "[INFO] 3. Downloading config.json..."
    [ -f config.json ] || curl -L -O "https://huggingface.co/sentence-transformers/$MODEL_NAME/resolve/main/config.json"
    
    cd ../..
    echo "[OK] Embedding model $MODEL_NAME downloaded"
fi

echo "[OK] Embedding model setup completed!"
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

echo "  Database Libraries (lib/database/):"
ls -1 lib/database/*.jar 2>/dev/null || echo "  [WARNING] Database libraries not found!"
echo

echo "  Logging Libraries (lib/logs/):"
ls -1 lib/logs/*.jar 2>/dev/null || echo "  [WARNING] Logging libraries not found!"
echo

echo "  Utils Libraries (lib/utils/):"
ls -1 lib/utils/*.jar 2>/dev/null || echo "  [WARNING] Utils libraries not found!"
echo

echo "  UI Libraries (lib/ui/):"
ls -1 lib/ui/*.jar 2>/dev/null || echo "  [WARNING] UI libraries not found!"
echo

echo "  Icons Libraries (lib/icons/):"
ls -1 lib/icons/*.jar 2>/dev/null || echo "  [WARNING] Icons libraries not found!"
echo

echo "  RAG Libraries (lib/rag/):"
ls -1 lib/rag/*.jar 2>/dev/null || echo "  [WARNING] RAG libraries not found!"
echo

echo "  Vector Database (bin/):"
[ -f bin/qdrant ] && echo "  qdrant (v1.15.5)" || echo "  [WARNING] Qdrant not found!"
echo

echo "  Embedding Models (models/):"
ls -1 models/*/model.onnx 2>/dev/null || echo "  [WARNING] No models found!"
echo

echo "[INFO] Configuring IntelliJ IDEA project..."
echo

# Configure IntelliJ IDEA
if [ -f "scripts/configure-intellij.sh" ]; then
    chmod +x scripts/configure-intellij.sh
    echo "[INFO] Running IntelliJ IDEA configuration..."
    ./scripts/configure-intellij.sh
    echo "[OK] IntelliJ IDEA project configured!"
else
    echo "[WARNING] IntelliJ configuration script not found!"
fi
echo

echo "[INFO] Next Steps:"
echo "  1. Run: ./scripts/build.sh"
echo "  2. Run: ./scripts/run.sh"
echo "  3. Open project in IntelliJ IDEA (File -> Open -> Select project directory)"
echo
