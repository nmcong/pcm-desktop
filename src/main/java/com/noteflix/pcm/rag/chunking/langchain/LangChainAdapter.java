package com.noteflix.pcm.rag.chunking.langchain;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.model.RAGDocument;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter to integrate LangChain text splitters with PCM chunking system.
 * 
 * <p>This adapter provides a unified interface to use various LangChain text splitters
 * while maintaining compatibility with the PCM chunking framework. It supports:
 * 
 * <ul>
 * <li>Character-based text splitters</li>
 * <li>Token-based text splitters</li>
 * <li>Recursive character text splitters</li>
 * <li>Language-specific splitters</li>
 * <li>Code-aware splitters</li>
 * <li>Document-specific splitters</li>
 * </ul>
 * 
 * <p>The adapter automatically converts LangChain Document objects to PCM DocumentChunk
 * objects, preserving metadata and adding PCM-specific enhancements.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class LangChainAdapter implements ChunkingStrategy {

    private final LangChainTextSplitter langChainSplitter;
    private final String strategyName;
    private final String description;
    private final LangChainConfig config;

    /**
     * Create LangChain adapter with specific splitter.
     * 
     * @param langChainSplitter The LangChain text splitter to wrap
     * @param config Configuration for the adapter
     */
    public LangChainAdapter(LangChainTextSplitter langChainSplitter, LangChainConfig config) {
        this.langChainSplitter = langChainSplitter;
        this.config = config;
        this.strategyName = "LangChain_" + langChainSplitter.getClass().getSimpleName();
        this.description = String.format("LangChain %s (chunk_size=%d, overlap=%d)", 
            langChainSplitter.getClass().getSimpleName(),
            config.getChunkSize(),
            config.getChunkOverlap());
    }

    @Override
    public List<DocumentChunk> chunk(RAGDocument document) {
        List<DocumentChunk> chunks = new ArrayList<>();
        
        if (document.getContent() == null || document.getContent().isEmpty()) {
            log.warn("Document {} has empty content", document.getId());
            return chunks;
        }

        try {
            // Create LangChain Document
            LangChainDocument langChainDoc = createLangChainDocument(document);
            
            // Split using LangChain
            List<LangChainDocument> langChainChunks = langChainSplitter.splitDocuments(List.of(langChainDoc));
            
            // Convert to PCM DocumentChunks
            for (int i = 0; i < langChainChunks.size(); i++) {
                LangChainDocument langChainChunk = langChainChunks.get(i);
                DocumentChunk pcmChunk = convertToPCMChunk(document, langChainChunk, i);
                chunks.add(pcmChunk);
            }
            
            // Link chunks
            linkChunks(chunks);
            
            log.debug("LangChain {} chunked document {} into {} chunks", 
                langChainSplitter.getClass().getSimpleName(), document.getId(), chunks.size());
                
        } catch (Exception e) {
            log.error("Error in LangChain chunking for document {}: {}", 
                document.getId(), e.getMessage(), e);
            
            // Fallback to simple splitting if LangChain fails
            return createFallbackChunks(document);
        }

        return chunks;
    }

    @Override
    public int getChunkSize() {
        return config.getChunkSize();
    }

    @Override
    public int getOverlapSize() {
        return config.getChunkOverlap();
    }

    @Override
    public String getStrategyName() {
        return strategyName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public double estimateQuality(RAGDocument document) {
        if (document.getContent() == null || document.getContent().isEmpty()) {
            return 0.0;
        }

        String content = document.getContent();
        
        // Base quality factors for LangChain splitters
        double baseQuality = 0.7; // LangChain splitters are generally high quality
        
        // Adjust based on content characteristics
        double lengthScore = Math.min(1.0, (double) content.length() / 5000);
        double structureScore = getStructureScore(content);
        
        // Splitter-specific adjustments
        double splitterScore = getSplitterQualityScore(content);
        
        return Math.min(1.0, baseQuality * 0.4 + lengthScore * 0.2 + structureScore * 0.2 + splitterScore * 0.2);
    }

    @Override
    public boolean isSuitableFor(RAGDocument document) {
        if (document.getContent() == null || document.getContent().length() < 100) {
            return false;
        }

        // Check splitter-specific suitability
        return langChainSplitter.isSuitableFor(document.getContent(), document.getType());
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void configure(Map<String, Object> parameters) {
        // Update configuration based on parameters
        if (parameters.containsKey("chunk_size")) {
            config.setChunkSize((Integer) parameters.get("chunk_size"));
        }
        if (parameters.containsKey("chunk_overlap")) {
            config.setChunkOverlap((Integer) parameters.get("chunk_overlap"));
        }
        
        // Apply configuration to LangChain splitter
        langChainSplitter.configure(parameters);
    }

    // === Private Helper Methods ===

    private LangChainDocument createLangChainDocument(RAGDocument document) {
        Map<String, Object> metadata = Map.of(
            "id", document.getId(),
            "title", document.getTitle() != null ? document.getTitle() : "",
            "type", document.getType() != null ? document.getType().toString() : "",
            "source", document.getSourcePath() != null ? document.getSourcePath() : "",
            "indexed_at", document.getIndexedAt() != null ? document.getIndexedAt().toString() : ""
        );
        
        return new LangChainDocument(document.getContent(), metadata);
    }

    private DocumentChunk convertToPCMChunk(RAGDocument document, LangChainDocument langChainChunk, int index) {
        String chunkId = document.getId() + "_lc_chunk_" + index;
        String content = langChainChunk.getPageContent();
        
        // Calculate position information
        int startPos = calculateStartPosition(document.getContent(), content, index);
        int endPos = startPos + content.length();
        
        // Calculate quality metrics
        double qualityScore = calculateChunkQuality(content);
        double coherenceScore = calculateCoherenceScore(content);
        double densityScore = calculateDensityScore(content);
        
        // Build PCM DocumentChunk
        DocumentChunk.DocumentChunkBuilder builder = DocumentChunk.builder()
            .chunkId(chunkId)
            .documentId(document.getId())
            .content(content)
            .index(index)
            .startPosition(startPos)
            .endPosition(endPos)
            
            // Document metadata
            .documentTitle(document.getTitle())
            .documentType(document.getType())
            .sourcePath(document.getSourcePath())
            .documentTimestamp(document.getIndexedAt())
            
            // Chunking metadata
            .chunkingStrategy(getStrategyName())
            .chunkSizeChars(content.length())
            .overlapSize(calculateActualOverlap(document, index))
            .hasOverlapBefore(index > 0)
            .hasOverlapAfter(false) // Will be set during linking
            
            // Quality metrics
            .qualityScore(qualityScore)
            .coherenceScore(coherenceScore)
            .densityScore(densityScore);

        DocumentChunk chunk = builder.build();
        
        // Add LangChain-specific metadata
        chunk.addMetadata("langchain_splitter", langChainSplitter.getClass().getSimpleName());
        chunk.addMetadata("chunk_type", "langchain");
        
        // Add original LangChain metadata
        Map<String, Object> langChainMetadata = langChainChunk.getMetadata();
        langChainMetadata.forEach((key, value) -> {
            if (value != null) {
                chunk.addMetadata("lc_" + key, value.toString());
            }
        });
        
        return chunk;
    }

    private void linkChunks(List<DocumentChunk> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            
            if (i > 0) {
                chunk.setPreviousChunkId(chunks.get(i - 1).getChunkId());
            }
            
            if (i < chunks.size() - 1) {
                chunk.setNextChunkId(chunks.get(i + 1).getChunkId());
                chunk.setHasOverlapAfter(true);
            }
        }
    }

    private int calculateStartPosition(String fullContent, String chunkContent, int index) {
        // Simple approximation - in production, you might want more sophisticated position tracking
        if (index == 0) {
            return 0;
        }
        
        // Estimate position based on average chunk size and overlap
        int avgChunkSize = config.getChunkSize();
        int overlap = config.getChunkOverlap();
        int effectiveChunkSize = avgChunkSize - overlap;
        
        return Math.max(0, index * effectiveChunkSize);
    }

    private int calculateActualOverlap(RAGDocument document, int index) {
        if (index == 0) {
            return 0;
        }
        return config.getChunkOverlap();
    }

    private double calculateChunkQuality(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }
        
        // Basic quality metrics
        int length = content.length();
        int targetLength = config.getChunkSize();
        
        // Length score (closer to target = higher quality)
        double lengthScore = 1.0 - Math.abs(length - targetLength) / (double) targetLength;
        lengthScore = Math.max(0.0, Math.min(1.0, lengthScore));
        
        // Content density score
        int nonWhitespace = content.replaceAll("\\s", "").length();
        double densityScore = (double) nonWhitespace / length;
        
        // Combine scores
        return (lengthScore * 0.6 + densityScore * 0.4);
    }

    private double calculateCoherenceScore(String content) {
        // Simple coherence estimation based on sentence structure
        String[] sentences = content.split("[.!?]+");
        if (sentences.length < 2) {
            return 1.0; // Single sentence or fragment
        }
        
        // Check for consistent sentence length (indicator of coherence)
        int totalLength = 0;
        for (String sentence : sentences) {
            totalLength += sentence.trim().length();
        }
        
        double avgLength = (double) totalLength / sentences.length;
        double variance = 0.0;
        
        for (String sentence : sentences) {
            double diff = sentence.trim().length() - avgLength;
            variance += diff * diff;
        }
        variance /= sentences.length;
        
        double coefficientOfVariation = avgLength > 0 ? Math.sqrt(variance) / avgLength : 1.0;
        
        // Lower variation = higher coherence
        return Math.max(0.0, 1.0 - coefficientOfVariation);
    }

    private double calculateDensityScore(String content) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }
        
        int totalChars = content.length();
        int nonWhitespaceChars = content.replaceAll("\\s", "").length();
        
        return (double) nonWhitespaceChars / totalChars;
    }

    private double getStructureScore(String content) {
        // Check for structural elements
        double score = 0.5; // Base score
        
        if (content.contains("\n\n")) score += 0.1; // Paragraphs
        if (content.matches(".*[.!?].*")) score += 0.1; // Sentences
        if (content.matches(".*[A-Z][a-z]+.*")) score += 0.1; // Proper capitalization
        if (content.length() > 200) score += 0.1; // Reasonable length
        
        return Math.min(1.0, score);
    }

    private double getSplitterQualityScore(String content) {
        // Override in specific splitter adapters for more accurate scoring
        return langChainSplitter.estimateQuality(content);
    }

    private List<DocumentChunk> createFallbackChunks(RAGDocument document) {
        log.warn("Creating fallback chunks for document {} due to LangChain error", document.getId());
        
        // Simple character-based fallback
        List<DocumentChunk> chunks = new ArrayList<>();
        String content = document.getContent();
        int chunkSize = config.getChunkSize();
        int overlap = config.getChunkOverlap();
        
        int start = 0;
        int index = 0;
        
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            String chunkContent = content.substring(start, end);
            
            DocumentChunk chunk = DocumentChunk.builder()
                .chunkId(document.getId() + "_fallback_chunk_" + index)
                .documentId(document.getId())
                .content(chunkContent)
                .index(index)
                .startPosition(start)
                .endPosition(end)
                .documentTitle(document.getTitle())
                .documentType(document.getType())
                .sourcePath(document.getSourcePath())
                .documentTimestamp(document.getIndexedAt())
                .chunkingStrategy(getStrategyName() + "_Fallback")
                .chunkSizeChars(chunkContent.length())
                .overlapSize(index > 0 ? overlap : 0)
                .hasOverlapBefore(index > 0)
                .hasOverlapAfter(end < content.length())
                .qualityScore(0.3) // Lower quality for fallback
                .densityScore(calculateDensityScore(chunkContent))
                .build();
            
            chunk.addMetadata("fallback_mode", "true");
            chunk.addMetadata("chunk_type", "langchain_fallback");
            
            chunks.add(chunk);
            
            start += chunkSize - overlap;
            index++;
        }
        
        return chunks;
    }
}