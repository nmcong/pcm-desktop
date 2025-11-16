package com.noteflix.pcm.rag.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a document in the RAG system.
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class RAGDocument {

    /**
     * Unique document identifier
     */
    private String id;

    /**
     * Document content/text
     */
    private String content;

    /**
     * Document type
     */
    private DocumentType type;

    /**
     * Document metadata (source, file, etc.)
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    /**
     * When document was indexed
     */
    private LocalDateTime indexedAt;

    /**
     * Document title/name
     */
    private String title;

    /**
     * Source file path (if applicable)
     */
    private String sourcePath;

    /**
     * Add metadata entry.
     */
    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * Get metadata value.
     */
    public String getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
}
