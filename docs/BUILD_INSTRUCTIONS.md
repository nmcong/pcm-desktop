# PCM Desktop - Build Instructions After Refactoring

## 🔧 Important: First Build After Refactoring

After the refactoring to MVVM architecture (v4.0.0), you need to rebuild the project to resolve module dependencies.

---

## ⚠️ Expected Linter Errors (Before Build)

You may see errors like:
- `javafx.controls cannot be resolved to a module`
- `lombok cannot be resolved to a module`
- `javafx.beans.property is not accessible`

**These are normal!** They will be resolved after the first build.

---

## 🚀 Build Steps

### Option 1: Using Scripts (Recommended)

```bash
# Clean and build
./scripts/build.sh

# Or on Windows
scripts\build.bat
```

### Option 2: Manual Build

```bash
# If using Gradle
gradle clean build

# If using Maven
mvn clean compile
```

### Option 3: IDE Build

1. Open project in IntelliJ IDEA or Eclipse
2. Right-click on project → **Build** or **Clean and Build**
3. Wait for dependencies to resolve

---

## ✅ Verification

After building, verify the refactoring:

1. **Check for compilation errors:**
   ```bash
   # Should complete without errors
   ./scripts/build.sh
   ```

2. **Run the application:**
   ```bash
   ./scripts/run.sh
   ```

3. **Verify new components:**
   - DI system should initialize (check logs)
   - i18n should be loaded (check console)
   - Application should start normally

---

## 📝 What Was Added

The refactoring added these new files:

### Core Infrastructure
- ✅ `core/di/Injector.java` - Dependency injection
- ✅ `core/i18n/I18n.java` - Internationalization
- ✅ `core/navigation/Route.java` - Navigation routes
- ✅ `core/utils/Asyncs.java` - Async utilities
- ✅ `core/utils/DialogService.java` - Dialog management
- ✅ `core/utils/FxBindings.java` - Binding helpers

### ViewModels (MVVM Pattern)
- ✅ `ui/viewmodel/BaseViewModel.java` - Base class
- ✅ `ui/viewmodel/AIAssistantViewModel.java` - AI chat
- ✅ `ui/viewmodel/SettingsViewModel.java` - Settings

### Resources
- ✅ `resources/i18n/messages.properties` - English
- ✅ `resources/i18n/messages_vi.properties` - Vietnamese

### Module System
- ✅ `src/main/java/module-info.java` - JPMS descriptor

### Documentation
- ✅ `docs/ARCHITECTURE_REFACTORING.md` - Architecture guide
- ✅ `docs/REFACTORING_QUICK_START.md` - Quick start
- ✅ `docs/development/REFACTORING_README.md` - README
- ✅ `REFACTORING_SUMMARY.md` - Summary
- ✅ `BUILD_INSTRUCTIONS.md` - This file

---

## 🐛 Troubleshooting

### If build fails with module errors:

1. **Check Java version:**
   ```bash
   java -version
   # Should be Java 21 or higher
   ```

2. **Verify dependencies:**
   - Check that all JARs in `lib/` are present
   - Verify JavaFX libraries are available

3. **Clean build:**
   ```bash
   # Remove compiled files
   rm -rf out/
   
   # Rebuild
   ./scripts/build.sh
   ```

### If module-info.java errors persist:

The `module-info.java` expects these modules. Verify they're available:
- JavaFX (javafx.controls, javafx.graphics, javafx.base)
- AtlantaFX (atlantafx.base)
- Ikonli (org.kordamp.ikonli.*)
- Logging (org.slf4j, ch.qos.logback.*)
- Lombok (lombok)
- SQLite (org.xerial.sqlitejdbc)
- Jackson (com.fasterxml.jackson.*)

### If DI errors occur at runtime:

Check that `Injector.getInstance()` is called in `PCMApplication.init()`:
```java
@Override
public void init() throws Exception {
    Injector injector = Injector.getInstance();
    I18n.setLocale("en");
    // ...
}
```

---

## 🎯 Expected Build Output

Successful build should show:
```
🔧 Initializing Dependency Injection...
✅ DI Container initialized
🌍 Initializing internationalization...
✅ i18n initialized: English
🔄 Running database migrations...
✅ Database migrations completed
✅ Application started successfully
```

---

## 📦 Dependency Check

Verify these libraries exist in `lib/`:

### JavaFX (lib/javafx/)
- javafx.base.jar
- javafx.controls.jar
- javafx.graphics.jar
- javafx.fxml.jar
- javafx.web.jar

### Others (lib/others/)
- atlantafx-base-2.0.1.jar
- ikonli-core-12.3.1.jar
- ikonli-javafx-12.3.1.jar
- ikonli-feather-pack-12.3.1.jar
- lombok-1.18.34.jar
- slf4j-api-2.0.16.jar
- logback-classic-1.5.12.jar
- logback-core-1.5.12.jar
- sqlite-jdbc-3.47.1.0.jar
- jackson-databind-2.18.2.jar
- jackson-core-2.18.2.jar
- jackson-annotations-2.18.2.jar

---

## 🔄 After Successful Build

1. **Test the application:**
   ```bash
   ./scripts/run.sh
   ```

2. **Verify i18n:**
   - Check menu labels
   - Test language switching (if implemented)

3. **Test AI Assistant:**
   - Navigate to AI Assistant page
   - Verify ViewModel is working
   - Check async operations

4. **Check logs:**
   - Look for DI initialization
   - Verify i18n loading
   - Check for any warnings

---

## 📚 Next Steps

After successful build:

1. **Read Documentation:**
   - `REFACTORING_SUMMARY.md` - What changed
   - `docs/ARCHITECTURE_REFACTORING.md` - How it works
   - `docs/REFACTORING_QUICK_START.md` - How to use

2. **Study Examples:**
   - `ui/viewmodel/AIAssistantViewModel.java` - Complete example
   - `core/di/Injector.java` - DI system
   - `core/utils/Asyncs.java` - Async patterns

3. **Apply Patterns:**
   - Refactor remaining pages to use ViewModels
   - Use new utilities in your code
   - Follow MVVM pattern

---

## ✅ Build Checklist

- [ ] Run `./scripts/build.sh` (or equivalent)
- [ ] Build completes without errors
- [ ] Run `./scripts/run.sh`
- [ ] Application starts successfully
- [ ] Check logs for DI and i18n initialization
- [ ] No runtime errors on startup
- [ ] AI Assistant page loads correctly

---

## 💡 Pro Tips

1. **Clean build** if you see weird errors:
   ```bash
   rm -rf out/
   ./scripts/build.sh
   ```

2. **Use IDE** for better error messages:
   - IntelliJ IDEA will show more detailed errors
   - Auto-completion works better after first build

3. **Check logs** for issues:
   - Console output shows initialization steps
   - Log file: `logs/pcm-desktop.log`

---

## 🎉 Success!

If the application starts without errors and you see the DI/i18n initialization messages, the refactoring is working correctly!

**Next:** Read the documentation and start applying the patterns to your code.

---

**Version:** 4.0.0  
**Status:** Ready to build and run  
**Architecture:** MVVM with Best Practices

Happy coding! 🚀

