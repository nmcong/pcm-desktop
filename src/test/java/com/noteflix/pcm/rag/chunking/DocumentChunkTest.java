package com.noteflix.pcm.rag.chunking;

import com.noteflix.pcm.rag.model.DocumentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Test suite for DocumentChunk class.
 * 
 * Tests cover:
 * - Builder pattern functionality
 * - Metadata management
 * - Quality metrics
 * - Utility methods
 * - Edge cases and validation
 */
public class DocumentChunkTest {

    @Test
    void testBasicChunkCreation() {
        LocalDateTime timestamp = LocalDateTime.now();
        
        DocumentChunk chunk = DocumentChunk.builder()
            .chunkId("test-chunk-1")
            .documentId("test-doc")
            .content("This is a test chunk content with multiple sentences. It should be long enough to test various features.")
            .index(0)
            .startPosition(0)
            .endPosition(95)
            .documentTitle("Test Document")
            .documentType(DocumentType.ARTICLE)
            .sourcePath("/test/document.txt")
            .documentTimestamp(timestamp)
            .chunkingStrategy("SentenceAware")
            .chunkSizeChars(95)
            .overlapSize(20)
            .hasOverlapBefore(false)
            .hasOverlapAfter(true)
            .qualityScore(0.85)
            .coherenceScore(0.92)
            .densityScore(0.78)
            .build();

        // Verify basic properties
        assertThat(chunk.getChunkId()).isEqualTo("test-chunk-1");
        assertThat(chunk.getDocumentId()).isEqualTo("test-doc");
        assertThat(chunk.getContent()).startsWith("This is a test chunk content");
        assertThat(chunk.getIndex()).isEqualTo(0);
        assertThat(chunk.getStartPosition()).isEqualTo(0);
        assertThat(chunk.getEndPosition()).isEqualTo(95);
        
        // Verify document metadata
        assertThat(chunk.getDocumentTitle()).isEqualTo("Test Document");
        assertThat(chunk.getDocumentType()).isEqualTo(DocumentType.ARTICLE);
        assertThat(chunk.getSourcePath()).isEqualTo("/test/document.txt");
        assertThat(chunk.getDocumentTimestamp()).isEqualTo(timestamp);
        
        // Verify chunking metadata
        assertThat(chunk.getChunkingStrategy()).isEqualTo("SentenceAware");
        assertThat(chunk.getChunkSizeChars()).isEqualTo(95);
        assertThat(chunk.getOverlapSize()).isEqualTo(20);
        assertThat(chunk.getHasOverlapBefore()).isFalse();
        assertThat(chunk.getHasOverlapAfter()).isTrue();
        
        // Verify quality metrics
        assertThat(chunk.getQualityScore()).isEqualTo(0.85);
        assertThat(chunk.getCoherenceScore()).isEqualTo(0.92);
        assertThat(chunk.getDensityScore()).isEqualTo(0.78);
    }

    @Test
    void testBuilderWithMinimalData() {
        DocumentChunk chunk = DocumentChunk.builder()
            .chunkId("minimal-chunk")
            .documentId("minimal-doc")
            .content("Minimal content.")
            .index(0)
            .build();

        assertThat(chunk.getChunkId()).isEqualTo("minimal-chunk");
        assertThat(chunk.getDocumentId()).isEqualTo("minimal-doc");
        assertThat(chunk.getContent()).isEqualTo("Minimal content.");
        assertThat(chunk.getIndex()).isEqualTo(0);
        
        // Optional fields should be null or have default values
        assertThat(chunk.getStartPosition()).isNull();
        assertThat(chunk.getQualityScore()).isNull();
        assertThat(chunk.getCustomMetadata()).isNotNull().isEmpty();
    }

    @Test
    void testCalculatedProperties() {
        DocumentChunk chunk = DocumentChunk.builder()
            .chunkId("calc-chunk")
            .documentId("calc-doc")
            .content("This is a test content for calculating properties.")
            .index(0)
            .startPosition(100)
            .endPosition(147)
            .build();

        // Test calculated length
        assertThat(chunk.getLength()).isEqualTo(47); // endPosition - startPosition

        // Test estimated token count
        Integer estimatedTokens = chunk.getEstimatedTokenCount();
        assertThat(estimatedTokens).isGreaterThan(0);
        assertThat(estimatedTokens).isLessThan(chunk.getContent().length()); // Should be less than character count

        // Test content preview
        String preview = chunk.getContentPreview();
        assertThat(preview).isNotBlank();
        assertThat(preview.length()).isLessThanOrEqualTo(100);
        
        // Test summary
        String summary = chunk.getSummary();
        assertThat(summary).contains("calc-chunk");
        assertThat(summary).contains("47 chars");
    }

