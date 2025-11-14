# Hệ thống Đặt lịch và Nhắc nhở - Tài liệu Implementation

## Tổng quan
Tài liệu này hướng dẫn chi tiết việc tích hợp hệ thống đặt lịch, nhắc nhở và thực hiện công việc tự động vào PCM Desktop Application. Hệ thống sẽ hỗ trợ các tính năng như đặt lịch nhắc nhở, thực hiện tác vụ định kỳ, và quản lý timeline công việc.

## Phân tích kiến trúc hiện tại

### Hạ tầng sẵn có
- **Threading**: `Asyncs.java` - ThreadPoolExecutor với 4 worker threads
- **Database**: SQLite với migration system
- **UI Framework**: JavaFX với MVVM pattern
- **Notification**: Có thể tích hợp system tray notifications

### Cơ sở để xây dựng
```
src/main/java/com/noteflix/pcm/
├── core/utils/Asyncs.java (Async execution)
├── infrastructure/database/ (Database layer)
├── ui/pages/ (UI components)
└── domain/ (Business entities)
```

## 1. Kiến trúc Hệ thống Scheduling

### 1.1 Core Components
```
📦 Scheduling System
├── 📂 scheduler/
│   ├── core/
│   │   ├── SchedulerEngine.java
│   │   ├── JobExecutor.java
│   │   └── TriggerManager.java
│   ├── model/
│   │   ├── ScheduledJob.java
│   │   ├── JobTrigger.java
│   │   ├── Reminder.java
│   │   └── JobExecutionResult.java
│   ├── trigger/
│   │   ├── CronTrigger.java
│   │   ├── IntervalTrigger.java
│   │   ├── DateTimeTrigger.java
│   │   └── DelayedTrigger.java
│   ├── job/
│   │   ├── JobFactory.java
│   │   ├── ReminderJob.java
│   │   ├── BackupJob.java
│   │   └── NotificationJob.java
│   └── persistence/
│       ├── SchedulerDAO.java
│       └── JobHistoryDAO.java
```

### 1.2 System Architecture Diagram
```
┌─────────────────┬─────────────────┬─────────────────┐
│   UI Layer      │   Service       │   Persistence   │
│                 │   Layer         │   Layer         │
├─────────────────┼─────────────────┼─────────────────┤
│ SchedulePage    │ SchedulerEngine │ SchedulerDAO    │
│ ReminderWidget  │ JobExecutor     │ JobHistoryDAO   │
│ NotificationUI  │ TriggerManager  │ Database        │
│ CalendarView    │ JobFactory      │ Migration       │
└─────────────────┴─────────────────┴─────────────────┘
         │                 │                 │
         └─────────────────┼─────────────────┘
                          │
               ┌─────────────────┐
               │ JavaFX Platform │
               │ Asyncs Utils    │
               │ System Tray     │
               └─────────────────┘
```

## 2. Data Model Design

### 2.1 Core Entities

```java
// ScheduledJob.java - Main job entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledJob {
    private Long id;
    private String name;
    private String description;
    private JobType type;
    private JobStatus status;
    private String jobClass;
    private Map<String, Object> parameters;
    private JobTrigger trigger;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime nextExecutionTime;
    private LocalDateTime lastExecutionTime;
    private String owner;
    private Integer executionCount;
    private Integer maxExecutions;
    private boolean enabled;
    
    public enum JobType {
        REMINDER("Nhắc nhở"),
        BACKUP("Sao lưu"),
        CLEANUP("Dọn dẹp"),
        NOTIFICATION("Thông báo"),
        DATA_SYNC("Đồng bộ dữ liệu"),
        REPORT("Báo cáo"),
        CUSTOM("Tùy chỉnh");
        
        private final String displayName;
    }
    
    public enum JobStatus {
        SCHEDULED("Đã lên lịch"),
        RUNNING("Đang chạy"),
        COMPLETED("Hoàn thành"),
        FAILED("Thất bại"),
        PAUSED("Tạm dừng"),
        CANCELLED("Đã hủy");
        
        private final String displayName;
    }
}

// JobTrigger.java - Trigger configuration
@Data
@Builder
public class JobTrigger {
    private Long id;
    private TriggerType type;
    private String expression; // Cron expression or interval
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ZoneId timeZone;
    private Integer repeatCount;
    private Long intervalMillis;
    private Map<String, Object> properties;
    
    public enum TriggerType {
        ONCE("Một lần"),
        CRON("Cron Expression"),
        INTERVAL("Khoảng thời gian"),
        DAILY("Hàng ngày"),
        WEEKLY("Hàng tuần"),
        MONTHLY("Hàng tháng"),
        DELAYED("Trì hoãn");
        
        private final String displayName;
    }
}

// Reminder.java - Specific reminder entity
@Data
@Builder
public class Reminder {
    private Long id;
    private String title;
    private String message;
    private ReminderType type;
    private Priority priority;
    private LocalDateTime reminderTime;
    private Duration snoozeInterval;
    private Integer maxSnoozeCount;
    private Integer currentSnoozeCount;
    private boolean acknowledged;
    private List<String> recipients;
    private Map<String, Object> metadata;
    
    public enum ReminderType {
        TASK("Công việc"),
        MEETING("Cuộc họp"),
        DEADLINE("Hạn chót"),
        PERSONAL("Cá nhân"),
        SYSTEM("Hệ thống");
    }
    
    public enum Priority {
        LOW("Thấp"),
        NORMAL("Bình thường"),
        HIGH("Cao"),
        URGENT("Khẩn cấp");
    }
}

// JobExecutionResult.java - Execution tracking
@Data
@Builder
public class JobExecutionResult {
    private Long id;
    private Long jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;
    private ExecutionStatus status;
    private String output;
    private String errorMessage;
    private String stackTrace;
    private Map<String, Object> metrics;
    
    public enum ExecutionStatus {
        SUCCESS, FAILED, TIMEOUT, CANCELLED
    }
}
```

### 2.2 Database Schema

