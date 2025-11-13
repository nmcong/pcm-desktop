package com.noteflix.pcm.rag.chunking.strategies;

import com.noteflix.pcm.rag.chunking.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.DocumentChunk;
import com.noteflix.pcm.rag.model.RAGDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Enhanced fixed-size chunking strategy.
 *
 * <p>Splits documents into chunks of fixed size with overlap. This is the simplest and most
 * predictable chunking strategy, ideal for:
 * - Documents with consistent structure
 * - When you need predictable chunk sizes
 * - Simple text processing pipelines
 * - Performance-critical applications
 *
 * <p>Features:
 * - Configurable chunk size and overlap
 * - Rich metadata generation
 * - Quality scoring based on content characteristics
 * - Context linking between chunks
 *
 * @author PCM Team
 * @version 2.0.0 - Enhanced with metadata and quality metrics
 */
@Slf4j
public class FixedSizeChunking implements ChunkingStrategy {

  private final int chunkSize;
  private final int overlapSize;

  public FixedSizeChunking(int chunkSize, int overlapSize) {
    validateConfig(chunkSize, overlapSize);
    this.chunkSize = chunkSize;
    this.overlapSize = overlapSize;
  }

  /** Create default chunking (1000 chars, 200 overlap). */
  public static FixedSizeChunking defaults() {
    return new FixedSizeChunking(1000, 200);
  }

  /** Create small chunks for detailed analysis. */
  public static FixedSizeChunking small() {
    return new FixedSizeChunking(500, 100);
  }

  /** Create large chunks for broad context. */
  public static FixedSizeChunking large() {
    return new FixedSizeChunking(2000, 400);
  }

  @Override
  public List<DocumentChunk> chunk(RAGDocument document) {
    List<DocumentChunk> chunks = new ArrayList<>();
    String content = document.getContent();

    if (content == null || content.isEmpty()) {
      log.warn("Document {} has empty content", document.getId());
      return chunks;
    }

    int position = 0;
    int index = 0;
    String previousChunkId = null;

    while (position < content.length()) {
      int end = Math.min(position + chunkSize, content.length());
      String chunkContent = content.substring(position, end);
      String currentChunkId = document.getId() + "_chunk_" + index;

      // Calculate quality metrics
      double qualityScore = calculateQualityScore(chunkContent, position, content.length());
      double densityScore = calculateDensityScore(chunkContent);

      // Build enhanced chunk
      DocumentChunk.DocumentChunkBuilder builder = DocumentChunk.builder()
          .chunkId(currentChunkId)
          .documentId(document.getId())
          .content(chunkContent)
          .index(index)
          .startPosition(position)
          .endPosition(end)
          
          // Document metadata
          .documentTitle(document.getTitle())
          .documentType(document.getType())
          .sourcePath(document.getSourcePath())
          .documentTimestamp(document.getIndexedAt())
          
          // Chunking metadata
          .chunkingStrategy(getStrategyName())
          .chunkSizeChars(chunkContent.length())
          .overlapSize(index > 0 ? Math.min(overlapSize, position) : 0)
          .hasOverlapBefore(index > 0)
          .hasOverlapAfter(end < content.length())
          
          // Quality metrics
          .qualityScore(qualityScore)
          .densityScore(densityScore)
          
          // Context linking
          .previousChunkId(previousChunkId);

      DocumentChunk chunk = builder.build();

      // Update previous chunk's next link
      if (index > 0 && !chunks.isEmpty()) {
        DocumentChunk prevChunk = chunks.get(chunks.size() - 1);
        prevChunk.setNextChunkId(currentChunkId);
      }

      chunks.add(chunk);

      // Prepare for next iteration
      previousChunkId = currentChunkId;
      position += (chunkSize - overlapSize);
      index++;
    }

    log.debug("Fixed-size chunked document {} into {} chunks (size={}, overlap={})", 
        document.getId(), chunks.size(), chunkSize, overlapSize);
    
    return chunks;
  }

  @Override
  public int getChunkSize() {
    return chunkSize;
  }

  @Override
  public int getOverlapSize() {
    return overlapSize;
  }

  @Override
  public String getStrategyName() {
    return "FixedSize";
  }

  @Override
  public String getDescription() {
    return String.format(
        "Fixed-size chunking with %d characters per chunk and %d character overlap", 
        chunkSize, overlapSize);
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public void configure(Map<String, Object> parameters) {
    // FixedSizeChunking is immutable, so configuration would require creating new instance
    throw new UnsupportedOperationException(
        "FixedSizeChunking is immutable. Create new instance with desired parameters.");
  }

  @Override
  public double estimateQuality(RAGDocument document) {
    if (document.getContent() == null || document.getContent().isEmpty()) {
      return 0.0;
    }

    String content = document.getContent();
    int length = content.length();

    // Quality factors:
    // 1. Document size vs chunk size ratio
    double sizeRatio = Math.min(1.0, (double) length / chunkSize);
    
    // 2. Text density (non-whitespace ratio)
    double textDensity = calculateDensityScore(content);
    
    // 3. Overlap efficiency (less overlap = higher quality for fixed-size)
    double overlapEfficiency = 1.0 - ((double) overlapSize / chunkSize);
    
    return (sizeRatio * 0.4 + textDensity * 0.4 + overlapEfficiency * 0.2);
  }

  @Override
  public boolean isSuitableFor(RAGDocument document) {
    if (document.getContent() == null) {
      return false;
    }
    
    // Fixed-size chunking works for any document but is less suitable for:
    // - Very short documents (less than 2 chunks)
    // - Highly structured documents where structure matters
    int length = document.getContent().length();
    return length >= chunkSize * 2; // At least 2 chunks
  }

  // === Private Helper Methods ===

  private double calculateQualityScore(String chunkContent, int position, int totalLength) {
    double positionScore = 1.0; // Fixed-size doesn't consider position
    double lengthScore = (double) chunkContent.length() / chunkSize; // Closer to target = better
    double completenessScore = chunkContent.trim().isEmpty() ? 0.0 : 1.0;
    
    return (positionScore * 0.3 + lengthScore * 0.4 + completenessScore * 0.3);
  }

  private double calculateDensityScore(String content) {
    if (content == null || content.isEmpty()) {
      return 0.0;
    }
    
    int totalChars = content.length();
    int nonWhitespaceChars = content.replaceAll("\\s", "").length();
    
    return (double) nonWhitespaceChars / totalChars;
  }
}
