# CHM Integration - Detailed Design Specification

**Tạo từ các file nguồn:**

- `docs/RaD/ideas/chm-ingestion.md`

**Phiên bản:** 1.0  
**Ngày tạo:** 2025-11-15

---

## 1. Tổng quan

Tài liệu này mô tả chi tiết quy trình tích hợp Microsoft Compiled HTML Help (CHM) files vào hệ thống PCM Desktop. CHM
files thường chứa legacy documentation cần được index và search như các knowledge sources khác.

### 1.1 Lý do tích hợp CHM

- ✅ Legacy documentation thường được phân phối dưới dạng CHM
- ✅ CHM chứa TOC (Table of Contents) và structured HTML pages
- ✅ Có thể map trực tiếp vào chunking/indexing pipeline
- ✅ Sau khi extract, xử lý như document thông thường (SQLite + embeddings + FTS5)

### 1.2 Workflow tổng quan

```
User uploads CHM
    ↓
Extract to HTML
    ↓
Parse TOC & pages
    ↓
Normalize content
    ↓
Store in chm_documents
    ↓
Index in search_corpus + FTS5
    ↓
Chunk & embed to Qdrant
    ↓
Ready for RAG retrieval
```

---

## 2. Extraction Tools

### 2.1 Tool Comparison

| Tool                | Platform    | Pros                        | Cons              |
|---------------------|-------------|-----------------------------|-------------------|
| `hh.exe -decompile` | Windows     | Native, reliable            | Windows-only      |
| `extract_chmLib`    | Linux/macOS | Open-source, cross-platform | Needs compilation |
| `libmspack`         | Cross       | Well-maintained             | Complex API       |
| `pychm`             | Cross       | Python bindings, easy       | Requires Python   |
| `CHMLib` Java       | JVM         | Pure Java, portable         | Less maintained   |

**Recommendation:** Use `extract_chmLib` (Linux/macOS) or `hh.exe` (Windows), wrapped in Java `ProcessBuilder`.

### 2.2 Java Wrapper Implementation

```java
public class ChmExtractor {
    public ExtractionResult extract(Path chmFile, Path outputDir) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return extractWindows(chmFile, outputDir);
        } else {
            return extractUnix(chmFile, outputDir);
        }
    }
    
    private ExtractionResult extractWindows(Path chmFile, Path outputDir) throws IOException {
        // Use hh.exe
        ProcessBuilder pb = new ProcessBuilder(
            "hh.exe",
            "-decompile",
            outputDir.toString(),
            chmFile.toString()
        );
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new IOException("CHM extraction failed with code: " + exitCode);
        }
        
        return new ExtractionResult(outputDir, countExtractedFiles(outputDir));
    }
    
    private ExtractionResult extractUnix(Path chmFile, Path outputDir) throws IOException {
        // Use extract_chmLib
        ProcessBuilder pb = new ProcessBuilder(
            "extract_chmLib",
            chmFile.toString(),
            outputDir.toString()
        );
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            // Fallback to Python pychm
            return extractPython(chmFile, outputDir);
        }
        
        return new ExtractionResult(outputDir, countExtractedFiles(outputDir));
    }
    
    private ExtractionResult extractPython(Path chmFile, Path outputDir) throws IOException {
        // Python script using pychm
        String script = String.format("""
            import chm.chm
            import os
            
            chm_file = chm.chm.CHMFile()
            chm_file.LoadCHM('%s')
            
            for item in chm_file.GetTopicsTree():
                path = item.path
                content = chm_file.RetrieveObject(path)
                if content:
                    output_path = os.path.join('%s', path.lstrip('/'))
                    os.makedirs(os.path.dirname(output_path), exist_ok=True)
                    with open(output_path, 'wb') as f:
                        f.write(content)
            """, chmFile, outputDir);
        
        Files.write(Paths.get("/tmp/extract_chm.py"), script.getBytes());
        
        ProcessBuilder pb = new ProcessBuilder("python3", "/tmp/extract_chm.py");
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new IOException("Python CHM extraction failed");
        }
        
        return new ExtractionResult(outputDir, countExtractedFiles(outputDir));
    }
}
```

---

## 3. Data Model

### 3.1 CHM Imports Table

