package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;

/**
 * Represents the platforms information associated to a project.
 */
@Data
public class Platforms {

    private List<PlatformSection> sections;

    /**
     * Creates a ProjectPlatforms from a raw map response.
     *
     * @param responseBody the raw map containing platforms data
     * @return a new ProjectPlatforms instance
     */
    public static Platforms fromMap(Map<String, Object> responseBody) {
        Platforms platforms = new Platforms();

        // Map sections
        if (responseBody.containsKey("sections")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawSections = (List<Map<String, Object>>) responseBody.get("sections");
            List<PlatformSection> sections = rawSections.stream()
                    .map(PlatformSection::fromMap)
                    .collect(Collectors.toList());
            platforms.setSections(sections);
        }

        return platforms;
    }
}
