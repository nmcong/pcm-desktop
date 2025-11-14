# Oracle Database Connectivity Implementation Guide

## Tổng quan
Tài liệu này hướng dẫn chi tiết việc tích hợp kết nối Oracle Database vào PCM Desktop Application. Ứng dụng hiện tại sử dụng SQLite, và tài liệu này sẽ cung cấp roadmap hoàn chỉnh để hỗ trợ Oracle Database song song hoặc thay thế SQLite.

## Kiến trúc hiện tại

### Hệ thống Database hiện tại
- **Database**: SQLite (pcm-desktop.db)
- **Connection Manager**: `ConnectionManager.java` (Singleton pattern)
- **Migration**: `DatabaseMigrationManager.java` (Flyway-like)
- **URL**: `jdbc:sqlite:pcm-desktop.db`
- **Driver**: `org.sqlite.JDBC`

### Cấu trúc thành phần chính
```
src/main/java/com/noteflix/pcm/infrastructure/
├── database/
│   ├── ConnectionManager.java
│   └── DatabaseMigrationManager.java
├── dao/
│   ├── DAO.java (interface)
│   ├── ConversationDAO.java
│   └── MessageDAO.java
└── repository/
    └── chat/
        └── ConversationRepositoryImpl.java
```

## 1. Cài đặt Dependencies

### 1.1 Thêm Oracle JDBC Driver
Thêm vào file dependencies hiện tại (library structure):

```xml
<!-- Oracle JDBC Driver -->
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc11</artifactId>
    <version>23.3.0.23.09</version>
</dependency>

<!-- HikariCP Connection Pool (recommended) -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>

<!-- Oracle UCP (Universal Connection Pool) - Alternative -->
<dependency>
    <groupId>com.oracle.database.ucp</groupId>
    <artifactId>ucp</artifactId>
    <version>23.3.0.23.09</version>
</dependency>
```

### 1.2 Tải Oracle JDBC Jar (Manual)
Nếu không dùng Maven/Gradle, tải về và thêm vào `lib/` folder:

```bash
# Download Oracle JDBC từ Oracle website
wget https://download.oracle.com/otn-pub/otn_software/jdbc/233/ojdbc11.jar
mv ojdbc11.jar lib/database/ojdbc11.jar
```

## 2. Cấu hình Database Connection

### 2.1 Database Configuration Model
Tạo file `DatabaseConfig.java`:

```java
package com.noteflix.pcm.infrastructure.database;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseConfig {
    private DatabaseType type;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String serviceName;  // For Oracle
    private String sid;          // For Oracle
    private ConnectionPoolConfig poolConfig;
    
    public enum DatabaseType {
        SQLITE, ORACLE, MYSQL, POSTGRESQL
    }
    
    @Data
    @Builder
    public static class ConnectionPoolConfig {
        private int minimumIdle = 5;
        private int maximumPoolSize = 20;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
    }
}
```

### 2.2 Enhanced ConnectionManager
Cập nhật `ConnectionManager.java` để hỗ trợ nhiều loại database:

