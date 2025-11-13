# 🔄 Migration Guide - Maven to No Maven

## 📋 Overview

This guide explains the migration from Maven-based project to direct JAR import project.

## 🎯 What Changed

### Before (Maven)

```
pcm-desktop/
├── pom.xml                    # Maven configuration
├── src/
├── target/                    # Build output
└── docs/                      # Mixed with root
```

**Dependencies:** Downloaded automatically by Maven

**Build:** `mvn clean install`

**Run:** `mvn javafx:run`

### After (No Maven)

```
pcm-desktop/
├── lib/                       # ✨ NEW: External libraries
│   ├── javafx/                # JavaFX JARs
│   └── others/                # Other JARs
├── src/
├── out/                       # Build output
├── docs/                      # ✨ All docs here
├── download-libs.sh           # ✨ NEW: Download script
├── download-libs.ps1          # ✨ NEW: Windows script
└── README.md
```

**Dependencies:** Manual download with scripts

**Build:** `javac` command

**Run:** `java` command with classpath

---

## 🔄 Migration Steps

### Step 1: Backup Old Project

```bash
cp -r pcm-desktop pcm-desktop-maven-backup
```

### Step 2: Remove Maven Files

```bash
cd pcm-desktop
rm -rf pom.xml target/ .mvn/ mvnw mvnw.cmd
```

✅ **Done** - Maven files removed

### Step 3: Create Library Folders

```bash
mkdir -p lib/javafx
mkdir -p lib/others
```

✅ **Done** - Library structure created

### Step 4: Organize Documentation

```bash
mkdir -p docs
mv *.md docs/  # Move all markdown docs
# Keep only main README.md in root
```

✅ **Done** - Docs organized

### Step 5: Download Libraries

```bash
# Run download script
chmod +x download-libs.sh
./download-libs.sh

# Download JavaFX manually from:
# https://gluonhq.com/products/javafx/
```

✅ **Done** - Libraries downloaded

### Step 6: Configure IDE

**IntelliJ IDEA:**

1. File → Project Structure → Libraries
2. Add `lib/javafx` as library
3. Add `lib/others` as library

**Eclipse:**

1. Properties → Java Build Path → Libraries
2. Add External JARs from `lib/javafx`
3. Add External JARs from `lib/others`

✅ **Done** - IDE configured

---

## 📊 Library Mapping

### Maven Dependencies → Direct JARs

| Maven Dependency                 | Direct JAR                                      | Version  |
|----------------------------------|-------------------------------------------------|----------|
| `javafx-controls:21.0.1`         | `lib/javafx/javafx.controls.jar`                | 23       |
| `javafx-fxml:21.0.1`             | `lib/javafx/javafx.fxml.jar`                    | 23       |
| `javafx-web:21.0.1`              | `lib/javafx/javafx.web.jar`                     | 23       |
| `javafx-media:21.0.1`            | `lib/javafx/javafx.media.jar`                   | 23       |
| `lombok:1.18.30`                 | `lib/others/lombok-1.18.34.jar`                 | 1.18.34  |
| `jackson-databind:2.16.0`        | `lib/others/jackson-databind-2.17.2.jar`        | 2.17.2   |
| `jackson-core:2.16.0`            | `lib/others/jackson-core-2.17.2.jar`            | 2.17.2   |
| `jackson-annotations:2.16.0`     | `lib/others/jackson-annotations-2.17.2.jar`     | 2.17.2   |
| `jackson-datatype-jsr310:2.16.0` | `lib/others/jackson-datatype-jsr310-2.17.2.jar` | 2.17.2   |
| `slf4j-api:1.7.x`                | `lib/others/slf4j-api-2.0.13.jar`               | 2.0.13   |
| `logback-classic:1.4.11`         | `lib/others/logback-classic-1.5.6.jar`          | 1.5.6    |
| `logback-core:1.4.11`            | `lib/others/logback-core-1.5.6.jar`             | 1.5.6    |
| `sqlite-jdbc:3.44.1.0`           | `lib/others/sqlite-jdbc-3.46.1.0.jar`           | 3.46.1.0 |

**Note:** All versions upgraded to latest (Nov 2025)

---

## 🛠️ Build Command Changes

### Maven → Direct Commands

