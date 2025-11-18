package org.opendevstack.apiservice.projectusers.service.impl;

import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.projectusers.exception.AutomationPlatformException;
import org.opendevstack.apiservice.projectusers.exception.ProjectNotFoundException;
import org.opendevstack.apiservice.projectusers.model.AddUserToProjectRequest;
import org.opendevstack.apiservice.projectusers.model.MembershipRequestResponse;
import org.opendevstack.apiservice.projectusers.service.MembershipRequestTokenService;
import org.opendevstack.apiservice.projectusers.service.ProjectUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation of ProjectUserService that manages project users and integrates with automation platform.
 * This is a stateless implementation that uses the automation platform for persistence.
 */
@Service("projectUserService")
public class ProjectUserServiceImpl implements ProjectUserService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectUserServiceImpl.class);
    
    @Value("${apis.project-users.ansible-workflow-name}")
    private String addUserWorkflow;

    private final AutomationPlatformService automationPlatformService;
    private final MembershipRequestTokenService tokenService;

    public ProjectUserServiceImpl(AutomationPlatformService automationPlatformService,
                                 MembershipRequestTokenService membershipRequestTokenService) {
        this.automationPlatformService = automationPlatformService;
        this.tokenService = membershipRequestTokenService;
    }

    @Override
    public MembershipRequestResponse addUserToProject(String projectKey, AddUserToProjectRequest request) {
        logger.info("Adding user '{}' to project '{}' with role '{}'", request.getUser(), projectKey, request.getRole());

        // Validate project exists (this would typically be a call to a project service)
        validateProject(projectKey);

        try {
            // Prepare parameters for automation platform
            // Generate UIPath reference in format: project_user_role
            String uipathReference = String.format("%s_%s_%s", 
                projectKey, 
                request.getUser(), 
                request.getRole());            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("project_key", projectKey);
            parameters.put("environment", request.getEnvironment());
            parameters.put("requested_for", request.getUser());
            parameters.put("selected_account", request.getAccount() != null ? request.getAccount() : request.getUser());
            parameters.put("access_to_be_granted", request.getRole());
            parameters.put("comments", request.getComments());
            parameters.put("reference", uipathReference);

            // Execute workflow on automation platform
            AutomationExecutionResult result = automationPlatformService.executeWorkflow(addUserWorkflow, parameters);

            if (result.isSuccessful()) {               
                // Create request token for status tracking
                String requestId = tokenService.createRequestToken(
                    result.getJobId(),
                    uipathReference,
                    projectKey,
                    request.getUser(),
                    request.getEnvironment(),
                    request.getRole(),
                    java.time.LocalDateTime.now(),
                    getCurrentUser()
                );

                logger.info("Successfully triggered membership request for user '{}' to project '{}' with job ID: {} and request ID: {}", 
                    request.getUser(), projectKey, result.getJobId(), requestId);

                // Create response with request tracking information
                MembershipRequestResponse response = new MembershipRequestResponse();
                response.setRequestId(requestId);
                response.setProject(parameters.get("project_key").toString());
                response.setEnvironment(parameters.get("environment").toString());
                response.setUser(parameters.get("requested_for").toString());
                response.setAccount(parameters.get("selected_account").toString());
                response.setRole(parameters.get("access_to_be_granted").toString());
                response.setRequestedAt(java.time.OffsetDateTime.now());
                response.setStatus(MembershipRequestResponse.StatusEnum.PENDING);
                
                return response;
            } else {
                throw new AutomationPlatformException(
                    "Failed to add user through automation platform: " + result.getMessage(), null);
            }

        } catch (org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException e) {
            logger.error("Failed to add user '{}' to project '{}': {}", request.getUser(), projectKey, e.getMessage(), e);
            throw new AutomationPlatformException(
                "Automation platform execution failed", e);
        }
    }


    private void validateProject(String projectKey) {
        // In a real implementation, this would validate against a project service
        // In our case interact with Jira to check if the project exists.
        if (projectKey == null || projectKey.trim().isEmpty()) {
            logger.error("Invalid project key: {}", projectKey);
            throw new ProjectNotFoundException(projectKey);
        }
        // Add additional validation logic as needed
    }


    private static final String DEFAULT_USER = "system";

    private String getCurrentUser() {
        // In a real implementation, this would get the current authenticated user
        return DEFAULT_USER;
    }
}
