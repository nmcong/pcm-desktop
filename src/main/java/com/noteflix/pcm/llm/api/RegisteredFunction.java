package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.JsonSchema;

import java.util.Map;

/**
 * Interface for functions that can be called by LLM.
 *
 * <p>Implement this interface to create custom functions. Functions are registered in
 * FunctionRegistry and made available to LLMs.
 *
 * <p>Example:
 *
 * <pre>
 * public class SearchProjectsFunction implements RegisteredFunction {
 *     &#64;Override
 *     public String getName() {
 *         return "search_projects";
 *     }
 *
 *     &#64;Override
 *     public String getDescription() {
 *         return "Search for projects in database";
 *     }
 *
 *     &#64;Override
 *     public JsonSchema getParameters() {
 *         return JsonSchema.builder()
 *             .property("query", PropertySchema.string("Search query"))
 *             .required("query")
 *             .build();
 *     }
 *
 *     &#64;Override
 *     public Object execute(Map&lt;String, Object&gt; args) {
 *         String query = (String) args.get("query");
 *         return projectService.search(query);
 *     }
 * }
 * </pre>
 */
public interface RegisteredFunction {

    /**
     * Get function name (unique identifier). Use snake_case convention (e.g., "search_projects").
     *
     * @return Function name
     */
    String getName();

    /**
     * Get function description. This helps the LLM understand when to use this function. Be clear and
     * descriptive.
     *
     * @return Function description
     */
    String getDescription();

    /**
     * Get parameter schema. Defines expected parameters, their types, and which are required.
     *
     * @return JSON Schema for parameters
     */
    JsonSchema getParameters();

    /**
     * Execute the function with given arguments.
     *
     * @param args Function arguments (from LLM)
     * @return Function result (will be converted to string for LLM)
     * @throws Exception if execution fails
     */
    Object execute(Map<String, Object> args) throws Exception;
}
