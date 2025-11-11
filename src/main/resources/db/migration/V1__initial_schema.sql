-- PCM Desktop Database Schema
-- Version 1: Initial Schema
-- Created: 2025-11-11

-- Enable foreign key constraints
PRAGMA foreign_keys = ON;

-- ============================================================================
-- CORE TABLES
-- ============================================================================

-- Projects table
CREATE TABLE IF NOT EXISTS projects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    code TEXT NOT NULL UNIQUE,
    description TEXT,
    type TEXT NOT NULL CHECK(type IN ('SUBSYSTEM', 'MODULE', 'SERVICE')),
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'ARCHIVED', 'DEPRECATED')),
    color TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT,
    updated_by TEXT
);

CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_code ON projects(code);
CREATE INDEX idx_projects_type ON projects(type);

-- Screens table
CREATE TABLE IF NOT EXISTS screens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    description TEXT,
    type TEXT NOT NULL CHECK(type IN ('FORM', 'LIST', 'DETAIL', 'DASHBOARD', 'DIALOG')),
    category TEXT,
    file_path TEXT,
    class_name TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'INACTIVE', 'DEPRECATED')),
    priority TEXT DEFAULT 'MEDIUM' CHECK(priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    completion_percentage INTEGER DEFAULT 0 CHECK(completion_percentage >= 0 AND completion_percentage <= 100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE(project_id, code)
);

CREATE INDEX idx_screens_project ON screens(project_id);
CREATE INDEX idx_screens_status ON screens(status);
CREATE INDEX idx_screens_category ON screens(category);
CREATE INDEX idx_screens_type ON screens(type);

-- Database objects table
CREATE TABLE IF NOT EXISTS database_objects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER,
    name TEXT NOT NULL,
    schema_name TEXT DEFAULT 'main',
    type TEXT NOT NULL CHECK(type IN ('TABLE', 'VIEW', 'PROCEDURE', 'FUNCTION', 'TRIGGER')),
    description TEXT,
    definition TEXT,
    dependencies TEXT, -- JSON array
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    UNIQUE(schema_name, name, type)
);

CREATE INDEX idx_db_objects_project ON database_objects(project_id);
CREATE INDEX idx_db_objects_type ON database_objects(type);
CREATE INDEX idx_db_objects_name ON database_objects(name);

-- Batch jobs table
CREATE TABLE IF NOT EXISTS batch_jobs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER,
    name TEXT NOT NULL,
    code TEXT NOT NULL UNIQUE,
    description TEXT,
    job_type TEXT CHECK(job_type IN ('SCHEDULED', 'ON_DEMAND', 'TRIGGERED')),
    schedule_cron TEXT,
    command TEXT,
    script_path TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'INACTIVE', 'DISABLED')),
    last_run_at TIMESTAMP,
    last_run_status TEXT CHECK(last_run_status IN ('SUCCESS', 'FAILED', 'RUNNING')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL
);

CREATE INDEX idx_batch_jobs_project ON batch_jobs(project_id);
CREATE INDEX idx_batch_jobs_status ON batch_jobs(status);
CREATE INDEX idx_batch_jobs_type ON batch_jobs(job_type);

-- ============================================================================
-- WORKFLOW TABLES
-- ============================================================================

-- Workflows table
CREATE TABLE IF NOT EXISTS workflows (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER,
    name TEXT NOT NULL,
    description TEXT,
    start_screen_id INTEGER,
    workflow_data TEXT, -- JSON representation
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'INACTIVE', 'DEPRECATED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (start_screen_id) REFERENCES screens(id) ON DELETE SET NULL
);

CREATE INDEX idx_workflows_project ON workflows(project_id);
CREATE INDEX idx_workflows_status ON workflows(status);

