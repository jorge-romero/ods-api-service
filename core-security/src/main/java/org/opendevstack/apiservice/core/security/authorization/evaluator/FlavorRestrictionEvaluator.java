package org.opendevstack.apiservice.core.security.authorization.evaluator;

import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.policy.PolicyEvaluator;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.opendevstack.apiservice.core.contracts.policy.PolicyTypes;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Evaluates FLAVOR_RESTRICTION policy rules.
 * Verifies that the requested flavor in the request body is in the list
 * of allowed flavors for the client.
 */
@Component
public class FlavorRestrictionEvaluator implements PolicyEvaluator {

    @Override
    public boolean supports(String policyType) {
        return PolicyTypes.FLAVOR_RESTRICTION.equals(policyType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthorizationDecision evaluate(PolicyContext context) {
            PolicyRule rule = context.getActiveRule();
        if (rule == null || rule.getConfig() == null) {
            return AuthorizationDecision.ABSTAIN;
        }

        Map<String, Object> body = context.getRequestBody();
        if (body == null) {
            return AuthorizationDecision.ABSTAIN;
        }

        String requestedFlavor = (String) body.get("projectFlavor");
        if (requestedFlavor == null || requestedFlavor.trim().isEmpty()) {
            return AuthorizationDecision.ABSTAIN;
        }

        List<String> allowedFlavors = (List<String>) rule.getConfig().get("allowedFlavors");
        if (allowedFlavors == null || allowedFlavors.isEmpty()) {
            return AuthorizationDecision.ABSTAIN;
        }

        return allowedFlavors.contains(requestedFlavor)
                ? AuthorizationDecision.PERMIT
                : AuthorizationDecision.DENY;
    }
}