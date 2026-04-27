package org.opendevstack.apiservice.serviceproject.service;

import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;

import java.util.List;

public interface ProjectService {

    ProjectResponse saveProject(ProjectRequest request);

    ProjectResponse getProject(String projectKey);
    
    List<ProjectResponse> findProjectsByName(String projectName);
}

