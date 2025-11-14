package examples;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.core.ChunkingFactory;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jAdapter;
import com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jConfig;
import com.noteflix.pcm.rag.chunking.langchain4j.LangChain4jSplitterFactory;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.DocumentType;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.DocumentByWordSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;

import java.util.List;

/**
 * Real-world example demonstrating LangChain4j integration with PCM chunking system.
 * 
 * This example shows how to use actual LangChain4j library classes within
 * the PCM framework to achieve high-quality document chunking for RAG applications.
 */
public class LangChain4jRealUsageExample {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j Real Library Integration Example ===\n");
        
        // Create sample documents
        RAGDocument technicalDoc = createTechnicalDocument();
        RAGDocument narrativeDoc = createNarrativeDocument();
        RAGDocument codeDoc = createCodeDocument();
        
        // Example 1: Direct LangChain4j Usage
        directLangChain4jUsage(technicalDoc);
        
        // Example 2: LangChain4j via PCM Factory
        langChain4jViaPCMFactory(narrativeDoc);
        
        // Example 3: Compare Different LangChain4j Splitters
        compareLangChain4jSplitters(codeDoc);
        
        // Example 4: Advanced Configuration
        advancedLangChain4jConfiguration(technicalDoc);
        
        // Example 5: Use Case Specific Examples
        useCaseSpecificExamples(narrativeDoc);
    }
    
    /**
     * Example 1: Direct usage of LangChain4j DocumentSplitters through our adapter.
     */
    private static void directLangChain4jUsage(RAGDocument document) {
        System.out.println("1. Direct LangChain4j DocumentSplitter Usage:\n");
        
        // Create LangChain4j DocumentSplitters directly
        DocumentSplitter paragraphSplitter = new DocumentByParagraphSplitter(800, 100);
        DocumentSplitter sentenceSplitter = new DocumentBySentenceSplitter(600, 80);
        DocumentSplitter wordSplitter = new DocumentByWordSplitter(500, 60);
        DocumentSplitter hierarchicalSplitter = DocumentSplitters.recursive(1000, 200);
        
        // Wrap them with our adapter
        LangChain4jConfig config = LangChain4jConfig.defaults();
        
        LangChain4jAdapter paragraphAdapter = new LangChain4jAdapter(paragraphSplitter, config);
        LangChain4jAdapter sentenceAdapter = new LangChain4jAdapter(sentenceSplitter, config);
        LangChain4jAdapter wordAdapter = new LangChain4jAdapter(wordSplitter, config);
        LangChain4jAdapter hierarchicalAdapter = new LangChain4jAdapter(hierarchicalSplitter, config);
        
        // Test each splitter
        testSplitter("Paragraph Splitter", paragraphAdapter, document);
        testSplitter("Sentence Splitter", sentenceAdapter, document);
        testSplitter("Word Splitter", wordAdapter, document);
        testSplitter("Hierarchical Splitter", hierarchicalAdapter, document);
        
        System.out.println();
    }
    
    /**
     * Example 2: Using LangChain4j splitters through PCM ChunkingFactory.
     */
    private static void langChain4jViaPCMFactory(RAGDocument document) {
        System.out.println("2. LangChain4j via PCM ChunkingFactory:\n");
        
        // Use factory to create LangChain4j strategies
        ChunkingStrategy paragraphStrategy = ChunkingFactory.createStrategy(
            ChunkingConfig.forLangChain4jParagraph()
        );
        
        ChunkingStrategy sentenceStrategy = ChunkingFactory.createStrategy(
            ChunkingConfig.forLangChain4jSentence()
        );
        
        ChunkingStrategy hierarchicalStrategy = ChunkingFactory.createStrategy(
            ChunkingConfig.forLangChain4jHierarchical()
        );
        
        // Test strategies
        testStrategy("Factory Paragraph Strategy", paragraphStrategy, document);
        testStrategy("Factory Sentence Strategy", sentenceStrategy, document);
        testStrategy("Factory Hierarchical Strategy", hierarchicalStrategy, document);
        
        System.out.println();
    }
    
    /**
     * Example 3: Compare different LangChain4j splitters on the same document.
     */
    private static void compareLangChain4jSplitters(RAGDocument document) {
        System.out.println("3. Comparing LangChain4j Splitters:\n");
        
        // Get all LangChain4j recommendations
        List<ChunkingFactory.StrategyRecommendation> recommendations = 
            ChunkingFactory.getAllRecommendations(document, ChunkingConfig.defaults(), null);
        
        System.out.println("LangChain4j Strategy Quality Comparison:");
        recommendations.stream()
            .filter(rec -> rec.strategyType.name().startsWith("LANGCHAIN4J"))
            .forEach(rec -> 
                System.out.printf("  %s: %.3f\n", rec.strategyType, rec.expectedQuality));
        
        // Use the best LangChain4j strategy
        ChunkingFactory.StrategyRecommendation bestLangChain4j = recommendations.stream()
            .filter(rec -> rec.strategyType.name().startsWith("LANGCHAIN4J"))
            .findFirst()
            .orElse(null);
        
        if (bestLangChain4j != null) {
            System.out.printf("\nBest LangChain4j Strategy: %s\n", bestLangChain4j.strategyType);
            testStrategy("Best LangChain4j", bestLangChain4j.strategy, document);
        }
        
        System.out.println();
    }
    
    /**
     * Example 4: Advanced LangChain4j configuration with custom settings.
     */
    private static void advancedLangChain4jConfiguration(RAGDocument document) {
        System.out.println("4. Advanced LangChain4j Configuration:\n");
        
        // Custom paragraph splitter configuration
        LangChain4jConfig customConfig = LangChain4jConfig.builder()
            .splitterType(LangChain4jConfig.SplitterType.BY_PARAGRAPH)
            .maxSegmentSizeInChars(1200)
            .maxOverlapSizeInChars(150)
            .useTokenizer(false) // Character-based
            .includeSegmentMetadata(true)
            .calculateQualityScores(true)
            .build();
        
        DocumentSplitter customSplitter = LangChain4jSplitterFactory.createSplitter(customConfig);
        LangChain4jAdapter customAdapter = new LangChain4jAdapter(customSplitter, customConfig);
        
        testSplitter("Custom Configured Splitter", customAdapter, document);
        
        // Token-based configuration example
        LangChain4jConfig tokenConfig = LangChain4jConfig.builder()
            .splitterType(LangChain4jConfig.SplitterType.BY_SENTENCE)
            .maxSegmentSizeInTokens(200)
            .maxOverlapSizeInTokens(40)
            .useTokenizer(true)
            .tokenizerModel("gpt-4")
            .build();
        
        DocumentSplitter tokenSplitter = LangChain4jSplitterFactory.createSplitter(tokenConfig);
        LangChain4jAdapter tokenAdapter = new LangChain4jAdapter(tokenSplitter, tokenConfig);
        
        testSplitter("Token-Based Splitter", tokenAdapter, document);
        
        System.out.println();
    }
    
    /**
     * Example 5: Using LangChain4j for specific use cases.
     */
    private static void useCaseSpecificExamples(RAGDocument document) {
        System.out.println("5. Use Case Specific LangChain4j Examples:\n");
        
        // Paragraph-focused processing
        ChunkingStrategy paragraphFocused = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.LANGCHAIN4J_PARAGRAPH_FOCUSED, null);
        testStrategy("Paragraph Focused", paragraphFocused, document);
        
        // Sentence-precise processing
        ChunkingStrategy sentencePrecise = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.LANGCHAIN4J_SENTENCE_PRECISE, null);
        testStrategy("Sentence Precise", sentencePrecise, document);
        
        // Hierarchical smart processing
        ChunkingStrategy hierarchicalSmart = ChunkingFactory.createForUseCase(
            ChunkingFactory.UseCase.LANGCHAIN4J_HIERARCHICAL_SMART, null);
        testStrategy("Hierarchical Smart", hierarchicalSmart, document);
        
        System.out.println();
    }
    
    /**
     * Helper method to test a LangChain4j adapter.
     */
    private static void testSplitter(String name, LangChain4jAdapter adapter, RAGDocument document) {
        List<DocumentChunk> chunks = adapter.chunk(document);
        
        System.out.printf("%s: %d chunks created\n", name, chunks.size());
        
        if (!chunks.isEmpty()) {
            DocumentChunk firstChunk = chunks.get(0);
            System.out.printf("  First chunk (%d chars): %s...\n", 
                firstChunk.getContent().length(),
                firstChunk.getContent().substring(0, Math.min(60, firstChunk.getContent().length())));
            System.out.printf("  Quality Score: %.3f\n", firstChunk.getQualityScore());
            System.out.printf("  LangChain4j Splitter: %s\n", 
                firstChunk.getMetadata().get("langchain4j_splitter"));
        }
        System.out.println();
    }
    
    /**
     * Helper method to test a chunking strategy.
     */
    private static void testStrategy(String name, ChunkingStrategy strategy, RAGDocument document) {
        List<DocumentChunk> chunks = strategy.chunk(document);
        
        System.out.printf("%s: %d chunks created\n", name, chunks.size());
        
        if (!chunks.isEmpty()) {
            DocumentChunk firstChunk = chunks.get(0);
            System.out.printf("  Strategy: %s\n", strategy.getStrategyName());
            System.out.printf("  First chunk (%d chars): %s...\n", 
                firstChunk.getContent().length(),
                firstChunk.getContent().substring(0, Math.min(60, firstChunk.getContent().length())));
            System.out.printf("  Quality Score: %.3f\n", firstChunk.getQualityScore());
        }
        System.out.println();
    }
    
    /**
     * Create a technical documentation sample.
     */
    private static RAGDocument createTechnicalDocument() {
        String content = """
            # LangChain4j Integration Guide
            
            LangChain4j provides powerful document splitting capabilities for RAG applications.
            This integration guide explains how to use LangChain4j splitters within the PCM framework.
            
            ## DocumentSplitter Interface
            
            The DocumentSplitter interface is the core abstraction in LangChain4j for text splitting.
            It provides methods to split documents into manageable segments while preserving context.
            
            ### Available Splitters
            
            LangChain4j offers several splitter implementations:
            
            - DocumentByParagraphSplitter: Splits text by paragraphs
            - DocumentBySentenceSplitter: Splits text by sentences  
            - DocumentByWordSplitter: Splits text by words
            - DocumentByCharacterSplitter: Splits text by character count
            - HierarchicalDocumentSplitter: Uses hierarchical splitting strategy
            
            ## Configuration Options
            
            Each splitter can be configured with:
            - Maximum segment size (characters or tokens)
            - Overlap between segments
            - Tokenizer for token-based splitting
            - Custom parameters specific to the splitter type
            
            ## Integration Benefits
            
            Using LangChain4j splitters provides:
            1. Battle-tested splitting algorithms
            2. Active community support and updates
            3. Compatibility with LangChain ecosystem
            4. Professional-grade text processing capabilities
            """;
        
        return RAGDocument.builder()
            .id("technical-doc-langchain4j")
            .type(DocumentType.MARKDOWN)
            .content(content)
            .title("LangChain4j Integration Guide")
            .author("PCM Development Team")
            .build();
    }
    
    /**
     * Create a narrative content sample.
     */
    private static RAGDocument createNarrativeDocument() {
        String content = """
            The integration of LangChain4j into the PCM chunking system represents a significant 
            milestone in our quest to provide the most comprehensive text processing capabilities 
            for RAG applications.
            
            After months of research and development, our team identified LangChain4j as the ideal 
            companion library to enhance our existing chunking strategies. The library's mature 
            document splitting algorithms complement our custom implementations perfectly.
            
            The journey began when we realized that while our custom chunking strategies were 
            powerful, there was value in leveraging the collective wisdom of the LangChain 
            community. LangChain4j has been battle-tested across thousands of applications 
            and represents years of community refinement.
            
            What makes this integration particularly exciting is how seamlessly LangChain4j 
            splitters work within our existing framework. Users can now choose from both our 
            custom strategies and proven LangChain4j implementations, giving them the best of 
            both worlds.
            
            The technical implementation involved creating an adapter that bridges LangChain4j's 
            DocumentSplitter interface with our ChunkingStrategy interface. This adapter preserves 
            all metadata, maintains quality scoring, and ensures consistent behavior across all 
            splitter types.
            
            Looking ahead, this integration opens up exciting possibilities for future enhancements. 
            We can now easily incorporate new splitter types as the LangChain4j community develops 
            them, while continuing to innovate with our own custom strategies.
            """;
        
        return RAGDocument.builder()
            .id("narrative-integration-story")
            .type(DocumentType.TEXT)
            .content(content)
            .title("The LangChain4j Integration Story")
            .author("Technical Writer")
            .build();
    }
    
    /**
     * Create a code documentation sample.
     */
    private static RAGDocument createCodeDocument() {
        String content = """
            ```java
            // Example: Using LangChain4j DocumentSplitters
            
            import dev.langchain4j.data.document.DocumentSplitter;
            import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
            import dev.langchain4j.data.segment.TextSegment;
            
            public class LangChain4jExample {
                
                public void splitDocument() {
                    // Create a paragraph splitter
                    DocumentSplitter splitter = new DocumentByParagraphSplitter(1000, 200);
                    
                    // Create document
                    Document document = Document.from("Your text content here...");
                    
                    // Split into segments
                    List<TextSegment> segments = splitter.split(document);
                    
                    // Process segments
                    for (TextSegment segment : segments) {
                        System.out.println("Segment: " + segment.text());
                        System.out.println("Metadata: " + segment.metadata());
                    }
                }
                
                public void hierarchicalSplitting() {
                    // Use hierarchical splitter
                    DocumentSplitter splitter = DocumentSplitters.recursive(800, 100);
                    
                    // This splitter tries multiple strategies:
                    // 1. Split by paragraphs first
                    // 2. If too large, split by sentences
                    // 3. If still too large, split by words
                    // 4. Finally, split by characters if needed
                }
            }
            ```
            
            The code above demonstrates the basic usage patterns for LangChain4j splitters.
            The library provides a clean, intuitive API for document splitting operations.
            
            ## Key Classes
            
            - Document: Represents a document with text content and metadata
            - TextSegment: Represents a chunk of text with associated metadata
            - DocumentSplitter: Interface for all splitting operations
            - DocumentSplitters: Utility class with factory methods
            
            ## Best Practices
            
            1. Choose the right splitter for your content type
            2. Configure appropriate chunk sizes and overlaps
            3. Preserve metadata across splitting operations
            4. Use hierarchical splitting for complex documents
            5. Test different strategies to find optimal performance
            """;
        
        return RAGDocument.builder()
            .id("code-example-langchain4j")
            .type(DocumentType.CODE)
            .content(content)
            .title("LangChain4j Code Examples")
            .author("Developer Documentation")
            .build();
    }
}