package org.opendevstack.apiservice.externalservice.ocp.service;

import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;

import java.util.Map;
import java.util.Set;

/**
 * Service interface for interacting with OpenShift clusters.
 * Provides high-level methods to retrieve secrets and other resources from multiple OpenShift instances.
 */
public interface OpenshiftService {
    
    /**
     * Get a secret from a specific OpenShift instance
     * 
     * @param instanceName Name of the OpenShift instance
     * @param secretName Name of the secret to retrieve
     * @return Map containing the decoded secret data
     * @throws OpenshiftException if the secret cannot be retrieved
     */
    Map<String, String> getSecret(String instanceName, String secretName) throws OpenshiftException;
    
    /**
     * Get a secret from a specific namespace in a specific OpenShift instance
     * 
     * @param instanceName Name of the OpenShift instance
     * @param secretName Name of the secret to retrieve
     * @param namespace Namespace where the secret is located
     * @return Map containing the decoded secret data
     * @throws OpenshiftException if the secret cannot be retrieved
     */
    Map<String, String> getSecret(String instanceName, String secretName, String namespace) throws OpenshiftException;
    
    /**
     * Get a specific value from a secret
     * 
     * @param instanceName Name of the OpenShift instance
     * @param secretName Name of the secret
     * @param key Key within the secret data
     * @return The decoded secret value
     * @throws OpenshiftException if the secret or key cannot be retrieved
     */
    String getSecretValue(String instanceName, String secretName, String key) throws OpenshiftException;
    
    /**
     * Get a specific value from a secret in a specific namespace
     * 
     * @param instanceName Name of the OpenShift instance
     * @param secretName Name of the secret
     * @param key Key within the secret data
     * @param namespace Namespace where the secret is located
     * @return The decoded secret value
     * @throws OpenshiftException if the secret or key cannot be retrieved
     */
    String getSecretValue(String instanceName, String secretName, String key, String namespace) throws OpenshiftException;
    
    /**
     * Check if a secret exists in a specific OpenShift instance
     * 
     * @param instanceName Name of the OpenShift instance
     * @param secretName Name of the secret
     * @return true if the secret exists, false otherwise
     */
    boolean secretExists(String instanceName, String secretName);
    
    /**
     * Check if a secret exists in a specific namespace in a specific OpenShift instance
     * 
     * @param instanceName Name of the OpenShift instance
     * @param secretName Name of the secret
     * @param namespace Namespace where the secret might be located
     * @return true if the secret exists, false otherwise
     */
    boolean secretExists(String instanceName, String secretName, String namespace);
    
    /**
     * Get all available OpenShift instance names
     * 
     * @return Set of configured instance names
     */
    Set<String> getAvailableInstances();
    
    /**
     * Check if an OpenShift instance is configured
     * 
     * @param instanceName Name of the instance to check
     * @return true if configured, false otherwise
     */
    boolean hasInstance(String instanceName);
}

