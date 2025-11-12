# Kế Hoạch Sắp Xếp Lại Docs

## Cấu Trúc Mới Đề Xuất

```
docs/
├── README.md                           # Main documentation index
├── CHANGELOG.md                        # Project changelog
│
├── 📚 getting-started/                 # Khởi đầu với PCM Desktop  
│   ├── README.md                       # Getting started overview
│   ├── installation.md                 # Installation guide
│   ├── quick-start.md                  # Quick start guide
│   ├── first-steps.md                  # First steps tutorial
│   └── system-requirements.md          # System requirements
│
├── 🔧 setup/                           # Thiết lập môi trường phát triển
│   ├── README.md                       # Setup overview
│   ├── development-environment.md      # Dev environment setup
│   ├── intellij-setup.md              # IntelliJ configuration
│   ├── library-setup.md               # Library dependencies
│   └── run-configuration.md            # Run configurations
│
├── 🏗️ architecture/                    # Kiến trúc và thiết kế
│   ├── README.md                       # Architecture overview
│   ├── system-overview.md              # System architecture
│   ├── component-design.md             # Component design
│   ├── database-design.md              # Database architecture
│   └── ui-architecture.md              # UI architecture (AtlantaFX)
│
├── 🔌 integrations/                    # Tích hợp với hệ thống bên ngoài
│   ├── README.md                       # Integrations overview
│   ├── api/                           # API integrations
│   │   ├── README.md                   # API integration overview
│   │   ├── llm-integration.md          # LLM integration guide
│   │   ├── api-guide.md               # Complete API guide
│   │   └── api-quick-reference.md      # Quick reference
│   ├── sso/                           # Single Sign-On
│   │   ├── README.md                   # SSO overview
│   │   ├── sso-integration-guide.md    # Complete SSO guide
│   │   └── sso-quick-start.md          # SSO quick start
│   └── database/                       # Database integrations
│       ├── README.md                   # Database overview
│       ├── sqlite-setup.md             # SQLite setup
│       ├── migration-guide.md          # Migration guide
│       └── database-quick-start.md     # Database quick start
│
├── 🎨 ui-components/                   # UI Components và theming
│   ├── README.md                       # UI components overview
│   ├── atlantafx-integration.md        # AtlantaFX integration
│   ├── theming-guide.md               # Theming and styling
│   ├── component-development.md        # Custom component development
│   └── ikonli-icons.md                # Icon system (Ikonli)
│
├── 🔄 development/                     # Development workflows
│   ├── README.md                       # Development overview
│   ├── project-structure.md            # Project structure
│   ├── coding-standards.md             # Coding standards
│   ├── testing-guide.md               # Testing guidelines
│   └── release-process.md              # Release process
│
├── 📖 user-guides/                     # Hướng dẫn người dùng
│   ├── README.md                       # User guides overview
│   ├── end-user-manual.md             # End user manual
│   ├── admin-guide.md                 # Administrator guide
│   └── features-overview.md            # Features overview
│
├── 🚨 troubleshooting/                # Xử lý sự cố
│   ├── README.md                       # Troubleshooting overview
│   ├── common-issues.md               # Common issues and solutions
│   ├── quick-fixes.md                 # Quick fixes
│   ├── debugging-guide.md             # Debugging guide
│   └── faq.md                         # Frequently asked questions
│
├── 📜 legacy/                          # Tài liệu cũ (deprecated)
│   ├── README.md                       # Legacy docs explanation
│   └── old-docs/                      # Old documentation files
│       ├── refactoring-summaries/     # Old refactoring docs
│       ├── implementation-plans/      # Old implementation plans
│       └── archived-guides/           # Archived guides
│
└── 📋 references/                     # Tài liệu tham khảo
    ├── README.md                       # References overview
    ├── api-reference.md               # API reference
    ├── configuration-reference.md     # Configuration reference
    ├── glossary.md                    # Glossary of terms
    └── external-resources.md          # External resources and links
```

## Phân Loại Files Hiện Tại

### 🟢 Core Documentation (Keep & Reorganize)
- API_INTEGRATION_GUIDE.md → integrations/api/api-guide.md
- API_QUICK_REFERENCE.md → integrations/api/api-quick-reference.md
- SSO_INTEGRATION_GUIDE.md → integrations/sso/sso-integration-guide.md
- DATABASE_README.md → integrations/database/README.md
- DATABASE_MIGRATION_GUIDE.md → integrations/database/migration-guide.md

### 🟡 Setup & Configuration (Reorganize)
- guides/QUICK_START.md → getting-started/quick-start.md
- guides/SETUP_WINDOWS.md → setup/windows-setup.md
- setup/INTELLIJ_SETUP.md → setup/intellij-setup.md
- setup/LIBRARY_SETUP.md → setup/library-setup.md
- setup/RUN_CONFIGURATION_INSTRUCTIONS.md → setup/run-configuration.md

### 🟠 Architecture & Design (Reorganize)
- guides/PCM_CONCEPT.md → architecture/system-overview.md
- ATLANTAFX_REFACTOR.md → ui-components/atlantafx-integration.md
- IKONLI_INTEGRATION.md → ui-components/ikonli-icons.md

### 🔴 Legacy/Outdated (Move to Legacy)
- AI_ASSISTANT_REFACTOR_* → legacy/old-docs/refactoring-summaries/
- LLM_IMPLEMENTATION_STATUS.md → legacy/old-docs/implementation-plans/
- PHASE_2_* → legacy/old-docs/implementation-plans/
- development/*.md (most) → legacy/old-docs/

### ⚫ Keep in Root
- README.md (update to be main index)
- CHANGELOG.md

## Migration Steps

1. Create new folder structure
2. Move and rename files according to plan
3. Update all internal links and references
4. Create new README files for each section
5. Update main README.md with new structure
6. Remove duplicate and outdated files