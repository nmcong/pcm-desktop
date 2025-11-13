package com.noteflix.pcm.rag.chunking.langchain;

import com.noteflix.pcm.rag.model.DocumentType;

import java.util.List;
import java.util.Map;

/**
 * Base interface for LangChain text splitters.
 * 
 * <p>This interface provides a unified way to interact with various LangChain text splitters
 * within the PCM chunking system. Implementations should wrap actual LangChain splitters
 * and provide PCM-specific functionality.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
public interface LangChainTextSplitter {
    
    /**
     * Split documents into chunks using the LangChain splitter.
     * 
     * @param documents List of LangChain documents to split
     * @return List of document chunks
     */
    List<LangChainDocument> splitDocuments(List<LangChainDocument> documents);
    
    /**
     * Split text into chunks.
     * 
     * @param text Text to split
     * @return List of text chunks
     */
    List<String> splitText(String text);
    
    /**
     * Check if this splitter is suitable for the given content and document type.
     * 
     * @param content Content to analyze
     * @param documentType Type of document
     * @return true if suitable, false otherwise
     */
    boolean isSuitableFor(String content, DocumentType documentType);
    
    /**
     * Estimate quality score for given content with this splitter.
     * 
     * @param content Content to analyze
     * @return Quality estimate (0.0-1.0)
     */
    double estimateQuality(String content);
    
    /**
     * Configure the splitter with parameters.
     * 
     * @param parameters Configuration parameters
     */
    void configure(Map<String, Object> parameters);
    
    /**
     * Get the chunk size setting for this splitter.
     * 
     * @return Chunk size in characters
     */
    int getChunkSize();
    
    /**
     * Get the chunk overlap setting for this splitter.
     * 
     * @return Chunk overlap in characters
     */
    int getChunkOverlap();
    
    /**
     * Get the name of this splitter type.
     * 
     * @return Splitter name
     */
    String getSplitterName();
}