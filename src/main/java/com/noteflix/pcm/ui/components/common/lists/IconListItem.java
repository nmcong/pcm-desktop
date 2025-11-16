package com.noteflix.pcm.ui.components.common.lists;

import com.noteflix.pcm.ui.styles.LayoutConstants;
import javafx.scene.control.Label;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.Ikon;

/**
 * Icon List Item Component
 *
 * <p>List item with icon.
 * Useful for menu items, file lists, etc.
 *
 * <p>Features:
 * - Icon
 * - Title and subtitle
 * - Optional badge
 *
 * <p>Usage:
 * <pre>{@code
 * IconListItem fileItem = new IconListItem(
 *     Octicons.FILE_16,
 *     "Document.pdf"
 * ).withSubtitle("2.4 MB")
 *  .withBadge("NEW");
 * }</pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class IconListItem extends ListItem {

  @Getter private final FontIcon icon;
  private Label badgeLabel;

  /**
   * Create icon list item
   *
   * @param iconCode Icon to display
   * @param title Title text
   */
  public IconListItem(Ikon iconCode, String title) {
    super();
    
    // Create icon
    this.icon = new FontIcon(iconCode);
    this.icon.setIconSize(LayoutConstants.ICON_SIZE_MD);
    
    // Set as leading node
    withLeading(icon);
    
    // Set title
    withTitle(title);
  }

  /**
   * Set icon size
   *
   * @param size Icon size in pixels
   * @return this for chaining
   */
  public IconListItem withIconSize(int size) {
    icon.setIconSize(size);
    return this;
  }

  /**
   * Set badge (e.g., "NEW", "3", "!")
   *
   * @param badgeText Badge text
   * @return this for chaining
   */
  public IconListItem withBadge(String badgeText) {
    if (badgeLabel == null) {
      badgeLabel = new Label(badgeText);
      badgeLabel.getStyleClass().add("badge");
      badgeLabel.setStyle(
          "-fx-padding: 2 8 2 8; " +
          "-fx-background-color: -color-accent-emphasis; " +
          "-fx-text-fill: white; " +
          "-fx-background-radius: " + LayoutConstants.RADIUS_XL + "px; " +
          "-fx-font-size: 10px; " +
          "-fx-font-weight: bold;");
      withTrailing(badgeLabel);
    } else {
      badgeLabel.setText(badgeText);
    }
    return this;
  }

  /**
   * Remove badge
   *
   * @return this for chaining
   */
  public IconListItem withoutBadge() {
    if (badgeLabel != null) {
      getChildren().remove(badgeLabel);
      badgeLabel = null;
      trailingNode = null;
    }
    return this;
  }

  /**
   * Set badge color
   *
   * @param colorVar CSS color variable
   * @return this for chaining
   */
  public IconListItem withBadgeColor(String colorVar) {
    if (badgeLabel != null) {
      badgeLabel.setStyle(
          "-fx-padding: 2 8 2 8; " +
          "-fx-background-color: " + colorVar + "; " +
          "-fx-text-fill: white; " +
          "-fx-background-radius: " + LayoutConstants.RADIUS_XL + "px; " +
          "-fx-font-size: 10px; " +
          "-fx-font-weight: bold;");
    }
    return this;
  }
}

