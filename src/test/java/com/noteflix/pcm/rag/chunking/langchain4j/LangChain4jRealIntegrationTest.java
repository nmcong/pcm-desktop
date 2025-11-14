package com.noteflix.pcm.rag.chunking.langchain4j;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.core.ChunkingFactory;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.DocumentType;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Integration test for real LangChain4j library usage with PCM chunking system.
 */
public class LangChain4jRealIntegrationTest {

    private RAGDocument testDocument;
    private String testContent;

    @BeforeEach
    void setUp() {
        testContent = """
            This is a test document for LangChain4j integration testing.
            
            It contains multiple paragraphs to verify that the real LangChain4j
            DocumentSplitter implementations work correctly with our PCM adapter.
            
            The document includes various structural elements like paragraphs,
            sentences, and different content types to ensure comprehensive testing.
            
            This paragraph tests the splitter's ability to handle longer content
            that may need to be split across multiple chunks while maintaining
            coherence and preserving important metadata throughout the process.
            """;

        testDocument = RAGDocument.builder()
                .id("test-langchain4j-doc")
                .type(DocumentType.TEXT)
                .content(testContent)
                .title("LangChain4j Integration Test")
                .author("Test Suite")
                .build();
        
        testDocument.getMetadata().put("test_field", "test_value");
    }

    @Test
    void testDirectLangChain4jParagraphSplitter() {
        // Test direct usage of LangChain4j DocumentByParagraphSplitter
        DocumentSplitter langchain4jSplitter = new DocumentByParagraphSplitter(300, 50);
        LangChain4jConfig config = LangChain4jConfig.defaults();
        LangChain4jAdapter adapter = new LangChain4jAdapter(langchain4jSplitter, config);

        List<DocumentChunk> chunks = adapter.chunk(testDocument);

        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify chunk properties
        for (DocumentChunk chunk : chunks) {
            assertNotNull(chunk.getId());
            assertEquals(testDocument.getId(), chunk.getSourceDocumentId());
            assertEquals(testDocument.getType(), chunk.getDocumentType());
            assertEquals("LangChain4jAdapter", chunk.getStrategyUsed());
            assertNotNull(chunk.getQualityScore());
            
            // Verify LangChain4j-specific metadata
            assertEquals("DocumentByParagraphSplitter", 
                chunk.getMetadata().get("langchain4j_splitter"));
            assertEquals("test_value", chunk.getMetadata().get("test_field"));
        }
        
        System.out.println("✅ Direct LangChain4j paragraph splitter test passed");
    }

    @Test
    void testDirectLangChain4jSentenceSplitter() {
        // Test direct usage of LangChain4j DocumentBySentenceSplitter
        DocumentSplitter langchain4jSplitter = new DocumentBySentenceSplitter(200, 30);
        LangChain4jConfig config = LangChain4jConfig.defaults();
        LangChain4jAdapter adapter = new LangChain4jAdapter(langchain4jSplitter, config);

        List<DocumentChunk> chunks = adapter.chunk(testDocument);

        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify sentence-specific behavior
        for (DocumentChunk chunk : chunks) {
            assertEquals("DocumentBySentenceSplitter", 
                chunk.getMetadata().get("langchain4j_splitter"));
            assertTrue(chunk.getContent().length() <= 250); // Allow some tolerance
        }
        
        System.out.println("✅ Direct LangChain4j sentence splitter test passed");
    }

    @Test
    void testLangChain4jHierarchicalSplitter() {
        // Test LangChain4j hierarchical splitter (DocumentSplitters.recursive)
        DocumentSplitter langchain4jSplitter = DocumentSplitters.recursive(400, 60);
        LangChain4jConfig config = LangChain4jConfig.defaults();
        LangChain4jAdapter adapter = new LangChain4jAdapter(langchain4jSplitter, config);

        List<DocumentChunk> chunks = adapter.chunk(testDocument);

        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Hierarchical splitter should produce well-structured chunks
        for (DocumentChunk chunk : chunks) {
            assertTrue(chunk.getContent().length() <= 500); // Allow tolerance
            assertFalse(chunk.getContent().trim().isEmpty());
        }
        
        System.out.println("✅ LangChain4j hierarchical splitter test passed");
    }

