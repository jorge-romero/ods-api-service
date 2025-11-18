package org.opendevstack.apiservice.externalservice.uipath.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for SSL settings in external service calls.
 */
@Component("uipathSslProperties")
@ConfigurationProperties(prefix = "automation.platform.uipath.ssl")
@Data
public class SslProperties {

    /**
     * Whether to verify SSL certificates when making external service calls.
     * Default is true for security.
     */
    private boolean verifyCertificates = true;
    /**
     * Path to the trust store file for SSL certificate validation.
     * Optional - if not provided, uses system default trust store.
     */
    private String trustStorePath;
    /**
     * Password for the trust store.
     */
    private String trustStorePassword;
    /**
     * Type of the trust store (JKS, PKCS12, etc.).
     * Default is JKS.
     */
    private String trustStoreType = "JKS";
}
