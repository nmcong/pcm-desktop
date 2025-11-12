# 🚀 IntelliJ Run Configuration Instructions

## ⚠️ Fix: "JavaFX runtime components are missing"

### Problem
JavaFX không chạy vì thiếu module configuration.

### Solution: Configure Run Settings

#### Step 1: Open Run Configuration
- Menu: **Run → Edit Configurations...**
- Or: Click dropdown next to Run button → **Edit Configurations...**

#### Step 2: Create/Edit Application Configuration

Click **+** → **Application** (or edit existing)

Fill in these **EXACT** values:

```
┌─────────────────────────────────────────────────────────┐
│ Name: PCM Desktop                                       │
│                                                         │
│ Module: pcm-desktop                                     │
│                                                         │
│ Main class: com.noteflix.pcm.PCMApplication            │
│                                                         │
│ VM options:                                             │
│ --module-path lib/javafx                               │
│ --add-modules javafx.controls,javafx.fxml,javafx.web,  │
│               javafx.media                              │
│                                                         │
│ Working directory: $ProjectFileDir$                     │
└─────────────────────────────────────────────────────────┘
```

#### Step 3: Apply and Run

1. Click **Apply**
2. Click **OK**
3. Click **Run** button (▶️)

---

## 📋 Copy-Paste VM Options

```
--module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
```

---

## 🔍 Visual Guide

### Where to find "VM options":

```
Edit Configurations
├── Name: PCM Desktop
├── Build and run
│   ├── Module: pcm-desktop
│   └── Main class: com.noteflix.pcm.PCMApplication
├── Modify options
│   └── ☑ Add VM options  ← Click this if you don't see VM options field
└── VM options: [Enter here]
```

### If you don't see "VM options" field:

1. Look for **"Modify options"** link
2. Click it
3. Select: **☑ Add VM options**
4. The field will appear

---

## ✅ Verification

After configuration, the command should look like:

```bash
/path/to/java 
  --module-path lib/javafx 
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media 
  -classpath out/production/pcm-desktop:lib/others/* 
  com.noteflix.pcm.PCMApplication
```

**Key points:**
- ✅ `--module-path lib/javafx` is present
- ✅ `--add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media` is present
- ✅ JavaFX JARs are NOT in `-classpath`

---

## 🐛 Common Mistakes

### ❌ Wrong:
```
-classpath out:lib/javafx/*:lib/others/*
```
JavaFX in classpath = ERROR

### ✅ Correct:
```
--module-path lib/javafx 
--add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
-classpath out:lib/others/*
```
JavaFX in module-path = OK

---

## 🔧 Alternative: Run from Terminal

If IntelliJ is being difficult, you can always run from terminal:

```bash
cd /Users/nguyencong/Workspace/pcm-desktop

# Make sure code is compiled
javac -cp "lib/javafx/*:lib/others/*" \
  -d out \
  src/main/java/com/noteflix/pcm/**/*.java

# Run with proper JavaFX module config
java --module-path lib/javafx \
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media \
  -cp "out:lib/others/*" \
  com.noteflix.pcm.PCMApplication
```

---

## 📝 Creating .command file for macOS

Create a file `run-pcm.command`:

```bash
#!/bin/bash
cd "$(dirname "$0")"

java --module-path lib/javafx \
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media \
  -cp "out:lib/others/*" \
  com.noteflix.pcm.PCMApplication
```

Make it executable:
```bash
chmod +x run-pcm.command
```

Double-click to run!

---

## 🪟 Creating .bat file for Windows

Create a file `run-pcm.bat`:

```batch
@echo off
cd /d %~dp0

java --module-path lib\javafx ^
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media ^
  -cp "out;lib\others\*" ^
  com.noteflix.pcm.PCMApplication

pause
```

Double-click to run!

---

## 💡 Pro Tip: Save as Template

After creating the configuration:

1. Right-click on "PCM Desktop" configuration
2. Select **"Save 'PCM Desktop' as template"**
3. Future JavaFX projects can use this template

---

## 🆘 Still Not Working?

### Check Java Version:
```bash
java -version
# Should show: openjdk version "21.x.x"
```

### Check JavaFX Version:
```bash
ls -l lib/javafx/
# Should show JavaFX 21.0.9 JARs
```

### Check Module System:
```bash
java --list-modules | grep javafx
# Should show: javafx.base, javafx.controls, javafx.fxml, etc.
```

### Verify Libraries:
```bash
./verify-libs.sh
```

---

**Last Updated:** November 11, 2025

