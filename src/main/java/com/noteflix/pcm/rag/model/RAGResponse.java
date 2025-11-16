package com.noteflix.pcm.rag.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response from RAG query.
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class RAGResponse {

    /**
     * Original query
     */
    private String query;

    /**
     * Generated answer (from LLM)
     */
    private String answer;

    /**
     * Retrieved source contexts
     */
    private List<RAGContext> sources;

    /**
     * Query processing time (ms)
     */
    private long processingTimeMs;

    /**
     * Retrieval time (ms)
     */
    private long retrievalTimeMs;

    /**
     * LLM generation time (ms)
     */
    private long generationTimeMs;

    /**
     * Number of documents retrieved
     */
    private int documentsRetrieved;

    /**
     * Confidence score (0.0 to 1.0)
     */
    private double confidence;
}