| Task        | Maven            | Direct Java                                                                                                                      |
|-------------|------------------|----------------------------------------------------------------------------------------------------------------------------------|
| **Clean**   | `mvn clean`      | `rm -rf out/`                                                                                                                    |
| **Compile** | `mvn compile`    | `javac -cp "lib/javafx/*:lib/others/*" -d out $(find src/main/java -name "*.java")`                                              |
| **Package** | `mvn package`    | `jar cfm app.jar manifest.txt -C out .`                                                                                          |
| **Run**     | `mvn javafx:run` | `java --module-path lib/javafx --add-modules javafx.controls,javafx.fxml -cp "out:lib/others/*" com.noteflix.pcm.PCMApplication` |
| **Test**    | `mvn test`       | `java -cp "out:out-test:lib/*" org.junit.runner.JUnitCore TestClass`                                                             |

---

## 🔧 IDE Run Configuration Changes

### Maven Configuration

```xml
<!-- Old Maven run config -->
<configuration>
  <mainClass>com.noteflix.pcm.PCMApplication</mainClass>
</configuration>
```

### Direct Java Configuration

**IntelliJ IDEA:**

- **Main class:** `com.noteflix.pcm.PCMApplication`
- **VM options:**
  ```
  --module-path lib/javafx
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
  ```
- **Classpath:** Project + `lib/javafx/*` + `lib/others/*`

**Eclipse:**

- **Main class:** `com.noteflix.pcm.PCMApplication`
- **VM arguments:**
  ```
  --module-path lib/javafx
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
  ```

---

## ✅ Verification Checklist

After migration, verify:

- [ ] Maven files removed (`pom.xml`, `target/`)
- [ ] Library folders created (`lib/javafx/`, `lib/others/`)
- [ ] All JARs downloaded (17 total)
- [ ] Docs organized in `docs/` folder
- [ ] IDE libraries configured
- [ ] Lombok plugin installed
- [ ] Annotation processing enabled
- [ ] Run configuration created
- [ ] Application runs successfully

---

## 🎯 Advantages of No Maven

### ✅ Pros

1. **No Build Tool Required** - Just Java + IDE
2. **Simpler Structure** - Easy to understand
3. **Direct Control** - Know exactly what JARs are used
4. **Faster Setup** - No Maven download/config
5. **Portable** - Copy project anywhere
6. **Version Control** - Can commit JARs if needed
7. **Offline Work** - No internet required after setup

### ⚠️ Cons

1. **Manual Updates** - Update JARs manually
2. **No Transitive Deps** - Must download all dependencies
3. **More Setup** - Initial library download
4. **IDE Specific** - Different setup per IDE

---

## 📝 Update Workflow

### Adding New Library

**Before (Maven):**

```xml
<dependency>
  <groupId>org.example</groupId>
  <artifactId>library</artifactId>
  <version>1.0.0</version>
</dependency>
```

**After (Direct JAR):**

1. Download JAR from Maven Central or project site
2. Place in `lib/others/`
3. Refresh IDE libraries
4. Import in code

### Updating Existing Library

**Before (Maven):**

```xml
<!-- Change version in pom.xml -->
<version>1.0.0</version> → <version>2.0.0</version>
<!-- Run: mvn clean install -->
```

**After (Direct JAR):**

1. Download new version JAR
2. Replace old JAR in `lib/`
3. Refresh IDE libraries
4. Test application

---

## 🐛 Troubleshooting

### Issue: "Maven command not found"

**Solution:** ✅ Perfect! You don't need Maven anymore.

### Issue: "Dependencies not downloading"

**Solution:** Run `./download-libs.sh` and download JavaFX manually.

### Issue: "Cannot find JavaFX"

**Solution:**

1. Download from https://gluonhq.com/products/javafx/
2. Extract and copy JARs to `lib/javafx/`
3. Add to IDE libraries

### Issue: "Lombok not working"

**Solution:**

1. Install Lombok plugin
2. Enable annotation processing
3. Add `lib/others/lombok-1.18.34.jar` to classpath

---

## 📚 Documentation Updates

All documentation moved to `docs/`:

- `docs/README.md` - Main documentation (moved from root)
- `docs/QUICK_START.md` - Quick start guide
- `docs/STEP_BY_STEP_GUIDE.md` - Detailed tutorial
- `docs/PROJECT_SUMMARY.md` - Project overview
- `docs/LIBRARY_SETUP.md` - Library setup guide
- `docs/MIGRATION_GUIDE.md` - This file

**Root `README.md`:** Now focuses on no-Maven setup

---

## 🎉 Migration Complete!

Your project is now:

- ✅ Maven-free
- ✅ Using latest libraries (Nov 2025)
- ✅ Documentation organized
- ✅ Ready to run with direct JARs

**Next Steps:**

1. Run `./download-libs.sh`
2. Download JavaFX from https://gluonhq.com/products/javafx/
3. Configure IDE
4. Run application

---

**Questions?** Check `docs/LIBRARY_SETUP.md` for detailed setup instructions.

