Dưới đây là một “khung chuẩn” để bạn tổ chức dự án JavaFX gọn gàng, sạch, dễ test và bám SOLID. Mình dùng MVVM + DI nhẹ, tách navigation, async, i18n, theme, và đóng gói native.

# Kiến trúc đề xuất

* **MVVM** (Model–View–ViewModel):

    * **View (FXML + Controller mỏng)**: chỉ xử lý binding, forward event → ViewModel. Không chứa business logic.
    * **ViewModel**: state dưới dạng `Property/Observable*`, expose `Command`/handler, gọi **Service** qua interface.
    * **Model/Service**: thuần Java/IO/HTTP/DB, không trói buộc JavaFX.
* **DI (Dependency Injection)**: inject `Service` vào ViewModel, inject ViewModel vào Controller. Có thể dùng:

    * **Afterburner.fx** (siêu nhẹ), **Guice**, hoặc **Spring Boot** nếu app lớn (nhưng cân nhắc kích thước).
* **Navigation**: tách `Navigator/StageManager`, định nghĩa `Route` (enum) → thay Scene/Root, truyền tham số qua DTO.
* **Async**: mọi IO chạy bằng `Task/Service` + `ExecutorService`. `Platform.runLater` chỉ để chốt UI.
* **Resource/I18n**: dùng `ResourceBundle` + `FXML` `fx:resources`. Theme qua CSS, không hardcode style trong code.

# Cấu trúc thư mục gợi ý

```
com.myapp/
  App.java
  module-info.java
  core/
    Navigator.java
    StageNavigator.java
    Asyncs.java          // tiện ích chạy Task, thread pool
    Events.java          // pub/sub đơn giản nếu cần
  di/
    Injector.java        // nơi “wire” phụ thuộc nếu tự làm DI
  model/
    User.java
    ...
  service/
    AuthService.java
    AuthServiceImpl.java
    SettingsService.java
  feature/
    login/
      LoginView.fxml
      LoginController.java
      LoginViewModel.java
    dashboard/
      DashboardView.fxml
      DashboardController.java
      DashboardViewModel.java
  util/
    FxBindings.java      // binding helper, converters
  resources/
    i18n/messages.properties
    styles/app.css
    icons/...
```

# Ví dụ MVVM tối giản

**module-info.java**

```java
module com.myapp {
  requires javafx.controls;
  requires javafx.fxml;
  // ... các requires khác

  exports com.myapp;
  opens com.myapp.feature.login to javafx.fxml;      // mở package chứa FXML controller
  opens com.myapp.feature.dashboard to javafx.fxml;
}
```

**App.java**

```java
public class App extends Application {
  @Override public void start(Stage stage) {
    var nav = new StageNavigator(stage, ResourceBundle.getBundle("i18n.messages"));
    nav.goTo(Route.LOGIN);
    stage.setTitle("MyApp");
    stage.show();
  }
  public static void main(String[] args) { launch(args); }
}
```

**Navigator**

```java
public enum Route { LOGIN, DASHBOARD }

public interface Navigator {
  void goTo(Route route);
  void goTo(Route route, Object param);
}

public final class StageNavigator implements Navigator {
  private final Stage stage; private final ResourceBundle rb;
  public StageNavigator(Stage stage, ResourceBundle rb) { this.stage = stage; this.rb = rb; }

  @Override public void goTo(Route route) { goTo(route, null); }

  @Override public void goTo(Route route, Object param) {
    try {
      String fxml = switch (route) {
        case LOGIN -> "/com/myapp/feature/login/LoginView.fxml";
        case DASHBOARD -> "/com/myapp/feature/dashboard/DashboardView.fxml";
      };
      FXMLLoader l = new FXMLLoader(getClass().getResource(fxml), rb);
      Parent root = l.load();
      Scene scene = stage.getScene();
      if (scene == null) stage.setScene(new Scene(root));
      else scene.setRoot(root);
      var controller = l.<Initializable>getController();
      if (controller instanceof ReceivesParam r && param != null) r.setParam(param);
    } catch (IOException e) { /* log + dialog */ }
  }

  public interface ReceivesParam { void setParam(Object param); }
}
```

**Service & ViewModel**

```java
public interface AuthService {
  CompletableFuture<Boolean> login(String user, String pass);
}
public final class AuthServiceImpl implements AuthService {
  private final Executor executor = Executors.newFixedThreadPool(4);
  @Override public CompletableFuture<Boolean> login(String u, String p) {
    return CompletableFuture.supplyAsync(() -> {
      // giả lập IO
      try { Thread.sleep(500); } catch (InterruptedException ignored) {}
      return "admin".equals(u) && "123".equals(p);
    }, executor);
  }
}
```

```java
public final class LoginViewModel {
  private final AuthService auth;
  public final StringProperty username = new SimpleStringProperty();
  public final StringProperty password = new SimpleStringProperty();
  public final BooleanProperty busy = new SimpleBooleanProperty(false);
  public final StringProperty error = new SimpleStringProperty();

  public LoginViewModel(AuthService auth) { this.auth = auth; }

  public void doLogin(Runnable onSuccess) {
    error.set(null); busy.set(true);
    auth.login(username.get(), password.get())
        .whenComplete((ok, ex) -> Platform.runLater(() -> {
          busy.set(false);
          if (ex != null) error.set("Login failed");
          else if (ok) onSuccess.run();
          else error.set("Invalid credentials");
        }));
  }
}
```

**Controller (mỏng)**

