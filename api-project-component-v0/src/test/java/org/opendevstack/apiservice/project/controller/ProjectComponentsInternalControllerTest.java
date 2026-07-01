package org.opendevstack.apiservice.project.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.project.exception.ComponentDeletionException;
import org.opendevstack.apiservice.project.facade.ComponentsFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

class ProjectComponentsInternalControllerTest {

    @Mock
    private ComponentsFacade componentsFacade;

    private ProjectComponentsInternalController projectComponentsInternalController;

    private AutoCloseable openMocks;

    @BeforeEach
    void setup() {
        openMocks = MockitoAnnotations.openMocks(this);
        projectComponentsInternalController = new ProjectComponentsInternalController(componentsFacade);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void delete_project_component_returns_no_content_when_component_delete_works() {
        String projectId = "projectId";
        String componentId = "test-component-delete";

        ResponseEntity<Void> response = projectComponentsInternalController.deleteProjectComponent(projectId, componentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(componentsFacade).deleteProjectComponent(projectId, componentId);
    }

    @Test
    void delete_project_component_throws_exception_when_component_delete_api_throws_exception() {
        String projectId = "projectId";
        String componentId = "test-component-delete";

        doThrow(new ComponentDeletionException("test exception"))
                .when(componentsFacade).deleteProjectComponent(anyString(), anyString());

        assertThrows(ComponentDeletionException.class, () ->
                projectComponentsInternalController.deleteProjectComponent(projectId, componentId)
        );
        verify(componentsFacade).deleteProjectComponent(projectId, componentId);
    }
}