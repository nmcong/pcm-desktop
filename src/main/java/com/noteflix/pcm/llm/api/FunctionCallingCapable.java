package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.FunctionDefinition;
import com.noteflix.pcm.llm.model.LLMRequest;
import com.noteflix.pcm.llm.model.LLMResponse;

import java.util.List;

/**
 * Optional interface for LLM providers that support function calling
 * <p>
 * Function calling allows the LLM to:
 * 1. Understand available functions/tools
 * 2. Decide when to call them based on user input
 * 3. Generate structured arguments for the function
 * <p>
 * Follows SOLID principles:
 * - Interface Segregation: Separated capability
 * - Single Responsibility: Only handles function calling
 * <p>
 * Design Pattern: Strategy Pattern
 * Different providers implement function calling differently
 * <p>
 * Workflow:
 * 1. User asks: "What's the weather in Tokyo?"
 * 2. LLM decides to call get_weather("Tokyo")
 * 3. Your code executes get_weather function
 * 4. Send result back to LLM
 * 5. LLM generates natural language response
 *
 * @author PCM Team
 * @version 1.0.0
 */
public interface FunctionCallingCapable {

    /**
     * Send message with function definitions
     * <p>
     * The LLM can decide to:
     * - Answer directly (normal response)
     * - Call one or more functions (function_call in response)
     * <p>
     * Example:
     * <pre>
     * FunctionDefinition weatherFunc = FunctionDefinition.builder()
     *     .name("get_weather")
     *     .description("Get current weather")
     *     .parameters(createJsonSchema())
     *     .build();
     *
     * LLMResponse response = client.sendWithFunctions(request, List.of(weatherFunc));
     *
     * if (response.getFunctionCall() != null) {
     *     // Execute function and send result back
     * }
     * </pre>
     *
     * @param request   The LLM request with user message
     * @param functions Available functions the LLM can call
     * @return Response (may contain function call or direct answer)
     * @throws com.noteflix.pcm.llm.exception.FunctionExecutionException if function calling fails
     */
    LLMResponse sendWithFunctions(
            LLMRequest request,
            List<FunctionDefinition> functions
    );

    /**
     * Check if function calling is supported
     * May depend on model or configuration
     *
     * @return true if function calling is supported
     */
    boolean supportsFunctionCalling();

    /**
     * Get maximum number of functions supported in single request
     * Some providers limit the number of functions
     *
     * @return Maximum number of functions, or -1 for unlimited
     */
    default int getMaxFunctions() {
        return -1; // Unlimited by default
    }
}

