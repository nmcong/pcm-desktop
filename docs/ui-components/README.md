# UI Components Documentation

This section covers the user interface components, theming system, and UI architecture of PCM Desktop.

## 🎨 Component Overview

PCM Desktop uses **JavaFX** with **AtlantaFX** theme library to provide a modern, consistent, and accessible user interface.

## 📋 Component Categories

### 🏗️ [AtlantaFX Integration](atlantafx-integration.md)
Complete guide to AtlantaFX design system integration.

**Topics Covered:**
- AtlantaFX design system implementation
- Theme management and customization
- Component refactoring for consistency
- Design tokens and variables
- Before/after comparison

### 🎨 [Theming Guide](theming-guide.md)
Comprehensive theming system documentation.

**Topics Covered:**
- Available themes (Primer, Nord, Cupertino, Dracula)
- Custom theme creation
- Theme switching at runtime
- Dark/light mode support
- CSS variable system

### 🧩 [Component Development](component-development.md)
Guidelines for developing custom UI components.

**Topics Covered:**
- Custom component creation patterns
- FXML best practices
- Controller design patterns
- Component lifecycle management
- Testing UI components

### 🎯 [Icon System (Ikonli)](ikonli-icons.md)
Icon management using Ikonli icon library.

**Topics Covered:**
- Icon pack integration
- Icon usage patterns
- Custom icon creation
- Performance optimization
- Accessibility considerations

## 🏗️ Component Architecture

### 1. **Component Hierarchy**
```
Application
├── MainWindow
│   ├── MenuBar
│   ├── NavigationPanel
│   │   ├── SubsystemTree
│   │   ├── ProjectList
│   │   └── QuickSearch
│   ├── ContentArea
│   │   ├── TabManager
│   │   ├── ScreenEditor
│   │   ├── DatabaseViewer
│   │   └── AIChat
│   └── StatusBar
│       ├── ConnectionStatus
│       ├── AIStatus
│       └── SystemInfo
```

### 2. **Component Types**

#### **Layout Components**
- `MainView` - Primary application layout
- `Sidebar` - Navigation and project management
- `ContentArea` - Main working area
- `StatusBar` - System status and notifications

#### **Input Components**
- `ProjectForm` - Project creation and editing
- `ScreenForm` - Screen definition forms
- `SearchBox` - Quick search functionality
- `FilterPanel` - Advanced filtering controls

#### **Display Components**
- `ProjectCard` - Project summary display
- `ScreenList` - Screen listing with metadata
- `DatabaseTree` - Database object hierarchy
- `AIResponseView` - AI conversation display

#### **Dialog Components**
- `SettingsDialog` - Application settings
- `AboutDialog` - Application information
- `ConfirmationDialog` - User confirmations
- `ErrorDialog` - Error message display

## 🎨 Design System

### 1. **Color Palette**
```css
/* Primary Colors */
--color-accent: #0066CC;
--color-success: #28A745;
--color-warning: #FFC107;
--color-danger: #DC3545;

/* Neutral Colors */
--color-bg-default: #FFFFFF;
--color-bg-subtle: #F6F8FA;
--color-bg-inset: #F1F3F4;
--color-fg-default: #24292F;
--color-fg-muted: #656D76;
--color-fg-subtle: #8C959F;

/* Border Colors */
--color-border-default: #D0D7DE;
--color-border-muted: #D8DEE4;
--color-border-subtle: #AFB8C1;
```

### 2. **Typography Scale**
```css
/* Headers */
.title-1 { font-size: 32px; font-weight: 600; }
.title-2 { font-size: 24px; font-weight: 600; }
.title-3 { font-size: 20px; font-weight: 600; }
.title-4 { font-size: 16px; font-weight: 600; }

/* Body Text */
.text-body { font-size: 14px; font-weight: 400; }
.text-small { font-size: 12px; font-weight: 400; }
.text-caption { font-size: 11px; font-weight: 400; }
```

