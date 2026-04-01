package org.opendevstack.apiservice.core.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.security.authorization.PolicyAuthorizationManager;
import org.opendevstack.apiservice.core.security.filter.CachedBodyRequestFilter;
import org.opendevstack.apiservice.core.security.jwt.AzureJwtAuthenticationConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    private SecurityProperties securityProperties;
    private PolicyAuthorizationManager policyAuthorizationManager;
    private AzureJwtAuthenticationConverter azureJwtAuthenticationConverter;
    private CachedBodyRequestFilter cachedBodyRequestFilter;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityProperties = mock(SecurityProperties.class);
        policyAuthorizationManager = mock(PolicyAuthorizationManager.class);
        azureJwtAuthenticationConverter = new AzureJwtAuthenticationConverter();
        cachedBodyRequestFilter = mock(CachedBodyRequestFilter.class);
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

        SecurityFilterChain chain = securityConfig.securityFilterChain(http);
        assertNotNull(chain);
    }

    @Test
    void securityFilterChain_withNullPublicEndpoints() throws Exception {
        when(securityProperties.getPublicEndpoints()).thenReturn(null);
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain chain = securityConfig.securityFilterChain(http);
        assertNotNull(chain);
    }

    @Test
    void securityFilterChain_withEmptyPublicEndpoints() throws Exception {
        when(securityProperties.getPublicEndpoints()).thenReturn(new String[]{});
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain chain = securityConfig.securityFilterChain(http);
        assertNotNull(chain);
    }
}
