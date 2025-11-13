package com.noteflix.pcm.rag.examples.chunking;

import com.noteflix.pcm.rag.chunking.api.*;
import com.noteflix.pcm.rag.chunking.core.*;
import com.noteflix.pcm.rag.chunking.strategies.*;
import com.noteflix.pcm.rag.embedding.api.EmbeddingService;
import com.noteflix.pcm.rag.model.DocumentType;
import com.noteflix.pcm.rag.model.RAGDocument;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprehensive examples demonstrating all chunking strategies and their use cases.
 * 
 * This class provides real-world examples of:
 * - Strategy selection for different document types
 * - Configuration optimization
 * - Quality assessment and fallback handling
 * - Best practices for production usage
 */
@Slf4j
public class ChunkingExamples {

    // Mock embedding service for examples
    private final EmbeddingService embeddingService = new MockEmbeddingService();

    /**
     * Example 1: Basic chunking workflow for different document types
     */
    public void basicChunkingWorkflow() {
        log.info("=== Basic Chunking Workflow ===");

        // Create sample documents
        RAGDocument technicalDoc = createTechnicalDocument();
        RAGDocument academicDoc = createAcademicDocument();
        RAGDocument narrativeDoc = createNarrativeDocument();

        // Example 1.1: Fixed-Size Chunking (fast, predictable)
        log.info("1.1 Fixed-Size Chunking:");
        FixedSizeChunking fixedStrategy = new FixedSizeChunking(1000, 200);
        List<DocumentChunk> fixedChunks = fixedStrategy.chunk(narrativeDoc);
        
        log.info("Strategy: {}", fixedStrategy.getDescription());
        log.info("Chunks created: {}", fixedChunks.size());
        log.info("Average quality: {:.3f}", calculateAverageQuality(fixedChunks));
        printSampleChunk(fixedChunks.get(0));

        // Example 1.2: Sentence-Aware Chunking (balanced quality/performance)
        log.info("\n1.2 Sentence-Aware Chunking:");
        SentenceAwareChunking sentenceStrategy = SentenceAwareChunking.defaults();
        List<DocumentChunk> sentenceChunks = sentenceStrategy.chunk(narrativeDoc);
        
        log.info("Strategy: {}", sentenceStrategy.getDescription());
        log.info("Chunks created: {}", sentenceChunks.size());
        log.info("Average quality: {:.3f}", calculateAverageQuality(sentenceChunks));
        printSampleChunk(sentenceChunks.get(0));

        // Example 1.3: Markdown-Aware Chunking (structure preservation)
        log.info("\n1.3 Markdown-Aware Chunking:");
        MarkdownAwareChunking markdownStrategy = MarkdownAwareChunking.defaults();
        List<DocumentChunk> markdownChunks = markdownStrategy.chunk(technicalDoc);
        
        log.info("Strategy: {}", markdownStrategy.getDescription());
        log.info("Chunks created: {}", markdownChunks.size());
        log.info("Average quality: {:.3f}", calculateAverageQuality(markdownChunks));
        printSampleChunk(markdownChunks.get(0));

        // Example 1.4: Semantic Chunking (highest quality)
        log.info("\n1.4 Semantic Chunking:");
        SemanticChunking semanticStrategy = SemanticChunking.defaults(embeddingService);
        List<DocumentChunk> semanticChunks = semanticStrategy.chunk(academicDoc);
        
        log.info("Strategy: {}", semanticStrategy.getDescription());
        log.info("Chunks created: {}", semanticChunks.size());
        log.info("Average quality: {:.3f}", calculateAverageQuality(semanticChunks));
        printSampleChunk(semanticChunks.get(0));
    }

