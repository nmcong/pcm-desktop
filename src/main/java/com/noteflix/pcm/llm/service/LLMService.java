package com.noteflix.pcm.llm.service;

import com.noteflix.pcm.llm.api.FunctionCallingCapable;
import com.noteflix.pcm.llm.api.LLMClient;
import com.noteflix.pcm.llm.api.StreamingCapable;
import com.noteflix.pcm.llm.factory.LLMClientFactory;
import com.noteflix.pcm.llm.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * High-level service for LLM operations
 * <p>
 * Provides:
 * - Simple chat interface
 * - Conversation management
 * - Provider switching
 * - Error handling
 * - Logging and metrics
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class LLMService {

    private final LLMClientFactory factory;
    private LLMProviderConfig currentConfig;
    private LLMClient currentClient;

    public LLMService() {
        this.factory = LLMClientFactory.getInstance();
        log.info("Initialized LLMService");
    }

    /**
     * Initialize with a specific provider configuration
     */
    public void initialize(LLMProviderConfig config) {
        this.currentConfig = config;
        this.currentClient = factory.getClient(config);
        log.info("LLMService initialized with provider: {}", config.getProvider());
    }

    /**
     * Switch to a different provider
     */
    public void switchProvider(LLMProviderConfig config) {
        log.info("Switching from {} to {}",
                currentConfig != null ? currentConfig.getProvider() : "none",
                config.getProvider());
        initialize(config);
    }

    /**
     * Send a simple text message and get response
     */
    public String chat(String userMessage) {
        if (currentClient == null) {
            throw new IllegalStateException("LLMService not initialized. Call initialize() first.");
        }

        LLMRequest request = LLMRequest.builder()
                .model(currentConfig.getModel())
                .messages(List.of(Message.user(userMessage)))
                .temperature(0.7)
                .maxTokens(1000)
                .build();

        LLMResponse response = currentClient.sendMessage(request);
        return response.getContent();
    }

    /**
     * Send a message with full configuration
     */
    public LLMResponse sendMessage(LLMRequest request) {
        if (currentClient == null) {
            throw new IllegalStateException("LLMService not initialized. Call initialize() first.");
        }

        return currentClient.sendMessage(request);
    }

    /**
     * Stream a message (if supported)
     */
    public Stream<LLMChunk> streamMessage(LLMRequest request) {
        if (currentClient == null) {
            throw new IllegalStateException("LLMService not initialized. Call initialize() first.");
        }

        if (currentClient instanceof StreamingCapable) {
            return ((StreamingCapable) currentClient).streamMessage(request);
        } else {
            log.warn("Current provider does not support streaming, falling back to regular response");
            LLMResponse response = currentClient.sendMessage(request);

            LLMChunk chunk = LLMChunk.builder()
                    .id(response.getId())
                    .model(response.getModel())
                    .content(response.getContent())
                    .finishReason(response.getFinishReason())
                    .build();

            return Stream.of(chunk);
        }
    }

    /**
     * Stream message with observer pattern
     */
    public void streamMessage(LLMRequest request, StreamingObserver observer) {
        if (currentClient == null) {
            throw new IllegalStateException("LLMService not initialized. Call initialize() first.");
        }

        if (currentClient instanceof StreamingCapable) {
            ((StreamingCapable) currentClient).streamMessage(request, observer);
        } else {
            log.warn("Current provider does not support streaming");
            observer.onError(new UnsupportedOperationException("Streaming not supported"));
        }
    }

    /**
     * Send message with function calling (if supported)
     */
    public LLMResponse sendWithFunctions(LLMRequest request, List<FunctionDefinition> functions) {
        if (currentClient == null) {
            throw new IllegalStateException("LLMService not initialized. Call initialize() first.");
        }

        if (currentClient instanceof FunctionCallingCapable) {
            return ((FunctionCallingCapable) currentClient).sendWithFunctions(request, functions);
        } else {
            log.warn("Current provider does not support function calling, sending regular message");
            return currentClient.sendMessage(request);
        }
    }

    /**
     * Create a conversation builder for multi-turn conversations
     */
    public ConversationBuilder newConversation() {
        return new ConversationBuilder(this);
    }

    /**
     * Check if current provider supports streaming
     */
    public boolean supportsStreaming() {
        return currentClient instanceof StreamingCapable;
    }

    /**
     * Check if current provider supports function calling
     */
    public boolean supportsFunctionCalling() {
        return currentClient instanceof FunctionCallingCapable
                && ((FunctionCallingCapable) currentClient).supportsFunctionCalling();
    }

    /**
     * Get current provider name
     */
    public String getCurrentProvider() {
        return currentClient != null ? currentClient.getProviderName() : "none";
    }

    /**
     * Get current model
     */
    public String getCurrentModel() {
        return currentClient != null ? currentClient.getModel() : "none";
    }

    /**
     * Helper class for building multi-turn conversations
     */
    public static class ConversationBuilder {
        private final LLMService service;
        private final List<Message> messages;
        private Double temperature;
        private Integer maxTokens;

        ConversationBuilder(LLMService service) {
            this.service = service;
            this.messages = new ArrayList<>();
        }

        public ConversationBuilder addSystemMessage(String content) {
            messages.add(Message.system(content));
            return this;
        }

        public ConversationBuilder addUserMessage(String content) {
            messages.add(Message.user(content));
            return this;
        }

        public ConversationBuilder addAssistantMessage(String content) {
            messages.add(Message.assistant(content));
            return this;
        }

        public ConversationBuilder temperature(double temp) {
            this.temperature = temp;
            return this;
        }

        public ConversationBuilder maxTokens(int tokens) {
            this.maxTokens = tokens;
            return this;
        }

        public LLMResponse send() {
            LLMRequest request = LLMRequest.builder()
                    .model(service.currentConfig.getModel())
                    .messages(messages)
                    .temperature(temperature != null ? temperature : 0.7)
                    .maxTokens(maxTokens != null ? maxTokens : 1000)
                    .build();

            LLMResponse response = service.sendMessage(request);

            // Add assistant's response to conversation history
            messages.add(Message.assistant(response.getContent()));

            return response;
        }

        public List<Message> getMessages() {
            return new ArrayList<>(messages);
        }
    }
}