```sql
-- scheduled_jobs table
CREATE TABLE scheduled_jobs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    job_type TEXT NOT NULL,
    job_status TEXT NOT NULL DEFAULT 'SCHEDULED',
    job_class TEXT NOT NULL,
    parameters TEXT, -- JSON
    trigger_data TEXT, -- JSON
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    next_execution_time TIMESTAMP,
    last_execution_time TIMESTAMP,
    owner TEXT,
    execution_count INTEGER DEFAULT 0,
    max_executions INTEGER,
    enabled BOOLEAN DEFAULT 1
);

-- job_triggers table
CREATE TABLE job_triggers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    job_id INTEGER NOT NULL,
    trigger_type TEXT NOT NULL,
    cron_expression TEXT,
    interval_millis INTEGER,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    time_zone TEXT DEFAULT 'UTC',
    repeat_count INTEGER,
    properties TEXT, -- JSON
    FOREIGN KEY (job_id) REFERENCES scheduled_jobs(id) ON DELETE CASCADE
);

-- reminders table
CREATE TABLE reminders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    job_id INTEGER,
    title TEXT NOT NULL,
    message TEXT,
    reminder_type TEXT NOT NULL,
    priority TEXT DEFAULT 'NORMAL',
    reminder_time TIMESTAMP NOT NULL,
    snooze_interval_millis INTEGER,
    max_snooze_count INTEGER DEFAULT 3,
    current_snooze_count INTEGER DEFAULT 0,
    acknowledged BOOLEAN DEFAULT 0,
    recipients TEXT, -- JSON array
    metadata TEXT, -- JSON
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES scheduled_jobs(id) ON DELETE SET NULL
);

-- job_execution_history table
CREATE TABLE job_execution_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    job_id INTEGER NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_millis INTEGER,
    execution_status TEXT NOT NULL,
    output TEXT,
    error_message TEXT,
    stack_trace TEXT,
    metrics TEXT, -- JSON
    FOREIGN KEY (job_id) REFERENCES scheduled_jobs(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_scheduled_jobs_next_execution ON scheduled_jobs(next_execution_time);
CREATE INDEX idx_scheduled_jobs_status ON scheduled_jobs(job_status);
CREATE INDEX idx_scheduled_jobs_enabled ON scheduled_jobs(enabled);
CREATE INDEX idx_reminders_time ON reminders(reminder_time);
CREATE INDEX idx_reminders_acknowledged ON reminders(acknowledged);
CREATE INDEX idx_execution_history_job_id ON job_execution_history(job_id);
CREATE INDEX idx_execution_history_start_time ON job_execution_history(start_time);

-- Update triggers
CREATE TRIGGER trg_scheduled_jobs_updated_at
    AFTER UPDATE ON scheduled_jobs
    FOR EACH ROW
    BEGIN
        UPDATE scheduled_jobs SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
    END;
```

## 3. Core Scheduler Engine

### 3.1 SchedulerEngine Implementation

```java
package com.noteflix.pcm.scheduler.core;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchedulerEngine {
    
    private final ScheduledExecutorService scheduler;
    private final JobExecutor jobExecutor;
    private final SchedulerDAO schedulerDAO;
    private final TriggerManager triggerManager;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks;
    private volatile boolean running;
    
    public SchedulerEngine(SchedulerDAO schedulerDAO) {
        this.scheduler = Executors.newScheduledThreadPool(4, 
            r -> {
                Thread t = new Thread(r, "Scheduler-" + System.nanoTime());
                t.setDaemon(true);
                return t;
            });
        this.jobExecutor = new JobExecutor();
        this.schedulerDAO = schedulerDAO;
        this.triggerManager = new TriggerManager();
        this.scheduledTasks = new ConcurrentHashMap<>();
        this.running = false;
    }
    
    /**
     * Start the scheduler engine
     */
    public synchronized void start() {
        if (running) {
            log.warn("Scheduler already running");
            return;
        }
        
        running = true;
        log.info("🚀 Starting Scheduler Engine...");
        
        // Load and schedule all enabled jobs
        loadAndScheduleJobs();
        
        // Start job monitor
        startJobMonitor();
        
        log.info("✅ Scheduler Engine started successfully");
    }
    
    /**
     * Stop the scheduler engine
     */
    public synchronized void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        log.info("🛑 Stopping Scheduler Engine...");
        
        // Cancel all scheduled tasks
        scheduledTasks.values().forEach(task -> task.cancel(false));
        scheduledTasks.clear();
        
        // Shutdown scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("✅ Scheduler Engine stopped");
    }
    
    /**
     * Schedule a new job
     */
    public Long scheduleJob(ScheduledJob job) {
        try {
            // Save job to database
            Long jobId = schedulerDAO.createJob(job);
            job.setId(jobId);
            
            // Calculate next execution time
            LocalDateTime nextExecution = triggerManager.calculateNextExecution(job.getTrigger());
            job.setNextExecutionTime(nextExecution);
            schedulerDAO.updateNextExecutionTime(jobId, nextExecution);
            
            // Schedule job execution
            scheduleJobExecution(job);
            
            log.info("📅 Job scheduled: {} (ID: {})", job.getName(), jobId);
            return jobId;
            
        } catch (Exception e) {
            log.error("❌ Failed to schedule job: {}", job.getName(), e);
            throw new SchedulingException("Failed to schedule job", e);
        }
    }
    
    /**
     * Cancel a scheduled job
     */
    public boolean cancelJob(Long jobId) {
        try {
            // Cancel scheduled task
            ScheduledFuture<?> task = scheduledTasks.remove(jobId);
            if (task != null) {
                task.cancel(false);
            }
            
            // Update job status in database
            schedulerDAO.updateJobStatus(jobId, JobStatus.CANCELLED);
            
            log.info("❌ Job cancelled: {}", jobId);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Failed to cancel job: {}", jobId, e);
            return false;
        }
    }
    
    /**
     * Pause a job
     */
    public boolean pauseJob(Long jobId) {
        try {
            // Cancel current scheduling
            ScheduledFuture<?> task = scheduledTasks.remove(jobId);
            if (task != null) {
                task.cancel(false);
            }
            
            // Update status
            schedulerDAO.updateJobStatus(jobId, JobStatus.PAUSED);
            
            log.info("⏸️ Job paused: {}", jobId);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Failed to pause job: {}", jobId, e);
            return false;
        }
    }
    
    /**
     * Resume a paused job
     */
    public boolean resumeJob(Long jobId) {
        try {
            ScheduledJob job = schedulerDAO.getJob(jobId);
            if (job == null || job.getStatus() != JobStatus.PAUSED) {
                return false;
            }
            
            // Update status
            schedulerDAO.updateJobStatus(jobId, JobStatus.SCHEDULED);
            
            // Reschedule job
            LocalDateTime nextExecution = triggerManager.calculateNextExecution(job.getTrigger());
            job.setNextExecutionTime(nextExecution);
            schedulerDAO.updateNextExecutionTime(jobId, nextExecution);
            
            scheduleJobExecution(job);
            
            log.info("▶️ Job resumed: {}", jobId);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Failed to resume job: {}", jobId, e);
            return false;
        }
    }
    
    /**
     * Execute job immediately
     */
    public CompletableFuture<JobExecutionResult> executeJobNow(Long jobId) {
        try {
            ScheduledJob job = schedulerDAO.getJob(jobId);
            if (job == null) {
                throw new JobNotFoundException("Job not found: " + jobId);
            }
            
            return jobExecutor.execute(job);
            
        } catch (Exception e) {
            log.error("❌ Failed to execute job immediately: {}", jobId, e);
            CompletableFuture<JobExecutionResult> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    // Private helper methods
    
    private void loadAndScheduleJobs() {
        try {
            List<ScheduledJob> jobs = schedulerDAO.getEnabledJobs();
            log.info("📋 Loading {} enabled jobs", jobs.size());
            
            for (ScheduledJob job : jobs) {
                try {
                    scheduleJobExecution(job);
                } catch (Exception e) {
                    log.error("❌ Failed to schedule job: {} (ID: {})", job.getName(), job.getId(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to load jobs from database", e);
        }
    }
    
    private void scheduleJobExecution(ScheduledJob job) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecution = job.getNextExecutionTime();
        
        if (nextExecution == null) {
            nextExecution = triggerManager.calculateNextExecution(job.getTrigger());
            if (nextExecution == null) {
                log.warn("⚠️ Cannot calculate next execution time for job: {}", job.getName());
                return;
            }
        }
        
        // Calculate delay
        long delayMillis = java.time.Duration.between(now, nextExecution).toMillis();
        
        if (delayMillis < 0) {
            // Missed execution - reschedule for next occurrence
            nextExecution = triggerManager.calculateNextExecution(job.getTrigger());
            if (nextExecution == null) {
                log.warn("⚠️ Job missed and no future executions: {}", job.getName());
                return;
            }
            delayMillis = java.time.Duration.between(now, nextExecution).toMillis();
        }
        
        // Schedule the job
        ScheduledFuture<?> future = scheduler.schedule(
            () -> executeJob(job), 
            delayMillis, 
            TimeUnit.MILLISECONDS
        );
        
        scheduledTasks.put(job.getId(), future);
        
        log.debug("⏰ Job scheduled for execution: {} at {}", job.getName(), nextExecution);
    }
    
    private void executeJob(ScheduledJob job) {
        log.info("🚀 Executing job: {} (ID: {})", job.getName(), job.getId());
        
        CompletableFuture<JobExecutionResult> executionFuture = jobExecutor.execute(job);
        
        executionFuture.whenComplete((result, error) -> {
            try {
                // Record execution result
                schedulerDAO.recordExecution(result);
                
                // Update execution count
                schedulerDAO.incrementExecutionCount(job.getId());
                
                // Schedule next execution if needed
                if (job.getTrigger().getType() != TriggerType.ONCE) {
                    LocalDateTime nextExecution = triggerManager.calculateNextExecution(job.getTrigger());
                    if (nextExecution != null) {
                        job.setNextExecutionTime(nextExecution);
                        schedulerDAO.updateNextExecutionTime(job.getId(), nextExecution);
                        scheduleJobExecution(job);
                    }
                }
                
                if (error == null) {
                    log.info("✅ Job execution completed: {} (Duration: {}ms)", 
                        job.getName(), result.getDuration().toMillis());
                } else {
                    log.error("❌ Job execution failed: {}", job.getName(), error);
                }
                
            } catch (Exception e) {
                log.error("❌ Failed to process job execution result", e);
            } finally {
                // Remove from scheduled tasks
                scheduledTasks.remove(job.getId());
            }
        });
    }
    
    private void startJobMonitor() {
        // Monitor for missed jobs, health checks, etc.
        scheduler.scheduleAtFixedRate(this::monitorJobs, 1, 1, TimeUnit.MINUTES);
    }
    
    private void monitorJobs() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Check for missed executions
            List<ScheduledJob> missedJobs = schedulerDAO.getMissedJobs(now.minusMinutes(5));
            
            for (ScheduledJob job : missedJobs) {
                log.warn("⚠️ Missed job execution: {} (was scheduled for: {})", 
                    job.getName(), job.getNextExecutionTime());
                
                // Reschedule missed job
                LocalDateTime nextExecution = triggerManager.calculateNextExecution(job.getTrigger());
                if (nextExecution != null) {
                    job.setNextExecutionTime(nextExecution);
                    schedulerDAO.updateNextExecutionTime(job.getId(), nextExecution);
                    scheduleJobExecution(job);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error in job monitor", e);
        }
    }
}
```

