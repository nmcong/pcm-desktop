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
 * Ollama Provider implementation for local models.
 *
 * <p>Supports: - All Ollama models (Llama 2, Llama 3, Mistral, Phi, Gemma, etc.) - Streaming
 * responses - Function calling (experimental) - Local/private deployment - No API key required
 *
 * <p>Documentation: https://ollama.ai/docs/api
 */
@Slf4j
public class OllamaProvider extends BaseProvider {

  private static final String DEFAULT_BASE_URL = "http://localhost:11434/api";
  private static final String DEFAULT_MODEL = "llama2";

  private final ObjectMapper objectMapper;

  public OllamaProvider() {
    super();
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public String getName() {
    return "ollama";
  }

  @Override
  protected String getDefaultModel() {
    return DEFAULT_MODEL;
  }

  @Override
  public boolean isReady() {
    // Ollama doesn't require API key
    return config != null;
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
        .providerName("ollama")
        .supportsStreaming(true)
        .supportsFunctionCalling(false) // Limited support
        .supportsThinking(false)
        .supportsVision(false) // Some models support
        .supportsJsonMode(true)
        .maxTokens(2048)
        .maxContextWindow(4096) // Varies by model
        .build();
  }

  @Override
  public List<ModelInfo> getModels() {
    List<ModelInfo> models = new ArrayList<>();

    // Try to fetch from Ollama API
    try {
      String baseUrl =
          config != null && config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
      URL url = new URL(baseUrl.replace("/api", "") + "/api/tags");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(5000);

      if (conn.getResponseCode() == 200) {
        try (BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
          StringBuilder response = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
            response.append(line);
          }

          JsonNode root = objectMapper.readTree(response.toString());
          JsonNode modelsArray = root.get("models");

          if (modelsArray != null) {
            for (JsonNode modelNode : modelsArray) {
              String name = modelNode.get("name").asText();
              long size = modelNode.has("size") ? modelNode.get("size").asLong() : 0;

              models.add(
                  ModelInfo.builder()
                      .id(name)
                      .name(name)
                      .description("Local Ollama model")
                      .provider("ollama")
                      .contextWindow(4096)
                      .maxOutputTokens(2048)
                      .costPer1kInputTokens(0.0) // Local = free
                      .costPer1kOutputTokens(0.0)
                      .build());
            }
          }
        }
      }
    } catch (Exception e) {
      log.warn("Failed to fetch Ollama models, using defaults: {}", e.getMessage());
    }

    // Default models if API call failed
    if (models.isEmpty()) {
      models.add(
          ModelInfo.builder()
              .id("llama2")
              .name("Llama 2")
              .description("Meta's Llama 2 model")
              .provider("ollama")
              .contextWindow(4096)
              .maxOutputTokens(2048)
              .costPer1kInputTokens(0.0)
              .costPer1kOutputTokens(0.0)
              .build());

      models.add(
          ModelInfo.builder()
              .id("mistral")
              .name("Mistral")
              .description("Mistral 7B model")
              .provider("ollama")
              .contextWindow(8192)
              .maxOutputTokens(2048)
              .costPer1kInputTokens(0.0)
              .costPer1kOutputTokens(0.0)
              .build());
    }

    return models;
  }

  @Override
  public ModelInfo getModelInfo(String modelId) {
    return getModels().stream().filter(m -> m.getId().equals(modelId)).findFirst().orElse(null);
  }

  /** Build JSON request body for Ollama API. */
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
      }

      // Options
      ObjectNode optionsNode = root.putObject("options");
      if (options.getTemperature() != 0.0) {
        optionsNode.put("temperature", options.getTemperature());
      }
      if (options.getMaxTokens() > 0) {
        optionsNode.put("num_predict", options.getMaxTokens());
      }
      if (options.getTopP() != null) {
        optionsNode.put("top_p", options.getTopP());
      }
      if (options.getStop() != null && !options.getStop().isEmpty()) {
        ArrayNode stopArray = optionsNode.putArray("stop");
        options.getStop().forEach(stopArray::add);
      }

      // Stream
      root.put("stream", stream);

      return objectMapper.writeValueAsString(root);

    } catch (Exception e) {
      throw new ProviderException("ollama", "Failed to build request body", e);
    }
  }

  /** Send HTTP request to Ollama API. */
  private String sendHttpRequest(String requestBody) throws ProviderException {
    try {
      String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
      URL url = new URL(baseUrl + "/chat");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      // Setup connection (Ollama is simple - no auth needed)
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
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
        throw new ProviderException("ollama", statusCode, "Ollama API error: " + error);
      }

    } catch (ProviderException e) {
      throw e;
    } catch (Exception e) {
      throw new ProviderException("ollama", "Failed to communicate with Ollama", e);
    }
  }

  /** Stream HTTP request (Ollama uses newline-delimited JSON). */
  private void streamHttpRequest(String requestBody, ChatEventListener listener) {
    try {
      String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
      URL url = new URL(baseUrl + "/chat");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      // Setup connection
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      // Send request
      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      // Read streaming response (newline-delimited JSON)
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

        String line;
        StringBuilder contentBuilder = new StringBuilder();

        while ((line = reader.readLine()) != null) {
          try {
            JsonNode chunk = objectMapper.readTree(line);

            if (chunk.has("message")) {
              JsonNode message = chunk.get("message");
              if (message.has("content")) {
                String content = message.get("content").asText();
                contentBuilder.append(content);
                listener.onToken(content);
              }
            }

            // Check if done
            if (chunk.has("done") && chunk.get("done").asBoolean()) {
              ChatResponse response =
                  ChatResponse.builder()
                      .id(UUID.randomUUID().toString())
                      .provider("ollama")
                      .content(contentBuilder.toString())
                      .finishReason("stop")
                      .build();
              listener.onComplete(response);
              break;
            }

          } catch (Exception e) {
            log.warn("Failed to parse streaming chunk: {}", line, e);
          }
        }
      }

    } catch (Exception e) {
      log.error("Streaming failed", e);
      listener.onError(e);
    }
  }

  /** Parse response from Ollama API. */
  private ChatResponse parseResponse(String responseBody) throws ProviderException {
    try {
      JsonNode root = objectMapper.readTree(responseBody);

      String model = root.has("model") ? root.get("model").asText() : getDefaultModel();

      // Content
      String content = null;
      if (root.has("message")) {
        JsonNode message = root.get("message");
        if (message.has("content")) {
          content = message.get("content").asText();
        }
      }

      // Usage (if available)
      Usage usage = null;
      if (root.has("prompt_eval_count") && root.has("eval_count")) {
        usage =
            Usage.builder()
                .promptTokens(root.get("prompt_eval_count").asInt())
                .completionTokens(root.get("eval_count").asInt())
                .build();
      }

      return ChatResponse.builder()
          .id(UUID.randomUUID().toString())
          .provider("ollama")
          .model(model)
          .content(content)
          .usage(usage)
          .finishReason("stop")
          .timestamp(LocalDateTime.now())
          .build();

    } catch (Exception e) {
      throw new ProviderException("ollama", "Failed to parse response", e);
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
