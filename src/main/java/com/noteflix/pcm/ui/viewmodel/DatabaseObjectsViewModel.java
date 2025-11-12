package com.noteflix.pcm.ui.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

/**
 * ViewModel for Database Objects Page
 *
 * <p>Manages database schema and objects state
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class DatabaseObjectsViewModel extends BaseViewModel {

  // Connection info
  private final StringProperty connectionString =
      new SimpleStringProperty("jdbc:sqlite:pcm-desktop.db");
  private final StringProperty connectionStatus = new SimpleStringProperty("Connected");
  private final StringProperty databaseType = new SimpleStringProperty("SQLite");

  // Schema objects
  private final ObservableList<DatabaseObject> tables = FXCollections.observableArrayList();
  private final ObservableList<DatabaseObject> views = FXCollections.observableArrayList();
  private final ObservableList<DatabaseObject> procedures = FXCollections.observableArrayList();
  private final StringProperty selectedObjectInfo = new SimpleStringProperty("");

  /** Constructor */
  public DatabaseObjectsViewModel() {
    loadDatabaseObjects();
    log.info("DatabaseObjectsViewModel initialized");
  }

  /** Load database objects */
  private void loadDatabaseObjects() {
    // Load tables (mock data)
    tables.add(new DatabaseObject("projects", "TABLE", "12 rows"));
    tables.add(new DatabaseObject("screens", "TABLE", "45 rows"));
    tables.add(new DatabaseObject("database_objects", "TABLE", "78 rows"));
    tables.add(new DatabaseObject("batch_jobs", "TABLE", "34 rows"));
    tables.add(new DatabaseObject("workflows", "TABLE", "23 rows"));
    tables.add(new DatabaseObject("conversations", "TABLE", "8 rows"));
    tables.add(new DatabaseObject("messages", "TABLE", "24 rows"));

    // Load views
    views.add(new DatabaseObject("v_active_projects", "VIEW", "Active projects view"));
    views.add(new DatabaseObject("v_active_screens", "VIEW", "Active screens view"));
    views.add(new DatabaseObject("v_recent_activity", "VIEW", "Recent activity view"));
    views.add(new DatabaseObject("v_recent_conversations", "VIEW", "Recent conversations"));

    // Procedures (SQLite doesn't have stored procedures, but keeping for extensibility)
    procedures.add(new DatabaseObject("No stored procedures", "INFO", "SQLite limitation"));

    log.debug(
        "Loaded {} tables, {} views, {} procedures",
        tables.size(),
        views.size(),
        procedures.size());
  }

  /** Refresh database objects */
  public void refreshObjects() {
    setBusy(true);
    clearError();

    tables.clear();
    views.clear();
    procedures.clear();

    // Reload
    loadDatabaseObjects();

    setBusy(false);
    log.info("Database objects refreshed");
  }

  /** Show object details */
  public void showObjectDetails(DatabaseObject object) {
    if (object == null) {
      selectedObjectInfo.set("");
      return;
    }

    String info =
        String.format(
            "Object: %s\nType: %s\nInfo: %s",
            object.name, object.type, object.info);
    selectedObjectInfo.set(info);
    log.debug("Showing details for object: {}", object.name);
  }

  // Property accessors
  public StringProperty connectionStringProperty() {
    return connectionString;
  }

  public String getConnectionString() {
    return connectionString.get();
  }

  public StringProperty connectionStatusProperty() {
    return connectionStatus;
  }

  public String getConnectionStatus() {
    return connectionStatus.get();
  }

  public StringProperty databaseTypeProperty() {
    return databaseType;
  }

  public String getDatabaseType() {
    return databaseType.get();
  }

  public ObservableList<DatabaseObject> getTables() {
    return tables;
  }

  public ObservableList<DatabaseObject> getViews() {
    return views;
  }

  public ObservableList<DatabaseObject> getProcedures() {
    return procedures;
  }

  public StringProperty selectedObjectInfoProperty() {
    return selectedObjectInfo;
  }

  public String getSelectedObjectInfo() {
    return selectedObjectInfo.get();
  }

  /** Database Object model */
  public static class DatabaseObject {
    public final String name;
    public final String type;
    public final String info;

    public DatabaseObject(String name, String type, String info) {
      this.name = name;
      this.type = type;
      this.info = info;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}