```java
package com.noteflix.pcm.infrastructure.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnhancedConnectionManager {
    
    private static EnhancedConnectionManager instance;
    private HikariDataSource dataSource;
    private DatabaseConfig config;
    
    private EnhancedConnectionManager() {}
    
    public static synchronized EnhancedConnectionManager getInstance() {
        if (instance == null) {
            instance = new EnhancedConnectionManager();
        }
        return instance;
    }
    
    public void initialize(DatabaseConfig config) {
        this.config = config;
        setupDataSource();
    }
    
    private void setupDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        
        switch (config.getType()) {
            case ORACLE:
                setupOracleConfig(hikariConfig);
                break;
            case SQLITE:
                setupSQLiteConfig(hikariConfig);
                break;
            case MYSQL:
                setupMySQLConfig(hikariConfig);
                break;
            case POSTGRESQL:
                setupPostgreSQLConfig(hikariConfig);
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + config.getType());
        }
        
        // Pool configuration
        if (config.getPoolConfig() != null) {
            hikariConfig.setMinimumIdle(config.getPoolConfig().getMinimumIdle());
            hikariConfig.setMaximumPoolSize(config.getPoolConfig().getMaximumPoolSize());
            hikariConfig.setConnectionTimeout(config.getPoolConfig().getConnectionTimeout());
            hikariConfig.setIdleTimeout(config.getPoolConfig().getIdleTimeout());
            hikariConfig.setMaxLifetime(config.getPoolConfig().getMaxLifetime());
        }
        
        this.dataSource = new HikariDataSource(hikariConfig);
        log.info("✅ Database connection pool initialized: {}", config.getType());
    }
    
    private void setupOracleConfig(HikariConfig hikariConfig) {
        // Oracle connection URL formats:
        // 1. Service name: jdbc:oracle:thin:@//host:port/service_name
        // 2. SID: jdbc:oracle:thin:@host:port:SID
        // 3. TNS: jdbc:oracle:thin:@TNS_NAME
        
        String jdbcUrl;
        if (config.getServiceName() != null) {
            jdbcUrl = String.format("jdbc:oracle:thin:@//%s:%d/%s", 
                config.getHost(), config.getPort(), config.getServiceName());
        } else if (config.getSid() != null) {
            jdbcUrl = String.format("jdbc:oracle:thin:@%s:%d:%s", 
                config.getHost(), config.getPort(), config.getSid());
        } else {
            jdbcUrl = String.format("jdbc:oracle:thin:@%s:%d:%s", 
                config.getHost(), config.getPort(), config.getDatabase());
        }
        
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName("oracle.jdbc.OracleDriver");
        
        // Oracle specific properties
        hikariConfig.addDataSourceProperty("oracle.jdbc.ReadTimeout", "30000");
        hikariConfig.addDataSourceProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        hikariConfig.addDataSourceProperty("oracle.jdbc.implicitStatementCacheSize", "25");
        hikariConfig.addDataSourceProperty("oracle.jdbc.defaultExecuteBatch", "50");
        
        // Connection validation
        hikariConfig.setConnectionTestQuery("SELECT 1 FROM DUAL");
    }
    
    private void setupSQLiteConfig(HikariConfig hikariConfig) {
        // Keep existing SQLite logic
        hikariConfig.setJdbcUrl("jdbc:sqlite:pcm-desktop.db");
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setMaximumPoolSize(1); // SQLite is single-threaded
    }
    
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not initialized. Call initialize() first.");
        }
        return dataSource.getConnection();
    }
    
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            log.error("❌ Database connection test failed", e);
            return false;
        }
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("✅ Database connection pool closed");
        }
    }
}
```

### 2.3 Configuration Factory
Tạo `DatabaseConfigFactory.java`:

```java
package com.noteflix.pcm.infrastructure.database;

import java.util.Properties;

public class DatabaseConfigFactory {
    
    public static DatabaseConfig createFromProperties(Properties props) {
        String type = props.getProperty("database.type", "SQLITE");
        
        DatabaseConfig.DatabaseConfigBuilder builder = DatabaseConfig.builder()
            .type(DatabaseConfig.DatabaseType.valueOf(type.toUpperCase()));
            
        if ("ORACLE".equalsIgnoreCase(type)) {
            return builder
                .host(props.getProperty("oracle.host", "localhost"))
                .port(Integer.parseInt(props.getProperty("oracle.port", "1521")))
                .database(props.getProperty("oracle.database"))
                .serviceName(props.getProperty("oracle.serviceName"))
                .sid(props.getProperty("oracle.sid"))
                .username(props.getProperty("oracle.username"))
                .password(props.getProperty("oracle.password"))
                .poolConfig(DatabaseConfig.ConnectionPoolConfig.builder()
                    .minimumIdle(Integer.parseInt(props.getProperty("pool.minimumIdle", "5")))
                    .maximumPoolSize(Integer.parseInt(props.getProperty("pool.maximumPoolSize", "20")))
                    .build())
                .build();
        }
        
        return builder.build(); // Default SQLite
    }
    
    public static DatabaseConfig createOracleConfig(String host, int port, 
            String serviceName, String username, String password) {
        return DatabaseConfig.builder()
            .type(DatabaseConfig.DatabaseType.ORACLE)
            .host(host)
            .port(port)
            .serviceName(serviceName)
            .username(username)
            .password(password)
            .poolConfig(DatabaseConfig.ConnectionPoolConfig.builder()
                .minimumIdle(5)
                .maximumPoolSize(20)
                .connectionTimeout(30000)
                .idleTimeout(600000)
                .maxLifetime(1800000)
                .build())
            .build();
    }
}
```

## 3. Database Migration cho Oracle

### 3.1 Oracle-specific Migration Manager
Tạo `OracleMigrationManager.java`:

