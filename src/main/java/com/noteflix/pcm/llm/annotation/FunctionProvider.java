package com.noteflix.pcm.llm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as containing LLM-callable functions.
 *
 * <p>Classes annotated with @FunctionProvider are scanned for methods annotated with @LLMFunction.
 *
 * <p>The class must be in the application context (managed by DI) to ensure functions execute with
 * proper dependencies.
 *
 * <p>Example:
 *
 * <pre>
 * &#64;FunctionProvider
 * public class ProjectFunctions {
 *
 *     private final ProjectService projectService;
 *
 *     // Constructor injection via DI
 *     public ProjectFunctions(ProjectService projectService) {
 *         this.projectService = projectService;
 *     }
 *
 *     &#64;LLMFunction(description = "Search projects")
 *     public List&lt;Project&gt; searchProjects(
 *         &#64;Param(description = "Query", required = true)
 *         String query
 *     ) {
 *         return projectService.search(query);
 *     }
 *
 *     &#64;LLMFunction(description = "Get project details")
 *     public Project getProject(
 *         &#64;Param(description = "Project ID", required = true)
 *         int id
 *     ) {
 *         return projectService.findById(id);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionProvider {

    /**
     * Provider name (for identification). If empty, uses class name.
     *
     * @return Provider name
     */
    String value() default "";

    /**
     * Whether this provider is enabled. Disabled providers are not scanned.
     *
     * @return true if enabled (default), false if disabled
     */
    boolean enabled() default true;

    /**
     * Priority for provider loading. Higher priority providers are loaded first.
     *
     * @return Priority (default 0)
     */
    int priority() default 0;
}
