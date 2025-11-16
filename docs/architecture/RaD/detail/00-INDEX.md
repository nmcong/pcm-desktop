# PCM Desktop - Detailed Design Documentation Index

**Phiên bản:** 1.0  
**Ngày tạo:** 2025-11-15  
**Tổng số tài liệu:** 7

---

## 📋 Tổng quan

Bộ tài liệu Detailed Design được tạo ra từ các file nguồn trong `docs/RaD/ideas/`, cung cấp chi tiết kỹ thuật đầy đủ cho
việc implementation PCM Desktop system.

**Nguyên tắc tạo tài liệu:**

- ✅ Đầy đủ nội dung từ các file nguồn
- ✅ Mỗi file detailed design ghi rõ nguồn gốc
- ✅ Code examples và implementation details
- ✅ Database schemas, API specs, UI mockups
- ✅ Best practices và troubleshooting guides

---

## 📚 Danh sách tài liệu

### 01. Database Schema

**File:** `01-database-schema-detailed-design.md`  
**Tạo từ:**

- `system-hierarchy.md`
- `system-analysis.md`
- `system-analysis-erd.md`
- `0003_add_semantic_search.sql`
- `0004_add_chm_tables.sql`

**Nội dung:**

- ✅ SQLite schema đầy đủ (18 bảng)
- ✅ System hierarchy (System → Subsystem → Project → Batch)
- ✅ Source file management & AST storage
- ✅ Semantic search (FTS5) & vector documents
- ✅ CHM integration tables
- ✅ User requests & agent responses
- ✅ Review comments & test catalog
- ✅ ERD diagram
- ✅ Migration scripts & index strategy
- ✅ Performance optimization tips

**Khi nào cần đọc:**

- Thiết kế database schema mới
- Tạo migration scripts
- Optimize queries và indexes
- Hiểu data model và relationships

---

### 02. RAG & Search Architecture

**File:** `02-rag-search-architecture-detailed-design.md`  
**Tạo từ:**

- `rag-strategy.md`
- `semantic-search.md`
- `source-search-guide.md`

**Nội dung:**

- ✅ Hybrid retrieval architecture (vector + lexical)
- ✅ Chunking strategies (code, docs, responses)
- ✅ Embedding generation & caching
- ✅ FTS5 full-text search với BM25 scoring
- ✅ Qdrant vector search configuration
- ✅ Fusion algorithms (RRF, weighted sum)
- ✅ Context assembly & token budgeting
- ✅ LLM integration (OpenAI, Anthropic, Local)
- ✅ Quality metrics & monitoring
- ✅ Deployment modes (Cloud, Hybrid, Offline)

**Khi nào cần đọc:**

- Implement RAG pipeline
- Configure search engines
- Optimize retrieval quality
- Setup embeddings và LLM providers

---

### 03. AST & Code Analysis

**File:** `03-ast-code-analysis-detailed-design.md`  
**Tạo từ:**

- `ast-source-analysis.md`

**Nội dung:**

- ✅ AST parsing pipeline (source scan → parse → store)
- ✅ Multi-language parser support (Java, Python, TypeScript, etc.)
- ✅ AST nodes, relationships, dependencies storage
- ✅ Impact analysis implementation
- ✅ Snapshot management & diffing
- ✅ Integration với RAG (AST-aware chunking)
- ✅ Code snippet formatting cho chat UI
- ✅ Function calling endpoints
- ✅ Performance optimization (batch inserts, caching)

**Khi nào cần đọc:**

- Implement source code parsing
- Add new language parser
- Build impact analyzer
- Display code snippets trong UI

---

### 04. CHM Integration

**File:** `04-chm-integration-detailed-design.md`  
**Tạo từ:**

- `chm-ingestion.md`

**Nội dung:**

- ✅ CHM extraction tools (Windows, Linux, macOS)
- ✅ Data model (chm_imports, chm_documents, chm_assets)
- ✅ Import pipeline (extract → parse → normalize → index)
- ✅ TOC (Table of Contents) parsing
- ✅ HTML processing & encoding handling
- ✅ Feed to search_corpus & vector_documents
- ✅ UI integration (import dialog, browser)
- ✅ Security & best practices
- ✅ Troubleshooting common issues

