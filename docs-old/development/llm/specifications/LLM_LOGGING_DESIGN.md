# LLM Call Logging & Audit Trail System

## 🎯 Requirements

1. ✅ **Log LLM Calls** - Request & response
2. ✅ **Log Tool Calls** - Name, parameters, results
3. ✅ **Persistent Storage** - Database or file
4. ✅ **Queryable** - Search and filter logs
5. ✅ **Performance** - Async logging, no blocking
6. ✅ **Privacy** - Sensitive data masking
7. ✅ **Retention** - Auto-cleanup old logs

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│         LLMProvider (Enhanced)                  │
│  Before call:  log request                      │
│  After call:   log response                     │
│  Tool calls:   log each execution               │
└────────────────┬────────────────────────────────┘
                 │
      ┌──────────┴──────────┐
      │                     │
┌─────▼─────┐      ┌────────▼────────┐
│  Logger   │      │   Repository    │
│ Interface │      │   (Storage)     │
└───────────┘      └─────────────────┘
      │
      ├── FileLogger (JSON files)
      ├── DatabaseLogger (SQLite)
      └── CompositeLogger (Multiple)
```

---

## 📊 Data Models

### **1. LLMCallLog**

```java
@Data
@Builder
public class LLMCallLog {
    // Identification
    private String id;                    // Unique call ID
    private String conversationId;        // Link to conversation
    private String provider;              // openai, anthropic, ollama
    private String model;                 // gpt-4, claude-3.5, etc.
    
    // Timing
    private LocalDateTime timestamp;      // When call started
    private long durationMs;              // How long it took
    
    // Request
    private List<Message> requestMessages;
    private ChatOptions requestOptions;
    private List<Tool> requestTools;      // Available tools
    
    // Response
    private String responseContent;       // Text response
    private String thinkingContent;       // Thinking (if any)
    private List<ToolCallLog> toolCalls;  // Tool calls (if any)
    private Usage usage;                  // Token usage
    private String finishReason;          // stop, tool_calls, etc.
    
    // Error handling
    private boolean hasError;
    private String errorMessage;
    private String errorStackTrace;
    
    // Metadata
    private String userId;                // Who made the call
    private String sessionId;             // Session identifier
    private Map<String, Object> metadata; // Custom metadata
}
```

### **2. ToolCallLog**

```java
@Data
@Builder
public class ToolCallLog {
    // Identification
    private String id;                    // Tool call ID
    private String llmCallId;             // Parent LLM call
    private String toolName;              // Function name
    
    // Timing
    private LocalDateTime timestamp;
    private long executionMs;
    
    // Input
    private Map<String, Object> arguments; // Tool arguments
    
    // Output
    private boolean success;
    private Object result;                // Tool result
    private String errorMessage;          // If failed
    
    // Metadata
    private String provider;              // Where tool came from
    private int executionOrder;           // Order in sequence
}
```

### **3. Usage (Token Stats)**

```java
@Data
@Builder
public class Usage {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private int thinkingTokens;           // For reasoning models
    private double estimatedCost;         // In USD
}
```

---

## 🔧 Logger Interface

```java
public interface LLMCallLogger {
    
    /**
     * Log a complete LLM call
     */
    void logCall(LLMCallLog callLog);
    
    /**
     * Log tool execution
     */
    void logToolExecution(ToolCallLog toolLog);
    
    /**
     * Query logs
     */
    List<LLMCallLog> queryLogs(LogQuery query);
    
    /**
     * Get logs by conversation
     */
    List<LLMCallLog> getByConversation(String conversationId);
    
    /**
     * Get logs by time range
     */
    List<LLMCallLog> getByTimeRange(LocalDateTime start, LocalDateTime end);
    
    /**
     * Get statistics
     */
    LogStatistics getStatistics(LocalDateTime start, LocalDateTime end);
    
    /**
     * Delete old logs (retention policy)
     */
    int cleanupOldLogs(int retentionDays);
}
```

---

## 💾 Implementations

### **1. DatabaseLogger (SQLite)**

```java
@Slf4j
public class DatabaseLLMLogger implements LLMCallLogger {
    
    private Connection connection;
    private ExecutorService asyncExecutor;
    
    public DatabaseLLMLogger(String dbPath) {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        this.asyncExecutor = Executors.newSingleThreadExecutor();
        initializeTables();
    }
    
