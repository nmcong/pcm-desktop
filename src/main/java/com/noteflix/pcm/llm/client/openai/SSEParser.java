package com.noteflix.pcm.llm.client.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noteflix.pcm.llm.exception.StreamingException;
import com.noteflix.pcm.llm.model.LLMChunk;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Parser for Server-Sent Events (SSE) from OpenAI streaming API
 *
 * <p>SSE Format: data: {"id":"chatcmpl-123","choices":[{"delta":{"content":"Hello"},"index":0}]}
 * data: {"id":"chatcmpl-123","choices":[{"delta":{"content":" world"},"index":0}]} data: [DONE]
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class SSEParser {

  private static final String DATA_PREFIX = "data: ";
  private static final String DONE_MESSAGE = "[DONE]";
  private final ObjectMapper objectMapper;

  public SSEParser() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Parse SSE stream and return list of chunks
   *
   * @param inputStream The stream from HTTP response
   * @return List of parsed chunks
   * @throws StreamingException if parsing fails
   */
  public List<LLMChunk> parseStream(InputStream inputStream) throws StreamingException {
    List<LLMChunk> chunks = new ArrayList<>();

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();

        // Skip empty lines
        if (line.isEmpty()) {
          continue;
        }

        // Check if it's a data line
        if (line.startsWith(DATA_PREFIX)) {
          String data = line.substring(DATA_PREFIX.length()).trim();

          // Check for completion
          if (DONE_MESSAGE.equals(data)) {
            log.debug("Stream completed");
            break;
          }

          // Parse JSON chunk
          try {
            LLMChunk chunk = parseChunk(data);
            if (chunk != null) {
              chunks.add(chunk);
            }
          } catch (Exception e) {
            log.warn("Failed to parse chunk: {}", data, e);
            // Continue processing other chunks
          }
        }
      }

    } catch (IOException e) {
      throw new StreamingException("Failed to read SSE stream", e);
    }

    return chunks;
  }

  /**
   * Parse a single SSE chunk
   *
   * @param jsonData JSON string from SSE data line
   * @return Parsed LLMChunk or null if no content
   */
  public LLMChunk parseChunk(String jsonData) throws IOException {
    JsonNode root = objectMapper.readTree(jsonData);

    String id = root.path("id").asText();
    String model = root.path("model").asText();

    JsonNode choicesNode = root.path("choices");
    if (choicesNode.isArray() && choicesNode.size() > 0) {
      JsonNode firstChoice = choicesNode.get(0);
      JsonNode deltaNode = firstChoice.path("delta");

      String content = deltaNode.path("content").asText("");
      String role = deltaNode.path("role").asText(null);
      String finishReason = firstChoice.path("finish_reason").asText(null);
      int index = firstChoice.path("index").asInt(0);

      // Skip chunks with no content (unless it's the first chunk with role)
      if (content.isEmpty() && role == null && finishReason == null) {
        return null;
      }

      return LLMChunk.builder()
          .id(id)
          .model(model)
          .content(content)
          .finishReason(finishReason)
          .index(index)
          .build();
    }

    return null;
  }

  /**
   * Stream and process SSE events with callback
   *
   * @param inputStream The stream from HTTP response
   * @param callback Callback to handle each chunk
   * @throws StreamingException if streaming fails
   */
  public void streamWithCallback(InputStream inputStream, ChunkCallback callback)
      throws StreamingException {

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();

        if (line.isEmpty()) {
          continue;
        }

        if (line.startsWith(DATA_PREFIX)) {
          String data = line.substring(DATA_PREFIX.length()).trim();

          if (DONE_MESSAGE.equals(data)) {
            callback.onComplete();
            break;
          }

          try {
            LLMChunk chunk = parseChunk(data);
            if (chunk != null) {
              callback.onChunk(chunk);
            }
          } catch (Exception e) {
            log.warn("Failed to parse chunk, continuing...", e);
          }
        }
      }

    } catch (IOException e) {
      callback.onError(new StreamingException("Failed to read SSE stream", e));
    }
  }

  /** Callback interface for streaming chunks */
  public interface ChunkCallback {
    void onChunk(LLMChunk chunk);

    void onComplete();

    void onError(Throwable error);
  }
}
