-- ============================================
-- Chat/Conversation Tables Migration
-- Version: 2.0
-- Author: PCM Team
-- Description: Adds chat conversation and message tables
-- ============================================

-- ============================================
-- Table: conversations
-- Stores chat conversation metadata
-- ============================================
CREATE TABLE IF NOT EXISTS conversations
(
    id
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT,
    title
    TEXT
    NOT
    NULL,
    user_id
    TEXT
    NOT
    NULL,
    preview
    TEXT,
    llm_provider
    TEXT
    NOT
    NULL
    CHECK (
    llm_provider
    IN
(
    'openai',
    'anthropic',
    'ollama',
    'custom'
)),
    llm_model TEXT NOT NULL,
    system_prompt TEXT,
    message_count INTEGER DEFAULT 0,
    total_tokens INTEGER DEFAULT 0,
    archived BOOLEAN DEFAULT 0,
    pinned BOOLEAN DEFAULT 0,
    metadata TEXT, -- JSON for additional data
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- ============================================
-- Table: messages
-- Stores individual chat messages
-- ============================================
CREATE TABLE IF NOT EXISTS messages
(
    id
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT,
    conversation_id
    INTEGER
    NOT
    NULL,
    role
    TEXT
    NOT
    NULL
    CHECK (
    role
    IN
(
    'SYSTEM',
    'USER',
    'ASSISTANT',
    'FUNCTION'
)),
    content TEXT NOT NULL,
    function_name TEXT,
    function_arguments TEXT, -- JSON
    token_count INTEGER,
    metadata TEXT, -- JSON
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraint
    FOREIGN KEY
(
    conversation_id
) REFERENCES conversations
(
    id
) ON DELETE CASCADE
    );

-- ============================================
-- Indexes for Performance
-- ============================================

-- Conversations indexes
CREATE INDEX IF NOT EXISTS idx_conversations_user_id
    ON conversations(user_id);

CREATE INDEX IF NOT EXISTS idx_conversations_updated_at
    ON conversations(updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_conversations_user_updated
    ON conversations(user_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_conversations_pinned
    ON conversations(user_id, pinned)
    WHERE pinned = 1;

CREATE INDEX IF NOT EXISTS idx_conversations_archived
    ON conversations(user_id, archived)
    WHERE archived = 1;

-- Messages indexes
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id
    ON messages(conversation_id);

CREATE INDEX IF NOT EXISTS idx_messages_created_at
    ON messages(created_at);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_created
    ON messages(conversation_id, created_at);

-- ============================================
-- Triggers for Auto-update Timestamps
-- ============================================

-- Trigger: Update conversations updated_at
CREATE TRIGGER IF NOT EXISTS update_conversations_timestamp 
AFTER
UPDATE ON conversations
BEGIN
UPDATE conversations
SET updated_at = CURRENT_TIMESTAMP
WHERE id = NEW.id;
END;

-- Trigger: Update messages updated_at
CREATE TRIGGER IF NOT EXISTS update_messages_timestamp 
AFTER
UPDATE ON messages
BEGIN
UPDATE messages
SET updated_at = CURRENT_TIMESTAMP
WHERE id = NEW.id;
END;

-- Trigger: Update conversation counts when message is inserted
CREATE TRIGGER IF NOT EXISTS update_conversation_on_message_insert
AFTER INSERT ON messages
BEGIN
UPDATE conversations
SET message_count = message_count + 1,
    updated_at    = CURRENT_TIMESTAMP
WHERE id = NEW.conversation_id;
END;

-- Trigger: Update conversation counts when message is deleted
CREATE TRIGGER IF NOT EXISTS update_conversation_on_message_delete
AFTER
DELETE
ON messages
BEGIN
UPDATE conversations
SET message_count = CASE
                        WHEN message_count > 0 THEN message_count - 1
                        ELSE 0
    END,
    updated_at    = CURRENT_TIMESTAMP
WHERE id = OLD.conversation_id;
END;

-- ============================================
-- Views for Convenience
-- ============================================

-- View: Recent conversations with message count
CREATE VIEW IF NOT EXISTS v_recent_conversations AS
SELECT c.id,
       c.title,
       c.user_id,
       c.preview,
       c.llm_provider,
       c.llm_model,
       c.message_count,
       c.total_tokens,
       c.pinned,
       c.archived,
       c.created_at,
       c.updated_at,
       (SELECT content FROM messages WHERE conversation_id = c.id ORDER BY created_at DESC LIMIT 1) as last_message
FROM conversations c
WHERE c.archived = 0
ORDER BY c.updated_at DESC;

-- View: Conversation statistics
CREATE VIEW IF NOT EXISTS v_conversation_stats AS
SELECT user_id,
       COUNT(*)                                      as total_conversations,
       SUM(message_count)                            as total_messages,
       SUM(total_tokens)                             as total_tokens,
       SUM(CASE WHEN pinned = 1 THEN 1 ELSE 0 END)   as pinned_count,
       SUM(CASE WHEN archived = 1 THEN 1 ELSE 0 END) as archived_count
FROM conversations
GROUP BY user_id;

-- ============================================
-- Sample Data (Optional - for development)
-- ============================================

-- Insert sample conversation
INSERT
OR IGNORE INTO conversations (id, title, user_id, preview, llm_provider, llm_model, message_count, created_at, updated_at)
VALUES (
    1,
    'Welcome to AI Assistant',
    'default-user',
    'This is your first conversation with AI Assistant',
    'openai',
    'gpt-3.5-turbo',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert welcome message
INSERT
OR IGNORE INTO messages (id, conversation_id, role, content, created_at, updated_at)
VALUES (
    1,
    1,
    'ASSISTANT',
    'Welcome to AI Assistant! I''m here to help you with system analysis, code review, performance optimization, and more. How can I assist you today?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================
-- Migration Complete
-- ============================================
-- Version 2 migration completed successfully