```java
package com.noteflix.pcm.infrastructure.database;

import java.sql.Connection;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OracleMigrationManager extends DatabaseMigrationManager {
    
    public OracleMigrationManager(EnhancedConnectionManager connectionManager) {
        super(connectionManager);
    }
    
    @Override
    protected void createSchemaVersionTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE schema_version (
                version VARCHAR2(50) PRIMARY KEY,
                description VARCHAR2(200),
                applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
            
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.debug("Oracle schema version table created/verified");
        } catch (SQLException e) {
            if (!e.getMessage().contains("ORA-00955")) { // Table already exists
                throw e;
            }
        }
    }
    
    @Override
    protected String getAvailableMigrations() {
        // Return Oracle-specific migration files
        return """
            V1__oracle_initial_schema.sql
            V2__oracle_chat_tables.sql
            V3__oracle_indexes.sql
            """;
    }
}
```

### 3.2 Oracle Migration Scripts
Tạo thư mục `src/main/resources/db/migration/oracle/`:

**V1__oracle_initial_schema.sql**:
```sql
-- Oracle Initial Schema
-- PCM Desktop Application

-- Projects table
CREATE TABLE projects (
    id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR2(200) NOT NULL,
    description CLOB,
    project_type VARCHAR2(50) DEFAULT 'GENERAL',
    project_status VARCHAR2(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_project_type CHECK (project_type IN ('GENERAL', 'SOFTWARE', 'RESEARCH', 'BUSINESS')),
    CONSTRAINT chk_project_status CHECK (project_status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED', 'COMPLETED'))
);

-- Create updated_at trigger for projects
CREATE OR REPLACE TRIGGER trg_projects_updated_at
    BEFORE UPDATE ON projects
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Create sequence for projects (if not using IDENTITY)
-- CREATE SEQUENCE seq_projects START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE projects IS 'PCM Desktop Application Projects';
COMMENT ON COLUMN projects.id IS 'Primary key - auto generated';
COMMENT ON COLUMN projects.name IS 'Project name';
COMMENT ON COLUMN projects.description IS 'Project description (CLOB for large text)';
COMMENT ON COLUMN projects.project_type IS 'Type of project';
COMMENT ON COLUMN projects.project_status IS 'Current status of project';
```

**V2__oracle_chat_tables.sql**:
```sql
-- Chat system tables for Oracle
-- Conversations and Messages

-- Conversations table
CREATE TABLE conversations (
    id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    title VARCHAR2(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    project_id NUMBER,
    CONSTRAINT fk_conversations_project 
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- Messages table
CREATE TABLE messages (
    id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    conversation_id NUMBER NOT NULL,
    role VARCHAR2(50) NOT NULL,
    content CLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata CLOB, -- JSON metadata stored as CLOB
    CONSTRAINT fk_messages_conversation 
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT chk_message_role 
        CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM'))
);

-- Triggers for updated_at
CREATE OR REPLACE TRIGGER trg_conversations_updated_at
    BEFORE UPDATE ON conversations
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Indexes for performance
CREATE INDEX idx_conversations_project_id ON conversations(project_id);
CREATE INDEX idx_conversations_created_at ON conversations(created_at);
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_messages_role ON messages(role);

-- Comments
COMMENT ON TABLE conversations IS 'Chat conversations in PCM application';
COMMENT ON TABLE messages IS 'Individual messages within conversations';
COMMENT ON COLUMN messages.metadata IS 'JSON metadata stored as CLOB (can be enhanced with JSON data type in Oracle 21c+)';
```

## 4. Data Access Layer (DAO) Enhancement

### 4.1 Oracle-specific DAO Implementation
Tạo `OracleConversationDAO.java`:

```java
package com.noteflix.pcm.infrastructure.dao.oracle;

import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.infrastructure.dao.ConversationDAO;
import com.noteflix.pcm.infrastructure.database.EnhancedConnectionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OracleConversationDAO implements ConversationDAO {
    
    private final EnhancedConnectionManager connectionManager;
    
    public OracleConversationDAO(EnhancedConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    @Override
    public Long create(Conversation conversation) {
        String sql = """
            INSERT INTO conversations (title, project_id) 
            VALUES (?, ?) 
            RETURNING id INTO ?
            """;
            
        try (Connection conn = connectionManager.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
             
            stmt.setString(1, conversation.getTitle());
            if (conversation.getProjectId() != null) {
                stmt.setLong(2, conversation.getProjectId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            
            // Register OUT parameter for RETURNING clause
            stmt.registerOutParameter(3, Types.BIGINT);
            
            stmt.executeUpdate();
            
            Long id = stmt.getLong(3);
            log.info("✅ Created conversation with ID: {}", id);
            return id;
            
        } catch (SQLException e) {
            log.error("❌ Failed to create conversation", e);
            throw new RuntimeException("Failed to create conversation", e);
        }
    }
    
    @Override
    public Optional<Conversation> findById(Long id) {
        String sql = """
            SELECT id, title, created_at, updated_at, project_id 
            FROM conversations 
            WHERE id = ?
            """;
            
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToConversation(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("❌ Failed to find conversation by ID: {}", id, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Conversation> findAll() {
        String sql = """
            SELECT id, title, created_at, updated_at, project_id 
            FROM conversations 
            ORDER BY updated_at DESC
            """;
            
        List<Conversation> conversations = new ArrayList<>();
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                conversations.add(mapResultSetToConversation(rs));
            }
            
        } catch (SQLException e) {
            log.error("❌ Failed to find all conversations", e);
        }
        
        return conversations;
    }
    
    @Override
    public void update(Conversation conversation) {
        String sql = """
            UPDATE conversations 
            SET title = ?, project_id = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE id = ?
            """;
            
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, conversation.getTitle());
            if (conversation.getProjectId() != null) {
                stmt.setLong(2, conversation.getProjectId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setLong(3, conversation.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Conversation not found: " + conversation.getId());
            }
            
            log.info("✅ Updated conversation ID: {}", conversation.getId());
            
        } catch (SQLException e) {
            log.error("❌ Failed to update conversation", e);
            throw new RuntimeException("Failed to update conversation", e);
        }
    }
    
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM conversations WHERE id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setLong(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Conversation not found: " + id);
            }
            
            log.info("✅ Deleted conversation ID: {}", id);
            
        } catch (SQLException e) {
            log.error("❌ Failed to delete conversation", e);
            throw new RuntimeException("Failed to delete conversation", e);
        }
    }
    
    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        return Conversation.builder()
            .id(rs.getLong("id"))
            .title(rs.getString("title"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .projectId(rs.getLong("project_id"))
            .build();
    }
}
```

### 4.2 DAO Factory Pattern
Tạo `DAOFactory.java`:

```java
package com.noteflix.pcm.infrastructure.dao;

import com.noteflix.pcm.infrastructure.dao.oracle.OracleConversationDAO;
import com.noteflix.pcm.infrastructure.dao.sqlite.SQLiteConversationDAO;
import com.noteflix.pcm.infrastructure.database.DatabaseConfig;
import com.noteflix.pcm.infrastructure.database.EnhancedConnectionManager;

public class DAOFactory {
    
    private final DatabaseConfig.DatabaseType databaseType;
    private final EnhancedConnectionManager connectionManager;
    
    public DAOFactory(DatabaseConfig.DatabaseType databaseType, 
                     EnhancedConnectionManager connectionManager) {
        this.databaseType = databaseType;
        this.connectionManager = connectionManager;
    }
    
    public ConversationDAO createConversationDAO() {
        switch (databaseType) {
            case ORACLE:
                return new OracleConversationDAO(connectionManager);
            case SQLITE:
                return new SQLiteConversationDAO(connectionManager);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }
    
    public MessageDAO createMessageDAO() {
        switch (databaseType) {
            case ORACLE:
                return new OracleMessageDAO(connectionManager);
            case SQLITE:
                return new SQLiteMessageDAO(connectionManager);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }
}
```

## 5. Configuration Management

### 5.1 Application Properties
Tạo file `database.properties`:

```properties
# Database Configuration for PCM Desktop
# Supported types: SQLITE, ORACLE, MYSQL, POSTGRESQL

# Default database type
database.type=SQLITE

# SQLite Configuration (current)
sqlite.url=jdbc:sqlite:pcm-desktop.db

# Oracle Configuration
oracle.host=localhost
oracle.port=1521
oracle.database=PCMDB
oracle.serviceName=PCMSERVICE
# oracle.sid=PCMSID  # Use either serviceName or SID
oracle.username=pcm_user
oracle.password=pcm_password

# Connection Pool Settings
pool.minimumIdle=5
pool.maximumPoolSize=20
pool.connectionTimeout=30000
pool.idleTimeout=600000
pool.maxLifetime=1800000
pool.validationTimeout=5000

# Oracle specific settings
oracle.readTimeout=30000
oracle.connectTimeout=10000
oracle.statementCacheSize=25
oracle.executeBatch=50

# Migration settings
migration.enabled=true
migration.validateOnMigrate=true
migration.outOfOrder=false
```

