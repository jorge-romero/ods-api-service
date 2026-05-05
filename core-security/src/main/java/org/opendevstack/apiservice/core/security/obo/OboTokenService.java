package org.opendevstack.apiservice.core.security.obo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OboTokenService {

    private static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    private final OboTokenProperties properties;
    private final RestTemplate restTemplate;

    @Autowired
    public OboTokenService(OboTokenProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    /**
     * Exchanges the given JWT assertion for an OBO (On-Behalf-Of) access token.
     *
     * @param assertion the incoming JWT token value (from the original request)
     * @param scope     the target API scope (e.g. {@code api://<app-id>/Api.Access})
     * @return the OBO access token string
     * @throws OboTokenException if the token exchange fails
     */
    public String exchangeToken(String assertion, String scope) {
        log.debug("Exchanging JWT for OBO token with scope '{}'", scope);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        body.add("assertion", assertion);
        body.add("requested_token_use", "on_behalf_of");
        body.add("scope", scope);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<OboTokenResponse> response =
                    restTemplate.postForEntity(properties.getTokenUrl(), request, OboTokenResponse.class);

            if (response.getBody() == null || response.getBody().getAccessToken() == null) {
                throw new OboTokenException("OBO token response body or access_token is null");
            }

            log.debug("OBO token obtained successfully, expires in {} seconds", response.getBody().getExpiresIn());
            return response.getBody().getAccessToken();
        } catch (RestClientException e) {
            throw new OboTokenException("Failed to exchange JWT for OBO token: " + e.getMessage(), e);
        }
    }
}
