package com.noteflix.pcm.rag.vectorstore.core;

import com.noteflix.pcm.rag.vectorstore.api.VectorStore;
import com.noteflix.pcm.rag.vectorstore.core.QdrantClient.QdrantPoint;
import com.noteflix.pcm.rag.vectorstore.core.QdrantClient.QdrantSearchResult;
import com.noteflix.pcm.rag.embedding.EmbeddingService;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.RetrievalOptions;
import com.noteflix.pcm.rag.model.ScoredDocument;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Qdrant vector store implementation.
 *
 * <p>Production-ready implementation using Qdrant REST API.
 *
 * <p>Features: - Fast vector similarity search - Scalable (handles millions of documents) - Can run
 * locally or remote - No external Java client required (uses REST API)
 *
 * <p>Setup: 1. Run Qdrant: docker run -p 6333:6333 qdrant/qdrant 2. Or use embedded: new
 * QdrantEmbeddedManager().start() 3. Create store:
 * VectorStoreFactory.create(VectorStoreConfig.qdrantLocal())
 *
 * @author PCM Team
 * @version 2.0.0
 */
@Slf4j
public class QdrantVectorStore implements VectorStore {

  private final String collectionName;
  private final QdrantClient client;
  private final EmbeddingService embeddingService;
  private final int vectorDimension;

  /**
   * Create Qdrant vector store.
   *
   * @param host Qdrant host
   * @param port Qdrant port
   * @param apiKey API key (optional)
   * @param collectionName Collection name
   */
  public QdrantVectorStore(String host, int port, String apiKey, String collectionName) {
    this(host, port, apiKey, collectionName, null, 384);
  }

  /**
   * Create Qdrant vector store with embedding service.
   *
   * @param host Qdrant host
   * @param port Qdrant port
   * @param apiKey API key (optional)
   * @param collectionName Collection name
   * @param embeddingService Embedding service
   * @param vectorDimension Vector dimension
   */
  public QdrantVectorStore(
      String host,
      int port,
      String apiKey,
      String collectionName,
      EmbeddingService embeddingService,
      int vectorDimension) {

    this.collectionName = collectionName != null ? collectionName : "rag_documents";
    this.client = new QdrantClient(host, port, apiKey);
    this.embeddingService = embeddingService;
    this.vectorDimension = vectorDimension;

    log.info(
        "🚀 QdrantVectorStore initialized: {}:{}, collection: {}", host, port, this.collectionName);

    // Create collection if not exists with proper error handling
    try {
      client.createCollectionIfNotExists(this.collectionName, vectorDimension);
      log.info("✅ Collection '{}' is ready", this.collectionName);
    } catch (IOException e) {
      log.error("❌ Failed to initialize collection: {}", e.getMessage());
      // Verify collection exists and is accessible
      try {
        if (client.collectionExists(this.collectionName)) {
          log.info("Collection '{}' already exists and is accessible", this.collectionName);
        } else {
          throw new RuntimeException(
              "Collection does not exist and cannot be created: " + e.getMessage(), e);
        }
      } catch (Exception verifyError) {
        throw new RuntimeException(
            "Cannot access Qdrant server or collection: " + verifyError.getMessage(), verifyError);
      }
    }
  }

  @Override
  public void indexDocument(RAGDocument document) {
    if (document == null || document.getId() == null) {
      throw new IllegalArgumentException("Document and ID cannot be null");
    }

    if (embeddingService == null) {
      throw new IllegalStateException(
          "Embedding service not configured. Use constructor with EmbeddingService parameter or"
              + " call indexDocuments() with pre-computed vectors.");
    }

    log.debug("Indexing document: {}", document.getId());

    try {
      // Generate embedding
      float[] vector = embeddingService.embed(document.getContent());

      // Create point
      Map<String, String> payload = createPayload(document);
      QdrantPoint point = new QdrantPoint(document.getId(), vector, payload);

      // Upsert to Qdrant
      client.upsertPoints(collectionName, List.of(point));

      log.debug("✅ Document indexed: {}", document.getId());

    } catch (Exception e) {
      log.error("❌ Failed to index document: {}", document.getId(), e);
      throw new RuntimeException("Failed to index document: " + document.getId(), e);
    }
  }

