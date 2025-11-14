# Examples Guide - Vietnamese & English Embeddings

> 🎯 **Comprehensive examples with real test data**

---

## 📚 Available Examples

### 1. VietnameseEnglishEmbeddingDemo.java ⭐

**Location:** `src/main/java/com/noteflix/pcm/rag/examples/VietnameseEnglishEmbeddingDemo.java`

**What it demonstrates:**
- ✅ Vietnamese code documentation search
- ✅ English technical documentation
- ✅ Vietnamese customer support Q&A
- ✅ English Stack Overflow style Q&A
- ✅ Mixed language project
- ✅ Performance comparison

**How to run:**
```bash
# Build first
./scripts/build.sh

# Run demo
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*" \
  com.noteflix.pcm.rag.examples.VietnameseEnglishEmbeddingDemo
```

---

### 2. MultilingualEmbeddingTest.java

**Location:** `src/test/java/com/noteflix/pcm/rag/embedding/MultilingualEmbeddingTest.java`

**What it tests:**
- ✅ Vietnamese model functionality
- ✅ English model functionality
- ✅ Registry operations
- ✅ Semantic similarity
- ✅ Performance benchmarks
- ✅ Edge cases

**How to run:**
```bash
# Run tests with Maven
mvn test -Dtest=MultilingualEmbeddingTest

# Or with Gradle
./gradlew test --tests MultilingualEmbeddingTest

# Or directly with Java
java -cp "out:lib/*:test-lib/*" \
  org.junit.platform.console.ConsoleLauncher \
  --select-class com.noteflix.pcm.rag.embedding.MultilingualEmbeddingTest
```

---

## 🎯 Demo Scenarios

### Demo 1: Vietnamese Code Documentation Search

**Scenario:** Vietnamese company with code documentation in Vietnamese

**Test Data:**
```java
"Làm thế nào để validate dữ liệu đầu vào trong Java?"
"Hướng dẫn kết nối cơ sở dữ liệu MySQL trong Java"
"Cách xử lý exception trong Java"
```

**Queries:**
```java
"Kiểm tra dữ liệu người dùng nhập vào"
"Làm sao connect database?"
"Xử lý lỗi trong code Java"
```

**Expected Output:**
```
Query: "Kiểm tra dữ liệu người dùng nhập vào"
  1. [Score: 0.8542] Làm thế nào để validate dữ liệu đầu vào...
  2. [Score: 0.6234] Cách xử lý exception trong Java...
  3. [Score: 0.5123] Hướng dẫn kết nối cơ sở dữ liệu...
```

---

### Demo 2: English Technical Documentation

**Scenario:** Technical documentation for developers

**Test Data:**
```java
"How to implement JWT authentication in Spring Security"
"Docker containerization best practices"
"Building microservices with Spring Cloud"
```

**Queries:**
```java
"How to secure REST APIs with tokens?"
"Best practices for containers"
"Testing Java applications"
```

**Expected Output:**
```
Query: "How to secure REST APIs with tokens?"
  1. [Score: 0.8721] How to implement JWT authentication...
  2. [Score: 0.5234] Building microservices with Spring Cloud...
```

---

### Demo 3: Vietnamese Customer Support Q&A

**Scenario:** FAQ chatbot for Vietnamese customers

**Test Data:**
```java
"Làm thế nào để đặt lại mật khẩu?"
"Thời gian giao hàng là bao lâu?"
"Làm sao để hủy đơn hàng?"
"Chính sách đổi trả như thế nào?"
```

**Queries:**
```java
"Tôi quên mật khẩu rồi"          → Matches "Làm thế nào để đặt lại mật khẩu?"
"Bao giờ hàng đến?"              → Matches "Thời gian giao hàng là bao lâu?"
"Muốn trả lại sản phẩm"          → Matches "Chính sách đổi trả như thế nào?"
```

**Expected Output:**
```
Customer: "Tôi quên mật khẩu rồi"
  → Answer: Làm thế nào để đặt lại mật khẩu? Bạn có thể nhấn vào...
     (Confidence: 87%)
```

---

### Demo 4: English Stack Overflow Style Q&A

**Scenario:** Programming Q&A system

**Test Data:**
```java
"To prevent SQL injection, use PreparedStatement..."
"For thread-safe Singleton in Java, use enum..."
"Handle NullPointerException by using Optional<T>..."
```

**Queries:**
```java
"How to avoid SQL injection attacks?"
"Best way to create singleton?"
"Why am I getting null pointer errors?"
```

**Expected Output:**
```
Question: "How to avoid SQL injection attacks?"
  Best answers:
    1. [92% match] To prevent SQL injection, use PreparedStatement...
    2. [45% match] For thread-safe Singleton in Java, use enum...
```

---

### Demo 5: Mixed Language Project

**Scenario:** Vietnamese company with English technical docs

