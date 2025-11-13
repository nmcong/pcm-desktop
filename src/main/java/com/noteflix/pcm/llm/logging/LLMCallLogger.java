package com.noteflix.pcm.llm.logging;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for logging LLM calls and tool executions.
 *
 * <p>Implementations can log to: - Database (SQLite, PostgreSQL, etc.) - Files (JSON, CSV, etc.) -
 * Remote services (logging servers, analytics) - Multiple destinations (CompositeLogger)
 *
 * <p>Example usage:
 *
 * <pre>
 * LLMCallLogger logger = new DatabaseLLMLogger("logs/llm-calls.db");
 *
 * // Log a complete LLM call
 * logger.logCall(callLog);
 *
 * // Log tool execution
 * logger.logToolExecution(toolLog);
 *
 * // Query logs
 * List&lt;LLMCallLog&gt; logs = logger.getByConversation(conversationId);
 * </pre>
 */
public interface LLMCallLogger {

  /**
   * Log a complete LLM call.
   *
   * @param callLog Complete call log entry
   */
  void logCall(LLMCallLog callLog);

  /**
   * Log a tool execution.
   *
   * @param toolLog Tool execution log entry
   */
  void logToolExecution(ToolCallLog toolLog);

  /**
   * Get logs for a specific conversation.
   *
   * @param conversationId Conversation ID
   * @return List of call logs
   */
  List<LLMCallLog> getByConversation(String conversationId);

  /**
   * Get logs within a time range.
   *
   * @param start Start time
   * @param end End time
   * @return List of call logs
   */
  List<LLMCallLog> getByTimeRange(LocalDateTime start, LocalDateTime end);

  /**
   * Get statistics for a time range.
   *
   * @param start Start time
   * @param end End time
   * @return Log statistics
   */
  LogStatistics getStatistics(LocalDateTime start, LocalDateTime end);

  /**
   * Clean up old logs (retention policy).
   *
   * @param retentionDays Keep logs newer than this many days
   * @return Number of logs deleted
   */
  int cleanupOldLogs(int retentionDays);
}
