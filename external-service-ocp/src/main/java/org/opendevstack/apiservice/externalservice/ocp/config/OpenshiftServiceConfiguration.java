package org.opendevstack.apiservice.externalservice.ocp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for OpenShift service.
 * Loads multiple OpenShift instance configurations from Spring configuration files.
 */
@Configuration
@ConfigurationProperties(prefix = "externalservices.openshift")
@Data
public class OpenshiftServiceConfiguration {
    
    /**
     * Map of OpenShift instances with instance name as key and configuration as value.
     * Example:
     * openshift:
     *   instances:
     *     dev:
     *       api-url: https://api.dev.ocp.example.com:6443
     *       token: your-token-here
     *       namespace: default
     *     prod:
     *       api-url: https://api.prod.ocp.example.com:6443
     *       token: your-token-here
     *       namespace: production
     */
    private Map<String, OpenshiftInstanceConfig> instances = new HashMap<>();
    
    /**
     * Configuration for a single OpenShift instance.
     */
    @Data
    public static class OpenshiftInstanceConfig {
        /**
         * The API URL of the OpenShift cluster
         */
        private String apiUrl;
        
        /**
         * Authentication token for accessing the OpenShift API
         */
        private String token;
        
        /**
         * Default namespace to use for this instance
         */
        private String namespace;
        
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
