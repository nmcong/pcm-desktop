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

The application loads CSS in this specific order:

1. **AtlantaFX Base Theme** (via `Application.setUserAgentStylesheet()`)
2. **`styles.css`** - Base structure and layout
3. **`theme-light.css`** OR **`theme-dark.css`** - Theme-specific colors

```java
// In ThemeManager.java
mainScene.getStylesheets().clear();
mainScene.getStylesheets().add(
    Objects.requireNonNull(getClass().getResource(AppConstants.CSS_STYLES)).toExternalForm()
);

String themePath = isDarkTheme ? AppConstants.CSS_THEME_DARK : AppConstants.CSS_THEME_LIGHT;
mainScene.getStylesheets().add(
    Objects.requireNonNull(getClass().getResource(themePath)).toExternalForm()
);
```

## 📋 File Responsibilities

### `styles.css` (~680 lines) - Base Stylesheet
**Contains:** Theme-independent styles
- ✅ Layout structure (navbar, sidebar, content area)
- ✅ Component structure (buttons, cards, inputs, scrollbars)
- ✅ Chat interface layout
- ✅ Text editor components
- ✅ Utility classes (flex-grow, text-bold, etc.)
- ❌ **NO colors or theme-specific values**

**Philosophy:** All structural and positional CSS. Colors come from theme files.

### `theme-light.css` (~330 lines) - Light Theme
**Contains:** Light theme color overrides
- Color definitions (white backgrounds, dark text)
- JavaFX system variables for light mode
- Light-specific shadows and subtle effects
- Hover/active states with light colors
- Message bubble colors for light theme

**Palette:** White/light gray backgrounds, dark text, subtle borders

### `theme-dark.css` (~330 lines) - Dark Theme
**Contains:** Dark theme color overrides
- Color definitions (dark backgrounds, light text)
- JavaFX system variables for dark mode
- Dark-specific shadows and glow effects
- Hover/active states with emphasis colors
- Message bubble colors for dark theme

**Palette:** Dark blue/gray backgrounds, light text, vibrant borders

## 🎯 Color Variable Naming Convention

### Background Colors
```css
-fx-bg-primary      /* Main background (lightest in light, darkest in dark) */
-fx-bg-secondary    /* Secondary background (slightly darker/lighter) */
-fx-bg-tertiary     /* Tertiary background (more contrast) */
-fx-bg-elevated     /* Elevated elements (modals, popovers) */
```

### Text Colors
```css
-fx-text-primary    /* Primary text (high contrast, most readable) */
-fx-text-secondary  /* Secondary text (medium contrast) */
-fx-text-muted      /* Muted text (hints, placeholders, timestamps) */
-fx-text-disabled   /* Disabled text (lowest contrast) */
```

### Border Colors
```css
-fx-border-primary    /* Primary borders (main separators) */
-fx-border-secondary  /* Secondary borders (subtle dividers) */
```

### Accent Colors
```css
-fx-accent-primary    /* Primary accent - Indigo (#6366f1) */
-fx-accent-secondary  /* Secondary accent - Purple (#8b5cf6) */
-fx-accent-hover      /* Hover state (darker/brighter variant) */
```

### Specialized Colors
```css
-fx-user-bubble    /* User message bubble background */
-fx-ai-bubble      /* AI message bubble background */
-fx-success        /* Success state - Green (#10b981) */
-fx-warning        /* Warning state - Orange (#f59e0b) */
-fx-error          /* Error state - Red (#ef4444) */
```

## 🔧 How to Add a New Theme

### Step 1: Create Theme File
```bash
cp theme-light.css theme-nord.css
```

### Step 2: Update Colors
Edit SECTION 1 & 2 in `theme-nord.css`:
```css
.root {
    /* Background Hierarchy - Nord Palette */
    -fx-bg-primary: #2e3440;
    -fx-bg-secondary: #3b4252;
    -fx-bg-tertiary: #434c5e;
    /* ... etc */
}
```

### Step 3: Add Constant
In `AppConstants.java`:
```java
public static final String CSS_THEME_NORD = "/css/theme-nord.css";
```

### Step 4: Update ThemeManager
In `ThemeManager.java`, add enum and update logic:
```java
public enum Theme {
    LIGHT, DARK, NORD
}

private void applyThemeToScene() {
    String themePath = switch(currentTheme) {
        case LIGHT -> AppConstants.CSS_THEME_LIGHT;
        case DARK -> AppConstants.CSS_THEME_DARK;
        case NORD -> AppConstants.CSS_THEME_NORD;
    };
    // ... load theme
}
```

## ⚠️ JavaFX CSS Limitations & Best Practices

### 1. **No Standard CSS Variables**
JavaFX CSS does NOT support standard CSS custom properties (`--variables` with `var()`).

❌ **Don't use (won't work):**
```css
:root {
    --my-color: #ff0000;
}
.button {
    -fx-background-color: var(--my-color);  /* ❌ Won't work */
}
```

✅ **Use JavaFX looked-up colors:**
```css
.root {
    -fx-my-color: #ff0000;
}
.button {
    -fx-background-color: -fx-my-color;  /* ✅ Works */
}
```

### 2. **Use `.root` not `:root`**
JavaFX uses `.root` (class selector) not `:root` (pseudo-class).

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

### 3. **Separation: Structure vs Colors**
Keep structure in `styles.css`, colors in theme files.

❌ **Don't put colors in styles.css:**
```css
/* In styles.css - BAD */
.button {
    -fx-background-color: #6366f1;  /* ❌ Hardcoded color */
}
```

