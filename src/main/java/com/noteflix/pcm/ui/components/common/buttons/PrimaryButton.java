package com.noteflix.pcm.ui.components.common.buttons;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import javafx.scene.control.Button;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.Ikon;

/**
 * Primary Button Component
 *
 * <p>Main action button with accent color.
 * Use for primary actions like "Submit", "Save", "Create", etc.
 *
 * <p>Features:
 * - Accent color background
 * - Optional icon
 * - Minimum width for consistency
 * - Fluent API for configuration
 *
 * <p>Usage:
 * <pre>{@code
 * PrimaryButton submitBtn = new PrimaryButton("Submit")
 *     .withIcon(Octicons.CHECK_16)
 *     .withAction(this::handleSubmit);
 * }</pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class PrimaryButton extends Button {

  @Getter private FontIcon icon;

  /**
   * Create primary button with text only
   *
   * @param text Button text
   */
  public PrimaryButton(String text) {
    super(text);
    initializeStyle();
  }

  /**
   * Create primary button with text and icon
   *
   * @param text Button text
   * @param icon Icon to display
   */
  public PrimaryButton(String text, Ikon icon) {
    super(text);
    initializeStyle();
    withIcon(icon);
  }

  /**
   * Initialize default styling
   */
  private void initializeStyle() {
    getStyleClass().add(Styles.ACCENT);
    setMinWidth(LayoutConstants.BUTTON_MIN_WIDTH);
  }

  /**
   * Add icon to button
   *
   * @param iconCode Icon to display
   * @return this for chaining
   */
  public PrimaryButton withIcon(Ikon iconCode) {
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
  public PrimaryButton withIconSize(int size) {
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
  public PrimaryButton withAction(Runnable action) {
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
  public PrimaryButton withMinWidth(double width) {
    setMinWidth(width);
    return this;
  }

  /**
   * Make button large (increased height)
   *
   * @return this for chaining
   */
  public PrimaryButton asLarge() {
    getStyleClass().add(Styles.LARGE);
    setPrefHeight(LayoutConstants.BUTTON_HEIGHT_LG);
    return this;
  }

  /**
   * Make button small (decreased height)
   *
   * @return this for chaining
   */
  public PrimaryButton asSmall() {
    getStyleClass().add(Styles.SMALL);
    return this;
  }

  /**
   * Disable button
   *
   * @param disabled true to disable
   * @return this for chaining
   */
  public PrimaryButton withDisabled(boolean disabled) {
    setDisable(disabled);
    return this;
  }
}

