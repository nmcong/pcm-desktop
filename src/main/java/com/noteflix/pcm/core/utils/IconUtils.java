package com.noteflix.pcm.core.utils;

import java.util.Objects;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

/** Utility class for loading and creating icons */
@Slf4j
public class IconUtils {

  /** Creates an ImageView from icon path with specified size */
  public static ImageView createImageView(String iconPath, double width, double height) {
    try {
      Image image =
          new Image(Objects.requireNonNull(IconUtils.class.getResourceAsStream(iconPath)));
      ImageView imageView = new ImageView(image);
      imageView.setFitWidth(width);
      imageView.setFitHeight(height);
      imageView.setPreserveRatio(true);
      imageView.setSmooth(true);
      return imageView;
    } catch (Exception e) {
      log.error("Failed to load icon: {}", iconPath, e);
      return createFallbackImageView(width, height);
    }
  }

  /** Creates a fallback ImageView when icon loading fails */
  private static ImageView createFallbackImageView(double width, double height) {
    try {
      // Try to load a fallback icon
      Image fallbackImage =
          new Image(
              Objects.requireNonNull(
                  IconUtils.class.getResourceAsStream("/images/icons/app-icon.png")));
      ImageView imageView = new ImageView(fallbackImage);
      imageView.setFitWidth(width);
      imageView.setFitHeight(height);
      imageView.setPreserveRatio(true);
      imageView.setSmooth(true);
      return imageView;
    } catch (Exception e) {
      log.error("Failed to load fallback icon", e);
      // Return empty ImageView as last resort
      ImageView emptyView = new ImageView();
      emptyView.setFitWidth(width);
      emptyView.setFitHeight(height);
      return emptyView;
    }
  }

  /** Updates an existing ImageView with a new icon */
  public static void updateImageView(ImageView imageView, String iconPath) {
    try {
      Image image =
          new Image(Objects.requireNonNull(IconUtils.class.getResourceAsStream(iconPath)));
      imageView.setImage(image);
    } catch (Exception e) {
      log.error("Failed to update ImageView with icon: {}", iconPath, e);
    }
  }
}
