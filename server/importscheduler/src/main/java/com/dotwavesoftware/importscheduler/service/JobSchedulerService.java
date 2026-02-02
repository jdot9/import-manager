package com.dotwavesoftware.importscheduler.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import com.dotwavesoftware.importscheduler.entity.ConnectionImportMappingEntity;
import com.dotwavesoftware.importscheduler.entity.ImportScheduleEntity;
import com.dotwavesoftware.importscheduler.repository.ConnectionImportMappingRepository;
import com.dotwavesoftware.importscheduler.repository.ImportRepository;
import com.dotwavesoftware.importscheduler.repository.ImportScheduleRepository;
import com.dotwavesoftware.importscheduler.repository.ImportScheduleRepository.ScheduleWithImportId;
import com.dotwavesoftware.importscheduler.worker.HubSpotPayload;
import com.dotwavesoftware.importscheduler.worker.Job;
import com.dotwavesoftware.importscheduler.worker.JobType;
import com.dotwavesoftware.importscheduler.worker.JobWorker;

import jakarta.annotation.PostConstruct;

@Service
public class JobSchedulerService {

    private static final Logger logger = Logger.getLogger(JobSchedulerService.class.getName());

    private final TaskScheduler taskScheduler;
    private final ImportScheduleRepository importScheduleRepository;
    private final ImportRepository importRepository;
    private final ConnectionImportMappingRepository mappingRepository;
    private final JobWorker jobWorker;
    private final Executor virtualThreadExecutor;

    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<Integer, String> scheduledCronExpressions = new ConcurrentHashMap<>();

    public JobSchedulerService(
            TaskScheduler taskScheduler,
            ImportScheduleRepository importScheduleRepository,
            ImportRepository importRepository,
            ConnectionImportMappingRepository mappingRepository,
            JobWorker jobWorker,
            @Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor) {

        this.taskScheduler = taskScheduler;
        this.importScheduleRepository = importScheduleRepository;
        this.importRepository = importRepository;
        this.mappingRepository = mappingRepository;
        this.jobWorker = jobWorker;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @PostConstruct
    public void scheduleAllJobs() {
        logger.info("Scheduling all active import jobs...");
        List<ScheduleWithImportId> schedules = importScheduleRepository.findAllWithImportId();
        
        for (ScheduleWithImportId scheduleData : schedules) {
            scheduleImport(scheduleData);
        }
        
        logger.info("Scheduled " + scheduledTasks.size() + " import jobs");
    }

    // Schedule an import based on its schedule configuration
    public void scheduleImport(ScheduleWithImportId scheduleData) {
        ImportScheduleEntity schedule = scheduleData.schedule();
        int importId = scheduleData.importId();
        String hubspotListId = scheduleData.hubspotListId();

        // Get connection IDs from mappings
        List<ConnectionImportMappingEntity> mappings = mappingRepository.findByImportId(importId);
        if (mappings.isEmpty()) {
            logger.warning("No mappings found for import ID: " + importId + ", skipping schedule");
            return;
        }

        ConnectionImportMappingEntity firstMapping = mappings.get(0);
        int hubspotConnectionId = firstMapping.getId().getSendingConnectionId();
        int five9ConnectionId = firstMapping.getId().getReceivingConnectionId();

        // Build the cron expression from schedule
        String cronExpression = buildCronExpression(schedule);
        if (cronExpression == null) {
            logger.warning("Could not build cron expression for import ID: " + importId);
            return;
        }

        logger.info("Scheduling import ID: " + importId + " with cron: " + cronExpression);

        // Create the job payload
        HubSpotPayload payload = new HubSpotPayload(
            five9ConnectionId,
            hubspotConnectionId,
            hubspotListId,
            importId
        );

        Job<HubSpotPayload> job = new Job<>(JobType.hubspot, payload);
        
        Runnable task = () -> virtualThreadExecutor.execute(() -> {
            try {
                logger.info("Executing scheduled import ID: " + importId);

                importRepository.updateStatus(importId, "ACTIVE");
                logger.info("Updated import ID: " + importId + " status to ACTIVE");

                jobWorker.execute(job);

            } catch (Exception e) {
                logger.severe("Error executing import ID " + importId + ": " + e.getMessage());
            }
        });

        ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(cronExpression));
        scheduledTasks.put(importId, future);
        scheduledCronExpressions.put(importId, cronExpression);
    }

    /**
     * Schedule or reschedule an import by ID
     */
    public void scheduleImportById(int importId) {
        // Cancel existing schedule if any
        cancelJob(importId);

        // Find the schedule
        importScheduleRepository.findByImportId(importId).ifPresent(schedule -> {
            // Need to get hubspotListId - fetch from findAllWithImportId filtered
            List<ScheduleWithImportId> all = importScheduleRepository.findAllWithImportId();
            all.stream()
                .filter(s -> s.importId() == importId)
                .findFirst()
                .ifPresent(this::scheduleImport);
        });
    }

