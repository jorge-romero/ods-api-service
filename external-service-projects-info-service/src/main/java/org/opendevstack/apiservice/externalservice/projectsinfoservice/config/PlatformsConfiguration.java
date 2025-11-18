package org.opendevstack.apiservice.externalservice.projectsinfoservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "externalservices.projects-info-service.platforms")
public class PlatformsConfiguration {
    private String basePath;
    private String bearerToken;
    private Map<String, String> clusters;
}
