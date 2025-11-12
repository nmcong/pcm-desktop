# DatabaseManager API Reference

## 📌 Quick Reference - Method Names

⚠️ **IMPORTANT**: DatabaseManager has **inconsistent naming convention**. Always refer to this guide!

---

## ✅ Correct Method Names

### Projects

```javascript
// ❌ WRONG
databaseManager.getAllProjects();

// ✅ CORRECT
databaseManager.getProjects();
databaseManager.getProject(id);
databaseManager.getProjectById(id);
databaseManager.getProjectsBySubsystem(subsystemId);
databaseManager.getFavoriteProjects();
databaseManager.searchProjects(query);
```

---

### Screens

```javascript
// ❌ WRONG
databaseManager.getAllScreens();

// ✅ CORRECT
databaseManager.getScreens();
databaseManager.getScreen(id);
databaseManager.getScreensByProject(projectId);
databaseManager.getFavoriteScreens(projectId);
```

---

### Subsystems

```javascript
// ❌ WRONG
databaseManager.getAllSubsystems();

// ✅ CORRECT
databaseManager.getSubsystems();
databaseManager.getSubsystem(id);
databaseManager.getSubsystemById(id);
databaseManager.getFavoriteSubsystems();
```

---

### Knowledge Base

```javascript
// ❌ WRONG
databaseManager.getAllKnowledgeItems();
databaseManager.getAllKnowledgeCategories();

// ✅ CORRECT
databaseManager.getAllKBItems(); // ← Note: "KB" not "Knowledge"
databaseManager.getAllKBCategories(); // ← Note: "KB" not "Knowledge"
databaseManager.getKBItemById(id);
databaseManager.getKBItemsByCategory(categoryId);
databaseManager.getKBCategoryById(id);
databaseManager.getKBStats();
```

---

### Database Objects

```javascript
// ✅ CORRECT (has "All" prefix)
databaseManager.getAllDBObjects();
databaseManager.getDBObjectById(id); // ← Note: "ById" not just "getDBObject"
databaseManager.getDBObjectsByProject(projectId);
databaseManager.getDBObjectsByType(type);

// ❌ WRONG - These don't exist
databaseManager.getDBObject(id); // Use getDBObjectById(id)
databaseManager.getDBObjectsByName(name); // Use getAllDBObjects() + filter
```

---

### Batch Jobs

```javascript
// ✅ CORRECT (has "All" prefix)
databaseManager.getAllBatchJobs();
databaseManager.getBatchJobById(id);
```

---

### Chat Conversations

```javascript
// ✅ CORRECT (has "All" prefix)
databaseManager.getAllChatConversations();
databaseManager.getChatConversation(id);
```

---

## 📊 Naming Pattern Summary

| Entity                 | Get All Method              | Get By ID Method          | Pattern     |
|------------------------|-----------------------------|---------------------------|-------------|
| **Projects**           | `getProjects()`             | `getProject(id)`          | ❌ No "All"  |
| **Screens**            | `getScreens()`              | `getScreen(id)`           | ❌ No "All"  |
| **Subsystems**         | `getSubsystems()`           | `getSubsystem(id)`        | ❌ No "All"  |
| **KB Items**           | `getAllKBItems()`           | `getKBItemById(id)`       | ✅ Has "All" |
| **KB Categories**      | `getAllKBCategories()`      | `getKBCategoryById(id)`   | ✅ Has "All" |
| **DB Objects**         | `getAllDBObjects()`         | `getDBObjectById(id)` ⚠️  | ✅ Has "All" |
| **Batch Jobs**         | `getAllBatchJobs()`         | `getBatchJobById(id)`     | ✅ Has "All" |
| **Chat Conversations** | `getAllChatConversations()` | `getChatConversation(id)` | ✅ Has "All" |

⚠️ **Note**: DB Objects use `getDBObjectById(id)` (with "ById"), NOT `getDBObject(id)`!

---

## 🔍 Search Methods

```javascript
// Search across entities
databaseManager.searchProjects(query);
databaseManager.searchScreens(query);

// Get by filters
databaseManager.getProjectsBySubsystem(subsystemId);
databaseManager.getScreensByProject(projectId);
databaseManager.getKBItemsByCategory(categoryId);
databaseManager.getDBObjectsByProject(projectId);
databaseManager.getDBObjectsByType(type);

// Get favorites
databaseManager.getFavoriteProjects();
databaseManager.getFavoriteScreens(projectId);
databaseManager.getFavoriteSubsystems();
```

---

## ✏️ CRUD Operations

### Create

```javascript
await databaseManager.createProject(projectData);
await databaseManager.createScreen(screenData);
await databaseManager.createSubsystem(subsystemData);
await databaseManager.createKBItem(itemData);
await databaseManager.createKBCategory(categoryData);
await databaseManager.createDBObject(objectData);
await databaseManager.createBatchJob(jobData);
```

### Update

```javascript
await databaseManager.updateProject(id, updates);
await databaseManager.updateScreen(id, updates);
await databaseManager.updateSubsystem(id, updates);
await databaseManager.updateKBItem(id, updates);
await databaseManager.updateKBCategory(id, updates);
await databaseManager.updateDBObject(id, updates);
await databaseManager.updateBatchJob(id, updates);
```

