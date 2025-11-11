package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.LLMRequest;
import com.noteflix.pcm.llm.model.LLMResponse;

/**
 * Base interface for all LLM clients
 * 
 * Follows SOLID principles:
 * - Single Responsibility: Only defines contract for LLM communication
 * - Interface Segregation: Base interface with minimal methods
 * - Dependency Inversion: High-level code depends on this abstraction
 * 
 * Design Pattern: Strategy Pattern
 * Different LLM providers implement this interface as different strategies
 * 
 * @author PCM Team
 * @version 1.0.0
 */
public interface LLMClient {
    
    /**
     * Send a message to the LLM and get response
     * 
     * This is the core method that all providers must implement
     * 
     * @param request The LLM request containing messages and parameters
     * @return The LLM response with generated content
     * @throws com.noteflix.pcm.llm.exception.LLMException if request fails
     */
    LLMResponse sendMessage(LLMRequest request);
    
    /**
     * Get the provider name
     * 
     * @return Provider name (e.g., "OpenAI", "Anthropic", "Ollama")
     */
    String getProviderName();
    
    /**
     * Check if the provider is available/reachable
     * 
     * @return true if provider is available
     */
    boolean isAvailable();
    
    /**
     * Get the model name this client is configured for
     * 
     * @return Model name (e.g., "gpt-4", "claude-3-5-sonnet")
     */
    String getModel();
}

