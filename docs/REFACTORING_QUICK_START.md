# PCM Desktop Refactoring - Quick Start Guide

## 🚀 Quick Overview

PCM Desktop has been refactored to follow **MVVM architecture** with **Best Practices**, **Clean Code**, and **SOLID principles**.

---

## 📋 What's New

### Core Infrastructure

1. **✅ Dependency Injection (DI)** - `Injector` class
   - Manages service and ViewModel dependencies
   - Location: `com.noteflix.pcm.core.di.Injector`

2. **✅ ViewModels** - MVVM pattern
   - Separate UI state from UI components
   - Location: `com.noteflix.pcm.ui.viewmodel/`

3. **✅ Async Utilities** - Background task management
   - `Asyncs` class for non-blocking operations
   - Location: `com.noteflix.pcm.core.utils.Asyncs`

4. **✅ Dialog Service** - Centralized dialogs
   - `DialogService` for consistent UI
   - Location: `com.noteflix.pcm.core.utils.DialogService`

5. **✅ Internationalization (i18n)** - Multi-language support
   - `I18n` class with ResourceBundle
   - Location: `com.noteflix.pcm.core.i18n.I18n`

6. **✅ Navigation** - Type-safe routing
   - `Route` enum for navigation
   - Location: `com.noteflix.pcm.core.navigation.Route`

7. **✅ Binding Helpers** - Property binding utilities
   - `FxBindings` for common binding patterns
   - Location: `com.noteflix.pcm.core.utils.FxBindings`

8. **✅ Module System** - JPMS support
   - `module-info.java` for modular architecture

---

## 🎯 Quick Examples

### 1. Using Dependency Injection

```java
// Get the DI container
Injector injector = Injector.getInstance();

// Get a service
ConversationService service = injector.get(ConversationService.class);

// Get a ViewModel
AIAssistantViewModel viewModel = injector.get(AIAssistantViewModel.class);
```

### 2. Creating a ViewModel

```java
public class MyViewModel extends BaseViewModel {
    // Observable properties
    private final StringProperty name = new SimpleStringProperty("");
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    
    // Constructor with DI
    public MyViewModel(MyService service) {
        this.service = service;
    }
    
    // Commands (actions)
    public void save() {
        setBusy(true);
        Asyncs.runAsync(
            () -> service.saveData(name.get()),
            result -> {
                setBusy(false);
                DialogService.showInfo("Success", "Saved!");
            },
            error -> {
                setBusy(false);
                setError("Failed to save", error);
            }
        );
    }
    
    // Property accessors
    public StringProperty nameProperty() { return name; }
    public BooleanProperty validProperty() { return valid; }
}
```

### 3. Binding in View

```java
public class MyPage extends BasePage {
    private final MyViewModel viewModel;
    
    public MyPage() {
        this(Injector.getInstance().get(MyViewModel.class));
    }
    
    public MyPage(MyViewModel viewModel) {
        super("My Page", "Description", icon);
        this.viewModel = viewModel;
    }
    
    @Override
    protected VBox createMainContent() {
        TextField nameField = new TextField();
        nameField.textProperty().bindBidirectional(viewModel.nameProperty());
        
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> viewModel.save());
        saveButton.disableProperty().bind(
            viewModel.busyProperty().or(viewModel.validProperty().not())
        );
        
        return new VBox(nameField, saveButton);
    }
}
```

### 4. Using Async

```java
// Simple async operation
Asyncs.runAsync(
    () -> database.fetchData(),      // Background
    data -> label.setText(data),      // Success (UI thread)
    error -> showError(error)         // Error (UI thread)
);

// With JavaFX Task (for progress)
Task<String> task = Asyncs.createTask(() -> {
    // Long running task
    return heavyComputation();
});

task.progressProperty().addListener((obs, old, progress) -> {
    progressBar.setProgress(progress.doubleValue());
});

Asyncs.executeTask(task, 
    result -> label.setText(result),
    error -> showError(error)
);
```

### 5. Using DialogService

```java
// Info dialog
DialogService.showInfo("Success", "Operation completed");

// Error dialog
DialogService.showError("Error", exception);

// Confirmation
boolean confirmed = DialogService.showConfirm(
    "Delete", 
    "Are you sure you want to delete this?"
);

if (confirmed) {
    // Delete
}

// Input dialog
Optional<String> input = DialogService.showInput("Name", "Enter your name:");
input.ifPresent(name -> System.out.println("Hello " + name));

// Progress dialog
var progress = DialogService.showProgress("Loading", "Please wait...");
// ... do work ...
progress.updateMessage("Almost done...");
progress.updateProgress(0.8);
// ... finish ...
progress.close();
```

### 6. Using i18n

```java
// Set language
I18n.setLocale("vi"); // Vietnamese
I18n.setLocale("en"); // English

// Get message
String title = I18n.get("app.title");
String save = I18n.get("action.save");

// Format with parameters
String msg = I18n.format("time.minutes.ago", 5); // "5 minutes ago"

// Convenience methods
String appTitle = I18n.appTitle();
String actionSave = I18n.actionSave();
```

### 7. Property Binding Helpers

