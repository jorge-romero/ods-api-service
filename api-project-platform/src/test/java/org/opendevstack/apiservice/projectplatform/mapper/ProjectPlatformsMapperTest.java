package org.opendevstack.apiservice.projectplatform.mapper;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformSection;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformSectionLink;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.projectplatform.model.ProjectPlatforms;
import org.opendevstack.apiservice.projectplatform.model.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ProjectPlatformsMapperTest {

    @InjectMocks
    private ProjectPlatformsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProjectPlatformsMapper();
    }

    @Test
    void testToApiModel_WithNullInput_ReturnsNull() {
        // When
        ProjectPlatforms result = mapper.toApiModel(null);

        // Then
        assertNull(result);
    }

    @Test
    void testToApiModel_WithCompleteData_MapsAllFields() {
        // Given
        Platforms externalPlatforms =
                createCompleteExternalProjectPlatforms();

        // When
        ProjectPlatforms result = mapper.toApiModel(externalPlatforms);

        // Then
        assertNotNull(result);

        assertNotNull(result.getSections());
        assertEquals(2, result.getSections().size());

        Section firstSection = result.getSections().getFirst();
        assertEquals("Development", firstSection.getSection());
        assertEquals(2, firstSection.getLinks().size());
        assertEquals("GitHub Repo", firstSection.getLinks().getFirst().getLabel());
        assertEquals("https://github.com/repo", firstSection.getLinks().getFirst().getUrl());

        Section secondSection = result.getSections().get(1);
        assertEquals("CI/CD", secondSection.getSection());
        assertEquals(1, secondSection.getLinks().size());
        assertEquals("Jenkins", secondSection.getLinks().getFirst().getLabel());
        assertEquals("https://jenkins.com/job", secondSection.getLinks().getFirst().getUrl());
    }

    @Test
    void testToApiModel_WithNullPlatformLinks_MapsOtherFields() {
        // Given
        Platforms externalPlatforms =
                new Platforms();
        externalPlatforms.setSections(new ArrayList<>());

        // When
        ProjectPlatforms result = mapper.toApiModel(externalPlatforms);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSections());
        assertTrue(result.getSections().isEmpty());
    }

    @Test
    void testToApiModel_WithNullDisabledPlatforms_MapsOtherFields() {
        // Given
        Platforms externalPlatforms =
                new Platforms();
        Map<String, URI> platformLinks = new HashMap<>();
        platformLinks.put("github", URI.create("https://github.com"));
        externalPlatforms.setSections(new ArrayList<>());

        // When
        ProjectPlatforms result = mapper.toApiModel(externalPlatforms);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSections());
        assertTrue(result.getSections().isEmpty());
    }

    @Test
    void testToApiModel_WithNullSections_MapsOtherFields() {
        // Given
        Platforms externalPlatforms =
                new Platforms();
        Map<String, URI> platformLinks = new HashMap<>();
        platformLinks.put("github", URI.create("https://github.com"));
        externalPlatforms.setSections(null);

        // When
        ProjectPlatforms result = mapper.toApiModel(externalPlatforms);

        // Then
        assertNotNull(result);
        assertTrue(result.getSections().isEmpty());
    }

    @Test
    void testToApiModel_WithEmptyCollections_ReturnsEmptyCollections() {
        // Given
        Platforms externalPlatforms =
                new Platforms();
        externalPlatforms.setSections(new ArrayList<>());

        // When
        ProjectPlatforms result = mapper.toApiModel(externalPlatforms);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSections());
        assertTrue(result.getSections().isEmpty());
    }

    @Test
    void testToApiModel_WithSectionContainingNullLinks_HandlesGracefully() {
        // Given
        Platforms externalPlatforms =
                new Platforms();

        PlatformSection section =
                new PlatformSection();
        section.setSection("Development");
        section.setLinks(null);

        externalPlatforms.setSections(List.of(section));

        // When
        ProjectPlatforms result = mapper.toApiModel(externalPlatforms);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSections());
        assertEquals(1, result.getSections().size());
        assertEquals("Development", result.getSections().getFirst().getSection());
        assertTrue(result.getSections().getFirst().getLinks().isEmpty());
    }

    @Test
    void testToApiModel_WithSectionContainingEmptyLinks_ReturnsEmptyLinks() {
        // Given
        Platforms externalPlatforms =
                new Platforms();

        PlatformSection section =
                new PlatformSection();
        section.setSection("Development");
        section.setLinks(new ArrayList<>());

        externalPlatforms.setSections(List.of(section));

        // When
        ProjectPlatforms result = mapper.toApiModel(externalPlatforms);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSections());
        assertEquals(1, result.getSections().size());
        assertEquals("Development", result.getSections().getFirst().getSection());
        assertNotNull(result.getSections().getFirst().getLinks());
        assertTrue(result.getSections().getFirst().getLinks().isEmpty());
    }

    @Test
    void testToApiModel_WithSectionsContainingNullSection_HandlesGracefully() {
        // Given
        Platforms externalPlatforms =
                new Platforms();

        List<PlatformSection> sections =
                new ArrayList<>();
        sections.add(null);

        externalPlatforms.setSections(sections);

        // When
        ProjectPlatforms result = mapper.toApiModel(externalPlatforms);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSections());
        assertEquals(1, result.getSections().size());
        assertNull(result.getSections().getFirst());
    }

    @Test
    void testToApiModel_WithLinksContainingNullLink_HandlesGracefully() {
        // Given
        Platforms externalPlatforms =
                new Platforms();

        PlatformSection section =
                new PlatformSection();
        section.setSection("Development");

        List<PlatformSectionLink> links =
                new ArrayList<>();
        links.add(null);
        section.setLinks(links);

        externalPlatforms.setSections(List.of(section));

        // When
        ProjectPlatforms result = mapper.toApiModel(externalPlatforms);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSections());
        assertEquals(1, result.getSections().size());
        assertEquals("Development", result.getSections().getFirst().getSection());
        assertNotNull(result.getSections().getFirst().getLinks());
        assertEquals(1, result.getSections().getFirst().getLinks().size());
        assertNull(result.getSections().getFirst().getLinks().getFirst());
    }

    @Test
    void testToApiModel_WithMultipleSectionsAndLinks_MapsCorrectly() {
        // Given
        Platforms externalPlatforms =
                createCompleteExternalProjectPlatforms();

        // When
        ProjectPlatforms result = mapper.toApiModel(externalPlatforms);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSections());
        assertEquals(2, result.getSections().size());

        // Verify all links are properly mapped
        assertEquals(2, result.getSections().getFirst().getLinks().size());
        assertEquals(1, result.getSections().get(1).getLinks().size());
    }

    // Helper method to create a complete external ProjectPlatforms object
    private Platforms createCompleteExternalProjectPlatforms() {
        Platforms externalPlatforms =
                new Platforms();

        // Setup sections
        List<PlatformSection> sections = new ArrayList<>();

        // First section with 2 links
        PlatformSection section1 =
                new PlatformSection();
        section1.setSection("Development");

        List<PlatformSectionLink> links1 = new ArrayList<>();

        PlatformSectionLink link1 =
                new PlatformSectionLink();
        link1.setLabel("GitHub Repo");
        link1.setUrl("https://github.com/repo");
        links1.add(link1);

        PlatformSectionLink link2 =
                new PlatformSectionLink();
        link2.setLabel("Jira Board");
        link2.setUrl("https://jira.com/board");
        links1.add(link2);

        section1.setLinks(links1);
        sections.add(section1);

        // Second section with 1 link
        PlatformSection section2 =
                new PlatformSection();
        section2.setSection("CI/CD");

        List<PlatformSectionLink> links2 = new ArrayList<>();

        PlatformSectionLink link3 =
                new PlatformSectionLink();
        link3.setLabel("Jenkins");
        link3.setUrl("https://jenkins.com/job");
        links2.add(link3);

        section2.setLinks(links2);
        sections.add(section2);

        externalPlatforms.setSections(sections);

        return externalPlatforms;
    }
}

