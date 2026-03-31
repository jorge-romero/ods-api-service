package org.opendevstack.apiservice.project.facade;

import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;

import java.util.UUID;

public interface ProjectsFacade {

    CreateProjectResponse createProject(CreateProjectRequest request, UUID clientId);

    CreateProjectResponse getProject(String projectKey);
}