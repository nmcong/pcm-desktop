package com.noteflix.pcm.domain.chat;

import com.noteflix.pcm.domain.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Conversation entity (chat session)
 *
 * <p>Represents a chat conversation between user and AI assistant. Contains multiple messages.
 *
 * <p>Follows: - Single Responsibility Principle (only conversation data) - Builder Pattern for
 * construction - Aggregate Root in DDD (manages Messages)
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class Conversation extends BaseEntity {

    /**
     * Conversation ID (primary key)
     */
    private Long id;

    /**
     * Conversation title
     */
    private String title;

    /**
     * User ID (owner of conversation)
     */
    private String userId;

    /**
     * Preview text (first message or summary)
     */
    private String preview;

    /**
     * LLM provider used (openai, anthropic, ollama, etc.)
     */
    private String llmProvider;

    /**
     * LLM model used (gpt-3.5-turbo, claude-3-5-sonnet, etc.)
     */
    private String llmModel;

    /**
     * System prompt (if any)
     */
    private String systemPrompt;

    /**
     * Timestamp when conversation was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when conversation was last updated
     */
    private LocalDateTime updatedAt;

    /**
     * Messages in this conversation (lazy loaded)
     */
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    /**
     * Total message count
     */
    private Integer messageCount;

    /**
     * Total token count (for cost tracking)
     */
    private Integer totalTokens;

    /**
     * Is conversation archived
     */
    @Builder.Default
    private Boolean archived = false;

    /**
     * Is conversation pinned
     */
    @Builder.Default
    private Boolean pinned = false;

    /**
     * Metadata (JSON)
     */
    private String metadata;

    /**
     * Helper method to create new conversation
     */
    public static Conversation create(
            String title, String userId, String llmProvider, String llmModel) {
        return Conversation.builder()
                .title(title)
                .userId(userId)
                .llmProvider(llmProvider)
                .llmModel(llmModel)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .messageCount(0)
                .totalTokens(0)
                .archived(false)
                .pinned(false)
                .messages(new ArrayList<>())
                .build();
    }

    /**
     * Add message to conversation
     *
     * @param message Message to add
     */
    public void addMessage(Message message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }

        message.setConversationId(this.id);
        this.messages.add(message);
        this.updatedAt = LocalDateTime.now();

        // Update counts
        if (this.messageCount == null) {
            this.messageCount = 1;
        } else {
            this.messageCount++;
        }

        // Update token count
        if (message.getTokenCount() != null) {
            if (this.totalTokens == null) {
                this.totalTokens = message.getTokenCount();
            } else {
                this.totalTokens += message.getTokenCount();
            }
        }

        // Update preview (from first user message)
        if (this.preview == null && message.isFromUser()) {
            this.preview =
                    message.getContent().length() > 100
                            ? message.getContent().substring(0, 97) + "..."
                            : message.getContent();
        }
    }

    /**
     * Get last message in conversation
     */
    public Message getLastMessage() {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }

    /**
     * Check if conversation is empty (no messages)
     */
    public boolean isEmpty() {
        return messages == null || messages.isEmpty();
    }

    /**
     * Get message count
     */
    public int getMessageCount() {
        if (messageCount != null) {
            return messageCount;
        }
        return messages != null ? messages.size() : 0;
    }

    /**
     * Validate conversation
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Conversation title is required");
        }

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (llmProvider == null || llmProvider.trim().isEmpty()) {
            throw new IllegalArgumentException("LLM provider is required");
        }

        if (llmModel == null || llmModel.trim().isEmpty()) {
            throw new IllegalArgumentException("LLM model is required");
        }
    }
}
