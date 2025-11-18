package org.opendevstack.apiservice.externalservice.bitbucket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Bitbucket service.
 * Loads multiple Bitbucket instance configurations from Spring configuration files.
 */
@Configuration
@ConfigurationProperties(prefix = "externalservices.bitbucket")
@Data
public class BitbucketServiceConfiguration {
    
    /**
     * Map of Bitbucket instances with instance name as key and configuration as value.
     * Example:
     * externalservice:
     *   bitbucket:
     *     instances:
     *       dev:
     *         base-url: https://bitbucket.dev.example.com
     *         username: admin
     *         password: password123
     *       prod:
     *         base-url: https://bitbucket.example.com
     *         username: admin
     *         password: secret
     */
    private Map<String, BitbucketInstanceConfig> instances = new HashMap<>();
    
    /**
     * Configuration for a single Bitbucket instance.
     */
    @Data
    public static class BitbucketInstanceConfig {
        /**
         * The base URL of the Bitbucket server (e.g., https://bitbucket.example.com)
         */
        private String baseUrl;
        
        /**
         * Bearer token for authentication (preferred method)
         * If provided, this will be used instead of username/password
         */
        private String bearerToken;
        
        /**
         * Username for authentication (used with password for basic auth)
         * Only used if bearerToken is not provided
         */
        private String username;
        
        /**
         * Password or personal access token for authentication
         * Only used if bearerToken is not provided
         */
        private String password;
        
        /**
         * Connection timeout in milliseconds (default: 30000)
         */
        private int connectionTimeout = 30000;
        
        /**
         * Read timeout in milliseconds (default: 30000)
         */
        private int readTimeout = 30000;
        
        /**
         * Whether to trust all SSL certificates (default: false)
         * WARNING: Should only be used in development environments
         */
        private boolean trustAllCertificates = false;
    }
}
