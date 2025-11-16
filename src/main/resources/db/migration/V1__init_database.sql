-- PCM Desktop Database - Complete Schema
-- Auto-initialized on first startup if tables don't exist
-- No sample data - all tables empty by default

PRAGMA foreign_keys = ON;

-- ============================================================================
-- CORE TABLES
-- ============================================================================

-- Projects
CREATE TABLE IF NOT EXISTS projects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    code TEXT NOT NULL UNIQUE,
    description TEXT,
    type TEXT NOT NULL CHECK (type IN ('SUBSYSTEM', 'MODULE', 'SERVICE')),
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED', 'INACTIVE')),
    color TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Screens/Forms
CREATE TABLE IF NOT EXISTS screens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    screen_id TEXT,
    path TEXT,
    description TEXT,
    category TEXT,
    type TEXT CHECK (type IN ('SCREEN', 'FORM', 'DIALOG', 'COMPONENT')),
    status TEXT DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED', 'DEPRECATED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- Database Objects
CREATE TABLE IF NOT EXISTS database_objects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER,
    name TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('TABLE', 'VIEW', 'PACKAGE', 'PROCEDURE', 'FUNCTION', 'TRIGGER', 'SEQUENCE')),
    schema_name TEXT,
    description TEXT,
    ddl_script TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
);

-- Batch Jobs
CREATE TABLE IF NOT EXISTS batch_jobs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER,
    name TEXT NOT NULL,
    job_id TEXT,
    job_type TEXT CHECK (job_type IN ('SCHEDULED', 'TRIGGERED', 'MANUAL')),
    schedule TEXT,
    description TEXT,
    status TEXT DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'DEPRECATED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
);

-- Workflows
CREATE TABLE IF NOT EXISTS workflows (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER,
    name TEXT NOT NULL,
    workflow_id TEXT,
    description TEXT,
    status TEXT DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
);

-- Workflow Steps
CREATE TABLE IF NOT EXISTS workflow_steps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    workflow_id INTEGER NOT NULL,
    step_order INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    action_type TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workflow_id) REFERENCES workflows(id) ON DELETE CASCADE
);

-- Tags
CREATE TABLE IF NOT EXISTS tags (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    color TEXT,
    category TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Screen Tags (Many-to-Many)
CREATE TABLE IF NOT EXISTS screen_tags (
    screen_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    PRIMARY KEY (screen_id, tag_id),
    FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Screen Relations
CREATE TABLE IF NOT EXISTS screen_relations (
    from_screen_id INTEGER NOT NULL,
    to_screen_id INTEGER NOT NULL,
    relation_type TEXT CHECK (relation_type IN ('CALLS', 'INCLUDES', 'EXTENDS', 'USES')),
    description TEXT,
    PRIMARY KEY (from_screen_id, to_screen_id),
    FOREIGN KEY (from_screen_id) REFERENCES screens(id) ON DELETE CASCADE,
    FOREIGN KEY (to_screen_id) REFERENCES screens(id) ON DELETE CASCADE
);

-- Knowledge Base
CREATE TABLE IF NOT EXISTS knowledge_base (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    category TEXT,
    project_id INTEGER,
    screen_id INTEGER,
    tags TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE SET NULL
);

-- Activity Log
CREATE TABLE IF NOT EXISTS activity_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_type TEXT NOT NULL,
    entity_id INTEGER NOT NULL,
    action TEXT NOT NULL,
    description TEXT,
    user_name TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Settings
CREATE TABLE IF NOT EXISTS settings (
    key TEXT PRIMARY KEY,
    value TEXT,
    category TEXT,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Favorites
CREATE TABLE IF NOT EXISTS favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_name TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_name, entity_type, entity_id)
);

-- Schema Version (for migration tracking)
CREATE TABLE IF NOT EXISTS schema_version (
    version TEXT PRIMARY KEY,
    description TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- CHAT TABLES
-- ============================================================================

-- Conversations
CREATE TABLE IF NOT EXISTS conversations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    user_id TEXT DEFAULT 'default',
    is_pinned INTEGER DEFAULT 0,
    is_archived INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Messages
CREATE TABLE IF NOT EXISTS messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    conversation_id INTEGER NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('user', 'assistant', 'system')),
    content TEXT NOT NULL,
    model TEXT,
    tokens_used INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

-- ============================================================================
-- PROVIDER CONFIGURATIONS
-- ============================================================================

-- LLM Provider Configurations
CREATE TABLE IF NOT EXISTS provider_configurations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_name TEXT NOT NULL UNIQUE,
    display_name TEXT NOT NULL,
    api_key TEXT,
    api_base_url TEXT,
    default_model TEXT,
    is_active INTEGER DEFAULT 0,
    is_enabled INTEGER DEFAULT 1,
    requires_api_key INTEGER DEFAULT 1,
    connection_timeout INTEGER DEFAULT 30000,
    max_retries INTEGER DEFAULT 3,
    extra_config TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_tested_at TIMESTAMP,
    test_status TEXT
);

