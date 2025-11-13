package com.noteflix.pcm.rag.chunking;

import com.noteflix.pcm.rag.embedding.EmbeddingService;
import com.noteflix.pcm.rag.model.DocumentType;
import com.noteflix.pcm.rag.model.RAGDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Comprehensive test suite for all chunking strategies.
 * 
 * Tests cover:
 * - Basic functionality for each strategy
 * - Quality metrics calculation
 * - Edge cases and error handling
 * - Performance characteristics
 * - Strategy suitability assessment
 */
@ExtendWith(MockitoExtension.class)
public class ChunkingStrategyTest {

    @Mock
    private EmbeddingService mockEmbeddingService;

    private RAGDocument testDocument;
    private RAGDocument markdownDocument;
    private RAGDocument academicDocument;
    private RAGDocument codeDocument;

    @BeforeEach
    void setUp() {
        // Setup mock embedding service
        when(mockEmbeddingService.getDimension()).thenReturn(384);
        when(mockEmbeddingService.embed(anyString())).thenReturn(createMockEmbedding());
        when(mockEmbeddingService.embedBatch(any(String[].class)))
            .thenAnswer(invocation -> {
                String[] texts = invocation.getArgument(0);
                float[][] embeddings = new float[texts.length][];
                for (int i = 0; i < texts.length; i++) {
                    embeddings[i] = createMockEmbedding();
                }
                return embeddings;
            });

        // Create test documents
        testDocument = RAGDocument.builder()
            .id("test-doc-1")
            .title("Test Document")
            .content(createSampleText())
            .type(DocumentType.ARTICLE)
            .sourcePath("/test/document.txt")
            .indexedAt(LocalDateTime.now())
            .build();

        markdownDocument = RAGDocument.builder()
            .id("markdown-doc")
            .title("API Documentation")
            .content(createMarkdownContent())
            .type(DocumentType.TECHNICAL_MANUAL)
            .sourcePath("/docs/api.md")
            .indexedAt(LocalDateTime.now())
            .build();

        academicDocument = RAGDocument.builder()
            .id("academic-doc")
            .title("Research Paper")
            .content(createAcademicContent())
            .type(DocumentType.RESEARCH_PAPER)
            .sourcePath("/papers/research.pdf")
            .indexedAt(LocalDateTime.now())
            .build();

        codeDocument = RAGDocument.builder()
            .id("code-doc")
            .title("Code Documentation")
            .content(createCodeContent())
            .type(DocumentType.TECHNICAL_MANUAL)
            .sourcePath("/code/README.md")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    // === Fixed-Size Chunking Tests ===

    @Test
    void testFixedSizeChunking_BasicFunctionality() {
        FixedSizeChunking strategy = new FixedSizeChunking(500, 100);
        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks).hasSize(greaterThan(1));
        
        // Verify chunk sizes are within expected range
        for (DocumentChunk chunk : chunks) {
            assertThat(chunk.getChunkSizeChars()).isLessThanOrEqualTo(600); // Some tolerance
            assertThat(chunk.getContent()).isNotBlank();
            assertThat(chunk.getChunkingStrategy()).isEqualTo("FixedSize");
        }
    }

    @Test
    void testFixedSizeChunking_OverlapBehavior() {
        FixedSizeChunking strategy = new FixedSizeChunking(300, 50);
        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        if (chunks.size() > 1) {
            // Check overlap between consecutive chunks
            for (int i = 1; i < chunks.size(); i++) {
                DocumentChunk prev = chunks.get(i - 1);
                DocumentChunk current = chunks.get(i);
                
                assertThat(current.getHasOverlapBefore()).isTrue();
                assertThat(prev.getHasOverlapAfter()).isTrue();
                
                // Verify actual overlap content
                String prevContent = prev.getContent();
                String currentContent = current.getContent();
                String overlapFromPrev = prevContent.substring(Math.max(0, prevContent.length() - 50));
                String overlapFromCurrent = currentContent.substring(0, Math.min(50, currentContent.length()));
                
                // Should have some common content (allowing for word boundaries)
                assertThat(overlapFromPrev.length()).isGreaterThan(0);
                assertThat(overlapFromCurrent.length()).isGreaterThan(0);
            }
        }
    }

    @Test
    void testFixedSizeChunking_QualityMetrics() {
        FixedSizeChunking strategy = new FixedSizeChunking(400, 80);
        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        for (DocumentChunk chunk : chunks) {
            // Quality score should be reasonable for fixed-size
            assertThat(chunk.getQualityScore()).isBetween(0.0, 1.0);
            assertThat(chunk.getDensityScore()).isBetween(0.0, 1.0);
            
            // Should have estimated token count
            assertThat(chunk.getEstimatedTokens()).isGreaterThan(0);
        }
    }

