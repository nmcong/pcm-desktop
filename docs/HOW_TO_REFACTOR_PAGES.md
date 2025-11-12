# How to Refactor Pages to MVVM Pattern

## 📚 Overview

This guide shows you how to refactor existing pages to use the MVVM pattern with ViewModels and property binding.

**Example:** Compare `SettingsPage.java` (old) with `SettingsPageRefactored.java` (new MVVM)

---

## ✅ ViewModels Already Created

The following ViewModels are ready to use:

1. ✅ **AIAssistantViewModel** - Already used in AIAssistantPage
2. ✅ **SettingsViewModel** - Example in SettingsPageRefactored.java
3. ✅ **KnowledgeBaseViewModel** - Ready to use
4. ✅ **DatabaseObjectsViewModel** - Ready to use
5. ✅ **BatchJobsViewModel** - Ready to use

All ViewModels are registered in `Injector` and ready to use!

---

## 🎯 Refactoring Steps

### Step 1: Create/Get ViewModel

#### Option A: Use existing ViewModel
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
}
```

#### Option B: Create new ViewModel
```java
// 1. Create ViewModel class
public class MyViewModel extends BaseViewModel {
    private final StringProperty data = new SimpleStringProperty("");
    
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
registerFactory(MyViewModel.class, MyViewModel::new);
```

---

### Step 2: Move State to ViewModel

#### ❌ Before (state in Page):
```java
public class MyPage extends BasePage {
    private String userName;
    private List<Item> items = new ArrayList<>();
    
    private void handleLoad() {
        userName = service.load();
        items = service.getItems();
        updateUI(); // Manual update
    }
}
```

#### ✅ After (state in ViewModel):
```java
// ViewModel
public class MyViewModel extends BaseViewModel {
    private final StringProperty userName = new SimpleStringProperty("");
    private final ObservableList<Item> items = FXCollections.observableArrayList();
    
    public void loadData() {
        setBusy(true);
        Asyncs.runAsync(
            () -> service.load(),
            result -> {
                userName.set(result.name);
                items.setAll(result.items);
                setBusy(false);
            }
        );
    }
}

// Page (just UI)
public class MyPage extends BasePage {
    private final MyViewModel viewModel;
    
    @Override
    protected VBox createMainContent() {
        Label nameLabel = new Label();
        nameLabel.textProperty().bind(viewModel.userNameProperty());
        
        ListView<Item> listView = new ListView<>();
        listView.setItems(viewModel.getItems());
        
        return new VBox(nameLabel, listView);
    }
}
```

---

### Step 3: Use Property Binding

#### One-way binding (read-only):
```java
// Label text from ViewModel
label.textProperty().bind(viewModel.nameProperty());

// Button disabled when busy
button.disableProperty().bind(viewModel.busyProperty());

// List items from ViewModel
listView.setItems(viewModel.getItems());
```

#### Two-way binding (read-write):
```java
// TextField bidirectional with ViewModel
textField.textProperty().bindBidirectional(viewModel.nameProperty());

// CheckBox bidirectional
checkBox.selectedProperty().bindBidirectional(viewModel.enabledProperty());
```

#### Custom converters:
```java
// Using FxBindings helper
FxBindings.bindBidirectionalWithConverter(
    textField.textProperty(),
    viewModel.ageProperty(),
    Integer::parseInt,
    String::valueOf
);
```

---

### Step 4: Replace Manual Updates with Commands

#### ❌ Before (logic in Page):
```java
button.setOnAction(e -> {
    String text = textField.getText();
    if (text.isEmpty()) {
        showError("Empty!");
        return;
    }
    
    try {
        service.save(text);
        showSuccess("Saved!");
    } catch (Exception ex) {
        showError(ex.getMessage());
    }
});
```

#### ✅ After (command in ViewModel):
```java
// ViewModel
public void save() {
    if (name.get().isEmpty()) {
        setError("Name is required");
        return;
    }
    
    setBusy(true);
    clearError();
    
    Asyncs.runAsync(
        () -> service.save(name.get()),
        result -> {
            setBusy(false);
            // Show success (or use callback)
        },
        error -> {
            setBusy(false);
            setError("Save failed: " + error.getMessage());
        }
    );
}

// Page
button.setOnAction(e -> viewModel.save());
button.disableProperty().bind(viewModel.busyProperty());
```

---

### Step 5: Handle Lifecycle

```java
@Override
public void onPageActivated() {
    super.onPageActivated();
    viewModel.onActivate(); // Load data, subscribe events
}

@Override
public void onPageDeactivated() {
    super.onPageDeactivated();
    viewModel.onDeactivate(); // Cleanup, unsubscribe
}
```

---

## 📖 Complete Example

### ViewModel
```java
public class SettingsViewModel extends BaseViewModel {
    private final BooleanProperty darkTheme = new SimpleBooleanProperty(false);
    private final StringProperty language = new SimpleStringProperty("en");
    
