package com.noteflix.pcm.rag.model;

import lombok.Builder;
import lombok.Data;

import java.util.*;

/**
 * Options for document retrieval.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class RetrievalOptions {
    
    /** Maximum number of results */
    @Builder.Default
    private int maxResults = 10;
    
    /** Minimum relevance score (0.0 to 1.0) */
    @Builder.Default
    private double minScore = 0.0;
    
    /** Document types to filter */
    private Set<DocumentType> types;
    
    /** Metadata filters (key-value pairs) */
    @Builder.Default
    private Map<String, String> filters = new HashMap<>();
    
    /** Whether to include snippets */
    @Builder.Default
    private boolean includeSnippets = true;
    
    /** Search mode */
    @Builder.Default
    private SearchMode searchMode = SearchMode.HYBRID;
    
    /**
     * Add a filter.
     */
    public void addFilter(String key, String value) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        filters.put(key, value);
    }
    
    /**
     * Add document types.
     */
    public void addTypes(DocumentType... documentTypes) {
        if (types == null) {
            types = new HashSet<>();
        }
        types.addAll(Arrays.asList(documentTypes));
    }
    
    /**
     * Create default options.
     */
    public static RetrievalOptions defaults() {
        return RetrievalOptions.builder().build();
    }
}

