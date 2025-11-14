#!/usr/bin/env python3
"""
Script to remove all LangChain4j dependencies from the codebase.
"""
import re
import os
from pathlib import Path

def remove_langchain4j_from_chunking_config(filepath):
    """Remove LangChain4j references from ChunkingConfig.java"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Remove import
    content = re.sub(r'import com\.noteflix\.pcm\.rag\.chunking\.langchain4j\..*?\n', '', content)
    
    # Remove field
    content = re.sub(
        r'  /\*\* Configuration for LangChain4j splitters.*?\n.*?@Builder\.Default\n.*?private com\.noteflix\.pcm\.rag\.chunking\.langchain4j\.LangChain4jConfig langChain4jConfig = \n.*?com\.noteflix\.pcm\.rag\.chunking\.langchain4j\.LangChain4jConfig\.defaults\(\);\n\n',
        '',
        content,
        flags=re.DOTALL
    )
    
    # Remove factory methods section
    content = re.sub(
        r'  // === LangChain4j Factory Methods ===.*?(?=  // ===|\Z)',
        '',
        content,
        flags=re.DOTALL
    )
    
    # Remove enum values
    content = re.sub(
        r',\s*\n\s*// LangChain4j real splitters\n.*?LANGCHAIN4J_HIERARCHICAL\("LangChain4j hierarchical document splitter"\)',
        '',
        content,
        flags=re.DOTALL
    )
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"[OK] Cleaned {filepath}")

def remove_langchain4j_from_chunking_factory(filepath):
    """Remove LangChain4j references from ChunkingFactory.java"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Remove recommendations array entries
    for i in range(8, 15):
        content = re.sub(
            rf'    recommendations\[{i}\] = evaluateStrategy\(ChunkingConfig\.ChunkingStrategyType\.LANGCHAIN4J_.*?;\n',
            '',
            content
        )
    
    # Remove use case mappings
    content = re.sub(
        r'      case LANGCHAIN4J_.*? -> ChunkingConfig\.forLangChain4j.*?\(\).*?;\n',
        '',
        content
    )
    
    # Remove already disabled case block (the one with throw UnsupportedOperationException)
    content = re.sub(
        r'      // LangChain4j strategies.*?/\*\n.*?\*/\n',
        '',
        content,
        flags=re.DOTALL
    )
    
    # Remove use case enum values
    content = re.sub(
        r',\n\s*LANGCHAIN4J_.*?\(".*?"\)',
        '',
        content
    )
    
    # Fix trailing comma/semicolon if needed
    content = re.sub(r',(\s*;)', r'\1', content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"[OK] Cleaned {filepath}")

def main():
    project_root = Path(__file__).parent.parent
    
    config_file = project_root / "src/main/java/com/noteflix/pcm/rag/chunking/core/ChunkingConfig.java"
    factory_file = project_root / "src/main/java/com/noteflix/pcm/rag/chunking/core/ChunkingFactory.java"
    
    if config_file.exists():
        remove_langchain4j_from_chunking_config(config_file)
    
    if factory_file.exists():
        remove_langchain4j_from_chunking_factory(factory_file)
    
    print("\n[SUCCESS] Successfully removed all LangChain4j dependencies!")

if __name__ == "__main__":
    main()

