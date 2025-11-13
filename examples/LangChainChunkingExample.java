package examples;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.core.ChunkingFactory;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.chunking.langchain.LangChainAdapter;
import com.noteflix.pcm.rag.chunking.langchain.LangChainConfig;
import com.noteflix.pcm.rag.chunking.langchain.splitters.CharacterTextSplitter;
import com.noteflix.pcm.rag.chunking.langchain.splitters.RecursiveCharacterTextSplitter;
import com.noteflix.pcm.rag.chunking.langchain.splitters.TokenTextSplitter;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.DocumentType;

import java.util.List;

/**
 * Examples demonstrating LangChain integration with PCM chunking system.
 * 
 * This class shows various ways to use LangChain text splitters within
 * the PCM framework, both through the factory pattern and direct usage.
 */
public class LangChainChunkingExample {

    public static void main(String[] args) {
        // Sample document for demonstration
        RAGDocument document = createSampleDocument();
        
        System.out.println("=== LangChain Integration Examples ===\n");
        
        // Example 1: Using LangChain strategies through PCM Factory
        factoryBasedExamples(document);
        
        // Example 2: Direct usage of LangChain splitters
        directUsageExamples(document);
        
        // Example 3: Custom configuration examples
        customConfigurationExamples(document);
        
        // Example 4: Strategy comparison
        strategyComparisonExample(document);
        
        // Example 5: Use case specific examples
        useCaseExamples(document);
    }

    /**
     * Examples using LangChain strategies through PCM ChunkingFactory.
     */
    private static void factoryBasedExamples(RAGDocument document) {
        System.out.println("1. Factory-Based LangChain Usage:\n");
        
        // Character Text Splitter through factory
        ChunkingConfig charConfig = ChunkingConfig.forLangChainCharacter();
        ChunkingStrategy charStrategy = ChunkingFactory.createStrategy(charConfig);
        List<DocumentChunk> charChunks = charStrategy.chunk(document);
        
        System.out.printf("Character Splitter: %d chunks created\n", charChunks.size());
        printFirstChunk("Character", charChunks);
        
        // Recursive Character Text Splitter through factory
        ChunkingConfig recursiveConfig = ChunkingConfig.forLangChainRecursive();
        ChunkingStrategy recursiveStrategy = ChunkingFactory.createStrategy(recursiveConfig);
        List<DocumentChunk> recursiveChunks = recursiveStrategy.chunk(document);
        
        System.out.printf("Recursive Splitter: %d chunks created\n", recursiveChunks.size());
        printFirstChunk("Recursive", recursiveChunks);
        
        // Token Text Splitter through factory
        ChunkingConfig tokenConfig = ChunkingConfig.forLangChainToken("gpt-3.5-turbo");
        ChunkingStrategy tokenStrategy = ChunkingFactory.createStrategy(tokenConfig);
        List<DocumentChunk> tokenChunks = tokenStrategy.chunk(document);
        
        System.out.printf("Token Splitter: %d chunks created\n", tokenChunks.size());
        printFirstChunk("Token", tokenChunks);
        
        // Code Splitter through factory
        ChunkingConfig codeConfig = ChunkingConfig.forLangChainCode("java");
        ChunkingStrategy codeStrategy = ChunkingFactory.createStrategy(codeConfig);
        List<DocumentChunk> codeChunks = codeStrategy.chunk(document);
        
        System.out.printf("Code Splitter: %d chunks created\n", codeChunks.size());
        printFirstChunk("Code", codeChunks);
        
        System.out.println();
    }

    /**
     * Examples using LangChain splitters directly.
     */
    private static void directUsageExamples(RAGDocument document) {
        System.out.println("2. Direct LangChain Splitter Usage:\n");
        
        // Direct Character Text Splitter usage
        CharacterTextSplitter charSplitter = CharacterTextSplitter.forParagraphs(500, 50);
        LangChainAdapter charAdapter = new LangChainAdapter(charSplitter, LangChainConfig.defaults());
        List<DocumentChunk> directCharChunks = charAdapter.chunk(document);
        
        System.out.printf("Direct Character Splitter: %d chunks\n", directCharChunks.size());
        
        // Direct Recursive Text Splitter usage
        RecursiveCharacterTextSplitter recursiveSplitter = 
                RecursiveCharacterTextSplitter.forMarkdown(600, 60);
        LangChainAdapter recursiveAdapter = new LangChainAdapter(recursiveSplitter, LangChainConfig.defaults());
        List<DocumentChunk> directRecursiveChunks = recursiveAdapter.chunk(document);
        
        System.out.printf("Direct Recursive Splitter: %d chunks\n", directRecursiveChunks.size());
        
        // Direct Token Text Splitter usage
        TokenTextSplitter tokenSplitter = TokenTextSplitter.forGPT4(200, 40);
        LangChainAdapter tokenAdapter = new LangChainAdapter(tokenSplitter, LangChainConfig.defaults());
        List<DocumentChunk> directTokenChunks = tokenAdapter.chunk(document);
        
        System.out.printf("Direct Token Splitter: %d chunks\n", directTokenChunks.size());
        
        System.out.println();
    }

