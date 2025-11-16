package com.noteflix.pcm.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noteflix.pcm.llm.api.ChatEventListener;
import com.noteflix.pcm.llm.exception.ProviderException;
import com.noteflix.pcm.llm.model.*;

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

import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI Provider implementation.
 *
 * <p>Supports: - GPT-4, GPT-4 Turbo, GPT-3.5 Turbo - Streaming with Server-Sent Events -
 * Function/tool calling - Vision (GPT-4V) - JSON mode
 *
 * <p>API Documentation: https://platform.openai.com/docs/api-reference
 */
@Slf4j
public class OpenAIProvider extends BaseProvider {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final String DEFAULT_MODEL = "gpt-4-turbo-preview";

    private final ObjectMapper objectMapper;

    public OpenAIProvider() {
        super();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getName() {
        return "openai";
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
                .providerName("openai")
                .supportsStreaming(true)
                .supportsFunctionCalling(true)
                .supportsThinking(false) // o1 models support this
                .supportsVision(true)
                .supportsJsonMode(true)
                .maxTokens(4096)
                .maxContextWindow(128000) // GPT-4 Turbo
                .build();
    }

    @Override
    public List<ModelInfo> getModels() {
        List<ModelInfo> models = new ArrayList<>();

        // GPT-4 Turbo
        models.add(
                ModelInfo.builder()
                        .id("gpt-4-turbo-preview")
                        .name("GPT-4 Turbo")
                        .description("Most capable model, great for complex tasks")
                        .provider("openai")
                        .contextWindow(128000)
                        .maxOutputTokens(4096)
                        .supportsTools(true)
                        .supportsVision(true)
                        .costPer1kInputTokens(0.01)
                        .costPer1kOutputTokens(0.03)
                        .build());

        // GPT-4
        models.add(
                ModelInfo.builder()
                        .id("gpt-4")
                        .name("GPT-4")
                        .description("High intelligence model")
                        .provider("openai")
                        .contextWindow(8192)
                        .maxOutputTokens(4096)
                        .supportsTools(true)
                        .costPer1kInputTokens(0.03)
                        .costPer1kOutputTokens(0.06)
                        .build());

        // GPT-3.5 Turbo
        models.add(
                ModelInfo.builder()
                        .id("gpt-3.5-turbo")
                        .name("GPT-3.5 Turbo")
                        .description("Fast and efficient for most tasks")
                        .provider("openai")
                        .contextWindow(16385)
                        .maxOutputTokens(4096)
                        .supportsTools(true)
                        .costPer1kInputTokens(0.0005)
                        .costPer1kOutputTokens(0.0015)
                        .build());

        return models;
    }

    @Override
    public ModelInfo getModelInfo(String modelId) {
        return getModels().stream().filter(m -> m.getId().equals(modelId)).findFirst().orElse(null);
    }

    /**
     * Build JSON request body for OpenAI API.
     */
    private String buildRequestBody(List<Message> messages, ChatOptions options, boolean stream)
            throws ProviderException {
        try {
            ObjectNode root = objectMapper.createObjectNode();

            // Model
            String model = options.getModel() != null ? options.getModel() : getDefaultModel();
            root.put("model", model);

            // Messages
            ArrayNode messagesArray = root.putArray("messages");
            for (Message message : messages) {
                ObjectNode messageNode = messagesArray.addObject();
                messageNode.put("role", message.getRole().name().toLowerCase());

                if (message.getContent() != null) {
                    messageNode.put("content", message.getContent());
                }

                // Tool calls in assistant messages
                if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
                    ArrayNode toolCallsArray = messageNode.putArray("tool_calls");
                    for (ToolCall toolCall : message.getToolCalls()) {
                        ObjectNode toolCallNode = toolCallsArray.addObject();
                        toolCallNode.put("id", toolCall.getId());
                        toolCallNode.put("type", "function");

                        ObjectNode functionNode = toolCallNode.putObject("function");
                        functionNode.put("name", toolCall.getName());
                        functionNode.put("arguments", objectMapper.writeValueAsString(toolCall.getArguments()));
                    }
                }

                // Tool results
                if (message.getRole() == Message.Role.TOOL && message.getToolCallId() != null) {
                    messageNode.put("tool_call_id", message.getToolCallId());
                }
            }

            // Parameters
            if (options.getTemperature() != 0.0) {
                root.put("temperature", options.getTemperature());
            }
            if (options.getMaxTokens() > 0) {
                root.put("max_tokens", options.getMaxTokens());
            }
            if (options.getTopP() != null) {
                root.put("top_p", options.getTopP());
            }
            if (options.getFrequencyPenalty() != null) {
                root.put("frequency_penalty", options.getFrequencyPenalty());
            }
            if (options.getPresencePenalty() != null) {
                root.put("presence_penalty", options.getPresencePenalty());
            }
            if (options.getStop() != null && !options.getStop().isEmpty()) {
                ArrayNode stopArray = root.putArray("stop");
                options.getStop().forEach(stopArray::add);
            }

            // Tools
            if (options.getTools() != null && !options.getTools().isEmpty()) {
                ArrayNode toolsArray = root.putArray("tools");
                for (Tool tool : options.getTools()) {
                    ObjectNode toolNode = toolsArray.addObject();
                    toolNode.put("type", "function");

                    ObjectNode functionNode = toolNode.putObject("function");
                    functionNode.put("name", tool.getFunction().getName());
                    functionNode.put("description", tool.getFunction().getDescription());

                    // Parameters schema
                    JsonSchema schema = tool.getFunction().getParameters();
                    if (schema != null) {
                        ObjectNode parametersNode = functionNode.putObject("parameters");
                        parametersNode.put("type", "object");

                        if (schema.getProperties() != null) {
                            ObjectNode propertiesNode = parametersNode.putObject("properties");
                            schema
                                    .getProperties()
                                    .forEach(
                                            (name, prop) -> {
                                                ObjectNode propNode = propertiesNode.putObject(name);
                                                propNode.put("type", prop.getType());
                                                if (prop.getDescription() != null) {
                                                    propNode.put("description", prop.getDescription());
                                                }
                                            });
                        }

                        if (schema.getRequired() != null && !schema.getRequired().isEmpty()) {
                            ArrayNode requiredArray = parametersNode.putArray("required");
                            schema.getRequired().forEach(requiredArray::add);
                        }
                    }
                }

                // Tool choice
                if (options.getToolChoice() != null && !options.getToolChoice().equals("auto")) {
                    root.put("tool_choice", options.getToolChoice());
                }
            }

            // Stream
            root.put("stream", stream);

            return objectMapper.writeValueAsString(root);

        } catch (Exception e) {
            throw new ProviderException("openai", "Failed to build request body", e);
        }
    }

