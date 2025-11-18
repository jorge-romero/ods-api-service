package org.opendevstack.apiservice.core.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

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
        CustomRoleConverter converter = securityConfig.customRoleConverter();
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
        assertTrue(exception.getMessage().contains("no JWK Set URI"));
    }

    @Test
    void testSecurityFilterChainBean() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        // Just verify bean creation does not throw
        SecurityFilterChain chain = securityConfig.securityFilterChain(http);
        assertNotNull(chain);
    }
}