    /**
     * Example 2: Automatic strategy selection using ChunkingFactory
     */
    public void automaticStrategySelection() {
        log.info("\n=== Automatic Strategy Selection ===");

        RAGDocument[] documents = {
            createTechnicalDocument(),
            createAcademicDocument(), 
            createNarrativeDocument(),
            createCodeDocument()
        };

        ChunkingConfig config = ChunkingConfig.defaults();

        for (RAGDocument doc : documents) {
            log.info("\n2.1 Analyzing document: {} ({})", doc.getTitle(), doc.getType());

            // Get strategy recommendations
            List<ChunkingFactory.StrategyRecommendation> recommendations = 
                ChunkingFactory.getAllRecommendations(doc, config, embeddingService);

            log.info("Strategy recommendations (ranked by quality):");
            for (ChunkingFactory.StrategyRecommendation rec : recommendations) {
                log.info("  {} - Quality: {:.3f}", rec.strategyType, rec.expectedQuality);
            }

            // Use optimal strategy
            ChunkingStrategy optimalStrategy = ChunkingFactory.createOptimalStrategy(
                doc, config, embeddingService);
            
            log.info("Selected strategy: {}", optimalStrategy.getStrategyName());
            log.info("Suitability: {}", optimalStrategy.isSuitableFor(doc));
            log.info("Quality estimate: {:.3f}", optimalStrategy.estimateQuality(doc));

            // Chunk the document
            List<DocumentChunk> chunks = optimalStrategy.chunk(doc);
            log.info("Chunks created: {}", chunks.size());
            log.info("Actual average quality: {:.3f}", calculateAverageQuality(chunks));
        }
    }

    /**
     * Example 3: Use case specific configurations
     */
    public void useCaseSpecificConfigurations() {
        log.info("\n=== Use Case Specific Configurations ===");

        // Example 3.1: Technical Documentation
        log.info("\n3.1 Technical Documentation Workflow:");
        RAGDocument techDoc = createTechnicalDocument();
        
        ChunkingStrategy techStrategy = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.TECHNICAL_DOCUMENTATION, embeddingService);
        
        log.info("Strategy: {}", techStrategy.getDescription());
        List<DocumentChunk> techChunks = techStrategy.chunk(techDoc);
        
        log.info("Chunks created: {}", techChunks.size());
        analyzeChunkCharacteristics(techChunks, "Technical");

        // Example 3.2: Academic Papers
        log.info("\n3.2 Academic Papers Workflow:");
        RAGDocument academicDoc = createAcademicDocument();
        
