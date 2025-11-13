package com.noteflix.pcm.llm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an LLM-callable function.
 *
 * <p>Methods annotated with @LLMFunction are automatically discovered and registered in the
 * FunctionRegistry.
 *
 * <p>Requirements: - Method must be public - Method must be in a class annotated
 * with @FunctionProvider - Parameters should be annotated with @Param for metadata
 *
 * <p>Example:
 *
 * <pre>
 * &#64;FunctionProvider
 * public class ProjectFunctions {
 *
 *     &#64;LLMFunction(
 *         name = "search_projects",
 *         description = "Search for projects in database matching the query"
 *     )
 *     public List&lt;Project&gt; searchProjects(
 *         &#64;Param(description = "Search query", required = true)
 *         String query,
 *
 *         &#64;Param(description = "Maximum results", defaultValue = "10")
 *         int limit
 *     ) {
 *         return projectService.search(query, limit);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LLMFunction {

  /**
   * Function name (unique identifier). If empty, uses method name converted to snake_case.
   *
   * @return Function name
   */
  String name() default "";

  /**
   * Function description. This helps the LLM understand when to use this function. Be clear and
   * descriptive.
   *
   * @return Function description
   */
  String description();

  /**
   * Whether this function is enabled. Disabled functions are not registered.
   *
   * @return true if enabled (default), false if disabled
   */
  boolean enabled() default true;

  /**
   * Categories/tags for this function. Can be used for filtering or grouping.
   *
   * @return Function categories
   */
  String[] categories() default {};
}
