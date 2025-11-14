# Resources and References - Multilingual Embeddings & RAG

> 📚 **Comprehensive guide** to resources for multilingual embeddings, RAG systems, and DJL  
> 🎯 **Focus:** Vietnamese & English semantic search with Java  
> 📅 **Updated:** November 14, 2024

---

## 🎯 MULTILINGUAL EMBEDDINGS

### Sentence Transformers (⭐ Best Resource!)

**Official Resources:**
- **Website:** https://www.sbert.net/
- **GitHub:** https://github.com/UKPLab/sentence-transformers
- **Documentation:** https://www.sbert.net/docs/
- **Pretrained Models:** https://www.sbert.net/docs/pretrained_models.html

**What it is:**
- State-of-the-art framework for sentence embeddings
- 100+ pretrained multilingual models
- Easy to use for semantic search
- Industry standard for embeddings

**Key Models:**
- **LaBSE:** https://huggingface.co/sentence-transformers/LaBSE (109 languages)
- **Multilingual E5:** https://huggingface.co/intfloat/multilingual-e5-base
- **Paraphrase Multilingual:** https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2

**Tutorials:**
- Getting Started: https://www.sbert.net/docs/quickstart.html
- Semantic Search: https://www.sbert.net/examples/applications/semantic-search/README.html
- Training Custom Models: https://www.sbert.net/docs/training/overview.html

---

### MTEB Leaderboard (Model Quality Rankings)

**Main Leaderboard:**
- **URL:** https://huggingface.co/spaces/mteb/leaderboard
- **Use:** Compare embedding models across 58 datasets
- **Metrics:** Retrieval, clustering, classification, etc.

**Vietnamese Models:**
- **vietnamese-sbert:** https://huggingface.co/keepitreal/vietnamese-sbert (768d, PhoBERT-based)
- **vinai/phobert-base:** https://huggingface.co/vinai/phobert-base-v2

**English Models:**
- **BAAI/bge-m3:** https://huggingface.co/BAAI/bge-m3 (1024d, MTEB #1)
- **all-mpnet-base-v2:** https://huggingface.co/sentence-transformers/all-mpnet-base-v2 (768d)
- **e5-large-v2:** https://huggingface.co/intfloat/e5-large-v2 (1024d)

**Search Models:**
- By Language: https://huggingface.co/models?pipeline_tag=sentence-similarity&language=vi
- By Library: https://huggingface.co/models?library=sentence-transformers
- By Task: https://huggingface.co/models?pipeline_tag=feature-extraction

---

## 🔥 DJL (Deep Java Library)

### Official Resources

**Main Website:**
- **Homepage:** https://djl.ai/
- **GitHub:** https://github.com/deepjavalibrary/djl
- **Documentation:** https://docs.djl.ai/
- **JavaDoc:** https://javadoc.io/doc/ai.djl/api/latest/index.html

**Getting Started:**
- Quick Start: https://docs.djl.ai/docs/quick_start.html
- Tutorials: https://docs.djl.ai/docs/demos/jupyter/index.html
- Examples: https://github.com/deepjavalibrary/djl/tree/master/examples

---

### DJL Engines

**ONNX Runtime Engine:**
- **Docs:** https://docs.djl.ai/engines/onnxruntime/onnxruntime-engine/index.html
- **Maven:** https://mvnrepository.com/artifact/ai.djl.onnxruntime/onnxruntime-engine
- **Use:** Lightweight, optimized inference, best for CPU
- **GitHub:** https://github.com/deepjavalibrary/djl/tree/master/engines/onnxruntime

**PyTorch Engine:**
- **Docs:** https://docs.djl.ai/engines/pytorch/pytorch-engine/index.html
- **Maven:** https://mvnrepository.com/artifact/ai.djl.pytorch/pytorch-engine
- **Use:** Full PyTorch features, GPU support
- **GitHub:** https://github.com/deepjavalibrary/djl/tree/master/engines/pytorch

