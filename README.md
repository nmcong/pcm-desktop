# PCM Desktop Application

**Project Code Management** - AI-Powered System Analysis & Business Management Tool

## 📋 Overview

PCM Desktop là ứng dụng desktop AI-powered sử dụng Large Language Models (LLM) để phân tích và quản lý toàn bộ hệ thống phần mềm doanh nghiệp. Ứng dụng cho phép người dùng đặt câu hỏi và nhận được phân tích chi tiết về source code, nghiệp vụ, database, và các thành phần hệ thống.

## ✨ Core Features

### 🤖 AI-Powered Analysis
- **LLM Integration** - Tích hợp AI models để phân tích và trả lời câu hỏi
- **Natural Language Queries** - Đặt câu hỏi bằng ngôn ngữ tự nhiên
- **Intelligent Responses** - Phân tích sâu và đưa ra insights

### 📊 System Management
- **Subsystems & Projects** - Quản lý cấu trúc hệ thống phân cấp
- **Screen/Form Management** - Theo dõi tất cả màn hình trong hệ thống
- **Event Tracking** - Quản lý events và sự kiện trên từng màn hình
- **Source Code Mapping** - Liên kết source code với màn hình tương ứng

### 🗄️ Database Management
- **Oracle DB Objects** - Quản lý toàn bộ database objects (Tables, Views, Procedures, Functions, Packages, Triggers, etc.)
- **Schema Analysis** - Phân tích cấu trúc database
- **Relationship Mapping** - Theo dõi mối quan hệ giữa các objects

### ⚙️ Batch Job Management
- **Job Configuration** - Thông tin cấu hình batch jobs
- **Schedule Information** - Thời gian chạy và tần suất
- **Code Analysis** - Source code của batch jobs
- **Database Connections** - Theo dõi database connections của jobs
- **Dependencies** - Mối quan hệ giữa các jobs

### 🔄 Workflow Management
- **Business Process Flows** - Quản lý quy trình nghiệp vụ
- **Workflow Visualization** - Hiển thị workflow diagram
- **Process Documentation** - Tài liệu hóa quy trình

### 📚 Knowledge Base
- **System Documentation** - Tài liệu hệ thống tập trung
- **Business Rules** - Quản lý nghiệp vụ rules
- **Technical Specifications** - Spec kỹ thuật
- **Best Practices** - Tài liệu best practices

## 🛠️ Tech Stack

### Backend & Core
- **Java 21** - Programming language
- **JavaFX 21.0.9** - UI framework (compatible with Java 21)
- **Lombok 1.18.34** - Reduce boilerplate (Latest)
- **Jackson 2.17.2** - JSON processing for data serialization (Latest)
- **SQLite 3.46.1.0** - Local metadata database (Latest)
- **Logback 1.5.6** - Logging framework (Latest)

### AI & Analysis
- **LLM Integration** - Large Language Model APIs
- **Oracle JDBC** - Connection to Oracle databases
- **Source Code Parser** - Java/SQL code analysis
- **AST Analysis** - Abstract Syntax Tree parsing

### Architecture
- **No Build Tool** - Direct JAR import for simplicity
- **Domain-Driven Design** - Clean architecture
- **Repository Pattern** - Data access abstraction
- **Service Layer** - Business logic separation

## 📦 Project Structure

