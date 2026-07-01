package org.opendevstack.apiservice.core.security.flow;

import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthFlowValidator {

    AuthType getSupportedFlow();

    boolean validate(Jwt jwt);
}