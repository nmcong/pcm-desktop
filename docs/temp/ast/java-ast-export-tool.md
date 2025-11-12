# Java AST Export Tool - Tạo Cấu Trúc AST Trung Gian cho PCM WebApp

## Tổng Quan

Tài liệu này mô tả phương án tạo một công cụ Java độc lập để export cấu trúc AST (Abstract Syntax Tree) từ source code
Java sang định dạng JSON trung gian, sau đó import vào IndexedDB của browser cho PCM WebApp. Phương án này cho phép xử
lý offline và tận dụng sức mạnh của các parser Java chuyên nghiệp.

## 1. Kiến Trúc Tổng Thể

### 1.1 Workflow Overview

```
Java Source Files → Java AST Export Tool → JSON Intermediate Format → Browser IndexedDB → LLM Analysis
```

### 1.2 Components Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Java AST Export Tool                         │
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │   File Scanner  │  │   AST Processor  │  │  JSON Exporter  │ │
│  │                 │  │                  │  │                 │ │
│  │ - Directory     │  │ - JavaParser     │  │ - Format        │ │
│  │   Traversal     │  │ - Eclipse JDT    │  │   Conversion    │ │
│  │ - File Filter   │  │ - Visitor        │  │ - Optimization  │ │
│  └─────────────────┘  └──────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                 Intermediate JSON Format                        │
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │   Metadata      │  │   AST Nodes      │  │   Relations     │ │
│  │                 │  │                  │  │                 │ │
│  │ - File Info     │  │ - Node Types     │  │ - Parent/Child  │ │
│  │ - Parse Time    │  │ - Positions      │  │ - References    │ │
│  │ - Version       │  │ - Properties     │  │ - Symbols       │ │
│  └─────────────────┘  └──────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Browser IndexedDB                            │
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │  AST Database   │  │   File Index     │  │   Search Index  │ │
│  │                 │  │                  │  │                 │ │
│  │ - AST Trees     │  │ - File Mapping   │  │ - Symbol Index  │ │
│  │ - Node Cache    │  │ - Dependencies   │  │ - Type Index    │ │
│  │ - Metadata      │  │ - Timestamps     │  │ - Method Index  │ │
│  └─────────────────┘  └──────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 2. Java AST Export Tool Implementation

### 2.1 Core Dependencies

```xml
<!-- pom.xml -->
<dependencies>
    <!-- JavaParser for AST parsing -->
    <dependency>
        <groupId>com.github.javaparser</groupId>
        <artifactId>javaparser-core</artifactId>
        <version>3.27.1</version>
    </dependency>

    <!-- Jackson for JSON serialization -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>

    <!-- Picocli for CLI -->
    <dependency>
        <groupId>info.picocli</groupId>
        <artifactId>picocli</artifactId>
        <version>4.7.5</version>
    </dependency>

    <!-- Commons IO for file operations -->
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.11.0</version>
    </dependency>
</dependencies>
```

### 2.2 Main Application Class

```java
// File: src/main/java/com/pcm/ast/JavaASTExportTool.java
package com.pcm.ast;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(
    name = "java-ast-export",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Export Java AST to JSON intermediate format for PCM WebApp"
)
public class JavaASTExportTool implements Callable<Integer> {

    @Parameters(
        index = "0",
        description = "Source directory containing Java files"
    )
    private File sourceDir;

    @Option(
        names = {"-o", "--output"},
        description = "Output JSON file path",
        defaultValue = "ast-export.json"
    )
    private String outputPath;

    @Option(
        names = {"-f", "--format"},
        description = "Output format: compact, readable",
        defaultValue = "compact"
    )
    private String format;

    @Option(
        names = {"-i", "--include"},
        description = "Include pattern (e.g., *.java)",
        defaultValue = "**/*.java"
    )
    private String includePattern;

    @Option(
        names = {"-e", "--exclude"},
        description = "Exclude patterns",
        split = ","
    )
    private String[] excludePatterns = {"**/target/**", "**/build/**"};

    @Option(
        names = {"-v", "--verbose"},
        description = "Verbose output"
    )
    private boolean verbose;

    @Option(
        names = {"-c", "--chunk-size"},
        description = "Chunk size for large projects",
        defaultValue = "1000"
    )
    private int chunkSize;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JavaASTExportTool()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        log("Starting Java AST Export Tool...");
        log("Source directory: " + sourceDir.getAbsolutePath());
        log("Output file: " + outputPath);
        log("Format: " + format);

        try {
            // Validate input
            if (!sourceDir.exists() || !sourceDir.isDirectory()) {
                System.err.println("Error: Source directory does not exist or is not a directory");
                return 1;
            }

            // Initialize components
            FileScanner scanner = new FileScanner(includePattern, excludePatterns);
            ASTProcessor processor = new ASTProcessor();
            JSONExporter exporter = new JSONExporter(format, chunkSize);

            // Process files
            Path sourcePath = sourceDir.toPath();
            Path outputFilePath = Paths.get(outputPath);

            log("Scanning files...");
            var javaFiles = scanner.scanJavaFiles(sourcePath);
            log(String.format("Found %d Java files", javaFiles.size()));

            log("Processing AST...");
            var astData = processor.processFiles(javaFiles, this::log);
            log(String.format("Processed %d files successfully", astData.getProcessedFiles().size()));

            log("Exporting to JSON...");
            exporter.exportToJSON(astData, outputFilePath);
            log("Export completed successfully!");

            return 0;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    private void log(String message) {
        if (verbose) {
            System.out.println("[INFO] " + message);
        }
    }
}
```

