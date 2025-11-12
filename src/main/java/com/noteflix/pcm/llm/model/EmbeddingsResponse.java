package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Response model for embeddings generation
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class EmbeddingsResponse {

    /**
     * Response ID
     */
    private String id;

    /**
     * Model used
     */
    private String model;

    /**
     * Object type (usually "list")
     */
    private String object;

    /**
     * List of embedding objects
     */
    private List<Embedding> embeddings;

    /**
     * Usage statistics
     */
    private Usage usage;

    /**
     * Response timestamp
     */
    private Instant createdAt;

    /**
     * Get the first embedding (for single text input)
     */
    public List<Double> getFirstEmbedding() {
        if (embeddings != null && !embeddings.isEmpty()) {
            return embeddings.get(0).getEmbedding();
        }
        return null;
    }

    /**
     * Get all embedding vectors
     */
    public List<List<Double>> getAllEmbeddings() {
        if (embeddings != null) {
            return embeddings.stream()
                    .map(Embedding::getEmbedding)
                    .toList();
        }
        return null;
    }

    /**
     * Single embedding object
     */
    @Data
    @Builder
    public static class Embedding {
        /**
         * Object type (usually "embedding")
         */
        private String object;

        /**
         * Index in the list
         */
        private int index;

        /**
         * Embedding vector
         * Typically 1536 dimensions for OpenAI ada-002
         */
        private List<Double> embedding;

        /**
         * Get embedding dimension
         */
        public int getDimension() {
            return embedding != null ? embedding.size() : 0;
        }
    }

    /**
     * Usage statistics
     */
    @Data
    @Builder
    public static class Usage {
        /**
         * Number of tokens in prompt
         */
        private int promptTokens;

        /**
         * Total tokens used
         */
        private int totalTokens;
    }
}

