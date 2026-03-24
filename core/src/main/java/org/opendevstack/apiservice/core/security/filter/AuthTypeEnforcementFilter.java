package org.opendevstack.apiservice.core.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.security.flow.AuthFlowResolver;
import org.opendevstack.apiservice.core.security.flow.AuthFlowType;
import org.opendevstack.apiservice.core.security.flow.AuthFlowValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AuthTypeEnforcementFilter extends OncePerRequestFilter {

    public static final String API_DEFINITION_ATTR = "oas.apiDefinition";

    private final AuthFlowResolver flowResolver;
    private final Map<AuthFlowType, AuthFlowValidator> validators;

    public AuthTypeEnforcementFilter(AuthFlowResolver flowResolver,
                                     List<AuthFlowValidator> validatorList) {
        this.flowResolver = flowResolver;
        this.validators = validatorList.stream()
                .collect(Collectors.toMap(AuthFlowValidator::getSupportedFlow, Function.identity()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ApiDefinition apiDef = (ApiDefinition) request.getAttribute(API_DEFINITION_ATTR);

        // If no API definition resolved or does not require auth, continue
        if (apiDef == null || !apiDef.requiresAuth()) {
            filterChain.doFilter(request, response);
            return;
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT required");
            return;
        }

        Jwt jwt = jwtAuth.getToken();
        AuthFlowType detectedFlow = flowResolver.resolve(jwt);

        // Verify detected flow is in the set of allowed flows for this API
        AuthType detectedAuthType = toAuthType(detectedFlow);
        if (detectedAuthType == null || !apiDef.getAuthTypes().contains(detectedAuthType)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Flow '" + detectedFlow + "' is not allowed for this API. Allowed: " + apiDef.getAuthTypes());
            return;
        }

        // Validate the flow specifics
        AuthFlowValidator validator = validators.get(detectedFlow);
        if (validator != null && !validator.validate(jwt)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token validation failed for flow: " + detectedFlow);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private AuthType toAuthType(AuthFlowType flowType) {
        return switch (flowType) {
            case OBO -> AuthType.OBO;
            case CLIENT_CREDENTIALS -> AuthType.CLIENT_CREDENTIALS;
            default -> null;
        };
    }
}
