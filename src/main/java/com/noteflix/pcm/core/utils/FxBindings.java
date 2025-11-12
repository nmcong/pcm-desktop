package com.noteflix.pcm.core.utils;

import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;

/**
 * JavaFX Binding Utilities
 *
 * <p>Provides helper methods for creating property bindings, converters, and binding expressions.
 * Follows: - Binding-first approach (minimize manual UI updates) - Type-safe bindings -
 * Bidirectional binding support
 *
 * <p>Usage: <code>
 *   // Bind text property with conversion
 *   FxBindings.bindBidirectionalWithConverter(
 *     textField.textProperty(),
 *     model.numberProperty(),
 *     Integer::parseInt,
 *     String::valueOf
 *   );
 *
 *   // Create not-empty binding
 *   BooleanBinding notEmpty = FxBindings.isNotEmpty(textField.textProperty());
 *   button.disableProperty().bind(notEmpty.not());
 * </code>
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class FxBindings {

  /**
   * Create a string converter from functions
   *
   * @param fromString Function to convert from string
   * @param toString Function to convert to string
   * @param <T> Type parameter
   * @return StringConverter
   */
  public static <T> StringConverter<T> converter(
      Function<String, T> fromString, Function<T, String> toString) {
    return new StringConverter<T>() {
      @Override
      public String toString(T object) {
        return object == null ? "" : toString.apply(object);
      }

      @Override
      public T fromString(String string) {
        return string == null || string.trim().isEmpty() ? null : fromString.apply(string);
      }
    };
  }

  /**
   * Bind bidirectionally with type conversion
   *
   * @param stringProperty String property (usually from UI)
   * @param objectProperty Object property (usually from model)
   * @param fromString Converter from string
   * @param toString Converter to string
   * @param <T> Type parameter
   */
  public static <T> void bindBidirectionalWithConverter(
      Property<String> stringProperty,
      Property<T> objectProperty,
      Function<String, T> fromString,
      Function<T, String> toString) {

    StringConverter<T> converter = converter(fromString, toString);
    Bindings.bindBidirectional(stringProperty, objectProperty, converter);
  }

  /**
   * Create a binding that checks if a string is not empty
   *
   * @param stringProperty String property to check
   * @return BooleanBinding that is true when string is not empty
   */
  public static BooleanBinding isNotEmpty(ObservableValue<String> stringProperty) {
    return Bindings.createBooleanBinding(
        () -> {
          String value = stringProperty.getValue();
          return value != null && !value.trim().isEmpty();
        },
        stringProperty);
  }

  /**
   * Create a binding that checks if a string is empty
   *
   * @param stringProperty String property to check
   * @return BooleanBinding that is true when string is empty
   */
  public static BooleanBinding isEmpty(ObservableValue<String> stringProperty) {
    return isNotEmpty(stringProperty).not();
  }

  /**
   * Create a binding that checks if all conditions are true
   *
   * @param conditions Boolean observables to check
   * @return BooleanBinding that is true when all conditions are true
   */
  public static BooleanBinding allTrue(ObservableValue<Boolean>... conditions) {
    return Bindings.createBooleanBinding(
        () -> {
          for (ObservableValue<Boolean> condition : conditions) {
            if (condition.getValue() == null || !condition.getValue()) {
              return false;
            }
          }
          return true;
        },
        conditions);
  }

  /**
   * Create a binding that checks if any condition is true
   *
   * @param conditions Boolean observables to check
   * @return BooleanBinding that is true when any condition is true
   */
  public static BooleanBinding anyTrue(ObservableValue<Boolean>... conditions) {
    return Bindings.createBooleanBinding(
        () -> {
          for (ObservableValue<Boolean> condition : conditions) {
            if (condition.getValue() != null && condition.getValue()) {
              return true;
            }
          }
          return false;
        },
        conditions);
  }

  /**
   * Create a string binding with format
   *
   * @param format Format string
   * @param dependencies Observable dependencies
   * @return StringBinding
   */
  public static StringBinding format(String format, ObservableValue<?>... dependencies) {
    return Bindings.createStringBinding(
        () -> {
          Object[] values = new Object[dependencies.length];
          for (int i = 0; i < dependencies.length; i++) {
            values[i] = dependencies[i].getValue();
          }
          return String.format(format, values);
        },
        dependencies);
  }

  /**
   * Map an observable value to another type
   *
   * @param source Source observable
   * @param mapper Mapping function
   * @param <S> Source type
   * @param <T> Target type
   * @return Mapped binding
   */
  public static <S, T> javafx.beans.binding.ObjectBinding<T> map(
      ObservableValue<S> source, Function<S, T> mapper) {
    return Bindings.createObjectBinding(() -> mapper.apply(source.getValue()), source);
  }

  /**
   * Create a binding that provides a default value when source is null
   *
   * @param source Source observable
   * @param defaultValue Default value when source is null
   * @param <T> Type parameter
   * @return Binding with default value
   */
  public static <T> javafx.beans.binding.ObjectBinding<T> withDefault(
      ObservableValue<T> source, T defaultValue) {
    return Bindings.createObjectBinding(
        () -> {
          T value = source.getValue();
          return value != null ? value : defaultValue;
        },
        source);
  }

  /**
   * Create a boolean binding that checks if a value equals another
   *
   * @param property Property to check
   * @param value Value to compare with
   * @param <T> Type parameter
   * @return BooleanBinding
   */
  public static <T> BooleanBinding isEqual(ObservableValue<T> property, T value) {
    return Bindings.createBooleanBinding(
        () -> {
          T propertyValue = property.getValue();
          return propertyValue != null && propertyValue.equals(value);
        },
        property);
  }

  /**
   * Create a boolean binding that checks if a value is not equal to another
   *
   * @param property Property to check
   * @param value Value to compare with
   * @param <T> Type parameter
   * @return BooleanBinding
   */
  public static <T> BooleanBinding isNotEqual(ObservableValue<T> property, T value) {
    return isEqual(property, value).not();
  }

  /**
   * Create a boolean binding that checks if a value is null
   *
   * @param property Property to check
   * @return BooleanBinding that is true when value is null
   */
  public static BooleanBinding isNull(ObservableValue<?> property) {
    return Bindings.createBooleanBinding(() -> property.getValue() == null, property);
  }

  /**
   * Create a boolean binding that checks if a value is not null
   *
   * @param property Property to check
   * @return BooleanBinding that is true when value is not null
   */
  public static BooleanBinding isNotNull(ObservableValue<?> property) {
    return isNull(property).not();
  }
}

