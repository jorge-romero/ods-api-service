package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

public class OpenshiftProjectClusterMother {

    public static OpenshiftProjectCluster of() {
        return OpenshiftProjectCluster.builder()
                .project("mother-project-key")
                .cluster("mother-cluster")
                .build();
    }
}
