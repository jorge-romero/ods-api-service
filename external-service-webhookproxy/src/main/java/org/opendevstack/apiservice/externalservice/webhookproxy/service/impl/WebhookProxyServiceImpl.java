package org.opendevstack.apiservice.externalservice.webhookproxy.service.impl;

import org.opendevstack.apiservice.externalservice.webhookproxy.client.WebhookProxyClient;
import org.opendevstack.apiservice.externalservice.webhookproxy.client.WebhookProxyClientFactory;
import org.opendevstack.apiservice.externalservice.webhookproxy.config.WebhookProxyConfiguration;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildRequest;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildResponse;
import org.opendevstack.apiservice.externalservice.webhookproxy.exception.WebhookProxyException;
import org.opendevstack.apiservice.externalservice.webhookproxy.service.WebhookProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Implementation of WebhookProxyService.
 * Uses WebhookProxyClientFactory to obtain clients for different clusters
 * and delegates build trigger operations to the appropriate client.
 */
@Service
@Slf4j
public class WebhookProxyServiceImpl implements WebhookProxyService {
    
    private final WebhookProxyClientFactory clientFactory;
    private final WebhookProxyConfiguration configuration;
    
    /**
     * Constructor with dependency injection
     * 
     * @param clientFactory Factory for creating webhook proxy clients
     * @param configuration Webhook proxy configuration
     */
    public WebhookProxyServiceImpl(WebhookProxyClientFactory clientFactory,
                                  WebhookProxyConfiguration configuration) {
        this.clientFactory = clientFactory;
        this.configuration = configuration;
        log.info("WebhookProxyServiceImpl initialized");
    }
    
    @Override
    public WebhookProxyBuildResponse triggerBuild(String clusterName, String projectKey,
                                                  WebhookProxyBuildRequest request, String triggerSecret)
            throws WebhookProxyException {
        
        log.info("Triggering build for project '{}' on cluster '{}': branch='{}', repository='{}'",
                projectKey, clusterName, request.getBranch(), request.getRepository());
        
        WebhookProxyClient client = clientFactory.getClient(clusterName, projectKey);
        WebhookProxyBuildResponse response = client.triggerBuild(request, triggerSecret);
        
        if (response.isSuccess()) {
            log.info("Build triggered successfully for project '{}' on cluster '{}'", projectKey, clusterName);
        } else {
            log.warn("Build trigger returned non-success status for project '{}' on cluster '{}': {}",
                    projectKey, clusterName, response.getStatusCode());
        }
        
        return response;
    }
    
    @Override
    public WebhookProxyBuildResponse triggerBuild(String clusterName, String projectKey,
                                                  WebhookProxyBuildRequest request, String triggerSecret,
                                                  String jenkinsfilePath, String component)
            throws WebhookProxyException {
        
        log.info("Triggering build for project '{}' on cluster '{}': branch='{}', repository='{}', " +
                "jenkinsfilePath='{}', component='{}'",
                projectKey, clusterName, request.getBranch(), request.getRepository(),
                jenkinsfilePath, component);
        
        WebhookProxyClient client = clientFactory.getClient(clusterName, projectKey);
        WebhookProxyBuildResponse response = client.triggerBuild(request, triggerSecret, jenkinsfilePath, component);
        
        if (response.isSuccess()) {
            log.info("Build triggered successfully for project '{}' on cluster '{}'", projectKey, clusterName);
        } else {
            log.warn("Build trigger returned non-success status for project '{}' on cluster '{}': {}",
                    projectKey, clusterName, response.getStatusCode());
        }
        
        return response;
    }
    
    @Override
    public Set<String> getAvailableClusters() {
        return clientFactory.getAvailableClusters();
    }
    
    @Override
    public boolean hasCluster(String clusterName) {
        return clientFactory.hasCluster(clusterName);
    }
    
    @Override
    public String getWebhookProxyUrl(String clusterName, String projectKey) 
            throws WebhookProxyException.ConfigurationException {
        
        var clusterConfig = configuration.getClusters().get(clusterName);
        
        if (clusterConfig == null) {
            throw new WebhookProxyException.ConfigurationException(
                String.format("Cluster '%s' is not configured. Available clusters: %s",
                            clusterName, configuration.getClusters().keySet())
            );
        }
        
        return clusterConfig.buildWebhookProxyUrl(projectKey);
    }
}
