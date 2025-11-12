# Development Documentation

This section contains guidelines, standards, and processes for developing PCM Desktop.

## 📋 Development Guidelines

### 🏗️ [Project Structure](project-structure.md)
Overview of the codebase organization and module structure.

**Topics Covered:**
- Maven module organization
- Package hierarchy
- Source code organization
- Resource management
- Build configuration

### 📏 [Coding Standards](coding-standards.md)
Code style guidelines and best practices.

**Topics Covered:**
- Java coding conventions
- FXML formatting standards
- CSS/styling guidelines
- Naming conventions
- Documentation standards

### 🧪 [Testing Guide](testing-guide.md)
Comprehensive testing strategies and practices.

**Topics Covered:**
- Unit testing with JUnit 5
- UI testing with TestFX
- Integration testing patterns
- Mock strategies
- Test data management

### 🚀 [Release Process](release-process.md)
Version management and release procedures.

**Topics Covered:**
- Version numbering scheme
- Release branching strategy
- Build and packaging
- Quality gates
- Deployment procedures

## 🛠️ Development Environment

### Prerequisites
- **Java 17+** - Latest LTS version required
- **Maven 3.8+** - Build and dependency management
- **IntelliJ IDEA 2023.3+** - Recommended IDE
- **Git** - Version control

### Quick Setup
```bash
# Clone repository
git clone <repository-url>
cd pcm-desktop

# Setup libraries
./scripts/setup-libraries.sh

# Build project
mvn clean install

# Run application
mvn javafx:run
```

## 🏗️ Architecture Overview

### Layered Architecture
```
┌─────────────────────────────────────────────────────────┐
│                   UI Layer                              │
│  Controllers, Views, Components                        │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│                Service Layer                            │
│  Business Logic, Validation, Orchestration             │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│                Repository Layer                         │
│  Data Access, Caching, Query Building                  │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│                   Data Layer                            │
│  Database, File System, External APIs                  │
└─────────────────────────────────────────────────────────┘
```

### Package Structure
```
com.noteflix.pcm/
├── 📂 ui/              # User Interface Layer
│   ├── controllers/    # FXML Controllers
│   ├── components/     # Custom UI Components
│   ├── views/          # FXML View Definitions
│   └── utils/          # UI Utilities
├── 📂 service/         # Service Layer
│   ├── impl/           # Service Implementations
│   └── interfaces/     # Service Contracts
├── 📂 data/            # Data Access Layer
│   ├── repository/     # Repository Interfaces
│   ├── impl/           # Repository Implementations
│   ├── entity/         # Data Entities
│   └── dao/            # Data Access Objects
├── 📂 core/            # Core Business Logic
│   ├── domain/         # Domain Models
│   ├── exceptions/     # Custom Exceptions
│   └── utils/          # Core Utilities
├── 📂 config/          # Configuration
│   ├── database/       # Database Configuration
│   ├── security/       # Security Configuration
│   └── properties/     # Application Properties
└── 📂 integration/     # External Integrations
    ├── llm/            # LLM Integration
    ├── database/       # Database Integration
    └── sso/            # SSO Integration
```

## 🔄 Development Workflow

### 1. **Feature Development Process**
```mermaid
graph LR
    A[Create Feature Branch] --> B[Implement Feature]
    B --> C[Write Tests]
    C --> D[Code Review]
    D --> E[Integration Testing]
    E --> F[Merge to Main]
    F --> G[Deploy to Test]
```

### 2. **Git Workflow**
```bash
# Create feature branch
git checkout -b feature/new-ai-integration
git push -u origin feature/new-ai-integration

# Regular development
git add .
git commit -m "feat: add new AI integration endpoint"
git push

# Before merging
git checkout main
git pull origin main
git checkout feature/new-ai-integration
git rebase main
git push --force-with-lease

# Create pull request
gh pr create --title "Add new AI integration" --body "Description of changes"
```

### 3. **Commit Message Format**
```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples:**
```bash
feat(ai): add Claude API integration
fix(database): resolve connection pool timeout
docs(setup): update installation instructions
style(ui): format FXML files consistently
refactor(service): extract common validation logic
test(repository): add integration tests for ProjectRepository
chore(build): update Maven dependencies
```

## 🧪 Testing Strategy

### 1. **Testing Pyramid**
```
    ┌─────────────────┐
    │   E2E Tests     │  ← Few, Slow, High Confidence
    └─────────────────┘
  ┌───────────────────────┐
  │  Integration Tests    │  ← Some, Medium Speed
  └───────────────────────┘
┌─────────────────────────────┐
│      Unit Tests             │  ← Many, Fast, Focused
└─────────────────────────────┘
```

### 2. **Test Categories**
```java
// Unit Tests - Fast, Isolated
@Test
public void shouldCalculateProjectStatistics() {
    // Test business logic in isolation
}

// Integration Tests - Medium Speed, Real Dependencies
@Test
public void shouldPersistProjectToDatabase() {
    // Test with real database
}

// UI Tests - Slow, Full System
@Test
public void shouldDisplayProjectListWhenLoaded() {
    // Test complete user workflows
}
```

### 3. **Test Naming Conventions**
```java
// Pattern: should[ExpectedBehavior]When[StateUnderTest]
@Test
public void shouldReturnEmptyListWhenNoDatabaseObjectsExist() { }

@Test
public void shouldThrowExceptionWhenProjectNameIsInvalid() { }

