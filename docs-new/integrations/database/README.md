# 🗄️ Database Integration - PCM Desktop

## 📋 Overview

PCM Desktop provides comprehensive database integration capabilities, supporting multiple database systems with a focus on SQLite as the default choice. The system is designed following SOLID principles and clean architecture patterns.

## 🎯 Supported Databases

### ✅ Currently Supported
- **SQLite** - Default embedded database (recommended for development and small deployments)
- **Oracle Database** - Enterprise-grade database (in progress)

### 🚧 Planned Support
- **PostgreSQL** - Open-source database
- **MySQL** - Popular open-source database
- **SQL Server** - Microsoft database

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UI Layer      │    │   Service Layer │    │   Data Layer    │
│                 │    │                 │    │                 │
│ • Controllers   │◄──►│ • Business      │◄──►│ • Repositories  │
│ • Views         │    │   Services      │    │ • DAOs          │
│ • Models        │    │ • Validators    │    │ • Entities      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │   Database      │
                                               │                 │
                                               │ • SQLite        │
                                               │ • Oracle        │
                                               │ • PostgreSQL    │
                                               └─────────────────┘
```

## 📊 Database Schema

### Core Tables

1. **projects** - Project management
2. **screens** - Screen/form definitions  
3. **database_objects** - Database objects metadata
4. **batch_jobs** - Batch job tracking
5. **workflows** - Workflow definitions
6. **knowledge_base** - Knowledge management
7. **ai_prompts** - AI prompt templates
8. **audit_logs** - System audit trail
9. **users** - User management
10. **user_roles** - Role definitions
11. **user_permissions** - Permission matrix
12. **system_configs** - Configuration storage
13. **subsystems** - Subsystem definitions

### Key Features

- ✅ **Full referential integrity** with foreign keys
- ✅ **Auto-timestamp triggers** for audit trail
- ✅ **Convenient views** for common queries
- ✅ **Sample data** for testing and development
- ✅ **Migration system** for schema evolution

## 🚀 Quick Start

### 1. Default SQLite Setup

```java
// Initialize with default SQLite database
ConnectionManager.INSTANCE.initialize("jdbc:sqlite:pcm.db");

// Or specify custom path
ConnectionManager.INSTANCE.initialize("jdbc:sqlite:/path/to/your/database.db");
```

### 2. Oracle Database Setup

```java
// Oracle connection
String url = "jdbc:oracle:thin:@localhost:1521:xe";
String username = "pcm_user";
String password = "your_password";

ConnectionManager.INSTANCE.initialize(url, username, password);
```

### 3. Basic Operations

```java
// Get service instances
ProjectService projectService = ServiceFactory.getProjectService();
ScreenService screenService = ServiceFactory.getScreenService();

// Create a new project
Project project = new Project();
project.setName("My Project");
project.setDescription("Project description");
project.setStatus("ACTIVE");

Long projectId = projectService.createProject(project);

// Find projects
List<Project> activeProjects = projectService.findProjectsByStatus("ACTIVE");
```

## 📁 Quick Navigation

### 📖 Getting Started
- **[Database Setup](database-setup.md)** - Complete setup guide for all databases
- **[Quick Start Guide](database-quick-start.md)** - Get started in 5 minutes
- **[Migration Guide](migration-guide.md)** - Database schema migrations

### 🏗️ Implementation Details  
- **[Schema Design](schema-design.md)** - Complete database schema
- **[Repository Pattern](repository-pattern.md)** - Data access patterns
- **[Transaction Management](transaction-management.md)** - ACID transactions

### 🔧 Configuration
- **[Connection Management](connection-management.md)** - Database connections
- **[Performance Tuning](performance-tuning.md)** - Optimization techniques
- **[Security](database-security.md)** - Security best practices

### 🧪 Testing
- **[Unit Testing](database-testing.md)** - Testing database operations
- **[Sample Data](sample-data.md)** - Test data generation
- **[Mock Strategies](mocking-strategies.md)** - Testing patterns

## 🎨 Design Patterns Used

### 1. Repository Pattern
```java
public interface ProjectRepository {
    Long create(Project project);
    Optional<Project> findById(Long id);
    List<Project> findAll();
    boolean update(Project project);
    boolean delete(Long id);
}
```

### 2. DAO Pattern
```java
public class ProjectDAO {
    public Long insert(Project project) { /* SQL implementation */ }
    public Project selectById(Long id) { /* SQL implementation */ }
    // ... other CRUD operations
}
```

### 3. Unit of Work Pattern
```java
try (UnitOfWork uow = UnitOfWork.begin()) {
    projectService.createProject(project);
    screenService.createScreen(screen);
    uow.commit();
} catch (Exception e) {
    // Auto-rollback on exception
}
```

### 4. Specification Pattern
```java
Specification<Project> spec = ProjectSpecifications
    .hasStatus("ACTIVE")
    .and(ProjectSpecifications.createdAfter(LocalDate.now().minusMonths(1)));
    
