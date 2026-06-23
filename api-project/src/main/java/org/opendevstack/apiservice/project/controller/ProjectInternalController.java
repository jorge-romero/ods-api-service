package org.opendevstack.apiservice.project.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.core.security.jwt.JwtUtils;
import org.opendevstack.apiservice.project.api.ProjectsInternalApi;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.model.UpdateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.project.validation.ProjectRequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ProjectInternalController.API_BASE_PATH)
@AllArgsConstructor
@Slf4j
public class ProjectInternalController implements ProjectsInternalApi {
    
    public static final String API_BASE_PATH = "/api/pub/v1/projects";

    private static final String HTTP_HEADER_LOCATION = "Location";

    private final ProjectsFacade projectsFacade;
    
    private final ProjectRequestValidator projectRequestValidator;

    @PatchMapping("/{projectKey}")
    @Override
    public ResponseEntity<Void> updateProject(@PathVariable String projectKey,
                                                               @Valid @RequestBody UpdateProjectRequest updateProjectRequest) {
        String location = API_BASE_PATH + "/" + projectKey;
        projectRequestValidator.validateUpdateRequest(updateProjectRequest);

        projectsFacade.updateProject(projectKey, updateProjectRequest);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .header(HTTP_HEADER_LOCATION, location)
                .build();
    }
}
