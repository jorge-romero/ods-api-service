package org.opendevstack.apiservice.serviceproject.service.impl;

import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.opendevstack.apiservice.serviceproject.exception.ProjectExistenceServiceException;
import org.opendevstack.apiservice.serviceproject.service.ProjectExistenceService;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Set;

@Slf4j
@Service
public class ProjectExistenceServiceImpl implements ProjectExistenceService {

    private final BitbucketService bitbucketService;
    private final JiraService jiraService;
    private final OpenshiftService openshiftService;
    private final ProjectService projectService;

    @Autowired
    public ProjectExistenceServiceImpl(BitbucketService bitbucketService, JiraService jiraService, 
                                       OpenshiftService openshiftService, ProjectService projectService) {
        this.bitbucketService = bitbucketService;
        this.jiraService = jiraService;
        this.openshiftService = openshiftService;
        this.projectService = projectService;
    }
    @Override
    public boolean isProjectFound(String projectKey) throws ProjectExistenceServiceException {
        try {
            if (existsInCollection(projectKey)) return true;
            if (existsInAnyBitbucketInstance(projectKey)) return true;
            if (existsInAnyJiraInstance(projectKey)) return true;
            return existsInAnyOpenshift(projectKey);
        } catch (BitbucketException e) {
            throw new ProjectExistenceServiceException("Failed to check project in Bitbucket", e);
        } catch (JiraException e) {
            throw new ProjectExistenceServiceException("Failed to check project in Jira", e);
        } catch (OpenshiftException e) {
            throw new ProjectExistenceServiceException("Failed to check project in Openshift", e);
        } catch (RuntimeException e) {
            log.error("Unexpected error while checking project existence for key '{}'", projectKey, e);
            throw new ProjectExistenceServiceException("Unexpected error while checking project existence", e);
        }
    }

    @Override
    public boolean isProjectFoundByName(String projecName) throws ProjectExistenceServiceException {
        return !projectService.findProjectsByName(projecName).isEmpty();
    }

    @Override
    public boolean isProjectFoundInCollection(String projectKey) throws ProjectExistenceServiceException {
        return existsInCollection(projectKey);
    }

    private boolean existsInCollection(String projectKey) {
        return projectService.getProject(projectKey) != null;
    }

    private boolean existsInAnyBitbucketInstance(String projectKey) throws BitbucketException {
        Set<String> instances = bitbucketService.getAvailableInstances();
        for (String instance : instances.stream().sorted(Comparator.naturalOrder()).toList()) {
            if (bitbucketService.projectExists(instance, projectKey)) return true;
        }
        return false;
    }

    private boolean existsInAnyJiraInstance(String projectKey) throws JiraException {
        Set<String> instances = jiraService.getAvailableInstances();
        if (instances == null || instances.isEmpty()) {
            return jiraService.projectExists(projectKey);
        }
        for (String instance : instances.stream().sorted(Comparator.naturalOrder()).toList()) {
            if (jiraService.projectExists(instance, projectKey)) return true;
        }
        return false;
    }

    private boolean existsInAnyOpenshift(String projectKey) throws OpenshiftException {
        Set<String> instances = openshiftService.getAvailableInstances();
        if (instances == null || instances.isEmpty()) return false;
        for (String instance : instances.stream().sorted(Comparator.naturalOrder()).toList()) {
            if (openshiftService.projectExists(instance, projectKey)) return true;
        }
        return false;
    }
}
