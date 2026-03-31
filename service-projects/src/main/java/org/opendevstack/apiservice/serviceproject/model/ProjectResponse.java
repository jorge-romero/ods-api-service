package org.opendevstack.apiservice.serviceproject.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    
    private UUID projectId;
    
    private String projectKey;
    
    private Status status;
    
    private String projectFlavor;
    
    private String message;
    
    private String error;
    
    private String errorKey;
}
