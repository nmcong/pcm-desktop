# 📚 Library Setup Guide - Manual Import

## 📦 Required Libraries (Latest Versions - Nov 2025)

### 1. JavaFX 23 (Latest Stable)

**Download:**
- **Site:** https://gluonhq.com/products/javafx/
- **Direct:** https://download2.gluonhq.com/openjfx/23/openjfx-23_windows-x64_bin-sdk.zip (Windows)
- **Direct:** https://download2.gluonhq.com/openjfx/23/openjfx-23_osx-aarch64_bin-sdk.zip (macOS M1/M2/M3)
- **Direct:** https://download2.gluonhq.com/openjfx/23/openjfx-23_linux-x64_bin-sdk.zip (Linux)

**Files Needed:**
```
lib/javafx/
├── javafx.base.jar
├── javafx.controls.jar
├── javafx.fxml.jar
├── javafx.graphics.jar
├── javafx.media.jar
├── javafx.swing.jar
├── javafx.web.jar
└── javafx-swt.jar
```

### 2. Lombok 1.18.34 (Latest)

**Download:**
- **Site:** https://projectlombok.org/download
- **Direct:** https://projectlombok.org/downloads/lombok.jar

**File:**
```
lib/others/
└── lombok-1.18.34.jar
```

### 3. Jackson 2.17.2 (Latest)

**Download from Maven Central:**
- **jackson-databind:** https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.2/jackson-databind-2.17.2.jar
- **jackson-core:** https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.2/jackson-core-2.17.2.jar
- **jackson-annotations:** https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.2/jackson-annotations-2.17.2.jar
- **jackson-datatype-jsr310:** https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.17.2/jackson-datatype-jsr310-2.17.2.jar

**Files:**
```
lib/others/
├── jackson-databind-2.17.2.jar
├── jackson-core-2.17.2.jar
├── jackson-annotations-2.17.2.jar
└── jackson-datatype-jsr310-2.17.2.jar
```

### 4. SLF4J 2.0.13 (Latest)

**Download:**
- **slf4j-api:** https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar

**File:**
```
lib/others/
└── slf4j-api-2.0.13.jar
```

### 5. Logback 1.5.6 (Latest)

**Download:**
- **logback-classic:** https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.6/logback-classic-1.5.6.jar
- **logback-core:** https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.6/logback-core-1.5.6.jar

**Files:**
```
lib/others/
├── logback-classic-1.5.6.jar
└── logback-core-1.5.6.jar
```

### 6. SQLite JDBC 3.46.1.0 (Latest)

**Download:**
- **Direct:** https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.0/sqlite-jdbc-3.46.1.0.jar

**File:**
```
lib/others/
└── sqlite-jdbc-3.46.1.0.jar
```

---

## 🚀 Quick Download Script

### macOS/Linux

```bash
#!/bin/bash
cd lib/others

# Lombok
curl -O https://projectlombok.org/downloads/lombok.jar
mv lombok.jar lombok-1.18.34.jar

# Jackson
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.2/jackson-databind-2.17.2.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.2/jackson-core-2.17.2.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.2/jackson-annotations-2.17.2.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.17.2/jackson-datatype-jsr310-2.17.2.jar

# SLF4J
curl -O https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar

# Logback
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.6/logback-classic-1.5.6.jar
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.6/logback-core-1.5.6.jar

# SQLite
curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.0/sqlite-jdbc-3.46.1.0.jar

cd ../javafx
# Download JavaFX for your platform
# Extract and copy JAR files here
```

### Windows (PowerShell)

```powershell
cd lib\others

# Lombok
Invoke-WebRequest -Uri "https://projectlombok.org/downloads/lombok.jar" -OutFile "lombok-1.18.34.jar"

# Jackson
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.2/jackson-databind-2.17.2.jar" -OutFile "jackson-databind-2.17.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.2/jackson-core-2.17.2.jar" -OutFile "jackson-core-2.17.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.2/jackson-annotations-2.17.2.jar" -OutFile "jackson-annotations-2.17.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.17.2/jackson-datatype-jsr310-2.17.2.jar" -OutFile "jackson-datatype-jsr310-2.17.2.jar"

# SLF4J
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar" -OutFile "slf4j-api-2.0.13.jar"

# Logback
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.6/logback-classic-1.5.6.jar" -OutFile "logback-classic-1.5.6.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.6/logback-core-1.5.6.jar" -OutFile "logback-core-1.5.6.jar"

# SQLite
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.0/sqlite-jdbc-3.46.1.0.jar" -OutFile "sqlite-jdbc-3.46.1.0.jar"
```

---

## 📁 Final Structure

