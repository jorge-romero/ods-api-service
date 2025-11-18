package org.opendevstack.apiservice.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Custom security expression root with additional methods
 * Provides custom security expressions like isAdmin(), isUser(), etc.
 * In Spring Security 6.x, we extend Spring's SecurityExpressionRoot and add custom methods
 */
public class CustomSecurityExpressionRoot extends org.springframework.security.access.expression.SecurityExpressionRoot {

    private final Authentication authentication;

    public CustomSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
        this.authentication = authentication;
    }

    /**
     * Check if user has all of the specified roles
     * Uses the inherited hasRole() method from Spring Security 6.x
     * Note: hasAnyRole is final in Spring Security 6.x and cannot be overridden
     */
    public boolean hasAllRoles(String... roles) {
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    public boolean isOwner() {
        // Simplified for example: owner if authenticated principal is a Jwt
        return authentication != null && authentication.getPrincipal() instanceof Jwt;
    }

    public Optional<String> getCurrentUserEmail() {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaimAsString("email"));
        }
        return Optional.empty();
    }

    public Optional<String> getCurrentUserId() {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaimAsString("sub"));
        }
        return Optional.empty();
    }

    public Optional<String> getCurrentUserName() {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaimAsString("preferred_username"));
        }
        return Optional.empty();
    }

    public boolean isAdmin() {
        return hasRole("admin") || hasRole("super-admin");
    }

    public boolean isUser() {
        return hasRole("user") || isAdmin();
    }
}
