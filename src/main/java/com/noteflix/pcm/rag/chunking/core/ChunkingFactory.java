package com.noteflix.pcm.rag.chunking.core;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.strategies.FixedSizeChunking;
import com.noteflix.pcm.rag.chunking.strategies.MarkdownAwareChunking;
import com.noteflix.pcm.rag.chunking.strategies.RecursiveCharacterTextSplitter;
import com.noteflix.pcm.rag.chunking.strategies.SemanticChunking;
import com.noteflix.pcm.rag.chunking.strategies.SentenceAwareChunking;
import com.noteflix.pcm.rag.embedding.api.EmbeddingService;
import com.noteflix.pcm.rag.model.RAGDocument;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating and managing chunking strategies.
 *
 * <p>Provides intelligent strategy selection, configuration-based creation, and automatic
 * fallback mechanisms for robust document chunking in RAG applications.
 *
 * <p>Features:
 * - Automatic strategy selection based on document analysis
 * - Configuration-driven strategy creation
 * - Quality-based fallback mechanisms
 * - Document-type specific optimizations
 * - Comprehensive strategy comparison and recommendation
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class ChunkingFactory {

  /**
   * Create chunking strategy from configuration.
   *
   * @param config Chunking configuration
   * @return Configured chunking strategy
   * @throws IllegalArgumentException if configuration is invalid
   */
  public static ChunkingStrategy createStrategy(ChunkingConfig config) {
    config.validate();
    return createStrategyInternal(config.getPrimaryStrategy(), config);
  }

  /**
   * Create strategy for specific document with automatic selection.
   *
   * @param document Document to analyze for strategy selection
   * @param config Base configuration
   * @param embeddingService Optional embedding service for semantic chunking
   * @return Best strategy for the document
   */
  public static ChunkingStrategy createOptimalStrategy(RAGDocument document, 
                                                     ChunkingConfig config,
                                                     EmbeddingService embeddingService) {
    if (document == null) {
      throw new IllegalArgumentException("Document cannot be null");
    }

    // Use document-type specific config if available
    ChunkingConfig effectiveConfig = config.getConfigForDocumentType(document.getType());

    if (!effectiveConfig.isAutoSelectStrategy()) {
      return createStrategy(effectiveConfig);
    }

    // Analyze document and select best strategy
    StrategyRecommendation recommendation = analyzeDocument(document, effectiveConfig, embeddingService);
    
    log.debug("Selected {} strategy for document {} (quality score: {:.3f})", 
        recommendation.strategyType, document.getId(), recommendation.expectedQuality);

    return recommendation.strategy;
  }

  /**
   * Create strategy with fallback mechanism.
   *
   * @param config Chunking configuration
   * @param embeddingService Optional embedding service
   * @return Strategy with automatic fallback support
   */
  public static ChunkingStrategy createWithFallback(ChunkingConfig config, 
                                                   EmbeddingService embeddingService) {
    return new FallbackChunkingStrategy(config, embeddingService);
  }

  /**
   * Get all available strategies with their quality estimates for a document.
   *
   * @param document Document to analyze
   * @param config Base configuration
   * @param embeddingService Optional embedding service
   * @return List of strategy recommendations ordered by quality
   */
  public static List<StrategyRecommendation> getAllRecommendations(RAGDocument document,
                                                                  ChunkingConfig config,
                                                                  EmbeddingService embeddingService) {
    StrategyRecommendation[] recommendations = new StrategyRecommendation[15]; // Updated to include LangChain4j
    
    // Evaluate PCM strategies
    recommendations[0] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE, 
                                        document, config, embeddingService);
    recommendations[1] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE, 
                                        document, config, embeddingService);
    recommendations[2] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.MARKDOWN_AWARE, 
                                        document, config, embeddingService);
    recommendations[3] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC, 
                                        document, config, embeddingService);
    
    // Evaluate LangChain strategies (custom implementation)
    recommendations[4] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN_CHARACTER, 
                                        document, config, embeddingService);
    recommendations[5] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN_RECURSIVE, 
                                        document, config, embeddingService);
    recommendations[6] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN_TOKEN, 
                                        document, config, embeddingService);
    recommendations[7] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN_CODE, 
                                        document, config, embeddingService);
    
    // Evaluate LangChain4j strategies (real library)
    recommendations[8] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN4J_PARAGRAPH, 
                                        document, config, embeddingService);
    recommendations[9] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN4J_SENTENCE, 
                                        document, config, embeddingService);
    recommendations[10] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN4J_WORD, 
                                         document, config, embeddingService);
    recommendations[11] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN4J_LINE, 
                                         document, config, embeddingService);
    recommendations[12] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN4J_CHARACTER, 
                                         document, config, embeddingService);
    recommendations[13] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN4J_REGEX, 
                                         document, config, embeddingService);
    recommendations[14] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN4J_HIERARCHICAL, 
                                         document, config, embeddingService);

    // Sort by quality (descending)
    Arrays.sort(recommendations, (a, b) -> Double.compare(b.expectedQuality, a.expectedQuality));
    
    return Arrays.asList(recommendations);
  }

  /**
   * Create default strategy for quick usage.
   *
   * @return Default sentence-aware chunking strategy
   */
  public static ChunkingStrategy createDefault() {
    return SentenceAwareChunking.defaults();
  }

  /**
   * Create strategy for specific use case.
   *
   * @param useCase Use case identifier
   * @param embeddingService Optional embedding service
   * @return Strategy optimized for use case
   */
  public static ChunkingStrategy createForUseCase(UseCase useCase, EmbeddingService embeddingService) {
    ChunkingConfig config = switch (useCase) {
      case TECHNICAL_DOCUMENTATION -> ChunkingConfig.forTechnicalDocs();
      case NARRATIVE_CONTENT -> ChunkingConfig.forNarrativeContent();
      case ACADEMIC_PAPERS -> ChunkingConfig.forAcademicPapers(embeddingService);
      case HIGH_VOLUME_PROCESSING -> ChunkingConfig.forHighVolume();
      case GENERAL_PURPOSE -> ChunkingConfig.defaults();
      
      // LangChain use cases (custom implementation)
      case LANGCHAIN_COMPATIBLE -> ChunkingConfig.forLangChainRecursive();
      case TOKEN_OPTIMIZED -> ChunkingConfig.forLangChainToken("gpt-3.5-turbo");
      case CODE_DOCUMENTS -> ChunkingConfig.forLangChainCode("python");
      case HIERARCHICAL_CONTENT -> ChunkingConfig.forLangChainRecursive();
      
      // LangChain4j use cases (real library)
      case LANGCHAIN4J_PARAGRAPH_FOCUSED -> ChunkingConfig.forLangChain4jParagraph();
      case LANGCHAIN4J_SENTENCE_PRECISE -> ChunkingConfig.forLangChain4jSentence();
      case LANGCHAIN4J_WORD_GRANULAR -> ChunkingConfig.forLangChain4jWord();
      case LANGCHAIN4J_LINE_STRUCTURED -> ChunkingConfig.forLangChain4jLine();
      case LANGCHAIN4J_CHARACTER_EXACT -> ChunkingConfig.forLangChain4jCharacter();
      case LANGCHAIN4J_REGEX_PATTERN -> ChunkingConfig.forLangChain4jRegex("\\n\\n");
      case LANGCHAIN4J_HIERARCHICAL_SMART -> ChunkingConfig.forLangChain4jHierarchical();
    };
    
    return createStrategy(config);
  }

  // === Private Helper Methods ===

  private static ChunkingStrategy createStrategyInternal(ChunkingConfig.ChunkingStrategyType type, 
                                                       ChunkingConfig config) {
    return switch (type) {
      case FIXED_SIZE -> new FixedSizeChunking(config.getTargetChunkSize(), config.getOverlapSize());
      
      case SENTENCE_AWARE -> new SentenceAwareChunking(
          config.getTargetChunkSize(), 
          config.getOverlapSize(),
          config.getSentenceAwareConfig().getSizeTolerance());
      
      case MARKDOWN_AWARE -> new MarkdownAwareChunking(
          config.getTargetChunkSize(),
          config.getMinChunkSize(),
          config.getMarkdownConfig().isPreserveCodeBlocks(),
          config.getMarkdownConfig().isRespectHeaders(),
          config.getMarkdownConfig().getMaxHeaderLevel());
      
      case SEMANTIC -> {
        EmbeddingService embeddingService = config.getSemanticConfig().getEmbeddingService();
        if (embeddingService == null) {
          throw new IllegalArgumentException("EmbeddingService required for semantic chunking");
        }
        yield new SemanticChunking(
            embeddingService,
            config.getMaxChunkSize(),
            config.getMinChunkSize(),
            config.getSemanticConfig().getSimilarityThreshold(),
            config.getSemanticConfig().getSlidingWindowSize());
      }
      
      // LangChain strategies
      case LANGCHAIN_CHARACTER -> {
        com.noteflix.pcm.rag.chunking.langchain.splitters.CharacterTextSplitter langChainSplitter = 
            new com.noteflix.pcm.rag.chunking.langchain.splitters.CharacterTextSplitter(config.getLangChainConfig());
        yield new com.noteflix.pcm.rag.chunking.langchain.LangChainAdapter(langChainSplitter, config.getLangChainConfig());
      }
      
      case LANGCHAIN_RECURSIVE -> {
        com.noteflix.pcm.rag.chunking.langchain.splitters.RecursiveCharacterTextSplitter langChainSplitter = 
            new com.noteflix.pcm.rag.chunking.langchain.splitters.RecursiveCharacterTextSplitter(config.getLangChainConfig());
        yield new com.noteflix.pcm.rag.chunking.langchain.LangChainAdapter(langChainSplitter, config.getLangChainConfig());
      }
      
      case LANGCHAIN_TOKEN -> {
        com.noteflix.pcm.rag.chunking.langchain.splitters.TokenTextSplitter langChainSplitter = 
            new com.noteflix.pcm.rag.chunking.langchain.splitters.TokenTextSplitter(config.getLangChainConfig());
        yield new com.noteflix.pcm.rag.chunking.langchain.LangChainAdapter(langChainSplitter, config.getLangChainConfig());
      }
      
      case LANGCHAIN_CODE -> {
        // For now, use recursive splitter optimized for code - can be extended later
        com.noteflix.pcm.rag.chunking.langchain.splitters.RecursiveCharacterTextSplitter langChainSplitter = 
            com.noteflix.pcm.rag.chunking.langchain.splitters.RecursiveCharacterTextSplitter.forCode(
                config.getTargetChunkSize(), config.getOverlapSize());
        yield new com.noteflix.pcm.rag.chunking.langchain.LangChainAdapter(langChainSplitter, config.getLangChainConfig());
      }
      
      // LangChain4j strategies (real library)
      case LANGCHAIN4J_PARAGRAPH -> {
        dev.langchain4j.data.document.DocumentSplitter splitter = 
            com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jSplitterFactory.createSplitter(config.getLangChain4jConfig());
        yield new com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jAdapter(splitter, config.getLangChain4jConfig());
      }
      
      case LANGCHAIN4J_SENTENCE -> {
        com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig sentenceConfig = 
            config.getLangChain4jConfig().toBuilder()
                .splitterType(com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig.SplitterType.BY_SENTENCE)
                .build();
        dev.langchain4j.data.document.DocumentSplitter splitter = 
            com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jSplitterFactory.createSplitter(sentenceConfig);
        yield new com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jAdapter(splitter, sentenceConfig);
      }
      
      case LANGCHAIN4J_WORD -> {
        com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig wordConfig = 
            config.getLangChain4jConfig().toBuilder()
                .splitterType(com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig.SplitterType.BY_WORD)
                .build();
        dev.langchain4j.data.document.DocumentSplitter splitter = 
            com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jSplitterFactory.createSplitter(wordConfig);
        yield new com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jAdapter(splitter, wordConfig);
      }
      
      case LANGCHAIN4J_LINE -> {
        com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig lineConfig = 
            config.getLangChain4jConfig().toBuilder()
                .splitterType(com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig.SplitterType.BY_LINE)
                .build();
        dev.langchain4j.data.document.DocumentSplitter splitter = 
            com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jSplitterFactory.createSplitter(lineConfig);
        yield new com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jAdapter(splitter, lineConfig);
      }
      
      case LANGCHAIN4J_CHARACTER -> {
        com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig charConfig = 
            config.getLangChain4jConfig().toBuilder()
                .splitterType(com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig.SplitterType.BY_CHARACTER)
                .build();
        dev.langchain4j.data.document.DocumentSplitter splitter = 
            com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jSplitterFactory.createSplitter(charConfig);
        yield new com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jAdapter(splitter, charConfig);
      }
      
      case LANGCHAIN4J_REGEX -> {
        com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig regexConfig = 
            config.getLangChain4jConfig().toBuilder()
                .splitterType(com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig.SplitterType.BY_REGEX)
                .build();
        dev.langchain4j.data.document.DocumentSplitter splitter = 
            com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jSplitterFactory.createSplitter(regexConfig);
        yield new com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jAdapter(splitter, regexConfig);
      }
      
      case LANGCHAIN4J_HIERARCHICAL -> {
        com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig hierarchicalConfig = 
            config.getLangChain4jConfig().toBuilder()
                .splitterType(com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig.SplitterType.HIERARCHICAL)
                .build();
        dev.langchain4j.data.document.DocumentSplitter splitter = 
            com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jSplitterFactory.createSplitter(hierarchicalConfig);
        yield new com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jAdapter(splitter, hierarchicalConfig);
      }
    };
  }

  private static StrategyRecommendation analyzeDocument(RAGDocument document, 
                                                      ChunkingConfig config,
                                                      EmbeddingService embeddingService) {
    List<StrategyRecommendation> recommendations = getAllRecommendations(document, config, embeddingService);
    
    // Filter by minimum quality threshold
    StrategyRecommendation best = recommendations.stream()
        .filter(r -> r.expectedQuality >= config.getMinQualityThreshold())
        .findFirst()
        .orElse(recommendations.get(0)); // Fallback to best available
    
    // If best strategy doesn't meet preferred threshold, check fallback
    if (best.expectedQuality < config.getPreferredQualityThreshold() && 
        config.isEnableQualityFallback()) {
      
      log.info("Primary strategy quality ({:.3f}) below preferred threshold ({:.3f}), using fallback",
          best.expectedQuality, config.getPreferredQualityThreshold());
      
      // Create fallback strategy
      ChunkingStrategy fallbackStrategy = createStrategyInternal(config.getFallbackStrategy(), config);
      double fallbackQuality = fallbackStrategy.estimateQuality(document);
      
      if (fallbackQuality > best.expectedQuality) {
        return new StrategyRecommendation(config.getFallbackStrategy(), fallbackStrategy, fallbackQuality);
      }
    }
    
    return best;
  }

  private static StrategyRecommendation evaluateStrategy(ChunkingConfig.ChunkingStrategyType type,
                                                       RAGDocument document, 
                                                       ChunkingConfig config,
                                                       EmbeddingService embeddingService) {
    try {
      // Create temporary config for this strategy
      ChunkingConfig tempConfig = ChunkingConfig.builder()
          .targetChunkSize(config.getTargetChunkSize())
          .minChunkSize(config.getMinChunkSize())
          .maxChunkSize(config.getMaxChunkSize())
          .overlapSize(config.getOverlapSize())
          .sentenceAwareConfig(config.getSentenceAwareConfig())
          .markdownConfig(config.getMarkdownConfig())
          .fixedSizeConfig(config.getFixedSizeConfig())
          .build();

      // For semantic chunking, ensure embedding service is available
      if (type == ChunkingConfig.ChunkingStrategyType.SEMANTIC && embeddingService != null) {
        tempConfig.setSemanticConfig(
            ChunkingConfig.SemanticConfig.builder()
                .embeddingService(embeddingService)
                .similarityThreshold(config.getSemanticConfig().getSimilarityThreshold())
                .slidingWindowSize(config.getSemanticConfig().getSlidingWindowSize())
                .build());
      } else if (type == ChunkingConfig.ChunkingStrategyType.SEMANTIC && embeddingService == null) {
        // Semantic chunking not available without embedding service
        return new StrategyRecommendation(type, null, 0.0);
      }

      ChunkingStrategy strategy = createStrategyInternal(type, tempConfig);
      double quality = strategy.estimateQuality(document);
      
      // Apply suitability multiplier
      if (!strategy.isSuitableFor(document)) {
        quality *= 0.5; // Penalize unsuitable strategies
      }

      return new StrategyRecommendation(type, strategy, quality);
      
    } catch (Exception e) {
      log.warn("Failed to evaluate strategy {}: {}", type, e.getMessage());
      return new StrategyRecommendation(type, null, 0.0);
    }
  }

  // === Public Classes ===

  public static class StrategyRecommendation {
    public final ChunkingConfig.ChunkingStrategyType strategyType;
    public final ChunkingStrategy strategy;
    public final double expectedQuality;

    public StrategyRecommendation(ChunkingConfig.ChunkingStrategyType strategyType, 
                                ChunkingStrategy strategy, 
                                double expectedQuality) {
      this.strategyType = strategyType;
      this.strategy = strategy;
      this.expectedQuality = expectedQuality;
    }

    @Override
    public String toString() {
      return String.format("%s (quality: %.3f)", strategyType, expectedQuality);
    }
  }

  public enum UseCase {
    GENERAL_PURPOSE("General purpose documents"),
    TECHNICAL_DOCUMENTATION("Technical documentation and APIs"),
    NARRATIVE_CONTENT("Stories, articles, and narrative content"),
    ACADEMIC_PAPERS("Research papers and academic content"),
    HIGH_VOLUME_PROCESSING("High-volume batch processing"),
    
    // LangChain specific use cases (custom implementation)
    LANGCHAIN_COMPATIBLE("LangChain compatible text splitting"),
    TOKEN_OPTIMIZED("Token-optimized splitting for LLMs"),
    CODE_DOCUMENTS("Code and programming documents"),
    HIERARCHICAL_CONTENT("Hierarchical structured content"),
    
    // LangChain4j specific use cases (real library)
    LANGCHAIN4J_PARAGRAPH_FOCUSED("LangChain4j paragraph-focused document processing"),
    LANGCHAIN4J_SENTENCE_PRECISE("LangChain4j precise sentence boundary splitting"),
    LANGCHAIN4J_WORD_GRANULAR("LangChain4j fine-grained word-level splitting"),
    LANGCHAIN4J_LINE_STRUCTURED("LangChain4j line-by-line structured splitting"),
    LANGCHAIN4J_CHARACTER_EXACT("LangChain4j character-level exact splitting"),
    LANGCHAIN4J_REGEX_PATTERN("LangChain4j pattern-based regex splitting"),
    LANGCHAIN4J_HIERARCHICAL_SMART("LangChain4j intelligent hierarchical splitting");

    private final String description;

    UseCase(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  // === Fallback Strategy Wrapper ===

  private static class FallbackChunkingStrategy implements ChunkingStrategy {
    private final ChunkingConfig config;
    private final EmbeddingService embeddingService;

    public FallbackChunkingStrategy(ChunkingConfig config, EmbeddingService embeddingService) {
      this.config = config;
      this.embeddingService = embeddingService;
    }

    @Override
    public List<DocumentChunk> chunk(RAGDocument document) {
      try {
        ChunkingStrategy primary = createOptimalStrategy(document, config, embeddingService);
        List<DocumentChunk> chunks = primary.chunk(document);
        
        // Validate quality
        if (!chunks.isEmpty() && config.isGenerateQualityMetrics()) {
          double avgQuality = chunks.stream()
              .mapToDouble(c -> c.getQualityScore() != null ? c.getQualityScore() : 0.5)
              .average()
              .orElse(0.0);
          
          if (avgQuality < config.getMinQualityThreshold()) {
            log.warn("Primary strategy quality ({:.3f}) below threshold, using fallback", avgQuality);
            ChunkingStrategy fallback = createStrategyInternal(config.getFallbackStrategy(), config);
            return fallback.chunk(document);
          }
        }
        
        return chunks;
        
      } catch (Exception e) {
        log.error("Primary strategy failed for document {}: {}", document.getId(), e.getMessage());
        log.info("Using fallback strategy");
        
        ChunkingStrategy fallback = createStrategyInternal(config.getFallbackStrategy(), config);
        return fallback.chunk(document);
      }
    }

    @Override
    public int getChunkSize() {
      return config.getTargetChunkSize();
    }

    @Override
    public int getOverlapSize() {
      return config.getOverlapSize();
    }

    @Override
    public String getStrategyName() {
      return "Adaptive(" + config.getPrimaryStrategy() + "/" + config.getFallbackStrategy() + ")";
    }

    @Override
    public String getDescription() {
      return "Adaptive strategy with automatic fallback";
    }

    @Override
    public double estimateQuality(RAGDocument document) {
      try {
        ChunkingStrategy optimal = createOptimalStrategy(document, config, embeddingService);
        return optimal.estimateQuality(document);
      } catch (Exception e) {
        return 0.5; // Default neutral quality
      }
    }

    @Override
    public boolean isSuitableFor(RAGDocument document) {
      return true; // Adaptive strategy works for any document
    }
  }
}