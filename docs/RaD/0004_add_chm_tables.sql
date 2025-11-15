PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS chm_imports (
    import_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    system_id      INTEGER REFERENCES systems(system_id) ON DELETE SET NULL,
    subsystem_id   INTEGER REFERENCES subsystems(subsystem_id) ON DELETE SET NULL,
    project_id     INTEGER REFERENCES projects(project_id) ON DELETE SET NULL,
    chm_path       TEXT NOT NULL,
    chm_checksum   TEXT,
    extracted_path TEXT,
    status         TEXT DEFAULT 'pending',
    notes          TEXT,
    imported_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chm_imports_project ON chm_imports(project_id);

CREATE TABLE IF NOT EXISTS chm_documents (
    document_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    import_id      INTEGER NOT NULL REFERENCES chm_imports(import_id) ON DELETE CASCADE,
    relative_path  TEXT NOT NULL,
    title          TEXT,
    toc_path       TEXT,
    order_index    INTEGER,
    content        TEXT NOT NULL,
    checksum       TEXT,
    metadata       TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_chm_documents_path ON chm_documents(import_id, relative_path);

CREATE TABLE IF NOT EXISTS chm_assets (
    asset_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    import_id      INTEGER NOT NULL REFERENCES chm_imports(import_id) ON DELETE CASCADE,
    relative_path  TEXT NOT NULL,
    mime_type      TEXT,
    size_bytes     INTEGER,
    checksum       TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_chm_assets_path ON chm_assets(import_id, relative_path);