```sql
CREATE TABLE chm_imports (
    import_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    system_id      INTEGER REFERENCES systems(system_id) ON DELETE SET NULL,
    subsystem_id   INTEGER REFERENCES subsystems(subsystem_id) ON DELETE SET NULL,
    project_id     INTEGER REFERENCES projects(project_id) ON DELETE SET NULL,
    chm_path       TEXT NOT NULL,
    chm_checksum   TEXT,
    extracted_path TEXT,
    status         TEXT DEFAULT 'pending',
    notes          TEXT,
    imported_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chm_imports_project ON chm_imports(project_id);
CREATE INDEX idx_chm_imports_status ON chm_imports(status);
```

**Status values:**

- `pending`: Awaiting processing
- `extracting`: Currently extracting
- `parsing`: Parsing HTML/TOC
- `indexing`: Creating embeddings/FTS
- `complete`: Successfully imported
- `failed`: Error occurred

### 3.2 CHM Documents Table

```sql
CREATE TABLE chm_documents (
    document_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    import_id      INTEGER NOT NULL REFERENCES chm_imports(import_id) ON DELETE CASCADE,
    relative_path  TEXT NOT NULL,
    title          TEXT,
    toc_path       TEXT,
    order_index    INTEGER,
    content        TEXT NOT NULL,
    checksum       TEXT,
    metadata       TEXT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_chm_documents_path ON chm_documents(import_id, relative_path);
CREATE INDEX idx_chm_documents_import ON chm_documents(import_id);
```

**Metadata JSON structure:**

```json
{
  "encoding": "UTF-8",
  "keywords": ["security", "authentication"],
  "author": "Tech Team",
  "last_updated": "2024-01-15",
  "has_images": true,
  "word_count": 1523,
  "headings": ["Introduction", "Installation", "Configuration"]
}
```

### 3.3 CHM Assets Table

```sql
CREATE TABLE chm_assets (
    asset_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    import_id      INTEGER NOT NULL REFERENCES chm_imports(import_id) ON DELETE CASCADE,
    relative_path  TEXT NOT NULL,
    mime_type      TEXT,
    size_bytes     INTEGER,
    checksum       TEXT
);

CREATE UNIQUE INDEX uq_chm_assets_path ON chm_assets(import_id, relative_path);
CREATE INDEX idx_chm_assets_import ON chm_assets(import_id);
```

---

## 4. Import Pipeline

### 4.1 Service Implementation

