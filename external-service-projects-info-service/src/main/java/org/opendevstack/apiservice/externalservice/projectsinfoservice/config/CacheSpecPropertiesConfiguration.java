package org.opendevstack.apiservice.externalservice.projectsinfoservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "externalservices.projects-info-service.custom.cache")
public class CacheSpecPropertiesConfiguration {
    private Map<String, CacheSpec> specs;

    @Getter
    @Setter
    public static class CacheSpec {
        private long ttl;
        private int maxSize;
    }
}
