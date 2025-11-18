package org.opendevstack.apiservice.externalservice.uipath.service.impl;

import org.opendevstack.apiservice.externalservice.uipath.config.UiPathProperties;
import org.opendevstack.apiservice.externalservice.uipath.exception.UiPathException;
import org.opendevstack.apiservice.externalservice.uipath.model.QueueItemStatus;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathAuthRequest;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathAuthResponse;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathODataResponse;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItem;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemRequest;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemResult;
import org.opendevstack.apiservice.externalservice.uipath.service.UiPathOrchestratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of UiPathService for UIPath Orchestrator.
 * This service provides integration with UIPath Orchestrator for managing queue items
 * and checking robot execution status.
 */
@Service("uiPathOrchestratorService")
public class UiPathOrchestratorServiceImpl implements UiPathOrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(UiPathOrchestratorServiceImpl.class);

    private final RestTemplate restTemplate;
    private final UiPathProperties properties;

    public UiPathOrchestratorServiceImpl(
            @Qualifier("uiPathRestTemplate") RestTemplate restTemplate,
            @Qualifier("uiPathOrchestratorProperties") UiPathProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public String authenticate() throws UiPathException.AuthenticationException {
        logger.debug("Authenticating to UIPath Orchestrator at {}", properties.getHost());

        try {
            UiPathAuthRequest authRequest = new UiPathAuthRequest(
                    properties.getTenancyName(),
                    properties.getClientId(),
                    properties.getClientSecret()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<UiPathAuthRequest> request = new HttpEntity<>(authRequest, headers);

            ResponseEntity<UiPathAuthResponse> response = restTemplate.postForEntity(
                    properties.getLoginUrl(),
                    request,
                    UiPathAuthResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UiPathAuthResponse authResponse = response.getBody();

                if (authResponse.isSuccess() && StringUtils.hasText(authResponse.getToken())) {
                    logger.debug("Successfully authenticated to UIPath Orchestrator");
                    return authResponse.getToken();
                } else {
                    String errorMsg = authResponse.getError() != null ? authResponse.getError() : "Unknown authentication error";
                    throw new UiPathException.AuthenticationException("Authentication failed: " + errorMsg);
                }
            } else {
                throw new UiPathException.AuthenticationException(
                        "Unexpected response status: " + response.getStatusCode()
                );
            }

        } catch (RestClientException e) {
            logger.error("Failed to authenticate to UIPath Orchestrator: {}", e.getMessage(), e);
            throw new UiPathException.AuthenticationException("Authentication failed", e);
        }
    }

    @Override
    public UiPathQueueItem addQueueItem(UiPathQueueItemRequest request) 
            throws UiPathException.QueueItemCreationException {
        
        String reference = request.getItemData() != null ? request.getItemData().getReference() : "unknown";
        logger.info("Adding queue item with reference '{}'", reference);

        try {
            // Authenticate first
            String token = authenticate();

            // Create headers with authentication and organization unit
            HttpHeaders headers = createAuthHeaders(token);

            HttpEntity<UiPathQueueItemRequest> httpRequest = new HttpEntity<>(request, headers);

            ResponseEntity<UiPathQueueItem> response = restTemplate.postForEntity(
                    properties.getQueueItemsUrl(),
                    httpRequest,
                    UiPathQueueItem.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UiPathQueueItem queueItem = response.getBody();
                logger.info("Successfully created queue item with ID {} and reference '{}'", 
                           queueItem.getId(), reference);
                return queueItem;
            } else {
                throw new UiPathException.QueueItemCreationException(
                        reference, 
                        "Unexpected response status: " + response.getStatusCode()
                );
            }

        } catch (UiPathException.AuthenticationException e) {
            logger.error("Failed to authenticate before adding queue item: {}", e.getMessage());
            throw new UiPathException.QueueItemCreationException(reference, e);
        } catch (RestClientException e) {
            logger.error("Failed to add queue item with reference '{}': {}", reference, e.getMessage(), e);
            throw new UiPathException.QueueItemCreationException(reference, e);
        }
    }

    @Override
    @Async
    public CompletableFuture<UiPathQueueItem> addQueueItemAsync(UiPathQueueItemRequest request) {
        try {
            UiPathQueueItem result = addQueueItem(request);
            return CompletableFuture.completedFuture(result);
        } catch (UiPathException.QueueItemCreationException e) {
            logger.error("Async queue item creation failed: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public UiPathQueueItem getQueueItemById(Long queueItemId) 
            throws UiPathException.QueueItemNotFoundException, UiPathException.StatusCheckException {
        
        logger.debug("Getting queue item by ID: {}", queueItemId);

        try {
            String token = authenticate();
            HttpHeaders headers = createAuthHeaders(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = properties.getQueueItemsUrl() + "(" + queueItemId + ")";

            ResponseEntity<UiPathQueueItem> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    UiPathQueueItem.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UiPathQueueItem queueItem = response.getBody();
                logger.debug("Found queue item {} with status: {}", queueItemId, queueItem.getStatus());
                return queueItem;
            } else {
                throw new UiPathException.QueueItemNotFoundException(queueItemId.toString());
            }

        } catch (UiPathException.AuthenticationException e) {
            logger.error("Authentication failed while getting queue item: {}", e.getMessage());
            throw new UiPathException.StatusCheckException(queueItemId.toString(), e);
        } catch (RestClientException e) {
            logger.debug("Queue item not found: {}", queueItemId);
            throw new UiPathException.QueueItemNotFoundException(queueItemId.toString());
        }
    }

    @Override
    public List<UiPathQueueItem> getQueueItemsByReference(String reference) 
            throws UiPathException.StatusCheckException {
        
        logger.debug("Getting queue items by reference: '{}'", reference);

        try {
            String token = authenticate();
            HttpHeaders headers = createAuthHeaders(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // Build OData query: $filter=Reference eq 'reference'&$select=Id (or all fields)
            String url = UriComponentsBuilder.fromUriString(properties.getQueueItemsUrl())
                    .queryParam("$filter", "Reference eq '" + reference + "'")
                    .build()
                    .toUriString();

            ResponseEntity<UiPathODataResponse<UiPathQueueItem>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<UiPathODataResponse<UiPathQueueItem>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UiPathODataResponse<UiPathQueueItem> odataResponse = response.getBody();
                List<UiPathQueueItem> items = odataResponse.getValue();
                
                logger.debug("Found {} queue item(s) with reference '{}'", items != null ? items.size() : 0, reference);
                
                return items != null ? items : List.of();
            } else {
                logger.warn("Unexpected response when querying by reference '{}': {}", 
                           reference, response.getStatusCode());
                return List.of();
            }

        } catch (UiPathException.AuthenticationException e) {
            logger.error("Authentication failed while querying by reference: {}", e.getMessage());
            throw new UiPathException.StatusCheckException(reference, e);
        } catch (RestClientException e) {
            logger.error("Failed to query queue items by reference '{}': {}", reference, e.getMessage(), e);
            throw new UiPathException.StatusCheckException(reference, e);
        }
    }

    @Override
    public Optional<UiPathQueueItem> getLatestQueueItemByReference(String reference) 
            throws UiPathException.StatusCheckException {
        
        logger.debug("Getting latest queue item by reference: '{}'", reference);

        List<UiPathQueueItem> items = getQueueItemsByReference(reference);

        if (items.isEmpty()) {
            logger.debug("No queue items found with reference '{}'", reference);
            return Optional.empty();
        }

        // Get the item with the highest ID (most recent)
        Optional<UiPathQueueItem> latestItem = items.stream()
                .max(Comparator.comparing(UiPathQueueItem::getId));

        latestItem.ifPresent(item -> 
            logger.debug("Latest queue item for reference '{}' is ID {} with status: {}", 
                        reference, item.getId(), item.getStatus())
        );

        return latestItem;
    }

    @Override
    public boolean hasQueueItemFinalized(String reference) 
            throws UiPathException.QueueItemNotFoundException, UiPathException.StatusCheckException {
        
        logger.debug("Checking if queue item with reference '{}' has finalized", reference);

        Optional<UiPathQueueItem> latestItem = getLatestQueueItemByReference(reference);

        if (latestItem.isEmpty()) {
            throw new UiPathException.QueueItemNotFoundException(reference, "reference");
        }

        UiPathQueueItem item = latestItem.get();
        boolean finalized = item.isFinalized();
        
        logger.debug("Queue item {} (reference '{}') finalized status: {} (status: {})", 
                    item.getId(), reference, finalized, item.getStatus());

        return finalized;
    }

    @Override
    public boolean hasQueueItemFinalizedById(Long queueItemId) 
            throws UiPathException.QueueItemNotFoundException, UiPathException.StatusCheckException {
        
        logger.debug("Checking if queue item {} has finalized", queueItemId);

        UiPathQueueItem item = getQueueItemById(queueItemId);
        boolean finalized = item.isFinalized();
        
        logger.debug("Queue item {} finalized status: {} (status: {})", 
                    queueItemId, finalized, item.getStatus());

        return finalized;
    }

    @Override
    public boolean validateConnection() {
        try {
            String token = authenticate();
            boolean isValid = StringUtils.hasText(token);
            logger.debug("Connection validation: {}", isValid ? "successful" : "failed");
            return isValid;
        } catch (Exception e) {
            logger.warn("Connection validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            return validateConnection();
        } catch (Exception e) {
            logger.debug("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public UiPathQueueItemResult checkQueueItemByReference(String reference) {
        // If no UIPath reference, consider the process complete and successful
        if (reference == null || reference.isEmpty()) {
            logger.debug("No UIPath reference provided, returning NO_REFERENCE result");
            return UiPathQueueItemResult.noReference();
        }

        try {
            logger.debug("Checking UIPath queue item status for reference: '{}'", reference);
            Optional<UiPathQueueItem> queueItem = getLatestQueueItemByReference(reference);

            if (queueItem.isEmpty()) {
                logger.warn("UIPath queue item not found for reference: '{}'", reference);
                return UiPathQueueItemResult.notFound(reference);
            }

            UiPathQueueItem item = queueItem.get();
            QueueItemStatus status = item.getStatusEnum();
            logger.debug("UIPath queue item '{}' status: {}", reference, status);

            // If UIPath is not in final state, return in-progress
            if (!status.isFinalState()) {
                logger.debug("UIPath queue item '{}' is still in progress with status: {}", reference, status);
                return UiPathQueueItemResult.inProgress(item);
            }

            // If UIPath failed, return failure
            if (!status.isSuccessful()) {
                logger.warn("UIPath queue item '{}' failed with status: {}", reference, status);
                return UiPathQueueItemResult.failure(item);
            }

            // UIPath succeeded
            logger.debug("UIPath queue item '{}' completed successfully", reference);
            return UiPathQueueItemResult.success(item);

        } catch (UiPathException.StatusCheckException e) {
            logger.error("Failed to check UIPath status for reference '{}': {}", reference, e.getMessage(), e);
            return UiPathQueueItemResult.error("Failed to check UIPath status", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error checking UIPath status for reference '{}': {}", reference, e.getMessage(), e);
            return UiPathQueueItemResult.error("Unexpected error checking UIPath", e.getMessage());
        }
    }

    /**
     * Creates HTTP headers with authentication token and organization unit ID.
     */
    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");
        
        if (StringUtils.hasText(properties.getOrganizationUnitId())) {
            headers.set("X-UIPATH-OrganizationUnitId", properties.getOrganizationUnitId());
        }
        
        return headers;
    }
}