```java
@Service
public class ChmImportService {
    
    public ChmImport startImport(
        Path chmFile,
        Integer projectId,
        String notes
    ) {
        // 1. Create import record
        ChmImport chmImport = new ChmImport();
        chmImport.setProjectId(projectId);
        chmImport.setChmPath(chmFile.toString());
        chmImport.setStatus("pending");
        chmImport.setNotes(notes);
        chmImport.setChmChecksum(computeChecksum(chmFile));
        chmImportRepo.save(chmImport);
        
        // 2. Start async processing
        CompletableFuture.runAsync(() -> {
            try {
                processImport(chmImport);
            } catch (Exception e) {
                handleImportError(chmImport, e);
            }
        });
        
        return chmImport;
    }
    
    private void processImport(ChmImport chmImport) {
        try {
            // Update status
            chmImport.setStatus("extracting");
            chmImportRepo.save(chmImport);
            
            // Extract CHM
            Path outputDir = Files.createTempDirectory("chm_extract_");
            chmImport.setExtractedPath(outputDir.toString());
            chmImportRepo.save(chmImport);
            
            ExtractionResult extraction = chmExtractor.extract(
                Paths.get(chmImport.getChmPath()),
                outputDir
            );
            
            // Parse TOC
            chmImport.setStatus("parsing");
            chmImportRepo.save(chmImport);
            
            TocStructure toc = parseToc(outputDir);
            
            // Process each document
            int orderIndex = 0;
            for (TocEntry entry : toc.getAllEntries()) {
                processDocument(chmImport, entry, orderIndex++);
            }
            
            // Process assets
            processAssets(chmImport, outputDir);
            
            // Index for search
            chmImport.setStatus("indexing");
            chmImportRepo.save(chmImport);
            
            indexDocuments(chmImport);
            
            // Complete
            chmImport.setStatus("complete");
            chmImportRepo.save(chmImport);
            
        } catch (Exception e) {
            handleImportError(chmImport, e);
        }
    }
    
    private void processDocument(
        ChmImport chmImport,
        TocEntry entry,
        int orderIndex
    ) {
        try {
            Path htmlFile = Paths.get(chmImport.getExtractedPath())
                .resolve(entry.getPath());
            
            if (!Files.exists(htmlFile)) {
                log.warn("File not found: {}", htmlFile);
                return;
            }
            
            // Parse HTML
            String html = Files.readString(htmlFile);
            Document doc = Jsoup.parse(html);
            
            // Extract title
            String title = extractTitle(doc, entry);
            
            // Convert to plain text (strip HTML)
            String content = doc.body().text();
            
            // Extract metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("encoding", doc.charset().name());
            metadata.put("keywords", extractKeywords(doc));
            metadata.put("headings", extractHeadings(doc));
            metadata.put("has_images", !doc.select("img").isEmpty());
            metadata.put("word_count", content.split("\\s+").length);
            
            // Create document record
            ChmDocument chmDoc = new ChmDocument();
            chmDoc.setImportId(chmImport.getImportId());
            chmDoc.setRelativePath(entry.getPath());
            chmDoc.setTitle(title);
            chmDoc.setTocPath(entry.getTocPath());
            chmDoc.setOrderIndex(orderIndex);
            chmDoc.setContent(content);
            chmDoc.setChecksum(computeChecksum(content));
            chmDoc.setMetadata(toJson(metadata));
            chmDocumentRepo.save(chmDoc);
            
        } catch (Exception e) {
            log.error("Failed to process document: {}", entry.getPath(), e);
        }
    }
    
    private void processAssets(ChmImport chmImport, Path extractedDir) {
        try {
            Files.walk(extractedDir)
                .filter(Files::isRegularFile)
                .filter(p -> isAsset(p))
                .forEach(assetPath -> {
                    try {
                        String relativePath = extractedDir.relativize(assetPath).toString();
                        
                        ChmAsset asset = new ChmAsset();
                        asset.setImportId(chmImport.getImportId());
                        asset.setRelativePath(relativePath);
                        asset.setMimeType(detectMimeType(assetPath));
                        asset.setSizeBytes(Files.size(assetPath));
                        asset.setChecksum(computeChecksum(assetPath));
                        chmAssetRepo.save(asset);
                        
                    } catch (Exception e) {
                        log.error("Failed to process asset: {}", assetPath, e);
                    }
                });
        } catch (IOException e) {
            log.error("Failed to walk assets directory", e);
        }
    }
    
    private boolean isAsset(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".png") ||
               name.endsWith(".jpg") ||
               name.endsWith(".jpeg") ||
               name.endsWith(".gif") ||
               name.endsWith(".css") ||
               name.endsWith(".js");
    }
}
```

### 4.2 TOC Parser

