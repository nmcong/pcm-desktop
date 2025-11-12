# Nghiên Cứu Java AST và Tích Hợp LLM cho PCM WebApp

## Tổng Quan

Tài liệu này nghiên cứu về các phương án tích hợp Java Abstract Syntax Tree (AST) vào hệ thống PCM WebApp để LLM có thể
hiểu và phân tích source code Java một cách hiệu quả. Do việc xử lý trực tiếp file trong `apps/pcm-webapp` là bất khả
thi, chúng ta cần tìm các phương án thay thế với độ chính xác cao.

## 1. Khảo Sát Hệ Thống Hiện Tại

### 1.1 Cấu Trúc PCM WebApp

PCM WebApp là một ứng dụng web single-page được xây dựng với:

- **Frontend**: Vanilla JavaScript với module system
- **Architecture**: Component-based với các modules độc lập
- **Core Components**:
    - AI Panel (`public/js/modules/ai/`) - Hệ thống AI tích hợp
    - Codebase Manager (`public/js/modules/codebase/`) - Quản lý source code
    - Database Objects (`public/js/modules/db-objects/`) - Quản lý objects database
    - Screen Source (`public/js/modules/screen-source/`) - Quản lý source code của màn hình

### 1.2 Hạn Chế Hiện Tại

- Không thể xử lý trực tiếp file Java trong môi trường browser
- Thiếu khả năng parsing AST cho Java source code
- LLM chỉ có thể hiểu code ở mức text thô, không có semantic analysis

## 2. Công Cụ Java AST Parsing Phổ Biến (2024)

### 2.1 JavaParser

**Ưu điểm:**

- Lightweight dependencies với số lượng dependency ít
- Active maintenance với hàng chục contributors
- Lexical preservation và pretty printing
- Symbol resolution mạnh mẽ
- Hỗ trợ JDK 21
- Dễ sử dụng và tích hợp

**Nhược điểm:**

- Không thể chạy trực tiếp trong browser
- Cần Java runtime environment

### 2.2 Eclipse JDT (Java Development Tools)

**Ưu điểm:**

- Robust error handling được thiết kế cho IDE
- Semantic analysis hoàn chỉnh
- Hỗ trợ code completion, quick fix
- Mature và stable

**Nhược điểm:**

- Heavy dependencies (cần Eclipse platform)
- Phức tạp để tích hợp standalone
- Không phù hợp với môi trường web

### 2.3 Spoon

**Ưu điểm:**

- Rich semantic analysis với binding information
- Runtime compilation support
- Unique identification cho source elements
- Shadow classes cho library manipulation

**Nhược điểm:**

- Learning curve cao
- Phức tạp cho use case đơn giản
- Không tối ưu cho web environment

### 2.4 Tree-sitter

**Ưu điểm:**

- **WebAssembly support** - có thể chạy trong browser
- Performance cao (written in C)
- Graceful error handling
- Hỗ trợ 81+ programming languages
- Incremental parsing
- Syntax highlighting và code analysis

**Nhược điểm:**

- Limited semantic analysis so với specialized Java parsers
- Cần học syntax pattern riêng

## 3. Hỗ Trợ Đa Ngôn Ngữ (Multi-Language Support)

### 3.1 Tình Trạng Hỗ Trợ Tree-sitter cho Các Ngôn Ngữ

#### 3.1.1 Ngôn Ngữ Web Core - **HỖ TRỢ HOÀN TOÀN** ✅

**HTML Grammar**

- **Repository**: `tree-sitter/tree-sitter-html`
- **Tình trạng**: Official, well-maintained
- **Tính năng**: Hỗ trợ đầy đủ HTML5 với tag nesting và attributes

**JavaScript/TypeScript Grammar**

- **JavaScript**: `tree-sitter/tree-sitter-javascript`
- **TypeScript**: `tree-sitter/tree-sitter-typescript`
- **Tình trạng**: Official, excellent support
- **Tính năng**:
    - ECMAScript specification compliance
    - JSX/TSX support tích hợp
    - Cả JS và TS variants đều có sẵn

#### 3.1.2 JSP (JavaServer Pages) - **HỖ TRỢ COMMUNITY** ⚠️

**JSP Grammar Status**:

