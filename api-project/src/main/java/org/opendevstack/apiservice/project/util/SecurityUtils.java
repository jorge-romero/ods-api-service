package org.opendevstack.apiservice.project.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.UUID;

public class SecurityUtils {

    private SecurityUtils() {
        // to avoid instantiation
    }

    public static UUID getClientId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Jwt jwt) {
            String clientId = jwt.getClaimAsString("azp");
            if (clientId == null || clientId.isBlank()) {
                clientId = jwt.getClaimAsString("appid");
            }
            
            if (clientId == null || clientId.isBlank()) {
                throw new InvalidBearerTokenException("Client ID not found in token claims");
            }
            
            return UUID.fromString(clientId);
        } else {
            throw new InvalidBearerTokenException("Invalid authentication token");
        }
    }
}
