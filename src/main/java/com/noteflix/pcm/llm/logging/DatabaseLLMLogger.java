package com.noteflix.pcm.llm.logging;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

/**
 * SQLite-based logger for LLM calls.
 *
 * <p>Features: - Async logging (doesn't block LLM calls) - Indexed queries (fast lookups) - JSON
 * storage for complex objects - Automatic schema creation
 *
 * <p>Example usage:
 *
 * <pre>
 * LLMCallLogger logger = new DatabaseLLMLogger("logs/llm-calls.db");
 * logger.logCall(callLog);
 * </pre>
 */
@Slf4j
public class DatabaseLLMLogger implements LLMCallLogger {

    private final Connection connection;
    private final ObjectMapper objectMapper;
    private final ExecutorService asyncExecutor;

    public DatabaseLLMLogger(String dbPath) {
        try {
            // Ensure parent directory exists
            Path path = Paths.get(dbPath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            // Connect to SQLite
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            this.objectMapper = new ObjectMapper();
            this.asyncExecutor = Executors.newSingleThreadExecutor();

            // Initialize schema
            initializeTables();

            log.info("DatabaseLLMLogger initialized: {}", dbPath);

        } catch (Exception e) {
            log.error("Failed to initialize DatabaseLLMLogger", e);
            throw new RuntimeException("Failed to initialize logger", e);
        }
    }

    private void initializeTables() {
        String createCallsTable =
                """
                            CREATE TABLE IF NOT EXISTS llm_calls (
                                id TEXT PRIMARY KEY,
                                conversation_id TEXT,
                                provider TEXT,
                                model TEXT,
                                timestamp DATETIME,
                                duration_ms INTEGER,
                                request_messages TEXT,
                                request_options TEXT,
                                request_tools TEXT,
                                response_content TEXT,
                                thinking_content TEXT,
                                usage_tokens INTEGER,
                                usage_cost REAL,
                                finish_reason TEXT,
                                has_error BOOLEAN,
                                error_message TEXT,
                                user_id TEXT,
                                session_id TEXT,
                                metadata TEXT,
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                            )
                        """;

        String createToolsTable =
                """
                            CREATE TABLE IF NOT EXISTS tool_executions (
                                id TEXT PRIMARY KEY,
                                llm_call_id TEXT,
                                tool_name TEXT,
                                timestamp DATETIME,
                                execution_ms INTEGER,
                                arguments TEXT,
                                success BOOLEAN,
                                result TEXT,
                                error_message TEXT,
                                execution_order INTEGER,
                                FOREIGN KEY (llm_call_id) REFERENCES llm_calls(id)
                            )
                        """;

        String createIndexes =
                """
                            CREATE INDEX IF NOT EXISTS idx_conversation ON llm_calls(conversation_id);
                            CREATE INDEX IF NOT EXISTS idx_timestamp ON llm_calls(timestamp);
                            CREATE INDEX IF NOT EXISTS idx_user ON llm_calls(user_id);
                            CREATE INDEX IF NOT EXISTS idx_tool_call ON tool_executions(llm_call_id);
                        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createCallsTable);
            stmt.execute(createToolsTable);

            // Execute indexes one by one
            for (String index : createIndexes.split(";")) {
                if (!index.trim().isEmpty()) {
                    stmt.execute(index);
                }
            }

            log.debug("Database schema initialized");

        } catch (SQLException e) {
            log.error("Failed to initialize database schema", e);
        }
    }

    @Override
    public void logCall(LLMCallLog callLog) {
        // Async to not block LLM calls
        asyncExecutor.submit(
                () -> {
                    try {
                        insertCallLog(callLog);

                        // Log tool calls
                        if (callLog.getToolCalls() != null) {
                            for (ToolCallLog toolLog : callLog.getToolCalls()) {
                                insertToolLog(toolLog);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to log LLM call", e);
                    }
                });
    }

    private void insertCallLog(LLMCallLog log) throws SQLException {
        String sql =
                """
                            INSERT INTO llm_calls (
                                id, conversation_id, provider, model, timestamp, duration_ms,
                                request_messages, request_options, request_tools,
                                response_content, thinking_content,
                                usage_tokens, usage_cost, finish_reason, has_error, error_message,
                                user_id, session_id, metadata
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, log.getId());
            stmt.setString(2, log.getConversationId());
            stmt.setString(3, log.getProvider());
            stmt.setString(4, log.getModel());
            stmt.setObject(5, log.getTimestamp());
            stmt.setLong(6, log.getDurationMs());
            stmt.setString(7, toJson(log.getRequestMessages()));
            stmt.setString(8, toJson(log.getRequestOptions()));
            stmt.setString(9, toJson(log.getRequestTools()));
            stmt.setString(10, log.getResponseContent());
            stmt.setString(11, log.getThinkingContent());
            stmt.setInt(12, log.getUsage() != null ? log.getUsage().getTotalTokens() : 0);
            stmt.setDouble(
                    13,
                    log.getUsage() != null && log.getUsage().getEstimatedCost() != null
                            ? log.getUsage().getEstimatedCost()
                            : 0.0);
            stmt.setString(14, log.getFinishReason());
            stmt.setBoolean(15, log.isHasError());
            stmt.setString(16, log.getErrorMessage());
            stmt.setString(17, log.getUserId());
            stmt.setString(18, log.getSessionId());
            stmt.setString(19, toJson(log.getMetadata()));
            stmt.executeUpdate();

            DatabaseLLMLogger.log.debug("Logged LLM call: {}", log.getId());
        }
    }

