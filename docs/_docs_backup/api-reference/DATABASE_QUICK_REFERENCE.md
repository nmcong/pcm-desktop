# 🚀 DatabaseManager Quick Reference Card

**Print this and keep it handy!**

---

## 📌 Golden Rules

1. **Projects, Screens, Subsystems** → `getXxx()` (NO "All")
2. **KB, DB Objects, Batch Jobs** → `getAllXxx()` (HAS "All")
3. **KB = Knowledge Base** (NOT "Knowledge")
4. **DB Objects** → `getDBObjectById(id)` (NOT `getDBObject`)
5. **Always `await databaseManager.init()`** before first use

---

## ⚡ Quick Copy-Paste

### Projects

```javascript
await databaseManager.getProjects();
await databaseManager.getProject(id);
await databaseManager.getProjectsBySubsystem(subsystemId);
await databaseManager.createProject(data);
await databaseManager.updateProject(id, updates);
await databaseManager.deleteProject(id);
```

### Screens

```javascript
await databaseManager.getScreens();
await databaseManager.getScreen(id);
await databaseManager.getScreensByProject(projectId);
await databaseManager.createScreen(data);
await databaseManager.updateScreen(id, updates);
await databaseManager.deleteScreen(id);
```

### Subsystems

```javascript
await databaseManager.getSubsystems();
await databaseManager.getSubsystem(id);
await databaseManager.createSubsystem(data);
await databaseManager.updateSubsystem(id, updates);
await databaseManager.deleteSubsystem(id);
```

### Knowledge Base

```javascript
await databaseManager.getAllKBItems(); // ← "KB" not "Knowledge"
await databaseManager.getAllKBCategories(); // ← "KB" not "Knowledge"
await databaseManager.getKBItemById(id);
await databaseManager.getKBCategoryById(id);
await databaseManager.getKBItemsByCategory(categoryId);
await databaseManager.searchKBItems(query);
await databaseManager.createKBItem(data);
await databaseManager.updateKBItem(id, updates);
```

### Database Objects

```javascript
await databaseManager.getAllDBObjects();
await databaseManager.getDBObjectById(id); // ← "ById" required!
await databaseManager.getDBObjectsByProject(projectId);
await databaseManager.getDBObjectsByType(type);
await databaseManager.searchDBObjects(query);
await databaseManager.createDBObject(data);
await databaseManager.updateDBObject(id, updates);
await databaseManager.deleteDBObject(id);
```

### Batch Jobs

```javascript
await databaseManager.getAllBatchJobs();
await databaseManager.getBatchJobById(id);
await databaseManager.createBatchJob(data);
await databaseManager.updateBatchJob(id, updates);
await databaseManager.deleteBatchJob(id);
```

---

## ❌ Common Mistakes

| ❌ DON'T                    | ✅ DO                            |
|----------------------------|---------------------------------|
| `getAllProjects()`         | `getProjects()`                 |
| `getAllScreens()`          | `getScreens()`                  |
| `getAllSubsystems()`       | `getSubsystems()`               |
| `getAllKnowledgeItems()`   | `getAllKBItems()`               |
| `getDBObject(id)`          | `getDBObjectById(id)`           |
| `getDBObjectsByName(name)` | `getAllDBObjects().filter(...)` |

---

## 🎯 Decision Tree

```
Need to get data from DatabaseManager?
  │
  ├─ Projects/Screens/Subsystems?
  │   └─ Use getXxx() (NO "All" prefix)
  │
  ├─ KB Items/Categories?
  │   └─ Use getAllKB... (HAS "All", use "KB" not "Knowledge")
  │
  ├─ DB Objects?
  │   ├─ Get all? → getAllDBObjects()
  │   └─ Get by ID? → getDBObjectById(id)  ← Must have "ById"!
  │
  └─ Batch Jobs?
      └─ Use getAllBatchJobs() / getBatchJobById(id)
```

---

## 🔍 Quick Test

Run audit script:

```bash
cd apps/pcm-webapp
bash scripts/audit-database-methods.sh
```

---

**Last Updated**: November 10, 2025  
**Version**: 1.0.0  
**Full Docs**: [DATABASE_MANAGER_API.md](./DATABASE_MANAGER_API.md)
