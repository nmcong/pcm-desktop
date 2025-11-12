package com.noteflix.pcm.core.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import lombok.extern.slf4j.Slf4j;

/**
 * Internationalization (i18n) support
 *
 * <p>Provides centralized access to localized messages. Follows: - Single source of truth for UI
 * text - Easy language switching - Support for parameterized messages
 *
 * <p>Usage: <code>
 *   String title = I18n.get("app.title");
 *   String msg = I18n.format("time.minutes.ago", 5); // "5 minutes ago"
 * </code>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class I18n {

  private static final String BUNDLE_BASE_NAME = "i18n.messages";
  private static ResourceBundle bundle;
  private static Locale currentLocale;

  static {
    // Initialize with system default locale
    setLocale(Locale.getDefault());
  }

  /**
   * Set the current locale
   *
   * @param locale Locale to use
   */
  public static void setLocale(Locale locale) {
    currentLocale = locale;
    bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
    log.info("Locale set to: {}", locale.getDisplayName());
  }

  /**
   * Set locale by language code
   *
   * @param languageCode Language code (e.g., "en", "vi")
   */
  public static void setLocale(String languageCode) {
    Locale locale = Locale.forLanguageTag(languageCode);
    setLocale(locale);
  }

  /**
   * Get current locale
   *
   * @return Current locale
   */
  public static Locale getCurrentLocale() {
    return currentLocale;
  }

  /**
   * Get current ResourceBundle
   *
   * @return Current resource bundle
   */
  public static ResourceBundle getBundle() {
    return bundle;
  }

  /**
   * Get a localized message
   *
   * @param key Message key
   * @return Localized message
   */
  public static String get(String key) {
    try {
      return bundle.getString(key);
    } catch (Exception e) {
      log.warn("Missing i18n key: {}", key);
      return "!" + key + "!";
    }
  }

  /**
   * Get a localized message with default value
   *
   * @param key Message key
   * @param defaultValue Default value if key not found
   * @return Localized message or default value
   */
  public static String get(String key, String defaultValue) {
    try {
      return bundle.getString(key);
    } catch (Exception e) {
      log.debug("Using default value for missing key: {}", key);
      return defaultValue;
    }
  }

  /**
   * Get a formatted message with parameters
   *
   * @param key Message key
   * @param args Parameters for message formatting
   * @return Formatted message
   */
  public static String format(String key, Object... args) {
    try {
      String pattern = bundle.getString(key);
      return MessageFormat.format(pattern, args);
    } catch (Exception e) {
      log.warn("Failed to format message for key: {}", key, e);
      return "!" + key + "!";
    }
  }

  /**
   * Check if a key exists in the bundle
   *
   * @param key Message key
   * @return true if key exists
   */
  public static boolean hasKey(String key) {
    return bundle.containsKey(key);
  }

  // Convenience methods for common keys

  public static String appTitle() {
    return get("app.title");
  }

  public static String actionNew() {
    return get("action.new");
  }

  public static String actionOpen() {
    return get("action.open");
  }

  public static String actionSave() {
    return get("action.save");
  }

  public static String actionDelete() {
    return get("action.delete");
  }

  public static String actionCancel() {
    return get("action.cancel");
  }

  public static String actionConfirm() {
    return get("action.confirm");
  }

  public static String actionClose() {
    return get("action.close");
  }

  public static String actionRefresh() {
    return get("action.refresh");
  }

  public static String actionSearch() {
    return get("action.search");
  }

  public static String loading() {
    return get("message.loading");
  }

  public static String errorGeneric() {
    return get("message.error.generic");
  }
}