### 2.3 File Scanner Component

```java
// File: src/main/java/com/pcm/ast/FileScanner.java
package com.pcm.ast;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileScanner {
    private final PathMatcher includeMatcher;
    private final List<PathMatcher> excludeMatchers;

    public FileScanner(String includePattern, String[] excludePatterns) {
        this.includeMatcher = FileSystems.getDefault().getPathMatcher("glob:" + includePattern);
        this.excludeMatchers = new ArrayList<>();

        for (String excludePattern : excludePatterns) {
            excludeMatchers.add(FileSystems.getDefault().getPathMatcher("glob:" + excludePattern));
        }
    }

    public List<Path> scanJavaFiles(Path sourceDir) throws IOException {
        List<Path> javaFiles = new ArrayList<>();

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = sourceDir.relativize(file);

                // Check if file matches include pattern
                if (includeMatcher.matches(relativePath)) {
                    // Check if file should be excluded
                    boolean shouldExclude = excludeMatchers.stream()
                        .anyMatch(matcher -> matcher.matches(relativePath));

                    if (!shouldExclude) {
                        javaFiles.add(file);
                    }
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = sourceDir.relativize(dir);

                // Check if directory should be excluded
                boolean shouldExclude = excludeMatchers.stream()
                    .anyMatch(matcher -> matcher.matches(relativePath));

                return shouldExclude ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }
        });

        return javaFiles;
    }
}
```

### 2.4 AST Processor Component

```java
// File: src/main/java/com/pcm/ast/ASTProcessor.java
package com.pcm.ast;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class ASTProcessor {
    private final JavaParser javaParser;
    private final Map<String, Object> globalMetadata;

    public ASTProcessor() {
        this.javaParser = new JavaParser();
        this.globalMetadata = new HashMap<>();
        this.globalMetadata.put("toolVersion", "1.0.0");
        this.globalMetadata.put("javaParserVersion", "3.27.1");
        this.globalMetadata.put("exportTime", Instant.now().toString());
    }

    public ASTExportData processFiles(List<Path> javaFiles, Consumer<String> logger) {
        ASTExportData exportData = new ASTExportData();
        exportData.setMetadata(globalMetadata);

        int processedCount = 0;
        int totalFiles = javaFiles.size();

        for (Path javaFile : javaFiles) {
            try {
                processedCount++;
                if (processedCount % 100 == 0) {
                    logger.accept(String.format("Processing file %d/%d: %s",
                        processedCount, totalFiles, javaFile.getFileName()));
                }

                FileASTData fileData = processFile(javaFile);
                exportData.addFileData(fileData);

            } catch (Exception e) {
                logger.accept("Error processing file " + javaFile + ": " + e.getMessage());
                // Continue with other files
            }
        }

        return exportData;
    }

    private FileASTData processFile(Path javaFile) throws IOException {
        String sourceCode = Files.readString(javaFile);
        ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);

        if (!parseResult.isSuccessful()) {
            throw new RuntimeException("Failed to parse file: " + javaFile);
        }

        CompilationUnit cu = parseResult.getResult().orElseThrow();

        FileASTData fileData = new FileASTData();
        fileData.setFilePath(javaFile.toString());
        fileData.setFileName(javaFile.getFileName().toString());
        fileData.setFileSize(Files.size(javaFile));
        fileData.setLastModified(Files.getLastModifiedTime(javaFile).toInstant());
        fileData.setSourceCode(sourceCode);

        // Convert AST to intermediate format
        ASTNodeData rootNode = convertToNodeData(cu);
        fileData.setRootNode(rootNode);

        // Extract symbols and references
        SymbolExtractor symbolExtractor = new SymbolExtractor();
        cu.accept(symbolExtractor, fileData);

        return fileData;
    }

    private ASTNodeData convertToNodeData(Node node) {
        ASTNodeData nodeData = new ASTNodeData();
        nodeData.setNodeType(node.getClass().getSimpleName());
        nodeData.setStartLine(node.getBegin().map(pos -> pos.line).orElse(-1));
        nodeData.setStartColumn(node.getBegin().map(pos -> pos.column).orElse(-1));
        nodeData.setEndLine(node.getEnd().map(pos -> pos.line).orElse(-1));
        nodeData.setEndColumn(node.getEnd().map(pos -> pos.column).orElse(-1));

        // Extract text content for leaf nodes
        if (node.getChildNodes().isEmpty()) {
            nodeData.setText(node.toString());
        }

        // Process child nodes
        List<ASTNodeData> children = new ArrayList<>();
        for (Node child : node.getChildNodes()) {
            children.add(convertToNodeData(child));
        }
        nodeData.setChildren(children);

        // Extract properties specific to node type
        Map<String, Object> properties = extractNodeProperties(node);
        nodeData.setProperties(properties);

        return nodeData;
    }

    private Map<String, Object> extractNodeProperties(Node node) {
        Map<String, Object> properties = new HashMap<>();

        // Add common properties based on node type
        switch (node.getClass().getSimpleName()) {
            case "ClassOrInterfaceDeclaration":
                var classDecl = (com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) node;
                properties.put("name", classDecl.getNameAsString());
                properties.put("isInterface", classDecl.isInterface());
                properties.put("modifiers", classDecl.getModifiers().toString());
                break;

            case "MethodDeclaration":
                var methodDecl = (com.github.javaparser.ast.body.MethodDeclaration) node;
                properties.put("name", methodDecl.getNameAsString());
                properties.put("returnType", methodDecl.getType().toString());
                properties.put("modifiers", methodDecl.getModifiers().toString());
                properties.put("parameterCount", methodDecl.getParameters().size());
                break;

            case "FieldDeclaration":
                var fieldDecl = (com.github.javaparser.ast.body.FieldDeclaration) node;
                properties.put("type", fieldDecl.getElementType().toString());
                properties.put("modifiers", fieldDecl.getModifiers().toString());
                break;

            case "VariableDeclarator":
                var varDecl = (com.github.javaparser.ast.body.VariableDeclarator) node;
                properties.put("name", varDecl.getNameAsString());
                break;
        }

        return properties;
    }

    // Inner class for symbol extraction
    private static class SymbolExtractor extends VoidVisitorAdapter<FileASTData> {
        @Override
        public void visit(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration n, FileASTData arg) {
            arg.addSymbol("class", n.getNameAsString(), n.getBegin().orElse(null));
            super.visit(n, arg);
        }

        @Override
        public void visit(com.github.javaparser.ast.body.MethodDeclaration n, FileASTData arg) {
            arg.addSymbol("method", n.getNameAsString(), n.getBegin().orElse(null));
            super.visit(n, arg);
        }

        @Override
        public void visit(com.github.javaparser.ast.body.FieldDeclaration n, FileASTData arg) {
            n.getVariables().forEach(var ->
                arg.addSymbol("field", var.getNameAsString(), n.getBegin().orElse(null))
            );
            super.visit(n, arg);
        }
    }
}
```