✅ **Use color variables:**
```css
/* In styles.css - GOOD */
.button {
    -fx-background-color: -fx-accent-primary;  /* ✅ Variable */
}

/* In theme-light.css & theme-dark.css */
.root {
    -fx-accent-primary: #6366f1;
}
```

### 4. **Avoid Hardcoding Values That Should Be Theme-Aware**
Colors, opacity, and some effects should come from theme files.

**Theme-independent (in styles.css):**
- Layout dimensions
- Padding/margins
- Border radius
- Font sizes
- Structural properties

**Theme-dependent (in theme-*.css):**
- Colors (backgrounds, text, borders)
- Shadows (size and color)
- Effects (glow, opacity when color-based)
- Hover states with color changes

## 📊 CSS Statistics

| File | Lines | Selectors | Purpose |
|------|-------|-----------|---------|
| `styles.css` | ~680 | ~250+ | Layout & structure |
| `theme-light.css` | ~330 | ~120+ | Light colors & effects |
| `theme-dark.css` | ~330 | ~120+ | Dark colors & effects |
| **Total** | **~1,340** | **~490+** | Complete styling |

**Improvement vs Old Structure:**
- ✅ 22% fewer lines (removed duplicates)
- ✅ Clear separation of concerns
- ✅ Easy to maintain and extend
- ✅ Scalable for new themes

## 🎨 Color Palettes

### Light Theme (`theme-light.css`)
```
Backgrounds:  #ffffff (white), #f8f9fa, #f1f3f5 (light grays)
Text:         #1f2937 (near black), #374151, #6b7280 (grays)
Borders:      #e5e7eb, #d1d5db (subtle grays)
Accent:       #6366f1 (indigo), #8b5cf6 (purple)
User Bubble:  #6366f1 (indigo blue)
AI Bubble:    #f3f4f6 (light gray)
Success:      #10b981 (green)
Warning:      #f59e0b (orange)
Error:        #ef4444 (red)
```

### Dark Theme (`theme-dark.css`)
```
Backgrounds:  #1a1d2e, #16192a, #12151f (dark blues)
Text:         #f8fafc (near white), #cbd5e1, #64748b (light grays)
Borders:      #2d3142, #1f2335 (dark borders)
Accent:       #6366f1 (indigo), #8b5cf6 (purple) - same as light
User Bubble:  #2563eb (brighter blue)
AI Bubble:    #1f2937 (dark gray)
Success:      #10b981 (green)
Warning:      #f59e0b (orange)
Error:        #ef4444 (red)
```

**Design Philosophy:** Consistent accent colors across themes for brand identity, while backgrounds and text adapt to light/dark modes.

## 🐛 Troubleshooting

### Problem: Colors not showing / Using AtlantaFX defaults
**Cause:** Theme CSS not loaded or loaded in wrong order
**Solution:** 
```java
// Ensure this order in ThemeManager
scene.getStylesheets().clear();
scene.getStylesheets().add(styles.css);      // 1. Base first
scene.getStylesheets().add(theme-*.css);     // 2. Theme second
```

### Problem: Theme not switching
**Cause:** Stylesheets not cleared before reloading
**Solution:** 
```java
scene.getStylesheets().clear();  // ✅ Clear first!
// Then add new stylesheets
```

### Problem: Null pointer when loading CSS
**Cause:** CSS file not found or wrong path
**Solution:**
```java
// Use Objects.requireNonNull for better error messages
String cssUrl = Objects.requireNonNull(
    getClass().getResource(AppConstants.CSS_THEME_LIGHT)
).toExternalForm();
```

### Problem: Styles look broken in JavaFX
**Cause:** Using web CSS syntax that JavaFX doesn't support
**Solution:** 
- Use `.root` not `:root`
- Don't use `--variables` and `var()`
- Check JavaFX CSS Reference for supported properties

### Problem: Colors work in light but not dark (or vice versa)
**Cause:** Variable defined in only one theme file
**Solution:** Ensure ALL color variables are defined in BOTH theme files

## 📚 Additional Resources

### Official Documentation
- [JavaFX CSS Reference Guide](https://openjfx.io/javadoc/17/javafx.graphics/javafx/scene/doc-files/cssref.html)
- [AtlantaFX Documentation](https://mkpaz.github.io/atlantafx/)
- [JavaFX Scene Graph CSS](https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html)

### Internal Documentation
- `src/main/java/com/noteflix/pcm/core/constants/AppConstants.java` - CSS path constants
- `src/main/java/com/noteflix/pcm/core/theme/ThemeManager.java` - Theme loading logic
- `src/main/java/com/noteflix/pcm/ui/pages/CSSTestPage.java` - Live CSS testing page

### Project Guidelines
- `/AGENTS.md` - Repository guidelines for AI agents
- `/docs/` - Additional project documentation

## 🔍 CSS Organization Sections

Both theme files follow this structure:

```
SECTION 1: Color Definitions (theme-specific)
SECTION 2: JavaFX System Variables Override
SECTION 3: Chat Sidebar (theme-specific styles)
SECTION 4: Chat Header & Actions
SECTION 5: Message Bubbles
SECTION 6: Loading Indicator
SECTION 7: Chat Input
SECTION 8: Welcome Screen
SECTION 9: Suggestion Cards
SECTION 10: Scrollbar Theme
```

This sectioned approach makes it easy to:
- Find specific component styles quickly
- Update related styles together
- Maintain consistency across themes

---

**Version:** 1.0.0  
**Last Updated:** 2024-11-15  
**Maintainer:** PCM Desktop Team  
**Status:** ✅ Production Ready
