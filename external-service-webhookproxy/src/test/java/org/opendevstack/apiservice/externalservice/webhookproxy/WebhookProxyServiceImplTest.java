package org.opendevstack.apiservice.externalservice.webhookproxy;

import org.opendevstack.apiservice.externalservice.webhookproxy.client.WebhookProxyClient;
import org.opendevstack.apiservice.externalservice.webhookproxy.client.WebhookProxyClientFactory;
import org.opendevstack.apiservice.externalservice.webhookproxy.config.WebhookProxyConfiguration;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildRequest;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildResponse;
import org.opendevstack.apiservice.externalservice.webhookproxy.exception.WebhookProxyException;
import org.opendevstack.apiservice.externalservice.webhookproxy.service.impl.WebhookProxyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebhookProxyServiceImplTest {
    private WebhookProxyClientFactory clientFactory;
    private WebhookProxyConfiguration configuration;
    private WebhookProxyClient client;
    private WebhookProxyServiceImpl service;

    @BeforeEach
    void setUp() {
        clientFactory = mock(WebhookProxyClientFactory.class);
        configuration = mock(WebhookProxyConfiguration.class);
        client = mock(WebhookProxyClient.class);
        service = new WebhookProxyServiceImpl(clientFactory, configuration);
    }

    @Test
    void triggerBuild_success() throws Exception {
        String cluster = "cluster-a";
        String project = "example-project";
        String secret = "triggersecret";
        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
                .branch("master")
                .repository("repo")
                .project(project)
                .build();
        WebhookProxyBuildResponse response = new WebhookProxyBuildResponse(200, "ok", true, null);
        when(clientFactory.getClient(cluster, project)).thenReturn(client);
        when(client.triggerBuild(request, secret)).thenReturn(response);

        WebhookProxyBuildResponse result = service.triggerBuild(cluster, project, request, secret);
        assertEquals(200, result.getStatusCode());
        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
        verify(clientFactory).getClient(cluster, project);
        verify(client).triggerBuild(request, secret);
    }

    @Test
    void triggerBuild_withJenkinsfileAndComponent_success() throws Exception {
        String cluster = "cluster-a";
        String project = "example-project";
        String secret = "triggersecret";
        String jenkinsfilePath = "Jenkinsfile.release";
        String component = "compA";
        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
                .branch("master")
                .repository("repo")
                .project(project)
                .build();
        WebhookProxyBuildResponse response = new WebhookProxyBuildResponse(201, "created", true, null);
        when(clientFactory.getClient(cluster, project)).thenReturn(client);
        when(client.triggerBuild(request, secret, jenkinsfilePath, component)).thenReturn(response);

        WebhookProxyBuildResponse result = service.triggerBuild(cluster, project, request, secret, jenkinsfilePath, component);
        assertEquals(201, result.getStatusCode());
        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
        verify(clientFactory).getClient(cluster, project);
        verify(client).triggerBuild(request, secret, jenkinsfilePath, component);
    }

    @Test
    void triggerBuild_failure() throws Exception {
        String cluster = "cluster-a";
        String project = "example-project";
        String secret = "triggersecret";
        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
                .branch("master")
                .repository("repo")
                .project(project)
                .build();
        WebhookProxyBuildResponse response = new WebhookProxyBuildResponse(403, "forbidden", false, "Forbidden");
        when(clientFactory.getClient(cluster, project)).thenReturn(client);
        when(client.triggerBuild(request, secret)).thenReturn(response);

        WebhookProxyBuildResponse result = service.triggerBuild(cluster, project, request, secret);
        assertEquals(403, result.getStatusCode());
        assertFalse(result.isSuccess());
        assertEquals("Forbidden", result.getErrorMessage());
    }

    @Test
    void getAvailableClusters_returnsSet() {
        Set<String> clusters = Set.of("cluster-a", "cluster-b");
        when(clientFactory.getAvailableClusters()).thenReturn(clusters);
        assertEquals(clusters, service.getAvailableClusters());
    }

    @Test
    void hasCluster_returnsTrueOrFalse() {
        when(clientFactory.hasCluster("cluster-a")).thenReturn(true);
        when(clientFactory.hasCluster("cluster-c")).thenReturn(false);
        assertTrue(service.hasCluster("cluster-a"));
        assertFalse(service.hasCluster("cluster-c"));
    }

    @Test
    void getWebhookProxyUrl_success() throws Exception {
        String cluster = "cluster-a";
        String project = "example-project";
        WebhookProxyConfiguration.ClusterConfig clusterConfig = mock(WebhookProxyConfiguration.ClusterConfig.class);
        when(configuration.getClusters()).thenReturn(Map.of(cluster, clusterConfig));
        when(clusterConfig.buildWebhookProxyUrl(project)).thenReturn("https://webhook-proxy-example-project-cd.apps.cluster-a.ocp.example.com");
        String url = service.getWebhookProxyUrl(cluster, project);
        assertEquals("https://webhook-proxy-example-project-cd.apps.cluster-a.ocp.example.com", url);
    }

    @Test
    void getWebhookProxyUrl_clusterNotConfigured_throwsException() {
        String cluster = "unknown";
        String project = "example-project";
        when(configuration.getClusters()).thenReturn(Map.of());
        Exception ex = assertThrows(WebhookProxyException.ConfigurationException.class,
                () -> service.getWebhookProxyUrl(cluster, project));
        assertTrue(ex.getMessage().contains("Cluster 'unknown' is not configured"));
    }
}
