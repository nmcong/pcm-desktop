# PCM Desktop - Quick Troubleshooting Reference

> 🚀 Fast solutions for common issues

## 🔥 Critical Issues (Start Here!)

### ❌ "Missing JavaFX application class" Error (macOS/Linux)

**Quick Fix**:

```bash
# Remove duplicate libraries
cd /Users/nguyencong/Workspace/pcm-desktop
rm -f lib/others/jackson-databind-2.18.2.jar
rm -f lib/others/jackson-core-2.18.2.jar
rm -f lib/others/logback-classic-1.5.12.jar
rm -f lib/others/slf4j-api-2.0.16.jar
rm -f lib/others/sqlite-jdbc-3.47.1.0.jar

# Run application
./scripts/run.sh
```

**Full Details**: [javafx-application-startup-issue.md](runtime/javafx-application-startup-issue.md)

---

### ❌ "Java 21 required" Error

**Quick Fix**:

```bash
# On macOS
brew install --cask temurin21

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

---

### ❌ "JavaFX libraries not found"

**Quick Fix**:

```bash
# Run setup script
./scripts/setup.sh
```

---

## 🔍 Diagnostic Commands

### Check Environment

```bash
# Java version (should be 21)
java -version

# JAVA_HOME
echo $JAVA_HOME

# Application status
ps aux | grep PCMApplication
```

### Check Libraries

```bash
# List JavaFX libraries
ls -la lib/javafx/

# Check for duplicate JARs
ls lib/others/*.jar | sed 's/-[0-9].*//' | sort | uniq -d

# Count libraries
ls lib/others/*.jar | wc -l
```

### Check Application

```bash
# View real-time logs
tail -f logs/pcm-desktop.log

# Search for errors
grep ERROR logs/pcm-desktop.log

# Check if compiled
ls -la out/com/noteflix/pcm/PCMApplication.class
```

---

## 🛠️ Common Commands

### Build & Run

```bash
# Clean build
./scripts/build.sh --clean

# Standard build
./scripts/build.sh

# Run application
./scripts/run.sh

# Run with text component
./scripts/run.sh --text
```

### Using Maven

```bash
# Clean and compile
mvn clean compile

# Run application
mvn exec:java

# Run tests
mvn test
```

---

## 🚨 Emergency Reset

If everything fails, reset completely:

```bash
# 1. Clean all build artifacts
rm -rf out/
rm -rf target/
rm -rf lib/

# 2. Re-download libraries
./scripts/setup.sh

# 3. Clean build
./scripts/build.sh --clean

# 4. Run application
./scripts/run.sh
```

---

## 📞 Get Help

If the issue persists:

1. Check detailed documentation in `docs/troubleshooting/`
2. Review logs in `logs/pcm-desktop.log`
3. Check git status: `git status`
4. Verify you're on the correct branch: `git branch`

---

## 🏷️ Issue Quick Links

| Issue                | Location                                                                                   | Status         |
|----------------------|--------------------------------------------------------------------------------------------|----------------|
| JavaFX Startup Error | [runtime/javafx-application-startup-issue.md](runtime/javafx-application-startup-issue.md) | ✅ Resolved     |
| Build Issues         | `build/`                                                                                   | 📝 Coming Soon |
| Database Issues      | `database/`                                                                                | 📝 Coming Soon |
| Integration Issues   | `integration/`                                                                             | 📝 Coming Soon |

---

**Last Updated**: 2025-11-15  
**Quick Access**: Save this file to your bookmarks!

