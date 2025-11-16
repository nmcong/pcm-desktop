# UI Module Documentation

Complete guide for PCM Desktop UI module.

---

## 📚 Documentation

### [UI_GUIDE.md](./UI_GUIDE.md) - Complete UI Reference (1070 lines)

Comprehensive guide covering:

1. **Architecture Overview** - MVVM pattern, package structure
2. **Base Classes** - BaseView, ViewLifecycle, EnhancedBaseViewModel
3. **Component Library** - 17 reusable components
   - Buttons (3)
   - Cards (3)
   - Lists (3)
   - Forms (1)
   - Dialogs (1)
   - AI Components (4)
4. **Constants & Styling** - StyleConstants, LayoutConstants, ColorConstants
5. **Utilities** - UIFactory, LayoutHelper, DialogHelper
6. **Creating Pages** - Step-by-step guide
7. **Best Practices** - Do's and don'ts
8. **Examples** - Real-world usage examples

---

## 🚀 Quick Start

### 1. Create a New Page

```java
public class MyPage extends BaseView {
    public MyPage() {
        super("My Page", "Description", new FontIcon(Feather.HOME));
    }
    
    @Override
    protected Node createMainContent() {
        VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
        // Build your UI here
        return content;
    }
}
```

### 2. Use Components

```java
// Buttons
Button btn = UIFactory.createPrimaryButton("Save", this::save);

// Cards
VBox card = UIFactory.createCard();

// Dialogs
DialogBuilder.confirm()
    .title("Delete")
    .content("Are you sure?")
    .onConfirm(this::delete)
    .show();
```

### 3. Use Constants

```java
// Layout
VBox box = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
box.setPadding(LayoutConstants.PADDING_DEFAULT);

// Styling
label.getStyleClass().add(StyleConstants.TEXT_MUTED);
```

---

## 📦 Component Library

### Available Components (17 total)

| Category | Components | Count |
|----------|-----------|-------|
| **Buttons** | IconButton, PrimaryButton, SecondaryButton | 3 |
| **Cards** | Card, StatCard, InfoCard | 3 |
| **Lists** | ListItem, AvatarListItem, IconListItem | 3 |
| **Forms** | ValidatedTextField | 1 |
| **Dialogs** | DialogBuilder | 1 |
| **AI** | ChatSidebar, ConversationItem, ChatInputArea, ChatMessageList | 4 |
| **Text** | UniversalTextComponent, TextRenderer, etc. | 6 |

---

## 🎨 Architecture

```
View (BaseView)
  ↓ binds to
ViewModel (Properties)
  ↓ uses
Model (Domain)
```

**Key Principles:**
- ✅ MVVM Pattern
- ✅ SOLID Principles
- ✅ Component-based
- ✅ Template Method
- ✅ Dependency Injection

---

## 📖 Resources

- **Main Guide:** [UI_GUIDE.md](./UI_GUIDE.md)
- **JavaFX:** https://openjfx.io/
- **AtlantaFX:** https://github.com/mkpaz/atlantafx

---

*Last Updated: November 16, 2025*