**Khi nào cần đọc:**

- Implement CHM import feature
- Handle legacy documentation
- Debug extraction problems
- Build CHM browser UI

---

### 05. Code Review & Testing

**File:** `05-code-review-testing-detailed-design.md`  
**Tạo từ:**

- `code-review-strategy.md`
- `testcase-strategy.md`

**Nội dung:**

- ✅ Automated code review architecture
- ✅ Heuristic rules (null safety, security, i18n, performance)
- ✅ Review comments data model & storage
- ✅ LLM-assisted review integration
- ✅ Test case generation strategies
- ✅ Impact-based test planning
- ✅ Edge case & boundary testing
- ✅ I18N test data (Korean, Chinese, Japanese, Vietnamese, emojis)
- ✅ Test recommendations với priority
- ✅ UI panels cho review và testing

**Khi nào cần đọc:**

- Build automated code review
- Implement test generation
- Add new review rules
- Generate test plans

---

### 06. Integration Flow & Architecture

**File:** `06-integration-flow-detailed-design.md`  
**Tạo từ:**

- `request-to-code-flow.md`
- `function-calling-spec.md`
- `project-structure.md`

**Nội dung:**

- ✅ Request-to-Code flow (end-to-end)
- ✅ Module architecture & sequence diagrams
- ✅ Service implementations (RequestService, RetrievalService, AstContextService, ImpactAnalyzer)
- ✅ Function calling specification (13+ API endpoints)
- ✅ REST API design với examples
- ✅ Clean Architecture package structure
- ✅ Dependency rules (application → domain ← infrastructure)
- ✅ Configuration management (application.yml)
- ✅ Testing strategy (unit, integration)
- ✅ Build & deployment

**Khi nào cần đọc:**

- Understand system architecture
- Implement new use cases
- Design API endpoints
- Setup project structure
- Configure deployment

---

### 07. UI & Domain Overview

**File:** `07-ui-domain-detailed-design.md`  
**Tạo từ:**

- `ui-domain-overview.md`
- `ui-ui-review.md`

**Nội dung:**

- ✅ Core domain entities & ERD
- ✅ Screen catalogue (8 main screens):
    - System Hierarchy Management
    - Source Repository Manager
    - AST & Dependency Explorer
    - Semantic Search Console
    - Knowledge Import Center
    - RAG Workspace (AI Assistant)
    - Request Tracking & History
    - Settings & Analytics
- ✅ MVVM architecture pattern
- ✅ Data binding & command pattern
- ✅ Known UI issues & fixes:
    - DatabaseObjectsPage: Schema tree never updates
    - AIAssistantPage: Messages never persist
    - Service calls blocking FX thread
    - Settings listeners registered repeatedly
- ✅ UI best practices (threading, binding, errors)
- ✅ Future enhancements (dark mode, accessibility)

**Khi nào cần đọc:**

- Build UI screens
- Fix UI bugs
- Implement MVVM pattern
- Understand domain model
- Plan UI improvements

---

## 🗺️ Navigation Guide

### Theo role/nhiệm vụ:

#### Backend Developer

1. Start: **01-Database Schema** (hiểu data model)
2. Then: **03-AST & Code Analysis** (parsing & storage)
3. Then: **02-RAG & Search** (retrieval pipeline)
4. Then: **06-Integration Flow** (service layer)
5. Optional: **04-CHM Integration**, **05-Code Review & Testing**

#### Frontend Developer

1. Start: **07-UI & Domain** (screens & MVVM)
2. Then: **06-Integration Flow** (API contracts)
3. Then: **02-RAG & Search** (understand retrieval results)
4. Optional: **01-Database Schema** (data model reference)

#### Full-stack Developer

1. **06-Integration Flow** (big picture)
2. **01-Database Schema** (foundation)
3. **07-UI & Domain** (user interface)
4. Pick others based on feature needs

#### Architect/Tech Lead

1. **06-Integration Flow** (architecture overview)
2. **02-RAG & Search** (core technology)
3. **01-Database Schema** (data design)
4. Skim others for completeness

---

## 📊 Document Statistics

