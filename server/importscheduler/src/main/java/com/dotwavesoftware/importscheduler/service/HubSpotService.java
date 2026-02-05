package com.dotwavesoftware.importscheduler.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.dotwavesoftware.importscheduler.dto.HubSpotListDTO;
import com.dotwavesoftware.importscheduler.entity.ConnectionEntity;
import com.dotwavesoftware.importscheduler.entity.ConnectionImportMappingEntity;
import com.dotwavesoftware.importscheduler.repository.ConnectionRepository;
import com.dotwavesoftware.importscheduler.repository.ConnectionImportMappingRepository;
import com.dotwavesoftware.importscheduler.util.ConversionUtil;
import com.dotwavesoftware.importscheduler.util.EncryptionUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class HubSpotService {
    
    private final WebClient webClient;
    private ConnectionRepository connectionRepository;
    private ConnectionImportMappingRepository connectionImportMappingRepository;
    private final EncryptionUtil encryptionUtil;
    private static final Logger logger = Logger.getLogger(HubSpotService.class.getName());
    
    public HubSpotService(@Value("${api.hubspot.baseurl}") String baseUrl, 
                          ConnectionRepository connectionRepository,
                          ConnectionImportMappingRepository connectionImportMappingRepository,
                          EncryptionUtil encryptionUtil) 
    {
        this.connectionRepository = connectionRepository;
        this.connectionImportMappingRepository = connectionImportMappingRepository;
        this.encryptionUtil = encryptionUtil;
        
        // Increase buffer size to 2MB to handle large API responses
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
        
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .build();
    }


    public Mono<Boolean> testHubSpotConnection(String accessToken) {
        logger.info("Validating HubSpot access token.");
        
        // Validate that access token is provided
        if (accessToken == null || accessToken.trim().isEmpty()) {
            logger.warning("Access token is missing or empty.");
            return Mono.just(false);
        }
        
        logger.info("Testing access token against HubSpot API...");
        return webClient.get()
                .uri("/crm/v3/objects/contacts?limit=1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchangeToMono(response -> {
                    int status = response.statusCode().value();
                    if (status == 200) {
                        logger.info("HubSpot connection validated successfully.");
                        return Mono.just(true);
                    } else if (status == 401) {
                        logger.warning("HubSpot authentication failed - invalid access token.");
                        return Mono.just(false);
                    } else {
                        // Throw other unexpected status codes as 500 or log them as needed
                        logger.warning("Unexpected failure when authenticating access token. Status: " + status);
                        return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Unexpected response from HubSpot: " + status));
                    }
                });
    }

public Mono<List<JsonNode>> getAllContacts(int id) {
    return getAllContactsFlux(id)
            .flatMapIterable(response -> response.get("results")) // extract contacts from each page
            .collectList(); // gather into a single List
}

