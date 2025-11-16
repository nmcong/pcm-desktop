package com.noteflix.pcm.rag.chunking.api;

import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.model.RAGDocument;

import java.util.List;
import java.util.Map;

/**
 * Enhanced interface for document chunking strategies.
 *
 * <p>Chunking strategies break large documents into smaller, semantically meaningful pieces for
 * better retrieval, processing, and LLM context management in RAG applications.
 *
 * <p>Different strategies optimize for different use cases:
 * - Fixed-size chunking: Simple, predictable sizes
 * - Sentence-aware chunking: Preserves sentence boundaries
 * - Semantic chunking: Groups semantically related content
 * - Structure-aware chunking: Respects document hierarchy
 *
 * @author PCM Team
 * @version 2.0.0 - Enhanced interface with metadata and quality metrics
 */
public interface ChunkingStrategy {

    // === Core Chunking Method ===

    /**
     * Chunk a document into smaller pieces with enhanced metadata.
     *
     * @param document Document to chunk
     * @return List of enhanced chunks with metadata
     */
    List<DocumentChunk> chunk(RAGDocument document);

    // === Configuration Methods ===

    /**
     * Get target chunk size in characters.
     *
     * @return Target chunk size in characters
     */
    int getChunkSize();

    /**
     * Get chunk overlap size in characters.
     *
     * @return Overlap size in characters
     */
    int getOverlapSize();

    /**
     * Get strategy name for identification.
     *
     * @return Strategy name (e.g., "FixedSize", "SentenceAware")
     */
    String getStrategyName();

    /**
     * Get strategy description.
     *
     * @return Human-readable strategy description
     */
    String getDescription();

    // === Advanced Configuration ===

    /**
     * Check if strategy supports custom parameters.
     *
     * @return true if strategy accepts custom parameters
     */
    default boolean isConfigurable() {
        return false;
    }

    /**
     * Configure strategy with custom parameters.
     *
     * @param parameters Custom configuration parameters
     * @throws UnsupportedOperationException if strategy is not configurable
     */
    default void configure(Map<String, Object> parameters) {
        if (!parameters.isEmpty()) {
            throw new UnsupportedOperationException(
                    "Strategy " + getStrategyName() + " does not support custom configuration");
        }
    }

    // === Quality Assessment ===

    /**
     * Estimate chunking quality for given document.
     *
     * @param document Document to evaluate
     * @return Quality score (0.0 = poor, 1.0 = excellent)
     */
    default double estimateQuality(RAGDocument document) {
        return 0.5; // Default neutral quality
    }

    /**
     * Check if strategy is suitable for document type.
     *
     * @param document Document to check
     * @return true if strategy is recommended for this document
     */
    default boolean isSuitableFor(RAGDocument document) {
        return true; // Default: all strategies work for all documents
    }

    // === Utility Methods ===

    /**
     * Get minimum chunk size for this strategy.
     *
     * @return Minimum effective chunk size
     */
    default int getMinChunkSize() {
        return Math.max(50, getChunkSize() / 10);
    }

    /**
     * Get maximum chunk size for this strategy.
     *
     * @return Maximum chunk size before splitting
     */
    default int getMaxChunkSize() {
        return getChunkSize() * 2;
    }

    /**
     * Validate chunk size configuration.
     *
     * @param chunkSize   Proposed chunk size
     * @param overlapSize Proposed overlap size
     * @throws IllegalArgumentException if configuration is invalid
     */
    default void validateConfig(int chunkSize, int overlapSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        }
        if (overlapSize < 0) {
            throw new IllegalArgumentException("Overlap size cannot be negative");
        }
        if (overlapSize >= chunkSize) {
            throw new IllegalArgumentException("Overlap must be smaller than chunk size");
        }
    }
}
