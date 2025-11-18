package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import lombok.*;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PlatformsWithTitle {
    private String title;
    private Map<String, Platform> platforms;
}
