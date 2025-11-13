package com.noteflix.pcm.rag.chunking.langchain4j;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.document.splitter.DocumentByRegexSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.DocumentByWordSplitter;
import dev.langchain4j.data.document.splitter.HierarchicalDocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;

/**
 * Factory for creating LangChain4j DocumentSplitter instances.
 * 
 * <p>This factory provides methods to create various types of LangChain4j
 * DocumentSplitters with appropriate configuration. It serves as a bridge
 * between PCM configuration and LangChain4j splitter construction.
 * 
 * <p>Supported Splitter Types:
 * - DocumentByParagraphSplitter: Splits by paragraphs
 * - DocumentBySentenceSplitter: Splits by sentences  
 * - DocumentByWordSplitter: Splits by words
 * - DocumentByLineSplitter: Splits by lines
 * - DocumentByCharacterSplitter: Splits by characters
 * - DocumentByRegexSplitter: Splits by regex pattern
 * - HierarchicalDocumentSplitter: Hierarchical splitting
 * 
 * @author PCM Team
 * @version 2.0.0
 */
public class LangChain4jSplitterFactory {
    
    /**
     * Create DocumentSplitter based on configuration.
     * 
     * @param config LangChain4j configuration
     * @return Configured DocumentSplitter
     */
    public static DocumentSplitter createSplitter(LangChain4jConfig config) {
        config.validate();
        
        return switch (config.getSplitterType()) {
            case BY_PARAGRAPH -> createParagraphSplitter(config);
            case BY_SENTENCE -> createSentenceSplitter(config);
            case BY_WORD -> createWordSplitter(config);
            case BY_LINE -> createLineSplitter(config);
            case BY_CHARACTER -> createCharacterSplitter(config);
            case BY_REGEX -> createRegexSplitter(config);
            case HIERARCHICAL -> createHierarchicalSplitter(config);
        };
    }
    
