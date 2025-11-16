# PCM Desktop - Refactoring to MVVM Architecture

## 🎯 Overview

PCM Desktop đã được refactor theo **MVVM architecture** với **Best Practices**, **Clean Code**, và **SOLID principles**.

**Version:** 4.0.0  
**Date:** November 2025  
**Status:** ✅ Core framework complete

---

## 📚 Documentation

### Tiếng Việt

1. **BESTPRACTICES.md** - Hướng dẫn Best Practices gốc
2. **BESTPRACTICES_02.md** - Các pattern nâng cao
3. **REFACTORING_SUMMARY.md** - Tổng kết refactoring

### English

1. **docs/ARCHITECTURE_REFACTORING.md** - Complete architecture guide
2. **docs/REFACTORING_QUICK_START.md** - Quick start guide with examples
3. **REFACTORING_SUMMARY.md** - Summary of changes

---

## ✅ What Was Completed

### Core Infrastructure (100%)

- ✅ Dependency Injection system (`Injector`)
- ✅ Async utilities (`Asyncs`)
- ✅ Dialog service (`DialogService`)
- ✅ i18n support (`I18n`, message files)
- ✅ Binding helpers (`FxBindings`)
- ✅ Navigation system (`Route`)
- ✅ Module system (`module-info.java`)

### ViewModels (Examples Created)

- ✅ Base ViewModel pattern
- ✅ AIAssistantViewModel (complete example)
- ✅ SettingsViewModel (complete example)
- ⏳ Other pages (framework ready, needs implementation)

### Application Updates

- ✅ PCMApplication - DI and i18n initialization
- ✅ MainController - Using new utilities
- ✅ Injector - Service and ViewModel registration

### Documentation (Comprehensive)

- ✅ Architecture guide (detailed)
- ✅ Quick start guide (examples)
- ✅ Refactoring summary
- ✅ Code comments and JavaDoc

---

## 🚀 Quick Start

### 1. Read Documentation

```bash
# Start here
cat REFACTORING_SUMMARY.md

# Detailed architecture
cat docs/ARCHITECTURE_REFACTORING.md

# Quick examples
cat docs/REFACTORING_QUICK_START.md
```

### 2. Study Examples

```bash
# ViewModels
src/main/java/com/noteflix/pcm/ui/viewmodel/
├── BaseViewModel.java          # Base class
├── AIAssistantViewModel.java   # Complete example
└── SettingsViewModel.java      # Settings example

# Core utilities
src/main/java/com/noteflix/pcm/core/
├── di/Injector.java            # DI container
├── i18n/I18n.java              # i18n support
└── utils/
    ├── Asyncs.java             # Async utilities
    ├── DialogService.java      # Dialogs
    └── FxBindings.java         # Binding helpers
```

### 3. Run the Application

```bash
# Using scripts
./scripts/run.sh

# Or with Gradle/Maven
gradle run
```

---

## 📖 How to Use

### Creating a New Page with ViewModel

```java
// 1. Create ViewModel
public class MyViewModel extends BaseViewModel {
    private final StringProperty data = new SimpleStringProperty("");
    
    public MyViewModel(MyService service) {
        this.service = service;
    }
    
    public void loadData() {
        setBusy(true);
        Asyncs.runAsync(
            () -> service.fetch(),
            result -> { 
                data.set(result); 
                setBusy(false); 
            }
        );
    }
    
    public StringProperty dataProperty() { return data; }
}

// 2. Register in Injector
// In Injector.registerDefaults():
registerFactory(MyViewModel.class, () -> 
    new MyViewModel(get(MyService.class))
);

// 3. Create Page with binding
public class MyPage extends BasePage {
    private final MyViewModel vm;
    
    public MyPage() {
        this(Injector.getInstance().get(MyViewModel.class));
    }
    
    public MyPage(MyViewModel vm) {
        super("My Page", "Description", icon);
        this.vm = vm;
    }
    
    @Override
    protected VBox createMainContent() {
        Label label = new Label();
        label.textProperty().bind(vm.dataProperty());
        
        Button btn = new Button("Load");
        btn.setOnAction(e -> vm.loadData());
        btn.disableProperty().bind(vm.busyProperty());
        
        return new VBox(label, btn);
    }
}
```

---

## 🎓 Key Concepts

### 1. MVVM Pattern

- **View**: UI components (JavaFX)
- **ViewModel**: UI state + commands
- **Model**: Domain entities + services

### 2. Dependency Injection

```java
// Get services via DI
Injector injector = Injector.getInstance();
ConversationService service = injector.get(ConversationService.class);
```

### 3. Property Binding

```java
// Automatic UI updates
label.textProperty().bind(viewModel.nameProperty());
button.disableProperty().bind(viewModel.busyProperty());
```

### 4. Async Operations

```java
// Non-blocking IO
Asyncs.runAsync(
    () -> database.load(),          // Background
    data -> updateUI(data),          // Success (FX thread)
    error -> showError(error)        // Error (FX thread)
);
```

