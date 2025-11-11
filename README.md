# PCM Desktop Application

Personal Content Manager - Desktop Application built with JavaFX (No Maven)

## 📋 Overview

PCM Desktop là ứng dụng desktop cross-platform cho personal content management. Xây dựng với JavaFX, không sử dụng Maven, import thư viện trực tiếp.

## ✨ Features

- 📊 **Dashboard** - Overview of your content
- 📁 **Projects** - Manage your projects
- 📝 **Notes** - Quick note-taking
- ✓ **Tasks** - Task management
- ⚙️ **Settings** - Customize your experience

## 🛠️ Tech Stack

- **Java 17+** - Programming language
- **JavaFX 23** - UI framework (Latest)
- **Lombok 1.18.34** - Reduce boilerplate (Latest)
- **Jackson 2.17.2** - JSON processing (Latest)
- **SQLite 3.46.1.0** - Local database (Latest)
- **Logback 1.5.6** - Logging (Latest)
- **No Build Tool** - Direct JAR import

## 📦 Project Structure

```
pcm-desktop/
├── lib/                           # External Libraries
│   ├── javafx/                    # JavaFX 23 JARs
│   └── others/                    # Other library JARs
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/noteflix/pcm/
│   │   │       ├── PCMApplication.java      # Main entry
│   │   │       ├── ui/                      # UI Controllers
│   │   │       ├── domain/                  # Domain models
│   │   │       ├── application/             # Services
│   │   │       └── infrastructure/          # Data access
│   │   └── resources/
│   │       ├── fxml/                        # UI layouts
│   │       ├── css/                         # Stylesheets
│   │       ├── images/                      # Assets
│   │       └── logback.xml                  # Logging config
│   └── test/java/                           # Tests
├── docs/                                    # Documentation
│   ├── README.md                            # Main docs (moved)
│   ├── QUICK_START.md                       # Quick guide (moved)
│   ├── STEP_BY_STEP_GUIDE.md                # Tutorial (moved)
│   ├── PROJECT_SUMMARY.md                   # Summary (moved)
│   └── LIBRARY_SETUP.md                     # Library guide
├── download-libs.sh                         # Download script (Unix)
├── download-libs.ps1                        # Download script (Windows)
├── .gitignore                               # Git rules
└── README.md                                # This file
```

## 🚀 Quick Start

### Step 1: Download Libraries

#### macOS/Linux

```bash
chmod +x download-libs.sh
./download-libs.sh
```

#### Windows

```powershell
powershell -ExecutionPolicy Bypass -File download-libs.ps1
```

### Step 2: Download JavaFX 23 Manually

**Visit:** https://gluonhq.com/products/javafx/

**Download cho platform của bạn:**
- macOS (Apple Silicon): `openjfx-23_osx-aarch64_bin-sdk.zip`
- macOS (Intel): `openjfx-23_osx-x64_bin-sdk.zip`  
- Windows: `openjfx-23_windows-x64_bin-sdk.zip`
- Linux: `openjfx-23_linux-x64_bin-sdk.zip`

**Extract và copy:**
- Copy tất cả `.jar` files từ `lib/` folder trong ZIP
- Paste vào `pcm-desktop/lib/javafx/`

### Step 3: Verify Libraries

```bash
ls -l lib/javafx/    # Should have 8 JAR files
ls -l lib/others/    # Should have 9 JAR files
```

### Step 4: Open in IDE

#### IntelliJ IDEA (Recommended)

1. **Open Project:**
   - File → Open → Select `pcm-desktop` folder

2. **Add Libraries:**
   - File → Project Structure (⌘;) → Libraries
   - Click `+` → Java → Select `lib/javafx` → Add all JARs
   - Click `+` → Java → Select `lib/others` → Add all JARs
   - Click Apply

3. **Configure Lombok:**
   - Settings (⌘,) → Plugins → Install "Lombok"
   - Settings → Compiler → Annotation Processors
   - ✅ Enable annotation processing
   - Click Apply

4. **Create Run Configuration:**
   - Run → Edit Configurations → Add New (+) → Application
   - Name: `PCM Desktop`
   - Main class: `com.noteflix.pcm.PCMApplication`
   - VM options:
     ```
     --module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
     ```
   - Click Apply

5. **Run Application:**
   - Click Run button ▶️ or press ⌃R

#### Eclipse

See `docs/LIBRARY_SETUP.md` for Eclipse setup.

#### VS Code

See `docs/LIBRARY_SETUP.md` for VS Code setup.

## 📖 Documentation

All documentation is in `docs/` folder:

