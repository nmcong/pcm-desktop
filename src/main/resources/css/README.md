# PCM Desktop CSS Architecture

## 📁 File Structure

```
src/main/resources/css/
├── styles.css          # Base stylesheet (theme-independent)
├── theme-light.css     # Light theme colors
├── theme-dark.css      # Dark theme colors
└── README.md          # This file
```

## 🎨 CSS Loading Order

1. **AtlantaFX Base Theme** (via `Application.setUserAgentStylesheet()`)
2. **`styles.css`** - Base structure and layout
3. **`theme-light.css`** OR **`theme-dark.css`** - Theme colors

```java
// In ThemeManager.java
mainScene.getStylesheets().clear();
mainScene.getStylesheets().add("/css/styles.css");
mainScene.getStylesheets().add(isDark ? "/css/theme-dark.css" : "/css/theme-light.css");
```

## 📋 File Responsibilities

### `styles.css` - Base Stylesheet
**Contains:** Theme-independent styles
- ✅ Layout structure (navbar, sidebar, content)
- ✅ Component structure (buttons, cards, inputs)
- ✅ Scrollbar structure
- ✅ Utility classes
- ❌ **NO colors or theme-specific values**

### `theme-light.css` - Light Theme
**Contains:** Light theme color overrides
- Color definitions (`-fx-bg-primary`, `-fx-text-primary`, etc.)
- JavaFX system variables override
- Light-specific shadows and effects
- Light hover/active states

### `theme-dark.css` - Dark Theme
**Contains:** Dark theme color overrides
- Color definitions for dark palette
- JavaFX system variables override
- Dark-specific shadows and glow effects
- Dark hover/active states

## 🎯 Color Variable Naming

### Background Colors
```css
-fx-bg-primary      /* Main background (lightest/darkest) */
-fx-bg-secondary    /* Secondary background */
-fx-bg-tertiary     /* Tertiary background */
-fx-bg-elevated     /* Elevated/modal backgrounds */
```

### Text Colors
```css
-fx-text-primary    /* Main text */
-fx-text-secondary  /* Secondary text */
-fx-text-muted      /* Muted/hint text */
-fx-text-disabled   /* Disabled text */
```

### Border Colors
```css
-fx-border-primary    /* Main border color */
-fx-border-secondary  /* Secondary border color */
```

### Accent Colors
```css
-fx-accent-primary    /* Primary accent (#6366f1) */
-fx-accent-secondary  /* Secondary accent (#8b5cf6) */
-fx-accent-hover      /* Hover state color */
```

### Special Colors
```css
-fx-user-bubble    /* User message bubble */
-fx-ai-bubble      /* AI message bubble */
-fx-success        /* Success/green */
-fx-warning        /* Warning/orange */
-fx-error          /* Error/red */
```

## 🔧 How to Add a New Theme

1. **Create theme file** (e.g., `theme-nord.css`)
```bash
cp theme-light.css theme-nord.css
```

2. **Update colors** in SECTION 1 & 2 of the new file

3. **Update `AppConstants.java`**
```java
public static final String CSS_THEME_NORD = "/css/theme-nord.css";
```

4. **Update `ThemeManager.java`**
```java
String themePath = switch(themeType) {
    case LIGHT -> AppConstants.CSS_THEME_LIGHT;
    case DARK -> AppConstants.CSS_THEME_DARK;
    case NORD -> AppConstants.CSS_THEME_NORD;
};
```

## ⚠️ Important Rules

### 1. **No CSS Variables** (JavaFX limitation)
❌ **Don't use:**
```css
.root {
    --my-color: #ff0000;
}
.button {
    -fx-background-color: var(--my-color);
}
```

✅ **Use directly:**
```css
.root {
    -fx-my-color: #ff0000;
}
.button {
    -fx-background-color: -fx-my-color;
}
```

### 2. **Use `.root` not `:root`**
❌ **Don't use:**
```css
:root {
    -fx-bg-primary: #ffffff;
}
```

✅ **Use:**
```css
.root {
    -fx-bg-primary: #ffffff;
}
```

### 3. **Structure vs Colors**
- **Structure** (layout, sizes, borders) → `styles.css`
- **Colors** (backgrounds, text, effects) → theme files

### 4. **Avoid Hardcoded Colors in styles.css**
❌ **Don't:**
```css
/* In styles.css */
.button {
    -fx-background-color: #6366f1;  /* Hardcoded */
}
```

✅ **Do:**
```css
/* In styles.css */
.button {
    -fx-background-color: -fx-accent-primary;  /* Use variable */
}

/* In theme-light.css & theme-dark.css */
.root {
    -fx-accent-primary: #6366f1;
}
```

## 📊 File Statistics

| File | Lines | Purpose |
|------|-------|---------|
| `styles.css` | ~680 | Structure & layout |
| `theme-light.css` | ~330 | Light theme |
| `theme-dark.css` | ~330 | Dark theme |

## 🎨 Color Palettes

### Light Theme
```
Backgrounds:  #ffffff, #f8f9fa, #f1f3f5
Text:         #1f2937, #374151, #6b7280
Borders:      #e5e7eb, #d1d5db
Accents:      #6366f1 (Indigo)
```

### Dark Theme
```
Backgrounds:  #1a1d2e, #16192a, #12151f
Text:         #f8fafc, #cbd5e1, #64748b
Borders:      #2d3142, #1f2335
Accents:      #6366f1 (Indigo)
```

## 🐛 Troubleshooting

### Problem: Colors not showing
**Solution:** Ensure theme CSS loaded AFTER styles.css

### Problem: Theme not switching
**Solution:** Check `ThemeManager.applyThemeToScene()` clears and re-adds CSS

### Problem: Style looks broken
**Solution:** Verify JavaFX CSS syntax (`.root`, no `var()`)

## 📚 Resources

- [JavaFX CSS Reference](https://openjfx.io/javadoc/17/javafx.graphics/javafx/scene/doc-files/cssref.html)
- [AtlantaFX Documentation](https://mkpaz.github.io/atlantafx/)
- Project CSS Guide: `/docs/` (when available)

---

**Last Updated:** 2024-11-15
**Maintainer:** PCM Desktop Team