    private void initializeTables() {
        String createCallsTable = """
            CREATE TABLE IF NOT EXISTS llm_calls (
                id TEXT PRIMARY KEY,
                conversation_id TEXT,
                provider TEXT,
                model TEXT,
                timestamp DATETIME,
                duration_ms INTEGER,
                request_messages TEXT,
                request_options TEXT,
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
        
        String createToolsTable = """
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
        
        String createIndexes = """
            CREATE INDEX IF NOT EXISTS idx_conversation ON llm_calls(conversation_id);
            CREATE INDEX IF NOT EXISTS idx_timestamp ON llm_calls(timestamp);
            CREATE INDEX IF NOT EXISTS idx_user ON llm_calls(user_id);
            CREATE INDEX IF NOT EXISTS idx_tool_call ON tool_executions(llm_call_id);
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createCallsTable);
            stmt.execute(createToolsTable);
            stmt.execute(createIndexes);
        } catch (SQLException e) {
            log.error("Failed to initialize logging tables", e);
        }
    }
    
    @Override
    public void logCall(LLMCallLog callLog) {
        // Async to not block LLM calls
        asyncExecutor.submit(() -> {
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
        String sql = """
            INSERT INTO llm_calls (
                id, conversation_id, provider, model, timestamp, duration_ms,
                request_messages, request_options, response_content, thinking_content,
                usage_tokens, usage_cost, finish_reason, has_error, error_message,
                user_id, session_id, metadata
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            stmt.setString(9, log.getResponseContent());
            stmt.setString(10, log.getThinkingContent());
            stmt.setInt(11, log.getUsage().getTotalTokens());
            stmt.setDouble(12, log.getUsage().getEstimatedCost());
            stmt.setString(13, log.getFinishReason());
            stmt.setBoolean(14, log.isHasError());
            stmt.setString(15, log.getErrorMessage());
            stmt.setString(16, log.getUserId());
            stmt.setString(17, log.getSessionId());
            stmt.setString(18, toJson(log.getMetadata()));
            stmt.executeUpdate();
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
            log.error("Failed to query logs", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public LogStatistics getStatistics(LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT 
                COUNT(*) as total_calls,
                SUM(usage_tokens) as total_tokens,
                SUM(usage_cost) as total_cost,
                AVG(duration_ms) as avg_duration,
                COUNT(DISTINCT conversation_id) as unique_conversations
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
                    .build();
            }
        } catch (SQLException e) {
            log.error("Failed to get statistics", e);
        }
        
        return null;
    }
}
```

### **2. FileLogger (JSON)**

```java
@Slf4j
public class FileLLMLogger implements LLMCallLogger {
    
    private Path logDirectory;
    private ObjectMapper mapper;
    private ExecutorService asyncExecutor;
    
    public FileLLMLogger(String logDir) {
        this.logDirectory = Paths.get(logDir);
        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);
        this.asyncExecutor = Executors.newSingleThreadExecutor();
        
        try {
            Files.createDirectories(logDirectory);
        } catch (IOException e) {
            log.error("Failed to create log directory", e);
        }
    }
    
    @Override
    public void logCall(LLMCallLog callLog) {
        asyncExecutor.submit(() -> {
            try {
                String date = callLog.getTimestamp().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                );
                Path dateDir = logDirectory.resolve(date);
                Files.createDirectories(dateDir);
                
                Path logFile = dateDir.resolve(callLog.getId() + ".json");
                mapper.writeValue(logFile.toFile(), callLog);
                
                log.debug("Logged LLM call: {}", callLog.getId());
            } catch (IOException e) {
                log.error("Failed to write log file", e);
            }
        });
    }
    
    @Override
    public List<LLMCallLog> getByConversation(String conversationId) {
        // Scan files and filter by conversationId
        // More expensive than DB, but simpler
        List<LLMCallLog> logs = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(logDirectory)) {
            paths.filter(p -> p.toString().endsWith(".json"))
                 .forEach(path -> {
                     try {
                         LLMCallLog log = mapper.readValue(
                             path.toFile(), 
                             LLMCallLog.class
                         );
                         if (conversationId.equals(log.getConversationId())) {
                             logs.add(log);
                         }
                     } catch (IOException e) {
                         log.warn("Failed to read log file: {}", path);
                     }
                 });
        } catch (IOException e) {
            log.error("Failed to scan log files", e);
        }
        
        return logs;
    }
}
```

### **3. CompositeLogger**

```java
public class CompositeLLMLogger implements LLMCallLogger {
    
    private List<LLMCallLogger> loggers;
    
    public CompositeLLMLogger(LLMCallLogger... loggers) {
        this.loggers = Arrays.asList(loggers);
    }
    
