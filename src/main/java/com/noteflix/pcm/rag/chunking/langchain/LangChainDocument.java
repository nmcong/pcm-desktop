package com.noteflix.pcm.rag.chunking.langchain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a LangChain document for processing.
 * 
 * <p>This class mirrors the LangChain Document structure and provides
 * compatibility with LangChain text splitters while integrating with
 * the PCM system.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LangChainDocument {
    
    /**
     * The main content of the document.
     */
    private String pageContent;
    
    /**
     * Metadata associated with the document.
     */
    private Map<String, Object> metadata;
    
    /**
     * Create a document with content and empty metadata.
     * 
     * @param pageContent The document content
     */
    public LangChainDocument(String pageContent) {
        this.pageContent = pageContent;
        this.metadata = new HashMap<>();
    }
    
    /**
     * Add metadata entry.
     * 
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * Get metadata value.
     * 
     * @param key Metadata key
     * @return Metadata value or null if not found
     */
    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }
    
    /**
     * Check if metadata contains key.
     * 
     * @param key Metadata key
     * @return true if key exists
     */
    public boolean hasMetadata(String key) {
        return this.metadata != null && this.metadata.containsKey(key);
    }
    
    /**
     * Get the length of the page content.
     * 
     * @return Length in characters
     */
    public int length() {
        return pageContent != null ? pageContent.length() : 0;
    }
    
    /**
     * Check if the document is empty.
     * 
     * @return true if content is null or empty
     */
    public boolean isEmpty() {
        return pageContent == null || pageContent.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("LangChainDocument(content_length=%d, metadata_keys=%s)", 
            length(), 
            metadata != null ? metadata.keySet() : "[]");
    }
}