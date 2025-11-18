package org.opendevstack.apiservice.externalservice.projectsinfoservice.service;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;

/**
 * Service interface for integrating with projects info service
 * This interface provides a generic way to consume the service.
 */
public interface ProjectsInfoService {

    /**
     * Retrieves the platforms associated with a given project.
     *
     * @param projectKey the key of the project you want to check
     * @return the platforms details associated with the project
     * @throws ProjectsInfoServiceException if workflow execution fails
     */
    Platforms getProjectPlatforms(String projectKey) throws ProjectsInfoServiceException;

    /**
     * Validates connection to the projects info service.
     *
     * @return true if connection is valid, false otherwise
     */
    boolean validateConnection();

    /**
     * Checks if the projects info service is healthy and reachable.
     * This method is used by health indicators and should not throw exceptions.
     *
     * @return true if the service is healthy, false otherwise
     */
    boolean isHealthy();


}
