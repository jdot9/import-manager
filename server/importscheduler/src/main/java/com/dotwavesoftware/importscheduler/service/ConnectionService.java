package com.dotwavesoftware.importscheduler.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.dotwavesoftware.importscheduler.dto.ConnectionDTO;
import com.dotwavesoftware.importscheduler.dto.ContactFieldsResponseDTO;
import com.dotwavesoftware.importscheduler.dto.Five9DialingListDTO;
import com.dotwavesoftware.importscheduler.dto.HubSpotListDTO;
import com.dotwavesoftware.importscheduler.entity.ConnectionEntity;
import com.dotwavesoftware.importscheduler.entity.UserEntity;
import com.dotwavesoftware.importscheduler.repository.ConnectionRepository;
import com.dotwavesoftware.importscheduler.repository.UserRepository;
import com.dotwavesoftware.importscheduler.util.EncryptionUtil;

import reactor.core.publisher.Mono;

@Service
public class ConnectionService {
    
    private static final Logger logger = Logger.getLogger(ConnectionService.class.getName());
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final HubSpotService hubSpotClient;
    private final Five9Service five9Client;
    private final EncryptionUtil encryptionUtil;
    

    public ConnectionService(ConnectionRepository connectionRepository, UserRepository userRepository, HubSpotService hubSpotClient, Five9Service five9Client, EncryptionUtil encryptionUtil) {
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
        this.hubSpotClient = hubSpotClient;
        this.five9Client = five9Client;
        this.encryptionUtil = encryptionUtil;
    }

    // Get all connections associated with a user's uuid
    public List<ConnectionEntity> getAllConnections(String uuidString) {
        logger.info("Getting all connnections for " + uuidString);
        try {
          UUID uuid = UUID.fromString(uuidString);
          List<ConnectionEntity> connections = connectionRepository.findAllConnectionsByUserUuid(uuid);
          return connections != null ? connections : Collections.emptyList();
        } catch (IllegalArgumentException e) {
           logger.warning("Failed to process uuid string. " + e);
           return Collections.emptyList();
        } catch (Exception e) {
           logger.warning("Failed to get connections. " + e);
           return Collections.emptyList();
        }
    }

    public ConnectionEntity getConnectionById(Integer id) {
        logger.info("Getting connection with id: " + id);
        return connectionRepository.findById(id).orElse(null);
    }

    public List<ConnectionEntity> getAllHubSpotConnections(String uuidString) {
        logger.info("Getting all HubSpot connections for " + uuidString);
        try {
          UUID uuid = UUID.fromString(uuidString);
          List<ConnectionEntity> connections = connectionRepository.findAllConnectionsByUserUuidAndConnectionType(uuid, "hubspot");
          return connections != null ? connections : Collections.emptyList();
        } catch (IllegalArgumentException e) {
          logger.warning("Failed to process uuid string. " + e);
          return Collections.emptyList();
        } catch (Exception e) {
          logger.warning("Failed to get HubSpot connections. " + e);
          return Collections.emptyList();
        }
    }

    public List<ConnectionEntity> getAllFive9Connections(String uuidString) {
        logger.info("Getting all Five9 connections for " + uuidString);
        try {
          UUID uuid = UUID.fromString(uuidString);
          List<ConnectionEntity> connections = connectionRepository.findAllConnectionsByUserUuidAndConnectionType(uuid, "five9");
          return connections != null ? connections : Collections.emptyList();
        } catch (IllegalArgumentException e) {
          logger.warning("Failed to process uuid string. " + e);
          return Collections.emptyList();
        } catch (Exception e) {
          logger.warning("Failed to get Five9 connections. " + e);
          return Collections.emptyList();
        }
    }

