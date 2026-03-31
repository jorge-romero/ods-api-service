package org.opendevstack.apiservice.serviceproject.service;

import org.opendevstack.apiservice.serviceproject.exception.ProjectExistenceServiceException;
import org.opendevstack.apiservice.serviceproject.exception.ProjectKeyGenerationException;

public interface GenerateProjectKeyService {

    String DEFAULT_PROJECT_KEY_PATTERN = "SS%06d";
    
    String generateProjectKey(String projectKeyPattern) throws ProjectKeyGenerationException, ProjectExistenceServiceException;
}

