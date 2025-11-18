package org.opendevstack.apiservice.externalservice.projectsinfoservice.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.client.AzureGraphClient;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.Link;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.ProjectInfoMother;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.ProjectPlatforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.SectionMother;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.OpenshiftProjectCluster;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.OpenshiftProjectClusterMother;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformMother;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformsWithTitleMother;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.MockProjectsService;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.OpenShiftProjectService;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.PlatformService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectsFacadeTest {

    @Mock
    private AzureGraphClient azureGraphClient;

    @Mock
    private OpenShiftProjectService openShiftProjectService;

    @Mock
    private MockProjectsService mockProjectsService;

    @Mock
    private PlatformService platformService;

    @InjectMocks
    private ProjectsFacade projectsFacade;

    @Test
    void givenAProjectKey_whenGetProjectPlatforms_ThenPlatformsAreReturned() {
        // given
        var openshiftProjectCluster = OpenshiftProjectClusterMother.of();
        var projectKey = openshiftProjectCluster.getProject();
        var cluster = openshiftProjectCluster.getCluster();
        var disabledPlatforms = List.of("platform1", "platform2");
        var expectedSection = SectionMother.of();
        var expectedSections = List.of(expectedSection);
        var expectedPlatforms = PlatformsWithTitleMother.of(
                Map.of(
                        "platform1", PlatformMother.of("platform1", "Platform 1 label"),
                        "platform2", PlatformMother.of("platform2", "Platform 2 label"),
                        "platform3", PlatformMother.of("platform2", "Platform 3 label")
                )
        );

        List<OpenshiftProjectCluster> projectClusters = List.of(openshiftProjectCluster);

        when(openShiftProjectService.fetchProjects()).thenReturn(projectClusters);

        when(platformService.getDisabledPlatforms(projectKey)).thenReturn(disabledPlatforms);
        when(platformService.getPlatforms(projectKey, cluster)).thenReturn(expectedPlatforms);
        when(platformService.getSections(projectKey, cluster)).thenReturn(expectedSections);

        // when
        ProjectPlatforms result = projectsFacade.getProjectPlatforms(projectKey);

        // then
        assertThat(result).isNotNull();

        var sections = result.getSections();

        assertThat(sections).isNotNull()
                .hasSize(2);

        assertThat(sections.get(0).getSection()).isEqualTo("Simple title");
        assertThat(sections.get(0).getLinks()).contains(Link.builder()
                .label("Platform 1 label")
                .url("http://www.example.com/platform1")
                .abbreviation("ABRPLATFORM1")
                .type("platform")
                .disabled(true)
                .build());
        assertThat(sections.get(1)).isEqualTo(expectedSection);

    }

    @Test
    void givenAProjectKey_whenGetProjectPlatforms_AndProjectNotInOpenshift_ThenReturnNull() {
        // given
        var projectKey = "sampleProject";

        List<OpenshiftProjectCluster> projectClusters = List.of(OpenshiftProjectClusterMother.of());

        when(openShiftProjectService.fetchProjects()).thenReturn(projectClusters);

        // when
        ProjectPlatforms result = projectsFacade.getProjectPlatforms(projectKey);

        // then
        assertThat(result).isNull();
    }

    @Test
    void givenAProjectKey_whenGetProjectPlatforms_AndProjectNotInOpenshift_butMockedProjects_ThenReturnMockProject() {
        // given
        var projectKey = "mock-project-key";
        var projectInfo = ProjectInfoMother.of(projectKey);

        List<OpenshiftProjectCluster> projectClusters = List.of(OpenshiftProjectClusterMother.of());
        var disabledPlatforms = List.of("datahub", "testinghub");
        var expectedSection = SectionMother.of();
        var expectedSections = List.of(expectedSection);

        when(openShiftProjectService.fetchProjects()).thenReturn(projectClusters);
        when(mockProjectsService.getDefaultProjectsAndClusters()).thenReturn(Map.of(projectKey, projectInfo));

        when(platformService.getDisabledPlatforms(projectKey)).thenReturn(disabledPlatforms);
        when(platformService.getSections(projectKey, projectInfo.getClusters().getFirst())).thenReturn(expectedSections);
        when(platformService.getPlatforms(projectKey, projectInfo.getClusters().getFirst())).thenReturn(PlatformsWithTitleMother.of());

        // when
        ProjectPlatforms result = projectsFacade.getProjectPlatforms(projectKey);

        // then
        assertThat(result).isNotNull();

        var sections = result.getSections();

        assertThat(sections).isNotNull()
                .hasSize(2);

        assertThat(sections.get(0).getSection()).isEqualTo("Simple title");
        assertThat(sections.get(1)).isEqualTo(expectedSection);

    }

    @Test
    void givenAProjectKey_andProjectExistsInOpenshift_andThereAreMockedProjectWithSameKey_whenGetProjectPlatforms_ThenOpenshiftProjectClustersArePrioritized() {
        // given
        var openshiftProjectCluster = OpenshiftProjectClusterMother.of();
        var projectKey = openshiftProjectCluster.getProject();
        var cluster = openshiftProjectCluster.getCluster();
        var projectInfo = ProjectInfoMother.of(projectKey);
        var disabledPlatforms = List.of("datahub", "testinghub");
        var expectedSection = SectionMother.of();
        var expectedSections = List.of(expectedSection);

        List<OpenshiftProjectCluster> projectClusters = List.of(openshiftProjectCluster);

        when(openShiftProjectService.fetchProjects()).thenReturn(projectClusters);
        when(mockProjectsService.getDefaultProjectsAndClusters()).thenReturn(Map.of(projectKey, projectInfo));

        when(platformService.getDisabledPlatforms(projectKey)).thenReturn(disabledPlatforms);
        when(platformService.getSections(projectKey, cluster)).thenReturn(expectedSections);
        when(platformService.getPlatforms(projectKey, cluster)).thenReturn(PlatformsWithTitleMother.of());

        // when
        ProjectPlatforms result = projectsFacade.getProjectPlatforms(projectKey);

        // then
        assertThat(result).isNotNull();

        var sections = result.getSections();

        assertThat(sections).isNotNull()
                .hasSize(2);

        assertThat(sections.get(0).getSection()).isEqualTo("Simple title");
        assertThat(sections.get(1)).isEqualTo(expectedSection);
    }
}
