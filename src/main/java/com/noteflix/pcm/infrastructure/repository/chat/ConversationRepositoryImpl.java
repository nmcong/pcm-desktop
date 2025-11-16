package com.noteflix.pcm.infrastructure.repository.chat;

import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.infrastructure.dao.ConversationDAO;
import com.noteflix.pcm.infrastructure.dao.MessageDAO;
import com.noteflix.pcm.infrastructure.database.ConnectionManager;
import com.noteflix.pcm.infrastructure.exception.DatabaseException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ConversationRepository using SQLite
 *
 * <p>Follows Repository Pattern: - Abstracts database operations - Provides transaction management
 * - Converts SQLException to domain exceptions
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class ConversationRepositoryImpl implements ConversationRepository {

    private final ConversationDAO conversationDAO;
    private final MessageDAO messageDAO;
    private final ConnectionManager connectionManager;

    public ConversationRepositoryImpl() {
        this.conversationDAO = new ConversationDAO();
        this.messageDAO = new MessageDAO();
        this.connectionManager = ConnectionManager.INSTANCE;
    }

    // Constructor for testing with custom DAOs and connection manager
    public ConversationRepositoryImpl(ConversationDAO conversationDAO, MessageDAO messageDAO) {
        this.conversationDAO = conversationDAO;
        this.messageDAO = messageDAO;
        this.connectionManager = ConnectionManager.INSTANCE;
    }

    @Override
    public Conversation save(Conversation entity) {
        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Create conversation
                Conversation saved = conversationDAO.create(entity, conn);

                // Create messages if any
                if (entity.getMessages() != null && !entity.getMessages().isEmpty()) {
                    for (Message message : entity.getMessages()) {
                        message.setConversationId(saved.getId());
                        messageDAO.create(message, conn);
                    }
                }

                conn.commit();
                log.info("Saved conversation with ID: {}", saved.getId());
                return saved;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            log.error("Failed to save conversation", e);
            throw new DatabaseException("Failed to save conversation", e);
        }
    }

    @Override
    public Optional<Conversation> findById(Long id) {
        try (Connection conn = connectionManager.getConnection()) {
            return conversationDAO.read(id, conn);
        } catch (SQLException e) {
            log.error("Failed to find conversation by ID: {}", id, e);
            throw new DatabaseException("Failed to find conversation", e);
        }
    }

    @Override
    public List<Conversation> findAll() {
        try (Connection conn = connectionManager.getConnection()) {
            return conversationDAO.readAll(conn);
        } catch (SQLException e) {
            log.error("Failed to find all conversations", e);
            throw new DatabaseException("Failed to find conversations", e);
        }
    }

    @Override
    public void update(Conversation entity) {
        try (Connection conn = connectionManager.getConnection()) {
            conversationDAO.update(entity, conn);
            log.info("Updated conversation with ID: {}", entity.getId());
        } catch (SQLException e) {
            log.error("Failed to update conversation", e);
            throw new DatabaseException("Failed to update conversation", e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Delete messages first (cascade should handle this, but being explicit)
                messageDAO.deleteByConversationId(id, conn);

                // Delete conversation
                conversationDAO.delete(id, conn);

                conn.commit();
                log.info("Deleted conversation with ID: {}", id);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            log.error("Failed to delete conversation", e);
            throw new DatabaseException("Failed to delete conversation", e);
        }
    }

    @Override
    public boolean exists(Long id) {
        try (Connection conn = connectionManager.getConnection()) {
            return conversationDAO.exists(id, conn);
        } catch (SQLException e) {
            log.error("Failed to check if conversation exists", e);
            throw new DatabaseException("Failed to check conversation existence", e);
        }
    }

    @Override
    public long count() {
        try (Connection conn = connectionManager.getConnection()) {
            return conversationDAO.readAll(conn).size();
        } catch (SQLException e) {
            log.error("Failed to count conversations", e);
            throw new DatabaseException("Failed to count conversations", e);
        }
    }

    @Override
    public List<Conversation> findByUserId(String userId) {
        try (Connection conn = connectionManager.getConnection()) {
            return conversationDAO.readByUserId(userId, conn);
        } catch (SQLException e) {
            log.error("Failed to find conversations by user ID: {}", userId, e);
            throw new DatabaseException("Failed to find conversations by user", e);
        }
    }

    @Override
    public List<Conversation> findRecent(String userId, int limit) {
        try (Connection conn = connectionManager.getConnection()) {
            List<Conversation> all = conversationDAO.readByUserId(userId, conn);
            return all.size() > limit ? all.subList(0, limit) : all;
        } catch (SQLException e) {
            log.error("Failed to find recent conversations", e);
            throw new DatabaseException("Failed to find recent conversations", e);
        }
    }

    @Override
    public Optional<Conversation> findWithMessages(Long id) {
        try (Connection conn = connectionManager.getConnection()) {
            Optional<Conversation> conversationOpt = conversationDAO.read(id, conn);

            if (conversationOpt.isPresent()) {
                Conversation conversation = conversationOpt.get();

                // Load messages
                List<Message> messages = messageDAO.readByConversationId(id, conn);
                conversation.setMessages(messages != null ? messages : new ArrayList<>());

                return Optional.of(conversation);
            }

            return Optional.empty();
        } catch (SQLException e) {
            log.error("Failed to find conversation with messages", e);
            throw new DatabaseException("Failed to find conversation with messages", e);
        }
    }

    @Override
    public List<Conversation> search(String userId, String query) {
        try (Connection conn = connectionManager.getConnection()) {
            return conversationDAO.search(userId, query, conn);
        } catch (SQLException e) {
            log.error("Failed to search conversations", e);
            throw new DatabaseException("Failed to search conversations", e);
        }
    }

    @Override
    public List<Conversation> findPinned(String userId) {
        try (Connection conn = connectionManager.getConnection()) {
            return conversationDAO.readByUserId(userId, conn).stream()
                    .filter(c -> c.getPinned() != null && c.getPinned())
                    .toList();
        } catch (SQLException e) {
            log.error("Failed to find pinned conversations", e);
            throw new DatabaseException("Failed to find pinned conversations", e);
        }
    }

    @Override
    public List<Conversation> findArchived(String userId) {
        try (Connection conn = connectionManager.getConnection()) {
            return conversationDAO.readByUserId(userId, conn).stream()
                    .filter(c -> c.getArchived() != null && c.getArchived())
                    .toList();
        } catch (SQLException e) {
            log.error("Failed to find archived conversations", e);
            throw new DatabaseException("Failed to find archived conversations", e);
        }
    }

    @Override
    public void archive(Long id) {
        updateFlag(id, "archived", true);
    }

    @Override
    public void unarchive(Long id) {
        updateFlag(id, "archived", false);
    }

    @Override
    public void pin(Long id) {
        updateFlag(id, "pinned", true);
    }

    @Override
    public void unpin(Long id) {
        updateFlag(id, "pinned", false);
    }

    private void updateFlag(Long id, String field, boolean value) {
        try (Connection conn = connectionManager.getConnection()) {
            Optional<Conversation> convOpt = conversationDAO.read(id, conn);

            if (convOpt.isPresent()) {
                Conversation conversation = convOpt.get();

                if ("archived".equals(field)) {
                    conversation.setArchived(value);
                } else if ("pinned".equals(field)) {
                    conversation.setPinned(value);
                }

                conversationDAO.update(conversation, conn);
                log.info("Updated {} flag for conversation ID: {}", field, id);
            }
        } catch (SQLException e) {
            log.error("Failed to update {} flag for conversation", field, e);
            throw new DatabaseException("Failed to update conversation flag", e);
        }
    }

    @Override
    public long countByUserId(String userId) {
        try (Connection conn = connectionManager.getConnection()) {
            return conversationDAO.readByUserId(userId, conn).size();
        } catch (SQLException e) {
            log.error("Failed to count conversations by user", e);
            throw new DatabaseException("Failed to count conversations by user", e);
        }
    }
}
