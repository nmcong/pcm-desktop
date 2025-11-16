package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.core.di.Injector;
import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.UIFactory;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.viewmodel.DatabaseObjectsViewModel;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.octicons.Octicons;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Database Objects page - Modern MVVM Architecture
 * 
 * Uses DatabaseObjectsViewModel for state management and business logic.
 * Refactored to follow UI_GUIDE.md best practices with:
 * - BaseView template pattern
 * - UI constants and factory methods
 * - Proper component structure
 */
@Slf4j
public class DatabaseObjectsPage extends BaseView {

  private final DatabaseObjectsViewModel viewModel;
  private TreeView<String> schemaTree;
  private VBox objectDetails;
  private Label connectionStatusLabel;
  private Label databaseNameLabel;
  private Label schemaVersionLabel;

  public DatabaseObjectsPage() {
    this(initializeViewModel());
  }
  
  private DatabaseObjectsPage(DatabaseObjectsViewModel viewModel) {
    super(
        I18n.get("page.db.title"), 
        I18n.get("page.db.subtitle"), 
        new FontIcon(Octicons.DATABASE_24)
    );
    this.viewModel = viewModel;
    log.debug("DatabaseObjectsPage initialized with ViewModel");
  }
  
  private static DatabaseObjectsViewModel initializeViewModel() {
    try {
      DatabaseObjectsViewModel vm = Injector.getInstance().get(DatabaseObjectsViewModel.class);
      if (vm == null) {
        log.error("Failed to inject DatabaseObjectsViewModel - got null");
        throw new RuntimeException("DatabaseObjectsViewModel injection failed");
      }
      return vm;
    } catch (Exception e) {
      log.error("Failed to initialize DatabaseObjectsViewModel", e);
      throw new RuntimeException("Failed to initialize DatabaseObjectsViewModel", e);
    }
  }

  @Override
  protected Node createMainContent() {
    if (viewModel == null) {
      log.error("ViewModel is null when creating main content");
      VBox errorContainer = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
      errorContainer.getChildren().add(UIFactory.createMutedLabel("Error: Unable to load database objects view"));
      return errorContainer;
    }
    
    VBox mainContent = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
    mainContent.getStyleClass().add(StyleConstants.PAGE_CONTAINER);
    LayoutHelper.setVGrow(mainContent);

    // Database connection info
    mainContent.getChildren().add(createConnectionInfo());

    // Schema explorer
    mainContent.getChildren().add(createSchemaExplorer());

    return mainContent;
  }

  private VBox createConnectionInfo() {
    VBox section = UIFactory.createCard();
    section.setPadding(LayoutConstants.PADDING_DEFAULT);

    Label title = UIFactory.createSectionTitle(I18n.get("db.connection.title"));

    HBox connectionRow = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_LG);

    VBox connectionInfo = LayoutHelper.createVBox(LayoutConstants.SPACING_XS);
    databaseNameLabel = UIFactory.createBoldLabel("");
    if (viewModel != null && viewModel.databaseNameProperty() != null) {
      databaseNameLabel.textProperty().bind(viewModel.databaseNameProperty());
    } else {
      databaseNameLabel.setText("Database");
    }

    schemaVersionLabel = UIFactory.createMutedLabel("");
    schemaVersionLabel.getStyleClass().add(Styles.TEXT_SMALL);
    if (viewModel != null && viewModel.schemaVersionProperty() != null) {
      schemaVersionLabel.textProperty().bind(viewModel.schemaVersionProperty());
    } else {
      schemaVersionLabel.setText("Unknown version");
    }
    connectionInfo.getChildren().addAll(databaseNameLabel, schemaVersionLabel);

    Region spacer = UIFactory.createHorizontalSpacer();

