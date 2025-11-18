package org.opendevstack.apiservice.projectplatform.facade;

import org.opendevstack.apiservice.projectplatform.exception.ProjectPlatformsException;
import org.opendevstack.apiservice.projectplatform.model.ProjectPlatforms;

public interface ProjectsFacade {
    ProjectPlatforms getProjectPlatforms(String projectKey) throws ProjectPlatformsException;
}
