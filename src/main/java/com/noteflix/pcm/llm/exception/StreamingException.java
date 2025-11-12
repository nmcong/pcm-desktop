package com.noteflix.pcm.llm.exception;

/**
 * Exception for streaming errors
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class StreamingException extends LLMException {

    public StreamingException(String message) {
        super(message);
    }

    public StreamingException(String message, Throwable cause) {
        super(message, cause);
    }
}

