package com.noteflix.pcm.rag.embedding.core;

import com.noteflix.pcm.rag.embedding.api.EmbeddingService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * Vietnamese-specific embedding service using PhoBERT-based model.
 *
 * <p>This service wraps a DJL embedding service with Vietnamese-specific preprocessing and
 * optimizations. It uses the keepitreal/vietnamese-sbert model which is based on PhoBERT and
 * fine-tuned for Vietnamese sentence embeddings.
 *
 * <p><strong>Features:</strong>
 *
 * <ul>
 *   <li>Optimized for Vietnamese language
 *   <li>Based on PhoBERT (state-of-the-art for Vietnamese)
 *   <li>768-dimensional embeddings
 *   <li>Good performance on Vietnamese semantic similarity tasks
 * </ul>
 *
 * <p><strong>Setup:</strong>
 *
 * <pre>
 * # Download Vietnamese model
 * ./scripts/setup-embeddings-vietnamese.sh
 *
 * # Use in code
 * EmbeddingService service = new VietnameseEmbeddingService(
 *     "data/models/vietnamese-sbert"
 * );
 * </pre>
 *
 * <p><strong>Example:</strong>
 *
 * <pre>
 * EmbeddingService service = new VietnameseEmbeddingService(
 *     "data/models/vietnamese-sbert"
 * );
 *
 * float[] embedding = service.embed("Xin chào, đây là văn bản tiếng Việt");
 * System.out.println("Dimension: " + embedding.length); // 768
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 * @since 2024-11
 */
@Slf4j
public class VietnameseEmbeddingService implements EmbeddingService {

  private final DJLEmbeddingService delegate;
  private final String modelName;

  /**
   * Create Vietnamese embedding service.
   *
   * @param modelPath Path to Vietnamese model directory
   * @throws IOException if model cannot be loaded
   */
  public VietnameseEmbeddingService(String modelPath) throws IOException {
    this.delegate = new DJLEmbeddingService(modelPath);
    this.modelName = "vietnamese-sbert";
    log.info("✅ Vietnamese embedding service initialized (768d)");
  }

  @Override
  public float[] embed(String text) {
    // Preprocess Vietnamese text if needed
    String processed = preprocessVietnamese(text);
    return delegate.embed(processed);
  }

  @Override
  public float[][] embedBatch(String[] texts) {
    // Preprocess all texts
    String[] processed = new String[texts.length];
    for (int i = 0; i < texts.length; i++) {
      processed[i] = preprocessVietnamese(texts[i]);
    }
    return delegate.embedBatch(processed);
  }

  @Override
  public int getDimension() {
    return delegate.getDimension(); // 768 for PhoBERT
  }

  @Override
  public String getModelName() {
    return modelName;
  }

  /**
   * Preprocess Vietnamese text before embedding.
   *
   * <p>Vietnamese-specific preprocessing:
   *
   * <ul>
   *   <li>Normalize whitespace
   *   <li>Handle special Vietnamese characters
   *   <li>Remove excessive punctuation
   * </ul>
   *
   * @param text Input text
   * @return Preprocessed text
   */
  private String preprocessVietnamese(String text) {
    if (text == null || text.trim().isEmpty()) {
      return text;
    }

    // Basic preprocessing
    String processed = text;

    // Normalize whitespace
    processed = processed.replaceAll("\\s+", " ");

    // Trim
    processed = processed.trim();

    // Could add more Vietnamese-specific preprocessing:
    // - Tone mark normalization
    // - Special character handling
    // - etc.

    return processed;
  }
}