    connectionStatusLabel = new Label();
    connectionStatusLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "status-connected");
    if (viewModel != null && viewModel.connectionStatusProperty() != null) {
      connectionStatusLabel.textProperty().bind(viewModel.connectionStatusProperty());
    } else {
      connectionStatusLabel.setText("Disconnected");
    }

    Button refreshButton = UIFactory.createSecondaryButton(
        I18n.get("db.refresh.button"), 
        () -> {
          if (viewModel != null) {
            viewModel.refreshSchema();
          }
        }
    );
    refreshButton.setGraphic(new FontIcon(Octicons.SYNC_24));

    connectionRow
        .getChildren()
        .addAll(connectionInfo, spacer, connectionStatusLabel, refreshButton);

    section.getChildren().addAll(title, connectionRow);
    return section;
  }

  private HBox createSchemaExplorer() {
    HBox explorer = LayoutHelper.createHBox(Pos.TOP_LEFT, LayoutConstants.SPACING_LG);
    explorer.getStyleClass().add("schema-explorer");
    LayoutHelper.setHGrow(explorer);

    // Left panel - Schema tree
    VBox treePanel = createSchemaTreePanel();
    treePanel.setPrefWidth(300);

    // Right panel - Object details
    VBox detailsPanel = createObjectDetailsPanel();
    LayoutHelper.setHGrow(detailsPanel);

    explorer.getChildren().addAll(treePanel, detailsPanel);
    return explorer;
  }

  private VBox createSchemaTreePanel() {
    VBox panel = UIFactory.createCard();
    panel.setPadding(LayoutConstants.PADDING_DEFAULT);

    HBox headerRow = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_SM);

    Label title = UIFactory.createSectionTitle(I18n.get("db.schema.objects"));

    Region spacer = UIFactory.createHorizontalSpacer();

    TextField searchField = new TextField();
    searchField.setPromptText(I18n.get("db.search.placeholder"));
    searchField.setPrefWidth(120);
    searchField.getStyleClass().add(StyleConstants.SEARCH_INPUT);

    headerRow.getChildren().addAll(title, spacer, searchField);

    // Create schema tree
    schemaTree = new TreeView<>();
    schemaTree.getStyleClass().add("schema-tree");
    LayoutHelper.setVGrow(schemaTree);

    // Bind schema objects from ViewModel
    updateSchemaTree();

    // Handle selection - notify ViewModel
    schemaTree
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (newVal != null && viewModel != null) {
                viewModel.selectSchemaObject(newVal.getValue());
              }
            });

    panel.getChildren().addAll(headerRow, schemaTree);
    return panel;
  }

  private void updateSchemaTree() {
    String dbName = (viewModel != null && viewModel.getDatabaseName() != null) 
        ? viewModel.getDatabaseName() 
        : "Database";
    TreeItem<String> root = new TreeItem<>(dbName);
    root.setExpanded(true);

    // Populate from ViewModel's observable list
    if (viewModel != null && viewModel.getSchemaObjects() != null) {
      for (String obj : viewModel.getSchemaObjects()) {
        TreeItem<String> item = new TreeItem<>(obj);
        root.getChildren().add(item);
      }
    }

    schemaTree.setRoot(root);
  }

  private TreeItem<String> createTableItem(String name, String type) {
    TreeItem<String> item = new TreeItem<>(name);
    FontIcon icon =
        switch (type) {
          case "Table" -> new FontIcon(Octicons.TABLE_24);
          case "View" -> new FontIcon(Octicons.EYE_24);
          case "Procedure" -> new FontIcon(Octicons.GEAR_24);
          case "Function" -> new FontIcon(Octicons.ZAP_24);
          case "Sequence" -> new FontIcon(Octicons.NUMBER_24);
          default -> new FontIcon(Octicons.DOT_24);
        };
    item.setGraphic(icon);
    return item;
  }

  private VBox createObjectDetailsPanel() {
    VBox panel = UIFactory.createCard();
    panel.setPadding(LayoutConstants.PADDING_DEFAULT);
    LayoutHelper.setHGrow(panel);

    Label title = UIFactory.createSectionTitle("Object Details");

    objectDetails = LayoutHelper.createVBox(LayoutConstants.SPACING_LG);
    objectDetails.getStyleClass().add("object-details");
    LayoutHelper.setVGrow(objectDetails);

    // Default content
    showWelcomeMessage();

    panel.getChildren().addAll(title, objectDetails);
    return panel;
  }

  private void showWelcomeMessage() {
    objectDetails.getChildren().clear();

    VBox welcome = LayoutHelper.createVBox(LayoutConstants.SPACING_LG);
    welcome.setAlignment(Pos.CENTER);
    welcome.getStyleClass().add("welcome-message");

    FontIcon icon = new FontIcon(Octicons.DATABASE_24);
    icon.setIconSize((int)(LayoutConstants.ICON_SIZE_XL * 1.5));
    icon.getStyleClass().add("welcome-icon");

    Label message = UIFactory.createMutedLabel("Select an object from the schema tree to view its details");
    message.setAlignment(Pos.CENTER);

    welcome.getChildren().addAll(icon, message);
    objectDetails.getChildren().add(welcome);
  }

  private void showObjectDetails(String objectName, String objectType) {
    objectDetails.getChildren().clear();

    // Object header
    HBox header = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_MD);

    FontIcon typeIcon = getObjectIcon(objectType);
    typeIcon.setIconSize(LayoutConstants.ICON_SIZE_MD);

    Label nameLabel = UIFactory.createSectionTitle(objectName);

    Label typeLabel = UIFactory.createMutedLabel(objectType);
    typeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "object-type-badge");

    header.getChildren().addAll(typeIcon, nameLabel, typeLabel);

    // Tabs for different views
    TabPane tabPane = new TabPane();
    tabPane.getStyleClass().add("object-tabs");
    LayoutHelper.setVGrow(tabPane);

    if (objectType.equals("Table")) {
      tabPane
          .getTabs()
          .addAll(
              createColumnsTab(objectName),
              createIndexesTab(objectName),
              createConstraintsTab(objectName),
              createDataTab(objectName));
    } else {
      tabPane.getTabs().addAll(createDefinitionTab(objectName), createDependenciesTab(objectName));
    }

    objectDetails.getChildren().addAll(header, tabPane);
  }

  private FontIcon getObjectIcon(String objectType) {
    return switch (objectType) {
      case "Table" -> new FontIcon(Octicons.TABLE_24);
      case "View" -> new FontIcon(Octicons.EYE_24);
      case "Procedure" -> new FontIcon(Octicons.GEAR_24);
      case "Function" -> new FontIcon(Octicons.ZAP_24);
      case "Sequence" -> new FontIcon(Octicons.NUMBER_24);
      default -> new FontIcon(Octicons.DOT_24);
    };
  }

  private Tab createColumnsTab(String tableName) {
    Tab tab = new Tab("Columns");

    TableView<ColumnInfo> columnsTable = new TableView<>();

    TableColumn<ColumnInfo, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(
        data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));

    TableColumn<ColumnInfo, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(
        data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));

    TableColumn<ColumnInfo, String> nullableCol = new TableColumn<>("Nullable");
    nullableCol.setCellValueFactory(
        data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNullable()));

    TableColumn<ColumnInfo, String> defaultCol = new TableColumn<>("Default");
    defaultCol.setCellValueFactory(
        data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDefaultValue()));

    columnsTable.getColumns().add(nameCol);
    columnsTable.getColumns().add(typeCol);
    columnsTable.getColumns().add(nullableCol);
    columnsTable.getColumns().add(defaultCol);

    // Sample data
    columnsTable
        .getItems()
        .add(new ColumnInfo("ID", "NUMBER(10)", "NO", "SEQ_" + tableName + "_ID.NEXTVAL"));
    columnsTable.getItems().add(new ColumnInfo("NAME", "VARCHAR2(100)", "NO", null));
    columnsTable.getItems().add(new ColumnInfo("DESCRIPTION", "VARCHAR2(500)", "YES", null));
    columnsTable.getItems().add(new ColumnInfo("CREATED_DATE", "TIMESTAMP", "NO", "SYSTIMESTAMP"));
    columnsTable.getItems().add(new ColumnInfo("UPDATED_DATE", "TIMESTAMP", "YES", null));

    tab.setContent(columnsTable);
    return tab;
  }

  private Tab createIndexesTab(String tableName) {
    Tab tab = new Tab("Indexes");
    Label content = new Label("Indexes for " + tableName);
    tab.setContent(content);
    return tab;
  }

  private Tab createConstraintsTab(String tableName) {
    Tab tab = new Tab("Constraints");
    Label content = new Label("Constraints for " + tableName);
    tab.setContent(content);
    return tab;
  }

  private Tab createDataTab(String tableName) {
    Tab tab = new Tab("Data Preview");
    Label content = new Label("Data preview for " + tableName);
    tab.setContent(content);
    return tab;
  }

  private Tab createDefinitionTab(String objectName) {
    Tab tab = new Tab("Definition");
    TextArea definition =
        new TextArea(
            "CREATE OR REPLACE PROCEDURE "
                + objectName
                + "\nAS\nBEGIN\n  -- Procedure body here\nEND;");
    definition.getStyleClass().add("sql-definition");
    definition.setWrapText(false);
    tab.setContent(definition);
    return tab;
  }

  private Tab createDependenciesTab(String objectName) {
    Tab tab = new Tab("Dependencies");
    Label content = new Label("Dependencies for " + objectName);
    tab.setContent(content);
    return tab;
  }

  @Override
  public void onActivate() {
    super.onActivate();
    if (viewModel != null) {
      viewModel.loadDatabaseInfo();
    } else {
      log.warn("Cannot load database info - viewModel is null");
    }
  }

  // Inner class for column information
  public static class ColumnInfo {
    private final String name;
    private final String type;
    private final String nullable;
    private final String defaultValue;

    public ColumnInfo(String name, String type, String nullable, String defaultValue) {
      this.name = name;
      this.type = type;
      this.nullable = nullable;
      this.defaultValue = defaultValue;
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public String getNullable() {
      return nullable;
    }

    public String getDefaultValue() {
      return defaultValue;
    }
  }
}
