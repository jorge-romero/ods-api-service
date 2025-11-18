package org.opendevstack.apiservice.externalservice.webhookproxy.service;

import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildRequest;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildResponse;
import org.opendevstack.apiservice.externalservice.webhookproxy.exception.WebhookProxyException;

import java.util.Set;

/**
 * Service interface for triggering builds via the ODS Webhook Proxy.
 * Provides high-level methods to trigger release manager builds across different clusters.
 */
public interface WebhookProxyService {
    
    /**
     * Trigger a release manager build via webhook proxy
     * 
     * @param clusterName Name of the target cluster (e.g., "cluster-a", "cluster-b")
     * @param projectKey Project key (e.g., "example-project")
     * @param request Build request containing branch, repository, project, and environment variables
     * @param triggerSecret Secret required to authenticate with the webhook proxy
     * @return Build response containing status and result
     * @throws WebhookProxyException if the build trigger fails
     */
    WebhookProxyBuildResponse triggerBuild(String clusterName, String projectKey,
                                          WebhookProxyBuildRequest request, String triggerSecret)
            throws WebhookProxyException;
    
    /**
     * Trigger a release manager build with optional parameters
     * 
     * @param clusterName Name of the target cluster
     * @param projectKey Project key
     * @param request Build request
     * @param triggerSecret Trigger secret for authentication
     * @param jenkinsfilePath Optional custom Jenkinsfile path (uses default if null)
     * @param component Optional component name (extracted from repository if null)
     * @return Build response containing status and result
     * @throws WebhookProxyException if the build trigger fails
     */
    WebhookProxyBuildResponse triggerBuild(String clusterName, String projectKey,
                                          WebhookProxyBuildRequest request, String triggerSecret,
                                          String jenkinsfilePath, String component)
            throws WebhookProxyException;
    
    /**
     * Get all available cluster names
     * 
     * @return Set of configured cluster names
     */
    Set<String> getAvailableClusters();
    
    /**
     * Check if a cluster is configured
     * 
     * @param clusterName Name of the cluster to check
     * @return true if configured, false otherwise
     */
    boolean hasCluster(String clusterName);
    
    /**
     * Build the webhook proxy URL for a specific cluster and project
     * 
     * @param clusterName Name of the cluster
     * @param projectKey Project key
     * @return The constructed webhook proxy URL
     * @throws WebhookProxyException.ConfigurationException if the cluster is not configured
     */
    String getWebhookProxyUrl(String clusterName, String projectKey) throws WebhookProxyException.ConfigurationException;
}
