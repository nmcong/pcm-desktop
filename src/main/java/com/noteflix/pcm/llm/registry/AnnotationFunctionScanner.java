package com.noteflix.pcm.llm.registry;

import com.noteflix.pcm.llm.annotation.LLMFunction;
import com.noteflix.pcm.llm.annotation.Param;
import com.noteflix.pcm.llm.api.RegisteredFunction;
import com.noteflix.pcm.llm.model.JsonSchema;
import com.noteflix.pcm.llm.model.PropertySchema;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
// Reflections library not available - using manual scanning
// To enable automatic package scanning, add dependency:
// org.reflections:reflections:0.10.2

/**
 * Scans for @LLMFunction annotated methods and registers them.
 *
 * <p>Uses reflection to: 1. Find all classes annotated with @FunctionProvider 2. Find all methods
 * annotated with @LLMFunction in those classes 3. Extract parameter metadata from @Param
 * annotations 4. Create RegisteredFunction wrappers 5. Register functions in FunctionRegistry
 *
 * <p>Functions execute in the current application context, so provider instances can have
 * dependencies injected.
 */
@Slf4j
public class AnnotationFunctionScanner {

  private final FunctionRegistry registry;
  private final Map<Class<?>, Object> providerInstances;

  public AnnotationFunctionScanner(FunctionRegistry registry) {
    this.registry = registry;
    this.providerInstances = new HashMap<>();
  }

  /**
   * Scan a package for @FunctionProvider classes and register functions.
   *
   * <p>NOTE: Automatic package scanning requires org.reflections library. For now, use scanClass()
   * or register() methods to add functions manually.
   *
   * @param packageName Package to scan (e.g., "com.noteflix.pcm.functions")
   * @return Number of functions registered
   */
  public int scanPackage(String packageName) {
    log.warn("Automatic package scanning not available without reflections library");
    log.info("To enable package scanning, add dependency: org.reflections:reflections:0.10.2");
    log.info("For now, use FunctionRegistry.scanClass(YourClass.class) to register functions");
    return 0;

    // TODO: Implement with reflections library or custom classpath scanning
    // When reflections is available, uncomment this code:
    /*
    log.info("Scanning package for LLM functions: {}", packageName);

    int functionsRegistered = 0;

    try {
        // Use Reflections library to scan for annotated classes
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(packageName)
                .setScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated)
        );

        // Find all @FunctionProvider classes
        Set<Class<?>> providerClasses = reflections.getTypesAnnotatedWith(FunctionProvider.class);

        log.info("Found {} function provider classes", providerClasses.size());

        for (Class<?> providerClass : providerClasses) {
            FunctionProvider annotation = providerClass.getAnnotation(FunctionProvider.class);

            if (!annotation.enabled()) {
                log.debug("Skipping disabled provider: {}", providerClass.getName());
                continue;
            }

            functionsRegistered += scanProviderClass(providerClass);
        }

        log.info("Registered {} functions from package: {}", functionsRegistered, packageName);

    } catch (Exception e) {
        log.error("Failed to scan package: {}", packageName, e);
    }

    return functionsRegistered;
    */
  }

  /**
   * Scan a specific provider class for @LLMFunction methods.
   *
   * @param providerClass Provider class
   * @return Number of functions registered
   */
  public int scanProviderClass(Class<?> providerClass) {
    log.debug("Scanning provider class: {}", providerClass.getName());

    int functionsRegistered = 0;

    try {
      // Get or create provider instance
      Object providerInstance = getProviderInstance(providerClass);

      // Find all @LLMFunction methods
      for (Method method : providerClass.getDeclaredMethods()) {
        if (method.isAnnotationPresent(LLMFunction.class)) {
          LLMFunction annotation = method.getAnnotation(LLMFunction.class);

          if (!annotation.enabled()) {
            log.debug(
                "Skipping disabled function: {}.{}",
                providerClass.getSimpleName(),
                method.getName());
            continue;
          }

          RegisteredFunction function =
              createFunctionFromMethod(providerInstance, method, annotation);

          registry.register(function);
          functionsRegistered++;

          log.debug(
              "Registered function: {} from {}.{}",
              function.getName(),
              providerClass.getSimpleName(),
              method.getName());
        }
      }

    } catch (Exception e) {
      log.error("Failed to scan provider class: {}", providerClass.getName(), e);
    }

    return functionsRegistered;
  }

  /**
   * Get or create an instance of the provider class.
   *
   * <p>First checks DI container (Injector), then creates new instance.
   */
  private Object getProviderInstance(Class<?> providerClass) throws Exception {
    // Check cache first
    if (providerInstances.containsKey(providerClass)) {
      return providerInstances.get(providerClass);
    }

    Object instance;

    // Try to get from DI container first
    try {
      instance = com.noteflix.pcm.core.di.Injector.getInstance().get(providerClass);
      log.debug("Got provider instance from DI: {}", providerClass.getSimpleName());
    } catch (Exception e) {
      // Fallback: create new instance
      log.debug("Creating new provider instance: {}", providerClass.getSimpleName());
      instance = providerClass.getDeclaredConstructor().newInstance();
    }

    providerInstances.put(providerClass, instance);
    return instance;
  }

