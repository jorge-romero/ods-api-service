package org.opendevstack.apiservice.externalservice.uipath.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
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
 * Configuration class for UIPath service components.
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(UiPathProperties.class)
public class UiPathServiceConfig {

    private static final Logger logger = LoggerFactory.getLogger(UiPathServiceConfig.class);

    private final UiPathProperties uiPathProperties;

    public UiPathServiceConfig(@org.springframework.beans.factory.annotation.Qualifier("uiPathOrchestratorProperties") UiPathProperties uiPathProperties) {
        this.uiPathProperties = uiPathProperties;
    }

    /**
     * Creates a RestTemplate bean for HTTP client operations with configurable SSL settings.
     * Uses a different bean name to avoid conflicts with other RestTemplate beans.
     *
     * @return RestTemplate instance with SSL configuration
     */
    @Bean(name = "uiPathRestTemplate")
    public RestTemplate uiPathRestTemplate() {
        if (!uiPathProperties.getSsl().isVerifyCertificates()) {
            logger.warn("UIPath SSL certificate verification is DISABLED - this should only be used in development environments");
            return createInsecureRestTemplate();
        } else {
            logger.info("UIPath SSL certificate verification is ENABLED");
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
                        return new X509Certificate[0];
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

            requestFactory.setConnectTimeout(uiPathProperties.getTimeout());
            requestFactory.setReadTimeout(uiPathProperties.getTimeout());

            return new RestTemplate(requestFactory);

        } catch (GeneralSecurityException e) {
            logger.warn("Failed to create insecure RestTemplate, falling back to default: {}", e.getMessage());
            return createSecureRestTemplate();
        }
    }

    private RestTemplate createSecureRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(uiPathProperties.getTimeout());
        requestFactory.setReadTimeout(uiPathProperties.getTimeout());
        return new RestTemplate(requestFactory);
    }
}
