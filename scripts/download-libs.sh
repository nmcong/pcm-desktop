#!/bin/bash

# PCM Desktop - Library Download Script
# Downloads all required libraries (latest versions)

set -e

echo "📦 Downloading PCM Desktop Libraries..."
echo ""

# Create directories
mkdir -p lib/javafx
mkdir -p lib/others

cd lib/others

echo "1️⃣ Downloading Lombok 1.18.34..."
curl -L -o lombok-1.18.34.jar https://projectlombok.org/downloads/lombok.jar

echo "2️⃣ Downloading Jackson 2.18.2..."
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.18.2/jackson-databind-2.18.2.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.18.2/jackson-core-2.18.2.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.18.2/jackson-annotations-2.18.2.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.18.2/jackson-datatype-jsr310-2.18.2.jar

echo "3️⃣ Downloading SLF4J 2.0.16..."
curl -O https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar

echo "4️⃣ Downloading Logback 1.5.12..."
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.12/logback-classic-1.5.12.jar
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.12/logback-core-1.5.12.jar

echo "5️⃣ Downloading SQLite JDBC 3.47.1.0..."
curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.47.1.0/sqlite-jdbc-3.47.1.0.jar

cd ../..

echo ""
echo "✅ Library download complete!"
echo ""
echo "📋 Downloaded libraries in lib/others/:"
ls -lh lib/others/

echo ""
echo "⚠️  Manual Step Required:"
echo "   JavaFX 21.0.9 must be downloaded manually from:"
echo "   https://gluonhq.com/products/javafx/"
echo ""
echo "   Download for your platform (JavaFX 21.0.9 for Java 21):"
echo "   - macOS (Apple Silicon): https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-aarch64_bin-sdk.zip"
echo "   - macOS (Intel): https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-x64_bin-sdk.zip"
echo "   - Windows: https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip"
echo "   - Linux: https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_linux-x64_bin-sdk.zip"
echo ""
echo "   Steps:"
echo "   1. Download the appropriate ZIP file for your platform"
echo "   2. Extract the ZIP file"
echo "   3. Copy all .jar files from 'javafx-sdk-21.0.9/lib/' folder to: ./lib/javafx/"
echo "   4. Delete old JavaFX 25 jar files from ./lib/javafx/"
echo ""
echo "🚀 After adding JavaFX JARs, you can run the application!"

