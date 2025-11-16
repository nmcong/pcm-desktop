package com.noteflix.pcm.rag.model;

/**
 * Search mode for retrieval.
 *
 * @author PCM Team
 * @version 1.0.0
 */
public enum SearchMode {
    /**
     * Keyword-based search (BM25)
     */
    KEYWORD,

    /**
     * Semantic search (embeddings)
     */
    SEMANTIC,

    /**
     * Hybrid: combine keyword + semantic
     */
    HYBRID
}
