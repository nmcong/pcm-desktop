# AtlantaFX Design Integration - Complete Guide

## 📋 Overview

PCM Desktop đã được refactor toàn bộ để sử dụng **AtlantaFX** design system, theo best practices từ AtlantaFX Sampler. Thiết kế mới hiện đại, nhất quán và dễ maintain hơn.

## ✨ Key Improvements

### Before vs After

| Aspect | Before | After |
|--------|--------|-------|
| **CSS** | 900+ lines custom CSS | ~200 lines minimal CSS |
| **Design System** | Custom colors & styles | AtlantaFX variables |
| **Components** | Custom controls | Native AtlantaFX components |
| **Consistency** | Manual styling | Automatic theme consistency |
| **Maintenance** | High effort | Low effort |
| **Dark Mode** | Not supported | Ready (7 themes) |

## 🎨 Refactored Components

### 1. **Navbar** (`components/Navbar.fxml`)
**Changes:**
- ✅ Sử dụng `accent` button style cho "New Screen"
- ✅ Sử dụng `flat` button style cho icons
- ✅ AtlantaFX `title-3` cho brand text
- ✅ Proper spacing với `Insets` và `HBox spacing`
- ✅ Tooltips cho buttons

**AtlantaFX Classes Used:**
- `accent` - Primary action button
- `flat` - Icon buttons
- `title-3` - Typography

### 2. **Sidebar** (`components/Sidebar.fxml`)
**Changes:**
- ✅ Sử dụng `card` cho project lists
- ✅ `list-item` pattern cho clickable items
- ✅ Color-coded project avatars: `accent`, `success`, `warning`, `danger`
- ✅ `text-muted` cho secondary text
- ✅ `ScrollPane` với `edge-to-edge` style
- ✅ `flat` button cho Settings

**AtlantaFX Classes Used:**
- `card` - Container styling
- `list-item` - Interactive list items
- `accent`, `success`, `warning`, `danger` - Semantic colors
- `text-muted` - Muted text
- `flat`, `small` - Button variants

### 3. **Main Content** (`views/MainView.fxml`)
**Changes:**
- ✅ Clean layout sử dụng `BorderPane` patterns
- ✅ Consistent margins và padding
- ✅ TabPane với built-in AtlantaFX styling
- ✅ Proper focus indicators

**AtlantaFX Classes Used:**
- `content` - Main content area
- `tab-header-area` - Tab styling

### 4. **Dialogs & Forms** (Various `.fxml` files)
**Changes:**
- ✅ `card` containers for form groups
- ✅ `form-control` for input fields
- ✅ Button groups với proper spacing
- ✅ Validation state classes

**AtlantaFX Classes Used:**
- `card` - Form containers
- `form-control` - Input styling
- `button-group` - Button collections
- `success`, `warning`, `danger` - Validation states

## 🎯 AtlantaFX Design Tokens Used

### Colors
```css
/* Primary Colors */
-fx-accent-color: var(-color-accent-emphasis);
-fx-success-color: var(-color-success-emphasis);
-fx-warning-color: var(-color-warning-emphasis);
-fx-danger-color: var(-color-danger-emphasis);

/* Text Colors */
-fx-text-fill: var(-color-fg-default);
-fx-text-muted: var(-color-fg-muted);
-fx-text-subtle: var(-color-fg-subtle);

/* Background Colors */
-fx-background-color: var(-color-bg-default);
-fx-background-subtle: var(-color-bg-subtle);
-fx-background-inset: var(-color-bg-inset);
```

### Typography
```css
/* Headers */
.title-1 { -fx-font-size: 2rem; -fx-font-weight: bold; }
.title-2 { -fx-font-size: 1.5rem; -fx-font-weight: bold; }
.title-3 { -fx-font-size: 1.25rem; -fx-font-weight: bold; }
.title-4 { -fx-font-size: 1rem; -fx-font-weight: bold; }

/* Body Text */
.text-caption { -fx-font-size: 0.875rem; }
.text-small { -fx-font-size: 0.75rem; }
.text-muted { -fx-text-fill: var(-color-fg-muted); }
```

### Spacing
```css
/* Padding */
.padding-xs { -fx-padding: 0.25rem; }
.padding-sm { -fx-padding: 0.5rem; }
.padding-md { -fx-padding: 1rem; }
.padding-lg { -fx-padding: 1.5rem; }

/* Margin */
.margin-xs { -fx-margin: 0.25rem; }
.margin-sm { -fx-margin: 0.5rem; }
.margin-md { -fx-margin: 1rem; }
.margin-lg { -fx-margin: 1.5rem; }
```

## 🏗️ Architecture Integration

### 1. **Theme Management**