-- ============================================================================
-- INDEXES
-- ============================================================================

-- Projects
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status);
CREATE INDEX IF NOT EXISTS idx_projects_code ON projects(code);
CREATE INDEX IF NOT EXISTS idx_projects_type ON projects(type);

-- Screens
CREATE INDEX IF NOT EXISTS idx_screens_project ON screens(project_id);
CREATE INDEX IF NOT EXISTS idx_screens_status ON screens(status);
CREATE INDEX IF NOT EXISTS idx_screens_category ON screens(category);
CREATE INDEX IF NOT EXISTS idx_screens_type ON screens(type);

-- Database Objects
CREATE INDEX IF NOT EXISTS idx_db_objects_project ON database_objects(project_id);
CREATE INDEX IF NOT EXISTS idx_db_objects_type ON database_objects(type);
CREATE INDEX IF NOT EXISTS idx_db_objects_name ON database_objects(name);

-- Batch Jobs
CREATE INDEX IF NOT EXISTS idx_batch_jobs_project ON batch_jobs(project_id);
CREATE INDEX IF NOT EXISTS idx_batch_jobs_status ON batch_jobs(status);
CREATE INDEX IF NOT EXISTS idx_batch_jobs_type ON batch_jobs(job_type);

-- Workflows
CREATE INDEX IF NOT EXISTS idx_workflows_project ON workflows(project_id);
CREATE INDEX IF NOT EXISTS idx_workflows_status ON workflows(status);

-- Workflow Steps
CREATE INDEX IF NOT EXISTS idx_workflow_steps_workflow ON workflow_steps(workflow_id);
CREATE INDEX IF NOT EXISTS idx_workflow_steps_order ON workflow_steps(workflow_id, step_order);

-- Tags
CREATE INDEX IF NOT EXISTS idx_tags_category ON tags(category);
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);

-- Screen Tags
CREATE INDEX IF NOT EXISTS idx_screen_tags_screen ON screen_tags(screen_id);
CREATE INDEX IF NOT EXISTS idx_screen_tags_tag ON screen_tags(tag_id);

-- Screen Relations
CREATE INDEX IF NOT EXISTS idx_screen_relations_from ON screen_relations(from_screen_id);
CREATE INDEX IF NOT EXISTS idx_screen_relations_to ON screen_relations(to_screen_id);

-- Knowledge Base
CREATE INDEX IF NOT EXISTS idx_kb_category ON knowledge_base(category);
CREATE INDEX IF NOT EXISTS idx_kb_project ON knowledge_base(project_id);
CREATE INDEX IF NOT EXISTS idx_kb_screen ON knowledge_base(screen_id);
CREATE INDEX IF NOT EXISTS idx_kb_title ON knowledge_base(title);

