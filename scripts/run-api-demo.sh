#!/bin/bash

# PCM Desktop API Integration Demo Runner
# =======================================

echo "🚀 PCM Desktop API Integration Demo"
echo "===================================="

# Check if OPENAI_API_KEY is set
if [ -z "$OPENAI_API_KEY" ]; then
    echo ""
    echo "⚠️  OPENAI_API_KEY environment variable not found!"
    echo ""
    echo "Please set your OpenAI API key first:"
    echo "  export OPENAI_API_KEY=your-api-key-here"
    echo ""
    echo "Or create a .env file in the project root:"
    echo "  echo 'OPENAI_API_KEY=your-api-key-here' > .env"
    echo "  source .env"
    echo ""
    exit 1
fi

echo "✅ API Key found"
echo "📁 Compiling Java sources..."

# Get the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

# Compile the demo
javac -cp "out:lib/others/*:lib/javafx/*" \
    --module-path lib/javafx \
    --add-modules javafx.controls,javafx.fxml \
    -d out \
    src/main/java/com/noteflix/pcm/llm/examples/APIDemo.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed"
    exit 1
fi

echo "✅ Compilation successful"
echo "🎯 Running demo..."
echo ""

# Run the demo
java -cp "out:lib/others/*" com.noteflix.pcm.llm.examples.APIDemo

echo ""
echo "👋 Demo completed. Thanks for trying PCM Desktop API Integration!"