package org.opendevstack.apiservice.core.security.flow;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.config.SecurityProperties;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientCredentialsFlowValidatorTest {

    private static final String APP_CLIENT_ID = "my-api-client-id";

    private ClientCredentialsFlowValidator validatorWithAudience(String audience) {
        SecurityProperties props = new SecurityProperties();
        props.setAudience(audience);
        return new ClientCredentialsFlowValidator(props);
    }

    private Jwt buildJwt(String azp, String scp, List<String> aud) {
        Map<String, Object> headers = Map.of("alg", "RS256");
        Map<String, Object> claims = new java.util.HashMap<>();
        if (azp != null) {
            claims.put("azp", azp);
        }
        if (scp != null) {
            claims.put("scp", scp);
        }
        if (aud != null) {
            claims.put("aud", aud);
        }
        claims.put("iss", "https://login.microsoftonline.com/test-tenant/v2.0");
        claims.put("sub", "some-subject");
        return Jwt.withTokenValue("token")
                .headers(h -> h.putAll(headers))
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    // ── audience check disabled (no audience configured) ─────────────────────

    @Test
    void validToken_withNoAudienceConfigured_passes() {
        ClientCredentialsFlowValidator validator = validatorWithAudience(null);

        Jwt jwt = buildJwt("some-client", null, List.of("any-audience"));

        assertTrue(validator.validate(jwt));
    }

    @Test
    void validToken_withBlankAudienceConfigured_passes() {
        ClientCredentialsFlowValidator validator = validatorWithAudience("   ");

        Jwt jwt = buildJwt("some-client", null, List.of("any-audience"));

        assertTrue(validator.validate(jwt));
    }

    // ── audience check enabled ────────────────────────────────────────────────

    @Test
    void validToken_withMatchingAudience_passes() {
        ClientCredentialsFlowValidator validator = validatorWithAudience(APP_CLIENT_ID);

        Jwt jwt = buildJwt("caller-client", null, List.of(APP_CLIENT_ID));

        assertTrue(validator.validate(jwt));
    }

    @Test
    void validToken_withMatchingAudienceAmongMultiple_passes() {
        ClientCredentialsFlowValidator validator = validatorWithAudience(APP_CLIENT_ID);

        Jwt jwt = buildJwt("caller-client", null, List.of("other-api", APP_CLIENT_ID));

        assertTrue(validator.validate(jwt));
    }

    @Test
    void token_withWrongAudience_fails() {
        ClientCredentialsFlowValidator validator = validatorWithAudience(APP_CLIENT_ID);

        Jwt jwt = buildJwt("caller-client", null, List.of("some-other-api"));

        assertFalse(validator.validate(jwt));
    }

    @Test
    void token_withMissingAudience_fails() {
        ClientCredentialsFlowValidator validator = validatorWithAudience(APP_CLIENT_ID);

        Jwt jwt = buildJwt("caller-client", null, null);

        assertFalse(validator.validate(jwt));
    }

    @Test
    void token_withEmptyAudienceList_fails() {
        ClientCredentialsFlowValidator validator = validatorWithAudience(APP_CLIENT_ID);

        Jwt jwt = buildJwt("caller-client", null, List.of());

        assertFalse(validator.validate(jwt));
    }

    // ── existing azp / scp rules still apply ─────────────────────────────────

    @Test
    void token_withMissingAzp_failsEvenWithCorrectAudience() {
        ClientCredentialsFlowValidator validator = validatorWithAudience(APP_CLIENT_ID);

        Jwt jwt = buildJwt(null, null, List.of(APP_CLIENT_ID));

        assertFalse(validator.validate(jwt));
    }

    @Test
    void token_withDelegatedScope_failsEvenWithCorrectAudience() {
        ClientCredentialsFlowValidator validator = validatorWithAudience(APP_CLIENT_ID);

        Jwt jwt = buildJwt("caller-client", "openid profile", List.of(APP_CLIENT_ID));

        assertFalse(validator.validate(jwt));
    }

}
