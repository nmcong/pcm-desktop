package com.noteflix.pcm.rag.chunking.langchain;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.core.ChunkingFactory;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.chunking.langchain.splitters.CharacterTextSplitter;
import com.noteflix.pcm.rag.chunking.langchain.splitters.RecursiveCharacterTextSplitter;
import com.noteflix.pcm.rag.chunking.langchain.splitters.TokenTextSplitter;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Integration tests for LangChain text splitters within PCM system.
 */
public class LangChainIntegrationTest {

    private String testText;
    private RAGDocument testDocument;

    @BeforeEach
    void setUp() {
        testText = """
            This is a test document for LangChain integration.
            
            It has multiple paragraphs separated by double newlines.
            Each paragraph contains multiple sentences. The sentences are separated by periods.
            
            The document also has some code blocks:
            ```java
            public class TestClass {
                public void testMethod() {
                    System.out.println("Hello World");
                }
            }
            ```
            
            And some lists:
            - Item 1
            - Item 2  
            - Item 3
            
            This ensures we can test various splitting strategies effectively.
            """;

        testDocument = RAGDocument.builder()
                .id("test-doc")
                .type(DocumentType.TEXT)
                .content(testText)
                .title("Test Document")
                .build();
    }

    @Test
    void testLangChainCharacterSplitterIntegration() {
        // Create LangChain character splitter through PCM factory
        ChunkingConfig config = ChunkingConfig.forLangChainCharacter();
        ChunkingStrategy strategy = ChunkingFactory.createStrategy(config);

        // Test chunking
        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        // Verify results
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify chunk metadata contains LangChain information
        DocumentChunk firstChunk = chunks.get(0);
        assertEquals("LangChainAdapter", firstChunk.getStrategyUsed());
        assertNotNull(firstChunk.getMetadata().get("langchain_splitter"));
        
        // Verify chunk size constraints
        for (DocumentChunk chunk : chunks) {
            assertTrue(chunk.getContent().length() <= config.getTargetChunkSize() + 100); // Allow some tolerance
            assertFalse(chunk.getContent().trim().isEmpty());
        }
    }

    @Test
    void testLangChainRecursiveSplitterIntegration() {
        // Create LangChain recursive splitter through PCM factory
        ChunkingConfig config = ChunkingConfig.forLangChainRecursive();
        ChunkingStrategy strategy = ChunkingFactory.createStrategy(config);

        // Test chunking
        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        // Verify results
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify hierarchical splitting preserved structure better than simple splitting
        boolean hasLargeParagraphs = chunks.stream()
                .anyMatch(chunk -> chunk.getContent().contains("\n\n"));
        assertTrue(hasLargeParagraphs, "Recursive splitter should preserve paragraph structure when possible");
        
        // Verify chunk quality
        for (DocumentChunk chunk : chunks) {
            assertNotNull(chunk.getQualityScore());
            assertTrue(chunk.getQualityScore() >= 0.0);
            assertTrue(chunk.getQualityScore() <= 1.0);
        }
    }

    @Test
    void testLangChainTokenSplitterIntegration() {
        // Create LangChain token splitter through PCM factory
        ChunkingConfig config = ChunkingConfig.forLangChainToken("gpt-3.5-turbo");
        ChunkingStrategy strategy = ChunkingFactory.createStrategy(config);

        // Test chunking
        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        // Verify results
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify token-specific metadata
        DocumentChunk firstChunk = chunks.get(0);
        assertNotNull(firstChunk.getMetadata().get("estimated_tokens"));
        assertNotNull(firstChunk.getMetadata().get("model_name"));
        assertEquals("gpt-3.5-turbo", firstChunk.getMetadata().get("model_name"));
        
        // Verify token constraints (smaller chunks for token-based splitting)
        for (DocumentChunk chunk : chunks) {
            Integer estimatedTokens = (Integer) chunk.getMetadata().get("estimated_tokens");
            assertNotNull(estimatedTokens);
            assertTrue(estimatedTokens <= config.getTargetChunkSize() + 50); // Token-based, allow tolerance
        }
    }

    @Test
    void testLangChainCodeSplitterIntegration() {
        // Create LangChain code splitter through PCM factory
        ChunkingConfig config = ChunkingConfig.forLangChainCode("java");
        ChunkingStrategy strategy = ChunkingFactory.createStrategy(config);

        // Test chunking
        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        // Verify results
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Code splitter should handle code blocks well
        boolean preservedCodeBlock = chunks.stream()
                .anyMatch(chunk -> chunk.getContent().contains("public class TestClass"));
        assertTrue(preservedCodeBlock, "Code splitter should preserve code block structure");
    }

    @Test
    void testLangChainAdapterMetadataPreservation() {
        ChunkingConfig config = ChunkingConfig.forLangChainRecursive();
        ChunkingStrategy strategy = ChunkingFactory.createStrategy(config);

        // Add custom metadata to document
        testDocument.getMetadata().put("custom_field", "test_value");
        testDocument.getMetadata().put("author", "test_author");

        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        // Verify original metadata is preserved
        for (DocumentChunk chunk : chunks) {
            assertEquals("test_value", chunk.getMetadata().get("custom_field"));
            assertEquals("test_author", chunk.getMetadata().get("author"));
            assertEquals(testDocument.getId(), chunk.getSourceDocumentId());
            assertEquals(testDocument.getType(), chunk.getDocumentType());
        }
    }

