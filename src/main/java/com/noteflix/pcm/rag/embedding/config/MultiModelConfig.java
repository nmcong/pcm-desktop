package com.noteflix.pcm.rag.embedding.config;

/**
 * Configuration for multi-model embedding architecture.
 *
 * <p>Defines paths and parameters for multiple embedding models: - Vietnamese model (PhoBERT-based)
 * - English model (BGE-M3) - Fallback model (MiniLM)
 *
 * @author PCM Team
 * @version 1.0.0
 * @since 2024-11
 */
public class MultiModelConfig {

  // ═══════════════════════════════════════════════════════════
  // Model Paths
  // ═══════════════════════════════════════════════════════════

  /** Vietnamese model path (PhoBERT-based) */
  public static final String VIETNAMESE_MODEL_PATH = "data/models/vietnamese-sbert";

  /** English model path (BGE-M3) */
  public static final String ENGLISH_MODEL_PATH = "data/models/bge-m3";

  /** Fallback model path (MiniLM-L6-v2) */
  public static final String FALLBACK_MODEL_PATH = "data/models/all-MiniLM-L6-v2";

  // ═══════════════════════════════════════════════════════════
  // Model Dimensions
  // ═══════════════════════════════════════════════════════════

  /** Vietnamese model dimension */
  public static final int VIETNAMESE_DIM = 768;

  /** English model dimension */
  public static final int ENGLISH_DIM = 1024;

  /** Fallback model dimension */
  public static final int FALLBACK_DIM = 384;

  // ═══════════════════════════════════════════════════════════
  // Model Names
  // ═══════════════════════════════════════════════════════════

  /** Vietnamese model identifier */
  public static final String VIETNAMESE_MODEL_NAME = "vietnamese-sbert";

  /** English model identifier */
  public static final String ENGLISH_MODEL_NAME = "bge-m3";

  /** Fallback model identifier */
  public static final String FALLBACK_MODEL_NAME = "all-MiniLM-L6-v2";

  // ═══════════════════════════════════════════════════════════
  // Behavior Configuration
  // ═══════════════════════════════════════════════════════════

  /**
   * Enable automatic fallback when language-specific model fails.
   *
   * <p>If true: Falls back to universal model on error If false: Throws exception on error
   */
  public static final boolean ENABLE_AUTO_FALLBACK = true;

  /**
   * Log warnings when using fallback model.
   *
   * <p>Useful for debugging model availability issues
   */
  public static final boolean LOG_FALLBACK_WARNINGS = true;

  /**
   * Fail fast if no models are available.
   *
   * <p>If true: Throws exception if all models fail to load If false: Allows initialization with
   * partial models
   */
  public static final boolean FAIL_FAST_ON_NO_MODELS = true;

  // ═══════════════════════════════════════════════════════════
  // Alternative Model Configurations
  // ═══════════════════════════════════════════════════════════

  /**
   * Alternative English models (ordered by preference)
   *
   * <p>Used if primary model is not available
   */
  public static final String[] ALTERNATIVE_ENGLISH_MODELS = {
    "data/models/bge-large-en-v1.5", // Alternative 1
    "data/models/e5-large-v2", // Alternative 2
    "data/models/all-mpnet-base-v2" // Alternative 3
  };

  /**
   * Alternative Vietnamese models (ordered by preference)
   *
   * <p>Used if primary model is not available
   */
  public static final String[] ALTERNATIVE_VIETNAMESE_MODELS = {
    "data/models/sup-SimCSE-VietNamese-phobert-base", // Alternative 1
    "data/models/paraphrase-multilingual-mpnet-base-v2" // Alternative 2 (multilingual)
  };

  // ═══════════════════════════════════════════════════════════
  // Helper Methods
  // ═══════════════════════════════════════════════════════════

  /**
   * Check if a model path exists.
   *
   * @param modelPath Path to model directory
   * @return true if model exists
   */
  public static boolean modelExists(String modelPath) {
    return java.nio.file.Files.exists(java.nio.file.Paths.get(modelPath));
  }

  /**
   * Get model info as formatted string.
   *
   * @return Model configuration summary
   */
  public static String getConfigSummary() {
    return String.format(
        """
                Multi-Model Configuration:
                ═══════════════════════════════════════════
                Vietnamese Model:
                  Path: %s
                  Dim: %d
                  Exists: %s
                
                English Model:
                  Path: %s
                  Dim: %d
                  Exists: %s
                
                Fallback Model:
                  Path: %s
                  Dim: %d
                  Exists: %s
                
                Settings:
                  Auto Fallback: %s
                  Log Warnings: %s
                  Fail Fast: %s
                ═══════════════════════════════════════════
                """,
        VIETNAMESE_MODEL_PATH,
        VIETNAMESE_DIM,
        modelExists(VIETNAMESE_MODEL_PATH),
        ENGLISH_MODEL_PATH,
        ENGLISH_DIM,
        modelExists(ENGLISH_MODEL_PATH),
        FALLBACK_MODEL_PATH,
        FALLBACK_DIM,
        modelExists(FALLBACK_MODEL_PATH),
        ENABLE_AUTO_FALLBACK,
        LOG_FALLBACK_WARNINGS,
        FAIL_FAST_ON_NO_MODELS);
  }

  // Prevent instantiation
  private MultiModelConfig() {}
}

