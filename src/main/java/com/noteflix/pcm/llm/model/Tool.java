package com.noteflix.pcm.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a tool/function available to the LLM.
 *
 * <p>Uses the standardized OpenAI function calling format:
 *
 * <pre>
 * {
 *   "type": "function",
 *   "function": {
 *     "name": "search_projects",
 *     "description": "Search for projects in database",
 *     "parameters": { ... }
 *   }
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tool {

    /**
     * Type of tool (always "function" for now). Reserved for future expansion.
     */
    @Builder.Default
    private String type = "function";

    /**
     * Function definition (name, description, parameters).
     */
    private FunctionDefinition function;

    /**
     * Create a function tool.
     *
     * @param name        Function name
     * @param description Function description
     * @param parameters  Parameter schema
     * @return Tool instance
     */
    public static Tool function(String name, String description, JsonSchema parameters) {
        return Tool.builder()
                .type("function")
                .function(
                        FunctionDefinition.builder()
                                .name(name)
                                .description(description)
                                .parameters(parameters)
                                .build())
                .build();
    }

    /**
     * Get function name (convenience method).
     */
    public String getName() {
        return function != null ? function.getName() : null;
    }

    /**
     * Get function description (convenience method).
     */
    public String getDescription() {
        return function != null ? function.getDescription() : null;
    }
}