    @Test
    void testLangChain4jViaFactory() {
        // Test LangChain4j splitters through PCM ChunkingFactory
        ChunkingStrategy strategy = ChunkingFactory.createStrategy(
            ChunkingConfig.forLangChain4jParagraph()
        );

        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        assertTrue(strategy.getStrategyName().contains("LangChain4j"));
        
        System.out.println("✅ LangChain4j via PCM Factory test passed");
    }

    @Test
    void testLangChain4jFactoryConfiguration() {
        // Test factory-created splitters with custom configuration
        LangChain4jConfig config = LangChain4jConfig.builder()
                .splitterType(LangChain4jConfig.SplitterType.BY_PARAGRAPH)
                .maxSegmentSizeInChars(500)
                .maxOverlapSizeInChars(75)
                .build();

        DocumentSplitter splitter = LangChain4jSplitterFactory.createSplitter(config);
        LangChain4jAdapter adapter = new LangChain4jAdapter(splitter, config);

        List<DocumentChunk> chunks = adapter.chunk(testDocument);

        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify configuration is respected
        for (DocumentChunk chunk : chunks) {
            assertTrue(chunk.getContent().length() <= 600); // Allow tolerance
        }
        
        System.out.println("✅ LangChain4j factory configuration test passed");
    }

    @Test
    void testLangChain4jSplitterTypes() {
        // Test all supported LangChain4j splitter types
        LangChain4jConfig.SplitterType[] types = {
            LangChain4jConfig.SplitterType.BY_PARAGRAPH,
            LangChain4jConfig.SplitterType.BY_SENTENCE,
            LangChain4jConfig.SplitterType.BY_WORD,
            LangChain4jConfig.SplitterType.BY_LINE,
            LangChain4jConfig.SplitterType.BY_CHARACTER,
            LangChain4jConfig.SplitterType.HIERARCHICAL
        };

        for (LangChain4jConfig.SplitterType type : types) {
            LangChain4jConfig config = LangChain4jConfig.builder()
                    .splitterType(type)
                    .maxSegmentSizeInChars(300)
                    .maxOverlapSizeInChars(50)
                    .build();

            try {
                DocumentSplitter splitter = LangChain4jSplitterFactory.createSplitter(config);
                LangChain4jAdapter adapter = new LangChain4jAdapter(splitter, config);
                
                List<DocumentChunk> chunks = adapter.chunk(testDocument);
                
                assertNotNull(chunks, "Splitter type " + type + " should produce chunks");
                assertFalse(chunks.isEmpty(), "Splitter type " + type + " should produce non-empty chunks");
                
            } catch (Exception e) {
                fail("Splitter type " + type + " failed: " + e.getMessage());
            }
        }
        
        System.out.println("✅ All LangChain4j splitter types test passed");
    }

    @Test
    void testLangChain4jUseCases() {
        // Test LangChain4j-specific use cases
        ChunkingFactory.UseCase[] langchain4jUseCases = {
            ChunkingFactory.UseCase.LANGCHAIN4J_PARAGRAPH_FOCUSED,
            ChunkingFactory.UseCase.LANGCHAIN4J_SENTENCE_PRECISE,
            ChunkingFactory.UseCase.LANGCHAIN4J_HIERARCHICAL_SMART
        };

        for (ChunkingFactory.UseCase useCase : langchain4jUseCases) {
            ChunkingStrategy strategy = ChunkingFactory.createForUseCase(useCase, null);
            
            List<DocumentChunk> chunks = strategy.chunk(testDocument);
            
            assertNotNull(chunks, "Use case " + useCase + " should produce chunks");
            assertFalse(chunks.isEmpty(), "Use case " + useCase + " should produce non-empty chunks");
            assertTrue(strategy.getStrategyName().contains("LangChain4j"), 
                "Use case " + useCase + " should use LangChain4j strategy");
        }
        
        System.out.println("✅ LangChain4j use cases test passed");
    }