**Engine Comparison:**
- https://docs.djl.ai/docs/engine.html
- https://github.com/deepjavalibrary/djl/blob/master/docs/development/inference_performance_optimization.md

---

### DJL Examples

**Text & NLP:**
- Sentence Similarity: https://github.com/deepjavalibrary/djl/blob/master/examples/docs/sentence_similarity.md
- Text Embedding: https://github.com/deepjavalibrary/djl/tree/master/examples/src/main/java/ai/djl/examples/inference/nlp
- Question Answering: https://github.com/deepjavalibrary/djl/blob/master/examples/docs/question_answering.md

**Model Loading:**
- HuggingFace Models: https://docs.djl.ai/docs/load_model.html#huggingface-models
- Model Zoo: https://docs.djl.ai/docs/model-zoo.html
- Custom Models: https://docs.djl.ai/docs/load_model.html

**Performance:**
- Optimization Guide: https://docs.djl.ai/docs/development/inference_performance_optimization.html
- Benchmarks: https://docs.djl.ai/docs/development/benchmark.html
- Memory Management: https://docs.djl.ai/docs/development/memory_management.html

---

### DJL Community

**Discussion & Support:**
- Slack: https://deepjavalibrary.slack.com/
- GitHub Issues: https://github.com/deepjavalibrary/djl/issues
- Stack Overflow: https://stackoverflow.com/questions/tagged/djl

**Demos & Projects:**
- DJL Demo: https://github.com/deepjavalibrary/djl-demo
- Spring Boot Integration: https://github.com/deepjavalibrary/djl-spring-boot-starter
- Android Examples: https://github.com/deepjavalibrary/djl/tree/master/android

---

## 🌍 VIETNAMESE NLP

### PhoBERT (Vietnamese BERT)

**Official Resources:**
- **Paper:** https://arxiv.org/abs/2003.00744 ("PhoBERT: Pre-trained language models for Vietnamese")
- **GitHub:** https://github.com/VinAIResearch/PhoBERT
- **Models:** 
  - PhoBERT-base: https://huggingface.co/vinai/phobert-base
  - PhoBERT-large: https://huggingface.co/vinai/phobert-large
  - PhoBERT-base-v2: https://huggingface.co/vinai/phobert-base-v2

**Key Features:**
- First large-scale Vietnamese language model
- Trained on 20GB Vietnamese text
- State-of-the-art for Vietnamese NLP tasks
- BPE tokenization (bpe.codes)

**Citation:**
```bibtex
@inproceedings{phobert,
  title={PhoBERT: Pre-trained language models for Vietnamese},
  author={Nguyen, Dat Quoc and Nguyen, Anh Tuan},
  booktitle={Findings of EMNLP},
  year={2020}
}
```

---

### Vietnamese Embedding Models

**Recommended Models:**

1. **vietnamese-sbert (keepitreal)** ⭐
   - **HuggingFace:** https://huggingface.co/keepitreal/vietnamese-sbert
   - **Base:** PhoBERT
   - **Dimension:** 768
   - **Format:** PyTorch (vocab.txt + bpe.codes)
   - **Use:** Best Vietnamese sentence embeddings

2. **vinai/phobert-base-v2**
   - **HuggingFace:** https://huggingface.co/vinai/phobert-base-v2
   - **Dimension:** 768
   - **Use:** Latest PhoBERT version

3. **VoVanPhuc/sup-SimCSE-VietNamese-phobert-base**
   - **HuggingFace:** https://huggingface.co/VoVanPhuc/sup-SimCSE-VietNamese-phobert-base
   - **Approach:** SimCSE (contrastive learning)
   - **Dimension:** 768

**Comparison:**
- Paper with Code: https://paperswithcode.com/task/sentence-embeddings
- Vietnamese NLP Benchmarks: https://github.com/VinAIResearch/JointIDSF

---

### Vietnamese NLP Tools

