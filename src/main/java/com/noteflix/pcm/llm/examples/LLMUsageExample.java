package com.noteflix.pcm.llm.examples;

import com.noteflix.pcm.llm.model.*;
import com.noteflix.pcm.llm.service.LLMService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Examples of using the LLM integration
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class LLMUsageExample {
    
    public static void main(String[] args) {
        // Example 1: Basic Chat
        basicChatExample();
        
        // Example 2: Multi-turn Conversation
        // conversationExample();
        
        // Example 3: Streaming Response
        // streamingExample();
        
        // Example 4: Function Calling
        // functionCallingExample();
    }
    
    /**
     * Example 1: Simple chat with OpenAI
     */
    public static void basicChatExample() {
        log.info("=== Example 1: Basic Chat ===");
        
        // Configure OpenAI provider
        LLMProviderConfig config = LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .url("https://api.openai.com/v1/chat/completions")
            .token(System.getenv("OPENAI_API_KEY")) // Get from environment variable
            .model("gpt-3.5-turbo")
            .timeout(30)
            .build();
        
        // Initialize service
        LLMService service = new LLMService();
        service.initialize(config);
        
        // Send a simple message
        try {
            String response = service.chat("Hello! Can you help me with Java programming?");
            log.info("Response: {}", response);
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }
    
    /**
     * Example 2: Multi-turn conversation
     */
    public static void conversationExample() {
        log.info("=== Example 2: Multi-turn Conversation ===");
        
        LLMProviderConfig config = LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .url("https://api.openai.com/v1/chat/completions")
            .token(System.getenv("OPENAI_API_KEY"))
            .model("gpt-4")
            .build();
        
        LLMService service = new LLMService();
        service.initialize(config);
        
        // Build a conversation
        LLMService.ConversationBuilder conversation = service.newConversation()
            .addSystemMessage("You are a helpful Java programming assistant.")
            .addUserMessage("How do I create a singleton in Java?")
            .temperature(0.7)
            .maxTokens(500);
        
        // Send and get response
        LLMResponse response1 = conversation.send();
        log.info("Assistant: {}", response1.getContent());
        
        // Continue conversation
        conversation.addUserMessage("Can you show me an example with thread-safety?");
        LLMResponse response2 = conversation.send();
        log.info("Assistant: {}", response2.getContent());
    }
    
    /**
     * Example 3: Streaming response
     */
    public static void streamingExample() {
        log.info("=== Example 3: Streaming Response ===");
        
        LLMProviderConfig config = LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .url("https://api.openai.com/v1/chat/completions")
            .token(System.getenv("OPENAI_API_KEY"))
            .model("gpt-3.5-turbo")
            .build();
        
        LLMService service = new LLMService();
        service.initialize(config);
        
        LLMRequest request = LLMRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(List.of(Message.user("Tell me a short story about Java programming")))
            .stream(true)
            .build();
        
        // Stream with observer
        service.streamMessage(request, new StreamingObserver() {
            @Override
            public void onChunk(LLMChunk chunk) {
                System.out.print(chunk.getContent());
            }
            
            @Override
            public void onComplete() {
                System.out.println("\n[Stream completed]");
            }
            
            @Override
            public void onError(Throwable error) {
                log.error("Stream error: {}", error.getMessage());
            }
        });
    }
    
    /**
     * Example 4: Function calling
     */
    public static void functionCallingExample() {
        log.info("=== Example 4: Function Calling ===");
        
        LLMProviderConfig config = LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .url("https://api.openai.com/v1/chat/completions")
            .token(System.getenv("OPENAI_API_KEY"))
            .model("gpt-3.5-turbo")
            .build();
        
        LLMService service = new LLMService();
        service.initialize(config);
        
        // Define a function
        FunctionDefinition weatherFunction = FunctionDefinition.builder()
            .name("get_weather")
            .description("Get the current weather in a given location")
            .parameters(java.util.Map.of(
                "type", "object",
                "properties", java.util.Map.of(
                    "location", java.util.Map.of(
                        "type", "string",
                        "description", "The city and state, e.g. San Francisco, CA"
                    ),
                    "unit", java.util.Map.of(
                        "type", "string",
                        "description", "The unit of temperature (celsius or fahrenheit)"
                    )
                ),
                "required", List.of("location")
            ))
            .build();
        
        LLMRequest request = LLMRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(List.of(Message.user("What's the weather like in Tokyo?")))
            .build();
        
        // Send with functions
        LLMResponse response = service.sendWithFunctions(request, List.of(weatherFunction));
        
        if (response.hasFunctionCall()) {
            FunctionCall call = response.getFunctionCall();
            log.info("Function called: {}", call.getName());
            log.info("Arguments: {}", call.getArguments());
        } else {
            log.info("Response: {}", response.getContent());
        }
    }
    
    /**
     * Example 5: Switch between providers
     */
    public static void providerSwitchingExample() {
        log.info("=== Example 5: Provider Switching ===");
        
        LLMService service = new LLMService();
        
        // Start with OpenAI
        LLMProviderConfig openaiConfig = LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .url("https://api.openai.com/v1/chat/completions")
            .token(System.getenv("OPENAI_API_KEY"))
            .model("gpt-3.5-turbo")
            .build();
        
        service.initialize(openaiConfig);
        String response1 = service.chat("Hello!");
        log.info("OpenAI says: {}", response1);
        
        // Switch to local Ollama (when implemented)
        /*
        LLMProviderConfig ollamaConfig = LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OLLAMA)
            .url("http://localhost:11434/api/chat")
            .model("llama2")
            .build();
        
        service.switchProvider(ollamaConfig);
        String response2 = service.chat("Hello!");
        log.info("Ollama says: {}", response2);
        */
    }
}

