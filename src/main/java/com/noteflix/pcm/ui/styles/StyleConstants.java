package com.noteflix.pcm.ui.styles;

/**
 * CSS Style Class Constants
 *
 * <p>Centralized constants for CSS class names used throughout the UI.
 * Using constants instead of magic strings improves:
 * - Type safety
 * - Refactoring support
 * - Code completion
 * - Consistency
 *
 * @author PCM Team
 * @version 2.0.0
 */
public final class StyleConstants {

  private StyleConstants() {
    throw new UnsupportedOperationException("Utility class");
  }

  // ===== LAYOUT CLASSES =====

  public static final String ROOT = "root";
  public static final String PAGE_CONTAINER = "page-container";
  public static final String CONTENT_AREA = "content-area";
  public static final String CONTENT_WRAPPER = "content-wrapper";
  public static final String RIGHT_SIDE = "right-side";

  // ===== NAVIGATION CLASSES =====

  public static final String SIDEBAR = "sidebar";
  public static final String SIDEBAR_FULL_HEIGHT = "sidebar-full-height";
  public static final String SIDEBAR_MENU_ITEM = "sidebar-menu-item";
  public static final String APP_HEADER = "app-header";
  public static final String BREADCRUMB = "breadcrumb";
  public static final String NAV_BAR = "nav-bar";

  // ===== COMPONENT CLASSES =====

  public static final String CARD = "card";
  public static final String STAT_CARD = "stat-card";
  public static final String INFO_CARD = "info-card";
  public static final String LIST_ITEM = "list-item";
  public static final String ICON_BTN = "icon-btn";

  // ===== PAGE SPECIFIC CLASSES =====

  public static final String AI_CHAT_PAGE = "ai-chat-page";
  public static final String AI_CHAT_MAIN = "ai-chat-main";
  public static final String CHAT_SIDEBAR = "chat-sidebar";
  public static final String CHAT_SESSIONS_LIST = "chat-sessions-list";
  public static final String CHAT_MESSAGES = "chat-messages";
  public static final String CHAT_INPUT_AREA = "chat-input-area";

  // ===== TEXT CLASSES =====

  public static final String PAGE_HEADER = "page-header";
  public static final String PAGE_TITLE = "page-title";
  public static final String PAGE_DESCRIPTION = "page-description";
  public static final String SECTION_TITLE = "section-title";
  public static final String TEXT_MUTED = "text-muted";
  public static final String TEXT_SMALL = "text-small";

  // ===== FORM CLASSES =====

  public static final String FORM_FIELD = "form-field";
  public static final String FORM_LABEL = "form-label";
  public static final String FORM_ERROR = "form-error";
  public static final String SEARCH_INPUT = "search-input";
  public static final String SEARCH_BOX = "search-box";

  // ===== STATE CLASSES =====

  public static final String LOADING = "loading";
  public static final String DISABLED = "disabled";
  public static final String ACTIVE = "active";
  public static final String SELECTED = "selected";
  public static final String ERROR = "error";

  // ===== THEME CLASSES =====

  public static final String DARK_THEME = "dark-theme";
  public static final String LIGHT_THEME = "light-theme";

  // ===== UTILITY CLASSES =====

  public static final String SPACER = "spacer";
  public static final String SEPARATOR = "separator";
  public static final String HIDDEN = "hidden";
  public static final String VISIBLE = "visible";
}