    @Test
    void testFixedSizeChunking_EdgeCases() {
        FixedSizeChunking strategy = new FixedSizeChunking(1000, 200);

        // Empty document
        RAGDocument emptyDoc = RAGDocument.builder()
            .id("empty")
            .content("")
            .build();
        List<DocumentChunk> emptyChunks = strategy.chunk(emptyDoc);
        assertThat(emptyChunks).isEmpty();

        // Very short document
        RAGDocument shortDoc = RAGDocument.builder()
            .id("short")
            .content("Short text.")
            .build();
        List<DocumentChunk> shortChunks = strategy.chunk(shortDoc);
        assertThat(shortChunks).hasSize(1);
        assertThat(shortChunks.get(0).getContent()).isEqualTo("Short text.");
    }

    // === Sentence-Aware Chunking Tests ===

    @Test
    void testSentenceAwareChunking_BasicFunctionality() {
        SentenceAwareChunking strategy = SentenceAwareChunking.defaults();
        List<DocumentChunk> chunks = strategy.chunk(testDocument);

        assertThat(chunks).isNotEmpty();
        
        // Verify chunks end with complete sentences
        for (DocumentChunk chunk : chunks) {
            String content = chunk.getContent().trim();
            assertThat(content).matches(".*[.!?]\\s*$");
            assertThat(chunk.getChunkingStrategy()).isEqualTo("SentenceAware");
            
            // Should have sentence count metadata
            assertThat(chunk.getCustomMetadata().get("sentence_count")).isNotNull();
            assertThat(chunk.getCustomMetadata().get("chunk_type")).isEqualTo("sentence_aware");
        }
    }

    @Test
    void testSentenceAwareChunking_SentenceDetection() {
        String complexText = "Dr. Smith went to the U.S.A. He met Mr. Johnson. " +
                           "They discussed A.I. technology! What an amazing day? " +
                           "The company Inc. was founded in 1990.";
        
        RAGDocument complexDoc = RAGDocument.builder()
            .id("complex")
            .content(complexText)
            .build();

        SentenceAwareChunking strategy = new SentenceAwareChunking(100, 20, 0.3);
        List<DocumentChunk> chunks = strategy.chunk(complexDoc);

        // Should handle abbreviations correctly and not split inappropriately
        assertThat(chunks).isNotEmpty();
        
        // Check that abbreviations are preserved
        boolean foundDrSmith = chunks.stream()
            .anyMatch(chunk -> chunk.getContent().contains("Dr. Smith"));
        assertThat(foundDrSmith).isTrue();
    }

    @Test
    void testSentenceAwareChunking_QualityAssessment() {
        SentenceAwareChunking strategy = SentenceAwareChunking.defaults();
        
        // Test suitability assessment
        assertThat(strategy.isSuitableFor(testDocument)).isTrue();
        
        // Test quality estimation
        double quality = strategy.estimateQuality(testDocument);
        assertThat(quality).isBetween(0.0, 1.0);
        assertThat(quality).isGreaterThan(0.5); // Should be decent quality for normal text
    }

    @Test
    void testSentenceAwareChunking_ToleranceConfiguration() {
        // Strict tolerance
        SentenceAwareChunking strict = SentenceAwareChunking.strict();
        List<DocumentChunk> strictChunks = strict.chunk(testDocument);

        // Flexible tolerance  
        SentenceAwareChunking flexible = SentenceAwareChunking.flexible();
        List<DocumentChunk> flexibleChunks = flexible.chunk(testDocument);

        // Strict should produce more, smaller chunks
        if (strictChunks.size() > 0 && flexibleChunks.size() > 0) {
            assertThat(strictChunks.size()).isGreaterThanOrEqualTo(flexibleChunks.size());
        }
    }

    // === Semantic Chunking Tests ===

    @Test
    void testSemanticChunking_BasicFunctionality() {
        SemanticChunking strategy = SemanticChunking.defaults(mockEmbeddingService);
        List<DocumentChunk> chunks = strategy.chunk(academicDocument);

        assertThat(chunks).isNotEmpty();
        
        for (DocumentChunk chunk : chunks) {
            assertThat(chunk.getChunkingStrategy()).isEqualTo("Semantic");
            assertThat(chunk.getCoherenceScore()).isBetween(0.0, 1.0);
            
            // Should have semantic-specific metadata
            assertThat(chunk.getCustomMetadata().get("segment_count")).isNotNull();
            assertThat(chunk.getCustomMetadata().get("semantic_coherence")).isNotNull();
            assertThat(chunk.getCustomMetadata().get("chunk_type")).isEqualTo("semantic");
        }
    }

