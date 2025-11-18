package org.opendevstack.apiservice.externalservice.projectsinfoservice.client;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.annotations.CacheableWithFallback;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.InvalidContentProcessException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.InvalidTokenException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.UnableToReachAzureException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class AzureGraphClient {

    private static final String USER_INFO_URL = "https://graph.microsoft.com/v1.0/me";
    private static final String MEMBER_OF_URL = "https://graph.microsoft.com/v1.0/me/memberOf";
    public static final String ERROR_WHILE_GETTING_USER_GROUPS = "Error while getting user groups";
    public static final String ERROR_WHILE_PROCESSING_SERVER_RESPONSE = "Error while processing server response";
    public static final String ERROR_WHILE_GETTING_APPLICATION_GROUPS = "Error while getting application groups";

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    @Value("${externalservices.projects-info-service.azure.groups.page-size}")
    private Integer pageSize;

    @Value("${externalservices.projects-info-service.azure.access-token}")
    private String azureAccessToken;

    @Value("${externalservices.projects-info-service.azure.datahub.group-id}")
    private String dataHubGroupId;

    AzureGraphClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.mapper = new ObjectMapper();
    }

    public Set<String> getUserGroups(String userAccessToken) {
        Set<String> groupIds = new HashSet<>();
        String url = MEMBER_OF_URL + "?$top=" + pageSize; // e.g., "https://graph.microsoft.com/v1.0/me/memberOf?$top=100"

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userAccessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            while (url != null) {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                groupIds.addAll(processAzureResponse(response));

                // Parse the nextLink if present
                JsonNode root = new ObjectMapper().readTree(response.getBody());
                JsonNode nextLinkNode = root.get("@odata.nextLink");

                if (nextLinkNode != null) {
                    log.debug("Next link found: {}", nextLinkNode.asText());

                    url = nextLinkNode != null ? nextLinkNode.asText() : null;
                } else {
                    url = null; // No more pages
                }
            }
        } catch (HttpClientErrorException | IOException e) {
            log.error(ERROR_WHILE_GETTING_USER_GROUPS, e);
            throw new InvalidTokenException(ERROR_WHILE_GETTING_USER_GROUPS, e);
        }

        return groupIds;
    }

    // TODO: Can not use short circuit at the moment, we will do it when integration is completed
    @Cacheable("dataHubGroups")
    public Set<String> getDataHubGroups() {
        return getApplicationGroups(azureAccessToken, dataHubGroupId);
    }

    // TODO: Can not use short circuit at the moment, we will do it when integration is completed
    @Cacheable("testingHubGroups")
    public Set<String> getTestingHubGroups() {
        return getApplicationGroups(azureAccessToken, dataHubGroupId);
    }

    protected Set<String> getApplicationGroups(String accessToken, String appObjectId) {
        Set<String> groupNames = new HashSet<>();
        String url = "https://graph.microsoft.com/v1.0/servicePrincipals/" + appObjectId + "/appRoleAssignedTo?$top=" + pageSize;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            while (url != null) {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                groupNames.addAll(processAzureResponse(response));

                JsonNode root = mapper.readTree(response.getBody());
                JsonNode nextLinkNode = root.get("@odata.nextLink");
                url = nextLinkNode != null ? nextLinkNode.asText() : null;
            }
        } catch (HttpClientErrorException e) {
            log.error(ERROR_WHILE_GETTING_APPLICATION_GROUPS, e);

            if(e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new UnableToReachAzureException(ERROR_WHILE_GETTING_APPLICATION_GROUPS, e);
            } else {
                log.trace("There were an error when trying to get groups. We continue the process and consider platform as disabled, " +
                        "in order to not block application usage", e);
            }
        } catch (IOException e) {
            log.error(ERROR_WHILE_PROCESSING_SERVER_RESPONSE, e);
        }

        return groupNames;
    }

    @CacheableWithFallback(primary = "userEmail", fallback = "userEmail-fallback")
    public String getUserEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, entity, String.class);
            JsonNode root = mapper.readTree(response.getBody());

            // Try to get mail or userPrincipalName
            if (root.has("mail") && !root.get("mail").isNull()) {
                return root.get("mail").asText();
            } else if (root.has("userPrincipalName")) {
                return root.get("userPrincipalName").asText();
            } else {
                throw new InvalidContentProcessException("Email not found in user profile");
            }

        } catch (HttpClientErrorException e) {
            log.error("Error while getting user email", e);
            throw new InvalidTokenException("Error while getting user email", e);
        } catch (Exception e) {
            log.error("Error while processing user email response", e);
            throw new InvalidContentProcessException("Error while processing user email response", e);
        }
    }


    private Set<String> processAzureResponse(ResponseEntity<String> response) {
        log.trace("Processing Azure response: {}", response.getBody());

        Set<String> groups = new HashSet<>();

        try {
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode values = root.path("value");

            for (JsonNode group : values) {
                if (group.has("displayName")) {
                    groups.add(group.get("displayName").asText());
                }
            }
        } catch (Exception e) {
            log.error(ERROR_WHILE_PROCESSING_SERVER_RESPONSE, e);

            throw new InvalidContentProcessException(ERROR_WHILE_PROCESSING_SERVER_RESPONSE, e);
        }

        return groups;
    }
}