### 5. Internationalization

```java
// Multi-language
I18n.setLocale("vi");
String title = I18n.get("app.title");
```

---

## 🔄 Migration Guide

### For Existing Pages

1. **Extract to ViewModel:**
    - Move properties to ViewModel
    - Move logic to ViewModel methods
    - Keep only UI wiring in Page/Controller

2. **Add Binding:**
    - Replace setText/setDisable with binding
    - Use ObservableList for tables

3. **Use Utilities:**
    - DialogService for dialogs
    - Asyncs for background tasks
    - I18n for text

4. **Register in DI:**
    - Add ViewModel factory to Injector

---

## 📝 TODO for Complete Migration

### Optional Enhancements

- [ ] Refactor all remaining pages to use ViewModels
- [ ] Reorganize into feature-based packages (optional)
- [ ] Add more i18n languages
- [ ] Write unit tests for ViewModels
- [ ] Add TestFX integration tests

**Note:** Core framework is complete and functional. Above items are enhancements for full migration.

---

## 🐛 Troubleshooting

### If you see "Service not found" errors:

- Check if service is registered in `Injector.registerDefaults()`

### If UI doesn't update:

- Use property binding instead of manual updates
- Check if properties are Observable (StringProperty, etc.)

### If app blocks:

- Move IO operations to `Asyncs.runAsync()`
- Never do network/DB calls on FX thread

### If i18n keys show as "!key!":

- Check if key exists in `resources/i18n/messages.properties`
- Verify bundle is loaded with `I18n.getBundle()`

---

## 📦 Project Structure

```
pcm-desktop/
├── src/main/java/com/noteflix/pcm/
│   ├── PCMApplication.java          ✅ Updated
│   ├── core/                        ✅ NEW! Infrastructure
│   │   ├── di/                      ✅ DI system
│   │   ├── i18n/                    ✅ i18n support
│   │   ├── navigation/              ✅ Navigation
│   │   ├── utils/                   ✅ Utilities
│   │   └── ...
│   ├── ui/
│   │   ├── viewmodel/               ✅ NEW! ViewModels
│   │   ├── pages/                   
│   │   ├── MainController.java      ✅ Updated
│   │   └── ...
│   ├── domain/                      Domain layer
│   ├── application/                 Service layer
│   ├── infrastructure/              Data layer
│   └── llm/                         LLM integration
├── src/main/resources/
│   ├── i18n/                        ✅ NEW! Message files
│   │   ├── messages.properties
│   │   └── messages_vi.properties
│   └── ...
├── docs/
│   ├── ARCHITECTURE_REFACTORING.md  ✅ NEW!
│   ├── REFACTORING_QUICK_START.md   ✅ NEW!
│   └── ...
├── REFACTORING_SUMMARY.md           ✅ NEW!
├── BESTPRACTICES.md                 Original guide
└── BESTPRACTICES_02.md              Advanced guide
```

---

## 🎯 Benefits

### For Developers

- ✅ Cleaner, more maintainable code
- ✅ Easy to test (ViewModels)
- ✅ Less boilerplate
- ✅ Type-safe navigation
- ✅ Better IDE support

### For the Project

- ✅ Follows industry standards
- ✅ SOLID principles applied
- ✅ Easy to extend
- ✅ Well documented
- ✅ Scalable architecture

### For Users

- ✅ Responsive UI (non-blocking)
- ✅ Consistent UX
- ✅ Multi-language support
- ✅ Better error handling

---

## 📚 Learning Path

1. **Start:** Read `REFACTORING_SUMMARY.md`
2. **Deep dive:** Read `docs/ARCHITECTURE_REFACTORING.md`
3. **Practice:** Check `docs/REFACTORING_QUICK_START.md`
4. **Study:** Review `ui/viewmodel/AIAssistantViewModel.java`
5. **Apply:** Refactor your own pages using the pattern

---

## 💡 Tips

- Start small - refactor one page at a time
- Use binding - reduce manual UI updates
- Think async - IO on background thread
- Test ViewModels - they're easy to test
- Follow examples - AIAssistantViewModel is complete

---

## 🎉 Success Criteria

✅ Core framework complete  
✅ DI system working  
✅ Async utilities available  
✅ i18n system functional  
✅ Example ViewModels created  
✅ Documentation comprehensive  
✅ Application running with new architecture

---

## 📞 Need Help?

Check these resources:

1. `REFACTORING_SUMMARY.md` - What changed
2. `docs/ARCHITECTURE_REFACTORING.md` - How it works
3. `docs/REFACTORING_QUICK_START.md` - How to use
4. `BESTPRACTICES.md` & `BESTPRACTICES_02.md` - Why it matters

---

**Status:** ✅ Ready to use  
**Next Steps:** Apply patterns to remaining pages (optional)

Happy coding! 🚀

