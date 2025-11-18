package org.opendevstack.apiservice.core.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FlowPropertiesTest {

    private FlowProperties flowProperties;

    @BeforeEach
    void setUp() {
        flowProperties = new FlowProperties();
    }

    @Test
    void testDefaultGlobalProperties() {
        // When
        FlowProperties.Global global = flowProperties.getGlobal();

        // Then
        assertNotNull(global);
        assertEquals("client-credentials", global.getDefaultFlow());
    }

    @Test
    void testSetAndGetGlobal() {
        // Given
        FlowProperties.Global global = new FlowProperties.Global();
        global.setDefaultFlow("authorization-code");
        global.setEnabledFlows(Arrays.asList("authorization-code", "client-credentials"));

        // When
        flowProperties.setGlobal(global);

        // Then
        assertEquals("authorization-code", flowProperties.getGlobal().getDefaultFlow());
        assertEquals(2, flowProperties.getGlobal().getEnabledFlows().size());
        assertTrue(flowProperties.getGlobal().getEnabledFlows().contains("authorization-code"));
    }

    @Test
    void testSetAndGetApis() {
        // Given
        Map<String, FlowProperties.ApiFlows> apis = new HashMap<>();
        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        apiFlows.setDefaultFlow("on-behalf-of");
        apis.put("test-api", apiFlows);

        // When
        flowProperties.setApis(apis);

        // Then
        assertNotNull(flowProperties.getApis());
        assertEquals(1, flowProperties.getApis().size());
        assertTrue(flowProperties.getApis().containsKey("test-api"));
        assertEquals("on-behalf-of", flowProperties.getApis().get("test-api").getDefaultFlow());
    }

    @Test
    void testGlobalEnabledFlows() {
        // Given
        FlowProperties.Global global = new FlowProperties.Global();
        List<String> flows = Arrays.asList("authorization-code", "client-credentials", "on-behalf-of");

        // When
        global.setEnabledFlows(flows);

        // Then
        assertEquals(3, global.getEnabledFlows().size());
        assertTrue(global.getEnabledFlows().contains("authorization-code"));
        assertTrue(global.getEnabledFlows().contains("client-credentials"));
        assertTrue(global.getEnabledFlows().contains("on-behalf-of"));
    }

    @Test
    void testGlobalDefaultFlow() {
        // Given
        FlowProperties.Global global = new FlowProperties.Global();

        // When
        global.setDefaultFlow("authorization-code");

        // Then
        assertEquals("authorization-code", global.getDefaultFlow());
    }

    @Test
    void testApiFlowsDefaultFlow() {
        // Given
        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();

        // When
        apiFlows.setDefaultFlow("client-credentials");

        // Then
        assertEquals("client-credentials", apiFlows.getDefaultFlow());
    }

    @Test
    void testApiFlowsEndpoints() {
        // Given
        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        FlowProperties.EndpointFlow endpoint1 = new FlowProperties.EndpointFlow();
        endpoint1.setPattern("/api/test");

        FlowProperties.EndpointFlow endpoint2 = new FlowProperties.EndpointFlow();
        endpoint2.setPattern("/api/admin/**");

        List<FlowProperties.EndpointFlow> endpoints = Arrays.asList(endpoint1, endpoint2);

        // When
        apiFlows.setEndpoints(endpoints);

        // Then
        assertNotNull(apiFlows.getEndpoints());
        assertEquals(2, apiFlows.getEndpoints().size());
    }

    @Test
    void testEndpointFlowPattern() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();

        // When
        endpoint.setPattern("/api/users/**");

        // Then
        assertEquals("/api/users/**", endpoint.getPattern());
    }

    @Test
    void testEndpointFlowFlows() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();
        List<String> flows = Arrays.asList("authorization-code", "on-behalf-of");

        // When
        endpoint.setFlows(flows);

        // Then
        assertEquals(2, endpoint.getFlows().size());
        assertTrue(endpoint.getFlows().contains("authorization-code"));
        assertTrue(endpoint.getFlows().contains("on-behalf-of"));
    }

    @Test
    void testEndpointFlowRoles() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();
        List<String> roles = Arrays.asList("admin", "user");

        // When
        endpoint.setRoles(roles);

        // Then
        assertEquals(2, endpoint.getRoles().size());
        assertTrue(endpoint.getRoles().contains("admin"));
        assertTrue(endpoint.getRoles().contains("user"));
    }

    @Test
    void testEndpointFlowPermitAll() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();

        // When
        endpoint.setPermitAll(true);

        // Then
        assertTrue(endpoint.isPermitAll());
    }

    @Test
    void testEndpointFlowPermitAllDefaultFalse() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();

        // Then
        assertFalse(endpoint.isPermitAll());
    }

    @Test
    void testEndpointFlowRequireActor() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();

        // When
        endpoint.setRequireActor(true);

        // Then
        assertTrue(endpoint.isRequireActor());
    }

    @Test
    void testEndpointFlowRequireActorDefaultFalse() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();

        // Then
        assertFalse(endpoint.isRequireActor());
    }

    @Test
    void testEndpointFlowRequireAuthentication() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();

        // When
        endpoint.setRequireAuthentication(false);

        // Then
        assertFalse(endpoint.isRequireAuthentication());
    }

    @Test
    void testEndpointFlowRequireAuthenticationDefaultTrue() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();

        // Then
        assertTrue(endpoint.isRequireAuthentication());
    }

    @Test
    void testEndpointFlowRequireDelegationDepth() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();

        // When
        endpoint.setRequireDelegationDepth(3);

        // Then
        assertEquals(3, endpoint.getRequireDelegationDepth());
    }

    @Test
    void testEndpointFlowRequireDelegationDepthDefaultZero() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();

        // Then
        assertEquals(0, endpoint.getRequireDelegationDepth());
    }

    @Test
    void testEndpointFlowRequiredScopes() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();
        List<String> scopes = Arrays.asList("read:data", "write:data");

        // When
        endpoint.setRequiredScopes(scopes);

        // Then
        assertEquals(2, endpoint.getRequiredScopes().size());
        assertTrue(endpoint.getRequiredScopes().contains("read:data"));
        assertTrue(endpoint.getRequiredScopes().contains("write:data"));
    }

    @Test
    void testCompleteConfigurationStructure() {
        // Given
        FlowProperties config = new FlowProperties();

        // Global configuration
        FlowProperties.Global global = new FlowProperties.Global();
        global.setDefaultFlow("client-credentials");
        global.setEnabledFlows(Arrays.asList("authorization-code", "client-credentials"));
        config.setGlobal(global);

        // API configuration
        FlowProperties.ApiFlows userApi = new FlowProperties.ApiFlows();
        userApi.setDefaultFlow("authorization-code");

        FlowProperties.EndpointFlow userEndpoint = new FlowProperties.EndpointFlow();
        userEndpoint.setPattern("/api/users/**");
        userEndpoint.setFlows(Arrays.asList("authorization-code"));
        userEndpoint.setRoles(Arrays.asList("user", "admin"));
        userEndpoint.setRequireAuthentication(true);

        userApi.setEndpoints(Arrays.asList(userEndpoint));

        Map<String, FlowProperties.ApiFlows> apis = new HashMap<>();
        apis.put("user-api", userApi);
        config.setApis(apis);

        // Then
        assertNotNull(config.getGlobal());
        assertEquals("client-credentials", config.getGlobal().getDefaultFlow());
        assertEquals(2, config.getGlobal().getEnabledFlows().size());

        assertNotNull(config.getApis());
        assertEquals(1, config.getApis().size());

        FlowProperties.ApiFlows retrievedApi = config.getApis().get("user-api");
        assertNotNull(retrievedApi);
        assertEquals("authorization-code", retrievedApi.getDefaultFlow());
        assertEquals(1, retrievedApi.getEndpoints().size());

        FlowProperties.EndpointFlow retrievedEndpoint = retrievedApi.getEndpoints().get(0);
        assertEquals("/api/users/**", retrievedEndpoint.getPattern());
        assertEquals(1, retrievedEndpoint.getFlows().size());
        assertEquals(2, retrievedEndpoint.getRoles().size());
        assertTrue(retrievedEndpoint.isRequireAuthentication());
    }

    @Test
    void testMultipleApisConfiguration() {
        // Given
        FlowProperties config = new FlowProperties();

        FlowProperties.ApiFlows api1 = new FlowProperties.ApiFlows();
        api1.setDefaultFlow("authorization-code");

        FlowProperties.ApiFlows api2 = new FlowProperties.ApiFlows();
        api2.setDefaultFlow("client-credentials");

        Map<String, FlowProperties.ApiFlows> apis = new HashMap<>();
        apis.put("api1", api1);
        apis.put("api2", api2);

        // When
        config.setApis(apis);

        // Then
        assertEquals(2, config.getApis().size());
        assertEquals("authorization-code", config.getApis().get("api1").getDefaultFlow());
        assertEquals("client-credentials", config.getApis().get("api2").getDefaultFlow());
    }

    @Test
    void testEndpointFlowWithAllProperties() {
        // Given
        FlowProperties.EndpointFlow endpoint = new FlowProperties.EndpointFlow();

        // When
        endpoint.setPattern("/api/secure/**");
        endpoint.setFlows(Arrays.asList("on-behalf-of"));
        endpoint.setRoles(Arrays.asList("admin"));
        endpoint.setPermitAll(false);
        endpoint.setRequireActor(true);
        endpoint.setRequireAuthentication(true);
        endpoint.setRequireDelegationDepth(2);
        endpoint.setRequiredScopes(Arrays.asList("admin:all"));

        // Then
        assertEquals("/api/secure/**", endpoint.getPattern());
        assertEquals(1, endpoint.getFlows().size());
        assertEquals("on-behalf-of", endpoint.getFlows().get(0));
        assertEquals(1, endpoint.getRoles().size());
        assertEquals("admin", endpoint.getRoles().get(0));
        assertFalse(endpoint.isPermitAll());
        assertTrue(endpoint.isRequireActor());
        assertTrue(endpoint.isRequireAuthentication());
        assertEquals(2, endpoint.getRequireDelegationDepth());
        assertEquals(1, endpoint.getRequiredScopes().size());
        assertEquals("admin:all", endpoint.getRequiredScopes().get(0));
    }
}
