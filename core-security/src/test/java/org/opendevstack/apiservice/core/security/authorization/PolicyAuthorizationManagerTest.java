package org.opendevstack.apiservice.core.security.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.security.registry.ApiDefinitionResolver;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PolicyAuthorizationManagerTest {

    private PolicyEngine policyEngine;
    private PolicyService policyService;
    private PolicyContextFactory contextFactory;
    private ApiDefinitionResolver resolver;
    private PolicyAuthorizationManager manager;

    @BeforeEach
    void setUp() {
        policyEngine = mock(PolicyEngine.class);
        policyService = mock(PolicyService.class);
        contextFactory = mock(PolicyContextFactory.class);
        resolver = mock(ApiDefinitionResolver.class);
        manager = new PolicyAuthorizationManager(policyEngine, policyService, contextFactory, resolver, new ObjectMapper());
    }

    @Test
    void check_unknownRoute_denied() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/unknown");
        when(resolver.resolve(request)).thenReturn(Optional.empty());

        var decision = manager.check(() -> null, new RequestAuthorizationContext(request));

        assertFalse(decision.isGranted());
    }

    @Test
    void check_publicApi_permitted() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v0/health");
        ApiDefinition apiDef = new ApiDefinition("api-1", "Health", "/health", "v0",
                Set.of(AuthType.NONE), true, null, true);
        when(resolver.resolve(request)).thenReturn(Optional.of(apiDef));

        var decision = manager.check(() -> null, new RequestAuthorizationContext(request));

        assertTrue(decision.isGranted());
        verifyNoInteractions(policyEngine);
    }

    @Test
    void check_securedApi_policyPermits() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v0/projects");
        ApiDefinition apiDef = new ApiDefinition("api-1", "Projects", "/projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(resolver.resolve(request)).thenReturn(Optional.of(apiDef));

        PolicyContext ctx = mock(PolicyContext.class);
        when(ctx.getClientId()).thenReturn("client-a");
        when(ctx.withRequestBody(anyMap())).thenReturn(ctx);
        when(contextFactory.create(apiDef, request)).thenReturn(ctx);

        List<PolicyRule> rules = List.of(new PolicyRule(UUID.randomUUID(), "api-1", "client-a", "ALLOWED_CLIENTS", Map.of()));
        when(policyService.findPolicies("api-1", "client-a")).thenReturn(rules);
        when(policyEngine.evaluate(ctx, rules)).thenReturn(AuthorizationDecision.PERMIT);

        var decision = manager.check(() -> null, new RequestAuthorizationContext(request));

        assertTrue(decision.isGranted());
    }

    @Test
    void check_securedApi_policyDenies() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v0/projects");
        ApiDefinition apiDef = new ApiDefinition("api-1", "Projects", "/projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(resolver.resolve(request)).thenReturn(Optional.of(apiDef));

        PolicyContext ctx = mock(PolicyContext.class);
        when(ctx.getClientId()).thenReturn("client-b");
        when(ctx.withRequestBody(anyMap())).thenReturn(ctx);
        when(contextFactory.create(apiDef, request)).thenReturn(ctx);

        List<PolicyRule> rules = List.of(new PolicyRule(UUID.randomUUID(), "api-1", "client-b", "ALLOWED_CLIENTS", Map.of()));
        when(policyService.findPolicies("api-1", "client-b")).thenReturn(rules);
        when(policyEngine.evaluate(ctx, rules)).thenReturn(AuthorizationDecision.DENY);

        var decision = manager.check(() -> null, new RequestAuthorizationContext(request));

        assertFalse(decision.isGranted());
    }

    @Test
    void check_securedApi_abstainPermits() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v0/projects");
        ApiDefinition apiDef = new ApiDefinition("api-1", "Projects", "/projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(resolver.resolve(request)).thenReturn(Optional.of(apiDef));

        PolicyContext ctx = mock(PolicyContext.class);
        when(ctx.getClientId()).thenReturn("client-a");
        when(ctx.withRequestBody(anyMap())).thenReturn(ctx);
        when(contextFactory.create(apiDef, request)).thenReturn(ctx);

        when(policyService.findPolicies(anyString(), anyString())).thenReturn(List.of());
        when(policyEngine.evaluate(any(), any())).thenReturn(AuthorizationDecision.ABSTAIN);

        var decision = manager.check(() -> null, new RequestAuthorizationContext(request));

        assertTrue(decision.isGranted());
    }

    @Test
    void check_securedPostRequest_populatesRequestBodyInContext() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v0/projects");
        request.setContentType("application/json");
        String requestBody = "{" +
            "\"projectFlavor\":\"DLSS\"," +
            "\"location\":\"UNKNOWN_REGION\"" +
            "}";
        request.setContent(requestBody.getBytes(StandardCharsets.UTF_8));

        ApiDefinition apiDef = new ApiDefinition("api-1", "Projects", "/projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(resolver.resolve(request)).thenReturn(Optional.of(apiDef));

        PolicyContext ctx = mock(PolicyContext.class);
        when(ctx.getClientId()).thenReturn("client-a");
        when(ctx.withRequestBody(anyMap())).thenReturn(ctx);
        when(contextFactory.create(apiDef, request)).thenReturn(ctx);

        List<PolicyRule> rules = List.of(new PolicyRule(UUID.randomUUID(), "api-1", "client-a", "FLAVOR_RESTRICTION", Map.of()));
        when(policyService.findPolicies("api-1", "client-a")).thenReturn(rules);
        when(policyEngine.evaluate(ctx, rules)).thenReturn(AuthorizationDecision.PERMIT);

        var decision = manager.check(() -> null, new RequestAuthorizationContext(request));

        assertTrue(decision.isGranted());
    }
}
