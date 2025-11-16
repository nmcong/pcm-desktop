package com.noteflix.pcm.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Definition of a function/tool that can be called by the LLM.
 *
 * <p>Contains metadata about the function: - Name (unique identifier) - Description (helps LLM
 * understand when to use it) - Parameters (JSON schema defining inputs)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionDefinition {

    /**
     * Function name (unique identifier). Use snake_case convention (e.g., "search_projects",
     * "get_user_info").
     */
    private String name;

    /**
     * Function description. Be descriptive - this helps the LLM decide when to use the function.
     *
     * <p>Example: "Search for projects in the database. Returns matching projects with their
     * details."
     */
    private String description;

    /**
     * Parameter schema (JSON Schema format). Defines the expected parameters, their types, and which
     * are required.
     */
    private JsonSchema parameters;

    /**
     * Whether strict schema validation should be enforced. If true, LLM must provide exactly the
     * parameters defined.
     */
    @Builder.Default
    private boolean strict = false;
}
