package com.dotwavesoftware.importscheduler.repository;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.dotwavesoftware.importscheduler.entity.UserEntity;
import com.dotwavesoftware.importscheduler.entity.UserSecurityQuestionEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

public class UserSecurityQuestionRowMapper implements RowMapper<UserSecurityQuestionEntity> {
    
    @Override
    public UserSecurityQuestionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserSecurityQuestionEntity userSecurityQuestion = new UserSecurityQuestionEntity();
        
        // Map BaseEntity fields
        userSecurityQuestion.setId(rs.getInt("id"));
        
        byte[] uuidBytes = rs.getBytes("uuid");
        if (uuidBytes != null) {
            userSecurityQuestion.setUuid(ConversionUtil.bytesToUuid(uuidBytes));
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp modifiedAt = rs.getTimestamp("modified_at");
        userSecurityQuestion.setCreatedAt(ConversionUtil.toLocalDateTime(createdAt));
        userSecurityQuestion.setModifiedAt(ConversionUtil.toLocalDateTime(modifiedAt));
        
        // Map UserSecurityQuestionEntity fields
        userSecurityQuestion.setQuestion(rs.getString("question"));
        userSecurityQuestion.setAnswer(rs.getString("answer"));
        
        // Map User relationship (create minimal object with just ID)
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            UserEntity user = new UserEntity();
            user.setId(userId);
            userSecurityQuestion.setUser(user);
        }
        
        return userSecurityQuestion;
    }
}
