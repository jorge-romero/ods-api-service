package org.opendevstack.apiservice.core.security.flow;

import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.config.SecurityProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientCredentialsFlowValidator implements AuthFlowValidator {

    private final SecurityProperties securityProperties;

    public ClientCredentialsFlowValidator(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public AuthType getSupportedFlow() {
        return AuthType.CLIENT_CREDENTIALS;
    }

    @Override
    public boolean validate(Jwt jwt) {
        // Client credentials tokens: azp must be present, no delegated scp
        String azp = jwt.getClaimAsString("azp");
        String scp = jwt.getClaimAsString("scp");
        if (azp == null || azp.isBlank()) {
            return false;
        }
        if (scp != null && !scp.isBlank()) {
            return false;
        }
        return audienceMatches(jwt);
    }

    /**
     * If {@code app.security.audience} is configured, the token's {@code aud} claim
     * must contain that value. When the property is absent or blank the check is skipped.
     */
    private boolean audienceMatches(Jwt jwt) {
        String requiredAudience = securityProperties.getAudience();
        if (requiredAudience == null || requiredAudience.isBlank()) {
            return true;
        }
        List<String> tokenAudiences = jwt.getAudience();
        return tokenAudiences != null && tokenAudiences.contains(requiredAudience);
    }

}
