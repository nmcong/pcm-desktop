package com.noteflix.pcm.rag.chunking.langchain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LangChainConfig and its nested configuration classes.
 */
public class LangChainConfigTest {

    @Test
    void testDefaultConfiguration() {
        LangChainConfig config = LangChainConfig.defaults();
        
        assertEquals(1000, config.getChunkSize());
        assertEquals(200, config.getChunkOverlap());
        assertTrue(config.isStripWhitespace());
        assertFalse(config.isKeepSeparator());
        
        // Should have default configurations for all splitter types
        assertNotNull(config.getCharacterConfig());
        assertNotNull(config.getRecursiveConfig());
        assertNotNull(config.getTokenConfig());
    }

    @Test
    void testBuilderPattern() {
        LangChainConfig config = LangChainConfig.builder()
                .chunkSize(500)
                .chunkOverlap(100)
                .stripWhitespace(false)
                .keepSeparator(true)
                .build();
        
        assertEquals(500, config.getChunkSize());
        assertEquals(100, config.getChunkOverlap());
        assertFalse(config.isStripWhitespace());
        assertTrue(config.isKeepSeparator());
    }

    @Test
    void testCharacterSplitterConfig() {
        LangChainConfig config = LangChainConfig.forCharacterSplitting();
        
        assertEquals(1000, config.getChunkSize());
        assertEquals(200, config.getChunkOverlap());
        
        LangChainConfig.CharacterTextSplitterConfig charConfig = config.getCharacterConfig();
        assertNotNull(charConfig);
        assertEquals("\n\n", charConfig.getSeparator());
        assertFalse(charConfig.isSeparatorRegex());
        
        // Test custom character configuration
        LangChainConfig customConfig = LangChainConfig.builder()
                .characterConfig(LangChainConfig.CharacterTextSplitterConfig.builder()
                    .separator("\\.")
                    .isSeparatorRegex(true)
                    .build())
                .build();
        
        assertEquals("\\.", customConfig.getCharacterConfig().getSeparator());
        assertTrue(customConfig.getCharacterConfig().isSeparatorRegex());
    }

    @Test
    void testRecursiveSplitterConfig() {
        LangChainConfig config = LangChainConfig.forRecursiveSplitting();
        
        assertEquals(1000, config.getChunkSize());
        assertEquals(200, config.getChunkOverlap());
        
        LangChainConfig.RecursiveCharacterTextSplitterConfig recursiveConfig = config.getRecursiveConfig();
        assertNotNull(recursiveConfig);
        assertArrayEquals(new String[]{"\n\n", "\n", " ", ""}, recursiveConfig.getSeparators());
        assertTrue(recursiveConfig.isKeepSeparator());
        assertFalse(recursiveConfig.isSeparatorRegex());
        
        // Test custom recursive configuration
        String[] customSeparators = {"\n### ", "\n## ", "\n\n", "\n", " "};
        LangChainConfig customConfig = LangChainConfig.builder()
                .recursiveConfig(LangChainConfig.RecursiveCharacterTextSplitterConfig.builder()
                    .separators(customSeparators)
                    .keepSeparator(false)
                    .isSeparatorRegex(true)
                    .build())
                .build();
        
        assertArrayEquals(customSeparators, customConfig.getRecursiveConfig().getSeparators());
        assertFalse(customConfig.getRecursiveConfig().isKeepSeparator());
        assertTrue(customConfig.getRecursiveConfig().isSeparatorRegex());
    }

