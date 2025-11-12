package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.core.di.Injector;
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
 * Settings Page - MVVM Refactored Example
 *
 * <p>This is an example of a page refactored to use MVVM pattern with ViewModel and property
 * binding.
 *
 * <p>Key improvements: - ViewModel holds all state (no state in page) - Property binding for
 * automatic UI updates - Thin controller (this class) - just UI wiring - Business logic in
 * ViewModel
 *
 * <p>Compare with SettingsPage.java to see the difference!
 *
 * @author PCM Team
 * @version 2.0.0 - MVVM Refactored
 */
@Slf4j
public class SettingsPageRefactored extends BasePage {

  private final SettingsViewModel viewModel;

  // UI Components
  private ComboBox<String> themeCombo;
  private ComboBox<String> languageCombo;
  private TextField llmProviderField;
  private TextField llmModelField;
  private PasswordField apiKeyField;
  private Button saveButton;

  /** Default constructor - gets ViewModel from DI */
  public SettingsPageRefactored() {
    this(Injector.getInstance().get(SettingsViewModel.class));
  }

  /** Constructor with ViewModel injection (for testing) */
  public SettingsPageRefactored(SettingsViewModel viewModel) {
    super(
        "Settings (MVVM)",
        "Configure your application preferences - MVVM Refactored Example",
        new FontIcon(Feather.SETTINGS));

    this.viewModel = viewModel;
    log.info("SettingsPageRefactored initialized with ViewModel");
  }

  @Override
  protected VBox createMainContent() {
    VBox mainContent = new VBox(20);
    mainContent.getStyleClass().add("settings-content");
    VBox.setVgrow(mainContent, Priority.ALWAYS);

    // Appearance settings
    mainContent.getChildren().add(createAppearanceSection());

    // Language settings
    mainContent.getChildren().add(createLanguageSection());

    // LLM Configuration
    mainContent.getChildren().add(createLLMSection());

    // Save button
    mainContent.getChildren().add(createSaveButton());

    return mainContent;
  }

  private VBox createAppearanceSection() {
    VBox section = new VBox(16);
    section.getStyleClass().add("card");
    section.setPadding(new Insets(20));

    Label title = new Label("Appearance");
    title.getStyleClass().addAll(Styles.TITLE_3);
    title.setGraphic(new FontIcon(Feather.MONITOR));
    title.setGraphicTextGap(12);

    // Theme selection with binding
    HBox themeRow = createSettingRow("Theme", "Choose between light and dark theme");
    themeCombo = new ComboBox<>();
    themeCombo.getItems().addAll("Light", "Dark");

    // ✅ BINDING: Bidirectional binding with custom converter
    themeCombo
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (newVal != null) {
                viewModel.setDarkTheme("Dark".equals(newVal));
              }
            });

    // Set initial value from ViewModel
    themeCombo.setValue(viewModel.isDarkTheme() ? "Dark" : "Light");

    themeRow.getChildren().add(themeCombo);

    section.getChildren().addAll(title, themeRow);
    return section;
  }

  private VBox createLanguageSection() {
    VBox section = new VBox(16);
    section.getStyleClass().add("card");
    section.setPadding(new Insets(20));

    Label title = new Label("Language");
    title.getStyleClass().addAll(Styles.TITLE_3);
    title.setGraphic(new FontIcon(Feather.GLOBE));
    title.setGraphicTextGap(12);

    // Language selection with binding
    HBox langRow = createSettingRow("Application Language", "Select your preferred language");
    languageCombo = new ComboBox<>();
    languageCombo.setItems(viewModel.getAvailableLanguages());

    // ✅ BINDING: Bidirectional binding
    languageCombo.valueProperty().bindBidirectional(viewModel.selectedLanguageProperty());

    langRow.getChildren().add(languageCombo);

    section.getChildren().addAll(title, langRow);
    return section;
  }

  private VBox createLLMSection() {
    VBox section = new VBox(16);
    section.getStyleClass().add("card");
    section.setPadding(new Insets(20));

    Label title = new Label("LLM Configuration");
    title.getStyleClass().addAll(Styles.TITLE_3);
    title.setGraphic(new FontIcon(Feather.CPU));
    title.setGraphicTextGap(12);

    // LLM Provider with binding
    HBox providerRow = createSettingRow("Provider", "LLM provider (openai, anthropic, ollama)");
    llmProviderField = new TextField();
    llmProviderField.setPrefWidth(200);

    // ✅ BINDING: Bidirectional text binding
    llmProviderField.textProperty().bindBidirectional(viewModel.llmProviderProperty());

    providerRow.getChildren().add(llmProviderField);

    // LLM Model with binding
    HBox modelRow = createSettingRow("Model", "Model name (e.g. gpt-3.5-turbo)");
    llmModelField = new TextField();
    llmModelField.setPrefWidth(200);

    // ✅ BINDING: Bidirectional text binding
    llmModelField.textProperty().bindBidirectional(viewModel.llmModelProperty());

    modelRow.getChildren().add(llmModelField);

    // API Key with binding
    HBox keyRow = createSettingRow("API Key", "Your LLM provider API key");
    apiKeyField = new PasswordField();
    apiKeyField.setPrefWidth(200);

    // ✅ BINDING: Bidirectional text binding
    apiKeyField.textProperty().bindBidirectional(viewModel.apiKeyProperty());

    keyRow.getChildren().add(apiKeyField);

    section.getChildren().addAll(title, providerRow, modelRow, keyRow);
    return section;
  }

  private HBox createSaveButton() {
    HBox buttonBox = new HBox(12);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.setPadding(new Insets(20, 0, 0, 0));

    Button resetButton = new Button("Reset to Defaults");
    resetButton.setGraphic(new FontIcon(Feather.ROTATE_CCW));
    resetButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
    resetButton.setOnAction(e -> viewModel.resetToDefaults());

    saveButton = new Button("Save Settings");
    saveButton.setGraphic(new FontIcon(Feather.SAVE));
    saveButton.getStyleClass().addAll(Styles.ACCENT);

    // ✅ BINDING: Disable button when busy
    saveButton.disableProperty().bind(viewModel.busyProperty());

    // ✅ COMMAND: Call ViewModel method
    saveButton.setOnAction(e -> viewModel.saveSettings());

    buttonBox.getChildren().addAll(resetButton, saveButton);
    return buttonBox;
  }

  private HBox createSettingRow(String label, String description) {
    HBox row = new HBox(16);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setPadding(new Insets(12, 0, 12, 0));

    VBox labelBox = new VBox(4);
    Label titleLabel = new Label(label);
    titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD);

    Label descLabel = new Label(description);
    descLabel.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);

    labelBox.getChildren().addAll(titleLabel, descLabel);
    HBox.setHgrow(labelBox, Priority.ALWAYS);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    row.getChildren().addAll(labelBox, spacer);
    return row;
  }

  @Override
  public void onPageActivated() {
    super.onPageActivated();
    // ✅ LIFECYCLE: Notify ViewModel
    viewModel.onActivate();
  }

  @Override
  public void onPageDeactivated() {
    super.onPageDeactivated();
    // ✅ LIFECYCLE: Notify ViewModel
    viewModel.onDeactivate();
  }
}

