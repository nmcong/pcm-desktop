# Tài Liệu Chi Tiết về Các Chiến Thuật Chunking trong RAG

## Mục Lục
1. [Tổng Quan về Document Chunking](#tổng-quan-về-document-chunking)
2. [Chunking Strategies](#chunking-strategies)
   - [Fixed-Size Chunking](#1-fixed-size-chunking)
   - [Sentence-Aware Chunking](#2-sentence-aware-chunking)
   - [Semantic Chunking](#3-semantic-chunking)
   - [Markdown-Aware Chunking](#4-markdown-aware-chunking)
3. [Bảng So Sánh Chi Tiết](#bảng-so-sánh-chi-tiết)
4. [Hướng Dẫn Sử Dụng](#hướng-dẫn-sử-dụng)
5. [Cấu Hình và Tùy Chỉnh](#cấu-hình-và-tùy-chỉnh)
6. [Best Practices](#best-practices)
7. [Ví Dụ Thực Tế](#ví-dụ-thực-tế)

---

## Tổng Quan về Document Chunking

### Chunking là gì?
Document chunking là quá trình chia nhỏ văn bản dài thành các đoạn nhỏ hơn (chunks) để tối ưu hiệu quả tìm kiếm và xử lý trong hệ thống RAG (Retrieval Augmented Generation). Mỗi chunk chứa một phần thông tin có ý nghĩa và có thể được xử lý độc lập.

### Tại sao cần Chunking?
- **Giới hạn context window**: LLM có giới hạn về số token có thể xử lý cùng lúc
- **Tăng độ chính xác**: Chunks nhỏ giúp tìm kiếm thông tin chính xác hơn
- **Giảm nhiễu**: Tránh thông tin không liên quan trong quá trình retrieval
- **Tối ưu hiệu suất**: Xử lý chunks nhỏ nhanh hơn so với toàn bộ document

### Enhanced DocumentChunk
Hệ thống sử dụng lớp `DocumentChunk` được nâng cấp với 30+ metadata fields:

```java
public class DocumentChunk {
    // Core chunk data
    private String chunkId;
    private String documentId;
    private String content;
    private int index;
    
    // Position information
    private Integer startPosition;
    private Integer endPosition;
    private Integer length;
    
    // Document metadata
    private String documentTitle;
    private DocumentType documentType;
    private String sourcePath;
    private LocalDateTime documentTimestamp;
    
    // Chunking metadata
    private String chunkingStrategy;
    private Integer chunkSizeChars;
    private Integer estimatedTokens;
    private Integer overlapSize;
    private Boolean hasOverlapBefore;
    private Boolean hasOverlapAfter;
    
    // Quality metrics
    private Double qualityScore;        // 0.0-1.0
    private Double coherenceScore;      // 0.0-1.0
    private Double densityScore;        // 0.0-1.0
    
    // Context linking
    private String previousChunkId;
    private String nextChunkId;
    private String parentSectionId;
    
    // Structural metadata
    private String sectionTitle;
    private Integer hierarchyLevel;
    private String contentType;
    
    // Custom metadata map
    private Map<String, String> customMetadata;
}
```

---

## Chunking Strategies

### 1. Fixed-Size Chunking

**Mô tả**: Chia văn bản thành các chunks có kích thước cố định, không quan tâm đến cấu trúc ngữ nghĩa.

**Đặc điểm**:
- ✅ Đơn giản, nhanh chóng
- ✅ Kích thước chunk có thể dự đoán
- ✅ Hiệu suất cao cho xử lý batch
- ❌ Có thể cắt giữa câu/từ
- ❌ Mất thông tin ngữ cảnh
- ❌ Chất lượng semantic thấp

**Cách hoạt động**:
```java
public class FixedSizeChunking implements ChunkingStrategy {
    private final int chunkSize;
    private final int overlapSize;
    
    @Override
    public List<DocumentChunk> chunk(RAGDocument document) {
        // 1. Chia văn bản theo kích thước cố định
        // 2. Thêm overlap giữa các chunks
        // 3. Tính toán quality metrics
        // 4. Tạo metadata cho từng chunk
    }
}
```

**Khi nào sử dụng**:
- Xử lý khối lượng lớn document
- Không quan tâm đến cấu trúc ngữ nghĩa
- Cần tốc độ xử lý nhanh
- Document có cấu trúc đơn giản

**Cấu hình**:
```java
// Cấu hình mặc định
FixedSizeChunking strategy = new FixedSizeChunking(1000, 200);

// Sử dụng qua factory
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    UseCase.HIGH_VOLUME_PROCESSING, null);
```

**Ví dụ thực tế**:
```java
// Input: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua..."
// Chunk 1 (0-1000): "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod..."
// Chunk 2 (800-1800): "...tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim..."
```

---

### 2. Sentence-Aware Chunking

**Mô tả**: Chia văn bản dựa trên ranh giới câu, đảm bảo tính toàn vẹn ngữ nghĩa của từng câu.

**Đặc điểm**:
- ✅ Bảo toàn cấu trúc câu
- ✅ Tốt cho văn bản narrative
- ✅ Dễ đọc và hiểu
- ✅ Quality score cao
- ❌ Kích thước chunk không đồng đều
- ❌ Phức tạp hơn fixed-size
- ❌ Yêu cầu xử lý ngôn ngữ

**Cách hoạt động**:
```java
public class SentenceAwareChunking implements ChunkingStrategy {
    // Regex pattern để nhận diện câu
    private static final Pattern SENTENCE_PATTERN = Pattern.compile(
        "(?<![A-Z][a-z]\\.)(?<![A-Z]\\.)(?<=\\.|\\!|\\?)\\s+(?=[A-Z])|" +
        "(?<=[.!?][\"'])\\s+(?=[A-Z])|" +
        "(?<=\\.|\\!|\\?)\\n+(?=[A-Z])",
        Pattern.MULTILINE
    );
    
    @Override
    public List<DocumentChunk> chunk(RAGDocument document) {
        // 1. Tìm ranh giới câu bằng regex
        // 2. Nhóm câu theo target size
        // 3. Đảm bảo size tolerance
        // 4. Tạo overlap intelligent
    }
}
```

**Thuật toán chi tiết**:
1. **Sentence Detection**: Sử dụng regex pattern phức tạp để nhận diện câu
2. **Abbreviation Protection**: Bảo vệ các từ viết tắt (Dr., Mr., etc.)
3. **Sentence Grouping**: Nhóm câu để đạt target chunk size
4. **Quality Scoring**: Tính toán dựa trên completeness và coherence

**Cấu hình linh hoạt**:
```java
// Strict mode - tolerance thấp
SentenceAwareChunking strict = SentenceAwareChunking.strict();

// Flexible mode - tolerance cao  
SentenceAwareChunking flexible = SentenceAwareChunking.flexible();

// Custom configuration
SentenceAwareChunking custom = new SentenceAwareChunking(
    1000,   // targetChunkSize
    200,    // overlapSize  
    0.3     // sizeTolerance (30%)
);
```

**Quality Metrics**:
```java
// Quality Score calculation
double sizeScore = 1.0 - Math.abs(chunkSize - targetSize) / targetSize;
double completenessScore = allSentencesComplete ? 1.0 : 0.5;
double structureScore = sentenceCount >= 2 ? 1.0 : 0.7;
double qualityScore = (sizeScore * 0.4 + completenessScore * 0.4 + structureScore * 0.2);
```

---

### 3. Semantic Chunking

**Mô tả**: Chia văn bản dựa trên semantic similarity sử dụng embeddings, tạo chunks có tính coherence cao về mặt nội dung.

**Đặc điểm**:
- ✅ Chất lượng semantic cao nhất
- ✅ Chunks có tính nhất quán về topic
- ✅ Tối ưu cho RAG applications
- ✅ Adaptive chunk sizes
- ❌ Chi phí tính toán cao (cần embeddings)
- ❌ Phụ thuộc vào EmbeddingService
- ❌ Thời gian xử lý lâu hơn
- ❌ Cần model embeddings chất lượng cao

**Cách hoạt động**:
```java
public class SemanticChunking implements ChunkingStrategy {
    private final EmbeddingService embeddingService;
    private final double similarityThreshold;
    private final int slidingWindowSize;
    
    @Override
    public List<DocumentChunk> chunk(RAGDocument document) {
        // 1. Tạo segments từ sentences
        // 2. Generate embeddings cho từng segment
        // 3. Group segments dựa trên cosine similarity
        // 4. Tạo chunks từ semantic groups
    }
}
```

**Thuật toán Semantic Grouping**:
```java
private List<SemanticGroup> groupSegmentsBySimilarity(
    List<TextSegment> segments, List<float[]> embeddings) {
    
    SemanticGroup currentGroup = new SemanticGroup();
    currentGroup.addSegment(segments.get(0), embeddings.get(0));
    
    for (int i = 1; i < segments.size(); i++) {
        float[] embedding = embeddings.get(i);
        
        // Tính similarity với group hiện tại
        double similarity = calculateGroupSimilarity(embedding, currentGroup);
        
        if (similarity >= similarityThreshold && 
            currentGroup.getTotalLength() + segment.length <= maxChunkSize) {
            // Thêm vào group hiện tại
            currentGroup.addSegment(segments.get(i), embedding);
        } else {
            // Tạo group mới
            groups.add(currentGroup);
            currentGroup = new SemanticGroup();
            currentGroup.addSegment(segments.get(i), embedding);
        }
    }
}
```

**Cosine Similarity Calculation**:
```java
private double cosineSimilarity(float[] a, float[] b) {
    double dotProduct = 0.0;
    double normA = 0.0, normB = 0.0;
    
    for (int i = 0; i < a.length; i++) {
        dotProduct += a[i] * b[i];
        normA += a[i] * a[i];
        normB += b[i] * b[i];
    }
    
    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
}
```

**Sliding Window Strategy**:
- Sử dụng sliding window để tính similarity với các segments gần nhất
- Giúp duy trì context cục bộ
- Tránh việc so sánh với toàn bộ group (expensive)

**Cấu hình cho các use cases**:
```java
// High precision - academic papers
SemanticChunking precise = SemanticChunking.precise(embeddingService);
// threshold=0.85, windowSize=2

// Flexible - general content  
SemanticChunking flexible = SemanticChunking.flexible(embeddingService);
// threshold=0.65, windowSize=4

// Custom configuration
SemanticChunking custom = new SemanticChunking(
    embeddingService,
    2000,   // maxChunkSize
    200,    // minChunkSize  
    0.75,   // similarityThreshold
    3       // slidingWindowSize
);
```

---

### 4. Markdown-Aware Chunking

**Mô tả**: Chia văn bản Markdown dựa trên cấu trúc tài liệu (headers, code blocks, lists, tables), bảo toàn hierarchy và formatting.

**Đặc điểm**:
- ✅ Bảo toàn cấu trúc Markdown
- ✅ Tôn trọng hierarchy (H1, H2, H3...)
- ✅ Preserve code blocks và tables
- ✅ Tối ưu cho technical docs
- ✅ Rich structural metadata
- ❌ Chỉ áp dụng cho Markdown
- ❌ Phức tạp về parsing
- ❌ Kích thước chunks không đều

**Cách hoạt động**:
```java
public class MarkdownAwareChunking implements ChunkingStrategy {
    // Markdown regex patterns
    private static final Pattern HEADER_PATTERN = 
        Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern CODE_BLOCK_PATTERN = 
        Pattern.compile("```[\\s\\S]*?```|`[^`]+`", Pattern.MULTILINE);
    
    @Override
    public List<DocumentChunk> chunk(RAGDocument document) {
        // 1. Analyze markdown structure
        // 2. Create sections based on headers  
        // 3. Preserve special elements
        // 4. Generate hierarchy metadata
    }
}
```

**Markdown Structure Analysis**:
```java
private MarkdownStructure analyzeMarkdownStructure(String content) {
    MarkdownStructure structure = new MarkdownStructure();
    
    // Find headers (H1-H6)
    Matcher headerMatcher = HEADER_PATTERN.matcher(content);
    while (headerMatcher.find()) {
        int level = headerMatcher.group(1).length();
        String title = headerMatcher.group(2).trim();
        int position = headerMatcher.start();
        structure.addHeader(new MarkdownHeader(level, title, position));
    }
    
    // Find code blocks  
    Matcher codeMatcher = CODE_BLOCK_PATTERN.matcher(content);
    while (codeMatcher.find()) {
        structure.addCodeBlock(new MarkdownElement(
            codeMatcher.start(), codeMatcher.end(), codeMatcher.group()));
    }
    
    // Create sections based on header hierarchy
    createSections(content, structure);
    
    return structure;
}
```

**Section Creation Algorithm**:
```java
private void createSections(String content, MarkdownStructure structure) {
    for (int i = 0; i < headers.size(); i++) {
        MarkdownHeader header = headers.get(i);
        
        // Find section end (next header at same/higher level)
        int sectionEnd = findSectionEnd(headers, i, content.length());
        
        String sectionContent = content.substring(header.position, sectionEnd);
        structure.addSection(new MarkdownSection(
            header.position, sectionEnd, header.title, 
            header.level, sectionContent));
    }
}
```

**Preservation Features**:
- **Code Blocks**: Giữ nguyên ```code``` blocks
- **Tables**: Bảo toàn format table Markdown
- **Lists**: Maintain list structure (ordered/unordered)
- **Headers**: Respect hierarchy levels (H1 > H2 > H3...)
- **Horizontal Rules**: Preserve document sections

**Cấu hình linh hoạt**:
```java
// Header-focused chunking
MarkdownAwareChunking headerFocused = MarkdownAwareChunking.headerFocused();

// Code-preserving chunking  
MarkdownAwareChunking codePreserving = MarkdownAwareChunking.codePreserving();

// Custom configuration
MarkdownAwareChunking custom = new MarkdownAwareChunking(
    1500,   // targetChunkSize
    200,    // minChunkSize
    true,   // preserveCodeBlocks
    true,   // respectHeaders  
    3       // maxHeaderLevel
);
```

**Quality Scoring cho Markdown**:
```java
private double calculateMarkdownQuality(List<MarkdownSection> sections) {
    double structureScore = Math.min(1.0, sections.size() / 3.0);
    double headerScore = sections.stream().anyMatch(s -> s.headerTitle != null) ? 1.0 : 0.5;
    double avgLength = sections.stream().mapToInt(s -> s.content.length()).average().orElse(0.0);
    double lengthScore = 1.0 - Math.abs(avgLength - targetChunkSize) / targetChunkSize;
    
    return (structureScore * 0.4 + headerScore * 0.3 + lengthScore * 0.3);
}
```

---

## Bảng So Sánh Chi Tiết

| Tiêu Chí | Fixed-Size | Sentence-Aware | Semantic | Markdown-Aware |
|----------|------------|----------------|----------|----------------|
| **Complexity** | ⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Performance** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| **Quality** | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Predictable Size** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐ |
| **Semantic Coherence** | ⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Structure Preservation** | ❌ | ⭐⭐ | ⭐ | ⭐⭐⭐⭐⭐ |
| **Resource Usage** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| **Dependencies** | None | None | EmbeddingService | None |

### Performance Comparison

| Strategy | Avg Processing Time | Memory Usage | CPU Usage | Best for Document Size |
|----------|-------------------|--------------|-----------|----------------------|
| **Fixed-Size** | ~10ms | Low | Low | Any |
| **Sentence-Aware** | ~50ms | Low-Medium | Medium | 1KB-1MB |
| **Semantic** | ~500ms | High | High | 5KB-500KB |
| **Markdown-Aware** | ~100ms | Medium | Medium | 1KB-5MB |

### Use Case Matrix

| Document Type | Best Strategy | Alternative | Avoid |
|---------------|---------------|-------------|-------|
| **Technical Docs** | Markdown-Aware | Sentence-Aware | Fixed-Size |
| **Academic Papers** | Semantic | Sentence-Aware | Fixed-Size |
| **Novels/Stories** | Sentence-Aware | Semantic | Markdown-Aware |
| **Code Documentation** | Markdown-Aware | Fixed-Size | Semantic |
| **News Articles** | Sentence-Aware | Semantic | Markdown-Aware |
| **Legal Documents** | Sentence-Aware | Semantic | Fixed-Size |
| **API Documentation** | Markdown-Aware | Sentence-Aware | Fixed-Size |
| **Bulk Processing** | Fixed-Size | Sentence-Aware | Semantic |

### Quality Metrics Comparison

| Strategy | Typical Quality Score | Coherence Score | Density Score | Completeness |
|----------|---------------------|-----------------|---------------|-------------|
| **Fixed-Size** | 0.3-0.6 | 0.2-0.5 | 0.7-0.9 | Low |
| **Sentence-Aware** | 0.6-0.8 | 0.7-0.9 | 0.8-0.9 | High |
| **Semantic** | 0.8-0.95 | 0.8-0.95 | 0.7-0.9 | Very High |
| **Markdown-Aware** | 0.7-0.9 | 0.8-0.9 | 0.8-0.9 | High |

---

## Hướng Dẫn Sử Dụng

### Lựa Chọn Strategy

#### 1. Dựa trên loại document:
```java
// For Markdown documents
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    UseCase.TECHNICAL_DOCUMENTATION, null);

// For academic content  
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    UseCase.ACADEMIC_PAPERS, embeddingService);

// For narrative content
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    UseCase.NARRATIVE_CONTENT, null);
```

#### 2. Automatic selection:
```java
// Let system choose optimal strategy
ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
    document, config, embeddingService);

// Get all recommendations ranked by quality
List<StrategyRecommendation> recommendations = 
    ChunkingFactory.getAllRecommendations(document, config, embeddingService);
```

#### 3. With fallback mechanism:
```java
ChunkingStrategy strategy = ChunkingFactory.createWithFallback(
    config, embeddingService);
```

### Cấu Hình Chi Tiết

#### ChunkingConfig Builder:
```java
ChunkingConfig config = ChunkingConfig.builder()
    .targetChunkSize(1500)
    .minChunkSize(200)  
    .maxChunkSize(2500)
    .overlapSize(300)
    .primaryStrategy(ChunkingStrategyType.SEMANTIC)
    .fallbackStrategy(ChunkingStrategyType.SENTENCE_AWARE)
    .autoSelectStrategy(true)
    .preferredQualityThreshold(0.8)
    .minQualityThreshold(0.3)
    .build();
```

#### Document-type specific configs:
```java
// Add custom config for specific document types
config.addDocumentTypeConfig(
    DocumentType.TECHNICAL_MANUAL,
    ChunkingConfig.forTechnicalDocs()
);

config.addDocumentTypeConfig(
    DocumentType.RESEARCH_PAPER,  
    ChunkingConfig.forAcademicPapers(embeddingService)
);
```

#### Strategy-specific configurations:

**Sentence-Aware Config:**
```java
ChunkingConfig config = ChunkingConfig.builder()
    .sentenceAwareConfig(
        SentenceAwareConfig.builder()
            .sizeTolerance(0.3)
            .strictPunctuation(false)
            .build())
    .build();
```

**Semantic Config:**
```java
ChunkingConfig config = ChunkingConfig.builder()
    .semanticConfig(
        SemanticConfig.builder()
            .embeddingService(embeddingService)
            .similarityThreshold(0.75)
            .slidingWindowSize(3)
            .enableBatchProcessing(true)
            .build())
    .build();
```

**Markdown Config:**
```java
ChunkingConfig config = ChunkingConfig.builder()
    .markdownConfig(
        MarkdownConfig.builder()
            .preserveCodeBlocks(true)
            .respectHeaders(true)
            .maxHeaderLevel(3)
            .preserveTables(true)
            .preserveLists(true)
            .build())
    .build();
```

### Quality Assessment

#### Strategy Suitability:
```java
// Check if strategy is suitable for document
if (strategy.isSuitableFor(document)) {
    List<DocumentChunk> chunks = strategy.chunk(document);
} else {
    // Use fallback or different strategy
    ChunkingStrategy fallback = ChunkingFactory.createDefault();
    chunks = fallback.chunk(document);
}
```

#### Quality Estimation:
```java
// Estimate quality before chunking
double estimatedQuality = strategy.estimateQuality(document);

if (estimatedQuality < 0.7) {
    // Consider different strategy or configuration
    log.warn("Low quality estimate: {}, consider different strategy", estimatedQuality);
}
```

---

## Cấu Hình và Tùy Chỉnh

### ChunkingConfig Deep Dive

#### Core Parameters:
```java
@Data
@Builder  
public class ChunkingConfig {
    // Size configuration
    @Builder.Default private int targetChunkSize = 1000;
    @Builder.Default private int minChunkSize = 200; 
    @Builder.Default private int maxChunkSize = 2000;
    @Builder.Default private int overlapSize = 200;
    
    // Strategy selection
    @Builder.Default private ChunkingStrategyType primaryStrategy = SENTENCE_AWARE;
    @Builder.Default private ChunkingStrategyType fallbackStrategy = FIXED_SIZE;
    @Builder.Default private boolean autoSelectStrategy = true;
    
    // Quality thresholds
    @Builder.Default private double minQualityThreshold = 0.3;
    @Builder.Default private double preferredQualityThreshold = 0.7;
    @Builder.Default private boolean enableQualityFallback = true;
}
```

#### Advanced Features:
```java
// Custom parameters for extensibility
config.getCustomParameters().put("custom_threshold", 0.85);
config.getCustomParameters().put("special_mode", "academic");

// Document-type specific configurations  
Map<DocumentType, ChunkingConfig> typeConfigs = new HashMap<>();
typeConfigs.put(DocumentType.TECHNICAL_MANUAL, ChunkingConfig.forTechnicalDocs());
config.setDocumentTypeConfigs(typeConfigs);

// Quality and metadata options
config.setPreserveMetadata(true);
config.setGenerateQualityMetrics(true);  
config.setMaxChunksPerDocument(100); // Limit for safety
```

### Factory Pattern Usage

#### UseCase-based Creation:
```java
public enum UseCase {
    GENERAL_PURPOSE("General purpose documents"),
    TECHNICAL_DOCUMENTATION("Technical documentation and APIs"),
    NARRATIVE_CONTENT("Stories, articles, and narrative content"),
    ACADEMIC_PAPERS("Research papers and academic content"),  
    HIGH_VOLUME_PROCESSING("High-volume batch processing");
}

// Usage
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    UseCase.ACADEMIC_PAPERS, embeddingService);
```

#### Strategy Recommendations:
```java
// Get ranked recommendations
List<StrategyRecommendation> recommendations = 
    ChunkingFactory.getAllRecommendations(document, config, embeddingService);

for (StrategyRecommendation rec : recommendations) {
    System.out.printf("%s: quality=%.3f%n", 
        rec.strategyType, rec.expectedQuality);
}

// Output example:
// SEMANTIC: quality=0.892
// SENTENCE_AWARE: quality=0.756  
// MARKDOWN_AWARE: quality=0.623
// FIXED_SIZE: quality=0.345
```

---

## Best Practices

### 1. Strategy Selection Guidelines

**For Technical Documentation:**
- **Primary**: Markdown-Aware (preserves structure)
- **Secondary**: Sentence-Aware (fallback for non-markdown)
- **Avoid**: Fixed-Size (loses code structure)

**For Academic Papers:**
- **Primary**: Semantic (topic coherence)
- **Secondary**: Sentence-Aware (preserves arguments)  
- **Configuration**: High similarity threshold (0.8+)

**For Narrative Content:**
- **Primary**: Sentence-Aware (preserves story flow)
- **Secondary**: Semantic (for complex narratives)
- **Configuration**: Flexible size tolerance (0.4+)

**For High-Volume Processing:**
- **Primary**: Fixed-Size (speed)
- **Secondary**: Sentence-Aware (better quality)
- **Configuration**: Disable quality metrics for speed

### 2. Quality Optimization

#### Size Configuration:
```java
// Optimal size ranges by content type
Map<ContentType, SizeConfig> sizeGuide = Map.of(
    ContentType.TECHNICAL, new SizeConfig(1200, 2000, 300),
    ContentType.ACADEMIC, new SizeConfig(1500, 2500, 400),  
    ContentType.NARRATIVE, new SizeConfig(1000, 1800, 250),
    ContentType.NEWS, new SizeConfig(800, 1500, 200)
);
```

#### Quality Thresholds:
```java
// Recommended quality thresholds
Map<UseCase, QualityThresholds> qualityGuide = Map.of(
    UseCase.ACADEMIC_PAPERS, new QualityThresholds(0.7, 0.9),
    UseCase.TECHNICAL_DOCS, new QualityThresholds(0.6, 0.8),
    UseCase.GENERAL_PURPOSE, new QualityThresholds(0.5, 0.7),
    UseCase.HIGH_VOLUME, new QualityThresholds(0.3, 0.5)
);
```

### 3. Performance Optimization

#### Batch Processing:
```java
// For multiple documents
List<RAGDocument> documents = loadDocuments();
ChunkingStrategy strategy = createOptimalStrategy();

// Process in batches for memory efficiency
int batchSize = 100;
for (int i = 0; i < documents.size(); i += batchSize) {
    List<RAGDocument> batch = documents.subList(i, 
        Math.min(i + batchSize, documents.size()));
    
    List<DocumentChunk> allChunks = batch.parallelStream()
        .flatMap(doc -> strategy.chunk(doc).stream())
        .collect(Collectors.toList());
    
    persistChunks(allChunks);
}
```

#### Caching Strategies:
```java
// Cache embeddings for semantic chunking
@Component
public class CachedEmbeddingService implements EmbeddingService {
    private final Cache<String, float[]> embeddingCache = 
        Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build();
    
    @Override
    public float[] embed(String text) {
        return embeddingCache.get(text, this::computeEmbedding);
    }
}
```

### 4. Error Handling and Fallbacks

#### Robust Strategy Implementation:
```java
@Component
public class RobustChunkingService {
    
    public List<DocumentChunk> chunkDocument(RAGDocument document, 
                                           ChunkingConfig config) {
        try {
            // Try primary strategy
            ChunkingStrategy primary = createStrategy(config.getPrimaryStrategy());
            if (primary.isSuitableFor(document)) {
                return primary.chunk(document);
            }
        } catch (Exception e) {
            log.warn("Primary strategy failed: {}", e.getMessage());
        }
        
        try {
            // Try fallback strategy
            ChunkingStrategy fallback = createStrategy(config.getFallbackStrategy());
            return fallback.chunk(document);
        } catch (Exception e) {
            log.error("Fallback strategy failed: {}", e.getMessage());
        }
        
        // Last resort: fixed-size chunking
        return new FixedSizeChunking(1000, 200).chunk(document);
    }
}
```

---

## Ví Dụ Thực Tế

### Example 1: Technical Documentation

**Input Document** (Markdown):
```markdown
# API Documentation

## Authentication
All API calls require authentication using JWT tokens.

### Getting a Token
Send a POST request to `/auth/login`:

```json
{
  "username": "user@example.com",
  "password": "password123"
}
```

## User Management

### Create User
POST `/users` - Create a new user account.

### Update User  
PUT `/users/{id}` - Update existing user.
```

**Markdown-Aware Chunking Result**:
```java
// Chunk 1: Authentication Section
DocumentChunk chunk1 = DocumentChunk.builder()
    .content("# API Documentation\n\n## Authentication\nAll API calls require authentication using JWT tokens.\n\n### Getting a Token\nSend a POST request to `/auth/login`:\n\n```json\n{\n  \"username\": \"user@example.com\",\n  \"password\": \"password123\"\n}\n```")
    .sectionTitle("Authentication")
    .hierarchyLevel(2)
    .qualityScore(0.89)
    .build();

// Chunk 2: User Management Section  
DocumentChunk chunk2 = DocumentChunk.builder()
    .content("## User Management\n\n### Create User\nPOST `/users` - Create a new user account.\n\n### Update User\nPUT `/users/{id}` - Update existing user.")
    .sectionTitle("User Management")  
    .hierarchyLevel(2)
    .qualityScore(0.85)
    .build();
```

### Example 2: Academic Paper

**Input Document**:
```text
Machine learning has revolutionized the field of artificial intelligence. 
The development of neural networks, particularly deep learning architectures, 
has enabled unprecedented advances in computer vision and natural language processing.

Recent studies have shown that transformer models achieve state-of-the-art 
performance across multiple domains. The attention mechanism, first introduced 
in sequence-to-sequence models, has become a fundamental building block.

However, these models face significant challenges in terms of computational 
efficiency and interpretability. Research efforts are now focusing on 
developing more efficient architectures and better understanding mechanisms.
```

**Semantic Chunking Process**:
```java
// 1. Create text segments (sentences)
List<TextSegment> segments = [
    "Machine learning has revolutionized...",
    "The development of neural networks...",  
    "Recent studies have shown...",
    "The attention mechanism...",
    "However, these models face...",
    "Research efforts are now focusing..."
];

// 2. Generate embeddings
float[][] embeddings = embeddingService.embedBatch(segmentTexts);

// 3. Calculate similarities and group
// Segment 1-2: High similarity (ML/DL concepts) -> Group 1
// Segment 3-4: High similarity (Transformers/Attention) -> Group 2  
// Segment 5-6: High similarity (Challenges/Future) -> Group 3

// Result: 3 semantically coherent chunks
```

**Result**:
```java
// Chunk 1: ML/DL Introduction (coherence: 0.92)
DocumentChunk chunk1 = DocumentChunk.builder()
    .content("Machine learning has revolutionized the field of artificial intelligence. The development of neural networks, particularly deep learning architectures, has enabled unprecedented advances in computer vision and natural language processing.")
    .coherenceScore(0.92)
    .qualityScore(0.88)
    .build();

// Chunk 2: Transformers/Attention (coherence: 0.89) 
DocumentChunk chunk2 = DocumentChunk.builder()
    .content("Recent studies have shown that transformer models achieve state-of-the-art performance across multiple domains. The attention mechanism, first introduced in sequence-to-sequence models, has become a fundamental building block.")
    .coherenceScore(0.89)
    .qualityScore(0.87)
    .build();

// Chunk 3: Challenges/Future (coherence: 0.91)
DocumentChunk chunk3 = DocumentChunk.builder()
    .content("However, these models face significant challenges in terms of computational efficiency and interpretability. Research efforts are now focusing on developing more efficient architectures and better understanding mechanisms.")
    .coherenceScore(0.91)
    .qualityScore(0.86)
    .build();
```

### Example 3: Batch Processing Pipeline

**Complete Processing Workflow**:
```java
@Service
public class DocumentProcessingService {
    
    @Autowired private ChunkingService chunkingService;
    @Autowired private EmbeddingService embeddingService;
    @Autowired private VectorStoreService vectorStore;
    
    @Async
    public CompletableFuture<ProcessingResult> processDocuments(
            List<RAGDocument> documents) {
        
        // 1. Determine optimal chunking strategy for each document
        Map<RAGDocument, ChunkingStrategy> strategies = documents.parallelStream()
            .collect(Collectors.toMap(
                doc -> doc,
                doc -> ChunkingFactory.createOptimalStrategy(
                    doc, getConfigForDocument(doc), embeddingService)
            ));
        
        // 2. Chunk all documents
        List<DocumentChunk> allChunks = documents.parallelStream()
            .flatMap(doc -> {
                ChunkingStrategy strategy = strategies.get(doc);
                List<DocumentChunk> chunks = strategy.chunk(doc);
                
                log.info("Chunked document {} into {} chunks using {} strategy", 
                    doc.getId(), chunks.size(), strategy.getStrategyName());
                
                return chunks.stream();
            })
            .collect(Collectors.toList());
        
        // 3. Generate embeddings for chunks (batch processing)
        List<float[]> embeddings = generateEmbeddingsInBatches(allChunks);
        
        // 4. Store in vector database
        vectorStore.storeChunks(allChunks, embeddings);
        
        return CompletableFuture.completedFuture(
            new ProcessingResult(allChunks.size(), calculateAverageQuality(allChunks))
        );
    }
    
    private ChunkingConfig getConfigForDocument(RAGDocument document) {
        return switch (document.getType()) {
            case TECHNICAL_MANUAL -> ChunkingConfig.forTechnicalDocs();
            case RESEARCH_PAPER -> ChunkingConfig.forAcademicPapers(embeddingService);
            case NEWS_ARTICLE -> ChunkingConfig.forNarrativeContent();
            default -> ChunkingConfig.defaults();
        };
    }
}
```

---

## Kết Luận

Hệ thống chunking được thiết kế với 4 strategies chính, mỗi strategy có những ưu điểm và use cases riêng:

1. **Fixed-Size**: Đơn giản, nhanh, phù hợp xử lý khối lượng lớn
2. **Sentence-Aware**: Cân bằng giữa quality và performance, phù hợp văn bản narrative  
3. **Semantic**: Chất lượng cao nhất, phù hợp academic content và complex documents
4. **Markdown-Aware**: Tối ưu cho technical documentation và structured content

**Recommendation Engine** tự động lựa chọn strategy phù hợp nhất dựa trên:
- Document type và structure analysis
- Content length và complexity
- Quality requirements
- Performance constraints

**Quality Metrics** cung cấp feedback để tối ưu hóa:
- Quality Score (0.0-1.0): Chất lượng tổng thể
- Coherence Score (0.0-1.0): Tính nhất quán semantic
- Density Score (0.0-1.0): Mật độ thông tin

**Extensibility**: Hệ thống được thiết kế để dễ dàng thêm strategies mới và custom configurations cho các use cases đặc biệt.