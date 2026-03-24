package org.opendevstack.apiservice.core.security.flow;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthFlowResolver {

    public AuthFlowType resolve(Jwt jwt) {
        if (jwt == null) {
            return AuthFlowType.ANONYMOUS;
        }
        // In Azure AD, OBO tokens have a "scp" claim (delegated permissions).
        // Client credentials tokens have a "roles" claim but no "scp".
        String scp = jwt.getClaimAsString("scp");
        if (scp != null && !scp.isBlank()) {
            return AuthFlowType.OBO;
        }
        return AuthFlowType.CLIENT_CREDENTIALS;
    }
}
