package org.opendevstack.apiservice.project.facade.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.mapper.AutomationParametersMapper;
import org.opendevstack.apiservice.project.mapper.ProjectCreationResponseMapper;
import org.opendevstack.apiservice.project.mapper.ProjectMapper;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.project.model.UpdateProjectRequest;
import org.opendevstack.apiservice.project.service.ClientAppService;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component("apiProjectFacadeImpl")
@Slf4j
public class ProjectsFacadeImpl implements ProjectsFacade {

    @Value("${apis.projects.ansible-workflow-name}")
    private String createProjectWorkflow;

    private final ProjectService projectService;
    
    private final ProjectMapper projectMapper;
    
    private final AutomationParametersMapper automationParametersMapper;
    
    private final ProjectCreationResponseMapper projectCreationResponseMapper;

    private final ProjectCreationCommandBuilder projectCreationCommandBuilder;
    
    private final ClientAppService clientAppService;
    
    private final AutomationPlatformService automationPlatformService;

    public ProjectsFacadeImpl(
            ProjectService projectService,
            ProjectMapper projectMapper,
            AutomationParametersMapper automationParametersMapper,
            ProjectCreationResponseMapper projectCreationResponseMapper,
            ProjectCreationCommandBuilder projectCreationCommandBuilder,
            ClientAppService clientAppService,
            AutomationPlatformService automationPlatformService) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
        this.automationParametersMapper = automationParametersMapper;
        this.projectCreationResponseMapper = projectCreationResponseMapper;
        this.projectCreationCommandBuilder = projectCreationCommandBuilder;
        this.clientAppService = clientAppService;
        this.automationPlatformService = automationPlatformService;
    }

    @Override
    public CreateProjectResponse createProject(CreateProjectRequest request, UUID clientId) {

        ClientAppEntity clientApp = clientAppService.findByClientId(clientId);

        ProjectCreationCommand command;
        ProjectResponse project;

        if (Boolean.TRUE.equals(request.getRegistrationOnly())) {
            command = projectCreationCommandBuilder.buildForRegistration(request, clientApp);
            project = registerProject(command);
        } else {
            command = projectCreationCommandBuilder.buildForCreation(request, clientApp);
            project = createNewProject(command);
        }

        return projectCreationResponseMapper.toSuccessResponse(command, project);
    }

    @Override
    public CreateProjectResponse getProject(String projectKey) {
        return projectMapper.toApiResponse(projectService.getProject(projectKey));
    }

    @Override
    public void updateProject(String projectKey, UpdateProjectRequest request) {
        projectService.updateProjectStatus(projectKey, request.getStatus());
    }

    private ProjectResponse registerProject(ProjectCreationCommand command) {

        ProjectRequest projectRequest = projectMapper.toServiceRequest(command);
        projectRequest.setStatus(Status.RUNNING);

        return projectService.saveProject(projectRequest);
    }

    private ProjectResponse createNewProject(ProjectCreationCommand command) {

        ProjectRequest projectRequest = projectMapper.toServiceRequest(command);
        projectRequest.setStatus(Status.PENDING);

        ProjectResponse project = projectService.saveProject(projectRequest);

        initializeProject(command, projectRequest, project);

        return project;
    }

    private void initializeProject(
            ProjectCreationCommand command, ProjectRequest projectRequest, ProjectResponse project) {
        String projectId = project.getProjectId().toString();
        Map<String, Object> workflowParameters = automationParametersMapper.toWorkflowParameters(command, projectId);

        AutomationExecutionResult automationExecutionResult = automationPlatformService
                .executeWorkflow(createProjectWorkflow, workflowParameters);

        if (!automationExecutionResult.isSuccessful()) {
            projectRequest.setProjectId(project.getProjectId());
            projectRequest.setStatus(Status.FAILED);
            projectService.saveProject(projectRequest);
            throw new ProjectCreationException("Failed to create project: "
                    + automationExecutionResult.getErrorDetails());
        }
    }
}