public Flux<JsonNode> getAllContactsFlux(int id) {
    logger.info("Getting contacts from HubSpot.");
    Optional<ConnectionEntity> connection = connectionRepository.findById(id);
    String token = getDecryptedAccessToken(connection.get());
    logger.info("Getting access token from connection " + id);
    // Start with an initial empty request (no "after")
    return fetchContactsPage(token, null)
            .expand(response -> {
                JsonNode paging = response.get("paging");
                if (paging != null && paging.get("next") != null) {
                    String nextAfter = paging.get("next").get("after").asText();
                    return fetchContactsPage(token, nextAfter);
                } else {
                    return Mono.empty(); // no more pages
                }
            });
}

    public Mono<JsonNode> fetchContactsPage(String token, String after) {
        String uri = "/crm/v3/objects/contacts?limit=100";
        if (after != null) {
            uri += "&after=" + after;
        }
        return webClient.get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(JsonNode.class);
    }

    public Mono<List<String>> getAllProperties(int id) {
        // Get Hubspot connection object 
        logger.info("Getting all Hubspot Properties");
        Optional<ConnectionEntity> connection = connectionRepository.findById(id);
        String accessToken = getDecryptedAccessToken(connection.get());
        return webClient.get()
                    .uri("/crm/v3/properties/contacts")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(response -> {
                        List<String> propertyNames = new ArrayList<>();
                        JsonNode results = response.path("results");
                        if (results.isArray()) {
                            for (JsonNode property : results) {
                                String name = property.path("name").asText();
                                if (!name.isEmpty()) {
                                    propertyNames.add(name);
                                }
                            }
                        }
                        logger.info("Retrieved " + propertyNames.size() + " properties from HubSpot");
                        return propertyNames;
                    });
    }

 
    public Mono<List<HubSpotListDTO>> getAllHubspotLists(int id) {
        Map<String, Object> requestBody = Map.of(
                "processingTypes", new String[] { "DYNAMIC", "MANUAL", "SNAPSHOT" }
        );
        Optional<ConnectionEntity> connection = connectionRepository.findById(id);
        logger.info("Getting access token from connection " + id);
        logger.info("Attempting to retrieve lists from HubSpot.");
        String accessToken = getDecryptedAccessToken(connection.get());
        return webClient.post()
                .uri("/crm/v3/lists/search")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(json -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(json);
    
                        // Make sure you're using the correct root key: "results" (most common for HubSpot)
                        JsonNode results = root.path("lists");
    
                        if (!results.isArray()) {
                            return Mono.error(new RuntimeException("Expected an array under 'results'"));
                        }
                        
                        List<HubSpotListDTO> lists = new ArrayList<>();
                        for (JsonNode node : results) {
                            HubSpotListDTO list = new HubSpotListDTO();
                            list.setListId(node.path("listId").asText()); // or "listId" if that's correct
                            list.setProcessingType(node.path("processingType").asText());
                            list.setName(node.path("name").asText());
                            list.setListSize(node.path("additionalProperties").path("hs_list_size").asText());
                            list.setLastUpdated(node.path("updatedAt").asText());
                            
                            // Parse to Instant
                            Instant instant = Instant.parse(list.getLastUpdated());
                            // Convert to local ZonedDateTime (e.g., system default zone)
                            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
                            // Define desired format
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mm a");
                            // Format the date
                            list.setLastUpdated(zdt.format(formatter));
                            list.setObjectTypeId(node.path("objectTypeId").asText());
                            if (list.getObjectTypeId().equals("0-1")) {
                                list.setObjectTypeId("CONTACT");
                            } else if (list.getObjectTypeId().equals("0-2")) {
                                list.setObjectTypeId("COMPANY");
                            } else if (list.getObjectTypeId().equals("0-3")) {
                                list.setObjectTypeId("DEAL");
                            } else if (list.getObjectTypeId().equals("0-5")) {
                                list.setObjectTypeId("TICKET");
                            } 
                            lists.add(list);
                        }
                        
                        return Mono.just(lists);
    
                    } catch (Exception e) {
                        logger.warning("Failed to retrieve lists from HubSpot");
                        return Mono.error(new RuntimeException("Failed to parse nested JSON", e));
                    }
                });
    }

    

    
    
    /** STEP 1
     * Get all memberships (contact IDs) for a specific HubSpot list with pagination support.
     * @param connectionId The connection ID to get the access token from
     * @param listId The HubSpot list ID to get memberships for
     * @return A Mono containing a list of all membership record IDs
     */
    public Mono<List<String>> getListMemberships(int connectionId, String listId) {
        return getListMembershipsFlux(connectionId, listId)
                .flatMapIterable(response -> {
                    List<String> recordIds = new ArrayList<>();
                    JsonNode results = response.get("results");
                    if (results != null && results.isArray()) {
                        for (JsonNode node : results) {
                            String recordId = node.get("recordId").asText();
                            if (recordId != null && !recordId.isEmpty()) {
                                recordIds.add(recordId);
                            }
                        }
                    }
                    return recordIds;
                })
                .collectList();
    }

    /**
     * Fetch all pages of list memberships using reactive streams.
     */
    public Flux<JsonNode> getListMembershipsFlux(int connectionId, String listId) {
        logger.info("Getting memberships for HubSpot list: " + listId);
        Optional<ConnectionEntity> connection = connectionRepository.findById(connectionId);
        if (connection.isEmpty()) {
            return Flux.error(new RuntimeException("Connection not found: " + connectionId));
        }
        String token = getDecryptedAccessToken(connection.get());
        
        return fetchListMembershipsPage(token, listId, null)
                .expand(response -> {
                    JsonNode paging = response.get("paging");
                    if (paging != null && paging.get("next") != null) {
                        String nextAfter = paging.get("next").get("after").asText();
                        return fetchListMembershipsPage(token, listId, nextAfter);
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Fetch a single page of list memberships.
     */
    public Mono<JsonNode> fetchListMembershipsPage(String token, String listId, String after) {
        String uri = "/crm/v3/lists/" + listId + "/memberships?limit=100";
        if (after != null) {
            uri += "&after=" + after;
        }
        logger.info("Fetching list memberships page: " + uri);
        return webClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    /** STEP 2
     * Batch read contacts from HubSpot using the mapped properties.
     * Retrieves sending_connection_field_name values from connection_import_mappings table
     * and fetches contact data for all members of the specified list.
     * HubSpot batch read API has a limit of 100 records per request, so this batches requests.
     * 
     * @param connectionId The HubSpot connection ID (used to get access token and mappings)
     * @param listId The HubSpot list ID to get memberships from
     * @param importId The import ID to get field mappings from
     * @return A Mono containing a list of all contact data from all batches
     */
    public Mono<List<JsonNode>> batchReadContacts(int connectionId, String listId, int importId) {
        logger.info("Starting batch read for connectionId: " + connectionId + ", listId: " + listId + ", importId: " + importId);
        
        // Get connection for access token
        Optional<ConnectionEntity> connection = connectionRepository.findById(connectionId);
        if (connection.isEmpty()) {
            return Mono.error(new RuntimeException("Connection not found: " + connectionId));
        }
        String token = getDecryptedAccessToken(connection.get());
        
        // Get sending_connection_field_name values from mappings
        List<ConnectionImportMappingEntity> mappings = connectionImportMappingRepository.findByImportId(importId);
        List<String> properties = new ArrayList<>();
        for (ConnectionImportMappingEntity mapping : mappings) {
            String fieldName = mapping.getId().getSendingConnectionFieldName();
            if (fieldName != null && !fieldName.isEmpty() && !properties.contains(fieldName)) {
                properties.add(fieldName);
            }
        }
        logger.info("Properties to fetch: " + properties);
        
        if (properties.isEmpty()) {
            return Mono.error(new RuntimeException("No field mappings found for importId: " + importId));
        }
        
        // Get list memberships and then batch read
        return getListMemberships(connectionId, listId)
                .flatMap(memberIds -> {
                    if (memberIds.isEmpty()) {
                        return Mono.error(new RuntimeException("No members found in list: " + listId));
                    }
                    logger.info("Found " + memberIds.size() + " members in list. Performing batch read in chunks of 100...");
                    
                    // Split into batches of 100 (HubSpot limit)
                    List<List<String>> batches = new ArrayList<>();
                    for (int i = 0; i < memberIds.size(); i += 100) {
                        batches.add(memberIds.subList(i, Math.min(i + 100, memberIds.size())));
                    }
                    logger.info("Split into " + batches.size() + " batches");
                    
                    // Process each batch and collect results - extract only the mapped properties
                    final List<String> mappedProperties = properties; // capture for lambda
                    ObjectMapper mapper = new ObjectMapper();
                    
                    return Flux.fromIterable(batches)
                            .concatMap(batch -> fetchBatchContacts(token, mappedProperties, batch))
                            .flatMapIterable(response -> {
                                List<JsonNode> results = new ArrayList<>();
                                JsonNode resultsNode = response.get("results");
                                if (resultsNode != null && resultsNode.isArray()) {
                                    for (JsonNode node : resultsNode) {
                                        JsonNode propertiesNode = node.get("properties");
                                        if (propertiesNode != null) {
                                            // Filter to only include the properties from database mappings
                                            var filteredNode = mapper.createObjectNode();
                                            for (String prop : mappedProperties) {
                                                JsonNode value = propertiesNode.get(prop);
                                                if (value != null) {
                                                    // Format phone number properties to 10-digit format
                                                    if (isPhoneProperty(prop) && value.isTextual()) {
                                                        String formattedPhone = ConversionUtil.formatPhoneNumber(value.asText());
                                                        if (formattedPhone != null) {
                                                            filteredNode.set(prop, new TextNode(formattedPhone));
                                                        } else {
                                                            filteredNode.set(prop, value); // Keep original if can't format
                                                        }
                                                    } else {
                                                        filteredNode.set(prop, value);
                                                    }
                                                }
                                            }
                                            results.add(filteredNode);
                                        }
                                    }
                                }
                                return results;
                            })
                            .collectList()
                            .doOnSuccess(results -> logger.info("Batch read completed. Total contacts: " + results.size()));
                });
    }

    /**
     * Fetch a single batch of contacts (max 100).
     */
    private Mono<JsonNode> fetchBatchContacts(String token, List<String> properties, List<String> memberIds) {
        // Build inputs array: [{"id": "123"}, {"id": "456"}, ...]
        List<Map<String, String>> inputs = new ArrayList<>();
        for (String memberId : memberIds) {
            inputs.add(Map.of("id", memberId));
        }
        
        // Build request body
        Map<String, Object> requestBody = Map.of(
                "properties", properties,
                "inputs", inputs
        );
        
        logger.info("Fetching batch of " + memberIds.size() + " contacts...");
        return webClient.post()
                .uri("/crm/v3/objects/contacts/batch/read")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnError(error -> logger.warning("Batch fetch failed: " + error.getMessage()));
    }

    /**
     * Check if a property name represents a phone number field.
     * Matches common phone property names like: phone, mobilephone, phone_number, etc.
     */
    private boolean isPhoneProperty(String propertyName) {
        if (propertyName == null) return false;
        String lower = propertyName.toLowerCase();
        return lower.contains("phone") || 
               lower.contains("mobile") || 
               lower.contains("fax") ||
               lower.equals("number1") ||
               lower.equals("number2") ||
               lower.equals("number3");
    }

    /**
     * Get the decrypted HubSpot access token from a connection.
     * @param connection The connection entity
     * @return Decrypted access token
     */
    private String getDecryptedAccessToken(ConnectionEntity connection) {
        String encryptedToken = connection.getHubspotAccessToken();
        if (encryptedToken == null || encryptedToken.isEmpty()) {
            return null;
        }
        return encryptionUtil.decrypt(encryptedToken);
    }
}