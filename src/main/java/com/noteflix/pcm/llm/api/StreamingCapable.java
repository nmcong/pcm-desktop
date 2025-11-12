package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.LLMChunk;
import com.noteflix.pcm.llm.model.LLMRequest;
import com.noteflix.pcm.llm.model.StreamingObserver;
import java.util.stream.Stream;

/**
 * Optional interface for LLM providers that support streaming
 *
 * <p>Follows SOLID principles: - Interface Segregation: Separated from base LLMClient interface
 * Only providers that support streaming need to implement this - Open/Closed: New streaming methods
 * can be added without modifying base interface
 *
 * <p>Design Pattern: Observer Pattern Observers are notified as chunks arrive
 *
 * <p>Usage: if (client instanceof StreamingCapable streamingClient) {
 * streamingClient.streamMessage(request, observer); }
 *
 * @author PCM Team
 * @version 1.0.0
 */
public interface StreamingCapable {

  /**
   * Stream message chunks in real-time using Java Stream API
   *
   * <p>This method returns a Stream that yields chunks as they arrive. The stream will complete
   * when the response is finished.
   *
   * <p>Example:
   *
   * <pre>
   * streamingClient.streamMessage(request)
   *     .forEach(chunk -> System.out.print(chunk.getContent()));
   * </pre>
   *
   * @param request The LLM request
   * @return Stream of response chunks
   * @throws com.noteflix.pcm.llm.exception.StreamingException if streaming fails
   */
  Stream<LLMChunk> streamMessage(LLMRequest request);

  /**
   * Stream message with observer pattern
   *
   * <p>This method uses callbacks instead of Stream API. Better for UI integration where you want
   * callbacks.
   *
   * <p>Example:
   *
   * <pre>
   * streamingClient.streamMessage(request, new StreamingObserver() {
   *     public void onChunk(LLMChunk chunk) {
   *         Platform.runLater(() -> textArea.appendText(chunk.getContent()));
   *     }
   *     public void onComplete() {
   *         System.out.println("Done!");
   *     }
   *     public void onError(Throwable error) {
   *         error.printStackTrace();
   *     }
   * });
   * </pre>
   *
   * @param request The LLM request
   * @param observer Observer to receive callbacks
   */
  void streamMessage(LLMRequest request, StreamingObserver observer);

  /**
   * Check if streaming is currently supported May depend on configuration or model
   *
   * @return true if streaming is supported
   */
  default boolean isStreamingSupported() {
    return true;
  }
}
