package org.opendevstack.apiservice.core.security.flow.validator;

import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.security.flow.AuthFlowValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class OboFlowValidator implements AuthFlowValidator {

    @Override
    public AuthType getSupportedFlow() {
        return AuthType.OBO;
    }

    @Override
    public boolean validate(Jwt jwt) {
        String scp = jwt.getClaimAsString("scp");
        if (scp == null || scp.isBlank()) {
            return false;
        }
        
        String roles = jwt.getClaimAsString("roles");
        if (roles != null && !roles.isBlank()) {
            return false;
        }
        
        String user = jwt.getClaimAsString("upn");
        if (user == null || user.isBlank()) {
            user = jwt.getClaimAsString("preferred_username");
        }
        
        if (user == null || user.isBlank()) {
            user = jwt.getSubject();
        }
        
        return user != null && !user.isBlank();
    }
    
}