```java
public final class LoginController implements StageNavigator.ReceivesParam {
  @FXML private TextField txtUser;
  @FXML private PasswordField txtPass;
  @FXML private Button btnLogin;
  @FXML private Label lblError;
  private final LoginViewModel vm;
  private final Navigator nav;

  public LoginController() {
    // nếu không dùng framework DI, tự “wire” tối thiểu:
    var auth = new AuthServiceImpl();
    this.vm = new LoginViewModel(auth);
    this.nav = /* lấy từ singleton/ServiceLocator nhỏ hoặc truyền qua AppContext */;
    // khuyến nghị: dùng Afterburner.fx/Guice để inject vm/nav chuẩn chỉnh.
  }

  @FXML private void initialize() {
    txtUser.textProperty().bindBidirectional(vm.username);
    txtPass.textProperty().bindBidirectional(vm.password);
    btnLogin.disableProperty().bind(vm.busy);
    lblError.textProperty().bind(vm.error);
    btnLogin.setOnAction(e -> vm.doLogin(() -> nav.goTo(Route.DASHBOARD)));
  }

  @Override public void setParam(Object param) { /* optional */ }
}
```

# Best practices (ngắn gọn, trọng điểm)

* **SRP**: mỗi lớp làm một việc (Controller: wiring UI; ViewModel: state/logic; Service: IO).
* **Binding-first**: ưu tiên `Bindings`, `Property`, `ObservableList`. Tránh setText/setDisable rải rác.
* **FXML + CSS**: View trong FXML, style trong CSS; tránh inline style; dùng class selectors.
* **Threading**: IO chạy trong `Task/CompletableFuture` + `ExecutorService`. Chỉ chạm UI trong FX thread.
* **Navigation tách biệt**: không “new Stage/Scene” lung tung trong controller.
* **DI & Interface**: code vào interface, inject implementation; không `new` trực tiếp trong ViewModel.
* **Error/Loading UX**: `busy` property để disable nút, show spinner; gom dialog qua `DialogService`.
* **I18n**: `ResourceBundle` từ đầu; key rõ ràng; tránh string literal trong code.
* **Dispose**: gỡ listener khi không dùng; cân nhắc `WeakListener` cho sống lâu; hủy `ExecutorService` khi exit.
* **Logging**: SLF4J + Logback; tuyệt đối không `System.out.println` cho production.
* **Config**: `java.util.prefs.Preferences` hoặc file JSON/YAML qua `SettingsService`.
* **Packaging**: `jpackage`/`jlink` để tạo app native; ký code khi cần.
* **Libraries hữu ích**: ControlsFX, Ikonli (icon), RichTextFX (editor), Flowless (virtual flow).

# Mapping SOLID vào JavaFX

* **S**: View, ViewModel, Service mỗi thứ một trách nhiệm.
* **O**: mở rộng tính năng qua ViewModel/Service mới, không sửa lõi Navigator/Asyncs.
* **L**: contract ViewModel/Service rõ; class con thay thế được class cha.
* **I**: chia `Service` nhỏ (AuthService, SettingsService…), controller chỉ dùng interface cần.
* **D**: ViewModel phụ thuộc **interface Service**, instance cung cấp bởi DI.

# Quy ước clean code

* Tên property: `xxxProperty()` + field `xxx` dạng `StringProperty`.
* Phương thức ngắn, tránh “God Controller”.
* Không logic nghiệp vụ trong Controller.
* Tránh static global state; nếu cần context, gói trong `AppContext` (immutable) và inject.

# CSS & theme

* `:root` custom properties (JavaFX 17+ có hỗ trợ hạn chế) → hoặc đặt biến qua class app, đổi theme bằng thay CSS.
* Tách `styles/app.css`, `styles/dark.css`; toggle bằng `scene.getStylesheets().setAll(...)`.

# Test & chất lượng

* **ViewModel/Service**: JUnit + Mockito (test đồng bộ vì không dính FX thread).
* **UI**: TestFX cho smoke test (không lạm dụng).
* **Static analysis**: Checkstyle, SpotBugs, PMD, ErrorProne. Format bằng Spotless/Google Java Format.
* **CI**: chạy test + packaging; ký app nếu phát hành.

# Gradle & đóng gói (gợi ý ngắn)

```groovy
plugins {
  id 'application'
  id 'org.openjfx.javafxplugin' version 'LATEST' // ghim phiên bản bạn dùng
}
javafx {
  version = '21'           // đồng bộ JDK bạn dùng
  modules = [ 'javafx.controls', 'javafx.fxml' ]
}
application {
  mainClass = 'com.myapp.App'
}
```

Đóng gói native:

```
jpackage --name MyApp --input build/libs --main-jar myapp-all.jar \
  --type dmg|msi|pkg --icon icons/app.icns
```

# Checklist triển khai nhanh

* [ ] Chọn DI (Afterburner.fx/Guice) và tạo `Injector`.
* [ ] Tạo `Navigator`, `Route`, `StageNavigator`.
* [ ] Tạo `Asyncs` + `ExecutorService` dùng chung.
* [ ] Định nghĩa `Service` qua interface; viết impl riêng.
* [ ] Mỗi màn hình: `FeatureView.fxml` + `Controller` mỏng + `ViewModel`.
* [ ] Ràng buộc tất cả UI bằng `Property/Binding`.
* [ ] Thêm i18n, CSS, dark mode.
* [ ] Viết test cho ViewModel/Service.
* [ ] Thiết lập Checkstyle/SpotBugs/Spotless.
* [ ] jpackage phát hành.

Nếu bạn muốn, mình có thể biến khung này thành skeleton project (MVVM + DI nhẹ + 2 màn hình mẫu + Gradle + jpackage) để bạn dùng ngay.
