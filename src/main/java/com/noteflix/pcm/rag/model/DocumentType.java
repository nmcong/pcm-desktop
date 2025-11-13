package com.noteflix.pcm.rag.model;

/**
 * Types of documents that can be indexed in the RAG system.
 *
 * @author PCM Team
 * @version 1.0.0
 */
public enum DocumentType {
  // Source code
  SOURCE_CODE,
  JAVA_CLASS,
  JAVA_INTERFACE,
  JAVA_METHOD,

  // Database objects
  DATABASE_SCHEMA,
  TABLE,
  VIEW,
  PROCEDURE,
  FUNCTION,
  PACKAGE,
  TRIGGER,

  // System metadata
  SCREEN,
  WORKFLOW,
  BATCH_JOB,
  SUBSYSTEM,
  PROJECT,

  // Documentation
  KNOWLEDGE_BASE,
  API_DOC,
  COMMENT,
  TEXT,

  // Generic
  OTHER,
  UNKNOWN
}