    @Override
    public void logCall(LLMCallLog callLog) {
        // Log to all destinations
        for (LLMCallLogger logger : loggers) {
            logger.logCall(callLog);
        }
    }
    
    @Override
    public List<LLMCallLog> queryLogs(LogQuery query) {
        // Use first logger for queries
        return loggers.get(0).queryLogs(query);
    }
}
```

---

## 🔌 Integration with Provider

```java
public abstract class BaseProvider implements LLMProvider {
    
    protected LLMCallLogger logger;
    protected TokenCounter tokenCounter;
    
    @Override
    public CompletableFuture<ChatResponse> chat(
        List<Message> messages, 
        ChatOptions options
    ) {
        // Generate call ID
        String callId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        // Build log object
        LLMCallLog.LLMCallLogBuilder logBuilder = LLMCallLog.builder()
            .id(callId)
            .provider(getProviderName())
            .model(options.getModel())
            .timestamp(LocalDateTime.now())
            .requestMessages(messages)
            .requestOptions(options)
            .requestTools(options.getTools())
            .userId(getCurrentUserId())
            .sessionId(getCurrentSessionId());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Make actual LLM call
                ChatResponse response = makeAPICall(messages, options);
                
                // Calculate duration
                long duration = System.currentTimeMillis() - startTime;
                
                // Complete log
                LLMCallLog callLog = logBuilder
                    .durationMs(duration)
                    .responseContent(response.getContent())
                    .thinkingContent(response.getThinkingContent())
                    .usage(response.getUsage())
                    .finishReason(response.getFinishReason())
                    .toolCalls(convertToolCalls(response.getToolCalls()))
                    .hasError(false)
                    .build();
                
                // Log asynchronously
                logger.logCall(callLog);
                
                return response;
                
            } catch (Exception e) {
                // Log error
                long duration = System.currentTimeMillis() - startTime;
                
                LLMCallLog errorLog = logBuilder
                    .durationMs(duration)
                    .hasError(true)
                    .errorMessage(e.getMessage())
                    .errorStackTrace(getStackTrace(e))
                    .build();
                
                logger.logCall(errorLog);
                
                throw new LLMException("LLM call failed", e);
            }
        });
    }
    
    /**
     * Execute tools and log each execution
     */
    protected List<ToolResult> executeTools(
        String callId,
        List<ToolCall> toolCalls
    ) {
        List<ToolResult> results = new ArrayList<>();
        
        for (int i = 0; i < toolCalls.size(); i++) {
            ToolCall call = toolCalls.get(i);
            long startTime = System.currentTimeMillis();
            
            ToolCallLog.ToolCallLogBuilder toolLogBuilder = ToolCallLog.builder()
                .id(UUID.randomUUID().toString())
                .llmCallId(callId)
                .toolName(call.getName())
                .timestamp(LocalDateTime.now())
                .arguments(call.getArguments())
                .executionOrder(i);
            
            try {
                // Execute tool
                Object result = FunctionRegistry.getInstance()
                    .execute(call.getName(), call.getArguments());
                
                long duration = System.currentTimeMillis() - startTime;
                
                // Log success
                ToolCallLog toolLog = toolLogBuilder
                    .executionMs(duration)
                    .success(true)
                    .result(result)
                    .build();
                
                logger.logToolExecution(toolLog);
                
                results.add(ToolResult.success(call.getId(), result));
                
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                
                // Log error
                ToolCallLog toolLog = toolLogBuilder
                    .executionMs(duration)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
                
                logger.logToolExecution(toolLog);
                
                results.add(ToolResult.error(call.getId(), e.getMessage()));
            }
        }
        
        return results;
    }
}
```

---

## 🔍 Query & Analytics

```java
public class LogQuery {
    private String conversationId;
    private String provider;
    private String model;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String userId;
    private Boolean hasError;
    private Integer limit;
    private Integer offset;
}

public class LogStatistics {
    private int totalCalls;
    private long totalTokens;
    private double totalCost;
    private long avgDurationMs;
    private int uniqueConversations;
    private Map<String, Integer> callsByProvider;
    private Map<String, Integer> callsByModel;
    private Map<String, Integer> toolUsageCount;
}

// Usage
LogStatistics stats = logger.getStatistics(
    LocalDateTime.now().minusDays(7),
    LocalDateTime.now()
);

