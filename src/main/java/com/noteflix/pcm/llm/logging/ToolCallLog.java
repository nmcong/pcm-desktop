package com.noteflix.pcm.llm.logging;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Log entry for a tool/function execution.
 *
 * <p>Records: - Which tool was called - What arguments were passed - What result was returned - How
 * long it took - Whether it succeeded or failed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallLog {

  // ========== Identification ==========

  /** Unique tool call ID */
  private String id;

  /** Parent LLM call ID */
  private String llmCallId;

  /** Tool/function name */
  private String toolName;

  // ========== Timing ==========

  /** When execution started */
  @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();

  /** Execution duration (milliseconds) */
  private long executionMs;

  // ========== Input ==========

  /** Function arguments */
  private Map<String, Object> arguments;

  // ========== Output ==========

  /** Whether execution succeeded */
  private boolean success;

  /** Function result (if successful) */
  private Object result;

  /** Error message (if failed) */
  private String errorMessage;

  // ========== Metadata ==========

  /** Provider that registered this function */
  private String provider;

  /** Order in sequence (0-based) */
  private int executionOrder;
}
