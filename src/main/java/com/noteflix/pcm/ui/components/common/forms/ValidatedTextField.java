package com.noteflix.pcm.ui.components.common.forms;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.function.Predicate;

/**
 * Validated Text Field
 *
 * <p>TextField with validation support.
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class ValidatedTextField extends VBox {

  @Getter private final TextField textField;
  private final Label errorLabel;
  private Predicate<String> validator;

  /**
   * Create validated text field
   */
  public ValidatedTextField() {
    super(LayoutConstants.SPACING_XS);

    textField = new TextField();
    errorLabel = new Label();
    errorLabel.getStyleClass().addAll(Styles.DANGER, Styles.TEXT_SMALL);
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);

    getChildren().addAll(textField, errorLabel);
  }

  /**
   * Set validator
   */
  public ValidatedTextField withValidator(Predicate<String> validator) {
    this.validator = validator;
    return this;
  }

  /**
   * Set placeholder
   */
  public ValidatedTextField withPlaceholder(String placeholder) {
    textField.setPromptText(placeholder);
    return this;
  }

  /**
   * Validate
   */
  public boolean isValid() {
    if (validator == null) {
      return true;
    }
    
    boolean valid = validator.test(textField.getText());
    
    if (!valid) {
      textField.getStyleClass().add(Styles.DANGER);
    } else {
      textField.getStyleClass().remove(Styles.DANGER);
      hideError();
    }
    
    return valid;
  }

  /**
   * Show error
   */
  public void showError(String message) {
    errorLabel.setText(message);
    errorLabel.setVisible(true);
    errorLabel.setManaged(true);
    textField.getStyleClass().add(Styles.DANGER);
  }

  /**
   * Hide error
   */
  public void hideError() {
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
    textField.getStyleClass().remove(Styles.DANGER);
  }

  /**
   * Get text
   */
  public String getText() {
    return textField.getText();
  }

  /**
   * Set text
   */
  public void setText(String text) {
    textField.setText(text);
  }
}

