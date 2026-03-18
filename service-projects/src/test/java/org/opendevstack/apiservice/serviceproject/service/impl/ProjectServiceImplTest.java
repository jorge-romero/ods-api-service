package org.opendevstack.apiservice.serviceproject.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.opendevstack.apiservice.serviceproject.mapper.ProjectResponseMapper;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectServiceImplTest {

    @Mock
    private OpenshiftService openshiftService;

    @Mock
    private BitbucketService bitbucketService;

    @Mock
    private JiraService jiraService;

    @Mock
    private GenerateProjectKeyService generateProjectKeyService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectResponseMapper projectResponseMapper;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        projectService = new ProjectServiceImpl(
                openshiftService,
                bitbucketService,
                jiraService,
                generateProjectKeyService,
                projectRepository,
                projectResponseMapper
        );
    }

    @Test
    void get_project_returns_response_when_project_exists() {
        
        String projectKey = "MY-PROJECT";
        UUID projectId = UUID.randomUUID();

        ProjectEntity projectEntity = ProjectEntity.builder()
                .id(projectId)
                .projectKey(projectKey)
                .projectName("My Project")
                .description("Test project")
                .configurationItem("CI-123")
                .location("eu")
                .projectFlavor("AMP")
                .status("Completed")
                .deleted(false)
                .ldapGroupManager("cn=my-project-manager,ou=groups,dc=example,dc=com")
                .ldapGroupTeam("cn=my-project-team,ou=groups,dc=example,dc=com")
                .build();

        ProjectResponse expectedResponse = ProjectResponse.builder()
                .projectKey(projectKey)
                .status(Status.RUNNING)
                .build();

        when(projectRepository.findByProjectKeyIgnoreCase(projectKey)).thenReturn(Optional.of(projectEntity));
        when(projectResponseMapper.toCreateProjectResponse(projectEntity)).thenReturn(expectedResponse);

        
        ProjectResponse result = projectService.getProject(projectKey);

        
        assertNotNull(result);
        assertEquals(projectKey, result.getProjectKey());
        assertEquals(Status.RUNNING, result.getStatus());
        verify(projectRepository).findByProjectKeyIgnoreCase(projectKey);
        verify(projectResponseMapper).toCreateProjectResponse(projectEntity);
    }

    @Test
    void get_project_returns_null_when_project_does_not_exist() {
        
        String projectKey = "NON-EXISTING";

        when(projectRepository.findByProjectKeyIgnoreCase(projectKey)).thenReturn(Optional.empty());

        
        ProjectResponse result = projectService.getProject(projectKey);

        
        assertNull(result);
        verify(projectRepository).findByProjectKeyIgnoreCase(projectKey);
        verify(projectResponseMapper, never()).toCreateProjectResponse(any());
    }

    @Test
    void create_project_returns_empty_response() {
        
        ProjectRequest request = new ProjectRequest();
        request.setProjectKey("NEW-PROJECT");
        request.setProjectKeyPattern("NEW%06d");
        request.setProjectName("New Project");
        request.setProjectDescription("New test project");

        
        ProjectResponse result = projectService.createProject(request);

        
        assertNotNull(result);
        assertNull(result.getProjectKey());
    }

    @Test
    void get_project_propagates_repository_exception() {
        
        String projectKey = "ERROR-PROJECT";

        when(projectRepository.findByProjectKeyIgnoreCase(projectKey))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> projectService.getProject(projectKey));
        verify(projectRepository).findByProjectKeyIgnoreCase(projectKey);
    }

    @Test
    void get_project_returns_null_when_project_key_is_null() {
        
        when(projectRepository.findByProjectKeyIgnoreCase(null)).thenReturn(Optional.empty());

        
        ProjectResponse result = projectService.getProject(null);

        
        assertNull(result);
        verify(projectRepository).findByProjectKeyIgnoreCase(null);
    }

    @Test
    void get_project_returns_response_for_soft_deleted_project() {
        
        String projectKey = "DELETED-PROJECT";

        ProjectEntity deletedEntity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .projectKey(projectKey)
                .projectName("Deleted Project")
                .configurationItem("CI-456")
                .location("eu")
                .deleted(true)
                .build();

        ProjectResponse expectedResponse = ProjectResponse.builder()
                .projectKey(projectKey)
                .status(Status.RUNNING)
                .build();

        when(projectRepository.findByProjectKeyIgnoreCase(projectKey)).thenReturn(Optional.of(deletedEntity));
        when(projectResponseMapper.toCreateProjectResponse(deletedEntity)).thenReturn(expectedResponse);

        
        ProjectResponse result = projectService.getProject(projectKey);

        
        assertNotNull(result);
        assertEquals(projectKey, result.getProjectKey());
        verify(projectRepository).findByProjectKeyIgnoreCase(projectKey);
    }
}