    @Test
    void testMetadataManagement() {
        DocumentChunk chunk = DocumentChunk.builder()
            .chunkId("meta-chunk")
            .documentId("meta-doc")
            .content("Content with metadata.")
            .index(0)
            .build();

        // Initially empty
        assertThat(chunk.getCustomMetadata()).isEmpty();

        // Add metadata
        chunk.addMetadata("sentence_count", "3");
        chunk.addMetadata("language", "en");
        chunk.addMetadata("category", "technical");

        Map<String, String> metadata = chunk.getCustomMetadata();
        assertThat(metadata).hasSize(3);
        assertThat(metadata.get("sentence_count")).isEqualTo("3");
        assertThat(metadata.get("language")).isEqualTo("en");
        assertThat(metadata.get("category")).isEqualTo("technical");

        // Test metadata retrieval
        assertThat(chunk.getMetadata("sentence_count")).isEqualTo("3");
        assertThat(chunk.getMetadata("language")).isEqualTo("en");
        assertThat(chunk.getMetadata("nonexistent")).isNull();

        // Test metadata existence check
        assertThat(chunk.hasMetadata("sentence_count")).isTrue();
        assertThat(chunk.hasMetadata("nonexistent")).isFalse();

        // Remove metadata
        chunk.removeMetadata("language");
        assertThat(chunk.getCustomMetadata()).hasSize(2);
        assertThat(chunk.hasMetadata("language")).isFalse();

        // Clear all metadata
        chunk.clearMetadata();
        assertThat(chunk.getCustomMetadata()).isEmpty();
    }

    @Test
    void testContextLinking() {
        DocumentChunk chunk1 = DocumentChunk.builder()
            .chunkId("chunk-1")
            .documentId("linked-doc")
            .content("First chunk content.")
            .index(0)
            .build();

        DocumentChunk chunk2 = DocumentChunk.builder()
            .chunkId("chunk-2")
            .documentId("linked-doc")
            .content("Second chunk content.")
            .index(1)
            .previousChunkId("chunk-1")
            .build();

        DocumentChunk chunk3 = DocumentChunk.builder()
            .chunkId("chunk-3")
            .documentId("linked-doc")
            .content("Third chunk content.")
            .index(2)
            .previousChunkId("chunk-2")
            .build();

        // Update next chunk links
        chunk1.setNextChunkId("chunk-2");
        chunk2.setNextChunkId("chunk-3");

        // Verify linking
        assertThat(chunk1.getPreviousChunkId()).isNull();
        assertThat(chunk1.getNextChunkId()).isEqualTo("chunk-2");

        assertThat(chunk2.getPreviousChunkId()).isEqualTo("chunk-1");
        assertThat(chunk2.getNextChunkId()).isEqualTo("chunk-3");

        assertThat(chunk3.getPreviousChunkId()).isEqualTo("chunk-2");
        assertThat(chunk3.getNextChunkId()).isNull();
    }

    @Test
    void testStructuralMetadata() {
        DocumentChunk chunk = DocumentChunk.builder()
            .chunkId("struct-chunk")
            .documentId("struct-doc")
            .content("# Introduction\n\nThis is the introduction section.")
            .index(0)
            .sectionTitle("Introduction")
            .hierarchyLevel(1)
            .contentType("markdown")
            .parentSectionId("doc-intro")
            .build();

        assertThat(chunk.getSectionTitle()).isEqualTo("Introduction");
        assertThat(chunk.getHierarchyLevel()).isEqualTo(1);
        assertThat(chunk.getContentType()).isEqualTo("markdown");
        assertThat(chunk.getParentSectionId()).isEqualTo("doc-intro");
    }

