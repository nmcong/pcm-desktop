package com.noteflix.pcm.domain.chat;

import com.noteflix.pcm.domain.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Message entity in a conversation
 * <p>
 * Represents a single message from user or AI assistant.
 * Follows:
 * - Single Responsibility Principle (only message data)
 * - Builder Pattern for construction
 * - Immutable after creation (use setters carefully)
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class Message extends BaseEntity {

    /**
     * Message ID (primary key)
     */
    private Long id;

    /**
     * Conversation ID (foreign key)
     */
    private Long conversationId;

    /**
     * Role of message sender
     */
    private MessageRole role;

    /**
     * Message content (text)
     */
    private String content;

    /**
     * Function name (if role is FUNCTION)
     */
    private String functionName;

    /**
     * Function arguments (JSON, if role is FUNCTION)
     */
    private String functionArguments;

    /**
     * Timestamp when message was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when message was updated
     */
    private LocalDateTime updatedAt;

    /**
     * Token count (for AI messages)
     */
    private Integer tokenCount;

    /**
     * Metadata (JSON)
     */
    private String metadata;

    /**
     * Helper method to create user message
     */
    public static Message user(Long conversationId, String content) {
        return Message.builder()
                .conversationId(conversationId)
                .role(MessageRole.USER)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Helper method to create assistant message
     */
    public static Message assistant(Long conversationId, String content) {
        return Message.builder()
                .conversationId(conversationId)
                .role(MessageRole.ASSISTANT)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Helper method to create system message
     */
    public static Message system(Long conversationId, String content) {
        return Message.builder()
                .conversationId(conversationId)
                .role(MessageRole.SYSTEM)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Check if message is from AI
     */
    public boolean isFromAI() {
        return role == MessageRole.ASSISTANT;
    }

    /**
     * Check if message is from user
     */
    public boolean isFromUser() {
        return role == MessageRole.USER;
    }

    /**
     * Check if message is system message
     */
    public boolean isSystem() {
        return role == MessageRole.SYSTEM;
    }

    /**
     * Check if message is function result
     */
    public boolean isFunction() {
        return role == MessageRole.FUNCTION;
    }

    /**
     * Validate message
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation ID is required");
        }

        if (role == null) {
            throw new IllegalArgumentException("Message role is required");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content is required");
        }

        if (role == MessageRole.FUNCTION) {
            if (functionName == null || functionName.trim().isEmpty()) {
                throw new IllegalArgumentException("Function name is required for FUNCTION role");
            }
        }
    }
}

