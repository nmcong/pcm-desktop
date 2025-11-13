package com.noteflix.pcm.rag.chunking.langchain.splitters;

import com.noteflix.pcm.rag.chunking.langchain.LangChainConfig;
import com.noteflix.pcm.rag.chunking.langchain.LangChainDocument;
import com.noteflix.pcm.rag.chunking.langchain.LangChainTextSplitter;
import com.noteflix.pcm.rag.model.DocumentType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * LangChain Character Text Splitter implementation.
 * 
 * <p>Splits text based on a single character separator (e.g., "\n\n", "\n", " ").
 * This is the simplest form of text splitting and works well for documents
 * with clear structural separators.
 * 
 * <p>Features:
 * - Single separator-based splitting
 * - Configurable separator (string or regex)
 * - Chunk size and overlap control
 * - Metadata preservation
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class CharacterTextSplitter implements LangChainTextSplitter {
    
    private int chunkSize;
    private int chunkOverlap;
    private String separator;
    private boolean keepSeparator;
    private boolean isSeparatorRegex;
    private boolean stripWhitespace;
    
    /**
     * Create Character Text Splitter with configuration.
     * 
     * @param config LangChain configuration
     */
    public CharacterTextSplitter(LangChainConfig config) {
        this.chunkSize = config.getChunkSize();
        this.chunkOverlap = config.getChunkOverlap();
        this.separator = config.getCharacterConfig().getSeparator();
        this.keepSeparator = config.isKeepSeparator();
        this.isSeparatorRegex = config.getCharacterConfig().isSeparatorRegex();
        this.stripWhitespace = config.isStripWhitespace();
    }
    
    /**
     * Create with basic parameters.
     * 
     * @param chunkSize Target chunk size
     * @param chunkOverlap Overlap between chunks
     * @param separator Separator character(s)
     */
    public CharacterTextSplitter(int chunkSize, int chunkOverlap, String separator) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.separator = separator;
        this.keepSeparator = false;
        this.isSeparatorRegex = false;
        this.stripWhitespace = true;
    }
    
    @Override
    public List<LangChainDocument> splitDocuments(List<LangChainDocument> documents) {
        List<LangChainDocument> allChunks = new ArrayList<>();
        
        for (LangChainDocument doc : documents) {
            List<String> textChunks = splitText(doc.getPageContent());
            
            for (int i = 0; i < textChunks.size(); i++) {
                String chunkText = textChunks.get(i);
                
                // Create new document with chunk content
                LangChainDocument chunkDoc = new LangChainDocument(chunkText);
                
                // Copy metadata from original document
                if (doc.getMetadata() != null) {
                    chunkDoc.getMetadata().putAll(doc.getMetadata());
                }
                
                // Add chunk-specific metadata
                chunkDoc.addMetadata("chunk_index", i);
                chunkDoc.addMetadata("chunk_count", textChunks.size());
                chunkDoc.addMetadata("splitter_type", "CharacterTextSplitter");
                chunkDoc.addMetadata("separator", separator);
                
                allChunks.add(chunkDoc);
            }
        }
        
        return allChunks;
    }
    
    @Override
    public List<String> splitText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        
        // Split text by separator
        String[] segments = isSeparatorRegex ? 
            text.split(separator) : 
            text.split(Pattern.quote(separator));
        
        // Merge segments into appropriately sized chunks
        List<String> chunks = new ArrayList<>();
        List<String> currentChunk = new ArrayList<>();
        int currentLength = 0;
        
        for (String segment : segments) {
            if (stripWhitespace) {
                segment = segment.trim();
            }
            
            if (segment.isEmpty()) {
                continue;
            }
            
            // Check if adding this segment would exceed chunk size
            int segmentLength = segment.length();
            int separatorLength = keepSeparator && !currentChunk.isEmpty() ? separator.length() : 0;
            
            if (!currentChunk.isEmpty() && 
                currentLength + separatorLength + segmentLength > chunkSize) {
                
                // Finalize current chunk
                String chunkText = joinSegments(currentChunk);
                if (!chunkText.trim().isEmpty()) {
                    chunks.add(chunkText);
                }
                
                // Start new chunk with overlap
                currentChunk = createOverlapChunk(chunks, currentChunk);
                currentLength = calculateChunkLength(currentChunk);
            }
            
            currentChunk.add(segment);
            currentLength += segmentLength;
            if (keepSeparator && currentChunk.size() > 1) {
                currentLength += separator.length();
            }
        }
        
        // Add final chunk
        if (!currentChunk.isEmpty()) {
            String chunkText = joinSegments(currentChunk);
            if (!chunkText.trim().isEmpty()) {
                chunks.add(chunkText);
            }
        }
        
        return chunks;
    }
    
    @Override
    public boolean isSuitableFor(String content, DocumentType documentType) {
        if (content == null || content.length() < 100) {
            return false;
        }
        
        // Count separator occurrences
        int separatorCount = isSeparatorRegex ?
            content.split(separator).length - 1 :
            (content.length() - content.replace(separator, "").length()) / separator.length();
        
        // Suitable if document has reasonable number of separators
        double separatorDensity = (double) separatorCount / content.length() * 1000; // per 1000 chars
        
        return separatorDensity > 0.5 && separatorDensity < 50; // Reasonable separator density
    }
    
    @Override
    public double estimateQuality(String content) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }
        
        // Estimate quality based on separator distribution
        String[] segments = isSeparatorRegex ? 
            content.split(separator) : 
            content.split(Pattern.quote(separator));
        
        if (segments.length < 2) {
            return 0.3; // Low quality if no separators
        }
        
        // Calculate segment length variance
        double totalLength = 0;
        for (String segment : segments) {
            totalLength += segment.trim().length();
        }
        
        double avgLength = totalLength / segments.length;
        double variance = 0;
        
        for (String segment : segments) {
            double diff = segment.trim().length() - avgLength;
            variance += diff * diff;
        }
        variance /= segments.length;
        
        // Lower variance = more consistent segments = higher quality
        double coefficientOfVariation = avgLength > 0 ? Math.sqrt(variance) / avgLength : 1.0;
        double consistencyScore = Math.max(0.0, 1.0 - coefficientOfVariation);
        
        // Size appropriateness score
        double sizeScore = Math.min(1.0, avgLength / chunkSize);
        
        return (consistencyScore * 0.6 + sizeScore * 0.4);
    }
    
    @Override
    public void configure(Map<String, Object> parameters) {
        if (parameters.containsKey("chunk_size")) {
            this.chunkSize = (Integer) parameters.get("chunk_size");
        }
        if (parameters.containsKey("chunk_overlap")) {
            this.chunkOverlap = (Integer) parameters.get("chunk_overlap");
        }
        if (parameters.containsKey("separator")) {
            this.separator = (String) parameters.get("separator");
        }
        if (parameters.containsKey("keep_separator")) {
            this.keepSeparator = (Boolean) parameters.get("keep_separator");
        }
        if (parameters.containsKey("is_separator_regex")) {
            this.isSeparatorRegex = (Boolean) parameters.get("is_separator_regex");
        }
    }
    
    @Override
    public int getChunkSize() {
        return chunkSize;
    }
    
    @Override
    public int getChunkOverlap() {
        return chunkOverlap;
    }
    
    @Override
    public String getSplitterName() {
        return "CharacterTextSplitter";
    }
    
    // === Private Helper Methods ===
    
    private String joinSegments(List<String> segments) {
        if (segments.isEmpty()) {
            return "";
        }
        
        if (!keepSeparator || segments.size() == 1) {
            return String.join(" ", segments);
        }
        
        return String.join(separator, segments);
    }
    
    private List<String> createOverlapChunk(List<String> existingChunks, List<String> currentChunk) {
        if (chunkOverlap <= 0 || existingChunks.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Calculate how much overlap we need
        int targetOverlapLength = chunkOverlap;
        List<String> overlapSegments = new ArrayList<>();
        int overlapLength = 0;
        
        // Take segments from the end of current chunk for overlap
        for (int i = currentChunk.size() - 1; i >= 0; i--) {
            String segment = currentChunk.get(i);
            if (overlapLength + segment.length() <= targetOverlapLength) {
                overlapSegments.add(0, segment);
                overlapLength += segment.length();
                if (keepSeparator && overlapSegments.size() > 1) {
                    overlapLength += separator.length();
                }
            } else {
                break;
            }
        }
        
        return overlapSegments;
    }
    
    private int calculateChunkLength(List<String> segments) {
        int length = 0;
        for (String segment : segments) {
            length += segment.length();
        }
        if (keepSeparator && segments.size() > 1) {
            length += separator.length() * (segments.size() - 1);
        }
        return length;
    }
    
    // === Factory Methods ===
    
    /** Create splitter with paragraph separator */
    public static CharacterTextSplitter forParagraphs(int chunkSize, int overlap) {
        return new CharacterTextSplitter(chunkSize, overlap, "\n\n");
    }
    
    /** Create splitter with line separator */
    public static CharacterTextSplitter forLines(int chunkSize, int overlap) {
        return new CharacterTextSplitter(chunkSize, overlap, "\n");
    }
    
    /** Create splitter with custom separator */
    public static CharacterTextSplitter withSeparator(int chunkSize, int overlap, String separator) {
        return new CharacterTextSplitter(chunkSize, overlap, separator);
    }
    
    /** Create splitter with regex separator */
    public static CharacterTextSplitter withRegexSeparator(int chunkSize, int overlap, String regexSeparator) {
        LangChainConfig config = LangChainConfig.builder()
            .chunkSize(chunkSize)
            .chunkOverlap(overlap)
            .characterConfig(LangChainConfig.CharacterTextSplitterConfig.builder()
                .separator(regexSeparator)
                .isSeparatorRegex(true)
                .build())
            .build();
        
        return new CharacterTextSplitter(config);
    }
}