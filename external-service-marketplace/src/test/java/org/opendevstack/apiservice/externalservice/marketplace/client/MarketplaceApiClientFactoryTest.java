package org.opendevstack.apiservice.externalservice.marketplace.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceInstanceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceServiceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MarketplaceApiClientFactory}.
 * Focuses on the default-instance resolution logic introduced in {@code resolveInstanceName}.
 */
@ExtendWith(MockitoExtension.class)
class MarketplaceApiClientFactoryTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private MarketplaceServiceConfig configuration;

    @BeforeEach
    void setUp() {
        configuration = new MarketplaceServiceConfig();
        lenient().when(restTemplateBuilder.build()).thenReturn(restTemplate);
    }

    private MarketplaceApiClientFactory factory() {
        return new MarketplaceApiClientFactory(configuration, restTemplateBuilder);
    }

    // -------------------------------------------------------------------------
    // resolveInstanceName → configured default
    // -------------------------------------------------------------------------

    @Test
    void resolveInstanceName_null_returnsConfiguredDefaultInstance() throws MarketplaceException {
        configuration.setDefaultInstance("prod");

        assertEquals("prod", factory().getDefaultInstanceName());
    }

    // -------------------------------------------------------------------------
    // resolveInstanceName – without default → fallback to first instance
    // -------------------------------------------------------------------------

    @Test
    void resolveInstanceName_null_noDefaultConfigured_returnsFirstInstance() throws MarketplaceException {
        // LinkedHashMap preserves insertion order → "alpha" is first
        Map<String, MarketplaceInstanceConfig> instances = new LinkedHashMap<>();
        instances.put("alpha", config("https://marketplace.example.com"));
        instances.put("beta",  config("https://marketplace-beta.example.com"));
        configuration.setInstances(instances);

        assertEquals("alpha", factory().getDefaultInstanceName());
    }

    // -------------------------------------------------------------------------
    // resolveInstanceName – no instances at all → exception
    // -------------------------------------------------------------------------

    @Test
    void resolveInstanceName_null_noInstancesConfigured_throwsMarketplaceException() {
        // no instances set → empty map
        MarketplaceApiClientFactory f = factory();

        MarketplaceException ex = assertThrows(MarketplaceException.class, () -> f.getDefaultInstanceName());
        assertTrue(ex.getMessage().toLowerCase().contains("no marketplace instances configured"),
                "Expected 'no marketplace instances configured' in: " + ex.getMessage());
    }

    @Test
    void getClient_null_throwsMarketplaceException() throws MarketplaceException {
        MarketplaceException ex = assertThrows(MarketplaceException.class, () -> factory().getClient(null));
        assertTrue(ex.getMessage().toLowerCase().contains("provide instance name"),
                "Expected 'provide instance name' in: " + ex.getMessage());
    }

    @Test
    void getClient_blank_throwsMarketplaceException() throws MarketplaceException {
        MarketplaceException ex = assertThrows(MarketplaceException.class, () -> factory().getClient(""));
        assertTrue(ex.getMessage().toLowerCase().contains("provide instance name"),
                "Expected 'provide instance name' in: " + ex.getMessage());
    }

    @Test
    void getClient_unknownInstance_throwsMarketplaceException() {
        configuration.setInstances(Map.of("dev", config("https://marketplace.dev.example.com")));

        MarketplaceException ex = assertThrows(MarketplaceException.class,
                () -> factory().getClient("nonexistent"));
        assertTrue(ex.getMessage().contains("not configured"));
        assertTrue(ex.getMessage().contains("nonexistent"));
    }

    @Test
    void getClient_returnsClientForConfiguredDefaultInstance() throws MarketplaceException {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        configuration.setDefaultInstance("prod");
        configuration.setInstances(orderedMap("dev", "prod"));

        MarketplaceApiClient client = factory().getClient();

        assertNotNull(client);
        assertEquals("prod", client.getInstanceName());
    }

    @Test
    void getClient_noDefaultConfigured_returnsFirstInstance() throws MarketplaceException {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        Map<String, MarketplaceInstanceConfig> instances = new LinkedHashMap<>();
        instances.put("alpha", config("https://marketplace.example.com"));
        instances.put("beta",  config("https://marketplace-beta.example.com"));
        configuration.setInstances(instances);

        MarketplaceApiClient client = factory().getClient();

        assertNotNull(client);
        assertEquals("alpha", client.getInstanceName());
    }

    @Test
    void getClient_noInstancesConfigured_throwsMarketplaceException() {
        MarketplaceException ex = assertThrows(MarketplaceException.class, () -> factory().getClient());
        assertTrue(ex.getMessage().toLowerCase().contains("no marketplace instances configured"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static MarketplaceInstanceConfig config(String baseUrl) {
        MarketplaceInstanceConfig c = new MarketplaceInstanceConfig();
        c.setProjectComponentsBaseUrl(baseUrl);
        c.setProvisionerActionsBaseUrl(baseUrl);
        return c;
    }

    /** Creates a LinkedHashMap with two configs using their names as base-url stems. */
    private static Map<String, MarketplaceInstanceConfig> orderedMap(String first, String second) {
        Map<String, MarketplaceInstanceConfig> m = new LinkedHashMap<>();
        m.put(first,  config("https://" + first + ".example.com"));
        m.put(second, config("https://" + second + ".example.com"));
        return m;
    }
}