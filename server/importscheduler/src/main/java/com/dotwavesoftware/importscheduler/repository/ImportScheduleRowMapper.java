package com.dotwavesoftware.importscheduler.repository;

import org.springframework.jdbc.core.RowMapper;

import com.dotwavesoftware.importscheduler.entity.ImportScheduleEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ImportScheduleRowMapper implements RowMapper<ImportScheduleEntity> {

    @Override
    public ImportScheduleEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        ImportScheduleEntity entity = new ImportScheduleEntity();
        
        entity.setId(rs.getInt("id"));
        entity.setUuid(ConversionUtil.bytesToUuid(rs.getBytes("uuid")));
        
        Timestamp startTs = rs.getTimestamp("start_datetime");
        if (startTs != null) {
            entity.setStartDatetime(startTs.toLocalDateTime());
        }
        
        Timestamp completionTs = rs.getTimestamp("completion_datetime");
        if (completionTs != null) {
            entity.setCompletionDatetime(completionTs.toLocalDateTime());
        }
        
        entity.setRecurring(rs.getBoolean("recurring"));
        entity.setSunday(rs.getBoolean("sunday"));
        entity.setMonday(rs.getBoolean("monday"));
        entity.setTuesday(rs.getBoolean("tuesday"));
        entity.setWednesday(rs.getBoolean("wednesday"));
        entity.setThursday(rs.getBoolean("thursday"));
        entity.setFriday(rs.getBoolean("friday"));
        entity.setSaturday(rs.getBoolean("saturday"));
        entity.setYearly(rs.getBoolean("yearly"));
        entity.setDay(rs.getInt("day"));
        entity.setMonth(rs.getInt("month"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            entity.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp modifiedAt = rs.getTimestamp("modified_at");
        if (modifiedAt != null) {
            entity.setModifiedAt(modifiedAt.toLocalDateTime());
        }
        
        return entity;
    }
}