    @Test
    void testLangChain4jQualityAssessment() {
        // Test quality assessment for LangChain4j splitters
        DocumentSplitter splitter = new DocumentByParagraphSplitter(400, 60);
        LangChain4jConfig config = LangChain4jConfig.defaults();
        LangChain4jAdapter adapter = new LangChain4jAdapter(splitter, config);

        // Test suitability
        assertTrue(adapter.isSuitableFor(testDocument));
        
        // Test quality estimation
        double quality = adapter.estimateQuality(testDocument);
        assertTrue(quality >= 0.0 && quality <= 1.0);
        assertTrue(quality > 0.3); // Should be reasonable for structured text
        
        // Test empty document
        RAGDocument emptyDoc = RAGDocument.builder()
                .id("empty")
                .type(DocumentType.TEXT)
                .content("")
                .build();
        
        assertFalse(adapter.isSuitableFor(emptyDoc));
        assertEquals(0.0, adapter.estimateQuality(emptyDoc));
        
        System.out.println("✅ LangChain4j quality assessment test passed");
    }

    @Test
    void testLangChain4jMetadataPreservation() {
        // Test that metadata is properly preserved through LangChain4j processing
        testDocument.getMetadata().put("custom_field", "custom_value");
        testDocument.getMetadata().put("importance", "high");

        DocumentSplitter splitter = new DocumentByParagraphSplitter(300, 50);
        LangChain4jConfig config = LangChain4jConfig.defaults();
        LangChain4jAdapter adapter = new LangChain4jAdapter(splitter, config);

        List<DocumentChunk> chunks = adapter.chunk(testDocument);

        for (DocumentChunk chunk : chunks) {
            // Original metadata should be preserved
            assertEquals("custom_value", chunk.getMetadata().get("custom_field"));
            assertEquals("high", chunk.getMetadata().get("importance"));
            assertEquals("Test Suite", chunk.getMetadata().get("author"));
            assertEquals("LangChain4j Integration Test", chunk.getMetadata().get("title"));
            
            // LangChain4j metadata should be added
            assertNotNull(chunk.getMetadata().get("langchain4j_splitter"));
            assertNotNull(chunk.getMetadata().get("segment_index"));
            assertNotNull(chunk.getMetadata().get("segment_count"));
        }
        
        System.out.println("✅ LangChain4j metadata preservation test passed");
    }

    @Test
    void testLangChain4jRecommendations() {
        // Test that LangChain4j strategies are included in recommendations
        List<ChunkingFactory.StrategyRecommendation> recommendations = 
            ChunkingFactory.getAllRecommendations(testDocument, ChunkingConfig.defaults(), null);

        // Should have 15 total recommendations (4 PCM + 4 LangChain + 7 LangChain4j)
        assertEquals(15, recommendations.size());

        // Count LangChain4j strategies
        long langchain4jCount = recommendations.stream()
                .filter(rec -> rec.strategyType.name().startsWith("LANGCHAIN4J"))
                .count();
        
        assertEquals(7, langchain4jCount);

        // All LangChain4j strategies should have quality scores
        recommendations.stream()
                .filter(rec -> rec.strategyType.name().startsWith("LANGCHAIN4J"))
                .forEach(rec -> {
                    assertTrue(rec.expectedQuality >= 0.0 && rec.expectedQuality <= 1.0);
                    if (rec.strategy != null) {
                        assertTrue(rec.strategy.getStrategyName().contains("LangChain4j"));
                    }
                });
        
        System.out.println("✅ LangChain4j recommendations test passed");
    }
}