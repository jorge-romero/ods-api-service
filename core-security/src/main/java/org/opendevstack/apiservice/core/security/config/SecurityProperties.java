package org.opendevstack.apiservice.core.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private boolean enabled = true;

    /**
     * Endpoints that don't require authentication.
     */
    private String[] publicEndpoints;

    /**
     * Audience values accepted by this API. A valid token must carry at least one of these.
     * Applies to every configured issuer.
     */
    private List<String> audiences = new ArrayList<>();

    /**
     * Trusted issuers. Each entry maps an issuer URI to a JWK set URI used for
     * signature validation. The {@code iss} claim of the incoming token selects
     * which entry is used.
     */
    private List<IssuerConfig> issuers = new ArrayList<>();

    @Getter
    @Setter
    public static class IssuerConfig {
        /** Expected value of the {@code iss} claim. */
        private String issuerUri;
        /** URL of the JWK set used to verify token signatures for this issuer. */
        private String jwkSetUri;
    }
}
