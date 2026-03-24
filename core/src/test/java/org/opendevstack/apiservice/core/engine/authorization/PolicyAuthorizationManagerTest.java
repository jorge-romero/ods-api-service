package org.opendevstack.apiservice.core.engine.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.security.filter.AuthTypeEnforcementFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PolicyAuthorizationManagerTest {

    @Test
    void requestWithoutApiDefinition_isDenied() {
        PolicyEngine policyEngine = mock(PolicyEngine.class);
        PolicyCacheService policyCacheService = mock(PolicyCacheService.class);
        PolicyContextFactory contextFactory = mock(PolicyContextFactory.class);
        PolicyAuthorizationManager manager = new PolicyAuthorizationManager(
                policyEngine,
                policyCacheService,
                contextFactory,
                new ObjectMapper());

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR)).thenReturn(null);

        var decision = manager.check(() -> null, new RequestAuthorizationContext(request));

        assertFalse(decision.isGranted());
        verifyNoInteractions(policyEngine, policyCacheService, contextFactory);
    }

    @Test
    void requestWithPublicApiDefinition_isAllowed() {
        PolicyEngine policyEngine = mock(PolicyEngine.class);
        PolicyCacheService policyCacheService = mock(PolicyCacheService.class);
        PolicyContextFactory contextFactory = mock(PolicyContextFactory.class);
        PolicyAuthorizationManager manager = new PolicyAuthorizationManager(
                policyEngine,
                policyCacheService,
                contextFactory,
                new ObjectMapper());

        ApiDefinition apiDefinition = new ApiDefinition("id", "name", "/api/test", "v1", Set.of(), true, null, true);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR)).thenReturn(apiDefinition);

        var decision = manager.check(() -> null, new RequestAuthorizationContext(request));

        assertTrue(decision.isGranted());
        verifyNoInteractions(policyEngine, policyCacheService, contextFactory);
    }

    @Test
    void nonPublicApiDefinition_delegatesToPolicyEngine() {
        PolicyEngine policyEngine = mock(PolicyEngine.class);
        PolicyCacheService policyCacheService = mock(PolicyCacheService.class);
        PolicyContextFactory contextFactory = mock(PolicyContextFactory.class);
        PolicyAuthorizationManager manager = new PolicyAuthorizationManager(
                policyEngine,
                policyCacheService,
                contextFactory,
                new ObjectMapper());

        ApiDefinition apiDefinition = new ApiDefinition("api-1", "name", "/api/test", "v1", Set.of(), false, null, true);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR)).thenReturn(apiDefinition);

        PolicyContext policyContext = mock(PolicyContext.class);
        when(policyContext.getClientId()).thenReturn("client-1");
        when(contextFactory.create(apiDefinition, request)).thenReturn(policyContext);
        when(policyCacheService.getPolicies("api-1", "client-1")).thenReturn(List.of());
        when(policyEngine.evaluate(policyContext, List.of())).thenReturn(AuthorizationDecision.PERMIT);

        var decision = manager.check(() -> null, new RequestAuthorizationContext(request));

        assertTrue(decision.isGranted());
    }
}
