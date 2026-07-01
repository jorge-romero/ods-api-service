package org.opendevstack.apiservice.serviceproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
    
    private UUID projectId;

    private String projectKey;

    private String projectName;

    private String projectDescription;

    private String projectFlavor;

    private String configurationItem;

    private String location;

    private String x2OdsAccount;

    private String owner;   
    
    private UUID clientId;
    
    private Status status;
}
