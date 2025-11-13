package com.noteflix.pcm.rag.api;

import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.model.RetrievalOptions;
import com.noteflix.pcm.rag.model.ScoredDocument;
import java.util.List;

/**
 * Interface for vector storage implementations.
 *
 * <p>This allows easy swapping between different vector databases: - Lucene (offline, embedded) -
 * Qdrant (local/remote) - PostgreSQL + pgvector - Chroma - Milvus
 *
 * @author PCM Team
 * @version 1.0.0
 */
public interface VectorStore {

  /**
   * Index a single document.
   *
   * @param document Document to index
   */
  void indexDocument(RAGDocument document);

  /**
   * Index multiple documents in batch.
   *
   * @param documents Documents to index
   */
  void indexDocuments(List<RAGDocument> documents);

  /**
   * Search for documents.
   *
   * @param query Search query
   * @param options Retrieval options
   * @return List of scored documents
   */
  List<ScoredDocument> search(String query, RetrievalOptions options);

  /**
   * Delete a document by ID.
   *
   * @param documentId Document ID
   */
  void deleteDocument(String documentId);

  /**
   * Delete multiple documents.
   *
   * @param documentIds Document IDs
   */
  void deleteDocuments(List<String> documentIds);

  /** Clear all documents. */
  void clear();

  /**
   * Get document count.
   *
   * @return Number of indexed documents
   */
  long getDocumentCount();

  /**
   * Check if document exists.
   *
   * @param documentId Document ID
   * @return true if exists
   */
  boolean exists(String documentId);

  /**
   * Get document by ID.
   *
   * @param documentId Document ID
   * @return Document or null
   */
  RAGDocument getDocument(String documentId);

  /** Close/cleanup resources. */
  void close();
}
