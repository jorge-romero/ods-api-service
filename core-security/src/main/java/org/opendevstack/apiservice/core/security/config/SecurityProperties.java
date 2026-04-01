package org.opendevstack.apiservice.core.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private boolean enabled = true;

    /**
     * Map of endpoints that don't require authentication
     */
    private String[] publicEndpoints;
}