### 3.2 JobExecutor Implementation

```java
package com.noteflix.pcm.scheduler.core;

@Slf4j
public class JobExecutor {
    
    private final ExecutorService executorService;
    private final JobFactory jobFactory;
    
    public JobExecutor() {
        this.executorService = ForkJoinPool.commonPool();
        this.jobFactory = new JobFactory();
    }
    
    /**
     * Execute a scheduled job
     */
    public CompletableFuture<JobExecutionResult> execute(ScheduledJob job) {
        LocalDateTime startTime = LocalDateTime.now();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create job instance
                ExecutableJob executableJob = jobFactory.createJob(job);
                
                log.info("🚀 Starting job execution: {}", job.getName());
                
                // Execute job
                JobExecutionContext context = JobExecutionContext.builder()
                    .job(job)
                    .startTime(startTime)
                    .parameters(job.getParameters())
                    .build();
                    
                String output = executableJob.execute(context);
                
                LocalDateTime endTime = LocalDateTime.now();
                Duration duration = Duration.between(startTime, endTime);
                
                return JobExecutionResult.builder()
                    .jobId(job.getId())
                    .startTime(startTime)
                    .endTime(endTime)
                    .duration(duration)
                    .status(ExecutionStatus.SUCCESS)
                    .output(output)
                    .build();
                    
            } catch (Exception e) {
                LocalDateTime endTime = LocalDateTime.now();
                Duration duration = Duration.between(startTime, endTime);
                
                log.error("❌ Job execution failed: {}", job.getName(), e);
                
                return JobExecutionResult.builder()
                    .jobId(job.getId())
                    .startTime(startTime)
                    .endTime(endTime)
                    .duration(duration)
                    .status(ExecutionStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .stackTrace(getStackTrace(e))
                    .build();
            }
        }, executorService);
    }
    
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
```

### 3.3 TriggerManager Implementation

```java
package com.noteflix.pcm.scheduler.core;

@Slf4j
public class TriggerManager {
    
    /**
     * Calculate next execution time based on trigger configuration
     */
    public LocalDateTime calculateNextExecution(JobTrigger trigger) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (trigger.getType()) {
            case ONCE:
                return trigger.getStartTime().isAfter(now) ? trigger.getStartTime() : null;
                
            case CRON:
                return calculateCronNextExecution(trigger, now);
                
            case INTERVAL:
                return calculateIntervalNextExecution(trigger, now);
                
            case DAILY:
                return calculateDailyNextExecution(trigger, now);
                
            case WEEKLY:
                return calculateWeeklyNextExecution(trigger, now);
                
            case MONTHLY:
                return calculateMonthlyNextExecution(trigger, now);
                
            case DELAYED:
                return now.plus(Duration.ofMillis(trigger.getIntervalMillis()));
                
            default:
                throw new IllegalArgumentException("Unsupported trigger type: " + trigger.getType());
        }
    }
    
    private LocalDateTime calculateCronNextExecution(JobTrigger trigger, LocalDateTime now) {
        try {
            // Simple cron parser - for production use library like Quartz CronExpression
            String expression = trigger.getExpression();
            
            // Basic format: "minute hour day month dayOfWeek"
            // Example: "0 9 * * MON-FRI" = 9:00 AM on weekdays
            String[] parts = expression.split("\\s+");
            
            if (parts.length != 5) {
                throw new IllegalArgumentException("Invalid cron expression: " + expression);
            }
            
            int minute = parseInt(parts[0]);
            int hour = parseInt(parts[1]);
            
            LocalDateTime next = now.plusDays(1)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0);
                
            // Simple implementation - for full cron support use dedicated library
            return next;
            
        } catch (Exception e) {
            log.error("❌ Failed to parse cron expression: {}", trigger.getExpression(), e);
            return null;
        }
    }
    
    private LocalDateTime calculateIntervalNextExecution(JobTrigger trigger, LocalDateTime now) {
        long intervalMillis = trigger.getIntervalMillis();
        return now.plus(Duration.ofMillis(intervalMillis));
    }
    
    private LocalDateTime calculateDailyNextExecution(JobTrigger trigger, LocalDateTime now) {
        LocalTime targetTime = trigger.getStartTime().toLocalTime();
        LocalDateTime next = now.toLocalDate().atTime(targetTime);
        
        if (next.isBefore(now) || next.isEqual(now)) {
            next = next.plusDays(1);
        }
        
        return next;
    }
    
    // Additional helper methods for weekly, monthly calculations...
}
```

