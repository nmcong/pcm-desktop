# PCM Desktop Scripts

Thư mục này chứa các script tiện ích để xây dựng, chạy và quản lý ứng dụng PCM Desktop.

> **✨ Mới:** Scripts đã được tối giản và hợp nhất để dễ sử dụng hơn!

## 📁 Available Scripts

### 🔧 Setup Scripts

**`setup.sh`** - Tải xuống tất cả thư viện cần thiết (macOS/Linux)
```bash
./scripts/setup.sh              # Tải tất cả thư viện
./scripts/setup.sh --core       # Chỉ tải core libraries
./scripts/setup.sh --ui         # Chỉ tải UI libraries
./scripts/setup.sh --javafx     # Xem hướng dẫn tải JavaFX
```

**`download-libs.ps1`** - Tải xuống thư viện (Windows PowerShell)

### 🔨 Build Scripts

**`build.sh`** - Biên dịch mã nguồn Java (macOS/Linux)
```bash
./scripts/build.sh              # Build tiêu chuẩn
./scripts/build.sh --text       # Build với text component
./scripts/build.sh --clean      # Xóa và build lại
```

**`build.bat`** - Biên dịch mã nguồn Java (Windows)
```cmd
scripts\build.bat               # Build tiêu chuẩn
scripts\build.bat --text        # Build với text component
scripts\build.bat --clean       # Xóa và build lại
```

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

### 🔍 Utility Scripts

**`verify-libs.sh`** - Kiểm tra thư viện đã cài đặt đầy đủ chưa
```bash
./scripts/verify-libs.sh
```

**`download-lucide-icon.sh`** - Tải xuống Lucide icons cho UI
```bash
./scripts/download-lucide-icon.sh home              # Tải icon home
./scripts/download-lucide-icon.sh -s 32 heart      # Tải với size tùy chỉnh
./scripts/download-lucide-icon.sh --list           # Xem danh sách icons
```

## 🚀 Quick Start

### macOS/Linux

```bash
# Bước 1: Tải thư viện (chỉ cần làm một lần)
./scripts/setup.sh

# Bước 2: Tải JavaFX thủ công (xem hướng dẫn)
./scripts/setup.sh --javafx

# Bước 3: Build ứng dụng
./scripts/build.sh

# Bước 4: Chạy ứng dụng
./scripts/run.sh
```

### Windows

```cmd
REM Bước 1: Tải thư viện (chỉ cần làm một lần)
powershell -ExecutionPolicy Bypass -File scripts\download-libs.ps1

REM Bước 2: Tải JavaFX thủ công (xem hướng dẫn trong PowerShell script)

REM Bước 3: Build ứng dụng
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
- **Windows**: PowerShell (cho download-libs.ps1)

## 📝 Script Options Reference

### setup.sh
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
powershell -ExecutionPolicy Bypass -File scripts\download-libs.ps1
```

### Lỗi compilation

1. Đảm bảo đã tải đủ thư viện:
   ```bash
   ./scripts/verify-libs.sh
   ```

2. Thử clean build:
   ```bash
   ./scripts/build.sh --clean
   ```

3. Kiểm tra Java version:
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

### Trước đây: 18 scripts
- compile-macos.command
- compile-windows.bat
- compile-with-text-component.sh
- run-macos.command
- run-windows.bat
- run-with-text-component.sh
- run-api-demo.sh
- run-api-demo.bat
- run-sso-demo.sh
- run-sso-demo.bat
- download-libs.sh
- download-atlantafx.sh
- download-ikonli.sh
- copy-icons-to-build.sh
- copy-resources-to-build.sh
- verify-libs.sh
- download-lucide-icon.sh
- download-libs.ps1

### Bây giờ: 8 scripts (**giảm 56%**)
- **setup.sh** (thay thế 3 download scripts)
- **build.sh** (thay thế 3 compile scripts)
- **run.sh** (thay thế 6 run scripts)
- **build.bat** (Windows build)
- **run.bat** (Windows run)
- **verify-libs.sh** (giữ nguyên)
- **download-lucide-icon.sh** (giữ nguyên)
- **download-libs.ps1** (giữ nguyên)

### Lợi ích
- ✅ Dễ hiểu và sử dụng hơn
- ✅ Ít file hơn cần quản lý
- ✅ Tính năng tương tự nhưng gọn gàng hơn
- ✅ Tất cả options trong một script
- ✅ Nhất quán giữa macOS/Linux và Windows
