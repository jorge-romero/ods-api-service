package org.opendevstack.apiservice.projectusers.service;

/**
 * Public constants for JWT claims used in membership request tokens.
 */
public final class JwtMembershipRequestClaims {
    private JwtMembershipRequestClaims() {}

    public static final String CLAIM_JOB_ID = "jobId";
    public static final String CLAIM_UIPATH_REFERENCE = "uipathReference";
    public static final String CLAIM_PROJECT_KEY = "projectKey";
    public static final String CLAIM_USER = "user";
    public static final String CLAIM_ENVIRONMENT = "environment";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_INITIATED_AT = "initiatedAt";
    public static final String CLAIM_INITIATED_BY = "initiatedBy";
}