    /**
     * Examples with custom LangChain configurations.
     */
    private static void customConfigurationExamples(RAGDocument document) {
        System.out.println("3. Custom Configuration Examples:\n");
        
        // Custom Character Splitter Configuration
        LangChainConfig customCharConfig = LangChainConfig.builder()
                .chunkSize(300)
                .chunkOverlap(30)
                .stripWhitespace(true)
                .keepSeparator(false)
                .characterConfig(LangChainConfig.CharacterTextSplitterConfig.builder()
                    .separator("\n")
                    .isSeparatorRegex(false)
                    .build())
                .build();
        
        CharacterTextSplitter customCharSplitter = new CharacterTextSplitter(customCharConfig);
        LangChainAdapter customCharAdapter = new LangChainAdapter(customCharSplitter, customCharConfig);
        List<DocumentChunk> customCharChunks = customCharAdapter.chunk(document);
        
        System.out.printf("Custom Character Config: %d chunks (line-based)\n", customCharChunks.size());
        
        // Custom Recursive Splitter Configuration
        String[] customSeparators = {
            "\n## ",      // Markdown H2
            "\n### ",     // Markdown H3
            "\n\n",       // Paragraphs
            "\n",         // Lines
            ". ",         // Sentences
            " ",          // Words
            ""            // Characters
        };
        
        LangChainConfig customRecursiveConfig = LangChainConfig.builder()
                .chunkSize(400)
                .chunkOverlap(40)
                .recursiveConfig(LangChainConfig.RecursiveCharacterTextSplitterConfig.builder()
                    .separators(customSeparators)
                    .keepSeparator(true)
                    .isSeparatorRegex(false)
                    .build())
                .build();
        
        RecursiveCharacterTextSplitter customRecursiveSplitter = 
                new RecursiveCharacterTextSplitter(customRecursiveConfig);
        LangChainAdapter customRecursiveAdapter = 
                new LangChainAdapter(customRecursiveSplitter, customRecursiveConfig);
        List<DocumentChunk> customRecursiveChunks = customRecursiveAdapter.chunk(document);
        
        System.out.printf("Custom Recursive Config: %d chunks (markdown-aware)\n", customRecursiveChunks.size());
        
        // Custom Token Splitter Configuration
        LangChainConfig customTokenConfig = LangChainConfig.builder()
                .chunkSize(150)
                .chunkOverlap(30)
                .tokenConfig(LangChainConfig.TokenTextSplitterConfig.builder()
                    .modelName("gpt-4")
                    .encodingName("cl100k_base")
                    .build())
                .build();
        
        TokenTextSplitter customTokenSplitter = new TokenTextSplitter(customTokenConfig);
        LangChainAdapter customTokenAdapter = new LangChainAdapter(customTokenSplitter, customTokenConfig);
        List<DocumentChunk> customTokenChunks = customTokenAdapter.chunk(document);
        
        System.out.printf("Custom Token Config: %d chunks (GPT-4 optimized)\n", customTokenChunks.size());
        
        System.out.println();
    }

    /**
     * Example comparing different LangChain strategies.
     */
    private static void strategyComparisonExample(RAGDocument document) {
        System.out.println("4. Strategy Comparison:\n");
        
        // Get all recommendations including LangChain strategies
        List<ChunkingFactory.StrategyRecommendation> recommendations = 
                ChunkingFactory.getAllRecommendations(document, ChunkingConfig.defaults(), null);
        
        System.out.println("Strategy Quality Scores:");
        recommendations.stream()
                .filter(rec -> rec.strategyType.name().startsWith("LANGCHAIN"))
                .forEach(rec -> 
                    System.out.printf("  %s: %.3f\n", 
                        rec.strategyType, rec.expectedQuality));
        
        // Find best LangChain strategy
        ChunkingFactory.StrategyRecommendation bestLangChain = recommendations.stream()
                .filter(rec -> rec.strategyType.name().startsWith("LANGCHAIN"))
                .findFirst()
                .orElse(null);
        
        if (bestLangChain != null) {
            System.out.printf("\nBest LangChain Strategy: %s (quality: %.3f)\n", 
                    bestLangChain.strategyType, bestLangChain.expectedQuality);
            
            List<DocumentChunk> bestChunks = bestLangChain.strategy.chunk(document);
            System.out.printf("Produced %d chunks\n", bestChunks.size());
        }
        
        System.out.println();
    }

