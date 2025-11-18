package org.opendevstack.apiservice.externalservice.projectsinfoservice.service;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.client.AzureGraphClient;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.client.PlatformsYmlClient;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.client.TestingHubClient;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.config.PlatformsConfiguration;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.config.ProjectFilterConfiguration;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.LinkMother;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.InvalidConfigurationException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformServiceTest {

    @Mock
    PlatformsConfiguration platformsConfiguration;
    @Mock
    ProjectFilterConfiguration projectFilterConfiguration;
    @Mock
    PlatformsYmlClient platformsYmlClient;
    @Mock
    AzureGraphClient azureGraphClient;
    @Mock
    TestingHubClient testingHubClient;

    @InjectMocks
    private PlatformService platformService;

    @Test
    void givenASetOfPlatforms_whenGetDisabledPlatforms_AndAzureIsWorking_AndNoPlatformAvailable_thenReturnTheExpectedList() {
        // given
        when(projectFilterConfiguration.getProjectRolesGroupPrefix()).thenReturn("project-roles-");

        when(azureGraphClient.getDataHubGroups()).thenReturn(Set.of("group1", "group2"));
        when(azureGraphClient.getTestingHubGroups()).thenReturn(Set.of("group2", "group3"));
        when(testingHubClient.getDefaultProjects()).thenReturn(Set.of(TestingHubProjectMother.of("anyProjectKey", "1")));

        // when
        var disabledPlatforms = platformService.getDisabledPlatforms("anyProjectKey");

        // then
        assertThat(disabledPlatforms).hasSize(2)
            .containsExactlyInAnyOrder("datahub", "testinghub");
    }

    @Test
    void givenAHardcodedSections_whenGetSections_thenReturnTheExpectedList() {
        // given
        var currentCluster = "unit-test-cluster";
        var projectKey = "anyProjectKey";

        when(platformsConfiguration.getBasePath()).thenReturn("https://any-base-path/");
        when(platformsConfiguration.getClusters()).thenReturn(
                Map.of(
                        currentCluster, "path/to/platforms.yml"
                )
        );
        var expectedUrl = "https://any-base-path/path/to/platforms.yml";

        when(platformsYmlClient.fetchSectionsFromYaml(expectedUrl)).thenReturn(buildExpectedPlatformSections());

        // when
        var sections = platformService.getSections(projectKey, currentCluster);

        // then
        assertThat(sections).isNotNull()
                .hasSize(3);

        assertThat(sections.getFirst())
                .extracting("section").isEqualTo("Project Shortcuts - Application Platforms");

        assertThat(sections.getFirst())
                .extracting("links").asInstanceOf(InstanceOfAssertFactories.LIST).isNotEmpty()
                .hasSize(9)
                .first().isEqualTo(LinkMother.of("JIRA", "https://www.google.com/" + projectKey, "general", "help text")); // Check that token is replaced

        assertThat(sections.get(1))
                .extracting("section").isEqualTo("Project Shortcuts - Data platform");

        assertThat(sections.get(2))
                .extracting("section").isEqualTo("Services");
    }

    private static List<PlatformSectionService> buildExpectedPlatformSections() {
        return List.of(
                PlatformSectionService.builder()
                        .section("Project Shortcuts - Application Platforms")
                        .links(
                                List.of(
                                        PlatformLinkMother.of("JIRA", "https://www.google.com/${projectKey}", "general", "help text"),
                                        PlatformLinkMother.of("Confluence", "https://www.google.com", "general", "help text"),
                                        PlatformLinkMother.of("SonarQube", "https://www.google.com", "general", "help text"),
                                        PlatformLinkMother.of("Nexus", "https://www.google.com", "general", "help text"),
                                        PlatformLinkMother.of("Jenkins", "https://www.google.com", "general", "help text"),
                                        PlatformLinkMother.of("Artifactory", "https://www.google.com", "general", "help text"),
                                        PlatformLinkMother.of("GitLab", "https://www.google.com", "general", "help text"),
                                        PlatformLinkMother.of("Harbor", "https://www.google.com", "general", "help text"),
                                        PlatformLinkMother.of("Kibana", "https://www.google.com", "general", "help text")
                                )
                        )
                        .build(),
                PlatformSectionService.builder()
                        .section("Project Shortcuts - Data platform")
                        .links(java.util.Collections.emptyList())
                        .build(),
                PlatformSectionService.builder()
                        .section("Services")
                        .links(java.util.Collections.emptyList())
                        .build()
        );
    }

    @Test
    void givenValidClusterAndPlatforms_whenGetPlatformLinks_thenReturnPlatformLinksMap() {
        // given
        var cluster = "test-cluster";
        var projectKey = "testProject";
        var basePath = "https://config.example.com/";
        var clusterPath = "clusters/test-cluster.yml";

        when(platformsConfiguration.getClusters()).thenReturn(Map.of(cluster, clusterPath));
        when(platformsConfiguration.getBasePath()).thenReturn(basePath);

        var jiraPlatform = createPlatform("jira", "JIRA", "https://jira.example.com");
        var gitlabPlatform = createPlatform("gitlab", "GitLab", "https://gitlab.example.com/${project_key}");
        var sonarPlatform = createPlatform("sonar", "SonarQube", "https://sonar.example.com/${PROJECT_KEY}/dashboard");

        var platforms = Pair.of("Simple title", List.of(jiraPlatform, gitlabPlatform, sonarPlatform));

        when(platformsYmlClient.fetchPlatformsFromYaml(basePath + clusterPath)).thenReturn(platforms);

        // when
        var result = platformService.getPlatforms(projectKey, cluster);

        // then

        assertThat(result).isNotNull()
                .extracting("title").isEqualTo("Simple title");

        assertThat(result.getPlatforms()).isNotNull()
                .containsEntry("jira", jiraPlatform)
                .containsEntry("gitlab", gitlabPlatform)
                .containsEntry("sonar", sonarPlatform);

    }

    @Test
    void givenPlatformWithTestingHubToken_whenGetPlatformLinks_AndProjectExists_thenReturnResolvedUrl() {
        // given
        var cluster = "test-cluster";
        var projectKey = "testProject";
        var basePath = "https://config.example.com/";
        var clusterPath = "clusters/test-cluster.yml";

        when(platformsConfiguration.getClusters()).thenReturn(Map.of(cluster, clusterPath));
        when(platformsConfiguration.getBasePath()).thenReturn(basePath);

        var testingHubPlatform = createPlatform("testinghub", "TestingHub", "https://testinghub.example.com/project/${testingHubProject}");

        var platforms = Pair.of("simple title", List.of(testingHubPlatform));

        Set<TestingHubProject> testingHubProjects = Set.of(
                TestingHubProjectMother.of("testProject", "12345"),
                TestingHubProjectMother.of("otherProject", "67890")
        );

        when(platformsYmlClient.fetchPlatformsFromYaml(basePath + clusterPath)).thenReturn(platforms);
        when(testingHubClient.getDefaultProjects()).thenReturn(testingHubProjects);

        // when
        var result = platformService.getPlatforms(projectKey, cluster);

        // then
        assertThat(result.getPlatforms()).isNotNull()
                .hasSize(1)
                .containsEntry("testinghub", testingHubPlatform);
    }

    @Test
    void givenPlatformWithTestingHubToken_whenGetPlatformLinks_AndProjectNotFound_thenReturnEmptyUrl() {
        // given
        var cluster = "test-cluster";
        var projectKey = "nonExistentProject";
        var basePath = "https://config.example.com/";
        var clusterPath = "clusters/test-cluster.yml";

        when(platformsConfiguration.getClusters()).thenReturn(Map.of(cluster, clusterPath));
        when(platformsConfiguration.getBasePath()).thenReturn(basePath);

        var testingHubPlatform = createPlatform("testinghub", "TestingHub", "https://testinghub.example.com/project/${testingHubProject}");
        var jiraPlatform = createPlatform("jira", "JIRA", "https://jira.example.com/${project_ey}");

        var platforms = Pair.of("simple title", List.of(testingHubPlatform, jiraPlatform));

        Set<TestingHubProject> testingHubProjects = Set.of(
                TestingHubProjectMother.of("testProject", "12345")
        );

        when(platformsYmlClient.fetchPlatformsFromYaml(basePath + clusterPath)).thenReturn(platforms);
        when(testingHubClient.getDefaultProjects()).thenReturn(testingHubProjects);

        // when
        var result = platformService.getPlatforms(projectKey, cluster);

        // then
        assertThat(result).isNotNull();

        assertThat(result.getPlatforms().get("testinghub").getUrl()).isEmpty();  // Empty because project not found in TestingHub
    }

    @Test
    void givenPlatformWithBothTokens_whenGetPlatformLinks_thenResolveBothTokens() {
        // given
        var cluster = "test-cluster";
        var projectKey = "testProject";
        var basePath = "https://config.example.com/";
        var clusterPath = "clusters/test-cluster.yml";

        when(platformsConfiguration.getClusters()).thenReturn(Map.of(cluster, clusterPath));
        when(platformsConfiguration.getBasePath()).thenReturn(basePath);

        var platforms = Pair.of("title", List.of(
                createPlatform("custom", "Custom", "https://custom.example.com/${testingHubProject}/${project_key}/view")
            )
        );

        Set<TestingHubProject> testingHubProjects = Set.of(
                TestingHubProjectMother.of("testProject", "12345")
        );

        when(platformsYmlClient.fetchPlatformsFromYaml(basePath + clusterPath)).thenReturn(platforms);
        when(testingHubClient.getDefaultProjects()).thenReturn(testingHubProjects);

        // when
        var result = platformService.getPlatforms(projectKey, cluster);

        // then
        assertThat(result.getPlatforms()).isNotNull()
                .hasSize(1);

        assertThat(result.getPlatforms().get("custom").getUrl()).isEqualTo("https://custom.example.com/12345/testproject/view");
    }

    @Test
    void givenInvalidCluster_whenGetPlatformLinks_thenThrowInvalidConfigurationException() {
        // given
        var invalidCluster = "non-existent-cluster";
        var projectKey = "testProject";

        when(platformsConfiguration.getClusters()).thenReturn(Map.of(
                "cluster1", "path1.yml",
                "cluster2", "path2.yml"
        ));

        // when & then
        assertThatThrownBy(() -> platformService.getPlatforms(projectKey, invalidCluster))
                .isInstanceOf(InvalidConfigurationException.class)
                .hasMessageContaining("Cluster " + invalidCluster + " is not configured")
                .hasMessageContaining("cluster1")
                .hasMessageContaining("cluster2");
    }

    @Test
    void givenEmptyPlatformList_whenGetPlatformLinks_thenReturnEmptyMap() {
        // given
        var cluster = "test-cluster";
        var projectKey = "testProject";
        var basePath = "https://config.example.com/";
        var clusterPath = "clusters/test-cluster.yml";

        when(platformsConfiguration.getClusters()).thenReturn(Map.of(cluster, clusterPath));
        when(platformsConfiguration.getBasePath()).thenReturn(basePath);
        when(platformsYmlClient.fetchPlatformsFromYaml(basePath + clusterPath)).thenReturn(Pair.of("", List.of()));

        // when
        var result = platformService.getPlatforms(projectKey, cluster);

        // then
        assertThat(result.getPlatforms()).isNotNull().isEmpty();
    }

    private Platform createPlatform(String id, String label, String url) {
        Platform platform = new Platform();
        platform.setId(id);
        platform.setLabel(label);
        platform.setUrl(url);
        return platform;
    }
}