| Tài liệu                 | Lines       | Sections | Code Examples   | Diagrams       |
|--------------------------|-------------|----------|-----------------|----------------|
| 01-Database Schema       | ~1700       | 18       | 50+ SQL/Java    | 1 ERD          |
| 02-RAG & Search          | ~1600       | 14       | 40+ Java/Python | 1 architecture |
| 03-AST & Code            | ~1200       | 10       | 30+ Java        | 1 flow         |
| 04-CHM Integration       | ~900        | 9        | 20+ Java        | 0              |
| 05-Code Review & Testing | ~1100       | 6        | 25+ Java        | 1 flow         |
| 06-Integration Flow      | ~1300       | 7        | 35+ Java/YAML   | 2 diagrams     |
| 07-UI & Domain           | ~1400       | 7        | 30+ Java        | 1 ERD          |
| **TOTAL**                | **~10,200** | **71**   | **230+**        | **7**          |

---

## 🔍 Quick Reference

### Tìm thông tin về...

**Database:**

- Schema design → 01 §2-§11
- Migrations → 01 §16
- Performance → 01 §12

**Search & Retrieval:**

- Vector search → 02 §5
- FTS5 search → 02 §4
- Hybrid retrieval → 02 §6
- Chunking → 02 §3

**AST & Parsing:**

- Parser implementation → 03 §3
- AST storage → 03 §2
- Impact analysis → 03 §4
- Snippet formatting → 03 §6

**API Endpoints:**

- Function catalog → 06 §3.1
- REST API → 06 §3.2
- Request flow → 06 §2

**UI Screens:**

- Screen list → 07 §3
- MVVM pattern → 07 §4
- Known issues → 07 §5

---

## 🛠️ Implementation Checklist

Khi implement một feature mới:

1. **Phase 1: Database**
    - [ ] Read §01: Database Schema
    - [ ] Design tables và relationships
    - [ ] Create migration script
    - [ ] Add indexes

2. **Phase 2: Domain & Application**
    - [ ] Read §06: Integration Flow (§4.2 Domain package)
    - [ ] Define domain entities
    - [ ] Define repository interfaces
    - [ ] Implement use cases

3. **Phase 3: Infrastructure**
    - [ ] Read §06: Integration Flow (§4.3 Infrastructure package)
    - [ ] Implement repositories
    - [ ] Add external clients (if needed)
    - [ ] Write integration tests

4. **Phase 4: API (nếu cần)**
    - [ ] Read §06: Function Calling (§3.2)
    - [ ] Design REST endpoints
    - [ ] Implement controllers
    - [ ] Add API tests

5. **Phase 5: UI**
    - [ ] Read §07: UI & Domain (§3)
    - [ ] Design screen layout
    - [ ] Implement ViewModel
    - [ ] Implement View with data binding
    - [ ] Test user interactions

6. **Phase 6: Testing & Docs**
    - [ ] Write unit tests
    - [ ] Write integration tests
    - [ ] Update documentation
    - [ ] Add troubleshooting guide

---

## 📞 Support & Contribution

**Maintainer:** PCM Desktop Team  
**Last Updated:** 2025-11-15  
**Version:** 1.0

**Liên hệ:**

- Issues: GitHub Issues
- Docs: `docs/RaD/detail/`
- Examples: `examples/`

**Contribute:**

1. Đọc tài liệu liên quan
2. Implement theo Clean Architecture
3. Follow coding standards (AGENTS.md)
4. Add tests
5. Update docs nếu cần

---

## 🎯 Next Steps

**Recommended reading order cho người mới:**

1. **Day 1:** Read §00 (this file) + §06 (Integration Flow) → Hiểu big picture
2. **Day 2:** Read §01 (Database) + §07 (UI) → Hiểu foundation
3. **Day 3:** Read §02 (RAG) + §03 (AST) → Hiểu core technology
4. **Day 4+:** Read §04, §05 as needed → Feature-specific knowledge

**Sau khi đọc xong:**

- Clone repository
- Run `./scripts/setup.sh`
- Follow `docs/guides/QUICK_START.md`
- Build first feature!

---

**Happy coding! 🚀**