**VnCoreNLP:**
- **GitHub:** https://github.com/vncorenlp/VnCoreNLP
- **Use:** Word segmentation, POS tagging, NER
- **Java library available**

**Underthesea:**
- **GitHub:** https://github.com/undertheseanlp/underthesea
- **Website:** https://underthesea.readthedocs.io/
- **Use:** Complete Vietnamese NLP toolkit (Python)

**Vietnamese Datasets:**
- VLSP Shared Tasks: http://vlsp.org.vn/
- Vietnamese Wikipedia: https://vi.wikipedia.org/
- Vietnamese News Corpus: https://github.com/binhvq/news-corpus

---

## 📊 RAG (Retrieval-Augmented Generation)

### LangChain4j (Java RAG Framework)

**Official Resources:**
- **Website:** https://docs.langchain4j.dev/
- **GitHub:** https://github.com/langchain4j/langchain4j
- **Examples:** https://github.com/langchain4j/langchain4j-examples

**Key Features:**
- Complete RAG framework for Java
- Integration with 15+ LLM providers
- Vector store support (Pinecone, Weaviate, etc.)
- Document loaders & splitters

**RAG Tutorial:**
- Quick Start: https://docs.langchain4j.dev/tutorials/rag
- Embedding Stores: https://docs.langchain4j.dev/tutorials/rag#2-embedding-store
- Retrieval Strategies: https://docs.langchain4j.dev/tutorials/rag#retrieval

**Code Examples:**
```java
// LangChain4j RAG example
https://github.com/langchain4j/langchain4j-examples/tree/main/rag-examples
```

---

### RAG Concepts & Best Practices

**Comprehensive Guides:**
- **Pinecone RAG Guide:** https://www.pinecone.io/learn/retrieval-augmented-generation/
- **LlamaIndex Docs:** https://docs.llamaindex.ai/en/stable/
- **Weaviate RAG:** https://weaviate.io/developers/weaviate/tutorials/rag

**Key Concepts:**
- Chunking Strategies: https://www.pinecone.io/learn/chunking-strategies/
- Vector Databases: https://www.pinecone.io/learn/vector-database/
- Hybrid Search: https://www.pinecone.io/learn/hybrid-search/
- Re-ranking: https://www.pinecone.io/learn/series/rag/reranking/

**Academic Papers:**
- RAG Paper (2020): https://arxiv.org/abs/2005.11401
- Self-RAG (2023): https://arxiv.org/abs/2310.11511
- RAPTOR (2024): https://arxiv.org/abs/2401.18059

---

### Vector Databases

**Popular Options:**

1. **Pinecone**
   - Website: https://www.pinecone.io/
   - Docs: https://docs.pinecone.io/
   - Java Client: https://github.com/pinecone-io/pinecone-java-client

2. **Weaviate**
   - Website: https://weaviate.io/
   - Docs: https://weaviate.io/developers/weaviate
   - Java Client: https://github.com/weaviate/java-client

3. **Qdrant**
   - Website: https://qdrant.tech/
   - Docs: https://qdrant.tech/documentation/
   - Java Client: https://github.com/qdrant/java-client

4. **Milvus**
   - Website: https://milvus.io/
   - Docs: https://milvus.io/docs
   - Java SDK: https://github.com/milvus-io/milvus-sdk-java

5. **ChromaDB**
   - Website: https://www.trychroma.com/
   - GitHub: https://github.com/chroma-core/chroma

**Comparison:**
- https://www.pinecone.io/learn/vector-database-comparison/
- https://weaviate.io/blog/vector-database-comparison

---

## 🔧 ONNX RUNTIME

### Official Resources

**Main Website:**
- **Homepage:** https://onnxruntime.ai/
- **GitHub:** https://github.com/microsoft/onnxruntime
- **Documentation:** https://onnxruntime.ai/docs/

