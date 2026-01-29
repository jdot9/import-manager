package com.dotwavesoftware.importscheduler.controller;

import org.springframework.web.bind.annotation.RestController;

import com.dotwavesoftware.importscheduler.dto.UserDTO;
import com.dotwavesoftware.importscheduler.dto.UserResponseDTO;
import com.dotwavesoftware.importscheduler.entity.UserEntity;
import com.dotwavesoftware.importscheduler.entity.UserSecurityQuestionEntity;
import com.dotwavesoftware.importscheduler.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody UserDTO userDTO) {

        String email = userDTO.getEmail();
        int result = userService.createUser(userDTO);
        if (result < 1) {
            logger.warning("Failed to create user.");
            return ResponseEntity.status(409).body(("Registration failed because a user with this email address already exists: " + email));
        }
        logger.info("User successfully created.");
        return ResponseEntity.status(201).body("User successfully created.");
    } 

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        logger.info(users.size() + " user(s) sent.");
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        try {
            logger.info("ID received.");
            UUID uuid = UUID.fromString(id);
            UserResponseDTO user = userService.getUserByUUID(uuid);
             if (user == null) {
                logger.info("User not found. Response Sent.");
                return ResponseEntity.status(404).body("User " + uuid + " not found.");
              } else {
                logger.info("User found. Response sent.");
                return ResponseEntity.ok(user);    
              }
        
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid UUID string: " + id);
            return ResponseEntity.badRequest().body("Invalid UUID format.");
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserEntity updatedUser) {
        try {
            logger.info("Update request received for user ID: " + id);
            UUID uuid = UUID.fromString(id);
            
            // Check if user exists
            UserResponseDTO existingUser = userService.getUserByUUID(uuid);
            if (existingUser == null) {
                logger.info("User not found. Response sent.");
                return ResponseEntity.status(404).body("User " + uuid + " not found.");
            }
            
            // Update the user
            int rowsAffected = userService.updateUserByUUID(updatedUser, uuid);
            
            if (rowsAffected > 0) {
                // Fetch the updated user to return
                UserResponseDTO updatedUserResponse = userService.getUserByUUID(uuid);
                logger.info("User successfully updated. Response sent.");
                return ResponseEntity.ok(updatedUserResponse);
            } else {
                logger.warning("Failed to update user.");
                return ResponseEntity.status(500).body("Failed to update user.");
            }
        
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid UUID string: " + id);
            return ResponseEntity.badRequest().body("Invalid UUID format.");
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        try {
            logger.info("Delete request received for user ID: " + id);
            UUID uuid = UUID.fromString(id);
            
            // Check if user exists
            UserResponseDTO existingUser = userService.getUserByUUID(uuid);
            if (existingUser == null) {
                logger.info("User not found. Response sent.");
                return ResponseEntity.status(404).body("User " + uuid + " not found.");
            }
            
            // Get the user entity to access the ID
            Optional<UserEntity> userEntity = userService.getUserByEmail(existingUser.getEmail());
            if (userEntity.isEmpty()) {
                return ResponseEntity.status(404).body("User " + uuid + " not found.");
            }
            
            // Delete the user
            int rowsAffected = userService.deleteUser(userEntity.get().getId());
            
            if (rowsAffected > 0) {
                logger.info("User successfully deleted.");
                return ResponseEntity.ok("User " + uuid + " successfully deleted.");
            } else {
                logger.warning("Failed to delete user.");
                return ResponseEntity.status(500).body("Failed to delete user.");
            }
        
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid UUID string: " + id);
            return ResponseEntity.badRequest().body("Invalid UUID format.");
        }
    }
}
