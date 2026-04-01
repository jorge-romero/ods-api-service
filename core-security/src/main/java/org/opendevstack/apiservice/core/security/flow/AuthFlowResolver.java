package org.opendevstack.apiservice.core.security.flow;

import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthFlowResolver {

    public AuthType resolve(Jwt jwt) {
        if (jwt == null) {
            return AuthType.NONE;
        }
        // In Azure AD, OBO tokens have a "scp" claim (delegated permissions).
        // Client credentials tokens have a "roles" claim but no "scp".
        String scp = jwt.getClaimAsString("scp");
        if (scp != null && !scp.isBlank()) {
            return AuthType.OBO;
        }
        return AuthType.CLIENT_CREDENTIALS;
    }
}