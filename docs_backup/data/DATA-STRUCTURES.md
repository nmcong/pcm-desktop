# PCM WebApp - Data Structures Documentation

## 📚 Overview

PCM WebApp sử dụng **IndexedDB** làm database client-side để lưu trữ toàn bộ dữ liệu ứng dụng. Database được thiết kế theo mô hình quan hệ với các object stores (tương đương tables) và indexes để tối ưu query performance.

**Database Information:**

- **Name:** `PCM_WebApp_Database`
- **Current Version:** 9
- **Technology:** IndexedDB (Browser Native)
- **Auto-increment:** Được sử dụng cho tất cả primary keys

---

## 🗄️ Database Schema

### 1. **Settings Store** (`settings`)

Lưu trữ các cấu hình ứng dụng dưới dạng key-value pairs.

**Schema:**

```javascript
{
  key: string,      // PRIMARY KEY - unique setting identifier
  value: any        // Setting value (can be any JSON-serializable type)
}
```

**Indexes:** None

**Usage:**

- Theme preferences
- User preferences
- Application configuration
- GitHub PAT token

---

### 2. **Subsystems Store** (`subsystems`)

Quản lý các hệ thống con (subsystems) trong tổ chức.

**Schema:**

```javascript
{
  id: number,           // PRIMARY KEY (auto-increment)
  name: string,         // Tên subsystem (VD: "E-Commerce", "Payment Gateway")
  description: string,  // Mô tả chi tiết
  isFavorite: boolean,  // Đánh dấu yêu thích
  createdAt: Date,      // Timestamp tạo
  updatedAt: Date       // Timestamp cập nhật
}
```

**Indexes:**

- `name` (non-unique)
- `isFavorite` (non-unique)

**Relationships:**

- **1 Subsystem → N Projects** (One-to-Many)

---

### 3. **Projects Store** (`projects`)

Quản lý các dự án thuộc từng subsystem.

**Schema:**

```javascript
{
  id: number,              // PRIMARY KEY (auto-increment)
  subsystemId: number,     // FOREIGN KEY → subsystems.id
  name: string,            // Tên dự án
  shortName: string,       // Tên viết tắt (VD: "PCM", "PAY")
  description: string,     // Mô tả chi tiết (Markdown supported)

  // GitHub Integration
  repositoryUrl: string,   // GitHub repository URL
  repositoryBranch: string, // Branch name (default: "main")

  // Environment Links
  devAccessLink: string,   // Development environment URL
  devDevOpsLink: string,   // Dev DevOps/CI-CD link
  qaAccessLink: string,    // QA environment URL
  qaDevOpsLink: string,    // QA DevOps/CI-CD link
  prodAccessLink: string,  // Production environment URL
  prodDevOpsLink: string,  // Prod DevOps/CI-CD link

  isFavorite: boolean,     // Đánh dấu yêu thích
  createdAt: Date,         // Timestamp tạo
  updatedAt: Date          // Timestamp cập nhật
}
```

**Indexes:**

- `subsystemId` (non-unique) - For filtering projects by subsystem
- `name` (non-unique) - For searching by name
- `isFavorite` (non-unique) - For filtering favorites

**Relationships:**

- **N Projects → 1 Subsystem** (Many-to-One)
- **1 Project → N Screens** (One-to-Many)
- **1 Project → N Database Objects** (One-to-Many)

---

### 4. **Screens Store** (`screens`)

Quản lý các màn hình (screens/pages) trong từng project.

**Schema:**

