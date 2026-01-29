package com.dotwavesoftware.importscheduler.repository;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.dotwavesoftware.importscheduler.entity.ConnectionEntity;
import com.dotwavesoftware.importscheduler.entity.ConnectionImportIdKey;
import com.dotwavesoftware.importscheduler.entity.ConnectionImportMappingEntity;
import com.dotwavesoftware.importscheduler.entity.ImportEntity;
import com.dotwavesoftware.importscheduler.entity.MappingFormatEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

public class ConnectionImportMappingRowMapper implements RowMapper<ConnectionImportMappingEntity> {
    
    @Override
    public ConnectionImportMappingEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        ConnectionImportMappingEntity entity = new ConnectionImportMappingEntity();
        
        // Map composite key (now includes field names)
        int sendingConnectionId = rs.getInt("sending_connection_id");
        int receivingConnectionId = rs.getInt("receiving_connection_id");
        int importId = rs.getInt("import_id");
        String sendingFieldName = rs.getString("sending_connection_field_name");
        String receivingFieldName = rs.getString("receiving_connection_field_name");
        ConnectionImportIdKey compositeId = new ConnectionImportIdKey(
            sendingConnectionId, receivingConnectionId, importId, sendingFieldName, receivingFieldName);
        entity.setId(compositeId);
        
        // Map UUID
        byte[] uuidBytes = rs.getBytes("uuid");
        if (uuidBytes != null) {
            entity.setUuid(ConversionUtil.bytesToUuid(uuidBytes));
        }
        
        // Map timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp modifiedAt = rs.getTimestamp("modified_at");
        entity.setCreatedAt(ConversionUtil.toLocalDateTime(createdAt));
        entity.setModifiedAt(ConversionUtil.toLocalDateTime(modifiedAt));
        
        // Map foreign key relationships (set only IDs)
        ConnectionEntity sendingConnection = new ConnectionEntity();
        sendingConnection.setId(sendingConnectionId);
        entity.setSendingConnection(sendingConnection);
        
        ConnectionEntity receivingConnection = new ConnectionEntity();
        receivingConnection.setId(receivingConnectionId);
        entity.setReceivingConnection(receivingConnection);
        
        ImportEntity importEntity = new ImportEntity();
        importEntity.setId(importId);
        entity.setImportEntity(importEntity);
        
        int mappingFormatId = rs.getInt("mapping_format_id");
        if (!rs.wasNull()) {
            MappingFormatEntity mappingFormat = new MappingFormatEntity();
            mappingFormat.setId(mappingFormatId);
            entity.setMappingFormat(mappingFormat);
        }
        
        // Map five9_key
        entity.setFive9Key(rs.getBoolean("five9_key"));
        
        return entity;
    }
}
