# PCM Desktop - Architecture Documentation Index

**Phiên bản:** 1.0  
**Ngày tạo:** 2025-11-15  
**Tổng số tài liệu:** 4

---

## 📋 Tổng quan

Bộ tài liệu Architecture này được tạo ra dựa trên phân tích chi tiết từ `docs/RaD/detail/`, cung cấp **blueprint hoàn chỉnh** để implement PCM Desktop.

**Phạm vi tài liệu:**
- ✅ Thông tin được quản lý (user inputs)
- ✅ Cấu trúc menu và màn hình
- ✅ Chi tiết từng field và modal
- ✅ Kiến trúc code (Services, Repos, Entities, DTOs, VOs)

---

## 📚 Danh sách tài liệu

### 01. Data Management Overview
**File:** `01-data-management-overview.md`  
**Dòng:** ~900 lines  
**Mục đích:** Mô tả chi tiết thông tin được nhập bởi user

**Nội dung:**
- ✅ **10+ loại thông tin user input:**
  - System Hierarchy (System, Subsystem, Project, Batch)
  - Source Code Management
  - Knowledge Management (CHM, articles)
  - User Requests & Interactions
  - Feedback
  - Test Management
  
- ✅ **Auto-generated data:** 20+ entity types
- ✅ **Quy trình nhập liệu:** 5 workflows chính
- ✅ **Validation rules:** Đầy đủ cho mỗi field
- ✅ **Data relationships:** ERD diagram
- ✅ **Import/Export capabilities**

**Khi nào cần đọc:**
- Hiểu business domain
- Design forms và validation
- Plan data migration
- Understand user workflows

---

### 02. Screen Structure & Navigation
**File:** `02-screen-structure.md`  
**Dòng:** ~1400 lines  
**Mục đích:** Mô tả chi tiết cấu trúc menu và 8 màn hình chính

**Nội dung:**
- ✅ **Main Menu Bar:** 6 menu (File, Edit, View, Navigate, Tools, Help)
- ✅ **8 Main Screens:**
  1. 💬 AI Assistant (Chat interface)
  2. 🏢 System Manager (Hierarchy tree)
  3. 📁 Source Manager (Repository tracking)
  4. 🌳 AST Explorer (Code structure)
  5. 🔍 Search Console (Semantic search)
  6. 📚 Knowledge Center (CHM import)
  7. 📊 Request History (Tracking)
  8. ⚙️ Settings (Configuration)

- ✅ **Screen layouts:** ASCII diagrams cho mỗi màn hình
- ✅ **Components:** Chi tiết UI elements
- ✅ **Modals:** 20+ dialog specifications
- ✅ **Navigation flow:** User journeys
- ✅ **Keyboard shortcuts:** Đầy đủ

**Khi nào cần đọc:**
- Design UI layouts
- Implement screens
- Plan navigation flow
- Design keyboard shortcuts

---

### 03. Screen Field Specifications
**File:** `03-screen-field-specifications.md`  
**Dòng:** ~1500 lines  
**Mục đích:** Chi tiết từng field trong mỗi form và modal

**Nội dung:**
- ✅ **15+ Forms chi tiết:**
  - System/Subsystem/Project/Batch forms
  - Source addition forms
  - CHM import forms
  - Knowledge article forms
  - Search forms
  - Settings forms
  
- ✅ **Mỗi field có:**
  - Type (TextField, ComboBox, DatePicker, etc.)
  - Required/Optional
  - Validation rules
  - Default values
  - Helper text/tooltips
  - Error messages
  
- ✅ **Modal specifications:**
  - Layout diagrams
  - Button states
  - Progress indicators
  - Confirmation dialogs
  
- ✅ **Common validation patterns:** 7 types
- ✅ **Field types reference:** 15+ controls
- ✅ **Accessibility attributes:** ARIA labels

**Khi nào cần đọc:**
- Implement forms
- Design validation
- Build custom controls
- Ensure accessibility

---

### 04. Architecture Layers
**File:** `04-architecture-layers.md`  
**Dòng:** ~2000 lines  
**Mục đích:** Mô tả chi tiết kiến trúc code (Services, Repos, Entities, DTOs, VOs)

