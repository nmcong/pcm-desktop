package com.noteflix.pcm.rag.embedding.config;

/**
 * Configuration for embedding engine selection.
 *
 * <p>Supports both ONNX Runtime and PyTorch engines.
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class EngineConfig {

  /** Embedding engine type */
  public enum EngineType {
    /** ONNX Runtime (lightweight, fast CPU, requires fast tokenizer) */
    ONNX,

    /** PyTorch Engine (full compatibility, all tokenizers, GPU support) */
    PYTORCH
  }

  // ═══════════════════════════════════════════════════════════════
  // Engine Selection Configuration
  // ═══════════════════════════════════════════════════════════════

  /** Use ONNX engine for Vietnamese model (PyTorch too complex for HuggingFace models) */
  public static final EngineType VIETNAMESE_ENGINE = EngineType.ONNX;

  /** Use ONNX engine for English model (PyTorch too complex for HuggingFace models) */
  public static final EngineType ENGLISH_ENGINE = EngineType.ONNX;

  /** Use ONNX for fallback (smaller, faster) */
  public static final EngineType FALLBACK_ENGINE = EngineType.ONNX;

  // ═══════════════════════════════════════════════════════════════
  // Model Paths by Engine
  // ═══════════════════════════════════════════════════════════════

  /** Vietnamese model path for ONNX engine */
  public static final String VIETNAMESE_ONNX_PATH = "data/models/vietnamese-sbert";

  /** Vietnamese model path for PyTorch engine */
  public static final String VIETNAMESE_PYTORCH_PATH = "data/models/vietnamese-sbert-pytorch";

  /** English model path for ONNX engine */
  public static final String ENGLISH_ONNX_PATH = "data/models/bge-m3";

  /** English model path for PyTorch engine */
  public static final String ENGLISH_PYTORCH_PATH = "data/models/bge-m3-pytorch";

  /** Fallback model path (ONNX only) */
  public static final String FALLBACK_PATH = "data/models/all-MiniLM-L6-v2";

  /**
   * Get Vietnamese model path based on configured engine.
   *
   * @return Model path
   */
  public static String getVietnameseModelPath() {
    return VIETNAMESE_ENGINE == EngineType.PYTORCH ? VIETNAMESE_PYTORCH_PATH
                                                    : VIETNAMESE_ONNX_PATH;
  }

  /**
   * Get English model path based on configured engine.
   *
   * @return Model path
   */
  public static String getEnglishModelPath() {
    return ENGLISH_ENGINE == EngineType.PYTORCH ? ENGLISH_PYTORCH_PATH : ENGLISH_ONNX_PATH;
  }

  /**
   * Get fallback model path.
   *
   * @return Model path
   */
  public static String getFallbackModelPath() {
    return FALLBACK_PATH;
  }

  /**
   * Check if PyTorch engine is used for any language.
   *
   * @return true if PyTorch is enabled
   */
  public static boolean isPyTorchEnabled() {
    return VIETNAMESE_ENGINE == EngineType.PYTORCH || ENGLISH_ENGINE == EngineType.PYTORCH;
  }
}

