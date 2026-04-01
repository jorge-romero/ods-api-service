package org.opendevstack.apiservice.core.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureJwtAuthenticationConverterTest {

    private AzureJwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new AzureJwtAuthenticationConverter();
    }

    // ── Authority extraction ──

    @Test
    void convert_withRolesOnly_mapsToRolePrefixedAuthorities() {
        Jwt jwt = buildJwt(Map.of("roles", List.of("ADMIN", "READER")));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Collection<String> authorities = authorityStrings(token);
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_READER"));
        assertEquals(2, authorities.size());
    }

    @Test
    void convert_withScpOnly_mapsToScopePrefixedAuthorities() {
        Jwt jwt = buildJwt(Map.of("scp", "api.read api.write"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Collection<String> authorities = authorityStrings(token);
        assertTrue(authorities.contains("SCOPE_api.read"));
        assertTrue(authorities.contains("SCOPE_api.write"));
        assertEquals(2, authorities.size());
    }

    @Test
    void convert_withBothRolesAndScp_mapsBoth() {
        Jwt jwt = buildJwt(Map.of("roles", List.of("ADMIN"), "scp", "api.read"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Collection<String> authorities = authorityStrings(token);
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("SCOPE_api.read"));
        assertEquals(2, authorities.size());
    }

    @Test
    void convert_withNeitherClaim_returnsEmptyAuthorities() {
        Jwt jwt = buildJwt(Map.of());

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertTrue(token.getAuthorities().isEmpty());
    }

    @Test
    void convert_withBlankRole_isIgnored() {
        Jwt jwt = buildJwt(Map.of("roles", List.of("ADMIN", "  ", "")));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Collection<String> authorities = authorityStrings(token);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains("ROLE_ADMIN"));
    }

    @Test
    void convert_withBlankScp_returnsNoScopeAuthorities() {
        Jwt jwt = buildJwt(Map.of("scp", "   "));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertTrue(token.getAuthorities().isEmpty());
    }

    @Test
    void convert_setsSubjectAsName() {
        Jwt jwt = buildJwt(Map.of("sub", "user-123"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertEquals("user-123", token.getName());
    }

    // ── extractClientId ──

    @Test
    void extractClientId_withAzp_returnsAzp() {
        Jwt jwt = buildJwt(Map.of("azp", "client-a"));
        assertEquals("client-a", AzureJwtAuthenticationConverter.extractClientId(jwt));
    }

    @Test
    void extractClientId_withAzpBlank_fallsBackToAppid() {
        Jwt jwt = buildJwt(Map.of("azp", "", "appid", "client-b"));
        assertEquals("client-b", AzureJwtAuthenticationConverter.extractClientId(jwt));
    }

    @Test
    void extractClientId_withAzpNull_fallsBackToAppid() {
        Jwt jwt = buildJwt(Map.of("appid", "client-b"));
        assertEquals("client-b", AzureJwtAuthenticationConverter.extractClientId(jwt));
    }

    @Test
    void extractClientId_withNeitherClaim_returnsNull() {
        Jwt jwt = buildJwt(Map.of());
        assertNull(AzureJwtAuthenticationConverter.extractClientId(jwt));
    }

    // ── helpers ──

    private Collection<String> authorityStrings(AbstractAuthenticationToken token) {
        return token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    private Jwt buildJwt(Map<String, Object> extraClaims) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "test-subject")
                .claims(c -> c.putAll(extraClaims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
