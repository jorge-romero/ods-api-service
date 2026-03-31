package org.opendevstack.apiservice.serviceproject.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.serviceproject.exception.ProjectExistenceServiceException;
import org.opendevstack.apiservice.serviceproject.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;
import org.opendevstack.apiservice.serviceproject.service.ProjectExistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
@Slf4j
public class GenerateProjectKeyServiceImpl implements GenerateProjectKeyService {

    private static final int MAX_RETRIES = 10;

    private final ProjectExistenceService projectExistenceService;
    
    private final Random random;
    
    @Autowired
    public GenerateProjectKeyServiceImpl(ProjectExistenceService projectExistenceService) {
        this(projectExistenceService, new SecureRandom());
    }
    
    GenerateProjectKeyServiceImpl(ProjectExistenceService projectExistenceService, Random random) {
        this.projectExistenceService = projectExistenceService;
        this.random = random;
    }

    @Override
    public String generateProjectKey(String projectKeyPattern) throws ProjectKeyGenerationException, ProjectExistenceServiceException {
        String pattern = resolveProjectKeyPattern(projectKeyPattern);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            int randomNumber = random.nextInt(1_000_000);
            String projectKey = String.format(pattern, randomNumber);

            if (!projectExistenceService.isProjectFound(projectKey)) {
                log.debug("Generated unique project key '{}' on attempt {}", projectKey, attempt);
                return projectKey;
            }

            log.debug("Project key '{}' already exists (attempt {}/{})", projectKey, attempt, MAX_RETRIES);
        }

        throw new ProjectKeyGenerationException(
                String.format("Failed to generate unique project key after %d retries", MAX_RETRIES));
    }

    private String resolveProjectKeyPattern(String projectKeyPattern) {
        if (projectKeyPattern == null || projectKeyPattern.isBlank()) {
            return DEFAULT_PROJECT_KEY_PATTERN;
        }
        return projectKeyPattern;
    }
}
