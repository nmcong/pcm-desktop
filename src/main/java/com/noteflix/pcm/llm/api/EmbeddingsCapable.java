package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.EmbeddingsRequest;
import com.noteflix.pcm.llm.model.EmbeddingsResponse;

/**
 * Interface for LLM clients that support embeddings generation
 *
 * <p>Embeddings are vector representations of text useful for: - Semantic search - Text similarity
 * - Clustering - Classification
 *
 * @author PCM Team
 * @version 1.0.0
 */
public interface EmbeddingsCapable {

  /**
   * Generate embeddings for input text
   *
   * @param request Embeddings request
   * @return Embeddings response with vectors
   */
  EmbeddingsResponse generateEmbeddings(EmbeddingsRequest request);

  /**
   * Check if this provider supports embeddings
   *
   * @return true if embeddings are supported
   */
  default boolean supportsEmbeddings() {
    return true;
  }

  /**
   * Get the embedding model name
   *
   * @return Model name (e.g., "text-embedding-ada-002")
   */
  String getEmbeddingModel();

  /**
   * Get the dimension of embeddings returned by this model
   *
   * @return Vector dimension (e.g., 1536 for OpenAI ada-002)
   */
  int getEmbeddingDimension();
}
