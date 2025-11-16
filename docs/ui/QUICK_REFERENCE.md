# 🚀 UI Components Quick Reference

> **Quick guide for using the refactored UI components**

---

## 📦 Components Library (15 Components)

### Buttons

```java
// Primary button
PrimaryButton btn = new PrimaryButton("Save", Feather.SAVE)
    .withAction(this::save);

// Secondary button  
SecondaryButton btn = new SecondaryButton("Cancel", this::cancel);

// Icon button
IconButton btn = new IconButton(Feather.TRASH, "Delete")
    .withAction(this::delete);
```

### Cards

```java
// Basic card
VBox card = UIFactory.createCard();

// Stat card
StatCard card = new StatCard(
    "Total Users", 
    "1,234",
    "Active this month",
    ColorConstants.COLOR_SUCCESS
);

// Info card
InfoCard card = new InfoCard(
    "Welcome",
    "This is the description"
);
```

### Lists

```java
// Basic list item
ListItem item = new ListItem("Title", "Description");

// Icon list item
IconListItem item = new IconListItem(
    new FontIcon(Feather.FILE),
    "Document.pdf",
    "Last modified: 2 hours ago"
);

// Avatar list item
AvatarListItem item = new AvatarListItem(
    "JD",
    "John Doe",
    "john@example.com",
    ColorConstants.COLOR_PRIMARY
);
```

### Forms

```java
// Validated text field
ValidatedTextField field = new ValidatedTextField()
    .withPlaceholder("Enter email")
    .withValidator(text -> text.contains("@"));

if (!field.isValid()) {
    field.showError("Invalid email");
}
```

### Dialogs

```java
// Info dialog
DialogBuilder.info()
    .title("Success")
    .content("Operation completed")
    .show();

// Confirmation dialog
DialogBuilder.confirm()
    .title("Delete Item")
    .content("Are you sure?")
    .onConfirm(this::deleteItem)
    .show();

// Error dialog
DialogBuilder.error()
    .title("Error")
    .content("Something went wrong")
    .show();
```

---

## 🎨 UIFactory Methods

### Buttons
```java
UIFactory.createPrimaryButton(text, action)
UIFactory.createSecondaryButton(text, action)
UIFactory.createDangerButton(text, action)
UIFactory.createIconButton(icon, tooltip, action)
```

### Labels
```java
UIFactory.createTitleLabel(text)
UIFactory.createSectionTitle(text)
UIFactory.createMutedLabel(text)
UIFactory.createBoldLabel(text)
```

### Layouts
```java
UIFactory.createCard()                    // VBox with card style
UIFactory.createHorizontalSpacer()        // Growing spacer
UIFactory.createVerticalSpacer()          // Growing spacer
UIFactory.createSpacer(width, height)     // Fixed spacer
```

---

## 📏 LayoutHelper Methods

### Containers
```java
// VBox with spacing
VBox box = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);

// HBox with alignment and spacing
HBox box = LayoutHelper.createHBox(
    Pos.CENTER_LEFT, 
    LayoutConstants.SPACING_SM
);

// VBox with padding
VBox box = LayoutHelper.createVBox(spacing);
box.setPadding(LayoutConstants.PADDING_DEFAULT);
```

### Growth Control
```java
LayoutHelper.setVGrow(node);  // Vertical grow
LayoutHelper.setHGrow(node);  // Horizontal grow
```

---

## 📐 Constants

### Layout Constants
```java
// Spacing
LayoutConstants.SPACING_XS   // 4.0
LayoutConstants.SPACING_SM   // 8.0
LayoutConstants.SPACING_MD   // 12.0
LayoutConstants.SPACING_LG   // 16.0
LayoutConstants.SPACING_XL   // 20.0
LayoutConstants.SPACING_XXL  // 24.0

// Padding
LayoutConstants.PADDING_COMPACT   // Insets(8)
LayoutConstants.PADDING_DEFAULT   // Insets(16)
LayoutConstants.PADDING_SPACIOUS  // Insets(24)

// Sizes
LayoutConstants.ICON_SIZE_SM   // 16
LayoutConstants.ICON_SIZE_MD   // 20
LayoutConstants.ICON_SIZE_LG   // 24

// Widths
LayoutConstants.SIDEBAR_WIDTH  // 280
```

### Style Constants
```java
// Cards
StyleConstants.CARD
StyleConstants.CARD_BORDERED

// Buttons
StyleConstants.BUTTON_PRIMARY
StyleConstants.BUTTON_SECONDARY
StyleConstants.BUTTON_DANGER

// Text
StyleConstants.TEXT_BOLD
StyleConstants.TEXT_MUTED
StyleConstants.TEXT_SMALL
```

