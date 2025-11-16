package com.noteflix.pcm.llm.exception;

/**
 * Exception thrown when a function execution fails.
 *
 * <p>This wraps the underlying error from function execution, providing context about which
 * function failed and why.
 *
 * <p>Example:
 *
 * <pre>
 * try {
 *     Object result = functionRegistry.execute("search_projects", args);
 * } catch (FunctionExecutionException e) {
 *     log.error("Function {} failed: {}", e.getFunctionName(), e.getMessage());
 *     // Handle error, maybe return error to LLM
 * }
 * </pre>
 */
public class FunctionExecutionException extends LLMException {

    private final String functionName;
    private final Object arguments;

    public FunctionExecutionException(String functionName, String message) {
        super(String.format("Function '%s' execution failed: %s", functionName, message));
        this.functionName = functionName;
        this.arguments = null;
    }

    public FunctionExecutionException(String functionName, String message, Throwable cause) {
        super(String.format("Function '%s' execution failed: %s", functionName, message), cause);
        this.functionName = functionName;
        this.arguments = null;
    }

    public FunctionExecutionException(String functionName, Object arguments, Throwable cause) {
        super(
                String.format("Function '%s' execution failed with args %s", functionName, arguments),
                cause);
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    public Object getArguments() {
        return arguments;
    }
}
