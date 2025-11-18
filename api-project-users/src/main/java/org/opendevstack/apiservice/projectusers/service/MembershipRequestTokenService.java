package org.opendevstack.apiservice.projectusers.service;

import org.opendevstack.apiservice.projectusers.exception.ProjectUserException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for handling membership request tokens.
 * Provides stateless request tracking using JWT tokens.
 */
public interface MembershipRequestTokenService {

    /**
     * Creates a request token containing all necessary information for status tracking.
     *
     * @param jobId the Ansible job ID
     * @param uipathReference the UIPath queue item reference (optional)
     * @param projectKey the project identifier
     * @param user the user identifier
     * @param environment the environment
     * @param role the user role
     * @param initiatedAt when the request was initiated
     * @param initiatedBy who initiated the request
     * @return encoded request token
     * @throws ProjectUserException if token creation fails
     */
    @SuppressWarnings("squid:S107")
    String createRequestToken(String jobId, String uipathReference, String projectKey, String user, 
                             String environment, String role, 
                             LocalDateTime initiatedAt, String initiatedBy);

    /**
     * Decodes and validates a request token.
     *
     * @param token the encoded request token
     * @return decoded token data
     * @throws ProjectUserException if token is invalid or expired
     */
    Map<String, Object> decodeRequestToken(String token);

    /**
     * Validates that a request token is valid and not expired.
     *
     * @param token the encoded request token
     * @return true if token is valid
     */
    boolean isValidToken(String token);

    /**
     * Extracts the job ID from a request token without full validation.
     * Used for quick lookups.
     *
     * @param token the encoded request token
     * @return job ID or null if token is invalid
     */
    String extractJobId(String token);
}