    @Test
    void testLangChainStrategySelection() {
        // Test automatic strategy selection includes LangChain strategies
        ChunkingConfig config = ChunkingConfig.builder()
                .autoSelectStrategy(true)
                .build();

        ChunkingFactory.StrategyRecommendation recommendation = 
                ChunkingFactory.getAllRecommendations(testDocument, config, null)
                .stream()
                .filter(r -> r.strategyType.name().startsWith("LANGCHAIN"))
                .findFirst()
                .orElse(null);

        assertNotNull(recommendation, "LangChain strategies should be included in recommendations");
        assertTrue(recommendation.expectedQuality > 0.0);
    }

    @Test
    void testLangChainFallbackMechanism() {
        // Create config with LangChain primary and PCM fallback
        ChunkingConfig config = ChunkingConfig.builder()
                .primaryStrategy(ChunkingConfig.ChunkingStrategyType.LANGCHAIN_RECURSIVE)
                .fallbackStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE)
                .enableQualityFallback(true)
                .minQualityThreshold(0.8) // High threshold to trigger fallback
                .build();

        // Create strategy with fallback
        ChunkingStrategy strategy = ChunkingFactory.createWithFallback(config, null);

        // Test chunking - should work even if primary fails or quality is low
        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
    }

    @Test
    void testLangChainUseCases() {
        // Test LangChain-specific use cases
        ChunkingStrategy langchainCompatible = ChunkingFactory.createForUseCase(
                ChunkingFactory.UseCase.LANGCHAIN_COMPATIBLE, null);
        ChunkingStrategy tokenOptimized = ChunkingFactory.createForUseCase(
                ChunkingFactory.UseCase.TOKEN_OPTIMIZED, null);
        ChunkingStrategy codeDocuments = ChunkingFactory.createForUseCase(
                ChunkingFactory.UseCase.CODE_DOCUMENTS, null);

        // Test all strategies can chunk the document
        assertDoesNotThrow(() -> {
            langchainCompatible.chunk(testDocument);
            tokenOptimized.chunk(testDocument);
            codeDocuments.chunk(testDocument);
        });

        // Verify strategy names
        assertTrue(langchainCompatible.getStrategyName().contains("LangChain"));
        assertTrue(tokenOptimized.getStrategyName().contains("LangChain"));
        assertTrue(codeDocuments.getStrategyName().contains("LangChain"));
    }

    @Test
    void testLangChainConfigurationValidation() {
        // Test that LangChain configurations are properly validated
        LangChainConfig langchainConfig = LangChainConfig.builder()
                .chunkSize(1000)
                .chunkOverlap(200)
                .build();

        assertDoesNotThrow(() -> langchainConfig.validate());

        // Test invalid configuration
        LangChainConfig invalidConfig = LangChainConfig.builder()
                .chunkSize(-100) // Invalid
                .chunkOverlap(200)
                .build();

        assertThrows(IllegalArgumentException.class, () -> invalidConfig.validate());
    }

    @Test
    void testLangChainSplitterDirectUsage() {
        // Test LangChain splitters can be used directly (not just through adapter)
        CharacterTextSplitter charSplitter = CharacterTextSplitter.forParagraphs(500, 50);
        RecursiveCharacterTextSplitter recursiveSplitter = RecursiveCharacterTextSplitter.defaults(500, 50);
        TokenTextSplitter tokenSplitter = TokenTextSplitter.forGPT(300, 30);

        // Test direct splitting
        List<String> charChunks = charSplitter.splitText(testText);
        List<String> recursiveChunks = recursiveSplitter.splitText(testText);
        List<String> tokenChunks = tokenSplitter.splitText(testText);

        // Verify all splitters produce reasonable results
        assertFalse(charChunks.isEmpty());
        assertFalse(recursiveChunks.isEmpty());
        assertFalse(tokenChunks.isEmpty());

        // Verify suitability assessment
        assertTrue(charSplitter.isSuitableFor(testText, DocumentType.TEXT));
        assertTrue(recursiveSplitter.isSuitableFor(testText, DocumentType.TEXT));
        assertTrue(tokenSplitter.isSuitableFor(testText, DocumentType.TEXT));

        // Verify quality estimation
        double charQuality = charSplitter.estimateQuality(testText);
        double recursiveQuality = recursiveSplitter.estimateQuality(testText);
        double tokenQuality = tokenSplitter.estimateQuality(testText);

        assertTrue(charQuality > 0.0 && charQuality <= 1.0);
        assertTrue(recursiveQuality > 0.0 && recursiveQuality <= 1.0);
        assertTrue(tokenQuality > 0.0 && tokenQuality <= 1.0);
    }

    @Test
    void testHybridSystemIntegration() {
        // Test that PCM and LangChain strategies can work together seamlessly
        
        // Get all recommendations including both PCM and LangChain strategies
        List<ChunkingFactory.StrategyRecommendation> recommendations = 
                ChunkingFactory.getAllRecommendations(testDocument, ChunkingConfig.defaults(), null);

        // Should have 8 recommendations total (4 PCM + 4 LangChain)
        assertEquals(8, recommendations.size());

        // Verify we have both types
        long pcmStrategies = recommendations.stream()
                .filter(r -> !r.strategyType.name().startsWith("LANGCHAIN"))
                .count();
        long langchainStrategies = recommendations.stream()
                .filter(r -> r.strategyType.name().startsWith("LANGCHAIN"))
                .count();

        assertEquals(4, pcmStrategies);
        assertEquals(4, langchainStrategies);

        // Test that all strategies can actually chunk the document
        for (ChunkingFactory.StrategyRecommendation rec : recommendations) {
            if (rec.strategy != null) {
                List<DocumentChunk> chunks = rec.strategy.chunk(testDocument);
                assertFalse(chunks.isEmpty(), 
                        "Strategy " + rec.strategyType + " should produce chunks");
            }
        }
    }
}