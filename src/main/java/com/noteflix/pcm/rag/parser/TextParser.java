package com.noteflix.pcm.rag.parser;

import com.noteflix.pcm.rag.model.DocumentType;
import com.noteflix.pcm.rag.model.RAGDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Parser for plain text files.
 *
 * <p>Fallback parser for any text-based files.
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class TextParser implements DocumentParser {

  @Override
  public boolean canParse(Path filePath) {
    String fileName = filePath.getFileName().toString().toLowerCase();

    // Handle common text extensions
    return fileName.endsWith(".txt")
        || fileName.endsWith(".log")
        || fileName.endsWith(".properties")
        || fileName.endsWith(".xml")
        || fileName.endsWith(".json")
        || fileName.endsWith(".yml")
        || fileName.endsWith(".yaml");
  }

  @Override
  public RAGDocument parse(Path filePath) throws IOException {
    String content = Files.readString(filePath);

    // Build document
    RAGDocument doc =
        RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(DocumentType.TEXT)
            .title(filePath.getFileName().toString())
            .sourcePath(filePath.toString())
            .content(content)
            .indexedAt(LocalDateTime.now())
            .build();

    // Add metadata
    String extension = getExtension(filePath);
    doc.addMetadata("format", extension);
    doc.addMetadata("fileType", determineFileType(extension));

    log.debug("Parsed text file: {} (format: {})", filePath.getFileName(), extension);

    return doc;
  }

  @Override
  public String[] getSupportedExtensions() {
    return new String[] {".txt", ".log", ".properties", ".xml", ".json", ".yml", ".yaml"};
  }

  private String getExtension(Path filePath) {
    String fileName = filePath.getFileName().toString();
    int dotIndex = fileName.lastIndexOf('.');
    return dotIndex > 0 ? fileName.substring(dotIndex) : "";
  }

  private String determineFileType(String extension) {
    switch (extension.toLowerCase()) {
      case ".properties":
        return "configuration";
      case ".xml":
      case ".json":
      case ".yml":
      case ".yaml":
        return "configuration";
      case ".log":
        return "log";
      default:
        return "text";
    }
  }
}
