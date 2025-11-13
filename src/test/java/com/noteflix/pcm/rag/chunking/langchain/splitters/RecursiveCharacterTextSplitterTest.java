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
 * Tests for RecursiveCharacterTextSplitter implementation.
 */
public class RecursiveCharacterTextSplitterTest {

    private RecursiveCharacterTextSplitter splitter;
    private String testText;

    @BeforeEach
    void setUp() {
        splitter = new RecursiveCharacterTextSplitter(500, 50);
        testText = """
            This is the first paragraph with multiple sentences. It contains enough content to test chunking.
            
            This is the second paragraph. It also has multiple sentences for testing purposes.
            
            Here is a third paragraph that is quite short.
            
            The fourth paragraph is longer and contains more detailed information. This should help us test how the recursive splitter handles different text structures and maintains coherence across chunks.
            """;
    }

    @Test
    void testBasicRecursiveSplitting() {
        List<String> chunks = splitter.splitText(testText);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify chunks are within size limits
        for (String chunk : chunks) {
            assertFalse(chunk.trim().isEmpty());
            assertTrue(chunk.length() <= 600); // Allow tolerance
        }
    }

    @Test
    void testHierarchicalSeparators() {
        // Text designed to test separator hierarchy
        String hierarchicalText = """
            Section One
            
            Paragraph one with sentences. Multiple sentences here.
            
            Paragraph two with more content.
            
            Section Two
            
            Another paragraph with content. More sentences follow.
            """;
        
        List<String> chunks = splitter.splitText(hierarchicalText);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Should prefer paragraph breaks over line breaks
        boolean hasDoubleNewlines = chunks.stream()
                .anyMatch(chunk -> chunk.contains("\n\n"));
        assertTrue(hasDoubleNewlines, "Should preserve paragraph structure when possible");
    }

    @Test
    void testDocumentSplitting() {
        LangChainDocument doc = new LangChainDocument(testText);
        doc.addMetadata("source", "test_source");
        doc.addMetadata("category", "test_category");

        List<LangChainDocument> chunks = splitter.splitDocuments(List.of(doc));
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Verify metadata preservation and chunk metadata
        for (LangChainDocument chunk : chunks) {
            assertEquals("test_source", chunk.getMetadata().get("source"));
            assertEquals("test_category", chunk.getMetadata().get("category"));
            assertEquals("RecursiveCharacterTextSplitter", chunk.getMetadata().get("splitter_type"));
            assertTrue(chunk.getMetadata().containsKey("separators"));
            assertTrue(chunk.getMetadata().containsKey("chunk_index"));
            assertTrue(chunk.getMetadata().containsKey("chunk_count"));
        }
    }

    @Test
    void testCustomSeparators() {
        String[] customSeparators = {"\n### ", "\n## ", "\n\n", "\n", " ", ""};
        RecursiveCharacterTextSplitter customSplitter = 
                new RecursiveCharacterTextSplitter(300, 30, customSeparators);
        
        String markdownText = """
            ## Section 1
            Content for section 1.
            
            ### Subsection 1.1
            More detailed content here.
            
            ## Section 2
            Content for section 2.
            """;
        
        List<String> chunks = customSplitter.splitText(markdownText);
        
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Should respect markdown header structure
        boolean preservedHeaders = chunks.stream()
                .anyMatch(chunk -> chunk.contains("##"));
        assertTrue(preservedHeaders, "Should preserve markdown headers when possible");
    }

    @Test
    void testSuitabilityAssessment() {
        // Should be suitable for structured text
        assertTrue(splitter.isSuitableFor(testText, DocumentType.TEXT));
        
        // Should not be suitable for very short text
        assertFalse(splitter.isSuitableFor("Short", DocumentType.TEXT));
        
        // Should be suitable for text with hierarchical structure
        String structuredText = """
            Title
            
            Paragraph one.
            Paragraph two.
            
            Another section.
            """;
        assertTrue(splitter.isSuitableFor(structuredText, DocumentType.MARKDOWN));
        
        // Should handle text without clear structure
        String flatText = "This is just one long paragraph without any clear structure or separators to speak of.";
        assertFalse(splitter.isSuitableFor(flatText, DocumentType.TEXT));
    }

    @Test
    void testQualityEstimation() {
        double quality = splitter.estimateQuality(testText);
        
        assertTrue(quality >= 0.0);
        assertTrue(quality <= 1.0);
        
        // Text with good hierarchical structure should have high quality
        assertTrue(quality > 0.5, "Structured text should have good quality score");
        
        // Test with highly structured text
        String wellStructuredText = """
            ## Header 1
            
            Paragraph under header 1.
            Another paragraph.
            
            ## Header 2
            
            Content under header 2.
            More structured content.
            """;
        
        double structuredQuality = splitter.estimateQuality(wellStructuredText);
        assertTrue(structuredQuality > quality, "More structured text should have higher quality");
    }

