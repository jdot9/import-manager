package com.dotwavesoftware.importscheduler.repository;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import com.dotwavesoftware.importscheduler.entity.UserEntity;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Repository
public class UserRepository extends BaseRepository<UserEntity> {
  
    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());

    public UserRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public int save(UserEntity user) {

        String email = user.getEmail();
        Optional<UserEntity> userSearchResult = findByEmail(email);
        if (userSearchResult.isPresent()) {
            logger.warning("Cannot create a new user because a user with this email address already exists: " + email);
            return 0;
        }

        String sql = "INSERT INTO users (uuid, first_name, last_name, email, password, oauth_provider, oauth_user_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        
        int rowsAffected = jdbcTemplate.update(sql, 
            ConversionUtil.uuidToBytes(user.getUuid()),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPassword(),
            user.getOauthProvider(),
            user.getOauthUserId()
        );
        
        logger.info("Saving user to database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    @Override
    public List<UserEntity> findAll() {
        String sql = "SELECT * FROM users";
        List<UserEntity> users = jdbcTemplate.query(sql, new UserRowMapper());
        logger.info("Retrieving all users from database. Total users: " + users.size());
        return users;
    }

    @Override
    public Optional<UserEntity> findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            logger.info("Retrieving user with id " + id + " from database.");
            UserEntity user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No user found with id " + id);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Failed to retrieve user with id " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<UserEntity> findByEmail(String email) {
        String sql = "SELECT users.*" +
                     "FROM users " +
                     "WHERE users.email = ?";
        try {
            logger.info("Retrieving user with email: " + email);
            UserEntity user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No user found with email: " + email);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Failed to retrieve user with email " + email + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<UserEntity> findByUUID(UUID uuid) {
        String sql = "SELECT * FROM users WHERE uuid = ?";
        try {
            logger.info("Retrieving user with UUID: " + uuid);
            UserEntity user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), 
            ConversionUtil.uuidToBytes(uuid));
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No user found with UUID: " + uuid);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Failed to retrieve user with UUID " + uuid + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public int update(UserEntity user, Integer id) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, password = ?, " +
                     "oauth_provider = ?, oauth_user_id = ?, modified_at = NOW() " +
                     "WHERE id = ?";
        
        int rowsAffected = jdbcTemplate.update(sql,
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPassword(),
            user.getOauthProvider(),
            user.getOauthUserId(),
            id
        );
        
        logger.info("Updating user with id " + id + " in database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    @Override
    public int deleteById(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Deleting user with id " + id + " from database. Rows affected: " + rowsAffected);
        return rowsAffected;
    }
    
    public void updateLastLogin(Integer id) {
        String sql = "UPDATE users SET last_login_at = NOW() WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Updated last login for user id " + id + ". Rows affected: " + rowsAffected);
    }
}