```java
public class TocParser {
    public TocStructure parseToc(Path extractedDir) {
        // Look for .hhc file (TOC) or .hhk file (Index)
        Path hhcFile = findFile(extractedDir, "*.hhc");
        
        if (hhcFile == null) {
            log.warn("No TOC file found, using file listing");
            return buildTocFromFiles(extractedDir);
        }
        
        try {
            String content = Files.readString(hhcFile);
            Document doc = Jsoup.parse(content);
            
            TocStructure toc = new TocStructure();
            parseHhcNode(doc.body(), toc.getRoot(), "");
            
            return toc;
            
        } catch (Exception e) {
            log.error("Failed to parse TOC", e);
            return buildTocFromFiles(extractedDir);
        }
    }
    
    private void parseHhcNode(Element element, TocNode parent, String pathPrefix) {
        // CHM TOC format:
        // <ul>
        //   <li>
        //     <object type="text/sitemap">
        //       <param name="Name" value="Chapter Title">
        //       <param name="Local" value="chapter.html">
        //     </object>
        //     <ul> ... nested items ... </ul>
        //   </li>
        // </ul>
        
        for (Element li : element.select("> ul > li")) {
            Element object = li.selectFirst("object[type=text/sitemap]");
            if (object == null) continue;
            
            String name = null;
            String local = null;
            
            for (Element param : object.select("param")) {
                String paramName = param.attr("name");
                String value = param.attr("value");
                
                if ("Name".equalsIgnoreCase(paramName)) {
                    name = value;
                } else if ("Local".equalsIgnoreCase(paramName)) {
                    local = value;
                }
            }
            
            if (name != null) {
                String newPath = pathPrefix.isEmpty() ? name : pathPrefix + " > " + name;
                TocNode node = new TocNode(name, local, newPath);
                parent.addChild(node);
                
                // Recurse for nested items
                Element ul = li.selectFirst("ul");
                if (ul != null) {
                    parseHhcNode(li, node, newPath);
                }
            }
        }
    }
    
    private TocStructure buildTocFromFiles(Path extractedDir) {
        // Fallback: build TOC from file structure
        TocStructure toc = new TocStructure();
        
        try {
            Files.walk(extractedDir)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".html") || p.toString().endsWith(".htm"))
                .forEach(htmlFile -> {
                    String relativePath = extractedDir.relativize(htmlFile).toString();
                    String name = htmlFile.getFileName().toString()
                        .replaceFirst("\\.[^.]+$", ""); // Remove extension
                    
                    TocNode node = new TocNode(name, relativePath, name);
                    toc.getRoot().addChild(node);
                });
        } catch (IOException e) {
            log.error("Failed to build TOC from files", e);
        }
        
        return toc;
    }
}
```

---

## 5. Indexing Pipeline

### 5.1 Feed to Search Corpus

```java
public class ChmIndexingService {
    
    public void indexDocuments(ChmImport chmImport) {
        List<ChmDocument> documents = chmDocumentRepo.findByImportId(
            chmImport.getImportId()
        );
        
        for (ChmDocument doc : documents) {
            try {
                // Insert into search_corpus
                SearchCorpus corpus = new SearchCorpus();
                corpus.setProjectId(chmImport.getProjectId());
                corpus.setSourceType("chm_doc");
                corpus.setLabel(doc.getTitle());
                corpus.setContent(doc.getContent());
                corpus.setChecksum(doc.getChecksum());
                searchCorpusRepo.save(corpus);
                
                // FTS5 will be auto-updated by trigger
                
                // Chunk and embed
                List<Chunk> chunks = chunkDocument(doc);
                for (Chunk chunk : chunks) {
                    embedAndIndex(chunk, corpus.getCorpusId());
                }
                
            } catch (Exception e) {
                log.error("Failed to index document: {}", doc.getDocumentId(), e);
            }
        }
    }
    
    private List<Chunk> chunkDocument(ChmDocument doc) {
        // Split by headings or fixed token count
        List<Chunk> chunks = new ArrayList<>();
        
        String[] paragraphs = doc.getContent().split("\n\n+");
        StringBuilder currentChunk = new StringBuilder();
        int tokenCount = 0;
        
        for (String para : paragraphs) {
            int paraTokens = estimateTokens(para);
            
            if (tokenCount + paraTokens > 500 && tokenCount > 0) {
                // Create chunk
                Chunk chunk = new Chunk();
                chunk.setContent(currentChunk.toString());
                chunk.setMetadata(Map.of(
                    "document_id", doc.getDocumentId(),
                    "title", doc.getTitle(),
                    "toc_path", doc.getTocPath(),
                    "source_type", "chm_doc"
                ));
                chunks.add(chunk);
                
                // Reset
                currentChunk = new StringBuilder();
                tokenCount = 0;
            }
            
            currentChunk.append(para).append("\n\n");
            tokenCount += paraTokens;
        }
        
        // Last chunk
        if (tokenCount > 0) {
            Chunk chunk = new Chunk();
            chunk.setContent(currentChunk.toString());
            chunk.setMetadata(Map.of(
                "document_id", doc.getDocumentId(),
                "title", doc.getTitle(),
                "toc_path", doc.getTocPath(),
                "source_type", "chm_doc"
            ));
            chunks.add(chunk);
        }
        
        return chunks;
    }
    
    private void embedAndIndex(Chunk chunk, long corpusId) {
        // Generate embedding
        float[] embedding = embeddingService.generate(chunk.getContent());
        
        // Store in vector_documents
        VectorDocument vectorDoc = new VectorDocument();
        vectorDoc.setProjectId(chunk.getMetadata().get("project_id"));
        vectorDoc.setChunkId(UUID.randomUUID().toString());
        vectorDoc.setContentType("chm_doc");
        vectorDoc.setMetadata(toJson(chunk.getMetadata()));
        vectorDoc.setQdrantPoint(vectorDoc.getChunkId());
        vectorDocumentRepo.save(vectorDoc);
        
        // Upsert to Qdrant
        qdrantClient.upsert(
            "pcm_chunks",
            vectorDoc.getChunkId(),
            embedding,
            chunk.getMetadata()
        );
    }
}
```

