# Database Migration Guide

## 📋 Overview

Guide này hướng dẫn cách quản lý database schema changes và migrations trong PCM WebApp.

**Current Database Version:** 9

---

## 🔄 Version History

### Version 9 (Current) - 2025-11-08

**Added:**

- `batchJobs` store with 8 indexes
- `databaseObjects` store with 6 indexes

**Purpose:** Support batch job scheduling and Oracle database object management

### Version 8 - 2025-10-20

**Added:**

- `sourceFiles` array field to `screens` store

**Purpose:** Link source code files to screens with GitHub integration

### Version 7 - 2025-09-15

**Added:**

- `knowledgeBaseCategories` store
- `knowledgeBaseItems` store with 5 indexes

**Purpose:** Stack Overflow-style knowledge base for team documentation

### Version 6 - 2025-08-01

**Added:**

- `repositoryUrl` field to `projects`
- `repositoryBranch` field to `projects`

**Purpose:** GitHub integration for projects

### Version 5 - 2025-06-10

**Added:**

- Indexes for `isFavorite` on all major stores

**Purpose:** Performance optimization for favorites filtering

### Version 4 - 2025-05-01

**Added:**

- `chatConversations` store

**Purpose:** AI assistant chat history

### Version 3 - 2025-04-01

**Added:**

- Additional indexes on `projects` and `subsystems`

**Purpose:** Query performance improvements

### Version 2 - 2025-03-01

**Added:**

- Index migrations for existing stores

**Purpose:** Backward compatibility improvements

### Version 1 - 2025-01-15

**Initial Release:**

- `settings` store
- `subsystems` store
- `projects` store
- `screens` store

**Purpose:** Core application functionality

---

## 🛠️ How to Create a Migration

### Step 1: Update DB_VERSION

Edit `DatabaseManager.js`:

```javascript
export class DatabaseManager {
  static DB_NAME = "PCM_WebApp_Database";
  static DB_VERSION = 10; // Increment version
  // ...
}
```

### Step 2: Add Store Name Constant

```javascript
static STORES = {
  // ... existing stores
  NEW_STORE: "newStore", // Add new store
};
```

### Step 3: Create Store in createStores()

```javascript
createStores(db, transaction) {
  // ... existing stores

  // New store
  if (!db.objectStoreNames.contains(DatabaseManager.STORES.NEW_STORE)) {
    const newStore = db.createObjectStore(
      DatabaseManager.STORES.NEW_STORE,
      {
        keyPath: "id",
        autoIncrement: true,
      }
    );

    // Add indexes
    newStore.createIndex("name", "name", { unique: false });
    newStore.createIndex("createdAt", "createdAt", { unique: false });
  }
}
```

### Step 4: Add CRUD Methods

```javascript
// Create
async createNewItem(data) {
  const item = {
    ...data,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
  return await this.put(DatabaseManager.STORES.NEW_STORE, item);
}

// Read
async getNewItem(id) {
  return await this.get(DatabaseManager.STORES.NEW_STORE, id);
}

// Update
async updateNewItem(id, updates) {
  const item = await this.getNewItem(id);
  if (!item) throw new Error("Item not found");

  const updated = {
    ...item,
    ...updates,
    updatedAt: new Date().toISOString(),
  };

  return await this.put(DatabaseManager.STORES.NEW_STORE, updated);
}

// Delete
async deleteNewItem(id) {
  return await this.delete(DatabaseManager.STORES.NEW_STORE, id);
}

// Query by index
async getNewItemsByName(name) {
  return await this.getAllByIndex(
    DatabaseManager.STORES.NEW_STORE,
    "name",
    name
  );
}
```

### Step 5: Update Export/Import

Edit `DataExportService.js`:

```javascript
async exportAllData() {
  // ... existing exports

  const newItems = await databaseManager.getAll(
    DatabaseManager.STORES.NEW_STORE
  );

  return {
    // ... existing data
    newItems,
  };
}

async importAllData(data) {
  // ... existing imports

  if (data.newItems?.length > 0) {
    for (const item of data.newItems) {
      await databaseManager.put(
        DatabaseManager.STORES.NEW_STORE,
        item
      );
    }
  }
}
```

### Step 6: Document the Change

Update this migration guide with:

- Version number
- Date
- Changes made
- Purpose/reason

