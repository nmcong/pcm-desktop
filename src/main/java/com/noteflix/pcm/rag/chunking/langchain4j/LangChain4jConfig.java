package com.noteflix.pcm.rag.chunking.langchain4j;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration class for LangChain4j integration with PCM chunking system.
 * 
 * <p>This configuration provides settings for using real LangChain4j DocumentSplitters
 * within the PCM framework. It includes parameters for chunk sizing, overlaps,
 * and various splitter-specific settings.
 * 
 * <p>Features:
 * - Support for character and token-based sizing
 * - Flexible overlap configuration
 * - Splitter-specific parameter support
 * - Validation for configuration consistency
 * 
 * @author PCM Team
 * @version 2.0.0
 */
@Data
@Builder(toBuilder = true)
public class LangChain4jConfig {
    
    // === Size Configuration ===
    
    /** Maximum segment size in characters (used by most splitters) */
    @Builder.Default
    private int maxSegmentSizeInChars = 1000;
    
    /** Maximum overlap size in characters */
    @Builder.Default
    private int maxOverlapSizeInChars = 200;
    
    /** Maximum segment size in tokens (when using tokenizer) */
    @Builder.Default
    private int maxSegmentSizeInTokens = 250;
    
    /** Maximum overlap size in tokens (when using tokenizer) */
    @Builder.Default
    private int maxOverlapSizeInTokens = 50;
    
    // === Tokenizer Configuration ===
    
    /** Whether to use token-based sizing instead of character-based */
    @Builder.Default
    private boolean useTokenizer = false;
    
    /** Tokenizer model name (e.g., "gpt-4", "gpt-3.5-turbo") */
    @Builder.Default
    private String tokenizerModel = "gpt-3.5-turbo";
    
    // === Splitter Type Configuration ===
    
    /** Type of LangChain4j splitter to use */
    @Builder.Default
    private SplitterType splitterType = SplitterType.BY_PARAGRAPH;
    
    // === Quality and Processing Options ===
    
    /** Whether to include segment metadata in chunks */
    @Builder.Default
    private boolean includeSegmentMetadata = true;
    
    /** Whether to preserve original document metadata */
    @Builder.Default
    private boolean preserveOriginalMetadata = true;
    
    /** Whether to calculate quality scores for chunks */
    @Builder.Default
    private boolean calculateQualityScores = true;
    
    // === Splitter-Specific Configuration ===
    
    /** Configuration for regex-based splitter */
    @Builder.Default
    private RegexSplitterConfig regexConfig = RegexSplitterConfig.defaults();
    
    /** Configuration for hierarchical splitter */
    @Builder.Default
    private HierarchicalSplitterConfig hierarchicalConfig = HierarchicalSplitterConfig.defaults();
    
    // === Factory Methods ===
    
    /** Create default configuration. */
    public static LangChain4jConfig defaults() {
        return LangChain4jConfig.builder().build();
    }
    
    /** Create configuration for character-based paragraph splitter. */
    public static LangChain4jConfig forCharacterParagraphSplitter(int maxChars, int overlapChars) {
        return LangChain4jConfig.builder()
                .splitterType(SplitterType.BY_PARAGRAPH)
                .maxSegmentSizeInChars(maxChars)
                .maxOverlapSizeInChars(overlapChars)
                .useTokenizer(false)
                .build();
    }
    
    /** Create configuration for token-based paragraph splitter. */
    public static LangChain4jConfig forTokenParagraphSplitter(int maxTokens, int overlapTokens, String model) {
        return LangChain4jConfig.builder()
                .splitterType(SplitterType.BY_PARAGRAPH)
                .maxSegmentSizeInTokens(maxTokens)
                .maxOverlapSizeInTokens(overlapTokens)
                .tokenizerModel(model)
                .useTokenizer(true)
                .build();
    }
    
    /** Create configuration for sentence-based splitter. */
    public static LangChain4jConfig forSentenceSplitter(int maxChars, int overlapChars) {
        return LangChain4jConfig.builder()
                .splitterType(SplitterType.BY_SENTENCE)
                .maxSegmentSizeInChars(maxChars)
                .maxOverlapSizeInChars(overlapChars)
                .useTokenizer(false)
                .build();
    }
    
    /** Create configuration for word-based splitter. */
    public static LangChain4jConfig forWordSplitter(int maxChars, int overlapChars) {
        return LangChain4jConfig.builder()
                .splitterType(SplitterType.BY_WORD)
                .maxSegmentSizeInChars(maxChars)
                .maxOverlapSizeInChars(overlapChars)
                .useTokenizer(false)
                .build();
    }
    
    /** Create configuration for line-based splitter. */
    public static LangChain4jConfig forLineSplitter(int maxChars, int overlapChars) {
        return LangChain4jConfig.builder()
                .splitterType(SplitterType.BY_LINE)
                .maxSegmentSizeInChars(maxChars)
                .maxOverlapSizeInChars(overlapChars)
                .useTokenizer(false)
                .build();
    }
    
    /** Create configuration for character-based splitter. */
    public static LangChain4jConfig forCharacterSplitter(int maxChars, int overlapChars) {
        return LangChain4jConfig.builder()
                .splitterType(SplitterType.BY_CHARACTER)
                .maxSegmentSizeInChars(maxChars)
                .maxOverlapSizeInChars(overlapChars)
                .useTokenizer(false)
                .build();
    }
    
