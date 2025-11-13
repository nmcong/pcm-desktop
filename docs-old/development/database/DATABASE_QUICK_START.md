# Database Quick Start Guide

## 🚀 Quick Overview

This guide helps you get started with the SQLite database implementation for PCM Desktop.

---

## 📁 Project Structure

```
src/main/java/com/noteflix/pcm/
├── domain/
│   ├── entity/              # Domain entities (Project, Screen, etc.)
│   └── repository/          # Repository interfaces
├── infrastructure/
│   ├── database/           # Connection management
│   ├── dao/                # DAO implementations
│   ├── repository/         # Repository implementations
│   └── exception/          # Custom exceptions
└── application/
    └── service/            # Business logic services

src/main/resources/
└── db/
    └── migration/          # SQL migration scripts
```

---

## 🎯 Design Principles Applied

### SOLID Principles

1. **Single Responsibility Principle (SRP)**
    - Each class has ONE reason to change
    - Example: `ProjectDAO` only handles Project database operations

2. **Open/Closed Principle (OCP)**
    - Open for extension, closed for modification
    - Example: `AbstractDAO` can be extended without modification

3. **Liskov Substitution Principle (LSP)**
    - Subtypes can replace parent types
    - Example: Any `Repository<T, ID>` implementation can replace the interface

4. **Interface Segregation Principle (ISP)**
    - Specific interfaces over fat interfaces
    - Example: Separate `Readable` and `Writable` interfaces if needed

5. **Dependency Inversion Principle (DIP)**
    - Depend on abstractions, not implementations
    - Example: Services depend on `Repository` interface, not implementation

### Design Patterns

1. **Repository Pattern** - Abstraction over data access
2. **DAO Pattern** - Direct database access layer
3. **Singleton Pattern** - Connection manager
4. **Factory Pattern** - Entity creation
5. **Unit of Work Pattern** - Transaction management
6. **Specification Pattern** - Query building

---

## 🔧 Getting Started

### Step 1: Initialize Database

```java
// Initialize database and run migrations
DatabaseInitializer initializer = new DatabaseInitializer();
initializer.initialize();
```

### Step 2: Get a Connection

```java
// Get database connection (Singleton pattern)
Connection conn = ConnectionManager.INSTANCE.getConnection();
```

### Step 3: Use Repository

```java
// Create repository
ProjectRepository projectRepo = new ProjectRepositoryImpl();

// Create a project
Project project = Project.builder()
    .name("My Project")
    .code("MYPROJ")
    .type(ProjectType.MODULE)
    .status(ProjectStatus.ACTIVE)
    .build();

// Save project
Project saved = projectRepo.save(project);

// Find project
Optional<Project> found = projectRepo.findById(saved.getId());

// Find all projects
List<Project> all = projectRepo.findAll();
```

### Step 4: Use Service Layer

```java
// Use service for business logic
ProjectService projectService = new ProjectService(projectRepo);

// Service handles validation, logging, etc.
Project project = projectService.createProject("My Project", "MYPROJ");
```

---

## 📊 Database Schema Overview

### Core Tables

| Table              | Purpose                   | Key Fields                           |
|--------------------|---------------------------|--------------------------------------|
| `projects`         | Store projects/subsystems | id, name, code, type, status         |
| `screens`          | Store UI screens          | id, project_id, name, code, type     |
| `database_objects` | Store DB objects          | id, name, type, definition           |
| `batch_jobs`       | Store batch jobs          | id, name, schedule_cron, status      |
| `workflows`        | Store workflows           | id, name, workflow_data              |
| `knowledge_base`   | Store documentation       | id, title, content, embedding_vector |

### Relationship Tables

| Table              | Purpose                            |
|--------------------|------------------------------------|
| `screen_tags`      | Many-to-many: Screens ↔ Tags       |
| `screen_relations` | Many-to-many: Screen relationships |
| `workflow_steps`   | One-to-many: Workflow ↔ Steps      |

### System Tables

| Table            | Purpose              |
|------------------|----------------------|
| `activity_log`   | Audit trail          |
| `favorites`      | User favorites       |
| `settings`       | Application settings |
| `schema_version` | Migration tracking   |

---

## 🛠️ Common Operations

### Create

```java
Project project = Project.builder()
    .name("Customer Service")
    .code("CS")
    .type(ProjectType.SUBSYSTEM)
    .status(ProjectStatus.ACTIVE)
    .color("#3b82f6")
    .build();

project.validate(); // Validate before saving
Project saved = projectRepository.save(project);
```

### Read

```java
// By ID
Optional<Project> project = projectRepository.findById(1L);

// All
List<Project> allProjects = projectRepository.findAll();

// With specification (advanced)
Specification<Project> activeSpec = new ActiveProjectSpecification();
List<Project> activeProjects = projectRepository.findBySpecification(activeSpec);
```

### Update

