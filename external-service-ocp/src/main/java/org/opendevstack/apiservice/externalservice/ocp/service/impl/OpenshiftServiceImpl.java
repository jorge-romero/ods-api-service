package org.opendevstack.apiservice.externalservice.ocp.service.impl;

import org.opendevstack.apiservice.externalservice.ocp.client.OpenshiftApiClient;
import org.opendevstack.apiservice.externalservice.ocp.client.OpenshiftApiClientFactory;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Implementation of OpenshiftService.
 * Uses OpenshiftApiClientFactory to obtain clients for different OpenShift instances
 * and delegates operations to the appropriate client.
 */
@Service
@Slf4j
public class OpenshiftServiceImpl implements OpenshiftService {
    
    private final OpenshiftApiClientFactory clientFactory;
    
    /**
     * Constructor with dependency injection
     * 
     * @param clientFactory Factory for creating OpenShift API clients
     */
    public OpenshiftServiceImpl(OpenshiftApiClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        log.info("OpenshiftServiceImpl initialized");
    }
    
    @Override
    public Map<String, String> getSecret(String instanceName, String secretName) throws OpenshiftException {
        log.debug("Getting secret '{}' from instance '{}'", secretName, instanceName);
        OpenshiftApiClient client = clientFactory.getClient(instanceName);
        return client.getSecret(secretName);
    }
    
    @Override
    public Map<String, String> getSecret(String instanceName, String secretName, String namespace) throws OpenshiftException {
        log.debug("Getting secret '{}' from namespace '{}' in instance '{}'", 
                  secretName, namespace, instanceName);
        OpenshiftApiClient client = clientFactory.getClient(instanceName);
        return client.getSecret(secretName, namespace);
    }
    
    @Override
    public String getSecretValue(String instanceName, String secretName, String key) throws OpenshiftException {
        log.debug("Getting secret value for key '{}' from secret '{}' in instance '{}'", 
                  key, secretName, instanceName);
        OpenshiftApiClient client = clientFactory.getClient(instanceName);
        return client.getSecretValue(secretName, key);
    }
    
    @Override
    public String getSecretValue(String instanceName, String secretName, String key, String namespace) throws OpenshiftException {
        log.debug("Getting secret value for key '{}' from secret '{}' in namespace '{}' in instance '{}'", 
                  key, secretName, namespace, instanceName);
        OpenshiftApiClient client = clientFactory.getClient(instanceName);
        return client.getSecretValue(secretName, key, namespace);
    }
    
    @Override
    public boolean secretExists(String instanceName, String secretName) {
        try {
            log.debug("Checking if secret '{}' exists in instance '{}'", secretName, instanceName);
            OpenshiftApiClient client = clientFactory.getClient(instanceName);
            return client.secretExists(secretName);
        } catch (OpenshiftException e) {
            log.error("Error checking if secret exists", e);
            return false;
        }
    }
    
    @Override
    public boolean secretExists(String instanceName, String secretName, String namespace) {
        try {
            log.debug("Checking if secret '{}' exists in namespace '{}' in instance '{}'", 
                      secretName, namespace, instanceName);
            OpenshiftApiClient client = clientFactory.getClient(instanceName);
            return client.secretExists(secretName, namespace);
        } catch (OpenshiftException e) {
            log.error("Error checking if secret exists", e);
            return false;
        }
    }
    
    @Override
    public Set<String> getAvailableInstances() {
        return clientFactory.getAvailableInstances();
    }
    
    @Override
    public boolean hasInstance(String instanceName) {
        return clientFactory.hasInstance(instanceName);
    }
}

    