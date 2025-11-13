# PCM Desktop Architecture Refactoring

## 📐 Overview

This document describes the architectural refactoring of PCM Desktop to follow **JavaFX Best Practices**, **Clean Code**, and **SOLID principles**.

**Version:** 4.0.0  
**Architecture Pattern:** MVVM (Model-View-ViewModel)  
**Date:** November 2025

---

## 🎯 Goals

1. **Clean Architecture**: Clear separation of concerns
2. **Testability**: Easy to test business logic
3. **Maintainability**: Easy to understand and modify
4. **Scalability**: Easy to add new features
5. **Internationalization**: Multi-language support
6. **Performance**: Proper async handling

---

## 🏗️ Architecture Overview

### MVVM Pattern

```
┌─────────────────────────────────────────────┐
│              View (FXML/JavaFX)              │
│  • Pure UI components                        │
│  • Data binding to ViewModel                 │
│  • No business logic                         │
└──────────────┬──────────────────────────────┘
               │ Binding
               ↓
┌─────────────────────────────────────────────┐
│              ViewModel                       │
│  • Observable Properties                     │
│  • Commands (methods)                        │
│  • UI State management                       │
│  • Coordinates with Services                 │
└──────────────┬──────────────────────────────┘
               │ Calls
               ↓
┌─────────────────────────────────────────────┐
│              Service Layer                   │
│  • Business logic                            │
│  • Data access via Repositories              │
│  • No JavaFX dependencies                    │
└──────────────┬──────────────────────────────┘
               │ Uses
               ↓
┌─────────────────────────────────────────────┐
│         Repository/DAO Layer                 │
│  • Database access                           │
│  • External API calls                        │
│  • Pure Java (no JavaFX)                     │
└─────────────────────────────────────────────┘
```

---

## 📦 Package Structure

```
com.noteflix.pcm/
├── core/                      # Core infrastructure
│   ├── di/                    # Dependency Injection
│   │   └── Injector.java
│   ├── navigation/            # Navigation system
│   │   ├── Route.java
│   │   ├── PageNavigator.java
│   │   └── DefaultPageNavigator.java
│   ├── i18n/                  # Internationalization
│   │   └── I18n.java
│   ├── utils/                 # Utilities
│   │   ├── Asyncs.java
│   │   ├── DialogService.java
│   │   └── FxBindings.java
│   ├── theme/
│   │   └── ThemeManager.java
│   ├── constants/
│   │   └── AppConstants.java
│   └── events/
│       └── ThemeChangeListener.java
│
├── domain/                    # Domain layer
│   ├── entity/                # Domain entities
│   ├── repository/            # Repository interfaces
│   └── chat/                  # Chat domain
│       ├── Conversation.java
│       ├── Message.java
│       └── MessageRole.java
│
├── application/               # Application layer
│   └── service/               # Application services
│       └── chat/
│           ├── ConversationService.java
│           └── AIService.java
│
├── infrastructure/            # Infrastructure layer
│   ├── database/              # Database management
│   │   ├── ConnectionManager.java
│   │   └── DatabaseMigrationManager.java
│   ├── dao/                   # Data Access Objects
│   │   ├── ConversationDAO.java
│   │   └── MessageDAO.java
│   ├── repository/            # Repository implementations
│   │   └── chat/
│   │       ├── ConversationRepository.java
│   │       └── ConversationRepositoryImpl.java
│   └── exception/
│       └── DatabaseException.java
│
├── llm/                       # LLM integration
│   ├── api/                   # LLM interfaces
│   ├── client/                # LLM client implementations
│   ├── factory/
│   ├── model/
│   ├── service/
│   └── exception/
│
└── ui/                        # UI layer
    ├── viewmodel/             # ViewModels (NEW!)
    │   ├── BaseViewModel.java
    │   ├── AIAssistantViewModel.java
    │   └── SettingsViewModel.java
    ├── pages/                 # Page views
    │   ├── BasePage.java
    │   └── AIAssistantPage.java
    ├── components/            # Reusable components
    ├── layout/                # Layout components
    ├── MainController.java
    └── MainView.java
```

---

## 🔑 Key Components

### 1. Dependency Injection (DI)

**Location:** `com.noteflix.pcm.core.di.Injector`

Simple DI container that manages dependencies:

```java
// Get singleton instance
Injector injector = Injector.getInstance();

// Register a service
injector.registerSingleton(ThemeManager.class, ThemeManager.getInstance());

// Get a service
ConversationService service = injector.get(ConversationService.class);

// Register ViewModels
injector.registerFactory(AIAssistantViewModel.class, () -> {
    return new AIAssistantViewModel(
        injector.get(ConversationService.class),
        injector.get(AIService.class)
    );
});
```

**Benefits:**
- Loose coupling
- Easy testing (mock dependencies)
- Follows Dependency Inversion Principle

