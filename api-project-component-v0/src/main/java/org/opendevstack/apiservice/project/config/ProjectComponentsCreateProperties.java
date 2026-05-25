package org.opendevstack.apiservice.project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "apis.project-components.create")
public class ProjectComponentsCreateProperties {

    private List<String> reservedParams;
}