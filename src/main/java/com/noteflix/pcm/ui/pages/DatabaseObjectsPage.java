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

/**
 * Database Objects page - Single Responsibility Principle
 * Manages database schema and object visualization
 */
@Slf4j
public class DatabaseObjectsPage extends BasePage {
    
    private TreeView<String> schemaTree;
    private VBox objectDetails;
    
    public DatabaseObjectsPage() {
        super(
            "Database Objects",
            "Explore and manage your database schema, tables, views, and procedures",
            new FontIcon(Feather.DATABASE)
        );
    }
    
    @Override
    protected VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.getStyleClass().add("database-objects-content");
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Database connection info
        mainContent.getChildren().add(createConnectionInfo());
        
        // Schema explorer
        mainContent.getChildren().add(createSchemaExplorer());
        
        return mainContent;
    }
    
    private VBox createConnectionInfo() {
        VBox section = new VBox(12);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label title = new Label("Database Connection");
        title.getStyleClass().addAll(Styles.TITLE_3);
        
        HBox connectionRow = new HBox(16);
        connectionRow.setAlignment(Pos.CENTER_LEFT);
        
        VBox connectionInfo = new VBox(4);
        Label serverLabel = new Label("Server: production-db.company.com:1521");
        serverLabel.getStyleClass().addAll(Styles.TEXT_BOLD);
        Label schemaLabel = new Label("Schema: PCM_PROD");
        schemaLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        connectionInfo.getChildren().addAll(serverLabel, schemaLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusLabel = new Label("Connected");
        statusLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "status-connected");
        
        Button refreshButton = new Button("Refresh Schema");
        refreshButton.setGraphic(new FontIcon(Feather.REFRESH_CW));
        refreshButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        refreshButton.setOnAction(e -> handleRefreshSchema());
        
        connectionRow.getChildren().addAll(connectionInfo, spacer, statusLabel, refreshButton);
        
        section.getChildren().addAll(title, connectionRow);
        return section;
    }
    
    private HBox createSchemaExplorer() {
        HBox explorer = new HBox(16);
        explorer.getStyleClass().add("schema-explorer");
        HBox.setHgrow(explorer, Priority.ALWAYS);
        
        // Left panel - Schema tree
        VBox treePanel = createSchemaTreePanel();
        treePanel.setPrefWidth(300);
        
        // Right panel - Object details
        VBox detailsPanel = createObjectDetailsPanel();
        HBox.setHgrow(detailsPanel, Priority.ALWAYS);
        
        explorer.getChildren().addAll(treePanel, detailsPanel);
        return explorer;
    }
    
    private VBox createSchemaTreePanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(20));
        
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Schema Objects");
        title.getStyleClass().addAll(Styles.TITLE_4);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search objects...");
        searchField.setPrefWidth(120);
        
        headerRow.getChildren().addAll(title, spacer, searchField);
        
        // Create schema tree
        schemaTree = new TreeView<>();
        schemaTree.getStyleClass().add("schema-tree");
        VBox.setVgrow(schemaTree, Priority.ALWAYS);
        
        TreeItem<String> root = new TreeItem<>("PCM_PROD");
        root.setExpanded(true);
        
        // Tables
        TreeItem<String> tables = new TreeItem<>("Tables (45)");
        tables.setGraphic(new FontIcon(Feather.GRID));
        tables.getChildren().add(createTableItem("USERS", "Table"));
        tables.getChildren().add(createTableItem("PROJECTS", "Table"));
        tables.getChildren().add(createTableItem("SCREENS", "Table"));
        tables.getChildren().add(createTableItem("WORKFLOWS", "Table"));
        tables.getChildren().add(createTableItem("AUDIT_LOG", "Table"));
        
        // Views
        TreeItem<String> views = new TreeItem<>("Views (12)");
        views.setGraphic(new FontIcon(Feather.EYE));
        views.getChildren().add(createTableItem("V_USER_PROJECTS", "View"));
        views.getChildren().add(createTableItem("V_ACTIVE_WORKFLOWS", "View"));
        views.getChildren().add(createTableItem("V_PROJECT_STATS", "View"));
        
        // Procedures
        TreeItem<String> procedures = new TreeItem<>("Procedures (28)");
        procedures.setGraphic(new FontIcon(Feather.SETTINGS));
        procedures.getChildren().add(createTableItem("SP_CREATE_PROJECT", "Procedure"));
        procedures.getChildren().add(createTableItem("SP_UPDATE_WORKFLOW", "Procedure"));
        procedures.getChildren().add(createTableItem("SP_GENERATE_REPORT", "Procedure"));
        
        // Functions
        TreeItem<String> functions = new TreeItem<>("Functions (15)");
        functions.setGraphic(new FontIcon(Feather.ZAP));
        functions.getChildren().add(createTableItem("FN_CALCULATE_METRICS", "Function"));
        functions.getChildren().add(createTableItem("FN_VALIDATE_USER", "Function"));
        
        // Sequences
        TreeItem<String> sequences = new TreeItem<>("Sequences (8)");
        sequences.setGraphic(new FontIcon(Feather.HASH));
        sequences.getChildren().add(createTableItem("SEQ_USER_ID", "Sequence"));
        sequences.getChildren().add(createTableItem("SEQ_PROJECT_ID", "Sequence"));
        
        root.getChildren().add(tables);
        root.getChildren().add(views);
        root.getChildren().add(procedures);
        root.getChildren().add(functions);
        root.getChildren().add(sequences);
        schemaTree.setRoot(root);
        
        // Handle selection
        schemaTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleObjectSelection(newVal.getValue());
            }
        });
        
        panel.getChildren().addAll(headerRow, schemaTree);
        return panel;
    }
    
    private TreeItem<String> createTableItem(String name, String type) {
        TreeItem<String> item = new TreeItem<>(name);
        FontIcon icon = switch (type) {
            case "Table" -> new FontIcon(Feather.GRID);
            case "View" -> new FontIcon(Feather.EYE);
            case "Procedure" -> new FontIcon(Feather.SETTINGS);
            case "Function" -> new FontIcon(Feather.ZAP);
            case "Sequence" -> new FontIcon(Feather.HASH);
            default -> new FontIcon(Feather.CIRCLE);
        };
        item.setGraphic(icon);
        return item;
    }
    
    private VBox createObjectDetailsPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(20));
        HBox.setHgrow(panel, Priority.ALWAYS);
        
        Label title = new Label("Object Details");
        title.getStyleClass().addAll(Styles.TITLE_4);
        
        objectDetails = new VBox(16);
        objectDetails.getStyleClass().add("object-details");
        VBox.setVgrow(objectDetails, Priority.ALWAYS);
        
        // Default content
        showWelcomeMessage();
        
        panel.getChildren().addAll(title, objectDetails);
        return panel;
    }
    
    private void showWelcomeMessage() {
        objectDetails.getChildren().clear();
        
        VBox welcome = new VBox(16);
        welcome.setAlignment(Pos.CENTER);
        welcome.getStyleClass().add("welcome-message");
        
        FontIcon icon = new FontIcon(Feather.DATABASE);
        icon.setIconSize(48);
        icon.getStyleClass().add("welcome-icon");
        
        Label message = new Label("Select an object from the schema tree to view its details");
        message.getStyleClass().addAll(Styles.TEXT_MUTED);
        message.setAlignment(Pos.CENTER);
        
        welcome.getChildren().addAll(icon, message);
        objectDetails.getChildren().add(welcome);
    }
    
    private void showObjectDetails(String objectName, String objectType) {
        objectDetails.getChildren().clear();
        
        // Object header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon typeIcon = getObjectIcon(objectType);
        typeIcon.setIconSize(20);
        
        Label nameLabel = new Label(objectName);
        nameLabel.getStyleClass().addAll(Styles.TITLE_3);
        
        Label typeLabel = new Label(objectType);
        typeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "object-type-badge");
        
        header.getChildren().addAll(typeIcon, nameLabel, typeLabel);
        
        // Tabs for different views
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("object-tabs");
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        if (objectType.equals("Table")) {
            tabPane.getTabs().addAll(
                createColumnsTab(objectName),
                createIndexesTab(objectName),
                createConstraintsTab(objectName),
                createDataTab(objectName)
            );
        } else {
            tabPane.getTabs().addAll(
                createDefinitionTab(objectName),
                createDependenciesTab(objectName)
            );
        }
        
        objectDetails.getChildren().addAll(header, tabPane);
    }
    
    private FontIcon getObjectIcon(String objectType) {
        return switch (objectType) {
            case "Table" -> new FontIcon(Feather.GRID);
            case "View" -> new FontIcon(Feather.EYE);
            case "Procedure" -> new FontIcon(Feather.SETTINGS);
            case "Function" -> new FontIcon(Feather.ZAP);
            case "Sequence" -> new FontIcon(Feather.HASH);
            default -> new FontIcon(Feather.CIRCLE);
        };
    }
    
    private Tab createColumnsTab(String tableName) {
        Tab tab = new Tab("Columns");
        
        TableView<ColumnInfo> columnsTable = new TableView<>();
        
        TableColumn<ColumnInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        
        TableColumn<ColumnInfo, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
        
        TableColumn<ColumnInfo, String> nullableCol = new TableColumn<>("Nullable");
        nullableCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNullable()));
        
        TableColumn<ColumnInfo, String> defaultCol = new TableColumn<>("Default");
        defaultCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDefaultValue()));
        
        columnsTable.getColumns().add(nameCol);
        columnsTable.getColumns().add(typeCol);
        columnsTable.getColumns().add(nullableCol);
        columnsTable.getColumns().add(defaultCol);
        
        // Sample data
        columnsTable.getItems().add(new ColumnInfo("ID", "NUMBER(10)", "NO", "SEQ_" + tableName + "_ID.NEXTVAL"));
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
        TextArea definition = new TextArea("CREATE OR REPLACE PROCEDURE " + objectName + "\nAS\nBEGIN\n  -- Procedure body here\nEND;");
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
    
    private void handleObjectSelection(String selectedObject) {
        log.info("Selected object: {}", selectedObject);
        
        // Determine object type and show details
        if (selectedObject.startsWith("USERS") || selectedObject.startsWith("PROJECTS") || 
            selectedObject.startsWith("SCREENS") || selectedObject.startsWith("WORKFLOWS") || 
            selectedObject.startsWith("AUDIT_LOG")) {
            showObjectDetails(selectedObject, "Table");
        } else if (selectedObject.startsWith("V_")) {
            showObjectDetails(selectedObject, "View");
        } else if (selectedObject.startsWith("SP_")) {
            showObjectDetails(selectedObject, "Procedure");
        } else if (selectedObject.startsWith("FN_")) {
            showObjectDetails(selectedObject, "Function");
        } else if (selectedObject.startsWith("SEQ_")) {
            showObjectDetails(selectedObject, "Sequence");
        }
    }
    
    private void handleRefreshSchema() {
        log.info("Refreshing database schema");
        // Refresh schema tree
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
        
        public String getName() { return name; }
        public String getType() { return type; }
        public String getNullable() { return nullable; }
        public String getDefaultValue() { return defaultValue; }
    }
}