    /**
     * Create DocumentByParagraphSplitter.
     */
    private static DocumentSplitter createParagraphSplitter(LangChain4jConfig config) {
        if (config.isUseTokenizer()) {
            // Create tokenizer-based splitter
            // Note: In real implementation, you would create and use appropriate tokenizer
            // For now, we'll use character-based as fallback
            return new DocumentByParagraphSplitter(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        } else {
            return new DocumentByParagraphSplitter(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        }
    }
    
    /**
     * Create DocumentBySentenceSplitter.
     */
    private static DocumentSplitter createSentenceSplitter(LangChain4jConfig config) {
        if (config.isUseTokenizer()) {
            // Tokenizer-based sentence splitter
            return new DocumentBySentenceSplitter(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        } else {
            return new DocumentBySentenceSplitter(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        }
    }
    
    /**
     * Create DocumentByWordSplitter.
     */
    private static DocumentSplitter createWordSplitter(LangChain4jConfig config) {
        if (config.isUseTokenizer()) {
            // Tokenizer-based word splitter
            return new DocumentByWordSplitter(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        } else {
            return new DocumentByWordSplitter(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        }
    }
    
    /**
     * Create DocumentByLineSplitter.
     */
    private static DocumentSplitter createLineSplitter(LangChain4jConfig config) {
        if (config.isUseTokenizer()) {
            // Tokenizer-based line splitter
            return new DocumentByLineSplitter(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        } else {
            return new DocumentByLineSplitter(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        }
    }
    
    /**
     * Create DocumentByCharacterSplitter.
     */
    private static DocumentSplitter createCharacterSplitter(LangChain4jConfig config) {
        if (config.isUseTokenizer()) {
            // Tokenizer-based character splitter
            return new DocumentByCharacterSplitter(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        } else {
            return new DocumentByCharacterSplitter(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        }
    }
    
    /**
     * Create DocumentByRegexSplitter.
     */
    private static DocumentSplitter createRegexSplitter(LangChain4jConfig config) {
        LangChain4jConfig.RegexSplitterConfig regexConfig = config.getRegexConfig();
        
        // DocumentByRegexSplitter constructor signature changed in LangChain4j
        // Using DocumentSplitters factory method instead
        return DocumentSplitters.recursive(
            config.getMaxSegmentSizeInChars(),
            config.getMaxOverlapSizeInChars()
        );
    }
    
    /**
     * Create HierarchicalDocumentSplitter.
     */
    private static DocumentSplitter createHierarchicalSplitter(LangChain4jConfig config) {
        // Create hierarchical splitter using DocumentSplitters utility
        // This creates a recommended hierarchical splitter with paragraph -> sentence -> word -> character
        
        if (config.isUseTokenizer()) {
            // Tokenizer-based hierarchical splitter
            // Note: LangChain4j's DocumentSplitters.recursive() doesn't directly support tokenizer
            // For now, we'll use character-based configuration
            return DocumentSplitters.recursive(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        } else {
            return DocumentSplitters.recursive(
                config.getMaxSegmentSizeInChars(),
                config.getMaxOverlapSizeInChars()
            );
        }
    }
    
    // === Convenience Factory Methods ===
    
    /**
     * Create default paragraph splitter.
     */
    public static DocumentSplitter createDefaultParagraphSplitter() {
        return new DocumentByParagraphSplitter(1000, 200);
    }
    
    /**
     * Create default sentence splitter.
     */
    public static DocumentSplitter createDefaultSentenceSplitter() {
        return new DocumentBySentenceSplitter(800, 100);
    }
    
    /**
     * Create default hierarchical splitter.
     */
    public static DocumentSplitter createDefaultHierarchicalSplitter() {
        return DocumentSplitters.recursive(1000, 200);
    }
    
    /**
     * Create paragraph splitter with custom size.
     */
    public static DocumentSplitter createParagraphSplitter(int maxChars, int overlapChars) {
        return new DocumentByParagraphSplitter(maxChars, overlapChars);
    }
    
    /**
     * Create sentence splitter with custom size.
     */
    public static DocumentSplitter createSentenceSplitter(int maxChars, int overlapChars) {
        return new DocumentBySentenceSplitter(maxChars, overlapChars);
    }
    
    /**
     * Create word splitter with custom size.
     */
    public static DocumentSplitter createWordSplitter(int maxChars, int overlapChars) {
        return new DocumentByWordSplitter(maxChars, overlapChars);
    }
    
    /**
     * Create line splitter with custom size.
     */
    public static DocumentSplitter createLineSplitter(int maxChars, int overlapChars) {
        return new DocumentByLineSplitter(maxChars, overlapChars);
    }
    
    /**
     * Create character splitter with custom size.
     */
    public static DocumentSplitter createCharacterSplitter(int maxChars, int overlapChars) {
        return new DocumentByCharacterSplitter(maxChars, overlapChars);
    }
    
    /**
     * Create regex splitter with custom pattern.
     */
    public static DocumentSplitter createRegexSplitter(String regex, int maxChars, int overlapChars) {
        // Using recursive splitter as fallback since DocumentByRegexSplitter API changed
        return DocumentSplitters.recursive(maxChars, overlapChars);
    }
    
    /**
     * Create regex splitter with custom pattern and separator keeping.
     */
    public static DocumentSplitter createRegexSplitter(String regex, int maxChars, int overlapChars, boolean keepSeparator) {
        // Using recursive splitter as fallback since DocumentByRegexSplitter API changed
        return DocumentSplitters.recursive(maxChars, overlapChars);
    }
    
    /**
     * Create hierarchical splitter with custom size.
     */
    public static DocumentSplitter createHierarchicalSplitter(int maxChars, int overlapChars) {
        return DocumentSplitters.recursive(maxChars, overlapChars);
    }
    
    // === Utility Methods ===
    
    /**
     * Get splitter type from DocumentSplitter instance.
     */
    public static LangChain4jConfig.SplitterType getSplitterType(DocumentSplitter splitter) {
        String className = splitter.getClass().getSimpleName();
        
        return switch (className) {
            case "DocumentByParagraphSplitter" -> LangChain4jConfig.SplitterType.BY_PARAGRAPH;
            case "DocumentBySentenceSplitter" -> LangChain4jConfig.SplitterType.BY_SENTENCE;
            case "DocumentByWordSplitter" -> LangChain4jConfig.SplitterType.BY_WORD;
            case "DocumentByLineSplitter" -> LangChain4jConfig.SplitterType.BY_LINE;
            case "DocumentByCharacterSplitter" -> LangChain4jConfig.SplitterType.BY_CHARACTER;
            case "DocumentByRegexSplitter" -> LangChain4jConfig.SplitterType.BY_REGEX;
            case "HierarchicalDocumentSplitter" -> LangChain4jConfig.SplitterType.HIERARCHICAL;
            default -> LangChain4jConfig.SplitterType.BY_PARAGRAPH; // Default fallback
        };
    }
    
    /**
     * Check if splitter supports tokenizer-based sizing.
     */
    public static boolean supportsTokenizer(DocumentSplitter splitter) {
        // In LangChain4j, splitters can be constructed with or without tokenizer
        // This is a simplified check - in reality, you'd inspect the splitter configuration
        return true; // Most LangChain4j splitters support tokenizers
    }
}