```javascript
{
  id: number,              // PRIMARY KEY (auto-increment)
  projectId: number,       // FOREIGN KEY → projects.id
  name: string,            // Tên màn hình
  description: string,     // Mô tả chi tiết (Markdown supported)

  // Screen Details
  wireframe: string,       // Wireframe image URL/base64
  inputs: Array<{          // Input fields
    name: string,
    type: string,          // text, number, select, etc.
    required: boolean,
    validation: string,
    defaultValue: any
  }>,
  outputs: Array<{         // Output fields/displays
    name: string,
    type: string,
    format: string
  }>,
  events: Array<{          // User interactions & workflows
    type: string,          // click, submit, change, etc.
    trigger: string,       // Element/action that triggers event
    action: string,        // navigate, api_call, validate, etc.
    targetScreen: string,  // For navigation events (screen name)
    apiEndpoint: string,   // For API call events
    validation: string,    // For validation events
    branchOptions: Array<{ // For conditional branching
      condition: string,
      targetScreen: string,
      description: string
    }>
  }>,
  sourceFiles: Array<{     // Linked source code files
    id: number,
    type: string,          // frontend, backend, script, config, test, docs, other
    source: string,        // "github" or "manual"
    path: string,          // File path
    description: string,
    githubRepo: string,    // GitHub repo URL (if source = github)
    githubBranch: string   // GitHub branch (if source = github)
  }>,

  isFavorite: boolean,     // Đánh dấu yêu thích
  createdAt: Date,         // Timestamp tạo
  updatedAt: Date          // Timestamp cập nhật
}
```

**Indexes:**

- `projectId` (non-unique) - For filtering screens by project
- `name` (non-unique) - For searching by name
- `isFavorite` (non-unique) - For filtering favorites

**Relationships:**

- **N Screens → 1 Project** (Many-to-One)

---

### 5. **Chat Conversations Store** (`chatConversations`)

Lưu trữ các cuộc hội thoại với AI Assistant.

**Schema:**

```javascript
{
  id: number,              // PRIMARY KEY (auto-increment)
  title: string,           // Tiêu đề conversation
  messages: Array<{        // Danh sách messages
    role: string,          // "user" or "assistant"
    content: string,       // Message content
    timestamp: Date        // Message timestamp
  }>,
  context: {               // Conversation context
    projectId: number,     // Related project (optional)
    screenId: number,      // Related screen (optional)
    subsystemId: number    // Related subsystem (optional)
  },
  createdAt: Date,         // Timestamp tạo
  updatedAt: Date          // Timestamp cập nhật
}
```

**Indexes:**

- `createdAt` (non-unique) - For sorting by date
- `updatedAt` (non-unique) - For recent conversations

**Relationships:**

- Soft references to Projects/Screens (no foreign key constraints)

---

### 6. **Knowledge Base Categories Store** (`knowledgeBaseCategories`)

Quản lý các danh mục trong Knowledge Base (giống Stack Overflow tags).

**Schema:**

```javascript
{
  id: number,           // PRIMARY KEY (auto-increment)
  name: string,         // Tên category (VD: "Java", "Database", "React")
  description: string,  // Mô tả category
  icon: string,         // Lucide icon name
  color: string,        // Màu sắc (hex code)
  createdAt: Date,      // Timestamp tạo
  updatedAt: Date       // Timestamp cập nhật
}
```

**Indexes:**

- `name` (non-unique) - For searching categories
- `createdAt` (non-unique) - For sorting

**Relationships:**

- **1 Category → N KB Items** (One-to-Many)

---

### 7. **Knowledge Base Items Store** (`knowledgeBaseItems`)

Lưu trữ các bài viết/giải pháp trong Knowledge Base.

**Schema:**

```javascript
{
  id: number,              // PRIMARY KEY (auto-increment)
  categoryId: number,      // FOREIGN KEY → knowledgeBaseCategories.id

  // Content
  title: string,           // Tiêu đề bài viết
  type: string,            // "bug", "feature", "question", "documentation"
  priority: string,        // "low", "medium", "high", "critical"
  problem: string,         // Mô tả vấn đề gặp phải
  solution: string,        // Giải pháp/hướng dẫn
  tags: Array<string>,     // Tags để search (VD: ["api", "authentication"])

  // Metadata
  author: string,          // Tên tác giả
  status: string,          // "draft", "published", "archived"
  views: number,           // Số lượt xem
  upvotes: number,         // Số lượt upvote

  createdAt: Date,         // Timestamp tạo
  updatedAt: Date          // Timestamp cập nhật
}
```

**Indexes:**