```java
// Check if text is not empty
BooleanBinding notEmpty = FxBindings.isNotEmpty(textField.textProperty());
button.disableProperty().bind(notEmpty.not());

// Check all conditions true
BooleanBinding allValid = FxBindings.allTrue(
    field1.validProperty(),
    field2.validProperty(),
    field3.validProperty()
);
saveButton.disableProperty().bind(allValid.not());

// Format string
StringBinding formatted = FxBindings.format(
    "Hello %s, you have %d messages",
    nameProperty,
    messageCountProperty
);
label.textProperty().bind(formatted);

// Bidirectional with conversion
FxBindings.bindBidirectionalWithConverter(
    textField.textProperty(),
    model.ageProperty(),
    Integer::parseInt,
    String::valueOf
);
```

---

## 📁 File Organization

### Before Refactoring
```
ui/
├── MainController.java (logic + UI)
├── pages/
    └── AIAssistantPage.java (all logic in page)
```

### After Refactoring
```
ui/
├── viewmodel/                    # NEW!
│   ├── BaseViewModel.java
│   ├── AIAssistantViewModel.java (UI state & commands)
│   └── SettingsViewModel.java
├── pages/
│   └── AIAssistantPage.java     (thin, just UI)
└── MainController.java           (thin, just wiring)

core/                              # NEW!
├── di/                           
│   └── Injector.java
├── i18n/
│   └── I18n.java
├── utils/
│   ├── Asyncs.java
│   ├── DialogService.java
│   └── FxBindings.java
└── navigation/
    └── Route.java

resources/i18n/                    # NEW!
├── messages.properties            (English)
└── messages_vi.properties         (Vietnamese)
```

---

## 🔄 Migration Checklist

If you're updating existing code:

- [ ] Extract business logic from Controllers to ViewModels
- [ ] Use Observable Properties instead of manual setters
- [ ] Replace manual dialogs with DialogService
- [ ] Use Asyncs for background tasks
- [ ] Use I18n instead of hardcoded strings
- [ ] Use DI to get services (don't use `new`)
- [ ] Add property bindings instead of manual UI updates
- [ ] Register ViewModels in Injector

---

## 🎓 Key Concepts

### 1. **Observable Properties**

Instead of:
```java
private String name;
public void setName(String name) {
    this.name = name;
    updateUI(); // Manual update
}
```

Use:
```java
private final StringProperty name = new SimpleStringProperty("");
public StringProperty nameProperty() { return name; }

// In UI
label.textProperty().bind(viewModel.nameProperty());
```

### 2. **Commands vs Event Handlers**

Instead of:
```java
button.setOnAction(e -> {
    // Lots of logic here
});
```

Use:
```java
// In ViewModel
public void save() {
    // Logic here
}

// In View
button.setOnAction(e -> viewModel.save());
```

### 3. **Async Pattern**

**Always** run IO on background thread:
```java
Asyncs.runAsync(
    () -> {
        // IO/Network on background thread
        return database.query();
    },
    result -> {
        // UI update on FX thread
        table.setItems(result);
    }
);
```

---

## 📚 Key Files to Check

1. **Injector** - `core/di/Injector.java`
   - See how services are registered
   - See how ViewModels are created

2. **AIAssistantViewModel** - `ui/viewmodel/AIAssistantViewModel.java`
   - Example of complete ViewModel
   - Shows property usage and commands

3. **Asyncs** - `core/utils/Asyncs.java`
   - See async patterns
   - Understand thread management

4. **PCMApplication** - `PCMApplication.java`
   - See initialization order
   - See how DI is set up

5. **I18n** - `core/i18n/I18n.java`
   - See how to use messages
   - Check message files in `resources/i18n/`

---

## ⚡ Performance Tips

1. **Use binding** instead of listeners when possible
2. **Run IO async** - never block UI thread
3. **Lazy load** - create pages only when needed
4. **Dispose resources** - clear bindings when done
5. **Use ObservableList** - automatic UI updates

---

## 🐛 Common Mistakes

### ❌ Creating services with `new`
```java
ConversationService service = new ConversationService(); // NO!
```

### ✅ Use DI instead
```java
ConversationService service = Injector.getInstance().get(ConversationService.class);
```

---

### ❌ Blocking UI thread
```java
button.setOnAction(e -> {
    String data = database.load(); // BLOCKS!
    label.setText(data);
});
```

### ✅ Use async
```java
button.setOnAction(e -> {
    Asyncs.runAsync(
        () -> database.load(),
        data -> label.setText(data)
    );
});
```

---

### ❌ Manual UI updates
```java
nameProperty.addListener((obs, old, newVal) -> {
    label.setText(newVal);
});
```

### ✅ Use binding
```java
label.textProperty().bind(nameProperty);
```

---

## 🎯 Next Steps

1. Read **ARCHITECTURE_REFACTORING.md** for detailed architecture
2. Study existing ViewModels (AIAssistantViewModel, SettingsViewModel)
3. Check examples in this guide
4. Start refactoring your code following the patterns

---

## 💡 Tips

- **Start small**: Refactor one page at a time
- **Test as you go**: Write tests for ViewModels
- **Use binding**: Reduce manual UI updates
- **Think async**: IO always on background thread
- **Log properly**: Use SLF4J, not System.out

---

## 📞 Need Help?

Check these resources:
- `ARCHITECTURE_REFACTORING.md` - Full architecture details
- `BESTPRACTICES.md` - Original best practices
- `BESTPRACTICES_02.md` - Advanced patterns
- Existing code examples in `ui/viewmodel/`

Happy coding! 🚀

