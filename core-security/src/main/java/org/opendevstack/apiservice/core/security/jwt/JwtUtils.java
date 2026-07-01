package org.opendevstack.apiservice.core.security.jwt;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    public static List<String> extractAudiences(Jwt jwt) {
        List<String> audiences = jwt.getAudience();
        if (audiences == null || audiences.isEmpty()) {
            throw new InvalidBearerTokenException("Audience not found in token claims");
        }
        return audiences;
    }


    public static List<String> getAudiences() {
        Object principal = currentPrincipal();
        if (principal instanceof Jwt jwt) {
            return extractAudiences(jwt);
        }
        throw new InvalidBearerTokenException("Invalid authentication token");
    }

    /**
     * Determines whether the current SecurityContext JWT already targets the configured bypass
     * audience and scope, meaning the incoming token can be forwarded as-is without an OBO exchange.
     *
     * @param bypassAudience the configured bypass audience that must be present in the token {@code aud} claim
     * @param bypassScope    the configured bypass scope that must be present in the token {@code scp} claim
     * @return {@code true} only when both the bypass audience and scope match the token claims
     */
    public static boolean tokenMatchesScopeAudience(String bypassAudience, String bypassScope) {
        if (bypassAudience == null || bypassAudience.isBlank()
                || bypassScope == null || bypassScope.isBlank()) {
            return false;
        }
        Set<String> tokenAudiences = new LinkedHashSet<>(getAudiences());
        if (!tokenAudiences.contains(bypassAudience)) {
            return false;
        }
        return getScopes().contains(bypassScope);
    }

    /**
     * Extracts the delegated scopes from the current SecurityContext JWT {@code scp} claim.
     *
     * @return the set of scopes, empty when the {@code scp} claim is absent
     * @throws InvalidBearerTokenException if the principal is not a JWT
     */
    public static Set<String> getScopes() {
        Object principal = currentPrincipal();
        if (principal instanceof Jwt jwt) {
            return extractScopes(jwt);
        }
        throw new InvalidBearerTokenException("Invalid authentication token");
    }

    /**
     * Extracts the delegated scopes from the given JWT {@code scp} claim.
     * Entra ID exposes delegated permissions as a space-separated {@code scp} claim;
     * there is no dedicated accessor for it in Spring's {@code JwtClaimAccessor}.
     */
    public static Set<String> extractScopes(Jwt jwt) {
        String scp = jwt.getClaimAsString("scp");
        Set<String> scopes = new LinkedHashSet<>();
        if (scp != null && !scp.isBlank()) {
            for (String scope : scp.trim().split("\\s+")) {
                if (!scope.isBlank()) {
                    scopes.add(scope);
                }
            }
        }
        return scopes;
    }
}