-- Workflow steps table
CREATE TABLE IF NOT EXISTS workflow_steps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    workflow_id INTEGER NOT NULL,
    screen_id INTEGER,
    step_order INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    action_type TEXT CHECK(action_type IN ('NAVIGATION', 'VALIDATION', 'API_CALL', 'DATA_TRANSFORM')),
    next_step_id INTEGER,
    conditions TEXT, -- JSON conditions
    FOREIGN KEY (workflow_id) REFERENCES workflows(id) ON DELETE CASCADE,
    FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE SET NULL,
    FOREIGN KEY (next_step_id) REFERENCES workflow_steps(id) ON DELETE SET NULL
);

CREATE INDEX idx_workflow_steps_workflow ON workflow_steps(workflow_id);
CREATE INDEX idx_workflow_steps_order ON workflow_steps(workflow_id, step_order);

-- ============================================================================
-- TAGGING & RELATIONS
-- ============================================================================

-- Tags table
CREATE TABLE IF NOT EXISTS tags (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    color TEXT,
    category TEXT CHECK(category IN ('TECHNOLOGY', 'PRIORITY', 'STATUS', 'CUSTOM')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tags_category ON tags(category);
CREATE INDEX idx_tags_name ON tags(name);

-- Screen tags (many-to-many)
CREATE TABLE IF NOT EXISTS screen_tags (
    screen_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (screen_id, tag_id),
    FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX idx_screen_tags_screen ON screen_tags(screen_id);
CREATE INDEX idx_screen_tags_tag ON screen_tags(tag_id);

-- Screen relations (self-referencing many-to-many)
CREATE TABLE IF NOT EXISTS screen_relations (
    from_screen_id INTEGER NOT NULL,
    to_screen_id INTEGER NOT NULL,
    relation_type TEXT NOT NULL CHECK(relation_type IN ('NAVIGATES_TO', 'CALLS', 'INCLUDES', 'PARENT_OF')),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (from_screen_id, to_screen_id, relation_type),
    FOREIGN KEY (from_screen_id) REFERENCES screens(id) ON DELETE CASCADE,
    FOREIGN KEY (to_screen_id) REFERENCES screens(id) ON DELETE CASCADE
);

CREATE INDEX idx_screen_relations_from ON screen_relations(from_screen_id);
CREATE INDEX idx_screen_relations_to ON screen_relations(to_screen_id);

-- ============================================================================
-- KNOWLEDGE BASE
-- ============================================================================

-- Knowledge base table
CREATE TABLE IF NOT EXISTS knowledge_base (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    category TEXT CHECK(category IN ('DOCUMENTATION', 'BEST_PRACTICE', 'DESIGN_PATTERN', 'TECHNICAL_NOTE')),
    tags TEXT, -- JSON array
    project_id INTEGER,
    screen_id INTEGER,
    embedding_vector TEXT, -- JSON array for AI/RAG
    metadata TEXT, -- JSON metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE SET NULL
);

CREATE INDEX idx_kb_category ON knowledge_base(category);
CREATE INDEX idx_kb_project ON knowledge_base(project_id);
CREATE INDEX idx_kb_screen ON knowledge_base(screen_id);
CREATE INDEX idx_kb_title ON knowledge_base(title);

-- ============================================================================
-- SYSTEM TABLES
-- ============================================================================

-- Activity log table
CREATE TABLE IF NOT EXISTS activity_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_type TEXT NOT NULL,
    entity_id INTEGER NOT NULL,
    action TEXT NOT NULL CHECK(action IN ('CREATED', 'UPDATED', 'DELETED', 'VIEWED')),
    user_name TEXT,
    description TEXT,
    changes TEXT, -- JSON representation
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activity_entity ON activity_log(entity_type, entity_id);
CREATE INDEX idx_activity_created ON activity_log(created_at DESC);
CREATE INDEX idx_activity_action ON activity_log(action);

-- Settings table
CREATE TABLE IF NOT EXISTS settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    category TEXT CHECK(category IN ('APPEARANCE', 'DATABASE', 'AI', 'GENERAL')),
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Favorites table
CREATE TABLE IF NOT EXISTS favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_name TEXT NOT NULL,
    entity_type TEXT NOT NULL CHECK(entity_type IN ('PROJECT', 'SCREEN', 'WORKFLOW')),
    entity_id INTEGER NOT NULL,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_name, entity_type, entity_id)
);

CREATE INDEX idx_favorites_user ON favorites(user_name);
CREATE INDEX idx_favorites_entity ON favorites(entity_type, entity_id);

-- Schema version tracking
CREATE TABLE IF NOT EXISTS schema_version (
    version INTEGER PRIMARY KEY,
    description TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial version
INSERT INTO schema_version (version, description) VALUES (1, 'Initial schema');

-- ============================================================================
-- DEFAULT DATA
-- ============================================================================

-- Default settings
INSERT INTO settings (key, value, category, description) VALUES
    ('theme', 'PrimerLight', 'APPEARANCE', 'Application theme'),
    ('db_version', '1', 'DATABASE', 'Database schema version'),
    ('ai_enabled', 'false', 'AI', 'Enable AI features'),
    ('auto_backup', 'true', 'GENERAL', 'Enable automatic backups');

-- Default tags
INSERT INTO tags (name, color, category) VALUES
    ('High Priority', '#dc2626', 'PRIORITY'),
    ('Authentication', '#2563eb', 'TECHNOLOGY'),
    ('Reporting', '#16a34a', 'TECHNOLOGY'),
    ('Active', '#10b981', 'STATUS'),
    ('Deprecated', '#6b7280', 'STATUS');

-- Sample project (for testing)
INSERT INTO projects (name, code, description, type, status, color) VALUES
    ('Sample Project', 'SAMPLE', 'Sample project for testing', 'SUBSYSTEM', 'ACTIVE', '#3b82f6');

-- ============================================================================
-- TRIGGERS FOR UPDATED_AT
-- ============================================================================

-- Auto-update updated_at for projects
CREATE TRIGGER IF NOT EXISTS projects_updated_at 
AFTER UPDATE ON projects
BEGIN
    UPDATE projects SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Auto-update updated_at for screens
CREATE TRIGGER IF NOT EXISTS screens_updated_at 
AFTER UPDATE ON screens
BEGIN
    UPDATE screens SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Auto-update updated_at for database_objects
CREATE TRIGGER IF NOT EXISTS database_objects_updated_at 
AFTER UPDATE ON database_objects
BEGIN
    UPDATE database_objects SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Auto-update updated_at for batch_jobs
CREATE TRIGGER IF NOT EXISTS batch_jobs_updated_at 
AFTER UPDATE ON batch_jobs
BEGIN
    UPDATE batch_jobs SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Auto-update updated_at for workflows
CREATE TRIGGER IF NOT EXISTS workflows_updated_at 
AFTER UPDATE ON workflows
BEGIN
    UPDATE workflows SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Auto-update updated_at for knowledge_base
CREATE TRIGGER IF NOT EXISTS knowledge_base_updated_at 
AFTER UPDATE ON knowledge_base
BEGIN
    UPDATE knowledge_base SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- ============================================================================
-- VIEWS (for convenience)
-- ============================================================================

-- Active projects view
CREATE VIEW IF NOT EXISTS v_active_projects AS
SELECT * FROM projects WHERE status = 'ACTIVE';

-- Active screens view
CREATE VIEW IF NOT EXISTS v_active_screens AS
SELECT 
    s.*,
    p.name as project_name,
    p.code as project_code
FROM screens s
JOIN projects p ON s.project_id = p.id
WHERE s.status = 'ACTIVE';

-- Recent activity view
CREATE VIEW IF NOT EXISTS v_recent_activity AS
SELECT * FROM activity_log
ORDER BY created_at DESC
LIMIT 100;

-- ============================================================================
-- END OF MIGRATION V1
-- ============================================================================

