package com.noteflix.pcm.ui.viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

/**
 * ViewModel for Batch Jobs Page
 *
 * <p>Manages batch job monitoring and scheduling state
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class BatchJobsViewModel extends BaseViewModel {

  // Statistics
  private final IntegerProperty totalJobs = new SimpleIntegerProperty(0);
  private final IntegerProperty runningJobs = new SimpleIntegerProperty(0);
  private final IntegerProperty completedJobs = new SimpleIntegerProperty(0);
  private final IntegerProperty failedJobs = new SimpleIntegerProperty(0);

  // Jobs list
  private final ObservableList<BatchJob> jobs = FXCollections.observableArrayList();

  /** Constructor */
  public BatchJobsViewModel() {
    loadJobs();
    log.info("BatchJobsViewModel initialized");
  }

  /** Load batch jobs */
  private void loadJobs() {
    // Mock data
    jobs.add(new BatchJob("DATA_IMPORT_001", "Data Import", "RUNNING", "85%"));
    jobs.add(new BatchJob("REPORT_GEN_002", "Report Generation", "COMPLETED", "100%"));
    jobs.add(new BatchJob("EMAIL_BATCH_003", "Email Dispatch", "PENDING", "0%"));
    jobs.add(new BatchJob("DATA_CLEAN_004", "Data Cleanup", "COMPLETED", "100%"));
    jobs.add(new BatchJob("BACKUP_005", "Database Backup", "FAILED", "45%"));

    updateStatistics();
    log.debug("Loaded {} batch jobs", jobs.size());
  }

  /** Update statistics */
  private void updateStatistics() {
    totalJobs.set(jobs.size());
    runningJobs.set((int) jobs.stream().filter(j -> "RUNNING".equals(j.status)).count());
    completedJobs.set((int) jobs.stream().filter(j -> "COMPLETED".equals(j.status)).count());
    failedJobs.set((int) jobs.stream().filter(j -> "FAILED".equals(j.status)).count());
  }

  /** Refresh jobs list */
  public void refreshJobs() {
    setBusy(true);
    clearError();

    jobs.clear();
    loadJobs();

    setBusy(false);
    log.info("Jobs refreshed");
  }

  /** Start a job */
  public void startJob(BatchJob job) {
    if (job == null) return;

    job.status = "RUNNING";
    job.progress = "0%";
    updateStatistics();
    log.info("Started job: {}", job.name);
  }

  /** Stop a job */
  public void stopJob(BatchJob job) {
    if (job == null) return;

    job.status = "STOPPED";
    updateStatistics();
    log.info("Stopped job: {}", job.name);
  }

  /** Delete a job */
  public void deleteJob(BatchJob job) {
    if (job == null) return;

    jobs.remove(job);
    updateStatistics();
    log.info("Deleted job: {}", job.name);
  }

  /** Create new job */
  public void createNewJob() {
    String jobId = "JOB_" + String.format("%03d", jobs.size() + 1);
    BatchJob newJob = new BatchJob(jobId, "New Job", "PENDING", "0%");
    jobs.add(newJob);
    updateStatistics();
    log.info("Created new job: {}", jobId);
  }

  // Property accessors
  public IntegerProperty totalJobsProperty() {
    return totalJobs;
  }

  public int getTotalJobs() {
    return totalJobs.get();
  }

  public IntegerProperty runningJobsProperty() {
    return runningJobs;
  }

  public int getRunningJobs() {
    return runningJobs.get();
  }

  public IntegerProperty completedJobsProperty() {
    return completedJobs;
  }

  public int getCompletedJobs() {
    return completedJobs.get();
  }

  public IntegerProperty failedJobsProperty() {
    return failedJobs;
  }

  public int getFailedJobs() {
    return failedJobs.get();
  }

  public ObservableList<BatchJob> getJobs() {
    return jobs;
  }

  /** Batch Job model */
  public static class BatchJob {
    public String id;
    public String name;
    public String status;
    public String progress;

    public BatchJob(String id, String name, String status, String progress) {
      this.id = id;
      this.name = name;
      this.status = status;
      this.progress = progress;
    }
  }
}

