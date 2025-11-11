# 🚀 Hướng dẫn nhanh cho Windows

## 📥 Bước 1: Cài đặt Java 21

Nếu chưa có Java 21, tải và cài đặt từ:
- **Adoptium (Khuyến nghị):** https://adoptium.net/
- Chọn version: **JDK 21 (LTS)**
- Download và cài đặt file `.msi`

Kiểm tra Java đã cài đặt:
```cmd
java -version
javac -version
```

Phải hiển thị: `openjdk version "21.x.x"`

---

## 📦 Bước 2: Tải thư viện

### Cách 1: Tự động (PowerShell - Khuyến nghị)

1. Mở **PowerShell** trong thư mục project
2. Chạy lệnh:
   ```powershell
   .\download-libs.ps1
   ```

Script sẽ tự động:
- ✅ Tải tất cả thư viện Java (Lombok, Jackson, SLF4J, Logback, SQLite)
- ✅ Tải JavaFX 21.0.9 cho Windows
- ✅ Giải nén và cài đặt vào đúng thư mục

### Cách 2: Thủ công

1. **Tải JavaFX 21.0.9:**
   ```
   https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip
   ```

2. **Giải nén** file ZIP

3. **Copy** tất cả file `.jar` từ `javafx-sdk-21.0.9\lib\` vào:
   ```
   pcm-desktop\lib\javafx\
   ```

4. **Tải các thư viện khác:**
   - Xem file `docs\LIBRARY_SETUP.md` để biết link tải
   - Hoặc chạy: `.\download-libs.ps1`

---

## 🔨 Bước 3: Biên dịch

Chạy file batch để biên dịch:
```cmd
compile-windows.bat
```

Hoặc thủ công:
```cmd
javac -cp "lib\javafx\*;lib\others\*" ^
  -d out ^
  src\main\java\com\noteflix\pcm\*.java ^
  src\main\java\com\noteflix\pcm\ui\*.java
```

---

## ▶️ Bước 4: Chạy ứng dụng

### Cách 1: Double-click file batch
```
run-windows.bat
```

### Cách 2: Từ Command Prompt
```cmd
java --module-path lib\javafx ^
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media ^
  -cp "out;lib\others\*" ^
  com.noteflix.pcm.PCMApplication
```

---

## 🎯 Tóm tắt nhanh

1. Cài Java 21
2. Chạy: `.\download-libs.ps1` (PowerShell)
3. Chạy: `compile-windows.bat`
4. Chạy: `run-windows.bat`

---

## 🛠️ Sử dụng với IDE

### IntelliJ IDEA (Khuyến nghị)

1. **Mở project:**
   - File → Open → Chọn thư mục `pcm-desktop`

2. **Cấu hình SDK:**
   - File → Project Structure → Project
   - SDK: Java 21
   - Language Level: 21

3. **Thêm libraries:**
   - File → Project Structure → Libraries
   - Add `lib\javafx` và `lib\others`

4. **Cài Lombok Plugin:**
   - File → Settings → Plugins
   - Tìm "Lombok" → Install → Restart

5. **Enable Annotation Processing:**
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - ✅ Enable annotation processing

6. **Tạo Run Configuration:**
   - Run → Edit Configurations → + → Application
   - Main class: `com.noteflix.pcm.PCMApplication`
   - VM options:
     ```
     --module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
     ```

7. **Run:** Click nút Run (▶️) hoặc Shift+F10

### Eclipse

1. Import project
2. Add libraries từ `lib\javafx` và `lib\others`
3. Cài Lombok: Chạy `java -jar lib\others\lombok-1.18.34.jar`
4. Cấu hình Run Configuration với VM arguments
5. Run application

### VS Code

1. Cài extension: **Extension Pack for Java**
2. Tạo file `.vscode\settings.json`:
   ```json
   {
     "java.project.sourcePaths": ["src/main/java"],
     "java.project.referencedLibraries": [
       "lib/javafx/*.jar",
       "lib/others/*.jar"
     ]
   }
   ```
3. Tạo file `.vscode\launch.json`:
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
4. Press F5 để chạy

---

## ❌ Xử lý lỗi

### Lỗi: "class file has wrong version 67.0, should be 65.0"

**Nguyên nhân:** Đang dùng JavaFX 23/25 thay vì JavaFX 21

**Giải pháp:**
- Xóa tất cả file trong `lib\javafx\`
- Tải JavaFX 21.0.9 từ link trên
- Copy các JAR mới vào `lib\javafx\`

### Lỗi: "Error: JavaFX runtime components are missing"

**Giải pháp:**
- Kiểm tra `lib\javafx\` có đủ 8 file JAR
- Thêm VM options: `--module-path lib/javafx --add-modules javafx.controls,javafx.fxml`

### Lỗi: "Cannot find symbol 'log'"

**Nguyên nhân:** Lombok chưa được cài đặt hoặc annotation processing chưa bật

**Giải pháp:**
- Cài Lombok plugin cho IDE
- Enable annotation processing trong IDE settings
- Restart IDE

### Lỗi: "Main class not found"

**Giải pháp:**
- Chạy `compile-windows.bat` trước
- Kiểm tra thư mục `out` đã có file `.class`

---

## 📁 Cấu trúc thư mục

```
pcm-desktop/
├── lib/
│   ├── javafx/              # 8 JAR files (~48MB)
│   │   ├── javafx.base.jar
│   │   ├── javafx.controls.jar
│   │   ├── javafx.fxml.jar
│   │   ├── javafx.graphics.jar
│   │   ├── javafx.media.jar
│   │   ├── javafx.swing.jar
│   │   ├── javafx.web.jar
│   │   └── javafx-swt.jar
│   └── others/              # 9 JAR files
│       ├── lombok-1.18.34.jar
│       ├── jackson-*.jar (4 files)
│       ├── slf4j-api-2.0.13.jar
│       ├── logback-*.jar (2 files)
│       └── sqlite-jdbc-3.46.1.0.jar
├── src/
│   └── main/
│       ├── java/
│       └── resources/
├── out/                     # Compiled classes
├── download-libs.ps1        # Tải thư viện
├── compile-windows.bat      # Biên dịch
└── run-windows.bat          # Chạy ứng dụng
```

---

## 📋 Checklist

- [ ] Java 21 đã cài đặt (`java -version`)
- [ ] Đã chạy `download-libs.ps1`
- [ ] Thư mục `lib\javafx` có 8 file JAR
- [ ] Thư mục `lib\others` có 9 file JAR
- [ ] Đã biên dịch (`compile-windows.bat`)
- [ ] Thư mục `out` có file `.class`
- [ ] Chạy được `run-windows.bat`

---

## 🔗 Links hữu ích

- **Java 21 (Adoptium):** https://adoptium.net/
- **JavaFX 21.0.9:** https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip
- **IntelliJ IDEA Community:** https://www.jetbrains.com/idea/download/
- **VS Code:** https://code.visualstudio.com/
- **Lombok:** https://projectlombok.org/

---

## 💡 Tips

1. **Dùng IntelliJ IDEA** để có trải nghiệm tốt nhất
2. **Bật Annotation Processing** để Lombok hoạt động
3. **Dùng Java 21 LTS** để đảm bảo tương thích
4. **Kiểm tra VM options** nếu JavaFX không chạy
5. **Rebuild project** sau khi thêm/xóa thư viện

---

**✅ Hoàn tất! Chúc bạn code vui vẻ! 🎉**

