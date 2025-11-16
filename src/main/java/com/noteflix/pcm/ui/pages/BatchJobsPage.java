package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.core.di.Injector;
import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.UIFactory;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.viewmodel.BatchJobsViewModel;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.octicons.Octicons;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Batch Jobs page - Modern MVVM Architecture
 * 
 * Uses BatchJobsViewModel for state management and business logic.
 * Refactored to follow UI_GUIDE.md best practices with:
 * - BaseView template pattern
 * - UI constants and factory methods
 * - Proper component structure
 */
@Slf4j
public class BatchJobsPage extends BaseView {

  private final BatchJobsViewModel viewModel;
  private TableView<BatchJobsViewModel.JobEntry> jobsTable;
  private Label totalJobsLabel;
  private Label runningJobsLabel;
  private Label failedJobsLabel;
  private Label lastRefreshLabel;

  public BatchJobsPage() {
    // Initialize viewModel BEFORE calling super() to avoid null in createMainContent()
    this(initializeViewModel());
  }
  
  private BatchJobsPage(BatchJobsViewModel viewModel) {
    super(
        I18n.get("page.jobs.title"), 
        I18n.get("page.jobs.subtitle"), 
        new FontIcon(Octicons.CLOCK_24)
    );
    
    this.viewModel = viewModel;
    log.debug("BatchJobsPage initialized with ViewModel");
  }
  
  private static BatchJobsViewModel initializeViewModel() {
    try {
      BatchJobsViewModel vm = Injector.getInstance().get(BatchJobsViewModel.class);
      if (vm == null) {
        log.error("Failed to inject BatchJobsViewModel - got null");
        throw new RuntimeException("BatchJobsViewModel injection failed");
      }
      return vm;
    } catch (Exception e) {
      log.error("Failed to initialize BatchJobsViewModel", e);
      throw new RuntimeException("Failed to initialize BatchJobsViewModel", e);
    }
  }

  @Override
  protected Node createMainContent() {
    // Safety check for viewModel
    if (viewModel == null) {
      log.error("ViewModel is null when creating main content");
      VBox errorContainer = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
      errorContainer.getChildren().add(UIFactory.createMutedLabel("Error: Unable to load batch jobs view"));
      return errorContainer;
    }
    
    VBox mainContent = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
    mainContent.getStyleClass().add(StyleConstants.PAGE_CONTAINER);
    LayoutHelper.setVGrow(mainContent);

    // Statistics section
    mainContent.getChildren().add(createStatsSection());

    // Control panel
    mainContent.getChildren().add(createControlPanel());

    // Jobs table
    mainContent.getChildren().add(createJobsTable());

    return mainContent;
  }

  private HBox createStatsSection() {
    HBox statsRow = LayoutHelper.createHBox(Pos.CENTER, LayoutConstants.SPACING_LG);

    // Total jobs stat
    VBox totalCard = createStatCard(I18n.get("jobs.stat.total"), Octicons.BRIEFCASE_24, "info");
    totalJobsLabel = (Label) ((VBox) totalCard.getChildren().get(1)).getChildren().get(0);
    if (viewModel != null && viewModel.totalJobsProperty() != null) {
      totalJobsLabel.textProperty().bind(viewModel.totalJobsProperty().asString());
    } else {
      totalJobsLabel.setText("0");
    }

    // Running jobs stat
    VBox runningCard = createStatCard(I18n.get("jobs.stat.running"), Octicons.PLAY_24, "success");
    runningJobsLabel = (Label) ((VBox) runningCard.getChildren().get(1)).getChildren().get(0);
    if (viewModel != null && viewModel.runningJobsProperty() != null) {
      runningJobsLabel.textProperty().bind(viewModel.runningJobsProperty().asString());
    } else {
      runningJobsLabel.setText("0");
    }

    // Failed jobs stat
    VBox failedCard = createStatCard(I18n.get("jobs.stat.failed"), Octicons.ALERT_24, "danger");
    failedJobsLabel = (Label) ((VBox) failedCard.getChildren().get(1)).getChildren().get(0);
    if (viewModel != null && viewModel.failedJobsProperty() != null) {
      failedJobsLabel.textProperty().bind(viewModel.failedJobsProperty().asString());
    } else {
      failedJobsLabel.setText("0");
    }

    // Last refresh stat
    VBox refreshCard = createStatCard(I18n.get("jobs.stat.refresh"), Octicons.CLOCK_24, "info");
    lastRefreshLabel = (Label) ((VBox) refreshCard.getChildren().get(1)).getChildren().get(0);
    if (viewModel != null && viewModel.lastRefreshTimeProperty() != null) {
      lastRefreshLabel.textProperty().bind(viewModel.lastRefreshTimeProperty());
    } else {
      lastRefreshLabel.setText("-");
    }

    statsRow.getChildren().addAll(totalCard, runningCard, failedCard, refreshCard);
    return statsRow;
  }