**API Documentation:**
- Java API: https://onnxruntime.ai/docs/api/java/
- Python API: https://onnxruntime.ai/docs/api/python/
- C++ API: https://onnxruntime.ai/docs/api/c/

**Getting Started:**
- Quick Start: https://onnxruntime.ai/docs/get-started/
- Installation: https://onnxruntime.ai/docs/install/
- Tutorials: https://onnxruntime.ai/docs/tutorials/

---

### Model Conversion

**HuggingFace Optimum (Best Tool):**
- **Docs:** https://huggingface.co/docs/optimum/
- **GitHub:** https://github.com/huggingface/optimum
- **ONNX Export:** https://huggingface.co/docs/optimum/exporters/onnx/usage_guides/export_a_model

**Export Examples:**
```python
# Export any HuggingFace model to ONNX
optimum-cli export onnx --model <model_name> <output_dir>
```

**Conversion Guides:**
- PyTorch to ONNX: https://pytorch.org/docs/stable/onnx.html
- TensorFlow to ONNX: https://github.com/onnx/tensorflow-onnx
- Model Optimization: https://huggingface.co/docs/optimum/onnxruntime/usage_guides/optimization

**ONNX Model Zoo:**
- **Repository:** https://github.com/onnx/models
- **Pre-converted models:** Vision, NLP, Speech

---

### Performance Optimization

**Optimization Guides:**
- Performance Tuning: https://onnxruntime.ai/docs/performance/
- Quantization: https://onnxruntime.ai/docs/performance/quantization.html
- Graph Optimization: https://onnxruntime.ai/docs/performance/graph-optimizations.html

**Execution Providers:**
- CPU: https://onnxruntime.ai/docs/execution-providers/CPU-ExecutionProvider.html
- CUDA: https://onnxruntime.ai/docs/execution-providers/CUDA-ExecutionProvider.html
- CoreML: https://onnxruntime.ai/docs/execution-providers/CoreML-ExecutionProvider.html

---

## 🎓 TUTORIALS & LEARNING RESOURCES

### Semantic Search Tutorials

**Pinecone University:**
- Semantic Search Course: https://www.pinecone.io/learn/semantic-search/
- Vector Embeddings: https://www.pinecone.io/learn/vector-embeddings/
- Sentence Transformers: https://www.pinecone.io/learn/sentence-embeddings/

**OpenAI Guides:**
- Embeddings Guide: https://platform.openai.com/docs/guides/embeddings
- Use Cases: https://platform.openai.com/docs/guides/embeddings/use-cases
- Best Practices: https://help.openai.com/en/articles/6824809-embeddings-frequently-asked-questions

**Cohere:**
- Semantic Search: https://docs.cohere.com/docs/semantic-search
- Multilingual: https://docs.cohere.com/docs/multilingual-language-models

---

### Multilingual NLP

**Academic Resources:**
- Papers with Code: https://paperswithcode.com/task/cross-lingual-sentence-embedding
- Multilingual BERT: https://github.com/google-research/bert/blob/master/multilingual.md
- XLM-RoBERTa: https://huggingface.co/docs/transformers/model_doc/xlm-roberta

**Google Research:**
- LaBSE Blog: https://ai.googleblog.com/2020/08/language-agnostic-bert-sentence.html
- Multilingual Models: https://github.com/google-research/bert

**Industry Best Practices:**
- Meta AI: https://ai.meta.com/blog/multilingual-model-speech-recognition/
- Microsoft Research: https://www.microsoft.com/en-us/research/project/multilingual-nlp/

---

### Video Tutorials

**YouTube Channels:**
- Sentence Transformers: https://www.youtube.com/results?search_query=sentence+transformers+tutorial
- DJL Tutorials: https://www.youtube.com/c/deepjavalibrary
- RAG Systems: https://www.youtube.com/results?search_query=retrieval+augmented+generation

**Courses:**
- Hugging Face NLP Course: https://huggingface.co/learn/nlp-course/
- Fast.ai NLP: https://www.fast.ai/
- DeepLearning.AI: https://www.deeplearning.ai/courses/

