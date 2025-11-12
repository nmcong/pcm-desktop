package com.noteflix.pcm.llm.examples;

import com.noteflix.pcm.llm.model.*;
import com.noteflix.pcm.llm.service.LLMService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive demo for PCM Desktop API Integration
 * <p>
 * Cách sử dụng:
 * 1. Set environment variable: export OPENAI_API_KEY=your-key
 * 2. Run: java APIDemo
 * 3. Follow prompts to test different features
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class APIDemo {

    private static LLMService llmService;
    private static Scanner scanner;

    public static void main(String[] args) {
        System.out.println("🚀 PCM Desktop API Integration Demo");
        System.out.println("=====================================");

        scanner = new Scanner(System.in);

        // Initialize service
        if (!initializeService()) {
            System.out.println("❌ Failed to initialize service. Exiting...");
            return;
        }

        System.out.println("✅ Service initialized successfully!");
        System.out.println();

        // Main menu
        showMainMenu();

        scanner.close();
    }

    private static boolean initializeService() {
        try {
            llmService = new LLMService();

            // Check for API key
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.out.println("⚠️  OPENAI_API_KEY environment variable not found");
                System.out.println("Please set it with: export OPENAI_API_KEY=your-key");
                return false;
            }

            // Configure OpenAI (default)
            LLMProviderConfig config = LLMProviderConfig.builder()
                    .provider(LLMProviderConfig.Provider.OPENAI)
                    .name("OpenAI GPT Demo")
                    .url("https://api.openai.com/v1/chat/completions")
                    .token(apiKey)
                    .model("gpt-3.5-turbo")
                    .supportsStreaming(true)
                    .supportsFunctionCalling(true)
                    .timeout(30)
                    .build();

            llmService.initialize(config);

            return true;

        } catch (Exception e) {
            log.error("Failed to initialize service", e);
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\\n🔧 Choose a demo:");
            System.out.println("1. Simple Chat");
            System.out.println("2. Advanced Request");
            System.out.println("3. Streaming Response");
            System.out.println("4. Multi-turn Conversation");
            System.out.println("5. Provider Information");
            System.out.println("6. Switch to GPT-4");
            System.out.println("7. Function Calling Demo");
            System.out.println("0. Exit");
            System.out.print("\\nYour choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    demoSimpleChat();
                    break;
                case "2":
                    demoAdvancedRequest();
                    break;
                case "3":
                    demoStreaming();
                    break;
                case "4":
                    demoConversation();
                    break;
                case "5":
                    showProviderInfo();
                    break;
                case "6":
                    switchToGPT4();
                    break;
                case "7":
                    demoFunctionCalling();
                    break;
                case "0":
                    System.out.println("👋 Goodbye!");
                    return;
                default:
                    System.out.println("❌ Invalid choice. Please try again.");
            }
        }
    }

    private static void demoSimpleChat() {
        System.out.println("\\n💬 Simple Chat Demo");
        System.out.println("=====================");
        System.out.print("Enter your message: ");
        String userMessage = scanner.nextLine();

        if (userMessage.trim().isEmpty()) {
            System.out.println("❌ Empty message");
            return;
        }

        try {
            System.out.println("\\n🤖 AI is thinking...");
            long startTime = System.currentTimeMillis();

            String response = llmService.chat(userMessage);

            long endTime = System.currentTimeMillis();

            System.out.println("\\n✅ Response (" + (endTime - startTime) + "ms):");
            System.out.println(response);

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            log.error("Chat error", e);
        }
    }

    private static void demoAdvancedRequest() {
        System.out.println("\\n⚙️  Advanced Request Demo");
        System.out.println("==========================");

        System.out.print("System message (optional): ");
        String systemMsg = scanner.nextLine().trim();

        System.out.print("User message: ");
        String userMsg = scanner.nextLine().trim();

        if (userMsg.isEmpty()) {
            System.out.println("❌ User message required");
            return;
        }

        System.out.print("Temperature (0.0-2.0, default 0.7): ");
        String tempStr = scanner.nextLine().trim();
        double temperature = tempStr.isEmpty() ? 0.7 : Double.parseDouble(tempStr);

        System.out.print("Max tokens (default 1000): ");
        String tokensStr = scanner.nextLine().trim();
        int maxTokens = tokensStr.isEmpty() ? 1000 : Integer.parseInt(tokensStr);

        try {
            // Build request
            LLMRequest.LLMRequestBuilder requestBuilder = LLMRequest.builder()
                    .model("gpt-3.5-turbo")
                    .temperature(temperature)
                    .maxTokens(maxTokens);

            if (!systemMsg.isEmpty()) {
                requestBuilder.messages(List.of(
                        Message.system(systemMsg),
                        Message.user(userMsg)
                ));
            } else {
                requestBuilder.messages(List.of(Message.user(userMsg)));
            }

            LLMRequest request = requestBuilder.build();

            System.out.println("\\n🤖 Processing advanced request...");
            long startTime = System.currentTimeMillis();

            LLMResponse response = llmService.sendMessage(request);

            long endTime = System.currentTimeMillis();

            System.out.println("\\n✅ Response (" + (endTime - startTime) + "ms):");
            System.out.println(response.getContent());

            // Show usage stats
            if (response.getUsage() != null) {
                System.out.println("\\n📊 Usage Stats:");
                System.out.println("- Prompt tokens: " + response.getUsage().getPromptTokens());
                System.out.println("- Completion tokens: " + response.getUsage().getCompletionTokens());
                System.out.println("- Total tokens: " + response.getUsage().getTotalTokens());
            }

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            log.error("Advanced request error", e);
        }
    }

    private static void demoStreaming() {
        System.out.println("\\n🌊 Streaming Response Demo");
        System.out.println("============================");

        if (!llmService.supportsStreaming()) {
            System.out.println("❌ Current provider doesn't support streaming");
            return;
        }

        System.out.print("Enter your message: ");
        String userMessage = scanner.nextLine();

        if (userMessage.trim().isEmpty()) {
            System.out.println("❌ Empty message");
            return;
        }

        try {
            LLMRequest request = LLMRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(List.of(Message.user(userMessage)))
                    .stream(true)
                    .build();

            System.out.println("\\n🤖 AI Response (streaming):");
            System.out.println("─".repeat(50));

            llmService.streamMessage(request, new StreamingObserver() {
                @Override
                public void onChunk(LLMChunk chunk) {
                    System.out.print(chunk.getContent());
                    System.out.flush();
                }

                @Override
                public void onComplete() {
                    System.out.println("\\n─".repeat(50));
                    System.out.println("✅ Streaming completed!");
                }

                @Override
                public void onError(Throwable error) {
                    System.out.println("\\n❌ Streaming error: " + error.getMessage());
                }
            });

            // Wait for completion
            Thread.sleep(100);

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            log.error("Streaming error", e);
        }
    }

    private static void demoConversation() {
        System.out.println("\\n💬 Multi-turn Conversation Demo");
        System.out.println("=================================");
        System.out.println("Type 'exit' to end conversation\\n");

        try {
            LLMService.ConversationBuilder conversation = llmService.newConversation()
                    .addSystemMessage("You are a helpful programming assistant. Be concise and friendly.")
                    .temperature(0.8)
                    .maxTokens(500);

            while (true) {
                System.out.print("You: ");
                String userInput = scanner.nextLine().trim();

                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("👋 Conversation ended!");
                    break;
                }

                if (userInput.isEmpty()) {
                    continue;
                }

                conversation.addUserMessage(userInput);

                System.out.print("🤖 AI: ");
                LLMResponse response = conversation.send();
                System.out.println(response.getContent());
                System.out.println();
            }

            // Show conversation stats
            List<Message> messages = conversation.getMessages();
            System.out.println("\\n📊 Conversation had " + messages.size() + " messages");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            log.error("Conversation error", e);
        }
    }

    private static void showProviderInfo() {
        System.out.println("\\n🔍 Provider Information");
        System.out.println("========================");
        System.out.println("Current Provider: " + llmService.getCurrentProvider());
        System.out.println("Current Model: " + llmService.getCurrentModel());
        System.out.println("Supports Streaming: " + llmService.supportsStreaming());
        System.out.println("Supports Function Calling: " + llmService.supportsFunctionCalling());
    }

    private static void switchToGPT4() {
        System.out.println("\\n🔄 Switching to GPT-4");
        System.out.println("======================");

        try {
            String apiKey = System.getenv("OPENAI_API_KEY");
            LLMProviderConfig gpt4Config = LLMProviderConfig.builder()
                    .provider(LLMProviderConfig.Provider.OPENAI)
                    .name("OpenAI GPT-4")
                    .url("https://api.openai.com/v1/chat/completions")
                    .token(apiKey)
                    .model("gpt-4")
                    .supportsStreaming(true)
                    .supportsFunctionCalling(true)
                    .timeout(60) // GPT-4 is slower
                    .build();

            llmService.switchProvider(gpt4Config);

            System.out.println("✅ Switched to GPT-4 successfully!");
            System.out.println("⚠️  Note: GPT-4 is slower and more expensive than GPT-3.5");

        } catch (Exception e) {
            System.out.println("❌ Failed to switch to GPT-4: " + e.getMessage());
            log.error("Provider switch error", e);
        }
    }

    private static void demoFunctionCalling() {
        System.out.println("\\n🔧 Function Calling Demo");
        System.out.println("==========================");

        if (!llmService.supportsFunctionCalling()) {
            System.out.println("❌ Current provider doesn't support function calling");
            return;
        }

        try {
            // Define a simple calculator function
            FunctionDefinition calcFunction = FunctionDefinition.builder()
                    .name("calculate")
                    .description("Perform basic arithmetic operations")
                    .parameters(java.util.Map.of(
                            "type", "object",
                            "properties", java.util.Map.of(
                                    "operation", java.util.Map.of(
                                            "type", "string",
                                            "enum", List.of("add", "subtract", "multiply", "divide"),
                                            "description", "The arithmetic operation to perform"
                                    ),
                                    "a", java.util.Map.of(
                                            "type", "number",
                                            "description", "First number"
                                    ),
                                    "b", java.util.Map.of(
                                            "type", "number",
                                            "description", "Second number"
                                    )
                            ),
                            "required", List.of("operation", "a", "b")
                    ))
                    .build();

            System.out.print("Ask a math question (e.g., 'What is 15 + 27?'): ");
            String question = scanner.nextLine();

            LLMRequest request = LLMRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(List.of(Message.user(question)))
                    .build();

            System.out.println("\\n🤖 AI is analyzing your question...");

            LLMResponse response = llmService.sendWithFunctions(request, List.of(calcFunction));

            if (response.hasFunctionCall()) {
                FunctionCall call = response.getFunctionCall();
                System.out.println("\\n🔧 AI wants to call function: " + call.getName());
                System.out.println("With arguments: " + call.getArguments());

                // Mock function execution (in real app, you'd call actual function)
                String result = "42"; // Mock result
                System.out.println("\\n📊 Function result: " + result);

                System.out.println("\\n✅ Function calling demo completed!");

            } else {
                System.out.println("\\n💭 AI Response: " + response.getContent());
                System.out.println("(No function call was made)");
            }

        } catch (Exception e) {
            System.out.println("❌ Function calling error: " + e.getMessage());
            log.error("Function calling error", e);
        }
    }
}