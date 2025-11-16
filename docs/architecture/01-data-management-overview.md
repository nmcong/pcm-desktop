# Data Management Overview - PCM Desktop

**Phiên bản:** 1.0  
**Ngày tạo:** 2025-11-15  
**Mục đích:** Mô tả chi tiết các thông tin được quản lý trong hệ thống

---

## 1. Tổng quan

PCM Desktop là một hệ thống **Requirement Analysis & Code Intelligence Platform** quản lý và phân tích:
- Phân cấp tổ chức (Systems, Subsystems, Projects, Batches)
- Mã nguồn và AST (Abstract Syntax Tree)
- Knowledge base (Documentation, CHM files)
- User requests và AI responses
- Code reviews và test recommendations

---

## 2. Thông tin được nhập bởi User

### 2.1 System Hierarchy (Phân cấp hệ thống)

#### **System (Hệ thống)**
**Input Fields:**
- ✏️ `code` - Mã định danh (VD: "ERP", "CRM") - **Required, Unique**
- ✏️ `name` - Tên hệ thống (VD: "Enterprise Resource Planning") - **Required**
- ✏️ `description` - Mô tả chi tiết - Optional
- ✏️ `owner` - Người chịu trách nhiệm - Optional

**User Actions:**
- Create new System
- Edit existing System
- Delete System (cascade delete subsystems)
- View System details

---

#### **Subsystem (Hệ thống con)**
**Input Fields:**
- 🔗 `system_id` - Thuộc System nào - **Required, Dropdown**
- ✏️ `code` - Mã định danh (unique trong System) - **Required**
- ✏️ `name` - Tên subsystem (VD: "Human Resources") - **Required**
- ✏️ `description` - Mô tả chi tiết - Optional
- ✏️ `tech_stack` - Công nghệ sử dụng (VD: "Java, Spring Boot") - Optional
- 🎚️ `status` - Trạng thái - **Dropdown: active, deprecated, archived**

**User Actions:**
- Create new Subsystem under System
- Edit Subsystem
- Delete Subsystem (cascade delete projects/batches)
- Change status
- View Subsystem details

---

#### **Project (Dự án)**
**Input Fields:**
- 🔗 `subsystem_id` - Thuộc Subsystem nào - **Required, Dropdown**
- ✏️ `code` - Mã dự án (unique trong Subsystem) - **Required**
- ✏️ `name` - Tên dự án (VD: "Payroll System") - **Required**
- ✏️ `description` - Mô tả chi tiết - Optional
- ✏️ `lead` - Người dẫn đầu dự án - Optional
- 🎚️ `status` - Trạng thái - **Dropdown: draft, active, completed, cancelled**
- 📅 `start_date` - Ngày bắt đầu - Optional, Date picker
- 📅 `end_date` - Ngày kết thúc - Optional, Date picker

**User Actions:**
- Create new Project under Subsystem
- Edit Project
- Delete Project
- Attach source code repositories
- View Project details và progress

---

#### **Batch Job (Công việc batch)**
**Input Fields:**
- 🔗 `subsystem_id` - Thuộc Subsystem nào - **Required, Dropdown**
- ✏️ `code` - Mã batch (unique trong Subsystem) - **Required**
- ✏️ `name` - Tên batch job - **Required**
- ✏️ `description` - Mô tả chi tiết - Optional
- ⏰ `schedule_cron` - Cron expression (VD: "0 0 * * *") - Optional
- 🎚️ `status` - Trạng thái - **Dropdown: idle, running, failed, disabled**

**User Actions:**
- Create new Batch under Subsystem
- Edit Batch
- Delete Batch
- Start/Stop Batch
- View execution history

---

### 2.2 Source Code Management

#### **Project Source (Nguồn mã)**
**Input Fields:**
- 🔗 `project_id` - Thuộc Project nào - **Required, Dropdown**
- 📁 `root_path` - Đường dẫn thư mục gốc - **Required, Directory Picker**
- 🎚️ `vcs_type` - Loại version control - **Dropdown: git, svn, none**
- ✏️ `default_branch` - Branch mặc định (VD: "main") - Optional
- ✏️ `current_commit` - Commit hash hiện tại - Auto-filled
- ✏️ `language` - Ngôn ngữ chính (VD: "Java") - Auto-detected

**User Actions:**
- Add source root to Project
- Select directory via file browser
- Trigger manual scan
- Remove source root
- View scan status và statistics

**Auto-populated:**
- `scan_status` - pending → scanning → complete/failed
- `last_scanned_at` - Timestamp

---

### 2.3 Knowledge Management