## 4. Job Types Implementation

### 4.1 Job Factory và Base Job Interface

```java
// ExecutableJob.java - Base interface for all jobs
public interface ExecutableJob {
    String execute(JobExecutionContext context) throws Exception;
    String getJobType();
    boolean supportsParameters();
}

// JobExecutionContext.java - Execution context
@Data
@Builder
public class JobExecutionContext {
    private ScheduledJob job;
    private LocalDateTime startTime;
    private Map<String, Object> parameters;
    private Map<String, Object> runtimeData;
}

// JobFactory.java - Creates job instances
@Component
public class JobFactory {
    
    private final Map<String, Class<? extends ExecutableJob>> jobTypes;
    
    public JobFactory() {
        jobTypes = new HashMap<>();
        registerDefaultJobTypes();
    }
    
    private void registerDefaultJobTypes() {
        jobTypes.put("reminder", ReminderJob.class);
        jobTypes.put("notification", NotificationJob.class);
        jobTypes.put("backup", BackupJob.class);
        jobTypes.put("cleanup", CleanupJob.class);
        jobTypes.put("sync", DataSyncJob.class);
        jobTypes.put("report", ReportJob.class);
    }
    
    public ExecutableJob createJob(ScheduledJob scheduledJob) throws Exception {
        String jobClass = scheduledJob.getJobClass();
        Class<? extends ExecutableJob> clazz = jobTypes.get(jobClass);
        
        if (clazz == null) {
            throw new JobNotFoundException("Job type not found: " + jobClass);
        }
        
        return clazz.getDeclaredConstructor().newInstance();
    }
    
    public void registerJobType(String type, Class<? extends ExecutableJob> jobClass) {
        jobTypes.put(type, jobClass);
    }
}
```

### 4.2 Reminder Job Implementation

```java
package com.noteflix.pcm.scheduler.job;

@Slf4j
public class ReminderJob implements ExecutableJob {
    
    @Override
    public String execute(JobExecutionContext context) throws Exception {
        Map<String, Object> params = context.getParameters();
        
        String title = (String) params.get("title");
        String message = (String) params.get("message");
        String priority = (String) params.get("priority");
        List<String> recipients = (List<String>) params.get("recipients");
        
        log.info("📢 Executing reminder: {}", title);
        
        // Create reminder notification
        ReminderNotification notification = ReminderNotification.builder()
            .title(title)
            .message(message)
            .priority(Priority.valueOf(priority))
            .timestamp(LocalDateTime.now())
            .build();
        
        // Send notification through multiple channels
        NotificationResult result = sendNotification(notification, recipients);
        
        // Update reminder status in database
        updateReminderStatus(context.getJob().getId(), true);
        
        return String.format("Reminder sent successfully. Recipients: %d, Success: %d", 
            result.getTotalRecipients(), result.getSuccessCount());
    }
    
    private NotificationResult sendNotification(ReminderNotification notification, 
                                              List<String> recipients) {
        NotificationService notificationService = NotificationService.getInstance();
        
        // Desktop notification
        notificationService.showDesktopNotification(notification);
        
        // System tray notification
        notificationService.showTrayNotification(notification);
        
        // Sound notification
        if (notification.getPriority() == Priority.HIGH || 
            notification.getPriority() == Priority.URGENT) {
            notificationService.playNotificationSound();
        }
        
        // Email notification (if configured)
        if (recipients != null && !recipients.isEmpty()) {
            notificationService.sendEmailNotifications(notification, recipients);
        }
        
        return NotificationResult.builder()
            .totalRecipients(recipients != null ? recipients.size() : 1)
            .successCount(1)
            .build();
    }
    
    @Override
    public String getJobType() {
        return "reminder";
    }
    
    @Override
    public boolean supportsParameters() {
        return true;
    }
}
```

### 4.3 Notification Service

```java
package com.noteflix.pcm.scheduler.notification;

@Service
@Slf4j
public class NotificationService {
    
    private static NotificationService instance;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private boolean traySupported;
    
    private NotificationService() {
        initializeSystemTray();
    }
    
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    private void initializeSystemTray() {
        if (SystemTray.isSupported()) {
            systemTray = SystemTray.getSystemTray();
            traySupported = true;
            
            try {
                // Create tray icon
                Image image = Toolkit.getDefaultToolkit()
                    .createImage(getClass().getResource("/images/icons/app-icon.png"));
                    
                trayIcon = new TrayIcon(image, "PCM Desktop");
                trayIcon.setImageAutoSize(true);
                
                systemTray.add(trayIcon);
                
            } catch (Exception e) {
                log.warn("Failed to initialize system tray", e);
                traySupported = false;
            }
        }
    }
    
    /**
     * Show desktop notification
     */
    public void showDesktopNotification(ReminderNotification notification) {
        Platform.runLater(() -> {
            try {
                // JavaFX notification
                Notifications.create()
                    .title(notification.getTitle())
                    .text(notification.getMessage())
                    .position(Pos.TOP_RIGHT)
                    .hideAfter(Duration.seconds(10))
                    .action(new Label("Dismiss"), e -> {
                        log.debug("Notification dismissed");
                    })
                    .showInformation();
                    
                log.info("💬 Desktop notification shown: {}", notification.getTitle());
                
            } catch (Exception e) {
                log.error("Failed to show desktop notification", e);
            }
        });
    }
    
    /**
     * Show system tray notification
     */
    public void showTrayNotification(ReminderNotification notification) {
        if (!traySupported || trayIcon == null) {
            return;
        }
        
        TrayIcon.MessageType messageType = switch (notification.getPriority()) {
            case URGENT, HIGH -> TrayIcon.MessageType.WARNING;
            case NORMAL -> TrayIcon.MessageType.INFO;
            case LOW -> TrayIcon.MessageType.NONE;
        };
        
        trayIcon.displayMessage(
            notification.getTitle(),
            notification.getMessage(),
            messageType
        );
        
        log.info("🔔 Tray notification shown: {}", notification.getTitle());
    }
    
    /**
     * Play notification sound
     */
    public void playNotificationSound() {
        try {
            // Play system beep
            Toolkit.getDefaultToolkit().beep();
            
            // Or play custom sound file
            /*
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                getClass().getResource("/sounds/notification.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            */
            
        } catch (Exception e) {
            log.warn("Failed to play notification sound", e);
        }
    }
    
    /**
     * Send email notifications (if configured)
     */
    public void sendEmailNotifications(ReminderNotification notification, 
                                     List<String> recipients) {
        // Implementation for email notifications
        // This would require SMTP configuration
        log.info("📧 Email notification would be sent to: {}", recipients);
    }
}
```

