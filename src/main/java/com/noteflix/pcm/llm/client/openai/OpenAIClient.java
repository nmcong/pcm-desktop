package com.noteflix.pcm.llm.client.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noteflix.pcm.llm.api.FunctionCallingCapable;
import com.noteflix.pcm.llm.api.LLMClient;
import com.noteflix.pcm.llm.api.StreamingCapable;
import com.noteflix.pcm.llm.exception.LLMProviderException;
import com.noteflix.pcm.llm.model.*;
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
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI API client implementation Supports GPT-4, GPT-3.5-turbo, etc.
 *
 * <p>Features: - Basic chat completion - Streaming responses (SSE) - Function calling
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class OpenAIClient implements LLMClient, StreamingCapable, FunctionCallingCapable {

  private final LLMProviderConfig config;
  private final ObjectMapper objectMapper;
  private final SSEParser sseParser;

  public OpenAIClient(LLMProviderConfig config) {
    this.config = config;
    this.config.validate();
    this.objectMapper = new ObjectMapper();
    this.sseParser = new SSEParser();
    log.info("Initialized OpenAI client for model: {}", config.getModel());
  }

  @Override
  public LLMResponse sendMessage(LLMRequest request) {
    try {
      request.validate();

      // Build JSON request
      String jsonRequest = buildRequestJson(request);

      // Send HTTP request
      String jsonResponse = sendHttpRequest(jsonRequest, false);

      // Parse response
      return parseResponse(jsonResponse);

    } catch (IOException e) {
      log.error("Failed to send message to OpenAI", e);
      throw new LLMProviderException("Failed to communicate with OpenAI", e);
    }
  }

  @Override
  public Stream<LLMChunk> streamMessage(LLMRequest request) {
    try {
      request.validate();

      // Force streaming mode
      LLMRequest streamRequest =
          LLMRequest.builder()
              .model(request.getModel())
              .messages(request.getMessages())
              .temperature(request.getTemperature())
              .maxTokens(request.getMaxTokens())
              .topP(request.getTopP())
              .n(request.getN())
              .stop(request.getStop())
              .presencePenalty(request.getPresencePenalty())
              .frequencyPenalty(request.getFrequencyPenalty())
              .functions(request.getFunctions())
              .functionCall(request.getFunctionCall())
              .stream(true) // Force streaming
              .build();

      // Build JSON request
      String jsonRequest = buildRequestJson(streamRequest);

      // Send HTTP request and get stream
      List<LLMChunk> chunks = sendStreamingHttpRequest(jsonRequest);

      return chunks.stream();

    } catch (IOException e) {
      log.error("Failed to stream message from OpenAI", e);
      throw new LLMProviderException("Failed to stream from OpenAI", e);
    }
  }

  @Override
  public void streamMessage(LLMRequest request, StreamingObserver observer) {
    try {
      request.validate();

      // Force streaming mode
      LLMRequest streamRequest =
          LLMRequest.builder()
              .model(request.getModel())
              .messages(request.getMessages())
              .temperature(request.getTemperature())
              .maxTokens(request.getMaxTokens())
              .topP(request.getTopP())
              .n(request.getN())
              .stop(request.getStop())
              .presencePenalty(request.getPresencePenalty())
              .frequencyPenalty(request.getFrequencyPenalty())
              .functions(request.getFunctions())
              .functionCall(request.getFunctionCall())
              .stream(true)
              .build();

      // Build JSON request
      String jsonRequest = buildRequestJson(streamRequest);

      // Send streaming request with callback
      sendStreamingHttpRequestWithCallback(jsonRequest, observer);

    } catch (Exception e) {
      observer.onError(e);
    }
  }

  @Override
  public LLMResponse sendWithFunctions(LLMRequest request, List<FunctionDefinition> functions) {
    // Add functions to request
    LLMRequest modifiedRequest =
        LLMRequest.builder()
            .model(request.getModel())
            .messages(request.getMessages())
            .temperature(request.getTemperature())
            .maxTokens(request.getMaxTokens())
            .functions(functions)
            .functionCall("auto")
            .build();

    return sendMessage(modifiedRequest);
  }

  @Override
  public boolean supportsFunctionCalling() {
    return config.getSupportsFunctionCalling();
  }

  @Override
  public String getProviderName() {
    return "OpenAI";
  }

  @Override
  public boolean isAvailable() {
    // Simple availability check
    return config.getUrl() != null && config.getToken() != null;
  }

  @Override
  public String getModel() {
    return config.getModel() != null ? config.getModel() : "gpt-3.5-turbo";
  }

  // Private helper methods

  private String buildRequestJson(LLMRequest request) throws IOException {
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

    // Parameters
    if (request.getTemperature() != null) {
      root.put("temperature", request.getTemperature());
    }
    if (request.getMaxTokens() != null) {
      root.put("max_tokens", request.getMaxTokens());
    }
    if (request.getTopP() != null) {
      root.put("top_p", request.getTopP());
    }
    if (request.getN() != null) {
      root.put("n", request.getN());
    }
    if (request.getStream() != null) {
      root.put("stream", request.getStream());
    }

    // Functions (if any)
    if (request.getFunctions() != null && !request.getFunctions().isEmpty()) {
      ArrayNode functionsArray = root.putArray("functions");
      for (FunctionDefinition func : request.getFunctions()) {
        ObjectNode funcNode = functionsArray.addObject();
        funcNode.put("name", func.getName());
        funcNode.put("description", func.getDescription());
        funcNode.set("parameters", objectMapper.valueToTree(func.getParameters()));
      }

      if (request.getFunctionCall() != null) {
        root.set("function_call", objectMapper.valueToTree(request.getFunctionCall()));
      }
    }

    return objectMapper.writeValueAsString(root);
  }

  private String sendHttpRequest(String jsonRequest, boolean stream) throws IOException {
    URL url = new URL(config.getUrl());
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    try {
      // Setup connection
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Authorization", "Bearer " + config.getToken());
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
        throw new LLMProviderException("OpenAI API error", statusCode, errorResponse);
      }

    } finally {
      conn.disconnect();
    }
  }

  private String readResponse(HttpURLConnection conn) throws IOException {
    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        response.append(line.trim());
      }
      return response.toString();
    }
  }

  private String readErrorResponse(HttpURLConnection conn) throws IOException {
    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
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

    JsonNode choicesNode = root.path("choices");
    if (choicesNode.isArray() && choicesNode.size() > 0) {
      JsonNode firstChoice = choicesNode.get(0);
      JsonNode messageNode = firstChoice.path("message");

      String content = messageNode.path("content").asText();
      String finishReason = firstChoice.path("finish_reason").asText();

      // Parse usage
      LLMResponse.Usage usage = null;
      if (root.has("usage")) {
        JsonNode usageNode = root.path("usage");
        usage =
            LLMResponse.Usage.builder()
                .promptTokens(usageNode.path("prompt_tokens").asInt())
                .completionTokens(usageNode.path("completion_tokens").asInt())
                .totalTokens(usageNode.path("total_tokens").asInt())
                .build();
      }

      // Check for function call
      FunctionCall functionCall = null;
      if (messageNode.has("function_call")) {
        JsonNode funcNode = messageNode.path("function_call");
        functionCall =
            FunctionCall.builder()
                .name(funcNode.path("name").asText())
                .arguments(funcNode.path("arguments").asText())
                .build();
      }

      return LLMResponse.builder()
          .id(id)
          .model(model)
          .content(content)
          .finishReason(finishReason)
          .functionCall(functionCall)
          .usage(usage)
          .createdAt(Instant.now())
          .build();
    }

    throw new LLMProviderException("Invalid response format from OpenAI");
  }

  /**
   * Send HTTP request with streaming enabled
   *
   * @param jsonRequest JSON request body
   * @return List of chunks from the stream
   * @throws IOException if request fails
   */
  private List<LLMChunk> sendStreamingHttpRequest(String jsonRequest) throws IOException {
    URL url = new URL(config.getUrl());
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    try {
      // Setup connection
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Authorization", "Bearer " + config.getToken());
      conn.setRequestProperty("Accept", "text/event-stream"); // SSE
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
        // Parse SSE stream
        return sseParser.parseStream(conn.getInputStream());
      } else {
        String errorResponse = readErrorResponse(conn);
        throw new LLMProviderException("OpenAI API error", statusCode, errorResponse);
      }

    } finally {
      conn.disconnect();
    }
  }

  /**
   * Send HTTP request with streaming and callback observer
   *
   * @param jsonRequest JSON request body
   * @param observer Observer to handle chunks
   * @throws IOException if request fails
   */
  private void sendStreamingHttpRequestWithCallback(String jsonRequest, StreamingObserver observer)
      throws IOException {
    URL url = new URL(config.getUrl());
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    try {
      // Setup connection
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Authorization", "Bearer " + config.getToken());
      conn.setRequestProperty("Accept", "text/event-stream");
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
        // Stream with callback
        sseParser.streamWithCallback(
            conn.getInputStream(),
            new SSEParser.ChunkCallback() {
              @Override
              public void onChunk(LLMChunk chunk) {
                observer.onChunk(chunk);
              }

              @Override
              public void onComplete() {
                observer.onComplete();
              }

              @Override
              public void onError(Throwable error) {
                observer.onError(error);
              }
            });
      } else {
        String errorResponse = readErrorResponse(conn);
        observer.onError(new LLMProviderException("OpenAI API error", statusCode, errorResponse));
      }

    } finally {
      conn.disconnect();
    }
  }
}
