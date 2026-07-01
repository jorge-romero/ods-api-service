package org.opendevstack.apiservice.core.security.jwt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void get_token_value_returns_jwt_token_string() {
        // GIVEN
        Jwt jwt = buildJwt("eyJhbGciOiJSUzI1NiJ9.test", Map.of("azp", "client-a"));
        setSecurityContext(jwt);

        // WHEN
        String token = JwtUtils.getTokenValue();

        // THEN
        assertEquals("eyJhbGciOiJSUzI1NiJ9.test", token);
    }

    @Test
    void get_token_value_throws_when_principal_is_not_jwt() {
        // GIVEN
        SecurityContext ctx = mock(SecurityContext.class);
        var auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn("not-a-jwt");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        // WHEN / THEN
        assertThrows(InvalidBearerTokenException.class, JwtUtils::getTokenValue);
    }

    @Test
    void get_client_id_returns_azp_claim() {
        // GIVEN
        String clientId = "00000000-0000-0000-0000-000000000001";
        Jwt jwt = buildJwt("token", Map.of("azp", clientId));
        setSecurityContext(jwt);

        // WHEN
        UUID result = JwtUtils.getClientId();

        // THEN
        assertEquals(UUID.fromString(clientId), result);
    }

    @Test
    void get_client_id_falls_back_to_appid_when_azp_is_blank() {
        // GIVEN
        String clientId = "00000000-0000-0000-0000-000000000002";
        Jwt jwt = buildJwt("token", Map.of("azp", "", "appid", clientId));
        setSecurityContext(jwt);

        // WHEN
        UUID result = JwtUtils.getClientId();

        // THEN
        assertEquals(UUID.fromString(clientId), result);
    }

    @Test
    void get_client_id_throws_when_no_client_claim() {
        // GIVEN
        Jwt jwt = buildJwt("token", Map.of("sub", "user"));
        setSecurityContext(jwt);

        // WHEN / THEN
        assertThrows(InvalidBearerTokenException.class, JwtUtils::getClientId);
    }

    @Test
    void extract_client_id_from_jwt_with_azp() {
        // GIVEN
        String clientId = "00000000-0000-0000-0000-000000000003";
        Jwt jwt = buildJwt("token", Map.of("azp", clientId));

        // WHEN
        UUID result = JwtUtils.extractClientId(jwt);

        // THEN
        assertEquals(UUID.fromString(clientId), result);
    }

    @Test
    void extract_client_id_from_jwt_with_appid_fallback() {
        // GIVEN
        String clientId = "00000000-0000-0000-0000-000000000004";
        Jwt jwt = buildJwt("token", Map.of("appid", clientId));

        // WHEN
        UUID result = JwtUtils.extractClientId(jwt);

        // THEN
        assertEquals(UUID.fromString(clientId), result);
    }

    @Test
    void extract_audiences_supports_single_string_claim() {
        // GIVEN
        Jwt jwt = buildJwt("token", Map.of("aud", "aud-1"));

        // WHEN
        List<String> audiences = JwtUtils.extractAudiences(jwt);

        // THEN
        assertEquals(List.of("aud-1"), audiences);
    }

    @Test
    void extract_audiences_supports_list_claim() {
        // GIVEN
        Jwt jwt = buildJwt("token", Map.of("aud", List.of("aud-1", "aud-2")));

        // WHEN
        List<String> audiences = JwtUtils.extractAudiences(jwt);

        // THEN
        assertEquals(List.of("aud-1", "aud-2"), audiences);
    }

    @Test
    void token_matches_scope_audience_when_audience_and_scope_match_bypass() {
        // GIVEN
        Jwt jwt = buildJwt("token", Map.of(
                "aud", "6d81fd84-9b38-4aed-9403-ed95f6395cd7",
                "scp", "Api.Access"));
        setSecurityContext(jwt);

        // WHEN
        boolean result = JwtUtils.tokenMatchesScopeAudience(
                "6d81fd84-9b38-4aed-9403-ed95f6395cd7", "Api.Access");

        // THEN
        assertTrue(result);
    }

    @Test
    void token_matches_scope_audience_when_scope_is_one_of_several_scp_values() {
        // GIVEN
        Jwt jwt = buildJwt("token", Map.of(
                "aud", "api://6d81fd84-9b38-4aed-9403-ed95f6395cd7",
                "scp", "User.Read Api.Access Files.Read"));
        setSecurityContext(jwt);

        // WHEN
        boolean result = JwtUtils.tokenMatchesScopeAudience(
                "api://6d81fd84-9b38-4aed-9403-ed95f6395cd7", "Api.Access");

        // THEN
        assertTrue(result);
    }

    @Test
    void token_matches_scope_audience_returns_false_when_audience_does_not_match() {
        // GIVEN
        Jwt jwt = buildJwt("token", Map.of(
                "aud", "api://another-app",
                "scp", "Api.Access"));
        setSecurityContext(jwt);

        // WHEN
        boolean result = JwtUtils.tokenMatchesScopeAudience(
                "6d81fd84-9b38-4aed-9403-ed95f6395cd7", "Api.Access");

        // THEN
        assertFalse(result);
    }

    @Test
    void token_matches_scope_audience_returns_false_when_scope_does_not_match() {
        // GIVEN
        Jwt jwt = buildJwt("token", Map.of(
                "aud", "6d81fd84-9b38-4aed-9403-ed95f6395cd7",
                "scp", "User.Read"));
        setSecurityContext(jwt);

        // WHEN
        boolean result = JwtUtils.tokenMatchesScopeAudience(
                "6d81fd84-9b38-4aed-9403-ed95f6395cd7", "Api.Access");

        // THEN
        assertFalse(result);
    }

    @Test
    void token_matches_scope_audience_returns_false_when_bypass_values_are_blank() {
        // GIVEN
        Jwt jwt = buildJwt("token", Map.of(
                "aud", "6d81fd84-9b38-4aed-9403-ed95f6395cd7",
                "scp", "Api.Access"));
        setSecurityContext(jwt);

        // WHEN / THEN
        assertFalse(JwtUtils.tokenMatchesScopeAudience("", "Api.Access"));
        assertFalse(JwtUtils.tokenMatchesScopeAudience("6d81fd84-9b38-4aed-9403-ed95f6395cd7", ""));
        assertFalse(JwtUtils.tokenMatchesScopeAudience(null, null));
    }

    @Test
    void extract_scopes_splits_scp_claim_by_whitespace() {
        // GIVEN
        Jwt jwt = buildJwt("token", Map.of("scp", "User.Read Api.Access"));

        // WHEN
        var scopes = JwtUtils.extractScopes(jwt);

        // THEN
        assertTrue(scopes.contains("User.Read"));
        assertTrue(scopes.contains("Api.Access"));
        assertEquals(2, scopes.size());
    }

    @Test
    void extract_scopes_returns_empty_set_when_scp_claim_absent() {
        // GIVEN
        Jwt jwt = buildJwt("token", Map.of("aud", "some-audience"));

        // WHEN
        var scopes = JwtUtils.extractScopes(jwt);

        // THEN
        assertTrue(scopes.isEmpty());
    }

    private Jwt buildJwt(String tokenValue, Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue(tokenValue)
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        claims.forEach(builder::claim);
        return builder.build();
    }

    private void setSecurityContext(Jwt jwt) {
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }
}
