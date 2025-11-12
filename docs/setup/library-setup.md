# 📚 Library Setup Guide - Manual Import

## 📦 Required Libraries (Latest Versions - Nov 2024)

### 1. JavaFX 21.0.9 (For Java 21)

**Download:**
- **Site:** https://gluonhq.com/products/javafx/
- **Direct:** https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip (Windows)
- **Direct:** https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-aarch64_bin-sdk.zip (macOS M1/M2/M3)
- **Direct:** https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-x64_bin-sdk.zip (macOS Intel)
- **Direct:** https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_linux-x64_bin-sdk.zip (Linux)

**Files Needed:**
```
lib/javafx/
├── javafx.base.jar
├── javafx.controls.jar
├── javafx.fxml.jar
├── javafx.graphics.jar
├── javafx.media.jar
├── javafx.swing.jar
├── javafx.web.jar
└── javafx-swt.jar
```

### 2. Lombok 1.18.34 (Latest)

**Download:**
- **Site:** https://projectlombok.org/download
- **Direct:** https://projectlombok.org/downloads/lombok.jar

**File:**
```
lib/others/
└── lombok-1.18.34.jar
```

### 3. Jackson 2.18.2 (Latest)

**Download from Maven Central:**
- **jackson-databind:** https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.18.2/jackson-databind-2.18.2.jar
- **jackson-core:** https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.18.2/jackson-core-2.18.2.jar
- **jackson-annotations:** https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.18.2/jackson-annotations-2.18.2.jar
- **jackson-datatype-jsr310:** https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.18.2/jackson-datatype-jsr310-2.18.2.jar

**Files:**
```
lib/others/
├── jackson-databind-2.18.2.jar
├── jackson-core-2.18.2.jar
├── jackson-annotations-2.18.2.jar
└── jackson-datatype-jsr310-2.18.2.jar
```

### 4. SLF4J 2.0.16 (Latest)

**Download from Maven Central:**
- **slf4j-api:** https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar
- **slf4j-simple:** https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.16/slf4j-simple-2.0.16.jar

**Files:**
```
lib/others/
├── slf4j-api-2.0.16.jar
└── slf4j-simple-2.0.16.jar
```

### 5. AtlantaFX 2.1.0 (Latest)

**Download from Maven Central:**
- **atlantafx-base:** https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/2.1.0/atlantafx-base-2.1.0.jar

**File:**
```
lib/others/
└── atlantafx-base-2.1.0.jar
```

### 6. Ikonli 12.3.1 (Latest)

**Download from Maven Central:**
- **ikonli-core:** https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-core/12.3.1/ikonli-core-12.3.1.jar
- **ikonli-javafx:** https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-javafx/12.3.1/ikonli-javafx-12.3.1.jar
- **ikonli-material2-pack:** https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-material2-pack/12.3.1/ikonli-material2-pack-12.3.1.jar
- **ikonli-feather-pack:** https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-feather-pack/12.3.1/ikonli-feather-pack-12.3.1.jar
- **ikonli-fontawesome5-pack:** https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-fontawesome5-pack/12.3.1/ikonli-fontawesome5-pack-12.3.1.jar

**Files:**
```
lib/others/
├── ikonli-core-12.3.1.jar
├── ikonli-javafx-12.3.1.jar
├── ikonli-material2-pack-12.3.1.jar
├── ikonli-feather-pack-12.3.1.jar
└── ikonli-fontawesome5-pack-12.3.1.jar
```

### 7. SQLite JDBC 3.47.1.0 (Latest)

**Download from Maven Central:**
- **sqlite-jdbc:** https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.47.1.0/sqlite-jdbc-3.47.1.0.jar

**File:**
```
lib/others/
└── sqlite-jdbc-3.47.1.0.jar
```

### 8. OkHttp 4.12.0 (For HTTP Clients)

**Download from Maven Central:**
- **okhttp:** https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.12.0/okhttp-4.12.0.jar
- **okio:** https://repo1.maven.org/maven2/com/squareup/okio/okio-jvm/3.9.1/okio-jvm-3.9.1.jar
- **kotlin-stdlib:** https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.0.21/kotlin-stdlib-2.0.21.jar

**Files:**
```
lib/others/
├── okhttp-4.12.0.jar
├── okio-jvm-3.9.1.jar
└── kotlin-stdlib-2.0.21.jar
```

---

## 📥 Quick Download Script

### macOS/Linux:

