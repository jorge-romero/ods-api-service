package org.opendevstack.apiservice.core.security.authorization;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyContextFactoryTest {

    private final PolicyContextFactory factory = new PolicyContextFactory();

    @Test
    void create_withJwtAuthentication_extractsClientIdSubjectAndClaims() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user-1")
                .claim("azp", "client-app")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        ApiDefinition apiDef = new ApiDefinition("api-1", "Test API", "/test", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v0/test");

        PolicyContext ctx = factory.create(apiDef, request);

        assertEquals("client-app", ctx.getClientId());
        assertEquals("user-1", ctx.getSubject());
        assertSame(apiDef, ctx.getApiDefinition());
        assertSame(request, ctx.getRequest());
        assertFalse(ctx.getClaims().isEmpty());

        SecurityContextHolder.clearContext();
    }

    @Test
    void create_withNoAuthentication_returnsNullClientIdAndSubject() {
        SecurityContextHolder.clearContext();

        ApiDefinition apiDef = new ApiDefinition("api-1", "Test API", "/test", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v0/test");

        PolicyContext ctx = factory.create(apiDef, request);

        assertNull(ctx.getClientId());
        assertNull(ctx.getSubject());
        assertTrue(ctx.getClaims().isEmpty());
    }

    @Test
    void create_withAzpBlank_fallsBackToAppid() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user-1")
                .claim("azp", "")
                .claim("appid", "v1-client")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        ApiDefinition apiDef = new ApiDefinition("api-1", "Test API", "/test", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v0/test");

        PolicyContext ctx = factory.create(apiDef, request);

        assertEquals("v1-client", ctx.getClientId());

        SecurityContextHolder.clearContext();
    }
}