### 2.5 Data Models

```java
// File: src/main/java/com/pcm/ast/model/ASTExportData.java
package com.pcm.ast.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;

public class ASTExportData {
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("files")
    private List<FileASTData> files;

    @JsonProperty("statistics")
    private Map<String, Object> statistics;

    public ASTExportData() {
        this.files = new ArrayList<>();
        this.statistics = new HashMap<>();
    }

    public void addFileData(FileASTData fileData) {
        files.add(fileData);
        updateStatistics();
    }

    private void updateStatistics() {
        statistics.put("totalFiles", files.size());
        statistics.put("totalNodes", files.stream()
            .mapToInt(f -> countNodes(f.getRootNode()))
            .sum());
        statistics.put("totalSymbols", files.stream()
            .mapToInt(f -> f.getSymbols().size())
            .sum());
    }

    private int countNodes(ASTNodeData node) {
        if (node == null) return 0;
        return 1 + node.getChildren().stream()
            .mapToInt(this::countNodes)
            .sum();
    }

    // Getters and setters
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public List<FileASTData> getFiles() { return files; }
    public void setFiles(List<FileASTData> files) { this.files = files; }

    public Map<String, Object> getStatistics() { return statistics; }
    public void setStatistics(Map<String, Object> statistics) { this.statistics = statistics; }

    public List<FileASTData> getProcessedFiles() { return files; }
}
```

```java
// File: src/main/java/com/pcm/ast/model/FileASTData.java
package com.pcm.ast.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.Position;
import java.time.Instant;
import java.util.*;

public class FileASTData {
    @JsonProperty("filePath")
    private String filePath;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("fileSize")
    private long fileSize;

    @JsonProperty("lastModified")
    private Instant lastModified;

    @JsonProperty("sourceCode")
    private String sourceCode;

    @JsonProperty("rootNode")
    private ASTNodeData rootNode;

    @JsonProperty("symbols")
    private List<SymbolData> symbols;

    public FileASTData() {
        this.symbols = new ArrayList<>();
    }

    public void addSymbol(String type, String name, Position position) {
        SymbolData symbol = new SymbolData();
        symbol.setType(type);
        symbol.setName(name);
        if (position != null) {
            symbol.setLine(position.line);
            symbol.setColumn(position.column);
        }
        symbols.add(symbol);
    }

    // Getters and setters
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public Instant getLastModified() { return lastModified; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public ASTNodeData getRootNode() { return rootNode; }
    public void setRootNode(ASTNodeData rootNode) { this.rootNode = rootNode; }

    public List<SymbolData> getSymbols() { return symbols; }
    public void setSymbols(List<SymbolData> symbols) { this.symbols = symbols; }
}
```

