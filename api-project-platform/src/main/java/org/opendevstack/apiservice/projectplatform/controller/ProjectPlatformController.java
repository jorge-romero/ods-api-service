package org.opendevstack.apiservice.projectplatform.controller;

import org.opendevstack.apiservice.projectplatform.api.ProjectsApi;
import org.opendevstack.apiservice.projectplatform.exception.ProjectPlatformsException;
import org.opendevstack.apiservice.projectplatform.facade.ProjectsFacade;
import org.opendevstack.apiservice.projectplatform.model.ProjectPlatforms;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@AllArgsConstructor
public class ProjectPlatformController implements ProjectsApi {

    ProjectsFacade projectsFacade;

    @CrossOrigin(origins = "*")
    @GetMapping("/{projectKey}/platforms")
    public ResponseEntity<ProjectPlatforms> getProjectPlatforms(@PathVariable String projectKey) {
        ProjectPlatforms projectPlatforms;
        try {
            projectPlatforms = projectsFacade.getProjectPlatforms(projectKey);
        } catch (ProjectPlatformsException e) {
            return sneakyThrow(e);
        }

        if (projectPlatforms == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(projectPlatforms);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable, R> R sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
