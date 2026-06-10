package org.opendevstack.apiservice.externalservice.marketplace.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.opendevstack.apiservice.core.security.obo.OboTokenService;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClientFactory;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisioningStatusUpdateRequest;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceInstanceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.client.ApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentProvisionStatus;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisionActionResponse;
import org.opendevstack.apiservice.externalservice.marketplace.service.impl.MarketplaceServiceImpl;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MarketplaceService}.
 * These tests use mocks and do not require actual Marketplace connectivity.
 */
@ExtendWith(MockitoExtension.class)
class MarketplaceServiceImplTest {

    // Generated-API endpoint paths invoked by ApiClient#invokeAPI. These mirror the
    // values produced by the OpenAPI generator so tests fail loudly if a path or
    // HTTP method changes unexpectedly.
    private static final String PATH_CATALOG_ITEM_BY_ID   = "/catalog-items/{id}";
    private static final String PATH_PROJECT_COMPONENT    = "/project/{projectKey}/component/{componentId}";
    private static final String PATH_PROVISION_ACTIONS    = "/provision-actions";
    private static final String PATH_DELETE_COMPONENT     = "/support/delete/{projectKey}/{componentId}";
    private static final String PATH_NOTIFY_PROVISIONING  = "/provision/{projectKey}/{status}";

    @Mock
    private MarketplaceApiClientFactory clientFactory;

    @Mock
    private MarketplaceApiClient marketplaceApiClient;

    @Mock
    private ApiClient apiClient;

    @Mock
    private OboTokenService oboTokenService;

    private MarketplaceService marketplaceService;

    @BeforeEach
    void setUp() {
        marketplaceService = new MarketplaceServiceImpl(clientFactory, oboTokenService);

        // Set up a fake SecurityContext so JwtUtils.getTokenValue() works
        Jwt jwt = Jwt.withTokenValue("test-jwt-assertion")
                .header("alg", "RS256")
                .claim("azp", "test-client-id")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new JwtAuthenticationToken(jwt));
        SecurityContextHolder.setContext(ctx);

        // Default OBO stub — tests that fail before OBO won't reach this
        lenient().when(oboTokenService.exchangeToken(anyString(), anyString()))
                .thenReturn("obo-test-token");

        // Stub ApiClient utility methods used by the generated ProjectApi / ServerInfoApi
        // before invokeAPI is reached. Without these, putAll(null) causes NullPointerException.
        lenient().when(apiClient.parameterToMultiValueMap(any(), anyString(), any()))
                .thenReturn(new LinkedMultiValueMap<>());
        lenient().when(apiClient.selectHeaderAccept(any()))
                .thenReturn(List.of(MediaType.APPLICATION_JSON));
        lenient().when(apiClient.selectHeaderContentType(any()))
                .thenReturn(MediaType.APPLICATION_JSON);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Stubs {@code apiClient.invokeAPI(...)} for a specific endpoint path and HTTP method.
     * Using {@code eq(path)} and {@code eq(method)} (instead of bare {@code any()} for every
     * argument) makes the test assert that the service is calling the expected generated-API
     * operation, while still being tolerant of the surrounding boilerplate arguments.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private OngoingStubbing<ResponseEntity<Object>> whenInvokeAPI(String path, HttpMethod method) {
        return (OngoingStubbing) when(apiClient.invokeAPI(
                eq(path),
                eq(method),
                any(),
                any(),
                any(),
                any(HttpHeaders.class),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(ParameterizedTypeReference.class)));
    }

    // -------------------------------------------------------------------------
    // getProjectComponent
    // -------------------------------------------------------------------------

    @Test
    void testGetProjectComponent_InstanceNotConfigured() throws MarketplaceException {
        // Arrange
        String instanceName = "nonexistent";
        String projectKey = "PROJ";
        String componentId = "test-component";

        when(clientFactory.getClient(instanceName))
                .thenThrow(new MarketplaceException("Marketplace instance 'nonexistent' is not configured"));

        // Act & Assert
        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
                marketplaceService.getProjectComponent(instanceName, projectKey, componentId));

