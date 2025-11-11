# 🪟 Hướng dẫn cài đặt JavaFX cho Windows

## 📥 Bước 1: Tải JavaFX 21.0.9 cho Windows

### Link tải trực tiếp:
```
https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip
```

**Hoặc tải thủ công:**
1. Truy cập: https://gluonhq.com/products/javafx/
2. Chọn version: **21.0.9**
3. Chọn platform: **Windows x64**
4. Tải file: `openjfx-21.0.9_windows-x64_bin-sdk.zip`

---

## 📦 Bước 2: Cài đặt JavaFX

### Cách 1: Thủ công (Dễ nhất)

1. **Giải nén** file ZIP vừa tải (click phải → Extract All...)

2. **Mở** thư mục `javafx-sdk-21.0.9\lib\`

3. **Xóa** tất cả file `.jar` cũ trong thư mục project:
   ```
   pcm-desktop\lib\javafx\
   ```

4. **Copy** tất cả file `.jar` từ `javafx-sdk-21.0.9\lib\` vào:
   ```
   pcm-desktop\lib\javafx\
   ```

### Cách 2: Dùng PowerShell (Tự động)

Mở **PowerShell** trong thư mục project và chạy:

```powershell
# Di chuyển đến thư mục project
cd C:\Path\To\pcm-desktop

# Tải JavaFX 21.0.9
Invoke-WebRequest -Uri "https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip" -OutFile "javafx-21.zip"

# Giải nén
Expand-Archive -Path "javafx-21.zip" -DestinationPath "." -Force

# Xóa các file JAR cũ
Remove-Item "lib\javafx\*.jar" -ErrorAction SilentlyContinue

# Copy các file JAR mới
Copy-Item "javafx-sdk-21.0.9\lib\*.jar" -Destination "lib\javafx\"

# Dọn dẹp
Remove-Item "javafx-21.zip"
Remove-Item "javafx-sdk-21.0.9" -Recurse

Write-Host "✅ Cài đặt JavaFX 21.0.9 hoàn tất!" -ForegroundColor Green
```

---

## 🔧 Bước 3: Cấu hình IDE trên Windows

### IntelliJ IDEA

1. **Mở Project:**
   - File → Open → Chọn thư mục `pcm-desktop`

2. **Cài đặt SDK:**
   - File → Project Structure (Ctrl+Alt+Shift+S)
   - Project → SDK: **Java 21**
   - Language Level: **21**

3. **Thêm Libraries:**
   - File → Project Structure → Libraries
   - Click `+` → Java
   - Chọn thư mục `lib\javafx` → OK
   - Click `+` → Java
   - Chọn thư mục `lib\others` → OK
   - Apply

4. **Cài đặt Lombok:**
   - File → Settings → Plugins
   - Tìm "Lombok" → Install
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - ✅ Enable annotation processing

5. **Tạo Run Configuration:**
   - Run → Edit Configurations
   - Click `+` → Application
   - Name: `PCM Application`
   - Main class: `com.noteflix.pcm.PCMApplication`
   - VM options:
     ```
     --module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
     ```
   - Working directory: `$ProjectFileDir$`
   - Apply → OK

6. **Rebuild Project:**
   - Build → Rebuild Project
   - Hoặc: File → Invalidate Caches → Invalidate and Restart

### Eclipse

1. **Import Project:**
   - File → Import → General → Existing Projects into Workspace
   - Chọn thư mục `pcm-desktop`

2. **Thêm Libraries:**
   - Right-click project → Properties
   - Java Build Path → Libraries tab
   - Add External JARs → Chọn tất cả JAR từ `lib\javafx`
   - Add External JARs → Chọn tất cả JAR từ `lib\others`

3. **Cài đặt Lombok:**
   - Mở Command Prompt với quyền Administrator
   - Chạy:
     ```cmd
     java -jar C:\Path\To\pcm-desktop\lib\others\lombok-1.18.34.jar
     ```
   - Install vào Eclipse
   - Restart Eclipse

4. **Run Configuration:**
   - Run → Run Configurations → Java Application
   - Main class: `com.noteflix.pcm.PCMApplication`
   - Arguments tab → VM arguments:
     ```
     --module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
     ```

### VS Code

1. **Cài Extensions:**
   - Extension Pack for Java
   - Debugger for Java

2. **Tạo `.vscode\settings.json`:**
   ```json
   {
     "java.project.sourcePaths": ["src/main/java"],
     "java.project.referencedLibraries": [
       "lib/javafx/*.jar",
       "lib/others/*.jar"
     ],
     "java.configuration.runtimes": [
       {
         "name": "JavaSE-21",
         "path": "C:\\Program Files\\Java\\jdk-21",
         "default": true
       }
     ]
   }
   ```

3. **Tạo `.vscode\launch.json`:**
   ```json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "PCM Application",
         "request": "launch",
         "mainClass": "com.noteflix.pcm.PCMApplication",
         "vmArgs": "--module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media"
       }
     ]
   }
   ```

---

## 💻 Bước 4: Compile và Run từ Command Line

### Compile

```cmd
javac -cp "lib/javafx/*;lib/others/*" ^
  -d out ^
  -encoding UTF-8 ^
  src/main/java/com/noteflix/pcm/PCMApplication.java ^
  src/main/java/com/noteflix/pcm/ui/MainController.java
