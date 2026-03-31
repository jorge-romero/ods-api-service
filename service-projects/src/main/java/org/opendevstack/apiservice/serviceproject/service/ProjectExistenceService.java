package org.opendevstack.apiservice.serviceproject.service;

import org.opendevstack.apiservice.serviceproject.exception.ProjectExistenceServiceException;

public interface ProjectExistenceService {
    
    boolean isProjectFound(String projectKey) throws ProjectExistenceServiceException;
}
