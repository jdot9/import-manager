package com.dotwavesoftware.importscheduler.repository;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import com.dotwavesoftware.importscheduler.entity.MappingFormatEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Repository
public class MappingFormatRepository extends BaseRepository<MappingFormatEntity> {
    
    private static final Logger logger = Logger.getLogger(MappingFormatRepository.class.getName());

    public MappingFormatRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public int save(MappingFormatEntity mappingFormatEntity) {
        String sql = "INSERT INTO mapping_formats (uuid, format, created_at) VALUES (?, ?, NOW())";
        
        int rowsAffected = jdbcTemplate.update(sql,
            ConversionUtil.uuidToBytes(mappingFormatEntity.getUuid()),
            mappingFormatEntity.getFormat()
        );
        
        logger.info("Saving mapping format to database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }
    
    @Override
    public List<MappingFormatEntity> findAll() {
        String sql = "SELECT * FROM mapping_formats";
        List<MappingFormatEntity> mappingFormats = jdbcTemplate.query(sql, new MappingFormatRowMapper());
        logger.info("Retrieving all mapping formats from database. Found: " + mappingFormats.size());
        return mappingFormats;
    }

    @Override
    public Optional<MappingFormatEntity> findById(Integer id) {
        String sql = "SELECT * FROM mapping_formats WHERE id = ?";
        try {
            logger.info("Retrieving mapping format with id " + id + " from database.");
            MappingFormatEntity mappingFormatEntity = jdbcTemplate.queryForObject(sql, new MappingFormatRowMapper(), id);
            return Optional.ofNullable(mappingFormatEntity);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No mapping format found with id " + id);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Failed to retrieve mapping format with id " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public int update(MappingFormatEntity mappingFormatEntity, Integer id) {
        String sql = "UPDATE mapping_formats SET format = ?, modified_at = NOW() WHERE id = ?";
        
        int rowsAffected = jdbcTemplate.update(sql,
            mappingFormatEntity.getFormat(),
            id
        );
        
        logger.info("Updating mapping format with id " + id + " in database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    @Override
    public int deleteById(Integer id) {
        String sql = "DELETE FROM mapping_formats WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Deleting mapping format with id " + id + " from database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }
    
    /**
     * Find mapping format by format string
     * @param format The format string (e.g., "DATE", "STRING", "NUMBER")
     * @return Optional containing the mapping format if found
     */
    public Optional<MappingFormatEntity> findByFormat(String format) {
        String sql = "SELECT * FROM mapping_formats WHERE format = ?";
        try {
            logger.info("Retrieving mapping format with format: " + format);
            MappingFormatEntity mappingFormatEntity = jdbcTemplate.queryForObject(sql, new MappingFormatRowMapper(), format);
            return Optional.ofNullable(mappingFormatEntity);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No mapping format found with format: " + format);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Failed to retrieve mapping format with format " + format + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}
