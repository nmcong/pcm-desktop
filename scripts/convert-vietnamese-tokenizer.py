#!/usr/bin/env python3
"""
Convert Vietnamese model tokenizer to tokenizer.json format.

The Vietnamese model uses old format (vocab.txt + bpe.codes)
but DJL requires tokenizer.json format.
"""

import json
import sys
from pathlib import Path

def convert_tokenizer(model_dir):
    """Convert old-format tokenizer to tokenizer.json"""
    
    model_path = Path(model_dir)
    
    # Check if tokenizer.json already exists
    tokenizer_json = model_path / "tokenizer.json"
    if tokenizer_json.exists():
        print(f"✅ tokenizer.json already exists at {tokenizer_json}")
        return True
    
    # Check for required files
    vocab_file = model_path / "vocab.txt"
    if not vocab_file.exists():
        print(f"❌ vocab.txt not found at {model_path}")
        return False
    
    print(f"📥 Converting tokenizer for: {model_path}")
    print(f"   Found vocab.txt with {sum(1 for _ in open(vocab_file))} tokens")
    
    try:
        from transformers import AutoTokenizer
        
        # Load tokenizer from directory
        print("   Loading tokenizer from model files...")
        tokenizer = AutoTokenizer.from_pretrained(str(model_path))
        
        # Save as tokenizer.json
        print(f"   Saving tokenizer.json...")
        tokenizer.save_pretrained(str(model_path))
        
        # Verify
        if tokenizer_json.exists():
            print(f"✅ Successfully created tokenizer.json")
            print(f"   Location: {tokenizer_json}")
            return True
        else:
            print(f"❌ Failed to create tokenizer.json")
            return False
            
    except ImportError:
        print("❌ transformers library not found. Install with:")
        print("   pip3 install transformers")
        return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 convert-vietnamese-tokenizer.py <model_dir>")
        print("")
        print("Example:")
        print("  python3 scripts/convert-vietnamese-tokenizer.py data/models/vietnamese-sbert")
        sys.exit(1)
    
    model_dir = sys.argv[1]
    
    print("═══════════════════════════════════════════════════════════════")
    print("  Vietnamese Tokenizer Converter")
    print("═══════════════════════════════════════════════════════════════")
    print("")
    
    success = convert_tokenizer(model_dir)
    
    print("")
    print("═══════════════════════════════════════════════════════════════")
    if success:
        print("  ✅ Conversion Complete")
        print("═══════════════════════════════════════════════════════════════")
        print("")
        print("Next steps:")
        print("  1. Rebuild project: ./scripts/build.sh")
        print("  2. Run demo: java -cp 'out:lib/*' \\")
        print("              com.noteflix.pcm.rag.examples.VietnameseEnglishEmbeddingDemo")
    else:
        print("  ❌ Conversion Failed")
        print("═══════════════════════════════════════════════════════════════")
        sys.exit(1)

if __name__ == "__main__":
    main()