---

## 🔧 Testing Migrations

### Manual Testing

**Test 1: Fresh Install**

```javascript
// 1. Clear database
await DatabaseManager.deleteDatabase();

// 2. Reload application
location.reload();

// 3. Verify all stores exist
const db = await databaseManager.init();
console.log("Object stores:", Array.from(db.objectStoreNames));

// Expected: All 9 stores present
```

**Test 2: Upgrade from Previous Version**

```javascript
// 1. Export current data
const backup = await dataExportService.exportAllData();

// 2. Downgrade database (manually set version to N-1)
// 3. Reload application
// 4. Verify upgrade completes successfully
// 5. Verify data integrity
```

**Test 3: Index Migration**

```javascript
// 1. Open database in DevTools (Application → IndexedDB)
// 2. Check each store for correct indexes
// 3. Verify index names and uniqueness settings
```

### Automated Testing

Create test file `DatabaseManager.test.js`:

```javascript
describe("DatabaseManager Migrations", () => {
  it("should create all stores on fresh install", async () => {
    await DatabaseManager.deleteDatabase();
    const manager = new DatabaseManager();
    await manager.init();

    const stores = Array.from(manager.db.objectStoreNames);
    expect(stores).toContain("subsystems");
    expect(stores).toContain("projects");
    expect(stores).toContain("screens");
    // ... check all stores
  });

  it("should have correct indexes", async () => {
    const manager = new DatabaseManager();
    await manager.init();

    const tx = manager.db.transaction("projects", "readonly");
    const store = tx.objectStore("projects");

    expect(store.indexNames.contains("subsystemId")).toBe(true);
    expect(store.indexNames.contains("name")).toBe(true);
    expect(store.indexNames.contains("isFavorite")).toBe(true);
  });
});
```

---

## ⚠️ Common Migration Issues

### Issue 1: Missing Store After Upgrade

**Symptom:**

```
NotFoundError: Failed to execute 'transaction' on 'IDBDatabase':
One of the specified object stores was not found.
```

**Solution:**

```javascript
// DatabaseManager automatically detects and fixes this
// by recreating the database with all stores.

// If issue persists, manually clear:
await DatabaseManager.deleteDatabase();
location.reload();
```

### Issue 2: Data Loss During Migration

**Prevention:**

```javascript
// Always export data before major version upgrades
const backup = await dataExportService.exportAllData();
localStorage.setItem('db_backup', JSON.stringify(backup));

// After upgrade, verify data integrity
// If issues, restore from backup
const backup = JSON.parse(localStorage.getItem('db_backup'));
await dataExportService.importAllData(backup);
```

### Issue 3: Index Creation Fails

**Symptom:**

```
ConstraintError: Unable to create index with duplicate values
```

**Solution:**

```javascript
// Check for duplicate data before creating unique index
const items = await databaseManager.getAll("storeName");
const hasDuplicates = new Set(items.map((i) => i.field)).size !== items.length;

if (hasDuplicates) {
  // Clean up duplicates first
  // Then create index
}
```

---

## 📊 Database Size Management

### Check Current Size

```javascript
// In Chrome DevTools Console
navigator.storage.estimate().then((estimate) => {
  console.log("Used:", (estimate.usage / 1024 / 1024).toFixed(2), "MB");
  console.log("Quota:", (estimate.quota / 1024 / 1024).toFixed(2), "MB");
  console.log(
    "Percentage:",
    ((estimate.usage / estimate.quota) * 100).toFixed(2),
    "%",
  );
});
```

### Size Limits

| Browser | Typical Limit     | Notes                     |
| ------- | ----------------- | ------------------------- |
| Chrome  | 60% of disk space | Shared with other origins |
| Firefox | 50% of disk space | Shared with other origins |
| Safari  | 1 GB              | Per origin                |
| Edge    | Similar to Chrome | Chromium-based            |

### Size Optimization Tips

1. **Compress large text fields:**

```javascript
// Before storing
const compressed = LZString.compress(largeText);
await databaseManager.put("store", { data: compressed });

// After retrieving
const decompressed = LZString.decompress(stored.data);
```

2. **Clean up old chat conversations:**

