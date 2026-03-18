package org.opendevstack.apiservice.project.facade;

import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;

public interface ProjectsFacade {

    CreateProjectResponse createProject(CreateProjectRequest request)
            throws ProjectCreationException, ProjectKeyGenerationException;

    CreateProjectResponse getProject(String projectKey);
}