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

echo "2️⃣ Downloading Jackson 2.17.2..."
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.2/jackson-databind-2.17.2.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.2/jackson-core-2.17.2.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.2/jackson-annotations-2.17.2.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.17.2/jackson-datatype-jsr310-2.17.2.jar

echo "3️⃣ Downloading SLF4J 2.0.13..."
curl -O https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar

echo "4️⃣ Downloading Logback 1.5.6..."
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.6/logback-classic-1.5.6.jar
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.6/logback-core-1.5.6.jar

echo "5️⃣ Downloading SQLite JDBC 3.46.1.0..."
curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.0/sqlite-jdbc-3.46.1.0.jar

cd ../..

echo ""
echo "✅ Library download complete!"
echo ""
echo "📋 Downloaded libraries in lib/others/:"
ls -lh lib/others/

echo ""
echo "⚠️  Manual Step Required:"
echo "   JavaFX 23 must be downloaded manually from:"
echo "   https://gluonhq.com/products/javafx/"
echo ""
echo "   Download for your platform:"
echo "   - macOS (Apple Silicon): openjfx-23_osx-aarch64_bin-sdk.zip"
echo "   - macOS (Intel): openjfx-23_osx-x64_bin-sdk.zip"
echo "   - Windows: openjfx-23_windows-x64_bin-sdk.zip"
echo "   - Linux: openjfx-23_linux-x64_bin-sdk.zip"
echo ""
echo "   Then extract and copy all .jar files from 'lib/' folder to:"
echo "   ./lib/javafx/"
echo ""
echo "🚀 After adding JavaFX JARs, you can run the application!"

