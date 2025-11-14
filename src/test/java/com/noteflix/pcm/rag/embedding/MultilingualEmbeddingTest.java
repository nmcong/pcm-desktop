package com.noteflix.pcm.rag.embedding;

import com.noteflix.pcm.rag.embedding.core.BgeEmbeddingService;
import com.noteflix.pcm.rag.embedding.core.EmbeddingServiceRegistry;
import com.noteflix.pcm.rag.embedding.core.VietnameseEmbeddingService;
import com.noteflix.pcm.rag.embedding.model.Language;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for multilingual embedding models.
 *
 * <p>Tests:
 * - Vietnamese model (PhoBERT)
 * - English model (BGE-M3)
 * - Service registry
 * - Semantic similarity
 *
 * @author PCM Team
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultilingualEmbeddingTest {

  private static EmbeddingServiceRegistry registry;

  @BeforeAll
  public static void setup() throws Exception {
    System.out.println("═══════════════════════════════════════════════════════════════");
    System.out.println("  Multilingual Embedding Tests");
    System.out.println("═══════════════════════════════════════════════════════════════");
    System.out.println();

    registry = new EmbeddingServiceRegistry();
  }

  @AfterAll
  public static void tearDown() {
    if (registry != null) {
      registry.close();
    }
    System.out.println();
    System.out.println("═══════════════════════════════════════════════════════════════");
    System.out.println("  ✅ All Tests Completed");
    System.out.println("═══════════════════════════════════════════════════════════════");
  }

  // ═══════════════════════════════════════════════════════════════
  // Vietnamese Model Tests
  // ═══════════════════════════════════════════════════════════════

  @Test
  @Order(1)
  @DisplayName("Vietnamese: Single Text Embedding")
  public void testVietnameseSingleEmbedding() throws Exception {
    System.out.println("Test 1: Vietnamese single text embedding");

    String text = "Xin chào, đây là văn bản tiếng Việt";
    float[] embedding = registry.embed(text, Language.VIETNAMESE);

    assertNotNull(embedding, "Embedding should not be null");
    assertEquals(768, embedding.length, "Vietnamese model should produce 768-dim vectors");

    // Check for non-zero values
    boolean hasNonZero = false;
    for (float v : embedding) {
      if (v != 0.0f) {
        hasNonZero = true;
        break;
      }
    }
    assertTrue(hasNonZero, "Embedding should have non-zero values");

    System.out.printf("   ✓ Embedding dimension: %d\n", embedding.length);
    System.out.printf("   ✓ Sample values: [%.4f, %.4f, %.4f, ...]\n", embedding[0],
        embedding[1], embedding[2]);
    System.out.println();
  }

  @Test
  @Order(2)
  @DisplayName("Vietnamese: Batch Embedding")
  public void testVietnameseBatchEmbedding() throws Exception {
    System.out.println("Test 2: Vietnamese batch embedding");

    String[] texts = {
      "Lập trình Java rất thú vị", "Tôi thích học machine learning", "Hôm nay thời tiết đẹp"
    };

    float[][] embeddings = registry.embedBatch(texts, Language.VIETNAMESE);

    assertNotNull(embeddings, "Batch embeddings should not be null");
    assertEquals(texts.length, embeddings.length, "Should return same number of embeddings");

    for (int i = 0; i < embeddings.length; i++) {
      assertNotNull(embeddings[i], "Each embedding should not be null");
      assertEquals(768, embeddings[i].length, "Each embedding should be 768-dim");
    }

    System.out.printf("   ✓ Batch size: %d\n", embeddings.length);
    System.out.printf("   ✓ Each dimension: 768\n");
    System.out.println();
  }

  @Test
  @Order(3)
  @DisplayName("Vietnamese: Semantic Similarity")
  public void testVietnameseSemanticSimilarity() throws Exception {
    System.out.println("Test 3: Vietnamese semantic similarity");

    // Similar texts
    String text1 = "Tôi thích lập trình Java";
    String text2 = "Java là ngôn ngữ tôi yêu thích";

    // Dissimilar text
    String text3 = "Hôm nay trời mưa to";

    float[] emb1 = registry.embed(text1, Language.VIETNAMESE);
    float[] emb2 = registry.embed(text2, Language.VIETNAMESE);
    float[] emb3 = registry.embed(text3, Language.VIETNAMESE);

    float sim12 = cosineSimilarity(emb1, emb2);
    float sim13 = cosineSimilarity(emb1, emb3);

    System.out.printf("   Similarity(text1, text2): %.4f (similar)\n", sim12);
    System.out.printf("   Similarity(text1, text3): %.4f (dissimilar)\n", sim13);

    assertTrue(
        sim12 > sim13,
        "Similar texts should have higher similarity than dissimilar texts");
    assertTrue(sim12 > 0.5, "Similar texts should have similarity > 0.5");

    System.out.println("   ✓ Semantic similarity working correctly");
    System.out.println();
  }

  @Test
  @Order(4)
  @DisplayName("Vietnamese: Empty and Special Cases")
  public void testVietnameseSpecialCases() throws Exception {
    System.out.println("Test 4: Vietnamese special cases");

    // Short text
    String shortText = "OK";
    float[] shortEmb = registry.embed(shortText, Language.VIETNAMESE);
    assertNotNull(shortEmb, "Should handle short text");
    assertEquals(768, shortEmb.length);
    System.out.println("   ✓ Short text handled");

    // Long text
    String longText =
        "Đây là một văn bản rất dài với nhiều từ để test khả năng xử lý của model. "
            .repeat(10);
    float[] longEmb = registry.embed(longText, Language.VIETNAMESE);
    assertNotNull(longEmb, "Should handle long text");
    assertEquals(768, longEmb.length);
    System.out.println("   ✓ Long text handled");

    // Text with special characters
    String specialText = "Email: test@example.com, Phone: +84-123-456-789";
    float[] specialEmb = registry.embed(specialText, Language.VIETNAMESE);
    assertNotNull(specialEmb, "Should handle special characters");
    System.out.println("   ✓ Special characters handled");

    System.out.println();
  }

  // ═══════════════════════════════════════════════════════════════
  // English Model Tests
  // ═══════════════════════════════════════════════════════════════

  @Test
  @Order(5)
  @DisplayName("English: Single Text Embedding")
  public void testEnglishSingleEmbedding() throws Exception {
    System.out.println("Test 5: English single text embedding");

    String text = "Hello, this is an English text";
    float[] embedding = registry.embed(text, Language.ENGLISH);

    assertNotNull(embedding, "Embedding should not be null");
    assertEquals(1024, embedding.length, "BGE-M3 model should produce 1024-dim vectors");

    boolean hasNonZero = false;
    for (float v : embedding) {
      if (v != 0.0f) {
        hasNonZero = true;
        break;
      }
    }
    assertTrue(hasNonZero, "Embedding should have non-zero values");

    System.out.printf("   ✓ Embedding dimension: %d\n", embedding.length);
    System.out.printf("   ✓ Sample values: [%.4f, %.4f, %.4f, ...]\n", embedding[0],
        embedding[1], embedding[2]);
    System.out.println();
  }

  @Test
  @Order(6)
  @DisplayName("English: Batch Embedding")
  public void testEnglishBatchEmbedding() throws Exception {
    System.out.println("Test 6: English batch embedding");

    String[] texts = {
      "Java programming is fun", "I enjoy learning machine learning", "The weather is nice today"
    };

    float[][] embeddings = registry.embedBatch(texts, Language.ENGLISH);

    assertNotNull(embeddings, "Batch embeddings should not be null");
    assertEquals(texts.length, embeddings.length);

    for (int i = 0; i < embeddings.length; i++) {
      assertNotNull(embeddings[i]);
      assertEquals(1024, embeddings[i].length, "Each embedding should be 1024-dim");
    }

    System.out.printf("   ✓ Batch size: %d\n", embeddings.length);
    System.out.printf("   ✓ Each dimension: 1024\n");
    System.out.println();
  }

  @Test
  @Order(7)
  @DisplayName("English: Semantic Similarity")
  public void testEnglishSemanticSimilarity() throws Exception {
    System.out.println("Test 7: English semantic similarity");

    // Similar: programming related
    String text1 = "How to code in Java?";
    String text2 = "Java programming tutorial";

    // Dissimilar
    String text3 = "The cat sat on the mat";

    float[] emb1 = registry.embed(text1, Language.ENGLISH);
    float[] emb2 = registry.embed(text2, Language.ENGLISH);
    float[] emb3 = registry.embed(text3, Language.ENGLISH);

    float sim12 = cosineSimilarity(emb1, emb2);
    float sim13 = cosineSimilarity(emb1, emb3);

    System.out.printf("   Similarity(text1, text2): %.4f (similar)\n", sim12);
    System.out.printf("   Similarity(text1, text3): %.4f (dissimilar)\n", sim13);

    assertTrue(sim12 > sim13, "Similar texts should have higher similarity");
    assertTrue(sim12 > 0.5, "Similar texts should have similarity > 0.5");

    System.out.println("   ✓ Semantic similarity working correctly");
    System.out.println();
  }

  @Test
  @Order(8)
  @DisplayName("English: Code Understanding")
  public void testEnglishCodeUnderstanding() throws Exception {
    System.out.println("Test 8: English code understanding");

    String query = "How to validate user input?";
    String doc1 = "Use Bean Validation with @NotNull, @Size annotations for input validation";
    String doc2 = "Database connection pooling improves performance";

    float[] queryEmb = registry.embed(query, Language.ENGLISH);
    float[] doc1Emb = registry.embed(doc1, Language.ENGLISH);
    float[] doc2Emb = registry.embed(doc2, Language.ENGLISH);

    float sim1 = cosineSimilarity(queryEmb, doc1Emb);
    float sim2 = cosineSimilarity(queryEmb, doc2Emb);

    System.out.printf("   Query-Doc1 similarity: %.4f (relevant)\n", sim1);
    System.out.printf("   Query-Doc2 similarity: %.4f (irrelevant)\n", sim2);

    assertTrue(
        sim1 > sim2, "Relevant document should have higher similarity than irrelevant one");

    System.out.println("   ✓ Code understanding working correctly");
    System.out.println();
  }

  // ═══════════════════════════════════════════════════════════════
  // Registry Tests
  // ═══════════════════════════════════════════════════════════════

  @Test
  @Order(9)
  @DisplayName("Registry: Model Availability")
  public void testRegistryModelAvailability() {
    System.out.println("Test 9: Registry model availability");

    boolean hasVietnamese = registry.hasModel(Language.VIETNAMESE);
    boolean hasEnglish = registry.hasModel(Language.ENGLISH);

    System.out.printf("   Vietnamese model: %s\n", hasVietnamese ? "✓ Available" : "✗ Missing");
    System.out.printf("   English model:    %s\n", hasEnglish ? "✓ Available" : "✗ Missing");

    assertTrue(hasVietnamese || hasEnglish, "At least one language-specific model should be available");

    System.out.println();
  }

  @Test
  @Order(10)
  @DisplayName("Registry: Dimension Check")
  public void testRegistryDimensions() {
    System.out.println("Test 10: Registry dimensions");

    if (registry.hasModel(Language.VIETNAMESE)) {
      int viDim = registry.getDimension(Language.VIETNAMESE);
      assertEquals(768, viDim, "Vietnamese model dimension");
      System.out.printf("   ✓ Vietnamese: %d dimensions\n", viDim);
    }

    if (registry.hasModel(Language.ENGLISH)) {
      int enDim = registry.getDimension(Language.ENGLISH);
      assertEquals(1024, enDim, "English model dimension");
      System.out.printf("   ✓ English: %d dimensions\n", enDim);
    }

    System.out.println();
  }

  @Test
  @Order(11)
  @DisplayName("Registry: Statistics")
  public void testRegistryStatistics() {
    System.out.println("Test 11: Registry statistics");

    String stats = registry.getStatistics();
    assertNotNull(stats, "Statistics should not be null");
    assertTrue(stats.contains("Registry"), "Statistics should contain registry info");

    System.out.println(stats);
  }

  // ═══════════════════════════════════════════════════════════════
  // Cross-Language Tests
  // ═══════════════════════════════════════════════════════════════

  @Test
  @Order(12)
  @DisplayName("Cross-Language: Dimension Comparison")
  public void testCrossLanguageDimensions() throws Exception {
    System.out.println("Test 12: Cross-language dimension comparison");

    String viText = "Xin chào";
    String enText = "Hello";

    float[] viEmb = registry.embed(viText, Language.VIETNAMESE);
    float[] enEmb = registry.embed(enText, Language.ENGLISH);

    System.out.printf("   Vietnamese dimension: %d\n", viEmb.length);
    System.out.printf("   English dimension:    %d\n", enEmb.length);

    assertNotEquals(
        viEmb.length, enEmb.length, "Different models should have different dimensions");

    System.out.println("   ✓ Models have different dimensions (expected)");
    System.out.println();
  }

  @Test
  @Order(13)
  @DisplayName("Performance: Benchmark")
  public void testPerformanceBenchmark() throws Exception {
    System.out.println("Test 13: Performance benchmark");

    String viText = "Văn bản test hiệu suất";
    String enText = "Performance test text";

    // Warm up
    for (int i = 0; i < 10; i++) {
      registry.embed(viText, Language.VIETNAMESE);
      registry.embed(enText, Language.ENGLISH);
    }

    // Benchmark Vietnamese
    long viStart = System.currentTimeMillis();
    for (int i = 0; i < 100; i++) {
      registry.embed(viText, Language.VIETNAMESE);
    }
    long viTime = System.currentTimeMillis() - viStart;

    // Benchmark English
    long enStart = System.currentTimeMillis();
    for (int i = 0; i < 100; i++) {
      registry.embed(enText, Language.ENGLISH);
    }
    long enTime = System.currentTimeMillis() - enStart;

    System.out.printf("   Vietnamese: %.2f ms/embedding (100 iterations)\n", viTime / 100.0);
    System.out.printf("   English:    %.2f ms/embedding (100 iterations)\n", enTime / 100.0);

    assertTrue(viTime > 0, "Vietnamese embedding should take measurable time");
    assertTrue(enTime > 0, "English embedding should take measurable time");

    System.out.println("   ✓ Performance benchmarks completed");
    System.out.println();
  }

  // ═══════════════════════════════════════════════════════════════
  // Utility Methods
  // ═══════════════════════════════════════════════════════════════

  private float cosineSimilarity(float[] a, float[] b) {
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
}

