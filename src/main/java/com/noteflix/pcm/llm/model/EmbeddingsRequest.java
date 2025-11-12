package com.noteflix.pcm.llm.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Request model for embeddings generation
 *
 * <p>Example usage:
 *
 * <pre>
 * EmbeddingsRequest request = EmbeddingsRequest.builder()
 *     .model("text-embedding-ada-002")
 *     .input("Hello world")
 *     .build();
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class EmbeddingsRequest {

  /**
   * Model to use for embeddings
   *
   * <p>OpenAI: "text-embedding-ada-002", "text-embedding-3-small", "text-embedding-3-large" Ollama:
   * "nomic-embed-text", "mxbai-embed-large"
   */
  private String model;

  /** Input text(s) to generate embeddings for Can be a single string or list of strings */
  private Object input;

  /** User identifier for tracking (optional) */
  private String user;

  /** Helper method to create request for single text */
  public static EmbeddingsRequest forText(String text, String model) {
    return EmbeddingsRequest.builder().model(model).input(text).build();
  }

  /** Helper method to create request for multiple texts */
  public static EmbeddingsRequest forTexts(List<String> texts, String model) {
    return EmbeddingsRequest.builder().model(model).input(texts).build();
  }

  /**
   * Validate request
   *
   * @throws IllegalArgumentException if validation fails
   */
  public void validate() {
    if (model == null || model.trim().isEmpty()) {
      throw new IllegalArgumentException("Model is required");
    }

    if (input == null) {
      throw new IllegalArgumentException("Input is required");
    }

    if (input instanceof String) {
      if (((String) input).trim().isEmpty()) {
        throw new IllegalArgumentException("Input text cannot be empty");
      }
    } else if (input instanceof List) {
      List<?> inputList = (List<?>) input;
      if (inputList.isEmpty()) {
        throw new IllegalArgumentException("Input list cannot be empty");
      }
    } else {
      throw new IllegalArgumentException("Input must be String or List<String>");
    }
  }
}