### Delete

```javascript
await databaseManager.deleteProject(id);
await databaseManager.deleteScreen(id);
await databaseManager.deleteSubsystem(id);
await databaseManager.deleteKBItem(id);
await databaseManager.deleteKBCategory(id);
await databaseManager.deleteDBObject(id);
await databaseManager.deleteBatchJob(id);
```

---

## 🔧 Initialization

```javascript
import databaseManager from "./services/DatabaseManager.js";

// Initialize before use
await databaseManager.init();

// Then use any method
const projects = await databaseManager.getProjects();
```

---

## 🐛 Common Errors

### Error 1: `getAllProjects is not a function`

```javascript
// ❌ WRONG
const projects = await databaseManager.getAllProjects();

// ✅ CORRECT
const projects = await databaseManager.getProjects();
```

### Error 2: `getAllKnowledgeItems is not a function`

```javascript
// ❌ WRONG
const items = await databaseManager.getAllKnowledgeItems();

// ✅ CORRECT
const items = await databaseManager.getAllKBItems();
```

### Error 3: `getDBObject is not a function`

```javascript
// ❌ WRONG
const dbObject = await databaseManager.getDBObject(id);

// ✅ CORRECT
const dbObject = await databaseManager.getDBObjectById(id);
```

### Error 4: `getDBObjectsByName is not a function`

```javascript
// ❌ WRONG
const objects = await databaseManager.getDBObjectsByName("users_table");

// ✅ CORRECT - Get all and filter
const allObjects = await databaseManager.getAllDBObjects();
const objects = allObjects.filter(obj => obj.name === "users_table");

// ✅ BETTER - Multiple names
const allObjects = await databaseManager.getAllDBObjects();
const targetNames = ["users_table", "auth_tokens"];
const objects = allObjects.filter(obj => targetNames.includes(obj.name));
```

### Error 5: Database not initialized

```javascript
// ❌ WRONG
const projects = await databaseManager.getProjects(); // Error: Database not initialized

// ✅ CORRECT
await databaseManager.init();
const projects = await databaseManager.getProjects();
```

---

## 📝 Best Practices

### 1. Always Initialize First

```javascript
// In app initialization
async function initApp() {
  await databaseManager.init();
  // ... rest of app initialization
}
```

### 2. Use Correct Method Names

```javascript
// ✅ GOOD - Always refer to this document
const projects = await databaseManager.getProjects();

// ❌ BAD - Guessing method names
const projects = await databaseManager.getAllProjects();
```

### 3. Handle Errors

```javascript
try {
  const projects = await databaseManager.getProjects();
} catch (error) {
  console.error("Error loading projects:", error);
  // Handle error appropriately
}
```

### 4. Cache Frequently Used Data

```javascript
// Cache results to avoid repeated queries
let cachedProjects = null;
let cacheTime = null;

async function getProjects() {
  const now = Date.now();
  if (cachedProjects && now - cacheTime < 5 * 60 * 1000) {
    // 5 min cache
    return cachedProjects;
  }

  cachedProjects = await databaseManager.getProjects();
  cacheTime = now;
  return cachedProjects;
}
```

---

## 🎯 Quick Copy-Paste

```javascript
// Import
import databaseManager from "./services/DatabaseManager.js";

// Initialize
await databaseManager.init();

// Get all
const projects = await databaseManager.getProjects(); // No "All"
const screens = await databaseManager.getScreens(); // No "All"
const subsystems = await databaseManager.getSubsystems(); // No "All"
const kbItems = await databaseManager.getAllKBItems(); // Has "All"
const kbCategories = await databaseManager.getAllKBCategories(); // Has "All"
const dbObjects = await databaseManager.getAllDBObjects(); // Has "All"
const batchJobs = await databaseManager.getAllBatchJobs(); // Has "All"

// Get by ID
const project = await databaseManager.getProject(id);
const screen = await databaseManager.getScreen(id);
const subsystem = await databaseManager.getSubsystem(id);
const kbItem = await databaseManager.getKBItemById(id);
const dbObject = await databaseManager.getDBObjectById(id);
const batchJob = await databaseManager.getBatchJobById(id);

// Search
const foundProjects = await databaseManager.searchProjects(query);
const foundScreens = await databaseManager.searchScreens(query);

// Create
const newProject = await databaseManager.createProject(data);
const newScreen = await databaseManager.createScreen(data);
const newSubsystem = await databaseManager.createSubsystem(data);

// Update
await databaseManager.updateProject(id, updates);
await databaseManager.updateScreen(id, updates);
await databaseManager.updateSubsystem(id, updates);

// Delete
await databaseManager.deleteProject(id);
await databaseManager.deleteScreen(id);
await databaseManager.deleteSubsystem(id);
```

---

## ⚠️ Remember

1. **Projects, Screens, Subsystems** → `getXxx()` (NO "All")
2. **KB, DB Objects, Batch Jobs** → `getAllXxx()` (HAS "All")
3. **Knowledge Base** → Use "KB" not "Knowledge"
4. **Always initialize** with `await databaseManager.init()`
5. **Refer to this document** when in doubt!

---

**Version**: 1.0.0  
**Last Updated**: November 10, 2025  
**Location**: `public/js/services/DatabaseManager.js`
