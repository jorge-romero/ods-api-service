package org.opendevstack.apiservice.externalservice.projectsinfoservice.dto;

import java.util.List;

public class ProjectInfoMother {

    public static ProjectInfo of() {
        return of("mother-project-key");
    }

    public static ProjectInfo of(String projectKey) {
        return ProjectInfo.builder()
                .projectKey(projectKey)
                .clusters(List.of("mother-cluster-1", "mother-cluster-2"))
                .build();
    }
}
