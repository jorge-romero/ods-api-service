package org.opendevstack.apiservice.serviceproject.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.opendevstack.apiservice.serviceproject.mapper.ProjectResponseMapper;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.Status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectResponseMapper projectResponseMapper;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        projectService = new ProjectServiceImpl(
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
        verify(projectResponseMapper, never()).toCreateProjectResponse(any(ProjectEntity.class));
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

    @Test
    void find_projects_by_name_returns_responses_when_projects_exist() {
        String projectName = "My Project";

        ProjectEntity projectEntity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .projectKey("MY-PROJECT")
                .projectName(projectName)
                .build();

        ProjectResponse expectedResponse = ProjectResponse.builder()
                .projectKey("MY-PROJECT")
                .status(Status.RUNNING)
                .build();

        when(projectRepository.findByProjectNameIgnoreCase(projectName)).thenReturn(List.of(projectEntity));
        when(projectResponseMapper.toCreateProjectResponse(List.of(projectEntity))).thenReturn(List.of(expectedResponse));

        List<ProjectResponse> result = projectService.findProjectsByName(projectName);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MY-PROJECT", result.get(0).getProjectKey());
        verify(projectRepository).findByProjectNameIgnoreCase(projectName);
        verify(projectResponseMapper).toCreateProjectResponse(List.of(projectEntity));
    }

    @Test
    void find_projects_by_name_returns_empty_list_when_project_does_not_exist() {
        String projectName = "Unknown Project";

        when(projectRepository.findByProjectNameIgnoreCase(projectName)).thenReturn(List.of());
        when(projectResponseMapper.toCreateProjectResponse(anyList())).thenReturn(List.of());

        List<ProjectResponse> result = projectService.findProjectsByName(projectName);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(projectRepository).findByProjectNameIgnoreCase(projectName);
        verify(projectResponseMapper).toCreateProjectResponse(anyList());
    }

    @Test
    void update_project_status_saves_entity_with_new_status_when_project_exists() {
        String projectKey = "PROJ01";
        ProjectEntity entity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .projectKey(projectKey)
                .projectName("My Project")
                .status("Pending")
                .build();

        when(projectRepository.findByProjectKeyIgnoreCase(projectKey)).thenReturn(Optional.of(entity));

        projectService.updateProjectStatus(projectKey, "Running");

        ArgumentCaptor<ProjectEntity> captor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(projectRepository).save(captor.capture());
        assertEquals("Running", captor.getValue().getStatus());
    }

    @Test
    void update_project_status_does_nothing_when_project_not_found() {
        String projectKey = "UNKNOWN";

        when(projectRepository.findByProjectKeyIgnoreCase(projectKey)).thenReturn(Optional.empty());

        projectService.updateProjectStatus(projectKey, "Running");

        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void update_project_status_propagates_repository_exception() {
        String projectKey = "ERROR";

        when(projectRepository.findByProjectKeyIgnoreCase(projectKey))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> projectService.updateProjectStatus(projectKey, "Running"));

        verify(projectRepository).findByProjectKeyIgnoreCase(projectKey);
    }
}
