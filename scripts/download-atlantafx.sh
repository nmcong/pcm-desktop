#!/bin/bash

# Script to download AtlantaFX library
# AtlantaFX - Modern CSS theme collection for JavaFX

cd "$(dirname "$0")/.."

echo "📦 Downloading AtlantaFX..."
echo "================================"

ATLANTAFX_VERSION="2.1.0"
ATLANTAFX_JAR="atlantafx-base-${ATLANTAFX_VERSION}.jar"
ATLANTAFX_URL="https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/${ATLANTAFX_VERSION}/${ATLANTAFX_JAR}"

LIB_DIR="lib/others"
mkdir -p "$LIB_DIR"

echo "📥 Downloading AtlantaFX ${ATLANTAFX_VERSION}..."
if curl -L -o "$LIB_DIR/$ATLANTAFX_JAR" "$ATLANTAFX_URL"; then
    echo "✅ AtlantaFX downloaded successfully!"
    echo "📁 Location: $LIB_DIR/$ATLANTAFX_JAR"
    
    # Verify file size
    FILE_SIZE=$(ls -lh "$LIB_DIR/$ATLANTAFX_JAR" | awk '{print $5}')
    echo "📊 File size: $FILE_SIZE"
else
    echo "❌ Failed to download AtlantaFX"
    exit 1
fi

echo ""
echo "✅ AtlantaFX installation complete!"
echo ""
echo "Available themes:"
echo "  • PrimerLight (GitHub-inspired light theme)"
echo "  • PrimerDark (GitHub-inspired dark theme)"
echo "  • NordLight (Nord color palette light)"
echo "  • NordDark (Nord color palette dark)"
echo "  • CupertinoLight (macOS-inspired light)"
echo "  • CupertinoDark (macOS-inspired dark)"
echo "  • Dracula (Dracula color scheme)"
echo ""

