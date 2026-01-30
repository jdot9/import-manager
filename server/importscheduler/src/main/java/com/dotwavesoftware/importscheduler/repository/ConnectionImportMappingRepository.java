package com.dotwavesoftware.importscheduler.repository;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import com.dotwavesoftware.importscheduler.entity.ConnectionImportMappingEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Repository
public class ConnectionImportMappingRepository {
    
    private static final Logger logger = Logger.getLogger(ConnectionImportMappingRepository.class.getName());
    private final JdbcTemplate jdbcTemplate;

    public ConnectionImportMappingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Save a new connection import mapping
     * @param entity The entity to save
     * @return Number of rows affected
     */
    public int save(ConnectionImportMappingEntity entity) {
        String sql = "INSERT INTO connection_import_mappings (sending_connection_id, receiving_connection_id, " +
                     "import_id, uuid, sending_connection_field_name, receiving_connection_field_name, " +
                     "mapping_format_id, five9_key, five9_dialing_list, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        int rowsAffected = jdbcTemplate.update(sql,
            entity.getId().getSendingConnectionId(),
            entity.getId().getReceivingConnectionId(),
            entity.getId().getImportId(),
            ConversionUtil.uuidToBytes(entity.getUuid()),
            entity.getId().getSendingConnectionFieldName(),
            entity.getId().getReceivingConnectionFieldName(),
            entity.getMappingFormat() != null ? entity.getMappingFormat().getId() : null,
            entity.getFive9Key() != null && entity.getFive9Key() ? 1 : 0,
            entity.getFive9DialingList()
        );
        
