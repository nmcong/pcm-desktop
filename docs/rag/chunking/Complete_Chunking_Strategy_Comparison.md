# Complete Chunking Strategy Comparison Guide

## Tổng quan

Hệ thống PCM Chunking hiện cung cấp **15 chunking strategies** khác nhau, được phân thành 3 nhóm chính:
- **4 PCM Strategies** (Custom implementation)
- **4 LangChain Strategies** (Custom implementation)  
- **7 LangChain4j Strategies** (Real library)

Tài liệu này cung cấp so sánh chi tiết và hướng dẫn lựa chọn strategy phù hợp.

## Bảng So Sánh Tổng Quan

| Strategy Group | Implementation | Số lượng | Ưu điểm | Nhược điểm | Best For |
|---------------|----------------|----------|---------|------------|----------|
| **PCM Strategies** | Custom PCM | 4 | Full control, Semantic support | Limited ecosystem | Semantic analysis, PCM-specific |
| **LangChain Strategies** | Custom PCM | 4 | LangChain compatibility | Maintenance overhead | LangChain-style processing |
| **LangChain4j Strategies** | Real library | 7 | Battle-tested, Community support | Limited customization | Production reliability |

---

## Chi Tiết Từng Strategy

### PCM Strategies (Custom Implementation)

#### 1. FIXED_SIZE
```java
ChunkingConfig.defaults() // Default sử dụng SENTENCE_AWARE
ChunkingConfig.builder().primaryStrategy(FIXED_SIZE).build()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Kiểu chia** | Fixed character count |
| **Kích thước mặc định** | 1000 chars |
| **Overlap mặc định** | 200 chars |
| **Tốc độ** | ⭐⭐⭐⭐⭐ (Fastest) |
| **Chất lượng** | ⭐⭐⭐ (Good) |
| **Phù hợp với** | High-volume processing, Simple content |

**Ưu điểm:**
- Tốc độ cao nhất
- Predictable chunk sizes
- Memory efficient

**Nhược điểm:**
- Có thể cắt giữa câu
- Không preserve semantic meaning

#### 2. SENTENCE_AWARE
```java
ChunkingConfig.forNarrativeContent()
ChunkingConfig.builder().primaryStrategy(SENTENCE_AWARE).build()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Kiểu chia** | Sentence boundaries |
| **Kích thước mặc định** | 1000 chars |
| **Overlap mặc định** | 200 chars |
| **Tốc độ** | ⭐⭐⭐⭐ (Fast) |
| **Chất lượng** | ⭐⭐⭐⭐ (Very Good) |
| **Phù hợp với** | Narrative content, General text |

**Ưu điểm:**
- Preserves sentence integrity
- Good quality/performance balance
- Robust sentence detection

**Nhược điểm:**
- Slower than fixed-size
- May create variable chunk sizes

#### 3. SEMANTIC
```java
ChunkingConfig.forAcademicPapers(embeddingService)
ChunkingConfig.builder().primaryStrategy(SEMANTIC).build()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Kiểu chia** | Semantic similarity |
| **Kích thước mặc định** | 1800 chars |
| **Overlap mặc định** | 200 chars |
| **Tốc độ** | ⭐⭐ (Slow) |
| **Chất lượng** | ⭐⭐⭐⭐⭐ (Excellent) |
| **Phù hợp với** | Academic papers, Complex analysis |

**Ưu điểm:**
- Highest semantic coherence
- Preserves topical unity
- Best for RAG applications

**Nhược điểm:**
- Requires embedding service
- Slowest performance
- Higher memory usage

#### 4. MARKDOWN_AWARE
```java
ChunkingConfig.forTechnicalDocs()
ChunkingConfig.builder().primaryStrategy(MARKDOWN_AWARE).build()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Kiểu chia** | Markdown structure |
| **Kích thước mặc định** | 1500 chars |
| **Overlap mặc định** | 200 chars |
| **Tốc độ** | ⭐⭐⭐ (Good) |
| **Chất lượng** | ⭐⭐⭐⭐ (Very Good) |
| **Phù hợp với** | Technical docs, Structured content |

**Ưu điểm:**
- Preserves document structure
- Header-aware chunking
- Code block preservation

