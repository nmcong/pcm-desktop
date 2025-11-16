# Project Cognition Mentor - UI Module Complete Guide

**Version:** 3.0.0  
**Date:** November 16, 2025  
**Framework:** JavaFX + AtlantaFX

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Base Classes](#base-classes)
3. [Component Library](#component-library)
4. [Constants & Styling](#constants--styling)
5. [Utilities](#utilities)
6. [Creating Pages](#creating-pages)
7. [Best Practices](#best-practices)
8. [Examples](#examples)

---

## Architecture Overview

### MVVM Pattern

```
┌─────────────┐      ┌──────────────┐      ┌───────────┐
│    View     │─────▶│  ViewModel   │─────▶│   Model   │
│  (BaseView) │      │ (Properties) │      │ (Domain)  │
└─────────────┘      └──────────────┘      └───────────┘
      ▲                      │
      └──────────────────────┘
         Data Binding
```

### Package Structure

```
com.noteflix.pcm.ui/
├── base/              # Base classes & interfaces
├── components/        # Reusable UI components
│   ├── common/       # Generic components
│   │   ├── buttons/
│   │   ├── cards/
│   │   ├── dialogs/
│   │   ├── forms/
│   │   └── lists/
│   └── text/         # Text-specific components
├── pages/            # Application pages
│   └── ai/          # AI module pages
├── styles/           # Style constants
├── utils/            # UI utilities
├── layout/           # Layout components
└── viewmodel/        # ViewModels
```

---

## Base Classes

### BaseView

Modern base class for all pages.

```java
public abstract class BaseView extends VBox implements ViewLifecycle {
  protected final String pageTitle;
  protected final String pageDescription;
  protected final FontIcon pageIcon;
  
  protected BaseView(String title, String description, FontIcon icon);
  
  // Template methods
  protected abstract Node createMainContent();
  protected VBox createPageHeader();
  protected Node createPageFooter();
  
  // Lifecycle
  @Override public void onActivate();
  @Override public void onDeactivate();
  @Override public void cleanup();
  
  // Utilities
  protected void setBusy(boolean busy);
  protected void setError(boolean hasError);
}
```

**Features:**
- ✅ Template method pattern
- ✅ Consistent structure
- ✅ Lifecycle management
- ✅ Uses constants (no magic numbers)
- ✅ Built-in busy/error states

### ViewLifecycle Interface

```java
public interface ViewLifecycle {
  void onActivate();    // Called when view becomes active
  void onDeactivate();  // Called when view becomes inactive
  void cleanup();       // Cleanup resources
}
```

### EnhancedBaseViewModel

Enhanced ViewModel with common properties.

```java
public abstract class EnhancedBaseViewModel extends BaseViewModel {
  // Common properties
  private final BooleanProperty busy = new SimpleBooleanProperty(false);
  private final StringProperty errorMessage = new SimpleStringProperty(null);
  private final StringProperty successMessage = new SimpleStringProperty(null);
  
  // Utility methods
  protected void runAsync(Runnable task);
  protected void showError(String message);
  protected void showSuccess(String message);
}
```

---

## Component Library

### Buttons (3 components)

#### IconButton
```java
public class IconButton extends Button {
  public IconButton(Ikon icon, String tooltip);
  public IconButton withAction(Runnable action);
}
```

**Usage:**
```java
IconButton btn = new IconButton(Feather.TRASH, "Delete")
    .withAction(this::handleDelete);
```

#### PrimaryButton
```java
public class PrimaryButton extends Button {
  public PrimaryButton(String text, Runnable action);
  public PrimaryButton(String text, Ikon icon);
  public PrimaryButton withAction(Runnable action);
}
```

**Usage:**
```java
PrimaryButton btn = new PrimaryButton("Save", Feather.SAVE)
    .withAction(this::handleSave);
```

#### SecondaryButton
```java
public class SecondaryButton extends Button {
  public SecondaryButton(String text, Runnable action);
  public SecondaryButton withAction(Runnable action);
}
```

**Usage:**
```java
SecondaryButton btn = new SecondaryButton("Cancel", this::handleCancel);
```

---

### Cards (3 components)

#### Card
Basic card container.

```java
public class Card extends VBox {
  public Card(Node... children);
}
```

**Usage:**
```java
VBox card = UIFactory.createCard();
card.getChildren().addAll(
    UIFactory.createSectionTitle("Settings"),
    UIFactory.createMutedLabel("Configure your preferences")
);
```

#### StatCard
Statistics display card.

```java
public class StatCard extends VBox {
  public StatCard(String title, String value, String subtitle, String colorVar);
}
```

**Usage:**
```java
StatCard card = new StatCard(
    "Total Users",
    "1,234",
    "Active this month",
    ColorConstants.COLOR_SUCCESS
);
```

#### InfoCard
Information display card.

```java
public class InfoCard extends VBox {
  public InfoCard(String title, String description);
}
```

**Usage:**
```java
InfoCard card = new InfoCard(
    "Welcome",
    "Start by creating a new project"
);
```

---

### Lists (3 components)

#### ListItem
```java
public class ListItem extends HBox {
  public ListItem(String title, String description);
}
```

#### AvatarListItem
```java
public class AvatarListItem extends HBox {
  public AvatarListItem(String initials, String name, String details, String colorVar);
}
```

**Usage:**
```java
AvatarListItem item = new AvatarListItem(
    "JD",
    "John Doe",
    "john@example.com",
    ColorConstants.COLOR_PRIMARY
);
```

#### IconListItem
```java
public class IconListItem extends HBox {
  public IconListItem(FontIcon icon, String title, String description);
}
```

**Usage:**
```java
IconListItem item = new IconListItem(
    new FontIcon(Feather.FILE),
    "Document.pdf",
    "Last modified: 2 hours ago"
);
```

---

### Forms (1 component)

#### ValidatedTextField
```java
public class ValidatedTextField extends VBox {
  public ValidatedTextField();
  public ValidatedTextField withValidator(Predicate<String> validator);
  public ValidatedTextField withPlaceholder(String placeholder);
  
  public boolean isValid();
  public void showError(String message);
  public void hideError();
  public String getText();
  public void setText(String text);
}
```

**Usage:**
```java
ValidatedTextField emailField = new ValidatedTextField()
    .withPlaceholder("Enter email")
    .withValidator(text -> text.contains("@"));

if (!emailField.isValid()) {
    emailField.showError("Invalid email address");
}
```

---

### Dialogs (1 component)

#### DialogBuilder
```java
public class DialogBuilder {
  // Factory methods
  public static DialogBuilder info();
  public static DialogBuilder warning();
  public static DialogBuilder error();
  public static DialogBuilder confirm();
  
  // Builder methods
  public DialogBuilder title(String title);
  public DialogBuilder header(String header);
  public DialogBuilder content(String content);
  public DialogBuilder onConfirm(Runnable handler);
  public DialogBuilder onCancel(Runnable handler);
  
  // Show
  public Optional<ButtonType> show();
  public void showAndWait();
}
```

**Usage:**
```java
// Info dialog
DialogBuilder.info()
    .title("Success")
    .content("Operation completed")
    .show();

// Confirmation dialog
DialogBuilder.confirm()
    .title("Delete Item")
    .content("Are you sure you want to delete this?")
    .onConfirm(this::deleteItem)
    .onCancel(this::cancelDelete)
    .show();

// Error dialog
DialogBuilder.error()
    .title("Error")
    .content("Something went wrong: " + errorMessage)
    .show();
```

---

## Constants & Styling

### StyleConstants

CSS class name constants.

```java
public final class StyleConstants {
  // Layout
  public static final String PAGE_CONTAINER = "page-container";
  public static final String PAGE_HEADER = "page-header";
  public static final String PAGE_TITLE = "page-title";
  public static final String PAGE_DESCRIPTION = "page-description";
  
  // Cards
  public static final String CARD = "card";
  public static final String CARD_BORDERED = "card-bordered";
  public static final String CARD_HEADER = "card-header";
  public static final String CARD_BODY = "card-body";
  
  // Buttons
  public static final String BUTTON_PRIMARY = "button-primary";
  public static final String BUTTON_SECONDARY = "button-secondary";
  public static final String BUTTON_DANGER = "button-danger";
  
  // Text
  public static final String TEXT_BOLD = "text-bold";
  public static final String TEXT_MUTED = "text-muted";
  public static final String TEXT_SMALL = "text-small";
  
  // State
  public static final String LOADING = "loading";
  public static final String ERROR = "error";
  public static final String SUCCESS = "success";
  
  // Chat
  public static final String CHAT_SIDEBAR = "chat-sidebar";
  public static final String CHAT_SESSIONS_LIST = "chat-sessions-list";
  public static final String SEARCH_INPUT = "search-input";
}
```

**Usage:**
```java
vbox.getStyleClass().add(StyleConstants.CARD);
label.getStyleClass().add(StyleConstants.TEXT_MUTED);
```

---

### LayoutConstants

Layout dimension constants.

```java
public final class LayoutConstants {
  // Spacing
  public static final double SPACING_XS = 4.0;
  public static final double SPACING_SM = 8.0;
  public static final double SPACING_MD = 12.0;
  public static final double SPACING_LG = 16.0;
  public static final double SPACING_XL = 20.0;
  public static final double SPACING_XXL = 24.0;
  
  // Padding
  public static final Insets PADDING_COMPACT = new Insets(8);
  public static final Insets PADDING_DEFAULT = new Insets(16);
  public static final Insets PADDING_SPACIOUS = new Insets(24);
  public static final Insets PADDING_PAGE = new Insets(20);
  public static final Insets CARD_PADDING = new Insets(16);
  
  // Sizes
  public static final double ICON_SIZE_SM = 16.0;
  public static final double ICON_SIZE_MD = 20.0;
  public static final double ICON_SIZE_LG = 24.0;
  public static final double ICON_SIZE_XL = 32.0;
  
  // Widths
  public static final double SIDEBAR_WIDTH = 280.0;
  public static final double CONTENT_MAX_WIDTH = 1200.0;
  public static final double FORM_FIELD_WIDTH = 300.0;
}
```

**Usage:**
```java
vbox.setSpacing(LayoutConstants.SPACING_MD);
vbox.setPadding(LayoutConstants.PADDING_DEFAULT);
icon.setIconSize(LayoutConstants.ICON_SIZE_SM);
```

---

### ColorConstants

Theme-aware color variables.

```java
public final class ColorConstants {
  // Accent colors
  public static final String COLOR_PRIMARY = "-color-accent-emphasis";
  public static final String COLOR_SECONDARY = "-color-accent-subtle";
  
  // Semantic colors
  public static final String COLOR_SUCCESS = "-color-success-emphasis";
  public static final String COLOR_WARNING = "-color-warning-emphasis";
  public static final String COLOR_DANGER = "-color-danger-emphasis";
  public static final String COLOR_INFO = "-color-info-emphasis";
  
  // Text colors
  public static final String COLOR_TEXT = "-color-fg-default";
  public static final String COLOR_TEXT_MUTED = "-color-fg-muted";
  public static final String COLOR_TEXT_SUBTLE = "-color-fg-subtle";
  
  // Background colors
  public static final String COLOR_BG = "-color-bg-default";
  public static final String COLOR_BG_SUBTLE = "-color-bg-subtle";
  public static final String COLOR_BG_INSET = "-color-bg-inset";
}
```

**Usage:**
```java
label.setStyle("-fx-text-fill: " + ColorConstants.COLOR_SUCCESS + ";");
```

---

## Utilities

### UIFactory

Factory methods for common UI elements.

#### Buttons
```java
// Primary button
Button btn = UIFactory.createPrimaryButton("Save", this::save);

// Secondary button
Button btn = UIFactory.createSecondaryButton("Cancel", this::cancel);

// Danger button
Button btn = UIFactory.createDangerButton("Delete", this::delete);

// Icon button
Button btn = UIFactory.createIconButton(Feather.SETTINGS, "Settings", this::settings);
```

#### Labels
```java
// Title label
Label title = UIFactory.createTitleLabel("Welcome");

// Section title
Label section = UIFactory.createSectionTitle("Settings");

// Muted label
Label muted = UIFactory.createMutedLabel("Optional field");

// Bold label
Label bold = UIFactory.createBoldLabel("Important");
```

#### Layouts
```java
// Card
VBox card = UIFactory.createCard();

// Horizontal spacer (grows to fill space)
Region spacer = UIFactory.createHorizontalSpacer();

// Vertical spacer
Region spacer = UIFactory.createVerticalSpacer();

// Fixed spacer
Region spacer = UIFactory.createSpacer(10, 20);
```

---

### LayoutHelper

Helper methods for creating layouts.

```java
// VBox with spacing
VBox vbox = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);

// HBox with alignment and spacing
HBox hbox = LayoutHelper.createHBox(
    Pos.CENTER_LEFT,
    LayoutConstants.SPACING_SM
);

// Set vertical grow
LayoutHelper.setVGrow(node);

// Set horizontal grow
LayoutHelper.setHGrow(node);
```

---

### DialogHelper

Helper methods for dialogs.

```java
// Info dialog
DialogHelper.showInfo("Success", "Operation completed");

// Warning dialog
DialogHelper.showWarning("Warning", "Please check your input");

// Error dialog
DialogHelper.showError("Error", "Something went wrong");

// Confirmation dialog
Optional<ButtonType> result = DialogHelper.showConfirmation(
    "Delete",
    "Are you sure?"
);
if (result.isPresent() && result.get() == ButtonType.OK) {
    deleteItem();
}
```

---

## Creating Pages

### Step 1: Extend BaseView

```java
package com.noteflix.pcm.ui.pages;

import com.noteflix.pcm.ui.base.BaseView;
import javafx.scene.Node;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class MyPage extends BaseView {
    
    public MyPage() {
        super(
            "My Page",                          // Title
            "Description of my page",           // Description
            new FontIcon(Feather.HOME)         // Icon
        );
    }
    
    @Override
    protected Node createMainContent() {
        // Create your UI here
        return content;
    }
}
```

### Step 2: Build Content with Components

```java
@Override
protected Node createMainContent() {
    VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
    
    // Add a card section
    VBox card = UIFactory.createCard();
    card.getChildren().addAll(
        UIFactory.createSectionTitle("General Settings"),
        createSettingRow("Username", usernameField),
        createSettingRow("Email", emailField)
    );
    
    // Add action buttons
    HBox buttons = LayoutHelper.createHBox(
        Pos.CENTER_RIGHT,
        LayoutConstants.SPACING_SM
    );
    buttons.getChildren().addAll(
        UIFactory.createSecondaryButton("Cancel", this::cancel),
        UIFactory.createPrimaryButton("Save", this::save)
    );
    
    content.getChildren().addAll(card, buttons);
    return content;
}
```

### Step 3: Add Lifecycle Methods (Optional)

```java
@Override
public void onActivate() {
    super.onActivate();
    // Load data, start timers, etc.
    loadData();
}

@Override
public void onDeactivate() {
    super.onDeactivate();
    // Stop timers, save state, etc.
    saveState();
}

@Override
public void cleanup() {
    super.cleanup();
    // Cleanup resources, listeners, etc.
    removeListeners();
}
```

---

## Best Practices

### 1. Use Constants

❌ **Bad:**
```java
VBox box = new VBox(12);
box.setPadding(new Insets(16));
button.getStyleClass().add("button-primary");
```

✅ **Good:**
```java
VBox box = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
box.setPadding(LayoutConstants.PADDING_DEFAULT);
button.getStyleClass().add(StyleConstants.BUTTON_PRIMARY);
```

### 2. Use Factory Methods

❌ **Bad:**
```java
Button btn = new Button("Save");
btn.getStyleClass().add("button-primary");
btn.setOnAction(e -> save());
```

✅ **Good:**
```java
Button btn = UIFactory.createPrimaryButton("Save", this::save);
```

### 3. Use Components

❌ **Bad:**
```java
VBox card = new VBox(12);
card.getStyleClass().add("card");
card.setPadding(new Insets(16));
Label title = new Label("Settings");
title.getStyleClass().add("text-bold");
card.getChildren().add(title);
```

✅ **Good:**
```java
VBox card = UIFactory.createCard();
card.getChildren().add(
    UIFactory.createSectionTitle("Settings")
);
```

### 4. Separate Concerns

✅ **View** (Page):
- UI layout only
- Binding to ViewModel
- Event routing

✅ **ViewModel**:
- Business logic
- Data management
- State handling

✅ **Model**:
- Domain objects
- Data structures

### 5. Lifecycle Management

```java
@Override
public void onActivate() {
    super.onActivate();
    // Start background tasks
    viewModel.startPolling();
}

@Override
public void onDeactivate() {
    super.onDeactivate();
    // Stop background tasks
    viewModel.stopPolling();
}

@Override
public void cleanup() {
    super.cleanup();
    // Remove listeners
    viewModel.removeListeners();
}
```

---

## Examples

### Example 1: Simple Settings Page

```java
public class SettingsPage extends BaseView {
    private final SettingsViewModel viewModel;
    
    public SettingsPage() {
        super("Settings", "Configure application settings", 
              new FontIcon(Feather.SETTINGS));
        this.viewModel = new SettingsViewModel();
    }
    
    @Override
    protected Node createMainContent() {
        VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
        
        // General settings
        VBox generalCard = UIFactory.createCard();
        generalCard.getChildren().addAll(
            UIFactory.createSectionTitle("General"),
            createSettingRow("Theme", createThemeSelector()),
            createSettingRow("Language", createLanguageSelector())
        );
        
        // Action buttons
        HBox buttons = LayoutHelper.createHBox(
            Pos.CENTER_RIGHT,
            LayoutConstants.SPACING_SM
        );
        buttons.getChildren().addAll(
            UIFactory.createSecondaryButton("Reset", this::reset),
            UIFactory.createPrimaryButton("Save", this::save)
        );
        
        content.getChildren().addAll(generalCard, buttons);
        return content;
    }
    
    private HBox createSettingRow(String label, Node control) {
        HBox row = LayoutHelper.createHBox(
            Pos.CENTER_LEFT,
            LayoutConstants.SPACING_MD
        );
        
        Label labelNode = new Label(label);
        labelNode.setPrefWidth(150);
        
        row.getChildren().addAll(
            labelNode,
            control,
            UIFactory.createHorizontalSpacer()
        );
        
        return row;
    }
    
    private void save() {
        viewModel.saveSettings();
        DialogHelper.showInfo("Success", "Settings saved");
    }
    
    private void reset() {
        DialogBuilder.confirm()
            .title("Reset Settings")
            .content("Reset all settings to default?")
            .onConfirm(viewModel::resetSettings)
            .show();
    }
}
```

### Example 2: Data Table Page

```java
public class DataPage extends BaseView {
    private final DataViewModel viewModel;
    private TableView<DataItem> table;
    
    public DataPage() {
        super("Data", "Manage data items",
              new FontIcon(Feather.DATABASE));
        this.viewModel = new DataViewModel();
    }
    
    @Override
    protected Node createMainContent() {
        VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
        
        // Toolbar
        HBox toolbar = createToolbar();
        
        // Table
        table = createTable();
        LayoutHelper.setVGrow(table);
        
        content.getChildren().addAll(toolbar, table);
        return content;
    }
    
    private HBox createToolbar() {
        HBox toolbar = LayoutHelper.createHBox(
            Pos.CENTER_LEFT,
            LayoutConstants.SPACING_SM
        );
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.textProperty().addListener((obs, old, val) -> 
            viewModel.search(val)
        );
        
        toolbar.getChildren().addAll(
            searchField,
            UIFactory.createHorizontalSpacer(),
            UIFactory.createPrimaryButton("Add", Feather.PLUS, this::add),
            UIFactory.createSecondaryButton("Refresh", this::refresh)
        );
        
        return toolbar;
    }
    
    private TableView<DataItem> createTable() {
        TableView<DataItem> table = new TableView<>();
        table.setItems(viewModel.getItems());
        
        // Add columns...
        
        return table;
    }
    
    @Override
    public void onActivate() {
        super.onActivate();
        viewModel.loadData();
    }
}
```

### Example 3: Form Page with Validation

```java
public class FormPage extends BaseView {
    private ValidatedTextField nameField;
    private ValidatedTextField emailField;
    
    public FormPage() {
        super("Create Account", "Fill in your details",
              new FontIcon(Feather.USER_PLUS));
    }
    
    @Override
    protected Node createMainContent() {
        VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
        
        VBox form = UIFactory.createCard();
        
        // Name field
        nameField = new ValidatedTextField()
            .withPlaceholder("Enter your name")
            .withValidator(text -> !text.trim().isEmpty());
        
        // Email field
        emailField = new ValidatedTextField()
            .withPlaceholder("Enter your email")
            .withValidator(text -> text.contains("@"));
        
        form.getChildren().addAll(
            UIFactory.createSectionTitle("Account Details"),
            new Label("Name:"),
            nameField,
            new Label("Email:"),
            emailField
        );
        
        // Submit button
        HBox buttons = LayoutHelper.createHBox(
            Pos.CENTER_RIGHT,
            LayoutConstants.SPACING_SM
        );
        buttons.getChildren().add(
            UIFactory.createPrimaryButton("Create Account", this::submit)
        );
        
        content.getChildren().addAll(form, buttons);
        return content;
    }
    
    private void submit() {
        boolean valid = true;
        
        if (!nameField.isValid()) {
            nameField.showError("Name is required");
            valid = false;
        }
        
        if (!emailField.isValid()) {
            emailField.showError("Valid email is required");
            valid = false;
        }
        
        if (valid) {
            // Submit form
            DialogHelper.showInfo("Success", "Account created!");
        }
    }
}
```

---

## Component Statistics

### Total Components: **17**

- **Buttons:** 3
- **Cards:** 3
- **Lists:** 3
- **Forms:** 1
- **Dialogs:** 1
- **AI Components:** 4
- **Text Components:** 6

### Total Utilities: **3**

- UIFactory
- LayoutHelper
- DialogHelper

### Total Constants: **3**

- StyleConstants
- LayoutConstants
- ColorConstants

### Total Base Classes: **3**

- BaseView
- EnhancedBaseViewModel
- ViewLifecycle

---

## Quick Reference

### Import Statements

```java
// Base classes
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.base.ViewLifecycle;

// Components
import com.noteflix.pcm.ui.components.common.buttons.*;
import com.noteflix.pcm.ui.components.common.cards.*;
import com.noteflix.pcm.ui.components.common.lists.*;
import com.noteflix.pcm.ui.components.common.forms.*;
import com.noteflix.pcm.ui.components.common.dialogs.*;

// Constants
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.ColorConstants;

// Utilities
import com.noteflix.pcm.ui.utils.UIFactory;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.utils.DialogHelper;
```

---

## Troubleshooting

### Common Issues

**Q: My page doesn't show header?**  
A: Override `createPageHeader()` or pass title/description in constructor.

**Q: Lifecycle methods not called?**  
A: Ensure PageNavigator is calling `onActivate()` when navigating.

**Q: Constants not found?**  
A: Add proper imports for `StyleConstants`, `LayoutConstants`.

**Q: Components look unstyled?**  
A: Ensure AtlantaFX CSS is loaded in your Application class.

---

## Resources

- **JavaFX Documentation:** https://openjfx.io/
- **AtlantaFX:** https://github.com/mkpaz/atlantafx
- **Ikonli Icons:** https://kordamp.org/ikonli/

---

**End of UI Guide**

*PCM Desktop - Version 3.0.0*  
*November 16, 2025*

