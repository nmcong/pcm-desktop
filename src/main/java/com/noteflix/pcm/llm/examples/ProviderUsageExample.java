package com.noteflix.pcm.llm.examples;

import com.noteflix.pcm.llm.api.ChatEventAdapter;
import com.noteflix.pcm.llm.api.LLMProvider;
import com.noteflix.pcm.llm.model.*;
import com.noteflix.pcm.llm.provider.AnthropicProvider;
import com.noteflix.pcm.llm.provider.OllamaProvider;
import com.noteflix.pcm.llm.provider.OpenAIProvider;
import com.noteflix.pcm.llm.registry.FunctionRegistry;
import com.noteflix.pcm.llm.registry.ProviderRegistry;
import com.noteflix.pcm.llm.tool.ToolExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

/**
 * Example usage of new LLM Provider architecture.
 *
 * <p>Demonstrates: - Provider registration & configuration - Simple chat - Streaming chat -
 * Function calling - Multiple providers - Error handling
 */
@Slf4j
public class ProviderUsageExample {

    public static void main(String[] args) throws Exception {
        log.info("=== LLM Provider Usage Examples ===\n");

        // Example 1: Basic Setup & Chat
        example1_BasicSetup();

        // Example 2: Streaming
        // example2_Streaming();

        // Example 3: Multiple Providers
        // example3_MultipleProviders();

        // Example 4: Function Calling
        // example4_FunctionCalling();
    }

    /**
     * Example 1: Basic setup and simple chat.
     */
    private static void example1_BasicSetup() throws Exception {
        log.info("=== Example 1: Basic Setup & Chat ===");

        // Create and configure OpenAI provider
        OpenAIProvider openai = new OpenAIProvider();
        openai.configure(
                ProviderConfig.builder()
                        .apiKey(System.getenv("OPENAI_API_KEY"))
                        .model("gpt-3.5-turbo")
                        .maxRetries(3)
                        .timeoutMs(30000)
                        .build());

        // Register provider
        ProviderRegistry registry = ProviderRegistry.getInstance();
        registry.register("openai", openai);
        registry.setActive("openai");

        // Get active provider
        LLMProvider provider = registry.getActive();
        log.info("Active provider: {}", provider.getName());

        // Check capabilities
        ProviderCapabilities caps = provider.getCapabilities();
        log.info(
                "Streaming: {}, Tools: {}, Context: {}",
                caps.isSupportsStreaming(),
                caps.isSupportsFunctionCalling(),
                caps.getMaxContextWindow());

        // Simple chat
        List<Message> messages =
                List.of(
                        Message.system("You are a helpful assistant."),
                        Message.user("What is 2+2? Answer briefly."));

        CompletableFuture<ChatResponse> future = provider.chat(messages, ChatOptions.defaults());
        ChatResponse response = future.get();

        log.info("Response: {}", response.getContent());
        log.info("Tokens used: {}", response.getTotalTokens());

        log.info("✅ Example 1 complete\n");
    }

    /**
     * Example 2: Streaming responses.
     */
    private static void example2_Streaming() throws Exception {
        log.info("=== Example 2: Streaming Chat ===");

        LLMProvider provider = ProviderRegistry.getInstance().getActive();

        List<Message> messages = List.of(Message.user("Write a short poem about coding."));

        log.info("Streaming response:");

        provider.chatStream(
                messages,
                ChatOptions.defaults(),
                new ChatEventAdapter() {
                    @Override
                    public void onToken(String token) {
                        System.out.print(token);
                    }

                    @Override
                    public void onComplete(ChatResponse response) {
                        System.out.println("\n");
                        log.info("Stream complete. Tokens: {}", response.getTotalTokens());
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("Stream error", error);
                    }
                });

        // Wait a bit for streaming to complete
        Thread.sleep(5000);

        log.info("✅ Example 2 complete\n");
    }

