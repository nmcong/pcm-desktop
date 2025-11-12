package com.noteflix.pcm.infrastructure.dao;

import com.noteflix.pcm.domain.chat.Conversation;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for Conversation entity
 * 
 * Handles direct SQL operations for conversations table.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class ConversationDAO implements DAO<Conversation, Long> {
    
    private static final String TABLE_NAME = "conversations";
    
    private static final String INSERT_SQL = 
        "INSERT INTO " + TABLE_NAME + " (title, user_id, preview, llm_provider, llm_model, " +
        "system_prompt, message_count, total_tokens, archived, pinned, metadata, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT * FROM " + TABLE_NAME + " ORDER BY updated_at DESC";
    
    private static final String SELECT_BY_USER_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? ORDER BY updated_at DESC";
    
    private static final String UPDATE_SQL = 
        "UPDATE " + TABLE_NAME + " SET title = ?, user_id = ?, preview = ?, " +
        "llm_provider = ?, llm_model = ?, system_prompt = ?, message_count = ?, " +
        "total_tokens = ?, archived = ?, pinned = ?, metadata = ?, updated_at = ? " +
        "WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
    
    private static final String SEARCH_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? AND (title LIKE ? OR preview LIKE ?) " +
        "ORDER BY updated_at DESC";
    
    @Override
    public Conversation create(Conversation entity, Connection connection) throws SQLException {
        entity.validate();
        
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getTitle());
            stmt.setString(2, entity.getUserId());
            stmt.setString(3, entity.getPreview());
            stmt.setString(4, entity.getLlmProvider());
            stmt.setString(5, entity.getLlmModel());
            stmt.setString(6, entity.getSystemPrompt());
            stmt.setInt(7, entity.getMessageCount());
            stmt.setInt(8, entity.getTotalTokens());
            stmt.setBoolean(9, Boolean.TRUE.equals(entity.getArchived()));
            stmt.setBoolean(10, Boolean.TRUE.equals(entity.getPinned()));
            stmt.setString(11, entity.getMetadata());
            stmt.setTimestamp(12, Timestamp.valueOf(entity.getCreatedAt()));
            stmt.setTimestamp(13, Timestamp.valueOf(entity.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating conversation failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating conversation failed, no ID obtained.");
                }
            }
        }
        
        log.debug("Created conversation with ID: {}", entity.getId());
        return entity;
    }
    
    @Override
    public Optional<Conversation> read(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Conversation> readAll(Connection connection) throws SQLException {
        List<Conversation> conversations = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                conversations.add(mapResultSet(rs));
            }
        }
        
        return conversations;
    }
    
    public List<Conversation> readByUserId(String userId, Connection connection) throws SQLException {
        List<Conversation> conversations = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_USER_SQL)) {
            stmt.setString(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    conversations.add(mapResultSet(rs));
                }
            }
        }
        
        return conversations;
    }
    
    public List<Conversation> search(String userId, String query, Connection connection) throws SQLException {
        List<Conversation> conversations = new ArrayList<>();
        String searchPattern = "%" + query + "%";
        
        try (PreparedStatement stmt = connection.prepareStatement(SEARCH_SQL)) {
            stmt.setString(1, userId);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    conversations.add(mapResultSet(rs));
                }
            }
        }
        
        return conversations;
    }
    
    @Override
    public void update(Conversation entity, Connection connection) throws SQLException {
        entity.validate();
        
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, entity.getTitle());
            stmt.setString(2, entity.getUserId());
            stmt.setString(3, entity.getPreview());
            stmt.setString(4, entity.getLlmProvider());
            stmt.setString(5, entity.getLlmModel());
            stmt.setString(6, entity.getSystemPrompt());
            stmt.setInt(7, entity.getMessageCount());
            stmt.setInt(8, entity.getTotalTokens());
            stmt.setBoolean(9, Boolean.TRUE.equals(entity.getArchived()));
            stmt.setBoolean(10, Boolean.TRUE.equals(entity.getPinned()));
            stmt.setString(11, entity.getMetadata());
            stmt.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(13, entity.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating conversation failed, no rows affected.");
            }
        }
        
        log.debug("Updated conversation ID: {}", entity.getId());
    }
    
    @Override
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_SQL)) {
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Deleting conversation failed, no rows affected.");
            }
        }
        
        log.debug("Deleted conversation ID: {}", id);
    }
    
    @Override
    public boolean exists(Long id, Connection connection) throws SQLException {
        return read(id, connection).isPresent();
    }
    
    @Override
    public long count(Connection connection) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        try (PreparedStatement stmt = connection.prepareStatement(countSql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }
    
    private Conversation mapResultSet(ResultSet rs) throws SQLException {
        return Conversation.builder()
            .id(rs.getLong("id"))
            .title(rs.getString("title"))
            .userId(rs.getString("user_id"))
            .preview(rs.getString("preview"))
            .llmProvider(rs.getString("llm_provider"))
            .llmModel(rs.getString("llm_model"))
            .systemPrompt(rs.getString("system_prompt"))
            .messageCount(rs.getInt("message_count"))
            .totalTokens(rs.getInt("total_tokens"))
            .archived(rs.getBoolean("archived"))
            .pinned(rs.getBoolean("pinned"))
            .metadata(rs.getString("metadata"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();
    }
}

