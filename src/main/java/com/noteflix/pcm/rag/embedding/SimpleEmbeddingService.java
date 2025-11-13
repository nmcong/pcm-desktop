package com.noteflix.pcm.rag.embedding;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple embedding service for testing/demo.
 *
 * <p>Generates fake embeddings using hash-based approach. NOT for production! Use DJL or ONNX-based
 * implementation.
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class SimpleEmbeddingService implements EmbeddingService {

  private final int dimension;
  private final Map<String, float[]> cache = new HashMap<>();

  public SimpleEmbeddingService() {
    this(384); // Default dimension
  }

  public SimpleEmbeddingService(int dimension) {
    this.dimension = dimension;
    log.warn("Using SimpleEmbeddingService - NOT for production!");
    log.warn("For real semantic search, use DJL or ONNX-based implementation");
  }

  @Override
  public float[] embed(String text) {
    // Check cache
    if (cache.containsKey(text)) {
      return cache.get(text);
    }

    // Generate pseudo-embedding based on text hash
    float[] embedding = new float[dimension];

    int hash = text.hashCode();

    for (int i = 0; i < dimension; i++) {
      // Use hash + position to generate pseudo-random values
      int seed = hash + i * 31;
      embedding[i] = (float) Math.sin(seed) * 0.5f;
    }

    // Normalize
    normalize(embedding);

    // Cache it
    cache.put(text, embedding);

    return embedding;
  }

  @Override
  public float[][] embedBatch(String[] texts) {
    float[][] embeddings = new float[texts.length][];

    for (int i = 0; i < texts.length; i++) {
      embeddings[i] = embed(texts[i]);
    }

    return embeddings;
  }

  @Override
  public int getDimension() {
    return dimension;
  }

  @Override
  public String getModelName() {
    return "simple-hash-based-embeddings";
  }

  /** Normalize vector (L2 normalization). */
  private void normalize(float[] vector) {
    float norm = 0;

    for (float v : vector) {
      norm += v * v;
    }

    norm = (float) Math.sqrt(norm);

    if (norm > 0) {
      for (int i = 0; i < vector.length; i++) {
        vector[i] /= norm;
      }
    }
  }
}