    /** Create configuration for regex-based splitter. */
    public static LangChain4jConfig forRegexSplitter(String regex, int maxChars, int overlapChars) {
        return LangChain4jConfig.builder()
                .splitterType(SplitterType.BY_REGEX)
                .maxSegmentSizeInChars(maxChars)
                .maxOverlapSizeInChars(overlapChars)
                .regexConfig(RegexSplitterConfig.builder()
                    .regex(regex)
                    .build())
                .useTokenizer(false)
                .build();
    }
    
    /** Create configuration for hierarchical splitter. */
    public static LangChain4jConfig forHierarchicalSplitter(int maxChars, int overlapChars) {
        return LangChain4jConfig.builder()
                .splitterType(SplitterType.HIERARCHICAL)
                .maxSegmentSizeInChars(maxChars)
                .maxOverlapSizeInChars(overlapChars)
                .hierarchicalConfig(HierarchicalSplitterConfig.defaults())
                .useTokenizer(false)
                .build();
    }
    
    // === Validation ===
    
    /**
     * Validate configuration parameters.
     * 
     * @throws IllegalArgumentException if configuration is invalid
     */
    public void validate() {
        if (maxSegmentSizeInChars <= 0) {
            throw new IllegalArgumentException("maxSegmentSizeInChars must be positive");
        }
        
        if (maxOverlapSizeInChars < 0) {
            throw new IllegalArgumentException("maxOverlapSizeInChars must be non-negative");
        }
        
        if (maxOverlapSizeInChars >= maxSegmentSizeInChars) {
            throw new IllegalArgumentException("maxOverlapSizeInChars must be less than maxSegmentSizeInChars");
        }
        
        if (useTokenizer) {
            if (maxSegmentSizeInTokens <= 0) {
                throw new IllegalArgumentException("maxSegmentSizeInTokens must be positive when using tokenizer");
            }
            
            if (maxOverlapSizeInTokens < 0) {
                throw new IllegalArgumentException("maxOverlapSizeInTokens must be non-negative");
            }
            
            if (maxOverlapSizeInTokens >= maxSegmentSizeInTokens) {
                throw new IllegalArgumentException("maxOverlapSizeInTokens must be less than maxSegmentSizeInTokens");
            }
            
            if (tokenizerModel == null || tokenizerModel.trim().isEmpty()) {
                throw new IllegalArgumentException("tokenizerModel must be specified when using tokenizer");
            }
        }
        
        if (splitterType == null) {
            throw new IllegalArgumentException("splitterType must be specified");
        }
        
        // Validate splitter-specific configurations
        if (splitterType == SplitterType.BY_REGEX && regexConfig != null) {
            regexConfig.validate();
        }
        
        if (splitterType == SplitterType.HIERARCHICAL && hierarchicalConfig != null) {
            hierarchicalConfig.validate();
        }
    }
    
    // === Inner Classes ===
    
    /** Enum for different LangChain4j splitter types. */
    public enum SplitterType {
        BY_PARAGRAPH("DocumentByParagraphSplitter", "Splits by paragraphs"),
        BY_SENTENCE("DocumentBySentenceSplitter", "Splits by sentences"),
        BY_WORD("DocumentByWordSplitter", "Splits by words"),
        BY_LINE("DocumentByLineSplitter", "Splits by lines"),
        BY_CHARACTER("DocumentByCharacterSplitter", "Splits by characters"),
        BY_REGEX("DocumentByRegexSplitter", "Splits by regex pattern"),
        HIERARCHICAL("HierarchicalDocumentSplitter", "Hierarchical splitting strategy");
        
        private final String className;
        private final String description;
        
        SplitterType(String className, String description) {
            this.className = className;
            this.description = description;
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /** Configuration for regex-based splitter. */
    @Data
    @Builder
    public static class RegexSplitterConfig {
        @Builder.Default
        private String regex = "\\n\\n";
        
        @Builder.Default
        private boolean keepSeparator = false;
        
        public static RegexSplitterConfig defaults() {
            return RegexSplitterConfig.builder().build();
        }
        
        public void validate() {
            if (regex == null || regex.trim().isEmpty()) {
                throw new IllegalArgumentException("regex pattern cannot be null or empty");
            }
        }
    }
    
    /** Configuration for hierarchical splitter. */
    @Data
    @Builder
    public static class HierarchicalSplitterConfig {
        /** Fallback strategy when splitting fails */
        @Builder.Default
        private SplitterType fallbackSplitter = SplitterType.BY_CHARACTER;
        
        /** Maximum recursion depth */
        @Builder.Default
        private int maxRecursionDepth = 5;
        
        /** Whether to use progressive sizing */
        @Builder.Default
        private boolean useProgressiveSizing = true;
        
        public static HierarchicalSplitterConfig defaults() {
            return HierarchicalSplitterConfig.builder().build();
        }
        
        public void validate() {
            if (maxRecursionDepth <= 0) {
                throw new IllegalArgumentException("maxRecursionDepth must be positive");
            }
            
            if (fallbackSplitter == null) {
                throw new IllegalArgumentException("fallbackSplitter must be specified");
            }
        }
    }
}