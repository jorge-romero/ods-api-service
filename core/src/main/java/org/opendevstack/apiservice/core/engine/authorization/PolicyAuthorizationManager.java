package org.opendevstack.apiservice.core.engine.authorization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.engine.filter.CachedBodyHttpServletRequest;
import org.opendevstack.apiservice.core.security.filter.AuthTypeEnforcementFilter;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Spring Security AuthorizationManager that delegates authorization decisions
 * to the policy engine. This replaces the former PolicyEnforcementFilter,
 * integrating policy evaluation directly into the Spring Security authorization
 * pipeline as recommended by Spring Security 6.x.
 */
@Component
public class PolicyAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final PolicyEngine policyEngine;
    private final PolicyCacheService policyCacheService;
    private final PolicyContextFactory contextFactory;
    private final ObjectMapper objectMapper;

    public PolicyAuthorizationManager(PolicyEngine policyEngine,
                                      PolicyCacheService policyCacheService,
                                      PolicyContextFactory contextFactory,
                                      ObjectMapper objectMapper) {
        this.policyEngine = policyEngine;
        this.policyCacheService = policyCacheService;
        this.contextFactory = contextFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public org.springframework.security.authorization.AuthorizationDecision check(
            Supplier<Authentication> authentication,
            RequestAuthorizationContext context) {

        HttpServletRequest request = context.getRequest();

        ApiDefinition apiDef = (ApiDefinition) request.getAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR);

        // No API definition resolved or API is public: allow
        if (apiDef == null || apiDef.isPublic()) {
            return new org.springframework.security.authorization.AuthorizationDecision(true);
        }

        PolicyContext policyContext = contextFactory.create(apiDef, request);

        // Enrich context with request body if it has been cached
        if (request instanceof CachedBodyHttpServletRequest cached && cached.getBody().length > 0) {
            try {
                Map<String, Object> body = objectMapper.readValue(cached.getBody(), new TypeReference<>() {});
                policyContext.withRequestBody(body);
            } catch (Exception ignored) {
                // body is not JSON (GET, form-data, etc.) — safe to skip
            }
        }

        List<PolicyRule> rules = policyCacheService.getPolicies(apiDef.getId(), policyContext.getClientId());

        AuthorizationDecision decision = policyEngine.evaluate(policyContext, rules);

        return new org.springframework.security.authorization.AuthorizationDecision(
                decision != AuthorizationDecision.DENY
        );
    }
}
