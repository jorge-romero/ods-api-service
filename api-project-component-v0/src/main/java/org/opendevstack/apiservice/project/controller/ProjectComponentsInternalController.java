package org.opendevstack.apiservice.project.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.api.ProjectComponentsInternalApi;
import org.opendevstack.apiservice.project.facade.ComponentsFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(ProjectComponentsInternalController.API_BASE_PATH)
public class ProjectComponentsInternalController implements ProjectComponentsInternalApi {

    public static final String API_BASE_PATH = "/api/pub/v1";

    private final ComponentsFacade componentsFacade;

    @Override
    public ResponseEntity<Void> deleteProjectComponent(String projectId, String componentId) {
        componentsFacade.deleteProjectComponent(projectId, componentId);
        log.info("Deleted component with id '{}' for project '{}'", componentId, projectId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
