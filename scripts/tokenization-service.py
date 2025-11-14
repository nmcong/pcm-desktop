#!/usr/bin/env python3
"""
Lightweight tokenization service for models without fast tokenizer.
Java calls this service to tokenize, then runs ONNX inference itself.

Usage:
  # Start server
  python3 scripts/tokenization-service.py --port 5000

  # From Java
  POST http://localhost:5000/tokenize
  Body: {"text": "Xin chào", "model": "vietnamese-sbert"}
  Response: {"input_ids": [...], "attention_mask": [...]}
"""

import argparse
import json
from flask import Flask, request, jsonify
from transformers import AutoTokenizer
import logging

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)

# Cache tokenizers
tokenizers = {}

def load_tokenizer(model_name):
    """Load and cache tokenizer"""
    if model_name not in tokenizers:
        logging.info(f"Loading tokenizer: {model_name}")
        tokenizers[model_name] = AutoTokenizer.from_pretrained(f"data/models/{model_name}")
    return tokenizers[model_name]

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({"status": "ok", "loaded_models": list(tokenizers.keys())})

@app.route('/tokenize', methods=['POST'])
def tokenize():
    """Tokenize text"""
    try:
        data = request.json
        text = data.get('text')
        model = data.get('model', 'vietnamese-sbert')
        
        if not text:
            return jsonify({"error": "Missing 'text' field"}), 400
        
        # Load tokenizer
        tokenizer = load_tokenizer(model)
        
        # Tokenize
        result = tokenizer(
            text,
            padding=True,
            truncation=True,
            max_length=512,
            return_tensors=None  # Return lists, not tensors
        )
        
        return jsonify({
            "input_ids": result['input_ids'],
            "attention_mask": result['attention_mask'],
            "token_count": len(result['input_ids'])
        })
        
    except Exception as e:
        logging.error(f"Tokenization error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/batch_tokenize', methods=['POST'])
def batch_tokenize():
    """Tokenize multiple texts"""
    try:
        data = request.json
        texts = data.get('texts', [])
        model = data.get('model', 'vietnamese-sbert')
        
        if not texts:
            return jsonify({"error": "Missing 'texts' field"}), 400
        
        # Load tokenizer
        tokenizer = load_tokenizer(model)
        
        # Tokenize batch
        result = tokenizer(
            texts,
            padding=True,
            truncation=True,
            max_length=512,
            return_tensors=None
        )
        
        return jsonify({
            "input_ids": result['input_ids'],
            "attention_mask": result['attention_mask'],
            "batch_size": len(texts)
        })
        
    except Exception as e:
        logging.error(f"Batch tokenization error: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Tokenization Service')
    parser.add_argument('--port', type=int, default=5000, help='Port number')
    parser.add_argument('--host', default='127.0.0.1', help='Host address')
    args = parser.parse_args()
    
    print("═══════════════════════════════════════════════════════════════")
    print("  Tokenization Service")
    print("═══════════════════════════════════════════════════════════════")
    print(f"  Host: {args.host}")
    print(f"  Port: {args.port}")
    print("  Endpoints:")
    print(f"    - GET  {args.host}:{args.port}/health")
    print(f"    - POST {args.host}:{args.port}/tokenize")
    print(f"    - POST {args.host}:{args.port}/batch_tokenize")
    print("═══════════════════════════════════════════════════════════════")
    print("")
    
    app.run(host=args.host, port=args.port, debug=False)

