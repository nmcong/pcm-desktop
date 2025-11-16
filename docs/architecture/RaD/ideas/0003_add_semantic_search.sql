PRAGMA
foreign_keys = ON;

CREATE TABLE IF NOT EXISTS search_corpus
(
    corpus_id
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT,
    project_id
    INTEGER
    REFERENCES
    projects
(
    project_id
) ON DELETE CASCADE,
    file_id INTEGER REFERENCES source_files
(
    file_id
)
  ON DELETE CASCADE,
    source_type TEXT NOT NULL,
    label TEXT,
    content TEXT NOT NULL,
    checksum TEXT,
    last_indexed_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_search_corpus_project ON search_corpus(project_id);
CREATE INDEX IF NOT EXISTS idx_search_corpus_file ON search_corpus(file_id);
CREATE INDEX IF NOT EXISTS idx_search_corpus_type ON search_corpus(source_type);

CREATE
VIRTUAL TABLE IF NOT EXISTS search_index USING fts5(
    corpus_id UNINDEXED,
    project_id UNINDEXED,
    source_type,
    label,
    content,
    tokenize = 'unicode61 remove_diacritics 2',
    detail = 'column'
);

CREATE TRIGGER IF NOT EXISTS trg_search_corpus_insert AFTER INSERT ON search_corpus
BEGIN
INSERT INTO search_index(rowid, corpus_id, project_id, source_type, label, content)
VALUES (new.corpus_id, new.corpus_id, new.project_id, new.source_type, new.label, new.content);
END;

CREATE TRIGGER IF NOT EXISTS trg_search_corpus_update AFTER
UPDATE ON search_corpus
BEGIN
UPDATE search_index
SET project_id  = new.project_id,
    source_type = new.source_type,
    label       = new.label,
    content     = new.content
WHERE rowid = new.corpus_id;
END;

CREATE TRIGGER IF NOT EXISTS trg_search_corpus_delete AFTER
DELETE
ON search_corpus
BEGIN
DELETE
FROM search_index
WHERE rowid = old.corpus_id;
END;
