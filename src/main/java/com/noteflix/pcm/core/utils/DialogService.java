package com.noteflix.pcm.core.utils;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Centralized dialog service for consistent UI interactions
 *
 * <p>Provides convenient methods for showing alerts, confirmations, and input dialogs. Follows: -
 * Single Responsibility Principle - DRY (Don't Repeat Yourself) - Consistent user experience
 *
 * <p>Usage: <code>
 * DialogService.showInfo("Success", "Data saved successfully");
 * boolean confirmed = DialogService.showConfirm("Delete", "Are you sure?");
 * </code>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class DialogService {

    /**
     * Show an information dialog
     *
     * @param title   Dialog title
     * @param message Dialog message
     */
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message);
    }

    /**
     * Show a warning dialog
     *
     * @param title   Dialog title
     * @param message Dialog message
     */
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, message);
    }

    /**
     * Show an error dialog
     *
     * @param title   Dialog title
     * @param message Dialog message
     */
    public static void showError(String title, String message) {
        showAlert(AlertType.ERROR, title, message);
    }

    /**
     * Show an error dialog from exception
     *
     * @param title Dialog title
     * @param error Exception
     */
    public static void showError(String title, Throwable error) {
        String message = error.getMessage();
        if (message == null || message.isEmpty()) {
            message = error.getClass().getSimpleName();
        }
        showError(title, message);
        log.error(title, error);
    }

    /**
     * Show a confirmation dialog
     *
     * @param title   Dialog title
     * @param message Dialog message
     * @return true if user confirmed, false otherwise
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
     * Show a confirmation dialog with custom buttons
     *
     * @param title   Dialog title
     * @param message Dialog message
     * @param buttons Custom buttons
     * @return Selected button
     */
    public static Optional<ButtonType> showConfirm(
            String title, String message, ButtonType... buttons) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(buttons);

        return alert.showAndWait();
    }

    /**
     * Show a text input dialog
     *
     * @param title        Dialog title
     * @param message      Dialog message
     * @param defaultValue Default value
     * @return User input or empty if cancelled
     */
    public static Optional<String> showInput(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        return dialog.showAndWait();
    }

    /**
     * Show a text input dialog without default value
     *
     * @param title   Dialog title
     * @param message Dialog message
     * @return User input or empty if cancelled
     */
    public static Optional<String> showInput(String title, String message) {
        return showInput(title, message, "");
    }

    /**
     * Show a choice dialog
     *
     * @param title         Dialog title
     * @param message       Dialog message
     * @param choices       Available choices
     * @param defaultChoice Default choice
     * @param <T>           Choice type
     * @return Selected choice or empty if cancelled
     */
    public static <T> Optional<T> showChoice(
            String title, String message, java.util.List<T> choices, T defaultChoice) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(defaultChoice, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        return dialog.showAndWait();
    }

    /**
     * Show an exception dialog with details
     *
     * @param title   Dialog title
     * @param message Dialog message
     * @param error   Exception
     */
    public static void showException(String title, String message, Throwable error) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(error.getMessage());

        // Create expandable Exception area
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        error.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Exception stacktrace:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        javafx.scene.layout.GridPane expContent = new javafx.scene.layout.GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();

        log.error(title + ": " + message, error);
    }

    /**
     * Show a loading dialog (progress)
     *
     * @param title   Dialog title
     * @param message Dialog message
     * @return ProgressDialog that can be closed later
     */
    public static ProgressDialog showProgress(String title, String message) {
        return new ProgressDialog(title, message);
    }

    /**
     * Show a generic alert
     *
     * @param type    Alert type
     * @param title   Dialog title
     * @param message Dialog message
     */
    private static void showAlert(AlertType type, String title, String message) {
        Asyncs.runOnFxThread(
                () -> {
                    Alert alert = new Alert(type);
                    alert.setTitle(title);
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                });
    }

    /**
     * Progress dialog wrapper for long-running operations
     */
    public static class ProgressDialog {
        private final Dialog<Void> dialog;
        private final ProgressIndicator progressIndicator;
        private final Label messageLabel;

        private ProgressDialog(String title, String message) {
            dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.setHeaderText(null);

            progressIndicator = new ProgressIndicator();
            progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

            messageLabel = new Label(message);

            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
            content.getChildren().addAll(progressIndicator, messageLabel);
            content.setAlignment(javafx.geometry.Pos.CENTER);
            content.setPadding(new javafx.geometry.Insets(20));

            dialog.getDialogPane().setContent(content);
            dialog.show();
        }

        /**
         * Update progress message
         */
        public void updateMessage(String message) {
            Asyncs.runOnFxThread(() -> messageLabel.setText(message));
        }

        /**
         * Update progress value (0.0 to 1.0)
         */
        public void updateProgress(double progress) {
            Asyncs.runOnFxThread(() -> progressIndicator.setProgress(progress));
        }

        /**
         * Close the dialog
         */
        public void close() {
            Asyncs.runOnFxThread(() -> dialog.close());
        }
    }
}
