package com.noteflix.pcm.rag.model;

import lombok.Builder;
import lombok.Data;

/**
 * Retrieved context for RAG query.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class RAGContext {
    
    /** The scored document */
    private ScoredDocument scoredDocument;
    
    /** Relevant text chunk */
    private String chunk;
    
    /** Position in original document */
    private int chunkIndex;
    
    /** Why this was retrieved (for debugging) */
    private String reason;
    
    /**
     * Convenience method to get document.
     */
    public RAGDocument getDocument() {
        return scoredDocument != null ? scoredDocument.getDocument() : null;
    }
    
    /**
     * Convenience method to get score.
     */
    public double getScore() {
        return scoredDocument != null ? scoredDocument.getScore() : 0.0;
    }
}