**Test Data:**
```
Vietnamese Business Docs:
  - "Quy trình phê duyệt đơn hàng..."
  - "Chính sách bảo mật dữ liệu..."

English Technical Docs:
  - "API rate limiting implementation..."
  - "Logging best practices..."
```

**Queries:**
```
Vietnamese: "Làm sao xử lý đơn hàng?"
English:    "How to implement rate limiting?"
```

**Expected Output:**
```
🔍 Vietnamese query:
   Query: "Làm sao xử lý đơn hàng?"
   Results:
     1. [85%] [VIETNAMESE] Quy trình phê duyệt đơn hàng...
     2. [42%] [VIETNAMESE] Chính sách bảo mật dữ liệu...

🔍 English query:
   Query: "How to implement rate limiting?"
   Results:
     1. [88%] [ENGLISH] API rate limiting implementation...
     2. [35%] [ENGLISH] Logging best practices...
```

---

### Demo 6: Performance Comparison

**Scenario:** Benchmark both models

**Expected Output:**
```
⏱️  Vietnamese Model (PhoBERT, 768d):
   Average: 38.42 ms per embedding

⏱️  English Model (BGE-M3, 1024d):
   Average: 42.18 ms per embedding

📊 Comparison:
   Vietnamese: 38.42 ms
   English:    42.18 ms
   Difference: 3.76 ms (9.8%)
```

---

## 🧪 Unit Tests Overview

### Test Categories

#### 1. Vietnamese Model Tests
```
✓ Single text embedding (768 dimensions)
✓ Batch embedding
✓ Semantic similarity
✓ Special cases (short, long, special characters)
```

#### 2. English Model Tests
```
✓ Single text embedding (1024 dimensions)
✓ Batch embedding
✓ Semantic similarity
✓ Code understanding
```

#### 3. Registry Tests
```
✓ Model availability check
✓ Dimension verification
✓ Statistics display
```

#### 4. Cross-Language Tests
```
✓ Dimension comparison
✓ Performance benchmark
```

---

## 📊 Test Data Sets

### Vietnamese Test Data

#### Code Documentation
```java
- "Làm thế nào để validate dữ liệu đầu vào trong Java?"
- "Hướng dẫn kết nối cơ sở dữ liệu MySQL trong Java"
- "Cách xử lý exception trong Java"
- "REST API design trong Spring Boot"
- "Tối ưu hóa hiệu suất ứng dụng Java"
```

#### Customer Support
```java
- "Làm thế nào để đặt lại mật khẩu?"
- "Thời gian giao hàng là bao lâu?"
- "Làm sao để hủy đơn hàng?"
- "Chính sách đổi trả như thế nào?"
- "Có những phương thức thanh toán nào?"
```

#### Business Queries
```java
- "Quy trình phê duyệt đơn hàng"
- "Chính sách bảo mật dữ liệu"
- "Kiểm soát truy cập hệ thống"
```

---

### English Test Data

#### Technical Documentation
```java
- "How to implement JWT authentication in Spring Security"
- "Docker containerization best practices"
- "Building microservices with Spring Cloud"
- "Database migration strategies"
- "Unit testing in Java with JUnit 5"
```

#### Programming Q&A
```java
- "To prevent SQL injection, use PreparedStatement..."
- "For thread-safe Singleton in Java, use enum..."
- "Handle NullPointerException by using Optional<T>..."
- "Optimize database queries by adding indexes..."
- "Debug memory leaks with profilers like VisualVM..."
```

#### API Documentation
```java
- "API rate limiting implementation"
- "Logging best practices"
- "Authentication and authorization"
```

---

## 🚀 Running the Examples

### Prerequisites

```bash
# 1. Models downloaded
ls -la data/models/vietnamese-sbert/model.onnx
ls -la data/models/bge-m3/model.onnx
ls -la data/models/all-MiniLM-L6-v2/model.onnx

# 2. Project built
./scripts/build.sh
```

---

### Run Main Demo

```bash
# Full demo (all 6 scenarios)
java -cp "out:lib/javafx/*:lib/others/*:lib/rag/*" \
  com.noteflix.pcm.rag.examples.VietnameseEnglishEmbeddingDemo
```

**Expected runtime:** ~2-3 minutes

**Output:** 
- 6 demo scenarios
- Semantic search examples
- Performance benchmarks
- Real results with similarity scores

---

### Run Unit Tests

```bash
# All tests
java -cp "out:lib/*:test-lib/*" \
  org.junit.platform.console.ConsoleLauncher \
  --scan-classpath

# Specific test class
java -cp "out:lib/*:test-lib/*" \
  org.junit.platform.console.ConsoleLauncher \
  --select-class com.noteflix.pcm.rag.embedding.MultilingualEmbeddingTest
```

**Expected runtime:** ~1-2 minutes

