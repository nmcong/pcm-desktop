package com.noteflix.pcm.rag.examples;

import com.noteflix.pcm.rag.embedding.core.EmbeddingServiceRegistry;
import com.noteflix.pcm.rag.embedding.model.Language;
import java.util.Arrays;

/**
 * Example demonstrating multi-language embedding with specialized models.
 *
 * <p>This example shows how to use the EmbeddingServiceRegistry to embed Vietnamese and English
 * content with language-specific high-quality models, with automatic fallback support.
 *
 * <p><strong>Prerequisites:</strong>
 *
 * <pre>
 * # Download all models
 * ./scripts/setup-multilingual-embeddings.sh
 *
 * # Build project
 * ./scripts/build.sh
 *
 * # Run this example
 * java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*" \
 *   com.noteflix.pcm.rag.examples.MultilingualEmbeddingExample
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 * @since 2024-11
 */
public class MultilingualEmbeddingExample {

  public static void main(String[] args) {
    System.out.println("═══════════════════════════════════════════════════════════════");
    System.out.println("   Multi-Language Embedding Example");
    System.out.println("═══════════════════════════════════════════════════════════════");
    System.out.println();

    try (EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry()) {

      // ═══════════════════════════════════════════════════════════
      // Example 1: Vietnamese Text Embedding
      // ═══════════════════════════════════════════════════════════
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println("  Example 1: Vietnamese Text Embedding");
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println();

      String viText = "Xin chào! Đây là một ví dụ về hệ thống RAG đa ngôn ngữ.";
      System.out.println("Input text (Vietnamese):");
      System.out.println("  \"" + viText + "\"");
      System.out.println();

      float[] viEmbedding = registry.embed(viText, Language.VIETNAMESE);

      System.out.println("Output embedding:");
      System.out.println("  Dimension: " + viEmbedding.length);
      System.out.println("  Model: " + registry.getModelName(Language.VIETNAMESE));
      System.out.println("  First 5 values: " + formatVector(viEmbedding, 5));
      System.out.println("  L2 norm: " + String.format("%.6f", l2Norm(viEmbedding)));
      System.out.println();

      // ═══════════════════════════════════════════════════════════
      // Example 2: English Text Embedding
      // ═══════════════════════════════════════════════════════════
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println("  Example 2: English Text Embedding");
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println();

      String enText = "Hello! This is an example of a multilingual RAG system.";
      System.out.println("Input text (English):");
      System.out.println("  \"" + enText + "\"");
      System.out.println();

      float[] enEmbedding = registry.embed(enText, Language.ENGLISH);

      System.out.println("Output embedding:");
      System.out.println("  Dimension: " + enEmbedding.length);
      System.out.println("  Model: " + registry.getModelName(Language.ENGLISH));
      System.out.println("  First 5 values: " + formatVector(enEmbedding, 5));
      System.out.println("  L2 norm: " + String.format("%.6f", l2Norm(enEmbedding)));
      System.out.println();

      // ═══════════════════════════════════════════════════════════
      // Example 3: Semantic Similarity (Vietnamese)
      // ═══════════════════════════════════════════════════════════
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println("  Example 3: Semantic Similarity (Vietnamese)");
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println();

      String vi1 = "Tôi thích học lập trình Java";
      String vi2 = "Tôi yêu thích code bằng Java";
      String vi3 = "Thời tiết hôm nay rất đẹp";

      float[] emb1 = registry.embed(vi1, Language.VIETNAMESE);
      float[] emb2 = registry.embed(vi2, Language.VIETNAMESE);
      float[] emb3 = registry.embed(vi3, Language.VIETNAMESE);

      System.out.println("Text 1: \"" + vi1 + "\"");
      System.out.println("Text 2: \"" + vi2 + "\"");
      System.out.println("Text 3: \"" + vi3 + "\"");
      System.out.println();

      float sim12 = cosineSimilarity(emb1, emb2);
      float sim13 = cosineSimilarity(emb1, emb3);
      float sim23 = cosineSimilarity(emb2, emb3);

      System.out.println("Similarity scores:");
      System.out.println("  Text 1 ↔ Text 2: " + String.format("%.4f", sim12) + " (similar topics)");
      System.out.println("  Text 1 ↔ Text 3: " + String.format("%.4f", sim13) + " (different topics)");
      System.out.println("  Text 2 ↔ Text 3: " + String.format("%.4f", sim23) + " (different topics)");
      System.out.println();

      // ═══════════════════════════════════════════════════════════
      // Example 4: Semantic Similarity (English)
      // ═══════════════════════════════════════════════════════════
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println("  Example 4: Semantic Similarity (English)");
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println();

      String en1 = "How to validate user input in Java?";
      String en2 = "What is the best way to check input validation?";
      String en3 = "Python is a popular programming language.";

      float[] enEmb1 = registry.embed(en1, Language.ENGLISH);
      float[] enEmb2 = registry.embed(en2, Language.ENGLISH);
      float[] enEmb3 = registry.embed(en3, Language.ENGLISH);

      System.out.println("Text 1: \"" + en1 + "\"");
      System.out.println("Text 2: \"" + en2 + "\"");
      System.out.println("Text 3: \"" + en3 + "\"");
      System.out.println();

      float enSim12 = cosineSimilarity(enEmb1, enEmb2);
      float enSim13 = cosineSimilarity(enEmb1, enEmb3);
      float enSim23 = cosineSimilarity(enEmb2, enEmb3);

      System.out.println("Similarity scores:");
      System.out.println("  Text 1 ↔ Text 2: " + String.format("%.4f", enSim12) + " (similar topics)");
      System.out.println("  Text 1 ↔ Text 3: " + String.format("%.4f", enSim13) + " (different topics)");
      System.out.println("  Text 2 ↔ Text 3: " + String.format("%.4f", enSim23) + " (different topics)");
      System.out.println();

      // ═══════════════════════════════════════════════════════════
      // Example 5: Batch Embedding
      // ═══════════════════════════════════════════════════════════
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println("  Example 5: Batch Embedding (Performance)");
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println();

      String[] batchTexts = {
        "Document 1: Customer validation example",
        "Document 2: Error handling in Java",
        "Document 3: Database connection pooling",
        "Document 4: RESTful API design patterns",
        "Document 5: Unit testing best practices"
      };

      System.out.println("Embedding " + batchTexts.length + " documents...");
      System.out.println();

      long startTime = System.currentTimeMillis();
      float[][] batchEmbeddings = registry.embedBatch(batchTexts, Language.ENGLISH);
      long endTime = System.currentTimeMillis();

      System.out.println("✅ Batch embedding completed!");
      System.out.println("  Documents: " + batchEmbeddings.length);
      System.out.println("  Dimension: " + batchEmbeddings[0].length);
      System.out.println("  Total time: " + (endTime - startTime) + " ms");
      System.out.println("  Avg per doc: " + ((endTime - startTime) / batchTexts.length) + " ms");
      System.out.println();

      // ═══════════════════════════════════════════════════════════
      // Example 6: Registry Statistics
      // ═══════════════════════════════════════════════════════════
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println("  Example 6: Registry Statistics");
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      System.out.println();

      System.out.println(registry.getStatistics());

      // ═══════════════════════════════════════════════════════════
      // Summary
      // ═══════════════════════════════════════════════════════════
      System.out.println("═══════════════════════════════════════════════════════════════");
      System.out.println("   ✅ All Examples Completed Successfully!");
      System.out.println("═══════════════════════════════════════════════════════════════");
      System.out.println();
      System.out.println("Key Takeaways:");
      System.out.println("  • Language-specific models provide better quality");
      System.out.println("  • Vietnamese model: PhoBERT-based (768 dim)");
      System.out.println("  • English model: BGE-M3 (1024 dim, MTEB #1)");
      System.out.println("  • Automatic fallback ensures reliability");
      System.out.println("  • Batch processing improves performance");
      System.out.println();
      System.out.println("Next Steps:");
      System.out.println("  1. Integrate into your RAG pipeline");
      System.out.println("  2. Add language field to documents");
      System.out.println("  3. Use appropriate model for each document type");
      System.out.println("  4. Monitor fallback usage in production");
      System.out.println();

    } catch (Exception e) {
      System.err.println("❌ Error: " + e.getMessage());
      e.printStackTrace();
      System.err.println();
      System.err.println("Make sure you have downloaded the models:");
      System.err.println("  ./scripts/setup-multilingual-embeddings.sh");
    }
  }

