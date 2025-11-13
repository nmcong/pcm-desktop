package com.noteflix.pcm.rag.chunking.langchain4j;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.model.RAGDocument;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that bridges LangChain4j DocumentSplitters with PCM ChunkingStrategy interface.
 * 
 * <p>This adapter allows using real LangChain4j text splitters within the PCM
 * chunking system while preserving all metadata and maintaining compatibility
 * with PCM interfaces.
 * 
 * <p>Features:
 * - Uses actual LangChain4j DocumentSplitter implementations
 * - Converts between LangChain4j Document/TextSegment and PCM DocumentChunk
 * - Preserves all metadata from source documents
 * - Adds LangChain4j-specific metadata
 * - Quality assessment and suitability evaluation
 * - Thread-safe operation
 * 
 * @author PCM Team
 * @version 2.0.0
 */
public class LangChain4jAdapter implements ChunkingStrategy {
    
    private final DocumentSplitter langchain4jSplitter;
    private final String splitterName;
    private final LangChain4jConfig config;
    
    /**
     * Create adapter with LangChain4j DocumentSplitter.
     * 
     * @param langchain4jSplitter Real LangChain4j DocumentSplitter
     * @param config Configuration for the adapter
     */
    public LangChain4jAdapter(DocumentSplitter langchain4jSplitter, LangChain4jConfig config) {
        this.langchain4jSplitter = langchain4jSplitter;
        this.splitterName = langchain4jSplitter.getClass().getSimpleName();
        this.config = config;
    }
    
    /**
     * Create adapter with splitter name.
     * 
     * @param langchain4jSplitter Real LangChain4j DocumentSplitter
     * @param splitterName Custom name for the splitter
     * @param config Configuration for the adapter
     */
    public LangChain4jAdapter(DocumentSplitter langchain4jSplitter, 
                             String splitterName, 
                             LangChain4jConfig config) {
        this.langchain4jSplitter = langchain4jSplitter;
        this.splitterName = splitterName;
        this.config = config;
    }
    
