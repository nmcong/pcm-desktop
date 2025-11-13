package com.noteflix.pcm.rag.chunking;

import com.noteflix.pcm.rag.embedding.EmbeddingService;
import com.noteflix.pcm.rag.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ChunkingConfig class and its nested configurations.
 * 
 * Tests cover:
 * - Configuration validation
 * - Builder pattern functionality
 * - Factory methods
 * - Document-type specific configurations
 * - Nested configuration classes
 */
@ExtendWith(MockitoExtension.class)
public class ChunkingConfigTest {

    @Mock
    private EmbeddingService mockEmbeddingService;

    @Test
    void testDefaultConfiguration() {
        ChunkingConfig config = ChunkingConfig.defaults();

        assertThat(config.getPrimaryStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE);
        assertThat(config.getFallbackStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE);
        assertThat(config.getTargetChunkSize()).isEqualTo(1000);
        assertThat(config.getMinChunkSize()).isEqualTo(200);
        assertThat(config.getMaxChunkSize()).isEqualTo(2000);
        assertThat(config.getOverlapSize()).isEqualTo(200);
        assertThat(config.isAutoSelectStrategy()).isTrue();
        assertThat(config.getMinQualityThreshold()).isEqualTo(0.3);
        assertThat(config.getPreferredQualityThreshold()).isEqualTo(0.7);
        assertThat(config.isEnableQualityFallback()).isTrue();
    }

