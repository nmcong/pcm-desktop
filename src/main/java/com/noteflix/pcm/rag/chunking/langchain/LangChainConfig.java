package com.noteflix.pcm.rag.chunking.langchain;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for LangChain text splitters integration.
 * 
 * <p>Provides configuration options for various LangChain text splitters
 * including size limits, overlap settings, and splitter-specific parameters.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class LangChainConfig {
    
    // === Basic Configuration ===
    
    /** Target chunk size in characters */
    @Builder.Default
    private int chunkSize = 1000;
    
    /** Overlap between chunks in characters */
    @Builder.Default
    private int chunkOverlap = 200;
    
    /** Whether to keep separator characters */
    @Builder.Default
    private boolean keepSeparator = false;
    
    /** Whether to strip whitespace from chunks */
    @Builder.Default
    private boolean stripWhitespace = true;
    
    // === Quality Settings ===
    
    /** Minimum chunk size to accept */
    @Builder.Default
    private int minChunkSize = 50;
    
    /** Maximum chunk size allowed */
    @Builder.Default
    private int maxChunkSize = 2000;
    
    /** Whether to enable quality validation */
    @Builder.Default
    private boolean enableQualityValidation = true;
    
    /** Minimum quality threshold */
    @Builder.Default
    private double minQualityThreshold = 0.3;
    
    // === Splitter-Specific Configuration ===
    
    /** Configuration for Character Text Splitter */
    @Builder.Default
    private CharacterTextSplitterConfig characterConfig = CharacterTextSplitterConfig.defaults();
    
    /** Configuration for Recursive Character Text Splitter */
    @Builder.Default
    private RecursiveCharacterTextSplitterConfig recursiveConfig = RecursiveCharacterTextSplitterConfig.defaults();
    
    /** Configuration for Token Text Splitter */
    @Builder.Default
    private TokenTextSplitterConfig tokenConfig = TokenTextSplitterConfig.defaults();
    
    /** Configuration for Language-specific splitters */
    @Builder.Default
    private LanguageSplitterConfig languageConfig = LanguageSplitterConfig.defaults();
    
    /** Configuration for Code splitters */
    @Builder.Default
    private CodeSplitterConfig codeConfig = CodeSplitterConfig.defaults();
    
    // === Advanced Options ===
    
    /** Custom parameters for specific splitters */
    @Builder.Default
    private Map<String, Object> customParameters = new HashMap<>();
    
    /** Whether to preserve metadata through splitting */
    @Builder.Default
    private boolean preserveMetadata = true;
    
    /** Whether to add position metadata */
    @Builder.Default
    private boolean addPositionMetadata = true;
    
    // === Factory Methods ===
    
    /** Create default configuration */
    public static LangChainConfig defaults() {
        return LangChainConfig.builder().build();
    }
    
    /** Create configuration for character-based splitting */
    public static LangChainConfig forCharacterSplitting() {
        return LangChainConfig.builder()
            .chunkSize(1000)
            .chunkOverlap(200)
            .characterConfig(CharacterTextSplitterConfig.defaults())
            .build();
    }
    
    /** Create configuration for recursive character splitting */
    public static LangChainConfig forRecursiveSplitting() {
        return LangChainConfig.builder()
            .chunkSize(1000)
            .chunkOverlap(200)
            .recursiveConfig(RecursiveCharacterTextSplitterConfig.defaults())
            .build();
    }
    
    /** Create configuration for token-based splitting */
    public static LangChainConfig forTokenSplitting(String modelName) {
        return LangChainConfig.builder()
            .chunkSize(500) // Smaller for token-based
            .chunkOverlap(50)
            .tokenConfig(TokenTextSplitterConfig.forModel(modelName))
            .build();
    }
    
    /** Create configuration for code splitting */
    public static LangChainConfig forCodeSplitting(String language) {
        return LangChainConfig.builder()
            .chunkSize(1500) // Larger for code blocks
            .chunkOverlap(100)
            .codeConfig(CodeSplitterConfig.forLanguage(language))
            .build();
    }
    
    /** Create configuration for large documents */
    public static LangChainConfig forLargeDocuments() {
        return LangChainConfig.builder()
            .chunkSize(2000)
            .chunkOverlap(400)
            .maxChunkSize(4000)
            .enableQualityValidation(false) // Skip for performance
            .build();
    }
    
    // === Validation ===
    
    /**
     * Validate configuration parameters.
     * 
     * @throws IllegalArgumentException if configuration is invalid
     */
    public void validate() {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        }
        if (chunkOverlap < 0 || chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException("Invalid chunk overlap");
        }
        if (minChunkSize <= 0 || minChunkSize > chunkSize) {
            throw new IllegalArgumentException("Invalid min chunk size");
        }
        if (maxChunkSize <= chunkSize) {
            throw new IllegalArgumentException("Invalid max chunk size");
        }
        if (minQualityThreshold < 0.0 || minQualityThreshold > 1.0) {
            throw new IllegalArgumentException("Quality threshold must be between 0.0 and 1.0");
        }
    }
    
    // === Nested Configuration Classes ===
    
    @Data
    @Builder
    public static class CharacterTextSplitterConfig {
        @Builder.Default
        private String separator = "\n\n";
        
        @Builder.Default
        private boolean isSeparatorRegex = false;
        
        public static CharacterTextSplitterConfig defaults() {
            return CharacterTextSplitterConfig.builder().build();
        }
        
        public static CharacterTextSplitterConfig withSeparator(String separator) {
            return CharacterTextSplitterConfig.builder()
                .separator(separator)
                .build();
        }
    }
    
    @Data
    @Builder
    public static class RecursiveCharacterTextSplitterConfig {
        @Builder.Default
        private String[] separators = {"\n\n", "\n", " ", ""};
        
        @Builder.Default
        private boolean keepSeparator = true;
        
        @Builder.Default
        private boolean isSeparatorRegex = false;
        
        public static RecursiveCharacterTextSplitterConfig defaults() {
            return RecursiveCharacterTextSplitterConfig.builder().build();
        }
        
        public static RecursiveCharacterTextSplitterConfig forMarkdown() {
            return RecursiveCharacterTextSplitterConfig.builder()
                .separators(new String[]{"\\n## ", "\\n### ", "\\n\\n", "\\n", " ", ""})
                .isSeparatorRegex(true)
                .build();
        }
    }
    
    @Data
    @Builder
    public static class TokenTextSplitterConfig {
        @Builder.Default
        private String modelName = "gpt-3.5-turbo";
        
        @Builder.Default
        private String encodingName = "cl100k_base";
        
        @Builder.Default
        private int allowedSpecial = 0;
        
        @Builder.Default
        private int disallowedSpecial = 0;
        
        public static TokenTextSplitterConfig defaults() {
            return TokenTextSplitterConfig.builder().build();
        }
        
        public static TokenTextSplitterConfig forModel(String modelName) {
            return TokenTextSplitterConfig.builder()
                .modelName(modelName)
                .build();
        }
    }
    
    @Data
    @Builder
    public static class LanguageSplitterConfig {
        @Builder.Default
        private String language = "python";
        
        public static LanguageSplitterConfig defaults() {
            return LanguageSplitterConfig.builder().build();
        }
        
        public static LanguageSplitterConfig forLanguage(String language) {
            return LanguageSplitterConfig.builder()
                .language(language)
                .build();
        }
    }
    
    @Data
    @Builder
    public static class CodeSplitterConfig {
        @Builder.Default
        private String language = "python";
        
        @Builder.Default
        private boolean preserveFunctions = true;
        
        @Builder.Default
        private boolean preserveClasses = true;
        
        @Builder.Default
        private boolean preserveComments = true;
        
        @Builder.Default
        private int maxCharsPerLine = 100;
        
        public static CodeSplitterConfig defaults() {
            return CodeSplitterConfig.builder().build();
        }
        
        public static CodeSplitterConfig forLanguage(String language) {
            return CodeSplitterConfig.builder()
                .language(language)
                .build();
        }
        
        public static CodeSplitterConfig preservingStructure() {
            return CodeSplitterConfig.builder()
                .preserveFunctions(true)
                .preserveClasses(true)
                .preserveComments(true)
                .build();
        }
    }
}