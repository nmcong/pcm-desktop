package com.noteflix.pcm.llm.model;

/**
 * Observer interface for streaming responses
 *
 * <p>Follows Observer Pattern for reactive streaming
 *
 * <p>Example usage:
 *
 * <pre>
 * streamingClient.streamMessage(request, new StreamingObserver() {
 *     private StringBuilder fullResponse = new StringBuilder();
 *
 *     @Override
 *     public void onChunk(LLMChunk chunk) {
 *         if (chunk.hasContent()) {
 *             fullResponse.append(chunk.getContent());
 *             System.out.print(chunk.getContent());
 *         }
 *     }
 *
 *     @Override
 *     public void onComplete() {
 *         System.out.println("\n\nFull response: " + fullResponse);
 *     }
 *
 *     @Override
 *     public void onError(Throwable error) {
 *         System.err.println("Error: " + error.getMessage());
 *     }
 * });
 * </pre>
 *
 * <p>For JavaFX UI integration:
 *
 * <pre>
 * streamingClient.streamMessage(request, new StreamingObserver() {
 *     @Override
 *     public void onChunk(LLMChunk chunk) {
 *         Platform.runLater(() -> {
 *             textArea.appendText(chunk.getContent());
 *         });
 *     }
 *
 *     @Override
 *     public void onComplete() {
 *         Platform.runLater(() -> {
 *             statusLabel.setText("Complete!");
 *         });
 *     }
 *
 *     @Override
 *     public void onError(Throwable error) {
 *         Platform.runLater(() -> {
 *             Alert alert = new Alert(Alert.AlertType.ERROR);
 *             alert.setContentText(error.getMessage());
 *             alert.show();
 *         });
 *     }
 * });
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
public interface StreamingObserver {

  /**
   * Called when a new chunk arrives
   *
   * <p>This method is called multiple times as chunks stream in. Process each chunk immediately for
   * real-time display.
   *
   * @param chunk The received chunk
   */
  void onChunk(LLMChunk chunk);

  /**
   * Called when streaming completes successfully
   *
   * <p>No more chunks will arrive after this. This is the place to do cleanup or final processing.
   */
  void onComplete();

  /**
   * Called when an error occurs during streaming
   *
   * <p>No more chunks will arrive after this. Handle the error appropriately (log, show to user,
   * retry, etc.)
   *
   * @param error The error that occurred
   */
  void onError(Throwable error);
}