    @Test
    void testBuilderPattern() {
        ChunkingConfig config = ChunkingConfig.builder()
            .targetChunkSize(1500)
            .minChunkSize(300)
            .maxChunkSize(2500)
            .overlapSize(250)
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SEMANTIC)
            .fallbackStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE)
            .autoSelectStrategy(false)
            .minQualityThreshold(0.5)
            .preferredQualityThreshold(0.8)
            .enableQualityFallback(false)
            .preserveMetadata(false)
            .generateQualityMetrics(false)
            .maxChunksPerDocument(50)
            .build();

        assertThat(config.getTargetChunkSize()).isEqualTo(1500);
        assertThat(config.getMinChunkSize()).isEqualTo(300);
        assertThat(config.getMaxChunkSize()).isEqualTo(2500);
        assertThat(config.getOverlapSize()).isEqualTo(250);
        assertThat(config.getPrimaryStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.SEMANTIC);
        assertThat(config.getFallbackStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE);
        assertThat(config.isAutoSelectStrategy()).isFalse();
        assertThat(config.getMinQualityThreshold()).isEqualTo(0.5);
        assertThat(config.getPreferredQualityThreshold()).isEqualTo(0.8);
        assertThat(config.isEnableQualityFallback()).isFalse();
        assertThat(config.isPreserveMetadata()).isFalse();
        assertThat(config.isGenerateQualityMetrics()).isFalse();
        assertThat(config.getMaxChunksPerDocument()).isEqualTo(50);
    }

    @Test
    void testConfigurationValidation() {
        // Valid configuration should not throw
        ChunkingConfig.builder()
            .targetChunkSize(1000)
            .minChunkSize(200)
            .maxChunkSize(2000)
            .overlapSize(100)
            .minQualityThreshold(0.3)
            .preferredQualityThreshold(0.7)
            .build()
            .validate();

        // Invalid target chunk size
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .targetChunkSize(-100)
                .build()
                .validate();
        });

        // Invalid min chunk size (negative)
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .minChunkSize(-50)
                .build()
                .validate();
        });

        // Invalid min chunk size (greater than target)
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .targetChunkSize(1000)
                .minChunkSize(1500)
                .build()
                .validate();
        });

        // Invalid max chunk size (less than target)
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .targetChunkSize(1000)
                .maxChunkSize(800)
                .build()
                .validate();
        });

        // Invalid overlap size (negative)
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .overlapSize(-50)
                .build()
                .validate();
        });

        // Invalid overlap size (greater than target)
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .targetChunkSize(1000)
                .overlapSize(1200)
                .build()
                .validate();
        });

        // Invalid quality threshold (negative)
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .minQualityThreshold(-0.1)
                .build()
                .validate();
        });

        // Invalid quality threshold (greater than 1.0)
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .minQualityThreshold(1.5)
                .build()
                .validate();
        });

        // Invalid preferred quality threshold (less than min)
        assertThrows(IllegalArgumentException.class, () -> {
            ChunkingConfig.builder()
                .minQualityThreshold(0.7)
                .preferredQualityThreshold(0.5)
                .build()
                .validate();
        });
    }

    @Test
    void testFactoryMethods() {
        // Technical documentation config
        ChunkingConfig techConfig = ChunkingConfig.forTechnicalDocs();
        assertThat(techConfig.getPrimaryStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.MARKDOWN_AWARE);
        assertThat(techConfig.getFallbackStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE);
        assertThat(techConfig.getTargetChunkSize()).isEqualTo(1500);
        assertThat(techConfig.getMaxChunkSize()).isEqualTo(2500);
        assertThat(techConfig.getMarkdownConfig().isPreserveCodeBlocks()).isTrue();

        // Narrative content config
        ChunkingConfig narrativeConfig = ChunkingConfig.forNarrativeContent();
        assertThat(narrativeConfig.getPrimaryStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE);
        assertThat(narrativeConfig.getFallbackStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE);
        assertThat(narrativeConfig.getTargetChunkSize()).isEqualTo(1200);
        assertThat(narrativeConfig.getPreferredQualityThreshold()).isEqualTo(0.8);
        assertThat(narrativeConfig.getSentenceAwareConfig().getSizeTolerance()).isEqualTo(0.5);

        // Academic papers config
        ChunkingConfig academicConfig = ChunkingConfig.forAcademicPapers(mockEmbeddingService);
        assertThat(academicConfig.getPrimaryStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.SEMANTIC);
        assertThat(academicConfig.getFallbackStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE);
        assertThat(academicConfig.getTargetChunkSize()).isEqualTo(1800);
        assertThat(academicConfig.getMaxChunkSize()).isEqualTo(3000);
        assertThat(academicConfig.getSemanticConfig().getEmbeddingService()).isEqualTo(mockEmbeddingService);
        assertThat(academicConfig.getSemanticConfig().getSimilarityThreshold()).isEqualTo(0.85);

        // High volume processing config
        ChunkingConfig highVolumeConfig = ChunkingConfig.forHighVolume();
        assertThat(highVolumeConfig.getPrimaryStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE);
        assertThat(highVolumeConfig.getFallbackStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE);
        assertThat(highVolumeConfig.isAutoSelectStrategy()).isFalse();
        assertThat(highVolumeConfig.isGenerateQualityMetrics()).isFalse();
        assertThat(highVolumeConfig.getTargetChunkSize()).isEqualTo(800);
        assertThat(highVolumeConfig.getOverlapSize()).isEqualTo(100);
    }

    @Test
    void testDocumentTypeConfigurations() {
        ChunkingConfig baseConfig = ChunkingConfig.defaults();

        // Add document-type specific config
        ChunkingConfig techConfig = ChunkingConfig.forTechnicalDocs();
        baseConfig.addDocumentTypeConfig(DocumentType.TECHNICAL_MANUAL, techConfig);

        ChunkingConfig academicConfig = ChunkingConfig.forAcademicPapers(mockEmbeddingService);
        baseConfig.addDocumentTypeConfig(DocumentType.RESEARCH_PAPER, academicConfig);

        // Test retrieval
        ChunkingConfig retrievedTechConfig = baseConfig.getConfigForDocumentType(DocumentType.TECHNICAL_MANUAL);
        assertThat(retrievedTechConfig).isEqualTo(techConfig);
        assertThat(retrievedTechConfig.getPrimaryStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.MARKDOWN_AWARE);

        ChunkingConfig retrievedAcademicConfig = baseConfig.getConfigForDocumentType(DocumentType.RESEARCH_PAPER);
        assertThat(retrievedAcademicConfig).isEqualTo(academicConfig);
        assertThat(retrievedAcademicConfig.getPrimaryStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.SEMANTIC);

        // Test fallback to base config for unknown types
        ChunkingConfig unknownTypeConfig = baseConfig.getConfigForDocumentType(DocumentType.ARTICLE);
        assertThat(unknownTypeConfig).isEqualTo(baseConfig);
    }

    @Test
    void testCustomParameters() {
        ChunkingConfig config = ChunkingConfig.builder().build();

        // Initially empty
        assertThat(config.getCustomParameters()).isEmpty();

        // Add custom parameters
        config.getCustomParameters().put("custom_threshold", 0.85);
        config.getCustomParameters().put("special_mode", "academic");
        config.getCustomParameters().put("batch_size", 100);

        assertThat(config.getCustomParameters()).hasSize(3);
        assertThat(config.getCustomParameters().get("custom_threshold")).isEqualTo(0.85);
        assertThat(config.getCustomParameters().get("special_mode")).isEqualTo("academic");
        assertThat(config.getCustomParameters().get("batch_size")).isEqualTo(100);
    }

    // === Nested Configuration Classes Tests ===

    @Test
    void testSentenceAwareConfig() {
        // Default configuration
        ChunkingConfig.SentenceAwareConfig defaultConfig = ChunkingConfig.SentenceAwareConfig.defaults();
        assertThat(defaultConfig.getSizeTolerance()).isEqualTo(0.3);
        assertThat(defaultConfig.isStrictPunctuation()).isFalse();

        // Strict configuration
        ChunkingConfig.SentenceAwareConfig strictConfig = ChunkingConfig.SentenceAwareConfig.strict();
        assertThat(strictConfig.getSizeTolerance()).isEqualTo(0.1);
        assertThat(strictConfig.isStrictPunctuation()).isTrue();

        // Flexible configuration
        ChunkingConfig.SentenceAwareConfig flexibleConfig = ChunkingConfig.SentenceAwareConfig.flexible();
        assertThat(flexibleConfig.getSizeTolerance()).isEqualTo(0.5);
        assertThat(flexibleConfig.isStrictPunctuation()).isFalse();

        // Custom configuration
        ChunkingConfig.SentenceAwareConfig customConfig = ChunkingConfig.SentenceAwareConfig.builder()
            .sizeTolerance(0.25)
            .strictPunctuation(true)
            .build();
        assertThat(customConfig.getSizeTolerance()).isEqualTo(0.25);
        assertThat(customConfig.isStrictPunctuation()).isTrue();
    }

    @Test
    void testSemanticConfig() {
        // Default configuration
        ChunkingConfig.SemanticConfig defaultConfig = ChunkingConfig.SemanticConfig.defaults();
        assertThat(defaultConfig.getEmbeddingService()).isNull();
        assertThat(defaultConfig.getSimilarityThreshold()).isEqualTo(0.75);
        assertThat(defaultConfig.getSlidingWindowSize()).isEqualTo(3);
        assertThat(defaultConfig.isEnableBatchProcessing()).isTrue();

        // Precise configuration
        ChunkingConfig.SemanticConfig preciseConfig = ChunkingConfig.SemanticConfig.precise(mockEmbeddingService);
        assertThat(preciseConfig.getEmbeddingService()).isEqualTo(mockEmbeddingService);
        assertThat(preciseConfig.getSimilarityThreshold()).isEqualTo(0.85);
        assertThat(preciseConfig.getSlidingWindowSize()).isEqualTo(2);

        // Flexible configuration
        ChunkingConfig.SemanticConfig flexibleConfig = ChunkingConfig.SemanticConfig.flexible(mockEmbeddingService);
        assertThat(flexibleConfig.getEmbeddingService()).isEqualTo(mockEmbeddingService);
        assertThat(flexibleConfig.getSimilarityThreshold()).isEqualTo(0.65);
        assertThat(flexibleConfig.getSlidingWindowSize()).isEqualTo(4);

        // Custom configuration
        ChunkingConfig.SemanticConfig customConfig = ChunkingConfig.SemanticConfig.builder()
            .embeddingService(mockEmbeddingService)
            .similarityThreshold(0.8)
            .slidingWindowSize(5)
            .enableBatchProcessing(false)
            .build();
        assertThat(customConfig.getEmbeddingService()).isEqualTo(mockEmbeddingService);
        assertThat(customConfig.getSimilarityThreshold()).isEqualTo(0.8);
        assertThat(customConfig.getSlidingWindowSize()).isEqualTo(5);
        assertThat(customConfig.isEnableBatchProcessing()).isFalse();
    }

    @Test
    void testMarkdownConfig() {
        // Default configuration
        ChunkingConfig.MarkdownConfig defaultConfig = ChunkingConfig.MarkdownConfig.defaults();
        assertThat(defaultConfig.isPreserveCodeBlocks()).isTrue();
        assertThat(defaultConfig.isRespectHeaders()).isTrue();
        assertThat(defaultConfig.getMaxHeaderLevel()).isEqualTo(3);
        assertThat(defaultConfig.isPreserveTables()).isTrue();
        assertThat(defaultConfig.isPreserveLists()).isTrue();

        // Header-focused configuration
        ChunkingConfig.MarkdownConfig headerConfig = ChunkingConfig.MarkdownConfig.headerFocused();
        assertThat(headerConfig.isRespectHeaders()).isTrue();
        assertThat(headerConfig.getMaxHeaderLevel()).isEqualTo(2);
        assertThat(headerConfig.isPreserveCodeBlocks()).isTrue();

        // Code-preserving configuration
        ChunkingConfig.MarkdownConfig codeConfig = ChunkingConfig.MarkdownConfig.codePreserving();
        assertThat(codeConfig.isPreserveCodeBlocks()).isTrue();
        assertThat(codeConfig.isPreserveTables()).isTrue();
        assertThat(codeConfig.isRespectHeaders()).isFalse();
        assertThat(codeConfig.getMaxHeaderLevel()).isEqualTo(4);

        // Custom configuration
        ChunkingConfig.MarkdownConfig customConfig = ChunkingConfig.MarkdownConfig.builder()
            .preserveCodeBlocks(false)
            .respectHeaders(true)
            .maxHeaderLevel(5)
            .preserveTables(false)
            .preserveLists(false)
            .build();
        assertThat(customConfig.isPreserveCodeBlocks()).isFalse();
        assertThat(customConfig.isRespectHeaders()).isTrue();
        assertThat(customConfig.getMaxHeaderLevel()).isEqualTo(5);
        assertThat(customConfig.isPreserveTables()).isFalse();
        assertThat(customConfig.isPreserveLists()).isFalse();
    }

    @Test
    void testFixedSizeConfig() {
        // Default configuration
        ChunkingConfig.FixedSizeConfig defaultConfig = ChunkingConfig.FixedSizeConfig.defaults();
        assertThat(defaultConfig.isWordBoundaryAware()).isFalse();
        assertThat(defaultConfig.isPreserveParagraphs()).isFalse();

        // Word-aware configuration
        ChunkingConfig.FixedSizeConfig wordAwareConfig = ChunkingConfig.FixedSizeConfig.wordAware();
        assertThat(wordAwareConfig.isWordBoundaryAware()).isTrue();
        assertThat(wordAwareConfig.isPreserveParagraphs()).isTrue();

        // Custom configuration
        ChunkingConfig.FixedSizeConfig customConfig = ChunkingConfig.FixedSizeConfig.builder()
            .wordBoundaryAware(true)
            .preserveParagraphs(false)
            .build();
        assertThat(customConfig.isWordBoundaryAware()).isTrue();
        assertThat(customConfig.isPreserveParagraphs()).isFalse();
    }

    @Test
    void testChunkingStrategyTypeEnum() {
        // Test enum values
        ChunkingConfig.ChunkingStrategyType[] types = ChunkingConfig.ChunkingStrategyType.values();
        assertThat(types).hasSize(4);
        assertThat(types).contains(
            ChunkingConfig.ChunkingStrategyType.FIXED_SIZE,
            ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE,
            ChunkingConfig.ChunkingStrategyType.SEMANTIC,
            ChunkingConfig.ChunkingStrategyType.MARKDOWN_AWARE
        );

        // Test descriptions
        assertThat(ChunkingConfig.ChunkingStrategyType.FIXED_SIZE.getDescription())
            .isEqualTo("Fixed-size chunking with predictable chunk sizes");
        assertThat(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE.getDescription())
            .isEqualTo("Sentence-aware chunking preserving sentence boundaries");
        assertThat(ChunkingConfig.ChunkingStrategyType.SEMANTIC.getDescription())
            .isEqualTo("Semantic chunking using embeddings for topical coherence");
        assertThat(ChunkingConfig.ChunkingStrategyType.MARKDOWN_AWARE.getDescription())
            .isEqualTo("Markdown-aware chunking respecting document structure");
    }

    @Test
    void testConfigurationMerging() {
        // Test that document-type configs properly override base config
        ChunkingConfig baseConfig = ChunkingConfig.builder()
            .targetChunkSize(1000)
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE)
            .build();

        ChunkingConfig techConfig = ChunkingConfig.builder()
            .targetChunkSize(1500)
            .primaryStrategy(ChunkingConfig.ChunkingStrategyType.MARKDOWN_AWARE)
            .build();

        baseConfig.addDocumentTypeConfig(DocumentType.TECHNICAL_MANUAL, techConfig);

        // Base config should remain unchanged
        assertThat(baseConfig.getTargetChunkSize()).isEqualTo(1000);
        assertThat(baseConfig.getPrimaryStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.SENTENCE_AWARE);

        // Retrieved tech config should have overridden values
        ChunkingConfig retrievedConfig = baseConfig.getConfigForDocumentType(DocumentType.TECHNICAL_MANUAL);
        assertThat(retrievedConfig.getTargetChunkSize()).isEqualTo(1500);
        assertThat(retrievedConfig.getPrimaryStrategy()).isEqualTo(ChunkingConfig.ChunkingStrategyType.MARKDOWN_AWARE);
    }

    @Test
    void testConfigurationImmutability() {
        // Test that nested configs are properly isolated
        ChunkingConfig config = ChunkingConfig.defaults();
        
        ChunkingConfig.SentenceAwareConfig originalSentenceConfig = config.getSentenceAwareConfig();
        double originalTolerance = originalSentenceConfig.getSizeTolerance();

        // Modifying the config should not affect the original
        ChunkingConfig modifiedConfig = ChunkingConfig.builder()
            .sentenceAwareConfig(ChunkingConfig.SentenceAwareConfig.builder()
                .sizeTolerance(0.9)
                .build())
            .build();

        assertThat(originalSentenceConfig.getSizeTolerance()).isEqualTo(originalTolerance);
        assertThat(modifiedConfig.getSentenceAwareConfig().getSizeTolerance()).isEqualTo(0.9);
    }
}