    /**
     * Example 3: Using multiple providers.
     */
    private static void example3_MultipleProviders() throws Exception {
        log.info("=== Example 3: Multiple Providers ===");

        ProviderRegistry registry = ProviderRegistry.getInstance();

        // Setup Anthropic
        AnthropicProvider anthropic = new AnthropicProvider();
        anthropic.configure(
                ProviderConfig.builder()
                        .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                        .model("claude-3-5-sonnet-20241022")
                        .build());
        registry.register("anthropic", anthropic);

        // Setup Ollama (local)
        OllamaProvider ollama = new OllamaProvider();
        ollama.configure(
                ProviderConfig.builder().baseUrl("http://localhost:11434/api").model("llama2").build());
        registry.register("ollama", ollama);

        // Test each provider
        String question = "What is the capital of France? Answer in one word.";
        List<Message> messages = List.of(Message.user(question));

        for (String providerName : List.of("openai", "anthropic", "ollama")) {
            try {
                LLMProvider provider = registry.get(providerName);
                if (provider == null || !provider.isReady()) {
                    log.warn("Provider {} not available", providerName);
                    continue;
                }

                registry.setActive(providerName);

                log.info("Testing {}...", providerName);
                ChatResponse response = provider.chat(messages, ChatOptions.defaults()).get();
                log.info("{}: {}", providerName, response.getContent().trim());

            } catch (Exception e) {
                log.warn("Provider {} failed: {}", providerName, e.getMessage());
            }
        }

        log.info("✅ Example 3 complete\n");
    }

    /**
     * Example 4: Function calling.
     */
    private static void example4_FunctionCalling() throws Exception {
        log.info("=== Example 4: Function Calling ===");

        // Register a simple function
        FunctionRegistry funcRegistry = FunctionRegistry.getInstance();

        // Create anonymous implementation of RegisteredFunction
        com.noteflix.pcm.llm.api.RegisteredFunction weatherFunc =
                new com.noteflix.pcm.llm.api.RegisteredFunction() {
                    @Override
                    public String getName() {
                        return "get_weather";
                    }

                    @Override
                    public String getDescription() {
                        return "Get current weather for a location";
                    }

                    @Override
                    public JsonSchema getParameters() {
                        return JsonSchema.builder()
                                .type("object")
                                .property(
                                        "location",
                                        PropertySchema.builder().type("string").description("City name").build())
                                .required("location")
                                .build();
                    }

                    @Override
                    public Object execute(Map<String, Object> args) {
                        String location = (String) args.get("location");
                        return Map.of(
                                "location", location,
                                "temperature", "22°C",
                                "condition", "Sunny");
                    }
                };

        funcRegistry.register(weatherFunc);

        // Chat with tools
        LLMProvider provider = ProviderRegistry.getInstance().getActive();

        List<Message> messages = new ArrayList<>();
        messages.add(Message.user("What's the weather in Paris?"));

        ChatOptions options = ChatOptions.withTools(funcRegistry.getAllTools());

        ChatResponse response = provider.chat(messages, options).get();

        log.info("Response finish reason: {}", response.getFinishReason());

        // Check for tool calls
        if (response.hasToolCalls()) {
            log.info("LLM requested {} tool calls", response.getToolCalls().size());

            // Execute tools
            ToolExecutor executor = new ToolExecutor(funcRegistry);
            List<ToolResult> results = executor.executeAll(response.getToolCalls());

            // Add results to conversation
            messages.add(Message.assistantWithTools(response.getToolCalls()));
            for (ToolResult result : results) {
                log.info("Tool {} result: {}", result.getToolName(), result.getResultAsString());
                messages.add(Message.tool(result.getToolCallId(), result.getResultAsString()));
            }

            // Get final response
            ChatResponse finalResponse = provider.chat(messages, ChatOptions.defaults()).get();
            log.info("Final response: {}", finalResponse.getContent());
        } else {
            log.info("No tool calls. Direct response: {}", response.getContent());
        }

        log.info("✅ Example 4 complete\n");
    }
}
