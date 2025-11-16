# 📚 Phân Tích Thư Viện PCM Desktop

> **Ngày phân tích:** 15/11/2025  
> **Tổng số thư viện:** 41 JAR files + Native libraries  
> **Mục đích:** Đánh giá và tối ưu hóa dependencies

---

## 📋 Mục Lục

1. [Database Libraries](#1-database-libraries)
2. [Icon Libraries](#2-icon-libraries)
3. [JavaFX Libraries](#3-javafx-libraries)
4. [Logging Libraries](#4-logging-libraries)
5. [RAG Libraries](#5-rag-libraries)
6. [UI Libraries](#6-ui-libraries)
7. [Utility Libraries](#7-utility-libraries)
8. [Tổng Kết & Đề Xuất](#-tổng-kết--đề-xuất)

---

## 1. Database Libraries

### ✅ ĐANG SỬ DỤNG

#### `sqlite-jdbc-3.51.0.0.jar` (~7MB)

- **Vai trò:** SQLite JDBC driver - kết nối và tương tác với database SQLite
- **Mô tả:** Driver cơ bản để Java có thể làm việc với SQLite database
- **Sử dụng tại:**
    - `ConnectionManager.java` (line 20-21, 26)
    - Connection string: `jdbc:sqlite:data/pcm-desktop.db`
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL** - Không thể thiếu
- **Đề xuất:** **GIỮ LẠI** - Cốt lõi của persistence layer

---

### ❌ KHÔNG SỬ DỤNG

#### `HikariCP-7.0.2.jar` (~150KB)

- **Vai trò:** Connection pooling library - quản lý pool các kết nối database
- **Mô tả:**
    - HikariCP là một JDBC connection pool hiệu suất cao
    - Giúp tối ưu hóa việc tái sử dụng connections, giảm overhead tạo connection mới
    - Best practice cho production apps với nhiều concurrent connections
- **Lý do không dùng:**
    - `ConnectionManager` sử dụng singleton pattern với 1 connection duy nhất
    - Desktop app với ít concurrent access không cần connection pooling
- **Nên giữ?** 🤔 **CÂN NHẮC:**
    - ❌ **Xóa** nếu: App chỉ có 1 user, ít concurrent queries
    - ✅ **Giữ** nếu: Kế hoạch mở rộng với multi-threading, background jobs
- **Kích thước tiết kiệm:** ~150KB
- **Đề xuất:** **XÓA** - Hiện tại không cần thiết, có thể thêm lại sau nếu cần

#### `ojdbc11-23.26.0.0.0.jar` (~4.7MB)

- **Vai trò:** Oracle JDBC Driver - kết nối đến Oracle Database
- **Mô tả:**
    - Official Oracle Database JDBC driver version 23.26
    - Hỗ trợ Oracle 11g, 12c, 18c, 19c, 21c
    - Bao gồm advanced features: RAC, DataGuard, connection pooling
- **Lý do không dùng:**
    - Project chỉ sử dụng SQLite (`jdbc:sqlite:`)
    - Không có import `oracle.jdbc` trong toàn bộ codebase
    - Không có connection strings dạng `jdbc:oracle:`
- **Nên giữ?** 🤔 **CÂN NHẮC:**
    - ❌ **Xóa** nếu: Chỉ làm việc với SQLite, không có Oracle DB trong roadmap
    - ✅ **Giữ** nếu: Có kế hoạch connect đến enterprise Oracle databases
- **Kích thước tiết kiệm:** ~4.7MB
- **Đề xuất:** **XÓA** - SQLite đủ dùng cho desktop app

#### `ucp-23.26.0.0.0.jar` (~2.8MB)

- **Vai trò:** Oracle Universal Connection Pool
- **Mô tả:**
    - Connection pooling cho Oracle databases
    - Advanced features: Fast Connection Failover, Runtime Load Balancing
    - Tích hợp với Oracle RAC và DataGuard
- **Lý do không dùng:**
    - Không có Oracle database trong project
    - Không có import `oracle.ucp` trong codebase
- **Nên giữ?** ❌ **XÓA** - Dependency của ojdbc11, không cần nếu không dùng Oracle
- **Kích thước tiết kiệm:** ~2.8MB
- **Đề xuất:** **XÓA** - Chỉ cần thiết khi dùng Oracle

**💡 Lưu ý về Database:**

- Nếu trong tương lai cần multi-database support, nên sử dụng abstraction layer như JPA/Hibernate thay vì thêm từng JDBC
  driver riêng lẻ

---

## 2. Icon Libraries

### ✅ ĐANG SỬ DỤNG

#### `ikonli-core-12.4.0.jar` (~50KB)

- **Vai trò:** Core library của Ikonli framework
- **Mô tả:** Base API và interfaces cho icon system
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL** - Required by other ikonli libraries
- **Đề xuất:** **GIỮ LẠI**

#### `ikonli-javafx-12.4.0.jar` (~20KB)

- **Vai trò:** JavaFX integration cho Ikonli
- **Mô tả:** Cung cấp `FontIcon` class để render icons trong JavaFX
- **Sử dụng tại:** Tất cả UI files (12+ files)
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL**
- **Đề xuất:** **GIỮ LẠI**

#### `ikonli-feather-pack-12.4.0.jar` (~10KB)

- **Vai trò:** Feather Icons pack - Simply beautiful open source icons
- **Mô tả:**
    - 280+ minimalist icons designed by Cole Bemis
    - Style: Thin outline, modern, clean
- **Sử dụng tại:**
    - `CSSTestPage.java` - UI testing
    - `AIAssistantPage.java` - Chat interface icons
    - `UniversalTextDemoPage.java` - Text editor toolbar
    - `SettingsPage.java` - Settings UI
    - `KnowledgeBasePage.java` - Document icons
    - `DatabaseObjectsPage.java` - DB UI icons
    - `BatchJobsPage.java` - Job management icons
    - `UniversalTextComponent.java` - Text editor buttons
    - `UIIntegrationExample.java` - Demo icons
- **Icon examples:** `Feather.SEARCH`, `Feather.SAVE`, `Feather.FILE`
- **Tầm quan trọng:** ⭐⭐⭐⭐ **HIGH** - Được sử dụng nhiều
- **Đề xuất:** **GIỮ LẠI**

#### `ikonli-octicons-pack-12.4.0.jar` (~30KB)

- **Vai trò:** GitHub Octicons pack
- **Mô tả:**
    - GitHub's official icon set
    - 200+ icons with 16px và 24px variants
    - Style: Filled solid shapes, recognizable GitHub aesthetic
- **Sử dụng tại:**
    - `MainView.java` - Toolbar, navigation (THREE_BARS, MOON, SUN, BELL, GEAR, PERSON, etc.)
    - `SidebarView.java` - Navigation menu (BOOK, FILE_CODE, DATABASE, TOOLS, REPO, etc.)
    - `AIAssistantPage.java` - Send message button
- **Icon examples:** `Octicons.MOON_16`, `Octicons.DATABASE_24`, `Octicons.BOOK_24`
- **Tầm quan trọng:** ⭐⭐⭐⭐ **HIGH** - Core navigation icons
- **Đề xuất:** **GIỮ LẠI**

---

### ❌ KHÔNG SỬ DỤNG

#### `ikonli-antdesignicons-pack-12.4.0.jar` (~150KB)

- **Vai trò:** Ant Design Icons pack
- **Mô tả:**
    - Enterprise-class design language from Alibaba (Ant Financial)
    - 600+ icons covering business, e-commerce, data visualization
    - Style: Filled, outlined, two-tone variants
    - Popular trong enterprise React/Angular apps
- **Lý do không dùng:** Không có import `org.kordamp.ikonli.antdesignicons` trong codebase
- **Nên giữ?** 🤔 **CÂN NHẮC:**
    - ❌ **Xóa** nếu: Feather + Octicons đủ dùng
    - ✅ **Giữ** nếu: Cần icon pack phong phú hơn cho enterprise features
- **Kích thước tiết kiệm:** ~150KB
- **Đề xuất:** **XÓA** - Có thể thêm lại nếu cần

#### `ikonli-bpmn-pack-12.4.0.jar` (~40KB)

- **Vai trò:** Business Process Model and Notation icons
- **Mô tả:**
    - Icons cho workflow/process diagrams
    - BPMN 2.0 standard symbols (gateways, tasks, events, etc.)
    - Use case: Business process modeling, workflow designers
- **Lý do không dùng:** Không có import `org.kordamp.ikonli.bpmn` trong codebase
- **Nên giữ?** 🤔 **CÂN NHẮC:**
    - ❌ **Xóa** nếu: Không có workflow/process management features
    - ✅ **Giữ** nếu: Kế hoạch thêm batch job visualization hoặc workflow designer
- **Kích thước tiết kiệm:** ~40KB
- **Đề xuất:** **XÓA** - Quá specialized, không liên quan đến current features

---

## 3. JavaFX Libraries

### ✅ TẤT CẢ ĐANG SỬ DỤNG

> **Note:** Đây là runtime dependencies bắt buộc cho JavaFX applications

#### Core JAR Files:

##### `javafx.base.jar` (~750KB)

- **Vai trò:** Core JavaFX classes và utilities
- **Bao gồm:** Properties, collections, events, observables
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL**

##### `javafx.controls.jar` (~2.5MB)

- **Vai trò:** UI controls (Button, TextField, TableView, etc.)
- **Bao gồm:** Tất cả standard controls và skins
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL**

##### `javafx.fxml.jar` (~150KB)

- **Vai trò:** FXML support - declarative UI markup
- **Note:** Project có FXML files trong `src/main/resources/fxml/`
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL**

##### `javafx.graphics.jar` (~4.5MB)

- **Vai trò:** Graphics rendering, shapes, canvas, images
- **Bao gồm:** Scene graph, CSS, transforms, effects
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL**

##### `javafx.media.jar` (~250KB)

- **Vai trò:** Audio/video playback support
- **Tầm quan trọng:** ⭐⭐⭐ **MEDIUM** - Có thể cần cho multimedia features
- **Đề xuất:** **GIỮ LẠI** - Lightweight, có thể cần trong tương lai

##### `javafx.web.jar` (~700KB)

- **Vai trò:** WebView component (embedded browser using WebKit)
- **Use cases:** Render HTML content, OAuth flows, rich text preview
- **Tầm quan trọng:** ⭐⭐⭐⭐ **HIGH** - Có thể dùng cho markdown preview, docs
- **Đề xuất:** **GIỮ LẠI**

##### `javafx.swing.jar` (~100KB)

- **Vai trò:** JavaFX-Swing interoperability
- **Use case:** Embed JavaFX in Swing apps hoặc ngược lại
- **Tầm quan trọng:** ⭐⭐ **LOW** - Chỉ cần nếu có legacy Swing code
- **Đề xuất:** **GIỮ LẠI** - Lightweight

##### `javafx-swt.jar` (~50KB)

- **Vai trò:** JavaFX-SWT interoperability
- **Use case:** Embed JavaFX in Eclipse SWT apps
- **Tầm quan trọng:** ⭐ **VERY LOW** - Hiếm khi cần
- **Đề xuất:** **GIỮ LẠI** - Very small

#### Native Libraries (.dylib files):

> **Platform:** macOS (Darwin)  
> **Note:** Windows sẽ có .dll, Linux sẽ có .so

##### `libglass.dylib` (~350KB)

- **Vai trò:** Windowing system integration
- **Chức năng:** Window creation, event handling, native OS integration

##### `libjavafx_font.dylib` (~250KB)

- **Vai trò:** Font rendering và text layout

##### `libprism_*.dylib` (3 files, ~2MB total)

- **Vai trò:** Hardware-accelerated graphics pipeline
- **Variants:**
    - `libprism_common.dylib` - Common code
    - `libprism_es2.dylib` - OpenGL ES 2.0 backend
    - `libprism_sw.dylib` - Software fallback renderer

##### `libjavafx_iio.dylib` (~200KB)

- **Vai trò:** Image I/O - load/save PNG, JPEG, GIF, BMP

##### Media Libraries (4 files, ~8MB total):

- `libgstreamer-lite.dylib` (~4MB) - GStreamer framework
- `libjfxmedia.dylib` (~2MB) - JavaFX media implementation
- `libjfxmedia_avf.dylib` (~1.5MB) - AVFoundation backend (macOS)
- `libfxplugins.dylib` (~500KB) - Media plugins

##### `libjfxwebkit.dylib` (~70MB)

- **Vai trò:** WebKit engine cho WebView
- **Note:** Đây là file lớn nhất (~70MB)

##### Supporting Libraries:

- `libdecora_sse.dylib` - Effects và decorations
- `libglib-lite.dylib` - GLib utilities subset

**Đề xuất cho JavaFX:** **GIỮ TẤT CẢ** - Đây là runtime dependencies bắt buộc

---

## 4. Logging Libraries

### ✅ TẤT CẢ ĐANG SỬ DỤNG

#### `slf4j-api-2.0.17.jar` (~65KB)

- **Vai trò:** Simple Logging Facade for Java - Logging abstraction API
- **Mô tả:**
    - Interface layer cho logging, không có implementation
    - Cho phép switch logging frameworks mà không thay đổi code
- **Sử dụng:** Annotation `@Slf4j` trong 144+ Java files
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL**
- **Đề xuất:** **GIỮ LẠI**

#### `logback-core-1.5.21.jar` (~600KB)

- **Vai trò:** Logback core engine
- **Mô tả:** Base functionality cho logback framework
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL** - Required by logback-classic
- **Đề xuất:** **GIỮ LẠI**

#### `logback-classic-1.5.21.jar` (~300KB)

- **Vai trò:** SLF4J implementation using Logback
- **Mô tả:**
    - Native implementation của SLF4J API
    - Advanced features: MDC, markers, filters, appenders
    - Configuration: `src/main/resources/logback.xml`
- **Sử dụng:** Runtime logging implementation trong toàn bộ app
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL**
- **Đề xuất:** **GIỮ LẠI**

**💡 Logging Stack:**

```
Application Code
     ↓ @Slf4j
  slf4j-api
     ↓
logback-classic
     ↓
logback-core
     ↓
Log Files (logs/pcm-desktop.log)
```

---

## 5. RAG Libraries

### ✅ TẤT CẢ ĐANG SỬ DỤNG

> **Note:** RAG = Retrieval Augmented Generation - Core AI functionality

#### Lucene Libraries (5 JARs, ~15MB total):

##### `lucene-core-10.3.1.jar` (~3.5MB)

- **Vai trò:** Apache Lucene core search engine
- **Chức năng:** Indexing, searching, scoring algorithms
- **Sử dụng tại:** `LuceneVectorStore.java`
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL** - Core của vector store

##### `lucene-analysis-common-10.3.1.jar` (~2MB)

- **Vai trò:** Text analysis và tokenization
- **Chức năng:** Analyzers, tokenizers, filters cho nhiều ngôn ngữ

##### `lucene-queryparser-10.3.1.jar` (~400KB)

- **Vai trò:** Query parsing và building
- **Chức năng:** Parse query syntax thành Lucene queries

##### `lucene-queries-10.3.1.jar` (~250KB)

- **Vai trò:** Advanced query types
- **Chức năng:** Complex queries, scoring, filtering

##### `lucene-highlighter-10.3.1.jar` (~300KB)

- **Vai trò:** Search result highlighting
- **Chức năng:** Highlight matching terms trong search results

#### ONNX Runtime Libraries (2 JARs, ~35MB):

##### `onnxruntime-1.23.2.jar` (~35MB)

- **Vai trò:** ONNX Runtime Java bindings
- **Mô tả:**
    - Microsoft's cross-platform ML inference engine
    - Executes ONNX models (Open Neural Network Exchange format)
    - Hardware acceleration: CPU, GPU, NPU
- **Model:** Runs `models/all-MiniLM-L6-v2/model.onnx`
- **Sử dụng tại:** `DJLEmbeddingService.java`
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL** - Embedding generation
- **Note:** File lớn nhất trong RAG libraries (~35MB)

##### `onnxruntime-engine-0.35.0.jar` (~500KB)

- **Vai trò:** DJL (Deep Java Library) engine cho ONNX Runtime
- **Mô tả:** Integration layer giữa DJL và ONNX Runtime
- **Sử dụng tại:** `DJLEmbeddingService.java`
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL**

#### DJL Libraries (2 JARs):

##### `api-0.35.0.jar` (~1.5MB)

- **Vai trò:** Deep Java Library (DJL) API
- **Mô tả:**
    - Framework-agnostic deep learning API
    - Abstraction over multiple ML engines (PyTorch, TensorFlow, ONNX, etc.)
- **Sử dụng tại:** `DJLEmbeddingService.java`, `QdrantClient.java`
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL**

##### `tokenizers-0.35.0.jar` (~8MB)

- **Vai trò:** DJL tokenizers library
- **Mô tả:**
    - Fast tokenization using Rust-based HuggingFace tokenizers
    - Loads `tokenizer.json` from model directory
- **Model tokenizer:** `models/all-MiniLM-L6-v2/tokenizer.json`
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL** - Text preprocessing

#### JavaParser Libraries (2 JARs, ~3MB):

##### `javaparser-core-3.26.3.jar` (~2.5MB)

- **Vai trò:** Java source code parser
- **Mô tả:**
    - Parse Java code thành AST (Abstract Syntax Tree)
    - Support Java 17+ syntax
- **Sử dụng tại:**
    - `ASTParser.java`
    - `EnhancedASTAnalyzer.java`
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL** - Code analysis

##### `javaparser-symbol-solver-core-3.26.3.jar` (~500KB)

- **Vai trò:** Symbol resolution cho JavaParser
- **Mô tả:**
    - Resolve types, methods, variables
    - Understand imports và dependencies
- **Sử dụng tại:** AST analysis features
- **Tầm quan trọng:** ⭐⭐⭐⭐ **HIGH**

**💡 RAG Pipeline:**

```
Source Code/Documents
     ↓ JavaParser
  AST Analysis
     ↓ Chunking
  Text Chunks
     ↓ Tokenizers
  Token IDs
     ↓ ONNX Runtime (all-MiniLM-L6-v2)
  Embeddings (384-dim vectors)
     ↓ Lucene
  Vector Store (Index)
     ↓ Search
  Retrieved Documents → LLM
```

**Đề xuất:** **GIỮ TẤT CẢ** - Core functionality cho RAG system

---

## 6. UI Libraries

### ✅ ĐANG SỬ DỤNG

#### `atlantafx-base-2.1.0.jar` (~1.5MB)

- **Vai trò:** Modern JavaFX theme framework
- **Mô tả:**
    - Modern CSS themes (Primer Light/Dark, Nord, Cupertino, Dracula)
    - Based on GitHub Primer design system
    - Improved default controls styling
    - Dark mode support
- **Sử dụng tại:**
    - `ThemeManager.java` - Theme switching logic
    - 11 UI pages - Apply AtlantaFX stylesheets
- **Import:** `import atlantafx.base.theme.*`
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL** - Core UI theming
- **Đề xuất:** **GIỮ LẠI**

---

### ❌ KHÔNG SỬ DỤNG

> **Note:** Các thư viện này là ecosystem của RichTextFX - một text editor component

#### `richtextfx-0.11.6.jar` (~500KB)

- **Vai trò:** Rich text editor component cho JavaFX
- **Mô tả:**
    - Advanced text editing: Syntax highlighting, line numbers, code folding
    - Components: `CodeArea`, `StyleClassedTextArea`, `InlineCssTextArea`
    - Use cases: Code editors, rich text documents, logs viewers
- **Lý do không dùng:**
    - Không có import `org.fxmisc.richtext` trong codebase
    - Project có custom `UniversalTextComponent` instead
- **Nên giữ?** 🤔 **CÂN NHẮC:**
    - ❌ **Xóa** nếu: `UniversalTextComponent` đủ dùng
    - ✅ **Giữ** nếu: Cần professional code editor với syntax highlighting
- **Dependencies:** Cần `flowless`, `reactfx`, `undofx`, `wellbehavedfx`
- **Kích thước tiết kiệm:** ~500KB
- **Đề xuất:** **XÓA** - Nhưng cân nhắc kỹ nếu cần code editor

#### `flowless-0.7.4.jar` (~80KB)

- **Vai trò:** Efficient VirtualFlow implementation
- **Mô tả:**
    - Virtual scrolling cho large content
    - Chỉ render visible items → performance
    - Dependency của RichTextFX
- **Lý do không dùng:** RichTextFX không được dùng
- **Đề xuất:** **XÓA** - Dependency của RichTextFX

#### `reactfx-2.0-M6.jar` (~150KB)

- **Vai trò:** Reactive programming extensions cho JavaFX
- **Mô tả:**
    - Event streams và reactive bindings
    - Lazy evaluation, combining events
    - Similar to RxJava nhưng cho JavaFX
    - Dependency của RichTextFX
- **Lý do không dùng:** Không có import `org.reactfx`
- **Nên giữ?** 🤔 **CÂN NHẮC:**
    - ❌ **Xóa** nếu: Không cần reactive patterns
    - ✅ **Giữ** nếu: Muốn dùng reactive programming trong UI
- **Đề xuất:** **XÓA** - Không được sử dụng

#### `undofx-2.1.1.jar` (~50KB)

- **Vai trò:** Undo/Redo framework cho JavaFX
- **Mô tả:**
    - Generic undo manager
    - Command pattern implementation
    - Dependency của RichTextFX
- **Lý do không dùng:** Không có import `org.fxmisc.undo`
- **Nên giữ?** 🤔 **CÂN NHẮC:**
    - ❌ **Xóa** nếu: Không cần undo/redo functionality
    - ✅ **Giữ** nếu: Kế hoạch implement undo/redo trong text editor
- **Đề xuất:** **XÓA** - Lightweight nhưng không dùng

#### `wellbehavedfx-0.3.3.jar` (~30KB)

- **Vai trò:** Better event handling cho JavaFX
- **Mô tả:**
    - Correct event handling semantics
    - Prevent event handler leaks
    - Dependency của RichTextFX
- **Lý do không dùng:** Không có import `org.fxmisc.wellbehaved`
- **Đề xuất:** **XÓA**

**💡 RichTextFX Ecosystem:**

```
RichTextFX (Main component)
    ├── Flowless (Virtual scrolling)
    ├── ReactFX (Reactive bindings)
    ├── UndoFX (Undo/redo)
    └── WellBehavedFX (Event handling)
```

**Tổng kích thước tiết kiệm nếu xóa RichTextFX ecosystem:** ~810KB

**⚠️ Cảnh báo:** Nếu trong tương lai cần professional code editor với syntax highlighting, sẽ phải download lại tất cả 5
libraries này.

---

## 7. Utility Libraries

### ✅ TẤT CẢ ĐANG SỬ DỤNG

#### Jackson Libraries (4 JARs, ~2MB total):

##### `jackson-core-2.20.1.jar` (~500KB)

- **Vai trò:** Core JSON streaming API
- **Mô tả:** Low-level JSON parsing và generation (JsonParser, JsonGenerator)

##### `jackson-databind-2.20.1.jar` (~1.5MB)

- **Vai trò:** Object mapping - POJO ↔ JSON
- **Mô tả:**
    - Serialize Java objects → JSON
    - Deserialize JSON → Java objects
    - ObjectMapper API

##### `jackson-annotations-2.20.jar` (~80KB)

- **Vai trò:** Annotations cho databinding
- **Mô tả:** `@JsonProperty`, `@JsonIgnore`, `@JsonFormat`, etc.

##### `jackson-datatype-jsr310-2.20.1.jar` (~150KB)

- **Vai trò:** Java 8 Date/Time API support
- **Mô tả:** Serialize/deserialize `LocalDateTime`, `Instant`, etc.

**Sử dụng tại:** 144+ files (rất nhiều!)

- All LLM providers (OpenAI, Anthropic, Ollama, Custom API)
- RAG pipeline (document processing)
- Configuration management
- API communication
- Database logging
- Conversation/message serialization

**Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL** - Core data serialization
**Đề xuất:** **GIỮ TẤT CẢ**

---

#### `lombok-1.18.34.jar` (~2MB)

- **Vai trò:** Code generation library - giảm boilerplate code
- **Mô tả:**
    - Compile-time annotation processor
    - Generate getters, setters, constructors, toString, equals, hashCode
    - Annotations: `@Data`, `@Getter`, `@Setter`, `@Slf4j`, `@Builder`, etc.
- **Sử dụng tại:** 144+ files
    - `@Slf4j` - Auto-generate logger field (most common)
    - `@Data` - Generate all boilerplate
    - Domain entities, DTOs, models
- **Examples:**
  ```java
  @Slf4j  // → private static final Logger log = ...
  @Data   // → getters, setters, toString, equals, hashCode
  public class Message {
      private String content;
      private LocalDateTime timestamp;
  }
  ```
- **Tầm quan trọng:** ⭐⭐⭐⭐⭐ **CRITICAL** - Used everywhere
- **Note:** Compile-time only, không ảnh hưởng runtime performance
- **Đề xuất:** **GIỮ LẠI**

---

## 📊 Tổng Kết & Đề Xuất

### ✅ Thư Viện Cần Giữ (31 JARs + Native libs):

| Category  | Count            | Total Size  | Priority |
|-----------|------------------|-------------|----------|
| Database  | 1                | ~7 MB       | ⭐⭐⭐⭐⭐    |
| Icons     | 4                | ~110 KB     | ⭐⭐⭐⭐⭐    |
| JavaFX    | 8 JARs + natives | ~75 MB      | ⭐⭐⭐⭐⭐    |
| Logging   | 3                | ~1 MB       | ⭐⭐⭐⭐⭐    |
| RAG       | 11               | ~65 MB      | ⭐⭐⭐⭐⭐    |
| UI        | 1                | ~1.5 MB     | ⭐⭐⭐⭐⭐    |
| Utils     | 5                | ~4 MB       | ⭐⭐⭐⭐⭐    |
| **TOTAL** | **31 JARs**      | **~153 MB** |          |

---

### ❌ Thư Viện Đề Xuất Xóa (10 JARs):

| Library                        | Size         | Lý Do                         | Có Thể Cần?               |
|--------------------------------|--------------|-------------------------------|---------------------------|
| **Database:**                  |              |                               |                           |
| `HikariCP-7.0.2.jar`           | ~150 KB      | Không dùng connection pooling | 🤔 Nếu cần performance    |
| `ojdbc11-23.26.0.0.0.jar`      | ~4.7 MB      | Chỉ dùng SQLite               | ❌ Không                   |
| `ucp-23.26.0.0.0.jar`          | ~2.8 MB      | Dependency của Oracle         | ❌ Không                   |
| **Icons:**                     |              |                               |                           |
| `ikonli-antdesignicons-pack`   | ~150 KB      | Icon pack không dùng          | 🤔 Nếu cần thêm icons     |
| `ikonli-bpmn-pack`             | ~40 KB       | BPMN workflow icons           | ❌ Không (quá specialized) |
| **UI (RichTextFX ecosystem):** |              |                               |                           |
| `richtextfx-0.11.6.jar`        | ~500 KB      | Có UniversalTextComponent     | 🤔 Nếu cần code editor    |
| `flowless-0.7.4.jar`           | ~80 KB       | Dependency của RichTextFX     | ❌ Không                   |
| `reactfx-2.0-M6.jar`           | ~150 KB      | Reactive framework không dùng | 🤔 Nếu dùng reactive UI   |
| `undofx-2.1.1.jar`             | ~50 KB       | Undo/redo không dùng          | 🤔 Nếu cần undo/redo      |
| `wellbehavedfx-0.3.3.jar`      | ~30 KB       | Dependency của RichTextFX     | ❌ Không                   |
| **TOTAL**                      | **~8.65 MB** |                               |                           |

---

### 📈 Thống Kê Tổng Quan:

- **Tổng số thư viện:** 41 JAR files
- **Đang sử dụng:** 31 JARs (75.6%)
- **Không sử dụng:** 10 JARs (24.4%)
- **Dung lượng tiết kiệm nếu xóa:** ~8.65 MB (~5.6% total size)

---

### 🎯 Kế Hoạch Hành Động:

#### Option 1: Conservative (An toàn) - XÓA 5 JARs

**Xóa các thư viện chắc chắn không cần:**

- ✅ `ojdbc11-23.26.0.0.0.jar` (4.7 MB)
- ✅ `ucp-23.26.0.0.0.jar` (2.8 MB)
- ✅ `ikonli-bpmn-pack-12.4.0.jar` (40 KB)
- ✅ `flowless-0.7.4.jar` (80 KB)
- ✅ `wellbehavedfx-0.3.3.jar` (30 KB)

**Tiết kiệm:** ~7.6 MB  
**Rủi ro:** ❌ Không có

---

#### Option 2: Aggressive (Tối ưu) - XÓA 10 JARs

**Xóa tất cả thư viện không sử dụng:**

- ✅ Tất cả 5 JARs từ Option 1
- ✅ `HikariCP-7.0.2.jar` (150 KB)
- ✅ `ikonli-antdesignicons-pack-12.4.0.jar` (150 KB)
- ✅ `richtextfx-0.11.6.jar` (500 KB)
- ✅ `reactfx-2.0-M6.jar` (150 KB)
- ✅ `undofx-2.1.1.jar` (50 KB)

**Tiết kiệm:** ~8.65 MB  
**Rủi ro:** ⚠️ Thấp - Có thể cần RichTextFX trong tương lai

---

#### Option 3: Hybrid (Cân bằng) - XÓA 8 JARs

**Xóa hầu hết, giữ lại RichTextFX ecosystem để backup:**

- ✅ Tất cả Database không dùng (3 JARs, ~7.6 MB)
- ✅ Tất cả Icon packs không dùng (2 JARs, ~190 KB)
- ❌ Giữ RichTextFX ecosystem (5 JARs) - Có thể dùng trong tương lai

**Tiết kiệm:** ~7.8 MB  
**Rủi ro:** ❌ Không có - Vẫn có option cho text editor

---

### 💡 Khuyến Nghị:

**Tôi đề xuất: Option 1 (Conservative)**

**Lý do:**

1. ✅ An toàn 100% - Không ảnh hưởng current features
2. ✅ Vẫn tiết kiệm ~7.6 MB (~88% của tổng có thể tiết kiệm)
3. ✅ Giữ lại options cho future features:
    - RichTextFX → Professional code editor
    - HikariCP → Connection pooling nếu scale up
    - Ant Design Icons → Enterprise UI

**Thư viện giữ lại "just in case":**

- `HikariCP` - Lightweight (150KB), có thể cần khi optimize performance
- `ikonli-antdesignicons-pack` - Lightweight (150KB), icon pack backup
- RichTextFX ecosystem (~810KB) - Nếu cần text editor tốt hơn

---

### 📝 Backup Strategy:

Trước khi xóa, tạo backup:

```bash
# Tạo thư mục backup
mkdir -p lib/backup/unused

# Move thay vì delete (để có thể restore)
mv lib/database/ojdbc11-*.jar lib/backup/unused/
mv lib/database/ucp-*.jar lib/backup/unused/
mv lib/icons/ikonli-bpmn-pack-*.jar lib/backup/unused/
mv lib/ui/flowless-*.jar lib/backup/unused/
mv lib/ui/wellbehavedfx-*.jar lib/backup/unused/
```

Test app sau khi xóa:

```bash
./scripts/build.sh
./scripts/run.sh
```

Nếu OK → Delete backup folder  
Nếu lỗi → Restore từ backup

---

### 🔄 Future Considerations:

1. **Maven/Gradle Migration:**
    - Hiện tại: Manual dependency management trong `lib/`
    - Tương lai: Migrate sang Maven/Gradle để auto-manage dependencies
    - Benefit: Version updates, conflict resolution, transitive dependencies

2. **JLink/JPackage:**
    - Create custom JRE với chỉ modules cần thiết
    - Giảm distribution size đáng kể
    - Package thành native installer (.dmg, .exe, .deb)

3. **Code Analysis Tools:**
    - JDeps - Analyze actual dependency usage
    - Dependency-check - Security vulnerabilities
    - Versions plugin - Check for updates

---

## ❓ Câu Hỏi Cần Trả Lời Trước Khi Xóa:

1. **Text Editor:** `UniversalTextComponent` có đủ features không? Hay cần upgrade lên RichTextFX?

2. **Performance:** Có kế hoạch optimize database với connection pooling không?

3. **Multi-Database:** Có khả năng support Oracle hoặc databases khác không?

4. **Icons:** Feather + Octicons có đủ không? Hay cần thêm icon sets?

5. **Reactive UI:** Có plan sử dụng reactive programming patterns không?

---

## 📚 Tài Liệu Tham Khảo:

- **Ikonli:** https://kordamp.org/ikonli/
- **AtlantaFX:** https://github.com/mkpaz/atlantafx
- **RichTextFX:** https://github.com/FXMisc/RichTextFX
- **Jackson:** https://github.com/FasterXML/jackson
- **Lombok:** https://projectlombok.org/
- **Logback:** https://logback.qos.ch/
- **Apache Lucene:** https://lucene.apache.org/
- **ONNX Runtime:** https://onnxruntime.ai/
- **DJL:** https://djl.ai/
- **JavaParser:** https://javaparser.org/

---

**📅 Ngày tạo:** 15/11/2025  
**✍️ Người tạo:** AI Code Assistant  
**📋 Version:** 1.0  
**🔄 Cập nhật cuối:** 15/11/2025