```java
Project project = projectRepository.findById(1L).orElseThrow();
project.setDescription("Updated description");
projectRepository.update(project);
```

### Delete

```java
projectRepository.delete(1L);
```

### Transaction

```java
Connection conn = ConnectionManager.INSTANCE.getConnection();
try {
    conn.setAutoCommit(false);
    
    // Multiple operations
    projectDAO.save(project, conn);
    screenDAO.save(screen, conn);
    
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw new DatabaseException("Transaction failed", e);
} finally {
    conn.setAutoCommit(true);
}
```

---

## 🧪 Testing

### Unit Test Example

```java
@Test
void testSaveProject() {
    // Given
    Project project = Project.builder()
        .name("Test Project")
        .code("TEST")
        .type(ProjectType.MODULE)
        .build();
    
    // When
    Project saved = projectRepository.save(project);
    
    // Then
    assertNotNull(saved.getId());
    assertEquals("Test Project", saved.getName());
    assertEquals("TEST", saved.getCode());
}
```

### Integration Test Example

```java
@Test
void testProjectWithScreens() {
    // Create project
    Project project = projectService.createProject("Test", "TST");
    
    // Create screens
    Screen screen1 = screenService.createScreen(project.getId(), "Login", "LOGIN");
    Screen screen2 = screenService.createScreen(project.getId(), "Dashboard", "DASH");
    
    // Verify
    List<Screen> screens = screenService.findScreensByProject(project.getId());
    assertEquals(2, screens.size());
}
```

---

## 📝 Best Practices

### 1. Always Use Prepared Statements

```java
// ✅ Good
String sql = "SELECT * FROM projects WHERE id = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setLong(1, projectId);

// ❌ Bad (SQL injection risk)
String sql = "SELECT * FROM projects WHERE id = " + projectId;
```

### 2. Use Try-With-Resources

```java
// ✅ Good - auto-close resources
try (Connection conn = ConnectionManager.INSTANCE.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql);
     ResultSet rs = stmt.executeQuery()) {
    // Process results
}
```

### 3. Validate Before Persist

```java
// ✅ Good
project.validate(); // Throws exception if invalid
projectRepository.save(project);
```

### 4. Use Transactions for Multiple Operations

```java
// ✅ Good - atomic operation
transactionManager.executeInTransaction(conn -> {
    projectDAO.save(project, conn);
    screenDAO.saveAll(screens, conn);
});
```

### 5. Handle Exceptions Properly

```java
// ✅ Good - specific exception handling
try {
    project = projectRepository.findById(id)
        .orElseThrow(() -> DatabaseException.entityNotFound("Project", id));
} catch (DatabaseException e) {
    log.error("Failed to find project: {}", id, e);
    showErrorDialog("Project not found");
}
```

---

## 🚦 Common Pitfalls

### ❌ Don't Forget Foreign Keys

```sql
-- ✅ Enable foreign keys
PRAGMA foreign_keys = ON;
```

### ❌ Don't Forget to Close Connections

```java
// ✅ Use try-with-resources or finally block
```

### ❌ Don't Build SQL with String Concatenation

```java
// ❌ Bad
String sql = "SELECT * FROM projects WHERE name = '" + name + "'";

// ✅ Good
String sql = "SELECT * FROM projects WHERE name = ?";
```

### ❌ Don't Ignore Transactions

```java
// ❌ Bad - no transaction
projectDAO.save(project);
screenDAO.save(screen);

// ✅ Good - transaction ensures atomicity
transactionManager.executeInTransaction(conn -> {
    projectDAO.save(project, conn);
    screenDAO.save(screen, conn);
});
```

---

## 📚 Next Steps

1. **Read** the full [SQLITE_IMPLEMENTATION_PLAN.md](./SQLITE_IMPLEMENTATION_PLAN.md)
2. **Implement** Phase 1 (Foundation)
3. **Write** unit tests for each component
4. **Integrate** with UI layer
5. **Optimize** queries and indexes
6. **Document** your code with JavaDoc

---

## 🆘 Troubleshooting

### Database Locked Error

```
org.sqlite.SQLiteException: [SQLITE_BUSY] The database file is locked
```

**Solution**: Close all connections properly, use connection pooling

### Foreign Key Constraint Failed

```
org.sqlite.SQLiteException: FOREIGN KEY constraint failed
```

**Solution**: Ensure `PRAGMA foreign_keys = ON` and referenced entities exist

### Table Already Exists

```
org.sqlite.SQLiteException: table projects already exists
```

**Solution**: Use `CREATE TABLE IF NOT EXISTS` or check migration state

---

## 📖 Resources

- [SQLite Documentation](https://www.sqlite.org/docs.html)
- [JDBC Tutorial](https://docs.oracle.com/javase/tutorial/jdbc/)
- [Design Patterns](https://refactoring.guru/design-patterns/java)
- [Clean Code](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)

---

**Happy Coding! 🚀**

