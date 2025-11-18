package org.opendevstack.apiservice.externalservice.projectsinfoservice.service;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.config.OpenshiftClusterConfiguration;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.annotations.CacheableWithFallback;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.OpenshiftProjectCluster;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.ProjectList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OpenShiftProjectService {
    @Value("${externalservices.projects-info-service.openshift.api.project.url}")
    private String projectApiUrl;

    private final RestTemplate restTemplate;
    private final OpenshiftClusterConfiguration openshiftClusterConfig;

    public OpenShiftProjectService(RestTemplate restTemplate, OpenshiftClusterConfiguration openshiftClusterConfig) {
        this.restTemplate = restTemplate;
        this.openshiftClusterConfig = openshiftClusterConfig;
    }

    @CacheableWithFallback(primary = "openshiftProjects", fallback = "openshiftProjects-fallback", defaultValue = "T(java.util.Collections).emptyList()")
    public List<OpenshiftProjectCluster> fetchProjects() {
        final List<OpenshiftProjectCluster> result = new ArrayList<>();

        openshiftClusterConfig.getClusters().forEach((cluster, clusterValues) -> {
            log.debug("Fetching projects for cluster: {}", cluster);

            final HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + clusterValues.get("token"));
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            final String url = clusterValues.get("url") + projectApiUrl;
            final HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("Setting headers to request: {} for url {}", headers, url);

            try {
                ResponseEntity<ProjectList> response = restTemplate.exchange(url, HttpMethod.GET, entity, ProjectList.class);
                ProjectList body = response.getBody();

                if (body != null && body.getItems() != null) {
                    log.debug("Found {} projects for cluster {}:", body.getItems().size(), cluster);
                    log.debug("Projects: \n {}", body.getItems().stream()
                            .map(project -> project.getMetadata().getName())
                            .collect(Collectors.joining(", ")));

                    List<OpenshiftProjectCluster> clusterProjects = body.getItems().stream()
                            .filter(project -> project.getMetadata() != null)
                            .map(project -> project.getMetadata().getName())
                            .filter(projectName -> projectName.endsWith("-cd"))
                            .map(projectName -> projectName.replace("-cd", ""))
                            .map(String::toUpperCase)
                            .map(projectName -> OpenshiftProjectCluster.builder()
                                    .project(projectName)
                                    .cluster(cluster)
                                    .build())
                            .toList();

                    result.addAll(clusterProjects);
                }
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                log.error("HTTP error while fetching projects for cluster {}: {} - {}", cluster, e.getStatusCode(), e.getMessage());
            } catch (ResourceAccessException e) {
                log.error("Resource access error for cluster {}: {}", cluster, e.getMessage());
            } catch (RestClientException e) {
                log.error("Unexpected error while fetching projects for cluster {}: {}", cluster, e.getMessage());
            }
        });

        return result;
    }
}
