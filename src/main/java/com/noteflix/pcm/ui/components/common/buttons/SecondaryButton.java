package com.noteflix.pcm.ui.components.common.buttons;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import javafx.scene.control.Button;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.Ikon;

/**
 * Secondary Button Component
 *
 * <p>Secondary action button with outline style.
 * Use for secondary actions like "Cancel", "Back", "Close", etc.
 *
 * <p>Features:
 * - Outline/border style (no filled background)
 * - Optional icon
 * - Minimum width for consistency
 * - Fluent API for configuration
 *
 * <p>Usage:
 * <pre>{@code
 * SecondaryButton cancelBtn = new SecondaryButton("Cancel")
 *     .withAction(this::handleCancel);
 * }</pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class SecondaryButton extends Button {

  @Getter private FontIcon icon;

  /**
   * Create secondary button with text only
   *
   * @param text Button text
   */
  public SecondaryButton(String text) {
    super(text);
    initializeStyle();
  }

  /**
   * Create secondary button with text and icon
   *
   * @param text Button text
   * @param icon Icon to display
   */
  public SecondaryButton(String text, Ikon icon) {
    super(text);
    initializeStyle();
    withIcon(icon);
  }

  /**
   * Initialize default styling
   */
  private void initializeStyle() {
    getStyleClass().add(Styles.BUTTON_OUTLINED);
    setMinWidth(LayoutConstants.BUTTON_MIN_WIDTH);
  }

  /**
   * Add icon to button
   *
   * @param iconCode Icon to display
   * @return this for chaining
   */
  public SecondaryButton withIcon(Ikon iconCode) {
    if (this.icon == null) {
      this.icon = new FontIcon(iconCode);
      this.icon.setIconSize(LayoutConstants.ICON_SIZE_MD);
      setGraphic(this.icon);
    } else {
      this.icon.setIconCode(iconCode);
    }
    return this;
  }

  /**
   * Set icon size
   *
   * @param size Icon size in pixels
   * @return this for chaining
   */
  public SecondaryButton withIconSize(int size) {
    if (icon != null) {
      icon.setIconSize(size);
    }
    return this;
  }

  /**
   * Set action handler
   *
   * @param action Action to perform on click
   * @return this for chaining
   */
  public SecondaryButton withAction(Runnable action) {
    if (action != null) {
      setOnAction(e -> action.run());
    }
    return this;
  }

  /**
   * Set minimum width
   *
   * @param width Minimum width
   * @return this for chaining
   */
  public SecondaryButton withMinWidth(double width) {
    setMinWidth(width);
    return this;
  }

  /**
   * Make button large (increased height)
   *
   * @return this for chaining
   */
  public SecondaryButton asLarge() {
    getStyleClass().add(Styles.LARGE);
    setPrefHeight(LayoutConstants.BUTTON_HEIGHT_LG);
    return this;
  }

  /**
   * Make button small (decreased height)
   *
   * @return this for chaining
   */
  public SecondaryButton asSmall() {
    getStyleClass().add(Styles.SMALL);
    return this;
  }

  /**
   * Disable button
   *
   * @param disabled true to disable
   * @return this for chaining
   */
  public SecondaryButton withDisabled(boolean disabled) {
    setDisable(disabled);
    return this;
  }

  /**
   * Apply as flat style (even more subtle)
   *
   * @return this for chaining
   */
  public SecondaryButton asFlat() {
    getStyleClass().remove(Styles.BUTTON_OUTLINED);
    getStyleClass().add(Styles.FLAT);
    return this;
  }
}

