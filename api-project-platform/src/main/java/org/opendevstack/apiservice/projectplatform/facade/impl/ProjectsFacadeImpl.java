package org.opendevstack.apiservice.projectplatform.facade.impl;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.ProjectsInfoService;
import org.opendevstack.apiservice.projectplatform.exception.ProjectPlatformsException;
import org.opendevstack.apiservice.projectplatform.facade.ProjectsFacade;
import org.opendevstack.apiservice.projectplatform.mapper.ProjectPlatformsMapper;
import org.opendevstack.apiservice.projectplatform.model.ProjectPlatforms;
import org.springframework.stereotype.Component;

@Component
public class ProjectsFacadeImpl implements ProjectsFacade {

    private final ProjectsInfoService projectsInfoService;
    private final ProjectPlatformsMapper mapper;

    public ProjectsFacadeImpl(ProjectsInfoService projectsInfoService, ProjectPlatformsMapper mapper) {
        this.projectsInfoService = projectsInfoService;
        this.mapper = mapper;
    }

    @Override
    public ProjectPlatforms getProjectPlatforms(String projectKey) throws ProjectPlatformsException {
        try {
            Platforms externalPlatforms =
                    projectsInfoService.getProjectPlatforms(projectKey);
            return mapper.toApiModel(externalPlatforms);
        } catch (ProjectsInfoServiceException e) {
            throw new ProjectPlatformsException("Failed to retrieve project platforms", e);
        }
    }
}