### 3. **Spacing System**
```css
/* Spacing Scale */
--spacing-xs: 4px;
--spacing-sm: 8px;
--spacing-md: 16px;
--spacing-lg: 24px;
--spacing-xl: 32px;
--spacing-2xl: 48px;
--spacing-3xl: 64px;
```

### 4. **Component Sizing**
```css
/* Button Sizes */
.button-small { height: 28px; padding: 0 8px; }
.button-medium { height: 32px; padding: 0 12px; }
.button-large { height: 40px; padding: 0 16px; }

/* Input Sizes */
.input-small { height: 28px; }
.input-medium { height: 32px; }
.input-large { height: 40px; }
```

## 🧩 Custom Component Patterns

### 1. **Component Factory**
```java
public class ComponentFactory {
    
    public static Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", "accent");
        return button;
    }
    
    public static VBox createCard(String title, Node... content) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        
        if (title != null) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("title-4");
            card.getChildren().add(titleLabel);
        }
        
        card.getChildren().addAll(content);
        return card;
    }
}
```

### 2. **Custom Control Pattern**
```java
public class ProjectCard extends VBox {
    
    private final Label titleLabel;
    private final Label descriptionLabel;
    private final HBox actionBar;
    
    public ProjectCard() {
        initialize();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initialize() {
        getStyleClass().add("project-card");
        setSpacing(8);
        setPadding(new Insets(12));
    }
    
    private void setupLayout() {
        titleLabel = new Label();
        titleLabel.getStyleClass().add("title-4");
        
        descriptionLabel = new Label();
        descriptionLabel.getStyleClass().addAll("text-small", "text-muted");
        
        actionBar = new HBox(4);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        
        getChildren().addAll(titleLabel, descriptionLabel, actionBar);
    }
}
```

### 3. **MVVM Pattern**
```java
// View Model
public class ProjectViewModel {
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final BooleanProperty isActive = new SimpleBooleanProperty();
    
    // Getters and setters...
}

// Controller
@FXML
public class ProjectCardController {
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Circle statusIndicator;
    
    private ProjectViewModel viewModel;
    
    public void initialize() {
        bindViewModel();
    }
    
    private void bindViewModel() {
        titleLabel.textProperty().bind(viewModel.titleProperty());
        descriptionLabel.textProperty().bind(viewModel.descriptionProperty());
        // Status indicator binding...
    }
}
```

## 🎭 Theming System

### 1. **Theme Manager**
```java
@Service
public class ThemeManager {
    
    private final ObservableList<Theme> availableThemes;
    private final ObjectProperty<Theme> currentTheme;
    
    public void setTheme(Theme theme) {
        currentTheme.set(theme);
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
        saveThemePreference(theme);
    }
    
    public void toggleDarkMode() {
        Theme current = currentTheme.get();
        Theme opposite = current.isDark() ? 
            findLightVariant(current) : findDarkVariant(current);
        setTheme(opposite);
    }
}
```

### 2. **Dynamic Theming**
```java
public class DynamicThemeProvider {
    
    public void customizeTheme(String primaryColor, String accentColor) {
        String customCSS = String.format("""
            .root {
                -color-accent-emphasis: %s;
                -color-accent-fg: %s;
                -color-accent-subtle: %s;
            }
            """, primaryColor, accentColor, lighten(primaryColor, 0.9));
            
        Scene scene = getMainScene();
        scene.getStylesheets().add(createStylesheet(customCSS));
    }
}
```

## 🧪 Component Testing

### 1. **Visual Testing**
```java
@ExtendWith(ApplicationExtension.class)
public class ProjectCardTest {
    
    @Test
    public void shouldDisplayProjectInformation() {
        // Given
        ProjectViewModel viewModel = new ProjectViewModel();
        viewModel.setTitle("Test Project");
        viewModel.setDescription("Test Description");
        
        // When
        ProjectCard card = new ProjectCard();
        card.setViewModel(viewModel);
        
        // Then
        assertEquals("Test Project", card.getTitleText());
        assertEquals("Test Description", card.getDescriptionText());
    }
}
```