### Color Constants
```java
ColorConstants.COLOR_PRIMARY
ColorConstants.COLOR_SUCCESS
ColorConstants.COLOR_WARNING
ColorConstants.COLOR_DANGER
```

---

## 🏗️ Creating a New Page

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
            "Page description",                 // Description
            new FontIcon(Feather.HOME)         // Icon
        );
    }
    
    @Override
    protected Node createMainContent() {
        // Your UI here
        return content;
    }
}
```

### Step 2: Build Content with Helpers

```java
@Override
protected Node createMainContent() {
    VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
    
    // Add a card
    VBox card = UIFactory.createCard();
    card.getChildren().addAll(
        UIFactory.createSectionTitle("Section Title"),
        UIFactory.createMutedLabel("Description here")
    );
    
    // Add buttons
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
}

@Override
public void onDeactivate() {
    super.onDeactivate();
    // Cleanup resources
}
```

---

## 🎯 Common Patterns

### Pattern 1: Settings Row
```java
private HBox createSettingRow(String label, Node value, Button action) {
    HBox row = LayoutHelper.createHBox(
        Pos.CENTER_LEFT,
        LayoutConstants.SPACING_MD
    );
    
    Label labelNode = new Label(label);
    labelNode.setPrefWidth(150);
    
    row.getChildren().addAll(
        labelNode,
        value,
        UIFactory.createHorizontalSpacer(),
        action
    );
    
    return row;
}
```

### Pattern 2: Form Section
```java
private VBox createFormSection(String title) {
    VBox section = UIFactory.createCard();
    section.getChildren().add(
        UIFactory.createSectionTitle(title)
    );
    return section;
}
```

### Pattern 3: Action Buttons Row
```java
private HBox createActionButtons() {
    HBox buttons = LayoutHelper.createHBox(
        Pos.CENTER_RIGHT,
        LayoutConstants.SPACING_SM
    );
    
    buttons.getChildren().addAll(
        UIFactory.createSecondaryButton("Cancel", this::cancel),
        UIFactory.createPrimaryButton("Save", this::save)
    );
    
    return buttons;
}
```

---

## 🚨 Common Mistakes to Avoid

### ❌ Don't Do This
```java
// Hard-coded spacing
VBox box = new VBox(12);

// Hard-coded padding
box.setPadding(new Insets(16));

// Hard-coded CSS classes
button.getStyleClass().add("button-primary");

// Manual button creation
Button btn = new Button("Click Me");
btn.setOnAction(e -> handleClick());
```

### ✅ Do This Instead
```java
// Use constants
VBox box = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);

// Use constants
box.setPadding(LayoutConstants.PADDING_DEFAULT);

// Use constants
button.getStyleClass().add(StyleConstants.BUTTON_PRIMARY);

// Use factory
Button btn = UIFactory.createPrimaryButton("Click Me", this::handleClick);
```

---

## 📚 Full Example

```java
package com.noteflix.pcm.ui.pages;

import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.utils.UIFactory;
import com.noteflix.pcm.ui.utils.DialogHelper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class ExamplePage extends BaseView {
    
    public ExamplePage() {
        super(
            "Example Page",
            "Demonstrates the new UI components",
            new FontIcon(Feather.LAYERS)
        );
    }
    
    @Override
    protected Node createMainContent() {
        VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
        content.setPadding(LayoutConstants.PADDING_DEFAULT);
        
        // Section 1: Info Card
        VBox infoCard = UIFactory.createCard();
        infoCard.getChildren().addAll(
            UIFactory.createSectionTitle("Information"),
            UIFactory.createMutedLabel("This is a sample page using new components")
        );
        
        // Section 2: Action Buttons
        HBox buttons = LayoutHelper.createHBox(
            Pos.CENTER_LEFT,
            LayoutConstants.SPACING_SM
        );
        buttons.getChildren().addAll(
            UIFactory.createPrimaryButton("Primary", this::handlePrimary),
            UIFactory.createSecondaryButton("Secondary", this::handleSecondary)
        );
        
        content.getChildren().addAll(infoCard, buttons);
        return content;
    }
    
    private void handlePrimary() {
        DialogHelper.showInfo("Success", "Primary action executed");
    }
    
    private void handleSecondary() {
        DialogHelper.showInfo("Info", "Secondary action executed");
    }
}
```

---

## 🔗 Related Documents

- **[COMPLETE_REFACTORING_SUMMARY.md](./COMPLETE_REFACTORING_SUMMARY.md)** - Full refactoring details
- **[PHASE2_SUMMARY.md](./PHASE2_SUMMARY.md)** - Phase 2 summary
- **[REFACTORING_SUMMARY.md](./REFACTORING_SUMMARY.md)** - Phase 1 summary

---

*Last Updated: November 16, 2025*

