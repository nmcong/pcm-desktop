package com.noteflix.pcm.llm.exception;

/**
 * Exception thrown when a requested function is not found in registry.
 */
public class FunctionNotFoundException extends LLMException {

    private final String functionName;

    public FunctionNotFoundException(String functionName) {
        super(String.format("Function not found: '%s'", functionName));
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }
}