- **Có sẵn**: `merico-dev/tree-sitter-jsp` (third-party)
- **Dựa trên**: tree-sitter-vue và tree-sitter-html
- **Tình trạng**: Community-maintained, không official
- **Phương án thay thế**: `QthCN/tree-sitter-jsp` (implementation khác)

#### 3.1.3 XFDL (XML Forms Description Language) - **CHƯA CÓ GRAMMAR CHUYÊN DỤNG** ❌

**XFDL Status**:

- **Không có parser XFDL chuyên dụng**
- **Giải pháp tạm thời**: Sử dụng XML grammar (vì XFDL là XML-based)
- **Repository**: Có thể leverage `tree-sitter-grammars/tree-sitter-xml`
- **Hạn chế**: Không cung cấp XFDL-specific syntax highlighting hoặc validation

#### 3.1.4 XML Grammar - **HỖ TRỢ HOÀN TOÀN** ✅

**XML Grammar**:

- **Repository**: `tree-sitter-grammars/tree-sitter-xml`
- **Tình trạng**: Official, maintained
- **Tính năng**: XML 1.0 specification compliant, DTD support

### 3.2 Kiến Trúc Universal Multi-Language Parser

#### 3.2.1 Universal AST Parser Architecture

```javascript
// File: public/js/modules/ast/UniversalASTParser.js
export class UniversalASTParser {
  constructor() {
    this.parsers = new Map();
    this.supportedLanguages = {
      java: "tree-sitter-java.wasm",
      javascript: "tree-sitter-javascript.wasm",
      typescript: "tree-sitter-typescript.wasm",
      html: "tree-sitter-html.wasm",
      jsp: "tree-sitter-jsp.wasm",
      xml: "tree-sitter-xml.wasm",
      xfdl: "tree-sitter-xml.wasm", // fallback to XML
    };
    this.isInitialized = false;
  }

  async initialize(languages = []) {
    await Parser.init();

    const languagesToLoad =
      languages.length > 0 ? languages : Object.keys(this.supportedLanguages);

    for (const language of languagesToLoad) {
      if (this.supportedLanguages[language]) {
        await this.loadLanguage(language);
      }
    }

    this.isInitialized = true;
  }

  async loadLanguage(language) {
    if (this.parsers.has(language)) return;

    const parser = new Parser();
    const wasmFile = this.supportedLanguages[language];
    const Language = await Parser.Language.load(`/vendor/${wasmFile}`);

    parser.setLanguage(Language);
    this.parsers.set(language, parser);
  }

  detectLanguage(filename, content) {
    const extension = filename.split(".").pop().toLowerCase();

    const extensionMap = {
      java: "java",
      js: "javascript",
      ts: "typescript",
      jsx: "javascript",
      tsx: "typescript",
      html: "html",
      htm: "html",
      jsp: "jsp",
      xml: "xml",
      xfdl: "xfdl",
    };

    return extensionMap[extension] || this.detectByContent(content);
  }

  detectByContent(content) {
    // Content-based detection logic
    if (content.includes("<%@") || content.includes("<%=")) return "jsp";
    if (content.includes("<!DOCTYPE html") || content.includes("<html"))
      return "html";
    if (content.includes("<?xml")) return "xml";
    if (content.includes("class ") && content.includes("public")) return "java";
    return "javascript"; // default fallback
  }

  async parseCode(content, language = null, filename = "") {
    if (!this.isInitialized) {
      throw new Error("Parser not initialized");
    }

    const detectedLanguage = language || this.detectLanguage(filename, content);

    if (!this.parsers.has(detectedLanguage)) {
      await this.loadLanguage(detectedLanguage);
    }

    const parser = this.parsers.get(detectedLanguage);
    const tree = parser.parse(content);

    return this.convertToLLMFormat(tree.rootNode, detectedLanguage);
  }

  convertToLLMFormat(node, language) {
    return {
      language: language,
      type: node.type,
      text: node.text,
      startPosition: node.startPosition,
      endPosition: node.endPosition,
      children: node.children.map((child) =>
        this.convertToLLMFormat(child, language),
      ),
      metadata: {
        parseTime: Date.now(),
        languageVersion: this.getLanguageVersion(language),
      },
    };
  }

  getLanguageVersion(language) {
    // Return version info for each language grammar
    const versions = {
      java: "0.21.0",
      javascript: "0.21.0",
      typescript: "0.21.0",
      html: "0.20.0",
      jsp: "0.1.0", // community version
      xml: "0.6.0",
      xfdl: "0.6.0", // using XML version
    };
    return versions[language] || "0.0.0";
  }
}
```