### 2. **Interaction Testing**
```java
@Test
public void shouldHandleButtonClick() {
    // Given
    AtomicBoolean clicked = new AtomicBoolean(false);
    Button button = ComponentFactory.createPrimaryButton("Click Me");
    button.setOnAction(e -> clicked.set(true));
    
    // When
    clickOn(button);
    
    // Then
    assertTrue(clicked.get());
}
```

## 🚀 Performance Optimization

### 1. **Lazy Loading**
```java
public class LazyLoadingListView<T> extends ListView<T> {
    
    private final int pageSize;
    private final ObservableList<T> allItems;
    private final ObservableList<T> visibleItems;
    
    public void loadNextPage() {
        int currentSize = visibleItems.size();
        int endIndex = Math.min(currentSize + pageSize, allItems.size());
        
        List<T> nextPage = allItems.subList(currentSize, endIndex);
        Platform.runLater(() -> visibleItems.addAll(nextPage));
    }
}
```

### 2. **Virtual Flow**
```java
public class VirtualizedTreeView<T> extends TreeView<T> {
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new VirtualizedTreeViewSkin<>(this);
    }
    
    // Custom skin implementation for virtualization
}
```

## 📱 Responsive Design

### 1. **Adaptive Layout**
```java
public class ResponsivePane extends BorderPane {
    
    public ResponsivePane() {
        widthProperty().addListener(this::handleWidthChange);
    }
    
    private void handleWidthChange(Observable obs, Number oldVal, Number newVal) {
        double width = newVal.doubleValue();
        
        if (width < 600) {
            setupMobileLayout();
        } else if (width < 1024) {
            setupTabletLayout();
        } else {
            setupDesktopLayout();
        }
    }
}
```

### 2. **Breakpoint System**
```css
/* Responsive CSS */
@media screen and (max-width: 600px) {
    .sidebar { -fx-pref-width: 0; }
    .content { -fx-padding: 8px; }
}

@media screen and (min-width: 601px) and (max-width: 1024px) {
    .sidebar { -fx-pref-width: 200px; }
    .content { -fx-padding: 16px; }
}

@media screen and (min-width: 1025px) {
    .sidebar { -fx-pref-width: 300px; }
    .content { -fx-padding: 24px; }
}
```

## 🎯 Accessibility

### 1. **Screen Reader Support**
```java
button.setAccessibleText("Create new project");
button.setAccessibleHelp("Opens dialog to create a new project in the current subsystem");
button.setAccessibleRole(AccessibleRole.BUTTON);
```

### 2. **Keyboard Navigation**
```java
// Custom key event handling
scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
    if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
        handleQuickAction();
        event.consume();
    }
});
```

### 3. **High Contrast Support**
```css
/* High contrast theme overrides */
@media (prefers-contrast: high) {
    .button {
        -fx-border-width: 2px;
        -fx-border-color: -color-fg-default;
    }
    
    .text-muted {
        -fx-text-fill: -color-fg-default;
    }
}
```

## 📚 Best Practices

### 1. **Component Guidelines**
- Keep components focused and single-purpose
- Use composition over inheritance
- Implement proper data binding
- Follow naming conventions
- Provide comprehensive documentation

### 2. **Styling Guidelines**
- Use AtlantaFX design tokens
- Avoid hardcoded colors and sizes
- Maintain consistent spacing
- Test across multiple themes
- Optimize for accessibility

### 3. **Performance Guidelines**
- Minimize scene graph complexity
- Use virtual flows for large data sets
- Implement proper caching
- Avoid memory leaks with listeners
- Profile UI performance regularly

---

For specific UI component topics, select the appropriate document from the links above.

*Last updated: November 12, 2024*