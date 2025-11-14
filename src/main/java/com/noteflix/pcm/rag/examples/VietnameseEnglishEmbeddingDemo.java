package com.noteflix.pcm.rag.examples;

import com.noteflix.pcm.rag.embedding.core.EmbeddingServiceRegistry;
import com.noteflix.pcm.rag.embedding.model.Language;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comprehensive demo: Vietnamese & English embedding models with real test data.
 *
 * <p>Demonstrates:
 * - Vietnamese semantic search
 * - English semantic search
 * - Cross-document similarity
 * - Real-world use cases
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class VietnameseEnglishEmbeddingDemo {

  public static void main(String[] args) {
    printHeader("Vietnamese & English Embedding Models Demo");

    try (EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry()) {

      // ═══════════════════════════════════════════════════════
      // Demo 1: Vietnamese Code Documentation Search
      // ═══════════════════════════════════════════════════════
      demo1_VietnameseCodeDocumentation(registry);

      // ═══════════════════════════════════════════════════════
      // Demo 2: English Technical Documentation
      // ═══════════════════════════════════════════════════════
//      demo2_EnglishTechnicalDocs(registry);

      // ═══════════════════════════════════════════════════════
      // Demo 3: Vietnamese Customer Support Q&A
      // ═══════════════════════════════════════════════════════
//      demo3_VietnameseCustomerSupport(registry);

      // ═══════════════════════════════════════════════════════
      // Demo 4: English Stack Overflow Style Q&A
      // ═══════════════════════════════════════════════════════
//      demo4_EnglishStackOverflowQA(registry);

      // ═══════════════════════════════════════════════════════
      // Demo 5: Mixed Language Project
      // ═══════════════════════════════════════════════════════
//      demo5_MixedLanguageProject(registry);

      // ═══════════════════════════════════════════════════════
      // Demo 6: Performance Comparison
      // ═══════════════════════════════════════════════════════
//      demo6_PerformanceComparison(registry);

      printSuccess("All demos completed successfully!");

    } catch (Exception e) {
      printError("Demo failed: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // DEMO 1: Vietnamese Code Documentation Search
  // ═══════════════════════════════════════════════════════════════

  private static void demo1_VietnameseCodeDocumentation(EmbeddingServiceRegistry registry)
      throws Exception {
    printDemoHeader("Demo 1", "Vietnamese Code Documentation Search");

    // Test data: Vietnamese code documentation
    List<Document> docs = new ArrayList<>();
    docs.add(
        new Document(
            "doc1",
            "Làm thế nào để validate dữ liệu đầu vào trong Java? Sử dụng Bean Validation với annotations như @NotNull, @Size, @Email.",
            Language.VIETNAMESE));
    docs.add(
        new Document(
            "doc2",
            "Hướng dẫn kết nối cơ sở dữ liệu MySQL trong Java. Sử dụng JDBC Driver và Connection Pool để quản lý kết nối hiệu quả.",
            Language.VIETNAMESE));
    docs.add(
        new Document(
            "doc3",
            "Cách xử lý exception trong Java. Sử dụng try-catch-finally, throw và throws để quản lý lỗi một cách an toàn.",
            Language.VIETNAMESE));
    docs.add(
        new Document(
            "doc4",
            "REST API design trong Spring Boot. Sử dụng @RestController, @RequestMapping và ResponseEntity để xây dựng API.",
            Language.VIETNAMESE));
    docs.add(
        new Document(
            "doc5",
            "Tối ưu hóa hiệu suất ứng dụng Java. Sử dụng caching, connection pooling và asynchronous processing.",
            Language.VIETNAMESE));

    // Index documents
    System.out.println("📚 Indexing Vietnamese documents...");
    for (Document doc : docs) {
      float[] embedding = registry.embed(doc.content, doc.language);
      doc.setEmbedding(embedding);
    }
    System.out.println("   ✓ Indexed " + docs.size() + " documents");
    System.out.println();

    // Search queries
    String[] queries = {
      "Kiểm tra dữ liệu người dùng nhập vào",
      "Làm sao connect database?",
      "Xử lý lỗi trong code Java"
    };

    System.out.println("🔍 Search queries:");
    System.out.println();

    for (String query : queries) {
      System.out.println("Query: \"" + query + "\"");

      // Embed query
      float[] queryEmb = registry.embed(query, Language.VIETNAMESE);

      // Search
      List<SearchResult> results = search(queryEmb, docs, 3);

      // Display results
      for (int i = 0; i < results.size(); i++) {
        SearchResult result = results.get(i);
        System.out.printf(
            "  %d. [Score: %.4f] %s\n",
            i + 1, result.score, truncate(result.doc.content, 60));
      }
      System.out.println();
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // DEMO 2: English Technical Documentation
  // ═══════════════════════════════════════════════════════════════

  private static void demo2_EnglishTechnicalDocs(EmbeddingServiceRegistry registry)
      throws Exception {
    printDemoHeader("Demo 2", "English Technical Documentation");

    // Test data: English technical docs
    List<Document> docs = new ArrayList<>();
    docs.add(
        new Document(
            "doc1",
            "How to implement JWT authentication in Spring Security. Use JwtTokenProvider and configure security filters.",
            Language.ENGLISH));
    docs.add(
        new Document(
            "doc2",
            "Docker containerization best practices. Use multi-stage builds, minimize layers, and leverage caching.",
            Language.ENGLISH));
    docs.add(
        new Document(
            "doc3",
            "Building microservices with Spring Cloud. Implement service discovery, load balancing, and circuit breakers.",
            Language.ENGLISH));
    docs.add(
        new Document(
            "doc4",
            "Database migration strategies. Use tools like Flyway or Liquibase for version-controlled schema changes.",
            Language.ENGLISH));
    docs.add(
        new Document(
            "doc5",
            "Unit testing in Java with JUnit 5. Write effective tests using assertions, mocking, and test lifecycle hooks.",
            Language.ENGLISH));

    // Index documents
    System.out.println("📚 Indexing English documents...");
    for (Document doc : docs) {
      float[] embedding = registry.embed(doc.content, doc.language);
      doc.setEmbedding(embedding);
    }
    System.out.println("   ✓ Indexed " + docs.size() + " documents");
    System.out.println();

    // Search queries
    String[] queries = {
      "How to secure REST APIs with tokens?",
      "Best practices for containers",
      "Testing Java applications"
    };

    System.out.println("🔍 Search queries:");
    System.out.println();

    for (String query : queries) {
      System.out.println("Query: \"" + query + "\"");

      // Embed query
      float[] queryEmb = registry.embed(query, Language.ENGLISH);

      // Search
      List<SearchResult> results = search(queryEmb, docs, 3);

      // Display results
      for (int i = 0; i < results.size(); i++) {
        SearchResult result = results.get(i);
        System.out.printf(
            "  %d. [Score: %.4f] %s\n",
            i + 1, result.score, truncate(result.doc.content, 70));
      }
      System.out.println();
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // DEMO 3: Vietnamese Customer Support Q&A
  // ═══════════════════════════════════════════════════════════════

  private static void demo3_VietnameseCustomerSupport(EmbeddingServiceRegistry registry)
      throws Exception {
    printDemoHeader("Demo 3", "Vietnamese Customer Support Q&A");

    // Test data: FAQ database
    List<Document> faqs = new ArrayList<>();
    faqs.add(
        new Document(
            "faq1",
            "Làm thế nào để đặt lại mật khẩu? Bạn có thể nhấn vào 'Quên mật khẩu' trên trang đăng nhập và làm theo hướng dẫn.",
            Language.VIETNAMESE));
    faqs.add(
        new Document(
            "faq2",
            "Thời gian giao hàng là bao lâu? Chúng tôi giao hàng trong vòng 2-3 ngày làm việc cho đơn hàng nội thành.",
            Language.VIETNAMESE));
    faqs.add(
        new Document(
            "faq3",
            "Làm sao để hủy đơn hàng? Bạn có thể hủy đơn hàng trong vòng 24h sau khi đặt bằng cách liên hệ bộ phận CSKH.",
            Language.VIETNAMESE));
    faqs.add(
        new Document(
            "faq4",
            "Chính sách đổi trả như thế nào? Chúng tôi chấp nhận đổi trả trong vòng 7 ngày nếu sản phẩm còn nguyên tem mác.",
            Language.VIETNAMESE));
    faqs.add(
        new Document(
            "faq5",
            "Có những phương thức thanh toán nào? Chúng tôi hỗ trợ thanh toán qua thẻ tín dụng, chuyển khoản và COD.",
            Language.VIETNAMESE));

    // Index FAQs
    System.out.println("📚 Indexing FAQ database...");
    for (Document faq : faqs) {
      float[] embedding = registry.embed(faq.content, faq.language);
      faq.setEmbedding(embedding);
    }
    System.out.println("   ✓ Indexed " + faqs.size() + " FAQs");
    System.out.println();

    // Customer queries
    String[] queries = {
      "Tôi quên mật khẩu rồi",
      "Bao giờ hàng đến?",
      "Muốn trả lại sản phẩm",
      "Thanh toán bằng gì?"
    };

    System.out.println("🔍 Customer queries:");
    System.out.println();

    for (String query : queries) {
      System.out.println("Customer: \"" + query + "\"");

      // Embed query
      float[] queryEmb = registry.embed(query, Language.VIETNAMESE);

      // Find best match
      List<SearchResult> results = search(queryEmb, faqs, 1);

      if (!results.isEmpty()) {
        SearchResult best = results.get(0);
        System.out.printf("  → Answer: %s\n", best.doc.content);
        System.out.printf("     (Confidence: %.2f%%)\n", best.score * 100);
      }
      System.out.println();
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // DEMO 4: English Stack Overflow Style Q&A
  // ═══════════════════════════════════════════════════════════════

  private static void demo4_EnglishStackOverflowQA(EmbeddingServiceRegistry registry)
      throws Exception {
    printDemoHeader("Demo 4", "English Stack Overflow Style Q&A");

    // Test data: Programming Q&A
    List<Document> answers = new ArrayList<>();
    answers.add(
        new Document(
            "ans1",
            "To prevent SQL injection, use PreparedStatement instead of concatenating strings. This parameterizes queries and escapes dangerous characters.",
            Language.ENGLISH));
    answers.add(
        new Document(
            "ans2",
            "For thread-safe Singleton in Java, use enum or double-checked locking with volatile. Enums are preferred for simplicity.",
            Language.ENGLISH));
    answers.add(
        new Document(
            "ans3",
            "Handle NullPointerException by using Optional<T>, null checks, or Objects.requireNonNull(). Consider @NonNull annotations.",
            Language.ENGLISH));
    answers.add(
        new Document(
            "ans4",
            "Optimize database queries by adding indexes, using EXPLAIN, avoiding N+1 queries, and implementing connection pooling.",
            Language.ENGLISH));
    answers.add(
        new Document(
            "ans5",
            "Debug memory leaks with profilers like VisualVM or JProfiler. Look for unclosed resources, static collections, and listeners.",
            Language.ENGLISH));

    // Index answers
    System.out.println("📚 Indexing programming Q&A...");
    for (Document answer : answers) {
      float[] embedding = registry.embed(answer.content, answer.language);
      answer.setEmbedding(embedding);
    }
    System.out.println("   ✓ Indexed " + answers.size() + " answers");
    System.out.println();

    // Developer questions
    String[] questions = {
      "How to avoid SQL injection attacks?",
      "Best way to create singleton?",
      "Why am I getting null pointer errors?",
      "My app is running out of memory"
    };

    System.out.println("🔍 Developer questions:");
    System.out.println();

    for (String question : questions) {
      System.out.println("Question: \"" + question + "\"");

      // Embed query
      float[] queryEmb = registry.embed(question, Language.ENGLISH);

      // Find best answer
      List<SearchResult> results = search(queryEmb, answers, 2);

      System.out.println("  Best answers:");
      for (int i = 0; i < results.size(); i++) {
        SearchResult result = results.get(i);
        System.out.printf("    %d. [%d%% match] %s\n", i + 1, (int) (result.score * 100),
            truncate(result.doc.content, 70));
      }
      System.out.println();
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // DEMO 5: Mixed Language Project
  // ═══════════════════════════════════════════════════════════════

  private static void demo5_MixedLanguageProject(EmbeddingServiceRegistry registry)
      throws Exception {
    printDemoHeader("Demo 5", "Mixed Vietnamese/English Project");

    System.out.println("Scenario: Vietnamese company with English technical docs");
    System.out.println();

    // Mixed language documents
    List<Document> docs = new ArrayList<>();

    // Vietnamese business docs
    docs.add(
        new Document(
            "vi1",
            "Quy trình phê duyệt đơn hàng: Kiểm tra thông tin khách hàng, xác nhận tồn kho, tạo phiếu xuất kho.",
            Language.VIETNAMESE));
    docs.add(
        new Document(
            "vi2",
            "Chính sách bảo mật dữ liệu: Mã hóa thông tin nhạy cảm, backup định kỳ, kiểm soát truy cập.",
            Language.VIETNAMESE));

    // English technical docs
    docs.add(
        new Document(
            "en1",
            "API rate limiting implementation: Use Redis for counters, implement sliding window algorithm, return 429 status.",
            Language.ENGLISH));
    docs.add(
        new Document(
            "en2",
            "Logging best practices: Use structured logging (JSON), include correlation IDs, set appropriate log levels.",
            Language.ENGLISH));

    // Index all documents
    System.out.println("📚 Indexing mixed language documents...");
    for (Document doc : docs) {
      float[] embedding = registry.embed(doc.content, doc.language);
      doc.setEmbedding(embedding);
      System.out.printf(
          "   ✓ [%s] %s (dim: %d)\n",
          doc.language, truncate(doc.content, 40), embedding.length);
    }
    System.out.println();

    // Test searches in both languages
    System.out.println("🔍 Vietnamese query:");
    searchAndDisplay(registry, "Làm sao xử lý đơn hàng?", Language.VIETNAMESE, docs);

    System.out.println("🔍 English query:");
    searchAndDisplay(registry, "How to implement rate limiting?", Language.ENGLISH, docs);
  }

  // ═══════════════════════════════════════════════════════════════
  // DEMO 6: Performance Comparison
  // ═══════════════════════════════════════════════════════════════

  private static void demo6_PerformanceComparison(EmbeddingServiceRegistry registry)
      throws Exception {
    printDemoHeader("Demo 6", "Performance Comparison");

    String viText = "Đây là văn bản tiếng Việt để test hiệu suất của model PhoBERT.";
    String enText = "This is English text to test the performance of BGE-M3 model.";

    // Warm up
    System.out.println("🔥 Warming up models...");
    for (int i = 0; i < 5; i++) {
      registry.embed(viText, Language.VIETNAMESE);
      registry.embed(enText, Language.ENGLISH);
    }
    System.out.println("   ✓ Warm up complete");
    System.out.println();

    // Benchmark Vietnamese model
    System.out.println("⏱️  Vietnamese Model (PhoBERT, 768d):");
    long viTime = benchmarkModel(registry, viText, Language.VIETNAMESE, 100);
    System.out.printf("   Average: %.2f ms per embedding\n", viTime / 100.0);
    System.out.println();

    // Benchmark English model
    System.out.println("⏱️  English Model (BGE-M3, 1024d):");
    long enTime = benchmarkModel(registry, enText, Language.ENGLISH, 100);
    System.out.printf("   Average: %.2f ms per embedding\n", enTime / 100.0);
    System.out.println();

    // Comparison
    System.out.println("📊 Comparison:");
    System.out.printf("   Vietnamese: %.2f ms\n", viTime / 100.0);
    System.out.printf("   English:    %.2f ms\n", enTime / 100.0);
    System.out.printf("   Difference: %.2f ms (%.1f%%)\n", Math.abs(viTime - enTime) / 100.0,
        Math.abs(viTime - enTime) * 100.0 / Math.min(viTime, enTime));
  }

  // ═══════════════════════════════════════════════════════════════
  // UTILITY METHODS
  // ═══════════════════════════════════════════════════════════════

  private static List<SearchResult> search(float[] query, List<Document> docs, int topK) {
    List<SearchResult> results = new ArrayList<>();

    for (Document doc : docs) {
      float similarity = cosineSimilarity(query, doc.embedding);
      results.add(new SearchResult(doc, similarity));
    }

    results.sort(Comparator.comparingDouble(r -> -r.score));
    return results.subList(0, Math.min(topK, results.size()));
  }

  private static void searchAndDisplay(
      EmbeddingServiceRegistry registry, String query, Language lang, List<Document> docs)
      throws Exception {
    System.out.println("   Query: \"" + query + "\"");
    float[] queryEmb = registry.embed(query, lang);
    List<SearchResult> results = search(queryEmb, docs, 2);

    System.out.println("   Results:");
    for (int i = 0; i < results.size(); i++) {
      SearchResult result = results.get(i);
      System.out.printf(
          "     %d. [%.2f%%] [%s] %s\n",
          i + 1, result.score * 100, result.doc.language, truncate(result.doc.content, 50));
    }
    System.out.println();
  }

  private static long benchmarkModel(
      EmbeddingServiceRegistry registry, String text, Language lang, int iterations)
      throws Exception {
    long start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++) {
      registry.embed(text, lang);
    }
    return System.currentTimeMillis() - start;
  }

  private static float cosineSimilarity(float[] a, float[] b) {
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

  private static String truncate(String text, int maxLen) {
    if (text.length() <= maxLen) return text;
    return text.substring(0, maxLen - 3) + "...";
  }

  private static void printHeader(String title) {
    System.out.println();
    System.out.println("═══════════════════════════════════════════════════════════════");
    System.out.println("  " + title);
    System.out.println("═══════════════════════════════════════════════════════════════");
    System.out.println();
  }

  private static void printDemoHeader(String number, String title) {
    System.out.println();
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("  " + number + ": " + title);
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println();
  }

  private static void printSuccess(String message) {
    System.out.println();
    System.out.println("═══════════════════════════════════════════════════════════════");
    System.out.println("  ✅ " + message);
    System.out.println("═══════════════════════════════════════════════════════════════");
    System.out.println();
  }

  private static void printError(String message) {
    System.err.println();
    System.err.println("═══════════════════════════════════════════════════════════════");
    System.err.println("  ❌ " + message);
    System.err.println("═══════════════════════════════════════════════════════════════");
    System.err.println();
  }

  // ═══════════════════════════════════════════════════════════════
  // HELPER CLASSES
  // ═══════════════════════════════════════════════════════════════

  static class Document {
    String id;
    String content;
    Language language;
    float[] embedding;

    Document(String id, String content, Language language) {
      this.id = id;
      this.content = content;
      this.language = language;
    }

    void setEmbedding(float[] embedding) {
      this.embedding = embedding;
    }
  }

  static class SearchResult {
    Document doc;
    float score;

    SearchResult(Document doc, float score) {
      this.doc = doc;
      this.score = score;
    }
  }
}