### 5.2 Environment-based Configuration
Tạo `DatabaseConfigLoader.java`:

```java
package com.noteflix.pcm.infrastructure.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseConfigLoader {
    
    private static final String DEFAULT_CONFIG_FILE = "database.properties";
    
    public static DatabaseConfig loadFromResources() {
        return loadFromResources(DEFAULT_CONFIG_FILE);
    }
    
    public static DatabaseConfig loadFromResources(String configFile) {
        Properties props = new Properties();
        
        try (InputStream is = DatabaseConfigLoader.class.getResourceAsStream("/" + configFile)) {
            if (is == null) {
                log.warn("Configuration file not found: {}. Using defaults.", configFile);
                return getDefaultSQLiteConfig();
            }
            
            props.load(is);
            log.info("✅ Database configuration loaded from: {}", configFile);
            
        } catch (IOException e) {
            log.error("❌ Failed to load configuration: {}", configFile, e);
            return getDefaultSQLiteConfig();
        }
        
        // Override with environment variables
        overrideWithEnvironmentVariables(props);
        
        return DatabaseConfigFactory.createFromProperties(props);
    }
    
    private static void overrideWithEnvironmentVariables(Properties props) {
        // Allow environment variable overrides
        String dbType = System.getenv("DB_TYPE");
        if (dbType != null) {
            props.setProperty("database.type", dbType);
        }
        
        String oracleHost = System.getenv("ORACLE_HOST");
        if (oracleHost != null) {
            props.setProperty("oracle.host", oracleHost);
        }
        
        String oraclePort = System.getenv("ORACLE_PORT");
        if (oraclePort != null) {
            props.setProperty("oracle.port", oraclePort);
        }
        
        String oracleService = System.getenv("ORACLE_SERVICE");
        if (oracleService != null) {
            props.setProperty("oracle.serviceName", oracleService);
        }
        
        String oracleUser = System.getenv("ORACLE_USERNAME");
        if (oracleUser != null) {
            props.setProperty("oracle.username", oracleUser);
        }
        
        String oraclePassword = System.getenv("ORACLE_PASSWORD");
        if (oraclePassword != null) {
            props.setProperty("oracle.password", oraclePassword);
        }
        
        log.info("Configuration overridden with environment variables");
    }
    
    private static DatabaseConfig getDefaultSQLiteConfig() {
        return DatabaseConfig.builder()
            .type(DatabaseConfig.DatabaseType.SQLITE)
            .build();
    }
}
```

## 6. Integration vào Application

### 6.1 Cập nhật PCMApplication.java
Modify main application để support multiple databases:

```java
package com.noteflix.pcm;

import com.noteflix.pcm.infrastructure.database.*;
import com.noteflix.pcm.infrastructure.dao.DAOFactory;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PCMApplication extends Application {
    
    private EnhancedConnectionManager connectionManager;
    private DAOFactory daoFactory;
    
    @Override
    public void init() throws Exception {
        super.init();
        
        // Load database configuration
        DatabaseConfig dbConfig = DatabaseConfigLoader.loadFromResources();
        log.info("🔧 Database configuration loaded: {}", dbConfig.getType());
        
        // Initialize connection manager
        connectionManager = EnhancedConnectionManager.getInstance();
        connectionManager.initialize(dbConfig);
        
        // Test connection
        if (!connectionManager.testConnection()) {
            throw new RuntimeException("Failed to establish database connection");
        }
        log.info("✅ Database connection established successfully");
        
        // Initialize DAO factory
        daoFactory = new DAOFactory(dbConfig.getType(), connectionManager);
        
        // Run migrations
        if (dbConfig.getType() == DatabaseConfig.DatabaseType.ORACLE) {
            OracleMigrationManager migrationManager = new OracleMigrationManager(connectionManager);
            migrationManager.migrate();
        } else {
            DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(connectionManager);
            migrationManager.migrate();
        }
        
        log.info("🚀 PCM Application initialized with {} database", dbConfig.getType());
    }
    
    @Override
    public void stop() throws Exception {
        // Cleanup database connections
        if (connectionManager != null) {
            connectionManager.close();
            log.info("✅ Database connections closed");
        }
        
        super.stop();
    }
    
    // Getter for dependency injection
    public DAOFactory getDAOFactory() {
        return daoFactory;
    }
}
```

