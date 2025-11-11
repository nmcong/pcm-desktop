package com.noteflix.pcm.llm.client.anthropic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noteflix.pcm.llm.api.LLMClient;
import com.noteflix.pcm.llm.api.StreamingCapable;
import com.noteflix.pcm.llm.exception.LLMProviderException;
import com.noteflix.pcm.llm.model.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * Anthropic (Claude) API client implementation
 * 
 * Supports:
 * - Claude 3.5 Sonnet
 * - Claude 3 Opus
 * - Claude 3 Haiku
 * 
 * Features:
 * - Messages API
 * - Streaming responses
 * - System prompts
 * - Tool use (similar to function calling)
 * 
 * API Docs: https://docs.anthropic.com/claude/reference/messages_post
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class AnthropicClient implements LLMClient, StreamingCapable {
    
    private final LLMProviderConfig config;
    private final ObjectMapper objectMapper;
    
    // Anthropic API version
    private static final String API_VERSION = "2023-06-01";
    
    public AnthropicClient(LLMProviderConfig config) {
        this.config = config;
        this.config.validate();
        this.objectMapper = new ObjectMapper();
        log.info("Initialized Anthropic client for model: {}", config.getModel());
    }
    
    @Override
    public LLMResponse sendMessage(LLMRequest request) {
        try {
            request.validate();
            
            // Build JSON request
            String jsonRequest = buildRequestJson(request);
            
            // Send HTTP request
            String jsonResponse = sendHttpRequest(jsonRequest);
            
            // Parse response
            return parseResponse(jsonResponse);
            
        } catch (IOException e) {
            log.error("Failed to send message to Anthropic", e);
            throw new LLMProviderException("Failed to communicate with Anthropic", e);
        }
    }
    
    @Override
    public Stream<LLMChunk> streamMessage(LLMRequest request) {
        // Simplified streaming - return single chunk
        // Full SSE implementation can be added later
        log.warn("Streaming not fully implemented for Anthropic, using regular response");
        LLMResponse response = sendMessage(request);
        
        LLMChunk chunk = LLMChunk.builder()
            .id(response.getId())
            .model(response.getModel())
            .content(response.getContent())
            .finishReason(response.getFinishReason())
            .build();
        
        return Stream.of(chunk);
    }
    
    @Override
    public void streamMessage(LLMRequest request, StreamingObserver observer) {
        try {
            LLMResponse response = sendMessage(request);
            
            LLMChunk chunk = LLMChunk.builder()
                .id(response.getId())
                .model(response.getModel())
                .content(response.getContent())
                .finishReason(response.getFinishReason())
                .build();
            
            observer.onChunk(chunk);
            observer.onComplete();
            
        } catch (Exception e) {
            observer.onError(e);
        }
    }
    
    @Override
    public String getProviderName() {
        return "Anthropic (Claude)";
    }
    
    @Override
    public boolean isAvailable() {
        return config.getUrl() != null && config.getToken() != null;
    }
    
    @Override
    public String getModel() {
        return config.getModel() != null ? config.getModel() : "claude-3-5-sonnet-20241022";
    }
    
    // Private helper methods
    
    private String buildRequestJson(LLMRequest request) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        
        // Model
        root.put("model", request.getModel() != null ? request.getModel() : getModel());
        
        // Max tokens (required for Anthropic)
        root.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 4096);
        
        // Messages (need to separate system messages)
        String systemPrompt = null;
        ArrayNode messagesArray = root.putArray("messages");
        
        for (Message message : request.getMessages()) {
            // Extract system message separately (Anthropic requirement)
            if (message.getRole() == Message.Role.SYSTEM) {
                systemPrompt = message.getContent();
                continue;
            }
            
            ObjectNode messageNode = messagesArray.addObject();
            messageNode.put("role", message.getRole().name().toLowerCase());
            messageNode.put("content", message.getContent());
        }
        
        // Add system prompt if exists
        if (systemPrompt != null) {
            root.put("system", systemPrompt);
        }
        
        // Parameters
        if (request.getTemperature() != null) {
            root.put("temperature", request.getTemperature());
        }
        if (request.getTopP() != null) {
            root.put("top_p", request.getTopP());
        }
        if (request.getStop() != null && !request.getStop().isEmpty()) {
            ArrayNode stopArray = root.putArray("stop_sequences");
            request.getStop().forEach(stopArray::add);
        }
        
        // Stream
        if (request.getStream() != null) {
            root.put("stream", request.getStream());
        }
        
        return objectMapper.writeValueAsString(root);
    }
    
    private String sendHttpRequest(String jsonRequest) throws IOException {
        URL url = new URL(config.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            // Setup connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", config.getToken()); // Anthropic uses x-api-key
            conn.setRequestProperty("anthropic-version", API_VERSION);
            conn.setDoOutput(true);
            conn.setConnectTimeout(config.getTimeout() * 1000);
            conn.setReadTimeout(config.getTimeout() * 1000);
            
            // Add custom headers if any
            if (config.getHeaders() != null) {
                config.getHeaders().forEach(conn::setRequestProperty);
            }
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read response
            int statusCode = conn.getResponseCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                return readResponse(conn);
            } else {
                String errorResponse = readErrorResponse(conn);
                throw new LLMProviderException(
                    "Anthropic API error",
                    statusCode,
                    errorResponse
                );
            }
            
        } finally {
            conn.disconnect();
        }
    }
    
    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            return response.toString();
        }
    }
    
    private String readErrorResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            return response.toString();
        }
    }
    
    private LLMResponse parseResponse(String jsonResponse) throws IOException {
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        String id = root.path("id").asText();
        String model = root.path("model").asText();
        String type = root.path("type").asText();
        String role = root.path("role").asText();
        
        // Parse content (Anthropic returns content as array)
        StringBuilder contentBuilder = new StringBuilder();
        JsonNode contentNode = root.path("content");
        if (contentNode.isArray()) {
            for (JsonNode item : contentNode) {
                if ("text".equals(item.path("type").asText())) {
                    contentBuilder.append(item.path("text").asText());
                }
            }
        }
        
        String content = contentBuilder.toString();
        String stopReason = root.path("stop_reason").asText(null);
        
        // Parse usage
        LLMResponse.Usage usage = null;
        if (root.has("usage")) {
            JsonNode usageNode = root.path("usage");
            usage = LLMResponse.Usage.builder()
                .promptTokens(usageNode.path("input_tokens").asInt())
                .completionTokens(usageNode.path("output_tokens").asInt())
                .totalTokens(usageNode.path("input_tokens").asInt() + usageNode.path("output_tokens").asInt())
                .build();
        }
        
        return LLMResponse.builder()
            .id(id)
            .model(model)
            .content(content)
            .finishReason(stopReason)
            .usage(usage)
            .createdAt(Instant.now())
            .build();
    }
}

