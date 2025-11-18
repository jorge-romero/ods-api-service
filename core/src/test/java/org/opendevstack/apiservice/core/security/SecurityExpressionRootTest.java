package org.opendevstack.apiservice.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SecurityExpressionRootTest {

    private CustomSecurityExpressionRoot expressionRoot;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Default setup with basic authentication
        authentication = new TestingAuthenticationToken("user", "password",
                Arrays.asList(
                        new SimpleGrantedAuthority("ROLE_user"),
                        new SimpleGrantedAuthority("ROLE_admin")
                ));
        expressionRoot = new CustomSecurityExpressionRoot(authentication);
    }

    @Test
    void testHasAllRolesWithAllRolesPresent() {
        // Given - authentication has both ROLE_user and ROLE_admin

        // When
        boolean result = expressionRoot.hasAllRoles("user", "admin");

        // Then
        assertTrue(result);
    }

    @Test
    void testHasAllRolesWithSomeRolesMissing() {
        // Given - authentication has ROLE_user and ROLE_admin but not ROLE_super-admin

        // When
        boolean result = expressionRoot.hasAllRoles("user", "super-admin");

        // Then
        assertFalse(result);
    }

    @Test
    void testHasAllRolesWithSingleRole() {
        // Given - authentication has ROLE_user

        // When
        boolean result = expressionRoot.hasAllRoles("user");

        // Then
        assertTrue(result);
    }

    @Test
    void testHasAllRolesWithNoRoles() {
        // Given - authentication with no roles
        authentication = new TestingAuthenticationToken("user", "password");
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        boolean result = expressionRoot.hasAllRoles("user");

        // Then
        assertFalse(result);
    }

    @Test
    void testIsAdminWithAdminRole() {
        // Given - authentication has ROLE_admin

        // When
        boolean result = expressionRoot.isAdmin();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsAdminWithSuperAdminRole() {
        // Given - authentication has ROLE_super-admin
        authentication = new TestingAuthenticationToken("user", "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_super-admin")));
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        boolean result = expressionRoot.isAdmin();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsAdminWithoutAdminRole() {
        // Given - authentication without admin role
        authentication = new TestingAuthenticationToken("user", "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_user")));
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        boolean result = expressionRoot.isAdmin();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsUserWithUserRole() {
        // Given - authentication has ROLE_user
        authentication = new TestingAuthenticationToken("user", "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_user")));
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        boolean result = expressionRoot.isUser();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsUserWithAdminRole() {
        // Given - authentication has ROLE_admin (admin is also a user)
        authentication = new TestingAuthenticationToken("admin", "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_admin")));
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        boolean result = expressionRoot.isUser();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsUserWithoutUserOrAdminRole() {
        // Given - authentication without user or admin role
        authentication = new TestingAuthenticationToken("guest", "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_guest")));
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        boolean result = expressionRoot.isUser();

        // Then
        assertFalse(result);
    }

    @Test
    void testGetCurrentUserEmailFromJwt() {
        // Given - JWT with email claim
        Jwt jwt = createJwtWithClaims(Map.of("email", "test@example.com"));
        authentication = new TestingAuthenticationToken(jwt, null);
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        Optional<String> email = expressionRoot.getCurrentUserEmail();

        // Then
        assertTrue(email.isPresent());
        assertEquals("test@example.com", email.get());
    }

    @Test
    void testGetCurrentUserEmailWithoutJwt() {
        // Given - non-JWT authentication
        authentication = new TestingAuthenticationToken("user", "password");
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        Optional<String> email = expressionRoot.getCurrentUserEmail();

        // Then
        assertFalse(email.isPresent());
    }

    @Test
    void testGetCurrentUserEmailWhenNullAuthentication() {
        // Given - null authentication
        expressionRoot = new CustomSecurityExpressionRoot(null);

        // When
        Optional<String> email = expressionRoot.getCurrentUserEmail();

        // Then
        assertFalse(email.isPresent());
    }

    @Test
    void testGetCurrentUserIdFromJwt() {
        // Given - JWT with subject
        Jwt jwt = createJwtWithClaims(Map.of("sub", "user-123"));
        authentication = new TestingAuthenticationToken(jwt, null);
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        Optional<String> userId = expressionRoot.getCurrentUserId();

        // Then
        assertTrue(userId.isPresent());
        assertEquals("user-123", userId.get());
    }

    @Test
    void testGetCurrentUserIdWithoutJwt() {
        // Given - non-JWT authentication
        authentication = new TestingAuthenticationToken("user", "password");
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        Optional<String> userId = expressionRoot.getCurrentUserId();

        // Then
        assertFalse(userId.isPresent());
    }

    @Test
    void testGetCurrentUserNameFromJwt() {
        // Given - JWT with preferred_username claim
        Jwt jwt = createJwtWithClaims(Map.of("preferred_username", "john.doe"));
        authentication = new TestingAuthenticationToken(jwt, null);
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        Optional<String> username = expressionRoot.getCurrentUserName();

        // Then
        assertTrue(username.isPresent());
        assertEquals("john.doe", username.get());
    }

    @Test
    void testGetCurrentUserNameWithoutJwt() {
        // Given - non-JWT authentication
        authentication = new TestingAuthenticationToken("user", "password");
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        Optional<String> username = expressionRoot.getCurrentUserName();

        // Then
        assertFalse(username.isPresent());
    }

    @Test
    void testIsOwnerReturnsTrue() {
        // Given - JWT authentication
        Jwt jwt = createJwtWithClaims(Map.of("sub", "user-123"));
        authentication = new TestingAuthenticationToken(jwt, null);
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        boolean result = expressionRoot.isOwner();

        // Then
        // Note: Current implementation is simplified and returns true
        assertTrue(result);
    }

    @Test
    void testIsOwnerWithNullAuthentication() {
        // Given - null authentication
        expressionRoot = new CustomSecurityExpressionRoot(null);

        // When
        boolean result = expressionRoot.isOwner();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsOwnerWithNonJwtAuthentication() {
        // Given - non-JWT authentication
        authentication = new TestingAuthenticationToken("user", "password");
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        boolean result = expressionRoot.isOwner();

        // Then
        assertFalse(result);
    }

    @Test
    void testGetCurrentUserEmailWithMissingEmailClaim() {
        // Given - JWT without email claim
        Jwt jwt = createJwtWithClaims(Map.of("sub", "user-123"));
        authentication = new TestingAuthenticationToken(jwt, null);
        expressionRoot = new CustomSecurityExpressionRoot(authentication);

        // When
        Optional<String> email = expressionRoot.getCurrentUserEmail();

        // Then
        assertFalse(email.isPresent());
    }

    // Helper methods

    private Jwt createJwtWithClaims(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));

        // Add subject if present in claims
        if (claims.containsKey("sub")) {
            builder.subject((String) claims.get("sub"));
        } else {
            builder.subject("test-user");
        }

        return builder.claims(c -> c.putAll(claims)).build();
    }
}
