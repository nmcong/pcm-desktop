package com.noteflix.pcm.ui.utils;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

/**
 * UI Factory for creating common UI components
 *
 * <p>Provides factory methods for creating frequently used UI elements
 * with consistent styling and behavior.
 *
 * <p>Benefits:
 * - Consistency across the application
 * - Reduced code duplication
 * - Easier to update styles globally
 * - Cleaner code in pages/components
 *
 * @author PCM Team
 * @version 2.0.0
 */
public final class UIFactory {

  private UIFactory() {
    throw new UnsupportedOperationException("Utility class");
  }

  // ===== BUTTONS =====

  /**
   * Create primary button (accent color)
   *
   * @param text Button text
   * @param action Action to perform
   * @return Primary button
   */
  public static Button createPrimaryButton(String text, Runnable action) {
    Button button = new Button(text);
    button.getStyleClass().add(Styles.ACCENT);
    button.setMinWidth(LayoutConstants.BUTTON_MIN_WIDTH);
    if (action != null) {
      button.setOnAction(e -> action.run());
    }
    return button;
  }

  /**
   * Create primary button with icon
   *
   * @param text Button text
   * @param icon Icon
   * @param action Action to perform
   * @return Primary button with icon
   */
  public static Button createPrimaryButton(String text, Octicons icon, Runnable action) {
    Button button = createPrimaryButton(text, action);
    button.setGraphic(new FontIcon(icon));
    return button;
  }

  /**
   * Create secondary button
   *
   * @param text Button text
   * @param action Action to perform
   * @return Secondary button
   */
  public static Button createSecondaryButton(String text, Runnable action) {
    Button button = new Button(text);
    button.getStyleClass().add(Styles.BUTTON_OUTLINED);
    button.setMinWidth(LayoutConstants.BUTTON_MIN_WIDTH);
    if (action != null) {
      button.setOnAction(e -> action.run());
    }
    return button;
  }

  /**
   * Create icon button
   *
   * @param icon Icon
   * @param tooltip Tooltip text
   * @param action Action to perform
   * @return Icon button
   */
  public static Button createIconButton(Octicons icon, String tooltip, Runnable action) {
    Button button = new Button();
    FontIcon iconNode = new FontIcon(icon);
    iconNode.setIconSize(LayoutConstants.ICON_SIZE_MD);
    button.setGraphic(iconNode);
    button.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, StyleConstants.ICON_BTN);
    
    if (tooltip != null) {
      button.setTooltip(new Tooltip(tooltip));
    }
    
    if (action != null) {
      button.setOnAction(e -> action.run());
    }
    
    return button;
  }

  /**
   * Create success button (green)
   *
   * @param text Button text
   * @param action Action to perform
   * @return Success button
   */
  public static Button createSuccessButton(String text, Runnable action) {
    Button button = new Button(text);
    button.getStyleClass().add(Styles.SUCCESS);
    button.setMinWidth(LayoutConstants.BUTTON_MIN_WIDTH);
    if (action != null) {
      button.setOnAction(e -> action.run());
    }
    return button;
  }

  /**
   * Create danger button (red)
   *
   * @param text Button text
   * @param action Action to perform
   * @return Danger button
   */
  public static Button createDangerButton(String text, Runnable action) {
    Button button = new Button(text);
    button.getStyleClass().add(Styles.DANGER);
    button.setMinWidth(LayoutConstants.BUTTON_MIN_WIDTH);
    if (action != null) {
      button.setOnAction(e -> action.run());
    }
    return button;
  }

  // ===== LABELS =====

  /**
   * Create title label (large, bold)
   *
   * @param text Label text
   * @return Title label
   */
  public static Label createTitleLabel(String text) {
    Label label = new Label(text);
    label.getStyleClass().addAll(Styles.TITLE_3, StyleConstants.PAGE_TITLE);
    label.setStyle("-fx-font-weight: bold;");
    return label;
  }

  /**
   * Create section title label
   *
   * @param text Label text
   * @return Section title label
   */
  public static Label createSectionTitle(String text) {
    Label label = new Label(text);
    label.getStyleClass().addAll(Styles.TITLE_4, StyleConstants.SECTION_TITLE);
    label.setStyle("-fx-font-weight: bold;");
    return label;
  }

  /**
   * Create muted label (secondary text)
   *
   * @param text Label text
   * @return Muted label
   */
  public static Label createMutedLabel(String text) {
    Label label = new Label(text);
    label.getStyleClass().addAll(Styles.TEXT_MUTED, StyleConstants.TEXT_MUTED);
    return label;
  }

  // ===== LAYOUT HELPERS =====

  /**
   * Create horizontal spacer (grows to fill space)
   *
   * @return Horizontal spacer
   */
  public static Region createHorizontalSpacer() {
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    spacer.getStyleClass().add(StyleConstants.SPACER);
    return spacer;
  }

  /**
   * Create vertical spacer (grows to fill space)
   *
   * @return Vertical spacer
   */
  public static Region createVerticalSpacer() {
    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);
    spacer.getStyleClass().add(StyleConstants.SPACER);
    return spacer;
  }

  /**
   * Create separator
   *
   * @return Separator
   */
  public static Separator createSeparator() {
    Separator separator = new Separator();
    separator.getStyleClass().add(StyleConstants.SEPARATOR);
    return separator;
  }

  // ===== SECTION HEADERS =====

  /**
   * Create section header with title and optional action button
   *
   * @param title Section title
   * @param icon Title icon (optional)
   * @param actionButton Action button (optional)
   * @return Section header
   */
  public static HBox createSectionHeader(String title, Octicons icon, Button actionButton) {
    HBox header = new HBox(LayoutConstants.SPACING_SM);
    header.setAlignment(Pos.CENTER_LEFT);

    // Icon (if provided)
    if (icon != null) {
      FontIcon iconNode = new FontIcon(icon);
      iconNode.setIconSize(LayoutConstants.ICON_SIZE_MD);
      header.getChildren().add(iconNode);
    }

    // Title
    Label titleLabel = createSectionTitle(title);
    header.getChildren().add(titleLabel);

    // Action button (if provided)
    if (actionButton != null) {
      Region spacer = createHorizontalSpacer();
      header.getChildren().addAll(spacer, actionButton);
    }

    return header;
  }

  /**
   * Create section header with title only
   *
   * @param title Section title
   * @return Section header
   */
  public static HBox createSectionHeader(String title) {
    return createSectionHeader(title, null, null);
  }

  // ===== CONTAINERS =====

  /**
   * Create card container
   *
   * @return Card container
   */
  public static VBox createCard() {
    VBox card = new VBox(LayoutConstants.SPACING_MD);
    card.getStyleClass().add(StyleConstants.CARD);
    card.setPadding(LayoutConstants.PADDING_CARD);
    return card;
  }

  /**
   * Create card with title
   *
   * @param title Card title
   * @param content Card content
   * @return Card with title and content
   */
  public static VBox createCard(String title, Node content) {
    VBox card = createCard();
    
    Label titleLabel = createSectionTitle(title);
    card.getChildren().add(titleLabel);
    
    if (content != null) {
      card.getChildren().addAll(createSeparator(), content);
    }
    
    return card;
  }

  // ===== TOOLBAR =====

  /**
   * Create toolbar with actions
   *
   * @param actions Action buttons
   * @return Toolbar
   */
  public static HBox createToolbar(Node... actions) {
    HBox toolbar = new HBox(LayoutConstants.SPACING_MD);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    toolbar.getChildren().addAll(actions);
    return toolbar;
  }
}

