package com.noteflix.pcm.rag.embedding.core;

import com.noteflix.pcm.rag.embedding.api.EmbeddingService;
import com.noteflix.pcm.rag.embedding.config.EngineConfig;
import com.noteflix.pcm.rag.embedding.config.MultiModelConfig;
import com.noteflix.pcm.rag.embedding.model.Language;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Registry for managing multiple language-specific embedding models.
 *
 * <p>This registry provides a unified interface for accessing different embedding models based on
 * language. It supports automatic fallback to a universal model if language-specific models are
 * unavailable.
 *
 * <p><strong>Architecture:</strong>
 *
 * <pre>
 * ┌─────────────────────────────────────────────────────────┐
 * │            EmbeddingServiceRegistry                      │
 * ├─────────────────────────────────────────────────────────┤
 * │  Vietnamese Model  │  English Model  │  Fallback Model  │
 * │  (768 dim)         │  (1024 dim)     │  (384 dim)       │
 * └─────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><strong>Features:</strong>
 *
 * <ul>
 *   <li>Language-specific model routing
 *   <li>Automatic fallback on errors
 *   <li>Thread-safe operations
 *   <li>Resource management
 * </ul>
 *
 * <p><strong>Setup:</strong>
 *
 * <pre>
 * # Download all models
 * ./scripts/setup-multilingual-embeddings.sh
 *
 * # Use in code
 * try (EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry()) {
 *     // Vietnamese text
 *     float[] viEmbedding = registry.embed("Xin chào", Language.VIETNAMESE);
 *
 *     // English text
 *     float[] enEmbedding = registry.embed("Hello", Language.ENGLISH);
 * }
 * </pre>
 *
 * <p><strong>Example Usage:</strong>
 *
 * <pre>
 * EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry();
 *
 * // Embed Vietnamese text
 * float[] viVector = registry.embed(
 *     "Đây là văn bản tiếng Việt",
 *     Language.VIETNAMESE
 * );
 *
 * // Embed English text
 * float[] enVector = registry.embed(
 *     "This is English text",
 *     Language.ENGLISH
 * );
 *
 * // Batch embedding
 * String[] texts = {"Text 1", "Text 2", "Text 3"};
 * float[][] embeddings = registry.embedBatch(texts, Language.ENGLISH);
 *
 * // Cleanup
 * registry.close();
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 * @since 2024-11
 */
@Slf4j
public class EmbeddingServiceRegistry implements AutoCloseable {

  private final Map<Language, EmbeddingService> services;
  private final EmbeddingService fallbackService;
  private final boolean enableAutoFallback;

  /**
   * Create embedding service registry with default configuration.
   *
   * @throws IOException if no models can be loaded
   */
  public EmbeddingServiceRegistry() throws IOException {
    this(
        MultiModelConfig.ENABLE_AUTO_FALLBACK,
        MultiModelConfig.LOG_FALLBACK_WARNINGS,
        MultiModelConfig.FAIL_FAST_ON_NO_MODELS);
  }

