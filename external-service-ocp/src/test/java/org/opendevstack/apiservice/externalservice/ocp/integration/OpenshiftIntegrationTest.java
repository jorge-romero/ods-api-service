package org.opendevstack.apiservice.externalservice.ocp.integration;

import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OpenShift Service
 * These tests require actual OpenShift cluster connectivity
 * 
 * To run these tests:
 * 1. Ensure you have proper OpenShift configuration in application-local.yaml or environment variables
 * 2. Make sure the OpenShift token has access to the namespace
 * 3. Remove @Disabled annotation from the test you want to run
 * 4. Run with: mvn test -Dtest=OpenshiftIntegrationTest
 */
@SpringBootTest(classes = OpenshiftIntegrationTest.TestConfiguration.class)
@ActiveProfiles("local")
@Slf4j
@Disabled("Integration tests require actual OpenShift cluster access - enable manually when needed")
class OpenshiftIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "org.opendevstack.apiservice.externalservice.ocp")
    static class TestConfiguration {
    }

    @Autowired
    private OpenshiftService openshiftService;

    /**
     * Test to retrieve a specific secret from the example-project-cd namespace in cluster-a cluster
     * 
     * Before running this test:
     * 1. Ensure OPENSHIFT_CLUSTER_A_API_URL environment variable is set
     * 2. Ensure OPENSHIFT_CLUSTER_A_TOKEN environment variable is set with a valid token
     * 3. Verify the token has access to the example-project-cd namespace
     * 4. Remove @Disabled annotation
     */
    @Test
    @Disabled("Remove this annotation to run the test")
    void testGetTriggerSecretFromRompiCdNamespace() {
        // Configuration
        String instanceName = "cluster-a";
        String namespace = "example-project-cd";
        String secretName = "webhook-proxy";
        
        log.info("Attempting to retrieve secret '{}' from namespace '{}' in instance '{}'", 
                 secretName, namespace, instanceName);
        
        try {
            // Verify instance is configured
            assertTrue(openshiftService.hasInstance(instanceName), 
                      "Instance '" + instanceName + "' is not configured");
            log.info("✓ Instance '{}' is configured", instanceName);
            
            // Check if secret exists
            boolean exists = openshiftService.secretExists(instanceName, secretName, namespace);
            assertTrue(exists, 
                      "Secret '" + secretName + "' does not exist in namespace '" + namespace + "'");
            log.info("✓ Secret '{}' exists in namespace '{}'", secretName, namespace);
            
            // Get the entire secret
            Map<String, String> secret = openshiftService.getSecret(instanceName, secretName, namespace);
            
            // Assertions
            assertNotNull(secret, "Secret should not be null");
            assertFalse(secret.isEmpty(), "Secret should not be empty");
            
            // Log secret keys (not values for security)
            log.info("✓ Successfully retrieved secret with {} keys", secret.size());
            log.info("Secret keys: {}", secret.keySet());
            
            // Example: Get a specific value from the secret
            if (!secret.isEmpty()) {
                String firstKey = secret.keySet().iterator().next();
                String value = openshiftService.getSecretValue(instanceName, secretName, firstKey, namespace);
                assertNotNull(value, "Secret value should not be null");
                log.info("✓ Successfully retrieved value for key '{}'", firstKey);
            }
            
        } catch (OpenshiftException e) {
            log.error("Failed to retrieve secret", e);
            fail("Should be able to retrieve the secret: " + e.getMessage());
        }
    }
    
    /**
     * Test to get a specific value from the webhook-proxy
     * Customize this test based on the keys you know exist in the secret
     */
    @Test
    @Disabled("Remove this annotation and customize with actual key names")
    void testGetSpecificValueFromTriggerSecret() {
        String instanceName = "cluster-a";
        String namespace = "example-project-cd";
        String secretName = "webhook-proxy";
        String keyName = "trigger-secret"; // Replace with actual key name
        
        log.info("Attempting to retrieve key '{}' from secret '{}'", keyName, secretName);
        
        try {
            String value = openshiftService.getSecretValue(instanceName, secretName, keyName, namespace);
            
            assertNotNull(value, "Secret value should not be null");
            assertFalse(value.isEmpty(), "Secret value should not be empty");
            
            log.info("✓ Successfully retrieved value for key '{}'", keyName);
            log.info("Value length: {} characters", value.length());
            
        } catch (OpenshiftException e) {
            log.error("Failed to retrieve secret value", e);
            fail("Should be able to retrieve the secret value: " + e.getMessage());
        }
    }
    
    /**
     * Helper test to list all available instances
     */
    @Test
    @Disabled("Remove this annotation to check configured instances")
    void testListAvailableInstances() {
        log.info("Listing available OpenShift instances");
        
        var instances = openshiftService.getAvailableInstances();
        
        assertNotNull(instances, "Available instances should not be null");
        assertFalse(instances.isEmpty(), "Should have at least one configured instance");
        
        log.info("Available instances: {}", instances);
        instances.forEach(instance -> {
            log.info("  - {}", instance);
        });
        
        assertTrue(instances.contains("cluster-a"), 
                  "cluster-a instance should be configured");
    }
    
    /**
     * Test to verify all keys in the webhook-proxy
     * Useful to discover what's inside the secret
     */
    @Test
    @Disabled("Remove this annotation to discover secret contents")
    void testDiscoverTriggerSecretContents() {
        String instanceName = "cluster-a";
        String namespace = "example-project-cd";
        String secretName = "webhook-proxy";
        
        log.info("Discovering contents of secret '{}'", secretName);
        
        try {
            Map<String, String> secret = openshiftService.getSecret(instanceName, secretName, namespace);
            
            log.info("Secret '{}' contains the following keys:", secretName);
            secret.forEach((key, value) -> {
                log.info("  - Key: '{}', Value length: {} characters", key, value.length());
            });
            
            // Print sanitized info (first and last 2 chars only)
            secret.forEach((key, value) -> {
                String sanitized = sanitizeValue(value);
                log.info("  - {}: {}", key, sanitized);
            });
            
        } catch (OpenshiftException e) {
            log.error("Failed to discover secret contents", e);
            fail("Should be able to retrieve the secret: " + e.getMessage());
        }
    }
    
    /**
     * Sanitize sensitive values for logging
     */
    private String sanitizeValue(String value) {
        if (value == null || value.length() < 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