**Nội dung:**
- ✅ **Clean Architecture:** 4 layers
  - UI Layer (JavaFX)
  - Application Layer (Use Cases)
  - Domain Layer (Entities, VOs, Repos)
  - Infrastructure Layer (Adapters)
  
- ✅ **20+ Entity classes:** Đầy đủ code
  - System, Subsystem, Project, Batch
  - ProjectSource, SourceFile
  - AstSnapshot, AstNode
  - UserRequest, AgentResponse
  - CHM entities
  - Review, Test entities
  
- ✅ **Value Objects (VOs):** 5+ examples
  - EmbeddingVector
  - FilePath
  - ChunkMetadata
  - SearchScore
  
- ✅ **Repository Interfaces:** 10+ repos
  - Base repository pattern
  - Domain-specific methods
  - Pagination support
  
- ✅ **DTOs:** 15+ data transfer objects
  - Request/Response DTOs
  - Search result DTOs
  - AST node DTOs
  
- ✅ **Mappers:** Entity ↔ DTO conversion
  
- ✅ **Use Cases:** 5+ examples với code
  - Submit user request
  - Hybrid retrieval
  - Source scanning
  
- ✅ **Services:** 8+ core services
  - System management
  - Embedding service
  - Fusion service
  - AST parser service

**Khi nào cần đọc:**
- Understand system architecture
- Implement entities
- Design services
- Build repositories
- Create DTOs và mappers

---

## 🗺️ Navigation Guide

### Theo role/nhiệm vụ:

#### Product Manager / BA
1. **01-Data Management** → Hiểu business domain
2. **02-Screen Structure** → Understand user flows
3. Skip: 03, 04

#### UI/UX Designer
1. **02-Screen Structure** → Screen layouts
2. **03-Field Specifications** → Field details
3. **01-Data Management** (§2-§6) → Data to display
4. Skip: 04

#### Frontend Developer
1. **02-Screen Structure** → What to build
2. **03-Field Specifications** → How to build
3. **04-Architecture** (§3.2, §4.1-§4.2) → DTOs và ViewModels
4. **01-Data Management** (§5, §7) → Validation rules

#### Backend Developer
1. **04-Architecture** → Complete read (entities, repos, services)
2. **01-Data Management** (§2-§5) → Business rules
3. Optional: 02, 03 (để hiểu UI requirements)

#### Full-stack Developer
1. **04-Architecture** → Foundation
2. **02-Screen Structure** → UI overview
3. **03-Field Specifications** → Implementation details
4. **01-Data Management** → Business context

#### Architect / Tech Lead
1. **04-Architecture** (all) → System design
2. **01-Data Management** (§7-§10) → Data flow
3. Skim: 02, 03

---

## 📊 Quick Reference

### Tìm thông tin về...

**User Input:**
- Hierarchy (System/Subsystem/Project/Batch) → 01 §2.1
- Source management → 01 §2.2
- Knowledge (CHM, articles) → 01 §2.3
- User requests → 01 §2.4
- Feedback → 01 §2.4
- Test cases → 01 §2.6

**Screens:**
- AI Assistant (chat) → 02 §3.1
- System Manager → 02 §3.2
- Source Manager → 02 §3.3
- AST Explorer → 02 §3.4
- Search Console → 02 §3.5
- Knowledge Center → 02 §3.6
- Request History → 02 §3.7
- Settings → 02 §3.8

**Forms:**
- System forms → 03 §1.1-1.4
- Source forms → 03 §2.1-2.3
- Knowledge forms → 03 §3.1-3.4
- Chat input → 03 §4.1-4.3
- Search forms → 03 §5.1-5.2
- Settings → 03 §6.1-6.4

**Code Architecture:**
- Package structure → 04 §2
- Entities → 04 §3.1
- Value Objects → 04 §3.2
- Repositories → 04 §3.3
- DTOs → 04 §4.1
- Mappers → 04 §4.2
- Use Cases → 04 §4.3
- Services → 04 §5.2

---

## 🎯 Implementation Checklist