    @Override
    public List<DocumentChunk> chunk(RAGDocument document) {
        if (document == null || document.getContent() == null || document.getContent().trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // Convert PCM RAGDocument to LangChain4j Document
            Document langchain4jDoc = convertToLangChain4jDocument(document);
            
            // Split using LangChain4j splitter
            List<TextSegment> segments = langchain4jSplitter.split(langchain4jDoc);
            
            // Convert back to PCM DocumentChunks
            return convertToDocumentChunks(segments, document);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to split document using LangChain4j: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert PCM RAGDocument to LangChain4j Document.
     * 
     * @param ragDocument PCM document
     * @return LangChain4j Document
     */
    private Document convertToLangChain4jDocument(RAGDocument ragDocument) {
        // Create Document with text content
        Document document = Document.from(ragDocument.getContent());
        
        // Add metadata
        if (ragDocument.getId() != null) {
            document.metadata().put("id", ragDocument.getId());
        }
        if (ragDocument.getTitle() != null) {
            document.metadata().put("title", ragDocument.getTitle());
        }
        String author = ragDocument.getMetadata("author");
        if (author != null) {
            document.metadata().put("author", author);
        }
        if (ragDocument.getType() != null) {
            document.metadata().put("document_type", ragDocument.getType().toString());
        }
        if (ragDocument.getSourcePath() != null) {
            document.metadata().put("source_path", ragDocument.getSourcePath());
        }
        
        // Add all custom metadata
        if (ragDocument.getMetadata() != null) {
            ragDocument.getMetadata().forEach((k, v) -> 
                document.metadata().put(k, v != null ? v : ""));
        }
        
        return document;
    }
    
    /**
     * Convert LangChain4j TextSegments back to PCM DocumentChunks.
     * 
     * @param segments LangChain4j text segments
     * @param originalDocument Original PCM document
     * @return List of PCM DocumentChunks
     */
    private List<DocumentChunk> convertToDocumentChunks(List<TextSegment> segments, RAGDocument originalDocument) {
        List<DocumentChunk> chunks = new ArrayList<>();
        LocalDateTime creationTime = LocalDateTime.now();
        
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            
            // Calculate positions in original text
            int startPosition = findStartPosition(originalDocument.getContent(), segment.text(), i);
            int endPosition = startPosition + segment.text().length();
            
            DocumentChunk chunk = DocumentChunk.builder()
                    .chunkId(generateChunkId(originalDocument.getId(), i))
                    .content(segment.text())
                    .documentId(originalDocument.getId())
                    .documentType(originalDocument.getType())
                    .index(i)
                    .startPosition(startPosition)
                    .endPosition(endPosition)
                    .chunkingStrategy("LangChain4jAdapter")
                    .chunkSizeChars(segment.text().length())
                    .qualityScore(calculateQualityScore(segment.text()))
                    .build();
            
            // Copy original metadata
            chunk.getMetadata().putAll(originalDocument.getMetadata());
            
            // Add LangChain4j segment metadata
            if (segment.metadata() != null) {
                segment.metadata().toMap().forEach((key, value) -> 
                    chunk.getMetadata().put(key, String.valueOf(value)));
            }
            
            // Add adapter-specific metadata
            chunk.getMetadata().put("langchain4j_splitter", splitterName);
            chunk.getMetadata().put("langchain4j_strategy", "LangChain4jAdapter");
            chunk.getMetadata().put("segment_index", String.valueOf(i));
            chunk.getMetadata().put("segment_count", String.valueOf(segments.size()));
            chunk.getMetadata().put("character_count", String.valueOf(segment.text().length()));
            
            // Add title and author from original document
            if (originalDocument.getTitle() != null) {
                chunk.getMetadata().put("title", originalDocument.getTitle());
            }
            String author = originalDocument.getMetadata("author");
            if (author != null) {
                chunk.getMetadata().put("author", author);
            }
            
            chunks.add(chunk);
        }
        
        return chunks;
    }
    
    /**
     * Find start position of text segment in original document.
     * Simple implementation - could be improved for accuracy.
     */
    private int findStartPosition(String originalText, String segmentText, int index) {
        if (index == 0) {
            return 0;
        }
        
        // Try to find the segment in the original text
        int position = originalText.indexOf(segmentText.trim());
        if (position >= 0) {
            return position;
        }
        
        // Fallback: estimate based on average chunk size and index
        int averageChunkSize = originalText.length() / Math.max(1, index + 1);
        return Math.min(index * averageChunkSize, originalText.length());
    }
    
    /**
     * Generate unique chunk ID.
     */
    private String generateChunkId(String documentId, int index) {
        return documentId + "_langchain4j_chunk_" + index;
    }
    
    /**
     * Calculate quality score for a text segment.
     */
    private double calculateQualityScore(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        
        double score = 0.5; // Base score
        
        // Length appropriateness
        int length = text.length();
        if (length >= 100 && length <= 2000) {
            score += 0.2;
        }
        
        // Structural indicators
        if (text.contains(".") || text.contains("!") || text.contains("?")) {
            score += 0.1; // Has sentences
        }
        
        if (text.contains("\n")) {
            score += 0.1; // Has line breaks
        }
        
        // Content density (avoid very sparse or very dense text)
        double wordCount = text.split("\\s+").length;
        double averageWordLength = length / wordCount;
        if (averageWordLength >= 3 && averageWordLength <= 8) {
            score += 0.1;
        }
        
        return Math.min(1.0, Math.max(0.0, score));
    }
    
    @Override
    public int getChunkSize() {
        return config.getMaxSegmentSizeInChars();
    }
    
    @Override
    public int getOverlapSize() {
        return config.getMaxOverlapSizeInChars();
    }
    
    @Override
    public String getStrategyName() {
        return "LangChain4jAdapter(" + splitterName + ")";
    }
    
    @Override
    public String getDescription() {
        return "LangChain4j " + splitterName + " integrated with PCM chunking system";
    }
    
    @Override
    public double estimateQuality(RAGDocument document) {
        if (document == null || document.getContent() == null) {
            return 0.0;
        }
        
        String content = document.getContent();
        
        // Length appropriateness for the splitter
        double lengthScore = Math.min(1.0, (double) content.length() / 5000);
        
        // Structural analysis
        boolean hasNewlines = content.contains("\n");
        boolean hasParagraphs = content.contains("\n\n");
        boolean hasSentences = content.matches(".*[.!?].*");
        
        double structureScore = 0.2;
        if (hasNewlines) structureScore += 0.2;
        if (hasParagraphs) structureScore += 0.3;
        if (hasSentences) structureScore += 0.3;
        
        // Document type compatibility
        double typeScore = 0.8; // LangChain4j splitters are generally versatile
        
        return (lengthScore * 0.3 + structureScore * 0.5 + typeScore * 0.2);
    }
    
    @Override
    public boolean isSuitableFor(RAGDocument document) {
        if (document == null || document.getContent() == null) {
            return false;
        }
        
        String content = document.getContent();
        
        // Basic length requirements
        if (content.length() < 50) {
            return false;
        }
        
        // LangChain4j splitters are generally suitable for most text
        return true;
    }
    
    /**
     * Get the underlying LangChain4j DocumentSplitter.
     * 
     * @return LangChain4j DocumentSplitter
     */
    public DocumentSplitter getLangChain4jSplitter() {
        return langchain4jSplitter;
    }
    
    /**
     * Get splitter name.
     * 
     * @return Splitter name
     */
    public String getSplitterName() {
        return splitterName;
    }
    
    /**
     * Get configuration.
     * 
     * @return Configuration
     */
    public LangChain4jConfig getConfig() {
        return config;
    }
}