```javascript
async cleanOldChats(daysToKeep = 30) {
  const cutoffDate = new Date();
  cutoffDate.setDate(cutoffDate.getDate() - daysToKeep);

  const allChats = await this.getAll('chatConversations');
  const oldChats = allChats.filter(
    chat => new Date(chat.createdAt) < cutoffDate
  );

  for (const chat of oldChats) {
    await this.delete('chatConversations', chat.id);
  }
}
```

3. **Archive infrequently accessed data:**

```javascript
// Export to JSON file
const archived = await dataExportService.exportAllData();
const blob = new Blob([JSON.stringify(archived)], { type: "application/json" });
const url = URL.createObjectURL(blob);

// Download and then delete from database
const link = document.createElement("a");
link.href = url;
link.download = `archive-${Date.now()}.json`;
link.click();
```

---

## 🔒 Data Integrity Rules

### Foreign Key Enforcement

**Rule:** Application-level enforcement

```javascript
async deleteProject(id) {
  // 1. Check for dependent screens
  const screens = await this.getScreensByProject(id);

  if (screens.length > 0) {
    // Cascade delete
    for (const screen of screens) {
      await this.deleteScreen(screen.id);
    }
  }

  // 2. Check for dependent DB objects
  const dbObjects = await this.getAllByIndex(
    'databaseObjects',
    'projectId',
    id
  );

  for (const obj of dbObjects) {
    await this.delete('databaseObjects', obj.id);
  }

  // 3. Delete project
  await this.delete('projects', id);
}
```

### Unique Constraints

**Indexes with unique: true**

- None currently (by design - allow flexibility)

**Application-level uniqueness:**

```javascript
async createSubsystem(data) {
  // Check for duplicate name
  const existing = await this.getAllByIndex(
    'subsystems',
    'name',
    data.name
  );

  if (existing.length > 0) {
    throw new Error(`Subsystem "${data.name}" already exists`);
  }

  return await this.put('subsystems', data);
}
```

### Data Validation

```javascript
async createProject(data) {
  // Validate required fields
  if (!data.name?.trim()) {
    throw new Error('Project name is required');
  }

  if (!data.subsystemId) {
    throw new Error('Subsystem is required');
  }

  // Validate foreign key
  const subsystem = await this.get('subsystems', data.subsystemId);
  if (!subsystem) {
    throw new Error(`Subsystem ${data.subsystemId} not found`);
  }

  // Validate URL format
  if (data.repositoryUrl && !isValidUrl(data.repositoryUrl)) {
    throw new Error('Invalid repository URL');
  }

  return await this.put('projects', data);
}
```

---

## 📝 Migration Checklist

Before releasing a new database version:

- [ ] Increment `DB_VERSION`
- [ ] Add new stores to `STORES` constant
- [ ] Implement store creation in `createStores()`
- [ ] Add CRUD methods for new stores
- [ ] Update export/import logic
- [ ] Add indexes for query optimization
- [ ] Test fresh install (clear database)
- [ ] Test upgrade from previous version
- [ ] Export sample data before upgrade
- [ ] Verify data integrity after upgrade
- [ ] Update this migration guide
- [ ] Update `DATA-STRUCTURES.md`
- [ ] Update `DATABASE-EXAMPLES.json`
- [ ] Create git commit with clear description

---

## 🆘 Rollback Procedure

If a migration fails:

**Step 1: Identify the Issue**

```javascript
console.log("Current DB version:", manager.db.version);
console.log("Expected version:", DatabaseManager.DB_VERSION);
console.log("Stores:", Array.from(manager.db.objectStoreNames));
```

**Step 2: Export Current Data (if possible)**

```javascript
const backup = await dataExportService.exportAllData();
// Save backup externally
```

**Step 3: Revert Code Changes**

```bash
git revert <migration-commit-hash>
```

**Step 4: Clear Database**

```javascript
await DatabaseManager.deleteDatabase();
```

**Step 5: Restore Data**

```javascript
location.reload(); // Initialize with reverted version
// Then import backup
await dataExportService.importAllData(backup);
```

---

## 📞 Support

For migration issues or questions:

- Check Chrome DevTools → Application → IndexedDB
- Review error logs in console
- Consult `DATA-STRUCTURES.md` for schema details
- Contact team lead or database administrator

---

**Last Updated:** 2025-11-08  
**Current Version:** 9  
**Next Version:** TBD