    @Test
    void testTokenSplitterConfig() {
        LangChainConfig config = LangChainConfig.forTokenSplitting("gpt-4");
        
        assertEquals(500, config.getChunkSize()); // Token-based should be smaller
        assertEquals(50, config.getChunkOverlap());
        
        LangChainConfig.TokenTextSplitterConfig tokenConfig = config.getTokenConfig();
        assertNotNull(tokenConfig);
        assertEquals("gpt-4", tokenConfig.getModelName());
        assertEquals("cl100k_base", tokenConfig.getEncodingName());
        
        // Test with different model
        LangChainConfig gpt35Config = LangChainConfig.forTokenSplitting("gpt-3.5-turbo");
        assertEquals("gpt-3.5-turbo", gpt35Config.getTokenConfig().getModelName());
        assertEquals("cl100k_base", gpt35Config.getTokenConfig().getEncodingName());
        
        // Test custom token configuration
        LangChainConfig customConfig = LangChainConfig.builder()
                .tokenConfig(LangChainConfig.TokenTextSplitterConfig.builder()
                    .modelName("custom-model")
                    .encodingName("custom-encoding")
                    .build())
                .build();
        
        assertEquals("custom-model", customConfig.getTokenConfig().getModelName());
        assertEquals("custom-encoding", customConfig.getTokenConfig().getEncodingName());
    }

    @Test
    void testCodeSplitterConfig() {
        LangChainConfig config = LangChainConfig.forCodeSplitting("python");
        
        assertEquals(1500, config.getChunkSize());
        assertEquals(150, config.getChunkOverlap());
        
        // Code splitting uses recursive splitter with code-specific separators
        LangChainConfig.RecursiveCharacterTextSplitterConfig recursiveConfig = config.getRecursiveConfig();
        assertNotNull(recursiveConfig);
        
        // Should have code-specific separators
        String[] separators = recursiveConfig.getSeparators();
        assertNotNull(separators);
        assertTrue(separators.length > 0);
    }

    @Test
    void testValidation() {
        // Valid configuration should pass
        LangChainConfig validConfig = LangChainConfig.builder()
                .chunkSize(1000)
                .chunkOverlap(200)
                .build();
        
        assertDoesNotThrow(() -> validConfig.validate());
        
        // Invalid chunk size
        LangChainConfig invalidSizeConfig = LangChainConfig.builder()
                .chunkSize(0)
                .chunkOverlap(200)
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> invalidSizeConfig.validate());
        
