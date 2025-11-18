package org.opendevstack.apiservice.core.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CustomRoleConverterTest {

    private CustomRoleConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CustomRoleConverter();
    }

    @Test
    void testConvertWithRealmRoles() {
        // Given
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("admin", "user"));

        Jwt jwt = createJwtWithClaims(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_admin"));
        assertTrue(containsAuthority(authorities, "ROLE_user"));
    }

    @Test
    void testConvertWithResourceRoles() {
        // Given
        Map<String, Object> clientRoles = new HashMap<>();
        clientRoles.put("roles", Arrays.asList("manager", "viewer"));

        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("my-client", clientRoles);

        Jwt jwt = createJwtWithClaims(Map.of("resource_access", resourceAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_manager"));
        assertTrue(containsAuthority(authorities, "ROLE_viewer"));
    }

    @Test
    void testConvertWithBothRealmAndResourceRoles() {
        // Given
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("admin"));

        Map<String, Object> clientRoles = new HashMap<>();
        clientRoles.put("roles", Arrays.asList("manager"));

        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("my-client", clientRoles);

        Map<String, Object> claims = new HashMap<>();
        claims.put("realm_access", realmAccess);
        claims.put("resource_access", resourceAccess);

        Jwt jwt = createJwtWithClaims(claims);

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_admin"));
        assertTrue(containsAuthority(authorities, "ROLE_manager"));
    }

    @Test
    void testConvertWithMultipleResourceClients() {
        // Given
        Map<String, Object> client1Roles = new HashMap<>();
        client1Roles.put("roles", Arrays.asList("role1", "role2"));

        Map<String, Object> client2Roles = new HashMap<>();
        client2Roles.put("roles", Arrays.asList("role3"));

        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("client1", client1Roles);
        resourceAccess.put("client2", client2Roles);

        Jwt jwt = createJwtWithClaims(Map.of("resource_access", resourceAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertEquals(3, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_role1"));
        assertTrue(containsAuthority(authorities, "ROLE_role2"));
        assertTrue(containsAuthority(authorities, "ROLE_role3"));
    }

    @Test
    void testConvertWithNullRealmAccess() {
        // Given
        Jwt jwt = createJwtWithClaims(Map.of());

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testConvertWithEmptyRealmRoles() {
        // Given
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Collections.emptyList());

        Jwt jwt = createJwtWithClaims(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testConvertWithNullResourceAccess() {
        // Given
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("admin"));

        Jwt jwt = createJwtWithClaims(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_admin"));
    }

    @Test
    void testConvertWithRealmAccessWithoutRoles() {
        // Given
        Map<String, Object> realmAccess = new HashMap<>();
        // No "roles" key

        Jwt jwt = createJwtWithClaims(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testConvertWithMalformedResourceAccess() {
        // Given - resource_access without proper structure
        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("client1", "not-a-map"); // Should be a Map

        Jwt jwt = createJwtWithClaims(Map.of("resource_access", resourceAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testConvertWithResourceAccessClientWithoutRoles() {
        // Given
        Map<String, Object> clientConfig = new HashMap<>();
        clientConfig.put("other-field", "value");
        // No "roles" key

        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("my-client", clientConfig);

        Jwt jwt = createJwtWithClaims(Map.of("resource_access", resourceAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testConvertWithNullRolesInRealmAccess() {
        // Given
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", null);

        Jwt jwt = createJwtWithClaims(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testRolePrefixIsAdded() {
        // Given
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("test-role"));

        Jwt jwt = createJwtWithClaims(Map.of("realm_access", realmAccess));

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        GrantedAuthority authority = authorities.iterator().next();
        assertTrue(authority.getAuthority().startsWith("ROLE_"));
        assertEquals("ROLE_test-role", authority.getAuthority());
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
