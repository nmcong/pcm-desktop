package com.noteflix.pcm.rag.core;

import com.noteflix.pcm.rag.api.VectorStore;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.RetrievalOptions;
import com.noteflix.pcm.rag.model.ScoredDocument;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Qdrant vector store implementation.
 *
 * <p>TODO: Implement when Qdrant Java client is added.
 *
 * <p>To use Qdrant: 1. Download Qdrant Java client: qdrant-client-1.x.x.jar 2. Run Qdrant: docker
 * run -p 6333:6333 qdrant/qdrant 3. Implement this class using Qdrant client
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class QdrantVectorStore implements VectorStore {

  private final String host;
  private final int port;
  private final String apiKey;
  private final String collectionName;

  public QdrantVectorStore(String host, int port, String apiKey, String collectionName) {
    this.host = host;
    this.port = port;
    this.apiKey = apiKey;
    this.collectionName = collectionName != null ? collectionName : "rag_documents";

    log.warn("QdrantVectorStore is not yet implemented. Use LuceneVectorStore for offline mode.");
    log.info("Qdrant config: {}:{}, collection: {}", host, port, this.collectionName);
  }

  @Override
  public void indexDocument(RAGDocument document) {
    throw new UnsupportedOperationException(
        "QdrantVectorStore not implemented. "
            + "Use VectorStoreConfig.lucene() for offline mode, "
            + "or implement this class with Qdrant Java client.");
  }

  @Override
  public void indexDocuments(List<RAGDocument> documents) {
    throw new UnsupportedOperationException("QdrantVectorStore not implemented");
  }

  @Override
  public List<ScoredDocument> search(String query, RetrievalOptions options) {
    throw new UnsupportedOperationException("QdrantVectorStore not implemented");
  }

  @Override
  public void deleteDocument(String documentId) {
    throw new UnsupportedOperationException("QdrantVectorStore not implemented");
  }

  @Override
  public void deleteDocuments(List<String> documentIds) {
    throw new UnsupportedOperationException("QdrantVectorStore not implemented");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("QdrantVectorStore not implemented");
  }

  @Override
  public long getDocumentCount() {
    return 0;
  }

  @Override
  public boolean exists(String documentId) {
    return false;
  }

  @Override
  public RAGDocument getDocument(String documentId) {
    return null;
  }

  @Override
  public void close() {
    log.info("QdrantVectorStore closed (stub)");
  }
}
