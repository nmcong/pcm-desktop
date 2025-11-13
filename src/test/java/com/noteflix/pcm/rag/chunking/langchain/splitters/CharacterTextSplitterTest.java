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
 * Tests for CharacterTextSplitter implementation.
 */
public class CharacterTextSplitterTest {

    private CharacterTextSplitter splitter;
    private String testText;

    @BeforeEach
    void setUp() {
        splitter = new CharacterTextSplitter(500, 50, "\n\n");
        testText = """
            First paragraph with some content.
            This continues the first paragraph.
            
            Second paragraph starts here.
            More content in the second paragraph.
            
            Third paragraph is shorter.
            
            Fourth and final paragraph with additional content to make it longer.
            This ensures we test chunking behavior properly.
            """;
    }

    @Test
    void testBasicTextSplitting() {
        List<String> chunks = splitter.splitText(testText);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Should split by paragraphs
        assertTrue(chunks.size() >= 2);
        
        // Each chunk should be reasonably sized
        for (String chunk : chunks) {
            assertFalse(chunk.trim().isEmpty());
            assertTrue(chunk.length() <= 600); // Allow some tolerance over target size
        }
    }

    @Test
    void testDocumentSplitting() {
        LangChainDocument doc = new LangChainDocument(testText);
        doc.addMetadata("title", "Test Document");
        doc.addMetadata("author", "Test Author");

        List<LangChainDocument> chunks = splitter.splitDocuments(List.of(doc));
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify metadata preservation
        for (LangChainDocument chunk : chunks) {
            assertEquals("Test Document", chunk.getMetadata().get("title"));
            assertEquals("Test Author", chunk.getMetadata().get("author"));
            assertEquals("CharacterTextSplitter", chunk.getMetadata().get("splitter_type"));
            assertEquals("\n\n", chunk.getMetadata().get("separator"));
            assertTrue(chunk.getMetadata().containsKey("chunk_index"));
            assertTrue(chunk.getMetadata().containsKey("chunk_count"));
        }
    }

    @Test
    void testEmptyAndNullInput() {
        assertTrue(splitter.splitText(null).isEmpty());
        assertTrue(splitter.splitText("").isEmpty());
        assertTrue(splitter.splitText("   ").isEmpty());
    }

    @Test
    void testShortTextNoSplitting() {
        String shortText = "This is a short text with no paragraph separators.";
        List<String> chunks = splitter.splitText(shortText);
        
        assertEquals(1, chunks.size());
        assertEquals(shortText, chunks.get(0));
    }

    @Test
    void testSeparatorKeeping() {
        LangChainConfig config = LangChainConfig.builder()
                .chunkSize(200)
                .chunkOverlap(20)
                .keepSeparator(true)
                .characterConfig(LangChainConfig.CharacterTextSplitterConfig.builder()
                    .separator("\n\n")
                    .build())
                .build();
        
        CharacterTextSplitter keepSeparatorSplitter = new CharacterTextSplitter(config);
        List<String> chunks = keepSeparatorSplitter.splitText(testText);
        
        // With separator keeping, we should see separators in the joined content
        String rejoined = String.join("", chunks);
        assertTrue(rejoined.contains("\n\n"));
    }

    @Test
    void testRegexSeparator() {
        CharacterTextSplitter regexSplitter = CharacterTextSplitter
                .withRegexSeparator(300, 30, "\\n\\n+"); // Multiple newlines
        
        List<String> chunks = regexSplitter.splitText(testText);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
    }

    @Test
    void testSuitabilityAssessment() {
        // Should be suitable for text with paragraph separators
        assertTrue(splitter.isSuitableFor(testText, DocumentType.TEXT));
        
        // Should not be suitable for very short text
        assertFalse(splitter.isSuitableFor("Short", DocumentType.TEXT));
        
        // Should not be suitable for text without separators
        String noSeparators = "This text has no paragraph separators just one long line.";
        assertFalse(splitter.isSuitableFor(noSeparators, DocumentType.TEXT));
    }

    @Test
    void testQualityEstimation() {
        double quality = splitter.estimateQuality(testText);
        
        assertTrue(quality >= 0.0);
        assertTrue(quality <= 1.0);
        
        // Text with consistent paragraph structure should have reasonable quality
        assertTrue(quality > 0.3);
        
        // Empty text should have zero quality
        assertEquals(0.0, splitter.estimateQuality(""));
        assertEquals(0.0, splitter.estimateQuality(null));
    }

    @Test
    void testConfiguration() {
        Map<String, Object> params = Map.of(
                "chunk_size", 300,
                "chunk_overlap", 30,
                "separator", "\n",
                "keep_separator", true
        );
        
        splitter.configure(params);
        
        assertEquals(300, splitter.getChunkSize());
        assertEquals(30, splitter.getChunkOverlap());
    }

    @Test
    void testFactoryMethods() {
        // Test paragraph splitter
        CharacterTextSplitter paragraphSplitter = CharacterTextSplitter.forParagraphs(400, 40);
        assertEquals(400, paragraphSplitter.getChunkSize());
        assertEquals(40, paragraphSplitter.getChunkOverlap());
        
        // Test line splitter
        CharacterTextSplitter lineSplitter = CharacterTextSplitter.forLines(600, 60);
        assertEquals(600, lineSplitter.getChunkSize());
        assertEquals(60, lineSplitter.getChunkOverlap());
        
        // Test custom separator
        CharacterTextSplitter customSplitter = CharacterTextSplitter.withSeparator(500, 50, "---");
        assertEquals(500, customSplitter.getChunkSize());
        assertEquals(50, customSplitter.getChunkOverlap());
    }

    @Test
    void testOverlapBehavior() {
        // Use small chunk size to force multiple chunks
        CharacterTextSplitter smallChunkSplitter = new CharacterTextSplitter(100, 20, "\n\n");
        
        List<String> chunks = smallChunkSplitter.splitText(testText);
        
        assertTrue(chunks.size() > 2); // Should create multiple chunks
        
        // There should be some content overlap between consecutive chunks
        // (This is a simplified check - in practice overlap detection is complex)
        assertTrue(chunks.stream().anyMatch(chunk -> chunk.length() > 50));
    }

    @Test
    void testSplitterName() {
        assertEquals("CharacterTextSplitter", splitter.getSplitterName());
    }

    @Test
    void testDifferentSeparators() {
        // Test with different separators
        CharacterTextSplitter dotSplitter = new CharacterTextSplitter(200, 20, ".");
        CharacterTextSplitter commaSplitter = new CharacterTextSplitter(150, 15, ",");
        
        String textWithDots = "Sentence one. Sentence two. Sentence three. Sentence four.";
        String textWithCommas = "Item one, item two, item three, item four, item five.";
        
        List<String> dotChunks = dotSplitter.splitText(textWithDots);
        List<String> commaChunks = commaSplitter.splitText(textWithCommas);
        
        assertFalse(dotChunks.isEmpty());
        assertFalse(commaChunks.isEmpty());
        
        // Should split appropriately by separator
        assertTrue(dotChunks.size() > 1);
        assertTrue(commaChunks.size() > 1);
    }
}