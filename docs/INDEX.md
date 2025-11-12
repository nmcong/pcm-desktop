# 📚 PCM Desktop - Cấu Trúc Tài Liệu

> Cập nhật: 12/11/2025

## 📂 Cấu Trúc Thư Mục

```
docs/
├── README.md                    # Tổng quan dự án và hướng dẫn tài liệu
├── CHANGELOG.md                 # Lịch sử thay đổi dự án
├── INDEX.md                     # File này - chỉ mục tài liệu
│
├── setup/                       # 🔧 Hướng dẫn cài đặt
│   ├── INTELLIJ_SETUP.md       # Cài đặt IntelliJ IDEA
│   ├── LIBRARY_SETUP.md        # Cài đặt thư viện
│   ├── RUN_CONFIGURATION_INSTRUCTIONS.md  # Cấu hình chạy
│   └── SETUP_WINDOWS.md        # Cài đặt trên Windows
│
├── guides/                      # 📖 Hướng dẫn sử dụng
│   ├── QUICK_START.md          # Bắt đầu nhanh
│   ├── QUICK_START_WINDOWS.md  # Bắt đầu nhanh trên Windows
│   ├── STEP_BY_STEP_GUIDE.md   # Hướng dẫn từng bước
│   ├── MIGRATION_GUIDE.md      # Hướng dẫn migration
│   ├── PCM_CONCEPT.md          # Khái niệm PCM
│   │
│   └── integration/            # 🔗 Hướng dẫn tích hợp
│       ├── API_INTEGRATION_GUIDE.md      # Tích hợp API LLM
│       ├── API_QUICK_REFERENCE.md        # Tham khảo nhanh API
│       ├── DATABASE_README.md            # Database overview
│       ├── DATABASE_MIGRATION_GUIDE.md   # Hướng dẫn migration DB
│       ├── SSO_INTEGRATION_GUIDE_VI.md   # SSO integration (tiếng Việt)
│       ├── SSO_INTEGRATION_GUIDE_DETAILED.md  # SSO chi tiết (English)
│       └── SSO_QUICK_START.md            # SSO bắt đầu nhanh
│
├── development/                 # 💻 Tài liệu phát triển
│   ├── PROJECT_SUMMARY.md      # Tổng quan dự án
│   │
│   ├── ai-assistant/           # 🤖 AI Assistant Development
│   │   ├── AI_ASSISTANT_REFACTOR_README.md
│   │   ├── AI_ASSISTANT_REFACTOR_PLAN.md
│   │   ├── AI_ASSISTANT_REFACTOR_STATUS.md
│   │   ├── AI_ASSISTANT_REFACTOR_COMPLETE.md
│   │   ├── AI_ASSISTANT_REFACTOR_SUMMARY.md
│   │   └── AI_ASSISTANT_REFACTORING_SUMMARY.md
│   │
│   ├── llm/                    # 🧠 LLM Integration
│   │   ├── LLM_README.md
│   │   ├── LLM_QUICK_START.md
│   │   ├── LLM_INTEGRATION_PLAN.md
│   │   ├── LLM_INTEGRATION_COMPLETE.md
│   │   ├── LLM_IMPLEMENTATION_STATUS.md
│   │   ├── LLM_PHASES_COMPLETE.md
│   │   ├── LLM_COMPLETE_SUMMARY.md
│   │   └── RAG_IMPLEMENTATION_PLAN.md
│   │
│   ├── database/               # 🗄️ Database Development
│   │   ├── DATABASE_QUICK_START.md
│   │   └── SQLITE_IMPLEMENTATION_PLAN.md
│   │
│   └── ui/                     # 🎨 UI/UX Development
│       ├── ATLANTAFX_REFACTOR.md
│       ├── IKONLI_INTEGRATION.md
│       ├── PHASE_2_UI_INTEGRATION_COMPLETE.md
│       └── PHASE_2_FINAL_SUMMARY.md
│
└── troubleshooting/            # 🔧 Khắc phục sự cố
    ├── TROUBLESHOOTING.md      # Khắc phục sự cố chung
    └── QUICK_FIX_GUIDE.md      # Sửa lỗi nhanh
```

## 🚀 Bắt Đầu Nhanh

### Người Dùng Mới

1. **[QUICK_START.md](guides/QUICK_START.md)** - Bắt đầu sử dụng PCM Desktop
2. **[PCM_CONCEPT.md](guides/PCM_CONCEPT.md)** - Hiểu khái niệm PCM
3. **[STEP_BY_STEP_GUIDE.md](guides/STEP_BY_STEP_GUIDE.md)** - Hướng dẫn chi tiết

