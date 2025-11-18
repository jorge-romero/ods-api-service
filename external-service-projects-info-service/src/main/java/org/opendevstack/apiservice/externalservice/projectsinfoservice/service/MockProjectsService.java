package org.opendevstack.apiservice.externalservice.projectsinfoservice.service;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.config.MockConfiguration;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.ProjectInfo;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.InvalidContentProcessException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
@Service
public class MockProjectsService {

    private final MockConfiguration mockConfiguration;

    public Map<String, ProjectInfo> getProjectsAndClusters(String userEmail) {
        Map<String, ProjectInfo> defaultProjects = getDefaultProjectsAndClusters();
        Map<String, ProjectInfo> userProjects = getUserProjectsAndClusters(userEmail);

        return merge(defaultProjects, userProjects);
    }

    public Map<String, ProjectInfo> getDefaultProjectsAndClusters() {
        return mockConfiguration.getDefaultProjects().stream()
                .map(String::trim)
                .map(this::extractProjectAndCluster)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, ProjectInfo> getUserProjectsAndClusters(String userEmail) {
        return getUserProjects(userEmail).stream()
                .map(this::extractProjectAndCluster)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Set<String> getUserProjects(String userEmail) {
        var projectsByUsers = mockConfiguration.getUsersProjects().replace("{", "")
                .replace("}", "")
                .split(";");

        var userConfigurations = Stream.of(projectsByUsers)
                .map(String::trim)
                .filter(s -> s.startsWith(userEmail))
                .map(s -> s.replace(userEmail + ":", "").trim())
                .toList();

        return userConfigurations.stream()
                .map(this::getSingleUserProjects)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private Set<String> getSingleUserProjects(String userProjects) {
        if (userProjects.equals("[]")) {
            return Collections.emptySet();
        } else {
            if (!userProjects.startsWith("[") || !userProjects.endsWith("]")) {
                log.error("User projects string is not well formatted: {}", userProjects);

                throw new InvalidContentProcessException("User projects string is not well formatted: " + userProjects);
            }

            var projects = userProjects.substring(userProjects.indexOf("[") + 1, userProjects.indexOf("]"))
                    .split(",");

            return Stream.of(projects)
                    .map(String::trim)
                    .map(this::extractProjectAndCluster)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }
    }

    private Map.Entry<String, ProjectInfo> extractProjectAndCluster(String projectWithCluster) {
        if (projectWithCluster.contains(":")) {
            var parts = projectWithCluster.split(":");

            var key = parts[0].trim();
            var projectInfo = new ProjectInfo(key, List.of(parts[1].trim()));

            return Map.entry(key, projectInfo);
        } else {
            var key = projectWithCluster.trim();
            var projectInfo = new ProjectInfo(key, mockConfiguration.getClusters().stream()
                    .toList());

            return Map.entry(key, projectInfo);
        }
    }

    private  Map<String, ProjectInfo> merge(Map<String, ProjectInfo> map1, Map<String, ProjectInfo> map2) {
        Set<String> allKeysSet = Stream.concat(map1.keySet().stream(), map2.keySet().stream())
                .collect(Collectors.toSet());

        Map<String, ProjectInfo> result = new HashMap<>();

        for (String key : allKeysSet) {
            ProjectInfo projectInfo1 = map1.getOrDefault(key, new ProjectInfo(key, Collections.emptyList()));
            ProjectInfo projectInfo2 = map2.getOrDefault(key, new ProjectInfo(key, Collections.emptyList()));

            List<String> mergedClusters = Stream.concat(
                    projectInfo1.getClusters().stream(),
                    projectInfo2.getClusters().stream()
                )
                .map(String::trim)
                .distinct()
                .toList();

            result.put(key, new ProjectInfo(key, mergedClusters));
        }

        return result;
    }
}
