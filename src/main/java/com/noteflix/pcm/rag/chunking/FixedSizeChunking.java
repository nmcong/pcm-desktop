package com.noteflix.pcm.rag.chunking;

import com.noteflix.pcm.rag.model.RAGDocument;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fixed-size chunking strategy.
 * 
 * Splits documents into chunks of fixed size with overlap.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class FixedSizeChunking implements ChunkingStrategy {
    
    private final int chunkSize;
    private final int overlapSize;
    
    public FixedSizeChunking(int chunkSize, int overlapSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        }
        if (overlapSize < 0) {
            throw new IllegalArgumentException("Overlap size cannot be negative");
        }
        if (overlapSize >= chunkSize) {
            throw new IllegalArgumentException("Overlap must be smaller than chunk size");
        }
        
        this.chunkSize = chunkSize;
        this.overlapSize = overlapSize;
    }
    
    /**
     * Create default chunking (1000 chars, 200 overlap).
     */
    public static FixedSizeChunking defaults() {
        return new FixedSizeChunking(1000, 200);
    }
    
    @Override
    public List<DocumentChunk> chunk(RAGDocument document) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String content = document.getContent();
        
        if (content == null || content.isEmpty()) {
            return chunks;
        }
        
        int position = 0;
        int index = 0;
        
        while (position < content.length()) {
            int end = Math.min(position + chunkSize, content.length());
            String chunkContent = content.substring(position, end);
            
            DocumentChunk chunk = DocumentChunk.builder()
                .documentId(document.getId())
                .content(chunkContent)
                .index(index)
                .startPosition(position)
                .endPosition(end)
                .chunkId(document.getId() + "_chunk_" + index)
                .build();
            
            chunks.add(chunk);
            
            // Move position with overlap
            position += (chunkSize - overlapSize);
            index++;
        }
        
        log.debug("Chunked document {} into {} chunks", document.getId(), chunks.size());
        return chunks;
    }
    
    @Override
    public int getChunkSize() {
        return chunkSize;
    }
    
    @Override
    public int getOverlapSize() {
        return overlapSize;
    }
}

