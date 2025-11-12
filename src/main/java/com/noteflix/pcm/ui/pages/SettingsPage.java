package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.core.di.Injector;
import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.ui.viewmodel.SettingsViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Settings page - MVVM Architecture
 * Uses SettingsViewModel for state management and business logic
 */
@Slf4j
public class SettingsPage extends BasePage {

  private final SettingsViewModel viewModel;

  public SettingsPage() {
    super(
        I18n.get("page.settings.title"),
        I18n.get("page.settings.subtitle"),
        new FontIcon(Feather.SETTINGS));
    this.viewModel = Injector.getInstance().get(SettingsViewModel.class);
    log.debug("SettingsPage initialized with ViewModel");
  }

  @Override
  protected VBox createMainContent() {
    VBox mainContent = new VBox(20);
    mainContent.getStyleClass().add("settings-content");
    VBox.setVgrow(mainContent, Priority.ALWAYS);

    mainContent.getChildren().add(createAppearanceSection());
    mainContent.getChildren().add(createDatabaseSection());
    mainContent.getChildren().add(createNotificationSection());
    mainContent.getChildren().add(createAdvancedSection());

    return mainContent;
  }

  @Override
  public void onPageActivated() {
    super.onPageActivated();
    viewModel.loadSettings();
  }

  private VBox createAppearanceSection() {
    VBox section = new VBox(16);
    section.getStyleClass().add("card");
    section.setPadding(new Insets(20));

    Label title = new Label(I18n.get("settings.appearance.title"));
    title.getStyleClass().addAll(Styles.TITLE_3);
    title.setGraphic(new FontIcon(Feather.MONITOR));
    title.setGraphicTextGap(12);

    // Theme selection with bidirectional binding
    HBox themeRow = createSettingRow(I18n.get("settings.appearance.theme"), I18n.get("settings.appearance.theme.desc"));
    ComboBox<String> themeCombo = new ComboBox<>();
    themeCombo.getItems().addAll(viewModel.getAvailableThemes());
    themeCombo.valueProperty().bindBidirectional(viewModel.selectedThemeProperty());
    themeRow.getChildren().add(themeCombo);

    // Language selection with bidirectional binding
    HBox languageRow = createSettingRow(I18n.get("settings.appearance.language"), I18n.get("settings.appearance.language.desc"));
    ComboBox<String> languageCombo = new ComboBox<>();
    languageCombo.getItems().addAll(viewModel.getAvailableLanguages());
    languageCombo.valueProperty().bindBidirectional(viewModel.selectedLanguageProperty());
    languageRow.getChildren().add(languageCombo);

    // Font size with bidirectional binding
    HBox fontRow = createSettingRow(I18n.get("settings.appearance.font.size"), I18n.get("settings.appearance.font.size.desc"));
    Slider fontSlider = new Slider(10, 18, 14);
    fontSlider.setShowTickLabels(true);
    fontSlider.setShowTickMarks(true);
    fontSlider.setMajorTickUnit(2);
    fontSlider.setPrefWidth(200);
    fontSlider.valueProperty().bindBidirectional(viewModel.fontSizeProperty());
    fontRow.getChildren().add(fontSlider);

    // Sidebar width with bidirectional binding
    HBox sidebarRow = createSettingRow(I18n.get("settings.appearance.sidebar.width"), I18n.get("settings.appearance.sidebar.width.desc"));
    Slider sidebarSlider = new Slider(200, 400, 280);
    sidebarSlider.setShowTickLabels(true);
    sidebarSlider.setShowTickMarks(true);
    sidebarSlider.setMajorTickUnit(50);
    sidebarSlider.setPrefWidth(200);
    sidebarSlider.valueProperty().bindBidirectional(viewModel.sidebarWidthProperty());
    sidebarRow.getChildren().add(sidebarSlider);

    section.getChildren().addAll(title, themeRow, languageRow, fontRow, sidebarRow);
    return section;
  }

  private VBox createDatabaseSection() {
    VBox section = new VBox(16);
    section.getStyleClass().add("card");
    section.setPadding(new Insets(20));

    Label title = new Label(I18n.get("settings.database.title"));
    title.getStyleClass().addAll(Styles.TITLE_3);
    title.setGraphic(new FontIcon(Feather.DATABASE));
    title.setGraphicTextGap(12);

    HBox dbPathRow = createSettingRow(I18n.get("settings.database.path"), I18n.get("settings.database.path.desc"));
    Label dbPathLabel = new Label();
    dbPathLabel.textProperty().bind(viewModel.databasePathProperty());
    Button changeDbPathBtn = new Button(I18n.get("settings.database.path.change"));
    changeDbPathBtn.setOnAction(e -> viewModel.changeDatabasePath());
    dbPathRow.getChildren().addAll(dbPathLabel, changeDbPathBtn);

    HBox migrateRow = createSettingRow(I18n.get("settings.database.migrate"), I18n.get("settings.database.migrate.desc"));
    Button migrateBtn = new Button(I18n.get("settings.database.migrate.run"));
    migrateBtn.setOnAction(e -> viewModel.runMigrations());
    migrateRow.getChildren().add(migrateBtn);

    section.getChildren().addAll(title, dbPathRow, migrateRow);
    return section;
  }

  private VBox createNotificationSection() {
    VBox section = new VBox(16);
    section.getStyleClass().add("card");
    section.setPadding(new Insets(20));

    Label title = new Label(I18n.get("settings.notifications.title"));
    title.getStyleClass().addAll(Styles.TITLE_3);
    title.setGraphic(new FontIcon(Feather.BELL));
    title.setGraphicTextGap(12);

    HBox emailNotifRow = createSettingRow(I18n.get("settings.notifications.email"), I18n.get("settings.notifications.email.desc"));
    CheckBox emailCheck = new CheckBox();
    emailCheck.selectedProperty().bindBidirectional(viewModel.emailNotificationsEnabledProperty());
    emailNotifRow.getChildren().add(emailCheck);

    section.getChildren().addAll(title, emailNotifRow);
    return section;
  }

  private VBox createAdvancedSection() {
    VBox section = new VBox(16);
    section.getStyleClass().add("card");
    section.setPadding(new Insets(20));

    Label title = new Label(I18n.get("settings.advanced.title"));
    title.getStyleClass().addAll(Styles.TITLE_3);
    title.setGraphic(new FontIcon(Feather.CODE));
    title.setGraphicTextGap(12);

    HBox resetRow = createSettingRow(I18n.get("settings.advanced.reset"), I18n.get("settings.advanced.reset.desc"));
    Button resetBtn = new Button(I18n.get("settings.advanced.reset.button"));
    resetBtn.getStyleClass().add(Styles.DANGER);
    resetBtn.setOnAction(e -> viewModel.resetSettings());
    resetRow.getChildren().add(resetBtn);

    section.getChildren().addAll(title, resetRow);
    return section;
  }

  private HBox createSettingRow(String title, String description) {
    HBox row = new HBox(12);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("setting-row");

    VBox textContent = new VBox(2);
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add(Styles.TEXT_BOLD);
    Label descLabel = new Label(description);
    descLabel.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
    textContent.getChildren().addAll(titleLabel, descLabel);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    row.getChildren().addAll(textContent, spacer);
    return row;
  }
}
