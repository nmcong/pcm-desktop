# UI Component Library

> **Package**: `com.noteflix.pcm.ui.components`  
> **Version**: 2.0.0  
> **Status**: In Development

## Mục Lục

- [Overview](#overview)
- [Common Components](#common-components)
- [Navigation Components](#navigation-components)
- [Text Components](#text-components)
- [Widgets](#widgets)
- [Layout Components](#layout-components)
- [Usage Examples](#usage-examples)

---

## Overview

Component Library cung cấp các UI components tái sử dụng, nhất quán và dễ dàng customize. Tất cả components tuân theo
AtlantaFX design system và JavaFX best practices.

### Design Principles

1. **Reusability**: Components có thể dùng ở nhiều nơi
2. **Consistency**: Styling và behavior nhất quán
3. **Flexibility**: Dễ dàng customize
4. **Accessibility**: Tuân thủ WCAG standards
5. **Performance**: Optimized cho performance

---

## Common Components

### Buttons

#### IconButton

Button chỉ có icon, không có text.

```java
public class IconButton extends Button {
    public IconButton(Octicons icon, String tooltip);
    public IconButton withSize(int size);
    public IconButton withStyle(String... styles);
}
```

**Example:**

```java
IconButton addButton = new IconButton(Octicons.PLUS_16, "Add Item");
addButton.setOnAction(e -> handleAdd());

IconButton deleteButton = new IconButton(Octicons.TRASH_16, "Delete")
    .withStyle(Styles.DANGER);
```

#### PrimaryButton

Primary action button với accent color.

```java
public class PrimaryButton extends Button {
    public PrimaryButton(String text);
    public PrimaryButton(String text, Octicons icon);
    public PrimaryButton withAction(Runnable action);
}
```

**Example:**

```java
PrimaryButton submitButton = new PrimaryButton("Submit", Octicons.CHECK_16)
    .withAction(this::handleSubmit);
```

#### SecondaryButton

Secondary action button với subtle styling.

```java
public class SecondaryButton extends Button {
    public SecondaryButton(String text);
    public SecondaryButton(String text, Octicons icon);
    public SecondaryButton withAction(Runnable action);
}
```

**Example:**

```java
SecondaryButton cancelButton = new SecondaryButton("Cancel")
    .withAction(this::handleCancel);
```

#### ButtonBuilder

Fluent API để tạo custom buttons.

```java
public class ButtonBuilder {
    public ButtonBuilder withText(String text);
    public ButtonBuilder withIcon(Octicons icon);
    public ButtonBuilder withStyle(ButtonStyle style);
    public ButtonBuilder withAction(Runnable action);
    public ButtonBuilder withTooltip(String tooltip);
    public ButtonBuilder disabled(boolean disabled);
    public Button build();
}
```

**Example:**

```java
Button customButton = new ButtonBuilder()
    .withText("Process")
    .withIcon(Octicons.GEAR_16)
    .withStyle(ButtonStyle.SUCCESS)
    .withAction(this::processData)
    .withTooltip("Process all items")
    .build();
```

**Button Styles:**

```java
public enum ButtonStyle {
    PRIMARY,    // Accent color
    SECONDARY,  // Subtle
    SUCCESS,    // Green
    WARNING,    // Orange
    DANGER,     // Red
    FLAT,       // No background
    OUTLINED    // Border only
}
```

---

### Cards

#### Card

Basic card container với padding và shadow.

```java
public class Card extends VBox {
    public Card();
    public Card withTitle(String title);
    public Card withContent(Node... content);
    public Card withFooter(Node footer);
    public Card withStyle(String... styles);
}
```

**Example:**

```java
Card infoCard = new Card()
    .withTitle("User Information")
    .withContent(
        new Label("Name: John Doe"),
        new Label("Email: john@example.com")
    )
    .withFooter(editButton);
```

#### StatCard

Card để hiển thị statistics.

```java
public class StatCard extends Card {
    public StatCard(String label, String value);
    public StatCard withSubtitle(String subtitle);
    public StatCard withIcon(Octicons icon);
    public StatCard withColor(String colorVar);
    public StatCard withTrend(TrendDirection direction, String change);
}
```

**Example:**

```java
StatCard activeUsers = new StatCard("Active Users", "1,234")
    .withSubtitle("Currently online")
    .withIcon(Octicons.PERSON_16)
    .withColor("-color-success-emphasis")
    .withTrend(TrendDirection.UP, "+12%");
```

**Trend Direction:**

```java
public enum TrendDirection {
    UP,      // ↑ Positive trend
    DOWN,    // ↓ Negative trend
    NEUTRAL  // → No change
}
```

#### InfoCard

Card với icon và description.

```java
public class InfoCard extends Card {
    public InfoCard(String title, String description, Octicons icon);
    public InfoCard withAction(String text, Runnable action);
}
```

**Example:**

```java
InfoCard helpCard = new InfoCard(
    "Need Help?",
    "Contact our support team for assistance",
    Octicons.QUESTION_16
).withAction("Contact Support", this::openSupport);
```

---

### Forms

#### FormField

Single form field với label và validation.

```java
public class FormField extends VBox {
    public FormField(String label, Node control);
    public FormField withRequired(boolean required);
    public FormField withValidation(Validator validator);
    public FormField withHelp(String helpText);
    public boolean validate();
    public void setError(String errorMessage);
    public void clearError();
}
```

**Example:**

```java
TextField nameField = new TextField();
FormField nameFormField = new FormField("Name", nameField)
    .withRequired(true)
    .withValidation(value -> !value.isEmpty())
    .withHelp("Enter your full name");
```

#### ValidatedTextField

TextField với built-in validation.

```java
public class ValidatedTextField extends TextField {
    public ValidatedTextField();
    public ValidatedTextField withValidator(Validator validator);
    public ValidatedTextField withPattern(String regex);
    public ValidatedTextField withMinLength(int minLength);
    public ValidatedTextField withMaxLength(int maxLength);
    public boolean isValid();
    public void showError(String message);
    public void clearError();
}
```

**Example:**

```java
ValidatedTextField emailField = new ValidatedTextField()
    .withPattern("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}")
    .withPlaceholder("email@example.com");

if (!emailField.isValid()) {
    emailField.showError("Invalid email format");
}
```

#### FormBuilder

Fluent API để build forms.

```java
public class FormBuilder {
    public FormBuilder addField(String label, Node control);
    public FormBuilder addField(FormField field);
    public FormBuilder addSection(String title);
    public FormBuilder addSeparator();
    public FormBuilder withSpacing(double spacing);
    public FormBuilder withPadding(Insets padding);
    public Form build();
}
```

**Example:**

```java
Form userForm = new FormBuilder()
    .addSection("Personal Information")
    .addField("Name", nameField)
    .addField("Email", emailField)
    .addSeparator()
    .addSection("Account Settings")
    .addField("Username", usernameField)
    .addField("Password", passwordField)
    .build();

if (userForm.validate()) {
    saveUser();
}
```

---

### Dialogs

#### DialogBuilder

Fluent API để tạo dialogs.

```java
public class DialogBuilder {
    public static DialogBuilder info();
    public static DialogBuilder warning();
    public static DialogBuilder error();
    public static DialogBuilder confirm();
    
    public DialogBuilder title(String title);
    public DialogBuilder header(String header);
    public DialogBuilder content(String content);
    public DialogBuilder content(Node content);
    public DialogBuilder buttons(ButtonType... buttons);
    public DialogBuilder onConfirm(Runnable action);
    public DialogBuilder onCancel(Runnable action);
    
    public Optional<ButtonType> show();
    public void showAndWait();
}
```

**Example:**

```java
// Info dialog
DialogBuilder.info()
    .title("Success")
    .content("Operation completed successfully")
    .show();

// Confirm dialog
DialogBuilder.confirm()
    .title("Delete Item")
    .content("Are you sure you want to delete this item?")
    .onConfirm(this::deleteItem)
    .show();

// Custom dialog
DialogBuilder.warning()
    .title("Warning")
    .header("Data will be lost")
    .content(customWarningContent)
    .buttons(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
    .show();
```

#### ConfirmDialog

Pre-configured confirmation dialog.

```java
public class ConfirmDialog {
    public static boolean show(String title, String message);
    public static boolean showDelete(String itemName);
    public static boolean showDiscard(String message);
}
```

**Example:**

```java
if (ConfirmDialog.showDelete("User Account")) {
    deleteUserAccount();
}
```

#### InfoDialog

Pre-configured info dialog.

```java
public class InfoDialog {
    public static void show(String title, String message);
    public static void showSuccess(String message);
    public static void showError(String message);
    public static void showWarning(String message);
}
```

---

### Lists

#### ListItem

Generic list item với flexible layout.

```java
public class ListItem extends HBox {
    public ListItem();
    public ListItem withLeading(Node leading);
    public ListItem withTitle(String title);
    public ListItem withSubtitle(String subtitle);
    public ListItem withTrailing(Node trailing);
    public ListItem withAction(Runnable action);
}
```

**Example:**

```java
ListItem item = new ListItem()
    .withLeading(new FontIcon(Octicons.FILE_16))
    .withTitle("Document.pdf")
    .withSubtitle("Modified 2 hours ago")
    .withTrailing(moreButton)
    .withAction(this::openDocument);
```

#### AvatarListItem

List item với avatar (initials hoặc image).

```java
public class AvatarListItem extends ListItem {
    public AvatarListItem(String initials, String name);
    public AvatarListItem(Image avatar, String name);
    public AvatarListItem withSubtitle(String subtitle);
    public AvatarListItem withColor(String color);
    public AvatarListItem withStatus(Status status);
}
```

**Example:**

```java
AvatarListItem userItem = new AvatarListItem("JD", "John Doe")
    .withSubtitle("john@example.com")
    .withColor("-color-accent-emphasis")
    .withStatus(Status.ONLINE);
```

#### IconListItem

List item với icon.

```java
public class IconListItem extends ListItem {
    public IconListItem(Octicons icon, String title);
    public IconListItem withSubtitle(String subtitle);
    public IconListItem withBadge(String badgeText);
}
```

---

## Navigation Components

### NavigationBar

Top navigation bar với breadcrumb và actions.

```java
public class NavigationBar extends HBox {
    public NavigationBar();
    public NavigationBar withBreadcrumb(String... items);
    public NavigationBar withTitle(String title);
    public NavigationBar withActions(Node... actions);
}
```

**Example:**

```java
NavigationBar navBar = new NavigationBar()
    .withBreadcrumb("Projects", "Customer Service", "Login Screen")
    .withActions(
        editButton,
        shareButton,
        moreButton
    );
```

### Breadcrumb

Breadcrumb navigation component.

```java
public class Breadcrumb extends HBox {
    public Breadcrumb(String... items);
    public Breadcrumb(List<BreadcrumbItem> items);
    public void setOnItemClick(Consumer<String> handler);
}
```

**Example:**

```java
Breadcrumb breadcrumb = new Breadcrumb("Home", "Projects", "Details");
breadcrumb.setOnItemClick(item -> navigateTo(item));
```

### AppHeader

Application header với logo, search, và user menu.

```java
public class AppHeader extends HBox {
    public AppHeader();
    public AppHeader withLogo(Node logo);
    public AppHeader withSearch(boolean enabled);
    public AppHeader withUserMenu(Node userMenu);
    public AppHeader withActions(Node... actions);
}
```

### SidebarView

Main application sidebar (existing, refactored).

```java
public class SidebarView extends VBox {
    public SidebarView();
    public void setPageNavigator(PageNavigator navigator);
    public void addMenuItem(String text, Octicons icon, Runnable action);
    public void addSection(String title);
    public void addFavorite(String initials, String name, String details);
}
```

---

## Text Components

### UniversalTextComponent

Universal text component hỗ trợ nhiều content types.

```java
public class UniversalTextComponent extends BorderPane {
    public UniversalTextComponent();
    public void setContent(String content, TextContentType type);
    public void setViewMode(ViewMode mode);
    public ViewMode getViewMode();
    public String getContent();
}
```

**Content Types:**

```java
public enum TextContentType {
    PLAIN,      // Plain text
    MARKDOWN,   // Markdown
    CODE,       // Source code
    JSON,       // JSON
    XML,        // XML
    HTML        // HTML
}
```

**View Modes:**

```java
public enum ViewMode {
    EDIT,       // Editor mode
    PREVIEW,    // Preview mode
    SPLIT       // Split editor/preview
}
```

**Example:**

```java
UniversalTextComponent textComponent = new UniversalTextComponent();
textComponent.setContent(markdownText, TextContentType.MARKDOWN);
textComponent.setViewMode(ViewMode.SPLIT);
```

---

## Widgets

### SearchBox

Search input với icon và clear button.

```java
public class SearchBox extends HBox {
    public SearchBox();
    public SearchBox(String placeholder);
    public StringProperty textProperty();
    public void setOnSearch(Consumer<String> handler);
    public void clear();
}
```

**Example:**

```java
SearchBox searchBox = new SearchBox("Search conversations...");
searchBox.setOnSearch(query -> handleSearch(query));
```

### LoadingIndicator

Loading spinner với optional message.

```java
public class LoadingIndicator extends VBox {
    public LoadingIndicator();
    public LoadingIndicator(String message);
    public void setMessage(String message);
    public void show();
    public void hide();
}
```

**Example:**

```java
LoadingIndicator loader = new LoadingIndicator("Loading data...");
contentArea.getChildren().add(loader);

// When done
loader.hide();
```

### StatusBadge

Colored badge để hiển thị status.

```java
public class StatusBadge extends Label {
    public StatusBadge(String text, StatusType type);
    public void setStatus(StatusType type);
}
```

**Status Types:**

```java
public enum StatusType {
    SUCCESS,    // Green
    WARNING,    // Orange
    ERROR,      // Red
    INFO,       // Blue
    NEUTRAL     // Gray
}
```

**Example:**

```java
StatusBadge activeBadge = new StatusBadge("Active", StatusType.SUCCESS);
StatusBadge pendingBadge = new StatusBadge("Pending", StatusType.WARNING);
```

### ThemeToggle

Toggle button để switch theme.

```java
public class ThemeToggle extends Button {
    public ThemeToggle();
    public void setOnThemeChange(Consumer<Boolean> handler);
}
```

---

## Layout Components

### TwoColumnLayout

Layout với 2 cột: sidebar và content.

```java
public class TwoColumnLayout extends HBox {
    public TwoColumnLayout();
    public TwoColumnLayout withSidebar(Node sidebar);
    public TwoColumnLayout withContent(Node content);
    public TwoColumnLayout withSidebarWidth(double width);
    public TwoColumnLayout withSpacing(double spacing);
    public Region build();
}
```

**Example:**

```java
Region layout = new TwoColumnLayout()
    .withSidebar(chatSidebar)
    .withContent(chatContent)
    .withSidebarWidth(280)
    .build();
```

### ThreeColumnLayout

Layout với 3 cột: left, center, right.

```java
public class ThreeColumnLayout extends HBox {
    public ThreeColumnLayout();
    public ThreeColumnLayout withLeft(Node left);
    public ThreeColumnLayout withCenter(Node center);
    public ThreeColumnLayout withRight(Node right);
    public ThreeColumnLayout withSpacing(double spacing);
    public Region build();
}
```

### HeaderContentLayout

Layout với header và content.

```java
public class HeaderContentLayout extends BorderPane {
    public HeaderContentLayout();
    public HeaderContentLayout withHeader(Node header);
    public HeaderContentLayout withContent(Node content);
    public HeaderContentLayout withFooter(Node footer);
    public Region build();
}
```

---

## Usage Examples

### Complete Form Example

```java
// Create form fields
ValidatedTextField nameField = new ValidatedTextField()
    .withPlaceholder("John Doe")
    .withMinLength(2);

ValidatedTextField emailField = new ValidatedTextField()
    .withPattern("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}")
    .withPlaceholder("john@example.com");

PasswordField passwordField = new PasswordField();

// Build form
Form userForm = new FormBuilder()
    .addSection("Account Information")
    .addField("Full Name", nameField)
    .addField("Email", emailField)
    .addField("Password", passwordField)
    .withSpacing(16)
    .build();

// Create action buttons
HBox actions = new HBox(12,
    new PrimaryButton("Create Account", this::createAccount),
    new SecondaryButton("Cancel", this::cancel)
);

// Wrap in card
Card formCard = new Card()
    .withTitle("Create New Account")
    .withContent(userForm)
    .withFooter(actions);
```

### Complete Page Example

```java
public class UserManagementPage extends BaseView {
    
    @Override
    protected Node createMainContent() {
        // Search and filter
        HBox toolbar = new HBox(12,
            new SearchBox("Search users..."),
            new SecondaryButton("Filter", Octicons.FILTER_16),
            new Separator(Orientation.VERTICAL),
            new PrimaryButton("Add User", Octicons.PLUS_16)
        );
        
        // User list
        VBox userList = new VBox(8);
        users.forEach(user -> {
            AvatarListItem item = new AvatarListItem(user.getInitials(), user.getName())
                .withSubtitle(user.getEmail())
                .withStatus(user.isOnline() ? Status.ONLINE : Status.OFFLINE)
                .withAction(() -> showUserDetails(user));
            userList.getChildren().add(item);
        });
        
        ScrollPane scrollPane = new ScrollPane(userList);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Layout
        return new VBox(20, toolbar, scrollPane);
    }
}
```

---

## Component Catalog

| Category       | Component              | Status     | Priority |
|----------------|------------------------|------------|----------|
| **Buttons**    |
|                | IconButton             | 🔄 Planned | High     |
|                | PrimaryButton          | 🔄 Planned | High     |
|                | SecondaryButton        | 🔄 Planned | High     |
|                | ButtonBuilder          | 🔄 Planned | Medium   |
| **Cards**      |
|                | Card                   | 🔄 Planned | High     |
|                | StatCard               | 🔄 Planned | High     |
|                | InfoCard               | 🔄 Planned | Medium   |
| **Forms**      |
|                | FormField              | 🔄 Planned | High     |
|                | ValidatedTextField     | 🔄 Planned | High     |
|                | FormBuilder            | 🔄 Planned | High     |
| **Dialogs**    |
|                | DialogBuilder          | 🔄 Planned | High     |
|                | ConfirmDialog          | 🔄 Planned | High     |
|                | InfoDialog             | 🔄 Planned | Medium   |
| **Lists**      |
|                | ListItem               | 🔄 Planned | High     |
|                | AvatarListItem         | 🔄 Planned | Medium   |
|                | IconListItem           | 🔄 Planned | Medium   |
| **Navigation** |
|                | NavigationBar          | 🔄 Planned | Medium   |
|                | Breadcrumb             | 🔄 Planned | Medium   |
|                | AppHeader              | 🔄 Planned | High     |
|                | SidebarView            | ✅ Exists   | High     |
| **Text**       |
|                | UniversalTextComponent | ✅ Exists   | High     |
| **Widgets**    |
|                | SearchBox              | 🔄 Planned | High     |
|                | LoadingIndicator       | 🔄 Planned | High     |
|                | StatusBadge            | 🔄 Planned | Medium   |
|                | ThemeToggle            | 🔄 Planned | Low      |
| **Layouts**    |
|                | TwoColumnLayout        | 🔄 Planned | High     |
|                | ThreeColumnLayout      | 🔄 Planned | Low      |
|                | HeaderContentLayout    | 🔄 Planned | Medium   |

**Status Legend:**

- ✅ Exists: Component đã tồn tại
- 🔄 Planned: Component sẽ được tạo trong refactoring
- ⏸️ Deferred: Tạm hoãn

---

## Contributing

### Adding New Components

1. Create component class trong appropriate package
2. Extend từ JavaFX component (Button, VBox, etc.)
3. Follow naming convention: `XxxComponent` or `XxxWidget`
4. Add builder methods (fluent API)
5. Add documentation và examples
6. Write tests
7. Update this catalog

### Component Checklist

- [ ] Extends appropriate JavaFX class
- [ ] Follows AtlantaFX styling
- [ ] Has builder/fluent API
- [ ] Supports customization
- [ ] Accessible (keyboard, screen reader)
- [ ] Responsive
- [ ] Documented với examples
- [ ] Unit tested
- [ ] Added to catalog

---

**Last Updated**: 2025-11-15  
**Version**: 2.0.0