    public int createConnection(ConnectionDTO connectionDTO) {
       if (connectionDTO == null) {
           throw new IllegalArgumentException("Connection data is required");
       }
       
       String name = connectionDTO.getName();
       logger.info("Creating connection for user: " + connectionDTO.getUserUuid());
       logger.info("Connection name: " + name);
       logger.info("Connection description: " + connectionDTO.getDescription());
       logger.info("HubSpot Client ID: " + connectionDTO.getHubspotAccessToken());
       logger.info("Five9 Username: " + connectionDTO.getFive9Username());
       
       if (name == null || name.trim().isEmpty()) {
           throw new IllegalArgumentException("Connection name is required");
       }
       
       // Check if this is a HubSpot connection and test the connection first
       String hubspotAccessToken = connectionDTO.getHubspotAccessToken();
       if (!isNullOrEmpty(hubspotAccessToken)) {
           logger.info("Testing HubSpot connection before saving...");
           Boolean isValid = hubSpotClient.testHubSpotConnection(hubspotAccessToken).block();
           if (isValid == null || !isValid) {
               logger.warning("HubSpot connection test failed. Connection not saved.");
               throw new IllegalArgumentException("Invalid HubSpot access token. Please verify your credentials.");
           }
           logger.info("HubSpot connection test successful.");
       }
       
       // Check if this is a Five9 connection and test the connection first
       String five9Username = connectionDTO.getFive9Username();
       String five9Password = connectionDTO.getFive9Password();
       if (!isNullOrEmpty(five9Username) && !isNullOrEmpty(five9Password)) {
           logger.info("Testing Five9 connection before saving...");
           String credentials = five9Username + ":" + five9Password;
           String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());
           Boolean isValid = five9Client.testFive9Connection(base64Credentials).block();
           if (isValid == null || !isValid) {
               logger.warning("Five9 connection test failed. Connection not saved.");
               throw new IllegalArgumentException("Invalid Five9 credentials. Please verify your username and password.");
           }
           logger.info("Five9 connection test successful.");
       }
       
       ConnectionEntity connection = new ConnectionEntity();
       connection.setName(name);
       connection.setDescription(connectionDTO.getDescription() != null ? connectionDTO.getDescription() : "");
       
       // Set type based on credentials provided, or use DTO type if specified
       if (connectionDTO.getType() != null && !connectionDTO.getType().trim().isEmpty()) {
           connection.setType(connectionDTO.getType().toLowerCase());
       } else if (!isNullOrEmpty(hubspotAccessToken)) {
           connection.setType("hubspot");
       } else if (!isNullOrEmpty(five9Username)) {
           connection.setType("five9");
       }
       
       // Convert empty strings to null to avoid UNIQUE constraint violations
       // Encrypt sensitive credentials before saving to database
       connection.setFive9Username(isNullOrEmpty(connectionDTO.getFive9Username()) ? null : encryptionUtil.encrypt(connectionDTO.getFive9Username()));
       connection.setFive9Password(isNullOrEmpty(connectionDTO.getFive9Password()) ? null : encryptionUtil.encrypt(connectionDTO.getFive9Password()));
       connection.setHubspotAccessToken(isNullOrEmpty(hubspotAccessToken) ? null : encryptionUtil.encrypt(hubspotAccessToken));
       connection.setStatus("CONNECTED");
       
       UUID userUuid = UUID.fromString(connectionDTO.getUserUuid());
       UserEntity user = userRepository.findByUUID(userUuid)
                                       .orElseThrow(() -> new RuntimeException("User not found"));
       connection.setUser(user);
       
       // Generate UUID if not already set
       if (connection.getUuid() == null) {
           connection.setUuid(UUID.randomUUID());
       }
       return connectionRepository.save(connection);
    }

    /**
     * Delete a connection by ID
     */
    public int deleteConnection(Integer id) {
        logger.info("Deleting connection with id: " + id);
        return connectionRepository.deleteById(id);
    }

    /**
     * Get all HubSpot lists for a connection
     */
    public List<HubSpotListDTO> getAllHubspotLists(Integer connectionId) {
        logger.info("Getting all HubSpot lists for connection id: " + connectionId);
        return hubSpotClient.getAllHubspotLists(connectionId).block();
    }

    /**
     * Get all Five9 dialing lists for a connection
     */
    public List<Five9DialingListDTO> getAllFive9DialingLists(Integer connectionId) {
        logger.info("Getting all Five9 dialing lists for connection id: " + connectionId);
        HashMap<String, String> dialingListsMap = five9Client.getDialingLists(connectionId).block();
        
        if (dialingListsMap == null || dialingListsMap.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Five9DialingListDTO> dialingLists = new ArrayList<>();
        for (var entry : dialingListsMap.entrySet()) {
            dialingLists.add(new Five9DialingListDTO(entry.getKey(), entry.getValue(), null));
        }
        return dialingLists;
    }

    public Mono<List<String>> getFive9ContactFields(Integer connectionId) {
        logger.info("Getting all Five9 Contact fields.");
        return five9Client.getContactFields(connectionId);
    }

    public Mono<List<String>> getHubSpotProperties(Integer connectionId) {
        logger.info("Getting all HubSpot properties for connection id: " + connectionId);
        return hubSpotClient.getAllProperties(connectionId);
    }

    /**
     * Helper method to check if a string is null or empty
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
