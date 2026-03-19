package org.opendevstack.apiservice.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final FlowProperties flowProperties;

    public SecurityConfig(SecurityProperties securityProperties, FlowProperties flowProperties) {
        this.securityProperties = securityProperties;
        this.flowProperties = flowProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> {
                // Allow public endpoints from security properties
                if (securityProperties.getPublicEndpoints() != null) {
                    Arrays.stream(securityProperties.getPublicEndpoints())
                        .forEach(endpoint -> authz.requestMatchers(endpoint).permitAll());
                }

                // Apply flow-based security rules
                applyFlowBasedSecurity(authz);

                // All other requests require authentication
                authz.anyRequest().authenticated();
            })
            .oauth2ResourceServer(oauth2 -> 
                oauth2.jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    private void applyFlowBasedSecurity(org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
        if (flowProperties.getApis() == null) {
            return;
        }

        // Apply security rules based on flow configuration
        flowProperties.getApis().forEach((apiName, apiFlows) -> {
            if (apiFlows.getEndpoints() != null) {
                apiFlows.getEndpoints().forEach(endpointFlow -> {
                    String pattern = endpointFlow.getPattern();
                    
                    // Debug logging to see what patterns we're receiving
                    log.info("Processing security pattern: '{}' for API: {}", pattern, apiName);
                    
                    // Permit all if specified
                    if (endpointFlow.isPermitAll()) {
                        authz.requestMatchers(pattern).permitAll();
                    }
                    // Apply role-based security
                    else if (endpointFlow.getRoles() != null && !endpointFlow.getRoles().isEmpty()) {
                        String[] rolesArray = endpointFlow.getRoles().toArray(new String[0]);
                        authz.requestMatchers(pattern).hasAnyRole(rolesArray);
                    }
                    // Otherwise require authentication
                    else if (endpointFlow.isRequireAuthentication()) {
                        authz.requestMatchers(pattern).authenticated();
                    }
                });
            }
        });
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(new EntraIdRoleConverter());
        return authenticationConverter;
    }

    @Bean
    public EntraIdRoleConverter customRoleConverter() {
        return new EntraIdRoleConverter();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // For development: use a lenient decoder when JWT validation is disabled
        if (!securityProperties.isJwtValidationEnabled()) {
            return createLenientJwtDecoder();
        }
        
        // For production: use proper JWT validation with JWK Set URI
        return createValidatingJwtDecoder();
    }

    private JwtDecoder createLenientJwtDecoder() {
        return token -> {
            // Parse JWT without validation
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new org.springframework.security.oauth2.jwt.BadJwtException("Invalid JWT token");
            }
            
            // Decode payload
            java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
            String payload = new String(decoder.decode(parts[1]));
            
            return parseJwtPayload(token, payload);
        };
    }

    private org.springframework.security.oauth2.jwt.Jwt parseJwtPayload(String token, String payload) {
        org.springframework.security.oauth2.jwt.Jwt.Builder builder = 
            org.springframework.security.oauth2.jwt.Jwt.withTokenValue(token);
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> claims = mapper.readValue(payload, java.util.Map.class);
            
            builder.claims(c -> c.putAll(claims));
            
            // Set standard claims if present
            if (claims.containsKey("iss")) {
                builder.issuer(claims.get("iss").toString());
            }
            if (claims.containsKey("sub")) {
                builder.subject(claims.get("sub").toString());
            }
            if (claims.containsKey("exp")) {
                Number exp = (Number) claims.get("exp");
                builder.expiresAt(java.time.Instant.ofEpochSecond(exp.longValue()));
            } else {
                // Set a far future expiration if not present
                builder.expiresAt(java.time.Instant.now().plusSeconds(3600));
            }
            if (claims.containsKey("iat")) {
                Number iat = (Number) claims.get("iat");
                builder.issuedAt(java.time.Instant.ofEpochSecond(iat.longValue()));
            } else {
                builder.issuedAt(java.time.Instant.now());
            }
            
            builder.header("alg", "none");
            
            return builder.build();
        } catch (Exception e) {
            throw new org.springframework.security.oauth2.jwt.BadJwtException("Failed to parse JWT", e);
        }
    }

    private JwtDecoder createValidatingJwtDecoder() {
        String audience = securityProperties.getAudience();
        if (securityProperties.getJwkSetUri() != null && !securityProperties.getJwkSetUri().isEmpty()) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(securityProperties.getJwkSetUri()).build();
            decoder.setJwtValidator(buildJwtValidator(audience));
            return decoder;
        }
        
        throw new IllegalStateException(
            "JWT validation is enabled but no JWK Set URI is configured. " +
            "Please set app.security.jwk-set-uri in your configuration."
        );
    }

    private OAuth2TokenValidator<Jwt> buildJwtValidator(String audience) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();

        String issuer = securityProperties.getIssuer();
        if (issuer != null && !issuer.isBlank()) {
            validators.add(JwtValidators.createDefaultWithIssuer(issuer));
        } else {
            validators.add(JwtValidators.createDefault());
        }

        if (audience != null && !audience.isBlank()) {
            validators.add(jwt -> {
                List<String> audiences = jwt.getAudience();
                if (audiences != null && audiences.contains(audience)) {
                    return OAuth2TokenValidatorResult.success();
                }
                return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                        "invalid_token",
                        "Invalid audience. Expected " + audience,
                        null
                    )
                );
            });
        }

        return new DelegatingOAuth2TokenValidator<>(validators);
    }
}
