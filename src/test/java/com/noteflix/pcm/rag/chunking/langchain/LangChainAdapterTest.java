package com.noteflix.pcm.rag.chunking.langchain;

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
 * Tests for LangChainAdapter that bridges LangChain splitters with PCM chunking system.
 */
public class LangChainAdapterTest {

    private LangChainAdapter adapter;
    private RAGDocument testDocument;
    private String testContent;

    @BeforeEach
    void setUp() {
        testContent = """
            This is a comprehensive test document for the LangChain adapter.
            
            It contains multiple paragraphs to test how the adapter converts
            LangChain splitter results into PCM DocumentChunk objects.
            
            The adapter should preserve all metadata and add appropriate
            chunk-specific information while maintaining compatibility
            between the LangChain and PCM systems.
            """;

        testDocument = RAGDocument.builder()
                .id("test-document-123")
                .type(DocumentType.TEXT)
                .content(testContent)
                .title("Test Document")
                .author("Test Author")
                .build();
        
        testDocument.getMetadata().put("custom_field", "custom_value");
        testDocument.getMetadata().put("importance", "high");

        // Create adapter with character splitter
        CharacterTextSplitter splitter = new CharacterTextSplitter(300, 30, "\n\n");
        LangChainConfig config = LangChainConfig.defaults();
        adapter = new LangChainAdapter(splitter, config);
    }

