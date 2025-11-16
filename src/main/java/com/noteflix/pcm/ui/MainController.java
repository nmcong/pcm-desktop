package com.noteflix.pcm.ui;

import com.noteflix.pcm.core.i18n.I18n;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;

/**
 * Main Controller for PCM Desktop WebApp
 *
 * <p>This controller manages the main PCM interface for project code management. Now using pure
 * Java code (no FXML) following AtlantaFX Sampler patterns.
 *
 * @author Noteflix Team
 * @version 4.0.0 - Pure Java UI Architecture
 */
@Slf4j
public class MainController {

  public MainController() {
    log.info("🚀 Initializing PCM Desktop Controller - Pure Java Architecture...");
    log.info("✅ Controller initialized successfully");
    log.info("🎨 Using AtlantaFX Sampler patterns for UI");
  }

  public void exit() {
    log.info("🚪 Exit");
    Platform.exit();
  }

  public void openAbout() {
    log.info("ℹ️ About");

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(I18n.get("menu.help.about"));
    alert.setHeaderText(I18n.appTitle());
    alert.setContentText(
      """
        Version 1.0.0

        AI-Powered System Analysis & Business Management

        Features:
        • Screen and form tracking
        • Workflow visualization
        • Database object management
        • Source code analysis
        • AI-powered natural language queries
        • Knowledge base integration

        © 2025 nmcong.it@gmail.com""");
    alert.showAndWait();
  }
}