Khi implement một feature mới, follow theo thứ tự:

### Phase 1: Business Understanding
- [ ] Read `01-Data Management` (relevant sections)
- [ ] Understand user inputs
- [ ] Understand validation rules
- [ ] Understand data relationships

### Phase 2: UI Design
- [ ] Read `02-Screen Structure` (relevant screen)
- [ ] Understand screen layout
- [ ] Understand navigation flow
- [ ] Design screen mockup

### Phase 3: Field Details
- [ ] Read `03-Field Specifications` (relevant forms)
- [ ] List all fields
- [ ] Define validation rules
- [ ] Design error handling

### Phase 4: Domain Layer
- [ ] Read `04-Architecture` §3 (Domain)
- [ ] Define entities
- [ ] Define value objects
- [ ] Define repository interfaces

### Phase 5: Application Layer
- [ ] Read `04-Architecture` §4 (Application)
- [ ] Define DTOs
- [ ] Create mappers
- [ ] Implement use cases

### Phase 6: Infrastructure Layer
- [ ] Read `04-Architecture` §5 (Infrastructure)
- [ ] Implement repositories
- [ ] Implement services
- [ ] Add external integrations

### Phase 7: UI Implementation
- [ ] Implement ViewModels (reference `02-Screen Structure`)
- [ ] Implement Views với data binding
- [ ] Implement forms với validation (reference `03-Field Specifications`)
- [ ] Test user workflows

### Phase 8: Testing & Documentation
- [ ] Unit tests (domain, application)
- [ ] Integration tests (infrastructure)
- [ ] UI tests (user flows)
- [ ] Update documentation if needed

---

## 📈 Statistics

| Tài liệu | Lines | Sections | Diagrams | Code Examples |
|----------|-------|----------|----------|---------------|
| 01-Data Management | ~900 | 10 | 2 | 10+ |
| 02-Screen Structure | ~1400 | 7 | 9+ | 5+ |
| 03-Field Specifications | ~1500 | 9 | 10+ | 20+ |
| 04-Architecture Layers | ~2000 | 6 | 2 | 40+ |
| **TOTAL** | **~5800** | **32** | **23+** | **75+** |

---

## 🔍 Cross-References

### Screen → Code
| Screen | Entities | Services | Use Cases |
|--------|----------|----------|-----------|
| AI Assistant | UserRequest, AgentResponse | RetrievalService, LlmService | SubmitRequestUseCase, StreamResponseUseCase |
| System Manager | System, Subsystem, Project, Batch | SystemManagementService | CreateSystemUseCase, DeleteSystemUseCase |
| Source Manager | ProjectSource, SourceFile | SourceScanService | ScanProjectSourceUseCase |
| AST Explorer | AstNode, AstRelationship | AstParserService | ParseSourceUseCase, FindSymbolUseCase |
| Search Console | SearchCorpus | VectorSearchService, LexicalSearchService | HybridRetrievalUseCase |
| Knowledge Center | ChmImport, ChmDocument | ChmImportService | ImportChmUseCase |
| Request History | UserRequest, AgentResponse | RequestService | ListRequestsUseCase |
| Settings | - | ConfigService | UpdateSettingsUseCase |

### Form → Validation → Entity
| Form | Validation (Doc 03) | Entity (Doc 04) | DTO (Doc 04) |
|------|---------------------|-----------------|--------------|
| System Form | §1.1 | System | SystemDTO, CreateSystemRequest |
| Project Form | §1.3 | Project | ProjectDTO, CreateProjectRequest |
| Add Source | §2.1 | ProjectSource | ProjectSourceDTO, AddSourceRequest |
| CHM Import | §3.1 | ChmImport | ChmImportDTO, ImportChmRequest |
| Chat Input | §4.1 | UserRequest | UserRequestDTO, CreateRequestCommand |

---

## 🚀 Quick Start Guide

**Cho người mới:**

### Day 1: Overview
1. Read `00-INDEX.md` (this file) → 30 mins
2. Skim `01-Data Management` §1-§2 → 30 mins
3. Browse `02-Screen Structure` §2-§3 → 1 hour
4. **Goal:** Hiểu big picture

