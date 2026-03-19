package org.opendevstack.apiservice.core.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CustomRoleConverterTest {

    private EntraIdRoleConverter converter;

    @BeforeEach
    void setUp() {
        converter = new EntraIdRoleConverter();
    }

    @Test
    void testConvertWithEntraRoles() {
        Jwt jwt = createJwtWithClaims(Map.of("roles", List.of("admin", "user")));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_admin"));
        assertTrue(containsAuthority(authorities, "ROLE_user"));
    }

    @Test
    void testConvertWithEntraScopes() {
        Jwt jwt = createJwtWithClaims(Map.of("scp", "read write"));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(containsAuthority(authorities, "SCOPE_read"));
        assertTrue(containsAuthority(authorities, "SCOPE_write"));
    }

    @Test
    void testConvertWithRolesAndScopes() {
        Jwt jwt = createJwtWithClaims(Map.of(
                "roles", List.of("admin"),
                "scp", "read write"
        ));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(3, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_admin"));
        assertTrue(containsAuthority(authorities, "SCOPE_read"));
        assertTrue(containsAuthority(authorities, "SCOPE_write"));
    }

    @Test
    void testConvertWithNullJwt() {
        Collection<GrantedAuthority> authorities = converter.convert(null);

        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testConvertWithNoRoleOrScopeClaims() {
        Jwt jwt = createJwtWithClaims(Map.of("sub", "test-user"));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testConvertWithEmptyRolesAndBlankScopes() {
        Jwt jwt = createJwtWithClaims(Map.of(
                "roles", Collections.emptyList(),
                "scp", "   "
        ));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testRolePrefixIsAdded() {
        Jwt jwt = createJwtWithClaims(Map.of("roles", List.of("test-role")));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        GrantedAuthority authority = authorities.iterator().next();
        assertTrue(authority.getAuthority().startsWith("ROLE_"));
        assertEquals("ROLE_test-role", authority.getAuthority());
    }

    @Test
    void testScopePrefixIsAdded() {
        Jwt jwt = createJwtWithClaims(Map.of("scp", "custom-scope"));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        GrantedAuthority authority = authorities.iterator().next();
        assertTrue(authority.getAuthority().startsWith("SCOPE_"));
        assertEquals("SCOPE_custom-scope", authority.getAuthority());
    }

    @Test
    void testConvertIgnoresNonStringRolesEntries() {
        Jwt jwt = createJwtWithClaims(Map.of("roles", Arrays.asList("admin", 123, "user", null)));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_admin"));
        assertTrue(containsAuthority(authorities, "ROLE_user"));
    }

    @Test
    void testConvertIgnoresBlankRoleValues() {
        Jwt jwt = createJwtWithClaims(Map.of("roles", List.of("admin", " ", "user")));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_admin"));
        assertTrue(containsAuthority(authorities, "ROLE_user"));
    }

    @Test
    void testConvertIgnoresExtraSpacesInScopes() {
        Jwt jwt = createJwtWithClaims(Map.of("scp", "read   write  "));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(containsAuthority(authorities, "SCOPE_read"));
        assertTrue(containsAuthority(authorities, "SCOPE_write"));
    }

    @Test
    void testConvertIgnoresKeycloakClaims() {
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("legacy-admin"));

        Map<String, Object> clientRoles = new HashMap<>();
        clientRoles.put("roles", List.of("legacy-manager"));
        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("my-client", clientRoles);

        Jwt jwt = createJwtWithClaims(Map.of(
                "realm_access", realmAccess,
                "resource_access", resourceAccess
        ));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    // Helper methods

    private Jwt createJwtWithClaims(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claims(c -> c.putAll(claims))
                .build();
    }

    private boolean containsAuthority(Collection<GrantedAuthority> authorities, String authority) {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
