package com.noteflix.pcm.llm.prompt;

import java.util.Map;

/**
 * Interface for prompt templates.
 *
 * <p>Templates allow customization of prompts without modifying code. Supports variable
 * substitution, i18n, and dynamic generation.
 *
 * <p>Example:
 *
 * <pre>
 * PromptTemplate template = ...;
 * String prompt = template.render(Map.of(
 *     "role", "Java expert",
 *     "context", "Spring Boot application"
 * ));
 * </pre>
 */
public interface PromptTemplate {

    /**
     * Render template with variables.
     *
     * @param variables Variables to substitute in template
     * @return Rendered prompt string
     */
    String render(Map<String, Object> variables);

    /**
     * Get template name.
     *
     * @return Template name
     */
    String getName();

    /**
     * Get template description.
     *
     * @return Description of what this template does
     */
    String getDescription();
}
