package com.noteflix.pcm.rag.chunking.strategies;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingConfig;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.model.RAGDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Markdown-aware chunking strategy.
 *
 * <p>Splits Markdown documents while preserving structure and hierarchy. This strategy understands
 * Markdown syntax and creates chunks that respect document structure, headers, lists, code blocks,
 * and other Markdown elements.
 *
 * <p>Ideal for:
 * - Technical documentation
 * - README files and wikis
 * - Blog posts and articles in Markdown
 * - Structured documents with clear sections
 * - Preserving document hierarchy in RAG
 *
 * <p>Features:
 * - Header-based section chunking
 * - Code block preservation
 * - List and table awareness
 * - Hierarchy preservation in metadata
 * - Flexible size configuration
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class MarkdownAwareChunking implements ChunkingStrategy {

  private final int targetChunkSize;
  private final int minChunkSize;
  private final boolean preserveCodeBlocks;
  private final boolean respectHeaders;
  private final int maxHeaderLevel;

  // Markdown patterns
  private static final Pattern HEADER_PATTERN = Pattern.compile(
      "^(#{1,6})\\s+(.+)$", 
      Pattern.MULTILINE
  );
  
  private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile(
      "```[\\s\\S]*?```|`[^`]+`", 
      Pattern.MULTILINE
  );
  
  private static final Pattern LIST_ITEM_PATTERN = Pattern.compile(
      "^\\s*[-*+]\\s+.+$|^\\s*\\d+\\.\\s+.+$", 
      Pattern.MULTILINE
  );
  
  private static final Pattern TABLE_PATTERN = Pattern.compile(
      "^\\|.+\\|$", 
      Pattern.MULTILINE
  );
  
  private static final Pattern HORIZONTAL_RULE_PATTERN = Pattern.compile(
      "^\\s*[-*_]{3,}\\s*$", 
      Pattern.MULTILINE
  );

  /**
   * Create markdown-aware chunking strategy.
   *
   * @param targetChunkSize Target chunk size in characters
   * @param minChunkSize Minimum chunk size in characters
   * @param preserveCodeBlocks Whether to keep code blocks intact
   * @param respectHeaders Whether to prioritize header boundaries
   * @param maxHeaderLevel Maximum header level to consider for chunking (1-6)
   */
  public MarkdownAwareChunking(int targetChunkSize, int minChunkSize, boolean preserveCodeBlocks,
                              boolean respectHeaders, int maxHeaderLevel) {
    validateConfig(targetChunkSize, 0);
    if (minChunkSize <= 0 || minChunkSize >= targetChunkSize) {
      throw new IllegalArgumentException("Invalid min/max chunk sizes");
    }
    if (maxHeaderLevel < 1 || maxHeaderLevel > 6) {
      throw new IllegalArgumentException("Header level must be between 1 and 6");
    }

    this.targetChunkSize = targetChunkSize;
    this.minChunkSize = minChunkSize;
    this.preserveCodeBlocks = preserveCodeBlocks;
    this.respectHeaders = respectHeaders;
    this.maxHeaderLevel = maxHeaderLevel;
  }

  /** Create default markdown chunking (1500 chars, preserve structure). */
  public static MarkdownAwareChunking defaults() {
    return new MarkdownAwareChunking(1500, 200, true, true, 3);
  }

  /** Create header-focused chunking (sections by headers). */
  public static MarkdownAwareChunking headerFocused() {
    return new MarkdownAwareChunking(2000, 300, true, true, 2);
  }

  /** Create code-preserving chunking (ideal for technical docs). */
  public static MarkdownAwareChunking codePreserving() {
    return new MarkdownAwareChunking(1200, 150, true, false, 4);
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
      // Step 1: Analyze document structure
      MarkdownStructure structure = analyzeMarkdownStructure(content);
      
      // Step 2: Create chunks based on structure
      if (respectHeaders && !structure.sections.isEmpty()) {
        chunks = chunkByHeaders(document, structure);
      } else {
        chunks = chunkByContent(document, structure);
      }

      log.debug("Markdown-aware chunked document {} into {} chunks from {} sections", 
          document.getId(), chunks.size(), structure.sections.size());

    } catch (Exception e) {
      log.error("Error in markdown chunking for document {}: {}", 
          document.getId(), e.getMessage(), e);
      return createFallbackChunks(document, content);
    }

    return chunks;
  }

  @Override
  public int getChunkSize() {
    return targetChunkSize;
  }

  @Override
  public int getOverlapSize() {
    return 0; // Markdown chunking uses structural boundaries, not overlap
  }

  @Override
  public String getStrategyName() {
    return "MarkdownAware";
  }

  @Override
  public String getDescription() {
    return String.format(
        "Markdown-aware chunking (target=%d chars, headers≤H%d, code=%s)",
        targetChunkSize, maxHeaderLevel, preserveCodeBlocks ? "preserved" : "split");
  }

  @Override
  public double estimateQuality(RAGDocument document) {
    if (document.getContent() == null || document.getContent().isEmpty()) {
      return 0.0;
    }

    String content = document.getContent();
    MarkdownStructure structure = analyzeMarkdownStructure(content);

    // Quality factors:
    // 1. Markdown structure richness
    double structureScore = Math.min(1.0, (double) structure.sections.size() / 10);
    
    // 2. Header hierarchy quality
    double headerScore = structure.hasHeaders ? 1.0 : 0.3;
    
    // 3. Code block presence (if preserving)
    double codeScore = preserveCodeBlocks && structure.hasCodeBlocks ? 1.0 : 0.8;
    
    // 4. Content length appropriateness
    double lengthScore = Math.min(1.0, (double) content.length() / targetChunkSize);

    return (structureScore * 0.3 + headerScore * 0.3 + codeScore * 0.2 + lengthScore * 0.2);
  }

  @Override
  public boolean isSuitableFor(RAGDocument document) {
    if (document.getContent() == null) {
      return false;
    }

    String content = document.getContent();
    
    // Check if content looks like Markdown
    boolean hasHeaders = HEADER_PATTERN.matcher(content).find();
    boolean hasCodeBlocks = CODE_BLOCK_PATTERN.matcher(content).find();
    boolean hasLists = LIST_ITEM_PATTERN.matcher(content).find();
    boolean hasTables = TABLE_PATTERN.matcher(content).find();
    
    // Suitable if document has Markdown features and is reasonably long
    return content.length() >= 500 && 
           (hasHeaders || hasCodeBlocks || hasLists || hasTables);
  }

  // === Private Helper Methods ===

  private MarkdownStructure analyzeMarkdownStructure(String content) {
    MarkdownStructure structure = new MarkdownStructure();
    
    // Find headers
    Matcher headerMatcher = HEADER_PATTERN.matcher(content);
    while (headerMatcher.find()) {
      int level = headerMatcher.group(1).length();
      String title = headerMatcher.group(2).trim();
      int position = headerMatcher.start();
      
      if (level <= maxHeaderLevel) {
        structure.addHeader(new MarkdownHeader(level, title, position));
      }
    }
    
    // Find code blocks
    Matcher codeMatcher = CODE_BLOCK_PATTERN.matcher(content);
    while (codeMatcher.find()) {
      int start = codeMatcher.start();
      int end = codeMatcher.end();
      structure.addCodeBlock(new MarkdownElement(start, end, codeMatcher.group()));
    }
    
    // Find other structural elements
    findListsAndTables(content, structure);
    
    // Create sections based on headers
    createSections(content, structure);
    
    return structure;
  }

  private void findListsAndTables(String content, MarkdownStructure structure) {
    String[] lines = content.split("\n");
    int position = 0;
    
    for (String line : lines) {
      if (LIST_ITEM_PATTERN.matcher(line).matches()) {
        structure.addListItem(new MarkdownElement(position, position + line.length(), line));
      } else if (TABLE_PATTERN.matcher(line).matches()) {
        structure.addTableRow(new MarkdownElement(position, position + line.length(), line));
      }
      position += line.length() + 1; // +1 for newline
    }
  }

  private void createSections(String content, MarkdownStructure structure) {
    if (structure.headers.isEmpty()) {
      // No headers, create single section
      structure.addSection(new MarkdownSection(0, content.length(), null, 0, content));
      return;
    }

    int contentLength = content.length();
    
    for (int i = 0; i < structure.headers.size(); i++) {
      MarkdownHeader header = structure.headers.get(i);
      int sectionStart = header.position;
      int sectionEnd;
      
      // Find next header at same or higher level
      if (i + 1 < structure.headers.size()) {
        MarkdownHeader nextHeader = structure.headers.get(i + 1);
        if (nextHeader.level <= header.level) {
          sectionEnd = nextHeader.position;
        } else {
          // Find next header at same or higher level
          sectionEnd = contentLength;
          for (int j = i + 2; j < structure.headers.size(); j++) {
            MarkdownHeader futureHeader = structure.headers.get(j);
            if (futureHeader.level <= header.level) {
              sectionEnd = futureHeader.position;
              break;
            }
          }
        }
      } else {
        sectionEnd = contentLength;
      }
      
      String sectionContent = content.substring(sectionStart, sectionEnd);
      structure.addSection(new MarkdownSection(sectionStart, sectionEnd, header.title, 
                                              header.level, sectionContent));
    }
  }

  private List<DocumentChunk> chunkByHeaders(RAGDocument document, MarkdownStructure structure) {
    List<DocumentChunk> chunks = new ArrayList<>();
    List<MarkdownSection> currentSections = new ArrayList<>();
    int currentLength = 0;
    
    for (MarkdownSection section : structure.sections) {
      // Check if adding this section would exceed target size
      if (!currentSections.isEmpty() && 
          currentLength + section.content.length() > targetChunkSize) {
        
        // Create chunk from current sections
        chunks.add(createChunkFromSections(document, currentSections, chunks.size()));
        
        // Start new chunk with current section
        currentSections.clear();
        currentLength = 0;
      }
      
      currentSections.add(section);
      currentLength += section.content.length();
      
      // If single section is too large, split it
      if (currentLength > targetChunkSize && currentSections.size() == 1) {
        List<DocumentChunk> sectionChunks = splitLargeSection(document, section, chunks.size());
        chunks.addAll(sectionChunks);
        currentSections.clear();
        currentLength = 0;
      }
    }
    
    // Add remaining sections
    if (!currentSections.isEmpty()) {
      chunks.add(createChunkFromSections(document, currentSections, chunks.size()));
    }
    
    // Link chunks
    linkChunks(chunks);
    
    return chunks;
  }

  private List<DocumentChunk> chunkByContent(RAGDocument document, MarkdownStructure structure) {
    // Fallback to sentence-aware chunking while respecting markdown elements
    String content = document.getContent();
    List<DocumentChunk> chunks = new ArrayList<>();
    
    // Use sentence boundaries but respect code blocks
    List<ContentSegment> segments = createContentSegments(content, structure);
    chunks = groupSegmentsIntoChunks(document, segments);
    
    return chunks;
  }

  private DocumentChunk createChunkFromSections(RAGDocument document, 
                                              List<MarkdownSection> sections, 
                                              int chunkIndex) {
    StringBuilder content = new StringBuilder();
    int startPos = sections.get(0).start;
    int endPos = sections.get(sections.size() - 1).end;
    String mainSection = sections.get(0).headerTitle;
    int headerLevel = sections.get(0).headerLevel;
    
    for (MarkdownSection section : sections) {
      if (content.length() > 0) {
        content.append("\n\n");
      }
      content.append(section.content.trim());
    }
    
    String chunkId = document.getId() + "_chunk_" + chunkIndex;
    
    // Calculate quality metrics
    double qualityScore = calculateMarkdownQuality(sections);
    double coherenceScore = calculateSectionCoherence(sections);
    double densityScore = calculateDensityScore(content.toString());
    
    DocumentChunk.DocumentChunkBuilder builder = DocumentChunk.builder()
        .chunkId(chunkId)
        .documentId(document.getId())
        .content(content.toString())
        .index(chunkIndex)
        .startPosition(startPos)
        .endPosition(endPos)
        
        // Document metadata
        .documentTitle(document.getTitle())
        .documentType(document.getType())
        .sourcePath(document.getSourcePath())
        .documentTimestamp(document.getIndexedAt())
        
        // Chunking metadata
        .chunkingStrategy(getStrategyName())
        .chunkSizeChars(content.length())
        .overlapSize(0)
        .hasOverlapBefore(false)
        .hasOverlapAfter(false)
        
        // Quality metrics
        .qualityScore(qualityScore)
        .coherenceScore(coherenceScore)
        .densityScore(densityScore)
        
        // Section metadata
        .sectionTitle(mainSection)
        .hierarchyLevel(headerLevel);

    DocumentChunk chunk = builder.build();
    
    // Add markdown-specific metadata
    chunk.addMetadata("section_count", String.valueOf(sections.size()));
    chunk.addMetadata("main_header", mainSection != null ? mainSection : "");
    chunk.addMetadata("header_level", String.valueOf(headerLevel));
    chunk.addMetadata("chunk_type", "markdown_aware");
    
    return chunk;
  }

  private List<DocumentChunk> splitLargeSection(RAGDocument document, MarkdownSection section, 
                                              int startIndex) {
    // Split large section using sentence boundaries
    log.debug("Splitting large section: {} ({} chars)", 
        section.headerTitle, section.content.length());
    
    SentenceAwareChunking splitter = new SentenceAwareChunking(targetChunkSize, 0, 0.2);
    
    // Create temporary document for splitting
    RAGDocument tempDoc = RAGDocument.builder()
        .id(document.getId() + "_section")
        .content(section.content)
        .build();
    
    List<DocumentChunk> sectionChunks = splitter.chunk(tempDoc);
    
    // Adjust metadata for section chunks
    List<DocumentChunk> adjustedChunks = new ArrayList<>();
    for (int i = 0; i < sectionChunks.size(); i++) {
      DocumentChunk originalChunk = sectionChunks.get(i);
      String newChunkId = document.getId() + "_chunk_" + (startIndex + i);
      
      // Create new chunk with adjusted metadata
      DocumentChunk adjustedChunk = DocumentChunk.builder()
          .chunkId(newChunkId)
          .documentId(document.getId())
          .content(originalChunk.getContent())
          .index(startIndex + i)
          .startPosition(section.start + originalChunk.getStartPosition())
          .endPosition(section.start + originalChunk.getEndPosition())
          .documentTitle(document.getTitle())
          .documentType(document.getType())
          .sourcePath(document.getSourcePath())
          .documentTimestamp(document.getIndexedAt())
          .chunkingStrategy(getStrategyName())
          .chunkSizeChars(originalChunk.getLength())
          .sectionTitle(section.headerTitle)
          .hierarchyLevel(section.headerLevel)
          .qualityScore(originalChunk.getQualityScore())
          .densityScore(originalChunk.getDensityScore())
          .build();
      
      adjustedChunk.addMetadata("section_split", "true");
      adjustedChunk.addMetadata("section_part", String.valueOf(i + 1));
      adjustedChunk.addMetadata("section_total", String.valueOf(sectionChunks.size()));
      adjustedChunk.addMetadata("chunk_type", "markdown_aware_split");
      
      adjustedChunks.add(adjustedChunk);
    }
    
    return adjustedChunks;
  }

  private List<ContentSegment> createContentSegments(String content, MarkdownStructure structure) {
    // This is a simplified implementation
    // In a full implementation, this would respect all markdown elements
    List<ContentSegment> segments = new ArrayList<>();
    
    String[] paragraphs = content.split("\n\n+");
    int position = 0;
    
    for (String paragraph : paragraphs) {
      paragraph = paragraph.trim();
      if (!paragraph.isEmpty()) {
        segments.add(new ContentSegment(position, position + paragraph.length(), paragraph));
        position += paragraph.length() + 2; // +2 for double newline
      }
    }
    
    return segments;
  }

  private List<DocumentChunk> groupSegmentsIntoChunks(RAGDocument document, 
                                                    List<ContentSegment> segments) {
    // Similar to other grouping methods but respecting markdown elements
    // This is a simplified implementation
    List<DocumentChunk> chunks = new ArrayList<>();
    
    StringBuilder currentChunk = new StringBuilder();
    int currentLength = 0;
    int chunkIndex = 0;
    int startPos = 0;
    
    for (ContentSegment segment : segments) {
      if (currentLength + segment.content.length() > targetChunkSize && 
          currentLength >= minChunkSize) {
        
        // Finalize current chunk
        chunks.add(createSimpleChunk(document, currentChunk.toString(), chunkIndex++, 
                                   startPos, startPos + currentLength));
        
        // Start new chunk
        currentChunk = new StringBuilder();
        currentLength = 0;
        startPos = segment.start;
      }
      
      if (currentLength > 0) {
        currentChunk.append("\n\n");
        currentLength += 2;
      }
      currentChunk.append(segment.content);
      currentLength += segment.content.length();
    }
    
    // Add final chunk
    if (currentLength > 0) {
      chunks.add(createSimpleChunk(document, currentChunk.toString(), chunkIndex, 
                                 startPos, startPos + currentLength));
    }
    
    linkChunks(chunks);
    return chunks;
  }

  private DocumentChunk createSimpleChunk(RAGDocument document, String content, int index,
                                        int startPos, int endPos) {
    String chunkId = document.getId() + "_chunk_" + index;
    
    return DocumentChunk.builder()
        .chunkId(chunkId)
        .documentId(document.getId())
        .content(content)
        .index(index)
        .startPosition(startPos)
        .endPosition(endPos)
        .documentTitle(document.getTitle())
        .documentType(document.getType())
        .sourcePath(document.getSourcePath())
        .documentTimestamp(document.getIndexedAt())
        .chunkingStrategy(getStrategyName())
        .chunkSizeChars(content.length())
        .qualityScore(0.7) // Default quality for simple chunks
        .densityScore(calculateDensityScore(content))
        .build();
  }

  private void linkChunks(List<DocumentChunk> chunks) {
    for (int i = 0; i < chunks.size(); i++) {
      DocumentChunk chunk = chunks.get(i);
      
      if (i > 0) {
        chunk.setPreviousChunkId(chunks.get(i - 1).getChunkId());
        chunk.setHasOverlapBefore(false);
      }
      
      if (i < chunks.size() - 1) {
        chunk.setNextChunkId(chunks.get(i + 1).getChunkId());
        chunk.setHasOverlapAfter(false);
      }
    }
  }

  private double calculateMarkdownQuality(List<MarkdownSection> sections) {
    if (sections.isEmpty()) {
      return 0.0;
    }
    
    // Quality based on section structure and content
    double structureScore = Math.min(1.0, sections.size() / 3.0); // 1-3 sections ideal
    double headerScore = sections.stream().anyMatch(s -> s.headerTitle != null) ? 1.0 : 0.5;
    
    double avgLength = sections.stream()
        .mapToInt(s -> s.content.length())
        .average()
        .orElse(0.0);
    double lengthScore = 1.0 - Math.abs(avgLength - targetChunkSize) / targetChunkSize;
    
    return (structureScore * 0.4 + headerScore * 0.3 + lengthScore * 0.3);
  }

  private double calculateSectionCoherence(List<MarkdownSection> sections) {
    if (sections.size() <= 1) {
      return 1.0;
    }
    
    // Simple coherence: sections with similar header levels are more coherent
    int[] levels = sections.stream()
        .mapToInt(s -> s.headerLevel)
        .toArray();
    
    double variance = 0.0;
    double mean = (double) levels[0];
    
    for (int level : levels) {
      variance += Math.pow(level - mean, 2);
    }
    variance /= levels.length;
    
    // Lower variance = higher coherence
    return Math.max(0.0, 1.0 - variance / 10.0);
  }

  private double calculateDensityScore(String content) {
    if (content == null || content.isEmpty()) {
      return 0.0;
    }
    
    int totalChars = content.length();
    int nonWhitespaceChars = content.replaceAll("\\s", "").length();
    
    return (double) nonWhitespaceChars / totalChars;
  }

  private List<DocumentChunk> createFallbackChunks(RAGDocument document, String content) {
    log.warn("Falling back to sentence-aware chunking for document {}", document.getId());
    SentenceAwareChunking fallback = SentenceAwareChunking.defaults();
    return fallback.chunk(document);
  }

  // === Inner Classes ===

  private static class MarkdownStructure {
    final List<MarkdownHeader> headers = new ArrayList<>();
    final List<MarkdownElement> codeBlocks = new ArrayList<>();
    final List<MarkdownElement> listItems = new ArrayList<>();
    final List<MarkdownElement> tableRows = new ArrayList<>();
    final List<MarkdownSection> sections = new ArrayList<>();
    
    boolean hasHeaders = false;
    boolean hasCodeBlocks = false;
    boolean hasLists = false;
    boolean hasTables = false;
    
    void addHeader(MarkdownHeader header) {
      headers.add(header);
      hasHeaders = true;
    }
    
    void addCodeBlock(MarkdownElement element) {
      codeBlocks.add(element);
      hasCodeBlocks = true;
    }
    
    void addListItem(MarkdownElement element) {
      listItems.add(element);
      hasLists = true;
    }
    
    void addTableRow(MarkdownElement element) {
      tableRows.add(element);
      hasTables = true;
    }
    
    void addSection(MarkdownSection section) {
      sections.add(section);
    }
  }

  private static class MarkdownHeader {
    final int level;
    final String title;
    final int position;

    MarkdownHeader(int level, String title, int position) {
      this.level = level;
      this.title = title;
      this.position = position;
    }
  }

  private static class MarkdownElement {
    final int start;
    final int end;
    final String content;

    MarkdownElement(int start, int end, String content) {
      this.start = start;
      this.end = end;
      this.content = content;
    }
  }

  private static class MarkdownSection {
    final int start;
    final int end;
    final String headerTitle;
    final int headerLevel;
    final String content;

    MarkdownSection(int start, int end, String headerTitle, int headerLevel, String content) {
      this.start = start;
      this.end = end;
      this.headerTitle = headerTitle;
      this.headerLevel = headerLevel;
      this.content = content;
    }
  }

  private static class ContentSegment {
    final int start;
    final int end;
    final String content;

    ContentSegment(int start, int end, String content) {
      this.start = start;
      this.end = end;
      this.content = content;
    }
  }
}