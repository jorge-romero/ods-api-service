package org.opendevstack.apiservice.project.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.api.ProjectsApi;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.project.validation.ProjectRequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ProjectController.API_BASE_PATH)
@AllArgsConstructor
@Slf4j
public class ProjectController implements ProjectsApi {
    
    public static final String API_BASE_PATH = "/api/pub/v0/projects";

    private static final String HTTP_HEADER_LOCATION = "Location";

    private final ProjectsFacade projectsFacade;
    
    private final ProjectRequestValidator projectRequestValidator;

    @PostMapping
    @Override
    public ResponseEntity<CreateProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest createProjectRequest) {
        projectRequestValidator.validate(createProjectRequest);
        UUID clientId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        CreateProjectResponse projectResponse = projectsFacade.createProject(createProjectRequest, clientId);
        projectResponse.setLocation(API_BASE_PATH + "/" + projectResponse.getProjectKey());
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HTTP_HEADER_LOCATION, API_BASE_PATH)
                .body(projectResponse);
    }
    
    @GetMapping("/{projectKey}")
    @Override
    public ResponseEntity<CreateProjectResponse> getProject(@PathVariable String projectKey) {
        String location = API_BASE_PATH + "/" + projectKey;
        CreateProjectResponse response = projectsFacade.getProject(projectKey);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HTTP_HEADER_LOCATION, location)
                    .body(ProjectResponseFactory.notFound(projectKey, location));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .header(HTTP_HEADER_LOCATION, location)
                .body(response);
    }
}