**Nhược điểm:**
- Markdown-specific
- Complex parsing logic

---

### LangChain Strategies (Custom Implementation)

#### 5. LANGCHAIN_CHARACTER
```java
ChunkingConfig.forLangChainCharacter()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Kiểu chia** | Single character separator |
| **Separator mặc định** | "\n\n" |
| **Tốc độ** | ⭐⭐⭐⭐ (Fast) |
| **Chất lượng** | ⭐⭐⭐ (Good) |
| **Phù hợp với** | Simple structured text |

#### 6. LANGCHAIN_RECURSIVE
```java
ChunkingConfig.forLangChainRecursive()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Kiểu chia** | Hierarchical separators |
| **Hierarchy** | "\n\n" → "\n" → " " → "" |
| **Tốc độ** | ⭐⭐⭐ (Good) |
| **Chất lượng** | ⭐⭐⭐⭐ (Very Good) |
| **Phù hợp với** | General purpose, Mixed content |

#### 7. LANGCHAIN_TOKEN
```java
ChunkingConfig.forLangChainToken("gpt-3.5-turbo")
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Kiểu chia** | Token count |
| **Default model** | gpt-3.5-turbo |
| **Tốc độ** | ⭐⭐⭐ (Good) |
| **Chất lượng** | ⭐⭐⭐⭐ (Very Good) |
| **Phù hợp với** | LLM-ready content |

#### 8. LANGCHAIN_CODE
```java
ChunkingConfig.forLangChainCode("java")
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Kiểu chia** | Code-aware splitting |
| **Features** | Function/class boundaries |
| **Tốc độ** | ⭐⭐⭐ (Good) |
| **Chất lượng** | ⭐⭐⭐⭐ (Very Good) |
| **Phù hợp với** | Source code, Technical docs |

---

### LangChain4j Strategies (Real Library)

#### 9. LANGCHAIN4J_PARAGRAPH
```java
ChunkingConfig.forLangChain4jParagraph()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Implementation** | DocumentByParagraphSplitter |
| **Kích thước mặc định** | 1000 chars |
| **Overlap mặc định** | 200 chars |
| **Tốc độ** | ⭐⭐⭐⭐ (Fast) |
| **Chất lượng** | ⭐⭐⭐⭐ (Very Good) |
| **Phù hợp với** | Well-structured documents |

**Ưu điểm:**
- Battle-tested implementation
- Efficient paragraph detection
- Good performance

#### 10. LANGCHAIN4J_SENTENCE
```java
ChunkingConfig.forLangChain4jSentence()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Implementation** | DocumentBySentenceSplitter |
| **Kích thước mặc định** | 800 chars |
| **Overlap mặc định** | 100 chars |
| **Tốc độ** | ⭐⭐⭐ (Good) |
| **Chất lượng** | ⭐⭐⭐⭐⭐ (Excellent) |
| **Phù hợp với** | Narrative content, Literature |

#### 11. LANGCHAIN4J_WORD
```java
ChunkingConfig.forLangChain4jWord()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Implementation** | DocumentByWordSplitter |
| **Kích thước mặc định** | 600 chars |
| **Overlap mặc định** | 80 chars |
| **Tốc độ** | ⭐⭐⭐ (Good) |
| **Chất lượng** | ⭐⭐⭐ (Good) |
| **Phù hợp với** | Fine-grained analysis |

#### 12. LANGCHAIN4J_LINE
```java
ChunkingConfig.forLangChain4jLine()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Implementation** | DocumentByLineSplitter |
| **Kích thước mặc định** | 500 chars |
| **Overlap mặc định** | 50 chars |
| **Tốc độ** | ⭐⭐⭐⭐ (Fast) |
| **Chất lượng** | ⭐⭐⭐ (Good) |
| **Phù hợp với** | Line-structured content |

