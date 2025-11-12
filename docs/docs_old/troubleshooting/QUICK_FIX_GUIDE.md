# 🚀 Quick Fix Guide - JavaFX Runtime Missing

## ⚠️ Error Message
```
Error: JavaFX runtime components are missing, and are required to run this application
```

---

## ✅ Solution: 3 Options (Choose One)

### Option 1: Fix IntelliJ Configuration ⭐ (Recommended)

1. **Open Run Configuration:**
   - Run → Edit Configurations...

2. **Add VM options:**
   ```
   --module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
   ```

3. **Apply & Run**

**📖 Detailed Guide:** See `RUN_CONFIGURATION_INSTRUCTIONS.md`

---

### Option 2: Run from Terminal (Quick Test)

```bash
cd /Users/nguyencong/Workspace/pcm-desktop

# Compile (if needed)
javac -cp "lib/javafx/*:lib/others/*" \
  -d out \
  src/main/java/com/noteflix/pcm/**/*.java

# Run
java --module-path lib/javafx \
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media \
  -cp "out:lib/others/*" \
  com.noteflix.pcm.PCMApplication
```

---

### Option 3: Use .command Files (macOS - Easiest!)

Just double-click these files:

1. **`compile-macos.command`** - Compile the project
2. **`run-macos.command`** - Run the application

✅ No IntelliJ needed!

---

## 📝 For Windows Users

Use these batch files:

1. **`compile-windows.bat`** - Compile
2. **`run-windows.bat`** - Run

---

## 🔍 Why This Happens

JavaFX 9+ uses **Java Module System** and requires:
- `--module-path` instead of `-classpath`
- `--add-modules` to specify which JavaFX modules to load

**Wrong** ❌:
```
java -classpath lib/javafx/*:lib/others/*:out com.noteflix.pcm.PCMApplication
```

**Correct** ✅:
```
java --module-path lib/javafx 
     --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
     -classpath lib/others/*:out 
     com.noteflix.pcm.PCMApplication
```

---

## 📚 Full Documentation

| File | Description |
|------|-------------|
| `RUN_CONFIGURATION_INSTRUCTIONS.md` | Complete IntelliJ setup guide |
| `INTELLIJ_SETUP.md` | Full IntelliJ configuration |
| `verify-libs.sh` | Verify all libraries are present |
| `run-macos.command` | Run app on macOS (double-click) |
| `compile-macos.command` | Compile on macOS (double-click) |
| `run-windows.bat` | Run app on Windows |
| `compile-windows.bat` | Compile on Windows |

---

## ✅ Quick Checklist

Before running:
- [ ] All libraries present (`./verify-libs.sh`)
- [ ] Code compiled (check `out` folder exists)
- [ ] JavaFX 21.0.9 for your platform (macOS/Windows/Linux)
- [ ] Java 21 is being used (`java -version`)
- [ ] VM options configured in IntelliJ

---

## 🆘 Still Not Working?

### 1. Verify Libraries
```bash
./verify-libs.sh
```

### 2. Check Java Version
```bash
java -version
# Should show: openjdk version "21.x.x"
```

### 3. Check IntelliJ SDK
- File → Project Structure → Project
- SDK should be Java 21

### 4. Try Terminal
```bash
./run-macos.command
# or
.\run-windows.bat
```

If terminal works but IntelliJ doesn't = IntelliJ configuration issue

---

## 💡 Pro Tips

### Quick Test
```bash
# macOS/Linux
./run-macos.command

# Windows  
run-windows.bat
```

### Quick Recompile
```bash
# macOS/Linux
./compile-macos.command

# Windows
compile-windows.bat
```

### Verify Everything
```bash
./verify-libs.sh
```

---

## 🎯 Expected Behavior

When running correctly, you should see:

```
🚀 Starting PCM Desktop Application - AI-Powered System Analysis Tool...
[Logger messages]
✅ Application started successfully
```

And the JavaFX window should open showing:
- Welcome to PCM Desktop
- AI-Powered System Analysis & Business Management
- Navigation menu with Dashboard, Subsystems, Screens, etc.

---

**Quick Start:**
1. Try: `./run-macos.command` or `run-windows.bat`
2. If works: Great! Configure IntelliJ later
3. If not: Run `./verify-libs.sh` and check output

---

**Last Updated:** November 11, 2025

