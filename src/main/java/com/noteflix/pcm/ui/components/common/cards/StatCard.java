package com.noteflix.pcm.ui.components.common.cards;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.styles.ColorConstants;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.UIFactory;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.Ikon;

/**
 * Stat Card Component
 *
 * <p>Card for displaying statistics with a large value and optional trend indicator.
 *
 * <p>Features:
 * - Label (description)
 * - Large value display
 * - Optional subtitle/trend
 * - Optional icon
 * - Color-coded (accent, success, warning, danger)
 *
 * <p>Usage:
 * <pre>{@code
 * StatCard activeUsers = new StatCard("Active Users", "1,234")
 *     .withSubtitle("+12% from last week")
 *     .withIcon(Octicons.PERSON_16)
 *     .withColor(ColorConstants.COLOR_SUCCESS);
 * }</pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class StatCard extends VBox {

  @Getter private final String label;
  @Getter private String value;

  private Label labelLabel;
  private Label valueLabel;
  private Label subtitleLabel;
  private FontIcon icon;
  private String colorVar = ColorConstants.COLOR_ACCENT;

  /**
   * Create stat card
   *
   * @param label Label text (e.g., "Active Users")
   * @param value Value to display (e.g., "1,234")
   */
  public StatCard(String label, String value) {
    super(LayoutConstants.SPACING_SM);
    this.label = label;
    this.value = value;

    // Styling
    getStyleClass().addAll(StyleConstants.CARD, "stat-card");
    setPadding(LayoutConstants.PADDING_CARD);
    HBox.setHgrow(this, Priority.ALWAYS);

    // Build UI
    buildUI();
  }

  /**
   * Build card UI
   */
  private void buildUI() {
    // Label
    labelLabel = UIFactory.createMutedLabel(label);
    labelLabel.getStyleClass().add(Styles.TEXT_SMALL);

    // Value
    valueLabel = new Label(value);
    valueLabel.getStyleClass().add(Styles.TITLE_2);
    valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorVar + ";");

    getChildren().addAll(labelLabel, valueLabel);
  }

  /**
   * Set subtitle (trend or additional info)
   *
   * @param subtitle Subtitle text
   * @return this for chaining
   */
  public StatCard withSubtitle(String subtitle) {
    if (subtitleLabel == null) {
      subtitleLabel = UIFactory.createMutedLabel(subtitle);
      subtitleLabel.getStyleClass().add(Styles.TEXT_SMALL);
      getChildren().add(subtitleLabel);
    } else {
      subtitleLabel.setText(subtitle);
    }
    return this;
  }

  /**
   * Set icon
   *
   * @param iconCode Icon to display
   * @return this for chaining
   */
  public StatCard withIcon(Ikon iconCode) {
    if (icon == null) {
      icon = new FontIcon(iconCode);
      icon.setIconSize(LayoutConstants.ICON_SIZE_LG);
      
      // Add icon to layout (top-right corner)
      HBox headerRow = new HBox(LayoutConstants.SPACING_SM);
      headerRow.setAlignment(Pos.CENTER_LEFT);
      headerRow.getChildren().addAll(labelLabel, UIFactory.createHorizontalSpacer(), icon);
      
      // Replace labelLabel with headerRow
      getChildren().remove(labelLabel);
      getChildren().add(0, headerRow);
    } else {
      icon.setIconCode(iconCode);
    }
    return this;
  }

  /**
   * Set color for value
   *
   * @param colorVar CSS color variable (from ColorConstants)
   * @return this for chaining
   */
  public StatCard withColor(String colorVar) {
    this.colorVar = colorVar;
    valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorVar + ";");
    return this;
  }

  /**
   * Apply accent color (blue)
   *
   * @return this for chaining
   */
  public StatCard asAccent() {
    return withColor(ColorConstants.COLOR_ACCENT);
  }

  /**
   * Apply success color (green)
   *
   * @return this for chaining
   */
  public StatCard asSuccess() {
    return withColor(ColorConstants.COLOR_SUCCESS);
  }

  /**
   * Apply warning color (orange)
   *
   * @return this for chaining
   */
  public StatCard asWarning() {
    return withColor(ColorConstants.COLOR_WARNING);
  }

  /**
   * Apply danger color (red)
   *
   * @return this for chaining
   */
  public StatCard asDanger() {
    return withColor(ColorConstants.COLOR_DANGER);
  }

  /**
   * Update value
   *
   * @param newValue New value to display
   * @return this for chaining
   */
  public StatCard updateValue(String newValue) {
    this.value = newValue;
    valueLabel.setText(newValue);
    return this;
  }

  /**
   * Set trend indicator with direction
   *
   * @param direction Trend direction (UP, DOWN, NEUTRAL)
   * @param changeText Change text (e.g., "+12%")
   * @return this for chaining
   */
  public StatCard withTrend(TrendDirection direction, String changeText) {
    String trendText = switch (direction) {
      case UP -> "↑ " + changeText;
      case DOWN -> "↓ " + changeText;
      case NEUTRAL -> "→ " + changeText;
    };
    
    String trendColor = switch (direction) {
      case UP -> ColorConstants.COLOR_SUCCESS;
      case DOWN -> ColorConstants.COLOR_DANGER;
      case NEUTRAL -> ColorConstants.COLOR_FG_MUTED;
    };
    
    if (subtitleLabel == null) {
      withSubtitle(trendText);
    } else {
      subtitleLabel.setText(trendText);
    }
    subtitleLabel.setStyle("-fx-text-fill: " + trendColor + ";");
    
    return this;
  }

  /**
   * Trend direction enum
   */
  public enum TrendDirection {
    UP,
    DOWN,
    NEUTRAL
  }
}