    /**
     * Send HTTP request to OpenAI API.
     */
    private String sendHttpRequest(String requestBody) throws ProviderException {
        try {
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
            URL url = new URL(baseUrl + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Setup connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            if (config.getOrganizationId() != null) {
                conn.setRequestProperty("OpenAI-Organization", config.getOrganizationId());
            }
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
                throw new ProviderException("openai", statusCode, "OpenAI API error: " + error);
            }

        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException("openai", "Failed to communicate with OpenAI", e);
        }
    }

    /**
     * Stream HTTP request with SSE.
     */
    private void streamHttpRequest(String requestBody, ChatEventListener listener) {
        try {
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
            URL url = new URL(baseUrl + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Setup connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            if (config.getOrganizationId() != null) {
                conn.setRequestProperty("OpenAI-Organization", config.getOrganizationId());
            }
            conn.setDoOutput(true);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read streaming response
            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                StringBuilder contentBuilder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);

                        if (data.equals("[DONE]")) {
                            // Stream complete
                            ChatResponse response =
                                    ChatResponse.builder()
                                            .id(UUID.randomUUID().toString())
                                            .provider("openai")
                                            .content(contentBuilder.toString())
                                            .finishReason("stop")
                                            .build();
                            listener.onComplete(response);
                            break;
                        }

                        try {
                            JsonNode chunk = objectMapper.readTree(data);
                            JsonNode choices = chunk.get("choices");

                            if (choices != null && choices.size() > 0) {
                                JsonNode delta = choices.get(0).get("delta");
                                if (delta != null && delta.has("content")) {
                                    String content = delta.get("content").asText();
                                    contentBuilder.append(content);
                                    listener.onToken(content);
                                }

                                // Check for tool calls
                                if (delta != null && delta.has("tool_calls")) {
                                    // Handle tool calls in streaming
                                    log.debug("Tool calls in stream (not yet fully implemented)");
                                }
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
     * Parse response from OpenAI API.
     */
    private ChatResponse parseResponse(String responseBody) throws ProviderException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String id = root.get("id").asText();
            String model = root.get("model").asText();

            JsonNode choices = root.get("choices");
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");

            String content =
                    message.has("content") && !message.get("content").isNull()
                            ? message.get("content").asText()
                            : null;

            String finishReason = firstChoice.get("finish_reason").asText();

            // Parse tool calls
            List<ToolCall> toolCalls = null;
            if (message.has("tool_calls")) {
                toolCalls = new ArrayList<>();
                JsonNode toolCallsArray = message.get("tool_calls");

                for (JsonNode toolCallNode : toolCallsArray) {
                    String toolCallId = toolCallNode.get("id").asText();
                    JsonNode function = toolCallNode.get("function");
                    String functionName = function.get("name").asText();
                    String argumentsJson = function.get("arguments").asText();

                    ToolCall toolCall =
                            ToolCall.builder()
                                    .id(toolCallId)
                                    .type("function")
                                    .function(
                                            com.noteflix.pcm.llm.model.FunctionCall.builder()
                                                    .name(functionName)
                                                    .arguments(argumentsJson)
                                                    .build())
                                    .build();

                    toolCalls.add(toolCall);
                }
            }

            // Parse usage
            Usage usage = null;
            if (root.has("usage")) {
                JsonNode usageNode = root.get("usage");
                usage =
                        Usage.builder()
                                .promptTokens(usageNode.get("prompt_tokens").asInt())
                                .completionTokens(usageNode.get("completion_tokens").asInt())
                                .totalTokens(usageNode.get("total_tokens").asInt())
                                .build();
            }

            return ChatResponse.builder()
                    .id(id)
                    .provider("openai")
                    .model(model)
                    .content(content)
                    .toolCalls(toolCalls)
                    .usage(usage)
                    .finishReason(finishReason)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            throw new ProviderException("openai", "Failed to parse response", e);
        }
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
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
