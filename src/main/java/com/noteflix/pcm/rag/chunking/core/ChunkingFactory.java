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
    StrategyRecommendation[] recommendations = new StrategyRecommendation[4];
    
    // Evaluate each strategy type
    recommendations[0] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE, 
                                        document, config, embeddingService);
    recommendations[1] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE, 
                                        document, config, embeddingService);
    recommendations[2] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.MARKDOWN_AWARE, 
                                        document, config, embeddingService);
    recommendations[3] = evaluateStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC, 
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
    HIGH_VOLUME_PROCESSING("High-volume batch processing");

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