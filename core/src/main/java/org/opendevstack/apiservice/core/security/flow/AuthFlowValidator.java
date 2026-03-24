package org.opendevstack.apiservice.core.security.flow;

import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthFlowValidator {

    AuthFlowType getSupportedFlow();

    boolean validate(Jwt jwt);
}