- **[LIBRARY_SETUP.md](docs/LIBRARY_SETUP.md)** - Complete library setup guide
- **[README.md](docs/README.md)** - Full documentation (moved)
- **[QUICK_START.md](docs/QUICK_START.md)** - Quick start guide (moved)
- **[STEP_BY_STEP_GUIDE.md](docs/STEP_BY_STEP_GUIDE.md)** - Detailed tutorial (moved)
- **[PROJECT_SUMMARY.md](docs/PROJECT_SUMMARY.md)** - Project overview (moved)

## 🏃 Run from Command Line

### Compile

```bash
# macOS/Linux
javac -cp "lib/javafx/*:lib/others/*" \
  -d out \
  $(find src/main/java -name "*.java")

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

## 🧪 Testing

```bash
# Compile tests
javac -cp "lib/javafx/*:lib/others/*:out" \
  -d out-test \
  src/test/java/**/*.java

# Run tests (requires JUnit in lib/others/)
java -cp "out:out-test:lib/javafx/*:lib/others/*" \
  org.junit.runner.JUnitCore com.noteflix.pcm.YourTest
```

## 📦 Building Distribution

### Create JAR

```bash
# Create manifest
echo "Main-Class: com.noteflix.pcm.PCMApplication" > manifest.txt

# Create JAR
jar cfm pcm-desktop.jar manifest.txt -C out .

# Copy dependencies
mkdir dist
cp pcm-desktop.jar dist/
cp -r lib dist/

# Run
cd dist
java --module-path lib/javafx \
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media \
  -cp "pcm-desktop.jar:lib/others/*" \
  com.noteflix.pcm.PCMApplication
```

### Create Installer (jpackage)

```bash
# Windows
jpackage \
  --input dist \
  --name "PCM Desktop" \
  --main-jar pcm-desktop.jar \
  --main-class com.noteflix.pcm.PCMApplication \
  --type exe \
  --java-options "--module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media"

# macOS
jpackage \
  --input dist \
  --name "PCM Desktop" \
  --main-jar pcm-desktop.jar \
  --main-class com.noteflix.pcm.PCMApplication \
  --type dmg \
  --java-options "--module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media"
```

## 🔧 Library Versions

| Library | Version | Latest | Download |
|---------|---------|--------|----------|
| JavaFX | 23 | ✅ Nov 2025 | https://gluonhq.com/products/javafx/ |
| Lombok | 1.18.34 | ✅ Nov 2025 | https://projectlombok.org/ |
| Jackson | 2.17.2 | ✅ Nov 2025 | https://github.com/FasterXML/jackson |
| SLF4J | 2.0.13 | ✅ Nov 2025 | https://www.slf4j.org/ |
| Logback | 1.5.6 | ✅ Nov 2025 | https://logback.qos.ch/ |
| SQLite JDBC | 3.46.1.0 | ✅ Nov 2025 | https://github.com/xerial/sqlite-jdbc |

**All libraries are the latest stable versions as of November 2025.**

## 🐛 Troubleshooting

### JavaFX not found

**Error:** `Error: JavaFX runtime components are missing`

**Solution:**
1. Ensure JavaFX JARs are in `lib/javafx/`
2. Add VM options in run configuration
3. Check all 8 JavaFX JARs are present

### Lombok not working

**Error:** `Cannot find symbol 'log'`

**Solution:**
1. Install Lombok plugin in IDE
2. Enable annotation processing
3. Rebuild project

### Libraries not found

**Error:** `ClassNotFoundException`

**Solution:**
1. Run download script: `./download-libs.sh`
2. Download JavaFX manually
3. Refresh IDE libraries

See **[docs/LIBRARY_SETUP.md](docs/LIBRARY_SETUP.md)** for complete troubleshooting guide.

## 📚 Learning Resources

- [JavaFX Documentation](https://openjfx.io/)
- [JavaFX Tutorial](https://docs.oracle.com/javafx/2/)
- [Scene Builder](https://gluonhq.com/products/scene-builder/) - Visual FXML editor
- [Lombok Documentation](https://projectlombok.org/)

## 🤝 Contributing

1. Create feature branch
2. Make changes
3. Test thoroughly
4. Submit pull request

## 📄 License

Private project - All rights reserved

## 👤 Author

Noteflix Team

---

**✅ No Maven Required - Pure JAR Import!**

**🚀 Ready to Start:**
```bash
./download-libs.sh    # Download libraries
# Download JavaFX from https://gluonhq.com/products/javafx/
# Open in IntelliJ IDEA
# Run PCMApplication
```
# pcm-desktop
