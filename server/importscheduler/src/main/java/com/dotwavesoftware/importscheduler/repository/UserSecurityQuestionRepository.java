package com.dotwavesoftware.importscheduler.repository;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import com.dotwavesoftware.importscheduler.entity.UserSecurityQuestionEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Repository
public class UserSecurityQuestionRepository extends BaseRepository<UserSecurityQuestionEntity> {
    
    private static final Logger logger = Logger.getLogger(UserSecurityQuestionRepository.class.getName());

    public UserSecurityQuestionRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public int save(UserSecurityQuestionEntity userSecurityQuestion) {
        String sql = "INSERT INTO user_security_questions (uuid, question, answer, user_id, created_at) " +
                     "VALUES (?, ?, ?, ?, NOW())";
        
        int rowsAffected = jdbcTemplate.update(sql,
            ConversionUtil.uuidToBytes(userSecurityQuestion.getUuid()),
            userSecurityQuestion.getQuestion(),
            userSecurityQuestion.getAnswer(),
            userSecurityQuestion.getUser() != null ? userSecurityQuestion.getUser().getId() : null
        );
        
        logger.info("Saving user security question to database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    @Override
    public List<UserSecurityQuestionEntity> findAll() {
        String sql = "SELECT * FROM user_security_questions";
        List<UserSecurityQuestionEntity> userSecurityQuestions = jdbcTemplate.query(sql, new UserSecurityQuestionRowMapper());
        logger.info("Retrieving all user security questions from database. Found: " + userSecurityQuestions.size());
        return userSecurityQuestions;
    }

    @Override
    public Optional<UserSecurityQuestionEntity> findById(Integer id) {
        String sql = "SELECT * FROM user_security_questions WHERE id = ?";
        try {
            logger.info("Retrieving user security question with id " + id + " from database.");
            UserSecurityQuestionEntity userSecurityQuestion = jdbcTemplate.queryForObject(sql, new UserSecurityQuestionRowMapper(), id);
            return Optional.ofNullable(userSecurityQuestion);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No user security question found with id " + id);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Failed to retrieve user security question with id " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public int update(UserSecurityQuestionEntity userSecurityQuestion, Integer id) {
        String sql = "UPDATE user_security_questions SET question = ?, answer = ?, user_id = ?, modified_at = NOW() " +
                     "WHERE id = ?";
        
        int rowsAffected = jdbcTemplate.update(sql,
            userSecurityQuestion.getQuestion(),
            userSecurityQuestion.getAnswer(),
            userSecurityQuestion.getUser() != null ? userSecurityQuestion.getUser().getId() : null,
            id
        );
        
        logger.info("Updating user security question with id " + id + " in database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    @Override
    public int deleteById(Integer id) {
        String sql = "DELETE FROM user_security_questions WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Deleting user security question with id " + id + " from database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }
    
    /**
     * Find all security questions for a specific user
     * @param userId The user ID
     * @return List of security questions for the user
     */
    public List<UserSecurityQuestionEntity> findByUserId(Integer userId) {
        String sql = "SELECT * FROM user_security_questions WHERE user_id = ?";
        List<UserSecurityQuestionEntity> userSecurityQuestions = jdbcTemplate.query(sql, new UserSecurityQuestionRowMapper(), userId);
        logger.info("Retrieving security questions for user id " + userId + ". Found: " + userSecurityQuestions.size());
        return userSecurityQuestions;
    }
    
    /**
     * Delete all security questions for a specific user
     * @param userId The user ID
     * @return Number of rows affected
     */
    public int deleteByUserId(Integer userId) {
        String sql = "DELETE FROM user_security_questions WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId);
        logger.info("Deleting security questions for user id " + userId + ". Rows affected: " + rowsAffected);
        return rowsAffected;
    }
}
