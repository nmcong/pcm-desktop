# Script Migration Guide

Scripts trong thư mục này đã được tối giản và hợp nhất để dễ sử dụng hơn.

## 📊 Tổng Quan

**Trước:** 18 scripts  
**Sau:** 8 scripts  
**Giảm:** 56% số lượng file

## 🔄 Mapping Scripts Cũ sang Mới

### Build Scripts

| Script Cũ | Script Mới | Cách Dùng |
|-----------|-----------|-----------|
| `compile-macos.command` | `build.sh` | `./scripts/build.sh` |
| `compile-windows.bat` | `build.bat` | `scripts\build.bat` |
| `compile-with-text-component.sh` | `build.sh --text` | `./scripts/build.sh --text` |

### Run Scripts

| Script Cũ | Script Mới | Cách Dùng |
|-----------|-----------|-----------|
| `run-macos.command` | `run.sh` | `./scripts/run.sh` |
| `run-windows.bat` | `run.bat` | `scripts\run.bat` |
| `run-with-text-component.sh` | `run.sh --text` | `./scripts/run.sh --text` |
| `run-api-demo.sh` | `run.sh --api-demo` | `./scripts/run.sh --api-demo` |
| `run-api-demo.bat` | `run.bat --api-demo` | `scripts\run.bat --api-demo` |
| `run-sso-demo.sh` | `run.sh --sso-demo` | `./scripts/run.sh --sso-demo` |
| `run-sso-demo.bat` | `run.bat --sso-demo` | `scripts\run.bat --sso-demo` |

### Setup Scripts

| Script Cũ | Script Mới | Cách Dùng |
|-----------|-----------|-----------|
| `download-libs.sh` | `setup.sh` | `./scripts/setup.sh` |
| `download-atlantafx.sh` | `setup.sh --ui` | `./scripts/setup.sh --ui` |
| `download-ikonli.sh` | `setup.sh --ui` | `./scripts/setup.sh --ui` |

### Resource Management

| Script Cũ | Thay Thế Bởi | Ghi Chú |
|-----------|-------------|---------|
| `copy-icons-to-build.sh` | `build.sh` / `run.sh` | Tự động copy khi build/run |
| `copy-resources-to-build.sh` | `build.sh` / `run.sh` | Tự động copy khi build/run |

### Giữ Nguyên

| Script | Mô Tả |
|--------|-------|
| `verify-libs.sh` | Kiểm tra thư viện |
| `download-lucide-icon.sh` | Tải Lucide icons |
| `download-libs.ps1` | Setup cho Windows PowerShell |

## 📝 Ví Dụ Migration

### Ví Dụ 1: Build Tiêu Chuẩn

**Trước:**
```bash
./scripts/compile-macos.command
```

**Sau:**
```bash
./scripts/build.sh
```

### Ví Dụ 2: Build với Text Component

**Trước:**
```bash
./scripts/compile-with-text-component.sh
```

**Sau:**
```bash
./scripts/build.sh --text
```

### Ví Dụ 3: Chạy Application

**Trước:**
```bash
./scripts/run-macos.command
```

**Sau:**
```bash
./scripts/run.sh
```

### Ví Dụ 4: Chạy API Demo

**Trước:**
```bash
./scripts/run-api-demo.sh
```

**Sau:**
```bash
./scripts/run.sh --api-demo
```

### Ví Dụ 5: Download All Libraries

**Trước:**
```bash
./scripts/download-libs.sh
./scripts/download-atlantafx.sh
./scripts/download-ikonli.sh
```

**Sau:**
```bash
./scripts/setup.sh
# Hoặc từng phần:
./scripts/setup.sh --core
./scripts/setup.sh --ui
```

## 🎯 Lợi Ích

### 1. Đơn Giản Hóa
- Ít file hơn để quản lý
- Dễ nhớ tên scripts
- Tất cả options trong một script

### 2. Nhất Quán
- Cùng pattern cho macOS/Linux và Windows
- Cùng naming convention
- Cùng option flags

### 3. Linh Hoạt
- Nhiều chế độ trong một script
- Options có thể kết hợp
- Dễ mở rộng thêm chức năng

### 4. Dễ Bảo Trì
- Ít duplicate code
- Dễ update logic
- Dễ debug

## 🔍 Chi Tiết Options

### build.sh / build.bat

```bash
# Standard build
./scripts/build.sh

# Build với text component support
./scripts/build.sh --text

# Clean build (xóa out/ trước khi build)
./scripts/build.sh --clean

# Kết hợp options
./scripts/build.sh --clean --text
```

### run.sh / run.bat

```bash
# Chạy ứng dụng chính
./scripts/run.sh

# Chạy với text component
./scripts/run.sh --text

# Chạy API demo
./scripts/run.sh --api-demo

# Chạy SSO demo
./scripts/run.sh --sso-demo

# Bỏ qua auto-compile
./scripts/run.sh --no-compile
```

### setup.sh

```bash
# Tải tất cả thư viện
./scripts/setup.sh

# Chỉ tải core libraries
./scripts/setup.sh --core

# Chỉ tải UI libraries
./scripts/setup.sh --ui

# Xem hướng dẫn tải JavaFX
./scripts/setup.sh --javafx

# Hiển thị help
./scripts/setup.sh --help
```

## 🆘 Cần Trợ Giúp?

Mỗi script đều có `--help` option:

```bash
./scripts/build.sh --help
./scripts/run.sh --help
./scripts/setup.sh --help
```

Hoặc xem [README.md](./README.md) để biết thêm chi tiết.

## 🐛 Vấn Đề Thường Gặp

### Q: Script cũ của tôi không hoạt động nữa?

**A:** Scripts cũ đã bị xóa. Sử dụng mapping table ở trên để tìm script mới tương ứng.

### Q: Tôi có script tự động sử dụng các scripts cũ?

**A:** Cập nhật scripts tự động của bạn theo mapping table. Ví dụ:
- `compile-macos.command` → `build.sh`
- `run-macos.command` → `run.sh`

### Q: Có cách nào để sử dụng tên cũ không?

**A:** Bạn có thể tạo symbolic links hoặc aliases:

```bash
# Symbolic links (macOS/Linux)
ln -s build.sh compile-macos.command
ln -s run.sh run-macos.command

# Aliases trong ~/.bashrc hoặc ~/.zshrc
alias compile-macos='./scripts/build.sh'
alias run-macos='./scripts/run.sh'
```

### Q: Scripts Windows có thay đổi gì không?

**A:** Yes! Windows scripts cũng được hợp nhất:
- `compile-windows.bat` → `build.bat`
- `run-windows.bat` → `run.bat`
- Và giờ hỗ trợ options giống Linux/macOS

## ✅ Checklist Migration

- [ ] Cập nhật documentation/README của project
- [ ] Cập nhật CI/CD scripts (nếu có)
- [ ] Cập nhật IDE run configurations
- [ ] Thông báo cho team members về changes
- [ ] Cập nhật automation scripts
- [ ] Xóa bookmarks/shortcuts đến scripts cũ

## 📚 Tài Liệu Liên Quan

- [Scripts README](./README.md) - Hướng dẫn sử dụng scripts mới
- [Quick Start Guide](../docs/guides/QUICK_START.md) - Hướng dẫn bắt đầu
- [Troubleshooting](../docs/troubleshooting/TROUBLESHOOTING.md) - Xử lý lỗi

---

**Ngày cập nhật:** November 12, 2025  
**Phiên bản:** 2.0 (Simplified Scripts)

