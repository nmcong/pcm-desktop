package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.ChatResponse;
import com.noteflix.pcm.llm.model.ToolCall;

/**
 * Event listener for LLM chat interactions.
 *
 * <p>Provides callbacks for all stages of LLM communication: - Token streaming (for real-time
 * display) - Thinking process (for reasoning models like o1/o3) - Tool calls (when LLM wants to use
 * a function) - Completion (when response is done) - Errors (when something goes wrong)
 *
 * <p>This enables rich UI integration with progress indicators, streaming text display, and error
 * handling.
 *
 * <p>Example usage:
 *
 * <pre>
 * provider.chatStream(messages, options, new ChatEventListener() {
 *     &#64;Override
 *     public void onToken(String token) {
 *         // Append to UI in real-time
 *         textArea.appendText(token);
 *     }
 *
 *     &#64;Override
 *     public void onComplete(ChatResponse response) {
 *         // Show final token count
 *         statusLabel.setText("Done: " + response.getUsage().getTotalTokens() + " tokens");
 *     }
 *
 *     &#64;Override
 *     public void onError(Throwable error) {
 *         // Show error dialog
 *         showError(error.getMessage());
 *     }
 * });
 * </pre>
 */
public interface ChatEventListener {

  /**
   * Called when a new token is received from the LLM. Only called during streaming responses.
   *
   * @param token The text token received
   */
  void onToken(String token);

  /**
   * Called when a thinking token is received (for reasoning models). This is the internal reasoning
   * process, not the final answer.
   *
   * @param thinkingToken The thinking/reasoning text
   */
  void onThinking(String thinkingToken);

  /**
   * Called when the LLM requests a tool/function call. May be called multiple times if LLM requests
   * multiple tools.
   *
   * @param toolCall The tool call details (name, arguments, etc.)
   */
  void onToolCall(ToolCall toolCall);

  /**
   * Called when the LLM response is complete. Contains the full response with all metadata.
   *
   * @param response The complete chat response
   */
  void onComplete(ChatResponse response);

  /**
   * Called when an error occurs during the LLM interaction.
   *
   * @param error The error that occurred
   */
  void onError(Throwable error);
}
