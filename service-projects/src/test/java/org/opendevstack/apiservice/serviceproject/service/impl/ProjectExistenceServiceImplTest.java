package org.opendevstack.apiservice.serviceproject.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.opendevstack.apiservice.serviceproject.exception.ProjectExistenceServiceException;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;

import java.util.List;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProjectExistenceServiceImplTest {
    
    @Mock
    private BitbucketService bitbucketService;
    @Mock
    private JiraService jiraService;
    @Mock
    private OpenshiftService openshiftService;
    @Mock
    private ProjectService projectService;

    private ProjectExistenceServiceImpl sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sut = new ProjectExistenceServiceImpl(bitbucketService, jiraService, openshiftService, projectService);
    }

    @Test
    void is_project_found_returns_true_when_project_exists_in_db() throws Exception {
        when(projectService.getProject("KEY1")).thenReturn(new ProjectResponse());
        
        boolean result = sut.isProjectFound("KEY1");
        assertTrue(result);
        verify(projectService).getProject("KEY1");
        verifyNoInteractions(bitbucketService, jiraService, openshiftService);
    }

    @Test
    void is_project_found_returns_true_when_project_exists_in_bitbucket() throws Exception {
        when(projectService.getProject("KEY2")).thenReturn(null);
        when(bitbucketService.getAvailableInstances()).thenReturn(Set.of("inst1"));
        when(bitbucketService.projectExists("inst1", "KEY2")).thenReturn(true);
        
        boolean result = sut.isProjectFound("KEY2");
        assertTrue(result);
        verify(bitbucketService).projectExists("inst1", "KEY2");
    }

    @Test
    void is_project_found_returns_true_when_project_exists_in_jira() throws Exception {
        when(projectService.getProject("KEY3")).thenReturn(null);
        when(bitbucketService.getAvailableInstances()).thenReturn(Collections.emptySet());
        when(jiraService.getAvailableInstances()).thenReturn(Set.of("jira1"));
        when(jiraService.projectExists("jira1", "KEY3")).thenReturn(true);
        boolean result = sut.isProjectFound("KEY3");
        assertTrue(result);
        verify(jiraService).projectExists("jira1", "KEY3");
    }

    @Test
    void is_project_found_returns_true_when_project_exists_in_openshift() throws Exception {
        when(projectService.getProject("KEY4")).thenReturn(null);
        when(bitbucketService.getAvailableInstances()).thenReturn(Collections.emptySet());
        when(jiraService.getAvailableInstances()).thenReturn(Collections.emptySet());
        when(openshiftService.getAvailableInstances()).thenReturn(Set.of("ocp1"));
        when(openshiftService.projectExists("ocp1", "KEY4")).thenReturn(true);
        
        boolean result = sut.isProjectFound("KEY4");
        assertTrue(result);
        verify(openshiftService).projectExists("ocp1", "KEY4");
    }

    @Test
    void is_project_found_returns_false_when_project_not_found_anywhere() throws Exception {
        when(projectService.getProject("KEY5")).thenReturn(null);
        when(bitbucketService.getAvailableInstances()).thenReturn(Set.of("inst1"));
        when(bitbucketService.projectExists("inst1", "KEY5")).thenReturn(false);
        when(jiraService.getAvailableInstances()).thenReturn(Set.of("jira1"));
        when(jiraService.projectExists("jira1", "KEY5")).thenReturn(false);
        when(openshiftService.getAvailableInstances()).thenReturn(Set.of("ocp1"));
        
        when(openshiftService.projectExists("ocp1", "KEY5")).thenReturn(false);
        boolean result = sut.isProjectFound("KEY5");
        assertFalse(result);
    }

    @Test
    void is_project_found_throws_exception_when_bitbucket_fails() {
        when(projectService.getProject("KEY6")).thenReturn(null);
        when(bitbucketService.getAvailableInstances()).thenThrow(new RuntimeException("fail"));
        assertThrows(ProjectExistenceServiceException.class, () -> sut.isProjectFound("KEY6"));

        assertThrows(ProjectExistenceServiceException.class, () -> {
            sut.isProjectFound("KEY6");
        });
    }

    @Test
    void is_project_found_throws_exception_when_jira_fails() {
        when(projectService.getProject("KEY7")).thenReturn(null);
        when(bitbucketService.getAvailableInstances()).thenReturn(Collections.emptySet());
        when(jiraService.getAvailableInstances()).thenThrow(new RuntimeException("fail"));
        
        assertThrows(ProjectExistenceServiceException.class, () -> sut.isProjectFound("KEY7"));

        assertThrows(ProjectExistenceServiceException.class, () -> {
            sut.isProjectFound("KEY7");
        });
    }

    @Test
    void is_project_found_throws_exception_when_openshift_fails() {
        when(projectService.getProject("KEY8")).thenReturn(null);
        when(bitbucketService.getAvailableInstances()).thenReturn(Collections.emptySet());
        when(jiraService.getAvailableInstances()).thenReturn(Collections.emptySet());
        when(openshiftService.getAvailableInstances()).thenThrow(new RuntimeException("fail"));

        assertThrows(ProjectExistenceServiceException.class, () -> {
            sut.isProjectFound("KEY8");
        });
    }

    @Test
    void is_project_found_by_name_returns_true_when_project_exists_in_db() throws Exception {
        when(projectService.findProjectsByName("Project One")).thenReturn(List.of(new ProjectResponse()));

        boolean result = sut.isProjectFoundByName("Project One");

        assertTrue(result);
        verify(projectService).findProjectsByName("Project One");
    }

    @Test
    void is_project_found_by_name_returns_false_when_project_does_not_exist_in_db() throws Exception {
        when(projectService.findProjectsByName("Missing Project")).thenReturn(List.of());

        boolean result = sut.isProjectFoundByName("Missing Project");

        assertFalse(result);
        verify(projectService).findProjectsByName("Missing Project");
    }
}