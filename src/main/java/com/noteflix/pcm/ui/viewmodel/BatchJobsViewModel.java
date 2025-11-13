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
  }

  public void loadJobs() {
    setBusy(true);
    setErrorMessage(null);
    log.info("Loading batch jobs...");
    runAsync(
            () -> {
              Thread.sleep(1200); // Simulate network/DB call
              if (Math.random() < 0.05) { // Simulate occasional failure
                throw new RuntimeException("Failed to load jobs from server.");
              }
              // Simulate data
              return FXCollections.observableArrayList(
                  new JobEntry("Job A", "Running", "2023-10-26 10:00", "Daily backup"),
                  new JobEntry("Job B", "Completed", "2023-10-26 09:30", "Data sync"),
                  new JobEntry("Job C", "Failed", "2023-10-25 23:00", "Report generation"),
                  new JobEntry("Job D", "Pending", "2023-10-27 01:00", "Cleanup"));
            },
            jobs -> {
              jobList.setAll(jobs);
              updateStats();
              setLastRefreshTime(
                  java.time.LocalDateTime.now()
                      .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
              log.info("Batch jobs loaded successfully. Total: {}", jobs.size());
            },
            error -> {
              setErrorMessage(I18n.get("error.jobs.load.failed") + ": " + error.getMessage());
              log.error("Error loading batch jobs", error);
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

  public IntegerProperty totalJobsProperty() {
    return totalJobs;
  }

  public void setTotalJobs(int totalJobs) {
    this.totalJobs.set(totalJobs);
  }

  public int getRunningJobs() {
    return runningJobs.get();
  }

  public IntegerProperty runningJobsProperty() {
    return runningJobs;
  }

  public void setRunningJobs(int runningJobs) {
    this.runningJobs.set(runningJobs);
  }

  public int getFailedJobs() {
    return failedJobs.get();
  }

  public IntegerProperty failedJobsProperty() {
    return failedJobs;
  }

  public void setFailedJobs(int failedJobs) {
    this.failedJobs.set(failedJobs);
  }

  public String getLastRefreshTime() {
    return lastRefreshTime.get();
  }

  public StringProperty lastRefreshTimeProperty() {
    return lastRefreshTime;
  }

  public void setLastRefreshTime(String lastRefreshTime) {
    this.lastRefreshTime.set(lastRefreshTime);
  }

  public ObservableList<JobEntry> getJobList() {
    return jobList;
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

    public StringProperty nameProperty() {
      return name;
    }

    public void setName(String name) {
      this.name.set(name);
    }

    public String getStatus() {
      return status.get();
    }

    public StringProperty statusProperty() {
      return status;
    }

    public void setStatus(String status) {
      this.status.set(status);
    }

    public String getLastRun() {
      return lastRun.get();
    }

    public StringProperty lastRunProperty() {
      return lastRun;
    }

    public void setLastRun(String lastRun) {
      this.lastRun.set(lastRun);
    }

    public String getDescription() {
      return description.get();
    }

    public StringProperty descriptionProperty() {
      return description;
    }

    public void setDescription(String description) {
      this.description.set(description);
    }
  }
}
