package org.opendevstack.apiservice.core.contracts.policy;

/**
 * Well-known policy type constants used by core.
 * Modules are free to define their own policy type strings
 * without modifying this class.
 */
public final class PolicyTypes {

    private PolicyTypes() {}

    public static final String ALLOWED_CLIENTS = "ALLOWED_CLIENTS";
    public static final String SCOPE_REQUIRED = "SCOPE_REQUIRED";
}