```

### Run

```cmd
java --module-path lib/javafx ^
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media ^
  -cp "out;lib/others/*" ^
  com.noteflix.pcm.PCMApplication
```

### Hoặc tạo file `run.bat`:

```batch
@echo off
echo 🚀 Starting PCM Desktop Application...
echo.

java --module-path lib/javafx ^
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media ^
  -cp "out;lib/others/*" ^
  com.noteflix.pcm.PCMApplication

pause
```

Lưu file này vào thư mục gốc project và double-click để chạy.

---

## 📋 Xác nhận cài đặt

Kiểm tra các file JAR đã copy đúng chưa:

```cmd
dir lib\javafx
```

Phải có các file sau:
- ✅ javafx.base.jar
- ✅ javafx.controls.jar
- ✅ javafx.fxml.jar
- ✅ javafx.graphics.jar
- ✅ javafx.media.jar
- ✅ javafx.swing.jar
- ✅ javafx.web.jar
- ✅ javafx-swt.jar

Tổng cộng khoảng **8 file JAR**, dung lượng ~48MB

---

## 🐛 Xử lý lỗi thường gặp trên Windows

### Lỗi: "Error: JavaFX runtime components are missing"

**Giải pháp:**
- Kiểm tra các JAR đã có trong `lib\javafx`
- Thêm VM options: `--module-path lib/javafx --add-modules javafx.controls,javafx.fxml`

### Lỗi: "Graphics Device initialization failed"

**Giải pháp:**
- Cập nhật driver card màn hình
- Thêm VM option: `-Dprism.order=sw` (software rendering)

### Lỗi: "UnsatisfiedLinkError"

**Giải pháp:**
- Đảm bảo đang dùng JavaFX cho Windows x64
- Kiểm tra Java version: `java -version` (phải là 64-bit)

### Lỗi: Lombok không hoạt động

**Giải pháp:**
- Cài Lombok plugin cho IDE
- Enable annotation processing trong IDE settings
- Restart IDE

---

## 🔄 Tải tất cả thư viện (Tự động)

Chạy script PowerShell để tải tất cả thư viện cần thiết:

```powershell
# Download-libs-windows.ps1
cd lib\others

# Lombok
Invoke-WebRequest -Uri "https://projectlombok.org/downloads/lombok.jar" -OutFile "lombok-1.18.34.jar"

# Jackson
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.2/jackson-databind-2.17.2.jar" -OutFile "jackson-databind-2.17.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.2/jackson-core-2.17.2.jar" -OutFile "jackson-core-2.17.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.2/jackson-annotations-2.17.2.jar" -OutFile "jackson-annotations-2.17.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.17.2/jackson-datatype-jsr310-2.17.2.jar" -OutFile "jackson-datatype-jsr310-2.17.2.jar"

# SLF4J
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar" -OutFile "slf4j-api-2.0.13.jar"

# Logback
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.6/logback-classic-1.5.6.jar" -OutFile "logback-classic-1.5.6.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.6/logback-core-1.5.6.jar" -OutFile "logback-core-1.5.6.jar"

# SQLite
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.0/sqlite-jdbc-3.46.1.0.jar" -OutFile "sqlite-jdbc-3.46.1.0.jar"

cd ..\..

Write-Host "✅ Downloaded all libraries successfully!" -ForegroundColor Green
Write-Host "⚠️  Remember to download JavaFX 21.0.9 separately!" -ForegroundColor Yellow
```

Lưu nội dung trên vào file `download-libs-windows.ps1` và chạy trong PowerShell.

---

## 📦 Yêu cầu hệ thống

| Thành phần | Yêu cầu |
|------------|---------|
| **OS** | Windows 10/11 (64-bit) |
| **Java** | JDK 21 (64-bit) |
| **JavaFX** | 21.0.9 |
| **RAM** | Tối thiểu 4GB |
| **Disk** | ~100MB cho thư viện |

---

## ✅ Tóm tắt

1. ✅ Tải JavaFX 21.0.9 cho Windows từ link trên
2. ✅ Giải nén và copy các JAR vào `lib\javafx`
3. ✅ Cấu hình IDE với VM options
4. ✅ Rebuild project và chạy

**Link tải:** https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip

---

**🎉 Hoàn tất! Project sẽ chạy được trên Windows với Java 21.**

