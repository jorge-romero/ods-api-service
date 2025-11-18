package org.opendevstack.apiservice.externalservice.webhookproxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request payload for triggering a build via the webhook proxy /build endpoint.
 * 
 * Example JSON:
 * {
 *   "branch": "master",
 *   "repository": "ods-project-quickstarters",
 *   "project": "opendevstack",
 *   "env": [
 *     {"name": "RELEASE_PARAM", "value": "value1"},
 *     {"name": "OTHER_PARAM", "value": "value2"}
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookProxyBuildRequest {
    
    /**
     * Git branch to build (e.g., "master", "develop", "feature/my-feature")
     */
    private String branch;
    
    /**
     * Repository name (e.g., "ods-project-quickstarters")
     */
    private String repository;
    
    /**
     * Project key (e.g., "opendevstack")
     */
    private String project;
    
    /**
     * Environment variables to pass to the build pipeline
     */
    @Builder.Default
    private List<EnvPair> env = new ArrayList<>();
    
    /**
     * Add an environment variable to the request
     * 
     * @param name Variable name
     * @param value Variable value
     * @return This request for method chaining
     */
    public WebhookProxyBuildRequest addEnv(String name, String value) {
        if (this.env == null) {
            this.env = new ArrayList<>();
        }
        this.env.add(new EnvPair(name, value));
        return this;
    }
}
