package com.noteflix.pcm.rag.pipeline.indexer;

import com.noteflix.pcm.rag.api.RAGService;
import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.ChunkingFactory;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.embedding.api.EmbeddingService;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.parser.api.DocumentParser;
import com.noteflix.pcm.rag.parser.core.*;
import com.noteflix.pcm.rag.vectorstore.api.VectorStore;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for indexing documents into RAG system. Supports: - Single file indexing - Directory
 * indexing (recursive) - Multiple file parsers - Progress tracking
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class DocumentIndexer {

  private final RAGService ragService;
  private final List<DocumentParser> parsers;

  /** -- GETTER -- Get indexing progress. */
  @Getter private IndexingProgress progress;

  public DocumentIndexer(RAGService ragService) {
    this.ragService = ragService;
    this.parsers = new ArrayList<>();

    // Register default parsers
    registerParser(new JavaParser());
    registerParser(new SQLParser());
    registerParser(new MarkdownParser());
    registerParser(new TextParser());
  }

  /** Register a document parser. */
  public void registerParser(DocumentParser parser) {
    parsers.add(parser);
    log.debug("Registered parser for: {}", String.join(", ", parser.getSupportedExtensions()));
  }

  /** Index a single file. */
  public void indexFile(Path filePath) throws IOException {
    if (!Files.exists(filePath)) {
      throw new IOException("File not found: " + filePath);
    }

    if (!Files.isRegularFile(filePath)) {
      throw new IOException("Not a regular file: " + filePath);
    }

    // Find parser
    DocumentParser parser = findParser(filePath);

    if (parser == null) {
      log.warn("No parser found for: {}", filePath);
      return;
    }

    // Parse and index
    RAGDocument document = parser.parse(filePath);
    ragService.indexDocument(document);

    log.info("Indexed file: {}", filePath);
  }

  /** Index entire directory (recursive). */
  public IndexingProgress indexDirectory(Path dirPath) throws IOException {
    return indexDirectory(dirPath, true);
  }

  /**
   * Index directory with options.
   *
   * @param dirPath Directory path
   * @param recursive Whether to index subdirectories
   * @return Indexing progress
   */
  public IndexingProgress indexDirectory(Path dirPath, boolean recursive) throws IOException {
    if (!Files.exists(dirPath)) {
      throw new IOException("Directory not found: " + dirPath);
    }

    if (!Files.isDirectory(dirPath)) {
      throw new IOException("Not a directory: " + dirPath);
    }

    // Initialize progress
    progress = new IndexingProgress();
    progress.setStartTime(System.currentTimeMillis());

    log.info("Starting directory indexing: {} (recursive: {})", dirPath, recursive);

    // Walk file tree
    if (recursive) {
      Files.walkFileTree(
          dirPath,
          new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
              // Skip hidden directories
              if (dir.getFileName().toString().startsWith(".")) {
                return FileVisitResult.SKIP_SUBTREE;
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              processFile(file);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
              log.error("Failed to visit file: {}", file, exc);
              progress.incrementFailed();
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
              return FileVisitResult.CONTINUE;
            }
          });
    } else {
      // Non-recursive: only files in directory
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
        for (Path file : stream) {
          if (Files.isRegularFile(file)) {
            processFile(file);
          }
        }
      }
    }

    // Finalize progress
    progress.setEndTime(System.currentTimeMillis());

    log.info(
        "Directory indexing completed: {} files indexed, {} skipped, {} failed in {}ms",
        progress.getIndexedCount(),
        progress.getSkippedCount(),
        progress.getFailedCount(),
        progress.getDuration());

    return progress;
  }

  // ========== Private Methods ==========

  private void processFile(Path file) {
    try {
      // Skip hidden files
      if (file.getFileName().toString().startsWith(".")) {
        progress.incrementSkipped();
        return;
      }

      // Find parser
      DocumentParser parser = findParser(file);

      if (parser == null) {
        log.debug("No parser for: {}", file);
        progress.incrementSkipped();
        return;
      }

      // Parse and index
      RAGDocument document = parser.parse(file);
      ragService.indexDocument(document);

      progress.incrementIndexed();
      log.debug("Indexed: {}", file);

    } catch (Exception e) {
      log.error("Failed to index file: {}", file, e);
      progress.incrementFailed();
    }
  }

  private DocumentParser findParser(Path filePath) {
    for (DocumentParser parser : parsers) {
      if (parser.canParse(filePath)) {
        return parser;
      }
    }
    return null;
  }

  /** Indexing progress tracker. */
  public static class IndexingProgress {
    @Getter private int indexedCount = 0;
    @Getter private int skippedCount = 0;
    @Getter private int failedCount = 0;
    @Setter private long startTime;
    @Setter private long endTime;

    public void incrementIndexed() {
      indexedCount++;
    }

    public void incrementSkipped() {
      skippedCount++;
    }

    public void incrementFailed() {
      failedCount++;
    }

    public int getTotalCount() {
      return indexedCount + skippedCount + failedCount;
    }

    public long getDuration() {
      return endTime - startTime;
    }

    public boolean isComplete() {
      return endTime > 0;
    }

    @Override
    public String toString() {
      return String.format(
          "Indexing Progress: %d indexed, %d skipped, %d failed (total: %d) in %dms",
          indexedCount, skippedCount, failedCount, getTotalCount(), getDuration());
    }
  }
}
