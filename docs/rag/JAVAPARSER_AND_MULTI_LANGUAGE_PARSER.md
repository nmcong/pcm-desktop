# 📖 JavaParser & Multi-Language Parser Guide

> **Ngày tạo:** 15/11/2025  
> **Mục đích:** Tài liệu về JavaParser và các phương án parser cho JSP, JavaScript, HTML, XFDL

---

## 📋 Mục Lục

1. [JavaParser - Phân Tích Chi Tiết](#1-javaparser---phân-tích-chi-tiết)
2. [JavaParser trong PCM Desktop](#2-javaparser-trong-pcm-desktop)
3. [JSP Parser - Java Server Pages](#3-jsp-parser---java-server-pages)
4. [JavaScript Parser](#4-javascript-parser)
5. [HTML Parser](#5-html-parser)
6. [XFDL Parser - Extensible Forms Description Language](#6-xfdl-parser---extensible-forms-description-language)
7. [Implementation Strategy](#7-implementation-strategy)
8. [Comparison Matrix](#8-comparison-matrix)
9. [Recommended Architecture](#9-recommended-architecture)

---

## 1. JavaParser - Phân Tích Chi Tiết

### 📚 Giới Thiệu

**JavaParser** là một thư viện Java mạnh mẽ để parse, analyze và manipulate Java source code.

- **Website:** https://javaparser.org/
- **GitHub:** https://github.com/javaparser/javaparser
- **License:** Apache 2.0 hoặc LGPL 3.0+
- **Version hiện tại:** 3.26.3 (trong project)

### 🎯 Chức Năng Chính

#### 1. **Source Code Parsing**
- Parse Java source code thành Abstract Syntax Tree (AST)
- Support Java 17+ syntax (records, sealed classes, pattern matching, etc.)
- Xử lý incomplete hoặc invalid code một cách graceful

#### 2. **AST Navigation & Analysis**
- Traverse AST bằng Visitor pattern
- Query nodes với XPath-like queries
- Extract thông tin về classes, methods, fields, annotations

#### 3. **Symbol Resolution**
- Resolve types, methods, variables
- Understand imports và dependencies
- Link method calls đến declarations (qua symbol-solver)

#### 4. **Code Generation & Transformation**
- Modify existing AST
- Generate new code programmatically
- Preserve formatting và comments

### 📦 Thư Viện Liên Quan

#### `javaparser-core-3.26.3.jar` (~2.5MB)
**Chức năng:**
- Core parsing engine
- AST representation
- Visitor pattern implementation
- Basic analysis capabilities

**Khi nào dùng:**
- Parse Java code
- Navigate AST structure
- Extract basic information (classes, methods, fields)

**Ví dụ trong project:**
```java
JavaParser javaParser = new JavaParser();
ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
CompilationUnit cu = result.getResult().get();
```

#### `javaparser-symbol-solver-core-3.26.3.jar` (~500KB)
**Chức năng:**
- Advanced type resolution
- Resolve fully qualified names
- Understand inheritance hierarchies
- Link method calls to declarations

**Khi nào dùng:**
- Cần resolve types chính xác
- Analyze dependencies giữa classes
- Build call graphs
- Code refactoring tools

**Ví dụ use cases:**
- "Find all callers of method X"
- "What's the actual type of this variable?"
- "List all implementations of interface Y"

### 🔍 AST Structure

JavaParser tổ chức code thành cây phân cấp:

```
CompilationUnit (root)
├── PackageDeclaration
│   └── Name
├── ImportDeclaration[]
│   └── Name
└── TypeDeclaration[]
    ├── ClassOrInterfaceDeclaration
    │   ├── SimpleName
    │   ├── Modifiers
    │   ├── ExtendedTypes
    │   ├── ImplementedTypes
    │   └── Members[]
    │       ├── FieldDeclaration
    │       ├── MethodDeclaration
    │       │   ├── Parameters
    │       │   ├── Type
    │       │   └── BlockStmt
    │       │       └── Statements[]
    │       └── ConstructorDeclaration
    └── EnumDeclaration
```

### 💡 Core Concepts

#### 1. **CompilationUnit**
- Đại diện cho một Java source file
- Root node của AST
- Chứa package, imports, type declarations

#### 2. **Node**
- Base class cho tất cả AST nodes
- Có position information (line, column)
- Support visitor pattern

#### 3. **Visitor Pattern**
```java
// VoidVisitor - không return value
public class MyVisitor extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(MethodDeclaration n, Void arg) {
        System.out.println("Found method: " + n.getNameAsString());
        super.visit(n, arg); // Continue traversal
    }
}

// GenericVisitor - return values
public class MyCollector extends GenericVisitorAdapter<List<String>, Void> {
    @Override
    public List<String> visit(MethodDeclaration n, Void arg) {
        List<String> result = new ArrayList<>();
        result.add(n.getNameAsString());
        return result;
    }
}
```

#### 4. **Range & Position**
Mỗi node có thông tin vị trí:
```java
Optional<Range> range = node.getRange();
if (range.isPresent()) {
    Range r = range.get();
    int startLine = r.begin.line;
    int startCol = r.begin.column;
    int endLine = r.end.line;
    int endCol = r.end.column;
}
```

### 🛠️ Common Operations

#### Parse Code
```java
JavaParser parser = new JavaParser();
ParseResult<CompilationUnit> result = parser.parse(sourceCode);

if (result.isSuccessful()) {
    CompilationUnit cu = result.getResult().get();
    // Use cu...
} else {
    System.out.println("Errors: " + result.getProblems());
}
```

#### Extract Package
```java
cu.getPackageDeclaration().ifPresent(pkg -> {
    String packageName = pkg.getNameAsString();
});
```

#### Find All Classes
```java
List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
for (ClassOrInterfaceDeclaration cls : classes) {
    String name = cls.getNameAsString();
    boolean isInterface = cls.isInterface();
}
```

#### Find All Methods
```java
List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
for (MethodDeclaration method : methods) {
    String name = method.getNameAsString();
    String returnType = method.getTypeAsString();
    int paramCount = method.getParameters().size();
}
```

#### Walk AST
```java
cu.walk(node -> {
    System.out.println(node.getClass().getSimpleName());
});
```

### ⚡ Performance Considerations

#### Memory
- AST có thể lớn (1 line code ≈ nhiều AST nodes)
- Large files (>5000 lines) có thể tốn nhiều memory
- Consider streaming hoặc selective parsing

#### Speed
- Parsing nhanh (~1000 lines/ms trên hardware hiện đại)
- Symbol resolution chậm hơn (cần resolve dependencies)
- Cache results khi có thể

#### Best Practices
```java
// ✅ Good - Cache parser nếu parse nhiều files
JavaParser parser = new JavaParser();
for (String code : codes) {
    parser.parse(code);
}

// ❌ Bad - Tạo parser mới mỗi lần
for (String code : codes) {
    new JavaParser().parse(code);
}

// ✅ Good - Specific visitor cho task cụ thể
public class MethodNameCollector extends VoidVisitorAdapter<List<String>> {
    @Override
    public void visit(MethodDeclaration n, List<String> collector) {
        collector.add(n.getNameAsString());
        // Don't call super.visit() if you don't need to go deeper
    }
}

// ❌ Bad - Walk toàn bộ tree khi chỉ cần methods
cu.walk(node -> {
    if (node instanceof MethodDeclaration) {
        // process...
    }
});
```

---

## 2. JavaParser trong PCM Desktop

### 🏗️ Architecture

Project sử dụng JavaParser trong 2 modules:

#### 1. **AST Module** (`com.noteflix.pcm.ast`)
**Mục đích:** Deep code analysis cho AI features

**Components:**
- `ASTParser` - Basic parsing và analysis
- `EnhancedASTAnalyzer` - Advanced analysis với metadata
- `ASTService` - Service layer với caching

**Use Cases:**
- Code understanding cho AI assistant
- Generate code representations cho LLM
- Extract method signatures cho function calling
- Calculate code metrics (complexity, LOC)

#### 2. **RAG Parser** (`com.noteflix.pcm.rag.parser.core.JavaParser`)
**Mục đích:** Parse Java files cho RAG indexing

**Use Cases:**
- Index Java source code vào vector store
- Enable semantic search trong codebase
- Extract code snippets cho context

### 📊 Current Usage

#### ASTParser.java
```java
// Simple parsing
Optional<CompilationUnit> cu = parseJavaCode(code);

// Basic analysis
ASTAnalysisResult result = analyzeJavaCode(code);
// Returns: package, imports, classes, methods

// Get AST as string
String astStr = getASTAsString(code);

// Get all nodes
List<Node> nodes = getAllNodes(code);
```

#### EnhancedASTAnalyzer.java
```java
// Comprehensive analysis
Optional<CodeMetadata> metadata = analyzeFile(filePath);

// Returns detailed info:
// - File metadata (size, lines, last modified)
// - Package & imports
// - Classes with fields, methods, constructors
// - Interfaces with methods, constants
// - Code metrics (LOC, comments, blank lines)
// - Method flow analysis (control flow, calls, variables)
// - Cyclomatic complexity
```

#### ASTService.java
```java
// High-level service
ASTAnalysisResult result = analyzeJavaFile(filePath);

// With caching
Map<String, Object> projectAnalysis = getProjectStructureAnalysis(rootPath);

// JSON export
String json = getAnalysisAsJson(code);
```

### 🎯 Features Implemented

#### ✅ Currently Available
1. **Package & Import Extraction**
2. **Class/Interface Detection**
   - Name, modifiers (public, abstract, final)
   - Superclass & implemented interfaces
   - Inner classes
   - Annotations
3. **Method Analysis**
   - Signature, return type, parameters
   - Modifiers, annotations, Javadoc
   - Control flow (if, while, for)
   - Method calls & field accesses
   - Local variables
   - Try-catch blocks
   - Cyclomatic complexity
4. **Field Analysis**
   - Type, modifiers, initial value
   - Annotations, Javadoc
5. **Constructor Analysis**
6. **Code Metrics**
   - Total lines, LOC, comments, blank lines
   - Method count, field count

#### 🚀 Potential Enhancements
1. **Symbol Resolution** (using symbol-solver)
   - Resolve fully qualified names
   - Build dependency graphs
   - Find all implementations/callers
2. **Data Flow Analysis**
   - Track variable usage
   - Find unused code
   - Detect potential NPEs
3. **Code Smells Detection**
   - Long methods
   - God classes
   - Duplicate code
4. **Refactoring Suggestions**
   - Extract method opportunities
   - Simplify conditions
5. **Test Coverage Mapping**

### 💾 Storage Strategy

**CodeMetadata** được store vào:
- SQLite database (via `CodeMetadataStorage`)
- Indexed với file path
- Serialized as JSON

**Benefits:**
- Fast retrieval cho analyzed files
- Avoid re-parsing unchanged files
- Enable project-wide queries
- Support AI context building

---

## 3. JSP Parser - Java Server Pages

### 📚 Tổng Quan

**JSP** (Java Server Pages) là technology để create dynamic web pages bằng cách embed Java code trong HTML.

**Đặc điểm:**
- Mix HTML, XML, và Java code
- JSP tags: `<% %>`, `<%= %>`, `<%@ %>`
- JSTL (JSP Standard Tag Library)
- Expression Language (EL): `${...}`
- Custom tags

**Ví dụ từ project:**
```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>${pageTitle}</title>
</head>
<body>
    <c:forEach items="${employees}" var="emp">
        <div>
            <h3>${emp.name}</h3>
            <p>${emp.email}</p>
        </div>
    </c:forEach>
</body>
</html>
```

### 🔧 Parser Options

#### Option 1: **Jsoup** ⭐⭐⭐⭐⭐ RECOMMENDED
**Library:** `org.jsoup:jsoup:1.17.2`

**Pros:**
- ✅ Best HTML/XML parser cho Java
- ✅ Robust error handling (handles malformed HTML)
- ✅ CSS selectors support
- ✅ Easy to use API
- ✅ No external dependencies
- ✅ Active maintenance
- ✅ ~500KB

**Cons:**
- ❌ Không parse Java code trong JSP
- ❌ Không understand JSP tags semantics

**Best For:**
- Extract HTML structure
- Get text content
- Find specific elements
- Clean/sanitize HTML

**Maven:**
```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
```

**Example Usage:**
```java
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// Parse JSP as HTML
Document doc = Jsoup.parse(jspContent);

// Extract text
String text = doc.text();

// Find elements
Elements forms = doc.select("form");
Elements tables = doc.select("table");

// Extract JSTL variables
Elements jstlVars = doc.select("[*^=\\$]"); // Elements with ${...}
```

#### Option 2: **Custom Regex Parser** ⭐⭐⭐
**Approach:** Pattern matching cho JSP-specific constructs

**Pros:**
- ✅ No dependencies
- ✅ Lightweight
- ✅ Fast cho simple cases
- ✅ Full control

**Cons:**
- ❌ Fragile (break với complex JSP)
- ❌ Hard to maintain
- ❌ Không handle nested structures well

**Best For:**
- Extract JSP directives (`<%@ %>`)
- Find scriptlets (`<% %>`)
- Extract EL expressions (`${...}`)

**Example:**
```java
public class JSPParser {
    private static final Pattern DIRECTIVE = Pattern.compile("<%@\\s*(\\w+)([^%]*)%>");
    private static final Pattern SCRIPTLET = Pattern.compile("<%([^%@].*?)%>", Pattern.DOTALL);
    private static final Pattern EXPRESSION = Pattern.compile("<%=([^%]+)%>");
    private static final Pattern EL = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Pattern JSTL_TAG = Pattern.compile("<([c|fmt|fn]:[\\w]+)([^>]*)>");

    public JSPMetadata parse(String jsp) {
        // Extract directives
        Matcher m = DIRECTIVE.matcher(jsp);
        while (m.find()) {
            String directiveType = m.group(1); // page, taglib, include
            String attributes = m.group(2);
        }

        // Extract EL expressions
        m = EL.matcher(jsp);
        while (m.find()) {
            String expression = m.group(1); // ${emp.name}
        }

        // Continue for other patterns...
    }
}
```

#### Option 3: **Jasper** ⭐⭐
**Library:** Apache Tomcat's JSP engine

**Pros:**
- ✅ Official JSP compiler
- ✅ Complete JSP understanding
- ✅ Can generate Java from JSP

**Cons:**
- ❌ Heavy dependency (~5-10MB)
- ❌ Complex API
- ❌ Overkill cho parsing only
- ❌ Requires Tomcat libraries

**Best For:**
- Validate JSP syntax
- Generate Java servlet code
- Deploy JSP applications

**Not Recommended For:** Static analysis/parsing

#### Option 4: **DOM4J + XPath** ⭐⭐⭐
**Library:** `org.dom4j:dom4j:2.1.4`

**Pros:**
- ✅ Powerful XML parsing
- ✅ XPath queries
- ✅ Good for structured XML-like JSP

**Cons:**
- ❌ Strict XML requirements (break với malformed HTML)
- ❌ Không handle JSP directives
- ❌ Less user-friendly than Jsoup

**Best For:**
- JSP với valid XML structure
- JSPX (JSP XML syntax)

### 🎯 Recommended Approach

**Hybrid Strategy:**

```java
public class JSPParser implements DocumentParser {
    
    @Override
    public RAGDocument parse(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        
        // 1. Use Jsoup for HTML structure
        Document doc = Jsoup.parse(content);
        String textContent = doc.text();
        
        // 2. Use regex for JSP-specific constructs
        List<String> directives = extractDirectives(content);
        List<String> scriptlets = extractScriptlets(content);
        List<String> elExpressions = extractEL(content);
        List<String> jstlTags = extractJSTL(content);
        
        // 3. Build RAGDocument
        RAGDocument rag = RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(DocumentType.JSP)
            .title(filePath.getFileName().toString())
            .sourcePath(filePath.toString())
            .content(textContent) // Clean text for embedding
            .rawContent(content)  // Original for display
            .indexedAt(LocalDateTime.now())
            .build();
        
        // 4. Add metadata
        rag.addMetadata("language", "jsp");
        rag.addMetadata("directives", directives);
        rag.addMetadata("scriptletCount", scriptlets.size());
        rag.addMetadata("elExpressions", elExpressions);
        rag.addMetadata("jstlTags", jstlTags);
        
        return rag;
    }
    
    private List<String> extractDirectives(String jsp) {
        List<String> result = new ArrayList<>();
        Pattern p = Pattern.compile("<%@\\s*(\\w+)([^%]*)%>");
        Matcher m = p.matcher(jsp);
        while (m.find()) {
            result.add(m.group(0));
        }
        return result;
    }
    
    // Similar methods for scriptlets, EL, JSTL...
}
```

---

## 4. JavaScript Parser

### 📚 Tổng Quan

JavaScript là scripting language phổ biến cho web development.

**Use Cases trong Project:**
- Frontend logic trong JSP pages
- Standalone `.js` files
- Embedded scripts trong HTML

### 🔧 Parser Options

#### Option 1: **Nashorn** ⭐⭐ (Legacy)
**Built-in:** Java's JavaScript engine (deprecated in Java 15, removed in Java 17)

**Status:** ❌ **NOT AVAILABLE** - Project uses Java 21

#### Option 2: **GraalVM JavaScript** ⭐⭐⭐⭐
**Library:** `org.graalvm.js:js:23.1.1` + `org.graalvm.js:js-scriptengine:23.1.1`

**Pros:**
- ✅ Modern ES6+ support
- ✅ High performance
- ✅ ECMAScript 2023 compliant
- ✅ Can execute code
- ✅ Active development

**Cons:**
- ❌ Large dependency (~30MB+)
- ❌ Complex setup
- ❌ Overkill cho static parsing

**Best For:**
- Execute JavaScript code
- Full ES6+ compatibility needed
- Dynamic analysis

**Maven:**
```xml
<dependency>
    <groupId>org.graalvm.js</groupId>
    <artifactId>js</artifactId>
    <version>23.1.1</version>
</dependency>
```

#### Option 3: **Rhino** ⭐⭐⭐
**Library:** `org.mozilla:rhino:1.7.15`

**Pros:**
- ✅ Mature và stable
- ✅ Reasonable size (~1.5MB)
- ✅ Pure Java
- ✅ Easy to use
- ✅ AST access available

**Cons:**
- ❌ ES5 primarily (limited ES6)
- ❌ Slower than modern engines
- ❌ Less active development

**Best For:**
- Parse ES5 JavaScript
- Simple AST analysis
- Lightweight solution

**Maven:**
```xml
<dependency>
    <groupId>org.mozilla</groupId>
    <artifactId>rhino</artifactId>
    <version>1.7.15</version>
</dependency>
```

**Example:**
```java
import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.*;

Context cx = Context.enter();
try {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    
    Parser parser = new Parser(env);
    AstRoot ast = parser.parse(jsCode, "script.js", 1);
    
    // Visit AST
    ast.visit(node -> {
        if (node instanceof FunctionNode) {
            FunctionNode fn = (FunctionNode) node;
            String name = fn.getName();
            // Process function...
        }
        return true; // Continue visiting
    });
} finally {
    Context.exit();
}
```

#### Option 4: **Regex-Based Parser** ⭐⭐⭐⭐ RECOMMENDED
**For RAG Use Case**

**Pros:**
- ✅ No dependencies
- ✅ Very fast
- ✅ Simple implementation
- ✅ Good enough for text extraction

**Cons:**
- ❌ Not a true parser
- ❌ Can't handle complex syntax
- ❌ No AST

**Best For:**
- Extract functions, variables, comments
- Get text content cho RAG
- Lightweight analysis

**Example:**
```java
public class JavaScriptParser implements DocumentParser {
    
    private static final Pattern FUNCTION = 
        Pattern.compile("function\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern ARROW_FUNCTION = 
        Pattern.compile("(const|let|var)\\s+(\\w+)\\s*=\\s*\\(([^)]*)\\)\\s*=>");
    private static final Pattern VARIABLE = 
        Pattern.compile("(const|let|var)\\s+(\\w+)");
    private static final Pattern COMMENT_SINGLE = 
        Pattern.compile("//(.*)$", Pattern.MULTILINE);
    private static final Pattern COMMENT_MULTI = 
        Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    
    @Override
    public RAGDocument parse(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        
        // Extract functions
        List<String> functions = new ArrayList<>();
        Matcher m = FUNCTION.matcher(content);
        while (m.find()) {
            String name = m.group(1);
            String params = m.group(2);
            functions.add(name + "(" + params + ")");
        }
        
        // Extract arrow functions
        m = ARROW_FUNCTION.matcher(content);
        while (m.find()) {
            String name = m.group(2);
            String params = m.group(3);
            functions.add(name + "(" + params + ")");
        }
        
        // Remove comments for clean text
        String cleanContent = content;
        cleanContent = COMMENT_MULTI.matcher(cleanContent).replaceAll("");
        cleanContent = COMMENT_SINGLE.matcher(cleanContent).replaceAll("");
        
        RAGDocument doc = RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(DocumentType.JAVASCRIPT)
            .title(filePath.getFileName().toString())
            .sourcePath(filePath.toString())
            .content(cleanContent)
            .rawContent(content)
            .indexedAt(LocalDateTime.now())
            .build();
        
        doc.addMetadata("language", "javascript");
        doc.addMetadata("functions", functions);
        doc.addMetadata("functionCount", functions.size());
        
        return doc;
    }
}
```

#### Option 5: **Node.js + Babel Parser** ⭐⭐
**External Tool**

**Approach:** Use Node.js subprocess to parse with Babel

**Pros:**
- ✅ Perfect JavaScript parsing
- ✅ Full ES2024 support
- ✅ TypeScript support

**Cons:**
- ❌ Requires Node.js installed
- ❌ Slower (subprocess overhead)
- ❌ Complex integration

**Not Recommended:** Too complex for the value

### 🎯 Recommendation

**For PCM Desktop RAG Use Case:**

Use **Regex-Based Parser** because:
1. ✅ No additional dependencies
2. ✅ Fast và simple
3. ✅ Adequate cho text extraction
4. ✅ Easy to maintain
5. ✅ Works with embedded JS trong JSP

If need proper AST analysis later, consider **Rhino**.

---

## 5. HTML Parser

### 📚 Tổng Quan

HTML (HyperText Markup Language) là markup language cho web pages.

### 🔧 Parser Options

#### Option 1: **Jsoup** ⭐⭐⭐⭐⭐ RECOMMENDED
**Library:** `org.jsoup:jsoup:1.17.2`

**Pros:**
- ✅ Industry standard cho HTML parsing trong Java
- ✅ Best-in-class error handling
- ✅ Clean API với jQuery-like selectors
- ✅ Handles malformed HTML gracefully
- ✅ ~500KB, no dependencies
- ✅ Active development
- ✅ Whitelist-based cleaning
- ✅ XSS prevention

**Cons:**
- (None significant)

**Features:**
- Parse HTML từ String, File, URL
- CSS selectors: `doc.select("div.class")`
- DOM manipulation
- Extract text, attributes, structure
- Clean unsafe HTML

**Maven:**
```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
```

**Example:**
```java
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// Parse HTML
Document doc = Jsoup.parse(htmlContent);

// Extract title
String title = doc.title();

// Get meta tags
Elements metaTags = doc.select("meta");
for (Element meta : metaTags) {
    String name = meta.attr("name");
    String content = meta.attr("content");
}

// Get all text
String text = doc.text();

// Get body text only
String bodyText = doc.body().text();

// Find specific elements
Elements headers = doc.select("h1, h2, h3");
Elements links = doc.select("a[href]");
Elements images = doc.select("img[src]");

// Get structured text
for (Element header : headers) {
    System.out.println(header.tagName() + ": " + header.text());
}
```

#### Option 2: **NekoHTML** ⭐⭐⭐
**Library:** `net.sourceforge.nekohtml:nekohtml:1.9.22`

**Pros:**
- ✅ Lenient HTML parser
- ✅ Converts HTML to well-formed XML
- ✅ Good cho malformed HTML

**Cons:**
- ❌ Less intuitive API
- ❌ Older project (less active)
- ❌ Jsoup is better trong most cases

**Best For:**
- Legacy projects đã dùng NekoHTML
- Need strict XML output

#### Option 3: **JTidy** ⭐⭐
**Library:** `net.sf.jtidy:jtidy:r938`

**Pros:**
- ✅ Port of HTML Tidy
- ✅ Clean/validate HTML

**Cons:**
- ❌ Old project (not maintained)
- ❌ Limited features vs Jsoup
- ❌ Less robust error handling

**Not Recommended:** Use Jsoup instead

### 🎯 Implementation

```java
public class HTMLParser implements DocumentParser {
    
    @Override
    public boolean canParse(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".html") || fileName.endsWith(".htm");
    }
    
    @Override
    public RAGDocument parse(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        
        // Parse with Jsoup
        Document doc = Jsoup.parse(content);
        
        // Extract metadata
        String title = doc.title();
        String description = doc.select("meta[name=description]").attr("content");
        String keywords = doc.select("meta[name=keywords]").attr("content");
        
        // Extract structured text
        StringBuilder textBuilder = new StringBuilder();
        
        // Add title
        if (!title.isEmpty()) {
            textBuilder.append(title).append("\n\n");
        }
        
        // Add headers with hierarchy
        for (Element header : doc.select("h1, h2, h3, h4, h5, h6")) {
            String level = header.tagName();
            textBuilder.append(level.toUpperCase())
                      .append(": ")
                      .append(header.text())
                      .append("\n");
        }
        
        // Add paragraphs
        for (Element p : doc.select("p")) {
            textBuilder.append(p.text()).append("\n\n");
        }
        
        // Add lists
        for (Element list : doc.select("ul, ol")) {
            for (Element item : list.select("li")) {
                textBuilder.append("- ").append(item.text()).append("\n");
            }
            textBuilder.append("\n");
        }
        
        String cleanText = textBuilder.toString().trim();
        
        // Build RAGDocument
        RAGDocument rag = RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(DocumentType.HTML)
            .title(title.isEmpty() ? filePath.getFileName().toString() : title)
            .sourcePath(filePath.toString())
            .content(cleanText)
            .rawContent(content)
            .indexedAt(LocalDateTime.now())
            .build();
        
        // Add metadata
        rag.addMetadata("language", "html");
        rag.addMetadata("title", title);
        if (!description.isEmpty()) {
            rag.addMetadata("description", description);
        }
        if (!keywords.isEmpty()) {
            rag.addMetadata("keywords", keywords);
        }
        
        // Extract links
        Elements links = doc.select("a[href]");
        List<String> hrefs = links.stream()
            .map(link -> link.attr("href"))
            .collect(Collectors.toList());
        rag.addMetadata("links", hrefs);
        rag.addMetadata("linkCount", hrefs.size());
        
        return rag;
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[] {".html", ".htm"};
    }
}
```

---

## 6. XFDL Parser - Extensible Forms Description Language

### 📚 Tổng Quan

**XFDL** là XML-based language để định nghĩa electronic forms.

**Đặc điểm:**
- Based on XML
- Used trong government, military, healthcare
- Complex form logic và validation
- Digital signatures support
- Originated from PureEdge (now IBM)

**Ví dụ XFDL:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<XFDL xmlns="http://www.xfdl.org/2004/xfdl">
    <page sid="PAGE1">
        <field sid="FIELD1">
            <type>text</type>
            <label>Full Name</label>
            <value></value>
            <compute>
                <expression>FIELD2 + " " + FIELD3</expression>
            </compute>
        </field>
        <field sid="FIELD2">
            <type>text</type>
            <label>First Name</label>
        </field>
    </page>
</XFDL>
```

### 🔧 Parser Options

#### Option 1: **Standard XML Parsers** ⭐⭐⭐⭐⭐ RECOMMENDED
**Built-in:** Java's DOM/SAX/StAX parsers

**Pros:**
- ✅ No dependencies (built into Java)
- ✅ Standard XML parsing
- ✅ Well documented
- ✅ Multiple APIs (DOM, SAX, StAX)

**Cons:**
- ❌ Không hiểu XFDL semantics
- ❌ Cần manually interpret structure

**Best For:**
- Parse XFDL as XML
- Extract fields, values, labels
- Get form structure

**Example with DOM:**
```java
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class XFDLParser implements DocumentParser {
    
    @Override
    public RAGDocument parse(Path filePath) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document doc = builder.parse(filePath.toFile());
            doc.getDocumentElement().normalize();
            
            // Extract fields
            NodeList fields = doc.getElementsByTagName("field");
            List<XFDLField> xfdlFields = new ArrayList<>();
            
            for (int i = 0; i < fields.getLength(); i++) {
                Element field = (Element) fields.item(i);
                
                String sid = field.getAttribute("sid");
                String type = getElementText(field, "type");
                String label = getElementText(field, "label");
                String value = getElementText(field, "value");
                
                xfdlFields.add(new XFDLField(sid, type, label, value));
            }
            
            // Build text representation
            StringBuilder text = new StringBuilder();
            text.append("XFDL Form\n\n");
            
            for (XFDLField field : xfdlFields) {
                text.append(field.label)
                    .append(" (").append(field.type).append(")")
                    .append(": ").append(field.value)
                    .append("\n");
            }
            
            RAGDocument rag = RAGDocument.builder()
                .id(UUID.randomUUID().toString())
                .type(DocumentType.XFDL)
                .title(filePath.getFileName().toString())
                .sourcePath(filePath.toString())
                .content(text.toString())
                .rawContent(Files.readString(filePath))
                .indexedAt(LocalDateTime.now())
                .build();
            
            rag.addMetadata("format", "xfdl");
            rag.addMetadata("fieldCount", xfdlFields.size());
            rag.addMetadata("fields", xfdlFields.stream()
                .map(f -> f.sid + ":" + f.label)
                .collect(Collectors.toList()));
            
            return rag;
            
        } catch (Exception e) {
            throw new IOException("Failed to parse XFDL", e);
        }
    }
    
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[] {".xfdl", ".xfd"};
    }
    
    private static class XFDLField {
        String sid;
        String type;
        String label;
        String value;
        
        XFDLField(String sid, String type, String label, String value) {
            this.sid = sid;
            this.type = type;
            this.label = label;
            this.value = value;
        }
    }
}
```

**Example with StAX (streaming):**
```java
import javax.xml.stream.*;

public class XFDLStreamParser {
    
    public List<XFDLField> parseFields(Path filePath) throws Exception {
        List<XFDLField> fields = new ArrayList<>();
        
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(
            Files.newInputStream(filePath)
        );
        
        XFDLField currentField = null;
        String currentElement = null;
        
        while (reader.hasNext()) {
            int event = reader.next();
            
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    String elementName = reader.getLocalName();
                    
                    if ("field".equals(elementName)) {
                        currentField = new XFDLField();
                        currentField.sid = reader.getAttributeValue(null, "sid");
                    }
                    currentElement = elementName;
                    break;
                    
                case XMLStreamConstants.CHARACTERS:
                    if (currentField != null && currentElement != null) {
                        String text = reader.getText().trim();
                        if (!text.isEmpty()) {
                            switch (currentElement) {
                                case "type":
                                    currentField.type = text;
                                    break;
                                case "label":
                                    currentField.label = text;
                                    break;
                                case "value":
                                    currentField.value = text;
                                    break;
                            }
                        }
                    }
                    break;
                    
                case XMLStreamConstants.END_ELEMENT:
                    if ("field".equals(reader.getLocalName()) && currentField != null) {
                        fields.add(currentField);
                        currentField = null;
                    }
                    currentElement = null;
                    break;
            }
        }
        
        reader.close();
        return fields;
    }
}
```

#### Option 2: **JDOM2** ⭐⭐⭐⭐
**Library:** `org.jdom:jdom2:2.0.6.1`

**Pros:**
- ✅ Cleaner API than DOM
- ✅ Easy to use
- ✅ Good for complex XML

**Cons:**
- ❌ Additional dependency (~150KB)
- ❌ Built-in DOM is sufficient

**Example:**
```java
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

SAXBuilder builder = new SAXBuilder();
Document doc = builder.build(filePath.toFile());
Element root = doc.getRootElement();

List<Element> fields = root.getChildren("field");
for (Element field : fields) {
    String sid = field.getAttributeValue("sid");
    String type = field.getChildText("type");
    String label = field.getChildText("label");
}
```

#### Option 3: **DOM4J** ⭐⭐⭐
**Library:** `org.dom4j:dom4j:2.1.4`

**Pros:**
- ✅ XPath support
- ✅ Flexible API

**Cons:**
- ❌ Additional dependency
- ❌ Less modern than JDOM2

#### Option 4: **XPath Queries** ⭐⭐⭐⭐⭐
**Built-in:** Java's XPath API

**Best for complex queries:**
```java
import javax.xml.xpath.*;

XPathFactory xpathFactory = XPathFactory.newInstance();
XPath xpath = xpathFactory.newXPath();

// Find all field labels
XPathExpression expr = xpath.compile("//field/label/text()");
NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

// Find field by sid
expr = xpath.compile("//field[@sid='FIELD1']/label");
String label = (String) expr.evaluate(doc, XPathConstants.STRING);
```

### 🎯 Recommendation

**For XFDL Parsing:**

Use **built-in DOM parser with XPath** because:
1. ✅ No dependencies
2. ✅ Sufficient cho XFDL structure
3. ✅ XPath makes queries easy
4. ✅ Standard Java APIs

Only add JDOM2 if:
- Need significantly cleaner code
- Working with many XML files
- Team preference

---

## 7. Implementation Strategy

### 🏗️ Architecture

#### Current Pattern
```
DocumentParser (interface)
    ├── JavaParser
    ├── MarkdownParser
    ├── SQLParser
    └── TextParser
```

#### Extended Pattern
```
DocumentParser (interface)
    ├── JavaParser (existing)
    ├── MarkdownParser (existing)
    ├── SQLParser (existing)
    ├── TextParser (existing)
    ├── JSPParser (new) 
    ├── JavaScriptParser (new)
    ├── HTMLParser (new)
    └── XFDLParser (new)
```

### 📦 Dependencies to Add

```xml
<!-- pom.xml (if using Maven) or add to lib/ -->

<!-- HTML & JSP Parsing -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>

<!-- Optional: JavaScript Parsing (if needed) -->
<dependency>
    <groupId>org.mozilla</groupId>
    <artifactId>rhino</artifactId>
    <version>1.7.15</version>
</dependency>

<!-- XFDL - No additional dependencies (use built-in) -->
```

**If adding manually to `lib/`:**
- Download `jsoup-1.17.2.jar` (~500KB)
- Optionally `rhino-1.7.15.jar` (~1.5MB)

### 🎯 Implementation Priority

#### Phase 1: Essential (Week 1)
1. **HTMLParser** - Most common, uses Jsoup
2. **JSPParser** - For web apps, uses Jsoup + regex
3. **JavaScriptParser** - Regex-based, no dependencies

#### Phase 2: Specialized (Week 2)
4. **XFDLParser** - If project has XFDL forms

### 📝 Code Template

```java
package com.noteflix.pcm.rag.parser.core;

import com.noteflix.pcm.rag.parser.api.DocumentParser;
import com.noteflix.pcm.rag.model.DocumentType;
import com.noteflix.pcm.rag.model.RAGDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NewFormatParser implements DocumentParser {

    @Override
    public boolean canParse(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        // Implement file type detection
        return fileName.endsWith(".ext");
    }

    @Override
    public RAGDocument parse(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        
        // 1. Parse content
        // 2. Extract metadata
        // 3. Clean text for embedding
        
        String title = extractTitle(content, filePath);
        String cleanText = extractCleanText(content);
        
        RAGDocument doc = RAGDocument.builder()
            .id(UUID.randomUUID().toString())
            .type(DocumentType.CUSTOM) // Define new type
            .title(title)
            .sourcePath(filePath.toString())
            .content(cleanText)
            .rawContent(content)
            .indexedAt(LocalDateTime.now())
            .build();
        
        // Add metadata
        doc.addMetadata("language", "format-name");
        // Add more metadata...
        
        log.debug("Parsed {} file: {}", "format-name", filePath.getFileName());
        
        return doc;
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] {".ext1", ".ext2"};
    }
    
    // Helper methods
    private String extractTitle(String content, Path filePath) {
        // Implementation
        return filePath.getFileName().toString();
    }
    
    private String extractCleanText(String content) {
        // Implementation
        return content;
    }
}
```

### 🧪 Testing Strategy

```java
package com.noteflix.pcm.rag.parser.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

class HTMLParserTest {
    
    private final HTMLParser parser = new HTMLParser();
    
    @Test
    void testCanParse() {
        assertTrue(parser.canParse(Paths.get("test.html")));
        assertTrue(parser.canParse(Paths.get("test.htm")));
        assertFalse(parser.canParse(Paths.get("test.txt")));
    }
    
    @Test
    void testParseBasicHTML() throws Exception {
        String html = """
            <html>
            <head><title>Test Page</title></head>
            <body>
                <h1>Header</h1>
                <p>Paragraph text.</p>
            </body>
            </html>
            """;
        
        // Create temp file
        Path tempFile = Files.createTempFile("test", ".html");
        Files.writeString(tempFile, html);
        
        try {
            RAGDocument doc = parser.parse(tempFile);
            
            assertNotNull(doc);
            assertEquals("Test Page", doc.getTitle());
            assertTrue(doc.getContent().contains("Header"));
            assertTrue(doc.getContent().contains("Paragraph text"));
            assertEquals("html", doc.getMetadata().get("language"));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
    
    @Test
    void testParseMalformedHTML() throws Exception {
        String html = "<html><body><p>Unclosed paragraph</body></html>";
        Path tempFile = Files.createTempFile("test", ".html");
        Files.writeString(tempFile, html);
        
        try {
            RAGDocument doc = parser.parse(tempFile);
            assertNotNull(doc); // Should handle gracefully
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
```

---

## 8. Comparison Matrix

### 📊 Parser Options Summary

| Language | Recommended Parser | Size | Complexity | AST Support | Dependencies |
|----------|-------------------|------|------------|-------------|--------------|
| **Java** | JavaParser | 3MB | Medium | ✅ Full | javaparser-core |
| **JSP** | Jsoup + Regex | 500KB | Low | ❌ No | jsoup |
| **JavaScript** | Regex | 0 | Very Low | ❌ No | None |
| **JavaScript** | Rhino (alt) | 1.5MB | Medium | ✅ Yes | rhino |
| **HTML** | Jsoup | 500KB | Low | ✅ DOM | jsoup |
| **XFDL** | Built-in DOM | 0 | Low | ✅ DOM | None |
| **Markdown** | Regex (current) | 0 | Very Low | ❌ No | None |
| **SQL** | Regex (current) | 0 | Very Low | ❌ No | None |

### 🎯 Feature Comparison

| Parser | Parse Speed | Memory | Accuracy | Maintenance | Use Case |
|--------|-------------|--------|----------|-------------|----------|
| **JavaParser** | Fast | Medium | 100% | Easy | Production Java analysis |
| **Jsoup** | Very Fast | Low | 99%+ | Easy | HTML/JSP parsing |
| **Regex** | Very Fast | Very Low | 70-90% | Medium | Quick extraction |
| **Rhino** | Medium | Medium | 95%+ | Medium | ES5 JS parsing |
| **DOM/SAX** | Fast | Low | 100% | Easy | XML/XFDL |

### 💰 Cost-Benefit Analysis

#### Option A: Minimal (Regex-based)
**Dependencies:** None  
**Total Size:** 0 MB  
**Pros:**
- ✅ No setup
- ✅ Fast
- ✅ Simple

**Cons:**
- ❌ Limited accuracy
- ❌ Fragile
- ❌ Hard to extend

**Best For:** Quick prototypes, simple extraction

#### Option B: Jsoup-based (Recommended)
**Dependencies:** Jsoup  
**Total Size:** ~500 KB  
**Pros:**
- ✅ Professional quality
- ✅ Robust
- ✅ Easy to use
- ✅ Handles edge cases

**Cons:**
- ❌ One dependency to add

**Best For:** Production use, web content parsing

#### Option C: Full-featured
**Dependencies:** Jsoup + Rhino  
**Total Size:** ~2 MB  
**Pros:**
- ✅ Complete parsing
- ✅ JavaScript AST
- ✅ All features

**Cons:**
- ❌ Larger size
- ❌ More complex

**Best For:** Advanced code analysis needs

---

## 9. Recommended Architecture

### 🏆 Final Recommendation

**For PCM Desktop Project:**

#### Tier 1: Core Parsers (Add Now)
1. **HTMLParser** - Jsoup-based
   - Essential for web content
   - Used in JSP support
   - ~500KB dependency

2. **JSPParser** - Jsoup + Regex hybrid
   - Parse HTML structure with Jsoup
   - Extract JSP constructs with regex
   - Reuses Jsoup dependency

3. **JavaScriptParser** - Regex-based
   - No dependencies
   - Good enough cho RAG use case
   - Can upgrade to Rhino later if needed

#### Tier 2: Specialized (Add If Needed)
4. **XFDLParser** - Built-in DOM
   - Only if project has XFDL forms
   - No dependencies

### 📦 Dependency Plan

```
Current Dependencies (RAG):
├── lucene-* (10.3.1)
├── onnxruntime (1.23.2)
├── javaparser-* (3.26.3) ✅
├── tokenizers (0.35.0)
└── api (0.35.0)

Add for Multi-Language:
└── jsoup (1.17.2) 📦 NEW (~500KB)

Optional Future:
└── rhino (1.7.15) 🔮 OPTIONAL (~1.5MB)
```

### 🚀 Implementation Roadmap

#### Week 1: Foundation
- [x] Research parsers
- [ ] Create parser interfaces
- [ ] Implement HTMLParser
- [ ] Unit tests

#### Week 2: Web Support
- [ ] Implement JSPParser
- [ ] Implement JavaScriptParser
- [ ] Integration tests
- [ ] Update RAG pipeline

#### Week 3: Polish
- [ ] Performance optimization
- [ ] Error handling
- [ ] Documentation
- [ ] Update examples

#### Optional: Advanced
- [ ] Implement XFDLParser (if needed)
- [ ] Upgrade JS parser to Rhino (if needed)
- [ ] Add symbol resolution for JS
- [ ] Cross-language analysis

### 📝 Code Organization

```
src/main/java/com/noteflix/pcm/rag/parser/
├── api/
│   └── DocumentParser.java (existing interface)
├── core/
│   ├── JavaParser.java (existing)
│   ├── MarkdownParser.java (existing)
│   ├── SQLParser.java (existing)
│   ├── TextParser.java (existing)
│   ├── HTMLParser.java (new)
│   ├── JSPParser.java (new)
│   ├── JavaScriptParser.java (new)
│   └── XFDLParser.java (optional, new)
├── factory/
│   └── ParserFactory.java (auto-detect format)
└── util/
    ├── HTMLUtils.java (Jsoup helpers)
    ├── JSPUtils.java (JSP-specific extraction)
    └── JavaScriptUtils.java (JS helpers)
```

### 🧪 Testing Files

```
src/test/resources/parser/
├── test.html
├── test.jsp
├── test.js
├── test.xfdl
├── malformed.html
├── complex.jsp
└── embedded.js
```

### 📊 Success Metrics

1. **Coverage:** Parse >90% of web files trong examples/
2. **Accuracy:** Extract meaningful text for RAG
3. **Performance:** <100ms per file
4. **Memory:** <50MB for typical batch
5. **Robustness:** Handle malformed files gracefully

---

## ✅ Action Items

### Immediate (This Week)
1. ✅ Research completed - This document
2. [ ] Review với team
3. [ ] Approve dependency: Jsoup
4. [ ] Download jsoup-1.17.2.jar

### Short-term (Next 2 Weeks)
1. [ ] Implement HTMLParser
2. [ ] Implement JSPParser
3. [ ] Implement JavaScriptParser
4. [ ] Write unit tests
5. [ ] Update build scripts

### Medium-term (Next Month)
1. [ ] Integration with RAG pipeline
2. [ ] Test với real examples/
3. [ ] Performance benchmarks
4. [ ] Documentation updates

### Long-term (Future)
1. [ ] Consider XFDLParser nếu cần
2. [ ] Upgrade to Rhino for JS nếu cần advanced features
3. [ ] Add TypeScript support
4. [ ] Cross-language call graph

---

## 📚 References

### Official Documentation
- **JavaParser:** https://javaparser.org/
- **Jsoup:** https://jsoup.org/
- **Rhino:** https://github.com/mozilla/rhino
- **GraalVM JS:** https://www.graalvm.org/javascript/
- **Java XML:** https://docs.oracle.com/javase/tutorial/jaxp/

### Tutorials
- JavaParser Getting Started: https://javaparser.org/getting-started
- Jsoup Cookbook: https://jsoup.org/cookbook/
- Java XML Processing: https://www.baeldung.com/java-xml

### API Documentation
- JavaParser JavaDocs: https://javadoc.io/doc/com.github.javaparser/javaparser-core
- Jsoup API: https://jsoup.org/apidocs/
- Java XML APIs: https://docs.oracle.com/en/java/javase/21/docs/api/java.xml/module-summary.html

### Examples
- JavaParser Examples: https://github.com/javaparser/javaparser/tree/master/javaparser-core-testing/src/test/java/com/github/javaparser
- Jsoup Examples: https://jsoup.org/cookbook/
- JSP Specification: https://jakarta.ee/specifications/pages/

---

**📅 Document Version:** 1.0  
**✍️ Author:** AI Code Assistant  
**🔄 Last Updated:** 15/11/2025  
**📧 Feedback:** Báo issues qua project issue tracker

---

## 💡 Quick Reference

### Adding Jsoup to Project

**Manual (lib/ directory):**
```bash
cd lib/
mkdir -p parsers/
cd parsers/
curl -O https://repo1.maven.org/maven2/org/jsoup/jsoup/1.17.2/jsoup-1.17.2.jar
```

**Update build.sh:**
```bash
# Add Parser libraries (line ~260)
for jar in lib/parsers/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done
```

### Quick Test

```java
// Test Jsoup
import org.jsoup.Jsoup;

String html = "<html><body><h1>Test</h1></body></html>";
Document doc = Jsoup.parse(html);
System.out.println(doc.title());
```

### Common Issues

**Issue:** ClassNotFoundException: org.jsoup.Jsoup  
**Fix:** Add jsoup.jar to classpath

**Issue:** Malformed HTML not parsing  
**Fix:** Jsoup handles this automatically - check parser output

**Issue:** JavaScript not extracting correctly  
**Fix:** Check regex patterns, consider Rhino

**Issue:** XFDL namespace errors  
**Fix:** Set namespace-aware: `factory.setNamespaceAware(true);`