### Developer Mới

1. **[setup/INTELLIJ_SETUP.md](setup/INTELLIJ_SETUP.md)** - Cài đặt môi trường phát triển
2. **[setup/LIBRARY_SETUP.md](setup/LIBRARY_SETUP.md)** - Cài đặt thư viện
3. **[development/PROJECT_SUMMARY.md](development/PROJECT_SUMMARY.md)** - Tổng quan kiến trúc dự án

### Tích Hợp API/LLM

1. **[guides/integration/API_INTEGRATION_GUIDE.md](guides/integration/API_INTEGRATION_GUIDE.md)** - Hướng dẫn chi tiết tích hợp API
2. **[guides/integration/API_QUICK_REFERENCE.md](guides/integration/API_QUICK_REFERENCE.md)** - Tham khảo nhanh
3. **[development/llm/LLM_QUICK_START.md](development/llm/LLM_QUICK_START.md)** - Bắt đầu với LLM

## 📋 Tài Liệu Theo Chủ Đề

### 🔧 Cài Đặt & Setup
- [INTELLIJ_SETUP.md](setup/INTELLIJ_SETUP.md) - Cài đặt IntelliJ IDEA
- [LIBRARY_SETUP.md](setup/LIBRARY_SETUP.md) - Cài đặt thư viện cần thiết
- [RUN_CONFIGURATION_INSTRUCTIONS.md](setup/RUN_CONFIGURATION_INSTRUCTIONS.md) - Cấu hình chạy ứng dụng
- [SETUP_WINDOWS.md](setup/SETUP_WINDOWS.md) - Hướng dẫn cài đặt trên Windows

### 📖 Hướng Dẫn Sử Dụng
- [QUICK_START.md](guides/QUICK_START.md) - Hướng dẫn bắt đầu nhanh
- [QUICK_START_WINDOWS.md](guides/QUICK_START_WINDOWS.md) - Bắt đầu nhanh trên Windows
- [STEP_BY_STEP_GUIDE.md](guides/STEP_BY_STEP_GUIDE.md) - Hướng dẫn từng bước chi tiết
- [PCM_CONCEPT.md](guides/PCM_CONCEPT.md) - Hiểu về khái niệm PCM
- [MIGRATION_GUIDE.md](guides/MIGRATION_GUIDE.md) - Hướng dẫn migration

### 🔗 Tích Hợp
- [API_INTEGRATION_GUIDE.md](guides/integration/API_INTEGRATION_GUIDE.md) - Hướng dẫn tích hợp API LLM
- [API_QUICK_REFERENCE.md](guides/integration/API_QUICK_REFERENCE.md) - Tham khảo nhanh API
- [DATABASE_README.md](guides/integration/DATABASE_README.md) - Tổng quan Database
- [DATABASE_MIGRATION_GUIDE.md](guides/integration/DATABASE_MIGRATION_GUIDE.md) - Migration database
- [SSO_INTEGRATION_GUIDE_VI.md](guides/integration/SSO_INTEGRATION_GUIDE_VI.md) - Tích hợp SSO (Tiếng Việt)
- [SSO_INTEGRATION_GUIDE_DETAILED.md](guides/integration/SSO_INTEGRATION_GUIDE_DETAILED.md) - SSO chi tiết (English)
- [SSO_QUICK_START.md](guides/integration/SSO_QUICK_START.md) - Bắt đầu nhanh với SSO

### 🤖 AI Assistant Development
- [AI_ASSISTANT_REFACTOR_README.md](development/ai-assistant/AI_ASSISTANT_REFACTOR_README.md) - Tổng quan refactor AI Assistant
- [AI_ASSISTANT_REFACTOR_PLAN.md](development/ai-assistant/AI_ASSISTANT_REFACTOR_PLAN.md) - Kế hoạch refactor
- [AI_ASSISTANT_REFACTOR_STATUS.md](development/ai-assistant/AI_ASSISTANT_REFACTOR_STATUS.md) - Trạng thái refactor
- [AI_ASSISTANT_REFACTOR_COMPLETE.md](development/ai-assistant/AI_ASSISTANT_REFACTOR_COMPLETE.md) - Hoàn thành refactor
- [AI_ASSISTANT_REFACTOR_SUMMARY.md](development/ai-assistant/AI_ASSISTANT_REFACTOR_SUMMARY.md) - Tóm tắt refactor
- [AI_ASSISTANT_REFACTORING_SUMMARY.md](development/ai-assistant/AI_ASSISTANT_REFACTORING_SUMMARY.md) - Tóm tắt chi tiết

