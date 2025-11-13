# Vector Store Documentation

This directory contains comprehensive documentation for the PCM RAG system's vector store implementations and architecture.

## 📚 Documentation Index

### Core Documentation

| Document | Description | Audience |
|----------|-------------|----------|
| [Lucene Implementation Guide](lucene-guide.md) | Complete guide to our Lucene-based vector store | Developers, DevOps |
| [Architecture Overview](architecture-overview.md) | System architecture and design patterns | Architects, Senior Developers |
| [Qdrant vs Lucene Comparison](qdrant-vs-lucene.md) | Detailed comparison and decision framework | Technical Leaders, PMs |
| [Performance Benchmarks](performance-benchmarks.md) | Comprehensive performance analysis and tuning | DevOps, Performance Engineers |

### Quick Navigation

#### 🚀 Getting Started
- **New to the project?** Start with [Architecture Overview](architecture-overview.md)
- **Implementing search?** Go to [Lucene Implementation Guide](lucene-guide.md)
- **Choosing vector store?** Read [Qdrant vs Lucene Comparison](qdrant-vs-lucene.md)
- **Performance issues?** Check [Performance Benchmarks](performance-benchmarks.md)

#### 👥 By Role

**Software Engineers:**
1. [Architecture Overview](architecture-overview.md) - Understanding the system
2. [Lucene Implementation Guide](lucene-guide.md) - Implementation details
3. Code examples and API reference

**DevOps/SRE:**
1. [Performance Benchmarks](performance-benchmarks.md) - Tuning and monitoring
2. [Lucene Implementation Guide](lucene-guide.md) - Configuration and deployment
3. Production best practices

**Technical Leads/Architects:**
1. [Qdrant vs Lucene Comparison](qdrant-vs-lucene.md) - Strategic decisions
2. [Architecture Overview](architecture-overview.md) - Design patterns
3. Future roadmap and technology evolution

**Product Managers:**
1. [Qdrant vs Lucene Comparison](qdrant-vs-lucene.md) - Business impact analysis
2. Performance characteristics and trade-offs
3. Cost analysis and ROI considerations

## 🏗 System Overview

Our vector store system provides a unified interface for document indexing and retrieval, supporting both traditional full-text search and semantic similarity search.

### Current Implementation
- **Primary**: Apache Lucene-based vector store
- **Features**: BM25 ranking, metadata filtering, batch operations
- **Deployment**: Embedded library with file-based persistence
- **Scale**: Optimized for 1M-10M documents per node

### Planned Enhancements
- **Q1 2024**: Qdrant integration for semantic search
- **Q2 2024**: Hybrid search combining both approaches  
- **Q3 2024**: Distributed architecture and auto-scaling

## 🎯 Key Features

### ✅ Current Features
- **Full-text search** with BM25 ranking algorithm
- **Metadata filtering** with boolean operators
- **Batch operations** for efficient indexing
- **Thread-safe** concurrent access
- **Configurable** analyzers and similarity functions
- **Production-ready** with comprehensive error handling

### 🔮 Upcoming Features
- **Semantic search** with vector embeddings
- **Hybrid search** combining text and vector similarity
- **Distributed indexing** across multiple nodes
- **Real-time updates** with minimal latency impact
- **Advanced analytics** and search insights

## 📊 Performance Characteristics

### Current Performance (Lucene)
- **Query Latency**: P95 < 15ms (1M documents)
- **Indexing Rate**: ~400 docs/sec (single-threaded)
- **Memory Usage**: ~2GB per 1M documents
- **Throughput**: 5,000+ QPS (optimized setup)
- **Storage**: ~4KB per document (compressed)

### Scaling Recommendations
- **Small Scale** (<1M docs): Single Lucene node
- **Medium Scale** (1M-10M docs): Optimized Lucene with caching
- **Large Scale** (10M+ docs): Distributed Qdrant cluster
- **Enterprise Scale** (100M+ docs): Hybrid architecture

## 🔧 Quick Start

### Basic Usage
```java
// Create vector store
LuceneVectorStore vectorStore = new LuceneVectorStore("/path/to/index");

// Index documents
RAGDocument doc = RAGDocument.builder()
    .id("doc1")
    .content("Machine learning algorithms...")
    .type(DocumentType.ARTICLE)
    .build();
    
vectorStore.indexDocument(doc);

// Search documents
RetrievalOptions options = new RetrievalOptions()
    .setMaxResults(10)
    .setMinScore(0.1);
    
List<ScoredDocument> results = vectorStore.search("machine learning", options);
```

