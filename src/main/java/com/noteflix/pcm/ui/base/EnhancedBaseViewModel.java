package com.noteflix.pcm.ui.base;

import com.noteflix.pcm.core.utils.Asyncs;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Enhanced Base ViewModel
 *
 * <p>Improved version of BaseViewModel with:
 * - Better error handling
 * - Success message support
 * - More utility methods
 * - Consistent API
 *
 * <p>Provides common state and behavior for all ViewModels:
 * - Busy/loading state
 * - Error handling
 * - Success messages
 * - Lifecycle methods
 * - Async operation helpers
 *
 * <p>Follows:
 * - Template Method Pattern (lifecycle hooks)
 * - DRY principle (common properties)
 * - Single Responsibility Principle
 *
 * @author PCM Team
 * @version 2.0.0
 */
@Slf4j
public abstract class EnhancedBaseViewModel implements ViewLifecycle {

    // ===== STATE PROPERTIES =====

    /**
     * Indicates if ViewModel is busy (loading, processing)
     */
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    /**
     * Error message to display
     */
    private final StringProperty errorMessage = new SimpleStringProperty(null);

    /**
     * Success message to display
     */
    private final StringProperty successMessage = new SimpleStringProperty(null);

    // ===== LIFECYCLE METHODS =====

    /**
     * Called when ViewModel is activated (page shown)
     * Override to add custom initialization
     */
    @Override
    public void onActivate() {
        clearMessages();
        log.debug("{} activated", getClass().getSimpleName());
    }

    /**
     * Called when ViewModel is deactivated (page hidden)
     * Override to add custom cleanup
     */
    @Override
    public void onDeactivate() {
        clearMessages();
        log.debug("{} deactivated", getClass().getSimpleName());
    }

    /**
     * Cleanup resources
     * Override to cleanup listeners, cancel tasks, etc.
     */
    @Override
    public void cleanup() {
        clearMessages();
        log.debug("{} cleanup", getClass().getSimpleName());
    }

    // ===== BUSY STATE =====

    public BooleanProperty busyProperty() {
        return busy;
    }

    public boolean isBusy() {
        return busy.get();
    }

    protected void setBusy(boolean value) {
        busy.set(value);
        log.debug("{} busy state: {}", getClass().getSimpleName(), value);
    }

    // ===== ERROR HANDLING =====

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage.get();
    }

    public boolean hasError() {
        return errorMessage.get() != null && !errorMessage.get().isEmpty();
    }

    /**
     * Set error message
     *
     * @param message Error message
     */
    protected void setError(String message) {
        errorMessage.set(message);
        successMessage.set(null); // Clear success message
        log.error("{} error: {}", getClass().getSimpleName(), message);
    }

    /**
     * Set error message with exception
     *
     * @param message Error message
     * @param error   Exception
     */
    protected void setError(String message, Throwable error) {
        String fullMessage = message + ": " + error.getMessage();
        errorMessage.set(fullMessage);
        successMessage.set(null); // Clear success message
        log.error("{} error: {}", getClass().getSimpleName(), fullMessage, error);
    }

    /**
     * Clear error message
     */
    protected void clearError() {
        errorMessage.set(null);
    }

    // ===== SUCCESS HANDLING =====

    public StringProperty successMessageProperty() {
        return successMessage;
    }

    public String getSuccessMessage() {
        return successMessage.get();
    }

    public boolean hasSuccess() {
        return successMessage.get() != null && !successMessage.get().isEmpty();
    }

    /**
     * Set success message
     *
     * @param message Success message
     */
    protected void setSuccess(String message) {
        successMessage.set(message);
        errorMessage.set(null); // Clear error message
        log.info("{} success: {}", getClass().getSimpleName(), message);
    }

    /**
     * Clear success message
     */
    protected void clearSuccess() {
        successMessage.set(null);
    }

    /**
     * Clear all messages (error and success)
     */
    protected void clearMessages() {
        clearError();
        clearSuccess();
    }

    // ===== ASYNC OPERATIONS =====

    /**
     * Run async task with success and error handlers
     *
     * @param task      Task to run
     * @param onSuccess Success handler
     * @param onError   Error handler
     * @param <T>       Result type
     * @return CompletableFuture
     */
    protected <T> CompletableFuture<T> runAsync(
            Callable<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        return Asyncs.runAsync(task, onSuccess, onError);
    }

    /**
     * Run async task with automatic busy state management
     *
     * @param task      Task to run
     * @param onSuccess Success handler
     * @param onError   Error handler
     * @param <T>       Result type
     * @return CompletableFuture
     */
    protected <T> CompletableFuture<T> runAsyncWithBusy(
            Callable<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        setBusy(true);
        clearMessages();

        return Asyncs.runAsync(
                task,
                result -> {
                    onSuccess.accept(result);
                    setBusy(false);
                },
                error -> {
                    onError.accept(error);
                    setBusy(false);
                });
    }

    /**
     * Run async task with automatic error handling
     *
     * @param task         Task to run
     * @param onSuccess    Success handler
     * @param errorMessage Error message prefix
     * @param <T>          Result type
     * @return CompletableFuture
     */
    protected <T> CompletableFuture<T> runAsyncWithErrorHandling(
            Callable<T> task, Consumer<T> onSuccess, String errorMessage) {
        setBusy(true);
        clearMessages();

        return Asyncs.runAsync(
                task,
                result -> {
                    onSuccess.accept(result);
                    setBusy(false);
                },
                error -> {
                    setError(errorMessage, error);
                    setBusy(false);
                });
    }
}

