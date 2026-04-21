package org.opendevstack.apiservice.project.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.api.ProjectComponentsApi;
import org.opendevstack.apiservice.project.exception.ComponentCreationException;
import org.opendevstack.apiservice.project.exception.ComponentNotFoundException;
import org.opendevstack.apiservice.project.facade.ComponentsFacade;
import org.opendevstack.apiservice.project.mapper.ComponentResponseMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(ProjectComponentsController.API_BASE_PATH)
public class ProjectComponentsController implements ProjectComponentsApi {

    public static final String API_BASE_PATH = "/api/pub/v0";

    private final ComponentsFacade componentsFacade;

    private final ComponentResponseMapper componentResponseMapper;

    @Override
    public ResponseEntity<CreateComponentResponse> createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        Component component = componentsFacade.createProjectComponent(projectId, createComponentRequest);
        if (component == null) {
            throw new ComponentCreationException(String.format("Failed to create component for project '%s'", projectId));
        }

        log.info("Created component {} for project id {} and request {}", component, projectId, createComponentRequest);
        return componentResponseMapper.toResponseEntity(
                ComponentsResponseFactory.entityCreated(projectId, component.getId())
        );
    }

    @Override
    public ResponseEntity<Component> getProjectComponent(String projectId, UUID componentId) {
        Component component = componentsFacade.getProjectComponent(projectId, componentId.toString());
        if (component == null) {
            throw new ComponentNotFoundException(
                    String.format("Component '%s' not found for project '%s'", componentId, projectId)
            );
        }

        log.info("Retrieved component '{}' for project '{}': {}", componentId, projectId, component);
        return ResponseEntity.status(HttpStatus.OK).body(component);
    }
}
