# 🗄️ Database Development

Tài liệu về database implementation và quản lý trong PCM Desktop.

## 📚 Tài Liệu

- **[DATABASE_QUICK_START.md](DATABASE_QUICK_START.md)** - Bắt đầu nhanh với Database
    - Setup database
    - Connection management
    - Basic CRUD operations
    - Examples

- **[SQLITE_IMPLEMENTATION_PLAN.md](SQLITE_IMPLEMENTATION_PLAN.md)** - Kế hoạch SQLite
    - Architecture design
    - Schema design
    - Migration strategy
    - Performance optimization

## 🏗️ Architecture

### Database Stack

```
┌─────────────────────────────────────┐
│     Application Layer               │
│  - Services                         │
│  - Business Logic                   │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│     Repository Layer                │
│  - ConversationRepository           │
│  - UserRepository                   │
│  - SettingsRepository               │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│     DAO Layer                       │
│  - ConversationDAO                  │
│  - MessageDAO                       │
│  - UserDAO                          │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│     Database Layer                  │
│  - SQLite Database                  │
│  - Connection Manager               │
│  - Migration Manager                │
└─────────────────────────────────────┘
```

## 📊 Database Schema

### Main Tables

- **conversations** - Chat conversations
- **messages** - Chat messages
- **users** - User information
- **settings** - Application settings
- **embeddings** - Vector embeddings (for RAG)

### Relationships

```
conversations (1) ──── (*) messages
users (1) ──── (*) conversations
conversations (1) ──── (*) embeddings
```

## 🚀 Features

### ✅ Implemented

- ✅ SQLite database
- ✅ Connection pooling
- ✅ Transaction management
- ✅ Repository pattern
- ✅ DAO pattern
- ✅ Migration system
- ✅ Error handling

### 🚧 In Progress

- 🚧 Vector database integration
- 🚧 Full-text search
- 🚧 Database encryption
- 🚧 Backup/restore

### 📋 Planned

- 📋 Cloud sync
- 📋 Multi-user support
- 📋 Advanced indexing
- 📋 Query optimization

## 💡 Quick Examples

### Get Connection

```java
ConnectionManager cm = ConnectionManager.INSTANCE;
try (Connection conn = cm.getConnection()) {
    // Use connection
}
```

### Using Repository

```java
ConversationRepository repo = new ConversationRepositoryImpl();
Conversation conv = repo.save(newConversation);
List<Conversation> recent = repo.findRecent(10);
```

### Using DAO

```java
ConversationDAO dao = new ConversationDAOImpl(connectionManager);
dao.insert(conversation);
Optional<Conversation> found = dao.findById(id);
```

## 🔧 Database Location

### Default Paths

- **macOS/Linux**: `~/.pcm/pcm-desktop.db`
- **Windows**: `%USERPROFILE%\.pcm\pcm-desktop.db`

### Custom Path

```java
System.setProperty("pcm.db.path", "/custom/path/db.sqlite");
```

## 📝 Migration

### Creating Migration

```sql
-- V3__add_user_preferences.sql
CREATE TABLE user_preferences (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    preference_key TEXT NOT NULL,
    preference_value TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Running Migrations

```java
MigrationManager migrationManager = new MigrationManager();
migrationManager.runMigrations();
```

## 🔗 Related Documentation

- [Database Integration Guide](../../guides/integration/DATABASE_README.md)
- [Database Migration Guide](../../guides/integration/DATABASE_MIGRATION_GUIDE.md)
- [LLM Integration](../llm/) - For conversation storage

## 📞 Support

- Check [TROUBLESHOOTING.md](../../troubleshooting/TROUBLESHOOTING.md)
- See examples in `src/main/java/com/noteflix/pcm/infrastructure/`

---

**Status**: ✅ Core Features Complete, 🚧 Advanced Features In Progress  
**Updated**: 12/11/2025