List<Project> projects = projectRepository.findAll(spec);
```

## 🔧 Configuration Examples

### SQLite Configuration
```json
{
  "database": {
    "type": "sqlite",
    "url": "jdbc:sqlite:pcm.db",
    "properties": {
      "enable_load_extension": true,
      "journal_mode": "WAL",
      "synchronous": "NORMAL"
    }
  }
}
```

### Oracle Configuration
```json
{
  "database": {
    "type": "oracle",
    "url": "jdbc:oracle:thin:@//localhost:1521/xe",
    "username": "pcm_user",
    "password": "${DB_PASSWORD}",
    "properties": {
      "oracle.jdbc.timezoneAsRegion": false,
      "oracle.net.disableOob": true
    },
    "pool": {
      "minimumIdle": 2,
      "maximumPoolSize": 10,
      "connectionTimeout": 30000
    }
  }
}
```

## 📊 Performance Features

### Connection Pooling
```java
// Automatic connection pooling
ConnectionPool pool = ConnectionPoolManager.getPool();
pool.setMinimumIdle(2);
pool.setMaximumPoolSize(10);
pool.setConnectionTimeout(30000);
```

### Query Optimization
```java
// Prepared statements with caching
String sql = "SELECT * FROM projects WHERE status = ? AND created_date > ?";
PreparedStatementCache.getCachedStatement(sql);

// Batch operations
List<Project> projects = Arrays.asList(/* large list */);
projectRepository.batchCreate(projects);
```

### Caching Strategy
```java
// Result caching
@Cacheable(value = "projects", key = "#id")
public Project findById(Long id) {
    return projectDAO.selectById(id);
}

// Cache eviction
@CacheEvict(value = "projects", key = "#project.id")
public boolean update(Project project) {
    return projectDAO.update(project);
}
```

## 🔒 Security Features

### SQL Injection Prevention
```java
// ✅ Good: Parameterized queries
String sql = "SELECT * FROM projects WHERE name = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setString(1, projectName);

// ❌ Bad: String concatenation
String sql = "SELECT * FROM projects WHERE name = '" + projectName + "'";
```

### Access Control
```java
// Role-based access
@RequiresRole("ADMIN")
public boolean deleteProject(Long projectId) {
    return projectRepository.delete(projectId);
}

// Data filtering based on user permissions
public List<Project> findAccessibleProjects(User user) {
    return projectRepository.findByUserAccess(user.getId());
}
```

### Audit Logging
```java
// Automatic audit trail
@AuditLog(action = "CREATE", entity = "Project")
public Long createProject(Project project) {
    // Implementation automatically logs the action
    return projectDAO.insert(project);
}
```

## 🧪 Testing Support

### In-Memory Testing
```java
@TestConfiguration
public class TestDatabaseConfig {
    
    @Bean
    @Primary
    public DataSource testDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema.sql")
            .addScript("classpath:test-data.sql")
            .build();
    }
}
```

### Mock Repositories
```java
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @InjectMocks
    private ProjectService projectService;
    
    @Test
    void shouldCreateProject() {
        // Given
        Project project = new Project("Test Project");
        when(projectRepository.create(project)).thenReturn(1L);
        
        // When
        Long projectId = projectService.createProject(project);
        
        // Then
        assertThat(projectId).isEqualTo(1L);
        verify(projectRepository).create(project);
    }
}
```

## 🚀 Migration System

### Version Management
```java
// Database migration
MigrationManager migrationManager = new MigrationManager();
migrationManager.migrate(); // Automatically applies pending migrations

// Check current version
int currentVersion = migrationManager.getCurrentVersion();
List<Migration> pendingMigrations = migrationManager.getPendingMigrations();
```

### Migration Scripts
```sql
-- V1__Create_projects_table.sql
CREATE TABLE projects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V2__Add_project_owner.sql  
ALTER TABLE projects ADD COLUMN owner_id INTEGER;
ALTER TABLE projects ADD FOREIGN KEY (owner_id) REFERENCES users(id);
```

## 📈 Monitoring & Metrics

### Database Metrics
```java
// Query performance monitoring
@Timed(name = "database.query.duration", description = "Database query duration")
public List<Project> findProjects(ProjectCriteria criteria) {
    return projectRepository.findByCriteria(criteria);
}

// Connection pool monitoring
@Gauge(name = "database.connections.active", description = "Active database connections")
public int getActiveConnections() {
    return connectionPool.getActiveConnections();
}
```

### Health Checks
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            connectionManager.testConnection();
            return Health.up()
                .withDetail("database", "Available")
                .withDetail("type", "SQLite")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 🔧 Troubleshooting

### Common Issues

1. **Connection Issues**
   - Check database file permissions
   - Verify connection URL format
   - Ensure database driver is in classpath

2. **Performance Issues**
   - Enable query logging to identify slow queries
   - Check for missing indexes
   - Consider connection pool tuning

3. **Migration Issues**
   - Backup database before running migrations
   - Check migration script syntax
   - Verify migration version conflicts

### Debug Configuration
```properties
# Logging configuration
logging.level.org.springframework.jdbc=DEBUG
logging.level.com.noteflix.pcm.data=DEBUG

# SQL statement logging
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## 📚 Best Practices

1. **Connection Management**
   - Always use connection pooling in production
   - Close resources properly (use try-with-resources)
   - Set appropriate timeout values

2. **Query Optimization**
   - Use prepared statements for parameterized queries
   - Add indexes for frequently queried columns
   - Use batch operations for bulk inserts/updates

3. **Transaction Management**
   - Keep transactions as short as possible
   - Use appropriate isolation levels
   - Handle rollback scenarios properly

4. **Security**
   - Never concatenate user input in SQL queries
   - Use role-based access control
   - Encrypt sensitive data at rest

5. **Testing**
   - Use in-memory databases for unit tests
   - Test migration scripts thoroughly
   - Mock external dependencies

---

For detailed implementation guides, choose the appropriate section from the links above.

*Last updated: $(date)*