package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.dto.Section;

/**
 * Represents a section of links from a specific platform associated to a project.
 */
@Data
public class PlatformSection {

    private String section;
    private String tooltip;
    private List<PlatformSectionLink> links;

    public PlatformSection() {
        // default constructor
    }

    public PlatformSection(Section section) {
        this.section = section.getSection();
        this.tooltip = section.getTooltip();
        this.links = section.getLinks().stream()
                .map(PlatformSectionLink::new)
                .toList();
    }

    /**
     * Creates a ProjectPlatformSection from a raw map.
     *
     * @param rawSection the raw map containing section data
     * @return a new ProjectPlatformSection instance
     */
    public static PlatformSection fromMap(Map<String, Object> rawSection) {
        PlatformSection section = new PlatformSection();

        if (rawSection.containsKey("section")) {
            section.setSection((String) rawSection.get("section"));
        }

        if (rawSection.containsKey("tooltip")) {
            section.setTooltip((String) rawSection.get("tooltip"));
        }

        if (rawSection.containsKey("links")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawLinks = (List<Map<String, Object>>) rawSection.get("links");
            List<PlatformSectionLink> links = rawLinks.stream()
                    .map(PlatformSectionLink::fromMap)
                    .collect(Collectors.toList());
            section.setLinks(links);
        }

        return section;
    }
}
