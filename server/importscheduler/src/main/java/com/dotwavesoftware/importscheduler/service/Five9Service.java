package com.dotwavesoftware.importscheduler.service;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.dotwavesoftware.importscheduler.dto.ContactFieldsResponseDTO;
import com.dotwavesoftware.importscheduler.entity.ConnectionEntity;
import com.dotwavesoftware.importscheduler.entity.ConnectionImportMappingEntity;
import com.dotwavesoftware.importscheduler.repository.ConnectionRepository;
import com.dotwavesoftware.importscheduler.repository.ConnectionImportMappingRepository;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Five9Service {
    private final WebClient webClient;
    private static final Logger logger = Logger.getLogger(Five9Service.class.getName());
    private ConnectionRepository connectionRepository;
    private ConnectionImportMappingRepository connectionImportMappingRepository;
    private HubSpotService hubSpotService;
    private ImportProgressService importProgressService;
    private com.dotwavesoftware.importscheduler.repository.ImportRepository importRepository;
    
    // Track failed records per import (importId -> list of failed contacts)
    private final Map<Integer, List<JsonNode>> failedRecordsMap = new ConcurrentHashMap<>();
    
    public Five9Service(@Value("${api.five9.baseurl}") String baseUrl, 
                        ConnectionRepository connectionRepository, 
                        ConnectionImportMappingRepository connectionImportMappingRepository,
                        HubSpotService hubSpotService,
                        ImportProgressService importProgressService,
                        com.dotwavesoftware.importscheduler.repository.ImportRepository importRepository) 
    {
        this.connectionRepository = connectionRepository;
        this.connectionImportMappingRepository = connectionImportMappingRepository;
        this.hubSpotService = hubSpotService;
        this.importProgressService = importProgressService;
        this.importRepository = importRepository;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<Boolean> testFive9Connection(String base64Credentials) {
        String soapRequest = """
<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/"
xmlns:ser="http://service.admin.ws.five9.com/">
    <env:Header/>
    <env:Body>
        <ser:addRecordToList>
            <listName>JASON_TEST_LIST</listName>
            <listUpdateSettings>
                <fieldsMapping>
                    <columnNumber>1</columnNumber>
                    <fieldName>number1</fieldName>
                    <key>true</key>
                </fieldsMapping>
                <fieldsMapping>
                    <columnNumber>2</columnNumber>
                    <fieldName>first_name</fieldName>
                    <key>false</key>
                </fieldsMapping>
                <fieldsMapping>
                    <columnNumber>3</columnNumber>
                    <fieldName>last_name</fieldName>
                    <key>false</key>
                </fieldsMapping>
                <separator>,</separator>
                <skipHeaderLine>false</skipHeaderLine>
                <callNowMode>ANY</callNowMode>
                <cleanListBeforeUpdate>false</cleanListBeforeUpdate>
                <crmAddMode>ADD_NEW</crmAddMode>
                <crmUpdateMode>UPDATE_FIRST</crmUpdateMode>
                <listAddMode>ADD_FIRST</listAddMode>
            </listUpdateSettings>
            <record>
                <fields></fields>
                <fields></fields>
                <fields></fields>
            </record>
        </ser:addRecordToList>
    </env:Body>
</env:Envelope>
""";
        // Removes extra quotes
        String credentials = base64Credentials.replaceAll("^\"|\"$", "");
        logger.info("Testing Five9 Connection.");
        return webClient.post()
                .uri("/wsadmin/AdminWebService")
                .header(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .bodyValue(soapRequest)
                .exchangeToMono(response -> {
                    int status = response.statusCode().value();
                    if (status == 200) {
                        return Mono.just(true);
                    } else if (status == 401) {
                        return Mono.just(false);
                    } else {
                        // Throw other unexpected status codes as 500 or log them as needed
                        logger.warning("Unexpected response from Five9 API.");
                        return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Unexpected response from Five9: " + status));
                    }
                });
    }

        public Mono<HashMap<String, String>> getDialingLists(int five9ConnectionId) {
        String soapRequest = """
            <env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/"
            xmlns:ser="http://service.admin.ws.five9.com/">
                <env:Header/>
                <env:Body>
                    <ser:getListsInfo>
                     
                    </ser:getListsInfo>
                </env:Body>
            </env:Envelope> 
            """;
        Optional<ConnectionEntity> connection = connectionRepository.findById(five9ConnectionId);
        String username = connection.get().getFive9Username();
        String password = connection.get().getFive9Password();
        String credentials = username +":"+ password;
        String base64credentials = Base64.getEncoder().encodeToString(credentials.getBytes());
       // logger.warning(base64credentials);
       // String credentials = base64Credentials.replaceAll("^\"|\"$", "");
        logger.info("Getting Dialing lists from Five9 API.");
        return webClient.post()
            .uri("/wsadmin/AdminWebService")
            .header(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8")
            .header(HttpHeaders.AUTHORIZATION, "Basic " + base64credentials)
            .bodyValue(soapRequest)
            .retrieve()
            .bodyToMono(String.class)
            .flatMap(responseXml -> {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new InputSource(new StringReader(responseXml)));
    
                    // Create a HashMap to store name -> size
                    HashMap<String, String> dialingList = new HashMap<>();
    
                    NodeList listNames = doc.getElementsByTagName("name");
                    NodeList listSizes = doc.getElementsByTagName("size");
    
                    for (int i = 0; i < listNames.getLength(); i++) {
                        String listName = listNames.item(i).getTextContent();
                        String size = (i < listSizes.getLength()) ? listSizes.item(i).getTextContent() : "";
                        dialingList.put(listName, size);
                    }
                    
                    return Mono.just(dialingList);
    
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warning("Failed to parse SOAP response");
                    return Mono.error(new RuntimeException("Failed to parse SOAP response", e));
                }
            });
    }

    
    public Mono<List<String>> getContactFields(int five9ConnectionId) {
        String soapRequest = """
            <env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/"
            xmlns:ser="http://service.admin.ws.five9.com/">
                <env:Header/>
                <env:Body>
                    <ser:getContactFields>
                
                    </ser:getContactFields>
                </env:Body>
            </env:Envelope>
            """;
        // Get Connection credentials
        Optional<ConnectionEntity> connection = connectionRepository.findById(five9ConnectionId);
        String username = connection.get().getFive9Username();
        String password = connection.get().getFive9Password();
        String credentials = username +":"+ password;

        // Convert Connection credentials to Base64
        String base64credentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        // Get Five9 Contact Fields from Five9 Web Services API
        return webClient.post()
            .uri("/wsadmin/AdminWebService")
            .header(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8")
            .header(HttpHeaders.AUTHORIZATION, "Basic " + base64credentials)
            .bodyValue(soapRequest)
            .retrieve()
            .bodyToMono(String.class)
            .map(xml -> {
            // parse XML manually, e.g., with Jsoup, DOM, or XPath
            List<String> list = new ArrayList<>();
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

                NodeList nodes = doc.getElementsByTagName("name"); 
                for (int i = 0; i < nodes.getLength(); i++) {
                    list.add(nodes.item(i).getTextContent());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        });
    }

    // TODO: This method is a work in progress - needs to build dynamic SOAP request from contacts

    public Mono<Boolean> importContactsToDialingList(int five9ConnectionId, int hubspotConnectionId, String hubspotListId, int importId) {
        // Update status to ACTIVE now that the import is actually starting
        importRepository.updateStatus(importId, "STARTING");
        importProgressService.sendStatus(importId, "STARTING", "Import is running");
        logger.info("Import " + importId + " status set to STARTING");
        
        Optional<ConnectionEntity> connection = connectionRepository.findById(five9ConnectionId);
        if (connection.isEmpty()) {
            return Mono.error(new RuntimeException("Five9 connection not found: " + five9ConnectionId));
        }
        String username = connection.get().getFive9Username();
        String password = connection.get().getFive9Password();
        String credentials = username + ":" + password;
        String base64credentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        // Get all mappings from connection_import_mapping table by import_id
        List<ConnectionImportMappingEntity> mappings = connectionImportMappingRepository.findByImportId(importId);
        
        // Build dynamic fieldsMapping XML from database mappings (same for all batches)
        StringBuilder fieldsMappingXml = new StringBuilder();
        int columnNumber = 1;
        for (ConnectionImportMappingEntity mapping : mappings) {
            String fieldName = mapping.getId().getReceivingConnectionFieldName();
            Boolean isKey = mapping.getFive9Key() != null && mapping.getFive9Key();
            fieldsMappingXml.append(String.format("""
                <fieldsMapping>
                    <columnNumber>%d</columnNumber>
                    <fieldName>%s</fieldName>
                    <key>%s</key>
                </fieldsMapping>
""", columnNumber, fieldName, isKey));
            columnNumber++;
        }
        final String fieldsMappingXmlStr = fieldsMappingXml.toString();
        
        // Get contacts from HubSpot, then send to Five9 in batches of 100
        return hubSpotService.batchReadContacts(hubspotConnectionId, hubspotListId, importId)
            .flatMap(contacts -> {
                logger.info("Received " + contacts.size() + " contacts from HubSpot for Five9 import");
                
                if (contacts.isEmpty()) {
                    logger.warning("No contacts to import");
                    importProgressService.sendCompletion(importId, false, 0, 0, 0);
                    return Mono.just(false);
                }
                
                // Send started notification via WebSocket
                importProgressService.sendStarted(importId, contacts.size());
                
                // Initialize failed records list for this import
                failedRecordsMap.put(importId, new ArrayList<>());
                
                // Split contacts into batches of 100 (Five9 limit)
                List<List<JsonNode>> batches = new ArrayList<>();
                for (int i = 0; i < contacts.size(); i += 100) {
                    batches.add(contacts.subList(i, Math.min(i + 100, contacts.size())));
                }
                logger.info("Split into " + batches.size() + " batches of max 100 contacts each");
                
                // Track total records added
                final int[] totalRecordsAdded = {0};
                final int[] batchNumber = {0};
                final int totalContacts = contacts.size();
                
                // Process each batch sequentially and collect results
                return Flux.fromIterable(batches)
                    .concatMap(batch -> {
                        batchNumber[0]++;
                        return sendBatchToFive9(batch, mappings, fieldsMappingXmlStr, base64credentials, batchNumber[0], batches.size())
                            .doOnNext(success -> {
                                if (success) {
                                    totalRecordsAdded[0] += batch.size();
                                    logger.info("Batch " + batchNumber[0] + " sent. Batches processed: " + batchNumber[0] + "/" + batches.size());
                                } else {
                                    // Track failed records in memory
                                    failedRecordsMap.get(importId).addAll(batch);
                                    logger.warning("Batch " + batchNumber[0] + " failed. Total failed records: " + failedRecordsMap.get(importId).size());
                                }
                                
                                // Send progress update via WebSocket (batch progress, not list size)
                                importProgressService.sendProgress(
                                    importId, 
                                    "ACTIVE", 
                                    batchNumber[0] * 100 / batches.size(), // percentage
                                    100,
                                    "Processing batch " + (batchNumber[0] * 100 / batches.size()) + "%"
                                );
                                logger.info("Progress: " + (batchNumber[0] * 100 / batches.size()) + "%");
                            });
                    })
                    .all(success -> success) // Returns true only if all batches succeed
                    .delayElement(java.time.Duration.ofSeconds(10)) // Wait for Five9 to process records
                    .flatMap(allSuccess -> {
                        // Fetch the actual dialing list size from Five9 API
                        return getDialingLists(five9ConnectionId)
                            .map(dialingListsMap -> {
                                // Log all keys in the map for debugging
                                logger.info("Five9 dialing lists returned: " + dialingListsMap.keySet());
                                
                                // Get size of the target dialing list (JASON_TEST_LIST)
                                String listSizeStr = dialingListsMap.get("JASON_TEST_LIST");
                                int listSize = 0;
                                if (listSizeStr != null && !listSizeStr.isEmpty()) {
                                    try {
                                        listSize = Integer.parseInt(listSizeStr);
                                    } catch (NumberFormatException e) {
                                        logger.warning("Failed to parse list size: " + listSizeStr);
                                    }
                                }
                                
                                logger.info("Five9 dialing list 'JASON_TEST_LIST' size: " + listSize);
                                
                                List<JsonNode> failedRecords = failedRecordsMap.get(importId);
                                int failedCount = failedRecords != null ? failedRecords.size() : 0;
                                
                                // Calculate final progress percentage (100% since all batches processed)
                                int progressPercent = 100;
                                
                                if (allSuccess) {
                                    logger.info("=== IMPORT COMPLETE: " + listSize + " records in Five9 dialing list ===");
                                    importProgressService.sendCompletion(importId, true, listSize, totalContacts, progressPercent);
                                } else {
                                    logger.warning("Import completed with failures. List size: " + listSize + ", Failed: " + failedCount + "/" + totalContacts);
                                    importProgressService.sendCompletion(importId, false, listSize, totalContacts, progressPercent);
                                }
                                
                                return allSuccess;
                            })
                            .onErrorResume(e -> {
                                logger.warning("Failed to fetch dialing list size: " + e.getMessage());
                                // Fallback to tracked count if API call fails
                                importProgressService.sendCompletion(importId, allSuccess, totalRecordsAdded[0], totalContacts, 100);
                                return Mono.just(allSuccess);
                            });
                    });
            });
    }

    /**
     * Send a batch of contacts (max 100) to Five9.
     */
    private Mono<Boolean> sendBatchToFive9(List<JsonNode> contacts, 
                                            List<ConnectionImportMappingEntity> mappings,
                                            String fieldsMappingXml,
                                            String base64credentials,
                                            int batchNumber,
                                            int totalBatches) {
        logger.info("Sending batch " + batchNumber + "/" + totalBatches + " (" + contacts.size() + " contacts) to Five9...");
        
        // Build dynamic importData from contacts
        StringBuilder importDataXml = new StringBuilder();
        for (JsonNode contact : contacts) {
            importDataXml.append("                <values>\n");
            for (ConnectionImportMappingEntity mapping : mappings) {
                String hubspotProperty = mapping.getId().getSendingConnectionFieldName();
                JsonNode valueNode = contact.get(hubspotProperty);
                String value = (valueNode != null && !valueNode.isNull()) ? valueNode.asText() : "";
                // Escape XML special characters
                value = value.replace("&", "&amp;")
                             .replace("<", "&lt;")
                             .replace(">", "&gt;")
                             .replace("\"", "&quot;")
                             .replace("'", "&apos;");
                importDataXml.append(String.format("                    <item>%s</item>\n", value));
            }
            importDataXml.append("                </values>\n");
        }
        
        String soapRequest = String.format("""
<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/"
xmlns:ser="http://service.admin.ws.five9.com/">
    <env:Header/>
    <env:Body>
        <ser:addToList>
            <listName>JASON_TEST_LIST</listName>
            <listUpdateSettings>
%s                <callTimeColumnNumber>1</callTimeColumnNumber>
                <crmAddMode>ADD_NEW</crmAddMode>
                <callNowMode>NONE</callNowMode>
                <crmUpdateMode>UPDATE_FIRST</crmUpdateMode>
                <listAddMode>ADD_IF_SOLE_CRM_MATCH</listAddMode>
                <reportEmail>djason77@gmail.com</reportEmail>
            </listUpdateSettings>
            <importData>
%s            </importData>
        </ser:addToList>
    </env:Body>
</env:Envelope>
""", fieldsMappingXml, importDataXml.toString());
        
        // Log the first batch's SOAP request for debugging (truncated)
        if (contacts.size() <= 5) {
            logger.info("SOAP Request:\n" + soapRequest);
        } else {
            logger.info("SOAP Request (first 2000 chars):\n" + soapRequest.substring(0, Math.min(2000, soapRequest.length())) + "...");
        }
        
        return webClient.post()
            .uri("/wsadmin/AdminWebService")
            .header(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8")
            .header(HttpHeaders.AUTHORIZATION, "Basic " + base64credentials)
            .bodyValue(soapRequest)
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(String.class);
                } else {
                    return response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            logger.warning("Five9 error response (" + response.statusCode() + "): " + errorBody);
                            return Mono.just(errorBody);
                        });
                }
            })
            .map(responseXml -> {
                logger.info("Five9 response: " + responseXml);
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new ByteArrayInputStream(responseXml.getBytes(StandardCharsets.UTF_8)));
                    NodeList nodes = doc.getElementsByTagName("return");
                    if (nodes.getLength() > 0) {
                        String identifier = nodes.item(0).getTextContent();
                        logger.info("Five9 batch accepted with identifier: " + identifier);
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    logger.warning("Error parsing Five9 response: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            })
            .onErrorResume(e -> {
                logger.warning("Error sending batch to Five9: " + e.getMessage());
                return Mono.just(false);
            });
    }

    /**
     * Get failed records for a specific import
     * @param importId The import ID
     * @return List of failed contact records, or empty list if none
     */
    public List<JsonNode> getFailedRecords(int importId) {
        return failedRecordsMap.getOrDefault(importId, new ArrayList<>());
    }

    /**
     * Get the count of failed records for a specific import
     * @param importId The import ID
     * @return Number of failed records
     */
    public int getFailedRecordsCount(int importId) {
        List<JsonNode> failed = failedRecordsMap.get(importId);
        return failed != null ? failed.size() : 0;
    }

    /**
     * Clear failed records for a specific import (call after retry or when no longer needed)
     * @param importId The import ID
     */
    public void clearFailedRecords(int importId) {
        failedRecordsMap.remove(importId);
        logger.info("Cleared failed records for import " + importId);
    }

    /**
     * Get all failed records across all imports
     * @return Map of importId to list of failed records
     */
    public Map<Integer, List<JsonNode>> getAllFailedRecords() {
        return new ConcurrentHashMap<>(failedRecordsMap);
    }
}