@Test
public void shouldUpdateProjectStatusWhenValidDataProvided() { }
```

## 📊 Quality Metrics

### Code Quality Gates
- **Code Coverage**: Minimum 80% line coverage
- **Cyclomatic Complexity**: Maximum 10 per method
- **Method Length**: Maximum 50 lines
- **Class Size**: Maximum 500 lines
- **Package Coupling**: Minimize circular dependencies

### Performance Targets
- **Application Startup**: < 3 seconds
- **UI Response Time**: < 100ms for user interactions
- **Database Queries**: < 50ms for common operations
- **Memory Usage**: < 512MB for typical workloads

### Code Quality Tools
```xml
<!-- pom.xml - Quality plugins -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>

<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
    </configuration>
</plugin>

<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
</plugin>
```

## 🔧 Build Configuration

### Maven Profiles
```xml
<!-- Development Profile -->
<profile>
    <id>dev</id>
    <activation>
        <activeByDefault>true</activeByDefault>
    </activation>
    <properties>
        <log.level>DEBUG</log.level>
        <database.url>jdbc:sqlite:pcm-dev.db</database.url>
    </properties>
</profile>

<!-- Production Profile -->
<profile>
    <id>prod</id>
    <properties>
        <log.level>INFO</log.level>
        <database.url>jdbc:sqlite:pcm.db</database.url>
    </properties>
</profile>

<!-- Testing Profile -->
<profile>
    <id>test</id>
    <properties>
        <log.level>WARN</log.level>
        <database.url>jdbc:h2:mem:testdb</database.url>
    </properties>
</profile>
```

### Build Scripts
```bash
#!/bin/bash
# build.sh - Complete build script

set -e

echo "🏗️ Building PCM Desktop..."

# Clean
echo "🧹 Cleaning previous builds..."
mvn clean

# Validate
echo "✅ Validating dependencies..."
mvn validate

# Compile
echo "🔨 Compiling sources..."
mvn compile

# Test
echo "🧪 Running tests..."
mvn test

# Package
echo "📦 Creating package..."
mvn package

# Quality checks
echo "🔍 Running quality checks..."
mvn spotbugs:check
mvn jacoco:check

echo "✅ Build completed successfully!"
```

## 📝 Documentation Standards

### Code Documentation
```java
/**
 * Service for managing projects within the PCM system.
 * 
 * <p>This service provides CRUD operations for projects and handles
 * business logic related to project management, including validation,
 * status transitions, and dependency management.
 * 
 * @author PCM Development Team
 * @since 1.0.0
 */
@Service
public class ProjectService {
    
    /**
     * Creates a new project in the system.
     * 
     * @param project the project to create, must not be null
     * @return the ID of the created project
     * @throws ValidationException if project data is invalid
     * @throws DuplicateProjectException if project name already exists
     */
    public Long createProject(@NonNull Project project) {
        // Implementation...
    }
}
```

### README Structure
```markdown
# Component/Module Name

Brief description of the component's purpose.

## Usage

Basic usage examples.

## API Reference

Detailed API documentation.

## Configuration

Configuration options and examples.

## Testing

How to test this component.

## Contributing

Guidelines for contributing to this component.
```

## 🚀 Performance Optimization

### Profiling Setup
```bash
# JProfiler
java -agentpath:/path/to/jprofiler/bin/linux-x64/libjprofilerti.so=port=8849 \
     -jar pcm-desktop.jar

# JConsole
jconsole [process-id]

# Flight Recorder
java -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=recording.jfr \
     -jar pcm-desktop.jar
```

### Memory Optimization
```java
// Weak references for caches
private final Map<String, WeakReference<Image>> imageCache = new WeakHashMap<>();

// Object pooling for frequent allocations
private final ObjectPool<StringBuilder> stringBuilderPool = new GenericObjectPool<>(...);

// Lazy initialization for expensive resources
private final Lazy<DatabaseConnection> connection = Lazy.of(() -> createConnection());
```

## 🔐 Security Guidelines

### Secure Coding Practices
```java
// Input validation
public void processUserInput(@NotNull String input) {
    if (input.trim().isEmpty()) {
        throw new ValidationException("Input cannot be empty");
    }
    
    // Sanitize input
    String sanitized = input.replaceAll("[^a-zA-Z0-9\\s]", "");
    
    // Process sanitized input
}

// Secret management
public class ApiKeyManager {
    private static final String API_KEY = System.getenv("API_KEY");
    
    static {
        if (API_KEY == null || API_KEY.trim().isEmpty()) {
            throw new IllegalStateException("API_KEY environment variable not set");
        }
    }
}

// SQL injection prevention
public List<Project> findProjectsByName(String name) {
    String sql = "SELECT * FROM projects WHERE name LIKE ?";
    return jdbcTemplate.query(sql, new Object[]{"%" + name + "%"}, projectRowMapper);
}
```

## 📚 Learning Resources

### Recommended Reading
- **Clean Code** by Robert C. Martin
- **Effective Java** by Joshua Bloch
- **JavaFX in Action** by Simon Morris
- **Building Maintainable Software** by Joost Visser

### Online Resources
- [JavaFX Documentation](https://openjfx.io/)
- [AtlantaFX Documentation](https://mkpaz.github.io/atlantafx/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

### Training Materials
- Internal code review guidelines
- Architecture decision records
- Design pattern examples
- Performance optimization techniques

---

For specific development topics, select the appropriate document from the sections above.

*Last updated: November 12, 2024*