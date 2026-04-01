package org.opendevstack.apiservice.core.security.authorization;


import jakarta.servlet.http.HttpServletRequest;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.security.jwt.AzureJwtAuthenticationConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PolicyContextFactory {

    public PolicyContext create(ApiDefinition apiDefinition, HttpServletRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        String clientId = null;
        String subject = null;
        Map<String, Object> claims = Map.of();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            clientId = AzureJwtAuthenticationConverter.extractClientId(jwt);
            subject = jwt.getClaimAsString("sub");
            claims = jwt.getClaims();
        }

        return new PolicyContext(clientId, subject, claims, apiDefinition, request);
    }
}