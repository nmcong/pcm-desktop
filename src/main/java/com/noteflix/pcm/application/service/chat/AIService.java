package com.noteflix.pcm.application.service.chat;

import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.domain.chat.MessageRole;
import com.noteflix.pcm.llm.api.ChatEventAdapter;
import com.noteflix.pcm.llm.api.ChatEventListener;
import com.noteflix.pcm.llm.api.LLMProvider;
import com.noteflix.pcm.llm.model.*;
import com.noteflix.pcm.llm.provider.AnthropicProvider;
import com.noteflix.pcm.llm.provider.CustomAPIProvider;
import com.noteflix.pcm.llm.provider.OllamaProvider;
import com.noteflix.pcm.llm.provider.OpenAIProvider;
import com.noteflix.pcm.llm.registry.ProviderRegistry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for AI integration - FULLY MIGRATED TO NEW ARCHITECTURE
 *
 * <p>Uses new ProviderRegistry & LLMProvider interface. Supports: OpenAI, Anthropic, Ollama, and
 * Custom API providers.
 *
 * <p>New Features: - Event-driven streaming with thinking mode - Token tracking & monitoring -
 * Error callbacks - Multi-provider support - CustomAPIProvider for your LLM service
 *
 * @author PCM Team
 * @version 2.0.0 (MIGRATED FROM OLD ARCHITECTURE)
 */
@Slf4j
public class AIService {

  private final ProviderRegistry providerRegistry;

  // Callbacks for monitoring
  private Consumer<String> onThinking;
  private Consumer<Integer> onTokenUpdate;
  private Consumer<String> onError;

  /** Default constructor - initializes all providers */
  public AIService() {
    this.providerRegistry = ProviderRegistry.getInstance();
    initializeProviders();
    log.info(
        "✅ AIService initialized with NEW provider architecture (OpenAI, Anthropic, Ollama,"
            + " Custom)");
  }

  /** Initialize all available providers. */
  private void initializeProviders() {
    // OpenAI
    try {
      String openaiKey = System.getenv("OPENAI_API_KEY");
      if (openaiKey != null && !openaiKey.isEmpty()) {
        OpenAIProvider openai = new OpenAIProvider();
        openai.configure(
            ProviderConfig.builder()
                .apiKey(openaiKey)
                .model("gpt-4-turbo-preview")
                .timeoutMs(60000)
                .maxRetries(3)
                .build());
        providerRegistry.register("openai", openai);
        log.info("✅ Registered OpenAI provider");
      }
    } catch (Exception e) {
      log.warn("Could not initialize OpenAI: {}", e.getMessage());
    }

    // Anthropic (Claude)
    try {
      String anthropicKey = System.getenv("ANTHROPIC_API_KEY");
      if (anthropicKey != null && !anthropicKey.isEmpty()) {
        AnthropicProvider anthropic = new AnthropicProvider();
        anthropic.configure(
            ProviderConfig.builder()
                .apiKey(anthropicKey)
                .model("claude-3-5-sonnet-20241022")
                .timeoutMs(60000)
                .maxRetries(3)
                .build());
        providerRegistry.register("anthropic", anthropic);
        log.info("✅ Registered Anthropic provider");
      }
    } catch (Exception e) {
      log.warn("Could not initialize Anthropic: {}", e.getMessage());
    }

    // Ollama (Local)
    try {
      OllamaProvider ollama = new OllamaProvider();
      ollama.configure(
          ProviderConfig.builder()
              .baseUrl("http://localhost:11434/api")
              .model("llama2")
              .timeoutMs(120000)
              .build());

      if (ollama.testConnection()) {
        providerRegistry.register("ollama", ollama);
        log.info("✅ Registered Ollama provider");
      }
    } catch (Exception e) {
      log.warn("Could not initialize Ollama: {}", e.getMessage());
    }

    // Custom API (your service)
    try {
      String customUrl = System.getenv("CUSTOM_LLM_URL");
      String customKey = System.getenv("CUSTOM_LLM_KEY");

      if (customUrl != null && !customUrl.isEmpty()) {
        CustomAPIProvider custom = new CustomAPIProvider();
        custom.configure(
            ProviderConfig.builder()
                .baseUrl(customUrl)
                .apiKey(customKey)
                .model("default")
                .timeoutMs(60000)
                .maxRetries(3)
                .build());
        providerRegistry.register("custom", custom);
        log.info("✅ Registered Custom API provider");
      }
    } catch (Exception e) {
      log.warn("Could not initialize Custom API: {}", e.getMessage());
    }

    // Set default active provider
    if (providerRegistry.hasProvider("openai")) {
      providerRegistry.setActive("openai");
    } else if (providerRegistry.hasProvider("anthropic")) {
      providerRegistry.setActive("anthropic");
    } else if (providerRegistry.hasProvider("ollama")) {
      providerRegistry.setActive("ollama");
    } else if (providerRegistry.hasProvider("custom")) {
      providerRegistry.setActive("custom");
    }
  }

