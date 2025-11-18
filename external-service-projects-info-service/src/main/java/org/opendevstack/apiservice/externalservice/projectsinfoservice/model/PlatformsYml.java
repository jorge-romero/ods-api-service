package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlatformsYml {
    private String title;
    private List<Platform> platforms;
    private List<PlatformSectionService> sections;
}