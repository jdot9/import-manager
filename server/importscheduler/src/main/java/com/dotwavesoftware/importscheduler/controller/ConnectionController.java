package com.dotwavesoftware.importscheduler.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotwavesoftware.importscheduler.dto.ConnectionDTO;
import com.dotwavesoftware.importscheduler.dto.ConnectionIdRequestDTO;
import com.dotwavesoftware.importscheduler.dto.Five9DialingListDTO;
import com.dotwavesoftware.importscheduler.dto.HubSpotListDTO;
import com.dotwavesoftware.importscheduler.dto.UserUuidRequestDTO;
import com.dotwavesoftware.importscheduler.entity.ConnectionEntity;
import com.dotwavesoftware.importscheduler.service.ConnectionService;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api")
public class ConnectionController {

    private static final Logger logger = Logger.getLogger(ConnectionController.class.getName());
    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }
    
    @GetMapping("/connections")
    public List<ConnectionEntity> getAllConnectionsForUser(@RequestParam String userId) {
        logger.info("Request for user connections received.");
        List<ConnectionEntity> connections = connectionService.getAllConnections(userId);
        if (connections.size() < 1) {
            logger.info("Response sent (0 connections found)");
        } else {
            logger.info("Response sent (" + connections.size() + " connections found.)");
        }
        return connections;
    }

    @PostMapping("/connections")
    public ResponseEntity<String> saveConnection(@RequestBody ConnectionDTO connection) {
        logger.info("Request to save connection received.");
        logger.info("Received connection data - name: " + connection.getName() + ", userUuid: " + connection.getUserUuid());
        try {
            int result = connectionService.createConnection(connection);
            if (result > 0) {
                logger.info("Connection saved. Response sent.");
                return ResponseEntity.ok().body("Connection saved.");
            } else {
                logger.warning("Connection failed to save. Response sent.");
                return ResponseEntity.status(500).body("Connection failed to save.");
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.warning("Error saving connection: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/connections/{id}")
    public ResponseEntity<String> deleteConnection(@PathVariable Integer id) {
        logger.info("Request to delete connection with id: " + id);
        try {
            int result = connectionService.deleteConnection(id);
            if (result > 0) {
                logger.info("Connection deleted successfully.");
                return ResponseEntity.ok().body("Connection deleted successfully.");
            } else {
                logger.warning("Connection not found or failed to delete.");
                return ResponseEntity.status(404).body("Connection not found.");
            }
        } catch (Exception e) {
            logger.warning("Error deleting connection: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/connections/hubspot")
    public List<ConnectionEntity> getHubSpotConnections(@RequestBody UserUuidRequestDTO request) {
        logger.info("Request to get HubSpot connections for user: " + request.getUserUuid());
        List<ConnectionEntity> connections = connectionService.getAllHubSpotConnections(request.getUserUuid());
        return connections;
    }

    @PostMapping("/connections/hubspot/lists")
    public List<HubSpotListDTO> getHubSpotLists(@RequestBody ConnectionIdRequestDTO request) {
        logger.info("Request to get HubSpot lists for connection id: " + request.getId());
        List<HubSpotListDTO> lists = connectionService.getAllHubspotLists(request.getId());
        return lists;
    }

    @PostMapping("/connections/five9")
    public List<ConnectionEntity> getFive9Connections(@RequestBody UserUuidRequestDTO request) {
        logger.info("Request to get Five9 connections for user: " + request.getUserUuid());
        List<ConnectionEntity> connections = connectionService.getAllFive9Connections(request.getUserUuid());
        return connections;
    }

    @PostMapping("/connections/five9/dialing-lists")
    public List<Five9DialingListDTO> getFive9DialingLists(@RequestBody ConnectionIdRequestDTO request) {
        logger.info("Request to get Five9 dialing lists for connection id: " + request.getId());
        List<Five9DialingListDTO> lists = connectionService.getAllFive9DialingLists(request.getId());
        return lists;
    }

    @GetMapping("/five9-contact-fields/{connectionId}")
    public Mono<List<String>> getFive9ContactFields(@PathVariable String connectionId) {
        logger.info("Testing endpoint for getting Five9 Contact fields");
        int id = Integer.parseInt(connectionId);
        return connectionService.getFive9ContactFields(id);
         
    }

    @GetMapping("/hubspot-properties/{connectionId}")
    public Mono<List<String>> getHubSpotProperties(@PathVariable String connectionId) {
        logger.info("Request to get HubSpot properties for connection id: " + connectionId);
        int id = Integer.parseInt(connectionId);
        return connectionService.getHubSpotProperties(id);
    }
}
