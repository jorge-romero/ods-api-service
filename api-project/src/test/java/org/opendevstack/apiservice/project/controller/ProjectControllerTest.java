package org.opendevstack.apiservice.project.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.project.validation.ProjectRequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

class ProjectControllerTest {

    @Mock
    private ProjectsFacade projectsFacade;

    @Mock
    private ProjectRequestValidator projectRequestValidator;

    private ProjectController sut;
    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        sut = new ProjectController(projectsFacade, projectRequestValidator);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void create_project_returns_ok_when_creation_succeeds() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectKey("PROJ01");

        CreateProjectResponse serviceResponse = new CreateProjectResponse();
        serviceResponse.setProjectKey("PROJ01");
        serviceResponse.setStatus("Initiated");
        serviceResponse.setErrorKey("000");
        serviceResponse.setMessage("The project creation process has been successfully initiated.");

        when(projectsFacade.createProject(any(CreateProjectRequest.class), any(UUID.class)))
                .thenReturn(serviceResponse);

        ResponseEntity<CreateProjectResponse> result = sut.createProject(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getProjectKey()).isEqualTo("PROJ01");
        assertThat(result.getBody().getStatus()).isEqualTo("Initiated");
        assertThat(result.getBody().getError()).isNull();
        assertThat(result.getBody().getErrorKey()).isEqualTo("000");
        assertThat(result.getBody().getErrorDescription()).isNull();
        verify(projectRequestValidator).validate(request);
        verify(projectsFacade).createProject(any(CreateProjectRequest.class), any(UUID.class));
    }

    @Test
    void get_project_returns_ok_when_project_exists() {
        CreateProjectResponse serviceResponse = new CreateProjectResponse();
        serviceResponse.setProjectKey("PROJ01");
        serviceResponse.setStatus("Initiated");

        when(projectsFacade.getProject("PROJ01")).thenReturn(serviceResponse);

        ResponseEntity<CreateProjectResponse> result = sut.getProject("PROJ01");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getProjectKey()).isEqualTo("PROJ01");
        assertThat(result.getBody().getError()).isNull();
        assertThat(result.getBody().getErrorKey()).isNull();
        verify(projectsFacade).getProject("PROJ01");
    }

    @Test
    void get_project_returns_not_found_when_project_does_not_exist() {
        when(projectsFacade.getProject("UNKNOWN")).thenReturn(null);

        ResponseEntity<CreateProjectResponse> result = sut.getProject("UNKNOWN");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo("Not Found");
        assertThat(result.getBody().getErrorKey()).isEqualTo("012");
        assertThat(result.getBody().getMessage()).contains("UNKNOWN");
        assertThat(result.getBody().getProjectKey()).isNull();
        assertThat(result.getBody().getStatus()).isNull();
        assertThat(result.getBody().getErrorDescription()).isNull();
        verify(projectsFacade).getProject("UNKNOWN");
    }

}