### 6.2 Dependency Injection Update
Cập nhật `Injector.java`:

```java
package com.noteflix.pcm.core.di;

import com.noteflix.pcm.infrastructure.dao.DAOFactory;
import com.noteflix.pcm.infrastructure.database.EnhancedConnectionManager;

public class Injector {
    
    private static Injector instance;
    private DAOFactory daoFactory;
    private EnhancedConnectionManager connectionManager;
    
    // ... existing code ...
    
    public void setDAOFactory(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }
    
    public DAOFactory getDAOFactory() {
        return daoFactory;
    }
    
    public EnhancedConnectionManager getConnectionManager() {
        return connectionManager;
    }
    
    public void setConnectionManager(EnhancedConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
}
```

## 7. Testing & Validation

### 7.1 Database Connection Test
Tạo `DatabaseConnectionTest.java`:

```java
package com.noteflix.pcm.infrastructure.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {
    
    @Test
    public void testOracleConnection() {
        DatabaseConfig config = DatabaseConfigFactory.createOracleConfig(
            "localhost", 1521, "PCMSERVICE", "pcm_user", "pcm_password"
        );
        
        EnhancedConnectionManager manager = EnhancedConnectionManager.getInstance();
        manager.initialize(config);
        
        assertTrue(manager.testConnection(), "Oracle connection should be successful");
        
        manager.close();
    }
    
    @Test
    public void testSQLiteConnection() {
        DatabaseConfig config = DatabaseConfig.builder()
            .type(DatabaseConfig.DatabaseType.SQLITE)
            .build();
        
        EnhancedConnectionManager manager = EnhancedConnectionManager.getInstance();
        manager.initialize(config);
        
        assertTrue(manager.testConnection(), "SQLite connection should be successful");
        
        manager.close();
    }
}
```

### 7.2 Migration Test
```java
package com.noteflix.pcm.infrastructure.database;

import org.junit.jupiter.api.Test;

public class MigrationTest {
    
    @Test
    public void testOracleMigrations() {
        DatabaseConfig config = DatabaseConfigFactory.createOracleConfig(
            "localhost", 1521, "PCMSERVICE", "pcm_user", "pcm_password"
        );
        
        EnhancedConnectionManager manager = EnhancedConnectionManager.getInstance();
        manager.initialize(config);
        
        OracleMigrationManager migrationManager = new OracleMigrationManager(manager);
        
        // Should not throw exceptions
        assertDoesNotThrow(() -> migrationManager.migrate());
        
        manager.close();
    }
}
```

## 8. Deployment & Operations

### 8.1 Oracle Database Setup
Script để setup Oracle database:

```sql
-- Create tablespace
CREATE TABLESPACE PCM_DATA
DATAFILE 'pcm_data.dbf' 
SIZE 100M AUTOEXTEND ON MAXSIZE 1G;

-- Create user
CREATE USER pcm_user IDENTIFIED BY pcm_password
DEFAULT TABLESPACE PCM_DATA
TEMPORARY TABLESPACE TEMP;

-- Grant permissions
GRANT CREATE SESSION TO pcm_user;
GRANT CREATE TABLE TO pcm_user;
GRANT CREATE SEQUENCE TO pcm_user;
GRANT CREATE TRIGGER TO pcm_user;
GRANT CREATE PROCEDURE TO pcm_user;
GRANT UNLIMITED TABLESPACE TO pcm_user;

-- Additional permissions for application
GRANT SELECT_CATALOG_ROLE TO pcm_user;
GRANT EXECUTE ON SYS.DBMS_LOCK TO pcm_user;

-- Verify setup
SELECT username, default_tablespace, temporary_tablespace 
FROM dba_users 
WHERE username = 'PCM_USER';
```

### 8.2 Production Configuration
File `database-production.properties`:

