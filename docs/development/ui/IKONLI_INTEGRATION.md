# Ikonli Icons Integration

## 📦 Overview

PCM Desktop now uses **Ikonli** - a comprehensive icon library for Java applications, replacing emoji icons with
professional vector icons from Material Design 2 and Feather icon sets.

## 🎨 Installed Icon Packs

| Pack                  | Version | Icons | Usage Prefix       |
|-----------------------|---------|-------|--------------------|
| **ikonli-core**       | 12.3.1  | -     | Core library       |
| **ikonli-javafx**     | 12.3.1  | -     | JavaFX integration |
| **Material Design 2** | 12.3.1  | 6000+ | `mdi2`             |
| **Feather**           | 12.3.1  | 280+  | `feather`          |

## 🚀 Usage

### In FXML

```xml
<?import org.kordamp.ikonli.javafx.FontIcon?>

<!-- Simple icon -->
<FontIcon iconLiteral="mdi2h-home" iconSize="20"/>

<!-- Icon with color -->
<FontIcon iconLiteral="mdi2s-star" iconSize="16" 
          style="-fx-icon-color: -color-warning-emphasis;"/>

<!-- Icon in Button -->
<Button styleClass="flat">
    <graphic>
        <FontIcon iconLiteral="mdi2c-cog-outline" iconSize="20"/>
    </graphic>
</Button>
```

### In Java

```java
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

// Create icon
FontIcon icon = new FontIcon("mdi2h-home");
icon.setIconSize(20);

// Or using enum
FontIcon icon2 = new FontIcon(Material2AL.HOME);
icon2.setIconSize(20);

// Set color
icon.setStyle("-fx-icon-color: -color-accent-emphasis;");
```

## 📊 Icons Used in PCM Desktop

### Navbar

- `mdi2p-plus-circle-outline` - New Screen button
- `mdi2b-bell-outline` - Notifications
- `mdi2c-cog-outline` - Settings

### Sidebar

- `mdi2s-star` - Favorites section
- `mdi2f-folder-multiple` - Projects section
- `mdi2p-plus` - New project button
- `mdi2c-cog-outline` - Settings

### Content Header

- `mdi2c-circle` - Status indicator
- `mdi2s-share-variant` - Share button
- `mdi2d-dots-vertical` - More menu
- `mdi2p-pencil` - Edit
- `mdi2c-content-copy` - Duplicate
- `mdi2d-download` - Export
- `mdi2d-delete` - Delete

### Stats Cards

- `mdi2l-lightning-bolt` - Events
- `mdi2f-file-code` - Source files
- `mdi2d-database` - Database tables
- `mdi2t-trending-up` - Trending up indicator
- `mdi2m-minus` - No change indicator
- `mdi2c-check-circle` - Success indicator

### Description Card

- `mdi2s-sparkles` - AI Enhance

### Related Items

- `mdi2m-monitor-screenshot` - Screen icon
- `mdi2a-arrow-right` - Navigate arrow

## 🎯 Icon Naming Convention

Material Design 2 icons follow this pattern:

```
mdi2[first-letter]-[icon-name]

Examples:
- mdi2h-home (h = home)
- mdi2s-star (s = star)
- mdi2c-cog (c = cog)
- mdi2f-folder (f = folder)
```

## 🔍 Finding Icons

### Material Design Icons

Browse at: https://materialdesignicons.com/

Search example:

- Home → `mdi2h-home`
- Settings → `mdi2c-cog` or `mdi2s-settings`
- Star → `mdi2s-star`

### In Code

```java
// List all Material Design icons
for (Material2AL icon : Material2AL.values()) {
    System.out.println(icon.getDescription());
}
```

## 🎨 Styling Icons

### Color

```xml
<!-- Using AtlantaFX color variables -->
<FontIcon iconLiteral="mdi2h-home" 
          style="-fx-icon-color: -color-accent-emphasis;"/>

<!-- Direct color -->
<FontIcon iconLiteral="mdi2s-star" 
          style="-fx-icon-color: #ff9800;"/>
```

### Size

```xml
<!-- Set size -->
<FontIcon iconLiteral="mdi2h-home" iconSize="16"/>  <!-- Small -->
<FontIcon iconLiteral="mdi2h-home" iconSize="20"/>  <!-- Medium -->
<FontIcon iconLiteral="mdi2h-home" iconSize="24"/>  <!-- Large -->
```

### In CSS

```css
.my-icon {
    -fx-icon-color: -color-accent-emphasis;
    -fx-icon-size: 20px;
}
```

