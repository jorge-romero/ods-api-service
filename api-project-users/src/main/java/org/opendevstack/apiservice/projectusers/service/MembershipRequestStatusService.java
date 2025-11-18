package org.opendevstack.apiservice.projectusers.service;

import org.opendevstack.apiservice.projectusers.model.MembershipRequestStatusResponse;
import org.opendevstack.apiservice.projectusers.exception.ProjectUserException;

/**
 * Service for handling membership request status operations.
 */
public interface MembershipRequestStatusService {

    /**
     * Gets the current status of a membership request by request ID.
     *
     * @param requestId the request ID (JWT token)
     * @return the current status of the request
     * @throws ProjectUserException if the request ID is invalid or status cannot be retrieved
     */
    MembershipRequestStatusResponse getRequestStatus(String requestId);

    /**
     * Validates that the request token corresponds to the given project and user.
     *
     * @param requestId the request ID (JWT token)
     * @param projectKey the project identifier
     * @param user the user identifier
     * @return true if the token is valid for the specified project and user, false otherwise
     */
    boolean validateRequestToken(String requestId, String projectKey, String user);
}