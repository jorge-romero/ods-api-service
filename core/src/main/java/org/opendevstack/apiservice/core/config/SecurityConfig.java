package org.opendevstack.apiservice.core.config;

import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.engine.filter.ApiRegistryFilter;
import org.opendevstack.apiservice.core.security.filter.AuthTypeEnforcementFilter;
import org.opendevstack.apiservice.core.security.jwt.AzureJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AzureJwtAuthenticationConverter jwtConverter;
    private final AuthTypeEnforcementFilter authTypeEnforcementFilter;
    private final ApiRegistryFilter apiRegistryFilter;
    private final SecurityProperties securityProperties;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    public SecurityConfig(AzureJwtAuthenticationConverter jwtConverter,
                          AuthTypeEnforcementFilter authTypeEnforcementFilter,
                          ApiRegistryFilter apiRegistryFilter,
                          SecurityProperties securityProperties) {
        this.jwtConverter = jwtConverter;
        this.authTypeEnforcementFilter = authTypeEnforcementFilter;
        this.apiRegistryFilter = apiRegistryFilter;
        this.securityProperties = securityProperties;
    }

    /**
     * Prevents ApiRegistryFilter from being auto-registered as a standalone
     * servlet filter by Spring Boot (since it's @Component), which would cause
     * it to execute outside the Security filter chain.
     */
    @Bean
    public FilterRegistrationBean<ApiRegistryFilter> apiRegistryFilterRegistration() {
        FilterRegistrationBean<ApiRegistryFilter> registration = new FilterRegistrationBean<>(apiRegistryFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AuthTypeEnforcementFilter> authTypeEnforcementFilterRegistration() {
        FilterRegistrationBean<AuthTypeEnforcementFilter> registration = new FilterRegistrationBean<>(authTypeEnforcementFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwkSetUri(jwkSetUri)
                                .jwtAuthenticationConverter(jwtConverter)))
                .addFilterBefore(apiRegistryFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(authTypeEnforcementFilter, BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    if (securityProperties.getPublicEndpoints() != null) {
                        Arrays.stream(securityProperties.getPublicEndpoints())
                                .forEach(endpoint -> auth.requestMatchers(endpoint).permitAll());
                    }
                    auth.requestMatchers("/admin/**").hasRole("admin");
                    // Check if the resolved API definition is public
                    auth.anyRequest().access((authentication, context) -> {
                        var request = context.getRequest();
                        Object apiDefAttr = request.getAttribute("oas.apiDefinition");
                        
                        if (apiDefAttr instanceof ApiDefinition def && def.isPublic()) {
                            return new AuthorizationDecision(true);
                        }
                        
                        return new AuthorizationDecision(authentication.get().isAuthenticated());
                    });
                })
                .build();
    }
}