## 📁 Library Files

Located in `lib/others/`:

- `ikonli-core-12.3.1.jar` (15 KB)
- `ikonli-javafx-12.3.1.jar` (37 KB)
- `ikonli-material2-pack-12.3.1.jar` (739 KB)
- `ikonli-feather-pack-12.3.1.jar` (55 KB)

Total: ~846 KB

## 🔄 Migration from Emoji

### Before (Emoji)

```xml
<Label text="⚙️"/>
<Label text="📁"/>
<Label text="⚡"/>
```

### After (Ikonli)

```xml
<FontIcon iconLiteral="mdi2c-cog-outline" iconSize="20"/>
<FontIcon iconLiteral="mdi2f-folder" iconSize="20"/>
<FontIcon iconLiteral="mdi2l-lightning-bolt" iconSize="20"/>
```

## 💡 Best Practices

### 1. **Use Semantic Colors**

```xml
<!-- Good: Uses AtlantaFX color variables -->
<FontIcon style="-fx-icon-color: -color-accent-emphasis;"/>

<!-- Avoid: Hard-coded colors -->
<FontIcon style="-fx-icon-color: blue;"/>
```

### 2. **Consistent Sizing**

- **Small icons**: 14-16px (in small buttons, inline)
- **Medium icons**: 18-20px (standard buttons, sidebar)
- **Large icons**: 24px+ (prominent actions)

### 3. **Button with Icon and Text**

```xml
<Button>
    <graphic>
        <HBox spacing="6" alignment="CENTER">
            <FontIcon iconLiteral="mdi2p-plus" iconSize="16"/>
            <Label text="New"/>
        </HBox>
    </graphic>
</Button>
```

### 4. **Icon-Only Buttons**

```xml
<Button styleClass="flat">
    <graphic>
        <FontIcon iconLiteral="mdi2c-cog" iconSize="20"/>
    </graphic>
    <tooltip>
        <Tooltip text="Settings"/>
    </tooltip>
</Button>
```

## 🆕 Adding New Icon Packs

To add more icon packs (e.g., FontAwesome):

1. Download from Maven Central:

```bash
curl -L -o lib/others/ikonli-fontawesome5-pack-12.3.1.jar \
  https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-fontawesome5-pack/12.3.1/ikonli-fontawesome5-pack-12.3.1.jar
```

2. Import in FXML:

```xml
<?import org.kordamp.ikonli.javafx.FontIcon?>
```

3. Use icons:

```xml
<FontIcon iconLiteral="fas-home" iconSize="20"/>
```

## 📊 Icon Coverage

### Current Coverage

- ✅ Navbar (100%)
- ✅ Sidebar (100%)
- ✅ Content Header (100%)
- ✅ Stats Cards (100%)
- ✅ Description Card (100%)
- ✅ Related Items (100%)
- ⏳ Properties Panel (partial - avatars still use labels)
- ⏳ Activity Panel (partial - avatars still use labels)
- ⏳ Tags Card (could add tag icons)

## 🎉 Benefits

### vs Emoji

✅ **Professional** - Vector icons, not emoji  
✅ **Scalable** - Perfect at any size  
✅ **Customizable** - Color, size, style  
✅ **Consistent** - Same style across platforms  
✅ **Semantic** - Named icons, not Unicode

### vs Image Files

✅ **Lightweight** - Icons bundled in JAR  
✅ **No resources** - No separate image files  
✅ **Dynamic** - Change color/size in code  
✅ **Theme-aware** - Use CSS variables

## 🔗 Resources

- **Ikonli GitHub**: https://github.com/kordamp/ikonli
- **Material Design Icons**: https://materialdesignicons.com/
- **Feather Icons**: https://feathericons.com/
- **Documentation**: https://kordamp.org/ikonli/

## ✅ Integration Checklist

- [x] Downloaded ikonli libraries (12.3.1)
- [x] Added Material Design 2 pack
- [x] Added Feather pack
- [x] Refactored Navbar with icons
- [x] Refactored Sidebar with icons
- [x] Refactored ContentHeader with icons
- [x] Refactored StatsCards with icons
- [x] Refactored DescriptionCard with icons
- [x] Refactored RelatedItemsCard with icons
- [x] Tested and verified
- [x] Documentation complete

## 🎊 Result

PCM Desktop now has:

- 🎨 Professional vector icons
- 📐 Consistent icon sizing
- 🎯 Semantic icon names
- 🌈 Theme-aware colors
- ⚡ Lightweight integration

No more emoji - pure professional icons! 🚀

