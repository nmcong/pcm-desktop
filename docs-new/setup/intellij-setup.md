# IntelliJ IDEA Setup Guide for PCM Desktop

## 🚨 Quick Fix for "package org.slf4j does not exist" Error

### Method 1: Invalidate Caches (Try This First!)

1. **File → Invalidate Caches...**
2. Select ALL options:
   - ✅ Clear file system cache and Local History
   - ✅ Clear downloaded shared indexes
   - ✅ Clear VCS Log caches and indexes
   - ✅ Invalidate code completion and symbol name caches
3. Click **"Invalidate and Restart"**
4. Wait for IntelliJ to restart and re-index (1-2 minutes)
5. **Build → Rebuild Project**

✅ **This fixes 95% of library recognition issues!**

---

## Method 2: Complete Library Re-import

If Method 1 doesn't work, follow these steps:

### Step 1: Remove Existing Libraries

1. **File → Project Structure** (`⌘;` on Mac, `Ctrl+Alt+Shift+S` on Windows)
2. Click **Libraries** in left sidebar
3. Find library named **"others"**
4. Click **-** (minus button) to remove it
5. Find library named **"javafx"**  
6. Click **-** (minus button) to remove it
7. Click **Apply**

### Step 2: Re-add Libraries

#### Add JavaFX:
1. Click **+** (plus button)
2. Select **Java**
3. Navigate to: `/Users/nguyencong/Workspace/pcm-desktop/lib/javafx`
4. Select ALL `.jar` files (`⌘A` or `Ctrl+A`)
5. Click **OK**
6. Name: **javafx**
7. Click **OK**

#### Add Others:
1. Click **+** (plus button)
2. Select **Java**
3. Navigate to: `/Users/nguyencong/Workspace/pcm-desktop/lib/others`
4. Select ALL `.jar` files (`⌘A` or `Ctrl+A`)
5. Click **OK**
6. Name: **others**
7. Click **OK**

### Step 3: Verify Module Dependencies

1. Still in **Project Structure**, click **Modules**
2. Select **pcm-desktop** module
3. Click **Dependencies** tab
4. Ensure both libraries are listed:
   - ✅ javafx (Compile)
   - ✅ others (Compile)
5. If not, click **+** → **Library** → Add them
6. Click **Apply** → **OK**

### Step 4: Configure Annotation Processing (for Lombok)

1. **File → Settings** (`⌘,` on Mac, `Ctrl+Alt+S` on Windows)
2. Navigate to: **Build, Execution, Deployment → Compiler → Annotation Processors**
3. ✅ Enable annotation processing
4. Click **Apply** → **OK**

### Step 5: Install Lombok Plugin

1. **File → Settings → Plugins**
2. Search for **"Lombok"**
3. Install **Lombok Plugin** by JetBrains
4. Restart IntelliJ if prompted

### Step 6: Rebuild Project

1. **Build → Rebuild Project**
2. Wait for build to complete
3. Check for errors in **Build** window

---

## Method 3: Verify from Command Line

If IDE still has issues, verify that compilation works from command line:

```bash
cd /Users/nguyencong/Workspace/pcm-desktop

# Compile all Java files
javac -cp "lib/javafx/*:lib/others/*" \
  -d out \
  -encoding UTF-8 \
  src/main/java/com/noteflix/pcm/**/*.java
```

If this succeeds but IntelliJ still shows errors, it's definitely an IDE cache issue.

---

## ⚙️ Run Configuration Setup

After libraries are recognized:

### Create Run Configuration:

1. **Run → Edit Configurations...**
2. Click **+** → **Application**
3. Fill in:
   - **Name:** PCM Desktop
   - **Module:** pcm-desktop
   - **Main class:** `com.noteflix.pcm.PCMApplication`
   - **VM options:**
     ```
     --module-path lib/javafx 
     --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
     ```
   - **Working directory:** `$ProjectFileDir$`
4. Click **Apply** → **OK**

### Run the Application:

- Click **Run** button (▶️) or press `⌃R` (Mac) / `Shift+F10` (Windows)

---

## 🔍 Troubleshooting

### Issue: "Cannot resolve symbol 'log'"

**Cause:** Lombok not working

**Solution:**
1. Install Lombok plugin
2. Enable annotation processing
3. Restart IntelliJ
4. Rebuild project

### Issue: "Error: JavaFX runtime components are missing"

**Cause:** VM options not set correctly

**Solution:**
- Add VM options to run configuration:
  ```
  --module-path lib/javafx --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media
  ```

### Issue: Libraries not showing up in External Libraries

**Cause:** .idea cache corruption

**Solution:**
1. Close IntelliJ
2. Delete `.idea` folder:
   ```bash
   rm -rf /Users/nguyencong/Workspace/pcm-desktop/.idea
   ```
3. Re-open project in IntelliJ
4. Re-configure libraries (Method 2 above)

### Issue: "class file has wrong version 67.0, should be 65.0"

**Cause:** Using JavaFX 25 instead of JavaFX 21

**Solution:**
1. Delete all JARs in `lib/javafx/`
2. Download JavaFX 21.0.9 for your platform
3. Extract and copy JARs to `lib/javafx/`
4. Invalidate caches and restart

---

## 📋 Checklist

Before asking for help, verify:

- [ ] All library JAR files exist (run `./verify-libs.sh`)
- [ ] IntelliJ caches invalidated and restarted
- [ ] Libraries added to Project Structure
- [ ] Lombok plugin installed
- [ ] Annotation processing enabled
- [ ] Project rebuilt
- [ ] Run configuration has correct VM options
- [ ] Java SDK is Java 21 (not 17 or 23)

---

## 💡 Pro Tips

### Faster Cache Clearing:
```bash
# macOS/Linux
rm -rf ~/Library/Caches/JetBrains/IntelliJIdea*/

# Then restart IntelliJ
```

### Verify Setup Script:
```bash
./verify-libs.sh
```

### Quick Rebuild:
```bash
⌘⇧F9  (Mac)
Ctrl+Shift+F9  (Windows)
```

---

## 🆘 Still Having Issues?

1. Check IntelliJ IDEA version (should be 2023.3+)
2. Check Java SDK version (should be Java 21)
3. Check that you're using correct JavaFX for your platform
4. Try creating a new IntelliJ project and import this one fresh

---

**Last Updated:** November 12, 2024