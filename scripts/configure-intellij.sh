#!/bin/bash
# =================================================================
# PCM Desktop - IntelliJ IDEA Library Configuration Script
# =================================================================
# Adds library entries to existing .idea/pcm-desktop.iml file
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

echo "[INFO] Configuring IntelliJ IDEA libraries..."

# Define the libraries we want to add
declare -a LIBRARIES=()

# Check which library folders exist and add them to the list
if [ -d "lib/database" ]; then
    LIBRARIES+=("database")
fi

if [ -d "lib/icons" ]; then
    LIBRARIES+=("icons")
fi

if [ -d "lib/javafx" ]; then
    LIBRARIES+=("javafx")
fi

if [ -d "lib/logs" ]; then
    LIBRARIES+=("logs")
fi

if [ -d "lib/rag" ]; then
    LIBRARIES+=("rag")
fi

if [ -d "lib/ui" ]; then
    LIBRARIES+=("ui")
fi

if [ -d "lib/utils" ]; then
    LIBRARIES+=("utils")
fi

# Create library XML files
for lib in "${LIBRARIES[@]}"; do
    echo "[INFO] Creating ${lib}.xml library configuration..."
    cat > .idea/libraries/${lib}.xml << EOF
<component name="libraryTable">
  <library name="${lib}">
    <CLASSES>
      <root url="file://\$PROJECT_DIR\$/lib/${lib}" />
    </CLASSES>
    <JAVADOC />
EOF

    # Add NATIVE section only for JavaFX
    if [ "$lib" = "javafx" ]; then
        cat >> .idea/libraries/${lib}.xml << EOF
    <NATIVE>
      <root url="file://\$PROJECT_DIR\$/lib/${lib}" />
    </NATIVE>
EOF
    fi

    cat >> .idea/libraries/${lib}.xml << EOF
    <SOURCES />
    <jarDirectory url="file://\$PROJECT_DIR\$/lib/${lib}" recursive="false" />
  </library>
</component>
EOF
    echo "[OK] ${lib}.xml created"
done

# Check if .idea/pcm-desktop.iml exists
IML_FILE=".idea/pcm-desktop.iml"

if [ ! -f "$IML_FILE" ]; then
    echo "[WARNING] File $IML_FILE not found!"
    echo "[INFO] Please ensure your project is opened in IntelliJ IDEA first."
    echo "[INFO] Library configurations have been created in .idea/libraries/"
else
    echo "[INFO] Updating $IML_FILE with library dependencies..."
    
    # Create temporary file for modification
    TEMP_FILE=$(mktemp)
    
    # Check if libraries are already present to avoid duplicates
    LIBRARIES_TO_ADD=()
    for lib in "${LIBRARIES[@]}"; do
        if ! grep -q "name=\"$lib\"" "$IML_FILE"; then
            LIBRARIES_TO_ADD+=("$lib")
        fi
    done
    
    if [ ${#LIBRARIES_TO_ADD[@]} -eq 0 ]; then
        echo "[INFO] All libraries already present in $IML_FILE"
    else
        # Add library entries after the sourceFolder line
        while IFS= read -r line; do
            echo "$line" >> "$TEMP_FILE"
            
            # If we find the sourceFolder line, add library entries after it
            if [[ "$line" == *'<orderEntry type="sourceFolder"'* ]]; then
                for lib in "${LIBRARIES_TO_ADD[@]}"; do
                    echo "    <orderEntry type=\"library\" name=\"$lib\" level=\"project\" />" >> "$TEMP_FILE"
                done
            fi
        done < "$IML_FILE"
        
        # Replace original file with modified one
        mv "$TEMP_FILE" "$IML_FILE"
        echo "[OK] $IML_FILE updated with library dependencies"
    fi
fi

echo
echo "[SUCCESS] IntelliJ IDEA configuration completed!"
echo
echo "[INFO] Files created/updated:"
for lib in "${LIBRARIES[@]}"; do
    echo "  - .idea/libraries/${lib}.xml"
done
if [ -f "$IML_FILE" ]; then
    echo "  - $IML_FILE (updated with library dependencies)"
fi
echo
echo "[INFO] To apply changes in IntelliJ IDEA:"
echo "  1. Refresh project (Ctrl+Shift+F5 or File -> Reload Gradle Project)"
echo "  2. Or restart IntelliJ IDEA"
echo "  3. Libraries will be automatically available"
echo