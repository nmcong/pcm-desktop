package com.noteflix.pcm.llm.client.ollama;

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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ollama API client implementation for local models
 * <p>
 * Supports local models like:
 * - Llama 2/3
 * - Mistral
 * - Phi-2/3
 * - CodeLlama
 * - And any other models installed locally
 * <p>
 * Features:
 * - Local inference (no API key needed)
 * - Chat completion
 * - Streaming responses
 * - Model management
 * <p>
 * API Docs: https://github.com/ollama/ollama/blob/main/docs/api.md
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class OllamaClient implements LLMClient, StreamingCapable {

    private final LLMProviderConfig config;
    private final ObjectMapper objectMapper;

    public OllamaClient(LLMProviderConfig config) {
        this.config = config;
        this.config.validate();
        this.objectMapper = new ObjectMapper();
        log.info("Initialized Ollama client for model: {}", config.getModel());
    }

    @Override
    public LLMResponse sendMessage(LLMRequest request) {
        try {
            request.validate();

            // Build JSON request
            String jsonRequest = buildRequestJson(request, false);

            // Send HTTP request
            String jsonResponse = sendHttpRequest(jsonRequest);

            // Parse response
            return parseResponse(jsonResponse);

        } catch (IOException e) {
            log.error("Failed to send message to Ollama", e);
            throw new LLMProviderException("Failed to communicate with Ollama", e);
        }
    }

    @Override
    public Stream<LLMChunk> streamMessage(LLMRequest request) {
        try {
            request.validate();

            // Build JSON request with streaming enabled
            String jsonRequest = buildRequestJson(request, true);

            // Send HTTP request and get streaming response
            List<LLMChunk> chunks = sendStreamingHttpRequest(jsonRequest);

            return chunks.stream();

        } catch (IOException e) {
            log.error("Failed to stream message from Ollama", e);
            throw new LLMProviderException("Failed to stream from Ollama", e);
        }
    }

    @Override
    public void streamMessage(LLMRequest request, StreamingObserver observer) {
        try {
            request.validate();

            // Build JSON request with streaming enabled
            String jsonRequest = buildRequestJson(request, true);

            // Send HTTP request with streaming callback
            sendStreamingHttpRequestWithCallback(jsonRequest, observer);

        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public String getProviderName() {
        return "Ollama (Local)";
    }

    @Override
    public boolean isAvailable() {
        // Ollama doesn't require token, just check URL
        return config.getUrl() != null;
    }

    @Override
    public String getModel() {
        return config.getModel() != null ? config.getModel() : "llama2";
    }

    // Private helper methods

    private String buildRequestJson(LLMRequest request, boolean stream) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();

        // Model
        root.put("model", request.getModel() != null ? request.getModel() : getModel());

        // Messages
        ArrayNode messagesArray = root.putArray("messages");
        for (Message message : request.getMessages()) {
            ObjectNode messageNode = messagesArray.addObject();
            messageNode.put("role", message.getRole().name().toLowerCase());
            messageNode.put("content", message.getContent());
        }

        // Options (Ollama-specific format)
        ObjectNode optionsNode = root.putObject("options");
        if (request.getTemperature() != null) {
            optionsNode.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            optionsNode.put("num_predict", request.getMaxTokens());
        }
        if (request.getTopP() != null) {
            optionsNode.put("top_p", request.getTopP());
        }
        if (request.getStop() != null && !request.getStop().isEmpty()) {
            ArrayNode stopArray = optionsNode.putArray("stop");
            request.getStop().forEach(stopArray::add);
        }

        // Stream
        root.put("stream", stream);

        return objectMapper.writeValueAsString(root);
    }

    private String sendHttpRequest(String jsonRequest) throws IOException {
        URL url = new URL(config.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            // Setup connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(config.getTimeout() * 1000);
            conn.setReadTimeout(config.getTimeout() * 1000);

            // Ollama doesn't need authentication for local usage
            // But allow custom headers if needed
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
                        "Ollama API error",
                        statusCode,
                        errorResponse
                );
            }

        } finally {
            conn.disconnect();
        }
    }

    private List<LLMChunk> sendStreamingHttpRequest(String jsonRequest) throws IOException {
        URL url = new URL(config.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        List<LLMChunk> chunks = new ArrayList<>();

        try {
            // Setup connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(config.getTimeout() * 1000);
            conn.setReadTimeout(config.getTimeout() * 1000);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read streaming response (Ollama sends JSON lines, not SSE)
            int statusCode = conn.getResponseCode();

            if (statusCode >= 200 && statusCode < 300) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            LLMChunk chunk = parseStreamChunk(line);
                            if (chunk != null) {
                                chunks.add(chunk);
                            }
                        }
                    }
                }
            } else {
                String errorResponse = readErrorResponse(conn);
                throw new LLMProviderException("Ollama API error", statusCode, errorResponse);
            }

        } finally {
            conn.disconnect();
        }

        return chunks;
    }

    private void sendStreamingHttpRequestWithCallback(String jsonRequest, StreamingObserver observer)
            throws IOException {
        URL url = new URL(config.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            // Setup connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(config.getTimeout() * 1000);
            conn.setReadTimeout(config.getTimeout() * 1000);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read streaming response
            int statusCode = conn.getResponseCode();

            if (statusCode >= 200 && statusCode < 300) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            try {
                                LLMChunk chunk = parseStreamChunk(line);
                                if (chunk != null) {
                                    observer.onChunk(chunk);
                                }
                            } catch (Exception e) {
                                log.warn("Failed to parse chunk, continuing...", e);
                            }
                        }
                    }

                    observer.onComplete();
                }
            } else {
                String errorResponse = readErrorResponse(conn);
                observer.onError(new LLMProviderException("Ollama API error", statusCode, errorResponse));
            }

        } catch (Exception e) {
            observer.onError(e);
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

        String model = root.path("model").asText();
        String content = root.path("message").path("content").asText();
        boolean done = root.path("done").asBoolean();

        // Parse timing info (Ollama-specific)
        long promptEvalCount = root.path("prompt_eval_count").asLong(0);
        long evalCount = root.path("eval_count").asLong(0);

        LLMResponse.Usage usage = LLMResponse.Usage.builder()
                .promptTokens((int) promptEvalCount)
                .completionTokens((int) evalCount)
                .totalTokens((int) (promptEvalCount + evalCount))
                .build();

        return LLMResponse.builder()
                .id("ollama-" + System.currentTimeMillis())
                .model(model)
                .content(content)
                .finishReason(done ? "stop" : null)
                .usage(usage)
                .createdAt(Instant.now())
                .build();
    }

    private LLMChunk parseStreamChunk(String jsonLine) throws IOException {
        JsonNode root = objectMapper.readTree(jsonLine);

        String model = root.path("model").asText();
        String content = root.path("message").path("content").asText("");
        boolean done = root.path("done").asBoolean();

        // Skip empty chunks
        if (content.isEmpty() && !done) {
            return null;
        }

        return LLMChunk.builder()
                .id("ollama-chunk-" + System.currentTimeMillis())
                .model(model)
                .content(content)
                .finishReason(done ? "stop" : null)
                .index(0)
                .build();
    }
}

