package com.dotwavesoftware.importscheduler.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dotwavesoftware.importscheduler.dto.ImportMappingDTO;
import com.dotwavesoftware.importscheduler.dto.ImportResponseDTO;
import com.dotwavesoftware.importscheduler.entity.ConnectionImportMappingEntity;
import com.dotwavesoftware.importscheduler.entity.ImportEntity;
import com.dotwavesoftware.importscheduler.repository.ConnectionImportMappingRepository;
import com.dotwavesoftware.importscheduler.repository.ImportRepository;
import com.dotwavesoftware.importscheduler.service.ImportProgressService;
import com.dotwavesoftware.importscheduler.service.ImportService;
import com.dotwavesoftware.importscheduler.service.JobSchedulerService;
import com.dotwavesoftware.importscheduler.worker.HubSpotPayload;
import com.dotwavesoftware.importscheduler.worker.Job;
import com.dotwavesoftware.importscheduler.worker.JobType;
import com.dotwavesoftware.importscheduler.worker.JobWorker;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api")
public class ImportController {
    private static final Logger logger = Logger.getLogger(ImportController.class.getName());
    private final ImportService importService;
    private final ImportRepository importRepository;
    private final ConnectionImportMappingRepository mappingRepository;
    private final JobWorker jobWorker;
    private final JobSchedulerService jobSchedulerService;
    private final ImportProgressService importProgressService;

    public ImportController(ImportService importService,
                            ImportRepository importRepository,
                            ConnectionImportMappingRepository mappingRepository,
                            JobWorker jobWorker,
                            JobSchedulerService jobSchedulerService,
                            ImportProgressService importProgressService) {
        this.importService = importService;
        this.importRepository = importRepository;
        this.mappingRepository = mappingRepository;
        this.jobWorker = jobWorker;
        this.jobSchedulerService = jobSchedulerService;
        this.importProgressService = importProgressService;
    }

    @PostMapping("/imports")
    public ResponseEntity<String> saveImport(@RequestBody ImportMappingDTO request) {
        logger.info("Received import request for user: " + request.getUserUuid());
        
        
        try {
            ImportEntity savedImport = importService.saveImport(request);
            
            if (savedImport != null) {
                logger.info("Import saved successfully with ID: " + savedImport.getId());
                
                // Automatically schedule the new import
                jobSchedulerService.scheduleImportById(savedImport.getId());
                logger.info("Scheduled import ID: " + savedImport.getId());
                
                return ResponseEntity.ok().body("Import saved successfully with ID: " + savedImport.getId());
            } else {
                logger.warning("Failed to save import");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to save import");
            }
        } catch (Exception e) {
            logger.severe("Error saving import: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving import: " + e.getMessage());
        }
    }

