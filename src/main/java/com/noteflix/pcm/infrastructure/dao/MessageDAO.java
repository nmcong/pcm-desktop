package com.noteflix.pcm.infrastructure.dao;

import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.domain.chat.MessageRole;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for Message entity
 * 
 * Handles direct SQL operations for messages table.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class MessageDAO implements DAO<Message, Long> {
    
    private static final String TABLE_NAME = "messages";
    
    private static final String INSERT_SQL = 
        "INSERT INTO " + TABLE_NAME + " (conversation_id, role, content, function_name, " +
        "function_arguments, token_count, metadata, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at ASC";
    
    private static final String SELECT_BY_CONVERSATION_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE conversation_id = ? ORDER BY created_at ASC";
    
    private static final String UPDATE_SQL = 
        "UPDATE " + TABLE_NAME + " SET conversation_id = ?, role = ?, content = ?, " +
        "function_name = ?, function_arguments = ?, token_count = ?, metadata = ?, updated_at = ? " +
        "WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
    
    private static final String DELETE_BY_CONVERSATION_SQL = 
        "DELETE FROM " + TABLE_NAME + " WHERE conversation_id = ?";
    
    @Override
    public Message create(Message entity, Connection connection) throws SQLException {
        entity.validate();
        
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, entity.getConversationId());
            stmt.setString(2, entity.getRole().name());
            stmt.setString(3, entity.getContent());
            stmt.setString(4, entity.getFunctionName());
            stmt.setString(5, entity.getFunctionArguments());
            
            if (entity.getTokenCount() != null) {
                stmt.setInt(6, entity.getTokenCount());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setString(7, entity.getMetadata());
            stmt.setTimestamp(8, Timestamp.valueOf(entity.getCreatedAt()));
            stmt.setTimestamp(9, Timestamp.valueOf(entity.getUpdatedAt() != null 
                ? entity.getUpdatedAt() : entity.getCreatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating message failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating message failed, no ID obtained.");
                }
            }
        }
        
        log.debug("Created message with ID: {}", entity.getId());
        return entity;
    }
    
    @Override
    public Optional<Message> read(Long id, Connection connection) throws SQLException {
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
    public List<Message> readAll(Connection connection) throws SQLException {
        List<Message> messages = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                messages.add(mapResultSet(rs));
            }
        }
        
        return messages;
    }
    
    public List<Message> readByConversationId(Long conversationId, Connection connection) throws SQLException {
        List<Message> messages = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_CONVERSATION_SQL)) {
            stmt.setLong(1, conversationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSet(rs));
                }
            }
        }
        
        return messages;
    }
    
    @Override
    public void update(Message entity, Connection connection) throws SQLException {
        entity.validate();
        
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
            stmt.setLong(1, entity.getConversationId());
            stmt.setString(2, entity.getRole().name());
            stmt.setString(3, entity.getContent());
            stmt.setString(4, entity.getFunctionName());
            stmt.setString(5, entity.getFunctionArguments());
            
            if (entity.getTokenCount() != null) {
                stmt.setInt(6, entity.getTokenCount());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setString(7, entity.getMetadata());
            stmt.setTimestamp(8, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmt.setLong(9, entity.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating message failed, no rows affected.");
            }
        }
        
        log.debug("Updated message ID: {}", entity.getId());
    }
    
    @Override
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_SQL)) {
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Deleting message failed, no rows affected.");
            }
        }
        
        log.debug("Deleted message ID: {}", id);
    }
    
    public void deleteByConversationId(Long conversationId, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_BY_CONVERSATION_SQL)) {
            stmt.setLong(1, conversationId);
            stmt.executeUpdate();
        }
        
        log.debug("Deleted all messages for conversation ID: {}", conversationId);
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
    
    private Message mapResultSet(ResultSet rs) throws SQLException {
        return Message.builder()
            .id(rs.getLong("id"))
            .conversationId(rs.getLong("conversation_id"))
            .role(MessageRole.valueOf(rs.getString("role")))
            .content(rs.getString("content"))
            .functionName(rs.getString("function_name"))
            .functionArguments(rs.getString("function_arguments"))
            .tokenCount(rs.getObject("token_count") != null ? rs.getInt("token_count") : null)
            .metadata(rs.getString("metadata"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();
    }
}

