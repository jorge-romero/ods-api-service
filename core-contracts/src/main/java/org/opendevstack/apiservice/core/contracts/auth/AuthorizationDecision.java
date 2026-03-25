package org.opendevstack.apiservice.core.contracts.auth;

public enum AuthorizationDecision {

    PERMIT,
    DENY,
    ABSTAIN;

    public static AuthorizationDecision combine(AuthorizationDecision a, AuthorizationDecision b) {
        if (a == DENY || b == DENY) {
            return DENY;
        }
        if (a == PERMIT || b == PERMIT) {
            return PERMIT;
        }
        return ABSTAIN;
    }
}
