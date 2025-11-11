package com.noteflix.pcm.ui.components.text;

/**
 * Enum defining different types of text content that can be rendered
 * by the Universal Text Component.
 */
public enum TextContentType {
    /**
     * Plain markdown text with standard markdown formatting
     */
    MARKDOWN("md", "markdown"),
    
    /**
     * Mermaid diagram definition text
     */
    MERMAID("mmd", "mermaid"),
    
    /**
     * Source code with syntax highlighting
     */
    CODE("code", "source"),
    
    /**
     * Plain text without any formatting
     */
    PLAIN_TEXT("txt", "text");
    
    private final String fileExtension;
    private final String displayName;
    
    TextContentType(String fileExtension, String displayName) {
        this.fileExtension = fileExtension;
        this.displayName = displayName;
    }
    
    public String getFileExtension() {
        return fileExtension;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Detect content type from file extension or content analysis
     */
    public static TextContentType fromExtension(String extension) {
        if (extension == null) return PLAIN_TEXT;
        
        String ext = extension.toLowerCase().trim();
        if (ext.startsWith(".")) ext = ext.substring(1);
        
        switch (ext) {
            case "md": case "markdown": return MARKDOWN;
            case "mmd": case "mermaid": return MERMAID;
            case "java": case "js": case "py": case "cpp": 
            case "c": case "cs": case "go": case "rs":
            case "sql": case "json": case "xml": case "html":
            case "css": case "scss": case "yaml": case "yml":
                return CODE;
            default: return PLAIN_TEXT;
        }
    }
    
    /**
     * Auto-detect content type from text content
     */
    public static TextContentType autoDetect(String content) {
        if (content == null || content.trim().isEmpty()) {
            return PLAIN_TEXT;
        }
        
        String trimmed = content.trim();
        
        // Check for Mermaid diagrams
        if (trimmed.startsWith("graph") || trimmed.startsWith("sequenceDiagram") ||
            trimmed.startsWith("classDiagram") || trimmed.startsWith("flowchart") ||
            trimmed.contains("-->") || trimmed.contains("->")) {
            return MERMAID;
        }
        
        // Check for Markdown indicators
        if (trimmed.contains("# ") || trimmed.contains("## ") ||
            trimmed.contains("**") || trimmed.contains("*") ||
            trimmed.contains("[") && trimmed.contains("](") ||
            trimmed.contains("```")) {
            return MARKDOWN;
        }
        
        // Check for code patterns
        if (trimmed.contains("{") && trimmed.contains("}") ||
            trimmed.contains("class ") || trimmed.contains("function ") ||
            trimmed.contains("import ") || trimmed.contains("package ") ||
            trimmed.contains("public ") || trimmed.contains("private ")) {
            return CODE;
        }
        
        return PLAIN_TEXT;
    }
}