package org.opendevstack.apiservice.externalservice.marketplace.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceInstanceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.ApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.auth.HttpBearerAuth;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Getter
@Slf4j
public class MarketplaceApiClient {


    private final String instanceName;
    private final MarketplaceInstanceConfig config;
    private final ApiClient apiClient;

    /**
     * Constructor for MarketplaceApiClient.
     *
     * @param instanceName Name of the Marketplace instance
     * @param config       Configuration for this instance
     * @param restTemplate RestTemplate configured with appropriate timeouts and SSL settings
     */
    public MarketplaceApiClient(String instanceName, MarketplaceInstanceConfig config, RestTemplate restTemplate) {
        this.instanceName = instanceName;
        this.config = config;

        // Configure ObjectMapper with JsonNullableModule for the RestTemplate
        configureRestTemplateWithJsonNullable(restTemplate);

        // Initialize the generated ApiClient
        this.apiClient = new ApiClient(restTemplate);

        if (config.getUsername() != null && config.getPassword() != null) {
            this.apiClient.setUsername(config.getUsername());
            this.apiClient.setPassword(config.getPassword());
            log.info("MarketplaceApiClient for instance '{}' uses basic authentication", instanceName);
        }

        log.info("MarketplaceApiClient initialized for instance '{}'", instanceName);
    }

    /**
     * Configure RestTemplate's ObjectMapper to handle JsonNullable types.
     *
     * @param restTemplate RestTemplate to configure
     */
    private void configureRestTemplateWithJsonNullable(RestTemplate restTemplate) {
        for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                ObjectMapper objectMapper = jacksonConverter.getObjectMapper();
                objectMapper.registerModule(new JsonNullableModule());
                log.debug("Registered JsonNullableModule with ObjectMapper for instance '{}'", instanceName);
                return;
            }
        }
        log.warn("No MappingJackson2HttpMessageConverter found in RestTemplate for instance '{}'", instanceName);
    }

    /**
     * Sets the bearer token for authentication on this client.
     * This replaces any previously configured bearer token.
     *
     * @param bearerToken the bearer token to use for subsequent API calls
     */
    public void setBearerToken(String bearerToken) {
        HttpBearerAuth auth = (HttpBearerAuth) this.apiClient.getAuthentication("bearerAuth");
        auth.setBearerToken(bearerToken);
    }
}
