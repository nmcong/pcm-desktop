# Script Migration Guide

Scripts trong thư mục này đã được tối giản tối đa để cực kỳ dễ sử dụng.

## 📊 Tổng Quan

**Ban đầu (V1.0):** 18 scripts  
**Sau lần tối giản đầu (V2.0):** 8 scripts (giảm 56%)  
**Bây giờ (V3.0):** **5 scripts** (giảm **72%!**) ⭐

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
| `download-libs.ps1` | `setup.bat` | `scripts\setup.bat` |
| `download-atlantafx.sh` | `setup.sh --ui` | `./scripts/setup.sh --ui` |
| `download-ikonli.sh` | `setup.sh --ui` | `./scripts/setup.sh --ui` |

### Utilities & Resource Management

| Script Cũ | Thay Thế Bởi | Ghi Chú |
|-----------|-------------|---------|
| `verify-libs.sh` | `build.sh` / `build.bat` | **Tự động verify trước khi build** |
| `copy-icons-to-build.sh` | `build.sh` / `run.sh` | Tự động copy khi build/run |
| `copy-resources-to-build.sh` | `build.sh` / `run.sh` | Tự động copy khi build/run |

### Xóa Hoàn Toàn

| Script | Lý Do Xóa |
|--------|-----------|
| `download-lucide-icon.sh` | Không còn cần thiết |

## ✨ Tính Năng Mới Version 3.0

### 🔍 Auto Library Verification

Build scripts giờ **tự động kiểm tra thư viện** trước khi build:

```bash
# Trước: Phải chạy verify riêng
./scripts/verify-libs.sh
./scripts/build.sh

# Bây giờ: Tự động verify khi build
./scripts/build.sh
# ✅ Kiểm tra thư viện
# ✅ Build nếu đủ thư viện
# ❌ Dừng và báo lỗi nếu thiếu thư viện
```

### 🪟 Windows Setup Script

Giờ có `setup.bat` cho Windows - không cần PowerShell nữa!

```cmd
REM Trước: Phải dùng PowerShell
powershell -ExecutionPolicy Bypass -File scripts\download-libs.ps1

REM Bây giờ: Dùng batch script đơn giản
scripts\setup.bat
```

### 🎯 Hoàn Toàn Nhất Quán

macOS/Linux và Windows giờ hoàn toàn giống nhau:

```bash
# macOS/Linux
./scripts/setup.sh
./scripts/build.sh
./scripts/run.sh

# Windows (tương tự!)
scripts\setup.bat
scripts\build.bat
scripts\run.bat
```

## 📝 Ví Dụ Migration

### Ví Dụ 1: Build Tiêu Chuẩn

**Trước (V1.0):**
```bash
./scripts/verify-libs.sh       # Kiểm tra thư viện
./scripts/compile-macos.command # Build
```

**Sau (V3.0):**
```bash
./scripts/build.sh             # Tự động verify + build
```

### Ví Dụ 2: Build với Text Component

**Trước (V1.0):**
```bash
./scripts/verify-libs.sh
./scripts/compile-with-text-component.sh
```

**Sau (V3.0):**
```bash
./scripts/build.sh --text      # Tự động verify + build
```

### Ví Dụ 3: Setup trên Windows

**Trước (V1.0):**
```cmd
powershell -ExecutionPolicy Bypass -File scripts\download-libs.ps1
REM Rồi tải AtlantaFX và Ikonli riêng
```

**Sau (V3.0):**
```cmd
scripts\setup.bat              # Tải tất cả!
```

### Ví Dụ 4: Chạy API Demo

**Trước (V1.0):**
```bash
export OPENAI_API_KEY=xxx
./scripts/run-api-demo.sh
```

**Sau (V3.0):**
```bash
export OPENAI_API_KEY=xxx
./scripts/run.sh --api-demo    # Gọn hơn!
```

### Ví Dụ 5: Download All Libraries

**Trước (V1.0):**
```bash
./scripts/download-libs.sh
./scripts/download-atlantafx.sh
./scripts/download-ikonli.sh
```

**Sau (V3.0):**
```bash
./scripts/setup.sh             # Một lệnh, tải tất cả!
```

## 🎯 5 Scripts Còn Lại

### 1. setup.sh / setup.bat
**Download tất cả libraries**
- Core libraries (Lombok, Jackson, SLF4J, SQLite)
- UI libraries (AtlantaFX, Ikonli)
- Hướng dẫn tải JavaFX

### 2. build.sh / build.bat
**Build với auto-verification**
- ✅ Tự động verify libraries
- ✅ Compile Java code
- ✅ Copy resources
- Hỗ trợ: `--clean`, `--text`

### 3. run.sh / run.bat
**Run với nhiều modes**
- Normal mode
- Text component mode
- API demo
- SSO demo

## 🔍 Chi Tiết Options

### setup.sh / setup.bat

```bash
# Tải tất cả
./scripts/setup.sh              # hoặc scripts\setup.bat

# Tải từng phần
./scripts/setup.sh --core       # Core libraries only
./scripts/setup.sh --ui         # UI libraries only

# Xem hướng dẫn JavaFX
./scripts/setup.sh --javafx
```

### build.sh / build.bat

```bash
# Build tiêu chuẩn (với auto-verify!)
./scripts/build.sh              # hoặc scripts\build.bat

# Build với text component
./scripts/build.sh --text

# Clean build
./scripts/build.sh --clean

# Kết hợp
./scripts/build.sh --clean --text
```

### run.sh / run.bat

