package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/** Batch Jobs page - Single Responsibility Principle Manages batch job monitoring and scheduling */
@Slf4j
public class BatchJobsPage extends BasePage {

  private TableView<JobEntry> jobsTable;

  public BatchJobsPage() {
    super(
        "Batch Jobs",
        "Monitor, schedule, and manage your batch processing operations",
        new FontIcon(Feather.CLOCK));
  }

  @Override
  protected VBox createMainContent() {
    VBox mainContent = new VBox(20);
    mainContent.getStyleClass().add("batch-jobs-content");
    VBox.setVgrow(mainContent, Priority.ALWAYS);

    // Statistics section
    mainContent.getChildren().add(createStatsSection());

    // Control panel
    mainContent.getChildren().add(createControlPanel());

    // Jobs table
    mainContent.getChildren().add(createJobsTable());

    return mainContent;
  }

  private HBox createStatsSection() {
    HBox statsRow = new HBox(16);
    statsRow
        .getChildren()
        .addAll(
            createStatCard("Running Jobs", "3", Feather.PLAY_CIRCLE, "success"),
            createStatCard("Scheduled", "12", Feather.CLOCK, "info"),
            createStatCard("Failed Today", "1", Feather.ALERT_CIRCLE, "danger"),
            createStatCard("Completed", "45", Feather.CHECK_CIRCLE, "success"));
    return statsRow;
  }

  private VBox createStatCard(String title, String value, Feather icon, String type) {
    VBox card = new VBox(8);
    card.getStyleClass().addAll("card", "stat-card", "stat-" + type);
    card.setPadding(new Insets(20));
    card.setAlignment(Pos.CENTER);
    HBox.setHgrow(card, Priority.ALWAYS);

    FontIcon iconNode = new FontIcon(icon);
    iconNode.setIconSize(24);
    iconNode.getStyleClass().add("stat-icon");

    Label valueLabel = new Label(value);
    valueLabel.getStyleClass().addAll(Styles.TITLE_2, "stat-value");

    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    card.getChildren().addAll(iconNode, valueLabel, titleLabel);
    return card;
  }

  private VBox createControlPanel() {
    VBox panel = new VBox(16);
    panel.getStyleClass().add("card");
    panel.setPadding(new Insets(20));

    HBox controlsRow = new HBox(12);
    controlsRow.setAlignment(Pos.CENTER_LEFT);

    Button newJobButton = new Button("New Job");
    newJobButton.setGraphic(new FontIcon(Feather.PLUS));
    newJobButton.getStyleClass().addAll(Styles.ACCENT);
    newJobButton.setOnAction(e -> handleNewJob());

    Button refreshButton = new Button("Refresh");
    refreshButton.setGraphic(new FontIcon(Feather.REFRESH_CW));
    refreshButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
    refreshButton.setOnAction(e -> handleRefresh());

    ComboBox<String> filterCombo = new ComboBox<>();
    filterCombo.getItems().addAll("All Jobs", "Running", "Scheduled", "Failed", "Completed");
    filterCombo.setValue("All Jobs");
    filterCombo.getStyleClass().add("filter-combo");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    TextField searchField = new TextField();
    searchField.setPromptText("Search jobs...");
    searchField.setPrefWidth(200);

    controlsRow.getChildren().addAll(newJobButton, refreshButton, filterCombo, spacer, searchField);

    panel.getChildren().add(controlsRow);
    return panel;
  }

  private VBox createJobsTable() {
    VBox tableSection = new VBox(12);
    tableSection.getStyleClass().add("card");
    tableSection.setPadding(new Insets(20));
    VBox.setVgrow(tableSection, Priority.ALWAYS);

    Label tableTitle = new Label("Recent Jobs");
    tableTitle.getStyleClass().addAll(Styles.TITLE_3);

    jobsTable = new TableView<>();
    jobsTable.getStyleClass().add("jobs-table");
    VBox.setVgrow(jobsTable, Priority.ALWAYS);

    // Create columns
    TableColumn<JobEntry, String> nameColumn = new TableColumn<>("Job Name");
    nameColumn.setCellValueFactory(
        data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
    nameColumn.setPrefWidth(200);

    TableColumn<JobEntry, String> statusColumn = new TableColumn<>("Status");
    statusColumn.setCellValueFactory(
        data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
    statusColumn.setPrefWidth(100);
    statusColumn.setCellFactory(
        column ->
            new TableCell<JobEntry, String>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setStyle("");
                } else {
                  setText(item);
                  getStyleClass()
                      .removeAll(
                          "status-running",
                          "status-completed",
                          "status-failed",
                          "status-scheduled");
                  getStyleClass().add("status-" + item.toLowerCase());
                }
              }
            });

