package org.opendevstack.apiservice.externalservice.ocp.service;

import org.opendevstack.apiservice.externalservice.ocp.client.OpenshiftApiClient;
import org.opendevstack.apiservice.externalservice.ocp.client.OpenshiftApiClientFactory;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import org.opendevstack.apiservice.externalservice.ocp.service.impl.OpenshiftServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OpenshiftService
 */
@ExtendWith(MockitoExtension.class)
class OpenshiftServiceTest {

    @Mock
    private OpenshiftApiClientFactory clientFactory;

    @Mock
    private OpenshiftApiClient apiClient;

    private OpenshiftService openshiftService;

    @BeforeEach
    void setUp() {
        openshiftService = new OpenshiftServiceImpl(clientFactory);
    }

    @Test
    void testGetSecret_Success() throws OpenshiftException {
        // Arrange
        String instanceName = "dev";
        String secretName = "test-secret";
        Map<String, String> expectedSecret = new HashMap<>();
        expectedSecret.put("username", "admin");
        expectedSecret.put("password", "secret123");

        when(clientFactory.getClient(instanceName)).thenReturn(apiClient);
        when(apiClient.getSecret(secretName)).thenReturn(expectedSecret);

        // Act
        Map<String, String> result = openshiftService.getSecret(instanceName, secretName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("admin", result.get("username"));
        assertEquals("secret123", result.get("password"));
        verify(clientFactory).getClient(instanceName);
        verify(apiClient).getSecret(secretName);
    }

    @Test
    void testGetSecretWithNamespace_Success() throws OpenshiftException {
        // Arrange
        String instanceName = "dev";
        String secretName = "test-secret";
        String namespace = "custom-namespace";
        Map<String, String> expectedSecret = new HashMap<>();
        expectedSecret.put("api-key", "abc123");

        when(clientFactory.getClient(instanceName)).thenReturn(apiClient);
        when(apiClient.getSecret(secretName, namespace)).thenReturn(expectedSecret);

        // Act
        Map<String, String> result = openshiftService.getSecret(instanceName, secretName, namespace);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("abc123", result.get("api-key"));
        verify(clientFactory).getClient(instanceName);
        verify(apiClient).getSecret(secretName, namespace);
    }

    @Test
    void testGetSecretValue_Success() throws OpenshiftException {
        // Arrange
        String instanceName = "dev";
        String secretName = "test-secret";
        String key = "password";
        String expectedValue = "secret123";

        when(clientFactory.getClient(instanceName)).thenReturn(apiClient);
        when(apiClient.getSecretValue(secretName, key)).thenReturn(expectedValue);

        // Act
        String result = openshiftService.getSecretValue(instanceName, secretName, key);

        // Assert
        assertEquals(expectedValue, result);
        verify(clientFactory).getClient(instanceName);
        verify(apiClient).getSecretValue(secretName, key);
    }

    @Test
    void testGetSecretValueWithNamespace_Success() throws OpenshiftException {
        // Arrange
        String instanceName = "prod";
        String secretName = "db-credentials";
        String key = "connection-string";
        String namespace = "production";
        String expectedValue = "postgresql://localhost:5432/mydb";

        when(clientFactory.getClient(instanceName)).thenReturn(apiClient);
        when(apiClient.getSecretValue(secretName, key, namespace)).thenReturn(expectedValue);

        // Act
        String result = openshiftService.getSecretValue(instanceName, secretName, key, namespace);

        // Assert
        assertEquals(expectedValue, result);
        verify(clientFactory).getClient(instanceName);
        verify(apiClient).getSecretValue(secretName, key, namespace);
    }

    @Test
    void testSecretExists_ReturnsTrue() throws OpenshiftException {
        // Arrange
        String instanceName = "dev";
        String secretName = "existing-secret";

        when(clientFactory.getClient(instanceName)).thenReturn(apiClient);
        when(apiClient.secretExists(secretName)).thenReturn(true);

        // Act
        boolean result = openshiftService.secretExists(instanceName, secretName);

        // Assert
        assertTrue(result);
        verify(clientFactory).getClient(instanceName);
        verify(apiClient).secretExists(secretName);
    }

    @Test
    void testSecretExists_ReturnsFalse() throws OpenshiftException {
        // Arrange
        String instanceName = "dev";
        String secretName = "non-existing-secret";

        when(clientFactory.getClient(instanceName)).thenReturn(apiClient);
        when(apiClient.secretExists(secretName)).thenReturn(false);

        // Act
        boolean result = openshiftService.secretExists(instanceName, secretName);

        // Assert
        assertFalse(result);
        verify(clientFactory).getClient(instanceName);
        verify(apiClient).secretExists(secretName);
    }

    @Test
    void testSecretExistsWithNamespace_ReturnsTrue() throws OpenshiftException {
        // Arrange
        String instanceName = "dev";
        String secretName = "existing-secret";
        String namespace = "custom-namespace";

        when(clientFactory.getClient(instanceName)).thenReturn(apiClient);
        when(apiClient.secretExists(secretName, namespace)).thenReturn(true);

        // Act
        boolean result = openshiftService.secretExists(instanceName, secretName, namespace);

        // Assert
        assertTrue(result);
        verify(clientFactory).getClient(instanceName);
        verify(apiClient).secretExists(secretName, namespace);
    }

    @Test
    void testSecretExists_HandlesException() throws OpenshiftException {
        // Arrange
        String instanceName = "dev";
        String secretName = "test-secret";

        when(clientFactory.getClient(instanceName)).thenThrow(new OpenshiftException("Connection failed"));

        // Act
        boolean result = openshiftService.secretExists(instanceName, secretName);

        // Assert
        assertFalse(result);
        verify(clientFactory).getClient(instanceName);
        verify(apiClient, never()).secretExists(anyString());
    }

    @Test
    void testGetAvailableInstances() {
        // Arrange
        Set<String> expectedInstances = Set.of("dev", "test", "prod");
        when(clientFactory.getAvailableInstances()).thenReturn(expectedInstances);

        // Act
        Set<String> result = openshiftService.getAvailableInstances();

        // Assert
        assertEquals(expectedInstances, result);
        assertEquals(3, result.size());
        assertTrue(result.contains("dev"));
        assertTrue(result.contains("test"));
        assertTrue(result.contains("prod"));
        verify(clientFactory).getAvailableInstances();
    }

    @Test
    void testHasInstance_ReturnsTrue() {
        // Arrange
        String instanceName = "dev";
        when(clientFactory.hasInstance(instanceName)).thenReturn(true);

        // Act
        boolean result = openshiftService.hasInstance(instanceName);

        // Assert
        assertTrue(result);
        verify(clientFactory).hasInstance(instanceName);
    }

    @Test
    void testHasInstance_ReturnsFalse() {
        // Arrange
        String instanceName = "non-existing";
        when(clientFactory.hasInstance(instanceName)).thenReturn(false);

        // Act
        boolean result = openshiftService.hasInstance(instanceName);

        // Assert
        assertFalse(result);
        verify(clientFactory).hasInstance(instanceName);
    }

    @Test
    void testGetSecret_ThrowsException() throws OpenshiftException {
        // Arrange
        String instanceName = "dev";
        String secretName = "test-secret";
        String errorMessage = "Instance not configured";

        when(clientFactory.getClient(instanceName)).thenThrow(new OpenshiftException(errorMessage));

        // Act & Assert
        OpenshiftException exception = assertThrows(
            OpenshiftException.class,
            () -> openshiftService.getSecret(instanceName, secretName)
        );

        assertEquals(errorMessage, exception.getMessage());
        verify(clientFactory).getClient(instanceName);
        verify(apiClient, never()).getSecret(anyString());
    }

    @Test
    void testGetSecretValue_ThrowsException() throws OpenshiftException {
        // Arrange
        String instanceName = "dev";
        String secretName = "test-secret";
        String key = "password";
        String errorMessage = "Key not found";

        when(clientFactory.getClient(instanceName)).thenReturn(apiClient);
        when(apiClient.getSecretValue(secretName, key)).thenThrow(new OpenshiftException(errorMessage));

        // Act & Assert
        OpenshiftException exception = assertThrows(
            OpenshiftException.class,
            () -> openshiftService.getSecretValue(instanceName, secretName, key)
        );

        assertEquals(errorMessage, exception.getMessage());
        verify(clientFactory).getClient(instanceName);
        verify(apiClient).getSecretValue(secretName, key);
    }
}
