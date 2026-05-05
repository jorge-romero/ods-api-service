package org.opendevstack.apiservice.core.security.obo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security.obo")
@Data
public class OboTokenProperties {

    private String tokenUrl;

    private String clientId;

    private String clientSecret;
}
