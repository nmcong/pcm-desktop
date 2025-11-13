package com.noteflix.pcm.rag.chunking.langchain.splitters;

import com.noteflix.pcm.rag.chunking.langchain.LangChainConfig;
import com.noteflix.pcm.rag.chunking.langchain.LangChainDocument;
import com.noteflix.pcm.rag.model.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;

/**
 * Tests for TokenTextSplitter implementation.
 */
public class TokenTextSplitterTest {

    private TokenTextSplitter splitter;
    private String testText;

    @BeforeEach
    void setUp() {
        splitter = new TokenTextSplitter(100, 20, "gpt-3.5-turbo");
        testText = """
            This is a comprehensive test document for token-based text splitting.
            The document contains various types of content including sentences,
            paragraphs, and different punctuation marks. It should provide a good
            foundation for testing how the token splitter handles different text
            structures and maintains appropriate token counts within specified limits.
            """;
    }

    @Test
    void testBasicTokenSplitting() {
        List<String> chunks = splitter.splitText(testText);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify chunks respect token limits (approximately)
        for (String chunk : chunks) {
            assertFalse(chunk.trim().isEmpty());
            // Token estimation is approximate, so we allow some tolerance
            int estimatedTokens = (int) Math.ceil(chunk.length() * 0.25);
            assertTrue(estimatedTokens <= 120); // Allow tolerance for target of 100
        }
    }

    @Test
    void testDocumentSplitting() {
        LangChainDocument doc = new LangChainDocument(testText);
        doc.addMetadata("model", "test_model");
        doc.addMetadata("source_type", "test_document");

        List<LangChainDocument> chunks = splitter.splitDocuments(List.of(doc));
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify metadata preservation and token-specific metadata
        for (LangChainDocument chunk : chunks) {
            assertEquals("test_model", chunk.getMetadata().get("model"));
            assertEquals("test_document", chunk.getMetadata().get("source_type"));
            assertEquals("TokenTextSplitter", chunk.getMetadata().get("splitter_type"));
            assertEquals("gpt-3.5-turbo", chunk.getMetadata().get("model_name"));
            assertEquals("cl100k_base", chunk.getMetadata().get("encoding"));
            assertTrue(chunk.getMetadata().containsKey("estimated_tokens"));
            assertTrue(chunk.getMetadata().containsKey("chunk_index"));
            assertTrue(chunk.getMetadata().containsKey("chunk_count"));
            
            // Verify token estimation
            Integer estimatedTokens = (Integer) chunk.getMetadata().get("estimated_tokens");
            assertNotNull(estimatedTokens);
            assertTrue(estimatedTokens > 0);
        }
    }

    @Test
    void testDifferentModels() {
        // Test GPT-3.5-turbo
        TokenTextSplitter gpt35Splitter = TokenTextSplitter.forGPT(80, 15);
        assertEquals("gpt-3.5-turbo", gpt35Splitter.modelName);
        assertEquals(80, gpt35Splitter.getChunkSize());
        assertEquals(15, gpt35Splitter.getChunkOverlap());
        
        // Test GPT-4
        TokenTextSplitter gpt4Splitter = TokenTextSplitter.forGPT4(120, 25);
        assertEquals("gpt-4", gpt4Splitter.modelName);
        assertEquals(120, gpt4Splitter.getChunkSize());
        assertEquals(25, gpt4Splitter.getChunkOverlap());
        
        // Test custom model
        TokenTextSplitter customSplitter = TokenTextSplitter.forModel("custom-model", 90, 18);
        assertEquals("custom-model", customSplitter.modelName);
        assertEquals(90, customSplitter.getChunkSize());
        assertEquals(18, customSplitter.getChunkOverlap());
    }