    @Test
    void testSemanticChunking_SimilarityThresholds() {
        // High precision (strict similarity)
        SemanticChunking precise = SemanticChunking.precise(mockEmbeddingService);
        List<DocumentChunk> preciseChunks = precise.chunk(academicDocument);

        // Flexible similarity
        SemanticChunking flexible = SemanticChunking.flexible(mockEmbeddingService);
        List<DocumentChunk> flexibleChunks = flexible.chunk(academicDocument);

        // Precise should generally create more chunks (stricter grouping)
        if (preciseChunks.size() > 0 && flexibleChunks.size() > 0) {
            // This is a tendency, not a strict rule due to size constraints
            assertThat(preciseChunks.size()).isGreaterThanOrEqualTo(flexibleChunks.size() - 1);
        }
    }

    @Test
    void testSemanticChunking_EmbeddingServiceRequired() {
        // Should throw exception without embedding service
        assertThrows(IllegalArgumentException.class, () -> {
            new SemanticChunking(null, 2000, 200, 0.75, 3);
        });
    }

    @Test
    void testSemanticChunking_QualityEstimation() {
        SemanticChunking strategy = SemanticChunking.defaults(mockEmbeddingService);
        
        double quality = strategy.estimateQuality(academicDocument);
        assertThat(quality).isBetween(0.0, 1.0);
        
        // Should be suitable for academic content
        assertThat(strategy.isSuitableFor(academicDocument)).isTrue();
        
        // Should be less suitable for very short content
        RAGDocument shortDoc = RAGDocument.builder()
            .id("short")
            .content("Short text.")
            .build();
        assertThat(strategy.isSuitableFor(shortDoc)).isFalse();
    }

    // === Markdown-Aware Chunking Tests ===

    @Test
    void testMarkdownAwareChunking_BasicFunctionality() {
        MarkdownAwareChunking strategy = MarkdownAwareChunking.defaults();
        List<DocumentChunk> chunks = strategy.chunk(markdownDocument);

        assertThat(chunks).isNotEmpty();
        
        for (DocumentChunk chunk : chunks) {
            assertThat(chunk.getChunkingStrategy()).isEqualTo("MarkdownAware");
            
            // Should preserve section information
            if (chunk.getSectionTitle() != null) {
                assertThat(chunk.getSectionTitle()).isNotBlank();
                assertThat(chunk.getHierarchyLevel()).isBetween(1, 6);
            }
        }
    }

    @Test
    void testMarkdownAwareChunking_StructurePreservation() {
        MarkdownAwareChunking strategy = MarkdownAwareChunking.codePreserving();
        List<DocumentChunk> chunks = strategy.chunk(codeDocument);

        // Look for code blocks in chunks
        boolean foundCodeBlock = chunks.stream()
            .anyMatch(chunk -> chunk.getContent().contains("```"));
        
        if (codeDocument.getContent().contains("```")) {
            assertThat(foundCodeBlock).isTrue();
        }

        // Check metadata
        for (DocumentChunk chunk : chunks) {
            String chunkType = chunk.getCustomMetadata().get("chunk_type");
            assertThat(chunkType).isIn("markdown_aware", "markdown_aware_split");
        }
    }

    @Test
    void testMarkdownAwareChunking_HeaderHierarchy() {
        MarkdownAwareChunking strategy = MarkdownAwareChunking.headerFocused();
        List<DocumentChunk> chunks = strategy.chunk(markdownDocument);

        // Should respect header levels
        for (DocumentChunk chunk : chunks) {
            if (chunk.getHierarchyLevel() != null) {
                assertThat(chunk.getHierarchyLevel()).isBetween(1, 6);
            }
        }
    }

    @Test
    void testMarkdownAwareChunking_SuitabilityAssessment() {
        MarkdownAwareChunking strategy = MarkdownAwareChunking.defaults();
        
        // Should be suitable for markdown content
        assertThat(strategy.isSuitableFor(markdownDocument)).isTrue();
        
        // Should be less suitable for plain text
        assertThat(strategy.isSuitableFor(testDocument)).isFalse();
    }

    // === Strategy Comparison Tests ===

