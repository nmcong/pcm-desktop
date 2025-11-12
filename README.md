# PCM Desktop Application

**Project Code Management** - AI-Powered System Analysis & Business Management Tool

## 📋 Overview

PCM Desktop là ứng dụng desktop AI-powered sử dụng Large Language Models (LLM) để phân tích và quản lý toàn bộ hệ thống phần mềm doanh nghiệp. Ứng dụng cho phép người dùng đặt câu hỏi và nhận được phân tích chi tiết về source code, nghiệp vụ, database, và các thành phần hệ thống.

## ✨ Core Features

### 🤖 AI-Powered Analysis
- **Multi-LLM Support** - Tích hợp OpenAI GPT, Anthropic Claude, Ollama
- **Streaming Responses** - Real-time AI responses với streaming support
- **Function Calling** - AI có thể gọi functions để thực hiện tasks
- **Natural Language Queries** - Đặt câu hỏi bằng ngôn ngữ tự nhiên
- **Intelligent Responses** - Phân tích sâu và đưa ra insights
- **Conversation Memory** - Multi-turn conversations với context

### 📊 System Management
- **Projects & Subsystems** - Quản lý cấu trúc hệ thống phân cấp
- **Screen/Form Management** - Theo dõi tất cả màn hình trong hệ thống  
- **Event Tracking** - Quản lý events và sự kiện trên từng màn hình
- **Source Code Mapping** - Liên kết source code với màn hình tương ứng
- **Activity Logging** - Theo dõi mọi thay đổi trong hệ thống

### 🗄️ Database Management
- **SQLite Metadata Storage** - Local database để lưu trữ metadata
- **Database Migration System** - Tự động migrate schema với versioning
- **Schema Version Control** - Theo dõi và quản lý phiên bản database
- **Transaction Support** - Rollback khi migration thất bại
- **Index Management** - Tối ưu hóa performance với proper indexing

### 💬 Chat System
- **Multi-LLM Conversations** - Chat với AI qua nhiều providers
- **Conversation History** - Lưu trữ và quản lý lịch sử chat
- **Message Threading** - Organize conversations theo topics
- **Search & Filter** - Tìm kiếm trong conversation history
- **Export Conversations** - Export chat history ra nhiều formats

### ⚙️ Batch Job Management  
- **Job Configuration** - Thông tin cấu hình batch jobs
- **Schedule Information** - Thời gian chạy và tần suất
- **Code Analysis** - Source code của batch jobs
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
- **JavaFX 21.0.9** - Modern UI framework (compatible with Java 21)
- **Lombok 1.18.34** - Reduce boilerplate code (Latest stable)
- **Jackson 2.18.2** - JSON processing for data serialization (Latest)
- **SQLite 3.47.1.0** - Local metadata database với JDBC driver (Latest)
- **Logback 1.5.12** - Advanced logging framework (Latest)

### Database & Migration
- **Database Migration Manager** - Custom migration system with versioning
- **Transaction Support** - ACID compliance with rollback capability
- **Schema Version Control** - Track và manage database versions
- **Idempotent Migrations** - Safe để run multiple times
- **Index Optimization** - Performance tuning với proper indexing

### UI & User Experience  
- **AtlantaFX** - Modern JavaFX theme framework
- **Pure Java UI** - No FXML, code-first approach
- **Responsive Design** - Adaptive layouts
- **Theme System** - Light/dark mode support
- **Component Library** - Reusable UI components

### AI & Analysis
- **Multi-LLM Integration** - OpenAI, Anthropic Claude, Ollama support
- **Streaming Responses** - Real-time AI conversation
- **Function Calling** - AI can execute system functions
- **Conversation Management** - Persistent chat history
- **Context Awareness** - Multi-turn conversations

### Architecture
- **No Build Tool Required** - Direct JAR import for simplicity
- **Domain-Driven Design** - Clean architecture principles
- **Repository Pattern** - Data access abstraction layer
- **Service Layer** - Business logic separation
- **Event-Driven** - Loosely coupled components
- **Dependency Injection** - Manual DI for lightweight design

