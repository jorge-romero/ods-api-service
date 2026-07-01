package org.opendevstack.apiservice.project.facade.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreationCommand {

    private String projectKey;
    
    private String projectName;
    
    private String projectDescription;
    
    private String projectFlavor;
    
    private String configurationItem;
    
    private String location;
    
    private String x2OdsAccount;
    
    private String owner;
    
    private UUID clientId;
}