#### **CHM Import (Nhập tài liệu CHM)**
**Input Fields:**
- 🔗 `project_id` - Thuộc Project nào - Optional, Dropdown
- 🔗 `subsystem_id` - Hoặc Subsystem - Optional, Dropdown
- 🔗 `system_id` - Hoặc System - Optional, Dropdown
- 📁 `chm_path` - File CHM cần import - **Required, File Picker (.chm)**
- ✏️ `notes` - Ghi chú về tài liệu - Optional, Text Area

**User Actions:**
- Upload CHM file
- Select scope (System/Subsystem/Project)
- Monitor import progress
- View imported documents
- Delete import

**Auto-populated:**
- `status` - pending → extracting → parsing → indexing → complete/failed
- `imported_at` - Timestamp
- `chm_checksum` - MD5/SHA256 hash
- `extracted_path` - Temp directory

---

#### **Knowledge Chunk (Tài liệu tùy chỉnh)**
**Input Fields:**
- 🔗 `project_id` - Thuộc Project nào - Optional, Dropdown
- 🎚️ `source_type` - Loại nguồn - **Dropdown: doc, kb_article, chm_doc, code, response**
- ✏️ `label` - Tiêu đề/nhãn - **Required**
- 📝 `content` - Nội dung - **Required, Rich Text Editor**
- 🏷️ `tags` - Tags (comma-separated) - Optional

**User Actions:**
- Create custom knowledge article
- Import Markdown/HTML files
- Edit content
- Delete article

---

### 2.4 User Requests & Interactions

#### **User Request (Yêu cầu/Câu hỏi)**
**Input Fields:**
- 🔗 `project_id` - Scope Project - Optional, Dropdown
- 🔗 `subsystem_id` - Scope Subsystem - Optional, Dropdown
- ✏️ `title` - Tiêu đề ngắn gọn - Optional
- 📝 `description` - Câu hỏi/yêu cầu chi tiết - **Required, Text Area**
- 🎚️ `request_type` - Loại yêu cầu - **Dropdown: question, feature, bug, analysis, review**
- 🎚️ `priority` - Độ ưu tiên - **Dropdown: low, normal, high, urgent**
- 📎 `attachments` - File đính kèm - Optional, File Upload

**User Actions:**
- Submit new request/question
- Type in chat interface
- Attach files (code snippets, screenshots)
- View request history
- Re-submit similar requests

**Auto-populated:**
- `user_id` - Current user
- `status` - received → processing → answered → resolved
- `created_at` - Timestamp

---

#### **Answer Feedback (Đánh giá phản hồi)**
**Input Fields:**
- ⭐ `rating` - Đánh giá 1-5 sao - **Required, Star Rating Widget**
- 💬 `comment` - Nhận xét chi tiết - Optional, Text Area

**User Actions:**
- Rate AI response (thumb up/down or stars)
- Provide feedback comments
- Submit feedback

---

### 2.5 Code Review

#### **Review Comment (Nhận xét review)**

**Auto-generated** bởi hệ thống, nhưng user có thể:
- ✅ Mark as resolved
- ❌ Dismiss comment
- 💬 Add reply/notes
- 🔗 Link to related task

**Display Fields:**
- `severity` - info, warning, error, critical
- `category` - naming, null_safety, security, i18n, performance, etc.
- `message` - Nội dung nhận xét
- `suggestion` - Đề xuất fix
- `file_path` + `start_line` + `end_line` - Vị trí code

---

### 2.6 Test Management

#### **Test Case (Manual Entry)**
**Input Fields:**
- ✏️ `name` - Tên test case - **Required**
- 📝 `description` - Mô tả test - **Required**
- 🎚️ `scope` - Phạm vi - **Dropdown: unit, integration, e2e, performance**
- 🔗 `related_nodes` - Link to AST nodes - Multi-select
- 📁 `test_file` - File test code - Optional
- 🏷️ `tags` - Tags (VD: security, i18n) - Optional

**User Actions:**
- Create manual test case
- Link to code symbols
- Update test status
- Delete test

---

## 3. Dữ liệu Auto-generated (Không nhập trực tiếp)

### 3.1 Source Analysis
- **Source Files** - Tự động scan từ file system
- **AST Nodes** - Tự động parse từ source code
- **AST Relationships** - Tự động extract (call graph, inheritance)
- **File Dependencies** - Tự động phát hiện imports/includes

### 3.2 Search & Indexing
- **Search Corpus** - Tự động từ source files + documents
- **Search Index (FTS5)** - Tự động build từ search_corpus
- **Vector Documents** - Tự động chunk + embed
- **Embeddings** - Tự động generate và cache

### 3.3 AI Responses
- **Agent Responses** - Tự động generate từ LLM
- **Request Artifacts** - Tự động log retrieved chunks
- **Citations** - Tự động extract từ response

### 3.4 Recommendations
- **Test Recommendations** - Tự động generate từ impact analysis
- **Review Comments** - Tự động từ heuristic rules + LLM

