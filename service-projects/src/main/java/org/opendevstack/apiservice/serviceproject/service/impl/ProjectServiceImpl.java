package org.opendevstack.apiservice.serviceproject.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.opendevstack.apiservice.serviceproject.mapper.ProjectResponseMapper;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final OpenshiftService openshiftService;
    
    private final BitbucketService bitbucketService;
    
    private final JiraService jiraService;
    
    private final GenerateProjectKeyService generateProjectKeyService;
    
    private final ProjectRepository projectRepository;
    
    private final ProjectResponseMapper projectResponseMapper;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        return ProjectResponse.builder().build();
    }

    @Override
    public ProjectResponse getProject(String projectKey) {
        Optional<ProjectEntity> project = projectRepository.findByProjectKeyIgnoreCase(projectKey);
        
        if (project.isPresent()) {
            return projectResponseMapper.toCreateProjectResponse(project.get());
        }
        
        return null;
    }
}