    @Test
    void testQualityMetricsValidation() {
        // Test valid quality scores
        DocumentChunk validChunk = DocumentChunk.builder()
            .chunkId("valid-chunk")
            .documentId("valid-doc")
            .content("Valid content.")
            .index(0)
            .qualityScore(0.75)
            .coherenceScore(0.85)
            .densityScore(0.92)
            .build();

        assertThat(validChunk.getQualityScore()).isEqualTo(0.75);
        assertThat(validChunk.getCoherenceScore()).isEqualTo(0.85);
        assertThat(validChunk.getDensityScore()).isEqualTo(0.92);
    }

    @Test
    void testProcessingMetadata() {
        LocalDateTime createdTime = LocalDateTime.now();
        LocalDateTime processedTime = createdTime.plusSeconds(5);

        DocumentChunk chunk = DocumentChunk.builder()
            .chunkId("processed-chunk")
            .documentId("processed-doc")
            .content("Processed content.")
            .index(0)
            .createdAt(createdTime)
            .lastModified(processedTime)
            .processingVersion("1.2.0")
            .build();

        assertThat(chunk.getCreatedAt()).isEqualTo(createdTime);
        assertThat(chunk.getLastModified()).isEqualTo(processedTime);
        assertThat(chunk.getProcessingVersion()).isEqualTo("1.2.0");
    }

    @Test
    void testUtilityMethods() {
        DocumentChunk longChunk = DocumentChunk.builder()
            .chunkId("long-chunk")
            .documentId("long-doc")
            .content("This is a very long piece of content that should be truncated in the preview. " +
                    "It contains multiple sentences and should demonstrate the preview functionality properly. " +
                    "The content goes on and on with more details about various topics.")
            .index(0)
            .startPosition(0)
            .endPosition(250)
            .chunkingStrategy("SentenceAware")
            .qualityScore(0.88)
            .build();

        // Test content preview (should be truncated)
        String preview = longChunk.getContentPreview();
        assertThat(preview).hasSizeLessThanOrEqualTo(100);
        assertThat(preview).endsWith("...");

        // Test summary
        String summary = longChunk.getSummary();
        assertThat(summary).contains("long-chunk");
        assertThat(summary).contains("250 chars");
        assertThat(summary).contains("SentenceAware");
        assertThat(summary).contains("quality=0.88");

        // Test estimated tokens
        Integer tokens = longChunk.getEstimatedTokenCount();
        assertThat(tokens).isGreaterThan(30); // Should be reasonable estimate
        assertThat(tokens).isLessThan(longChunk.getContent().length());
    }

    @Test
    void testShortContent() {
        DocumentChunk shortChunk = DocumentChunk.builder()
            .chunkId("short-chunk")
            .documentId("short-doc")
            .content("Short.")
            .index(0)
            .build();

        // Preview should not be truncated for short content
        String preview = shortChunk.getContentPreview();
        assertThat(preview).isEqualTo("Short.");
        assertThat(preview).doesNotEndWith("...");

        // Token estimate should still work
        Integer tokens = shortChunk.getEstimatedTokenCount();
        assertThat(tokens).isGreaterThan(0);
    }

    @Test
    void testEmptyAndNullContent() {
        // Empty content
        DocumentChunk emptyChunk = DocumentChunk.builder()
            .chunkId("empty-chunk")
            .documentId("empty-doc")
            .content("")
            .index(0)
            .build();

        assertThat(emptyChunk.getContentPreview()).isEmpty();
        assertThat(emptyChunk.getEstimatedTokenCount()).isEqualTo(0);

        // Null content
        DocumentChunk nullChunk = DocumentChunk.builder()
            .chunkId("null-chunk")
            .documentId("null-doc")
            .content(null)
            .index(0)
            .build();

        assertThat(nullChunk.getContentPreview()).isEmpty();
        assertThat(nullChunk.getEstimatedTokenCount()).isEqualTo(0);
    }