---

## 📦 GITHUB REPOSITORIES

### Java ML & Embeddings

**Production Examples:**
```
https://github.com/deepjavalibrary/djl-demo
https://github.com/langchain4j/langchain4j-examples  
https://github.com/vespa-engine/sample-apps
https://github.com/deepjavalibrary/djl-spring-boot-starter
```

**Semantic Search:**
```
https://github.com/UKPLab/sentence-transformers
https://github.com/nmslib/hnswlib (Fast similarity search)
https://github.com/facebookresearch/faiss (Vector similarity)
```

---

### Multilingual RAG

**Complete Systems:**
```
https://github.com/run-llama/llama_index
https://github.com/hwchase17/langchain
https://github.com/weaviate/weaviate
https://github.com/chroma-core/chroma
```

**RAG Components:**
```
https://github.com/neuml/txtai
https://github.com/jina-ai/jina
https://github.com/deepset-ai/haystack
```

---

### Vietnamese NLP

**Core Libraries:**
```
https://github.com/VinAIResearch/PhoBERT
https://github.com/vncorenlp/VnCoreNLP
https://github.com/undertheseanlp/underthesea
https://github.com/binhvq/news-corpus
```

**Datasets:**
```
https://github.com/XuanKhanh94/Vietnamese-sentiment-dataset
https://github.com/NTT123/WikiDumps (Vietnamese Wikipedia)
https://github.com/sonvx/vietnews (Vietnamese news)
```

---

## 📚 ACADEMIC PAPERS

### Must-Read Papers

**Embeddings:**

1. **Sentence-BERT (2019)** ⭐
   - Paper: https://arxiv.org/abs/1908.10084
   - Code: https://github.com/UKPLab/sentence-transformers
   - Impact: Foundation of modern sentence embeddings

2. **LaBSE (2020)** ⭐⭐⭐
   - Paper: https://arxiv.org/abs/2007.01852
   - Title: "Language-agnostic BERT Sentence Embedding"
   - Impact: Best multilingual model (109 languages)

3. **BGE-M3 (2024)**
   - Paper: https://arxiv.org/abs/2402.03216
   - Title: "BGE M3-Embedding: Multi-Lingual, Multi-Functionality"
   - Impact: SOTA for multilingual embeddings

4. **E5 (2022)**
   - Paper: https://arxiv.org/abs/2212.03533
   - Title: "Text Embeddings by Weakly-Supervised Contrastive Pre-training"
   - Impact: Strong English embeddings

---

**Vietnamese NLP:**

1. **PhoBERT (2020)** ⭐
   - Paper: https://arxiv.org/abs/2003.00744
   - Title: "PhoBERT: Pre-trained language models for Vietnamese"
   - Impact: First large Vietnamese language model

2. **BERTweet (2020)**
   - Paper: https://arxiv.org/abs/2005.10200
   - Title: "BERTweet: A pre-trained language model for English Tweets"
   - Note: Also includes Vietnamese version

---

**RAG:**

1. **RAG Original (2020)** ⭐
   - Paper: https://arxiv.org/abs/2005.11401
   - Title: "Retrieval-Augmented Generation"
   - Impact: Introduced RAG concept

2. **Self-RAG (2023)**
   - Paper: https://arxiv.org/abs/2310.11511
   - Title: "Self-RAG: Learning to Retrieve, Generate, and Critique"

3. **RAPTOR (2024)**
   - Paper: https://arxiv.org/abs/2401.18059
   - Title: "RAPTOR: Recursive Abstractive Processing"

---

### Paper Collections

**Survey Papers:**
- Sentence Embeddings Survey: https://arxiv.org/abs/2402.16829
- Multilingual NLP Survey: https://arxiv.org/abs/2107.00676
- RAG Survey: https://arxiv.org/abs/2312.10997

