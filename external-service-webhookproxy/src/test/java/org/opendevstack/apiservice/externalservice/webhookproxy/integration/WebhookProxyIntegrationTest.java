package org.opendevstack.apiservice.externalservice.webhookproxy.integration;

import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildRequest;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildResponse;
import org.opendevstack.apiservice.externalservice.webhookproxy.exception.WebhookProxyException;
import org.opendevstack.apiservice.externalservice.webhookproxy.service.WebhookProxyService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Webhook Proxy Service
 * These tests require actual webhook proxy connectivity and valid trigger secret
 * 
 * Prerequisites:
 * 1. Set environment variables:
 *    - WEBHOOK_PROXY_CLUSTER_A_CLUSTER_BASE (default: apps.cluster-a.ocp.example.com)
 *    - WEBHOOK_PROXY_TRIGGER_SECRET (required - the trigger secret for authentication)
 *    - WEBHOOK_PROXY_PROJECT_KEY (default: example-project)
 *    - WEBHOOK_PROXY_TEST_BRANCH (default: master)
 *    - WEBHOOK_PROXY_TEST_REPOSITORY (default: example-project-releasemanager)
 * 
 * 2. Ensure the webhook proxy is accessible at:
 *    https://webhook-proxy-{projectKey}-cd.{clusterBase}
 * 
 * 3. The trigger secret must match the webhook proxy configuration
 * 
 * To run these tests:
 * mvn test -Dtest=WebhookProxyIntegrationTest -Dspring.profiles.active=local
 * 
 * Or remove @Disabled annotations and run individual tests
 */
