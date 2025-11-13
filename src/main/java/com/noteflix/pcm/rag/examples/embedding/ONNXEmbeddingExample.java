package com.noteflix.pcm.rag.examples.embedding;

import com.noteflix.pcm.rag.embedding.EmbeddingService;
import com.noteflix.pcm.rag.embedding.ONNXEmbeddingService;

/**
 * Example: Using ONNX Runtime for embeddings.
 *
 * <p>Setup: 1. Run: ./scripts/setup-embeddings-onnx.sh 2. Rebuild: ./scripts/build.sh 3. Run this
 * example
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class ONNXEmbeddingExample {

  public static void main(String[] args) {
    System.out.println("=== ONNX Runtime Embedding Example ===\n");

    try {
      // 1. Create ONNX embedding service
      System.out.println("📦 Loading ONNX embedding model...");

      EmbeddingService embeddings = new ONNXEmbeddingService("data/models/all-MiniLM-L6-v2");

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

      // 3. Batch processing
      System.out.println("=== Batch Processing ===\n");

      long start = System.currentTimeMillis();
      float[][] batch = embeddings.embedBatch(texts);
      long time = System.currentTimeMillis() - start;

      System.out.printf("Processed %d texts in %dms%n", texts.length, time);
      System.out.printf("Average: %.1fms per text%n%n", (double) time / texts.length);

      // 4. Semantic similarity
      System.out.println("=== Semantic Similarity ===\n");

      float[] query = batch[0];

      System.out.println("Query: \"" + texts[0] + "\"\n");
      System.out.println("Similarities:");

      for (int i = 0; i < texts.length; i++) {
        double similarity = cosineSimilarity(query, batch[i]);
        System.out.printf("  %.3f - \"%s\"%n", similarity, texts[i]);
      }

      System.out.println();
      System.out.println("✅ Example completed!");

    } catch (UnsupportedOperationException e) {
      System.err.println("\n❌ ONNX Runtime libraries not yet installed\n");
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
