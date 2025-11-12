package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Universal LLM response model
 * <p>
 * Contains the generated response, function calls, usage stats, etc.
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class LLMResponse {

    /**
     * Unique response ID from provider
     * Example: "chatcmpl-123456"
     */
    private String id;

    /**
     * Model that generated this response
     * Example: "gpt-4-0613"
     */
    private String model;

    /**
     * Main response content (text)
     * This is what the LLM generated
     */
    private String content;

    /**
     * Finish reason
     * - "stop": Natural completion
     * - "length": Hit max tokens
     * - "function_call": LLM wants to call a function
     * - "content_filter": Content filtered
     */
    private String finishReason;

    /**
     * Function call request from LLM
     * Only present if finishReason is "function_call"
     */
    private FunctionCall functionCall;

    /**
     * Token usage statistics
     */
    private Usage usage;

    /**
     * Response creation timestamp
     */
    private Instant createdAt;

    /**
     * Additional metadata from provider
     */
    private Map<String, Object> metadata;

    /**
     * Check if response contains a function call
     *
     * @return true if LLM wants to call a function
     */
    public boolean hasFunctionCall() {
        return functionCall != null;
    }

    /**
     * Check if response is complete
     *
     * @return true if finish reason is "stop"
     */
    public boolean isComplete() {
        return "stop".equals(finishReason);
    }

    /**
     * Token usage statistics
     */
    @Data
    @Builder
    public static class Usage {

        /**
         * Tokens in the prompt (input)
         */
        private Integer promptTokens;

        /**
         * Tokens in the completion (output)
         */
        private Integer completionTokens;

        /**
         * Total tokens (prompt + completion)
         */
        private Integer totalTokens;

        /**
         * Calculate cost (example for GPT-4)
         * Actual costs vary by model and provider
         *
         * @param promptCostPer1k     Cost per 1000 prompt tokens
         * @param completionCostPer1k Cost per 1000 completion tokens
         * @return Estimated cost in USD
         */
        public double estimateCost(double promptCostPer1k, double completionCostPer1k) {
            double promptCost = (promptTokens / 1000.0) * promptCostPer1k;
            double completionCost = (completionTokens / 1000.0) * completionCostPer1k;
            return promptCost + completionCost;
        }
    }
}

