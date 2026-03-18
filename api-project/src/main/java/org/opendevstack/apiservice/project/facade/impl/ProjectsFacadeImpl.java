package org.opendevstack.apiservice.project.facade.impl;

import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.mapper.ProjectMapper;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.stereotype.Component;

@Component("apiProjectFacadeImpl")
public class ProjectsFacadeImpl implements ProjectsFacade {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    public ProjectsFacadeImpl(
            ProjectService projectService,
            ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    @Override
    public CreateProjectResponse createProject(CreateProjectRequest request)
            throws ProjectCreationException, ProjectKeyGenerationException {
        return projectMapper.toApiResponse(projectService.createProject(projectMapper.toServiceRequest(request)));
    }

    @Override
    public CreateProjectResponse getProject(String projectKey) {
        return projectMapper.toApiResponse(projectService.getProject(projectKey));
    }
}