    @Test
    void testBasicChunking() {
        List<DocumentChunk> chunks = adapter.chunk(testDocument);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify basic chunk properties
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            
            // Basic properties
            assertNotNull(chunk.getId());
            assertEquals(testDocument.getId(), chunk.getSourceDocumentId());
            assertEquals(testDocument.getType(), chunk.getDocumentType());
            assertFalse(chunk.getContent().trim().isEmpty());
            
            // Index and position
            assertEquals(i, chunk.getChunkIndex());
            assertEquals(chunks.size(), chunk.getTotalChunks());
            
            // Chunk size constraints
            assertTrue(chunk.getContent().length() <= 400); // Allow tolerance
        }
    }

    @Test
    void testMetadataPreservation() {
        List<DocumentChunk> chunks = adapter.chunk(testDocument);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        for (DocumentChunk chunk : chunks) {
            // Original document metadata should be preserved
            assertEquals("custom_value", chunk.getMetadata().get("custom_field"));
            assertEquals("high", chunk.getMetadata().get("importance"));
            assertEquals("Test Document", chunk.getMetadata().get("title"));
            assertEquals("Test Author", chunk.getMetadata().get("author"));
            
            // LangChain-specific metadata should be added
            assertNotNull(chunk.getMetadata().get("langchain_splitter"));
            assertNotNull(chunk.getMetadata().get("langchain_strategy"));
            
            // PCM-specific metadata should be present
            assertEquals("LangChainAdapter", chunk.getStrategyUsed());
            assertNotNull(chunk.getCreationTime());
            assertNotNull(chunk.getQualityScore());
        }
    }

    @Test
    void testDifferentSplitters() {
        // Test with Character Text Splitter
        CharacterTextSplitter charSplitter = new CharacterTextSplitter(200, 20, "\n\n");
        LangChainAdapter charAdapter = new LangChainAdapter(charSplitter, LangChainConfig.defaults());
        List<DocumentChunk> charChunks = charAdapter.chunk(testDocument);
        
        // Test with Recursive Character Text Splitter
        RecursiveCharacterTextSplitter recursiveSplitter = new RecursiveCharacterTextSplitter(200, 20);
        LangChainAdapter recursiveAdapter = new LangChainAdapter(recursiveSplitter, LangChainConfig.defaults());
        List<DocumentChunk> recursiveChunks = recursiveAdapter.chunk(testDocument);
        
        // Test with Token Text Splitter
        TokenTextSplitter tokenSplitter = new TokenTextSplitter(50, 10, "gpt-3.5-turbo");
        LangChainAdapter tokenAdapter = new LangChainAdapter(tokenSplitter, LangChainConfig.defaults());
        List<DocumentChunk> tokenChunks = tokenAdapter.chunk(testDocument);
        
        // All should produce valid chunks
        assertFalse(charChunks.isEmpty());
        assertFalse(recursiveChunks.isEmpty());
        assertFalse(tokenChunks.isEmpty());
        
        // Verify splitter-specific metadata
        assertEquals("CharacterTextSplitter", 
                charChunks.get(0).getMetadata().get("langchain_splitter"));
        assertEquals("RecursiveCharacterTextSplitter", 
                recursiveChunks.get(0).getMetadata().get("langchain_splitter"));
        assertEquals("TokenTextSplitter", 
                tokenChunks.get(0).getMetadata().get("langchain_splitter"));
    }

    @Test
    void testStrategyInterface() {
        // Test ChunkingStrategy interface methods
        assertEquals(300, adapter.getChunkSize());
        assertEquals(30, adapter.getOverlapSize());
        assertEquals("LangChainAdapter", adapter.getStrategyName());
        assertTrue(adapter.getDescription().contains("LangChain"));
        
        // Test suitability assessment
        assertTrue(adapter.isSuitableFor(testDocument));
        
        // Test with unsuitable document
        RAGDocument emptyDoc = RAGDocument.builder()
                .id("empty")
                .type(DocumentType.TEXT)
                .content("")
                .build();
        assertFalse(adapter.isSuitableFor(emptyDoc));
        
        // Test quality estimation
        double quality = adapter.estimateQuality(testDocument);
        assertTrue(quality >= 0.0);
        assertTrue(quality <= 1.0);
    }

    @Test
    void testEmptyAndNullContent() {
        // Test with empty content
        RAGDocument emptyDoc = RAGDocument.builder()
                .id("empty-doc")
                .type(DocumentType.TEXT)
                .content("")
                .build();
        
        List<DocumentChunk> emptyChunks = adapter.chunk(emptyDoc);
        assertTrue(emptyChunks.isEmpty());
        
        // Test with null content
        RAGDocument nullDoc = RAGDocument.builder()
                .id("null-doc")
                .type(DocumentType.TEXT)
                .content(null)
                .build();
        
        List<DocumentChunk> nullChunks = adapter.chunk(nullDoc);
        assertTrue(nullChunks.isEmpty());
    }

    @Test
    void testChunkQualityScoring() {
        List<DocumentChunk> chunks = adapter.chunk(testDocument);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        for (DocumentChunk chunk : chunks) {
            Double qualityScore = chunk.getQualityScore();
            assertNotNull(qualityScore, "All chunks should have quality scores");
            assertTrue(qualityScore >= 0.0, "Quality score should be non-negative");
            assertTrue(qualityScore <= 1.0, "Quality score should not exceed 1.0");
        }
        
        // Quality scores should be reasonable for good text
        double avgQuality = chunks.stream()
                .mapToDouble(chunk -> chunk.getQualityScore())
                .average()
                .orElse(0.0);
        assertTrue(avgQuality > 0.3, "Average quality should be reasonable for structured text");
    }

    @Test
    void testLangChainDocumentConversion() {
        // Create LangChain documents directly
        LangChainDocument langDoc1 = new LangChainDocument("First chunk content");
        langDoc1.addMetadata("chunk_type", "intro");
        langDoc1.addMetadata("priority", "high");
        
        LangChainDocument langDoc2 = new LangChainDocument("Second chunk content");
        langDoc2.addMetadata("chunk_type", "body");
        langDoc2.addMetadata("priority", "medium");
        
        List<LangChainDocument> langDocs = List.of(langDoc1, langDoc2);
        
        // Convert to DocumentChunks
        List<DocumentChunk> pcmChunks = adapter.convertToDocumentChunks(langDocs, testDocument);
        
        assertEquals(2, pcmChunks.size());
        
        // Verify conversion preserved metadata
        DocumentChunk chunk1 = pcmChunks.get(0);
        assertEquals("First chunk content", chunk1.getContent());
        assertEquals("intro", chunk1.getMetadata().get("chunk_type"));
        assertEquals("high", chunk1.getMetadata().get("priority"));
        
        DocumentChunk chunk2 = pcmChunks.get(1);
        assertEquals("Second chunk content", chunk2.getContent());
        assertEquals("body", chunk2.getMetadata().get("chunk_type"));
        assertEquals("medium", chunk2.getMetadata().get("priority"));
    }

    @Test
    void testChunkPositionalMetadata() {
        List<DocumentChunk> chunks = adapter.chunk(testDocument);
        
        assertNotNull(chunks);
        assertTrue(chunks.size() > 1, "Should have multiple chunks for position testing");
        
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            
            // Position metadata
            assertEquals(i, chunk.getChunkIndex());
            assertEquals(chunks.size(), chunk.getTotalChunks());
            
            // Start position should be tracked
            assertTrue(chunk.getStartPosition() >= 0);
            if (i > 0) {
                assertTrue(chunk.getStartPosition() >= chunks.get(i-1).getStartPosition());
            }
            
            // End position should be logical
            assertTrue(chunk.getEndPosition() > chunk.getStartPosition());
            assertTrue(chunk.getEndPosition() <= testContent.length());
        }
    }

    @Test
    void testConfigurationImpact() {
        // Test with different configurations
        LangChainConfig config1 = LangChainConfig.builder()
                .chunkSize(200)
                .chunkOverlap(20)
                .stripWhitespace(true)
                .build();
        
        LangChainConfig config2 = LangChainConfig.builder()
                .chunkSize(400)
                .chunkOverlap(40)
                .stripWhitespace(false)
                .build();
        
        CharacterTextSplitter splitter = new CharacterTextSplitter(200, 20, "\n\n");
        
        LangChainAdapter adapter1 = new LangChainAdapter(splitter, config1);
        LangChainAdapter adapter2 = new LangChainAdapter(splitter, config2);
        
        // Both should work but may produce different results
        List<DocumentChunk> chunks1 = adapter1.chunk(testDocument);
        List<DocumentChunk> chunks2 = adapter2.chunk(testDocument);
        
        assertFalse(chunks1.isEmpty());
        assertFalse(chunks2.isEmpty());
        
        // Configuration should affect chunk size reporting
        assertEquals(200, adapter1.getChunkSize());
        assertEquals(400, adapter2.getChunkSize());
    }

    @Test
    void testLargeDocumentHandling() {
        // Create a larger document
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            largeContent.append("This is paragraph number ").append(i)
                    .append(" with some content to make it reasonably sized. ");
            largeContent.append("It contains multiple sentences to test chunking behavior. ");
            largeContent.append("The content should be split appropriately by the LangChain splitter.\n\n");
        }
        
        RAGDocument largeDoc = RAGDocument.builder()
                .id("large-doc")
                .type(DocumentType.TEXT)
                .content(largeContent.toString())
                .title("Large Test Document")
                .build();
        
        List<DocumentChunk> chunks = adapter.chunk(largeDoc);
        
        assertNotNull(chunks);
        assertTrue(chunks.size() > 5, "Large document should create multiple chunks");
        
        // Verify all chunks are reasonable
        for (DocumentChunk chunk : chunks) {
            assertTrue(chunk.getContent().length() > 0);
            assertTrue(chunk.getContent().length() <= 400); // Reasonable upper bound
        }
        
        // Verify chunk ordering
        for (int i = 1; i < chunks.size(); i++) {
            assertTrue(chunks.get(i).getStartPosition() >= chunks.get(i-1).getStartPosition(),
                    "Chunks should maintain positional order");
        }
    }

    @Test
    void testErrorHandling() {
        // Test with problematic content
        String problematicContent = "\0\1\2\3\4\5"; // Control characters
        
        RAGDocument problematicDoc = RAGDocument.builder()
                .id("problematic-doc")
                .type(DocumentType.TEXT)
                .content(problematicContent)
                .build();
        
        // Should handle gracefully without throwing exceptions
        assertDoesNotThrow(() -> {
            List<DocumentChunk> chunks = adapter.chunk(problematicDoc);
            assertNotNull(chunks);
        });
    }
}