package com.noteflix.pcm.ui;

import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.core.utils.DialogService;
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

  // ===== MENU HANDLERS =====

  /** Handle File -> New */
  public void handleFileNew() {
    log.info("📄 File -> New");
    DialogService.showInfo(
        I18n.get("menu.file.new"),
        "Create New Screen\n\n"
            + "Choose template:\n"
            + "• Blank screen\n"
            + "• List view\n"
            + "• Detail view\n"
            + "• Form\n"
            + "• Dashboard");
  }

  /** Handle File -> Open */
  public void handleFileOpen() {
    log.info("📂 File -> Open");
    DialogService.showInfo(I18n.get("menu.file.open"), "Select a PCM project to open");
  }

  /** Handle File -> Save */
  public void handleFileSave() {
    log.info("💾 File -> Save");
    DialogService.showInfo(I18n.get("menu.file.save"), I18n.get("message.saving"));
    // TODO: Implement save functionality
  }

  /** Handle File -> Exit */
  public void handleFileExit() {
    log.info("🚪 File -> Exit");
    javafx.application.Platform.exit();
  }

  /** Handle Help -> About */
  public void handleHelpAbout() {
    log.info("ℹ️ Help -> About");

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(I18n.get("menu.help.about"));
    alert.setHeaderText(I18n.appTitle());
    alert.setContentText(
        "Version 4.0.0 - MVVM Architecture with Best Practices\n\n"
            + "AI-Powered System Analysis & Business Management\n\n"
            + "Features:\n"
            + "• Screen and form tracking\n"
            + "• Workflow visualization\n"
            + "• Database object management\n"
            + "• Source code analysis\n"
            + "• AI-powered natural language queries\n"
            + "• Knowledge base integration\n\n"
            + "Architecture:\n"
            + "• MVVM pattern with ViewModels\n"
            + "• Dependency Injection\n"
            + "• Internationalization (i18n)\n"
            + "• Async task management\n"
            + "• Clean Code & SOLID principles\n\n"
            + "Built with JavaFX 21 and Java 21\n"
            + "© 2025 Noteflix Team");
    alert.showAndWait();
  }

  // ===== UTILITY METHODS =====
  // Note: Now using DialogService for consistent dialog management

  /** Refresh current view */
  public void refreshView() {
    log.info("🔄 Refreshing view");
    // TODO: Implement view refresh
  }
}
