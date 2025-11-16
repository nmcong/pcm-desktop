package com.noteflix.pcm.ui.components.common.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Dialog Builder
 *
 * <p>Fluent API for creating dialogs.
 *
 * <p>Usage:
 * <pre>{@code
 * DialogBuilder.info()
 *     .title("Success")
 *     .content("Operation completed")
 *     .show();
 * }</pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class DialogBuilder {

    private final Alert alert;
    private Runnable onConfirm;
    private Runnable onCancel;

    private DialogBuilder(Alert.AlertType type) {
        this.alert = new Alert(type);
        this.alert.setHeaderText(null);
    }

    /**
     * Create info dialog
     */
    public static DialogBuilder info() {
        return new DialogBuilder(Alert.AlertType.INFORMATION);
    }

    /**
     * Create warning dialog
     */
    public static DialogBuilder warning() {
        return new DialogBuilder(Alert.AlertType.WARNING);
    }

    /**
     * Create error dialog
     */
    public static DialogBuilder error() {
        return new DialogBuilder(Alert.AlertType.ERROR);
    }

    /**
     * Create confirmation dialog
     */
    public static DialogBuilder confirm() {
        return new DialogBuilder(Alert.AlertType.CONFIRMATION);
    }

    /**
     * Set title
     */
    public DialogBuilder title(String title) {
        alert.setTitle(title);
        return this;
    }

    /**
     * Set header
     */
    public DialogBuilder header(String header) {
        alert.setHeaderText(header);
        return this;
    }

    /**
     * Set content
     */
    public DialogBuilder content(String content) {
        alert.setContentText(content);
        return this;
    }

    /**
     * Set confirm handler
     */
    public DialogBuilder onConfirm(Runnable handler) {
        this.onConfirm = handler;
        return this;
    }

    /**
     * Set cancel handler
     */
    public DialogBuilder onCancel(Runnable handler) {
        this.onCancel = handler;
        return this;
    }

    /**
     * Show and return result
     */
    public Optional<ButtonType> show() {
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == ButtonType.OK || result.get() == ButtonType.YES) {
                if (onConfirm != null) {
                    onConfirm.run();
                }
            } else if (onCancel != null) {
                onCancel.run();
            }
        }

        return result;
    }

    /**
     * Show without waiting
     */
    public void showAndWait() {
        show();
    }
}