```java
// Application.java
public class PCMApplication extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Set AtlantaFX theme
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        
        // Load application styles
        Scene scene = new Scene(loadMainView());
        scene.getStylesheets().addAll(
            getClass().getResource("/css/app.css").toExternalForm()
        );
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

### 2. **Custom CSS Integration**

```css
/* app.css - Custom styles on top of AtlantaFX */

/* AI Chat specific styling */
.ai-chat-container {
    -fx-background-color: var(-color-bg-subtle);
    -fx-background-radius: 8px;
    -fx-padding: 1rem;
}

.ai-message {
    -fx-background-color: var(-color-accent-subtle);
    -fx-background-radius: 12px;
    -fx-padding: 0.75rem 1rem;
}

.user-message {
    -fx-background-color: var(-color-bg-inset);
    -fx-background-radius: 12px;
    -fx-padding: 0.75rem 1rem;
}

/* Project cards in sidebar */
.project-card {
    -fx-background-color: var(-color-bg-default);
    -fx-background-radius: 6px;
    -fx-border-color: var(-color-border-default);
    -fx-border-radius: 6px;
    -fx-border-width: 1px;
    -fx-padding: 0.75rem;
}

.project-card:hover {
    -fx-background-color: var(-color-bg-subtle);
    -fx-border-color: var(-color-border-muted);
}

/* Status indicators */
.status-active {
    -fx-background-color: var(-color-success-emphasis);
    -fx-background-radius: 50%;
    -fx-pref-width: 8px;
    -fx-pref-height: 8px;
}

.status-maintenance {
    -fx-background-color: var(-color-warning-emphasis);
    -fx-background-radius: 50%;
    -fx-pref-width: 8px;
    -fx-pref-height: 8px;
}
```

### 3. **Component Factory Pattern**

```java
public class AtlantaFXComponentFactory {
    
    public static Button createAccentButton(String text, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", "accent");
        button.setOnAction(action);
        return button;
    }
    
    public static Button createFlatIconButton(String iconCode, EventHandler<ActionEvent> action) {
        Button button = new Button();
        button.setGraphic(new FontIcon(iconCode));
        button.getStyleClass().addAll("button", "flat", "icon-button");
        button.setOnAction(action);
        return button;
    }
    
    public static VBox createCard(String title, Node... content) {
        VBox card = new VBox();
        card.getStyleClass().addAll("card");
        card.setSpacing(12);
        
        if (title != null) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().addAll("title-4");
            card.getChildren().add(titleLabel);
        }
        
        card.getChildren().addAll(content);
        return card;
    }
    
    public static HBox createListItem(String text, String description) {
        HBox item = new HBox();
        item.getStyleClass().addAll("list-item");
        item.setSpacing(8);
        item.setAlignment(Pos.CENTER_LEFT);
        
        VBox textContainer = new VBox();
        
        Label textLabel = new Label(text);
        textContainer.getChildren().add(textLabel);
        
        if (description != null) {
            Label descLabel = new Label(description);
            descLabel.getStyleClass().addAll("text-caption", "text-muted");
            textContainer.getChildren().add(descLabel);
        }
        
        item.getChildren().add(textContainer);
        return item;
    }
}
```

## 🎨 Theme System

### Available Themes

```java
public enum AppTheme {
    PRIMER_LIGHT(new PrimerLight()),
    PRIMER_DARK(new PrimerDark()),
    NORD_LIGHT(new NordLight()),
    NORD_DARK(new NordDark()),
    CUPERTINO_LIGHT(new CupertinoLight()),
    CUPERTINO_DARK(new CupertinoDark()),
    DRACULA(new Dracula());
    
    private final Theme theme;
    
    AppTheme(Theme theme) {
        this.theme = theme;
    }
    
    public void apply() {
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
    }
}
```

### Theme Manager Service

```java
@Service
public class ThemeManagerService {
    
    private AppTheme currentTheme = AppTheme.PRIMER_LIGHT;
    
    public void setTheme(AppTheme theme) {
        this.currentTheme = theme;
        theme.apply();
        
        // Save to preferences
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        prefs.put("theme", theme.name());
        
        // Notify listeners
        notifyThemeChanged(theme);
    }
    
    public AppTheme getCurrentTheme() {
        return currentTheme;
    }
    
    public void loadSavedTheme() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        String themeName = prefs.get("theme", AppTheme.PRIMER_LIGHT.name());
        
        try {
            AppTheme theme = AppTheme.valueOf(themeName);
            setTheme(theme);
        } catch (IllegalArgumentException e) {
            // Fallback to default theme
            setTheme(AppTheme.PRIMER_LIGHT);
        }
    }
    
    private void notifyThemeChanged(AppTheme theme) {
        // Implementation for theme change notifications
    }
}
```

## 📱 Responsive Design

### Layout Adaptations

```java
public class ResponsiveLayout {
    