```java
// File: src/main/java/com/pcm/ast/model/ASTNodeData.java
package com.pcm.ast.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;

public class ASTNodeData {
    @JsonProperty("nodeType")
    private String nodeType;

    @JsonProperty("startLine")
    private int startLine;

    @JsonProperty("startColumn")
    private int startColumn;

    @JsonProperty("endLine")
    private int endLine;

    @JsonProperty("endColumn")
    private int endColumn;

    @JsonProperty("text")
    private String text;

    @JsonProperty("properties")
    private Map<String, Object> properties;

    @JsonProperty("children")
    private List<ASTNodeData> children;

    public ASTNodeData() {
        this.properties = new HashMap<>();
        this.children = new ArrayList<>();
    }

    // Getters and setters
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public int getStartLine() { return startLine; }
    public void setStartLine(int startLine) { this.startLine = startLine; }

    public int getStartColumn() { return startColumn; }
    public void setStartColumn(int startColumn) { this.startColumn = startColumn; }

    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }

    public int getEndColumn() { return endColumn; }
    public void setEndColumn(int endColumn) { this.endColumn = endColumn; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }

    public List<ASTNodeData> getChildren() { return children; }
    public void setChildren(List<ASTNodeData> children) { this.children = children; }
}
```

```java
// File: src/main/java/com/pcm/ast/model/SymbolData.java
package com.pcm.ast.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SymbolData {
    @JsonProperty("type")
    private String type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("line")
    private int line;

    @JsonProperty("column")
    private int column;

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }

    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }
}
```

### 2.6 JSON Exporter Component

```java
// File: src/main/java/com/pcm/ast/JSONExporter.java
package com.pcm.ast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pcm.ast.model.ASTExportData;
import com.pcm.ast.model.FileASTData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JSONExporter {
    private final ObjectMapper objectMapper;
    private final boolean prettyPrint;
    private final int chunkSize;

    public JSONExporter(String format, int chunkSize) {
        this.objectMapper = new ObjectMapper();
        this.prettyPrint = "readable".equals(format);
        this.chunkSize = chunkSize;

        if (prettyPrint) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        // Configure for better JSON handling
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.findAndRegisterModules();
    }

    public void exportToJSON(ASTExportData exportData, Path outputPath) throws IOException {
        List<FileASTData> allFiles = exportData.getFiles();

        if (allFiles.size() <= chunkSize) {
            // Single file export
            exportSingleFile(exportData, outputPath);
        } else {
            // Chunked export for large datasets
            exportChunkedFiles(exportData, outputPath);
        }
    }

    private void exportSingleFile(ASTExportData exportData, Path outputPath) throws IOException {
        objectMapper.writeValue(outputPath.toFile(), exportData);
    }

    private void exportChunkedFiles(ASTExportData exportData, Path outputPath) throws IOException {
        List<FileASTData> allFiles = exportData.getFiles();
        String baseName = getBaseName(outputPath);
        String extension = getExtension(outputPath);
        Path parentDir = outputPath.getParent();

        // Create chunks
        List<List<FileASTData>> chunks = createChunks(allFiles, chunkSize);

        // Export index file
        ChunkedExportIndex index = new ChunkedExportIndex();
        index.setMetadata(exportData.getMetadata());
        index.setStatistics(exportData.getStatistics());
        index.setTotalChunks(chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            String chunkFileName = String.format("%s_chunk_%03d.%s", baseName, i, extension);
            index.addChunkFile(chunkFileName, chunks.get(i).size());
        }

        Path indexPath = parentDir.resolve(baseName + "_index.json");
        objectMapper.writeValue(indexPath.toFile(), index);

        // Export chunk files
        for (int i = 0; i < chunks.size(); i++) {
            ASTExportData chunkData = new ASTExportData();
            chunkData.setMetadata(exportData.getMetadata());
            chunkData.setFiles(chunks.get(i));

            String chunkFileName = String.format("%s_chunk_%03d.%s", baseName, i, extension);
            Path chunkPath = parentDir.resolve(chunkFileName);
            objectMapper.writeValue(chunkPath.toFile(), chunkData);
        }
    }

    private List<List<FileASTData>> createChunks(List<FileASTData> files, int chunkSize) {
        List<List<FileASTData>> chunks = new ArrayList<>();

        for (int i = 0; i < files.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, files.size());
            chunks.add(new ArrayList<>(files.subList(i, end)));
        }

        return chunks;
    }

    private String getBaseName(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "json";
    }

    // Helper class for chunked export
    private static class ChunkedExportIndex {
        private Map<String, Object> metadata;
        private Map<String, Object> statistics;
        private int totalChunks;
        private List<ChunkInfo> chunks = new ArrayList<>();

        public void addChunkFile(String fileName, int fileCount) {
            ChunkInfo chunk = new ChunkInfo();
            chunk.fileName = fileName;
            chunk.fileCount = fileCount;
            chunks.add(chunk);
        }

        // Getters and setters
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

        public Map<String, Object> getStatistics() { return statistics; }
        public void setStatistics(Map<String, Object> statistics) { this.statistics = statistics; }

        public int getTotalChunks() { return totalChunks; }
        public void setTotalChunks(int totalChunks) { this.totalChunks = totalChunks; }

        public List<ChunkInfo> getChunks() { return chunks; }
        public void setChunks(List<ChunkInfo> chunks) { this.chunks = chunks; }

        private static class ChunkInfo {
            public String fileName;
            public int fileCount;
        }
    }
}
```

## 3. Intermediate JSON Format Specification

### 3.1 Format Structure