#### 3.2.2 Multi-Language File Processor

```javascript
// File: public/js/modules/ast/MultiLanguageProcessor.js
export class MultiLanguageProcessor {
  constructor(universalParser) {
    this.universalParser = universalParser;
  }

  async processMultiLanguageFile(content, filename) {
    const primaryLanguage = this.universalParser.detectLanguage(
      filename,
      content,
    );

    // Handle mixed language files (JSP, HTML with scripts, etc.)
    if (primaryLanguage === "jsp") {
      return await this.processJSPFile(content, filename);
    } else if (primaryLanguage === "html") {
      return await this.processHTMLFile(content, filename);
    } else {
      return await this.universalParser.parseCode(
        content,
        primaryLanguage,
        filename,
      );
    }
  }

  async processJSPFile(content, filename) {
    // Extract different language sections from JSP
    const sections = this.extractJSPSections(content);
    const results = [];

    for (const section of sections) {
      const ast = await this.universalParser.parseCode(
        section.content,
        section.language,
        filename,
      );
      results.push({
        ...ast,
        section: section.type,
        range: section.range,
      });
    }

    return {
      type: "multi-language",
      primaryLanguage: "jsp",
      sections: results,
      fullAST: await this.universalParser.parseCode(content, "jsp", filename),
    };
  }

  extractJSPSections(content) {
    const sections = [];

    // JSP scriptlets: <% ... %>
    const scriptletRegex = /<%\s*(.*?)\s*%>/gs;
    let match;
    while ((match = scriptletRegex.exec(content)) !== null) {
      sections.push({
        type: "scriptlet",
        language: "java",
        content: match[1],
        range: { start: match.index, end: match.index + match[0].length },
      });
    }

    // JSP expressions: <%= ... %>
    const expressionRegex = /<%=\s*(.*?)\s*%>/gs;
    while ((match = expressionRegex.exec(content)) !== null) {
      sections.push({
        type: "expression",
        language: "java",
        content: match[1],
        range: { start: match.index, end: match.index + match[0].length },
      });
    }

    // HTML sections
    const htmlContent = content.replace(/<%.*?%>/gs, "");
    if (htmlContent.trim()) {
      sections.push({
        type: "html",
        language: "html",
        content: htmlContent,
        range: { start: 0, end: content.length },
      });
    }

    return sections;
  }

  async processHTMLFile(content, filename) {
    // Extract JavaScript sections from HTML
    const sections = this.extractHTMLSections(content);
    const results = [];

    for (const section of sections) {
      const ast = await this.universalParser.parseCode(
        section.content,
        section.language,
        filename,
      );
      results.push({
        ...ast,
        section: section.type,
        range: section.range,
      });
    }

    return {
      type: "multi-language",
      primaryLanguage: "html",
      sections: results,
      fullAST: await this.universalParser.parseCode(content, "html", filename),
    };
  }

  extractHTMLSections(content) {
    const sections = [];

    // JavaScript in <script> tags
    const scriptRegex = /<script[^>]*>(.*?)<\/script>/gs;
    let match;
    while ((match = scriptRegex.exec(content)) !== null) {
      sections.push({
        type: "script",
        language: "javascript",
        content: match[1],
        range: { start: match.index, end: match.index + match[0].length },
      });
    }

    // CSS in <style> tags
    const styleRegex = /<style[^>]*>(.*?)<\/style>/gs;
    while ((match = styleRegex.exec(content)) !== null) {
      sections.push({
        type: "style",
        language: "css",
        content: match[1],
        range: { start: match.index, end: match.index + match[0].length },
      });
    }

    return sections;
  }
}
```

### 3.3 Tối Ưu Hóa Performance cho Multi-Language

#### 3.3.1 Lazy Loading Strategy