#### 13. LANGCHAIN4J_CHARACTER
```java
ChunkingConfig.forLangChain4jCharacter()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Implementation** | DocumentByCharacterSplitter |
| **Kích thước mặc định** | 400 chars |
| **Overlap mặc định** | 40 chars |
| **Tốc độ** | ⭐⭐⭐⭐⭐ (Fastest) |
| **Chất lượng** | ⭐⭐ (Fair) |
| **Phù hợp với** | Exact size requirements |

#### 14. LANGCHAIN4J_REGEX
```java
ChunkingConfig.forLangChain4jRegex("\\n\\n")
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Implementation** | DocumentSplitters.recursive (fallback) |
| **Pattern mặc định** | "\\n\\n" |
| **Tốc độ** | ⭐⭐⭐ (Good) |
| **Chất lượng** | ⭐⭐⭐ (Good) |
| **Phù hợp với** | Custom patterns |

#### 15. LANGCHAIN4J_HIERARCHICAL
```java
ChunkingConfig.forLangChain4jHierarchical()
```

| Thuộc tính | Giá trị |
|------------|---------|
| **Implementation** | DocumentSplitters.recursive |
| **Strategy** | Paragraph → Sentence → Word → Char |
| **Kích thước mặc định** | 1200 chars |
| **Overlap mặc định** | 200 chars |
| **Tốc độ** | ⭐⭐⭐ (Good) |
| **Chất lượng** | ⭐⭐⭐⭐⭐ (Excellent) |
| **Phù hợp với** | General purpose, Unknown content |

---

## Use Case Matrix

### Theo Document Type

| Document Type | Recommended Strategies | Reason |
|---------------|----------------------|---------|
| **Technical Documentation** | MARKDOWN_AWARE, LANGCHAIN4J_PARAGRAPH | Structure preservation |
| **Academic Papers** | SEMANTIC, LANGCHAIN4J_HIERARCHICAL | Semantic coherence |
| **Narrative Content** | SENTENCE_AWARE, LANGCHAIN4J_SENTENCE | Sentence integrity |
| **Source Code** | LANGCHAIN_CODE, LANGCHAIN4J_LINE | Code structure |
| **Structured Data** | LANGCHAIN4J_LINE, FIXED_SIZE | Predictable format |
| **Mixed Content** | LANGCHAIN4J_HIERARCHICAL, LANGCHAIN_RECURSIVE | Adaptive splitting |

### Theo Performance Requirements

| Performance Need | Recommended Strategies | Trade-offs |
|------------------|----------------------|-----------|
| **Highest Speed** | FIXED_SIZE, LANGCHAIN4J_CHARACTER | Lower quality |
| **Balanced** | SENTENCE_AWARE, LANGCHAIN4J_PARAGRAPH | Good quality/speed |
| **Highest Quality** | SEMANTIC, LANGCHAIN4J_HIERARCHICAL | Slower performance |
| **LLM Optimized** | LANGCHAIN_TOKEN, LANGCHAIN4J_SENTENCE | Token-aware |

### Theo Use Case

| Use Case | Strategy | Configuration |
|----------|----------|---------------|
| **RAG Q&A** | SEMANTIC | Large chunks, semantic coherence |
| **Search Indexing** | LANGCHAIN4J_PARAGRAPH | Medium chunks, structure preservation |
| **Text Classification** | LANGCHAIN4J_SENTENCE | Small chunks, sentence integrity |
| **Code Analysis** | LANGCHAIN_CODE | Variable chunks, function boundaries |
| **Content Summarization** | LANGCHAIN4J_HIERARCHICAL | Adaptive chunks |
| **High-Volume Processing** | FIXED_SIZE | Small fixed chunks |

---

## Performance Comparison

### Speed Ranking (Fastest to Slowest)

1. **LANGCHAIN4J_CHARACTER** - Simple character counting
2. **FIXED_SIZE** - Direct string manipulation
3. **LANGCHAIN4J_LINE** - Line boundary detection
4. **SENTENCE_AWARE** - Sentence regex processing
5. **LANGCHAIN4J_PARAGRAPH** - Paragraph detection
6. **LANGCHAIN_CHARACTER** - Custom character logic
7. **LANGCHAIN4J_WORD** - Word boundary processing
8. **MARKDOWN_AWARE** - Structure parsing
9. **LANGCHAIN_RECURSIVE** - Hierarchical logic
10. **LANGCHAIN4J_HIERARCHICAL** - LangChain4j recursive
11. **LANGCHAIN4J_REGEX** - Pattern matching
12. **LANGCHAIN_TOKEN** - Tokenization overhead
13. **LANGCHAIN_CODE** - Code structure analysis
14. **SEMANTIC** - Embedding calculations

