package org.opendevstack.apiservice.externalservice.ocp.example;

import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Example usage of OpenshiftService
 * This class demonstrates how to use the OpenShift service to retrieve secrets
 */
@Component
@Slf4j
public class OpenshiftServiceUsageExample {
    
    private final OpenshiftService openshiftService;
    
    public OpenshiftServiceUsageExample(OpenshiftService openshiftService) {
        this.openshiftService = openshiftService;
    }
    
    /**
     * Example 1: Get entire secret from default namespace
     */
    public void getEntireSecret() {
        try {
            Map<String, String> secret = openshiftService.getSecret("dev", "database-credentials");
            
            log.info("Retrieved secret with {} keys", secret.size());
            secret.forEach((key, value) -> 
                log.debug("Key: {}, Value: {}", key, maskValue(value))
            );
            
        } catch (OpenshiftException e) {
            log.error("Failed to retrieve secret", e);
        }
    }
    
    /**
     * Example 2: Get specific value from a secret
     */
    public String getDatabasePassword(String environment) {
        try {
            return openshiftService.getSecretValue(environment, "database-credentials", "password");
        } catch (OpenshiftException e) {
            log.error("Failed to retrieve database password from environment: {}", environment, e);
            return null;
        }
    }
    
    /**
     * Example 3: Get secret from specific namespace
     */
    public Map<String, String> getSecretFromNamespace(String instanceName, String namespace, String secretName) {
        try {
            return openshiftService.getSecret(instanceName, secretName, namespace);
        } catch (OpenshiftException e) {
            log.error("Failed to retrieve secret {} from namespace {} in instance {}", 
                     secretName, namespace, instanceName, e);
            return Map.of();
        }
    }
    
    /**
     * Example 4: Check if secret exists before retrieving
     */
    public String getSecretValueSafely(String instanceName, String secretName, String key) {
        if (openshiftService.secretExists(instanceName, secretName)) {
            try {
                return openshiftService.getSecretValue(instanceName, secretName, key);
            } catch (OpenshiftException e) {
                log.error("Failed to retrieve secret value", e);
                return null;
            }
        } else {
            log.warn("Secret {} does not exist in instance {}", secretName, instanceName);
            return null;
        }
    }
    
    /**
     * Example 5: Work with multiple environments
     */
    public void compareSecretsAcrossEnvironments() {
        try {
            // Get secret from dev
            Map<String, String> devSecret = openshiftService.getSecret("dev", "app-config");
            log.info("Dev environment has {} configuration keys", devSecret.size());
            
            // Get secret from prod
            Map<String, String> prodSecret = openshiftService.getSecret("prod", "app-config");
            log.info("Prod environment has {} configuration keys", prodSecret.size());
            
            // Compare
            devSecret.keySet().forEach(key -> {
                if (!prodSecret.containsKey(key)) {
                    log.warn("Key {} exists in dev but not in prod", key);
                }
            });
            
        } catch (OpenshiftException e) {
            log.error("Failed to compare secrets", e);
        }
    }
    
    /**
     * Example 6: List all available instances
     */
    public void listAvailableInstances() {
        log.info("Available OpenShift instances: {}", openshiftService.getAvailableInstances());
        
        openshiftService.getAvailableInstances().forEach(instance -> {
            log.info("Instance '{}' is configured", instance);
        });
    }
    
    /**
     * Example 7: Conditional logic based on instance availability
     */
    public String getConfigurationValue(String key) {
        String instanceName;
        
        if (openshiftService.hasInstance("prod")) {
            instanceName = "prod";
        } else if (openshiftService.hasInstance("test")) {
            instanceName = "test";
        } else {
            instanceName = "dev";
        }
        
        try {
            return openshiftService.getSecretValue(instanceName, "app-config", key);
        } catch (OpenshiftException e) {
            log.error("Failed to get configuration value for key: {}", key, e);
            return null;
        }
    }
    
    /**
     * Mask sensitive values for logging
     */
    private String maskValue(String value) {
        if (value == null || value.length() < 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