```javascript
// File: public/js/modules/ast/PerformanceOptimizer.js
export class ASTPerformanceOptimizer {
  constructor() {
    this.loadedLanguages = new Set();
    this.parsingCache = new Map();
    this.maxCacheSize = 100;
  }

  async loadLanguageOnDemand(language, universalParser) {
    if (this.loadedLanguages.has(language)) return;

    await universalParser.loadLanguage(language);
    this.loadedLanguages.add(language);

    console.log(`✅ Loaded ${language} parser on demand`);
  }

  getCachedAST(content, language) {
    const key = `${language}:${this.hashContent(content)}`;
    return this.parsingCache.get(key);
  }

  setCachedAST(content, language, ast) {
    const key = `${language}:${this.hashContent(content)}`;

    // Implement LRU cache
    if (this.parsingCache.size >= this.maxCacheSize) {
      const firstKey = this.parsingCache.keys().next().value;
      this.parsingCache.delete(firstKey);
    }

    this.parsingCache.set(key, ast);
  }

  hashContent(content) {
    // Simple hash function for content
    let hash = 0;
    for (let i = 0; i < content.length; i++) {
      const char = content.charCodeAt(i);
      hash = (hash << 5) - hash + char;
      hash = hash & hash; // Convert to 32-bit integer
    }
    return hash.toString();
  }
}
```

## 4. Phương Án Tích Hợp cho LLM

### 4.1 Phương Án 1: Microservice AST Parser

**Kiến trúc:**

```
PCM WebApp (Browser) → REST API → Java AST Microservice → Database/Cache
```

**Chi tiết implementation:**

- **Backend Service**: Spring Boot microservice sử dụng JavaParser
- **API Endpoints**:
    - `POST /api/ast/parse` - Parse Java source code
    - `GET /api/ast/analyze/{id}` - Lấy AST analysis
    - `POST /api/ast/semantic` - Semantic analysis
- **Containerization**: Docker container với Java runtime
- **Caching**: Redis cache cho parsed AST
- **Output**: JSON-formatted AST cho LLM consumption

**Ưu điểm:**

- Full semantic analysis với JavaParser
- Scalable và maintainable
- Caching để tối ưu performance
- Tách biệt concerns

**Nhược điểm:**

- Latency network calls
- Cần infrastructure thêm
- Complexity tăng

### 3.2 Phương Án 2: Tree-sitter WebAssembly Integration

**Kiến trúc:**

```
PCM WebApp → Tree-sitter WASM → In-browser AST → LLM
```

**Chi tiết implementation:**

- **Web Tree-sitter**: `npm install web-tree-sitter`
- **Java Grammar**: Tree-sitter Java grammar compiled to WASM
- **Browser Integration**: Client-side parsing không cần backend
- **AST Format**: S-expression hoặc JSON tree structure

**Code example:**

```javascript
import Parser from "web-tree-sitter";

async function initJavaParser() {
  await Parser.init();
  const parser = new Parser();
  const Java = await Parser.Language.load("/tree-sitter-java.wasm");
  parser.setLanguage(Java);
  return parser;
}

function parseJavaCode(parser, sourceCode) {
  const tree = parser.parse(sourceCode);
  return tree.rootNode;
}
```

**Ưu điểm:**

- Zero latency (client-side)
- Không cần backend infrastructure
- Real-time parsing
- Hỗ trợ incremental updates
- Mature tree-sitter ecosystem

**Nhược điểm:**

- Limited semantic analysis
- WASM file size (2-5MB)
- Browser compatibility requirements

### 3.3 Phương Án 3: Hybrid Approach

**Kiến trúc:**

```
Tree-sitter (syntax) + Microservice (semantics) + LLM
```

**Chi tiết implementation:**

- **Phase 1**: Tree-sitter WASM cho syntax parsing
- **Phase 2**: Microservice cho semantic analysis khi cần
- **Optimization**: Chỉ gọi semantic service khi LLM yêu cầu deep analysis

### 3.4 Phương Án 4: Language Server Protocol (LSP)

**Kiến trúc:**

```
PCM WebApp → WebSocket → Java LSP Server → AST Analysis
```

**Chi tiết implementation:**

- **LSP Server**: Eclipse JDT.LS hoặc custom Java LSP
- **WebSocket**: Real-time communication
- **Features**: Code completion, diagnostics, symbol resolution

## 4. Đánh Giá và Khuyến Nghị

