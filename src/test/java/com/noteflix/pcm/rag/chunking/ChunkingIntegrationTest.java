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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the complete chunking system.
 * 
 * These tests verify:
 * - End-to-end chunking workflows
 * - Integration between different components
 * - Performance under load
 * - Error handling in complex scenarios
 * - Real-world usage patterns
 */
@ExtendWith(MockitoExtension.class)
public class ChunkingIntegrationTest {

    @Mock
    private EmbeddingService mockEmbeddingService;

    @BeforeEach
    void setUp() {
        // Setup mock embedding service with realistic behavior
        when(mockEmbeddingService.getDimension()).thenReturn(384);
        when(mockEmbeddingService.embed(anyString()))
            .thenAnswer(invocation -> createRealisticEmbedding(invocation.getArgument(0)));
        when(mockEmbeddingService.embedBatch(any(String[].class)))
            .thenAnswer(invocation -> {
                String[] texts = invocation.getArgument(0);
                float[][] embeddings = new float[texts.length][];
                for (int i = 0; i < texts.length; i++) {
                    embeddings[i] = createRealisticEmbedding(texts[i]);
                }
                return embeddings;
            });
    }

    @Test
    void testCompleteWorkflowWithDifferentDocumentTypes() {
        // Test the complete workflow from document to chunks with different document types
        
        // Create diverse set of documents
        RAGDocument markdownDoc = createMarkdownDocument();
        RAGDocument academicDoc = createAcademicDocument();
        RAGDocument codeDoc = createCodeDocument();
        RAGDocument narrativeDoc = createNarrativeDocument();

        RAGDocument[] documents = {markdownDoc, academicDoc, codeDoc, narrativeDoc};
        String[] expectedStrategies = {"MarkdownAware", "Semantic", "MarkdownAware", "SentenceAware"};

        for (int i = 0; i < documents.length; i++) {
            RAGDocument doc = documents[i];
            String expectedStrategy = expectedStrategies[i];

            // Use factory to get optimal strategy
            ChunkingConfig config = ChunkingConfig.defaults();
            ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
                doc, config, mockEmbeddingService);

            // Verify strategy selection makes sense
            assertThat(strategy.getStrategyName()).isIn(expectedStrategy, "SentenceAware", "FixedSize");

            // Chunk the document
            List<DocumentChunk> chunks = strategy.chunk(doc);

            // Verify chunk quality
            assertThat(chunks).isNotEmpty();
            assertThat(chunks.size()).isBetween(1, 20); // Reasonable chunk count

            // Verify chunk integrity
            for (int j = 0; j < chunks.size(); j++) {
                DocumentChunk chunk = chunks.get(j);
                
                assertThat(chunk.getChunkId()).isNotBlank();
                assertThat(chunk.getDocumentId()).isEqualTo(doc.getId());
                assertThat(chunk.getContent()).isNotBlank();
                assertThat(chunk.getIndex()).isEqualTo(j);
                assertThat(chunk.getChunkingStrategy()).isNotBlank();
                
                if (chunk.getQualityScore() != null) {
                    assertThat(chunk.getQualityScore()).isBetween(0.0, 1.0);
                }
            }

            // Verify chunk linking
            for (int j = 0; j < chunks.size(); j++) {
                DocumentChunk chunk = chunks.get(j);
                
                if (j > 0) {
                    assertThat(chunk.getPreviousChunkId()).isNotNull();
                }
                if (j < chunks.size() - 1) {
                    assertThat(chunk.getNextChunkId()).isNotNull();
                }
            }
        }
    }

    @Test
    void testDocumentTypeSpecificConfiguration() {
        // Test that document-type specific configurations are properly applied
        
        ChunkingConfig baseConfig = ChunkingConfig.defaults();
        
        // Add document-type specific configurations
        baseConfig.addDocumentTypeConfig(
            DocumentType.TECHNICAL_MANUAL, 
            ChunkingConfig.forTechnicalDocs()
        );
        baseConfig.addDocumentTypeConfig(
            DocumentType.RESEARCH_PAPER,
            ChunkingConfig.forAcademicPapers(mockEmbeddingService)
        );

        // Test technical manual
        RAGDocument techDoc = createCodeDocument();
        ChunkingStrategy techStrategy = ChunkingFactory.createOptimalStrategy(
            techDoc, baseConfig, mockEmbeddingService);
        List<DocumentChunk> techChunks = techStrategy.chunk(techDoc);
        
        assertThat(techChunks).isNotEmpty();
        // Technical docs should preserve structure
        boolean hasStructuralInfo = techChunks.stream()
            .anyMatch(chunk -> chunk.getSectionTitle() != null || 
                              chunk.getCustomMetadata().containsKey("chunk_type"));
        assertThat(hasStructuralInfo).isTrue();

        // Test research paper
        RAGDocument academicDoc = createAcademicDocument();
        ChunkingStrategy academicStrategy = ChunkingFactory.createOptimalStrategy(
            academicDoc, baseConfig, mockEmbeddingService);
        List<DocumentChunk> academicChunks = academicStrategy.chunk(academicDoc);
        
        assertThat(academicChunks).isNotEmpty();
        // Academic content should have high coherence
        double avgCoherence = academicChunks.stream()
            .filter(chunk -> chunk.getCoherenceScore() != null)
            .mapToDouble(DocumentChunk::getCoherenceScore)
            .average()
            .orElse(0.0);
        assertThat(avgCoherence).isGreaterThan(0.5);
    }

    @Test
    void testQualityBasedFallbackMechanism() {
        // Test that quality-based fallback works correctly
        
        ChunkingConfig strictConfig = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC)
            .fallbackStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE)
            .minQualityThreshold(0.9) // Very high threshold to trigger fallback
            .preferredQualityThreshold(0.95)
            .enableQualityFallback(true)
            .semanticConfig(ChunkingConfig.SemanticConfig.builder()
                .embeddingService(mockEmbeddingService)
                .similarityThreshold(0.99) // Very strict similarity
                .build())
            .build();

        RAGDocument testDoc = createNarrativeDocument();
        
        // Create fallback strategy
        ChunkingStrategy fallbackStrategy = ChunkingFactory.createWithFallback(
            strictConfig, mockEmbeddingService);
        
        List<DocumentChunk> chunks = fallbackStrategy.chunk(testDoc);
        
        // Should successfully create chunks despite high quality threshold
        assertThat(chunks).isNotEmpty();
        assertThat(fallbackStrategy.getStrategyName()).contains("Adaptive");
    }

    @Test
    void testConcurrentChunking() {
        // Test that chunking works correctly under concurrent access
        
        int numThreads = 4;
        int documentsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        try {
            List<CompletableFuture<ChunkingResult>> futures = IntStream.range(0, numThreads)
                .mapToObj(threadId -> CompletableFuture.supplyAsync(() -> {
                    ChunkingResult result = new ChunkingResult();
                    
                    for (int i = 0; i < documentsPerThread; i++) {
                        RAGDocument doc = createTestDocument(threadId * documentsPerThread + i);
                        
                        ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
                            doc, ChunkingConfig.defaults(), mockEmbeddingService);
                        
                        List<DocumentChunk> chunks = strategy.chunk(doc);
                        
                        result.addResult(doc.getId(), chunks.size(), 
                                       calculateAverageQuality(chunks));
                    }
                    
                    return result;
                }, executor))
                .toList();

            // Wait for all tasks to complete
            List<ChunkingResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

            // Verify all results
            assertThat(results).hasSize(numThreads);
            
            int totalDocuments = 0;
            int totalChunks = 0;
            double totalQuality = 0.0;
            
            for (ChunkingResult result : results) {
                totalDocuments += result.getDocumentCount();
                totalChunks += result.getTotalChunks();
                totalQuality += result.getAverageQuality() * result.getDocumentCount();
            }
            
            assertThat(totalDocuments).isEqualTo(numThreads * documentsPerThread);
            assertThat(totalChunks).isGreaterThan(totalDocuments); // Should have multiple chunks per doc
            assertThat(totalQuality / totalDocuments).isGreaterThan(0.3); // Reasonable quality
            
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testLargeDocumentHandling() {
        // Test handling of very large documents
        
        RAGDocument largeDoc = createLargeDocument();
        
        // Test different strategies on large document
        ChunkingStrategy[] strategies = {
            new FixedSizeChunking(1000, 200),
            SentenceAwareChunking.defaults(),
            MarkdownAwareChunking.defaults(),
            SemanticChunking.defaults(mockEmbeddingService)
        };

        for (ChunkingStrategy strategy : strategies) {
            List<DocumentChunk> chunks = strategy.chunk(largeDoc);
            
            assertThat(chunks).isNotEmpty();
            assertThat(chunks.size()).isGreaterThan(10); // Large doc should create many chunks
            
            // Verify no chunk is excessively large
            for (DocumentChunk chunk : chunks) {
                assertThat(chunk.getChunkSizeChars()).isLessThan(5000);
            }
            
            // Verify total content is preserved (approximately)
            int totalChunkContent = chunks.stream()
                .mapToInt(DocumentChunk::getChunkSizeChars)
                .sum();
            
            // Account for overlap - total chunk content may be larger than original
            assertThat(totalChunkContent).isGreaterThanOrEqualTo(largeDoc.getContent().length());
        }
    }

    @Test
    void testErrorRecoveryAndRobustness() {
        // Test system's ability to recover from various error conditions
        
        // Test with malformed content
        RAGDocument malformedDoc = RAGDocument.builder()
            .id("malformed-doc")
            .title("Malformed Document")
            .content("Short.\n\n\n\n\nFragmented.\n\nText.")
            .type(DocumentType.ARTICLE)
            .build();

        ChunkingStrategy robustStrategy = ChunkingFactory.createWithFallback(
            ChunkingConfig.defaults(), mockEmbeddingService);
        
        List<DocumentChunk> chunks = robustStrategy.chunk(malformedDoc);
        assertThat(chunks).isNotEmpty(); // Should handle malformed content gracefully

        // Test with empty document
        RAGDocument emptyDoc = RAGDocument.builder()
            .id("empty-doc")
            .title("Empty Document")
            .content("")
            .type(DocumentType.ARTICLE)
            .build();

        chunks = robustStrategy.chunk(emptyDoc);
        assertThat(chunks).isEmpty(); // Should return empty list for empty content

        // Test with very short content
        RAGDocument shortDoc = RAGDocument.builder()
            .id("short-doc")
            .title("Short Document")
            .content("Hi.")
            .type(DocumentType.ARTICLE)
            .build();

        chunks = robustStrategy.chunk(shortDoc);
        assertThat(chunks).hasSize(1); // Should create single chunk for short content
        assertThat(chunks.get(0).getContent()).isEqualTo("Hi.");
    }

    @Test
    void testMetadataPreservationAndEnrichment() {
        // Test that metadata is properly preserved and enriched throughout the chunking process
        
        LocalDateTime timestamp = LocalDateTime.now();
        RAGDocument sourceDoc = RAGDocument.builder()
            .id("metadata-test-doc")
            .title("Metadata Test Document")
            .content(createMarkdownContent())
            .type(DocumentType.TECHNICAL_MANUAL)
            .sourcePath("/test/metadata.md")
            .indexedAt(timestamp)
            .build();

        ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
            sourceDoc, ChunkingConfig.defaults(), mockEmbeddingService);
        
        List<DocumentChunk> chunks = strategy.chunk(sourceDoc);
        
        for (DocumentChunk chunk : chunks) {
            // Verify document metadata is preserved
            assertThat(chunk.getDocumentId()).isEqualTo(sourceDoc.getId());
            assertThat(chunk.getDocumentTitle()).isEqualTo(sourceDoc.getTitle());
            assertThat(chunk.getDocumentType()).isEqualTo(sourceDoc.getType());
            assertThat(chunk.getSourcePath()).isEqualTo(sourceDoc.getSourcePath());
            assertThat(chunk.getDocumentTimestamp()).isEqualTo(sourceDoc.getIndexedAt());
            
            // Verify chunking metadata is added
            assertThat(chunk.getChunkingStrategy()).isNotBlank();
            assertThat(chunk.getChunkSizeChars()).isGreaterThan(0);
            
            // Verify position information
            assertThat(chunk.getStartPosition()).isNotNull();
            assertThat(chunk.getEndPosition()).isNotNull();
            assertThat(chunk.getEndPosition()).isGreaterThan(chunk.getStartPosition());
            
            // Verify custom metadata is present (strategy-specific)
            assertThat(chunk.getCustomMetadata()).isNotEmpty();
        }
    }

    @Test
    void testStrategyRecommendationSystem() {
        // Test the strategy recommendation system with various document types
        
        RAGDocument[] testDocuments = {
            createMarkdownDocument(),
            createAcademicDocument(),
            createNarrativeDocument(),
            createCodeDocument()
        };

        ChunkingConfig config = ChunkingConfig.defaults();

        for (RAGDocument doc : testDocuments) {
            List<ChunkingFactory.StrategyRecommendation> recommendations = 
                ChunkingFactory.getAllRecommendations(doc, config, mockEmbeddingService);
            
            // Should return all 4 strategies
            assertThat(recommendations).hasSize(4);
            
            // Should be ordered by quality (descending)
            for (int i = 1; i < recommendations.size(); i++) {
                assertThat(recommendations.get(i - 1).expectedQuality)
                    .isGreaterThanOrEqualTo(recommendations.get(i).expectedQuality);
            }
            
            // Quality scores should be valid
            for (ChunkingFactory.StrategyRecommendation rec : recommendations) {
                assertThat(rec.expectedQuality).isBetween(0.0, 1.0);
                assertThat(rec.strategyType).isNotNull();
                
                if (rec.strategy != null) {
                    // Test that the strategy can actually process the document
                    List<DocumentChunk> testChunks = rec.strategy.chunk(doc);
                    assertThat(testChunks).isNotEmpty();
                }
            }
            
            // Best strategy should have reasonable quality for suitable documents
            ChunkingFactory.StrategyRecommendation best = recommendations.get(0);
            if (best.strategy != null && best.strategy.isSuitableFor(doc)) {
                assertThat(best.expectedQuality).isGreaterThan(0.5);
            }
        }
    }

    @Test
    void testConfigurationValidationAndEdgeCases() {
        // Test configuration validation and edge case handling
        
        // Test invalid configurations
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .targetChunkSize(-100)
                .build()
                .validate();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .minQualityThreshold(1.5)
                .build()
                .validate();
        });

        // Test extreme but valid configurations
        ChunkingConfig extremeConfig = ChunkingConfig.builder()
            .targetChunkSize(50) // Very small chunks
            .minChunkSize(10)
            .maxChunkSize(100)
            .overlapSize(5)
            .build();

        extremeConfig.validate(); // Should not throw

        ChunkingStrategy extremeStrategy = ChunkingFactory.createStrategy(extremeConfig);
        RAGDocument testDoc = createNarrativeDocument();
        List<DocumentChunk> extremeChunks = extremeStrategy.chunk(testDoc);
        
        assertThat(extremeChunks).isNotEmpty();
        // Should create many small chunks
        assertThat(extremeChunks.size()).isGreaterThan(10);
        
        for (DocumentChunk chunk : extremeChunks) {
            assertThat(chunk.getChunkSizeChars()).isLessThanOrEqualTo(120); // Some tolerance
        }
    }

    @Test
    void testPerformanceCharacteristics() {
        // Test performance characteristics of different strategies
        
        RAGDocument testDoc = createLargeDocument();
        
        ChunkingStrategy[] strategies = {
            new FixedSizeChunking(1000, 200),
            SentenceAwareChunking.defaults(),
            MarkdownAwareChunking.defaults()
            // Note: Excluding Semantic to avoid slow tests
        };

        String[] strategyNames = {"FixedSize", "SentenceAware", "MarkdownAware"};
        
        for (int i = 0; i < strategies.length; i++) {
            ChunkingStrategy strategy = strategies[i];
            String name = strategyNames[i];
            
            // Measure processing time
            long startTime = System.nanoTime();
            List<DocumentChunk> chunks = strategy.chunk(testDoc);
            long endTime = System.nanoTime();
            
            long processingTimeMs = (endTime - startTime) / 1_000_000;
            
            assertThat(chunks).isNotEmpty();
            assertThat(processingTimeMs).isLessThan(5000); // Should complete within 5 seconds
            
            // Log performance metrics
            double avgQuality = calculateAverageQuality(chunks);
            System.out.printf("%s: %d chunks, %.3f quality, %dms%n", 
                name, chunks.size(), avgQuality, processingTimeMs);
        }
    }

    // === Helper Methods ===

    private float[] createRealisticEmbedding(String text) {
        // Create more realistic embeddings based on text characteristics
        float[] embedding = new float[384];
        
        // Simple hash-based approach to create consistent embeddings
        int hash = text.toLowerCase().hashCode();
        
        for (int i = 0; i < 384; i++) {
            // Use different aspects of the text to create varied embeddings
            float value = (float) Math.sin((hash + i) * 0.1) * 0.3f;
            value += (float) Math.cos(text.length() + i) * 0.2f;
            value += (text.toLowerCase().contains("technical") ? 0.1f : -0.1f);
            value += (text.toLowerCase().contains("research") ? 0.15f : -0.05f);
            
            embedding[i] = Math.max(-1.0f, Math.min(1.0f, value));
        }
        
        return embedding;
    }

    private RAGDocument createMarkdownDocument() {
        String content = """
            # API Documentation
            
            ## Authentication
            All API calls require authentication.
            
            ### JWT Tokens
            Use JWT tokens for authentication:
            
            ```json
            {
              "token": "your-jwt-token"
            }
            ```
            
            ## Endpoints
            
            ### Users
            - GET /users - List users
            - POST /users - Create user
            """;

        return RAGDocument.builder()
            .id("markdown-integration-doc")
            .title("API Documentation")
            .content(content)
            .type(DocumentType.TECHNICAL_MANUAL)
            .sourcePath("/docs/api.md")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createAcademicDocument() {
        String content = """
            Abstract: This paper presents a novel approach to machine learning optimization 
            using evolutionary algorithms. We demonstrate significant improvements in 
            convergence speed and solution quality compared to traditional gradient-based methods.
            
            1. Introduction
            
            Machine learning has revolutionized artificial intelligence through gradient-based 
            optimization methods. However, these approaches face limitations including local 
            minima, vanishing gradients, and sensitivity to hyperparameters.
            
            2. Related Work
            
            Evolutionary algorithms have been successfully applied to neural network optimization 
            since the 1990s. Recent developments in neuroevolution show promise for modern 
            deep learning architectures.
            
            3. Methodology
            
            Our approach combines evolution strategies with modern deep learning frameworks. 
            We use a population-based method where each individual represents a complete 
            set of network parameters.
            """;

        return RAGDocument.builder()
            .id("academic-integration-doc")
            .title("Evolutionary Deep Learning")
            .content(content)
            .type(DocumentType.RESEARCH_PAPER)
            .sourcePath("/papers/evolution_dl.pdf")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createNarrativeDocument() {
        String content = """
            The lighthouse keeper watched the storm approach from his tower high above the rocky coast. 
            Dark clouds gathered on the horizon, and the wind began to pick up, causing the beacon's 
            light to flicker slightly in its housing.
            
            Captain Morrison had tended this lighthouse for twenty-three years, through countless 
            storms and quiet nights alike. He knew the patterns of the sea and the behavior of 
            the ships that passed this dangerous stretch of coastline.
            
            Tonight would be different, though. The weather reports spoke of unprecedented wind 
            speeds and waves that could reach thirty feet in height. Ships would need all the 
            guidance they could get to navigate safely to harbor.
            
            As the first raindrops began to hit the lighthouse windows, Morrison checked the 
            beacon one final time, ensuring it would shine brightly through the coming storm.
            """;

        return RAGDocument.builder()
            .id("narrative-integration-doc")
            .title("The Storm Watch")
            .content(content)
            .type(DocumentType.ARTICLE)
            .sourcePath("/stories/lighthouse.txt")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createCodeDocument() {
        String content = """
            # Setup Guide
            
            ## Prerequisites
            - Java 17+
            - Maven 3.8+
            
            ## Installation
            
            ### Clone Repository
            ```bash
            git clone https://github.com/example/project.git
            cd project
            ```
            
            ### Build Project
            ```bash
            mvn clean install
            mvn test
            ```
            
            ### Configuration
            ```properties
            server.port=8080
            spring.datasource.url=jdbc:h2:mem:testdb
            ```
            
            ## Running
            ```bash
            mvn spring-boot:run
            ```
            """;

        return RAGDocument.builder()
            .id("code-integration-doc")
            .title("Project Setup")
            .content(content)
            .type(DocumentType.TECHNICAL_MANUAL)
            .sourcePath("/docs/setup.md")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createTestDocument(int id) {
        String[] contents = {
            "This is a test document with basic content for chunking tests.",
            "Another test document with different content and structure for validation.",
            "A third test document containing various types of content including technical terms.",
            "Fourth test document with longer paragraphs and more complex sentence structures for testing."
        };

        return RAGDocument.builder()
            .id("test-doc-" + id)
            .title("Test Document " + id)
            .content(contents[id % contents.length])
            .type(DocumentType.ARTICLE)
            .sourcePath("/test/doc" + id + ".txt")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createLargeDocument() {
        StringBuilder content = new StringBuilder();
        
        String[] paragraphs = {
            "Machine learning algorithms have transformed the field of artificial intelligence by enabling systems to learn patterns from data without explicit programming.",
            "Deep neural networks, particularly convolutional neural networks, have achieved remarkable success in computer vision tasks such as image classification and object detection.",
            "Natural language processing has benefited greatly from transformer architectures, which use attention mechanisms to capture long-range dependencies in text.",
            "Reinforcement learning combines machine learning with decision theory to enable agents to learn optimal behaviors through interaction with environments.",
            "Unsupervised learning techniques such as clustering and dimensionality reduction help discover hidden patterns and structures in unlabeled data.",
            "Transfer learning allows models trained on one task to be adapted for related tasks, significantly reducing the amount of data and computation required.",
            "Ensemble methods combine multiple learning algorithms to create more robust and accurate predictive models than any individual algorithm alone.",
            "Feature engineering remains crucial for many machine learning applications, requiring domain expertise to identify and construct relevant input variables."
        };

        // Create a large document by repeating paragraphs
        for (int section = 0; section < 10; section++) {
            content.append("## Section ").append(section + 1).append("\n\n");
            for (String paragraph : paragraphs) {
                content.append(paragraph).append(" ");
                content.append("This section explores various aspects of the topic in greater detail. ");
                content.append("Additional research and experimentation continue to advance our understanding. ");
                content.append("\n\n");
            }
        }

        return RAGDocument.builder()
            .id("large-integration-doc")
            .title("Comprehensive Machine Learning Guide")
            .content(content.toString())
            .type(DocumentType.RESEARCH_PAPER)
            .sourcePath("/docs/large_guide.md")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private String createMarkdownContent() {
        return """
            # Technical Guide
            
            ## Overview
            This guide covers technical concepts.
            
            ### Code Examples
            ```java
            public void example() {
                System.out.println("Hello World");
            }
            ```
            
            ## Configuration
            Basic configuration steps:
            1. Install dependencies
            2. Set environment variables
            3. Run tests
            """;
    }

    private double calculateAverageQuality(List<DocumentChunk> chunks) {
        return chunks.stream()
            .filter(chunk -> chunk.getQualityScore() != null)
            .mapToDouble(DocumentChunk::getQualityScore)
            .average()
            .orElse(0.0);
    }

    // === Helper Classes ===

    private static class ChunkingResult {
        private int documentCount = 0;
        private int totalChunks = 0;
        private double totalQuality = 0.0;

        public void addResult(String documentId, int chunkCount, double quality) {
            documentCount++;
            totalChunks += chunkCount;
            totalQuality += quality;
        }

        public int getDocumentCount() { return documentCount; }
        public int getTotalChunks() { return totalChunks; }
        public double getAverageQuality() { 
            return documentCount > 0 ? totalQuality / documentCount : 0.0; 
        }
    }
}