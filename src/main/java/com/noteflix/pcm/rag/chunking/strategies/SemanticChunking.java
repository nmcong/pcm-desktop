package com.noteflix.pcm.rag.chunking.strategies;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.embedding.api.EmbeddingService;
import com.noteflix.pcm.rag.model.RAGDocument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Semantic chunking strategy using embeddings.
 *
 * <p>This advanced strategy groups content based on semantic similarity rather than fixed sizes
 * or sentence boundaries. It uses embeddings to identify topically coherent segments, creating
 * chunks that are semantically meaningful.
 *
 * <p>Ideal for:
 * - Complex documents with varied topics
 * - Academic papers, research documents
 * - Long-form content with topic shifts
 * - Maximum semantic coherence in RAG applications
 *
 * <p>Features:
 * - Semantic similarity-based grouping
 * - Adaptive chunk sizes based on content
 * - Topic boundary detection
 * - High-quality embeddings-based analysis
 * - Configurable similarity thresholds
 *
 * <p>Requirements:
 * - EmbeddingService for generating embeddings
 * - Higher computational cost than other strategies
 * - Best results with quality embedding models
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class SemanticChunking implements ChunkingStrategy {

  private final EmbeddingService embeddingService;
  private final int maxChunkSize;
  private final int minChunkSize;
  private final double similarityThreshold;
  private final int slidingWindowSize;

  /**
   * Create semantic chunking strategy.
   *
   * @param embeddingService Service for generating embeddings
   * @param maxChunkSize Maximum chunk size in characters
   * @param minChunkSize Minimum chunk size in characters  
   * @param similarityThreshold Cosine similarity threshold for grouping (0.0-1.0)
   * @param slidingWindowSize Size of sliding window for semantic analysis
   */
  public SemanticChunking(EmbeddingService embeddingService, int maxChunkSize, int minChunkSize,
                         double similarityThreshold, int slidingWindowSize) {
    if (embeddingService == null) {
      throw new IllegalArgumentException("EmbeddingService cannot be null");
    }
    validateConfig(maxChunkSize, 0);
    if (minChunkSize <= 0 || minChunkSize >= maxChunkSize) {
      throw new IllegalArgumentException("Invalid min/max chunk sizes");
    }
    if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
      throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
    }
    if (slidingWindowSize <= 0) {
      throw new IllegalArgumentException("Sliding window size must be positive");
    }

    this.embeddingService = embeddingService;
    this.maxChunkSize = maxChunkSize;
    this.minChunkSize = minChunkSize;
    this.similarityThreshold = similarityThreshold;
    this.slidingWindowSize = slidingWindowSize;
  }

  /** Create default semantic chunking (requires embedding service). */
  public static SemanticChunking defaults(EmbeddingService embeddingService) {
    return new SemanticChunking(embeddingService, 2000, 200, 0.75, 3);
  }

  /** Create high-precision semantic chunking with stricter similarity. */
  public static SemanticChunking precise(EmbeddingService embeddingService) {
    return new SemanticChunking(embeddingService, 1500, 300, 0.85, 2);
  }

  /** Create flexible semantic chunking with looser similarity. */
  public static SemanticChunking flexible(EmbeddingService embeddingService) {
    return new SemanticChunking(embeddingService, 3000, 150, 0.65, 4);
  }

  @Override
  public List<DocumentChunk> chunk(RAGDocument document) {
    List<DocumentChunk> chunks = new ArrayList<>();
    String content = document.getContent();

    if (content == null || content.isEmpty()) {
      log.warn("Document {} has empty content", document.getId());
      return chunks;
    }

    try {
      // Step 1: Create initial segments using sentences as base units
      List<TextSegment> segments = createInitialSegments(content);
      if (segments.isEmpty()) {
        log.warn("No segments created for document {}", document.getId());
        return createFallbackChunks(document, content);
      }

      // Step 2: Generate embeddings for segments
      List<float[]> embeddings = generateSegmentEmbeddings(segments);
      if (embeddings.size() != segments.size()) {
        log.error("Embedding count mismatch for document {}", document.getId());
        return createFallbackChunks(document, content);
      }

      // Step 3: Group segments based on semantic similarity
      List<SemanticGroup> groups = groupSegmentsBySimilarity(segments, embeddings);

      // Step 4: Convert groups to chunks
      chunks = convertGroupsToChunks(document, groups, content);

      log.debug("Semantic chunked document {} into {} chunks from {} segments", 
          document.getId(), chunks.size(), segments.size());

    } catch (Exception e) {
      log.error("Error in semantic chunking for document {}: {}", 
          document.getId(), e.getMessage(), e);
      return createFallbackChunks(document, content);
    }

    return chunks;
  }

  @Override
  public int getChunkSize() {
    return (maxChunkSize + minChunkSize) / 2; // Average target size
  }

  @Override
  public int getOverlapSize() {
    return 0; // Semantic chunking doesn't use traditional overlap
  }

  @Override
  public String getStrategyName() {
    return "Semantic";
  }

  @Override
  public String getDescription() {
    return String.format(
        "Semantic chunking with embeddings (threshold=%.2f, size=%d-%d chars)",
        similarityThreshold, minChunkSize, maxChunkSize);
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public void configure(Map<String, Object> parameters) {
    // SemanticChunking is immutable due to embedding service dependency
    throw new UnsupportedOperationException(
        "SemanticChunking configuration requires creating new instance with EmbeddingService");
  }

  @Override
  public double estimateQuality(RAGDocument document) {
    if (document.getContent() == null || document.getContent().isEmpty()) {
      return 0.0;
    }

    String content = document.getContent();
    int length = content.length();

    // Quality factors for semantic chunking:
    // 1. Document length (semantic chunking works better on longer documents)
    double lengthScore = Math.min(1.0, (double) length / 5000); // Optimal at 5000+ chars
    
    // 2. Sentence density (more sentences = better semantic analysis)
    int sentenceCount = content.split("[.!?]+").length;
    double sentenceDensity = Math.min(1.0, (double) sentenceCount / (length / 100)); // per 100 chars
    
    // 3. Vocabulary richness (more unique words = better semantic analysis)
    String[] words = content.toLowerCase().split("\\W+");
    long uniqueWords = Arrays.stream(words).distinct().count();
    double vocabularyRichness = Math.min(1.0, (double) uniqueWords / words.length);
    
    // 4. Embedding service availability
    double serviceScore = embeddingService != null ? 1.0 : 0.0;

    return (lengthScore * 0.3 + sentenceDensity * 0.2 + vocabularyRichness * 0.2 + serviceScore * 0.3);
  }

  @Override
  public boolean isSuitableFor(RAGDocument document) {
    if (document.getContent() == null || embeddingService == null) {
      return false;
    }

    String content = document.getContent();
    
    // Suitable if:
    // - Document is reasonably long (semantic analysis needs content)
    // - Has multiple sentences/paragraphs
    // - Not too structured (better for prose than code/data)
    return content.length() >= 1000 && 
           content.split("[.!?]+").length >= 10 &&
           !isHighlyStructured(content);
  }

  // === Private Helper Methods ===

  private List<TextSegment> createInitialSegments(String content) {
    List<TextSegment> segments = new ArrayList<>();
    
    // Use sentence boundaries as initial segmentation
    String[] sentences = content.split("(?<=[.!?])\\s+");
    int position = 0;
    
    for (String sentence : sentences) {
      sentence = sentence.trim();
      if (!sentence.isEmpty()) {
        int start = content.indexOf(sentence, position);
        int end = start + sentence.length();
        segments.add(new TextSegment(start, end, sentence));
        position = end;
      }
    }
    
    return segments;
  }

  private List<float[]> generateSegmentEmbeddings(List<TextSegment> segments) {
    List<float[]> embeddings = new ArrayList<>();
    
    // Prepare texts for batch embedding
    String[] texts = segments.stream()
        .map(s -> s.text)
        .toArray(String[]::new);
    
    try {
      // Use batch embedding for efficiency
      float[][] batchEmbeddings = embeddingService.embedBatch(texts);
      for (float[] embedding : batchEmbeddings) {
        embeddings.add(embedding);
      }
    } catch (Exception e) {
      log.warn("Batch embedding failed, falling back to individual embeddings: {}", e.getMessage());
      
      // Fallback to individual embeddings
      for (TextSegment segment : segments) {
        try {
          float[] embedding = embeddingService.embed(segment.text);
          embeddings.add(embedding);
        } catch (Exception ex) {
          log.error("Failed to generate embedding for segment: {}", ex.getMessage());
          // Use zero vector as placeholder
          embeddings.add(new float[embeddingService.getDimension()]);
        }
      }
    }
    
    return embeddings;
  }

  private List<SemanticGroup> groupSegmentsBySimilarity(List<TextSegment> segments, 
                                                      List<float[]> embeddings) {
    List<SemanticGroup> groups = new ArrayList<>();
    
    if (segments.isEmpty()) {
      return groups;
    }
    
    // Initialize with first segment
    SemanticGroup currentGroup = new SemanticGroup();
    currentGroup.addSegment(segments.get(0), embeddings.get(0));
    
    // Process remaining segments
    for (int i = 1; i < segments.size(); i++) {
      TextSegment segment = segments.get(i);
      float[] embedding = embeddings.get(i);
      
      // Calculate similarity with current group
      double similarity = calculateGroupSimilarity(embedding, currentGroup);
      
      // Decide whether to add to current group or start new one
      if (similarity >= similarityThreshold && 
          currentGroup.getTotalLength() + segment.text.length() <= maxChunkSize) {
        // Add to current group
        currentGroup.addSegment(segment, embedding);
      } else {
        // Finalize current group and start new one
        if (currentGroup.getTotalLength() >= minChunkSize || groups.isEmpty()) {
          groups.add(currentGroup);
        } else {
          // Merge with previous group if too small
          if (!groups.isEmpty()) {
            groups.get(groups.size() - 1).mergeWith(currentGroup);
          } else {
            groups.add(currentGroup); // Keep even if small for first group
          }
        }
        
        currentGroup = new SemanticGroup();
        currentGroup.addSegment(segment, embedding);
      }
    }
    
    // Add final group
    if (!currentGroup.segments.isEmpty()) {
      if (currentGroup.getTotalLength() >= minChunkSize || groups.isEmpty()) {
        groups.add(currentGroup);
      } else {
        // Merge with previous group if too small
        if (!groups.isEmpty()) {
          groups.get(groups.size() - 1).mergeWith(currentGroup);
        } else {
          groups.add(currentGroup);
        }
      }
    }
    
    return groups;
  }

  private double calculateGroupSimilarity(float[] embedding, SemanticGroup group) {
    if (group.segments.isEmpty()) {
      return 0.0;
    }
    
    // Calculate average similarity with recent segments in group
    List<float[]> recentEmbeddings = group.getRecentEmbeddings(slidingWindowSize);
    double totalSimilarity = 0.0;
    
    for (float[] groupEmbedding : recentEmbeddings) {
      totalSimilarity += cosineSimilarity(embedding, groupEmbedding);
    }
    
    return totalSimilarity / recentEmbeddings.size();
  }

  private double cosineSimilarity(float[] a, float[] b) {
    if (a.length != b.length) {
      return 0.0;
    }
    
    double dotProduct = 0.0;
    double normA = 0.0;
    double normB = 0.0;
    
    for (int i = 0; i < a.length; i++) {
      dotProduct += a[i] * b[i];
      normA += a[i] * a[i];
      normB += b[i] * b[i];
    }
    
    if (normA == 0.0 || normB == 0.0) {
      return 0.0;
    }
    
    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
  }

  private List<DocumentChunk> convertGroupsToChunks(RAGDocument document, 
                                                  List<SemanticGroup> groups, 
                                                  String content) {
    List<DocumentChunk> chunks = new ArrayList<>();
    String previousChunkId = null;
    
    for (int i = 0; i < groups.size(); i++) {
      SemanticGroup group = groups.get(i);
      String currentChunkId = document.getId() + "_chunk_" + i;
      
      // Build chunk content from segments
      StringBuilder chunkContent = new StringBuilder();
      int startPos = group.segments.get(0).start;
      int endPos = group.segments.get(group.segments.size() - 1).end;
      
      for (TextSegment segment : group.segments) {
        if (chunkContent.length() > 0) {
          chunkContent.append(" ");
        }
        chunkContent.append(segment.text);
      }
      
      // Calculate quality metrics
      double qualityScore = calculateSemanticQuality(group);
      double coherenceScore = group.calculateCoherence();
      double densityScore = calculateDensityScore(chunkContent.toString());
      
      // Build enhanced chunk
      DocumentChunk.DocumentChunkBuilder builder = DocumentChunk.builder()
          .chunkId(currentChunkId)
          .documentId(document.getId())
          .content(chunkContent.toString().trim())
          .index(i)
          .startPosition(startPos)
          .endPosition(endPos)
          
          // Document metadata
          .documentTitle(document.getTitle())
          .documentType(document.getType())
          .sourcePath(document.getSourcePath())
          .documentTimestamp(document.getIndexedAt())
          
          // Chunking metadata
          .chunkingStrategy(getStrategyName())
          .chunkSizeChars(chunkContent.length())
          .overlapSize(0) // Semantic chunking doesn't use traditional overlap
          .hasOverlapBefore(false)
          .hasOverlapAfter(false)
          
          // Quality metrics
          .qualityScore(qualityScore)
          .coherenceScore(coherenceScore)
          .densityScore(densityScore)
          
          // Context linking
          .previousChunkId(previousChunkId);

      DocumentChunk chunk = builder.build();
      
      // Add semantic-specific metadata
      chunk.addMetadata("segment_count", String.valueOf(group.segments.size()));
      chunk.addMetadata("avg_similarity", String.format("%.3f", group.getAverageSimilarity()));
      chunk.addMetadata("semantic_coherence", String.format("%.3f", coherenceScore));
      chunk.addMetadata("chunk_type", "semantic");
      
      // Update previous chunk's next link
      if (i > 0 && !chunks.isEmpty()) {
        DocumentChunk prevChunk = chunks.get(chunks.size() - 1);
        prevChunk.setNextChunkId(currentChunkId);
      }
      
      chunks.add(chunk);
      previousChunkId = currentChunkId;
    }
    
    return chunks;
  }

  private double calculateSemanticQuality(SemanticGroup group) {
    // Quality based on semantic coherence and size appropriateness
    double coherenceScore = group.calculateCoherence();
    double sizeScore = calculateSizeScore(group.getTotalLength());
    double segmentScore = Math.min(1.0, (double) group.segments.size() / 5); // Prefer 3-5 segments
    
    return (coherenceScore * 0.5 + sizeScore * 0.3 + segmentScore * 0.2);
  }

  private double calculateSizeScore(int length) {
    int targetSize = (maxChunkSize + minChunkSize) / 2;
    double deviation = Math.abs(length - targetSize) / (double) targetSize;
    return Math.max(0.0, 1.0 - deviation);
  }

  private double calculateDensityScore(String content) {
    if (content == null || content.isEmpty()) {
      return 0.0;
    }
    
    int totalChars = content.length();
    int nonWhitespaceChars = content.replaceAll("\\s", "").length();
    
    return (double) nonWhitespaceChars / totalChars;
  }

  private boolean isHighlyStructured(String content) {
    // Simple heuristic: if content has many special characters or formatting,
    // it might be code, data, or structured text (less suitable for semantic chunking)
    int specialChars = content.replaceAll("[a-zA-Z0-9\\s.,!?]", "").length();
    return (double) specialChars / content.length() > 0.2;
  }

  private List<DocumentChunk> createFallbackChunks(RAGDocument document, String content) {
    // Fallback to sentence-aware chunking if semantic analysis fails
    log.warn("Falling back to sentence-aware chunking for document {}", document.getId());
    SentenceAwareChunking fallback = SentenceAwareChunking.defaults();
    return fallback.chunk(document);
  }

  // === Inner Classes ===

  private static class TextSegment {
    final int start;
    final int end;
    final String text;

    TextSegment(int start, int end, String text) {
      this.start = start;
      this.end = end;
      this.text = text;
    }
  }

  private static class SemanticGroup {
    final List<TextSegment> segments = new ArrayList<>();
    final List<float[]> embeddings = new ArrayList<>();

    void addSegment(TextSegment segment, float[] embedding) {
      segments.add(segment);
      embeddings.add(embedding);
    }

    void mergeWith(SemanticGroup other) {
      segments.addAll(other.segments);
      embeddings.addAll(other.embeddings);
    }

    int getTotalLength() {
      return segments.stream().mapToInt(s -> s.text.length()).sum();
    }

    List<float[]> getRecentEmbeddings(int windowSize) {
      int start = Math.max(0, embeddings.size() - windowSize);
      return embeddings.subList(start, embeddings.size());
    }

    double calculateCoherence() {
      if (embeddings.size() < 2) {
        return 1.0;
      }

      double totalSimilarity = 0.0;
      int pairCount = 0;

      for (int i = 0; i < embeddings.size(); i++) {
        for (int j = i + 1; j < embeddings.size(); j++) {
          totalSimilarity += cosineSimilarity(embeddings.get(i), embeddings.get(j));
          pairCount++;
        }
      }

      return pairCount > 0 ? totalSimilarity / pairCount : 0.0;
    }

    double getAverageSimilarity() {
      return calculateCoherence();
    }

    private double cosineSimilarity(float[] a, float[] b) {
      if (a.length != b.length) {
        return 0.0;
      }

      double dotProduct = 0.0;
      double normA = 0.0;
      double normB = 0.0;

      for (int i = 0; i < a.length; i++) {
        dotProduct += a[i] * b[i];
        normA += a[i] * a[i];
        normB += b[i] * b[i];
      }

      if (normA == 0.0 || normB == 0.0) {
        return 0.0;
      }

      return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
  }
}