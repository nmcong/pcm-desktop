# Knowledge Base Module

## 📋 Tổng Quan

Module **Knowledge Base** là hệ thống internal StackOverflow-like cho team knowledge sharing. Cung cấp nền tảng để lưu
trữ, tìm kiếm và chia sẻ kiến thức, giải pháp cho các vấn đề thường gặp trong dự án.

### ✨ Features

- 📚 **Knowledge Items**: Problem-solution pairs với markdown support
- 🏷️ **Categories**: Phân loại knowledge items
- 🔍 **Search & Filter**: Tìm kiếm và lọc theo category
- ⭐ **Priority System**: Đánh dấu important items
- 📝 **Markdown Support**: Rich text cho problem và solution
- 🎨 **Modern UI**: Card-based layout với hover effects
- 📱 **Fully Responsive**: Tối ưu cho mọi devices

### 📊 Thống Kê

- **Total Files**: 5 files
- **Page**: 1 file (581 lines)
- **Components**: 3 files (755 lines total)
    - KnowledgeBaseModal: 248 lines
    - CategoryManagementModal: 332 lines
    - KnowledgeBaseDetailModal: 175 lines
- **Styles**: 1 file (755 lines)
- **Total**: ~2,091 lines

---

## 📂 Cấu Trúc Module

```
knowledge-base/
├── pages/                              # Page components
│   └── KnowledgeBasePage.js           # Main page (581 lines) ✅
├── components/                         # Modal components
│   ├── KnowledgeBaseModal.js          # Create/Edit modal (248 lines) ✅
│   ├── CategoryManagementModal.js     # Category CRUD (332 lines) ✅
│   └── KnowledgeBaseDetailModal.js    # Detail view modal (175 lines) ✅
├── styles/                             # Module-specific styles
│   └── knowledge-base.css             # All styles (755 lines) ✅
└── README.md                           # This file
```

---

## ⚠️ Status

### ✅ Completed

- **Module Structure**: Created folder structure ✅
- **File Migration**: All files moved to module ✅
    - KnowledgeBasePage.js → pages/
    - 3 modal components → components/
    - knowledge-base.css → styles/
- **Import Path Updates**: All imports fixed ✅
- **CSS Integration**: Updated in index.html ✅
- **Export Updates**: Updated pages/index.js ✅
- **Documentation**: Complete README ✅

### ✨ Migration Complete!

**All knowledge base code đã được organized thành công!**

---

## 🔧 Components API

### KnowledgeBasePage

Main page component cho knowledge base system.

**Features:**

- List view với search và filter
- Category management
- Create/Edit/Delete knowledge items
- Priority sorting
- Responsive grid layout

**Methods:**

- `initialize()` - Setup page và load data
- `loadData()` - Load categories và items
- `renderGrid()` - Render knowledge items grid
- `handleSearch()` - Search functionality
- `handleFilter()` - Category filtering
- `showCreateModal()` - Open create modal
- `showDetailModal()` - Open detail modal
- `showCategoryModal()` - Open category management

### KnowledgeBaseModal

Modal for creating/editing knowledge items.

**Constructor:**

```javascript
constructor((item = null), (categories = []), onSuccess);
```

**Parameters:**

- `item` (Object|null): Item to edit, null for create
- `categories` (Array): Available categories
- `onSuccess` (Function): Callback on successful save

**Features:**

- Problem & solution fields với markdown
- Category selection
- Priority toggle
- Tags input
- Form validation

### CategoryManagementModal

Modal for managing categories.

**Constructor:**

```javascript
constructor(onUpdate);
```

**Features:**

- List all categories
- Create new categories
- Edit existing categories
- Delete categories (with validation)
- Color picker for categories

### KnowledgeBaseDetailModal

Modal for viewing full item details.

**Constructor:**

```javascript
constructor(item, categories, onUpdate);
```

**Features:**

- Markdown rendering for problem/solution
- Metadata display (created, updated, priority)
- Tags display
- Edit và delete actions
- Full-screen modal

---

## 💻 Usage

### Basic Integration

```javascript
import { KnowledgeBasePage } from "./modules/knowledge-base/pages/KnowledgeBasePage.js";

// In PageManager or routing
const page = new KnowledgeBasePage();
await page.initialize();
```

### Using Modals

```javascript
import { CategoryManagementModal } from "./modules/knowledge-base/components/CategoryManagementModal.js";
import { KnowledgeBaseModal } from "./modules/knowledge-base/components/KnowledgeBaseModal.js";

// Create new item
const createModal = new KnowledgeBaseModal(null, categories, (savedItem) => {
  console.log("Item created:", savedItem);
  loadData();
});
createModal.show();

// Edit existing item
const editModal = new KnowledgeBaseModal(item, categories, (updatedItem) => {
  console.log("Item updated:", updatedItem);
  loadData();
});
editModal.show();

// Manage categories
const categoryModal = new CategoryManagementModal(() => {
  loadCategories();
});
categoryModal.show();
```

---