  @Override
  public void indexDocuments(List<RAGDocument> documents) {
    if (documents == null || documents.isEmpty()) {
      return;
    }

    log.info("Indexing {} documents...", documents.size());

    if (embeddingService == null) {
      throw new IllegalStateException("Embedding service not configured");
    }

    try {
      // Process in chunks for memory efficiency
      int chunkSize = calculateOptimalChunkSize(documents);

      for (int i = 0; i < documents.size(); i += chunkSize) {
        int endIndex = Math.min(i + chunkSize, documents.size());
        List<RAGDocument> chunk = documents.subList(i, endIndex);

        processDocumentChunk(chunk);

        log.info("Progress: {}/{} documents indexed", endIndex, documents.size());
      }

      log.info("✅ {} documents indexed successfully", documents.size());

    } catch (Exception e) {
      log.error("❌ Failed to index documents", e);
      throw new RuntimeException("Failed to index documents", e);
    }
  }

  /** Process a chunk of documents using batch embeddings for optimal performance. */
  private void processDocumentChunk(List<RAGDocument> documents) throws IOException {
    // Extract texts for batch embedding
    String[] texts = documents.stream().map(RAGDocument::getContent).toArray(String[]::new);

    // Generate embeddings in batch - much faster than individual calls
    float[][] embeddings = embeddingService.embedBatch(texts);

    // Create points with pre-computed embeddings
    List<QdrantPoint> points = new ArrayList<>();
    for (int i = 0; i < documents.size(); i++) {
      RAGDocument doc = documents.get(i);
      float[] vector = embeddings[i];
      Map<String, String> payload = createPayload(doc);
      QdrantPoint point = new QdrantPoint(doc.getId(), vector, payload);
      points.add(point);
    }

    // Batch upsert to Qdrant
    client.upsertPoints(collectionName, points);
  }

  /** Calculate optimal chunk size based on estimated memory usage. */
  private int calculateOptimalChunkSize(List<RAGDocument> documents) {
    if (documents.isEmpty()) {
      return 100;
    }

    // Estimate average document size
    long totalSize =
        documents.stream()
            .limit(Math.min(10, documents.size())) // Sample first 10 docs
            .mapToLong(doc -> doc.getContent() != null ? doc.getContent().length() : 0)
            .sum();

    double avgDocSize = (double) totalSize / Math.min(10, documents.size());

    // Estimate memory usage: text + embeddings (4 bytes per float * dimension)
    double avgEmbeddingSize = vectorDimension * 4; // 4 bytes per float
    double estimatedMemoryPerDoc =
        (avgDocSize * 2) + avgEmbeddingSize; // 2x for tokenization overhead

    // Target max 100MB per chunk
    long targetMemoryMB = 100 * 1024 * 1024;
    int chunkSize = (int) Math.max(1, targetMemoryMB / estimatedMemoryPerDoc);

    // Clamp between reasonable bounds
    chunkSize = Math.max(10, Math.min(chunkSize, 1000));

    log.debug(
        "Calculated optimal chunk size: {} (avg doc size: {:.0f} bytes)", chunkSize, avgDocSize);
    return chunkSize;
  }

  @Override
  public List<ScoredDocument> search(String query, RetrievalOptions options) {
    if (query == null || query.trim().isEmpty()) {
      return List.of();
    }

    if (embeddingService == null) {
      throw new IllegalStateException("Embedding service not configured");
    }

    log.debug("Searching with query: {}", query);

    try {
      // Generate query embedding
      float[] queryVector = embeddingService.embed(query);

      // Search
      int limit = options != null ? options.getMaxResults() : 10;
      Map<String, Object> filter = null;
      if (options != null && options.getFilters() != null) {
        filter = new HashMap<>(options.getFilters());
      }

      List<QdrantSearchResult> results = client.search(collectionName, queryVector, limit, filter);

      // Convert to ScoredDocument
      List<ScoredDocument> scoredDocs = new ArrayList<>();
      int rank = 1;
      for (QdrantSearchResult result : results) {
        RAGDocument doc = payloadToDocument(result.getId(), result.getPayload());
        // Use builder pattern for ScoredDocument
        ScoredDocument scoredDoc =
            ScoredDocument.builder()
                .document(doc)
                .score(result.getScore())
                .rank(rank++)
                .snippet(truncateContent(doc.getContent(), 200))
                .build();
        scoredDocs.add(scoredDoc);
      }

      log.debug("✅ Found {} results", scoredDocs.size());
      return scoredDocs;

    } catch (Exception e) {
      log.error("❌ Search failed", e);
      throw new RuntimeException("Search failed", e);
    }
  }

  @Override
  public void deleteDocument(String documentId) {
    if (documentId == null) {
      return;
    }

    log.debug("Deleting document: {}", documentId);

    try {
      client.deletePoints(collectionName, List.of(documentId));
      log.debug("✅ Document deleted: {}", documentId);

    } catch (Exception e) {
      log.error("❌ Failed to delete document: {}", documentId, e);
      throw new RuntimeException("Failed to delete document: " + documentId, e);
    }
  }

