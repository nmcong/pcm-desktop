# PCM Desktop Scripts

Thư mục này chứa các script tiện ích để xây dựng, chạy và quản lý ứng dụng PCM Desktop.

> **✨ Mới:** Scripts đã được tối giản tối đa - chỉ còn 5 scripts cốt lõi!

## 📁 Available Scripts

### 🔧 Setup Scripts

**`setup.sh`** - Tải xuống tất cả thư viện cần thiết (macOS/Linux)
```bash
./scripts/setup.sh              # Tải tất cả thư viện
./scripts/setup.sh --core       # Chỉ tải core libraries
./scripts/setup.sh --ui         # Chỉ tải UI libraries
./scripts/setup.sh --javafx     # Xem hướng dẫn tải JavaFX
```

**`setup.bat`** - Tải xuống tất cả thư viện cần thiết (Windows)
```cmd
scripts\setup.bat               # Tải tất cả thư viện
scripts\setup.bat --core        # Chỉ tải core libraries
scripts\setup.bat --ui          # Chỉ tải UI libraries
scripts\setup.bat --javafx      # Xem hướng dẫn tải JavaFX
```

### 🔨 Build Scripts

**`build.sh`** - Biên dịch mã nguồn Java (macOS/Linux)
```bash
./scripts/build.sh              # Build tiêu chuẩn
./scripts/build.sh --text       # Build với text component
./scripts/build.sh --clean      # Xóa và build lại
```

> 🔍 **Tự động kiểm tra thư viện** trước khi build!

**`build.bat`** - Biên dịch mã nguồn Java (Windows)
```cmd
scripts\build.bat               # Build tiêu chuẩn
scripts\build.bat --text        # Build với text component
scripts\build.bat --clean       # Xóa và build lại
```

> 🔍 **Tự động kiểm tra thư viện** trước khi build!

### 🚀 Run Scripts

**`run.sh`** - Chạy ứng dụng với nhiều chế độ (macOS/Linux)
```bash
./scripts/run.sh                # Chạy ứng dụng chính
./scripts/run.sh --text         # Chạy với text component
./scripts/run.sh --api-demo     # Chạy API demo
./scripts/run.sh --sso-demo     # Chạy SSO demo
```

**`run.bat`** - Chạy ứng dụng với nhiều chế độ (Windows)
```cmd
scripts\run.bat                 # Chạy ứng dụng chính
scripts\run.bat --text          # Chạy với text component
scripts\run.bat --api-demo      # Chạy API demo
scripts\run.bat --sso-demo      # Chạy SSO demo
```

## 🚀 Quick Start

### macOS/Linux

```bash
# Bước 1: Tải thư viện (chỉ cần làm một lần)
./scripts/setup.sh

# Bước 2: Tải JavaFX thủ công (xem hướng dẫn)
./scripts/setup.sh --javafx

# Bước 3: Build ứng dụng (tự động kiểm tra thư viện)
./scripts/build.sh

# Bước 4: Chạy ứng dụng
./scripts/run.sh
```

### Windows

```cmd
REM Bước 1: Tải thư viện (chỉ cần làm một lần)
scripts\setup.bat

REM Bước 2: Tải JavaFX thủ công (xem hướng dẫn)
scripts\setup.bat --javafx

REM Bước 3: Build ứng dụng (tự động kiểm tra thư viện)
scripts\build.bat

REM Bước 4: Chạy ứng dụng
scripts\run.bat
```

## 📋 Common Tasks

### Build và Run (macOS/Linux)
```bash
# Build tiêu chuẩn và chạy
./scripts/build.sh && ./scripts/run.sh

# Build với text component và chạy
./scripts/build.sh --text && ./scripts/run.sh --text

# Clean build và chạy
./scripts/build.sh --clean && ./scripts/run.sh
```

### Build và Run (Windows)
```cmd
REM Build và chạy
scripts\build.bat && scripts\run.bat

REM Build với text component và chạy
scripts\build.bat --text && scripts\run.bat --text
```

### Chạy Demos

#### API Integration Demo (macOS/Linux)
```bash
# Thiết lập API key
export OPENAI_API_KEY=your-api-key-here

# Chạy demo
./scripts/run.sh --api-demo
```

#### API Integration Demo (Windows)
```cmd
REM Thiết lập API key
set OPENAI_API_KEY=your-api-key-here

REM Chạy demo
scripts\run.bat --api-demo
```

#### SSO Integration Demo
```bash
# macOS/Linux
./scripts/run.sh --sso-demo

# Windows
scripts\run.bat --sso-demo
```

## 🔧 Requirements

- **Java 21** (JDK cho biên dịch, JRE cho chạy)
- **JavaFX 21.0.9** (tải thủ công qua setup scripts)
- **macOS/Linux**: bash shell (đã cài sẵn)
- **Windows**: Command Prompt hoặc PowerShell

## 📝 Script Options Reference

### setup.sh / setup.bat
| Option | Mô tả |
|--------|-------|
| _(no option)_ | Tải tất cả thư viện |
| `--core` | Chỉ tải core libraries (Lombok, Jackson, SLF4J, SQLite) |
| `--ui` | Chỉ tải UI libraries (AtlantaFX, Ikonli) |
| `--javafx` | Hiển thị hướng dẫn tải JavaFX |
| `--help` | Hiển thị trợ giúp |

### build.sh / build.bat
| Option | Mô tả |
|--------|-------|
| _(no option)_ | Build tiêu chuẩn |
| `--text`, `--text-component` | Build với Universal Text Component |
| `--clean`, `-c` | Xóa build directory trước khi build |
| `--help`, `-h` | Hiển thị trợ giúp |