```bash
#!/bin/bash
# download-libs.sh

# Create directories
mkdir -p lib/javafx lib/others

cd lib

# JavaFX 21.0.9 (adjust URL for your platform)
echo "Downloading JavaFX..."
# For macOS M1/M2/M3:
wget -O javafx.zip https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-aarch64_bin-sdk.zip
# For macOS Intel:
# wget -O javafx.zip https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_osx-x64_bin-sdk.zip
# For Windows:
# wget -O javafx.zip https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip

unzip javafx.zip
mv javafx-sdk-21.0.9/lib/* javafx/
rm -rf javafx-sdk-21.0.9 javafx.zip

# Lombok
echo "Downloading Lombok..."
wget -O others/lombok-1.18.34.jar https://projectlombok.org/downloads/lombok.jar

# Jackson
echo "Downloading Jackson..."
wget -O others/jackson-databind-2.18.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.18.2/jackson-databind-2.18.2.jar
wget -O others/jackson-core-2.18.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.18.2/jackson-core-2.18.2.jar
wget -O others/jackson-annotations-2.18.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.18.2/jackson-annotations-2.18.2.jar
wget -O others/jackson-datatype-jsr310-2.18.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.18.2/jackson-datatype-jsr310-2.18.2.jar

# SLF4J
echo "Downloading SLF4J..."
wget -O others/slf4j-api-2.0.16.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar
wget -O others/slf4j-simple-2.0.16.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.16/slf4j-simple-2.0.16.jar

# AtlantaFX
echo "Downloading AtlantaFX..."
wget -O others/atlantafx-base-2.1.0.jar https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/2.1.0/atlantafx-base-2.1.0.jar

# Ikonli
echo "Downloading Ikonli..."
wget -O others/ikonli-core-12.3.1.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-core/12.3.1/ikonli-core-12.3.1.jar
wget -O others/ikonli-javafx-12.3.1.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-javafx/12.3.1/ikonli-javafx-12.3.1.jar
wget -O others/ikonli-material2-pack-12.3.1.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-material2-pack/12.3.1/ikonli-material2-pack-12.3.1.jar
wget -O others/ikonli-feather-pack-12.3.1.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-feather-pack/12.3.1/ikonli-feather-pack-12.3.1.jar
wget -O others/ikonli-fontawesome5-pack-12.3.1.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-fontawesome5-pack/12.3.1/ikonli-fontawesome5-pack-12.3.1.jar

# SQLite JDBC
echo "Downloading SQLite JDBC..."
wget -O others/sqlite-jdbc-3.47.1.0.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.47.1.0/sqlite-jdbc-3.47.1.0.jar

# OkHttp
echo "Downloading OkHttp..."
wget -O others/okhttp-4.12.0.jar https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.12.0/okhttp-4.12.0.jar
wget -O others/okio-jvm-3.9.1.jar https://repo1.maven.org/maven2/com/squareup/okio/okio-jvm/3.9.1/okio-jvm-3.9.1.jar
wget -O others/kotlin-stdlib-2.0.21.jar https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.0.21/kotlin-stdlib-2.0.21.jar

echo "✅ All libraries downloaded successfully!"
echo "📁 Check lib/javafx/ and lib/others/ folders"

cd ..
```

### Windows (PowerShell):

```powershell
# download-libs.ps1

# Create directories
New-Item -ItemType Directory -Force -Path "lib\javafx", "lib\others"

Set-Location lib

# JavaFX 21.0.9
Write-Host "Downloading JavaFX..."
Invoke-WebRequest -Uri "https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip" -OutFile "javafx.zip"
Expand-Archive -Path "javafx.zip" -DestinationPath "." -Force
Move-Item "javafx-sdk-21.0.9\lib\*" "javafx\"
Remove-Item -Recurse -Force "javafx-sdk-21.0.9", "javafx.zip"

# Download other libraries (similar pattern with Invoke-WebRequest)
Write-Host "Downloading other libraries..."

# Lombok
Invoke-WebRequest -Uri "https://projectlombok.org/downloads/lombok.jar" -OutFile "others\lombok-1.18.34.jar"

# Jackson
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.18.2/jackson-databind-2.18.2.jar" -OutFile "others\jackson-databind-2.18.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.18.2/jackson-core-2.18.2.jar" -OutFile "others\jackson-core-2.18.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.18.2/jackson-annotations-2.18.2.jar" -OutFile "others\jackson-annotations-2.18.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.18.2/jackson-datatype-jsr310-2.18.2.jar" -OutFile "others\jackson-datatype-jsr310-2.18.2.jar"

# Continue with other libraries...

Set-Location ..

Write-Host "✅ All libraries downloaded successfully!"
```

---

## 📂 Final Folder Structure

After successful download and setup:

