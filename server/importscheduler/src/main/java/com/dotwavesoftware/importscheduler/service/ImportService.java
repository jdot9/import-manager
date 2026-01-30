package com.dotwavesoftware.importscheduler.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.dotwavesoftware.importscheduler.dto.ImportMappingDTO;
import com.dotwavesoftware.importscheduler.dto.ImportResponseDTO;
import com.dotwavesoftware.importscheduler.entity.ConnectionImportIdKey;
import com.dotwavesoftware.importscheduler.entity.ConnectionImportMappingEntity;
import com.dotwavesoftware.importscheduler.entity.ImportEntity;
import com.dotwavesoftware.importscheduler.entity.MappingFormatEntity;
import com.dotwavesoftware.importscheduler.entity.UserEntity;
import com.dotwavesoftware.importscheduler.repository.ConnectionImportMappingRepository;
import com.dotwavesoftware.importscheduler.repository.ImportRepository;
import com.dotwavesoftware.importscheduler.repository.UserRepository;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

@Service
public class ImportService {
    private static final Logger logger = Logger.getLogger(ImportService.class.getName());
    private final ImportRepository importRepository;
    private final ConnectionImportMappingRepository mappingRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public ImportService(ImportRepository importRepository, 
                         ConnectionImportMappingRepository mappingRepository,
                         UserRepository userRepository,
                         JdbcTemplate jdbcTemplate) {
        this.importRepository = importRepository;
        this.mappingRepository = mappingRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Save a complete import with schedule and mappings
     * @param request The import request DTO
     * @return The saved ImportEntity, or null if save failed
     */
    public ImportEntity saveImport(ImportMappingDTO request) {
        logger.info("Saving import for user: " + request.getUserUuid());
        
        // Find the user by UUID
        Optional<UserEntity> userOpt = userRepository.findByUUID(UUID.fromString(request.getUserUuid()));
        if (userOpt.isEmpty()) {
            logger.warning("User not found with UUID: " + request.getUserUuid());
            return null;
        }
        
        // Create and save the import entity
        ImportEntity importEntity = new ImportEntity();
        importEntity.setUuid(UUID.randomUUID());
        importEntity.setName(request.getImportName());
        importEntity.setStatus("PAUSED");
        importEntity.setEmailNotification(request.getEmailNotifications() != null && request.getEmailNotifications());
        String email = request.getEmail();
        importEntity.setEmail(email != null && !email.isEmpty() ? email : null);
        importEntity.setUser(userOpt.get());
        
        // Get hubspotListId from the first mapping item (all mappings share the same list)
        if (request.getMapping() != null && !request.getMapping().isEmpty()) {
            importEntity.setHubspotListId(request.getMapping().get(0).getHubspotListId());
        }
        
        int result = importRepository.save(importEntity);
        if (result <= 0) {
            logger.warning("Failed to save import entity");
            return null;
        }
        
        // Retrieve the saved import to get the generated ID
        Optional<ImportEntity> savedImport = importRepository.findByName(request.getImportName());
        if (savedImport.isEmpty()) {
            logger.warning("Failed to retrieve saved import");
            return null;
        }
        
        ImportEntity saved = savedImport.get();
        
        // Save the schedule
        if (request.getSchedule() != null) {
            saveImportSchedule(request.getSchedule(), saved);
        }
        
        // Save the mappings
        if (request.getMapping() != null && !request.getMapping().isEmpty()) {
            saveImportMapping(request.getMapping(), saved);
        }
        
        logger.info("Import saved successfully with ID: " + saved.getId());
        return saved;
    }

    /**
     * Save the import schedule
     * @param schedule The schedule DTO
     * @param importEntity The parent import entity
     */
    public void saveImportSchedule(ImportMappingDTO.Schedule schedule, ImportEntity importEntity) {
        logger.info("Saving import schedule for import ID: " + importEntity.getId());
        
        String sql = "INSERT INTO import_schedules (uuid, import_id, start_datetime, completion_datetime, " +
                     "recurring, sunday, monday, tuesday, wednesday, thursday, friday, saturday, " +
                     "yearly, day, month, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        // Parse date strings to LocalDateTime
        LocalDateTime startDateTime = parseDateTime(schedule.getStartDate());
        LocalDateTime completionDateTime = parseDateTime(schedule.getStopDate());
        
        // Parse day and month as integers (default to 0 if null or empty)
        int day = parseIntOrDefault(schedule.getDay(), 0);
        int month = parseIntOrDefault(schedule.getMonth(), 0);
        
        int rowsAffected = jdbcTemplate.update(sql,
            ConversionUtil.uuidToBytes(UUID.randomUUID()),
            importEntity.getId(),
            startDateTime,
            completionDateTime,
            schedule.getRecurring() != null && schedule.getRecurring(),
            schedule.getSunday() != null && schedule.getSunday(),
            schedule.getMonday() != null && schedule.getMonday(),
            schedule.getTuesday() != null && schedule.getTuesday(),
            schedule.getWednesday() != null && schedule.getWednesday(),
            schedule.getThursday() != null && schedule.getThursday(),
            schedule.getFriday() != null && schedule.getFriday(),
            schedule.getSaturday() != null && schedule.getSaturday(),
            schedule.getYearly() != null && schedule.getYearly(),
            day,
            month
        );
        
        // Create cron job here

        logger.info("Import schedule saved. Rows affected: " + rowsAffected);
    }

    /**
     * Save the import field mappings
     * @param mappings The list of mapping items
     * @param importEntity The parent import entity
     */
    public void saveImportMapping(List<ImportMappingDTO.MappingItem> mappings, ImportEntity importEntity) {
        logger.info("Saving " + mappings.size() + " mappings for import ID: " + importEntity.getId());
        
        for (ImportMappingDTO.MappingItem item : mappings) {
            ConnectionImportMappingEntity entity = new ConnectionImportMappingEntity();
            
            // Create composite key (now includes field names)
            ConnectionImportIdKey compositeId = new ConnectionImportIdKey();
            compositeId.setSendingConnectionId(item.getHubspotConnectionId());
            compositeId.setReceivingConnectionId(item.getFive9ConnectionId());
            compositeId.setImportId(importEntity.getId());
            compositeId.setSendingConnectionFieldName(item.getHubspotProperty());
            compositeId.setReceivingConnectionFieldName(item.getFive9Field());
            entity.setId(compositeId);
            
            entity.setUuid(UUID.randomUUID());
            // Field names are now part of the composite key only
            
            // Set five9Key (default to false if null)
            entity.setFive9Key(item.getFive9Key() != null && item.getFive9Key() == 1);

            // Set five9 dialing list name
            entity.setFive9DialingList(item.getFive9DialingList());
            
            // Set mapping format if provided
            if (item.getFormatId() != null) {
                MappingFormatEntity mappingFormat = new MappingFormatEntity();
                mappingFormat.setId(item.getFormatId());
                entity.setMappingFormat(mappingFormat);
            }
            
            int result = mappingRepository.save(entity);
            if (result > 0) {
                logger.info("Saved mapping: " + item.getHubspotProperty() + " -> " + item.getFive9Field());
            } else {
                logger.warning("Failed to save mapping: " + item.getHubspotProperty() + " -> " + item.getFive9Field());
            }
        }
        
        logger.info("Finished saving mappings for import ID: " + importEntity.getId());
    }
    
    /**
     * Parse a datetime string to LocalDateTime
     * @param dateTimeStr The datetime string (expected format: yyyy-MM-dd'T'HH:mm)
     * @return LocalDateTime or null if parsing fails
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            logger.warning("Failed to parse datetime: " + dateTimeStr + ". Error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse a string to int, returning default value if null or invalid
     * @param value The string value
     * @param defaultValue The default value
     * @return Parsed int or default value
     */
    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Delete an import by ID
     * @param id The import ID
     * @return true if deleted, false if not found
     */
    public boolean deleteImport(Integer id) {
        logger.info("Deleting import with ID: " + id);
        
        // Check if import exists
        if (importRepository.findById(id).isEmpty()) {
            logger.warning("Import not found with ID: " + id);
            return false;
        }
        
        // Delete associated mappings first (due to foreign key constraints)
        mappingRepository.deleteByImportId(id);
        
        // Delete the import
        int rowsAffected = importRepository.deleteById(id);
        return rowsAffected > 0;
    }

    /**
     * Get all imports for a specific user
     * @param userUuid The user's UUID
     * @return List of ImportResponseDTO objects
     */
    public List<ImportResponseDTO> getImportsByUserUuid(UUID userUuid) {
        logger.info("Fetching imports for user UUID: " + userUuid);
        
        List<ImportEntity> imports = importRepository.findByUserUuid(userUuid);
        
        return imports.stream()
            .map(entity -> {
                LocalDateTime startDatetime = getScheduleDatetime(entity.getId(), "start_datetime");
                LocalDateTime completionDatetime = getScheduleDatetime(entity.getId(), "completion_datetime");
                return new ImportResponseDTO(
                    entity.getId(),
                    entity.getUuid() != null ? entity.getUuid().toString() : null,
                    entity.getName(),
                    entity.getStatus(),
                    entity.isEmailNotification(),
                    entity.getEmail(),
                    entity.getCreatedAt(),
                    entity.getModifiedAt(),
                    startDatetime,
                    completionDatetime,
                    entity.getRecordsImported(),
                    entity.getTotalRecords(),
                    entity.getProgress()
                );
            })
            .toList();
    }

    /**
     * Get a datetime field from an import's schedule
     * @param importId The import ID
     * @param columnName The column name (start_datetime or completion_datetime)
     * @return The datetime value, or null if not found
     */
    private LocalDateTime getScheduleDatetime(Integer importId, String columnName) {
        String sql = "SELECT " + columnName + " FROM import_schedules WHERE import_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, LocalDateTime.class, importId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Update start_datetime to current time for an import
     * @param importId The import ID
     * @return Number of rows affected
     */
    public int updateStartDatetime(Integer importId) {
        String sql = "UPDATE import_schedules SET start_datetime = NOW(), modified_at = NOW() WHERE import_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, importId);
        logger.info("Updated start_datetime for import id " + importId + " to current time. Rows affected: " + rowsAffected);
        return rowsAffected;
    }
}
