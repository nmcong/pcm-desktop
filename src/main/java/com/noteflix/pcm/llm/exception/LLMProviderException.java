package com.noteflix.pcm.llm.exception;

/**
 * Exception for LLM provider errors (API errors, network issues, etc.)
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class LLMProviderException extends LLMException {

    private final int statusCode;
    private final String providerMessage;

    public LLMProviderException(String message) {
        super(message);
        this.statusCode = -1;
        this.providerMessage = null;
    }

    public LLMProviderException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.providerMessage = null;
    }

    public LLMProviderException(String message, int statusCode, String providerMessage) {
        super(message + " (Status: " + statusCode + ", Provider: " + providerMessage + ")");
        this.statusCode = statusCode;
        this.providerMessage = providerMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getProviderMessage() {
        return providerMessage;
    }
}
