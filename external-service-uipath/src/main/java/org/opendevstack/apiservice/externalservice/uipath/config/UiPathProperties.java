package org.opendevstack.apiservice.externalservice.uipath.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * Configuration properties for UIPath Orchestrator integration.
 */
@Data
@Component("uiPathOrchestratorProperties")
@ConfigurationProperties(prefix = "automation.platform.uipath")
public class UiPathProperties {

    /**
     * Base URL of the UIPath Orchestrator instance.
     * Example: https://orchestrator.example.com
     */
    private String host;

    /**
     * Authentication endpoint for UIPath Orchestrator.
     * Default: /api/Account/Authenticate
     */
    private String loginEndpoint = "/api/Account/Authenticate";

    /**
     * OData endpoint for Queue Items.
     * Default: /odata/QueueItems
     */
    private String queueItemsEndpoint = "/odata/QueueItems";

    /**
     * Client ID for UIPath Orchestrator authentication.
     */
    private String clientId;

    /**
     * Client Secret for UIPath Orchestrator authentication.
     */
    private String clientSecret;

    /**
     * Tenancy name in UIPath Orchestrator.
     * Default: default
     */
    private String tenancyName = "default";

    /**
     * Organization Unit ID for UIPath Orchestrator.
     * Required for multi-tenant setups.
     */
    private String organizationUnitId;

    /**
     * Request timeout in milliseconds.
     * Default: 30000 (30 seconds)
     */
    private int timeout = 30000;

    /**
     * SSL configuration properties.
     */
    private SslProperties ssl = new SslProperties();


    /**
     * Get the full login URL.
     */
    public String getLoginUrl() {
        return host + loginEndpoint;
    }

    /**
     * Get the full queue items URL.
     */
    public String getQueueItemsUrl() {
        return host + queueItemsEndpoint;
    }
}
