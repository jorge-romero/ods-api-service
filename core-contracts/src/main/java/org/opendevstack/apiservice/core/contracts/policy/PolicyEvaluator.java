package org.opendevstack.apiservice.core.contracts.policy;

import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;

public interface PolicyEvaluator {

    boolean supports(String policyType);

    AuthorizationDecision evaluate(PolicyContext context);
}
