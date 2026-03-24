package org.opendevstack.apiservice.core.security.flow;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class OBOFlowValidator implements AuthFlowValidator {

    @Override
    public AuthFlowType getSupportedFlow() {
        return AuthFlowType.OBO;
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