  /**
   * Generate AI response for a user message.
   *
   * @param conversation Current conversation
   * @param userMessage User message content
   * @return AI response message
   */
  public Message generateResponse(Conversation conversation, String userMessage) {
    log.info("Generating AI response for conversation: {}", conversation.getId());

    try {
      // Initialize provider for this conversation
      LLMProvider provider = initializeProvider(conversation);

      // Convert messages
      List<com.noteflix.pcm.llm.model.Message> llmMessages = convertToLLMMessages(conversation);
      llmMessages.add(com.noteflix.pcm.llm.model.Message.user(userMessage));

      // Build options
      ChatOptions options =
          ChatOptions.builder()
              .model(conversation.getLlmModel())
              .temperature(0.7)
              .maxTokens(2000)
              .build();

      // Get response
      ChatResponse response = provider.chat(llmMessages, options).get();

      // Convert to domain message
      Message aiMessage =
          Message.builder()
              .conversationId(conversation.getId())
              .role(MessageRole.ASSISTANT)
              .content(response.getContent())
              .tokenCount(response.getTotalTokens())
              .createdAt(LocalDateTime.now())
              .build();

      // Update token count callback
      if (onTokenUpdate != null && response.getUsage() != null) {
        onTokenUpdate.accept(response.getTotalTokens());
      }

      log.info("Generated AI response for conversation: {}", conversation.getId());
      return aiMessage;

    } catch (Exception e) {
      log.error("Failed to generate AI response", e);

      // Notify error callback
      if (onError != null) {
        onError.accept(e.getMessage());
      }

      // Return error message
      return Message.builder()
          .conversationId(conversation.getId())
          .role(MessageRole.ASSISTANT)
          .content("I apologize, but I encountered an error: " + e.getMessage())
          .createdAt(LocalDateTime.now())
          .build();
    }
  }

  /**
   * Stream AI response with ChatEventListener (NEW API).
   *
   * @param conversation Current conversation
   * @param userMessage User message content
   * @param listener Event listener for streaming
   */
  public void streamResponse(
      Conversation conversation, String userMessage, ChatEventListener listener) {

    log.info("Streaming AI response for conversation: {} (NEW API)", conversation.getId());

    try {
      // Initialize provider
      LLMProvider provider = initializeProvider(conversation);

      // Convert messages
      List<com.noteflix.pcm.llm.model.Message> llmMessages = convertToLLMMessages(conversation);
      llmMessages.add(com.noteflix.pcm.llm.model.Message.user(userMessage));

      // Build options
      ChatOptions options =
          ChatOptions.builder()
              .model(conversation.getLlmModel())
              .temperature(0.7)
              .maxTokens(2000)
              .build();

      // Wrap listener to integrate with callbacks
      ChatEventListener wrappedListener =
          new ChatEventAdapter() {
            @Override
            public void onThinking(String thinking) {
              // Forward to original listener
              listener.onThinking(thinking);

              // Also call our callback
              if (AIService.this.onThinking != null) {
                AIService.this.onThinking.accept(thinking);
              }
            }

            @Override
            public void onToken(String token) {
              listener.onToken(token);
            }

            @Override
            public void onToolCall(ToolCall toolCall) {
              listener.onToolCall(toolCall);
            }

            @Override
            public void onComplete(ChatResponse response) {
              listener.onComplete(response);

              // Update token count
              if (onTokenUpdate != null && response.getUsage() != null) {
                onTokenUpdate.accept(response.getTotalTokens());
              }
            }

            @Override
            public void onError(Throwable error) {
              listener.onError(error);

              // Notify error callback
              if (onError != null) {
                onError.accept(error.getMessage());
              }
            }
          };

      // Stream response
      provider.chatStream(llmMessages, options, wrappedListener);

    } catch (Exception e) {
      log.error("Failed to stream AI response", e);
      listener.onError(e);

      if (onError != null) {
        onError.accept(e.getMessage());
      }
    }
  }

