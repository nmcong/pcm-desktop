package com.noteflix.pcm.rag.api;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for vector store.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class VectorStoreConfig {
    
    /** Vector store type */
    private VectorStoreType type;
    
    /** Storage path (for embedded stores like Lucene) */
    private String storagePath;
    
    /** Host (for remote stores like Qdrant) */
    private String host;
    
    /** Port (for remote stores) */
    private Integer port;
    
    /** API key (if required) */
    private String apiKey;
    
    /** Collection/index name */
    @Builder.Default
    private String collectionName = "rag_documents";
    
    /** Vector dimension (for semantic search) */
    @Builder.Default
    private int vectorDimension = 384;
    
    /** Additional properties */
    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();
    
    /**
     * Create default Lucene config (offline).
     */
    public static VectorStoreConfig lucene(String storagePath) {
        return VectorStoreConfig.builder()
            .type(VectorStoreType.LUCENE)
            .storagePath(storagePath)
            .build();
    }
    
    /**
     * Create Qdrant config (local).
     */
    public static VectorStoreConfig qdrantLocal() {
        return VectorStoreConfig.builder()
            .type(VectorStoreType.QDRANT)
            .host("localhost")
            .port(6333)
            .build();
    }
    
    /**
     * Create Qdrant config (remote).
     */
    public static VectorStoreConfig qdrant(String host, int port, String apiKey) {
        return VectorStoreConfig.builder()
            .type(VectorStoreType.QDRANT)
            .host(host)
            .port(port)
            .apiKey(apiKey)
            .build();
    }
    
    /**
     * Create in-memory config (for testing).
     */
    public static VectorStoreConfig inMemory() {
        return VectorStoreConfig.builder()
            .type(VectorStoreType.IN_MEMORY)
            .build();
    }
}

