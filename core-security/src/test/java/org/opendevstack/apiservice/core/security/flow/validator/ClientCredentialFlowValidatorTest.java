package org.opendevstack.apiservice.core.security.flow.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientCredentialFlowValidatorTest {

    private ClientCredentialFlowValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ClientCredentialFlowValidator();
    }

    // --- v1 token (appid) ---

    @Test
    void validate_v1Token_valid() {
        Jwt jwt = buildJwt(Map.of(
                "appid", "client-id-v1",
                "aud", "api://my-app",
                "oid", "same-as-sub"
        ), "same-as-sub");
        assertTrue(validator.validate(jwt));
    }

    @Test
    void validate_v1Token_withScp_invalid() {
        Jwt jwt = buildJwt(Map.of(
                "appid", "client-id-v1",
                "aud", "api://my-app",
                "scp", "api.read",
                "oid", "same-as-sub"
        ), "same-as-sub");
        assertFalse(validator.validate(jwt));
    }

    @Test
    void validate_v1Token_withUpn_invalid() {
        Jwt jwt = buildJwt(Map.of(
                "appid", "client-id-v1",
                "aud", "api://my-app",
                "upn", "user@example.com",
                "oid", "same-as-sub"
        ), "same-as-sub");
        assertFalse(validator.validate(jwt));
    }

    @Test
    void validate_v1Token_subNotEqualOid_invalid() {
        Jwt jwt = buildJwt(Map.of(
                "appid", "client-id-v1",
                "aud", "api://my-app",
                "oid", "different-oid"
        ), "subject");
        assertFalse(validator.validate(jwt));
    }

    // --- v2 token (azp, no appid) ---

    @Test
    void validate_v2Token_valid() {
        Jwt jwt = buildJwt(Map.of(
                "azp", "client-id-v2",
                "aud", "api://my-app",
                "oid", "same-as-sub"
        ), "same-as-sub");
        assertTrue(validator.validate(jwt));
    }

    @Test
    void validate_v2Token_withScp_invalid() {
        Jwt jwt = buildJwt(Map.of(
                "azp", "client-id-v2",
                "aud", "api://my-app",
                "scp", "api.read",
                "oid", "same-as-sub"
        ), "same-as-sub");
        assertFalse(validator.validate(jwt));
    }

    @Test
    void validate_v2Token_withPreferredUsername_invalid() {
        Jwt jwt = buildJwt(Map.of(
                "azp", "client-id-v2",
                "aud", "api://my-app",
                "preferred_username", "user@example.com",
                "oid", "same-as-sub"
        ), "same-as-sub");
        assertFalse(validator.validate(jwt));
    }

    @Test
    void validate_v2Token_subNotEqualOid_invalid() {
        Jwt jwt = buildJwt(Map.of(
                "azp", "client-id-v2",
                "aud", "api://my-app",
                "oid", "different-oid"
        ), "subject");
        assertFalse(validator.validate(jwt));
    }

    // --- no client ID at all ---

    @Test
    void validate_noClientIdClaim_invalid() {
        Jwt jwt = buildJwt(Map.of(
                "aud", "api://my-app",
                "oid", "same-as-sub"
        ), "same-as-sub");
        assertFalse(validator.validate(jwt));
    }

    @Test
    void validate_missingAud_invalid() {
        Jwt jwt = buildJwt(Map.of(
                "appid", "client-id-v1",
                "oid", "same-as-sub"
        ), "same-as-sub");
        assertFalse(validator.validate(jwt));
    }

    private Jwt buildJwt(Map<String, Object> extraClaims, String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", subject)
                .claims(c -> c.putAll(extraClaims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
