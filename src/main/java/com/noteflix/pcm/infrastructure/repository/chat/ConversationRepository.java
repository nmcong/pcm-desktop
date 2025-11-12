package com.noteflix.pcm.infrastructure.repository.chat;

import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Conversation entity
 * <p>
 * Follows Repository Pattern:
 * - Abstracts data access from business logic
 * - Provides domain-oriented query methods
 * - Independent of storage technology
 *
 * @author PCM Team
 * @version 1.0.0
 */
public interface ConversationRepository extends Repository<Conversation, Long> {

    /**
     * Find all conversations for a specific user
     *
     * @param userId User ID
     * @return List of conversations ordered by updated_at DESC
     */
    List<Conversation> findByUserId(String userId);

    /**
     * Find recent conversations for a user
     *
     * @param userId User ID
     * @param limit  Maximum number of conversations to return
     * @return List of recent conversations
     */
    List<Conversation> findRecent(String userId, int limit);

    /**
     * Find conversation with all its messages loaded
     *
     * @param id Conversation ID
     * @return Optional containing conversation with messages
     */
    Optional<Conversation> findWithMessages(Long id);

    /**
     * Search conversations by query
     *
     * @param userId User ID
     * @param query  Search query (searches in title and preview)
     * @return List of matching conversations
     */
    List<Conversation> search(String userId, String query);

    /**
     * Find pinned conversations
     *
     * @param userId User ID
     * @return List of pinned conversations
     */
    List<Conversation> findPinned(String userId);

    /**
     * Find archived conversations
     *
     * @param userId User ID
     * @return List of archived conversations
     */
    List<Conversation> findArchived(String userId);

    /**
     * Archive a conversation
     *
     * @param id Conversation ID
     */
    void archive(Long id);

    /**
     * Unarchive a conversation
     *
     * @param id Conversation ID
     */
    void unarchive(Long id);

    /**
     * Pin a conversation
     *
     * @param id Conversation ID
     */
    void pin(Long id);

    /**
     * Unpin a conversation
     *
     * @param id Conversation ID
     */
    void unpin(Long id);

    /**
     * Get total conversation count for a user
     *
     * @param userId User ID
     * @return Total count
     */
    long countByUserId(String userId);
}

