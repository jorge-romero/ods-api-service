package org.opendevstack.apiservice.serviceproject.service;

import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;

public interface ProjectService {

    ProjectResponse saveProject(ProjectRequest request);

    ProjectResponse getProject(String projectKey);
}

