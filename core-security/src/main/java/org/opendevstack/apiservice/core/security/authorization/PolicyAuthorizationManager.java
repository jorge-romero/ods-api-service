package org.opendevstack.apiservice.core.security.authorization;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.security.filter.AuthTypeEnforcementFilter;
import org.opendevstack.apiservice.core.security.registry.ApiDefinitionResolver;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Spring Security AuthorizationManager that delegates authorization decisions to the policy engine.
 */
@Component
public class PolicyAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final PolicyEngine policyEngine;
    private final PolicyService policyService;
    private final PolicyContextFactory contextFactory;
    private final ApiDefinitionResolver resolver;

    private final ObjectMapper objectMapper;

    public PolicyAuthorizationManager(PolicyEngine policyEngine,
                                      PolicyService policyService,
                                      PolicyContextFactory contextFactory,
                                      ApiDefinitionResolver resolver,
                                      ObjectMapper objectMapper) {
        this.policyEngine = policyEngine;
        this.policyService = policyService;
        this.contextFactory = contextFactory;
        this.resolver = resolver;
        this.objectMapper = objectMapper;
    }

    @Override
    public org.springframework.security.authorization.AuthorizationDecision check(
            Supplier<Authentication> authentication,
            RequestAuthorizationContext context) {

        HttpServletRequest request = context.getRequest();

        Optional<ApiDefinition> apiDef = this.resolver.resolve(request);

        if (apiDef.isPresent()) {
            request.setAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR, apiDef.get());   
        }

        // Unknown routes are denied (fail-closed). Public API definitions are allowed.
        if (apiDef.isEmpty()) {
            return new org.springframework.security.authorization.AuthorizationDecision(false);
        }

        if (apiDef.get().isPublic()) {
            return new org.springframework.security.authorization.AuthorizationDecision(true);
        }

        PolicyContext policyContext = contextFactory.create(apiDef.get(), request);
        policyContext = policyContext.withRequestBody(extractRequestBody(request));

        List<PolicyRule> rules = policyService.findPolicies(apiDef.get().getId(), policyContext.getClientId());

        AuthorizationDecision decision = policyEngine.evaluate(policyContext, rules);

        return new org.springframework.security.authorization.AuthorizationDecision(
                decision != AuthorizationDecision.DENY
        );
    }

    private Map<String, Object> extractRequestBody(HttpServletRequest request) {
        if (!isJsonWriteRequest(request)) {
            return Map.of();
        }

        try {
            byte[] bytes = request.getInputStream().readAllBytes();
            if (bytes.length == 0) {
                return Map.of();
            }
            return objectMapper.readValue(bytes, new TypeReference<>() {
            });
        } catch (IOException ex) {
            return Map.of();
        }
    }

    private boolean isJsonWriteRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method)
                && !"PUT".equalsIgnoreCase(method)
                && !"PATCH".equalsIgnoreCase(method)) {
            return false;
        }

        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("application/json");
    }
}
