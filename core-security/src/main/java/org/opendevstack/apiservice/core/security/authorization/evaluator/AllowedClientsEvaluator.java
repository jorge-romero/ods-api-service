package org.opendevstack.apiservice.core.security.authorization.evaluator;


import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.policy.PolicyEvaluator;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.opendevstack.apiservice.core.contracts.policy.PolicyTypes;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluates ALLOWED_CLIENTS policy rules.
 * Verifies that the clientId from the JWT is in the list of authorized clients.
 */
@Component
public class AllowedClientsEvaluator implements PolicyEvaluator {

    @Override
    public boolean supports(String policyType) {
        return PolicyTypes.ALLOWED_CLIENTS.equals(policyType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthorizationDecision evaluate(PolicyContext context) {
        PolicyRule rule = context.getActiveRule();
        if (rule == null || rule.getConfig() == null) {
            return AuthorizationDecision.ABSTAIN;
        }

        String clientId = context.getClientId();
        if (clientId == null) {
            return AuthorizationDecision.DENY;
        }

        List<String> allowedClients = (List<String>) rule.getConfig().get("allowedClients");
        if (allowedClients == null || allowedClients.isEmpty()) {
            return AuthorizationDecision.ABSTAIN;
        }

        return allowedClients.contains(clientId)
                ? AuthorizationDecision.PERMIT
                : AuthorizationDecision.DENY;
    }
}