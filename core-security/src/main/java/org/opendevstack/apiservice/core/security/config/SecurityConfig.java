package org.opendevstack.apiservice.core.security.config;

import org.opendevstack.apiservice.core.security.authorization.PolicyAuthorizationManager;
import org.opendevstack.apiservice.core.security.filter.CachedBodyRequestFilter;
import org.opendevstack.apiservice.core.security.jwt.AzureJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.SecurityFilterChain;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver() {
        List<String> audiences = securityProperties.getAudiences();
        Map<String, AuthenticationManager> managers = new HashMap<>();

        for (SecurityProperties.IssuerConfig issuerConfig : securityProperties.getIssuers()) {
            String issuerUri = issuerConfig.getIssuerUri();
            AuthenticationManager manager = buildAuthenticationManager(issuerConfig, audiences);
            managers.put(issuerUri, manager);
            log.info("Registered JWT issuer: {}", issuerUri);
        }

        return new JwtIssuerAuthenticationManagerResolver(managers::get);
    }

    private AuthenticationManager buildAuthenticationManager(
            SecurityProperties.IssuerConfig issuerConfig, List<String> audiences) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(issuerConfig.getJwkSetUri())
                .build();

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(JwtValidators.createDefaultWithIssuer(issuerConfig.getIssuerUri()));
        if (audiences != null && !audiences.isEmpty()) {
            validators.add(new JwtClaimValidator<List<String>>(
                    JwtClaimNames.AUD,
                    aud -> aud != null && !Collections.disjoint(aud, audiences)));
        }
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));

        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
        provider.setJwtAuthenticationConverter(azureJwtAuthenticationConverter);
        return provider::authenticate;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver) throws Exception {
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
            .oauth2ResourceServer(oauth2 ->
                oauth2.authenticationManagerResolver(jwtIssuerAuthenticationManagerResolver)
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .csrf(csrf -> csrf.disable());

        // Body-caching filter must run before AuthorizationFilter so that
        // PolicyAuthorizationManager can read the request body for policy evaluation,
        // and the CachedBodyHttpServletRequest wrapper propagates to later servlet
        // filters (including BypassRoutingFilter) for transparent body replay.
        http.addFilterBefore(cachedBodyRequestFilter, AuthorizationFilter.class);

        return http.build();
    }
}
