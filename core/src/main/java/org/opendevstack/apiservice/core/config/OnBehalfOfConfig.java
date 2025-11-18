package org.opendevstack.apiservice.core.config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OnBehalfOfProperties.class)
public class OnBehalfOfConfig {
    // This class enables binding of OnBehalfOfProperties to configuration
    // No additional code needed - just enables the configuration properties
}
