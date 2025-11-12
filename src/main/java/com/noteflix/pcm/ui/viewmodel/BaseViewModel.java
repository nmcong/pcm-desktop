package com.noteflix.pcm.ui.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.extern.slf4j.Slf4j;

/**
 * Base ViewModel for all ViewModels
 *
 * <p>Provides common state and behavior for all ViewModels: - Busy/loading state - Error handling
 * - Lifecycle methods
 *
 * <p>Follows: - Template Method Pattern (lifecycle hooks) - DRY principle (common properties)
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public abstract class BaseViewModel {

  // Common state properties
  private final BooleanProperty busy = new SimpleBooleanProperty(false);
  private final StringProperty errorMessage = new SimpleStringProperty(null);

  /** Called when ViewModel is activated (page shown) */
  public void onActivate() {
    clearError();
    log.debug("{} activated", getClass().getSimpleName());
  }

  /** Called when ViewModel is deactivated (page hidden) */
  public void onDeactivate() {
    clearError();
    log.debug("{} deactivated", getClass().getSimpleName());
  }

  // ========== Common Property Accessors ==========

  public BooleanProperty busyProperty() {
    return busy;
  }

  public boolean isBusy() {
    return busy.get();
  }

  protected void setBusy(boolean value) {
    busy.set(value);
  }

  public StringProperty errorMessageProperty() {
    return errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage.get();
  }

  public boolean hasError() {
    return errorMessage.get() != null && !errorMessage.get().isEmpty();
  }

  protected void setError(String message) {
    errorMessage.set(message);
    log.error("ViewModel error: {}", message);
  }

  protected void setError(String message, Throwable error) {
    errorMessage.set(message + ": " + error.getMessage());
    log.error(message, error);
  }

  protected void clearError() {
    errorMessage.set(null);
  }
}

