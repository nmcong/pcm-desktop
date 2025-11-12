# Database Migration Guide

## 🎯 Problem Solved

**Error:** `org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (no such table: conversations)`

**Solution:** Automatic database migrations on application startup!

---

## ✅ What Was Fixed

### 1. Created `DatabaseMigrationManager`

Located at: `src/main/java/com/noteflix/pcm/infrastructure/database/DatabaseMigrationManager.java`

**Features:**
- ✅ Tracks applied migrations in `schema_version` table
- ✅ Runs pending migrations automatically
- ✅ Transaction support (rollback on error)
- ✅ Idempotent (safe to run multiple times)
- ✅ Follows Flyway-like convention: `V<version>__<description>.sql`

### 2. Updated `PCMApplication`

Added `init()` method to run migrations BEFORE UI initialization:

```java
@Override
public void init() throws Exception {
    super.init();
    
    // Run database migrations BEFORE UI initialization
    log.info("🔄 Running database migrations...");
    runDatabaseMigrations();
}

private void runDatabaseMigrations() {
    DatabaseMigrationManager migrationManager = 
        new DatabaseMigrationManager(ConnectionManager.INSTANCE);
    
    migrationManager.migrate();
    
    log.info("✅ Database migrations completed");
}
```

### 3. Updated Build Scripts

Modified `scripts/compile-macos.command` to copy migration SQL files:

```bash
mkdir -p out/db/migration
cp src/main/resources/db/migration/*.sql out/db/migration/
```

---

## 📁 Migration Files

Located in: `src/main/resources/db/migration/`

### V1__initial_schema.sql
- Creates initial 13 core tables
- Indexes
- Triggers
- Sample data

### V2__chat_tables.sql
- Creates `conversations` table
- Creates `messages` table
- Foreign keys
- Indexes

---

## 🔄 How It Works

### On Application Startup:

```
1. PCMApplication.init() called
     ↓
2. DatabaseMigrationManager.migrate() executed
     ↓
3. Create schema_version table (if not exists)
     ↓
4. Check which migrations are already applied
     ↓
5. Run pending migrations in order (V1, V2, V3...)
     ↓
6. Each migration runs in a transaction
     ↓
7. If successful: Record in schema_version table
     ↓
8. If error: Rollback and throw exception
     ↓
9. Continue with UI initialization
```

### Example Log Output:

```
🚀 Starting PCM Desktop Application...
🔄 Running database migrations...
📝 Running migration: V1__initial_schema.sql
✅ Migration applied: V1 - initial schema
📝 Running migration: V2__chat_tables.sql
✅ Migration applied: V2 - chat tables
✅ Applied 2 migration(s) successfully
✅ Database migrations completed
Initializing application window...
✅ Application started successfully
```

---

## 📊 Schema Version Table

Tracks applied migrations:

```sql
CREATE TABLE schema_version (
    version TEXT PRIMARY KEY,
    description TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Example Data:**

| version | description | applied_at |
|---------|-------------|------------|
| V1 | initial schema | 2024-11-12 06:25:00 |
| V2 | chat tables | 2024-11-12 06:25:01 |

---

## 🧪 Testing

### Check Migration Status

Add this to `DatabaseMigrationManager`:

```java
public void printStatus() {
    List<String> applied = getAppliedMigrations(conn);
    System.out.println("📊 Applied migrations:");
    for (String version : applied) {
        System.out.println("   - " + version);
    }
}
```

### Verify Tables Created

```bash
sqlite3 pcm-desktop.db

sqlite> .tables
# Should show: conversations, messages, schema_version, etc.

