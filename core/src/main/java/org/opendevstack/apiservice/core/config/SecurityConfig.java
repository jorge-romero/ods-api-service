package org.opendevstack.apiservice.core.config;

import org.opendevstack.apiservice.core.engine.authorization.PolicyAuthorizationManager;
import org.opendevstack.apiservice.core.engine.filter.ApiRegistryFilter;
import org.opendevstack.apiservice.core.engine.filter.RequestBodyCachingFilter;
import org.opendevstack.apiservice.core.security.filter.AuthTypeEnforcementFilter;
import org.opendevstack.apiservice.core.security.jwt.AzureJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private final PolicyAuthorizationManager policyAuthorizationManager;
    private final SecurityProperties securityProperties;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    public SecurityConfig(AzureJwtAuthenticationConverter jwtConverter,
                          AuthTypeEnforcementFilter authTypeEnforcementFilter,
                          ApiRegistryFilter apiRegistryFilter,
                          PolicyAuthorizationManager policyAuthorizationManager,
                          SecurityProperties securityProperties) {
        this.jwtConverter = jwtConverter;
        this.authTypeEnforcementFilter = authTypeEnforcementFilter;
        this.apiRegistryFilter = apiRegistryFilter;
        this.policyAuthorizationManager = policyAuthorizationManager;
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

    /**
     * Prevents AuthTypeEnforcementFilter from being auto-registered as a
     * standalone servlet filter by Spring Boot (since it's @Component).
     * Same rationale as {@link #apiRegistryFilterRegistration()}.
     */
    @Bean
    public FilterRegistrationBean<AuthTypeEnforcementFilter> authTypeEnforcementFilterRegistration() {
        FilterRegistrationBean<AuthTypeEnforcementFilter> registration =
                new FilterRegistrationBean<>(authTypeEnforcementFilter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Prevents RequestBodyCachingFilter from being auto-registered as a
     * standalone servlet filter by Spring Boot (since it is a Filter @Bean).
     * Without this, the filter runs both outside and inside the security chain:
     * <ul>
     *   <li>Outside: caches the body before FilterChainProxy wraps the request
     *       in a {@code FirewalledRequest}.</li>
     *   <li>Inside: OncePerRequestFilter skips (already ran), so
     *       {@code PolicyAuthorizationManager} sees a {@code FirewalledRequest}
     *       instead of a {@code CachedBodyHttpServletRequest}.</li>
     * </ul>
     */
    @Bean
    public FilterRegistrationBean<RequestBodyCachingFilter> requestBodyCachingFilterRegistration(
            RequestBodyCachingFilter filter) {
        FilterRegistrationBean<RequestBodyCachingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public RequestBodyCachingFilter requestBodyCachingFilter() {
        return new RequestBodyCachingFilter();
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
                .addFilterBefore(requestBodyCachingFilter(), BearerTokenAuthenticationFilter.class)
                .addFilterBefore(apiRegistryFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(authTypeEnforcementFilter, BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    if (securityProperties.getPublicEndpoints() != null) {
                        Arrays.stream(securityProperties.getPublicEndpoints())
                                .forEach(endpoint -> auth.requestMatchers(endpoint).permitAll());
                    }
                    auth.requestMatchers("/admin/**").hasRole("admin");
                    auth.anyRequest().access(policyAuthorizationManager);
                })
                .build();
    }
}