### 2. ViewModels

**Location:** `com.noteflix.pcm.ui.viewmodel/`

ViewModels contain:
- **Observable Properties** for UI state
- **Commands** (methods) for actions
- **No JavaFX UI components**

Example:

```java
public class AIAssistantViewModel extends BaseViewModel {
    // Properties
    private final StringProperty userInput = new SimpleStringProperty("");
    private final BooleanProperty isBusy = new SimpleBooleanProperty(false);
    private final ObservableList<Message> messages = FXCollections.observableArrayList();
    
    // Commands
    public void sendMessage() {
        // Business logic here
    }
    
    // Property accessors
    public StringProperty userInputProperty() { return userInput; }
}
```

### 3. Async Utilities

**Location:** `com.noteflix.pcm.core.utils.Asyncs`

Handles background tasks properly:

```java
// Run async with callbacks
Asyncs.runAsync(
    () -> {
        // Background work (not on FX thread)
        return database.fetchData();
    },
    result -> {
        // Success callback (on FX thread)
        label.setText(result);
    },
    error -> {
        // Error callback (on FX thread)
        DialogService.showError("Error", error);
    }
);
```

**Important:** 
- All IO operations run on background threads
- UI updates only on JavaFX Application Thread

### 4. Dialog Service

**Location:** `com.noteflix.pcm.core.utils.DialogService`

Centralized dialog management:

```java
// Show info
DialogService.showInfo("Success", "Data saved");

// Show error
DialogService.showError("Error", exception);

// Show confirmation
boolean confirmed = DialogService.showConfirm("Delete", "Are you sure?");

// Show input
Optional<String> input = DialogService.showInput("Name", "Enter your name:");
```

### 5. Internationalization (i18n)

**Location:** `com.noteflix.pcm.core.i18n.I18n`

Multi-language support:

```java
// Set locale
I18n.setLocale("vi"); // Vietnamese
I18n.setLocale("en"); // English

// Get message
String title = I18n.get("app.title");

// Format message with parameters
String msg = I18n.format("time.minutes.ago", 5); // "5 minutes ago"
```

**Message Files:**
- `resources/i18n/messages.properties` (English)
- `resources/i18n/messages_vi.properties` (Vietnamese)

### 6. Navigation

**Location:** `com.noteflix.pcm.core.navigation/`

Type-safe navigation with Route enum:

```java
// Define routes
public enum Route {
    AI_ASSISTANT,
    SETTINGS,
    KNOWLEDGE_BASE
}

// Navigate
PageNavigator navigator = injector.getNavigator();
navigator.navigateToPage(AIAssistantPage.class);
```

---

## ✅ SOLID Principles Applied

### Single Responsibility Principle (SRP)
- ✅ Controllers only handle UI wiring
- ✅ ViewModels only manage UI state
- ✅ Services only contain business logic
- ✅ Repositories only handle data access

### Open/Closed Principle (OCP)
- ✅ Services use interfaces (easy to extend)
- ✅ Navigation via Routes (add new routes without changing core)
- ✅ ViewModels extend BaseViewModel

### Liskov Substitution Principle (LSP)
- ✅ All repository implementations follow their interfaces
- ✅ ViewModels can replace BaseViewModel

### Interface Segregation Principle (ISP)
- ✅ Small, focused interfaces (ConversationRepository, AIService)
- ✅ No "god" interfaces

### Dependency Inversion Principle (DIP)
- ✅ Depend on abstractions (interfaces), not concrete classes
- ✅ DI container manages dependencies

---

## 🎨 Best Practices

### 1. Binding-First Approach

**❌ BAD:**
```java
// Manual UI update
button.setOnAction(e -> {
    label.setText(viewModel.getValue());
});
```

**✅ GOOD:**
```java
// Property binding
label.textProperty().bind(viewModel.valueProperty());
```

### 2. Async Operations

**❌ BAD:**
```java
// Blocking UI thread
button.setOnAction(e -> {
    String data = database.loadData(); // BLOCKS UI!
    label.setText(data);
});
```

**✅ GOOD:**
```java
// Background task
button.setOnAction(e -> {
    Asyncs.runAsync(
        () -> database.loadData(),
        data -> label.setText(data)
    );
});
```

### 3. Error Handling

**❌ BAD:**
```java
// System.out and raw exceptions
try {
    service.save();
} catch (Exception e) {
    System.out.println("Error: " + e);
}
```

**✅ GOOD:**
```java
// Proper logging and user feedback
try {
    service.save();
    log.info("Data saved successfully");
} catch (Exception e) {
    log.error("Failed to save data", e);
    DialogService.showError("Save Failed", e);
}
```

### 4. Dependency Injection

