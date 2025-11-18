package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import java.util.Collections;
import java.util.Map;

public class PlatformsWithTitleMother {

    public static final String DEFAULT_TITLE = "Simple title";

    public static PlatformsWithTitle of() {
        return of(DEFAULT_TITLE);
    }

    public static PlatformsWithTitle of(String title) {
        return PlatformsWithTitle.builder()
                .title(title)
                .platforms(Collections.emptyMap())
                .build();
    }

    public static PlatformsWithTitle of(Map<String, Platform> platforms) {
        return PlatformsWithTitle.builder()
                .title(DEFAULT_TITLE)
                .platforms(platforms)
                .build();
    }
}