    public void saveSettings() {
        setBusy(true);
        clearError();
        
        try {
            ThemeManager.getInstance().setTheme(darkTheme.get());
            I18n.setLocale(language.get());
            setBusy(false);
        } catch (Exception e) {
            setError("Failed to save", e);
            setBusy(false);
        }
    }
    
    // Property accessors
    public BooleanProperty darkThemeProperty() { return darkTheme; }
    public StringProperty languageProperty() { return language; }
}
```

### Page
```java
public class SettingsPage extends BasePage {
    private final SettingsViewModel viewModel;
    
    public SettingsPage() {
        this(Injector.getInstance().get(SettingsViewModel.class));
    }
    
    public SettingsPage(SettingsViewModel viewModel) {
        super("Settings", "Configure preferences", icon);
        this.viewModel = viewModel;
    }
    
    @Override
    protected VBox createMainContent() {
        // Theme toggle
        CheckBox themeCheck = new CheckBox("Dark Theme");
        themeCheck.selectedProperty().bindBidirectional(
            viewModel.darkThemeProperty()
        );
        
        // Language combo
        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll("en", "vi");
        langCombo.valueProperty().bindBidirectional(
            viewModel.languageProperty()
        );
        
        // Save button
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> viewModel.saveSettings());
        saveBtn.disableProperty().bind(viewModel.busyProperty());
        
        return new VBox(themeCheck, langCombo, saveBtn);
    }
}
```

---

## 🎨 Best Practices

### 1. ViewModel Properties
```java
// ✅ GOOD: Use JavaFX properties
private final StringProperty name = new SimpleStringProperty("");
public StringProperty nameProperty() { return name; }

// ❌ BAD: Plain fields
private String name;
public String getName() { return name; }
```

### 2. Binding vs Listeners
```java
// ✅ GOOD: Use binding
label.textProperty().bind(viewModel.nameProperty());

// ❌ BAD: Manual listener
viewModel.nameProperty().addListener((obs, old, newVal) -> {
    label.setText(newVal);
});
```

### 3. Async Operations
```java
// ✅ GOOD: Use Asyncs utility
Asyncs.runAsync(
    () -> database.load(),
    data -> updateUI(data),
    error -> showError(error)
);

// ❌ BAD: Block UI thread
String data = database.load(); // BLOCKS!
updateUI(data);
```

### 4. Error Handling
```java
// ✅ GOOD: Use ViewModel error property
viewModel.save();
errorLabel.textProperty().bind(viewModel.errorMessageProperty());

// ❌ BAD: Show dialog directly
try {
    service.save();
} catch (Exception e) {
    Alert alert = new Alert(...);
    alert.show();
}
```

---

## 📋 Refactoring Checklist

- [ ] Create ViewModel extending BaseViewModel
- [ ] Move all state to ViewModel as Properties
- [ ] Register ViewModel in Injector
- [ ] Inject ViewModel in Page constructor
- [ ] Replace manual setters with property binding
- [ ] Move business logic to ViewModel commands
- [ ] Bind UI controls to ViewModel properties
- [ ] Add lifecycle hooks (onActivate/onDeactivate)
- [ ] Test the refactored page

---

## 🎯 Pages to Refactor

### Already Refactored
- ✅ **AIAssistantPage** - Using AIAssistantViewModel
- ✅ **SettingsPageRefactored** - Example implementation

### Ready to Refactor (ViewModels exist)
- ⏳ **KnowledgeBasePage** - Use KnowledgeBaseViewModel
- ⏳ **DatabaseObjectsPage** - Use DatabaseObjectsViewModel
- ⏳ **BatchJobsPage** - Use BatchJobsViewModel
- ⏳ **SettingsPage** - Use SettingsViewModel (or use SettingsPageRefactored)

### Demo Pages (Can skip)
- ⏭️ CSSTestPage - Test page
- ⏭️ UniversalTextDemoPage - Demo page

---

## 💡 Tips

1. **Start small:** Refactor one page at a time
2. **Test as you go:** Build and test after each refactoring
3. **Use SettingsPageRefactored as reference:** It shows all patterns
4. **Binding first:** Always prefer binding over manual updates
5. **Keep Page thin:** Move all logic to ViewModel

---

## 📚 Related Documentation

- `docs/ARCHITECTURE_REFACTORING.md` - Full architecture guide
- `docs/REFACTORING_QUICK_START.md` - Quick examples
- `src/main/java/com/noteflix/pcm/ui/pages/SettingsPageRefactored.java` - Complete example
- `src/main/java/com/noteflix/pcm/ui/viewmodel/` - All ViewModels

---

## 🎉 Benefits

After refactoring, you'll have:

- ✅ **Testable logic** - Test ViewModels without UI
- ✅ **Automatic UI updates** - Via property binding
- ✅ **Cleaner code** - Separation of concerns
- ✅ **Easier maintenance** - Logic in one place
- ✅ **Better UX** - Proper async handling

Happy refactoring! 🚀