## 5. User Interface Components

### 5.1 Schedule Management Page

```java
package com.noteflix.pcm.ui.pages;

@Component
public class SchedulePage extends VBox {
    
    @FXML private TableView<ScheduledJob> jobsTable;
    @FXML private Button createJobButton;
    @FXML private Button editJobButton;
    @FXML private Button deleteJobButton;
    @FXML private Button executeNowButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<JobType> typeFilter;
    @FXML private ComboBox<JobStatus> statusFilter;
    
    private final ScheduleViewModel viewModel;
    private final SchedulerEngine schedulerEngine;
    
    public SchedulePage(ScheduleViewModel viewModel, SchedulerEngine schedulerEngine) {
        this.viewModel = viewModel;
        this.schedulerEngine = schedulerEngine;
        initializeUI();
        bindViewModel();
    }
    
    private void initializeUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/schedule-page.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        
        try {
            loader.load();
            setupTableColumns();
            setupEventHandlers();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schedule page", e);
        }
    }
    
    private void setupTableColumns() {
        // Job name column
        TableColumn<ScheduledJob, String> nameCol = new TableColumn<>("Tên công việc");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Job type column
        TableColumn<ScheduledJob, String> typeCol = new TableColumn<>("Loại");
        typeCol.setCellValueFactory(param -> 
            new SimpleStringProperty(param.getValue().getType().getDisplayName()));
        
        // Status column
        TableColumn<ScheduledJob, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(param -> 
            new SimpleStringProperty(param.getValue().getStatus().getDisplayName()));
        statusCol.setCellFactory(param -> new StatusTableCell());
        
        // Next execution column
        TableColumn<ScheduledJob, String> nextExecCol = new TableColumn<>("Lần chạy tiếp theo");
        nextExecCol.setCellValueFactory(param -> {
            LocalDateTime nextTime = param.getValue().getNextExecutionTime();
            return new SimpleStringProperty(nextTime != null ? 
                nextTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A");
        });
        
        // Actions column
        TableColumn<ScheduledJob, Void> actionsCol = new TableColumn<>("Thao tác");
        actionsCol.setCellFactory(param -> new ActionTableCell());
        
        jobsTable.getColumns().addAll(nameCol, typeCol, statusCol, nextExecCol, actionsCol);
    }
    
    private void setupEventHandlers() {
        createJobButton.setOnAction(e -> showCreateJobDialog());
        editJobButton.setOnAction(e -> editSelectedJob());
        deleteJobButton.setOnAction(e -> deleteSelectedJob());
        executeNowButton.setOnAction(e -> executeSelectedJobNow());
        
        searchField.textProperty().addListener((obs, oldText, newText) -> 
            viewModel.setSearchText(newText));
        
        typeFilter.valueProperty().addListener((obs, oldType, newType) -> 
            viewModel.setTypeFilter(newType));
        
        statusFilter.valueProperty().addListener((obs, oldStatus, newStatus) -> 
            viewModel.setStatusFilter(newStatus));
    }
    
    private void bindViewModel() {
        jobsTable.itemsProperty().bind(viewModel.filteredJobsProperty());
        
        // Enable/disable buttons based on selection
        BooleanBinding hasSelection = jobsTable.getSelectionModel()
            .selectedItemProperty().isNotNull();
        editJobButton.disableProperty().bind(hasSelection.not());
        deleteJobButton.disableProperty().bind(hasSelection.not());
        executeNowButton.disableProperty().bind(hasSelection.not());
    }
    
    // Event handlers
    
    private void showCreateJobDialog() {
        CreateJobDialog dialog = new CreateJobDialog();
        Optional<ScheduledJob> result = dialog.showAndWait();
        
        result.ifPresent(job -> {
            try {
                Long jobId = schedulerEngine.scheduleJob(job);
                viewModel.refreshJobs();
                
                showNotification("Thành công", 
                    "Đã tạo công việc: " + job.getName());
                    
            } catch (Exception e) {
                showErrorDialog("Lỗi", "Không thể tạo công việc: " + e.getMessage());
            }
        });
    }
    
    private void editSelectedJob() {
        ScheduledJob selectedJob = jobsTable.getSelectionModel().getSelectedItem();
        if (selectedJob == null) return;
        
        EditJobDialog dialog = new EditJobDialog(selectedJob);
        Optional<ScheduledJob> result = dialog.showAndWait();
        
        result.ifPresent(updatedJob -> {
            try {
                viewModel.updateJob(updatedJob);
                showNotification("Thành công", 
                    "Đã cập nhật công việc: " + updatedJob.getName());
            } catch (Exception e) {
                showErrorDialog("Lỗi", "Không thể cập nhật công việc: " + e.getMessage());
            }
        });
    }
    
    private void deleteSelectedJob() {
        ScheduledJob selectedJob = jobsTable.getSelectionModel().getSelectedItem();
        if (selectedJob == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa công việc này?");
        alert.setContentText(selectedJob.getName());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                schedulerEngine.cancelJob(selectedJob.getId());
                viewModel.deleteJob(selectedJob.getId());
                showNotification("Thành công", "Đã xóa công việc");
            } catch (Exception e) {
                showErrorDialog("Lỗi", "Không thể xóa công việc: " + e.getMessage());
            }
        }
    }
    
    private void executeSelectedJobNow() {
        ScheduledJob selectedJob = jobsTable.getSelectionModel().getSelectedItem();
        if (selectedJob == null) return;
        
        executeNowButton.setDisable(true);
        executeNowButton.setText("Đang chạy...");
        
        CompletableFuture<JobExecutionResult> future = 
            schedulerEngine.executeJobNow(selectedJob.getId());
            
        future.whenComplete((result, error) -> {
            Platform.runLater(() -> {
                executeNowButton.setDisable(false);
                executeNowButton.setText("Chạy ngay");
                
                if (error == null && result.getStatus() == ExecutionStatus.SUCCESS) {
                    showNotification("Thành công", 
                        "Công việc đã được thực hiện: " + selectedJob.getName());
                } else {
                    showErrorDialog("Lỗi", 
                        "Thực hiện công việc thất bại: " + 
                        (error != null ? error.getMessage() : result.getErrorMessage()));
                }
                
                viewModel.refreshJobs();
            });
        });
    }
    
    // Custom table cells
    
    private class StatusTableCell extends TableCell<ScheduledJob, String> {
        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            
            if (empty || status == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(status);
                
                // Color coding for status
                getStyleClass().removeAll("status-active", "status-failed", "status-paused");
                
                if (status.equals("Đang chạy")) {
                    getStyleClass().add("status-active");
                } else if (status.equals("Thất bại")) {
                    getStyleClass().add("status-failed");
                } else if (status.equals("Tạm dừng")) {
                    getStyleClass().add("status-paused");
                }
            }
        }
    }
    
    private class ActionTableCell extends TableCell<ScheduledJob, Void> {
        private final Button pauseButton = new Button("⏸️");
        private final Button resumeButton = new Button("▶️");
        private final HBox actionBox = new HBox(5, pauseButton, resumeButton);
        
        {
            pauseButton.getStyleClass().add("action-button");
            resumeButton.getStyleClass().add("action-button");
            
            pauseButton.setOnAction(e -> {
                ScheduledJob job = getTableView().getItems().get(getIndex());
                schedulerEngine.pauseJob(job.getId());
                viewModel.refreshJobs();
            });
            
            resumeButton.setOnAction(e -> {
                ScheduledJob job = getTableView().getItems().get(getIndex());
                schedulerEngine.resumeJob(job.getId());
                viewModel.refreshJobs();
            });
        }
        
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty) {
                setGraphic(null);
            } else {
                ScheduledJob job = getTableView().getItems().get(getIndex());
                
                pauseButton.setVisible(job.getStatus() == JobStatus.SCHEDULED);
                resumeButton.setVisible(job.getStatus() == JobStatus.PAUSED);
                
                setGraphic(actionBox);
            }
        }
    }
}
```

