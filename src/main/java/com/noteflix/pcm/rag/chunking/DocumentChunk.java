package com.noteflix.pcm.rag.chunking;

import lombok.Builder;
import lombok.Data;

/**
 * A chunk of a document.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class DocumentChunk {
    
    /** Original document ID */
    private String documentId;
    
    /** Chunk content */
    private String content;
    
    /** Chunk index (0-based) */
    private int index;
    
    /** Start position in original document */
    private int startPosition;
    
    /** End position in original document */
    private int endPosition;
    
    /** Unique chunk ID */
    private String chunkId;
}

