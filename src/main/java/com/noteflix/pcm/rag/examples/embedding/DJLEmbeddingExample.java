package com.noteflix.pcm.rag.examples.embedding;

import com.noteflix.pcm.rag.embedding.DJLEmbeddingService;
import com.noteflix.pcm.rag.embedding.EmbeddingService;

/**
 * Example: Using DJL for embeddings.
 *
 * <p>Setup: 1. Run: ./scripts/setup-embeddings-djl.sh 2. Rebuild: ./scripts/build.sh 3. Run this
 * example
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class DJLEmbeddingExample {

  public static void main(String[] args) {
    System.out.println("=== DJL Embedding Example ===\n");

    try {
      // 1. Create DJL embedding service
      System.out.println("📦 Loading DJL embedding model...");

      EmbeddingService embeddings = new DJLEmbeddingService("data/models/all-MiniLM-L6-v2");

      System.out.println("✅ Model loaded!");
      System.out.println("   Model: " + embeddings.getModelName());
      System.out.println("   Dimensions: " + embeddings.getDimension());
      System.out.println();

      // 2. Generate embeddings
      System.out.println("📊 Generating embeddings...\n");

      String[] texts = {
        "How to validate customer email?",
        "Customer email verification process",
        "Database connection issues",
        "How to check email format?"
      };

      for (String text : texts) {
        long start = System.currentTimeMillis();
        float[] embedding = embeddings.embed(text);
        long time = System.currentTimeMillis() - start;

        System.out.printf("Text: \"%s\"%n", text);
        System.out.printf(
            "Vector: [%.3f, %.3f, %.3f, ..., %.3f] (%d dims)%n",
            embedding[0],
            embedding[1],
            embedding[2],
            embedding[embedding.length - 1],
            embedding.length);
        System.out.printf("Time: %dms%n%n", time);
      }

      // 3. Semantic similarity
      System.out.println("=== Semantic Similarity ===\n");

      String question = "How to get customer email correct?";
      float[] query = embeddings.embed(question);

      System.out.println("Query: \"" + question + "\"\n");
      System.out.println("Similarities:");

      for (String text : texts) {
        float[] embedding = embeddings.embed(text);
        double similarity = cosineSimilarity(query, embedding);
        System.out.printf("  %.3f - \"%s\"%n", similarity, text);
      }

      System.out.println();
      System.out.println("✅ Example completed!");

    } catch (UnsupportedOperationException e) {
      System.err.println("\n❌ DJL libraries not yet installed\n");
      System.err.println(e.getMessage());
      System.err.println();

    } catch (Exception e) {
      System.err.println("❌ Error: " + e.getMessage());
      e.printStackTrace();
    }
  }

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