    TableColumn<JobEntry, String> lastRunColumn = new TableColumn<>("Last Run");
    lastRunColumn.setCellValueFactory(
        data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLastRun()));
    lastRunColumn.setPrefWidth(150);

    TableColumn<JobEntry, String> nextRunColumn = new TableColumn<>("Next Run");
    nextRunColumn.setCellValueFactory(
        data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNextRun()));
    nextRunColumn.setPrefWidth(150);

    TableColumn<JobEntry, String> durationColumn = new TableColumn<>("Duration");
    durationColumn.setCellValueFactory(
        data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDuration()));
    durationColumn.setPrefWidth(100);

    TableColumn<JobEntry, Void> actionsColumn = new TableColumn<>("Actions");
    actionsColumn.setPrefWidth(150);
    actionsColumn.setCellFactory(
        column ->
            new TableCell<JobEntry, Void>() {
              private final Button actionButton = new Button();

              {
                actionButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
                actionButton.setGraphic(new FontIcon(Feather.MORE_HORIZONTAL));
              }

              @Override
              protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                  setGraphic(null);
                } else {
                  setGraphic(actionButton);
                  actionButton.setOnAction(e -> handleJobAction(getTableRow().getItem()));
                }
              }
            });

    jobsTable.getColumns().add(nameColumn);
    jobsTable.getColumns().add(statusColumn);
    jobsTable.getColumns().add(lastRunColumn);
    jobsTable.getColumns().add(nextRunColumn);
    jobsTable.getColumns().add(durationColumn);
    jobsTable.getColumns().add(actionsColumn);

    // Sample data
    loadSampleData();

    tableSection.getChildren().addAll(tableTitle, jobsTable);
    return tableSection;
  }

  private void loadSampleData() {
    jobsTable
        .getItems()
        .add(
            new JobEntry(
                "Daily Report Generation", "Completed", "Today 02:00", "Tomorrow 02:00", "5m 32s"));
    jobsTable
        .getItems()
        .add(
            new JobEntry(
                "Database Backup", "Running", "Today 01:45", "Tomorrow 01:45", "Running..."));
    jobsTable
        .getItems()
        .add(new JobEntry("Data Cleanup", "Scheduled", "Yesterday 23:00", "Today 23:00", "2m 15s"));
    jobsTable
        .getItems()
        .add(new JobEntry("Email Notifications", "Failed", "Today 08:00", "Today 09:00", "Failed"));
    jobsTable
        .getItems()
        .add(new JobEntry("Log Archive", "Completed", "Today 00:30", "Tomorrow 00:30", "12m 05s"));
    jobsTable
        .getItems()
        .add(new JobEntry("User Sync", "Scheduled", "Not run yet", "Today 18:00", "-"));
    jobsTable
        .getItems()
        .add(new JobEntry("Cache Refresh", "Completed", "Today 12:00", "Today 18:00", "30s"));
  }

  private void handleNewJob() {
    log.info("Creating new batch job");
    // Open new job dialog
  }

  private void handleRefresh() {
    log.info("Refreshing batch jobs list");
    // Refresh jobs data
  }

  private void handleJobAction(JobEntry job) {
    if (job != null) {
      log.info("Job action for: {}", job.getName());
      // Show job actions menu (start, stop, edit, delete, view logs, etc.)
    }
  }

  // Inner class for job data
  public static class JobEntry {
    private final String name;
    private final String status;
    private final String lastRun;
    private final String nextRun;
    private final String duration;

    public JobEntry(String name, String status, String lastRun, String nextRun, String duration) {
      this.name = name;
      this.status = status;
      this.lastRun = lastRun;
      this.nextRun = nextRun;
      this.duration = duration;
    }

    public String getName() {
      return name;
    }

    public String getStatus() {
      return status;
    }

    public String getLastRun() {
      return lastRun;
    }

    public String getNextRun() {
      return nextRun;
    }

    public String getDuration() {
      return duration;
    }
  }
}
