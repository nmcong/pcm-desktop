# 🎨 UI/UX Development

Tài liệu về phát triển giao diện người dùng và trải nghiệm người dùng.

## 📚 Tài Liệu

- **[ATLANTAFX_REFACTOR.md](ATLANTAFX_REFACTOR.md)** - Refactor với AtlantaFX
    - Migration từ JavaFX cơ bản sang AtlantaFX
    - Theme system
    - Components overview
    - Best practices

- **[IKONLI_INTEGRATION.md](IKONLI_INTEGRATION.md)** - Tích hợp Ikonli Icons
    - Icon packs available
    - Usage examples
    - Custom icons
    - Performance tips

- **[PHASE_2_UI_INTEGRATION_COMPLETE.md](PHASE_2_UI_INTEGRATION_COMPLETE.md)** - Hoàn thành Phase 2
    - UI components implemented
    - Integration with backend
    - User feedback

- **[PHASE_2_FINAL_SUMMARY.md](PHASE_2_FINAL_SUMMARY.md)** - Tóm tắt Phase 2
    - Complete feature list
    - Screenshots
    - Metrics
    - Next steps

## 🎨 UI Stack

### Technologies

```
┌─────────────────────────────────────┐
│         JavaFX 21                   │
│  - Core UI framework                │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│         AtlantaFX                   │
│  - Modern theme system              │
│  - Beautiful components             │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│         Ikonli                      │
│  - Icon library                     │
│  - Font icons                       │
└─────────────────────────────────────┘
```

### Design System

#### Colors

- **Primary**: `#6366f1` (Indigo)
- **Secondary**: `#8b5cf6` (Purple)
- **Success**: `#10b981` (Green)
- **Danger**: `#ef4444` (Red)
- **Warning**: `#f59e0b` (Amber)

#### Dark Theme Colors

- **Background Primary**: `#1a1d2e`
- **Background Secondary**: `#16192a`
- **Background Tertiary**: `#12151f`
- **Text Primary**: `#f8fafc`
- **Text Secondary**: `#cbd5e1`
- **Border**: `#2d3142`

## 🏗️ Component Architecture

### Pages

```
BasePage (Abstract)
  ├── DashboardPage
  ├── ProjectsPage
  ├── AIAssistantPage
  │   ├── ConversationListView
  │   ├── ChatMessagesArea
  │   └── ChatInputArea
  ├── SettingsPage
  └── ...
```

### Layouts

- **MainView** - Root layout with navigation
- **SideBar** - Navigation menu
- **ContentArea** - Main content area
- **StatusBar** - Bottom status bar

## 🚀 Features

### ✅ Implemented

#### Phase 1

- ✅ Basic layout structure
- ✅ Navigation system
- ✅ Theme switching
- ✅ Basic components

#### Phase 2

- ✅ AtlantaFX integration
- ✅ Ikonli icons
- ✅ AI Assistant Page UI
- ✅ Dark theme
- ✅ Responsive design
- ✅ Animations

### 🚧 In Progress

- 🚧 Settings page enhancement
- 🚧 Dashboard widgets
- 🚧 Advanced charts
- 🚧 Keyboard shortcuts

### 📋 Planned

- 📋 Multi-window support
- 📋 Drag & drop
- 📋 Advanced animations
- 📋 Accessibility improvements

## 💡 Quick Examples

### Using AtlantaFX Theme

```java
Application.setUserAgentStylesheet(
    new PrimerDark().getUserAgentStylesheet()
);
```

### Using Ikonli Icons

```java
FontIcon icon = new FontIcon(Feather.MESSAGE_CIRCLE);
icon.setIconSize(24);
icon.setIconColor(Color.WHITE);
```

### Creating Custom Component

```java
public class CustomButton extends Button {
    public CustomButton(String text) {
        super(text);
        getStyleClass().add("custom-button");
    }
}
```

## 🎨 Styling

### CSS Files

- `styles.css` - Main application styles
- `ai-assistant-dark.css` - AI Assistant dark theme
- Custom component styles

### CSS Variables

```css
:root {
    --primary-color: #6366f1;
    --secondary-color: #8b5cf6;
    --background-color: #1a1d2e;
    --text-color: #f8fafc;
}
```

## 📱 Responsive Design

### Breakpoints

- **Small**: < 600px
- **Medium**: 600px - 1200px
- **Large**: > 1200px

### Adaptive Layouts

```java
if (width < 600) {
    // Mobile layout
} else if (width < 1200) {
    // Tablet layout
} else {
    // Desktop layout
}
```

## 🔗 Related Documentation

- [AI Assistant Development](../ai-assistant/)
- [Project Summary](../PROJECT_SUMMARY.md)
- [Setup Guides](../../setup/)

## 📞 Resources

### AtlantaFX

- [Documentation](https://mkpaz.github.io/atlantafx/)
- [Sampler](https://github.com/mkpaz/atlantafx/tree/master/sampler)

### Ikonli

- [Documentation](https://kordamp.org/ikonli/)
- [Available Icon Packs](https://kordamp.org/ikonli/cheat-sheet.html)

### JavaFX

- [Official Docs](https://openjfx.io/)
- [CSS Reference](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/doc-files/cssref.html)

---

**Status**: ✅ Phase 2 Complete, 🚧 Phase 3 In Progress  
**Updated**: 12/11/2025