```json
{
  "metadata": {
    "toolVersion": "1.0.0",
    "javaParserVersion": "3.27.1",
    "exportTime": "2024-11-09T10:30:00Z",
    "sourceDirectory": "/path/to/source",
    "totalProcessingTime": "45.2s"
  },
  "statistics": {
    "totalFiles": 150,
    "totalNodes": 25000,
    "totalSymbols": 3500,
    "averageNodesPerFile": 166.67,
    "largestFile": "com/example/BigClass.java",
    "mostComplexFile": "com/example/ComplexClass.java"
  },
  "files": [
    {
      "filePath": "/src/main/java/com/example/Calculator.java",
      "fileName": "Calculator.java",
      "fileSize": 1250,
      "lastModified": "2024-11-09T09:15:00Z",
      "sourceCode": "package com.example;\n\npublic class Calculator {\n...",
      "rootNode": {
        "nodeType": "CompilationUnit",
        "startLine": 1,
        "startColumn": 1,
        "endLine": 25,
        "endColumn": 1,
        "properties": {
          "packageName": "com.example"
        },
        "children": [
          {
            "nodeType": "ClassOrInterfaceDeclaration",
            "startLine": 3,
            "startColumn": 1,
            "endLine": 24,
            "endColumn": 1,
            "properties": {
              "name": "Calculator",
              "isInterface": false,
              "modifiers": "public"
            },
            "children": [
              {
                "nodeType": "MethodDeclaration",
                "startLine": 5,
                "startColumn": 5,
                "endLine": 7,
                "endColumn": 5,
                "properties": {
                  "name": "add",
                  "returnType": "int",
                  "modifiers": "public",
                  "parameterCount": 2
                },
                "children": [...]
              }
            ]
          }
        ]
      },
      "symbols": [
        {
          "type": "class",
          "name": "Calculator",
          "line": 3,
          "column": 14
        },
        {
          "type": "method",
          "name": "add",
          "line": 5,
          "column": 16
        }
      ]
    }
  ]
}
```

### 3.2 Optimization Features

**Compression**:

- Optional GZIP compression for large datasets
- JSON minification for production exports
- Selective field inclusion based on use case

**Chunking**:

- Automatic splitting for files > 1000 files
- Index file với chunk metadata
- Parallel loading support trong browser

## 4. Browser IndexedDB Integration

### 4.1 Database Schema Design

```javascript
// File: public/js/modules/ast/ASTDatabase.js
export class ASTDatabase {
  constructor() {
    this.dbName = "PCM_AST_Database";
    this.version = 1;
    this.db = null;
  }

  async initialize() {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, this.version);

      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.db = request.result;
        resolve(this.db);
      };

      request.onupgradeneeded = (event) => {
        const db = event.target.result;
        this.createObjectStores(db);
      };
    });
  }

  createObjectStores(db) {
    // Files store - main file data
    if (!db.objectStoreNames.contains("files")) {
      const filesStore = db.createObjectStore("files", { keyPath: "filePath" });
      filesStore.createIndex("fileName", "fileName", { unique: false });
      filesStore.createIndex("lastModified", "lastModified", { unique: false });
      filesStore.createIndex("fileSize", "fileSize", { unique: false });
    }

    // AST Nodes store - optimized for queries
    if (!db.objectStoreNames.contains("astNodes")) {
      const astStore = db.createObjectStore("astNodes", {
        keyPath: "id",
        autoIncrement: true,
      });
      astStore.createIndex("filePath", "filePath", { unique: false });
      astStore.createIndex("nodeType", "nodeType", { unique: false });
      astStore.createIndex("parentId", "parentId", { unique: false });
      astStore.createIndex("startLine", "startLine", { unique: false });
    }

    // Symbols store - for fast symbol lookup
    if (!db.objectStoreNames.contains("symbols")) {
      const symbolsStore = db.createObjectStore("symbols", {
        keyPath: "id",
        autoIncrement: true,
      });
      symbolsStore.createIndex("filePath", "filePath", { unique: false });
      symbolsStore.createIndex("type", "type", { unique: false });
      symbolsStore.createIndex("name", "name", { unique: false });
      symbolsStore.createIndex("typeAndName", ["type", "name"], {
        unique: false,
      });
    }

    // Metadata store - export info and statistics
    if (!db.objectStoreNames.contains("metadata")) {
      const metadataStore = db.createObjectStore("metadata", {
        keyPath: "key",
      });
    }

    // Search index store - for full-text search
    if (!db.objectStoreNames.contains("searchIndex")) {
      const searchStore = db.createObjectStore("searchIndex", {
        keyPath: "id",
        autoIncrement: true,
      });
      searchStore.createIndex("filePath", "filePath", { unique: false });
      searchStore.createIndex("content", "content", { unique: false });
      searchStore.createIndex("nodeType", "nodeType", { unique: false });
    }
  }
}
```

### 4.2 JSON Import Service

