# 📚 API Reference Documentation

Complete reference for PCM WebApp APIs and services.

---

## 📖 Available References

### 🗄️ Database Manager

| Document                                                         | Purpose                | When to Use                      |
|------------------------------------------------------------------|------------------------|----------------------------------|
| **[DATABASE_MANAGER_API.md](./DATABASE_MANAGER_API.md)**         | Complete API reference | When coding with DatabaseManager |
| **[DATABASE_QUICK_REFERENCE.md](./DATABASE_QUICK_REFERENCE.md)** | Quick cheat sheet      | Keep open while coding           |
| **[DATABASE_METHOD_AUDIT.md](./DATABASE_METHOD_AUDIT.md)**       | Audit report           | Review compliance status         |

---

## 🚀 Quick Start

### For Developers

**Step 1**: Read the Quick Reference

```bash
cat docs/api-reference/DATABASE_QUICK_REFERENCE.md
```

**Step 2**: Keep it handy

- Print it out
- Pin it in IDE
- Bookmark it

**Step 3**: When in doubt, check full API

```bash
cat docs/api-reference/DATABASE_MANAGER_API.md
```

---

## 🔍 Run Audit

Check if your code complies with the API:

```bash
cd apps/pcm-webapp
bash scripts/audit-database-methods.sh
```

**Expected output**:

```
🎉 AUDIT PASSED - No issues found!
All function files comply with DATABASE_MANAGER_API.md
```

---

## 📋 Common Patterns

### ✅ Correct Usage

```javascript
import databaseManager from "./services/DatabaseManager.js";

// Initialize first
await databaseManager.init();

// Get all items
const projects = await databaseManager.getProjects(); // ✅ No "All"
const kbItems = await databaseManager.getAllKBItems(); // ✅ Has "All"
const dbObjects = await databaseManager.getAllDBObjects(); // ✅ Has "All"

// Get by ID
const project = await databaseManager.getProject(id); // ✅ No "ById"
const kbItem = await databaseManager.getKBItemById(id); // ✅ Has "ById"
const dbObject = await databaseManager.getDBObjectById(id); // ✅ Has "ById"
```

### ❌ Common Mistakes

```javascript
// ❌ WRONG
const projects = await databaseManager.getAllProjects(); // No such method
const kbItems = await databaseManager.getAllKnowledgeItems(); // Use "KB" not "Knowledge"
const dbObject = await databaseManager.getDBObject(id); // Missing "ById"
```

---

## 🎯 Golden Rules

1. **Projects, Screens, Subsystems** → `getXxx()` (NO "All")
2. **KB, DB Objects, Batch Jobs** → `getAllXxx()` (HAS "All")
3. **Knowledge Base** → Always use "KB" abbreviation
4. **DB Objects by ID** → Must use `getDBObjectById(id)`
5. **Always initialize** → `await databaseManager.init()` first

---

## 🐛 Troubleshooting

### Error: `getAllProjects is not a function`

**Solution**: Use `getProjects()` instead

```javascript
// ❌ const projects = await databaseManager.getAllProjects();
// ✅
const projects = await databaseManager.getProjects();
```

### Error: `getDBObject is not a function`

**Solution**: Use `getDBObjectById(id)` instead

```javascript
// ❌ const obj = await databaseManager.getDBObject(id);
// ✅
const obj = await databaseManager.getDBObjectById(id);
```

### Error: `getAllKnowledgeItems is not a function`

**Solution**: Use `getAllKBItems()` instead

```javascript
// ❌ const items = await databaseManager.getAllKnowledgeItems();
// ✅
const items = await databaseManager.getAllKBItems();
```

---

## 📊 Files in This Directory

```
api-reference/
├── README.md                          # This file
├── DATABASE_MANAGER_API.md            # Complete API reference (detailed)
├── DATABASE_QUICK_REFERENCE.md        # Quick cheat sheet (1 page)
└── DATABASE_METHOD_AUDIT.md           # Audit report (compliance status)
```

---

## 🔄 Maintenance

### When to Update

- ✅ Adding new DatabaseManager methods
- ✅ Changing method signatures
- ✅ Finding new bugs or issues
- ✅ After major refactoring

### How to Update

1. Edit `DATABASE_MANAGER_API.md` first
2. Update `DATABASE_QUICK_REFERENCE.md`
3. Run audit script: `bash scripts/audit-database-methods.sh`
4. Fix any issues found
5. Update `DATABASE_METHOD_AUDIT.md` with results

---

## 🎓 Learning Path

### Beginner (Day 1)

1. Read [DATABASE_QUICK_REFERENCE.md](./DATABASE_QUICK_REFERENCE.md)
2. Try basic CRUD operations
3. Run audit script

### Intermediate (Week 1)

1. Read [DATABASE_MANAGER_API.md](./DATABASE_MANAGER_API.md)
2. Understand all patterns
3. Write functions using DatabaseManager

### Advanced (Week 2+)

1. Review [DATABASE_METHOD_AUDIT.md](./DATABASE_METHOD_AUDIT.md)
2. Contribute to improving APIs
3. Help others with naming issues

---

## 📞 Support

**Found an issue?**

1. Check [DATABASE_MANAGER_API.md](./DATABASE_MANAGER_API.md)
2. Run `bash scripts/audit-database-methods.sh`
3. Review [Troubleshooting](#troubleshooting) section

**Still stuck?**

- Check console for exact error message
- Search for error in Database API docs
- Ask team members

---

## 🎉 Summary

- ✅ **3 reference documents** covering all levels
- ✅ **1 audit script** for automatic validation
- ✅ **0 known issues** after latest audit
- ✅ **100% compliance** with API standards

**Status**: 🟢 Production Ready

---

**Last Updated**: November 10, 2025  
**Version**: 1.0.0  
**Next Review**: When adding new methods
