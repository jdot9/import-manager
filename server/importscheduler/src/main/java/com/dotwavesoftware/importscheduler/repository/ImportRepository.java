package com.dotwavesoftware.importscheduler.repository;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import com.dotwavesoftware.importscheduler.entity.ImportEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Repository
public class ImportRepository extends BaseRepository<ImportEntity> {
    
    private static final Logger logger = Logger.getLogger(ImportRepository.class.getName());

    public ImportRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public int save(ImportEntity importEntity) {
        String sql = "INSERT INTO imports (uuid, name, status, email_notification, email, user_uuid, hubspot_list_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        
        int rowsAffected = jdbcTemplate.update(sql,
            ConversionUtil.uuidToBytes(importEntity.getUuid()),
            importEntity.getName(),
            importEntity.getStatus(),
            importEntity.isEmailNotification(),
            importEntity.getEmail(),
            importEntity.getUser() != null ? ConversionUtil.uuidToBytes(importEntity.getUser().getUuid()) : null,
            importEntity.getHubspotListId()
        );
        
        logger.info("Saving import to database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    @Override
    public List<ImportEntity> findAll() {
        String sql = "SELECT * FROM imports";
        List<ImportEntity> imports = jdbcTemplate.query(sql, new ImportRowMapper());
        logger.info("Retrieving all imports from database. Found: " + imports.size());
        return imports;
    }

    @Override
    public Optional<ImportEntity> findById(Integer id) {
        String sql = "SELECT * FROM imports WHERE id = ?";
        try {
            logger.info("Retrieving import with id " + id + " from database.");
            ImportEntity importEntity = jdbcTemplate.queryForObject(sql, new ImportRowMapper(), id);
            return Optional.ofNullable(importEntity);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No import found with id " + id);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Failed to retrieve import with id " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public int update(ImportEntity importEntity, Integer id) {
        String sql = "UPDATE imports SET name = ?, status = ?, email_notification = ?, email = ?, " +
                     "user_uuid = ?, modified_at = NOW() WHERE id = ?";
        
        int rowsAffected = jdbcTemplate.update(sql,
            importEntity.getName(),
            importEntity.getStatus(),
            importEntity.isEmailNotification(),
            importEntity.getEmail(),
            importEntity.getUser() != null ? ConversionUtil.uuidToBytes(importEntity.getUser().getUuid()) : null,
            id
        );
        
        logger.info("Updating import with id " + id + " in database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    @Override
    public int deleteById(Integer id) {
        String sql = "DELETE FROM imports WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Deleting import with id " + id + " from database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }
    
    /**
     * Find all imports for a specific user
     * @param userUuid The user UUID
     * @return List of imports owned by the user
     */
    public List<ImportEntity> findByUserUuid(java.util.UUID userUuid) {
        String sql = "SELECT * FROM imports WHERE user_uuid = ?";
        List<ImportEntity> imports = jdbcTemplate.query(sql, new ImportRowMapper(), ConversionUtil.uuidToBytes(userUuid));
        logger.info("Retrieving imports for user uuid " + userUuid + ". Found: " + imports.size());
        return imports;
    }
    
    /**
     * Find all imports by status
     * @param status The import status (e.g., "ACTIVE", "PAUSED", "COMPLETED")
     * @return List of imports with the specified status
     */
    public List<ImportEntity> findByStatus(String status) {
        String sql = "SELECT * FROM imports WHERE status = ?";
        List<ImportEntity> imports = jdbcTemplate.query(sql, new ImportRowMapper(), status);
        logger.info("Retrieving imports with status '" + status + "'. Found: " + imports.size());
        return imports;
    }
    
    /**
     * Find all imports with email notifications enabled
     * @return List of imports with email notifications enabled
     */
    public List<ImportEntity> findByEmailNotificationEnabled() {
        String sql = "SELECT * FROM imports WHERE email_notification = true";
        List<ImportEntity> imports = jdbcTemplate.query(sql, new ImportRowMapper());
        logger.info("Retrieving imports with email notifications enabled. Found: " + imports.size());
        return imports;
    }
    
    /**
     * Find import by name
     * @param name The import name
     * @return Optional containing the import if found
     */
    public Optional<ImportEntity> findByName(String name) {
        String sql = "SELECT * FROM imports WHERE name = ?";
        try {
            logger.info("Retrieving import with name: " + name);
            ImportEntity importEntity = jdbcTemplate.queryForObject(sql, new ImportRowMapper(), name);
            return Optional.ofNullable(importEntity);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No import found with name: " + name);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Failed to retrieve import with name " + name + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Update import status
     * @param id The import ID
     * @param status The new status
     * @return Number of rows affected
     */
    public int updateStatus(Integer id, String status) {
        String sql = "UPDATE imports SET status = ?, modified_at = NOW() WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, status, id);
        logger.info("Updated status for import id " + id + " to '" + status + "'. Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    /**
     * Update import progress
     * @param id The import ID
     * @param recordsImported Number of records imported so far
     * @param totalRecords Total number of records to import
     * @return Number of rows affected
     */
    public int updateProgress(Integer id, Integer recordsImported, Integer totalRecords) {
        String sql = "UPDATE imports SET records_imported = ?, total_records = ?, modified_at = NOW() WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, recordsImported, totalRecords, id);
        logger.info("Updated progress for import id " + id + ": " + recordsImported + "/" + totalRecords);
        return rowsAffected;
    }

    /**
     * Update import status and progress together
     * @param id The import ID
     * @param status The new status
     * @param recordsImported Number of records imported
     * @param totalRecords Total number of records
     * @return Number of rows affected
     */
    public int updateStatusAndProgress(Integer id, String status, Integer recordsImported, Integer totalRecords) {
        String sql = "UPDATE imports SET status = ?, records_imported = ?, total_records = ?, modified_at = NOW() WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, status, recordsImported, totalRecords, id);
        logger.info("Updated status and progress for import id " + id + ": " + status + " (" + recordsImported + "/" + totalRecords + ")");
        return rowsAffected;
    }

    /**
     * Update completion datetime in import_schedules table
     * @param importId The import ID
     * @param completionDatetime The completion datetime
     * @return Number of rows affected
     */
    public int updateCompletionDatetime(Integer importId, java.time.LocalDateTime completionDatetime) {
        String sql = "UPDATE import_schedules SET completion_datetime = ?, modified_at = NOW() WHERE import_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, completionDatetime, importId);
        logger.info("Updated completion datetime for import id " + importId + ": " + completionDatetime);
        return rowsAffected;
    }

    /**
     * Update progress percentage for an import
     * @param id The import ID
     * @param progress The progress percentage (0-100)
     * @return Number of rows affected
     */
    public int updateProgress(Integer id, Integer progress) {
        String sql = "UPDATE imports SET progress = ?, modified_at = NOW() WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, progress, id);
        logger.info("Updated progress for import id " + id + ": " + progress + "%");
        return rowsAffected;
    }
}
