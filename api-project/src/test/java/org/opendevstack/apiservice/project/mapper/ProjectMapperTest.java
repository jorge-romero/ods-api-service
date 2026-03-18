package org.opendevstack.apiservice.project.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProjectMapperTest {

    private ProjectMapper projectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        projectMapper = Mappers.getMapper(ProjectMapper.class);
    }

    @Test
    void to_service_request_maps_all_fields_correctly() {
        CreateProjectRequest apiRequest = new CreateProjectRequest("My Project");
        apiRequest.setProjectKey("PROJ01");
        apiRequest.setProjectKeyPattern("SS%06d");
        apiRequest.setProjectDescription("A test project");

        ProjectRequest result = projectMapper.toServiceRequest(apiRequest);

        assertNotNull(result);
        assertEquals("PROJ01", result.getProjectKey());
        assertEquals("SS%06d", result.getProjectKeyPattern());
        assertEquals("My Project", result.getProjectName());
        assertEquals("A test project", result.getProjectDescription());
    }

    @Test
    void to_service_request_returns_null_when_input_is_null() {
        CreateProjectRequest apiRequest = null;

        ProjectRequest result = projectMapper.toServiceRequest(apiRequest);

        assertNull(result);
    }

    @Test
    void to_service_request_maps_only_required_field() {
        CreateProjectRequest apiRequest = new CreateProjectRequest("Only Name");

        ProjectRequest result = projectMapper.toServiceRequest(apiRequest);

        assertNotNull(result);
        assertNull(result.getProjectKey());
        assertNull(result.getProjectKeyPattern());
        assertEquals("Only Name", result.getProjectName());
        assertNull(result.getProjectDescription());
    }
    
    @Test
    void to_api_response_maps_all_fields_correctly() {
        ProjectResponse serviceResponse = ProjectResponse.builder()
                .projectKey("PROJ01")
                .status(Status.PENDING)
                .projectFlavor("AMP")
                .build();

        CreateProjectResponse result = projectMapper.toApiResponse(serviceResponse);

        assertNotNull(result);
        assertEquals("PROJ01", result.getProjectKey());
        assertEquals("Pending", result.getStatus());
        assertEquals("AMP", result.getProjectFlavor());
        assertEquals("/api/pub/v0/projects/PROJ01", result.getLocation());
    }

    @Test
    void to_api_response_returns_null_when_input_is_null() {
        ProjectResponse serviceResponse = null;

        CreateProjectResponse result = projectMapper.toApiResponse(serviceResponse);

        assertNull(result);
    }

    @Test
    void to_api_response_maps_with_null_status() {
        ProjectResponse serviceResponse = ProjectResponse.builder()
                .projectKey("PROJ02")
                .status(null)
                .build();

        CreateProjectResponse result = projectMapper.toApiResponse(serviceResponse);

        assertNotNull(result);
        assertEquals("PROJ02", result.getProjectKey());
        assertNull(result.getStatus());
        assertEquals("/api/pub/v0/projects/PROJ02", result.getLocation());
    }

    @Test
    void to_api_response_does_not_set_location_when_project_key_is_null() {
        ProjectResponse serviceResponse = ProjectResponse.builder()
                .projectKey(null)
                .status(Status.RUNNING)
                .build();

        CreateProjectResponse result = projectMapper.toApiResponse(serviceResponse);

        assertNotNull(result);
        assertNull(result.getProjectKey());
        assertEquals("Running", result.getStatus());
        assertNull(result.getLocation());
    }

    @Test
    void to_api_response_does_not_set_location_when_project_key_is_empty() {
        ProjectResponse serviceResponse = ProjectResponse.builder()
                .projectKey("")
                .status(Status.FAILED)
                .build();

        CreateProjectResponse result = projectMapper.toApiResponse(serviceResponse);

        assertNotNull(result);
        assertEquals("", result.getProjectKey());
        assertEquals("Failed", result.getStatus());
        assertNull(result.getLocation());
    }

    @Test
    void to_api_response_maps_status_running_to_db_value() {
        ProjectResponse serviceResponse = ProjectResponse.builder()
                .projectKey("RUN01")
                .status(Status.RUNNING)
                .build();

        CreateProjectResponse result = projectMapper.toApiResponse(serviceResponse);

        assertNotNull(result);
        assertEquals("Running", result.getStatus());
    }

    @Test
    void to_api_response_maps_status_failed_to_db_value() {
        ProjectResponse serviceResponse = ProjectResponse.builder()
                .projectKey("FAIL01")
                .status(Status.FAILED)
                .build();

        CreateProjectResponse result = projectMapper.toApiResponse(serviceResponse);

        assertNotNull(result);
        assertEquals("Failed", result.getStatus());
    }

    @Test
    void to_api_response_maps_null_project_flavor() {
        ProjectResponse serviceResponse = ProjectResponse.builder()
                .projectKey("PROJ03")
                .status(Status.PENDING)
                .projectFlavor(null)
                .build();

        CreateProjectResponse result = projectMapper.toApiResponse(serviceResponse);

        assertNotNull(result);
        assertEquals("PROJ03", result.getProjectKey());
        assertNull(result.getProjectFlavor());
    }

    @Test
    void to_api_response_sets_error_description_when_status_is_failed() {
        ProjectResponse serviceResponse = ProjectResponse.builder()
                .projectKey("FAIL01")
                .status(Status.FAILED)
                .build();
        
        CreateProjectResponse result = projectMapper.toApiResponse(serviceResponse);
        
        assertNotNull(result);
        assertEquals(
                "There was an error when creating the project FAIL01.\n\n " +
                        "The error has been reported to our Support team as an incident. " +
                        "You will be informed about the incident via email.",
                result.getErrorDescription()
        );
    }

    @Test
    void to_api_response_sets_null_error_description_when_status_is_not_failed() {
        ProjectResponse serviceResponse = ProjectResponse.builder()
                .projectKey("PROJ01")
                .status(Status.RUNNING)
                .build();
        
        CreateProjectResponse result = projectMapper.toApiResponse(serviceResponse);
        
        assertNotNull(result);
        assertNull(result.getErrorDescription());
    }    
}
