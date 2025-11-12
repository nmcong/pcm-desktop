# Báo Cáo Toàn Diện: Tích Hợp Source Code với PCM WebApp

**Version:** 2.0  
**Date:** 2025-11-09  
**Author:** AI Architecture Team  
**Status:** 📋 Proposal & Design Document

---

## 📑 Mục Lục

1. [Executive Summary](#1-executive-summary)
2. [Phân Tích Hiện Trạng](#2-phân-tích-hiện-trạng)
3. [Yêu Cầu & Use Cases](#3-yêu-cầu--use-cases)
4. [So Sánh Các Giải Pháp](#4-so-sánh-các-giải-pháp)
5. [Kiến Trúc Đề Xuất](#5-kiến-trúc-đề-xuất)
6. [Implementation Details](#6-implementation-details)
7. [Roadmap & Timeline](#7-roadmap--timeline)
8. [Risk Analysis](#8-risk-analysis)
9. [Best Practices](#9-best-practices)
10. [Appendix](#10-appendix)

---

## 1. Executive Summary

### 🎯 Vấn Đề Cốt Lõi

PCM WebApp hiện tại chỉ quản lý **metadata nghiệp vụ** (subsystems, projects, screens, events) nhưng **không liên kết với source code thực tế**. Điều này gây ra:

- ❌ AI Chat không thể trả lời câu hỏi về code: _"File nào implement screen này?"_
- ❌ Developer phải manually mapping giữa design và code
- ❌ Thiếu traceability: thay đổi code không reflect vào PCM
- ❌ Code review khó khăn: không biết impact của thay đổi

### 💡 Giải Pháp Đề Xuất

**Xây dựng Code Index System** bao gồm:

1. **Desktop/CLI Indexer**: Quét codebase, tạo AST, extract metadata
2. **Code Index Database**: Lưu trữ mappings (screen ↔ files, API ↔ endpoints, DB ↔ tables)
3. **AI Chat Integration**: Thêm tools để LLM query code index
4. **UI Enhancement**: Hiển thị code references trong Project/Screen detail

### 📊 Impact

| Metric                     | Before             | After (Estimated) |
| -------------------------- | ------------------ | ----------------- |
| Time to find relevant code | 15-30 min          | < 2 min           |
| AI Chat code accuracy      | 0% (hallucination) | 90%+ (verified)   |
| Developer onboarding       | 2-3 days           | 4-6 hours         |
| Code review efficiency     | Baseline           | +50%              |
| Documentation sync         | Manual             | Automated         |

---

## 2. Phân Tích Hiện Trạng

### 2.1 Hệ Thống Hiện Tại

#### ✅ Những Gì Đã Có

```javascript
// PCM WebApp hiện có:
- IndexedDB với: projects, screens, events, subsystems, knowledge_base
- GitHub Integration: fetch file content qua GitHub API (GitHubService.js)
- AI Chat với 10+ tools: search_projects, get_screen_details, search_knowledge_base
- Function Calling: OpenAI/Claude native tool calling
```

**Strengths:**

- ✅ AI Chat infrastructure hoàn chỉnh
- ✅ Function calling đã hoạt động tốt
- ✅ GitHub authentication & API integration ready
- ✅ IndexedDB cho offline-first architecture

#### ❌ Những Gì Còn Thiếu

**Gap #1: No Code Metadata**

```json
// Screen trong PCM:
{
  "id": 104,
  "name": "Finance Approval Desk",
  "subsystem": "Refund Management",
  // ❌ Missing:
  "sourceFiles": [], // No link to actual code
  "components": [], // No React/Vue components info
  "apiEndpoints": [], // No API calls mapping
  "databaseTables": [] // No DB schema info
}
```

**Gap #2: No Reverse Lookup**

- Có file → không biết screen/project nào dùng
- Có API endpoint → không biết nơi gọi
- Có DB table → không biết feature nào access

**Gap #3: AI Hallucination**

```
User: "File nào implement Finance Approval?"
AI: "Có thể là FinanceApproval.tsx trong src/pages/..."
❌ LLM đang đoán, không có dữ liệu thực
```

### 2.2 Hiện Trạng GitHub Integration

```javascript
// GitHubFunctions.js - Đã có:
- getFileContent(owner, repo, path, branch)
- getRepositoryFiles(owner, repo, path)
- parseRepositoryUrl(url)

// ⚠️ Hạn chế:
- Chỉ fetch on-demand (slow, rate-limited)
- Không có index/cache
- Không parse code structure
- Phụ thuộc network
```

---

## 3. Yêu Cầu & Use Cases

### 3.1 User Stories

#### US-1: Developer Finding Code

```
As a: Backend Developer
I want to: Ask "Which files implement the refund approval workflow?"
So that: I can quickly locate and modify the right code

Acceptance Criteria:
- AI returns exact file paths
- Shows components, functions, API calls
- Links to GitHub with line numbers
- Response time < 3s
```

#### US-2: Code Impact Analysis

```
As a: Tech Lead
I want to: Ask "What features will break if I change the payout_batches table?"
So that: I can assess migration impact

Acceptance Criteria:
- Lists all screens using the table
- Shows API endpoints accessing it
- Identifies risky dependencies
- Suggests migration strategy
```

#### US-3: Onboarding New Developer

```
As a: Junior Developer
I want to: Explore codebase through natural language
So that: I can understand architecture without senior help

Example Questions:
- "Show me how authentication works"
- "Where are validation rules defined?"
- "How do I add a new screen to the refund module?"
```

#### US-4: Documentation Sync

```
As a: QA Engineer
I want to: Verify PCM data matches actual code
So that: Test cases stay updated

Acceptance Criteria:
- Auto-detect when code changes
- Flag outdated PCM entries
- Suggest documentation updates
```

### 3.2 Technical Requirements

#### Functional Requirements

| ID    | Requirement                          | Priority | Complexity |
| ----- | ------------------------------------ | -------- | ---------- |
| FR-1  | Index TypeScript/JavaScript files    | P0       | Medium     |
| FR-2  | Extract React components & props     | P0       | Medium     |
| FR-3  | Parse API calls (fetch, axios)       | P0       | High       |
| FR-4  | Identify database queries            | P1       | High       |
| FR-5  | Support incremental updates          | P1       | Medium     |
| FR-6  | Map screens to files via annotations | P0       | Low        |
| FR-7  | Reverse lookup (file → screens)      | P1       | Low        |
| FR-8  | AI Chat tool integration             | P0       | Medium     |
| FR-9  | UI code reference display            | P1       | Low        |
| FR-10 | Multi-repo support                   | P2       | Medium     |

#### Non-Functional Requirements

- **Performance**: Index 10K files in < 30s
- **Accuracy**: 95%+ correct mappings
- **Storage**: Code index < 5MB for typical project
- **Offline-First**: Work without network after initial index
- **Incremental**: Re-index only changed files (< 2s)
- **Maintainability**: Clear separation of concerns

---

## 4. So Sánh Các Giải Pháp

### 4.1 Approach Matrix

| Approach                          | Pros                                                         | Cons                                                                  | Verdict            |
| --------------------------------- | ------------------------------------------------------------ | --------------------------------------------------------------------- | ------------------ |
| **1. Pure GitHub API**            | - No local tool needed<br>- Always up-to-date                | - Slow (rate-limited)<br>- Network dependent<br>- No semantic parsing | ❌ Not suitable    |
| **2. AST-based (ts-morph)**       | - Accurate parsing<br>- Understands TS/JS<br>- Extract types | - Heavy (RAM)<br>- Slow for large repos<br>- Complex setup            | ✅ **Recommended** |
| **3. Regex + Annotations**        | - Fast<br>- Simple<br>- Lightweight                          | - Low accuracy<br>- Requires discipline<br>- Manual effort            | ⚠️ Fallback only   |
| **4. Tree-sitter**                | - Multi-language<br>- Fast<br>- Battle-tested                | - Native binding<br>- Complex API<br>- Overkill for JS/TS             | ⚠️ Future option   |
| **5. LSP Integration**            | - IDE-quality<br>- Real-time<br>- Accurate                   | - Requires LSP server<br>- Complex setup<br>- Not portable            | ❌ Too complex     |
| **6. Vector Search (Embeddings)** | - Semantic search<br>- Fuzzy matching                        | - Expensive (API)<br>- Slow (inference)<br>- Storage overhead         | ⚠️ Phase 2         |

### 4.2 Recommended Hybrid Approach

```
┌─────────────────────────────────────┐
│         Phase 1: Foundation         │
├─────────────────────────────────────┤
│ 1. Annotations (@pcm-screen:104)    │ ← Fast, reliable
│ 2. ts-morph AST for TS/JS/TSX       │ ← Accurate parsing
│ 3. Simple regex for SQL/Prisma      │ ← Good enough
│ 4. GitHub API for on-demand fetch   │ ← Already have
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│       Phase 2: Enhancement          │
├─────────────────────────────────────┤
│ 5. Vector embeddings for search     │ ← Better UX
│ 6. Incremental indexing (git diff)  │ ← Performance
│ 7. Multi-language (tree-sitter)     │ ← Scalability
└─────────────────────────────────────┘
```

### 4.3 Tool Selection: ts-morph

**Why ts-morph?**

```typescript
// Example: Extract component info
import { Project } from "ts-morph";

const project = new Project({
  tsConfigFilePath: "./tsconfig.json",
});

const sourceFile = project.getSourceFile("FinanceApproval.tsx");

// Extract exports
const exports = sourceFile.getExportedDeclarations();

// Find API calls
sourceFile.getDescendantsOfKind(SyntaxKind.CallExpression).forEach((call) => {
  if (call.getExpression().getText().includes("fetch")) {
    const url = call.getArguments()[0].getText();
    console.log(`API call: ${url}`);
  }
});

// Find props interface
const interfaces = sourceFile.getInterfaces();
```

**Alternatives Considered:**

- ❌ Babel: Parse only, no type info
- ❌ TypeScript Compiler API: Too low-level
- ❌ SWC: Fast but limited query API
- ✅ **ts-morph**: Perfect balance of power & usability

---

## 5. Kiến Trúc Đề Xuất

### 5.1 System Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                     PCM WebApp (Browser)                      │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────┐         ┌──────────────────┐            │
│  │   AI Chat UI   │────────▶│ Function Calling │            │
│  └────────────────┘         └──────────────────┘            │
│         │                            │                        │
│         ├─────────┬─────────────────┤                        │
│         ▼         ▼                  ▼                        │
│  ┌──────────┐  ┌────────────────┐  ┌──────────────┐        │
│  │ Projects │  │ Code Index DB  │  │ GitHub API   │        │
│  │ Screens  │  │  (IndexedDB)   │  │  (on-demand) │        │
│  │ Events   │  └────────────────┘  └──────────────┘        │
│  └──────────┘         ▲                                      │
│                       │                                       │
└───────────────────────┼───────────────────────────────────────┘
                        │
                        │ Import code-index.json
                        │
┌───────────────────────┼───────────────────────────────────────┐
│                       │                                        │
│              ┌────────┴─────────┐                            │
│              │  Code Index File │                            │
│              │  (Static JSON)   │                            │
│              └────────▲─────────┘                            │
│                       │                                       │
│                       │ Generate                              │
│                       │                                       │
│              ┌────────┴─────────┐                            │
│              │  CLI Indexer     │                            │
│              │  (Node.js Tool)  │                            │
│              └────────▲─────────┘                            │
│                       │                                       │
│                       │ Scan                                  │
│                       │                                       │
│              ┌────────┴─────────┐                            │
│              │   Source Code    │                            │
│              │  (Git Repo)      │                            │
│              └──────────────────┘                            │
│                                                               │
│                  Developer Machine                            │
└──────────────────────────────────────────────────────────────┘
```

### 5.2 Data Model

#### Code Index Schema

```typescript
interface CodeIndex {
  metadata: {
    version: string; // "1.0.0"
    generatedAt: string; // ISO timestamp
    repository: {
      url: string;
      commit: string; // Git SHA
      branch: string;
    };
    stats: {
      totalFiles: number;
      indexedFiles: number;
      components: number;
      apiCalls: number;
    };
  };

  // Screen → Code mapping
  screens: {
    [screenId: string]: ScreenCodeInfo;
  };

  // File → Metadata
  files: {
    [filePath: string]: FileCodeInfo;
  };

  // API → Usage
  apiEndpoints: {
    [endpoint: string]: ApiEndpointInfo;
  };

  // Database → Access
  databaseTables: {
    [tableName: string]: DatabaseTableInfo;
  };

  // Component → Props
  components: {
    [componentName: string]: ComponentInfo;
  };
}

interface ScreenCodeInfo {
  screenId: number;
  screenName: string;

  // Primary files
  sourceFiles: string[]; // ["apps/refund/FinanceApproval.tsx"]

  // Components used
  components: string[]; // ["FinanceApprovalForm", "PayoutModal"]

  // API calls made
  apiCalls: ApiCallReference[];

  // Database tables accessed
  databaseTables: string[]; // ["refund_cases", "payout_batches"]

  // Test files
  testFiles: string[]; // ["FinanceApproval.test.tsx"]

  // Related documentation
  docs: string[]; // ["docs/refund-workflow.md"]
}

interface FileCodeInfo {
  path: string;
  type: "component" | "page" | "service" | "util" | "test" | "config";
  language: "typescript" | "javascript" | "tsx" | "jsx";

  // Size & complexity
  lines: number;
  size: number; // bytes
  complexity: number; // cyclomatic complexity

  // What this file exports
  exports: {
    name: string;
    type: "function" | "class" | "const" | "interface" | "type";
    isDefault: boolean;
  }[];

  // What this file imports
  imports: {
    module: string;
    names: string[];
  }[];

  // PCM references
  pcmAnnotations: {
    screens: number[]; // From @pcm-screen comments
    projects: string[]; // From @pcm-project comments
  };

  // API calls
  apiCalls: ApiCallReference[];

  // Database access
  databaseQueries: DatabaseQueryReference[];

  // AST summary
  astSummary: string; // "React FC with useQuery hook"

  // Last modified
  lastModified: string; // ISO timestamp
  gitBlame: {
    author: string;
    commit: string;
  };
}

interface ApiCallReference {
  method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  path: string; // "/api/v2/payout/schedule"
  file: string;
  line: number;
  function: string; // "handleSubmit"

  // Request/Response types (if available)
  requestType?: string;
  responseType?: string;
}

interface DatabaseQueryReference {
  type: "select" | "insert" | "update" | "delete" | "raw";
  table: string;
  file: string;
  line: number;
  rawQuery?: string; // If SQL string literal
}

interface ApiEndpointInfo {
  method: string;
  path: string;

  // Where it's called from
  calledFrom: {
    file: string;
    line: number;
    screen?: number;
  }[];

  // Backend handler (if known)
  handler?: {
    file: string;
    function: string;
  };
}

interface DatabaseTableInfo {
  name: string;

  // Which files access it
  accessedBy: {
    file: string;
    line: number;
    operation: "select" | "insert" | "update" | "delete";
    screen?: number;
  }[];

  // Schema info (if available)
  schema?: {
    columns: string[];
    primaryKey: string;
  };
}

interface ComponentInfo {
  name: string;
  file: string;

  // Props interface
  props?: {
    name: string;
    type: string;
    required: boolean;
    description?: string;
  }[];

  // Where it's used
  usedIn: {
    file: string;
    line: number;
  }[];

  // Hooks used
  hooks: string[]; // ["useState", "useQuery", "useCustomHook"]
}
```

#### Storage Strategy

```typescript
// IndexedDB Stores
{
  "codeIndex": {
    keyPath: "version",
    data: CodeIndex                // Single JSON blob
  },

  "fileIndex": {
    keyPath: "path",
    indexes: ["type", "lastModified"],
    data: FileCodeInfo[]           // One per file
  },

  "screenCodeMap": {
    keyPath: "screenId",
    data: ScreenCodeInfo[]         // Quick screen → code lookup
  }
}

// Size Estimate:
// - 1,000 files × 2KB = 2MB
// - 100 screens × 5KB = 500KB
// - Metadata = 100KB
// Total: ~2.6MB (acceptable for IndexedDB)
```

---

## 6. Implementation Details

### 6.1 CLI Indexer Tool

#### Project Structure

```
packages/code-indexer/
├── package.json
├── tsconfig.json
├── src/
│   ├── index.ts                # CLI entry point
│   ├── indexer.ts              # Main indexer logic
│   ├── parsers/
│   │   ├── typescript.ts       # ts-morph parser
│   │   ├── javascript.ts       # Babel parser
│   │   ├── sql.ts              # SQL parser
│   │   └── annotations.ts      # @pcm-* comment parser
│   ├── extractors/
│   │   ├── components.ts       # React component extraction
│   │   ├── apiCalls.ts         # fetch/axios detection
│   │   ├── database.ts         # DB query detection
│   │   └── exports.ts          # Module export analysis
│   ├── analyzers/
│   │   ├── complexity.ts       # Cyclomatic complexity
│   │   ├── dependencies.ts     # Import graph
│   │   └── relationships.ts    # Screen-file mapping
│   ├── output/
│   │   ├── json.ts             # JSON generator
│   │   └── sqlite.ts           # SQLite generator (Phase 2)
│   └── utils/
│       ├── git.ts              # Git operations
│       ├── config.ts           # Config loader
│       └── logger.ts           # CLI logger
└── tests/
    └── fixtures/               # Test codebases
```

#### CLI Interface

```bash
# Install
pnpm add -g @pcm/code-indexer

# Basic usage
pcm-index --src ../my-repo --out ./public/code-index.json

# With config
pcm-index --config ./pcm-index.config.json

# Incremental (only changed files)
pcm-index --src ../my-repo --incremental

# Watch mode
pcm-index --src ../my-repo --watch

# Verbose output
pcm-index --src ../my-repo --verbose
```

#### Configuration File

```json
// pcm-index.config.json
{
  "source": {
    "path": "../noteflix",
    "include": [
      "apps/**/*.{ts,tsx,js,jsx}",
      "packages/**/*.{ts,tsx,js,jsx}",
      "services/**/*.sql"
    ],
    "exclude": [
      "**/node_modules/**",
      "**/dist/**",
      "**/*.test.ts",
      "**/*.spec.ts"
    ]
  },

  "output": {
    "path": "./public/assets/code-index.json",
    "format": "json",
    "compress": true,
    "splitByModule": false
  },

  "parsing": {
    "typescript": {
      "enabled": true,
      "tsConfigPath": "../noteflix/tsconfig.json",
      "extractTypes": true,
      "extractJsDoc": true
    },
    "annotations": {
      "enabled": true,
      "patterns": {
        "screen": "@pcm-screen:(\\d+)",
        "project": "@pcm-project:([\\w-]+)",
        "table": "@pcm-table:([\\w_]+)"
      }
    },
    "apiCalls": {
      "enabled": true,
      "patterns": [
        "fetch\\(['\"]([^'\"]+)['\"]",
        "axios\\.\\w+\\(['\"]([^'\"]+)['\"]",
        "apiClient\\.[\\w]+\\(['\"]([^'\"]+)['\"]"
      ]
    },
    "database": {
      "enabled": true,
      "ormDetection": ["prisma", "typeorm", "sequelize"],
      "rawQueryPatterns": ["db\\.query\\(", "connection\\.execute\\("]
    }
  },

  "analysis": {
    "calculateComplexity": true,
    "buildDependencyGraph": true,
    "detectCircularDeps": true,
    "findUnusedExports": false
  },

  "git": {
    "includeBlame": true,
    "includeCommitInfo": true,
    "trackChanges": true
  },

  "performance": {
    "parallel": true,
    "maxWorkers": 4,
    "cacheDir": "./.pcm-index-cache"
  }
}
```

#### Core Implementation

```typescript
// src/indexer.ts
import { Project } from "ts-morph";
import { ApiCallExtractor } from "./extractors/apiCalls";
import { ComponentExtractor } from "./extractors/components";
import { DatabaseExtractor } from "./extractors/database";
import { AnnotationParser } from "./parsers/annotations";
import { CodeIndex, FileCodeInfo } from "./types";

export class CodeIndexer {
  private project: Project;
  private config: IndexerConfig;
  private index: CodeIndex;

  constructor(config: IndexerConfig) {
    this.config = config;
    this.project = new Project({
      tsConfigFilePath: config.parsing.typescript.tsConfigPath,
    });

    this.index = {
      metadata: {
        version: "1.0.0",
        generatedAt: new Date().toISOString(),
        repository: {},
        stats: {},
      },
      screens: {},
      files: {},
      apiEndpoints: {},
      databaseTables: {},
      components: {},
    };
  }

  async run(): Promise<CodeIndex> {
    console.log("🔍 Scanning source files...");
    const files = await this.scanFiles();

    console.log(`📝 Found ${files.length} files to index`);

    // Parse files in parallel
    await Promise.all(files.map((file) => this.indexFile(file)));

    console.log("🔗 Building relationships...");
    await this.buildRelationships();

    console.log("📊 Calculating statistics...");
    this.calculateStats();

    console.log("✅ Indexing complete!");
    return this.index;
  }

  private async indexFile(filePath: string): Promise<void> {
    const sourceFile = this.project.getSourceFile(filePath);
    if (!sourceFile) return;

    const fileInfo: FileCodeInfo = {
      path: filePath,
      type: this.detectFileType(filePath),
      language: this.detectLanguage(filePath),
      lines: sourceFile.getFullText().split("\n").length,
      size: sourceFile.getFullText().length,
      complexity: 0,
      exports: [],
      imports: [],
      pcmAnnotations: { screens: [], projects: [] },
      apiCalls: [],
      databaseQueries: [],
      astSummary: "",
      lastModified: "",
      gitBlame: { author: "", commit: "" },
    };

    // Extract exports
    fileInfo.exports = this.extractExports(sourceFile);

    // Extract imports
    fileInfo.imports = this.extractImports(sourceFile);

    // Parse PCM annotations
    const annotations = AnnotationParser.parse(sourceFile);
    fileInfo.pcmAnnotations = annotations;

    // Extract API calls
    const apiCallExtractor = new ApiCallExtractor(this.config);
    fileInfo.apiCalls = apiCallExtractor.extract(sourceFile);

    // Extract database queries
    const dbExtractor = new DatabaseExtractor(this.config);
    fileInfo.databaseQueries = dbExtractor.extract(sourceFile);

    // Extract components (if React file)
    if (this.isReactFile(filePath)) {
      const componentExtractor = new ComponentExtractor();
      const components = componentExtractor.extract(sourceFile);

      components.forEach((component) => {
        this.index.components[component.name] = component;
      });
    }

    // Generate AST summary
    fileInfo.astSummary = this.generateAstSummary(sourceFile);

    // Store in index
    this.index.files[filePath] = fileInfo;

    // Build screen mappings
    annotations.screens.forEach((screenId) => {
      if (!this.index.screens[screenId]) {
        this.index.screens[screenId] = {
          screenId,
          screenName: "",
          sourceFiles: [],
          components: [],
          apiCalls: [],
          databaseTables: [],
          testFiles: [],
          docs: [],
        };
      }

      this.index.screens[screenId].sourceFiles.push(filePath);
      this.index.screens[screenId].apiCalls.push(...fileInfo.apiCalls);
    });
  }

  private extractExports(sourceFile: SourceFile) {
    const exports = [];

    // Named exports
    sourceFile.getExportedDeclarations().forEach((declarations, name) => {
      declarations.forEach((decl) => {
        exports.push({
          name,
          type: this.detectDeclarationType(decl),
          isDefault: false,
        });
      });
    });

    // Default export
    const defaultExport = sourceFile.getDefaultExportSymbol();
    if (defaultExport) {
      exports.push({
        name: defaultExport.getName(),
        type: "unknown",
        isDefault: true,
      });
    }

    return exports;
  }

  private extractImports(sourceFile: SourceFile) {
    return sourceFile.getImportDeclarations().map((importDecl) => ({
      module: importDecl.getModuleSpecifierValue(),
      names: importDecl.getNamedImports().map((i) => i.getName()),
    }));
  }

  private generateAstSummary(sourceFile: SourceFile): string {
    const parts = [];

    // Detect React component
    if (this.hasReactComponent(sourceFile)) {
      parts.push("React component");
    }

    // Detect hooks
    const hooks = this.detectHooks(sourceFile);
    if (hooks.length > 0) {
      parts.push(`uses ${hooks.join(", ")}`);
    }

    // Detect API calls
    const hasApiCalls = sourceFile.getText().match(/fetch|axios/);
    if (hasApiCalls) {
      parts.push("makes API calls");
    }

    // Detect state management
    const hasState = sourceFile.getText().match(/useState|useReducer|Redux/);
    if (hasState) {
      parts.push("manages state");
    }

    return parts.join(", ") || "utility module";
  }

  private async buildRelationships(): Promise<void> {
    // Build API endpoint → usage mapping
    Object.values(this.index.files).forEach((file) => {
      file.apiCalls.forEach((apiCall) => {
        const key = `${apiCall.method} ${apiCall.path}`;

        if (!this.index.apiEndpoints[key]) {
          this.index.apiEndpoints[key] = {
            method: apiCall.method,
            path: apiCall.path,
            calledFrom: [],
          };
        }

        this.index.apiEndpoints[key].calledFrom.push({
          file: file.path,
          line: apiCall.line,
          screen: file.pcmAnnotations.screens[0],
        });
      });

      // Build database table → access mapping
      file.databaseQueries.forEach((query) => {
        if (!this.index.databaseTables[query.table]) {
          this.index.databaseTables[query.table] = {
            name: query.table,
            accessedBy: [],
          };
        }

        this.index.databaseTables[query.table].accessedBy.push({
          file: file.path,
          line: query.line,
          operation: query.type,
          screen: file.pcmAnnotations.screens[0],
        });
      });
    });
  }

  private calculateStats(): void {
    this.index.metadata.stats = {
      totalFiles: Object.keys(this.index.files).length,
      indexedFiles: Object.keys(this.index.files).length,
      components: Object.keys(this.index.components).length,
      apiCalls: Object.values(this.index.files).reduce(
        (sum, f) => sum + f.apiCalls.length,
        0,
      ),
    };
  }
}
```

### 6.2 PCM WebApp Integration

#### New AI Chat Tools

```typescript
// public/js/services/ai/functions/CodeFunctions.js

export const codeFunctions = {
  /**
   * Find source files for a screen
   */
  findSourceByScreen: {
    name: "findSourceByScreen",
    description:
      "Find source code files, components, and API calls for a screen",
    parameters: {
      type: "object",
      properties: {
        screenId: {
          type: "number",
          description: "Screen ID to find code for",
        },
      },
      required: ["screenId"],
    },
    handler: async ({ screenId }) => {
      const codeIndex = await codeIndexManager.getIndex();
      const screenCode = codeIndex.screens[screenId];

      if (!screenCode) {
        return { error: "No code information found for this screen" };
      }

      return {
        sourceFiles: screenCode.sourceFiles,
        components: screenCode.components,
        apiCalls: screenCode.apiCalls.map((api) => ({
          method: api.method,
          path: api.path,
          file: api.file,
          line: api.line,
        })),
        databaseTables: screenCode.databaseTables,
        testFiles: screenCode.testFiles,
      };
    },
  },

  /**
   * Search code by keyword
   */
  searchCodeByKeyword: {
    name: "searchCodeByKeyword",
    description: "Search source code metadata by keyword",
    parameters: {
      type: "object",
      properties: {
        keyword: {
          type: "string",
          description: "Keyword to search for",
        },
        type: {
          type: "string",
          enum: ["file", "component", "api", "table"],
          description: "Type of code element to search",
        },
      },
      required: ["keyword"],
    },
    handler: async ({ keyword, type }) => {
      const codeIndex = await codeIndexManager.getIndex();
      const results = [];

      // Search files
      if (!type || type === "file") {
        Object.values(codeIndex.files).forEach((file) => {
          if (file.path.toLowerCase().includes(keyword.toLowerCase())) {
            results.push({
              type: "file",
              path: file.path,
              summary: file.astSummary,
            });
          }
        });
      }

      // Search components
      if (!type || type === "component") {
        Object.values(codeIndex.components).forEach((comp) => {
          if (comp.name.toLowerCase().includes(keyword.toLowerCase())) {
            results.push({
              type: "component",
              name: comp.name,
              file: comp.file,
              props: comp.props,
            });
          }
        });
      }

      // Search API endpoints
      if (!type || type === "api") {
        Object.entries(codeIndex.apiEndpoints).forEach(([key, api]) => {
          if (key.toLowerCase().includes(keyword.toLowerCase())) {
            results.push({
              type: "api",
              method: api.method,
              path: api.path,
              usageCount: api.calledFrom.length,
            });
          }
        });
      }

      // Search database tables
      if (!type || type === "table") {
        Object.values(codeIndex.databaseTables).forEach((table) => {
          if (table.name.toLowerCase().includes(keyword.toLowerCase())) {
            results.push({
              type: "table",
              name: table.name,
              accessCount: table.accessedBy.length,
            });
          }
        });
      }

      return {
        query: keyword,
        resultCount: results.length,
        results: results.slice(0, 20), // Limit to 20 results
      };
    },
  },

  /**
   * Get database table usage
   */
  getDatabaseTableUsage: {
    name: "getDatabaseTableUsage",
    description: "Find all screens and files that access a database table",
    parameters: {
      type: "object",
      properties: {
        tableName: {
          type: "string",
          description: "Name of the database table",
        },
      },
      required: ["tableName"],
    },
    handler: async ({ tableName }) => {
      const codeIndex = await codeIndexManager.getIndex();
      const tableInfo = codeIndex.databaseTables[tableName];

      if (!tableInfo) {
        return { error: `Table '${tableName}' not found in code index` };
      }

      // Group by operation type
      const operations = {
        select: [],
        insert: [],
        update: [],
        delete: [],
      };

      tableInfo.accessedBy.forEach((access) => {
        operations[access.operation].push({
          file: access.file,
          line: access.line,
          screen: access.screen,
        });
      });

      // Get unique screens
      const screens = [
        ...new Set(
          tableInfo.accessedBy.filter((a) => a.screen).map((a) => a.screen),
        ),
      ];

      return {
        tableName,
        totalAccess: tableInfo.accessedBy.length,
        operations,
        affectedScreens: screens,
        files: [...new Set(tableInfo.accessedBy.map((a) => a.file))],
      };
    },
  },

  /**
   * Get API endpoint usage
   */
  getApiEndpointUsage: {
    name: "getApiEndpointUsage",
    description: "Find all places where an API endpoint is called",
    parameters: {
      type: "object",
      properties: {
        method: {
          type: "string",
          enum: ["GET", "POST", "PUT", "PATCH", "DELETE"],
          description: "HTTP method",
        },
        path: {
          type: "string",
          description: "API endpoint path",
        },
      },
      required: ["path"],
    },
    handler: async ({ method, path }) => {
      const codeIndex = await codeIndexManager.getIndex();
      const key = method ? `${method} ${path}` : path;

      // Find matching endpoint (fuzzy)
      const endpoint = Object.entries(codeIndex.apiEndpoints).find(([k, _]) =>
        k.includes(path),
      )?.[1];

      if (!endpoint) {
        return { error: `API endpoint '${path}' not found in code index` };
      }

      return {
        method: endpoint.method,
        path: endpoint.path,
        callCount: endpoint.calledFrom.length,
        callers: endpoint.calledFrom.map((caller) => ({
          file: caller.file,
          line: caller.line,
          screen: caller.screen,
        })),
        affectedScreens: [
          ...new Set(
            endpoint.calledFrom.filter((c) => c.screen).map((c) => c.screen),
          ),
        ],
      };
    },
  },

  /**
   * Analyze component usage
   */
  analyzeComponentUsage: {
    name: "analyzeComponentUsage",
    description: "Get information about where a component is used",
    parameters: {
      type: "object",
      properties: {
        componentName: {
          type: "string",
          description: "Name of the React component",
        },
      },
      required: ["componentName"],
    },
    handler: async ({ componentName }) => {
      const codeIndex = await codeIndexManager.getIndex();
      const component = codeIndex.components[componentName];

      if (!component) {
        return { error: `Component '${componentName}' not found` };
      }

      return {
        name: component.name,
        file: component.file,
        props: component.props,
        hooks: component.hooks,
        usageCount: component.usedIn.length,
        usedIn: component.usedIn.map((usage) => ({
          file: usage.file,
          line: usage.line,
        })),
      };
    },
  },
};
```

#### Code Index Manager

```typescript
// public/js/services/CodeIndexManager.js

class CodeIndexManager {
  constructor() {
    this.index = null;
    this.loaded = false;
    this.dbName = "PCMCodeIndexDB";
    this.storeName = "codeIndex";
  }

  /**
   * Initialize and load code index
   */
  async init() {
    try {
      // Try to load from IndexedDB first
      this.index = await this.loadFromIndexedDB();

      if (!this.index) {
        // Fall back to static JSON file
        this.index = await this.loadFromStaticFile();

        // Cache in IndexedDB
        if (this.index) {
          await this.saveToIndexedDB(this.index);
        }
      }

      this.loaded = true;
      console.log("✅ Code index loaded:", this.index?.metadata);
    } catch (error) {
      console.error("Failed to load code index:", error);
      this.index = null;
    }
  }

  /**
   * Load from static JSON file
   */
  async loadFromStaticFile() {
    try {
      const response = await fetch("/assets/code-index.json");
      if (!response.ok) {
        console.warn("Code index file not found");
        return null;
      }

      const data = await response.json();
      console.log("📦 Loaded code index from static file");
      return data;
    } catch (error) {
      console.error("Failed to fetch code index:", error);
      return null;
    }
  }

  /**
   * Load from IndexedDB
   */
  async loadFromIndexedDB() {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, 1);

      request.onerror = () => reject(request.error);

      request.onsuccess = () => {
        const db = request.result;
        const transaction = db.transaction([this.storeName], "readonly");
        const store = transaction.objectStore(this.storeName);
        const getRequest = store.get("current");

        getRequest.onsuccess = () => {
          resolve(getRequest.result?.data || null);
        };

        getRequest.onerror = () => reject(getRequest.error);
      };

      request.onupgradeneeded = (event) => {
        const db = event.target.result;
        if (!db.objectStoreNames.contains(this.storeName)) {
          db.createObjectStore(this.storeName, { keyPath: "version" });
        }
      };
    });
  }

  /**
   * Save to IndexedDB
   */
  async saveToIndexedDB(index) {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, 1);

      request.onsuccess = () => {
        const db = request.result;
        const transaction = db.transaction([this.storeName], "readwrite");
        const store = transaction.objectStore(this.storeName);

        store.put({
          version: "current",
          data: index,
          updatedAt: Date.now(),
        });

        transaction.oncomplete = () => resolve();
        transaction.onerror = () => reject(transaction.error);
      };

      request.onerror = () => reject(request.error);
    });
  }

  /**
   * Get current index
   */
  async getIndex() {
    if (!this.loaded) {
      await this.init();
    }
    return this.index;
  }

  /**
   * Check if code index is available
   */
  isAvailable() {
    return this.loaded && this.index !== null;
  }

  /**
   * Reload index (after update)
   */
  async reload() {
    this.loaded = false;
    this.index = null;
    await this.init();
  }
}

export default new CodeIndexManager();
```

#### UI Enhancement: Code References Tab

```javascript
// public/js/components/project/CodeReferencesTab.js

export class CodeReferencesTab {
  constructor(container, screenId) {
    this.container = container;
    this.screenId = screenId;
  }

  async render() {
    const codeIndex = await codeIndexManager.getIndex();

    if (!codeIndex) {
      this.container.innerHTML = `
        <div class="code-references-empty">
          <p>Code index not available</p>
          <p class="hint">Generate code index using: <code>pcm-index --src ./src</code></p>
        </div>
      `;
      return;
    }

    const screenCode = codeIndex.screens[this.screenId];

    if (!screenCode) {
      this.container.innerHTML = `
        <div class="code-references-empty">
          <p>No code references found for this screen</p>
          <p class="hint">Add <code>// @pcm-screen:${this.screenId}</code> to your source files</p>
        </div>
      `;
      return;
    }

    this.container.innerHTML = `
      <div class="code-references">
        ${this.renderSourceFiles(screenCode)}
        ${this.renderComponents(screenCode)}
        ${this.renderApiCalls(screenCode)}
        ${this.renderDatabaseTables(screenCode)}
      </div>
    `;
  }

  renderSourceFiles(screenCode) {
    return `
      <div class="code-section">
        <h3>📄 Source Files (${screenCode.sourceFiles.length})</h3>
        <ul class="code-list">
          ${screenCode.sourceFiles
            .map(
              (file) => `
            <li class="code-item">
              <span class="code-icon">📄</span>
              <span class="code-path">${file}</span>
              <button class="btn-view" onclick="viewFile('${file}')">View</button>
            </li>
          `,
            )
            .join("")}
        </ul>
      </div>
    `;
  }

  renderComponents(screenCode) {
    if (screenCode.components.length === 0) return "";

    return `
      <div class="code-section">
        <h3>🧩 Components (${screenCode.components.length})</h3>
        <ul class="code-list">
          ${screenCode.components
            .map(
              (comp) => `
            <li class="code-item">
              <span class="code-icon">🧩</span>
              <span class="component-name">${comp}</span>
            </li>
          `,
            )
            .join("")}
        </ul>
      </div>
    `;
  }

  renderApiCalls(screenCode) {
    if (screenCode.apiCalls.length === 0) return "";

    return `
      <div class="code-section">
        <h3>🌐 API Calls (${screenCode.apiCalls.length})</h3>
        <ul class="code-list">
          ${screenCode.apiCalls
            .map(
              (api) => `
            <li class="code-item">
              <span class="api-method ${api.method.toLowerCase()}">${api.method}</span>
              <span class="api-path">${api.path}</span>
              <span class="code-location">${api.file}:${api.line}</span>
            </li>
          `,
            )
            .join("")}
        </ul>
      </div>
    `;
  }

  renderDatabaseTables(screenCode) {
    if (screenCode.databaseTables.length === 0) return "";

    return `
      <div class="code-section">
        <h3>🗄️ Database Tables (${screenCode.databaseTables.length})</h3>
        <ul class="code-list">
          ${screenCode.databaseTables
            .map(
              (table) => `
            <li class="code-item">
              <span class="code-icon">🗄️</span>
              <span class="table-name">${table}</span>
            </li>
          `,
            )
            .join("")}
        </ul>
      </div>
    `;
  }
}
```

---

## 7. Roadmap & Timeline

### Phase 1: Foundation (4-6 weeks)

**Week 1-2: CLI Indexer Development**

- ✅ Setup project structure
- ✅ Implement ts-morph parser
- ✅ Extract components, exports, imports
- ✅ Parse @pcm-\* annotations
- ✅ Generate JSON output

**Week 3: Advanced Parsing**

- ✅ API call detection (fetch, axios)
- ✅ Database query detection
- ✅ Component props extraction
- ✅ AST summary generation

**Week 4: PCM Integration**

- ✅ CodeIndexManager service
- ✅ IndexedDB storage
- ✅ New AI Chat tools (5 functions)
- ✅ Tool registration

**Week 5-6: UI & Testing**

- ✅ Code References tab
- ✅ GitHub link integration
- ✅ E2E testing
- ✅ Documentation

**Deliverables:**

- ✅ Working CLI tool
- ✅ AI Chat can answer code questions
- ✅ UI shows code references
- ✅ 80%+ accuracy on test codebase

### Phase 2: Enhancement (4-6 weeks)

**Week 7-8: Performance**

- Incremental indexing (git diff)
- Parallel processing
- Cache optimization
- Index compression

**Week 9-10: Advanced Features**

- Vector embeddings for semantic search
- Fuzzy matching
- Multi-repo support
- Monorepo handling

**Week 11-12: DX Improvements**

- Desktop app (Electron)
- VS Code extension
- Auto-sync on save
- CI/CD integration

**Deliverables:**

- ⚡ 10x faster re-indexing
- 🔍 Semantic code search
- 🖥️ Desktop companion app
- 🔄 Auto-sync workflow

### Phase 3: Scale (Optional, 6-8 weeks)

**Advanced Parsing:**

- Multi-language support (Java, Python, Go)
- Tree-sitter integration
- API schema extraction
- Test coverage mapping

**Advanced Analysis:**

- Impact analysis (change preview)
- Circular dependency detection
- Unused code detection
- Security vulnerability scanning

**Advanced Integration:**

- Backend service (optional)
- Real-time collaboration
- Team-wide code index
- Analytics dashboard

---

## 8. Risk Analysis

### Technical Risks

| Risk                                   | Probability | Impact | Mitigation                                                        |
| -------------------------------------- | ----------- | ------ | ----------------------------------------------------------------- |
| **AST parsing fails on complex code**  | Medium      | High   | - Fallback to regex<br>- Skip problematic files<br>- Log warnings |
| **Index too large (>10MB)**            | Low         | Medium | - Compression (gzip)<br>- Lazy loading<br>- Split by module       |
| **Performance issues (slow indexing)** | Medium      | Medium | - Parallel processing<br>- Incremental updates<br>- Cache AST     |
| **Annotation discipline not followed** | High        | High   | - Auto-detection heuristics<br>- Linter rules<br>- Documentation  |
| **GitHub rate limiting**               | Low         | Low    | - Primary use: local index<br>- GitHub as fallback only           |
| **Version conflicts (ts-morph)**       | Low         | Medium | - Pin versions<br>- Isolated package<br>- Clear docs              |

### Operational Risks

| Risk                              | Probability | Impact | Mitigation                                                          |
| --------------------------------- | ----------- | ------ | ------------------------------------------------------------------- |
| **Developers forget to re-index** | High        | Medium | - Git hooks (pre-commit)<br>- CI automation<br>- Stale detection UI |
| **Index out of sync with code**   | High        | High   | - Timestamp validation<br>- Auto-refresh prompt<br>- Watch mode     |
| **Multi-team coordination**       | Medium      | Medium | - Clear ownership<br>- Documentation<br>- Training                  |
| **Maintenance burden**            | Medium      | Medium | - Automated tests<br>- Simple architecture<br>- Good docs           |

### Mitigation Strategies

**For Annotation Discipline:**

```typescript
// ESLint rule to enforce annotations
{
  rules: {
    "pcm/require-screen-annotation": ["error", {
      "filePattern": "**/pages/**/*.tsx",
      "annotationPattern": "@pcm-screen:\\d+"
    }]
  }
}
```

**For Index Freshness:**

```javascript
// Auto-detect stale index
function checkIndexFreshness(codeIndex, gitCommit) {
  if (codeIndex.metadata.repository.commit !== gitCommit) {
    showWarning(
      "Code index may be out of date. " +
        "Run 'pcm-index --src ./src' to update.",
    );
  }
}
```

**For Performance:**

```typescript
// Incremental indexing
async function incrementalIndex(lastCommit, currentCommit) {
  const changedFiles = await git.diff(lastCommit, currentCommit);

  // Only re-index changed files (10x faster)
  for (const file of changedFiles) {
    await indexer.indexFile(file);
  }

  await indexer.rebuildRelationships();
}
```

---

## 9. Best Practices

### 9.1 Code Annotation Standards

```typescript
/**
 * Finance Approval Page
 * @pcm-screen:104
 * @pcm-project:RIP
 * @description Handles manual approval for high-value refunds
 */
export const FinanceApprovalPage: React.FC = () => {
  // Component implementation
};

/**
 * Fetch payout schedule
 * @pcm-api:POST /payout/v2/schedule
 * @pcm-table:payout_batches
 */
async function fetchPayoutSchedule(caseId: number) {
  const response = await fetch("/api/v2/payout/schedule", {
    method: "POST",
    body: JSON.stringify({ caseId }),
  });
  return response.json();
}
```

### 9.2 Project Structure

```
my-app/
├── .pcm/
│   ├── index.config.json       # Indexer configuration
│   ├── annotations.schema.json # Annotation schema
│   └── last-index.json         # Incremental tracking
├── src/
│   ├── pages/
│   │   └── FinanceApproval.tsx # @pcm-screen:104
│   ├── components/
│   └── services/
└── public/
    └── assets/
        └── code-index.json     # Generated index
```

### 9.3 Workflow Integration

```bash
# Git pre-commit hook (.git/hooks/pre-commit)
#!/bin/bash

echo "Checking PCM annotations..."
pnpm pcm:lint-annotations

if [ $? -ne 0 ]; then
  echo "❌ PCM annotation check failed"
  echo "Add @pcm-screen or @pcm-project annotations"
  exit 1
fi

echo "✅ PCM annotations OK"
```

```bash
# CI/CD (GitHub Actions)
name: Update Code Index

on:
  push:
    branches: [main, develop]

jobs:
  update-index:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Install dependencies
        run: pnpm install

      - name: Generate code index
        run: pnpm pcm:index --src ./src --out ./public/assets/code-index.json

      - name: Commit index
        run: |
          git config user.name "PCM Bot"
          git config user.email "bot@pcm.com"
          git add public/assets/code-index.json
          git commit -m "chore: update code index [skip ci]"
          git push
```

### 9.4 Team Guidelines

**For Developers:**

1. ✅ Add `@pcm-screen` annotation to all page components
2. ✅ Run `pnpm pcm:index` before pushing
3. ✅ Review code references in PCM UI
4. ✅ Update annotations when refactoring

**For Tech Leads:**

1. ✅ Set up pre-commit hooks
2. ✅ Configure CI/CD automation
3. ✅ Review index in PR checks
4. ✅ Enforce annotation linter

**For QA:**

1. ✅ Verify PCM data matches code
2. ✅ Use AI Chat for test planning
3. ✅ Check database impact before testing
4. ✅ Report annotation issues

---

## 10. Appendix

### 10.1 Example Use Cases

#### Example 1: Finding Code for a Feature Request

```
User: "Customer wants to add 'reason code' field to Finance Approval.
       What files do I need to change?"

AI Chat:
1. Calls: search_screens("Finance Approval")
   → Found screen #104

2. Calls: findSourceByScreen(104)
   → Returns:
     - Files: FinanceApproval.tsx, FinanceApprovalForm.tsx
     - API: POST /payout/v2/schedule
     - DB: payout_batches, refund_cases

3. Calls: getDatabaseTableUsage("payout_batches")
   → Shows other screens using this table

AI Response:
"To add 'reason code' to Finance Approval, you'll need to:

📝 Frontend Changes:
- apps/refund-intake/src/pages/FinanceApproval.tsx (line 45-60)
- apps/refund-intake/src/components/FinanceApprovalForm.tsx (add field)

🌐 API Changes:
- POST /payout/v2/schedule needs new parameter

🗄️ Database Changes:
- Add column to payout_batches table
- ⚠️ This table is also used by:
  * Screen #108 (Payout History)
  * Screen #112 (Batch Report)

💡 Recommendation:
1. Add DB migration first
2. Update API endpoint
3. Update frontend forms
4. Update the 2 screens that display this data

Would you like me to generate the migration SQL?"
```

#### Example 2: Impact Analysis

```
User: "We're deprecating the /api/v1/refund/submit endpoint.
       What will break?"

AI Chat:
1. Calls: getApiEndpointUsage("POST", "/api/v1/refund/submit")
   → Returns 12 call sites

AI Response:
"⚠️ Deprecating POST /api/v1/refund/submit will affect:

📱 Affected Screens (3):
- Screen #95: Refund Submission Form
- Screen #101: Quick Refund Widget
- Screen #103: Bulk Upload

📄 Files to Update (5):
- apps/refund/src/pages/RefundSubmission.tsx:142
- apps/refund/src/components/QuickRefund.tsx:89
- apps/refund/src/services/RefundApi.ts:45
- apps/admin/src/pages/BulkUpload.tsx:201
- apps/mobile/src/screens/RefundScreen.tsx:156

💡 Migration Strategy:
1. Update RefundApi.ts to use new endpoint
2. Test screens #95, #101, #103
3. Update mobile app
4. Keep v1 endpoint for 2 sprints (deprecation period)

Total effort: ~2 days (3 devs)
Risk: Medium (affects customer-facing features)"
```

### 10.2 Configuration Examples

#### Basic Config (Small Project)

```json
{
  "source": {
    "path": "./src",
    "include": ["**/*.{ts,tsx,js,jsx}"],
    "exclude": ["**/node_modules/**", "**/dist/**"]
  },
  "output": {
    "path": "./public/code-index.json",
    "format": "json",
    "compress": true
  },
  "parsing": {
    "typescript": { "enabled": true },
    "annotations": { "enabled": true },
    "apiCalls": { "enabled": true },
    "database": { "enabled": false }
  }
}
```

#### Advanced Config (Monorepo)

```json
{
  "source": {
    "path": ".",
    "include": [
      "apps/**/*.{ts,tsx}",
      "packages/**/*.{ts,tsx}",
      "services/**/*.{ts,java}"
    ],
    "exclude": [
      "**/node_modules/**",
      "**/dist/**",
      "**/*.test.ts",
      "**/*.spec.ts"
    ]
  },
  "output": {
    "path": "./apps/pcm-webapp/public/assets/code-index.json",
    "format": "json",
    "compress": true,
    "splitByModule": true
  },
  "parsing": {
    "typescript": {
      "enabled": true,
      "tsConfigPath": "./tsconfig.base.json",
      "extractTypes": true,
      "extractJsDoc": true
    },
    "java": {
      "enabled": true,
      "parser": "tree-sitter"
    },
    "annotations": {
      "enabled": true,
      "patterns": {
        "screen": "@pcm-screen:(\\d+)",
        "project": "@pcm-project:([\\w-]+)",
        "table": "@pcm-table:([\\w_]+)",
        "api": "@pcm-api:(\\w+)\\s+([^\\s]+)"
      }
    },
    "apiCalls": {
      "enabled": true,
      "patterns": [
        "fetch\\(['\"]([^'\"]+)['\"]",
        "axios\\.\\w+\\(['\"]([^'\"]+)['\"]",
        "RestTemplate\\.\\w+\\(['\"]([^'\"]+)['\"]"
      ]
    },
    "database": {
      "enabled": true,
      "ormDetection": ["prisma", "typeorm", "mybatis"],
      "rawQueryPatterns": [
        "db\\.query\\(",
        "connection\\.execute\\(",
        "@Query\\(",
        "@Select\\("
      ]
    }
  },
  "analysis": {
    "calculateComplexity": true,
    "buildDependencyGraph": true,
    "detectCircularDeps": true,
    "findUnusedExports": true
  },
  "git": {
    "includeBlame": true,
    "includeCommitInfo": true,
    "trackChanges": true
  },
  "performance": {
    "parallel": true,
    "maxWorkers": 8,
    "cacheDir": "./.pcm-index-cache",
    "incremental": true
  },
  "modules": [
    {
      "name": "refund-intake",
      "path": "apps/refund-intake",
      "project": "RIP"
    },
    {
      "name": "user-service",
      "path": "services/user",
      "project": "IAM"
    }
  ]
}
```

### 10.3 Command Reference

```bash
# Basic Commands
pcm-index --src ./src --out ./public/code-index.json
pcm-index --config ./pcm-index.config.json
pcm-index --help
pcm-index --version

# Advanced Options
pcm-index --src ./src --verbose                    # Debug output
pcm-index --src ./src --incremental                # Only changed files
pcm-index --src ./src --watch                      # Watch mode
pcm-index --src ./src --parallel --workers 8       # Parallel processing
pcm-index --src ./src --no-cache                   # Disable cache
pcm-index --src ./src --validate                   # Validate annotations

# Specific Parsers
pcm-index --src ./src --only typescript            # Only TS/JS files
pcm-index --src ./src --only annotations           # Only annotation scan
pcm-index --src ./src --skip database              # Skip DB parsing

# Output Formats
pcm-index --src ./src --out ./index.json           # JSON (default)
pcm-index --src ./src --out ./index.sqlite         # SQLite
pcm-index --src ./src --out ./index.msgpack        # MessagePack (smaller)

# CI/CD
pcm-index --src ./src --ci                         # CI mode (no colors)
pcm-index --src ./src --check                      # Check only (no output)
pcm-index --src ./src --diff HEAD~1                # Compare with previous

# Utilities
pcm-index --lint-annotations ./src                 # Check annotation syntax
pcm-index --stats ./src                            # Show statistics
pcm-index --export-schema > schema.json            # Export index schema
```

### 10.4 FAQ

**Q: Do I need to run the indexer every time I change code?**
A: No. Use `--watch` mode during development, or set up pre-commit hooks. In production, CI/CD handles it automatically.

**Q: What if I have multiple repos?**
A: Run the indexer for each repo, then merge the indexes, or use multi-repo config.

**Q: Can it work without annotations?**
A: Yes, but with lower accuracy. The indexer will use heuristics (file names, folder structure), but annotations ensure correctness.

**Q: How big is the generated index?**
A: Typically 1-5MB for 1000-5000 files. Compression can reduce by 70%.

**Q: Does it support languages other than TypeScript?**
A: Phase 1: TS/JS only. Phase 2 will add Java, Python, Go via tree-sitter.

**Q: What about private repos?**
A: All processing is local. The index never leaves your machine unless you commit it.

**Q: Can multiple developers share one index?**
A: Yes, commit `code-index.json` to git. CI regenerates on every merge.

---

## 📊 Success Metrics

### KPIs (3 months after deployment)

| Metric                     | Target                                     | Measurement         |
| -------------------------- | ------------------------------------------ | ------------------- |
| **Adoption Rate**          | 80% of devs use AI Chat for code questions | Analytics           |
| **Accuracy**               | 90%+ correct responses                     | User feedback       |
| **Time Savings**           | 50% reduction in "find code" time          | Before/after survey |
| **Index Freshness**        | <24h lag between code & index              | Automated check     |
| **Developer Satisfaction** | 8/10 average rating                        | Quarterly survey    |

---

## ✅ Conclusion & Recommendations

### Summary

This comprehensive plan provides a **practical, incremental approach** to linking source code with PCM WebApp:

1. ✅ **Phase 1 (Foundation)**: Annotation-based system with ts-morph parsing
2. 🔄 **Phase 2 (Enhancement)**: Semantic search, incremental updates, desktop app
3. 🚀 **Phase 3 (Scale)**: Multi-language, advanced analysis, team collaboration

### Recommended Next Steps

**Immediate (Week 1):**

1. Review and approve this plan
2. Setup `packages/code-indexer` project
3. Define annotation standards
4. Create test codebase (10-20 files)

**Short-term (Week 2-4):**

1. Build MVP CLI indexer
2. Test on real project
3. Integrate with AI Chat
4. Collect feedback

**Medium-term (Month 2-3):**

1. Refine based on feedback
2. Add UI enhancements
3. Setup automation (git hooks, CI/CD)
4. Roll out to team

### Risk Mitigation

- Start small (1 module)
- Iterate based on feedback
- Maintain escape hatches (GitHub API fallback)
- Document everything

### Final Thoughts

This system will **transform how developers interact with PCM WebApp**, turning it from a business metadata tool into a **comprehensive development companion**. The AI Chat will finally be able to give **accurate, verified answers** about code structure, dependencies, and impact analysis.

**The key to success is:**

- ✅ Simple, clear annotation standards
- ✅ Automated tooling (hooks, CI/CD)
- ✅ Incremental rollout
- ✅ Continuous feedback loop

---

**Document Version:** 2.0  
**Last Updated:** 2025-11-09  
**Next Review:** 2025-12-01  
**Owner:** AI Architecture Team

**Questions? Feedback?**
Open an issue at: `noteflix/issues` or contact the team via Slack: `#pcm-webapp`
