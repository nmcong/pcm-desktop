# Embedding & RAG Documentation

## 📚 Overview

Tài liệu này hướng dẫn sử dụng DJL (Deep Java Library) và ONNX Runtime để tạo embeddings cho semantic search, RAG systems, và các ứng dụng AI khác.

---

## 📖 Documents

### 1. [DJL Overview](./DJL_OVERVIEW.md) ⭐ **START HERE**

**Comprehensive guide về DJL:**
- Giới thiệu DJL & ONNX Runtime
- Kiến trúc system & data flow
- Installation guide (automatic & manual)
- Usage examples & code samples
- Complete API reference
- Troubleshooting guide
- Performance optimization tips
- Best practices

**👉 Read if:** Bạn mới bắt đầu với DJL hoặc cần reference đầy đủ

---

### 2. [Model Selection Guide](./MODEL_SELECTION_GUIDE.md) 🎯 **CHOOSE YOUR MODEL**

**Decision guide để chọn model phù hợp:**
- Selection matrix theo use case
- Detailed review của 5+ popular models
- Trade-offs: Quality ↔ Speed ↔ Memory
- Interactive decision tree
- Setup guide cho từng model
- Real-world use cases với recommendations
- Migration strategies

**👉 Read if:** Bạn đang bắt đầu project mới hoặc cần optimize performance

---

### 3. [Model Comparison & Benchmarks](./MODEL_COMPARISON.md) 📊 **DATA & BENCHMARKS**

**Detailed performance analysis:**
- Complete comparison matrix
- Inference speed benchmarks
- Quality metrics (MTEB, STS scores)
- Memory & storage requirements
- Real-world performance tests
- Migration guide between models
- Optimization strategies
- Winner by category

**👉 Read if:** Bạn cần data cụ thể để quyết định hoặc đang optimize production system

---

## 🚀 Quick Navigation

### By Goal

**🎯 "Tôi muốn bắt đầu nhanh"**
→ [DJL Overview - Quick Start](./DJL_OVERVIEW.md#-cài-đặt)

**🤔 "Model nào phù hợp với tôi?"**
→ [Model Selection - Decision Tree](./MODEL_SELECTION_GUIDE.md#-decision-tree)

**📊 "Tôi cần benchmark data"**
→ [Model Comparison - Benchmarks](./MODEL_COMPARISON.md#-detailed-benchmarks)

**🐛 "Tôi gặp lỗi..."**
→ [DJL Overview - Troubleshooting](./DJL_OVERVIEW.md#-troubleshooting)

**⚡ "Làm sao tăng performance?"**
→ [Model Comparison - Optimization](./MODEL_COMPARISON.md#-performance-optimization-tips)

---

## 📋 Quick Reference

### Default Recommendation (90% use cases)

```yaml
Model: all-MiniLM-L6-v2
Speed: ⚡⚡⚡⚡⚡ (15ms)
Quality: ⭐⭐⭐⭐ (69.4/100)
Memory: 💚💚💚💚💚 (110MB)
Size: 90MB
```

**Setup:**
```bash
./scripts/setup-embeddings-djl.sh all-MiniLM-L6-v2
```

**Usage:**
```java
DJLEmbeddingService embeddings = new DJLEmbeddingService(
    "data/models/all-MiniLM-L6-v2"
);
float[] vector = embeddings.embed("Your text here");
```

---

### High-Quality Alternative

```yaml
Model: all-mpnet-base-v2
Speed: ⚡⚡⚡ (40ms)
Quality: ⭐⭐⭐⭐⭐ (72.8/100)
Memory: 💚💚💚 (400MB)
Size: 420MB
```

---

### Multilingual Option

```yaml
Model: paraphrase-multilingual-mpnet-base-v2
Speed: ⚡⚡ (60ms)
Quality: ⭐⭐⭐⭐ (65.7/100)
Memory: 💚💚 (850MB)
Size: 1GB
Languages: 50+
```

---

## 🎓 Learning Path

### Beginner → Advanced

1. **📖 Start:** Read [DJL Overview](./DJL_OVERVIEW.md) intro & setup
2. **🔧 Install:** Run setup script
3. **💻 Test:** Run example code
4. **🎯 Choose:** Use [Model Selection Guide](./MODEL_SELECTION_GUIDE.md)
5. **📊 Optimize:** Review [Model Comparison](./MODEL_COMPARISON.md)
6. **🚀 Deploy:** Apply best practices

---

## 📊 Document Stats

| Document | Pages | Words | Topics | Level |
|----------|-------|-------|--------|-------|
| DJL Overview | ~15 | ~5,000 | 10+ | Beginner-Advanced |
| Model Selection | ~12 | ~4,500 | 8+ | Intermediate |
| Model Comparison | ~15 | ~5,500 | 12+ | Advanced |
| **Total** | **~42** | **~15,000** | **30+** | All levels |

---

## 🔗 External Resources

### Official Documentation
- **DJL Official Docs**: https://djl.ai/
- **ONNX Runtime**: https://onnxruntime.ai/
- **Sentence Transformers**: https://www.sbert.net/

### Model Hub
- **HuggingFace Models**: https://huggingface.co/sentence-transformers
- **MTEB Leaderboard**: https://huggingface.co/spaces/mteb/leaderboard

### Community
- **DJL Discord**: https://discord.gg/deepjavalibrary
- **GitHub Issues**: https://github.com/deepjavalibrary/djl/issues

---

## 💡 Quick Tips

```
💡 Tip 1: Luôn warm-up JVM với 10-20 inference calls
💡 Tip 2: Cache embeddings cho queries thường xuyên
💡 Tip 3: Dùng batch processing cho large datasets
💡 Tip 4: MiniLM-L6-v2 đủ tốt cho 90% use cases
💡 Tip 5: Pre-compute document embeddings, chỉ embed queries at runtime
💡 Tip 6: Monitor memory usage khi scale up
💡 Tip 7: Consider model size khi deploy to edge devices
```

---

## 🤝 Contributing

Contributions are welcome!

**Ways to contribute:**
- 📝 Improve documentation
- 🐛 Report bugs or issues
- ✨ Add new examples
- 📊 Submit benchmark results
- 🌍 Add translations

**How to contribute:**
1. Fork repository
2. Create feature branch
3. Make changes
4. Submit pull request

---

## 📝 Changelog

### v2.0.0 (2024-11-13) ✅

**✨ New Features:**
- Complete DJL ONNX Runtime implementation
- Support for multiple embedding models
- Comprehensive documentation (15,000+ words)
- Production-ready examples

**🔧 Improvements:**
- Updated to DJL 0.35.0
- Updated to ONNX Runtime 1.19.0
- Better error handling & logging
- Optimized resource management

**📚 Documentation:**
- DJL Overview guide
- Model Selection guide
- Model Comparison & Benchmarks
- Best practices & tips

---

**Last updated:** November 13, 2024  
**Version:** 2.0.0  
**Status:** ✅ Production Ready  
**Authors:** PCM Team

---

[← Back to Main Docs](../README.md)

