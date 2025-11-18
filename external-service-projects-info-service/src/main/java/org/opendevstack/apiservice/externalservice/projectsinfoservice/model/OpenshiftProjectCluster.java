package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class OpenshiftProjectCluster {
    String project;
    String cluster;
}
