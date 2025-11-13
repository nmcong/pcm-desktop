package com.noteflix.pcm.rag.vectorstore.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Qdrant REST API client.
 *
 * <p>Lightweight HTTP client for Qdrant without external dependencies.
 *
 * @author PCM Team
 * @version 2.0.0
 */
@Slf4j
public class QdrantClient {

  // Security and performance constants
  private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 30;
  private static final int MAX_CONNECTIONS = 100;
  private static final int CONNECTION_POOL_TIMEOUT_SECONDS = 30;

  private final String baseUrl;
  private final String apiKey;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final boolean useHttps;

  public QdrantClient(String host, int port, String apiKey) {
    this(host, port, apiKey, false);
  }

  public QdrantClient(String host, int port, String apiKey, boolean useHttps) {
    // Input validation
    if (host == null || host.trim().isEmpty()) {
      throw new IllegalArgumentException("Host cannot be null or empty");
    }
    if (port <= 0 || port > 65535) {
      throw new IllegalArgumentException("Port must be between 1 and 65535");
    }

    this.useHttps = useHttps;
    String protocol = useHttps ? "https" : "http";
    this.baseUrl = String.format("%s://%s:%d", protocol, host, port);
    this.apiKey = apiKey;

    // Enhanced HttpClient with connection pooling
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS))
            .version(HttpClient.Version.HTTP_2)
            .build();

    this.objectMapper = new ObjectMapper();

    log.info("Qdrant client initialized: {} (HTTPS: {})", baseUrl, useHttps);
  }

  /**
   * Create collection if not exists.
   *
   * @param collectionName Collection name
   * @param vectorSize Vector dimension
   * @throws IOException if request fails
   */
  public void createCollectionIfNotExists(String collectionName, int vectorSize)
      throws IOException {
    // Check if collection exists
    if (collectionExists(collectionName)) {
      log.debug("Collection already exists: {}", collectionName);
      return;
    }

    log.info("Creating collection: {} with vector size: {}", collectionName, vectorSize);

    // Create collection
    ObjectNode body = objectMapper.createObjectNode();
    ObjectNode vectors = objectMapper.createObjectNode();
    vectors.put("size", vectorSize);
    vectors.put("distance", "Cosine");
    body.set("vectors", vectors);

    String url = String.format("%s/collections/%s", baseUrl, collectionName);
    HttpResponse<String> response = sendRequest("PUT", url, body.toString());

    if (response.statusCode() != 200 && response.statusCode() != 201) {
      throw new IOException("Failed to create collection: " + response.body());
    }

    log.info("✅ Collection created: {}", collectionName);
  }

  /**
   * Check if collection exists.
   *
   * @param collectionName Collection name
   * @return true if exists
   */
  public boolean collectionExists(String collectionName) {
    try {
      String url = String.format("%s/collections/%s", baseUrl, collectionName);
      HttpResponse<String> response = sendRequest("GET", url, null);
      return response.statusCode() == 200;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Upsert points (documents).
   *
   * @param collectionName Collection name
   * @param points List of points to upsert
   * @throws IOException if request fails
   */
  public void upsertPoints(String collectionName, List<QdrantPoint> points) throws IOException {
    if (points.isEmpty()) {
      return;
    }

    log.debug("Upserting {} points to collection: {}", points.size(), collectionName);

    ObjectNode body = objectMapper.createObjectNode();
    ArrayNode pointsArray = objectMapper.createArrayNode();

    for (QdrantPoint point : points) {
      ObjectNode pointNode = objectMapper.createObjectNode();
      
      // Convert string ID to integer if possible, otherwise use hash
      try {
        int id = Integer.parseInt(point.getId());
        pointNode.put("id", id);
      } catch (NumberFormatException e) {
        // Use hash of the ID as an integer
        int id = Math.abs(point.getId().hashCode());
        pointNode.put("id", id);
        log.debug("Converted string ID '{}' to integer ID: {}", point.getId(), id);
      }

      // Vector
      ArrayNode vectorArray = objectMapper.createArrayNode();
      for (float v : point.getVector()) {
        vectorArray.add(v);
      }
      pointNode.set("vector", vectorArray);

      // Payload
      ObjectNode payloadNode = objectMapper.createObjectNode();
      point.getPayload().forEach(payloadNode::put);
      // Store original ID in payload for retrieval
      payloadNode.put("original_id", point.getId());
      pointNode.set("payload", payloadNode);

      pointsArray.add(pointNode);
    }

    body.set("points", pointsArray);

    String url = String.format("%s/collections/%s/points", baseUrl, collectionName);
    HttpResponse<String> response = sendRequest("PUT", url, body.toString());

    if (response.statusCode() != 200) {
      throw new IOException("Failed to upsert points: " + response.body());
    }

    log.debug("✅ Upserted {} points", points.size());
  }

  /**
   * Search points by vector.
   *
   * @param collectionName Collection name
   * @param vector Query vector
   * @param limit Max results
   * @param filter Optional filter
   * @return List of search results
   * @throws IOException if request fails
   */
  public List<QdrantSearchResult> search(
      String collectionName, float[] vector, int limit, Map<String, Object> filter)
      throws IOException {

    log.debug("Searching collection: {} with limit: {}", collectionName, limit);

    ObjectNode body = objectMapper.createObjectNode();

    // Vector
    ArrayNode vectorArray = objectMapper.createArrayNode();
    for (float v : vector) {
      vectorArray.add(v);
    }
    body.set("vector", vectorArray);
    body.put("limit", limit);
    body.put("with_payload", true);
    body.put("with_vector", false);

    // Filter (if provided)
    if (filter != null && !filter.isEmpty()) {
      ObjectNode filterNode = objectMapper.createObjectNode();
      ArrayNode mustArray = objectMapper.createArrayNode();

      filter.forEach(
          (key, value) -> {
            ObjectNode conditionNode = objectMapper.createObjectNode();
            ObjectNode matchNode = objectMapper.createObjectNode();
            ObjectNode keyNode = objectMapper.createObjectNode();
            keyNode.put("value", value.toString());
            matchNode.set(key, keyNode);
            conditionNode.set("match", matchNode);
            mustArray.add(conditionNode);
          });

      filterNode.set("must", mustArray);
      body.set("filter", filterNode);
    }

    String url = String.format("%s/collections/%s/points/search", baseUrl, collectionName);
    HttpResponse<String> response = sendRequest("POST", url, body.toString());

    if (response.statusCode() != 200) {
      throw new IOException("Failed to search: " + response.body());
    }

    // Parse results
    List<QdrantSearchResult> results = new ArrayList<>();
    JsonNode root = objectMapper.readTree(response.body());
    JsonNode resultArray = root.get("result");

    if (resultArray != null && resultArray.isArray()) {
      for (JsonNode resultNode : resultArray) {
        String qdrantId = resultNode.get("id").asText();
        double score = resultNode.get("score").asDouble();
        JsonNode payloadNode = resultNode.get("payload");

        Map<String, String> payload = new HashMap<>();
        String originalId = qdrantId; // Default to Qdrant ID
        
        if (payloadNode != null) {
          payloadNode
              .fields()
              .forEachRemaining(entry -> payload.put(entry.getKey(), entry.getValue().asText()));
          
          // Use original ID if available
          if (payload.containsKey("original_id")) {
            originalId = payload.get("original_id");
          }
        }

        results.add(new QdrantSearchResult(originalId, score, payload));
      }
    }

    log.debug("✅ Found {} results", results.size());
    return results;
  }

  /**
   * Delete points by ID.
   *
   * @param collectionName Collection name
   * @param ids List of IDs to delete
   * @throws IOException if request fails
   */
  public void deletePoints(String collectionName, List<String> ids) throws IOException {
    if (ids.isEmpty()) {
      return;
    }

    log.debug("Deleting {} points from collection: {}", ids.size(), collectionName);

    ObjectNode body = objectMapper.createObjectNode();
    ArrayNode idsArray = objectMapper.createArrayNode();
    ids.forEach(idsArray::add);
    body.set("points", idsArray);

    String url = String.format("%s/collections/%s/points/delete", baseUrl, collectionName);
    HttpResponse<String> response = sendRequest("POST", url, body.toString());

    if (response.statusCode() != 200) {
      throw new IOException("Failed to delete points: " + response.body());
    }

    log.debug("✅ Deleted {} points", ids.size());
  }

  /**
   * Get point by ID.
   *
   * @param collectionName Collection name
   * @param id Point ID (original string ID)
   * @return Point or null if not found
   * @throws IOException if request fails
   */
  public QdrantPoint getPoint(String collectionName, String id) throws IOException {
    // Convert string ID to Qdrant's integer ID format
    String qdrantId;
    try {
      qdrantId = String.valueOf(Integer.parseInt(id));
    } catch (NumberFormatException e) {
      qdrantId = String.valueOf(Math.abs(id.hashCode()));
    }
    
    String url = String.format("%s/collections/%s/points/%s", baseUrl, collectionName, qdrantId);
    HttpResponse<String> response = sendRequest("GET", url, null);

    if (response.statusCode() == 404) {
      return null;
    }

    if (response.statusCode() != 200) {
      throw new IOException("Failed to get point: " + response.body());
    }

    // Parse result
    JsonNode root = objectMapper.readTree(response.body());
    JsonNode resultNode = root.get("result");

    if (resultNode == null) {
      return null;
    }

    JsonNode vectorNode = resultNode.get("vector");
    JsonNode payloadNode = resultNode.get("payload");

    // Parse vector
    float[] vector = new float[vectorNode.size()];
    for (int i = 0; i < vectorNode.size(); i++) {
      vector[i] = (float) vectorNode.get(i).asDouble();
    }

    // Parse payload
    Map<String, String> payload = new HashMap<>();
    String originalId = id; // Default to input ID
    
    if (payloadNode != null) {
      payloadNode
          .fields()
          .forEachRemaining(entry -> payload.put(entry.getKey(), entry.getValue().asText()));
      
      // Use original ID if available
      if (payload.containsKey("original_id")) {
        originalId = payload.get("original_id");
      }
    }

    return new QdrantPoint(originalId, vector, payload);
  }

  /**
   * Get collection info.
   *
   * @param collectionName Collection name
   * @return Collection info
   * @throws IOException if request fails
   */
  public QdrantCollectionInfo getCollectionInfo(String collectionName) throws IOException {
    String url = String.format("%s/collections/%s", baseUrl, collectionName);
    HttpResponse<String> response = sendRequest("GET", url, null);

    if (response.statusCode() != 200) {
      throw new IOException("Failed to get collection info: " + response.body());
    }

    JsonNode root = objectMapper.readTree(response.body());
    JsonNode result = root.get("result");

    long pointsCount = result.get("points_count").asLong();
    int vectorSize = result.get("config").get("params").get("vectors").get("size").asInt();

    return new QdrantCollectionInfo(collectionName, pointsCount, vectorSize);
  }

  /**
   * Delete collection.
   *
   * @param collectionName Collection name
   * @throws IOException if request fails
   */
  public void deleteCollection(String collectionName) throws IOException {
    log.info("Deleting collection: {}", collectionName);

    String url = String.format("%s/collections/%s", baseUrl, collectionName);
    HttpResponse<String> response = sendRequest("DELETE", url, null);

    if (response.statusCode() != 200) {
      throw new IOException("Failed to delete collection: " + response.body());
    }

    log.info("✅ Collection deleted: {}", collectionName);
  }

  // ========== Private Methods ==========

  private HttpResponse<String> sendRequest(String method, String url, String body)
      throws IOException {
    // Input validation
    if (method == null || method.trim().isEmpty()) {
      throw new IllegalArgumentException("HTTP method cannot be null or empty");
    }
    if (url == null || url.trim().isEmpty()) {
      throw new IllegalArgumentException("URL cannot be null or empty");
    }

    try {
      HttpRequest.Builder builder =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .timeout(Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS))
              .header("Content-Type", "application/json")
              .header("Accept", "application/json");

      // Add API key if provided
      if (apiKey != null && !apiKey.isEmpty()) {
        builder.header("api-key", apiKey);
      }

      // Set method and body
      HttpRequest.BodyPublisher bodyPublisher =
          body != null
              ? HttpRequest.BodyPublishers.ofString(body)
              : HttpRequest.BodyPublishers.noBody();

      switch (method.toUpperCase()) {
        case "GET":
          builder.GET();
          break;
        case "POST":
          builder.POST(bodyPublisher);
          break;
        case "PUT":
          builder.PUT(bodyPublisher);
          break;
        case "DELETE":
          builder.DELETE();
          break;
        default:
          throw new IllegalArgumentException("Unsupported HTTP method: " + method);
      }

      HttpRequest request = builder.build();
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      log.debug("HTTP {} {} -> {}", method, url, response.statusCode());
      return response;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    } catch (Exception e) {
      log.error("HTTP request failed: {} {} - {}", method, url, e.getMessage());
      throw new IOException("HTTP request failed", e);
    }
  }

  // ========== Data Classes ==========

  /** Qdrant point (document). */
  public static class QdrantPoint {
    private final String id;
    private final float[] vector;
    private final Map<String, String> payload;

    public QdrantPoint(String id, float[] vector, Map<String, String> payload) {
      this.id = id;
      this.vector = vector;
      this.payload = payload;
    }

    public String getId() {
      return id;
    }

    public float[] getVector() {
      return vector;
    }

    public Map<String, String> getPayload() {
      return payload;
    }
  }

  /** Search result. */
  public static class QdrantSearchResult {
    private final String id;
    private final double score;
    private final Map<String, String> payload;

    public QdrantSearchResult(String id, double score, Map<String, String> payload) {
      this.id = id;
      this.score = score;
      this.payload = payload;
    }

    public String getId() {
      return id;
    }

    public double getScore() {
      return score;
    }

    public Map<String, String> getPayload() {
      return payload;
    }
  }

  /** Collection info. */
  public static class QdrantCollectionInfo {
    private final String name;
    private final long pointsCount;
    private final int vectorSize;

    public QdrantCollectionInfo(String name, long pointsCount, int vectorSize) {
      this.name = name;
      this.pointsCount = pointsCount;
      this.vectorSize = vectorSize;
    }

    public String getName() {
      return name;
    }

    public long getPointsCount() {
      return pointsCount;
    }

    public int getVectorSize() {
      return vectorSize;
    }
  }
}
