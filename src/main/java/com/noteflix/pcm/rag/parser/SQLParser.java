package com.noteflix.pcm.rag.parser;

import com.noteflix.pcm.rag.model.DocumentType;
import com.noteflix.pcm.rag.model.RAGDocument;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for SQL files.
 * 
 * Handles:
 * - Table DDL
 * - Stored procedures
 * - Functions
 * - Views
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class SQLParser implements DocumentParser {
    
    private static final Pattern CREATE_TABLE_PATTERN = 
        Pattern.compile("CREATE\\s+TABLE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_PROC_PATTERN = 
        Pattern.compile("CREATE\\s+(?:OR\\s+REPLACE\\s+)?PROCEDURE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_FUNCTION_PATTERN = 
        Pattern.compile("CREATE\\s+(?:OR\\s+REPLACE\\s+)?FUNCTION\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
    
    @Override
    public boolean canParse(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".sql") || fileName.endsWith(".ddl");
    }
    
    @Override
    public RAGDocument parse(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        
        // Determine SQL object type
        DocumentType type = determineSQLType(content);
        String objectName = extractObjectName(content);
        
        // Build document
        RAGDocument doc = RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(type)
            .title(objectName != null ? objectName : filePath.getFileName().toString())
            .sourcePath(filePath.toString())
            .content(content)
            .indexedAt(LocalDateTime.now())
            .build();
        
        // Add metadata
        doc.addMetadata("language", "sql");
        doc.addMetadata("fileType", "database");
        if (objectName != null) {
            doc.addMetadata("objectName", objectName);
        }
        
        log.debug("Parsed SQL file: {} (type: {}, object: {})", 
            filePath.getFileName(), type, objectName);
        
        return doc;
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[] { ".sql", ".ddl" };
    }
    
    private DocumentType determineSQLType(String content) {
        String upperContent = content.toUpperCase();
        
        if (upperContent.contains("CREATE TABLE") || upperContent.contains("ALTER TABLE")) {
            return DocumentType.TABLE;
        } else if (upperContent.contains("CREATE PROCEDURE") || upperContent.contains("CREATE OR REPLACE PROCEDURE")) {
            return DocumentType.PROCEDURE;
        } else if (upperContent.contains("CREATE FUNCTION") || upperContent.contains("CREATE OR REPLACE FUNCTION")) {
            return DocumentType.FUNCTION;
        } else if (upperContent.contains("CREATE VIEW") || upperContent.contains("CREATE OR REPLACE VIEW")) {
            return DocumentType.VIEW;
        }
        
        return DocumentType.UNKNOWN;
    }
    
    private String extractObjectName(String content) {
        // Try table
        Matcher tableMatcher = CREATE_TABLE_PATTERN.matcher(content);
        if (tableMatcher.find()) {
            return tableMatcher.group(1);
        }
        
        // Try procedure
        Matcher procMatcher = CREATE_PROC_PATTERN.matcher(content);
        if (procMatcher.find()) {
            return procMatcher.group(1);
        }
        
        // Try function
        Matcher funcMatcher = CREATE_FUNCTION_PATTERN.matcher(content);
        if (funcMatcher.find()) {
            return funcMatcher.group(1);
        }
        
        return null;
    }
}

