#!/bin/bash

# Script để copy icons từ src/main/resources/images/icons vào build directory (out)

# Change to project root directory (parent of scripts folder)
cd "$(dirname "$0")/.."

echo "📋 Copying icons from src to build directory..."
echo "=================================================="

# Định nghĩa đường dẫn
SRC_DIR="src/main/resources/images/icons"
BUILD_DIR="out/images/icons"

# Kiểm tra thư mục source
if [ ! -d "$SRC_DIR" ]; then
    echo "❌ Source directory không tồn tại: $SRC_DIR"
    exit 1
fi

# Tạo thư mục build nếu chưa tồn tại
mkdir -p "$BUILD_DIR"

# Đếm số files trong source
src_files=$(find "$SRC_DIR" -type f \( -name "*.png" -o -name "*.svg" \) | wc -l | tr -d ' ')

if [ "$src_files" -eq 0 ]; then
    echo "⚠️  Không có icon files nào trong $SRC_DIR"
    exit 0
fi

echo "📂 Source: $SRC_DIR"
echo "📁 Destination: $BUILD_DIR"
echo "📊 Found $src_files icon files"
echo ""

# Copy các file icons
echo "🔄 Copying icon files..."

# Copy PNG files
png_count=$(find "$SRC_DIR" -name "*.png" | wc -l | tr -d ' ')
if [ "$png_count" -gt 0 ]; then
    cp "$SRC_DIR"/*.png "$BUILD_DIR/" 2>/dev/null
    echo "✅ Copied $png_count PNG files"
fi

# Copy SVG files  
svg_count=$(find "$SRC_DIR" -name "*.svg" | wc -l | tr -d ' ')
if [ "$svg_count" -gt 0 ]; then
    cp "$SRC_DIR"/*.svg "$BUILD_DIR/" 2>/dev/null
    echo "✅ Copied $svg_count SVG files"
fi

# Copy other image files (ico, jpg, etc.)
other_count=$(find "$SRC_DIR" -type f \( -name "*.ico" -o -name "*.jpg" -o -name "*.jpeg" -o -name "*.gif" \) | wc -l | tr -d ' ')
if [ "$other_count" -gt 0 ]; then
    cp "$SRC_DIR"/*.ico "$BUILD_DIR/" 2>/dev/null
    cp "$SRC_DIR"/*.jpg "$BUILD_DIR/" 2>/dev/null  
    cp "$SRC_DIR"/*.jpeg "$BUILD_DIR/" 2>/dev/null
    cp "$SRC_DIR"/*.gif "$BUILD_DIR/" 2>/dev/null
    echo "✅ Copied $other_count other image files"
fi

# Verify copy operation
build_files=$(find "$BUILD_DIR" -type f \( -name "*.png" -o -name "*.svg" -o -name "*.ico" -o -name "*.jpg" -o -name "*.jpeg" \) | wc -l | tr -d ' ')

echo ""
echo "📊 Copy Summary:"
echo "   Source files: $src_files"
echo "   Build files:  $build_files"

if [ "$build_files" -gt 0 ]; then
    echo "✅ Copy operation completed successfully!"
    echo ""
    echo "📁 Files in build directory:"
    ls -la "$BUILD_DIR"
else
    echo "❌ Copy operation failed - no files found in build directory"
    exit 1
fi