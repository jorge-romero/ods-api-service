package org.opendevstack.apiservice.projectusers.controller;

import org.opendevstack.apiservice.projectusers.model.AddUserToProjectRequest;
import org.opendevstack.apiservice.projectusers.model.ApiResponseMembershipRequestResponse;
import org.opendevstack.apiservice.projectusers.model.ApiResponseMembershipRequestStatusResponse;
import org.opendevstack.apiservice.projectusers.model.MembershipRequestResponse;
import org.opendevstack.apiservice.projectusers.model.MembershipRequestStatusResponse;
import org.opendevstack.apiservice.projectusers.api.ProjectUsersApi;
import org.opendevstack.apiservice.projectusers.exception.InvalidTokenException;
import org.opendevstack.apiservice.projectusers.service.MembershipRequestStatusService;
import org.opendevstack.apiservice.projectusers.service.ProjectUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing project users and their roles.
 * Provides endpoints for adding, removing, updating, and querying users in projects.
 */
@RestController
@RequestMapping("/api/v1")
public class ProjectUserController implements ProjectUsersApi{

    private static final Logger logger = LoggerFactory.getLogger(ProjectUserController.class);

    private final ProjectUserService projectUserService;
    private final MembershipRequestStatusService statusService;

    public ProjectUserController(ProjectUserService projectUserService,
                               MembershipRequestStatusService statusService) {
        this.projectUserService = projectUserService;
        this.statusService = statusService;
    }

    /**
     * Request the membership for a user to join a project with a specific role.
     *
     * @param projectKey the project identifier
     * @param addUserToProjectRequest the request containing user details and role
     * @return API response with the created project user
     */
    @Override
    public ResponseEntity<ApiResponseMembershipRequestResponse> triggerMembershipRequest(
            String projectKey,
            AddUserToProjectRequest addUserToProjectRequest) {
        
        logger.info("Triggering membership request for account '{}' of user '{}' to project '{}' with role '{}'", 
            addUserToProjectRequest.getAccount(), addUserToProjectRequest.getUser(), projectKey, addUserToProjectRequest.getRole());
        
        MembershipRequestResponse response = projectUserService.addUserToProject(projectKey, addUserToProjectRequest);
        
        ApiResponseMembershipRequestResponse apiResponse = new ApiResponseMembershipRequestResponse();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Membership request triggered successfully");
        apiResponse.setData(response);
        apiResponse.setTimestamp(java.time.OffsetDateTime.now());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @Override
    public ResponseEntity<ApiResponseMembershipRequestStatusResponse> getRequestStatus(
            String projectKey,
            String user,
            String requestId) {
        
        logger.info("Getting status for request '{}' - project '{}', user '{}'", requestId, projectKey, user);
        
        // Check the request is valid for the given project and user
        if (!statusService.validateRequestToken(requestId, projectKey, user)) {
            throw new InvalidTokenException("Invalid request ID for the specified project and user");
        }

        MembershipRequestStatusResponse statusResponse = statusService.getRequestStatus(requestId);
        
        ApiResponseMembershipRequestStatusResponse apiResponse = new ApiResponseMembershipRequestStatusResponse();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Request status retrieved successfully");
        apiResponse.setData(statusResponse);
        apiResponse.setTimestamp(java.time.OffsetDateTime.now());
        
        return ResponseEntity.ok(apiResponse);
    }


}