  /**
   * Stream AI response with observer - LEGACY API (for backward compatibility).
   *
   * @param conversation Current conversation
   * @param userMessage User message content
   * @param observer Observer for streaming chunks
   */
  public void streamResponse(
      Conversation conversation,
      String userMessage,
      com.noteflix.pcm.llm.model.StreamingObserver observer) {

    log.info("Streaming AI response for conversation: {}", conversation.getId());

    try {
      // Initialize provider
      LLMProvider provider = initializeProvider(conversation);

      // Convert messages
      List<com.noteflix.pcm.llm.model.Message> llmMessages = convertToLLMMessages(conversation);
      llmMessages.add(com.noteflix.pcm.llm.model.Message.user(userMessage));

      // Build options
      ChatOptions options =
          ChatOptions.builder()
              .model(conversation.getLlmModel())
              .temperature(0.7)
              .maxTokens(2000)
              .build();

      // Wrap observer to use new ChatEventListener
      ChatEventListener eventListener =
          new ChatEventAdapter() {
            private StringBuilder content = new StringBuilder();

            @Override
            public void onThinking(String thinking) {
              // Forward to callback
              if (AIService.this.onThinking != null) {
                AIService.this.onThinking.accept(thinking);
              }
            }

            @Override
            public void onToken(String token) {
              content.append(token);
              // Convert to old format for compatibility
              com.noteflix.pcm.llm.model.LLMChunk chunk =
                  com.noteflix.pcm.llm.model.LLMChunk.builder().content(token).build();
              observer.onChunk(chunk);
            }

            @Override
            public void onComplete(ChatResponse response) {
              // Update token count
              if (onTokenUpdate != null && response.getUsage() != null) {
                onTokenUpdate.accept(response.getTotalTokens());
              }
              observer.onComplete();
            }

            @Override
            public void onError(Throwable error) {
              // Notify error callback
              if (onError != null) {
                onError.accept(error.getMessage());
              }
              observer.onError(error);
            }
          };

      // Stream with new architecture
      provider.chatStream(llmMessages, options, eventListener);

    } catch (Exception e) {
      log.error("Failed to stream AI response", e);
      if (onError != null) {
        onError.accept(e.getMessage());
      }
      observer.onError(e);
    }
  }

  /** Initialize LLM provider based on conversation settings. */
  private LLMProvider initializeProvider(Conversation conversation) throws Exception {
    String providerName = conversation.getLlmProvider();

    // If no provider specified, use first available
    if (providerName == null || providerName.trim().isEmpty()) {
      List<String> available = providerRegistry.getAvailableProviders();

      if (available.isEmpty()) {
        throw new IllegalStateException(
            "No LLM providers available. Please configure at least one provider (OpenAI, Anthropic,"
                + " Ollama, or Custom API). Set environment variables: OPENAI_API_KEY,"
                + " ANTHROPIC_API_KEY, or CUSTOM_LLM_URL");
      }

      providerName = available.get(0);
      log.info("No provider specified, using first available: {}", providerName);
    }

    providerName = providerName.toLowerCase();

    // Get provider from registry
    LLMProvider provider = providerRegistry.get(providerName);

    if (provider == null) {
      // Provider not available, try to fallback
      List<String> available = providerRegistry.getAvailableProviders();

      if (available.isEmpty()) {
        throw new IllegalStateException(
            "No LLM providers available. "
                + "Requested provider '"
                + providerName
                + "' not found. Please configure at least one provider (OpenAI, Anthropic, Ollama,"
                + " or Custom API). Set environment variables: OPENAI_API_KEY, ANTHROPIC_API_KEY,"
                + " or CUSTOM_LLM_URL");
      }

      // Fallback to first available
      providerName = available.get(0);
      provider = providerRegistry.get(providerName);

      log.warn("Requested provider not available, falling back to: {}", providerName);
    }

    // Configure provider
    ProviderConfig config =
        ProviderConfig.builder()
            .model(conversation.getLlmModel())
            .apiKey(getProviderToken(providerName))
            .baseUrl(getProviderUrl(providerName))
            .build();
    provider.configure(config);

    // Set as active
    providerRegistry.setActive(providerName);

    return provider;
  }

