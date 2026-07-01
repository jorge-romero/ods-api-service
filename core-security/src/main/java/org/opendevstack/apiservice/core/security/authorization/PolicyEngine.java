package org.opendevstack.apiservice.core.security.authorization;

import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.policy.PolicyEvaluator;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PolicyEngine {

    private final List<PolicyEvaluator> evaluators;

    public PolicyEngine(List<PolicyEvaluator> evaluators) {
        this.evaluators = evaluators;
    }

    public AuthorizationDecision evaluate(PolicyContext context, List<PolicyRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return AuthorizationDecision.DENY;
        }

        AuthorizationDecision finalDecision = AuthorizationDecision.ABSTAIN;

        for (PolicyRule rule : rules) {
            PolicyEvaluator evaluator = evaluators.stream()
                    .filter(e -> e.supports(rule.getPolicyType()))
                    .findFirst()
                    .orElse(null);

            if (evaluator == null) {
                continue;
            }

            AuthorizationDecision decision = evaluator.evaluate(context.withRule(rule));
            finalDecision = AuthorizationDecision.combine(finalDecision, decision);

            if (finalDecision == AuthorizationDecision.DENY) {
                return AuthorizationDecision.DENY;
            }
        }

        return finalDecision == AuthorizationDecision.ABSTAIN
                ? AuthorizationDecision.PERMIT
                : finalDecision;
    }
}