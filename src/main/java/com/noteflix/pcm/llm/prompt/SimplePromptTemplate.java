package com.noteflix.pcm.llm.prompt;

import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

/**
 * Simple string-based prompt template.
 *
 * <p>Uses {variable} syntax for placeholders.
 *
 * <p>Example:
 *
 * <pre>
 * SimplePromptTemplate template = SimplePromptTemplate.builder()
 *     .name("expert_assistant")
 *     .template("You are a {role} expert. Help with: {task}")
 *     .requiredVariables(Set.of("role", "task"))
 *     .build();
 *
 * String prompt = template.render(Map.of(
 *     "role", "Java",
 *     "task", "debugging"
 * ));
 * // Output: "You are a Java expert. Help with: debugging"
 * </pre>
 */
@Data
@Builder
public class SimplePromptTemplate implements PromptTemplate {

    private String name;
    private String description;
    private String template;

    @Builder.Default
    private Set<String> requiredVariables = Set.of();

    @Override
    public String render(Map<String, Object> variables) {
        // Validate required variables
        for (String required : requiredVariables) {
            if (!variables.containsKey(required)) {
                throw new IllegalArgumentException("Missing required variable: " + required);
            }
        }

        // Simple string replacement
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(placeholder, value);
        }

        return result;
    }
}
