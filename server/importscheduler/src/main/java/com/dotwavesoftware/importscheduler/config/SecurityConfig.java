package com.dotwavesoftware.importscheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.dotwavesoftware.importscheduler.entity.UserEntity;
import com.dotwavesoftware.importscheduler.service.OAuth2UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger logger = Logger.getLogger(SecurityConfig.class.getName());
    
    private final OAuth2UserService oauth2UserService;
    
    // Use constructor injection with @Lazy to break circular dependency
    public SecurityConfig(@Lazy OAuth2UserService oauth2UserService) {
        this.oauth2UserService = oauth2UserService;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for easier API integration
            .cors(cors -> cors.configure(http)) // Enable CORS
            .authorizeHttpRequests(auth -> auth
                // Allow public access to auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Allow OAuth2 authorization and login endpoints
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/**").permitAll()
                // Allow WebSocket endpoints
                .requestMatchers("/ws/**").permitAll()
                // Allow all API endpoints (you can customize this later)
                .requestMatchers("/api/**").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2AuthenticationSuccessHandler())
            );

        return http.build();
    }

    /**
     * Custom OAuth2 authentication success handler
     * Processes OAuth2 user and redirects to React frontend
     */
    @Bean
    public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            try {
                OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                OAuth2User oauth2User = oauth2Token.getPrincipal();
                
                // Extract user information from OAuth2 provider
                String email = oauth2User.getAttribute("email");
                String givenName = oauth2User.getAttribute("given_name");
                String familyName = oauth2User.getAttribute("family_name");
                String name = oauth2User.getAttribute("name");
                String providerId = oauth2User.getAttribute("sub");
                String provider = oauth2Token.getAuthorizedClientRegistrationId();
                
                logger.info("OAuth2 login successful for: " + email);
                
                // Parse name if given_name/family_name not available
                String firstName = givenName != null ? givenName : "";
                String lastName = familyName != null ? familyName : "";
                
                if (firstName.isEmpty() && name != null) {
                    String[] nameParts = name.split(" ", 2);
                    firstName = nameParts[0];
                    lastName = nameParts.length > 1 ? nameParts[1] : "";
                }
                
                // Process OAuth2 user (create or update in database)
                UserEntity user = oauth2UserService.processOAuth2User(
                    email, 
                    firstName, 
                    lastName, 
                    provider, 
                    providerId
                );
                
                // Redirect to React frontend login page to process OAuth
                String redirectUrl = "http://localhost:5173/login?oauth=success&uuid=" + 
                    URLEncoder.encode(user.getUuid().toString(), StandardCharsets.UTF_8) +
                    "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
                
                logger.info("Redirecting to: " + redirectUrl);
                response.sendRedirect(redirectUrl);
                
            } catch (Exception e) {
                logger.severe("Error processing OAuth2 login: " + e.getMessage());
                e.printStackTrace();
                response.sendRedirect("http://localhost:5173/login?error=oauth_failed");
            }
        };
    }

    /**
     * BCryptPasswordEncoder bean for password hashing
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
