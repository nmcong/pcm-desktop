package com.noteflix.pcm.llm.exception;

/**
 * Base exception for all LLM-related errors.
 * 
 * This is the parent exception for all LLM module exceptions.
 * Catch this to handle any LLM error generically.
 */
public class LLMException extends Exception {
    
    public LLMException(String message) {
        super(message);
    }
    
    public LLMException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public LLMException(Throwable cause) {
        super(cause);
    }
}
