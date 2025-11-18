package org.opendevstack.apiservice.externalservice.ocp.client;

import org.opendevstack.apiservice.externalservice.ocp.config.OpenshiftServiceConfiguration.OpenshiftInstanceConfig;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for interacting with OpenShift API.
 * Provides methods to retrieve secrets and other resources from an OpenShift cluster.
 */
@Slf4j
public class OpenshiftApiClient {
    
    private final String instanceName;
    private final OpenshiftInstanceConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for OpenshiftApiClient
     * 
     * @param instanceName Name of the OpenShift instance
     * @param config Configuration for this instance
     * @param restTemplate RestTemplate configured with appropriate timeouts and SSL settings
     */
    public OpenshiftApiClient(String instanceName, OpenshiftInstanceConfig config, RestTemplate restTemplate) {
        this.instanceName = instanceName;
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Get a secret from the OpenShift cluster
     * 
     * @param secretName Name of the secret to retrieve
     * @return Map containing the decoded secret data
     * @throws OpenshiftException if the secret cannot be retrieved
     */
    public Map<String, String> getSecret(String secretName) throws OpenshiftException {
        return getSecret(secretName, config.getNamespace());
    }
    
    /**
     * Get a secret from a specific namespace in the OpenShift cluster
     * 
     * @param secretName Name of the secret to retrieve
     * @param namespace Namespace where the secret is located
     * @return Map containing the decoded secret data
     * @throws OpenshiftException if the secret cannot be retrieved
     */
    public Map<String, String> getSecret(String secretName, String namespace) throws OpenshiftException {
        log.debug("Retrieving secret '{}' from namespace '{}' in OpenShift instance '{}'", 
                  secretName, namespace, instanceName);
        
        String url = String.format("%s/api/v1/namespaces/%s/secrets/%s", 
                                   config.getApiUrl(), namespace, secretName);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseSecretData(response.getBody());
            } else {
                throw new OpenshiftException(
                    String.format("Failed to retrieve secret '%s' from namespace '%s'. Status: %s", 
                                  secretName, namespace, response.getStatusCode())
                );
            }
            
        } catch (RestClientException e) {
            log.error("Error retrieving secret '{}' from OpenShift instance '{}'", secretName, instanceName, e);
            throw new OpenshiftException(
                String.format("Failed to retrieve secret '%s' from OpenShift instance '%s'", 
                              secretName, instanceName), 
                e
            );
        }
    }
    
    /**
     * Get a specific value from a secret
     * 
     * @param secretName Name of the secret
     * @param key Key within the secret data
     * @return The decoded secret value
     * @throws OpenshiftException if the secret or key cannot be retrieved
     */
    public String getSecretValue(String secretName, String key) throws OpenshiftException {
        return getSecretValue(secretName, key, config.getNamespace());
    }
    
    /**
     * Get a specific value from a secret in a specific namespace
     * 
     * @param secretName Name of the secret
     * @param key Key within the secret data
     * @param namespace Namespace where the secret is located
     * @return The decoded secret value
     * @throws OpenshiftException if the secret or key cannot be retrieved
     */
    public String getSecretValue(String secretName, String key, String namespace) throws OpenshiftException {
        Map<String, String> secretData = getSecret(secretName, namespace);
        
        if (secretData.containsKey(key)) {
            return secretData.get(key);
        } else {
            throw new OpenshiftException(
                String.format("Key '%s' not found in secret '%s' in namespace '%s'", 
                              key, secretName, namespace)
            );
        }
    }
    
    /**
     * Check if a secret exists
     * 
     * @param secretName Name of the secret
     * @return true if the secret exists, false otherwise
     */
    public boolean secretExists(String secretName) {
        return secretExists(secretName, config.getNamespace());
    }
    
    /**
     * Check if a secret exists in a specific namespace
     * 
     * @param secretName Name of the secret
     * @param namespace Namespace where the secret might be located
     * @return true if the secret exists, false otherwise
     */
    public boolean secretExists(String secretName, String namespace) {
        try {
            getSecret(secretName, namespace);
            return true;
        } catch (OpenshiftException e) {
            log.debug("Secret '{}' does not exist in namespace '{}'", secretName, namespace);
            return false;
        }
    }
    
    /**
     * Create HTTP headers with authentication token
     * 
     * @return HttpHeaders with authorization and content type
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(config.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    
    /**
     * Parse secret data from the API response and decode base64 values
     * 
     * @param jsonResponse JSON response from OpenShift API
     * @return Map with decoded secret values
     * @throws OpenshiftException if parsing fails
     */
    private Map<String, String> parseSecretData(String jsonResponse) throws OpenshiftException {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode dataNode = root.get("data");
            
            Map<String, String> secretData = new HashMap<>();
            
            if (dataNode != null && dataNode.isObject()) {
                dataNode.fieldNames().forEachRemaining(key -> {
                    String base64Value = dataNode.get(key).asText();
                    String decodedValue = decodeBase64(base64Value);
                    secretData.put(key, decodedValue);
                });
            }
            
            log.debug("Successfully parsed secret data with {} keys", secretData.size());
            return secretData;
            
        } catch (Exception e) {
            log.error("Error parsing secret data", e);
            throw new OpenshiftException("Failed to parse secret data from OpenShift response", e);
        }
    }
    
    /**
     * Decode a base64 encoded string
     * 
     * @param base64Value Base64 encoded string
     * @return Decoded string
     */
    private String decodeBase64(String base64Value) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decode base64 value, returning original value", e);
            return base64Value;
        }
    }
    
    /**
     * Get the instance name this client is configured for
     * 
     * @return Instance name
     */
    public String getInstanceName() {
        return instanceName;
    }
    
    /**
     * Get the default namespace for this instance
     * 
     * @return Default namespace
     */
    public String getDefaultNamespace() {
        return config.getNamespace();
    }
}

