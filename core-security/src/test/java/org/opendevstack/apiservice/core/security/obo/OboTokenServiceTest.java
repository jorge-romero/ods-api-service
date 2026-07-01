package org.opendevstack.apiservice.core.security.obo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OboTokenServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private OboTokenProperties properties;

    private OboTokenService sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        properties = new OboTokenProperties();
        properties.setTokenUrl("https://login.microsoftonline.com/tenant-id/oauth2/v2.0/token");
        properties.setClientId("test-client-id");
        properties.setClientSecret("test-client-secret");
        sut = new OboTokenService(properties, restTemplate);
    }

    @Test
    void exchange_token_returns_access_token_on_success() {
        // GIVEN
        String assertion = "jwt-assertion-value";
        String scope = "api://target-app/Api.Access";
        OboTokenResponse tokenResponse = new OboTokenResponse();
        tokenResponse.setAccessToken("obo-access-token");
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(3600);
        tokenResponse.setScope(scope);

        when(restTemplate.postForEntity(eq(properties.getTokenUrl()), any(HttpEntity.class), eq(OboTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        // WHEN
        String result = sut.exchangeToken(assertion, scope);

        // THEN
        assertEquals("obo-access-token", result);
        verify(restTemplate).postForEntity(eq(properties.getTokenUrl()), any(HttpEntity.class), eq(OboTokenResponse.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void exchange_token_sends_correct_form_parameters() {
        // GIVEN
        String assertion = "my-jwt";
        String scope = "api://app/scope";
        OboTokenResponse tokenResponse = new OboTokenResponse();
        tokenResponse.setAccessToken("token");

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(OboTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        // WHEN
        sut.exchangeToken(assertion, scope);

        // THEN
        MultiValueMap<String, String> body = captor.getValue().getBody();
        assertNotNull(body);
        assertEquals("urn:ietf:params:oauth:grant-type:jwt-bearer", body.getFirst("grant_type"));
        assertEquals("test-client-id", body.getFirst("client_id"));
        assertEquals("test-client-secret", body.getFirst("client_secret"));
        assertEquals("my-jwt", body.getFirst("assertion"));
        assertEquals("on_behalf_of", body.getFirst("requested_token_use"));
        assertEquals("api://app/scope", body.getFirst("scope"));
    }

    @Test
    void exchange_token_throws_obo_exception_when_response_body_is_null() {
        // GIVEN
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OboTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // WHEN / THEN
        OboTokenException ex = assertThrows(OboTokenException.class,
                () -> sut.exchangeToken("jwt", "scope"));
        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    void exchange_token_throws_obo_exception_when_access_token_is_null() {
        // GIVEN
        OboTokenResponse response = new OboTokenResponse();
        response.setAccessToken(null);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OboTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // WHEN / THEN
        assertThrows(OboTokenException.class, () -> sut.exchangeToken("jwt", "scope"));
    }

    @Test
    void exchange_token_throws_obo_exception_on_rest_client_error() {
        // GIVEN
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(OboTokenResponse.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // WHEN / THEN
        OboTokenException ex = assertThrows(OboTokenException.class,
                () -> sut.exchangeToken("jwt", "scope"));
        assertTrue(ex.getMessage().contains("Connection refused"));
    }
}
