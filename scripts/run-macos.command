#!/bin/bash

# PCM Desktop - macOS Run Script
# Double-click this file to run the application

cd "$(dirname "$0")"

echo "🚀 Starting PCM Desktop Application..."
echo ""

# Check if compiled
if [ ! -d "out" ] || [ -z "$(ls -A out 2>/dev/null)" ]; then
    echo "⚠️  Project not compiled. Compiling now..."
    javac -cp "lib/javafx/*:lib/others/*" \
      -d out \
      -encoding UTF-8 \
      src/main/java/com/noteflix/pcm/**/*.java
    
    if [ $? -ne 0 ]; then
        echo "❌ Compilation failed!"
        echo ""
        read -p "Press Enter to exit..."
        exit 1
    fi
    
    echo "✅ Compilation successful!"
    echo ""
fi

# Copy resources to output directory
echo "📦 Copying resources..."
mkdir -p out/fxml out/css out/images
cp src/main/resources/fxml/*.fxml out/fxml/ 2>/dev/null || true
cp src/main/resources/css/*.css out/css/ 2>/dev/null || true
cp src/main/resources/logback.xml out/ 2>/dev/null || true
echo "✅ Resources copied!"
echo ""

# Run application
java --module-path lib/javafx \
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media \
  -cp "out:lib/others/*" \
  com.noteflix.pcm.PCMApplication

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Application crashed or exited with error!"
    echo ""
    read -p "Press Enter to exit..."
fi