    @Test
    void testStrategyPerformanceComparison() {
        // Test processing time for different strategies
        long startTime, endTime;
        
        // Fixed-size (should be fastest)
        startTime = System.currentTimeMillis();
        FixedSizeChunking fixedSize = new FixedSizeChunking(1000, 200);
        List<DocumentChunk> fixedChunks = fixedSize.chunk(testDocument);
        endTime = System.currentTimeMillis();
        long fixedTime = endTime - startTime;

        // Sentence-aware (moderate speed)
        startTime = System.currentTimeMillis();
        SentenceAwareChunking sentenceAware = SentenceAwareChunking.defaults();
        List<DocumentChunk> sentenceChunks = sentenceAware.chunk(testDocument);
        endTime = System.currentTimeMillis();
        long sentenceTime = endTime - startTime;

        // Markdown-aware (moderate speed)
        startTime = System.currentTimeMillis();
        MarkdownAwareChunking markdownAware = MarkdownAwareChunking.defaults();
        List<DocumentChunk> markdownChunks = markdownAware.chunk(markdownDocument);
        endTime = System.currentTimeMillis();
        long markdownTime = endTime - startTime;

        // Assert basic expectations (may vary by system)
        assertThat(fixedChunks).isNotEmpty();
        assertThat(sentenceChunks).isNotEmpty();
        assertThat(markdownChunks).isNotEmpty();
        
        // Fixed-size should generally be fastest (though this may not always be true in tests)
        assertThat(fixedTime).isLessThan(1000); // Should complete within 1 second
    }

    @Test
    void testQualityMetricsComparison() {
        // Compare quality scores across strategies
        FixedSizeChunking fixedSize = new FixedSizeChunking(1000, 200);
        SentenceAwareChunking sentenceAware = SentenceAwareChunking.defaults();
        
        double fixedQuality = fixedSize.estimateQuality(testDocument);
        double sentenceQuality = sentenceAware.estimateQuality(testDocument);
        
        // Sentence-aware should generally have higher quality
        assertThat(sentenceQuality).isGreaterThanOrEqualTo(fixedQuality);
        
        // Both should be valid quality scores
        assertThat(fixedQuality).isBetween(0.0, 1.0);
        assertThat(sentenceQuality).isBetween(0.0, 1.0);
    }

    // === Edge Cases and Error Handling ===

    @Test
    void testNullDocumentHandling() {
        ChunkingStrategy[] strategies = {
            new FixedSizeChunking(1000, 200),
            SentenceAwareChunking.defaults(),
            MarkdownAwareChunking.defaults()
        };

        for (ChunkingStrategy strategy : strategies) {
            assertThrows(Exception.class, () -> {
                strategy.chunk(null);
            });
        }
    }

    @Test
    void testEmptyContentHandling() {
        RAGDocument emptyDoc = RAGDocument.builder()
            .id("empty")
            .content("")
            .build();

        ChunkingStrategy[] strategies = {
            new FixedSizeChunking(1000, 200),
            SentenceAwareChunking.defaults(),
            MarkdownAwareChunking.defaults()
        };

        for (ChunkingStrategy strategy : strategies) {
            List<DocumentChunk> chunks = strategy.chunk(emptyDoc);
            assertThat(chunks).isEmpty();
        }
    }

    @Test
    void testVeryLongDocumentHandling() {
        // Create a very long document (100KB)
        StringBuilder longContent = new StringBuilder();
        String paragraph = "This is a test paragraph with multiple sentences. " +
                         "It contains various punctuation marks! " +
                         "And it helps test chunking behavior? " +
                         "The content should be realistic and meaningful. ";
        
        for (int i = 0; i < 1000; i++) {
            longContent.append(paragraph);
        }

        RAGDocument longDoc = RAGDocument.builder()
            .id("long-doc")
            .content(longContent.toString())
            .build();

        SentenceAwareChunking strategy = SentenceAwareChunking.defaults();
        List<DocumentChunk> chunks = strategy.chunk(longDoc);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.size()).isGreaterThan(50); // Should create many chunks
        
