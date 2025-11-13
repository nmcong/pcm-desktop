package com.noteflix.pcm.llm.examples;

import com.noteflix.pcm.llm.api.ChatEventAdapter;
import com.noteflix.pcm.llm.model.*;
import com.noteflix.pcm.llm.provider.CustomAPIProvider;
import com.noteflix.pcm.llm.registry.FunctionRegistry;
import com.noteflix.pcm.llm.registry.ProviderRegistry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * Example usage of CustomAPIProvider.
 *
 * <p>Demonstrates: - Setup and configuration - Simple chat - Streaming with thinking - Function
 * calling (injected into content) - Token tracking
 */
@Slf4j
public class CustomAPIUsageExample {

  public static void main(String[] args) throws Exception {
    log.info("=== Custom API Provider Usage Example ===\n");

    // Example 1: Basic Setup
    example1_BasicSetup();

    // Example 2: Streaming with Thinking
    // example2_StreamingWithThinking();

    // Example 3: Function Calling
    // example3_FunctionCalling();

    // Example 4: Token Tracking
    // example4_TokenTracking();
  }

  /** Example 1: Basic setup and configuration. */
  private static void example1_BasicSetup() throws Exception {
    log.info("=== Example 1: Basic Setup ===");

    // Create and configure Custom API provider
    CustomAPIProvider provider = new CustomAPIProvider();
    provider.configure(
        ProviderConfig.builder()
            .baseUrl("https://your-api.com") // ⬅️ CHANGE THIS
            .apiKey(System.getenv("YOUR_API_KEY")) // ⬅️ OR USE YOUR AUTH
            .model("default")
            .timeoutMs(60000) // 60 seconds for longer responses
            .maxRetries(3)
            .build());

    // Register provider
    ProviderRegistry registry = ProviderRegistry.getInstance();
    registry.register("custom", provider);
    registry.setActive("custom");

    log.info("✅ Provider configured and registered");

    // Simple chat
    List<Message> messages =
        List.of(
            Message.system("You are a helpful AI assistant."),
            Message.user("What is the capital of France? Answer briefly."));

    CompletableFuture<ChatResponse> future = provider.chat(messages, ChatOptions.defaults());
    ChatResponse response = future.get();

    log.info("Response: {}", response.getContent());

    if (response.getThinkingContent() != null) {
      log.info("Thinking: {}", response.getThinkingContent());
    }

    if (response.getUsage() != null) {
      log.info("Tokens: {}", response.getTotalTokens());
    }

    log.info("✅ Example 1 complete\n");
  }

  /** Example 2: Streaming with thinking support. */
  private static void example2_StreamingWithThinking() throws Exception {
    log.info("=== Example 2: Streaming with Thinking ===");

    CustomAPIProvider provider = (CustomAPIProvider) ProviderRegistry.getInstance().getActive();

    List<Message> messages = List.of(Message.user("Explain quantum computing in simple terms."));

    log.info("Streaming response:\n");

    provider.chatStream(
        messages,
        ChatOptions.defaults(),
        new ChatEventAdapter() {

          @Override
          public void onThinking(String thinking) {
            // Thinking mode - show in different color/style in UI
            System.out.print("[Thinking] " + thinking);
          }

          @Override
          public void onToken(String token) {
            // Regular content
            System.out.print(token);
          }

          @Override
          public void onComplete(ChatResponse response) {
            System.out.println("\n");
            log.info("Stream complete");

            if (response.getUsage() != null) {
              log.info("Total tokens: {}", response.getTotalTokens());
            }
          }

          @Override
          public void onError(Throwable error) {
            log.error("Stream error", error);
          }
        });

    // Wait for streaming to complete
    Thread.sleep(5000);

    log.info("✅ Example 2 complete\n");
  }

