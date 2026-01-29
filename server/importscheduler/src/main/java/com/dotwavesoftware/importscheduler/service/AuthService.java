package com.dotwavesoftware.importscheduler.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.dotwavesoftware.importscheduler.dto.LoginRequestDTO;
import com.dotwavesoftware.importscheduler.dto.LoginResponseDTO;
import com.dotwavesoftware.importscheduler.entity.UserEntity;
import com.dotwavesoftware.importscheduler.entity.UserSecurityQuestionEntity;
import com.dotwavesoftware.importscheduler.repository.UserRepository;
import com.dotwavesoftware.importscheduler.repository.UserSecurityQuestionRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class AuthService {
    
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());
    private final UserRepository userRepository;
    private final UserSecurityQuestionRepository securityQuestionRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, UserSecurityQuestionRepository securityQuestionRepository, BCryptPasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.securityQuestionRepository = securityQuestionRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Authenticate user with email and password
     * @param loginRequest Login credentials
     * @return LoginResponseDTO if successful, null if authentication fails
     */
    public LoginResponseDTO authenticateUser(LoginRequestDTO loginRequest) {
        logger.info("Attempting to authenticate user with email: " + loginRequest.getEmail());
        
        // Find user by email
        Optional<UserEntity> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        
        if (userOptional.isEmpty()) {
            logger.warning("Authentication failed - user not found with email: " + loginRequest.getEmail());
            return null;
        }
        
        UserEntity user = userOptional.get();
        
        // Verify password using BCrypt
        if (user.getPassword() == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warning("Authentication failed - invalid password for email: " + loginRequest.getEmail());
            return null;
        }
        
        // Update last login timestamp
        userRepository.updateLastLogin(user.getId());
        
        logger.info("User authenticated successfully: " + loginRequest.getEmail());
        
        // Generate a simple token (in production, use JWT)
        String token = UUID.randomUUID().toString();
        

        
        // Create response
        return new LoginResponseDTO(
            user.getUuid(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            token
        );
    }
    
    /**
     * Hash a password using BCrypt
     * @param plainPassword Plain text password
     * @return Hashed password
     */
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * Verify a password against a hash
     * @param plainPassword Plain text password
     * @param hashedPassword Hashed password from database
     * @return true if password matches
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    /**
     * Get security question by email
     * @param email User's email
     * @return Security question or null if not found
     */
    public String getSecurityQuestionByEmail(String email) {
        logger.info("Getting security question for email: " + email);
        
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warning("User not found with email: " + email);
            return null;
        }
        
        UserEntity user = userOptional.get();
        List<UserSecurityQuestionEntity> questions = securityQuestionRepository.findByUserId(user.getId());
        
        if (questions.isEmpty()) {
            logger.warning("No security question found for user: " + email);
            return null;
        }
        
        // Return the first security question
        return questions.get(0).getQuestion();
    }

    /**
     * Verify security answer
     * @param email User's email
     * @param answer Answer to verify
     * @return true if answer matches
     */
    public boolean verifySecurityAnswer(String email, String answer) {
        logger.info("Verifying security answer for email: " + email);
        
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warning("User not found with email: " + email);
            return false;
        }
        
        UserEntity user = userOptional.get();
        List<UserSecurityQuestionEntity> questions = securityQuestionRepository.findByUserId(user.getId());
        
        if (questions.isEmpty()) {
            logger.warning("No security question found for user: " + email);
            return false;
        }
        
        // Verify the answer using BCrypt
        String storedAnswer = questions.get(0).getAnswer();
        return passwordEncoder.matches(answer, storedAnswer);
    }

    /**
     * Reset password for a user
     * @param email User's email
     * @param newPassword New password
     * @return true if password was reset successfully
     */
    public boolean resetPassword(String email, String newPassword) {
        logger.info("Resetting password for email: " + email);
        
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warning("User not found with email: " + email);
            return false;
        }
        
        UserEntity user = userOptional.get();
        
        // Hash the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        
        int result = userRepository.update(user, user.getId());
        
        if (result > 0) {
            logger.info("Password reset successfully for email: " + email);
            // Send confirmation email
            emailService.sendPasswordResetEmail(email, null);
            return true;
        } else {
            logger.warning("Failed to reset password for email: " + email);
            return false;
        }
    }
}

