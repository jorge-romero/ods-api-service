package org.opendevstack.apiservice.externalservice.projectsinfoservice.service.impl;


import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.ProjectPlatforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.InvalidContentProcessException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.facade.ProjectsFacade;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformLink;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformSection;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.ProjectsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ProjectsInfoService.
 * This service provides integration with projects info service to retrieve projects details.
 */
@Service
@Slf4j
public class ProjectsInfoServiceImpl implements ProjectsInfoService {

    @Qualifier("projectsInfoServiceRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${externalservices.projects-info-service.base-url:http://localhost:8080}")
    private String baseUrl;

    private final ProjectsFacade projectsFacade;

    public ProjectsInfoServiceImpl(RestTemplate restTemplate, ProjectsFacade projectsFacade) {
        this.restTemplate = restTemplate;
        this.projectsFacade = projectsFacade;
    }

    @Override
    public Platforms getProjectPlatforms(String projectKey) throws ProjectsInfoServiceException {
        log.debug("Getting project platforms");

        var projectPlatforms = projectsFacade.getProjectPlatforms(projectKey);

        if (projectPlatforms == null) {
            return null;
        } else {
            List<PlatformSection> platformSections = projectPlatforms.getSections().stream()
                    .map(PlatformSection::new)
                    .toList();

            var platforms = new Platforms();
            platforms.setSections(platformSections);

            return platforms;
        }
    }

    @Override
    public boolean validateConnection() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = baseUrl + "/actuator/health";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});

            boolean isValid = response.getStatusCode().is2xxSuccessful();
            log.debug("Connection validation: {}", isValid ? "successful" : "failed");
            return isValid;

        } catch (Exception e) {
            log.warn("Connection validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Use validateConnection for health checks, but don't log warnings on failure
            // as health checks are frequent and failures are expected to be handled by the health indicator
            return validateConnection();
        } catch (Exception e) {
            log.debug("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