```properties
# Production Oracle Configuration
database.type=ORACLE

oracle.host=oracle-prod-server.company.com
oracle.port=1521
oracle.serviceName=PCMPROD
oracle.username=${ORACLE_USERNAME}
oracle.password=${ORACLE_PASSWORD}

# Production pool settings
pool.minimumIdle=10
pool.maximumPoolSize=50
pool.connectionTimeout=30000
pool.idleTimeout=300000
pool.maxLifetime=1800000

# Enable monitoring
pool.metricRegistry=true
pool.healthCheckRegistry=true
pool.leakDetectionThreshold=60000

# Oracle performance tuning
oracle.readTimeout=60000
oracle.connectTimeout=15000
oracle.statementCacheSize=50
oracle.executeBatch=100
```

### 8.3 Monitoring & Logging
Enhanced logging configuration cho Oracle:

```xml
<!-- logback-oracle.xml -->
<configuration>
    <appender name="ORACLE_LOG" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/pcm-oracle.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/pcm-oracle.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level [%logger{36}] - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.noteflix.pcm.infrastructure.database" level="DEBUG" additivity="false">
        <appender-ref ref="ORACLE_LOG" />
    </logger>
    
    <logger name="com.zaxxer.hikari" level="INFO" additivity="false">
        <appender-ref ref="ORACLE_LOG" />
    </logger>
    
    <logger name="oracle.jdbc" level="WARN" additivity="false">
        <appender-ref ref="ORACLE_LOG" />
    </logger>
</configuration>
```

## 9. Performance Optimization

### 9.1 Oracle-specific Optimizations
```java
// Oracle connection properties for performance
public void addOraclePerformanceProperties(HikariConfig config) {
    // Statement caching
    config.addDataSourceProperty("oracle.jdbc.implicitStatementCacheSize", "100");
    config.addDataSourceProperty("oracle.jdbc.explicitStatementCacheSize", "100");
    
    // Batch processing
    config.addDataSourceProperty("oracle.jdbc.defaultExecuteBatch", "100");
    config.addDataSourceProperty("oracle.jdbc.enableQueryResultCache", "true");
    
    // Network optimization
    config.addDataSourceProperty("oracle.net.RECV_TIMEOUT", "30000");
    config.addDataSourceProperty("oracle.net.SEND_TIMEOUT", "30000");
    config.addDataSourceProperty("oracle.jdbc.ReadTimeout", "60000");
    
    // LOB handling
    config.addDataSourceProperty("oracle.jdbc.defaultLobPrefetchSize", "4096");
    
    // Timezone
    config.addDataSourceProperty("oracle.jdbc.timezoneAsRegion", "false");
}
```

### 9.2 Query Optimization Examples
```java
// Optimized pagination for Oracle
public List<Conversation> findConversationsPaginated(int offset, int limit) {
    String sql = """
        SELECT * FROM (
            SELECT c.*, ROW_NUMBER() OVER (ORDER BY c.updated_at DESC) as rn
            FROM conversations c
        ) WHERE rn BETWEEN ? AND ?
        """;
        
    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
         
        stmt.setInt(1, offset + 1);
        stmt.setInt(2, offset + limit);
        
        // Implementation...
    }
}

// Batch insert optimization
public void createConversationsBatch(List<Conversation> conversations) {
    String sql = "INSERT INTO conversations (title, project_id) VALUES (?, ?)";
    
    try (Connection conn = connectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
         
        conn.setAutoCommit(false);
        
        for (Conversation conv : conversations) {
            stmt.setString(1, conv.getTitle());
            stmt.setLong(2, conv.getProjectId());
            stmt.addBatch();
        }
        
        stmt.executeBatch();
        conn.commit();
    }
}
```

## 10. Troubleshooting Guide

### 10.1 Common Oracle Connection Issues
```java
public class OracleTroubleshooter {
    
    public static void diagnoseConnection(DatabaseConfig config) {
        log.info("🔍 Diagnosing Oracle connection...");
        
        // Test 1: Basic connectivity
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            log.info("✅ Oracle JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            log.error("❌ Oracle JDBC driver not found in classpath");
            return;
        }
        
        // Test 2: Network connectivity
        String host = config.getHost();
        int port = config.getPort();
        
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            log.info("✅ Network connection to {}:{} successful", host, port);
        } catch (IOException e) {
            log.error("❌ Network connection failed to {}:{}", host, port);
            return;
        }
        
        // Test 3: Oracle listener
        String jdbcUrl = buildOracleUrl(config);
        try (Connection conn = DriverManager.getConnection(
                jdbcUrl, config.getUsername(), config.getPassword())) {
            log.info("✅ Oracle database connection successful");
            
            // Test 4: Basic query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT SYSDATE FROM DUAL")) {
                if (rs.next()) {
                    log.info("✅ Oracle query test successful: {}", rs.getTimestamp(1));
                }
            }
            
        } catch (SQLException e) {
            log.error("❌ Oracle database connection failed: {}", e.getMessage());
            suggestSolutions(e);
        }
    }
    
    private static void suggestSolutions(SQLException e) {
        String message = e.getMessage().toLowerCase();
        
        if (message.contains("ora-12541") || message.contains("no listener")) {
            log.warn("💡 Solution: Check if Oracle listener is running and configured correctly");
        } else if (message.contains("ora-12514") || message.contains("service name")) {
            log.warn("💡 Solution: Verify service name or SID in connection string");
        } else if (message.contains("ora-01017") || message.contains("invalid username")) {
            log.warn("💡 Solution: Check username and password credentials");
        } else if (message.contains("ora-28000") || message.contains("account is locked")) {
            log.warn("💡 Solution: Contact DBA to unlock the Oracle user account");
        }
    }
}
```

