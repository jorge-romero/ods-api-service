package org.opendevstack.apiservice.core.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {
    private SecurityProperties securityProperties;
    private FlowProperties flowProperties;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityProperties = mock(SecurityProperties.class);
        flowProperties = mock(FlowProperties.class);
        securityConfig = new SecurityConfig(securityProperties, flowProperties);
    }

    @Test
    void testJwtAuthenticationConverterBean() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();
        assertNotNull(converter);
    }

    @Test
    void testCustomRoleConverterBean() {
        EntraIdRoleConverter converter = securityConfig.customRoleConverter();
        assertNotNull(converter);
    }

    @Test
    void testJwtDecoderLenient() {
        when(securityProperties.isJwtValidationEnabled()).thenReturn(false);
        JwtDecoder decoder = securityConfig.jwtDecoder();
        assertNotNull(decoder);
    }

    @Test
    void testJwtDecoderValidating() {
        when(securityProperties.isJwtValidationEnabled()).thenReturn(true);
        when(securityProperties.getJwkSetUri()).thenReturn("http://localhost/jwk");
        JwtDecoder decoder = securityConfig.jwtDecoder();
        assertNotNull(decoder);
    }

    @Test
    void testJwtDecoderThrowsIfNoJwkSetUri() {
        when(securityProperties.isJwtValidationEnabled()).thenReturn(true);
        when(securityProperties.getJwkSetUri()).thenReturn("");
        Exception exception = assertThrows(IllegalStateException.class, () -> securityConfig.jwtDecoder());
        assertTrue(exception.getMessage().contains("no JWK Set URI is configured"));
    }

    @Test
    void testAudienceValidatorAcceptsExpectedAudience() throws Exception {
        OAuth2TokenValidator<Jwt> validator = buildJwtValidator("api://expected-audience");
        Jwt jwt = createJwtWithAudience(List.of("api://expected-audience", "other-audience"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertFalse(result.hasErrors());
    }

    @Test
    void testAudienceValidatorRejectsUnexpectedAudience() throws Exception {
        OAuth2TokenValidator<Jwt> validator = buildJwtValidator("api://expected-audience");
        Jwt jwt = createJwtWithAudience(List.of("api://another-audience"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream().anyMatch(error -> "invalid_token".equals(error.getErrorCode())));
    }

    @Test
    void testSecurityFilterChainBean() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        // Just verify bean creation does not throw
        SecurityFilterChain chain = securityConfig.securityFilterChain(http);
        assertNotNull(chain);
    }

    @SuppressWarnings("unchecked")
    private OAuth2TokenValidator<Jwt> buildJwtValidator(String audience) throws Exception {
        Method method = SecurityConfig.class.getDeclaredMethod("buildJwtValidator", String.class);
        method.setAccessible(true);
        return (OAuth2TokenValidator<Jwt>) method.invoke(securityConfig, audience);
    }

    private Jwt createJwtWithAudience(List<String> audiences) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject("test-subject")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(300))
            .claim("aud", audiences)
            .build();
    }
}
