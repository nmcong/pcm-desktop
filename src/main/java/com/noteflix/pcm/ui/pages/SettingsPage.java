package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.core.di.Injector;
import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.utils.UIFactory;
import com.noteflix.pcm.ui.viewmodel.SettingsViewModel;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Settings Page - Refactored (Example)
 *
 * <p>Demonstrates refactoring using:
 * - New constants (LayoutConstants, StyleConstants)
 * - UI utilities (UIFactory, LayoutHelper)
 * - Better code organization
 * - Reduced code duplication
 *
 * <p>Still uses BasePage for backward compatibility.
 * Future: Migrate to BaseView directly when navigation system is updated.
 *
 * <p>Architecture: MVVM
 * - View: Settings UI
 * - ViewModel: SettingsViewModel
 *
 * @author PCM Team
 * @version 2.0.0
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
  protected Node createMainContent() {
    VBox mainContent = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
    mainContent.getStyleClass().add("settings-content");
    LayoutHelper.setVGrow(mainContent);

    mainContent.getChildren().addAll(
        createAppearanceSection(),
        createDatabaseSection(),
        createNotificationSection(),
        createAdvancedSection());

    return mainContent;
  }

  @Override
  public void onActivate() {
    super.onActivate();
    viewModel.loadSettings();
  }

  private VBox createAppearanceSection() {
    VBox section = UIFactory.createCard();
    section.getStyleClass().add(StyleConstants.CARD);

    // Section header
    Label title = UIFactory.createSectionTitle(I18n.get("settings.appearance.title"));
    title.setGraphic(new FontIcon(Feather.MONITOR));
    title.setGraphicTextGap(LayoutConstants.SPACING_MD);

    // Theme selection with bidirectional binding
    HBox themeRow =
        createSettingRow(
            I18n.get("settings.appearance.theme"), I18n.get("settings.appearance.theme.desc"));
    ComboBox<String> themeCombo = new ComboBox<>();
    themeCombo.getItems().addAll(viewModel.getAvailableThemes());
    themeCombo.valueProperty().bindBidirectional(viewModel.selectedThemeProperty());
    themeRow.getChildren().add(themeCombo);

    // Language selection with bidirectional binding
    HBox languageRow =
        createSettingRow(
            I18n.get("settings.appearance.language"),
            I18n.get("settings.appearance.language.desc"));
    ComboBox<String> languageCombo = new ComboBox<>();
    languageCombo.getItems().addAll(viewModel.getAvailableLanguages());
    languageCombo.valueProperty().bindBidirectional(viewModel.selectedLanguageProperty());
    languageRow.getChildren().add(languageCombo);

    // Font size with bidirectional binding
    HBox fontRow =
        createSettingRow(
            I18n.get("settings.appearance.font.size"),
            I18n.get("settings.appearance.font.size.desc"));
    Slider fontSlider = new Slider(10, 18, 14);
    fontSlider.setShowTickLabels(true);
    fontSlider.setShowTickMarks(true);
    fontSlider.setMajorTickUnit(2);
    fontSlider.setPrefWidth(200);
    fontSlider.valueProperty().bindBidirectional(viewModel.fontSizeProperty());
    fontRow.getChildren().add(fontSlider);

    // Sidebar width with bidirectional binding
    HBox sidebarRow =
        createSettingRow(
            I18n.get("settings.appearance.sidebar.width"),
            I18n.get("settings.appearance.sidebar.width.desc"));
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
    VBox section = UIFactory.createCard();
    section.getStyleClass().add(StyleConstants.CARD);

    // Section header
    Label title = UIFactory.createSectionTitle(I18n.get("settings.database.title"));
    title.setGraphic(new FontIcon(Feather.DATABASE));
    title.setGraphicTextGap(LayoutConstants.SPACING_MD);

    HBox dbPathRow =
        createSettingRow(
            I18n.get("settings.database.path"), I18n.get("settings.database.path.desc"));
    Label dbPathLabel = new Label();
    dbPathLabel.textProperty().bind(viewModel.databasePathProperty());
    Button changeDbPathBtn = UIFactory.createSecondaryButton(
        I18n.get("settings.database.path.change"),
        viewModel::changeDatabasePath);
    dbPathRow.getChildren().addAll(dbPathLabel, changeDbPathBtn);

    HBox migrateRow =
        createSettingRow(
            I18n.get("settings.database.migrate"), I18n.get("settings.database.migrate.desc"));
    Button migrateBtn = UIFactory.createSecondaryButton(
        I18n.get("settings.database.migrate.run"),
        viewModel::runMigrations);
    migrateRow.getChildren().add(migrateBtn);

    section.getChildren().addAll(title, dbPathRow, migrateRow);
    return section;
  }

  private VBox createNotificationSection() {
    VBox section = UIFactory.createCard();
    section.getStyleClass().add(StyleConstants.CARD);

    // Section header
    Label title = UIFactory.createSectionTitle(I18n.get("settings.notifications.title"));
    title.setGraphic(new FontIcon(Feather.BELL));
    title.setGraphicTextGap(LayoutConstants.SPACING_MD);

    HBox emailNotifRow =
        createSettingRow(
            I18n.get("settings.notifications.email"),
            I18n.get("settings.notifications.email.desc"));
    CheckBox emailCheck = new CheckBox();
    emailCheck.selectedProperty().bindBidirectional(viewModel.emailNotificationsEnabledProperty());
    emailNotifRow.getChildren().add(emailCheck);

    section.getChildren().addAll(title, emailNotifRow);
    return section;
  }

  private VBox createAdvancedSection() {
    VBox section = UIFactory.createCard();
    section.getStyleClass().add(StyleConstants.CARD);

    // Section header
    Label title = UIFactory.createSectionTitle(I18n.get("settings.advanced.title"));
    title.setGraphic(new FontIcon(Feather.CODE));
    title.setGraphicTextGap(LayoutConstants.SPACING_MD);

    HBox resetRow =
        createSettingRow(
            I18n.get("settings.advanced.reset"), I18n.get("settings.advanced.reset.desc"));
    Button resetBtn = UIFactory.createDangerButton(
        I18n.get("settings.advanced.reset.button"),
        viewModel::resetSettings);
    resetRow.getChildren().add(resetBtn);

    section.getChildren().addAll(title, resetRow);
    return section;
  }

  private HBox createSettingRow(String title, String description) {
    HBox row = LayoutHelper.createHBox(LayoutConstants.SPACING_MD);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("setting-row");

    // Text content
    VBox textContent = LayoutHelper.createVBox(LayoutConstants.SPACING_XS);
    
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add(Styles.TEXT_BOLD);
    
    Label descLabel = UIFactory.createMutedLabel(description);
    descLabel.getStyleClass().add(Styles.TEXT_SMALL);
    
    textContent.getChildren().addAll(titleLabel, descLabel);

    // Spacer
    Region spacer = UIFactory.createHorizontalSpacer();

    row.getChildren().addAll(textContent, spacer);
    return row;
  }
}