## 📦 Project Structure

```
pcm-desktop/
├── lib/                           # External Libraries
│   ├── javafx/                    # JavaFX 21.0.9 JARs (platform-specific)
│   ├── others/                    # Core library JARs (platform-independent)
│   └── text-component/            # Rich text editing components
├── src/main/
│   ├── java/com/noteflix/pcm/
│   │   ├── PCMApplication.java         # Main entry point
│   │   ├── core/                       # Core system components
│   │   │   ├── auth/                   # SSO & authentication
│   │   │   │   ├── SSOTokenManager.java         # SSO token management
│   │   │   │   ├── BrowserCookieExtractor.java  # Extract browser tokens
│   │   │   │   └── SecurityAuditLogger.java     # Security audit trail
│   │   │   ├── navigation/             # Page navigation system
│   │   │   ├── theme/                  # Theme management (AtlantaFX)
│   │   │   └── utils/                  # Common utilities
│   │   ├── ui/                         # User Interface Layer
│   │   │   ├── MainController.java     # Main window controller
│   │   │   ├── MainView.java          # Main view layout
│   │   │   ├── components/             # Reusable UI components
│   │   │   │   ├── SidebarView.java    # Navigation sidebar
│   │   │   │   └── text/               # Universal text component
│   │   │   ├── layout/                 # Layout managers
│   │   │   └── pages/                  # Application pages
│   │   │       ├── AIAssistantPage.java        # AI chat interface
│   │   │       ├── DatabaseObjectsPage.java    # Database management
│   │   │       ├── BatchJobsPage.java          # Batch job management
│   │   │       ├── KnowledgeBasePage.java      # Knowledge base
│   │   │       └── SettingsPage.java           # Application settings
│   │   ├── domain/                     # Domain Models
│   │   │   ├── entity/                 # Core business entities
│   │   │   │   ├── Project.java        # Project management
│   │   │   │   └── BaseEntity.java     # Base entity class
│   │   │   ├── chat/                   # Chat domain models
│   │   │   │   ├── Conversation.java   # Chat conversation
│   │   │   │   ├── Message.java        # Chat message
│   │   │   │   └── MessageRole.java    # Message roles (user/assistant)
│   │   │   └── repository/             # Repository interfaces
│   │   ├── application/                # Application Services
│   │   │   └── service/chat/           # Chat services
│   │   │       ├── AIService.java      # AI interaction service
│   │   │       └── ConversationService.java # Conversation management
│   │   ├── infrastructure/             # Infrastructure Layer
│   │   │   ├── database/               # Database infrastructure
│   │   │   │   ├── ConnectionManager.java      # Database connections
│   │   │   │   └── DatabaseMigrationManager.java # Schema migrations
│   │   │   ├── dao/                    # Data Access Objects
│   │   │   │   ├── ConversationDAO.java        # Conversation data access
│   │   │   │   └── MessageDAO.java             # Message data access
│   │   │   ├── repository/chat/        # Chat repository implementations
│   │   │   └── exception/              # Custom exceptions
│   │   ├── llm/                        # LLM Integration Layer
│   │   │   ├── api/                    # LLM API interfaces
│   │   │   ├── client/                 # LLM provider clients
│   │   │   │   ├── openai/             # OpenAI integration
│   │   │   │   ├── anthropic/          # Anthropic Claude integration
│   │   │   │   └── ollama/             # Ollama local LLM integration
│   │   │   ├── model/                  # LLM data models
│   │   │   ├── service/                # LLM services
│   │   │   ├── factory/                # LLM client factory
│   │   │   ├── middleware/             # Request middleware (retry, rate limiting)
│   │   │   ├── exception/              # LLM-specific exceptions
│   │   │   └── examples/               # Usage examples and demos
│   │   └── examples/                   # Integration examples
│   │       └── SSOIntegrationDemo.java # SSO demo application
│   └── resources/
│       ├── db/migration/               # Database migration scripts
│       │   ├── V1__initial_schema.sql  # Initial database schema
│       │   └── V2__chat_tables.sql     # Chat functionality tables
│       ├── css/                        # Application stylesheets
│       │   ├── styles.css              # Main application styles
│       │   └── ai-assistant-dark.css   # Dark theme for AI assistant
│       ├── images/                     # Application assets
│       │   └── icons/                  # Application icons
│       └── logback.xml                 # Logging configuration
├── scripts/                            # Build & run scripts
│   ├── build.sh / build.bat            # Cross-platform build scripts
│   ├── run.sh / run.bat               # Cross-platform run scripts
│   └── setup.sh / setup.bat           # Environment setup scripts
├── out/                               # Compiled output
│   ├── com/noteflix/pcm/              # Compiled Java classes
│   ├── css/                           # Copied CSS files
│   ├── db/migration/                  # Copied migration files
│   └── images/                        # Copied image assets
├── docs/                              # Documentation
│   ├── setup/                         # Setup guides
│   ├── integrations/                  # Integration documentation
│   ├── troubleshooting/               # Troubleshooting guides
│   └── ui-components/                 # UI component documentation
├── logs/                              # Application logs
├── pcm-desktop.db                     # SQLite database file
└── README.md                          # This file
```

