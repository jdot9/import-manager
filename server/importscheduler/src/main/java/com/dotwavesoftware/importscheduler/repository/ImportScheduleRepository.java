package com.dotwavesoftware.importscheduler.repository;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import com.dotwavesoftware.importscheduler.entity.ImportScheduleEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Repository
public class ImportScheduleRepository extends BaseRepository<ImportScheduleEntity> {
    
    private static final Logger logger = Logger.getLogger(ImportScheduleRepository.class.getName());

    public ImportScheduleRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public int save(ImportScheduleEntity entity) {
        String sql = "INSERT INTO import_schedules (uuid, import_id, start_datetime, completion_datetime, " +
                     "recurring, sunday, monday, tuesday, wednesday, thursday, friday, saturday, " +
                     "yearly, day, month, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        int rowsAffected = jdbcTemplate.update(sql,
            ConversionUtil.uuidToBytes(entity.getUuid()),
            entity.getImportEntity() != null ? entity.getImportEntity().getId() : null,
            entity.getStartDatetime(),
            entity.getCompletionDatetime(),
            entity.isRecurring(),
            entity.isSunday(),
            entity.isMonday(),
            entity.isTuesday(),
            entity.isWednesday(),
            entity.isThursday(),
            entity.isFriday(),
            entity.isSaturday(),
            entity.isYearly(),
            entity.getDay(),
            entity.getMonth()
        );
        
        logger.info("Saved import schedule. Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    @Override
    public List<ImportScheduleEntity> findAll() {
        String sql = "SELECT * FROM import_schedules";
        List<ImportScheduleEntity> schedules = jdbcTemplate.query(sql, new ImportScheduleRowMapper());
        logger.info("Retrieving all import schedules. Found: " + schedules.size());
        return schedules;
    }

    @Override
    public Optional<ImportScheduleEntity> findById(Integer id) {
        String sql = "SELECT * FROM import_schedules WHERE id = ?";
        try {
            ImportScheduleEntity entity = jdbcTemplate.queryForObject(sql, new ImportScheduleRowMapper(), id);
            return Optional.ofNullable(entity);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public int update(ImportScheduleEntity entity, Integer id) {
        String sql = "UPDATE import_schedules SET start_datetime = ?, completion_datetime = ?, " +
                     "recurring = ?, sunday = ?, monday = ?, tuesday = ?, wednesday = ?, " +
                     "thursday = ?, friday = ?, saturday = ?, yearly = ?, day = ?, month = ?, " +
                     "modified_at = NOW() WHERE id = ?";
        
        return jdbcTemplate.update(sql,
            entity.getStartDatetime(),
            entity.getCompletionDatetime(),
            entity.isRecurring(),
            entity.isSunday(),
            entity.isMonday(),
            entity.isTuesday(),
            entity.isWednesday(),
            entity.isThursday(),
            entity.isFriday(),
            entity.isSaturday(),
            entity.isYearly(),
            entity.getDay(),
            entity.getMonth(),
            id
        );
    }

    @Override
    public int deleteById(Integer id) {
        String sql = "DELETE FROM import_schedules WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    /**
     * Find schedule by import ID
     */
    public Optional<ImportScheduleEntity> findByImportId(Integer importId) {
        String sql = "SELECT * FROM import_schedules WHERE import_id = ?";
        try {
            ImportScheduleEntity entity = jdbcTemplate.queryForObject(sql, new ImportScheduleRowMapper(), importId);
            return Optional.ofNullable(entity);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    /**
     * Find all schedules with their import IDs (for scheduling)
     * Returns all schedules that have a start_datetime set
     */
    public List<ScheduleWithImportId> findAllWithImportId() {
        String sql = "SELECT s.*, i.id as actual_import_id, i.status as import_status, i.hubspot_list_id " +
                     "FROM import_schedules s " +
                     "JOIN imports i ON s.import_id = i.id " +
                     "WHERE s.start_datetime IS NOT NULL";
        
        logger.info("Querying for all import schedules with start_datetime");
        
        List<ScheduleWithImportId> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ImportScheduleEntity schedule = new ImportScheduleRowMapper().mapRow(rs, rowNum);
            return new ScheduleWithImportId(
                schedule,
                rs.getInt("actual_import_id"),
                rs.getString("hubspot_list_id")
            );
        });
        
        logger.info("Found " + results.size() + " schedules to process");
        return results;
    }

    /**
     * Helper record to hold schedule with its import ID
     */
    public record ScheduleWithImportId(
        ImportScheduleEntity schedule,
        int importId,
        String hubspotListId
    ) {}
}
