# PCM WebApp Database - Quick Reference

## 🎯 Overview

PCM WebApp sử dụng **IndexedDB** (browser native database) để lưu trữ dữ liệu client-side.

**Database:** `PCM_WebApp_Database` (Version 9)

---

## 📋 Stores (Tables)

| Store Name                | Purpose           | Key Fields                                   |
| ------------------------- | ----------------- | -------------------------------------------- |
| `settings`                | App configuration | `key`, `value`                               |
| `subsystems`              | Hệ thống con      | `id`, `name`, `isFavorite`                   |
| `projects`                | Dự án             | `id`, `subsystemId`, `name`, `repositoryUrl` |
| `screens`                 | Màn hình/Pages    | `id`, `projectId`, `name`, `events`          |
| `chatConversations`       | AI Chat history   | `id`, `messages`, `context`                  |
| `knowledgeBaseCategories` | KB Categories     | `id`, `name`, `icon`, `color`                |
| `knowledgeBaseItems`      | KB Articles       | `id`, `categoryId`, `title`, `solution`      |
| `batchJobs`               | Scheduled tasks   | `id`, `name`, `schedule`, `status`           |
| `databaseObjects`         | Oracle DB objects | `id`, `name`, `type`, `sqlScript`            |

---

## 🔗 Relationships

```
Subsystems (1) ──→ (N) Projects ──→ (N) Screens
                         │
                         └──→ (N) DB Objects

KB Categories (1) ──→ (N) KB Items
```

---

## 🚀 Quick Usage

```javascript
import databaseManager from "./services/DatabaseManager.js";

// Initialize
await databaseManager.init();

// CRUD Operations
const project = await databaseManager.getProject(projectId);
await databaseManager.createProject(data);
await databaseManager.updateProject(id, updates);
await databaseManager.deleteProject(id);

// Query by Index
const projects = await databaseManager.getProjectsBySubsystem(subsystemId);
const screens = await databaseManager.getScreensByProject(projectId);
```

---

## 📦 Data Import/Export

**Export:** Settings → Data Backup → Export All Data  
**Import:** Settings → Data Backup → Import Data → Choose JSON file

**Format:** JSON with all stores data + metadata

---

## 📖 Full Documentation

Xem chi tiết tại: [DATA-STRUCTURES.md](./DATA-STRUCTURES.md)

- Complete schema cho tất cả stores
- Indexes và query optimization
- Entity relationship diagram
- Migration history
- Best practices

---

**Version:** 9 | **Last Updated:** 2025-11-08