        ChunkingStrategy academicStrategy = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.ACADEMIC_PAPERS, embeddingService);
        
        log.info("Strategy: {}", academicStrategy.getDescription());
        List<DocumentChunk> academicChunks = academicStrategy.chunk(academicDoc);
        
        log.info("Chunks created: {}", academicChunks.size());
        analyzeChunkCharacteristics(academicChunks, "Academic");

        // Example 3.3: Narrative Content
        log.info("\n3.3 Narrative Content Workflow:");
        RAGDocument narrativeDoc = createNarrativeDocument();
        
        ChunkingStrategy narrativeStrategy = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.NARRATIVE_CONTENT, embeddingService);
        
        log.info("Strategy: {}", narrativeStrategy.getDescription());
        List<DocumentChunk> narrativeChunks = narrativeStrategy.chunk(narrativeDoc);
        
        log.info("Chunks created: {}", narrativeChunks.size());
        analyzeChunkCharacteristics(narrativeChunks, "Narrative");

        // Example 3.4: High Volume Processing
        log.info("\n3.4 High Volume Processing:");
        ChunkingStrategy highVolumeStrategy = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.HIGH_VOLUME_PROCESSING, embeddingService);
        
        log.info("Strategy: {}", highVolumeStrategy.getDescription());
        
        // Process multiple documents quickly
        long startTime = System.currentTimeMillis();
        int totalChunks = 0;
        
        for (int i = 0; i < 10; i++) {
            RAGDocument doc = createVariedDocument(i);
            List<DocumentChunk> chunks = highVolumeStrategy.chunk(doc);
            totalChunks += chunks.size();
        }
        
        long endTime = System.currentTimeMillis();
        log.info("Processed 10 documents in {}ms", endTime - startTime);
        log.info("Total chunks created: {}", totalChunks);
        log.info("Average processing time per document: {}ms", (endTime - startTime) / 10);
    }

    /**
     * Example 4: Advanced configuration and customization
     */
    public void advancedConfiguration() {
        log.info("\n=== Advanced Configuration ===");

        // Example 4.1: Custom ChunkingConfig
        log.info("\n4.1 Custom Configuration:");
        ChunkingConfig customConfig = ChunkingConfig.builder()
            .targetChunkSize(1500)
            .minChunkSize(300)
            .maxChunkSize(2500)
            .overlapSize(300)
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC)
            .fallbackStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE)
            .autoSelectStrategy(true)
            .minQualityThreshold(0.6)
            .preferredQualityThreshold(0.85)
            .enableQualityFallback(true)
            .semanticConfig(ChunkingConfig.SemanticConfig.builder()
                .embeddingService(embeddingService)
                .similarityThreshold(0.8)
                .slidingWindowSize(4)
                .enableBatchProcessing(true)
                .build())
            .build();

        RAGDocument doc = createAcademicDocument();
        ChunkingStrategy customStrategy = ChunkingFactory.createStrategy(customConfig);
        
        log.info("Custom strategy: {}", customStrategy.getDescription());
        List<DocumentChunk> customChunks = customStrategy.chunk(doc);
        log.info("Custom chunks created: {}", customChunks.size());
        analyzeChunkCharacteristics(customChunks, "Custom");

        // Example 4.2: Document-type specific configurations
        log.info("\n4.2 Document-Type Specific Configurations:");
        ChunkingConfig baseConfig = ChunkingConfig.defaults();
        
        // Add specialized configs for different document types
        baseConfig.addDocumentTypeConfig(DocumentType.TECHNICAL_MANUAL, 
            ChunkingConfig.forTechnicalDocs());
        baseConfig.addDocumentTypeConfig(DocumentType.RESEARCH_PAPER, 
            ChunkingConfig.forAcademicPapers(embeddingService));

        RAGDocument techManual = createTechnicalDocument();
        ChunkingConfig techConfig = baseConfig.getConfigForDocumentType(techManual.getType());
        log.info("Tech manual strategy: {}", techConfig.getPrimaryStrategy());

        RAGDocument researchPaper = createAcademicDocument();  
        ChunkingConfig academicConfig = baseConfig.getConfigForDocumentType(researchPaper.getType());
        log.info("Research paper strategy: {}", academicConfig.getPrimaryStrategy());
    }

    /**
     * Example 5: Quality assessment and fallback mechanisms
     */
    public void qualityAssessmentAndFallback() {
        log.info("\n=== Quality Assessment and Fallback ===");

        // Example 5.1: Quality-based strategy selection
        log.info("\n5.1 Quality-Based Selection:");
        RAGDocument testDoc = createNarrativeDocument();

        ChunkingStrategy[] strategies = {
            new FixedSizeChunking(1000, 200),
            SentenceAwareChunking.defaults(),
            MarkdownAwareChunking.defaults(),
            SemanticChunking.defaults(embeddingService)
        };

        log.info("Quality estimates for different strategies:");
        for (ChunkingStrategy strategy : strategies) {
            double quality = strategy.estimateQuality(testDoc);
            boolean suitable = strategy.isSuitableFor(testDoc);
            log.info("  {} - Quality: {:.3f}, Suitable: {}", 
                strategy.getStrategyName(), quality, suitable);
        }

        // Example 5.2: Fallback mechanisms
        log.info("\n5.2 Fallback Mechanisms:");
        ChunkingConfig fallbackConfig = ChunkingConfig.builder()
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC)
            .fallbackStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE)
            .minQualityThreshold(0.8) // High threshold to trigger fallback
            .enableQualityFallback(true)
            .semanticConfig(ChunkingConfig.SemanticConfig.builder()
                .embeddingService(embeddingService)
                .similarityThreshold(0.9) // Very strict similarity
                .build())
            .build();

        ChunkingStrategy fallbackStrategy = ChunkingFactory.createWithFallback(
            fallbackConfig, embeddingService);
        
        log.info("Fallback strategy: {}", fallbackStrategy.getDescription());
        
        List<DocumentChunk> fallbackChunks = fallbackStrategy.chunk(testDoc);
        log.info("Chunks with fallback: {}", fallbackChunks.size());
        log.info("Average quality: {:.3f}", calculateAverageQuality(fallbackChunks));

        // Example 5.3: Error handling
        log.info("\n5.3 Error Handling:");
        try {
            // Attempt chunking with problematic configuration
            ChunkingConfig problematicConfig = ChunkingConfig.builder()
                .targetChunkSize(-100) // Invalid size
                .build();
            
            ChunkingFactory.createStrategy(problematicConfig);
        } catch (IllegalArgumentException e) {
            log.info("Caught expected validation error: {}", e.getMessage());
        }

        // Robust chunking service that handles errors gracefully
        RobustChunkingService robustService = new RobustChunkingService();
        List<DocumentChunk> robustChunks = robustService.chunkDocumentSafely(testDoc);
        log.info("Robust service created {} chunks", robustChunks.size());
    }

    /**
     * Example 6: Performance optimization techniques
     */
    public void performanceOptimization() {
        log.info("\n=== Performance Optimization ===");

        // Example 6.1: Batch processing
        log.info("\n6.1 Batch Processing:");
        List<RAGDocument> documents = List.of(
            createTechnicalDocument(),
            createAcademicDocument(),
            createNarrativeDocument(),
            createCodeDocument()
        );

        long startTime = System.currentTimeMillis();
        
        // Process documents in parallel
        documents.parallelStream().forEach(doc -> {
            ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
                doc, ChunkingConfig.defaults(), embeddingService);
            List<DocumentChunk> chunks = strategy.chunk(doc);
            log.info("Processed {} into {} chunks", doc.getTitle(), chunks.size());
        });
        
        long endTime = System.currentTimeMillis();
        log.info("Batch processing time: {}ms", endTime - startTime);

        // Example 6.2: Strategy caching
        log.info("\n6.2 Strategy Caching:");
        CachedChunkingService cachedService = new CachedChunkingService(embeddingService);
        
        for (int i = 0; i < 3; i++) {
            startTime = System.currentTimeMillis();
            List<DocumentChunk> chunks = cachedService.chunkWithCaching(createTechnicalDocument());
            endTime = System.currentTimeMillis();
            log.info("Iteration {} - Processing time: {}ms, Chunks: {}", 
                i + 1, endTime - startTime, chunks.size());
        }
    }

    /**
     * Example 7: Production-ready chunking pipeline
     */
    public void productionPipeline() {
        log.info("\n=== Production Pipeline ===");

        ProductionChunkingPipeline pipeline = new ProductionChunkingPipeline(embeddingService);
        
        // Configure pipeline
        PipelineConfig pipelineConfig = PipelineConfig.builder()
            .batchSize(10)
            .maxConcurrency(4)
            .retryAttempts(3)
            .qualityThreshold(0.7)
            .enableMetrics(true)
            .build();

        List<RAGDocument> documents = createLargeDocumentSet();
        
        log.info("Processing {} documents through production pipeline", documents.size());
        
        PipelineResult result = pipeline.processDocuments(documents, pipelineConfig);
        
        log.info("Pipeline Results:");
        log.info("  Total documents: {}", result.getTotalDocuments());
        log.info("  Total chunks: {}", result.getTotalChunks());
        log.info("  Processing time: {}ms", result.getProcessingTime());
        log.info("  Average quality: {:.3f}", result.getAverageQuality());
        log.info("  Error rate: {:.2f}%", result.getErrorRate() * 100);
        log.info("  Throughput: {:.2f} docs/sec", result.getThroughput());
    }

    // === Utility Methods ===

    private RAGDocument createTechnicalDocument() {
        String content = """
            # REST API Documentation
            
            ## Overview
            This API provides endpoints for managing user accounts and data.
            
            ## Authentication
            All requests require a valid API token in the Authorization header.
            
            ### Getting a Token
            ```bash
            curl -X POST https://api.example.com/auth/login \\
              -H "Content-Type: application/json" \\
              -d '{"username": "user", "password": "pass"}'
            ```
            
            ## Endpoints
            
            ### Users
            
            #### GET /users
            Retrieve a list of users.
            
            **Parameters:**
            - `limit` (optional): Maximum number of results (default: 10)
            - `offset` (optional): Number of results to skip (default: 0)
            
            **Response:**
            ```json
            {
              "users": [
                {"id": 1, "name": "John Doe", "email": "john@example.com"}
              ],
              "total": 100,
              "limit": 10,
              "offset": 0
            }
            ```
            
            #### POST /users
            Create a new user account.
            
            **Request Body:**
            ```json
            {
              "name": "Jane Smith",
              "email": "jane@example.com",
              "password": "securepassword"
            }
            ```
            """;

        return RAGDocument.builder()
            .id("tech-doc-1")
            .title("REST API Documentation")
            .content(content)
            .type(DocumentType.TECHNICAL_MANUAL)
            .sourcePath("/docs/api.md")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createAcademicDocument() {
        String content = """
            Abstract: This paper presents a novel approach to neural network optimization using 
            gradient-free methods. We demonstrate that evolutionary algorithms can effectively 
            optimize deep neural networks without relying on backpropagation.
            
            1. Introduction
            
            Deep learning has revolutionized machine learning through the use of gradient-based 
            optimization methods, particularly backpropagation. However, these methods face several 
            challenges including vanishing gradients, local minima, and sensitivity to 
            hyperparameter selection.
            
            Recent research has explored alternative optimization approaches, including evolutionary 
            algorithms, genetic programming, and reinforcement learning-based methods. These 
            gradient-free approaches offer potential advantages in terms of robustness and the 
            ability to optimize discrete parameters.
            
            2. Related Work
            
            Evolutionary algorithms have been successfully applied to neural network optimization 
            since the early days of artificial neural networks. Stanley and Miikkulainen (2002) 
            introduced NEAT (NeuroEvolution of Augmenting Topologies), which evolves both network 
            weights and topologies. More recently, researchers have applied evolution strategies 
            to modern deep learning architectures.
            
            3. Methodology
            
            Our approach combines evolution strategies with modern deep learning frameworks. 
            We use a population-based optimization method where each individual represents a 
            complete set of neural network parameters. The fitness function is based on 
            validation accuracy, with additional regularization terms to prevent overfitting.
            
            The algorithm maintains a population of candidate solutions and iteratively improves 
            them through selection, mutation, and recombination operations. We implement several 
            mutation strategies including Gaussian perturbations and structured mutations that 
            respect the network architecture.
            """;

        return RAGDocument.builder()
            .id("academic-doc-1")
            .title("Gradient-Free Neural Network Optimization")
            .content(content)
            .type(DocumentType.RESEARCH_PAPER)
            .sourcePath("/papers/nn_optimization.pdf")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createNarrativeDocument() {
        String content = """
            The old lighthouse keeper had seen many storms in his forty years of service, 
            but none quite like this one. The wind howled with an intensity that seemed to 
            shake the very foundations of the ancient stone tower, and the waves crashed 
            against the rocky shore with thunderous roars that echoed through the night.
            
            Samuel Morrison adjusted the brass lens one more time, ensuring the beam would 
            cut through the thick fog that had rolled in from the Atlantic. Ships would be 
            struggling to find their way to harbor tonight, and his light was their only 
            guide through the treacherous waters.
            
            As he climbed down the spiral staircase, his weathered hands gripping the worn 
            iron railing, Samuel reflected on the countless lives his beacon had saved over 
            the decades. Each storm brought its own challenges, its own stories of vessels 
            lost and found, of families reunited and sometimes, tragically, torn apart.
            
            The radio crackled to life in the small quarters below. "Lighthouse Station 
            Seven, this is Coast Guard Patrol Boat Delta-Five. We have a fishing vessel 
            in distress approximately two miles northeast of your position. Can you confirm 
            visibility conditions?"
            
            Samuel picked up the handset, his voice steady despite the chaos outside. 
            "Delta-Five, this is Lighthouse Seven. Visibility is severely limited, 
            approximately fifty yards in the fog. I'm maintaining full beam intensity. 
            The vessel should be able to see our light if they're within visual range."
            
            "Copy that, Seven. We're en route to assist. Keep that light burning bright."
            
            As Samuel hung up the radio, he felt the familiar weight of responsibility 
            settle on his shoulders. Tonight, like so many nights before, lives depended 
            on the steady beam that had been guiding sailors safely home for over a century.
            """;

        return RAGDocument.builder()
            .id("narrative-doc-1")
            .title("The Lighthouse Keeper's Storm")
            .content(content)
            .type(DocumentType.ARTICLE)
            .sourcePath("/stories/lighthouse_storm.txt")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createCodeDocument() {
        String content = """
            # Project Setup Guide
            
            This guide walks you through setting up the development environment 
            for the PCM Desktop application.
            
            ## Prerequisites
            
            - Java 17 or higher
            - Maven 3.8+
            - Node.js 16+ (for frontend components)
            - Docker (optional, for containerized development)
            
            ## Installation Steps
            
            ### 1. Clone the Repository
            
            ```bash
            git clone https://github.com/noteflix/pcm-desktop.git
            cd pcm-desktop
            ```
            
            ### 2. Backend Setup
            
            ```bash
            # Install dependencies
            mvn clean install
            
            # Run tests
            mvn test
            
            # Start the application
            mvn spring-boot:run
            ```
            
            ### 3. Frontend Setup
            
            ```bash
            cd frontend
            npm install
            npm run dev
            ```
            
            ### 4. Database Configuration
            
            Create a `application-local.properties` file:
            
            ```properties
            # Database settings
            spring.datasource.url=jdbc:postgresql://localhost:5432/pcm_dev
            spring.datasource.username=dev_user
            spring.datasource.password=dev_password
            
            # JPA settings
            spring.jpa.hibernate.ddl-auto=update
            spring.jpa.show-sql=true
            
            # Logging
            logging.level.com.noteflix.pcm=DEBUG
            ```
            
            ## Development Workflow
            
            1. Create a feature branch: `git checkout -b feature/your-feature`
            2. Make your changes and write tests
            3. Run the full test suite: `mvn verify`
            4. Submit a pull request
            
            ## Troubleshooting
            
            ### Common Issues
            
            **Port already in use:**
            ```bash
            # Find process using port 8080
            lsof -i :8080
            kill -9 <PID>
            ```
            
            **Database connection errors:**
            - Ensure PostgreSQL is running
            - Check connection parameters in application.properties
            - Verify database exists and user has permissions
            """;

        return RAGDocument.builder()
            .id("code-doc-1")
            .title("Development Setup Guide")
            .content(content)
            .type(DocumentType.TECHNICAL_MANUAL)
            .sourcePath("/docs/setup.md")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private RAGDocument createVariedDocument(int index) {
        String[] contents = {
            "Short document content for testing batch processing speed and efficiency.",
            "Medium length document with multiple sentences and paragraphs. This content is designed to test different chunking strategies under various conditions and document characteristics.",
            "Much longer document content with extensive text that spans multiple topics and themes. This type of content is commonly found in academic papers, technical documentation, and comprehensive guides. It provides an excellent test case for semantic chunking algorithms that need to identify topical boundaries and maintain coherence across related concepts."
        };

        String content = contents[index % contents.length];
        
        return RAGDocument.builder()
            .id("varied-doc-" + index)
            .title("Test Document " + index)
            .content(content)
            .type(DocumentType.ARTICLE)
            .sourcePath("/test/doc_" + index + ".txt")
            .indexedAt(LocalDateTime.now())
            .build();
    }

    private List<RAGDocument> createLargeDocumentSet() {
        return List.of(
            createTechnicalDocument(),
            createAcademicDocument(),
            createNarrativeDocument(),
            createCodeDocument(),
            createVariedDocument(0),
            createVariedDocument(1),
            createVariedDocument(2)
        );
    }

    private static double calculateAverageQuality(List<DocumentChunk> chunks) {
        return chunks.stream()
            .mapToDouble(chunk -> chunk.getQualityScore() != null ? chunk.getQualityScore() : 0.0)
            .average()
            .orElse(0.0);
    }

    private void printSampleChunk(DocumentChunk chunk) {
        log.info("Sample chunk: {}", chunk.getChunkId());
        log.info("  Content preview: {}", chunk.getContentPreview(100));
        log.info("  Size: {} chars", chunk.getChunkSizeChars());
        log.info("  Quality: {:.3f}", chunk.getQualityScore() != null ? chunk.getQualityScore() : 0.0);
        
        if (chunk.getMetadata() != null && !chunk.getMetadata().isEmpty()) {
            log.info("  Metadata: {}", chunk.getMetadata());
        }
    }

    private void analyzeChunkCharacteristics(List<DocumentChunk> chunks, String category) {
        int totalChars = chunks.stream().mapToInt(DocumentChunk::getChunkSizeChars).sum();
        double avgSize = (double) totalChars / chunks.size();
        double avgQuality = calculateAverageQuality(chunks);
        
        int minSize = chunks.stream().mapToInt(DocumentChunk::getChunkSizeChars).min().orElse(0);
        int maxSize = chunks.stream().mapToInt(DocumentChunk::getChunkSizeChars).max().orElse(0);
        
        log.info("{} chunk characteristics:", category);
        log.info("  Count: {}", chunks.size());
        log.info("  Average size: {:.1f} chars", avgSize);
        log.info("  Size range: {} - {} chars", minSize, maxSize);
        log.info("  Average quality: {:.3f}", avgQuality);
        log.info("  Total content: {} chars", totalChars);
    }

    // === Mock and Helper Classes ===

    private static class MockEmbeddingService implements EmbeddingService {
        @Override
        public float[] embed(String text) {
            // Simple mock embedding based on text characteristics
            float[] embedding = new float[384];
            int hash = text.hashCode();
            for (int i = 0; i < 384; i++) {
                embedding[i] = (float) Math.sin(hash + i) * 0.1f;
            }
            return embedding;
        }

        @Override
        public float[][] embedBatch(String[] texts) {
            float[][] embeddings = new float[texts.length][];
            for (int i = 0; i < texts.length; i++) {
                embeddings[i] = embed(texts[i]);
            }
            return embeddings;
        }

        @Override
        public String getModelName() {
            return "mock-embedding-model";
        }

        @Override
        public int getDimension() {
            return 384;
        }
    }

    private static class RobustChunkingService {
        public List<DocumentChunk> chunkDocumentSafely(RAGDocument document) {
            try {
                // Try optimal strategy first
                ChunkingConfig config = ChunkingConfig.defaults();
                ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
                    document, config, new MockEmbeddingService());
                return strategy.chunk(document);
            } catch (Exception e) {
                log.warn("Primary chunking failed, using fallback: {}", e.getMessage());
                
                try {
                    // Fallback to sentence-aware
                    return SentenceAwareChunking.defaults().chunk(document);
                } catch (Exception e2) {
                    log.error("Sentence-aware fallback failed, using fixed-size: {}", e2.getMessage());
                    
                    // Last resort: fixed-size chunking
                    return new FixedSizeChunking(1000, 200).chunk(document);
                }
            }
        }
    }

    private static class CachedChunkingService {
        private final EmbeddingService embeddingService;
        private ChunkingStrategy cachedStrategy;

        public CachedChunkingService(EmbeddingService embeddingService) {
            this.embeddingService = embeddingService;
        }

        public List<DocumentChunk> chunkWithCaching(RAGDocument document) {
            if (cachedStrategy == null) {
                log.info("Creating and caching chunking strategy");
                ChunkingConfig config = ChunkingConfig.forTechnicalDocs();
                cachedStrategy = ChunkingFactory.createStrategy(config);
            } else {
                log.info("Using cached chunking strategy");
            }
            
            return cachedStrategy.chunk(document);
        }
    }

    private static class ProductionChunkingPipeline {
        private final EmbeddingService embeddingService;

        public ProductionChunkingPipeline(EmbeddingService embeddingService) {
            this.embeddingService = embeddingService;
        }

        public PipelineResult processDocuments(List<RAGDocument> documents, PipelineConfig config) {
            long startTime = System.currentTimeMillis();
            int totalChunks = 0;
            int errors = 0;
            double totalQuality = 0.0;

            for (RAGDocument doc : documents) {
                try {
                    ChunkingStrategy strategy = ChunkingFactory.createOptimalStrategy(
                        doc, ChunkingConfig.defaults(), embeddingService);
                    List<DocumentChunk> chunks = strategy.chunk(doc);
                    
                    totalChunks += chunks.size();
                    totalQuality += calculateAverageQuality(chunks);
                } catch (Exception e) {
                    errors++;
                    log.error("Failed to process document {}: {}", doc.getId(), e.getMessage());
                }
            }

            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            
            return new PipelineResult(
                documents.size(),
                totalChunks,
                processingTime,
                totalQuality / Math.max(1, documents.size() - errors),
                (double) errors / documents.size(),
                (double) documents.size() / (processingTime / 1000.0)
            );
        }
    }

    private static class PipelineConfig {
        private final int batchSize;
        private final int maxConcurrency;
        private final int retryAttempts;
        private final double qualityThreshold;
        private final boolean enableMetrics;

        private PipelineConfig(Builder builder) {
            this.batchSize = builder.batchSize;
            this.maxConcurrency = builder.maxConcurrency;
            this.retryAttempts = builder.retryAttempts;
            this.qualityThreshold = builder.qualityThreshold;
            this.enableMetrics = builder.enableMetrics;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int batchSize = 10;
            private int maxConcurrency = 4;
            private int retryAttempts = 3;
            private double qualityThreshold = 0.7;
            private boolean enableMetrics = true;

            public Builder batchSize(int batchSize) {
                this.batchSize = batchSize;
                return this;
            }

            public Builder maxConcurrency(int maxConcurrency) {
                this.maxConcurrency = maxConcurrency;
                return this;
            }

            public Builder retryAttempts(int retryAttempts) {
                this.retryAttempts = retryAttempts;
                return this;
            }

            public Builder qualityThreshold(double qualityThreshold) {
                this.qualityThreshold = qualityThreshold;
                return this;
            }

            public Builder enableMetrics(boolean enableMetrics) {
                this.enableMetrics = enableMetrics;
                return this;
            }

            public PipelineConfig build() {
                return new PipelineConfig(this);
            }
        }

        // Getters
        public int getBatchSize() { return batchSize; }
        public int getMaxConcurrency() { return maxConcurrency; }
        public int getRetryAttempts() { return retryAttempts; }
        public double getQualityThreshold() { return qualityThreshold; }
        public boolean isEnableMetrics() { return enableMetrics; }
    }

    private static class PipelineResult {
        private final int totalDocuments;
        private final int totalChunks;
        private final long processingTime;
        private final double averageQuality;
        private final double errorRate;
        private final double throughput;

        public PipelineResult(int totalDocuments, int totalChunks, long processingTime,
                            double averageQuality, double errorRate, double throughput) {
            this.totalDocuments = totalDocuments;
            this.totalChunks = totalChunks;
            this.processingTime = processingTime;
            this.averageQuality = averageQuality;
            this.errorRate = errorRate;
            this.throughput = throughput;
        }

        // Getters
        public int getTotalDocuments() { return totalDocuments; }
        public int getTotalChunks() { return totalChunks; }
        public long getProcessingTime() { return processingTime; }
        public double getAverageQuality() { return averageQuality; }
        public double getErrorRate() { return errorRate; }
        public double getThroughput() { return throughput; }
    }

    // === Main method to run examples ===
    public static void main(String[] args) {
        ChunkingExamples examples = new ChunkingExamples();
        
        try {
            examples.basicChunkingWorkflow();
            examples.automaticStrategySelection();
            examples.useCaseSpecificConfigurations();
            examples.advancedConfiguration();
            examples.qualityAssessmentAndFallback();
            examples.performanceOptimization();
            examples.productionPipeline();
            
            log.info("\n=== Examples completed successfully! ===");
        } catch (Exception e) {
            log.error("Error running examples", e);
        }
    }
}