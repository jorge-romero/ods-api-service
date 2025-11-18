package org.opendevstack.apiservice.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private boolean enabled = true;
    private boolean jwtValidationEnabled = false;
    private String issuer;
    private String audience;
    private String jwkSetUri;

    /**
     * Map of endpoint patterns to required roles
     * Format: pattern -> list of roles
     */
    private Map<String, String[]> endpointRoles = new HashMap<>();

    /**
     * Map of endpoints that don't require authentication
     */
    private String[] publicEndpoints = {
        "/api/public/**",
        "/actuator/health",
        "/actuator/info",
        "/h2-console/**"
    };
}
