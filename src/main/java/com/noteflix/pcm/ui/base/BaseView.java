package com.noteflix.pcm.ui.base;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Enhanced Base View for all pages
 *
 * <p>Replaces legacy BasePage with improved:
 * - Better naming (View instead of Page)
 * - Uses constants for styling
 * - Template method pattern
 * - Lifecycle management
 * - Consistent structure
 *
 * <p>Follows SOLID principles:
 * - Single Responsibility: Only handles page structure
 * - Open/Closed: Open for extension via template methods
 * - Template Method Pattern for consistent page structure
 *
 * @author PCM Team
 * @version 2.0.0
 */
@Slf4j
@Getter
public abstract class BaseView extends VBox implements ViewLifecycle {

  protected final String pageTitle;
  protected final String pageDescription;
  protected final FontIcon pageIcon;

  /**
   * Constructor with page metadata
   *
   * @param title Page title
   * @param description Page description
   * @param icon Page icon
   */
  protected BaseView(String title, String description, FontIcon icon) {
    this.pageTitle = title;
    this.pageDescription = description;
    this.pageIcon = icon;

    initializeLayout();
    buildContent();
  }

  /**
   * Initialize base layout structure
   * Uses constants instead of magic numbers
   */
  private void initializeLayout() {
    setSpacing(LayoutConstants.SPACING_XL);
    setPadding(LayoutConstants.PADDING_PAGE);
    getStyleClass().add(StyleConstants.PAGE_CONTAINER);
    VBox.setVgrow(this, Priority.ALWAYS);

    log.debug("Initialized layout for page: {}", pageTitle);
  }

  /**
   * Build the page content using template method pattern
   */
  private void buildContent() {
    // Header section (if provided)
    var header = createPageHeader();
    if (header != null) {
      getChildren().add(header);
    }

    // Main content - implemented by subclasses
    Node mainContent = createMainContent();
    if (mainContent != null) {
      getChildren().add(mainContent);
    }

    // Footer section (if needed)
    var footer = createPageFooter();
    if (footer != null) {
      getChildren().add(footer);
    }
  }

  /**
   * Creates the page header with title and description
   * Can be overridden to customize header
   */
  protected VBox createPageHeader() {
    VBox header = new VBox(LayoutConstants.SPACING_SM);
    header.setAlignment(Pos.TOP_LEFT);
    header.getStyleClass().add(StyleConstants.PAGE_HEADER);

    // Title with icon
    Label titleLabel = new Label(pageTitle);
    titleLabel.setGraphic(pageIcon);
    titleLabel.getStyleClass().addAll(Styles.TITLE_1, StyleConstants.PAGE_TITLE);
    titleLabel.setStyle(
        "-fx-font-weight: bold; -fx-graphic-text-gap: " + LayoutConstants.SPACING_MD + "px;");

    // Description
    Label descriptionLabel = new Label(pageDescription);
    descriptionLabel.getStyleClass().addAll(Styles.TEXT_MUTED, StyleConstants.PAGE_DESCRIPTION);
    descriptionLabel.setWrapText(true);

    header.getChildren().addAll(titleLabel, descriptionLabel);
    return header;
  }

  /**
   * Abstract method for creating main content
   * Must be implemented by subclasses
   *
   * @return Main content node
   */
  protected abstract Node createMainContent();

  /**
   * Optional footer creation
   * Can be overridden by subclasses
   *
   * @return Footer node or null
   */
  protected Node createPageFooter() {
    return null; // No footer by default
  }

  // ===== LIFECYCLE METHODS =====

  /**
   * Called when page becomes active
   * Override to add custom behavior
   */
  @Override
  public void onActivate() {
    log.info("Page activated: {}", pageTitle);
    // Can be overridden by subclasses
  }

  /**
   * Called when page becomes inactive
   * Override to add cleanup logic
   */
  @Override
  public void onDeactivate() {
    log.debug("Page deactivated: {}", pageTitle);
    // Can be overridden by subclasses
  }

  /**
   * Cleanup resources
   * Override to cleanup listeners, tasks, etc.
   */
  @Override
  public void cleanup() {
    log.debug("Cleaning up page: {}", pageTitle);
    // Can be overridden by subclasses
  }

  // ===== CONVENIENCE METHODS =====

  /**
   * Set page as busy/loading
   *
   * @param busy true to disable interactions
   */
  protected void setBusy(boolean busy) {
    setDisable(busy);
    if (busy) {
      getStyleClass().add(StyleConstants.LOADING);
    } else {
      getStyleClass().remove(StyleConstants.LOADING);
    }
  }

  /**
   * Show error state
   *
   * @param hasError true to show error state
   */
  protected void setError(boolean hasError) {
    if (hasError) {
      getStyleClass().add(StyleConstants.ERROR);
    } else {
      getStyleClass().remove(StyleConstants.ERROR);
    }
  }
}

