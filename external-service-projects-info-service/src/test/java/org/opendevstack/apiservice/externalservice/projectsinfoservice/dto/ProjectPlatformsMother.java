package org.opendevstack.apiservice.externalservice.projectsinfoservice.dto;

import java.util.List;

public class ProjectPlatformsMother {

    public static ProjectPlatforms of(List<Section> sections) {
        return ProjectPlatforms.builder()
                .sections(sections)
                .build();
    }
}
