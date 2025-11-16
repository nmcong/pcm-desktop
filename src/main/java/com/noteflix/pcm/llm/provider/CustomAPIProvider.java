package com.noteflix.pcm.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noteflix.pcm.llm.api.ChatEventAdapter;
import com.noteflix.pcm.llm.api.ChatEventListener;
import com.noteflix.pcm.llm.exception.ProviderException;
import com.noteflix.pcm.llm.model.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Custom API Provider for your existing LLM service.
 *
 * <p>Features: - Conversation management (create conversation) - SSE streaming with built-in
 * thinking support - Function calling injection into content - Token tracking
 *
 * <p>API Endpoints: - POST /api/chat/create - Create conversation, returns ID - POST
 * /api/chat/stream - Stream chat (SSE) - GET /api/chat/tokens/{conversationId} - Get remaining
 * tokens
 */
@Slf4j
public class CustomAPIProvider extends BaseProvider {

    private static final String DEFAULT_MODEL = "default";

    private final ObjectMapper objectMapper;
    private final Map<String, String> conversationCache; // conversation tracking

    public CustomAPIProvider() {
        super();
        this.objectMapper = new ObjectMapper();
        this.conversationCache = new HashMap<>();
    }

    @Override
    public String getName() {
        return "custom-api";
    }

    @Override
    protected String getDefaultModel() {
        return DEFAULT_MODEL;
    }

    @Override
    public CompletableFuture<ChatResponse> chat(List<Message> messages, ChatOptions options) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        validateRequest(messages, options);

