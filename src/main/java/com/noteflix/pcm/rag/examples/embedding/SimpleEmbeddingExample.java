package com.noteflix.pcm.rag.examples.embedding;

import com.noteflix.pcm.rag.embedding.EmbeddingService;
import com.noteflix.pcm.rag.embedding.SimpleEmbeddingService;

/**
 * Example: Generating embeddings for semantic search.
 *
 * <p>This example uses SimpleEmbeddingService (for demo). For production, use DJL or ONNX-based
 * implementation.
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class SimpleEmbeddingExample {

  public static void main(String[] args) {
    System.out.println("=== Embedding Example ===\n");

    // Create embedding service
    EmbeddingService embeddings = new SimpleEmbeddingService(384);

    System.out.println("Model: " + embeddings.getModelName());
    System.out.println("Dimensions: " + embeddings.getDimension());
    System.out.println();

    // Example texts
    String[] texts = {
      "How to validate customer email?",
      "Customer email verification process",
      "Database connection issues",
      "How to check email format?"
    };

    System.out.println("Generating embeddings...\n");

    // Generate embeddings
    float[][] vectors = new float[texts.length][];

    for (int i = 0; i < texts.length; i++) {
      long start = System.currentTimeMillis();
      vectors[i] = embeddings.embed(texts[i]);
      long time = System.currentTimeMillis() - start;

      System.out.printf("%d. \"%s\"%n", i + 1, texts[i]);
      System.out.printf(
          "   Vector: [%.3f, %.3f, %.3f, ... %.3f]%n",
          vectors[i][0], vectors[i][1], vectors[i][2], vectors[i][vectors[i].length - 1]);
      System.out.printf("   Time: %dms%n%n", time);
    }

    // Calculate similarities
    System.out.println("=== Semantic Similarity ===\n");

    String query = "How to validate customer email?";
    float[] queryVector = vectors[0]; // First text

    System.out.println("Query: \"" + query + "\"\n");
    System.out.println("Similarities:");

    for (int i = 0; i < texts.length; i++) {
      double similarity = cosineSimilarity(queryVector, vectors[i]);
      System.out.printf("  %.3f - \"%s\"%n", similarity, texts[i]);
    }

    System.out.println();
    System.out.println("✅ Example completed!");
    System.out.println();
    System.out.println("Note: This uses SimpleEmbeddingService (demo only)");
    System.out.println("For production, use:");
    System.out.println("  - DJL with all-MiniLM-L6-v2 (RECOMMENDED)");
    System.out.println("  - ONNX Runtime with sentence-transformers");
    System.out.println();
    System.out.println("See: docs/development/rag/OFFLINE_EMBEDDINGS_GUIDE.md");
  }

  /** Calculate cosine similarity between two vectors. */
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