## 🚀 Quick Start

### Method 1: Fastest Start (Recommended) 

```bash
# macOS/Linux - One-line setup and run
./scripts/setup.sh && ./scripts/build.sh && ./scripts/run.sh

# Windows - One-line setup and run  
.\scripts\setup.bat && .\scripts\build.bat && .\scripts\run.bat
```

### Method 2: Step by Step

#### Step 1: Setup Dependencies

**macOS/Linux:**
```bash
./scripts/setup.sh
```

**Windows:**
```bash
.\scripts\setup.bat
```

This automatically:
- Downloads all required libraries to `lib/others/`
- Downloads correct JavaFX 21.0.9 for your platform to `lib/javafx/`
- Sets up the development environment

#### Step 2: Build Application

```bash
# macOS/Linux
./scripts/build.sh

# Windows  
.\scripts\build.bat
```

#### Step 3: Run Application

```bash
# macOS/Linux
./scripts/run.sh

# Windows
.\scripts\run.bat
```

### Method 3: Manual Setup (Advanced Users)

<details>
<summary>Click to expand manual setup instructions</summary>

#### Download Libraries Manually

**macOS/Linux:**
```bash
chmod +x download-libs.sh
./download-libs.sh
```

**Windows:**
```powershell
powershell -ExecutionPolicy Bypass -File download-libs.ps1
```

#### Download JavaFX 21.0.9 Manually

**⚠️ Important:** JavaFX contains platform-specific native libraries. You MUST download the correct version for your OS.

**Visit:** https://gluonhq.com/products/javafx/

**Download for your platform:**
- macOS (Apple Silicon): `openjfx-21.0.9_osx-aarch64_bin-sdk.zip`
- macOS (Intel): `openjfx-21.0.9_osx-x64_bin-sdk.zip`  
- Windows: `openjfx-21.0.9_windows-x64_bin-sdk.zip`
- Linux: `openjfx-21.0.9_linux-x64_bin-sdk.zip`

**Extract and install:**
1. Extract the downloaded ZIP file
2. Navigate to `javafx-sdk-21.0.9/lib/` folder
3. Copy all `.jar` files to `pcm-desktop/lib/javafx/`

#### Verify Libraries

```bash
ls -l lib/javafx/    # Should have 8 JAR files + native libraries
ls -l lib/others/    # Should have 14 JAR files
ls -l lib/text-component/    # Should have 6 JAR files for rich text
```

</details>

## 🔧 IDE Setup (Optional)

After running the application successfully with scripts, you can optionally set up your IDE for development:

### IntelliJ IDEA (Recommended)

1. **Open Project:**
   - File → Open → Select `pcm-desktop` folder