```
pcm-desktop/
├── lib/
│   ├── javafx/
│   │   ├── javafx.base.jar
│   │   ├── javafx.controls.jar
│   │   ├── javafx.fxml.jar
│   │   ├── javafx.graphics.jar
│   │   ├── javafx.media.jar
│   │   ├── javafx.swing.jar
│   │   ├── javafx.web.jar
│   │   └── javafx-swt.jar
│   └── others/
│       ├── lombok-1.18.34.jar
│       ├── jackson-databind-2.18.2.jar
│       ├── jackson-core-2.18.2.jar
│       ├── jackson-annotations-2.18.2.jar
│       ├── jackson-datatype-jsr310-2.18.2.jar
│       ├── slf4j-api-2.0.16.jar
│       ├── slf4j-simple-2.0.16.jar
│       ├── atlantafx-base-2.1.0.jar
│       ├── ikonli-core-12.3.1.jar
│       ├── ikonli-javafx-12.3.1.jar
│       ├── ikonli-material2-pack-12.3.1.jar
│       ├── ikonli-feather-pack-12.3.1.jar
│       ├── ikonli-fontawesome5-pack-12.3.1.jar
│       ├── sqlite-jdbc-3.47.1.0.jar
│       ├── okhttp-4.12.0.jar
│       ├── okio-jvm-3.9.1.jar
│       └── kotlin-stdlib-2.0.21.jar
├── src/
└── ...
```

---

## ✅ Verification

### Manual Verification:

```bash
# Check JavaFX files
ls -la lib/javafx/
# Should show 8 JAR files

# Check other libraries
ls -la lib/others/
# Should show 15+ JAR files

# Total file count
find lib -name "*.jar" | wc -l
# Should show 23+ files
```

### Script Verification:

Create `verify-libs.sh`:

```bash
#!/bin/bash

echo "🔍 Verifying library setup..."

JAVAFX_COUNT=$(ls lib/javafx/*.jar 2>/dev/null | wc -l)
OTHERS_COUNT=$(ls lib/others/*.jar 2>/dev/null | wc -l)

echo "JavaFX JARs: $JAVAFX_COUNT/8"
echo "Other JARs: $OTHERS_COUNT/15"

if [ $JAVAFX_COUNT -ge 8 ] && [ $OTHERS_COUNT -ge 15 ]; then
    echo "✅ All libraries are present!"
    echo "📁 Ready for IntelliJ import"
else
    echo "❌ Missing libraries detected"
    echo "📥 Run download script again"
fi

# Check specific required files
REQUIRED_FILES=(
    "lib/javafx/javafx.controls.jar"
    "lib/javafx/javafx.fxml.jar"
    "lib/others/lombok-1.18.34.jar"
    "lib/others/slf4j-api-2.0.16.jar"
    "lib/others/atlantafx-base-2.1.0.jar"
)

echo ""
echo "🔍 Checking critical files..."
for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file"
    fi
done
```

Make it executable:
```bash
chmod +x verify-libs.sh
./verify-libs.sh
```

---

## 🚨 Common Issues

### Issue: "wget command not found" (macOS)

**Solution:** Install wget:
```bash
brew install wget
```

### Issue: "Invoke-WebRequest not recognized" (Windows)

**Solution:** Use PowerShell (not Command Prompt):
```powershell
powershell -ExecutionPolicy Bypass -File download-libs.ps1
```

### Issue: Downloads are corrupted/incomplete

**Solution:** Clear and re-download:
```bash
rm -rf lib/
mkdir -p lib/javafx lib/others
# Run download script again
```

### Issue: Wrong JavaFX platform

**Symptoms:** "class file has wrong version" errors

**Solution:** Download correct platform:
- **macOS M1/M2/M3:** `openjfx-21.0.9_osx-aarch64_bin-sdk.zip`
- **macOS Intel:** `openjfx-21.0.9_osx-x64_bin-sdk.zip`
- **Windows:** `openjfx-21.0.9_windows-x64_bin-sdk.zip`
- **Linux:** `openjfx-21.0.9_linux-x64_bin-sdk.zip`

---

## 💡 Pro Tips

1. **Always verify checksums** for downloaded files (if available)
2. **Keep backup** of working library sets
3. **Document versions** you're using for reproducible builds
4. **Test compilation** after library setup:
   ```bash
   javac -cp "lib/javafx/*:lib/others/*" src/main/java/com/noteflix/pcm/PCMApplication.java
   ```

---

## 🔄 Updates

When updating libraries:

1. **Backup current `lib/` folder**
2. **Download new versions**
3. **Test compilation and runtime**
4. **Update this documentation with new versions**

---

**Last Updated:** November 12, 2024