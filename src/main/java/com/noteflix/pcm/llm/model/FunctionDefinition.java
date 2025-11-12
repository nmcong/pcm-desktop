package com.noteflix.pcm.llm.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Function definition for LLM function calling
 *
 * <p>Describes a function that the LLM can call. Parameters use JSON Schema format.
 *
 * <p>Example:
 *
 * <pre>
 * FunctionDefinition weatherFunc = FunctionDefinition.builder()
 *     .name("get_current_weather")
 *     .description("Get the current weather in a given location")
 *     .parameters(Map.of(
 *         "type", "object",
 *         "properties", Map.of(
 *             "location", Map.of(
 *                 "type", "string",
 *                 "description", "City name, e.g. Tokyo"
 *             ),
 *             "unit", Map.of(
 *                 "type", "string",
 *                 "enum", List.of("celsius", "fahrenheit")
 *             )
 *         ),
 *         "required", List.of("location")
 *     ))
 *     .build();
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class FunctionDefinition {

  /**
   * Function name Must be unique, lowercase, no spaces Example: "get_weather", "search_database",
   * "send_email"
   */
  private String name;

  /**
   * Function description Tell the LLM what this function does Be clear and concise
   *
   * <p>Example: "Get the current weather for a specific location"
   */
  private String description;

  /**
   * Function parameters in JSON Schema format
   *
   * <p>Structure: { "type": "object", "properties": { "param1": {"type": "string", "description":
   * "..."}, "param2": {"type": "number", "description": "..."} }, "required": ["param1"] }
   *
   * <p>Supported types: string, number, integer, boolean, array, object
   */
  private Map<String, Object> parameters;

  /**
   * Validate function definition
   *
   * @throws IllegalArgumentException if validation fails
   */
  public void validate() {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Function name is required");
    }

    if (description == null || description.trim().isEmpty()) {
      throw new IllegalArgumentException("Function description is required");
    }

    if (parameters == null) {
      throw new IllegalArgumentException("Function parameters are required");
    }

    // Validate JSON Schema structure
    if (!"object".equals(parameters.get("type"))) {
      throw new IllegalArgumentException("Parameters type must be 'object'");
    }
  }
}
