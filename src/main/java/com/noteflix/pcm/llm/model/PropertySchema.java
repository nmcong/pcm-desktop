package com.noteflix.pcm.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Schema for a single property in a JSON Schema.
 * 
 * Defines type, description, constraints, and validation rules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySchema {
    
    /**
     * Property type (string, number, integer, boolean, array, object).
     */
    private String type;
    
    /**
     * Property description (helps LLM understand the parameter).
     */
    private String description;
    
    /**
     * Enum values (if property must be one of specific values).
     */
    private List<Object> enumValues;
    
    /**
     * Default value.
     */
    private Object defaultValue;
    
    /**
     * Minimum value (for numbers).
     */
    private Double minimum;
    
    /**
     * Maximum value (for numbers).
     */
    private Double maximum;
    
    /**
     * Minimum length (for strings/arrays).
     */
    private Integer minLength;
    
    /**
     * Maximum length (for strings/arrays).
     */
    private Integer maxLength;
    
    /**
     * Pattern (regex for strings).
     */
    private String pattern;
    
    /**
     * Items schema (for arrays).
     */
    private PropertySchema items;
    
    /**
     * Create a string property.
     */
    public static PropertySchema string(String description) {
        return PropertySchema.builder()
            .type("string")
            .description(description)
            .build();
    }
    
    /**
     * Create an integer property.
     */
    public static PropertySchema integer(String description) {
        return PropertySchema.builder()
            .type("integer")
            .description(description)
            .build();
    }
    
    /**
     * Create a number property.
     */
    public static PropertySchema number(String description) {
        return PropertySchema.builder()
            .type("number")
            .description(description)
            .build();
    }
    
    /**
     * Create a boolean property.
     */
    public static PropertySchema bool(String description) {
        return PropertySchema.builder()
            .type("boolean")
            .description(description)
            .build();
    }
    
    /**
     * Create an array property.
     */
    public static PropertySchema array(String description, PropertySchema items) {
        return PropertySchema.builder()
            .type("array")
            .description(description)
            .items(items)
            .build();
    }
    
    /**
     * Create an enum property.
     */
    public static PropertySchema enumeration(String description, List<Object> values) {
        return PropertySchema.builder()
            .type("string")
            .description(description)
            .enumValues(values)
            .build();
    }
}

