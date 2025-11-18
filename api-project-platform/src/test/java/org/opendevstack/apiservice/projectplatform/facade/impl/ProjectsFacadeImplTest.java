package org.opendevstack.apiservice.projectplatform.facade.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.ProjectsInfoService;
import org.opendevstack.apiservice.projectplatform.exception.ProjectPlatformsException;
import org.opendevstack.apiservice.projectplatform.mapper.ProjectPlatformsMapper;
import org.opendevstack.apiservice.projectplatform.model.Link;
import org.opendevstack.apiservice.projectplatform.model.ProjectPlatforms;
import org.opendevstack.apiservice.projectplatform.model.Section;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectsFacadeImplTest {

    @Mock
    private ProjectsInfoService projectsInfoService;

    @Mock
    private ProjectPlatformsMapper mapper;

    private ProjectsFacadeImpl facade;

    @BeforeEach
    void setUp() {
        facade = new ProjectsFacadeImpl(projectsInfoService, mapper);
    }

    @Test
    void givenAnyProjectKey_whenGetProjectPlatforms_thenGetMockProjectPlatforms() throws ProjectsInfoServiceException, ProjectPlatformsException {
        // Arrange
        String projectKey = "DEVSTACK";

        // Create external service response
        Platforms externalPlatforms = new Platforms();

        // Create expected API response
        ProjectPlatforms expectedPlatforms = createExpectedProjectPlatforms();

        // Mock behavior
        when(projectsInfoService.getProjectPlatforms(projectKey)).thenReturn(externalPlatforms);
        when(mapper.toApiModel(externalPlatforms)).thenReturn(expectedPlatforms);

        // Act
        ProjectPlatforms result = facade.getProjectPlatforms(projectKey);

        // Assert
        assertNotNull(result, "Result should not be null");

        List<Section> sections = result.getSections();
        assertNotNull(sections, "Sections should not be null");
        assertEquals(3, sections.size(), "There should be 3 sections");

        // Validate first section
        Section appPlatformSection = sections.get(0);
        assertEquals("Project Shortcuts - Application Platform", appPlatformSection.getSection());
        assertEquals(4, appPlatformSection.getLinks().size());
        assertTrue(appPlatformSection.getLinks().stream().anyMatch(link -> link.getLabel().equals("JIRA")));
        assertTrue(appPlatformSection.getLinks().stream().allMatch(link -> link.getUrl().equals("https://www.google.com")));

        // Validate second section
        Section dataPlatformSection = sections.get(1);
        assertEquals("Project Shortcuts - Data Platform", dataPlatformSection.getSection());
        assertEquals(2, dataPlatformSection.getLinks().size());

        // Validate third section
        Section servicesSection = sections.get(2);
        assertEquals("Services", servicesSection.getSection());
        assertEquals(3, servicesSection.getLinks().size());

        // Verify interactions
        verify(projectsInfoService, times(1)).getProjectPlatforms(projectKey);
        verify(mapper, times(1)).toApiModel(externalPlatforms);
    }

    @Test
    void givenProjectsInfoServiceThrowsException_whenGetProjectPlatforms_thenRuntimeExceptionIsThrown() throws ProjectsInfoServiceException {
        // Arrange
        String projectKey = "DEVSTACK";
        when(projectsInfoService.getProjectPlatforms(projectKey))
                .thenThrow(new ProjectsInfoServiceException("Service error"));

        // Act & Assert
        ProjectPlatformsException exception = assertThrows(ProjectPlatformsException.class, () -> facade.getProjectPlatforms(projectKey));

        assertEquals("Failed to retrieve project platforms", exception.getMessage());
        verify(projectsInfoService, times(1)).getProjectPlatforms(projectKey);
        verify(mapper, never()).toApiModel(any());
    }

    private ProjectPlatforms createExpectedProjectPlatforms() {
        ProjectPlatforms platforms = new ProjectPlatforms();

        // Set sections
        Section appPlatformSection = new Section("Project Shortcuts - Application Platform", "tooltip", List.of(
                new Link("JIRA", "https://www.google.com", "tooltip", "type", "abbreviation", false),
                new Link("Bitbucket", "https://www.google.com", "tooltip", "type", "abbreviation", false),
                new Link("Confluence", "https://www.google.com", "tooltip", "type", "abbreviation", false),
                new Link("Jenkins", "https://www.google.com", "tooltip", "type", "abbreviation", false)
        ));

        Section dataPlatformSection = new Section("Project Shortcuts - Data Platform", "tooltip", List.of(
                new Link("EKG", "https://www.google.com", "tooltip", "type", "abbreviation", false),
                new Link("EDGC", "https://www.google.com", "tooltip", "type", "abbreviation", false)
        ));

        Section servicesSection = new Section("Services", "tooltip", List.of(
                new Link("Service Onboarding", "https://www.google.com", "tooltip", "type", "abbreviation", false),
                new Link("Documentation", "https://www.google.com", "tooltip", "type", "abbreviation", false),
                new Link("Service Training", "https://www.google.com", "tooltip", "type", "abbreviation", false)
        ));

        platforms.setSections(List.of(appPlatformSection, dataPlatformSection, servicesSection));

        return platforms;
    }
}