  /**
   * Calculate cosine similarity between two vectors.
   *
   * @param a First vector
   * @param b Second vector
   * @return Similarity score (0 to 1)
   */
  private static float cosineSimilarity(float[] a, float[] b) {
    if (a.length != b.length) {
      throw new IllegalArgumentException("Vectors must have same dimension");
    }

    float dotProduct = 0.0f;
    float normA = 0.0f;
    float normB = 0.0f;

    for (int i = 0; i < a.length; i++) {
      dotProduct += a[i] * b[i];
      normA += a[i] * a[i];
      normB += b[i] * b[i];
    }

    return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
  }

  /**
   * Calculate L2 norm of a vector.
   *
   * @param vector Input vector
   * @return L2 norm
   */
  private static double l2Norm(float[] vector) {
    double sum = 0.0;
    for (float v : vector) {
      sum += v * v;
    }
    return Math.sqrt(sum);
  }

  /**
   * Format vector for display.
   *
   * @param vector Input vector
   * @param count Number of elements to show
   * @return Formatted string
   */
  private static String formatVector(float[] vector, int count) {
    if (vector == null || vector.length == 0) {
      return "[]";
    }

    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < Math.min(count, vector.length); i++) {
      if (i > 0) sb.append(", ");
      sb.append(String.format("%.4f", vector[i]));
    }
    sb.append(", ...]");
    return sb.toString();
  }
}