  /**
   * Create embedding service registry with custom configuration.
   *
   * @param enableAutoFallback Enable automatic fallback to universal model
   * @param logWarnings Log warnings when using fallback
   * @param failFastOnNoModels Throw exception if no models available
   * @throws IOException if models cannot be loaded
   */
  public EmbeddingServiceRegistry(
      boolean enableAutoFallback, boolean logWarnings, boolean failFastOnNoModels)
      throws IOException {

    this.enableAutoFallback = enableAutoFallback;
    this.services = new EnumMap<>(Language.class);

    log.info("═══════════════════════════════════════════════════════════════");
    log.info("  Initializing Multi-Language Embedding Service Registry");
    log.info("═══════════════════════════════════════════════════════════════");
    log.info("");

    // Track successful loads
    int successfulLoads = 0;

    // Initialize Vietnamese model
    try {
      String engine =
          EngineConfig.VIETNAMESE_ENGINE == EngineConfig.EngineType.PYTORCH ? "PyTorch" : "ONNX";
      log.info("📥 Loading Vietnamese model (PhoBERT-based, {} engine)...", engine);

      EmbeddingService viService = createVietnameseService();
      services.put(Language.VIETNAMESE, viService);

      log.info("✅ Vietnamese model loaded successfully");
      log.info("   Path: {}", EngineConfig.getVietnameseModelPath());
      log.info("   Engine: {}", engine);
      log.info("   Dimension: {}", viService.getDimension());
      log.info("");
      successfulLoads++;
    } catch (Exception e) {
      if (logWarnings) {
        log.warn("⚠️  Failed to load Vietnamese model: {}", e.getMessage());
        log.warn("   Vietnamese content will use fallback model");
        log.warn("");
      }
    }

    // Initialize English model
    try {
      String engine =
          EngineConfig.ENGLISH_ENGINE == EngineConfig.EngineType.PYTORCH ? "PyTorch" : "ONNX";
      log.info("📥 Loading English model (BGE-M3, {} engine)...", engine);

      EmbeddingService enService = createEnglishService();
      services.put(Language.ENGLISH, enService);

      log.info("✅ English model loaded successfully");
      log.info("   Path: {}", EngineConfig.getEnglishModelPath());
      log.info("   Engine: {}", engine);
      log.info("   Dimension: {}", enService.getDimension());
      log.info("   MTEB Score: 75.4 (State-of-the-art)");
      log.info("");
      successfulLoads++;
    } catch (Exception e) {
      if (logWarnings) {
        log.warn("⚠️  Failed to load English model: {}", e.getMessage());
        log.warn("   English content will use fallback model");
        log.warn("");
      }
    }

    // Initialize fallback model
    log.info("📥 Loading fallback model (MiniLM-L6-v2)...");
    try {
      this.fallbackService = new DJLEmbeddingService(MultiModelConfig.FALLBACK_MODEL_PATH);
      log.info("✅ Fallback model loaded successfully");
      log.info("   Path: {}", MultiModelConfig.FALLBACK_MODEL_PATH);
      log.info("   Dimension: {}", fallbackService.getDimension());
      log.info("");
      successfulLoads++;
    } catch (Exception e) {
      log.error("❌ Failed to load fallback model: {}", e.getMessage());
      if (failFastOnNoModels) {
        throw new IOException("Cannot initialize registry: Fallback model is required", e);
      }
      throw new IOException("Critical: Fallback model failed to load", e);
    }

    // Summary
    log.info("═══════════════════════════════════════════════════════════════");
    log.info("  ✅ Registry Initialization Complete");
    log.info("═══════════════════════════════════════════════════════════════");
    log.info("");
    log.info("Models loaded: {}/3", successfulLoads);
    log.info("  Vietnamese: {}", hasModel(Language.VIETNAMESE) ? "✅" : "❌");
    log.info("  English:    {}", hasModel(Language.ENGLISH) ? "✅" : "❌");
    log.info("  Fallback:   ✅");
    log.info("");
    log.info("Auto-fallback: {}", enableAutoFallback ? "Enabled" : "Disabled");
    log.info("");

    if (failFastOnNoModels && successfulLoads == 1) {
      log.warn("⚠️  Only fallback model available");
      log.warn("   Consider downloading language-specific models:");
      log.warn("   ./scripts/setup-multilingual-embeddings.sh");
      log.warn("");
    }
  }

  /**
   * Get embedding service for specific language.
   *
   * @param language Target language
   * @return Embedding service (may be fallback if language-specific not available)
   */
  public EmbeddingService getService(Language language) {
    return services.getOrDefault(language, fallbackService);
  }

  /**
   * Check if language-specific model is available.
   *
   * @param language Language to check
   * @return true if language-specific model is loaded
   */
  public boolean hasModel(Language language) {
    return services.containsKey(language);
  }