```bash
# Chạy ứng dụng chính
./scripts/run.sh                # hoặc scripts\run.bat

# Các modes khác
./scripts/run.sh --text         # Text component
./scripts/run.sh --api-demo     # API demo
./scripts/run.sh --sso-demo     # SSO demo
./scripts/run.sh --no-compile   # Skip auto-compile check
```

## 🆘 Cần Trợ Giúp?

Mỗi script đều có `--help` option:

```bash
./scripts/setup.sh --help
./scripts/build.sh --help
./scripts/run.sh --help
```

Hoặc xem [README.md](./README.md) để biết thêm chi tiết.

## 🐛 Vấn Đề Thường Gặp

### Q: Script verify-libs.sh đâu rồi?

**A:** Đã tích hợp vào build scripts! Giờ build scripts tự động verify libraries trước khi build. Bạn không cần chạy verify riêng nữa.

```bash
# Không cần làm thế này nữa:
./scripts/verify-libs.sh && ./scripts/build.sh

# Chỉ cần:
./scripts/build.sh  # Auto verify!
```

### Q: Script download-libs.ps1 đâu rồi?

**A:** Đã được thay thế bởi `setup.bat` - đơn giản hơn và không cần PowerShell!

```cmd
REM Trước
powershell -ExecutionPolicy Bypass -File scripts\download-libs.ps1

REM Bây giờ
scripts\setup.bat
```

### Q: Làm sao download Lucide icons?

**A:** Script đó đã bị xóa vì không còn cần thiết cho workflow chính. Nếu cần icons, có thể download trực tiếp từ https://lucide.dev/

### Q: Tôi có script tự động sử dụng các scripts cũ?

**A:** Cập nhật theo mapping table:

```bash
# Build
verify-libs.sh → (xóa, không cần)
compile-macos.command → build.sh
compile-windows.bat → build.bat

# Run
run-macos.command → run.sh
run-windows.bat → run.bat
run-api-demo.sh → run.sh --api-demo

# Setup
download-libs.sh → setup.sh
download-libs.ps1 → setup.bat
```

### Q: Build script có chậm hơn không vì phải verify?

**A:** Không đáng kể! Verification chỉ mất vài milliseconds. Và bạn tiết kiệm được thời gian vì không phải chạy verify riêng.

### Q: Tôi muốn skip verification được không?

**A:** Hiện tại không. Verification rất nhanh và giúp phát hiện lỗi sớm. Nếu thật sự cần, bạn có thể edit build script và comment out phần `verify_libraries`.

## ✅ Checklist Migration

- [ ] Xóa bookmarks/shortcuts đến scripts cũ
- [ ] Cập nhật documentation của project
- [ ] Cập nhật CI/CD scripts (nếu có)
- [ ] Cập nhật IDE run configurations
- [ ] Thông báo cho team về changes
- [ ] Test build workflow mới
- [ ] Test run workflow mới
- [ ] Kiểm tra Windows scripts (nếu có Windows users)

## 🎨 Workflow Mới

### Workflow Hoàn Chỉnh

```bash
# 1. Setup (chỉ cần 1 lần)
./scripts/setup.sh              # Download libraries
./scripts/setup.sh --javafx     # Xem hướng dẫn JavaFX

# 2. Build (tự động verify!)
./scripts/build.sh              # Build với auto-verification

# 3. Run
./scripts/run.sh                # Run application

# 4. Development loop
./scripts/build.sh && ./scripts/run.sh
```

### Quick Commands

```bash
# One-liner: Build và run
./scripts/build.sh && ./scripts/run.sh

# Clean build và run
./scripts/build.sh --clean && ./scripts/run.sh

# Build và run với text component
./scripts/build.sh --text && ./scripts/run.sh --text

# Run demo
./scripts/run.sh --api-demo
```

## 📚 Tài Liệu Liên Quan

- [Scripts README](./README.md) - Hướng dẫn sử dụng scripts mới
- [Quick Start Guide](../docs/guides/QUICK_START.md) - Hướng dẫn bắt đầu
- [Troubleshooting](../docs/troubleshooting/TROUBLESHOOTING.md) - Xử lý lỗi

## 🌟 Benefits Summary

### Version 3.0 Improvements

1. **Cực kỳ đơn giản** - Chỉ 5 scripts
2. **Tự động hóa** - Auto verify libraries
3. **Nhất quán** - macOS/Linux = Windows
4. **Ít lỗi hơn** - Verify trước khi build
5. **Dễ bảo trì** - Ít files, ít code duplicate
6. **User-friendly** - Workflow rõ ràng: setup → build → run

### So Sánh Versions

```
V1.0: 18 scripts → Quá nhiều, khó quản lý
      ❌ Phải nhớ nhiều tên scripts
      ❌ Phải chạy verify riêng
      ❌ Windows khác macOS/Linux

V2.0: 8 scripts  → Đã tốt hơn nhiều
      ✅ Scripts hợp nhất
      ✅ Options linh hoạt
      ⚠️  Vẫn có verify riêng

V3.0: 5 scripts  → HOÀN HẢO! ⭐
      ✅ Cực kỳ gọn gàng
      ✅ Auto verify
      ✅ Hoàn toàn nhất quán
      ✅ Zero redundancy
```

---

**Phiên bản:** 3.0 (Ultra Simplified)  
**Cập nhật:** November 12, 2025  
**Số lượng scripts:** 5 (giảm 72% so với V1.0)  
**Tính năng mới:** Auto library verification, Windows batch setup