### Quality Ranking (Best to Good)

1. **SEMANTIC** - Highest semantic coherence
2. **LANGCHAIN4J_HIERARCHICAL** - Smart adaptive splitting
3. **LANGCHAIN4J_SENTENCE** - Perfect sentence boundaries
4. **SENTENCE_AWARE** - Good sentence detection
5. **MARKDOWN_AWARE** - Structure-aware
6. **LANGCHAIN4J_PARAGRAPH** - Good paragraph detection
7. **LANGCHAIN_RECURSIVE** - Adaptive approach
8. **LANGCHAIN_TOKEN** - LLM-optimized
9. **LANGCHAIN_CODE** - Code-aware
10. **LANGCHAIN_CHARACTER** - Separator-based
11. **LANGCHAIN4J_WORD** - Word boundaries
12. **LANGCHAIN4J_REGEX** - Pattern-based
13. **LANGCHAIN4J_LINE** - Line boundaries
14. **FIXED_SIZE** - Basic splitting
15. **LANGCHAIN4J_CHARACTER** - Character counting

---

## Decision Matrix

### Quick Selection Guide

```
Need semantic analysis? 
└── Yes: SEMANTIC
└── No: Continue...

Need maximum speed?
└── Yes: FIXED_SIZE or LANGCHAIN4J_CHARACTER  
└── No: Continue...

Have structured content (Markdown)?
└── Yes: MARKDOWN_AWARE
└── No: Continue...

Processing code?
└── Yes: LANGCHAIN_CODE
└── No: Continue...

Need LangChain ecosystem compatibility?
└── Yes: LANGCHAIN4J_HIERARCHICAL (best overall)
└── No: Continue...

General purpose text?
└── Yes: SENTENCE_AWARE or LANGCHAIN4J_PARAGRAPH
└── No: Use specific strategy based on content type
```

### Advanced Selection

```java
// Get all recommendations for your document
List<ChunkingFactory.StrategyRecommendation> recommendations = 
    ChunkingFactory.getAllRecommendations(document, config, embeddingService);

// Filter by group if needed
List<StrategyRecommendation> pcmStrategies = 
    recommendations.stream()
        .filter(r -> !r.strategyType.name().startsWith("LANGCHAIN"))
        .toList();

List<StrategyRecommendation> langchain4jStrategies = 
    recommendations.stream()
        .filter(r -> r.strategyType.name().startsWith("LANGCHAIN4J"))
        .toList();

// Choose based on quality score
ChunkingStrategy bestStrategy = recommendations.get(0).strategy;
```

---

## Configuration Recommendations

### Document Size-Based Configuration

| Document Size | Recommended Chunk Size | Recommended Overlap | Best Strategies |
|---------------|----------------------|-------------------|-----------------|
| **Small (< 5KB)** | 500-800 chars | 50-100 chars | LANGCHAIN4J_SENTENCE, SENTENCE_AWARE |
| **Medium (5-50KB)** | 800-1200 chars | 100-200 chars | LANGCHAIN4J_PARAGRAPH, SEMANTIC |
| **Large (50-500KB)** | 1000-1500 chars | 150-250 chars | LANGCHAIN4J_HIERARCHICAL, MARKDOWN_AWARE |
| **Very Large (>500KB)** | 600-1000 chars | 80-150 chars | FIXED_SIZE, LANGCHAIN4J_CHARACTER |

### Content Type-Based Configuration

#### Technical Documentation
```java
ChunkingConfig config = ChunkingConfig.builder()
    .primaryStrategy(ChunkingStrategyType.MARKDOWN_AWARE)
    .fallbackStrategy(ChunkingStrategyType.LANGCHAIN4J_PARAGRAPH)
    .targetChunkSize(1500)
    .maxChunkSize(2500)
    .overlapSize(200)
    .build();
```