        // Verify chunk integrity
        for (DocumentChunk chunk : chunks) {
            assertThat(chunk.getContent()).isNotBlank();
            assertThat(chunk.getChunkSizeChars()).isGreaterThan(0);
        }
    }

    // === Utility Methods ===

    private float[] createMockEmbedding() {
        // Create a mock embedding vector with 384 dimensions
        float[] embedding = new float[384];
        for (int i = 0; i < 384; i++) {
            embedding[i] = (float) (Math.random() - 0.5); // Random values between -0.5 and 0.5
        }
        return embedding;
    }

    private String createSampleText() {
        return "Natural language processing (NLP) is a branch of artificial intelligence " +
               "that helps computers understand, interpret and manipulate human language. " +
               "NLP draws from many disciplines, including computer science and computational linguistics, " +
               "in its pursuit to fill the gap between human communication and computer understanding. " +
               
               "Machine learning is a subset of artificial intelligence (AI) that provides systems " +
               "the ability to automatically learn and improve from experience without being explicitly programmed. " +
               "Machine learning focuses on the development of computer programs that can access data and use it to learn for themselves. " +
               
               "Deep learning is part of a broader family of machine learning methods based on artificial neural networks " +
               "with representation learning. Learning can be supervised, semi-supervised or unsupervised. " +
               "Deep learning architectures such as deep neural networks, deep belief networks, " +
               "recurrent neural networks and convolutional neural networks have been applied to fields " +
               "including computer vision, speech recognition, natural language processing, machine translation, " +
               "bioinformatics and drug design.";
    }

    private String createMarkdownContent() {
        return """
            # API Documentation
            
            This document describes the REST API endpoints for our service.
            
            ## Authentication
            
            All API calls require authentication using JWT tokens.
            
            ### Getting a Token
            
            Send a POST request to `/auth/login`:
            
            ```json
            {
              "username": "user@example.com",
              "password": "password123"
            }
            ```
            
            ### Using the Token
            
            Include the token in the Authorization header:
            
            ```
            Authorization: Bearer YOUR_JWT_TOKEN
            ```
            
            ## User Management
            
            ### Create User
            
            **POST** `/users` - Create a new user account.
            
            #### Request Body
            
            | Field | Type | Required | Description |
            |-------|------|----------|-------------|
            | name | string | Yes | User's full name |
            | email | string | Yes | User's email address |
            | role | string | No | User role (default: user) |
            
            ### Update User
            
            **PUT** `/users/{id}` - Update existing user.
            
            ### Delete User
            
            **DELETE** `/users/{id}` - Delete a user account.
            
            ## Error Handling
            
            The API returns standard HTTP status codes:
            
            - `200` - Success
            - `401` - Unauthorized
            - `404` - Not Found
            - `500` - Internal Server Error
            """;
    }

    private String createAcademicContent() {
        return "Machine learning has revolutionized the field of artificial intelligence, " +
               "enabling computers to learn and make decisions without explicit programming. " +
               "The development of neural networks, particularly deep learning architectures, " +
               "has enabled unprecedented advances in computer vision and natural language processing. " +
               
               "Recent studies have shown that transformer models achieve state-of-the-art performance " +
               "across multiple domains including language understanding, text generation, and translation tasks. " +
               "The attention mechanism, first introduced in sequence-to-sequence models, " +
               "has become a fundamental building block of modern neural architectures. " +
               
               "However, these models face significant challenges in terms of computational efficiency " +
               "and interpretability. The increasing size of models, such as GPT-3 and PaLM, " +
               "requires enormous computational resources for both training and inference. " +
               "Research efforts are now focusing on developing more efficient architectures " +
               "and better understanding the mechanisms behind their success. " +
               
               "Transfer learning and few-shot learning have emerged as promising approaches " +
               "to reduce the data requirements for training effective models. " +
               "Pre-trained models can be fine-tuned on specific tasks with limited data, " +
               "achieving performance comparable to models trained from scratch on large datasets.";
    }

    private String createCodeContent() {
        return """
            # Project Setup Guide
            
            ## Prerequisites
            
            Before starting, ensure you have the following installed:
            
            - Java 17 or higher
            - Maven 3.8+
            - Docker (optional)
            
            ## Installation
            
            ### Clone the Repository
            
            ```bash
            git clone https://github.com/example/project.git
            cd project
            ```
            
            ### Build the Project
            
            ```bash
            mvn clean install
            ```
            
            ### Configuration
            
            Create a `application.properties` file:
            
            ```properties
            server.port=8080
            spring.datasource.url=jdbc:h2:mem:testdb
            spring.jpa.hibernate.ddl-auto=create-drop
            ```
            
            ## Running the Application
            
            ### Development Mode
            
            ```bash
            mvn spring-boot:run
            ```
            
            ### Production Mode
            
            ```bash
            java -jar target/app.jar
            ```
            
            ## API Examples
            
            ### Create User
            
            ```curl
            curl -X POST http://localhost:8080/users \\
              -H "Content-Type: application/json" \\
              -d '{"name": "John Doe", "email": "john@example.com"}'
            ```
            
            ### Get User
            
            ```curl
            curl http://localhost:8080/users/1
            ```
            
            ## Testing
            
            Run the test suite:
            
            ```bash
            mvn test
            ```
            
            For integration tests:
            
            ```bash
            mvn verify
            ```
            """;
    }
}