  /** Example 3: Function calling (injected into content). */
  private static void example3_FunctionCalling() throws Exception {
    log.info("=== Example 3: Function Calling ===");

    // Register a function
    FunctionRegistry funcRegistry = FunctionRegistry.getInstance();

    com.noteflix.pcm.llm.api.RegisteredFunction searchFunc =
        new com.noteflix.pcm.llm.api.RegisteredFunction() {
          @Override
          public String getName() {
            return "search_database";
          }

          @Override
          public String getDescription() {
            return "Search for information in the database";
          }

          @Override
          public JsonSchema getParameters() {
            return JsonSchema.builder()
                .type("object")
                .property(
                    "query",
                    PropertySchema.builder().type("string").description("Search query").build())
                .required("query")
                .build();
          }

          @Override
          public Object execute(Map<String, Object> args) {
            String query = (String) args.get("query");
            log.info("Searching database for: {}", query);
            return Map.of("results", List.of("Result 1", "Result 2", "Result 3"), "total", 3);
          }
        };

    funcRegistry.register(searchFunc);

    // Chat with tools
    CustomAPIProvider provider = (CustomAPIProvider) ProviderRegistry.getInstance().getActive();

    List<Message> messages =
        List.of(Message.user("Search the database for 'quantum physics papers'"));

    ChatOptions options = ChatOptions.withTools(funcRegistry.getAllTools());

    ChatResponse response = provider.chat(messages, options).get();

    log.info("Response: {}", response.getContent());

    // Check if LLM requested a function call
    // (You'll need to parse the response content for function call format)
    if (response.getContent().contains("<function_call>")) {
      log.info("LLM requested a function call!");
      // Parse and execute function
      // This is simplified - you'd want proper XML/JSON parsing
    }

    log.info("✅ Example 3 complete\n");
  }

  /** Example 4: Token tracking. */
  private static void example4_TokenTracking() throws Exception {
    log.info("=== Example 4: Token Tracking ===");

    CustomAPIProvider provider = (CustomAPIProvider) ProviderRegistry.getInstance().getActive();

    // Chat first
    List<Message> messages = List.of(Message.user("Tell me a short joke."));

    ChatResponse response = provider.chat(messages, ChatOptions.defaults()).get();

    log.info("Response: {}", response.getContent());
    log.info("Conversation ID: {}", response.getId());

    // Check remaining tokens
    try {
      int remainingTokens = provider.getRemainingTokens(response.getId());
      log.info("Remaining tokens: {}", remainingTokens);
    } catch (Exception e) {
      log.warn("Could not get remaining tokens: {}", e.getMessage());
    }

    log.info("✅ Example 4 complete\n");
  }

  /** Example 5: JavaFX UI Integration. */
  public static void integrateWithUI() {
    // In your ViewModel:

    CustomAPIProvider provider = (CustomAPIProvider) ProviderRegistry.getInstance().get("custom");

    List<Message> messages = List.of(Message.user("User's question here"));

    provider.chatStream(
        messages,
        ChatOptions.defaults(),
        new ChatEventAdapter() {

          @Override
          public void onThinking(String thinking) {
            // Update UI to show thinking indicator
            javafx.application.Platform.runLater(
                () -> {
                  // thinkingIndicator.setText("Thinking: " + thinking);
                  // thinkingIndicator.setVisible(true);
                });
          }

          @Override
          public void onToken(String token) {
            // Append token to response area
            javafx.application.Platform.runLater(
                () -> {
                  // responseTextArea.appendText(token);
                  // scrollToBottom();
                });
          }

          @Override
          public void onComplete(ChatResponse response) {
            javafx.application.Platform.runLater(
                () -> {
                  // thinkingIndicator.setVisible(false);
                  // setBusy(false);

                  // Show usage
                  if (response.getUsage() != null) {
                    // tokensLabel.setText("Tokens: " + response.getTotalTokens());
                  }
                });
          }

          @Override
          public void onError(Throwable error) {
            javafx.application.Platform.runLater(
                () -> {
                  // showError(error.getMessage());
                  // setBusy(false);
                });
          }
        });
  }
}