---

## 4. Quy trình nhập liệu chính

### 4.1 Setup Project (Lần đầu)
```
1. Create System
   ↓
2. Create Subsystem(s)
   ↓
3. Create Project(s)
   ↓
4. Attach Source Root
   ↓
5. Trigger Scan (auto: parse AST, build index)
```

### 4.2 Import Documentation
```
1. Upload CHM file
   → OR →
   Create Knowledge Article
   ↓
2. Select scope (System/Subsystem/Project)
   ↓
3. Monitor import progress
   ↓
4. Auto: Extract, parse, index, embed
```

### 4.3 Ask Question
```
1. Type question in chat
   ↓
2. Optional: Select project scope
   ↓
3. Optional: Attach files
   ↓
4. Submit
   ↓
5. Auto: Retrieve, generate answer, cite sources
   ↓
6. User: Rate response
```

### 4.4 Code Review
```
1. System detects code changes (git diff or AST diff)
   ↓
2. Auto: Apply heuristic rules
   ↓
3. Auto: Generate review comments
   ↓
4. User: Review comments, mark resolved
```

### 4.5 Test Planning
```
1. User submits change request
   ↓
2. Auto: Run impact analysis
   ↓
3. Auto: Generate test recommendations
   ↓
4. User: Review recommendations, create actual tests
```

---

## 5. Validation Rules

### 5.1 System Hierarchy
- ✅ `code` must be unique per scope (System code globally unique, Subsystem code unique per System)
- ✅ `name` is required
- ✅ Cannot delete if has children (unless cascade)
- ✅ Status must be valid enum value

### 5.2 Source Management
- ✅ `root_path` must exist and be readable
- ✅ Cannot add duplicate source root for same project
- ✅ VCS type must be git, svn, or none

### 5.3 User Requests
- ✅ `description` cannot be empty
- ✅ Either project_id or subsystem_id must be set (for scoping)
- ✅ Priority and request_type must be valid enum

### 5.4 Feedback
- ✅ Rating must be 1-5
- ✅ Must have associated response_id

---

## 6. Data Relationships

```
System (1) ──< (N) Subsystem
    │
    └──< (N) Project ──< (N) ProjectSource ──< (N) SourceFile
              │                                       │
              ├──< (N) UserRequest                   └──< (N) AstNode
              │         └──< (N) AgentResponse
              │                   └──< (N) Feedback
              │
              └──< (N) ChmImport ──< (N) ChmDocument
```

---

## 7. Storage & Persistence

### 7.1 Primary Database (SQLite)
- All user input data
- System hierarchy
- Metadata (files, AST, requests)
- ~18 core tables

### 7.2 Vector Store (Qdrant)
- Embeddings for semantic search
- Payload với metadata
- Không nhập trực tiếp (auto-sync từ SQLite)

### 7.3 File System
- Source code files (read-only)
- Extracted CHM files (temp)
- Uploaded attachments
- Log files

---

## 8. Import/Export Capabilities

### 8.1 Import
- ✅ CHM files → Documentation
- ✅ Source code directories → AST + Index
- ✅ Markdown/HTML files → Knowledge Base
- ✅ Test files → Test Catalog
- 🔮 CSV/Excel → Bulk entity import (future)
- 🔮 Git repository URLs → Auto-clone and scan (future)

### 8.2 Export
- ✅ Request history → Markdown/PDF
- ✅ Test recommendations → CSV
- ✅ Review comments → HTML report
- ✅ Analytics → Charts (PNG/SVG)
- 🔮 Full knowledge base → ZIP archive (future)
- 🔮 Conversation history → JSON (future)

---

## 9. User Permissions (Future)

Current: Single-user desktop app  
Future: Multi-user with permissions

**Planned roles:**
- **Admin**: Full access, manage all entities
- **Developer**: Create/edit projects, submit requests, view all
- **QA**: View projects, create test cases, submit bug reports
- **Viewer**: Read-only access

---

## 10. Summary: User Input Points

| Category | Entities | Input Method | Frequency |
|----------|----------|--------------|-----------|
| **Hierarchy** | System, Subsystem, Project, Batch | Forms | Setup (rare) |
| **Source** | ProjectSource | Directory picker | Setup + updates |
| **Knowledge** | ChmImport, KnowledgeChunk | File upload, Editor | Occasional |
| **Requests** | UserRequest | Chat interface | Daily (frequent) |
| **Feedback** | AnswerFeedback | Rating widget | After each response |
| **Tests** | TestCase (manual) | Form | Occasional |

**Total user input entities:** ~10 main types  
**Auto-generated entities:** ~20 types  

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-15  
**Next:** See `02-screen-structure.md` for UI details

