package com.noteflix.pcm.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Anthropic (Claude) Provider implementation.
 * 
 * Supports:
 * - Claude 3.5 Sonnet, Claude 3 Opus, Claude 3 Sonnet, Claude 3 Haiku
 * - Streaming with Server-Sent Events
 * - Function/tool calling (beta)
 * - Vision capabilities
 * - Long context (200K tokens)
 * 
 * API Documentation: https://docs.anthropic.com/claude/reference
 */
@Slf4j
public class AnthropicProvider extends BaseProvider {
    
    private static final String DEFAULT_BASE_URL = "https://api.anthropic.com/v1";
    private static final String API_VERSION = "2023-06-01";
    private static final String DEFAULT_MODEL = "claude-3-5-sonnet-20241022";
    
    private final ObjectMapper objectMapper;
    
    public AnthropicProvider() {
        super();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public String getName() {
        return "anthropic";
    }
    
    @Override
    protected String getDefaultModel() {
        return DEFAULT_MODEL;
    }
    
    @Override
    public CompletableFuture<ChatResponse> chat(List<Message> messages, ChatOptions options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                validateRequest(messages, options);
                
                return executeWithRetry(() -> {
                    String requestBody = buildRequestBody(messages, options, false);
                    String responseBody = sendHttpRequest(requestBody);
                    return parseResponse(responseBody);
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
            
            String requestBody = buildRequestBody(messages, options, true);
            streamHttpRequest(requestBody, listener);
            
        } catch (Exception e) {
            log.error("Stream request failed", e);
            listener.onError(e);
        }
    }
    
    @Override
    public ProviderCapabilities getCapabilities() {
        return ProviderCapabilities.builder()
            .providerName("anthropic")
            .supportsStreaming(true)
            .supportsFunctionCalling(true)
            .supportsThinking(false)
            .supportsVision(true)
            .supportsJsonMode(false)
            .maxTokens(4096)
            .maxContextWindow(200000) // Claude 3.5 Sonnet
            .build();
    }
    
    @Override
    public List<ModelInfo> getModels() {
        List<ModelInfo> models = new ArrayList<>();
        
        // Claude 3.5 Sonnet (Latest)
        models.add(ModelInfo.builder()
            .id("claude-3-5-sonnet-20241022")
            .name("Claude 3.5 Sonnet")
            .description("Most intelligent model, best for complex tasks")
            .provider("anthropic")
            .contextWindow(200000)
            .maxOutputTokens(4096)
            .supportsTools(true)
            .supportsVision(true)
            .costPer1kInputTokens(0.003)
            .costPer1kOutputTokens(0.015)
            .build());
        
        // Claude 3 Opus
        models.add(ModelInfo.builder()
            .id("claude-3-opus-20240229")
            .name("Claude 3 Opus")
            .description("Powerful model for complex analysis")
            .provider("anthropic")
            .contextWindow(200000)
            .maxOutputTokens(4096)
            .supportsTools(true)
            .supportsVision(true)
            .costPer1kInputTokens(0.015)
            .costPer1kOutputTokens(0.075)
            .build());
        
        // Claude 3 Sonnet
        models.add(ModelInfo.builder()
            .id("claude-3-sonnet-20240229")
            .name("Claude 3 Sonnet")
            .description("Balanced performance and speed")
            .provider("anthropic")
            .contextWindow(200000)
            .maxOutputTokens(4096)
            .supportsTools(true)
            .supportsVision(true)
            .costPer1kInputTokens(0.003)
            .costPer1kOutputTokens(0.015)
            .build());
        
        // Claude 3 Haiku
        models.add(ModelInfo.builder()
            .id("claude-3-haiku-20240307")
            .name("Claude 3 Haiku")
            .description("Fast and efficient for simple tasks")
            .provider("anthropic")
            .contextWindow(200000)
            .maxOutputTokens(4096)
            .supportsTools(true)
            .supportsVision(true)
            .costPer1kInputTokens(0.00025)
            .costPer1kOutputTokens(0.00125)
            .build());
        
        return models;
    }
    
    @Override
    public ModelInfo getModelInfo(String modelId) {
        return getModels().stream()
            .filter(m -> m.getId().equals(modelId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Build JSON request body for Anthropic API.
     */
    private String buildRequestBody(List<Message> messages, ChatOptions options, boolean stream) 
            throws ProviderException {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            
            // Model
            String model = options.getModel() != null ? options.getModel() : getDefaultModel();
            root.put("model", model);
            
            // Max tokens (required for Anthropic)
            int maxTokens = options.getMaxTokens() > 0 ? options.getMaxTokens() : 4096;
            root.put("max_tokens", maxTokens);
            
            // Messages - separate system messages
            String systemPrompt = null;
            ArrayNode messagesArray = root.putArray("messages");
            
            for (Message message : messages) {
                // Extract system message separately (Anthropic requirement)
                if (message.getRole() == Message.Role.SYSTEM) {
                    systemPrompt = message.getContent();
                    continue;
                }
                
                ObjectNode messageNode = messagesArray.addObject();
                
                // Map roles (Anthropic uses "user" and "assistant")
                String role = message.getRole() == Message.Role.USER ? "user" : "assistant";
                messageNode.put("role", role);
                
                if (message.getContent() != null) {
                    messageNode.put("content", message.getContent());
                }
            }
            
            // Add system prompt if exists
            if (systemPrompt != null) {
                root.put("system", systemPrompt);
            }
            
            // Parameters
            if (options.getTemperature() != 0.0) {
                root.put("temperature", options.getTemperature());
            }
            if (options.getTopP() != null) {
                root.put("top_p", options.getTopP());
            }
            if (options.getStop() != null && !options.getStop().isEmpty()) {
                ArrayNode stopArray = root.putArray("stop_sequences");
                options.getStop().forEach(stopArray::add);
            }
            
            // Tools (Anthropic format)
            if (options.getTools() != null && !options.getTools().isEmpty()) {
                ArrayNode toolsArray = root.putArray("tools");
                for (Tool tool : options.getTools()) {
                    ObjectNode toolNode = toolsArray.addObject();
                    toolNode.put("name", tool.getFunction().getName());
                    toolNode.put("description", tool.getFunction().getDescription());
                    
                    // Input schema
                    JsonSchema schema = tool.getFunction().getParameters();
                    if (schema != null) {
                        ObjectNode inputSchema = toolNode.putObject("input_schema");
                        inputSchema.put("type", "object");
                        
                        if (schema.getProperties() != null) {
                            ObjectNode propertiesNode = inputSchema.putObject("properties");
                            schema.getProperties().forEach((name, prop) -> {
                                ObjectNode propNode = propertiesNode.putObject(name);
                                propNode.put("type", prop.getType());
                                if (prop.getDescription() != null) {
                                    propNode.put("description", prop.getDescription());
                                }
                            });
                        }
                        
                        if (schema.getRequired() != null && !schema.getRequired().isEmpty()) {
                            ArrayNode requiredArray = inputSchema.putArray("required");
                            schema.getRequired().forEach(requiredArray::add);
                        }
                    }
                }
            }
            
            // Stream
            root.put("stream", stream);
            
            return objectMapper.writeValueAsString(root);
            
        } catch (Exception e) {
            throw new ProviderException("anthropic", "Failed to build request body", e);
        }
    }
    
    /**
     * Send HTTP request to Anthropic API.
     */
    private String sendHttpRequest(String requestBody) throws ProviderException {
        try {
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
            URL url = new URL(baseUrl + "/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // Setup connection (Anthropic uses different headers)
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", config.getApiKey()); // Note: x-api-key, not Authorization
            conn.setRequestProperty("anthropic-version", API_VERSION);
            conn.setDoOutput(true);
            conn.setConnectTimeout((int) config.getTimeoutMs());
            conn.setReadTimeout((int) config.getTimeoutMs());
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read response
            int statusCode = conn.getResponseCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                return readResponse(conn);
            } else {
                String error = readErrorResponse(conn);
                throw new ProviderException("anthropic", statusCode, "Anthropic API error: " + error);
            }
            
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException("anthropic", "Failed to communicate with Anthropic", e);
        }
    }
    
    /**
     * Stream HTTP request with SSE.
     */
    private void streamHttpRequest(String requestBody, ChatEventListener listener) {
        try {
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
            URL url = new URL(baseUrl + "/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // Setup connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", config.getApiKey());
            conn.setRequestProperty("anthropic-version", API_VERSION);
            conn.setDoOutput(true);
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read streaming response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                StringBuilder contentBuilder = new StringBuilder();
                
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        
                        try {
                            JsonNode chunk = objectMapper.readTree(data);
                            String type = chunk.get("type").asText();
                            
                            if (type.equals("content_block_delta")) {
                                JsonNode delta = chunk.get("delta");
                                if (delta != null && delta.has("text")) {
                                    String text = delta.get("text").asText();
                                    contentBuilder.append(text);
                                    listener.onToken(text);
                                }
                            } else if (type.equals("message_stop")) {
                                // Stream complete
                                ChatResponse response = ChatResponse.builder()
                                    .id(UUID.randomUUID().toString())
                                    .provider("anthropic")
                                    .content(contentBuilder.toString())
                                    .finishReason("stop")
                                    .build();
                                listener.onComplete(response);
                                break;
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse streaming chunk: {}", data, e);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Streaming failed", e);
            listener.onError(e);
        }
    }
    
    /**
     * Parse response from Anthropic API.
     */
    private ChatResponse parseResponse(String responseBody) throws ProviderException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            String id = root.get("id").asText();
            String model = root.get("model").asText();
            
            // Content
            String content = null;
            JsonNode contentArray = root.get("content");
            if (contentArray != null && contentArray.size() > 0) {
                JsonNode firstContent = contentArray.get(0);
                if (firstContent.has("text")) {
                    content = firstContent.get("text").asText();
                }
            }
            
            String stopReason = root.get("stop_reason").asText();
            
            // Tool calls (if any)
            List<ToolCall> toolCalls = null;
            // TODO: Parse tool calls from Anthropic format
            
            // Usage
            Usage usage = null;
            if (root.has("usage")) {
                JsonNode usageNode = root.get("usage");
                usage = Usage.builder()
                    .promptTokens(usageNode.get("input_tokens").asInt())
                    .completionTokens(usageNode.get("output_tokens").asInt())
                    .build();
            }
            
            return ChatResponse.builder()
                .id(id)
                .provider("anthropic")
                .model(model)
                .content(content)
                .toolCalls(toolCalls)
                .usage(usage)
                .finishReason(stopReason)
                .timestamp(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            throw new ProviderException("anthropic", "Failed to parse response", e);
        }
    }
    
    private String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    
    private String readErrorResponse(HttpURLConnection conn) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
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

