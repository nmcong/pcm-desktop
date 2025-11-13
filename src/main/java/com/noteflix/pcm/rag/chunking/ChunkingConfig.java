package com.noteflix.pcm.rag.chunking;

import com.noteflix.pcm.rag.embedding.EmbeddingService;
import com.noteflix.pcm.rag.model.DocumentType;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Configuration class for chunking strategies.
 *
 * <p>Provides a flexible way to configure different chunking strategies with various parameters,
 * quality thresholds, and document-type-specific settings.
 *
 * <p>Features:
 * - Strategy-specific configuration
 * - Document-type aware settings
 * - Quality thresholds and fallback options
 * - Extensible parameter system
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class ChunkingConfig {

  // === Strategy Selection ===
  
  /** Primary chunking strategy to use */
  @Builder.Default
  private ChunkingStrategyType primaryStrategy = ChunkingStrategyType.SENTENCE_AWARE;
  
  /** Fallback strategy if primary fails */
  @Builder.Default
  private ChunkingStrategyType fallbackStrategy = ChunkingStrategyType.FIXED_SIZE;
  
  /** Whether to auto-select best strategy based on document analysis */
  @Builder.Default
  private boolean autoSelectStrategy = true;

  // === Size Configuration ===
  
  /** Target chunk size in characters */
  @Builder.Default
  private int targetChunkSize = 1000;
  
  /** Minimum chunk size in characters */
  @Builder.Default
  private int minChunkSize = 200;
  
  /** Maximum chunk size in characters */
  @Builder.Default
  private int maxChunkSize = 2000;
  
  /** Overlap size for applicable strategies */
  @Builder.Default
  private int overlapSize = 200;

  // === Quality Thresholds ===
  
  /** Minimum quality score to accept a chunking result (0.0-1.0) */
  @Builder.Default
  private double minQualityThreshold = 0.3;
  
  /** Preferred quality score for strategy selection (0.0-1.0) */
  @Builder.Default
  private double preferredQualityThreshold = 0.7;
  
  /** Whether to fallback to simpler strategy if quality is too low */
  @Builder.Default
  private boolean enableQualityFallback = true;

  // === Strategy-Specific Configuration ===
  
  /** Configuration for sentence-aware chunking */
  @Builder.Default
  private SentenceAwareConfig sentenceAwareConfig = SentenceAwareConfig.defaults();
  
  /** Configuration for semantic chunking */
  @Builder.Default
  private SemanticConfig semanticConfig = SemanticConfig.defaults();
  
  /** Configuration for markdown-aware chunking */
  @Builder.Default
  private MarkdownConfig markdownConfig = MarkdownConfig.defaults();
  
  /** Configuration for fixed-size chunking */
  @Builder.Default
  private FixedSizeConfig fixedSizeConfig = FixedSizeConfig.defaults();

  // === Document-Type Specific Settings ===
  
  /** Document-type specific configurations */
  @Builder.Default
  private Map<DocumentType, ChunkingConfig> documentTypeConfigs = new HashMap<>();
  
  /** Whether to use document-type specific configurations */
  @Builder.Default
  private boolean useDocumentTypeConfigs = true;

  // === Advanced Options ===
  
  /** Custom parameters for extensibility */
  @Builder.Default
  private Map<String, Object> customParameters = new HashMap<>();
  
  /** Whether to preserve metadata across chunking */
  @Builder.Default
  private boolean preserveMetadata = true;
  
  /** Whether to generate quality metrics */
  @Builder.Default
  private boolean generateQualityMetrics = true;
  
  /** Maximum number of chunks per document (0 = unlimited) */
  @Builder.Default
  private int maxChunksPerDocument = 0;

  // === Factory Methods ===

  /** Create default configuration for general documents. */
  public static ChunkingConfig defaults() {
    return ChunkingConfig.builder().build();
  }

  /** Create configuration optimized for technical documentation. */
  public static ChunkingConfig forTechnicalDocs() {
    return ChunkingConfig.builder()
        .primaryStrategy(ChunkingStrategyType.MARKDOWN_AWARE)
        .fallbackStrategy(ChunkingStrategyType.SENTENCE_AWARE)
        .targetChunkSize(1500)
        .maxChunkSize(2500)
        .markdownConfig(MarkdownConfig.codePreserving())
        .build();
  }

  /** Create configuration for narrative/literary content. */
  public static ChunkingConfig forNarrativeContent() {
    return ChunkingConfig.builder()
        .primaryStrategy(ChunkingStrategyType.SENTENCE_AWARE)
        .fallbackStrategy(ChunkingStrategyType.FIXED_SIZE)
        .targetChunkSize(1200)
        .sentenceAwareConfig(SentenceAwareConfig.flexible())
        .preferredQualityThreshold(0.8)
        .build();
  }

  /** Create configuration for academic/research papers. */
  public static ChunkingConfig forAcademicPapers(EmbeddingService embeddingService) {
    return ChunkingConfig.builder()
        .primaryStrategy(ChunkingStrategyType.SEMANTIC)
        .fallbackStrategy(ChunkingStrategyType.SENTENCE_AWARE)
        .targetChunkSize(1800)
        .maxChunkSize(3000)
        .semanticConfig(SemanticConfig.precise(embeddingService))
        .preferredQualityThreshold(0.8)
        .build();
  }

  /** Create fast configuration for high-volume processing. */
  public static ChunkingConfig forHighVolume() {
    return ChunkingConfig.builder()
        .primaryStrategy(ChunkingStrategyType.FIXED_SIZE)
        .fallbackStrategy(ChunkingStrategyType.FIXED_SIZE)
        .autoSelectStrategy(false)
        .generateQualityMetrics(false)
        .targetChunkSize(800)
        .overlapSize(100)
        .build();
  }

  // === Validation Methods ===

  /**
   * Validate configuration parameters.
   *
   * @throws IllegalArgumentException if configuration is invalid
   */
  public void validate() {
    if (targetChunkSize <= 0) {
      throw new IllegalArgumentException("Target chunk size must be positive");
    }
    if (minChunkSize <= 0 || minChunkSize >= targetChunkSize) {
      throw new IllegalArgumentException("Invalid min chunk size");
    }
    if (maxChunkSize <= targetChunkSize) {
      throw new IllegalArgumentException("Max chunk size must be greater than target");
    }
    if (overlapSize < 0 || overlapSize >= targetChunkSize) {
      throw new IllegalArgumentException("Invalid overlap size");
    }
    if (minQualityThreshold < 0.0 || minQualityThreshold > 1.0) {
      throw new IllegalArgumentException("Quality threshold must be between 0.0 and 1.0");
    }
    if (preferredQualityThreshold < minQualityThreshold || preferredQualityThreshold > 1.0) {
      throw new IllegalArgumentException("Preferred quality threshold invalid");
    }
  }

  /**
   * Get configuration for specific document type.
   *
   * @param documentType Document type
   * @return Document-type specific config or this config if none found
   */
  public ChunkingConfig getConfigForDocumentType(DocumentType documentType) {
    if (useDocumentTypeConfigs && documentTypeConfigs.containsKey(documentType)) {
      return documentTypeConfigs.get(documentType);
    }
    return this;
  }

  /**
   * Add document-type specific configuration.
   *
   * @param documentType Document type
   * @param config Configuration for this document type
   */
  public void addDocumentTypeConfig(DocumentType documentType, ChunkingConfig config) {
    documentTypeConfigs.put(documentType, config);
  }

  // === Inner Configuration Classes ===

  @Data
  @Builder
  public static class SentenceAwareConfig {
    @Builder.Default
    private double sizeTolerance = 0.3;
    
    @Builder.Default
    private boolean strictPunctuation = false;
    
    public static SentenceAwareConfig defaults() {
      return SentenceAwareConfig.builder().build();
    }
    
    public static SentenceAwareConfig strict() {
      return SentenceAwareConfig.builder()
          .sizeTolerance(0.1)
          .strictPunctuation(true)
          .build();
    }
    
    public static SentenceAwareConfig flexible() {
      return SentenceAwareConfig.builder()
          .sizeTolerance(0.5)
          .strictPunctuation(false)
          .build();
    }
  }

  @Data
  @Builder
  public static class SemanticConfig {
    private EmbeddingService embeddingService;
    
    @Builder.Default
    private double similarityThreshold = 0.75;
    
    @Builder.Default
    private int slidingWindowSize = 3;
    
    @Builder.Default
    private boolean enableBatchProcessing = true;

    public static SemanticConfig defaults() {
      return SemanticConfig.builder().build();
    }
    
    public static SemanticConfig precise(EmbeddingService embeddingService) {
      return SemanticConfig.builder()
          .embeddingService(embeddingService)
          .similarityThreshold(0.85)
          .slidingWindowSize(2)
          .build();
    }
    
    public static SemanticConfig flexible(EmbeddingService embeddingService) {
      return SemanticConfig.builder()
          .embeddingService(embeddingService)
          .similarityThreshold(0.65)
          .slidingWindowSize(4)
          .build();
    }
  }

  @Data
  @Builder
  public static class MarkdownConfig {
    @Builder.Default
    private boolean preserveCodeBlocks = true;
    
    @Builder.Default
    private boolean respectHeaders = true;
    
    @Builder.Default
    private int maxHeaderLevel = 3;
    
    @Builder.Default
    private boolean preserveTables = true;
    
    @Builder.Default
    private boolean preserveLists = true;

    public static MarkdownConfig defaults() {
      return MarkdownConfig.builder().build();
    }
    
    public static MarkdownConfig headerFocused() {
      return MarkdownConfig.builder()
          .respectHeaders(true)
          .maxHeaderLevel(2)
          .preserveCodeBlocks(true)
          .build();
    }
    
    public static MarkdownConfig codePreserving() {
      return MarkdownConfig.builder()
          .preserveCodeBlocks(true)
          .preserveTables(true)
          .respectHeaders(false)
          .maxHeaderLevel(4)
          .build();
    }
  }

  @Data
  @Builder
  public static class FixedSizeConfig {
    @Builder.Default
    private boolean wordBoundaryAware = false;
    
    @Builder.Default
    private boolean preserveParagraphs = false;

    public static FixedSizeConfig defaults() {
      return FixedSizeConfig.builder().build();
    }
    
    public static FixedSizeConfig wordAware() {
      return FixedSizeConfig.builder()
          .wordBoundaryAware(true)
          .preserveParagraphs(true)
          .build();
    }
  }

  // === Chunking Strategy Type Enum ===

  public enum ChunkingStrategyType {
    FIXED_SIZE("Fixed-size chunking with predictable chunk sizes"),
    SENTENCE_AWARE("Sentence-aware chunking preserving sentence boundaries"),
    SEMANTIC("Semantic chunking using embeddings for topical coherence"),
    MARKDOWN_AWARE("Markdown-aware chunking respecting document structure");

    private final String description;

    ChunkingStrategyType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}