2. **Add Libraries:**
   - File → Project Structure (⌘;) → Libraries
   - Click `+` → Java → Select `lib/javafx` → Add all JARs
   - Click `+` → Java → Select `lib/others` → Add all JARs  
   - Click `+` → Java → Select `lib/text-component` → Add all JARs
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

### Other IDEs

For Eclipse, VS Code, and other IDEs, see:
- **[IntelliJ Setup Guide](docs/setup/intellij-setup.md)** - Detailed IntelliJ configuration
- **[Library Setup Guide](docs/setup/library-setup.md)** - Multi-IDE library setup
- **[Run Configuration Guide](docs/setup/run-configuration.md)** - Run configuration for all IDEs

## 📖 Documentation

Complete documentation is organized in the `docs/` folder:

### 🚀 Getting Started
- **[Quick Start Guide](docs/getting-started/quick-start.md)** - Get up and running fast
- **[README](docs/getting-started/README.md)** - Getting started overview

### ⚙️ Setup & Configuration  
- **[IntelliJ Setup](docs/setup/intellij-setup.md)** - Complete IntelliJ IDEA configuration
- **[Library Setup](docs/setup/library-setup.md)** - Multi-IDE library setup guide
- **[Run Configuration](docs/setup/run-configuration.md)** - Run configurations for all IDEs

### 🔌 Integrations
- **[API Integration](docs/integrations/api/api-guide.md)** - LLM API integration guide
- **[API Quick Reference](docs/integrations/api/api-quick-reference.md)** - Quick API reference
- **[SSO Integration](docs/integrations/sso/sso-integration-guide.md)** - Single Sign-On setup
- **[Database Integration](docs/integrations/database/README.md)** - Database setup and migration

### 🏗️ Architecture & Development
- **[Architecture Overview](docs/architecture/system-overview.md)** - System architecture
- **[Development Guide](docs/development/README.md)** - Development guidelines

### 🎨 UI Components
- **[AtlantaFX Integration](docs/ui-components/atlantafx-integration.md)** - Theme system
- **[UI Components](docs/ui-components/README.md)** - Component documentation

### 🔧 Troubleshooting  
- **[Troubleshooting Guide](docs/troubleshooting/README.md)** - Common issues and solutions

## 🏃 Run from Command Line

### Simple Scripts (Recommended)

```bash
# Build the application
./scripts/build.sh

# Run the application  
./scripts/run.sh
```

### Manual Compilation

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

### Manual Run

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
| Jackson | 2.18.2 | All Java versions ✅ | https://github.com/FasterXML/jackson |
| SLF4J | 2.0.16 | All Java versions ✅ | https://www.slf4j.org/ |
| Logback | 1.5.12 | All Java versions ✅ | https://logback.qos.ch/ |
| SQLite JDBC | 3.47.1.0 | All Java versions ✅ | https://github.com/xerial/sqlite-jdbc |

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

**✅ No Build Tools Required - Pure JAR Import with Automated Scripts!**

**🚀 Ready to Start:**

```bash
# 🎯 One-Line Start (Recommended)
# macOS/Linux
./scripts/setup.sh && ./scripts/build.sh && ./scripts/run.sh

# Windows  
.\scripts\setup.bat && .\scripts\build.bat && .\scripts\run.bat

# 📝 Step by Step (Alternative)
# macOS/Linux
./scripts/setup.sh    # Auto-download all dependencies + JavaFX
./scripts/build.sh    # Compile application  
./scripts/run.sh      # Launch PCM Desktop

# Windows
.\scripts\setup.bat   # Auto-download all dependencies + JavaFX
.\scripts\build.bat   # Compile application
.\scripts\run.bat     # Launch PCM Desktop
```

**✨ What makes this special:**
- 🚫 **No Maven/Gradle** - Simple JAR imports
- 🤖 **Auto-Setup Scripts** - Downloads everything automatically
- 🎯 **Cross-Platform** - Works on macOS, Linux, Windows
- 📦 **28 JARs Total** - JavaFX (8) + Core libs (14) + Text components (6)
- ⚡ **Fast Development** - Pure Java, no complex build configs

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

## 🔌 API Integration