### 4.1 Ma Trận Đánh Giá

| Tiêu chí             | Microservice | Tree-sitter WASM | Hybrid | LSP   |
|----------------------|--------------|------------------|--------|-------|
| **Độ chính xác AST** | ⭐⭐⭐⭐⭐        | ⭐⭐⭐              | ⭐⭐⭐⭐   | ⭐⭐⭐⭐⭐ |
| **Performance**      | ⭐⭐⭐          | ⭐⭐⭐⭐⭐            | ⭐⭐⭐⭐   | ⭐⭐⭐   |
| **Complexity**       | ⭐⭐           | ⭐⭐⭐⭐             | ⭐⭐     | ⭐⭐    |
| **Scalability**      | ⭐⭐⭐⭐⭐        | ⭐⭐⭐              | ⭐⭐⭐⭐   | ⭐⭐⭐⭐  |
| **LLM Integration**  | ⭐⭐⭐⭐         | ⭐⭐⭐⭐             | ⭐⭐⭐⭐⭐  | ⭐⭐⭐⭐  |
| **Maintenance**      | ⭐⭐⭐          | ⭐⭐⭐⭐             | ⭐⭐     | ⭐⭐⭐   |

### 4.2 Ma Trận Đánh Giá Cập Nhật (Bao Gồm Multi-Language)

| Tiêu chí             | Microservice | Tree-sitter Universal | Hybrid Multi-Lang | LSP   |
|----------------------|--------------|-----------------------|-------------------|-------|
| **Độ chính xác AST** | ⭐⭐⭐⭐⭐        | ⭐⭐⭐⭐                  | ⭐⭐⭐⭐⭐             | ⭐⭐⭐⭐⭐ |
| **Multi-Language**   | ⭐⭐⭐          | ⭐⭐⭐⭐⭐                 | ⭐⭐⭐⭐⭐             | ⭐⭐⭐   |
| **Performance**      | ⭐⭐⭐          | ⭐⭐⭐⭐⭐                 | ⭐⭐⭐⭐              | ⭐⭐⭐   |
| **Complexity**       | ⭐⭐           | ⭐⭐⭐⭐                  | ⭐⭐⭐               | ⭐⭐    |
| **Scalability**      | ⭐⭐⭐⭐⭐        | ⭐⭐⭐⭐                  | ⭐⭐⭐⭐⭐             | ⭐⭐⭐⭐  |
| **LLM Integration**  | ⭐⭐⭐⭐         | ⭐⭐⭐⭐⭐                 | ⭐⭐⭐⭐⭐             | ⭐⭐⭐⭐  |
| **Maintenance**      | ⭐⭐⭐          | ⭐⭐⭐⭐⭐                 | ⭐⭐⭐               | ⭐⭐⭐   |
| **Future-Proof**     | ⭐⭐⭐          | ⭐⭐⭐⭐⭐                 | ⭐⭐⭐⭐              | ⭐⭐⭐   |

### 4.3 Khuyến Nghị Chính

**Phương án được khuyến nghị: Tree-sitter Universal Multi-Language Parser**

**Lý do:**

1. **Multi-Language Excellence**: Hỗ trợ hoàn hảo cho Java, HTML, JS, JSP và XFDL
2. **Zero latency**: Client-side parsing không có network delay
3. **Extensible Architecture**: Dễ dàng thêm ngôn ngữ mới trong tương lai
4. **Proven ecosystem**: 165+ ngôn ngữ được hỗ trợ với WebAssembly
5. **Performance tối ưu**: Lazy loading và caching thông minh
6. **Future-ready**: WebAssembly và incremental parsing
7. **Cost-effective**: Không cần infrastructure backend phức tạp
8. **Developer experience**: Kiến trúc modular dễ maintain

### 4.4 Implementation Plan Multi-Language

#### Phase 1: Core Universal Parser Setup (2-3 tuần)

