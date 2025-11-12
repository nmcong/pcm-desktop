package com.noteflix.pcm.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;
import java.util.Map;

/**
 * JSON Schema for function parameters.
 * 
 * Defines the structure and types of parameters for a function.
 * Based on JSON Schema specification.
 * 
 * Example:
 * <pre>
 * JsonSchema.builder()
 *   .type("object")
 *   .property("query", PropertySchema.string("Search query"))
 *   .property("limit", PropertySchema.integer("Max results"))
 *   .required("query")
 *   .build()
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonSchema {
    
    /**
     * Schema type (usually "object" for function parameters).
     */
    @Builder.Default
    private String type = "object";
    
    /**
     * Property definitions.
     * Map of property name to property schema.
     */
    @Singular
    private Map<String, PropertySchema> properties;
    
    /**
     * Required properties.
     * List of property names that must be provided.
     */
    @Singular("required")
    private List<String> required;
    
    /**
     * Additional properties allowed?
     */
    private Boolean additionalProperties;
    
    /**
     * Schema description.
     */
    private String description;
    
    /**
     * Create a simple object schema.
     */
    public static JsonSchema object() {
        return JsonSchema.builder()
            .type("object")
            .build();
    }
}

