#!/bin/bash

# PCM Desktop SSO Integration Demo Runner
# ========================================

echo "🔐 PCM Desktop SSO Integration Demo"
echo "===================================="

# Get the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

echo "📁 Compiling SSO components..."

# Compile SSO classes
javac -cp "out:lib/others/*" \
    -d out \
    src/main/java/com/noteflix/pcm/core/auth/*.java \
    src/main/java/com/noteflix/pcm/examples/SSOIntegrationDemo.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed"
    exit 1
fi

echo "✅ Compilation successful"
echo ""
echo "🎯 Running SSO Integration Demo..."
echo ""
echo "📝 This demo will show you how to:"
echo "  • Extract tokens from browser cookies, localStorage, registry, and files"
echo "  • Manage token caching and expiration"
echo "  • Integrate with SSO systems for automatic authentication"
echo ""

# Create logs directory if it doesn't exist
mkdir -p logs

# Run the demo
java -cp "out:lib/others/*" com.noteflix.pcm.examples.SSOIntegrationDemo

echo ""
echo "👋 SSO Integration Demo completed!"
echo ""
echo "📚 For more information, see:"
echo "  • docs/SSO_INTEGRATION_GUIDE.md - Complete SSO integration guide"
echo "  • logs/security-audit.log - Security audit events"