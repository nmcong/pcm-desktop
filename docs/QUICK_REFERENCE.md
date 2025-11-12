# 🚀 Quick Reference - Tài Liệu PCM Desktop

> Tham khảo nhanh để tìm tài liệu bạn cần

## 📍 Tôi Muốn...

### Cài Đặt & Setup

| Tôi muốn... | Xem file... |
|-------------|-------------|
| Cài đặt IntelliJ IDEA | [setup/INTELLIJ_SETUP.md](setup/INTELLIJ_SETUP.md) |
| Cài đặt thư viện | [setup/LIBRARY_SETUP.md](setup/LIBRARY_SETUP.md) |
| Cấu hình chạy ứng dụng | [setup/RUN_CONFIGURATION_INSTRUCTIONS.md](setup/RUN_CONFIGURATION_INSTRUCTIONS.md) |
| Setup trên Windows | [setup/SETUP_WINDOWS.md](setup/SETUP_WINDOWS.md) |

### Bắt Đầu Sử Dụng

| Tôi muốn... | Xem file... |
|-------------|-------------|
| Bắt đầu nhanh | [guides/QUICK_START.md](guides/QUICK_START.md) |
| Bắt đầu nhanh trên Windows | [guides/QUICK_START_WINDOWS.md](guides/QUICK_START_WINDOWS.md) |
| Hướng dẫn từng bước | [guides/STEP_BY_STEP_GUIDE.md](guides/STEP_BY_STEP_GUIDE.md) |
| Hiểu khái niệm PCM | [guides/PCM_CONCEPT.md](guides/PCM_CONCEPT.md) |

### Tích Hợp API/LLM

| Tôi muốn... | Xem file... |
|-------------|-------------|
| Tích hợp LLM (chi tiết) | [guides/integration/API_INTEGRATION_GUIDE.md](guides/integration/API_INTEGRATION_GUIDE.md) |
| Tham khảo nhanh API | [guides/integration/API_QUICK_REFERENCE.md](guides/integration/API_QUICK_REFERENCE.md) |
| Setup OpenAI/Claude/Ollama | [development/llm/LLM_QUICK_START.md](development/llm/LLM_QUICK_START.md) |
| Kế hoạch LLM integration | [development/llm/LLM_INTEGRATION_PLAN.md](development/llm/LLM_INTEGRATION_PLAN.md) |
| RAG implementation | [development/llm/RAG_IMPLEMENTATION_PLAN.md](development/llm/RAG_IMPLEMENTATION_PLAN.md) |

### Database

| Tôi muốn... | Xem file... |
|-------------|-------------|
| Bắt đầu với Database | [development/database/DATABASE_QUICK_START.md](development/database/DATABASE_QUICK_START.md) |
| Database overview | [guides/integration/DATABASE_README.md](guides/integration/DATABASE_README.md) |
| Migration guide | [guides/integration/DATABASE_MIGRATION_GUIDE.md](guides/integration/DATABASE_MIGRATION_GUIDE.md) |
| SQLite implementation | [development/database/SQLITE_IMPLEMENTATION_PLAN.md](development/database/SQLITE_IMPLEMENTATION_PLAN.md) |

### SSO Integration

| Tôi muốn... | Xem file... |
|-------------|-------------|
| SSO quick start | [guides/integration/SSO_QUICK_START.md](guides/integration/SSO_QUICK_START.md) |
| SSO guide (Tiếng Việt) | [guides/integration/SSO_INTEGRATION_GUIDE_VI.md](guides/integration/SSO_INTEGRATION_GUIDE_VI.md) |
| SSO detailed (English) | [guides/integration/SSO_INTEGRATION_GUIDE_DETAILED.md](guides/integration/SSO_INTEGRATION_GUIDE_DETAILED.md) |

### AI Assistant Development

| Tôi muốn... | Xem file... |
|-------------|-------------|
| Overview refactoring | [development/ai-assistant/AI_ASSISTANT_REFACTOR_README.md](development/ai-assistant/AI_ASSISTANT_REFACTOR_README.md) |
| Kế hoạch refactor | [development/ai-assistant/AI_ASSISTANT_REFACTOR_PLAN.md](development/ai-assistant/AI_ASSISTANT_REFACTOR_PLAN.md) |
| Trạng thái hiện tại | [development/ai-assistant/AI_ASSISTANT_REFACTOR_STATUS.md](development/ai-assistant/AI_ASSISTANT_REFACTOR_STATUS.md) |
| Tóm tắt refactor | [development/ai-assistant/AI_ASSISTANT_REFACTORING_SUMMARY.md](development/ai-assistant/AI_ASSISTANT_REFACTORING_SUMMARY.md) |

### UI/UX Development

| Tôi muốn... | Xem file... |
|-------------|-------------|
| AtlantaFX refactor | [development/ui/ATLANTAFX_REFACTOR.md](development/ui/ATLANTAFX_REFACTOR.md) |
| Ikonli icons | [development/ui/IKONLI_INTEGRATION.md](development/ui/IKONLI_INTEGRATION.md) |
| Phase 2 UI summary | [development/ui/PHASE_2_FINAL_SUMMARY.md](development/ui/PHASE_2_FINAL_SUMMARY.md) |