#### Academic Papers
```java
ChunkingConfig config = ChunkingConfig.builder()
    .primaryStrategy(ChunkingStrategyType.SEMANTIC)
    .fallbackStrategy(ChunkingStrategyType.LANGCHAIN4J_HIERARCHICAL)
    .targetChunkSize(1800)
    .maxChunkSize(3000)
    .overlapSize(300)
    .build();
```

#### General Content
```java
ChunkingConfig config = ChunkingConfig.builder()
    .primaryStrategy(ChunkingStrategyType.LANGCHAIN4J_HIERARCHICAL)
    .fallbackStrategy(ChunkingStrategyType.SENTENCE_AWARE)
    .targetChunkSize(1000)
    .maxChunkSize(2000)
    .overlapSize(200)
    .build();
```

---

## Migration Guide

### From Single Strategy to Multi-Strategy

**Before:**
```java
FixedSizeChunking chunking = new FixedSizeChunking(1000, 200);
List<DocumentChunk> chunks = chunking.chunk(document);
```

**After (Recommended):**
```java
// Automatic strategy selection
ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
    document, ChunkingConfig.defaults(), embeddingService);
List<DocumentChunk> chunks = strategy.chunk(document);

// Or use specific LangChain4j strategy
ChunkingStrategy langchain4jStrategy = ChunkingFactory.createStrategy(
    ChunkingConfig.forLangChain4jHierarchical());
List<DocumentChunk> chunks = langchain4jStrategy.chunk(document);
```

### From Custom to LangChain4j

**Before:**
```java
// Custom implementation
SentenceAwareChunking chunking = SentenceAwareChunking.defaults();
```

**After:**
```java
// LangChain4j equivalent
ChunkingStrategy strategy = ChunkingFactory.createStrategy(
    ChunkingConfig.forLangChain4jSentence());
```

---

## Monitoring and Optimization

### Performance Monitoring
```java
long startTime = System.currentTimeMillis();
List<DocumentChunk> chunks = strategy.chunk(document);
long duration = System.currentTimeMillis() - startTime;

System.out.println("Strategy: " + strategy.getStrategyName());
System.out.println("Duration: " + duration + "ms");
System.out.println("Chunks: " + chunks.size());
System.out.println("Avg chunk size: " + 
    chunks.stream().mapToInt(c -> c.getContent().length()).average().orElse(0));
```

### Quality Monitoring
```java
double avgQuality = chunks.stream()
    .mapToDouble(DocumentChunk::getQualityScore)
    .average()
    .orElse(0.0);

if (avgQuality < 0.6) {
    // Try different strategy
    strategy = ChunkingFactory.createStrategy(
        ChunkingConfig.forLangChain4jHierarchical());
    chunks = strategy.chunk(document);
}
```

### A/B Testing Framework
```java
Map<String, List<DocumentChunk>> results = new HashMap<>();

ChunkingStrategy[] strategies = {
    ChunkingFactory.createStrategy(ChunkingConfig.forLangChain4jHierarchical()),
    ChunkingFactory.createStrategy(ChunkingConfig.forSentenceAware()),
    ChunkingFactory.createStrategy(ChunkingConfig.forSemanticChunking(embeddingService))
};

for (ChunkingStrategy strategy : strategies) {
    List<DocumentChunk> chunks = strategy.chunk(document);
    results.put(strategy.getStrategyName(), chunks);
}

// Compare results and choose best
```

---

## Kết luận

Với **15 chunking strategies** khác nhau, hệ thống PCM cung cấp sự linh hoạt tối đa cho mọi use case:

1. **PCM Strategies**: Tối ưu cho semantic analysis và control hoàn toàn
2. **LangChain Strategies**: Compatibility với LangChain ecosystem  
3. **LangChain4j Strategies**: Battle-tested reliability cho production

**Khuyến nghị tổng quan:**
- **Bắt đầu với**: `LANGCHAIN4J_HIERARCHICAL` cho general-purpose
- **Semantic analysis**: `SEMANTIC` cho highest quality
- **High performance**: `FIXED_SIZE` cho speed
- **Production reliability**: Các LangChain4j strategies
- **Custom requirements**: PCM strategies với full control

Sử dụng `ChunkingFactory.getAllRecommendations()` để automatic strategy selection dựa trên document characteristics và quality requirements.