```
pcm-desktop/
├── lib/
│   ├── javafx/                    # JavaFX JARs
│   │   ├── javafx.base.jar
│   │   ├── javafx.controls.jar
│   │   ├── javafx.fxml.jar
│   │   ├── javafx.graphics.jar
│   │   ├── javafx.media.jar
│   │   ├── javafx.swing.jar
│   │   ├── javafx.web.jar
│   │   └── javafx-swt.jar
│   └── others/                    # Other JARs
│       ├── lombok-1.18.34.jar
│       ├── jackson-databind-2.17.2.jar
│       ├── jackson-core-2.17.2.jar
│       ├── jackson-annotations-2.17.2.jar
│       ├── jackson-datatype-jsr310-2.17.2.jar
│       ├── slf4j-api-2.0.13.jar
│       ├── logback-classic-1.5.6.jar
│       ├── logback-core-1.5.6.jar
│       └── sqlite-jdbc-3.46.1.0.jar
├── src/                           # Source code
└── docs/                          # Documentation
```

---

## ⚙️ IDE Setup

### IntelliJ IDEA

1. **Open Project:**
   - File → Open → Select `pcm-desktop` folder

2. **Add Libraries:**
   - File → Project Structure → Libraries
   - Click `+` → Java
   - Select `lib/javafx` folder → Add all JARs
   - Click `+` → Java
   - Select `lib/others` folder → Add all JARs
   - Apply

3. **Configure Lombok:**
   - Settings → Plugins → Install "Lombok"
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - ✅ Enable annotation processing

4. **Configure Module:**
   - File → Project Structure → Modules
   - SDK: Java 17 or later
   - Language Level: 17

5. **Run Configuration:**
   - Run → Edit Configurations → Add New → Application
   - Main class: `com.noteflix.pcm.PCMApplication`
   - VM options:
     ```
     --module-path lib/javafx
     --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
     ```

### Eclipse

1. **Open Project:**
   - File → Import → General → Existing Projects into Workspace

2. **Add Libraries:**
   - Right-click project → Properties → Java Build Path → Libraries
   - Add External JARs → Select all JARs from `lib/javafx`
   - Add External JARs → Select all JARs from `lib/others`

3. **Configure Lombok:**
   - Run `java -jar lib/others/lombok-1.18.34.jar`
   - Install Lombok into Eclipse
   - Restart Eclipse

4. **Run Configuration:**
   - Run → Run Configurations → Java Application
   - Main class: `com.noteflix.pcm.PCMApplication`
   - VM arguments:
     ```
     --module-path lib/javafx
     --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
     ```

### VS Code

1. **Install Extensions:**
   - Extension Pack for Java
   - Debugger for Java

2. **Configure `.vscode/settings.json`:**
   ```json
   {
     "java.project.sourcePaths": ["src/main/java"],
     "java.project.referencedLibraries": [
       "lib/javafx/*.jar",
       "lib/others/*.jar"
     ]
   }
   ```

3. **Configure Launch:**
   - Create `.vscode/launch.json`:
   ```json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "PCM Application",
         "request": "launch",
         "mainClass": "com.noteflix.pcm.PCMApplication",
         "vmArgs": "--module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media"
       }
     ]
   }
   ```

---

## 🏃 Run from Command Line

### Compile

```bash
# macOS/Linux
javac -cp "lib/javafx/*:lib/others/*" \
  -d out \
  src/main/java/com/noteflix/pcm/**/*.java

# Windows
javac -cp "lib/javafx/*;lib/others/*" ^
  -d out ^
  src/main/java/com/noteflix/pcm/**/*.java
```

### Run

```bash
# macOS/Linux
java --module-path lib/javafx \
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media \
  -cp "out:lib/others/*" \
  com.noteflix.pcm.PCMApplication

# Windows
java --module-path lib/javafx ^
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media ^
  -cp "out;lib/others/*" ^
  com.noteflix.pcm.PCMApplication
```

---

## ✅ Verification

After downloading all libraries, verify:

```bash
ls -l lib/javafx/
# Should show 8 JAR files

ls -l lib/others/
# Should show 9 JAR files

# Total: 17 JAR files
```

---

## 🔄 Update Libraries

To update to newer versions:

1. Check Maven Central: https://mvnrepository.com/
2. Download new JAR files
3. Replace old JARs in `lib/` folders
4. Update this document with new versions
5. Refresh IDE libraries

---

## 📚 Library Sizes (Approximate)

| Library | Size | Purpose |
|---------|------|---------|
| JavaFX (all) | ~70 MB | UI framework |
| Lombok | ~2 MB | Reduce boilerplate |
| Jackson (all) | ~3 MB | JSON processing |
| SLF4J | ~40 KB | Logging API |
| Logback (all) | ~800 KB | Logging implementation |
| SQLite JDBC | ~10 MB | Database driver |
| **Total** | **~86 MB** | All libraries |

---

## 🐛 Troubleshooting

### JavaFX not found

**Error:** `Error: JavaFX runtime components are missing`

**Solution:**
- Ensure JavaFX JARs are in `lib/javafx/`
- Add VM options: `--module-path lib/javafx --add-modules javafx.controls,javafx.fxml`

### Lombok not working

**Error:** `Cannot find symbol 'log'`

**Solution:**
- Install Lombok plugin in IDE
- Enable annotation processing
- Ensure `lombok.jar` is in classpath

### Class not found

**Error:** `ClassNotFoundException`

**Solution:**
- Check all JARs are in `lib/` folders
- Refresh IDE libraries
- Rebuild project

---

**✅ Libraries Setup Complete!**