### Khắc Phục Sự Cố

| Tôi muốn... | Xem file... |
|-------------|-------------|
| Troubleshooting chung | [troubleshooting/TROUBLESHOOTING.md](troubleshooting/TROUBLESHOOTING.md) |
| Quick fix guide | [troubleshooting/QUICK_FIX_GUIDE.md](troubleshooting/QUICK_FIX_GUIDE.md) |

### Tổng Quan & Tham Khảo

| Tôi muốn... | Xem file... |
|-------------|-------------|
| Tổng quan dự án | [README.md](README.md) |
| Lịch sử thay đổi | [CHANGELOG.md](CHANGELOG.md) |
| Chỉ mục tất cả tài liệu | [INDEX.md](INDEX.md) |
| Project summary | [development/PROJECT_SUMMARY.md](development/PROJECT_SUMMARY.md) |
| Báo cáo tổ chức tài liệu | [ORGANIZATION_SUMMARY.md](ORGANIZATION_SUMMARY.md) |

## 🗂️ Theo Thư Mục

### 🔧 [setup/](setup/)
Cài đặt môi trường, IDE, thư viện

### 📖 [guides/](guides/)
Hướng dẫn sử dụng cho end-users

### 🔗 [guides/integration/](guides/integration/)
Tích hợp API, Database, SSO

### 💻 [development/](development/)
Tài liệu dành cho developers

### 🤖 [development/ai-assistant/](development/ai-assistant/)
AI Assistant refactoring

### 🧠 [development/llm/](development/llm/)
LLM integration & RAG

### 🗄️ [development/database/](development/database/)
Database implementation

### 🎨 [development/ui/](development/ui/)
UI/UX, AtlantaFX, Ikonli

### 🔧 [troubleshooting/](troubleshooting/)
Khắc phục sự cố

## 🔍 Tìm Kiếm

### Theo Keywords

```bash
# Tìm theo keyword
grep -r "keyword" docs/

# Tìm trong tên file
find docs/ -name "*keyword*.md"

# Tìm trong nội dung
grep -rl "keyword" docs/ --include="*.md"
```

### Ví Dụ

```bash
# Tìm tài liệu về LLM
grep -r "LLM" docs/ --include="*.md"

# Tìm tài liệu về SSO
find docs/ -name "*SSO*.md"

# Tìm code examples
grep -r "```java" docs/ --include="*.md"
```

## 📱 Quick Links

- **New User?** → Start with [QUICK_START.md](guides/QUICK_START.md)
- **Developer?** → Check [PROJECT_SUMMARY.md](development/PROJECT_SUMMARY.md)
- **Want to integrate LLM?** → See [API_INTEGRATION_GUIDE.md](guides/integration/API_INTEGRATION_GUIDE.md)
- **Need help?** → Check [TROUBLESHOOTING.md](troubleshooting/TROUBLESHOOTING.md)
- **Want full index?** → See [INDEX.md](INDEX.md)

## 💡 Tips

1. **Mỗi thư mục có README** - Đọc để hiểu structure
2. **INDEX.md** - Xem toàn bộ cấu trúc
3. **QUICK_START** - Bắt đầu nhanh nhất
4. **Search function** - Dùng `grep` để tìm nhanh
5. **Related links** - Mỗi doc có links liên quan

## 🎯 Use Cases

### "Tôi là người dùng mới, muốn bắt đầu"
1. [QUICK_START.md](guides/QUICK_START.md)
2. [PCM_CONCEPT.md](guides/PCM_CONCEPT.md)
3. [STEP_BY_STEP_GUIDE.md](guides/STEP_BY_STEP_GUIDE.md)

### "Tôi là developer, muốn contribute"
1. [PROJECT_SUMMARY.md](development/PROJECT_SUMMARY.md)
2. [INTELLIJ_SETUP.md](setup/INTELLIJ_SETUP.md)
3. Explore [development/](development/) folders

### "Tôi muốn tích hợp LLM vào app"
1. [API_INTEGRATION_GUIDE.md](guides/integration/API_INTEGRATION_GUIDE.md)
2. [LLM_QUICK_START.md](development/llm/LLM_QUICK_START.md)
3. [API_QUICK_REFERENCE.md](guides/integration/API_QUICK_REFERENCE.md)

### "Tôi gặp lỗi"
1. [TROUBLESHOOTING.md](troubleshooting/TROUBLESHOOTING.md)
2. [QUICK_FIX_GUIDE.md](troubleshooting/QUICK_FIX_GUIDE.md)
3. Search logs: `logs/pcm-desktop.log`

### "Tôi muốn refactor AI Assistant"
1. [AI_ASSISTANT_REFACTOR_README.md](development/ai-assistant/AI_ASSISTANT_REFACTOR_README.md)
2. [AI_ASSISTANT_REFACTOR_PLAN.md](development/ai-assistant/AI_ASSISTANT_REFACTOR_PLAN.md)
3. [AI_ASSISTANT_REFACTORING_SUMMARY.md](development/ai-assistant/AI_ASSISTANT_REFACTORING_SUMMARY.md)

---

**Tip**: Bookmark file này để tham khảo nhanh! 🔖

**Updated**: 12/11/2025

