package com.dotwavesoftware.importscheduler.service;

import org.springframework.stereotype.Service;

import com.dotwavesoftware.importscheduler.entity.UserEntity;
import com.dotwavesoftware.importscheduler.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class OAuth2UserService {
    
    private static final Logger logger = Logger.getLogger(OAuth2UserService.class.getName());
    private final UserRepository userRepository;

    public OAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Process OAuth2 user - create if doesn't exist, update if exists
     * @param email User email from OAuth2
     * @param firstName User first name from OAuth2
     * @param lastName User last name from OAuth2
     * @param provider OAuth2 provider (e.g., "google")
     * @param providerId User ID from OAuth2 provider
     * @return UserEntity
     */
    public UserEntity processOAuth2User(String email, String firstName, String lastName, String provider, String providerId) {
        logger.info("Processing OAuth2 user with email: " + email);
        
        // Check if user already exists
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            logger.info("OAuth2 user already exists: " + email);
            UserEntity user = existingUser.get();
            
            // Update OAuth provider info if it's different
            if (user.getOauthProvider() == null || !user.getOauthProvider().equals(provider)) {
                user.setOauthProvider(provider);
                user.setOauthUserId(providerId);
                userRepository.update(user, user.getId());
                logger.info("Updated OAuth2 provider info for user: " + email);
            }
            
            // Update last login
            userRepository.updateLastLogin(user.getId());
            
            return user;
        }
        
        // Create new user
        logger.info("Creating new OAuth2 user: " + email);
        UserEntity newUser = new UserEntity();
        newUser.setUuid(UUID.randomUUID());
        newUser.setEmail(email);
        newUser.setFirstName(firstName != null ? firstName : "");
        newUser.setLastName(lastName != null ? lastName : "");
        newUser.setOauthProvider(provider);
        newUser.setOauthUserId(providerId);
        // No password for OAuth users
        
        int result = userRepository.save(newUser);
        
        if (result > 0) {
            logger.info("OAuth2 user created successfully: " + email);
            // Fetch the user again to get the ID and update last login
            Optional<UserEntity> savedUser = userRepository.findByEmail(email);
            if (savedUser.isPresent()) {
                userRepository.updateLastLogin(savedUser.get().getId());
                return savedUser.get();
            }
            return newUser;
        } else {
            logger.severe("Failed to create OAuth2 user: " + email);
            throw new RuntimeException("Failed to create OAuth2 user");
        }
    }
}

