package org.opendevstack.apiservice.externalservice.aap.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

/**
 * Configuration class for external service components.
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(SslProperties.class)
public class ExternalServiceConfig {

    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceConfig.class);

    private final SslProperties sslProperties;

    public ExternalServiceConfig(@Qualifier("aapSslProperties") SslProperties sslProperties) {
        this.sslProperties = sslProperties;
    }

    /**
     * Creates a RestTemplate bean for HTTP client operations with configurable SSL settings.
     *
     * @return RestTemplate instance with SSL configuration
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        if (!sslProperties.isVerifyCertificates()) {
            logger.warn("SSL certificate verification is DISABLED - this should only be used in development environments");
            return createInsecureRestTemplate();
        } else {
            logger.info("SSL certificate verification is ENABLED");
            return createSecureRestTemplate();
        }
    }

    private RestTemplate createInsecureRestTemplate() {
        try {
            // Create a trust manager that accepts all certificates
            // WARNING: This is insecure and should only be used in development environments
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { 
                        return new X509Certificate[0]; // Return empty array instead of null
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { 
                        // Intentionally empty - accepts all client certificates (insecure)
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { 
                        // Intentionally empty - accepts all server certificates (insecure)
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // Create hostname verifier that accepts all hostnames (insecure)
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Create a custom request factory that uses our SSL configuration
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                    if (connection instanceof HttpsURLConnection httpsConnection) {
                        httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                        httpsConnection.setHostnameVerifier(allHostsValid);
                    }
                    super.prepareConnection(connection, httpMethod);
                }
            };

            return new RestTemplate(requestFactory);
            
        } catch (GeneralSecurityException e) {
            logger.warn("Failed to create insecure RestTemplate, falling back to default: {}", e.getMessage());
            return new RestTemplate();
        }
    }

    private RestTemplate createSecureRestTemplate() {
        try {
            // If custom trust store is provided, configure it
            if (StringUtils.hasText(sslProperties.getTrustStorePath())) {
                logger.info("Custom trust store specified: {} (custom trust store support can be added in future versions)", 
                    sslProperties.getTrustStorePath());
            }
            
            // Return default RestTemplate with system SSL settings
            return new RestTemplate();
            
        } catch (Exception e) {
            logger.warn("Failed to create secure RestTemplate with custom trust store, using default: {}", e.getMessage());
            return new RestTemplate();
        }
    }
}