### Day 2: Deep Dive (theo role)
**Backend:**
- Read `04-Architecture` completely → 3 hours
- Read `01-Data Management` §2-§7 → 1 hour

**Frontend:**
- Read `02-Screen Structure` completely → 2 hours
- Read `03-Field Specifications` §1-§6 → 2 hours

**Full-stack:**
- Read `04-Architecture` §1-§4 → 2 hours
- Read `02-Screen Structure` §3 (8 screens overview) → 1 hour
- Read `03-Field Specifications` (skim) → 1 hour

### Day 3: Implementation Prep
- Setup development environment
- Clone repository
- Review existing code structure
- Map docs → code
- Identify first task

### Day 4+: Implementation
- Follow Implementation Checklist (above)
- Reference docs as needed
- Ask questions
- Contribute!

---

## 📞 Support & Contribution

**Maintainer:** PCM Desktop Team  
**Last Updated:** 2025-11-15  
**Version:** 1.0

**Đóng góp:**
1. Đọc tài liệu liên quan
2. Follow Architecture patterns
3. Add tests
4. Update docs nếu có thay đổi

**Câu hỏi thường gặp:**
- Q: "Tôi cần implement screen mới, bắt đầu từ đâu?"
  - A: Read `02-Screen Structure` → Design layout → Read `03-Field Specifications` → Read `04-Architecture` §3-§4 → Implement
  
- Q: "Làm sao để validate user input?"
  - A: Read `01-Data Management` §5 + `03-Field Specifications` (relevant form section)
  
- Q: "Entity và DTO khác nhau thế nào?"
  - A: Read `04-Architecture` §3.1 (Entities) và §4.1 (DTOs) + §4.2 (Mappers)
  
- Q: "Clean Architecture là gì?"
  - A: Read `04-Architecture` §1 (Architecture Overview)

---

## 🎓 Related Documentation

**Also see:**
- `docs/RaD/detail/` - Technical detailed design (source docs)
- `docs/architecture/` - This folder (architecture docs)
- `docs/guides/` - Quick start và step-by-step guides
- `docs/troubleshooting/` - Common issues và solutions
- `AGENTS.md` - Repository guidelines

**Build & Run:**
- `scripts/build.sh` - Build application
- `scripts/run.sh` - Run application
- `scripts/test.sh` - Run tests
- `README.md` - Project overview

---

## 📖 Document Relationships

```
                    ┌─────────────────────┐
                    │   00-INDEX.md       │
                    │   (This file)       │
                    └──────────┬──────────┘
                               │
            ┌──────────────────┼──────────────────┐
            │                  │                  │
            ▼                  ▼                  ▼
    ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
    │ 01-Data Mgmt │  │ 02-Screens   │  │ 04-Arch      │
    │ (Business)   │  │ (UI Design)  │  │ (Code)       │
    └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
           │                  │                  │
           │         ┌────────┴────────┐        │
           │         │                 │        │
           │         ▼                 │        │
           │  ┌──────────────┐        │        │
           │  │ 03-Fields    │        │        │
           │  │ (Details)    │        │        │
           │  └──────┬───────┘        │        │
           │         │                 │        │
           └─────────┴─────────────────┴────────┘
                            │
                     All feed into
                            │
                            ▼
                    ┌──────────────┐
                    │ Implementation│
                    └──────────────┘
```

---

## ✅ Checklist: Have You Read?

Before starting implementation:
- [ ] Read this INDEX completely
- [ ] Identified your role (PM/Designer/Frontend/Backend/Full-stack/Architect)
- [ ] Read recommended docs for your role
- [ ] Understood data management basics (Doc 01)
- [ ] Familiar with screen structure (Doc 02)
- [ ] Know where to find field details (Doc 03)
- [ ] Understood architecture layers (Doc 04)
- [ ] Setup development environment
- [ ] Ready to code! 🚀

---

**Happy Coding! 💪**

_"Good architecture is not about getting it right the first time, it's about making it easy to change."_ 

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-15  
**Status:** ✅ Complete

