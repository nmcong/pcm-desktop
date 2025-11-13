package com.noteflix.pcm.rag.chunking.langchain.splitters;

import com.noteflix.pcm.rag.chunking.langchain.LangChainConfig;
import com.noteflix.pcm.rag.chunking.langchain.LangChainDocument;
import com.noteflix.pcm.rag.chunking.langchain.LangChainTextSplitter;
import com.noteflix.pcm.rag.model.DocumentType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * LangChain Recursive Character Text Splitter implementation.
 * 
 * <p>This is the most commonly used text splitter in LangChain. It tries to split
 * text by looking for separators in order (e.g., paragraphs, then sentences, then words).
 * This maintains related pieces of text together as much as possible.
 * 
 * <p>Default separator hierarchy:
 * 1. Double newlines (paragraphs): "\n\n"
 * 2. Single newlines (lines): "\n"  
 * 3. Spaces (words): " "
 * 4. Empty string (characters): ""
 * 
 * <p>Features:
 * - Hierarchical separator-based splitting
 * - Maintains semantic coherence
 * - Configurable separator hierarchy
 * - Regex separator support
 * - Chunk size and overlap control
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class RecursiveCharacterTextSplitter implements LangChainTextSplitter {
    
    private int chunkSize;
    private int chunkOverlap;
    private String[] separators;
    private boolean keepSeparator;
    private boolean isSeparatorRegex;
    private boolean stripWhitespace;
    
    /**
     * Create Recursive Character Text Splitter with configuration.
     * 
     * @param config LangChain configuration
     */
    public RecursiveCharacterTextSplitter(LangChainConfig config) {
        this.chunkSize = config.getChunkSize();
        this.chunkOverlap = config.getChunkOverlap();
        this.separators = config.getRecursiveConfig().getSeparators();
        this.keepSeparator = config.getRecursiveConfig().isKeepSeparator();
        this.isSeparatorRegex = config.getRecursiveConfig().isSeparatorRegex();
        this.stripWhitespace = config.isStripWhitespace();
    }
    
    /**
     * Create with basic parameters using default separators.
     * 
     * @param chunkSize Target chunk size
     * @param chunkOverlap Overlap between chunks
     */
    public RecursiveCharacterTextSplitter(int chunkSize, int chunkOverlap) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.separators = new String[]{"\n\n", "\n", " ", ""};
        this.keepSeparator = true;
        this.isSeparatorRegex = false;
        this.stripWhitespace = true;
    }
    
    /**
     * Create with custom separators.
     * 
     * @param chunkSize Target chunk size
     * @param chunkOverlap Overlap between chunks
     * @param separators Custom separator hierarchy
     */
    public RecursiveCharacterTextSplitter(int chunkSize, int chunkOverlap, String[] separators) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.separators = separators;
        this.keepSeparator = true;
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
                chunkDoc.addMetadata("splitter_type", "RecursiveCharacterTextSplitter");
                chunkDoc.addMetadata("separators", Arrays.toString(separators));
                
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
        
        return splitTextRecursive(text, separators);
    }
    
    /**
     * Recursively split text using separator hierarchy.
     * 
     * @param text Text to split
     * @param separators Array of separators to try in order
     * @return List of text chunks
     */
    private List<String> splitTextRecursive(String text, String[] separators) {
        List<String> finalChunks = new ArrayList<>();
        
        // Find the best separator to use
        String separator = "";
        for (String sep : separators) {
            if (sep.isEmpty() || text.contains(sep) || (isSeparatorRegex && text.matches(".*" + sep + ".*"))) {
                separator = sep;
                break;
            }
        }
        
        // Split by the chosen separator
        List<String> splits;
        if (separator.isEmpty()) {
            // Character-level splitting
            splits = List.of(text);
        } else {
            splits = splitBySeparator(text, separator);
        }
        
        // Process each split
        List<String> goodSplits = new ArrayList<>();
        String separatorToUse = keepSeparator ? separator : "";
        
        for (String split : splits) {
            if (stripWhitespace) {
                split = split.trim();
            }
            
            if (split.isEmpty()) {
                continue;
            }
            
            if (split.length() <= chunkSize) {
                goodSplits.add(split);
            } else {
                // Split is too large, need to split further
                if (goodSplits.size() > 0) {
                    // Merge good splits first
                    List<String> mergedChunks = mergeSplits(goodSplits, separatorToUse);
                    finalChunks.addAll(mergedChunks);
                    goodSplits.clear();
                }
                
                // Find the next separator to try
                String[] remainingSeparators = findRemainingSeparators(separators, separator);
                if (remainingSeparators.length > 0) {
                    List<String> subChunks = splitTextRecursive(split, remainingSeparators);
                    finalChunks.addAll(subChunks);
                } else {
                    // No more separators, split by character
                    List<String> charChunks = splitByCharacter(split);
                    finalChunks.addAll(charChunks);
                }
            }
        }
        
        // Merge remaining good splits
        if (goodSplits.size() > 0) {
            List<String> mergedChunks = mergeSplits(goodSplits, separatorToUse);
            finalChunks.addAll(mergedChunks);
        }
        
        return finalChunks;
    }
    
    /**
     * Split text by a specific separator.
     * 
     * @param text Text to split
     * @param separator Separator to use
     * @return List of splits
     */
    private List<String> splitBySeparator(String text, String separator) {
        if (isSeparatorRegex) {
            return Arrays.asList(text.split(separator));
        } else {
            return Arrays.asList(text.split(Pattern.quote(separator)));
        }
    }
    
    /**
     * Merge splits into chunks of appropriate size.
     * 
     * @param splits List of text splits
     * @param separator Separator to use when joining
     * @return List of merged chunks
     */
    private List<String> mergeSplits(List<String> splits, String separator) {
        List<String> chunks = new ArrayList<>();
        List<String> currentChunk = new ArrayList<>();
        int currentLength = 0;
        
        for (String split : splits) {
            int splitLength = split.length();
            int separatorLength = !currentChunk.isEmpty() && !separator.isEmpty() ? separator.length() : 0;
            
            // Check if adding this split would exceed chunk size
            if (!currentChunk.isEmpty() && currentLength + separatorLength + splitLength > chunkSize) {
                // Finalize current chunk
                String chunkText = String.join(separator, currentChunk);
                chunks.add(chunkText);
                
                // Start new chunk with overlap
                currentChunk = createOverlapChunk(chunks, currentChunk, separator);
                currentLength = calculateChunkLength(currentChunk, separator);
            }
            
            currentChunk.add(split);
            currentLength += splitLength;
            if (!currentChunk.isEmpty() && !separator.isEmpty()) {
                currentLength += separator.length() * (currentChunk.size() - 1);
            }
        }
        
        // Add final chunk
        if (!currentChunk.isEmpty()) {
            String chunkText = String.join(separator, currentChunk);
            chunks.add(chunkText);
        }
        
        return chunks;
    }
    
    /**
     * Split text by character when no separators work.
     * 
     * @param text Text to split
     * @return List of character-based chunks
     */
    private List<String> splitByCharacter(String text) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunk = text.substring(start, end);
            chunks.add(chunk);
            start += chunkSize - chunkOverlap;
        }
        
        return chunks;
    }
    
    /**
     * Find remaining separators after the current one.
     * 
     * @param allSeparators All available separators
     * @param usedSeparator Currently used separator
     * @return Array of remaining separators
     */
    private String[] findRemainingSeparators(String[] allSeparators, String usedSeparator) {
        List<String> remaining = new ArrayList<>();
        boolean found = false;
        
        for (String sep : allSeparators) {
            if (found) {
                remaining.add(sep);
            } else if (sep.equals(usedSeparator)) {
                found = true;
            }
        }
        
        return remaining.toArray(new String[0]);
    }
    
    /**
     * Create overlap chunk from previous chunks.
     * 
     * @param existingChunks List of existing chunks
     * @param currentChunk Current chunk being processed
     * @param separator Separator being used
     * @return List of overlap splits
     */
    private List<String> createOverlapChunk(List<String> existingChunks, List<String> currentChunk, String separator) {
        if (chunkOverlap <= 0 || existingChunks.isEmpty()) {
            return new ArrayList<>();
        }
        
        int targetOverlapLength = chunkOverlap;
        List<String> overlapSplits = new ArrayList<>();
        int overlapLength = 0;
        
        // Take splits from the end of current chunk for overlap
        for (int i = currentChunk.size() - 1; i >= 0; i--) {
            String split = currentChunk.get(i);
            int splitLength = split.length();
            int separatorLength = !overlapSplits.isEmpty() && !separator.isEmpty() ? separator.length() : 0;
            
            if (overlapLength + separatorLength + splitLength <= targetOverlapLength) {
                overlapSplits.add(0, split);
                overlapLength += splitLength + separatorLength;
            } else {
                break;
            }
        }
        
        return overlapSplits;
    }
    
    /**
     * Calculate total length of chunk splits with separator.
     * 
     * @param splits List of splits
     * @param separator Separator being used
     * @return Total length
     */
    private int calculateChunkLength(List<String> splits, String separator) {
        if (splits.isEmpty()) {
            return 0;
        }
        
        int totalLength = splits.stream().mapToInt(String::length).sum();
        if (!separator.isEmpty() && splits.size() > 1) {
            totalLength += separator.length() * (splits.size() - 1);
        }
        
        return totalLength;
    }
    
    @Override
    public boolean isSuitableFor(String content, DocumentType documentType) {
        if (content == null || content.length() < 100) {
            return false;
        }
        
        // Check if content has hierarchical structure
        boolean hasParagraphs = content.contains("\n\n");
        boolean hasLines = content.contains("\n");
        boolean hasWords = content.contains(" ");
        
        // Recursive splitter works well for structured text
        return hasParagraphs || hasLines || hasWords;
    }
    
    @Override
    public double estimateQuality(String content) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }
        
        double structureScore = 0.0;
        
        // Check for each separator type
        for (String separator : separators) {
            if (!separator.isEmpty() && content.contains(separator)) {
                structureScore += 0.25; // Each separator type adds to structure score
            }
        }
        
        // Length appropriateness
        double lengthScore = Math.min(1.0, (double) content.length() / (chunkSize * 5));
        
        // Hierarchical structure bonus
        boolean hasParagraphs = content.contains("\n\n");
        boolean hasLines = content.contains("\n");
        double hierarchyBonus = (hasParagraphs && hasLines) ? 0.2 : 0.0;
        
        return Math.min(1.0, structureScore + lengthScore * 0.3 + hierarchyBonus);
    }
    
    @Override
    public void configure(Map<String, Object> parameters) {
        if (parameters.containsKey("chunk_size")) {
            this.chunkSize = (Integer) parameters.get("chunk_size");
        }
        if (parameters.containsKey("chunk_overlap")) {
            this.chunkOverlap = (Integer) parameters.get("chunk_overlap");
        }
        if (parameters.containsKey("separators")) {
            this.separators = (String[]) parameters.get("separators");
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
        return "RecursiveCharacterTextSplitter";
    }
    
    // === Factory Methods ===
    
    /** Create default recursive splitter */
    public static RecursiveCharacterTextSplitter defaults(int chunkSize, int overlap) {
        return new RecursiveCharacterTextSplitter(chunkSize, overlap);
    }
    
    /** Create splitter for Markdown documents */
    public static RecursiveCharacterTextSplitter forMarkdown(int chunkSize, int overlap) {
        String[] markdownSeparators = {
            "\n## ",      // H2 headers
            "\n### ",     // H3 headers  
            "\n#### ",    // H4 headers
            "\n\n",       // Paragraphs
            "\n",         // Lines
            " ",          // Words
            ""            // Characters
        };
        
        return new RecursiveCharacterTextSplitter(chunkSize, overlap, markdownSeparators);
    }
    
    /** Create splitter for code documents */
    public static RecursiveCharacterTextSplitter forCode(int chunkSize, int overlap) {
        String[] codeSeparators = {
            "\nclass ",    // Class definitions
            "\ndef ",     // Function definitions
            "\n\n",       // Code blocks
            "\n",         // Lines
            " ",          // Words  
            ""            // Characters
        };
        
        return new RecursiveCharacterTextSplitter(chunkSize, overlap, codeSeparators);
    }
    
    /** Create splitter with custom separators */
    public static RecursiveCharacterTextSplitter withSeparators(int chunkSize, int overlap, String[] separators) {
        return new RecursiveCharacterTextSplitter(chunkSize, overlap, separators);
    }
}