        // Invalid overlap (greater than chunk size)
        LangChainConfig invalidOverlapConfig = LangChainConfig.builder()
                .chunkSize(100)
                .chunkOverlap(150)
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> invalidOverlapConfig.validate());
        
        // Negative overlap
        LangChainConfig negativeOverlapConfig = LangChainConfig.builder()
                .chunkSize(100)
                .chunkOverlap(-10)
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> negativeOverlapConfig.validate());
    }

    @Test
    void testFactoryMethods() {
        // Test all factory methods produce valid configurations
        assertDoesNotThrow(() -> LangChainConfig.defaults().validate());
        assertDoesNotThrow(() -> LangChainConfig.forCharacterSplitting().validate());
        assertDoesNotThrow(() -> LangChainConfig.forRecursiveSplitting().validate());
        assertDoesNotThrow(() -> LangChainConfig.forTokenSplitting("gpt-3.5-turbo").validate());
        assertDoesNotThrow(() -> LangChainConfig.forCodeSplitting("java").validate());
        
        // Test factory method parameters
        LangChainConfig tokenConfig = LangChainConfig.forTokenSplitting("test-model");
        assertEquals("test-model", tokenConfig.getTokenConfig().getModelName());
        
        LangChainConfig codeConfig = LangChainConfig.forCodeSplitting("javascript");
        assertNotNull(codeConfig.getRecursiveConfig());
    }

    @Test
    void testNestedConfigurationBuilders() {
        // Test character config builder
        LangChainConfig.CharacterTextSplitterConfig charConfig = 
                LangChainConfig.CharacterTextSplitterConfig.builder()
                    .separator("---")
                    .isSeparatorRegex(false)
                    .build();
        
        assertEquals("---", charConfig.getSeparator());
        assertFalse(charConfig.isSeparatorRegex());
        
        // Test recursive config builder
        String[] testSeparators = {"\n\n", "\n", " "};
        LangChainConfig.RecursiveCharacterTextSplitterConfig recursiveConfig = 
                LangChainConfig.RecursiveCharacterTextSplitterConfig.builder()
                    .separators(testSeparators)
                    .keepSeparator(true)
                    .isSeparatorRegex(false)
                    .build();
        
        assertArrayEquals(testSeparators, recursiveConfig.getSeparators());
        assertTrue(recursiveConfig.isKeepSeparator());
        assertFalse(recursiveConfig.isSeparatorRegex());
        
        // Test token config builder
        LangChainConfig.TokenTextSplitterConfig tokenConfig = 
                LangChainConfig.TokenTextSplitterConfig.builder()
                    .modelName("test-model")
                    .encodingName("test-encoding")
                    .build();
        
        assertEquals("test-model", tokenConfig.getModelName());
        assertEquals("test-encoding", tokenConfig.getEncodingName());
    }

    @Test
    void testConfigurationCopy() {
        LangChainConfig original = LangChainConfig.builder()
                .chunkSize(800)
                .chunkOverlap(160)
                .stripWhitespace(false)
                .keepSeparator(true)
                .characterConfig(LangChainConfig.CharacterTextSplitterConfig.builder()
                    .separator("\\.")
                    .isSeparatorRegex(true)
                    .build())
                .build();
        
        // Create a copy with modified values
        LangChainConfig copy = original.toBuilder()
                .chunkSize(1200)
                .build();
        
        // Original should be unchanged
        assertEquals(800, original.getChunkSize());
        assertEquals(160, original.getChunkOverlap());
        
        // Copy should have new chunk size but same other values
        assertEquals(1200, copy.getChunkSize());
        assertEquals(160, copy.getChunkOverlap());
        assertEquals(original.isStripWhitespace(), copy.isStripWhitespace());
        assertEquals(original.isKeepSeparator(), copy.isKeepSeparator());
    }

    @Test
    void testConfigurationDefaults() {
        // Test that default configurations have sensible values
        LangChainConfig.CharacterTextSplitterConfig defaultCharConfig = 
                LangChainConfig.CharacterTextSplitterConfig.defaults();
        assertEquals("\n\n", defaultCharConfig.getSeparator());
        assertFalse(defaultCharConfig.isSeparatorRegex());
        
        LangChainConfig.RecursiveCharacterTextSplitterConfig defaultRecursiveConfig = 
                LangChainConfig.RecursiveCharacterTextSplitterConfig.defaults();
        assertArrayEquals(new String[]{"\n\n", "\n", " ", ""}, defaultRecursiveConfig.getSeparators());
        assertTrue(defaultRecursiveConfig.isKeepSeparator());
        assertFalse(defaultRecursiveConfig.isSeparatorRegex());
        
        LangChainConfig.TokenTextSplitterConfig defaultTokenConfig = 
                LangChainConfig.TokenTextSplitterConfig.defaults();
        assertEquals("gpt-3.5-turbo", defaultTokenConfig.getModelName());
        assertEquals("cl100k_base", defaultTokenConfig.getEncodingName());
    }

    @Test
    void testConfigurationEdgeCases() {
        // Test minimum valid values
        LangChainConfig minConfig = LangChainConfig.builder()
                .chunkSize(1)
                .chunkOverlap(0)
                .build();
        
        assertDoesNotThrow(() -> minConfig.validate());
        
        // Test large values
        LangChainConfig largeConfig = LangChainConfig.builder()
                .chunkSize(100000)
                .chunkOverlap(50000)
                .build();
        
        assertDoesNotThrow(() -> largeConfig.validate());
        
        // Test empty separator
        LangChainConfig.CharacterTextSplitterConfig emptySeperatorConfig = 
                LangChainConfig.CharacterTextSplitterConfig.builder()
                    .separator("")
                    .build();
        
        assertEquals("", emptySeperatorConfig.getSeparator());
        
        // Test empty separators array
        LangChainConfig.RecursiveCharacterTextSplitterConfig emptySeparatorsConfig = 
                LangChainConfig.RecursiveCharacterTextSplitterConfig.builder()
                    .separators(new String[]{})
                    .build();
        
        assertEquals(0, emptySeparatorsConfig.getSeparators().length);
    }
}