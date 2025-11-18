package org.opendevstack.apiservice.externalservice.projectsinfoservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for SSL settings in external service calls.
 */
@ConfigurationProperties(prefix = "externalservices.projects-info-service.ssl")
public class ProjectsInfoServiceSslProperties {
    
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

    public boolean isVerifyCertificates() {
        return verifyCertificates;
    }

    public void setVerifyCertificates(boolean verifyCertificates) {
        this.verifyCertificates = verifyCertificates;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }
}