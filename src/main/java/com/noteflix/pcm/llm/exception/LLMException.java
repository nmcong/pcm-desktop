package com.noteflix.pcm.llm.exception;

/**
 * Base exception for LLM operations
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class LLMException extends RuntimeException {

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
