package com.dotwavesoftware.importscheduler.repository;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.dotwavesoftware.importscheduler.entity.UserEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

public class UserRowMapper implements RowMapper<UserEntity> {
    
    @Override
    public UserEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserEntity user = new UserEntity();
        
        // Map BaseEntity fields
        user.setId(rs.getInt("id"));
        
        // Map UUID (stored as BINARY(16))
        byte[] uuidBytes = rs.getBytes("uuid");
        if (uuidBytes != null) {
            user.setUuid(ConversionUtil.bytesToUuid(uuidBytes));
        }
        
        // Map timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp modifiedAt = rs.getTimestamp("modified_at");
        user.setCreatedAt(ConversionUtil.toLocalDateTime(createdAt));
        user.setModifiedAt(ConversionUtil.toLocalDateTime(modifiedAt));
        
        // Map UserEntity fields
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setOauthProvider(rs.getString("oauth_provider"));
        user.setOauthUserId(rs.getString("oauth_user_id"));
        
        // Map last login timestamp
        Timestamp lastLoginAt = rs.getTimestamp("last_login_at");
        user.setLastLoginAt(ConversionUtil.toLocalDateTime(lastLoginAt));
        

            
        
        
        
        return user;
    }
}