@SpringBootTest(classes = WebhookProxyIntegrationTest.TestConfiguration.class)
@ActiveProfiles("local")
@Slf4j
@Disabled("Integration tests require actual webhook proxy access - enable manually when needed")
class WebhookProxyIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "org.opendevstack.apiservice.externalservice.webhookproxy")
    static class TestConfiguration {
    }

    @Autowired
    private WebhookProxyService webhookProxyService;

    // Test configuration from environment variables
    private String clusterName;
    private String projectKey;
    private String triggerSecret;
    private String testBranch;
    private String testRepository;

    @BeforeEach
    void setUp() {
        clusterName = System.getenv().getOrDefault("WEBHOOK_PROXY_CLUSTER_NAME", "cluster-a");
        projectKey = System.getenv().getOrDefault("WEBHOOK_PROXY_PROJECT_KEY", "example-project");
        triggerSecret = System.getenv("WEBHOOK_PROXY_TRIGGER_SECRET");
        testBranch = System.getenv().getOrDefault("WEBHOOK_PROXY_TEST_BRANCH", "master");
        testRepository = System.getenv().getOrDefault("WEBHOOK_PROXY_TEST_REPOSITORY", "example-project-releasemanager");

        log.info("=".repeat(80));
        log.info("Integration Test Configuration:");
        log.info("  Cluster Name: {}", clusterName);
        log.info("  Project Key: {}", projectKey);
        log.info("  Test Branch: {}", testBranch);
        log.info("  Test Repository: {}", testRepository);
        log.info("  Trigger Secret: {}", triggerSecret != null ? "***SET***" : "NOT SET");
        log.info("=".repeat(80));
    }

    /**
     * Test 1: Verify service configuration and available clusters
     */
    @Test
    @Disabled("Remove this annotation to check service configuration")
    void testServiceConfiguration() {
        log.info("TEST: Verify service configuration");

        // Check available clusters
        Set<String> clusters = webhookProxyService.getAvailableClusters();
        
        assertNotNull(clusters, "Available clusters should not be null");
        assertFalse(clusters.isEmpty(), "Should have at least one configured cluster");
        
        log.info("✓ Available clusters: {}", clusters);
        
        // Verify cluster-a cluster is configured
        boolean hasCluster = webhookProxyService.hasCluster(clusterName);
        assertTrue(hasCluster, "Cluster '" + clusterName + "' should be configured");
        log.info("✓ Cluster '{}' is configured", clusterName);
    }

    /**
     * Test 2: Get webhook proxy URL for the project
     */
    @Test
    @Disabled("Remove this annotation to test URL construction")
    void testGetWebhookProxyUrl() throws WebhookProxyException {
        log.info("TEST: Get webhook proxy URL");

        String url = webhookProxyService.getWebhookProxyUrl(clusterName, projectKey);
        
        assertNotNull(url, "Webhook proxy URL should not be null");
        assertTrue(url.startsWith("https://"), "URL should start with https://");
        assertTrue(url.contains(projectKey), "URL should contain project key");
        assertTrue(url.contains("-cd."), "URL should contain -cd namespace suffix");
        
        log.info("✓ Webhook proxy URL: {}", url);
        
        // Verify URL format
        String expectedPattern = String.format("https://webhook-proxy-%s-cd.", projectKey);
        assertTrue(url.startsWith(expectedPattern), 
                  "URL should match pattern: " + expectedPattern);
        log.info("✓ URL matches expected pattern");
    }

    /**
     * Test 3: Trigger a build with minimal configuration
     * This test requires a valid trigger secret
     */
    @Test
    @Disabled("Remove this annotation to trigger an actual build - CAUTION: This will trigger a real build!")
    void testTriggerBuildBasic() throws WebhookProxyException {
        log.info("TEST: Trigger build with basic configuration");

        // Verify trigger secret is set
        assertNotNull(triggerSecret, 
                     "WEBHOOK_PROXY_TRIGGER_SECRET environment variable must be set");
        assertFalse(triggerSecret.trim().isEmpty(), 
                   "Trigger secret must not be empty");

        // Build request
        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
            .branch(testBranch)
            .repository(testRepository)
            .project(projectKey)
            .build();

        log.info("Triggering build:");
        log.info("  Branch: {}", request.getBranch());
        log.info("  Repository: {}", request.getRepository());
        log.info("  Project: {}", request.getProject());

        // Trigger the build
        WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
            clusterName,
            projectKey,
            request,
            triggerSecret
        );

        // Assertions
        assertNotNull(response, "Response should not be null");
        log.info("✓ Received response");
        
        log.info("Response details:");
        log.info("  Status Code: {}", response.getStatusCode());
        log.info("  Success: {}", response.isSuccess());
        log.info("  Body: {}", response.getBody());
        
        if (!response.isSuccess()) {
            log.warn("  Error Message: {}", response.getErrorMessage());
        }

        // Verify successful response
        assertTrue(response.isSuccess(), 
                  "Build trigger should be successful. Error: " + response.getErrorMessage());
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300,
                  "Status code should be 2xx");
        
        log.info("✓ Build triggered successfully!");
    }

    /**
     * Test 4: Trigger a build with environment variables
     * This test requires a valid trigger secret
     */
    @Test
    //Disabled("Remove this annotation to trigger a build with env vars - CAUTION: This will trigger a real build!")
    void testTriggerBuildWithEnvironmentVariables() throws WebhookProxyException {
        log.info("TEST: Trigger build with environment variables");

        assertNotNull(triggerSecret, 
                     "WEBHOOK_PROXY_TRIGGER_SECRET environment variable must be set");

        // Build request with environment variables
        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
            .branch(testBranch)
            .repository(testRepository)
            .project(projectKey)
            .build();
        
        // Add environment variables after building
        request.addEnv("TEST_MODE", "true");
        request.addEnv("INTEGRATION_TEST", "webhook-proxy-service");
        request.addEnv("TRIGGERED_BY", "WebhookProxyIntegrationTest");

        log.info("Triggering build with environment variables:");
        request.getEnv().forEach(env -> 
            log.info("  {}: {}", env.getName(), env.getValue())
        );

        WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
            clusterName,
            projectKey,
            request,
            triggerSecret
        );

        assertNotNull(response);
        assertTrue(response.isSuccess(), 
                  "Build trigger should be successful. Error: " + response.getErrorMessage());
        
        log.info("✓ Build with environment variables triggered successfully!");
        log.info("  Status: {}", response.getStatusCode());
    }

    /**
     * Test 5: Trigger build with custom Jenkinsfile path
     */
    @Test
    @Disabled("Remove this annotation to test custom Jenkinsfile - CAUTION: This will trigger a real build!")
    void testTriggerBuildWithCustomJenkinsfile() throws WebhookProxyException {
        log.info("TEST: Trigger build with custom Jenkinsfile");

        assertNotNull(triggerSecret, 
                     "WEBHOOK_PROXY_TRIGGER_SECRET environment variable must be set");

        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
            .branch(testBranch)
            .repository(testRepository)
            .project(projectKey)
            .build();

        String customJenkinsfilePath = "release-manager.Jenkinsfile";
        String componentName = "release-manager";

        log.info("Triggering build with custom parameters:");
        log.info("  Jenkinsfile Path: {}", customJenkinsfilePath);
        log.info("  Component: {}", componentName);

        WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
            clusterName,
            projectKey,
            request,
            triggerSecret,
            customJenkinsfilePath,
            componentName
        );

        assertNotNull(response);
        assertTrue(response.isSuccess(), 
                  "Build trigger should be successful. Error: " + response.getErrorMessage());
        
        log.info("✓ Build with custom Jenkinsfile triggered successfully!");
    }

    /**
     * Test 6: Test authentication failure with invalid trigger secret
     */
    @Test
    @Disabled("Remove this annotation to test authentication failure")
    void testAuthenticationFailure() {
        log.info("TEST: Test authentication failure with invalid trigger secret");

        String invalidSecret = "invalid-secret-12345";

        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
            .branch(testBranch)
            .repository(testRepository)
            .project(projectKey)
            .build();

        log.info("Attempting to trigger build with invalid trigger secret");

        // Should throw AuthenticationException
        assertThrows(WebhookProxyException.AuthenticationException.class, () -> {
            webhookProxyService.triggerBuild(
                clusterName,
                projectKey,
                request,
                invalidSecret
            );
        }, "Should throw AuthenticationException for invalid trigger secret");

        log.info("✓ Authentication correctly failed with invalid trigger secret");
    }

    /**
     * Test 7: Test validation errors
     */
    @Test
    void testValidationErrors() {
        log.info("TEST: Test validation errors");

        assertNotNull(triggerSecret, 
                     "WEBHOOK_PROXY_TRIGGER_SECRET environment variable must be set for this test");

        // Test missing branch
        WebhookProxyBuildRequest requestNoBranch = WebhookProxyBuildRequest.builder()
            .repository(testRepository)
            .project(projectKey)
            .build();

        assertThrows(WebhookProxyException.ValidationException.class, () -> {
            webhookProxyService.triggerBuild(clusterName, projectKey, requestNoBranch, triggerSecret);
        }, "Should throw ValidationException for missing branch");
        log.info("✓ Validation correctly failed for missing branch");

        // Test missing repository
        WebhookProxyBuildRequest requestNoRepo = WebhookProxyBuildRequest.builder()
            .branch(testBranch)
            .project(projectKey)
            .build();

        assertThrows(WebhookProxyException.ValidationException.class, () -> {
            webhookProxyService.triggerBuild(clusterName, projectKey, requestNoRepo, triggerSecret);
        }, "Should throw ValidationException for missing repository");
        log.info("✓ Validation correctly failed for missing repository");

        // Test missing project
        WebhookProxyBuildRequest requestNoProject = WebhookProxyBuildRequest.builder()
            .branch(testBranch)
            .repository(testRepository)
            .build();

        assertThrows(WebhookProxyException.ValidationException.class, () -> {
            webhookProxyService.triggerBuild(clusterName, projectKey, requestNoProject, triggerSecret);
        }, "Should throw ValidationException for missing project");
        log.info("✓ Validation correctly failed for missing project");

        // Test empty trigger secret
        WebhookProxyBuildRequest validRequest = WebhookProxyBuildRequest.builder()
            .branch(testBranch)
            .repository(testRepository)
            .project(projectKey)
            .build();

        assertThrows(WebhookProxyException.ValidationException.class, () -> {
            webhookProxyService.triggerBuild(clusterName, projectKey, validRequest, "");
        }, "Should throw ValidationException for empty trigger secret");
        log.info("✓ Validation correctly failed for empty trigger secret");

        log.info("✓ All validation tests passed");
    }

    /**
     * Test 8: Test configuration errors
     */
    @Test
    void testConfigurationErrors() {
        log.info("TEST: Test configuration errors");

        String nonExistentCluster = "non-existent-cluster";

        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
            .branch(testBranch)
            .repository(testRepository)
            .project(projectKey)
            .build();

        // Should throw ConfigurationException for non-existent cluster
        assertThrows(WebhookProxyException.ConfigurationException.class, () -> {
            webhookProxyService.triggerBuild(
                nonExistentCluster,
                projectKey,
                request,
                "some-secret"
            );
        }, "Should throw ConfigurationException for non-configured cluster");

        log.info("✓ Configuration error correctly thrown for non-existent cluster");

        // Test URL retrieval for non-existent cluster
        assertThrows(WebhookProxyException.ConfigurationException.class, () -> {
            webhookProxyService.getWebhookProxyUrl(nonExistentCluster, projectKey);
        }, "Should throw ConfigurationException when getting URL for non-configured cluster");

        log.info("✓ All configuration error tests passed");
    }

    /**
     * Test 9: Trigger build with full parameter set (environment map, dynamic branch)
     */
    @Test
    @Disabled("Remove this annotation to trigger a real build with all parameters!")
    void testTriggerBuildWithAllParameters() throws WebhookProxyException {
        log.info("TEST: Trigger build with all parameters");

        assertNotNull(triggerSecret, "WEBHOOK_PROXY_TRIGGER_SECRET environment variable must be set");

        // Example parameter values (replace with real values as needed)
        String configItem = "ODS_CONFIG_ITEM";
        String changeId = "2.0";
        String changeDescription = "ODS Change for release";
        String targetEnvironment = "DEV";
        String versionId = "2.0";
        String releaseStatusJiraIssue = "EXAMPLE-109";
        String releaseManagerBranchId = "master";

        // Environment variable names (simulate enum or constants)
        String configItemKey = "configItem";
        String changeIdKey = "changeId";
        String changeDescriptionKey = "changeDescription";
        String environmentKey = "environment";
        String versionKey = "version";
        String releaseStatusJiraIssueKey = "releaseStatusJiraIssueKey";

        // Build environment map
        java.util.Map<String, String> environmentMap = new java.util.HashMap<>();
        environmentMap.put(configItemKey, configItem);
        environmentMap.put(changeIdKey, changeId);
        environmentMap.put(changeDescriptionKey, changeDescription);
        environmentMap.put(environmentKey, targetEnvironment);
        environmentMap.put(versionKey, versionId);
        environmentMap.put(releaseStatusJiraIssueKey, releaseStatusJiraIssue);


        // Build request
        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
            .project(projectKey)
            .repository(testRepository)
            .branch(releaseManagerBranchId)
            .build();
        // Add environment variables from map
        environmentMap.forEach(request::addEnv);

        log.info("Triggering build with all parameters:");
        log.info("  Branch: {}", releaseManagerBranchId);
        log.info("  Repository: {}", testRepository);
        log.info("  Project: {}", projectKey);
        request.getEnv().forEach(env -> log.info("  ENV {}: {}", env.getName(), env.getValue()));

        WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
            clusterName,
            projectKey,
            request,
            triggerSecret
        );

        assertNotNull(response, "Response should not be null");
        log.info("✓ Received response");
        log.info("  Status Code: {}", response.getStatusCode());
        log.info("  Success: {}", response.isSuccess());
        log.info("  Body: {}", response.getBody());
        if (!response.isSuccess()) {
            log.warn("  Error Message: {}", response.getErrorMessage());
        }
        assertTrue(response.isSuccess(), "Build trigger should be successful. Error: " + response.getErrorMessage());
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300, "Status code should be 2xx");
        log.info("✓ Build triggered successfully with all parameters!");
    }


    /**
     * Test: Trigger build of a specific component with custom Jenkinsfile
     */
    @Test
    @Disabled("Remove this annotation to trigger a real build for a component!")
    void testTriggerBuildComponent() throws WebhookProxyException {
        log.info("TEST: Trigger build of a component");

        assertNotNull(triggerSecret, "WEBHOOK_PROXY_TRIGGER_SECRET environment variable must be set");

        String componentName = "flack"; // Example component name
        String customJenkinsfilePath = "Jenkinsfile"; // Example Jenkinsfile path
        String branch = "master";
        String repository = "example-project-component";

        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
            .branch(branch)
            .repository(repository)
            .project(projectKey)
            .build();

        log.info("Triggering build for component:");
        log.info("  Component: {}", componentName);
        log.info("  Jenkinsfile Path: {}", customJenkinsfilePath);
        log.info("  Branch: {}", branch);
        log.info("  Repository: {}", repository);
        log.info("  Project: {}", projectKey);

        WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
            clusterName,
            projectKey,
            request,
            triggerSecret,
            customJenkinsfilePath,
            componentName
        );

        assertNotNull(response, "Response should not be null");
        log.info("✓ Received response");
        log.info("  Status Code: {}", response.getStatusCode());
        log.info("  Success: {}", response.isSuccess());
        log.info("  Body: {}", response.getBody());
        if (!response.isSuccess()) {
            log.warn("  Error Message: {}", response.getErrorMessage());
        }
        assertTrue(response.isSuccess(), "Build trigger should be successful. Error: " + response.getErrorMessage());
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300, "Status code should be 2xx");
        log.info("✓ Build for component triggered successfully!");
    }    

    /**
     * Helper test to print current configuration
     * Useful for debugging
     */
    @Test
    @Disabled("Remove this annotation to print current configuration")
    void printCurrentConfiguration() {
        log.info("=".repeat(80));
        log.info("CURRENT WEBHOOK PROXY CONFIGURATION");
        log.info("=".repeat(80));
        
        log.info("Available Clusters: {}", webhookProxyService.getAvailableClusters());
        
        try {
            String url = webhookProxyService.getWebhookProxyUrl(clusterName, projectKey);
            log.info("Webhook Proxy URL for cluster '{}' and project '{}': {}", 
                    clusterName, projectKey, url);
        } catch (WebhookProxyException e) {
            log.error("Error getting webhook proxy URL: {}", e.getMessage());
        }
        
        log.info("Environment Variables:");
        log.info("  WEBHOOK_PROXY_CLUSTER_NAME: {}", 
                System.getenv().getOrDefault("WEBHOOK_PROXY_CLUSTER_NAME", "(not set, using default)"));
        log.info("  WEBHOOK_PROXY_PROJECT_KEY: {}", 
                System.getenv().getOrDefault("WEBHOOK_PROXY_PROJECT_KEY", "(not set, using default)"));
        log.info("  WEBHOOK_PROXY_TRIGGER_SECRET: {}", 
                System.getenv("WEBHOOK_PROXY_TRIGGER_SECRET") != null ? "***SET***" : "NOT SET");
        log.info("  WEBHOOK_PROXY_US_TEST_CLUSTER_BASE: {}", 
                System.getenv().getOrDefault("WEBHOOK_PROXY_US_TEST_CLUSTER_BASE", "(not set, using default)"));
        log.info("  WEBHOOK_PROXY_TEST_BRANCH: {}", 
                System.getenv().getOrDefault("WEBHOOK_PROXY_TEST_BRANCH", "(not set, using default)"));
        log.info("  WEBHOOK_PROXY_TEST_REPOSITORY: {}", 
                System.getenv().getOrDefault("WEBHOOK_PROXY_TEST_REPOSITORY", "(not set, using default)"));
        
        log.info("=".repeat(80));
    }
}