    @Test
    void testConfiguration() {
        Map<String, Object> params = Map.of(
                "chunk_size", 400,
                "chunk_overlap", 40,
                "separators", new String[]{"\n\n", "\n", " "},
                "keep_separator", true,
                "is_separator_regex", false
        );
        
        splitter.configure(params);
        
        assertEquals(400, splitter.getChunkSize());
        assertEquals(40, splitter.getChunkOverlap());
    }

    @Test
    void testFactoryMethods() {
        // Test default factory method
        RecursiveCharacterTextSplitter defaultSplitter = 
                RecursiveCharacterTextSplitter.defaults(600, 60);
        assertEquals(600, defaultSplitter.getChunkSize());
        assertEquals(60, defaultSplitter.getChunkOverlap());
        
        // Test markdown factory
        RecursiveCharacterTextSplitter markdownSplitter = 
                RecursiveCharacterTextSplitter.forMarkdown(500, 50);
        List<String> markdownChunks = markdownSplitter.splitText("""
            ## Header 1
            Content here.
            
            ### Subheader
            More content.
            """);
        assertFalse(markdownChunks.isEmpty());
        
        // Test code factory
        RecursiveCharacterTextSplitter codeSplitter = 
                RecursiveCharacterTextSplitter.forCode(400, 40);
        List<String> codeChunks = codeSplitter.splitText("""
            class TestClass:
                def method1(self):
                    pass
            
            def function1():
                return True
            """);
        assertFalse(codeChunks.isEmpty());
    }

    @Test
    void testEmptyAndNullInput() {
        assertTrue(splitter.splitText(null).isEmpty());
        assertTrue(splitter.splitText("").isEmpty());
        assertTrue(splitter.splitText("   ").isEmpty());
        
        assertEquals(0.0, splitter.estimateQuality(null));
        assertEquals(0.0, splitter.estimateQuality(""));
    }

    @Test
    void testVeryLongSingleParagraph() {
        // Test with content that needs character-level splitting
        StringBuilder longParagraph = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longParagraph.append("This is a very long sentence that keeps going and going. ");
        }
        
        List<String> chunks = splitter.splitText(longParagraph.toString());
        
        assertFalse(chunks.isEmpty());
        
        // Should eventually fall back to character-level splitting for oversized content
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= 600); // Within reasonable limits
        }
    }

    @Test
    void testOverlapBehavior() {
        // Use smaller chunk size to force overlap
        RecursiveCharacterTextSplitter overlapSplitter = 
                new RecursiveCharacterTextSplitter(200, 50);
        
        List<String> chunks = overlapSplitter.splitText(testText);
        
        assertTrue(chunks.size() >= 2, "Should create multiple chunks for testing overlap");
        
        // With proper overlap, consecutive chunks should share some content
        // This is a complex behavior to test precisely, so we check basic properties
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= 300); // Allow tolerance for overlap
        }
    }

    @Test
    void testSplitterName() {
        assertEquals("RecursiveCharacterTextSplitter", splitter.getSplitterName());
    }

    @Test
    void testCodeSplitting() {
        RecursiveCharacterTextSplitter codeSplitter = 
                RecursiveCharacterTextSplitter.forCode(300, 30);
        
        String codeText = """
            public class Example {
                private String field;
            
                public void method1() {
                    System.out.println("Hello");
                }
            
                public void method2() {
                    System.out.println("World");
                }
            }
            
            class Another {
                public void anotherMethod() {
                    // Some code here
                }
            }
            """;
        
        List<String> codeChunks = codeSplitter.splitText(codeText);
        
        assertNotNull(codeChunks);
        assertFalse(codeChunks.isEmpty());
        
        // Should try to preserve class/method boundaries
        boolean preservedStructure = codeChunks.stream()
                .anyMatch(chunk -> chunk.contains("class ") || chunk.contains("def "));
        assertTrue(preservedStructure, "Should attempt to preserve code structure");
    }

    @Test
    void testMarkdownSplitting() {
        RecursiveCharacterTextSplitter markdownSplitter = 
                RecursiveCharacterTextSplitter.forMarkdown(400, 40);
        
        String markdownText = """
            # Main Title
            
            Introduction paragraph.
            
            ## Section 1
            
            Content for section 1 with multiple sentences.
            
            ### Subsection 1.1
            
            Detailed content here.
            
            ## Section 2
            
            More content for the second section.
            """;
        
        List<String> markdownChunks = markdownSplitter.splitText(markdownText);
        
        assertNotNull(markdownChunks);
        assertFalse(markdownChunks.isEmpty());
        
        // Should preserve header hierarchy when possible
        boolean preservedHeaders = markdownChunks.stream()
                .anyMatch(chunk -> chunk.contains("##"));
        assertTrue(preservedHeaders, "Should preserve markdown headers");
    }
}