System.out.println("Total calls: " + stats.getTotalCalls());
System.out.println("Total cost: $" + stats.getTotalCost());
System.out.println("Avg duration: " + stats.getAvgDurationMs() + "ms");
```

---

## 🎯 Complete Example

```java
// 1. Setup logger at startup
LLMCallLogger logger = new CompositeLLMLogger(
    new DatabaseLLMLogger("logs/llm-calls.db"),
    new FileLLMLogger("logs/json")
);

// 2. Register with providers
OpenAIProvider openai = new OpenAIProvider();
openai.setLogger(logger);

// 3. Make LLM call - automatically logged!
ChatResponse response = openai.chat(messages, options).get();

// 4. Tool execution - automatically logged!
if (response.hasToolCalls()) {
    List<ToolResult> results = openai.executeTools(
        response.getCallId(),
        response.getToolCalls()
    );
}

// 5. Query logs
List<LLMCallLog> conversationLogs = logger.getByConversation(conversationId);

for (LLMCallLog log : conversationLogs) {
    System.out.println("Call: " + log.getId());
    System.out.println("  Model: " + log.getModel());
    System.out.println("  Duration: " + log.getDurationMs() + "ms");
    System.out.println("  Tokens: " + log.getUsage().getTotalTokens());
    System.out.println("  Cost: $" + log.getUsage().getEstimatedCost());
    
    if (log.getToolCalls() != null) {
        for (ToolCallLog toolLog : log.getToolCalls()) {
            System.out.println("  Tool: " + toolLog.getToolName());
            System.out.println("    Args: " + toolLog.getArguments());
            System.out.println("    Result: " + toolLog.getResult());
            System.out.println("    Duration: " + toolLog.getExecutionMs() + "ms");
        }
    }
}

// 6. Get statistics
LogStatistics stats = logger.getStatistics(
    LocalDateTime.now().minusDays(30),
    LocalDateTime.now()
);

System.out.println("=== 30 Day Statistics ===");
System.out.println("Total LLM calls: " + stats.getTotalCalls());
System.out.println("Total tokens used: " + stats.getTotalTokens());
System.out.println("Total cost: $" + String.format("%.2f", stats.getTotalCost()));
System.out.println("Average duration: " + stats.getAvgDurationMs() + "ms");
System.out.println("Unique conversations: " + stats.getUniqueConversations());
```

---

## 📊 UI Integration

```java
// In your UI
public class LLMLogsViewer {
    
    private LLMCallLogger logger;
    private TableView<LLMCallLog> logsTable;
    
    public void loadLogs(String conversationId) {
        List<LLMCallLog> logs = logger.getByConversation(conversationId);
        
        logsTable.getItems().setAll(logs);
    }
    
    public void showCallDetails(LLMCallLog log) {
        // Show in detail panel
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("LLM Call Details");
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Model: " + log.getModel()),
            new Label("Duration: " + log.getDurationMs() + "ms"),
            new Label("Tokens: " + log.getUsage().getTotalTokens()),
            new Label("Cost: $" + log.getUsage().getEstimatedCost()),
            new Separator(),
            new Label("Request:"),
            new TextArea(formatMessages(log.getRequestMessages())),
            new Separator(),
            new Label("Response:"),
            new TextArea(log.getResponseContent())
        );
        
        if (log.getToolCalls() != null && !log.getToolCalls().isEmpty()) {
            content.getChildren().add(new Separator());
            content.getChildren().add(new Label("Tool Calls:"));
            
            for (ToolCallLog toolLog : log.getToolCalls()) {
                content.getChildren().add(formatToolCall(toolLog));
            }
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.show();
    }
}
```

---

## ✅ Benefits

1. ✅ **Full Audit Trail** - Complete history of all LLM interactions
2. ✅ **Debugging** - Replay exact requests/responses
3. ✅ **Cost Tracking** - Monitor spending
4. ✅ **Performance Monitoring** - Track latency
5. ✅ **Tool Usage Analytics** - Which functions are called most
6. ✅ **Error Analysis** - Track failures
7. ✅ **Compliance** - Required for some industries

---

## 📁 File Structure

```
llm/
├── logging/
│   ├── LLMCallLogger.java         # Interface
│   ├── DatabaseLLMLogger.java     # SQLite impl
│   ├── FileLLMLogger.java         # JSON files impl
│   ├── CompositeLLMLogger.java    # Multiple loggers
│   ├── LLMCallLog.java            # Data model
│   ├── ToolCallLog.java           # Tool execution log
│   ├── LogQuery.java              # Query builder
│   └── LogStatistics.java         # Analytics
│
└── provider/
    └── BaseProvider.java           # Integrated logging
```

---

**Status:** 🎨 Complete Logging System Design - Ready for Implementation!

