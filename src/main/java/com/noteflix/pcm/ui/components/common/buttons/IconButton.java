package com.noteflix.pcm.ui.components.common.buttons;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.Ikon;

/**
 * Icon Button Component
 *
 * <p>A button that displays only an icon, no text.
 * Useful for toolbar actions, close buttons, etc.
 *
 * <p>Features:
 * - Configurable icon and size
 * - Optional tooltip
 * - Flat or styled appearance
 * - Consistent sizing
 *
 * <p>Usage:
 * <pre>{@code
 * IconButton addBtn = new IconButton(Octicons.PLUS_16, "Add Item");
 * addBtn.setOnAction(e -> handleAdd());
 * }</pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class IconButton extends Button {

  @Getter private final FontIcon icon;

  /**
   * Create icon button with default size
   *
   * @param icon Icon to display
   * @param tooltip Tooltip text (optional)
   */
  public IconButton(Ikon icon, String tooltip) {
    this(icon, tooltip, LayoutConstants.ICON_SIZE_MD);
  }

  /**
   * Create icon button with custom icon size
   *
   * @param icon Icon to display
   * @param tooltip Tooltip text (optional)
   * @param iconSize Icon size in pixels
   */
  public IconButton(Ikon icon, String tooltip, int iconSize) {
    super();

    // Create and configure icon
    this.icon = new FontIcon(icon);
    this.icon.setIconSize(iconSize);
    setGraphic(this.icon);

    // Default styling
    getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, StyleConstants.ICON_BTN);

    // Set tooltip if provided
    if (tooltip != null && !tooltip.isEmpty()) {
      setTooltip(new Tooltip(tooltip));
    }

    // Fixed size for icon buttons
    setPrefSize(LayoutConstants.ICON_BUTTON_SIZE, LayoutConstants.ICON_BUTTON_SIZE);
    setMinSize(LayoutConstants.ICON_BUTTON_SIZE, LayoutConstants.ICON_BUTTON_SIZE);
    setMaxSize(LayoutConstants.ICON_BUTTON_SIZE, LayoutConstants.ICON_BUTTON_SIZE);
  }

  /**
   * Set icon size
   *
   * @param size Icon size in pixels
   * @return this for chaining
   */
  public IconButton withIconSize(int size) {
    icon.setIconSize(size);
    return this;
  }

  /**
   * Apply accent style (colored)
   *
   * @return this for chaining
   */
  public IconButton withAccent() {
    getStyleClass().remove(Styles.FLAT);
    getStyleClass().add(Styles.ACCENT);
    return this;
  }

  /**
   * Apply success style (green)
   *
   * @return this for chaining
   */
  public IconButton withSuccess() {
    getStyleClass().remove(Styles.FLAT);
    getStyleClass().add(Styles.SUCCESS);
    return this;
  }

  /**
   * Apply danger style (red)
   *
   * @return this for chaining
   */
  public IconButton withDanger() {
    getStyleClass().remove(Styles.FLAT);
    getStyleClass().add(Styles.DANGER);
    return this;
  }

  /**
   * Apply warning style (orange)
   *
   * @return this for chaining
   */
  public IconButton withWarning() {
    getStyleClass().remove(Styles.FLAT);
    getStyleClass().add(Styles.WARNING);
    return this;
  }

  /**
   * Set action handler
   *
   * @param action Action to perform on click
   * @return this for chaining
   */
  public IconButton withAction(Runnable action) {
    if (action != null) {
      setOnAction(e -> action.run());
    }
    return this;
  }

  /**
   * Change icon
   *
   * @param newIcon New icon
   * @return this for chaining
   */
  public IconButton withIcon(Ikon newIcon) {
    icon.setIconCode(newIcon);
    return this;
  }

  /**
   * Update tooltip
   *
   * @param tooltip New tooltip text
   * @return this for chaining
   */
  public IconButton withTooltip(String tooltip) {
    if (tooltip != null && !tooltip.isEmpty()) {
      setTooltip(new Tooltip(tooltip));
    }
    return this;
  }
}

