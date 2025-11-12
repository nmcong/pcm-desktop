package com.noteflix.pcm.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a tool/function call requested by the LLM.
 * 
 * In the new architecture, LLMs can request multiple tool calls in a single response.
 * Each tool call has a unique ID and includes the function name and arguments.
 * 
 * Example:
 * <pre>
 * ToolCall.builder()
 *   .id("call_abc123")
 *   .type("function")
 *   .function(FunctionCall.builder()
 *     .name("search_projects")
 *     .arguments(Map.of("query", "noteflix"))
 *     .build())
 *   .build()
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {
    
    /**
     * Unique identifier for this tool call.
     * Used to link tool results back to the request.
     */
    private String id;
    
    /**
     * Type of tool (always "function" for now).
     * Reserved for future expansion (e.g., "code_interpreter", "retrieval").
     */
    @Builder.Default
    private String type = "function";
    
    /**
     * Function details (name and arguments).
     */
    private FunctionCall function;
    
    /**
     * Index in the sequence of tool calls (0-based).
     * Used when multiple tools are called in order.
     */
    @Builder.Default
    private int index = 0;
    
    /**
     * Get the function name (convenience method).
     */
    public String getName() {
        return function != null ? function.getName() : null;
    }
    
    /**
     * Get the function arguments (convenience method).
     * Parses arguments from JSON string to Map.
     */
    public Map<String, Object> getArguments() {
        if (function == null) {
            return null;
        }
        
        // If function has arguments as string, parse it
        if (function.getArguments() != null) {
            try {
                return function.parseArgumentsAsMap();
            } catch (Exception e) {
                // If parsing fails, return empty map
                return new java.util.HashMap<>();
            }
        }
        
        return null;
    }
}

