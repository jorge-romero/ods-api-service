package org.opendevstack.apiservice.project.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.project.exception.ComponentNotFoundException;
import org.opendevstack.apiservice.project.facade.ComponentsFacade;
import org.opendevstack.apiservice.project.mapper.ComponentResponseMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestComponent;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestCreateComponentRequest;

class ProjectComponentsControllerTest {

    @Mock
    private ComponentsFacade componentsFacade;

    private final ComponentResponseMapper componentResponseMapper = Mappers.getMapper(ComponentResponseMapper.class);

    private ProjectComponentsController projectComponentsController;

    private AutoCloseable openMocks;

    @BeforeEach
    void setup() {
        openMocks = MockitoAnnotations.openMocks(this);
        projectComponentsController = new ProjectComponentsController(componentsFacade, componentResponseMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void create_project_component_returns_ok_with_component_name_in_path() {
        String projectId = "testProjectId";
        CreateComponentRequest request = buildTestCreateComponentRequest(); // name = "testcomponent"

        doNothing().when(componentsFacade).provisionProjectComponent(eq(projectId), any(CreateComponentRequest.class));

        ResponseEntity<CreateComponentResponse> response = projectComponentsController.createProjectComponent(projectId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHttpStatus()).isEqualTo(HttpStatus.OK.name());
        assertThat(response.getBody().getErrorKey()).isEqualTo("000");
        assertThat(response.getBody().getMessage()).isEqualTo("Component created");
        assertThat(response.getBody().getPath()).isEqualTo("/api/pub/v0/projects/testProjectId/components/testcomponent");
        verify(componentsFacade).provisionProjectComponent(projectId, request);
    }

    @Test
    void create_project_component_propagates_exception_when_facade_throws_exception() {
        String projectId = "testProjectId";
        CreateComponentRequest request = buildTestCreateComponentRequest();

        org.mockito.Mockito.doThrow(new RuntimeException("boom"))
                .when(componentsFacade).provisionProjectComponent(eq(projectId), any(CreateComponentRequest.class));

        assertThatThrownBy(() -> projectComponentsController.createProjectComponent(projectId, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("boom");
        verify(componentsFacade).provisionProjectComponent(projectId, request);
    }

    @Test
    void get_project_component_returns_ok_when_component_exists() throws MarketplaceException {
        String projectId = "projectId";
        String componentId = "test-component-one";
        Component testComponent = buildTestComponent();

        when(componentsFacade.getProjectComponent(projectId, componentId)).thenReturn(testComponent);

        ResponseEntity<Component> response = projectComponentsController.getProjectComponent(projectId, componentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testComponent);
        verify(componentsFacade).getProjectComponent(projectId, componentId);
    }

    @Test
    void get_project_component_throws_not_found_when_component_does_not_exist() throws MarketplaceException {
        String projectId = "projectId";
        String componentId = "test-component-one";

        when(componentsFacade.getProjectComponent(projectId, componentId)).thenReturn(null);

        assertThatThrownBy(() -> projectComponentsController.getProjectComponent(projectId, componentId))
                .isInstanceOf(ComponentNotFoundException.class)
                .hasMessage("Component '" + componentId + "' not found for project 'projectId'");
        verify(componentsFacade).getProjectComponent(projectId, componentId);
    }

}