### 5.2 Create Job Dialog

```java
package com.noteflix.pcm.ui.dialogs;

public class CreateJobDialog extends Dialog<ScheduledJob> {
    
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<JobType> typeCombo;
    @FXML private TabPane triggerTabPane;
    
    // Once trigger
    @FXML private DatePicker onceDatePicker;
    @FXML private TextField onceTimeField;
    
    // Interval trigger
    @FXML private TextField intervalValue;
    @FXML private ComboBox<TimeUnit> intervalUnit;
    
    // Daily trigger
    @FXML private TextField dailyTimeField;
    
    // Cron trigger
    @FXML private TextField cronExpression;
    @FXML private Label cronPreview;
    
    // Job specific parameters
    @FXML private VBox parametersBox;
    
    public CreateJobDialog() {
        initializeDialog();
        setupValidation();
    }
    
    private void initializeDialog() {
        setTitle("Tạo công việc mới");
        setHeaderText("Thiết lập thông tin công việc và lịch trình");
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create-job-dialog.fxml"));
        loader.setController(this);
        
        try {
            DialogPane dialogPane = loader.load();
            setDialogPane(dialogPane);
            
            // Setup combo boxes
            typeCombo.getItems().setAll(JobType.values());
            typeCombo.valueProperty().addListener((obs, old, newType) -> 
                updateParametersPane(newType));
            
            intervalUnit.getItems().setAll(
                TimeUnit.SECONDS, TimeUnit.MINUTES, 
                TimeUnit.HOURS, TimeUnit.DAYS);
            
            // Setup result converter
            setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return buildScheduledJob();
                }
                return null;
            });
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load create job dialog", e);
        }
    }
    
    private void updateParametersPane(JobType jobType) {
        parametersBox.getChildren().clear();
        
        switch (jobType) {
            case REMINDER:
                addReminderParameters();
                break;
            case BACKUP:
                addBackupParameters();
                break;
            case NOTIFICATION:
                addNotificationParameters();
                break;
            // Add other job types...
        }
    }
    
    private void addReminderParameters() {
        // Title field
        TextField titleField = new TextField();
        titleField.setPromptText("Tiêu đề nhắc nhở");
        parametersBox.getChildren().add(new Label("Tiêu đề:"));
        parametersBox.getChildren().add(titleField);
        
        // Message area
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Nội dung nhắc nhở");
        messageArea.setPrefRowCount(3);
        parametersBox.getChildren().add(new Label("Nội dung:"));
        parametersBox.getChildren().add(messageArea);
        
        // Priority combo
        ComboBox<Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().setAll(Priority.values());
        priorityCombo.setValue(Priority.NORMAL);
        parametersBox.getChildren().add(new Label("Mức độ ưu tiên:"));
        parametersBox.getChildren().add(priorityCombo);
        
        // Recipients field
        TextField recipientsField = new TextField();
        recipientsField.setPromptText("Email người nhận (phân cách bằng dấu phẩy)");
        parametersBox.getChildren().add(new Label("Người nhận (tùy chọn):"));
        parametersBox.getChildren().add(recipientsField);
    }
    
    private ScheduledJob buildScheduledJob() {
        // Build job trigger based on selected tab
        Tab selectedTab = triggerTabPane.getSelectionModel().getSelectedItem();
        JobTrigger trigger = buildTriggerFromTab(selectedTab);
        
        // Build parameters based on job type
        Map<String, Object> parameters = buildParametersFromType(typeCombo.getValue());
        
        return ScheduledJob.builder()
            .name(nameField.getText())
            .description(descriptionArea.getText())
            .type(typeCombo.getValue())
            .status(JobStatus.SCHEDULED)
            .jobClass(getJobClassForType(typeCombo.getValue()))
            .parameters(parameters)
            .trigger(trigger)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    private JobTrigger buildTriggerFromTab(Tab selectedTab) {
        String tabId = selectedTab.getId();
        
        switch (tabId) {
            case "onceTab":
                LocalDate date = onceDatePicker.getValue();
                LocalTime time = LocalTime.parse(onceTimeField.getText());
                return JobTrigger.builder()
                    .type(TriggerType.ONCE)
                    .startTime(date.atTime(time))
                    .build();
                    
            case "intervalTab":
                long interval = Long.parseLong(intervalValue.getText());
                TimeUnit unit = intervalUnit.getValue();
                return JobTrigger.builder()
                    .type(TriggerType.INTERVAL)
                    .intervalMillis(unit.toMillis(interval))
                    .build();
                    
            case "dailyTab":
                LocalTime dailyTime = LocalTime.parse(dailyTimeField.getText());
                return JobTrigger.builder()
                    .type(TriggerType.DAILY)
                    .startTime(LocalDateTime.of(LocalDate.now(), dailyTime))
                    .build();
                    
            case "cronTab":
                return JobTrigger.builder()
                    .type(TriggerType.CRON)
                    .expression(cronExpression.getText())
                    .build();
                    
            default:
                throw new IllegalStateException("Unknown trigger tab: " + tabId);
        }
    }
    
    // Additional helper methods...
}
```

## 6. Integration với Application

### 6.1 Cập nhật PCMApplication

