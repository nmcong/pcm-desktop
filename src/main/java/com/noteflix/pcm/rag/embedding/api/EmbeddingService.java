package com.noteflix.pcm.rag.embedding.api;

/**
 * Service for generating text embeddings (vectors).
 *
 * <p>Embeddings enable semantic search - understanding meaning, not just keywords.
 *
 * @author PCM Team
 * @version 1.0.0
 */
public interface EmbeddingService {

    /**
     * Generate embedding for text.
     *
     * @param text Input text
     * @return Embedding vector (float array)
     */
    float[] embed(String text);

    /**
     * Generate embeddings for multiple texts (batch).
     *
     * @param texts Input texts
     * @return Embedding vectors
     */
    float[][] embedBatch(String[] texts);

    /**
     * Get embedding dimension.
     *
     * @return Vector dimension (e.g., 384 for all-MiniLM-L6-v2)
     */
    int getDimension();

    /**
     * Get model name.
     *
     * @return Model name
     */
    String getModelName();
}
