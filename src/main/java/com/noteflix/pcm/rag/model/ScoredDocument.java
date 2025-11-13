package com.noteflix.pcm.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A document with relevance score from search results.
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoredDocument {

  /** The document */
  private RAGDocument document;

  /** Relevance score (0.0 to 1.0) */
  private double score;

  /** Rank in search results (1-based) */
  private int rank;

  /** Highlighted snippet (if available) */
  private String snippet;
}
