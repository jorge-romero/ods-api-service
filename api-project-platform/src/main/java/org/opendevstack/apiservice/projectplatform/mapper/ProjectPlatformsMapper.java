package org.opendevstack.apiservice.projectplatform.mapper;

import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformSection;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformSectionLink;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.projectplatform.model.Link;
import org.opendevstack.apiservice.projectplatform.model.ProjectPlatforms;
import org.opendevstack.apiservice.projectplatform.model.Section;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper class for converting between external service and API models.
 */
@Component
public class ProjectPlatformsMapper {

    /**
     * Converts external service ProjectPlatforms to API ProjectPlatforms.
     *
     * @param externalPlatforms the external service ProjectPlatforms
     * @return the API ProjectPlatforms
     */
    public ProjectPlatforms toApiModel(Platforms externalPlatforms) {

        if (externalPlatforms == null) {
            return null;
        }

        ProjectPlatforms apiPlatforms = new ProjectPlatforms();

        // Map sections
        if (externalPlatforms.getSections() != null) {
            apiPlatforms.setSections(
                    externalPlatforms.getSections().stream()
                            .map(this::toApiSection)
                            .collect(Collectors.toList())
            );
        }

        return apiPlatforms;
    }

    /**
     * Converts external service Section to API Section.
     *
     * @param externalSection the external service ProjectPlatformSection
     * @return the API Section
     */
    private Section toApiSection(PlatformSection externalSection) {

        if (externalSection == null) {
            return null;
        }

        Section apiSection = new Section();
        apiSection.setSection(externalSection.getSection());
        apiSection.setTooltip(externalSection.getTooltip());

        // Map links
        if (externalSection.getLinks() != null) {
            apiSection.setLinks(
                    externalSection.getLinks().stream()
                            .map(this::toApiLink)
                            .collect(Collectors.toList())
            );
        }

        return apiSection;
    }

    /**
     * Converts external service Link to API Link.
     *
     * @param externalLink the external service ProjectPlatformSectionLink
     * @return the API Link
     */
    private Link toApiLink(PlatformSectionLink externalLink) {

        if (externalLink == null) {
            return null;
        }

        return new Link(externalLink.getLabel(), externalLink.getUrl(), externalLink.getTooltip(), externalLink.getType(), externalLink.getAbbreviation(), externalLink.getDisabled());
    }
}

