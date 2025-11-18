package org.opendevstack.apiservice.externalservice.webhookproxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for Webhook Proxy service.
 * Supports multiple cluster configurations where webhook proxy URLs are constructed dynamically.
 * 
 * URL Pattern: https://webhook-proxy-{projectKey}-cd.{clusterBase}
 * 
 * Example configuration:
 * externalservice:
 *   webhook-proxy:
 *     clusters:
 *       cluster-a:
 *         cluster-base: apps.cluster-a.ocp.example.com
 *         connection-timeout: 30000
 *         read-timeout: 30000
 *       cluster-b:
 *         cluster-base: apps.cluster-b.ocp.example.com
 *         connection-timeout: 60000
 *         read-timeout: 60000
 */
@Configuration
@ConfigurationProperties(prefix = "externalservices.webhook-proxy")
@Data
public class WebhookProxyConfiguration {
    
    /**
     * Map of cluster configurations with cluster name as key and configuration as value.
     */
    private Map<String, ClusterConfig> clusters = new HashMap<>();
    
    /**
     * Configuration for a single cluster.
     */
    @Data
    public static class ClusterConfig {
        
        /**
         * Base domain for the cluster (without protocol or webhook-proxy prefix)
         * Example: apps.cluster-a.ocp.example.com
         * 
         * The full webhook proxy URL will be constructed as:
         * https://webhook-proxy-{projectKey}-cd.{clusterBase}
         */
        private String clusterBase;
        
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
        
        /**
         * Default Jenkinsfile path if not specified in request (default: Jenkinsfile)
         */
        private String defaultJenkinsfilePath = "Jenkinsfile";
        
        /**
         * Constructs the webhook proxy base URL for a given project key
         * 
         * @param projectKey The project key (e.g., "example-project")
         * @return The complete webhook proxy URL (e.g., "https://webhook-proxy-example-project-cd.apps.cluster-a.ocp.example.com")
         */
        public String buildWebhookProxyUrl(String projectKey) {
            return String.format("https://webhook-proxy-%s-cd.%s", projectKey, clusterBase);
        }
    }
}
