package org.opendevstack.apiservice.project.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.api.ProjectsApi;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.project.exception.ProjectKeyGenerationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ProjectController.API_BASE_PATH)
@AllArgsConstructor
@Slf4j
public class ProjectController implements ProjectsApi {
    
    public static final String API_BASE_PATH = "/api/pub/v0/projects";

    private static final String HTTP_HEADER_LOCATION = "Location";

    private final ProjectsFacade projectsFacade;
    
    @PostMapping
    @Override
    public ResponseEntity<CreateProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest createProjectRequest) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HTTP_HEADER_LOCATION, API_BASE_PATH)
                    .body(projectsFacade.createProject(createProjectRequest));
        } catch (ProjectCreationException e) {
            log.error("Project creation conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header(HTTP_HEADER_LOCATION, API_BASE_PATH)
                    .body(ProjectResponseFactory.conflict(e.getMessage(), API_BASE_PATH));
        } catch (ProjectKeyGenerationException e) {
            log.error("Failed to generate project key: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HTTP_HEADER_LOCATION, API_BASE_PATH)
                    .body(ProjectResponseFactory.projectKeyGenerationFailed(API_BASE_PATH));
        }
    }
    
    @GetMapping("/{projectKey}")
    @Override
    public ResponseEntity<CreateProjectResponse> getProject(@PathVariable String projectKey) {
        String location = API_BASE_PATH + "/" + projectKey;
        try {
            CreateProjectResponse response = projectsFacade.getProject(projectKey);
            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header(HTTP_HEADER_LOCATION, location)
                        .body(ProjectResponseFactory.notFound(projectKey, location));
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HTTP_HEADER_LOCATION, location)
                    .body(response);
        } catch (Exception e) {
            log.error("Unexpected error retrieving project '{}': {}", projectKey, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HTTP_HEADER_LOCATION, location)
                    .body(ProjectResponseFactory.internalError(location));
        }
    }
}