    /**
     * Examples using LangChain-specific use cases.
     */
    private static void useCaseExamples(RAGDocument document) {
        System.out.println("5. Use Case Examples:\n");
        
        // LangChain Compatible Use Case
        ChunkingStrategy langchainCompatible = ChunkingFactory.createForUseCase(
                ChunkingFactory.UseCase.LANGCHAIN_COMPATIBLE, null);
        List<DocumentChunk> compatibleChunks = langchainCompatible.chunk(document);
        System.out.printf("LangChain Compatible: %d chunks\n", compatibleChunks.size());
        
        // Token Optimized Use Case
        ChunkingStrategy tokenOptimized = ChunkingFactory.createForUseCase(
                ChunkingFactory.UseCase.TOKEN_OPTIMIZED, null);
        List<DocumentChunk> tokenOptimizedChunks = tokenOptimized.chunk(document);
        System.out.printf("Token Optimized: %d chunks\n", tokenOptimizedChunks.size());
        
        // Code Documents Use Case
        ChunkingStrategy codeDocuments = ChunkingFactory.createForUseCase(
                ChunkingFactory.UseCase.CODE_DOCUMENTS, null);
        List<DocumentChunk> codeChunks = codeDocuments.chunk(document);
        System.out.printf("Code Documents: %d chunks\n", codeChunks.size());
        
        // Hierarchical Content Use Case
        ChunkingStrategy hierarchical = ChunkingFactory.createForUseCase(
                ChunkingFactory.UseCase.HIERARCHICAL_CONTENT, null);
        List<DocumentChunk> hierarchicalChunks = hierarchical.chunk(document);
        System.out.printf("Hierarchical Content: %d chunks\n", hierarchicalChunks.size());
        
        System.out.println();
    }

    /**
     * Helper method to print first chunk information.
     */
    private static void printFirstChunk(String strategyName, List<DocumentChunk> chunks) {
        if (!chunks.isEmpty()) {
            DocumentChunk firstChunk = chunks.get(0);
            System.out.printf("  %s First Chunk (%.0f chars): %s...\n", 
                    strategyName, 
                    (double) firstChunk.getContent().length(),
                    firstChunk.getContent().substring(0, Math.min(50, firstChunk.getContent().length())));
            System.out.printf("  Quality Score: %.3f\n", firstChunk.getQualityScore());
            System.out.printf("  LangChain Splitter: %s\n", 
                    firstChunk.getMetadata().get("langchain_splitter"));
        }
        System.out.println();
    }

    /**
     * Create a sample document for demonstration.
     */
    private static RAGDocument createSampleDocument() {
        String content = """
            # LangChain Integration Documentation
            
            This document demonstrates the integration of LangChain text splitters
            with the PCM chunking system. LangChain provides several powerful
            text splitting strategies that can be seamlessly integrated.
            
            ## Character Text Splitter
            
            The Character Text Splitter splits text based on a single separator.
            It's the simplest form of text splitting and works well for documents
            with clear structural separators like paragraphs or sections.
            
            ## Recursive Character Text Splitter
            
            This is the most commonly used splitter in LangChain. It tries to split
            text by looking for separators in order, maintaining related pieces
            of text together as much as possible.
            
            ### Example Code
            
            ```java
            // Create recursive splitter
            RecursiveCharacterTextSplitter splitter = 
                new RecursiveCharacterTextSplitter(1000, 200);
            
            // Use with PCM adapter
            LangChainAdapter adapter = new LangChainAdapter(splitter, config);
            List<DocumentChunk> chunks = adapter.chunk(document);
            ```
            
            ## Token Text Splitter
            
            The Token Text Splitter splits text based on token count rather than
            character count. This is particularly useful when working with LLMs
            that have token-based limits.
            
            ### Benefits
            
            - More accurate for LLM usage
            - Model-specific tokenization
            - Better resource management
            
            ## Integration Benefits
            
            By integrating LangChain splitters with PCM, you get:
            
            1. **Unified Interface**: Use LangChain splitters through standard PCM interfaces
            2. **Metadata Preservation**: All original metadata is maintained
            3. **Quality Assessment**: Automatic quality scoring for all chunks
            4. **Factory Support**: Easy creation through ChunkingFactory
            5. **Fallback Mechanisms**: Automatic fallback to other strategies if needed
            
            This integration provides the best of both worlds: LangChain's proven
            text splitting algorithms with PCM's comprehensive chunking framework.
            """;

        return RAGDocument.builder()
                .id("langchain-integration-doc")
                .type(DocumentType.MARKDOWN)
                .content(content)
                .title("LangChain Integration Documentation")
                .author("PCM Team")
                .build();
    }
}