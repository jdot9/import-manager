package com.dotwavesoftware.importscheduler.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotwavesoftware.importscheduler.service.HubSpotService;
import com.dotwavesoftware.importscheduler.service.Five9Service;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = Logger.getLogger(TestController.class.getName());
    private final HubSpotService hubSpotService;
    private final Five9Service five9Service;

    public TestController(HubSpotService hubSpotService, Five9Service five9Service) {
        this.hubSpotService = hubSpotService;
        this.five9Service = five9Service;
    }

    /**
     * Test endpoint for getting HubSpot list memberships.
     * Example: GET /api/test/hubspot/list-memberships?connectionId=1&listId=10
     */
    @GetMapping("/hubspot/list-memberships")
    public Mono<ResponseEntity<List<String>>> getListMemberships(
            @RequestParam int connectionId,
            @RequestParam String listId) {
        logger.info("Testing getListMemberships - connectionId: " + connectionId + ", listId: " + listId);
        
        return hubSpotService.getListMemberships(connectionId, listId)
                .map(memberships -> {
                    logger.info("Retrieved " + memberships.size() + " memberships from list " + listId);
                    return ResponseEntity.ok(memberships);
                })
                .onErrorResume(e -> {
                    logger.warning("Error getting list memberships: " + e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Test endpoint for batch reading HubSpot contacts with mapped properties.
     * Example: POST /api/test/hubspot/batch-read?connectionId=1&listId=10&importId=1
     */
    @PostMapping("/hubspot/batch-read")
    public Mono<ResponseEntity<List<JsonNode>>> batchReadContacts(
            @RequestParam int connectionId,
            @RequestParam String listId,
            @RequestParam int importId) {
        logger.info("Testing batchReadContacts - connectionId: " + connectionId + ", listId: " + listId + ", importId: " + importId);
        
        return hubSpotService.batchReadContacts(connectionId, listId, importId)
                .map(contacts -> {
                    logger.info("Batch read completed successfully. Total contacts: " + contacts.size());
                    return ResponseEntity.ok(contacts);
                })
                .onErrorResume(e -> {
                    logger.warning("Error in batch read: " + e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Test endpoint for importing contacts from HubSpot to Five9 dialing list.
     * Example: POST /api/test/five9/import?five9ConnectionId=1&hubspotConnectionId=2&hubspotListId=10&importId=1
     */
    @PostMapping("/five9/import")
    public Mono<ResponseEntity<Boolean>> importContactsToDialingList(
            @RequestParam int five9ConnectionId,
            @RequestParam int hubspotConnectionId,
            @RequestParam String hubspotListId,
            @RequestParam int importId) {
        logger.info("Testing importContactsToDialingList - five9ConnectionId: " + five9ConnectionId + 
                    ", hubspotConnectionId: " + hubspotConnectionId + 
                    ", hubspotListId: " + hubspotListId + 
                    ", importId: " + importId);
        
        return five9Service.importContactsToDialingList(five9ConnectionId, hubspotConnectionId, hubspotListId, importId)
                .map(success -> {
                    if (success) {
                        logger.info("Import to Five9 completed successfully");
                        return ResponseEntity.ok(true);
                    } else {
                        logger.warning("Import to Five9 failed");
                        return ResponseEntity.ok(false);
                    }
                })
                .onErrorResume(e -> {
                    logger.warning("Error importing to Five9: " + e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}