### 🧠 LLM Integration
- [LLM_README.md](development/llm/LLM_README.md) - Tổng quan LLM integration
- [LLM_QUICK_START.md](development/llm/LLM_QUICK_START.md) - Bắt đầu nhanh với LLM
- [LLM_INTEGRATION_PLAN.md](development/llm/LLM_INTEGRATION_PLAN.md) - Kế hoạch tích hợp LLM
- [LLM_INTEGRATION_COMPLETE.md](development/llm/LLM_INTEGRATION_COMPLETE.md) - Hoàn thành tích hợp
- [LLM_IMPLEMENTATION_STATUS.md](development/llm/LLM_IMPLEMENTATION_STATUS.md) - Trạng thái triển khai
- [LLM_PHASES_COMPLETE.md](development/llm/LLM_PHASES_COMPLETE.md) - Các giai đoạn hoàn thành
- [LLM_COMPLETE_SUMMARY.md](development/llm/LLM_COMPLETE_SUMMARY.md) - Tóm tắt hoàn chỉnh
- [RAG_IMPLEMENTATION_PLAN.md](development/llm/RAG_IMPLEMENTATION_PLAN.md) - Kế hoạch RAG

### 🗄️ Database Development
- [DATABASE_QUICK_START.md](development/database/DATABASE_QUICK_START.md) - Bắt đầu nhanh với Database
- [SQLITE_IMPLEMENTATION_PLAN.md](development/database/SQLITE_IMPLEMENTATION_PLAN.md) - Kế hoạch SQLite

### 🎨 UI/UX Development
- [ATLANTAFX_REFACTOR.md](development/ui/ATLANTAFX_REFACTOR.md) - Refactor với AtlantaFX
- [IKONLI_INTEGRATION.md](development/ui/IKONLI_INTEGRATION.md) - Tích hợp Ikonli icons
- [PHASE_2_UI_INTEGRATION_COMPLETE.md](development/ui/PHASE_2_UI_INTEGRATION_COMPLETE.md) - Hoàn thành Phase 2 UI
- [PHASE_2_FINAL_SUMMARY.md](development/ui/PHASE_2_FINAL_SUMMARY.md) - Tóm tắt Phase 2

### 🔧 Troubleshooting
- [TROUBLESHOOTING.md](troubleshooting/TROUBLESHOOTING.md) - Khắc phục sự cố chung
- [QUICK_FIX_GUIDE.md](troubleshooting/QUICK_FIX_GUIDE.md) - Hướng dẫn sửa lỗi nhanh

## 📊 Tiến Độ Dự Án

### ✅ Đã Hoàn Thành
- ✅ Cấu trúc dự án cơ bản
- ✅ Tích hợp Database (SQLite)
- ✅ Tích hợp LLM (OpenAI, Anthropic, Ollama)
- ✅ AI Assistant Page
- ✅ UI/UX với AtlantaFX
- ✅ Icon system với Ikonli
- ✅ SSO Integration

### 🚧 Đang Phát Triển
- 🚧 RAG Implementation
- 🚧 Vector Database Integration
- 🚧 Advanced AI Features

### 📋 Kế Hoạch
- 📋 Multi-language support
- 📋 Plugin system
- 📋 Advanced analytics

## 🤝 Đóng Góp

### Thêm Tài Liệu Mới

1. Xác định loại tài liệu:
   - **Setup**: Đặt trong `setup/`
   - **Hướng dẫn sử dụng**: Đặt trong `guides/`
   - **Tích hợp**: Đặt trong `guides/integration/`
   - **Phát triển**: Đặt trong `development/[category]/`

2. Đặt tên file theo quy ước:
   - Sử dụng UPPERCASE và underscore: `MY_DOCUMENT.md`
   - Tên file mô tả rõ nội dung
   - Thêm prefix nếu cần: `LLM_`, `API_`, `DATABASE_`

3. Cập nhật file INDEX.md này

4. Cập nhật README.md nếu cần

### Quy Tắc Viết Tài Liệu

- ✅ Sử dụng Markdown chuẩn
- ✅ Thêm mục lục cho tài liệu dài
- ✅ Sử dụng code blocks với syntax highlighting
- ✅ Thêm ví dụ cụ thể
- ✅ Thêm screenshots/diagrams nếu cần
- ✅ Cập nhật ngày tháng

## 📞 Liên Hệ & Hỗ Trợ

- **Issues**: [GitHub Issues](https://github.com/your-repo/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-repo/discussions)
- **Email**: support@noteflix.com

---

**Cập nhật lần cuối**: 12/11/2025  
**Phiên bản**: 1.0.0
