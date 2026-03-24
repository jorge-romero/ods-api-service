package org.opendevstack.apiservice.core.engine.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.engine.authorization.PolicyCacheService;
import org.opendevstack.apiservice.core.engine.authorization.PolicyContextFactory;
import org.opendevstack.apiservice.core.engine.authorization.PolicyEngine;
import org.opendevstack.apiservice.core.security.filter.AuthTypeEnforcementFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Order(3)
public class PolicyEnforcementFilter extends OncePerRequestFilter {

    public static final String POLICY_PERMIT_ATTR = "policy.permit";

    private final PolicyEngine policyEngine;
    private final PolicyCacheService policyCacheService;
    private final PolicyContextFactory contextFactory;
    private final ObjectMapper objectMapper;

    public PolicyEnforcementFilter(PolicyEngine policyEngine,
                                   PolicyCacheService policyCacheService,
                                   PolicyContextFactory contextFactory,
                                   ObjectMapper objectMapper) {
        this.policyEngine = policyEngine;
        this.policyCacheService = policyCacheService;
        this.contextFactory = contextFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ApiDefinition apiDef = (ApiDefinition) request.getAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR);

        if (apiDef == null || apiDef.isPublic()) {
            filterChain.doFilter(request, response);
            return;
        }

        PolicyContext context = contextFactory.create(apiDef, request);
        String clientId = context.getClientId();

        if (request instanceof CachedBodyHttpServletRequest cached && cached.getBody().length > 0) {
            try {
                Map<String, Object> body = objectMapper.readValue(cached.getBody(), new TypeReference<>() {});
                context.withRequestBody(body);
            } catch (Exception ignored) {
                // body is not JSON (GET, form-data, etc.)
            }
        }

        List<PolicyRule> rules = policyCacheService.getPolicies(apiDef.getId(), clientId);

        AuthorizationDecision decision = policyEngine.evaluate(context, rules);

        if (decision == AuthorizationDecision.DENY) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied by policy");
            return;
        }

        request.setAttribute(POLICY_PERMIT_ATTR, true);
        filterChain.doFilter(request, response);
    }
}