- `categoryId` (non-unique) - For filtering by category
- `type` (non-unique) - For filtering by type
- `status` (non-unique) - For filtering published items
- `createdAt` (non-unique) - For sorting by date
- `views` (non-unique) - For sorting by popularity

**Relationships:**

- **N KB Items → 1 Category** (Many-to-One)

---

### 8. **Batch Jobs Store** (`batchJobs`)

Quản lý các batch job tự động (scheduled tasks).

**Schema:**

```javascript
{
  id: number,              // PRIMARY KEY (auto-increment)
  name: string,            // Tên batch job
  description: string,     // Mô tả chi tiết

  // Configuration
  type: string,            // "backup", "cleanup", "report", "sync", "custom"
  schedule: string,        // Cron expression (VD: "0 2 * * *")
  command: string,         // Command/script to execute
  parameters: Object,      // JSON parameters for the job

  // Status & Execution
  status: string,          // "active", "paused", "error", "disabled"
  isActive: boolean,       // Enable/disable job
  lastRun: Date,           // Last execution timestamp
  lastResult: string,      // Last execution result/log
  nextRun: Date,           // Next scheduled run
  runCount: number,        // Total number of executions

  createdAt: Date,         // Timestamp tạo
  updatedAt: Date          // Timestamp cập nhật
}
```

**Indexes:**

- `name` (non-unique) - For searching jobs
- `status` (non-unique) - For filtering by status
- `type` (non-unique) - For filtering by type
- `schedule` (non-unique) - For grouping by schedule
- `lastRun` (non-unique) - For recent activity
- `nextRun` (non-unique) - For upcoming jobs
- `isActive` (non-unique) - For filtering active jobs
- `createdAt` (non-unique) - For sorting

---

### 9. **Database Objects Store** (`databaseObjects`)

Lưu trữ các Oracle database objects (tables, functions, procedures, etc.).

**Schema:**

```javascript
{
  id: number,              // PRIMARY KEY (auto-increment)
  name: string,            // Object name
  type: string,            // "TABLE", "VIEW", "FUNCTION", "PROCEDURE", "PACKAGE", "TRIGGER"
  schema: string,          // Oracle schema name (VD: "APP_SCHEMA")
  projectId: number,       // FOREIGN KEY → projects.id (optional)

  // SQL Script
  sqlScript: string,       // CREATE statement SQL
  description: string,     // Mô tả object

  // Metadata
  dependencies: Array<{    // Dependencies on other objects
    type: string,
    schema: string,
    name: string
  }>,
  columns: Array<{         // For tables only
    name: string,
    dataType: string,
    nullable: boolean,
    defaultValue: string,
    comment: string
  }>,
  tags: Array<string>,     // Tags để search

  // Version Control
  version: number,         // Version number
  changeLog: Array<{       // History of changes
    version: number,
    date: Date,
    author: string,
    description: string
  }>,

  // Validation
  isValid: boolean,        // SQL syntax validation status
  validationErrors: Array<string>, // Validation error messages

  createdAt: Date,         // Timestamp tạo
  updatedAt: Date          // Timestamp cập nhật
}
```

**Indexes:**

- `name` (non-unique) - For searching objects
- `type` (non-unique) - For filtering by type
- `schema` (non-unique) - For filtering by schema
- `projectId` (non-unique) - For project-related objects
- `createdAt` (non-unique) - For sorting
- `version` (non-unique) - For version tracking

**Relationships:**

- **N DB Objects → 1 Project** (Many-to-One, optional)

---

## 🔗 Entity Relationship Diagram

```
┌─────────────┐
│  Subsystems │
└──────┬──────┘
       │ 1:N
       ▼
┌─────────────┐      1:N    ┌─────────────┐
│  Projects   ├─────────────►│   Screens   │
└──────┬──────┘              └─────────────┘
       │ 1:N
       ▼
┌─────────────┐
│  DB Objects │
└─────────────┘

┌─────────────┐      1:N    ┌─────────────┐
│KB Categories├─────────────►│  KB Items   │
└─────────────┘              └─────────────┘

┌─────────────┐              ┌─────────────┐
│ Batch Jobs  │              │    Chat     │
│ (Independent)│              │Conversations│
└─────────────┘              └─────────────┘

┌─────────────┐
│  Settings   │
│(Key-Value)  │
└─────────────┘
```

