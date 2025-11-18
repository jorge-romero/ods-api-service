package org.opendevstack.apiservice.externalservice.projectsinfoservice.service;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.config.MockConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class MockProjectsServiceTest {

    @Test
    void givenAMockConfiguration_whenGetProjectsAndClusters_thenReturnExpectedProjects() {
        // Given
        MockConfiguration mockConfiguration = new MockConfiguration();
        mockConfiguration.setClusters(List.of("US-TEST", "eu", "CN"));
        mockConfiguration.setDefaultProjects(List.of("DEFAULT1", "DEFAULT2:cn"));
        mockConfiguration.setUsersProjects("{PEPE:[PROJECT-3, PROJECT-4:US-TEST]; PPT:[PROJECT-3, PROJECT-5]}");

        var userEmail = "PEPE";

        MockProjectsService mockProjectsService = new MockProjectsService(mockConfiguration);

        // when
        var projects = mockProjectsService.getProjectsAndClusters(userEmail);

        // then
        assertThat(projects.size()).isEqualTo(4);
        assertThat(projects.keySet()).containsExactlyInAnyOrder("DEFAULT1", "DEFAULT2", "PROJECT-3", "PROJECT-4");
    }
}
