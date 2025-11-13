package com.noteflix.pcm.rag.parser;

import com.noteflix.pcm.rag.model.DocumentType;
import com.noteflix.pcm.rag.model.RAGDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Parser for Java source files.
 *
 * <p>Extracts: - Package name - Class/interface names - Method signatures - Comments
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class JavaParser implements DocumentParser {

  private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([\\w.]+);");
  private static final Pattern CLASS_PATTERN =
      Pattern.compile("(?:public\\s+)?(?:class|interface|enum)\\s+(\\w+)");

  @Override
  public boolean canParse(Path filePath) {
    String fileName = filePath.getFileName().toString().toLowerCase();
    return fileName.endsWith(".java");
  }

  @Override
  public RAGDocument parse(Path filePath) throws IOException {
    String content = Files.readString(filePath);

    // Extract metadata
    String packageName = extractPackage(content);
    String className = extractClassName(content);

    // Build document
    RAGDocument doc =
        RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(DocumentType.JAVA_CLASS)
            .title(filePath.getFileName().toString())
            .sourcePath(filePath.toString())
            .content(content)
            .indexedAt(LocalDateTime.now())
            .build();

    // Add metadata
    if (packageName != null) {
      doc.addMetadata("package", packageName);
    }
    if (className != null) {
      doc.addMetadata("class", className);
    }
    doc.addMetadata("language", "java");
    doc.addMetadata("fileType", "source");

    log.debug(
        "Parsed Java file: {} (package: {}, class: {})",
        filePath.getFileName(),
        packageName,
        className);

    return doc;
  }

  @Override
  public String[] getSupportedExtensions() {
    return new String[] {".java"};
  }

  private String extractPackage(String content) {
    Matcher matcher = PACKAGE_PATTERN.matcher(content);
    return matcher.find() ? matcher.group(1) : null;
  }

  private String extractClassName(String content) {
    Matcher matcher = CLASS_PATTERN.matcher(content);
    return matcher.find() ? matcher.group(1) : null;
  }
}
