package org.opendevstack.apiservice.externalservice.webhookproxy.client;

import org.opendevstack.apiservice.externalservice.webhookproxy.config.WebhookProxyConfiguration.ClusterConfig;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildRequest;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildResponse;
import org.opendevstack.apiservice.externalservice.webhookproxy.exception.WebhookProxyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client for interacting with the ODS Webhook Proxy.
 * Provides methods to trigger release manager builds via the webhook proxy /build endpoint.
 */
@Slf4j
public class WebhookProxyClient {
    
    private static final String BUILD_ENDPOINT = "/build";
    
    private final String clusterName;
    private final String projectKey;
    private final String baseUrl;
    private final ClusterConfig config;
    private final RestTemplate restTemplate;
    
    /**
     * Constructor for WebhookProxyClient
     * 
     * @param clusterName Name of the cluster
     * @param projectKey Project key
     * @param baseUrl Base URL of the webhook proxy
     * @param config Cluster configuration
     * @param restTemplate RestTemplate configured with appropriate timeouts and SSL settings
     */
    public WebhookProxyClient(String clusterName, String projectKey, String baseUrl, 
                             ClusterConfig config, RestTemplate restTemplate) {
        this.clusterName = clusterName;
        this.projectKey = projectKey;
        this.baseUrl = baseUrl;
        this.config = config;
        this.restTemplate = restTemplate;
    }
    
    /**
     * Trigger a build via the webhook proxy /build endpoint
     * 
     * @param request Build request with branch, repository, project, and environment variables
     * @param triggerSecret Secret required to authenticate with the webhook proxy
     * @return Build response containing status and result
     * @throws WebhookProxyException if the build trigger fails
     */
    public WebhookProxyBuildResponse triggerBuild(WebhookProxyBuildRequest request, String triggerSecret) 
            throws WebhookProxyException {
        return triggerBuild(request, triggerSecret, null, null);
    }
    
    /**
     * Trigger a build via the webhook proxy /build endpoint with optional parameters
     * 
     * @param request Build request with branch, repository, project, and environment variables
     * @param triggerSecret Secret required to authenticate with the webhook proxy
     * @param jenkinsfilePath Optional custom Jenkinsfile path
     * @param component Optional component name (overrides default extraction from repository)
     * @return Build response containing status and result
     * @throws WebhookProxyException if the build trigger fails
     */
    public WebhookProxyBuildResponse triggerBuild(WebhookProxyBuildRequest request, String triggerSecret,
                                                  String jenkinsfilePath, String component) 
            throws WebhookProxyException {
        
        log.debug("Triggering build on cluster '{}' for project '{}': branch='{}', repository='{}'",
                 clusterName, projectKey, request.getBranch(), request.getRepository());
        
        // Validate request
        validateBuildRequest(request, triggerSecret);
        
        // Build URL with query parameters
        String url = buildRequestUrl(triggerSecret, jenkinsfilePath, component);
        
        try {
            // Create request entity
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WebhookProxyBuildRequest> entity = new HttpEntity<>(request, headers);
            
            // Make the request
            log.info("Sending build request to webhook proxy: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // Process response
            int statusCode = response.getStatusCode().value();
            String body = response.getBody();
            boolean success = response.getStatusCode().is2xxSuccessful();
            
            log.info("Build trigger response: status={}, success={}", statusCode, success);
            
            WebhookProxyBuildResponse buildResponse = new WebhookProxyBuildResponse();
            buildResponse.setStatusCode(statusCode);
            buildResponse.setBody(body);
            buildResponse.setSuccess(success);
            
            if (!success) {
                buildResponse.setErrorMessage("Build trigger returned non-success status: " + statusCode);
            }
            
            return buildResponse;
            
        } catch (HttpClientErrorException e) {
            log.error("Client error triggering build: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new WebhookProxyException.AuthenticationException(
                    "Authentication failed. Trigger secret may be incorrect.", e
                );
            }
            
            throw new WebhookProxyException.BuildTriggerException(
                "Build trigger failed: " + e.getMessage(),
                e.getStatusCode().value(),
                e
            );
            
        } catch (HttpServerErrorException e) {
            log.error("Server error triggering build: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new WebhookProxyException.BuildTriggerException(
                "Webhook proxy server error: " + e.getMessage(),
                e.getStatusCode().value(),
                e
            );
            
        } catch (RestClientException e) {
            log.error("Error communicating with webhook proxy: {}", e.getMessage(), e);
            throw new WebhookProxyException.ConnectionException(
                "Failed to connect to webhook proxy at " + baseUrl,
                e
            );
        }
    }
    
    /**
     * Validate the build request
     * 
     * @param request Build request to validate
     * @param triggerSecret Trigger secret
     * @throws WebhookProxyException.ValidationException if validation fails
     */
    private void validateBuildRequest(WebhookProxyBuildRequest request, String triggerSecret) 
            throws WebhookProxyException.ValidationException {
        
        if (request == null) {
            throw new WebhookProxyException.ValidationException("Build request cannot be null");
        }
        
        if (triggerSecret == null || triggerSecret.trim().isEmpty()) {
            throw new WebhookProxyException.ValidationException("Trigger secret is required");
        }
        
        if (request.getBranch() == null || request.getBranch().trim().isEmpty()) {
            throw new WebhookProxyException.ValidationException("Branch is required");
        }
        
        if (request.getRepository() == null || request.getRepository().trim().isEmpty()) {
            throw new WebhookProxyException.ValidationException("Repository is required");
        }
        
        if (request.getProject() == null || request.getProject().trim().isEmpty()) {
            throw new WebhookProxyException.ValidationException("Project is required");
        }
    }
    
    /**
     * Build the full request URL with query parameters
     * 
     * @param triggerSecret Trigger secret for authentication
     * @param jenkinsfilePath Optional Jenkinsfile path
     * @param component Optional component name
     * @return Complete URL with query parameters
     */
    private String buildRequestUrl(String triggerSecret, String jenkinsfilePath, String component) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(baseUrl + BUILD_ENDPOINT)
            .queryParam("trigger_secret", triggerSecret);
        
        if (jenkinsfilePath != null && !jenkinsfilePath.trim().isEmpty()) {
            builder.queryParam("jenkinsfile_path", jenkinsfilePath);
        } else if (config.getDefaultJenkinsfilePath() != null) {
            builder.queryParam("jenkinsfile_path", config.getDefaultJenkinsfilePath());
        }
        
        if (component != null && !component.trim().isEmpty()) {
            builder.queryParam("component", component);
        }
        
        return builder.toUriString();
    }
    
    /**
     * Get the cluster name
     * 
     * @return Cluster name
     */
    public String getClusterName() {
        return clusterName;
    }
    
    /**
     * Get the project key
     * 
     * @return Project key
     */
    public String getProjectKey() {
        return projectKey;
    }
    
    /**
     * Get the base URL
     * 
     * @return Webhook proxy base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
