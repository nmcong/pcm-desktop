package com.noteflix.pcm.application.service.chat;

import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.infrastructure.repository.chat.ConversationRepository;
import com.noteflix.pcm.infrastructure.repository.chat.ConversationRepositoryImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing conversations
 * <p>
 * Business logic layer for chat conversations.
 * Orchestrates operations between UI and repository.
 * <p>
 * Follows:
 * - Single Responsibility Principle (only conversation business logic)
 * - Dependency Inversion Principle (depends on repository interface)
 * - Service Layer Pattern
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class ConversationService {

    private final ConversationRepository repository;
    private final AIService aiService;

    /**
     * Constructor with dependency injection
     */
    public ConversationService(ConversationRepository repository, AIService aiService) {
        this.repository = repository;
        this.aiService = aiService;
        log.info("ConversationService initialized");
    }

    /**
     * Default constructor (for convenience)
     */
    public ConversationService() {
        this(new ConversationRepositoryImpl(), new AIService());
    }

    /**
     * Create a new conversation
     *
     * @param title       Conversation title
     * @param userId      User ID
     * @param llmProvider LLM provider (openai, anthropic, ollama)
     * @param llmModel    LLM model
     * @return Created conversation
     */
    public Conversation createConversation(String title, String userId, String llmProvider, String llmModel) {
        log.info("Creating new conversation: title={}, user={}, provider={}, model={}",
                title, userId, llmProvider, llmModel);

        Conversation conversation = Conversation.create(title, userId, llmProvider, llmModel);
        conversation.validate();

        Conversation saved = repository.save(conversation);

        log.info("Created conversation with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Create conversation with system prompt
     */
    public Conversation createConversationWithSystemPrompt(String title, String userId,
                                                           String llmProvider, String llmModel, String systemPrompt) {

        Conversation conversation = createConversation(title, userId, llmProvider, llmModel);

        // Add system message
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            Message systemMessage = Message.system(conversation.getId(), systemPrompt);
            addMessageToConversation(conversation.getId(), systemMessage);
        }

        return conversation;
    }

    /**
     * Get conversation by ID
     */
    public Optional<Conversation> getConversation(Long id) {
        return repository.findById(id);
    }

    /**
     * Get conversation with all messages loaded
     */
    public Optional<Conversation> getConversationWithMessages(Long id) {
        return repository.findWithMessages(id);
    }

    /**
     * Get all conversations for a user
     */
    public List<Conversation> getUserConversations(String userId) {
        return repository.findByUserId(userId);
    }

    /**
     * Get recent conversations
     */
    public List<Conversation> getRecentConversations(String userId, int limit) {
        return repository.findRecent(userId, limit);
    }

    /**
     * Search conversations
     */
    public List<Conversation> searchConversations(String userId, String query) {
        return repository.search(userId, query);
    }

    /**
     * Send a message and get AI response
     *
     * @param conversationId Conversation ID
     * @param userMessage    User message content
     * @return AI response message
     */
    public Message sendMessage(Long conversationId, String userMessage) {
        log.info("Sending message to conversation: {}", conversationId);

        // Get conversation with messages
        Optional<Conversation> convOpt = repository.findWithMessages(conversationId);

        if (convOpt.isEmpty()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = convOpt.get();

        // Create and add user message
        Message userMsg = Message.user(conversationId, userMessage);
        userMsg.setCreatedAt(LocalDateTime.now());

        conversation.addMessage(userMsg);
        repository.update(conversation);

        // Generate AI response
        Message aiResponse = aiService.generateResponse(conversation, userMessage);
        aiResponse.setConversationId(conversationId);

        conversation.addMessage(aiResponse);
        repository.update(conversation);

        log.info("Message exchange complete for conversation: {}", conversationId);
        return aiResponse;
    }

    /**
     * Send message asynchronously
     */
    public CompletableFuture<Message> sendMessageAsync(Long conversationId, String userMessage) {
        return CompletableFuture.supplyAsync(() -> sendMessage(conversationId, userMessage));
    }

    /**
     * Add a message to conversation (without AI response)
     */
    public void addMessageToConversation(Long conversationId, Message message) {
        Optional<Conversation> convOpt = repository.findWithMessages(conversationId);

        if (convOpt.isEmpty()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = convOpt.get();
        conversation.addMessage(message);
        repository.update(conversation);

        log.debug("Added message to conversation: {}", conversationId);
    }

    /**
     * Update conversation title
     */
    public void updateTitle(Long conversationId, String newTitle) {
        Optional<Conversation> convOpt = repository.findById(conversationId);

        if (convOpt.isPresent()) {
            Conversation conversation = convOpt.get();
            conversation.setTitle(newTitle);
            conversation.setUpdatedAt(LocalDateTime.now());
            repository.update(conversation);

            log.info("Updated title for conversation: {}", conversationId);
        }
    }

    /**
     * Delete conversation
     */
    public void deleteConversation(Long conversationId) {
        repository.delete(conversationId);
        log.info("Deleted conversation: {}", conversationId);
    }

    /**
     * Archive conversation
     */
    public void archiveConversation(Long conversationId) {
        repository.archive(conversationId);
        log.info("Archived conversation: {}", conversationId);
    }

    /**
     * Unarchive conversation
     */
    public void unarchiveConversation(Long conversationId) {
        repository.unarchive(conversationId);
        log.info("Unarchived conversation: {}", conversationId);
    }

    /**
     * Pin conversation
     */
    public void pinConversation(Long conversationId) {
        repository.pin(conversationId);
        log.info("Pinned conversation: {}", conversationId);
    }

    /**
     * Unpin conversation
     */
    public void unpinConversation(Long conversationId) {
        repository.unpin(conversationId);
        log.info("Unpinned conversation: {}", conversationId);
    }

    /**
     * Get pinned conversations
     */
    public List<Conversation> getPinnedConversations(String userId) {
        return repository.findPinned(userId);
    }

    /**
     * Get archived conversations
     */
    public List<Conversation> getArchivedConversations(String userId) {
        return repository.findArchived(userId);
    }

    /**
     * Clear all messages in conversation
     */
    public void clearConversation(Long conversationId) {
        Optional<Conversation> convOpt = repository.findWithMessages(conversationId);

        if (convOpt.isPresent()) {
            Conversation conversation = convOpt.get();
            conversation.getMessages().clear();
            conversation.setMessageCount(0);
            conversation.setTotalTokens(0);
            conversation.setUpdatedAt(LocalDateTime.now());
            repository.update(conversation);

            log.info("Cleared conversation: {}", conversationId);
        }
    }

    /**
     * Get conversation count for user
     */
    public long getConversationCount(String userId) {
        return repository.countByUserId(userId);
    }
}

