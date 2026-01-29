package com.dotwavesoftware.importscheduler.repository;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.dotwavesoftware.importscheduler.entity.MappingFormatEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

public class MappingFormatRowMapper implements RowMapper<MappingFormatEntity> {
    
    @Override
    public MappingFormatEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        MappingFormatEntity mappingFormatEntity = new MappingFormatEntity();
        
        // Map BaseEntity fields
        mappingFormatEntity.setId(rs.getInt("id"));
        
        byte[] uuidBytes = rs.getBytes("uuid");
        if (uuidBytes != null) {
            mappingFormatEntity.setUuid(ConversionUtil.bytesToUuid(uuidBytes));
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp modifiedAt = rs.getTimestamp("modified_at");
        mappingFormatEntity.setCreatedAt(ConversionUtil.toLocalDateTime(createdAt));
        mappingFormatEntity.setModifiedAt(ConversionUtil.toLocalDateTime(modifiedAt));
        
        // Map MappingFormatEntity fields
        mappingFormatEntity.setFormat(rs.getString("format"));
        
        return mappingFormatEntity;
    }
}
