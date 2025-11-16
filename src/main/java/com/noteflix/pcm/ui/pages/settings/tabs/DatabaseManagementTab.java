package com.noteflix.pcm.ui.pages.settings.tabs;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.infrastructure.database.ConnectionManager;
import com.noteflix.pcm.infrastructure.database.DatabaseMigrationManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Management Tab
 *
 * <p>Provides UI for manual database operations:
 * - View migration status
 * - Run pending migrations
 * - View database info
 * - Backup/restore (future)
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class DatabaseManagementTab extends VBox {

    private final DatabaseMigrationManager migrationManager;
    private final ConnectionManager connectionManager;

    // UI components
    private Label statusLabel;
    private ListView<String> appliedMigrationsList;
    private ListView<String> pendingMigrationsList;
    private Button runMigrationsButton;
    private TextArea logArea;
    private ProgressBar progressBar;

    public DatabaseManagementTab() {
        this.connectionManager = ConnectionManager.INSTANCE;
        this.migrationManager = new DatabaseMigrationManager(connectionManager);

        setSpacing(0);
        setPadding(new Insets(0));
        getStyleClass().add("database-management-tab");

        // Build UI
        buildUI();

        // Load initial data
        refreshStatus();
    }

    private void buildUI() {
        // Header
        VBox header = createHeader();

        // Content
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("database-content");

        // Status section
        VBox statusSection = createStatusSection();

        // Migrations section
        HBox migrationsSection = createMigrationsSection();

        // Actions section
        VBox actionsSection = createActionsSection();

        // Log section
        VBox logSection = createLogSection();

        content.getChildren().addAll(statusSection, migrationsSection, actionsSection, logSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("database-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, scrollPane);
    }

    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(20, 20, 16, 20));
        header.getStyleClass().add("tab-header");

        Label title = new Label("Database Management");
        title.getStyleClass().addAll(Styles.TITLE_3);

        Label subtitle = new Label(
                "Manage database migrations and view database status"
        );
        subtitle.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        subtitle.setWrapText(true);

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox createStatusSection() {
        VBox section = new VBox(12);
        section.getStyleClass().add("db-section");

        Label sectionTitle = new Label("Database Status");
        sectionTitle.getStyleClass().addAll(Styles.TITLE_4);

        HBox statusBox = new HBox(12);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.getStyleClass().add("status-box");
        statusBox.setPadding(new Insets(12));

        FontIcon statusIcon = new FontIcon(Octicons.DATABASE_24);
        statusIcon.setIconSize(24);
        statusIcon.getStyleClass().add("status-icon");

        statusLabel = new Label("Loading...");
        statusLabel.getStyleClass().addAll(Styles.TEXT);

        Button refreshButton = new Button();
        refreshButton.setGraphic(new FontIcon(Octicons.SYNC_16));
        refreshButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        refreshButton.setTooltip(new Tooltip("Refresh status"));
        refreshButton.setOnAction(e -> refreshStatus());

        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        statusBox.getChildren().addAll(statusIcon, statusLabel, refreshButton);

        section.getChildren().addAll(sectionTitle, statusBox);
        return section;
    }

    private HBox createMigrationsSection() {
        HBox section = new HBox(16);
        section.getStyleClass().add("migrations-section");

        // Applied migrations
        VBox appliedBox = createMigrationListBox(
                "Applied Migrations",
                Octicons.CHECK_CIRCLE_24,
                true
        );

        // Pending migrations
        VBox pendingBox = createMigrationListBox(
                "Pending Migrations",
                Octicons.CLOCK_24,
                false
        );

        HBox.setHgrow(appliedBox, Priority.ALWAYS);
        HBox.setHgrow(pendingBox, Priority.ALWAYS);

        section.getChildren().addAll(appliedBox, pendingBox);
        return section;
    }

    private VBox createMigrationListBox(String title, Octicons icon, boolean isApplied) {
        VBox box = new VBox(8);
        box.getStyleClass().add("migration-list-box");

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon titleIcon = new FontIcon(icon);
        titleIcon.setIconSize(16);
        if (isApplied) {
            titleIcon.getStyleClass().add("success");
        } else {
            titleIcon.getStyleClass().add("warning");
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD);

        titleBox.getChildren().addAll(titleIcon, titleLabel);

        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(150);
        listView.getStyleClass().add("migration-list");

        if (isApplied) {
            appliedMigrationsList = listView;
        } else {
            pendingMigrationsList = listView;
        }

        box.getChildren().addAll(titleBox, listView);
        return box;
    }

    private VBox createActionsSection() {
        VBox section = new VBox(12);
        section.getStyleClass().add("actions-section");

        Label sectionTitle = new Label("Migration Actions");
        sectionTitle.getStyleClass().addAll(Styles.TITLE_4);

        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setPadding(new Insets(12));
        actionsBox.getStyleClass().add("actions-box");

        runMigrationsButton = new Button("Run Pending Migrations");
        runMigrationsButton.setGraphic(new FontIcon(Octicons.PLAY_24));
        runMigrationsButton.getStyleClass().addAll(Styles.ACCENT, Styles.LARGE);
        runMigrationsButton.setOnAction(e -> runMigrations());

        Button backupButton = new Button("Backup Database");
        backupButton.setGraphic(new FontIcon(Octicons.DOWNLOAD_16));
        backupButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        backupButton.setTooltip(new Tooltip("Coming soon"));
        backupButton.setDisable(true);

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        actionsBox.getChildren().addAll(runMigrationsButton, backupButton, progressBar);

        section.getChildren().addAll(sectionTitle, actionsBox);
        return section;
    }

    private VBox createLogSection() {
        VBox section = new VBox(8);
        section.getStyleClass().add("log-section");

        Label sectionTitle = new Label("Migration Log");
        sectionTitle.getStyleClass().addAll(Styles.TITLE_4);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(10);
        logArea.setWrapText(true);
        logArea.getStyleClass().add("migration-log");
        logArea.setPromptText("Migration logs will appear here...");

        section.getChildren().addAll(sectionTitle, logArea);
        return section;
    }

    private void refreshStatus() {
        try {
            // Get applied migrations
            List<String> applied = getAppliedMigrations();
            appliedMigrationsList.getItems().setAll(applied);

            // Get pending migrations
            List<String> all = List.of(
                    "V1__init_database.sql"
            );
            List<String> pending = new ArrayList<>();
            for (String migration : all) {
                String version = extractVersion(migration);
                if (!applied.contains(version)) {
                    pending.add(migration);
                }
            }
            pendingMigrationsList.getItems().setAll(pending);

            // Update status
            String status = String.format(
                    "Database: pcm-desktop.db | Applied: %d migrations | Pending: %d migrations",
                    applied.size(),
                    pending.size()
            );
            statusLabel.setText(status);
            statusLabel.getStyleClass().removeAll("success", "warning");
            if (pending.isEmpty()) {
                statusLabel.getStyleClass().add("success");
            } else {
                statusLabel.getStyleClass().add("warning");
            }

            // Update button state
            runMigrationsButton.setDisable(pending.isEmpty());

            log.info("Refreshed database status: {} applied, {} pending", applied.size(), pending.size());

        } catch (Exception e) {
            log.error("Failed to refresh database status", e);
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.getStyleClass().add("danger");
        }
    }

    private void runMigrations() {
        runMigrationsButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Indeterminate

        logArea.clear();
        appendLog("🔄 Starting database migration...\n");

        // Run in background
        new Thread(() -> {
            try {
                migrationManager.migrate();
                Platform.runLater(() -> {
                    appendLog("✅ Migration completed successfully!\n");
                    progressBar.setProgress(1.0);
                    refreshStatus();
                    showSuccess("Migration completed successfully!");
                });
            } catch (Exception e) {
                log.error("Migration failed", e);
                Platform.runLater(() -> {
                    appendLog("❌ Migration failed: " + e.getMessage() + "\n");
                    progressBar.setProgress(0);
                    showError("Migration failed: " + e.getMessage());
                });
            } finally {
                Platform.runLater(() -> {
                    runMigrationsButton.setDisable(false);
                    // Hide progress bar after 2 seconds
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            Platform.runLater(() -> progressBar.setVisible(false));
                        } catch (InterruptedException ignored) {}
                    }).start();
                });
            }
        }).start();
    }

    private List<String> getAppliedMigrations() throws Exception {
        List<String> applied = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT version FROM schema_version ORDER BY version")) {

            while (rs.next()) {
                applied.add(rs.getString("version"));
            }
        } catch (Exception e) {
            // Table might not exist yet
            log.debug("Could not read applied migrations: {}", e.getMessage());
        }
        return applied;
    }

    private String extractVersion(String filename) {
        int endIndex = filename.indexOf("__");
        if (endIndex == -1) {
            return filename;
        }
        return filename.substring(0, endIndex);
    }

    private void appendLog(String message) {
        logArea.appendText(message);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