    /**
     * Execute an import immediately on a virtual thread and schedule for future runs
     * @param importId The import ID to execute
     * @return true if the import was started, false otherwise
     */
    public boolean executeImportNow(int importId) {
        logger.info("Executing import immediately on virtual thread - ID: " + importId);

        // Get the import's hubspotListId
        var importOpt = importRepository.findById(importId);
        if (importOpt.isEmpty()) {
            logger.warning("Import not found: " + importId);
            return false;
        }

        String hubspotListId = importOpt.get().getHubspotListId();
        if (hubspotListId == null || hubspotListId.isEmpty()) {
            logger.warning("Import has no HubSpot list configured: " + importId);
            return false;
        }

        // Get connection IDs from mappings
        List<ConnectionImportMappingEntity> mappings = mappingRepository.findByImportId(importId);
        if (mappings.isEmpty()) {
            logger.warning("No mappings found for import ID: " + importId);
            return false;
        }

        ConnectionImportMappingEntity firstMapping = mappings.get(0);
        int hubspotConnectionId = firstMapping.getId().getSendingConnectionId();
        int five9ConnectionId = firstMapping.getId().getReceivingConnectionId();

        // Create the job payload
        HubSpotPayload payload = new HubSpotPayload(
            five9ConnectionId,
            hubspotConnectionId,
            hubspotListId,
            importId
        );

        Job<HubSpotPayload> job = new Job<>(JobType.hubspot, payload);

        virtualThreadExecutor.execute(() -> {
            try {
                logger.info("Virtual thread executing import ID: " + importId);
                jobWorker.execute(job);

            } catch (Exception e) {
                logger.severe("Error executing import ID " + importId + ": " + e.getMessage());
            }
        });
        // Schedule for future runs based on updated start_datetime
        scheduleImportById(importId);

        return true;
    }

    
    // Cancel a scheduled job
    public void cancelJob(int importId) {
        ScheduledFuture<?> future = scheduledTasks.remove(importId);
        scheduledCronExpressions.remove(importId);
        if (future != null) {
            future.cancel(false);
            logger.info("Cancelled scheduled job for import ID: " + importId);
        }
    }

    /**
     * Build a cron expression from the schedule entity
     * Cron format: second minute hour day-of-month month day-of-week
     */
    private String buildCronExpression(ImportScheduleEntity schedule) {
        LocalDateTime startTime = schedule.getStartDatetime();
        if (startTime == null) {
            return null;
        }

        int second = 0;
        int minute = startTime.getMinute();
        int hour = startTime.getHour();

        if (schedule.isRecurring()) {
            // Recurring schedule - use day-of-week flags
            String daysOfWeek = buildDaysOfWeek(schedule);
            if (daysOfWeek.isEmpty()) {
                // No days selected, default to every day
                daysOfWeek = "*";
            }
            
            if (schedule.isYearly()) {
                // Yearly on specific day and month
                int day = schedule.getDay() > 0 ? schedule.getDay() : startTime.getDayOfMonth();
                int month = schedule.getMonth() > 0 ? schedule.getMonth() : startTime.getMonthValue();
                return String.format("%d %d %d %d %d ?", second, minute, hour, day, month);
            }
            
            // Weekly recurring
            return String.format("%d %d %d ? * %s", second, minute, hour, daysOfWeek);
        } else {
            // One-time schedule - run at specific date/time
            int day = startTime.getDayOfMonth();
            int month = startTime.getMonthValue();
            return String.format("%d %d %d %d %d ?", second, minute, hour, day, month);
        }
    }

    // Build day-of-week string from schedule flags
    private String buildDaysOfWeek(ImportScheduleEntity schedule) {
        StringJoiner joiner = new StringJoiner(",");
        if (schedule.isSunday()) joiner.add("SUN");
        if (schedule.isMonday()) joiner.add("MON");
        if (schedule.isTuesday()) joiner.add("TUE");
        if (schedule.isWednesday()) joiner.add("WED");
        if (schedule.isThursday()) joiner.add("THU");
        if (schedule.isFriday()) joiner.add("FRI");
        if (schedule.isSaturday()) joiner.add("SAT");  
        return joiner.toString();
    }

    // Get count of scheduled tasks
    public int getScheduledTaskCount() {
        return scheduledTasks.size();
    }

     // Get all scheduled cron expressions
    public Map<Integer, String> getScheduledCronExpressions() {
        return new ConcurrentHashMap<>(scheduledCronExpressions);
    }
}
