package org.opendevstack.apiservice.externalservice.projectsinfoservice.client;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.TestingHubProject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TestingHubClient {

    @Value("${externalservices.projects-info-service.testing-hub.api.url}")
    private String url;

    @Value("${externalservices.projects-info-service.testing-hub.api.token}")
    private String token;

    @Value("${externalservices.projects-info-service.testing-hub.api.page-size}")
    private String pageSize;

    @Value("#{'${externalservices.projects-info-service.testing-hub.default.projects}'.split(',')}")
    public List<String> defaultProjects;

    private final RestTemplate restTemplate;
    private final ObjectMapper jacksonMapper;

    public TestingHubClient(RestTemplate restTemplate, ObjectMapper jacksonMapper) {
        this.restTemplate = restTemplate;
        this.jacksonMapper = jacksonMapper;
    }

    public Set<TestingHubProject> getDefaultProjects() {
        return defaultProjects.stream()
                .map(String::trim)
                .map(this::parseKeyAndId)
                .map(keyAndId -> new TestingHubProject(keyAndId.getRight(), keyAndId.getLeft()))
                .collect(Collectors.toSet());
    }

    // TODO: This method will be used when TestingHub notifies the changes on their API.
    public Set<TestingHubProject> getAllProjects() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("X-Id-Token", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Let's discuss later about pagination handling
        var requestUrl = url + "?pageNumber=0&itemsPerPage=" + pageSize;

        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            String responseBody = response.getBody();

            try {
                return jacksonMapper.readValue(responseBody, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize response", e);

                return Collections.emptySet();
            }
        } else {
            log.error("Failed to fetch projects from Testing Hub. Status code: {}", response.getStatusCode());

            return Collections.emptySet();
        }
    }

    private Pair<String, String> parseKeyAndId(String keyAndId) {
        var parts = keyAndId.split(":");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid project key and id format: " + keyAndId +
                    ". Expected format: <projectKey>:<projectId>");
        }

        return new ImmutablePair<>(parts[0], parts[1]);
    }
}