  /** Convert domain messages to LLM messages. */
  private List<com.noteflix.pcm.llm.model.Message> convertToLLMMessages(Conversation conversation) {
    List<com.noteflix.pcm.llm.model.Message> llmMessages = new ArrayList<>();

    // Add system prompt
    if (conversation.getSystemPrompt() != null
        && !conversation.getSystemPrompt().trim().isEmpty()) {
      llmMessages.add(com.noteflix.pcm.llm.model.Message.system(conversation.getSystemPrompt()));
    }

    // Add conversation messages
    if (conversation.getMessages() != null) {
      for (Message msg : conversation.getMessages()) {
        com.noteflix.pcm.llm.model.Message.Role role;

        switch (msg.getRole()) {
          case SYSTEM:
            role = com.noteflix.pcm.llm.model.Message.Role.SYSTEM;
            break;
          case USER:
            role = com.noteflix.pcm.llm.model.Message.Role.USER;
            break;
          case ASSISTANT:
            role = com.noteflix.pcm.llm.model.Message.Role.ASSISTANT;
            break;
          case FUNCTION:
            // Map old FUNCTION role to new TOOL role for backward compatibility
            role = com.noteflix.pcm.llm.model.Message.Role.TOOL;
            break;
          default:
            role = com.noteflix.pcm.llm.model.Message.Role.USER;
        }

        llmMessages.add(
            com.noteflix.pcm.llm.model.Message.builder()
                .role(role)
                .content(msg.getContent())
                .build());
      }
    }

    return llmMessages;
  }

  /** Get current LLM provider name */
  public String getCurrentProvider() {
    LLMProvider provider = providerRegistry.getActive();
    return provider != null ? provider.getName() : "none";
  }

  /** Get current LLM model */
  public String getCurrentModel() {
    try {
      LLMProvider provider = providerRegistry.getActive();
      return provider != null && provider.getConfig() != null
          ? provider.getConfig().getModel()
          : "unknown";
    } catch (Exception e) {
      return "unknown";
    }
  }

  /** Check if streaming is supported */
  public boolean supportsStreaming() {
    try {
      LLMProvider provider = providerRegistry.getActive();
      return provider != null && provider.getCapabilities().isSupportsStreaming();
    } catch (Exception e) {
      return false;
    }
  }

  /** Get available providers. */
  public List<String> getAvailableProviders() {
    return providerRegistry.getAvailableProviders();
  }

  /** Switch to a different provider. */
  public void switchProvider(String providerName) {
    providerRegistry.setActive(providerName);
    log.info("Switched to provider: {}", providerName);
  }

  /** Get remaining tokens for custom provider. */
  public int getRemainingTokens(String conversationId) {
    LLMProvider provider = providerRegistry.getActive();

    if (provider instanceof CustomAPIProvider) {
      try {
        return ((CustomAPIProvider) provider).getRemainingTokens(conversationId);
      } catch (Exception e) {
        log.warn("Could not get remaining tokens: {}", e.getMessage());
        return -1;
      }
    }

    return -1; // Not supported
  }

  // ========== Callback Setters ==========

  /** Set callback for thinking mode. */
  public void setOnThinking(Consumer<String> callback) {
    this.onThinking = callback;
  }

  /** Set callback for token updates. */
  public void setOnTokenUpdate(Consumer<Integer> callback) {
    this.onTokenUpdate = callback;
  }

  /** Set callback for errors. */
  public void setOnError(Consumer<String> callback) {
    this.onError = callback;
  }

  // ========== Provider Configuration Helpers ==========

  /** Get provider API key/token from environment. */
  private String getProviderToken(String providerName) {
    switch (providerName.toLowerCase()) {
      case "openai":
        return System.getenv("OPENAI_API_KEY");
      case "anthropic":
        return System.getenv("ANTHROPIC_API_KEY");
      case "ollama":
        return null; // Ollama typically doesn't need a token
      case "custom":
        return System.getenv("CUSTOM_LLM_KEY");
      default:
        return null;
    }
  }

  /** Get provider base URL from environment or use default. */
  private String getProviderUrl(String providerName) {
    switch (providerName.toLowerCase()) {
      case "openai":
        String openaiUrl = System.getenv("OPENAI_API_URL");
        return openaiUrl != null ? openaiUrl : "https://api.openai.com/v1";
      case "anthropic":
        String anthropicUrl = System.getenv("ANTHROPIC_API_URL");
        return anthropicUrl != null ? anthropicUrl : "https://api.anthropic.com/v1";
      case "ollama":
        String ollamaUrl = System.getenv("OLLAMA_API_URL");
        return ollamaUrl != null ? ollamaUrl : "http://localhost:11434";
      case "custom":
        return System.getenv("CUSTOM_LLM_URL");
      default:
        return null;
    }
  }
}
