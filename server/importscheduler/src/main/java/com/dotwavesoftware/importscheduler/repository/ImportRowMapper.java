package com.dotwavesoftware.importscheduler.repository;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.dotwavesoftware.importscheduler.entity.ImportEntity;
import com.dotwavesoftware.importscheduler.entity.UserEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

public class ImportRowMapper implements RowMapper<ImportEntity> {
    
    @Override
    public ImportEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        ImportEntity importEntity = new ImportEntity();
        
        // Map BaseEntity fields
        importEntity.setId(rs.getInt("id"));
        
        byte[] uuidBytes = rs.getBytes("uuid");
        if (uuidBytes != null) {
            importEntity.setUuid(ConversionUtil.bytesToUuid(uuidBytes));
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp modifiedAt = rs.getTimestamp("modified_at");
        importEntity.setCreatedAt(ConversionUtil.toLocalDateTime(createdAt));
        importEntity.setModifiedAt(ConversionUtil.toLocalDateTime(modifiedAt));
        
        // Map ImportEntity fields
        importEntity.setName(rs.getString("name"));
        importEntity.setStatus(rs.getString("status"));
        importEntity.setEmailNotification(rs.getBoolean("email_notification"));
        importEntity.setEmail(rs.getString("email"));
        importEntity.setHubspotListId(rs.getString("hubspot_list_id"));
        
        // Map progress fields
        importEntity.setRecordsImported(rs.getObject("records_imported") != null ? rs.getInt("records_imported") : null);
        importEntity.setTotalRecords(rs.getObject("total_records") != null ? rs.getInt("total_records") : null);
        importEntity.setProgress(rs.getObject("progress") != null ? rs.getInt("progress") : null);
        
        // Map foreign key relationship
        byte[] userUuidBytes = rs.getBytes("user_uuid");
        if (userUuidBytes != null) {
            UserEntity user = new UserEntity();
            user.setUuid(ConversionUtil.bytesToUuid(userUuidBytes));
            importEntity.setUser(user);
        }
        
        return importEntity;
    }
}
