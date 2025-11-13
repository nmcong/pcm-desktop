package com.noteflix.pcm.rag.parser;

import com.noteflix.pcm.rag.model.RAGDocument;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for parsing documents from files.
 * 
 * Parsers extract text content from various file formats.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
public interface DocumentParser {
    
    /**
     * Check if parser can handle this file.
     * 
     * @param filePath File path
     * @return true if parser can handle
     */
    boolean canParse(Path filePath);
    
    /**
     * Parse file into RAG document.
     * 
     * @param filePath File path
     * @return Parsed document
     * @throws IOException if parsing fails
     */
    RAGDocument parse(Path filePath) throws IOException;
    
    /**
     * Get supported file extensions.
     * 
     * @return Array of extensions (e.g., [".java", ".txt"])
     */
    String[] getSupportedExtensions();
}