  /** Create a RegisteredFunction from an annotated method. */
  private RegisteredFunction createFunctionFromMethod(
      Object instance, Method method, LLMFunction annotation) {
    // Get function name
    String functionName =
        annotation.name().isEmpty() ? camelToSnakeCase(method.getName()) : annotation.name();

    // Get description
    String description = annotation.description();

    // Build parameter schema
    JsonSchema parameters = buildParameterSchema(method);

    // Create function wrapper
    return new AnnotatedFunction(functionName, description, parameters, instance, method);
  }

  /** Build JSON Schema from method parameters. */
  private JsonSchema buildParameterSchema(Method method) {
    JsonSchema.JsonSchemaBuilder schemaBuilder = JsonSchema.builder();

    Parameter[] parameters = method.getParameters();

    for (Parameter parameter : parameters) {
      Param paramAnnotation = parameter.getAnnotation(Param.class);

      if (paramAnnotation == null) {
        log.warn(
            "Parameter without @Param annotation: {} in method {}",
            parameter.getName(),
            method.getName());
        continue;
      }

      // Determine property type from Java type
      Class<?> paramType = parameter.getType();
      PropertySchema propertySchema = buildPropertySchema(paramType, paramAnnotation);

      // Add to schema
      schemaBuilder.property(parameter.getName(), propertySchema);

      // Add to required list if required
      if (paramAnnotation.required()) {
        schemaBuilder.required(parameter.getName());
      }
    }

    return schemaBuilder.build();
  }

  /** Build PropertySchema from Java type and @Param annotation. */
  private PropertySchema buildPropertySchema(Class<?> type, Param annotation) {
    PropertySchema.PropertySchemaBuilder builder = PropertySchema.builder();

    // Set type based on Java type
    if (type == String.class) {
      builder.type("string");
    } else if (type == int.class
        || type == Integer.class
        || type == long.class
        || type == Long.class) {
      builder.type("integer");
    } else if (type == double.class
        || type == Double.class
        || type == float.class
        || type == Float.class) {
      builder.type("number");
    } else if (type == boolean.class || type == Boolean.class) {
      builder.type("boolean");
    } else if (type.isArray() || List.class.isAssignableFrom(type)) {
      builder.type("array");
    } else {
      builder.type("object");
    }

    // Set description
    builder.description(annotation.description());

    // Set enum values if specified
    if (annotation.enumValues().length > 0) {
      builder.enumValues(Arrays.asList((Object[]) annotation.enumValues()));
    }

    // Set default value if specified
    if (!annotation.defaultValue().isEmpty()) {
      builder.defaultValue(annotation.defaultValue());
    }

    // Set numeric constraints
    if (annotation.min() != Double.MIN_VALUE) {
      builder.minimum(annotation.min());
    }
    if (annotation.max() != Double.MAX_VALUE) {
      builder.maximum(annotation.max());
    }

    // Set length constraints
    if (annotation.minLength() > 0) {
      builder.minLength(annotation.minLength());
    }
    if (annotation.maxLength() != Integer.MAX_VALUE) {
      builder.maxLength(annotation.maxLength());
    }

    // Set pattern if specified
    if (!annotation.pattern().isEmpty()) {
      builder.pattern(annotation.pattern());
    }

    return builder.build();
  }

  /** Convert camelCase to snake_case. */
  private String camelToSnakeCase(String camelCase) {
    return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }

  /** Inner class: Wrapper for annotated functions. Handles reflection-based method invocation. */
  private static class AnnotatedFunction implements RegisteredFunction {

    private final String name;
    private final String description;
    private final JsonSchema parameters;
    private final Object instance;
    private final Method method;

    public AnnotatedFunction(
        String name, String description, JsonSchema parameters, Object instance, Method method) {
      this.name = name;
      this.description = description;
      this.parameters = parameters;
      this.instance = instance;
      this.method = method;

      // Make method accessible
      method.setAccessible(true);
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public JsonSchema getParameters() {
      return parameters;
    }

    @Override
    public Object execute(Map<String, Object> args) throws Exception {
      // Convert args map to method parameters
      Parameter[] parameters = method.getParameters();
      Object[] methodArgs = new Object[parameters.length];

      for (int i = 0; i < parameters.length; i++) {
        Parameter parameter = parameters[i];
        String paramName = parameter.getName();
        Object value = args.get(paramName);

        // Handle default values
        if (value == null) {
          Param paramAnnotation = parameter.getAnnotation(Param.class);
          if (paramAnnotation != null && !paramAnnotation.defaultValue().isEmpty()) {
            value = convertToType(paramAnnotation.defaultValue(), parameter.getType());
          }
        }

        // Convert to correct type
        methodArgs[i] = convertToType(value, parameter.getType());
      }

      // Invoke method
      return method.invoke(instance, methodArgs);
    }

    /** Convert value to target type. */
    private Object convertToType(Object value, Class<?> targetType) {
      if (value == null) {
        return null;
      }

      if (targetType.isAssignableFrom(value.getClass())) {
        return value;
      }

      String strValue = value.toString();

      try {
        if (targetType == int.class || targetType == Integer.class) {
          return Integer.parseInt(strValue);
        } else if (targetType == long.class || targetType == Long.class) {
          return Long.parseLong(strValue);
        } else if (targetType == double.class || targetType == Double.class) {
          return Double.parseDouble(strValue);
        } else if (targetType == float.class || targetType == Float.class) {
          return Float.parseFloat(strValue);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
          return Boolean.parseBoolean(strValue);
        } else if (targetType == String.class) {
          return strValue;
        }
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Cannot convert " + value + " to " + targetType.getSimpleName(), e);
      }

      return value;
    }
  }
}
