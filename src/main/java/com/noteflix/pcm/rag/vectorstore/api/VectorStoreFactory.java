package com.noteflix.pcm.rag.vectorstore.api;

import com.noteflix.pcm.rag.vectorstore.core.InMemoryVectorStore;
import com.noteflix.pcm.rag.vectorstore.core.LuceneVectorStore;
import com.noteflix.pcm.rag.vectorstore.core.QdrantVectorStore;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating vector store instances.
 *
 * <p>This allows easy swapping between different implementations: - LUCENE: Offline, embedded, no
 * external dependencies - QDRANT: High-performance, can run locally or remote - IN_MEMORY: For
 * testing
 *
 * <p>Usage:
 *
 * <pre>
 * // Lucene (offline)
 * VectorStore store = VectorStoreFactory.create(
 *     VectorStoreConfig.lucene("data/rag/index")
 * );
 *
 * // Qdrant (local)
 * VectorStore store = VectorStoreFactory.create(
 *     VectorStoreConfig.qdrantLocal()
 * );
 *
 * // Qdrant (remote)
 * VectorStore store = VectorStoreFactory.create(
 *     VectorStoreConfig.qdrant("api.qdrant.io", 6333, "api-key")
 * );
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class VectorStoreFactory {

  /**
   * Create vector store from config.
   *
   * @param config Vector store configuration
   * @return Vector store instance
   * @throws IllegalArgumentException if config is invalid
   */
  public static VectorStore create(VectorStoreConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("Config cannot be null");
    }

    if (config.getType() == null) {
      throw new IllegalArgumentException("Vector store type cannot be null");
    }

    log.info("Creating vector store: {}", config.getType());

    switch (config.getType()) {
      case LUCENE:
        return createLucene(config);

      case QDRANT:
        return createQdrant(config);

      case IN_MEMORY:
        return new InMemoryVectorStore();

      default:
        throw new IllegalArgumentException("Unsupported vector store type: " + config.getType());
    }
  }

  /** Create Lucene vector store (offline). */
  private static VectorStore createLucene(VectorStoreConfig config) {
    if (config.getStoragePath() == null || config.getStoragePath().isEmpty()) {
      throw new IllegalArgumentException("Storage path required for Lucene");
    }

    try {
      return new LuceneVectorStore(config.getStoragePath());
    } catch (Exception e) {
      throw new RuntimeException("Failed to create Lucene vector store", e);
    }
  }

  /** Create Qdrant vector store. */
  private static VectorStore createQdrant(VectorStoreConfig config) {
    if (config.getHost() == null || config.getHost().isEmpty()) {
      throw new IllegalArgumentException("Host required for Qdrant");
    }

    if (config.getPort() == null) {
      config.setPort(6333); // Default port
    }

    try {
      return new QdrantVectorStore(
          config.getHost(), config.getPort(), config.getApiKey(), config.getCollectionName());
    } catch (Exception e) {
      throw new RuntimeException("Failed to create Qdrant vector store", e);
    }
  }

  /** Create default In-Memory store (for development). */
  public static VectorStore createDefault() {
    return create(VectorStoreConfig.inMemory());
  }
}