```javascript
// File: public/js/modules/ast/ASTImportService.js
export class ASTImportService {
  constructor(astDatabase) {
    this.db = astDatabase;
    this.batchSize = 100; // Batch size for performance
  }

  async importJSONData(jsonData, onProgress = null) {
    const startTime = performance.now();

    try {
      // Store metadata
      await this.storeMetadata(jsonData.metadata, jsonData.statistics);

      // Process files in batches
      const files = jsonData.files;
      const totalFiles = files.length;

      for (let i = 0; i < totalFiles; i += this.batchSize) {
        const batch = files.slice(i, Math.min(i + this.batchSize, totalFiles));
        await this.processBatch(batch);

        if (onProgress) {
          const progress = Math.min((i + this.batchSize) / totalFiles, 1.0);
          onProgress(progress, i + batch.length, totalFiles);
        }
      }

      const endTime = performance.now();
      console.log(`AST import completed in ${(endTime - startTime) / 1000}s`);

      return {
        success: true,
        filesProcessed: totalFiles,
        processingTime: endTime - startTime,
      };
    } catch (error) {
      console.error("AST import failed:", error);
      throw error;
    }
  }

  async importChunkedData(indexData, chunkLoader, onProgress = null) {
    const startTime = performance.now();

    try {
      // Store metadata from index
      await this.storeMetadata(indexData.metadata, indexData.statistics);

      const totalChunks = indexData.chunks.length;
      let processedFiles = 0;
      const totalFiles = indexData.statistics.totalFiles;

      // Process each chunk
      for (let i = 0; i < totalChunks; i++) {
        const chunkInfo = indexData.chunks[i];
        console.log(
          `Loading chunk ${i + 1}/${totalChunks}: ${chunkInfo.fileName}`,
        );

        const chunkData = await chunkLoader(chunkInfo.fileName);
        await this.importJSONData(chunkData, null); // No progress for individual chunks

        processedFiles += chunkInfo.fileCount;

        if (onProgress) {
          const progress = processedFiles / totalFiles;
          onProgress(progress, processedFiles, totalFiles);
        }
      }

      const endTime = performance.now();
      console.log(
        `Chunked AST import completed in ${(endTime - startTime) / 1000}s`,
      );

      return {
        success: true,
        filesProcessed: processedFiles,
        chunksProcessed: totalChunks,
        processingTime: endTime - startTime,
      };
    } catch (error) {
      console.error("Chunked AST import failed:", error);
      throw error;
    }
  }

  async processBatch(files) {
    const transaction = this.db.db.transaction(
      ["files", "astNodes", "symbols", "searchIndex"],
      "readwrite",
    );

    const filesStore = transaction.objectStore("files");
    const astStore = transaction.objectStore("astNodes");
    const symbolsStore = transaction.objectStore("symbols");
    const searchStore = transaction.objectStore("searchIndex");

    const promises = files.map(async (fileData) => {
      // Store file metadata
      const fileRecord = {
        filePath: fileData.filePath,
        fileName: fileData.fileName,
        fileSize: fileData.fileSize,
        lastModified: fileData.lastModified,
        sourceCode: fileData.sourceCode,
      };
      filesStore.put(fileRecord);

      // Process AST nodes
      await this.processASTNode(
        fileData.rootNode,
        fileData.filePath,
        null,
        astStore,
        searchStore,
      );

      // Store symbols
      fileData.symbols.forEach((symbol) => {
        const symbolRecord = {
          filePath: fileData.filePath,
          type: symbol.type,
          name: symbol.name,
          line: symbol.line,
          column: symbol.column,
        };
        symbolsStore.put(symbolRecord);
      });
    });

    await Promise.all(promises);

    return new Promise((resolve, reject) => {
      transaction.oncomplete = () => resolve();
      transaction.onerror = () => reject(transaction.error);
    });
  }

  async processASTNode(
    node,
    filePath,
    parentId,
    astStore,
    searchStore,
    nodeId = null,
  ) {
    // Generate unique ID for this node
    const currentNodeId = nodeId || this.generateNodeId();

    // Create AST node record
    const astRecord = {
      id: currentNodeId,
      filePath: filePath,
      nodeType: node.nodeType,
      startLine: node.startLine,
      startColumn: node.startColumn,
      endLine: node.endLine,
      endColumn: node.endColumn,
      text: node.text,
      properties: node.properties,
      parentId: parentId,
      hasChildren: node.children && node.children.length > 0,
    };

    astStore.put(astRecord);

    // Create search index entry
    if (node.text || node.properties) {
      const searchContent = this.buildSearchContent(node);
      const searchRecord = {
        filePath: filePath,
        nodeId: currentNodeId,
        nodeType: node.nodeType,
        content: searchContent,
        startLine: node.startLine,
      };
      searchStore.put(searchRecord);
    }

    // Process children
    if (node.children) {
      for (let i = 0; i < node.children.length; i++) {
        const childNodeId = `${currentNodeId}_${i}`;
        await this.processASTNode(
          node.children[i],
          filePath,
          currentNodeId,
          astStore,
          searchStore,
          childNodeId,
        );
      }
    }
  }

  buildSearchContent(node) {
    const content = [];

    if (node.text) {
      content.push(node.text);
    }

    if (node.properties) {
      Object.values(node.properties).forEach((value) => {
        if (typeof value === "string") {
          content.push(value);
        }
      });
    }

    return content.join(" ").toLowerCase();
  }

  generateNodeId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  async storeMetadata(metadata, statistics) {
    const transaction = this.db.db.transaction(["metadata"], "readwrite");
    const metadataStore = transaction.objectStore("metadata");

    metadataStore.put({ key: "exportMetadata", value: metadata });
    metadataStore.put({ key: "statistics", value: statistics });
    metadataStore.put({ key: "importTime", value: new Date().toISOString() });

    return new Promise((resolve, reject) => {
      transaction.oncomplete = () => resolve();
      transaction.onerror = () => reject(transaction.error);
    });
  }
}
```