```
pcm-desktop/
├── lib/                           # External Libraries
│   ├── javafx/                    # JavaFX 21.0.9 JARs (platform-specific)
│   └── others/                    # Other library JARs (platform-independent)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/noteflix/pcm/
│   │   │       ├── PCMApplication.java      # Main entry point
│   │   │       ├── ui/                      # UI Controllers & Views
│   │   │       │   ├── MainController.java  # Main window controller
│   │   │       │   ├── dashboard/           # Dashboard views
│   │   │       │   ├── subsystem/           # Subsystem management
│   │   │       │   ├── screen/              # Screen/Form management
│   │   │       │   ├── database/            # DB objects management
│   │   │       │   ├── batch/               # Batch job management
│   │   │       │   ├── workflow/            # Workflow visualization
│   │   │       │   ├── knowledge/           # Knowledge base
│   │   │       │   └── query/               # AI Query interface
│   │   │       ├── domain/                  # Domain models
│   │   │       │   ├── model/               # Entity models
│   │   │       │   │   ├── Subsystem.java
│   │   │       │   │   ├── Screen.java
│   │   │       │   │   ├── Event.java
│   │   │       │   │   ├── SourceFile.java
│   │   │       │   │   ├── DatabaseObject.java
│   │   │       │   │   ├── BatchJob.java
│   │   │       │   │   ├── Workflow.java
│   │   │       │   │   └── KnowledgeEntry.java
│   │   │       │   └── repository/          # Repository interfaces
│   │   │       ├── application/             # Application services
│   │   │       │   ├── service/
│   │   │       │   │   ├── LLMService.java        # LLM integration
│   │   │       │   │   ├── CodeAnalyzer.java      # Source code analysis
│   │   │       │   │   ├── DatabaseAnalyzer.java  # DB analysis
│   │   │       │   │   ├── BatchJobService.java   # Batch job management
│   │   │       │   │   ├── WorkflowService.java   # Workflow management
│   │   │       │   │   └── QueryService.java      # Query processing
│   │   │       │   └── dto/                 # Data Transfer Objects
│   │   │       └── infrastructure/          # Infrastructure layer
│   │   │           ├── persistence/         # Data access implementations
│   │   │           ├── oracle/              # Oracle DB integration
│   │   │           ├── ai/                  # AI/LLM integration
│   │   │           └── parser/              # Code parsers
│   │   └── resources/
│   │       ├── fxml/                        # UI layouts (FXML files)
│   │       ├── css/                         # Stylesheets
│   │       ├── images/                      # Icons & assets
│   │       └── logback.xml                  # Logging configuration
│   └── test/java/                           # Unit & integration tests
├── docs/                                    # Documentation
│   ├── README.md                            # Full documentation
│   ├── LIBRARY_SETUP.md                     # Library setup guide
│   ├── ARCHITECTURE.md                      # System architecture
│   └── API_GUIDE.md                         # LLM API integration guide
├── download-libs.sh                         # Download script (Unix/macOS)
├── download-libs.ps1                        # Download script (Windows)
├── compile-windows.bat                      # Compile script (Windows)
├── run-windows.bat                          # Run script (Windows)
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

### Step 2: Download JavaFX 21.0.9 Manually

**⚠️ Important:** JavaFX contains platform-specific native libraries. You MUST download the correct version for your OS.

**Visit:** https://gluonhq.com/products/javafx/

**Download for your platform:**
- macOS (Apple Silicon): `openjfx-21.0.9_osx-aarch64_bin-sdk.zip`
- macOS (Intel): `openjfx-21.0.9_osx-x64_bin-sdk.zip`  
- Windows: `openjfx-21.0.9_windows-x64_bin-sdk.zip`
- Linux: `openjfx-21.0.9_linux-x64_bin-sdk.zip`

**Direct download links:**
- macOS (Apple Silicon): https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-aarch64_bin-sdk.zip
- macOS (Intel): https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-x64_bin-sdk.zip
- Windows: https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip
- Linux: https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_linux-x64_bin-sdk.zip

**Extract and install:**
1. Extract the downloaded ZIP file
2. Navigate to `javafx-sdk-21.0.9/lib/` folder
3. Copy all `.jar` files to `pcm-desktop/lib/javafx/`

**Why platform-specific?** JavaFX JARs contain native libraries (.dll for Windows, .dylib for macOS, .so for Linux) that are OS-specific and cannot be shared between platforms.

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

| Library | Version | Java Compatibility | Download |
|---------|---------|-------------------|----------|
| JavaFX | 21.0.9 | Java 21 ✅ | https://gluonhq.com/products/javafx/ |
| Lombok | 1.18.34 | All Java versions ✅ | https://projectlombok.org/ |
| Jackson | 2.17.2 | All Java versions ✅ | https://github.com/FasterXML/jackson |
| SLF4J | 2.0.13 | All Java versions ✅ | https://www.slf4j.org/ |
| Logback | 1.5.6 | All Java versions ✅ | https://logback.qos.ch/ |
| SQLite JDBC | 3.46.1.0 | All Java versions ✅ | https://github.com/xerial/sqlite-jdbc |

**Note:** This project uses **Java 21** and **JavaFX 21.0.9** (not JavaFX 23/25) for compatibility.

## 🐛 Troubleshooting

### Java Version Mismatch

**Error:** `class file has wrong version 67.0, should be 65.0`

**Cause:** Using JavaFX 25 (requires Java 23) with Java 21

**Solution:**
1. Delete all JARs in `lib/javafx/`
2. Download **JavaFX 21.0.9** (not 23 or 25)
3. Extract and copy JARs to `lib/javafx/`
4. Rebuild project

### Platform Mismatch

**Error:** `UnsatisfiedLinkError` or `Can't load library`

**Cause:** Using JavaFX JARs from different OS (e.g., macOS JARs on Windows)

**Solution:**
- Download the **correct platform-specific JavaFX 21.0.9**
- Windows: `openjfx-21.0.9_windows-x64_bin-sdk.zip`
- macOS (M1/M2/M3): `openjfx-21.0.9_osx-aarch64_bin-sdk.zip`
- macOS (Intel): `openjfx-21.0.9_osx-x64_bin-sdk.zip`

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
1. Run download script: `./download-libs.sh` or `.\download-libs.ps1`
2. Download JavaFX manually for your OS
3. Refresh IDE libraries

See **[docs/LIBRARY_SETUP.md](docs/LIBRARY_SETUP.md)** for complete troubleshooting guide.

## 📚 Learning Resources

### JavaFX & UI
- [JavaFX Documentation](https://openjfx.io/)
- [JavaFX Tutorial](https://docs.oracle.com/javafx/2/)
- [Scene Builder](https://gluonhq.com/products/scene-builder/) - Visual FXML editor

### Development Tools
- [Lombok Documentation](https://projectlombok.org/)
- [Jackson Documentation](https://github.com/FasterXML/jackson-docs)

### AI & LLM Integration
- [OpenAI API](https://platform.openai.com/docs)
- [Anthropic Claude API](https://docs.anthropic.com/)
- [LangChain Java](https://github.com/hwchase17/langchain)

### Database
- [Oracle JDBC Driver](https://www.oracle.com/database/technologies/appdev/jdbc.html)
- [SQLite JDBC](https://github.com/xerial/sqlite-jdbc)

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
# macOS/Linux
./download-libs.sh
# Download JavaFX 21.0.9 for your platform
# Open in IntelliJ IDEA
# Configure libraries and run PCMApplication

# Windows
.\download-libs.ps1
# JavaFX 21.0.9 will be downloaded automatically
# Or run: .\compile-windows.bat && .\run-windows.bat
```

---

## 🎯 Use Cases

### For System Analysts
- Query system architecture and component relationships
- Understand business flows across subsystems
- Find screens and related code quickly

### For Developers
- Locate source code for specific features
- Understand database schema and dependencies
- Analyze batch job logic and schedules

### For Database Administrators
- Browse all Oracle database objects
- Understand table relationships and dependencies
- Find stored procedures and packages by functionality

### For Project Managers
- Get overview of system complexity
- Understand subsystem boundaries
- Track workflow implementations

---

**PCM Desktop - AI-Powered System Analysis for Enterprise Software**