```javascript
// File: public/js/modules/ast/index.js
import { MultiLanguageProcessor } from "./MultiLanguageProcessor.js";
import { ASTPerformanceOptimizer } from "./PerformanceOptimizer.js";
import { UniversalASTParser } from "./UniversalASTParser.js";

export class ASTManager {
  constructor() {
    this.universalParser = new UniversalASTParser();
    this.multiProcessor = new MultiLanguageProcessor(this.universalParser);
    this.optimizer = new ASTPerformanceOptimizer();
  }

  async initialize() {
    // Initialize with core languages first
    const coreLanguages = ["java", "javascript", "html", "xml"];
    await this.universalParser.initialize(coreLanguages);
  }

  async parseFile(content, filename) {
    const language = this.universalParser.detectLanguage(filename, content);

    // Check cache first
    const cached = this.optimizer.getCachedAST(content, language);
    if (cached) return cached;

    // Load language on demand
    await this.optimizer.loadLanguageOnDemand(language, this.universalParser);

    // Process multi-language files
    const result = await this.multiProcessor.processMultiLanguageFile(
      content,
      filename,
    );

    // Cache result
    this.optimizer.setCachedAST(content, language, result);

    return result;
  }
}
```

#### Phase 2: Integration với AI Module (2-3 tuần)

- Tích hợp ASTManager vào `public/js/modules/ai/`
- Enhanced LLM prompts với multi-language AST context
- Context-aware code suggestions cho từng ngôn ngữ

#### Phase 3: Advanced Multi-Language Features (3-4 tuần)

- JSP section analysis và Java code extraction
- HTML embedded JavaScript parsing
- XFDL form structure analysis
- Cross-language symbol references

## 5. Lợi Ích Cho LLM

### 5.1 Enhanced Code Understanding

```json
{
  "source": "public class Calculator { public int add(int a, int b) { return a + b; } }",
  "ast": {
    "type": "program",
    "children": [
      {
        "type": "class_declaration",
        "name": "Calculator",
        "methods": [
          {
            "type": "method_declaration",
            "name": "add",
            "parameters": ["int a", "int b"],
            "return_type": "int",
            "body": "return a + b;"
          }
        ]
      }
    ]
  }
}
```

### 5.2 Structured Prompting

- LLM nhận structured AST thay vì raw text
- Tách biệt syntax và semantic concerns
- Context-aware code suggestions

### 5.3 Improved Accuracy

- Giảm hallucination trong code generation
- Syntax-aware modifications
- Semantic consistency checking

## 6. Lộ Trình Mở Rộng Tương Lai

### 6.1 Ngôn Ngữ Bổ Sung

**Ưu tiên cao:**

- **CSS**: `tree-sitter/tree-sitter-css` (official)
- **SQL**: `tree-sitter-grammars/tree-sitter-sql`
- **JSON**: `tree-sitter/tree-sitter-json` (official)

**Ưu tiên trung bình:**

- **Python**: `tree-sitter/tree-sitter-python` (cho scripting)
- **Bash**: `tree-sitter/tree-sitter-bash` (cho scripts)
- **YAML**: `tree-sitter-grammars/tree-sitter-yaml`

### 6.2 Custom Grammar Development

**XFDL Grammar Development:**

- Tạo custom tree-sitter grammar cho XFDL
- Extend từ XML grammar với XFDL-specific nodes
- Support form validation và structure analysis

### 6.3 Advanced Features Roadmap

**Q1 2025:**

- Multi-language code completion
- Cross-language symbol resolution
- AST-based refactoring suggestions

**Q2 2025:**

- Real-time syntax error detection
- Semantic code analysis
- Custom rule engines cho code quality

## 7. Kết Luận

Tree-sitter Universal Multi-Language Parser là phương án tối ưu nhất cho PCM WebApp với:

- **Multi-language excellence** cho Java, HTML, JS, JSP và XFDL
- **Performance tối ưu** với client-side processing và caching
- **Extensible architecture** cho tương lai mở rộng
- **Future-proof** technology stack với WebAssembly
- **Cost-effective** solution không cần backend phức tạp
- **Developer experience** tuyệt vời với kiến trúc modular

Phương án này không chỉ nâng cao khả năng hiểu source code Java mà còn mở ra tiềm năng phân tích đa ngôn ngữ toàn diện
cho LLM trong PCM WebApp, tạo nền tảng vững chắc cho việc phát triển các tính năng AI tiên tiến trong tương lai.

---

_Tài liệu được tạo cho dự án PCM WebApp - Java AST Integration Research_  
_Ngày tạo: 2025-11-09_  
_Phiên bản: 1.0_
