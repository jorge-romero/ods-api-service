package org.opendevstack.apiservice.project.facade.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectsFacadeImplTest {

    private static final UUID CLIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private ProjectService projectService;
    
    @Mock
    private ProjectMapper projectMapper;
    
    @Mock
    private AutomationParametersMapper automationParametersMapper;
    
    @Mock
    private ProjectCreationResponseMapper projectCreationResponseMapper;
    
    @Mock
    private ProjectCreationCommandBuilder projectCreationCommandBuilder;
    
    @Mock
    private ClientAppService clientAppService;
    
    @Mock
    private AutomationPlatformService automationPlatformService;

    private ProjectsFacadeImpl sut;
    private AutoCloseable mocks;

    @BeforeEach
    void set_up() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        sut = new ProjectsFacadeImpl(
                projectService,
                projectMapper,
                automationParametersMapper,
                projectCreationResponseMapper,
                projectCreationCommandBuilder,
                clientAppService,
                automationPlatformService);

        Field workflowField = ProjectsFacadeImpl.class.getDeclaredField("createProjectWorkflow");
        workflowField.setAccessible(true);
        workflowField.set(sut, "create-project-workflow");
    }

    @AfterEach
    void tear_down() throws Exception {
        mocks.close();
    }

    @Test
    void create_project_returns_success_response_when_automation_is_successful() {
        CreateProjectRequest request = new CreateProjectRequest();
        ClientAppEntity clientApp = ClientAppEntity.builder().clientId(CLIENT_ID).build();
        ProjectCreationCommand command = new ProjectCreationCommand(
                "DLSS01", "name", "desc", "DLSS", "CI-001", "eu", "x2test", "owner", CLIENT_ID);
        ProjectRequest serviceRequest = new ProjectRequest();
        ProjectResponse projectResponse = ProjectResponse.builder()
                .projectId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .projectKey("DLSS01")
                .status(Status.PENDING)
                .build();
        CreateProjectResponse apiResponse = new CreateProjectResponse();
        apiResponse.setStatus("Pending");
        apiResponse.setProjectFlavor("DLSS");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectCreationCommandBuilder.buildForCreation(request, clientApp)).thenReturn(command);
        when(projectMapper.toServiceRequest(command)).thenReturn(serviceRequest);
        when(projectService.saveProject(serviceRequest)).thenReturn(projectResponse);
        when(automationParametersMapper.toWorkflowParameters(command, "11111111-1111-1111-1111-111111111111"))
                .thenReturn(Map.of("project_key", "DLSS01"));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job-1", "ok"));
        when(projectCreationResponseMapper.toSuccessResponse(command, projectResponse)).thenReturn(apiResponse);

        CreateProjectResponse result = sut.createProject(request, CLIENT_ID);

        assertEquals("Pending", result.getStatus());
        assertEquals("DLSS", result.getProjectFlavor());
        verify(projectCreationCommandBuilder).buildForCreation(request, clientApp);
        verify(projectMapper).toServiceRequest(command);
        verify(automationParametersMapper)
                .toWorkflowParameters(command, "11111111-1111-1111-1111-111111111111");
        verify(projectCreationResponseMapper).toSuccessResponse(command, projectResponse);
    }

    @Test
    void create_project_with_registrationOnly_returns_success_response_when_automation_is_not_executed() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setRegistrationOnly(true);
        ClientAppEntity clientApp = ClientAppEntity.builder().clientId(CLIENT_ID).build();
        ProjectCreationCommand command = new ProjectCreationCommand(
                "P3RO01", "name", "desc", null, "CI-001", "eu", "x2test", "owner", CLIENT_ID);
        ProjectRequest serviceRequest = new ProjectRequest();
        ProjectResponse projectResponse = ProjectResponse.builder()
                .projectId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .projectKey("P3RO01")
                .status(Status.RUNNING)
                .build();
        CreateProjectResponse apiResponse = new CreateProjectResponse();
        apiResponse.setStatus("Running");
        apiResponse.setProjectFlavor("REGULAR");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectCreationCommandBuilder.buildForRegistration(request, clientApp)).thenReturn(command);
        when(projectMapper.toServiceRequest(command)).thenReturn(serviceRequest);
        when(projectService.saveProject(serviceRequest)).thenReturn(projectResponse);
        when(projectCreationResponseMapper.toSuccessResponse(command, projectResponse)).thenReturn(apiResponse);

        CreateProjectResponse result = sut.createProject(request, CLIENT_ID);

        assertEquals("Running", result.getStatus());
        assertEquals("REGULAR", result.getProjectFlavor());
        verify(projectCreationCommandBuilder).buildForRegistration(request, clientApp);
        verify(projectMapper).toServiceRequest(command);
        verify(automationParametersMapper, never())
                .toWorkflowParameters(command, "11111111-1111-1111-1111-111111111111");
        verify(automationPlatformService, never()).executeWorkflow(anyString(), anyMap());
        verify(projectCreationResponseMapper).toSuccessResponse(command, projectResponse);
    }

    @Test
    void create_project_throws_project_creation_exception_when_automation_is_not_successful() {
        CreateProjectRequest request = new CreateProjectRequest();
        ClientAppEntity clientApp = ClientAppEntity.builder().clientId(CLIENT_ID).build();
        ProjectCreationCommand command = new ProjectCreationCommand(
                "DLSS01", "name", "desc", "DLSS", "CI-001", "eu", "x2test", "owner", CLIENT_ID);

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectCreationCommandBuilder.buildForCreation(request, clientApp)).thenReturn(command);
        when(projectMapper.toServiceRequest(command)).thenReturn(new ProjectRequest());
        when(projectService.saveProject(any(ProjectRequest.class)))
                .thenReturn(ProjectResponse.builder()
                        .projectId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                        .projectKey("DLSS01")
                        .status(Status.PENDING)
                        .build());
        when(automationParametersMapper.toWorkflowParameters(command, "11111111-1111-1111-1111-111111111111"))
                .thenReturn(Map.of());
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.failure("job-1", "error", "workflow failed"));

        assertThrows(ProjectCreationException.class, () -> sut.createProject(request, CLIENT_ID));
    }

    @Test
    void get_project_returns_mapped_response_when_service_returns_project() {
        ProjectResponse serviceResponse = ProjectResponse.builder()
                .projectKey("PROJ01")
                .status(Status.RUNNING)
                .build();
        CreateProjectResponse mappedResponse = new CreateProjectResponse();
        mappedResponse.setProjectKey("PROJ01");
        mappedResponse.setStatus("Running");

        when(projectService.getProject("PROJ01")).thenReturn(serviceResponse);
        when(projectMapper.toApiResponse(serviceResponse)).thenReturn(mappedResponse);

        CreateProjectResponse result = sut.getProject("PROJ01");

        assertEquals("PROJ01", result.getProjectKey());
        assertEquals("Running", result.getStatus());
    }

    @Test
    void get_project_returns_null_when_service_returns_null() {
        when(projectService.getProject("UNKNOWN")).thenReturn(null);
        when(projectMapper.toApiResponse(null)).thenReturn(null);

        CreateProjectResponse result = sut.getProject("UNKNOWN");

        assertNull(result);
    }

    @Test
    void update_project_calls_update_status() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setStatus("Running");
        ProjectResponse projectResponse = ProjectResponse.builder()
                .projectKey("PROJ01")
                .status(Status.PENDING)
                .build();

        when(projectService.getProject("PROJ01")).thenReturn(projectResponse);

        sut.updateProject("PROJ01", request);

        verify(projectService).updateProjectStatus("PROJ01", "Running");
    }
}