**Output:**
```
═══════════════════════════════════════════════════════════════
  Multilingual Embedding Tests
═══════════════════════════════════════════════════════════════

Test 1: Vietnamese single text embedding
   ✓ Embedding dimension: 768
   ✓ Sample values: [0.1234, -0.5678, 0.9012, ...]

Test 2: Vietnamese batch embedding
   ✓ Batch size: 3
   ✓ Each dimension: 768

...

═══════════════════════════════════════════════════════════════
  ✅ All Tests Completed
═══════════════════════════════════════════════════════════════

Tests run: 13, Successes: 13, Failures: 0
```

---

## 💡 Usage Patterns

### Pattern 1: Simple Search

```java
// Initialize
EmbeddingServiceRegistry registry = new EmbeddingServiceRegistry();

// Vietnamese search
String query = "Làm sao kết nối database?";
float[] queryEmb = registry.embed(query, Language.VIETNAMESE);

// Search in documents
for (Document doc : documents) {
    float similarity = cosineSimilarity(queryEmb, doc.embedding);
    if (similarity > 0.7) {
        System.out.println("Found: " + doc.content);
    }
}
```

---

### Pattern 2: Customer Support Bot

```java
// Load FAQ database
List<FAQ> faqs = loadFAQs(); // Vietnamese FAQs

// Index FAQs
for (FAQ faq : faqs) {
    faq.embedding = registry.embed(faq.answer, Language.VIETNAMESE);
}

// Handle customer query
String customerQuery = "Tôi quên mật khẩu";
float[] queryEmb = registry.embed(customerQuery, Language.VIETNAMESE);

// Find best answer
FAQ bestMatch = findBestMatch(queryEmb, faqs);
System.out.println("Answer: " + bestMatch.answer);
```

---

### Pattern 3: Code Documentation Search

```java
// Index code documentation
List<CodeDoc> docs = parseCodebase();

for (CodeDoc doc : docs) {
    Language lang = detectLanguage(doc.description);
    doc.embedding = registry.embed(doc.description, lang);
}

// Search
String searchQuery = "How to validate user input?";
float[] queryEmb = registry.embed(searchQuery, Language.ENGLISH);

List<CodeDoc> results = search(queryEmb, docs, 5);
```

---

### Pattern 4: Mixed Language Project

```java
// Index mixed language documents
for (Document doc : allDocuments) {
    // Each document has its language tag
    doc.embedding = registry.embed(doc.content, doc.language);
}

// Search respects language
String viQuery = "Xử lý đơn hàng";
List<Document> viResults = searchByLanguage(viQuery, Language.VIETNAMESE);

String enQuery = "API documentation";
List<Document> enResults = searchByLanguage(enQuery, Language.ENGLISH);
```

---

## 📈 Expected Results

### Semantic Similarity Thresholds

```
Score > 0.8  → Highly similar (exact match or paraphrase)
Score > 0.6  → Similar (same topic)
Score > 0.4  → Somewhat related
Score < 0.4  → Different topics
```

### Performance Expectations

```
Vietnamese Model (PhoBERT, 768d):
  - Single text: ~35-45 ms
  - Batch (10):  ~300-400 ms

English Model (BGE-M3, 1024d):
  - Single text: ~40-50 ms
  - Batch (10):  ~350-450 ms

Fallback Model (MiniLM, 384d):
  - Single text: ~15-20 ms
  - Batch (10):  ~120-180 ms
```

---

## 🎓 Learning Path

### For Beginners

1. ✅ Run `VietnameseEnglishEmbeddingDemo`
2. ✅ Observe output and understand semantic search
3. ✅ Modify test data with your own examples
4. ✅ Run unit tests to see assertions

### For Advanced Users

1. ✅ Study `EmbeddingServiceRegistry` implementation
2. ✅ Create custom use cases
3. ✅ Benchmark on your data
4. ✅ Optimize for production

---

## 🆘 Troubleshooting

### Demo doesn't run

```bash
# Check models exist
ls -la data/models/*/model.onnx

# Rebuild
./scripts/build.sh

# Run with verbose logging
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
  -cp "out:lib/*" \
  com.noteflix.pcm.rag.examples.VietnameseEnglishEmbeddingDemo
```

### Tests fail

```bash
# Check Java version
java -version  # Need Java 21+

# Check models
ls data/models/

# Run single test
java -cp "out:lib/*" \
  org.junit.platform.console.ConsoleLauncher \
  --select-method \
  com.noteflix.pcm.rag.embedding.MultilingualEmbeddingTest#testVietnameseSingleEmbedding
```

### Poor results

- ✅ Check if using correct language
- ✅ Verify model loaded (not fallback)
- ✅ Ensure documents are indexed
- ✅ Check similarity threshold

---

## 📚 Related Documentation

- [Quick Start Guide](./QUICK_START_MULTILINGUAL.md)
- [Implementation Summary](./IMPLEMENTATION_SUMMARY.md)
- [Multilingual Model Recommendations](./MULTILINGUAL_MODEL_RECOMMENDATIONS.md)
- [Troubleshooting](./TROUBLESHOOTING.md)

---

**Created:** November 14, 2024  
**Author:** PCM Team  
**Status:** ✅ Complete with test data