sqlite> SELECT * FROM schema_version;
# Should show: V1, V2
```

---

## 🆕 Adding New Migrations

### Step 1: Create SQL File

Create: `src/main/resources/db/migration/V3__add_users_table.sql`

```sql
-- V3: Add users table

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
```

### Step 2: Update `getAvailableMigrations()`

In `DatabaseMigrationManager.java`:

```java
private List<String> getAvailableMigrations() {
    List<String> migrations = new ArrayList<>();
    migrations.add("V1__initial_schema.sql");
    migrations.add("V2__chat_tables.sql");
    migrations.add("V3__add_users_table.sql"); // ← Add new migration
    return migrations;
}
```

### Step 3: Recompile and Run

```bash
./scripts/compile-macos.command
./scripts/run-macos.command
```

**Output:**

```
🔄 Running database migrations...
📝 Running migration: V3__add_users_table.sql
✅ Migration applied: V3 - add users table
✅ Applied 1 migration(s) successfully
```

---

## 🚨 Error Handling

### Migration Failed

If a migration fails:

```
❌ Migration failed: V3 - SQL error at line 5
org.sqlite.SQLiteException: syntax error near "TALBE"
```

**What Happens:**
1. Transaction is rolled back
2. No changes applied to database
3. Migration NOT recorded in schema_version
4. Application startup fails with exception

**How to Fix:**
1. Fix the SQL syntax error in `V3__add_users_table.sql`
2. Recompile
3. Run again

### Database Locked

```
org.sqlite.SQLiteException: [SQLITE_BUSY] database is locked
```

**Solution:**
- Close any other connections to the database
- Kill any hanging processes
- Delete `pcm-desktop.db-journal` file

---

## 🔧 Troubleshooting

### Issue 1: Migration File Not Found

**Error:**
```
IllegalArgumentException: Migration file not found: /db/migration/V2__chat_tables.sql
```

**Solution:**
```bash
# Check file exists
ls src/main/resources/db/migration/V2__chat_tables.sql

# Recompile to copy to out/
./scripts/compile-macos.command

# Verify copied
ls out/db/migration/V2__chat_tables.sql
```

### Issue 2: Migration Already Applied

**Scenario:** You want to re-run a migration

**Solution:**

```bash
sqlite3 pcm-desktop.db

sqlite> DELETE FROM schema_version WHERE version = 'V2';
sqlite> DROP TABLE conversations;
sqlite> DROP TABLE messages;
sqlite> .quit

# Now restart app - migration will run again
```

### Issue 3: Wrong Migration Order

**Problem:** V3 created before V2

**Solution:** Migrations run in alphabetical order. Always name with incrementing numbers:
- ✅ V1, V2, V3, V4...
- ❌ VA, VB, VC...

---

## 📝 Best Practices

### 1. **Never Modify Existing Migrations**

❌ **Bad:**
```sql
-- V2__chat_tables.sql (modified after applied)
CREATE TABLE conversations (
    id INTEGER PRIMARY KEY,
    title TEXT,
    new_column TEXT -- Added later
);
```

✅ **Good:**
```sql
-- V3__add_conversation_columns.sql (new migration)
ALTER TABLE conversations ADD COLUMN new_column TEXT;
```

### 2. **Use Descriptive Names**

❌ **Bad:**
```
V3__update.sql
V4__changes.sql
```

✅ **Good:**
```
V3__add_users_table.sql
V4__add_conversation_tags.sql
```

### 3. **Test Migrations Locally**

Before committing:
1. Delete `pcm-desktop.db`
2. Run application
3. Verify all migrations apply successfully
4. Check data integrity

### 4. **Use Transactions**

Migrations automatically run in transactions, but you can be explicit:

```sql
BEGIN TRANSACTION;

CREATE TABLE users (...);
INSERT INTO users VALUES (...);

COMMIT;
```

### 5. **Add Rollback Comments**

```sql
-- V5__add_user_roles.sql

-- Forward migration
CREATE TABLE user_roles (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    role TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Rollback (for reference, not executed):
-- DROP TABLE user_roles;
```

---

## 🎓 Advanced: Dynamic Migration Discovery

To automatically discover migration files (instead of hardcoding):

```java
private List<String> getAvailableMigrations() throws Exception {
    List<String> migrations = new ArrayList<>();
    
    // Scan resources directory
    try (InputStream in = getClass().getResourceAsStream(MIGRATION_PATH)) {
        if (in != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("V") && line.endsWith(".sql")) {
                        migrations.add(line);
                    }
                }
            }
        }
    }
    
    Collections.sort(migrations); // Ensure alphabetical order
    return migrations;
}
```

---

## 📦 Summary

### ✅ What You Get

1. **Automatic Database Setup**
   - No manual SQL execution needed
   - Tables created on first run
   - Always up-to-date schema

2. **Version Control for Database**
   - Track changes over time
   - Easy rollback (delete from schema_version + manual cleanup)
   - Team collaboration friendly

3. **Safe Migrations**
   - Transaction support
   - Rollback on error
   - Idempotent

4. **Development Workflow**
   - Add SQL file → Recompile → Run
   - Migration happens automatically
   - No extra steps needed

---

**Status**: ✅ **Database Migration System Complete**

**Next Steps:**
1. Test with existing app
2. Verify conversations and messages tables work
3. Add more migrations as needed (V3, V4, etc.)

---

**Author**: PCM Development Team  
**Date**: November 12, 2024  
**Version**: 1.0.0

