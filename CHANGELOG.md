# PCM Desktop - Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased] - 2025-11-11

### 🔄 Changed

#### Library Updates
- **Jackson:** 2.17.2 → 2.18.2
  - Security fixes and performance improvements
  - Backward compatible with Java 21
- **SLF4J:** 2.0.13 → 2.0.16
  - Bug fixes and stability improvements
- **Logback:** 1.5.6 → 1.5.12
  - Enhanced logging performance
  - Bug fixes
- **SQLite JDBC:** 3.46.1.0 → 3.47.1.0
  - Performance improvements in concurrent access
  - Bug fixes

#### Application Rebranding
- **PCM** now stands for "**Project Code Management**" (was "Personal Content Manager")
- Updated application description to "AI-Powered System Analysis & Business Management Tool"
- Changed main window title to reflect new purpose
- Updated all documentation to reflect new vision

### ✨ Added

#### Documentation
- **`docs/PCM_CONCEPT.md`** - Comprehensive system concept and architecture document
  - Detailed explanation of all system components
  - Use cases for different user roles
  - Data model and relationships
  - Example queries and expected behavior
  
- **`docs/RAG_IMPLEMENTATION_PLAN.md`** - Complete RAG implementation roadmap
  - 8-week implementation plan
  - Technology stack recommendations
  - Architecture overview
  - Cost estimation
  - Success criteria

- **`docs/LIBRARY_VERSIONS_CHECK.md`** - Library version tracking
  - Current vs. latest versions comparison
  - Update recommendations
  - Migration notes
  - Direct download links

#### Scripts & Tools
- **Windows Support:**
  - `compile-windows.bat` - Compile Java source on Windows
  - `run-windows.bat` - Run application on Windows
  - Updated `download-libs.ps1` to auto-download JavaFX 21.0.9

#### Features
- **New Navigation Menu:**
  - 📊 Dashboard
  - 🏗️ Subsystems & Projects
  - 🖥️ Screens & Forms
  - 🗄️ Database Objects
  - ⚙️ Batch Jobs
  - 🔄 Workflows
  - 📚 Knowledge Base
  - 🤖 AI Query
  - ⚙️ Settings

- **Enhanced Welcome Screen:**
  - Updated title and subtitle
  - Added descriptive text about PCM capabilities
  - Improved layout and styling

- **Updated About Dialog:**
  - New branding
  - Updated version information
  - Copyright notice

### 📝 Updated

#### Source Code
- **`PCMApplication.java`**
  - Updated JavaDoc with comprehensive description
  - Changed window title
  - Increased default window size (1400x900)

- **`MainController.java`**
  - Updated controller documentation
  - New navigation categories
  - Enhanced welcome message
  - Updated about dialog

#### Documentation Files
- **`README.md`**
  - Complete rewrite of overview section
  - Updated features list
  - Added use cases section
  - Updated tech stack
  - Enhanced troubleshooting section
  - Added platform-specific notes

- **`docs/LIBRARY_SETUP.md`**
  - Updated all library versions
  - Updated download URLs
  - Updated example code snippets

- **Download Scripts:**
  - `download-libs.sh` - Updated library versions
  - `download-libs.ps1` - Updated library versions + JavaFX auto-download

### 🐛 Fixed

#### JavaFX Version Compatibility
- Clarified JavaFX 21.0.9 requirement (not 23 or 25)
- Added platform-specific download instructions
- Explained why JavaFX JARs are platform-specific
- Updated all documentation to use correct version

### 🔒 Security

- Updated Jackson to 2.18.2 (addresses known vulnerabilities in 2.17.x)
- All libraries updated to latest stable versions

---

## [1.0.0] - 2025-11-10

### Initial Release

- Basic JavaFX application structure
- Main window with navigation tree
- Welcome screen
- Menu bar with File and Help menus
- SQLite database support
- Logging with Logback
- Domain-Driven Design architecture
- Clean separation of concerns (UI, Application, Domain, Infrastructure)

---

## Library Version Matrix

| Library | Current | Previous | Status |
|---------|---------|----------|--------|
| Java | 21 | 21 | ✅ Current |
| JavaFX | 21.0.9 | 21.0.9 | ✅ Current |
| Lombok | 1.18.34 | 1.18.34 | ✅ Current |
| Jackson | 2.18.2 | 2.17.2 | ⬆️ Upgraded |
| SLF4J | 2.0.16 | 2.0.13 | ⬆️ Upgraded |
| Logback | 1.5.12 | 1.5.6 | ⬆️ Upgraded |
| SQLite JDBC | 3.47.1.0 | 3.46.1.0 | ⬆️ Upgraded |

---

## Upgrade Instructions

To upgrade from previous version:

1. **Backup** your current `lib/others` folder
2. **Delete** old JAR files:
   ```bash
   rm lib/others/jackson-*.jar
   rm lib/others/slf4j-*.jar
   rm lib/others/logback-*.jar
   rm lib/others/sqlite-jdbc-*.jar
   ```
3. **Download** new libraries:
   ```bash
   # macOS/Linux
   ./download-libs.sh
   
   # Windows
   .\download-libs.ps1
   ```
4. **Refresh** IDE libraries (IntelliJ: File → Invalidate Caches → Restart)
5. **Rebuild** project
6. **Test** application functionality

---

## Future Plans

### Version 1.1.0 (Planned Q1 2026)
- RAG implementation (Phase 1)
- AI query interface
- Basic code analysis
- Database schema indexing

### Version 1.2.0 (Planned Q2 2026)
- Advanced RAG features
- Workflow visualization
- Batch job management
- Knowledge base integration

### Version 2.0.0 (Planned Q3 2026)
- Full RAG integration
- Multi-LLM support
- Advanced analytics
- Export/import functionality

---

## Breaking Changes

None in this release. All updates are backward compatible.

---

## Known Issues

- None reported

---

## Contributors

- Noteflix Team

---

**Last Updated:** November 11, 2025

