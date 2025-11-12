package com.noteflix.pcm.ui.viewmodel;

import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.core.theme.ThemeManager;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

/**
 * ViewModel for Settings Page
 *
 * <p>Manages application settings state and actions: - Theme selection - Language/locale - LLM
 * provider configuration - Other preferences
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class SettingsViewModel extends BaseViewModel {

  // Dependencies
  private final ThemeManager themeManager;

  // Properties
  private final BooleanProperty darkTheme = new SimpleBooleanProperty(false);
  private final StringProperty selectedLanguage = new SimpleStringProperty("en");
  private final StringProperty llmProvider = new SimpleStringProperty("openai");
  private final StringProperty llmModel = new SimpleStringProperty("gpt-3.5-turbo");
  private final StringProperty apiKey = new SimpleStringProperty("");

  // Available options
  private final ObservableList<String> availableLanguages =
      FXCollections.observableArrayList("en", "vi");
  private final ObservableList<String> availableProviders =
      FXCollections.observableArrayList("openai", "anthropic", "ollama");

  /** Constructor with dependency injection */
  public SettingsViewModel(ThemeManager themeManager) {
    this.themeManager = themeManager;
    loadSettings();
    log.info("SettingsViewModel initialized");
  }

  /** Default constructor */
  public SettingsViewModel() {
    this(ThemeManager.getInstance());
  }

  // ========== Commands ==========

  /** Load current settings */
  private void loadSettings() {
    // Load theme setting
    darkTheme.set(themeManager.isDarkTheme());

    // TODO: Load other settings from preferences/config file
    log.debug("Settings loaded");
  }

  /** Save settings */
  public void saveSettings() {
    setBusy(true);
    clearError();

    try {
      // Apply theme
      themeManager.setTheme(darkTheme.get());

      // Apply language
      I18n.setLocale(selectedLanguage.get());

      // TODO: Save LLM configuration
      // TODO: Persist settings to file/database

      log.info("Settings saved successfully");
    } catch (Exception e) {
      setError("Failed to save settings", e);
    } finally {
      setBusy(false);
    }
  }

  /** Toggle theme */
  public void toggleTheme() {
    darkTheme.set(!darkTheme.get());
    themeManager.setTheme(darkTheme.get());
    log.info("Theme toggled to: {}", darkTheme.get() ? "dark" : "light");
  }

  /** Reset to defaults */
  public void resetToDefaults() {
    darkTheme.set(false);
    selectedLanguage.set("en");
    llmProvider.set("openai");
    llmModel.set("gpt-3.5-turbo");
    apiKey.set("");
    saveSettings();
    log.info("Settings reset to defaults");
  }

  // ========== Property Accessors ==========

  public BooleanProperty darkThemeProperty() {
    return darkTheme;
  }

  public boolean isDarkTheme() {
    return darkTheme.get();
  }

  public void setDarkTheme(boolean dark) {
    darkTheme.set(dark);
  }

  public StringProperty selectedLanguageProperty() {
    return selectedLanguage;
  }

  public String getSelectedLanguage() {
    return selectedLanguage.get();
  }

  public void setSelectedLanguage(String language) {
    selectedLanguage.set(language);
  }

  public StringProperty llmProviderProperty() {
    return llmProvider;
  }

  public String getLlmProvider() {
    return llmProvider.get();
  }

  public void setLlmProvider(String provider) {
    llmProvider.set(provider);
  }

  public StringProperty llmModelProperty() {
    return llmModel;
  }

  public String getLlmModel() {
    return llmModel.get();
  }

  public void setLlmModel(String model) {
    llmModel.set(model);
  }

  public StringProperty apiKeyProperty() {
    return apiKey;
  }

  public String getApiKey() {
    return apiKey.get();
  }

  public void setApiKey(String key) {
    apiKey.set(key);
  }

  public ObservableList<String> getAvailableLanguages() {
    return availableLanguages;
  }

  public ObservableList<String> getAvailableProviders() {
    return availableProviders;
  }

  // ========== Lifecycle ==========

  @Override
  public void onActivate() {
    super.onActivate();
    loadSettings();
  }
}