## 🎨 Styles

### Key CSS Classes

**Page Layout:**

- `.knowledge-base-page` - Main page container
- `.knowledge-base-header` - Page header với actions
- `.knowledge-base-controls` - Search và filter controls
- `.knowledge-base-grid` - Items grid layout

**Item Cards:**

- `.kb-item-card` - Individual knowledge item card
- `.kb-item-header` - Card header với title và actions
- `.kb-item-body` - Problem/solution preview
- `.kb-item-footer` - Metadata (date, category, tags)
- `.kb-item-priority` - Priority indicator
- `.kb-item-badge` - Category badge

**Modals:**

- `.kb-modal` - Base modal class
- `.kb-form-group` - Form field group
- `.kb-category-list` - Category management list
- `.kb-detail-content` - Detail modal content

### Theming

```css
/* Primary colors */
.kb-item-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
}

/* Priority colors */
.kb-item-priority.high {
  background: var(--error-color);
}

.kb-item-priority.medium {
  background: var(--warning-color);
}

/* Category badges - dynamic colors */
.kb-item-badge {
  background: var(--category-color);
}
```

---

## 📱 Responsive Design

### Breakpoints

**Desktop (> 1200px):**

- Grid: 3 columns
- Full sidebar navigation

**Tablet (768px - 1200px):**

- Grid: 2 columns
- Collapsed sidebar

**Mobile (< 768px):**

- Grid: 1 column
- Bottom navigation
- Stacked modals

---

## 🗄️ Database Schema

### Knowledge Items

```javascript
{
  id: number,
  title: string,
  problem: string,           // Markdown
  solution: string,          // Markdown
  category_id: number,
  priority: 'low' | 'medium' | 'high',
  tags: string[],
  created_at: timestamp,
  updated_at: timestamp,
  created_by: string
}
```

### Categories

```javascript
{
  id: number,
  name: string,
  color: string,             // Hex color
  icon: string,              // Icon name
  description: string,
  item_count: number,
  created_at: timestamp
}
```

---

## 🧪 Testing Checklist

### Functional Tests

- [ ] List view hiển thị đúng items
- [ ] Search functionality working
- [ ] Category filter working
- [ ] Create new item successful
- [ ] Edit item successful
- [ ] Delete item với confirmation
- [ ] Priority sorting correct
- [ ] Category management working

### UI/UX Tests

- [ ] Grid layout responsive
- [ ] Card hover effects smooth
- [ ] Modals open/close properly
- [ ] Markdown rendering correct
- [ ] Form validation working
- [ ] Loading states clear
- [ ] Error messages helpful

---

## 📚 Related Modules

Knowledge Base là một standalone module nhưng integrate với:

- **DatabaseManager**: Data persistence
- **EventBus**: Real-time updates
- **MarkdownRenderer**: Content rendering
- **UI Components**: Modal, PageHeader

---

## 🔄 Migration Notes

### What Was Moved

**From:** `public/js/pages/KnowledgeBasePage.js`  
**To:** `public/js/modules/knowledge-base/pages/KnowledgeBasePage.js`

**From:** `public/js/components/knowledge-base/*.js`  
**To:** `public/js/modules/knowledge-base/components/*.js`

**From:** `public/css/knowledge-base.css`  
**To:** `public/js/modules/knowledge-base/styles/knowledge-base.css`

### Import Path Changes

**Before:**

```javascript
import { KnowledgeBaseModal } from "./components/knowledge-base/KnowledgeBaseModal.js";
import { KnowledgeBasePage } from "./pages/KnowledgeBasePage.js";
```

**After:**

```javascript
import { KnowledgeBaseModal } from "./modules/knowledge-base/components/KnowledgeBaseModal.js";
import { KnowledgeBasePage } from "./modules/knowledge-base/pages/KnowledgeBasePage.js";
```

### Breaking Changes

None - All imports updated automatically. Module is backward compatible through `pages/index.js` export.

---

## 📖 Future Enhancements

### Potential Features

1. **Advanced Search**:
    - Full-text search
    - Search by tags
    - Date range filter

2. **Collaboration**:
    - Comments system
    - Upvote/downvote
    - Follow items for updates

3. **Export/Import**:
    - Export to PDF
    - Export to Markdown
    - Import from external sources

4. **Analytics**:
    - View counts
    - Most helpful items
    - Search analytics

5. **Attachments**:
    - File attachments
    - Images in solutions
    - Code snippets with syntax highlighting

---

## 🤝 Contributing

When modifying this module:

1. **Maintain structure** - Keep pages, components, styles separated
2. **Update docs** - Keep README in sync với changes
3. **Test thoroughly** - Check all modal interactions
4. **Preserve API** - Don't break existing integrations
5. **Follow conventions** - Consistent naming và formatting

---

**Maintained by:** PCM WebApp Team  
**Module Type:** Page + Components  
**Dependencies:** DatabaseManager, EventBus, MarkdownRenderer, Modal, PageHeader  
**Last Updated:** November 9, 2024
