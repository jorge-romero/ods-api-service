package org.opendevstack.apiservice.externalservice.bitbucket.client;

import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration.BitbucketInstanceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.openapitools.jackson.nullable.JsonNullableModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * Client for interacting with Bitbucket API.
 * Wraps the generated OpenAPI client and provides a simplified interface.
 */
@Slf4j
public class BitbucketApiClient {
    
    private final String instanceName;
    private final BitbucketInstanceConfig config;
    private final ApiClient apiClient;
    
    /**
     * Constructor for BitbucketApiClient
     * 
     * @param instanceName Name of the Bitbucket instance
     * @param config Configuration for this instance
     * @param restTemplate RestTemplate configured with appropriate timeouts and SSL settings
     */
    public BitbucketApiClient(String instanceName, BitbucketInstanceConfig config, RestTemplate restTemplate) {
        this.instanceName = instanceName;
        this.config = config;
        
        // Configure ObjectMapper with JsonNullableModule for the RestTemplate
        configureRestTemplateWithJsonNullable(restTemplate);
        
        // Initialize the generated ApiClient
        this.apiClient = new ApiClient(restTemplate);
        this.apiClient.setBasePath(config.getBaseUrl());

        // Configure authentication - prefer bearer token over basic auth
        if (config.getBearerToken() != null && !config.getBearerToken().isEmpty()) {
            // Use bearer token authentication
            var auth = (org.opendevstack.apiservice.externalservice.bitbucket.client.auth.HttpBearerAuth) 
                this.apiClient.getAuthentication("bearerAuth");
            auth.setBearerToken(config.getBearerToken());
            log.info("BitbucketApiClient initialized for instance '{}' with bearer token authentication, base URL: {}", 
                     instanceName, config.getBaseUrl());
        } else if (config.getUsername() != null && config.getPassword() != null) {
            // Fallback to basic authentication
            this.apiClient.setUsername(config.getUsername());
            this.apiClient.setPassword(config.getPassword());
            log.info("BitbucketApiClient initialized for instance '{}' with basic authentication, base URL: {}", 
                     instanceName, config.getBaseUrl());
        } else {
            log.warn("BitbucketApiClient initialized for instance '{}' without authentication (neither bearer token nor username/password provided)", 
                     instanceName);
        }
    }
    
    /**
     * Get the underlying generated ApiClient for advanced usage
     * 
     * @return The generated ApiClient instance
     */
    public ApiClient getApiClient() {
        return apiClient;
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
     * Get the base URL for this Bitbucket instance
     * 
     * @return Base URL
     */
    public String getBaseUrl() {
        return config.getBaseUrl();
    }
    
    /**
     * Get the configuration for this instance
     * 
     * @return Instance configuration
     */
    public BitbucketInstanceConfig getConfig() {
        return config;
    }
    
    /**
     * Configure RestTemplate's ObjectMapper to handle JsonNullable types
     * 
     * @param restTemplate RestTemplate to configure
     */
    private void configureRestTemplateWithJsonNullable(RestTemplate restTemplate) {
        // Find the Jackson message converter and add JsonNullableModule
        for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jacksonConverter = 
                    (MappingJackson2HttpMessageConverter) converter;
                ObjectMapper objectMapper = jacksonConverter.getObjectMapper();
                
                // Register JsonNullableModule to handle JsonNullable types
                objectMapper.registerModule(new JsonNullableModule());
                
                log.debug("Registered JsonNullableModule with ObjectMapper for instance '{}'", instanceName);
                return;
            }
        }
        
        log.warn("No MappingJackson2HttpMessageConverter found in RestTemplate for instance '{}'", instanceName);
    }
}