**Conferences:**
- ACL Anthology: https://aclanthology.org/
- NeurIPS: https://proceedings.neurips.cc/
- EMNLP: https://2024.emnlp.org/

---

## 🔍 SEARCH STRATEGIES

### Google Scholar Queries

**Embeddings:**
```
"multilingual sentence embeddings"
"cross-lingual semantic search"
"language-agnostic embeddings"
"sentence transformers"
```

**Vietnamese:**
```
"Vietnamese text embedding"
"Vietnamese BERT"
"PhoBERT applications"
"Vietnamese NLP"
```

**RAG:**
```
"retrieval augmented generation"
"semantic search systems"
"vector database"
"dense retrieval"
```

---

### GitHub Search

**Java + ML:**
```
language:Java topic:embeddings topic:nlp
language:Java topic:semantic-search
language:Java topic:machine-learning
```

**Vietnamese:**
```
Vietnamese sentence embeddings
Vietnamese text similarity
PhoBERT applications
```

**RAG Systems:**
```
topic:rag topic:embeddings
topic:semantic-search language:Java
topic:vector-database
```

---

### HuggingFace Explore

**By Task:**
- Sentence Similarity: https://huggingface.co/models?pipeline_tag=sentence-similarity
- Feature Extraction: https://huggingface.co/models?pipeline_tag=feature-extraction
- Fill Mask: https://huggingface.co/models?pipeline_tag=fill-mask

**By Language:**
- Vietnamese: https://huggingface.co/models?language=vi
- Multilingual: https://huggingface.co/models?language=multilingual

**By Library:**
- Sentence Transformers: https://huggingface.co/models?library=sentence-transformers
- PyTorch: https://huggingface.co/models?library=pytorch
- ONNX: https://huggingface.co/models?library=onnx

---

## 🎯 RECOMMENDED LEARNING PATH

### Phase 1: Foundations (Week 1)

**Understand Embeddings:**
1. Read: https://www.pinecone.io/learn/vector-embeddings/
2. Watch: Sentence Transformers intro (YouTube)
3. Explore: https://www.sbert.net/docs/quickstart.html

**Learn MTEB:**
1. Visit: https://huggingface.co/spaces/mteb/leaderboard
2. Compare models for your use case
3. Read model cards on HuggingFace

---

### Phase 2: Java Implementation (Week 2)

**DJL Basics:**
1. Quick Start: https://docs.djl.ai/docs/quick_start.html
2. Examples: https://github.com/deepjavalibrary/djl/tree/master/examples
3. Build first embedding app

**ONNX Runtime:**
1. Docs: https://docs.djl.ai/engines/onnxruntime/
2. Convert a model: https://huggingface.co/docs/optimum/
3. Load in Java with DJL

---

### Phase 3: Vietnamese + English (Week 3)

**Model Selection:**
1. Try LaBSE: https://huggingface.co/sentence-transformers/LaBSE
2. Test Vietnamese: https://huggingface.co/keepitreal/vietnamese-sbert
3. Compare results with MTEB

**Integration:**
1. Follow: Your current implementation (already excellent!)
2. Optimize: https://docs.djl.ai/docs/development/inference_performance_optimization.html
3. Test with real data

---

### Phase 4: Production RAG (Week 4)

**RAG Framework:**
1. Study: https://www.pinecone.io/learn/retrieval-augmented-generation/
2. Implement: https://docs.langchain4j.dev/tutorials/rag
3. Choose vector DB: https://www.pinecone.io/learn/vector-database-comparison/

**Optimization:**
1. Chunking: https://www.pinecone.io/learn/chunking-strategies/
2. Caching: Implement embedding cache
3. Monitoring: Add metrics

---

## 💡 QUICK REFERENCE BOOKMARKS

### Daily Use

**Model Downloads:**
```
https://huggingface.co/sentence-transformers/LaBSE
https://huggingface.co/BAAI/bge-m3
https://huggingface.co/keepitreal/vietnamese-sbert
https://huggingface.co/sentence-transformers/all-mpnet-base-v2
```

