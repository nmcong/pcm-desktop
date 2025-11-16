package com.noteflix.pcm.rag.parser.core;

import com.noteflix.pcm.rag.model.DocumentType;
import com.noteflix.pcm.rag.model.RAGDocument;
import com.noteflix.pcm.rag.parser.api.DocumentParser;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Markdown files.
 *
 * <p>Extracts: - Title (from # heading or filename) - Content - Metadata
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class MarkdownParser implements DocumentParser {

    private static final Pattern TITLE_PATTERN = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);

    @Override
    public boolean canParse(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".md") || fileName.endsWith(".markdown");
    }

    @Override
    public RAGDocument parse(Path filePath) throws IOException {
        String content = Files.readString(filePath);

        // Extract title
        String title = extractTitle(content);
        if (title == null) {
            title = filePath.getFileName().toString().replace(".md", "").replace(".markdown", "");
        }

        // Build document
        RAGDocument doc =
                RAGDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .type(DocumentType.KNOWLEDGE_BASE)
                        .title(title)
                        .sourcePath(filePath.toString())
                        .content(content)
                        .indexedAt(LocalDateTime.now())
                        .build();

        // Add metadata
        doc.addMetadata("format", "markdown");
        doc.addMetadata("fileType", "documentation");

        // Determine category from path
        String category = determineCategory(filePath);
        if (category != null) {
            doc.addMetadata("category", category);
        }

        log.debug("Parsed Markdown file: {} (title: {})", filePath.getFileName(), title);

        return doc;
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".md", ".markdown"};
    }

    private String extractTitle(String content) {
        Matcher matcher = TITLE_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String determineCategory(Path filePath) {
        String pathStr = filePath.toString();

        if (pathStr.contains("/docs/") || pathStr.contains("\\docs\\")) {
            return "documentation";
        } else if (pathStr.contains("/kb/")
                || pathStr.contains("\\kb\\")
                || pathStr.contains("knowledge")) {
            return "knowledge-base";
        } else if (pathStr.contains("/api/") || pathStr.contains("\\api\\")) {
            return "api";
        }

        return null;
    }
}
