# UI Architecture Documentation

> **Package**: `com.noteflix.pcm.ui`  
> **Architecture**: MVVM (Model-View-ViewModel)  
> **Version**: 2.0.0

## Mục Lục

1. [Overview](#overview)
2. [MVVM Architecture](#mvvm-architecture)
3. [Component Architecture](#component-architecture)
4. [Navigation Architecture](#navigation-architecture)
5. [State Management](#state-management)
6. [Event System](#event-system)
7. [Dependency Injection](#dependency-injection)
8. [Design Patterns](#design-patterns)
9. [Performance Optimization](#performance-optimization)

---

## Overview

### Architecture Goals

1. **Separation of Concerns**: UI, logic, và data tách biệt rõ ràng
2. **Testability**: Dễ dàng unit test các components
3. **Maintainability**: Code dễ đọc, dễ hiểu, dễ maintain
4. **Scalability**: Dễ dàng thêm features mới
5. **Reusability**: Components có thể tái sử dụng

### Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│  ┌───────────┐  ┌───────────┐  ┌──────────┐  ┌──────────┐  │
│  │   Views   │  │Components │  │ Layouts  │  │  Styles  │  │
│  └───────────┘  └───────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↕ Binding
┌─────────────────────────────────────────────────────────────┐
│                    ViewModel Layer                           │
│  ┌───────────┐  ┌───────────┐  ┌──────────┐  ┌──────────┐  │
│  │ViewModels │  │Properties │  │ Commands │  │  Events  │  │
│  └───────────┘  └───────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↕ Service Calls
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  ┌───────────┐  ┌───────────┐  ┌──────────┐  ┌──────────┐  │
│  │ Services  │  │   DTOs    │  │ Use Cases│  │ Mappers  │  │
│  └───────────┘  └───────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↕ Data Access
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                             │
│  ┌───────────┐  ┌───────────┐  ┌──────────┐  ┌──────────┐  │
│  │ Entities  │  │Repositories│  │ Value Obj│  │  Events  │  │
│  └───────────┘  └───────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## MVVM Architecture

### Overview

MVVM (Model-View-ViewModel) tách biệt UI logic khỏi business logic, giúp code dễ test và maintain hơn.

### Components

```
┌──────────────────────────────────────────────────────────┐
│                          View                             │
│  • JavaFX UI Components                                   │
│  • Layout structure                                       │
│  • User input handling                                    │
│  • Binding to ViewModel properties                        │
│                                                            │
│  Responsibilities:                                         │
│  - Display data                                           │
│  - Capture user input                                     │
│  - Delegate to ViewModel                                  │
│  - NO business logic                                      │
└──────────────────────────────────────────────────────────┘
                        ↕ Data Binding
┌──────────────────────────────────────────────────────────┐
│                       ViewModel                           │
│  • Observable Properties (state)                          │
│  • Commands (actions)                                     │
│  • Service coordination                                   │
│  • Data transformation                                    │
│                                                            │
│  Responsibilities:                                         │
│  - Maintain UI state                                      │
│  - Execute business operations                            │
│  - Transform data for display                             │
│  - Handle async operations                                │
│  - NO JavaFX components                                   │
└──────────────────────────────────────────────────────────┘
                        ↕ Service Calls
┌──────────────────────────────────────────────────────────┐
│                         Model                             │
│  • Domain entities                                        │
│  • Business services                                      │
│  • Data access                                            │
│                                                            │
│  Responsibilities:                                         │
│  - Business logic                                         │
│  - Data persistence                                       │
│  - Domain rules                                           │
│  - NO UI knowledge                                        │
└──────────────────────────────────────────────────────────┘
```

### Example Implementation

#### View (Page)

```java
public class MyPage extends BaseView {
    private final MyPageViewModel viewModel;
    private TableView<Item> tableView;
    private TextField searchField;
    
    public MyPage(MyPageViewModel viewModel) {
        super("My Page", "Description", icon);
        this.viewModel = viewModel;
    }
    
    @Override
    protected Node createMainContent() {
        VBox content = new VBox(20);
        
        // Create UI
        searchField = new TextField();
        tableView = new TableView<>();
        
        // Setup bindings
        setupBindings();
        
        content.getChildren().addAll(searchField, tableView);
        return content;
    }
    
    private void setupBindings() {
        // Bind search field to ViewModel
        searchField.textProperty().bindBidirectional(
            viewModel.searchQueryProperty()
        );
        
        // Bind table items to ViewModel
        tableView.setItems(viewModel.getItems());
        
        // Bind loading state
        tableView.disableProperty().bind(viewModel.busyProperty());
    }
    
    @Override
    public void onPageActivated() {
        super.onPageActivated();
        viewModel.loadData();
    }
}
```

#### ViewModel

```java
public class MyPageViewModel extends BaseViewModel {
    // State - Observable Properties
    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final ObservableList<Item> items = FXCollections.observableArrayList();
    
    // Dependencies
    private final MyService service;
    
    public MyPageViewModel(MyService service) {
        this.service = service;
        
        // Setup listeners
        searchQuery.addListener((obs, old, newVal) -> handleSearchChanged(newVal));
    }
    
    // Commands
    public void loadData() {
        setBusy(true);
        clearError();
        
        runAsync(
            () -> service.fetchItems(),
            data -> items.setAll(data),
            error -> setError("Failed to load data", error)
        ).whenComplete((result, error) -> setBusy(false));
    }
    
    public void deleteItem(Item item) {
        runAsync(
            () -> service.deleteItem(item.getId()),
            result -> {
                items.remove(item);
                showSuccess("Item deleted");
            },
            error -> setError("Failed to delete", error)
        );
    }
    
    private void handleSearchChanged(String query) {
        // Filter items based on search query
        if (query == null || query.isEmpty()) {
            loadData();
        } else {
            filterItems(query);
        }
    }
    
    // Property accessors
    public StringProperty searchQueryProperty() { return searchQuery; }
    public ObservableList<Item> getItems() { return items; }
}
```

### Rules và Best Practices

#### View Rules

✅ **DO:**

- Bind to ViewModel properties
- Call ViewModel commands
- Handle layout và styling
- Display data from ViewModel

❌ **DON'T:**

- Contains business logic
- Direct service calls
- Data manipulation
- Complex calculations

#### ViewModel Rules

✅ **DO:**

- Expose Observable Properties
- Coordinate services
- Handle async operations
- Transform data for display
- Maintain UI state

❌ **DON'T:**

- Reference JavaFX UI components
- Know about View structure
- Direct database access
- File I/O operations

---

## Component Architecture

### Component Hierarchy

```
Component (Abstract)
    │
    ├── BaseView (Pages)
    │   ├── AIAssistantPage
    │   ├── KnowledgeBasePage
    │   └── SettingsPage
    │
    ├── BaseComponent (Reusable)
    │   ├── Card
    │   ├── Button
    │   ├── Form
    │   └── List
    │
    └── BaseWidget (Specialized)
        ├── SearchBox
        ├── LoadingIndicator
        └── StatusBadge
```

### Component Lifecycle

```
┌────────────┐
│   Create   │ ← Constructor called
└─────┬──────┘
      │
      ▼
┌────────────┐
│ Initialize │ ← initialize() called
└─────┬──────┘
      │
      ▼
┌────────────┐
│  Activate  │ ← onActivate() called
└─────┬──────┘
      │
      ▼
┌────────────┐
│   Active   │ ← Component is visible/active
└─────┬──────┘
      │
      ▼
┌────────────┐
│ Deactivate │ ← onDeactivate() called
└─────┬──────┘
      │
      ▼
┌────────────┐
│  Cleanup   │ ← cleanup() called
└────────────┘
```

### Component Communication

#### 1. Parent → Child (Props)

```java
// Parent passes data to child
Card card = new Card();
card.setTitle("My Card");
card.setContent(content);
```

#### 2. Child → Parent (Events)

```java
// Child notifies parent via callbacks
SearchBox searchBox = new SearchBox();
searchBox.setOnSearch(query -> handleSearch(query));
```

#### 3. Sibling ↔ Sibling (Event Bus)

```java
// Siblings communicate via event bus
EventBus.publish(new ItemSelectedEvent(item));

// Other component subscribes
EventBus.subscribe(ItemSelectedEvent.class, event -> {
    handleItemSelected(event.getItem());
});
```

---

## Navigation Architecture

### Navigation System

```
┌─────────────────────────────────────────────────────────┐
│                   PageNavigator                          │
│  • navigateToPage(Class<? extends BasePage>)            │
│  • navigateToPage(BasePage instance)                    │
│  • goBack()                                              │
│  • getCurrentPage()                                      │
└─────────────────────────────────────────────────────────┘
                        ↕
┌─────────────────────────────────────────────────────────┐
│                  Page Cache                              │
│  • Stores page instances                                │
│  • Lazy initialization                                  │
│  • Memory management                                     │
└─────────────────────────────────────────────────────────┘
                        ↕
┌─────────────────────────────────────────────────────────┐
│               Navigation History                         │
│  • Stack of previous pages                              │
│  • Back navigation support                              │
│  • History limits                                        │
└─────────────────────────────────────────────────────────┘
```

### Navigation Flow

```
User Action (Click menu)
    ↓
Sidebar captures event
    ↓
Call PageNavigator.navigateToPage(MyPage.class)
    ↓
PageNavigator checks cache
    ↓
If not in cache: Create new instance
    ↓
Deactivate current page
    ↓
Push current page to history
    ↓
Set new page as current
    ↓
Display new page in content area
    ↓
Activate new page
```

### Implementation

```java
public class DefaultPageNavigator implements PageNavigator {
    private final StackPane contentContainer;
    private final Map<Class<? extends BasePage>, BasePage> pageCache;
    private final Stack<BasePage> navigationHistory;
    private BasePage currentPage;
    
    @Override
    public void navigateToPage(Class<? extends BasePage> pageClass) {
        // Get or create page
        BasePage page = getOrCreatePage(pageClass);
        
        // Lifecycle management
        if (currentPage != null) {
            currentPage.onPageDeactivated();
            navigationHistory.push(currentPage);
        }
        
        // Switch to new page
        currentPage = page;
        contentContainer.getChildren().setAll(page);
        page.onPageActivated();
    }
    
    private BasePage getOrCreatePage(Class<? extends BasePage> pageClass) {
        return pageCache.computeIfAbsent(pageClass, clazz -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create page", e);
            }
        });
    }
}
```

---

## State Management

### State Types

#### 1. Local State (Component Level)

```java
public class MyComponent extends VBox {
    // Local state - only this component knows about it
    private boolean isExpanded = false;
    
    public void toggle() {
        isExpanded = !isExpanded;
        updateUI();
    }
}
```

#### 2. Page State (Page Level)

```java
public class MyPageViewModel extends BaseViewModel {
    // Page state - managed by ViewModel
    private final ObservableList<Item> items = FXCollections.observableArrayList();
    private final StringProperty filter = new SimpleStringProperty("");
    
    // Shared across page components
}
```

#### 3. Global State (Application Level)

```java
public class AppState {
    // Singleton pattern for global state
    private static AppState instance;
    
    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);
    private final ObjectProperty<User> currentUser = new SimpleObjectProperty<>();
    
    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }
}
```

### State Flow

```
User Input
    ↓
View captures event
    ↓
View calls ViewModel command
    ↓
ViewModel updates properties
    ↓
Properties notify listeners (via JavaFX binding)
    ↓
View auto-updates (via binding)
```

---

## Event System

### Event Types

#### 1. UI Events (JavaFX native)

```java
button.setOnAction(event -> handleClick());
textField.textProperty().addListener((obs, old, newVal) -> handleTextChange(newVal));
```

#### 2. Custom Events (Event Bus)

```java
// Define event
public class ItemSelectedEvent {
    private final Item item;
    // ...
}

// Publish event
EventBus.publish(new ItemSelectedEvent(selectedItem));

// Subscribe to event
EventBus.subscribe(ItemSelectedEvent.class, event -> {
    handleItemSelected(event.getItem());
});
```

#### 3. ViewModel Events (Observable Properties)

```java
// ViewModel
public class MyViewModel extends BaseViewModel {
    private final ObjectProperty<Item> selectedItem = new SimpleObjectProperty<>();
    
    public ObjectProperty<Item> selectedItemProperty() {
        return selectedItem;
    }
}

// View
viewModel.selectedItemProperty().addListener((obs, old, newItem) -> {
    updateDetails(newItem);
});
```

---

## Dependency Injection

### Manual DI (Current)

```java
// Services created manually
ConversationService conversationService = new ConversationService();
AIService aiService = new AIService();

// Injected via constructor
AIAssistantViewModel viewModel = new AIAssistantViewModel(
    conversationService,
    aiService
);

AIAssistantPage page = new AIAssistantPage(viewModel);
```

### DI Container (Planned)

```java
// Register services
DIContainer container = new DIContainer();
container.registerSingleton(ConversationService.class, new ConversationService());
container.registerSingleton(AIService.class, new AIService());

// Register ViewModels (factory)
container.registerFactory(AIAssistantViewModel.class, () ->
    new AIAssistantViewModel(
        container.resolve(ConversationService.class),
        container.resolve(AIService.class)
    )
);

// Register Pages (factory)
container.registerFactory(AIAssistantPage.class, () ->
    new AIAssistantPage(container.resolve(AIAssistantViewModel.class))
);

// Resolve dependencies
AIAssistantPage page = container.resolve(AIAssistantPage.class);
```

---

## Design Patterns

### 1. Template Method Pattern

Used in: `BasePage`, `BaseViewModel`

```java
public abstract class BasePage extends VBox {
    protected BasePage(String title, String description, FontIcon icon) {
        initializeLayout();
        buildContent();
    }
    
    // Template method
    private void buildContent() {
        var header = createPageHeader();
        if (header != null) {
            getChildren().add(header);
        }
        
        // Hook - subclass implements
        getChildren().add(createMainContent());
        
        var footer = createPageFooter();
        if (footer != null) {
            getChildren().add(footer);
        }
    }
    
    // Hook methods
    protected abstract Node createMainContent();
    protected Node createPageFooter() { return null; }
}
```

### 2. Factory Pattern

Used in: `UIFactory`, `ViewModelFactory`

```java
public class UIFactory {
    public static Button createPrimaryButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add(Styles.ACCENT);
        button.setOnAction(e -> action.run());
        return button;
    }
    
    public static Card createStatCard(String label, String value) {
        return new StatCard(label, value);
    }
}
```

### 3. Builder Pattern

Used in: `FormBuilder`, `DialogBuilder`, `LayoutBuilder`

```java
public class FormBuilder {
    private List<FormField> fields = new ArrayList<>();
    
    public FormBuilder addField(String label, Node control) {
        fields.add(new FormField(label, control));
        return this;
    }
    
    public Form build() {
        Form form = new Form();
        fields.forEach(form::addField);
        return form;
    }
}

// Usage
Form form = new FormBuilder()
    .addField("Name", nameField)
    .addField("Email", emailField)
    .build();
```

### 4. Observer Pattern

Used in: JavaFX Properties, Event System

```java
// Observable
StringProperty searchQuery = new SimpleStringProperty("");

// Observer
searchQuery.addListener((observable, oldValue, newValue) -> {
    handleSearchChanged(newValue);
});
```

### 5. Strategy Pattern

Used in: Text renderers, Validators

```java
public interface TextRenderer {
    Node render(String content);
}

public class MarkdownRenderer implements TextRenderer {
    @Override
    public Node render(String content) {
        // Render markdown
    }
}

public class PlainTextRenderer implements TextRenderer {
    @Override
    public Node render(String content) {
        // Render plain text
    }
}

// Usage
TextRenderer renderer = isMarkdown 
    ? new MarkdownRenderer() 
    : new PlainTextRenderer();
Node rendered = renderer.render(content);
```

---

## Performance Optimization

### 1. Lazy Loading

```java
public class PageNavigator {
    private Map<Class<? extends BasePage>, BasePage> pageCache = new HashMap<>();
    
    private BasePage getOrCreatePage(Class<? extends BasePage> pageClass) {
        return pageCache.computeIfAbsent(pageClass, clazz -> {
            // Create only when needed
            return createPageInstance(clazz);
        });
    }
}
```

### 2. Virtual Scrolling

```java
// Use for large lists
VirtualFlow<Item> virtualFlow = new VirtualFlow<>();
virtualFlow.setItems(largeItemList);
```

### 3. Async Operations

```java
public void loadData() {
    setBusy(true);
    
    CompletableFuture.supplyAsync(() -> {
        // Heavy operation on background thread
        return service.fetchData();
    }).thenAcceptAsync(data -> {
        // Update UI on JavaFX thread
        Platform.runLater(() -> {
            items.setAll(data);
            setBusy(false);
        });
    });
}
```

### 4. Memory Management

```java
@Override
public void cleanup() {
    // Unregister listeners to prevent memory leaks
    if (themeManager != null) {
        themeManager.removeThemeChangeListener(this);
    }
    
    // Clear collections
    items.clear();
    
    // Nullify references
    viewModel = null;
}
```

### 5. CSS Optimization

```css
/* Use CSS for styling instead of Java code */
.card {
    -fx-padding: 16px;
    -fx-background-color: -color-bg-default;
    -fx-background-radius: 8px;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);
}
```

---

## Summary

UI Architecture của PCM Desktop được thiết kế với các nguyên tắc:

1. **MVVM Pattern**: Tách biệt UI và business logic
2. **Component-Based**: Tái sử dụng và modular
3. **Event-Driven**: Loose coupling giữa components
4. **Dependency Injection**: Flexible và testable
5. **Performance**: Optimized cho responsiveness

Architecture này đảm bảo code dễ maintain, test, và scale khi ứng dụng phát triển.

---

**Last Updated**: 2025-11-15  
**Version**: 2.0.0

