package com.noteflix.pcm.rag.examples;

import com.noteflix.pcm.rag.embedding.api.EmbeddingService;
import com.noteflix.pcm.rag.embedding.core.DJLEmbeddingService;
import com.noteflix.pcm.rag.vectorstore.core.QdrantVectorStore;
import com.noteflix.pcm.rag.vectorstore.embedded.QdrantEmbeddedManager;
import com.noteflix.pcm.rag.model.DocumentType;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.RetrievalOptions;
import com.noteflix.pcm.rag.model.ScoredDocument;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced example: DJL Embeddings + Qdrant Vector Database Integration.
 *
 * <p>This example demonstrates:
 * - Loading DJL embedding models 
 * - Starting embedded Qdrant database
 * - Indexing documents with vector embeddings
 * - Performing semantic search queries
 * - Batch processing optimization
 *
 * <p>Setup: 
 * 1. Run: ./scripts/setup-embeddings-djl.sh 
 * 2. Rebuild: ./scripts/build.sh 
 * 3. Run this example
 *
 * @author PCM Team
 * @version 2.0.0 - Qdrant Integration
 */
public class DJLEmbeddingExample {

  public static void main(String[] args) {
    System.out.println("=== DJL Embedding + Qdrant Integration Example ===\n");

    QdrantEmbeddedManager qdrantManager = null;
    EmbeddingService embeddingService = null;
    QdrantVectorStore vectorStore = null;

    try {
      // Step 1: Initialize DJL Embedding Service
      System.out.println("🔧 Step 1: Loading DJL embedding model...");
      embeddingService = new DJLEmbeddingService("data/models/all-MiniLM-L6-v2");
      
      System.out.println("✅ Model loaded!");
      System.out.println("   Model: " + embeddingService.getModelName());
      System.out.println("   Dimensions: " + embeddingService.getDimension());
      System.out.println();

      // Step 2: Start Embedded Qdrant Database  
      System.out.println("🚀 Step 2: Starting embedded Qdrant database...");
      qdrantManager = new QdrantEmbeddedManager();
      qdrantManager.start();
      
      System.out.println("✅ Qdrant started!");
      System.out.println("   URL: " + qdrantManager.getUrl());
      System.out.println("   Port: " + qdrantManager.getPort());
      System.out.println();

      // Step 3: Initialize Vector Store
      System.out.println("📊 Step 3: Setting up vector store...");
      vectorStore = new QdrantVectorStore(
          "localhost", 
          qdrantManager.getPort(), 
          null, // no API key for embedded
          "djl_example_docs",
          embeddingService,
          embeddingService.getDimension()
      );
      System.out.println("✅ Vector store initialized!");
      System.out.println();

      // Step 4: Create and Index Sample Documents
      System.out.println("📝 Step 4: Indexing sample documents...");
      List<RAGDocument> documents = createSampleDocuments();
      
      // Demonstrate batch indexing with performance measurement
      long startTime = System.currentTimeMillis();
      vectorStore.indexDocuments(documents);
      long indexTime = System.currentTimeMillis() - startTime;
      
      System.out.printf("✅ Indexed %d documents in %dms (avg: %.1fms per doc)\n", 
          documents.size(), indexTime, (double) indexTime / documents.size());
      System.out.printf("   Total documents in store: %d\n", vectorStore.getDocumentCount());
      System.out.println();

      // Step 5: Perform Semantic Search Queries
      System.out.println("🔍 Step 5: Testing semantic search...");
      performSearchTests(vectorStore);

      // Step 6: Demonstrate ThreadLocal Performance
      System.out.println("⚡ Step 6: Testing concurrent performance...");
      demonstrateConcurrentPerformance(embeddingService);

      System.out.println("✅ All tests completed successfully!");

    } catch (UnsupportedOperationException e) {
      System.err.println("\n❌ DJL libraries not yet installed\n");
      System.err.println(e.getMessage());
      System.err.println();

    } catch (Exception e) {
      System.err.println("❌ Error: " + e.getMessage());
      e.printStackTrace();

    } finally {
      // Cleanup resources
      System.out.println("\n🧹 Cleaning up resources...");
      
      if (vectorStore != null) {
        try {
          vectorStore.close();
          System.out.println("✅ Vector store closed");
        } catch (Exception e) {
          System.err.println("⚠️ Error closing vector store: " + e.getMessage());
        }
      }
      
      if (embeddingService != null) {
        try {
          ((DJLEmbeddingService) embeddingService).close();
          System.out.println("✅ Embedding service closed");
        } catch (Exception e) {
          System.err.println("⚠️ Error closing embedding service: " + e.getMessage());
        }
      }
      
      if (qdrantManager != null) {
        try {
          qdrantManager.stop();
          System.out.println("✅ Qdrant stopped");
        } catch (Exception e) {
          System.err.println("⚠️ Error stopping Qdrant: " + e.getMessage());
        }
      }
      
      System.out.println("👋 Example completed!");
    }
  }

