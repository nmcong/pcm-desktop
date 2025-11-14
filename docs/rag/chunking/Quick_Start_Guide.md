# PCM Chunking System - Quick Start Guide

## Giới thiệu nhanh

PCM Chunking System cung cấp **15 chunking strategies** cho document processing trong RAG applications. System hỗ trợ automatic strategy selection và quality assessment.

## 🚀 Sử dụng cơ bản (5 phút)

### 1. Import cần thiết
```java
import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.core.ChunkingFactory;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.DocumentType;
```

### 2. Tạo document
```java
RAGDocument document = RAGDocument.builder()
    .id("my-document")
    .type(DocumentType.TEXT)
    .content("Nội dung văn bản cần chia nhỏ...")
    .title("Tiêu đề document")
    .build();
```

### 3. Chunk document (Automatic - Recommended)
```java
// Automatic strategy selection
ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
    document, ChunkingConfig.defaults(), null);

List<DocumentChunk> chunks = strategy.chunk(document);

// Xem kết quả
System.out.println("Created " + chunks.size() + " chunks");
for (DocumentChunk chunk : chunks) {
    System.out.println("Chunk: " + chunk.getContent().substring(0, 50) + "...");
}
```

## 🎯 Lựa chọn Strategy cụ thể

### LangChain4j Strategies (Recommended for Production)
```java
// Best general-purpose strategy
ChunkingStrategy strategy = ChunkingFactory.createStrategy(
    ChunkingConfig.forLangChain4jHierarchical());

// Paragraph-focused
ChunkingStrategy strategy = ChunkingFactory.createStrategy(
    ChunkingConfig.forLangChain4jParagraph());

// Sentence-precise  
ChunkingStrategy strategy = ChunkingFactory.createStrategy(
    ChunkingConfig.forLangChain4jSentence());
```

### PCM Strategies (Best for Semantic Analysis)
```java
// Semantic chunking (highest quality)
ChunkingStrategy strategy = ChunkingFactory.createStrategy(
    ChunkingConfig.forAcademicPapers(embeddingService));

// Sentence-aware
ChunkingStrategy strategy = ChunkingFactory.createStrategy(
    ChunkingConfig.forNarrativeContent());

// Markdown-aware
ChunkingStrategy strategy = ChunkingFactory.createStrategy(
    ChunkingConfig.forTechnicalDocs());
```

## 📊 So sánh nhanh strategies

| Strategy | Speed | Quality | Best For |
|----------|-------|---------|----------|
| `LANGCHAIN4J_HIERARCHICAL` | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | **General purpose** |
| `SEMANTIC` | ⭐⭐ | ⭐⭐⭐⭐⭐ | Academic papers |
| `LANGCHAIN4J_PARAGRAPH` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Well-structured docs |
| `SENTENCE_AWARE` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Narrative content |
| `FIXED_SIZE` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | High-volume processing |

## 🔧 Configuration nhanh

### Theo Document Type
```java
// Technical documentation
ChunkingConfig config = ChunkingConfig.forTechnicalDocs();

// Academic papers  
ChunkingConfig config = ChunkingConfig.forAcademicPapers(embeddingService);

// General content
ChunkingConfig config = ChunkingConfig.forLangChain4jHierarchical();

// Narrative content
ChunkingConfig config = ChunkingConfig.forNarrativeContent();
```

### Custom Configuration
```java
ChunkingConfig config = ChunkingConfig.builder()
    .primaryStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN4J_HIERARCHICAL)
    .targetChunkSize(1000)
    .overlapSize(200)
    .autoSelectStrategy(true)
    .build();
```

## 🎪 Use Cases nhanh

```java
// RAG Q&A
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    ChunkingFactory.UseCase.ACADEMIC_PAPERS, embeddingService);

// General processing
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    ChunkingFactory.UseCase.LANGCHAIN4J_HIERARCHICAL_SMART, null);

// High-performance
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    ChunkingFactory.UseCase.HIGH_VOLUME_PROCESSING, null);
```

## 📈 Automatic Strategy Selection

```java
// Get all recommendations
List<ChunkingFactory.StrategyRecommendation> recommendations = 
    ChunkingFactory.getAllRecommendations(document, config, embeddingService);

// Show top 3
recommendations.stream()
    .limit(3)
    .forEach(rec -> 
        System.out.printf("%s: %.3f quality\n", rec.strategyType, rec.expectedQuality));

// Use best strategy
ChunkingStrategy bestStrategy = recommendations.get(0).strategy;
```

## 🔍 Xem thông tin chunk

```java
for (DocumentChunk chunk : chunks) {
    System.out.println("ID: " + chunk.getId());
    System.out.println("Strategy: " + chunk.getStrategyUsed());
    System.out.println("Quality: " + chunk.getQualityScore());
    System.out.println("Size: " + chunk.getContent().length() + " chars");
    System.out.println("Position: " + chunk.getStartPosition() + "-" + chunk.getEndPosition());
    System.out.println("Metadata: " + chunk.getMetadata());
    System.out.println("---");
}
```

## ⚡ Complete Example

```java
package examples;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.core.ChunkingFactory;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.DocumentType;
import java.util.List;

public class QuickStartExample {
    public static void main(String[] args) {
        // 1. Create document
        RAGDocument document = RAGDocument.builder()
            .id("quick-start-doc")
            .type(DocumentType.TEXT)
            .content("""
                PCM Chunking System là hệ thống chia nhỏ văn bản tiên tiến.
                
                Hệ thống cung cấp 15 chunking strategies khác nhau để phù hợp 
                với mọi use case. Từ academic papers đến technical documentation.
                
                LangChain4j integration mang lại battle-tested algorithms từ 
                cộng đồng LangChain với PCM framework mạnh mẽ.
                """)
            .title("PCM Chunking Quick Start")
            .build();
        
        // 2. Use best strategy (automatic)
        ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
            document, ChunkingConfig.defaults(), null);
        
        // 3. Chunk document
        List<DocumentChunk> chunks = strategy.chunk(document);
        
        // 4. Show results
        System.out.println("Strategy: " + strategy.getStrategyName());
        System.out.println("Created " + chunks.size() + " chunks\n");
        
        chunks.forEach(chunk -> {
            System.out.printf("Chunk %d (Quality: %.3f): %s...\n", 
                chunk.getChunkIndex(),
                chunk.getQualityScore(),
                chunk.getContent().substring(0, Math.min(60, chunk.getContent().length())));
        });
    }
}
```

## 🚀 Next Steps

### Để tìm hiểu sâu hơn:
1. **[LangChain4j Integration Guide](LangChain4j_Integration_Guide.md)** - Chi tiết về LangChain4j
2. **[Complete Strategy Comparison](Complete_Chunking_Strategy_Comparison.md)** - So sánh tất cả 15 strategies
3. **[Chunking Strategy Documentation](Chunking_Strategy_Documentation.md)** - Technical details

### Các tính năng nâng cao:
- **Semantic Chunking**: Sử dụng embeddings cho highest quality
- **Quality Assessment**: Automatic quality scoring và fallback
- **Metadata Preservation**: Giữ nguyên tất cả metadata qua chunking
- **Performance Optimization**: Tune configuration cho từng use case

### Performance Tips:
- Sử dụng `LANGCHAIN4J_HIERARCHICAL` cho general-purpose
- Sử dụng `SEMANTIC` cho highest quality (cần embedding service)
- Sử dụng `FIXED_SIZE` cho highest speed
- Always test multiple strategies với `getAllRecommendations()`

---

**Happy Chunking! 🎉**