/**
 * PCM Desktop Application Module
 *
 * <p>Java Platform Module System (JPMS) configuration
 *
 * <p>This module defines: - Required dependencies - Exported packages - Opened packages (for
 * reflection/FXML)
 *
 * @author PCM Team
 * @version 4.0.0
 */
module com.noteflix.pcm {
  // JavaFX modules
  requires javafx.controls;
  requires javafx.graphics;
  requires javafx.base;

  // AtlantaFX theme
  requires atlantafx.base;

  // Icon libraries
  requires org.kordamp.ikonli.core;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.ikonli.feather;
  requires org.kordamp.ikonli.material2;

  // Logging
  requires org.slf4j;
  requires ch.qos.logback.classic;
  requires ch.qos.logback.core;

  // Lombok
  requires static lombok;

  // Database
  requires java.sql;
  requires org.xerial.sqlitejdbc;

  // HTTP client (for LLM APIs)
  requires java.net.http;

  // JSON
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.datatype.jsr310;

  // Export main package for JavaFX
  exports com.noteflix.pcm;

  // Export core packages
  exports com.noteflix.pcm.core.constants;
  exports com.noteflix.pcm.core.di;
  exports com.noteflix.pcm.core.i18n;
  exports com.noteflix.pcm.core.navigation;
  exports com.noteflix.pcm.core.theme;
  exports com.noteflix.pcm.core.utils;
  exports com.noteflix.pcm.core.events;
  exports com.noteflix.pcm.core.auth;

  // Export domain
  exports com.noteflix.pcm.domain.entity;
  exports com.noteflix.pcm.domain.repository;
  exports com.noteflix.pcm.domain.chat;

  // Export application services
  exports com.noteflix.pcm.application.service.chat;

  // Export infrastructure
  exports com.noteflix.pcm.infrastructure.database;
  exports com.noteflix.pcm.infrastructure.dao;
  exports com.noteflix.pcm.infrastructure.repository.chat;
  exports com.noteflix.pcm.infrastructure.exception;

  // Export LLM
  exports com.noteflix.pcm.llm.api;
  exports com.noteflix.pcm.llm.client.openai;
  exports com.noteflix.pcm.llm.client.anthropic;
  exports com.noteflix.pcm.llm.client.ollama;
  exports com.noteflix.pcm.llm.factory;
  exports com.noteflix.pcm.llm.model;
  exports com.noteflix.pcm.llm.service;
  exports com.noteflix.pcm.llm.exception;

  // Export UI (for reflection and extension)
  exports com.noteflix.pcm.ui;
  exports com.noteflix.pcm.ui.pages;
  exports com.noteflix.pcm.ui.components;
  exports com.noteflix.pcm.ui.layout;
  exports com.noteflix.pcm.ui.viewmodel;

  // Open packages for reflection (needed for dependency injection, JavaFX, etc.)
  opens com.noteflix.pcm to
      javafx.graphics;
  opens com.noteflix.pcm.ui to
      javafx.graphics;
  opens com.noteflix.pcm.ui.pages to
      javafx.graphics;
  opens com.noteflix.pcm.ui.components to
      javafx.graphics;
  opens com.noteflix.pcm.ui.layout to
      javafx.graphics;

  // Open for JSON serialization
  opens com.noteflix.pcm.domain.chat to
      com.fasterxml.jackson.databind;
  opens com.noteflix.pcm.domain.entity to
      com.fasterxml.jackson.databind;
  opens com.noteflix.pcm.llm.model to
      com.fasterxml.jackson.databind;
}