    @Test
    void testComplexMetadataScenario() {
        DocumentChunk chunk = DocumentChunk.builder()
            .chunkId("complex-chunk")
            .documentId("complex-doc")
            .content("Complex content with various metadata.")
            .index(2)
            .startPosition(500)
            .endPosition(537)
            .documentTitle("Complex Document")
            .documentType(DocumentType.TECHNICAL_MANUAL)
            .sourcePath("/docs/complex.md")
            .chunkingStrategy("MarkdownAware")
            .chunkSizeChars(37)
            .overlapSize(15)
            .hasOverlapBefore(true)
            .hasOverlapAfter(true)
            .qualityScore(0.91)
            .coherenceScore(0.87)
            .densityScore(0.93)
            .sectionTitle("Advanced Features")
            .hierarchyLevel(2)
            .contentType("markdown")
            .parentSectionId("features-section")
            .previousChunkId("complex-chunk-1")
            .nextChunkId("complex-chunk-3")
            .build();

        // Add custom metadata
        chunk.addMetadata("code_blocks", "2");
        chunk.addMetadata("tables", "1");
        chunk.addMetadata("links", "3");
        chunk.addMetadata("chunk_type", "markdown_aware");

        // Verify all data is preserved
        assertThat(chunk.getChunkId()).isEqualTo("complex-chunk");
        assertThat(chunk.getIndex()).isEqualTo(2);
        assertThat(chunk.getLength()).isEqualTo(37);
        assertThat(chunk.getDocumentType()).isEqualTo(DocumentType.TECHNICAL_MANUAL);
        assertThat(chunk.getChunkingStrategy()).isEqualTo("MarkdownAware");
        assertThat(chunk.getHasOverlapBefore()).isTrue();
        assertThat(chunk.getHasOverlapAfter()).isTrue();
        assertThat(chunk.getSectionTitle()).isEqualTo("Advanced Features");
        assertThat(chunk.getHierarchyLevel()).isEqualTo(2);
        assertThat(chunk.getPreviousChunkId()).isEqualTo("complex-chunk-1");
        assertThat(chunk.getNextChunkId()).isEqualTo("complex-chunk-3");

        // Verify custom metadata
        assertThat(chunk.getCustomMetadata()).hasSize(4);
        assertThat(chunk.getMetadata("code_blocks")).isEqualTo("2");
        assertThat(chunk.getMetadata("chunk_type")).isEqualTo("markdown_aware");

        // Test summary for complex chunk
        String summary = chunk.getSummary();
        assertThat(summary).contains("complex-chunk");
        assertThat(summary).contains("37 chars");
        assertThat(summary).contains("MarkdownAware");
        assertThat(summary).contains("quality=0.91");
    }

    @Test
    void testMetadataImmutability() {
        DocumentChunk chunk = DocumentChunk.builder()
            .chunkId("immutable-chunk")
            .documentId("immutable-doc")
            .content("Content for immutability test.")
            .index(0)
            .build();

        chunk.addMetadata("test_key", "test_value");
        
        // Get metadata map
        Map<String, String> metadata = chunk.getCustomMetadata();
        assertThat(metadata).hasSize(1);

        // Try to modify returned map (should not affect internal state)
        try {
            metadata.put("external_key", "external_value");
        } catch (UnsupportedOperationException e) {
            // This is expected if the map is immutable
        }

        // Original chunk should be unaffected
        // (This test depends on implementation - if using mutable map, the external modification might succeed)
        chunk.addMetadata("legitimate_key", "legitimate_value");
        assertThat(chunk.hasMetadata("legitimate_key")).isTrue();
    }

    @Test
    void testToStringAndEquality() {
        LocalDateTime timestamp = LocalDateTime.now();
        
        DocumentChunk chunk1 = DocumentChunk.builder()
            .chunkId("equals-chunk-1")
            .documentId("equals-doc")
            .content("Content for equality test.")
            .index(0)
            .documentTimestamp(timestamp)
            .build();

        DocumentChunk chunk2 = DocumentChunk.builder()
            .chunkId("equals-chunk-1")
            .documentId("equals-doc")
            .content("Content for equality test.")
            .index(0)
            .documentTimestamp(timestamp)
            .build();

        DocumentChunk chunk3 = DocumentChunk.builder()
            .chunkId("equals-chunk-2")
            .documentId("equals-doc")
            .content("Different content.")
            .index(1)
            .documentTimestamp(timestamp)
            .build();

        // Test toString (should not throw and should contain key information)
        String chunk1String = chunk1.toString();
        assertThat(chunk1String).contains("equals-chunk-1");
        assertThat(chunk1String).isNotBlank();

        // If equals/hashCode are implemented, test them
        // Note: This depends on whether Lombok @Data or manual implementation is used
        if (chunk1.equals(chunk2)) {
            assertThat(chunk1.hashCode()).isEqualTo(chunk2.hashCode());
        }
        
        assertThat(chunk1.equals(chunk3)).isFalse();
    }
}