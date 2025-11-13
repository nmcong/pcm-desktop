package com.noteflix.pcm.rag.api;

/**
 * Supported vector store types.
 *
 * @author PCM Team
 * @version 1.0.0
 */
public enum VectorStoreType {
  /** Apache Lucene (embedded, offline) */
  LUCENE,

  /** Qdrant (local or remote) */
  QDRANT,

  /** PostgreSQL + pgvector */
  PGVECTOR,

  /** Chroma (embedded or server) */
  CHROMA,

  /** In-memory (for testing) */
  IN_MEMORY
}
