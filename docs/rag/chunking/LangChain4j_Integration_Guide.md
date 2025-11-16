# LangChain4j Integration Guide

## Tổng quan

Tài liệu này cung cấp hướng dẫn chi tiết về việc tích hợp thư viện LangChain4j với hệ thống PCM Chunking. Tích hợp này
cho phép sử dụng các text splitter chất lượng cao từ LangChain4j cùng với framework chunking mạnh mẽ của PCM.

## Mục lục

1. [Giới thiệu](#giới-thiệu)
2. [Cài đặt và Cấu hình](#cài-đặt-và-cấu-hình)
3. [Kiến trúc Tích hợp](#kiến-trúc-tích-hợp)
4. [Các Loại Splitter](#các-loại-splitter)
5. [Cách sử dụng](#cách-sử-dụng)
6. [Configuration Options](#configuration-options)
7. [Use Cases](#use-cases)
8. [So sánh với PCM Strategies](#so-sánh-với-pcm-strategies)
9. [Best Practices](#best-practices)
10. [Troubleshooting](#troubleshooting)

---

## Giới thiệu

### LangChain4j là gì?

LangChain4j là thư viện Java mã nguồn mở cung cấp các công cụ mạnh mẽ để xây dựng ứng dụng LLM. Thư viện này bao gồm các
DocumentSplitter chất lượng cao đã được kiểm nghiệm qua hàng nghìn ứng dụng thực tế.

### Tại sao tích hợp LangChain4j?

- **Chất lượng cao**: Algorithms đã được battle-tested
- **Cộng đồng mạnh**: Hỗ trợ và cập nhật tích cực
- **Tính tương thích**: Hoạt động tốt với LangChain ecosystem
- **Đa dạng**: 7 loại splitter khác nhau cho các use case khác nhau

### Hybrid Approach

PCM hiện tại cung cấp **15 chunking strategies**:

- **4 PCM strategies** (custom implementation)
- **4 LangChain strategies** (custom implementation)
- **7 LangChain4j strategies** (real library)

---

## Cài đặt và Cấu hình

### Dependencies

Hệ thống đã bao gồm các thư viện cần thiết:

```
lib/langchain4j/
├── langchain4j-1.8.0.jar
└── langchain4j-core-1.8.0.jar
```

### Build Requirements

- Java 21+
- Tất cả dependencies đã được tích hợp trong build script

### Verification

```bash
# Build project
./scripts/build.sh

# Verify integration
# Kiểm tra logs để đảm bảo LangChain4j libraries được load
```

---

## Kiến trúc Tích hợp

### Sơ đồ kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                    PCM Chunking System                         │
├─────────────────────────────────────────────────────────────────┤
│  ChunkingFactory & ChunkingConfig (Unified Interface)          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │ PCM         │  │ LangChain   │  │ LangChain4j             │  │
│  │ Strategies  │  │ Strategies  │  │ Strategies              │  │
│  │ (4 types)   │  │ (4 types)   │  │ (7 types)               │  │
│  │             │  │             │  │                         │  │
│  │ • FixedSize │  │ • Character │  │ • Paragraph             │  │
│  │ • Sentence  │  │ • Recursive │  │ • Sentence              │  │
│  │ • Semantic  │  │ • Token     │  │ • Word                  │  │
│  │ • Markdown  │  │ • Code      │  │ • Line                  │  │
│  │             │  │             │  │ • Character             │  │
│  │             │  │             │  │ • Regex                 │  │
│  │             │  │             │  │ • Hierarchical          │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                    DocumentChunk (Unified Output)              │
└─────────────────────────────────────────────────────────────────┘
```

### Core Components

#### 1. LangChain4jAdapter

```java
public class LangChain4jAdapter implements ChunkingStrategy {
    private final DocumentSplitter langchain4jSplitter;
    private final LangChain4jConfig config;
    
    // Bridges LangChain4j splitters with PCM interface
}
```

#### 2. LangChain4jConfig

```java
@Data @Builder(toBuilder = true)
public class LangChain4jConfig {
    private SplitterType splitterType;
    private int maxSegmentSizeInChars;
    private int maxOverlapSizeInChars;
    private boolean useTokenizer;
    // ... more configuration options
}
```

#### 3. LangChain4jSplitterFactory

```java
public class LangChain4jSplitterFactory {
    public static DocumentSplitter createSplitter(LangChain4jConfig config);
    // Factory methods for different splitter types
}
```

---

## Các Loại Splitter

### 1. Paragraph Splitter (`LANGCHAIN4J_PARAGRAPH`)

**Mô tả**: Chia văn bản theo đoạn văn, tối ưu cho nội dung có cấu trúc đoạn văn rõ ràng.

```java
// Sử dụng trực tiếp
DocumentSplitter splitter = new DocumentByParagraphSplitter(1000, 200);
LangChain4jAdapter adapter = new LangChain4jAdapter(splitter, config);

// Hoặc qua Factory
ChunkingConfig config = ChunkingConfig.forLangChain4jParagraph();
ChunkingStrategy strategy = ChunkingFactory.createStrategy(config);
```

**Phù hợp với**:

- Tài liệu kỹ thuật
- Bài viết blog
- Nội dung marketing

### 2. Sentence Splitter (`LANGCHAIN4J_SENTENCE`)

**Mô tả**: Chia văn bản theo câu, đảm bảo tính toàn vẹn của ý nghĩa câu.

```java
ChunkingConfig config = ChunkingConfig.forLangChain4jSentence();
ChunkingStrategy strategy = ChunkingFactory.createStrategy(config);
```

**Phù hợp với**:

- Văn học, tiểu thuyết
- Nội dung giáo dục
- Phân tích ngữ nghĩa

### 3. Word Splitter (`LANGCHAIN4J_WORD`)

**Mô tả**: Chia văn bản theo từ, control chính xác số lượng từ trong mỗi chunk.

```java
ChunkingConfig config = ChunkingConfig.forLangChain4jWord();
```

**Phù hợp với**:

- Text classification
- Sentiment analysis
- Token counting cho LLM

### 4. Line Splitter (`LANGCHAIN4J_LINE`)

**Mô tả**: Chia theo dòng, tối ưu cho nội dung có cấu trúc dòng rõ ràng.

```java
ChunkingConfig config = ChunkingConfig.forLangChain4jLine();
```

**Phù hợp với**:

- Code files
- Logs
- Structured data

### 5. Character Splitter (`LANGCHAIN4J_CHARACTER`)

**Mô tả**: Chia theo số ký tự, control chính xác kích thước chunk.

```java
ChunkingConfig config = ChunkingConfig.forLangChain4jCharacter();
```

**Phù hợp với**:

- High precision chunking
- Memory-constrained environments
- Exact size requirements

### 6. Regex Splitter (`LANGCHAIN4J_REGEX`)

**Mô tả**: Chia theo pattern regex tùy chỉnh.

```java
ChunkingConfig config = ChunkingConfig.forLangChain4jRegex("\\n\\n");
```

**Phù hợp với**:

- Custom document formats
- Structured text với delimiters đặc biệt
- Domain-specific content

### 7. Hierarchical Splitter (`LANGCHAIN4J_HIERARCHICAL`)

**Mô tả**: Splitter thông minh nhất, thử nhiều strategies theo thứ tự:

1. Paragraph → 2. Sentence → 3. Word → 4. Character

```java
ChunkingConfig config = ChunkingConfig.forLangChain4jHierarchical();
```

**Phù hợp với**:

- General-purpose chunking
- Mixed content types
- Unknown document structures

---

## Cách sử dụng

### 1. Sử dụng qua Factory (Khuyến nghị)

```java
// Tạo strategy từ config
ChunkingConfig config = ChunkingConfig.forLangChain4jParagraph();
ChunkingStrategy strategy = ChunkingFactory.createStrategy(config);

// Chunk document
RAGDocument document = createDocument();
List<DocumentChunk> chunks = strategy.chunk(document);
```

### 2. Sử dụng qua Use Case

```java
// Sử dụng use case specific
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    ChunkingFactory.UseCase.LANGCHAIN4J_PARAGRAPH_FOCUSED, 
    null
);

List<DocumentChunk> chunks = strategy.chunk(document);
```

### 3. Sử dụng trực tiếp LangChain4j

```java
// Tạo splitter trực tiếp
DocumentSplitter langchain4jSplitter = new DocumentByParagraphSplitter(800, 100);

// Wrap với adapter
LangChain4jConfig config = LangChain4jConfig.defaults();
LangChain4jAdapter adapter = new LangChain4jAdapter(langchain4jSplitter, config);

// Sử dụng
List<DocumentChunk> chunks = adapter.chunk(document);
```

### 4. Custom Configuration

```java
// Tạo custom configuration
LangChain4jConfig customConfig = LangChain4jConfig.builder()
    .splitterType(LangChain4jConfig.SplitterType.BY_SENTENCE)
    .maxSegmentSizeInChars(600)
    .maxOverlapSizeInChars(80)
    .useTokenizer(false)
    .includeSegmentMetadata(true)
    .calculateQualityScores(true)
    .build();

// Tạo splitter với custom config
DocumentSplitter splitter = LangChain4jSplitterFactory.createSplitter(customConfig);
LangChain4jAdapter adapter = new LangChain4jAdapter(splitter, customConfig);
```

---

## Configuration Options

### LangChain4jConfig Parameters

| Parameter                  | Type           | Default           | Mô tả                        |
|----------------------------|----------------|-------------------|------------------------------|
| `splitterType`             | `SplitterType` | `BY_PARAGRAPH`    | Loại splitter sử dụng        |
| `maxSegmentSizeInChars`    | `int`          | `1000`            | Kích thước tối đa (ký tự)    |
| `maxOverlapSizeInChars`    | `int`          | `200`             | Overlap giữa chunks (ký tự)  |
| `maxSegmentSizeInTokens`   | `int`          | `250`             | Kích thước tối đa (tokens)   |
| `maxOverlapSizeInTokens`   | `int`          | `50`              | Overlap (tokens)             |
| `useTokenizer`             | `boolean`      | `false`           | Sử dụng token-based sizing   |
| `tokenizerModel`           | `String`       | `"gpt-3.5-turbo"` | Model cho tokenizer          |
| `includeSegmentMetadata`   | `boolean`      | `true`            | Include metadata từ segments |
| `preserveOriginalMetadata` | `boolean`      | `true`            | Preserve metadata gốc        |
| `calculateQualityScores`   | `boolean`      | `true`            | Tính quality scores          |

### SplitterType Enum

```java
public enum SplitterType {
    BY_PARAGRAPH,     // DocumentByParagraphSplitter
    BY_SENTENCE,      // DocumentBySentenceSplitter  
    BY_WORD,          // DocumentByWordSplitter
    BY_LINE,          // DocumentByLineSplitter
    BY_CHARACTER,     // DocumentByCharacterSplitter
    BY_REGEX,         // DocumentByRegexSplitter
    HIERARCHICAL      // HierarchicalDocumentSplitter
}
```

### Factory Methods

```java
// Basic configurations
LangChain4jConfig.defaults()
LangChain4jConfig.forCharacterParagraphSplitter(maxChars, overlapChars)
LangChain4jConfig.forTokenParagraphSplitter(maxTokens, overlapTokens, model)
LangChain4jConfig.forSentenceSplitter(maxChars, overlapChars)
LangChain4jConfig.forWordSplitter(maxChars, overlapChars)
LangChain4jConfig.forLineSplitter(maxChars, overlapChars)
LangChain4jConfig.forCharacterSplitter(maxChars, overlapChars)
LangChain4jConfig.forRegexSplitter(regex, maxChars, overlapChars)
LangChain4jConfig.forHierarchicalSplitter(maxChars, overlapChars)
```

---

## Use Cases

### LangChain4j Specific Use Cases

| Use Case                         | Strategy                   | Mô tả                   |
|----------------------------------|----------------------------|-------------------------|
| `LANGCHAIN4J_PARAGRAPH_FOCUSED`  | `LANGCHAIN4J_PARAGRAPH`    | Tập trung vào đoạn văn  |
| `LANGCHAIN4J_SENTENCE_PRECISE`   | `LANGCHAIN4J_SENTENCE`     | Chính xác đến câu       |
| `LANGCHAIN4J_WORD_GRANULAR`      | `LANGCHAIN4J_WORD`         | Control từng từ         |
| `LANGCHAIN4J_LINE_STRUCTURED`    | `LANGCHAIN4J_LINE`         | Theo cấu trúc dòng      |
| `LANGCHAIN4J_CHARACTER_EXACT`    | `LANGCHAIN4J_CHARACTER`    | Exact character count   |
| `LANGCHAIN4J_REGEX_PATTERN`      | `LANGCHAIN4J_REGEX`        | Pattern-based splitting |
| `LANGCHAIN4J_HIERARCHICAL_SMART` | `LANGCHAIN4J_HIERARCHICAL` | Smart hierarchical      |

### Sử dụng Use Cases

```java
// Paragraph-focused processing
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    ChunkingFactory.UseCase.LANGCHAIN4J_PARAGRAPH_FOCUSED, null);

// Sentence-precise processing  
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    ChunkingFactory.UseCase.LANGCHAIN4J_SENTENCE_PRECISE, null);

// Smart hierarchical processing
ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
    ChunkingFactory.UseCase.LANGCHAIN4J_HIERARCHICAL_SMART, null);
```

---

## So sánh với PCM Strategies

### Comparison Table

| Feature            | PCM Strategies    | LangChain Strategies  | LangChain4j Strategies   |
|--------------------|-------------------|-----------------------|--------------------------|
| **Implementation** | Custom PCM        | Custom PCM            | Real LangChain4j Library |
| **Quality**        | High              | High                  | Battle-tested            |
| **Maintenance**    | PCM Team          | PCM Team              | LangChain4j Community    |
| **Updates**        | PCM Release       | PCM Release           | Library Updates          |
| **Customization**  | Full Control      | Full Control          | Library API              |
| **Performance**    | Optimized for PCM | Optimized for PCM     | LangChain4j Optimized    |
| **Ecosystem**      | PCM Native        | PCM + LangChain Style | LangChain Ecosystem      |

### Performance Characteristics

#### PCM Strategies

- **Pros**: Tối ưu cho PCM, full customization, semantic chunking
- **Cons**: Limited ecosystem support
- **Best for**: PCM-specific requirements, semantic analysis

#### LangChain Strategies (Custom)

- **Pros**: LangChain compatibility, PCM integration
- **Cons**: Custom implementation, maintenance overhead
- **Best for**: LangChain-style processing with PCM control

#### LangChain4j Strategies

- **Pros**: Battle-tested, community support, regular updates
- **Cons**: Limited customization, library dependency
- **Best for**: Production reliability, standard text processing

---

## Best Practices

### 1. Strategy Selection

```java
// Lấy recommendations cho document
List<ChunkingFactory.StrategyRecommendation> recommendations = 
    ChunkingFactory.getAllRecommendations(document, config, embeddingService);

// Filter LangChain4j strategies
List<ChunkingFactory.StrategyRecommendation> langchain4jRecs = 
    recommendations.stream()
        .filter(rec -> rec.strategyType.name().startsWith("LANGCHAIN4J"))
        .toList();

// Chọn strategy tốt nhất
ChunkingStrategy bestStrategy = langchain4jRecs.get(0).strategy;
```

### 2. Configuration Tuning

```java
// Start với default, sau đó tune
LangChain4jConfig config = LangChain4jConfig.defaults();

// Adjust cho document type
if (document.getType() == DocumentType.TECHNICAL) {
    config = config.toBuilder()
        .maxSegmentSizeInChars(1200)  // Larger for technical content
        .maxOverlapSizeInChars(150)
        .build();
} else if (document.getType() == DocumentType.NARRATIVE) {
    config = config.toBuilder()
        .splitterType(SplitterType.BY_SENTENCE)  // Better for stories
        .maxSegmentSizeInChars(800)
        .build();
}
```

### 3. Error Handling

```java
try {
    DocumentSplitter splitter = LangChain4jSplitterFactory.createSplitter(config);
    LangChain4jAdapter adapter = new LangChain4jAdapter(splitter, config);
    List<DocumentChunk> chunks = adapter.chunk(document);
    
} catch (Exception e) {
    // Fallback to PCM strategy
    ChunkingStrategy fallback = ChunkingFactory.createStrategy(
        ChunkingConfig.forSentenceAware()
    );
    List<DocumentChunk> chunks = fallback.chunk(document);
}
```

### 4. Performance Optimization

```java
// For high-volume processing
LangChain4jConfig optimizedConfig = LangChain4jConfig.builder()
    .splitterType(SplitterType.BY_CHARACTER)  // Fastest
    .calculateQualityScores(false)            // Skip expensive calculations
    .includeSegmentMetadata(false)            // Minimal metadata
    .build();
```

### 5. Quality Assurance

```java
List<DocumentChunk> chunks = adapter.chunk(document);

// Verify quality
double avgQuality = chunks.stream()
    .mapToDouble(chunk -> chunk.getQualityScore())
    .average()
    .orElse(0.0);

if (avgQuality < 0.7) {
    // Try different strategy or configuration
    ChunkingStrategy fallback = ChunkingFactory.createStrategy(
        ChunkingConfig.forLangChain4jHierarchical()
    );
    chunks = fallback.chunk(document);
}
```

---

## Troubleshooting

### Common Issues

#### 1. LangChain4j Library Not Found

```
Error: java.lang.ClassNotFoundException: dev.langchain4j.data.document.DocumentSplitter
```

**Solution**:

```bash
# Verify libraries exist
ls -la lib/langchain4j/

# Rebuild project
./scripts/build.sh
```

#### 2. Configuration Validation Errors

```
Error: IllegalArgumentException: maxSegmentSizeInChars must be positive
```

**Solution**:

```java
// Validate configuration before use
try {
    config.validate();
} catch (IllegalArgumentException e) {
    // Fix configuration
    config = config.toBuilder()
        .maxSegmentSizeInChars(1000)
        .maxOverlapSizeInChars(200)
        .build();
}
```

#### 3. Memory Issues với Large Documents

```
Error: OutOfMemoryError when processing large documents
```

**Solution**:

```java
// Use smaller chunks for large documents
LangChain4jConfig memoryFriendlyConfig = LangChain4jConfig.builder()
    .maxSegmentSizeInChars(500)  // Smaller chunks
    .maxOverlapSizeInChars(50)
    .calculateQualityScores(false)  // Skip expensive operations
    .build();
```

#### 4. Poor Chunking Quality

```
Warning: Average quality score below threshold
```

**Solution**:

```java
// Try hierarchical splitter
ChunkingConfig betterConfig = ChunkingConfig.forLangChain4jHierarchical();

// Or compare multiple strategies
List<ChunkingFactory.StrategyRecommendation> recommendations = 
    ChunkingFactory.getAllRecommendations(document, config, null);

ChunkingStrategy bestStrategy = recommendations.get(0).strategy;
```

### Debug Tips

#### 1. Enable Detailed Logging

```java
// Add to logback.xml
<logger name="com.noteflix.pcm.rag.chunking.langchain4j" level="DEBUG"/>
```

#### 2. Inspect Chunk Metadata

```java
for (DocumentChunk chunk : chunks) {
    System.out.println("Splitter: " + chunk.getMetadata().get("langchain4j_splitter"));
    System.out.println("Quality: " + chunk.getQualityScore());
    System.out.println("Size: " + chunk.getContent().length());
}
```

#### 3. Test Different Configurations

```java
LangChain4jConfig[] testConfigs = {
    LangChain4jConfig.forCharacterParagraphSplitter(800, 100),
    LangChain4jConfig.forSentenceSplitter(600, 80),
    LangChain4jConfig.forHierarchicalSplitter(1000, 150)
};

for (LangChain4jConfig config : testConfigs) {
    DocumentSplitter splitter = LangChain4jSplitterFactory.createSplitter(config);
    // Test and compare results
}
```

---

## Example Code

### Complete Example

```java
package examples;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.core.ChunkingFactory;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jAdapter;
import com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.DocumentType;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;

import java.util.List;

public class LangChain4jCompleteExample {
    
    public static void main(String[] args) {
        // 1. Create document
        RAGDocument document = RAGDocument.builder()
            .id("example-doc")
            .type(DocumentType.TEXT)
            .content("Your document content here...")
            .title("Example Document")
            .build();
        
        // 2. Method 1: Factory approach (Recommended)
        ChunkingStrategy strategy = ChunkingFactory.createStrategy(
            ChunkingConfig.forLangChain4jParagraph()
        );
        List<DocumentChunk> chunks = strategy.chunk(document);
        
        // 3. Method 2: Direct LangChain4j usage
        DocumentSplitter splitter = new DocumentByParagraphSplitter(800, 100);
        LangChain4jConfig config = LangChain4jConfig.defaults();
        LangChain4jAdapter adapter = new LangChain4jAdapter(splitter, config);
        List<DocumentChunk> directChunks = adapter.chunk(document);
        
        // 4. Method 3: Use case approach
        ChunkingStrategy useCaseStrategy = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.LANGCHAIN4J_HIERARCHICAL_SMART, null
        );
        List<DocumentChunk> useCaseChunks = useCaseStrategy.chunk(document);
        
        // 5. Process results
        processChunks(chunks);
    }
    
    private static void processChunks(List<DocumentChunk> chunks) {
        System.out.println("Created " + chunks.size() + " chunks");
        
        for (DocumentChunk chunk : chunks) {
            System.out.println("Chunk ID: " + chunk.getId());
            System.out.println("Strategy: " + chunk.getStrategyUsed());
            System.out.println("Quality: " + chunk.getQualityScore());
            System.out.println("Content: " + chunk.getContent().substring(0, 
                Math.min(100, chunk.getContent().length())) + "...");
            System.out.println("---");
        }
    }
}
```

---

## Kết luận

Tích hợp LangChain4j với PCM chunking system mang lại:

1. **15 chunking strategies** cho maximum flexibility
2. **Production-ready quality** từ LangChain4j library
3. **Unified interface** qua PCM framework
4. **Full backward compatibility** với existing code
5. **Rich configuration options** cho fine-tuning
6. **Battle-tested algorithms** từ LangChain community

Việc tích hợp này cho phép users chọn strategy tốt nhất cho từng use case cụ thể, từ high-performance chunking đến
precise semantic analysis.

**Recommendation**: Bắt đầu với `LANGCHAIN4J_HIERARCHICAL` cho general-purpose chunking, sau đó tune configuration theo
requirements cụ thể.