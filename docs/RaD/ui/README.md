# PCM Desktop - UI Module

> **Package**: `com.noteflix.pcm.ui`  
> **Version**: 2.0.0  
> **Architecture**: MVVM (Model-View-ViewModel)

## 📋 Mục Lục

- [Tổng Quan](#tổng-quan)
- [Kiến Trúc](#kiến-trúc)
- [Cấu Trúc Package](#cấu-trúc-package)
- [Bắt Đầu](#bắt-đầu)
- [Tài Liệu](#tài-liệu)

## Tổng Quan

Module UI của PCM Desktop được xây dựng với JavaFX và tuân theo kiến trúc MVVM (Model-View-ViewModel). Module này cung cấp giao diện người dùng cho toàn bộ ứng dụng, bao gồm các trang chính, components tái sử dụng, và các utilities hỗ trợ.

### Đặc Điểm Chính

- ✅ **MVVM Pattern**: Tách biệt UI logic và business logic
- ✅ **Component-Based**: UI components tái sử dụng
- ✅ **Responsive**: Giao diện tự động điều chỉnh theo kích thước
- ✅ **Themeable**: Hỗ trợ Light/Dark theme
- ✅ **Accessible**: Tuân thủ accessibility standards
- ✅ **Testable**: Dễ dàng unit test và integration test

### Công Nghệ

- **JavaFX 21**: UI framework
- **AtlantaFX**: Modern theme framework
- **Ikonli**: Icon library (Octicons, Feather)
- **Lombok**: Code generation
- **SLF4J**: Logging

## Kiến Trúc

### MVVM Pattern

```
┌─────────────────────────────────────────────────────────┐
│                         View                             │
│  ┌──────────┐         ┌──────────┐         ┌─────────┐ │
│  │  Page    │────────▶│ViewModel │────────▶│ Service │ │
│  └──────────┘         └──────────┘         └─────────┘ │
│       │                     │                            │
│       │ Binding             │ Properties                 │
│       ▼                     ▼                            │
│  ┌──────────┐         ┌──────────┐                      │
│  │Components│         │  State   │                      │
│  └──────────┘         └──────────┘                      │
└─────────────────────────────────────────────────────────┘
```

### Luồng Dữ Liệu

1. **User Input** → View captures event
2. **View** → Calls ViewModel command
3. **ViewModel** → Updates state, calls services
4. **Service** → Executes business logic
5. **ViewModel** → Updates observable properties
6. **View** → Auto-updates via bindings

## Cấu Trúc Package

```
ui/
├── README.md                    # Tài liệu này
├── UI_REFACTORING_PLAN.md       # Kế hoạch refactoring
├── ARCHITECTURE.md              # Chi tiết kiến trúc
├── COMPONENT_LIBRARY.md         # Thư viện components
├── BEST_PRACTICES.md            # Best practices
│
├── base/                        # Base classes
├── components/                  # UI components
│   ├── common/                  # Common components
│   ├── navigation/              # Navigation components
│   ├── text/                    # Text components
│   └── widgets/                 # Specialized widgets
├── layout/                      # Layout components
├── pages/                       # Application pages
│   ├── ai/                      # AI Assistant
│   ├── knowledge/               # Knowledge Base
│   ├── database/                # Database
│   ├── batch/                   # Batch Jobs
│   └── settings/                # Settings
├── viewmodel/                   # ViewModels
├── styles/                      # Style constants
├── utils/                       # UI utilities
└── events/                      # UI events
```

## Bắt Đầu

### Tạo Page Mới

#### Bước 1: Tạo ViewModel

```java
package com.noteflix.pcm.ui.viewmodel;

import com.noteflix.pcm.ui.base.BaseViewModel;
import javafx.beans.property.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyPageViewModel extends BaseViewModel {
    
    private final StringProperty title = new SimpleStringProperty("My Page");
    private final ObservableList<Item> items = FXCollections.observableArrayList();
    
    public MyPageViewModel(MyService service) {
        this.service = service;
    }
    
    // Commands
    public void loadData() {
        setBusy(true);
        runAsync(
            () -> service.fetchData(),
            data -> items.setAll(data),
            error -> setError("Failed to load data", error)
        ).whenComplete((result, error) -> setBusy(false));
    }
    
    // Properties
    public StringProperty titleProperty() { return title; }
    public ObservableList<Item> getItems() { return items; }
}
```

#### Bước 2: Tạo Page

```java
package com.noteflix.pcm.ui.pages.myfeature;

import com.noteflix.pcm.ui.base.BaseView;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.octicons.Octicons;

@RequiredArgsConstructor
public class MyPage extends BaseView {
    
    private final MyPageViewModel viewModel;
    
    public MyPage() {
        super("My Page", "Description of my page", Octicons.STAR_24);
    }
    
    @Override
    protected Node createMainContent() {
        VBox content = new VBox(20);
        content.getChildren().addAll(
            createToolbar(),
            createDataTable()
        );
        return content;
    }
    
    private Node createToolbar() {
        // Use UI components from component library
        return new HBox(10,
            UIFactory.createPrimaryButton("Load Data", viewModel::loadData),
            UIFactory.createSecondaryButton("Refresh", this::handleRefresh)
        );
    }
    
    private Node createDataTable() {
        TableView<Item> table = new TableView<>();
        table.setItems(viewModel.getItems());
        // Configure table...
        return table;
    }
    
    @Override
    public void onPageActivated() {
        super.onPageActivated();
        viewModel.onActivate();
        viewModel.loadData();
    }
}
```

#### Bước 3: Đăng Ký với Navigation

```java
// In SidebarView or navigation configuration
pageNavigator.navigateToPage(MyPage.class);
```

### Sử Dụng Components

```java
// Buttons
Button primary = UIFactory.createPrimaryButton("Submit", this::handleSubmit);
Button secondary = UIFactory.createSecondaryButton("Cancel", this::handleCancel);
Button icon = new IconButton(Octicons.PLUS_16, "Add Item");

// Cards
Card card = new Card()
    .withTitle("Statistics")
    .withContent(statsContent)
    .withFooter(actionButtons);

// Forms
Form form = new FormBuilder()
    .addField("Name", nameField)
    .addField("Email", emailField)
    .addField("Password", passwordField)
    .build();

// Dialogs
DialogBuilder.info()
    .title("Success")
    .content("Operation completed successfully")
    .show();
```

## Tài Liệu

### Tài Liệu Chi Tiết

- [UI_REFACTORING_PLAN.md](UI_REFACTORING_PLAN.md) - Kế hoạch refactoring chi tiết
- [ARCHITECTURE.md](ARCHITECTURE.md) - Kiến trúc và design patterns
- [COMPONENT_LIBRARY.md](COMPONENT_LIBRARY.md) - Thư viện components
- [BEST_PRACTICES.md](BEST_PRACTICES.md) - Best practices và coding standards

### External Resources

- [JavaFX Documentation](https://openjfx.io/)
- [AtlantaFX Theme](https://github.com/mkpaz/atlantafx)
- [Ikonli Icons](https://kordamp.org/ikonli/)

## Design Principles

### SOLID Principles

1. **Single Responsibility**: Mỗi class chỉ có một lý do để thay đổi
2. **Open/Closed**: Mở cho extension, đóng cho modification
3. **Liskov Substitution**: Subclasses có thể thay thế base classes
4. **Interface Segregation**: Nhiều interfaces nhỏ hơn một interface lớn
5. **Dependency Inversion**: Phụ thuộc vào abstractions, không phải implementations

### Clean Code

- ✅ Meaningful names
- ✅ Small functions
- ✅ Comments when necessary
- ✅ Error handling
- ✅ Formatting consistency

### UI Guidelines

- ✅ Consistent spacing và padding
- ✅ Responsive layouts
- ✅ Accessible controls
- ✅ Loading states
- ✅ Error messages
- ✅ Empty states

## Testing

### Unit Tests

```java
@Test
void testLoadData() {
    // Given
    MyPageViewModel vm = new MyPageViewModel(mockService);
    when(mockService.fetchData()).thenReturn(testData);
    
    // When
    vm.loadData();
    
    // Then
    await().until(() -> !vm.isBusy());
    assertEquals(testData.size(), vm.getItems().size());
}
```

### Integration Tests

```java
@Test
void testPageNavigation() {
    // Given
    PageNavigator nav = new DefaultPageNavigator(container);
    
    // When
    nav.navigateToPage(MyPage.class);
    
    // Then
    assertEquals(MyPage.class, nav.getCurrentPage().getClass());
}
```

## Contributing

### Workflow

1. Tạo feature branch từ `development`
2. Implement changes theo BEST_PRACTICES.md
3. Viết tests (coverage > 70%)
4. Run `mvn test` và `scripts/format.sh`
5. Create Pull Request
6. Code review
7. Merge sau khi approved

### Code Style

- Follow Google Java Style Guide
- Use `scripts/format.sh` trước khi commit
- Write JavaDoc for public APIs
- Add comments for complex logic

## FAQ

### Q: Làm sao để thêm một page mới?

**A**: Xem section "Bắt Đầu" ở trên. Tạo ViewModel → Tạo Page → Đăng ký navigation.

### Q: Làm sao để tạo custom component?

**A**: Kế thừa từ JavaFX components (VBox, HBox, etc.) và add custom behavior. Xem COMPONENT_LIBRARY.md.

### Q: Làm sao để theme component của tôi?

**A**: Sử dụng CSS classes từ AtlantaFX và custom classes. Xem ARCHITECTURE.md.

### Q: Làm sao để test UI code?

**A**: Tách logic vào ViewModel (dễ test) và test UI với TestFX. Xem Testing section.

## Contact

- **Team**: PCM Development Team
- **Email**: dev@noteflix.com
- **Slack**: #pcm-desktop

---

**Last Updated**: 2025-11-15  
**Version**: 2.0.0