```java
@Override
public void init() throws Exception {
    super.init();
    
    // ... existing initialization ...
    
    // Initialize scheduler system
    initializeSchedulerSystem();
    
    log.info("🚀 PCM Application initialized with scheduling system");
}

private void initializeSchedulerSystem() {
    try {
        // Initialize scheduler DAO
        SchedulerDAO schedulerDAO = new SchedulerDAO(connectionManager);
        
        // Initialize scheduler engine
        SchedulerEngine schedulerEngine = new SchedulerEngine(schedulerDAO);
        
        // Register with dependency injection
        Injector.getInstance().registerSingleton(SchedulerEngine.class, schedulerEngine);
        Injector.getInstance().registerSingleton(SchedulerDAO.class, schedulerDAO);
        
        // Start scheduler engine
        schedulerEngine.start();
        
        log.info("✅ Scheduler system initialized successfully");
        
    } catch (Exception e) {
        log.error("❌ Failed to initialize scheduler system", e);
        throw new RuntimeException("Scheduler initialization failed", e);
    }
}

@Override
public void stop() throws Exception {
    // Stop scheduler engine
    SchedulerEngine schedulerEngine = Injector.getInstance().getInstance(SchedulerEngine.class);
    if (schedulerEngine != null) {
        schedulerEngine.stop();
        log.info("✅ Scheduler engine stopped");
    }
    
    // ... existing cleanup ...
    
    super.stop();
}
```

### 6.2 Cập nhật Navigation

```java
// Route.java - Add new routes
public enum Route {
    // ... existing routes ...
    SCHEDULE("Lịch trình", "schedule", "/fxml/schedule-page.fxml", "📅"),
    REMINDERS("Nhắc nhở", "reminders", "/fxml/reminders-page.fxml", "🔔");
}

// SidebarView.java - Add menu items
private void initializeMenuItems() {
    // ... existing items ...
    
    addMenuItem("📅", "Lịch trình", Route.SCHEDULE);
    addMenuItem("🔔", "Nhắc nhở", Route.REMINDERS);
}
```

## 7. Advanced Features

### 7.1 Calendar Integration

```java
package com.noteflix.pcm.ui.components;

public class ScheduleCalendarView extends VBox {
    
    private CalendarView calendarView;
    private ListView<ScheduledJob> dayJobsList;
    
    public ScheduleCalendarView() {
        initializeCalendar();
    }
    
    private void initializeCalendar() {
        calendarView = new CalendarView();
        
        // Customize calendar appearance
        calendarView.getStylesheets().add("/css/calendar.css");
        calendarView.setShowWeekNumbers(false);
        calendarView.setRequestedTime(LocalTime.now());
        
        // Add job entries to calendar
        calendarView.getCalendarSources().add(createJobCalendarSource());
        
        // Listen for date selection
        calendarView.selectedPageProperty().addListener((obs, oldPage, newPage) -> {
            if (newPage != null) {
                updateDayJobsList(newPage.getValue());
            }
        });
        
        // Setup layout
        getChildren().addAll(calendarView, createDayJobsPanel());
    }
    
    private CalendarSource createJobCalendarSource() {
        CalendarSource jobSource = new CalendarSource("Scheduled Jobs");
        
        // Create calendar for each job type
        for (JobType type : JobType.values()) {
            Calendar calendar = new Calendar(type.getDisplayName());
            calendar.setStyle(Calendar.Style.getStyle(type.ordinal()));
            jobSource.getCalendars().add(calendar);
        }
        
        return jobSource;
    }
}
```

### 7.2 Smart Notifications

```java
package com.noteflix.pcm.scheduler.notification;

@Service
public class SmartNotificationService extends NotificationService {
    
    private final UserPreferencesService preferencesService;
    private final AIAssistantService aiService;
    
    /**
     * Send smart notifications based on user context and preferences
     */
    @Override
    public void showDesktopNotification(ReminderNotification notification) {
        // Check user availability
        UserAvailability availability = checkUserAvailability();
        
        if (availability == UserAvailability.BUSY) {
            // Delay notification or use less intrusive method
            scheduleDelayedNotification(notification, Duration.ofMinutes(15));
            return;
        }
        
        // Customize notification based on preferences
        NotificationPreferences prefs = preferencesService.getNotificationPreferences();
        
        if (prefs.isSmartPrioritization()) {
            notification = enhanceNotificationWithAI(notification);
        }
        
        super.showDesktopNotification(notification);
    }
    
    private UserAvailability checkUserAvailability() {
        // Check if user is in a meeting (via calendar integration)
        // Check if user is using fullscreen applications
        // Check do-not-disturb settings
        // etc.
        
        return UserAvailability.AVAILABLE;
    }
    
    private ReminderNotification enhanceNotificationWithAI(ReminderNotification notification) {
        // Use AI to suggest better timing or content
        // Analyze user patterns to optimize notification delivery
        
        return notification;
    }
}
```

### 7.3 Job Dependencies & Workflows

```java
package com.noteflix.pcm.scheduler.workflow;

@Data
@Builder
public class JobDependency {
    private Long jobId;
    private Long dependsOnJobId;
    private DependencyType type;
    private Map<String, Object> conditions;
    
    public enum DependencyType {
        SUCCESS("Thành công"),
        COMPLETION("Hoàn thành"),
        FAILURE("Thất bại"),
        CONDITIONAL("Có điều kiện");
        
        private final String displayName;
    }
}

@Service
public class WorkflowManager {
    
    private final SchedulerDAO schedulerDAO;
    private final SchedulerEngine schedulerEngine;
    
    /**
     * Execute workflow-based job scheduling
     */
    public void handleJobCompletion(Long jobId, JobExecutionResult result) {
        List<JobDependency> dependencies = schedulerDAO.getDependentJobs(jobId);
        
        for (JobDependency dependency : dependencies) {
            if (shouldTriggerDependentJob(dependency, result)) {
                ScheduledJob dependentJob = schedulerDAO.getJob(dependency.getJobId());
                
                // Schedule dependent job for immediate execution
                schedulerEngine.executeJobNow(dependentJob.getId());
                
                log.info("🔗 Triggered dependent job: {} -> {}", 
                    jobId, dependency.getJobId());
            }
        }
    }
    
    private boolean shouldTriggerDependentJob(JobDependency dependency, 
                                            JobExecutionResult result) {
        return switch (dependency.getType()) {
            case SUCCESS -> result.getStatus() == ExecutionStatus.SUCCESS;
            case COMPLETION -> result.getStatus() != ExecutionStatus.CANCELLED;
            case FAILURE -> result.getStatus() == ExecutionStatus.FAILED;
            case CONDITIONAL -> evaluateConditions(dependency.getConditions(), result);
        };
    }
}
```

## 8. Configuration & Settings

### 8.1 Scheduler Configuration

