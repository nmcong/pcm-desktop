# Kế Hoạch Refactoring UI Module - PCM Desktop

> **Tác giả**: PCM Team  
> **Ngày tạo**: 2025-11-15  
> **Phiên bản**: 1.0.0  
> **Trạng thái**: Draft

## 📋 Mục Lục

1. [Tổng Quan](#1-tổng-quan)
2. [Phân Tích Hiện Trạng](#2-phân-tích-hiện-trạng)
3. [Vấn Đề Và Điểm Yếu](#3-vấn-đề-và-điểm-yếu)
4. [Mục Tiêu Refactoring](#4-mục-tiêu-refactoring)
5. [Kiến Trúc Mới](#5-kiến-trúc-mới)
6. [Kế Hoạch Thực Hiện](#6-kế-hoạch-thực-hiện)
7. [Migration Guide](#7-migration-guide)
8. [Testing Strategy](#8-testing-strategy)
9. [Checklist](#9-checklist)

---

## 1. Tổng Quan

### 1.1. Bối Cảnh

Module UI hiện tại của PCM Desktop đã được xây dựng với JavaFX và tuân theo các nguyên tắc Clean Architecture. Tuy nhiên, qua thời gian phát triển, cấu trúc code đã xuất hiện một số vấn đề về:

- **Tính nhất quán**: Cách tổ chức code không đồng nhất giữa các pages
- **Khả năng mở rộng**: Khó thêm features mới mà không ảnh hưởng code cũ
- **Tái sử dụng**: Nhiều đoạn code UI bị duplicate
- **Testability**: Khó viết unit tests cho UI components
- **Maintainability**: Khó maintain khi team mở rộng

### 1.2. Phạm Vi

Refactoring sẽ được áp dụng cho toàn bộ package `com.noteflix.pcm.ui`:

```
src/main/java/com/noteflix/pcm/ui/
├── components/          # UI components tái sử dụng
├── layout/             # Layout containers
├── pages/              # Các pages/views chính
├── viewmodel/          # ViewModels (MVVM pattern)
├── MainController.java # Main controller
└── MainView.java       # Main view
```

---

## 2. Phân Tích Hiện Trạng

### 2.1. Cấu Trúc Hiện Tại

#### 2.1.1. Package Structure

```
ui/
├── components/
│   ├── SidebarView.java              (344 lines) ⚠️
│   └── text/
│       ├── UniversalTextComponent.java
│       ├── TextContentType.java
│       ├── ViewMode.java
│       └── renderers/
│           ├── TextRenderer.java
│           ├── PlainTextRenderer.java
│           └── MarkdownRenderer.java.bak
├── layout/
│   └── MainLayer.java                (79 lines)
├── pages/
│   ├── BasePage.java                 (105 lines)
│   ├── AIAssistantPage.java          (806 lines) ⚠️⚠️⚠️
│   ├── KnowledgeBasePage.java
│   ├── DatabaseObjectsPage.java
│   ├── BatchJobsPage.java
│   ├── SettingsPage.java
│   ├── UniversalTextDemoPage.java
│   └── CSSTestPage.java
├── viewmodel/
│   ├── BaseViewModel.java            (95 lines)
│   ├── AIAssistantViewModel.java     (383 lines) ⚠️
│   ├── KnowledgeBaseViewModel.java
│   ├── DatabaseObjectsViewModel.java
│   ├── BatchJobsViewModel.java
│   └── SettingsViewModel.java
├── MainController.java               (97 lines)
└── MainView.java                     (515 lines) ⚠️
```

#### 2.1.2. Design Patterns Hiện Tại

1. **MVVM Pattern** (Partial)
   - ✅ ViewModels được tách riêng
   - ❌ Không phải tất cả Pages đều sử dụng ViewModel
   - ❌ Data binding chưa được tận dụng tối đa

2. **Template Method Pattern**
   - ✅ `BasePage` sử dụng template method
   - ✅ Pages kế thừa và override methods

3. **Dependency Injection** (Manual)
   - ⚠️ DI thủ công qua constructor
   - ❌ Không có DI container/framework

4. **Observer Pattern**
   - ✅ Sử dụng JavaFX Properties
   - ✅ ThemeChangeListener cho theme switching

5. **Factory Pattern**
   - ⚠️ Sử dụng factory methods trong một số trường hợp
   - ❌ Chưa có dedicated Factory classes

### 2.2. Luồng Dữ Liệu Hiện Tại

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   MainView  │────────▶│ PageNavigator│────────▶│   BasePage  │
└─────────────┘         └──────────────┘         └─────────────┘
      │                                                  │
      │                                                  │
      ▼                                                  ▼
┌─────────────┐                                  ┌─────────────┐
│SidebarView  │                                  │ ViewModel   │
└─────────────┘                                  └─────────────┘
      │                                                  │
      │                                                  │
      ▼                                                  ▼
┌──────────────────────────────────────────────────────────────┐
│                        Services Layer                         │
└──────────────────────────────────────────────────────────────┘
```

### 2.3. Dependencies

#### External Dependencies
- **AtlantaFX**: Theme framework và UI components
- **Ikonli**: Icon library (Octicons, Feather)
- **Lombok**: Boilerplate reduction
- **SLF4J + Logback**: Logging

#### Internal Dependencies
- `com.noteflix.pcm.core.navigation`: Navigation system
- `com.noteflix.pcm.core.theme`: Theme management
- `com.noteflix.pcm.core.i18n`: Internationalization
- `com.noteflix.pcm.core.utils`: Utility classes
- `com.noteflix.pcm.application.service`: Business services
- `com.noteflix.pcm.domain`: Domain models

---

## 3. Vấn Đề Và Điểm Yếu

### 3.1. Code Smells Đã Phát Hiện

#### 3.1.1. God Classes ⚠️⚠️⚠️

**AIAssistantPage.java** (806 lines)
- Quá nhiều responsibilities trong một class
- Khó đọc, khó maintain
- Violates Single Responsibility Principle

**MainView.java** (515 lines)
- Chứa cả logic layout và demo content
- Mix concerns: navigation, theme, demo UI

**SidebarView.java** (344 lines)
- Chứa cả UI creation và event handling
- Tạo menu items inline

#### 3.1.2. Code Duplication

1. **UI Component Creation**
   ```java
   // Duplicate trong nhiều files
   Button button = new Button();
   button.setGraphic(new FontIcon(...));
   button.getStyleClass().addAll(Styles.FLAT, ...);
   button.setOnAction(e -> ...);
   ```

2. **Dialog/Alert Creation**
   ```java
   // Duplicate trong SidebarView
   Alert alert = new Alert(Alert.AlertType.INFORMATION);
   alert.setTitle(title);
   alert.setHeaderText(null);
   alert.setContentText(content);
   alert.showAndWait();
   ```

3. **Layout Patterns**
   - Section headers với icon và action button
   - Card layouts với padding và styling
   - List items với avatar và details

#### 3.1.3. Inconsistent Architecture

1. **ViewModel Usage**
   - ✅ `AIAssistantPage` + `AIAssistantViewModel`: MVVM properly
   - ❌ `CSSTestPage`: No ViewModel
   - ❌ `UniversalTextDemoPage`: No ViewModel
   - ⚠️ Mixed approach gây confusion

2. **Service Injection**
   - Some pages: Constructor injection
   - Some pages: Factory methods
   - Some pages: Direct instantiation

3. **Page Initialization**
   ```java
   // AIAssistantPage
   public AIAssistantPage() {
       this(createDefaultConversationService(), createDefaultAIService());
   }
   
   // Other pages: Direct instantiation
   public CSSTestPage() {
       super(...);
   }
   ```

#### 3.1.4. Tight Coupling

1. **Navigation**
   ```java
   // SidebarView directly references page classes
   pageNavigator.navigateToPage(AIAssistantPage.class);
   pageNavigator.navigateToPage(KnowledgeBasePage.class);
   ```

2. **Theme Management**
   - Direct dependency on `ThemeManager.getInstance()`
   - Singleton pattern tạo tight coupling

#### 3.1.5. Missing Abstractions

1. **No UI Component Library**
   - Mỗi page tự tạo buttons, cards, forms
   - Không có reusable UI components

2. **No Layout Utilities**
   - Duplicate layout code (spacing, padding, alignment)
   - Magic numbers everywhere

3. **No Validation Framework**
   - Form validation logic scattered
   - No consistent error handling

#### 3.1.6. Testing Issues

1. **Hard to Unit Test**
   - UI logic mixed với business logic
   - Heavy use của JavaFX components

2. **No Test Coverage**
   - Không có UI tests
   - Không có ViewModel tests

### 3.2. Performance Issues

1. **Page Creation**
   - Pages được tạo mới mỗi lần navigate (cached nhưng eager initialization)
   - Heavy pages như `AIAssistantPage` slow startup

2. **Memory Leaks**
   - Listeners không được cleanup properly
   - Navigation history có thể grow unbounded

### 3.3. Maintainability Issues

1. **Magic Strings**
   ```java
   getStyleClass().add("sidebar");
   getStyleClass().add("chat-sidebar");
   getStyleClass().add("ai-chat-page");
   ```

2. **Hard-coded Values**
   ```java
   setPrefWidth(280);
   setPadding(new Insets(16, 12, 16, 12));
   ```

3. **Poor Documentation**
   - Một số methods thiếu JavaDoc
   - Không có architecture documentation

---

## 4. Mục Tiêu Refactoring

### 4.1. Mục Tiêu Chính

1. **Cải thiện Tính Nhất Quán**
   - Tất cả pages sử dụng MVVM pattern
   - Consistent naming conventions
   - Consistent code structure

2. **Tăng Khả Năng Tái Sử Dụng**
   - UI component library
   - Reusable layout components
   - Shared utilities

3. **Giảm Coupling**
   - Dependency Injection framework
   - Interface-based design
   - Event-driven architecture

4. **Cải thiện Testability**
   - Separate UI từ business logic
   - Mockable dependencies
   - Test-friendly architecture

5. **Tăng Maintainability**
   - Smaller, focused classes
   - Clear separation of concerns
   - Better documentation

### 4.2. Metrics Đo Lường Thành Công

| Metric | Current | Target |
|--------|---------|--------|
| Avg. Lines per Page | ~300 | < 200 |
| Largest Class | 806 lines | < 300 |
| Code Duplication | ~25% | < 10% |
| Test Coverage | 0% | > 70% |
| MVVM Compliance | 40% | 100% |

---

## 5. Kiến Trúc Mới

### 5.1. Package Structure Mới

```
ui/
├── README.md                          # Package documentation
├── MainController.java                # Refactored
├── MainView.java                      # Refactored
│
├── base/                              # 🆕 Base classes & interfaces
│   ├── BaseView.java                  # Enhanced BasePage
│   ├── BaseViewModel.java             # Enhanced
│   ├── BaseController.java            # 🆕 Base for controllers
│   ├── ViewLifecycle.java             # 🆕 Lifecycle interface
│   └── ViewModelFactory.java          # 🆕 ViewModel creation
│
├── components/                        # Reusable UI components
│   ├── common/                        # 🆕 Common components
│   │   ├── buttons/
│   │   │   ├── IconButton.java
│   │   │   ├── PrimaryButton.java
│   │   │   └── SecondaryButton.java
│   │   ├── cards/
│   │   │   ├── Card.java
│   │   │   ├── StatCard.java
│   │   │   └── InfoCard.java
│   │   ├── forms/
│   │   │   ├── FormField.java
│   │   │   ├── ValidatedTextField.java
│   │   │   └── FormBuilder.java
│   │   ├── dialogs/
│   │   │   ├── DialogBuilder.java
│   │   │   ├── ConfirmDialog.java
│   │   │   └── InfoDialog.java
│   │   └── lists/
│   │       ├── ListItem.java
│   │       ├── AvatarListItem.java
│   │       └── IconListItem.java
│   │
│   ├── navigation/                    # 🆕 Navigation components
│   │   ├── NavigationBar.java
│   │   ├── Breadcrumb.java
│   │   ├── AppHeader.java
│   │   └── SidebarView.java          # Refactored
│   │
│   ├── text/                          # Text components (existing)
│   │   ├── UniversalTextComponent.java
│   │   ├── TextContentType.java
│   │   ├── ViewMode.java
│   │   └── renderers/
│   │       ├── TextRenderer.java
│   │       ├── PlainTextRenderer.java
│   │       └── MarkdownRenderer.java
│   │
│   └── widgets/                       # 🆕 Specialized widgets
│       ├── SearchBox.java
│       ├── LoadingIndicator.java
│       ├── StatusBadge.java
│       └── ThemeToggle.java
│
├── layout/                            # Layout components
│   ├── MainLayout.java                # Refactored MainLayer
│   ├── TwoColumnLayout.java           # 🆕
│   ├── ThreeColumnLayout.java         # 🆕
│   ├── HeaderContentLayout.java       # 🆕
│   └── builders/                      # 🆕 Layout builders
│       ├── LayoutBuilder.java
│       └── ResponsiveLayout.java
│
├── pages/                             # Application pages
│   ├── base/
│   │   └── BasePage.java              # Refactored
│   │
│   ├── ai/                            # 🆕 AI Assistant module
│   │   ├── AIAssistantPage.java       # Refactored (< 200 lines)
│   │   ├── AIAssistantViewModel.java  # Refactored
│   │   ├── components/                # 🆕 Page-specific components
│   │   │   ├── ChatSidebar.java
│   │   │   ├── ChatMessageList.java
│   │   │   ├── ChatInputArea.java
│   │   │   └── ConversationItem.java
│   │   └── models/                    # 🆕 Page-specific models
│   │       └── ChatUIModel.java
│   │
│   ├── knowledge/                     # 🆕 Knowledge Base module
│   │   ├── KnowledgeBasePage.java
│   │   ├── KnowledgeBaseViewModel.java
│   │   └── components/
│   │
│   ├── database/                      # 🆕 Database module
│   │   ├── DatabaseObjectsPage.java
│   │   ├── DatabaseObjectsViewModel.java
│   │   └── components/
│   │
│   ├── batch/                         # 🆕 Batch Jobs module
│   │   ├── BatchJobsPage.java
│   │   ├── BatchJobsViewModel.java
│   │   └── components/
│   │
│   ├── settings/                      # 🆕 Settings module
│   │   ├── SettingsPage.java
│   │   ├── SettingsViewModel.java
│   │   └── components/
│   │
│   └── demo/                          # 🆕 Demo pages
│       ├── UniversalTextDemoPage.java
│       └── CSSTestPage.java
│
├── viewmodel/                         # ViewModels (consolidated)
│   ├── base/
│   │   ├── BaseViewModel.java         # Enhanced
│   │   └── ViewModelLifecycle.java    # 🆕
│   │
│   └── shared/                        # 🆕 Shared ViewModels
│       └── NavigationViewModel.java
│
├── styles/                            # 🆕 Style constants
│   ├── StyleConstants.java            # CSS class names
│   ├── LayoutConstants.java           # Spacing, sizes
│   └── ColorConstants.java            # Color variables
│
├── utils/                             # 🆕 UI utilities
│   ├── UIFactory.java                 # Component factory
│   ├── LayoutHelper.java              # Layout utilities
│   ├── ValidationHelper.java          # Validation utilities
│   └── AnimationHelper.java           # Animation utilities
│
└── events/                            # 🆕 UI events
    ├── UIEvent.java                   # Base event
    ├── NavigationEvent.java
    ├── ThemeChangeEvent.java
    └── EventBus.java                  # Simple event bus
```

### 5.2. Architecture Principles

#### 5.2.1. MVVM Strict Enforcement

```
┌──────────────────────────────────────────────────────────────┐
│                         View Layer                            │
│  ┌────────────┐   ┌─────────────┐   ┌──────────────┐        │
│  │   Page     │──▶│  ViewModel  │──▶│   Service    │        │
│  └────────────┘   └─────────────┘   └──────────────┘        │
│         │                 │                                    │
│         │                 │                                    │
│         ▼                 ▼                                    │
│  ┌────────────┐   ┌─────────────┐                            │
│  │ Components │   │ Properties  │                            │
│  └────────────┘   └─────────────┘                            │
└──────────────────────────────────────────────────────────────┘

Rules:
1. View chỉ chứa UI code, không có business logic
2. ViewModel chứa UI state và coordinates services
3. View binds to ViewModel properties
4. View calls ViewModel commands
5. ViewModel không biết gì về View
```

#### 5.2.2. Dependency Injection

```java
// Simple DI Container
public class DIContainer {
    private Map<Class<?>, Object> singletons = new HashMap<>();
    private Map<Class<?>, Supplier<?>> factories = new HashMap<>();
    
    public <T> void registerSingleton(Class<T> type, T instance);
    public <T> void registerFactory(Class<T> type, Supplier<T> factory);
    public <T> T resolve(Class<T> type);
}

// Usage
DIContainer container = new DIContainer();
container.registerSingleton(ConversationService.class, new ConversationService());
container.registerFactory(AIAssistantViewModel.class, 
    () -> new AIAssistantViewModel(
        container.resolve(ConversationService.class),
        container.resolve(AIService.class)
    ));
```

#### 5.2.3. Component-Based Design

```java
// Reusable components
public class Card extends VBox {
    public Card() {
        getStyleClass().add("card");
        setPadding(new Insets(LayoutConstants.CARD_PADDING));
    }
    
    public Card withTitle(String title) { ... }
    public Card withContent(Node content) { ... }
}

// Usage
Card card = new Card()
    .withTitle("Description")
    .withContent(descriptionArea);
```

### 5.3. Design Patterns Mới

#### 5.3.1. Builder Pattern cho UI

```java
Button button = new ButtonBuilder()
    .withText("Submit")
    .withIcon(Octicons.CHECK_16)
    .withStyle(ButtonStyle.PRIMARY)
    .withAction(this::handleSubmit)
    .build();
```

#### 5.3.2. Factory Pattern cho Components

```java
public class UIFactory {
    public static Button createIconButton(Octicons icon, Runnable action);
    public static Card createStatCard(String title, String value);
    public static HBox createSectionHeader(String title, Button action);
}
```

#### 5.3.3. Strategy Pattern cho Rendering

```java
public interface ContentRenderer {
    Node render(Content content);
}

public class MarkdownRenderer implements ContentRenderer { ... }
public class CodeRenderer implements ContentRenderer { ... }
```

#### 5.3.4. Command Pattern cho Actions

```java
public interface Command {
    void execute();
    void undo();
}

public class NavigateCommand implements Command { ... }
public class SaveCommand implements Command { ... }
```

---

## 6. Kế Hoạch Thực Hiện

### 6.1. Phases Overview

```
Phase 1: Foundation (Week 1-2)
    ↓
Phase 2: Core Components (Week 3-4)
    ↓
Phase 3: Pages Refactoring (Week 5-7)
    ↓
Phase 4: Integration & Testing (Week 8-9)
    ↓
Phase 5: Documentation & Polish (Week 10)
```

### 6.2. Phase 1: Foundation (Week 1-2)

#### Tasks

1. **Setup New Package Structure** (2 days)
   - [ ] Create new package directories
   - [ ] Create README files for each package
   - [ ] Setup package-info.java files

2. **Create Base Classes** (3 days)
   - [ ] Refactor `BaseViewModel.java`
   - [ ] Create `ViewModelLifecycle.java`
   - [ ] Create `ViewModelFactory.java`
   - [ ] Refactor `BasePage.java` → `BaseView.java`
   - [ ] Create `ViewLifecycle.java`
   - [ ] Create `BaseController.java`

3. **Create DI Container** (2 days)
   - [ ] Implement simple DI container
   - [ ] Create service registration
   - [ ] Test DI with existing services

4. **Create Constants** (1 day)
   - [ ] `StyleConstants.java`
   - [ ] `LayoutConstants.java`
   - [ ] `ColorConstants.java`

5. **Create Utilities** (2 days)
   - [ ] `UIFactory.java`
   - [ ] `LayoutHelper.java`
   - [ ] `ValidationHelper.java`

**Deliverables:**
- New package structure created
- Base classes implemented
- DI container working
- Utilities ready to use

### 6.3. Phase 2: Core Components (Week 3-4)

#### Tasks

1. **Common Components - Buttons** (1 day)
   - [ ] `IconButton.java`
   - [ ] `PrimaryButton.java`
   - [ ] `SecondaryButton.java`
   - [ ] `ButtonBuilder.java`

2. **Common Components - Cards** (1 day)
   - [ ] `Card.java`
   - [ ] `StatCard.java`
   - [ ] `InfoCard.java`

3. **Common Components - Forms** (2 days)
   - [ ] `FormField.java`
   - [ ] `ValidatedTextField.java`
   - [ ] `FormBuilder.java`

4. **Common Components - Dialogs** (1 day)
   - [ ] `DialogBuilder.java`
   - [ ] `ConfirmDialog.java`
   - [ ] `InfoDialog.java`

5. **Common Components - Lists** (1 day)
   - [ ] `ListItem.java`
   - [ ] `AvatarListItem.java`
   - [ ] `IconListItem.java`

6. **Navigation Components** (2 days)
   - [ ] `NavigationBar.java`
   - [ ] `Breadcrumb.java`
   - [ ] `AppHeader.java`
   - [ ] Refactor `SidebarView.java`

7. **Widgets** (2 days)
   - [ ] `SearchBox.java`
   - [ ] `LoadingIndicator.java`
   - [ ] `StatusBadge.java`
   - [ ] `ThemeToggle.java`

**Deliverables:**
- Complete component library
- Components tested individually
- Documentation for each component

### 6.4. Phase 3: Pages Refactoring (Week 5-7)

#### Priority Order:
1. AIAssistantPage (highest priority, most complex)
2. KnowledgeBasePage
3. DatabaseObjectsPage
4. BatchJobsPage
5. SettingsPage
6. Demo pages

#### Tasks per Page (Example: AIAssistantPage)

**Week 5: AIAssistantPage** (5 days)

Day 1-2: Break down into components
- [ ] Create `ai/components/ChatSidebar.java`
- [ ] Create `ai/components/ChatMessageList.java`
- [ ] Create `ai/components/ChatInputArea.java`
- [ ] Create `ai/components/ConversationItem.java`

Day 3: Refactor ViewModel
- [ ] Review `AIAssistantViewModel.java`
- [ ] Extract common logic to base
- [ ] Improve property bindings
- [ ] Add validation

Day 4: Refactor Page
- [ ] Simplify `AIAssistantPage.java`
- [ ] Use new components
- [ ] Apply MVVM strictly
- [ ] Target < 200 lines

Day 5: Testing
- [ ] Unit tests for ViewModel
- [ ] Integration tests for Page
- [ ] Manual testing

**Week 6: KnowledgeBase + Database** (5 days)

Days 1-2: KnowledgeBasePage
- [ ] Create components
- [ ] Refactor ViewModel
- [ ] Refactor Page
- [ ] Tests

Days 3-4: DatabaseObjectsPage
- [ ] Create components
- [ ] Refactor ViewModel
- [ ] Refactor Page
- [ ] Tests

Day 5: Buffer/Review

**Week 7: Remaining Pages** (5 days)

Days 1-2: BatchJobsPage
Days 3-4: SettingsPage
Day 5: Demo pages

**Deliverables:**
- All pages refactored
- All pages use MVVM
- All pages < 300 lines
- Tests for all pages

### 6.5. Phase 4: Integration & Testing (Week 8-9)

#### Tasks

**Week 8: Integration**

1. **Refactor MainView** (2 days)
   - [ ] Use new `AppHeader` component
   - [ ] Use refactored `SidebarView`
   - [ ] Remove demo content
   - [ ] Simplify structure

2. **Refactor MainController** (1 day)
   - [ ] Remove boilerplate
   - [ ] Use DI
   - [ ] Simplify methods

3. **Update Navigation** (1 day)
   - [ ] Integrate with new page structure
   - [ ] Test navigation flow
   - [ ] Fix navigation history

4. **Theme Integration** (1 day)
   - [ ] Test theme switching
   - [ ] Verify all components
   - [ ] Fix theme issues

**Week 9: Testing**

1. **Unit Tests** (2 days)
   - [ ] ViewModel tests
   - [ ] Component tests
   - [ ] Utility tests

2. **Integration Tests** (2 days)
   - [ ] Page integration tests
   - [ ] Navigation tests
   - [ ] Service integration tests

3. **E2E Tests** (1 day)
   - [ ] User workflows
   - [ ] Critical paths

**Deliverables:**
- Fully integrated system
- Test coverage > 70%
- All tests passing

### 6.6. Phase 5: Documentation & Polish (Week 10)

#### Tasks

1. **Code Documentation** (2 days)
   - [ ] JavaDoc for all public APIs
   - [ ] Package documentation
   - [ ] Architecture diagrams

2. **User Documentation** (1 day)
   - [ ] Component usage guide
   - [ ] Page creation guide
   - [ ] Best practices

3. **Developer Documentation** (1 day)
   - [ ] Architecture overview
   - [ ] Design patterns used
   - [ ] Extension guide

4. **Code Review & Cleanup** (1 day)
   - [ ] Remove deprecated code
   - [ ] Fix warnings
   - [ ] Format code

**Deliverables:**
- Complete documentation
- Clean codebase
- Ready for production

---

## 7. Migration Guide

### 7.1. Migration Strategy

**Strategy: Incremental Migration**

- ✅ Không break existing code
- ✅ Migrate từng page một
- ✅ Keep old và new code chạy song song
- ✅ Gradual cutover

### 7.2. Backward Compatibility

```java
// Old way (still works)
public class MyPage extends BasePage {
    // ...
}

// New way (recommended)
public class MyPage extends BaseView {
    // ...
}

// BasePage deprecated but still available
@Deprecated
public abstract class BasePage extends BaseView {
    // Delegate to BaseView
}
```

### 7.3. Migration Checklist per Page

- [ ] Create ViewModel (if not exists)
- [ ] Extract reusable components
- [ ] Update imports
- [ ] Use new base classes
- [ ] Apply DI
- [ ] Update tests
- [ ] Remove deprecated code
- [ ] Update documentation

### 7.4. Breaking Changes

#### Changes Requiring Code Updates:

1. **BasePage → BaseView**
   ```java
   // Old
   public class MyPage extends BasePage
   
   // New
   public class MyPage extends BaseView
   ```

2. **ViewModel Constructor**
   ```java
   // Old
   new AIAssistantViewModel(new ConversationService(), new AIService())
   
   // New
   container.resolve(AIAssistantViewModel.class)
   ```

3. **Component Creation**
   ```java
   // Old
   Button btn = new Button("Submit");
   btn.getStyleClass().add(Styles.ACCENT);
   
   // New
   Button btn = UIFactory.createPrimaryButton("Submit", this::handleSubmit);
   ```

---

## 8. Testing Strategy

### 8.1. Test Pyramid

```
        ╱╲
       ╱  ╲
      ╱ E2E╲         < 10%
     ╱──────╲
    ╱        ╲
   ╱Integration╲     20-30%
  ╱────────────╲
 ╱              ╲
╱  Unit  Tests  ╲   60-70%
──────────────────
```

### 8.2. Unit Tests

**Target: 70% coverage**

#### ViewModel Tests

```java
@Test
void testSendMessage() {
    // Given
    AIAssistantViewModel vm = new AIAssistantViewModel(mockService, mockAI);
    vm.setUserInput("Hello");
    
    // When
    vm.sendMessage(null);
    
    // Then
    verify(mockAI).streamResponse(any(), eq("Hello"), any());
    assertTrue(vm.isBusy());
}
```

#### Component Tests

```java
@Test
void testCardCreation() {
    Card card = new Card()
        .withTitle("Test")
        .withContent(new Label("Content"));
    
    assertEquals("Test", card.getTitle());
    assertNotNull(card.getContent());
}
```

### 8.3. Integration Tests

```java
@Test
void testPageNavigation() {
    // Given
    PageNavigator nav = new DefaultPageNavigator(container);
    
    // When
    nav.navigateToPage(AIAssistantPage.class);
    
    // Then
    assertEquals(AIAssistantPage.class, nav.getCurrentPage().getClass());
}
```

### 8.4. E2E Tests

```java
@Test
void testChatWorkflow() {
    // 1. Navigate to AI Assistant
    clickOn("#ai-assistant-menu");
    
    // 2. Create new conversation
    clickOn("#new-chat-button");
    
    // 3. Send message
    write("Hello");
    clickOn("#send-button");
    
    // 4. Verify response
    verifyThat("#chat-messages", hasText("Hello"));
}
```

---

## 9. Checklist

### 9.1. Pre-Refactoring

- [ ] Review current codebase
- [ ] Identify all dependencies
- [ ] Create backup branch
- [ ] Setup CI/CD
- [ ] Prepare test environment

### 9.2. During Refactoring

#### Phase 1: Foundation
- [ ] Package structure created
- [ ] Base classes implemented
- [ ] DI container working
- [ ] Constants defined
- [ ] Utilities created

#### Phase 2: Components
- [ ] Button components
- [ ] Card components
- [ ] Form components
- [ ] Dialog components
- [ ] List components
- [ ] Navigation components
- [ ] Widgets

#### Phase 3: Pages
- [ ] AIAssistantPage refactored
- [ ] KnowledgeBasePage refactored
- [ ] DatabaseObjectsPage refactored
- [ ] BatchJobsPage refactored
- [ ] SettingsPage refactored
- [ ] Demo pages refactored

#### Phase 4: Integration
- [ ] MainView refactored
- [ ] MainController refactored
- [ ] Navigation updated
- [ ] Theme integration
- [ ] All tests passing

#### Phase 5: Documentation
- [ ] Code documentation
- [ ] User documentation
- [ ] Developer documentation
- [ ] Migration guide

### 9.3. Post-Refactoring

- [ ] Code review completed
- [ ] Performance testing
- [ ] Security review
- [ ] Accessibility check
- [ ] Documentation review
- [ ] Final QA
- [ ] Deploy to staging
- [ ] Monitor metrics
- [ ] Deploy to production
- [ ] Post-deployment monitoring

---

## 10. Risk Management

### 10.1. Identified Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Breaking existing features | High | Medium | Incremental migration, extensive testing |
| Performance degradation | Medium | Low | Performance benchmarks, profiling |
| Team learning curve | Medium | Medium | Training, documentation, pair programming |
| Scope creep | High | High | Strict phase boundaries, regular reviews |
| Timeline delays | Medium | Medium | Buffer time, prioritization |

### 10.2. Rollback Plan

If critical issues occur:

1. **Revert to backup branch**
2. **Identify root cause**
3. **Fix in isolation**
4. **Re-test thoroughly**
5. **Gradual re-deployment**

---

## 11. Success Metrics

### 11.1. Code Quality Metrics

- [ ] Code duplication < 10%
- [ ] Average class size < 200 lines
- [ ] Cyclomatic complexity < 10
- [ ] Test coverage > 70%

### 11.2. Performance Metrics

- [ ] Page load time < 500ms
- [ ] Navigation transition < 200ms
- [ ] Memory usage stable
- [ ] No memory leaks

### 11.3. Developer Experience Metrics

- [ ] New feature development time reduced 30%
- [ ] Bug fix time reduced 40%
- [ ] Code review time reduced 25%
- [ ] Onboarding time for new devs reduced 50%

---

## 12. References

### 12.1. Design Patterns

- **MVVM**: https://en.wikipedia.org/wiki/Model–view–viewmodel
- **Dependency Injection**: https://martinfowler.com/articles/injection.html
- **Builder Pattern**: https://refactoring.guru/design-patterns/builder

### 12.2. JavaFX Resources

- **JavaFX Documentation**: https://openjfx.io/
- **AtlantaFX**: https://github.com/mkpaz/atlantafx
- **TestFX**: https://github.com/TestFX/TestFX

### 12.3. Best Practices

- **Clean Code**: Robert C. Martin
- **SOLID Principles**: Uncle Bob
- **Refactoring**: Martin Fowler

---

## Appendix A: Detailed Component Specifications

### A.1. Button Components

#### IconButton

```java
/**
 * Reusable icon button with consistent styling
 */
public class IconButton extends Button {
    public IconButton(Octicons icon, String tooltip);
    public IconButton withSize(int size);
    public IconButton withStyle(String... styles);
}
```

#### PrimaryButton

```java
/**
 * Primary action button (accent color)
 */
public class PrimaryButton extends Button {
    public PrimaryButton(String text);
    public PrimaryButton(String text, Octicons icon);
    public PrimaryButton withAction(Runnable action);
}
```

### A.2. Card Components

#### Card

```java
/**
 * Basic card container with padding and styling
 */
public class Card extends VBox {
    public Card();
    public Card withTitle(String title);
    public Card withContent(Node... content);
    public Card withFooter(Node footer);
}
```

#### StatCard

```java
/**
 * Card for displaying statistics
 */
public class StatCard extends Card {
    public StatCard(String label, String value);
    public StatCard withTrend(String trend);
    public StatCard withColor(String color);
}
```

---

## Appendix B: Code Examples

### B.1. Before Refactoring

```java
// AIAssistantPage.java - 806 lines ⚠️
public class AIAssistantPage extends BasePage {
    private VBox chatSidebar;
    private VBox chatMessages;
    private TextArea input;
    
    // 50+ lines of UI creation code
    private VBox createChatSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(16));
        sidebar.setSpacing(12);
        
        // 20+ lines creating search box
        TextField searchBox = new TextField();
        searchBox.setPromptText("Search...");
        // ...
        
        // 30+ lines creating session list
        VBox sessions = new VBox(8);
        // ...
        
        return sidebar;
    }
    
    // 100+ lines more...
}
```

### B.2. After Refactoring

```java
// AIAssistantPage.java - < 200 lines ✅
@RequiredArgsConstructor
public class AIAssistantPage extends BaseView {
    private final AIAssistantViewModel viewModel;
    
    @Override
    protected Node createContent() {
        return new TwoColumnLayout()
            .withSidebar(new ChatSidebar(viewModel))
            .withContent(createMainContent())
            .build();
    }
    
    private Node createMainContent() {
        return new VBox(
            new AppHeader(viewModel.titleProperty()),
            new ChatMessageList(viewModel.getMessages()),
            new ChatInputArea(viewModel)
        );
    }
}

// ChatSidebar.java - dedicated component
public class ChatSidebar extends VBox {
    public ChatSidebar(AIAssistantViewModel viewModel) {
        getStyleClass().add("chat-sidebar");
        
        getChildren().addAll(
            createHeader(viewModel),
            new SearchBox(viewModel.searchQueryProperty()),
            new ConversationList(viewModel.getConversations())
        );
    }
}
```

---

## Changelog

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2025-11-15 | PCM Team | Initial draft |

---

**Document End**

