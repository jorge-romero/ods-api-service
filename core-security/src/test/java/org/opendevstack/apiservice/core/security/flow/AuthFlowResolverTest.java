package org.opendevstack.apiservice.core.security.flow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthFlowResolverTest {

    private AuthFlowResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new AuthFlowResolver();
    }

    @Test
    void resolve_withScpClaim_returnsObo() {
        Jwt jwt = buildJwt(Map.of("scp", "api.read api.write"));
        assertEquals(AuthType.OBO, resolver.resolve(jwt));
    }

    @Test
    void resolve_withBlankScpClaim_returnsClientCredentials() {
        Jwt jwt = buildJwt(Map.of("scp", "   "));
        assertEquals(AuthType.CLIENT_CREDENTIALS, resolver.resolve(jwt));
    }

    @Test
    void resolve_withoutScpClaim_returnsClientCredentials() {
        Jwt jwt = buildJwt(Map.of("sub", "user-id"));
        assertEquals(AuthType.CLIENT_CREDENTIALS, resolver.resolve(jwt));
    }

    @Test
    void resolve_withNullJwt_returnsNone() {
        assertEquals(AuthType.NONE, resolver.resolve(null));
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "test-subject")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
