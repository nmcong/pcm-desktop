package com.noteflix.pcm.llm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a parameter of an LLM function.
 *
 * <p>Provides metadata about function parameters including: - Description (helps LLM understand the
 * parameter) - Required/optional - Default value - Validation rules
 *
 * <p>Example:
 *
 * <pre>
 * &#64;LLMFunction(description = "Search projects")
 * public List&lt;Project&gt; search(
 *     &#64;Param(
 *         description = "Search query to match against project names",
 *         required = true
 *     )
 *     String query,
 *
 *     &#64;Param(
 *         description = "Maximum number of results to return",
 *         defaultValue = "10",
 *         min = 1,
 *         max = 100
 *     )
 *     int limit,
 *
 *     &#64;Param(
 *         description = "Sort order",
 *         enumValues = {"name", "date", "relevance"},
 *         defaultValue = "relevance"
 *     )
 *     String sortBy
 * ) {
 *     // Implementation
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    /**
     * Parameter description. Be clear about what the parameter does and expected values.
     *
     * @return Parameter description
     */
    String description();

    /**
     * Whether this parameter is required. If true, LLM must provide this parameter.
     *
     * @return true if required, false if optional (default)
     */
    boolean required() default false;

    /**
     * Default value (as string). Used when parameter is optional and not provided. Will be converted
     * to parameter type.
     *
     * @return Default value string
     */
    String defaultValue() default "";

    /**
     * Allowed enum values. If specified, parameter must be one of these values.
     *
     * @return Array of allowed values
     */
    String[] enumValues() default {};

    /**
     * Minimum value (for numeric parameters).
     *
     * @return Minimum value
     */
    double min() default Double.MIN_VALUE;

    /**
     * Maximum value (for numeric parameters).
     *
     * @return Maximum value
     */
    double max() default Double.MAX_VALUE;

    /**
     * Minimum length (for string/array parameters).
     *
     * @return Minimum length
     */
    int minLength() default 0;

    /**
     * Maximum length (for string/array parameters).
     *
     * @return Maximum length
     */
    int maxLength() default Integer.MAX_VALUE;

    /**
     * Regex pattern (for string parameters).
     *
     * @return Regex pattern
     */
    String pattern() default "";

    /**
     * Example value (for documentation).
     *
     * @return Example value
     */
    String example() default "";
}
