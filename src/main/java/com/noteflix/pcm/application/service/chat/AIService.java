package com.noteflix.pcm.application.service.chat;

import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.domain.chat.MessageRole;
import com.noteflix.pcm.llm.model.LLMProviderConfig;
import com.noteflix.pcm.llm.model.LLMRequest;
import com.noteflix.pcm.llm.model.LLMResponse;
import com.noteflix.pcm.llm.model.StreamingObserver;
import com.noteflix.pcm.llm.service.LLMService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for AI integration
 * <p>
 * Integrates with LLMService to generate AI responses.
 * Handles conversion between domain models and LLM models.
 * <p>
 * Follows:
 * - Single Responsibility Principle (only AI integration)
 * - Adapter Pattern (adapts domain models to LLM models)
 * - Strategy Pattern (different LLM providers)
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class AIService {

    private final LLMService llmService;

    /**
     * Constructor with dependency injection
     */
    public AIService(LLMService llmService) {
        this.llmService = llmService;
        log.info("AIService initialized");
    }

    /**
     * Default constructor
     */
    public AIService() {
        this.llmService = new LLMService();
        log.info("AIService initialized with default LLMService");
    }

    /**
     * Generate AI response for a user message
     *
     * @param conversation Current conversation
     * @param userMessage  User message content
     * @return AI response message
     */
    public Message generateResponse(Conversation conversation, String userMessage) {
        log.info("Generating AI response for conversation: {}", conversation.getId());

        try {
            // Initialize LLM provider if not already done
            initializeLLMProvider(conversation);

            // Convert conversation messages to LLM format
            List<com.noteflix.pcm.llm.model.Message> llmMessages = convertToLLMMessages(conversation);

            // Add current user message
            llmMessages.add(com.noteflix.pcm.llm.model.Message.user(userMessage));

            // Build LLM request
            LLMRequest request = LLMRequest.builder()
                    .model(conversation.getLlmModel())
                    .messages(llmMessages)
                    .temperature(0.7)
                    .maxTokens(2000)
                    .build();

            // Get response from LLM
            LLMResponse response = llmService.sendMessage(request);

            // Convert to domain message
            Message aiMessage = Message.builder()
                    .conversationId(conversation.getId())
                    .role(MessageRole.ASSISTANT)
                    .content(response.getContent())
                    .tokenCount(response.getUsage() != null ? response.getUsage().getTotalTokens() : null)
                    .createdAt(LocalDateTime.now())
                    .build();

            log.info("Generated AI response for conversation: {}", conversation.getId());
            return aiMessage;

        } catch (Exception e) {
            log.error("Failed to generate AI response", e);

            // Return error message
            return Message.builder()
                    .conversationId(conversation.getId())
                    .role(MessageRole.ASSISTANT)
                    .content("I apologize, but I encountered an error while processing your request: " + e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Stream AI response with observer
     *
     * @param conversation Current conversation
     * @param userMessage  User message content
     * @param observer     Observer for streaming chunks
     */
    public void streamResponse(Conversation conversation, String userMessage, StreamingObserver observer) {
        log.info("Streaming AI response for conversation: {}", conversation.getId());

        try {
            // Initialize LLM provider
            initializeLLMProvider(conversation);

            // Convert messages
            List<com.noteflix.pcm.llm.model.Message> llmMessages = convertToLLMMessages(conversation);
            llmMessages.add(com.noteflix.pcm.llm.model.Message.user(userMessage));

            // Build request with streaming
            LLMRequest request = LLMRequest.builder()
                    .model(conversation.getLlmModel())
                    .messages(llmMessages)
                    .temperature(0.7)
                    .maxTokens(2000)
                    .stream(true)
                    .build();

            // Stream response
            llmService.streamMessage(request, observer);

        } catch (Exception e) {
            log.error("Failed to stream AI response", e);
            observer.onError(e);
        }
    }

    /**
     * Initialize LLM provider based on conversation settings
     */
    private void initializeLLMProvider(Conversation conversation) {
        // Get provider from conversation
        String provider = conversation.getLlmProvider();
        String model = conversation.getLlmModel();

        // Map to LLMProviderConfig.Provider enum
        LLMProviderConfig.Provider providerEnum;

        switch (provider.toLowerCase()) {
            case "openai":
                providerEnum = LLMProviderConfig.Provider.OPENAI;
                break;
            case "anthropic":
                providerEnum = LLMProviderConfig.Provider.ANTHROPIC;
                break;
            case "ollama":
                providerEnum = LLMProviderConfig.Provider.OLLAMA;
                break;
            default:
                providerEnum = LLMProviderConfig.Provider.OPENAI; // Default
        }

        // Build config
        LLMProviderConfig config = LLMProviderConfig.builder()
                .provider(providerEnum)
                .url(getProviderUrl(providerEnum))
                .token(getProviderToken(providerEnum))
                .model(model)
                .timeout(30)
                .build();

        // Initialize service
        llmService.initialize(config);

        log.debug("Initialized LLM provider: {} with model: {}", provider, model);
    }

    /**
     * Get provider URL
     */
    private String getProviderUrl(LLMProviderConfig.Provider provider) {
        switch (provider) {
            case OPENAI:
                return "https://api.openai.com/v1/chat/completions";
            case ANTHROPIC:
                return "https://api.anthropic.com/v1/messages";
            case OLLAMA:
                return "http://localhost:11434/api/chat";
            default:
                return "https://api.openai.com/v1/chat/completions";
        }
    }

    /**
     * Get provider token from environment
     */
    private String getProviderToken(LLMProviderConfig.Provider provider) {
        switch (provider) {
            case OPENAI:
                return System.getenv("OPENAI_API_KEY");
            case ANTHROPIC:
                return System.getenv("ANTHROPIC_API_KEY");
            case OLLAMA:
                return null; // Ollama doesn't need token
            default:
                return null;
        }
    }

    /**
     * Convert domain messages to LLM messages
     */
    private List<com.noteflix.pcm.llm.model.Message> convertToLLMMessages(Conversation conversation) {
        List<com.noteflix.pcm.llm.model.Message> llmMessages = new ArrayList<>();

        // Add system prompt if exists
        if (conversation.getSystemPrompt() != null && !conversation.getSystemPrompt().trim().isEmpty()) {
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
                        role = com.noteflix.pcm.llm.model.Message.Role.FUNCTION;
                        break;
                    default:
                        role = com.noteflix.pcm.llm.model.Message.Role.USER;
                }

                llmMessages.add(com.noteflix.pcm.llm.model.Message.builder()
                        .role(role)
                        .content(msg.getContent())
                        .build());
            }
        }

        return llmMessages;
    }

    /**
     * Get current LLM provider name
     */
    public String getCurrentProvider() {
        return llmService.getCurrentProvider();
    }

    /**
     * Get current LLM model
     */
    public String getCurrentModel() {
        return llmService.getCurrentModel();
    }

    /**
     * Check if streaming is supported
     */
    public boolean supportsStreaming() {
        return llmService.supportsStreaming();
    }
}

