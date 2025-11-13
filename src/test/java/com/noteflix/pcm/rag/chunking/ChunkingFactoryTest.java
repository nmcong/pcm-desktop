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
 * Test suite for ChunkingFactory class.
 * 
 * Tests cover:
 * - Strategy creation from configuration
 * - Optimal strategy selection
 * - Fallback mechanisms  
 * - Strategy recommendations
 * - Use case specific factories
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
public class ChunkingFactoryTest {

    @Mock
    private EmbeddingService mockEmbeddingService;

    private RAGDocument markdownDocument;
    private RAGDocument academicDocument;
    private RAGDocument narrativeDocument;
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
        markdownDocument = createMarkdownDocument();
        academicDocument = createAcademicDocument();
        narrativeDocument = createNarrativeDocument();
        codeDocument = createCodeDocument();
    }

    // === Basic Factory Methods Tests ===

    @Test
    void testCreateStrategyFromConfig() {
        // Test Fixed-Size strategy creation
        ChunkingConfig fixedConfig = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE)
            .targetChunkSize(800)
            .overlapSize(150)
            .build();

        ChunkingStrategy fixedStrategy = ChunkingFactory.createStrategy(fixedConfig);
        assertThat(fixedStrategy).isInstanceOf(FixedSizeChunking.class);
        assertThat(fixedStrategy.getChunkSize()).isEqualTo(800);
        assertThat(fixedStrategy.getOverlapSize()).isEqualTo(150);

        // Test Sentence-Aware strategy creation
        ChunkingConfig sentenceConfig = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE)
            .targetChunkSize(1200)
            .overlapSize(200)
            .sentenceAwareConfig(ChunkingConfig.SentenceAwareConfig.builder()
                .sizeTolerance(0.4)
                .build())
            .build();

        ChunkingStrategy sentenceStrategy = ChunkingFactory.createStrategy(sentenceConfig);
        assertThat(sentenceStrategy).isInstanceOf(SentenceAwareChunking.class);
        assertThat(sentenceStrategy.getChunkSize()).isEqualTo(1200);
        assertThat(sentenceStrategy.getOverlapSize()).isEqualTo(200);

        // Test Markdown-Aware strategy creation
        ChunkingConfig markdownConfig = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.MARKDOWN_AWARE)
            .targetChunkSize(1500)
            .minChunkSize(300)
            .markdownConfig(ChunkingConfig.MarkdownConfig.builder()
                .preserveCodeBlocks(true)
                .respectHeaders(true)
                .maxHeaderLevel(2)
                .build())
            .build();

        ChunkingStrategy markdownStrategy = ChunkingFactory.createStrategy(markdownConfig);
        assertThat(markdownStrategy).isInstanceOf(MarkdownAwareChunking.class);
        assertThat(markdownStrategy.getChunkSize()).isEqualTo(1500);

        // Test Semantic strategy creation
        ChunkingConfig semanticConfig = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC)
            .maxChunkSize(2000)
            .minChunkSize(400)
            .semanticConfig(ChunkingConfig.SemanticConfig.builder()
                .embeddingService(mockEmbeddingService)
                .similarityThreshold(0.8)
                .slidingWindowSize(2)
                .build())
            .build();

        ChunkingStrategy semanticStrategy = ChunkingFactory.createStrategy(semanticConfig);
        assertThat(semanticStrategy).isInstanceOf(SemanticChunking.class);
        assertThat(semanticStrategy.getChunkSize()).isEqualTo(1200); // (max + min) / 2
    }

    @Test
    void testCreateStrategyValidation() {
        // Test invalid configuration validation
        ChunkingConfig invalidConfig = ChunkingConfig.builder()
            .targetChunkSize(-100)
            .build();

        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingFactory.createStrategy(invalidConfig);
        });

        // Test semantic strategy without embedding service
        ChunkingConfig semanticWithoutEmbedding = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC)
            .semanticConfig(ChunkingConfig.SemanticConfig.builder()
                .embeddingService(null)
                .build())
            .build();

        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingFactory.createStrategy(semanticWithoutEmbedding);
        });
    }

    @Test
    void testDefaultStrategyCreation() {
        ChunkingStrategy defaultStrategy = ChunkingFactory.createDefault();
        
        assertThat(defaultStrategy).isInstanceOf(SentenceAwareChunking.class);
        assertThat(defaultStrategy.getStrategyName()).isEqualTo("SentenceAware");
    }

    // === Optimal Strategy Selection Tests ===

    @Test
    void testOptimalStrategyForMarkdown() {
        ChunkingConfig config = ChunkingConfig.defaults();
        
        ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
            markdownDocument, config, mockEmbeddingService);
        
        // Should prefer Markdown-aware for markdown content
        assertThat(strategy.getStrategyName()).isIn("MarkdownAware", "SentenceAware");
    }

    @Test
    void testOptimalStrategyForAcademic() {
        ChunkingConfig config = ChunkingConfig.defaults();
        
        ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
            academicDocument, config, mockEmbeddingService);
        
        // Should work with any suitable strategy
        assertThat(strategy).isNotNull();
        assertThat(strategy.getStrategyName()).isIn("Semantic", "SentenceAware", "FixedSize");
    }

    @Test
    void testOptimalStrategyWithoutAutoSelect() {
        ChunkingConfig config = ChunkingConfig.builder()
            .autoSelectStrategy(false)
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE)
            .build();
        
        ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
            narrativeDocument, config, mockEmbeddingService);
        
        // Should use primary strategy when auto-select is disabled
        assertThat(strategy).isInstanceOf(FixedSizeChunking.class);
    }

    @Test
    void testOptimalStrategyNullDocument() {
        ChunkingConfig config = ChunkingConfig.defaults();
        
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingFactory.createOptimalStrategy(null, config, mockEmbeddingService);
        });
    }

    @Test
    void testOptimalStrategyWithDocumentTypeConfig() {
        ChunkingConfig baseConfig = ChunkingConfig.defaults();
        ChunkingConfig techConfig = ChunkingConfig.forTechnicalDocs();
        baseConfig.addDocumentTypeConfig(DocumentType.TECHNICAL_MANUAL, techConfig);
        
        ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
            markdownDocument, baseConfig, mockEmbeddingService);
        
        // Should use document-type specific configuration
        assertThat(strategy).isNotNull();
    }

    // === Strategy Recommendations Tests ===

    @Test
    void testGetAllRecommendations() {
        ChunkingConfig config = ChunkingConfig.defaults();
        
        List<ChunkingFactory.StrategyRecommendation> recommendations = 
            ChunkingFactory.getAllRecommendations(narrativeDocument, config, mockEmbeddingService);
        
        // Should return all 4 strategies
        assertThat(recommendations).hasSize(4);
        
        // Should be ordered by quality (descending)
        for (int i = 1; i < recommendations.size(); i++) {
            assertThat(recommendations.get(i - 1).expectedQuality)
                .isGreaterThanOrEqualTo(recommendations.get(i).expectedQuality);
        }
        
        // All quality scores should be valid
        for (ChunkingFactory.StrategyRecommendation rec : recommendations) {
            assertThat(rec.expectedQuality).isBetween(0.0, 1.0);
            assertThat(rec.strategyType).isNotNull();
            assertThat(rec.toString()).isNotBlank();
        }
    }

    @Test
    void testRecommendationsWithoutEmbeddingService() {
        ChunkingConfig config = ChunkingConfig.defaults();
        
        List<ChunkingFactory.StrategyRecommendation> recommendations = 
            ChunkingFactory.getAllRecommendations(narrativeDocument, config, null);
        
        // Should return 4 strategies, but semantic should have 0 quality
        assertThat(recommendations).hasSize(4);
        
        ChunkingFactory.StrategyRecommendation semanticRec = recommendations.stream()
            .filter(r -> r.strategyType == ChunkingConfig.ChunkingStrategyType.SEMANTIC)
            .findFirst()
            .orElseThrow();
        
        assertThat(semanticRec.expectedQuality).isEqualTo(0.0);
        assertThat(semanticRec.strategy).isNull();
    }

    @Test
    void testRecommendationForMarkdownContent() {
        ChunkingConfig config = ChunkingConfig.defaults();
        
        List<ChunkingFactory.StrategyRecommendation> recommendations = 
            ChunkingFactory.getAllRecommendations(markdownDocument, config, mockEmbeddingService);
        
        // Markdown-aware should have high quality for markdown content
        ChunkingFactory.StrategyRecommendation markdownRec = recommendations.stream()
            .filter(r -> r.strategyType == ChunkingConfig.ChunkingStrategyType.MARKDOWN_AWARE)
            .findFirst()
            .orElseThrow();
        
        assertThat(markdownRec.expectedQuality).isGreaterThan(0.5);
    }

    // === Use Case Factory Tests ===

    @Test
    void testCreateForTechnicalDocumentation() {
        ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.TECHNICAL_DOCUMENTATION, mockEmbeddingService);
        
        assertThat(strategy).isInstanceOf(MarkdownAwareChunking.class);
        assertThat(strategy.getChunkSize()).isEqualTo(1500);
    }

    @Test
    void testCreateForNarrativeContent() {
        ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.NARRATIVE_CONTENT, mockEmbeddingService);
        
        assertThat(strategy).isInstanceOf(SentenceAwareChunking.class);
        assertThat(strategy.getChunkSize()).isEqualTo(1200);
    }

    @Test
    void testCreateForAcademicPapers() {
        ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.ACADEMIC_PAPERS, mockEmbeddingService);
        
        assertThat(strategy).isInstanceOf(SemanticChunking.class);
        assertThat(strategy.getChunkSize()).isEqualTo(1600); // (3000 + 200) / 2
    }

    @Test
    void testCreateForHighVolumeProcessing() {
        ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.HIGH_VOLUME_PROCESSING, mockEmbeddingService);
        
        assertThat(strategy).isInstanceOf(FixedSizeChunking.class);
        assertThat(strategy.getChunkSize()).isEqualTo(800);
        assertThat(strategy.getOverlapSize()).isEqualTo(100);
    }

    @Test
    void testCreateForGeneralPurpose() {
        ChunkingStrategy strategy = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.GENERAL_PURPOSE, mockEmbeddingService);
        
        assertThat(strategy).isInstanceOf(SentenceAwareChunking.class);
        assertThat(strategy.getChunkSize()).isEqualTo(1000);
    }

    @Test
    void testUseCaseDescriptions() {
        ChunkingFactory.UseCase[] useCases = ChunkingFactory.UseCase.values();
        
        for (ChunkingFactory.UseCase useCase : useCases) {
            assertThat(useCase.getDescription()).isNotBlank();
        }
        
        assertThat(ChunkingFactory.UseCase.TECHNICAL_DOCUMENTATION.getDescription())
            .isEqualTo("Technical documentation and APIs");
        assertThat(ChunkingFactory.UseCase.ACADEMIC_PAPERS.getDescription())
            .isEqualTo("Research papers and academic content");
    }

    // === Fallback Strategy Tests ===

    @Test
    void testCreateWithFallback() {
        ChunkingConfig config = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC)
            .fallbackStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE)
            .minQualityThreshold(0.7)
            .semanticConfig(ChunkingConfig.SemanticConfig.builder()
                .embeddingService(mockEmbeddingService)
                .build())
            .build();
        
        ChunkingStrategy fallbackStrategy = ChunkingFactory.createWithFallback(config, mockEmbeddingService);
        
        assertThat(fallbackStrategy.getStrategyName()).contains("Adaptive");
        assertThat(fallbackStrategy.isSuitableFor(narrativeDocument)).isTrue();
        
        // Should be able to chunk documents
        List<DocumentChunk> chunks = fallbackStrategy.chunk(narrativeDocument);
        assertThat(chunks).isNotEmpty();
    }

    @Test
    void testFallbackStrategyErrorHandling() {
        // Create a config that might cause primary strategy to fail
        ChunkingConfig problematicConfig = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC)
            .fallbackStrategy(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE)
            .semanticConfig(ChunkingConfig.SemanticConfig.builder()
                .embeddingService(null) // This will cause semantic to fail
                .build())
            .build();
        
        ChunkingStrategy fallbackStrategy = ChunkingFactory.createWithFallback(
            problematicConfig, null);
        
        // Should still work due to fallback
        List<DocumentChunk> chunks = fallbackStrategy.chunk(narrativeDocument);
        assertThat(chunks).isNotEmpty();
    }

    // === Quality-based Fallback Tests ===

    @Test
    void testQualityBasedFallback() {
        // Create config with high quality threshold
        ChunkingConfig highQualityConfig = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE)
            .fallbackStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE)
            .minQualityThreshold(0.9) // Very high threshold
            .preferredQualityThreshold(0.95)
            .enableQualityFallback(true)
            .build();
        
        ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
            narrativeDocument, highQualityConfig, mockEmbeddingService);
        
        // Should potentially fallback to better strategy
        assertThat(strategy).isNotNull();
        assertThat(strategy.estimateQuality(narrativeDocument)).isGreaterThan(0.0);
    }

    @Test
    void testDisabledQualityFallback() {
        ChunkingConfig noFallbackConfig = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE)
            .fallbackStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE)
            .enableQualityFallback(false)
            .autoSelectStrategy(false)
            .build();
        
        ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
            narrativeDocument, noFallbackConfig, mockEmbeddingService);
        
        // Should use primary strategy regardless of quality
        assertThat(strategy).isInstanceOf(FixedSizeChunking.class);
    }

    // === Error Handling Tests ===

    @Test
    void testInvalidConfigurationHandling() {
        ChunkingConfig invalidConfig = ChunkingConfig.builder()
            .targetChunkSize(-100)
            .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingFactory.createStrategy(invalidConfig);
        });
    }

    @Test
    void testMissingEmbeddingServiceForSemantic() {
        ChunkingConfig semanticConfig = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC)
            .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingFactory.createStrategy(semanticConfig);
        });
    }

    // === Integration Tests ===

    @Test
    void testCompleteWorkflowWithDifferentDocumentTypes() {
        // Test complete workflow for different document types
        RAGDocument[] documents = {markdownDocument, academicDocument, narrativeDocument, codeDocument};
        ChunkingFactory.UseCase[] useCases = {
            ChunkingFactory.UseCase.TECHNICAL_DOCUMENTATION,
            ChunkingFactory.UseCase.ACADEMIC_PAPERS,
            ChunkingFactory.UseCase.NARRATIVE_CONTENT,
            ChunkingFactory.UseCase.TECHNICAL_DOCUMENTATION
        };
        
        for (int i = 0; i < documents.length; i++) {
            RAGDocument doc = documents[i];
            ChunkingFactory.UseCase useCase = useCases[i];
            
            // Get optimal strategy
            ChunkingStrategy strategy = ChunkingFactory.createForUseCase(useCase, mockEmbeddingService);
            
            // Check suitability
            boolean suitable = strategy.isSuitableFor(doc);
            
            // Chunk document
            List<DocumentChunk> chunks = strategy.chunk(doc);
            
            // Verify results
            assertThat(chunks).isNotEmpty();
            for (DocumentChunk chunk : chunks) {
                assertThat(chunk.getContent()).isNotBlank();
                assertThat(chunk.getChunkingStrategy()).isNotBlank();
                assertThat(chunk.getQualityScore()).isBetween(0.0, 1.0);
            }
        }
    }

    @Test
    void testStrategyRecommendationRanking() {
        // Test that strategy recommendations are properly ranked
        ChunkingConfig config = ChunkingConfig.defaults();
        
        List<ChunkingFactory.StrategyRecommendation> recommendations = 
            ChunkingFactory.getAllRecommendations(markdownDocument, config, mockEmbeddingService);
        
        assertThat(recommendations).hasSize(4);
        
        // Verify ranking order
        double previousQuality = Double.MAX_VALUE;
        for (ChunkingFactory.StrategyRecommendation rec : recommendations) {
            assertThat(rec.expectedQuality).isLessThanOrEqualTo(previousQuality);
            previousQuality = rec.expectedQuality;
        }
    }

    // === Utility Methods ===

    private float[] createMockEmbedding() {
        float[] embedding = new float[384];
        for (int i = 0; i < 384; i++) {
            embedding[i] = (float) (Math.random() - 0.5);
        }
        return embedding;
    }

    private RAGDocument createMarkdownDocument() {
        String markdownContent = """
            # API Documentation
            
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
            
            ## User Management
            
            ### Create User
            POST `/users` - Create a new user account.
            
            ### Update User
            PUT `/users/{id}` - Update existing user.
            """;

        return RAGDocument.builder()
            .id("markdown-doc")
            .title("API Documentation")
            .content(markdownContent)
            .type(DocumentType.TECHNICAL_MANUAL)
            .sourcePath("/docs/api.md")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createAcademicDocument() {
        String academicContent = """
            Machine learning has revolutionized the field of artificial intelligence, 
            enabling computers to learn and make decisions without explicit programming. 
            The development of neural networks, particularly deep learning architectures, 
            has enabled unprecedented advances in computer vision and natural language processing.
            
            Recent studies have shown that transformer models achieve state-of-the-art performance 
            across multiple domains including language understanding, text generation, and translation tasks. 
            The attention mechanism, first introduced in sequence-to-sequence models, 
            has become a fundamental building block of modern neural architectures.
            
            However, these models face significant challenges in terms of computational efficiency 
            and interpretability. The increasing size of models requires enormous computational resources 
            for both training and inference. Research efforts are now focusing on developing 
            more efficient architectures and better understanding the mechanisms behind their success.
            """;

        return RAGDocument.builder()
            .id("academic-doc")
            .title("Machine Learning Research")
            .content(academicContent)
            .type(DocumentType.RESEARCH_PAPER)
            .sourcePath("/papers/ml_research.pdf")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createNarrativeDocument() {
        String narrativeContent = """
            The old lighthouse stood majestically on the rocky cliff, its beacon cutting through 
            the dense fog that rolled in from the sea. Captain Morrison had seen this light 
            countless times during his forty years at sea, but tonight felt different.
            
            As he guided his ship closer to the shore, he noticed something unusual. 
            The light wasn't following its regular pattern. Instead of the familiar 
            three-second intervals, it was flashing erratically.
            
            "Something's not right," he muttered to his first mate. The crew gathered 
            on deck, their faces etched with concern. They had heard the stories about 
            this lighthouse - tales of strange occurrences and unexplained phenomena.
            
            The ship's radio crackled to life. "This is Coast Guard Station Alpha. 
            All vessels in the area, please be advised that Beacon Point Lighthouse 
            is experiencing technical difficulties. Use alternative navigation methods."
            """;

        return RAGDocument.builder()
            .id("narrative-doc")
            .title("The Lighthouse Mystery")
            .content(narrativeContent)
            .type(DocumentType.ARTICLE)
            .sourcePath("/stories/lighthouse.txt")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createCodeDocument() {
        String codeContent = """
            # Project Setup Guide
            
            ## Prerequisites
            Before starting, ensure you have Java 17 or higher installed.
            
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
            ```
            
            ## Running the Application
            ```bash
            mvn spring-boot:run
            ```
            """;

        return RAGDocument.builder()
            .id("code-doc")
            .title("Setup Guide")
            .content(codeContent)
            .type(DocumentType.TECHNICAL_MANUAL)
            .sourcePath("/docs/setup.md")
            .indexedAt(LocalDateTime.now())
            .build();
    }
}