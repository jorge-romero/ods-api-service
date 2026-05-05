package org.opendevstack.apiservice.core.security.jwt;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.UUID;

public final class JwtUtils {

    private JwtUtils() {
    }

    /**
     * Extracts the raw JWT token value from the current SecurityContext.
     *
     * @return the JWT token string
     * @throws InvalidBearerTokenException if the principal is not a JWT
     */
    public static String getTokenValue() {
        Object principal = currentPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getTokenValue();
        }
        throw new InvalidBearerTokenException("Invalid authentication token");
    }

    private static Object currentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new InvalidBearerTokenException("No authentication present in security context");
        }
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new InvalidBearerTokenException("No principal present in security context");
        }
        return principal;
    }

    /**
     * Extracts the Azure AD client ID from the current SecurityContext JWT.
     * Checks the {@code azp} claim first, then falls back to {@code appid}.
     *
     * @return the client ID as UUID
     * @throws InvalidBearerTokenException if the principal is not a JWT or no client ID claim is found
     */
    public static UUID getClientId() {
        Object principal = currentPrincipal();
        if (principal instanceof Jwt jwt) {
            return extractClientId(jwt);
        }
        throw new InvalidBearerTokenException("Invalid authentication token");
    }

    /**
     * Extracts the Azure AD client ID from the given JWT.
     */
    public static UUID extractClientId(Jwt jwt) {
        String clientId = jwt.getClaimAsString("azp");
        if (clientId == null || clientId.isBlank()) {
            clientId = jwt.getClaimAsString("appid");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new InvalidBearerTokenException("Client ID not found in token claims");
        }
        return UUID.fromString(clientId);
    }
}
