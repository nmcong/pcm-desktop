#!/bin/bash
# =================================================================
# PCM Desktop - IntelliJ IDEA Library Configuration Script
# =================================================================
# Automatically creates library configurations for IntelliJ IDEA
#
# Usage: ./configure-intellij.sh
# =================================================================

set -e

# Change to project root directory
cd "$(dirname "$0")/.."

echo
echo "========================================"
echo "   IntelliJ IDEA Library Configuration"
echo "========================================"
echo

# Create .idea directory if it doesn't exist
mkdir -p .idea/libraries

echo "[INFO] Creating IntelliJ IDEA library configurations..."

# Create JavaFX library
if [ -d "lib/javafx" ]; then
    echo "[INFO] Creating javafx.xml library configuration..."
    cat > .idea/libraries/javafx.xml << 'EOF'
<component name="libraryTable">
  <library name="javafx">
    <CLASSES>
      <root url="file://$PROJECT_DIR$/lib/javafx" />
    </CLASSES>
    <JAVADOC />
    <NATIVE>
      <root url="file://$PROJECT_DIR$/lib/javafx" />
    </NATIVE>
    <SOURCES />
    <jarDirectory url="file://$PROJECT_DIR$/lib/javafx" recursive="false" />
  </library>
</component>
EOF
    echo "[OK] javafx.xml created"
fi

# Create Database library
if [ -d "lib/database" ]; then
    echo "[INFO] Creating database.xml library configuration..."
    cat > .idea/libraries/database.xml << 'EOF'
<component name="libraryTable">
  <library name="database">
    <CLASSES>
      <root url="file://$PROJECT_DIR$/lib/database" />
    </CLASSES>
    <JAVADOC />
    <SOURCES />
    <jarDirectory url="file://$PROJECT_DIR$/lib/database" recursive="false" />
  </library>
</component>
EOF
    echo "[OK] database.xml created"
fi

# Create Logs library
if [ -d "lib/logs" ]; then
    echo "[INFO] Creating logs.xml library configuration..."
    cat > .idea/libraries/logs.xml << 'EOF'
<component name="libraryTable">
  <library name="logs">
    <CLASSES>
      <root url="file://$PROJECT_DIR$/lib/logs" />
    </CLASSES>
    <JAVADOC />
    <SOURCES />
    <jarDirectory url="file://$PROJECT_DIR$/lib/logs" recursive="false" />
  </library>
</component>
EOF
    echo "[OK] logs.xml created"
fi

# Create Utils library
if [ -d "lib/utils" ]; then
    echo "[INFO] Creating utils.xml library configuration..."
    cat > .idea/libraries/utils.xml << 'EOF'
<component name="libraryTable">
  <library name="utils">
    <CLASSES>
      <root url="file://$PROJECT_DIR$/lib/utils" />
    </CLASSES>
    <JAVADOC />
    <SOURCES />
    <jarDirectory url="file://$PROJECT_DIR$/lib/utils" recursive="false" />
  </library>
</component>
EOF
    echo "[OK] utils.xml created"
fi

# Create UI library
if [ -d "lib/ui" ]; then
    echo "[INFO] Creating ui.xml library configuration..."
    cat > .idea/libraries/ui.xml << 'EOF'
<component name="libraryTable">
  <library name="ui">
    <CLASSES>
      <root url="file://$PROJECT_DIR$/lib/ui" />
    </CLASSES>
    <JAVADOC />
    <SOURCES />
    <jarDirectory url="file://$PROJECT_DIR$/lib/ui" recursive="false" />
  </library>
</component>
EOF
    echo "[OK] ui.xml created"
fi

# Create Icons library
if [ -d "lib/icons" ]; then
    echo "[INFO] Creating icons.xml library configuration..."
    cat > .idea/libraries/icons.xml << 'EOF'
<component name="libraryTable">
  <library name="icons">
    <CLASSES>
      <root url="file://$PROJECT_DIR$/lib/icons" />
    </CLASSES>
    <JAVADOC />
    <SOURCES />
    <jarDirectory url="file://$PROJECT_DIR$/lib/icons" recursive="false" />
  </library>
</component>
EOF
    echo "[OK] icons.xml created"
fi

# Create RAG library
if [ -d "lib/rag" ]; then
    echo "[INFO] Creating rag.xml library configuration..."
    cat > .idea/libraries/rag.xml << 'EOF'
<component name="libraryTable">
  <library name="rag">
    <CLASSES>
      <root url="file://$PROJECT_DIR$/lib/rag" />
    </CLASSES>
    <JAVADOC />
    <SOURCES />
    <jarDirectory url="file://$PROJECT_DIR$/lib/rag" recursive="false" />
  </library>
</component>
EOF
    echo "[OK] rag.xml created"
fi

echo
echo "[SUCCESS] IntelliJ IDEA library configurations created successfully!"
echo
echo "[INFO] Library files created in .idea/libraries/:"
ls -1 .idea/libraries/*.xml 2>/dev/null || echo "  No library files found"
echo
echo "[INFO] To use these libraries in IntelliJ IDEA:"
echo "  1. Open/Reload project in IntelliJ IDEA"
echo "  2. Go to Project Structure (Ctrl+Alt+Shift+S)"
echo "  3. Libraries will be automatically detected"
echo "  4. Add them to your module dependencies as needed"
echo