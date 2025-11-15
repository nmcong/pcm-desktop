# PCM Desktop - Troubleshooting Guide

This directory contains detailed documentation for common issues and their solutions in PCM Desktop.

## 📁 Directory Structure

```
troubleshooting/
├── README.md                           # This file
├── runtime/                            # Runtime and startup issues
│   └── javafx-application-startup-issue.md
├── build/                              # Build and compilation issues
├── database/                           # Database-related issues
└── integration/                        # Integration and API issues
```

## 🔍 Issue Categories

### Runtime Issues (`runtime/`)

Issues that occur when running the application:

- **[JavaFX Application Startup Issue](runtime/javafx-application-startup-issue.md)** ⭐
  - Platform: macOS/Linux
  - Error: "Missing JavaFX application class"
  - Root Cause: Duplicate library JARs
  - Status: ✅ Resolved

### Build Issues (`build/`)

Coming soon:
- Compilation errors
- Maven build failures
- Resource copying issues

### Database Issues (`database/`)

Coming soon:
- SQLite connection problems
- Migration failures
- Data integrity issues

### Integration Issues (`integration/`)

Coming soon:
- API connection failures
- LLM integration problems
- RAG system issues

## 🆘 Quick Issue Resolution

### Common Error Messages

| Error Message | Category | Document |
|--------------|----------|----------|
| "Missing JavaFX application class" | Runtime | [javafx-application-startup-issue.md](runtime/javafx-application-startup-issue.md) |
| "Java 21 required" | Build | TBD |
| "Database locked" | Database | TBD |
| "OpenAI API key not found" | Integration | TBD |

## 🔧 General Troubleshooting Steps

### 1. Clean Build

```bash
# Clean all build artifacts
./scripts/build.sh --clean

# Or use Maven
mvn clean
```

### 2. Verify Java Version

```bash
# Check Java version
java -version

# Should show: openjdk version "21.0.9" or similar
```

### 3. Check Library Dependencies

```bash
# List all JARs in lib directories
ls -la lib/javafx/
ls -la lib/others/
ls -la lib/rag/

# Check for duplicates
ls lib/others/*.jar | sed 's/-[0-9].*//' | sort | uniq -d
```

### 4. Review Logs

```bash
# Check application logs
tail -f logs/pcm-desktop.log

# Check for errors
grep ERROR logs/pcm-desktop.log
```

### 5. Verify Environment

```bash
# Check environment variables
echo $JAVA_HOME
echo $PATH

# Verify JAVA_HOME points to Java 21
$JAVA_HOME/bin/java -version
```

## 📝 Reporting New Issues

When reporting a new issue, please include:

1. **Environment Information**:
   - OS and version
   - Java version
   - Shell (bash, zsh, etc.)

2. **Error Messages**:
   - Full error message
   - Stack trace if available
   - Log excerpts

3. **Steps to Reproduce**:
   - Commands executed
   - Configuration changes
   - Expected vs actual behavior

4. **Attempted Solutions**:
   - What you've tried
   - Results of each attempt

## 🔗 Related Documentation

- [Build Instructions](../../README.md#build)
- [Setup Guide](../planning/IMPLEMENTATION_CHECKLIST.md)
- [Architecture Overview](../architecture/PCM_CONCEPT.md)

## 🏷️ Tags Index

- `#runtime` - Runtime and execution issues
- `#build` - Build and compilation problems
- `#database` - Database-related issues
- `#integration` - API and service integration
- `#javafx` - JavaFX-specific problems
- `#macos` - macOS-specific issues
- `#linux` - Linux-specific issues
- `#windows` - Windows-specific issues
- `#dependencies` - Library and dependency problems
- `#configuration` - Configuration issues

---

**Note**: This troubleshooting guide is continuously updated. If you encounter an issue not covered here, please document it after resolution.