    public static void setupResponsiveMainView(BorderPane mainView) {
        mainView.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            
            if (width < 768) {
                // Mobile layout
                setupMobileLayout(mainView);
            } else if (width < 1024) {
                // Tablet layout
                setupTabletLayout(mainView);
            } else {
                // Desktop layout
                setupDesktopLayout(mainView);
            }
        });
    }
    
    private static void setupMobileLayout(BorderPane mainView) {
        // Hide sidebar by default on mobile
        // Use drawer pattern for navigation
    }
    
    private static void setupTabletLayout(BorderPane mainView) {
        // Collapsible sidebar
        // Responsive content area
    }
    
    private static void setupDesktopLayout(BorderPane mainView) {
        // Full sidebar visible
        // Multi-column content when appropriate
    }
}
```

## 🧪 Testing AtlantaFX Integration

### Visual Testing

```java
@TestFXTest
public class AtlantaFXVisualTest {
    
    @Test
    public void testThemeApplication() {
        // Test that themes are applied correctly
        AppTheme.PRIMER_LIGHT.apply();
        
        Button button = AtlantaFXComponentFactory.createAccentButton("Test", null);
        
        // Verify button has correct AtlantaFX classes
        assertTrue(button.getStyleClass().contains("accent"));
        assertTrue(button.getStyleClass().contains("button"));
    }
    
    @Test
    public void testComponentFactoryMethods() {
        VBox card = AtlantaFXComponentFactory.createCard("Test Card", new Label("Content"));
        
        // Verify card structure
        assertTrue(card.getStyleClass().contains("card"));
        assertEquals(2, card.getChildren().size()); // Title + Content
    }
}
```

### Style Verification

```java
public class StyleVerificationTest {
    
    @Test
    public void testCSSVariablesAvailability() {
        Scene scene = new Scene(new Button());
        
        // Apply theme
        AppTheme.PRIMER_LIGHT.apply();
        
        // Test that AtlantaFX variables are available
        StyleOrigin styleOrigin = scene.getRoot().getStyleOrigin();
        // Assert that variables like -color-accent-emphasis are resolved
    }
}
```

## 🔧 Troubleshooting

### Common Issues

1. **AtlantaFX Styles Not Applied**
   - Ensure `Application.setUserAgentStylesheet()` is called before creating scenes
   - Check that AtlantaFX JAR is in classpath
   - Verify theme class imports

2. **Custom CSS Conflicts**
   - Use AtlantaFX variables instead of hardcoded values
   - Follow CSS specificity rules
   - Use `!important` sparingly

3. **Performance Issues**
   - Lazy load themes
   - Cache computed styles
   - Minimize CSS recalculations

### Debug CSS

```java
public class CSSDebugger {
    
    public static void printAppliedStyles(Node node) {
        System.out.println("Node: " + node.getClass().getSimpleName());
        System.out.println("Style classes: " + node.getStyleClass());
        System.out.println("Computed styles: " + node.getStyle());
        
        // Print all CSS properties
        node.getProperties().forEach((key, value) -> {
            if (key.toString().startsWith("css")) {
                System.out.println(key + ": " + value);
            }
        });
    }
}
```

## 📚 Best Practices

### 1. **Use AtlantaFX Classes First**
```java
// ✅ Good - Use AtlantaFX semantic classes
button.getStyleClass().addAll("button", "accent");

// ❌ Avoid - Custom styling
button.setStyle("-fx-background-color: blue;");
```

### 2. **Follow Design Token System**
```css
/* ✅ Good - Use AtlantaFX variables */
.custom-component {
    -fx-background-color: var(-color-bg-subtle);
    -fx-text-fill: var(-color-fg-default);
}

/* ❌ Avoid - Hardcoded colors */
.custom-component {
    -fx-background-color: #f5f5f5;
    -fx-text-fill: #333333;
}
```

### 3. **Maintain Accessibility**
```java
// Always provide accessible names and descriptions
button.setAccessibleText("Create new project");
button.setAccessibleHelp("Opens dialog to create a new project");

// Use semantic colors for meaning
statusIndicator.getStyleClass().add(isActive ? "success" : "danger");
```

### 4. **Test Multiple Themes**
```java
// Test component appearance across themes
@ParameterizedTest
@EnumSource(AppTheme.class)
public void testComponentAcrossThemes(AppTheme theme) {
    theme.apply();
    // Test component rendering
}
```

---

This integration provides PCM Desktop with a modern, consistent, and maintainable design system while preserving the flexibility to customize components as needed.

*Last updated: November 12, 2024*