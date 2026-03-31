package org.opendevstack.apiservice.project.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.api.ProjectComponentsApi;
import org.opendevstack.apiservice.project.facade.ComponentsFacade;
import org.opendevstack.apiservice.project.mapper.ComponentResponseMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/pub/v0")
public class ProjectComponentsController implements ProjectComponentsApi {

    private final ComponentsFacade componentsFacade;

    private final ComponentResponseMapper componentResponseMapper;

    @Override
    public ResponseEntity<CreateComponentResponse> createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        try {
            Component component = componentsFacade.createProjectComponent(projectId, createComponentRequest);
            log.info("Created component {} for project id {} and request {}", component, projectId, createComponentRequest);
            if (component == null) {
                log.error("Failed to create component for project '{}'", projectId);
                return componentResponseMapper.toResponseEntity(ComponentsResponseFactory.error(projectId));
            }
            return componentResponseMapper.toResponseEntity(ComponentsResponseFactory.entityCreated(projectId, component.getId()));
        } catch (Exception e) {
            log.error("Error while trying to create component for project '" + projectId + "': " + e.getMessage(), e);
            return componentResponseMapper.toResponseEntity(ComponentsResponseFactory.error(projectId));
        }
    }

    @Override
    public ResponseEntity<Component> getProjectComponent(String projectId, String componentId) {
        try {
            Component component = componentsFacade.getProjectComponent(projectId, componentId);
            log.info("Retrieved component '{}' for project '{}': {}", componentId, projectId, component);
            if (component == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.OK).body(component);
        } catch (Exception e) {
            log.error("Error retrieving component '{}' for project '{}': {}", componentId, projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
