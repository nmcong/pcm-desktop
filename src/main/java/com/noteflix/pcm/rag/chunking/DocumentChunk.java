package com.noteflix.pcm.rag.chunking;

import com.noteflix.pcm.rag.model.DocumentType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Enhanced document chunk with comprehensive metadata.
 *
 * <p>Represents a portion of a larger document, enriched with metadata for better indexing,
 * retrieval, and context understanding in RAG applications.
 *
 * <p>Features:
 * - Rich metadata from parent document
 * - Position and boundary information
 * - Quality metrics and token estimation
 * - Overlap context tracking
 * - Extensible custom metadata
 *
 * @author PCM Team
 * @version 2.0.0 - Enhanced with metadata
 */
@Data
@Builder
public class DocumentChunk {

  // === Core Properties ===

  /** Unique chunk ID (format: {documentId}_chunk_{index}) */
  private String chunkId;

  /** Original document ID */
  private String documentId;

  /** Chunk content text */
  private String content;

  /** Chunk index (0-based) */
  private int index;

  // === Position Information ===

  /** Start position in original document (character-based) */
  private int startPosition;

  /** End position in original document (character-based) */
  private int endPosition;

  /** Start line number in original document (1-based) */
  private Integer startLine;

  /** End line number in original document (1-based) */
  private Integer endLine;

  // === Document Metadata ===

  /** Original document title */
  private String documentTitle;

  /** Original document type */
  private DocumentType documentType;

  /** Original document source path */
  private String sourcePath;

  /** Document creation/modification time */
  private LocalDateTime documentTimestamp;

  // === Chunking Information ===

  /** Chunking strategy used */
  private String chunkingStrategy;

  /** Chunk size in characters */
  private int chunkSizeChars;

  /** Estimated token count (approximation) */
  private Integer estimatedTokens;

  /** Overlap size with previous chunk */
  private Integer overlapSize;

  /** Has overlap with previous chunk */
  @Builder.Default
  private boolean hasOverlapBefore = false;

  /** Has overlap with next chunk */
  @Builder.Default
  private boolean hasOverlapAfter = false;

  // === Quality Metrics ===

  /** Chunk quality score (0.0 - 1.0) */
  private Double qualityScore;

  /** Semantic coherence score (0.0 - 1.0) */
  private Double coherenceScore;

  /** Information density score (0.0 - 1.0) */
  private Double densityScore;

  // === Context Information ===

  /** Previous chunk ID for context linking */
  private String previousChunkId;

  /** Next chunk ID for context linking */
  private String nextChunkId;

  /** Section/chapter title containing this chunk */
  private String sectionTitle;

  /** Hierarchical level (for structured documents) */
  private Integer hierarchyLevel;

  // === Processing Metadata ===

  /** When this chunk was created */
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  /** Language detected in chunk (ISO 639-1 code) */
  private String language;

  /** Custom metadata for extensibility */
  @Builder.Default
  private Map<String, String> metadata = new HashMap<>();

  // === Utility Methods ===

  /**
   * Get chunk length in characters.
   *
   * @return Character count
   */
  public int getLength() {
    return content != null ? content.length() : 0;
  }

  /**
   * Estimate token count using simple heuristic (4 chars per token average).
   *
   * @return Estimated token count
   */
  public int getEstimatedTokenCount() {
    if (estimatedTokens != null) {
      return estimatedTokens;
    }
    // Simple heuristic: ~4 characters per token for English text
    return Math.max(1, getLength() / 4);
  }

  /**
   * Check if this chunk is at document boundary.
   *
   * @return true if first or last chunk
   */
  public boolean isAtBoundary() {
    return index == 0 || nextChunkId == null;
  }

  /**
   * Get chunk summary for logging/debugging.
   *
   * @return Formatted summary string
   */
  public String getSummary() {
    return String.format(
        "Chunk[id=%s, index=%d, pos=%d-%d, chars=%d, tokens=%d]",
        chunkId, index, startPosition, endPosition, getLength(), getEstimatedTokenCount());
  }

  /**
   * Add custom metadata.
   *
   * @param key Metadata key
   * @param value Metadata value
   */
  public void addMetadata(String key, String value) {
    if (metadata == null) {
      metadata = new HashMap<>();
    }
    metadata.put(key, value);
  }

  /**
   * Get custom metadata.
   *
   * @param key Metadata key
   * @return Metadata value or null
   */
  public String getMetadata(String key) {
    return metadata != null ? metadata.get(key) : null;
  }

  /**
   * Create a truncated preview of chunk content.
   *
   * @param maxLength Maximum preview length
   * @return Truncated content with ellipsis if needed
   */
  public String getContentPreview(int maxLength) {
    if (content == null || content.length() <= maxLength) {
      return content;
    }
    return content.substring(0, maxLength - 3) + "...";
  }
}