---

## 📊 Data Migration & Versioning

**Version History:**

- **v1:** Initial schema with Subsystems, Projects, Screens
- **v2:** Added Chat Conversations
- **v3-5:** Index improvements and schema refinements
- **v6:** Added GitHub integration fields to Projects
- **v7:** Added Knowledge Base (Categories + Items)
- **v8:** Added source files support to Screens
- **v9:** Added Batch Jobs and Database Objects

**Migration Strategy:**

- Database auto-detects missing stores on initialization
- If stores are missing, database is automatically recreated
- All data is preserved during version upgrades
- Indexes are created/updated automatically

---

## 🔍 Query Patterns

### Common Queries

**1. Get all projects in a subsystem:**

```javascript
await databaseManager.getAllByIndex("projects", "subsystemId", subsystemId);
```

**2. Get all screens in a project:**

```javascript
await databaseManager.getAllByIndex("screens", "projectId", projectId);
```

**3. Search KB items by category:**

```javascript
await databaseManager.getAllByIndex(
  "knowledgeBaseItems",
  "categoryId",
  categoryId,
);
```

**4. Get favorite projects:**

```javascript
await databaseManager.getAllByIndex("projects", "isFavorite", true);
```

**5. Get active batch jobs:**

```javascript
await databaseManager.getAllByIndex("batchJobs", "isActive", true);
```

---

## 💾 Data Export/Import

**Export Format:** JSON

```json
{
  "version": "1.0",
  "exportDate": "2025-11-08T10:30:00.000Z",
  "subsystems": [...],
  "projects": [...],
  "screens": [...],
  "chatConversations": [...],
  "knowledgeBaseCategories": [...],
  "knowledgeBaseItems": [...],
  "batchJobs": [...],
  "databaseObjects": [...]
}
```

**Features:**

- Full database backup/restore
- Conflict resolution on import
- Selective data import (choose which entities to import)
- Duplicate detection
- Change comparison (added, removed, modified)

---

## 🔒 Data Integrity

**Rules:**

1. **Foreign Keys:** Enforced at application level (IndexedDB doesn't support native FKs)
2. **Cascading Deletes:**
   - Deleting a Subsystem deletes all related Projects
   - Deleting a Project deletes all related Screens
   - Deleting a Category prevents deletion if KB Items exist
3. **Auto-increment IDs:** Managed by IndexedDB
4. **Timestamps:** Automatically managed by application
5. **Validation:** Client-side validation before database operations

---

## 📝 Best Practices

**1. Always await `init()` before operations:**

```javascript
await databaseManager.init();
```

**2. Use transactions for multiple operations:**

```javascript
// Wrapped in service methods
await databaseManager.updateProject(id, updates);
```

**3. Index optimization:**

- Use indexes for frequently queried fields
- Avoid over-indexing (impacts write performance)

**4. Data consistency:**

- Always update `updatedAt` on modifications
- Validate relationships before deletion
- Handle migration gracefully

---

## 🔧 Maintenance

**Clear All Data:**

```javascript
await DatabaseManager.deleteDatabase();
```

**Rebuild Database:**

```javascript
await DatabaseManager.deleteDatabase();
await databaseManager.init(); // Recreates with current schema
```

**Backup Recommendation:**

- Export data regularly (Settings → Data Backup)
- Store exports in safe location
- Version control for schema changes

---

## 📚 Related Documentation

- [API Standards](/docs/vibytes-framework/API-STANDARD.md)
- [Development Guide](/docs/DEVELOPMENT-GUIDE.md)
- [GitHub Integration Guide](/apps/pcm-webapp/public/js/services/ai/functions/README.md)

---

**Last Updated:** 2025-11-08  
**Database Version:** 9  
**Document Version:** 1.0
