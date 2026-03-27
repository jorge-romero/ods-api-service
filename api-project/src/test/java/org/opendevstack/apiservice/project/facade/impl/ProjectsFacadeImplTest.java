package org.opendevstack.apiservice.project.facade.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.project.mapper.ProjectMapper;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectsFacadeImplTest {

    @Mock
    private ProjectService projectService;

    private final ProjectMapper projectMapper = Mappers.getMapper(ProjectMapper.class);

    private ProjectsFacadeImpl sut;

    @BeforeEach
    void setup() {
        sut = new ProjectsFacadeImpl(projectService, projectMapper);
    }

    @Test
    void createProject_whenServiceReturnsValue_thenMapToApiModel() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectKey("PROJ01");

        ProjectResponse serviceResponse =
                new ProjectResponse();
        serviceResponse.setProjectKey("PROJ01");
        serviceResponse.setStatus(Status.PENDING);

        when(projectService.createProject(org.mockito.ArgumentMatchers.any(
                ProjectRequest.class)))
                .thenReturn(serviceResponse);

        CreateProjectResponse response = sut.createProject(request);

        assertThat(response).isNotNull();
        assertThat(response.getProjectKey()).isEqualTo("PROJ01");
        assertThat(response.getStatus()).isEqualTo("Pending");
        verify(projectService).createProject(org.mockito.ArgumentMatchers.any(
                ProjectRequest.class));
    }

    @Test
    void createProject_whenServiceReturnsNull_thenReturnNull() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        when(projectService.createProject(org.mockito.ArgumentMatchers.any(
                ProjectRequest.class)))
                .thenReturn(null);

        CreateProjectResponse response = sut.createProject(request);

        assertThat(response).isNull();
    }

    @Test
    void getProject_whenServiceReturnsValue_thenMapToApiModel() throws Exception {
        ProjectResponse serviceResponse =
                new ProjectResponse();
        serviceResponse.setProjectKey("PROJ01");
        serviceResponse.setStatus(Status.RUNNING);

        when(projectService.getProject("PROJ01")).thenReturn(serviceResponse);

        CreateProjectResponse response = sut.getProject("PROJ01");

        assertThat(response).isNotNull();
        assertThat(response.getProjectKey()).isEqualTo("PROJ01");
        assertThat(response.getStatus()).isEqualTo("Running");
    }

    @Test
    void getProject_whenServiceReturnsNull_thenReturnNull() throws Exception {
        when(projectService.getProject("UNKNOWN")).thenReturn(null);

        CreateProjectResponse response = sut.getProject("UNKNOWN");

        assertThat(response).isNull();
    }
}
