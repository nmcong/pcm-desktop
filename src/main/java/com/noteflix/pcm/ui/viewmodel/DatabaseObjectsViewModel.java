package com.noteflix.pcm.ui.viewmodel;

import com.noteflix.pcm.core.i18n.I18n;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseObjectsViewModel extends BaseViewModel {

  public final StringProperty connectionStatus = new SimpleStringProperty(I18n.get("db.status.disconnected"));
  public final StringProperty databaseName = new SimpleStringProperty("N/A");
  public final StringProperty schemaVersion = new SimpleStringProperty("N/A");
  public final ObservableList<String> schemaObjects = FXCollections.observableArrayList();
  public final StringProperty selectedObjectDetails = new SimpleStringProperty("");

  public DatabaseObjectsViewModel() {
    log.debug("DatabaseObjectsViewModel initialized");
  }

  public void loadDatabaseInfo() {
    setBusy(true);
    clearError();
    log.info("Loading database information...");
    
    runAsync(() -> {
      Thread.sleep(1500); // Simulate DB connection and info retrieval
      if (Math.random() < 0.1) { // Simulate occasional failure
        throw new RuntimeException("Failed to connect to database.");
      }
      return "Connected";
    }, result -> {
      setConnectionStatus(I18n.get("db.status.connected"));
      setDatabaseName("pcm-desktop.db");
      setSchemaVersion("V2");
      schemaObjects.setAll(
        "Table: Projects", 
        "Table: Screens", 
        "View: v_active_projects", 
        "Function: calculate_age"
      );
      setSelectedObjectDetails(I18n.get("db.details.welcome"));
      log.info("Database info loaded successfully.");
    }, error -> {
      setConnectionStatus(I18n.get("db.status.failed"));
      setError(I18n.get("error.db.connection.failed") + ": " + error.getMessage(), error);
      log.error("Error loading database info", error);
    }).whenComplete((r, ex) -> setBusy(false));
  }

  public void refreshSchema() {
    log.info("Refreshing database schema...");
    loadDatabaseInfo(); // Simply re-load for this example
  }

  public void selectSchemaObject(String objectName) {
    if (objectName == null || objectName.isEmpty()) {
      setSelectedObjectDetails(I18n.get("db.details.welcome"));
      return;
    }
    log.info("Selected schema object: {}", objectName);
    setBusy(true);
    clearError();
    
    runAsync(() -> {
      Thread.sleep(500); // Simulate loading object details
      return "Details for " + objectName + ": Columns (ID, Name, Type), Rows (100)";
    }, details -> {
      setSelectedObjectDetails(details);
    }, error -> {
      setError(I18n.get("error.db.details.failed") + ": " + error.getMessage(), error);
      log.error("Error loading object details", error);
    }).whenComplete((r, ex) -> setBusy(false));
  }

  // Getters and Property methods
  public String getConnectionStatus() { return connectionStatus.get(); }
  public StringProperty connectionStatusProperty() { return connectionStatus; }
  public void setConnectionStatus(String connectionStatus) { this.connectionStatus.set(connectionStatus); }

  public String getDatabaseName() { return databaseName.get(); }
  public StringProperty databaseNameProperty() { return databaseName; }
  public void setDatabaseName(String databaseName) { this.databaseName.set(databaseName); }

  public String getSchemaVersion() { return schemaVersion.get(); }
  public StringProperty schemaVersionProperty() { return schemaVersion; }
  public void setSchemaVersion(String schemaVersion) { this.schemaVersion.set(schemaVersion); }

  public ObservableList<String> getSchemaObjects() { return schemaObjects; }

  public String getSelectedObjectDetails() { return selectedObjectDetails.get(); }
  public StringProperty selectedObjectDetailsProperty() { return selectedObjectDetails; }
  public void setSelectedObjectDetails(String selectedObjectDetails) { this.selectedObjectDetails.set(selectedObjectDetails); }
}
