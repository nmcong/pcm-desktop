package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.core.di.Injector;
import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.ui.viewmodel.BatchJobsViewModel;
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

/** 
 * Batch Jobs page - MVVM Architecture
 * Uses BatchJobsViewModel for state management and business logic
 */
@Slf4j
public class BatchJobsPage extends BasePage {

  private final BatchJobsViewModel viewModel;
  private TableView<BatchJobsViewModel.JobEntry> jobsTable;
  private Label totalJobsLabel;
  private Label runningJobsLabel;
  private Label failedJobsLabel;
  private Label lastRefreshLabel;

  public BatchJobsPage() {
    super(
        I18n.get("page.jobs.title"),
        I18n.get("page.jobs.subtitle"),
        new FontIcon(Feather.CLOCK));
    this.viewModel = Injector.getInstance().get(BatchJobsViewModel.class);
    log.debug("BatchJobsPage initialized with ViewModel");
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

    // Total jobs stat
    VBox totalCard = createStatCard(I18n.get("jobs.stat.total"), Feather.BRIEFCASE, "info");
    totalJobsLabel = (Label) ((VBox) totalCard.getChildren().get(1)).getChildren().get(0);
    totalJobsLabel.textProperty().bind(viewModel.totalJobsProperty().asString());

    // Running jobs stat
    VBox runningCard = createStatCard(I18n.get("jobs.stat.running"), Feather.PLAY_CIRCLE, "success");
    runningJobsLabel = (Label) ((VBox) runningCard.getChildren().get(1)).getChildren().get(0);
    runningJobsLabel.textProperty().bind(viewModel.runningJobsProperty().asString());

    // Failed jobs stat
    VBox failedCard = createStatCard(I18n.get("jobs.stat.failed"), Feather.ALERT_CIRCLE, "danger");
    failedJobsLabel = (Label) ((VBox) failedCard.getChildren().get(1)).getChildren().get(0);
    failedJobsLabel.textProperty().bind(viewModel.failedJobsProperty().asString());

    // Last refresh stat
    VBox refreshCard = createStatCard(I18n.get("jobs.stat.refresh"), Feather.CLOCK, "info");
    lastRefreshLabel = (Label) ((VBox) refreshCard.getChildren().get(1)).getChildren().get(0);
    lastRefreshLabel.textProperty().bind(viewModel.lastRefreshTimeProperty());

    statsRow.getChildren().addAll(totalCard, runningCard, failedCard, refreshCard);
    return statsRow;
  }

  private VBox createStatCard(String title, Feather icon, String type) {
    VBox card = new VBox(8);
    card.getStyleClass().addAll("card", "stat-card", "stat-" + type);
    card.setPadding(new Insets(20));
    card.setAlignment(Pos.CENTER);
    HBox.setHgrow(card, Priority.ALWAYS);

    FontIcon iconNode = new FontIcon(icon);
    iconNode.setIconSize(24);
    iconNode.getStyleClass().add("stat-icon");

    VBox valueContainer = new VBox();
    valueContainer.setAlignment(Pos.CENTER);
    Label valueLabel = new Label();
    valueLabel.getStyleClass().addAll(Styles.TITLE_2, "stat-value");

    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    valueContainer.getChildren().add(valueLabel);
    card.getChildren().addAll(iconNode, valueContainer, titleLabel);
    return card;
  }

  private VBox createControlPanel() {
    VBox panel = new VBox(16);
    panel.getStyleClass().add("card");
    panel.setPadding(new Insets(20));

    HBox controlsRow = new HBox(12);
    controlsRow.setAlignment(Pos.CENTER_LEFT);

    Button refreshButton = new Button(I18n.get("common.refresh"));
    refreshButton.setGraphic(new FontIcon(Feather.REFRESH_CW));
    refreshButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
    refreshButton.setOnAction(e -> viewModel.loadJobs());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    TextField searchField = new TextField();
    searchField.setPromptText(I18n.get("jobs.search.placeholder"));
    searchField.setPrefWidth(200);

    controlsRow.getChildren().addAll(refreshButton, spacer, searchField);

    panel.getChildren().add(controlsRow);
    return panel;
  }

  private VBox createJobsTable() {
    VBox tableSection = new VBox(12);
    tableSection.getStyleClass().add("card");
    tableSection.setPadding(new Insets(20));
    VBox.setVgrow(tableSection, Priority.ALWAYS);

    Label tableTitle = new Label(I18n.get("jobs.table.title"));
    tableTitle.getStyleClass().addAll(Styles.TITLE_3);

    jobsTable = new TableView<>();
    jobsTable.getStyleClass().add("jobs-table");
    VBox.setVgrow(jobsTable, Priority.ALWAYS);
    
    // Bind to ViewModel's observable list
    jobsTable.setItems(viewModel.getJobList());

    // Create columns with property bindings
    TableColumn<BatchJobsViewModel.JobEntry, String> nameColumn = new TableColumn<>(I18n.get("jobs.col.name"));
    nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
    nameColumn.setPrefWidth(200);

    TableColumn<BatchJobsViewModel.JobEntry, String> statusColumn = new TableColumn<>(I18n.get("jobs.col.status"));
    statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
    statusColumn.setPrefWidth(100);

    TableColumn<BatchJobsViewModel.JobEntry, String> lastRunColumn = new TableColumn<>(I18n.get("jobs.col.lastrun"));
    lastRunColumn.setCellValueFactory(data -> data.getValue().lastRunProperty());
    lastRunColumn.setPrefWidth(150);

    TableColumn<BatchJobsViewModel.JobEntry, String> descColumn = new TableColumn<>(I18n.get("jobs.col.description"));
    descColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());
    descColumn.setPrefWidth(250);

    TableColumn<BatchJobsViewModel.JobEntry, Void> actionsColumn = new TableColumn<>(I18n.get("jobs.col.actions"));
    actionsColumn.setPrefWidth(150);
    actionsColumn.setCellFactory(
        column ->
            new TableCell<>() {
              private final Button startButton = new Button(I18n.get("jobs.action.start"));
              private final Button stopButton = new Button(I18n.get("jobs.action.stop"));
              private final HBox buttonsBox = new HBox(8, startButton, stopButton);

              {
                startButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
                stopButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
              }

              @Override
              protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                  setGraphic(null);
                } else {
                  BatchJobsViewModel.JobEntry job = getTableRow().getItem();
                  if (job != null) {
                    startButton.setOnAction(e -> viewModel.startJob(job));
                    stopButton.setOnAction(e -> viewModel.stopJob(job));
                  }
                  setGraphic(buttonsBox);
                }
              }
            });

    jobsTable.getColumns().add(nameColumn);
    jobsTable.getColumns().add(statusColumn);
    jobsTable.getColumns().add(lastRunColumn);
    jobsTable.getColumns().add(descColumn);
    jobsTable.getColumns().add(actionsColumn);

    tableSection.getChildren().addAll(tableTitle, jobsTable);
    return tableSection;
  }

  @Override
  public void onPageActivated() {
    super.onPageActivated();
    viewModel.loadJobs();
  }
}
