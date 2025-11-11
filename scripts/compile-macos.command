#!/bin/bash

# PCM Desktop - macOS Compile Script
# Compiles the Java source code

# Change to project root directory (parent of scripts folder)
cd "$(dirname "$0")/.."

echo "🔨 Compiling PCM Desktop..."
echo ""

# Create output directory
mkdir -p out

# Compile Java files
javac -cp "lib/javafx/*:lib/others/*" \
  -d out \
  -encoding UTF-8 \
  -Xlint:unchecked \
  src/main/java/com/noteflix/pcm/**/*.java

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Compilation successful!"
    echo ""
    
    # Copy resources to output directory
    echo "📦 Copying resources..."
    mkdir -p out/fxml out/css out/images/icons
    cp src/main/resources/fxml/*.fxml out/fxml/ 2>/dev/null || true
    cp src/main/resources/css/*.css out/css/ 2>/dev/null || true
    cp src/main/resources/images/icons/*.png out/images/icons/ 2>/dev/null || true
    cp src/main/resources/images/icons/*.svg out/images/icons/ 2>/dev/null || true
    cp src/main/resources/images/*.png out/images/ 2>/dev/null || true
    cp src/main/resources/logback.xml out/ 2>/dev/null || true
    echo "✅ Resources copied!"
    echo ""
    
    echo "You can now run: ./scripts/run-macos.command"
    echo "Or run from IntelliJ IDEA"
else
    echo ""
    echo "❌ Compilation failed!"
    echo ""
    echo "Please check the errors above."
fi

echo ""
read -p "Press Enter to exit..."

