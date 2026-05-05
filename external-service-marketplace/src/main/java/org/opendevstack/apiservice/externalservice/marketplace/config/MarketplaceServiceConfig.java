package org.opendevstack.apiservice.externalservice.marketplace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "externalservices.marketplace")
@Data
public class MarketplaceServiceConfig {

    /**
     * Name of the default Marketplace instance to use when no instance name is provided.
     * If not set, the first configured instance is used as default.
     */
    private String defaultInstance;

    /**
     * Map of Marketplace instances with the instance name as the key and the configuration as the value.
     */
    private Map<String, MarketplaceInstanceConfig> instances = new HashMap<>();
}
