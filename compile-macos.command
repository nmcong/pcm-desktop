#!/bin/bash

# PCM Desktop - macOS Compile Script
# Compiles the Java source code

cd "$(dirname "$0")"

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
    echo "You can now run: ./run-macos.command"
    echo "Or run from IntelliJ IDEA"
else
    echo ""
    echo "❌ Compilation failed!"
    echo ""
    echo "Please check the errors above."
fi

echo ""
read -p "Press Enter to exit..."

