#!/bin/bash

# Script to download Ikonli icon library
# Ikonli - Icon packs for Java applications

cd "$(dirname "$0")/.."

echo "📦 Downloading Ikonli..."
echo "================================"

IKONLI_VERSION="12.3.1"
MAVEN_BASE="https://repo1.maven.org/maven2/org/kordamp/ikonli"

LIB_DIR="lib/others"
mkdir -p "$LIB_DIR"

# Download ikonli-core
echo "📥 Downloading ikonli-core..."
curl -L -o "$LIB_DIR/ikonli-core-${IKONLI_VERSION}.jar" \
    "${MAVEN_BASE}/ikonli-core/${IKONLI_VERSION}/ikonli-core-${IKONLI_VERSION}.jar"

# Download ikonli-javafx
echo "📥 Downloading ikonli-javafx..."
curl -L -o "$LIB_DIR/ikonli-javafx-${IKONLI_VERSION}.jar" \
    "${MAVEN_BASE}/ikonli-javafx/${IKONLI_VERSION}/ikonli-javafx-${IKONLI_VERSION}.jar"

# Download material design pack
echo "📥 Downloading ikonli-material2-pack..."
curl -L -o "$LIB_DIR/ikonli-material2-pack-${IKONLI_VERSION}.jar" \
    "${MAVEN_BASE}/ikonli-material2-pack/${IKONLI_VERSION}/ikonli-material2-pack-${IKONLI_VERSION}.jar"

# Download feather pack
echo "📥 Downloading ikonli-feather-pack..."
curl -L -o "$LIB_DIR/ikonli-feather-pack-${IKONLI_VERSION}.jar" \
    "${MAVEN_BASE}/ikonli-feather-pack/${IKONLI_VERSION}/ikonli-feather-pack-${IKONLI_VERSION}.jar"

echo ""
echo "✅ Ikonli installation complete!"
echo ""
echo "Available icon packs:"
echo "  • Material Design 2 (mdi2)"
echo "  • Feather Icons (feather)"
echo ""
echo "Usage in FXML:"
echo '  <FontIcon iconLiteral="mdi2h-home" iconSize="20"/>'
echo ""
echo "Usage in Java:"
echo '  FontIcon icon = new FontIcon("mdi2h-home");'
echo '  icon.setIconSize(20);'
echo ""

