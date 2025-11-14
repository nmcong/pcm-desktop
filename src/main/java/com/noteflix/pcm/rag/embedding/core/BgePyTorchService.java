package com.noteflix.pcm.rag.embedding.core;

import ai.djl.ModelException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BGE-M3 embedding service using PyTorch engine.
 *
 * <p>Wraps PyTorchEmbeddingService with BGE-M3-specific configuration.
 *
 * <p>Model: BAAI/bge-m3
 * - Format: PyTorch (model.safetensors)
 * - Tokenizer: All formats supported
 * - Dimension: 1024
 * - Quality: State-of-the-art (MTEB #1)
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class BgePyTorchService extends PyTorchEmbeddingService {

  private static final Logger log = LoggerFactory.getLogger(BgePyTorchService.class);

  /**
   * Create BGE-M3 PyTorch embedding service.
   *
   * @param modelPath Path to BGE-M3 PyTorch model directory
   * @throws IOException if model cannot be loaded
   * @throws ModelException if model format is invalid
   */
  public BgePyTorchService(String modelPath) throws IOException, ModelException {
    super(modelPath);
    log.info("✅ BGE-M3 PyTorch embedding service initialized ({}d, MTEB #1)", getDimension());
  }
}