---

## 6. UI Integration

### 6.1 Import Dialog

```java
public class ChmImportDialog extends Dialog<ChmImport> {
    private final TextField chmFileField = new TextField();
    private final Button browseButton = new Button("Browse...");
    private final ComboBox<Project> projectCombo = new ComboBox<>();
    private final TextArea notesArea = new TextArea();
    private final ProgressIndicator progress = new ProgressIndicator();
    private final Label statusLabel = new Label();
    
    public ChmImportDialog() {
        setTitle("Import CHM Documentation");
        
        // Layout
        GridPane grid = new GridPane();
        grid.add(new Label("CHM File:"), 0, 0);
        grid.add(chmFileField, 1, 0);
        grid.add(browseButton, 2, 0);
        
        grid.add(new Label("Project:"), 0, 1);
        grid.add(projectCombo, 1, 1, 2, 1);
        
        grid.add(new Label("Notes:"), 0, 2);
        grid.add(notesArea, 1, 2, 2, 1);
        
        grid.add(progress, 0, 3, 3, 1);
        grid.add(statusLabel, 0, 4, 3, 1);
        
        getDialogPane().setContent(grid);
        
        // Browse button
        browseButton.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                new ExtensionFilter("CHM Files", "*.chm")
            );
            File file = fc.showOpenDialog(getOwner());
            if (file != null) {
                chmFileField.setText(file.getAbsolutePath());
            }
        });
        
        // Load projects
        projectCombo.setItems(FXCollections.observableArrayList(
            projectService.getAllProjects()
        ));
        
        // Buttons
        ButtonType importButton = new ButtonType("Import", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(importButton, ButtonType.CANCEL);
        
        // Result converter
        setResultConverter(button -> {
            if (button == importButton) {
                return startImport();
            }
            return null;
        });
        
        progress.setVisible(false);
    }
    
    private ChmImport startImport() {
        Path chmFile = Paths.get(chmFileField.getText());
        Project project = projectCombo.getValue();
        String notes = notesArea.getText();
        
        progress.setVisible(true);
        statusLabel.setText("Starting import...");
        
        ChmImport chmImport = chmImportService.startImport(
            chmFile,
            project.getProjectId(),
            notes
        );
        
        // Monitor progress
        monitorImport(chmImport);
        
        return chmImport;
    }
    
    private void monitorImport(ChmImport chmImport) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    ChmImport updated = chmImportRepo.findById(chmImport.getImportId());
                    
                    Platform.runLater(() -> {
                        statusLabel.setText("Status: " + updated.getStatus());
                    });
                    
                    if ("complete".equals(updated.getStatus()) ||
                        "failed".equals(updated.getStatus())) {
                        break;
                    }
                    
                    Thread.sleep(1000);
                }
                return null;
            }
        };
        
        new Thread(task).start();
    }
}
```

### 6.2 CHM Browser Page