**Documentation:**
```
https://docs.djl.ai/
https://www.sbert.net/
https://onnxruntime.ai/docs/
https://docs.langchain4j.dev/
```

**Tools:**
```
https://huggingface.co/docs/optimum/ (Model conversion)
https://huggingface.co/spaces/mteb/leaderboard (Model comparison)
https://www.pinecone.io/learn/ (Learning resources)
```

---

### Troubleshooting

**Common Issues:**
- DJL FAQ: https://github.com/deepjavalibrary/djl/discussions
- ONNX Issues: https://github.com/microsoft/onnxruntime/issues
- Stack Overflow: https://stackoverflow.com/questions/tagged/djl

**Community:**
- DJL Slack: https://deepjavalibrary.slack.com/
- HuggingFace Forum: https://discuss.huggingface.co/
- Reddit r/LanguageTechnology: https://www.reddit.com/r/LanguageTechnology/

---

## 🌟 TOP 10 MUST-BOOKMARK

1. **Sentence Transformers** - https://www.sbert.net/
   → Best resource for semantic embeddings

2. **DJL Documentation** - https://docs.djl.ai/
   → Official Java ML framework

3. **MTEB Leaderboard** - https://huggingface.co/spaces/mteb/leaderboard
   → Compare model quality

4. **HuggingFace Optimum** - https://huggingface.co/docs/optimum/
   → Model conversion to ONNX

5. **LaBSE Model** - https://huggingface.co/sentence-transformers/LaBSE
   → Best multilingual (includes Vietnamese)

6. **Pinecone Learn** - https://www.pinecone.io/learn/
   → RAG & semantic search tutorials

7. **LangChain4j** - https://docs.langchain4j.dev/
   → Java RAG framework

8. **PhoBERT** - https://github.com/VinAIResearch/PhoBERT
   → Vietnamese language model

9. **DJL Examples** - https://github.com/deepjavalibrary/djl/tree/master/examples
   → Code examples

10. **ONNX Runtime** - https://onnxruntime.ai/
    → Optimized inference

---

## 📖 CITATION

If you use these resources in research or production:

**Sentence-BERT:**
```bibtex
@inproceedings{reimers-2019-sentence-bert,
  title = "Sentence-BERT: Sentence Embeddings using Siamese BERT-Networks",
  author = "Reimers, Nils and Gurevych, Iryna",
  booktitle = "Proceedings of the 2019 Conference on EMNLP",
  year = "2019"
}
```

**LaBSE:**
```bibtex
@article{feng2020language,
  title={Language-agnostic BERT Sentence Embedding},
  author={Feng, Fangxiaoyu and Yang, Yinfei and others},
  journal={arXiv preprint arXiv:2007.01852},
  year={2020}
}
```

**PhoBERT:**
```bibtex
@inproceedings{phobert,
  title={PhoBERT: Pre-trained language models for Vietnamese},
  author={Nguyen, Dat Quoc and Nguyen, Anh Tuan},
  booktitle={Findings of EMNLP},
  year={2020}
}
```

---

## 🔄 KEEPING UP TO DATE

**Follow These:**
- HuggingFace Blog: https://huggingface.co/blog
- DJL Blog: https://djl.ai/blog/
- Papers with Code: https://paperswithcode.com/
- ML News: https://www.reddit.com/r/MachineLearning/

**Monthly Check:**
- MTEB Leaderboard: New models
- HuggingFace: Trending models
- arXiv: Latest papers

**Subscribe:**
- HuggingFace Newsletter: https://huggingface.co/subscribe/newsletter
- DJL Announcements: GitHub watch
- NLP News: https://newsletter.ruder.io/

---

**Created:** November 14, 2024  
**Maintained by:** PCM Team  
**Purpose:** Reference guide for multilingual embeddings & RAG in Java  
**Status:** ✅ Comprehensive resource collection