### 4.3 Query Service

```javascript
// File: public/js/modules/ast/ASTQueryService.js
export class ASTQueryService {
  constructor(astDatabase) {
    this.db = astDatabase;
  }

  async findFilesByName(fileName) {
    const transaction = this.db.db.transaction(["files"], "readonly");
    const store = transaction.objectStore("files");
    const index = store.index("fileName");

    return new Promise((resolve, reject) => {
      const request = index.getAll(fileName);
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  async findNodesByType(nodeType, filePath = null) {
    const transaction = this.db.db.transaction(["astNodes"], "readonly");
    const store = transaction.objectStore("astNodes");
    const results = [];

    const query = filePath
      ? IDBKeyRange.only([nodeType, filePath])
      : IDBKeyRange.bound([nodeType], [nodeType, "\uffff"]);

    return new Promise((resolve, reject) => {
      const request = store.index("nodeType").openCursor(query);

      request.onsuccess = (event) => {
        const cursor = event.target.result;
        if (cursor) {
          if (!filePath || cursor.value.filePath === filePath) {
            results.push(cursor.value);
          }
          cursor.continue();
        } else {
          resolve(results);
        }
      };

      request.onerror = () => reject(request.error);
    });
  }

  async findSymbolsByName(symbolName, symbolType = null) {
    const transaction = this.db.db.transaction(["symbols"], "readonly");
    const store = transaction.objectStore("symbols");
    const results = [];

    const indexName = symbolType ? "typeAndName" : "name";
    const query = symbolType
      ? IDBKeyRange.only([symbolType, symbolName])
      : IDBKeyRange.only(symbolName);

    return new Promise((resolve, reject) => {
      const request = store.index(indexName).openCursor(query);

      request.onsuccess = (event) => {
        const cursor = event.target.result;
        if (cursor) {
          results.push(cursor.value);
          cursor.continue();
        } else {
          resolve(results);
        }
      };

      request.onerror = () => reject(request.error);
    });
  }

  async searchContent(searchTerm) {
    const transaction = this.db.db.transaction(["searchIndex"], "readonly");
    const store = transaction.objectStore("searchIndex");
    const results = [];
    const lowerSearchTerm = searchTerm.toLowerCase();

    return new Promise((resolve, reject) => {
      const request = store.openCursor();

      request.onsuccess = (event) => {
        const cursor = event.target.result;
        if (cursor) {
          const record = cursor.value;
          if (record.content && record.content.includes(lowerSearchTerm)) {
            results.push(record);
          }
          cursor.continue();
        } else {
          resolve(results);
        }
      };

      request.onerror = () => reject(request.error);
    });
  }

  async getFileAST(filePath) {
    // Get file metadata
    const fileData = await this.getFileData(filePath);
    if (!fileData) return null;

    // Get root nodes for the file
    const rootNodes = await this.findNodesByType("CompilationUnit", filePath);
    if (rootNodes.length === 0) return null;

    // Rebuild AST tree structure
    const rootNode = await this.buildASTTree(rootNodes[0]);

    return {
      fileData,
      ast: rootNode,
    };
  }

  async getFileData(filePath) {
    const transaction = this.db.db.transaction(["files"], "readonly");
    const store = transaction.objectStore("files");

    return new Promise((resolve, reject) => {
      const request = store.get(filePath);
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  async buildASTTree(node) {
    // Get children nodes
    const children = await this.getChildNodes(node.id);

    // Build tree recursively
    const childTrees = await Promise.all(
      children.map((child) => this.buildASTTree(child)),
    );

    return {
      ...node,
      children: childTrees,
    };
  }

  async getChildNodes(parentId) {
    const transaction = this.db.db.transaction(["astNodes"], "readonly");
    const store = transaction.objectStore("astNodes");
    const results = [];

    return new Promise((resolve, reject) => {
      const request = store
        .index("parentId")
        .openCursor(IDBKeyRange.only(parentId));

      request.onsuccess = (event) => {
        const cursor = event.target.result;
        if (cursor) {
          results.push(cursor.value);
          cursor.continue();
        } else {
          resolve(results.sort((a, b) => a.id.localeCompare(b.id)));
        }
      };

      request.onerror = () => reject(request.error);
    });
  }

  async getStatistics() {
    const transaction = this.db.db.transaction(["metadata"], "readonly");
    const store = transaction.objectStore("metadata");

    return new Promise((resolve, reject) => {
      const request = store.get("statistics");
      request.onsuccess = () => resolve(request.result?.value);
      request.onerror = () => reject(request.error);
    });
  }
}
```

## 5. Performance Optimizations

### 5.1 Java Tool Optimizations

**Memory Management**:

```java
// Use streaming for large files
private static final int MAX_MEMORY_USAGE = 512 * 1024 * 1024; // 512MB
private MemoryMonitor memoryMonitor = new MemoryMonitor(MAX_MEMORY_USAGE);

// Process files in batches when memory usage is high
if (memoryMonitor.isMemoryHigh()) {
    System.gc(); // Suggest garbage collection
    Thread.sleep(100); // Brief pause
}
```

**Parallel Processing**:

```java
// Parallel file processing for large codebases
ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
try {
    customThreadPool.submit(() ->
        javaFiles.parallelStream().forEach(this::processFile)
    ).get();
} finally {
    customThreadPool.shutdown();
}
```

### 5.2 IndexedDB Optimizations

**Transaction Batching**:

- Batch multiple operations trong single transaction
- Sử dụng readwrite transactions cho bulk operations
- Avoid long-running transactions

**Index Strategy**:

- Compound indexes cho multi-field queries
- Selective indexing dựa trên query patterns
- Regular index maintenance

**Memory Management**:

- Cursor-based iteration cho large result sets
- Lazy loading cho AST tree reconstruction
- Cleanup unused data periodically

## 6. Usage Examples

### 6.1 Running the Java Tool

```bash
# Basic usage
java -jar java-ast-export-tool-1.0.0.jar /path/to/source -o ast-export.json

# Advanced usage with options
java -jar java-ast-export-tool-1.0.0.jar \
  /path/to/source \
  --output /output/ast-export.json \
  --format readable \
  --chunk-size 500 \
  --exclude "**/test/**,**/target/**" \
  --verbose

# Large project with memory optimization
java -Xmx2G -jar java-ast-export-tool-1.0.0.jar \
  /large/project \
  --output /output/large-project-ast.json \
  --chunk-size 100 \
  --format compact
```

### 6.2 Browser Integration

```javascript
// Import AST data vào PCM WebApp
import { ASTDatabase } from "./modules/ast/ASTDatabase.js";
import { ASTImportService } from "./modules/ast/ASTImportService.js";
import { ASTQueryService } from "./modules/ast/ASTQueryService.js";

class PCMASTManager {
  async initialize() {
    this.astDB = new ASTDatabase();
    await this.astDB.initialize();

    this.importService = new ASTImportService(this.astDB);
    this.queryService = new ASTQueryService(this.astDB);
  }

  async importASTData(file) {
    const reader = new FileReader();
    return new Promise((resolve, reject) => {
      reader.onload = async (e) => {
        try {
          const jsonData = JSON.parse(e.target.result);
          const result = await this.importService.importJSONData(
            jsonData,
            (progress, current, total) => {
              console.log(
                `Import progress: ${(progress * 100).toFixed(1)}% (${current}/${total})`,
              );
            },
          );
          resolve(result);
        } catch (error) {
          reject(error);
        }
      };
      reader.readAsText(file);
    });
  }

  async findClasses(className) {
    return await this.queryService.findSymbolsByName(className, "class");
  }

  async findMethods(methodName) {
    return await this.queryService.findSymbolsByName(methodName, "method");
  }

  async searchCode(searchTerm) {
    return await this.queryService.searchContent(searchTerm);
  }

  async getFileAST(filePath) {
    return await this.queryService.getFileAST(filePath);
  }
}

// Usage trong PCM WebApp
const astManager = new PCMASTManager();
await astManager.initialize();

// Import AST data từ exported JSON
const fileInput = document.querySelector("#ast-file-input");
fileInput.addEventListener("change", async (e) => {
  const file = e.target.files[0];
  if (file) {
    try {
      const result = await astManager.importASTData(file);
      console.log("AST import successful:", result);
    } catch (error) {
      console.error("AST import failed:", error);
    }
  }
});
```

## 7. Lợi Ích và Hạn Chế

### 7.1 Lợi Ích

**Accuracy & Completeness**:

- Full semantic analysis với JavaParser
- Complete symbol resolution
- Detailed type information
- Accurate source positions

**Performance**:

- Offline processing không ảnh hưởng browser performance
- Pre-computed AST trees
- Indexed queries cho fast retrieval
- Cached results trong IndexedDB

**Scalability**:

- Handle large codebases (10,000+ files)
- Chunked processing cho memory efficiency
- Batch imports cho performance
- Parallel processing support

**Integration**:

- Native IndexedDB integration
- Compatible với existing PCM WebApp
- Standard JSON format
- Cross-platform Java tool

### 7.2 Hạn Chế

**Workflow Complexity**:

- Requires separate Java tool execution
- Two-step process (export + import)
- Manual synchronization needed
- Version management overhead

**Storage Requirements**:

- Large JSON files cho big projects
- IndexedDB storage limits
- Duplicate data storage (source + AST)
- Memory usage trong browser

**Maintenance**:

- Java tool updates needed
- Dependency management
- Export format versioning
- Database schema evolution

## 8. Kết Luận

Phương án Java AST Export Tool cung cấp giải pháp robust và accurate cho việc tích hợp AST analysis vào PCM WebApp. Mặc
dù có complexity cao hơn so với tree-sitter approach, nó mang lại:

- **Độ chính xác tối đa** với full semantic analysis
- **Scalability excellent** cho large codebases
- **Performance tối ưu** với pre-computed data
- **Integration flexibility** với existing systems

Phương án này phù hợp nhất cho:

- Large enterprise codebases
- Projects yêu cầu deep semantic analysis
- Scenarios cần offline processing capability
- Teams có Java development expertise

---

_Tài liệu được tạo cho dự án PCM WebApp - Java AST Export Tool Solution_  
_Ngày tạo: 2024-11-09_  
_Phiên bản: 1.0_