                        return executeWithRetry(
                                () -> {
                                    // Create conversation if needed
                                    String conversationId = getOrCreateConversation(messages);

                                    // Build content with function calling if needed
                                    String content = buildContentWithFunctions(messages, options);
                                    String model =
                                            options.getModel() != null ? options.getModel() : getDefaultModel();

                                    // Stream and collect response
                                    StringBuilder responseContent = new StringBuilder();
                                    StringBuilder thinkingContent = new StringBuilder();
                                    Usage[] usage = new Usage[1]; // Array to capture in lambda

                                    ChatEventListener eventListener =
                                            new ChatEventAdapter() {
                                                public void onThinking(String thinking) {
                                                    thinkingContent.append(thinking);
                                                }

                                                public void onToken(String token) {
                                                    responseContent.append(token);
                                                }

                                                public void onComplete(ChatResponse response) {
                                                    usage[0] = response.getUsage();
                                                }
                                            };

                                    streamChat(conversationId, content, model, eventListener);

                                    // Build final response
                                    return ChatResponse.builder()
                                            .id(conversationId)
                                            .provider("custom-api")
                                            .model(model)
                                            .content(responseContent.toString())
                                            .thinkingContent(
                                                    thinkingContent.length() > 0 ? thinkingContent.toString() : null)
                                            .usage(usage[0])
                                            .finishReason("stop")
                                            .timestamp(LocalDateTime.now())
                                            .build();
                                });

                    } catch (Exception e) {
                        log.error("Chat request failed", e);
                        throw new RuntimeException("Chat request failed", e);
                    }
                });
    }

    @Override
    public void chatStream(List<Message> messages, ChatOptions options, ChatEventListener listener) {
        try {
            validateRequest(messages, options);

            // Create conversation if needed
            String conversationId = getOrCreateConversation(messages);

            // Build content with function calling if needed
            String content = buildContentWithFunctions(messages, options);
            String model = options.getModel() != null ? options.getModel() : getDefaultModel();

            // Stream
            streamChat(conversationId, content, model, listener);

        } catch (Exception e) {
            log.error("Stream request failed", e);
            listener.onError(e);
        }
    }

    /**
     * Get or create conversation ID.
     *
     * <p>This caches conversation IDs to reuse them. You can customize this logic based on your
     * needs.
     */
    private String getOrCreateConversation(List<Message> messages) throws ProviderException {
        // Simple cache key based on first system message or generate new
        String cacheKey = "default";
        for (Message msg : messages) {
            if (msg.getRole() == Message.Role.SYSTEM) {
                cacheKey = msg.getContent().substring(0, Math.min(50, msg.getContent().length()));
                break;
            }
        }

        // Check cache
        if (conversationCache.containsKey(cacheKey)) {
            String existingId = conversationCache.get(cacheKey);
            log.debug("Reusing conversation: {}", existingId);
            return existingId;
        }

        // Create new conversation
        try {
            String baseUrl = config.getBaseUrl();
            URL url = new URL(baseUrl + "/api/chat/create");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            if (config.getApiKey() != null) {
                conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            }
            conn.setDoOutput(true);

            // Send empty request or with metadata
            ObjectNode requestBody = objectMapper.createObjectNode();
            // Add any metadata if your API supports it
            // requestBody.put("metadata", "...");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = objectMapper.writeValueAsBytes(requestBody);
                os.write(input, 0, input.length);
            }

            // Read response
            int statusCode = conn.getResponseCode();
            if (statusCode >= 200 && statusCode < 300) {
                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Parse conversation ID from response
                    // Adjust this based on your API response format
                    JsonNode root = objectMapper.readTree(response.toString());
                    String conversationId;

                    if (root.has("id")) {
                        conversationId = root.get("id").asText();
                    } else if (root.has("conversation_id")) {
                        conversationId = root.get("conversation_id").asText();
                    } else if (root.isTextual()) {
                        conversationId = root.asText();
                    } else {
                        conversationId = UUID.randomUUID().toString();
                        log.warn("Could not parse conversation ID, using generated: {}", conversationId);
                    }

                    // Cache it
                    conversationCache.put(cacheKey, conversationId);
                    log.info("Created new conversation: {}", conversationId);

                    return conversationId;
                }
            } else {
                String error = readErrorResponse(conn);
                throw new ProviderException(
                        "custom-api", statusCode, "Failed to create conversation: " + error);
            }

        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException("custom-api", "Failed to create conversation", e);
        }
    }

    /**
     * Build content with function calling injected.
     *
     * <p>Since your API doesn't natively support function calling, we inject function definitions
     * into the content.
     */
    private String buildContentWithFunctions(List<Message> messages, ChatOptions options) {
        StringBuilder content = new StringBuilder();

        // Build conversation context
        for (Message message : messages) {
            String role = message.getRole().name().toLowerCase();
            if (message.getContent() != null) {
                content.append("[").append(role).append("]: ").append(message.getContent()).append("\n\n");
            }
        }

        // Inject function definitions if present
        if (options.getTools() != null && !options.getTools().isEmpty()) {
            content.append("\n--- AVAILABLE FUNCTIONS ---\n");
            content.append("You can call these functions by responding in this format:\n");
            content.append("<function_call>\n");
            content.append("  <name>function_name</name>\n");
            content.append("  <arguments>{\"arg1\": \"value1\"}</arguments>\n");
            content.append("</function_call>\n\n");

            content.append("Available functions:\n");
            for (Tool tool : options.getTools()) {
                FunctionDefinition func = tool.getFunction();
                content
                        .append("- ")
                        .append(func.getName())
                        .append(": ")
                        .append(func.getDescription())
                        .append("\n");

                JsonSchema params = func.getParameters();
                if (params != null && params.getProperties() != null) {
                    content.append("  Parameters:\n");
                    params
                            .getProperties()
                            .forEach(
                                    (name, prop) -> {
                                        content
                                                .append("    - ")
                                                .append(name)
                                                .append(" (")
                                                .append(prop.getType())
                                                .append("): ")
                                                .append(prop.getDescription())
                                                .append("\n");
                                    });
                }
            }
            content.append("\n");
        }

        return content.toString();
    }

    /**
     * Stream chat with SSE.
     */
    private void streamChat(
            String conversationId, String content, String model, ChatEventListener listener)
            throws ProviderException {
        try {
            String baseUrl = config.getBaseUrl();
            URL url = new URL(baseUrl + "/api/chat/stream");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text/event-stream");
            if (config.getApiKey() != null) {
                conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            }
            conn.setDoOutput(true);

            // Build request
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("conversation_id", conversationId);
            requestBody.put("content", content);
            requestBody.put("model", model);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = objectMapper.writeValueAsBytes(requestBody);
                os.write(input, 0, input.length);
            }

            // Read SSE stream
            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                StringBuilder contentBuilder = new StringBuilder();
                StringBuilder thinkingBuilder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);

                        // Skip empty or [DONE] markers
                        if (data.trim().isEmpty() || data.equals("[DONE]")) {
                            continue;
                        }

                        try {
                            JsonNode chunk = objectMapper.readTree(data);

                            // Determine type of chunk
                            String type = chunk.has("type") ? chunk.get("type").asText() : "token";

                            if (type.equals("thinking")) {
                                // Thinking content
                                String thinking = chunk.get("content").asText();
                                thinkingBuilder.append(thinking);
                                listener.onThinking(thinking);

                            } else if (type.equals("token") || type.equals("content")) {
                                // Regular content token
                                String token = chunk.get("content").asText();
                                contentBuilder.append(token);
                                listener.onToken(token);

                            } else if (type.equals("done") || type.equals("complete")) {
                                // Stream complete with usage
                                Usage usage = null;
                                if (chunk.has("usage")) {
                                    JsonNode usageNode = chunk.get("usage");
                                    usage =
                                            Usage.builder()
                                                    .promptTokens(
                                                            usageNode.has("prompt_tokens")
                                                                    ? usageNode.get("prompt_tokens").asInt()
                                                                    : 0)
                                                    .completionTokens(
                                                            usageNode.has("completion_tokens")
                                                                    ? usageNode.get("completion_tokens").asInt()
                                                                    : 0)
                                                    .build();
                                }

                                ChatResponse response =
                                        ChatResponse.builder()
                                                .id(conversationId)
                                                .provider("custom-api")
                                                .model(model)
                                                .content(contentBuilder.toString())
                                                .thinkingContent(
                                                        thinkingBuilder.length() > 0 ? thinkingBuilder.toString() : null)
                                                .usage(usage)
                                                .finishReason("stop")
                                                .timestamp(LocalDateTime.now())
                                                .build();

                                listener.onComplete(response);
                                break;
                            }

                        } catch (Exception e) {
                            log.warn("Failed to parse SSE chunk: {}", data, e);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Streaming failed", e);
            throw new ProviderException("custom-api", "Streaming failed", e);
        }
    }

    /**
     * Get remaining tokens for a conversation.
     *
     * @param conversationId Conversation ID
     * @return Remaining tokens
     */
    public int getRemainingTokens(String conversationId) throws ProviderException {
        try {
            String baseUrl = config.getBaseUrl();
            URL url = new URL(baseUrl + "/api/chat/tokens/" + conversationId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            if (config.getApiKey() != null) {
                conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            }

            int statusCode = conn.getResponseCode();
            if (statusCode >= 200 && statusCode < 300) {
                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Parse response
                    JsonNode root = objectMapper.readTree(response.toString());

                    if (root.has("remaining_tokens")) {
                        return root.get("remaining_tokens").asInt();
                    } else if (root.has("tokens")) {
                        return root.get("tokens").asInt();
                    } else if (root.isNumber()) {
                        return root.asInt();
                    } else {
                        log.warn("Could not parse remaining tokens from: {}", response);
                        return 0;
                    }
                }
            } else {
                String error = readErrorResponse(conn);
                throw new ProviderException(
                        "custom-api", statusCode, "Failed to get remaining tokens: " + error);
            }

        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException("custom-api", "Failed to get remaining tokens", e);
        }
    }

    /**
     * Clear conversation cache for a specific key or all.
     */
    public void clearConversationCache(String cacheKey) {
        if (cacheKey != null) {
            conversationCache.remove(cacheKey);
        } else {
            conversationCache.clear();
        }
    }

    @Override
    public ProviderCapabilities getCapabilities() {
        return ProviderCapabilities.builder()
                .providerName("custom-api")
                .supportsStreaming(true)
                .supportsFunctionCalling(true) // Via content injection
                .supportsThinking(true) // Built-in!
                .supportsVision(false)
                .supportsJsonMode(false)
                .maxTokens(4096)
                .maxContextWindow(8192)
                .build();
    }

    @Override
    public List<ModelInfo> getModels() {
        // Return models supported by your API
        // You can make this dynamic by calling an API endpoint if available
        List<ModelInfo> models = new ArrayList<>();

        models.add(
                ModelInfo.builder()
                        .id("default")
                        .name("Default Model")
                        .description("Your custom LLM model")
                        .provider("custom-api")
                        .contextWindow(8192)
                        .maxOutputTokens(4096)
                        .supportsTools(true)
                        .costPer1kInputTokens(0.0) // Adjust as needed
                        .costPer1kOutputTokens(0.0)
                        .build());

        return models;
    }

    @Override
    public ModelInfo getModelInfo(String modelId) {
        return getModels().stream().filter(m -> m.getId().equals(modelId)).findFirst().orElse(null);
    }

    private String readErrorResponse(HttpURLConnection conn) {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            return "Failed to read error response";
        }
    }
}
