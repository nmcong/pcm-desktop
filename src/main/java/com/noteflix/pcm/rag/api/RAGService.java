package com.noteflix.pcm.rag.api;

import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.RAGResponse;
import com.noteflix.pcm.rag.model.RetrievalOptions;

import java.util.List;

/**
 * Main RAG service interface.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
public interface RAGService {
    
    /**
     * Query the RAG system.
     * 
     * @param query User query
     * @return Response with answer and sources
     */
    RAGResponse query(String query);
    
    /**
     * Query with custom options.
     * 
     * @param query User query
     * @param options Retrieval options
     * @return Response with answer and sources
     */
    RAGResponse query(String query, RetrievalOptions options);
    
    /**
     * Index a single document.
     * 
     * @param document Document to index
     */
    void indexDocument(RAGDocument document);
    
    /**
     * Index multiple documents.
     * 
     * @param documents Documents to index
     */
    void indexDocuments(List<RAGDocument> documents);
    
    /**
     * Get document count.
     * 
     * @return Number of indexed documents
     */
    long getDocumentCount();
    
    /**
     * Clear all indexed documents.
     */
    void clear();
    
    /**
     * Close and cleanup resources.
     */
    void close();
}

