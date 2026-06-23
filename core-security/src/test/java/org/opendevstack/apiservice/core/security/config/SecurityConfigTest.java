package org.opendevstack.apiservice.core.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.security.authorization.PolicyAuthorizationManager;
import org.opendevstack.apiservice.core.security.filter.CachedBodyRequestFilter;
import org.opendevstack.apiservice.core.security.jwt.AzureJwtAuthenticationConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    private SecurityProperties securityProperties;
    private PolicyAuthorizationManager policyAuthorizationManager;
    private AzureJwtAuthenticationConverter azureJwtAuthenticationConverter;
    private CachedBodyRequestFilter cachedBodyRequestFilter;
    private JwtIssuerAuthenticationManagerResolver resolver;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityProperties = mock(SecurityProperties.class);
        policyAuthorizationManager = mock(PolicyAuthorizationManager.class);
        azureJwtAuthenticationConverter = new AzureJwtAuthenticationConverter();
        cachedBodyRequestFilter = mock(CachedBodyRequestFilter.class);
        resolver = mock(JwtIssuerAuthenticationManagerResolver.class);
        securityConfig = new SecurityConfig(
            securityProperties,
            policyAuthorizationManager,
            azureJwtAuthenticationConverter,
            cachedBodyRequestFilter
        );
    }

    @Test
    void securityFilterChain_withPublicEndpoints() throws Exception {
        when(securityProperties.getPublicEndpoints()).thenReturn(new String[]{"/health", "/info"});
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain chain = securityConfig.securityFilterChain(http, resolver);
        assertNotNull(chain);
    }

    @Test
    void securityFilterChain_withNullPublicEndpoints() throws Exception {
        when(securityProperties.getPublicEndpoints()).thenReturn(null);
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain chain = securityConfig.securityFilterChain(http, resolver);
        assertNotNull(chain);
    }

    @Test
    void securityFilterChain_withEmptyPublicEndpoints() throws Exception {
        when(securityProperties.getPublicEndpoints()).thenReturn(new String[]{});
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain chain = securityConfig.securityFilterChain(http, resolver);
        assertNotNull(chain);
    }

    @Test
    void jwtIssuerAuthenticationManagerResolver_registersConfiguredIssuers() {
        SecurityProperties.IssuerConfig v1 = new SecurityProperties.IssuerConfig();
        v1.setIssuerUri("https://sts.windows.net/tenant/");
        v1.setJwkSetUri("https://login.microsoftonline.com/common/discovery/keys");

        SecurityProperties.IssuerConfig v2 = new SecurityProperties.IssuerConfig();
        v2.setIssuerUri("https://login.microsoftonline.com/tenant/v2.0");
        v2.setJwkSetUri("https://login.microsoftonline.com/tenant/discovery/v2.0/keys");

        when(securityProperties.getIssuers()).thenReturn(List.of(v1, v2));
        when(securityProperties.getAudiences()).thenReturn(List.of("api://my-app"));

        JwtIssuerAuthenticationManagerResolver built = securityConfig.jwtIssuerAuthenticationManagerResolver();
        assertNotNull(built);
    }
}
