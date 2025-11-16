package com.noteflix.pcm.ui.viewmodel;

import com.noteflix.pcm.core.i18n.I18n;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchJobsViewModel extends BaseViewModel {

    public final IntegerProperty totalJobs = new SimpleIntegerProperty(0);
    public final IntegerProperty runningJobs = new SimpleIntegerProperty(0);
    public final IntegerProperty failedJobs = new SimpleIntegerProperty(0);
    public final StringProperty lastRefreshTime = new SimpleStringProperty(I18n.get("common.na"));
    public final ObservableList<JobEntry> jobList = FXCollections.observableArrayList();

    public BatchJobsViewModel() {
        log.debug("BatchJobsViewModel initialized");
        // Initialize with some default data to avoid empty state
        loadFallbackData();
    }

    public void loadJobs() {
        setBusy(true);
        setErrorMessage(null);
        log.info("Loading batch jobs...");

        runAsync(
                () -> {
                    try {
                        Thread.sleep(800); // Simulate network/DB call (reduced time)

                        // Simulate very rare failure (0.5% instead of 5%)
                        if (Math.random() < 0.005) {
                            throw new RuntimeException("Failed to load jobs from server.");
                        }

                        // Create mock data with current timestamp
                        String currentTime = java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        String yesterdayTime = java.time.LocalDateTime.now().minusDays(1)
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                        return FXCollections.observableArrayList(
                                new JobEntry("Source Code Scan", "Running", currentTime, "Scanning project source files"),
                                new JobEntry("Vector Index Update", "Completed", currentTime, "Updating search embeddings"),
                                new JobEntry("Database Backup", "Failed", yesterdayTime, "Daily backup to external storage"),
                                new JobEntry("Log Cleanup", "Pending", "Never", "Clean up old log files"),
                                new JobEntry("Report Generation", "Completed", currentTime, "Generate weekly usage report"));

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Operation was interrupted", e);
                    }
                },
                jobs -> {
                    jobList.setAll(jobs);
                    updateStats();
                    setLastRefreshTime(
                            java.time.LocalDateTime.now()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                    log.info("Batch jobs loaded successfully. Total: {}", jobs.size());
                    setErrorMessage(null); // Clear any previous errors
                },
                error -> {
                    // Provide fallback data on error
                    loadFallbackData();

                    String errorMsg = "Unable to connect to job server. Showing cached data.";
                    setErrorMessage(errorMsg);
                    log.warn("Error loading batch jobs, using fallback data: {}", error.getMessage());
                })
                .whenComplete((r, ex) -> setBusy(false));
    }

    private void updateStats() {
        long running = jobList.stream().filter(j -> "Running".equals(j.getStatus())).count();
        long failed = jobList.stream().filter(j -> "Failed".equals(j.getStatus())).count();
        setTotalJobs(jobList.size());
        setRunningJobs((int) running);
        setFailedJobs((int) failed);
    }

    public void startJob(JobEntry job) {
        log.info("Starting job: {}", job.getName());
        // Simulate async operation
        runAsync(
                () -> {
                    Thread.sleep(500);
                    return true;
                },
                success -> {
                    if (success) {
                        job.setStatus("Running");
                        updateStats();
                        setErrorMessage(null);
                        log.info("Job '{}' started.", job.getName());
                    }
                },
                error -> {
                    setErrorMessage(I18n.get("error.job.start.failed") + ": " + error.getMessage());
                    log.error("Error starting job", error);
                });
    }

    public void stopJob(JobEntry job) {
        log.info("Stopping job: {}", job.getName());
        // Simulate async operation
        runAsync(
                () -> {
                    Thread.sleep(500);
                    return true;
                },
                success -> {
                    if (success) {
                        job.setStatus("Stopped");
                        updateStats();
                        setErrorMessage(null);
                        log.info("Job '{}' stopped.", job.getName());
                    }
                },
                error -> {
                    setErrorMessage(I18n.get("error.job.stop.failed") + ": " + error.getMessage());
                    log.error("Error stopping job", error);
                });
    }

    // Getters and Property methods
    public int getTotalJobs() {
        return totalJobs.get();
    }

    public void setTotalJobs(int totalJobs) {
        this.totalJobs.set(totalJobs);
    }

    public IntegerProperty totalJobsProperty() {
        return totalJobs;
    }

    public int getRunningJobs() {
        return runningJobs.get();
    }

    public void setRunningJobs(int runningJobs) {
        this.runningJobs.set(runningJobs);
    }

    public IntegerProperty runningJobsProperty() {
        return runningJobs;
    }

    public int getFailedJobs() {
        return failedJobs.get();
    }

    public void setFailedJobs(int failedJobs) {
        this.failedJobs.set(failedJobs);
    }

    public IntegerProperty failedJobsProperty() {
        return failedJobs;
    }

    public String getLastRefreshTime() {
        return lastRefreshTime.get();
    }

    public void setLastRefreshTime(String lastRefreshTime) {
        this.lastRefreshTime.set(lastRefreshTime);
    }

    public StringProperty lastRefreshTimeProperty() {
        return lastRefreshTime;
    }

    public ObservableList<JobEntry> getJobList() {
        return jobList;
    }

    /**
     * Load fallback/cached data when server is unavailable
     */
    private void loadFallbackData() {
        jobList.setAll(
                new JobEntry("System Monitor", "Running", "00:00", "Basic system monitoring"),
                new JobEntry("Health Check", "Completed", "08:00", "Application health verification"),
                new JobEntry("Cache Cleanup", "Pending", "Never", "Clean temporary cache files")
        );
        updateStats();
        setLastRefreshTime(
                java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
        );
    }

    public static class JobEntry {
        private final StringProperty name;
        private final StringProperty status;
        private final StringProperty lastRun;
        private final StringProperty description;

        public JobEntry(String name, String status, String lastRun, String description) {
            this.name = new SimpleStringProperty(name);
            this.status = new SimpleStringProperty(status);
            this.lastRun = new SimpleStringProperty(lastRun);
            this.description = new SimpleStringProperty(description);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public String getStatus() {
            return status.get();
        }

        public void setStatus(String status) {
            this.status.set(status);
        }

        public StringProperty statusProperty() {
            return status;
        }

        public String getLastRun() {
            return lastRun.get();
        }

        public void setLastRun(String lastRun) {
            this.lastRun.set(lastRun);
        }

        public StringProperty lastRunProperty() {
            return lastRun;
        }

        public String getDescription() {
            return description.get();
        }

        public void setDescription(String description) {
            this.description.set(description);
        }

        public StringProperty descriptionProperty() {
            return description;
        }
    }
}
