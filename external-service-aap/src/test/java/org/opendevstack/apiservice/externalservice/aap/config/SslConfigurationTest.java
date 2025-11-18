package org.opendevstack.apiservice.externalservice.aap.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SSL configuration functionality.
 */
class SslConfigurationTest {

    @Test
    void testSslPropertiesDefaultValues() {
        SslProperties sslProperties = new SslProperties();
        
        assertTrue(sslProperties.isVerifyCertificates(), "SSL verification should be enabled by default");
        assertEquals("JKS", sslProperties.getTrustStoreType(), "Default trust store type should be JKS");
        assertNull(sslProperties.getTrustStorePath(), "Trust store path should be null by default");
        assertNull(sslProperties.getTrustStorePassword(), "Trust store password should be null by default");
    }

    @Test
    void testSslPropertiesSetters() {
        SslProperties sslProperties = new SslProperties();
        
        sslProperties.setVerifyCertificates(false);
        sslProperties.setTrustStorePath("/path/to/truststore.jks");
        sslProperties.setTrustStorePassword("password");
        sslProperties.setTrustStoreType("PKCS12");
        
        assertFalse(sslProperties.isVerifyCertificates(), "SSL verification should be disabled");
        assertEquals("/path/to/truststore.jks", sslProperties.getTrustStorePath());
        assertEquals("password", sslProperties.getTrustStorePassword());
        assertEquals("PKCS12", sslProperties.getTrustStoreType());
    }

    @Test
    void testExternalServiceConfigCreation() {
        SslProperties sslProperties = new SslProperties();
        ExternalServiceConfig config = new ExternalServiceConfig(sslProperties);
        
        assertNotNull(config, "ExternalServiceConfig should be created successfully");
    }
}