### 10.2 Performance Monitoring
```java
public class DatabasePerformanceMonitor {
    
    public void monitorConnectionPool() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) dataSource;
            HikariPoolMXBean poolBean = hikari.getHikariPoolMXBean();
            
            log.info("📊 Connection Pool Stats:");
            log.info("   Active Connections: {}", poolBean.getActiveConnections());
            log.info("   Idle Connections: {}", poolBean.getIdleConnections());
            log.info("   Total Connections: {}", poolBean.getTotalConnections());
            log.info("   Threads Waiting: {}", poolBean.getThreadsAwaitingConnection());
        }
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void logPoolStatistics() {
        monitorConnectionPool();
    }
}
```

## 11. Migration Path từ SQLite sang Oracle

### 11.1 Data Migration Script
```java
public class SQLiteToOracleMigrator {
    
    public void migrateData() {
        EnhancedConnectionManager sqliteManager = createSQLiteManager();
        EnhancedConnectionManager oracleManager = createOracleManager();
        
        try {
            migrateProjects(sqliteManager, oracleManager);
            migrateConversations(sqliteManager, oracleManager);
            migrateMessages(sqliteManager, oracleManager);
            
            log.info("✅ Data migration from SQLite to Oracle completed successfully");
            
        } catch (Exception e) {
            log.error("❌ Data migration failed", e);
            throw new RuntimeException("Migration failed", e);
        }
    }
    
    private void migrateProjects(EnhancedConnectionManager source, 
                                EnhancedConnectionManager target) throws SQLException {
        String selectSql = "SELECT * FROM projects ORDER BY id";
        String insertSql = "INSERT INTO projects (name, description, project_type, project_status, created_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection sourceConn = source.getConnection();
             Connection targetConn = target.getConnection();
             PreparedStatement selectStmt = sourceConn.prepareStatement(selectSql);
             PreparedStatement insertStmt = targetConn.prepareStatement(insertSql)) {
             
            ResultSet rs = selectStmt.executeQuery();
            int count = 0;
            
            while (rs.next()) {
                insertStmt.setString(1, rs.getString("name"));
                insertStmt.setString(2, rs.getString("description"));
                insertStmt.setString(3, rs.getString("project_type"));
                insertStmt.setString(4, rs.getString("project_status"));
                insertStmt.setTimestamp(5, rs.getTimestamp("created_at"));
                
                insertStmt.executeUpdate();
                count++;
            }
            
            log.info("Migrated {} projects", count);
        }
    }
}
```

## Tổng kết

Tài liệu này cung cấp roadmap hoàn chỉnh để tích hợp Oracle Database vào PCM Desktop Application. Các thành phần chính bao gồm:

1. **Database Configuration**: Hỗ trợ multi-database với configuration linh hoạt
2. **Connection Management**: HikariCP connection pooling cho performance tối ưu
3. **Migration System**: Oracle-specific migration với version control
4. **Data Access Layer**: DAO pattern với Oracle optimizations
5. **Testing & Monitoring**: Comprehensive testing và performance monitoring
6. **Deployment Guide**: Production-ready deployment configurations

### Next Steps:
1. Implement từng component theo thứ tự priority
2. Setup Oracle development environment
3. Create comprehensive test suite
4. Performance testing và optimization
5. Production deployment planning

Tài liệu này có thể được extend để support thêm các database systems khác (PostgreSQL, MySQL, SQL Server) trong tương lai.