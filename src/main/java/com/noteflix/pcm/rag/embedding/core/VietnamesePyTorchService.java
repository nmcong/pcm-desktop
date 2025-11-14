package com.noteflix.pcm.rag.embedding.core;

import ai.djl.ModelException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vietnamese embedding service using PyTorch engine.
 *
 * <p>Wraps PyTorchEmbeddingService with Vietnamese-specific configuration.
 *
 * <p>Model: keepitreal/vietnamese-sbert
 * - Format: PyTorch (pytorch_model.bin)
 * - Tokenizer: vocab.txt + bpe.codes (full support!)
 * - Dimension: 768
 * - Quality: High (designed for Vietnamese)
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class VietnamesePyTorchService extends PyTorchEmbeddingService {

  private static final Logger log = LoggerFactory.getLogger(VietnamesePyTorchService.class);

  /**
   * Create Vietnamese PyTorch embedding service.
   *
   * @param modelPath Path to Vietnamese PyTorch model directory
   * @throws IOException if model cannot be loaded
   * @throws ModelException if model format is invalid
   */
  public VietnamesePyTorchService(String modelPath) throws IOException, ModelException {
    super(modelPath);
    log.info(
        "✅ Vietnamese PyTorch embedding service initialized ({}d, PhoBERT-based)",
        getDimension());
  }
}

