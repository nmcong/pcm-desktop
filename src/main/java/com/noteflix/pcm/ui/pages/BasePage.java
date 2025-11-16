package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.base.BaseView;
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
 * Legacy BasePage - Backward Compatibility Layer
 *
 * <p>This class now extends BaseView to provide backward compatibility
 * while we migrate pages to the new architecture.
 *
 * <p><strong>Deprecated:</strong> Use {@link BaseView} directly instead.
 * This class will be removed in a future version.
 *
 * @deprecated Use {@link BaseView} instead
 * @author PCM Team
 * @version 2.0.0
 */
@Deprecated
@Slf4j
@Getter
public abstract class BasePage extends BaseView {

  protected BasePage(String title, String description, FontIcon icon) {
    super(title, description, icon);
    log.warn("Using deprecated BasePage. Please migrate to BaseView.");
  }

  /**
   * Legacy method - delegates to onActivate()
   *
   * @deprecated Use {@link #onActivate()} instead
   */
  @Deprecated
  public void onPageActivated() {
    onActivate();
  }

  /**
   * Legacy method - delegates to onDeactivate()
   *
   * @deprecated Use {@link #onDeactivate()} instead
   */
  @Deprecated
  public void onPageDeactivated() {
    onDeactivate();
  }

  /**
   * For backward compatibility: createMainContent() returns VBox
   * Override this or use the new createMainContent() that returns Node
   *
   * @deprecated Override {@link BaseView#createMainContent()} instead
   */
  @Deprecated
  protected VBox createMainContentLegacy() {
    return null;
  }

  @Override
  protected Node createMainContent() {
    // Try legacy method first
    VBox legacy = createMainContentLegacy();
    if (legacy != null) {
      return legacy;
    }
    
    // Otherwise, subclass should override BaseView's createMainContent()
    // This allows direct override of createMainContent() returning Node
    return null;
  }
}
