package org.opendevstack.apiservice.externalservice.bitbucket.client;

import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration;
import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration.BitbucketInstanceConfig;
import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating BitbucketApiClient instances.
 * Uses the factory pattern to provide configured clients for different Bitbucket instances.
 * Clients are cached and reused for efficiency.
 */
@Component
@Slf4j
public class BitbucketApiClientFactory {
    
    private final BitbucketServiceConfiguration configuration;
    private final Map<String, BitbucketApiClient> clientCache;
    private final RestTemplateBuilder restTemplateBuilder;
    
    /**
     * Constructor with dependency injection
     * 
     * @param configuration Bitbucket service configuration
     * @param restTemplateBuilder RestTemplate builder for creating HTTP clients
     */
    public BitbucketApiClientFactory(BitbucketServiceConfiguration configuration, 
                                     RestTemplateBuilder restTemplateBuilder) {
        this.configuration = configuration;
        this.restTemplateBuilder = restTemplateBuilder;
        this.clientCache = new ConcurrentHashMap<>();
        
        log.info("BitbucketApiClientFactory initialized with {} instance(s)", 
                 configuration.getInstances().size());
    }
    
    /**
     * Get a BitbucketApiClient for a specific instance
     * 
     * @param instanceName Name of the Bitbucket instance
     * @return Configured BitbucketApiClient
     * @throws BitbucketException if the instance is not configured
     */
    public BitbucketApiClient getClient(String instanceName) throws BitbucketException {
        // Check cache first
        if (clientCache.containsKey(instanceName)) {
            log.debug("Returning cached client for instance '{}'", instanceName);
            return clientCache.get(instanceName);
        }
        
        // Create new client
        BitbucketInstanceConfig instanceConfig = configuration.getInstances().get(instanceName);
        
        if (instanceConfig == null) {
            throw new BitbucketException(
                String.format("Bitbucket instance '%s' is not configured. Available instances: %s", 
                              instanceName, configuration.getInstances().keySet())
            );
        }
        
        log.info("Creating new BitbucketApiClient for instance '{}'", instanceName);
        
        RestTemplate restTemplate = createRestTemplate(instanceConfig);
        BitbucketApiClient client = new BitbucketApiClient(instanceName, instanceConfig, restTemplate);
        
        // Cache the client
        clientCache.put(instanceName, client);
        
        return client;
    }
    
    /**
     * Get the default client (first configured instance)
     * 
     * @return BitbucketApiClient for the first configured instance
     * @throws BitbucketException if no instances are configured
     */
    public BitbucketApiClient getDefaultClient() throws BitbucketException {
        if (configuration.getInstances().isEmpty()) {
            throw new BitbucketException("No Bitbucket instances configured");
        }
        
        String firstInstanceName = configuration.getInstances().keySet().iterator().next();
        log.debug("Using default instance: '{}'", firstInstanceName);
        
        return getClient(firstInstanceName);
    }
    
    /**
     * Get all available instance names
     * 
     * @return Set of configured instance names
     */
    public java.util.Set<String> getAvailableInstances() {
        return configuration.getInstances().keySet();
    }
    
    /**
     * Check if an instance is configured
     * 
     * @param instanceName Name of the instance to check
     * @return true if configured, false otherwise
     */
    public boolean hasInstance(String instanceName) {
        return configuration.getInstances().containsKey(instanceName);
    }
    
    /**
     * Clear the client cache (useful for testing or when configuration changes)
     */
    public void clearCache() {
        log.info("Clearing BitbucketApiClient cache");
        clientCache.clear();
    }
    
    /**
     * Create a configured RestTemplate for a Bitbucket instance
     * 
     * @param config Configuration for the instance
     * @return Configured RestTemplate
     */
    private RestTemplate createRestTemplate(BitbucketInstanceConfig config) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        // Set timeouts using SimpleClientHttpRequestFactory
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(config.getConnectionTimeout());
        requestFactory.setReadTimeout(config.getReadTimeout());
        restTemplate.setRequestFactory(requestFactory);

        // Configure SSL if trustAllCertificates is enabled
        if (config.isTrustAllCertificates()) {
            log.warn("Trust all certificates is enabled for Bitbucket connection. " +
                    "This should only be used in development environments!");
            configureTrustAllCertificates(restTemplate);
        }

        return restTemplate;
    }
    
    /**
     * Configure RestTemplate to trust all SSL certificates
     * WARNING: This should only be used in development environments
     * 
     * @param restTemplate RestTemplate to configure
     */
    @SuppressWarnings({"java:S4830", "java:S1186"}) // Intentionally disabling SSL validation for development
    private void configureTrustAllCertificates(RestTemplate restTemplate) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    // Intentionally empty - trusting all certificates for development environments
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // No validation performed - development only
                    }
                    // Intentionally empty - trusting all certificates for development environments
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // No validation performed - development only
                    }
                }
            };
            
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            // Intentionally disabling hostname verification for development environments
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Failed to configure SSL trust all certificates", e);
        }
    }
}
