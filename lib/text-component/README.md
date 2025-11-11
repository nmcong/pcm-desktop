# Universal Text Component Libraries

## Thư viện đã tải (2024 latest versions):

### 1. Markdown Rendering
- **javafx-markdown-preview-all-1.0.3.jar** (22.9 MB)
  - All-in-one bundle với Flexmark dependencies
  - Hỗ trợ: Live preview, syntax highlighting, themes, export HTML
  - Repository: https://github.com/raghul-tech/JavaFX-Markdown-Preview

### 2. Rich Text Editing & Syntax Highlighting
- **richtextfx-0.11.4.jar** (293 KB)
  - Main library cho rich text editing
  - Repository: https://github.com/FXMisc/RichTextFX

#### Dependencies cho RichTextFX:
- **flowless-0.7.3.jar** (71 KB) - Virtualized flow container
- **reactfx-2.0-M5.jar** (428 KB) - Reactive programming for JavaFX
- **undofx-2.1.1.jar** (27 KB) - Undo/redo functionality  
- **wellbehavedfx-0.3.3.jar** (60 KB) - Well-behaved event handling

### 3. Mermaid Rendering
- Sử dụng JavaFX WebView + Mermaid.js CDN
- Không cần thư viện JAR riêng

## Usage trong code:

### Classpath cho compile:
```bash
-cp "lib/text-component/*"
```

### Import statements:
```java
// Markdown
import io.github.raghultech.javafx.markdownpreview.*;

// Rich Text
import org.fxmisc.richtext.*;
import org.fxmisc.flowless.*;
import org.reactfx.*;
import org.fxmisc.undofx.*;
import org.fxmisc.wellbehavedfx.*;

// WebView cho Mermaid
import javafx.scene.web.WebView;
```

## Cách sử dụng:

### 1. Markdown:
```java
MarkdownPreview preview = new MarkdownPreview();
preview.showMarkdown("# Hello\n\nThis is **markdown**!");
```

### 2. Code Editor:
```java
CodeArea codeArea = new CodeArea();
codeArea.setStyleSpans(0, computeHighlighting(codeText));
```

### 3. Mermaid:
```java
WebView webView = new WebView();
webView.getEngine().loadContent(mermaidHtml);
```

Tất cả thư viện đã được tải và ready để implement Universal Text Component!