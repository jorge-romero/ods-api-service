package org.opendevstack.apiservice.core.security.flow;

import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class OBOFlowValidator implements AuthFlowValidator {

    @Override
    public AuthType getSupportedFlow() {
        return AuthType.OBO;
    }

    @Override
    public boolean validate(Jwt jwt) {
        // OBO tokens must have a subject (user) and delegated scopes
        String sub = jwt.getClaimAsString("sub");
        String scp = jwt.getClaimAsString("scp");
        return sub != null && !sub.isBlank()
            && scp != null && !scp.isBlank();
    }
}