    @Test
    void testSuitabilityAssessment() {
        // Should be suitable for reasonable text
        assertTrue(splitter.isSuitableFor(testText, DocumentType.TEXT));
        
        // Should not be suitable for very short text
        assertFalse(splitter.isSuitableFor("Hi", DocumentType.TEXT));
        
        // Should not be suitable for null or empty text
        assertFalse(splitter.isSuitableFor(null, DocumentType.TEXT));
        assertFalse(splitter.isSuitableFor("", DocumentType.TEXT));
        
        // Should handle different document types
        assertTrue(splitter.isSuitableFor(testText, DocumentType.MARKDOWN));
        assertTrue(splitter.isSuitableFor(testText, DocumentType.CODE));
        
        // Should not be suitable for extremely large documents
        StringBuilder hugeText = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            hugeText.append("word ");
        }
        assertFalse(splitter.isSuitableFor(hugeText.toString(), DocumentType.TEXT));
    }

    @Test
    void testQualityEstimation() {
        double quality = splitter.estimateQuality(testText);
        
        assertTrue(quality >= 0.0);
        assertTrue(quality <= 1.0);
        
        // Well-structured text should have reasonable quality
        assertTrue(quality > 0.3);
        
        // Empty text should have zero quality
        assertEquals(0.0, splitter.estimateQuality(""));
        assertEquals(0.0, splitter.estimateQuality(null));
        
        // Test with different text types
        String structuredText = "This is a well-structured sentence. It has proper punctuation and spacing.";
        String unstructuredText = "thisislongwordwithoutspacesorpunctuationandisnotgoodfortokensplitting";
        
        double structuredQuality = splitter.estimateQuality(structuredText);
        double unstructuredQuality = splitter.estimateQuality(unstructuredText);
        
        assertTrue(structuredQuality > unstructuredQuality, 
                "Structured text should have higher quality than unstructured");
    }

    @Test
    void testConfiguration() {
        Map<String, Object> params = Map.of(
                "chunk_size", 150,
                "chunk_overlap", 30,
                "model_name", "gpt-4",
                "encoding_name", "custom_encoding"
        );
        
        splitter.configure(params);
        
        assertEquals(150, splitter.getChunkSize());
        assertEquals(30, splitter.getChunkOverlap());
    }

    @Test
    void testEmptyAndNullInput() {
        assertTrue(splitter.splitText(null).isEmpty());
        assertTrue(splitter.splitText("").isEmpty());
        assertTrue(splitter.splitText("   ").isEmpty());
    }

    @Test
    void testTokenization() {
        // Test with text that should be split into multiple tokens
        String tokenTestText = "Hello world! This is a test sentence with punctuation, numbers 123, and symbols @#$.";
        
        List<String> chunks = splitter.splitText(tokenTestText);
        assertNotNull(chunks);
        
        // Single sentence should fit in one chunk with our settings
        if (chunks.size() == 1) {
            assertEquals(tokenTestText, chunks.get(0));
        }
    }

    @Test
    void testOverlapBehavior() {
        // Use smaller token count to force multiple chunks
        TokenTextSplitter smallChunkSplitter = new TokenTextSplitter(20, 5, "gpt-3.5-turbo");
        
        // Create longer text to ensure multiple chunks
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            longText.append("This is sentence number ").append(i).append(". ");
        }
        
        List<String> chunks = smallChunkSplitter.splitText(longText.toString());
        
        assertTrue(chunks.size() > 1, "Should create multiple chunks");
        
        // Verify token limits are respected (approximately)
        for (String chunk : chunks) {
            int estimatedTokens = (int) Math.ceil(chunk.length() * 0.25);
            assertTrue(estimatedTokens <= 30); // Allow tolerance for overlap
        }
    }

    @Test
    void testModelEncodingMapping() {
        // Test different model encoding mappings
        TokenTextSplitter gpt4Splitter = new TokenTextSplitter(100, 20, "gpt-4");
        TokenTextSplitter gpt2Splitter = new TokenTextSplitter(100, 20, "gpt-2");
        TokenTextSplitter davinciSplitter = new TokenTextSplitter(100, 20, "text-davinci-003");
        
        // These should not throw exceptions
        assertDoesNotThrow(() -> gpt4Splitter.splitText(testText));
        assertDoesNotThrow(() -> gpt2Splitter.splitText(testText));
        assertDoesNotThrow(() -> davinciSplitter.splitText(testText));
    }

    @Test
    void testFactoryMethods() {
        // Test default factory
        TokenTextSplitter defaultSplitter = TokenTextSplitter.defaults();
        assertEquals(500, defaultSplitter.getChunkSize());
        assertEquals(50, defaultSplitter.getChunkOverlap());
        assertEquals("gpt-3.5-turbo", defaultSplitter.modelName);
        
        // Test GPT factory
        TokenTextSplitter gptSplitter = TokenTextSplitter.forGPT(200, 40);
        assertEquals(200, gptSplitter.getChunkSize());
        assertEquals(40, gptSplitter.getChunkOverlap());
        assertEquals("gpt-3.5-turbo", gptSplitter.modelName);
        
        // Test GPT-4 factory
        TokenTextSplitter gpt4Splitter = TokenTextSplitter.forGPT4(300, 60);
        assertEquals(300, gpt4Splitter.getChunkSize());
        assertEquals(60, gpt4Splitter.getChunkOverlap());
        assertEquals("gpt-4", gpt4Splitter.modelName);
    }

    @Test
    void testSplitterName() {
        assertEquals("TokenTextSplitter", splitter.getSplitterName());
    }

    @Test
    void testLangChainConfiguration() {
        LangChainConfig config = LangChainConfig.builder()
                .chunkSize(80)
                .chunkOverlap(16)
                .tokenConfig(LangChainConfig.TokenTextSplitterConfig.builder()
                    .modelName("gpt-4")
                    .encodingName("cl100k_base")
                    .build())
                .stripWhitespace(true)
                .build();
        
        TokenTextSplitter configuredSplitter = new TokenTextSplitter(config);
        
        assertEquals(80, configuredSplitter.getChunkSize());
        assertEquals(16, configuredSplitter.getChunkOverlap());
        
        // Should work properly with configuration
        List<String> chunks = configuredSplitter.splitText(testText);
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
    }

    @Test
    void testSpecialCharactersAndPunctuation() {
        String specialText = """
            Hello! How are you today? I'm doing well, thanks for asking.
            Here are some symbols: @#$%^&*()_+-=[]{}|;:,.<>?
            Numbers: 123456789 and words with apostrophes: don't, won't, I'll.
            """;
        
        List<String> chunks = splitter.splitText(specialText);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Should handle special characters without errors
        String rejoined = String.join("", chunks);
        assertTrue(rejoined.contains("@#$%"));
        assertTrue(rejoined.contains("don't"));
    }

    @Test
    void testModelCompatibilityScoring() {
        // Test with GPT models (should have high compatibility)
        double gptQuality = splitter.estimateQuality("This is a test for GPT model compatibility.");
        assertTrue(gptQuality > 0.5, "GPT models should have good compatibility score");
        
        // Test with different model types
        TokenTextSplitter otherModelSplitter = new TokenTextSplitter(100, 20, "other-model");
        double otherQuality = otherModelSplitter.estimateQuality("This is a test for other model compatibility.");
        assertTrue(otherQuality >= 0.0 && otherQuality <= 1.0);
    }
}