        logger.info("Saving connection import mapping to database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }
    
    /**
     * Find all connection import mappings
     * @return List of all mappings
     */
    public List<ConnectionImportMappingEntity> findAll() {
        String sql = "SELECT * FROM connection_import_mappings";
        List<ConnectionImportMappingEntity> mappings = jdbcTemplate.query(sql, new ConnectionImportMappingRowMapper());
        logger.info("Retrieving all connection import mappings from database. Found: " + mappings.size());
        return mappings;
    }

    /**
     * Find by ID - Not applicable for composite keys
     * @param id Not used (composite keys required)
     * @return Empty optional
     * @deprecated Use findByCompositeId instead
     */
    @Deprecated
    public Optional<ConnectionImportMappingEntity> findById(Integer id) {
        logger.warning("findById(Integer) is not applicable for composite keys. Use findByCompositeId instead.");
        return Optional.empty();
    }
    
    /**
     * Find connection import mapping by composite key
     * @param sendingConnectionId The sending connection ID
     * @param receivingConnectionId The receiving connection ID
     * @param importId The import ID
     * @return Optional containing the mapping if found
     */
    public Optional<ConnectionImportMappingEntity> findByCompositeId(Integer sendingConnectionId, 
            Integer receivingConnectionId, Integer importId) {
        String sql = "SELECT * FROM connection_import_mappings WHERE sending_connection_id = ? " +
                     "AND receiving_connection_id = ? AND import_id = ?";
        try {
            logger.info("Retrieving connection import mapping with sending_connection_id " + sendingConnectionId + 
                       ", receiving_connection_id " + receivingConnectionId + " and import_id " + importId);
            ConnectionImportMappingEntity entity = jdbcTemplate.queryForObject(sql, 
                new ConnectionImportMappingRowMapper(), sendingConnectionId, receivingConnectionId, importId);
            return Optional.ofNullable(entity);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No connection import mapping found with the specified composite key");
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Failed to retrieve connection import mapping: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Update by ID - Not applicable for composite keys
     * @param entity The entity with updated data
     * @param id Not used (composite keys required)
     * @return 0
     * @deprecated Use updateByCompositeId instead
     */
    @Deprecated
    public int update(ConnectionImportMappingEntity entity, Integer id) {
        logger.warning("update(entity, Integer) is not applicable for composite keys. Use updateByCompositeId instead.");
        return 0;
    }
    
    /**
     * Update connection import mapping by composite key
     * Note: Since field names are now part of the primary key, only mapping_format can be updated
     * @param entity The entity with updated data
     * @param sendingConnectionId The sending connection ID
     * @param receivingConnectionId The receiving connection ID
     * @param importId The import ID
     * @param sendingFieldName The sending connection field name
     * @param receivingFieldName The receiving connection field name
     * @return Number of rows affected
     */
    public int updateByCompositeId(ConnectionImportMappingEntity entity, Integer sendingConnectionId, 
            Integer receivingConnectionId, Integer importId, String sendingFieldName, String receivingFieldName) {
        String sql = "UPDATE connection_import_mappings SET mapping_format_id = ?, five9_key = ?, modified_at = NOW() " +
                     "WHERE sending_connection_id = ? AND receiving_connection_id = ? AND import_id = ? " +
                     "AND sending_connection_field_name = ? AND receiving_connection_field_name = ?";
        
        int rowsAffected = jdbcTemplate.update(sql,
            entity.getMappingFormat() != null ? entity.getMappingFormat().getId() : null,
            entity.getFive9Key() != null && entity.getFive9Key() ? 1 : 0,
            sendingConnectionId,
            receivingConnectionId,
            importId,
            sendingFieldName,
            receivingFieldName
        );
        
        logger.info("Updating connection import mapping. Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    /**
     * Delete connection import mapping by composite key
     * @param sendingConnectionId The sending connection ID
     * @param receivingConnectionId The receiving connection ID
     * @param importId The import ID
     * @return Number of rows affected
     */
    public int deleteByCompositeId(Integer sendingConnectionId, Integer receivingConnectionId, Integer importId) {
        String sql = "DELETE FROM connection_import_mappings WHERE sending_connection_id = ? " +
                     "AND receiving_connection_id = ? AND import_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, sendingConnectionId, receivingConnectionId, importId);
        logger.info("Deleting connection import mapping. Rows affected: " + rowsAffected);
        return rowsAffected;
    }
    
    /**
     * Find all mappings for a specific sending connection
     * @param sendingConnectionId The sending connection ID
     * @return List of mappings for the connection
     */
    public List<ConnectionImportMappingEntity> findBySendingConnectionId(Integer sendingConnectionId) {
        String sql = "SELECT * FROM connection_import_mappings WHERE sending_connection_id = ?";
        List<ConnectionImportMappingEntity> mappings = jdbcTemplate.query(sql, 
            new ConnectionImportMappingRowMapper(), sendingConnectionId);
        logger.info("Retrieving mappings for sending_connection_id " + sendingConnectionId + ". Found: " + mappings.size());
        return mappings;
    }
    
    /**
     * Find all mappings for a specific receiving connection
     * @param receivingConnectionId The receiving connection ID
     * @return List of mappings for the connection
     */
    public List<ConnectionImportMappingEntity> findByReceivingConnectionId(Integer receivingConnectionId) {
        String sql = "SELECT * FROM connection_import_mappings WHERE receiving_connection_id = ?";
        List<ConnectionImportMappingEntity> mappings = jdbcTemplate.query(sql, 
            new ConnectionImportMappingRowMapper(), receivingConnectionId);
        logger.info("Retrieving mappings for receiving_connection_id " + receivingConnectionId + ". Found: " + mappings.size());
        return mappings;
    }
    
    /**
     * Find all mappings for a specific import
     * @param importId The import ID
     * @return List of mappings for the import
     */
    public List<ConnectionImportMappingEntity> findByImportId(Integer importId) {
        String sql = "SELECT * FROM connection_import_mappings WHERE import_id = ?";
        List<ConnectionImportMappingEntity> mappings = jdbcTemplate.query(sql, 
            new ConnectionImportMappingRowMapper(), importId);
        logger.info("Retrieving mappings for import_id " + importId + ". Found: " + mappings.size());
        return mappings;
    }
    
    /**
     * Find all mappings by mapping format
     * @param mappingFormatId The mapping format ID
     * @return List of mappings with the specified format
     */
    public List<ConnectionImportMappingEntity> findByMappingFormatId(Integer mappingFormatId) {
        String sql = "SELECT * FROM connection_import_mappings WHERE mapping_format_id = ?";
        List<ConnectionImportMappingEntity> mappings = jdbcTemplate.query(sql, 
            new ConnectionImportMappingRowMapper(), mappingFormatId);
        logger.info("Retrieving mappings for mapping_format_id " + mappingFormatId + ". Found: " + mappings.size());
        return mappings;
    }
    
    /**
     * Delete all mappings for a specific import
     * @param importId The import ID
     * @return Number of rows affected
     */
    public int deleteByImportId(Integer importId) {
        String sql = "DELETE FROM connection_import_mappings WHERE import_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, importId);
        logger.info("Deleted all mappings for import_id " + importId + ". Rows affected: " + rowsAffected);
        return rowsAffected;
    }
    
    /**
     * Delete all mappings for a specific sending connection
     * @param sendingConnectionId The sending connection ID
     * @return Number of rows affected
     */
    public int deleteBySendingConnectionId(Integer sendingConnectionId) {
        String sql = "DELETE FROM connection_import_mappings WHERE sending_connection_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, sendingConnectionId);
        logger.info("Deleted all mappings for sending_connection_id " + sendingConnectionId + ". Rows affected: " + rowsAffected);
        return rowsAffected;
    }
    
    /**
     * Delete all mappings for a specific receiving connection
     * @param receivingConnectionId The receiving connection ID
     * @return Number of rows affected
     */
    public int deleteByReceivingConnectionId(Integer receivingConnectionId) {
        String sql = "DELETE FROM connection_import_mappings WHERE receiving_connection_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, receivingConnectionId);
        logger.info("Deleted all mappings for receiving_connection_id " + receivingConnectionId + ". Rows affected: " + rowsAffected);
        return rowsAffected;
    }
}
