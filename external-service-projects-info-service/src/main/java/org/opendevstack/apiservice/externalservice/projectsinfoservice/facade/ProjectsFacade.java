package org.opendevstack.apiservice.externalservice.projectsinfoservice.facade;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.client.AzureGraphClient;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.Link;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.ProjectPlatforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.Section;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platform;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformsWithTitle;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.MockProjectsService;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.OpenShiftProjectService;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.PlatformService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ProjectsFacade {

    private final AzureGraphClient azureGraphClient;

    private final OpenShiftProjectService openShiftProjectService;

    private final MockProjectsService mockProjectsService;

    private final PlatformService platformService;

    private Map<String, String> clusterMapper;

    public ProjectsFacade(AzureGraphClient azureGraphClient,
                          OpenShiftProjectService openShiftProjectService,
                          MockProjectsService mockProjectsService,
                          PlatformService platformService) {
        this.azureGraphClient = azureGraphClient;
        this.openShiftProjectService = openShiftProjectService;
        this.mockProjectsService = mockProjectsService;
        this.platformService = platformService;
    }

    public ProjectPlatforms getProjectPlatforms(String projectKey) {
        var allEdpProjectsInfo = openShiftProjectService.fetchProjects();
        var mockProjectsAndClusters = mockProjectsService.getDefaultProjectsAndClusters();

        var edProjectInfo = allEdpProjectsInfo.stream()
                .filter(p -> p.getProject().equals(projectKey))
                .findFirst();

        var mockClusters = mockProjectsAndClusters.entrySet().stream()
                .filter(e -> e.getValue().getProjectKey().equals(projectKey))
                .flatMap(e -> e.getValue().getClusters().stream())
                .toList();

        // If EDP project exists, add its clusters to the front of the list, so we prioritize them
        var mergedClusters = edProjectInfo.map(projectInfo -> {
            List<String> clusters = new ArrayList<>();

            clusters.add(projectInfo.getCluster());

            clusters.addAll(mockClusters);

            return List.copyOf(clusters); // We always prefer immutable lists
        }).orElse(mockClusters);

        if (mergedClusters.isEmpty()) {
            log.debug("Project not found: {}", projectKey);

            return null;
        } else {
            log.debug("Project found: {}, returning ProjectPlatforms for clusters: {}.", projectKey, mergedClusters);

            List<Section> sections = new ArrayList<>(platformService.getSections(projectKey, mergedClusters.getFirst()));
            var disabledPlatforms = platformService.getDisabledPlatforms(projectKey);
            var platformsWithTitle = platformService.getPlatforms(projectKey, mergedClusters.getFirst());

            var firstSection = componseFirstSection(platformsWithTitle, disabledPlatforms);

            sections.addFirst(firstSection);

            return ProjectPlatforms.builder()
                    .sections(sections)
                    .build();
        }
    }

    private Section componseFirstSection(PlatformsWithTitle platformsWithTitle, List<String> disabledPlatforms) {
        var links = platformsWithTitle.getPlatforms().entrySet().stream()
                .map(entry -> Link.builder()
                        .label(entry.getValue().getLabel())
                        .url(entry.getValue().getUrl())
                        .type("platform")
                        .disabled(disabledPlatforms.contains(entry.getKey()))
                        .abbreviation(entry.getValue().getAbbreviation())
                        .build()
                )
                .toList();

        return Section.builder()
                .section(platformsWithTitle.getTitle())
                .links(links)
                .build();
    }

}
