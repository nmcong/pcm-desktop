# PCM Desktop - Dependency Management

## Overview

This project now uses `pom.xml` to manage all library dependencies centrally. All versions are defined in one place for easy maintenance.

## Available Scripts

### 1. Download Dependencies

Downloads all dependencies from Maven Central based on `pom.xml`.

**Windows:**
```batch
scripts\download-deps.bat
```

**Linux/Mac:**
```bash
chmod +x scripts/*.sh  # Make executable (first time only)
./scripts/download-deps.sh
```

**Direct Python:**
```bash
python scripts/download_deps.py
# or
python3 scripts/download_deps.py
```

### 2. Create Library Archives

Creates multi-part ZIP archives of the `lib` directory. Each part is maximum 45MB for easy sharing.

**Windows:**
```batch
scripts\create-lib-archive.bat
```

**Linux/Mac:**
```bash
./scripts/create-lib-archive.sh
```

Archives will be created in the `archives/` directory:
- `pcm-libs-part01.zip`
- `pcm-libs-part02.zip`
- etc.

### 3. Check for Updates

Checks Maven Central for newer versions of all dependencies.

**Windows:**
```batch
scripts\check-updates.bat
```

**Linux/Mac:**
```bash
./scripts/check-updates.sh
```

This will show:
- Which dependencies have updates available
- Current vs. latest versions
- Which dependencies are up to date

### 4. Traditional Setup (Legacy)

The traditional `setup.bat`/`setup.sh` scripts are still available but use hardcoded URLs.

**Recommendation:** Use `download-deps` scripts instead for easier maintenance.

## Managing Dependencies

### Adding a New Dependency

1. Open `pom.xml`
2. Find the `<dependencies>` section
3. Add new dependency:
   ```xml
   <dependency>
     <groupId>com.example</groupId>
     <artifactId>example-lib</artifactId>
     <version>1.0.0</version>
   </dependency>
   ```
4. Run `download-deps` script to download

### Updating a Dependency Version

1. Open `pom.xml`
2. Find the `<properties>` section
3. Update the version property:
   ```xml
   <lombok.version>1.18.35</lombok.version>
   ```
4. Run `download-deps` script to download new version

### Checking for Updates

Run `check-updates` script to see which dependencies have newer versions available.

## Directory Structure

```
lib/
├── javafx/          # JavaFX SDK (platform-specific)
├── others/          # Core libraries (Lombok, Jackson, DB drivers, UI)
├── rag/             # RAG libraries (Lucene, DJL, ONNX, JavaParser)
└── text-component/  # Text editor components (RichTextFX, etc.)
```

## Dependencies Overview

### Core Libraries
- **Lombok** 1.18.34 - Code generation
- **Jackson** 2.20.1 - JSON processing
- **SLF4J/Logback** - Logging

### Database
- **SQLite JDBC** 3.51.0.0 - Embedded database
- **Oracle OJDBC** 23.26.0.0.0 - Oracle connectivity
- **HikariCP** 7.0.2 - Connection pooling

### UI Libraries
- **JavaFX** 21.0.9 - UI framework
- **AtlantaFX** 2.1.0 - Modern themes
- **Ikonli** 12.4.0 - Icon library
- **RichTextFX** 0.11.6 - Text editor component

### RAG Libraries
- **Apache Lucene** 10.3.1 - Full-text search
- **DJL ONNX** 0.35.0 - ML inference
- **ONNX Runtime** 1.23.2 - ML runtime
- **JavaParser** 3.26.3 - Java AST parsing

## Build Process

After downloading dependencies:

1. **Build the project:**
   ```batch
   scripts\build.bat
   ```

2. **Run the application:**
   ```batch
   scripts\run.bat
   ```

## Troubleshooting

### Dependencies not downloading

1. Check internet connection
2. Verify Maven Central is accessible
3. Check for firewall/proxy issues

### Version conflicts

If you encounter classpath issues:

1. Delete `lib/` directory
2. Run `download-deps` script to get fresh downloads
3. Check for duplicate JARs in lib subdirectories

### Archive creation fails

Ensure you have:
- Python 3.x installed
- Write permissions in project directory
- Sufficient disk space

## Notes

- **LangChain4j dependency has been removed** - The project no longer depends on LangChain4j
- JavaFX native libraries (DLLs) must be platform-specific
- Some dependencies may require manual download if not available on Maven Central