        assertTrue(exception.getMessage().contains("not configured"));
        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testGetProjectComponent_RestClientException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_PROJECT_COMPONENT, HttpMethod.GET)
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertThrows(MarketplaceException.class, () ->
                marketplaceService.getProjectComponent(instanceName, projectKey, componentId));

        verify(clientFactory).getClient(instanceName);
        verify(marketplaceApiClient).getApiClient();
    }

    @Test
    void testGetProjectComponent_NotFound_ReturnsNull() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "UNKNOWN";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");
        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        whenInvokeAPI(PATH_PROJECT_COMPONENT, HttpMethod.GET).thenThrow(notFoundEx);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act
        ProjectComponentProvisionStatus result = marketplaceService.getProjectComponent(instanceName, projectKey, componentId);

        // Assert
        assertNull(result);
        verify(clientFactory).getClient(instanceName);
    }

    // -------------------------------------------------------------------------
    // isHealthy
    // -------------------------------------------------------------------------

    @Test
    void testIsHealthy_NoInstancesConfigured_ReturnsFalse() {
        when(clientFactory.getAvailableInstances()).thenReturn(Set.of());

        MarketplaceServiceImpl service = new MarketplaceServiceImpl(clientFactory, oboTokenService) {
            @Override
            protected boolean isProvisionerEndpointUp(MarketplaceApiClient marketplaceClient) {
                return true;
            }

            @Override
            protected boolean isCatalogEndpointUp(MarketplaceApiClient marketplaceClient) {
                return true;
            }
        };

        boolean result = service.isHealthy();

        assertFalse(result);
    }

    @Test
    void testIsHealthy_BothEndpointsUp_ReturnsTrue() throws MarketplaceException {
        when(clientFactory.getAvailableInstances()).thenReturn(Set.of("dev"));
        when(clientFactory.getDefaultInstanceName()).thenReturn("dev");
        when(clientFactory.getClient("dev")).thenReturn(marketplaceApiClient);

        MarketplaceServiceImpl service = new MarketplaceServiceImpl(clientFactory, oboTokenService) {
            @Override
            protected boolean isProvisionerEndpointUp(MarketplaceApiClient marketplaceClient) {
                return true;
            }

            @Override
            protected boolean isCatalogEndpointUp(MarketplaceApiClient marketplaceClient) {
                return true;
            }
        };

        boolean result = service.isHealthy();

        assertTrue(result);
    }

    @Test
    void testIsHealthy_ProvisionerDown_ReturnsFalse() throws MarketplaceException {
        when(clientFactory.getAvailableInstances()).thenReturn(Set.of("dev"));
        when(clientFactory.getDefaultInstanceName()).thenReturn("dev");
        when(clientFactory.getClient("dev")).thenReturn(marketplaceApiClient);

        MarketplaceServiceImpl service = new MarketplaceServiceImpl(clientFactory, oboTokenService) {
            @Override
            protected boolean isProvisionerEndpointUp(MarketplaceApiClient marketplaceClient) {
                return false;
            }
        };

        boolean result = service.isHealthy();

        assertFalse(result);
    }

    @Test
    void testIsHealthy_CatalogDown_ReturnsFalse() throws MarketplaceException {
        when(clientFactory.getAvailableInstances()).thenReturn(Set.of("dev"));
        when(clientFactory.getDefaultInstanceName()).thenReturn("dev");
        when(clientFactory.getClient("dev")).thenReturn(marketplaceApiClient);

        MarketplaceServiceImpl service = new MarketplaceServiceImpl(clientFactory, oboTokenService) {
            @Override
            protected boolean isProvisionerEndpointUp(MarketplaceApiClient marketplaceClient) {
                return true;
            }

            @Override
            protected boolean isCatalogEndpointUp(MarketplaceApiClient marketplaceClient) {
                return false;
            }
        };

        boolean result = service.isHealthy();

        assertFalse(result);
    }

    // -------------------------------------------------------------------------
    // getAvailableInstances / hasInstance
    // -------------------------------------------------------------------------

    @Test
    void testGetAvailableInstances() {
        // Arrange
        Set<String> expected = Set.of("dev", "prod");
        when(clientFactory.getAvailableInstances()).thenReturn(expected);

        // Act
        Set<String> result = marketplaceService.getAvailableInstances();

        // Assert
        assertEquals(expected, result);
        verify(clientFactory).getAvailableInstances();
    }

    @Test
    void testHasInstance_Existing_ReturnsTrue() {
        // Arrange
        when(clientFactory.hasInstance("dev")).thenReturn(true);

        // Act + Assert
        assertTrue(marketplaceService.hasInstance("dev"));
    }

    @Test
    void testHasInstance_NonExistent_ReturnsFalse() {
        // Arrange
        when(clientFactory.hasInstance("nope")).thenReturn(false);

        // Act + Assert
        assertFalse(marketplaceService.hasInstance("nope"));
    }

    // -------------------------------------------------------------------------
    // Default-instance support
    // -------------------------------------------------------------------------

    @Test
    void testGetProjectComponent_NoInstanceArg_UsesDefaultClient() throws MarketplaceException {
        String projectKey = "PROJ";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        when(clientFactory.getDefaultInstanceName()).thenReturn("default");
        when(clientFactory.getClient("default")).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        whenInvokeAPI(PATH_PROJECT_COMPONENT, HttpMethod.GET).thenReturn(ResponseEntity.ok(null));
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        ProjectComponentProvisionStatus result = marketplaceService.getProjectComponent(projectKey, componentId);

        assertNull(result);
        verify(clientFactory).getClient("default");
    }

    @Test
    void testGetProjectComponent_NullInstanceName_PropagatesFactoryException() throws MarketplaceException {
        // The factory rejects null/blank instance names; the service must surface that.
        String projectKey = "PROJ";
        String componentId = "test-component";

        when(clientFactory.getClient(null))
                .thenThrow(new MarketplaceException("Provide instance name. Available instances: []"));

        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
                marketplaceService.getProjectComponent(null, projectKey, componentId));

        assertTrue(exception.getMessage().contains("Provide instance name"));
        verify(clientFactory).getClient(null);
    }

    @Test
    void testGetProjectComponent_BlankInstanceName_PropagatesFactoryException() throws MarketplaceException {
        // The factory rejects null/blank instance names; the service must surface that.
        String projectKey = "PROJ";
        String componentId = "test-component";

        when(clientFactory.getClient(""))
                .thenThrow(new MarketplaceException("Provide instance name. Available instances: []"));

        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
                marketplaceService.getProjectComponent("", projectKey, componentId));

        assertTrue(exception.getMessage().contains("Provide instance name"));
        verify(clientFactory).getClient("");
    }

    @Test
    void testGetProjectComponent_NoInstanceArg_NotFound_ReturnsFalse() throws MarketplaceException {
        String projectKey = "ZZZNOPE";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getDefaultInstanceName()).thenReturn("default");
        when(clientFactory.getClient("default")).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        whenInvokeAPI(PATH_PROJECT_COMPONENT, HttpMethod.GET).thenThrow(notFoundEx);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        ProjectComponentProvisionStatus result = marketplaceService.getProjectComponent(projectKey, componentId);

        assertNull(result);
    }

    @Test
    void testGetProjectComponent_NoInstanceArg_RestClientException_ThrowsMarketplaceException() throws MarketplaceException {
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        when(clientFactory.getDefaultInstanceName()).thenReturn("default");
        when(clientFactory.getClient("default")).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        whenInvokeAPI(PATH_PROJECT_COMPONENT, HttpMethod.GET).thenThrow(new RestClientException("timeout"));
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        assertThrows(MarketplaceException.class, () -> marketplaceService.getProjectComponent("PROJ",
                "test-component"));
    }

    @Test
    void testGetDefaultInstance_DelegatesToFactory() throws MarketplaceException {
        when(clientFactory.getDefaultInstanceName()).thenReturn("prod");

        String result = marketplaceService.getDefaultInstance();

        assertEquals("prod", result);
        verify(clientFactory).getDefaultInstanceName();
    }

    @Test
    void testGetDefaultInstance_FactoryThrows_PropagatesException() throws MarketplaceException {
        when(clientFactory.getDefaultInstanceName())
                .thenThrow(new MarketplaceException("No Marketplace instances configured"));

        assertThrows(MarketplaceException.class, () -> marketplaceService.getDefaultInstance());
    }

    @Test
    void testProvisionProjectComponent_Conflict_ThrowsMarketplaceExceptionWithDuplicateMessage() throws MarketplaceException {
        String instanceName = "dev";
        String projectKey = "EDPC";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setProvisionerActionsBaseUrl("https://example/provision-actions");
        instanceConfig.setOboScope("api://test/scope");

        HttpClientErrorException conflictEx = HttpClientErrorException.create(
                HttpStatus.CONFLICT,
                "Conflict",
                HttpHeaders.EMPTY,
                "{\"message\":\"This component name already exists, please choose another name.\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_PROVISION_ACTIONS, HttpMethod.POST).thenThrow(conflictEx);

        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
                marketplaceService.provisionProjectComponent(instanceName, projectKey, List.of()));

        assertEquals("This component name already exists, please choose another name.", exception.getMessage());
    }

    @Test
    void testGetCatalogItem_RestClientException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String catalogItemId = "test-catalog-item-base64-string";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_CATALOG_ITEM_BY_ID, HttpMethod.GET)
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertThrows(MarketplaceException.class, () ->
                marketplaceService.getCatalogItem(instanceName, catalogItemId));

        verify(clientFactory).getClient(instanceName);
        verify(marketplaceApiClient).getApiClient();
    }

    @Test
    void testGetCatalogItem_NotFound_ReturnsNull() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String catalogItemId = "test-catalog-item-base64-string";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");
        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        whenInvokeAPI(PATH_CATALOG_ITEM_BY_ID, HttpMethod.GET).thenThrow(notFoundEx);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act
        CatalogItem result = marketplaceService.getCatalogItem(instanceName, catalogItemId);

        // Assert
        assertNull(result);
        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testGetCatalogItem_AuthError_ThrowsMarketplaceException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String catalogItemId = "test-catalog-item-base64-string";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");
        HttpClientErrorException forbiddenEx = HttpClientErrorException.create(
                HttpStatus.FORBIDDEN, "Forbidden", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        whenInvokeAPI(PATH_CATALOG_ITEM_BY_ID, HttpMethod.GET).thenThrow(forbiddenEx);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act & Assert
        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
                marketplaceService.getCatalogItem(instanceName, catalogItemId));

        assertTrue(exception.getMessage().contains("Access denied"));
    }

    @Test
    void testGetCatalogItem_DefaultInstance() throws MarketplaceException {
        // Arrange
        String catalogItemId = "test-catalog-item-base64-string";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");
        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getDefaultInstanceName()).thenReturn("default");
        when(clientFactory.getClient("default")).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        whenInvokeAPI(PATH_CATALOG_ITEM_BY_ID, HttpMethod.GET).thenThrow(notFoundEx);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act
        CatalogItem result = marketplaceService.getCatalogItem(catalogItemId);

        // Assert
        assertNull(result);
        verify(clientFactory).getClient("default");
    }

    // -------------------------------------------------------------------------
    // getProjectComponent — auth error
    // -------------------------------------------------------------------------

    @Test
    void testGetProjectComponent_AuthError_ThrowsMarketplaceException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");
        HttpClientErrorException forbiddenEx = HttpClientErrorException.create(
                HttpStatus.FORBIDDEN, "Forbidden", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_PROJECT_COMPONENT, HttpMethod.GET).thenThrow(forbiddenEx);

        // Act & Assert
        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
                marketplaceService.getProjectComponent(instanceName, projectKey, componentId));

        assertTrue(exception.getMessage().contains("Access denied"));
    }

    // -------------------------------------------------------------------------
    // provisionProjectComponent — success, auth error, REST error, default instance
    // -------------------------------------------------------------------------

    @Test
    void testProvisionProjectComponent_Success_ReturnsTrue() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setProvisionerActionsBaseUrl("https://example/provision-actions");
        instanceConfig.setOboScope("api://test/scope");

        ProvisionActionResponse mockResponse = new ProvisionActionResponse();
        mockResponse.setFailed(false);

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_PROVISION_ACTIONS, HttpMethod.POST)
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        boolean result = marketplaceService.provisionProjectComponent(instanceName, projectKey, List.of());

        // Assert
        assertTrue(result);
        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testProvisionProjectComponent_Failed_ReturnsFalse() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setProvisionerActionsBaseUrl("https://example/provision-actions");
        instanceConfig.setOboScope("api://test/scope");

        ProvisionActionResponse mockResponse = new ProvisionActionResponse();
        mockResponse.setFailed(true);

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_PROVISION_ACTIONS, HttpMethod.POST)
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        boolean result = marketplaceService.provisionProjectComponent(instanceName, projectKey, List.of());

        // Assert
        assertFalse(result);
    }

    @Test
    void testProvisionProjectComponent_RestClientException_ThrowsMarketplaceException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setProvisionerActionsBaseUrl("https://example/provision-actions");
        instanceConfig.setOboScope("api://test/scope");

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_PROVISION_ACTIONS, HttpMethod.POST)
                .thenThrow(new RestClientException("Connection refused"));

        // Act & Assert
        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
                marketplaceService.provisionProjectComponent(instanceName, projectKey, List.of()));

        assertTrue(exception.getMessage().contains("Failed to provision"));
    }

    @Test
    void testProvisionProjectComponent_AuthError_ThrowsMarketplaceException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setProvisionerActionsBaseUrl("https://example/provision-actions");
        instanceConfig.setOboScope("api://test/scope");
        HttpClientErrorException unauthorizedEx = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_PROVISION_ACTIONS, HttpMethod.POST).thenThrow(unauthorizedEx);

        // Act & Assert
        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
                marketplaceService.provisionProjectComponent(instanceName, projectKey, List.of()));

        assertTrue(exception.getMessage().contains("Access denied"));
    }

    @Test
    void testProvisionProjectComponent_DefaultInstance_Success() throws MarketplaceException {
        // Arrange
        String projectKey = "PROJ";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setProvisionerActionsBaseUrl("https://example/provision-actions");
        instanceConfig.setOboScope("api://test/scope");

        ProvisionActionResponse mockResponse = new ProvisionActionResponse();
        mockResponse.setFailed(false);

        when(clientFactory.getDefaultInstanceName()).thenReturn("default");
        when(clientFactory.getClient("default")).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_PROVISION_ACTIONS, HttpMethod.POST)
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        boolean result = marketplaceService.provisionProjectComponent(projectKey, List.of());

        // Assert
        assertTrue(result);
        verify(clientFactory).getClient("default");
    }

    @Test
    void testProvisionProjectComponent_OboScopeNotConfigured_ThrowsMarketplaceException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setProvisionerActionsBaseUrl("https://example/provision-actions");
        // OBO scope is null

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act & Assert
        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
                marketplaceService.provisionProjectComponent(instanceName, projectKey, List.of()));

        assertTrue(exception.getMessage().contains("OBO scope not configured"));
    }

    // -------------------------------------------------------------------------
    // deleteProjectComponent
    // -------------------------------------------------------------------------
    @Test
    void testDeleteProjectComponent_RestClientException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String componentId = "test-component-id";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();

        when(clientFactory.getDefaultInstanceName()).thenReturn(instanceName);
        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertThrows(MarketplaceException.class, () ->
                marketplaceService.deleteProjectComponent(instanceName, componentId));

        verify(clientFactory).getClient(instanceName);
        verify(marketplaceApiClient).getApiClient();
    }

    @Test
    void testDeleteProjectComponent_Unauthorized_ThrowsException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String componentId = "test-component-id";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        HttpClientErrorException unauthorizedException = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getDefaultInstanceName()).thenReturn(instanceName);
        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(unauthorizedException);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act
        assertThrows(MarketplaceException.class, () ->
                marketplaceService.deleteProjectComponent(instanceName, componentId));

        // Assert
        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testDeleteProjectComponent_ComponentExists_NoExceptionThrown() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String componentId = "test-component-id";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();

        when(clientFactory.getDefaultInstanceName()).thenReturn(instanceName);
        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act
        marketplaceService.deleteProjectComponent(instanceName, componentId);

        // Assert
        verify(clientFactory).getClient(instanceName);
    }

    // -------------------------------------------------------------------------
    // registerProjectComponent
    // -------------------------------------------------------------------------

    @Test
    void testRegisterProjectComponent_RestClientException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String componentId = "test-component-id";
        String catalogItemSlug = "test-catalog-item-slug";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();

        when(clientFactory.getDefaultInstanceName()).thenReturn(instanceName);
        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_NOTIFY_PROVISIONING, HttpMethod.PUT).thenThrow(new RestClientException("Connection failed"));

        // Act — should not throw
        assertThrows(MarketplaceException.class, () ->
                marketplaceService.registerProjectComponent(projectKey, componentId, catalogItemSlug, List.of()));

        // Assert
        verify(clientFactory).getClient(instanceName);
        verify(marketplaceApiClient).getApiClient();
    }

    @Test
    void testRegisterProjectComponent_Unauthorized_ThrowsException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String componentId = "test-component-id";
        String catalogItemSlug = "test-catalog-item-slug";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        HttpClientErrorException unauthorizedException = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getDefaultInstanceName()).thenReturn(instanceName);
        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_NOTIFY_PROVISIONING, HttpMethod.PUT).thenThrow(unauthorizedException);

        // Act & Assert
        assertThrows(MarketplaceException.class, () ->
                marketplaceService.registerProjectComponent(projectKey, componentId, catalogItemSlug, List.of()));

        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testRegisterProjectComponent_BuildsCorrectComponentUrl_UsingBitbucketBaseUrl() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String componentId = "my-component";
        String catalogItemSlug = "test-slug";
        String bitbucketBaseUrl = "https://bitbucket.example.com";

        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setBitbucketBaseUrl(bitbucketBaseUrl);

        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);

        when(clientFactory.getDefaultInstanceName()).thenReturn(instanceName);
        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_NOTIFY_PROVISIONING, HttpMethod.PUT).thenReturn(ResponseEntity.ok(null));

        // Act
        marketplaceService.registerProjectComponent(projectKey, componentId, catalogItemSlug, List.of());

        // Assert — capture the body passed to invokeAPI and verify its componentUrl
        verify(apiClient).invokeAPI(
                eq(PATH_NOTIFY_PROVISIONING),
                eq(HttpMethod.PUT),
                any(),
                any(),
                bodyCaptor.capture(),
                any(HttpHeaders.class),
                any(), any(), any(), any(), any(), any());

        ProvisioningStatusUpdateRequest capturedRequest =
                (ProvisioningStatusUpdateRequest) bodyCaptor.getValue();

        String expectedUrl = bitbucketBaseUrl + "/projects/" + projectKey.toUpperCase() + "/repos/" +
                projectKey.toLowerCase() + "-" + componentId + "/browse";
        assertEquals(expectedUrl, capturedRequest.getComponentUrl());
    }

    @Test
    void testRegisterProjectComponent_ComponentIsRegistered_NoExceptionThrown() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String componentId = "test-component";
        String catalogItemSlug = "test-catalog-item-slug";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();

        when(clientFactory.getDefaultInstanceName()).thenReturn(instanceName);
        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        whenInvokeAPI(PATH_NOTIFY_PROVISIONING, HttpMethod.PUT).thenReturn(ResponseEntity.ok(null));

        // Act
        marketplaceService.registerProjectComponent(projectKey, componentId, catalogItemSlug, List.of());

        // Assert
        verify(clientFactory).getClient(instanceName);
    }

}