    @Override
    public void logToolExecution(ToolCallLog toolLog) {
        asyncExecutor.submit(
                () -> {
                    try {
                        insertToolLog(toolLog);
                    } catch (Exception e) {
                        log.error("Failed to log tool execution", e);
                    }
                });
    }

    private void insertToolLog(ToolCallLog toolLog) throws SQLException {
        String sql =
                """
                            INSERT INTO tool_executions (
                                id, llm_call_id, tool_name, timestamp, execution_ms,
                                arguments, success, result, error_message, execution_order
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, toolLog.getId());
            stmt.setString(2, toolLog.getLlmCallId());
            stmt.setString(3, toolLog.getToolName());
            stmt.setObject(4, toolLog.getTimestamp());
            stmt.setLong(5, toolLog.getExecutionMs());
            stmt.setString(6, toJson(toolLog.getArguments()));
            stmt.setBoolean(7, toolLog.isSuccess());
            stmt.setString(8, toJson(toolLog.getResult()));
            stmt.setString(9, toolLog.getErrorMessage());
            stmt.setInt(10, toolLog.getExecutionOrder());
            stmt.executeUpdate();

            DatabaseLLMLogger.log.debug("Logged tool execution: {}", toolLog.getId());
        }
    }

    @Override
    public List<LLMCallLog> getByConversation(String conversationId) {
        String sql = "SELECT * FROM llm_calls WHERE conversation_id = ? ORDER BY timestamp";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, conversationId);
            ResultSet rs = stmt.executeQuery();
            return mapResultSet(rs);
        } catch (SQLException e) {
            log.error("Failed to query logs by conversation", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<LLMCallLog> getByTimeRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM llm_calls WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, start);
            stmt.setObject(2, end);
            ResultSet rs = stmt.executeQuery();
            return mapResultSet(rs);
        } catch (SQLException e) {
            log.error("Failed to query logs by time range", e);
            return Collections.emptyList();
        }
    }

    @Override
    public LogStatistics getStatistics(LocalDateTime start, LocalDateTime end) {
        String sql =
                """
                            SELECT
                                COUNT(*) as total_calls,
                                SUM(usage_tokens) as total_tokens,
                                SUM(usage_cost) as total_cost,
                                AVG(duration_ms) as avg_duration,
                                COUNT(DISTINCT conversation_id) as unique_conversations,
                                SUM(CASE WHEN has_error = 1 THEN 1 ELSE 0 END) as error_count
                            FROM llm_calls
                            WHERE timestamp BETWEEN ? AND ?
                        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, start);
            stmt.setObject(2, end);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return LogStatistics.builder()
                        .totalCalls(rs.getInt("total_calls"))
                        .totalTokens(rs.getLong("total_tokens"))
                        .totalCost(rs.getDouble("total_cost"))
                        .avgDurationMs(rs.getLong("avg_duration"))
                        .uniqueConversations(rs.getInt("unique_conversations"))
                        .errorCount(rs.getInt("error_count"))
                        .build();
            }
        } catch (SQLException e) {
            log.error("Failed to get statistics", e);
        }

        return null;
    }

    @Override
    public int cleanupOldLogs(int retentionDays) {
        String sql = "DELETE FROM llm_calls WHERE timestamp < ?";
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, cutoff);
            int deleted = stmt.executeUpdate();
            log.info("Cleaned up {} old logs (older than {} days)", deleted, retentionDays);
            return deleted;
        } catch (SQLException e) {
            log.error("Failed to cleanup old logs", e);
            return 0;
        }
    }

    private List<LLMCallLog> mapResultSet(ResultSet rs) throws SQLException {
        List<LLMCallLog> logs = new ArrayList<>();

        while (rs.next()) {
            LLMCallLog log =
                    LLMCallLog.builder()
                            .id(rs.getString("id"))
                            .conversationId(rs.getString("conversation_id"))
                            .provider(rs.getString("provider"))
                            .model(rs.getString("model"))
                            .timestamp(rs.getObject("timestamp", LocalDateTime.class))
                            .durationMs(rs.getLong("duration_ms"))
                            .responseContent(rs.getString("response_content"))
                            .thinkingContent(rs.getString("thinking_content"))
                            .finishReason(rs.getString("finish_reason"))
                            .hasError(rs.getBoolean("has_error"))
                            .errorMessage(rs.getString("error_message"))
                            .userId(rs.getString("user_id"))
                            .sessionId(rs.getString("session_id"))
                            .build();

            logs.add(log);
        }

        return logs;
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize object to JSON", e);
            return null;
        }
    }

    /**
     * Close the logger and cleanup resources.
     */
    public void close() {
        try {
            asyncExecutor.shutdown();
            connection.close();
            log.info("DatabaseLLMLogger closed");
        } catch (SQLException e) {
            log.error("Failed to close DatabaseLLMLogger", e);
        }
    }
}
