package org.opendevstack.apiservice.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private String issuer;
    private String audience;

    /**
     * Endpoints that don't require authentication.
     */
    private String[] publicEndpoints = {
        "/api/public/**",
        "/actuator/health",
        "/actuator/info",
        "/h2-console/**"
    };
}
