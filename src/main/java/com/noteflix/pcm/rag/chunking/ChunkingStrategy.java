package com.noteflix.pcm.rag.chunking;

import com.noteflix.pcm.rag.model.RAGDocument;
import java.util.List;

/**
 * Interface for document chunking strategies.
 *
 * <p>Chunking breaks large documents into smaller pieces for better retrieval and LLM context
 * management.
 *
 * @author PCM Team
 * @version 1.0.0
 */
public interface ChunkingStrategy {

  /**
   * Chunk a document into smaller pieces.
   *
   * @param document Document to chunk
   * @return List of chunks
   */
  List<DocumentChunk> chunk(RAGDocument document);

  /**
   * Get recommended chunk size.
   *
   * @return Chunk size in characters
   */
  int getChunkSize();

  /**
   * Get chunk overlap size.
   *
   * @return Overlap size in characters
   */
  int getOverlapSize();
}