-- Activity Log
CREATE INDEX IF NOT EXISTS idx_activity_entity ON activity_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_activity_created ON activity_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_action ON activity_log(action);

-- Favorites
CREATE INDEX IF NOT EXISTS idx_favorites_user ON favorites(user_name);
CREATE INDEX IF NOT EXISTS idx_favorites_entity ON favorites(entity_type, entity_id);

-- Conversations
CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_updated_at ON conversations(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_user_updated ON conversations(user_id, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_pinned ON conversations(user_id, is_pinned, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_archived ON conversations(user_id, is_archived);

-- Messages
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON messages(conversation_id, created_at);

-- Provider Configurations
CREATE INDEX IF NOT EXISTS idx_provider_name ON provider_configurations(provider_name);
CREATE INDEX IF NOT EXISTS idx_is_active ON provider_configurations(is_active);

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Auto-update timestamps
CREATE TRIGGER IF NOT EXISTS projects_updated_at
AFTER UPDATE ON projects
BEGIN
    UPDATE projects SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS screens_updated_at
AFTER UPDATE ON screens
BEGIN
    UPDATE screens SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS database_objects_updated_at
AFTER UPDATE ON database_objects
BEGIN
    UPDATE database_objects SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS batch_jobs_updated_at
AFTER UPDATE ON batch_jobs
BEGIN
    UPDATE batch_jobs SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS workflows_updated_at
AFTER UPDATE ON workflows
BEGIN
    UPDATE workflows SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS knowledge_base_updated_at
AFTER UPDATE ON knowledge_base
BEGIN
    UPDATE knowledge_base SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Conversation triggers
CREATE TRIGGER IF NOT EXISTS update_conversations_timestamp
AFTER UPDATE ON conversations
BEGIN
    UPDATE conversations SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS update_messages_timestamp
AFTER UPDATE ON messages
BEGIN
    UPDATE messages SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS update_conversation_on_message_insert
AFTER INSERT ON messages
BEGIN
    UPDATE conversations SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.conversation_id;
END;

CREATE TRIGGER IF NOT EXISTS update_conversation_on_message_delete
AFTER DELETE ON messages
BEGIN
    UPDATE conversations SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.conversation_id;
END;

-- ============================================================================
-- VIEWS
-- ============================================================================

CREATE VIEW IF NOT EXISTS v_active_projects AS
SELECT * FROM projects WHERE status = 'ACTIVE';

CREATE VIEW IF NOT EXISTS v_active_screens AS
SELECT s.*, p.name as project_name, p.code as project_code
FROM screens s
LEFT JOIN projects p ON s.project_id = p.id
WHERE s.status = 'ACTIVE';

CREATE VIEW IF NOT EXISTS v_recent_activity AS
SELECT * FROM activity_log
ORDER BY created_at DESC
LIMIT 100;

CREATE VIEW IF NOT EXISTS v_recent_conversations AS
SELECT c.id, c.title, c.user_id, c.is_pinned, c.created_at, c.updated_at,
       COUNT(m.id) as message_count,
       MAX(m.created_at) as last_message_at
FROM conversations c
LEFT JOIN messages m ON c.conversation_id = m.conversation_id
WHERE c.is_archived = 0
GROUP BY c.id
ORDER BY c.is_pinned DESC, c.updated_at DESC;

CREATE VIEW IF NOT EXISTS v_conversation_stats AS
SELECT user_id,
       COUNT(*) as total_conversations,
       SUM(CASE WHEN is_pinned = 1 THEN 1 ELSE 0 END) as pinned_count,
       SUM(CASE WHEN is_archived = 1 THEN 1 ELSE 0 END) as archived_count
FROM conversations
GROUP BY user_id;

-- ============================================================================
-- RECORD INITIAL MIGRATION
-- ============================================================================

INSERT OR IGNORE INTO schema_version (version, description) VALUES ('V1', 'initial database setup');

