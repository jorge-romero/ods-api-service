package org.opendevstack.apiservice.externalservice.projectsinfoservice.service;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.config.PlatformsConfiguration;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.config.ProjectFilterConfiguration;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.client.AzureGraphClient;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.client.PlatformsYmlClient;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.client.TestingHubClient;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.Link;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.Section;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.InvalidConfigurationException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.UnableToReachAzureException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class PlatformService {

    private static final String PROJECT_KEY_TOKEN = "${projectKey}";
    private static final String LOWER_PROJECT_KEY_TOKEN = "${project_key}";
    private static final String UPPER_PROJECT_KEY_TOKEN = "${PROJECT_KEY}";
    private static final String TESTING_HUB_PROJECT_TOKEN = "${testingHubProject}";

    PlatformsConfiguration platformsConfiguration;
    ProjectFilterConfiguration projectFilterConfiguration;
    PlatformsYmlClient platformsYmlClient;
    AzureGraphClient azureGraphClient;
    TestingHubClient testingHubClient;

    public PlatformsWithTitle getPlatforms(String projectKey, String cluster) {
        log.debug("Getting platform links for project {} and cluster {}.", projectKey, cluster);

        if (platformsConfiguration.getClusters().containsKey(cluster)) {
            var clusterConfigurationPath = platformsConfiguration.getClusters().get(cluster);
            var url = platformsConfiguration.getBasePath() + clusterConfigurationPath;

            var titleAndPlatforms = platformsYmlClient.fetchPlatformsFromYaml(url);
            var platforms = titleAndPlatforms.getValue();

            platforms.forEach(platform -> {
                var resolvedUrl = Optional.ofNullable(resolvePlatformUrl(platform.getUrl(), projectKey)).orElse("");

                platform.setUrl(resolvedUrl);
            });

            var platformsMap = platforms.stream()
                    .collect(Collectors.toMap(
                            Platform::getId,
                            Function.identity(),
                            (existing, replacement) -> existing, // merge function
                            LinkedHashMap::new // supplier to preserve order
                    ));

            return PlatformsWithTitle.builder()
                    .title(titleAndPlatforms.getKey())
                    .platforms(platformsMap)
                    .build();
        } else {
            String errorMessage = "Cluster " + cluster + " is not configured. All valid clusters: " +
                    platformsConfiguration.getClusters().keySet();

            throw new InvalidConfigurationException(errorMessage);
        }
    }

    private String resolvePlatformUrl(String urlTemplate, String projectKey) {
        if (urlTemplate.contains(TESTING_HUB_PROJECT_TOKEN)) {
            // TODO: Change to getAllProjects TestingHub notifies the changes on their API.
            Set<TestingHubProject> testingHubProjects = testingHubClient.getDefaultProjects();
            String testingHubValue = testingHubProjects.stream()
                    .filter(project -> project.getName().equals(projectKey))
                    .map(TestingHubProject::getId)
                    .findFirst()
                    .orElse(null);

            // If the project is not found in TestingHub, we cannot resolve the URL so we return null
            if (testingHubValue == null) {
                return null;
            }

            urlTemplate = urlTemplate.replace(TESTING_HUB_PROJECT_TOKEN, testingHubValue);
        }

        return replaceTokens(urlTemplate, Map.of(
            PROJECT_KEY_TOKEN, projectKey,
            UPPER_PROJECT_KEY_TOKEN, projectKey.toUpperCase(),
            LOWER_PROJECT_KEY_TOKEN, projectKey.toLowerCase()
        ));
    }

    public List<String> getDisabledPlatforms(String projectKey) {
        List<String> disabledPlatforms = new ArrayList<>();

        disabledPlatforms.addAll(addDisabledPlatformIfDataHubIsDisabled(projectKey));
        disabledPlatforms.addAll(addDisabledPlatformIfTestingHubIsDisabled(projectKey));

        return disabledPlatforms;
    }

    private List<String> addDisabledPlatformIfDataHubIsDisabled(String projectKey) {
        List<String> disabledPlatforms = new ArrayList<>();

        try {
            var dataHubGroups = azureGraphClient.getDataHubGroups();

            var isDataHubDisabled = checkIfProjectIsEnabledForGroups(projectKey, dataHubGroups);

            if (!isDataHubDisabled) {
                disabledPlatforms.add("datahub");
            }
        } catch (UnableToReachAzureException e) {
            log.error("Unable to reach Azure to get DataHub groups", e);

            // As agreed, if we cannot reach Azure, we consider DataHub as NOT disabled for all projects
        }


        return disabledPlatforms;
    }

    private List<String> addDisabledPlatformIfTestingHubIsDisabled(String projectKey) {
        List<String> disabledPlatforms = new ArrayList<>();

        //var testingHubProjects = testingHubClient.getAllProjects(); // TODO: This will be used when TestingHub notifies the changes on their API.
        var testingHubDefaultProjects = testingHubClient.getDefaultProjects();

        var isTestingHubEnabled = false;

        try {
            var testingHubProjects = extractIdsFromDefaultProjects(azureGraphClient.getTestingHubGroups(), testingHubDefaultProjects);

            isTestingHubEnabled = testingHubProjects.stream()
                    .anyMatch(testingHubProject -> testingHubProject.getName().equals(projectKey));
        } catch (UnableToReachAzureException e) {
            log.error("Unable to reach Azure to get Testing Hub groups", e);

            isTestingHubEnabled = testingHubDefaultProjects.stream()
                    .anyMatch(testingHubProject -> testingHubProject.getName().equals(projectKey));
        }

        if (isTestingHubEnabled) {
            log.trace("testingHub is enabled");
        } else {
            log.trace("testingHub is not enabled");

            disabledPlatforms.add("testinghub");
        }

        return disabledPlatforms;
    }

    private Set<TestingHubProject> extractIdsFromDefaultProjects(Set<String> testingHubGroups, Set<TestingHubProject> testingHubDefaultProjects) {
        return testingHubDefaultProjects.stream()
                .filter(project -> testingHubGroups.contains(project.getName()))
                .collect(Collectors.toSet());
    }

    public List<Section> getSections(String projectKey, String cluster) {
        log.debug("Getting sections for project {} and cluster {}.", projectKey, cluster);

        if (platformsConfiguration.getClusters().containsKey(cluster)) {
            var clusterConfigurationPath = platformsConfiguration.getClusters().get(cluster);
            var url = platformsConfiguration.getBasePath() + clusterConfigurationPath;

            List<PlatformSectionService> sections = platformsYmlClient.fetchSectionsFromYaml(url);

            return sections.stream()
                    .map(section -> replaceTokens(section, Map.of(
                            PROJECT_KEY_TOKEN, projectKey,
                            UPPER_PROJECT_KEY_TOKEN, projectKey.toUpperCase(),
                            LOWER_PROJECT_KEY_TOKEN, projectKey.toLowerCase()
                    )))
                    .toList();
        } else {
            String errorMessage = "Cluster " + cluster + " is not configured. All valid clusters: " +
                    platformsConfiguration.getClusters().keySet();

            throw new InvalidConfigurationException(errorMessage);
        }
    }

    private Section replaceTokens(PlatformSectionService section, Map<String, String> tokens) {
        log.debug("Replacing tokens {} for section {}", tokens, section);

        var updatedLinksWithTokens = section.getLinks().stream()
                .map(link -> Link.builder()
                        .url(replaceTokens(link.getUrl(), tokens))
                        .label(link.getLabel())
                        .type(link.getType())
                        .tooltip(link.getTooltip())
                        .build()
                ).toList();

        return Section.builder()
                .section(section.getSection())
                .tooltip(section.getTooltip())
                .links(updatedLinksWithTokens)
                .build();
    }

    private String replaceTokens(String source, Map<String, String> tokens) {
        String result = source;

        for (Map.Entry<String, String> tokenEntry : tokens.entrySet()) {
            result = result.replace(tokenEntry.getKey(), tokenEntry.getValue());
        }

        return result;
    }

    private boolean checkIfProjectIsEnabledForGroups(String projectKey, Set<String> applicationGroups) {
        var edpAzureProjects = applicationGroups.stream()
                .filter( group -> group.startsWith(projectFilterConfiguration.getProjectRolesGroupPrefix()) )
                .filter(group -> projectFilterConfiguration.getProjectRolesGroupSuffixes().stream().anyMatch(group::endsWith))
                .map(group -> group.replaceFirst(projectFilterConfiguration.getProjectRolesGroupPrefix() + "-", ""))
                .map( this::removeSuffixes)
                .collect(Collectors.toSet());

        return edpAzureProjects.contains(projectKey);
    }

    private String removeSuffixes(String azureGroupName) {
        String result = azureGroupName;

        for (String suffix : projectFilterConfiguration.getProjectRolesGroupSuffixes()) {
            var suffixWithSeparator = "-" + suffix.trim();

            if (result.endsWith(suffixWithSeparator)) {
                result = result.substring(0, result.length() - suffixWithSeparator.length());
                break; // Assuming only one suffix will match
            }
        }

        return result;
    }
}