PCM Desktop cung cấp hệ thống tích hợp API mạnh mẽ để gọi các dịch vụ LLM:

### Quick Start
```java
// Initialize LLM service
LLMService llmService = new LLMService();

// Configure provider (OpenAI example)
LLMProviderConfig config = LLMProviderConfig.builder()
    .provider(LLMProviderConfig.Provider.OPENAI)
    .url("https://api.openai.com/v1/chat/completions")
    .token(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
    .build();

llmService.initialize(config);

// Simple chat
String response = llmService.chat("Explain Java Streams");
```

### Supported Providers
- ✅ **OpenAI** - GPT-4, GPT-3.5-turbo với streaming và function calling
- ✅ **Anthropic** - Claude 3.5 Sonnet với advanced reasoning  
- ✅ **Ollama** - Local models như Llama 3, Mistral
- ✅ **Custom** - Bất kỳ API tương thích nào

### Features
- 🌊 **Streaming Responses** - Real-time response streaming
- 🔧 **Function Calling** - AI có thể gọi external functions
- 💬 **Multi-turn Conversations** - Context-aware conversations
- 🔄 **Provider Switching** - Dễ dàng chuyển đổi giữa providers
- ⚡ **Async Support** - Non-blocking operations
- 🔒 **Error Handling** - Robust error handling và retry logic

### Demo & Documentation
- 📖 **[API Integration Guide](docs/API_INTEGRATION_GUIDE.md)** - Hướng dẫn chi tiết
- 🚀 **[Quick Reference](docs/API_QUICK_REFERENCE.md)** - Tham khảo nhanh
- 🎮 **Interactive Demo**: `./scripts/run-api-demo.sh` (macOS/Linux) hoặc `scripts\run-api-demo.bat` (Windows)

### Environment Setup
```bash
# Set API keys
export OPENAI_API_KEY=your-openai-key
export ANTHROPIC_API_KEY=your-anthropic-key

# Run API demo
./scripts/run-api-demo.sh

# Run SSO integration demo
./scripts/run-sso-demo.sh
```

## 🔐 Single Sign-On (SSO) Integration

PCM Desktop hỗ trợ tích hợp với hệ thống SSO tự động để sử dụng tokens từ enterprise login systems:

### Supported Token Sources
- 🍪 **Browser Cookies** - Chrome, Edge, Firefox cookies
- 💾 **Browser localStorage** - Tokens stored in browser storage  
- 🏢 **Windows Registry** - Enterprise registry-based SSO
- 📁 **Shared Files** - JSON, properties, text files
- 🔄 **Auto-refresh** - Automatic token renewal

### Quick SSO Setup
```java
// Initialize SSO token manager
SSOTokenManager ssoManager = SSOTokenManager.getInstance();

// Configure LLM with SSO
SSOLLMProviderConfig config = SSOLLMProviderConfig.builder()
    .provider(LLMProviderConfig.Provider.OPENAI)
    .url("https://api.openai.com/v1/chat/completions")
    .ssoServiceName("company-portal")
    .useSSOToken(true)
    .fallbackToken(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
    .build();

LLMService llmService = new LLMService();
llmService.initialize(config);

// Use automatically with SSO tokens
String response = llmService.chat("Hello from SSO user!");
```

### Features
- 🔍 **Multi-source Extraction** - Automatically finds tokens from multiple sources
- 🔒 **Secure Storage** - Encrypted token caching with expiration
- 📊 **Audit Logging** - Complete security audit trail
- ⚡ **Auto-refresh** - Handles token expiration gracefully
- 🌐 **Cross-platform** - Windows, macOS, Linux support

### Documentation & Demo
- 📖 **[SSO Integration Guide](docs/SSO_INTEGRATION_GUIDE.md)** - Complete implementation guide
- 🎮 **Interactive Demo**: `./scripts/run-sso-demo.sh` (macOS/Linux) hoặc `scripts\run-sso-demo.bat` (Windows)

---

**PCM Desktop - AI-Powered System Analysis for Enterprise Software**