**✨ Tính năng mới:** Tự động kiểm tra thư viện trước khi build!

### run.sh / run.bat
| Option | Mô tả |
|--------|-------|
| _(no option)_ | Chạy ứng dụng chính |
| `--text`, `--text-component` | Chạy với Text Component support |
| `--api-demo` | Chạy API integration demo |
| `--sso-demo` | Chạy SSO integration demo |
| `--no-compile` | Bỏ qua kiểm tra auto-compilation |
| `--help`, `-h` | Hiển thị trợ giúp |

## 📦 Thư Viện Được Tải Xuống

### Core Libraries
- **Lombok 1.18.34** - Giảm boilerplate code
- **Jackson 2.18.2** - JSON processing
- **SLF4J 2.0.16** - Logging API
- **Logback 1.5.12** - Logging implementation
- **SQLite JDBC 3.47.1.0** - Database driver

### UI Libraries
- **AtlantaFX 2.0.1** - Modern JavaFX themes
- **Ikonli 12.3.1** - Icon packs (Material Design, Feather)

### JavaFX (Manual)
- **JavaFX 21.0.9** - JavaFX SDK (cần tải thủ công)

## 🐛 Troubleshooting

### "Permission denied" trên macOS/Linux

Đặt quyền thực thi cho scripts:

```bash
chmod +x scripts/*.sh
```

### "Cannot be opened because it is from an unidentified developer" trên macOS

Chuột phải vào file .sh và chọn "Open" lần đầu tiên.

### JavaFX không tìm thấy

Chạy setup script để xem hướng dẫn tải JavaFX:

```bash
# macOS/Linux
./scripts/setup.sh --javafx

# Windows
scripts\setup.bat --javafx
```

### Lỗi compilation

Build scripts giờ đây **tự động kiểm tra thư viện** trước khi build. Nếu thiếu thư viện, script sẽ thông báo và dừng lại.

Nếu cần tải lại thư viện:
```bash
# macOS/Linux
./scripts/setup.sh

# Windows
scripts\setup.bat
```

Thử clean build:
```bash
./scripts/build.sh --clean
```

Kiểm tra Java version:
```bash
java -version    # Should be Java 21
javac -version   # Should be Java 21
```

### API Demo không chạy

Đảm bảo đã thiết lập `OPENAI_API_KEY`:

```bash
# macOS/Linux
export OPENAI_API_KEY=your-api-key-here

# Windows
set OPENAI_API_KEY=your-api-key-here
```

## 📚 Thông Tin Thêm

Xem thêm tài liệu trong thư mục [docs/](../docs/):
- [Quick Start Guide](../docs/guides/QUICK_START.md)
- [API Integration Guide](../docs/API_INTEGRATION_GUIDE.md)
- [SSO Integration Guide](../docs/SSO_INTEGRATION_GUIDE.md)
- [Database Guide](../docs/DATABASE_README.md)
- [LLM Integration](../docs/LLM_README.md)

## 🎯 Cải Tiến So Với Trước

### Version 1.0: 18 scripts
Quá nhiều scripts riêng lẻ, khó quản lý

### Version 2.0: 8 scripts (giảm 56%)
Scripts đã được hợp nhất theo chức năng

### **Version 3.0: 5 scripts (giảm 72%!)** ⭐ **CỰC KỲ TINH GỌN**

Chỉ còn 5 scripts cốt lõi:

| Script | Nền tảng | Mô tả |
|--------|----------|-------|
| **setup.sh** | macOS/Linux | Download tất cả libraries |
| **setup.bat** | Windows | Download tất cả libraries |
| **build.sh** | macOS/Linux | Build + verify libraries |
| **build.bat** | Windows | Build + verify libraries |
| **run.sh** | macOS/Linux | Run với nhiều modes |
| **run.bat** | Windows | Run với nhiều modes |

~~**Đã xóa:**~~
- ~~verify-libs.sh~~ → Tích hợp vào build scripts
- ~~download-lucide-icon.sh~~ → Không cần thiết
- ~~download-libs.ps1~~ → Thay bằng setup.bat

### Lợi ích Version 3.0

1. ✅ **Cực kỳ đơn giản** - chỉ 5 scripts
2. ✅ **Nhất quán hoàn toàn** - macOS/Linux và Windows giống nhau
3. ✅ **Tự động hóa cao** - build scripts tự verify libraries
4. ✅ **Dễ nhớ** - setup → build → run
5. ✅ **Zero redundancy** - không có code duplicate

## 🌟 Script Workflow

```
┌──────────────┐
│  setup.sh    │  Download libraries
│  setup.bat   │  (chỉ cần 1 lần)
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  build.sh    │  ✅ Auto verify libs
│  build.bat   │  ✅ Compile code
└──────┬───────┘  ✅ Copy resources
       │
       ▼
┌──────────────┐
│  run.sh      │  Run với options:
│  run.bat     │  • normal
└──────────────┘  • --text
                  • --api-demo
                  • --sso-demo
```

## 💡 Tips

- Build scripts giờ **tự động verify** thư viện - không cần chạy verify riêng
- Tất cả scripts đều có `--help` option
- Windows scripts giờ hoàn toàn tương đương với macOS/Linux
- Script names dễ nhớ: setup → build → run

---

**Phiên bản:** 3.0 (Ultra Simplified)  
**Cập nhật:** November 12, 2025  
**Số lượng scripts:** 5 (giảm 72% so với ban đầu)
