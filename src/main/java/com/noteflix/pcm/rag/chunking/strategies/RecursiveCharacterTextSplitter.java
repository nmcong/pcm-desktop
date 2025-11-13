package com.noteflix.pcm.rag.chunking.strategies;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.model.RAGDocument;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Recursive Character Text Splitter - Structure-aware chunking strategy.
 *
 * <p>This strategy recursively splits text by trying different separators in order of priority,
 * from most structural (paragraphs, sections) to least structural (characters). This approach
 * preserves document structure and creates more semantically coherent chunks.
 *
 * <p><b>Separator Hierarchy (in order):</b>
 * 1. Double newlines (paragraphs)
 * 2. Single newlines
 * 3. Spaces
 * 4. Characters (fallback)
 *
 * <p><b>Ideal for:</b>
 * - General text documents
 * - Mixed content (code + documentation)
 * - Long-form articles and essays
 * - Technical documentation
 * - Books and research papers
 *
 * <p><b>Features:</b>
 * - Preserves natural document structure
 * - Tries to keep related content together
 * - Configurable separators with priority order
 * - Handles edge cases gracefully
 * - Smart overlap at natural boundaries
 * - Quality scoring based on structure preservation
 *
 * <p><b>Algorithm:</b>
 * 1. Try to split by highest-priority separator (e.g., \n\n)
 * 2. If chunks are too large, recursively split with next separator
 * 3. Continue until chunks are within target size
 * 4. Apply overlap at natural boundaries
 * 5. Merge very small chunks when beneficial
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter(
 *     config,
 *     Arrays.asList("\n\n", "\n", " ", "")
 * );
 * List<DocumentChunk> chunks = splitter.chunk(document);
 * }</pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class RecursiveCharacterTextSplitter implements ChunkingStrategy {

  private final int chunkSize;
  private final int chunkOverlap;
  private final List<String> separators;
  private final boolean keepSeparator;

  /**
   * Default separators in priority order.
   * Tries to split at most natural boundaries first.
   */
  private static final List<String> DEFAULT_SEPARATORS = Arrays.asList(
      "\n\n",  // Paragraphs
      "\n",    // Lines
      " ",     // Words
      ""       // Characters (fallback)
  );

  /**
   * Create recursive splitter with configuration.
   *
   * @param config Chunking configuration
   */
  public RecursiveCharacterTextSplitter(ChunkingConfig config) {
    this(config, DEFAULT_SEPARATORS, true);
  }

  /**
   * Create recursive splitter with custom separators.
   *
   * @param config Chunking configuration
   * @param separators List of separators in priority order
   */
  public RecursiveCharacterTextSplitter(ChunkingConfig config, List<String> separators) {
    this(config, separators, true);
  }

  /**
   * Create recursive splitter with full customization.
   *
   * @param config Chunking configuration
   * @param separators List of separators in priority order
   * @param keepSeparator Whether to keep separators in chunks
   */
  public RecursiveCharacterTextSplitter(
      ChunkingConfig config, 
      List<String> separators,
      boolean keepSeparator) {
    
    this.chunkSize = config.getTargetChunkSize();
    this.chunkOverlap = config.getOverlapSize();
    this.separators = separators != null && !separators.isEmpty() ? separators : DEFAULT_SEPARATORS;
    this.keepSeparator = keepSeparator;

    log.debug("Initialized RecursiveCharacterTextSplitter: size={}, overlap={}, separators={}",
        chunkSize, chunkOverlap, this.separators.size());
  }

  @Override
  public List<DocumentChunk> chunk(RAGDocument document) {
    if (document == null || document.getContent() == null) {
      log.warn("Cannot chunk null document or content");
      return List.of();
    }

    String content = document.getContent();
    if (content.isEmpty()) {
      log.debug("Empty document content, returning empty list");
      return List.of();
    }

    log.debug("Chunking document: id={}, contentLength={}", 
        document.getId(), content.length());

    // Recursively split the text
    List<String> textChunks = splitTextRecursive(content, separators);

    // Merge small chunks and apply overlap
    textChunks = mergeTooSmall(textChunks, chunkSize, chunkOverlap);

    // Create DocumentChunk objects with metadata
    List<DocumentChunk> chunks = new ArrayList<>();
    int position = 0;
    
    for (int i = 0; i < textChunks.size(); i++) {
      String chunkText = textChunks.get(i);
      int startPos = content.indexOf(chunkText, position);
      int endPos = startPos + chunkText.length();

      DocumentChunk chunk = DocumentChunk.builder()
          .chunkId(document.getId() + "_chunk_" + i)
          .documentId(document.getId())
          .content(chunkText)
          .index(i)
          .startPosition(startPos)
          .endPosition(endPos)
          .documentTitle(document.getTitle())
          .documentType(document.getType())
          .sourcePath(document.getSourcePath())
          .documentTimestamp(document.getIndexedAt())
          .chunkingStrategy("recursive_character")
          .chunkSizeChars(chunkText.length())
          .estimatedTokens(estimateTokenCount(chunkText))
          .hasOverlapBefore(i > 0 && chunkOverlap > 0)
          .hasOverlapAfter(i < textChunks.size() - 1 && chunkOverlap > 0)
          .overlapSize(i > 0 ? chunkOverlap : 0)
          .qualityScore(calculateQualityScore(chunkText))
          .coherenceScore(calculateCoherenceScore(chunkText))
          .densityScore(calculateDensityScore(chunkText))
          .previousChunkId(i > 0 ? document.getId() + "_chunk_" + (i - 1) : null)
          .nextChunkId(i < textChunks.size() - 1 ? document.getId() + "_chunk_" + (i + 1) : null)
          .build();

      chunks.add(chunk);
      position = endPos;
    }

    log.info("Created {} chunks using recursive character splitting (avg size: {} chars)",
        chunks.size(),
        chunks.stream().mapToInt(DocumentChunk::getChunkSizeChars).average().orElse(0));

    return chunks;
  }

  /**
   * Recursively split text using separators in priority order.
   *
   * @param text Text to split
   * @param separators List of separators to try
   * @return List of text chunks
   */
  private List<String> splitTextRecursive(String text, List<String> separators) {
    List<String> finalChunks = new ArrayList<>();

    // Base case: use character splitting if we're at the last separator
    String separator = !separators.isEmpty() ? separators.get(0) : "";
    List<String> newSeparators = separators.size() > 1 
        ? separators.subList(1, separators.size()) 
        : new ArrayList<>();

    // Split by current separator
    List<String> splits;
    if (separator.isEmpty()) {
      // Character-level split
      splits = splitByCharacter(text);
    } else {
      splits = splitText(text, separator);
    }

    // Process each split
    List<String> goodSplits = new ArrayList<>();
    for (String split : splits) {
      if (split.length() < chunkSize) {
        goodSplits.add(split);
      } else {
        // This split is still too large, need to go deeper
        if (!goodSplits.isEmpty()) {
          // Merge accumulated good splits first
          finalChunks.addAll(mergeTooSmall(goodSplits, chunkSize, chunkOverlap));
          goodSplits.clear();
        }

        // Recursively split this chunk with next separator
        if (!newSeparators.isEmpty()) {
          List<String> subChunks = splitTextRecursive(split, newSeparators);
          finalChunks.addAll(subChunks);
        } else {
          // No more separators, force split by character
          finalChunks.addAll(splitByCharacter(split));
        }
      }
    }

    // Add remaining good splits
    if (!goodSplits.isEmpty()) {
      finalChunks.addAll(mergeTooSmall(goodSplits, chunkSize, chunkOverlap));
    }

    return finalChunks;
  }

  /**
   * Split text by separator.
   *
   * @param text Text to split
   * @param separator Separator to use
   * @return List of splits
   */
  private List<String> splitText(String text, String separator) {
    List<String> splits = new ArrayList<>();
    
    if (separator.isEmpty()) {
      return splitByCharacter(text);
    }

    String[] parts = text.split(Pattern.quote(separator), -1);
    
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      
      if (keepSeparator && i < parts.length - 1) {
        // Add separator back to the chunk
        part = part + separator;
      }
      
      if (!part.isEmpty()) {
        splits.add(part);
      }
    }

    return splits;
  }

  /**
   * Split text by characters when all other separators fail.
   *
   * @param text Text to split
   * @return List of character-level chunks
   */
  private List<String> splitByCharacter(String text) {
    List<String> chunks = new ArrayList<>();
    
    for (int i = 0; i < text.length(); i += chunkSize) {
      int end = Math.min(i + chunkSize, text.length());
      chunks.add(text.substring(i, end));
    }
    
    return chunks;
  }

  /**
   * Merge chunks that are too small and apply overlap.
   *
   * @param chunks List of chunks to merge
   * @param chunkSize Target chunk size
   * @param chunkOverlap Overlap size
   * @return Merged chunks with overlap
   */
  private List<String> mergeTooSmall(List<String> chunks, int chunkSize, int chunkOverlap) {
    if (chunks.isEmpty()) {
      return chunks;
    }

    List<String> merged = new ArrayList<>();
    StringBuilder currentChunk = new StringBuilder();

    for (String chunk : chunks) {
      if (currentChunk.length() == 0) {
        currentChunk.append(chunk);
      } else if (currentChunk.length() + chunk.length() <= chunkSize) {
        // Merge with current chunk
        currentChunk.append(chunk);
      } else {
        // Current chunk is complete
        merged.add(currentChunk.toString());
        
        // Start new chunk with overlap
        if (chunkOverlap > 0) {
          String overlap = getOverlap(currentChunk.toString(), chunkOverlap);
          currentChunk = new StringBuilder(overlap);
        } else {
          currentChunk = new StringBuilder();
        }
        
        currentChunk.append(chunk);
      }
    }

    // Add the last chunk
    if (currentChunk.length() > 0) {
      merged.add(currentChunk.toString());
    }

    return merged;
  }

  /**
   * Get overlap text from the end of a chunk.
   *
   * @param text Text to extract overlap from
   * @param overlapSize Size of overlap
   * @return Overlap text
   */
  private String getOverlap(String text, int overlapSize) {
    if (text.length() <= overlapSize) {
      return text;
    }
    return text.substring(text.length() - overlapSize);
  }

  /**
   * Calculate quality score based on structure preservation.
   *
   * @param text Chunk text
   * @return Quality score (0.0 - 1.0)
   */
  private double calculateQualityScore(String text) {
    if (text == null || text.isEmpty()) {
      return 0.0;
    }

    double score = 0.5; // Base score

    // Bonus for starting/ending at paragraph boundaries
    if (text.startsWith("\n\n") || text.endsWith("\n\n")) {
      score += 0.2;
    }

    // Bonus for complete sentences
    if (text.trim().matches(".*[.!?]$")) {
      score += 0.15;
    }

    // Penalty for very short chunks
    if (text.length() < chunkSize * 0.3) {
      score -= 0.2;
    }

    // Bonus for good length
    if (text.length() >= chunkSize * 0.7 && text.length() <= chunkSize * 1.1) {
      score += 0.15;
    }

    return Math.max(0.0, Math.min(1.0, score));
  }

  /**
   * Calculate coherence score based on text structure.
   *
   * @param text Chunk text
   * @return Coherence score (0.0 - 1.0)
   */
  private double calculateCoherenceScore(String text) {
    if (text == null || text.isEmpty()) {
      return 0.0;
    }

    int sentences = text.split("[.!?]+").length;
    int paragraphs = text.split("\n\n+").length;

    // Good balance of sentences and paragraphs indicates coherence
    double ratio = paragraphs > 0 ? (double) sentences / paragraphs : sentences;
    
    if (ratio >= 2 && ratio <= 5) {
      return 0.9;
    } else if (ratio >= 1 && ratio <= 8) {
      return 0.7;
    } else {
      return 0.5;
    }
  }

  /**
   * Calculate information density score.
   *
   * @param text Chunk text
   * @return Density score (0.0 - 1.0)
   */
  private double calculateDensityScore(String text) {
    if (text == null || text.isEmpty()) {
      return 0.0;
    }

    String trimmed = text.trim();
    double whitespaceRatio = 1.0 - ((double) trimmed.replace(" ", "").length() / text.length());
    
    // Good density: 10-30% whitespace
    if (whitespaceRatio >= 0.1 && whitespaceRatio <= 0.3) {
      return 0.9;
    } else if (whitespaceRatio >= 0.05 && whitespaceRatio <= 0.4) {
      return 0.7;
    } else {
      return 0.5;
    }
  }

  /**
   * Estimate token count (rough approximation: 1 token ≈ 4 characters).
   *
   * @param text Text to estimate
   * @return Estimated token count
   */
  private int estimateTokenCount(String text) {
    return (int) Math.ceil(text.length() / 4.0);
  }

  @Override
  public String getStrategyName() {
    return "recursive_character";
  }

  @Override
  public String getDescription() {
    return "Recursive character text splitter with structure-aware splitting. "
        + "Tries separators in order: paragraphs → lines → words → characters. "
        + "Preserves document structure while maintaining target chunk size.";
  }

  @Override
  public int getChunkSize() {
    return chunkSize;
  }

  @Override
  public int getOverlapSize() {
    return chunkOverlap;
  }
}

