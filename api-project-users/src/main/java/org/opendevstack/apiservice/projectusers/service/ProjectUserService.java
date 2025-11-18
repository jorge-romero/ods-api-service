package org.opendevstack.apiservice.projectusers.service;

import org.opendevstack.apiservice.projectusers.model.AddUserToProjectRequest;
import org.opendevstack.apiservice.projectusers.model.MembershipRequestResponse;
import org.opendevstack.apiservice.projectusers.exception.ProjectUserException;

/**
 * Service interface for managing project users and their roles.
 */
public interface ProjectUserService {

    /**
     * Adds a user to a project with the specified role.
     *
     * @param projectKey the project identifier
     * @param request the request containing user details and role
     * @return the membership request response with tracking information
     * @throws ProjectUserException if the operation fails
     */
    MembershipRequestResponse addUserToProject(String projectKey, AddUserToProjectRequest request);

}
