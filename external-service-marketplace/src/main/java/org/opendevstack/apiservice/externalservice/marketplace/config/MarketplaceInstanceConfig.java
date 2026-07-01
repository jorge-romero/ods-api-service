package org.opendevstack.apiservice.externalservice.marketplace.config;

import lombok.Data;

@Data
public class MarketplaceInstanceConfig {
    /**
     * The project components base URL of the Marketplace
     */
    private String projectComponentsBaseUrl;

    /**
     * The provisioner actions base URL of the Marketplace
     */
    private String provisionerActionsBaseUrl;

    /**
     * The Bitbucket base URL of the Marketplace project
     */
    private String bitbucketBaseUrl;

    /**
     * The username used for basic auth
     */
    private String username;

    /**
     * The password used for basic auth
     */
    private String password;

    /**
     * Connection timeout in milliseconds (default: 30000)
     */
    private int connectionTimeout = 30000;

    /**
     * Read timeout in milliseconds (default: 30000).
     */
    private int readTimeout = 30000;

    /**
     * Whether to trust all SSL certificates (default: false).
     * WARNING: Should only be used in development environments.
     */
    private boolean trustAllCertificates = false;

    /**
     * OAuth2 scope used for OBO token exchange when calling this Marketplace instance.
     * Example: {@code api://<marketplace-app-id>/Api.Access}
     */
    private String oboScope;

    /**
     * Bypass configuration. When the incoming token already targets the configured bypass
     * audience and scope, the OBO token exchange is skipped and the token is forwarded as-is.
     */
    private Bypass bypass = new Bypass();

    /**
     * Audience and scope used to decide whether the incoming token can be forwarded without
     * an OBO exchange.
     */
    @Data
    public static class Bypass {
        /**
         * Audience that must be present in the token {@code aud} claim to allow the bypass.
         */
        private String audience;

        /**
         * Scope that must be present in the token {@code scp} claim to allow the bypass.
         */
        private String scope;
    }
}