```java
public class ChmBrowserPage extends VBox {
    private final TableView<ChmImport> importsTable = new TableView<>();
    private final TreeView<TocNode> tocTree = new TreeView<>();
    private final WebView contentView = new WebView();
    
    public ChmBrowserPage() {
        // Imports table
        TableColumn<ChmImport, String> pathCol = new TableColumn<>("CHM File");
        pathCol.setCellValueFactory(new PropertyValueFactory<>("chmPath"));
        
        TableColumn<ChmImport, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<ChmImport, LocalDateTime> dateCol = new TableColumn<>("Imported");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("importedAt"));
        
        importsTable.getColumns().addAll(pathCol, statusCol, dateCol);
        
        // TOC tree
        tocTree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(TocNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        
        // Selection handler
        importsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                loadToc(selected);
            }
        });
        
        tocTree.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null && selected.getValue() != null) {
                loadDocument(selected.getValue());
            }
        });
        
        // Layout
        SplitPane mainSplit = new SplitPane();
        mainSplit.getItems().addAll(
            new VBox(new Label("Imports"), importsTable),
            new SplitPane(
                new VBox(new Label("TOC"), tocTree),
                new VBox(new Label("Content"), contentView)
            )
        );
        
        getChildren().add(mainSplit);
        
        // Load data
        refreshImports();
    }
    
    private void refreshImports() {
        List<ChmImport> imports = chmImportService.getAllImports();
        importsTable.setItems(FXCollections.observableArrayList(imports));
    }
    
    private void loadToc(ChmImport chmImport) {
        TocStructure toc = chmImportService.getTocStructure(chmImport);
        TreeItem<TocNode> root = buildTreeItem(toc.getRoot());
        tocTree.setRoot(root);
        tocTree.setShowRoot(false);
    }
    
    private TreeItem<TocNode> buildTreeItem(TocNode node) {
        TreeItem<TocNode> item = new TreeItem<>(node);
        for (TocNode child : node.getChildren()) {
            item.getChildren().add(buildTreeItem(child));
        }
        return item;
    }
    
    private void loadDocument(TocNode node) {
        if (node.getPath() != null) {
            ChmDocument doc = chmDocumentRepo.findByPath(node.getPath());
            if (doc != null) {
                contentView.getEngine().loadContent(formatDocument(doc));
            }
        }
    }
    
    private String formatDocument(ChmDocument doc) {
        return String.format("""
            <html>
            <head>
                <title>%s</title>
                <style>
                    body { font-family: Arial, sans-serif; padding: 20px; }
                    h1 { color: #333; }
                    pre { background: #f4f4f4; padding: 10px; }
                </style>
            </head>
            <body>
                <h1>%s</h1>
                <p><small>%s</small></p>
                <hr>
                <div>%s</div>
            </body>
            </html>
            """,
            doc.getTitle(),
            doc.getTitle(),
            doc.getTocPath(),
            formatContent(doc.getContent())
        );
    }
}
```

---

## 7. Best Practices

### 7.1 Security

✅ **Sanitize HTML** before rendering in UI  
✅ **Validate CHM paths** before extraction  
✅ **Scan for malware** in extracted files  
❌ **Never execute** scripts from CHM  
❌ **Don't trust** CHM metadata

### 7.2 Performance

✅ **Extract CHM asynchronously** (don't block UI)  
✅ **Batch insert** documents (500-1000 per transaction)  
✅ **Cache extracted files** for re-import  
✅ **Index incrementally** (detect unchanged checksums)  
❌ **Don't re-extract** identical CHM files

### 7.3 Quality

✅ **Preserve TOC structure** in metadata  
✅ **Extract images** and store references  
✅ **Handle encoding** properly (UTF-8, Latin1, etc.)  
✅ **Log errors** per document (don't fail entire import)  
❌ **Don't skip** broken pages (log and continue)

---

## 8. Troubleshooting

### 8.1 Common Issues

**Issue: Extraction fails on macOS**

Solution:

```bash
# Install extract_chmLib via Homebrew
brew install chmlib

# Or use Python alternative
pip install pychm
```

**Issue: Encoding problems (garbled text)**

Solution:

```java
// Detect encoding
Charset detectedCharset = detectEncoding(htmlFile);
String content = Files.readString(htmlFile, detectedCharset);

// Force UTF-8 conversion
String utf8Content = new String(
    content.getBytes(detectedCharset),
    StandardCharsets.UTF_8
);
```

**Issue: TOC not loading**

Solution:

- Check for `.hhc` file in extracted directory
- Fallback to file listing if TOC parsing fails
- Validate HTML structure (some CHMs have malformed TOC)

---

## 9. Future Enhancements

- **Incremental updates**: Re-import only changed CHM sections
- **Multi-language support**: Detect and index localized CHMs
- **Image OCR**: Extract text from embedded images
- **Link resolution**: Convert CHM internal links to app navigation
- **Export**: Convert CHM back to modern formats (Markdown, PDF)

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-15  
**Maintainer:** PCM Desktop Team

