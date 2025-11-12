package com.noteflix.pcm.llm.tool;

import com.noteflix.pcm.llm.exception.FunctionExecutionException;
import com.noteflix.pcm.llm.exception.FunctionNotFoundException;
import com.noteflix.pcm.llm.model.ToolCall;
import com.noteflix.pcm.llm.model.ToolResult;
import com.noteflix.pcm.llm.registry.FunctionRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executes tool/function calls requested by LLM.
 * 
 * Supports:
 * - Sequential execution (one after another)
 * - Parallel execution (when dependencies allow)
 * - Error handling for individual tool failures
 * - Execution timing and logging
 * 
 * Example usage:
 * <pre>
 * ToolExecutor executor = new ToolExecutor(FunctionRegistry.getInstance());
 * 
 * // Execute all tool calls sequentially
 * List&lt;ToolResult&gt; results = executor.executeAll(toolCalls);
 * 
 * // Execute with parallelization where possible
 * List&lt;ToolResult&gt; results = executor.executeOptimized(toolCalls);
 * </pre>
 */
@Slf4j
public class ToolExecutor {
    
    private final FunctionRegistry registry;
    private final ExecutorService executorService;
    
    public ToolExecutor(FunctionRegistry registry) {
        this.registry = registry;
        this.executorService = Executors.newFixedThreadPool(4);
    }
    
    /**
     * Execute all tool calls sequentially.
     * 
     * Tools are executed in order. If one fails, execution continues
     * with remaining tools (fail-fast is not used).
     * 
     * @param toolCalls List of tool calls to execute
     * @return List of tool results (in same order as calls)
     */
    public List<ToolResult> executeAll(List<ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            return new ArrayList<>();
        }
        
        log.info("Executing {} tool calls sequentially", toolCalls.size());
        
        List<ToolResult> results = new ArrayList<>();
        
        for (int i = 0; i < toolCalls.size(); i++) {
            ToolCall call = toolCalls.get(i);
            ToolResult result = executeSingle(call, i);
            results.add(result);
        }
        
        log.info("Completed {} tool executions", toolCalls.size());
        
        return results;
    }
    
    /**
     * Execute a single tool call.
     * 
     * @param call Tool call to execute
     * @param index Index in sequence (for logging)
     * @return Tool result
     */
    public ToolResult executeSingle(ToolCall call, int index) {
        String toolName = call.getName();
        Map<String, Object> arguments = call.getArguments();
        
        log.debug("Executing tool [{}]: {} with args: {}", index, toolName, arguments);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute function
            Object result = registry.execute(toolName, arguments);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Tool [{}] {} completed in {}ms", index, toolName, duration);
            
            return ToolResult.success(
                call.getId(),
                toolName,
                result,
                duration
            );
            
        } catch (FunctionNotFoundException e) {
            long duration = System.currentTimeMillis() - startTime;
            
            log.error("Tool [{}] {} not found after {}ms", 
                index, toolName, duration, e);
            
            return ToolResult.error(
                call.getId(),
                toolName,
                "Function not found: " + toolName,
                duration
            );
            
        } catch (FunctionExecutionException e) {
            long duration = System.currentTimeMillis() - startTime;
            
            log.error("Tool [{}] {} failed after {}ms: {}", 
                index, toolName, duration, e.getMessage(), e);
            
            return ToolResult.error(
                call.getId(),
                toolName,
                e.getMessage(),
                duration
            );
        }
    }
    
    /**
     * Execute tool calls with optimization (parallel where possible).
     * 
     * Currently executes sequentially. Future enhancement could analyze
     * dependencies and parallelize independent calls.
     * 
     * @param toolCalls List of tool calls to execute
     * @return List of tool results
     */
    public List<ToolResult> executeOptimized(List<ToolCall> toolCalls) {
        // TODO: Implement dependency analysis and parallel execution
        // For now, fall back to sequential execution
        log.debug("Optimized execution not yet implemented, using sequential");
        return executeAll(toolCalls);
    }
    
    /**
     * Execute tool calls in parallel.
     * 
     * All tools execute simultaneously. Use with caution -
     * only for tools with no dependencies on each other.
     * 
     * @param toolCalls List of tool calls to execute
     * @return List of tool results
     */
    public List<ToolResult> executeParallel(List<ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            return new ArrayList<>();
        }
        
        log.info("Executing {} tool calls in parallel", toolCalls.size());
        
        List<CompletableFuture<ToolResult>> futures = new ArrayList<>();
        
        for (int i = 0; i < toolCalls.size(); i++) {
            final ToolCall call = toolCalls.get(i);
            final int index = i;
            
            CompletableFuture<ToolResult> future = CompletableFuture.supplyAsync(
                () -> executeSingle(call, index),
                executorService
            );
            
            futures.add(future);
        }
        
        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // Collect results
        List<ToolResult> results = new ArrayList<>();
        for (CompletableFuture<ToolResult> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                log.error("Failed to get tool result", e);
            }
        }
        
        log.info("Completed {} parallel tool executions", toolCalls.size());
        
        return results;
    }
    
    /**
     * Shutdown the executor service.
     * Call this when done with the executor.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}

