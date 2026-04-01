package org.opendevstack.apiservice.core.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.security.flow.AuthFlowResolver;
import org.opendevstack.apiservice.core.security.flow.AuthFlowValidator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
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
    private final Map<AuthType, AuthFlowValidator> validators;

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
            throw new AuthenticationCredentialsNotFoundException("JWT required");
        }

        Jwt jwt = jwtAuth.getToken();
        AuthType detectedFlow = flowResolver.resolve(jwt);

        // Verify detected flow is in the set of allowed flows for this API
        if (detectedFlow == null || !apiDef.getAuthTypes().contains(detectedFlow)) {
            throw new AccessDeniedException(
                    "Flow '" + detectedFlow + "' is not allowed for this API. Allowed: " + apiDef.getAuthTypes());
        }

        // Validate the flow specifics
        AuthFlowValidator validator = validators.get(detectedFlow);
        if (validator != null && !validator.validate(jwt)) {
            throw new AccessDeniedException("Token validation failed for flow: " + detectedFlow);
        }

        filterChain.doFilter(request, response);
    }
}