#!/bin/bash

# Script để copy tất cả resources từ src/main/resources vào build directory (out)

# Change to project root directory (parent of scripts folder)
cd "$(dirname "$0")/.."

echo "🔧 Copying all resources from src to build directory..."
echo "=================================================="

# Định nghĩa đường dẫn
PROJECT_ROOT="$(pwd)"
SRC_RESOURCES="src/main/resources"
BUILD_DIR="out"

# Kiểm tra thư mục source
if [ ! -d "$SRC_RESOURCES" ]; then
    echo "❌ Source resources directory không tồn tại: $SRC_RESOURCES"
    exit 1
fi

# Tạo thư mục build nếu chưa tồn tại
mkdir -p "$BUILD_DIR"

echo "📂 Source: $SRC_RESOURCES"
echo "📁 Build: $BUILD_DIR"
echo ""

# Function để copy với progress
copy_with_progress() {
    local src="$1"
    local dst="$2"
    local desc="$3"
    
    if [ -d "$src" ]; then
        echo "🔄 Copying $desc..."
        mkdir -p "$dst"
        
        # Copy và đếm files
        file_count=$(find "$src" -type f | wc -l | tr -d ' ')
        if [ "$file_count" -gt 0 ]; then
            cp -r "$src"/* "$dst/" 2>/dev/null
            echo "✅ Copied $file_count files from $desc"
        else
            echo "⚠️  No files found in $desc"
        fi
    else
        echo "⚠️  Directory not found: $src"
    fi
}

# Copy CSS files
copy_with_progress "$SRC_RESOURCES/css" "$BUILD_DIR/css" "CSS files"

# Copy FXML files  
copy_with_progress "$SRC_RESOURCES/fxml" "$BUILD_DIR/fxml" "FXML files"

# Copy images and icons
copy_with_progress "$SRC_RESOURCES/images" "$BUILD_DIR/images" "Images and icons"

# Copy other resources (logback.xml, properties, etc.)
echo "🔄 Copying other resource files..."
other_files=$(find "$SRC_RESOURCES" -maxdepth 1 -type f | wc -l | tr -d ' ')
if [ "$other_files" -gt 0 ]; then
    cp "$SRC_RESOURCES"/*.xml "$BUILD_DIR/" 2>/dev/null
    cp "$SRC_RESOURCES"/*.properties "$BUILD_DIR/" 2>/dev/null
    cp "$SRC_RESOURCES"/*.json "$BUILD_DIR/" 2>/dev/null
    cp "$SRC_RESOURCES"/*.txt "$BUILD_DIR/" 2>/dev/null
    echo "✅ Copied $other_files other resource files"
else
    echo "⚠️  No other resource files found"
fi

echo ""
echo "📊 Copy Summary:"
echo "================================"

# Hiển thị kết quả
for subdir in css fxml images; do
    src_count=$(find "$SRC_RESOURCES/$subdir" -type f 2>/dev/null | wc -l | tr -d ' ')
    build_count=$(find "$BUILD_DIR/$subdir" -type f 2>/dev/null | wc -l | tr -d ' ')
    
    if [ "$src_count" -gt 0 ]; then
        echo "📁 $subdir: $src_count → $build_count files"
    fi
done

# Hiển thị tổng số files
total_src=$(find "$SRC_RESOURCES" -type f | wc -l | tr -d ' ')
total_build=$(find "$BUILD_DIR" -type f \( -name "*.css" -o -name "*.fxml" -o -name "*.png" -o -name "*.svg" -o -name "*.xml" -o -name "*.properties" \) | wc -l | tr -d ' ')

echo ""
echo "📊 Total files:"
echo "   Source: $total_src files"
echo "   Build:  $total_build files"

if [ "$total_build" -gt 0 ]; then
    echo ""
    echo "✅ Resources copied successfully to build directory!"
    echo ""
    echo "📁 Build directory structure:"
    tree "$BUILD_DIR" 2>/dev/null || find "$BUILD_DIR" -type d | sed 's/[^-][^\/]*\//  /g;s/^/📁 /;s/-/|/'
else
    echo ""
    echo "❌ No files were copied to build directory"
    exit 1
fi