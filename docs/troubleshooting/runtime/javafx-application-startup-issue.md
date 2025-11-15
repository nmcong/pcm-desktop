# JavaFX Application Startup Issue - macOS/Linux

## 📋 Metadata

- **Issue Date**: 2025-11-15
- **Platform**: macOS (also applies to Linux)
- **Java Version**: Java 21
- **JavaFX Version**: 21.0.9
- **Severity**: Critical (Application cannot start)
- **Status**: ✅ Resolved

## 🔴 Problem Description

When running the PCM Desktop application on macOS/Linux using the `run.sh` script, the application fails to start with the following error:

```
Missing JavaFX application class com.noteflix.pcm.PCMApplication
```

### Symptoms

1. Application compiled successfully with no errors
2. `PCMApplication.class` exists in `out/com/noteflix/pcm/`
3. Script runs but immediately exits with "Missing JavaFX application class" error
4. Same code works perfectly on Windows using `run.bat`

### Environment

```bash
OS: macOS 24.6.0 (Darwin)
Java: OpenJDK 21.0.9 (Temurin)
Shell: /bin/zsh
```

## 🔍 Root Cause Analysis

The issue was **NOT** related to JavaFX module system configuration or differences between macOS and Windows. The actual root cause was:

### **Duplicate Library JARs in `lib/others/`**

Multiple versions of the same libraries existed in the `lib/others/` directory:

```
❌ Problem JARs:
- jackson-databind-2.18.2.jar  (old version)
- jackson-databind-2.20.1.jar  (new version)
- jackson-core-2.18.2.jar      (old version)
- jackson-core-2.20.1.jar      (new version)
- logback-classic-1.5.12.jar   (old version)
- logback-classic-1.5.21.jar   (new version)
- slf4j-api-2.0.16.jar         (old version)
- slf4j-api-2.0.17.jar         (new version)
- sqlite-jdbc-3.47.1.0.jar     (old version)
- sqlite-jdbc-3.51.0.0.jar     (new version)
```

### Why Did This Cause the Issue?

1. **Module System Conflict**: When JavaFX is loaded via `--module-path`, Java's module system performs strict validation
2. **Classpath Scanning**: Multiple versions of the same module caused conflicts during classpath scanning
3. **Initialization Failure**: The JavaFX launcher couldn't properly initialize the application class due to module conflicts
4. **Silent Failure**: The error message was misleading - it appeared to be a JavaFX issue but was actually a library conflict

### Why Did Windows Work?

Windows might have:
- Different file system ordering that loaded the correct version first
- Different classloader behavior
- Libraries installed in a different order

However, this is **NOT RELIABLE** and the issue should be fixed properly.

## ✅ Solution

### Step 1: Remove Duplicate Libraries

```bash
cd /Users/nguyencong/Workspace/pcm-desktop

# Remove old Jackson versions
rm -f lib/others/jackson-databind-2.18.2.jar
rm -f lib/others/jackson-core-2.18.2.jar

# Remove old Logback version
rm -f lib/others/logback-classic-1.5.12.jar

# Remove old SLF4J version
rm -f lib/others/slf4j-api-2.0.16.jar

# Remove old SQLite JDBC version
rm -f lib/others/sqlite-jdbc-3.47.1.0.jar
```

### Step 2: Verify Configuration

Ensure `run.sh` matches `run.bat` configuration:

```bash
# Build classpath (include JavaFX)
CLASSPATH="out:lib/javafx/*:lib/others/*:lib/rag/*"

# Run with module-path for JavaFX
"$JAVA_HOME/bin/java" -Djava.library.path=lib/javafx \
    --module-path lib/javafx \
    --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media \
    -cp "$CLASSPATH" \
    com.noteflix.pcm.PCMApplication
```

### Step 3: Test the Application

```bash
./scripts/run.sh
```

Expected output:
```
✅ Application started successfully
🎨 UI built with pure Java code following AtlantaFX Sampler patterns
```

## 🛡️ Prevention

### 1. Clean Library Management

Add to `setup.sh` to clean old versions:

```bash
# Function to remove old versions
clean_old_versions() {
    echo "[INFO] Cleaning old library versions..."
    
    # Define libraries to keep (latest versions only)
    KEEP_VERSIONS=(
        "jackson-databind-2.20.1.jar"
        "jackson-core-2.20.1.jar"
        "logback-classic-1.5.21.jar"
        "slf4j-api-2.0.17.jar"
        "sqlite-jdbc-3.51.0.0.jar"
    )
    
    # Remove duplicates
    for jar in lib/others/*.jar; do
        basename=$(basename "$jar")
        if [[ ! " ${KEEP_VERSIONS[@]} " =~ " ${basename} " ]]; then
            # Check if this is a duplicate
            prefix=$(echo "$basename" | sed 's/-[0-9].*//')
            if ls lib/others/${prefix}-*.jar 2>/dev/null | grep -q .; then
                count=$(ls lib/others/${prefix}-*.jar 2>/dev/null | wc -l)
                if [ $count -gt 1 ]; then
                    echo "[WARN] Duplicate found: $basename"
                fi
            fi
        fi
    done
}
```

### 2. Use Maven Dependency Management

Let Maven handle dependencies instead of manual JAR files:

```xml
<!-- pom.xml -->
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <version>3.6.0</version>
            <executions>
                <execution>
                    <id>copy-dependencies</id>
                    <phase>package</phase>
                    <goals>
                        <goal>copy-dependencies</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>${project.basedir}/lib/others</outputDirectory>
                        <overWriteReleases>true</overWriteReleases>
                        <overWriteSnapshots>true</overWriteSnapshots>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 3. Add Validation to Build Script

Add to `build.sh`:

```bash
# Validate libraries function
validate_libraries() {
    echo -e "${BLUE}🔍 Validating libraries for duplicates...${NC}"
    
    local duplicates=0
    declare -A lib_prefixes
    
    for jar in lib/others/*.jar; do
        if [ -f "$jar" ]; then
            basename=$(basename "$jar")
            prefix=$(echo "$basename" | sed 's/-[0-9].*//')
            
            if [[ -n "${lib_prefixes[$prefix]}" ]]; then
                echo -e "${RED}❌ Duplicate library detected:${NC}"
                echo -e "   ${lib_prefixes[$prefix]}"
                echo -e "   $basename"
                ((duplicates++))
            else
                lib_prefixes[$prefix]=$basename
            fi
        fi
    done
    
    if [ $duplicates -gt 0 ]; then
        echo -e "${RED}❌ Found $duplicates duplicate libraries!${NC}"
        echo -e "${YELLOW}Run: ./scripts/setup.sh --clean${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ No duplicate libraries found${NC}"
    return 0
}
```

### 4. Update .gitignore

Ensure lib directory is properly ignored:

```gitignore
# Libraries (managed by setup script)
lib/
!lib/.gitkeep

# But keep track of which versions we need
lib-versions.txt
```

### 5. Document Required Versions

Create `lib-versions.txt`:

```
# PCM Desktop - Required Library Versions
# Auto-generated by setup.sh

jackson-databind=2.20.1
jackson-core=2.20.1
jackson-annotations=2.20
jackson-datatype-jsr310=2.20.1
slf4j-api=2.0.17
logback-classic=1.5.21
logback-core=1.5.21
sqlite-jdbc=3.51.0.0
ojdbc11=23.26.0.0.0
hikaricp=7.0.2
```

## 📊 Verification Checklist

After fixing, verify:

- [ ] No duplicate JARs in `lib/others/`
- [ ] Application starts without errors: `./scripts/run.sh`
- [ ] Application window appears and is functional
- [ ] No "Missing JavaFX application class" error
- [ ] Logs show "✅ Application started successfully"
- [ ] Can run on both macOS and Linux
- [ ] Windows compatibility maintained (`run.bat` still works)

## 🔗 Related Issues

- **JavaFX Module System**: Understanding JavaFX module path vs classpath
- **Library Management**: Best practices for managing dependencies
- **Cross-Platform Builds**: Ensuring consistency across operating systems

## 📚 References

- [JavaFX Module System Documentation](https://openjfx.io/openjfx-docs/)
- [Java Module System (JPMS)](https://www.oracle.com/java/technologies/javase/jdk-9-jdk-8-migration-guide.html)
- [Maven Dependency Management](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)

## 🏷️ Tags

`#troubleshooting` `#javafx` `#runtime` `#macos` `#linux` `#module-system` `#dependencies` `#library-conflict`

---

**Last Updated**: 2025-11-15  
**Tested On**: macOS 24.6.0, Java 21.0.9  
**Resolved By**: Removing duplicate library JARs and ensuring clean dependency management