  /**
   * Create sample documents for testing.
   */
  private static List<RAGDocument> createSampleDocuments() {
    return List.of(
        RAGDocument.builder()
            .id("1")
            .title("Email Validation Guide")
            .content("How to validate customer email addresses using regex patterns and DNS verification. " +
                    "Best practices include checking format, domain existence, and avoiding common typos.")
            .type(DocumentType.API_DOC)
            .sourcePath("/docs/email-validation.md")
            .indexedAt(LocalDateTime.now())
            .build(),

        RAGDocument.builder()
            .id("2") 
            .title("Customer Registration Process")
            .content("Complete guide for customer email verification during registration. " +
                    "Includes double opt-in confirmation, email format validation, and bounce handling.")
            .type(DocumentType.API_DOC)
            .sourcePath("/docs/customer-registration.md")
            .indexedAt(LocalDateTime.now())
            .build(),

        RAGDocument.builder()
            .id("3")
            .title("Database Connection Issues")
            .content("Troubleshooting database connectivity problems. Common issues include timeout errors, " +
                    "connection pool exhaustion, network configuration, and authentication failures.")
            .type(DocumentType.KNOWLEDGE_BASE)
            .sourcePath("/docs/database-troubleshooting.md")
            .indexedAt(LocalDateTime.now())
            .build(),

        RAGDocument.builder()
            .id("4")
            .title("Email Format Validation")
            .content("Technical details on email address format validation according to RFC standards. " +
                    "Covers valid characters, domain rules, internationalization, and security considerations.")
            .type(DocumentType.API_DOC)
            .sourcePath("/docs/email-format.md")
            .indexedAt(LocalDateTime.now())
            .build(),

        RAGDocument.builder()
            .id("5")
            .title("API Rate Limiting")
            .content("Implementing rate limiting for REST APIs to prevent abuse. " +
                    "Includes token bucket algorithms, sliding window approaches, and Redis-based implementations.")
            .type(DocumentType.API_DOC)
            .sourcePath("/docs/api-rate-limiting.md")
            .indexedAt(LocalDateTime.now())
            .build(),

        RAGDocument.builder()
            .id("6")
            .title("Vector Database Performance")
            .content("Optimizing vector database performance for semantic search applications. " +
                    "Covers indexing strategies, query optimization, memory management, and batch operations.")
            .type(DocumentType.KNOWLEDGE_BASE)
            .sourcePath("/docs/vector-db-performance.md")
            .indexedAt(LocalDateTime.now())
            .build()
    );
  }

  /**
   * Perform various search tests to demonstrate Qdrant functionality.
   */
  private static void performSearchTests(QdrantVectorStore vectorStore) {
    String[] queries = {
        "How to validate customer email?",
        "Database connection problems",
        "Vector search performance optimization",
        "API security best practices"
    };

    for (String query : queries) {
      System.out.printf("\n📋 Query: \"%s\"\n", query);
      
      RetrievalOptions options = RetrievalOptions.builder()
          .maxResults(3)
          .build();
      
      long searchStart = System.currentTimeMillis();
      List<ScoredDocument> results = vectorStore.search(query, options);
      long searchTime = System.currentTimeMillis() - searchStart;
      
      System.out.printf("🔍 Found %d results in %dms:\n", results.size(), searchTime);
      
      for (ScoredDocument result : results) {
        System.out.printf("   %.3f - %s: %s\n", 
            result.getScore(),
            result.getDocument().getTitle(),
            truncate(result.getSnippet(), 80));
      }
    }
  }

  /**
   * Demonstrate ThreadLocal concurrent performance benefits.
   */
  private static void demonstrateConcurrentPerformance(EmbeddingService embeddingService) {
    String[] testTexts = {
        "Concurrent processing with ThreadLocal optimization",
        "Parallel embedding generation for better performance",  
        "Thread-safe operations without synchronization overhead",
        "Scalable vector processing for large datasets"
    };

    // Test sequential processing
    System.out.println("📈 Sequential processing:");
    long sequentialStart = System.currentTimeMillis();
    for (String text : testTexts) {
      embeddingService.embed(text);
    }
    long sequentialTime = System.currentTimeMillis() - sequentialStart;
    System.out.printf("   Time: %dms\n", sequentialTime);

    // Test batch processing  
    System.out.println("🚀 Batch processing:");
    long batchStart = System.currentTimeMillis();
    embeddingService.embedBatch(testTexts);
    long batchTime = System.currentTimeMillis() - batchStart;
    System.out.printf("   Time: %dms\n", batchTime);
    
    if (sequentialTime > 0) {
      double speedup = (double) sequentialTime / batchTime;
      System.out.printf("   Speedup: %.1fx faster\n", speedup);
    }
  }

  /**
   * Truncate text to specified length with ellipsis.
   */
  private static String truncate(String text, int maxLength) {
    if (text == null || text.length() <= maxLength) {
      return text;
    }
    return text.substring(0, maxLength - 3) + "...";
  }

  /**
   * Calculate cosine similarity between two vectors (kept for reference).
   */
  private static double cosineSimilarity(float[] v1, float[] v2) {
    double dotProduct = 0;
    double norm1 = 0;
    double norm2 = 0;

    for (int i = 0; i < v1.length; i++) {
      dotProduct += v1[i] * v2[i];
      norm1 += v1[i] * v1[i];
      norm2 += v2[i] * v2[i];
    }

    return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
  }
}
