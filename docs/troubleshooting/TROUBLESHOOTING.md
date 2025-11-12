# PCM Desktop - Khắc phục sự cố

## Vấn đề JavaFX trên macOS

### Lỗi Architecture Mismatch

**Mô tả lỗi:**
```
java.lang.UnsatisfiedLinkError: libprism_sw.dylib: dlopen failed (mach-o file, but is an incompatible architecture (have 'arm64', need 'x86_64'))
```

**Nguyên nhân:** 
Sử dụng Java x86_64 với thư viện JavaFX ARM64 trên máy Apple Silicon.

**Giải pháp:**
1. Cài đặt Java ARM64 (aarch64) cho Apple Silicon:
   ```bash
   brew install --cask temurin@21
   ```
   
2. Hoặc tải từ: https://adoptium.net/temurin/releases/?version=21&arch=aarch64&os=mac

3. Kiểm tra kiến trúc Java:
   ```bash
   file /path/to/java/bin/java
   # Kết quả cần: Mach-O 64-bit executable arm64
   ```

### Lỗi FXML Loading

**Mô tả lỗi:**
```
java.lang.IllegalStateException: Location is not set.
at javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:2556)
```

**Nguyên nhân:**
- File FXML không được copy vào thư mục output
- Đường dẫn resource không chính xác

**Giải pháp:**

1. **Sửa đường dẫn resource trong code:**
   ```java
   // Từ:
   getClass().getResource("/fxml/MainView.fxml")
   
   // Thành:
   getClass().getClassLoader().getResource("fxml/MainView.fxml")
   ```

2. **Copy resources vào thư mục output:**
   ```bash
   cp -r src/main/resources/* out/production/pcm-desktop/
   ```

3. **Hoặc cấu hình IDE để tự động copy resources khi build**

## Các lệnh hữu ích

### Kiểm tra kiến trúc hệ thống
```bash
uname -m  # Kết quả: arm64 (Apple Silicon) hoặc x86_64 (Intel)
```

### Kiểm tra kiến trúc Java
```bash
file /path/to/java/bin/java
```

### Kiểm tra kiến trúc thư viện
```bash
file /path/to/library.dylib
```

### Chạy ứng dụng với debug
```bash
java -Dprism.verbose=true -Dprism.order=sw -Djava.awt.headless=false [other-options] MainClass
```

## Cấu hình IDE

### IntelliJ IDEA

1. **Project SDK:** Đảm bảo sử dụng Java ARM64
2. **Build Settings:** Cấu hình copy resources:
   - File → Project Structure → Modules
   - Sources tab → Mark resources folder as "Resources"
3. **Run Configuration:** 
   - VM Options: `-Djava.library.path=lib/javafx`
   - Module path: `lib/javafx`
   - Add modules: `javafx.controls,javafx.fxml,javafx.web,javafx.media`

## Ghi chú

- Luôn đảm bảo Java và JavaFX cùng kiến trúc (arm64 hoặc x86_64)
- Kiểm tra resources được copy vào output directory
- Sử dụng ClassLoader.getResource() thay vì Class.getResource() khi có vấn đề với đường dẫn