```properties
# scheduler.properties

# Scheduler Engine Settings
scheduler.enabled=true
scheduler.threadPoolSize=4
scheduler.missedJobThreshold=300000  # 5 minutes
scheduler.monitorInterval=60000      # 1 minute

# Notification Settings
notification.desktop.enabled=true
notification.systemTray.enabled=true
notification.sound.enabled=true
notification.email.enabled=false

# Email Settings (if enabled)
notification.email.smtp.host=smtp.gmail.com
notification.email.smtp.port=587
notification.email.smtp.username=your-email@gmail.com
notification.email.smtp.password=your-app-password
notification.email.smtp.starttls=true

# Job Execution Settings
job.timeout.default=300000           # 5 minutes
job.timeout.backup=3600000          # 1 hour
job.timeout.report=1800000          # 30 minutes

# Persistence Settings
job.history.retention.days=30
job.cleanup.interval=86400000       # 1 day

# UI Settings
calendar.defaultView=week
calendar.showWeekNumbers=false
calendar.startOfWeek=monday
```

### 8.2 User Preferences UI

```java
package com.noteflix.pcm.ui.dialogs;

public class SchedulerPreferencesDialog extends Dialog<Void> {
    
    @FXML private CheckBox enableSchedulerCheck;
    @FXML private CheckBox enableNotificationsCheck;
    @FXML private CheckBox enableSoundCheck;
    @FXML private CheckBox enableEmailCheck;
    @FXML private Slider notificationDuration;
    @FXML private ComboBox<String> defaultCalendarView;
    @FXML private TextField emailSmtpHost;
    @FXML private TextField emailUsername;
    @FXML private PasswordField emailPassword;
    
    // Implementation...
}
```

## 9. Testing & Quality Assurance

### 9.1 Unit Tests

```java
package com.noteflix.pcm.scheduler.core;

@ExtendWith(MockitoExtension.class)
class SchedulerEngineTest {
    
    @Mock private SchedulerDAO schedulerDAO;
    @Mock private JobExecutor jobExecutor;
    
    private SchedulerEngine schedulerEngine;
    
    @BeforeEach
    void setUp() {
        schedulerEngine = new SchedulerEngine(schedulerDAO);
    }
    
    @Test
    void shouldScheduleJobSuccessfully() {
        // Given
        ScheduledJob job = createTestJob();
        when(schedulerDAO.createJob(job)).thenReturn(1L);
        
        // When
        Long jobId = schedulerEngine.scheduleJob(job);
        
        // Then
        assertThat(jobId).isEqualTo(1L);
        verify(schedulerDAO).createJob(job);
        verify(schedulerDAO).updateNextExecutionTime(eq(1L), any(LocalDateTime.class));
    }
    
    @Test
    void shouldHandleJobExecutionFailure() {
        // Test error handling scenarios
    }
    
    private ScheduledJob createTestJob() {
        return ScheduledJob.builder()
            .name("Test Job")
            .type(JobType.REMINDER)
            .jobClass("reminder")
            .trigger(JobTrigger.builder()
                .type(TriggerType.ONCE)
                .startTime(LocalDateTime.now().plusMinutes(5))
                .build())
            .parameters(Map.of("title", "Test Reminder"))
            .enabled(true)
            .build();
    }
}
```

### 9.2 Integration Tests

```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class SchedulerIntegrationTest {
    
    @Autowired private SchedulerEngine schedulerEngine;
    @Autowired private SchedulerDAO schedulerDAO;
    
    @Test
    @Transactional
    void shouldExecuteCompleteJobLifecycle() {
        // Create job
        ScheduledJob job = createTestReminderJob();
        Long jobId = schedulerEngine.scheduleJob(job);
        
        // Execute job
        CompletableFuture<JobExecutionResult> future = 
            schedulerEngine.executeJobNow(jobId);
        
        // Verify execution
        JobExecutionResult result = future.join();
        assertThat(result.getStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        
        // Verify history
        List<JobExecutionResult> history = schedulerDAO.getExecutionHistory(jobId);
        assertThat(history).hasSize(1);
    }
}
```

## 10. Performance & Monitoring

### 10.1 Metrics Collection

```java
package com.noteflix.pcm.scheduler.monitoring;

@Service
public class SchedulerMetrics {
    
    private final AtomicLong jobsExecuted = new AtomicLong();
    private final AtomicLong jobsSuccessful = new AtomicLong();
    private final AtomicLong jobsFailed = new AtomicLong();
    private final Map<String, Timer> executionTimers = new ConcurrentHashMap<>();
    
    public void recordJobExecution(ScheduledJob job, JobExecutionResult result) {
        jobsExecuted.incrementAndGet();
        
        if (result.getStatus() == ExecutionStatus.SUCCESS) {
            jobsSuccessful.incrementAndGet();
        } else {
            jobsFailed.incrementAndGet();
        }
        
        // Record execution time
        String jobType = job.getType().name();
        Timer timer = executionTimers.computeIfAbsent(jobType, 
            k -> Timer.create("job.execution.time", "type", k));
        timer.record(result.getDuration());
    }
    
    public SchedulerStatistics getStatistics() {
        return SchedulerStatistics.builder()
            .totalJobsExecuted(jobsExecuted.get())
            .successfulJobs(jobsSuccessful.get())
            .failedJobs(jobsFailed.get())
            .averageExecutionTimes(calculateAverageExecutionTimes())
            .build();
    }
}
```

### 10.2 Health Check

```java
@Component
public class SchedulerHealthIndicator implements HealthIndicator {
    
    private final SchedulerEngine schedulerEngine;
    private final SchedulerDAO schedulerDAO;
    
    @Override
    public Health health() {
        try {
            // Check if scheduler is running
            if (!schedulerEngine.isRunning()) {
                return Health.down()
                    .withDetail("scheduler", "not running")
                    .build();
            }
            
            // Check database connectivity
            schedulerDAO.testConnection();
            
            // Check for stuck jobs
            List<ScheduledJob> stuckJobs = schedulerDAO.getStuckJobs();
            if (!stuckJobs.isEmpty()) {
                return Health.degraded()
                    .withDetail("stuck_jobs", stuckJobs.size())
                    .build();
            }
            
            return Health.up()
                .withDetail("active_jobs", schedulerDAO.getActiveJobCount())
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## Tổng kết Implementation

Tài liệu này cung cấp blueprint hoàn chỉnh cho việc implement hệ thống scheduling và reminder trong PCM Desktop Application:

### Key Features:
1. **Flexible Scheduling**: Support cho multiple trigger types (once, interval, cron, daily, etc.)
2. **Job Types**: Reminder, backup, notification, cleanup, sync jobs
3. **Smart Notifications**: Desktop, tray, email notifications với AI enhancement
4. **Workflow Support**: Job dependencies và conditional execution
5. **Calendar Integration**: Visual calendar interface cho job management
6. **Monitoring**: Comprehensive metrics và health checking
7. **User-friendly UI**: Intuitive interface cho job creation và management

### Architecture Benefits:
- **Scalable**: Thread-pool based execution với configurable parameters
- **Reliable**: Database persistence với transaction support
- **Extensible**: Plugin architecture cho custom job types
- **Maintainable**: Clean separation of concerns và comprehensive testing

Hệ thống này có thể được implement theo từng phase, bắt đầu từ core scheduling engine và dần thêm các advanced features như workflow management và AI-enhanced notifications.