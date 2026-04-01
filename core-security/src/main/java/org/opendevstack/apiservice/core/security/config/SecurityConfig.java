package org.opendevstack.apiservice.core.security.config;

import org.opendevstack.apiservice.core.security.authorization.PolicyAuthorizationManager;
import org.opendevstack.apiservice.core.security.filter.CachedBodyRequestFilter;
import org.opendevstack.apiservice.core.security.jwt.AzureJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.SecurityFilterChain;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final PolicyAuthorizationManager policyAuthorizationManager;
    private final AzureJwtAuthenticationConverter azureJwtAuthenticationConverter;
    private final CachedBodyRequestFilter cachedBodyRequestFilter;


    public SecurityConfig(SecurityProperties securityProperties,
                          PolicyAuthorizationManager policyAuthorizationManager,
                          AzureJwtAuthenticationConverter azureJwtAuthenticationConverter,
                          CachedBodyRequestFilter cachedBodyRequestFilter) {
        this.securityProperties = securityProperties;
        this.policyAuthorizationManager = policyAuthorizationManager;
        this.azureJwtAuthenticationConverter = azureJwtAuthenticationConverter;
        this.cachedBodyRequestFilter = cachedBodyRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> {
                // Allow public endpoints from security properties
                if (securityProperties.getPublicEndpoints() != null) {
                    Arrays.stream(securityProperties.getPublicEndpoints())
                        .forEach(endpoint -> {
                            log.info("Public endpoint configured: {}", endpoint);
                            authz.requestMatchers(endpoint).permitAll();
                        });
                }
                authz.anyRequest().access(policyAuthorizationManager);
            })
            .oauth2ResourceServer((oauth2) ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(azureJwtAuthenticationConverter))
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .csrf(csrf -> csrf.disable());

        http.addFilterBefore(cachedBodyRequestFilter, AuthorizationFilter.class);

        return http.build();
    }
}
