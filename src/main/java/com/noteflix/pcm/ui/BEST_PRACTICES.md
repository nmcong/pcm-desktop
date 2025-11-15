# UI Development Best Practices

> **Package**: `com.noteflix.pcm.ui`  
> **Version**: 2.0.0

## Mục Lục

- [General Guidelines](#general-guidelines)
- [MVVM Best Practices](#mvvm-best-practices)
- [Component Development](#component-development)
- [Styling Guidelines](#styling-guidelines)
- [Performance](#performance)
- [Accessibility](#accessibility)
- [Testing](#testing)
- [Code Organization](#code-organization)
- [Common Mistakes](#common-mistakes)

---

## General Guidelines

### 1. Follow SOLID Principles

#### Single Responsibility Principle

✅ **DO:**
```java
// Each class has one clear purpose
public class UserListPage extends BaseView {
    // Only handles UI layout and presentation
}

public class UserListViewModel extends BaseViewModel {
    // Only handles UI state and coordination
}

public class UserService {
    // Only handles user business logic
}
```

❌ **DON'T:**
```java
// Page doing everything
public class UserPage extends BaseView {
    private void loadUsers() {
        // Direct database access
        List<User> users = database.query("SELECT * FROM users");
        
        // Business logic
        users = users.stream()
            .filter(u -> u.isActive())
            .collect(Collectors.toList());
            
        // UI update
        userTable.setItems(FXCollections.observableList(users));
    }
}
```

#### Open/Closed Principle

✅ **DO:**
```java
// Open for extension
public abstract class BasePage extends VBox {
    protected abstract Node createMainContent();
}

// Extend without modifying base
public class MyPage extends BasePage {
    @Override
    protected Node createMainContent() {
        return new VBox(...);
    }
}
```

❌ **DON'T:**
```java
// Modifying base class for each new feature
public class BasePage extends VBox {
    private boolean showAIAssistant;
    private boolean showKnowledgeBase;
    
    // Adding more flags for each feature
}
```

### 2. Meaningful Names

✅ **DO:**
```java
Button submitButton = new Button("Submit");
TextField userEmailField = new TextField();
VBox chatMessagesContainer = new VBox();

public void loadUserConversations() { ... }
public void handleSendMessage() { ... }
```

❌ **DON'T:**
```java
Button b1 = new Button("Submit");
TextField tf = new TextField();
VBox box = new VBox();

public void doStuff() { ... }
public void onClick() { ... }
```

### 3. Keep Methods Small

✅ **DO:**
```java
@Override
protected Node createMainContent() {
    VBox content = new VBox(20);
    content.getChildren().addAll(
        createToolbar(),
        createDataView(),
        createFooter()
    );
    return content;
}

private Node createToolbar() {
    return new HBox(12,
        createSearchBox(),
        createFilterButton(),
        createAddButton()
    );
}
```

❌ **DON'T:**
```java
@Override
protected Node createMainContent() {
    VBox content = new VBox(20);
    
    // 50+ lines creating toolbar
    HBox toolbar = new HBox(12);
    TextField search = new TextField();
    search.setPromptText("Search...");
    search.getStyleClass().add("search-field");
    // ... more setup ...
    
    // 100+ lines creating data view
    TableView table = new TableView();
    // ... lots of configuration ...
    
    // 50+ lines creating footer
    // ...
    
    return content;
}
```

---

## MVVM Best Practices

### 1. View Rules

#### ✅ DO: Keep Views Dumb

```java
public class MyPage extends BaseView {
    private final MyPageViewModel viewModel;
    
    public MyPage(MyPageViewModel viewModel) {
        super("My Page", "Description", icon);
        this.viewModel = viewModel;
    }
    
    @Override
    protected Node createMainContent() {
        TableView<Item> table = new TableView<>();
        
        // Just bind to ViewModel
        table.setItems(viewModel.getItems());
        table.disableProperty().bind(viewModel.busyProperty());
        
        // Delegate to ViewModel
        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> viewModel.loadData());
        
        return new VBox(loadButton, table);
    }
}
```

#### ❌ DON'T: Business Logic in View

```java
public class MyPage extends BaseView {
    @Override
    protected Node createMainContent() {
        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> {
            // Business logic in View ❌
            List<Item> items = service.fetchItems();
            items = items.stream()
                .filter(i -> i.isActive())
                .sorted(Comparator.comparing(Item::getName))
                .collect(Collectors.toList());
            table.setItems(FXCollections.observableList(items));
        });
        
        return new VBox(loadButton, table);
    }
}
```

### 2. ViewModel Rules

#### ✅ DO: ViewModel Exposes State and Commands

```java
public class MyPageViewModel extends BaseViewModel {
    // State - Observable Properties
    private final ObservableList<Item> items = FXCollections.observableArrayList();
    private final StringProperty filter = new SimpleStringProperty("");
    
    // Dependencies
    private final MyService service;
    
    public MyPageViewModel(MyService service) {
        this.service = service;
    }
    
    // Commands
    public void loadData() {
        setBusy(true);
        runAsync(
            () -> service.fetchItems(),
            data -> items.setAll(data),
            error -> setError("Failed to load", error)
        ).whenComplete((r, e) -> setBusy(false));
    }
    
    public void deleteItem(Item item) {
        runAsync(
            () -> service.delete(item.getId()),
            result -> items.remove(item),
            error -> setError("Failed to delete", error)
        );
    }
    
    // Property accessors
    public ObservableList<Item> getItems() { return items; }
    public StringProperty filterProperty() { return filter; }
}
```

#### ❌ DON'T: ViewModel Knows About View

```java
public class MyPageViewModel extends BaseViewModel {
    private TableView<Item> table; // ❌ No JavaFX components!
    private Button loadButton;     // ❌
    
    public void setTable(TableView<Item> table) {
        this.table = table;
        this.table.setItems(items); // ❌
    }
    
    public void updateUI() {
        // ❌ ViewModel shouldn't manipulate UI
        loadButton.setDisable(true);
        table.refresh();
    }
}
```

### 3. Data Binding

#### ✅ DO: Use Bidirectional Binding When Appropriate

```java
// View
TextField searchField = new TextField();
searchField.textProperty().bindBidirectional(viewModel.searchQueryProperty());

// ViewModel
private final StringProperty searchQuery = new SimpleStringProperty("");

searchQuery.addListener((obs, old, newVal) -> {
    // React to changes
    filterItems(newVal);
});
```

#### ✅ DO: Use Unidirectional Binding for Display-Only

```java
// View
Label statusLabel = new Label();
statusLabel.textProperty().bind(viewModel.statusMessageProperty());

ProgressBar progressBar = new ProgressBar();
progressBar.progressProperty().bind(viewModel.progressProperty());
```

---

## Component Development

### 1. Builder Pattern

✅ **DO: Use Fluent API**

```java
public class Card extends VBox {
    private String title;
    private Node content;
    
    public Card() {
        getStyleClass().add("card");
    }
    
    public Card withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public Card withContent(Node content) {
        this.content = content;
        return this;
    }
    
    public Card build() {
        if (title != null) {
            getChildren().add(new Label(title));
        }
        if (content != null) {
            getChildren().add(content);
        }
        return this;
    }
}

// Usage
Card card = new Card()
    .withTitle("My Card")
    .withContent(contentNode)
    .build();
```

### 2. Component Composition

✅ **DO: Compose Small Components**

```java
public class UserCard extends Card {
    public UserCard(User user) {
        super();
        
        VBox content = new VBox(8,
            new AvatarListItem(user.getInitials(), user.getName()),
            new Label(user.getEmail()),
            createActionButtons(user)
        );
        
        withContent(content);
    }
    
    private Node createActionButtons(User user) {
        return new HBox(8,
            new PrimaryButton("Edit", () -> editUser(user)),
            new SecondaryButton("Delete", () -> deleteUser(user))
        );
    }
}
```

❌ **DON'T: Create Monolithic Components**

```java
public class UserCard extends VBox {
    public UserCard(User user) {
        // 200+ lines of inline UI creation
        Label avatar = new Label(user.getInitials());
        avatar.getStyleClass().add("avatar");
        // ...
        Label name = new Label(user.getName());
        // ...
        // ... lots more ...
    }
}
```

### 3. Separation of Style and Structure

✅ **DO: Use CSS for Styling**

```java
// Java - Structure only
public class Card extends VBox {
    public Card() {
        getStyleClass().add("card");
    }
}

// CSS - Styling
.card {
    -fx-padding: 16px;
    -fx-background-color: -color-bg-default;
    -fx-background-radius: 8px;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);
}
```

❌ **DON'T: Inline Styling in Java**

```java
public class Card extends VBox {
    public Card() {
        setPadding(new Insets(16));
        setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
    }
}
```

---

## Styling Guidelines

### 1. Use CSS Classes

✅ **DO:**

```java
button.getStyleClass().addAll(Styles.ACCENT, Styles.LARGE);
card.getStyleClass().add("stat-card");
```

❌ **DON'T:**

```java
button.setStyle("-fx-background-color: blue; -fx-font-size: 14px;");
```

### 2. Use AtlantaFX Styles

```java
// Button styles
button.getStyleClass().add(Styles.ACCENT);      // Primary color
button.getStyleClass().add(Styles.SUCCESS);     // Green
button.getStyleClass().add(Styles.DANGER);      // Red
button.getStyleClass().add(Styles.FLAT);        // No background

// Text styles
label.getStyleClass().add(Styles.TITLE_1);      // Large title
label.getStyleClass().add(Styles.TITLE_4);      // Small title
label.getStyleClass().add(Styles.TEXT_MUTED);   // Muted text
label.getStyleClass().add(Styles.TEXT_BOLD);    // Bold text

// Component styles
card.getStyleClass().add(Styles.ELEVATED_1);    // With shadow
```

### 3. Use Style Constants

✅ **DO:**

```java
public class StyleConstants {
    public static final String CARD = "card";
    public static final String STAT_CARD = "stat-card";
    public static final String CHAT_SIDEBAR = "chat-sidebar";
}

// Usage
card.getStyleClass().add(StyleConstants.CARD);
```

### 4. Theme-Aware Styling

```css
/* Use CSS variables for colors */
.card {
    -fx-background-color: -color-bg-default;
    -fx-text-fill: -color-fg-default;
}

.card:hover {
    -fx-background-color: -color-bg-subtle;
}
```

---

## Performance

### 1. Lazy Loading

✅ **DO:**

```java
public class PageNavigator {
    private Map<Class<?>, BasePage> pageCache = new HashMap<>();
    
    private BasePage getOrCreatePage(Class<?> pageClass) {
        return pageCache.computeIfAbsent(pageClass, this::createPage);
    }
}
```

### 2. Virtual Scrolling

✅ **DO: For large lists**

```java
// Use VirtualFlow or ListView for large datasets
ListView<Item> listView = new ListView<>();
listView.setItems(largeItemList);
```

❌ **DON'T: Render all items**

```java
// Creating thousands of nodes
VBox container = new VBox();
for (Item item : largeList) { // 10,000+ items
    container.getChildren().add(new ItemView(item)); // Memory explosion!
}
```

### 3. Async Operations

✅ **DO:**

```java
public void loadData() {
    setBusy(true);
    
    CompletableFuture.supplyAsync(() -> {
        // Heavy operation on background thread
        return service.fetchLargeDataset();
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

✅ **DO: Cleanup Resources**

```java
@Override
public void cleanup() {
    // Unregister listeners
    if (themeManager != null) {
        themeManager.removeThemeChangeListener(this);
    }
    
    // Clear collections
    items.clear();
    
    // Cancel ongoing tasks
    if (loadTask != null) {
        loadTask.cancel(true);
    }
}
```

---

## Accessibility

### 1. Keyboard Navigation

✅ **DO:**

```java
// Make all interactive elements keyboard accessible
button.setFocusTraversable(true);

// Add keyboard shortcuts
button.setOnKeyPressed(event -> {
    if (event.getCode() == KeyCode.ENTER) {
        handleAction();
    }
});
```

### 2. Screen Reader Support

✅ **DO:**

```java
// Add accessible text
button.setAccessibleText("Submit form");
image.setAccessibleText("User profile picture");

// Set accessible role
node.setAccessibleRole(AccessibleRole.BUTTON);
```

### 3. Focus Indicators

✅ **DO: Ensure visible focus**

```css
.button:focused {
    -fx-border-color: -color-accent-emphasis;
    -fx-border-width: 2px;
}
```

---

## Testing

### 1. Unit Test ViewModels

```java
@Test
void testLoadData() {
    // Given
    MyService mockService = mock(MyService.class);
    when(mockService.fetchItems()).thenReturn(testData);
    MyPageViewModel vm = new MyPageViewModel(mockService);
    
    // When
    vm.loadData();
    
    // Then
    await().until(() -> !vm.isBusy());
    assertEquals(testData.size(), vm.getItems().size());
    verify(mockService).fetchItems();
}
```

### 2. Integration Test Pages

```java
@Test
void testPageCreation() {
    // Given
    MyPageViewModel viewModel = new MyPageViewModel(service);
    
    // When
    MyPage page = new MyPage(viewModel);
    
    // Then
    assertNotNull(page);
    assertEquals("My Page", page.getPageTitle());
}
```

### 3. UI Test với TestFX

```java
@Test
void testButtonClick(FxRobot robot) {
    // When
    robot.clickOn("#submit-button");
    
    // Then
    robot.waitForFxEvents();
    verifyThat("#success-message", isVisible());
}
```

---

## Code Organization

### 1. File Structure

```java
public class MyPage extends BaseView {
    // ===== CONSTANTS =====
    private static final int SIDEBAR_WIDTH = 280;
    
    // ===== DEPENDENCIES =====
    private final MyPageViewModel viewModel;
    private final MyService service;
    
    // ===== UI COMPONENTS =====
    private TableView<Item> table;
    private TextField searchField;
    
    // ===== CONSTRUCTOR =====
    public MyPage(MyPageViewModel viewModel) {
        super("My Page", "Description", icon);
        this.viewModel = viewModel;
    }
    
    // ===== INITIALIZATION =====
    @Override
    protected Node createMainContent() {
        return createLayout();
    }
    
    // ===== UI CREATION =====
    private Node createLayout() { ... }
    private Node createToolbar() { ... }
    private Node createDataView() { ... }
    
    // ===== EVENT HANDLERS =====
    private void handleSearch() { ... }
    private void handleAdd() { ... }
    
    // ===== UTILITY METHODS =====
    private void setupBindings() { ... }
    private void updateUI() { ... }
    
    // ===== LIFECYCLE =====
    @Override
    public void onPageActivated() { ... }
    
    @Override
    public void cleanup() { ... }
}
```

### 2. Method Order

1. Constants
2. Fields
3. Constructor
4. Public methods
5. Protected methods
6. Private methods
7. Inner classes

### 3. Imports Organization

```java
// Java standard library
import java.util.*;
import java.time.*;

// JavaFX
import javafx.scene.control.*;
import javafx.scene.layout.*;

// Third-party
import atlantafx.base.theme.Styles;
import org.kordamp.ikonli.octicons.Octicons;

// Application
import com.noteflix.pcm.ui.base.*;
import com.noteflix.pcm.ui.components.*;
```

---

## Common Mistakes

### 1. ❌ Accessing UI from Background Thread

```java
// WRONG
CompletableFuture.runAsync(() -> {
    List<Item> data = service.fetchData();
    table.setItems(FXCollections.observableList(data)); // ❌ Not on FX thread!
});

// CORRECT
CompletableFuture.supplyAsync(() -> {
    return service.fetchData();
}).thenAccept(data -> {
    Platform.runLater(() -> {
        table.setItems(FXCollections.observableList(data)); // ✅
    });
});
```

### 2. ❌ Memory Leaks from Listeners

```java
// WRONG - Listener never removed
public MyComponent() {
    ThemeManager.getInstance().addThemeChangeListener(this);
}
// ❌ Component destroyed but listener still registered!

// CORRECT
public void cleanup() {
    ThemeManager.getInstance().removeThemeChangeListener(this);
}
```

### 3. ❌ Mixing Concerns

```java
// WRONG
public class MyPage extends BaseView {
    public MyPage() {
        Button button = new Button("Load");
        button.setOnAction(e -> {
            // Direct database access ❌
            List<User> users = database.query("SELECT * FROM users");
            
            // Business logic ❌
            users = users.stream()
                .filter(u -> u.isActive())
                .collect(Collectors.toList());
            
            // UI update
            table.setItems(FXCollections.observableList(users));
        });
    }
}

// CORRECT - Use ViewModel
public class MyPage extends BaseView {
    private final MyPageViewModel viewModel;
    
    public MyPage(MyPageViewModel viewModel) {
        this.viewModel = viewModel;
        
        Button button = new Button("Load");
        button.setOnAction(e -> viewModel.loadData()); // ✅
    }
}
```

### 4. ❌ Hard-coded Values

```java
// WRONG
setPadding(new Insets(16, 12, 16, 12));
setPrefWidth(280);
setStyle("-fx-font-size: 14px;");

// CORRECT
setPadding(new Insets(LayoutConstants.DEFAULT_PADDING));
setPrefWidth(LayoutConstants.SIDEBAR_WIDTH);
getStyleClass().add(Styles.TEXT_NORMAL);
```

---

## Checklist

### Before Committing

- [ ] Code follows MVVM pattern
- [ ] No business logic in Views
- [ ] No UI components in ViewModels
- [ ] Used CSS for styling
- [ ] No inline styles
- [ ] No hard-coded values
- [ ] Proper error handling
- [ ] Resources cleaned up
- [ ] Tests written
- [ ] Code formatted (`scripts/format.sh`)
- [ ] No warnings
- [ ] Documentation updated

### Code Review Checklist

- [ ] Single Responsibility followed
- [ ] Methods are small (< 30 lines)
- [ ] Classes are focused (< 300 lines)
- [ ] Meaningful names used
- [ ] No code duplication
- [ ] Proper separation of concerns
- [ ] Testable code
- [ ] Performance considered
- [ ] Accessibility considered
- [ ] Documentation clear

---

## Resources

- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Clean Code](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)
- [JavaFX Best Practices](https://openjfx.io/openjfx-docs/)
- [AtlantaFX Documentation](https://mkpaz.github.io/atlantafx/)

---

**Last Updated**: 2025-11-15  
**Version**: 2.0.0