### Configuration Example
```java
// Production configuration
VectorStore vectorStore = VectorStoreFactory.builder()
    .type(VectorStoreType.LUCENE)
    .indexPath("/data/production/index")
    .property("ramBufferSizeMB", 512)
    .property("maxBufferedDocs", 2000)
    .property("mergePolicy", "tiered")
    .build();
```

## 📈 Performance Tuning

### Quick Wins
1. **Increase RAM buffer**: Set to 512MB-1GB for better indexing performance
2. **Tune GC settings**: Use G1GC with optimized pause times  
3. **Optimize file system**: Use SSD with appropriate mount options
4. **Configure caching**: Ensure adequate OS file cache

### Advanced Tuning
- Review [Performance Benchmarks](performance-benchmarks.md) for detailed analysis
- Use provided JVM tuning recommendations
- Implement proper monitoring and alerting
- Consider horizontal scaling for large datasets

## 🔍 Troubleshooting

### Common Issues

#### High Query Latency
- **Check**: File system cache hit rate
- **Action**: Increase system RAM or optimize queries
- **Reference**: [Performance Benchmarks - Memory Analysis](performance-benchmarks.md#memory--resource-analysis)

#### OutOfMemoryError
- **Check**: Heap size and GC configuration
- **Action**: Tune JVM settings or reduce batch sizes
- **Reference**: [Lucene Guide - Troubleshooting](lucene-guide.md#troubleshooting)

#### Slow Indexing
- **Check**: RAM buffer size and merge policy
- **Action**: Increase buffer size or optimize merge settings
- **Reference**: [Performance Benchmarks - Optimization](performance-benchmarks.md#optimization-recommendations)

#### Lock Acquisition Failed
- **Check**: Concurrent access or stale locks
- **Action**: Ensure proper resource cleanup
- **Reference**: [Lucene Guide - Error Handling](lucene-guide.md#troubleshooting)

### Getting Help
1. Check the specific troubleshooting section in relevant documentation
2. Review error logs with appropriate log levels
3. Use provided debugging tools and metrics
4. Consult performance benchmarks for baseline comparisons

## 🚀 Migration Guide

### From Legacy Search Systems
1. **Assessment**: Analyze current usage patterns and requirements
2. **Planning**: Choose appropriate vector store based on comparison guide
3. **Implementation**: Follow step-by-step integration guide
4. **Testing**: Use provided benchmarking tools for validation
5. **Deployment**: Gradual rollout with monitoring

### To Distributed Architecture
1. **Evaluation**: Assess current limitations and scale requirements
2. **Technology Selection**: Review Qdrant vs Lucene comparison
3. **Architecture Design**: Plan sharding and replication strategy
4. **Migration**: Implement parallel systems with data sync
5. **Cutover**: Gradual traffic migration with rollback plan

## 📅 Roadmap & Updates

### Version History
- **v1.0**: Initial Lucene implementation with basic features
- **v1.1**: Enhanced error handling and performance optimizations
- **v1.2**: Advanced query capabilities and monitoring integration

### Upcoming Releases
- **v2.0**: Qdrant integration and hybrid search (Q1 2024)
- **v2.1**: Distributed architecture support (Q2 2024)  
- **v3.0**: Advanced ML integration and analytics (Q3 2024)

### Contributing
- Documentation improvements and corrections
- Performance testing and benchmark contributions
- Feature requests and enhancement proposals
- Bug reports with detailed reproduction steps

## 📞 Support & Resources

### Internal Resources
- **Architecture Team**: System design and technology decisions
- **Platform Team**: Infrastructure and deployment support
- **Data Team**: Search quality and relevance optimization

### External Resources
- [Apache Lucene Documentation](https://lucene.apache.org/core/documentation.html)
- [Qdrant Documentation](https://qdrant.tech/documentation/)
- [BM25 Algorithm Reference](https://en.wikipedia.org/wiki/Okapi_BM25)
- [Information Retrieval Best Practices](https://nlp.stanford.edu/IR-book/)

---

*Last Updated: 2024-01-15*  
*Version: 1.2*  
*Maintainers: Platform Engineering Team*