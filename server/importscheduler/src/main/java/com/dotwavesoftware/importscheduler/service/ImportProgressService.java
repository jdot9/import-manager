package com.dotwavesoftware.importscheduler.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.dotwavesoftware.importscheduler.dto.ImportProgressDTO;
import com.dotwavesoftware.importscheduler.repository.ImportRepository;

@Service
public class ImportProgressService {

    private static final Logger logger = Logger.getLogger(ImportProgressService.class.getName());

    private final SimpMessagingTemplate messagingTemplate;
    private final ImportRepository importRepository;

    // Track progress for each import
    private final Map<Integer, ImportProgressDTO> progressMap = new ConcurrentHashMap<>();

    public ImportProgressService(SimpMessagingTemplate messagingTemplate, ImportRepository importRepository) {
        this.messagingTemplate = messagingTemplate;
        this.importRepository = importRepository;
    }

    /**
     * Send status update for an import (without progress)
     */
    public void sendStatus(int importId, String status, String message) {
        ImportProgressDTO progress = new ImportProgressDTO(
            importId,
            status,
            0,
            0,
            message,
            null
        );
        
        progressMap.put(importId, progress);
        
        logger.info("Broadcasting status for import " + importId + ": " + status + " - " + message);
        messagingTemplate.convertAndSend("/topic/import-progress", progress);
    }

    /**
     * Send progress update for an import
     */
    public void sendProgress(int importId, String status, int currentRecord, int totalRecords, String message) {
        ImportProgressDTO progress = new ImportProgressDTO(
            importId,
            status,
            currentRecord,
            totalRecords,
            message,
            null
        );
        
        progressMap.put(importId, progress);
        
        // Save progress to database
        importRepository.updateProgress(importId, currentRecord, totalRecords);
        
        logger.info("Broadcasting progress for import " + importId + ": " + currentRecord + "/" + totalRecords + " - " + status);
        messagingTemplate.convertAndSend("/topic/import-progress", progress);
    }

    /**
     * Send completion update for an import
     * @param importId The import ID
     * @param success Whether the import was successful
     * @param recordsImported Number of records successfully imported (total - failed)
     * @param totalRecords Total number of records attempted
     * @param progressPercent The final progress percentage (0-100)
     */
    public void sendCompletion(int importId, boolean success, int recordsImported, int totalRecords, int progressPercent) {
        String status = success ? "COMPLETED" : "FAILED";
        LocalDateTime completionTime = LocalDateTime.now();
        
        ImportProgressDTO progress = new ImportProgressDTO(
            importId,
            status,
            recordsImported,
            totalRecords,
            success ? "Import completed successfully" : "Import completed with failures",
            completionTime
        );
        
        progressMap.put(importId, progress);
        
        // Update status and progress in database
        importRepository.updateStatusAndProgress(importId, status, recordsImported, totalRecords);
        
        // Save progress percentage
        importRepository.updateProgress(importId, progressPercent);
        
        // Save completion datetime to import_schedules table
        importRepository.updateCompletionDatetime(importId, completionTime);
        
        logger.info("Broadcasting completion for import " + importId + ": " + status + " (imported: " + recordsImported + "/" + totalRecords + ", progress: " + progressPercent + "%)");
        messagingTemplate.convertAndSend("/topic/import-progress", progress);
    }

    /**
     * Send started notification for an import
     */
    public void sendStarted(int importId, int totalRecords) {
        ImportProgressDTO progress = new ImportProgressDTO(
            importId,
            "ACTIVE",
            0,
            totalRecords,
            "Import started",
            null
        );
        
        progressMap.put(importId, progress);
        
        // Update status and progress in database
        importRepository.updateStatusAndProgress(importId, "ACTIVE", 0, totalRecords);
        
        logger.info("Broadcasting start for import " + importId + " with " + totalRecords + " total records");
        messagingTemplate.convertAndSend("/topic/import-progress", progress);
    }

    /**
     * Get current progress for an import
     */
    public ImportProgressDTO getProgress(int importId) {
        return progressMap.get(importId);
    }

    /**
     * Get all current progress
     */
    public Map<Integer, ImportProgressDTO> getAllProgress() {
        return new ConcurrentHashMap<>(progressMap);
    }
}