**❌ BAD:**
```java
// Direct instantiation
public class MyController {
    private ConversationService service = new ConversationService();
}
```

**✅ GOOD:**
```java
// Constructor injection
public class MyViewModel {
    private final ConversationService service;
    
    public MyViewModel(ConversationService service) {
        this.service = service;
    }
}
```

---

## 🚀 Usage Examples

### Creating a New Page with ViewModel

1. **Create ViewModel:**

```java
public class MyFeatureViewModel extends BaseViewModel {
    private final StringProperty data = new SimpleStringProperty("");
    private final MyService service;
    
    public MyFeatureViewModel(MyService service) {
        this.service = service;
    }
    
    public void loadData() {
        setBusy(true);
        Asyncs.runAsync(
            () -> service.fetchData(),
            result -> {
                data.set(result);
                setBusy(false);
            },
            error -> {
                setError("Failed to load data", error);
                setBusy(false);
            }
        );
    }
    
    public StringProperty dataProperty() { return data; }
}
```

2. **Register in DI:**

```java
// In Injector.registerDefaults()
registerFactory(MyFeatureViewModel.class, () -> {
    return new MyFeatureViewModel(get(MyService.class));
});
```

3. **Create Page:**

```java
public class MyFeaturePage extends BasePage {
    private final MyFeatureViewModel viewModel;
    private Label dataLabel;
    
    public MyFeaturePage() {
        this(Injector.getInstance().get(MyFeatureViewModel.class));
    }
    
    public MyFeaturePage(MyFeatureViewModel viewModel) {
        super("My Feature", "Description", icon);
        this.viewModel = viewModel;
    }
    
    @Override
    protected VBox createMainContent() {
        dataLabel = new Label();
        dataLabel.textProperty().bind(viewModel.dataProperty());
        
        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> viewModel.loadData());
        loadButton.disableProperty().bind(viewModel.busyProperty());
        
        return new VBox(dataLabel, loadButton);
    }
    
    @Override
    public void onPageActivated() {
        super.onPageActivated();
        viewModel.onActivate();
    }
}
```

---

## 🧪 Testing

### Testing ViewModels

ViewModels are easy to test (no JavaFX dependencies):

```java
@Test
public void testSendMessage() {
    // Arrange
    ConversationService mockService = mock(ConversationService.class);
    AIService mockAI = mock(AIService.class);
    AIAssistantViewModel vm = new AIAssistantViewModel(mockService, mockAI);
    
    // Act
    vm.setUserInput("Hello");
    vm.sendMessage(null);
    
    // Assert
    verify(mockService, times(1)).createConversation(...);
}
```

### Testing Services

```java
@Test
public void testConversationService() {
    // Arrange
    ConversationRepository mockRepo = mock(ConversationRepository.class);
    ConversationService service = new ConversationService(mockRepo, ...);
    
    // Act
    Conversation conv = service.createConversation(...);
    
    // Assert
    assertNotNull(conv);
    verify(mockRepo, times(1)).save(any());
}
```

---

## 🔧 Migration Guide

### Existing Code

If you have existing controllers with business logic:

1. **Extract to ViewModel:**
   - Move Observable Properties to ViewModel
   - Move business logic to ViewModel commands
   - Keep only UI wiring in Controller

2. **Use DI:**
   - Inject services instead of `new`
   - Register in Injector

3. **Use Utilities:**
   - Replace manual dialogs with DialogService
   - Replace Thread/ExecutorService with Asyncs
   - Replace string literals with I18n

4. **Add Binding:**
   - Replace manual setText/setDisable with property binding

---

## 📚 References

- **BESTPRACTICES.md** - Original best practices guide
- **BESTPRACTICES_02.md** - Advanced patterns and examples
- JavaFX Documentation: https://openjfx.io/
- SOLID Principles: https://en.wikipedia.org/wiki/SOLID

---

## 🎓 Learning Resources

### Key Concepts to Understand:

1. **MVVM Pattern**: Separation of UI from logic
2. **Observable Properties**: JavaFX data binding
3. **Dependency Injection**: Loose coupling
4. **Async Programming**: Non-blocking UI
5. **SOLID Principles**: Clean code design

### Recommended Reading:

- "Clean Architecture" by Robert C. Martin
- "Effective Java" by Joshua Bloch
- JavaFX documentation on Properties and Binding

---

## 📝 Summary

This refactoring provides:

✅ **Clean Architecture** - MVVM pattern  
✅ **Testable Code** - ViewModels with no UI dependencies  
✅ **Proper DI** - Injector for dependency management  
✅ **Async Support** - Background tasks with Asyncs  
✅ **i18n Support** - Multi-language ready  
✅ **SOLID Principles** - Maintainable and scalable  
✅ **Best Practices** - Following JavaFX guidelines  

The codebase is now more maintainable, testable, and follows industry best practices!