    @GetMapping("/imports")
    public ResponseEntity<List<ImportResponseDTO>> getImportsByUser(@RequestParam String userUuid) {
        logger.info("Fetching imports for user: " + userUuid);
        
        try {
            List<ImportResponseDTO> imports = importService.getImportsByUserUuid(UUID.fromString(userUuid));
            logger.info("Found " + imports.size() + " imports for user: " + userUuid);
            return ResponseEntity.ok(imports);
        } catch (Exception e) {
            logger.severe("Error fetching imports: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/imports/{id}")
    public ResponseEntity<String> deleteImport(@PathVariable Integer id) {
        logger.info("Deleting import with ID: " + id);
        
        try {
            boolean deleted = importService.deleteImport(id);
            
            if (deleted) {
                // Cancel any scheduled job for this import
                jobSchedulerService.cancelJob(id);
                logger.info("Import deleted successfully: " + id);
                return ResponseEntity.ok("Import deleted successfully");
            } else {
                logger.warning("Import not found: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Import not found");
            }
        } catch (Exception e) {
            logger.severe("Error deleting import: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting import: " + e.getMessage());
        }
    }

    @PostMapping("/imports/{id}/run")
    public ResponseEntity<String> runImport(@PathVariable Integer id) {
        logger.info("Running import with ID: " + id);
        
        try {
            // Get the import
            Optional<ImportEntity> importOpt = importRepository.findById(id);
            if (importOpt.isEmpty()) {
                logger.warning("Import not found: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Import not found");
            }
            
            ImportEntity importEntity = importOpt.get();
            String hubspotListId = importEntity.getHubspotListId();
            
            if (hubspotListId == null || hubspotListId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Import has no HubSpot list configured");
            }
            
            // Get connection IDs from the first mapping (all mappings share the same connections)
            List<ConnectionImportMappingEntity> mappings = mappingRepository.findByImportId(id);
            if (mappings.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Import has no field mappings configured");
            }
            
            ConnectionImportMappingEntity firstMapping = mappings.get(0);
            int hubspotConnectionId = firstMapping.getId().getSendingConnectionId();
            int five9ConnectionId = firstMapping.getId().getReceivingConnectionId();
            
            // Create payload and job
            HubSpotPayload payload = new HubSpotPayload(
                five9ConnectionId,
                hubspotConnectionId,
                hubspotListId,
                id
            );
            
            Job job = new Job(JobType.hubspot, payload);
            
            logger.info("Executing HubSpot import - Five9 Connection: " + five9ConnectionId + 
                       ", HubSpot Connection: " + hubspotConnectionId + 
                       ", HubSpot List: " + hubspotListId);
            
            jobWorker.execute(job);
            
            return ResponseEntity.ok("Import started successfully for import ID: " + id);
            
        } catch (Exception e) {
            logger.severe("Error running import: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error running import: " + e.getMessage());
        }
    }

    @PostMapping("/imports/{id}/start")
    public ResponseEntity<String> startImport(@PathVariable Integer id) {
        logger.info("Starting import with ID: " + id);
        
        try {
            // Update status to STARTING first
            importRepository.updateStatus(id, "STARTING");
            importProgressService.sendStatus(id, "STARTING", "Import is starting...");
            
            // Update start_datetime to current time
            importService.updateStartDatetime(id);
            
            // Execute import on virtual thread and schedule for future runs
            // The job will set status to ACTIVE when it actually starts running
            boolean started = jobSchedulerService.executeImportNow(id);
            
            if (!started) {
                importRepository.updateStatus(id, "FAILED");
                importProgressService.sendStatus(id, "FAILED", "Failed to start import");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failed to start import - check configuration");
            }
            
            return ResponseEntity.ok("Import started successfully");
            
        } catch (Exception e) {
            logger.severe("Error starting import: " + e.getMessage());
            importRepository.updateStatus(id, "FAILED");
            importProgressService.sendStatus(id, "FAILED", "Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error starting import: " + e.getMessage());
        }
    }

    @PostMapping("/imports/{id}/stop")
    public ResponseEntity<String> stopImport(@PathVariable Integer id) {
        logger.info("Stopping import with ID: " + id);
        
        try {
            // Update status to PAUSED
            importRepository.updateStatus(id, "PAUSED");
            importProgressService.sendStatus(id, "PAUSED", "Import paused");
            
            // TODO: Implement actual job cancellation if needed
            
            logger.info("Import stopped: " + id);
            return ResponseEntity.ok("Import stopped successfully");
            
        } catch (Exception e) {
            logger.severe("Error stopping import: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error stopping import: " + e.getMessage());
        }
    }

    @GetMapping("/imports/schedules")
    public ResponseEntity<Map<Integer, String>> getScheduledCronExpressions() {
        logger.info("Getting all scheduled cron expressions");
        Map<Integer, String> expressions = jobSchedulerService.getScheduledCronExpressions();
        logger.info("Found " + expressions.size() + " scheduled cron expressions");
        return ResponseEntity.ok(expressions);
    }
}
