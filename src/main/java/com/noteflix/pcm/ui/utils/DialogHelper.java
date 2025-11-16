package com.noteflix.pcm.ui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
 * Dialog Helper Utilities
 *
 * <p>Provides convenient methods for showing common dialogs.
 *
 * @author PCM Team
 * @version 2.0.0
 */
public final class DialogHelper {

  private DialogHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  // ===== INFO DIALOGS =====

  /**
   * Show info dialog
   *
   * @param title Dialog title
   * @param message Dialog message
   */
  public static void showInfo(String title, String message) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Show success dialog
   *
   * @param message Success message
   */
  public static void showSuccess(String message) {
    showInfo("Success", message);
  }

  // ===== ERROR DIALOGS =====

  /**
   * Show error dialog
   *
   * @param title Dialog title
   * @param message Error message
   */
  public static void showError(String title, String message) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Show error dialog with exception
   *
   * @param title Dialog title
   * @param message Error message
   * @param exception Exception
   */
  public static void showError(String title, String message, Throwable exception) {
    String fullMessage = message + "\n\n" + exception.getMessage();
    showError(title, fullMessage);
  }

  // ===== WARNING DIALOGS =====

  /**
   * Show warning dialog
   *
   * @param title Dialog title
   * @param message Warning message
   */
  public static void showWarning(String title, String message) {
    Alert alert = new Alert(AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  // ===== CONFIRMATION DIALOGS =====

  /**
   * Show confirmation dialog
   *
   * @param title Dialog title
   * @param message Confirmation message
   * @return true if user clicked OK
   */
  public static boolean showConfirm(String title, String message) {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    
    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  /**
   * Show delete confirmation dialog
   *
   * @param itemName Name of item to delete
   * @return true if user confirmed deletion
   */
  public static boolean showDeleteConfirm(String itemName) {
    return showConfirm(
        "Confirm Delete",
        "Are you sure you want to delete \"" + itemName + "\"?\n\nThis action cannot be undone.");
  }

  /**
   * Show discard changes confirmation dialog
   *
   * @return true if user confirmed discard
   */
  public static boolean showDiscardConfirm() {
    return showConfirm(
        "Discard Changes",
        "You have unsaved changes.\n\nAre you sure you want to discard them?");
  }

  // ===== YES/NO DIALOGS =====

  /**
   * Show yes/no dialog
   *
   * @param title Dialog title
   * @param message Dialog message
   * @return true if user clicked YES
   */
  public static boolean showYesNo(String title, String message) {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
    
    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.YES;
  }
}

