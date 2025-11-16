package com.noteflix.pcm.ui.utils;

import com.noteflix.pcm.ui.styles.LayoutConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Layout Helper Utilities
 *
 * <p>Provides utility methods for common layout operations
 * and configurations.
 *
 * @author PCM Team
 * @version 2.0.0
 */
public final class LayoutHelper {

  private LayoutHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  // ===== VBOX HELPERS =====

  /**
   * Create VBox with default spacing
   *
   * @param children Child nodes
   * @return VBox with default spacing
   */
  public static VBox createVBox(Node... children) {
    return createVBox(LayoutConstants.SPACING_MD, children);
  }

  /**
   * Create VBox with custom spacing
   *
   * @param spacing Spacing between children
   * @param children Child nodes
   * @return VBox with custom spacing
   */
  public static VBox createVBox(double spacing, Node... children) {
    VBox vbox = new VBox(spacing);
    vbox.getChildren().addAll(children);
    return vbox;
  }

  /**
   * Create VBox with padding and spacing
   *
   * @param padding Padding
   * @param spacing Spacing
   * @param children Child nodes
   * @return VBox with padding and spacing
   */
  public static VBox createVBox(Insets padding, double spacing, Node... children) {
    VBox vbox = createVBox(spacing, children);
    vbox.setPadding(padding);
    return vbox;
  }

  // ===== HBOX HELPERS =====

  /**
   * Create HBox with default spacing
   *
   * @param children Child nodes
   * @return HBox with default spacing
   */
  public static HBox createHBox(Node... children) {
    return createHBox(LayoutConstants.SPACING_MD, children);
  }

  /**
   * Create HBox with custom spacing
   *
   * @param spacing Spacing between children
   * @param children Child nodes
   * @return HBox with custom spacing
   */
  public static HBox createHBox(double spacing, Node... children) {
    HBox hbox = new HBox(spacing);
    hbox.getChildren().addAll(children);
    return hbox;
  }

  /**
   * Create HBox with alignment
   *
   * @param alignment Alignment
   * @param spacing Spacing
   * @param children Child nodes
   * @return HBox with alignment and spacing
   */
  public static HBox createHBox(Pos alignment, double spacing, Node... children) {
    HBox hbox = createHBox(spacing, children);
    hbox.setAlignment(alignment);
    return hbox;
  }

  // ===== GROWTH HELPERS =====

  /**
   * Set node to grow horizontally
   *
   * @param node Node to grow
   */
  public static void setHGrow(Node node) {
    HBox.setHgrow(node, Priority.ALWAYS);
  }

  /**
   * Set node to grow vertically
   *
   * @param node Node to grow
   */
  public static void setVGrow(Node node) {
    VBox.setVgrow(node, Priority.ALWAYS);
  }

  /**
   * Set node to grow both horizontally and vertically
   *
   * @param node Node to grow
   */
  public static void setGrow(Node node) {
    setHGrow(node);
    setVGrow(node);
  }

  // ===== SPACING HELPERS =====

  /**
   * Apply default padding to a region
   *
   * @param region Region to apply padding
   */
  public static void applyDefaultPadding(Region region) {
    region.setPadding(LayoutConstants.PADDING_DEFAULT);
  }

  /**
   * Apply card padding to a region
   *
   * @param region Region to apply padding
   */
  public static void applyCardPadding(Region region) {
    region.setPadding(LayoutConstants.PADDING_CARD);
  }

  /**
   * Apply page padding to a region
   *
   * @param region Region to apply padding
   */
  public static void applyPagePadding(Region region) {
    region.setPadding(LayoutConstants.PADDING_PAGE);
  }

  // ===== SIZE HELPERS =====

  /**
   * Set preferred size
   *
   * @param region Region to set size
   * @param width Preferred width
   * @param height Preferred height
   */
  public static void setPrefSize(Region region, double width, double height) {
    region.setPrefWidth(width);
    region.setPrefHeight(height);
  }

  /**
   * Set minimum size
   *
   * @param region Region to set size
   * @param width Minimum width
   * @param height Minimum height
   */
  public static void setMinSize(Region region, double width, double height) {
    region.setMinWidth(width);
    region.setMinHeight(height);
  }

  /**
   * Set maximum size
   *
   * @param region Region to set size
   * @param width Maximum width
   * @param height Maximum height
   */
  public static void setMaxSize(Region region, double width, double height) {
    region.setMaxWidth(width);
    region.setMaxHeight(height);
  }

  /**
   * Set fixed size (min, pref, max all the same)
   *
   * @param region Region to set size
   * @param width Fixed width
   * @param height Fixed height
   */
  public static void setFixedSize(Region region, double width, double height) {
    setMinSize(region, width, height);
    setPrefSize(region, width, height);
    setMaxSize(region, width, height);
  }

  // ===== COMMON LAYOUTS =====

  /**
   * Create two-column layout with fixed left width
   *
   * @param left Left content (fixed width)
   * @param right Right content (grows to fill)
   * @param leftWidth Left column width
   * @return HBox with two columns
   */
  public static HBox createTwoColumnLayout(Node left, Node right, double leftWidth) {
    HBox layout = new HBox();
    
    // Left column with fixed width
    if (left instanceof Region leftRegion) {
      setFixedSize(leftRegion, leftWidth, -1);
    }
    
    // Right column grows to fill
    if (right instanceof Region) {
      setHGrow(right);
    }
    
    layout.getChildren().addAll(left, right);
    return layout;
  }

  /**
   * Create toolbar layout (left content, spacer, right actions)
   *
   * @param leftContent Left content
   * @param rightActions Right actions
   * @return Toolbar layout
   */
  public static HBox createToolbarLayout(Node leftContent, Node... rightActions) {
    HBox toolbar = createHBox(LayoutConstants.SPACING_MD);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    
    if (leftContent != null) {
      toolbar.getChildren().add(leftContent);
    }
    
    Region spacer = UIFactory.createHorizontalSpacer();
    toolbar.getChildren().add(spacer);
    
    if (rightActions != null && rightActions.length > 0) {
      toolbar.getChildren().addAll(rightActions);
    }
    
    return toolbar;
  }
}

