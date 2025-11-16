package com.noteflix.pcm.llm.exception;

/**
 * Exception thrown when a provider operation fails.
 *
 * <p>This can be network errors, API errors, authentication failures, etc.
 */
public class ProviderException extends LLMException {

    private final String providerName;
    private final int statusCode;

    public ProviderException(String providerName, String message) {
        super(String.format("Provider '%s' error: %s", providerName, message));
        this.providerName = providerName;
        this.statusCode = -1;
    }

    public ProviderException(String providerName, String message, Throwable cause) {
        super(String.format("Provider '%s' error: %s", providerName, message), cause);
        this.providerName = providerName;
        this.statusCode = -1;
    }

    public ProviderException(String providerName, int statusCode, String message) {
        super(String.format("Provider '%s' error (HTTP %d): %s", providerName, statusCode, message));
        this.providerName = providerName;
        this.statusCode = statusCode;
    }

    public String getProviderName() {
        return providerName;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isRateLimited() {
        return statusCode == 429;
    }

    public boolean isAuthError() {
        return statusCode == 401 || statusCode == 403;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }
}
