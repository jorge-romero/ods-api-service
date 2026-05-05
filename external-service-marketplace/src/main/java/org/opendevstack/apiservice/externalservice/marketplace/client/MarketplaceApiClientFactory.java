package org.opendevstack.apiservice.externalservice.marketplace.client;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceInstanceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceServiceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class MarketplaceApiClientFactory {

    private final MarketplaceServiceConfig configuration;
    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Constructor with dependency injection.
     *
     * @param configuration      Marketplace service configuration
     * @param restTemplateBuilder RestTemplate builder for creating HTTP clients
     */
    public MarketplaceApiClientFactory(MarketplaceServiceConfig configuration,
                                RestTemplateBuilder restTemplateBuilder) {
        this.configuration = configuration;
        this.restTemplateBuilder = restTemplateBuilder;

        log.info("MarketplaceApiClientFactory initialized with {} instance(s)",
                configuration.getInstances().size());
    }

    /**
     * Resolve the effective instance name.
     * <ul>
     *   <li>If the default instance is configured via {@code externalservices.marketplace.default-instance}, it is returned.</li>
     *   <li>Otherwise the first entry of the instances map is returned (insertion order).</li>
     *   <li>If no instances are configured at all, a {@link MarketplaceException} is thrown.</li>
     * </ul>
     *
     * @return The resolved instance name (never {@code null}/blank)
     * @throws MarketplaceException if no Marketplace instances are configured
     */
    public String getDefaultInstanceName() throws MarketplaceException {

        String defaultInstance = configuration.getDefaultInstance();
        if (defaultInstance != null && !defaultInstance.isBlank()) {
            return defaultInstance;
        }

        Map<String, ?> instances = configuration.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new MarketplaceException("No Marketplace instances configured");
        }

        return instances.keySet().iterator().next();
    }

    /**
     * Get a {@link MarketplaceApiClient} for a specific instance.
     * If {@code instanceName} is {@code null} or blank, this method will throw a {@link MarketplaceException} 
     * to avoid ambiguity. The caller should explicitly call {@link #getClient()} to get the default instance client 
     * in that case.
     *
     * @param instanceName Name of the Marketplace instance, or {@code null}/{@code ""} for the default
     * @return Configured MarketplaceApiClient
     * @throws MarketplaceException if the instance is not configured
     */
    public MarketplaceApiClient getClient(String instanceName) throws MarketplaceException {
        if (instanceName == null || instanceName.isBlank()) {
            throw new MarketplaceException(
                    String.format("Provide instance name. Available instances: %s",
                            configuration.getInstances().keySet()));
        }

        MarketplaceInstanceConfig instanceConfig = configuration.getInstances().get(instanceName);

        if (instanceConfig == null) {
            throw new MarketplaceException(
                    String.format("Marketplace instance '%s' is not configured. Available instances: %s",
                            instanceName, configuration.getInstances().keySet()));
        }

        log.info("Creating new MarketplaceApiClient for instance '{}'", instanceName);

        RestTemplate restTemplate = createRestTemplate(instanceConfig);
        return new MarketplaceApiClient(instanceName, instanceConfig, restTemplate);
    }

    /**
     * Get a {@link MarketplaceApiClient} for a specific instance with the given bearer token.
     *
     * @param instanceName Name of the Marketplace instance
     * @param bearerToken  Bearer token to use for authentication
     * @return Configured MarketplaceApiClient with the given bearer token
     * @throws MarketplaceException if the instance is not configured
     */
    public MarketplaceApiClient getClient(String instanceName, String bearerToken) throws MarketplaceException {
        MarketplaceApiClient client = getClient(instanceName);
        client.setBearerToken(bearerToken);
        return client;
    }

    /**
     * Get the default client, as determined by {@code externalservices.marketplace.default-instance}.
     * Falls back to the first configured instance when {@code default-instance} is not set.
     *
     * @return MarketplaceApiClient for the default instance
     * @throws MarketplaceException if no instances are configured
     */
    public MarketplaceApiClient getClient() throws MarketplaceException {
        String defaultInstanceName = getDefaultInstanceName();
        MarketplaceInstanceConfig instanceConfig = configuration.getInstances().get(defaultInstanceName);
        RestTemplate restTemplate = createRestTemplate(instanceConfig);

        return new MarketplaceApiClient(defaultInstanceName, instanceConfig, restTemplate);
    }

    /**
     * Get all available instance names.
     *
     * @return Set of configured instance names
     */
    public Set<String> getAvailableInstances() {
        return configuration.getInstances().keySet();
    }

    /**
     * Check if an instance is configured.
     *
     * @param instanceName Name of the instance to check
     * @return true if configured, false otherwise
     */
    public boolean hasInstance(String instanceName) {
        return configuration.getInstances().containsKey(instanceName);
    }

    /**
     * Create a configured RestTemplate for a Marketplace instance.
     *
     * @param config Configuration for the instance
     * @return Configured RestTemplate
     */
    private RestTemplate createRestTemplate(MarketplaceInstanceConfig config) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        SimpleClientHttpRequestFactory requestFactory;
        if (config.isTrustAllCertificates()) {
            log.warn("Trust all certificates is enabled for Marketplace API connection. "
                    + "This should only be used in development environments!");
            requestFactory = createTrustAllRequestFactory();
        } else {
            requestFactory = new SimpleClientHttpRequestFactory();
        }
        requestFactory.setConnectTimeout(config.getConnectionTimeout());
        requestFactory.setReadTimeout(config.getReadTimeout());
        restTemplate.setRequestFactory(requestFactory);

        return restTemplate;
    }

    /**
     * Builds a {@link SimpleClientHttpRequestFactory} that disables certificate and hostname
     * verification only for the connections opened by this factory (no JVM-global side effects).
     * WARNING: should only be used in development environments.
     */
    @SuppressWarnings({"java:S4830", "java:S5527"})
    private SimpleClientHttpRequestFactory createTrustAllRequestFactory() {
        final javax.net.ssl.SSLSocketFactory socketFactory;
        try {
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // Intentionally empty - dev only
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // Intentionally empty - dev only
                        }
                    }
            };
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustAllCertificates, new java.security.SecureRandom());
            socketFactory = context.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            log.error("Failed to configure SSL trust all certificates for Marketplace API", ex);
            return new SimpleClientHttpRequestFactory();
        }

        return new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws java.io.IOException {
                if (connection instanceof HttpsURLConnection httpsConnection) {
                    httpsConnection.setSSLSocketFactory(socketFactory);
                    httpsConnection.setHostnameVerifier((hostname, session) -> true);
                }
                super.prepareConnection(connection, httpMethod);
            }
        };
    }
}