  private VBox createStatCard(String title, Octicons icon, String type) {
    VBox card = UIFactory.createCard();
    card.getStyleClass().addAll("stat-card", "stat-" + type);
    card.setPadding(LayoutConstants.PADDING_DEFAULT);
    card.setAlignment(Pos.CENTER);
    LayoutHelper.setHGrow(card);

    FontIcon iconNode = new FontIcon(icon);
    iconNode.setIconSize(LayoutConstants.ICON_SIZE_LG);
    iconNode.getStyleClass().add("stat-icon");

    VBox valueContainer = LayoutHelper.createVBox(LayoutConstants.SPACING_XS);
    valueContainer.setAlignment(Pos.CENTER);
    Label valueLabel = new Label();
    valueLabel.getStyleClass().addAll(Styles.TITLE_2, "stat-value");

    Label titleLabel = UIFactory.createMutedLabel(title);
    titleLabel.getStyleClass().add(Styles.TEXT_SMALL);

    valueContainer.getChildren().add(valueLabel);
    card.getChildren().addAll(iconNode, valueContainer, titleLabel);
    return card;
  }

  private VBox createControlPanel() {
    VBox panel = UIFactory.createCard();
    panel.setPadding(LayoutConstants.PADDING_DEFAULT);

    HBox controlsRow = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_MD);

    Button refreshButton = UIFactory.createSecondaryButton(
        I18n.get("common.refresh"), 
        () -> {
          if (viewModel != null) {
            viewModel.loadJobs();
          }
        }
    );
    refreshButton.setGraphic(new FontIcon(Octicons.SYNC_24));

    Region spacer = UIFactory.createHorizontalSpacer();

    TextField searchField = new TextField();
    searchField.setPromptText(I18n.get("jobs.search.placeholder"));
    searchField.setPrefWidth(200);
    searchField.getStyleClass().add(StyleConstants.SEARCH_INPUT);

    controlsRow.getChildren().addAll(refreshButton, spacer, searchField);
    panel.getChildren().add(controlsRow);
    return panel;
  }

  private VBox createJobsTable() {
    VBox tableSection = UIFactory.createCard();
    tableSection.setPadding(LayoutConstants.PADDING_DEFAULT);
    LayoutHelper.setVGrow(tableSection);

    Label tableTitle = UIFactory.createSectionTitle(I18n.get("jobs.table.title"));

    jobsTable = new TableView<>();
    jobsTable.getStyleClass().add("jobs-table");
    LayoutHelper.setVGrow(jobsTable);

    // Bind to ViewModel's observable list
    if (viewModel != null && viewModel.getJobList() != null) {
      jobsTable.setItems(viewModel.getJobList());
    }

    // Create columns with property bindings
    TableColumn<BatchJobsViewModel.JobEntry, String> nameColumn =
        new TableColumn<>(I18n.get("jobs.col.name"));
    nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
    nameColumn.setPrefWidth(200);

    TableColumn<BatchJobsViewModel.JobEntry, String> statusColumn =
        new TableColumn<>(I18n.get("jobs.col.status"));
    statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
    statusColumn.setPrefWidth(100);

    TableColumn<BatchJobsViewModel.JobEntry, String> lastRunColumn =
        new TableColumn<>(I18n.get("jobs.col.lastrun"));
    lastRunColumn.setCellValueFactory(data -> data.getValue().lastRunProperty());
    lastRunColumn.setPrefWidth(150);

    TableColumn<BatchJobsViewModel.JobEntry, String> descColumn =
        new TableColumn<>(I18n.get("jobs.col.description"));
    descColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());
    descColumn.setPrefWidth(250);

    TableColumn<BatchJobsViewModel.JobEntry, Void> actionsColumn =
        new TableColumn<>(I18n.get("jobs.col.actions"));
    actionsColumn.setPrefWidth(150);
    actionsColumn.setCellFactory(
        column ->
            new TableCell<>() {
              private final Button startButton = new Button(I18n.get("jobs.action.start"));
              private final Button stopButton = new Button(I18n.get("jobs.action.stop"));
              private final HBox buttonsBox = new HBox(8, startButton, stopButton);

              {
                buttonsBox.setSpacing(LayoutConstants.SPACING_SM);
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
                  if (job != null && viewModel != null) {
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
  public void onActivate() {
    super.onActivate();
    if (viewModel != null) {
      viewModel.loadJobs();
    } else {
      log.warn("Cannot load jobs - viewModel is null");
    }
  }
}
