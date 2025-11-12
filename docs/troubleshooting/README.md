# Troubleshooting Guide

This section contains solutions to common problems and debugging techniques for PCM Desktop.

## 🚨 Quick Fixes

### Most Common Issues

| Problem | Quick Solution | Reference |
|---------|---------------|-----------|
| **JavaFX runtime missing** | Set VM options: `--module-path lib/javafx --add-modules javafx.controls,javafx.fxml` | [Setup Guide](../setup/run-configuration.md) |
| **Library not found** | Invalidate IntelliJ caches and restart | [IntelliJ Setup](../setup/intellij-setup.md) |
| **Can't resolve symbol 'log'** | Enable annotation processing for Lombok | [IntelliJ Setup](../setup/intellij-setup.md) |
| **Database locked** | Close all connections and restart app | [Database Issues](#database-issues) |
| **API call failures** | Check API keys and network connection | [API Issues](#api-integration-issues) |

## 📋 Troubleshooting Sections

### 🔧 [Common Issues](common-issues.md)
Frequently encountered problems and their solutions.

**Topics Covered:**
- JavaFX runtime and module issues
- Library and dependency problems
- Database connection issues
- UI rendering problems
- Performance issues

### ⚡ [Quick Fixes](quick-fixes.md)
Rapid solutions for immediate problems.

**Topics Covered:**
- One-command fixes
- Environment variable solutions
- Cache clearing procedures
- Service restart commands
- Configuration resets

### 🐛 [Debugging Guide](debugging-guide.md)
Comprehensive debugging strategies and tools.

**Topics Covered:**
- Debugging setup in IntelliJ
- Logging configuration
- Performance profiling
- Memory leak detection
- Database query optimization

### ❓ [FAQ](faq.md)
Frequently asked questions and answers.

**Topics Covered:**
- General usage questions
- Technical implementation details
- Feature requests and roadmap
- System requirements clarification

## 🎯 Problem Categories

### 1. **Setup and Installation Issues**

#### Library Recognition Problems
```bash
# Quick fix: Clear IntelliJ caches
# File → Invalidate Caches → Select all options → Invalidate and Restart
```

#### JavaFX Module Issues
```bash
# VM Options for IntelliJ Run Configuration:
--module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
```

#### Lombok Not Working
```bash
# 1. Install Lombok plugin in IntelliJ
# 2. Enable annotation processing:
#    Settings → Build → Compiler → Annotation Processors → Enable annotation processing
```

### 2. **Runtime Issues**

#### Application Won't Start
```java
// Check Java version
java -version
// Should show Java 17 or later

// Verify libraries
ls -la lib/javafx/    // Should show 8 JavaFX JAR files
ls -la lib/others/    // Should show 15+ library JAR files
```

#### Memory Issues
```bash
# Increase heap size
java -Xmx2G -jar pcm-desktop.jar

# Monitor memory usage
jconsole [process-id]
```

#### Performance Problems
```java
// Enable performance logging
-Djavafx.pulseLogger=true
-Dprism.verbose=true
-Dprism.debug=true
```

### 3. **Database Issues**

#### Connection Failures
```java
// Check SQLite database file
File dbFile = new File("pcm.db");
System.out.println("DB exists: " + dbFile.exists());
System.out.println("DB readable: " + dbFile.canRead());
System.out.println("DB writable: " + dbFile.canWrite());
```

#### Schema Issues
```sql
-- Check database version
SELECT version FROM schema_info;

-- Check table structure
.schema projects

-- Verify data integrity
PRAGMA integrity_check;
```

#### Lock Issues
```bash
# Find processes using database
lsof pcm.db

# Force unlock (use with caution)
rm pcm.db-journal
```

### 4. **API Integration Issues**

#### Authentication Failures
```java
// Verify API keys
String apiKey = System.getenv("OPENAI_API_KEY");
if (apiKey == null || apiKey.isEmpty()) {
    System.err.println("OPENAI_API_KEY not set");
}

// Test connection
curl -H "Authorization: Bearer $OPENAI_API_KEY" \
     https://api.openai.com/v1/models
```

#### Network Issues
```java
// Check connectivity
InetAddress.getByName("api.openai.com").isReachable(5000);

// Verify proxy settings
System.getProperty("http.proxyHost");
System.getProperty("http.proxyPort");
```

#### Rate Limiting
```java
// Implement exponential backoff
public class RetryUtils {
    public static void retryWithBackoff(Runnable action, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                action.run();
                return;
            } catch (Exception e) {
                if (i == maxRetries - 1) throw e;
                try {
                    Thread.sleep((long) Math.pow(2, i) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
    }
}
```

### 5. **UI Issues**

#### Theme Not Applied
```java
// Force theme reload
Application.setUserAgentStylesheet(null);
Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

// Check CSS loading
scene.getStylesheets().forEach(System.out::println);
```

#### Layout Problems
```java
// Debug layout
Platform.runLater(() -> {
    Scene scene = primaryStage.getScene();
    scene.snapshot(null); // Force layout calculation
});
```

#### Icon Loading Issues
```java
// Verify icon fonts
FontIcon icon = new FontIcon("fas-home");
if (icon.getIconLiteral() == null) {
    System.err.println("Icon font not loaded");
}
```

## 🔍 Diagnostic Tools

### 1. **System Information**
```java
public class SystemDiagnostics {
    
    public static void printSystemInfo() {
        System.out.println("=== System Information ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("JavaFX Version: " + System.getProperty("javafx.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
        System.out.println("Available Memory: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
        System.out.println("Available Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        System.out.println("User Home: " + System.getProperty("user.home"));
        System.out.println("Temp Directory: " + System.getProperty("java.io.tmpdir"));
        
        // Check ClassPath
        System.out.println("\n=== ClassPath ===");
        String classPath = System.getProperty("java.class.path");
        String[] paths = classPath.split(System.getProperty("path.separator"));
        for (String path : paths) {
            System.out.println("  " + path);
        }
        
        // Check Module Path
        System.out.println("\n=== Module Path ===");
        String modulePath = System.getProperty("jdk.module.path");
        if (modulePath != null) {
            String[] modules = modulePath.split(System.getProperty("path.separator"));
            for (String module : modules) {
                System.out.println("  " + module);
            }
        }
    }
}
```

### 2. **Library Verification**
```bash
#!/bin/bash
# verify-environment.sh

echo "=== PCM Desktop Environment Verification ==="

# Check Java
echo "Java Version:"
java -version
echo ""

# Check JavaFX libraries
echo "JavaFX Libraries:"
if [ -d "lib/javafx" ]; then
    ls -la lib/javafx/ | grep -c "\.jar"
    echo "JavaFX JAR files found"
else
    echo "❌ JavaFX directory not found"
fi
echo ""

# Check other libraries
echo "Other Libraries:"
if [ -d "lib/others" ]; then
    ls -la lib/others/ | grep -c "\.jar"
    echo "Other JAR files found"
else
    echo "❌ Others directory not found"
fi
echo ""

# Check database
echo "Database:"
if [ -f "pcm.db" ]; then
    echo "✅ Database file exists"
    sqlite3 pcm.db "SELECT COUNT(*) FROM sqlite_master WHERE type='table';" 2>/dev/null || echo "❌ Database corrupted"
else
    echo "⚠️ Database file not found (will be created on first run)"
fi
echo ""

# Check configuration
echo "Configuration:"
if [ -d "config" ]; then
    echo "✅ Config directory exists"
else
    echo "⚠️ Config directory not found"
fi
echo ""

# Check logs
echo "Logs:"
if [ -d "logs" ]; then
    echo "✅ Logs directory exists"
    echo "Recent log files:"
    ls -lt logs/ | head -5
else
    echo "⚠️ Logs directory not found"
fi
```

### 3. **Health Check Service**
```java
@Service
public class HealthCheckService {
    
    public HealthStatus checkOverallHealth() {
        HealthStatus status = new HealthStatus();
        
        // Check database
        status.setDatabaseStatus(checkDatabaseHealth());
        
        // Check API connectivity
        status.setApiStatus(checkApiHealth());
        
        // Check file system
        status.setFileSystemStatus(checkFileSystemHealth());
        
        // Check memory
        status.setMemoryStatus(checkMemoryHealth());
        
        return status;
    }
    
    private DatabaseStatus checkDatabaseHealth() {
        try {
            ConnectionManager.getInstance().testConnection();
            return DatabaseStatus.HEALTHY;
        } catch (Exception e) {
            return DatabaseStatus.UNHEALTHY;
        }
    }
    
    private ApiStatus checkApiHealth() {
        // Test API connectivity
        return ApiStatus.HEALTHY;
    }
    
    private FileSystemStatus checkFileSystemHealth() {
        // Check file permissions and disk space
        return FileSystemStatus.HEALTHY;
    }
    
    private MemoryStatus checkMemoryHealth() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        
        double memoryUsage = (double) usedMemory / maxMemory;
        
        if (memoryUsage > 0.9) {
            return MemoryStatus.CRITICAL;
        } else if (memoryUsage > 0.7) {
            return MemoryStatus.WARNING;
        } else {
            return MemoryStatus.HEALTHY;
        }
    }
}
```

## 🛠️ Debug Configuration

### 1. **Logging Setup**
```properties
# logback.xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/pcm-desktop.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/pcm-desktop.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Application loggers -->
    <logger name="com.noteflix.pcm" level="DEBUG"/>
    <logger name="com.noteflix.pcm.data" level="TRACE"/>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### 2. **IntelliJ Debug Configuration**
```java
// VM Options for debugging
-Xdebug
-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005
-Djavafx.pulseLogger=true
-Dprism.debug=true
-Dcom.sun.javafx.isLoggingEnabled=true
```

## 📞 Getting Help

### 1. **Information to Provide**
When reporting issues, please include:

```bash
# Run this command and include output
java -version
./verify-environment.sh

# Include relevant log files
cat logs/pcm-desktop.log | tail -50

# Include stack trace if available
# Screenshot of error dialog if applicable
```

### 2. **Support Channels**
- Check [FAQ](faq.md) first
- Review [Common Issues](common-issues.md)
- Search existing documentation
- Create issue with detailed information

### 3. **Best Practices for Issue Reporting**
- Provide clear steps to reproduce
- Include system information
- Attach relevant log files
- Specify expected vs actual behavior
- Include screenshots if UI-related

---

For specific troubleshooting topics, select the appropriate document from the sections above.

*Last updated: November 12, 2024*