  @Override
  public void deleteDocuments(List<String> documentIds) {
    if (documentIds == null || documentIds.isEmpty()) {
      return;
    }

    log.info("Deleting {} documents...", documentIds.size());

    try {
      // Batch delete every 100 IDs
      List<String> batch = new ArrayList<>();
      for (String id : documentIds) {
        batch.add(id);
        if (batch.size() >= 100) {
          client.deletePoints(collectionName, batch);
          batch.clear();
        }
      }

      // Delete remaining
      if (!batch.isEmpty()) {
        client.deletePoints(collectionName, batch);
      }

      log.info("✅ {} documents deleted", documentIds.size());

    } catch (Exception e) {
      log.error("❌ Failed to delete documents", e);
      throw new RuntimeException("Failed to delete documents", e);
    }
  }

  @Override
  public void clear() {
    log.info("Clearing collection: {}", collectionName);

    try {
      // Delete and recreate collection
      client.deleteCollection(collectionName);
      client.createCollectionIfNotExists(collectionName, vectorDimension);

      log.info("✅ Collection cleared");

    } catch (Exception e) {
      log.error("❌ Failed to clear collection", e);
      throw new RuntimeException("Failed to clear collection", e);
    }
  }

  @Override
  public long getDocumentCount() {
    try {
      QdrantClient.QdrantCollectionInfo info = client.getCollectionInfo(collectionName);
      return info.getPointsCount();

    } catch (Exception e) {
      log.error("Failed to get document count", e);
      return 0;
    }
  }

  @Override
  public boolean exists(String documentId) {
    if (documentId == null) {
      return false;
    }

    try {
      QdrantPoint point = client.getPoint(collectionName, documentId);
      return point != null;

    } catch (Exception e) {
      log.debug("Document not found: {}", documentId);
      return false;
    }
  }

  @Override
  public RAGDocument getDocument(String documentId) {
    if (documentId == null) {
      return null;
    }

    try {
      QdrantPoint point = client.getPoint(collectionName, documentId);
      if (point == null) {
        return null;
      }

      return payloadToDocument(point.getId(), point.getPayload());

    } catch (Exception e) {
      log.error("Failed to get document: {}", documentId, e);
      return null;
    }
  }

  @Override
  public void close() {
    log.info("Closing QdrantVectorStore");
    // HTTP client doesn't need explicit closing
  }

  // ========== Helper Methods ==========

  private Map<String, String> createPayload(RAGDocument document) {
    Map<String, String> payload = new HashMap<>();

    payload.put("content", document.getContent());

    if (document.getTitle() != null) {
      payload.put("title", document.getTitle());
    }

    if (document.getType() != null) {
      payload.put("type", document.getType().toString());
    }

    if (document.getSourcePath() != null) {
      payload.put("sourcePath", document.getSourcePath());
    }

    if (document.getIndexedAt() != null) {
      payload.put(
          "indexedAt", document.getIndexedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    // Add all metadata
    if (document.getMetadata() != null) {
      document.getMetadata().forEach((key, value) -> payload.put("metadata_" + key, value));
    }

    return payload;
  }

  private RAGDocument payloadToDocument(String id, Map<String, String> payload) {
    RAGDocument.RAGDocumentBuilder builder = RAGDocument.builder().id(id);

    if (payload.containsKey("content")) {
      builder.content(payload.get("content"));
    }

    if (payload.containsKey("title")) {
      builder.title(payload.get("title"));
    }

    if (payload.containsKey("type")) {
      try {
        builder.type(com.noteflix.pcm.rag.model.DocumentType.valueOf(payload.get("type")));
      } catch (Exception e) {
        log.debug("Invalid document type: {}", payload.get("type"));
      }
    }

    if (payload.containsKey("sourcePath")) {
      builder.sourcePath(payload.get("sourcePath"));
    }

    if (payload.containsKey("indexedAt")) {
      try {
        builder.indexedAt(
            LocalDateTime.parse(payload.get("indexedAt"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      } catch (Exception e) {
        log.debug("Invalid date format: {}", payload.get("indexedAt"));
      }
    }

    // Extract metadata
    Map<String, String> metadata = new HashMap<>();
    payload.forEach(
        (key, value) -> {
          if (key.startsWith("metadata_")) {
            metadata.put(key.substring("metadata_".length()), value);
          }
        });

    if (!metadata.isEmpty()) {
      builder.metadata(metadata);
    }

    return builder.build();
  }

  private String truncateContent(String content, int maxLength) {
    if (content == null) {
      return null;
    }
    if (content.length() <= maxLength) {
      return content;
    }
    return content.substring(0, maxLength) + "...";
  }
}
