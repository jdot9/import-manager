package com.dotwavesoftware.importscheduler.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

import com.dotwavesoftware.importscheduler.dto.LoginRequestDTO;
import com.dotwavesoftware.importscheduler.dto.LoginResponseDTO;
import com.dotwavesoftware.importscheduler.service.AuthService;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = Logger.getLogger(AuthController.class.getName());
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login endpoint
     * @param loginRequest Login credentials (email and password)
     * @return LoginResponseDTO with user data and token if successful
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        logger.info("Login request received for email: " + loginRequest.getEmail());
        
        // Validate input
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        
        // Authenticate user
        LoginResponseDTO response = authService.authenticateUser(loginRequest);
        
        if (response == null) {
            logger.warning("Login failed for email: " + loginRequest.getEmail());
            return ResponseEntity.status(401).body("Invalid email or password");
        }
        
        logger.info("Login successful for email: " + loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Get security question for password reset
     * @param email User's email
     * @return Security question if user exists
     */
    @GetMapping("/security-question")
    public ResponseEntity<?> getSecurityQuestion(@RequestParam String email) {
        logger.info("Security question request for email: " + email);
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        
        String question = authService.getSecurityQuestionByEmail(email);
        
        if (question == null) {
            logger.warning("No security question found for email: " + email);
            return ResponseEntity.status(404).body(Map.of("error", "Email not found or no security question set"));
        }
        
        logger.info("Security question found for email: " + email);
        return ResponseEntity.ok(Map.of("question", question));
    }

    /**
     * Verify security answer
     * @param request Contains email and answer
     * @return Verification result
     */
    @PostMapping("/verify-security-answer")
    public ResponseEntity<?> verifySecurityAnswer(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String answer = request.get("answer");
        
        logger.info("Verifying security answer for email: " + email);
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        
        if (answer == null || answer.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Answer is required"));
        }
        
        boolean verified = authService.verifySecurityAnswer(email, answer);
        
        if (!verified) {
            logger.warning("Security answer verification failed for email: " + email);
            return ResponseEntity.status(401).body(Map.of("verified", false, "error", "Incorrect answer"));
        }
        
        logger.info("Security answer verified for email: " + email);
        return ResponseEntity.ok(Map.of("verified", true));
    }

    /**
     * Reset password
     * @param request Contains email and newPassword
     * @return Reset result
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        
        logger.info("Password reset request for email: " + email);
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password is required"));
        }
        
        boolean reset = authService.resetPassword(email, newPassword);
        
        if (!reset) {
            logger.warning("Password reset failed for email: " + email);
            return ResponseEntity.status(500).body(Map.of("error", "Password reset failed"));
        }
        
        logger.info("Password reset successful for email: " + email);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}

