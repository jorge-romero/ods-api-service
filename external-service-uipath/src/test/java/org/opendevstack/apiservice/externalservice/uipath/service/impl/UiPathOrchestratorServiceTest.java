package org.opendevstack.apiservice.externalservice.uipath.service.impl;

import org.opendevstack.apiservice.externalservice.uipath.config.UiPathProperties;
import org.opendevstack.apiservice.externalservice.uipath.exception.UiPathException;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathAuthResponse;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathODataResponse;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItem;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UiPathOrchestratorService.
 */
@ExtendWith(MockitoExtension.class)
class UiPathOrchestratorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private UiPathProperties properties;
    private UiPathOrchestratorServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new UiPathProperties();
        properties.setHost("https://orchestrator.example.com");
        properties.setClientId("testuser");
        properties.setClientSecret("testpass");
        properties.setTenancyName("default");
        properties.setOrganizationUnitId("123");
        properties.setTimeout(30000);

        service = new UiPathOrchestratorServiceImpl(restTemplate, properties);
    }

    @Test
    void authenticate_Success() throws Exception {
        // Given
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(true);
        authResponse.setResult("test-token-12345");

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // When
        String token = service.authenticate();

        // Then
        assertNotNull(token);
        assertEquals("test-token-12345", token);
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(UiPathAuthResponse.class));
    }

    @Test
    void authenticate_Failure() {
        // Given
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(false);
        authResponse.setError("Invalid credentials");

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // When & Then
        assertThrows(UiPathException.AuthenticationException.class, () -> service.authenticate());
    }

    @Test
    void addQueueItem_Success() throws Exception {
        // Given - Auth
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(true);
        authResponse.setResult("test-token");

        when(restTemplate.postForEntity(
                eq(properties.getLoginUrl()),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // Given - Queue Item Creation
        UiPathQueueItem createdItem = new UiPathQueueItem();
        createdItem.setId(12345L);
        createdItem.setReference("TEST-001");
        createdItem.setStatus("NEW");

        when(restTemplate.postForEntity(
                eq(properties.getQueueItemsUrl()),
                any(HttpEntity.class),
                eq(UiPathQueueItem.class)
        )).thenReturn(new ResponseEntity<>(createdItem, HttpStatus.CREATED));

        // When
        Map<String, Object> content = new HashMap<>();
        content.put("Project Key", "TEST-001");

        UiPathQueueItemRequest request = UiPathQueueItemRequest.builder()
                .queueName("Q_EDP_Project_Requests")
                .reference("TEST-001")
                .specificContent(content)
                .build();

        UiPathQueueItem result = service.addQueueItem(request);

        // Then
        assertNotNull(result);
        assertEquals(12345L, result.getId());
        assertEquals("TEST-001", result.getReference());
        assertEquals("NEW", result.getStatus());
    }

    @Test
    void getQueueItemById_Success() throws Exception {
        // Given - Auth
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(true);
        authResponse.setResult("test-token");

        when(restTemplate.postForEntity(
                eq(properties.getLoginUrl()),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // Given - Queue Item Retrieval
        UiPathQueueItem queueItem = new UiPathQueueItem();
        queueItem.setId(12345L);
        queueItem.setReference("TEST-001");
        queueItem.setStatus("SUCCESSFUL");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UiPathQueueItem.class)
        )).thenReturn(new ResponseEntity<>(queueItem, HttpStatus.OK));

        // When
        UiPathQueueItem result = service.getQueueItemById(12345L);

        // Then
        assertNotNull(result);
        assertEquals(12345L, result.getId());
        assertEquals("SUCCESSFUL", result.getStatus());
        assertTrue(result.isSuccessful());
        assertTrue(result.isFinalized());
    }

    @Test
    void getQueueItemById_NotFound() throws Exception {
        // Given - Auth
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(true);
        authResponse.setResult("test-token");

        when(restTemplate.postForEntity(
                eq(properties.getLoginUrl()),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // Given - Queue Item Not Found
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UiPathQueueItem.class)
        )).thenThrow(new RestClientException("404 Not Found"));

        // When & Then
        assertThrows(UiPathException.QueueItemNotFoundException.class, 
                    () -> service.getQueueItemById(99999L));
    }

    @Test
    void getQueueItemsByReference_Success() throws Exception {
        // Given - Auth
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(true);
        authResponse.setResult("test-token");

        when(restTemplate.postForEntity(
                eq(properties.getLoginUrl()),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // Given - OData Query Response
        UiPathQueueItem item1 = new UiPathQueueItem();
        item1.setId(100L);
        item1.setReference("TEST-001");
        item1.setStatus("FAILED");

        UiPathQueueItem item2 = new UiPathQueueItem();
        item2.setId(200L);
        item2.setReference("TEST-001");
        item2.setStatus("SUCCESSFUL");

        UiPathODataResponse<UiPathQueueItem> odataResponse = new UiPathODataResponse<>();
        odataResponse.setValue(List.of(item1, item2));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(odataResponse, HttpStatus.OK));

        // When
        List<UiPathQueueItem> results = service.getQueueItemsByReference("TEST-001");

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void getLatestQueueItemByReference_Success() throws Exception {
        // Given - Auth
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(true);
        authResponse.setResult("test-token");

        when(restTemplate.postForEntity(
                eq(properties.getLoginUrl()),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // Given - Multiple items, should return the one with highest ID
        UiPathQueueItem item1 = new UiPathQueueItem();
        item1.setId(100L);
        item1.setReference("TEST-001");
        item1.setStatus("FAILED");

        UiPathQueueItem item2 = new UiPathQueueItem();
        item2.setId(200L); // This should be returned (highest ID)
        item2.setReference("TEST-001");
        item2.setStatus("SUCCESSFUL");

        UiPathODataResponse<UiPathQueueItem> odataResponse = new UiPathODataResponse<>();
        odataResponse.setValue(List.of(item1, item2));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(odataResponse, HttpStatus.OK));

        // When
        Optional<UiPathQueueItem> result = service.getLatestQueueItemByReference("TEST-001");

        // Then
        assertTrue(result.isPresent());
        assertEquals(200L, result.get().getId());
        assertEquals("SUCCESSFUL", result.get().getStatus());
    }

    @Test
    void getLatestQueueItemByReference_NotFound() throws Exception {
        // Given - Auth
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(true);
        authResponse.setResult("test-token");

        when(restTemplate.postForEntity(
                eq(properties.getLoginUrl()),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // Given - Empty response
        UiPathODataResponse<UiPathQueueItem> odataResponse = new UiPathODataResponse<>();
        odataResponse.setValue(List.of());

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(odataResponse, HttpStatus.OK));

        // When
        Optional<UiPathQueueItem> result = service.getLatestQueueItemByReference("NONEXISTENT");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void hasQueueItemFinalized_Success() throws Exception {
        // Given - Auth
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(true);
        authResponse.setResult("test-token");

        when(restTemplate.postForEntity(
                eq(properties.getLoginUrl()),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // Given - Finalized item
        UiPathQueueItem item = new UiPathQueueItem();
        item.setId(200L);
        item.setReference("TEST-001");
        item.setStatus("SUCCESSFUL");

        UiPathODataResponse<UiPathQueueItem> odataResponse = new UiPathODataResponse<>();
        odataResponse.setValue(List.of(item));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(odataResponse, HttpStatus.OK));

        // When
        boolean finalized = service.hasQueueItemFinalized("TEST-001");

        // Then
        assertTrue(finalized);
    }

    @Test
    void hasQueueItemFinalized_StillProcessing() throws Exception {
        // Given - Auth
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(true);
        authResponse.setResult("test-token");

        when(restTemplate.postForEntity(
                eq(properties.getLoginUrl()),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // Given - Item still in progress
        UiPathQueueItem item = new UiPathQueueItem();
        item.setId(200L);
        item.setReference("TEST-001");
        item.setStatus("IN_PROGRESS");

        UiPathODataResponse<UiPathQueueItem> odataResponse = new UiPathODataResponse<>();
        odataResponse.setValue(List.of(item));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(odataResponse, HttpStatus.OK));

        // When
        boolean finalized = service.hasQueueItemFinalized("TEST-001");

        // Then
        assertFalse(finalized);
    }

    @Test
    void validateConnection_Success() throws Exception {
        // Given
        UiPathAuthResponse authResponse = new UiPathAuthResponse();
        authResponse.setSuccess(true);
        authResponse.setResult("test-token");

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // When
        boolean isValid = service.validateConnection();

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateConnection_Failure() {
        // Given
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(UiPathAuthResponse.class)
        )).thenThrow(new RestClientException("Connection refused"));

        // When
        boolean isValid = service.validateConnection();

        // Then
        assertFalse(isValid);
    }
}
