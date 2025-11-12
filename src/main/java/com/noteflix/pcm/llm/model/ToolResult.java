package com.noteflix.pcm.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a tool/function execution.
 * Contains both the raw result and metadata about the execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {
    
    /**
     * Tool call ID this result corresponds to.
     */
    private String toolCallId;
    
    /**
     * Name of the tool that was executed.
     */
    private String toolName;
    
    /**
     * Whether the tool execution succeeded.
     */
    private boolean success;
    
    /**
     * The result object from tool execution.
     * Can be any type - will be serialized to string when sent to LLM.
     */
    private Object result;
    
    /**
     * Error message if execution failed.
     */
    private String error;
    
    /**
     * Execution time in milliseconds.
     */
    private long executionTimeMs;
    
    /**
     * Create a successful tool result.
     */
    public static ToolResult success(String toolCallId, Object result) {
        return ToolResult.builder()
            .toolCallId(toolCallId)
            .success(true)
            .result(result)
            .build();
    }
    
    /**
     * Create a successful tool result with execution time.
     */
    public static ToolResult success(String toolCallId, String toolName, Object result, long executionTimeMs) {
        return ToolResult.builder()
            .toolCallId(toolCallId)
            .toolName(toolName)
            .success(true)
            .result(result)
            .executionTimeMs(executionTimeMs)
            .build();
    }
    
    /**
     * Create an error tool result.
     */
    public static ToolResult error(String toolCallId, String error) {
        return ToolResult.builder()
            .toolCallId(toolCallId)
            .success(false)
            .error(error)
            .build();
    }
    
    /**
     * Create an error tool result with execution time.
     */
    public static ToolResult error(String toolCallId, String toolName, String error, long executionTimeMs) {
        return ToolResult.builder()
            .toolCallId(toolCallId)
            .toolName(toolName)
            .success(false)
            .error(error)
            .executionTimeMs(executionTimeMs)
            .build();
    }
    
    /**
     * Get result as string for sending to LLM.
     */
    public String getResultAsString() {
        if (!success) {
            return "Error: " + error;
        }
        return result != null ? result.toString() : "";
    }
}