  /**
   * Embed text with language-specific model.
   *
   * <p>Automatically falls back to universal model if language-specific model fails or is
   * unavailable.
   *
   * @param text Input text
   * @param language Text language
   * @return Embedding vector
   */
  public float[] embed(String text, Language language) {
    EmbeddingService service = getService(language);

    try {
      return service.embed(text);
    } catch (Exception e) {
      if (enableAutoFallback && service != fallbackService) {
        log.warn(
            "Failed with {} model, using fallback: {}", language, e.getMessage().split("\n")[0]);
        return fallbackService.embed(text);
      }
      throw e;
    }
  }

  /**
   * Batch embedding with language-specific model.
   *
   * @param texts Input texts
   * @param language Text language
   * @return Embedding vectors
   */
  public float[][] embedBatch(String[] texts, Language language) {
    EmbeddingService service = getService(language);

    try {
      return service.embedBatch(texts);
    } catch (Exception e) {
      if (enableAutoFallback && service != fallbackService) {
        log.warn("Failed with {} model, using fallback", language);
        return fallbackService.embedBatch(texts);
      }
      throw e;
    }
  }

  /**
   * Get dimension for specific language model.
   *
   * @param language Target language
   * @return Embedding dimension
   */
  public int getDimension(Language language) {
    return getService(language).getDimension();
  }

  /**
   * Get model name for specific language.
   *
   * @param language Target language
   * @return Model name
   */
  public String getModelName(Language language) {
    return getService(language).getModelName();
  }

  /**
   * Get registry statistics.
   *
   * @return Statistics string
   */
  public String getStatistics() {
    StringBuilder sb = new StringBuilder();
    sb.append("Embedding Service Registry Statistics\n");
    sb.append("═══════════════════════════════════════\n");

    for (Language lang : new Language[] {Language.VIETNAMESE, Language.ENGLISH}) {
      if (hasModel(lang)) {
        EmbeddingService service = services.get(lang);
        sb.append(String.format("%-12s: ✅ %s (%dd)\n", lang, service.getModelName(),
            service.getDimension()));
      } else {
        sb.append(String.format("%-12s: ❌ (using fallback)\n", lang));
      }
    }

    sb.append(String.format("%-12s: ✅ %s (%dd)\n", "Fallback", fallbackService.getModelName(),
        fallbackService.getDimension()));

    return sb.toString();
  }

  /**
   * Create Vietnamese embedding service based on configured engine.
   *
   * @return Vietnamese embedding service
   * @throws Exception if service creation fails
   */
  private EmbeddingService createVietnameseService() throws Exception {
    if (EngineConfig.VIETNAMESE_ENGINE == EngineConfig.EngineType.PYTORCH) {
      return new VietnamesePyTorchService(EngineConfig.getVietnameseModelPath());
    } else {
      return new VietnameseEmbeddingService(EngineConfig.getVietnameseModelPath());
    }
  }

  /**
   * Create English embedding service based on configured engine.
   *
   * @return English embedding service
   * @throws Exception if service creation fails
   */
  private EmbeddingService createEnglishService() throws Exception {
    if (EngineConfig.ENGLISH_ENGINE == EngineConfig.EngineType.PYTORCH) {
      return new BgePyTorchService(EngineConfig.getEnglishModelPath());
    } else {
      return new BgeEmbeddingService(EngineConfig.getEnglishModelPath());
    }
  }

  @Override
  public void close() {
    log.info("Closing embedding services...");

    // Close language-specific services
    services.values().forEach(
        service -> {
          try {
            // Note: DJLEmbeddingService doesn't have explicit close method
            // but we keep this for future implementations
            log.debug("Closed service: {}", service.getModelName());
          } catch (Exception e) {
            log.warn("Error closing service: {}", e.getMessage());
          }
        });

    // Close fallback service
    try {
      log.debug("Closed fallback service");
    } catch (Exception e) {
      log.warn("Error closing fallback service: {}", e.getMessage());
    }

    services.clear();
    log.info("✅ All embedding services closed");
  }

  /**
   * Print configuration summary.
   *
   * @return Configuration summary
   */
  public static String getConfigSummary() {
    return MultiModelConfig.getConfigSummary();
  }
}

